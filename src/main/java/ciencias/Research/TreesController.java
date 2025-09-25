package ciencias.Research;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import ciencias.ResearchController;
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

public class TreesController {
    // Elementos FXML
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
    private ScrollPane treePane;

    private String treeString;
    private ResearchController researchController;

    // Estructuras de datos para los diferentes tipos de árboles
    private DigitalNode digitalRoot;
    private ResidueNode residueRoot;
    private MultipleResidueNode multipleResidueRoot;
    private HuffmanNode huffmanRoot;

    // Para manejar undo/redo
    private Deque<TreeState> undoStack = new ArrayDeque<>();
    private Deque<TreeState> redoStack = new ArrayDeque<>();

    // Para Huffman
    private Map<Character, String> huffmanCodes = new HashMap<>();
    private String huffmanMessage = "";

    private double currentScale = 1.0;
    private final double SCALE_DELTA = 0.1;
    private final double MAX_SCALE = 3.0;
    private final double MIN_SCALE = 0.5;

    private Timeline animationTimeline;
    private final int ANIMATION_DELAY = 500;

    // Clase interna para el estado de los árboles (para undo/redo)
    private class TreeState {
        DigitalNode digitalState;
        ResidueNode residueState;
        MultipleResidueNode multipleResidueState;
        HuffmanNode huffmanState;
        String message;

        TreeState(DigitalNode digital, ResidueNode residue, MultipleResidueNode multiple, HuffmanNode huffman,
                String msg) {
            this.digitalState = digital != null ? cloneDigitalTree(digital) : null;
            this.residueState = residue != null ? cloneResidueTree(residue) : null;
            this.multipleResidueState = multiple != null ? cloneMultipleResidueTree(multiple) : null;
            this.huffmanState = huffman != null ? cloneHuffmanTree(huffman) : null;
            this.message = msg;
        }
    }

    // Clases para los nodos de los diferentes árboles
    private class DigitalNode {
        Character letter;
        DigitalNode left;
        DigitalNode right;
        List<Integer> path; // Para visualización
        double x, y;

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
        List<Integer> path; // Para visualización

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
        List<Integer> path; // Para visualización

        MultipleResidueNode(boolean isLink, Character letter) {
            this.isLink = isLink;
            this.letter = letter;
            this.children = new MultipleResidueNode[4]; // 00, 01, 10, 11
            this.path = new ArrayList<>();
        }
    }

    private class HuffmanNode implements Comparable<HuffmanNode> {
        Character letter;
        int frequency;
        HuffmanNode left;
        HuffmanNode right;
        List<Integer> path; // Para visualización

        HuffmanNode(Character letter, int frequency) {
            this.letter = letter;
            this.frequency = frequency;
            this.left = null;
            this.right = null;
            this.path = new ArrayList<>();
        }

        @Override
        public int compareTo(HuffmanNode other) {
            return this.frequency - other.frequency;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }
    }

    public void initData() {
        if (researchController == null)
            return;
        treeString = researchController.getTreesString();
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
                residueRoot = new ResidueNode(true, null); // Raíz siempre es de enlace
                break;
            case "Arboles de busqueda por residuos multiple":
                treesTitle.setText("Arboles de busqueda por residuos multiple");
                treesDescription.setText(
                        "En cada nivel se toman varios bits de la clave a la vez: el nodo se bifurca en tantas ramas como combinaciones de esos bits.");
                multipleResidueRoot = new MultipleResidueNode(true, null);
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
                // Ocultar botones de undo/redo para Huffman
                undoButton.setVisible(false);
                redoButton.setVisible(false);
                break;
            default:
                break;
        }

