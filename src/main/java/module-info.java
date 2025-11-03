module com.tienda {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;
    requires lombok;
    
    opens com.tienda to javafx.fxml;
    opens com.tienda.controller to javafx.fxml;
    opens com.tienda.controller.auxiliar to javafx.fxml;
    opens com.tienda.model to javafx.base;

    exports com.tienda;
    exports com.tienda.controller;
    exports com.tienda.controller.auxiliar;
}
