package ciencias.Research;

import ciencias.ResearchController;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ciencias.ResearchController;
import javafx.scene.shape.Rectangle;
import javafx.animation.FillTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
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

    private class TreeState {
        DigitalNode digitalState;
        ResidueNode residueState;
        MultipleResidueNode multipleResidueState;
        HuffmanNode huffmanState;
        String message;
        List<Character> digitalOrder;
        List<Character> residueOrder;
        List<Character> multipleResidueOrder;

        TreeState(DigitalNode digital, ResidueNode residue, MultipleResidueNode multiple, HuffmanNode huffman,
                String msg) {
            this.digitalState = digital != null ? cloneDigitalTree(digital) : null;
            this.residueState = residue != null ? cloneResidueTree(residue) : null;
            this.multipleResidueState = multiple != null ? cloneMultipleResidueTree(multiple) : null;
            this.huffmanState = huffman != null ? cloneHuffmanTree(huffman) : null;
            this.message = msg;
            this.digitalOrder = new ArrayList<>(digitalInsertionOrder);
            this.residueOrder = new ArrayList<>(residueInsertionOrder);
            this.multipleResidueOrder = new ArrayList<>(multipleResidueInsertionOrder);
        }
    }

    private class DigitalNode {
        Character letter;
        DigitalNode left;
        DigitalNode right;
        List<Integer> path;
        double x, y;
        Circle circle;

        DigitalNode(Character letter) {
            this.letter = letter;
            this.left = null;
            this.right = null;
            this.path = new ArrayList<>();
        }
    }

    private class ResidueNode {
        boolean isLink;
        Character letter;
        ResidueNode left;
        ResidueNode right;
        List<Integer> path;
        double x, y;
        Circle circle;

        ResidueNode(boolean isLink, Character letter) {
            this.isLink = isLink;
            this.letter = letter;
            this.left = null;
            this.right = null;
            this.path = new ArrayList<>();
        }
    }

    private class MultipleResidueNode {
        boolean isLink;
        Character letter;
        MultipleResidueNode[] children;
        List<Integer> path;
        double x, y;
        Circle circle;

        MultipleResidueNode(boolean isLink, Character letter) {
            this.isLink = isLink;
            this.letter = letter;
            this.children = new MultipleResidueNode[4];
            this.path = new ArrayList<>();
        }
    }

    private class HuffmanNode implements Comparable<HuffmanNode> {
        Character letter; // Solo para nodos hoja
        double frequency;
        HuffmanNode left;
        HuffmanNode right;
        List<Integer> path;
        double x, y;
        Circle circle;
        int insertionOrder;
        String nodeType; // "leaf" o "link"

        // Constructor para nodos hoja
        HuffmanNode(Character letter, double frequency, int insertionOrder) {
            this.letter = letter;
            this.frequency = frequency;
            this.insertionOrder = insertionOrder;
            this.left = null;
            this.right = null;
            this.path = new ArrayList<>();
            this.nodeType = "leaf";
        }

        // Constructor para nodos de enlace
        HuffmanNode(HuffmanNode left, HuffmanNode right, double frequency, int insertionOrder) {
            this.letter = null;
            this.frequency = frequency;
            this.insertionOrder = insertionOrder;
            this.left = left;
            this.right = right;
            this.path = new ArrayList<>();
            this.nodeType = "link";
        }

        @Override
public int compareTo(HuffmanNode other) {
    // Primero por frecuencia (menor frecuencia primero)
    int freqCompare = Double.compare(this.frequency, other.frequency);
    if (freqCompare != 0) {
        return freqCompare;
    }
    // Para frecuencias iguales, por orden de inserción (MAYOR orden primero)
    // Esto asegura que los nodos más recientes se combinen primero
    return Integer.compare(other.insertionOrder, this.insertionOrder);
}

        public boolean isLeaf() {
            return "leaf".equals(nodeType);
        }

        @Override
        public String toString() {
            if (isLeaf()) {
                return letter + ":" + String.format("%.3f", frequency);
            }
            return String.format("%.3f", frequency);
        }

        public String getDisplayText() {
            if (isLeaf()) {
                return String.valueOf(letter);
            }
            return String.format("%.3f", frequency);
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

        initializeMultipleResidueTree();

        PauseTransition initialFocus = new PauseTransition(Duration.millis(300));
        initialFocus.setOnFinished(e -> focusOnRootWithDelay());
        initialFocus.play();
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

        switch (treeString) {
            case "Arboles de busqueda digital":
                treesTitle.setText("Arboles de busqueda digital");
                treesDescription.setText(
                        "Todos los bits de la clave se representan explícitamente en el recorrido, aunque haya caminos con nodos de un solo hijo.");
                digitalRoot = null;
                break;
            case "Arboles de busqueda por residuos":
                treesTitle.setText("Arboles de busqueda por residuos");
                treesDescription.setText(
                        "Si varias claves comparten prefijo el nodo se vuelve de enlace y se bifurca, mientras que los nodos hoja.");
                residueRoot = new ResidueNode(true, null);
                break;
            case "Arboles de busqueda por residuos multiple":
                treesTitle.setText("Arboles de busqueda por residuos multiple");
                treesDescription.setText(
                        "En cada nivel se toman varios bits de la clave a la vez: el nodo se bifurca en tantas ramas como combinaciones de esos bits.");
                initializeMultipleResidueTree();
                break;
            case "Tablas de indices":
                treesTitle.setText("Tablas de indices");
                treesDescription.setText(".");
                break;
            case "Arboles de Huffman":
                treesTitle.setText("Arboles de Huffman");
                treesDescription.setText(
                        "Es construido a partir de las frecuencias de aparición de los símbolos donde se toman siempre los dos nodos de menor frecuencia.");
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
                sb.append("Residuos Múltiples: ");
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
                    sb.append("Huffman - Ingrese un mensaje para construir el árbol");
                } else {
                    sb.append("Huffman Mensaje: ").append(huffmanMessage);
                    sb.append(" | Estructura: ").append(getHuffmanTreeStructure(huffmanRoot));
                    sb.append(" | Códigos: ");
                    for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
                        sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
                    }

                    sb.append(" | Frecuencias: ");
                    Map<Character, Double> freqMap = calculateFrequencies(huffmanMessage);
                    for (Map.Entry<Character, Double> entry : freqMap.entrySet()) {
                        sb.append(entry.getKey()).append(":").append(String.format("%.2f", entry.getValue()))
                                .append(" ");
                    }
                }
                treeLengthText
                        .setText("Número de caracteres: " + (huffmanMessage != null ? huffmanMessage.length() : 0));
                break;

            default:
                sb.append("Seleccione un tipo de árbol");
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
            initializeMultipleResidueTree();
        }

        return insertMultipleResidueData(multipleResidueRoot, letter, binary, 0);
    }

    private boolean insertMultipleResidueData(MultipleResidueNode node, Character letter, String binary, int index) {
        if (index >= binary.length()) {
            if (node.isLink && node.letter == null) {
                node.isLink = false;
                node.letter = letter;
                return true;
            } else if (node.letter != null && !node.letter.equals(letter)) {
                return false;
            }
            return node.letter != null && node.letter.equals(letter);
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
            debugHuffmanProcess(message);

            buildHuffmanTree(message);
            updateItemsText();
            saveState();
            updateTreeVisualization();

            newItemTree.clear();
            return;
        }

        Character letter = validateInput(newItemTree.getText());
        if (letter == null) {
            notificationText.setText("Entrada inválida. Solo letras de la A a la Z.");
            return;
        }

        boolean inserted = false;

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (digitalInsertionOrder.contains(letter)) {
                    notificationText.setText("La letra '" + letter + "' ya existe en el árbol.");
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
                    notificationText.setText("La letra '" + letter + "' ya existe en el árbol.");
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
                    notificationText.setText("La letra '" + letter + "' ya existe en el árbol.");
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
                notificationText.setText("Entrada inválida. Solo letras de la A a la Z.");
                return;
            }
            if (huffmanCodes.containsKey(letter)) {
                notificationText.setText("Letra '" + letter + "' encontrada. Código: " + huffmanCodes.get(letter));
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
            notificationText.setText("Entrada inválida. Solo letras de la A a la Z.");
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
            notificationText.setText("No se puede eliminar letras de un árbol de Huffman.");
            return;
        }

        Character letter = validateInput(modDeleteItem.getText());
        if (letter == null) {
            notificationText.setText("Entrada inválida. Solo letras de la A a la Z.");
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

    private DigitalNode cloneDigitalTree(DigitalNode original) {
        if (original == null)
            return null;
        DigitalNode clone = new DigitalNode(original.letter);
        clone.left = cloneDigitalTree(original.left);
        clone.right = cloneDigitalTree(original.right);
        clone.path = new ArrayList<>(original.path);
        return clone;
    }

    private ResidueNode cloneResidueTree(ResidueNode original) {
        if (original == null)
            return null;
        ResidueNode clone = new ResidueNode(original.isLink, original.letter);
        clone.left = cloneResidueTree(original.left);
        clone.right = cloneResidueTree(original.right);
        clone.path = new ArrayList<>(original.path);
        return clone;
    }

    private MultipleResidueNode cloneMultipleResidueTree(MultipleResidueNode original) {
        if (original == null)
            return null;
        MultipleResidueNode clone = new MultipleResidueNode(original.isLink, original.letter);
        for (int i = 0; i < 4; i++) {
            clone.children[i] = cloneMultipleResidueTree(original.children[i]);
        }
        clone.path = new ArrayList<>(original.path);
        return clone;
    }

    private HuffmanNode cloneHuffmanTree(HuffmanNode original) {
        if (original == null)
            return null;
        HuffmanNode clone;
        if (original.isLeaf()) {
            clone = new HuffmanNode(original.letter, original.frequency, original.insertionOrder);
        } else {
            clone = new HuffmanNode(cloneHuffmanTree(original.left), cloneHuffmanTree(original.right),
                    original.frequency, original.insertionOrder);
        }
        clone.path = new ArrayList<>(original.path);
        return clone;
    }

    private void saveState() {
        TreeState state = new TreeState(digitalRoot, residueRoot, multipleResidueRoot, huffmanRoot, huffmanMessage);
        undoStack.push(state);
        redoStack.clear();
        updateUndoRedoButtons();
    }

    private void restoreState(TreeState state) {
        this.digitalRoot = state.digitalState;
        this.residueRoot = state.residueState;
        this.multipleResidueRoot = state.multipleResidueState;
        this.huffmanRoot = state.huffmanState;
        this.huffmanMessage = state.message;
        this.digitalInsertionOrder = new ArrayList<>(state.digitalOrder);
        this.residueInsertionOrder = new ArrayList<>(state.residueOrder);
        this.multipleResidueInsertionOrder = new ArrayList<>(state.multipleResidueOrder);
        updateTreeVisualization();
        updateItemsText();
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

        // Tomar solo el primer carácter
        char c = input.charAt(0);

        // Convertir a mayúsculas si es minúscula
        if (c >= 'a' && c <= 'z') {
            c = Character.toUpperCase(c);
        }

        // Solo permitir letras A-Z
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
        notificationText.setText("Mensaje vacío para construir árbol de Huffman.");
        return;
    }

    // Paso 1: Invertir el mensaje
    String reversedMessage = new StringBuilder(message).reverse().toString();
    huffmanMessage = reversedMessage;

    // Paso 2: Calcular frecuencias
    Map<Character, Integer> charCount = new HashMap<>();
    int totalChars = 0;

    for (char c : reversedMessage.toCharArray()) {
        char upperChar = Character.toUpperCase(c);
        if (upperChar >= 'A' && upperChar <= 'Z') {
            charCount.put(upperChar, charCount.getOrDefault(upperChar, 0) + 1);
            totalChars++;
        }
    }

    if (charCount.isEmpty()) {
        notificationText.setText("El mensaje no contiene letras válidas (A-Z).");
        huffmanRoot = null;
        huffmanCodes.clear();
        updateTreeVisualization();
        return;
    }

    // Paso 3: Crear lista manteniendo orden de aparición en el mensaje invertido
    List<HuffmanNode> nodes = new ArrayList<>();
    int insertionOrder = 0;

    for (char c : reversedMessage.toCharArray()) {
        char upperChar = Character.toUpperCase(c);
        if (upperChar >= 'A' && upperChar <= 'Z' && 
            !containsNodeWithLetter(nodes, upperChar)) {
            double frequency = (double) charCount.get(upperChar) / totalChars;
            nodes.add(new HuffmanNode(upperChar, frequency, insertionOrder++));
        }
    }

    // Paso 4: Ordenar por frecuencia descendente y orden de inserción ascendente
    // Esto coloca los nodos con mayor frecuencia al inicio y para iguales, los más antiguos primero
    nodes.sort((a, b) -> {
        int freqCompare = Double.compare(b.frequency, a.frequency);
        if (freqCompare != 0) {
            return freqCompare;
        }
        return Integer.compare(a.insertionOrder, b.insertionOrder);
    });

    System.out.println("Lista inicial ordenada: " + nodesToString(nodes));

    // Paso 5: Construir árbol tomando siempre los dos últimos
    int currentInsertionOrder = insertionOrder;
    while (nodes.size() > 1) {
        // Tomar los dos últimos elementos (menores frecuencias y más recientes para iguales)
        int lastIndex = nodes.size() - 1;
        HuffmanNode right = nodes.get(lastIndex);
        HuffmanNode left = nodes.get(lastIndex - 1);

        System.out.println("Combinando: " + nodeToString(left) + " + " + nodeToString(right));

        // Remover los dos últimos
        nodes.remove(lastIndex);
        nodes.remove(lastIndex - 1);

        // Crear nuevo nodo padre
        double combinedFrequency = left.frequency + right.frequency;
        HuffmanNode parent = new HuffmanNode(left, right, combinedFrequency, currentInsertionOrder++);

        // Insertar el nuevo nodo manteniendo el orden descendente
        int insertIndex = 0;
        boolean found = false;
        
        // Buscar la posición correcta para insertar
        for (int i = 0; i < nodes.size(); i++) {
            HuffmanNode current = nodes.get(i);
            if (current.frequency > combinedFrequency) {
                continue;
            } else if (current.frequency == combinedFrequency) {
                // Para frecuencias iguales, insertar antes de nodos con menor orden de inserción
                if (current.insertionOrder < parent.insertionOrder) {
                    insertIndex = i;
                    found = true;
                    break;
                }
            } else {
                // current.frequency < combinedFrequency
                insertIndex = i;
                found = true;
                break;
            }
        }
        
        if (!found) {
            insertIndex = nodes.size();
        }
        
        nodes.add(insertIndex, parent);
        System.out.println("Lista después de inserción: " + nodesToString(nodes));
    }

    huffmanRoot = nodes.isEmpty() ? null : nodes.get(0);
    
    huffmanCodes.clear();
    generateHuffmanCodes(huffmanRoot, "");

    showHuffmanTreeInfo();
}

    private String nodesToString(List<HuffmanNode> nodes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(nodeToString(nodes.get(i)));
            if (i < nodes.size() - 1)
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private void debugHuffmanProcess(String message) {
        System.out.println("=== DEBUG HUFFMAN PARA: " + message + " ===");

        // Paso 1: Invertir el mensaje
        String reversedMessage = new StringBuilder(message).reverse().toString();
        System.out.println("Mensaje invertido: " + reversedMessage);

        // Paso 2: Calcular frecuencias
        Map<Character, Integer> charCount = new HashMap<>();
        int totalChars = 0;

        for (char c : reversedMessage.toCharArray()) {
            char upperChar = Character.toUpperCase(c);
            if (upperChar >= 'A' && upperChar <= 'Z') {
                charCount.put(upperChar, charCount.getOrDefault(upperChar, 0) + 1);
                totalChars++;
            }
        }

        System.out.println("Conteo de caracteres: " + charCount);
        System.out.println("Total de caracteres válidos: " + totalChars);

        // Paso 3: Crear lista manualmente
        List<HuffmanNode> nodes = new ArrayList<>();
        int insertionOrder = 0;

        for (char c : reversedMessage.toCharArray()) {
            char upperChar = Character.toUpperCase(c);
            if (upperChar >= 'A' && upperChar <= 'Z' &&
                    !containsNodeWithLetter(nodes, upperChar)) {
                double frequency = (double) charCount.get(upperChar) / totalChars;
                nodes.add(new HuffmanNode(upperChar, frequency, insertionOrder++));
                System.out.println("Agregado nodo: " + upperChar + " - Frecuencia: " + frequency + " - Orden: "
                        + (insertionOrder - 1));
            }
        }

        System.out.println("Lista inicial: " + nodes);

        // Paso 4: Construir árbol paso a paso
        int step = 1;
        while (nodes.size() > 1) {
            System.out.println("\n--- Paso " + step + " ---");
            System.out.println("Lista actual: " + nodes);

            // Tomar los dos últimos
            int lastIndex = nodes.size() - 1;
            HuffmanNode right = nodes.get(lastIndex);
            HuffmanNode left = nodes.get(lastIndex - 1);

            System.out.println("Combinando: " + left + " + " + right);

            nodes.remove(lastIndex);
            nodes.remove(lastIndex - 1);

            double combinedFrequency = left.frequency + right.frequency;
            HuffmanNode parent = new HuffmanNode(left, right, combinedFrequency, insertionOrder++);

            System.out.println("Nuevo nodo: " + parent);

            // Insertar en posición ordenada
            boolean inserted = false;
            for (int i = nodes.size() - 1; i >= 0; i--) {
                if (nodes.get(i).frequency >= combinedFrequency) {
                    nodes.add(i + 1, parent);
                    inserted = true;
                    System.out.println("Insertado en posición: " + (i + 1));
                    break;
                }
            }
            if (!inserted) {
                nodes.add(0, parent);
                System.out.println("Insertado al inicio");
            }

            step++;
        }

        if (!nodes.isEmpty()) {
            System.out.println("\n=== ESTRUCTURA FINAL ===");
            String structure = getHuffmanTreeStructure(nodes.get(0));
            System.out.println("Estructura: " + structure);

            // Generar códigos para verificación
            Map<Character, String> codes = new HashMap<>();
            generateHuffmanCodesDebug(nodes.get(0), "", codes);
            System.out.println("Códigos: " + codes);
        }
    }

    private void generateHuffmanCodesDebug(HuffmanNode node, String code, Map<Character, String> codes) {
        if (node == null)
            return;
        if (node.isLeaf()) {
            codes.put(node.letter, code);
            return;
        }
        generateHuffmanCodesDebug(node.left, code + "0", codes);
        generateHuffmanCodesDebug(node.right, code + "1", codes);
    }

    private String nodeToString(HuffmanNode node) {
        if (node.isLeaf()) {
            return node.letter + ":" + String.format("%.3f", node.frequency);
        } else {
            return "Node:" + String.format("%.3f", node.frequency);
        }
    }

    private boolean containsNodeWithLetter(List<HuffmanNode> nodes, char letter) {
        for (HuffmanNode node : nodes) {
            if (node.isLeaf() && node.letter != null && node.letter == letter) {
                return true;
            }
        }
        return false;
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
            return;
        }

        generateHuffmanCodes(node.left, code + "0");
        generateHuffmanCodes(node.right, code + "1");
    }

    private void showHuffmanTreeInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Árbol de Huffman - Mensaje invertido: ").append(huffmanMessage).append("\n");

        if (huffmanRoot != null) {
            String structure = getHuffmanTreeStructure(huffmanRoot);
            info.append("Estructura: ").append(structure).append("\n");
        }

        Map<Character, Double> frequencies = calculateFrequencies(huffmanMessage);
        info.append("Frecuencias: ");
        for (Map.Entry<Character, Double> entry : frequencies.entrySet()) {
            info.append(entry.getKey()).append(":").append(String.format("%.3f", entry.getValue())).append(" ");
        }

        info.append("\nCódigos Huffman: ");
        for (Map.Entry<Character, String> entry : huffmanCodes.entrySet()) {
            info.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
        }

        notificationText.setText(info.toString());
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

        double initialX = canvasWidth / 2;
        double initialY = 100;

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (digitalRoot != null) {
                    drawDigitalTree(canvas, digitalRoot, initialX, initialY, canvasWidth * 0.7);
                }
                break;
            case "Arboles de busqueda por residuos":
                if (residueRoot != null) {
                    drawResidueTree(canvas, residueRoot, initialX, initialY, canvasWidth * 0.4);
                }
                break;
            case "Arboles de busqueda por residuos multiple":
                if (multipleResidueRoot != null) {
                    drawMultipleResidueTree(canvas, multipleResidueRoot, initialX, initialY, canvasWidth * 0.9, 0);
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
            Text nodeText = new Text(x - 5, y + 5, "L");
            canvas.getChildren().add(nodeText);
        } else {
            if (node.isLink) {
                Circle circle = new Circle(x, y, 20);
                circle.setFill(nodeColor);
                circle.setStroke(strokeColor);
                canvas.getChildren().add(circle);
                node.circle = circle;
                Text nodeText = new Text(x - 5, y + 5, "L");
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
            Text nodeText = new Text(x - 5, y + 5, "L");
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

        // Colores uniformes con los otros árboles
        Color nodeColor, strokeColor;
        if (node.isLeaf()) {
            nodeColor = Color.WHITE;
            strokeColor = Color.BLACK;
        } else {
            nodeColor = Color.LIGHTGRAY;
            strokeColor = Color.BLACK;
        }

        // Resaltado para búsqueda (verde)
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

        // Mostrar solo letras en nodos hoja, frecuencias en nodos de enlace
        String text = node.getDisplayText();
        Text nodeText = new Text(x - (text.length() * 3), y + 5, text);
        nodeText.setStyle("-fx-font-size: " + (node.isLeaf() ? "12" : "10") + "px; -fx-font-weight: bold;");
        canvas.getChildren().add(nodeText);

        double childY = y + 80;
        double childWidth = levelWidth * 0.6;

        if (node.left != null) {
            double leftX = x - levelWidth / 2;
            double leftWidth = drawHuffmanTree(canvas, node.left, leftX, childY, childWidth);

            // Línea negra en lugar de azul
            Line line = new Line(x, y + nodeRadius, leftX, childY - 20);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.5);
            canvas.getChildren().add(line);

            // Etiqueta de frecuencia en negro
            Text edgeText = new Text((x + leftX) / 2 - 8, (y + childY) / 2,
                    String.format("%.3f", node.left.frequency));
            edgeText.setFill(Color.BLACK);
            edgeText.setStyle("-fx-font-weight: bold; -fx-font-size: 9px;");
            canvas.getChildren().add(edgeText);
        }

        if (node.right != null) {
            double rightX = x + levelWidth / 2;
            double rightWidth = drawHuffmanTree(canvas, node.right, rightX, childY, childWidth);

            // Línea negra en lugar de roja
            Line line = new Line(x, y + nodeRadius, rightX, childY - 20);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(1.5);
            canvas.getChildren().add(line);

            // Etiqueta de frecuencia en negro
            Text edgeText = new Text((x + rightX) / 2 - 8, (y + childY) / 2,
                    String.format("%.3f", node.right.frequency));
            edgeText.setFill(Color.BLACK);
            edgeText.setStyle("-fx-font-weight: bold; -fx-font-size: 9px;");
            canvas.getChildren().add(edgeText);
        }

        return levelWidth;
    }

    private void focusOnRootWithDelay() {
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> {
            focusOnRoot();
        });
        pause.play();
    }

    private void focusOnPosition(double x, double y) {
        Pane canvas = (Pane) treePane.getContent();
        if (canvas == null)
            return;

        double viewportWidth = treePane.getViewportBounds().getWidth();
        double viewportHeight = treePane.getViewportBounds().getHeight();

        double hValue = (x - viewportWidth / 2) / (canvasWidth - viewportWidth);
        double vValue = (y - viewportHeight / 2) / (canvasHeight - viewportHeight);

        hValue = Math.max(0, Math.min(1, hValue));
        vValue = Math.max(0, Math.min(1, vValue));

        treePane.setHvalue(hValue);
        treePane.setVvalue(vValue);
        treePane.requestFocus();
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
    private void redoAction() {
        if (redoStack.isEmpty())
            return;

        TreeState state = redoStack.pop();
        undoStack.push(new TreeState(digitalRoot, residueRoot, multipleResidueRoot, huffmanRoot, huffmanMessage));
        restoreState(state);
        updateUndoRedoButtons();
    }

    @FXML
    private void undoAction() {
        if (undoStack.size() <= 1)
            return;

        TreeState currentState = new TreeState(digitalRoot, residueRoot, multipleResidueRoot, huffmanRoot,
                huffmanMessage);
        redoStack.push(currentState);

        undoStack.pop();
        TreeState previousState = undoStack.peek();
        restoreState(previousState);
        updateUndoRedoButtons();
    }

    @FXML
    private void saveTree() {
        notificationText.setText("Funcionalidad de guardado no implementada aún.");
    }

    @FXML
    private void loadTree() {
        notificationText.setText("Funcionalidad de carga no implementada aún.");
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
                initializeMultipleResidueTree();
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
        notificationText.setText("Árbol reiniciado.");

        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        modDeleteItem.setDisable(true);
    }
}