module ni.edu.uam.proyectocrudg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens ni.edu.uam.proyectocrudg3 to javafx.fxml;
    exports ni.edu.uam.proyectocrudg3;
    opens ni.edu.uam.proyectocrudg3.connection to javafx.fxml;
    opens ni.edu.uam.proyectocrudg3.controllers to javafx.fxml;
    exports ni.edu.uam.proyectocrudg3.controllers to javafx.fxml;
}