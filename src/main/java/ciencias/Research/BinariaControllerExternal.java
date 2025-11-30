package ciencias.Research;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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

public class BinariaControllerExternal implements Initializable {

    @FXML
    private Button createButton, reiniciarButton, insertButton, searchButton, deleteButton, undoButton, redoButton,
            saveButton, vistaAmpliada;
    @FXML
    private Label titleBin, functionBin;
    @FXML
    private Text arrayLengthText, itemsArrayText;
    @FXML
    private TextField newItemArray, modDeleteItem, rangeBin;
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

    private Map<Integer, String[]> cellColorsByBlock;

    private final Deque<ActionState> undoStack = new ArrayDeque<>();
    private final Deque<ActionState> redoStack = new ArrayDeque<>();

    private static final double ANIM_STEP_MS = 450;

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
            if (blocks != null)
                for (List<String> b : blocks)
                    this.blocksSnapshot.add(new ArrayList<>(b));
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarValidadores();
        configurarSpinner();
        configurarListView();
        configurarBotones();
        configurarMenuBloques();
        titleBin.setText("Busqueda Binaria Externa");
        functionBin.setText("Busqueda binaria en las estructuras de bloques.");
    }

    private void configurarValidadores() {
        newItemArray.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        modDeleteItem.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
        rangeBin.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
    }

    private void configurarSpinner() {
        numberDigits.setValueFactory(new IntegerSpinnerValueFactory(1, 9, 3));
        numberDigits.valueProperty().addListener((o, oldV, newV) -> {
            if (blocks != null)
                itemsArrayText.setText("Numero de digitos cambiado a: " + newV + ". Reinicie para aplicar.");
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

                int index = getIndex();

                if (blocks == null) {
                    setStyle("-fx-background-color:white; -fx-text-fill:black;");
                    return;
                }

                int posZeroBased = index;
                String[] colors = cellColorsByBlock.getOrDefault(currentBlockView, null);
                String color = (colors != null && posZeroBased >= 0 && posZeroBased < colors.length)
                        ? colors[posZeroBased]
                        : "WHITE";
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
    }

    private void configurarMenuBloques() {
        bloquesButton.setVisible(false);
        bloquesButton.setDisable(true);
    }

    @FXML
    private void crearArray() {
        String rango = rangeBin.getText();
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
        for (int i = 0; i < numBlocks; i++) {

            blocks.add(new ArrayList<>());
        }

        cellColorsByBlock = new HashMap<>();
        for (int i = 0; i < numBlocks; i++)
            cellColorsByBlock.put(i, initColorArray(getBlockCapacity(i)));

        currentBlockView = 0;
        saveState(-1, -1);

        actualizarEstadoControlesCreacion();
        arrayLengthText.setText(
                "Array de " + tableSize + " elementos creado. " + numBlocks + " bloques de tamaño " + blockSize + ".");
        itemsArrayText.setText("Estructura creada. Insertar claves de " + maxDigits + " digitos.");
        actualizarVistaArray();
        populateBlocksMenu();
    }

    private String[] initColorArray(int capacity) {
        String[] arr = new String[capacity];
        Arrays.fill(arr, "WHITE");
        return arr;
    }

    private void actualizarEstadoControlesCreacion() {
        createButton.setDisable(true);
        rangeBin.setDisable(true);
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
    }

    @FXML
    private void reiniciar() {
        undoStack.clear();
        redoStack.clear();
        blocks = null;
        cellColorsByBlock = null;
        arrayLengthText.setText("Array sin crear");
        itemsArrayText.setText("No hay elementos en la estructura.");
        miViewList.getItems().clear();
        rangeBin.setDisable(false);
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
        bloquesButton.setDisable(true);
    }

    private int getBlockCapacity(int blockIndex) {
        if (blockIndex == numBlocks - 1)
            return tableSize - (numBlocks - 1) * blockSize;
        return blockSize;
    }

    private int calcularTotalElementos() {
        if (blocks == null)
            return 0;
        int s = 0;
        for (List<String> b : blocks)
            s += b.size();
        return s;
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
        if (blocks == null)
            return;
        if (blockIndex < 0 || blockIndex >= numBlocks)
            return;
        currentBlockView = blockIndex;
        List<String> b = blocks.get(blockIndex);
        StringBuilder sb = new StringBuilder();
        sb.append("Detalles del Bloque ").append(blockIndex + 1).append(":\n");
        sb.append("Ocupado: ").append(b.size()).append("/").append(getBlockCapacity(blockIndex)).append("\n");
        sb.append("Elementos: ");
        if (b.isEmpty())
            sb.append("-");
        else {
            for (int i = 0; i < b.size(); i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append("(").append(i + 1).append(") ").append(b.get(i));
            }
        }
        itemsArrayText.setText(sb.toString());
        actualizarVistaArray();
    }

    private void actualizarVistaArray() {
        miViewList.getItems().clear();
        if (blocks == null)
            return;
        List<String> bloque = blocks.get(currentBlockView);
        int capacity = getBlockCapacity(currentBlockView);

        for (int i = 0; i < capacity; i++) {
            String val = (i < bloque.size() ? bloque.get(i) : "-");
            miViewList.getItems().add("Pos " + (i + 1) + ": " + val);
        }
        miViewList.refresh();
        bloquesButton.setText("Bloque " + (currentBlockView + 1));
    }

    private void saveState(int lastModifiedBlock, int lastModifiedPosition) {
        redoStack.clear();
        undoStack.push(new ActionState(blocks, tableSize, blockSize, numBlocks, maxDigits, lastModifiedBlock,
                lastModifiedPosition));
        updateUndoRedoButtons();
    }

    private void applyState(ActionState state, boolean markLast) {
        this.blocks = new ArrayList<>();
        for (List<String> b : state.getBlocksSnapshot())
            this.blocks.add(new ArrayList<>(b));
        this.tableSize = state.getTableSizeSnapshot();
        this.blockSize = state.getBlockSizeSnapshot();
        this.numBlocks = state.getNumBlocksSnapshot();
        this.maxDigits = state.getMaxDigitsSnapshot();

        cellColorsByBlock = new HashMap<>();
        for (int i = 0; i < numBlocks; i++)
            cellColorsByBlock.put(i, initColorArray(getBlockCapacity(i)));
        if (markLast && state.getLastModifiedBlock() != -1)
            showBlockDetails(state.getLastModifiedBlock());
        else
            actualizarVistaArray();
        populateBlocksMenu();
        saveButton.setDisable(blocks == null);
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
    }

    @FXML
    public void vistaAmpliada(ActionEvent event) {
        try {
            Stage expandedStage = new Stage();
            expandedStage.setTitle("Vista Ampliada de la Secuencial Extendida");
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
        if (calcularTotalElementos() >= tableSize) {
            itemsArrayText.setText("Estructura llena. No se puede insertar.");
            return;
        }

        if (busquedaSecuencialGlobalNoAnim(input).found) {
            itemsArrayText.setText("Clave " + input + " ya existe.");
            return;
        }

        InsertionDecision dec = findInsertionPoint(input);
        if (!dec.canInsert) {
            itemsArrayText.setText("No se pudo insertar (estructura llena)");
            return;
        }

        boolean success = cascadeInsert(dec.blockIndex, dec.pos, input);
        if (!success) {
            itemsArrayText.setText("No hay espacio para insertar.");
            return;
        }

        List<VisitedPos> recorrido = buildRecorridoForInsertion(dec.blockIndex, dec.pos);

        animateRecorrido(recorrido, dec.blockIndex, dec.pos, Operation.INSERT, () -> {
            saveState(dec.blockIndex, dec.pos);
            showBlockDetails(dec.blockIndex);
        });

        newItemArray.clear();
        itemsArrayText.setText("Insercion en proceso.");
    }

    private static class InsertionDecision {
        int blockIndex;
        int pos;
        boolean canInsert;

        InsertionDecision(int b, int p, boolean c) {
            blockIndex = b;
            pos = p;
            canInsert = c;
        }
    }

    private InsertionDecision findInsertionPoint(String key) {

        if (calcularTotalElementos() == 0)
            return new InsertionDecision(0, 0, true);

        for (int b = 0; b < numBlocks; b++) {
            List<String> blk = blocks.get(b);
            if (blk.isEmpty()) {

                boolean prevAllFull = true;
                for (int pb = 0; pb < b; pb++) {
                    if (blocks.get(pb).size() < getBlockCapacity(pb)) {
                        prevAllFull = false;
                        break;
                    }
                }
                if (!prevAllFull) {

                    continue;
                } else {
                    return new InsertionDecision(b, 0, true);
                }
            } else {
                String last = blk.get(blk.size() - 1);

                if (Integer.parseInt(key) <= Integer.parseInt(last)) {

                    int p = 0;
                    while (p < blk.size() && Integer.parseInt(blk.get(p)) < Integer.parseInt(key))
                        p++;
                    return new InsertionDecision(b, p, true);
                } else {

                    if (blk.size() < getBlockCapacity(b)) {
                        return new InsertionDecision(b, blk.size(), true);
                    } else {

                        continue;
                    }
                }
            }
        }

        int lb = numBlocks - 1;
        List<String> lastBlk = blocks.get(lb);
        if (lastBlk.size() < getBlockCapacity(lb))
            return new InsertionDecision(lb, lastBlk.size(), true);
        return new InsertionDecision(-1, -1, false);
    }

    private boolean cascadeInsert(int b, int pos, String val) {
        if (b < 0 || b >= numBlocks)
            return false;
        List<String> blk = blocks.get(b);
        int capacity = getBlockCapacity(b);
        if (blk.size() < capacity) {

            if (pos <= blk.size())
                blk.add(pos, val);
            else
                blk.add(val);

            if (blk.size() > capacity)
                blk.remove(blk.size() - 1);

            cellColorsByBlock.put(b, ensureColorArray(cellColorsByBlock.get(b), capacity));
            return true;
        } else {

            String last = blk.get(blk.size() - 1);

            blk.add(pos, val);
            if (blk.size() > capacity)
                blk.remove(blk.size() - 1);
            int next = b + 1;
            if (next >= numBlocks) {

                blk.removeIf(s -> s.equals(val));
                return false;
            } else {

                return cascadeInsert(next, 0, last);
            }
        }
    }

    private String[] ensureColorArray(String[] arr, int capacity) {
        if (arr == null || arr.length != capacity) {
            String[] a = new String[capacity];
            Arrays.fill(a, "WHITE");
            return a;
        }
        return arr;
    }

    private static class VisitedPos {
        int block;
        int pos;

        VisitedPos(int b, int p) {
            block = b;
            pos = p;
        }
    }

    private List<VisitedPos> buildRecorridoForInsertion(int targetBlock, int targetPos) {
        List<VisitedPos> list = new ArrayList<>();
        for (int b = 0; b <= targetBlock; b++) {
            List<String> blk = blocks.get(b);
            int upto = (b < targetBlock) ? blk.size() : Math.max(1, Math.min(blk.size(), targetPos + 1));

            if (blk.isEmpty()) {
                list.add(new VisitedPos(b, 0));
            } else {
                for (int p = 0; p < upto; p++)
                    list.add(new VisitedPos(b, p));
            }
        }
        return list;
    }

    @FXML
    private void searchItem() {
        String key = modDeleteItem.getText();
        if (key == null || key.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave.");
            return;
        }
        if (!key.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("Clave debe tener " + maxDigits + " digitos.");
            return;
        }
        if (calcularTotalElementos() == 0) {
            itemsArrayText.setText("Estructura vacia.");
            return;
        }

        SearchPath path = buildSearchPath(key);
        if (path.recorrido.isEmpty()) {
            itemsArrayText.setText("Clave no encontrada (sin recorrido).");
            return;
        }

        long start = System.nanoTime();

        animateRecorridoSearch(path.recorrido, path.foundBlock, path.foundPos, path.found, Operation.SEARCH, () -> {
            long fin = System.nanoTime();
            long nanos = fin - start;
            String tiempo = nanos < 1_000_000 ? nanos + " ns" : String.format("%.4f ms", nanos / 1_000_000.0);
            if (path.found) {
                itemsArrayText.setText("Clave " + key + " encontrada en bloque " + (path.foundBlock + 1) + ", pos "
                        + (path.foundPos + 1) + " en " + tiempo + ".");
                showBlockDetails(path.foundBlock);
            } else {
                itemsArrayText.setText("Clave " + key + " no encontrada tras " + path.recorrido.size()
                        + " comparaciones en " + tiempo + ".");
            }
        });
    }

    private static class SearchPath {
        List<VisitedPos> recorrido = new ArrayList<>();
        boolean found = false;
        int foundBlock = -1;
        int foundPos = -1;
    }

    protected SearchPath buildSearchPath(String key) {
        SearchPath sp = new SearchPath();

        int left = 0;
        int right = numBlocks - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            List<String> blk = blocks.get(mid);

            if (blk.isEmpty()) {

                left = mid + 1;
                continue;
            }

            String last = blk.get(blk.size() - 1);

            sp.recorrido.add(new VisitedPos(mid, blk.size() - 1));

            int cmp = Integer.compare(Integer.parseInt(key), Integer.parseInt(last));

            if (cmp <= 0) {

                for (int p = 0; p < blk.size(); p++) {
                    sp.recorrido.add(new VisitedPos(mid, p));
                    if (blk.get(p).equals(key)) {
                        sp.found = true;
                        sp.foundBlock = mid;
                        sp.foundPos = p;
                        return sp;
                    }
                }

                right = mid - 1;
            } else {

                left = mid + 1;
            }
        }
        return sp;
    }

    @FXML
    protected void eliminateItem() {
        String key = modDeleteItem.getText();
        if (key == null || key.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave.");
            return;
        }
        if (!key.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("Clave debe tener " + maxDigits + " digitos.");
            return;
        }

        if (calcularTotalElementos() == 0) {
            itemsArrayText.setText("Estructura vacia.");
            return;
        }

        SearchPath path = buildSearchPath(key);
        if (path.recorrido.isEmpty()) {
            itemsArrayText.setText("Clave no encontrada.");
            return;
        }

        animateRecorridoSearch(path.recorrido, path.foundBlock, path.foundPos, path.found, Operation.DELETE, () -> {
            if (path.found) {
                removeAndCompact(path.foundBlock, path.foundPos);
                saveState(path.foundBlock, path.foundPos);
                itemsArrayText.setText("Clave eliminada con reorganizacion por bloques.");
                showBlockDetails(Math.max(0, Math.min(path.foundBlock, numBlocks - 1)));
            } else {
                itemsArrayText.setText("Clave no encontrada. No se elimino nada.");
            }
        });

        modDeleteItem.clear();
    }

    private void removeAndCompact(int blockIndex, int pos) {

        for (int b = blockIndex; b < numBlocks; b++) {
            List<String> blk = blocks.get(b);
            int capacity = getBlockCapacity(b);
            if (b == blockIndex) {
                if (pos < blk.size())
                    blk.remove(pos);
            }

            if (b + 1 < numBlocks) {
                List<String> next = blocks.get(b + 1);
                if (!next.isEmpty()) {

                    if (blk.size() < capacity) {
                        String moved = next.remove(0);
                        blk.add(moved);
                    } else {

                    }
                }
            }

            cellColorsByBlock.put(b, ensureColorArray(cellColorsByBlock.get(b), capacity));
        }

    }

    private enum Operation {
        INSERT, SEARCH, DELETE
    }

    private void animateRecorrido(List<VisitedPos> recorrido, int targetBlock, int targetPos, Operation op,
            Runnable callback) {

        Timeline timeline = new Timeline();
        Duration delay = Duration.ZERO;
        Duration step = Duration.millis(ANIM_STEP_MS);

        resetAllColors();

        for (VisitedPos vp : recorrido) {
            int b = vp.block, p = vp.pos;
            Duration d = delay;
            timeline.getKeyFrames().add(new KeyFrame(d, e -> {
                resetAllColors();

                setCellColor(b, p, "GRAY");

                currentBlockView = b;
                actualizarVistaArray();
            }));
            delay = delay.add(step);
        }

        timeline.getKeyFrames().add(new KeyFrame(delay, e -> {
            resetAllColors();
            if (targetBlock >= 0 && targetPos >= 0) {
                String finalColor = "GREEN";
                if (op == Operation.INSERT)
                    finalColor = "YELLOW";
                else if (op == Operation.DELETE)
                    finalColor = "RED";
                setCellColor(targetBlock, targetPos, finalColor);
                currentBlockView = targetBlock;
                actualizarVistaArray();
            } else {
                actualizarVistaArray();
            }
            if (callback != null)
                callback.run();
        }));

        timeline.play();
    }

    private void animateRecorridoSearch(List<VisitedPos> recorrido, int foundBlock, int foundPos, boolean found,
            Operation op, Runnable callback) {
        Timeline timeline = new Timeline();
        Duration delay = Duration.ZERO;
        Duration step = Duration.millis(ANIM_STEP_MS);

        resetAllColors();

        for (VisitedPos vp : recorrido) {
            int b = vp.block, p = vp.pos;
            Duration d = delay;
            timeline.getKeyFrames().add(new KeyFrame(d, e -> {
                resetAllColors();

                setCellColor(b, p, "GRAY");
                currentBlockView = b;
                actualizarVistaArray();
            }));
            delay = delay.add(step);
        }

        timeline.getKeyFrames().add(new KeyFrame(delay, e -> {
            resetAllColors();
            if (found && foundBlock >= 0 && foundPos >= 0) {
                setCellColor(foundBlock, foundPos, (op == Operation.DELETE) ? "RED" : "GREEN");
                currentBlockView = foundBlock;
                actualizarVistaArray();
            } else if (!recorrido.isEmpty()) {

                VisitedPos last = recorrido.get(recorrido.size() - 1);
                setCellColor(last.block, last.pos, "RED");
                currentBlockView = last.block;
                actualizarVistaArray();
            }
            if (callback != null)
                callback.run();
        }));

        timeline.play();
    }

    private void resetAllColors() {
        if (cellColorsByBlock == null)
            return;
        for (Map.Entry<Integer, String[]> e : cellColorsByBlock.entrySet())
            Arrays.fill(e.getValue(), "WHITE");
    }

    private void setCellColor(int blockIndex, int posZeroBased, String color) {
        if (cellColorsByBlock == null)
            return;
        String[] arr = cellColorsByBlock.get(blockIndex);
        if (arr == null) {
            arr = initColorArray(getBlockCapacity(blockIndex));
            cellColorsByBlock.put(blockIndex, arr);
        }
        if (posZeroBased >= 0 && posZeroBased < arr.length)
            arr[posZeroBased] = color.toUpperCase();
    }

    private SearchResult busquedaSecuencialGlobalNoAnim(String clave) {
        List<Integer> recorrido = new ArrayList<>();
        for (int b = 0; b < numBlocks; b++) {
            List<String> blk = blocks.get(b);
            for (int p = 0; p < blk.size(); p++) {
                recorrido.add(b);
                if (blk.get(p).equals(clave))
                    return new SearchResult(true, b, p, recorrido, clave);
            }
        }
        return new SearchResult(false, -1, -1, recorrido, clave);
    }

    private static class SearchResult {
        boolean found;
        int blockIndex;
        int position;
        List<Integer> recorrido;
        String foundKey;

        SearchResult(boolean f, int bi, int p, List<Integer> r, String k) {
            found = f;
            blockIndex = bi;
            position = p;
            recorrido = r;
            foundKey = k;
        }
    }

    @FXML
    private void undoAction() {
        if (undoStack.size() <= 1) {
            itemsArrayText.setText("No hay acciones para deshacer.");
            return;
        }
        ActionState cur = undoStack.pop();
        redoStack.push(cur);
        ActionState prev = undoStack.peek();
        applyState(prev, true);
        updateUndoRedoButtons();
        itemsArrayText.setText("Accion deshecha.");
    }

    @FXML
    private void redoAction() {
        if (redoStack.isEmpty()) {
            itemsArrayText.setText("No hay acciones para rehacer.");
            return;
        }
        ActionState next = redoStack.pop();
        undoStack.push(next);
        applyState(next, true);
        updateUndoRedoButtons();
        itemsArrayText.setText("Accion rehecha.");
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
        fileChooser.setTitle("Guardar Estado Secuencial Externa");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SecExt", "*.seqext"));
        File file = fileChooser.showSaveDialog(miViewList.getScene().getWindow());
        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                Map<String, Object> saveData = new HashMap<>();
                saveData.put("type", "SecuencialExterna");
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
        fileChooser.setTitle("Cargar Estado Secuencial Externa");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SecExt", "*.seqext"));
        File file = fileChooser.showOpenDialog(miViewList.getScene().getWindow());
        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object ld = ois.readObject();
                if (ld instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> save = (Map<String, Object>) ld;
                    String t = (String) save.get("type");
                    ActionState s = (ActionState) save.get("state");
                    if (!"SecuencialExterna".equals(t)) {
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

    private void setAllColorsToWhite() {
        if (cellColorsByBlock == null)
            return;
        for (String[] a : cellColorsByBlock.values())
            Arrays.fill(a, "WHITE");
    }
}
