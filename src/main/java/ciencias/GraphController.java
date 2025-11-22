package ciencias;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class GraphController {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabOperations;
    @FXML
    private Tab tabTrees;
    @FXML
    private Tab tabRepresentation;


    @FXML
    private ScrollPane scrollOperations;
    @FXML
    private ScrollPane scrollTrees;
    @FXML
    private ScrollPane scrollRepresentation;

    private Map<Tab, String> fxmlMap;

    @FXML
    public void initialize() {
        initializeMappings();
        setupTabListeners();

        loadTabContent(tabOperations, "operations.fxml");
        tabPane.getSelectionModel().select(tabOperations);
    }

    private void initializeMappings() {
        fxmlMap = new HashMap<>();
        fxmlMap.put(tabOperations, "operations.fxml");
        fxmlMap.put(tabTrees, "treesGraph.fxml");
        fxmlMap.put(tabRepresentation, "representation.fxml");
    }

    private void setupTabListeners() {

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldTab, newTab) -> {
                    if (newTab != null && fxmlMap.containsKey(newTab)) {
                        loadTabContent(newTab, fxmlMap.get(newTab));
                    }
                });
    }

    private void loadTabContent(Tab tab, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent vista = loader.load();

            switch (tab.getText()) {
                case "Operaciones entre grafos":
                    scrollOperations.setContent(vista);
                    break;
                case "Arboles como grafos":
                    scrollTrees.setContent(vista);
                    break;
                case "Representacion de grafos":
                    scrollRepresentation.setContent(vista);
                    break;
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
}