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
import javafx.animation.KeyValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class HashControllerExternal {

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
    private TextField rangeHash;
    @FXML
    private MenuButton collisionCustomButton;
    @FXML
    private Button saveButton;
    @FXML
    private MenuButton bloquesButton;

    private int[] truncPositions;
    private int truncMaxSelections = 0;
    private boolean truncPositionsSet = false;

    private Timeline currentAnimation;
    private int currentAnimatedBlock = -1;
    private String currentAnimationColor = "WHITE";
    private Stage expandedViewStage;

    private ResearchController researchController;
    private final List<String> insertedKeys = new ArrayList<>();

    private List<List<String>> blocks;
    private int tableSize;
    private int blockSize;
    private int numBlocks;
    private String hashString;
    private String collisionString;
    private int maxDigits;
    private String pendingKey = null;

    private String[] cellColors;
    private int currentBlockView = 0;

    private final Deque<ActionState> undoStack = new ArrayDeque<>();
    private final Deque<ActionState> redoStack = new ArrayDeque<>();

    private List<String[]> auxiliaryStructures;
    private Map<Integer, List<String>> chainedStructures;
    private int currentStructureLevel = 0;
    private int currentChainedPosition = -1;

    private static class ActionState implements Serializable {
        private final List<List<String>> blocksSnapshot;
        private final int tableSizeSnapshot;
        private final int blockSizeSnapshot;
        private final int numBlocksSnapshot;
        private final int maxDigitsSnapshot;
        private final String hashStringSnapshot;
        private final String collisionMethodSnapshot;
        private final int[] truncPositionsSnapshot;
        private final boolean truncPositionsSetSnapshot;
        private final int lastModifiedBlock;
        private final List<String> insertedKeysSnapshot;
        private final List<String[]> auxiliaryStructuresSnapshot;
        private final Map<Integer, List<String>> chainedStructuresSnapshot;

        ActionState(List<List<String>> blocks, int tableSize, int blockSize, int numBlocks, int maxDigits,
                String hashString,
                String collisionMethod, int[] truncPositions, boolean truncPositionsSet,
                int lastModifiedBlock, List<String> insertedKeys,
                List<String[]> auxiliaryStructures, Map<Integer, List<String>> chainedStructures) {

            this.blocksSnapshot = new ArrayList<>();
            if (blocks != null) {
                for (List<String> block : blocks) {
                    this.blocksSnapshot.add(new ArrayList<>(block));
                }
            }

            this.tableSizeSnapshot = tableSize;
            this.blockSizeSnapshot = blockSize;
            this.numBlocksSnapshot = numBlocks;
            this.maxDigitsSnapshot = maxDigits;
            this.hashStringSnapshot = hashString;
            this.collisionMethodSnapshot = collisionMethod;
            this.truncPositionsSnapshot = truncPositions != null ? truncPositions.clone() : null;
            this.truncPositionsSetSnapshot = truncPositionsSet;
            this.lastModifiedBlock = lastModifiedBlock;
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

        public List<List<String>> getBlocksSnapshot() {
            return blocksSnapshot;
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

        public int getLastModifiedBlock() {
            return lastModifiedBlock;
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

    private int aplicarFuncionHash(int clave) {
        switch (hashString) {
            case "Modulo":
                return (clave % numBlocks);
            case "Cuadrada":
                long sq = (long) clave * clave;
                String s = String.valueOf(sq);
                int digitos = (int) Math.log10(numBlocks) + 1;
                int digitosCentrales = digitos - 1;
                if (numBlocks >= 1000)
                    digitosCentrales = digitos - 1;
                int start = (s.length() - digitosCentrales) / 2;
                if (start < 0)
                    start = 0;
                String sub = s.substring(start, Math.min(start + digitosCentrales, s.length()));
                if (sub.isEmpty())
                    return 0;
                int resultadoCuadrada = Integer.parseInt(sub) % numBlocks;
                return resultadoCuadrada;
            case "Plegamiento":
                String claveStr = String.valueOf(clave);
                int sum = 0;
                for (int i = 0; i < claveStr.length(); i += 2) {
                    int end = Math.min(i + 2, claveStr.length());
                    String segmento = claveStr.substring(i, end);
                    sum += Integer.parseInt(segmento);
                }
                int digitosPlegamiento = (int) Math.log10(numBlocks) + 1;
                int divisor = (int) Math.pow(10, digitosPlegamiento - 1);
                int resultadoPlegamiento = sum % divisor;
                return (resultadoPlegamiento % numBlocks);
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
                    return 0;
                int resultadoTruncamiento = Integer.parseInt(truncatedNum.toString());
                return (resultadoTruncamiento % numBlocks);
            default:
                return (clave % numBlocks);
        }
    }

    private int getBlockCapacity(int blockIndex) {
        if (blockIndex == numBlocks - 1) {
            return tableSize - (numBlocks - 1) * blockSize;
        }
        return blockSize;
    }

    private void insertarEnBloqueOrdenado(List<String> bloque, String clave) {
        int i = 0;
        while (i < bloque.size() && clave.compareTo(bloque.get(i)) > 0) {
            i++;
        }
        bloque.add(i, clave);
    }

    private int buscarEnBloque(List<String> bloque, String clave) {
        for (int i = 0; i < bloque.size(); i++) {
            if (bloque.get(i).equals(clave)) {
                return i;
            }
        }
        return -1;
    }

    private boolean deberiaBuscarEnBloque(List<String> bloque, String clave) {
        if (bloque.isEmpty())
            return false;
        String ultimoElemento = bloque.get(bloque.size() - 1);
        return clave.compareTo(ultimoElemento) <= 0;
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
                    setStyle("-fx-background-color: white; -fx-text-fill: black;");
                }
            }
        });

        for (MenuItem item : collisionHash.getItems()) {
            item.setOnAction(event -> collisionHash.setText(item.getText()));
        }

        rangeHash.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

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
        bloquesButton.setVisible(false);
        bloquesButton.setDisable(true);
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
        if (rangoSeleccionado.isEmpty()) {
            arrayLengthText.setText("Debe ingresar un rango antes de crear el array.");
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

        blockSize = (int) Math.floor(Math.sqrt(tableSize));
        numBlocks = (int) Math.ceil((double) tableSize / blockSize);

        blocks = new ArrayList<>();
        for (int i = 0; i < numBlocks; i++) {
            blocks.add(new ArrayList<>());
        }

        cellColors = new String[numBlocks];
        Arrays.fill(cellColors, "WHITE");

        auxiliaryStructures.clear();
        chainedStructures.clear();
        currentStructureLevel = 0;
        currentChainedPosition = -1;
        currentBlockView = 0;

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
        bloquesButton.setVisible(true);
        bloquesButton.setDisable(false);

        arrayLengthText.setText("Array de " + tableSize + " elementos creado. " + numBlocks + " bloques de tamaño "
                + blockSize + ". Claves de " + maxDigits + " digitos.");
        if ("Truncamiento".equals(hashString)) {
            arrayLengthText
                    .setText(arrayLengthText.getText() + "\nPosiciones truncadas: " + Arrays.toString(truncPositions));
        }
        actualizarVistaArray();
        populateBlocksMenu();
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
            int blockIndex = aplicarFuncionHash(claveInt);
            boolean hayColision = (blocks.get(blockIndex).size() >= getBlockCapacity(blockIndex));
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
        int blockIndex = aplicarFuncionHash(claveInt);
        List<Integer> recorrido = new ArrayList<>();
        recorrido.add(blockIndex);

        if (buscarEnBloque(blocks.get(blockIndex), claveStr) != -1) {
            itemsArrayText.setText("Error: La clave " + claveStr + " ya existe en el bloque " + blockIndex + ".");
            return new InsertionResult(false, -1, 0, recorrido, "");
        }

        if (blocks.get(blockIndex).size() < getBlockCapacity(blockIndex)) {
            insertarEnBloqueOrdenado(blocks.get(blockIndex), claveStr);
            insertedKeys.add(claveStr);
            saveState(blockIndex);
            return new InsertionResult(true, blockIndex, 0, recorrido,
                    "Clave " + claveStr + " insertada en bloque " + blockIndex + ".");
        } else {
            if (hayColision) {
                List<String> chain = chainedStructures.getOrDefault(blockIndex, new ArrayList<>());
                if (chain.contains(claveStr)) {
                    itemsArrayText.setText("Error: La clave " + claveStr + " ya existe en la lista de encadenamiento.");
                    return new InsertionResult(false, -1, 0, recorrido, "");
                }

                for (int i = 0; i < chain.size(); i++) {
                    recorrido.add(blockIndex * 100 + i);
                }

                recorrido.add(blockIndex * 100 + chain.size());

                chain.add(claveStr);
                chainedStructures.put(blockIndex, chain);

                insertedKeys.add(claveStr);
                saveState(blockIndex);

                Platform.runLater(() -> {
                    updateCollisionCustomButton();
                    populateCollisionMenu();
                });

                return new InsertionResult(true, blockIndex, -1, recorrido,
                        "Clave " + claveStr + " insertada en lista de encadenamiento del bloque " + blockIndex + ".");
            } else {
                itemsArrayText.setText("Error inesperado en insercion con encadenamiento.");
                return new InsertionResult(false, -1, 0, recorrido, "");
            }
        }
    }

    private InsertionResult doInsertNested(String claveStr, int claveInt, int structureLevel) {
        int blockIndex = aplicarFuncionHash(claveInt);
        List<Integer> recorrido = new ArrayList<>();

        recorrido.add(structureLevel * 1000 + blockIndex);

        if (buscarEnBloque(blocks.get(blockIndex), claveStr) != -1) {
            itemsArrayText.setText("Error: La clave " + claveStr + " ya existe en el bloque " + blockIndex + ".");
            return new InsertionResult(false, -1, structureLevel, recorrido, "");
        }

        if (blocks.get(blockIndex).size() < getBlockCapacity(blockIndex)) {
            insertarEnBloqueOrdenado(blocks.get(blockIndex), claveStr);
            insertedKeys.add(claveStr);
            saveState(blockIndex);
            return new InsertionResult(true, blockIndex, structureLevel, recorrido,
                    "Clave " + claveStr + " insertada en bloque " + blockIndex + ".");
        } else {

            int nextBlockIndex = (blockIndex + 1) % numBlocks;
            int attempts = 0;
            while (blocks.get(nextBlockIndex).size() >= getBlockCapacity(nextBlockIndex) && attempts < numBlocks) {
                nextBlockIndex = (nextBlockIndex + 1) % numBlocks;
                attempts++;
                recorrido.add(structureLevel * 1000 + nextBlockIndex);
            }

            if (attempts >= numBlocks) {
                itemsArrayText.setText("Error: No hay espacio disponible en ningun bloque.");
                return new InsertionResult(false, -1, structureLevel, recorrido, "");
            }

            insertarEnBloqueOrdenado(blocks.get(nextBlockIndex), claveStr);
            insertedKeys.add(claveStr);
            saveState(nextBlockIndex);
            return new InsertionResult(true, nextBlockIndex, structureLevel, recorrido,
                    "Clave " + claveStr + " insertada en bloque " + nextBlockIndex + " por anidamiento.");
        }
    }

    private InsertionResult doInsertStandard(String claveStr, int claveInt) {
        int blockIndex = aplicarFuncionHash(claveInt);
        int step = 1;
        int intentos = 0;
        Set<Integer> bloquesVisitados = new HashSet<>();
        boolean usarAuxiliar = false;
        List<Integer> recorrido = new ArrayList<>();
        recorrido.add(blockIndex);

        if (buscarEnBloque(blocks.get(blockIndex), claveStr) != -1) {
            itemsArrayText.setText("Error: La clave " + claveStr + " ya existe en el bloque " + blockIndex + ".");
            return new InsertionResult(false, -1, 0, recorrido, "");
        }

        while (blocks.get(blockIndex).size() >= getBlockCapacity(blockIndex)) {
            if (collisionString == null) {
                itemsArrayText.setText(
                        "¡Colision detectada en el bloque " + blockIndex
                                + "!\nElija y defina un metodo de resolucion.");
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

            bloquesVisitados.add(blockIndex);
            int nextBlock;
            if (usarAuxiliar) {
                nextBlock = siguienteBloqueAuxiliar(blockIndex, step, claveInt);
            } else {
                nextBlock = siguienteBloque(blockIndex, step, claveInt);
                if (bloquesVisitados.contains(nextBlock)) {
                    usarAuxiliar = true;
                    nextBlock = siguienteBloqueAuxiliar(blockIndex, step, claveInt);
                }
            }

            blockIndex = nextBlock;
            recorrido.add(blockIndex);
            step++;
            intentos++;

            if (intentos >= numBlocks * 2) {
                int ocupadas = 0;
                for (int i = 0; i < numBlocks; i++) {
                    if (blocks.get(i).size() >= getBlockCapacity(i))
                        ocupadas++;
                }
                if (ocupadas >= numBlocks) {
                    itemsArrayText.setText("Todos los bloques estan llenos. No se pudo insertar " + claveStr + ".");
                    return new InsertionResult(false, -1, 0, recorrido, "");
                } else {
                    usarAuxiliar = true;
                    intentos = numBlocks;
                }
            }
        }

        insertarEnBloqueOrdenado(blocks.get(blockIndex), claveStr);
        insertedKeys.add(claveStr);
        saveState(blockIndex);
        return new InsertionResult(true, blockIndex, 0, recorrido,
                "Clave " + claveStr + " insertada en bloque " + blockIndex + ".");
    }

    private int siguienteBloque(int blockActual, int step, int claveInt) {
        if (collisionString == null)
            return blockActual;
        int next;
        switch (collisionString) {
            case "Lineal":
                next = (blockActual + 1) % numBlocks;
                break;
            case "Cuadratica":
                next = (blockActual + (step * step)) % numBlocks;
                break;
            case "Doble Hash":
                int nuevo = aplicarFuncionHash(blockActual);
                nuevo = (nuevo % numBlocks + numBlocks) % numBlocks;
                if (nuevo == blockActual)
                    nuevo = (blockActual + 1) % numBlocks;
                next = nuevo;
                break;
            default:
                next = blockActual;
        }
        return next;
    }

    private int siguienteBloqueAuxiliar(int blockActual, int step, int claveInt) {
        return (blockActual + 1) % numBlocks;
    }

    private void animateInsertion(List<Integer> recorrido, int finalBlock, int structureLevel) {
        System.out.println("Iniciando animacion de insercion - Recorrido: " + recorrido);
        System.out.println("Bloque final: " + finalBlock);

        resetearAnimacion();

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        currentAnimation = new Timeline();
        Duration delay = Duration.ZERO;
        Duration stepDuration = Duration.seconds(1.0);

        for (int i = 0; i < recorrido.size(); i++) {
            final int currentStep = i;
            final int currentBlock = getBlockFromCode(recorrido.get(i));

            KeyFrame keyFrame = new KeyFrame(delay, e -> {
                System.out.println("Animando insercion paso " + currentStep + ": bloque " + currentBlock);
                showBlockDetails(currentBlock);
                animateBlock(currentBlock, "GRAY", "Buscando espacio en bloque " + currentBlock);
            });

            currentAnimation.getKeyFrames().add(keyFrame);
            delay = delay.add(stepDuration);
        }

        KeyFrame finalFrame = new KeyFrame(delay, e -> {
            System.out.println("Insercion completada en bloque: " + finalBlock);
            showBlockDetails(finalBlock);
            animateBlock(finalBlock, "YELLOW", "Insercion completada en bloque " + finalBlock);

            Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                resetearAnimacion();
                actualizarVistaArray();
            }));
            resetTimeline.play();

            currentAnimation = null;
        });

        currentAnimation.getKeyFrames().add(finalFrame);
        currentAnimation.play();
    }

    private void findItem(String claveStr, boolean eliminar) {
        System.out.println("Buscando clave: " + claveStr + ", eliminar: " + eliminar);
        resetearAnimacion();

        if (blocks == null) {
            itemsArrayText.setText("No hay bloques creados.");
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
                saveState(result.blockIndex);
                itemsArrayText
                        .setText("Clave " + claveStr + " eliminada en " + result.structureType + " en " + tiempo + ".");
            } else {
                itemsArrayText.setText(
                        "Clave " + claveStr + " encontrada en " + result.structureType + " en " + tiempo + ".");
            }
            animateSearch(result.recorrido, true, result.blockIndex, eliminar, result.structureLevel,
                    result.chainIndices);
        } else {
            itemsArrayText.setText("Clave " + claveStr + " no encontrada tras " + result.recorrido.size()
                    + " intentos en " + tiempo + ".");
            animateSearch(result.recorrido, false, -1, eliminar, 0, new ArrayList<>());
        }

        if (eliminar) {
            limpiarEstructurasVacias();
        }
    }

    private SearchResult searchStandard(String claveStr, int claveInt, List<Integer> recorrido) {
        int blockIndex = aplicarFuncionHash(claveInt);
        int originalBlock = blockIndex;
        int step = 1;
        Set<Integer> bloquesVisitados = new HashSet<>();
        boolean usarAuxiliar = false;

        recorrido.add(blockIndex);

        if (deberiaBuscarEnBloque(blocks.get(blockIndex), claveStr)) {
            int posEnBloque = buscarEnBloque(blocks.get(blockIndex), claveStr);
            if (posEnBloque != -1) {
                return new SearchResult(true, blockIndex, recorrido, "bloque " + blockIndex, claveStr, 0);
            }
        }

        if (collisionString != null) {
            while (true) {
                bloquesVisitados.add(blockIndex);
                int nextBlock;
                if (usarAuxiliar) {
                    nextBlock = siguienteBloqueAuxiliar(blockIndex, step, claveInt);
                } else {
                    if ("Doble Hash".equals(collisionString)) {
                        nextBlock = siguienteBloque(blockIndex, step, claveInt);
                    } else {
                        nextBlock = siguienteBloque(originalBlock, step, claveInt);
                    }
                    if (bloquesVisitados.contains(nextBlock)) {
                        usarAuxiliar = true;
                        nextBlock = siguienteBloqueAuxiliar(blockIndex, step, claveInt);
                    }
                }

                blockIndex = nextBlock;
                recorrido.add(blockIndex);
                step++;

                if (deberiaBuscarEnBloque(blocks.get(blockIndex), claveStr)) {
                    int posEnBloque = buscarEnBloque(blocks.get(blockIndex), claveStr);
                    if (posEnBloque != -1) {
                        return new SearchResult(true, blockIndex, recorrido, "bloque " + blockIndex, claveStr, 0);
                    }
                }

                if (step > numBlocks * 2 || bloquesVisitados.size() >= numBlocks)
                    break;
            }
        }

        return new SearchResult(false, -1, recorrido, "", claveStr, 0);
    }

    private SearchResult searchInChainedStructures(String claveStr, int claveInt, List<Integer> recorrido) {
        int blockIndex = aplicarFuncionHash(claveInt);
        List<Integer> chainIndices = new ArrayList<>();

        recorrido.add(blockIndex);

        if (deberiaBuscarEnBloque(blocks.get(blockIndex), claveStr)) {
            int posEnBloque = buscarEnBloque(blocks.get(blockIndex), claveStr);
            if (posEnBloque != -1) {
                return new SearchResult(true, blockIndex, recorrido, "bloque " + blockIndex, claveStr, 0);
            }
        }

        List<String> chain = chainedStructures.get(blockIndex);
        if (chain != null) {
            for (int i = 0; i < chain.size(); i++) {
                chainIndices.add(i);
                recorrido.add(blockIndex * 100 + i);

                if (chain.get(i).equals(claveStr)) {
                    return new SearchResult(true, blockIndex, recorrido,
                            "Lista de encadenamiento bloque " + blockIndex, claveStr, -1, chainIndices);
                }
            }
        }

        return new SearchResult(false, -1, recorrido, "", claveStr, 0);
    }

    private SearchResult searchInNestedStructures(String claveStr, int claveInt, int structureLevel,
            List<Integer> recorrido) {
        int blockIndex = aplicarFuncionHash(claveInt);

        recorrido.add(structureLevel * 1000 + blockIndex);

        if (deberiaBuscarEnBloque(blocks.get(blockIndex), claveStr)) {
            int posEnBloque = buscarEnBloque(blocks.get(blockIndex), claveStr);
            if (posEnBloque != -1) {
                return new SearchResult(true, blockIndex, recorrido, getStructureName(structureLevel), claveStr,
                        structureLevel);
            }
        }

        int nextBlockIndex = (blockIndex + 1) % numBlocks;
        int attempts = 0;
        while (attempts < numBlocks) {
            recorrido.add(structureLevel * 1000 + nextBlockIndex);

            if (deberiaBuscarEnBloque(blocks.get(nextBlockIndex), claveStr)) {
                int posEnBloque = buscarEnBloque(blocks.get(nextBlockIndex), claveStr);
                if (posEnBloque != -1) {
                    return new SearchResult(true, nextBlockIndex, recorrido,
                            "bloque " + nextBlockIndex + " (anidamiento)", claveStr, structureLevel);
                }
            }

            nextBlockIndex = (nextBlockIndex + 1) % numBlocks;
            attempts++;
        }

        return new SearchResult(false, -1, recorrido, "", claveStr, structureLevel);
    }

    private void removeFromStructure(SearchResult result) {
        if (result.foundKey == null)
            return;

        if (result.structureType.startsWith("bloque")) {
            int blockIndex = result.blockIndex;
            blocks.get(blockIndex).remove(result.foundKey);
            insertedKeys.remove(result.foundKey);
        } else if (result.structureType.startsWith("Lista de encadenamiento")) {
            int blockIndex = result.blockIndex;
            List<String> chain = chainedStructures.get(blockIndex);
            if (chain != null) {
                chain.remove(result.foundKey);
                insertedKeys.remove(result.foundKey);
            }
        }

        limpiarEstructurasVacias();
    }

    private void animateSearch(List<Integer> recorrido, boolean found, int foundBlock, boolean eliminar,
            int structureLevel, List<Integer> chainIndices) {
        System.out.println("Iniciando animacion de busqueda - Recorrido: " + recorrido);
        System.out.println("Encontrado: " + found + ", Bloque: " + foundBlock);

        resetearAnimacion();

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        currentAnimation = new Timeline();
        Duration delay = Duration.ZERO;
        Duration stepDuration = Duration.seconds(1.0);

        for (int i = 0; i < recorrido.size(); i++) {
            final int currentStep = i;
            final int currentBlock = getBlockFromCode(recorrido.get(i));

            KeyFrame keyFrame = new KeyFrame(delay, e -> {
                System.out.println("Animando busqueda paso " + currentStep + ": bloque " + currentBlock);
                showBlockDetails(currentBlock);
                animateBlock(currentBlock, "GRAY", "Buscando en bloque " + currentBlock);
            });

            currentAnimation.getKeyFrames().add(keyFrame);
            delay = delay.add(stepDuration);
        }

        KeyFrame finalFrame = new KeyFrame(delay, e -> {
            String colorFinal = found ? (eliminar ? "RED" : "GREEN") : "RED";
            String mensaje = found ? "Elemento " + (eliminar ? "eliminado" : "encontrado") + " en bloque " + foundBlock
                    : "Elemento no encontrado";

            if (found && foundBlock != -1) {
                showBlockDetails(foundBlock);
                animateBlock(foundBlock, colorFinal, mensaje);
            } else if (!recorrido.isEmpty()) {
                int lastBlock = getBlockFromCode(recorrido.get(recorrido.size() - 1));
                showBlockDetails(lastBlock);
                animateBlock(lastBlock, colorFinal, mensaje);
            }

            Timeline resetTimeline = new Timeline(new KeyFrame(Duration.seconds(2), ev -> {
                resetearAnimacion();
                actualizarVistaArray();
            }));
            resetTimeline.play();

            currentAnimation = null;
        });

        currentAnimation.getKeyFrames().add(finalFrame);
        currentAnimation.play();
    }

    private int getBlockFromCode(int code) {
        if (code >= 1000) {
            return code % 1000;
        } else if (code >= 100) {
            return code / 100;
        } else {
            return code;
        }
    }

    private void actualizarVistaArray() {
        miViewList.getItems().clear();
        if (blocks == null || currentBlockView < 0 || currentBlockView >= numBlocks)
            return;

        List<String> bloque = blocks.get(currentBlockView);
        for (int j = 0; j < bloque.size(); j++) {
            miViewList.getItems().add("Pos " + j + ": " + bloque.get(j));
        }

        bloquesButton.setText("Bloque " + currentBlockView);
        miViewList.refresh();
    }

    private void populateBlocksMenu() {
        bloquesButton.getItems().clear();

        for (int i = 0; i < numBlocks; i++) {
            MenuItem blockItem = new MenuItem("Bloque " + i);
            final int blockIndex = i;
            blockItem.setOnAction(e -> showBlockDetails(blockIndex));
            bloquesButton.getItems().add(blockItem);
        }
    }

    private void showBlockDetails(int blockIndex) {
        if (blocks == null || blockIndex < 0 || blockIndex >= numBlocks)
            return;

        currentBlockView = blockIndex;
        List<String> bloque = blocks.get(blockIndex);
        StringBuilder details = new StringBuilder();
        details.append("Detalles del Bloque ").append(blockIndex).append(":\n");
        details.append("Capacidad: ").append(bloque.size()).append("/").append(getBlockCapacity(blockIndex))
                .append("\n");
        details.append("Elementos ordenados: ");

        if (bloque.isEmpty()) {
            details.append("Vacio");
        } else {
            for (int i = 0; i < bloque.size(); i++) {
                if (i > 0)
                    details.append(", ");
                details.append(bloque.get(i));
            }
        }

        itemsArrayText.setText(details.toString());
        actualizarVistaArray();
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

    private void saveState(int lastModifiedBlock) {
        redoStack.clear();
        undoStack.push(new ActionState(
                blocks, tableSize, blockSize, numBlocks, maxDigits, hashString,
                collisionString, truncPositions, truncPositionsSet,
                lastModifiedBlock, insertedKeys,
                auxiliaryStructures, chainedStructures));
        updateUndoRedoButtons();
    }

    private void applyState(ActionState state, boolean markLastModified) {

        this.blocks = new ArrayList<>();
        for (List<String> block : state.getBlocksSnapshot()) {
            this.blocks.add(new ArrayList<>(block));
        }

        this.tableSize = state.getTableSizeSnapshot();
        this.blockSize = state.getBlockSizeSnapshot();
        this.numBlocks = state.getNumBlocksSnapshot();
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

        if (cellColors == null || cellColors.length != numBlocks) {
            cellColors = new String[numBlocks];
        }
        Arrays.fill(cellColors, "WHITE");

        updateCollisionCustomButton();

        int lastBlock = state.getLastModifiedBlock();
        if (markLastModified && lastBlock != -1) {
            showBlockDetails(lastBlock);
        }

        handleUIState(state);
        actualizarVistaArray();
        populateBlocksMenu();
        saveButton.setDisable(blocks == null);
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

    @FXML
    private void reiniciar() {
        undoStack.clear();
        redoStack.clear();
        insertedKeys.clear();
        auxiliaryStructures.clear();
        chainedStructures.clear();
        currentStructureLevel = 0;
        currentChainedPosition = -1;
        currentBlockView = 0;

        blocks = null;
        arrayLengthText.setText("Array sin crear");
        itemsArrayText.setText("No hay elementos en el array");
        miViewList.getItems().clear();

        collisionString = null;
        collisionHash.setText("Elegir");
        pendingKey = null;

        collisionCustomButton.setVisible(false);
        collisionCustomButton.setDisable(true);
        bloquesButton.setVisible(false);
        bloquesButton.setDisable(true);

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

    private void resetearAnimacion() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }

        if (cellColors != null) {
            Arrays.fill(cellColors, "WHITE");
        }

        currentAnimatedBlock = -1;
        currentAnimationColor = "WHITE";

        actualizarVistaArray();
        if (expandedViewStage != null && expandedViewStage.isShowing()) {
            refreshExpandedView();
        }

        Platform.runLater(() -> {
            miViewList.getSelectionModel().clearSelection();
            miViewList.refresh();
        });
    }

    private void limpiarEstructurasVacias() {
        boolean changed = false;

        Iterator<Map.Entry<Integer, List<String>>> chainIterator = chainedStructures.entrySet().iterator();
        while (chainIterator.hasNext()) {
            Map.Entry<Integer, List<String>> entry = chainIterator.next();
            List<String> chain = entry.getValue();
            if (chain == null || chain.isEmpty()) {
                chainIterator.remove();
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
            for (Integer blockIndex : chainedStructures.keySet()) {
                List<String> chain = chainedStructures.get(blockIndex);
                if (chain != null && !chain.isEmpty()) {
                    MenuItem chainItem = new MenuItem("Lista auxiliar bloque " + blockIndex);
                    final int block = blockIndex;
                    chainItem.setOnAction(e -> showChainedList(block));
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
        currentBlockView = 0;
        actualizarVistaArray();
        collisionCustomButton.setText("Estructura principal");
    }

    private void showAuxiliaryStructure(int level) {
        currentStructureLevel = level;
        currentChainedPosition = -1;
        itemsArrayText.setText("Mostrando estructura auxiliar " + level);
    }

    private void showChainedList(int blockIndex) {
        currentStructureLevel = -1;
        currentChainedPosition = blockIndex;

        List<String> chain = chainedStructures.get(blockIndex);
        miViewList.getItems().clear();
        if (chain != null) {
            for (int i = 0; i < chain.size(); i++) {
                miViewList.getItems().add("Elemento " + i + ": " + chain.get(i));
            }
        }
        collisionCustomButton.setText("Lista auxiliar bloque " + blockIndex);
    }

    /**
     * Metodo para animar un bloque especifico
     */
    private void animateBlock(int blockIndex, String color, String message) {
        if (blockIndex < 0 || blockIndex >= numBlocks)
            return;

        currentAnimatedBlock = blockIndex;
        currentAnimationColor = color.toUpperCase();

        if (cellColors != null && blockIndex < cellColors.length) {
            cellColors[blockIndex] = color.toUpperCase();
        }

        itemsArrayText.setText(message);

        if (expandedViewStage != null && expandedViewStage.isShowing()) {
            refreshExpandedView();
        }

        actualizarVistaArray();
    }

    /**
     * Actualizar la vista ampliada si esta abierta
     */
    private void refreshExpandedView() {
        if (expandedViewStage != null && expandedViewStage.isShowing()) {

            Scene scene = expandedViewStage.getScene();
            if (scene != null) {
                ScrollPane scrollPane = (ScrollPane) scene.lookup(".scroll-pane");
                if (scrollPane != null && scrollPane.getContent() instanceof HBox) {
                    HBox blocksContainer = (HBox) scrollPane.getContent();
                    blocksContainer.getChildren().clear();

                    for (int i = 0; i < numBlocks; i++) {
                        VBox blockBox = createBlockVisualization(i);
                        blocksContainer.getChildren().add(blockBox);
                    }
                }
            }
        }
    }

    @FXML
    private void vistaAmpliada() {
        if (blocks == null || numBlocks == 0) {
            itemsArrayText.setText("No hay bloques creados para mostrar.");
            return;
        }

        expandedViewStage = new Stage();
        expandedViewStage.setTitle("Vista Ampliada - Hash Externo");

        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Vista Ampliada - Hash Externo");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(0, 0, 10, 0));

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        updateStatusLabel(statusLabel);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: #cccccc;");
        scrollPane.getStyleClass().add("scroll-pane");

        HBox blocksContainer = new HBox(20);
        blocksContainer.setPadding(new Insets(15));
        blocksContainer.setAlignment(Pos.TOP_LEFT);

        for (int i = 0; i < numBlocks; i++) {
            VBox blockBox = createBlockVisualization(i);
            blocksContainer.getChildren().add(blockBox);
        }

        scrollPane.setContent(blocksContainer);

        setupExpandedPan(blocksContainer, scrollPane);

        HBox controlButtons = createControlButtons(expandedViewStage, scrollPane, blocksContainer, statusLabel);

        mainContainer.getChildren().addAll(titleLabel, statusLabel, scrollPane, controlButtons);

        Scene scene = new Scene(mainContainer, 900, 600);
        expandedViewStage.setScene(scene);

        expandedViewStage.setOnHidden(e -> {
            expandedViewStage = null;
        });

        expandedViewStage.show();
    }

    private VBox createBlockVisualization(int blockIndex) {
        VBox blockContainer = new VBox(5);

        String baseStyle = "-fx-border-color: #333333; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-padding: 10px;";
        String animationStyle = "";

        if (currentAnimatedBlock == blockIndex) {
            switch (currentAnimationColor.toUpperCase()) {
                case "GRAY":
                    animationStyle = "-fx-background-color: #f1f2f6; -fx-border-color: #7f8c8d; -fx-border-width: 3px;";
                    break;
                case "GREEN":
                    animationStyle = "-fx-background-color: #d5f4e6; -fx-border-color: #27ae60; -fx-border-width: 3px;";
                    break;
                case "RED":
                    animationStyle = "-fx-background-color: #fadbd8; -fx-border-color: #e74c3c; -fx-border-width: 3px;";
                    break;
                case "YELLOW":
                    animationStyle = "-fx-background-color: #fef9e7; -fx-border-color: #f39c12; -fx-border-width: 3px;";
                    break;
                default:
                    animationStyle = "-fx-background-color: white;";
            }
        } else if (cellColors != null && blockIndex < cellColors.length && !cellColors[blockIndex].equals("WHITE")) {

            switch (cellColors[blockIndex].toUpperCase()) {
                case "GRAY":
                    animationStyle = "-fx-background-color: #f8f9fa;";
                    break;
                case "GREEN":
                    animationStyle = "-fx-background-color: #e8f6f3;";
                    break;
                case "RED":
                    animationStyle = "-fx-background-color: #fdedec;";
                    break;
                case "YELLOW":
                    animationStyle = "-fx-background-color: #fef9e7;";
                    break;
            }
        } else {
            animationStyle = "-fx-background-color: white;";
        }

        blockContainer.setStyle(baseStyle + animationStyle);
        blockContainer.setMinWidth(150);
        blockContainer.setMaxWidth(150);

        HBox titleBox = new HBox(5);
        titleBox.setAlignment(Pos.CENTER);

        Label blockTitle = new Label("Bloque " + blockIndex);
        blockTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Circle animationIndicator = new Circle(4);
        animationIndicator.setFill(Color.TRANSPARENT);

        if (currentAnimatedBlock == blockIndex) {

            Timeline blinkAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(animationIndicator.fillProperty(), Color.TRANSPARENT)),
                    new KeyFrame(Duration.seconds(0.5),
                            new KeyValue(animationIndicator.fillProperty(), getColorFromString(currentAnimationColor))),
                    new KeyFrame(Duration.seconds(1.0),
                            new KeyValue(animationIndicator.fillProperty(), Color.TRANSPARENT)));
            blinkAnimation.setCycleCount(Timeline.INDEFINITE);
            blinkAnimation.play();

            animationIndicator.setUserData(blinkAnimation);
        }

        titleBox.getChildren().addAll(blockTitle, animationIndicator);

        int currentSize = blocks.get(blockIndex).size();
        int capacity = getBlockCapacity(blockIndex);
        Label capacityLabel = new Label(currentSize + "/" + capacity);
        capacityLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-alignment: center;");
        capacityLabel.setMaxWidth(Double.MAX_VALUE);
        capacityLabel.setAlignment(Pos.CENTER);

        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));

        VBox elementsContainer = new VBox(3);
        elementsContainer.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 8px; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 3px;");

        List<String> blockElements = blocks.get(blockIndex);
        if (blockElements.isEmpty()) {
            Label emptyLabel = new Label("Vacio");
            emptyLabel.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-alignment: center;");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            emptyLabel.setAlignment(Pos.CENTER);
            elementsContainer.getChildren().add(emptyLabel);
        } else {
            for (String element : blockElements) {
                Label elementLabel = new Label(element);
                elementLabel.setStyle(
                        "-fx-font-size: 12px; -fx-text-fill: #2c3e50; -fx-padding: 2px 5px; -fx-background-color: #e8f4f8; -fx-background-radius: 3px; -fx-border-color: #3498db; -fx-border-width: 1px; -fx-border-radius: 3px;");
                elementLabel.setMaxWidth(Double.MAX_VALUE);
                elementLabel.setAlignment(Pos.CENTER);
                elementsContainer.getChildren().add(elementLabel);
            }
        }

        if (chainedStructures.containsKey(blockIndex)) {
            List<String> chain = chainedStructures.get(blockIndex);
            if (chain != null && !chain.isEmpty()) {

                Separator chainSeparator = new Separator();
                chainSeparator.setPadding(new Insets(5, 0, 5, 0));

                Label chainTitle = new Label("Encadenamiento:");
                chainTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

                VBox chainContainer = new VBox(2);
                chainContainer.setStyle(
                        "-fx-background-color: #fff5f5; -fx-padding: 5px; -fx-border-color: #e74c3c; -fx-border-width: 1px; -fx-border-radius: 3px;");

                for (String chainElement : chain) {
                    Label chainLabel = new Label(chainElement);
                    chainLabel.setStyle(
                            "-fx-font-size: 11px; -fx-text-fill: #c0392b; -fx-padding: 1px 3px; -fx-background-color: #ffebee; -fx-background-radius: 2px;");
                    chainLabel.setMaxWidth(Double.MAX_VALUE);
                    chainLabel.setAlignment(Pos.CENTER);
                    chainContainer.getChildren().add(chainLabel);
                }

                elementsContainer.getChildren().addAll(chainSeparator, chainTitle, chainContainer);
            }
        }

        blockContainer.getChildren().addAll(titleBox, capacityLabel, separator, elementsContainer);
        return blockContainer;
    }

    /**
     * Convertir color string a Color JavaFX
     */
    private Color getColorFromString(String colorStr) {
        switch (colorStr.toUpperCase()) {
            case "GRAY":
                return Color.GRAY;
            case "GREEN":
                return Color.GREEN;
            case "RED":
                return Color.RED;
            case "YELLOW":
                return Color.YELLOW;
            default:
                return Color.TRANSPARENT;
        }
    }

    private void setupExpandedPan(Pane canvas, ScrollPane scrollPane) {
        final double[] dragDelta = new double[2];

        canvas.setOnMousePressed(event -> {
            if (!(event.getTarget() instanceof Circle) && !(event.getTarget() instanceof javafx.scene.text.Text)) {
                dragDelta[0] = event.getSceneX();
                dragDelta[1] = event.getSceneY();
                canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
            }
        });

        canvas.setOnMouseReleased(event -> {
            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        canvas.setOnMouseDragged(event -> {
            if (canvas.getCursor() == javafx.scene.Cursor.CLOSED_HAND) {
                double deltaX = event.getSceneX() - dragDelta[0];
                double deltaY = event.getSceneY() - dragDelta[1];

                double newHValue = scrollPane.getHvalue()
                        - deltaX / (canvas.getWidth() * scrollPane.getContent().getScaleX());
                double newVValue = scrollPane.getVvalue()
                        - deltaY / (canvas.getHeight() * scrollPane.getContent().getScaleY());

                scrollPane.setHvalue(Math.max(0, Math.min(1, newHValue)));
                scrollPane.setVvalue(Math.max(0, Math.min(1, newVValue)));

                dragDelta[0] = event.getSceneX();
                dragDelta[1] = event.getSceneY();
            }
        });
    }

    private HBox createControlButtons(Stage stage, ScrollPane scrollPane, Pane canvas, Label statusLabel) {
        Button resetZoomButton = new Button("Centrar vista");
        resetZoomButton.setOnAction(e -> resetExpandedZoom(scrollPane, canvas));

        Button refreshButton = new Button("Actualizar vista");
        refreshButton.setOnAction(e -> {
            refreshExpandedView();
            updateStatusLabel(statusLabel);
        });

        HBox buttonBox = new HBox(10, resetZoomButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        return buttonBox;
    }

    private void updateStatusLabel(Label statusLabel) {
        String status = "Estado: ";
        if (currentAnimatedBlock != -1) {
            status += "Animando bloque " + currentAnimatedBlock + " (" + currentAnimationColor + ")";
        } else {
            status += "En reposo";
        }
        statusLabel.setText(status);
    }

    private void resetExpandedZoom(ScrollPane scrollPane, Pane canvas) {

        scrollPane.setHvalue(0);
        scrollPane.setVvalue(0);

        canvas.setScaleX(1.0);
        canvas.setScaleY(1.0);
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
        fileChooser.setTitle("Guardar Estado del Hash Externo");
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
    }

    @FXML
    private void loadArray() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Estado del Hash Externo");
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
                    if (blocks != null) {
                        for (List<String> block : blocks) {
                            insertedKeys.addAll(block);
                        }
                        for (List<String> chain : chainedStructures.values()) {
                            insertedKeys.addAll(chain);
                        }
                    }

                    if (blocks != null) {
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

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    private String getStructureName(int level) {
        return level == 0 ? "estructura principal" : "Estructura auxiliar " + level;
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
        int blockIndex;
        List<Integer> recorrido;
        String structureType;
        String foundKey;
        int structureLevel;
        List<Integer> chainIndices;

        SearchResult(boolean found, int blockIndex, List<Integer> recorrido, String structureType, String foundKey,
                int structureLevel) {
            this.found = found;
            this.blockIndex = blockIndex;
            this.recorrido = new ArrayList<>(recorrido);
            this.structureType = structureType;
            this.foundKey = foundKey;
            this.structureLevel = structureLevel;
            this.chainIndices = new ArrayList<>();
        }

        SearchResult(boolean found, int blockIndex, List<Integer> recorrido, String structureType, String foundKey,
                int structureLevel, List<Integer> chainIndices) {
            this.found = found;
            this.blockIndex = blockIndex;
            this.recorrido = new ArrayList<>(recorrido);
            this.structureType = structureType;
            this.foundKey = foundKey;
            this.structureLevel = structureLevel;
            this.chainIndices = new ArrayList<>(chainIndices);
        }
    }
}