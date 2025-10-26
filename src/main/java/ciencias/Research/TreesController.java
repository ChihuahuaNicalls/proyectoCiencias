package ciencias.Research;

import ciencias.ResearchController;
import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.scene.shape.Rectangle;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TreesController {
    @FXML
    private Label treesTitle;
    @FXML
    private Label treesDescription;
    @FXML
    private TextField newItemTree;
    @FXML
    private TextField modDeleteItem;
    @FXML
    private Button insertButton;
    @FXML
    private Button reiniciarButton;
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
    private Text treeLengthText;
    @FXML
    private Text itemsTreeText;
    @FXML
    private Text notificationText;
    @FXML
    private ScrollPane treePane;
    @FXML
    private Pane principalPane;
    @FXML
    private Label keyLabel;

    private String treeString;
    private ResearchController researchController;

    private DigitalNode digitalRoot;
    private ResidueNode residueRoot;
    private MultipleResidueNode multipleResidueRoot;
    private HuffmanNode huffmanRoot;

    private Deque<TreeState> undoStack = new ArrayDeque<>();
    private Deque<TreeState> redoStack = new ArrayDeque<>();

    private Map<Character, String> huffmanCodes = new HashMap<>();
    private String huffmanMessage = "";

    private double currentScale = 1.0;
    private final double SCALE_DELTA = 0.1;
    private final double MAX_SCALE = 3.0;
    private final double MIN_SCALE = 0.5;

    private Timeline animationTimeline;
    private final int ANIMATION_DELAY = 500;

    private double canvasWidth = 1000;
    private double canvasHeight = 700;
    private final double BASE_NODE_WIDTH = 50;
    private final double BASE_NODE_HEIGHT = 80;

    private List<Character> digitalInsertionOrder = new ArrayList<>();
    private List<Character> residueInsertionOrder = new ArrayList<>();
    private List<Character> multipleResidueInsertionOrder = new ArrayList<>();

    private List<Object> currentSearchPath = new ArrayList<>();
    private boolean isSearchInProgress = false;

    private static class TreeState implements Serializable {
        private static final long serialVersionUID = 1L;
        List<Character> digitalOrder;
        List<Character> residueOrder;
        List<Character> multipleResidueOrder;
        String huffmanMessage;

        TreeState(List<Character> digitalOrder, List<Character> residueOrder,
                List<Character> multipleResidueOrder, String huffmanMessage) {
            this.digitalOrder = new ArrayList<>(digitalOrder);
            this.residueOrder = new ArrayList<>(residueOrder);
            this.multipleResidueOrder = new ArrayList<>(multipleResidueOrder);
            this.huffmanMessage = huffmanMessage;
        }
    }

    private class DigitalNode implements Serializable {
        private static final long serialVersionUID = 1L;
        Character letter;
        DigitalNode left;
        DigitalNode right;
        List<Integer> path;
        transient double x, y;
        transient Circle circle;

        DigitalNode(Character letter) {
            this.letter = letter;
            this.left = null;
            this.right = null;
            this.path = new ArrayList<>();
        }
    }

    private class ResidueNode implements Serializable {
        private static final long serialVersionUID = 1L;
        boolean isLink;
        Character letter;
        ResidueNode left;
        ResidueNode right;
        List<Integer> path;
        transient double x, y;
        transient Circle circle;

        ResidueNode(boolean isLink, Character letter) {
            this.isLink = isLink;
            this.letter = letter;
            this.left = null;
            this.right = null;
            this.path = new ArrayList<>();
        }
    }

    private class MultipleResidueNode implements Serializable {
        private static final long serialVersionUID = 1L;
        boolean isLink;
        Character letter;
        MultipleResidueNode[] children;
        List<Integer> path;
        transient double x, y;
        transient Circle circle;

        MultipleResidueNode(boolean isLink, Character letter) {
            this.isLink = isLink;
            this.letter = letter;
            this.children = new MultipleResidueNode[4];
            this.path = new ArrayList<>();
        }
    }

    private class HuffmanNode implements Serializable, Comparable<HuffmanNode> {
        private static final long serialVersionUID = 1L;
        Character letter;
        double frequency;
        HuffmanNode left;
        HuffmanNode right;
        transient List<Integer> path;
        transient double x, y;
        transient Circle circle;
        int insertionOrder;
        String nodeType;

        private int numerator;
        private int denominator;

        HuffmanNode(Character letter, double frequency, int insertionOrder) {
            this.letter = letter;
            this.frequency = frequency;
            this.insertionOrder = insertionOrder;
            this.left = null;
            this.right = null;
            this.path = new ArrayList<>();
            this.nodeType = "leaf";

            double[] fraction = decimalToFraction(frequency);
            this.numerator = (int) fraction[0];
            this.denominator = (int) fraction[1];
        }

        HuffmanNode(HuffmanNode left, HuffmanNode right, double frequency, int insertionOrder) {
            this.letter = null;
            this.frequency = frequency;
            this.insertionOrder = insertionOrder;
            this.left = left;
            this.right = right;
            this.path = new ArrayList<>();
            this.nodeType = "link";

            double[] fraction = decimalToFraction(frequency);
            this.numerator = (int) fraction[0];
            this.denominator = (int) fraction[1];
        }

        @Override
        public int compareTo(HuffmanNode other) {
            int freqCompare = Double.compare(this.frequency, other.frequency);
            if (freqCompare != 0) {
                return freqCompare;
            }
            return Integer.compare(other.insertionOrder, this.insertionOrder);
        }

        public boolean isLeaf() {
            return "leaf".equals(nodeType);
        }

        @Override
        public String toString() {
            if (isLeaf()) {
                return letter + ":" + getFractionString();
            }
            return getFractionString();
        }

        public String getDisplayText() {
            if (isLeaf()) {
                return String.valueOf(letter);
            }
            return getFractionString();
        }

        public String getFractionString() {
            if (denominator == 1) {
                return String.valueOf(numerator);
            }
            return numerator + "/" + denominator;
        }

        public int getNumerator() {
            return numerator;
        }

        public int getDenominator() {
            return denominator;
        }
    }

    @FXML
    private void initialize() {
        newItemTree.setDisable(false);
        insertButton.setDisable(false);
        modDeleteItem.setDisable(false);
        searchButton.setDisable(false);
        deleteButton.setDisable(false);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);
        loadButton.setDisable(false);
        reiniciarButton.setDisable(false);
        setupZoomAndScroll();
        setupKeyboardPan();

        PauseTransition initialFocus = new PauseTransition(Duration.millis(300));
        initialFocus.setOnFinished(e -> focusOnRootWithDelay());
        initialFocus.play();
    }

    private void setupTextFieldRestrictions() {

        newItemTree.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                newItemTree.setText(oldValue);
            }
        });
    }

    private void initializeMultipleResidueTree() {
        if (multipleResidueRoot == null) {
            multipleResidueRoot = new MultipleResidueNode(true, null);
        }

        buildMultipleResidueLinkStructure(multipleResidueRoot, 0, 2);
    }

    private void buildMultipleResidueLinkStructure(MultipleResidueNode node, int currentDepth, int maxDepth) {
        if (currentDepth >= maxDepth) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (node.children[i] == null) {
                node.children[i] = new MultipleResidueNode(true, null);
            }

            buildMultipleResidueLinkStructure(node.children[i], currentDepth + 1, maxDepth);
        }
    }

    public void initData() {
        if (researchController == null)
            return;
        treeString = researchController.getTreesString();

        digitalInsertionOrder.clear();
        residueInsertionOrder.clear();
        multipleResidueInsertionOrder.clear();

        configureTextFieldForTreeType();

        switch (treeString) {
            case "Arboles de busqueda digital":
                keyLabel.setText("Insertar clave");
                treesTitle.setText("Arboles de busqueda digital");
                treesDescription.setText(
                        "Todos los bits de la clave se representan explicitamente en el recorrido, aunque haya caminos con nodos de un solo hijo.");
                digitalRoot = null;
                break;
            case "Arboles de busqueda por residuos":
                keyLabel.setText("Insertar clave");
                treesTitle.setText("Arboles de busqueda por residuos");
                treesDescription.setText(
                        "Si varias claves comparten prefijo el nodo se vuelve de enlace y se bifurca, mientras que los nodos hoja.");
                residueRoot = new ResidueNode(true, null);
                break;
            case "Arboles de busqueda por residuos multiple":
                keyLabel.setText("Insertar clave");
                treesTitle.setText("Arboles de busqueda por residuos multiple");
                treesDescription.setText(
                        "En cada nivel se toman varios bits de la clave a la vez: el nodo se bifurca en tantas ramas como combinaciones de esos bits.");

                multipleResidueRoot = new MultipleResidueNode(true, null);
                break;
            case "Tablas de indices":
                keyLabel.setText("Insertar clave");
                treesTitle.setText("Tablas de indices");
                treesDescription.setText(".");
                break;
            case "Arboles de Huffman":
                keyLabel.setText("Insertar mensaje");
                treesTitle.setText("Arboles de Huffman");
                treesDescription.setText(
                        "Es construido a partir de las frecuencias de aparicion de los simbolos donde se toman siempre los dos nodos de menor frecuencia.");
                huffmanRoot = null;
                undoButton.setVisible(false);
                redoButton.setVisible(false);
                break;
            default:
                break;
        }

        updateTreeVisualization();
        saveState();
        updateItemsText();

        focusOnRootWithDelay();
    }

    private void configureTextFieldForTreeType() {
        if (treeString.equals("Arboles de Huffman")) {
            newItemTree.textProperty().addListener((observable, oldValue, newValue) -> {
            });
        } else {
            newItemTree.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 1) {
                    newItemTree.setText(oldValue);
                }
            });
        }
    }

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    private void updateItemsText() {
        StringBuilder sb = new StringBuilder();

        switch (treeString) {
            case "Arboles de busqueda digital":
                sb.append("Digital: ");
                for (int i = 0; i < digitalInsertionOrder.size(); i++) {
                    Character c = digitalInsertionOrder.get(i);
                    String binary = letterToBinary(c);
                    sb.append(c).append("(").append(binary).append(")");
                    if (i < digitalInsertionOrder.size() - 1) {
                        sb.append(", ");
                    }
                }
                treeLengthText.setText("Numero de claves: " + digitalInsertionOrder.size());
                break;

            case "Arboles de busqueda por residuos":
                sb.append("Residuos: ");
                for (int i = 0; i < residueInsertionOrder.size(); i++) {
                    Character c = residueInsertionOrder.get(i);
                    String binary = letterToBinary(c);
                    sb.append(c).append("(").append(binary).append(")");
                    if (i < residueInsertionOrder.size() - 1) {
                        sb.append(", ");
                    }
                }
                treeLengthText.setText("Numero de claves: " + residueInsertionOrder.size());
                break;

            case "Arboles de busqueda por residuos multiple":
                sb.append("Residuos Multiples: ");
                for (int i = 0; i < multipleResidueInsertionOrder.size(); i++) {
                    Character c = multipleResidueInsertionOrder.get(i);
                    String binary = letterToBinary(c);
                    sb.append(c).append("(").append(binary).append(")");
                    if (i < multipleResidueInsertionOrder.size() - 1) {
                        sb.append(", ");
                    }
                }
                treeLengthText.setText("Numero de claves: " + multipleResidueInsertionOrder.size());
                break;

            case "Arboles de Huffman":
                if (huffmanRoot == null) {
                    sb.append("Huffman - Ingrese un mensaje para construir el arbol");
                } else {
                    sb.append("Huffman Mensaje: ").append(huffmanMessage);
                    sb.append("\nEstructura: ").append(getHuffmanTreeStructure(huffmanRoot));
                    sb.append("\nFrecuencias: ");
                    Map<Character, Double> freqMap = calculateFrequencies(huffmanMessage);
                    for (Map.Entry<Character, Double> entry : freqMap.entrySet()) {
                        double[] fraction = decimalToFraction(entry.getValue());
                        sb.append(entry.getKey()).append(": ").append((int) fraction[0] + "/" + (int) fraction[1])
                                .append(" ");
                    }
                }
                treeLengthText
                        .setText("Numero de caracteres: " + (huffmanMessage != null ? huffmanMessage.length() : 0));
                break;

            default:
                sb.append("Seleccione un tipo de arbol");
                break;
        }

        itemsTreeText.setText(sb.toString());
    }

    private Map<Character, Double> calculateFrequencies(String message) {
        Map<Character, Double> frequencies = new HashMap<>();
        int totalChars = message.length();

        for (char c : message.toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0.0) + 1.0 / totalChars);
        }

        return frequencies;
    }

    private double[] decimalToFraction(double decimal) {
        double tolerance = 1.0E-6;
        double numerator = 1;
        double denominator = 1;
        double error = decimal;

        for (double d = 1; d <= 1000; d++) {
            double n = Math.round(decimal * d);
            double currentError = Math.abs(decimal - n / d);

            if (currentError < error && currentError < tolerance) {
                numerator = n;
                denominator = d;
                error = currentError;
            }
        }

        int gcd = findGCD((int) numerator, (int) denominator);
        return new double[] { numerator / gcd, denominator / gcd };
    }

    private int findGCD(int a, int b) {
        if (b == 0) {
            return a;
        }
        return findGCD(b, a % b);
    }

    private void rebuildDigitalTree() {
        digitalRoot = null;
        for (Character letter : digitalInsertionOrder) {
            insertDigitalSimple(letter);
        }
    }

    private void rebuildResidueTree() {
        residueRoot = new ResidueNode(true, null);
        for (Character letter : residueInsertionOrder) {
            insertResidueSimple(letter);
        }
    }

    private void rebuildMultipleResidueTree() {
        multipleResidueRoot = new MultipleResidueNode(true, null);
        for (Character letter : multipleResidueInsertionOrder) {
            insertMultipleResidueSimple(letter);
        }
    }

    private boolean insertDigitalSimple(Character letter) {
        String binary = letterToBinary(letter);
        if (binary == null)
            return false;

        if (digitalRoot == null) {
            digitalRoot = new DigitalNode(letter);
            return true;
        }

        DigitalNode current = digitalRoot;
        int bitIndex = 0;

        while (true) {
            if (current.letter != null && current.letter.equals(letter)) {
                return false;
            }

            if (bitIndex >= binary.length()) {
                if (current.left == null) {
                    current.left = new DigitalNode(letter);
                    return true;
                }
                current = current.left;
            } else {
                char bit = binary.charAt(bitIndex);
                if (bit == '0') {
                    if (current.left == null) {
                        current.left = new DigitalNode(letter);
                        return true;
                    }
                    current = current.left;
                } else {
                    if (current.right == null) {
                        current.right = new DigitalNode(letter);
                        return true;
                    }
                    current = current.right;
                }
                bitIndex++;
            }
        }
    }

    private boolean insertResidueSimple(Character letter) {
        String binary = letterToBinary(letter);
        if (binary == null)
            return false;

        if (residueInsertionOrder.isEmpty()) {
            char firstBit = binary.charAt(0);
            if (firstBit == '0') {
                residueRoot.left = new ResidueNode(false, letter);
                return true;
            } else {
                residueRoot.right = new ResidueNode(false, letter);
                return true;
            }
        }

        return insertResidueDigital(residueRoot, letter, binary, 0);
    }

    private boolean insertResidueDigital(ResidueNode node, Character letter, String binary, int bitIndex) {
        if (bitIndex >= binary.length()) {
            return false;
        }

        char currentBit = binary.charAt(bitIndex);

        if (currentBit == '0') {
            if (node.left == null) {
                node.left = new ResidueNode(false, letter);
                return true;
            } else if (!node.left.isLink && node.left.letter != null) {
                Character existingLetter = node.left.letter;
                String existingBinary = letterToBinary(existingLetter);

                node.left.isLink = true;
                node.left.letter = null;

                insertResidueDigital(node.left, existingLetter, existingBinary, bitIndex + 1);

                return insertResidueDigital(node.left, letter, binary, bitIndex + 1);
            } else {
                return insertResidueDigital(node.left, letter, binary, bitIndex + 1);
            }
        } else {
            if (node.right == null) {
                node.right = new ResidueNode(false, letter);
                return true;
            } else if (!node.right.isLink && node.right.letter != null) {
                Character existingLetter = node.right.letter;
                String existingBinary = letterToBinary(existingLetter);

                node.right.isLink = true;
                node.right.letter = null;

                insertResidueDigital(node.right, existingLetter, existingBinary, bitIndex + 1);

                return insertResidueDigital(node.right, letter, binary, bitIndex + 1);
            } else {
                return insertResidueDigital(node.right, letter, binary, bitIndex + 1);
            }
        }
    }

    private boolean insertMultipleResidueSimple(Character letter) {
        String binary = letterToBinary(letter);
        if (binary == null)
            return false;

        if (multipleResidueRoot == null) {
            multipleResidueRoot = new MultipleResidueNode(true, null);
        }

        return insertMultipleResidueData(multipleResidueRoot, letter, binary, 0);
    }

    private boolean insertMultipleResidueData(MultipleResidueNode node, Character letter, String binary, int index) {
        if (index >= binary.length()) {

            if (node.isLink && node.letter == null) {

                node.isLink = false;
                node.letter = letter;
                return true;
            } else if (node.letter != null && node.letter.equals(letter)) {

                return false;
            }

            return false;
        }

        int bitsToTake;
        if (index + 2 <= binary.length()) {
            bitsToTake = 2;
        } else {
            bitsToTake = 1;
        }

        String bits = binary.substring(index, index + bitsToTake);
        int childIndex = Integer.parseInt(bits, 2);

        if (bitsToTake == 1) {
            childIndex = bits.equals("0") ? 0 : 1;
        }

        if (node.children[childIndex] == null) {
            node.children[childIndex] = new MultipleResidueNode(true, null);
        }

        return insertMultipleResidueData(node.children[childIndex], letter, binary, index + bitsToTake);
    }

    @FXML
    private void insertTree() {
        clearSearchHighlight();

        if (treeString.equals("Arboles de Huffman")) {
            String message = newItemTree.getText();
            if (message == null || message.isEmpty()) {
                notificationText.setText("Para Huffman, ingrese un mensaje.");
                return;
            }

            buildHuffmanTree(message);
            updateItemsText();
            saveState();
            updateTreeVisualization();

            if (saveButton.isDisabled()) {
                saveButton.setDisable(false);
            }

            newItemTree.clear();
            return;
        }

        Character letter = validateInput(newItemTree.getText());
        if (letter == null) {
            notificationText.setText("Entrada invalida. Solo letras de la A a la Z.");
            return;
        }

        boolean inserted = false;

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (digitalInsertionOrder.contains(letter)) {
                    notificationText.setText("La letra '" + letter + "' ya existe en el arbol.");
                    newItemTree.clear();
                    return;
                }
                digitalInsertionOrder.add(letter);
                rebuildDigitalTree();
                inserted = true;
                animateInsertion(letter, "digital");
                break;

            case "Arboles de busqueda por residuos":
                if (residueInsertionOrder.contains(letter)) {
                    notificationText.setText("La letra '" + letter + "' ya existe en el arbol.");
                    newItemTree.clear();
                    return;
                }
                residueInsertionOrder.add(letter);
                rebuildResidueTree();
                inserted = true;
                animateInsertion(letter, "residue");
                break;

            case "Arboles de busqueda por residuos multiple":
                if (multipleResidueInsertionOrder.contains(letter)) {
                    notificationText.setText("La letra '" + letter + "' ya existe en el arbol.");
                    newItemTree.clear();
                    return;
                }

                inserted = insertMultipleResidueSimple(letter);
                if (inserted) {
                    multipleResidueInsertionOrder.add(letter);
                    animateInsertion(letter, "multiple");
                } else {
                    notificationText.setText("No se pudo insertar la letra '" + letter + "'.");
                    newItemTree.clear();
                    return;
                }
                break;
        }

        if (inserted) {
            updateItemsText();
            saveState();
            updateTreeVisualization();

            if (saveButton.isDisabled()) {
                saveButton.setDisable(false);
            }

            searchButton.setDisable(false);
            deleteButton.setDisable(false);
            modDeleteItem.setDisable(false);
        }

        newItemTree.clear();
    }

    @FXML
    private void searchTree() {
        clearSearchHighlight();

        if (treeString.equals("Arboles de Huffman")) {
            Character letter = validateInput(modDeleteItem.getText());
            if (letter == null) {
                notificationText.setText("Entrada invalida. Solo letras de la A a la Z.");
                return;
            }
            if (huffmanCodes.containsKey(letter)) {
                notificationText.setText("Letra '" + letter + "' encontrada. Codigo: " + huffmanCodes.get(letter));
                isSearchInProgress = true;
                currentSearchPath.clear();
                List<HuffmanNode> tempPath = new ArrayList<>();
                searchHuffmanPath(huffmanRoot, letter, tempPath);
                updateTreeVisualization();
                focusOnRootWithDelay();
                isSearchInProgress = false;
            } else {
                notificationText.setText("Letra '" + letter + "' no encontrada en el mensaje.");
                focusOnRootWithDelay();
            }
            return;
        }

        Character letter = validateInput(modDeleteItem.getText());
        if (letter == null) {
            notificationText.setText("Entrada invalida. Solo letras de la A a la Z.");
            return;
        }

        boolean found = false;
        List<Integer> path = new ArrayList<>();

        switch (treeString) {
            case "Arboles de busqueda digital":
                found = digitalInsertionOrder.contains(letter);
                if (found) {
                    isSearchInProgress = true;
                    currentSearchPath.clear();
                    searchDigitalWithPath(letter, path, currentSearchPath);
                    updateTreeVisualization();
                    focusOnRoot();
                    isSearchInProgress = false;
                }
                break;
            case "Arboles de busqueda por residuos":
                found = residueInsertionOrder.contains(letter);
                if (found) {
                    isSearchInProgress = true;
                    currentSearchPath.clear();
                    searchResidueWithPath(letter, path, currentSearchPath);
                    updateTreeVisualization();
                    focusOnRoot();
                    isSearchInProgress = false;
                }
                break;
            case "Arboles de busqueda por residuos multiple":
                found = multipleResidueInsertionOrder.contains(letter);
                if (found) {
                    isSearchInProgress = true;
                    currentSearchPath.clear();
                    searchMultipleResidueWithPath(letter, path, currentSearchPath);
                    updateTreeVisualization();
                    focusOnRoot();
                    isSearchInProgress = false;
                }
                break;
        }

        if (found) {
            notificationText.setText("Letra '" + letter + "' encontrada.");
        } else {
            notificationText.setText("Letra '" + letter + "' no encontrada.");
            focusOnRoot();
        }
    }

    @FXML
    private void deleteTree() {
        clearSearchHighlight();

        if (treeString.equals("Arboles de Huffman")) {
            notificationText.setText("No se puede eliminar letras de un arbol de Huffman.");
            return;
        }

        Character letter = validateInput(modDeleteItem.getText());
        if (letter == null) {
            notificationText.setText("Entrada invalida. Solo letras de la A a la Z.");
            return;
        }

        boolean deleted = false;
        List<Integer> path = new ArrayList<>();

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (!digitalInsertionOrder.contains(letter)) {
                    notificationText.setText("Letra '" + letter + "' no encontrada para eliminar.");
                    return;
                }
                searchDigital(letter, path);
                digitalInsertionOrder.remove(letter);
                rebuildDigitalTree();
                deleted = true;
                animateDeletion(path, "digital");
                break;

            case "Arboles de busqueda por residuos":
                if (!residueInsertionOrder.contains(letter)) {
                    notificationText.setText("Letra '" + letter + "' no encontrada para eliminar.");
                    return;
                }
                searchResidue(letter, path);
                residueInsertionOrder.remove(letter);
                rebuildResidueTree();
                deleted = true;
                animateDeletion(path, "residue");
                break;

            case "Arboles de busqueda por residuos multiple":
                if (!multipleResidueInsertionOrder.contains(letter)) {
                    notificationText.setText("Letra '" + letter + "' no encontrada para eliminar.");
                    return;
                }

                searchMultipleResidue(letter, path);
                multipleResidueInsertionOrder.remove(letter);

                deleteMultipleResidueData(multipleResidueRoot, letter);
                deleted = true;
                animateDeletion(path, "multiple");
                break;
        }

        if (deleted) {
            updateItemsText();
            saveState();
            updateTreeVisualization();
            notificationText.setText("Letra '" + letter + "' eliminada.");
            focusOnRootWithDelay();
        }

        modDeleteItem.clear();

        boolean isEmpty = false;
        switch (treeString) {
            case "Arboles de busqueda digital":
                isEmpty = digitalInsertionOrder.isEmpty();
                break;
            case "Arboles de busqueda por residuos":
                isEmpty = residueInsertionOrder.isEmpty();
                break;
            case "Arboles de busqueda por residuos multiple":
                isEmpty = multipleResidueInsertionOrder.isEmpty();
                break;
        }

        if (isEmpty) {
            searchButton.setDisable(true);
            deleteButton.setDisable(true);
            modDeleteItem.setDisable(true);
        }
    }

    private void deleteMultipleResidueData(MultipleResidueNode node, Character letter) {
        if (node == null)
            return;

        if (!node.isLink && node.letter != null && node.letter.equals(letter)) {
            node.isLink = true;
            node.letter = null;
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                deleteMultipleResidueData(node.children[i], letter);
            }
        }
    }

    private void animateInsertion(Character letter, String treeType) {
        updateTreeVisualization();

        Circle circle = findNodeCircle(letter, treeType);
        if (circle != null) {
            FillTransition ft = new FillTransition(Duration.millis(1000), circle);
            ft.setFromValue(Color.WHITE);
            ft.setToValue(Color.LIGHTGREEN);
            ft.setCycleCount(2);
            ft.setAutoReverse(true);
            ft.play();
        }
    }

    private void animateDeletion(List<Integer> path, String treeType) {
        SequentialTransition sequentialTransition = new SequentialTransition();

        double startX = canvasWidth / 2;
        double startY = 150;
        double xOffset = 300;

        for (int i = 0; i < path.size(); i++) {
            int direction = path.get(i);
            xOffset *= 0.6;

            if (direction == 0) {
                startX -= xOffset;
            } else if (direction == 1) {
                startX += xOffset;
            } else {
                startX = startX - xOffset + (direction * xOffset * 2 / 3);
            }
            startY += 50;

            final double currentX = startX;
            final double currentY = startY;

            Circle circle = findCircleAt(currentX, currentY);
            if (circle != null) {
                FillTransition ft = new FillTransition(Duration.millis(500), circle);

                if (i == path.size() - 1) {
                    ft.setFromValue(Color.WHITE);
                    ft.setToValue(Color.RED);
                    ft.setCycleCount(3);
                    ft.setAutoReverse(true);
                } else {
                    ft.setFromValue(Color.WHITE);
                    ft.setToValue(Color.ORANGE);
                }

                sequentialTransition.getChildren().add(ft);

                if (i < path.size() - 1) {
                    PauseTransition pause = new PauseTransition(Duration.millis(200));
                    sequentialTransition.getChildren().add(pause);
                }
            }
        }

        sequentialTransition.play();
    }

    private Circle findNodeCircle(Character letter, String treeType) {
        switch (treeType) {
            case "digital":
                DigitalNode digitalNode = findDigitalNode(letter);
                return digitalNode != null ? digitalNode.circle : null;
            case "residue":
                ResidueNode residueNode = findResidueNode(letter);
                return residueNode != null ? residueNode.circle : null;
            case "multiple":
                MultipleResidueNode multipleNode = findMultipleResidueNode(letter);
                return multipleNode != null ? multipleNode.circle : null;
            default:
                return null;
        }
    }

    private DigitalNode findDigitalNode(Character letter) {
        return findDigitalNodeRecursive(digitalRoot, letter);
    }

    private DigitalNode findDigitalNodeRecursive(DigitalNode node, Character letter) {
        if (node == null)
            return null;
        if (node.letter != null && node.letter.equals(letter))
            return node;

        DigitalNode left = findDigitalNodeRecursive(node.left, letter);
        if (left != null)
            return left;

        return findDigitalNodeRecursive(node.right, letter);
    }

    private ResidueNode findResidueNode(Character letter) {
        return findResidueNodeRecursive(residueRoot, letter);
    }

    private ResidueNode findResidueNodeRecursive(ResidueNode node, Character letter) {
        if (node == null)
            return null;
        if (node.letter != null && node.letter.equals(letter))
            return node;

        ResidueNode left = findResidueNodeRecursive(node.left, letter);
        if (left != null)
            return left;

        return findResidueNodeRecursive(node.right, letter);
    }

    private MultipleResidueNode findMultipleResidueNode(Character letter) {
        return findMultipleResidueNodeRecursive(multipleResidueRoot, letter);
    }

    private MultipleResidueNode findMultipleResidueNodeRecursive(MultipleResidueNode node, Character letter) {
        if (node == null)
            return null;
        if (node.letter != null && node.letter.equals(letter))
            return node;

        for (int i = 0; i < 4; i++) {
            MultipleResidueNode child = findMultipleResidueNodeRecursive(node.children[i], letter);
            if (child != null)
                return child;
        }

        return null;
    }

    private boolean searchDigitalWithPath(Character letter, List<Integer> path, List<Object> searchPath) {
        String binary = letterToBinary(letter);
        if (binary == null || digitalRoot == null)
            return false;

        DigitalNode current = digitalRoot;
        searchPath.add(current);
        path.add(0);
        int bitIndex = 0;

        while (current != null) {
            if (current.letter != null && current.letter.equals(letter)) {
                return true;
            }

            if (bitIndex >= binary.length()) {
                return false;
            }
            char bit = binary.charAt(bitIndex);
            if (bit == '0') {
                if (current.left == null) {
                    return false;
                }
                searchPath.add(current.left);
                current = current.left;
                path.add(0);
            } else {
                if (current.right == null) {
                    return false;
                }
                searchPath.add(current.right);
                current = current.right;
                path.add(1);
            }
            bitIndex++;
        }
        return false;
    }

    private boolean searchResidueWithPath(Character letter, List<Integer> path, List<Object> searchPath) {
        String binary = letterToBinary(letter);
        if (binary == null || residueRoot == null)
            return false;

        searchPath.add(residueRoot);
        return searchResidueWithPathRecursive(residueRoot, letter, binary, 0, path, searchPath);
    }

    private boolean searchResidueWithPathRecursive(ResidueNode node, Character letter, String binary, int bitIndex,
            List<Integer> path, List<Object> searchPath) {
        if (node == null)
            return false;

        if (!node.isLink && node.letter != null) {
            return node.letter.equals(letter);
        }

        if (bitIndex >= binary.length()) {
            return false;
        }

        char bit = binary.charAt(bitIndex);
        if (bit == '0') {
            path.add(0);
            if (node.left != null) {
                searchPath.add(node.left);
                return searchResidueWithPathRecursive(node.left, letter, binary, bitIndex + 1, path, searchPath);
            }
        } else {
            path.add(1);
            if (node.right != null) {
                searchPath.add(node.right);
                return searchResidueWithPathRecursive(node.right, letter, binary, bitIndex + 1, path, searchPath);
            }
        }
        return false;
    }

    private boolean searchMultipleResidueWithPath(Character letter, List<Integer> path, List<Object> searchPath) {
        String binary = letterToBinary(letter);
        if (binary == null || multipleResidueRoot == null)
            return false;

        searchPath.add(multipleResidueRoot);
        path.add(0);
        return searchMultipleResidueWithPathRecursive(multipleResidueRoot, letter, binary, 0, path, searchPath);
    }

    private boolean searchMultipleResidueWithPathRecursive(MultipleResidueNode node, Character letter, String binary,
            int index, List<Integer> path, List<Object> searchPath) {
        if (index >= binary.length()) {
            return !node.isLink && node.letter != null && node.letter.equals(letter);
        }

        if (node.isLink) {
            int bitsToTake;
            if (index + 2 <= binary.length()) {
                bitsToTake = 2;
            } else {
                bitsToTake = 1;
            }

            String bits = binary.substring(index, index + bitsToTake);
            int childIndex = Integer.parseInt(bits, 2);

            if (bitsToTake == 1) {
                childIndex = bits.equals("0") ? 0 : 1;
            }

            if (node.children[childIndex] == null) {
                return false;
            }

            searchPath.add(node.children[childIndex]);
            path.add(childIndex);
            return searchMultipleResidueWithPathRecursive(node.children[childIndex], letter, binary, index + bitsToTake,
                    path, searchPath);
        } else {
            return node.letter != null && node.letter.equals(letter);
        }
    }

    private boolean searchHuffmanPath(HuffmanNode node, Character letter, List<HuffmanNode> path) {
        if (node == null)
            return false;

        path.add(node);

        if (node.isLeaf() && node.letter != null && node.letter.equals(letter)) {
            currentSearchPath.addAll(path);
            return true;
        }

        if (searchHuffmanPath(node.left, letter, path)) {
            return true;
        }

        if (searchHuffmanPath(node.right, letter, path)) {
            return true;
        }

        path.remove(path.size() - 1);
        return false;
    }

    private void clearSearchHighlight() {
        currentSearchPath.clear();
        isSearchInProgress = false;
    }

    private void saveState() {
        TreeState state = new TreeState(digitalInsertionOrder, residueInsertionOrder,
                multipleResidueInsertionOrder, huffmanMessage);
        undoStack.push(state);
        redoStack.clear();
        updateUndoRedoButtons();
    }

    private void restoreState(TreeState state) {

        this.digitalInsertionOrder = new ArrayList<>(state.digitalOrder);
        this.residueInsertionOrder = new ArrayList<>(state.residueOrder);
        this.multipleResidueInsertionOrder = new ArrayList<>(state.multipleResidueOrder);
        this.huffmanMessage = state.huffmanMessage;

        switch (treeString) {
            case "Arboles de busqueda digital":
                rebuildDigitalTree();
                break;
            case "Arboles de busqueda por residuos":
                rebuildResidueTree();
                break;
            case "Arboles de busqueda por residuos multiple":
                rebuildMultipleResidueTree();
                break;
            case "Arboles de Huffman":
                if (huffmanMessage != null && !huffmanMessage.isEmpty()) {
                    buildHuffmanTree(huffmanMessage);
                } else {
                    huffmanRoot = null;
                    huffmanCodes.clear();
                }
                break;
        }

        updateTreeVisualization();
        updateItemsText();
    }

    private void updateButtonStates() {
        boolean isEmpty = false;
        switch (treeString) {
            case "Arboles de busqueda digital":
                isEmpty = digitalInsertionOrder.isEmpty();
                break;
            case "Arboles de busqueda por residuos":
                isEmpty = residueInsertionOrder.isEmpty();
                break;
            case "Arboles de busqueda por residuos multiple":
                isEmpty = multipleResidueInsertionOrder.isEmpty();
                break;
            case "Arboles de Huffman":
                isEmpty = (huffmanMessage == null || huffmanMessage.isEmpty());
                break;
        }

        if (isEmpty) {
            searchButton.setDisable(true);
            deleteButton.setDisable(true);
            modDeleteItem.setDisable(true);
        } else {
            searchButton.setDisable(false);
            deleteButton.setDisable(false);
            modDeleteItem.setDisable(false);
        }
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
    }

    private void setupZoomAndScroll() {
        treePane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();

                double zoomFactor = (event.getDeltaY() > 0) ? 1 + SCALE_DELTA : 1 - SCALE_DELTA;
                double newScale = currentScale * zoomFactor;

                newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

                if (treePane.getContent() != null) {
                    double hValue = treePane.getHvalue();
                    double vValue = treePane.getVvalue();

                    treePane.getContent().setScaleX(newScale);
                    treePane.getContent().setScaleY(newScale);
                    currentScale = newScale;

                    treePane.setHvalue(hValue * (currentScale / newScale));
                    treePane.setVvalue(vValue * (currentScale / newScale));
                }
            }
        });

        treePane.contentProperty().addListener((obs, oldContent, newContent) -> {
            if (newContent != null) {
                newContent.setScaleX(currentScale);
                newContent.setScaleY(currentScale);
            }
        });

        treePane.setFitToWidth(false);
        treePane.setFitToHeight(false);
        treePane.setPannable(true);
    }

    private void setupKeyboardPan() {
        treePane.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                double delta = 50;
                switch (event.getCode()) {
                    case LEFT:
                        treePane.setHvalue(treePane.getHvalue() - delta / treePane.getWidth());
                        break;
                    case RIGHT:
                        treePane.setHvalue(treePane.getHvalue() + delta / treePane.getWidth());
                        break;
                    case UP:
                        treePane.setVvalue(treePane.getVvalue() - delta / treePane.getHeight());
                        break;
                    case DOWN:
                        treePane.setVvalue(treePane.getVvalue() + delta / treePane.getHeight());
                        break;
                    default:
                        break;
                }
                event.consume();
            }
        });

        treePane.setFocusTraversable(true);

        treePane.setOnMouseClicked(event -> treePane.requestFocus());
    }

    private String letterToBinary(char letter) {
        if (letter < 'A' || letter > 'Z') {
            return null;
        }

        int value = letter - 'A' + 1;
        String binary = Integer.toBinaryString(value);

        while (binary.length() < 5) {
            binary = "0" + binary;
        }

        return binary;
    }

    private Character validateInput(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        char c = input.charAt(0);

        if (c >= 'a' && c <= 'z') {
            c = Character.toUpperCase(c);
        }

        if (c < 'A' || c > 'Z') {
            return null;
        }

        return c;
    }

    private boolean searchDigital(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || digitalRoot == null)
            return false;

        DigitalNode current = digitalRoot;
        path.add(0);
        int bitIndex = 0;

        while (current != null) {
            if (current.letter != null && current.letter.equals(letter)) {
                return true;
            }

            if (bitIndex >= binary.length()) {
                return false;
            }
            char bit = binary.charAt(bitIndex);
            if (bit == '0') {
                if (current.left == null) {
                    return false;
                }
                current = current.left;
                path.add(0);
            } else {
                if (current.right == null) {
                    return false;
                }
                current = current.right;
                path.add(1);
            }
            bitIndex++;
        }
        return false;
    }

    private boolean searchResidue(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || residueRoot == null)
            return false;

        return searchResidueDigital(residueRoot, letter, binary, 0, path);
    }

    private boolean searchResidueDigital(ResidueNode node, Character letter, String binary, int bitIndex,
            List<Integer> path) {
        if (node == null)
            return false;

        if (!node.isLink && node.letter != null) {
            return node.letter.equals(letter);
        }

        if (bitIndex >= binary.length()) {
            return false;
        }

        char bit = binary.charAt(bitIndex);
        if (bit == '0') {
            path.add(0);
            return searchResidueDigital(node.left, letter, binary, bitIndex + 1, path);
        } else {
            path.add(1);
            return searchResidueDigital(node.right, letter, binary, bitIndex + 1, path);
        }
    }

    private boolean searchMultipleResidue(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || multipleResidueRoot == null)
            return false;

        path.add(0);
        return searchMultipleResidueData(multipleResidueRoot, letter, binary, 0, path);
    }

    private boolean searchMultipleResidueData(MultipleResidueNode node, Character letter, String binary, int index,
            List<Integer> path) {
        if (index >= binary.length()) {
            return !node.isLink && node.letter != null && node.letter.equals(letter);
        }

        if (node.isLink) {
            int bitsToTake;
            if (index + 2 <= binary.length()) {
                bitsToTake = 2;
            } else {
                bitsToTake = 1;
            }

            String bits = binary.substring(index, index + bitsToTake);
            int childIndex = Integer.parseInt(bits, 2);

            if (bitsToTake == 1) {
                childIndex = bits.equals("0") ? 0 : 1;
            }

            if (node.children[childIndex] == null) {
                return false;
            }

            path.add(childIndex);
            return searchMultipleResidueData(node.children[childIndex], letter, binary, index + bitsToTake, path);
        } else {
            return node.letter != null && node.letter.equals(letter);
        }
    }

    private void buildHuffmanTree(String message) {
        if (message == null || message.isEmpty()) {
            notificationText.setText("Mensaje vacio para construir arbol de Huffman.");
            return;
        }

        huffmanMessage = message;

        String reversedMessage = new StringBuilder(message).reverse().toString();

        Map<Character, Integer> charCount = new LinkedHashMap<>();
        int totalChars = 0;
        for (char c : reversedMessage.toCharArray()) {
            char upper = Character.toUpperCase(c);
            if (upper >= 'A' && upper <= 'Z') {
                charCount.put(upper, charCount.getOrDefault(upper, 0) + 1);
                totalChars++;
            }
        }

        if (charCount.isEmpty()) {
            notificationText.setText("El mensaje no contiene letras validas (A-Z).");
            huffmanRoot = null;
            huffmanCodes.clear();
            updateTreeVisualization();
            return;
        }

        LinkedList<HuffmanNode> nodeList = new LinkedList<>();
        int insertionOrder = 0;
        for (Map.Entry<Character, Integer> entry : charCount.entrySet()) {
            char letter = entry.getKey();
            double freq = (double) entry.getValue() / totalChars;
            nodeList.add(new HuffmanNode(letter, freq, insertionOrder++));
        }

        nodeList.sort((a, b) -> {
            int cmp = Double.compare(b.frequency, a.frequency);
            if (cmp != 0)
                return cmp;
            return Integer.compare(a.insertionOrder, b.insertionOrder);
        });

        int currentInsertion = insertionOrder;

        while (nodeList.size() > 1) {

            HuffmanNode last = nodeList.removeLast();
            HuffmanNode secondLast = nodeList.removeLast();

            double combinedFreq = last.frequency + secondLast.frequency;
            HuffmanNode parent = new HuffmanNode(last, secondLast, combinedFreq, currentInsertion++);

            boolean inserted = false;
            for (int i = 0; i < nodeList.size(); i++) {
                HuffmanNode current = nodeList.get(i);

                if (parent.frequency > current.frequency) {
                    nodeList.add(i, parent);
                    inserted = true;
                    break;
                } else if (parent.frequency == current.frequency) {

                    nodeList.add(i, parent);
                    inserted = true;
                    break;
                }
            }
            if (!inserted)
                nodeList.addLast(parent);

        }

        huffmanRoot = nodeList.getFirst();
        huffmanCodes.clear();
        generateHuffmanCodes(huffmanRoot, "");

        updateTreeVisualization();
    }

    private String getHuffmanTreeStructure(HuffmanNode node) {
        if (node == null)
            return "";

        if (node.isLeaf()) {
            return String.valueOf(node.letter);
        }

        String leftStructure = getHuffmanTreeStructure(node.left);
        String rightStructure = getHuffmanTreeStructure(node.right);

        return "(" + leftStructure + "+" + rightStructure + ")";
    }

    private void generateHuffmanCodes(HuffmanNode node, String code) {
        if (node == null)
            return;

        if (node.isLeaf()) {
            huffmanCodes.put(node.letter, code);
            System.out.println("Codigo para '" + node.letter + "': " + code);
            return;
        }

        generateHuffmanCodes(node.left, code + "0");
        generateHuffmanCodes(node.right, code + "1");
    }

    private void updateTreeVisualization() {
        int nodeCount = 0;
        switch (treeString) {
            case "Arboles de busqueda digital":
                nodeCount = digitalInsertionOrder.size();
                canvasWidth = Math.max(1000, 300 + nodeCount * BASE_NODE_WIDTH * 1.5);
                canvasHeight = Math.max(700, 200 + nodeCount * BASE_NODE_HEIGHT);
                break;
            case "Arboles de busqueda por residuos":
                nodeCount = residueInsertionOrder.size();
                canvasWidth = Math.max(1000, 300 + nodeCount * BASE_NODE_WIDTH * 1.5);
                canvasHeight = Math.max(700, 200 + nodeCount * BASE_NODE_HEIGHT);
                break;
            case "Arboles de busqueda por residuos multiple":
                nodeCount = multipleResidueInsertionOrder.size();
                double estimatedWidth = calculateTreeWidth();
                double estimatedHeight = calculateTreeHeight();
                canvasWidth = Math.max(2000, (int) estimatedWidth + 400);
                canvasHeight = Math.max(1200, (int) estimatedHeight + 300);
                break;
            case "Arboles de Huffman":
                nodeCount = huffmanMessage != null ? huffmanMessage.length() : 0;
                canvasWidth = Math.max(1000, 300 + nodeCount * BASE_NODE_WIDTH * 1.5);
                canvasHeight = Math.max(700, 200 + nodeCount * BASE_NODE_HEIGHT);
                break;
        }

        Pane canvas = new Pane();
        canvas.setPrefSize(canvasWidth, canvasHeight);
        canvas.setMinSize(canvasWidth, canvasHeight);
        canvas.setMaxSize(canvasWidth, canvasHeight);

        Button expandButton = new Button("Ampliar Vista");
        expandButton.setLayoutX(360);
        expandButton.setLayoutY(120);
        expandButton.setOnAction(e -> showExpandedTreeView());
        principalPane.getChildren().add(expandButton);

        double initialX = canvasWidth / 2;
        double initialY = 100;

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (digitalRoot != null) {
                    drawDigitalTree(canvas, digitalRoot, initialX, initialY, canvasWidth * 0.4);
                }
                break;
            case "Arboles de busqueda por residuos":
                if (residueRoot != null) {
                    drawResidueTree(canvas, residueRoot, initialX, initialY, canvasWidth * 0.4);
                }
                break;
            case "Arboles de busqueda por residuos multiple":
                if (multipleResidueRoot != null) {
                    drawMultipleResidueTree(canvas, multipleResidueRoot, initialX, initialY, canvasWidth * 0.4, 0);
                }
                break;
            case "Arboles de Huffman":
                if (huffmanRoot != null) {
                    drawHuffmanTree(canvas, huffmanRoot, initialX, initialY, canvasWidth * 0.4);
                }
                break;
            default:
                break;
        }

        canvas.setScaleX(currentScale);
        canvas.setScaleY(currentScale);

        treePane.setContent(canvas);
        treePane.layout();
    }

    private double calculateTreeWidth() {
        if (multipleResidueRoot == null)
            return 800;
        return calculateSubtreeWidth(multipleResidueRoot, 0) * 1.5;
    }

    private double calculateTreeHeight() {
        if (multipleResidueRoot == null)
            return 600;
        int maxDepth = calculateTreeDepth(multipleResidueRoot, 0);
        return 100 + (maxDepth * 120);
    }

    private int calculateTreeDepth(MultipleResidueNode node, int currentDepth) {
        if (node == null)
            return currentDepth;

        int maxDepth = currentDepth;
        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                int childDepth = calculateTreeDepth(node.children[i], currentDepth + 1);
                maxDepth = Math.max(maxDepth, childDepth);
            }
        }

        return maxDepth;
    }

    private double drawDigitalTree(Pane canvas, DigitalNode node, double x, double y, double levelWidth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor = Color.WHITE;
        Color strokeColor = Color.BLACK;

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        Circle circle = new Circle(x, y, 20);
        circle.setFill(nodeColor);
        circle.setStroke(strokeColor);
        canvas.getChildren().add(circle);
        node.circle = circle;

        String text = (node.letter != null) ? String.valueOf(node.letter) : "";
        Text nodeText = new Text(x - 5, y + 5, text);
        canvas.getChildren().add(nodeText);

        double childY = y + 100;
        double totalWidth = 0;

        if (node.left != null) {
            double leftWidth = drawDigitalTree(canvas, node.left, x - levelWidth / 2, childY, levelWidth * 0.5);
            totalWidth += leftWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.left)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + 20, x - levelWidth / 2, childY - 20);
            line.setStroke(lineColor);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x - levelWidth / 4 - 5, (y + childY) / 2, "0");
            edgeText.setFill(lineColor);
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightWidth = drawDigitalTree(canvas, node.right, x + levelWidth / 2, childY, levelWidth * 0.5);
            totalWidth += rightWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.right)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + 20, x + levelWidth / 2, childY - 20);
            line.setStroke(lineColor);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x + levelWidth / 4 - 5, (y + childY) / 2, "1");
            edgeText.setFill(lineColor);
            canvas.getChildren().add(edgeText);
        }

        return Math.max(levelWidth, totalWidth);
    }

    private double drawResidueTree(Pane canvas, ResidueNode node, double x, double y, double levelWidth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor = node.isLink ? Color.LIGHTGRAY : Color.WHITE;
        Color strokeColor = Color.BLACK;

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        if (node == residueRoot) {
            Circle circle = new Circle(x, y, 20);
            circle.setFill(nodeColor);
            circle.setStroke(strokeColor);
            canvas.getChildren().add(circle);
            node.circle = circle;
            Text nodeText = new Text(x - 5, y + 5, "");
            canvas.getChildren().add(nodeText);
        } else {
            if (node.isLink) {
                Circle circle = new Circle(x, y, 20);
                circle.setFill(nodeColor);
                circle.setStroke(strokeColor);
                canvas.getChildren().add(circle);
                node.circle = circle;
                Text nodeText = new Text(x - 5, y + 5, "");
                canvas.getChildren().add(nodeText);
            } else {
                Rectangle rect = new Rectangle(x - 20, y - 20, 40, 40);
                rect.setFill(nodeColor);
                rect.setStroke(strokeColor);
                canvas.getChildren().add(rect);

                Circle circle = new Circle(x, y, 20);
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(Color.TRANSPARENT);
                canvas.getChildren().add(circle);
                node.circle = circle;

                String text = (node.letter != null) ? String.valueOf(node.letter) : "";
                Text nodeText = new Text(x - 5, y + 5, text);
                canvas.getChildren().add(nodeText);
            }
        }

        double childY = y + 100;
        double totalWidth = 0;

        if (node.left != null) {
            double leftWidth = drawResidueTree(canvas, node.left, x - levelWidth / 2, childY, levelWidth * 0.5);
            totalWidth += leftWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.left)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + 20, x - levelWidth / 2, childY - 20);
            line.setStroke(lineColor);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x - levelWidth / 4 - 5, (y + childY) / 2, "0");
            edgeText.setFill(lineColor);
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightWidth = drawResidueTree(canvas, node.right, x + levelWidth / 2, childY, levelWidth * 0.5);
            totalWidth += rightWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.right)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + 20, x + levelWidth / 2, childY - 20);
            line.setStroke(lineColor);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x + levelWidth / 4 - 5, (y + childY) / 2, "1");
            edgeText.setFill(lineColor);
            canvas.getChildren().add(edgeText);
        }

        return Math.max(levelWidth, totalWidth);
    }

    private double drawMultipleResidueTree(Pane canvas, MultipleResidueNode node, double x, double y, double levelWidth,
            int depth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor = node.isLink ? Color.LIGHTGRAY : Color.WHITE;
        Color strokeColor = Color.BLACK;

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        if (node.isLink) {
            Circle circle = new Circle(x, y, 20);
            circle.setFill(nodeColor);
            circle.setStroke(strokeColor);
            canvas.getChildren().add(circle);
            node.circle = circle;
            Text nodeText = new Text(x - 5, y + 5, "");
            nodeText.setFill(strokeColor);
            canvas.getChildren().add(nodeText);
        } else {
            Rectangle rect = new Rectangle(x - 20, y - 20, 40, 40);
            rect.setFill(nodeColor);
            rect.setStroke(strokeColor);
            canvas.getChildren().add(rect);

            Circle circle = new Circle(x, y, 20);
            circle.setFill(Color.TRANSPARENT);
            circle.setStroke(Color.TRANSPARENT);
            canvas.getChildren().add(circle);
            node.circle = circle;

            String text = (node.letter != null) ? String.valueOf(node.letter) : "";
            Text nodeText = new Text(x - 5, y + 5, text);
            nodeText.setFill(strokeColor);
            canvas.getChildren().add(nodeText);
        }

        List<Integer> nonNullChildren = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                nonNullChildren.add(i);
            }
        }

        if (nonNullChildren.isEmpty()) {
            return 100;
        }

        double totalChildWidth = 0;
        List<Double> childWidths = new ArrayList<>();

        for (int childIndex : nonNullChildren) {
            double childTreeWidth = calculateSubtreeWidth(node.children[childIndex], depth + 1);
            childWidths.add(childTreeWidth);
            totalChildWidth += childTreeWidth;
        }

        double horizontalSpacing = 80;
        totalChildWidth += horizontalSpacing * (nonNullChildren.size() - 1);

        if (totalChildWidth > levelWidth) {
            levelWidth = totalChildWidth;
        }

        double verticalSpacing = 120;
        double childY = y + verticalSpacing;
        double currentX = x - (totalChildWidth / 2);

        double maxChildWidth = 0;
        int childCount = 0;

        for (int childIndex = 0; childIndex < 4; childIndex++) {
            if (node.children[childIndex] != null) {
                double childTreeWidth = childWidths.get(childCount);
                double childX = currentX + (childTreeWidth / 2);

                if (childCount > 0) {
                    double previousChildRight = currentX - horizontalSpacing;
                    if (childX - (childTreeWidth / 2) < previousChildRight) {
                        childX = previousChildRight + (childTreeWidth / 2) + horizontalSpacing;
                    }
                }

                Color lineColor = Color.BLACK;
                if (isSearchInProgress && currentSearchPath.contains(node)
                        && currentSearchPath.contains(node.children[childIndex])) {
                    lineColor = Color.GREEN;
                }

                Line line = new Line(x, y + 20, childX, childY - 20);
                line.setStroke(lineColor);
                canvas.getChildren().add(line);

                String label = getEdgeLabel(childIndex, depth);
                Text edgeText = new Text((x + childX) / 2 - 5, (y + childY) / 2, label);
                edgeText.setFill(lineColor);
                canvas.getChildren().add(edgeText);

                double actualChildWidth = drawMultipleResidueTree(canvas, node.children[childIndex],
                        childX, childY, childTreeWidth, depth + 1);

                maxChildWidth = Math.max(maxChildWidth, actualChildWidth);
                currentX += childTreeWidth + horizontalSpacing;
                childCount++;
            }
        }

        return Math.max(levelWidth, maxChildWidth);
    }

    private double calculateSubtreeWidth(MultipleResidueNode node, int depth) {
        if (node == null)
            return 0;

        double baseWidth = 40;
        double horizontalSpacing = 80;
        double maxChildWidth = 0;

        List<Integer> nonNullChildren = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                nonNullChildren.add(i);
            }
        }

        if (nonNullChildren.isEmpty()) {
            return baseWidth;
        }

        if (depth >= 3) {
            return baseWidth + (nonNullChildren.size() * 60);
        }

        double totalChildWidth = 0;
        for (int childIndex : nonNullChildren) {
            double childWidth = calculateSubtreeWidth(node.children[childIndex], depth + 1);
            totalChildWidth += childWidth;
            maxChildWidth = Math.max(maxChildWidth, childWidth);
        }

        totalChildWidth += horizontalSpacing * (nonNullChildren.size() - 1);

        return Math.max(baseWidth, totalChildWidth);
    }

    private String getEdgeLabel(int childIndex, int depth) {
        if (depth >= 2) {
            switch (childIndex) {
                case 0:
                    return "0";
                case 1:
                    return "1";
                case 2:
                    return "0";
                case 3:
                    return "1";
                default:
                    return "?";
            }
        } else {
            switch (childIndex) {
                case 0:
                    return "00";
                case 1:
                    return "01";
                case 2:
                    return "10";
                case 3:
                    return "11";
                default:
                    return "??";
            }
        }
    }

    private double drawHuffmanTree(Pane canvas, HuffmanNode node, double x, double y, double levelWidth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor, strokeColor;
        if (node.isLeaf()) {
            nodeColor = Color.WHITE;
            strokeColor = Color.BLACK;
        } else {
            nodeColor = Color.LIGHTGRAY;
            strokeColor = Color.BLACK;
        }

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        double nodeRadius = 20;
        Circle circle = new Circle(x, y, nodeRadius);
        circle.setFill(nodeColor);
        circle.setStroke(strokeColor);
        circle.setStrokeWidth(2);
        canvas.getChildren().add(circle);
        node.circle = circle;

        String text = node.getDisplayText();
        Text nodeText = new Text(x - (text.length() * 3), y + 5, text);
        nodeText.setStyle("-fx-font-size: " + (node.isLeaf() ? "12" : "10") + "px; -fx-font-weight: bold;");
        canvas.getChildren().add(nodeText);

        double childY = y + 80;
        double childWidth = levelWidth * 0.5;

        if (node.left != null) {
            double leftX = x - levelWidth / 2;
            double leftWidth = drawHuffmanTree(canvas, node.left, leftX, childY, childWidth);

            Line line = new Line(x, y + nodeRadius, leftX, childY - 20);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.5);
            canvas.getChildren().add(line);

            Text edgeText = new Text((x + leftX) / 2 - 8, (y + childY) / 2,
                    node.left.getFractionString());
            edgeText.setFill(Color.BLACK);
            edgeText.setStyle("-fx-font-weight: bold; -fx-font-size: 9px;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightX = x + levelWidth / 2;
            double rightWidth = drawHuffmanTree(canvas, node.right, rightX, childY, childWidth);

            Line line = new Line(x, y + nodeRadius, rightX, childY - 20);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.5);
            canvas.getChildren().add(line);

            Text edgeText = new Text((x + rightX) / 2 - 8, (y + childY) / 2,
                    node.right.getFractionString());
            edgeText.setFill(Color.BLACK);
            edgeText.setStyle("-fx-font-weight: bold; -fx-font-size: 9px;");
            canvas.getChildren().add(edgeText);
        }

        return levelWidth;
    }

    private void showExpandedTreeView() {

        Stage expandedStage = new Stage();
        expandedStage.setTitle("Vista Ampliada del arbol - " + treeString);
        expandedStage.initModality(Modality.WINDOW_MODAL);
        expandedStage.initOwner(treePane.getScene().getWindow());

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double stageWidth = screenBounds.getWidth() * 0.9;
        double stageHeight = screenBounds.getHeight() * 0.9;

        Pane expandedCanvas = createExpandedTreeCanvas();

        ScrollPane expandedScrollPane = createExpandedScrollPane(expandedCanvas, stageWidth, stageHeight);

        setupExpandedPan(expandedCanvas, expandedScrollPane);

        HBox buttonBox = createControlButtons(expandedStage, expandedScrollPane, expandedCanvas);

        BorderPane root = new BorderPane();
        root.setCenter(expandedScrollPane);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, stageWidth, stageHeight);
        expandedStage.setScene(scene);
        expandedStage.setMaximized(true);

        expandedStage.show();

        centerExpandedView(expandedScrollPane, expandedCanvas);
    }

    private ScrollPane createExpandedScrollPane(Pane expandedCanvas, double width, double height) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(expandedCanvas);
        scrollPane.setPrefSize(width, height);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);

        setupExpandedZoom(scrollPane);

        return scrollPane;
    }

    private void setupExpandedPan(Pane canvas, ScrollPane scrollPane) {
        final double[] dragDelta = new double[2];

        canvas.setOnMousePressed(event -> {

            if (!(event.getTarget() instanceof Circle) && !(event.getTarget() instanceof Text)) {
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

    private HBox createControlButtons(Stage stage, ScrollPane scrollPane, Pane canvas) {

        Button resetZoomButton = new Button("Centrar vista");
        resetZoomButton.setOnAction(e -> resetExpandedZoom(scrollPane, canvas));
        Button imprimirButton = new Button("Imprimir");
        imprimirButton.setOnAction(e -> imprimirVistaAmpliada());

        HBox buttonBox = new HBox(120, resetZoomButton, imprimirButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        return buttonBox;
    }

    private void imprimirVistaAmpliada() {
        try {

            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) {
                notificationText.setText("No se pudo crear el trabajo de impresion.");
                return;
            }

            boolean proceder = job.showPrintDialog(treePane.getScene().getWindow());
            if (!proceder) {
                return;
            }

            javafx.print.Printer printer = job.getPrinter();
            javafx.print.PageLayout pageLayout = printer.createPageLayout(
                    javafx.print.Paper.A4,
                    javafx.print.PageOrientation.PORTRAIT,
                    javafx.print.Printer.MarginType.HARDWARE_MINIMUM);

            double printableWidth = pageLayout.getPrintableWidth();
            double printableHeight = pageLayout.getPrintableHeight();

            Pane printCanvas = createPrintCanvasMaximized(printableWidth, printableHeight);

            job.getJobSettings().setPageLayout(pageLayout);

            boolean impreso = job.printPage(printCanvas);
            if (impreso) {
                job.endJob();
                notificationText.setText("Vista ampliada impresa exitosamente.");
            } else {
                notificationText.setText("Error al imprimir la vista ampliada.");
            }

        } catch (Exception e) {
            notificationText.setText("Error en impresion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Pane createPrintCanvasMaximized(double width, double height) {

        Pane printCanvas = new Pane();
        printCanvas.setPrefSize(width, height);
        printCanvas.setStyle("-fx-background-color: white;");

        double treeDisplayWidth = width * 0.95;
        double treeDisplayHeight = height * 0.85;

        double scaleX = treeDisplayWidth / canvasWidth;
        double scaleY = treeDisplayHeight / canvasHeight;
        double optimalScale = Math.min(scaleX, scaleY);

        double finalScale = Math.min(optimalScale, 1.2);

        double finalTreeWidth = canvasWidth * finalScale;
        double finalTreeHeight = canvasHeight * finalScale;

        double offsetX = (width - finalTreeWidth) / 2;
        double offsetY = 70;

        drawMaximizedTree(printCanvas, finalScale, offsetX, offsetY, finalTreeWidth, finalTreeHeight);

        addTreeInfoMaximized(printCanvas, width, height, finalScale);

        return printCanvas;
    }

    private void drawMaximizedTree(Pane canvas, double scale, double offsetX, double offsetY, double treeWidth,
            double treeHeight) {
        double initialX = (canvasWidth / 2) * scale + offsetX;
        double initialY = 100 * scale + offsetY;
        double levelWidth = treeWidth * 0.35;

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (digitalRoot != null) {
                    drawDigitalTreeMaximized(canvas, digitalRoot, initialX, initialY, levelWidth, scale);
                }
                break;
            case "Arboles de busqueda por residuos":
                if (residueRoot != null) {
                    drawResidueTreeMaximized(canvas, residueRoot, initialX, initialY, levelWidth * 0.9, scale);
                }
                break;
            case "Arboles de busqueda por residuos multiple":
                if (multipleResidueRoot != null) {
                    drawMultipleResidueTreeMaximized(canvas, multipleResidueRoot, initialX, initialY, levelWidth, scale,
                            0);
                }
                break;
            case "Arboles de Huffman":
                if (huffmanRoot != null) {
                    drawHuffmanTreeMaximized(canvas, huffmanRoot, initialX, initialY, levelWidth * 0.9, scale);
                }
                break;
        }
    }

    private void addTreeInfoMaximized(Pane canvas, double width, double height, double scale) {

        Text title = new Text(20, 25, "Arbol: " + treeString);
        title.setStyle("-fx-font-size: " + (16 * scale) + "px; -fx-font-weight: bold;");
        canvas.getChildren().add(title);

        String treeInfo = getTreeSpecificInfoMaximized();
        Text info = new Text(20, 65, treeInfo);
        info.setStyle("-fx-font-size: " + (10 * scale) + "px;");
        canvas.getChildren().add(info);

        Text pageInfo = new Text(width - 100, height - 15, "Pagina 1/1");
        pageInfo.setStyle("-fx-font-size: " + (9 * scale) + "px;");
        canvas.getChildren().add(pageInfo);
    }

    private String getTreeSpecificInfoMaximized() {
        switch (treeString) {
            case "Arboles de busqueda digital":
                return "Elementos: " + digitalInsertionOrder.size() +
                        "\nClaves: " + digitalInsertionOrder;
            case "Arboles de busqueda por residuos":
                return "Elementos: " + residueInsertionOrder.size() +
                        "\nClaves: " + residueInsertionOrder;
            case "Arboles de busqueda por residuos multiple":
                return "Elementos: " + multipleResidueInsertionOrder.size() +
                        "\nClaves: " + multipleResidueInsertionOrder;
            case "Arboles de Huffman":
                String msgPreview = huffmanMessage.length() > 50 ? huffmanMessage.substring(0, 50) + "..."
                        : huffmanMessage;
                return "Mensaje: " + msgPreview +
                        "\nCaracteres unicos: " + (huffmanCodes != null ? huffmanCodes.size() : 0) +
                        "\nLongitud total: " + huffmanMessage.length();
            default:
                return "";
        }
    }

    private double drawDigitalTreeMaximized(Pane canvas, DigitalNode node, double x, double y, double levelWidth,
            double scale) {
        if (node == null)
            return 0;

        double nodeRadius = 25 * scale;

        Circle circle = new Circle(x, y, nodeRadius);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2 * scale);
        canvas.getChildren().add(circle);

        String text = (node.letter != null) ? String.valueOf(node.letter) : "";
        Text nodeText = new Text(x - (5 * scale), y + (5 * scale), text);
        nodeText.setStyle("-fx-font-size: " + (12 * scale) + "px; -fx-font-weight: bold;");
        canvas.getChildren().add(nodeText);

        double childY = y + (90 * scale);

        if (node.left != null) {
            double leftX = x - levelWidth / 2;
            drawDigitalTreeMaximized(canvas, node.left, leftX, childY, levelWidth * 0.6, scale);

            Line line = new Line(x, y + nodeRadius, leftX, childY - (18 * scale));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x - levelWidth / 4 - (4 * scale), (y + childY) / 2, "0");
            edgeText.setStyle("-fx-font-size: " + (10 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightX = x + levelWidth / 2;
            drawDigitalTreeMaximized(canvas, node.right, rightX, childY, levelWidth * 0.6, scale);

            Line line = new Line(x, y + nodeRadius, rightX, childY - (18 * scale));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x + levelWidth / 4 - (4 * scale), (y + childY) / 2, "1");
            edgeText.setStyle("-fx-font-size: " + (10 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        return levelWidth;
    }

    private double drawResidueTreeMaximized(Pane canvas, ResidueNode node, double x, double y, double levelWidth,
            double scale) {
        if (node == null)
            return 0;

        double nodeSize = 35 * scale;
        double nodeRadius = 25 * scale;

        if (node == residueRoot || node.isLink) {

            Circle circle = new Circle(x, y, nodeRadius);
            circle.setFill(Color.LIGHTGRAY);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(circle);

            Text nodeText = new Text(x - (4 * scale), y + (5 * scale), "");
            nodeText.setStyle("-fx-font-size: " + (11 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(nodeText);
        } else {

            Rectangle rect = new Rectangle(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);
            rect.setFill(Color.WHITE);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(rect);

            String text = (node.letter != null) ? String.valueOf(node.letter) : "";
            Text nodeText = new Text(x - (4 * scale), y + (5 * scale), text);
            nodeText.setStyle("-fx-font-size: " + (12 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(nodeText);
        }

        double childY = y + (90 * scale);

        if (node.left != null) {
            double leftX = x - levelWidth / 2;
            drawResidueTreeMaximized(canvas, node.left, leftX, childY, levelWidth * 0.6, scale);

            Line line = new Line(x, y + nodeRadius, leftX, childY - (18 * scale));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x - levelWidth / 4 - (4 * scale), (y + childY) / 2, "0");
            edgeText.setStyle("-fx-font-size: " + (10 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightX = x + levelWidth / 2;
            drawResidueTreeMaximized(canvas, node.right, rightX, childY, levelWidth * 0.6, scale);

            Line line = new Line(x, y + nodeRadius, rightX, childY - (18 * scale));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x + levelWidth / 4 - (4 * scale), (y + childY) / 2, "1");
            edgeText.setStyle("-fx-font-size: " + (10 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        return levelWidth;
    }

    private double drawMultipleResidueTreeMaximized(Pane canvas, MultipleResidueNode node, double x, double y,
            double levelWidth, double scale, int depth) {
        if (node == null)
            return 0;

        double nodeSize = 35 * scale;
        double nodeRadius = 18 * scale;

        if (node.isLink) {

            Circle circle = new Circle(x, y, nodeRadius);
            circle.setFill(Color.LIGHTGRAY);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(circle);

            Text nodeText = new Text(x - (4 * scale), y + (5 * scale), "");
            nodeText.setStyle("-fx-font-size: " + (11 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(nodeText);
        } else {

            Rectangle rect = new Rectangle(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);
            rect.setFill(Color.WHITE);
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(1.8 * scale);
            canvas.getChildren().add(rect);

            String text = (node.letter != null) ? String.valueOf(node.letter) : "";
            Text nodeText = new Text(x - (4 * scale), y + (5 * scale), text);
            nodeText.setStyle("-fx-font-size: " + (12 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(nodeText);
        }

        List<Integer> nonNullChildren = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                nonNullChildren.add(i);
            }
        }

        if (nonNullChildren.isEmpty()) {
            return 90 * scale;
        }

        double totalChildWidth = 0;
        List<Double> childWidths = new ArrayList<>();

        for (int childIndex : nonNullChildren) {
            double childTreeWidth = calculateSubtreeWidthMaximized(node.children[childIndex], depth + 1) * scale;
            childWidths.add(childTreeWidth);
            totalChildWidth += childTreeWidth;
        }

        double horizontalSpacing = 70 * scale;
        totalChildWidth += horizontalSpacing * (nonNullChildren.size() - 1);

        if (totalChildWidth > levelWidth) {
            levelWidth = totalChildWidth;
        }

        double verticalSpacing = 110 * scale;
        double childY = y + verticalSpacing;
        double currentX = x - (totalChildWidth / 2);

        double maxChildWidth = 0;
        int childCount = 0;

        for (int childIndex = 0; childIndex < 4; childIndex++) {
            if (node.children[childIndex] != null) {
                double childTreeWidth = childWidths.get(childCount);
                double childX = currentX + (childTreeWidth / 2);

                Line line = new Line(x, y + nodeRadius, childX, childY - (18 * scale));
                line.setStroke(Color.BLACK);
                line.setStrokeWidth(1.8 * scale);
                canvas.getChildren().add(line);

                String label = getEdgeLabel(childIndex, depth);
                Text edgeText = new Text((x + childX) / 2 - (7 * scale), (y + childY) / 2, label);
                edgeText.setStyle("-fx-font-size: " + (9 * scale) + "px; -fx-font-weight: bold;");
                canvas.getChildren().add(edgeText);

                double actualChildWidth = drawMultipleResidueTreeMaximized(canvas, node.children[childIndex],
                        childX, childY, childTreeWidth, scale, depth + 1);

                maxChildWidth = Math.max(maxChildWidth, actualChildWidth);
                currentX += childTreeWidth + horizontalSpacing;
                childCount++;
            }
        }

        return Math.max(levelWidth, maxChildWidth);
    }

    private double calculateSubtreeWidthMaximized(MultipleResidueNode node, int depth) {
        if (node == null)
            return 0;

        double baseWidth = 35;
        double horizontalSpacing = 70;
        double maxChildWidth = 0;

        List<Integer> nonNullChildren = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                nonNullChildren.add(i);
            }
        }

        if (nonNullChildren.isEmpty()) {
            return baseWidth;
        }

        if (depth >= 3) {
            return baseWidth + (nonNullChildren.size() * 50);
        }

        double totalChildWidth = 0;
        for (int childIndex : nonNullChildren) {
            double childWidth = calculateSubtreeWidthMaximized(node.children[childIndex], depth + 1);
            totalChildWidth += childWidth;
            maxChildWidth = Math.max(maxChildWidth, childWidth);
        }

        totalChildWidth += horizontalSpacing * (nonNullChildren.size() - 1);

        return Math.max(baseWidth, totalChildWidth);
    }

    private double drawHuffmanTreeMaximized(Pane canvas, HuffmanNode node, double x, double y, double levelWidth,
            double scale) {
        if (node == null)
            return 0;

        double nodeRadius = 22 * scale;

        Color nodeColor, strokeColor;
        if (node.isLeaf()) {
            nodeColor = Color.WHITE;
            strokeColor = Color.BLACK;
        } else {
            nodeColor = Color.LIGHTGRAY;
            strokeColor = Color.BLACK;
        }

        Circle circle = new Circle(x, y, nodeRadius);
        circle.setFill(nodeColor);
        circle.setStroke(strokeColor);
        circle.setStrokeWidth(2.2 * scale);
        canvas.getChildren().add(circle);

        String text = node.getDisplayText();
        Text nodeText = new Text(x - (text.length() * 2.5 * scale), y + (5 * scale), text);
        String fontSize = node.isLeaf() ? "13" : "11";
        nodeText.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-weight: bold;");
        canvas.getChildren().add(nodeText);

        double childY = y + (85 * scale);
        double childWidth = levelWidth * 0.55;

        if (node.left != null) {
            double leftX = x - levelWidth / 2;
            drawHuffmanTreeMaximized(canvas, node.left, leftX, childY, childWidth, scale);

            Line line = new Line(x, y + nodeRadius, leftX, childY - (18 * scale));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2 * scale);
            canvas.getChildren().add(line);

            Text edgeText = new Text((x + leftX) / 2 - (13 * scale), (y + childY) / 2, node.left.getFractionString());
            edgeText.setStyle("-fx-font-size: " + (9 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightX = x + levelWidth / 2;
            drawHuffmanTreeMaximized(canvas, node.right, rightX, childY, childWidth, scale);

            Line line = new Line(x, y + nodeRadius, rightX, childY - (18 * scale));
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2 * scale);
            canvas.getChildren().add(line);

            Text edgeText = new Text((x + rightX) / 2 - (13 * scale), (y + childY) / 2, node.right.getFractionString());
            edgeText.setStyle("-fx-font-size: " + (9 * scale) + "px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        return levelWidth;
    }

    private void setupExpandedZoom(ScrollPane scrollPane) {
        final double[] expandedScale = { 1.0 };
        final double EXPANDED_SCALE_DELTA = 0.1;
        final double EXPANDED_MAX_SCALE = 3.0;
        final double EXPANDED_MIN_SCALE = 0.3;

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();

                double zoomFactor = (event.getDeltaY() > 0) ? 1 + EXPANDED_SCALE_DELTA : 1 - EXPANDED_SCALE_DELTA;
                double newScale = expandedScale[0] * zoomFactor;

                newScale = Math.max(EXPANDED_MIN_SCALE, Math.min(EXPANDED_MAX_SCALE, newScale));

                if (scrollPane.getContent() != null) {

                    double mouseX = event.getX();
                    double mouseY = event.getY();

                    double contentWidth = scrollPane.getContent().getBoundsInParent().getWidth();
                    double contentHeight = scrollPane.getContent().getBoundsInParent().getHeight();

                    double hValue = scrollPane.getHvalue();
                    double vValue = scrollPane.getVvalue();

                    double xRatio = (hValue * contentWidth + mouseX) / contentWidth;
                    double yRatio = (vValue * contentHeight + mouseY) / contentHeight;

                    scrollPane.getContent().setScaleX(newScale);
                    scrollPane.getContent().setScaleY(newScale);
                    expandedScale[0] = newScale;

                    double newContentWidth = contentWidth * (newScale / expandedScale[0]);
                    double newContentHeight = contentHeight * (newScale / expandedScale[0]);

                    double newHValue = (xRatio * newContentWidth - mouseX) / newContentWidth;
                    double newVValue = (yRatio * newContentHeight - mouseY) / newContentHeight;

                    scrollPane.setHvalue(Math.max(0, Math.min(1, newHValue)));
                    scrollPane.setVvalue(Math.max(0, Math.min(1, newVValue)));
                }
            }
        });
    }

    private void resetExpandedZoom(ScrollPane scrollPane, Pane canvas) {
        if (scrollPane.getContent() != null) {
            scrollPane.getContent().setScaleX(1.0);
            scrollPane.getContent().setScaleY(1.0);
            centerExpandedView(scrollPane, canvas);
        }
    }

    private Pane createExpandedTreeCanvas() {

        double expandedWidth = canvasWidth * 2.5;
        double expandedHeight = canvasHeight * 2.5;

        Pane expandedCanvas = new Pane();
        expandedCanvas.setPrefSize(expandedWidth, expandedHeight);
        expandedCanvas.setMinSize(expandedWidth, expandedHeight);
        expandedCanvas.setMaxSize(expandedWidth, expandedHeight);
        expandedCanvas.setStyle("-fx-background-color: white;");

        drawTreeOnExpandedCanvas(expandedCanvas, expandedWidth, expandedHeight);

        return expandedCanvas;
    }

    private void drawTreeOnExpandedCanvas(Pane canvas, double width, double height) {
        double initialX = width / 2;
        double initialY = 150;

        double expandedLevelWidth = width * 0.4;

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (digitalRoot != null) {
                    drawDigitalTreeExpanded(canvas, digitalRoot, initialX, initialY, expandedLevelWidth);
                }
                break;
            case "Arboles de busqueda por residuos":
                if (residueRoot != null) {
                    drawResidueTreeExpanded(canvas, residueRoot, initialX, initialY, expandedLevelWidth * 0.6);
                }
                break;
            case "Arboles de busqueda por residuos multiple":
                if (multipleResidueRoot != null) {
                    drawMultipleResidueTreeExpanded(canvas, multipleResidueRoot, initialX, initialY, expandedLevelWidth,
                            0);
                }
                break;
            case "Arboles de Huffman":
                if (huffmanRoot != null) {
                    drawHuffmanTreeExpanded(canvas, huffmanRoot, initialX, initialY, expandedLevelWidth * 0.6);
                }
                break;
            default:
                break;
        }
    }

    private double drawDigitalTreeExpanded(Pane canvas, DigitalNode node, double x, double y, double levelWidth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor = Color.WHITE;
        Color strokeColor = Color.BLACK;

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        double nodeRadius = 25;
        Circle circle = new Circle(x, y, nodeRadius);
        circle.setFill(nodeColor);
        circle.setStroke(strokeColor);
        circle.setStrokeWidth(2.5);
        canvas.getChildren().add(circle);

        String text = (node.letter != null) ? String.valueOf(node.letter) : "";
        Text nodeText = new Text(x - 6, y + 7, text);
        nodeText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        canvas.getChildren().add(nodeText);

        double childY = y + 120;
        double totalWidth = 0;

        if (node.left != null) {
            double leftWidth = drawDigitalTreeExpanded(canvas, node.left, x - levelWidth / 2, childY, levelWidth * 0.4);
            totalWidth += leftWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.left)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + nodeRadius, x - levelWidth / 2, childY - 25);
            line.setStroke(lineColor);
            line.setStrokeWidth(2);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x - levelWidth / 4 - 5, (y + childY) / 2, "0");
            edgeText.setFill(lineColor);
            edgeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightWidth = drawDigitalTreeExpanded(canvas, node.right, x + levelWidth / 2, childY,
                    levelWidth * 0.6);
            totalWidth += rightWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.right)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + nodeRadius, x + levelWidth / 2, childY - 25);
            line.setStroke(lineColor);
            line.setStrokeWidth(2);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x + levelWidth / 4 - 5, (y + childY) / 2, "1");
            edgeText.setFill(lineColor);
            edgeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        return Math.max(levelWidth, totalWidth);
    }

    private double drawResidueTreeExpanded(Pane canvas, ResidueNode node, double x, double y, double levelWidth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor = node.isLink ? Color.LIGHTGRAY : Color.WHITE;
        Color strokeColor = Color.BLACK;

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        double nodeRadius = 25;

        if (node == residueRoot) {
            Circle circle = new Circle(x, y, nodeRadius);
            circle.setFill(nodeColor);
            circle.setStroke(strokeColor);
            circle.setStrokeWidth(2.5);
            canvas.getChildren().add(circle);
            node.circle = circle;
            Text nodeText = new Text(x - 6, y + 7, "");
            nodeText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            canvas.getChildren().add(nodeText);
        } else {
            if (node.isLink) {
                Circle circle = new Circle(x, y, nodeRadius);
                circle.setFill(nodeColor);
                circle.setStroke(strokeColor);
                circle.setStrokeWidth(2.5);
                canvas.getChildren().add(circle);
                node.circle = circle;
                Text nodeText = new Text(x - 6, y + 7, "");
                nodeText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                canvas.getChildren().add(nodeText);
            } else {
                Rectangle rect = new Rectangle(x - 25, y - 25, 50, 50);
                rect.setFill(nodeColor);
                rect.setStroke(strokeColor);
                rect.setStrokeWidth(2.5);
                canvas.getChildren().add(rect);

                Circle circle = new Circle(x, y, nodeRadius);
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(Color.TRANSPARENT);
                canvas.getChildren().add(circle);
                node.circle = circle;

                String text = (node.letter != null) ? String.valueOf(node.letter) : "";
                Text nodeText = new Text(x - 6, y + 7, text);
                nodeText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                canvas.getChildren().add(nodeText);
            }
        }

        double childY = y + 120;
        double totalWidth = 0;

        if (node.left != null) {
            double leftWidth = drawResidueTreeExpanded(canvas, node.left, x - levelWidth / 2, childY, levelWidth * 0.6);
            totalWidth += leftWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.left)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + nodeRadius, x - levelWidth / 2, childY - 25);
            line.setStroke(lineColor);
            line.setStrokeWidth(2);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x - levelWidth / 4 - 5, (y + childY) / 2, "0");
            edgeText.setFill(lineColor);
            edgeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightWidth = drawResidueTreeExpanded(canvas, node.right, x + levelWidth / 2, childY,
                    levelWidth * 0.6);
            totalWidth += rightWidth;

            Color lineColor = Color.BLACK;
            if (isSearchInProgress && currentSearchPath.contains(node) && currentSearchPath.contains(node.right)) {
                lineColor = Color.GREEN;
            }

            Line line = new Line(x, y + nodeRadius, x + levelWidth / 2, childY - 25);
            line.setStroke(lineColor);
            line.setStrokeWidth(2);
            canvas.getChildren().add(line);

            Text edgeText = new Text(x + levelWidth / 4 - 5, (y + childY) / 2, "1");
            edgeText.setFill(lineColor);
            edgeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            canvas.getChildren().add(edgeText);
        }

        return Math.max(levelWidth, totalWidth);
    }

    private double drawMultipleResidueTreeExpanded(Pane canvas, MultipleResidueNode node, double x, double y,
            double levelWidth, int depth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor = node.isLink ? Color.LIGHTGRAY : Color.WHITE;
        Color strokeColor = Color.BLACK;

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        double nodeRadius = 25;

        if (node.isLink) {
            Circle circle = new Circle(x, y, nodeRadius);
            circle.setFill(nodeColor);
            circle.setStroke(strokeColor);
            circle.setStrokeWidth(2.5);
            canvas.getChildren().add(circle);
            node.circle = circle;
            Text nodeText = new Text(x - 6, y + 7, "");
            nodeText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            nodeText.setFill(strokeColor);
            canvas.getChildren().add(nodeText);
        } else {
            Rectangle rect = new Rectangle(x - 25, y - 25, 50, 50);
            rect.setFill(nodeColor);
            rect.setStroke(strokeColor);
            rect.setStrokeWidth(2.5);
            canvas.getChildren().add(rect);

            Circle circle = new Circle(x, y, nodeRadius);
            circle.setFill(Color.TRANSPARENT);
            circle.setStroke(Color.TRANSPARENT);
            canvas.getChildren().add(circle);
            node.circle = circle;

            String text = (node.letter != null) ? String.valueOf(node.letter) : "";
            Text nodeText = new Text(x - 6, y + 7, text);
            nodeText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            nodeText.setFill(strokeColor);
            canvas.getChildren().add(nodeText);
        }

        List<Integer> nonNullChildren = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                nonNullChildren.add(i);
            }
        }

        if (nonNullChildren.isEmpty()) {
            return 100;
        }

        double totalChildWidth = 0;
        List<Double> childWidths = new ArrayList<>();

        for (int childIndex : nonNullChildren) {
            double childTreeWidth = calculateSubtreeWidth(node.children[childIndex], depth + 1);
            childWidths.add(childTreeWidth);
            totalChildWidth += childTreeWidth;
        }

        double horizontalSpacing = 100;
        totalChildWidth += horizontalSpacing * (nonNullChildren.size() - 1);

        if (totalChildWidth > levelWidth) {
            levelWidth = totalChildWidth;
        }

        double verticalSpacing = 150;
        double childY = y + verticalSpacing;
        double currentX = x - (totalChildWidth / 2);

        double maxChildWidth = 0;
        int childCount = 0;

        for (int childIndex = 0; childIndex < 4; childIndex++) {
            if (node.children[childIndex] != null) {
                double childTreeWidth = childWidths.get(childCount);
                double childX = currentX + (childTreeWidth / 2);

                if (childCount > 0) {
                    double previousChildRight = currentX - horizontalSpacing;
                    if (childX - (childTreeWidth / 2) < previousChildRight) {
                        childX = previousChildRight + (childTreeWidth / 2) + horizontalSpacing;
                    }
                }

                Color lineColor = Color.BLACK;
                if (isSearchInProgress && currentSearchPath.contains(node)
                        && currentSearchPath.contains(node.children[childIndex])) {
                    lineColor = Color.GREEN;
                }

                Line line = new Line(x, y + nodeRadius, childX, childY - 25);
                line.setStroke(lineColor);
                line.setStrokeWidth(2);
                canvas.getChildren().add(line);

                String label = getEdgeLabel(childIndex, depth);
                Text edgeText = new Text((x + childX) / 2 - 8, (y + childY) / 2, label);
                edgeText.setFill(lineColor);
                edgeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                canvas.getChildren().add(edgeText);

                double actualChildWidth = drawMultipleResidueTreeExpanded(canvas, node.children[childIndex],
                        childX, childY, childTreeWidth, depth + 1);

                maxChildWidth = Math.max(maxChildWidth, actualChildWidth);
                currentX += childTreeWidth + horizontalSpacing;
                childCount++;
            }
        }

        return Math.max(levelWidth, maxChildWidth);
    }

    private double drawHuffmanTreeExpanded(Pane canvas, HuffmanNode node, double x, double y, double levelWidth) {
        if (node == null)
            return 0;

        node.x = x;
        node.y = y;

        Color nodeColor, strokeColor;
        if (node.isLeaf()) {
            nodeColor = Color.WHITE;
            strokeColor = Color.BLACK;
        } else {
            nodeColor = Color.LIGHTGRAY;
            strokeColor = Color.BLACK;
        }

        if (isSearchInProgress && currentSearchPath.contains(node)) {
            nodeColor = Color.LIGHTGREEN;
            strokeColor = Color.DARKGREEN;
        }

        double nodeRadius = 30;
        Circle circle = new Circle(x, y, nodeRadius);
        circle.setFill(nodeColor);
        circle.setStroke(strokeColor);
        circle.setStrokeWidth(3);
        canvas.getChildren().add(circle);

        String text = node.getDisplayText();
        Text nodeText = new Text(x - (text.length() * 4), y + 7, text);
        nodeText.setStyle("-fx-font-size: " + (node.isLeaf() ? "16" : "14") + "px; -fx-font-weight: bold;");
        canvas.getChildren().add(nodeText);

        double childY = y + 120;
        double childWidth = levelWidth * 0.5;

        if (node.left != null) {
            double leftX = x - levelWidth / 2;
            drawHuffmanTreeExpanded(canvas, node.left, leftX, childY, childWidth);

            Line line = new Line(x, y + nodeRadius, leftX, childY - 30);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);
            canvas.getChildren().add(line);

            Text edgeText = new Text((x + leftX) / 2 - 12, (y + childY) / 2,
                    node.left.getFractionString());
            edgeText.setFill(Color.BLACK);
            edgeText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightX = x + levelWidth / 2;
            drawHuffmanTreeExpanded(canvas, node.right, rightX, childY, childWidth);

            Line line = new Line(x, y + nodeRadius, rightX, childY - 30);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);
            canvas.getChildren().add(line);

            Text edgeText = new Text((x + rightX) / 2 - 12, (y + childY) / 2,
                    node.right.getFractionString());
            edgeText.setFill(Color.BLACK);
            edgeText.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            canvas.getChildren().add(edgeText);
        }

        return levelWidth;
    }

    private void centerExpandedView(ScrollPane scrollPane, Pane canvas) {
        if (canvas == null)
            return;

        double rootX = canvas.getPrefWidth() / 2;
        double rootY = 250;

        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double canvasScaledWidth = canvas.getPrefWidth() * scrollPane.getContent().getScaleX();
        double canvasScaledHeight = canvas.getPrefHeight() * scrollPane.getContent().getScaleY();

        double hValue = (rootX * scrollPane.getContent().getScaleX() - viewportWidth / 2)
                / (canvasScaledWidth - viewportWidth);
        double vValue = (rootY * scrollPane.getContent().getScaleY() - viewportHeight / 2)
                / (canvasScaledHeight - viewportHeight);

        hValue = Math.max(0, Math.min(1, hValue));
        vValue = Math.max(0, Math.min(1, vValue));

        scrollPane.setHvalue(hValue);
        scrollPane.setVvalue(vValue);
    }

    @FXML
    private void saveTree() {
        if (undoStack.isEmpty()) {
            notificationText.setText("No hay estado para guardar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Estado del arbol");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado arbol", "*.tree"));
        File file = fileChooser.showSaveDialog(treePane.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                Map<String, Object> saveData = new HashMap<>();
                saveData.put("treeType", this.treeString);
                saveData.put("state", undoStack.peek());

                oos.writeObject(saveData);
                notificationText.setText("Estado guardado en: " + file.getName());
            } catch (IOException e) {
                notificationText.setText("Error al guardar el archivo: " + e.getMessage());
                e.printStackTrace();
            }
        }

        restartTree();
    }

    @FXML
    private void loadTree() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Estado del arbol");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Documents"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de estado arbol", "*.tree"));
        File file = fileChooser.showOpenDialog(treePane.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object loadedData = ois.readObject();

                if (loadedData instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> saveData = (Map<String, Object>) loadedData;
                    String savedTreeType = (String) saveData.get("treeType");
                    TreeState loadedState = (TreeState) saveData.get("state");

                    if (!savedTreeType.equals(this.treeString)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error de Carga");
                        alert.setHeaderText("Tipo de arbol incompatible");
                        alert.setContentText("El archivo fue guardado con el tipo de arbol: " + savedTreeType +
                                "\nPero actualmente esta seleccionado: " + this.treeString +
                                "\n\nSeleccione el tipo de arbol correcto antes de cargar el archivo.");
                        alert.showAndWait();
                        notificationText.setText("Error: Tipo de arbol incompatible");
                        return;
                    }

                    undoStack.clear();
                    redoStack.clear();
                    undoStack.push(loadedState);

                    restoreState(loadedState);
                    updateUndoRedoButtons();

                    updateItemsText();
                    updateTreeVisualization();
                    focusOnRootWithDelay();

                    updateButtonStates();

                    saveButton.setDisable(false);
                    notificationText.setText("Estado cargado desde: " + file.getName());
                }
            } catch (IOException | ClassNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Carga");
                alert.setHeaderText("No se pudo cargar el archivo");
                alert.setContentText("El archivo seleccionado no es valido o esta corrupto: " + e.getMessage());
                alert.showAndWait();
                notificationText.setText("Archivo no valido o corrupto");
                e.printStackTrace();
            }
        }
    }

    private void focusOnRootWithDelay() {
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> {
            focusOnRoot();
        });
        pause.play();
    }

    private void focusOnRoot() {
        Pane canvas = (Pane) treePane.getContent();
        if (canvas == null) {
            focusOnRootWithDelay();
            return;
        }

        double rootX = canvasWidth / 2;
        double rootY = 100;

        double viewportWidth = treePane.getViewportBounds().getWidth();
        double viewportHeight = treePane.getViewportBounds().getHeight();

        double hValue = (rootX - viewportWidth / 2) / (canvasWidth - viewportWidth);
        double vValue = (rootY - viewportHeight / 2) / (canvasHeight - viewportHeight);

        hValue = Math.max(0, Math.min(1, hValue));
        vValue = Math.max(0, Math.min(1, vValue));

        treePane.setHvalue(hValue);
        treePane.setVvalue(vValue);

        treePane.requestFocus();
    }

    private Circle findCircleAt(double x, double y) {
        Pane canvas = (Pane) treePane.getContent();
        if (canvas == null)
            return null;

        for (javafx.scene.Node node : canvas.getChildren()) {
            if (node instanceof Circle) {
                Circle circle = (Circle) node;
                if (Math.abs(circle.getCenterX() - x) < 20 && Math.abs(circle.getCenterY() - y) < 20) {
                    return circle;
                }
            }
        }
        return null;
    }

    @FXML
    private void undoAction() {
        if (undoStack.size() <= 1)
            return;

        TreeState currentState = new TreeState(digitalInsertionOrder, residueInsertionOrder,
                multipleResidueInsertionOrder, huffmanMessage);
        redoStack.push(currentState);

        undoStack.pop();
        TreeState previousState = undoStack.peek();
        restoreState(previousState);
        updateUndoRedoButtons();

        if (undoStack.size() == 1) {
            saveButton.setDisable(true);
        } else {
            saveButton.setDisable(false);
        }
    }

    @FXML
    private void redoAction() {
        if (redoStack.isEmpty())
            return;

        TreeState state = redoStack.pop();

        TreeState currentState = new TreeState(digitalInsertionOrder, residueInsertionOrder,
                multipleResidueInsertionOrder, huffmanMessage);
        undoStack.push(currentState);

        restoreState(state);
        updateUndoRedoButtons();

        saveButton.setDisable(false);
    }

    @FXML
    private void restartTree() {
        switch (treeString) {
            case "Arboles de busqueda digital":
                digitalRoot = null;
                digitalInsertionOrder.clear();
                break;
            case "Arboles de busqueda por residuos":
                residueRoot = new ResidueNode(true, null);
                residueInsertionOrder.clear();
                break;
            case "Arboles de busqueda por residuos multiple":
                multipleResidueInsertionOrder.clear();
                multipleResidueRoot = new MultipleResidueNode(true, null);
                break;
            case "Arboles de Huffman":
                huffmanRoot = null;
                huffmanCodes.clear();
                huffmanMessage = "";
                break;
        }

        undoStack.clear();
        redoStack.clear();
        saveState();
        updateTreeVisualization();
        focusOnRoot();
        updateItemsText();
        notificationText.setText("Arbol reiniciado.");

        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        modDeleteItem.setDisable(true);
        saveButton.setDisable(true);
    }
}