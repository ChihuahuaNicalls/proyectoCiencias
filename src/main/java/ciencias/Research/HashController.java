package ciencias.Research;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import ciencias.ResearchController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;

import java.util.Arrays;

public class HashController {
    @FXML
    private TextField newItemArray;
    @FXML
    private TextField modDeleteItem;
    @FXML
    private TextField modItem;

    @FXML
    private Label titleHash;
    @FXML
    private Label functionHash;

    @FXML
    private Text truncText;
    @FXML
    private Text arrayLengthText;
    @FXML
    private Text itemsArrayText;
    @FXML
    private Text modText;
    @FXML
    private Text modOpText;

    @FXML
    private Button truncButton;
    @FXML
    private Button insertButton;
    @FXML
    private Button reiniciarButton;
    @FXML
    private Button createButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button modifyButton;
    @FXML
    private Button deleteButton;

    @FXML
    private ComboBox<Integer> truncElegir;

    @FXML
    private MenuButton collisionHash;
    @FXML
    private MenuButton rangeHash;

    @FXML
    private Tab tabEliminateMod;
    @FXML
    private Tab tabView;

    @FXML
    private ListView<String> miViewList;

    @FXML
    private TabPane tabPane;

    private ResearchController researchController;

    private int[] table;
    private int[] truncPositions;

    private String rangeString;
    private String collisionString;
    private String hashString;

    private int primeSize;
    private int tableSize;
    private int truncMaxSelections = 0;
    private int rangeInt = 0;

    private boolean arrayReady;
    private boolean truncPositionsSet;

