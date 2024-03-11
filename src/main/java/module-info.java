module com.myprojects.weather {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.net.http;


    opens com.myprojects.weather to javafx.fxml;
    exports com.myprojects.weather;
}