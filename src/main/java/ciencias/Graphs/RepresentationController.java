package ciencias.Graphs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.*;
import java.io.*;

public class RepresentationController {

    @FXML
    private ScrollPane graph;
    @FXML
    private ScrollPane medianaGraph;
    @FXML
    private ScrollPane centerGraph;
    @FXML
    private MenuButton matrixOptions;
    @FXML
    private TabPane matrixTabPane;
    @FXML
    private Button operateButton;
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
    private TextField edgeWeightNotation;
    @FXML
    private Text operationText;
    @FXML
    private Text modificationText;
    @FXML
    private TextField excText;
    @FXML
    private TextField radiusText;
    @FXML
    private TextField longText;
    @FXML
    private TextField distanceItem1;
    @FXML
    private TextField distanceItem2;
    @FXML
    private Button calculateButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button restartButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;

    private Graph graphData;
    private Stack<GraphState> history;
    private Stack<GraphState> redoStack;
    private Map<String, Point2D> layoutGraph;
    private Map<String, Point2D> layoutMediana;
    private Map<String, Point2D> layoutCenter;

    private static final double VERTEX_RADIUS = 20;
    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 600;
    private double currentScale = 1.0;
    private double currentScaleMediana = 1.0;
    private double currentScaleCenter = 1.0;
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

