package com.hepl.AppMailJava;

import com.hepl.AppMailJava.Model.Email;
import com.hepl.AppMailJava.Model.Model;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {
    @FXML
    private Label LabelFrom;
    @FXML
    private TextField InputTo;
    @FXML
    private TextField InputSubject;
    @FXML
    private TextArea TextAreaMailContent;
    @FXML
    private Button ButtonAttachments;
    @FXML
    private Tab TabMailbox;
    @FXML
    private TableView TableViewMails;
    @FXML
    private TableColumn<Email, String> ColumnFrom;
    @FXML
    private TableColumn<Email, String> ColumnReceivedDate;
    @FXML
    private TableColumn<Email, String> ColumnSubject;
    @FXML
    private ImageView loadingImage;
    @FXML
    private Label LabelFile;
    @FXML
    private Label LabelImg;


    private Model model;
    private static final int DELAY_REFRESH = 30;
    private ScheduledExecutorService threadRefresh;

    public void initialize() {
        model = Model.getInstance();
        LabelFrom.setText("From " + model.getUsername());
        threadRefresh = Executors.newSingleThreadScheduledExecutor();
        threadRefresh.scheduleWithFixedDelay(() -> onRefreshClick(), 0, DELAY_REFRESH, TimeUnit.SECONDS);
    }

    public void closeThread() {
        threadRefresh.shutdown();
    }

    @FXML
    protected void onSendClick() {
        if (InputTo.getText().isEmpty() || TextAreaMailContent.getText().isEmpty()) {
            Model.createTempStage("Please fill To and Mail Content fields", "Missing arguments", true).show();
            return;
        }
        if (InputSubject.getText().isEmpty() && !ConfirmationDialog("Missing subject", "Do you really want to send this message without subject?"))
            return;

        if (model.sendMail(InputTo.getText(), InputSubject.getText(), TextAreaMailContent.getText())) {
            InputSubject.clear();
            InputTo.clear();
            TextAreaMailContent.clear();
        }
    }

    @FXML
    protected void onAttachmentsClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attachments FileChooser");


        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
        try {
            File selectedFile = fileChooser.showOpenDialog((Stage) ButtonAttachments.getScene().getWindow());
            if (selectedFile != null) {
                model.setAttachment(selectedFile);
                LabelFile.setText(selectedFile.getName());
            }
        } catch (Exception e) {

        }
    }

    @FXML
    protected void onImgClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Image FileChooser");
        FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("Fichiers JPG (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter gifFilter = new FileChooser.ExtensionFilter("Fichiers GIF (*.gif)", "*.gif");
        fileChooser.getExtensionFilters().addAll(jpgFilter, gifFilter);
        try {
            File selectedFile = fileChooser.showOpenDialog((Stage) ButtonAttachments.getScene().getWindow());
            if (selectedFile != null) {
                model.setImage(selectedFile);
                LabelImg.setText(selectedFile.getName());
            }
        } catch (Exception e) {

        }
    }

    @FXML
    protected void onRefreshClick() {
        new Thread(() -> {
            Platform.runLater(() -> loadingImage.setImage(new Image(getClass().getResource("/images/circle-loader.gif").toExternalForm())));
            ArrayList<Email> mails = model.getMails();
            if (mails == null) {
                Platform.runLater(() -> loadingImage.setImage(null));
                return;
            }
            Platform.runLater(() -> {
                TableViewMails.getItems().setAll(mails);
            });

            ColumnFrom.setCellValueFactory(c -> c.getValue().fromProperty());
            ColumnReceivedDate.setCellValueFactory(c -> c.getValue().receivedDateProperty());
            ColumnSubject.setCellValueFactory(c -> c.getValue().subjectProperty());
            Platform.runLater(() -> loadingImage.setImage(null));
        }).start();
    }

    @FXML
    protected void onSelectionMailbox() {
        if (TabMailbox.isSelected())
            onRefreshClick();
    }

    public static boolean ConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initStyle(StageStyle.UTILITY);

        ButtonType buttonTypeOK = new ButtonType("Send");
        ButtonType buttonTypeCancel = new ButtonType("Not Send", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);

        alert.showAndWait();
        return alert.getResult() == buttonTypeOK;
    }

    @FXML
    protected void onDisplayClick() {
        int index = TableViewMails.getSelectionModel().getSelectedIndex();
        if (index == -1)
            return;
        String emailContent = null;
        Object content = model.getContentOfMessageAt(index);
        try {
            if (content instanceof String) {
                emailContent = (String) content;
            } else if (content instanceof Multipart) {
                Multipart msgMP = (Multipart) content;
                BodyPart bodyPart;
                for (int i = 0; i < msgMP.getCount(); i++) {
                    bodyPart = ((Multipart) content).getBodyPart(i);
                    if (bodyPart.isMimeType("text/plain"))
                        emailContent = (String) bodyPart.getContent();
                }
            } else {
                throw new UnsupportedOperationException("Unhandle content type : " + content.getClass());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (emailContent == null)
            return;

        displayWindow("Email content", emailContent);
    }

    @FXML
    protected void onDisplayHeader(){
        int index = TableViewMails.getSelectionModel().getSelectedIndex();
        if (index == -1)
            return;

        displayWindow("Header", model.getHeaderOfMessageAt(index));
    }

    private static void displayWindow(String title, String content){
        Stage emailWindow = new Stage();
        emailWindow.initModality(Modality.APPLICATION_MODAL);
        emailWindow.setTitle(title);
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        VBox layout = new VBox(10);
        layout.getChildren().addAll(textArea);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 600, 400);
        textArea.prefHeightProperty().bind(scene.heightProperty());
        emailWindow.setScene(scene);
        emailWindow.showAndWait();
    }

    @FXML
    protected void onResetClick() {
        InputTo.clear();
        InputSubject.clear();
        TextAreaMailContent.clear();
        model.resetAttachment();
        LabelFile.setText("");
        LabelImg.setText("");
    }
}