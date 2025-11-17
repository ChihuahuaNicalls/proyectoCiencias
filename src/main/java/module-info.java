
module ciencias {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens ciencias to javafx.fxml;
    opens ciencias.Research to javafx.fxml;
    opens ciencias.Graphs to javafx.fxml;
    exports ciencias;
    exports ciencias.Research;
    exports ciencias.Graphs to javafx.fxml;
}
