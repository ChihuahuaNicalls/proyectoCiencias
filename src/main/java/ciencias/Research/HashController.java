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

import ciencias.ResearchController;
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
    private Button saveButton;

    private int[] truncPositions;
    private int truncMaxSelections = 0;
    private boolean truncPositionsSet = false;

    private ResearchController researchController;

    private String[] table;
    private int tableSize;
    private String hashString;
    private String collisionString;
    private int maxDigits;
    private String pendingKey = null;

    private String[] cellColors;

    private final Deque<ActionState> undoStack = new ArrayDeque<>();
    private final Deque<ActionState> redoStack = new ArrayDeque<>();

    private static class ActionState implements Serializable {
        private final String[] tableSnapshot;
        private final int tableSizeSnapshot;
        private final int maxDigitsSnapshot;
        private final String hashStringSnapshot;
        private final String collisionMethodSnapshot;
        private final int[] truncPositionsSnapshot;
        private final boolean truncPositionsSetSnapshot;
        private final int lastModifiedPosition;

        ActionState(String[] table, int tableSize, int maxDigits, String hashString,
                String collisionMethod, int[] truncPositions, boolean truncPositionsSet, int lastModifiedPosition) {
            this.tableSnapshot = table != null ? table.clone() : null;
            this.tableSizeSnapshot = tableSize;
            this.maxDigitsSnapshot = maxDigits;
            this.hashStringSnapshot = hashString;
            this.collisionMethodSnapshot = collisionMethod;
            this.truncPositionsSnapshot = truncPositions != null ? truncPositions.clone() : null;
            this.truncPositionsSetSnapshot = truncPositionsSet;
            this.lastModifiedPosition = lastModifiedPosition;
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
    }

    private void marcarPosicion(int index, String color) {
        if (index < 1 || index > tableSize)
            return;
        cellColors[index] = color.toUpperCase();
        miViewList.refresh();
    }

    private void scrollToPosition(int position) {
        if (position >= 1 && position <= tableSize) {
            miViewList.scrollTo(position - 1);
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

                    int index = getIndex() + 1;
                    if (index >= 1 && index <= tableSize) {
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
            arrayLengthText.setText("Error: rango inválido.");
            return;
        }

        if ("Truncamiento".equals(hashString) && (truncPositions == null || truncPositions.length == 0)) {
            arrayLengthText.setText("Error: No se han definido posiciones de truncamiento.");
            return;
        }

        // Crear tabla con tamaño tableSize + 1 para índices 1-based
        table = new String[tableSize + 1]; // Cambiado a String[]
        cellColors = new String[tableSize + 1];
        Arrays.fill(table, null); // Inicializar con null
        Arrays.fill(cellColors, "white");

        saveState(-1); // -1 indica que no hay posición modificada

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
        saveButton.setDisable(false); // Habilitar guardar después de crear

        undoButton.setDisable(true);
        redoButton.setDisable(true);

        arrayLengthText.setText("Array de " + tableSize + " posiciones creado. Claves de " + maxDigits + " dígitos.");
        if ("Truncamiento".equals(hashString)) {
            arrayLengthText.setText(arrayLengthText.getText() +
                    "\nPosiciones truncadas: " + Arrays.toString(truncPositions));
        }
        actualizarVistaArray();
    }

    @FXML
    private void addToArray() {
        String input = newItemArray.getText();
        if (input.isEmpty()) {
            itemsArrayText.setText("Por favor, ingrese una clave.");
            return;
        }

        // Validar que sea numérico y tenga exactamente maxDigits dígitos
        if (!input.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("Error: La clave debe tener exactamente " + maxDigits + " dígitos.");
            return;
        }

        doInsert(input);
        newItemArray.clear();
    }

    private boolean doInsert(String claveStr) {
        if (claveStr.length() != maxDigits) {
            itemsArrayText.setText("Error: La clave debe tener " + maxDigits + " dígitos.");
            return false;
        }

        int claveInt = Integer.parseInt(claveStr); // Convertir a int para operaciones de hash
        int pos = aplicarFuncionHash(claveInt);
        int originalPos = pos;
        int step = 1;

        while (table[pos] != null) {
            if (Integer.parseInt(table[pos]) == claveInt) { // Comparar como enteros
                itemsArrayText.setText("Error: La clave " + claveStr + " ya existe.");
                return false;
            }

            if (collisionString == null) {
                itemsArrayText.setText("¡Colisión detectada en la posición " + pos +
                        "!\nElija y defina un método de resolución.");
                pendingKey = claveStr;
                collisionHash.setDisable(false);
                defineCollitionsButton.setDisable(false);

                insertButton.setDisable(true);
                searchButton.setDisable(true);
                deleteButton.setDisable(true);
                newItemArray.setDisable(true);
                modDeleteItem.setDisable(true);
                undoButton.setDisable(true);
                redoButton.setDisable(true);
                rangeHash.setDisable(true);

                return false;
            }

            pos = siguientePosicion(originalPos, step);
            step++;
            if (step > tableSize) {
                itemsArrayText.setText("Tabla llena. No se pudo insertar " + claveStr + ".");
                return false;
            }
        }

        table[pos] = claveStr; // Almacenar la cadena original

        Arrays.fill(cellColors, "WHITE");

        marcarPosicion(pos, "YELLOW");
        scrollToPosition(pos);

        saveState(pos);

        itemsArrayText.setText("Clave " + claveStr + " insertada en la posición " + pos + ".");
        actualizarVistaArray();

        return true;
    }

    @FXML
    private void defineCollitions() {
        String selected = collisionHash.getText();
        if ("Elegir".equalsIgnoreCase(selected)) {
            itemsArrayText.setText("Debe seleccionar un método válido.");
            return;
        }

        this.collisionString = selected;
        collisionHash.setDisable(true);
        defineCollitionsButton.setDisable(true);

        insertButton.setDisable(false);
        searchButton.setDisable(false);
        deleteButton.setDisable(false);
        newItemArray.setDisable(false);
        modDeleteItem.setDisable(false);
        undoButton.setDisable(false);
        redoButton.setDisable(false);
        rangeHash.setDisable(true);

        itemsArrayText.setText(
                "Método de colisión '" + collisionString + "' definido.\nIntentando insertar clave pendiente...");

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

                // Determinar cuántos dígitos centrales tomar según el tamaño de la tabla
                int digitos = (int) Math.log10(tableSize) + 1;
                int digitosCentrales = digitos - 1; // Para 100 (2 dígitos) -> tomar 1 dígito central

                // Ajustar para tablas más grandes
                if (tableSize >= 1000)
                    digitosCentrales = digitos - 1;

                int start = (s.length() - digitosCentrales) / 2;
                if (start < 0)
                    start = 0;

                String sub = s.substring(start, Math.min(start + digitosCentrales, s.length()));
                if (sub.isEmpty())
                    return 1;

                int resultadoCuadrada = Integer.parseInt(sub) % tableSize;
                return resultadoCuadrada + 1; // Sumar 1 para evitar posición 0

            case "Plegamiento":
                String claveStr = String.valueOf(clave);
                int sum = 0;

                // Dividir en segmentos de 2 dígitos
                for (int i = 0; i < claveStr.length(); i += 2) {
                    int end = Math.min(i + 2, claveStr.length());
                    String segmento = claveStr.substring(i, end);
                    sum += Integer.parseInt(segmento);
                }

                // Tomar los últimos dígitos según el tamaño de la tabla
                int digitosPlegamiento = (int) Math.log10(tableSize) + 1;
                int divisor = (int) Math.pow(10, digitosPlegamiento - 1);
                int resultadoPlegamiento = sum % divisor;

                return (resultadoPlegamiento % tableSize) + 1; // Sumar 1 para evitar posición 0

            case "Truncamiento":
                String numStr = String.format("%0" + maxDigits + "d", clave); // Preservar ceros a la izquierda
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
                return (resultadoTruncamiento % tableSize) + 1; // Sumar 1 para evitar posición 0

            default:
                return (clave % tableSize) + 1; // Sumar 1 para evitar posición 0
        }
    }

    private int siguientePosicion(int posInicial, int step) {
        if (collisionString == null)
            return posInicial;
        int next;
        switch (collisionString) {
            case "Lineal":
                next = (posInicial + step - 1) % tableSize + 1;
                break;
            case "Cuadratica":
                next = (posInicial + step * step - 1) % tableSize + 1;
                break;
            case "Doble Hash":
                next = aplicarFuncionHash(posInicial + 1);
                break;
            default:
                return posInicial;
        }
        return next;
    }

    private void findItem(String claveStr, boolean eliminar) {
        if (table == null) {
            itemsArrayText.setText("No hay tabla creada.");
            return;
        }

        int clave = Integer.parseInt(claveStr); // Convertir a int para búsqueda

        Arrays.fill(cellColors, "WHITE");
        miViewList.refresh();

        long inicio = System.nanoTime();

        int pos = aplicarFuncionHash(clave);
        int originalPos = pos;
        int step = 1;

        java.util.List<Integer> recorrido = new java.util.ArrayList<>();

        while (table[pos] != null) {
            recorrido.add(pos);

            if (Integer.parseInt(table[pos]) == clave) { // Comparar como enteros
                long fin = System.nanoTime();
                long nanos = fin - inicio;

                String tiempo;
                if (nanos < 1_000_000) {
                    tiempo = nanos + " ns";
                } else {
                    tiempo = String.format("%.4f ms", nanos / 1_000_000.0);
                }

                if (eliminar) {
                    table[pos] = null;
                    saveState(pos);
                    actualizarVistaArray();
                    itemsArrayText.setText("Clave " + claveStr + " eliminada en pos " + pos +
                            " en " + tiempo + ".");
                } else {
                    itemsArrayText.setText("Clave " + claveStr + " encontrada en pos " + pos +
                            " en " + tiempo + ".");
                }

                animateSearch(recorrido, true, pos, eliminar);
                return;
            }

            if (collisionString == null)
                break;

            pos = siguientePosicion(originalPos, step);
            step++;
            if (step > tableSize)
                break;
        }

        long fin = System.nanoTime();
        long nanos = fin - inicio;

        String tiempo;
        if (nanos < 1_000_000) {
            tiempo = nanos + " ns";
        } else {
            tiempo = String.format("%.4f ms", nanos / 1_000_000.0);
        }

        itemsArrayText.setText("Clave " + claveStr + " no encontrada tras " + recorrido.size() +
                " intentos en " + tiempo + ".");
        animateSearch(recorrido, false, -1, eliminar);
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
            itemsArrayText.setText("Ingrese una clave válida.");
            return;
        }
        if (!claveStr.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("La clave debe tener " + maxDigits + " dígitos.");
            return;
        }
        findItem(claveStr, false);
    }

    @FXML
    private void eliminateItem() {
        String claveStr = modDeleteItem.getText();
        if (claveStr.isEmpty()) {
            itemsArrayText.setText("Ingrese una clave válida.");
            return;
        }
        if (!claveStr.matches("\\d{" + maxDigits + "}")) {
            itemsArrayText.setText("La clave debe tener " + maxDigits + " dígitos.");
            return;
        }
        findItem(claveStr, true);
        modDeleteItem.clear();
    }

    private void saveState(int lastModifiedPosition) {
        redoStack.clear();
        undoStack.push(new ActionState(
                table, tableSize, maxDigits, hashString,
                collisionString, truncPositions, truncPositionsSet, lastModifiedPosition));
        updateUndoRedoButtons();
    }

    @FXML
    private void undoAction() {
        if (undoStack.size() <= 1)
            return;

        ActionState currentState = undoStack.pop();
        redoStack.push(currentState);

        ActionState previousState = undoStack.peek();
        applyState(previousState, true); // true = marcar última modificación
        updateUndoRedoButtons();
    }

    @FXML
    private void redoAction() {
        if (redoStack.isEmpty())
            return;

        ActionState nextState = redoStack.pop();
        undoStack.push(nextState);

        applyState(nextState, true); // true = marcar última modificación
        updateUndoRedoButtons();
    }

    private void applyState(ActionState state, boolean markLastModified) {
        this.table = state.getTableSnapshot() != null ? state.getTableSnapshot().clone() : null;
        this.tableSize = state.getTableSizeSnapshot();
        this.maxDigits = state.getMaxDigitsSnapshot();
        this.hashString = state.getHashStringSnapshot();
        this.collisionString = state.getCollisionMethodSnapshot();
        this.truncPositions = state.getTruncPositionsSnapshot() != null
                ? state.getTruncPositionsSnapshot().clone()
                : null;
        this.truncPositionsSet = state.getTruncPositionsSetSnapshot();

        // Inicializar colores en blanco
        if (cellColors == null || cellColors.length != tableSize + 1) {
            cellColors = new String[tableSize + 1];
        }
        Arrays.fill(cellColors, "WHITE");

        // Solo marcar la última posición modificada si se solicita
        int lastPos = state.getLastModifiedPosition();
        if (markLastModified && lastPos != -1) {
            marcarPosicion(lastPos, "GRAY");
            scrollToPosition(lastPos);
        }

        handleUIState(state);
        actualizarVistaArray();

        // Actualizar estado del botón de guardar
        saveButton.setDisable(table == null);
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

        table = null;
        arrayLengthText.setText("Array sin crear");
        itemsArrayText.setText("No hay elementos en el array");
        miViewList.getItems().clear();

        collisionString = null;
        collisionHash.setText("Elegir");
        pendingKey = null;

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
            String valor = (table[i] == null) ? "-" : table[i]; // Mostrar la cadena original
            miViewList.getItems().add("Pos " + i + ": " + valor);
            if (cellColors != null && i < cellColors.length && cellColors[i] == null) {
                cellColors[i] = "WHITE";
            }
        }
        miViewList.refresh();
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
            arrayLengthText.setText("El número de dígitos debe ser mayor a 2 para usar truncamiento.");
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
            arrayLengthText.setText("Posiciones seleccionadas: " + Arrays.toString(truncPositions) +
                    ". Restantes: " + (truncMaxSelections - truncPositions.length));
        }

        if (createButton.isDisable()) {
            truncButton.setDisable(true);
            truncElegir.setDisable(true);
        }
    }

    @FXML
    private void elegirTrunc() {
        if (!"Truncamiento".equals(hashString) || truncMaxSelections <= 0) {
            arrayLengthText.setText("El truncamiento no está disponible. Elige un número de dígitos mayor a 2.");
            return;
        }

        Integer position = truncElegir.getValue();
        if (position == null) {
            arrayLengthText.setText("Por favor, seleccione una posición de la lista.");
            return;
        }

        if (truncPositions == null) {
            truncPositions = new int[0];
        }

        boolean alreadySelected = false;
        for (int pos : truncPositions) {
            if (pos == position) {
                alreadySelected = true;
                break;
            }
        }
        if (alreadySelected) {
            arrayLengthText.setText("La posición " + position + " ya fue seleccionada.");
            return;
        }

        if (truncPositions.length >= truncMaxSelections) {
            arrayLengthText.setText("Ya ha seleccionado el máximo de " + truncMaxSelections + " posiciones.");
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
                        alert.setHeaderText("Función hash incompatible");
                        alert.setContentText("El archivo fue guardado con la función hash: " + savedHashFunction +
                                "\nPero actualmente está seleccionada: " + this.hashString +
                                "\n\nSeleccione la función hash correcta antes de cargar el archivo.");
                        alert.showAndWait();

                        itemsArrayText.setText("Error: Función hash incompatible");
                        return;
                    }

                    // Reiniciar método de colisión
                    collisionString = null;
                    collisionHash.setText("Elegir");
                    pendingKey = null;

                    undoStack.clear();
                    redoStack.clear();
                    undoStack.push(loadedState);

                    applyState(loadedState, false);
                    updateUndoRedoButtons();

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
                alert.setContentText("El archivo seleccionado no es válido o está corrupto: " + e.getMessage());
                alert.showAndWait();

                itemsArrayText.setText("Archivo no valido o corrupto");
                e.printStackTrace();
            }
        }
    }
}