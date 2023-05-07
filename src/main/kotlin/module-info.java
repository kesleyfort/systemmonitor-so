module com.dash.dashboard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.dash.dashboard to javafx.fxml;
    opens com.dash.dashboard.models to javafx.base;
    exports com.dash.dashboard;
}