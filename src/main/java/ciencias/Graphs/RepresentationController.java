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
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
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

            boolean requestedDirected = edgeDirection.isSelected();
            if (hasEdges) {
                if (isDirected != requestedDirected) {
                    throw new IllegalStateException(
                            "No se pueden mezclar aristas dirigidas y no dirigidas en el mismo grafo");
                }
            } else {
                isDirected = requestedDirected;
                hasEdges = true;
            }

            if (source.equals(destination)) {
                boolean existsLoop = edges.stream().anyMatch(e -> e.isLoop && e.source.equals(source));
                if (existsLoop) {
                    throw new IllegalStateException("Ya existe un bucle en el vertice " + source);
                }
                edges.add(new Edge(source, destination, label));
                return;
            }

            if (!isDirected) {

                String a = source.compareTo(destination) <= 0 ? source : destination;
                String b = source.compareTo(destination) <= 0 ? destination : source;
                boolean exists = edges.stream().anyMatch(e -> !e.isLoop &&
                        ((e.source.equals(a) && e.destination.equals(b))
                                || (e.source.equals(b) && e.destination.equals(a))));
                if (exists) {
                    throw new IllegalStateException("Ya existe una arista entre " + source + " y " + destination);
                }
                edges.add(new Edge(a, b, label));
            } else {

                boolean exists = edges.stream()
                        .anyMatch(e -> e.source.equals(source) && e.destination.equals(destination));
                if (exists) {
                    throw new IllegalStateException("Ya existe una arista dirigida de " + source + " a " + destination);
                }
                edges.add(new Edge(source, destination, label));
            }
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
        modificationText.setText("Añadidos vertices: " + String.join(", ", addedVertices));
    }

    @FXML
    private void vertDelete() {
        String vertex = vertDelete.getText().trim().toUpperCase();
        if (vertex.isEmpty()) {
            modificationText.setText("Error: Ingrese un vertice a eliminar");
            return;
        }

        if (!graphData.hasVertex(vertex)) {
            modificationText.setText("Error: El vertice " + vertex + " no existe");
            return;
        }

        saveState();
        graphData.removeVertex(vertex);
        vertDelete.clear();
        updateGraphDisplay();
        updateMetrics();
        modificationText.setText("Vertice " + vertex + " eliminado");
    }

    @FXML
    private void edgeAdd() {
        String source = edgeOrg.getText().trim().toUpperCase();
        String destination = edgeDest.getText().trim().toUpperCase();
        String weightText = edgeWeightNotation.getText().trim();

        if (source.isEmpty()) {
            modificationText.setText("Error: Ingrese al menos el vertice origen");
            return;
        }

        if (destination.isEmpty()) {
            destination = source;
        }

        if (!graphData.hasVertex(source) || !graphData.hasVertex(destination)) {
            modificationText.setText("Error: Los vertices deben existir");
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
                modificationText.setText("Error: Peso debe ser un numero");
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
            modificationText.setText("Error: Ingrese al menos el vertice origen");
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
                modificationText.setText("Error: Peso debe ser un numero");
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
        modificationText.setText("Accion deshecha");
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
        modificationText.setText("Accion rehecha");
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
        operationText.setText("Error: Ingrese ambos vertices");
        return;
    }

    if (!graphData.hasVertex(item1) || !graphData.hasVertex(item2)) {
        operationText.setText("Error: Los vertices deben existir");
        return;
    }

    double distance = calculateBellmanFordDistance(item1, item2);
    if (Double.isInfinite(distance)) {
        operationText.setText("No existe camino entre " + item1 + " y " + item2);
    } else {
        operationText.setText("Distancia entre " + item1 + " y " + item2 + ": " + (int) distance);
    }

    matrixTabPane.getTabs().clear();
    
    
    List<double[][]> iterations = computeFloydWarshallIterations();
    if (!iterations.isEmpty()) {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        
        for (int it = 0; it < iterations.size(); it++) {
            double[][] m = iterations.get(it);
            String title;
            if (it == 0) {
                title = "Inicial";
            } else {
                title = "i = " + (it - 1);
            }

            Tab tab = new Tab(title);
            GridPane matrixGrid = createDoubleMatrixGrid(m, vertices, vertices, 
                    "Matriz de Distancias - " + title);
            tab.setContent(new ScrollPane(matrixGrid));
            matrixTabPane.getTabs().add(tab);
        }
    }

    
    List<String> bellmanEquations = computeFinalBellmanEquations(item1, item2);
    if (!bellmanEquations.isEmpty()) {
        Tab bellmanTab = new Tab("Algoritmo de Bellman");
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(6);
        content.setPadding(new javafx.geometry.Insets(8));

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset=\"utf-8\">\n");
        html.append("<script src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\"></script>\n");
        html.append("<style>body{font-family:Consolas,monospace;padding:10px;font-size:16px;line-height:1.4;}</style>");
        html.append("</head><body>\n");
        html.append("<h4>Calculos para camino: " + item1 + " → " + item2 + "</h4>\n");
        
        for (String eq : bellmanEquations) {
            html.append("<div style=\"margin-bottom:8px;\">\\(").append(eq).append("\\)</div>\n");
        }
        html.append("</body></html>");
        webEngine.loadContent(html.toString());
        bellmanTab.setContent(webView);
        matrixTabPane.getTabs().add(bellmanTab);
    }
}

    private List<String> computeFinalBellmanEquations(String source, String target) {
        List<String> equations = new ArrayList<>();
        List<String> vertices = new ArrayList<>(graphData.vertices);

        if (vertices.isEmpty() || !graphData.hasVertex(source) || !graphData.hasVertex(target)) {
            return equations;
        }

        Map<String, Double> distances = new HashMap<>();
        for (String vertex : vertices) {
            distances.put(vertex, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);

        Map<String, String> predecessors = new HashMap<>();

        for (int i = 0; i < vertices.size() - 1; i++) {
            for (Edge edge : graphData.edges) {
                double edgeWeight = getWeight(edge);
                if (distances.get(edge.source) + edgeWeight < distances.get(edge.destination)) {
                    distances.put(edge.destination, distances.get(edge.source) + edgeWeight);
                    predecessors.put(edge.destination, edge.source);
                }
                if (!graphData.isDirected) {
                    if (distances.get(edge.destination) + edgeWeight < distances.get(edge.source)) {
                        distances.put(edge.source, distances.get(edge.destination) + edgeWeight);
                        predecessors.put(edge.source, edge.destination);
                    }
                }
            }
        }

        if (Double.isInfinite(distances.get(target))) {
            equations.add("\\text{No existe camino entre } " + source + " \\text{ y } " + target);
            return equations;
        }

        List<String> path = new ArrayList<>();
        String current = target;
        while (current != null && !current.equals(source)) {
            path.add(0, current);
            current = predecessors.get(current);
        }
        path.add(0, source);

        for (int i = 0; i < path.size() - 1; i++) {
            String u = path.get(i);
            String v = path.get(i + 1);

            double weight = 0;
            for (Edge edge : graphData.edges) {
                if ((edge.source.equals(u) && edge.destination.equals(v)) ||
                        (!graphData.isDirected && edge.source.equals(v) && edge.destination.equals(u))) {
                    weight = getWeight(edge);
                    break;
                }
            }

            double distU = distances.get(u);
            double distV = distances.get(v);

            String equation = "\\lambda_{" + vertexToNumber(v) + "} = " +
                    "\\lambda_{" + vertexToNumber(u) + "} + v_{" +
                    vertexToNumber(u) + vertexToNumber(v) + "} = " +
                    formatDouble(distU) + " + " + formatDouble(weight) + " = " +
                    formatDouble(distV);
            equations.add(equation);
        }

        equations.add("\\text{Distancia final: } \\lambda_{" + vertexToNumber(target) + "} = " +
                formatDouble(distances.get(target)));

        return equations;
    }

    private String formatDouble(double v) {
        if (Double.isInfinite(v))
            return "\\infty";
        if (Math.abs(v - Math.round(v)) < 1e-9)
            return String.valueOf((long) Math.round(v));
        return String.format("%.3f", v);
    }

    private int vertexToNumber(String v) {
        if (v == null || v.isEmpty())
            return 0;
        char c = v.toUpperCase().charAt(0);
        if (c >= 'A' && c <= 'Z')
            return c - 'A' + 1;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            return Math.abs(v.hashCode()) % 1000;
        }
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
            boolean unreachable = false;
            for (int j = 0; j < vertices.size(); j++) {
                if (i == j)
                    continue;
                if (distances[i][j] == Double.POSITIVE_INFINITY) {
                    unreachable = true;
                    break;
                }
                maxDistance = Math.max(maxDistance, distances[i][j]);
            }
            if (unreachable)
                maxDistance = Double.POSITIVE_INFINITY;
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

    private List<double[][]> computeFloydWarshallIterations() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        int n = vertices.size();
        List<double[][]> iterations = new ArrayList<>();
        if (n == 0)
            return iterations;

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
            double w = getWeight(edge);

            dist[u][v] = Math.min(dist[u][v], w);
            if (!graphData.isDirected) {
                dist[v][u] = Math.min(dist[v][u], w);
            }
        }

        iterations.add(copyDoubleMatrix(dist));

        for (int k = 0; k < n; k++) {
            double[][] newDist = copyDoubleMatrix(dist);

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Double.POSITIVE_INFINITY &&
                            dist[k][j] != Double.POSITIVE_INFINITY &&
                            dist[i][k] + dist[k][j] < dist[i][j]) {
                        newDist[i][j] = dist[i][k] + dist[k][j];
                    }
                }
            }

            dist = newDist;
            iterations.add(copyDoubleMatrix(dist));
        }

        return iterations;
    }

    private double[][] copyDoubleMatrix(double[][] src) {
        if (src == null)
            return new double[0][0];
        int r = src.length;
        if (r == 0)
            return new double[0][0];
        int c = src[0].length;
        double[][] dst = new double[r][c];
        for (int i = 0; i < r; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, c);
        }
        return dst;
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
            boolean unreachable = false;
            for (int j = 0; j < vertices.size(); j++) {
                if (idx == j)
                    continue;
                if (distances[idx][j] == Double.POSITIVE_INFINITY) {
                    unreachable = true;
                    break;
                }
                sum += distances[idx][j];
            }
            if (unreachable)
                sumDistances.put(vertex, Double.POSITIVE_INFINITY);
            else
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

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                    continue;
                }
                String vi = vertices.get(i);
                String vj = vertices.get(j);
                int val = 0;
                for (Edge e : graphData.edges) {
                    if (e.source.equals(vi) && e.destination.equals(vj))
                        val += 1;
                    if (e.source.equals(vj) && e.destination.equals(vi))
                        val -= 1;
                    if (!graphData.isDirected) {
                        if ((e.source.equals(vi) && e.destination.equals(vj))
                                || (e.source.equals(vj) && e.destination.equals(vi)))
                            val = 1;
                    }
                }
                matrix[i][j] = val;
            }
        }

        return createIntMatrixGrid(matrix, vertices, vertices, "Matriz de Adyacencia de Vertices");
    }

    private GridPane createEdgeAdjacencyMatrix() {
        List<Edge> edges = new ArrayList<>(graphData.edges);
        int n = edges.size();

        String[][] sm = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    sm[i][j] = "0";
                    continue;
                }
                Edge ei = edges.get(i);
                Edge ej = edges.get(j);
                String cell = "0";
                List<String> common = new ArrayList<>();
                if (ei.source.equals(ej.source) || ei.source.equals(ej.destination))
                    common.add(ei.source);
                if (ei.destination.equals(ej.source) || ei.destination.equals(ej.destination))
                    common.add(ei.destination);
                if (!common.isEmpty()) {
                    String v = common.get(0);
                    int si = 0;
                    int sj = 0;
                    if (graphData.isDirected) {
                        if (ei.destination.equals(v))
                            si = -1;
                        else if (ei.source.equals(v))
                            si = 1;
                        if (ej.destination.equals(v))
                            sj = -1;
                        else if (ej.source.equals(v))
                            sj = 1;
                    } else {
                        si = 1;
                        sj = 1;
                    }
                    cell = "(" + si + "," + sj + ")";
                }
                sm[i][j] = cell;
            }
        }

        List<String> edgeNames = edgeLabels(edges);
        return createStringMatrixGrid(sm, edgeNames, edgeNames, "Matriz de Adyacencia de Aristas");
    }

    private GridPane createIncidenceMatrix() {
        List<String> vertices = new ArrayList<>(graphData.vertices);
        List<Edge> edges = graphData.edges;
        int[][] matrix = new int[vertices.size()][edges.size()];

        for (int j = 0; j < edges.size(); j++) {
            Edge edge = edges.get(j);
            int sourceIdx = vertices.indexOf(edge.source);
            int destIdx = vertices.indexOf(edge.destination);

            if (graphData.isDirected) {
                if (sourceIdx != -1)
                    matrix[sourceIdx][j] = 1;
                if (destIdx != -1)
                    matrix[destIdx][j] = -1;
            } else {
                if (sourceIdx != -1)
                    matrix[sourceIdx][j] = 1;
                if (destIdx != -1)
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
        List<List<String>> cycles = findSimpleCycles(graphData, 100);
        List<Edge> edges = new ArrayList<>(graphData.edges);
        if (cycles.isEmpty()) {
            int[][] matrix = new int[1][edges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Circuitos"), edgeLabels(edges), "Matriz de Circuitos");
        }

        List<List<String>> reorientedCycles = reorientCyclesClockwise(cycles);

        int[][] matrix = new int[reorientedCycles.size()][edges.size()];
        for (int i = 0; i < reorientedCycles.size(); i++) {
            List<String> cycle = reorientedCycles.get(i);
            int m = cycle.size();
            for (int k = 0; k < m; k++) {
                String u = cycle.get(k);
                String v = cycle.get((k + 1) % m);
                for (int j = 0; j < edges.size(); j++) {
                    Edge e = edges.get(j);
                    if (!graphData.isDirected) {
                        if ((e.source.equals(u) && e.destination.equals(v))
                                || (e.source.equals(v) && e.destination.equals(u))) {
                            matrix[i][j] = 1;
                        }
                    } else {
                        if (e.source.equals(u) && e.destination.equals(v))
                            matrix[i][j] = 1;
                        else if (e.source.equals(v) && e.destination.equals(u))
                            matrix[i][j] = -1;
                    }
                }
            }
        }

        List<String> rowLabels = new ArrayList<>();
        for (int i = 0; i < reorientedCycles.size(); i++) {
            rowLabels.add("C" + (i + 1));
        }

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(edges), "Matriz de Circuitos");
    }

    private List<List<String>> reorientCyclesClockwise(List<List<String>> cycles) {
        List<List<String>> reoriented = new ArrayList<>();

        for (List<String> cycle : cycles) {
            List<String> clockwiseCycle = reorientSingleCycleClockwise(cycle);
            reoriented.add(clockwiseCycle);
        }

        return reoriented;
    }

    private List<String> reorientSingleCycleClockwise(List<String> cycle) {
        if (cycle.size() < 3)
            return cycle;

        String leftmostVertex = findLeftmostVertex(cycle);
        int startIndex = cycle.indexOf(leftmostVertex);

        List<String> reordered = new ArrayList<>();
        for (int i = 0; i < cycle.size(); i++) {
            reordered.add(cycle.get((startIndex + i) % cycle.size()));
        }

        if (!isClockwise(reordered)) {
            Collections.reverse(reordered);

            int newStart = reordered.indexOf(leftmostVertex);
            if (newStart > 0) {
                Collections.rotate(reordered, -newStart);
            }
        }

        return reordered;
    }

    private String findLeftmostVertex(List<String> cycle) {
        String leftmost = cycle.get(0);
        for (String vertex : cycle) {
            if (vertex.compareTo(leftmost) < 0) {
                leftmost = vertex;
            }
        }
        return leftmost;
    }

    private boolean isClockwise(List<String> cycle) {
        if (cycle.size() < 3)
            return true;

        return isCycleInNaturalOrder(cycle);
    }

    private boolean isCycleInNaturalOrder(List<String> cycle) {
        for (int i = 0; i < cycle.size() - 1; i++) {
            if (cycle.get(i).compareTo(cycle.get(i + 1)) > 0) {
                return false;
            }
        }
        return cycle.get(cycle.size() - 1).compareTo(cycle.get(0)) > 0;
    }

    private GridPane createFundamentalCircuitMatrix() {
        List<Edge> allEdges = new ArrayList<>(graphData.edges);
        List<String> vertices = new ArrayList<>(graphData.vertices);

        if (vertices.isEmpty() || allEdges.isEmpty()) {
            int[][] matrix = new int[1][allEdges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Circuitos Fundamentales"),
                    edgeLabels(allEdges), "Matriz de Circuitos Fundamentales");
        }

        Set<Edge> mstEdges = computeMSTEdges(graphData);
        List<Edge> mstEdgeList = new ArrayList<>(mstEdges);

        List<Edge> chords = new ArrayList<>();
        for (Edge e : allEdges) {
            if (!mstEdges.contains(e)) {
                chords.add(e);
            }
        }

        if (chords.isEmpty()) {
            int[][] matrix = new int[1][allEdges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Circuitos Fundamentales"),
                    edgeLabels(allEdges), "Matriz de Circuitos Fundamentales");
        }

        Map<String, List<Edge>> mstAdj = new HashMap<>();
        for (Edge e : mstEdgeList) {
            mstAdj.computeIfAbsent(e.source, k -> new ArrayList<>()).add(e);
            mstAdj.computeIfAbsent(e.destination, k -> new ArrayList<>()).add(e);
        }

        int[][] matrix = new int[chords.size()][allEdges.size()];
        List<String> rowLabels = new ArrayList<>();

        for (int i = 0; i < chords.size(); i++) {
            Edge chord = chords.get(i);

            List<String> path = findPathInTree(mstAdj, chord.source, chord.destination);

            if (path.isEmpty())
                continue;

            List<String> cycle = new ArrayList<>(path);

            cycle = reorientSingleCycleClockwise(cycle);

            for (int j = 0; j < cycle.size(); j++) {
                String u = cycle.get(j);
                String v = cycle.get((j + 1) % cycle.size());

                for (int k = 0; k < allEdges.size(); k++) {
                    Edge e = allEdges.get(k);

                    if (matchesEdge(e, u, v)) {
                        if (graphData.isDirected) {
                            if (e.source.equals(u) && e.destination.equals(v)) {
                                matrix[i][k] = 1;
                            } else if (e.source.equals(v) && e.destination.equals(u)) {
                                matrix[i][k] = -1;
                            }
                        } else {
                            matrix[i][k] = 1;
                        }
                    }
                }
            }

            rowLabels.add("CF" + (i + 1) + "(" + chord.source + "-" + chord.destination + ")");
        }

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(allEdges),
                "Matriz de Circuitos Fundamentales");
    }

    private List<String> findPathInTree(Map<String, List<Edge>> treeAdj, String start, String end) {
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.offer(start);
        visited.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(end)) {

                List<String> path = new ArrayList<>();
                String node = end;
                while (node != null) {
                    path.add(node);
                    node = parent.get(node);
                }
                Collections.reverse(path);
                return path;
            }

            for (Edge edge : treeAdj.getOrDefault(current, new ArrayList<>())) {
                String neighbor = edge.source.equals(current) ? edge.destination : edge.source;
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        return new ArrayList<>();
    }

    private boolean matchesEdge(Edge edge, String u, String v) {
        if (graphData.isDirected) {
            return (edge.source.equals(u) && edge.destination.equals(v)) ||
                    (edge.source.equals(v) && edge.destination.equals(u));
        } else {
            return (edge.source.equals(u) && edge.destination.equals(v)) ||
                    (edge.source.equals(v) && edge.destination.equals(u));
        }
    }

    private GridPane createCutSetMatrix() {
        List<Edge> edges = new ArrayList<>(graphData.edges);

        if (edges.isEmpty()) {
            int[][] matrix = new int[1][0];
            return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte"),
                    Collections.emptyList(), "Conjuntos de Corte");
        }

        List<Set<Edge>> cutSets = findAllMinimalCutSetsWeakConnectivity(graphData);

        if (cutSets.isEmpty()) {
            int[][] matrix = new int[1][edges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte"),
                    edgeLabels(edges), "Conjuntos de Corte");
        }

        int[][] matrix = new int[cutSets.size()][edges.size()];
        List<String> rowLabels = new ArrayList<>();

        for (int i = 0; i < cutSets.size(); i++) {
            Set<Edge> cutSet = cutSets.get(i);

            for (int j = 0; j < edges.size(); j++) {
                Edge e = edges.get(j);
                if (cutSet.contains(e)) {
                    if (graphData.isDirected) {

                        matrix[i][j] = 1;
                    } else {
                        matrix[i][j] = 1;
                    }
                } else {
                    matrix[i][j] = 0;
                }
            }

            rowLabels.add("CC" + (i + 1));
        }

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(edges), "Conjuntos de Corte");
    }

    private List<Set<Edge>> findAllMinimalCutSetsWeakConnectivity(Graph g) {
        List<Set<Edge>> minimalCuts = new ArrayList<>();
        List<String> vertices = new ArrayList<>(g.vertices);
        List<Edge> allEdges = new ArrayList<>(g.edges);

        if (vertices.size() < 2 || allEdges.isEmpty()) {
            return minimalCuts;
        }

        int maxCutSize = Math.min(4, allEdges.size());

        for (int size = 1; size <= maxCutSize; size++) {
            List<List<Edge>> combinations = generateCombinations(allEdges, size);

            for (List<Edge> candidateList : combinations) {
                Set<Edge> candidate = new HashSet<>(candidateList);

                if (disconnectsUnderlyingGraph(g, candidate)) {

                    boolean isMinimal = true;
                    for (Edge edge : candidate) {
                        Set<Edge> smaller = new HashSet<>(candidate);
                        smaller.remove(edge);
                        if (disconnectsUnderlyingGraph(g, smaller)) {
                            isMinimal = false;
                            break;
                        }
                    }

                    if (isMinimal) {

                        if (!containsCutSet(minimalCuts, candidate)) {
                            minimalCuts.add(candidate);
                        }
                    }
                }
            }
        }

        return minimalCuts;
    }

    private boolean disconnectsUnderlyingGraph(Graph g, Set<Edge> cutSet) {
        return countConnectedComponentsWeak(g, cutSet) > 1;
    }

    private int countConnectedComponentsWeak(Graph g, Set<Edge> removedEdges) {
        Set<String> visited = new HashSet<>();
        int componentCount = 0;

        for (String vertex : g.vertices) {
            if (!visited.contains(vertex)) {
                componentCount++;

                Queue<String> queue = new LinkedList<>();
                queue.offer(vertex);
                visited.add(vertex);

                while (!queue.isEmpty()) {
                    String current = queue.poll();

                    for (Edge edge : g.edges) {
                        if (removedEdges.contains(edge)) {
                            continue;
                        }

                        String neighbor = null;
                        if (edge.source.equals(current)) {
                            neighbor = edge.destination;
                        } else if (edge.destination.equals(current)) {
                            neighbor = edge.source;
                        }

                        if (neighbor != null && !visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.offer(neighbor);
                        }
                    }
                }
            }
        }

        return componentCount;
    }

    private boolean containsCutSet(List<Set<Edge>> cutSets, Set<Edge> newCut) {
        for (Set<Edge> existing : cutSets) {
            if (existing.size() == newCut.size() && existing.containsAll(newCut)) {
                return true;
            }
        }
        return false;
    }

    private <T> List<List<T>> generateCombinations(List<T> list, int k) {
        List<List<T>> result = new ArrayList<>();
        if (k <= 0 || k > list.size())
            return result;

        generateCombinationsHelper(list, k, 0, new ArrayList<>(), result);
        return result;
    }

    private <T> void generateCombinationsHelper(List<T> list, int k, int start,
            List<T> current, List<List<T>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            generateCombinationsHelper(list, k, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    private GridPane createFundamentalCutSetMatrix() {
        List<Edge> allEdges = new ArrayList<>(graphData.edges);
        List<String> vertices = new ArrayList<>(graphData.vertices);

        if (vertices.isEmpty() || allEdges.isEmpty()) {
            int[][] matrix = new int[1][allEdges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte Fundamentales"),
                    Collections.emptyList(), "Conjuntos de Corte Fundamentales");
        }

        Set<Edge> mstEdges = computeMSTEdges(graphData);
        List<Edge> mstEdgeList = new ArrayList<>(mstEdges);

        if (mstEdgeList.isEmpty()) {
            int[][] matrix = new int[1][allEdges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte Fundamentales"),
                    edgeLabels(allEdges), "Conjuntos de Corte Fundamentales");
        }

        int[][] matrix = new int[mstEdgeList.size()][allEdges.size()];
        List<String> rowLabels = new ArrayList<>();

        for (int i = 0; i < mstEdgeList.size(); i++) {
            Edge branch = mstEdgeList.get(i);

            Set<Edge> tempEdges = new HashSet<>(mstEdgeList);
            tempEdges.remove(branch);

            Set<Set<String>> components = findConnectedComponents(vertices, tempEdges);
            if (components.size() != 2)
                continue;

            Iterator<Set<String>> compIterator = components.iterator();
            Set<String> compA = compIterator.next();
            Set<String> compB = compIterator.next();

            for (int j = 0; j < allEdges.size(); j++) {
                Edge e = allEdges.get(j);
                boolean inA = compA.contains(e.source);
                boolean inB = compB.contains(e.source);
                boolean connects = (inA && compB.contains(e.destination)) ||
                        (inB && compA.contains(e.destination));

                if (connects) {
                    if (graphData.isDirected) {
                        if (compA.contains(e.source) && compB.contains(e.destination)) {
                            matrix[i][j] = 1;
                        } else if (compB.contains(e.source) && compA.contains(e.destination)) {
                            matrix[i][j] = -1;
                        }
                    } else {
                        matrix[i][j] = 1;
                    }
                }
            }

            rowLabels.add("CCF" + (i + 1) + "(" + branch.source + "-" + branch.destination + ")");
        }

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(allEdges),
                "Conjuntos de Corte Fundamentales");
    }

    private Set<Set<String>> findConnectedComponents(List<String> vertices, Set<Edge> edges) {
        Set<Set<String>> components = new HashSet<>();
        Set<String> visited = new HashSet<>();

        for (String vertex : vertices) {
            if (!visited.contains(vertex)) {
                Set<String> component = new HashSet<>();
                Queue<String> queue = new LinkedList<>();

                queue.offer(vertex);
                visited.add(vertex);
                component.add(vertex);

                while (!queue.isEmpty()) {
                    String current = queue.poll();

                    for (Edge edge : edges) {
                        String neighbor = null;
                        if (edge.source.equals(current)) {
                            neighbor = edge.destination;
                        } else if (edge.destination.equals(current)) {
                            neighbor = edge.source;
                        }

                        if (neighbor != null && !visited.contains(neighbor)) {
                            visited.add(neighbor);
                            component.add(neighbor);
                            queue.offer(neighbor);
                        }
                    }
                }

                components.add(component);
            }
        }

        return components;
    }

    private Set<Edge> computeMSTEdges(Graph g) {
        Set<Edge> mst = new LinkedHashSet<>();
        List<String> verts = new ArrayList<>(g.vertices);
        if (verts.isEmpty())
            return mst;

        List<Edge> edges = new ArrayList<>(g.edges);
        edges.sort(Comparator.comparingDouble(this::getWeight));

        Map<String, String> ufParent = new HashMap<>();
        for (String v : verts)
            ufParent.put(v, v);

        java.util.function.Function<String, String> findRoot = x -> {
            String r = x;
            while (!ufParent.get(r).equals(r)) {
                r = ufParent.get(r);
            }

            String cur = x;
            while (!ufParent.get(cur).equals(r)) {
                String next = ufParent.get(cur);
                ufParent.put(cur, r);
                cur = next;
            }
            return r;
        };

        for (Edge e : edges) {
            String u = e.source;
            String v = e.destination;
            if (!ufParent.containsKey(u) || !ufParent.containsKey(v))
                continue;
            String ru = findRoot.apply(u);
            String rv = findRoot.apply(v);
            if (!ru.equals(rv)) {
                ufParent.put(ru, rv);
                mst.add(e);
            }
            if (mst.size() >= Math.max(0, verts.size() - 1))
                break;
        }

        return mst;
    }

    private List<String> edgeLabels(List<Edge> edges) {
        List<String> labels = new ArrayList<>();
        for (Edge e : edges) {
            labels.add(e.source + "-" + e.destination + "(" + e.label + ")");
        }
        return labels;
    }

    private List<List<String>> findSimpleCycles(Graph g, int maxCycles) {
        if (g.isDirected) {
            return findSimpleCyclesDirected(g, maxCycles);
        }

        List<List<String>> cycles = new ArrayList<>();
        List<String> verts = new ArrayList<>(g.vertices);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < verts.size(); i++)
            index.put(verts.get(i), i);

        Set<String> seen = new HashSet<>();

        for (int s = 0; s < verts.size(); s++) {
            String start = verts.get(s);
            Deque<String> path = new ArrayDeque<>();
            path.addLast(start);
            dfsUndirectedCycles(g, start, start, s, path, cycles, index, seen, maxCycles);
            if (cycles.size() >= maxCycles)
                break;
        }

        return cycles;
    }

    private void dfsUndirectedCycles(Graph g, String start, String current, int startIndex, Deque<String> path,
            List<List<String>> cycles, Map<String, Integer> index, Set<String> seen, int maxCycles) {
        if (cycles.size() >= maxCycles)
            return;
        for (String neigh : g.getAdjacentVertices(current)) {
            int ni = index.getOrDefault(neigh, -1);
            if (ni < startIndex)
                continue;
            if (neigh.equals(start) && path.size() > 2) {
                List<String> cycle = new ArrayList<>(path);
                String key = canonicalizeUndirectedCycle(cycle, index);
                if (!seen.contains(key)) {
                    seen.add(key);
                    cycles.add(cycle);
                    if (cycles.size() >= maxCycles)
                        return;
                }
            } else if (!path.contains(neigh)) {
                path.addLast(neigh);
                dfsUndirectedCycles(g, start, neigh, startIndex, path, cycles, index, seen, maxCycles);
                path.removeLast();
                if (cycles.size() >= maxCycles)
                    return;
            }
        }
    }

    private String canonicalizeUndirectedCycle(List<String> cycle, Map<String, Integer> index) {
        int n = cycle.size();
        int minPos = 0;
        int minIdx = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int idx = index.getOrDefault(cycle.get(i), Integer.MAX_VALUE);
            if (idx < minIdx) {
                minIdx = idx;
                minPos = i;
            }
        }
        List<String> rotated = new ArrayList<>();
        for (int i = 0; i < n; i++)
            rotated.add(cycle.get((minPos + i) % n));

        List<String> rev = new ArrayList<>(rotated);
        Collections.reverse(rev);

        String a = String.join("|", rotated);
        String b = String.join("|", rev);
        return a.compareTo(b) <= 0 ? a : b;
    }

    private List<List<String>> findSimpleCyclesDirected(Graph g, int maxCycles) {
        List<List<String>> cycles = new ArrayList<>();
        List<String> verts = new ArrayList<>(g.vertices);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < verts.size(); i++)
            index.put(verts.get(i), i);

        for (int s = 0; s < verts.size(); s++) {
            String start = verts.get(s);
            Deque<String> path = new ArrayDeque<>();
            path.addLast(start);
            dfsDirectedCycles(g, start, start, s, path, cycles, index, maxCycles);
            if (cycles.size() >= maxCycles)
                break;
        }
        return filterCyclesByEdgeSets(g, cycles);
    }

    private void dfsDirectedCycles(Graph g, String start, String current, int startIndex, Deque<String> path,
            List<List<String>> cycles, Map<String, Integer> index, int maxCycles) {
        if (cycles.size() >= maxCycles)
            return;
        for (String neigh : getNeighborsBothDirections(g, current)) {
            if (neigh.equals(start) && path.size() > 1) {
                List<String> cycle = new ArrayList<>(path);
                int minIdx = Integer.MAX_VALUE;
                for (String v : cycle)
                    minIdx = Math.min(minIdx, index.getOrDefault(v, Integer.MAX_VALUE));
                if (minIdx == startIndex) {
                    cycles.add(cycle);
                    if (cycles.size() >= maxCycles)
                        return;
                }
            } else if (!path.contains(neigh)) {
                path.addLast(neigh);
                dfsDirectedCycles(g, start, neigh, startIndex, path, cycles, index, maxCycles);
                path.removeLast();
                if (cycles.size() >= maxCycles)
                    return;
            }
        }
    }

    private Set<String> getNeighborsBothDirections(Graph g, String vertex) {
        Set<String> neigh = new LinkedHashSet<>();
        for (Edge e : g.edges) {
            if (e.source.equals(vertex))
                neigh.add(e.destination);
            if (e.destination.equals(vertex))
                neigh.add(e.source);
        }
        return neigh;
    }

    private List<List<String>> filterCyclesByEdgeSets(Graph g, List<List<String>> cycles) {
        if (cycles == null || cycles.isEmpty())
            return cycles;

        List<Set<Edge>> edgeSets = new ArrayList<>();
        for (List<String> cycle : cycles) {
            Set<Edge> es = edgesFromVertexCycle(g, cycle);
            edgeSets.add(new HashSet<>(es));
        }

        int m = cycles.size();
        boolean[] remove = new boolean[m];

        for (int i = 0; i < m; i++) {
            if (edgeSets.get(i).size() <= 1)
                remove[i] = true;
        }

        for (int i = 0; i < m; i++) {
            if (remove[i])
                continue;
            for (int j = 0; j < m; j++) {
                if (i == j)
                    continue;
                if (remove[j])
                    continue;
                Set<Edge> a = edgeSets.get(i);
                Set<Edge> b = edgeSets.get(j);
                if (a.isEmpty() || b.isEmpty())
                    continue;
                if (b.containsAll(a) && b.size() > a.size()) {
                    remove[i] = true;
                    break;
                }
            }
        }

        List<List<String>> filtered = new ArrayList<>();
        for (int i = 0; i < m; i++)
            if (!remove[i])
                filtered.add(cycles.get(i));
        return filtered;
    }

    private Set<Edge> edgesFromVertexCycle(Graph g, List<String> cycle) {
        Set<Edge> result = new HashSet<>();
        if (cycle == null || cycle.size() < 2)
            return result;
        int n = cycle.size();
        for (int i = 0; i < n; i++) {
            String u = cycle.get(i);
            String v = cycle.get((i + 1) % n);
            for (Edge e : g.edges) {
                if (g.isDirected) {
                    if (e.source.equals(u) && e.destination.equals(v)) {
                        result.add(e);
                        break;
                    }
                } else {
                    String a = u.compareTo(v) <= 0 ? u : v;
                    String b = u.compareTo(v) <= 0 ? v : u;
                    if (e.source.equals(a) && e.destination.equals(b)) {
                        result.add(e);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private GridPane createPlaceholderMatrix(String matrixType) {
        GridPane grid = new GridPane();
        grid.add(new Text("Matriz " + matrixType + " no implementada"), 0, 0);
        return grid;
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
                    double v = matrix[i][j];
                    if (Math.abs(v - Math.round(v)) < 1e-9) {
                        value = String.valueOf((long) Math.round(v));
                    } else {
                        value = String.format("%.3f", v);
                    }
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
        int cols = rows > 0 ? matrix[0].length : 0;

        if (rowLabels.size() != rows) {

            List<String> adjustedRowLabels = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                if (i < rowLabels.size()) {
                    adjustedRowLabels.add(rowLabels.get(i));
                } else {
                    adjustedRowLabels.add("Fila " + (i + 1));
                }
            }
            rowLabels = adjustedRowLabels;
        }

        if (colLabels.size() != cols) {

            List<String> adjustedColLabels = new ArrayList<>();
            for (int i = 0; i < cols; i++) {
                if (i < colLabels.size()) {
                    adjustedColLabels.add(colLabels.get(i));
                } else {
                    adjustedColLabels.add("Col " + (i + 1));
                }
            }
            colLabels = adjustedColLabels;
        }

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

    private GridPane createStringMatrixGrid(String[][] matrix, List<String> rowLabels, List<String> colLabels,
            String title) {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);

        int rows = matrix.length;
        int cols = rows == 0 ? 0 : matrix[0].length;

        for (int i = 0; i <= cols; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(100));
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
                String value = matrix[i][j];
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
            canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        } else if (graphType.equals("center")) {
            canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        }

        if (graph.isEmpty()) {
            scrollPane.setContent(canvas);
            return;
        }

        Map<String, Point2D> customLayout = getLayoutForGraphType(graphType);
        double centerX = canvas.getPrefWidth() / 2;
        double centerY = canvas.getPrefHeight() / 2;

        if (customLayout == null || customLayout.isEmpty() || !customLayout.keySet().containsAll(graph.vertices)) {
            double radius = VERTEX_RADIUS;
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
                        double radius = VERTEX_RADIUS;
                        boolean hasReverse = false;
                        for (Edge check : graph.edges) {
                            if (check.source.equals(firstEdge.destination)
                                    && check.destination.equals(firstEdge.source)) {
                                hasReverse = true;
                                break;
                            }
                        }
                        drawEdge(canvas, sourcePos, destPos, edge.label,
                                graph.isDirected, radius, i, parallelEdges.size(),
                                allLabels, edgeLabelConnections, allEdgesInGroup, hasReverse);
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

    private void drawEdge(Pane canvas, Point2D source, Point2D target, String label,
            boolean isDirected, double radius, int parallelIndex, int totalEdges,
            List<String> allLabels, List<EdgeLabelConnection> connections,
            List<javafx.scene.shape.Shape> allEdges, boolean hasReverse) {

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

        if (isDirected || hasReverse) {
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

        if (hasReverse) {
            double angleBack = Math.atan2(adjustedSourceY - adjustedTargetY, adjustedSourceX - adjustedTargetX);
            double arrowBX = adjustedSourceX - arrowLength * Math.cos(angleBack);
            double arrowBY = adjustedSourceY - arrowLength * Math.sin(angleBack);

            double bx2 = arrowBX + arrowWidth * Math.cos(angleBack + Math.PI / 2);
            double by2 = arrowBY + arrowWidth * Math.sin(angleBack + Math.PI / 2);

            double bx3 = arrowBX + arrowWidth * Math.cos(angleBack - Math.PI / 2);
            double by3 = arrowBY + arrowWidth * Math.sin(angleBack - Math.PI / 2);

            Polygon arrowHeadBack = new Polygon();
            arrowHeadBack.getPoints().addAll(
                    adjustedSourceX - offsetX, adjustedSourceY - offsetY,
                    bx2, by2,
                    bx3, by3);
            arrowHeadBack.setFill(edgeColor);

            arrowHeadBack.setOnMouseEntered(e -> {
                arrowHeadBack.setFill(hoverColor);
                line.setStroke(hoverColor);
                line.setStrokeWidth(3);
            });

            arrowHeadBack.setOnMouseExited(e -> {
                arrowHeadBack.setFill(edgeColor);
                line.setStroke(edgeColor);
                line.setStrokeWidth(2);
            });

            canvas.getChildren().add(arrowHeadBack);
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