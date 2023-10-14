package hepl.mailproject.AppMailJava;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MailController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}