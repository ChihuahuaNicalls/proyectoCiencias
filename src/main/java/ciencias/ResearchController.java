package ciencias;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ciencias.Research.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
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
    private Pane paneBinaria;
    @FXML
    private Pane paneSecuencial;
    @FXML
    private Pane paneIndexada;

    @FXML
    private Text textHashMenu;

    @FXML
    private Tab tabIndexada;

    private List<MenuButton> menuButtons;
    private List<Pane> panes;

    @FXML
    public void initialize() {
        menuButtons = Arrays.asList(
                menuButtonSecuencial,
                menuButtonBinaria,
                menuButtonHash,
                menuButtonResiduos,
                menuButtonRangos);
        panes = Arrays.asList(
                paneHash,
                paneBinaria,
                paneSecuencial,
                paneIndexada);
        tabIndexada.setOnSelectionChanged(event -> {
            if (tabIndexada.isSelected()) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Indexada.fxml"));
                    Parent vista = loader.load();

                    IndexadaController indexadaController = loader.getController();
                    indexadaController.setResearchController(this);
                    indexadaController.initData();

                    paneIndexada.getChildren().setAll(vista);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
        if (panes != null) {
            panes.forEach(pane -> {
                if (pane != null) {
                    pane.getChildren().clear();
                }
            });
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("secuencial.fxml"));
            Parent vista = loader.load();

            SecuencialController secuencialController = loader.getController();

            secuencialController.setResearchController(this);

            secuencialController.initData();

            paneSecuencial.getChildren().setAll(vista);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBinariaAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonBinaria.setText(option);
        menuButtonSecuencial.setText(option);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("binaria.fxml"));
            Parent vista = loader.load();

            BinariaController binariaController = loader.getController();

            binariaController.setResearchController(this);

            binariaController.initData();

            paneBinaria.getChildren().setAll(vista);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHashAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonHash.setText(option);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hash.fxml"));
            Parent vista = loader.load();

            HashController hashController = loader.getController();

            hashController.setResearchController(this);

            hashController.initData();

            paneHash.getChildren().setAll(vista);

        } catch (IOException e) {
            e.printStackTrace();
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

    @FXML
    public String getHashString() {
        return menuButtonHash.getText();
    }

}
