package ciencias.Research;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

public class HashController {
    @FXML TextField lengthArray;
    @FXML TextField newItemArray;


    @FXML
    private void initialize() {
        lengthArray.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        newItemArray.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }

    @FXML
    private void crearTabla() {
    }

    @FXML
    private void reiniciar() {
    }

    @FXML
    private void addToArrayDispersion() {
    }

    @FXML
    private void addToArrayModulo() {
    }

    @FXML
    private void addToArrayCuadrada() {
    }

    @FXML
    private void addToArrayTruncamiento() {
    }

    @FXML
    private void addToArrayPlegamiento() {
    }

    @FXML
    private void agregarPosTrunc() {
    }
}
