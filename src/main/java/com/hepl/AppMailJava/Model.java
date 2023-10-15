package com.hepl.AppMailJava;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class Model {
    //Variables---------------------------------------------------------------------------------------------------------
    private static Model instance;
    private Session session;
    private Properties props;
    private String username;
    private String password;

    //Singleton constructor---------------------------------------------------------------------------------------------
    private Model() {
        setupPropsGmail();
        setLoginInfos("cyril.russe@gmail.com", "tfkfyfmhaesewmwp");
        setSession();

    }

    public static Model getInstance() {
        if (instance == null)
            instance = new Model();
        return instance;
    }


    //Private methods---------------------------------------------------------------------------------------------------
    private void setupPropsGmail() {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
    }

    private void setSession() {
        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                }
        );
    }

    public static Stage createTempStage(String message, boolean closable) {
        Stage stage = new Stage();
        stage.initOwner(null);  // Pour qu'il soit indépendant de la fenêtre principale
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);


        StackPane layout = new StackPane();
        layout.getChildren().add(new Label(message));
        if (!closable) {
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setOnCloseRequest(Event::consume);
            layout.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1px;"); // Ajoute une bordure noire
        }

        stage.setScene(new Scene(layout, 200, 100));
        return stage;
    }

    //Setters and Getters-----------------------------------------------------------------------------------------------
    public void setLoginInfos(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername(){
        return username;
    }

    //Methods-----------------------------------------------------------------------------------------------------------
    public void sendMail(String to, String subject, String text, String attachments) {
        Stage sendingStage = createTempStage("Sending message...", false);
        Stage successStage = createTempStage("Message sent successfully", true);
        Stage errorStage = createTempStage("Error. Message impossible to send", true);
        Message msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(username));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(text);
            sendingStage.show();
        } catch (Exception e) {
            errorStage.show();
            return;
        }
        new Thread(() -> {
            try {
                Transport.send(msg);
                Platform.runLater(() -> sendingStage.close());
                Platform.runLater(() -> successStage.show());
            } catch (Exception e) {
                Platform.runLater(() -> errorStage.show());
                System.out.println("Message Error : " + e.getMessage());
            }
        }
        ).start();
    }

    public void sendMail(String to, String subject, String text) {
        sendMail(to, subject, text, null);
    }
}
