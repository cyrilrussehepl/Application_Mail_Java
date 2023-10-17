package com.hepl.AppMailJava;

import com.hepl.AppMailJava.Model.Email;
import com.hepl.AppMailJava.Model.Model;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    private Model model;

    public void initialize() {
        model = Model.getInstance();
        LabelFrom.setText("From " + model.getUsername());
        ScheduledExecutorService threadRefresh = Executors.newSingleThreadScheduledExecutor();
        threadRefresh.scheduleWithFixedDelay(()->onRefreshClick(),0,15, TimeUnit.SECONDS);
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
        File selectedFile = fileChooser.showOpenDialog((Stage) ButtonAttachments.getScene().getWindow());
        model.setAttachment(selectedFile);
    }

    @FXML
    protected void onRefreshClick() {
        new Thread(() -> {
            Platform.runLater(() -> loadingImage.setImage(new Image("file:E:/1-Cyril/HEPL/B3/Réseau/JavaMail/Application_Mail_Java/src/main/resources/images/circle-loader.gif")));
            ArrayList<Email> mails = model.getMails();
            if(mails==null){
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
        alert.setHeaderText(null); // Pas de texte d'en-tête
        alert.setContentText(content);
        alert.initStyle(StageStyle.UTILITY);

        // Définir les boutons OK et Annuler
        ButtonType buttonTypeOK = new ButtonType("Send");
        ButtonType buttonTypeCancel = new ButtonType("Not Send", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeOK, buttonTypeCancel);

        // Afficher la boîte de dialogue et attendre la réponse de l'utilisateur
        alert.showAndWait();

        // Vérifier si l'utilisateur a appuyé sur le bouton OK
        return alert.getResult() == buttonTypeOK;
    }
}