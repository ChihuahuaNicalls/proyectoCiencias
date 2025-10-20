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
    private Pane paneHashInt;
    @FXML
    private Pane paneBinariaInt;
    @FXML
    private Pane paneSecuencialInt;
    @FXML
    private Pane paneHashExt;
    @FXML
    private Pane paneBinariaExt;
    @FXML
    private Pane paneSecuencialExt;
    @FXML
    private Pane paneDynamic;
    @FXML
    private Pane paneTrees;

    @FXML
    private Tab tabBinaria;
    @FXML
    private Tab tabSecuencial;
    @FXML
    private Tab tabSecuencialExt;
    @FXML
    private Tab tabBinariaExt;

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

            paneSecuencialInt.getChildren().setAll(vista);
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
                paneHashInt,
                paneBinariaInt,
                paneSecuencialInt,
                paneHashExt,
                paneBinariaExt,
                paneSecuencialExt,
                paneDynamic,
                paneTrees);
        tabBinaria.setOnSelectionChanged(event -> {
            if (tabBinaria.isSelected()) {
                try {
                    restart();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("binaria.fxml"));
                    Parent vista = loader.load();

                    paneBinariaInt.getChildren().setAll(vista);
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
                    paneSecuencialInt.getChildren().setAll(vista);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        tabBinariaExt.setOnSelectionChanged(event -> {
            if (tabBinariaExt.isSelected()) {
                try {
                    restart();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("binariaExt.fxml"));
                    Parent vista = loader.load();

                    paneBinariaExt.getChildren().setAll(vista);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        tabSecuencialExt.setOnSelectionChanged(event -> {
            if (tabSecuencialExt.isSelected()) {
                try {
                    restart();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("secuencialExt.fxml"));
                    Parent vista = loader.load();

                    paneSecuencialExt.getChildren().setAll(vista);
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
            if (paneSecuencialInt == null) {
                System.err.println("Error: paneSecuencial es null");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("secuencial.fxml"));
            Parent vista = loader.load();

            paneSecuencialInt.getChildren().clear();
            paneSecuencialInt.getChildren().add(vista);

            tabPaneInternal.getSelectionModel().select(0);

        } catch (IOException e) {
            System.err.println("Error cargando secuencial.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void external() {
        restart();
        try {
            if (paneSecuencialExt == null) {
                System.err.println("Error: paneSecuencial es null");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("secuencialExt.fxml"));
            Parent vista = loader.load();

            paneSecuencialExt.getChildren().clear();
            paneSecuencialExt.getChildren().add(vista);

            tabPaneExternal.getSelectionModel().select(0);

        } catch (IOException e) {
            System.err.println("Error cargando secuencialExt.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void index(){
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

            paneHashInt.getChildren().setAll(vista);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHashExternalAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonHash.setText(option);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hashExt.fxml"));
            Parent vista = loader.load();

            HashControllerExternal hashControllerExternal = loader.getController();

            hashControllerExternal.setResearchController(this);

            hashControllerExternal.initData();

            paneHashExt.getChildren().setAll(vista);

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
