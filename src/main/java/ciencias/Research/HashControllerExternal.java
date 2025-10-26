package ciencias.Research;

import ciencias.ResearchController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.util.*;

public class HashControllerExternal implements Initializable {

    @FXML
    private Button createButton, reiniciarButton, insertButton, searchButton, deleteButton, undoButton, redoButton,
            saveButton, vistaAmpliada, defineCollitionsButton, truncButton;
    @FXML
    private Label titleHash, functionHash;
    @FXML
    private Text arrayLengthText, itemsArrayText, truncText;
    @FXML
    private TextField newItemArray, modDeleteItem, rangeHash;
    @FXML
    private Spinner<Integer> numberDigits;
    @FXML
    private MenuButton bloquesButton, collisionHash, collisionCustomButton;
    @FXML
    private ComboBox<Integer> truncElegir;
    @FXML
    private ListView<String> miViewList;

    private List<List<String>> blocks;
    private int tableSize;
    private int blockSize;
    private int numBlocks;
    private int maxDigits;
    private int currentBlockView = 0;
    private String collisionString;
    private String hashString;
    private String pendingKey = null;
    private boolean isSearching;

    private int[] truncPositions;
    private int truncMaxSelections = 0;
    private boolean truncPositionsSet = false;

    private Map<Integer, String[]> cellColorsByBlock;
    private List<String> insertedKeys = new ArrayList<>();

    private final Deque<ActionState> undoStack = new ArrayDeque<>();
    private final Deque<ActionState> redoStack = new ArrayDeque<>();

    private static final double ANIM_STEP_MS = 450;

    private List<List<List<String>>> auxiliaryBlocks;
    private Map<Integer, List<String>> chainedStructures;

    private ResearchController researchController;

    private boolean isViewingAuxiliary = false;
    private int currentAuxiliaryLevel = 0;
    private boolean isViewingChainedList = false;
    private int currentChainedPosition = 0;

    private Map<Integer, Map<Integer, String[]>> auxCellColors;

    private static class ActionState implements Serializable {
        private final List<List<String>> blocksSnapshot;
        private final List<List<List<String>>> auxiliaryBlocksSnapshot;
        private final Map<Integer, List<String>> chainedStructuresSnapshot;
        private final int tableSizeSnapshot;
        private final int blockSizeSnapshot;
        private final int numBlocksSnapshot;
        private final int maxDigitsSnapshot;
        private final String collisionMethodSnapshot;
        private final String hashFunctionSnapshot;
        private final int[] truncPositionsSnapshot;
        private final boolean truncPositionsSetSnapshot;
        private final int lastModifiedBlock;
        private final int lastModifiedPosition;
        private final List<String> insertedKeysSnapshot;

        ActionState(List<List<String>> blocks, List<List<List<String>>> auxiliaryBlocks,
                Map<Integer, List<String>> chainedStructures, int tableSize, int blockSize,
                int numBlocks, int maxDigits, String collisionMethod, String hashFunction,
                int[] truncPositions, boolean truncPositionsSet,
                int lastModifiedBlock, int lastModifiedPosition,
                List<String> insertedKeys) {
            this.blocksSnapshot = new ArrayList<>();
            if (blocks != null)
                for (List<String> b : blocks)
                    this.blocksSnapshot.add(new ArrayList<>(b));

            this.auxiliaryBlocksSnapshot = new ArrayList<>();
            if (auxiliaryBlocks != null)
                for (List<List<String>> level : auxiliaryBlocks) {
                    List<List<String>> levelCopy = new ArrayList<>();
                    for (List<String> block : level)
                        levelCopy.add(new ArrayList<>(block));
                    this.auxiliaryBlocksSnapshot.add(levelCopy);
                }

            this.chainedStructuresSnapshot = new HashMap<>();
            if (chainedStructures != null)
                this.chainedStructuresSnapshot.putAll(chainedStructures);

            this.tableSizeSnapshot = tableSize;
            this.blockSizeSnapshot = blockSize;
            this.numBlocksSnapshot = numBlocks;
            this.maxDigitsSnapshot = maxDigits;
            this.collisionMethodSnapshot = collisionMethod;
            this.hashFunctionSnapshot = hashFunction;
            this.truncPositionsSnapshot = truncPositions != null ? truncPositions.clone() : null;
            this.truncPositionsSetSnapshot = truncPositionsSet;
            this.lastModifiedBlock = lastModifiedBlock;
            this.lastModifiedPosition = lastModifiedPosition;
            this.insertedKeysSnapshot = new ArrayList<>(insertedKeys);
        }

        public List<List<String>> getBlocksSnapshot() {
            return blocksSnapshot;
        }

        public List<List<List<String>>> getAuxiliaryBlocksSnapshot() {
            return auxiliaryBlocksSnapshot;
        }

        public Map<Integer, List<String>> getChainedStructuresSnapshot() {
            return chainedStructuresSnapshot;
        }

        public int getTableSizeSnapshot() {
            return tableSizeSnapshot;
        }

        public int getBlockSizeSnapshot() {
            return blockSizeSnapshot;
        }

        public int getNumBlocksSnapshot() {
            return numBlocksSnapshot;
        }

        public int getMaxDigitsSnapshot() {
            return maxDigitsSnapshot;
        }

        public String getCollisionMethodSnapshot() {
            return collisionMethodSnapshot;
        }

        public String getHashFunctionSnapshot() {
            return hashFunctionSnapshot;
        }

        public int[] getTruncPositionsSnapshot() {
            return truncPositionsSnapshot;
        }

        public boolean getTruncPositionsSetSnapshot() {
            return truncPositionsSetSnapshot;
        }

        public int getLastModifiedBlock() {
            return lastModifiedBlock;
        }

        public int getLastModifiedPosition() {
            return lastModifiedPosition;
        }

        public List<String> getInsertedKeysSnapshot() {
            return insertedKeysSnapshot;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarValidadores();
        configurarSpinner();
        configurarListView();
        configurarBotones();
        configurarMenuBloques();
        configurarMenuColisiones();

        auxiliaryBlocks = new ArrayList<>();
        chainedStructures = new HashMap<>();
        auxCellColors = new HashMap<>();

        truncText.setVisible(false);
        truncButton.setVisible(false);
        truncElegir.setVisible(false);
        collisionCustomButton.setVisible(false);
        collisionCustomButton.setDisable(true);
        defineCollitionsButton.setDisable(true);
    }

    public void initData() {
        if (researchController == null)
            return;
        hashString = researchController.getHashString();

        switch (hashString) {
            case "Modulo":
                titleHash.setText("Funcion de Hash: Modulo");
                functionHash.setText("h(k) = k mod n + 1");
                break;
            case "Cuadrada":
                titleHash.setText("Funcion de Hash: Cuadrada");
                functionHash.setText("h(k) = dig_cen(k^2) + 1");
                break;
            case "Truncamiento":
                titleHash.setText("Funcion de Hash: Truncamiento");
                functionHash.setText("h(k) = elegir_dig(k) + 1");
                setupTruncationUI();
                break;
            case "Plegamiento":
                titleHash.setText("Funcion de Hash: Plegamiento");
                functionHash.setText("h(k) = digmensig(k_1 + k_2) + 1");
                break;
            case "Transferencia de clave":
                titleHash.setText("Funcion de Hash: Transferencia de clave");
                functionHash.setText("h(k) = digmensig(k)_b7 + 1");
                break;
        }

        if ("Truncamiento".equals(hashString)) {
            truncText.setVisible(true);
            truncButton.setVisible(true);
            truncElegir.setVisible(true);
            setupTruncationUI();
        } else {
            truncText.setVisible(false);
            truncButton.setVisible(false);
            truncElegir.setVisible(false);
        }
    }

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    private void configurarValidadores() {
        newItemArray.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        modDeleteItem.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        rangeHash.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
    }