        if ("graph".equals(graphId)) {
            layoutGraph = map;
        } else if ("mediana".equals(graphId)) {
            layoutMediana = map;
        } else if ("center".equals(graphId)) {
            layoutCenter = map;
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
        boolean isLoop;

        public Edge(String source, String destination, String label) {
            this.source = source;
            this.destination = destination;
            this.label = label;
            this.isLoop = source.equals(destination);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Edge edge = (Edge) obj;
            return source.equals(edge.source) && destination.equals(edge.destination) && label.equals(edge.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, destination, label);
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

            List<Edge> edgesBetween = getEdgesBetween(source, destination);
            int maxEdges = 3;
            int maxLoops = 2;

            if (source.equals(destination)) {
                if (edgesBetween.size() >= maxLoops) {
                    throw new IllegalStateException(
                            "No se pueden tener más de " + maxLoops + " bucles en el vértice " + source);
                }
            } else {
                if (edgesBetween.size() >= maxEdges) {
                    throw new IllegalStateException(
                            "No se pueden tener más de " + maxEdges + " aristas entre " + source + " y " + destination);
                }
            }

            if (hasEdges) {
                if (isDirected != edgeDirection.isSelected()) {
                    throw new IllegalStateException(
                            "No se pueden mezclar aristas dirigidas y no dirigidas en el mismo grafo");
                }

            } else {
                isDirected = edgeDirection.isSelected();

                hasEdges = true;
            }

            edges.add(new Edge(source, destination, label));
        }

        public void removeEdge(String source, String destination) {
            removeEdge(source, destination, "1");
        }

        public void removeEdge(String source, String destination, String label) {
            if (!isDirected) {
                edges.removeIf(edge -> ((edge.source.equals(source) && edge.destination.equals(destination)) ||
                        (edge.source.equals(destination) && edge.destination.equals(source))) &&
                        edge.label.equals(label));
            } else {
                edges.removeIf(edge -> edge.source.equals(source) && edge.destination.equals(destination) &&
                        edge.label.equals(label));
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

        graphData = new Graph();
        history = new Stack<>();
        redoStack = new Stack<>();
        layoutGraph = new HashMap<>();
        layoutMediana = new HashMap<>();
        layoutCenter = new HashMap<>();

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                Integer.MAX_VALUE, 1);
        vertNum.setValueFactory(valueFactory);
        vertNum.setEditable(true);

        for (MenuItem item : matrixOptions.getItems()) {
            item.setOnAction(e -> {
                matrixOptions.setText(item.getText());
            });
        }

        setupZoomAndScroll(graph, "main");
        setupZoomAndScroll(medianaGraph, "mediana");
        setupZoomAndScroll(centerGraph, "center");

        updateMetrics();
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

        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
    }

    private double getCurrentScale(String graphType) {
        switch (graphType) {
            case "main":
                return currentScale;
            case "mediana":
                return currentScaleMediana;
            case "center":
                return currentScaleCenter;
            default:
                return 1.0;
        }
    }

    private void setCurrentScale(String graphType, double scale) {
        switch (graphType) {
            case "main":
                currentScale = scale;
                break;
            case "mediana":
                currentScaleMediana = scale;
                break;
            case "center":
                currentScaleCenter = scale;
                break;
        }
    }

    @FXML
    private void vertAdd() {
        saveState();
        int numVertices = vertNum.getValue();
        List<String> addedVertices = new ArrayList<>();

        for (int i = 0; i < numVertices; i++) {
            String vertex = generateNextVertexName();
            graphData.addVertex(vertex);
            addedVertices.add(vertex);
        }

        updateGraphDisplay();
        updateMetrics();
        modificationText.setText("Añadidos vértices: " + String.join(", ", addedVertices));
    }

    @FXML
    private void vertDelete() {
        String vertex = vertDelete.getText().trim().toUpperCase();
        if (vertex.isEmpty()) {
            modificationText.setText("Error: Ingrese un vértice a eliminar");
            return;
        }

        if (!graphData.hasVertex(vertex)) {
            modificationText.setText("Error: El vértice " + vertex + " no existe");
            return;
        }

        saveState();
        graphData.removeVertex(vertex);
        vertDelete.clear();
        updateGraphDisplay();
        updateMetrics();
        modificationText.setText("Vértice " + vertex + " eliminado");
    }

    @FXML
    private void edgeAdd() {
        String source = edgeOrg.getText().trim().toUpperCase();
        String destination = edgeDest.getText().trim().toUpperCase();
        String weightText = edgeWeightNotation.getText().trim();

        if (source.isEmpty()) {
            modificationText.setText("Error: Ingrese al menos el vértice origen");
            return;
        }

        if (destination.isEmpty()) {
            destination = source;
        }

        if (!graphData.hasVertex(source) || !graphData.hasVertex(destination)) {
            modificationText.setText("Error: Los vértices deben existir");
            return;
        }

        String label;
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

        try {
            saveState();
            graphData.addEdge(source, destination, label);
            edgeOrg.clear();
            edgeDest.clear();
            edgeWeightNotation.clear();
            updateGraphDisplay();
            updateMetrics();

            String edgeType = source.equals(destination) ? "Bucle en " + source : source + " → " + destination;
            modificationText.setText("Arista añadida: " + edgeType + " (peso: " + label + ")");
        } catch (IllegalStateException e) {
            modificationText.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void edgeDelete() {
        String source = edgeOrg.getText().trim().toUpperCase();
        String destination = edgeDest.getText().trim().toUpperCase();
        String weightText = edgeWeightNotation.getText().trim();

        if (source.isEmpty()) {
            modificationText.setText("Error: Ingrese al menos el vértice origen");
            return;
        }

        if (destination.isEmpty()) {
            destination = source;
        }

        String label;
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

        List<Edge> edgesBetween = graphData.getEdgesBetween(source, destination);
        boolean found = false;
        for (Edge edge : edgesBetween) {
            if (edge.label.equals(label)) {
                found = true;
                break;
            }
        }

        if (!found) {
            modificationText.setText("Error: No existe arista con el peso especificado");
            return;
        }

        saveState();
        graphData.removeEdge(source, destination, label);
        edgeOrg.clear();
        edgeDest.clear();
        edgeWeightNotation.clear();
        updateGraphDisplay();
        updateMetrics();
        modificationText.setText("Arista eliminada: " + source + " → " + destination);
    }

    @FXML
    private void undo() {
        if (history.isEmpty()) {
            modificationText.setText("No hay acciones para deshacer");
            return;
        }
        redoStack.push(graphData.getState());
        GraphState previousState = history.pop();
        graphData.setState(previousState);
        updateGraphDisplay();
        updateMetrics();
        modificationText.setText("Acción deshecha");
    }

    @FXML
    private void redo() {
        if (redoStack.isEmpty()) {
            modificationText.setText("No hay acciones para rehacer");
            return;
        }
        history.push(graphData.getState());
        GraphState nextState = redoStack.pop();
        graphData.setState(nextState);
        updateGraphDisplay();
        updateMetrics();
        modificationText.setText("Acción rehecha");
    }

    @FXML
    private void restart() {
        saveState();
        graphData = new Graph();
        layoutGraph.clear();
        layoutMediana.clear();
        layoutCenter.clear();
        updateGraphDisplay();
        updateMetrics();
        modificationText.setText("Grafo reiniciado");
    }

    private void saveState() {
        history.push(graphData.getState());
        redoStack.clear();
    }

    @FXML
    private void calculateDistance() {
        String item1 = distanceItem1.getText().trim().toUpperCase();
        String item2 = distanceItem2.getText().trim().toUpperCase();

        if (item1.isEmpty() || item2.isEmpty()) {
            operationText.setText("Error: Ingrese ambos vértices");
            return;
        }

        if (!graphData.hasVertex(item1) || !graphData.hasVertex(item2)) {
            operationText.setText("Error: Los vértices deben existir");
            return;
        }

        double distance = calculateBellmanFordDistance(item1, item2);
        if (Double.isInfinite(distance)) {
            operationText.setText("No existe camino entre " + item1 + " y " + item2);
        } else {
            operationText.setText("Distancia entre " + item1 + " y " + item2 + ": " + (int) distance);
        }

        matrixTabPane.getTabs().clear();
        double[][] floydMatrix = computeFloydWarshall();
        Tab floydTab = new Tab("Matriz de Distancias");
        GridPane matrixGrid = createMatrixGrid(floydMatrix, "Matriz de Distancias");
        floydTab.setContent(new ScrollPane(matrixGrid));
        matrixTabPane.getTabs().add(floydTab);
    }

    private double calculateBellmanFordDistance(String source, String destination) {
        Map<String, Double> distances = new HashMap<>();
        for (String vertex : graphData.vertices) {
            distances.put(vertex, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);

        for (int i = 0; i < graphData.vertices.size() - 1; i++) {
            for (Edge edge : graphData.edges) {
                double edgeWeight = getWeight(edge);
                if (distances.get(edge.source) + edgeWeight < distances.get(edge.destination)) {
                    distances.put(edge.destination, distances.get(edge.source) + edgeWeight);
                }
                if (!graphData.isDirected) {
                    if (distances.get(edge.destination) + edgeWeight < distances.get(edge.source)) {
                        distances.put(edge.source, distances.get(edge.destination) + edgeWeight);
                    }
                }
            }
        }

        return distances.get(destination);
    }

    private double getWeight(Edge edge) {
        try {
            return Double.parseDouble(edge.label);
        } catch (NumberFormatException e) {
            return 1.0;

        }
    }

    private void updateMetrics() {
        if (graphData.isEmpty()) {
            excText.setText("");
            radiusText.setText("");
            longText.setText("");
            medianaGraph.setContent(new Pane());
            centerGraph.setContent(new Pane());
            return;
        }

        double[][] distances = computeFloydWarshall();

        Map<String, Double> eccentricities = new HashMap<>();
        List<String> vertices = new ArrayList<>(graphData.vertices);

        for (int i = 0; i < vertices.size(); i++) {
            double maxDistance = 0;
            for (int j = 0; j < vertices.size(); j++) {
                if (i != j && distances[i][j] != Double.POSITIVE_INFINITY) {
                    maxDistance = Math.max(maxDistance, distances[i][j]);
                }
            }
            eccentricities.put(vertices.get(i), maxDistance);
        }

        double radius = Double.POSITIVE_INFINITY;
        double diameter = 0;
        for (double ecc : eccentricities.values()) {
            if (ecc < radius)
                radius = ecc;
            if (ecc > diameter)
                diameter = ecc;
        }

        excText.setText(eccentricities.toString());
        radiusText.setText((radius == Double.POSITIVE_INFINITY ? "∞" : String.valueOf((int) radius)));
        longText.setText((diameter == Double.POSITIVE_INFINITY ? "∞" : String.valueOf((int) diameter)));

        updateMedianaAndCenter(eccentricities, radius, distances);
    }

    private double[][] computeFloydWarshall() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        int n = vertices.size();
        double[][] dist = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                } else {
                    dist[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }

        Map<String, Integer> vertexIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            vertexIndex.put(vertices.get(i), i);
        }

        for (Edge edge : graphData.edges) {
            int u = vertexIndex.get(edge.source);
            int v = vertexIndex.get(edge.destination);
            double weight = getWeight(edge);

            dist[u][v] = Math.min(dist[u][v], weight);
            if (!graphData.isDirected) {
                dist[v][u] = Math.min(dist[v][u], weight);
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }
        }

        return dist;
    }

    private void updateMedianaAndCenter(Map<String, Double> eccentricities, double radius, double[][] distances) {
        List<String> vertices = new ArrayList<>(graphData.vertices);

        Set<String> centerVertices = new HashSet<>();
        for (Map.Entry<String, Double> entry : eccentricities.entrySet()) {
            if (Math.abs(entry.getValue() - radius) < 1e-9) {
                centerVertices.add(entry.getKey());
            }
        }

        Map<String, Double> sumDistances = new HashMap<>();
        Map<String, Integer> vertexIndex = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++) {
            vertexIndex.put(vertices.get(i), i);
        }

        for (String vertex : vertices) {
            double sum = 0;
            int idx = vertexIndex.get(vertex);
            for (int j = 0; j < vertices.size(); j++) {
                if (distances[idx][j] != Double.POSITIVE_INFINITY) {
                    sum += distances[idx][j];
                }
            }
            sumDistances.put(vertex, sum);
        }

        double minSum = Collections.min(sumDistances.values());
        Set<String> medianaVertices = new HashSet<>();
        for (Map.Entry<String, Double> entry : sumDistances.entrySet()) {
            if (Math.abs(entry.getValue() - minSum) < 1e-9) {
                medianaVertices.add(entry.getKey());
            }
        }

        Graph centerSubgraph = createInducedSubgraph(centerVertices);
        Graph medianaSubgraph = createInducedSubgraph(medianaVertices);

        drawGraph(centerSubgraph, centerGraph, "center");
        drawGraph(medianaSubgraph, medianaGraph, "mediana");
    }

    private Graph createInducedSubgraph(Set<String> vertices) {
        Graph subgraph = new Graph();
        subgraph.vertices.addAll(vertices);
        subgraph.isDirected = graphData.isDirected;
        subgraph.isWeighted = graphData.isWeighted;

        for (Edge edge : graphData.edges) {
            if (vertices.contains(edge.source) && vertices.contains(edge.destination)) {
                subgraph.edges.add(edge);
            }
        }

        return subgraph;
    }

    @FXML
    private void operate() {
        matrixTabPane.getTabs().clear();

        String selectedMatrix = matrixOptions.getText();
        if (selectedMatrix.equals("Elegir matriz")) {
            operationText.setText("Error: Seleccione una matriz");
            return;
        }

        GridPane matrixGrid;
        switch (selectedMatrix) {
            case "Matriz de adyacencia vertices":
                matrixGrid = createAdjacencyMatrix();
                break;
            case "Matriz de adyacencia aristas":
                matrixGrid = createEdgeAdjacencyMatrix();
                break;
            case "Matriz de incidencia":
                matrixGrid = createIncidenceMatrix();
                break;
            case "Matriz de circuitos":
                matrixGrid = createCircuitMatrix();
                break;
            case "Matriz de circuitos fundamentales":
                matrixGrid = createFundamentalCircuitMatrix();
                break;
            case "Conjuntos de corte":
                matrixGrid = createCutSetMatrix();
                break;
            case "Conjuntos de corte fundamentales":
                matrixGrid = createFundamentalCutSetMatrix();
                break;
            default:
                matrixGrid = createPlaceholderMatrix(selectedMatrix);
                break;
        }

        Tab matrixTab = new Tab(selectedMatrix);
        matrixTab.setContent(new ScrollPane(matrixGrid));
        matrixTabPane.getTabs().add(matrixTab);

        operationText.setText("Matriz mostrada: " + selectedMatrix);
    }

    private GridPane createAdjacencyMatrix() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        int n = vertices.size();
        int[][] matrix = new int[n][n];

        for (Edge edge : graphData.edges) {
            int i = vertices.indexOf(edge.source);
            int j = vertices.indexOf(edge.destination);
            if (i != -1 && j != -1) {
                matrix[i][j]++;
                if (!graphData.isDirected && i != j) {
                    matrix[j][i]++;
                }
            }
        }

        return createIntMatrixGrid(matrix, vertices, vertices, "Matriz de Adyacencia de Vértices");
    }

