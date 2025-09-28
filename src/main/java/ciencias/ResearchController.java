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
import javafx.scene.control.TabPane;
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
    private MenuButton menuButtonTrees;

    @FXML
    private Pane paneHash;
    @FXML
    private Pane paneBinaria;
    @FXML
    private Pane paneSecuencial;
    @FXML
    private Pane paneIndexada;
    @FXML
    private Pane paneTrees;
    @FXML
    private Pane paneResiduos;

    @FXML
    private Tab tabBinaria;
    @FXML
    private Tab tabSecuencial;

    @FXML
    private TabPane tabPaneInternal;
    @FXML
    private TabPane tabPaneExternal;

    @FXML
    private Text textHashMenu;

    private List<MenuButton> menuButtons;
    private List<Pane> panes;

    @FXML
    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("secuencial.fxml"));
            Parent vista = loader.load();

            paneSecuencial.getChildren().setAll(vista);
            restart();
        } catch (IOException e) {
            e.printStackTrace();
        }

        menuButtons = Arrays.asList(
                menuButtonSecuencial,
                menuButtonBinaria,
                menuButtonHash,
                menuButtonResiduos,
                menuButtonTrees);
        panes = Arrays.asList(
                paneHash,
                paneBinaria,
                paneSecuencial);
        tabBinaria.setOnSelectionChanged(event -> {
            if (tabBinaria.isSelected()) {
                try {
                    restart();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("binaria.fxml"));
                    Parent vista = loader.load();

                    paneBinaria.getChildren().setAll(vista);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        tabSecuencial.setOnSelectionChanged(event -> {
            if (tabSecuencial.isSelected()) {
                try {
                    restart();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("secuencial.fxml"));
                    Parent vista = loader.load();
                    paneSecuencial.getChildren().setAll(vista);
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
    private void internal() {
        restart();

        try {
            if (paneSecuencial == null) {
                System.err.println("Error: paneSecuencial es null");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("secuencial.fxml"));
            Parent vista = loader.load();

            paneSecuencial.getChildren().clear();
            paneSecuencial.getChildren().add(vista);

            tabPaneInternal.getSelectionModel().select(0);

        } catch (IOException e) {
            System.err.println("Error cargando secuencial.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void external() {
        restart();
        tabPaneExternal.getSelectionModel().select(0);
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
    private void handleTreesAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonTrees.setText(option);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("trees.fxml"));
            Parent vista = loader.load();

            TreesController treesController = loader.getController();
            treesController.setResearchController(this);
            treesController.initData();
            paneTrees.getChildren().setAll(vista);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public String getHashString() {
        return menuButtonHash.getText();
    }

    @FXML
    public String getTreesString() {
        return menuButtonTrees.getText();
    }

}
