package ciencias;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
    private MenuButton menuButtonClaves;

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
    private Tab tabDinamicas;

    @FXML
    private TabPane tabPaneInternal;
    @FXML
    private TabPane tabPaneExternal;

    @FXML
    private Text textHashMenu;

    private List<MenuButton> menuButtons;
    private List<Pane> panes;

    private Map<Tab, Pane> tabPaneMapInternal;
    private Map<Tab, Pane> tabPaneMapExternal;
    private Map<Tab, String> fxmlMapInternal;
    private Map<Tab, String> fxmlMapExternal;

    @FXML
    public void initialize() {
        initializeMappings();
        loadInitialContent();
        setupTabListeners();
        setupMenuButtonsAndPanes();
    }

    private void initializeMappings() {

        tabPaneMapInternal = new HashMap<>();
        tabPaneMapInternal.put(tabSecuencial, paneSecuencialInt);
        tabPaneMapInternal.put(tabBinaria, paneBinariaInt);

        fxmlMapInternal = new HashMap<>();
        fxmlMapInternal.put(tabSecuencial, "secuencial.fxml");
        fxmlMapInternal.put(tabBinaria, "binaria.fxml");

        tabPaneMapExternal = new HashMap<>();
        tabPaneMapExternal.put(tabSecuencialExt, paneSecuencialExt);
        tabPaneMapExternal.put(tabBinariaExt, paneBinariaExt);

        fxmlMapExternal = new HashMap<>();
        fxmlMapExternal.put(tabSecuencialExt, "secuencialExt.fxml");
        fxmlMapExternal.put(tabBinariaExt, "binariaExt.fxml");
    }

    private void setupMenuButtonsAndPanes() {
        menuButtons = Arrays.asList(
                menuButtonSecuencial,
                menuButtonBinaria,
                menuButtonHash,
                menuButtonResiduos,
                menuButtonTrees,
                menuButtonClaves);
        panes = Arrays.asList(
                paneHashInt,
                paneBinariaInt,
                paneSecuencialInt,
                paneHashExt,
                paneBinariaExt,
                paneSecuencialExt,
                paneDynamic,
                paneTrees);
    }

    private void loadInitialContent() {

        loadTabContent(tabSecuencial, paneSecuencialInt, "secuencial.fxml");
        loadTabContent(tabBinaria, paneBinariaInt, "binaria.fxml");

        loadTabContent(tabSecuencialExt, paneSecuencialExt, "secuencialExt.fxml");
        loadTabContent(tabBinariaExt, paneBinariaExt, "binariaExt.fxml");

        tabPaneInternal.getSelectionModel().select(tabSecuencial);
        tabPaneExternal.getSelectionModel().select(tabSecuencialExt);
    }

    private void setupTabListeners() {

        tabSecuencial.setOnSelectionChanged(event -> {
            if (tabSecuencial.isSelected()) {
                softRestart();
                ensureTabContent(tabSecuencial, paneSecuencialInt, "secuencial.fxml");
            }
        });

        tabBinaria.setOnSelectionChanged(event -> {
            if (tabBinaria.isSelected()) {
                softRestart();
                ensureTabContent(tabBinaria, paneBinariaInt, "binaria.fxml");
            }
        });

        tabSecuencialExt.setOnSelectionChanged(event -> {
            if (tabSecuencialExt.isSelected()) {
                softRestart();
                ensureTabContent(tabSecuencialExt, paneSecuencialExt, "secuencialExt.fxml");
            }
        });

        tabBinariaExt.setOnSelectionChanged(event -> {
            if (tabBinariaExt.isSelected()) {
                softRestart();
                ensureTabContent(tabBinariaExt, paneBinariaExt, "binariaExt.fxml");
            }
        });

        tabDinamicas.setOnSelectionChanged(event -> {
            if (tabDinamicas.isSelected()) {
                softRestart();
                ensureTabContent(tabDinamicas, paneDynamic, "dinamicas.fxml");
            }
        });
    }

    private void loadTabContent(Tab tab, Pane pane, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent vista = loader.load();
            pane.getChildren().setAll(vista);
        } catch (IOException e) {
            System.err.println("Error cargando " + fxmlFile + " para tab " + tab.getText() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureTabContent(Tab tab, Pane pane, String fxmlFile) {
        try {

            if (pane.getChildren().isEmpty()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent vista = loader.load();
                pane.getChildren().setAll(vista);
            }
        } catch (IOException e) {
            System.err.println("Error cargando " + fxmlFile + " para tab " + tab.getText() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void menu() throws IOException {
        App.setRoot("start");
    }

    @FXML
    private void softRestart() {

        if (menuButtons != null) {
            menuButtons.forEach(button -> {
                if (button != null) {
                    button.setText("Elegir");
                }
            });
        }
        if (textHashMenu != null) {
            textHashMenu.setText(null);
        }
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
        softRestart();
        tabPaneInternal.getSelectionModel().select(tabSecuencial);

        ensureTabContent(tabSecuencial, paneSecuencialInt, "secuencial.fxml");
    }

    @FXML
    private void external() {
        softRestart();
        tabPaneExternal.getSelectionModel().select(tabSecuencialExt);

        ensureTabContent(tabSecuencialExt, paneSecuencialExt, "secuencialExt.fxml");
    }

    @FXML
    private void index() {

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
        menuButtonClaves.setText(option);
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
    public String getClavesString() {
        return menuButtonClaves.getText();
    }

    @FXML
    public String getTreesString() {
        return menuButtonTrees.getText();
    }

    public void reloadTabContent(Tab tab) {
        if (tabPaneMapInternal.containsKey(tab) && fxmlMapInternal.containsKey(tab)) {
            Pane pane = tabPaneMapInternal.get(tab);
            String fxmlFile = fxmlMapInternal.get(tab);
            loadTabContent(tab, pane, fxmlFile);
        } else if (tabPaneMapExternal.containsKey(tab) && fxmlMapExternal.containsKey(tab)) {
            Pane pane = tabPaneMapExternal.get(tab);
            String fxmlFile = fxmlMapExternal.get(tab);
            loadTabContent(tab, pane, fxmlFile);
        }
    }
}