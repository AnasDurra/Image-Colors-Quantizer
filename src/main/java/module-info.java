module com.icq.imagecolorquantizer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.icq.imagecolorquantizer to javafx.fxml;
    exports com.icq.imagecolorquantizer;
    exports com.icq.imagecolorquantizer.controller;
    opens com.icq.imagecolorquantizer.controller to javafx.fxml;
}