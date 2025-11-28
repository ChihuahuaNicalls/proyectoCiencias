package ciencias.Graphs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.geometry.Point2D;
import java.util.*;
import java.io.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.shape.Path;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.VLineTo;
import javafx.scene.shape.CubicCurve;
import javafx.scene.input.ScrollEvent;

public class OperationsController {

    @FXML
    private ScrollPane graph1;
    @FXML
    private ScrollPane graph2;
    @FXML
    private ScrollPane graphResult;
    @FXML
    private Label titleHash12;
    @FXML
    private MenuButton graphOperate;
    @FXML
    private MenuButton operations;
    @FXML
    private Button operateButton;
    @FXML
    private Text fusionContractionText;
    @FXML
    private Text operationText;
    @FXML
    private TextField fusionContractionItem1;
    @FXML
    private TextField fusionContractionItem2;
    @FXML
    private MenuButton graphModify;
    @FXML
    private TextField vertDelete;
    @FXML
    private Button vertAddButton;
    @FXML
    private Button vertDeleteButton;
    @FXML
    private Spinner<Integer> vertNum;
    @FXML
    private TextField edgeOrg;
    @FXML
    private TextField edgeDest;
    @FXML
    private CheckBox edgeDirection;
    @FXML
    private Button edgeAddButton;
    @FXML
    private Button edgeDeleteButton;
    @FXML
    private Text textWeightNotation;
    @FXML
    private TextField edgeWeightNotation;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button restartButton;
    @FXML
    private Text modificationText;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;
    @FXML
    private Button saveButtonResult;
    @FXML
    private Button isomButton1;
    @FXML
    private Button isomButton2;
    @FXML
    private Button isomButtonResult;
    @FXML
    private CheckBox edgePonderation;
    @FXML
    private Button restartOpButton;

    private Graph graph1Data;
    private Graph graph2Data;
    private Graph graphResultData;

    private Stack<GraphState> graph1History;
    private Stack<GraphState> graph2History;
    private Stack<GraphState> graph1RedoStack;
    private Stack<GraphState> graph2RedoStack;

    private String currentModifyingGraph;
    private String currentOperatingGraph;
    private String currentOperation;

    private static final double VERTEX_RADIUS = 20;
    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 600;

    private double currentScaleGraph1 = 1.0;
    private double currentScaleGraph2 = 1.0;
    private double currentScaleResult = 1.0;
    private final double SCALE_DELTA = 0.1;
    private final double MAX_SCALE = 3.0;
    private final double MIN_SCALE = 0.5;

    private final Color[] EDGE_COLORS = {
            Color.BLACK, Color.RED, Color.ORANGE, Color.GREEN, Color.PURPLE, Color.BROWN
    };

    private final Color[] HOVER_COLORS = {
            Color.GRAY, Color.DARKRED, Color.DARKORANGE, Color.DARKGREEN, Color.DARKMAGENTA, Color.SADDLEBROWN
    };

    private Color getLighter(Color c) {
        if (c == null)
            return Color.LIGHTGRAY;
        return c.interpolate(Color.WHITE, 0.45);
    }

    private Map<String, Point2D> layoutGraph1 = new HashMap<>();
    private Map<String, Point2D> layoutGraph2 = new HashMap<>();
    private Map<String, Point2D> layoutGraphResult = new HashMap<>();

    private class EdgeLabelConnection {
        javafx.scene.shape.Shape edge;
        Text labelPart;
        int edgeIndex;
        javafx.scene.paint.Color normalTextColor;
        javafx.scene.paint.Color hoverTextColor;

        EdgeLabelConnection(javafx.scene.shape.Shape edge, Text labelPart, int edgeIndex,
                javafx.scene.paint.Color normalTextColor, javafx.scene.paint.Color hoverTextColor) {
            this.edge = edge;
            this.labelPart = labelPart;
            this.edgeIndex = edgeIndex;
            this.normalTextColor = normalTextColor;
            this.hoverTextColor = hoverTextColor;
        }
    }

    private void shuffleLayout(Graph g, String graphId) {
        if (g == null || g.vertices.isEmpty())
            return;

        Map<String, Point2D> base = calculateGraphLayout(g, CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2, VERTEX_RADIUS);
        List<String> verts = new ArrayList<>(base.keySet());
        List<Point2D> pts = new ArrayList<>(base.values());
        Collections.shuffle(pts, new Random());

        Map<String, Point2D> map = new HashMap<>();
        for (int i = 0; i < verts.size(); i++) {
            map.put(verts.get(i), pts.get(i));
        }

        if ("graph1".equals(graphId)) {
            layoutGraph1 = map;
        } else if ("graph2".equals(graphId)) {
            layoutGraph2 = map;
        } else {
            layoutGraphResult = map;
        }
    }

    private static class GraphState implements Serializable {
    private static final long serialVersionUID = 1L;
    Set<String> vertices;
    List<Edge> edges;
    boolean isDirected;
    boolean isWeighted;

    public GraphState() {
        this.vertices = new HashSet<>();
        this.edges = new ArrayList<>();
    }

    public GraphState(Set<String> vertices, List<Edge> edges, boolean isDirected, boolean isWeighted) {
        this.vertices = new HashSet<>(vertices);
        this.edges = new ArrayList<>(edges);
        this.isDirected = isDirected;
        this.isWeighted = isWeighted;
    }
}

    private static class Edge implements Serializable {
    private static final long serialVersionUID = 1L;
    String source;
    String destination;
    String label;
    boolean isSumEdge;
    boolean isLoop;

    public Edge() {
        this("", "", "");
    }

    public Edge(String source, String destination, String label) {
        this.source = source;
        this.destination = destination;
        this.label = label;
        this.isSumEdge = false;
        this.isLoop = source.equals(destination);
    }