    @FXML
    private void initialize() {
        newItemArray.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        modDeleteItem.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        modItem.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

        for (MenuItem item : collisionHash.getItems()) {
            item.setOnAction(event -> collisionHash.setText(item.getText()));
        }
        for (MenuItem item : rangeHash.getItems()) {
            item.setOnAction(event -> {
                rangeHash.setText(item.getText());
                rangeString = item.getText();
                if ("Truncamiento".equals(hashString))
                    enableTrunc();
            });
        }

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
                functionHash.setText("h(k) = k mod m");
                break;
            case "Cuadrada":
                titleHash.setText("Funcion de Hash: Cuadrada");
                functionHash.setText("h(k) = dig_cen(k^2)");
                break;
            case "Truncamiento":
                titleHash.setText("Funcion de Hash: Truncamiento");
                functionHash.setText("h(k) = elegir_dig(k)");
                truncText.setVisible(true);
                truncButton.setVisible(true);
                truncElegir.setVisible(true);
                truncPositionsSet = false;
                break;
            case "Plegamiento":
                titleHash.setText("Funcion de Hash: Plegamiento");
                functionHash.setText("h(k) = suma_de_grupos");
                break;
        }
    }

    private void actualizarVistaArray() {
        if (table == null) {
            miViewList.getItems().clear();
            return;
        }

        miViewList.getItems().clear();
        for (int i = 0; i < tableSize; i++) {
            String valor = table[i] == -1 ? "-" : String.valueOf(table[i]);
            miViewList.getItems().add("Pos " + (i + 1) + ": " + valor);
        }
    }

    private int siguientePosicion(int clave, int posActual, int step) {
        switch (collisionString) {
            case "Lineal":
                return (posActual + step) % tableSize;
            case "Cuadratica":
                return (posActual + step * step) % tableSize;
            case "Doble Hash":
                return aplicarFuncionHash(posActual);
            default:
                return posActual;
        }
    }

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    @FXML
    private void crearArray() {
        collisionString = collisionHash.getText();
        rangeString = rangeHash.getText();

        if (!"Truncamiento".equals(hashString)) {
            if (!"Elegir".equalsIgnoreCase(collisionString) && !"Elegir".equalsIgnoreCase(rangeString)) {
                iniciarTabla();
            } else {
                arrayLengthText.setText("Seleccione todas las opciones para crear el array");
            }
        } else {
            if (!"Elegir".equalsIgnoreCase(collisionString) && !"Elegir".equalsIgnoreCase(rangeString)
                    && truncPositionsSet) {
                iniciarTabla();
            } else {
                arrayLengthText.setText("Seleccione todas las opciones (incluyendo truncamiento)");
            }
        }

        if (rangeString != null && !"Elegir".equalsIgnoreCase(rangeString)) {
            rangeInt = Integer.parseInt(rangeString);
        }
    }

    private int nextPrime(int n) {
        if (n <= 2)
            return 2;
        for (int i = n;; i++) {
            boolean isPrime = true;
            for (int j = 2; j * j <= i; j++) {
                if (i % j == 0) {
                    isPrime = false;
                    break;
                }
            }
            if (isPrime)
                return i;
        }
    }

    private void iniciarTabla() {
        tableSize = Integer.parseInt(rangeString);
        primeSize = nextPrime(tableSize);
        table = new int[tableSize];
        Arrays.fill(table, -1);

        collisionHash.setDisable(true);
        rangeHash.setDisable(true);
        truncElegir.setDisable(true);
        createButton.setDisable(true);

        reiniciarButton.setDisable(false);
        insertButton.setDisable(false);
        newItemArray.setDisable(false);
        tabEliminateMod.setDisable(false);
        tabView.setDisable(false);

        arrayReady = true;

        StringBuilder info = new StringBuilder("Array de tamaño " + tableSize + " creado\n");
        info.append("Metodo de colision: ").append(collisionString).append("\n");
        if ("Truncamiento".equals(hashString)) {
            info.append("Posiciones truncadas: ").append(Arrays.toString(truncPositions));
        }
        arrayLengthText.setText(info.toString());
        itemsArrayText.setText("No hay elementos en el array");
    }

    @FXML
    private void addToArray() {
        if (!arrayReady) {
            itemsArrayText.setText("Primero cree el array.");
            return;
        }

        try {
            int clave = Integer.parseInt(newItemArray.getText());
            if (clave < 0 || clave >= rangeInt) {
                itemsArrayText.setText("La clave debe estar entre 0 y " + (rangeInt - 1));
                return;
            }

            long start = System.nanoTime();

            int pos = aplicarFuncionHash(clave);
            int originalPos = pos;
            int step = 1;

            while (table[pos] != -1) {
                pos = siguientePosicion(clave, pos, step);
                step++;
                if (step > tableSize) {
                    itemsArrayText.setText("No hay espacio para insertar la clave: " + clave);
                    return;
                }
            }

            table[pos] = clave;

            long end = System.nanoTime();
            double ms = (end - start) / 1e6;

            itemsArrayText.setText("Clave " + clave + " insertada en posicion " + (pos + 1) +
                    "\nTiempo: " + String.format("%.3f", ms) + " ms");

            actualizarVistaArray();
            newItemArray.clear();

        } catch (NumberFormatException e) {
            itemsArrayText.setText("Ingrese un numero valido.");
        }
    }

    private int aplicarFuncionHash(int clave) {
        switch (hashString) {
            case "Modulo":
                return clave % primeSize;
            case "Cuadrada":
                int sq = clave * clave;
                String s = String.valueOf(sq);
                int mid = s.length() / 2;
                return Character.getNumericValue(s.charAt(mid)) % tableSize;
            case "Truncamiento":
                String num = String.valueOf(clave);
                StringBuilder trunc = new StringBuilder();
                if (truncPositions != null) {
                    for (int pos : truncPositions) {
                        int idx = pos - 1; // truncPositions son 1-based
                        if (idx >= 0 && idx < num.length()) {
                            trunc.append(num.charAt(idx));
                        }
                    }
                }
                if (trunc.length() == 0)
                    return 0;
                return Integer.parseInt(trunc.toString()) % tableSize;
            case "Plegamiento":
                String claveStr = String.valueOf(clave);
                int sum = 0;
                for (char c : claveStr.toCharArray())
                    sum += Character.getNumericValue(c);
                return sum % tableSize;
            default:
                return clave % tableSize;
        }
    }

    @FXML
    private void reiniciar() {
        table = null;
        tableSize = 0;
        rangeInt = 0;
        arrayReady = false;
        collisionString = null;
        rangeString = null;
        truncPositions = null;
        truncPositionsSet = false;

        collisionHash.setDisable(false);
        collisionHash.setText("Elegir");
        rangeHash.setDisable(false);
        rangeHash.setText("Elegir");
        truncElegir.setDisable(true);
        truncElegir.setVisible(false);
        createButton.setDisable(false);

        insertButton.setDisable(true);
        newItemArray.setDisable(true);
        reiniciarButton.setDisable(true);

        arrayLengthText.setText("Array sin crear");
        itemsArrayText.setText("No hay elementos en el array");
    }

    @FXML
    private void enableTrunc() {
        if (!"Truncamiento".equals(hashString))
            return;

        truncText.setVisible(true);
        truncText.setDisable(false);
        truncButton.setDisable(false);
        truncElegir.setVisible(true);
        truncElegir.setDisable(false);
        truncElegir.getItems().clear();

        int digits = 0;
        try {
            int maxValue = Math.max(0, Integer.parseInt(rangeString) - 1);
            digits = String.valueOf(maxValue).length();
        } catch (NumberFormatException e) {
            arrayLengthText.setText("Rango invalido para truncamiento.");
            truncButton.setDisable(true);
            truncElegir.setDisable(true);
            truncMaxSelections = 0;
            return;
        }

        for (int i = 1; i <= digits; i++)
            truncElegir.getItems().add(i);

        truncMaxSelections = Math.max(0, digits - 2);

        if (truncMaxSelections <= 0) {
            truncButton.setDisable(true);
            arrayLengthText.setText("No hay suficientes digitos para aplicar truncamiento.");
            truncPositions = null;
            truncPositionsSet = false;
        } else {
            truncButton.setDisable(false);
            arrayLengthText.setText("Seleccione exactamente " + truncMaxSelections +
                    " posiciones para truncamiento (entre 1 y " + digits + ").");
            truncPositions = null;
            truncPositionsSet = false;
        }
    }

    @FXML
    private void elegirTrunc() {
        if (truncMaxSelections <= 0) {
            arrayLengthText.setText("No se pueden seleccionar posiciones para truncamiento en este rango.");
            return;
        }

        Integer position = truncElegir.getValue();
        if (position == null) {
            arrayLengthText.setText("Seleccione una posicion para agregar");
            return;
        }

        if (truncPositions != null) {
            for (int pos : truncPositions) {
                if (pos == position) {
                    arrayLengthText.setText("La posicion " + position + " ya fue seleccionada");
                    return;
                }
            }
        }

        int current = truncPositions == null ? 0 : truncPositions.length;
        if (current >= truncMaxSelections) {
            arrayLengthText.setText("Solo puede seleccionar exactamente " + truncMaxSelections + " posiciones.");
            return;
        }

        if (truncPositions == null) {
            truncPositions = new int[] { position };
        } else {
            int[] newTruncPositions = Arrays.copyOf(truncPositions, truncPositions.length + 1);
            newTruncPositions[truncPositions.length] = position;
            truncPositions = newTruncPositions;
        }

        truncPositionsSet = (truncPositions.length == truncMaxSelections);

        if (!truncPositionsSet) {
            arrayLengthText.setText("Posiciones seleccionadas: " + Arrays.toString(truncPositions) +
                    " (faltan " + (truncMaxSelections - truncPositions.length) + ")");
        } else {
            arrayLengthText.setText("Posiciones seleccionadas: " + Arrays.toString(truncPositions) +
                    " (maximo alcanzado). Ahora puede crear el array.");
        }
    }

    @FXML
    private void searchItem() {
        try {
            int clave = Integer.parseInt(modDeleteItem.getText());
            long start = System.nanoTime();

            int pos = aplicarFuncionHash(clave);
            int step = 1;

            while (table[pos] != -1) {
                if (table[pos] == clave) {
                    long end = System.nanoTime();
                    double ms = (end - start) / 1e6;

                    modText.setText("Clave " + clave + " encontrada en posicion " + (pos + 1));
                    modOpText.setText("Tiempo: " + String.format("%.3f", ms) + " ms");

                    modItem.setDisable(false);
                    modifyButton.setDisable(false);
                    deleteButton.setDisable(false);

                    modDeleteItem.setDisable(true);
                    searchButton.setDisable(true);
                    return;
                }

                pos = siguientePosicion(clave, pos, step);
                step++;
                if (step > tableSize)
                    break;
            }

            long end = System.nanoTime();
            double ms = (end - start) / 1e6;

            modText.setText("No se encuentra el elemento");
            modOpText.setText("Tiempo: " + String.format("%.3f", ms) + " ms");

            modItem.setDisable(true);
            modifyButton.setDisable(true);
            deleteButton.setDisable(true);

        } catch (NumberFormatException e) {
            modText.setText("Ingrese un numero valido.");
        }
    }

    @FXML
    private void modifyItem() {
        try {
            int oldClave = Integer.parseInt(modDeleteItem.getText());
            int newClave = Integer.parseInt(modItem.getText());

            eliminateItem();

            newItemArray.setText(String.valueOf(newClave));
            addToArray();
            actualizarVistaArray();

            modText.setText("Clave modificada: " + oldClave + " → " + newClave);
            modOpText.setText("Modificacion exitosa");

            modDeleteItem.clear();
            modDeleteItem.setDisable(false);
            searchButton.setDisable(false);

            modItem.clear();
            modItem.setDisable(true);
            modifyButton.setDisable(true);
            deleteButton.setDisable(true);

            newItemArray.clear();

        } catch (NumberFormatException e) {
            modText.setText("Ingrese un numero valido.");
        }
    }

    @FXML
    private void eliminateItem() {
        int clave = Integer.parseInt(modDeleteItem.getText());

        int pos = aplicarFuncionHash(clave);
        int step = 1;

        while (table[pos] != -1) {
            if (table[pos] == clave) {
                table[pos] = -1;
                actualizarVistaArray();
                modText.setText("Clave eliminada: " + clave);
                modOpText.setText("Posicion: " + (pos + 1));

                modDeleteItem.clear();
                modDeleteItem.setDisable(false);
                searchButton.setDisable(false);
                modItem.clear();
                modItem.setDisable(true);
                modifyButton.setDisable(true);
                deleteButton.setDisable(true);

                return;
            }

            pos = siguientePosicion(clave, pos, step);
            step++;
            if (step > tableSize)
                break;
        }

        modText.setText("No se encontro la clave para eliminar");
        modOpText.setText("");
    }

    @FXML
    private void saveArray() {
        if (table == null) {
            itemsArrayText.setText("No hay array para guardar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Array");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de texto", "*.txt"));
        File file = fileChooser.showSaveDialog(tabPane.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(tableSize + "," + primeSize);
                writer.newLine();
                for (int valor : table) {
                    writer.write(String.valueOf(valor));
                    writer.newLine();
                }
                itemsArrayText.setText("Array guardado en: " + file.getAbsolutePath());
            } catch (IOException e) {
                itemsArrayText.setText("Error al guardar el archivo: " + e.getMessage());
            }
        }
    }

    @FXML
    private void loadArray() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Array");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de texto", "*.txt"));
        File file = fileChooser.showOpenDialog(tabPane.getScene().getWindow());

        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String[] firstLine = reader.readLine().split(",");
                tableSize = Integer.parseInt(firstLine[0]);
                primeSize = Integer.parseInt(firstLine[1]);

                table = new int[tableSize];
                for (int i = 0; i < tableSize; i++) {
                    table[i] = Integer.parseInt(reader.readLine());
                }

                collisionHash.setDisable(true);
                rangeHash.setDisable(true);
                truncElegir.setDisable(true);
                createButton.setDisable(true);

                reiniciarButton.setDisable(false);
                insertButton.setDisable(false);
                newItemArray.setDisable(false);
                tabEliminateMod.setDisable(false);
                tabView.setDisable(false);

                arrayReady = true;
                actualizarVistaArray();
                itemsArrayText.setText("Array cargado desde: " + file.getAbsolutePath());
            } catch (IOException | NumberFormatException e) {
                itemsArrayText.setText("Error al cargar el archivo: " + e.getMessage());
            }
        }
    }
}
