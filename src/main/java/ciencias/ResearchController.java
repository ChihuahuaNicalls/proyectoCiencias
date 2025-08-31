package ciencias;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class ResearchController {

    @FXML
    private MenuButton menuButtonSecuencial;
    @FXML
    private MenuButton menuButtonBinaria;
    @FXML
    private MenuButton menuButtonHash;
    @FXML
    private MenuButton menuButtonResiduos;
    @FXML
    private MenuButton menuButtonRangos;

    @FXML
    private Pane paneHash;

    @FXML
    private Text textHashMenu;

    private List<MenuButton> menuButtons;

    @FXML
    public void initialize() {
        menuButtons = Arrays.asList(
                menuButtonSecuencial,
                menuButtonBinaria,
                menuButtonHash,
                menuButtonResiduos,
                menuButtonRangos);
    }

    private void cargarVistaHash(String fxml) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(fxml));
            paneHash.getChildren().setAll(vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void menu() throws IOException {
        App.setRoot("start");
    }

    @FXML
    private void restart() {
        if (menuButtons != null) {
            menuButtons.forEach(button -> {
                if (button != null) {
                    button.setText("Elegir");
                }
            });
        }
        if (paneHash!= null) {
            paneHash.getChildren().clear();
        }
        if (textHashMenu != null) {
            textHashMenu.setText(null);
        }
    }

    @FXML
    private void handleSecuencialAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonSecuencial.setText(option);
    }

    @FXML
    private void handleBinariaAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonBinaria.setText(option);
    }

    @FXML
    private void handleHashAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonHash.setText(option);

        switch (option) {
            case "Dispersion":
                textHashMenu.setText(
                        "Es el término general para transformar una clave (como un número o cadena) en una dirección de tabla usando una función hash. \n"
                                +
                                "La idea es que la clave se “disperse” lo más uniformemente posible sobre el espacio de direcciones, reduciendo colisiones.");
                cargarVistaHash("/ciencias/Hash/dispersion.fxml");
                break;
            case "Modulo":
                textHashMenu.setText(
                        "El método de hash por módulo utiliza la operación de módulo para determinar la dirección de la tabla. \n"
                                +
                                "Esto implica tomar el valor hash de la clave y calcular el resto de dividirlo por el tamaño de la tabla.");
                cargarVistaHash("/ciencias/Hash/modulo.fxml");
                break;
            case "Cuadrada":
                textHashMenu.setText(
                        "El método de hash cuadrado utiliza la técnica de tomar el valor cuadrado de la clave y luego extraer una parte de ese cuadrado como la dirección de la tabla.");
                cargarVistaHash("/ciencias/Hash/cuadrada.fxml");
                break;
            case "Truncamiento":
                textHashMenu.setText(
                        "El método de hash por truncamiento implica tomar el valor hash de la clave y luego truncarlo a un tamaño específico para que se ajuste a la tabla hash.");
                cargarVistaHash("/ciencias/Hash/truncamiento.fxml");
                break;
            case "Plegamiento":
                textHashMenu.setText(
                        "El método de hash por plegamiento implica dividir la clave en varias partes y luego sumar esas partes para obtener la dirección de la tabla hash.");
                cargarVistaHash("/ciencias/Hash/plegamiento.fxml");
                break;
        }
    }

    @FXML
    private void handleResiduosAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonResiduos.setText(option);
    }

    @FXML
    private void handleRangosAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonRangos.setText(option);
    }

}
