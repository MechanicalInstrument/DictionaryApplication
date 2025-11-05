module com.abi.fx.tutorial.dictionaryapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.oracle.database.jdbc;
    requires java.sql;

    opens com.abi.fx.tutorial.dictionaryapplication to javafx.fxml;
    exports com.abi.fx.tutorial.dictionaryapplication;
}