    private void configurarSpinner() {
        numberDigits.setValueFactory(new IntegerSpinnerValueFactory(1, 9, 3));
        numberDigits.valueProperty().addListener((obs, oldValue, newValue) -> {
            if ("Truncamiento".equals(hashString)) {
                truncPositions = null;
                truncPositionsSet = false;
                setupTruncationUI();
            }
        });
    }

    private void configurarListView() {
        miViewList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);

                if (blocks == null) {
                    setStyle("-fx-background-color:white; -fx-text-fill:black;");
                    return;
                }

                String color = "WHITE";
                int index = getIndex();

                if (isViewingAuxiliary) {
                    Map<Integer, String[]> levelColors = auxCellColors.get(currentAuxiliaryLevel);
                    if (levelColors != null) {
                        String[] arr = levelColors.get(currentBlockView);
                        if (arr != null && index >= 0 && index < arr.length) {
                            color = arr[index];
                        }
                    }
                } else if (isViewingChainedList) {

                    color = "WHITE";
                } else {

                    String[] colors = cellColorsByBlock.getOrDefault(currentBlockView, null);
                    if (colors != null && index >= 0 && index < colors.length) {
                        color = colors[index];
                    }
                }

                applyColorStyle(color);
            }

            private void applyColorStyle(String color) {
                switch (color) {
                    case "GRAY":
                        setStyle("-fx-background-color: lightgray; -fx-text-fill: black;");
                        break;
                    case "GREEN":
                        setStyle("-fx-background-color: lightgreen; -fx-text-fill: black;");
                        break;
                    case "RED":
                        setStyle("-fx-background-color: lightcoral; -fx-text-fill: black;");
                        break;
                    case "YELLOW":
                        setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                        break;
                    default:
                        setStyle("-fx-background-color: white; -fx-text-fill: black;");
                        break;
                }
            }
        });
    }

    private void configurarBotones() {
        reiniciarButton.setDisable(true);
        insertButton.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);
        vistaAmpliada.setDisable(true);
        newItemArray.setDisable(true);
        modDeleteItem.setDisable(true);
        collisionHash.setDisable(true);
        defineCollitionsButton.setDisable(true);
    }

    private void configurarMenuBloques() {
        bloquesButton.setVisible(false);
        bloquesButton.setDisable(true);
    }

    private void configurarMenuColisiones() {
        for (MenuItem item : collisionHash.getItems()) {
            item.setOnAction(event -> {
                collisionHash.setText(item.getText());
                this.collisionString = item.getText();
                defineCollitionsButton.setDisable(false);
            });
        }
    }

    @FXML
    private void defineCollitions() {
        if (collisionString == null || "Elegir".equals(collisionString)) {
            itemsArrayText.setText("Debe seleccionar un método válido.");
            return;
        }

        collisionHash.setDisable(true);
        defineCollitionsButton.setDisable(true);

        insertButton.setDisable(false);
        searchButton.setDisable(false);
        deleteButton.setDisable(false);
        newItemArray.setDisable(false);
        modDeleteItem.setDisable(false);
        undoButton.setDisable(false);
        redoButton.setDisable(false);

        updateCollisionCustomButton();

        itemsArrayText.setText("Método de colisión '" + collisionString + "' definido.");

        resetAllColors();
        actualizarVistaArray();

        if (pendingKey != null) {
            String keyToInsert = pendingKey;
            pendingKey = null;
            doInsert(keyToInsert);
        }
    }

    private void updateCollisionCustomButton() {
        if ("Anidamiento".equals(collisionString) || "Encadenamiento".equals(collisionString)) {
            collisionCustomButton.setVisible(true);
            collisionCustomButton.setDisable(false);
            populateCollisionMenu();
        } else {
            collisionCustomButton.setVisible(false);
            collisionCustomButton.setDisable(true);
        }
    }

    private void populateCollisionMenu() {
        collisionCustomButton.getItems().clear();

        MenuItem mainStructure = new MenuItem("Estructura principal");
        mainStructure.setOnAction(e -> showMainStructure());
        collisionCustomButton.getItems().add(mainStructure);

        if ("Anidamiento".equals(collisionString)) {

            for (int level = 0; level < auxiliaryBlocks.size(); level++) {
                final int currentLevel = level + 1;

                int elementosNoVacios = 0;
                for (List<String> bloque : auxiliaryBlocks.get(level)) {
                    if (!bloque.isEmpty()) {
                        elementosNoVacios++;
                    }
                }

                MenuItem auxItem = new MenuItem("Estructura auxiliar Nivel " + currentLevel +
                        " (" + elementosNoVacios + " bloques con datos)");
                auxItem.setOnAction(e -> showAuxiliaryStructure(currentLevel));
                collisionCustomButton.getItems().add(auxItem);
            }
        } else if ("Encadenamiento".equals(collisionString)) {

            for (int position = 0; position < numBlocks; position++) {
                final int pos = position;
                List<String> chain = chainedStructures.get(position);
                if (chain != null) {
                    MenuItem chainItem = new MenuItem(
                            "Lista en Bloque " + (pos + 1) + " (" + chain.size() + " elementos)");
                    chainItem.setOnAction(e -> showChainedList(pos));
                    collisionCustomButton.getItems().add(chainItem);
                }
            }
        }

        int auxStructuresCount = collisionCustomButton.getItems().size() - 1;
        if (auxStructuresCount > 0) {
            collisionCustomButton.setText("Estructuras (" + auxStructuresCount + ")");
        } else {
            collisionCustomButton.setText("Estructuras");
        }
    }

    private void showMainStructure() {
        isViewingAuxiliary = false;
        isViewingChainedList = false;
        currentBlockView = 0;
        resetAllColors();
        actualizarVistaArray();
        bloquesButton.setText("Bloque " + (currentBlockView + 1));
        itemsArrayText.setText("Mostrando estructura principal");
    }

    private void showAuxiliaryStructure(int level) {
        int levelIndex = level - 1;

        if (levelIndex >= 0 && levelIndex < auxiliaryBlocks.size()) {
            isViewingAuxiliary = true;
            currentAuxiliaryLevel = levelIndex;
            currentBlockView = 0;

            actualizarVistaAuxiliar();

            bloquesButton.setText("Bloque " + (currentBlockView + 1) + " (Aux " + level + ")");
            itemsArrayText.setText("Mostrando estructura auxiliar nivel " + level);
        } else {
            itemsArrayText.setText("Estructura auxiliar nivel " + level + " no disponible");
        }
    }

    private void showChainedList(int position) {
        List<String> chain = chainedStructures.get(position);
        if (chain != null) {
            isViewingChainedList = true;
            currentChainedPosition = position;

            actualizarVistaEncadenada();

            bloquesButton.setText("Lista Pos " + (position + 1));
            itemsArrayText.setText("Mostrando lista encadenada de la posición " + (position + 1));
        } else {
            itemsArrayText.setText("No hay lista encadenada en la posición " + (position + 1));
        }
    }

    private void setupTruncationUI() {
        if (!"Truncamiento".equals(hashString)) {
            return;
        }
        truncElegir.getItems().clear();

        int totalDigits = numberDigits.getValue();
        truncMaxSelections = totalDigits - 2;

        if (truncMaxSelections <= 0) {
            arrayLengthText.setText("El numero de digitos debe ser mayor a 2 para usar truncamiento.");
            truncButton.setDisable(true);
            truncElegir.setDisable(true);
            return;
        } else {
            truncButton.setDisable(false);
            truncElegir.setDisable(false);
        }

        for (int i = 1; i <= totalDigits; i++) {
            truncElegir.getItems().add(i);
        }

        if (truncPositions == null || truncPositions.length == 0) {
            arrayLengthText.setText(
                    "Selecciona " + truncMaxSelections + " posiciones a truncar (de 1 a " + totalDigits + ").");
        } else {
            arrayLengthText.setText("Posiciones seleccionadas: " + Arrays.toString(truncPositions) + ". Restantes: "
                    + (truncMaxSelections - truncPositions.length));
        }

        if (createButton.isDisable()) {
            truncButton.setDisable(true);
            truncElegir.setDisable(true);
        }
    }

    @FXML
    private void elegirTrunc() {
        if (!"Truncamiento".equals(hashString) || truncMaxSelections <= 0) {
            arrayLengthText.setText("El truncamiento no esta disponible. Elige un numero de digitos mayor a 2.");
            return;
        }

        Integer position = truncElegir.getValue();
        if (position == null) {
            arrayLengthText.setText("Por favor, seleccione una posicion de la lista.");
            return;
        }

        if (truncPositions == null)
            truncPositions = new int[0];
        for (int pos : truncPositions) {
            if (pos == position) {
                arrayLengthText.setText("La posicion " + position + " ya fue seleccionada.");
                return;
            }
        }

        if (truncPositions.length >= truncMaxSelections) {
            arrayLengthText.setText("Ya ha seleccionado el maximo de " + truncMaxSelections + " posiciones.");
            return;
        }

        int[] newPositions = Arrays.copyOf(truncPositions, truncPositions.length + 1);
        newPositions[truncPositions.length] = position;
        truncPositions = newPositions;
        Arrays.sort(truncPositions);

        if (truncPositions.length == truncMaxSelections) {
            truncPositionsSet = true;
            arrayLengthText
                    .setText("Posiciones finales: " + Arrays.toString(truncPositions) + ". Ya puede crear el array.");
            truncButton.setDisable(true);
            truncElegir.setDisable(true);
            createButton.setDisable(false);
        } else {
            int remaining = truncMaxSelections - truncPositions.length;
            arrayLengthText
                    .setText("Seleccionado: " + Arrays.toString(truncPositions) + " (Faltan " + remaining + ").");
        }
    }

    @FXML
    private void crearArray() {
        if ("Truncamiento".equals(hashString) && !truncPositionsSet) {
            arrayLengthText.setText("Debe seleccionar las posiciones para truncar antes de crear el array.");
            return;
        }

        String rango = rangeHash.getText();
        if (rango == null || rango.isEmpty()) {
            arrayLengthText.setText("Debe ingresar un rango antes de crear el array.");
            return;
        }
        try {
            tableSize = Integer.parseInt(rango);
            if (tableSize <= 0) {
                arrayLengthText.setText("Tamaño debe ser > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            arrayLengthText.setText("Rango invalido.");
            return;
        }

        maxDigits = numberDigits.getValue();
        blockSize = (int) Math.floor(Math.sqrt(tableSize));
        if (blockSize <= 0)
            blockSize = 1;
        numBlocks = (int) Math.ceil((double) tableSize / blockSize);

        blocks = new ArrayList<>();
        auxiliaryBlocks = new ArrayList<>();
        chainedStructures = new HashMap<>();
        insertedKeys.clear();

        for (int i = 0; i < numBlocks; i++) {
            blocks.add(new ArrayList<>());
        }

        if ("Anidamiento".equals(collisionString)) {
            List<List<String>> firstAuxLevel = new ArrayList<>();
            for (int i = 0; i < numBlocks; i++) {
                firstAuxLevel.add(new ArrayList<>());
            }
            auxiliaryBlocks.add(firstAuxLevel);
        }

        cellColorsByBlock = new HashMap<>();
        for (int i = 0; i < numBlocks; i++)
            cellColorsByBlock.put(i, initColorArray(getBlockCapacity(i)));

        initializeAuxColors();

        currentBlockView = 0;
        saveState(-1, -1);

        actualizarEstadoControlesCreacion();
        arrayLengthText.setText("Array de " + tableSize + " elementos creado. " +
                numBlocks + " bloques de tamaño " + blockSize + ".");
        itemsArrayText.setText("Estructura creada. Insertar claves de " + maxDigits + " digitos.");
        actualizarVistaArray();
        populateBlocksMenu();

        updateCollisionCustomButton();
        populateCollisionMenu();
    }

    private String[] initColorArray(int capacity) {
        String[] arr = new String[capacity];
        Arrays.fill(arr, "WHITE");
        return arr;
    }

    private void initializeAuxColors() {
        auxCellColors = new HashMap<>();
        for (int level = 0; level < auxiliaryBlocks.size(); level++) {
            Map<Integer, String[]> levelColors = new HashMap<>();
            for (int block = 0; block < numBlocks; block++) {
                levelColors.put(block, initColorArray(getBlockCapacity(block)));
            }
            auxCellColors.put(level, levelColors);
        }
    }

    private void actualizarEstadoControlesCreacion() {
        createButton.setDisable(true);
        rangeHash.setDisable(true);
        numberDigits.setDisable(true);
        reiniciarButton.setDisable(false);
        insertButton.setDisable(false);
        searchButton.setDisable(false);
        deleteButton.setDisable(false);
        saveButton.setDisable(false);
        vistaAmpliada.setDisable(false);
        newItemArray.setDisable(false);
        modDeleteItem.setDisable(false);
        bloquesButton.setVisible(true);
        bloquesButton.setDisable(false);
        undoButton.setDisable(false);
        redoButton.setDisable(false);

        if ("Truncamiento".equals(hashString)) {
            truncButton.setDisable(true);
            truncElegir.setDisable(true);
        }
    }

    private int aplicarFuncionHash(int clave) {
        int resultado = 0;

        switch (hashString) {
            case "Modulo":
                resultado = (clave % tableSize);
                break;
            case "Cuadrada":
                long sq = (long) clave * clave;
                String s = String.valueOf(sq);
                int digitos = (int) Math.log10(tableSize) + 1;
                int digitosCentrales = Math.max(1, digitos - 1);
                int start = (s.length() - digitosCentrales) / 2;
                if (start < 0)
                    start = 0;
                String sub = s.substring(start, Math.min(start + digitosCentrales, s.length()));
                if (sub.isEmpty())
                    resultado = 0;
                else
                    resultado = Integer.parseInt(sub) % tableSize;
                break;
            case "Plegamiento":
                String claveStr = String.valueOf(clave);
                int sum = 0;
                for (int i = 0; i < claveStr.length(); i += 2) {
                    int end = Math.min(i + 2, claveStr.length());
                    String segmento = claveStr.substring(i, end);
                    sum += Integer.parseInt(segmento);
                }
                int digitosPlegamiento = (int) Math.log10(tableSize) + 1;
                int divisor = (int) Math.pow(10, digitosPlegamiento - 1);
                resultado = sum % divisor;
                resultado = resultado % tableSize;
                break;
            case "Truncamiento":
                String numStr = String.format("%0" + maxDigits + "d", clave);
                StringBuilder truncatedNum = new StringBuilder();
                if (truncPositions != null) {
                    for (int pos : truncPositions) {
                        int idx = pos - 1;
                        if (idx >= 0 && idx < numStr.length()) {
                            truncatedNum.append(numStr.charAt(idx));
                        }
                    }
                }
                if (truncatedNum.length() == 0)
                    resultado = 0;
                else
                    resultado = Integer.parseInt(truncatedNum.toString()) % tableSize;
                break;
            case "Transferencia de clave":
                resultado = aplicarTransferencia(clave);
                break;
            default:
                resultado = (clave % tableSize);
        }

        if (numBlocks > 0) {
            resultado = Math.abs(resultado) % numBlocks;
        } else {
            resultado = 0;
        }

        return resultado;
    }

    private int aplicarTransferencia(int clave) {
        String base7 = convertirABase7(clave);
        String dosDigitos = base7.length() >= 2 ? base7.substring(base7.length() - 2) : base7;
        int valorBase10 = Integer.parseInt(dosDigitos, 7);

        return (valorBase10 % tableSize);
    }

    private String convertirABase7(int numero) {
        if (numero == 0)
            return "0";

        StringBuilder resultado = new StringBuilder();
        int n = Math.abs(numero);

        while (n > 0) {
            int residuo = n % 7;
            resultado.insert(0, residuo);
            n = n / 7;
        }

        return resultado.toString();
    }

    @FXML
    private void addToArray() {
        String input = newItemArray.getText();
        if (input == null || input.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave.");
            return;
        }
        if (!input.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("Clave debe tener " + maxDigits + " digitos.");
            return;
        }

        if (insertedKeys.contains(input)) {
            itemsArrayText.setText("Error: La clave " + input + " ya existe.");
            newItemArray.clear();
            return;
        }

        doInsert(input);
        newItemArray.clear();
    }

    private void doInsert(String claveStr) {
        int claveInt;
        try {
            claveInt = Integer.parseInt(claveStr);
        } catch (NumberFormatException e) {
            itemsArrayText.setText("Error: La clave debe ser numérica.");
            return;
        }

        if (insertedKeys.contains(claveStr)) {
            itemsArrayText.setText("Error: La clave " + claveStr + " ya existe.");
            return;
        }

        int targetBlock = aplicarFuncionHash(claveInt);

        InsertionResult result = null;

        if ("Anidamiento".equals(collisionString)) {
            result = doInsertNested(claveStr, claveInt, targetBlock, 0);
        } else if ("Encadenamiento".equals(collisionString)) {
            result = doInsertChained(claveStr, claveInt, targetBlock);
        } else {
            result = doInsertStandard(claveStr, claveInt, targetBlock);
        }

        if (result != null && result.success) {
            insertedKeys.add(claveStr);

            saveState(result.finalBlock, result.finalPos);

            animateInsertion(result.recorrido, result.finalBlock, result.finalPos, result.structureLevel);

            Timeline updateTimeline = new Timeline(new KeyFrame(
                    Duration.millis(ANIM_STEP_MS * (result.recorrido.size() + 1) + 100),
                    e -> {
                        updateCollisionCustomButton();
                        populateCollisionMenu();
                    }));
            updateTimeline.play();

            itemsArrayText.setText("Clave " + claveStr + " insertada exitosamente.");
        } else if (result != null && !result.success) {
            itemsArrayText.setText("Error: No se pudo insertar la clave " + claveStr);
        }
    }

    private InsertionResult doInsertStandard(String claveStr, int claveInt, int startBlock) {

        if (startBlock < 0 || startBlock >= numBlocks) {
            startBlock = Math.abs(startBlock) % numBlocks;
        }

        int currentBlock = startBlock;
        int step = 1;
        int intentos = 0;
        Set<Integer> bloquesVisitados = new HashSet<>();
        List<VisitedPos> recorrido = new ArrayList<>();

        while (true) {

            if (currentBlock < 0 || currentBlock >= numBlocks) {
                currentBlock = Math.abs(currentBlock) % numBlocks;
            }

            List<String> bloque = blocks.get(currentBlock);
            recorrido.add(new VisitedPos(currentBlock, -1, 0));

            if (bloque.size() < getBlockCapacity(currentBlock)) {
                int pos = findInsertPositionInBlock(bloque, claveStr);
                bloque.add(pos, claveStr);
                return new InsertionResult(true, currentBlock, pos, 0, recorrido);
            }

            if (collisionString == null) {
                itemsArrayText.setText(
                        "¡Colision detectada en el bloque " + (currentBlock + 1)
                                + "!\nElija y defina un metodo de resolucion.");
                pendingKey = claveStr;
                insertButton.setDisable(true);
                searchButton.setDisable(true);
                deleteButton.setDisable(true);
                newItemArray.setDisable(true);
                modDeleteItem.setDisable(true);
                undoButton.setDisable(true);
                redoButton.setDisable(true);
                collisionHash.setDisable(false);
                defineCollitionsButton.setDisable(false);
                return new InsertionResult(false, -1, -1, 0, recorrido);
            }

            bloquesVisitados.add(currentBlock);
            int nextBlock = siguienteBloque(currentBlock, step, claveInt);

            if (nextBlock < 0 || nextBlock >= numBlocks) {
                nextBlock = Math.abs(nextBlock) % numBlocks;
            }

            if (bloquesVisitados.contains(nextBlock)) {
                itemsArrayText.setText("No se pudo insertar: colisión circular.");
                return new InsertionResult(false, -1, -1, 0, recorrido);
            }

            currentBlock = nextBlock;
            recorrido.add(new VisitedPos(currentBlock, -1, 0));
            step++;
            intentos++;

            if (intentos >= numBlocks * 2) {
                itemsArrayText.setText("No se pudo insertar después de " + intentos + " intentos.");
                return new InsertionResult(false, -1, -1, 0, recorrido);
            }
        }
    }

    private InsertionResult doInsertChained(String claveStr, int claveInt, int startBlock) {
        List<String> bloque = blocks.get(startBlock);
        List<VisitedPos> recorrido = new ArrayList<>();
        recorrido.add(new VisitedPos(startBlock, -1, 0));

        boolean tieneLista = chainedStructures.containsKey(startBlock);

        if (!tieneLista && bloque.size() < getBlockCapacity(startBlock)) {

            int pos = findInsertPositionInBlock(bloque, claveStr);
            bloque.add(pos, claveStr);
            return new InsertionResult(true, startBlock, pos, 0, recorrido);
        } else if (!tieneLista && bloque.size() >= getBlockCapacity(startBlock)) {

            List<String> chain = new ArrayList<>();

            if (!bloque.isEmpty()) {
                String ultimoElemento = bloque.remove(bloque.size() - 1);
                chain.add(ultimoElemento);
            }

            chainedStructures.put(startBlock, chain);

            if (chain.contains(claveStr)) {
                itemsArrayText.setText("Error: La clave ya existe en la cadena.");
                return new InsertionResult(false, -1, -1, 0, recorrido);
            }

            int pos = 0;
            while (pos < chain.size() && Integer.parseInt(chain.get(pos)) < Integer.parseInt(claveStr)) {
                pos++;
            }
            chain.add(pos, claveStr);

            return new InsertionResult(true, startBlock, pos, -1, recorrido);
        } else {

            List<String> chain = chainedStructures.get(startBlock);

            if (chain == null) {

                chain = new ArrayList<>();
                chainedStructures.put(startBlock, chain);
            }

            if (chain.contains(claveStr)) {
                itemsArrayText.setText("Error: La clave ya existe en la cadena.");
                return new InsertionResult(false, -1, -1, 0, recorrido);
            }

            int pos = 0;
            while (pos < chain.size() && Integer.parseInt(chain.get(pos)) < Integer.parseInt(claveStr)) {
                pos++;
            }
            chain.add(pos, claveStr);

            return new InsertionResult(true, startBlock, pos, -1, recorrido);
        }
    }

    private InsertionResult doInsertNested(String claveStr, int claveInt, int startBlock, int structureLevel) {
        List<String> currentBlock = getBlockForLevel(startBlock, structureLevel);
        List<VisitedPos> recorrido = new ArrayList<>();
        recorrido.add(new VisitedPos(startBlock, -1, structureLevel));

        if (currentBlock.size() < getBlockCapacity(startBlock)) {
            int pos = findInsertPositionInBlock(currentBlock, claveStr);
            currentBlock.add(pos, claveStr);

            updateCollisionCustomButton();
            populateCollisionMenu();

            return new InsertionResult(true, startBlock, pos, structureLevel, recorrido);
        } else {

            recorrido.add(new VisitedPos(startBlock, -2, structureLevel));

            if (structureLevel >= auxiliaryBlocks.size()) {
                List<List<String>> newLevel = new ArrayList<>();
                for (int i = 0; i < numBlocks; i++) {
                    newLevel.add(new ArrayList<>());
                }
                auxiliaryBlocks.add(newLevel);
                initializeAuxColors();
            }

            InsertionResult result = doInsertNested(claveStr, claveInt, startBlock, structureLevel + 1);
            if (result.success) {

                recorrido.addAll(result.recorrido);
                updateCollisionCustomButton();
                populateCollisionMenu();
            }
            return result;
        }
    }

    private List<String> getBlockForLevel(int blockIndex, int structureLevel) {
        if (structureLevel == 0) {
            return blocks.get(blockIndex);
        } else {
            int levelIndex = structureLevel - 1;

            while (auxiliaryBlocks.size() <= levelIndex) {
                List<List<String>> newLevel = new ArrayList<>();
                for (int i = 0; i < numBlocks; i++) {
                    newLevel.add(new ArrayList<>());
                }
                auxiliaryBlocks.add(newLevel);
            }

            return auxiliaryBlocks.get(levelIndex).get(blockIndex);
        }
    }

    private int siguienteBloque(int bloqueActual, int step, int claveInt) {
        if (collisionString == null)
            return bloqueActual;

        int nextBlock;

        switch (collisionString) {
            case "Lineal":
                nextBlock = (bloqueActual + 1) % numBlocks;
                break;
            case "Cuadratica":
                nextBlock = (bloqueActual + (step * step)) % numBlocks;
                break;
            case "Doble Hash":
                int hash2 = (claveInt % (numBlocks - 1)) + 1;
                nextBlock = (bloqueActual + hash2) % numBlocks;
                break;
            default:
                nextBlock = (bloqueActual + 1) % numBlocks;
                break;
        }

        if (nextBlock < 0) {
            nextBlock = (nextBlock % numBlocks + numBlocks) % numBlocks;
        } else if (nextBlock >= numBlocks) {
            nextBlock = nextBlock % numBlocks;
        }

        return nextBlock;
    }

    private int findInsertPositionInBlock(List<String> bloque, String clave) {
        int claveInt = Integer.parseInt(clave);
        int pos = 0;
        while (pos < bloque.size() && Integer.parseInt(bloque.get(pos)) < claveInt) {
            pos++;
        }
        return pos;
    }

    @FXML
    private void searchItem() {
        isSearching = true;
        String key = modDeleteItem.getText();
        if (key == null || key.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave.");
            return;
        }
        if (!key.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("Clave debe tener " + maxDigits + " digitos.");
            return;
        }

        SearchResult result = doSearch(key);
        animateSearch(result.recorrido, result.foundBlock, result.foundPos, result.found, result.structureLevel);

        if (result.found) {
            itemsArrayText.setText("Clave " + key + " encontrada en bloque " +
                    (result.foundBlock + 1) + ", pos " + (result.foundPos + 1));
        } else {
            itemsArrayText.setText("Clave " + key + " no encontrada");
        }
    }

    private SearchResult doSearch(String claveStr) {
        int claveInt = Integer.parseInt(claveStr);
        int startBlock = aplicarFuncionHash(claveInt);

        if ("Anidamiento".equals(collisionString)) {
            return searchNested(claveStr, startBlock, 0, new ArrayList<>());
        } else if ("Encadenamiento".equals(collisionString)) {
            return searchChained(claveStr, startBlock, new ArrayList<>());
        } else {
            return searchStandard(claveStr, startBlock, new ArrayList<>());
        }
    }

    private SearchResult searchStandard(String claveStr, int startBlock, List<VisitedPos> recorrido) {
        int currentBlock = startBlock;
        int step = 1;
        Set<Integer> bloquesVisitados = new HashSet<>();

        while (true) {
            List<String> bloque = blocks.get(currentBlock);
            recorrido.add(new VisitedPos(currentBlock, -1, 0));

            for (int pos = 0; pos < bloque.size(); pos++) {
                recorrido.add(new VisitedPos(currentBlock, pos, 0));
                if (bloque.get(pos).equals(claveStr)) {
                    return new SearchResult(true, currentBlock, pos, 0, new ArrayList<>(recorrido), claveStr);
                }
            }

            if (collisionString == null) {
                return new SearchResult(false, -1, -1, 0, new ArrayList<>(recorrido), claveStr);
            }

            bloquesVisitados.add(currentBlock);
            int nextBlock = siguienteBloque(currentBlock, step, Integer.parseInt(claveStr));

            if (bloquesVisitados.contains(nextBlock) || step > numBlocks * 2) {
                return new SearchResult(false, -1, -1, 0, new ArrayList<>(recorrido), claveStr);
            }

            currentBlock = nextBlock;
            step++;
        }
    }

    private SearchResult searchChained(String claveStr, int startBlock, List<VisitedPos> recorrido) {
        List<String> bloque = blocks.get(startBlock);
        recorrido.add(new VisitedPos(startBlock, -1, 0));

        int capacidadReal = getBlockCapacity(startBlock);
        boolean tieneLista = chainedStructures.containsKey(startBlock);
        int elementosABuscar = tieneLista ? Math.min(bloque.size(), capacidadReal - 1) : bloque.size();

        for (int pos = 0; pos < elementosABuscar; pos++) {
            recorrido.add(new VisitedPos(startBlock, pos, 0));
            if (bloque.get(pos).equals(claveStr)) {
                return new SearchResult(true, startBlock, pos, 0, new ArrayList<>(recorrido), claveStr);
            }
        }

        List<String> chain = chainedStructures.get(startBlock);
        if (chain != null) {

            if (tieneLista) {
                recorrido.add(new VisitedPos(startBlock, capacidadReal - 1, 0));
            }

            for (int pos = 0; pos < chain.size(); pos++) {
                recorrido.add(new VisitedPos(startBlock, pos, -1));
                if (chain.get(pos).equals(claveStr)) {
                    return new SearchResult(true, startBlock, pos, -1, new ArrayList<>(recorrido), claveStr);
                }
            }
        }

        return new SearchResult(false, -1, -1, 0, new ArrayList<>(recorrido), claveStr);
    }

    private SearchResult searchNested(String claveStr, int startBlock, int structureLevel, List<VisitedPos> recorrido) {
        List<String> currentBlock = getBlockForLevel(startBlock, structureLevel);
        recorrido.add(new VisitedPos(startBlock, -1, structureLevel));

        for (int pos = 0; pos < currentBlock.size(); pos++) {
            recorrido.add(new VisitedPos(startBlock, pos, structureLevel));
            if (currentBlock.get(pos).equals(claveStr)) {
                return new SearchResult(true, startBlock, pos, structureLevel, new ArrayList<>(recorrido), claveStr);
            }
        }

        if (structureLevel < auxiliaryBlocks.size()) {

            recorrido.add(new VisitedPos(startBlock, -2, structureLevel));
            return searchNested(claveStr, startBlock, structureLevel + 1, recorrido);
        }

        return new SearchResult(false, -1, -1, structureLevel, new ArrayList<>(recorrido), claveStr);
    }

    @FXML
    private void eliminateItem() {
        isSearching = false;
        String key = modDeleteItem.getText();
        if (key == null || key.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave.");
            return;
        }

        SearchResult result = doSearch(key);
        if (result.found) {

            animateSearch(result.recorrido, result.foundBlock, result.foundPos, true, result.structureLevel);

            Timeline eliminationTimeline = new Timeline(new KeyFrame(
                    Duration.millis(ANIM_STEP_MS * (result.recorrido.size() + 1)),
                    e -> {
                        removeFromStructure(result);
                        saveState(result.foundBlock, result.foundPos);

                        updateCollisionCustomButton();
                        populateCollisionMenu();

                    }));
            eliminationTimeline.play();
        } else {
            itemsArrayText.setText("Clave no encontrada.");
        }
        modDeleteItem.clear();
    }

    private void removeFromStructure(SearchResult result) {
        if (result.structureLevel == 0) {

            List<String> bloque = blocks.get(result.foundBlock);
            if (result.foundPos < bloque.size()) {
                bloque.remove(result.foundPos);
                insertedKeys.remove(result.foundKey);
            }
        } else if (result.structureLevel == -1) {

            List<String> chain = chainedStructures.get(result.foundBlock);
            if (chain != null && result.foundPos < chain.size()) {
                chain.remove(result.foundPos);
                insertedKeys.remove(result.foundKey);

                if (chain.size() == 1) {
                    String ultimoElemento = chain.get(0);
                    List<String> bloquePrincipal = blocks.get(result.foundBlock);

                    if (bloquePrincipal.size() < getBlockCapacity(result.foundBlock)) {
                        bloquePrincipal.add(ultimoElemento);
                        chain.remove(0);
                        chainedStructures.remove(result.foundBlock);

                        itemsArrayText.setText("Elemento movido a bloque principal - Lista eliminada");
                    }
                } else if (chain.isEmpty()) {
                    chainedStructures.remove(result.foundBlock);
                }
            }
        } else {

            List<String> auxBlock = getBlockForLevel(result.foundBlock, result.structureLevel);
            if (result.foundPos < auxBlock.size()) {
                auxBlock.remove(result.foundPos);
                insertedKeys.remove(result.foundKey);

                int levelIndex = result.structureLevel - 1;
                if (levelIndex < auxiliaryBlocks.size()) {
                    List<List<String>> nivelActual = auxiliaryBlocks.get(levelIndex);
                    boolean todosVacios = true;

                    for (List<String> bloqueAux : nivelActual) {
                        if (!bloqueAux.isEmpty()) {
                            todosVacios = false;
                            break;
                        }
                    }

                    if (todosVacios) {
                        auxiliaryBlocks.remove(levelIndex);
                        auxCellColors.remove(levelIndex);
                        itemsArrayText.setText("Estructura auxiliar nivel " + (result.structureLevel)
                                + " eliminada - Todos los bloques vacíos");
                    }
                }
            }
        }

        showMainStructure();
    }

    private void animateInsertion(List<VisitedPos> recorrido, int targetBlock, int targetPos, int structureLevel) {
        Timeline timeline = new Timeline();
        Duration delay = Duration.ZERO;
        Duration step = Duration.millis(ANIM_STEP_MS);

        resetAllColors();

        for (VisitedPos vp : recorrido) {
            Duration d = delay;
            timeline.getKeyFrames().add(new KeyFrame(d, e -> {

                if (vp.pos == -2) {
                    itemsArrayText.setText("Cambiando a nivel " + (vp.structureLevel + 1));
                    return;
                }

                if (vp.structureLevel == 0) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = false;
                    currentBlockView = vp.block;
                    actualizarVistaArray();
                    if (vp.pos >= 0) {
                        setCellColor(vp.block, vp.pos, "GRAY");
                    }
                } else if (vp.structureLevel > 0) {
                    isViewingAuxiliary = true;
                    isViewingChainedList = false;
                    currentAuxiliaryLevel = vp.structureLevel - 1;
                    currentBlockView = vp.block;
                    actualizarVistaAuxiliar();
                    if (vp.pos >= 0) {
                        setAuxCellColor(vp.block, vp.pos, currentAuxiliaryLevel, "GRAY");
                    }
                } else if (vp.structureLevel == -1) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = true;
                    currentChainedPosition = vp.block;
                    actualizarVistaEncadenada();
                    resaltarElementoEnLista(vp.pos, "GRAY");
                }
            }));
            delay = delay.add(step);
        }

        timeline.getKeyFrames().add(new KeyFrame(delay, e -> {

            if (targetBlock >= 0 && targetPos >= 0) {
                if (structureLevel == 0) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = false;
                    setCellColor(targetBlock, targetPos, "YELLOW");
                    currentBlockView = targetBlock;
                    actualizarVistaArray();
                } else if (structureLevel > 0) {
                    isViewingAuxiliary = true;
                    isViewingChainedList = false;
                    currentAuxiliaryLevel = structureLevel - 1;
                    setAuxCellColor(targetBlock, targetPos, currentAuxiliaryLevel, "YELLOW");
                    currentBlockView = targetBlock;
                    actualizarVistaAuxiliar();
                } else if (structureLevel == -1) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = true;
                    currentChainedPosition = targetBlock;
                    actualizarVistaEncadenada();
                    resaltarElementoEnLista(targetPos, "YELLOW");
                }
            }
            itemsArrayText.setText("Inserción completada");
        }));

        timeline.play();
    }

    private void animateSearch(List<VisitedPos> recorrido, int foundBlock, int foundPos, boolean found,
            int structureLevel) {
        Timeline timeline = new Timeline();
        Duration delay = Duration.ZERO;
        Duration step = Duration.millis(ANIM_STEP_MS);

        resetAllColors();

        for (VisitedPos vp : recorrido) {
            Duration d = delay;
            timeline.getKeyFrames().add(new KeyFrame(d, e -> {
                resetAllColors();

                if (vp.pos == -2) {

                    itemsArrayText.setText("Cambiando a nivel " + (vp.structureLevel + 1));
                    return;
                }

                if (vp.structureLevel == 0) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = false;
                    currentBlockView = vp.block;
                    actualizarVistaArray();
                    if (vp.pos >= 0) {
                        setCellColor(vp.block, vp.pos, "GRAY");
                    }
                } else if (vp.structureLevel > 0) {
                    isViewingAuxiliary = true;
                    isViewingChainedList = false;
                    currentAuxiliaryLevel = vp.structureLevel - 1;
                    currentBlockView = vp.block;
                    actualizarVistaAuxiliar();
                    if (vp.pos >= 0) {
                        setAuxCellColor(vp.block, vp.pos, currentAuxiliaryLevel, "GRAY");
                    }
                } else if (vp.structureLevel == -1) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = true;
                    currentChainedPosition = vp.block;
                    actualizarVistaEncadenada();
                    resaltarElementoEnLista(vp.pos, "GRAY");
                }
            }));
            delay = delay.add(step);
        }

        timeline.getKeyFrames().add(new KeyFrame(delay, e -> {
            resetAllColors();
            String finalColor = "WHITE";
            if (found && foundBlock >= 0 && foundPos >= 0) {
                if (isSearching) {
                    finalColor = "GREEN";
                } else {
                    finalColor = "RED";
                }
                if (structureLevel == 0) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = false;
                    setCellColor(foundBlock, foundPos, finalColor);
                    currentBlockView = foundBlock;
                    actualizarVistaArray();
                } else if (structureLevel > 0) {
                    isViewingAuxiliary = true;
                    isViewingChainedList = false;
                    currentAuxiliaryLevel = structureLevel - 1;
                    setAuxCellColor(foundBlock, foundPos, currentAuxiliaryLevel, finalColor);
                    currentBlockView = foundBlock;
                    actualizarVistaAuxiliar();
                } else if (structureLevel == -1) {
                    isViewingAuxiliary = false;
                    isViewingChainedList = true;
                    currentChainedPosition = foundBlock;
                    actualizarVistaEncadenada();
                    resaltarElementoEnLista(foundPos, finalColor);
                }
            } else if (!recorrido.isEmpty()) {
                VisitedPos last = recorrido.get(recorrido.size() - 1);
                if (last.pos >= 0) {
                    if (last.structureLevel == 0) {
                        setCellColor(last.block, last.pos, "RED");
                        currentBlockView = last.block;
                        actualizarVistaArray();
                    } else if (last.structureLevel > 0) {
                        setAuxCellColor(last.block, last.pos, last.structureLevel - 1, "RED");
                        currentBlockView = last.block;
                        actualizarVistaAuxiliar();
                    } else if (last.structureLevel == -1) {
                        isViewingAuxiliary = false;
                        isViewingChainedList = true;
                        currentChainedPosition = last.block;
                        actualizarVistaEncadenada();
                        resaltarElementoEnLista(last.pos, "RED");
                    }
                }
            }
        }));

        timeline.play();
    }

    private int getBlockCapacity(int blockIndex) {
        if (blockIndex == numBlocks - 1)
            return tableSize - (numBlocks - 1) * blockSize;
        return blockSize;
    }

    private void actualizarVistaArray() {
        miViewList.getItems().clear();
        if (blocks == null)
            return;

        List<String> bloque = blocks.get(currentBlockView);
        int capacity = getBlockCapacity(currentBlockView);
        boolean tieneLista = chainedStructures.containsKey(currentBlockView);

        for (int i = 0; i < capacity; i++) {
            if (i < bloque.size()) {
                String val = bloque.get(i);
                miViewList.getItems().add("Pos " + (i + 1) + ": " + val);
            } else if (i == capacity - 1 && tieneLista) {

                miViewList.getItems().add("Pos " + (i + 1) + ": Lista");
            } else {
                miViewList.getItems().add("Pos " + (i + 1) + ": -");
            }
        }
        miViewList.refresh();
        bloquesButton.setText("Bloque " + (currentBlockView + 1));
    }

    private void actualizarVistaAuxiliar() {
        miViewList.getItems().clear();
        if (auxiliaryBlocks == null || currentAuxiliaryLevel >= auxiliaryBlocks.size()) {
            miViewList.getItems().add("Estructura auxiliar no disponible");
            return;
        }

        List<List<String>> currentAuxLevel = auxiliaryBlocks.get(currentAuxiliaryLevel);
        if (currentAuxLevel == null || currentBlockView >= currentAuxLevel.size()) {
            miViewList.getItems().add("Bloque no disponible");
            return;
        }

        List<String> bloque = currentAuxLevel.get(currentBlockView);
        int capacity = getBlockCapacity(currentBlockView);

        for (int i = 0; i < capacity; i++) {
            String val = (i < bloque.size() ? bloque.get(i) : "-");
            miViewList.getItems().add("Pos " + (i + 1) + ": " + val);
        }

        miViewList.refresh();
        bloquesButton.setText("Bloque " + (currentBlockView + 1) + " (Aux " + (currentAuxiliaryLevel + 1) + ")");
    }

    private void actualizarVistaEncadenada() {
        miViewList.getItems().clear();
        List<String> chain = chainedStructures.get(currentChainedPosition);

        if (chain != null && !chain.isEmpty()) {
            for (int i = 0; i < chain.size(); i++) {
                miViewList.getItems().add((i + 1) + ": " + chain.get(i));
            }
        } else {
            miViewList.getItems().add("Lista encadenada vacía");
        }
        miViewList.refresh();
        bloquesButton.setText("Lista Pos " + (currentChainedPosition + 1));
    }

    private void resaltarElementoEnLista(int pos, String color) {
        if (pos >= 0) {
            miViewList.getSelectionModel().select(pos);

            String style = "";
            switch (color) {
                case "GRAY":
                    style = "-fx-control-inner-background: lightgray;";
                    break;
                case "RED":
                    style = "-fx-control-inner-background: lightcoral;";
                    break;
                case "YELLOW":
                    style = "-fx-control-inner-background: yellow;";
                    break;
                case "GREEN":
                    style = "-fx-control-inner-background: lightgreen;";
                    break;
                default:
                    style = "-fx-control-inner-background: white;";
                    break;
            }
            miViewList.setStyle(style);
        }
    }

    private void populateBlocksMenu() {
        bloquesButton.getItems().clear();
        for (int i = 0; i < numBlocks; i++) {
            MenuItem it = new MenuItem("Bloque " + (i + 1));
            final int idx = i;
            it.setOnAction(e -> showBlockDetails(idx));
            bloquesButton.getItems().add(it);
        }
    }

    private void showBlockDetails(int blockIndex) {
        if (isViewingAuxiliary) {

            currentBlockView = blockIndex;
            actualizarVistaAuxiliar();

            bloquesButton.setText("Bloque " + (blockIndex + 1) + " (Aux " + (currentAuxiliaryLevel + 1) + ")");
            itemsArrayText.setText("Mostrando bloque " + (blockIndex + 1) + " en estructura auxiliar nivel "
                    + (currentAuxiliaryLevel + 1));
        } else if (isViewingChainedList) {

            List<String> chain = chainedStructures.get(blockIndex);
            if (chain != null) {
                isViewingChainedList = true;
                currentChainedPosition = blockIndex;
                actualizarVistaEncadenada();

                bloquesButton.setText("Lista Pos " + (blockIndex + 1));
                itemsArrayText.setText("Mostrando lista encadenada de la posición " + (blockIndex + 1));
            } else {
                itemsArrayText.setText("No hay lista encadenada en la posición " + (blockIndex + 1));
            }
        } else {

            currentBlockView = blockIndex;
            actualizarVistaArray();

            bloquesButton.setText("Bloque " + (blockIndex + 1));
            itemsArrayText.setText("Mostrando bloque " + (blockIndex + 1));
        }
    }

    private void resetAllColors() {
        if (cellColorsByBlock != null) {
            for (String[] arr : cellColorsByBlock.values()) {
                Arrays.fill(arr, "WHITE");
            }
        }
        resetAllAuxColors();

        miViewList.getSelectionModel().clearSelection();
        miViewList.setStyle("");
    }

    private void resetAllAuxColors() {
        if (auxCellColors == null)
            return;

        for (Map<Integer, String[]> levelColors : auxCellColors.values()) {
            for (String[] arr : levelColors.values()) {
                Arrays.fill(arr, "WHITE");
            }
        }
    }

    private void setCellColor(int blockIndex, int posZeroBased, String color) {
        String[] arr = cellColorsByBlock.get(blockIndex);
        if (arr != null && posZeroBased >= 0 && posZeroBased < arr.length) {
            arr[posZeroBased] = color;
        }
    }

    private void setAuxCellColor(int blockIndex, int posZeroBased, int level, String color) {
        if (auxCellColors == null)
            return;

        Map<Integer, String[]> levelColors = auxCellColors.get(level);
        if (levelColors != null) {
            String[] arr = levelColors.get(blockIndex);
            if (arr != null && posZeroBased >= 0 && posZeroBased < arr.length) {
                arr[posZeroBased] = color;
            }
        }
    }

    @FXML
    private void reiniciar() {
        undoStack.clear();
        redoStack.clear();
        blocks = null;
        auxiliaryBlocks.clear();
        chainedStructures.clear();
        insertedKeys.clear();
        collisionString = null;
        collisionHash.setText("Elegir");
        pendingKey = null;
        truncPositions = null;
        truncPositionsSet = false;
        isViewingAuxiliary = false;
        isViewingChainedList = false;

        arrayLengthText.setText("Array sin crear");
        itemsArrayText.setText("No hay elementos en la estructura.");
        miViewList.getItems().clear();

        rangeHash.setDisable(false);
        numberDigits.setDisable(false);
        createButton.setDisable(false);
        reiniciarButton.setDisable(true);
        insertButton.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        newItemArray.setDisable(true);
        modDeleteItem.setDisable(true);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);
        vistaAmpliada.setDisable(true);
        bloquesButton.setVisible(false);
        collisionHash.setDisable(true);
        defineCollitionsButton.setDisable(true);
        collisionCustomButton.setVisible(false);
        collisionCustomButton.setDisable(true);

        truncText.setVisible(false);
        truncButton.setVisible(false);
        truncElegir.setVisible(false);

        initData();
    }

    private void saveState(int lastModifiedBlock, int lastModifiedPosition) {
        redoStack.clear();
        undoStack.push(new ActionState(blocks, auxiliaryBlocks, chainedStructures, tableSize,
                blockSize, numBlocks, maxDigits, collisionString, hashString,
                truncPositions, truncPositionsSet,
                lastModifiedBlock, lastModifiedPosition,
                insertedKeys));
        updateUndoRedoButtons();
    }

    private void applyState(ActionState state, boolean markLast) {
        this.blocks = new ArrayList<>();
        for (List<String> b : state.getBlocksSnapshot())
            this.blocks.add(new ArrayList<>(b));

        this.auxiliaryBlocks = new ArrayList<>();
        for (List<List<String>> level : state.getAuxiliaryBlocksSnapshot()) {
            List<List<String>> levelCopy = new ArrayList<>();
            for (List<String> block : level)
                levelCopy.add(new ArrayList<>(block));
            this.auxiliaryBlocks.add(levelCopy);
        }

        this.chainedStructures = new HashMap<>(state.getChainedStructuresSnapshot());
        this.tableSize = state.getTableSizeSnapshot();
        this.blockSize = state.getBlockSizeSnapshot();
        this.numBlocks = state.getNumBlocksSnapshot();
        this.maxDigits = state.getMaxDigitsSnapshot();
        this.collisionString = state.getCollisionMethodSnapshot();
        this.hashString = state.getHashFunctionSnapshot();
        this.truncPositions = state.getTruncPositionsSnapshot() != null ? state.getTruncPositionsSnapshot().clone()
                : null;
        this.truncPositionsSet = state.getTruncPositionsSetSnapshot();
        this.insertedKeys = new ArrayList<>(state.getInsertedKeysSnapshot());

        cellColorsByBlock = new HashMap<>();
        for (int i = 0; i < numBlocks; i++)
            cellColorsByBlock.put(i, initColorArray(getBlockCapacity(i)));

        initializeAuxColors();

        showMainStructure();

        populateBlocksMenu();
        updateUndoRedoButtons();
        updateCollisionCustomButton();
        populateCollisionMenu();

        if (hashString != null) {
            initData();
        }
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
    }

    @FXML
    private void undoAction() {
        if (undoStack.size() <= 1)
            return;
        ActionState cur = undoStack.pop();
        redoStack.push(cur);
        ActionState prev = undoStack.peek();
        applyState(prev, true);
        itemsArrayText.setText("Accion deshecha.");
    }

    @FXML
    private void redoAction() {
        if (redoStack.isEmpty())
            return;
        ActionState next = redoStack.pop();
        undoStack.push(next);
        applyState(next, true);
        itemsArrayText.setText("Accion rehecha.");
    }

    @FXML
    private void vistaAmpliada(ActionEvent event) {
        try {
            Stage expandedStage = new Stage();
            expandedStage.setTitle("Vista Ampliada - Hash Externa");
            expandedStage.initModality(Modality.WINDOW_MODAL);

            if (event != null && event.getSource() instanceof Node) {
                expandedStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            }

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double stageWidth = screenBounds.getWidth() * 0.9;
            double stageHeight = screenBounds.getHeight() * 0.9;

            HBox bloquesContainer = new HBox(20);
            bloquesContainer.setPadding(new Insets(20));
            bloquesContainer.setAlignment(Pos.CENTER_LEFT);

            if (blocks == null || blocks.isEmpty()) {
                Label emptyLabel = new Label("No hay bloques para mostrar");
                emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
                bloquesContainer.getChildren().add(emptyLabel);
            } else {
                for (int i = 0; i < blocks.size(); i++) {
                    final int blockIndex = i;
                    List<String> bloque = blocks.get(blockIndex);
                    int capacity = getBlockCapacity(blockIndex);

                    ListView<String> listView = new ListView<>();
                    listView.setPrefWidth(160);
                    listView.setPrefHeight(Math.max(180, capacity * 28));

                    for (int p = 0; p < capacity; p++) {
                        String val = (p < bloque.size()) ? bloque.get(p) : "-";
                        listView.getItems().add("Pos " + (p + 1) + ": " + val);
                    }

                    listView.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                                return;
                            }
                            setText(item);

                            int idx = getIndex();
                            String color = "WHITE";
                            if (cellColorsByBlock != null) {
                                String[] arr = cellColorsByBlock.get(blockIndex);
                                if (arr != null && idx >= 0 && idx < arr.length)
                                    color = arr[idx];
                            }
                            switch (color) {
                                case "GRAY":
                                    setStyle("-fx-background-color: lightgray; -fx-text-fill: black;");
                                    break;
                                case "GREEN":
                                    setStyle("-fx-background-color: lightgreen; -fx-text-fill: black;");
                                    break;
                                case "RED":
                                    setStyle("-fx-background-color: lightcoral; -fx-text-fill: black;");
                                    break;
                                case "YELLOW":
                                    setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                                    break;
                                default:
                                    setStyle("-fx-background-color: white; -fx-text-fill: black;");
                                    break;
                            }
                        }
                    });

                    Label label = new Label("Bloque " + (blockIndex + 1));
                    label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    VBox bloqueBox = new VBox(6, label, listView);
                    bloqueBox.setAlignment(Pos.TOP_CENTER);

                    bloquesContainer.getChildren().add(bloqueBox);
                }
            }

            ScrollPane scrollPane = new ScrollPane(bloquesContainer);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setFitToHeight(true);
            scrollPane.setPannable(true);

            BorderPane root = new BorderPane(scrollPane);
            Scene scene = new Scene(root, stageWidth, stageHeight);

            expandedStage.setScene(scene);
            expandedStage.setMaximized(true);
            expandedStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void saveArray() {
        if (blocks == null) {
            itemsArrayText.setText("No hay estructura creada para guardar.");
            return;
        }
        if (undoStack.isEmpty()) {
            itemsArrayText.setText("No hay estado para guardar.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Estado Hash Externa");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HashExt", "*.hashext"));
        File file = fileChooser.showSaveDialog(miViewList.getScene().getWindow());
        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                Map<String, Object> saveData = new HashMap<>();
                saveData.put("type", "HashExterna");
                saveData.put("state", undoStack.peek());
                saveData.put("timestamp", System.currentTimeMillis());
                oos.writeObject(saveData);
                itemsArrayText.setText("Estado guardado: " + file.getName());
            } catch (IOException ex) {
                itemsArrayText.setText("Error al guardar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void loadArray() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Estado Hash Externa");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HashExt", "*.hashext"));
        File file = fileChooser.showOpenDialog(miViewList.getScene().getWindow());
        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object ld = ois.readObject();
                if (ld instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> save = (Map<String, Object>) ld;
                    String t = (String) save.get("type");
                    ActionState s = (ActionState) save.get("state");
                    if (!"HashExterna".equals(t)) {
                        Alert a = new Alert(Alert.AlertType.ERROR);
                        a.setTitle("Error");
                        a.setHeaderText("Tipo incompatible");
                        a.showAndWait();
                        itemsArrayText.setText("Tipo incompatible.");
                        return;
                    }
                    undoStack.clear();
                    redoStack.clear();
                    undoStack.push(s);
                    applyState(s, false);
                    updateUndoRedoButtons();
                    if (blocks != null)
                        actualizarEstadoControlesCreacion();
                    itemsArrayText.setText("Estado cargado: " + file.getName());
                }
            } catch (IOException | ClassNotFoundException ex) {
                itemsArrayText.setText("Error carga: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private static class InsertionResult {
        boolean success;
        int finalBlock;
        int finalPos;
        int structureLevel;
        List<VisitedPos> recorrido;

        InsertionResult(boolean s, int fb, int fp, int sl, List<VisitedPos> r) {
            success = s;
            finalBlock = fb;
            finalPos = fp;
            structureLevel = sl;
            recorrido = r;
        }
    }

    private static class SearchResult {
        boolean found;
        int foundBlock;
        int foundPos;
        int structureLevel;
        List<VisitedPos> recorrido;
        String foundKey;

        SearchResult(boolean f, int fb, int fp, int sl, List<VisitedPos> r, String fk) {
            found = f;
            foundBlock = fb;
            foundPos = fp;
            structureLevel = sl;
            recorrido = r;
            foundKey = fk;
        }
    }

    private static class VisitedPos {
        int block;
        int pos;
        int structureLevel;

        VisitedPos(int b, int p, int sl) {
            block = b;
            pos = p;
            structureLevel = sl;
        }
    }
}