module hepl.mailproject.application_mail_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;
    requires activation;

    opens com.hepl.AppMailJava to javafx.fxml;
    exports com.hepl.AppMailJava;
    exports com.hepl.AppMailJava.Model;
    opens com.hepl.AppMailJava.Model to javafx.fxml;
}