        updateTreeVisualization();
        saveState(); // Guardar estado inicial
    }

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    // Métodos para clonar árboles (para undo/redo)
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

        HuffmanNode clone = new HuffmanNode(original.letter, original.frequency);
        clone.left = cloneHuffmanTree(original.left);
        clone.right = cloneHuffmanTree(original.right);
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
        updateTreeVisualization();
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(undoStack.size() <= 1);
        redoButton.setDisable(redoStack.isEmpty());
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
    }

    private void setupZoomAndScroll() {
        treePane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                // Zoom con Ctrl + Rueda del ratón
                event.consume();

                double zoomFactor = (event.getDeltaY() > 0) ? 1 + SCALE_DELTA : 1 - SCALE_DELTA;
                double newScale = currentScale * zoomFactor;

                // Limitar el zoom entre los valores máximo y mínimo
                newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

                if (treePane.getContent() != null) {
                    // Calcular el punto de zoom relativo al ratón
                    double mouseX = event.getX();
                    double mouseY = event.getY();

                    // Obtener las dimensiones actuales
                    double hValue = treePane.getHvalue();
                    double vValue = treePane.getVvalue();

                    // Aplicar la nueva escala
                    treePane.getContent().setScaleX(newScale);
                    treePane.getContent().setScaleY(newScale);
                    currentScale = newScale;

                    // Ajustar la posición para mantener el zoom centrado en el ratón
                    treePane.setHvalue(hValue * (currentScale / newScale));
                    treePane.setVvalue(vValue * (currentScale / newScale));
                }
            }
            // Si no se presiona Ctrl, permitir el scroll normal (vertical y horizontal)
            // No consumir el evento para permitir el desplazamiento normal
        });

        // Asegurar que el contenido siempre tenga la escala actual
        treePane.contentProperty().addListener((obs, oldContent, newContent) -> {
            if (newContent != null) {
                newContent.setScaleX(currentScale);
                newContent.setScaleY(currentScale);
            }
        });

        // Configurar políticas de scroll
        treePane.setFitToWidth(false);
        treePane.setFitToHeight(false);
        treePane.setPannable(true); // Habilitar desplazamiento con arrastre del ratón
    }

    private void setupKeyboardPan() {
        treePane.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                double delta = 50; // Ajusta este valor para cambiar la velocidad de desplazamiento
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

        // Asegurar que el ScrollPane pueda recibir foco
        treePane.setFocusTraversable(true);

        // Opcional: Enfocar el ScrollPane al hacer clic en él
        treePane.setOnMouseClicked(event -> treePane.requestFocus());
    }

    // Método auxiliar para convertir letra a binario (5 bits)
    private String letterToBinary(char letter) {
        if (letter < 'A' || letter > 'Z') {
            return null;
        }

        int value = letter - 'A' + 1;
        String binary = Integer.toBinaryString(value);

        // Asegurar que tenga 5 bits
        while (binary.length() < 5) {
            binary = "0" + binary;
        }

        return binary;
    }

    // Método auxiliar para validar y normalizar entrada
    private Character validateInput(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        // Tomar solo el primer carácter
        char c = input.charAt(0);

        // Convertir a mayúscula si es minúscula
        if (c >= 'a' && c <= 'z') {
            c = Character.toUpperCase(c);
        }

        // Validar que sea una letra de la A a la Z
        if (c < 'A' || c > 'Z') {
            return null;
        }

        return c;
    }

    // Métodos para inserción en diferentes tipos de árboles
    @FXML
    private void insertTree() {
        // Para Huffman, se espera un mensaje completo
        if (treeString.equals("Arboles de Huffman")) {
            String message = newItemTree.getText();
            if (message == null || message.isEmpty()) {
                itemsTreeText.setText("Para Huffman, ingrese un mensaje.");
                return;
            }
            buildHuffmanTree(message);
            itemsTreeText.setText("Árbol de Huffman construido para el mensaje: " + message);
            saveState();
            updateTreeVisualization();
            newItemTree.clear();
            return;
        }

        Character letter = validateInput(newItemTree.getText());
        if (letter == null) {
            itemsTreeText.setText("Entrada inválida. Solo letras de la A a la Z.");
            return;
        }

        boolean inserted = false;
        List<Integer> path = new ArrayList<>();

        switch (treeString) {
            case "Arboles de busqueda digital":
                inserted = insertDigital(letter, path);
                break;
            case "Arboles de busqueda por residuos":
                inserted = insertResidue(letter, path);
                break;
            case "Arboles de busqueda por residuos multiple":
                inserted = insertMultipleResidue(letter, path);
                break;
            default:
                break;
        }

        if (inserted) {
            itemsTreeText.setText("Letra '" + letter + "' insertada correctamente.");
            saveState();
            updateTreeVisualization();
            highlightPath(path, Color.YELLOW);
            // Habilitar búsqueda y eliminación después de la primera inserción
            searchButton.setDisable(false);
            deleteButton.setDisable(false);
            modDeleteItem.setDisable(false);
        } else {
            itemsTreeText.setText("La letra '" + letter + "' ya existe en el árbol.");
        }

        newItemTree.clear();
    }

    private boolean insertDigital(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null)
            return false;

        // Si el árbol está vacío, la primera letra se convierte en la raíz
        if (digitalRoot == null) {
            digitalRoot = new DigitalNode(letter);
            path.add(0); // raíz
            return true;
        }

        DigitalNode current = digitalRoot;
        int bitIndex = 0;

        // Recorremos el árbol hasta encontrar una posición libre
        while (true) {
            // Si el nodo actual no tiene letra, asignamos aquí
            if (current.letter == null) {
                current.letter = letter;
                return true;
            }

            // Si es duplicado, no se inserta
            if (current.letter.equals(letter)) {
                return false;
            }

            // Si aún no se acabaron los bits seguimos recorriendo
            if (bitIndex < binary.length()) {
                char bit = binary.charAt(bitIndex);
                if (bit == '0') {
                    path.add(0);
                    if (current.left == null) {
                        current.left = new DigitalNode(letter);
                        return true;
                    } else {
                        current = current.left;
                    }
                } else { // bit == '1'
                    path.add(1);
                    if (current.right == null) {
                        current.right = new DigitalNode(letter);
                        return true;
                    } else {
                        current = current.right;
                    }
                }
                bitIndex++;
            } else {
                // Si ya no hay bits pero el lugar está ocupado, seguimos bajando a la izquierda
                // por defecto
                path.add(0);
                if (current.left == null) {
                    current.left = new DigitalNode(letter);
                    return true;
                } else {
                    current = current.left;
                }
            }
        }
    }

    private boolean insertResidue(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null)
            return false;

        if (residueRoot == null) {
            // La raíz siempre es de enlace
            residueRoot = new ResidueNode(true, null);
        }

        path.add(0); // raíz siempre es de enlace
        return insertResidueRecursive(residueRoot, letter, binary, 0, path);
    }

    private boolean insertResidueRecursive(ResidueNode node, Character letter, String binary, int index,
            List<Integer> path) {

        // Si llegamos al final del código binario
        if (index >= binary.length()) {
            if (node.letter != null) {
                // Si ya hay una letra igual → duplicado
                if (node.letter.equals(letter)) {
                    return false;
                }

                // Si hay otra letra distinta, convertir nodo en enlace
                Character existingLetter = node.letter;
                String existingBinary = letterToBinary(existingLetter);
                node.letter = null;
                node.isLink = true;

                // Reinsertar la letra existente en este subárbol
                insertResidueRecursive(node, existingLetter, existingBinary, index, new ArrayList<>());
            }

            // Insertar la nueva letra en este nodo
            node.letter = letter;
            node.isLink = false;
            return true;
        }

        // Si este nodo es de datos y tiene una letra distinta, convertirlo en enlace
        if (!node.isLink && node.letter != null && !node.letter.equals(letter)) {
            Character existingLetter = node.letter;
            String existingBinary = letterToBinary(existingLetter);

            node.letter = null;
            node.isLink = true;

            // Reinsertar la letra existente desde este mismo índice
            insertResidueRecursive(node, existingLetter, existingBinary, index, new ArrayList<>());
        }

        // Seguir el recorrido según el bit actual
        char bit = binary.charAt(index);
        if (bit == '0') {
            if (node.left == null) {
                node.left = new ResidueNode(true, null); // nuevo enlace
            }
            path.add(0);
            return insertResidueRecursive(node.left, letter, binary, index + 1, path);
        } else {
            if (node.right == null) {
                node.right = new ResidueNode(true, null); // nuevo enlace
            }
            path.add(1);
            return insertResidueRecursive(node.right, letter, binary, index + 1, path);
        }
    }

    private boolean insertMultipleResidue(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null)
            return false;

        path.add(0); // Raíz
        return insertMultipleResidueRecursive(multipleResidueRoot, letter, binary, 0, path);
    }

    private boolean insertMultipleResidueRecursive(MultipleResidueNode node, Character letter, String binary, int index,
            List<Integer> path) {
        if (index >= binary.length()) {
            if (node.letter != null)
                return false;
            node.letter = letter;
            node.isLink = false;
            return true;
        }

        if (!node.isLink && node.letter != null) {
            Character existingLetter = node.letter;
            node.letter = null;
            node.isLink = true;
            String existingBinary = letterToBinary(existingLetter);
            insertMultipleResidueRecursive(node, existingLetter, existingBinary, index, new ArrayList<>());
        }

        // Tomar 2 bits siempre que sea posible
        int bitsToTake = Math.min(2, binary.length() - index);
        String bits = binary.substring(index, index + bitsToTake);
        int childIndex = Integer.parseInt(bits, 2);

        if (node.children[childIndex] == null) {
            node.children[childIndex] = new MultipleResidueNode(true, null);
        }

        path.add(childIndex);
        return insertMultipleResidueRecursive(node.children[childIndex], letter, binary, index + bitsToTake, path);
    }

    // Métodos para búsqueda en diferentes tipos de árboles
    @FXML
    private void searchTree() {
        if (treeString.equals("Arboles de Huffman")) {
            Character letter = validateInput(modDeleteItem.getText());
            if (letter == null) {
                itemsTreeText.setText("Entrada inválida. Solo letras de la A a la Z.");
                return;
            }
            if (huffmanCodes.containsKey(letter)) {
                itemsTreeText.setText("Letra '" + letter + "' encontrada. Código: " + huffmanCodes.get(letter));
            } else {
                itemsTreeText.setText("Letra '" + letter + "' no encontrada en el mensaje.");
            }
            return;
        }

        Character letter = validateInput(modDeleteItem.getText());
        if (letter == null) {
            itemsTreeText.setText("Entrada inválida. Solo letras de la A a la Z.");
            return;
        }

        boolean found = false;
        List<Integer> path = new ArrayList<>();

        switch (treeString) {
            case "Arboles de busqueda digital":
                found = searchDigital(letter, path);
                break;
            case "Arboles de busqueda por residuos":
                found = searchResidue(letter, path);
                break;
            case "Arboles de busqueda por residuos multiple":
                found = searchMultipleResidue(letter, path);
                break;
            default:
                break;
        }

        if (found) {
            itemsTreeText.setText("Letra '" + letter + "' encontrada.");
            highlightPath(path, Color.GREEN);
        } else {
            itemsTreeText.setText("Letra '" + letter + "' no encontrada.");
        }
    }

    private boolean searchDigital(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || digitalRoot == null)
            return false;

        DigitalNode current = digitalRoot;
        path.add(0);
        int bitIndex = 0;

        while (current != null) {
            // Si el nodo actual tiene una letra y coincide, retornar true
            if (current.letter != null && current.letter.equals(letter)) {
                return true;
            }
            // Si hemos procesado todos los bits, y no coincide, retornar false
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

        path.add(0); // Raíz
        return searchResidueRecursive(residueRoot, letter, binary, 0, path);
    }

    private boolean searchResidueRecursive(ResidueNode node, Character letter, String binary, int index,
            List<Integer> path) {
        if (index >= binary.length()) {
            return letter.equals(node.letter);
        }

        if (node.isLink || node.letter == null) {
            char bit = binary.charAt(index);
            if (bit == '0') {
                if (node.left == null)
                    return false;
                path.add(0); // Izquierda
                return searchResidueRecursive(node.left, letter, binary, index + 1, path);
            } else {
                if (node.right == null)
                    return false;
                path.add(1); // Derecha
                return searchResidueRecursive(node.right, letter, binary, index + 1, path);
            }
        } else {
            return letter.equals(node.letter);
        }
    }

    private boolean searchMultipleResidue(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || multipleResidueRoot == null)
            return false;

        path.add(0); // Raíz
        return searchMultipleResidueRecursive(multipleResidueRoot, letter, binary, 0, path);
    }

    private boolean searchMultipleResidueRecursive(MultipleResidueNode node, Character letter, String binary, int index,
            List<Integer> path) {
        if (index >= binary.length()) {
            return letter.equals(node.letter);
        }

        if (node.isLink || node.letter == null) {
            if (index + 1 >= binary.length()) {
                // Último bit individual
                char bit = binary.charAt(index);
                int childIndex = (bit == '0') ? 0 : 1;

                if (node.children[childIndex] == null)
                    return false;
                path.add(childIndex);
                return searchMultipleResidueRecursive(node.children[childIndex], letter, binary, index + 1, path);
            } else {
                // Dos bits
                String twoBits = binary.substring(index, index + 2);
                int childIndex = Integer.parseInt(twoBits, 2);

                if (node.children[childIndex] == null)
                    return false;
                path.add(childIndex);
                return searchMultipleResidueRecursive(node.children[childIndex], letter, binary, index + 2, path);
            }
        } else {
            return letter.equals(node.letter);
        }
    }

    // Métodos para eliminación en diferentes tipos de árboles
    @FXML
    private void deleteTree() {
        if (treeString.equals("Arboles de Huffman")) {
            itemsTreeText.setText("No se puede eliminar letras de un árbol de Huffman.");
            return;
        }

        Character letter = validateInput(modDeleteItem.getText());
        if (letter == null) {
            itemsTreeText.setText("Entrada inválida. Solo letras de la A a la Z.");
            return;
        }

        boolean deleted = false;
        List<Integer> path = new ArrayList<>();

        switch (treeString) {
            case "Arboles de busqueda digital":
                deleted = deleteDigital(letter, path);
                break;
            case "Arboles de busqueda por residuos":
                deleted = deleteResidue(letter, path);
                break;
            case "Arboles de busqueda por residuos multiple":
                deleted = deleteMultipleResidue(letter, path);
                break;
            default:
                break;
        }

        if (deleted) {
            itemsTreeText.setText("Letra '" + letter + "' eliminada.");
            saveState();
            highlightPath(path, Color.RED);
        } else {
            itemsTreeText.setText("Letra '" + letter + "' no encontrada para eliminar.");
        }

        modDeleteItem.clear();
    }

    private boolean deleteDigital(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || digitalRoot == null)
            return false;

        path.add(0); // Raíz
        return deleteDigitalRecursive(digitalRoot, null, letter, binary, 0, path, false);
    }

    private boolean deleteDigitalRecursive(DigitalNode node, DigitalNode parent, Character letter,
            String binary, int index, List<Integer> path, boolean isLeft) {
        if (node == null)
            return false;

        if (index >= binary.length() || (node.letter != null && node.letter.equals(letter))) {
            if (node.letter != null && node.letter.equals(letter)) {
                // Encontrado el nodo a eliminar
                if (node.left != null) {
                    // Subir el hijo izquierdo
                    DigitalNode leftChild = node.left;
                    node.letter = leftChild.letter;
                    node.left = leftChild.left;
                    node.right = leftChild.right;
                } else if (node.right != null) {
                    // Subir el hijo derecho si no hay izquierdo
                    DigitalNode rightChild = node.right;
                    node.letter = rightChild.letter;
                    node.left = rightChild.left;
                    node.right = rightChild.right;
                } else {
                    // Nodo hoja - eliminar
                    if (parent != null) {
                        if (isLeft) {
                            parent.left = null;
                        } else {
                            parent.right = null;
                        }
                    } else {
                        digitalRoot = null;
                    }
                }
                return true;
            }
            return false;
        }

        char bit = binary.charAt(index);
        if (bit == '0') {
            if (node.left == null)
                return false;
            path.add(0);
            return deleteDigitalRecursive(node.left, node, letter, binary, index + 1, path, true);
        } else {
            if (node.right == null)
                return false;
            path.add(1);
            return deleteDigitalRecursive(node.right, node, letter, binary, index + 1, path, false);
        }
    }

    private boolean deleteResidue(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || residueRoot == null)
            return false;

        path.add(0); // Raíz
        return deleteResidueRecursive(residueRoot, null, letter, binary, 0, path, false);
    }

    private boolean deleteResidueRecursive(ResidueNode node, ResidueNode parent, Character letter,
            String binary, int index, List<Integer> path, boolean isLeft) {
        if (node == null)
            return false;

        if (index >= binary.length()) {
            if (node.letter != null && node.letter.equals(letter)) {
                // Encontrado el nodo a eliminar
                if (parent != null) {
                    // Buscar un hermano para subir
                    ResidueNode sibling = isLeft ? parent.right : parent.left;

                    if (sibling != null && sibling.letter != null) {
                        // Subir el hermano al nodo de enlace
                        parent.letter = sibling.letter;
                        parent.isLink = false;

                        // Eliminar el hermano
                        if (isLeft) {
                            parent.right = null;
                        } else {
                            parent.left = null;
                        }
                    } else {
                        // No hay hermanos con datos, simplemente eliminar este nodo
                        if (isLeft) {
                            parent.left = null;
                        } else {
                            parent.right = null;
                        }
                    }
                } else {
                    // Es la raíz - simplemente eliminar la letra
                    node.letter = null;
                    node.isLink = true;
                }
                return true;
            }
            return false;
        }

        char bit = binary.charAt(index);
        if (bit == '0') {
            if (node.left == null)
                return false;
            path.add(0);
            return deleteResidueRecursive(node.left, node, letter, binary, index + 1, path, true);
        } else {
            if (node.right == null)
                return false;
            path.add(1);
            return deleteResidueRecursive(node.right, node, letter, binary, index + 1, path, false);
        }
    }

    private boolean deleteMultipleResidue(Character letter, List<Integer> path) {
        String binary = letterToBinary(letter);
        if (binary == null || multipleResidueRoot == null)
            return false;

        path.add(0); // Raíz
        return deleteMultipleResidueRecursive(multipleResidueRoot, letter, binary, 0, path);
    }

    private boolean deleteMultipleResidueRecursive(MultipleResidueNode node, Character letter, String binary, int index,
            List<Integer> path) {
        if (index >= binary.length()) {
            if (!letter.equals(node.letter)) {
                return false;
            }

            node.letter = null;
            return true;
        }

        if (node.isLink || node.letter == null) {
            if (index + 1 >= binary.length()) {
                // Último bit individual
                char bit = binary.charAt(index);
                int childIndex = (bit == '0') ? 0 : 1;

                if (node.children[childIndex] == null)
                    return false;
                path.add(childIndex);
                return deleteMultipleResidueRecursive(node.children[childIndex], letter, binary, index + 1, path);
            } else {
                // Dos bits
                String twoBits = binary.substring(index, index + 2);
                int childIndex = Integer.parseInt(twoBits, 2);

                if (node.children[childIndex] == null)
                    return false;
                path.add(childIndex);
                return deleteMultipleResidueRecursive(node.children[childIndex], letter, binary, index + 2, path);
            }
        } else {
            return false;
        }
    }

    // Métodos para Huffman
    private void buildHuffmanTree(String message) {
        // Calcular frecuencias
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : message.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                c = Character.toUpperCase(c);
            }

            if (c >= 'A' && c <= 'Z') {
                frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
            }
        }

        // Crear nodos hoja
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
            queue.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        // Construir árbol
        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();

            HuffmanNode parent = new HuffmanNode(null, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;

            queue.offer(parent);
        }

        huffmanRoot = queue.poll();
        huffmanMessage = message;

        // Generar códigos
        huffmanCodes.clear();
        generateHuffmanCodes(huffmanRoot, "");
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

    private void updateTreeVisualization() {
        Pane canvas = new Pane();
        // Usar un canvas grande y poner la raíz en el centro
        double canvasWidth = 3000;
        double initialX = canvasWidth / 2;
        double initialY = 150;
        double xOffset = 300;

        switch (treeString) {
            case "Arboles de busqueda digital":
                if (digitalRoot != null) {
                    drawDigitalTree(canvas, digitalRoot, initialX, initialY, xOffset);
                }
                break;
            case "Arboles de busqueda por residuos":
                if (residueRoot != null) {
                    drawResidueTree(canvas, residueRoot, initialX, initialY, xOffset);
                }
                break;
            case "Arboles de busqueda por residuos multiple":
                if (multipleResidueRoot != null) {
                    drawMultipleResidueTree(canvas, multipleResidueRoot, initialX, initialY, xOffset);
                }
                break;
            case "Arboles de Huffman":
                if (huffmanRoot != null) {
                    drawHuffmanTree(canvas, huffmanRoot, initialX, initialY, xOffset);
                }
                break;
            default:
                break;
        }

        // Aplicar la escala actual al nuevo contenido
        canvas.setScaleX(currentScale);
        canvas.setScaleY(currentScale);

        // Usar el ancho del canvas grande
        canvas.setMinSize(canvasWidth, 1000);

        canvas.setFocusTraversable(true);

        treePane.setContent(canvas);
        treePane.layout();

        // Ajustar la vista para mostrar la raíz centrada
        treePane.setHvalue(0.5);
        treePane.setVvalue(0);
    }

    private void drawDigitalTree(Pane canvas, DigitalNode node, double x, double y, double xOffset) {
        if (node == null)
            return;

        node.x = x;
        node.y = y;

        // Dibujar el nodo actual
        Circle circle = new Circle(x, y, 15);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        canvas.getChildren().add(circle);

        String text = (node.letter != null) ? String.valueOf(node.letter) : "";
        javafx.scene.text.Text nodeText = new javafx.scene.text.Text(x - 5, y + 5, text);
        canvas.getChildren().add(nodeText);

        // Dibujar los hijos con nuevo espaciado
        double newXOffset = xOffset * 0.6;
        if (node.left != null) {
            double childX = x - newXOffset;
            double childY = y + 50;
            Line line = new Line(x, y + 15, childX, childY - 15);
            canvas.getChildren().add(line);

            // Añadir etiqueta "0" en la mitad de la línea
            Text edgeText = new Text((x + childX) / 2 - 5, (y + childY) / 2, "0");
            canvas.getChildren().add(edgeText);

            drawDigitalTree(canvas, node.left, childX, childY, newXOffset);
        }

        if (node.right != null) {
            double childX = x + newXOffset;
            double childY = y + 50;
            Line line = new Line(x, y + 15, childX, childY - 15);
            canvas.getChildren().add(line);

            // Añadir etiqueta "1" en la mitad de la línea
            Text edgeText = new Text((x + childX) / 2 - 5, (y + childY) / 2, "1");
            canvas.getChildren().add(edgeText);

            drawDigitalTree(canvas, node.right, childX, childY, newXOffset);
        }
    }

    private void drawResidueTree(Pane canvas, ResidueNode node, double x, double y, double xOffset) {
        if (node == null)
            return;

        // Dibujar nodo
        Circle circle = new Circle(x, y, 15);
        circle.setFill(node.isLink ? Color.LIGHTGRAY : Color.WHITE);
        circle.setStroke(Color.BLACK);
        canvas.getChildren().add(circle);

        String text = (node.letter != null) ? String.valueOf(node.letter) : (node.isLink ? "L" : "");
        javafx.scene.text.Text nodeText = new javafx.scene.text.Text(x - 5, y + 5, text);
        canvas.getChildren().add(nodeText);

        // Dibujar hijos con nuevo espaciado
        double newXOffset = xOffset * 0.6; // Factor de reducción ajustado a 0.6
        if (node.left != null) {
            Line line = new Line(x, y + 15, x - newXOffset, y + 50 - 15);
            canvas.getChildren().add(line);
            drawResidueTree(canvas, node.left, x - newXOffset, y + 50, newXOffset);
        }

        if (node.right != null) {
            Line line = new Line(x, y + 15, x + newXOffset, y + 50 - 15);
            canvas.getChildren().add(line);
            drawResidueTree(canvas, node.right, x + newXOffset, y + 50, newXOffset);
        }
    }

    private void drawMultipleResidueTree(Pane canvas, MultipleResidueNode node, double x, double y, double xOffset) {
        if (node == null)
            return;

        // Dibujar el nodo actual
        Circle circle = new Circle(x, y, 15);
        circle.setFill(node.isLink ? Color.LIGHTGRAY : Color.WHITE);
        circle.setStroke(Color.BLACK);
        canvas.getChildren().add(circle);

        String text = (node.letter != null) ? String.valueOf(node.letter) : (node.isLink ? "L" : "");
        javafx.scene.text.Text nodeText = new javafx.scene.text.Text(x - 5, y + 5, text);
        canvas.getChildren().add(nodeText);

        // Dibujar los hijos (4 ramas) con mejor espaciado
        double newXOffset = xOffset * 0.7; // Factor de reducción ajustado a 0.7
        double startX = x - (newXOffset * 2); // Aumentar el espaciado inicial
        double childY = y + 80; // Aumentar la separación vertical

        for (int i = 0; i < 4; i++) {
            if (node.children[i] != null) {
                double childX = startX + (i * newXOffset * 2);

                // Dibujar línea al hijo
                Line line = new Line(x, y + 15, childX, childY - 15);
                canvas.getChildren().add(line);

                // Etiqueta del hijo
                String label = String.format("%02d", i);
                javafx.scene.text.Text edgeText = new javafx.scene.text.Text(
                        (x + childX) / 2 - 5,
                        (y + childY) / 2,
                        label);
                canvas.getChildren().add(edgeText);

                drawMultipleResidueTree(canvas, node.children[i], childX, childY, newXOffset);
            }
        }
    }

    private void drawHuffmanTree(Pane canvas, HuffmanNode node, double x, double y, double xOffset) {
        if (node == null)
            return;

        // Dibujar nodo
        Circle circle = new Circle(x, y, 15);
        circle.setFill(node.isLeaf() ? Color.LIGHTGREEN : Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        canvas.getChildren().add(circle);

        String text = node.isLeaf() ? String.valueOf(node.letter) + ":" + node.frequency
                : String.valueOf(node.frequency);
        javafx.scene.text.Text nodeText = new javafx.scene.text.Text(x - (node.isLeaf() ? 10 : 5), y + 5, text);
        canvas.getChildren().add(nodeText);

        // Dibujar hijos con nuevo espaciado
        double newXOffset = xOffset * 0.6; // Factor de reducción ajustado a 0.6
        if (node.left != null) {
            Line line = new Line(x, y + 15, x - newXOffset, y + 50 - 15);
            canvas.getChildren().add(line);

            // Etiqueta 0
            javafx.scene.text.Text edgeText = new javafx.scene.text.Text(x - newXOffset / 2 - 5, (y + y + 50) / 2, "0");
            canvas.getChildren().add(edgeText);

            drawHuffmanTree(canvas, node.left, x - newXOffset, y + 50, newXOffset);
        }

        if (node.right != null) {
            Line line = new Line(x, y + 15, x + newXOffset, y + 50 - 15);
            canvas.getChildren().add(line);

            // Etiqueta 1
            javafx.scene.text.Text edgeText = new javafx.scene.text.Text(x + newXOffset / 2 - 5, (y + y + 50) / 2, "1");
            canvas.getChildren().add(edgeText);

            drawHuffmanTree(canvas, node.right, x + newXOffset, y + 50, newXOffset);
        }
    }

    private void highlightPath(List<Integer> path, Color color) {
        updateTreeVisualization();
        Pane canvas = (Pane) treePane.getContent();
        if (canvas == null)
            return;

        // Limpiar highlights anteriores
        for (javafx.scene.Node node : canvas.getChildren()) {
            if (node instanceof Circle) {
                Circle circle = (Circle) node;
                circle.setStroke(Color.BLACK);
                circle.setStrokeWidth(1);
            }
        }

        // Determinar las coordenadas iniciales
        double x = treePane.getContent().getBoundsInLocal().getWidth() / 2;
        double y = 50;
        double xOffset = 300;

        // Resaltar nodos en la ruta
        Circle currentNode = findCircleAt(canvas, x, y);
        if (currentNode != null) {
            currentNode.setStroke(color);
            currentNode.setStrokeWidth(3);
        }

        for (int direction : path) {
            xOffset *= 0.6;
            if (direction == 0) {
                x -= xOffset;
            } else {
                x += xOffset;
            }
            y += 50;

            currentNode = findCircleAt(canvas, x, y);
            if (currentNode != null) {
                currentNode.setStroke(color);
                currentNode.setStrokeWidth(3);
            }
        }
    }

    private Circle findCircleAt(Pane canvas, double x, double y) {
        for (javafx.scene.Node node : canvas.getChildren()) {
            if (node instanceof Circle) {
                Circle circle = (Circle) node;
                // Usar un rango de tolerancia más amplio para encontrar el círculo
                if (Math.abs(circle.getCenterX() - x) < 20 && Math.abs(circle.getCenterY() - y) < 20) {
                    return circle;
                }
            }
        }
        return null;
    }

    // Métodos para undo/redo
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

        undoStack.pop(); // Descartar estado actual
        TreeState previousState = undoStack.peek();
        restoreState(previousState);
        updateUndoRedoButtons();
    }

    @FXML
    private void saveTree() {
        // Implementación para guardar el estado del árbol
        itemsTreeText.setText("Funcionalidad de guardado no implementada aún.");
    }

    @FXML
    private void loadTree() {
        // Implementación para cargar el estado del árbol
        itemsTreeText.setText("Funcionalidad de carga no implementada aún.");
    }

    @FXML
    private void restartTree() {
        switch (treeString) {
            case "Arboles de busqueda digital":
                digitalRoot = null;
                break;
            case "Arboles de busqueda por residuos":
                residueRoot = new ResidueNode(true, null);
                break;
            case "Arboles de busqueda por residuos multiple":
                multipleResidueRoot = new MultipleResidueNode(true, null);
                break;
            case "Arboles de Huffman":
                huffmanRoot = null;
                huffmanCodes.clear();
                huffmanMessage = "";
                break;
            default:
                break;
        }

        undoStack.clear();
        redoStack.clear();
        saveState();
        updateTreeVisualization();
        itemsTreeText.setText("Árbol reiniciado.");

        // Deshabilitar búsqueda y eliminación
        searchButton.setDisable(true);
        deleteButton.setDisable(true);
        modDeleteItem.setDisable(true);
    }
}