    private GridPane createEdgeAdjacencyMatrix() {
        List<Edge> edges = graphData.edges;
        int n = edges.size();
        int[][] matrix = new int[n][n];

        for (int i = 0; i < n; i++) {
            Edge e1 = edges.get(i);
            for (int j = 0; j < n; j++) {
                Edge e2 = edges.get(j);
                if (i != j && (e1.source.equals(e2.source) || e1.source.equals(e2.destination) ||
                        e1.destination.equals(e2.source) || e1.destination.equals(e2.destination))) {
                    matrix[i][j] = 1;
                }
            }
        }

        List<String> edgeLabels = new ArrayList<>();
        for (Edge edge : edges) {
            edgeLabels.add(edge.source + "-" + edge.destination + "(" + edge.label + ")");
        }

        return createIntMatrixGrid(matrix, edgeLabels, edgeLabels, "Matriz de Adyacencia de Aristas");
    }

    private GridPane createIncidenceMatrix() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        List<Edge> edges = graphData.edges;
        int[][] matrix = new int[vertices.size()][edges.size()];

        for (int j = 0; j < edges.size(); j++) {
            Edge edge = edges.get(j);
            int sourceIdx = vertices.indexOf(edge.source);
            int destIdx = vertices.indexOf(edge.destination);

            if (sourceIdx != -1) {
                matrix[sourceIdx][j] = graphData.isDirected ? -1 : 1;
            }
            if (destIdx != -1) {
                matrix[destIdx][j] = 1;
            }
        }

