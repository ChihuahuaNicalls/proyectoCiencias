package ciencias.Research;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

public class BinariaController {

    @FXML
    private TextField newItemArray;
    @FXML
    private TextField modDeleteItem;
    @FXML
    private Label titleBin;
    @FXML
    private Label functionBin;
    @FXML
    private Text arrayLengthText;
    @FXML
    private Text itemsArrayText;
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
    private ListView<String> miViewList;
    @FXML
    private Spinner<Integer> numberDigits;
    @FXML
    private Button saveButton;
    @FXML
    private MenuButton rangeBin;

    private String[] array;
    private int arraySize;
    private int maxDigits;
    private int currentIndex = 0;

    private String[] cellColors;

    private final Deque<ActionState> undoStack = new ArrayDeque<>();
    private final Deque<ActionState> redoStack = new ArrayDeque<>();

    private static class ActionState implements Serializable {
        private final String[] arraySnapshot;
        private final int arraySizeSnapshot;
        private final int maxDigitsSnapshot;
        private final int currentIndexSnapshot;
        private final int lastModifiedPosition;

        ActionState(String[] array, int arraySize, int maxDigits, int currentIndex, int lastModifiedPosition) {
            this.arraySnapshot = array != null ? array.clone() : null;
            this.arraySizeSnapshot = arraySize;
            this.maxDigitsSnapshot = maxDigits;
            this.currentIndexSnapshot = currentIndex;
            this.lastModifiedPosition = lastModifiedPosition;
        }

        public String[] getArraySnapshot() {
            return arraySnapshot;
        }

        public int getArraySizeSnapshot() {
            return arraySizeSnapshot;
        }

        public int getMaxDigitsSnapshot() {
            return maxDigitsSnapshot;
        }

        public int getCurrentIndexSnapshot() {
            return currentIndexSnapshot;
        }

        public int getLastModifiedPosition() {
            return lastModifiedPosition;
        }
    }

    private void marcarPosicion(int index, String color) {
        if (index < 0 || index >= arraySize)
            return;
        cellColors[index] = color.toUpperCase();
        miViewList.refresh();
    }

