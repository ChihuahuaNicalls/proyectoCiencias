package ciencias.Research;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

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
import java.util.List;
import java.util.Map;

public class BinariaControllerExternal implements Initializable {

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
    private Button saveButton;
    @FXML
    private Button vistaAmpliada;

    @FXML
    private Label titleBin;
    @FXML
    private Label functionBin;
    @FXML
    private Text arrayLengthText;
    @FXML
    private Text itemsArrayText;

    @FXML
    private TextField newItemArray;
    @FXML
    private TextField modDeleteItem;
    @FXML
    private TextField rangeBin;
    @FXML
    private Spinner<Integer> numberDigits;

    @FXML
    private MenuButton bloquesButton;
    @FXML
    private ListView<String> miViewList;

    private List<List<String>> blocks;
    private int tableSize;
    private int blockSize;
    private int numBlocks;
    private int maxDigits;
    private int currentBlockView = 0;

    private String[] cellColors;
    private Timeline currentAnimation;
    private int currentAnimatedBlock = -1;
    private String currentAnimationColor = "WHITE";
    private Stage expandedViewStage;

    private final Deque<ActionState> undoStack = new ArrayDeque<>();
    private final Deque<ActionState> redoStack = new ArrayDeque<>();

    private static class ActionState implements Serializable {
        private final List<List<String>> blocksSnapshot;
        private final int tableSizeSnapshot;
        private final int blockSizeSnapshot;
        private final int numBlocksSnapshot;
        private final int maxDigitsSnapshot;
        private final int lastModifiedBlock;
        private final int lastModifiedPosition;

        ActionState(List<List<String>> blocks, int tableSize, int blockSize, int numBlocks, int maxDigits,
                int lastModifiedBlock, int lastModifiedPosition) {

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
            this.lastModifiedBlock = lastModifiedBlock;
            this.lastModifiedPosition = lastModifiedPosition;
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

        public int getLastModifiedBlock() {
            return lastModifiedBlock;
        }

        public int getLastModifiedPosition() {
            return lastModifiedPosition;
        }
    }

    public BinariaControllerExternal() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        newItemArray.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        modDeleteItem.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 3);
        numberDigits.setValueFactory(valueFactory);

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

        undoButton.setDisable(true);
        redoButton.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        saveButton.setDisable(true);
        insertButton.setDisable(true);
        newItemArray.setDisable(true);
        modDeleteItem.setDisable(true);
        bloquesButton.setVisible(false);
        bloquesButton.setDisable(true);

