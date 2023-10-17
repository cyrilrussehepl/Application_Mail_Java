package com.hepl.AppMailJava;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MailApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MailApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        Controller controller = fxmlLoader.getController();
        Image icon = new Image(getClass().getResourceAsStream("/images/mail.png"));
        stage.setTitle("MailUserAgent");
        stage.getIcons().add(icon);

        stage.setOnCloseRequest(event -> {
            controller.closeThread();
            Platform.exit();
            System.exit(0);
        });

        stage.setScene(scene);
        stage.setMinHeight(300);
        stage.setMinWidth(400);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}