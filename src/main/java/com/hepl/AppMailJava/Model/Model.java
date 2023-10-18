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
import javax.mail.search.FromTerm;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Model {
    //Variables---------------------------------------------------------------------------------------------------------
    private static Model instance;
    private Session session;
    private Properties props;
    private String username;
    private String password;
    private File attachment;
    private File image;
    private ArrayList<Email> mails;

    //Singleton constructor---------------------------------------------------------------------------------------------
    private Model() {
        setupPropsGmail();
        setLoginInfos("cyril.russe@gmail.com", "tfkfyfmhaesewmwp");
        setSession();
        mails = new ArrayList<>();
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

    public static Stage createTempStage(String message, String title, boolean closable) {
        Stage stage = new Stage();
        stage.initOwner(null);
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

    public void setImage(File image){
        this.image = image;
    }

    public synchronized Object getContentOfMessageAt(int index) {
        return mails.get(index).getContent();
    }

    public synchronized String getHeaderOfMessageAt(int index){return mails.get(index).getHeader();}

    public void resetAttachment() {
        this.attachment = null;
        this.image = null;
    }

    //Methods-----------------------------------------------------------------------------------------------------------

    public boolean sendMail(String to, String subject, String text) {
        Stage sendingStage = createTempStage("Sending message...",
                null,
                false);
        Stage successStage = createTempStage("Message sent successfully",
                "Success",
                true);
        Stage errorStage = createTempStage("Unable to send the message. An error occurred.\nPlease verify the recipient's email address and try again.",
                "Error",
                true);
        Message msg = new MimeMessage(session);
        AtomicBoolean success = new AtomicBoolean(false);

        try {
            msg.setFrom(new InternetAddress(username));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);

            if (attachment != null || image != null) {
                MimeMultipart msgMP = new MimeMultipart();
                MimeBodyPart msgBP = new MimeBodyPart();
                msgBP.setText(text);
                msgMP.addBodyPart(msgBP);

                if (attachment != null) {
                    msgBP = new MimeBodyPart();
                    DataSource so = new FileDataSource(attachment.getAbsolutePath());
                    msgBP.setDataHandler(new DataHandler(so));
                    msgBP.setFileName(attachment.getAbsolutePath());
                    msgMP.addBodyPart(msgBP);
                }

                if (image != null) {
                    msgBP = new MimeBodyPart();
                    DataSource so = new FileDataSource(image.getAbsolutePath());
                    msgBP.setDataHandler(new DataHandler(so));
                    msgBP.setFileName(image.getAbsolutePath());
                    msgMP.addBodyPart(msgBP);
                }
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
                Platform.runLater(() -> sendingStage.close());
                Platform.runLater(() -> errorStage.show());
                System.out.println("Message Error : " + e.getMessage());
            }
        }
        ).start();
        return success.get();
    }

    public synchronized ArrayList<Email> getMails() {
        try {
            Store store = session.getStore("imaps");
            store.connect("imap.gmail.com", username, password);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            Message messages[] = folder.search(new FromTerm(new InternetAddress("cyril.russe@hotmail.com")));
            mails.clear();
            String header;
            Enumeration e;
            for (int i = messages.length - 1; i >= 0; i--){
                e = messages[i].getAllHeaders();
                Header h = (Header)e.nextElement();
                header = new String();
                while (e.hasMoreElements()) {
                    header += h.getName() + " -- >" + h.getValue()+"\n";
                    h = (Header)e.nextElement();
                }

                mails.add(new Email(messages[i].getFrom()[0].toString(),
                        messages[i].getSubject(),
                        messages[i].getReceivedDate(),
                        messages[i].getContent(),
                        header));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return mails;
    }
}