        titleBin.setText("Busqueda Binaria Externa");
        functionBin.setText("O(n)=log(n)");
    }

    @FXML
    private void crearArray() {
        String rangoSeleccionado = rangeBin.getText();
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

        maxDigits = numberDigits.getValue();

        blockSize = (int) Math.floor(Math.sqrt(tableSize));
        numBlocks = (int) Math.ceil((double) tableSize / blockSize);

        blocks = new ArrayList<>();
        for (int i = 0; i < numBlocks; i++) {
            blocks.add(new ArrayList<>());
        }

        cellColors = new String[numBlocks];
        Arrays.fill(cellColors, "WHITE");
        currentBlockView = 0;

        saveState(-1, -1);

        createButton.setDisable(true);
        rangeBin.setDisable(true);
        numberDigits.setDisable(true);
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

        arrayLengthText.setText("Array de " + tableSize + " elementos creado. " + numBlocks +
                " bloques de tamaño " + blockSize + ". Claves de " + maxDigits + " digitos.");
        actualizarVistaArray();
        populateBlocksMenu();
    }

    @FXML
    private void reiniciar() {
        undoStack.clear();
        redoStack.clear();

        blocks = null;
        arrayLengthText.setText("Array sin crear");
        itemsArrayText.setText("No hay elementos en el array");
        miViewList.getItems().clear();

        rangeBin.setDisable(false);
        numberDigits.setDisable(false);
        createButton.setDisable(false);

        reiniciarButton.setDisable(true);
        insertButton.setDisable(true);
        newItemArray.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        modDeleteItem.setDisable(true);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);
        bloquesButton.setVisible(false);
        bloquesButton.setDisable(true);

        updateUndoRedoButtons();
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

        SearchResult existing = busquedaBinariaGlobal(input);
        if (existing.found) {
            itemsArrayText.setText("Error: La clave " + input + " ya existe en el bloque " + existing.blockIndex);
            return;
        }

        InsertionResult result = insertarEnBloqueOrdenado(input);
        if (result.success) {
            itemsArrayText.setText(result.message);
            animateInsertion(result.recorrido, result.finalBlock, result.finalPosition);
        } else {
            itemsArrayText.setText("Error: No se pudo insertar la clave. Todos los bloques estan llenos.");
        }

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
        fileChooser.setTitle("Guardar Estado de Busqueda Binaria Externa");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado Binaria", "*.bin"));
        File file = fileChooser.showSaveDialog(miViewList.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                Map<String, Object> saveData = new HashMap<>();
                saveData.put("type", "BinariaExterna");
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
        fileChooser.setTitle("Cargar Estado de Busqueda Binaria Externa");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado Binaria", "*.bin"));
        File file = fileChooser.showOpenDialog(miViewList.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object loadedData = ois.readObject();

                if (loadedData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> saveData = (Map<String, Object>) loadedData;
                    String savedType = (String) saveData.get("type");
                    ActionState loadedState = (ActionState) saveData.get("state");

                    if (!"BinariaExterna".equals(savedType)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error de Carga");
                        alert.setHeaderText("Tipo de estructura incompatible");
                        alert.setContentText("El archivo fue guardado para: " + savedType +
                                "\nPero actualmente esta seleccionada: Busqueda Binaria Externa");
                        alert.showAndWait();

                        itemsArrayText.setText("Error: Tipo de estructura incompatible");
                        return;
                    }

                    undoStack.clear();
                    redoStack.clear();
                    undoStack.push(loadedState);

                    applyState(loadedState, false);
                    updateUndoRedoButtons();

                    if (blocks != null) {
                        createButton.setDisable(true);
                        rangeBin.setDisable(true);
                        numberDigits.setDisable(true);
                        reiniciarButton.setDisable(false);
                        insertButton.setDisable(false);
                        newItemArray.setDisable(false);
                        modDeleteItem.setDisable(false);
                        searchButton.setDisable(false);
                        deleteButton.setDisable(false);
                        saveButton.setDisable(false);

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

    @FXML
    private void vistaAmpliada() {
        if (blocks == null || numBlocks == 0) {
            itemsArrayText.setText("No hay bloques creados para mostrar.");
            return;
        }

        expandedViewStage = new Stage();
        expandedViewStage.setTitle("Vista Ampliada - Busqueda Binaria Externa");

        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Vista Ampliada - Busqueda Binaria Externa");
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

    private InsertionResult insertarEnBloqueOrdenado(String clave) {
        List<Integer> recorrido = new ArrayList<>();

        for (int blockIndex = 0; blockIndex < numBlocks; blockIndex++) {
            recorrido.add(blockIndex);

            List<String> bloque = blocks.get(blockIndex);
            int capacidad = getBlockCapacity(blockIndex);

            if (bloque.size() < capacidad &&
                    (bloque.isEmpty() || clave.compareTo(bloque.get(bloque.size() - 1)) > 0)) {

                int posicion = encontrarPosicionInsercion(bloque, clave);
                bloque.add(posicion, clave);

                saveState(blockIndex, posicion);
                return new InsertionResult(true, blockIndex, posicion, recorrido,
                        "Clave " + clave + " insertada en bloque " + blockIndex + ", posicion " + posicion);
            }
        }

        for (int blockIndex = 0; blockIndex < numBlocks; blockIndex++) {
            if (blocks.get(blockIndex).size() < getBlockCapacity(blockIndex)) {
                List<String> bloque = blocks.get(blockIndex);
                int posicion = encontrarPosicionInsercion(bloque, clave);
                bloque.add(posicion, clave);

                saveState(blockIndex, posicion);
                return new InsertionResult(true, blockIndex, posicion, recorrido,
                        "Clave " + clave + " insertada en bloque " + blockIndex + ", posicion " + posicion);
            }
        }

        return new InsertionResult(false, -1, -1, recorrido, "");
    }

    private int encontrarPosicionInsercion(List<String> bloque, String clave) {
        int low = 0;
        int high = bloque.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int comparacion = bloque.get(mid).compareTo(clave);

            if (comparacion < 0) {
                low = mid + 1;
            } else if (comparacion > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return low;
    }

    private SearchResult busquedaBinariaGlobal(String clave) {
        List<Integer> recorrido = new ArrayList<>();

        int lowBlock = 0;
        int highBlock = numBlocks - 1;

        while (lowBlock <= highBlock) {
            int midBlock = (lowBlock + highBlock) / 2;
            recorrido.add(midBlock);

            List<String> bloque = blocks.get(midBlock);
            if (bloque.isEmpty()) {

                if (midBlock > 0 && !blocks.get(midBlock - 1).isEmpty() &&
                        clave.compareTo(blocks.get(midBlock - 1).get(blocks.get(midBlock - 1).size() - 1)) <= 0) {
                    highBlock = midBlock - 1;
                } else {
                    lowBlock = midBlock + 1;
                }
                continue;
            }

            String primerElemento = bloque.get(0);
            String ultimoElemento = bloque.get(bloque.size() - 1);

            if (clave.compareTo(primerElemento) < 0) {
                highBlock = midBlock - 1;
            } else if (clave.compareTo(ultimoElemento) > 0) {
                lowBlock = midBlock + 1;
            } else {

                int posicion = busquedaBinariaEnBloque(bloque, clave);
                if (posicion != -1) {
                    return new SearchResult(true, midBlock, posicion, recorrido, clave);
                }
                break;
            }
        }

        for (int blockIndex = 0; blockIndex < numBlocks; blockIndex++) {
            if (!recorrido.contains(blockIndex)) {
                recorrido.add(blockIndex);
            }
            int posicion = busquedaBinariaEnBloque(blocks.get(blockIndex), clave);
            if (posicion != -1) {
                return new SearchResult(true, blockIndex, posicion, recorrido, clave);
            }
        }

        return new SearchResult(false, -1, -1, recorrido, clave);
    }

    private int busquedaBinariaEnBloque(List<String> bloque, String clave) {
        int low = 0;
        int high = bloque.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int comparacion = bloque.get(mid).compareTo(clave);

            if (comparacion < 0) {
                low = mid + 1;
            } else if (comparacion > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return -1;
    }

    private void findItem(String claveStr, boolean eliminar) {
        resetearAnimacion();

        if (blocks == null) {
            itemsArrayText.setText("No hay bloques creados.");
            return;
        }

        long inicio = System.nanoTime();

        SearchResult result = busquedaBinariaGlobal(claveStr);

        long fin = System.nanoTime();
        long nanos = fin - inicio;
        String tiempo = nanos < 1_000_000 ? nanos + " ns" : String.format("%.4f ms", nanos / 1_000_000.0);

        if (result.found) {
            if (eliminar) {

                blocks.get(result.blockIndex).remove(result.position);
                saveState(result.blockIndex, result.position);
                itemsArrayText.setText("Clave " + claveStr + " eliminada del bloque " + result.blockIndex +
                        " en " + tiempo + ".");
            } else {
                itemsArrayText.setText("Clave " + claveStr + " encontrada en bloque " + result.blockIndex +
                        ", posicion " + result.position + " en " + tiempo + ".");
            }
            animateSearch(result.recorrido, true, result.blockIndex, result.position, eliminar);
        } else {
            itemsArrayText.setText("Clave " + claveStr + " no encontrada tras " + result.recorrido.size() +
                    " busquedas en " + tiempo + ".");
            animateSearch(result.recorrido, false, -1, -1, eliminar);
        }
    }

    private void animateInsertion(List<Integer> recorrido, int finalBlock, int finalPosition) {
        System.out.println("Iniciando animacion de insercion - Recorrido: " + recorrido);
        System.out.println("Bloque final: " + finalBlock + ", Posicion: " + finalPosition);

        resetearAnimacion();

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        currentAnimation = new Timeline();
        Duration delay = Duration.ZERO;
        Duration stepDuration = Duration.seconds(1.0);

        for (int i = 0; i < recorrido.size(); i++) {
            final int currentBlock = recorrido.get(i);

            KeyFrame keyFrame = new KeyFrame(delay, e -> {
                System.out.println("Animando insercion paso: bloque " + currentBlock);
                showBlockDetails(currentBlock);
                animateBlock(currentBlock, "GRAY", "Buscando posicion en bloque " + currentBlock);
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

    private void animateSearch(List<Integer> recorrido, boolean found, int foundBlock, int foundPosition,
            boolean eliminar) {
        System.out.println("Iniciando animacion de busqueda - Recorrido: " + recorrido);
        System.out.println("Encontrado: " + found + ", Bloque: " + foundBlock + ", Posicion: " + foundPosition);

        resetearAnimacion();

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        currentAnimation = new Timeline();
        Duration delay = Duration.ZERO;
        Duration stepDuration = Duration.seconds(1.0);

        for (int i = 0; i < recorrido.size(); i++) {
            final int currentBlock = recorrido.get(i);

            KeyFrame keyFrame = new KeyFrame(delay, e -> {
                System.out.println("Animando busqueda paso: bloque " + currentBlock);
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
                int lastBlock = recorrido.get(recorrido.size() - 1);
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

    private int getBlockCapacity(int blockIndex) {
        if (blockIndex == numBlocks - 1) {
            return tableSize - (numBlocks - 1) * blockSize;
        }
        return blockSize;
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

    private void saveState(int lastModifiedBlock, int lastModifiedPosition) {
        redoStack.clear();
        undoStack.push(new ActionState(
                blocks, tableSize, blockSize, numBlocks, maxDigits,
                lastModifiedBlock, lastModifiedPosition));
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

        if (cellColors == null || cellColors.length != numBlocks) {
            cellColors = new String[numBlocks];
        }
        Arrays.fill(cellColors, "WHITE");

        int lastBlock = state.getLastModifiedBlock();
        if (markLastModified && lastBlock != -1) {
            showBlockDetails(lastBlock);
        }

        actualizarVistaArray();
        populateBlocksMenu();
        saveButton.setDisable(blocks == null);
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
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

    private static class InsertionResult {
        boolean success;
        int finalBlock;
        int finalPosition;
        List<Integer> recorrido;
        String message;

        InsertionResult(boolean success, int finalBlock, int finalPosition, List<Integer> recorrido,
                String message) {
            this.success = success;
            this.finalBlock = finalBlock;
            this.finalPosition = finalPosition;
            this.recorrido = recorrido;
            this.message = message;
        }
    }

    private static class SearchResult {
        boolean found;
        int blockIndex;
        int position;
        List<Integer> recorrido;
        String foundKey;

        SearchResult(boolean found, int blockIndex, int position, List<Integer> recorrido, String foundKey) {
            this.found = found;
            this.blockIndex = blockIndex;
            this.position = position;
            this.recorrido = new ArrayList<>(recorrido);
            this.foundKey = foundKey;
        }
    }
}