    public Edge(String source, String destination, String label, boolean isSumEdge) {
        this.source = source;
        this.destination = destination;
        this.label = label;
        this.isSumEdge = isSumEdge;
        this.isLoop = source.equals(destination);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Edge edge = (Edge) obj;
        return source.equals(edge.source) && destination.equals(edge.destination) &&
                label.equals(edge.label) && isSumEdge == edge.isSumEdge;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination, label, isSumEdge);
    }
}

    private class Graph {
        private Set<String> vertices;
        private List<Edge> edges;
        private boolean isDirected;
        private boolean isWeighted;
        private boolean hasEdges;

        public Graph() {
            this.vertices = new HashSet<>();
            this.edges = new ArrayList<>();
            this.isDirected = false;
            this.isWeighted = true;
            this.hasEdges = false;
        }

        public Graph(Set<String> vertices, List<Edge> edges, boolean isDirected, boolean isWeighted) {
            this.vertices = new HashSet<>(vertices);
            this.edges = new ArrayList<>(edges);
            this.isDirected = isDirected;
            this.isWeighted = isWeighted;
            this.hasEdges = !edges.isEmpty();
        }

        public Graph copy() {
            return new Graph(this.vertices, this.edges, this.isDirected, this.isWeighted);
        }

        public GraphState getState() {
            return new GraphState(vertices, edges, isDirected, isWeighted);
        }

        public void setState(GraphState state) {
            this.vertices = new HashSet<>(state.vertices);
            this.edges = new ArrayList<>(state.edges);
            this.isDirected = state.isDirected;
            this.isWeighted = state.isWeighted;
            this.hasEdges = !state.edges.isEmpty();
        }

        public void addVertex(String vertex) {
            vertices.add(vertex);
        }

        public void removeVertex(String vertex) {
            vertices.remove(vertex);
            edges.removeIf(edge -> edge.source.equals(vertex) || edge.destination.equals(vertex));
        }

        public void addEdge(String source, String destination, String label) {
            addEdge(source, destination, label, false);
        }

        public void addEdge(String source, String destination, String label, boolean isSumEdge) {
            // Enforce unique label across the entire graph: no two edges may have the same notation
            // Exception: when performing the "Suma" operation between graphs, duplicate labels are allowed
            if (label != null && !label.trim().isEmpty()) {
                boolean isSumOperation = "Suma".equals(currentOperation);
                for (Edge existingEdge : edges) {
                    if (label.equals(existingEdge.label)) {
                        if (!isSumOperation) {
                            throw new IllegalStateException("No se permiten aristas con la misma notación '" + label + "' en el mismo grafo");
                        } else {
                            // allow duplicate labels during Suma operation
                            break;
                        }
                    }
                }
            }

            List<Edge> edgesBetween = getEdgesBetween(source, destination);
            boolean isOperation = isSumEdge || (currentOperation != null && !currentOperation.isEmpty());
            int maxEdges = isOperation ? 6 : 3;
            int maxLoops = isOperation ? 4 : 2;

            if (source.equals(destination)) {
                if (edgesBetween.size() >= maxLoops) {
                    throw new IllegalStateException(
                            "No se pueden tener más de " + maxLoops + " bucles en el vértice " + source);
                }
            } else {
                if (edgesBetween.size() >= maxEdges && !isOperation) {
                    throw new IllegalStateException(
                            "No se pueden tener más de " + maxEdges + " aristas entre " + source + " y " + destination);
                }
            }

            if (hasEdges) {
                if (isDirected != edgeDirection.isSelected()) {
                    throw new IllegalStateException(
                            "No se pueden mezclar aristas dirigidas y no dirigidas en el mismo grafo");
                }
                if (isWeighted != edgePonderation.isSelected()) {
                    throw new IllegalStateException(
                            "No se pueden mezclar aristas ponderadas y no ponderadas en el mismo grafo");
                }
            } else {
                isDirected = edgeDirection.isSelected();
                isWeighted = edgePonderation.isSelected();
                hasEdges = true;
            }

            edges.add(new Edge(source, destination, label, isSumEdge));
        }

        public void removeEdge(String source, String destination) {
            removeEdge(source, destination, "1", false);
        }

        public void removeEdge(String source, String destination, String label, boolean isSumEdge) {
            if (!isDirected) {
                for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
                    Edge edge = it.next();
                    if ((edge.source.equals(source) && edge.destination.equals(destination) ||
                            edge.source.equals(destination) && edge.destination.equals(source)) &&
                            edge.label.equals(label) && edge.isSumEdge == isSumEdge) {
                        it.remove();
                        break;
                    }
                }
            } else {
                for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
                    Edge edge = it.next();
                    if (edge.source.equals(source) && edge.destination.equals(destination) &&
                            edge.label.equals(label) && edge.isSumEdge == isSumEdge) {
                        it.remove();
                        break;
                    }
                }
            }

            hasEdges = !edges.isEmpty();
        }

        public boolean hasVertex(String vertex) {
            return vertices.contains(vertex);
        }

        public boolean hasEdge(String source, String destination) {
            if (!isDirected) {
                return edges.stream()
                        .anyMatch(edge -> (edge.source.equals(source) && edge.destination.equals(destination)) ||
                                (edge.source.equals(destination) && edge.destination.equals(source)));
            } else {
                return edges.stream()
                        .anyMatch(edge -> edge.source.equals(source) && edge.destination.equals(destination));
            }
        }

        public boolean hasEdgeWithLabel(String source, String destination, String label) {
            if (!isDirected) {
                return edges.stream()
                        .anyMatch(edge -> ((edge.source.equals(source) && edge.destination.equals(destination)) ||
                                (edge.source.equals(destination) && edge.destination.equals(source))) &&
                                edge.label.equals(label));
            } else {
                return edges.stream()
                        .anyMatch(edge -> edge.source.equals(source) && edge.destination.equals(destination) &&
                                edge.label.equals(label));
            }
        }

        public boolean hasLoop(String vertex) {
            return edges.stream().anyMatch(edge -> edge.source.equals(vertex) && edge.destination.equals(vertex));
        }

        public List<Edge> getEdgesBetween(String source, String destination) {
            List<Edge> result = new ArrayList<>();
            if (!isDirected) {
                for (Edge edge : edges) {
                    if ((edge.source.equals(source) && edge.destination.equals(destination)) ||
                            (edge.source.equals(destination) && edge.destination.equals(source))) {
                        result.add(edge);
                    }
                }
            } else {
                for (Edge edge : edges) {
                    if (edge.source.equals(source) && edge.destination.equals(destination)) {
                        result.add(edge);
                    }
                }
            }
            return result;
        }

        public Set<String> getAdjacentVertices(String vertex) {
            Set<String> adjacent = new HashSet<>();
            for (Edge edge : edges) {
                if (edge.source.equals(vertex))
                    adjacent.add(edge.destination);
                if (!isDirected && edge.destination.equals(vertex))
                    adjacent.add(edge.source);
            }
            return adjacent;
        }

        public boolean isEmpty() {
            return vertices.isEmpty();
        }

        public boolean isWeighted() {
            return isWeighted;
        }

        public boolean isDirected() {
            return isDirected;
        }
    }

    @FXML
    private void initialize() {
        graph1Data = new Graph();
        graph2Data = new Graph();
        graphResultData = new Graph();

        graph1History = new Stack<>();
        graph2History = new Stack<>();
        graph1RedoStack = new Stack<>();
        graph2RedoStack = new Stack<>();

        setInitialState();
        setupListeners();

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                Integer.MAX_VALUE, 1);
        vertNum.setValueFactory(valueFactory);

        vertNum.setEditable(true);

        TextFormatter<Integer> textFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {

                return change;
            }
            try {
                int value = Integer.parseInt(newText);
                if (value >= 1) {
                    return change;
                }
            } catch (NumberFormatException e) {

            }
            return null;
        });

        vertNum.getEditor().setTextFormatter(textFormatter);

        vertNum.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {

                javafx.application.Platform.runLater(() -> {
                    if (vertNum.getEditor().getText().isEmpty()) {
                        vertNum.getValueFactory().setValue(1);
                        vertNum.getEditor().setText("1");
                    }
                });
            }
        });

        vertNum.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String text = vertNum.getEditor().getText();
                if (text == null || text.isEmpty()) {
                    vertNum.getValueFactory().setValue(1);
                    vertNum.getEditor().setText("1");
                } else {
                    try {
                        int value = Integer.parseInt(text);
                        if (value < 1) {
                            vertNum.getValueFactory().setValue(1);
                            vertNum.getEditor().setText("1");
                        }
                    } catch (NumberFormatException e) {
                        vertNum.getValueFactory().setValue(1);
                        vertNum.getEditor().setText("1");
                    }
                }
            }
        });

        vertNum.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue < 1) {
                vertNum.getValueFactory().setValue(1);
            }
        });

        setupSpinnerArrows();

        edgePonderation.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateWeightNotation();
        });

        updateWeightNotation();

        setupZoomAndScroll(graph1, "graph1");
        setupZoomAndScroll(graph2, "graph2");
        setupZoomAndScroll(graphResult, "result");

        updateOperationState();
    }

    private void setupSpinnerArrows() {

        vertNum.increment();

        vertNum.getEditor().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    vertNum.increment();
                    event.consume();
                    break;
                case DOWN:
                    vertNum.decrement();
                    event.consume();
                    break;
            }
        });

        vertNum.setStyle("-fx-background-color: white; -fx-border-color: #cccccc;");
    }

    private void setupZoomAndScroll(ScrollPane scrollPane, String graphType) {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();

                double zoomFactor = (event.getDeltaY() > 0) ? 1 + SCALE_DELTA : 1 - SCALE_DELTA;
                double newScale = getCurrentScale(graphType) * zoomFactor;
                newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale));

                if (scrollPane.getContent() != null) {
                    double hValue = scrollPane.getHvalue();
                    double vValue = scrollPane.getVvalue();

                    scrollPane.getContent().setScaleX(newScale);
                    scrollPane.getContent().setScaleY(newScale);
                    setCurrentScale(graphType, newScale);

                    scrollPane.setHvalue(hValue);
                    scrollPane.setVvalue(vValue);
                }
            }
        });

        scrollPane.contentProperty().addListener((obs, oldContent, newContent) -> {
            if (newContent != null) {
                double scale = getCurrentScale(graphType);
                newContent.setScaleX(scale);
                newContent.setScaleY(scale);
            }
        });

        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);

        setupKeyboardPan(scrollPane);
    }

    private void setupKeyboardPan(ScrollPane scrollPane) {
        scrollPane.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                double delta = 50;
                switch (event.getCode()) {
                    case LEFT:
                        scrollPane.setHvalue(scrollPane.getHvalue() - delta / scrollPane.getWidth());
                        break;
                    case RIGHT:
                        scrollPane.setHvalue(scrollPane.getHvalue() + delta / scrollPane.getWidth());
                        break;
                    case UP:
                        scrollPane.setVvalue(scrollPane.getVvalue() - delta / scrollPane.getHeight());
                        break;
                    case DOWN:
                        scrollPane.setVvalue(scrollPane.getVvalue() + delta / scrollPane.getHeight());
                        break;
                    default:
                        break;
                }
                event.consume();
            }
        });

        scrollPane.setFocusTraversable(true);
        scrollPane.setOnMouseClicked(event -> scrollPane.requestFocus());
    }

    private double getCurrentScale(String graphType) {
        switch (graphType) {
            case "graph1":
                return currentScaleGraph1;
            case "graph2":
                return currentScaleGraph2;
            case "result":
                return currentScaleResult;
            default:
                return 1.0;
        }
    }

    private void setCurrentScale(String graphType, double scale) {
        switch (graphType) {
            case "graph1":
                currentScaleGraph1 = scale;
                break;
            case "graph2":
                currentScaleGraph2 = scale;
                break;
            case "result":
                currentScaleResult = scale;
                break;
        }
    }

    private void updateWeightNotation() {
        if (edgePonderation.isSelected()) {
            textWeightNotation.setText("Peso:");
            edgeWeightNotation.setPromptText("Ej: 1");
        } else {
            textWeightNotation.setText("Notacion:");
            edgeWeightNotation.setPromptText("Ej: a");
        }
    }

    private void setInitialState() {
        setModificationControlsEnabled(false);
        setOperationControlsEnabled(false);

        fusionContractionText.setVisible(false);
        fusionContractionItem1.setVisible(false);
        fusionContractionItem2.setVisible(false);

        setupGraphModifyMenu();
        setupGraphOperateMenu();
        setupOperationsMenu();

        modificationText.setText("");
        operationText.setText("");

        updateOperationState();

        if (saveButtonResult != null) {
            saveButtonResult.setDisable(true);
        }
    }

    private void setModificationControlsEnabled(boolean enabled) {
        vertNum.setDisable(!enabled);
        vertAddButton.setDisable(!enabled);
        vertDelete.setDisable(!enabled);
        vertDeleteButton.setDisable(!enabled);
        edgeOrg.setDisable(!enabled);
        edgeDest.setDisable(!enabled);
        edgeDirection.setDisable(!enabled);
        edgeAddButton.setDisable(!enabled);
        edgeDeleteButton.setDisable(!enabled);
        edgeWeightNotation.setDisable(!enabled);
        edgePonderation.setDisable(!enabled);
        undoButton.setDisable(!enabled);
        redoButton.setDisable(!enabled);
        restartButton.setDisable(!enabled);
        saveButton.setDisable(!enabled);
    }

    private void setOperationControlsEnabled(boolean enabled) {
        graphOperate.setDisable(!enabled);
        operations.setDisable(!enabled);
        operateButton.setDisable(!enabled || currentOperation == null);
        restartOpButton.setDisable(!enabled);

        if (!enabled) {
            graphOperate.setText("Elegir opcion");
            operations.setText("Elegir operacion");
            currentOperatingGraph = null;
            currentOperation = null;
            fusionContractionText.setVisible(false);
            fusionContractionItem1.setVisible(false);
            fusionContractionItem2.setVisible(false);
        }
    }

    private void setupGraphModifyMenu() {
        for (MenuItem item : graphModify.getItems()) {
            item.setOnAction(e -> {
                currentModifyingGraph = item.getText();
                graphModify.setText(currentModifyingGraph);
                setModificationControlsEnabled(true);
                saveGraphState();
                modificationText.setText("Modificando: " + currentModifyingGraph);
            });
        }
    }

    private void setupGraphOperateMenu() {
        for (MenuItem item : graphOperate.getItems()) {
            item.setOnAction(e -> {
                currentOperatingGraph = item.getText();
                graphOperate.setText(currentOperatingGraph);
                updateOperationsMenu();
                operationText.setText("Operando sobre: " + currentOperatingGraph);

                fusionContractionText.setVisible(false);
                fusionContractionItem1.setVisible(false);
                fusionContractionItem2.setVisible(false);
                fusionContractionItem1.clear();
                fusionContractionItem2.clear();

                updateOperationState();
            });
        }
    }

    private void setupOperationsMenu() {
        updateOperationsMenuForSingleGraph();
    }

    private void updateOperationsMenu() {
        if ("Entre los 2 grafos".equals(currentOperatingGraph)) {
            updateOperationsMenuForTwoGraphs();
        } else {
            updateOperationsMenuForSingleGraph();
        }
    }

    private void updateOperationsMenuForSingleGraph() {
        operations.getItems().clear();
        String[] singleGraphOperations = {
                "Grafo Linea", "Complemento", "Fusion Vertices", "Contraccion Aristas"
        };
        for (String op : singleGraphOperations) {
            MenuItem item = new MenuItem(op);
            item.setOnAction(e -> handleOperationSelection(op));
            operations.getItems().add(item);
        }
        operations.setText("Elegir operacion");
    }

    private void updateOperationsMenuForTwoGraphs() {
        operations.getItems().clear();
        String[] twoGraphOperations = {
                "Union", "Interseccion", "Suma anillo", "Suma",
                "Producto Cartesiano", "Producto Tensorial", "Composicion entre grafos"
        };
        for (String op : twoGraphOperations) {
            MenuItem item = new MenuItem(op);
            item.setOnAction(e -> handleOperationSelection(op));
            operations.getItems().add(item);
        }
        operations.setText("Elegir operacion");
    }

    private void handleOperationSelection(String operation) {
        currentOperation = operation;
        operations.setText(operation);

        boolean showFusionContraction = operation.equals("Fusion Vertices") ||
                operation.equals("Contraccion Aristas");
        fusionContractionText.setVisible(showFusionContraction);
        fusionContractionItem1.setVisible(showFusionContraction);
        fusionContractionItem2.setVisible(showFusionContraction);

        if (operation.equals("Fusion Vertices")) {
            fusionContractionText.setText("Seleccionar vertices a fusionar:");
        } else if (operation.equals("Contraccion Aristas")) {
            fusionContractionText.setText("Seleccionar arista a contraer (v1 v2):");
        }

        operationText.setText("Operación seleccionada: " + operation);
        updateOperationState();
    }

    @FXML
    private void vertAdd() {
        int numVertices = vertNum.getValue();
        Graph currentGraph = getCurrentModifyingGraph();

        if (currentGraph == null) {
            modificationText.setText("Error: Seleccione un grafo para modificar");
            return;
        }

        saveGraphState();

        List<String> addedVertices = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            String vertex = generateNextVertexName(currentGraph);
            currentGraph.addVertex(vertex);
            addedVertices.add(vertex);
        }

        updateGraphDisplay();
        modificationText.setText("Añadidos vértices: " + String.join(", ", addedVertices));
        updateOperationState();
    }

    @FXML
    private void vertDelete() {
        String vertex = vertDelete.getText().trim().toUpperCase();
        Graph currentGraph = getCurrentModifyingGraph();

        if (currentGraph == null) {
            modificationText.setText("Error: Seleccione un grafo para modificar");
            return;
        }

        if (vertex.isEmpty()) {
            modificationText.setText("Error: Ingrese un vértice a eliminar");
            return;
        }

        if (!currentGraph.hasVertex(vertex)) {
            modificationText.setText("Error: El vértice " + vertex + " no existe");
            return;
        }

        saveGraphState();
        currentGraph.removeVertex(vertex);
        updateGraphDisplay();
        modificationText.setText("Vértice " + vertex + " eliminado");
        updateOperationState();
    }

    @FXML
    private void edgeAdd() {
        String source = edgeOrg.getText().trim().toUpperCase();
        String destination = edgeDest.getText().trim().toUpperCase();
        String weightText = edgeWeightNotation.getText().trim();
        Graph currentGraph = getCurrentModifyingGraph();

        if (currentGraph == null) {
            modificationText.setText("Error: Seleccione un grafo para modificar");
            return;
        }

        if (source.isEmpty()) {
            modificationText.setText("Error: Ingrese al menos el vértice origen");
            return;
        }

        if (destination.isEmpty()) {
            destination = source;
        }

        if (!currentGraph.hasVertex(source) || !currentGraph.hasVertex(destination)) {
            modificationText.setText("Error: Los vértices deben existir");
            return;
        }

        if (source.equals(destination)) {
            List<Edge> loops = currentGraph.getEdgesBetween(source, source);
            boolean isOperation = currentOperation != null && !currentOperation.isEmpty();
            int maxLoops = isOperation ? 4 : 2;
            if (loops.size() >= maxLoops) {
                modificationText
                        .setText("Error: No se pueden tener más de " + maxLoops + " bucles en el vértice " + source);
                return;
            }
        }

        String label;
        if (edgePonderation.isSelected()) {
            if (weightText.isEmpty()) {
                label = "1";
            } else {
                try {
                    double weight = Double.parseDouble(weightText);
                    if (weight <= 0) {
                        modificationText.setText("Error: El peso debe ser positivo");
                        return;
                    }
                    label = weightText;
                } catch (NumberFormatException e) {
                    modificationText.setText("Error: Peso debe ser un número");
                    return;
                }
            }
        } else {
            if (weightText.isEmpty()) {
                label = "";
            } else {
                if (weightText.length() != 1 || !Character.isLetter(weightText.charAt(0))) {
                    modificationText.setText("Error: La notación debe ser una letra (a-z)");
                    return;
                }
                label = weightText.toLowerCase();
            }
        }

        try {
            saveGraphState();
            currentGraph.addEdge(source, destination, label);
            updateGraphDisplay();
            String labelDisplay = label.isEmpty() ? "sin etiqueta" : label;
            String edgeType = source.equals(destination) ? "Bucle en " + source : source + " → " + destination;
            modificationText.setText("Arista añadida: " + edgeType +
                    (edgePonderation.isSelected() ? " (peso: " + labelDisplay + ")"
                            : " (notación: " + labelDisplay + ")"));
            updateOperationState();
        } catch (IllegalStateException e) {
            modificationText.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void edgeDelete() {
        String source = edgeOrg.getText().trim().toUpperCase();
        String destination = edgeDest.getText().trim().toUpperCase();
        String weightText = edgeWeightNotation.getText().trim();
        Graph currentGraph = getCurrentModifyingGraph();

        if (currentGraph == null) {
            modificationText.setText("Error: Seleccione un grafo para modificar");
            return;
        }

        if (source.isEmpty()) {
            modificationText.setText("Error: Ingrese al menos el vértice origen");
            return;
        }

        if (destination.isEmpty()) {
            destination = source;
        }

        String label;
        if (edgePonderation.isSelected()) {
            if (weightText.isEmpty()) {
                label = "1";
            } else {
                try {
                    Double.parseDouble(weightText);
                    label = weightText;
                } catch (NumberFormatException e) {
                    modificationText.setText("Error: Peso debe ser un número");
                    return;
                }
            }
        } else {
            if (weightText.isEmpty()) {
                label = "";
            } else {
                label = weightText.toLowerCase();
            }
        }

        List<Edge> edgesBetween = currentGraph.getEdgesBetween(source, destination);
        if (edgesBetween.isEmpty()) {
            modificationText.setText("Error: No existe arista entre " + source + " y " + destination);
            return;
        }

        boolean found = false;
        for (Edge edge : edgesBetween) {
            if (edge.label.equals(label)) {
                found = true;
                break;
            }
        }

        if (!found) {
            modificationText.setText("Error: No existe arista con la " +
                    (edgePonderation.isSelected() ? "peso" : "notación") + " especificada");
            return;
        }

        saveGraphState();
        currentGraph.removeEdge(source, destination, label, false);
        updateGraphDisplay();
        modificationText.setText("Arista eliminada: " + source + " → " + destination);
        updateOperationState();
    }

    @FXML
    private void operate() {
        if (currentOperation == null) {
            operationText.setText("Error: Seleccione una operación");
            return;
        }

        if ("Entre los 2 grafos".equals(currentOperatingGraph)) {
            if (graph1Data.isEmpty() || graph2Data.isEmpty()) {
                operationText.setText("Error: Ambos grafos deben contener vértices para operar entre ellos");
                return;
            }

            if (graph1Data.isDirected() != graph2Data.isDirected()) {
                operationText.setText("Error: No se pueden operar grafos con diferente tipo de dirección");
                return;
            }
            if (graph1Data.isWeighted() != graph2Data.isWeighted()) {
                operationText.setText("Error: No se pueden operar grafos con diferente tipo de ponderación");
                return;
            }
        } else {
            Graph targetGraph = "Grafo 1".equals(currentOperatingGraph) ? graph1Data : graph2Data;
            if (targetGraph.isEmpty()) {
                operationText.setText("Error: El grafo operado está vacío");
                return;
            }
        }

        try {
            if ("Entre los 2 grafos".equals(currentOperatingGraph)) {
                performTwoGraphOperation();
            } else {
                performSingleGraphOperation();
            }
            updateResultGraphDisplay();
            operationText.setText("Operación completada: " + currentOperation);
        } catch (Exception e) {
            operationText.setText("Error en operación: " + e.getMessage());
        }
    }

    private void performSingleGraphOperation() {
        Graph targetGraph = "Grafo 1".equals(currentOperatingGraph) ? graph1Data : graph2Data;

        switch (currentOperation) {
            case "Grafo Linea":
                graphResultData = createLineGraph(targetGraph);
                break;
            case "Complemento":
                graphResultData = createComplementGraph(targetGraph);
                break;
            case "Fusion Vertices":
                String v1 = fusionContractionItem1.getText().trim().toUpperCase();
                String v2 = fusionContractionItem2.getText().trim().toUpperCase();
                if (v1.isEmpty() || v2.isEmpty()) {
                    throw new IllegalArgumentException("Ingrese ambos vértices para fusionar");
                }
                graphResultData = fuseVertices(targetGraph, v1, v2);
                break;
            case "Contraccion Aristas":
                String e1 = fusionContractionItem1.getText().trim().toUpperCase();
                String e2 = fusionContractionItem2.getText().trim().toUpperCase();
                if (e1.isEmpty() || e2.isEmpty()) {
                    throw new IllegalArgumentException("Ingrese ambos vértices de la arista a contraer");
                }
                graphResultData = contractEdge(targetGraph, e1, e2);
                break;
            default:
                throw new UnsupportedOperationException("Operación no implementada: " + currentOperation);
        }
    }

    private void performTwoGraphOperation() {
        switch (currentOperation) {
            case "Union":
                graphResultData = union(graph1Data, graph2Data);
                break;
            case "Interseccion":
                graphResultData = intersection(graph1Data, graph2Data);
                break;
            case "Suma anillo":
                graphResultData = ringSum(graph1Data, graph2Data);
                break;
            case "Suma":
                graphResultData = sum(graph1Data, graph2Data);
                break;
            case "Producto Cartesiano":
                graphResultData = cartesianProduct(graph1Data, graph2Data);
                break;
            case "Producto Tensorial":
                graphResultData = tensorProduct(graph1Data, graph2Data);
                break;
            case "Composicion entre grafos":
                graphResultData = composition(graph1Data, graph2Data);
                break;
            default:
                throw new UnsupportedOperationException("Operación no implementada: " + currentOperation);
        }
    }

    private Graph createLineGraph(Graph original) {
        Graph lineGraph = new Graph();
        lineGraph.isDirected = original.isDirected;
        lineGraph.isWeighted = original.isWeighted;

        Map<String, String> edgeToVertex = new HashMap<>();
        int edgeCounter = 1;

        for (Edge edge : original.edges) {
            if (!edge.isLoop) {
                String vertexName = edge.source + edge.destination;
                if (lineGraph.hasVertex(vertexName)) {
                    vertexName = edge.source + edge.destination + edgeCounter;
                }

                lineGraph.addVertex(vertexName);
                edgeToVertex.put(edge.source + "," + edge.destination, vertexName);
                if (!original.isDirected) {
                    edgeToVertex.put(edge.destination + "," + edge.source, vertexName);
                }
                edgeCounter++;
            }
        }

        Set<String> addedEdges = new HashSet<>();

        for (Edge edge1 : original.edges) {
            for (Edge edge2 : original.edges) {
                if (edge1 == edge2 || edge1.isLoop || edge2.isLoop)
                    continue;

                String vertex1 = edgeToVertex.get(edge1.source + "," + edge1.destination);
                String vertex2 = edgeToVertex.get(edge2.source + "," + edge2.destination);

                if (vertex1 != null && vertex2 != null && !vertex1.equals(vertex2)) {
                    String sharedVertex = null;
                    if (edge1.source.equals(edge2.source))
                        sharedVertex = edge1.source;
                    else if (edge1.source.equals(edge2.destination))
                        sharedVertex = edge1.source;
                    else if (edge1.destination.equals(edge2.source))
                        sharedVertex = edge1.destination;
                    else if (edge1.destination.equals(edge2.destination))
                        sharedVertex = edge1.destination;

                    if (sharedVertex != null) {
                        String edgeKey = vertex1.compareTo(vertex2) < 0 ? vertex1 + "|" + vertex2
                                : vertex2 + "|" + vertex1;

                        if (!addedEdges.contains(edgeKey)) {
                            lineGraph.addEdge(vertex1, vertex2, sharedVertex.toLowerCase());
                            addedEdges.add(edgeKey);
                        }
                    }
                }
            }
        }

        return lineGraph;
    }

    private Graph createComplementGraph(Graph original) {
        Graph complement = new Graph();
        complement.isDirected = original.isDirected;
        complement.isWeighted = original.isWeighted;

        complement.vertices.addAll(original.vertices);

        for (String v1 : original.vertices) {
            for (String v2 : original.vertices) {

                if (!v1.equals(v2)) {
                    if (!original.isDirected && v1.compareTo(v2) > 0) {
                        continue;
                    }

                    if (!original.hasEdge(v1, v2)) {
                        complement.addEdge(v1, v2, "");
                    }
                }
            }
        }

        return complement;
    }

    private Graph fuseVertices(Graph original, String v1, String v2) {
        if (!original.hasVertex(v1) || !original.hasVertex(v2)) {
            throw new IllegalArgumentException("Los vértices deben existir en el grafo");
        }

        Graph result = new Graph();
        result.isDirected = original.isDirected;
        result.isWeighted = original.isWeighted;
        String fusedVertex = v1 + v2;

        for (String vertex : original.vertices) {
            if (!vertex.equals(v1) && !vertex.equals(v2)) {
                result.addVertex(vertex);
            }
        }
        result.addVertex(fusedVertex);

        for (Edge edge : original.edges) {
            String newSource = edge.source.equals(v1) || edge.source.equals(v2) ? fusedVertex : edge.source;
            String newDest = edge.destination.equals(v1) || edge.destination.equals(v2) ? fusedVertex
                    : edge.destination;

            if (!newSource.equals(newDest)) {
                result.addEdge(newSource, newDest, edge.label);
            }
        }

        return result;
    }

    private Graph contractEdge(Graph original, String v1, String v2) {
        boolean edgeExists = original.hasEdge(v1, v2);
        if (!edgeExists) {
            throw new IllegalArgumentException("La arista entre " + v1 + " y " + v2 + " no existe");
        }

        Graph result = new Graph();
        result.isDirected = original.isDirected;
        result.isWeighted = original.isWeighted;
        String contractedVertex = v1 + v2;

        for (String vertex : original.vertices) {
            if (!vertex.equals(v1) && !vertex.equals(v2)) {
                result.addVertex(vertex);
            }
        }
        result.addVertex(contractedVertex);

        for (Edge edge : original.edges) {
            String currentSource = edge.source;
            String currentDest = edge.destination;

            String newSource = currentSource.equals(v1) || currentSource.equals(v2) ? contractedVertex : currentSource;
            String newDest = currentDest.equals(v1) || currentDest.equals(v2) ? contractedVertex : currentDest;

            boolean isContractedEdge = (currentSource.equals(v1) && currentDest.equals(v2)) ||
                    (currentSource.equals(v2) && currentDest.equals(v1));

            if (!isContractedEdge && !newSource.equals(newDest)) {
                result.addEdge(newSource, newDest, edge.label);
            }
        }

        return result;
    }

    private Graph union(Graph g1, Graph g2) {
        Graph union = new Graph();
        union.isDirected = g1.isDirected;
        union.isWeighted = g1.isWeighted;

        union.vertices.addAll(g1.vertices);
        union.vertices.addAll(g2.vertices);

        for (Edge edge : g1.edges) {
            try {
                union.addEdge(edge.source, edge.destination, edge.label, edge.isSumEdge);
            } catch (IllegalStateException e) {

            }
        }

        for (Edge edge : g2.edges) {
            try {
                union.addEdge(edge.source, edge.destination, edge.label, edge.isSumEdge);
            } catch (IllegalStateException e) {

            }
        }

        return union;
    }

    private Graph intersection(Graph g1, Graph g2) {
        Graph intersection = new Graph();
        intersection.isDirected = g1.isDirected;
        intersection.isWeighted = g1.isWeighted;

        for (String vertex : g1.vertices) {
            if (g2.vertices.contains(vertex)) {
                intersection.addVertex(vertex);
            }
        }

        for (Edge edge : g1.edges) {
            if (g2.hasEdgeWithLabel(edge.source, edge.destination, edge.label) &&
                    intersection.vertices.contains(edge.source) &&
                    intersection.vertices.contains(edge.destination)) {
                intersection.addEdge(edge.source, edge.destination, edge.label);
            }
        }

        return intersection;
    }

    private Graph ringSum(Graph g1, Graph g2) {
        Graph ringSum = new Graph();
        ringSum.isDirected = g1.isDirected;
        ringSum.isWeighted = g1.isWeighted;

        ringSum.vertices.addAll(g1.vertices);
        ringSum.vertices.addAll(g2.vertices);

        for (Edge edge : g1.edges) {
            if (!g2.hasEdgeWithLabel(edge.source, edge.destination, edge.label)) {
                ringSum.addEdge(edge.source, edge.destination, edge.label);
            }
        }
        for (Edge edge : g2.edges) {
            if (!g1.hasEdgeWithLabel(edge.source, edge.destination, edge.label)) {
                ringSum.addEdge(edge.source, edge.destination, edge.label);
            }
        }

        return ringSum;
    }

    private Graph sum(Graph g1, Graph g2) {
        Graph sumGraph = new Graph();
        sumGraph.isDirected = false;
        sumGraph.isWeighted = g1.isWeighted;

        for (String v : g1.vertices) {
            sumGraph.addVertex("G1_" + v);
        }
        for (String v : g2.vertices) {
            sumGraph.addVertex("G2_" + v);
        }

        for (Edge edge : g1.edges) {
            sumGraph.addEdge("G1_" + edge.source, "G1_" + edge.destination, edge.label);
        }
        for (Edge edge : g2.edges) {
            sumGraph.addEdge("G2_" + edge.source, "G2_" + edge.destination, edge.label);
        }

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                sumGraph.addEdge("G1_" + v1, "G2_" + v2, "", true);
            }
        }

        return sumGraph;
    }

    private Graph cartesianProduct(Graph g1, Graph g2) {
        Graph product = new Graph();
        product.isDirected = false;
        product.isWeighted = false;

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                product.addVertex("(" + v1 + "," + v2 + ")");
            }
        }

        Set<String> addedEdges = new HashSet<>();

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                for (String u1 : g1.vertices) {
                    for (String u2 : g2.vertices) {
                        if (v1.equals(u1) && g2.hasEdge(v2, u2)) {
                            String vertex1 = "(" + v1 + "," + v2 + ")";
                            String vertex2 = "(" + u1 + "," + u2 + ")";
                            String edgeKey = getUndirectedEdgeKey(vertex1, vertex2);

                            if (!addedEdges.contains(edgeKey)) {
                                product.addEdge(vertex1, vertex2, "");
                                addedEdges.add(edgeKey);
                            }
                        } else if (v2.equals(u2) && g1.hasEdge(v1, u1)) {
                            String vertex1 = "(" + v1 + "," + v2 + ")";
                            String vertex2 = "(" + u1 + "," + u2 + ")";
                            String edgeKey = getUndirectedEdgeKey(vertex1, vertex2);

                            if (!addedEdges.contains(edgeKey)) {
                                product.addEdge(vertex1, vertex2, "");
                                addedEdges.add(edgeKey);
                            }
                        }
                    }
                }
            }
        }

        return product;
    }

    private Graph tensorProduct(Graph g1, Graph g2) {
        Graph product = new Graph();
        product.isDirected = false;
        product.isWeighted = false;

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                product.addVertex("(" + v1 + "," + v2 + ")");
            }
        }

        Set<String> addedEdges = new HashSet<>();

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                for (String u1 : g1.vertices) {
                    for (String u2 : g2.vertices) {
                        if (g1.hasEdge(v1, u1) && g2.hasEdge(v2, u2)) {
                            String vertex1 = "(" + v1 + "," + v2 + ")";
                            String vertex2 = "(" + u1 + "," + u2 + ")";
                            String edgeKey = getUndirectedEdgeKey(vertex1, vertex2);

                            if (!addedEdges.contains(edgeKey)) {
                                product.addEdge(vertex1, vertex2, "");
                                addedEdges.add(edgeKey);
                            }
                        }
                    }
                }
            }
        }

        return product;
    }

    private Graph composition(Graph g1, Graph g2) {
        Graph comp = new Graph();
        comp.isDirected = false;
        comp.isWeighted = false;

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                comp.addVertex("(" + v1 + "," + v2 + ")");
            }
        }

        Set<String> addedEdges = new HashSet<>();

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                for (String u1 : g1.vertices) {
                    for (String u2 : g2.vertices) {
                        if (g1.hasEdge(v1, u1) || (v1.equals(u1) && g2.hasEdge(v2, u2))) {
                            String vertex1 = "(" + v1 + "," + v2 + ")";
                            String vertex2 = "(" + u1 + "," + u2 + ")";
                            String edgeKey = getUndirectedEdgeKey(vertex1, vertex2);

                            if (!addedEdges.contains(edgeKey)) {
                                comp.addEdge(vertex1, vertex2, "");
                                addedEdges.add(edgeKey);
                            }
                        }
                    }
                }
            }
        }

        return comp;
    }

    private String getUndirectedEdgeKey(String vertex1, String vertex2) {
        if (vertex1.compareTo(vertex2) < 0) {
            return vertex1 + "|" + vertex2;
        } else {
            return vertex2 + "|" + vertex1;
        }
    }

    private String getEdgeKey(String source, String destination, boolean isDirected) {
        if (isDirected) {
            return source + "->" + destination;
        } else {
            String sorted1 = source.compareTo(destination) < 0 ? source : destination;
            String sorted2 = source.compareTo(destination) < 0 ? destination : source;
            return sorted1 + "-" + sorted2;
        }
    }

    private void updateOperationState() {
        boolean graph1HasData = !graph1Data.isEmpty();
        boolean graph2HasData = !graph2Data.isEmpty();
        boolean anyGraphHasData = graph1HasData || graph2HasData;

        setOperationControlsEnabled(anyGraphHasData);

        if (!anyGraphHasData) {
            operationText.setText("Agregue vértices a los grafos para habilitar operaciones");
        } else {
            if (currentOperatingGraph != null && currentOperation != null) {
                if ("Entre los 2 grafos".equals(currentOperatingGraph)) {
                    if (!graph1HasData || !graph2HasData) {
                        operationText.setText("Error: Para operar entre los 2 grafos, ambos deben contener vértices");
                        operateButton.setDisable(true);
                    } else {
                        operateButton.setDisable(false);
                        operationText.setText("Listo para operar: " + currentOperation);
                    }
                } else {
                    Graph targetGraph = "Grafo 1".equals(currentOperatingGraph) ? graph1Data : graph2Data;
                    if (targetGraph.isEmpty()) {
                        operationText.setText("Error: El grafo operado está vacío");
                        operateButton.setDisable(true);
                    } else {
                        operateButton.setDisable(false);
                        operationText.setText("Listo para operar: " + currentOperation);
                    }
                }
            } else {
                operateButton.setDisable(true);
                if (currentOperatingGraph == null) {
                    operationText.setText("Seleccione un grafo para operar");
                } else if (currentOperation == null) {
                    operationText.setText("Seleccione una operación");
                }
            }
        }
    }

    @FXML
    private void undo() {
        Stack<GraphState> history = getCurrentHistory();
        Stack<GraphState> redoStack = getCurrentRedoStack();
        Graph currentGraph = getCurrentModifyingGraph();

        if (history.isEmpty()) {
            modificationText.setText("No hay acciones para deshacer");
            return;
        }

        redoStack.push(currentGraph.getState());
        GraphState previousState = history.pop();
        currentGraph.setState(previousState);

        updateGraphDisplay();
        modificationText.setText("Acción deshecha");
        updateOperationState();
    }

    @FXML
    private void redo() {
        Stack<GraphState> history = getCurrentHistory();
        Stack<GraphState> redoStack = getCurrentRedoStack();
        Graph currentGraph = getCurrentModifyingGraph();

        if (redoStack.isEmpty()) {
            modificationText.setText("No hay acciones para rehacer");
            return;
        }

        history.push(currentGraph.getState());
        GraphState nextState = redoStack.pop();
        currentGraph.setState(nextState);

        updateGraphDisplay();
        modificationText.setText("Acción rehecha");
        updateOperationState();
    }

    @FXML
    private void restart() {
        Graph currentGraph = getCurrentModifyingGraph();
        if (currentGraph != null) {
            saveGraphState();
            currentGraph.vertices.clear();
            currentGraph.edges.clear();
            currentGraph.hasEdges = false;
            currentGraph.isDirected = false;
            currentGraph.isWeighted = true;
            updateGraphDisplay();
            modificationText.setText("Grafo reiniciado: " + currentModifyingGraph);
            updateOperationState();
        } else {
            modificationText.setText("Error: Seleccione un grafo para reiniciar");
        }
    }

    @FXML
    private void restartOp() {
        graphResultData = new Graph();
        updateResultGraphDisplay();
        operationText.setText("Grafo resultado reiniciado");
    }

    @FXML
    private void save() {
        Graph currentGraph = getCurrentModifyingGraph();
        if (currentGraph == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Guardar grafo");
            alert.setHeaderText(null);
            alert.setContentText("Seleccione 'Grafo 1' o 'Grafo 2' en 'Modificar' para guardar.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar grafo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(currentGraph.getState());
            modificationText.setText("Grafo guardado: " + file.getName());
        } catch (Exception e) {
            modificationText.setText("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void load() {
        Graph currentGraph = getCurrentModifyingGraph();
        if (currentGraph == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cargar grafo");
            alert.setHeaderText(null);
            alert.setContentText("Seleccione 'Grafo 1' o 'Grafo 2' en 'Modificar' para cargar.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar grafo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));

        Stage stage = (Stage) loadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file == null) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<GraphState> states = new ArrayList<>();
            try {
                while (true) {
                    Object obj = ois.readObject();
                    if (obj instanceof GraphState) {
                        states.add((GraphState) obj);
                    }
                }
            } catch (EOFException eof) {

            }

            if (states.isEmpty()) {
                throw new IllegalArgumentException("Archivo no contiene grafos válidos");
            }

            if (states.size() >= 3) {

                graph1Data = new Graph();
                graph1Data.setState(states.get(0));
                graph2Data = new Graph();
                graph2Data.setState(states.get(1));
                graphResultData = new Graph();
                graphResultData.setState(states.get(2));

                graph1History.clear();
                graph2History.clear();
                graph1RedoStack.clear();
                graph2RedoStack.clear();

                updateAllGraphDisplays();
                modificationText.setText("Grafos cargados desde: " + file.getName());
                updateOperationState();
                return;
            }

            GraphState loadedState = states.get(0);

            if ("Grafo 1".equals(currentModifyingGraph)) {
                graph1Data = new Graph();
                graph1Data.setState(loadedState);
                graph1History.clear();
                graph1RedoStack.clear();
                currentScaleGraph1 = 1.0;
                drawGraph(graph1Data, graph1);
            } else if ("Grafo 2".equals(currentModifyingGraph)) {
                graph2Data = new Graph();
                graph2Data.setState(loadedState);
                graph2History.clear();
                graph2RedoStack.clear();
                currentScaleGraph2 = 1.0;
                drawGraph(graph2Data, graph2);
            }

            modificationText.setText("Grafo cargado: " + file.getName());
            updateOperationState();
        } catch (Exception e) {
            modificationText.setText("Error al cargar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void saveResult() {
        if (graphResultData == null || graphResultData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Guardar resultado");
            alert.setHeaderText(null);
            alert.setContentText("No hay grafo resultado para guardar.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar grafo resultado");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));

        Stage stage = (Stage) saveButtonResult.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(graphResultData.getState());
            modificationText.setText("Grafo resultado guardado: " + file.getName());
        } catch (Exception e) {
            modificationText.setText("Error al guardar resultado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Graph getCurrentModifyingGraph() {
        if ("Grafo 1".equals(currentModifyingGraph)) {
            return graph1Data;
        } else if ("Grafo 2".equals(currentModifyingGraph)) {
            return graph2Data;
        }
        return null;
    }

    private Stack<GraphState> getCurrentHistory() {
        return "Grafo 1".equals(currentModifyingGraph) ? graph1History : graph2History;
    }

    private Stack<GraphState> getCurrentRedoStack() {
        return "Grafo 1".equals(currentModifyingGraph) ? graph1RedoStack : graph2RedoStack;
    }

    private void saveGraphState() {
        Graph currentGraph = getCurrentModifyingGraph();
        if (currentGraph != null) {
            getCurrentHistory().push(currentGraph.getState());
            getCurrentRedoStack().clear();
        }
    }

    private String generateNextVertexName(Graph graph) {
        if (graph.vertices.isEmpty()) {
            return "A";
        }

        List<String> sortedVertices = new ArrayList<>(graph.vertices);
        sortedVertices.sort((a, b) -> {
            if (a.length() != b.length()) {
                return a.length() - b.length();
            }
            return a.compareTo(b);
        });

        String lastVertex = sortedVertices.get(sortedVertices.size() - 1);

        if (lastVertex.length() == 1) {
            char lastChar = lastVertex.charAt(0);
            if (lastChar < 'Z') {
                return String.valueOf((char) (lastChar + 1));
            } else {
                return "AA";
            }
        } else {
            char[] chars = lastVertex.toCharArray();
            for (int i = chars.length - 1; i >= 0; i--) {
                if (chars[i] < 'Z') {
                    chars[i]++;
                    return new String(chars);
                } else {
                    chars[i] = 'A';
                }
            }
            return lastVertex + "A";
        }
    }

    private void updateGraphDisplay() {
        if ("Grafo 1".equals(currentModifyingGraph)) {
            drawGraph(graph1Data, graph1);
        } else if ("Grafo 2".equals(currentModifyingGraph)) {
            drawGraph(graph2Data, graph2);
        }
    }

    private void updateResultGraphDisplay() {
        drawGraph(graphResultData, graphResult);

        boolean resultEmpty = (graphResultData == null || graphResultData.isEmpty());
        if (saveButtonResult != null) {
            saveButtonResult.setDisable(resultEmpty);
        }
        if (isomButtonResult != null) {
            isomButtonResult.setDisable(resultEmpty);
        }
    }

    private void updateAllGraphDisplays() {
        drawGraph(graph1Data, graph1);
        drawGraph(graph2Data, graph2);
        drawGraph(graphResultData, graphResult);
        boolean graph1Empty = (graph1Data == null || graph1Data.isEmpty());
        boolean graph2Empty = (graph2Data == null || graph2Data.isEmpty());
        boolean resultEmpty = (graphResultData == null || graphResultData.isEmpty());
        if (saveButtonResult != null) {
            saveButtonResult.setDisable(resultEmpty);
        }
        if (isomButton1 != null)
            isomButton1.setDisable(graph1Empty);
        if (isomButton2 != null)
            isomButton2.setDisable(graph2Empty);
        if (isomButtonResult != null)
            isomButtonResult.setDisable(resultEmpty);
    }

    @FXML
    private void onIsomorfismoGraph1() {
        if (graph1Data == null || graph1Data.isEmpty()) {
            showAlert("Isomorfismo", "Grafo 1 vacío");
            return;
        }

        shuffleLayout(graph1Data, "graph1");
        updateGraphDisplay();
        modificationText.setText("Grafo 1 reorganizado (posiciones aleatorias)");
        updateOperationState();
    }

    @FXML
    private void onIsomorfismoGraph2() {
        if (graph2Data == null || graph2Data.isEmpty()) {
            showAlert("Isomorfismo", "Grafo 2 vacío");
            return;
        }

        shuffleLayout(graph2Data, "graph2");
        updateGraphDisplay();
        modificationText.setText("Grafo 2 reorganizado (posiciones aleatorias)");
        updateOperationState();
    }

    @FXML
    private void onIsomorfismoGraphResult() {
        if (graphResultData == null || graphResultData.isEmpty()) {
            showAlert("Isomorfismo", "Grafo Resultante vacío");
            return;
        }

        shuffleLayout(graphResultData, "result");
        updateResultGraphDisplay();
        operationText.setText("Grafo resultante reorganizado (posiciones aleatorias)");
    }

    private boolean areIsomorphic(Graph a, Graph b) {
        if (a == null || b == null)
            return false;
        if (a.vertices.size() != b.vertices.size())
            return false;
        if (a.edges.size() != b.edges.size())
            return false;
        if (a.isDirected() != b.isDirected())
            return false;
        if (a.isWeighted() != b.isWeighted())
            return false;

        List<Integer> seqA = new ArrayList<>();
        List<Integer> seqB = new ArrayList<>();
        for (String v : a.vertices)
            seqA.add(a.getAdjacentVertices(v).size());
        for (String v : b.vertices)
            seqB.add(b.getAdjacentVertices(v).size());
        Collections.sort(seqA);
        Collections.sort(seqB);
        if (!seqA.equals(seqB))
            return false;

        List<String> vertsA = new ArrayList<>(a.vertices);
        List<String> vertsB = new ArrayList<>(b.vertices);

        Map<String, Map<String, List<Edge>>> adjB = buildAdjacencyMap(b);

        boolean[] used = new boolean[vertsB.size()];
        Map<String, String> mapping = new HashMap<>();

        return backtrackIsom(0, vertsA, vertsB, used, mapping, a, b, adjB);
    }

    private boolean backtrackIsom(int idx, List<String> vertsA, List<String> vertsB, boolean[] used,
            Map<String, String> mapping, Graph a, Graph b,
            Map<String, Map<String, List<Edge>>> adjB) {
        if (idx == vertsA.size())
            return true;
        String va = vertsA.get(idx);
        int degA = a.getAdjacentVertices(va).size();
        for (int j = 0; j < vertsB.size(); j++) {
            if (used[j])
                continue;
            String vb = vertsB.get(j);
            if (b.getAdjacentVertices(vb).size() != degA)
                continue;

            mapping.put(va, vb);
            used[j] = true;

            boolean ok = true;
            for (Map.Entry<String, String> e : mapping.entrySet()) {
                String sa = e.getKey();
                String sb = e.getValue();
                for (Edge edgeA : getEdgesForVertex(a, sa)) {
                    String otherA = edgeA.source.equals(sa) ? edgeA.destination : edgeA.source;
                    if (mapping.containsKey(otherA)) {
                        String otherB = mapping.get(otherA);
                        boolean exists = false;
                        Map<String, List<Edge>> mapFromSb = adjB.get(sb);
                        if (mapFromSb != null) {
                            List<Edge> candidates = mapFromSb.get(otherB);
                            if (candidates != null) {
                                for (Edge ce : candidates) {
                                    if (Objects.equals(ce.label, edgeA.label) && ce.isSumEdge == edgeA.isSumEdge) {
                                        exists = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!exists) {
                            ok = false;
                            break;
                        }
                    }
                }
                if (!ok)
                    break;
            }

            if (ok) {
                if (backtrackIsom(idx + 1, vertsA, vertsB, used, mapping, a, b, adjB))
                    return true;
            }

            used[j] = false;
            mapping.remove(va);
        }
        return false;
    }

    private List<Edge> getEdgesForVertex(Graph g, String v) {
        List<Edge> res = new ArrayList<>();
        for (Edge e : g.edges) {
            if (e.source.equals(v) || e.destination.equals(v))
                res.add(e);
        }
        return res;
    }

    private Map<String, Map<String, List<Edge>>> buildAdjacencyMap(Graph g) {
        Map<String, Map<String, List<Edge>>> map = new HashMap<>();
        for (Edge e : g.edges) {
            map.computeIfAbsent(e.source, k -> new HashMap<>()).computeIfAbsent(e.destination, k -> new ArrayList<>())
                    .add(e);
            if (!g.isDirected()) {
                map.computeIfAbsent(e.destination, k -> new HashMap<>())
                        .computeIfAbsent(e.source, k -> new ArrayList<>()).add(e);
            }
        }
        return map;
    }

    private void showAlert(String title, String msg) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void drawGraph(Graph graph, ScrollPane scrollPane) {
        Pane canvas = new Pane();
        canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        double centerX = CANVAS_WIDTH / 2;
        double centerY = CANVAS_HEIGHT / 2;

        Map<String, Point2D> custom = null;
        if (scrollPane == graph1)
            custom = layoutGraph1;
        else if (scrollPane == graph2)
            custom = layoutGraph2;
        else if (scrollPane == graphResult)
            custom = layoutGraphResult;

        Map<String, Point2D> vertexPositions;
        if (custom != null && !custom.isEmpty() && custom.keySet().containsAll(graph.vertices)) {
            vertexPositions = custom;
        } else {
            vertexPositions = calculateGraphLayout(graph, centerX, centerY, VERTEX_RADIUS);
        }

        Map<String, List<Edge>> edgesByPair = groupParallelEdges(graph);

        List<EdgeLabelConnection> edgeLabelConnections = new ArrayList<>();

        for (List<Edge> parallelEdges : edgesByPair.values()) {
            if (parallelEdges.isEmpty())
                continue;

            Edge firstEdge = parallelEdges.get(0);

            List<String> allLabels = new ArrayList<>();
            for (Edge edge : parallelEdges) {
                allLabels.add(edge.label != null ? edge.label : "");
            }

            List<javafx.scene.shape.Shape> allEdgesInGroup = new ArrayList<>();

            if (firstEdge.isLoop) {

                Point2D vertexPos = vertexPositions.get(firstEdge.source);
                if (vertexPos != null) {
                    for (int i = 0; i < parallelEdges.size(); i++) {
                        Edge edge = parallelEdges.get(i);
                        drawLoop(canvas, vertexPos, VERTEX_RADIUS, graph.isDirected, edge.isSumEdge,
                                edge.label, i, parallelEdges.size(), allLabels, edgeLabelConnections, allEdgesInGroup);
                    }
                }
            } else {
                Point2D sourcePos = vertexPositions.get(firstEdge.source);
                Point2D destPos = vertexPositions.get(firstEdge.destination);

                if (sourcePos != null && destPos != null) {

                    for (int i = 0; i < parallelEdges.size(); i++) {
                        Edge edge = parallelEdges.get(i);
                        drawEdge(canvas, sourcePos, destPos, edge.label,
                                graph.isDirected, VERTEX_RADIUS, edge.isSumEdge, i, parallelEdges.size(),
                                allLabels, edgeLabelConnections, allEdgesInGroup);
                    }
                }
            }
        }

        setupEdgeLabelConnections(edgeLabelConnections);

        for (Map.Entry<String, Point2D> entry : vertexPositions.entrySet()) {
            String vertex = entry.getKey();
            Point2D position = entry.getValue();
            drawVertex(canvas, position.getX(), position.getY(), VERTEX_RADIUS, vertex);
        }

        scrollPane.setContent(canvas);
        centerGraphView(scrollPane);
    }

        private void drawLoop(Pane canvas, Point2D vertex, double radius, boolean isDirected,
            boolean isSumEdge, String label, int loopIndex, int totalLoops, List<String> allLabels,
            List<EdgeLabelConnection> connections, List<javafx.scene.shape.Shape> allEdges) {
        Color[] loopColors = { Color.BLACK, Color.RED, Color.ORANGE, Color.GREEN };
        Color edgeColor = loopIndex < loopColors.length ? loopColors[loopIndex] : Color.BLACK;
        final Color hoverColor = getLighter(edgeColor);

        // Anchor point on the left-middle of the vertex
        double anchorX = vertex.getX() - radius;
        double anchorY = vertex.getY() + (loopIndex - (totalLoops - 1) / 2.0) * (radius / 3.0);

        // vertical offset for curve endpoints so they touch near the middle
        double deltaY = 8 + loopIndex * 4;
        double startX = anchorX;
        double startY = anchorY - deltaY;
        double endX = anchorX;
        double endY = anchorY + deltaY;

        double controlOffset = 40 + loopIndex * 15;
        double controlX = anchorX - controlOffset;
        double controlY1 = startY - controlOffset / 2.0;
        double controlY2 = endY + controlOffset / 2.0;

        CubicCurve curve = new CubicCurve(startX, startY, controlX, controlY1, controlX, controlY2, endX, endY);
        curve.setFill(Color.TRANSPARENT);
        if (isSumEdge) {
            curve.getStrokeDashArray().addAll(5d, 5d);
            curve.setStroke(Color.BLUE);
        } else {
            curve.setStroke(edgeColor);
        }
        curve.setStrokeWidth(2);

        final CubicCurve finalCurve = curve;
        curve.setOnMouseEntered(e -> {
            finalCurve.setStroke(edgeColor);
            finalCurve.setStrokeWidth(3);
        });
        curve.setOnMouseExited(e -> {
            finalCurve.setStroke(isSumEdge ? Color.BLUE : edgeColor);
            finalCurve.setStrokeWidth(2);
        });

        canvas.getChildren().add(curve);

        if (isDirected && !isSumEdge) {
            double arrowLength = 10;
            double angle = Math.atan2(endY - controlY2, endX - controlX);
            double arrowX = endX;
            double arrowY = endY;

            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(
                    arrowX, arrowY,
                    arrowX + arrowLength * Math.cos(angle - Math.PI / 6),
                    arrowY + arrowLength * Math.sin(angle - Math.PI / 6),
                    arrowX + arrowLength * Math.cos(angle + Math.PI / 6),
                    arrowY + arrowLength * Math.sin(angle + Math.PI / 6));
            arrowHead.setFill(edgeColor);

            arrowHead.setOnMouseEntered(e -> {
                arrowHead.setFill(getLighter(edgeColor));
                finalCurve.setStroke(getLighter(edgeColor));
                finalCurve.setStrokeWidth(3);
            });

            arrowHead.setOnMouseExited(e -> {
                arrowHead.setFill(edgeColor);
                finalCurve.setStroke(edgeColor);
                finalCurve.setStrokeWidth(2);
            });

            canvas.getChildren().add(arrowHead);
        }

        if (loopIndex == 0 && allLabels != null && !allLabels.isEmpty()) {
            List<String> nonEmptyLabels = new ArrayList<>();
            for (String lbl : allLabels) {
                if (lbl != null && !lbl.trim().isEmpty()) {
                    nonEmptyLabels.add(lbl);
                }
            }

            if (!nonEmptyLabels.isEmpty()) {
                double labelX = anchorX - controlOffset / 2 - 20;
                double labelY = anchorY - controlOffset / 2 - radius - 5;

                List<Color> textColors = Arrays.asList(loopColors);

                // Associate the actual curve with the labels so hovering either updates both
                allEdges.add(curve);

                drawMultiColorText(canvas, labelX, labelY, nonEmptyLabels, textColors, isSumEdge, connections,
                        allEdges);

                // Ensure hovering the curve updates all its associated label parts
                final CubicCurve fc = curve;
                fc.setOnMouseEntered(ev -> {
                    javafx.scene.paint.Color chosenHover = null;
                    for (EdgeLabelConnection c : connections) {
                        if (c.edge == fc) {
                            if (c.normalTextColor instanceof javafx.scene.paint.Color && chosenHover == null) {
                                chosenHover = getLighter((javafx.scene.paint.Color) c.normalTextColor);
                            }
                            if (c.labelPart != null) {
                                javafx.scene.paint.Color labelHover = c.hoverTextColor != null
                                        ? (javafx.scene.paint.Color) c.hoverTextColor
                                        : (c.normalTextColor != null
                                                ? getLighter((javafx.scene.paint.Color) c.normalTextColor)
                                                : Color.LIGHTGRAY);
                                c.labelPart.setFill(labelHover);
                            }
                        }
                    }
                    if (chosenHover == null && fc.getStroke() instanceof javafx.scene.paint.Color) {
                        chosenHover = getLighter((javafx.scene.paint.Color) fc.getStroke());
                    }
                    if (chosenHover != null) {
                        fc.setStroke(chosenHover);
                        fc.setStrokeWidth(3);
                    }
                });

                fc.setOnMouseExited(ev -> {
                    fc.setStroke(isSumEdge ? Color.BLUE : edgeColor);
                    fc.setStrokeWidth(2);
                    for (EdgeLabelConnection c : connections) {
                        if (c.edge == fc && c.labelPart != null && c.normalTextColor != null) {
                            c.labelPart.setFill((javafx.scene.paint.Color) c.normalTextColor);
                        }
                    }
                });
            }
        }
            // no placeholder connection needed: drawMultiColorText will create connections for the curve
    }

    private void centerGraphView(ScrollPane scrollPane) {
        if (scrollPane.getContent() != null) {
            javafx.application.Platform.runLater(() -> {
                scrollPane.setHvalue(0.5);
                scrollPane.setVvalue(0.3);
            });
        }
    }

    private void setupEdgeLabelConnections(List<EdgeLabelConnection> connections) {
        // Group connections by edge so we can set a single pair of handlers per Shape
        Map<javafx.scene.shape.Shape, List<EdgeLabelConnection>> map = new HashMap<>();
        for (EdgeLabelConnection connection : connections) {
            if (connection.edge != null) {
                map.computeIfAbsent(connection.edge, k -> new ArrayList<>()).add(connection);
            }
        }

        for (Map.Entry<javafx.scene.shape.Shape, List<EdgeLabelConnection>> entry : map.entrySet()) {
            javafx.scene.shape.Shape edge = entry.getKey();
            List<EdgeLabelConnection> conns = entry.getValue();

            // capture original stroke and width to restore later
            final javafx.scene.paint.Paint originalStroke = edge.getStroke();
            final double originalWidth = edge.getStrokeWidth();

            edge.setOnMouseEntered(e -> {
                // determine hover color per connection; pick first available normal color
                javafx.scene.paint.Color chosenHover = null;
                for (EdgeLabelConnection c : conns) {
                    if (c.normalTextColor instanceof javafx.scene.paint.Color) {
                        chosenHover = getLighter((javafx.scene.paint.Color) c.normalTextColor);
                        break;
                    }
                }
                if (chosenHover == null) {
                    if (originalStroke instanceof javafx.scene.paint.Color) {
                        chosenHover = getLighter((javafx.scene.paint.Color) originalStroke);
                    } else {
                        chosenHover = Color.LIGHTGRAY;
                    }
                }
                edge.setStroke(chosenHover);
                edge.setStrokeWidth(3);
                for (EdgeLabelConnection c : conns) {
                    if (c.labelPart != null) {
                        javafx.scene.paint.Color labelNormal = c.normalTextColor != null
                                ? (javafx.scene.paint.Color) c.normalTextColor
                                : null;
                        javafx.scene.paint.Color labelHover = c.hoverTextColor != null
                                ? (javafx.scene.paint.Color) c.hoverTextColor
                                : (labelNormal != null ? getLighter(labelNormal) : chosenHover);
                        c.labelPart.setFill(labelHover);
                    }
                }
            });

            edge.setOnMouseExited(e -> {
                edge.setStroke(originalStroke);
                edge.setStrokeWidth(originalWidth <= 0 ? 2 : originalWidth);
                for (EdgeLabelConnection c : conns) {
                    if (c.labelPart != null) {
                        if (c.normalTextColor != null) {
                            c.labelPart.setFill((javafx.scene.paint.Color) c.normalTextColor);
                        }
                    }
                }
            });
        }
    }

    private void drawMultiColorText(Pane canvas, double x, double y, List<String> labels,
            List<Color> colors, boolean isSumEdge, List<EdgeLabelConnection> connections,
            List<javafx.scene.shape.Shape> associatedEdges) {
        if (labels.isEmpty())
            return;

        double currentX = x;

        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            if (label == null || label.trim().isEmpty())
                continue;

            Color textColor = isSumEdge ? Color.BLUE : colors.get(i % colors.size());
            // Use a lighter variant of the label color for hover
            final Color hoverColor = getLighter(textColor);

            Text textPart = new Text(currentX, y, label);
            textPart.setFill(textColor);
            textPart.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");

            final int edgeIndex = i;
            textPart.setOnMouseEntered(e -> {
                textPart.setFill(hoverColor);

                if (associatedEdges != null && edgeIndex < associatedEdges.size()) {
                    javafx.scene.shape.Shape correspondingEdge = associatedEdges.get(edgeIndex);
                    if (correspondingEdge != null) {
                        correspondingEdge.setStroke(hoverColor);
                        correspondingEdge.setStrokeWidth(3);
                    }
                }
            });

            textPart.setOnMouseExited(e -> {
                textPart.setFill(textColor);

                if (associatedEdges != null && edgeIndex < associatedEdges.size()) {
                    javafx.scene.shape.Shape correspondingEdge = associatedEdges.get(edgeIndex);
                    if (correspondingEdge != null) {
                        correspondingEdge.setStroke(isSumEdge ? Color.BLUE : colors.get(edgeIndex % colors.size()));
                        correspondingEdge.setStrokeWidth(2);
                    }
                }
            });

            canvas.getChildren().add(textPart);

            if (associatedEdges != null && !associatedEdges.isEmpty()) {
                javafx.scene.shape.Shape edge = null;
                if (i < associatedEdges.size()) {
                    edge = associatedEdges.get(i);
                } else if (associatedEdges.size() == 1) {
                    // single associated edge (e.g., loop) -> associate same shape to all label parts
                    edge = associatedEdges.get(0);
                }
                if (edge != null) {
                    connections.add(new EdgeLabelConnection(edge, textPart, i, textColor, hoverColor));
                }
            }

            if (i < labels.size() - 1) {
                Text comma = new Text(currentX + textPart.getLayoutBounds().getWidth(), y, ", ");
                comma.setFill(Color.BLACK);
                comma.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
                canvas.getChildren().add(comma);
                currentX += textPart.getLayoutBounds().getWidth() + comma.getLayoutBounds().getWidth();
            } else {
                currentX += textPart.getLayoutBounds().getWidth();
            }
        }
    }

        private void drawEdge(Pane canvas, Point2D source, Point2D target, String label,
            boolean isDirected, double radius, boolean isSumEdge, int parallelIndex, int totalEdges,
            List<String> allLabels, List<EdgeLabelConnection> connections, List<javafx.scene.shape.Shape> allEdges) {

        double arrowLength = 15;
        double arrowWidth = 8;

        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double unitX = dx / distance;
        double unitY = dy / distance;

        double offset = 0;
        if (totalEdges > 1) {
            // Limit maxOffset so the perpendicular offset never exceeds the vertex radius
            double maxOffset = Math.min(12.0, Math.max(0, radius - 2));
            offset = (parallelIndex - (totalEdges - 1) / 2.0) * (maxOffset / Math.max(1, totalEdges - 1));
        }

        double offsetX = -unitY * offset;
        double offsetY = unitX * offset;

        // Center the edge between the two vertices instead of reaching to their extremes.
        double fullDist = distance;
        double innerGap = Math.min(fullDist * 0.25, radius); // leave 25% gap at each side, up to radius

        double adjustedSourceX = source.getX() + offsetX + unitX * innerGap;
        double adjustedSourceY = source.getY() + offsetY + unitY * innerGap;

        double adjustedTargetX = target.getX() + offsetX - unitX * innerGap;
        double adjustedTargetY = target.getY() + offsetY - unitY * innerGap;

        Color edgeColor = parallelIndex < EDGE_COLORS.length ? EDGE_COLORS[parallelIndex] : Color.BLACK;
        final Color hoverColor = getLighter(edgeColor);

        Line line = new Line(adjustedSourceX, adjustedSourceY,
            adjustedTargetX, adjustedTargetY);

        if (isSumEdge) {
            line.getStrokeDashArray().addAll(5d, 5d);
            line.setStroke(Color.BLUE);
        } else {
            line.setStroke(edgeColor);
        }

        line.setStrokeWidth(2);

        // Hover behavior for line is handled centrally in setupEdgeLabelConnections (grouped per Shape)

        canvas.getChildren().add(line);

        if (allEdges != null) {
            allEdges.add(line);
        }

        // Ensure hovering the line updates all its associated label parts
        line.setOnMouseEntered(e -> {
            javafx.scene.paint.Color chosenHover = null;
            for (EdgeLabelConnection c : connections) {
                if (c.edge == line) {
                    if (c.normalTextColor instanceof javafx.scene.paint.Color && chosenHover == null) {
                        chosenHover = getLighter((javafx.scene.paint.Color) c.normalTextColor);
                    }
                    if (c.labelPart != null) {
                        javafx.scene.paint.Color labelHover = c.hoverTextColor != null
                                ? (javafx.scene.paint.Color) c.hoverTextColor
                                : (c.normalTextColor != null ? getLighter((javafx.scene.paint.Color) c.normalTextColor)
                                        : Color.LIGHTGRAY);
                        c.labelPart.setFill(labelHover);
                    }
                }
            }
            if (chosenHover == null && line.getStroke() instanceof javafx.scene.paint.Color) {
                chosenHover = getLighter((javafx.scene.paint.Color) line.getStroke());
            }
            if (chosenHover != null) {
                line.setStroke(chosenHover);
                line.setStrokeWidth(3);
            }
        });

        line.setOnMouseExited(e -> {
            line.setStroke(isSumEdge ? Color.BLUE : edgeColor);
            line.setStrokeWidth(2);
            for (EdgeLabelConnection c : connections) {
                if (c.edge == line && c.labelPart != null && c.normalTextColor != null) {
                    c.labelPart.setFill((javafx.scene.paint.Color) c.normalTextColor);
                }
            }
        });

        if (isDirected && !isSumEdge) {
            double angle = Math.atan2(adjustedTargetY - adjustedSourceY, adjustedTargetX - adjustedSourceX);

            double arrowX = adjustedTargetX - arrowLength * Math.cos(angle);
            double arrowY = adjustedTargetY - arrowLength * Math.sin(angle);

            double x2 = arrowX + arrowWidth * Math.cos(angle + Math.PI / 2);
            double y2 = arrowY + arrowWidth * Math.sin(angle + Math.PI / 2);

            double x3 = arrowX + arrowWidth * Math.cos(angle - Math.PI / 2);
            double y3 = arrowY + arrowWidth * Math.sin(angle - Math.PI / 2);

            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(
                    adjustedTargetX + offsetX, adjustedTargetY + offsetY,
                    x2, y2,
                    x3, y3);
            arrowHead.setFill(edgeColor);

            arrowHead.setOnMouseEntered(e -> {
                arrowHead.setFill(hoverColor);
                line.setStroke(hoverColor);
                line.setStrokeWidth(3);
            });

            arrowHead.setOnMouseExited(e -> {
                arrowHead.setFill(edgeColor);
                line.setStroke(edgeColor);
                line.setStrokeWidth(2);
            });

            canvas.getChildren().add(arrowHead);
        }

        if (parallelIndex == 0 && allLabels != null && !allLabels.isEmpty()) {
            List<String> nonEmptyLabels = new ArrayList<>();
            for (String lbl : allLabels) {
                if (lbl != null && !lbl.trim().isEmpty()) {
                    nonEmptyLabels.add(lbl);
                }
            }

            if (!nonEmptyLabels.isEmpty()) {

                double midX = (adjustedSourceX + adjustedTargetX) / 2;
                double midY = (adjustedSourceY + adjustedTargetY) / 2;

                double textOffsetX = -unitY * (25 + Math.abs(offset));
                double textOffsetY = unitX * (25 + Math.abs(offset));

                List<Color> textColors = Arrays.asList(EDGE_COLORS);

                drawMultiColorText(canvas, midX + textOffsetX, midY + textOffsetY,
                        nonEmptyLabels, textColors, isSumEdge, connections, allEdges);
            }
        }

        connections.add(new EdgeLabelConnection(line, null, parallelIndex, null, null));
    }

    private void drawVertex(Pane canvas, double x, double y, double radius, String label) {

        Circle circle = new Circle(x, y, radius);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);

        circle.setViewOrder(1);

        Text text = new Text(label);
        text.setStyle("-fx-font-weight: bold;");

        double textWidth = text.getLayoutBounds().getWidth();
        double textHeight = text.getLayoutBounds().getHeight();

        text.setX(x - textWidth / 2);
        text.setY(y + textHeight / 4);

        text.setViewOrder(0);

        canvas.getChildren().addAll(circle, text);
    }

    private Map<String, List<Edge>> groupParallelEdges(Graph graph) {
        Map<String, List<Edge>> edgesByPair = new HashMap<>();

        for (Edge edge : graph.edges) {
            String key;
            if (edge.isLoop) {
                key = edge.source + "::LOOP";
            } else if (graph.isDirected) {
                key = edge.source + "->" + edge.destination;
            } else {

                String sorted1 = edge.source.compareTo(edge.destination) < 0 ? edge.source : edge.destination;
                String sorted2 = edge.source.compareTo(edge.destination) < 0 ? edge.destination : edge.source;
                key = sorted1 + "-" + sorted2;
            }
            edgesByPair.computeIfAbsent(key, k -> new ArrayList<>()).add(edge);
        }

        return edgesByPair;
    }

    private Map<String, Point2D> calculateGraphLayout(Graph graph, double centerX, double centerY, double radius) {
        Map<String, Point2D> positions = new HashMap<>();
        List<String> vertices = new ArrayList<>(graph.vertices);

        if (vertices.isEmpty()) {
            return positions;
        }

        if (vertices.size() > 10 || vertices.stream().anyMatch(v -> v.contains(","))) {

            int gridSize = (int) Math.ceil(Math.sqrt(vertices.size()));
            double spacing = Math.min(100, 500.0 / gridSize);

            for (int i = 0; i < vertices.size(); i++) {
                int row = i / gridSize;
                int col = i % gridSize;
                double x = centerX - (gridSize * spacing / 2) + col * spacing;
                double y = centerY - (gridSize * spacing / 2) + row * spacing;
                positions.put(vertices.get(i), new Point2D(x, y));
            }
        } else if (vertices.size() <= 10) {

            double layoutRadius = Math.min(200, Math.max(100, vertices.size() * 25));
            for (int i = 0; i < vertices.size(); i++) {
                double angle = 2 * Math.PI * i / vertices.size();
                double x = centerX + layoutRadius * Math.cos(angle);
                double y = centerY + layoutRadius * Math.sin(angle);
                positions.put(vertices.get(i), new Point2D(x, y));
            }
        } else {

            double area = 1500 * vertices.size();
            double k = Math.sqrt(area / vertices.size());

            Random rand = new Random();
            for (String vertex : vertices) {
                double x = centerX + (rand.nextDouble() - 0.5) * 400;
                double y = centerY + (rand.nextDouble() - 0.5) * 400;
                positions.put(vertex, new Point2D(x, y));
            }

            for (int iter = 0; iter < 150; iter++) {
                Map<String, Point2D> displacements = new HashMap<>();

                for (String v1 : vertices) {
                    Point2D pos1 = positions.get(v1);
                    Point2D disp = new Point2D(0, 0);

                    for (String v2 : vertices) {
                        if (!v1.equals(v2)) {
                            Point2D pos2 = positions.get(v2);
                            Point2D diff = pos1.subtract(pos2);
                            double distance = diff.magnitude();
                            if (distance > 0) {
                                double force = (k * k) / distance;
                                disp = disp.add(diff.normalize().multiply(force));
                            }
                        }
                    }
                    displacements.put(v1, disp);
                }

                for (Edge edge : graph.edges) {
                    Point2D pos1 = positions.get(edge.source);
                    Point2D pos2 = positions.get(edge.destination);
                    if (pos1 != null && pos2 != null && !edge.isLoop) {
                        Point2D diff = pos2.subtract(pos1);
                        double distance = diff.magnitude();
                        if (distance > 0) {
                            double force = (distance * distance) / k;
                            Point2D disp1 = displacements.getOrDefault(edge.source, new Point2D(0, 0))
                                    .add(diff.normalize().multiply(force));
                            Point2D disp2 = displacements.getOrDefault(edge.destination, new Point2D(0, 0))
                                    .subtract(diff.normalize().multiply(force));
                            displacements.put(edge.source, disp1);
                            displacements.put(edge.destination, disp2);
                        }
                    }
                }

                for (String vertex : vertices) {
                    Point2D disp = displacements.getOrDefault(vertex, new Point2D(0, 0));
                    double length = disp.magnitude();
                    if (length > 0) {
                        disp = disp.normalize().multiply(Math.min(length, 15));
                    }
                    Point2D newPos = positions.get(vertex).add(disp);

                    newPos = new Point2D(
                            Math.max(radius + 20, Math.min(CANVAS_WIDTH - radius - 20, newPos.getX())),
                            Math.max(radius + 20, Math.min(CANVAS_HEIGHT - radius - 20, newPos.getY())));
                    positions.put(vertex, newPos);
                }
            }
        }

        return positions;
    }

    private void setupListeners() {

    }
}