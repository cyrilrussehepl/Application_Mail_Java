module hepl.mailproject.application_mail_java {
    requires javafx.controls;
    requires javafx.fxml;


    opens hepl.mailproject.AppMailJava to javafx.fxml;
    exports hepl.mailproject.AppMailJava;
}