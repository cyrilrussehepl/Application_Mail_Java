package com.hepl.AppMailJava;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.StageStyle;

public class Controller {
    @FXML
    private Label LabelFrom;
    @FXML
    private TextField InputTo;
    @FXML
    private TextField InputSubject;
    @FXML
    private TextArea TextAreaMailContent;

    private Model model;

    public void initialize() {
        model = Model.getInstance();
        LabelFrom.setText("From " + model.getUsername());
    }

    @FXML
    protected void onSendClick() {
        if (InputTo.getText().isEmpty() || TextAreaMailContent.getText().isEmpty()) {
            Model.createTempStage("Please fill To and Mail Content fields", true).show();
            return;
        }
        if (InputSubject.getText().isEmpty() && !ConfirmationDialog("Missing subject", "Do you really want to send this message without subject?"))
            return;

        model.sendMail(InputTo.getText(), InputSubject.getText(), TextAreaMailContent.getText());
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