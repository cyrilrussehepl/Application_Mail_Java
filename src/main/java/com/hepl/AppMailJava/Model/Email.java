package com.hepl.AppMailJava.Model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Email {
    public String from;
    public String subject;
    public Date receivedDate;

    public Email(String from, String subject, Date receivedDate) {
        this.from = from;
        this.subject = subject;
        this.receivedDate = receivedDate;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public StringProperty fromProperty() {
        return new SimpleStringProperty(this.from);
    }

    public StringProperty receivedDateProperty() {
        return new SimpleStringProperty(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(this.receivedDate));
    }

    public StringProperty subjectProperty() {
        return new SimpleStringProperty(this.subject);
    }

}
