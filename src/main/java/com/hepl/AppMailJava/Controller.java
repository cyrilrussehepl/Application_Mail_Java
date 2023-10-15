package com.hepl.AppMailJava;

import javafx.fxml.FXML;

public class Controller {

    @FXML
    protected void onSendClick() {
        Model.getInstance().sendMail("cyril.russe@hotmail.com", "test", "contenu");
    }
}