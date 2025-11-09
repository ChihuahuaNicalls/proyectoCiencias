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
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class DynamicController {

    @FXML
    private MenuButton hashFunction;
    @FXML
    private MenuButton expRed;
    @FXML
    private Spinner<Integer> registrosCubeta;
    @FXML
    private Spinner<Integer> numberDigits;
    @FXML
    private Slider sliderOcup;
    @FXML
    private Label percentOcup;
    @FXML
    private Slider sliderOrd;
    @FXML
    private Label percentOrd;
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
    private Button loadButton;
    @FXML
    private TextField newItemArray;
    @FXML
    private TextField modDeleteItem;
    @FXML
    private TableView<ObservableList<String>> dynTable;
    @FXML
    private Text arrayLengthText;
    @FXML
    private Text itemsArrayText;
    @FXML
    private Label titleHash;

    private String selectedHashFunction;
    private String selectedExpRed;
    private int numRegistrosPorCubeta;
    private int maxDigits;
    private double porcentajeOcupacion;
    private double porcentajeOrdenamiento;

    private List<String> allKeys;
    private List<DynamicBucket> buckets;
    private int currentNumBuckets;

    private Timeline currentAnimation;
    private final Deque<List<String>> undoStack = new ArrayDeque<>();
    private final Deque<List<String>> redoStack = new ArrayDeque<>();

    private Map<String, String> cellColors = new HashMap<>();

    private static class DynamicBucket implements Serializable {
        private final List<String> records;
        private final List<String> auxiliaryCells;
        private final int bucketNumber;
        private final int maxRecords;

        public DynamicBucket(int bucketNumber, int maxRecords) {
            this.bucketNumber = bucketNumber;
            this.maxRecords = maxRecords;
            this.records = new ArrayList<>();
            this.auxiliaryCells = new ArrayList<>();
        }

        public boolean isFull() {
            return records.size() >= maxRecords;
        }

        public boolean addRecord(String key) {
            if (records.size() < maxRecords) {
                records.add(key);
                return true;
            } else {
                auxiliaryCells.add(key);
                return false;
            }
        }

        public boolean removeRecord(String key) {
            boolean removed = records.remove(key);
            if (!removed) {
                removed = auxiliaryCells.remove(key);
            } else {
                if (!auxiliaryCells.isEmpty() && records.size() < maxRecords) {
                    records.add(auxiliaryCells.remove(0));
                }
            }
            return removed;
        }

        public boolean contains(String key) {
            return records.contains(key) || auxiliaryCells.contains(key);
        }

        public List<String> getAllRecords() {
            List<String> all = new ArrayList<>(records);
            all.addAll(auxiliaryCells);
            return all;
        }

        public void clear() {
            records.clear();
            auxiliaryCells.clear();
        }

        public int getRecordCount() {
            return records.size() + auxiliaryCells.size();
        }

        public int getMainRecordCount() {
            return records.size();
        }

        public int getAuxiliaryCount() {
            return auxiliaryCells.size();
        }

        public int getBucketNumber() {
            return bucketNumber;
        }

        public List<String> getRecords() {
            return records;
        }

        public List<String> getAuxiliaryCells() {
            return auxiliaryCells;
        }
    }

    @FXML
    private void initialize() {
        SpinnerValueFactory<Integer> registrosFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 3);
        registrosCubeta.setValueFactory(registrosFactory);

        SpinnerValueFactory<Integer> digitsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 3);
        numberDigits.setValueFactory(digitsFactory);

        newItemArray.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        modDeleteItem.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

        configurarSliders();
        configurarMenuButtons();
        configurarTablaInicial();

        reiniciarButton.setDisable(true);
        insertButton.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);
        newItemArray.setDisable(true);
        modDeleteItem.setDisable(true);

        titleHash.setText("Hash Dinamico");
    }

    private void configurarSliders() {
        sliderOcup.setMin(0);
        sliderOcup.setMax(100);
        sliderOcup.setValue(70);
        percentOcup.setText("70%");
        porcentajeOcupacion = 0.70;

        sliderOcup.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            percentOcup.setText(value + "%");
            porcentajeOcupacion = value / 100.0;

            if (porcentajeOcupacion <= porcentajeOrdenamiento) {
                Platform.runLater(() -> {
                    sliderOcup.setValue(oldVal.doubleValue());
                    arrayLengthText.setText("Error: El porcentaje de ocupacion debe ser mayor al de ordenamiento");
                });
            } else {
                arrayLengthText.setText("Estructura sin crear");
            }
        });

        sliderOrd.setMin(0);
        sliderOrd.setMax(100);
        sliderOrd.setValue(30);
        percentOrd.setText("30%");
        porcentajeOrdenamiento = 0.30;

        sliderOrd.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            percentOrd.setText(value + "%");
            porcentajeOrdenamiento = value / 100.0;

            if (porcentajeOrdenamiento >= porcentajeOcupacion) {
                Platform.runLater(() -> {
                    sliderOrd.setValue(oldVal.doubleValue());
                    arrayLengthText.setText("Error: El porcentaje de ordenamiento debe ser menor al de ocupacion");
                });
            } else {
                arrayLengthText.setText("Estructura sin crear");
            }
        });
    }

    private void configurarMenuButtons() {
        for (MenuItem item : hashFunction.getItems()) {
            item.setOnAction(event -> {
                hashFunction.setText(item.getText());
                selectedHashFunction = item.getText();
            });
        }

        for (MenuItem item : expRed.getItems()) {
            item.setOnAction(event -> {
                expRed.setText(item.getText());
                selectedExpRed = item.getText();
            });
        }
    }

    private void configurarTablaInicial() {
        dynTable.getColumns().clear();
        dynTable.getItems().clear();

        for (int i = 0; i < 2; i++) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(String.valueOf(i));
            final int columnIndex = i;
            column.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                if (columnIndex < row.size()) {
                    return new SimpleStringProperty(row.get(columnIndex));
                } else {
                    return new SimpleStringProperty("");
                }
            });

            column.setCellFactory(tc -> new javafx.scene.control.TableCell<ObservableList<String>, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.isEmpty()) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);

                        int rowIndex = getIndex();
                        String cellKey = columnIndex + "," + rowIndex;

                        if (cellColors.containsKey(cellKey)) {
                            setStyle(cellColors.get(cellKey));
                        } else {

                            if (rowIndex >= numRegistrosPorCubeta) {
                                setStyle(
                                        "-fx-background-color: lightgray; -fx-border-color: darkgray; -fx-font-style: italic;");
                            } else {
                                setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
                            }
                        }
                    }
                }
            });

            dynTable.getColumns().add(column);
        }

        actualizarFilasTabla();
    }

    private void actualizarFilasTabla() {
        dynTable.getItems().clear();

        if (buckets == null) {

            int numFilas = numRegistrosPorCubeta > 0 ? numRegistrosPorCubeta : 3;
            for (int i = 0; i < numFilas; i++) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int j = 0; j < 2; j++) {
                    row.add("");
                }
                dynTable.getItems().add(row);
            }
            return;
        }

        int maxFilas = numRegistrosPorCubeta;
        for (DynamicBucket bucket : buckets) {
            maxFilas = Math.max(maxFilas, bucket.getRecordCount());
        }

        maxFilas = Math.min(maxFilas, numRegistrosPorCubeta + getMaxAuxiliaryRows());

        for (int fila = 0; fila < maxFilas; fila++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int cubeta = 0; cubeta < currentNumBuckets; cubeta++) {
                if (cubeta < buckets.size()) {
                    DynamicBucket bucket = buckets.get(cubeta);
                    List<String> allRecords = bucket.getAllRecords();
                    if (fila < allRecords.size()) {
                        row.add(allRecords.get(fila));
                    } else {
                        row.add("");
                    }
                } else {
                    row.add("");
                }
            }
            dynTable.getItems().add(row);
        }

        Platform.runLater(() -> {
            dynTable.refresh();
        });
    }

    private int getMaxAuxiliaryRows() {
        int maxAux = 0;
        if (buckets != null) {
            for (DynamicBucket bucket : buckets) {
                maxAux = Math.max(maxAux, bucket.getAuxiliaryCount());
            }
        }
        return maxAux;
    }

    private void actualizarColumnasTabla() {
        dynTable.getColumns().clear();

        for (int i = 0; i < currentNumBuckets; i++) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(String.valueOf(i));
            final int columnIndex = i;
            column.setCellValueFactory(param -> {
                ObservableList<String> row = param.getValue();
                if (columnIndex < row.size()) {
                    return new SimpleStringProperty(row.get(columnIndex));
                } else {
                    return new SimpleStringProperty("");
                }
            });

            column.setCellFactory(tc -> new javafx.scene.control.TableCell<ObservableList<String>, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.isEmpty()) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);

                        int rowIndex = getIndex();
                        String cellKey = columnIndex + "," + rowIndex;

                        if (cellColors.containsKey(cellKey)) {
                            setStyle(cellColors.get(cellKey));
                        } else {

                            if (rowIndex >= numRegistrosPorCubeta) {
                                setStyle(
                                        "-fx-background-color: lightgray; -fx-border-color: darkgray; -fx-font-style: italic;");
                            } else {
                                setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
                            }
                        }
                    }
                }
            });

            dynTable.getColumns().add(column);
        }

        actualizarFilasTabla();
    }

    @FXML
    private void crearArray() {
        if ("Elegir".equals(hashFunction.getText())) {
            arrayLengthText.setText("Debe seleccionar una funcion hash.");
            return;
        }
        if ("Elegir".equals(expRed.getText())) {
            arrayLengthText.setText("Debe seleccionar un metodo de expansion/reduccion.");
            return;
        }

        if (porcentajeOcupacion <= porcentajeOrdenamiento) {
            arrayLengthText.setText("Error: El porcentaje de ocupacion debe ser mayor al de ordenamiento.");
            return;
        }

        selectedHashFunction = hashFunction.getText();
        selectedExpRed = expRed.getText();
        numRegistrosPorCubeta = registrosCubeta.getValue();
        maxDigits = numberDigits.getValue();

        allKeys = new ArrayList<>();
        buckets = new ArrayList<>();
        currentNumBuckets = 2;

        for (int i = 0; i < currentNumBuckets; i++) {
            buckets.add(new DynamicBucket(i, numRegistrosPorCubeta));
        }

        actualizarColumnasTabla();
        saveState();

        createButton.setDisable(true);
        hashFunction.setDisable(true);
        expRed.setDisable(true);
        registrosCubeta.setDisable(true);
        numberDigits.setDisable(true);
        sliderOcup.setDisable(true);
        sliderOrd.setDisable(true);

        reiniciarButton.setDisable(false);
        insertButton.setDisable(false);
        searchButton.setDisable(false);
        deleteButton.setDisable(false);
        undoButton.setDisable(false);
        redoButton.setDisable(false);
        saveButton.setDisable(false);
        newItemArray.setDisable(false);
        modDeleteItem.setDisable(false);

        arrayLengthText.setText("Estructura creada con " + currentNumBuckets + " cubetas, " +
                numRegistrosPorCubeta + " registros por cubeta");
        itemsArrayText.setText("0 elementos insertados");
        titleHash.setText("Hash Dinamico - " + selectedHashFunction);
    }

    @FXML
    private void addToArray() {
        String key = newItemArray.getText();
        if (key.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave valida.");
            return;
        }

        if (!key.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("La clave debe tener " + maxDigits + " digitos.");
            return;
        }

        if (allKeys.contains(key)) {
            itemsArrayText.setText("La clave " + key + " ya existe.");
            return;
        }

        insertKeyWithoutAnimation(key);

        boolean expanded = checkAndExpand();

        if (expanded) {
            actualizarColumnasTabla();
        }

        int[] pos = findKeyPosition(key);
        if (pos[0] != -1) {
            animateInsertion(pos[0], pos[1], key);
        }

        newItemArray.clear();
        itemsArrayText.setText(allKeys.size() + " elementos insertados");
    }

    private void insertKey(String key) {
        int bucketIndex = calculateBucketIndex(key);
        DynamicBucket bucket = buckets.get(bucketIndex);

        int targetRow = bucket.getRecordCount();
        bucket.addRecord(key);
        allKeys.add(key);

        actualizarFilasTabla();
        saveState();
    }

    private void insertKeyWithoutAnimation(String key) {
        int bucketIndex = calculateBucketIndex(key);
        DynamicBucket bucket = buckets.get(bucketIndex);
        bucket.addRecord(key);
        allKeys.add(key);
        actualizarFilasTabla();
        saveState();
    }

    private int calculateBucketIndex(String key) {
        int keyInt = Integer.parseInt(key);

        switch (selectedHashFunction) {
            case "Modulo":
                return keyInt % currentNumBuckets;
            case "Cuadrada":
                long square = (long) keyInt * keyInt;
                String squareStr = String.valueOf(square);
                int mid = squareStr.length() / 2;
                int digits = (int) Math.ceil(Math.log10(currentNumBuckets + 1));
                int start = Math.max(0, mid - digits / 2);
                int end = Math.min(squareStr.length(), start + digits);
                String central = squareStr.substring(start, end);
                if (central.isEmpty())
                    return 0;
                return Integer.parseInt(central) % currentNumBuckets;
            case "Truncamiento":
                String keyStr = String.format("%0" + maxDigits + "d", keyInt);
                int truncValue = 0;
                for (int i = 0; i < keyStr.length(); i += 2) {
                    if (i < keyStr.length()) {
                        truncValue = truncValue * 10 + Character.getNumericValue(keyStr.charAt(i));
                    }
                }
                return truncValue % currentNumBuckets;
            case "Plegamiento":
                String foldStr = String.format("%0" + maxDigits + "d", keyInt);
                int sum = 0;
                for (int i = 0; i < foldStr.length(); i += 2) {
                    int endIndex = Math.min(i + 2, foldStr.length());
                    String segment = foldStr.substring(i, endIndex);
                    sum += Integer.parseInt(segment);
                }
                return sum % currentNumBuckets;
            default:
                return keyInt % currentNumBuckets;
        }
    }

    private int getPreviousSize(int currentSize) {
        if (currentSize <= 2)
            return 2;

        if (currentSize == 3)
            return 2;
        if (currentSize == 4)
            return 3;
        if (currentSize == 6)
            return 4;

        int prev2 = 0;
        int prev1 = 0;
        int current = 0;

        int a0 = 2;
        int a1 = 3;
        int a2 = 4;
        int a3 = 6;

        if (currentSize == a2)
            return a1;
        if (currentSize == a3)
            return a2;

        prev2 = a1;
        prev1 = a2;
        current = a3;

        while (current < currentSize) {
            prev2 = prev1;
            prev1 = current;
            current = 2 * prev2;
        }

        if (current == currentSize) {
            return prev1;
        }

        List<Integer> sequence = new ArrayList<>();
        sequence.add(2);
        sequence.add(3);

        int i = 2;
        while (true) {
            int next = 2 * sequence.get(i - 2);
            sequence.add(next);
            if (next >= currentSize) {
                break;
            }
            i++;
        }

        int index = sequence.size() - 1;
        for (int j = 0; j < sequence.size(); j++) {
            if (sequence.get(j) >= currentSize) {
                index = j - 1;
                break;
            }
        }

        return Math.max(2, sequence.get(Math.max(0, index)));
    }

    private int getNextSize(int currentSize) {
        if (currentSize < 2)
            return 2;

        if (currentSize == 2)
            return 3;
        if (currentSize == 3)
            return 4;
        if (currentSize == 4)
            return 6;

        int prev2 = 0;
        int prev1 = 0;

        int a0 = 2;
        int a1 = 3;

        if (currentSize == a0)
            return a1;
        if (currentSize == a1)
            return 4;

        prev2 = a0;
        prev1 = a1;
        int current = 4;

        while (current < currentSize) {
            prev2 = prev1;
            prev1 = current;
            current = 2 * prev2;
        }

        if (current == currentSize) {
            return 2 * prev1;
        }

        return currentSize * 2;
    }

    private boolean checkAndExpand() {
        int totalCapacity = currentNumBuckets * numRegistrosPorCubeta;
        if (totalCapacity == 0)
            return false;

        double ocupacionActual = (double) allKeys.size() / totalCapacity;

        if (ocupacionActual >= porcentajeOcupacion) {
            int newNumBuckets = getNextSize(currentNumBuckets);

            if (newNumBuckets > currentNumBuckets) {
                expandStructure(newNumBuckets);
                return true;
            }
        }
        return false;
    }

    private void expandStructure(int newNumBuckets) {
        List<DynamicBucket> newBuckets = new ArrayList<>();
        for (int i = 0; i < newNumBuckets; i++) {
            newBuckets.add(new DynamicBucket(i, numRegistrosPorCubeta));
        }

        List<String> keysToReinsert = new ArrayList<>(allKeys);
        allKeys.clear();
        buckets = newBuckets;
        currentNumBuckets = newNumBuckets;

        for (String key : keysToReinsert) {
            insertKeyWithoutAnimation(key);
        }

        saveState();
        arrayLengthText.setText("Estructura expandida a " + currentNumBuckets + " cubetas");
    }

    @FXML
    private void eliminateItem() {
        String key = modDeleteItem.getText();
        if (key.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave valida.");
            return;
        }

        if (!key.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("La clave debe tener " + maxDigits + " digitos.");
            return;
        }

        if (!allKeys.contains(key)) {
            itemsArrayText.setText("La clave " + key + " no existe.");
            return;
        }

        int[] pos = findKeyPosition(key);

        deleteKeyWithoutAnimation(key);

        boolean reduced = checkAndReduce();

        if (reduced) {
            actualizarColumnasTabla();
        }

        if (pos[0] != -1) {
            animateDeletion(pos[0], pos[1], key);
        }

        modDeleteItem.clear();
        itemsArrayText.setText(allKeys.size() + " elementos insertados");
    }

    private void deleteKey(String key) {
        for (DynamicBucket bucket : buckets) {
            if (bucket.removeRecord(key)) {
                break;
            }
        }

        allKeys.remove(key);
        actualizarFilasTabla();
        saveState();
    }

    private void deleteKeyWithoutAnimation(String key) {
        for (DynamicBucket bucket : buckets) {
            if (bucket.removeRecord(key)) {
                break;
            }
        }

        allKeys.remove(key);
        actualizarFilasTabla();
        saveState();
    }

    private int[] findKeyPosition(String key) {
        int[] result = { -1, -1 };

        for (int i = 0; i < buckets.size(); i++) {
            DynamicBucket bucket = buckets.get(i);
            List<String> allRecords = bucket.getAllRecords();
            for (int j = 0; j < allRecords.size(); j++) {
                if (allRecords.get(j).equals(key)) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return result;
    }

    private boolean checkAndReduce() {
        if (currentNumBuckets <= 2)
            return false;

        double ocupacionPorBucket = (double) allKeys.size() / currentNumBuckets;

        if (ocupacionPorBucket < porcentajeOrdenamiento) {
            int newNumBuckets = getPreviousSize(currentNumBuckets);

            if (newNumBuckets < currentNumBuckets) {
                reduceStructure(newNumBuckets);
                return true;
            }
        }
        return false;
    }

    private void reduceStructure(int newNumBuckets) {
        List<DynamicBucket> newBuckets = new ArrayList<>();
        for (int i = 0; i < newNumBuckets; i++) {
            newBuckets.add(new DynamicBucket(i, numRegistrosPorCubeta));
        }

        List<String> keysToReinsert = new ArrayList<>(allKeys);
        allKeys.clear();
        buckets = newBuckets;
        currentNumBuckets = newNumBuckets;

        for (String key : keysToReinsert) {
            insertKeyWithoutAnimation(key);
        }

        saveState();
        arrayLengthText.setText("Estructura reducida a " + currentNumBuckets + " cubetas");
    }

    @FXML
    private void searchItem() {
        String key = modDeleteItem.getText();
        if (key.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave valida.");
            return;
        }

        animateSearch(key);
    }

    private void animateInsertion(int bucketIndex, int targetRow, String key) {
        resetAnimation();

        Timeline timeline = new Timeline();
        final int finalBucketIndex = bucketIndex;
        final int finalTargetRow = targetRow;

        KeyFrame highlightStart = new KeyFrame(Duration.ZERO,
                e -> highlightCell(finalBucketIndex, finalTargetRow, "YELLOW"));

        KeyFrame highlightEnd = new KeyFrame(Duration.millis(1500),
                e -> {
                    resetCellColor(finalBucketIndex, finalTargetRow);
                    itemsArrayText.setText("Clave " + key + " insertada en cubeta " + bucketIndex);
                });

        timeline.getKeyFrames().addAll(highlightStart, highlightEnd);
        timeline.play();
        currentAnimation = timeline;
    }

    private void animateDeletion(int bucketIndex, int targetRow, String key) {
        resetAnimation();

        Timeline timeline = new Timeline();
        final int finalBucketIndex = bucketIndex;
        final int finalTargetRow = targetRow;

        KeyFrame highlightStart = new KeyFrame(Duration.ZERO,
                e -> highlightCell(finalBucketIndex, finalTargetRow, "RED"));

        KeyFrame highlightEnd = new KeyFrame(Duration.millis(1500),
                e -> {
                    resetCellColor(finalBucketIndex, finalTargetRow);
                    itemsArrayText.setText("Clave " + key + " eliminada");
                });

        timeline.getKeyFrames().addAll(highlightStart, highlightEnd);
        timeline.play();
        currentAnimation = timeline;
    }

    private void animateSearch(String key) {
        resetAnimation();

        final int[] targetInfo = findKeyPosition(key);
        final boolean found = targetInfo[0] != -1;
        final int finalTargetBucket = targetInfo[0];
        final int finalTargetRow = targetInfo[1];

        if (!found) {
            itemsArrayText.setText("Clave " + key + " no encontrada");
            return;
        }

        Timeline timeline = new Timeline();

        KeyFrame highlightStart = new KeyFrame(Duration.ZERO,
                e -> highlightCell(finalTargetBucket, finalTargetRow, "GREEN"));

        KeyFrame highlightEnd = new KeyFrame(Duration.millis(1500),
                e -> {
                    resetCellColor(finalTargetBucket, finalTargetRow);
                    itemsArrayText.setText("Clave " + key + " encontrada en cubeta " + finalTargetBucket);
                });

        timeline.getKeyFrames().addAll(highlightStart, highlightEnd);
        timeline.play();
        currentAnimation = timeline;
    }

    private void highlightCell(int columnIndex, int rowIndex, String color) {
        String cellKey = columnIndex + "," + rowIndex;
        String colorStyle;

        boolean isAuxiliary = rowIndex >= numRegistrosPorCubeta;

        if (isAuxiliary) {

            switch (color.toUpperCase()) {
                case "YELLOW":
                    colorStyle = "-fx-background-color: #FFF8C6; -fx-border-color: darkgray; -fx-font-style: italic; -fx-font-weight: bold;";
                    break;
                case "GREEN":
                    colorStyle = "-fx-background-color: #C6FFC6; -fx-border-color: darkgray; -fx-font-style: italic; -fx-font-weight: bold;";
                    break;
                case "RED":
                    colorStyle = "-fx-background-color: #FFC6C6; -fx-border-color: darkgray; -fx-font-style: italic; -fx-font-weight: bold;";
                    break;
                default:
                    colorStyle = "-fx-background-color: lightgray; -fx-border-color: darkgray; -fx-font-style: italic;";
            }
        } else {

            switch (color.toUpperCase()) {
                case "YELLOW":
                    colorStyle = "-fx-background-color: yellow; -fx-border-color: darkgray; -fx-font-weight: bold;";
                    break;
                case "GREEN":
                    colorStyle = "-fx-background-color: lightgreen; -fx-border-color: darkgray; -fx-font-weight: bold;";
                    break;
                case "RED":
                    colorStyle = "-fx-background-color: lightcoral; -fx-border-color: darkgray; -fx-font-weight: bold;";
                    break;
                default:
                    colorStyle = "-fx-background-color: white; -fx-border-color: lightgray;";
            }
        }

        cellColors.put(cellKey, colorStyle);
        Platform.runLater(() -> dynTable.refresh());
    }

    private void resetCellColor(int columnIndex, int rowIndex) {
        String cellKey = columnIndex + "," + rowIndex;
        cellColors.remove(cellKey);
        Platform.runLater(() -> dynTable.refresh());
    }

    private void resetAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
        }
        cellColors.clear();
        Platform.runLater(() -> dynTable.refresh());
    }

    private void saveState() {
        redoStack.clear();

        undoStack.push(new ArrayList<>(allKeys));
        updateUndoRedoButtons();
    }

    private void applyState(List<String> keys) {

        allKeys.clear();
        for (DynamicBucket bucket : buckets) {
            bucket.clear();
        }

        for (String key : keys) {
            int bucketIndex = calculateBucketIndex(key);
            if (bucketIndex < buckets.size()) {
                buckets.get(bucketIndex).addRecord(key);
            }
        }
        allKeys.addAll(keys);

        checkStructureSize();

        actualizarColumnasTabla();
        updateUndoRedoButtons();
    }

    private void checkStructureSize() {

        int totalCapacity = currentNumBuckets * numRegistrosPorCubeta;
        if (totalCapacity > 0) {
            double ocupacionActual = (double) allKeys.size() / totalCapacity;

            if (ocupacionActual >= porcentajeOcupacion) {
                int newNumBuckets = getNextSize(currentNumBuckets);
                if (newNumBuckets > currentNumBuckets) {
                    expandStructureForUndoRedo(newNumBuckets);
                }
            }
        }

        if (currentNumBuckets > 2) {
            double ocupacionPorBucket = (double) allKeys.size() / currentNumBuckets;

            if (ocupacionPorBucket < porcentajeOrdenamiento) {
                int newNumBuckets = getPreviousSize(currentNumBuckets);
                if (newNumBuckets < currentNumBuckets) {
                    reduceStructureForUndoRedo(newNumBuckets);
                }
            }
        }
    }

    private void expandStructureForUndoRedo(int newNumBuckets) {
        List<DynamicBucket> newBuckets = new ArrayList<>();
        for (int i = 0; i < newNumBuckets; i++) {
            newBuckets.add(new DynamicBucket(i, numRegistrosPorCubeta));
        }

        List<String> keysToReinsert = new ArrayList<>(allKeys);
        allKeys.clear();
        buckets = newBuckets;
        currentNumBuckets = newNumBuckets;

        for (String key : keysToReinsert) {
            insertKeyWithoutAnimation(key);
        }

        actualizarColumnasTabla();
    }

    private void reduceStructureForUndoRedo(int newNumBuckets) {
        List<DynamicBucket> newBuckets = new ArrayList<>();
        for (int i = 0; i < newNumBuckets; i++) {
            newBuckets.add(new DynamicBucket(i, numRegistrosPorCubeta));
        }

        List<String> keysToReinsert = new ArrayList<>(allKeys);
        allKeys.clear();
        buckets = newBuckets;
        currentNumBuckets = newNumBuckets;

        for (String key : keysToReinsert) {
            insertKeyWithoutAnimation(key);
        }

        actualizarColumnasTabla();
    }

    @FXML
    private void undoAction() {
        if (undoStack.size() <= 1)
            return;

        List<String> currentState = undoStack.pop();
        redoStack.push(currentState);
        List<String> previousState = undoStack.peek();
        applyState(previousState);

        itemsArrayText.setText(allKeys.size() + " elementos insertados (Deshecho)");
    }

    @FXML
    private void redoAction() {
        if (redoStack.isEmpty())
            return;

        List<String> nextState = redoStack.pop();
        undoStack.push(nextState);
        applyState(nextState);

        itemsArrayText.setText(allKeys.size() + " elementos insertados (Rehecho)");
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
    }

    @FXML
    private void reiniciar() {
        undoStack.clear();
        redoStack.clear();

        createButton.setDisable(false);
        hashFunction.setDisable(false);
        expRed.setDisable(false);
        registrosCubeta.setDisable(false);
        numberDigits.setDisable(false);
        sliderOcup.setDisable(false);
        sliderOrd.setDisable(false);

        reiniciarButton.setDisable(true);
        insertButton.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);
        newItemArray.setDisable(true);
        modDeleteItem.setDisable(true);

        hashFunction.setText("Elegir");
        expRed.setText("Elegir");

        allKeys = new ArrayList<>();
        buckets = new ArrayList<>();

        currentNumBuckets = 2;
        configurarTablaInicial();
        actualizarColumnasTabla();

        arrayLengthText.setText("Estructura sin crear");
        itemsArrayText.setText("No hay elementos en la estructura");
        titleHash.setText("Hash Dinamico");

        allKeys = null;
        buckets = null;

        resetAnimation();
    }

    @FXML
    private void saveArray() {
        if (allKeys == null || undoStack.isEmpty()) {
            itemsArrayText.setText("No hay estructura para guardar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Estado de Estructura Dinamica");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado", "*.dyn"));
        File file = fileChooser.showSaveDialog(dynTable.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                Map<String, Object> saveData = new HashMap<>();
                saveData.put("hashFunction", this.selectedHashFunction);
                saveData.put("expRed", this.selectedExpRed);
                saveData.put("numRegistrosPorCubeta", this.numRegistrosPorCubeta);
                saveData.put("maxDigits", this.maxDigits);
                saveData.put("porcentajeOcupacion", this.porcentajeOcupacion);
                saveData.put("porcentajeOrdenamiento", this.porcentajeOrdenamiento);
                saveData.put("currentNumBuckets", this.currentNumBuckets);
                saveData.put("keys", this.allKeys);
                oos.writeObject(saveData);
                itemsArrayText.setText("Estado guardado en: " + file.getName());
            } catch (IOException e) {
                itemsArrayText.setText("Error al guardar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void loadArray() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Estado de Estructura Dinamica");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado", "*.dyn"));
        File file = fileChooser.showOpenDialog(dynTable.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object loadedData = ois.readObject();

                if (loadedData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> saveData = (Map<String, Object>) loadedData;

                    String savedHashFunction = (String) saveData.get("hashFunction");
                    String savedExpRed = (String) saveData.get("expRed");
                    int savedNumRegistros = (int) saveData.get("numRegistrosPorCubeta");
                    int savedMaxDigits = (int) saveData.get("maxDigits");
                    double savedOcupacion = (double) saveData.get("porcentajeOcupacion");
                    double savedOrdenamiento = (double) saveData.get("porcentajeOrdenamiento");
                    int savedNumBuckets = (int) saveData.get("currentNumBuckets");
                    List<String> savedKeys = (List<String>) saveData.get("keys");

                    hashFunction.setText(savedHashFunction);
                    expRed.setText(savedExpRed);
                    selectedHashFunction = savedHashFunction;
                    selectedExpRed = savedExpRed;
                    numRegistrosPorCubeta = savedNumRegistros;
                    maxDigits = savedMaxDigits;
                    porcentajeOcupacion = savedOcupacion;
                    porcentajeOrdenamiento = savedOrdenamiento;

                    registrosCubeta.getValueFactory().setValue(savedNumRegistros);
                    numberDigits.getValueFactory().setValue(savedMaxDigits);
                    sliderOcup.setValue(savedOcupacion * 100);
                    sliderOrd.setValue(savedOrdenamiento * 100);

                    allKeys = new ArrayList<>(savedKeys);
                    buckets = new ArrayList<>();
                    currentNumBuckets = savedNumBuckets;

                    for (int i = 0; i < currentNumBuckets; i++) {
                        buckets.add(new DynamicBucket(i, numRegistrosPorCubeta));
                    }

                    for (String key : allKeys) {
                        int bucketIndex = calculateBucketIndex(key);
                        buckets.get(bucketIndex).addRecord(key);
                    }

                    actualizarColumnasTabla();

                    undoStack.clear();
                    redoStack.clear();
                    undoStack.push(new ArrayList<>(allKeys));

                    createButton.setDisable(true);
                    hashFunction.setDisable(true);
                    expRed.setDisable(true);
                    registrosCubeta.setDisable(true);
                    numberDigits.setDisable(true);
                    sliderOcup.setDisable(true);
                    sliderOrd.setDisable(true);

                    reiniciarButton.setDisable(false);
                    insertButton.setDisable(false);
                    searchButton.setDisable(false);
                    deleteButton.setDisable(false);
                    undoButton.setDisable(false);
                    redoButton.setDisable(false);
                    saveButton.setDisable(false);
                    newItemArray.setDisable(false);
                    modDeleteItem.setDisable(false);

                    itemsArrayText.setText("Estado cargado desde: " + file.getName());
                    titleHash.setText("Hash Dinamico - " + selectedHashFunction);
                }
            } catch (IOException | ClassNotFoundException e) {
                itemsArrayText.setText("Error al cargar el archivo: " + e.getMessage());
            }
        }
    }
}