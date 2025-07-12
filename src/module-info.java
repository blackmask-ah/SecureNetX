module SecureNetX {
    requires javafx.controls;
    requires javafx.fxml;

    opens application to javafx.fxml, javafx.base;
    exports application;
}
