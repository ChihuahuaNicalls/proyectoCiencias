package ciencias.Research;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ciencias.ResearchController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class HashController {

    @FXML
    private TextField newItemArray;
    @FXML
    private TextField modDeleteItem;
    @FXML
    private Label titleHash;
    @FXML
    private Label functionHash;
    @FXML
    private Text arrayLengthText;
    @FXML
    private Text itemsArrayText;
    @FXML
    private Text modOpText;
    @FXML
    private Button createButton;
    @FXML
    private Button reiniciarButton;
    @FXML
    private Button insertButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button defineCollitionsButton;
    @FXML
    private MenuButton collisionHash;
    @FXML
    private ListView<String> miViewList;
    @FXML
    private Spinner<Integer> numberDigits;
    @FXML
    private Text truncText;
    @FXML
    private Button truncButton;
    @FXML
    private ComboBox<Integer> truncElegir;
    @FXML
    private MenuButton rangeHash;
    @FXML
    private MenuButton collisionCustomButton;
    @FXML
    private Button saveButton;

    private int[] truncPositions;
    private int truncMaxSelections = 0;
    private boolean truncPositionsSet = false;

    private Timeline currentAnimation;
    private int highlightedChainIndex = -1;
    private String highlightChainColor = "WHITE";

    private ResearchController researchController;
    private final List<String> insertedKeys = new ArrayList<>();

    private String[] table;
    private int tableSize;
    private String hashString;
    private String collisionString;
    private int maxDigits;
    private String pendingKey = null;

    private String[] cellColors;

    private final Deque<ActionState> undoStack = new ArrayDeque<>();
    private final Deque<ActionState> redoStack = new ArrayDeque<>();

    private List<String[]> auxiliaryStructures;
    private Map<Integer, List<String>> chainedStructures;
    private int currentStructureLevel = 0;
    private int currentChainedPosition = -1;

    private static class ActionState implements Serializable {
        private final String[] tableSnapshot;
        private final int tableSizeSnapshot;
        private final int maxDigitsSnapshot;
        private final String hashStringSnapshot;
        private final String collisionMethodSnapshot;
        private final int[] truncPositionsSnapshot;
        private final boolean truncPositionsSetSnapshot;
        private final int lastModifiedPosition;
        private final List<String> insertedKeysSnapshot;
        private final List<String[]> auxiliaryStructuresSnapshot;
        private final Map<Integer, List<String>> chainedStructuresSnapshot;

        ActionState(String[] table, int tableSize, int maxDigits, String hashString,
                String collisionMethod, int[] truncPositions, boolean truncPositionsSet,
                int lastModifiedPosition, List<String> insertedKeys,
                List<String[]> auxiliaryStructures, Map<Integer, List<String>> chainedStructures) {
            this.tableSnapshot = table != null ? table.clone() : null;
            this.tableSizeSnapshot = tableSize;
            this.maxDigitsSnapshot = maxDigits;
            this.hashStringSnapshot = hashString;
            this.collisionMethodSnapshot = collisionMethod;
            this.truncPositionsSnapshot = truncPositions != null ? truncPositions.clone() : null;
            this.truncPositionsSetSnapshot = truncPositionsSet;
            this.lastModifiedPosition = lastModifiedPosition;
            this.insertedKeysSnapshot = new ArrayList<>(insertedKeys);

            this.auxiliaryStructuresSnapshot = new ArrayList<>();
            if (auxiliaryStructures != null) {
                for (String[] auxTable : auxiliaryStructures) {
                    this.auxiliaryStructuresSnapshot.add(auxTable != null ? auxTable.clone() : null);
                }
            }

            this.chainedStructuresSnapshot = new HashMap<>();
            if (chainedStructures != null) {
                for (Map.Entry<Integer, List<String>> entry : chainedStructures.entrySet()) {
                    this.chainedStructuresSnapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
        }

        public String[] getTableSnapshot() {
            return tableSnapshot;
        }

        public int getTableSizeSnapshot() {
            return tableSizeSnapshot;
        }

        public int getMaxDigitsSnapshot() {
            return maxDigitsSnapshot;
        }

        public String getHashStringSnapshot() {
            return hashStringSnapshot;
        }

        public String getCollisionMethodSnapshot() {
            return collisionMethodSnapshot;
        }

        public int[] getTruncPositionsSnapshot() {
            return truncPositionsSnapshot;
        }

        public boolean getTruncPositionsSetSnapshot() {
            return truncPositionsSetSnapshot;
        }

        public int getLastModifiedPosition() {
            return lastModifiedPosition;
        }

        public List<String> getInsertedKeysSnapshot() {
            return insertedKeysSnapshot;
        }

        public List<String[]> getAuxiliaryStructuresSnapshot() {
            return auxiliaryStructuresSnapshot;
        }

        public Map<Integer, List<String>> getChainedStructuresSnapshot() {
            return chainedStructuresSnapshot;
        }
    }

    private void marcarPosicion(int index, String color) {
        if (index < 1 || index > tableSize)
            return;
        cellColors[index] = color.toUpperCase();
        Platform.runLater(() -> {
            miViewList.refresh();
            scrollToPosition(index);
        });
    }

    private void scrollToPosition(int position) {
        if (position >= 1 && position <= tableSize) {
            Platform.runLater(() -> {
                miViewList.scrollTo(position - 1);
            });
        }
    }

    @FXML
    private void initialize() {
        newItemArray.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        modDeleteItem.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 3);
        numberDigits.setValueFactory(valueFactory);

        numberDigits.valueProperty().addListener((obs, oldValue, newValue) -> {
            if ("Truncamiento".equals(hashString)) {
                truncPositions = null;
                truncPositionsSet = false;
                setupTruncationUI();
            }
        });

        miViewList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    if (currentStructureLevel == 0) {

                        int index = getIndex() + 1;
                        if (index >= 1 && index <= tableSize) {
                            applyColorBasedOnCellState(index);
                        } else {
                            setStyle("-fx-background-color: white; -fx-text-fill: black;");
                        }
                    } else if (currentStructureLevel == -1) {

                        applyColorForChainElement(getIndex());
                    } else {

                        setStyle("-fx-background-color: white; -fx-text-fill: black;");
                    }
                }
            }

            private void applyColorBasedOnCellState(int pos) {
                if (pos < 1 || pos > tableSize || cellColors == null) {
                    setStyle("-fx-background-color: white; -fx-text-fill: black;");
                    return;
                }

                String color = cellColors[pos];
                if (color == null) {
                    setStyle("-fx-background-color: white; -fx-text-fill: black;");
                    return;
                }

                switch (color.toUpperCase()) {
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

            private void applyColorForChainElement(int chainIndex) {
                if (highlightedChainIndex == chainIndex) {
                    switch (highlightChainColor.toUpperCase()) {
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
                } else {
                    setStyle("-fx-background-color: white; -fx-text-fill: black;");
                }
            }
        });

        for (MenuItem item : collisionHash.getItems()) {
            item.setOnAction(event -> collisionHash.setText(item.getText()));
        }

        for (MenuItem item : rangeHash.getItems()) {
            item.setOnAction(event -> rangeHash.setText(item.getText()));
        }

        undoButton.setDisable(true);
        redoButton.setDisable(true);
        defineCollitionsButton.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        saveButton.setDisable(true);
        insertButton.setDisable(true);
        newItemArray.setDisable(true);
        modDeleteItem.setDisable(true);
        truncText.setVisible(false);
        truncButton.setVisible(false);
        truncElegir.setVisible(false);

        auxiliaryStructures = new ArrayList<>();
        chainedStructures = new HashMap<>();
        collisionCustomButton.setVisible(false);
        collisionCustomButton.setDisable(true);
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
        }
    }

    @FXML
    private void crearArray() {
        if ("Truncamiento".equals(hashString) && !truncPositionsSet) {
            arrayLengthText.setText("Debe seleccionar las posiciones para truncar antes de crear el array.");
            return;
        }
        maxDigits = numberDigits.getValue();

        String rangoSeleccionado = rangeHash.getText();
        if ("Elegir".equalsIgnoreCase(rangoSeleccionado)) {
            arrayLengthText.setText("Debe seleccionar un rango antes de crear el array.");
            return;
        }
        try {
            tableSize = Integer.parseInt(rangoSeleccionado);
        } catch (NumberFormatException e) {
            arrayLengthText.setText("Error: rango invalido.");
            return;
        }

        if ("Truncamiento".equals(hashString) && (truncPositions == null || truncPositions.length == 0)) {
            arrayLengthText.setText("Error: No se han definido posiciones de truncamiento.");
            return;
        }

        table = new String[tableSize + 1];
        cellColors = new String[tableSize + 1];
        Arrays.fill(table, null);
        Arrays.fill(cellColors, "WHITE");

        auxiliaryStructures.clear();
        chainedStructures.clear();
        currentStructureLevel = 0;
        currentChainedPosition = -1;

        saveState(-1);

        createButton.setDisable(true);
        rangeHash.setDisable(true);
        numberDigits.setDisable(true);
        if ("Truncamiento".equals(hashString)) {
            truncButton.setDisable(true);
            truncElegir.setDisable(true);
        }
        reiniciarButton.setDisable(false);
        insertButton.setDisable(false);
        newItemArray.setDisable(false);
        modDeleteItem.setDisable(false);
        searchButton.setDisable(false);
        deleteButton.setDisable(false);
        saveButton.setDisable(false);
        undoButton.setDisable(true);
        redoButton.setDisable(true);

        arrayLengthText.setText("Array de " + tableSize + " posiciones creado. Claves de " + maxDigits + " digitos.");
        if ("Truncamiento".equals(hashString)) {
            arrayLengthText
                    .setText(arrayLengthText.getText() + "\nPosiciones truncadas: " + Arrays.toString(truncPositions));
        }
        actualizarVistaArray();
    }

    private void doInsert(String claveStr) {
        System.out.println("Iniciando insercion de: " + claveStr);
        resetearAnimacion();

        int claveInt;
        try {
            claveInt = Integer.parseInt(claveStr);
        } catch (NumberFormatException e) {
            itemsArrayText.setText("Error: La clave debe ser numerica.");
            return;
        }

        if (insertedKeys.size() >= tableSize) {
            itemsArrayText.setText("Tabla llena. No se pudo insertar " + claveStr + ".");
            return;
        }

        if ("Anidamiento".equals(collisionString)) {
            InsertionResult result = doInsertNested(claveStr, claveInt, 0);
            if (result.success) {
                itemsArrayText.setText(result.message);
                animateInsertion(result.recorrido, result.finalPosition, result.structureLevel);
            }
        } else if ("Encadenamiento".equals(collisionString)) {
            int pos = aplicarFuncionHash(claveInt);
            boolean hayColision = (table[pos] != null && !table[pos].equals(claveStr));
            InsertionResult result = doInsertChained(claveStr, claveInt, hayColision);
            if (result.success) {
                itemsArrayText.setText(result.message);
                animateInsertion(result.recorrido, result.finalPosition, result.structureLevel);
            }
        } else {
            InsertionResult result = doInsertStandard(claveStr, claveInt);
            if (result.success) {
                itemsArrayText.setText(result.message);
                animateInsertion(result.recorrido, result.finalPosition, 0);
            }
        }

        limpiarEstructurasVacias();
    }

    private InsertionResult doInsertChained(String claveStr, int claveInt, boolean hayColision) {
        int pos = aplicarFuncionHash(claveInt);
        List<Integer> recorrido = new ArrayList<>();
        recorrido.add(pos);

        if (table[pos] == null) {
            table[pos] = claveStr;
            insertedKeys.add(claveStr);
            saveState(pos);
            return new InsertionResult(true, pos, 0, recorrido,
                    "Clave " + claveStr + " insertada en pos " + pos + ".");
        } else {
            if (table[pos].equals(claveStr)) {
                itemsArrayText.setText("Error: La clave " + claveStr + " ya existe en la posicion " + pos + ".");
                return new InsertionResult(false, -1, 0, recorrido, "");
            }

            if (hayColision) {
                List<String> chain = chainedStructures.getOrDefault(pos, new ArrayList<>());
                if (chain.contains(claveStr)) {
                    itemsArrayText.setText("Error: La clave " + claveStr + " ya existe en la lista de encadenamiento.");
                    return new InsertionResult(false, -1, 0, recorrido, "");
                }

                if (chain.isEmpty()) {
                    String existingKey = table[pos];
                    chain.add(existingKey);
                    table[pos] = "Lista";

                    Platform.runLater(() -> {
                        updateCollisionCustomButton();
                        populateCollisionMenu();
                    });
                }

                for (int i = 0; i < chain.size(); i++) {
                    recorrido.add(pos * 100 + i);
                }

                recorrido.add(pos * 100 + chain.size());

                chain.add(claveStr);
                chainedStructures.put(pos, chain);

                insertedKeys.add(claveStr);
                saveState(pos);

                Platform.runLater(() -> {
                    updateCollisionCustomButton();
                    populateCollisionMenu();
                });

                return new InsertionResult(true, pos, -1, recorrido,
                        "Clave " + claveStr + " insertada en lista de encadenamiento de la pos " + pos + ".");
            } else {
                itemsArrayText.setText("Error inesperado en insercion con encadenamiento.");
                return new InsertionResult(false, -1, 0, recorrido, "");
            }
        }
    }

    private InsertionResult doInsertNested(String claveStr, int claveInt, int structureLevel) {
        String[] currentTable = getTableForLevel(structureLevel);
        int pos = aplicarFuncionHash(claveInt);
        List<Integer> recorrido = new ArrayList<>();

        recorrido.add(structureLevel * 1000 + pos);

        if (currentTable[pos] == null) {
            currentTable[pos] = claveStr;
            insertedKeys.add(claveStr);
            saveState(pos);
            return new InsertionResult(true, pos, structureLevel, recorrido,
                    "Clave " + claveStr + " insertada en " + getStructureName(structureLevel) + " en pos " + pos + ".");
        } else {
            if (currentTable[pos].equals(claveStr)) {
                itemsArrayText.setText(
                        "Error: La clave " + claveStr + " ya existe en " + getStructureName(structureLevel) + ".");
                return new InsertionResult(false, -1, structureLevel, recorrido, "");
            }

            recorrido.add((structureLevel + 1) * 1000);

            InsertionResult result = doInsertNested(claveStr, claveInt, structureLevel + 1);
            if (result.success) {

                recorrido.addAll(result.recorrido);
            }
            return result;
        }
    }

    private InsertionResult doInsertStandard(String claveStr, int claveInt) {
        int pos = aplicarFuncionHash(claveInt);
        int step = 1;
        int intentos = 0;
        Set<Integer> posicionesVisitadas = new HashSet<>();
        boolean usarAuxiliar = false;
        List<Integer> recorrido = new ArrayList<>();
        recorrido.add(pos);

        while (table[pos] != null) {
            if (table[pos].equals(claveStr)) {
                itemsArrayText.setText("Error: La clave " + claveStr + " ya existe.");
                return new InsertionResult(false, -1, 0, recorrido, "");
            }

            if (collisionString == null) {
                itemsArrayText.setText(
                        "¡Colision detectada en la posicion " + pos + "!\nElija y defina un metodo de resolucion.");
                pendingKey = claveStr;
                insertButton.setDisable(true);
                searchButton.setDisable(true);
                deleteButton.setDisable(true);
                newItemArray.setDisable(true);
                modDeleteItem.setDisable(true);
                undoButton.setDisable(true);
                redoButton.setDisable(true);
                rangeHash.setDisable(true);
                collisionHash.setDisable(false);
                defineCollitionsButton.setDisable(false);
                return new InsertionResult(false, -1, 0, recorrido, "");
            }

            posicionesVisitadas.add(pos);
            int nextPos;
            if (usarAuxiliar) {
                nextPos = siguientePosicionAuxiliar(pos, step, claveInt);
            } else {
                nextPos = siguientePosicion(pos, step, claveInt);
                if (posicionesVisitadas.contains(nextPos)) {
                    usarAuxiliar = true;
                    nextPos = siguientePosicionAuxiliar(pos, step, claveInt);
                }
            }

            pos = nextPos;
            recorrido.add(pos);
            step++;
            intentos++;

            if (intentos >= tableSize * 2) {
                int ocupadas = 0;
                for (int i = 1; i <= tableSize; i++) {
                    if (table[i] != null)
                        ocupadas++;
                }
                if (ocupadas >= tableSize) {
                    itemsArrayText.setText("Tabla llena. No se pudo insertar " + claveStr + ".");
                    return new InsertionResult(false, -1, 0, recorrido, "");
                } else {
                    usarAuxiliar = true;
                    intentos = tableSize;
                }
            }
        }

        table[pos] = claveStr;
        insertedKeys.add(claveStr);
        saveState(pos);
        return new InsertionResult(true, pos, 0, recorrido,
                "Clave " + claveStr + " insertada en pos " + pos + ".");
    }

    private void animateInsertion(List<Integer> recorrido, int finalPosition, int structureLevel) {
        System.out.println("Iniciando animacion de insercion ANIDAMIENTO - Recorrido: " + recorrido);
        System.out.println("Posicion final: " + finalPosition);
        System.out.println("Nivel estructura: " + structureLevel);

        resetearAnimacion();

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        currentAnimation = new Timeline();
        Duration delay = Duration.ZERO;
        Duration step = Duration.seconds(1.0);

        for (int i = 0; i < recorrido.size(); i++) {
            final int currentStep = i;
            final int currentCode = recorrido.get(i);

            KeyFrame keyFrame = new KeyFrame(delay, e -> {
                System.out.println("Animando paso " + currentStep + ": codigo " + currentCode);

                if (currentCode >= 1000) {

                    int nivel = currentCode / 1000;
                    int pos = currentCode % 1000;

                    if (pos == 0) {

                        showAuxiliaryStructure(nivel);
                        itemsArrayText.setText("Cambiando a " + getStructureName(nivel) + "...");
                    } else {

                        showAuxiliaryStructure(nivel);
                        marcarPosicionEnEstructuraAuxiliar(pos, "GRAY", nivel);
                        itemsArrayText.setText("Buscando en " + getStructureName(nivel) + " posicion " + pos);
                    }
                } else if (currentCode >= 100) {

                    int chainPos = currentCode / 100;
                    int chainIndex = currentCode % 100;
                    showChainedList(chainPos);
                    highlightChainElement(chainIndex, "GRAY");
                } else {

                    showMainStructure();
                    marcarPosicion(currentCode, "GRAY");
                    itemsArrayText.setText("Buscando en estructura principal posicion " + currentCode);
                }
            });

            currentAnimation.getKeyFrames().add(keyFrame);
            delay = delay.add(step);
        }

        KeyFrame finalFrame = new KeyFrame(delay, e -> {
            System.out.println("Animacion finalizada en estructura: " + structureLevel);

            if (structureLevel == -1) {

                showChainedList(finalPosition);
                if (!recorrido.isEmpty()) {
                    int last = recorrido.get(recorrido.size() - 1);
                    if (last >= 100) {
                        highlightChainElement(last % 100, "YELLOW");
                    }
                }
                itemsArrayText.setText("Insercion completada en lista encadenada");
            } else if (structureLevel > 0) {

                showAuxiliaryStructure(structureLevel);
                marcarPosicionEnEstructuraAuxiliar(finalPosition, "YELLOW", structureLevel);
                itemsArrayText.setText(
                        "Insercion completada en " + getStructureName(structureLevel) + " posicion " + finalPosition);
            } else {

                showMainStructure();
                marcarPosicion(finalPosition, "YELLOW");
                itemsArrayText.setText("Insercion completada en estructura principal posicion " + finalPosition);
            }

            currentAnimation = null;
            limpiarEstructurasVacias();
        });

        currentAnimation.getKeyFrames().add(finalFrame);
        currentAnimation.play();
    }

    private void limpiarEstructurasVacias() {
        boolean changed = false;

        Iterator<Map.Entry<Integer, List<String>>> chainIterator = chainedStructures.entrySet().iterator();
        while (chainIterator.hasNext()) {
            Map.Entry<Integer, List<String>> entry = chainIterator.next();
            List<String> chain = entry.getValue();
            if (chain == null || chain.isEmpty()) {
                int position = entry.getKey();
                chainIterator.remove();
                if (table[position] != null && table[position].equals("Lista")) {
                    table[position] = null;
                }
                changed = true;
            }
        }

        Iterator<String[]> auxIterator = auxiliaryStructures.iterator();
        while (auxIterator.hasNext()) {
            String[] auxTable = auxIterator.next();
            boolean isEmpty = true;
            for (int i = 1; i <= tableSize; i++) {
                if (auxTable[i] != null) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                auxIterator.remove();
                changed = true;
            }
        }

        if (changed) {
            Platform.runLater(() -> {
                updateCollisionCustomButton();
                actualizarVistaArray();
            });
        }
    }

    @FXML
    private void defineCollitions() {
        String selected = collisionHash.getText();
        if ("Elegir".equalsIgnoreCase(selected)) {
            itemsArrayText.setText("Debe seleccionar un metodo valido.");
            return;
        }

        this.collisionString = selected;
        collisionHash.setDisable(true);
        defineCollitionsButton.setDisable(true);

        Platform.runLater(() -> {
            updateCollisionCustomButton();
            populateCollisionMenu();
        });

        insertButton.setDisable(false);
        searchButton.setDisable(false);
        deleteButton.setDisable(false);
        newItemArray.setDisable(false);
        modDeleteItem.setDisable(false);
        undoButton.setDisable(false);
        redoButton.setDisable(false);
        rangeHash.setDisable(true);

        itemsArrayText.setText(
                "Metodo de colision '" + collisionString + "' definido.\nIntentando insertar clave pendiente...");

        if (pendingKey != null) {
            String keyToInsert = pendingKey;
            pendingKey = null;
            doInsert(keyToInsert);
        }
    }

    private int aplicarFuncionHash(int clave) {
        switch (hashString) {
            case "Modulo":
                return (clave % tableSize) + 1;
            case "Cuadrada":
                long sq = (long) clave * clave;
                String s = String.valueOf(sq);
                int digitos = (int) Math.log10(tableSize) + 1;
                int digitosCentrales = digitos - 1;
                if (tableSize >= 1000)
                    digitosCentrales = digitos - 1;
                int start = (s.length() - digitosCentrales) / 2;
                if (start < 0)
                    start = 0;
                String sub = s.substring(start, Math.min(start + digitosCentrales, s.length()));
                if (sub.isEmpty())
                    return 1;
                int resultadoCuadrada = Integer.parseInt(sub) % tableSize;
                return resultadoCuadrada + 1;
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
                int resultadoPlegamiento = sum % divisor;
                return (resultadoPlegamiento % tableSize) + 1;
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
                    return 1;
                int resultadoTruncamiento = Integer.parseInt(truncatedNum.toString());
                return (resultadoTruncamiento % tableSize) + 1;
            default:
                return (clave % tableSize) + 1;
        }
    }

    private int siguientePosicion(int posActual, int step, int claveInt) {
        if (collisionString == null)
            return posActual;
        int next;
        switch (collisionString) {
            case "Lineal":
                next = ((posActual - 1) + 1) % tableSize + 1;
                break;
            case "Cuadratica":
                next = ((posActual - 1) + (step * step)) % tableSize + 1;
                break;
            case "Doble Hash":
                int nuevo = aplicarFuncionHash(posActual);
                nuevo = ((nuevo - 1) % tableSize + tableSize) % tableSize + 1;
                if (nuevo == posActual)
                    nuevo = (posActual % tableSize) + 1;
                next = nuevo;
                break;
            default:
                next = posActual;
        }
        return next;
    }

    private int siguientePosicionAuxiliar(int posActual, int step, int claveInt) {
        return (posActual % tableSize) + 1;
    }

    private void findItem(String claveStr, boolean eliminar) {
        System.out.println("Buscando clave: " + claveStr + ", eliminar: " + eliminar);
        resetearAnimacion();

        if (table == null) {
            itemsArrayText.setText("No hay tabla creada.");
            return;
        }

        int clave = Integer.parseInt(claveStr);

        long inicio = System.nanoTime();

        SearchResult result;
        if ("Anidamiento".equals(collisionString)) {
            result = searchInNestedStructures(claveStr, clave, 0, new ArrayList<>());
        } else if ("Encadenamiento".equals(collisionString)) {
            result = searchInChainedStructures(claveStr, clave, new ArrayList<>());
        } else {
            result = searchStandard(claveStr, clave, new ArrayList<>());
        }

        long fin = System.nanoTime();
        long nanos = fin - inicio;
        String tiempo = nanos < 1_000_000 ? nanos + " ns" : String.format("%.4f ms", nanos / 1_000_000.0);

        if (result.found) {
            if (eliminar) {
                removeFromStructure(result);
                saveState(result.position);
                itemsArrayText
                        .setText("Clave " + claveStr + " eliminada en " + result.structureType + " en " + tiempo + ".");
            } else {
                itemsArrayText.setText(
                        "Clave " + claveStr + " encontrada en " + result.structureType + " en " + tiempo + ".");
            }
            List<Integer> chainIndices = result.chainIndices != null ? result.chainIndices : new ArrayList<>();
            animateSearch(result.recorrido, true, result.position, eliminar, result.structureLevel, chainIndices);
        } else {
            itemsArrayText.setText("Clave " + claveStr + " no encontrada tras " + result.recorrido.size()
                    + " intentos en " + tiempo + ".");
            List<Integer> chainIndices = result.chainIndices != null ? result.chainIndices : new ArrayList<>();
            animateSearch(result.recorrido, false, -1, eliminar, 0, chainIndices);
        }

        if (eliminar) {
            limpiarEstructurasVacias();
        }
    }

    private void resetearAnimacion() {
        System.out.println("Reseteando animacion...");

        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }

        if (cellColors != null) {
            Arrays.fill(cellColors, "WHITE");
        }

        highlightedChainIndex = -1;
        highlightChainColor = "WHITE";

        Platform.runLater(() -> {
            miViewList.getSelectionModel().clearSelection();
            miViewList.refresh();
        });
    }

    private SearchResult searchInNestedStructures(String claveStr, int claveInt, int structureLevel,
            List<Integer> recorrido) {
        String[] currentTable = getTableForLevel(structureLevel);
        if (currentTable == null) {
            return new SearchResult(false, -1, recorrido, "", claveStr, structureLevel);
        }

        int pos = aplicarFuncionHash(claveInt);

        recorrido.add(structureLevel * 1000 + pos);

        if (currentTable[pos] != null && currentTable[pos].equals(claveStr)) {
            return new SearchResult(true, pos, recorrido, getStructureName(structureLevel), claveStr, structureLevel);
        } else if (currentTable[pos] != null) {

            recorrido.add((structureLevel + 1) * 1000);

            return searchInNestedStructures(claveStr, claveInt, structureLevel + 1, recorrido);
        }

        return new SearchResult(false, -1, recorrido, "", claveStr, structureLevel);
    }

    private SearchResult searchInChainedStructures(String claveStr, int claveInt, List<Integer> recorrido) {
        int pos = aplicarFuncionHash(claveInt);
        List<Integer> chainIndices = new ArrayList<>();

        recorrido.add(pos);

        if (table[pos] != null) {
            if (table[pos].equals(claveStr)) {
                return new SearchResult(true, pos, recorrido, "estructura principal", claveStr, 0);
            } else if (table[pos].equals("Lista")) {
                List<String> chain = chainedStructures.get(pos);
                if (chain != null) {
                    for (int i = 0; i < chain.size(); i++) {
                        chainIndices.add(i);
                        recorrido.add(pos * 100 + i);

                        if (chain.get(i).equals(claveStr)) {
                            return new SearchResult(true, pos, recorrido,
                                    "Lista de encadenamiento posicion " + pos, claveStr, -1, chainIndices);
                        }
                    }
                }
            }
        }

        return new SearchResult(false, -1, recorrido, "", claveStr, 0);
    }

    private SearchResult searchStandard(String claveStr, int claveInt, List<Integer> recorrido) {
        int pos = aplicarFuncionHash(claveInt);
        int originalPos = pos;
        int step = 1;
        Set<Integer> posicionesVisitadas = new HashSet<>();
        boolean usarAuxiliar = false;

        recorrido.add(pos);

        while (table[pos] != null) {
            if (table[pos].equals(claveStr)) {
                return new SearchResult(true, pos, recorrido, "estructura principal", claveStr, 0);
            }

            if (collisionString == null)
                break;

            posicionesVisitadas.add(pos);
            int nextPos;
            if (usarAuxiliar) {
                nextPos = siguientePosicionAuxiliar(pos, step, claveInt);
            } else {
                if ("Doble Hash".equals(collisionString)) {
                    nextPos = siguientePosicion(pos, step, claveInt);
                } else {
                    nextPos = siguientePosicion(originalPos, step, claveInt);
                }
                if (posicionesVisitadas.contains(nextPos)) {
                    usarAuxiliar = true;
                    nextPos = siguientePosicionAuxiliar(pos, step, claveInt);
                }
            }

            pos = nextPos;
            recorrido.add(pos);
            step++;

            if (step > tableSize * 2)
                break;
        }

        return new SearchResult(false, -1, recorrido, "", claveStr, 0);
    }

    private void animateSearch(List<Integer> recorrido, boolean found, int foundPos, boolean eliminar,
            int structureLevel, List<Integer> chainIndices) {
        System.out.println("Iniciando animacion de busqueda ANIDAMIENTO - Recorrido: " + recorrido);
        System.out.println("Encontrado: " + found + ", Estructura: " + structureLevel);

        resetearAnimacion();

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        currentAnimation = new Timeline();
        Duration delay = Duration.ZERO;
        Duration step = Duration.seconds(1.0);

        for (int i = 0; i < recorrido.size(); i++) {
            final int currentStep = i;
            final int currentCode = recorrido.get(i);

            KeyFrame keyFrame = new KeyFrame(delay, e -> {
                System.out.println("Animando busqueda paso " + currentStep + ": codigo " + currentCode);

                if (currentCode >= 1000) {

                    int nivel = currentCode / 1000;
                    int pos = currentCode % 1000;

                    if (pos == 0) {

                        showAuxiliaryStructure(nivel);
                        itemsArrayText.setText("Cambiando a " + getStructureName(nivel) + "...");
                    } else {

                        showAuxiliaryStructure(nivel);
                        marcarPosicionEnEstructuraAuxiliar(pos, "GRAY", nivel);
                        itemsArrayText.setText("Buscando en " + getStructureName(nivel) + " posicion " + pos);
                    }
                } else if (currentCode >= 100) {

                    int chainPos = currentCode / 100;
                    int chainIndex = currentCode % 100;
                    showChainedList(chainPos);
                    highlightChainElement(chainIndex, "GRAY");
                } else {

                    showMainStructure();
                    marcarPosicion(currentCode, "GRAY");
                    itemsArrayText.setText("Buscando en estructura principal posicion " + currentCode);
                }
            });

            currentAnimation.getKeyFrames().add(keyFrame);
            delay = delay.add(step);
        }

        KeyFrame finalFrame = new KeyFrame(delay, e -> {
            System.out.println("Animacion de busqueda finalizada - Encontrado: " + found);

            String colorFinal = found ? (eliminar ? "RED" : "GREEN") : "RED";
            String accion = eliminar ? "eliminado" : "encontrado";

            if (found) {
                if (structureLevel == -1) {
                    showChainedList(foundPos);
                    if (!chainIndices.isEmpty()) {
                        int lastIndex = chainIndices.get(chainIndices.size() - 1);
                        highlightChainElement(lastIndex, colorFinal);
                    }
                    itemsArrayText.setText("Elemento " + accion + " en lista encadenada");
                } else if (structureLevel > 0) {
                    showAuxiliaryStructure(structureLevel);
                    marcarPosicionEnEstructuraAuxiliar(foundPos, colorFinal, structureLevel);
                    itemsArrayText.setText(
                            "Elemento " + accion + " en " + getStructureName(structureLevel) + " posicion " + foundPos);
                } else {
                    showMainStructure();
                    marcarPosicion(foundPos, colorFinal);
                    itemsArrayText.setText("Elemento " + accion + " en estructura principal posicion " + foundPos);
                }
            } else {
                if (!recorrido.isEmpty()) {
                    int last = recorrido.get(recorrido.size() - 1);
                    if (last >= 1000) {
                        int nivel = last / 1000;
                        int pos = last % 1000;
                        if (pos > 0) {
                            showAuxiliaryStructure(nivel);
                            marcarPosicionEnEstructuraAuxiliar(pos, "RED", nivel);
                        } else {
                            showAuxiliaryStructure(nivel);
                        }
                    } else if (last >= 100) {
                        showChainedList(last / 100);
                        highlightChainElement(last % 100, "RED");
                    } else {
                        showMainStructure();
                        marcarPosicion(last, "RED");
                    }
                }
                itemsArrayText.setText("Elemento no encontrado despues de " + recorrido.size() + " intentos");
            }

            currentAnimation = null;
            limpiarEstructurasVacias();
        });

        currentAnimation.getKeyFrames().add(finalFrame);
        currentAnimation.play();
    }

    private void marcarPosicionEnEstructuraAuxiliar(int index, String color, int structureLevel) {
        if (index < 1 || index > tableSize)
            return;

        showAuxiliaryStructure(structureLevel);

        Platform.runLater(() -> {
            if (index >= 1 && index <= tableSize) {
                miViewList.getSelectionModel().clearSelection();
                miViewList.getSelectionModel().select(index - 1);
                miViewList.scrollTo(index - 1);

                miViewList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if (getIndex() == index - 1) {
                                switch (color.toUpperCase()) {
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
                            } else {
                                setStyle("-fx-background-color: white; -fx-text-fill: black;");
                            }
                        }
                    }
                });
                miViewList.refresh();
            }
        });
    }

    private void marcarPosicionEnEstructura(int index, String color, int structureLevel) {
        if (index < 1 || index > tableSize)
            return;

        String[] auxTable = getTableForLevel(structureLevel);
        if (auxTable != null && auxTable[index] != null) {

            actualizarVistaAuxiliar(structureLevel);

            Platform.runLater(() -> {
                miViewList.refresh();

                if (index >= 1 && index <= tableSize) {
                    miViewList.scrollTo(index - 1);
                }
            });
        }
    }

    private void highlightChainElement(int chainIndex, String color) {
        highlightedChainIndex = chainIndex;
        highlightChainColor = color.toUpperCase();
        Platform.runLater(() -> {
            miViewList.refresh();
            if (chainIndex >= 0 && miViewList.getItems().size() > chainIndex) {
                miViewList.scrollTo(chainIndex);
            }
        });
    }

    @FXML
    private void searchItem() {
        String claveStr = modDeleteItem.getText();
        if (claveStr.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave valida.");
            return;
        }
        if (!claveStr.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("La clave debe tener " + maxDigits + " digitos.");
            return;
        }
        findItem(claveStr, false);
    }

    @FXML
    private void eliminateItem() {
        String claveStr = modDeleteItem.getText();
        if (claveStr.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave valida.");
            return;
        }
        if (!claveStr.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("La clave debe tener " + maxDigits + " digitos.");
            return;
        }
        findItem(claveStr, true);
        modDeleteItem.clear();
    }

    @FXML
    private void addToArray() {
        String input = newItemArray.getText();
        if (input.isEmpty()) {
            itemsArrayText.setText("Por favor, ingrese una clave.");
            return;
        }

        if (!input.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("Error: La clave debe tener exactamente " + maxDigits + " digitos.");
            return;
        }

        doInsert(input);
        newItemArray.clear();
    }

    private void removeFromStructure(SearchResult result) {
        if (result.foundKey == null)
            return;

        if (result.structureType.equals("estructura principal")) {
            table[result.position] = null;
            insertedKeys.remove(result.foundKey);
        } else if (result.structureType.startsWith("Estructura auxiliar")) {
            int level = Integer.parseInt(result.structureType.replace("Estructura auxiliar ", ""));
            String[] auxTable = getTableForLevel(level);
            if (auxTable != null) {
                auxTable[result.position] = null;
                insertedKeys.remove(result.foundKey);
            }
        } else if (result.structureType.startsWith("Lista de encadenamiento")) {
            int position = result.position;
            List<String> chain = chainedStructures.get(position);
            if (chain != null) {
                chain.remove(result.foundKey);
                insertedKeys.remove(result.foundKey);
            }
        }

        limpiarEstructurasVacias();
    }

    private void saveState(int lastModifiedPosition) {
        redoStack.clear();
        undoStack.push(new ActionState(
                table, tableSize, maxDigits, hashString,
                collisionString, truncPositions, truncPositionsSet,
                lastModifiedPosition, insertedKeys,
                auxiliaryStructures, chainedStructures));
        updateUndoRedoButtons();
    }

    private void applyState(ActionState state, boolean markLastModified) {
        this.table = state.getTableSnapshot() != null ? state.getTableSnapshot().clone() : null;
        this.tableSize = state.getTableSizeSnapshot();
        this.maxDigits = state.getMaxDigitsSnapshot();
        this.hashString = state.getHashStringSnapshot();
        this.collisionString = state.getCollisionMethodSnapshot();
        this.truncPositions = state.getTruncPositionsSnapshot() != null ? state.getTruncPositionsSnapshot().clone()
                : null;
        this.truncPositionsSet = state.getTruncPositionsSetSnapshot();

        this.auxiliaryStructures = new ArrayList<>();
        for (String[] auxTable : state.getAuxiliaryStructuresSnapshot()) {
            this.auxiliaryStructures.add(auxTable != null ? auxTable.clone() : null);
        }

        this.chainedStructures = new HashMap<>();
        for (Map.Entry<Integer, List<String>> entry : state.getChainedStructuresSnapshot().entrySet()) {
            this.chainedStructures.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        insertedKeys.clear();
        insertedKeys.addAll(state.getInsertedKeysSnapshot());

        if (cellColors == null || cellColors.length != tableSize + 1) {
            cellColors = new String[tableSize + 1];
        }
        Arrays.fill(cellColors, "WHITE");

        updateCollisionCustomButton();

        int lastPos = state.getLastModifiedPosition();
        if (markLastModified && lastPos != -1) {
            marcarPosicion(lastPos, "GRAY");
        }

        handleUIState(state);
        actualizarVistaArray();
        saveButton.setDisable(table == null);
    }

    @FXML
    private void undoAction() {
        if (undoStack.size() <= 1)
            return;
        ActionState currentState = undoStack.pop();
        redoStack.push(currentState);
        ActionState previousState = undoStack.peek();
        applyState(previousState, true);
        updateUndoRedoButtons();
    }

    @FXML
    private void redoAction() {
        if (redoStack.isEmpty())
            return;
        ActionState nextState = redoStack.pop();
        undoStack.push(nextState);
        applyState(nextState, true);
        updateUndoRedoButtons();
    }

    private void handleUIState(ActionState state) {
        if (state.getCollisionMethodSnapshot() == null) {
            collisionHash.setDisable(true);
            collisionHash.setText("Elegir");
            defineCollitionsButton.setDisable(true);
            insertButton.setDisable(false);
            newItemArray.setDisable(false);
            modDeleteItem.setDisable(false);
            searchButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            collisionHash.setText(state.getCollisionMethodSnapshot());
            collisionHash.setDisable(true);
            defineCollitionsButton.setDisable(true);
            insertButton.setDisable(false);
            newItemArray.setDisable(false);
            modDeleteItem.setDisable(false);
            searchButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
    }

    @FXML
    private void reiniciar() {
        undoStack.clear();
        redoStack.clear();
        insertedKeys.clear();
        auxiliaryStructures.clear();
        chainedStructures.clear();
        currentStructureLevel = 0;
        currentChainedPosition = -1;

        table = null;
        arrayLengthText.setText("Array sin crear");
        itemsArrayText.setText("No hay elementos en el array");
        miViewList.getItems().clear();

        collisionString = null;
        collisionHash.setText("Elegir");
        pendingKey = null;

        collisionCustomButton.setVisible(false);
        collisionCustomButton.setDisable(true);

        rangeHash.setDisable(false);
        numberDigits.setDisable(false);
        createButton.setDisable(false);

        reiniciarButton.setDisable(true);
        insertButton.setDisable(true);
        newItemArray.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        modDeleteItem.setDisable(true);
        collisionHash.setDisable(true);
        defineCollitionsButton.setDisable(true);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);

        updateUndoRedoButtons();

        if ("Truncamiento".equalsIgnoreCase(hashString)) {
            truncElegir.setDisable(false);
            truncElegir.setVisible(true);
            truncText.setVisible(true);
            truncButton.setDisable(false);
            truncButton.setVisible(true);
            truncPositions = null;
            truncPositionsSet = false;
            setupTruncationUI();
        } else {
            truncElegir.setDisable(true);
            truncElegir.setVisible(false);
            truncText.setVisible(false);
            truncButton.setDisable(true);
            truncButton.setVisible(false);
        }
    }

    private void actualizarVistaArray() {
        miViewList.getItems().clear();
        if (table == null)
            return;
        for (int i = 1; i <= tableSize; i++) {
            String valor;
            if (table[i] != null && table[i].equals("Lista") && chainedStructures.containsKey(i)) {
                valor = "Lista";
            } else {
                valor = (table[i] == null) ? "-" : table[i];
            }
            miViewList.getItems().add("Pos " + i + ": " + valor);
            if (cellColors != null && i < cellColors.length && cellColors[i] == null) {
                cellColors[i] = "WHITE";
            }
        }
        miViewList.refresh();
    }

    private void actualizarVistaAuxiliar(int level) {
        miViewList.getItems().clear();
        if (level <= 0 || level > auxiliaryStructures.size())
            return;
        String[] auxTable = auxiliaryStructures.get(level - 1);
        for (int i = 1; i <= tableSize; i++) {
            String valor = (auxTable[i] == null) ? "-" : auxTable[i];
            miViewList.getItems().add("Pos " + i + ": " + valor);
        }
        miViewList.refresh();
    }

    private void actualizarVistaEncadenamiento(int position) {
        miViewList.getItems().clear();
        List<String> chain = chainedStructures.get(position);
        if (chain != null) {
            for (int i = 0; i < chain.size(); i++) {
                miViewList.getItems().add("Elemento " + (i + 1) + ": " + chain.get(i));
            }
        }
        miViewList.refresh();
    }

    private String[] getTableForLevel(int level) {
        if (level == 0)
            return table;
        int index = level - 1;
        if (index < auxiliaryStructures.size())
            return auxiliaryStructures.get(index);
        if (index == auxiliaryStructures.size()) {
            String[] newAuxTable = new String[tableSize + 1];
            auxiliaryStructures.add(newAuxTable);
            updateCollisionCustomButton();
            return newAuxTable;
        }
        return null;
    }

    private String getStructureName(int level) {
        return level == 0 ? "estructura principal" : "Estructura auxiliar " + level;
    }

    private void updateCollisionCustomButton() {
        if ("Anidamiento".equals(collisionString) || "Encadenamiento".equals(collisionString)) {
            collisionCustomButton.setVisible(true);
            collisionCustomButton.setDisable(false);
            populateCollisionMenu();
            collisionCustomButton.requestLayout();
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
            for (int i = 0; i < auxiliaryStructures.size(); i++) {
                MenuItem auxItem = new MenuItem("Estructura auxiliar " + (i + 1));
                final int level = i + 1;
                auxItem.setOnAction(e -> showAuxiliaryStructure(level));
                collisionCustomButton.getItems().add(auxItem);
            }
        } else if ("Encadenamiento".equals(collisionString)) {
            for (Integer position : chainedStructures.keySet()) {
                List<String> chain = chainedStructures.get(position);
                if (chain != null && !chain.isEmpty()) {
                    MenuItem chainItem = new MenuItem("Lista auxiliar posicion " + position);
                    final int pos = position;
                    chainItem.setOnAction(e -> showChainedList(pos));
                    collisionCustomButton.getItems().add(chainItem);
                }
            }
        }

        if (collisionCustomButton.getItems().size() > 1) {
            collisionCustomButton
                    .setText("Estructuras disponibles (" + (collisionCustomButton.getItems().size() - 1) + ")");
        } else {
            collisionCustomButton.setText("Estructuras disponibles");
        }
    }

    private void showMainStructure() {
        currentStructureLevel = 0;
        currentChainedPosition = -1;
        highlightedChainIndex = -1;
        actualizarVistaArray();
        collisionCustomButton.setText("Estructura principal");
    }

    private void showAuxiliaryStructure(int level) {
        currentStructureLevel = level;
        currentChainedPosition = -1;
        highlightedChainIndex = -1;
        actualizarVistaAuxiliar(level);
        collisionCustomButton.setText("Estructura auxiliar " + level);

        Platform.runLater(() -> {
            miViewList.getSelectionModel().clearSelection();
            miViewList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-background-color: white; -fx-text-fill: black;");
                    }
                }
            });
            miViewList.refresh();
        });
    }

    private void showChainedList(int position) {
        currentStructureLevel = -1;
        currentChainedPosition = position;
        highlightedChainIndex = -1;
        actualizarVistaEncadenamiento(position);
        collisionCustomButton.setText("Lista auxiliar posicion " + position);
    }

    private void setupTruncationUI() {
        if (!"Truncamiento".equals(hashString)) {
            truncText.setVisible(false);
            truncButton.setVisible(false);
            truncElegir.setVisible(false);
            return;
        }
        truncText.setVisible(true);
        truncButton.setVisible(true);
        truncElegir.setVisible(true);
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

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    private static class InsertionResult {
        boolean success;
        int finalPosition;
        int structureLevel;
        List<Integer> recorrido;
        String message;

        InsertionResult(boolean success, int finalPosition, int structureLevel, List<Integer> recorrido,
                String message) {
            this.success = success;
            this.finalPosition = finalPosition;
            this.structureLevel = structureLevel;
            this.recorrido = recorrido;
            this.message = message;
        }
    }

    private static class SearchResult {
        boolean found;
        int position;
        List<Integer> recorrido;
        String structureType;
        String foundKey;
        int structureLevel;
        List<Integer> chainIndices;

        SearchResult(boolean found, int position, List<Integer> recorrido, String structureType, String foundKey,
                int structureLevel) {
            this.found = found;
            this.position = position;
            this.recorrido = new ArrayList<>(recorrido);
            this.structureType = structureType;
            this.foundKey = foundKey;
            this.structureLevel = structureLevel;
            this.chainIndices = new ArrayList<>();
        }

        SearchResult(boolean found, int position, List<Integer> recorrido, String structureType, String foundKey,
                int structureLevel, List<Integer> chainIndices) {
            this.found = found;
            this.position = position;
            this.recorrido = new ArrayList<>(recorrido);
            this.structureType = structureType;
            this.foundKey = foundKey;
            this.structureLevel = structureLevel;
            this.chainIndices = new ArrayList<>(chainIndices);
        }
    }

    @FXML
    private void saveArray() {
        if (table == null) {
            itemsArrayText.setText("No hay estructura creada para guardar.");
            return;
        }
        if (undoStack.isEmpty()) {
            itemsArrayText.setText("No hay estado para guardar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Estado del Hash");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado Hash", "*.hsh"));
        File file = fileChooser.showSaveDialog(miViewList.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                Map<String, Object> saveData = new HashMap<>();
                saveData.put("hashFunction", this.hashString);
                saveData.put("state", undoStack.peek());
                oos.writeObject(saveData);
                itemsArrayText.setText("Estado guardado en: " + file.getName());
            } catch (IOException e) {
                itemsArrayText.setText("Error al guardar el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        }
        reiniciar();
    }

    @FXML
    private void loadArray() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Estado del Hash");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado Hash", "*.hsh"));
        File file = fileChooser.showOpenDialog(miViewList.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object loadedData = ois.readObject();

                if (loadedData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> saveData = (Map<String, Object>) loadedData;
                    String savedHashFunction = (String) saveData.get("hashFunction");
                    ActionState loadedState = (ActionState) saveData.get("state");

                    if (!savedHashFunction.equals(this.hashString)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error de Carga");
                        alert.setHeaderText("Funcion hash incompatible");
                        alert.setContentText("El archivo fue guardado con la funcion hash: " + savedHashFunction +
                                "\nPero actualmente esta seleccionada: " + this.hashString +
                                "\n\nSeleccione la funcion hash correcta antes de cargar el archivo.");
                        alert.showAndWait();
                        itemsArrayText.setText("Error: Funcion hash incompatible");
                        return;
                    }

                    collisionString = null;
                    collisionHash.setText("Elegir");
                    pendingKey = null;

                    undoStack.clear();
                    redoStack.clear();
                    undoStack.push(loadedState);

                    applyState(loadedState, false);
                    updateUndoRedoButtons();

                    insertedKeys.clear();
                    if (table != null) {
                        for (String key : table) {
                            if (key != null)
                                insertedKeys.add(key);
                        }
                    }

                    if (table != null) {
                        createButton.setDisable(true);
                        rangeHash.setDisable(true);
                        numberDigits.setDisable(true);
                        reiniciarButton.setDisable(false);
                        insertButton.setDisable(false);
                        newItemArray.setDisable(false);
                        modDeleteItem.setDisable(false);
                        searchButton.setDisable(false);
                        deleteButton.setDisable(false);
                        saveButton.setDisable(false);

                        if ("Truncamiento".equals(hashString)) {
                            truncButton.setDisable(true);
                            truncElegir.setDisable(true);
                        }

                        undoButton.setDisable(true);
                        redoButton.setDisable(true);
                    }

                    itemsArrayText.setText("Estado cargado desde: " + file.getName());
                }
            } catch (IOException | ClassNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Carga");
                alert.setHeaderText("No se pudo cargar el archivo");
                alert.setContentText("El archivo seleccionado no es valido o esta corrupto: " + e.getMessage());
                alert.showAndWait();
                itemsArrayText.setText("Archivo no valido o corrupto");
                e.printStackTrace();
            }
        }
    }
}