        List<String> edgeLabels = new ArrayList<>();
        for (Edge edge : edges) {
            edgeLabels.add(edge.source + "-" + edge.destination + "(" + edge.label + ")");
        }

        return createIntMatrixGrid(matrix, vertices, edgeLabels, "Matriz de Incidencia");
    }

    private GridPane createCircuitMatrix() {

        List<String> vertices = new ArrayList<>(graphData.vertices);
        int n = vertices.size();
        int[][] matrix = new int[1][n];

        return createIntMatrixGrid(matrix, Arrays.asList("Circuitos"), vertices, "Matriz de Circuitos");
    }

    private GridPane createFundamentalCircuitMatrix() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        int n = vertices.size();
        int[][] matrix = new int[1][n];

        return createIntMatrixGrid(matrix, Arrays.asList("Circuitos Fundamentales"), vertices,
                "Matriz de Circuitos Fundamentales");
    }

    private GridPane createCutSetMatrix() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        int n = vertices.size();
        int[][] matrix = new int[1][n];

        return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte"), vertices, "Conjuntos de Corte");
    }

    private GridPane createFundamentalCutSetMatrix() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        int n = vertices.size();
        int[][] matrix = new int[1][n];

        return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte Fundamentales"), vertices,
                "Conjuntos de Corte Fundamentales");
    }

    private GridPane createPlaceholderMatrix(String matrixType) {
        GridPane grid = new GridPane();
        grid.add(new Text("Matriz " + matrixType + " no implementada"), 0, 0);
        return grid;
    }

    private GridPane createMatrixGrid(double[][] matrix, String title) {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        List<String> rowLabels = new ArrayList<>(vertices);
        List<String> colLabels = new ArrayList<>(vertices);

        return createDoubleMatrixGrid(matrix, rowLabels, colLabels, title);
    }

    private GridPane createDoubleMatrixGrid(double[][] matrix, List<String> rowLabels, List<String> colLabels,
            String title) {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i <= cols; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(60));
        }
        for (int i = 0; i <= rows; i++) {
            grid.getRowConstraints().add(new RowConstraints(30));
        }

        for (int j = 0; j < cols; j++) {
            Text header = new Text(colLabels.get(j));
            header.setStyle("-fx-font-weight: bold;");
            grid.add(header, j + 1, 0);
        }

        for (int i = 0; i < rows; i++) {
            Text rowHeader = new Text(rowLabels.get(i));
            rowHeader.setStyle("-fx-font-weight: bold;");
            grid.add(rowHeader, 0, i + 1);

            for (int j = 0; j < cols; j++) {
                String value;
                if (Double.isInfinite(matrix[i][j])) {
                    value = "∞";
                } else {
                    value = String.valueOf((int) matrix[i][j]);
                }
                Text cell = new Text(value);
                StackPane cellPane = new StackPane(cell);
                cellPane.setStyle("-fx-border-color: lightgray;");
                grid.add(cellPane, j + 1, i + 1);
            }
        }

        return grid;
    }

    private GridPane createIntMatrixGrid(int[][] matrix, List<String> rowLabels, List<String> colLabels, String title) {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        int rows = matrix.length;
        int cols = matrix[0].length;

        for (int i = 0; i <= cols; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(60));
        }
        for (int i = 0; i <= rows; i++) {
            grid.getRowConstraints().add(new RowConstraints(30));
        }

        for (int j = 0; j < cols; j++) {
            Text header = new Text(colLabels.get(j));
            header.setStyle("-fx-font-weight: bold;");
            grid.add(header, j + 1, 0);
        }

        for (int i = 0; i < rows; i++) {
            Text rowHeader = new Text(rowLabels.get(i));
            rowHeader.setStyle("-fx-font-weight: bold;");
            grid.add(rowHeader, 0, i + 1);

            for (int j = 0; j < cols; j++) {
                String value = String.valueOf(matrix[i][j]);
                Text cell = new Text(value);
                StackPane cellPane = new StackPane(cell);
                cellPane.setStyle("-fx-border-color: lightgray;");
                grid.add(cellPane, j + 1, i + 1);
            }
        }

        return grid;
    }

    private void drawGraph(Graph graph, ScrollPane scrollPane, String graphType) {
        Pane canvas = new Pane();

        if (graphType.equals("main")) {
            canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        } else if (graphType.equals("mediana")) {
            canvas.setPrefSize(224, 121);
        } else if (graphType.equals("center")) {
            canvas.setPrefSize(224, 121);
        }

        if (graph.isEmpty()) {
            scrollPane.setContent(canvas);
            return;
        }

        Map<String, Point2D> customLayout = getLayoutForGraphType(graphType);
        double centerX = canvas.getPrefWidth() / 2;
        double centerY = canvas.getPrefHeight() / 2;

        if (customLayout == null || customLayout.isEmpty() || !customLayout.keySet().containsAll(graph.vertices)) {
            double radius = graphType.equals("main") ? VERTEX_RADIUS : 8;
            customLayout = calculateGraphLayout(graph, centerX, centerY, radius);
            setLayoutForGraphType(graphType, customLayout);
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
                Point2D vertexPos = customLayout.get(firstEdge.source);
                if (vertexPos != null) {
                    for (int i = 0; i < parallelEdges.size(); i++) {
                        Edge edge = parallelEdges.get(i);
                        double radius = graphType.equals("main") ? VERTEX_RADIUS : 8;
                        drawLoop(canvas, vertexPos, radius, graph.isDirected,
                                edge.label, i, parallelEdges.size(), allLabels,
                                edgeLabelConnections, allEdgesInGroup);
                    }
                }
            } else {
                Point2D sourcePos = customLayout.get(firstEdge.source);
                Point2D destPos = customLayout.get(firstEdge.destination);

                if (sourcePos != null && destPos != null) {
                    for (int i = 0; i < parallelEdges.size(); i++) {
                        Edge edge = parallelEdges.get(i);
                        double radius = graphType.equals("main") ? VERTEX_RADIUS : 8;
                        drawEdge(canvas, sourcePos, destPos, edge.label,
                                graph.isDirected, radius, i, parallelEdges.size(),
                                allLabels, edgeLabelConnections, allEdgesInGroup);
                    }
                }
            }
        }

        setupEdgeLabelConnections(edgeLabelConnections);

        for (Map.Entry<String, Point2D> entry : customLayout.entrySet()) {
            String vertex = entry.getKey();
            Point2D position = entry.getValue();
            double radius = graphType.equals("main") ? VERTEX_RADIUS : 8;
            drawVertex(canvas, position.getX(), position.getY(), radius, vertex);
        }

        scrollPane.setContent(canvas);
        centerGraphView(scrollPane);
    }

    private void updateGraphDisplay() {
        drawGraph(graphData, graph, "main");
    }

    private void drawSubgraph(Graph subgraph, ScrollPane scrollPane, String graphType) {
        Pane canvas = new Pane();

        if (graphType.equals("mediana")) {
            canvas.setPrefSize(224, 121);
        } else if (graphType.equals("center")) {
            canvas.setPrefSize(224, 121);
        } else {
            canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        }

        if (subgraph.isEmpty()) {
            scrollPane.setContent(canvas);
            return;
        }

        Map<String, Point2D> customLayout = getLayoutForGraphType(graphType);
        double centerX = canvas.getPrefWidth() / 2;
        double centerY = canvas.getPrefHeight() / 2;

        if (customLayout == null || customLayout.isEmpty() || !customLayout.keySet().containsAll(subgraph.vertices)) {
            customLayout = calculateGraphLayout(subgraph, centerX, centerY,
                    graphType.equals("mediana") || graphType.equals("center") ? 8 : VERTEX_RADIUS);
            setLayoutForGraphType(graphType, customLayout);
        }

        Map<String, List<Edge>> edgesByPair = groupParallelEdges(subgraph);
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
                Point2D vertexPos = customLayout.get(firstEdge.source);
                if (vertexPos != null) {
                    for (int i = 0; i < parallelEdges.size(); i++) {
                        Edge edge = parallelEdges.get(i);
                        drawLoop(canvas, vertexPos,
                                graphType.equals("mediana") || graphType.equals("center") ? 8 : VERTEX_RADIUS,
                                subgraph.isDirected, edge.label, i, parallelEdges.size(), allLabels,
                                edgeLabelConnections, allEdgesInGroup);
                    }
                }
            } else {
                Point2D sourcePos = customLayout.get(firstEdge.source);
                Point2D destPos = customLayout.get(firstEdge.destination);

                if (sourcePos != null && destPos != null) {
                    for (int i = 0; i < parallelEdges.size(); i++) {
                        Edge edge = parallelEdges.get(i);
                        drawEdge(canvas, sourcePos, destPos, edge.label,
                                subgraph.isDirected,
                                graphType.equals("mediana") || graphType.equals("center") ? 8 : VERTEX_RADIUS,
                                i, parallelEdges.size(), allLabels, edgeLabelConnections, allEdgesInGroup);
                    }
                }
            }
        }

        setupEdgeLabelConnections(edgeLabelConnections);

        for (Map.Entry<String, Point2D> entry : customLayout.entrySet()) {
            String vertex = entry.getKey();
            Point2D position = entry.getValue();
            drawVertex(canvas, position.getX(), position.getY(),
                    graphType.equals("mediana") || graphType.equals("center") ? 8 : VERTEX_RADIUS, vertex);
        }

        scrollPane.setContent(canvas);
        centerGraphView(scrollPane);
    }

    private void drawEdge(Pane canvas, Point2D source, Point2D target, String label,
            boolean isDirected, double radius, int parallelIndex, int totalEdges,
            List<String> allLabels, List<EdgeLabelConnection> connections,
            List<javafx.scene.shape.Shape> allEdges) {

        double arrowLength = 15;
        double arrowWidth = 8;

        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double unitX = dx / distance;
        double unitY = dy / distance;

        double offset = 0;
        if (totalEdges > 1) {
            double maxOffset = Math.min(12.0, Math.max(0, radius - 2));
            offset = (parallelIndex - (totalEdges - 1) / 2.0) * (maxOffset / Math.max(1, totalEdges - 1));
        }

        double offsetX = -unitY * offset;
        double offsetY = unitX * offset;

        double fullDist = distance;
        double innerGap = Math.min(fullDist * 0.25, radius);

        double adjustedSourceX = source.getX() + offsetX + unitX * innerGap;
        double adjustedSourceY = source.getY() + offsetY + unitY * innerGap;

        double adjustedTargetX = target.getX() + offsetX - unitX * innerGap;
        double adjustedTargetY = target.getY() + offsetY - unitY * innerGap;

        Color edgeColor = parallelIndex < EDGE_COLORS.length ? EDGE_COLORS[parallelIndex] : Color.BLACK;
        final Color hoverColor = getLighter(edgeColor);

        Line line = new Line(adjustedSourceX, adjustedSourceY, adjustedTargetX, adjustedTargetY);
        line.setStroke(edgeColor);
        line.setStrokeWidth(2);

        canvas.getChildren().add(line);

        if (allEdges != null) {
            allEdges.add(line);
        }

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
            line.setStroke(edgeColor);
            line.setStrokeWidth(2);
            for (EdgeLabelConnection c : connections) {
                if (c.edge == line && c.labelPart != null && c.normalTextColor != null) {
                    c.labelPart.setFill((javafx.scene.paint.Color) c.normalTextColor);
                }
            }
        });

        if (isDirected) {
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
                        nonEmptyLabels, textColors, false, connections, allEdges);
            }
        }

        connections.add(new EdgeLabelConnection(line, null, parallelIndex, null, null));
    }

    private void drawLoop(Pane canvas, Point2D vertex, double radius, boolean isDirected,
            String label, int loopIndex, int totalLoops, List<String> allLabels,
            List<EdgeLabelConnection> connections, List<javafx.scene.shape.Shape> allEdges) {

        Color[] loopColors = { Color.BLACK, Color.RED, Color.ORANGE, Color.GREEN };
        Color edgeColor = loopIndex < loopColors.length ? loopColors[loopIndex] : Color.BLACK;
        final Color hoverColor = getLighter(edgeColor);

        double anchorX = vertex.getX() - radius;
        double anchorY = vertex.getY() + (loopIndex - (totalLoops - 1) / 2.0) * (radius / 3.0);

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
        curve.setStroke(edgeColor);
        curve.setStrokeWidth(2);

        final CubicCurve finalCurve = curve;
        curve.setOnMouseEntered(e -> {
            finalCurve.setStroke(hoverColor);
            finalCurve.setStrokeWidth(3);
        });
        curve.setOnMouseExited(e -> {
            finalCurve.setStroke(edgeColor);
            finalCurve.setStrokeWidth(2);
        });

        canvas.getChildren().add(curve);

        if (isDirected) {
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
                arrowHead.setFill(hoverColor);
                finalCurve.setStroke(hoverColor);
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

                allEdges.add(curve);

                drawMultiColorText(canvas, labelX, labelY, nonEmptyLabels, textColors, false, connections,
                        allEdges);

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
                    fc.setStroke(edgeColor);
                    fc.setStrokeWidth(2);
                    for (EdgeLabelConnection c : connections) {
                        if (c.edge == fc && c.labelPart != null && c.normalTextColor != null) {
                            c.labelPart.setFill((javafx.scene.paint.Color) c.normalTextColor);
                        }
                    }
                });
            }
        }
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
                        correspondingEdge.setStroke(colors.get(edgeIndex % colors.size()));
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

    private void setupEdgeLabelConnections(List<EdgeLabelConnection> connections) {
        Map<javafx.scene.shape.Shape, List<EdgeLabelConnection>> map = new HashMap<>();
        for (EdgeLabelConnection connection : connections) {
            if (connection.edge != null) {
                map.computeIfAbsent(connection.edge, k -> new ArrayList<>()).add(connection);
            }
        }

        for (Map.Entry<javafx.scene.shape.Shape, List<EdgeLabelConnection>> entry : map.entrySet()) {
            javafx.scene.shape.Shape edge = entry.getKey();
            List<EdgeLabelConnection> conns = entry.getValue();

            final javafx.scene.paint.Paint originalStroke = edge.getStroke();
            final double originalWidth = edge.getStrokeWidth();

            edge.setOnMouseEntered(e -> {
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
                        javafx.scene.paint.Color labelHover = c.hoverTextColor != null
                                ? (javafx.scene.paint.Color) c.hoverTextColor
                                : (c.normalTextColor != null ? getLighter((javafx.scene.paint.Color) c.normalTextColor)
                                        : chosenHover);
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

    private void centerGraphView(ScrollPane scrollPane) {
        if (scrollPane.getContent() != null) {
            javafx.application.Platform.runLater(() -> {
                scrollPane.setHvalue(0.5);
                scrollPane.setVvalue(0.3);
            });
        }
    }

    private Map<String, Point2D> getLayoutForGraphType(String graphType) {
        switch (graphType) {
            case "main":
                return layoutGraph;
            case "mediana":
                return layoutMediana;
            case "center":
                return layoutCenter;
            default:
                return layoutGraph;
        }
    }

    private void setLayoutForGraphType(String graphType, Map<String, Point2D> layout) {
        switch (graphType) {
            case "main":
                layoutGraph = layout;
                break;
            case "mediana":
                layoutMediana = layout;
                break;
            case "center":
                layoutCenter = layout;
                break;
        }
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

    private String generateNextVertexName() {
        if (graphData.vertices.isEmpty()) {
            return "A";
        }

        List<String> sortedVertices = new ArrayList<>(graphData.vertices);
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
            return lastVertex + "A";
        }
    }

    @FXML
    private void save() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar grafo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(graphData.getState());
            modificationText.setText("Grafo guardado: " + file.getName());
        } catch (Exception e) {
            modificationText.setText("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void load() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar grafo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));

        Stage stage = (Stage) loadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file == null) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            GraphState loadedState = (GraphState) ois.readObject();
            graphData.setState(loadedState);
            layoutGraph.clear();
            history.clear();
            redoStack.clear();

            updateGraphDisplay();
            updateMetrics();
            modificationText.setText("Grafo cargado: " + file.getName());
        } catch (Exception e) {
            modificationText.setText("Error al cargar: " + e.getMessage());
        }
    }

    @FXML
    private void calculate() {
        calculateDistance();
    }
}