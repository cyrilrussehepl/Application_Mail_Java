package com.hepl.AppMailJava.Model;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class Model {
    //Variables---------------------------------------------------------------------------------------------------------
    private static Model instance;
    private Session session;
    private Properties props;
    private String username;
    private String password;
    private File attachment;
    private Folder mails;

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
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "smtp.gmail.com");
        props.put("mail.imaps.port", "993");
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

    // Création d'une fenêtre temporaire
    // @param message : chaine de char à afficher dans la fenêtre
    // @param title : param optionnel, titre de la fenêtre
    // @param closable : booléen pour définir si c'est une fenêtre que l'utilisateur peut fermer
    // @return : objet Stage correspondant à la fenêtre temporaire créée
    public static Stage createTempStage(String message, String title, boolean closable) {
        Stage stage = new Stage();
        stage.initOwner(null);  // Pour qu'il soit indépendant de la fenêtre principale
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        if (title != null)
            stage.setTitle(title);

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

    // Setteur des infos de connexion, donc du login et mot de passe
    public void setLoginInfos(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setAttachment(File attachment) {
        this.attachment = attachment;
    }

    //Methods-----------------------------------------------------------------------------------------------------------

    // Méthode d'envoi d'email, simple si aucun attachment défini au préalable, multipart sinon
    // @param to : adresse du destinataire
    // @param subject : objet du mail
    // @param text : contenu du mail
    // @param return : true si succès de l'envoi du mail, false sinon
    public boolean sendMail(String to, String subject, String text) {
        Stage sendingStage = createTempStage("Sending message...", null, false);
        Stage successStage = createTempStage("Message sent successfully", "Success", true);
        Stage errorStage = createTempStage("Unable to send the message. An error occurred.\nPlease verify the recipient's email address and try again.", "Error", true);
        Message msg = new MimeMessage(session);
        AtomicBoolean success = new AtomicBoolean(false);

        try {
            msg.setFrom(new InternetAddress(username));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);

            //Add attachment to msg if exists
            if (attachment != null) {
                //Text
                MimeMultipart msgMP = new MimeMultipart();
                MimeBodyPart msgBP = new MimeBodyPart();
                msgBP.setText(text);
                msgMP.addBodyPart(msgBP);

                //File
                msgBP = new MimeBodyPart();
                DataSource so = new FileDataSource(attachment.getAbsolutePath());
                msgBP.setDataHandler(new DataHandler(so));
                msgBP.setFileName(attachment.getAbsolutePath());
                msgMP.addBodyPart(msgBP);

                //Image

                msg.setContent(msgMP);
            } else
                msg.setText(text);
        } catch (Exception e) {
            errorStage.show();
            return false;
        }

        sendingStage.show();

        new Thread(() -> {
            try {
                Transport.send(msg);
                success.set(true);
                Platform.runLater(() -> sendingStage.close());
                Platform.runLater(() -> successStage.show());
            } catch (Exception e) {
                Platform.runLater(() -> errorStage.show());
                System.out.println("Message Error : " + e.getMessage());
            }
        }
        ).start();
        return success.get();
    }

    public ArrayList<Email> getMails() {
        ArrayList<Email> listMail = new ArrayList<>();
        try {
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", username, password);
            mails = store.getFolder("INBOX");
            mails.open(Folder.READ_ONLY);

            int messageCount = mails.getMessageCount();
            Message[] messages = mails.getMessages(Math.max(1, messageCount - 10 + 1), messageCount);
            for (int i = messages.length - 1; i >= 0; i--)
                listMail.add(new Email(getSender(messages[i]), messages[i].getSubject(), messages[i].getReceivedDate()));

            // Fermez la boîte aux lettres
            mails.close(false);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return listMail;
    }

    private static String getSender(Message msg) throws MessagingException {
        Address[] fromAddress = msg.getFrom();
        if (fromAddress != null && fromAddress.length > 0)
            return fromAddress[0].toString();
        return "Unknown Sender";
    }

}