    private void scrollToPosition(int position) {
        if (position >= 0 && position < arraySize) {
            miViewList.scrollTo(position);
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

        miViewList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    int index = getIndex();
                    if (index >= 0 && index < arraySize) {
                        switch (cellColors[index]) {
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
                            case "BLUE":
                                setStyle("-fx-background-color: lightblue; -fx-text-fill: black;");
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

        for (MenuItem item : rangeBin.getItems()) {
            item.setOnAction(event -> rangeBin.setText(item.getText()));
        }

        undoButton.setDisable(true);
        redoButton.setDisable(true);
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        saveButton.setDisable(true);

        insertButton.setDisable(true);
        newItemArray.setDisable(true);
        modDeleteItem.setDisable(true);
    }

    public void initData() {
        titleBin.setText("Busqueda Binaria");
        functionBin.setText("O(n)=log(n)");
    }

    @FXML
    private void crearArray() {
        String rangoSeleccionado = rangeBin.getText();
        if ("Elegir".equalsIgnoreCase(rangoSeleccionado)) {
            arrayLengthText.setText("Debe seleccionar un rango antes de crear el array.");
            return;
        }
        try {
            arraySize = Integer.parseInt(rangoSeleccionado);
        } catch (NumberFormatException e) {
            arrayLengthText.setText("Error: rango invalido.");
            return;
        }

        maxDigits = numberDigits.getValue();

        array = new String[arraySize];
        cellColors = new String[arraySize];
        Arrays.fill(array, null);
        Arrays.fill(cellColors, "WHITE");
        currentIndex = 0;

        saveState(-1);

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

        arrayLengthText.setText("Array de " + arraySize + " posiciones creado. Claves de " + maxDigits + " digitos.");
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

        if (currentIndex >= arraySize) {
            itemsArrayText.setText("Array lleno. No se pueden insertar mas elementos.");
            return;
        }

        int existingIndex = busquedaBinaria(input, 0, currentIndex - 1);
        if (existingIndex >= 0 && array[existingIndex] != null && array[existingIndex].equals(input)) {
            itemsArrayText.setText("Error: La clave " + input + " ya existe en el array.");
            return;
        }

        int insertIndex = encontrarPosicionInsercion(input);

        for (int i = currentIndex; i > insertIndex; i--) {
            array[i] = array[i - 1];
        }

        array[insertIndex] = input;
        currentIndex++;

        Arrays.fill(cellColors, "WHITE");
        marcarPosicion(insertIndex, "YELLOW");
        scrollToPosition(insertIndex);

        saveState(insertIndex);

        itemsArrayText.setText("Clave " + input + " insertada en la posicion " + insertIndex + ".");
        actualizarVistaArray();
    }

    private int encontrarPosicionInsercion(String clave) {
        if (currentIndex == 0)
            return 0;

        int low = 0;
        int high = currentIndex - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            int comparacion = array[mid].compareTo(clave);

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

    private int busquedaBinaria(String clave, int low, int high) {
        while (low <= high) {
            int mid = (low + high) / 2;
            int comparacion = array[mid].compareTo(clave);

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
        if (array == null || currentIndex == 0) {
            itemsArrayText.setText("No hay array creado o esta vacio.");
            return;
        }

        Arrays.fill(cellColors, "WHITE");
        miViewList.refresh();

        long inicio = System.nanoTime();

        java.util.List<Integer> recorrido = new java.util.ArrayList<>();
        boolean encontrado = false;
        int posicionEncontrada = -1;

        int low = 0;
        int high = currentIndex - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            recorrido.add(mid);

            int comparacion = array[mid].compareTo(claveStr);

            if (comparacion < 0) {
                low = mid + 1;
            } else if (comparacion > 0) {
                high = mid - 1;
            } else {
                encontrado = true;
                posicionEncontrada = mid;
                break;
            }
        }

        long fin = System.nanoTime();
        long nanos = fin - inicio;

        String tiempo;
        if (nanos < 1_000_000) {
            tiempo = nanos + " ns";
        } else {
            tiempo = String.format("%.4f ms", nanos / 1_000_000.0);
        }

        if (encontrado) {
            if (eliminar) {

                for (int i = posicionEncontrada; i < currentIndex - 1; i++) {
                    array[i] = array[i + 1];
                }
                array[currentIndex - 1] = null;
                currentIndex--;
                saveState(posicionEncontrada);
                actualizarVistaArray();
                itemsArrayText.setText("Clave " + claveStr + " eliminada en pos " + posicionEncontrada +
                        " en " + tiempo + ".");
            } else {
                itemsArrayText.setText("Clave " + claveStr + " encontrada en pos " + posicionEncontrada +
                        " en " + tiempo + ".");
            }
        } else {
            itemsArrayText.setText("Clave " + claveStr + " no encontrada tras " + recorrido.size() +
                    " comparaciones en " + tiempo + ".");
        }

        animateSearch(recorrido, encontrado, posicionEncontrada, eliminar);
    }

    private void animateSearch(java.util.List<Integer> recorrido, boolean found, int foundPos, boolean eliminar) {
        Arrays.fill(cellColors, "WHITE");
        miViewList.refresh();

        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.util.Duration delay = javafx.util.Duration.ZERO;
        javafx.util.Duration step = javafx.util.Duration.seconds(0.5);

        for (int pos : recorrido) {
            int current = pos;
            timeline.getKeyFrames().add(new javafx.animation.KeyFrame(delay, e -> {
                marcarPosicion(current, "GRAY");
                scrollToPosition(current);
            }));
            delay = delay.add(step);
        }

        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(delay, e -> {
            if (found) {
                if (eliminar) {
                    marcarPosicion(foundPos, "RED");
                } else {
                    marcarPosicion(foundPos, "GREEN");
                }
                scrollToPosition(foundPos);
            } else if (!recorrido.isEmpty()) {
                int last = recorrido.get(recorrido.size() - 1);
                marcarPosicion(last, "RED");
                scrollToPosition(last);
            }
        }));

        timeline.play();
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

    private void saveState(int lastModifiedPosition) {
        redoStack.clear();
        undoStack.push(new ActionState(
                array, arraySize, maxDigits, currentIndex, lastModifiedPosition));
        updateUndoRedoButtons();
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

    private void applyState(ActionState state, boolean markLastModified) {
        this.array = state.getArraySnapshot() != null ? state.getArraySnapshot().clone() : null;
        this.arraySize = state.getArraySizeSnapshot();
        this.maxDigits = state.getMaxDigitsSnapshot();
        this.currentIndex = state.getCurrentIndexSnapshot();

        if (cellColors == null || cellColors.length != arraySize) {
            cellColors = new String[arraySize];
        }
        Arrays.fill(cellColors, "WHITE");

        int lastPos = state.getLastModifiedPosition();
        if (markLastModified && lastPos != -1) {
            marcarPosicion(lastPos, "GRAY");
            scrollToPosition(lastPos);
        }

        actualizarVistaArray();

        saveButton.setDisable(array == null);
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
    }

    @FXML
    private void reiniciar() {
        undoStack.clear();
        redoStack.clear();

        array = null;
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

        updateUndoRedoButtons();
    }

    private void actualizarVistaArray() {
        miViewList.getItems().clear();
        if (array == null)
            return;
        for (int i = 0; i < arraySize; i++) {
            String valor = (array[i] == null) ? "-" : array[i];
            miViewList.getItems().add("Pos " + i + ": " + valor);
            if (cellColors != null && i < cellColors.length && cellColors[i] == null) {
                cellColors[i] = "WHITE";
            }
        }
        miViewList.refresh();
    }

    @FXML
    private void saveArray() {
        if (array == null) {
            itemsArrayText.setText("No hay estructura creada para guardar.");
            return;
        }

        if (undoStack.isEmpty()) {
            itemsArrayText.setText("No hay estado para guardar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Estado de Busqueda Binaria");
        fileChooser.setInitialDirectory(new File("src/main/resources/docs"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado Binaria", "*.bin"));
        File file = fileChooser.showSaveDialog(miViewList.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                Map<String, Object> saveData = new HashMap<>();
                saveData.put("type", "Binaria");
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
        fileChooser.setTitle("Cargar Estado de Busqueda Binaria");
        fileChooser.setInitialDirectory(new File("src/main/resources/docs"));
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

                    if (!"Binaria".equals(savedType)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error de Carga");
                        alert.setHeaderText("Tipo de estructura incompatible");
                        alert.setContentText("El archivo fue guardado para: " + savedType +
                                "\nPero actualmente esta seleccionada: Busqueda Binaria");
                        alert.showAndWait();

                        itemsArrayText.setText("Error: Tipo de estructura incompatible");
                        return;
                    }

                    undoStack.clear();
                    redoStack.clear();
                    undoStack.push(loadedState);

                    applyState(loadedState, false);
                    updateUndoRedoButtons();

                    if (array != null) {
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
}