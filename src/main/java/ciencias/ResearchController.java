package ciencias;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ciencias.Research.HashController;
import ciencias.Research.HashControllerExternal;
import ciencias.Research.IndexMonoController;
import ciencias.Research.IndexMultiController;
import ciencias.Research.TreesController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
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
    private MenuButton menuButtonMono;
    @FXML
    private MenuButton menuButtonMulti;

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
    private ScrollPane paneIndexMono;
    @FXML
    private ScrollPane paneIndexMulti;

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
    @FXML
    private Text textHashExternalMenu;
    @FXML
    private Text textTreeMenu;
    @FXML
    private Text textMonoMenu;
    @FXML
    private Text textMultiMenu;

    private List<MenuButton> menuButtons;
    private List<Pane> panes;
    private List<ScrollPane> scrollPanes;

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
        tabPaneMapExternal.put(tabDinamicas, paneDynamic);

        fxmlMapExternal = new HashMap<>();
        fxmlMapExternal.put(tabSecuencialExt, "secuencialExt.fxml");
        fxmlMapExternal.put(tabBinariaExt, "binariaExt.fxml");
        fxmlMapExternal.put(tabDinamicas, "dinamicas.fxml");
    }

    private void setupMenuButtonsAndPanes() {
        menuButtons = Arrays.asList(
                menuButtonSecuencial,
                menuButtonBinaria,
                menuButtonHash,
                menuButtonResiduos,
                menuButtonTrees,
                menuButtonClaves,
                menuButtonMono,
                menuButtonMulti);
        panes = Arrays.asList(
            paneHashInt,
            paneBinariaInt,
            paneSecuencialInt,
            paneHashExt,
            paneBinariaExt,
            paneSecuencialExt,
            paneDynamic,
            paneTrees);
        scrollPanes = Arrays.asList(
            paneIndexMono,
            paneIndexMulti);
    }

    private void loadInitialContent() {

        loadTabContent(tabSecuencial, paneSecuencialInt, "secuencial.fxml");
        loadTabContent(tabBinaria, paneBinariaInt, "binaria.fxml");

        loadTabContent(tabSecuencialExt, paneSecuencialExt, "secuencialExt.fxml");
        loadTabContent(tabBinariaExt, paneBinariaExt, "binariaExt.fxml");
        loadTabContent(tabDinamicas, paneDynamic, "dinamicas.fxml");

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
        if (textHashExternalMenu != null) {
            textHashExternalMenu.setText(null);
        }
        if (textTreeMenu != null) {
            textTreeMenu.setText(null);
        }
        if (textMonoMenu != null) {
            textMonoMenu.setText(null);
        }
        if (textMultiMenu != null) {
            textMultiMenu.setText(null);
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
        if (scrollPanes != null) {
            scrollPanes.forEach(sp -> {
                if (sp != null) {
                    sp.setContent(null);
                }
            });
        }
        if (textHashMenu != null) {
            textHashMenu.setText(null);
        }
        if (textHashExternalMenu != null) {
            textHashExternalMenu.setText(null);
        }
        if (textTreeMenu != null) {
            textTreeMenu.setText(null);
        }
        if (textMonoMenu != null) {
            textMonoMenu.setText(null);
        }
        if (textMultiMenu != null) {
            textMultiMenu.setText(null);
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

        if (textHashMenu != null) {
            textHashMenu.setText(getHashDescription(option));
        }
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
        if (textHashExternalMenu != null) {
            textHashExternalMenu.setText(getHashExternalDescription(option));
        }
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
        if (textTreeMenu != null) {
            textTreeMenu.setText(getTreesDescription(option));
        }
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

    @FXML
    private void monoAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonMono.setText(option);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mono.fxml"));
            Parent vista = loader.load();

            IndexMonoController indexMonoController = loader.getController();

            boolean esPrimario = "Primaria".equals(option);
            indexMonoController.setTipoIndice(esPrimario);

            indexMonoController.setResearchController(this);

            paneIndexMono.setContent(vista);
            if (textMonoMenu != null) {
                textMonoMenu.setText(getIndexDescription(option, true));
            }
        } catch (IOException e) {
            System.err.println("Error cargando mono.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void multiAction(javafx.event.ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        String option = source.getText();
        menuButtonMulti.setText(option);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("multi.fxml"));
            Parent vista = loader.load();

            IndexMultiController indexMultiController = loader.getController();

            boolean esPrimario = "Primaria".equals(option);
            indexMultiController.setTipoIndice(esPrimario);

            indexMultiController.setResearchController(this);

            paneIndexMulti.setContent(vista);
            if (textMultiMenu != null) {
                textMultiMenu.setText(getIndexDescription(option, false));
            }
        } catch (IOException e) {
            System.err.println("Error cargando multi.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getHashDescription(String option) {
        if (option == null || "Elegir".equals(option))
            return null;
        switch (option) {
            case "Modulo":
                return "La funcion mas comun, donde se divide la clave entre el tamaño de la estructura y se toma el resto como indice. Es rapido y uniforme si el tamaño de la estructura es un numero primo, evitando agrupamientos.";
            case "Cuadrada":
                return "Se eleva la clave al cuadrado y se extraen digitos centrales del resultado para formar el indice. Mitiga colisiones al dispersar claves similares, pero es mas costosa computacionalmente.";
            case "Truncamiento":
                return "Ignora parte de la clave y usa los digitos restantes directamente como indice. Simple pero propenso a colisiones si la parte eliminada contiene variabilidad importante.";
            case "Plegamiento":
                return "Divide la clave en segmentos que se suman o combinan para producir el indice. Efectivo para claves largas, distribuyendo valores de forma uniforme al mezclar todos los digitos.";
            default:
                return option;
        }
    }

    private String getHashExternalDescription(String option) {
        if (option == null || "Elegir".equals(option))
            return null;
        switch (option) {
            case "Modulo":
                return "En almacenamiento externo, la funcion modulo distribuye las claves entre multiples bloques calculando el resto de la division entre el tamaño total de la estructura. Cada bloque contiene un segmento contiguo de posiciones, y el resultado del hash determina en que bloque especifico debe ubicarse el registro, optimizando el acceso directo mediante calculo aritmetico simple.";
            case "Cuadrada":
                return "Eleva la clave al cuadrado y extrae digitos centrales para determinar el bloque de destino. En estructuras multi-bloque, esta tecnica dispersa eficientemente claves similares evitando concentracion en bloques especificos, aunque requiere mayor procesamiento para el calculo y extraccion de digitos en cada operacion.";
            case "Truncamiento":
                return "Selecciona posiciones especificas de la clave como indice de bloque. En contextos multi-bloque, ignora partes de la clave que podrian generar desbalance, pero requiere configuracion cuidadosa de las posiciones a conservar para evitar colisiones excesivas en bloques particulares.";
            case "Plegamiento":
                return "Divide la clave en segmentos que se suman para generar el indice de bloque. Particularmente util para claves largas en almacenamiento externo, ya que combina todos los digitos en la determinacion de posicion, distribuyendo uniformemente los registros entre los diferentes bloques fisicos.";
            case "Transferencia de clave":
                return "Convierte la clave a base no decimal (en este aplicativo a base 7) y toma digitos especificos de esta representacion para calcular el bloque destino. En almacenamiento externo, esta transformacion rompe patrones secuenciales y distribuye uniformemente entre bloques, aprovechando propiedades de numeracion no decimal para optimizar dispersion.";
            default:
                return option;
        }
    }

    private String getTreesDescription(String option) {
        if (option == null || "Elegir".equals(option))
            return null;
        switch (option) {
            case "Arboles de busqueda digital":
                return "Representa explicitamente cada bit de la clave en el recorrido del arbol, creando nodos para cada bit incluyendo caminos con un solo hijo. La busqueda sigue secuencialmente los bits de la clave desde el mas significativo, ideal para claves de longitud fija y busquedas por prefijos.";
            case "Arboles de busqueda por residuos":
                return "Convierte nodos hoja en nodos de enlace cuando varias claves comparten prefijo, bifurcandose al primer bit diferente. Optimiza el espacio colapsando rutas comunes y expandiendo solo en puntos de divergencia, eficiente para claves con prefijos compartidos.";
            case "Arboles de busqueda por residuos multiple":
                return "Procesa multiples bits por nivel, creando hasta 4 hijos por nodo. Reduce la altura del arbol procesando segmentos de bits en cada nivel, acelerando busquedas a costa de mayor complejidad en la estructura nodal.";
            case "Arboles de Huffman":
                return "Construye un arbol mas optimo basado en frecuencias de caracteres, donde los simbolos mas frecuentes tienen codigos mas cortos. Emplea un algoritmo vorzano que combina repetidamente los dos nodos de menor frecuencia hasta formar el arbol completo, utilizado para compresion eficiente de datos.";
            default:
                return option;
        }
    }

    private String getIndexDescription(String option, boolean isMono) {
        if (option == null || "Elegir".equals(option))
            return null;
        String tipo = isMono ? "Mononivel" : "Multinivel";
        if ("Primaria".equals(option))
            return tipo + " - indice Primario\nSe construyen sobre la clave primaria, manteniendo la secuencia fisica de los datos en disco. Proporcionan acceso directo y secuencial eficiente, con una estructura que refleja el orden de almacenamiento real de los registros en el archivo.";
        if ("Secundaria".equals(option))
            return tipo + " - indice Secundario\nSe crean sobre campos no ordenados fisicamente, permitiendo acceso eficiente por atributos distintos a la clave primaria. Mantienen un nivel adicional de indireccion mediante punteros a registros, independientes de la organizacion fisica, ideales para consultas por campos frecuentemente consultados pero no principales.";
        return tipo + " - " + option;
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