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
            // Enforce consistency of directed/undirected and at most one edge per relation
            boolean requestedDirected = edgeDirection.isSelected();
            if (hasEdges) {
                if (isDirected != requestedDirected) {
                    throw new IllegalStateException("No se pueden mezclar aristas dirigidas y no dirigidas en el mismo grafo");
                }
            } else {
                isDirected = requestedDirected;
                hasEdges = true;
            }

            // Loops: allow at most one loop per vertex
            if (source.equals(destination)) {
                boolean existsLoop = edges.stream().anyMatch(e -> e.isLoop && e.source.equals(source));
                if (existsLoop) {
                    throw new IllegalStateException("Ya existe un bucle en el vértice " + source);
                }
                edges.add(new Edge(source, destination, label));
                return;
            }

            if (!isDirected) {
                // canonicalize unordered pair to avoid duplicates for undirected edges
                String a = source.compareTo(destination) <= 0 ? source : destination;
                String b = source.compareTo(destination) <= 0 ? destination : source;
                boolean exists = edges.stream().anyMatch(e -> !e.isLoop &&
                        ((e.source.equals(a) && e.destination.equals(b)) || (e.source.equals(b) && e.destination.equals(a))));
                if (exists) {
                    throw new IllegalStateException("Ya existe una arista entre " + source + " y " + destination);
                }
                edges.add(new Edge(a, b, label));
            } else {
                // directed: allow one edge per ordered pair (but reverse edge allowed separately)
                boolean exists = edges.stream().anyMatch(e -> e.source.equals(source) && e.destination.equals(destination));
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
        List<double[][]> iterations = computeFloydWarshallIterations();
        if (iterations.isEmpty()) {
            Tab t = new Tab("Matriz de Distancias");
            t.setContent(new ScrollPane(new GridPane()));
            matrixTabPane.getTabs().add(t);
            return;
        }

        for (int it = 0; it < iterations.size()-1; it++) {
            double[][] m = iterations.get(it);
            String title;
            if (it == 0) title = "Inicial";
            else if (it == iterations.size() - 2) title = "Final";
            else title = "i=" + (it - 1);

            Tab tab = new Tab(title);
            GridPane matrixGrid = createMatrixGrid(m, "Matriz de Distancias - " + title);
            tab.setContent(new ScrollPane(matrixGrid));
            matrixTabPane.getTabs().add(tab);
        }

        // Bellman-Ford: show only the last iteration as chained LaTeX-like formulas
        List<BellmanStep> bellmanSteps = computeBellmanIterationsWithEquations(item1);
        if (!bellmanSteps.isEmpty()) {
            BellmanStep last = bellmanSteps.get(bellmanSteps.size() - 1);
            Tab bellmanTab = new Tab("Bellman - Cálculos");
            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(6);
            content.setPadding(new javafx.geometry.Insets(8));

            // Render using MathJax inside a WebView
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            StringBuilder html = new StringBuilder();
            html.append("<!doctype html><html><head><meta charset=\"utf-8\">\n");
            html.append("<script src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\"></script>\n");
            html.append("<style>body{font-family:Consolas,monospace;padding:10px;}</style>");
            html.append("</head><body>\n");
            for (String eq : last.equations) {
                html.append("<div style=\"margin-bottom:8px;font-size:14px;\">\\(").append(eq).append("\\)</div>\n");
            }
            html.append("</body></html>");
            webEngine.loadContent(html.toString());
            bellmanTab.setContent(webView);
            matrixTabPane.getTabs().add(bellmanTab);
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

    // --- Bellman-Ford detailed iteration helpers ---
    // Helper class to hold Bellman-Ford step data
    private static class BellmanStep {
        Map<String, Double> lambda;
        List<String> equations;

        BellmanStep(Map<String, Double> lambda, List<String> equations) {
            this.lambda = new LinkedHashMap<>(lambda);
            this.equations = new ArrayList<>(equations);
        }
    }

    private List<BellmanStep> computeBellmanIterationsWithEquations(String source) {
        List<BellmanStep> steps = new ArrayList<>();
        List<String> verts = new ArrayList<>(graphData.vertices);
        if (verts.isEmpty() || !graphData.hasVertex(source)) return steps;

        Map<String, Double> lambda = new LinkedHashMap<>();
        for (String v : verts) lambda.put(v, Double.POSITIVE_INFINITY);
        lambda.put(source, 0.0);

        // initial step (LaTeX)
        steps.add(new BellmanStep(lambda, Collections.singletonList("Inicial: \\lambda_{" + vertexToNumber(source) + "} = 0")));

        int iteration = 0;
        while (true) {
            iteration++;
            boolean anyChange = false;
            Map<String, Double> prev = new LinkedHashMap<>(lambda);
            List<String> equations = new ArrayList<>();

            for (String v : verts) {
                // gather predecessors of v
                List<Edge> preds = new ArrayList<>();
                for (Edge e : graphData.edges) {
                    if (e.destination.equals(v)) preds.add(e);
                    if (!graphData.isDirected && e.source.equals(v)) preds.add(new Edge(e.destination, e.source, e.label));
                }

                if (preds.isEmpty()) {
                    String eq = "\\lambda_{" + vertexToNumber(v) + "} = " + (Double.isInfinite(prev.get(v)) ? "\\infty" : formatDouble(prev.get(v)));
                    equations.add(eq);
                    continue;
                }

                List<String> symTerms = new ArrayList<>();
                List<String> evalTerms = new ArrayList<>();
                double best = prev.get(v);
                for (Edge p : preds) {
                    String u = p.source;
                    double w = getWeight(p);
                    double prevU = prev.getOrDefault(u, Double.POSITIVE_INFINITY);

                    // symbolic term like (\lambda_{1} + v_{13})
                    String sym = "(\\lambda_{" + vertexToNumber(u) + "} + v_{" + vertexToNumber(u) + "" + vertexToNumber(v) + "})";
                    symTerms.add(sym);

                    // evaluated term like (2 + 4) or (\infty)
                    String left = Double.isInfinite(prevU) ? "\\infty" : formatDouble(prevU);
                    String right = formatDouble(w);
                    String eval = "(" + left + " + " + right + ")";
                    evalTerms.add(eval);

                    if (!Double.isInfinite(prevU) && prevU + w < best) {
                        best = prevU + w;
                    }
                }


                // build chained LaTeX equation: \lambda_j=\min{(\lambda_u+v_uv),...}=\min{(prev_u+w),...}=result
                StringBuilder chain = new StringBuilder();
                chain.append("\\lambda_{").append(vertexToNumber(v)).append("}=\\min\\{");
                chain.append(String.join(",", symTerms));
                chain.append("\\}=\\min\\{");
                chain.append(String.join(",", evalTerms));
                chain.append("\\}=");
                chain.append(Double.isInfinite(best) ? "\\infty" : formatDouble(best));

                equations.add(chain.toString());

                // apply relaxation into lambda
                if (best != prev.get(v)) {
                    lambda.put(v, best);
                    anyChange = true;
                }
            }

            steps.add(new BellmanStep(lambda, equations));
            if (!anyChange) break;
            if (iteration > Math.max(1000, verts.size() * 5)) break; // safety cap
        }

        return steps;
    }

    private String formatDouble(double v) {
        if (Double.isInfinite(v)) return "∞";
        if (Math.abs(v - Math.round(v)) < 1e-9) return String.valueOf((long) Math.round(v));
        return String.format("%.3f", v);
    }

    private int vertexToNumber(String v) {
        if (v == null || v.isEmpty()) return 0;
        char c = v.toUpperCase().charAt(0);
        if (c >= 'A' && c <= 'Z') return c - 'A' + 1;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            return Math.abs(v.hashCode()) % 1000; // fallback
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
                if (i == j) continue;
                if (distances[i][j] == Double.POSITIVE_INFINITY) {
                    unreachable = true;
                    break;
                }
                maxDistance = Math.max(maxDistance, distances[i][j]);
            }
            if (unreachable) maxDistance = Double.POSITIVE_INFINITY;
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
        if (n == 0) return iterations;

        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) dist[i][j] = 0;
                else dist[i][j] = Double.POSITIVE_INFINITY;
            }
        }

        Map<String, Integer> vertexIndex = new HashMap<>();
        for (int i = 0; i < n; i++) vertexIndex.put(vertices.get(i), i);

        for (Edge edge : graphData.edges) {
            int u = vertexIndex.get(edge.source);
            int v = vertexIndex.get(edge.destination);
            double w = getWeight(edge);
            dist[u][v] = Math.min(dist[u][v], w);
            if (!graphData.isDirected) dist[v][u] = Math.min(dist[v][u], w);
        }

        // save initial
        iterations.add(copyDoubleMatrix(dist));
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != Double.POSITIVE_INFINITY && dist[k][j] != Double.POSITIVE_INFINITY) {
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                    }
                }
            }
            iterations.add(copyDoubleMatrix(dist));
            if (iterations.size() >= 2) {
                double[][] prev = iterations.get(iterations.size() - 2);
                double[][] cur = iterations.get(iterations.size() - 1);
                boolean different = false;
                outer: for (int ii = 0; ii < n; ii++) {
                    for (int jj = 0; jj < n; jj++) {
                        if (Double.compare(prev[ii][jj], cur[ii][jj]) != 0) {
                            different = true;
                            break outer;
                        }
                    }
                }
            }
        }

        return iterations;
    }

    private double[][] copyDoubleMatrix(double[][] src) {
        int r = src.length;
        if (r == 0) return new double[0][0];
        int c = src[0].length;
        double[][] dst = new double[r][c];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) dst[i][j] = src[i][j];
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
                if (idx == j) continue;
                if (distances[idx][j] == Double.POSITIVE_INFINITY) {
                    unreachable = true;
                    break;
                }
                sum += distances[idx][j];
            }
            if (unreachable) sumDistances.put(vertex, Double.POSITIVE_INFINITY);
            else sumDistances.put(vertex, sum);
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

        // For directed graphs: matrix[row][col] = (+1 if row->col exists) + (-1 if col->row exists)
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // adjacency diagonal must always be 0
                if (i == j) {
                    matrix[i][j] = 0;
                    continue;
                }
                String vi = vertices.get(i);
                String vj = vertices.get(j);
                int val = 0;
                for (Edge e : graphData.edges) {
                    if (e.source.equals(vi) && e.destination.equals(vj)) val += 1;
                    if (e.source.equals(vj) && e.destination.equals(vi)) val -= 1;
                    if (!graphData.isDirected) {
                        // count undirected as +1 for both endpoints
                        if ((e.source.equals(vi) && e.destination.equals(vj)) || (e.source.equals(vj) && e.destination.equals(vi))) val = 1;
                    }
                }
                matrix[i][j] = val;
            }
        }

        return createIntMatrixGrid(matrix, vertices, vertices, "Matriz de Adyacencia de Vértices");
    }

    private GridPane createEdgeAdjacencyMatrix() {
        List<Edge> edges = new ArrayList<>(graphData.edges);
        int n = edges.size();

        // Build a string matrix where each cell is either "0" or a pair like "(1,-1)" representing
        // the relation of ei and ej at their common vertex: value is (s_i,s_j) where s=1 if edge leaves the common vertex, -1 if edge enters it.
        String[][] sm = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // diagonal should be neutral/zero for adjacency matrices
                if (i == j) {
                    sm[i][j] = "0";
                    continue;
                }
                Edge ei = edges.get(i);
                Edge ej = edges.get(j);
                String cell = "0";
                // check common vertices
                List<String> common = new ArrayList<>();
                if (ei.source.equals(ej.source) || ei.source.equals(ej.destination)) common.add(ei.source);
                if (ei.destination.equals(ej.source) || ei.destination.equals(ej.destination)) common.add(ei.destination);
                // pick first common vertex if any
                if (!common.isEmpty()) {
                    String v = common.get(0);
                    int si = 0;
                    int sj = 0;
                    if (graphData.isDirected) {
                        // for ei relative to v
                        if (ei.destination.equals(v)) si = -1; // arrow points to v
                        else if (ei.source.equals(v)) si = 1; // arrow leaves v
                        // for ej relative to v
                        if (ej.destination.equals(v)) sj = -1;
                        else if (ej.source.equals(v)) sj = 1;
                    } else {
                        si = 1; sj = 1;
                    }
                    cell = "(" + si + "," + sj + ")";
                }
                sm[i][j] = cell;
            }
        }

        List<String> edgeNames = edgeLabels(edges);
        return createStringMatrixGrid(sm, edgeNames, edgeNames, "Matriz de Adyacencia de Aristas");
    }

    // Helper: compare if a set contains an edge by endpoints (ignore label)
    private boolean containsEdgeByEndpoints(Set<Edge> set, Edge target) {
        if (set == null || target == null) return false;
        for (Edge e : set) {
            if ((e.source.equals(target.source) && e.destination.equals(target.destination)) || (!graphData.isDirected && e.source.equals(target.destination) && e.destination.equals(target.source))) {
                return true;
            }
        }
        return false;
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
                if (sourceIdx != -1) matrix[sourceIdx][j] = 1; // arrow leaves source
                if (destIdx != -1) matrix[destIdx][j] = -1; // arrow points to destination
            } else {
                if (sourceIdx != -1) matrix[sourceIdx][j] = 1;
                if (destIdx != -1) matrix[destIdx][j] = 1;
            }
        }

        List<String> edgeLabels = new ArrayList<>();
        for (Edge edge : edges) {
            edgeLabels.add(edge.source + "-" + edge.destination + "(" + edge.label + ")");
        }

        return createIntMatrixGrid(matrix, vertices, edgeLabels, "Matriz de Incidencia");
    }

    private GridPane createCircuitMatrix() {
        // Find simple cycles (limited) and represent them as rows vs edges columns
        List<List<String>> cycles = findSimpleCycles(graphData, 100);
        List<Edge> edges = new ArrayList<>(graphData.edges);
        if (cycles.isEmpty()) {
            int[][] matrix = new int[1][edges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Circuitos"), edgeLabels(edges), "Matriz de Circuitos");
        }

        int[][] matrix = new int[cycles.size()][edges.size()];
        for (int i = 0; i < cycles.size(); i++) {
            List<String> cycle = cycles.get(i);
            int m = cycle.size();
            for (int k = 0; k < m; k++) {
                String u = cycle.get(k);
                String v = cycle.get((k + 1) % m);
                for (int j = 0; j < edges.size(); j++) {
                    Edge e = edges.get(j);
                    if (!graphData.isDirected) {
                        // undirected: mark 1 if edge connects u and v
                        if ((e.source.equals(u) && e.destination.equals(v)) || (e.source.equals(v) && e.destination.equals(u))) {
                            matrix[i][j] = 1;
                        }
                    } else {
                        // Use the canonical cycle order: if the edge orientation matches the traversal u->v => +1,
                        // if the edge is oriented v->u (opposite to traversal) => -1.
                        if (e.source.equals(u) && e.destination.equals(v)) matrix[i][j] = 1;
                        else if (e.source.equals(v) && e.destination.equals(u)) matrix[i][j] = -1;
                    }
                }
            }
        }

        List<String> rowLabels = new ArrayList<>();
        for (int i = 0; i < cycles.size(); i++) {
            rowLabels.add("C" + (i + 1));
        }

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(edges), "Matriz de Circuitos");
    }

    private GridPane createFundamentalCircuitMatrix() {
        List<Edge> allEdges = new ArrayList<>(graphData.edges);
        List<String> verts = new ArrayList<>(graphData.vertices);
        if (verts.isEmpty()) {
            int[][] matrix = new int[1][0];
            return createIntMatrixGrid(matrix, Arrays.asList("Circuitos Fundamentales"), Collections.emptyList(),
                    "Matriz de Circuitos Fundamentales");
        }

        // Compute Minimum Spanning Tree (Kruskal). For directed graphs treat edges as undirected for MST.
        Set<Edge> treeEdgeSet = computeMSTEdges(graphData);

        // chords are edges not in the MST
        List<Edge> chords = new ArrayList<>();
        for (Edge e : graphData.edges) {
            if (!treeEdgeSet.contains(e)) chords.add(e);
        }

        if (chords.isEmpty()) {
            int[][] matrix = new int[1][allEdges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Circuitos Fundamentales"), edgeLabels(allEdges),
                    "Matriz de Circuitos Fundamentales");
        }

        // Build adjacency for tree using actual tree edges
        Map<String, List<Edge>> treeAdj = new HashMap<>();
        for (Edge te : treeEdgeSet) {
            treeAdj.computeIfAbsent(te.source, k -> new ArrayList<>()).add(te);
            // allow traversal both ways
            treeAdj.computeIfAbsent(te.destination, k -> new ArrayList<>()).add(te);
        }


        List<int[]> rows = new ArrayList<>();
        for (Edge chord : chords) {
            // find vertex path in tree between chord.source and chord.destination
            List<String> pathVerts = findPathVerticesInTree(treeAdj, chord.source, chord.destination);
            // build cycle traversal: pathVerts from source->dest, then chord dest->source to close
            List<String> cycleVerts = new ArrayList<>(pathVerts);
            // close cycle by adding starting vertex at end implicitly when iterating pairs

            int[] row = new int[allEdges.size()];
            // traverse path edges in order
            for (int k = 0; k < pathVerts.size() - 1; k++) {
                String a = pathVerts.get(k);
                String b = pathVerts.get(k + 1);
                for (int c = 0; c < allEdges.size(); c++) {
                    Edge e = allEdges.get(c);
                    if (!graphData.isDirected) {
                        if ((e.source.equals(a) && e.destination.equals(b)) || (e.source.equals(b) && e.destination.equals(a))) row[c] = 1;
                    } else {
                        // Use canonical cycle order: if tree edge orientation matches traversal a->b => +1,
                        // otherwise if the edge is oriented b->a then mark -1.
                        if (e.source.equals(a) && e.destination.equals(b)) row[c] = 1;
                        else if (e.source.equals(b) && e.destination.equals(a)) row[c] = -1;
                    }
                }
            }
            // add chord edge (from chord.source -> chord.destination)
                for (int c = 0; c < allEdges.size(); c++) {
                    Edge e = allEdges.get(c);
                    if (e.source.equals(chord.source) && e.destination.equals(chord.destination)) {
                        row[c] = 1; // chord along its orientation => +1 (match traversal order)
                        break;
                    }
                    if (!graphData.isDirected && ((e.source.equals(chord.destination) && e.destination.equals(chord.source)) || (e.source.equals(chord.source) && e.destination.equals(chord.destination)))) {
                        row[c] = 1;
                        break;
                    }
                }
            rows.add(row);
        }

        int[][] matrix = new int[rows.size()][allEdges.size()];
        for (int r = 0; r < rows.size(); r++) matrix[r] = rows.get(r);

        List<String> rowLabels = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) rowLabels.add("CF" + (i + 1));

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(allEdges), "Matriz de Circuitos Fundamentales");
    }

    private GridPane createCutSetMatrix() {
        // Enumerate minimal edge cut-sets up to a bounded size (small graphs expected)
        int maxCutSize = 4;
        List<Set<Edge>> cutSets = findAllCutSets(graphData, maxCutSize);
        List<Edge> edges = new ArrayList<>(graphData.edges);

        if (cutSets.isEmpty()) {
            int[][] matrix = new int[1][edges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte"), edgeLabels(edges), "Conjuntos de Corte");
        }

        int[][] matrix = new int[cutSets.size()][edges.size()];
        List<String> rowLabels = new ArrayList<>();
        for (int i = 0; i < cutSets.size(); i++) {
            Set<Edge> cut = cutSets.get(i);
            // pick a starting vertex as the source of the first edge in the cut (if any)
            String start = null;
            if (!cut.isEmpty()) start = cut.iterator().next().source;
            Set<String> compA = componentWithoutEdges(graphData, start, cut);

            for (int j = 0; j < edges.size(); j++) {
                Edge e = edges.get(j);
                if (!cut.contains(e)) continue;
                if (!graphData.isDirected) {
                    matrix[i][j] = 1;
                } else {
                    boolean aIn = compA.contains(e.source);
                    boolean bIn = compA.contains(e.destination);
                    if (aIn && !bIn) matrix[i][j] = 1; // goes from A to B
                    else if (!aIn && bIn) matrix[i][j] = -1; // goes from B to A
                    else matrix[i][j] = 0;
                }
            }
            rowLabels.add("CC" + (i + 1));
        }

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(edges), "Conjuntos de Corte");
    }

    // Enumerate minimal cut-sets (edge removals that disconnect the graph)
    private List<Set<Edge>> findAllCutSets(Graph g, int maxSize) {
        List<Set<Edge>> result = new ArrayList<>();
        List<Edge> edges = new ArrayList<>(g.edges);
        int m = edges.size();
        if (m == 0)
            return result;

        // helper to check if removal of subset disconnects graph
        for (int size = 1; size <= Math.min(maxSize, m); size++) {
            // generate combinations of given size
            int[] idx = new int[size];
            for (int i = 0; i < size; i++) idx[i] = i;
            while (idx[0] <= m - size) {
                Set<Edge> subset = new HashSet<>();
                for (int k = 0; k < size; k++) subset.add(edges.get(idx[k]));

                if (removalDisconnects(g, subset)) {
                    // ensure minimal: no existing result is subset of this
                    boolean isMinimal = true;
                    for (Set<Edge> existing : result) {
                        if (subset.containsAll(existing)) {
                            isMinimal = false;
                            break;
                        }
                    }
                    if (isMinimal) {
                        result.add(subset);
                    }
                }

                // next combination
                int t = size - 1;
                idx[t]++;
                while (t > 0 && idx[t] >= m - (size - 1 - t)) {
                    t--;
                    idx[t]++;
                    for (int j = t + 1; j < size; j++) {
                        idx[j] = idx[j - 1] + 1;
                    }
                }
                if (idx[0] > m - size) break;
            }
        }

        return result;
    }

    private boolean removalDisconnects(Graph g, Set<Edge> forbidden) {
        if (g.vertices.isEmpty()) return false;
        // count components after removal
        int comps = countComponentsAfterRemoval(g, forbidden);
        return comps > 1;
    }

    private int countComponentsAfterRemoval(Graph g, Set<Edge> forbidden) {
        Set<String> seen = new HashSet<>();
        int compCount = 0;
        for (String v : g.vertices) {
            if (!seen.contains(v)) {
                compCount++;
                Deque<String> dq = new ArrayDeque<>();
                dq.add(v);
                seen.add(v);
                while (!dq.isEmpty()) {
                    String cur = dq.poll();
                    for (Edge e : g.edges) {
                        if (forbidden.contains(e)) continue;
                        String neigh = null;
                        if (e.source.equals(cur)) neigh = e.destination;
                        else if (!g.isDirected && e.destination.equals(cur)) neigh = e.source;
                        if (neigh != null && !seen.contains(neigh)) {
                            seen.add(neigh);
                            dq.add(neigh);
                        }
                    }
                }
            }
        }
        return compCount;
    }

    private Set<String> componentWithoutEdges(Graph g, String start, Set<Edge> forbidden) {
        Set<String> comp = new HashSet<>();
        if (start == null || !g.vertices.contains(start)) {
            // pick any vertex
            if (g.vertices.isEmpty()) return comp;
            start = g.vertices.iterator().next();
        }
        Deque<String> dq = new ArrayDeque<>();
        dq.add(start);
        comp.add(start);
        while (!dq.isEmpty()) {
            String v = dq.poll();
            for (Edge e : g.edges) {
                if (forbidden.contains(e)) continue;
                String neigh = null;
                if (e.source.equals(v)) neigh = e.destination;
                else if (!g.isDirected && e.destination.equals(v)) neigh = e.source;
                if (neigh != null && !comp.contains(neigh)) {
                    comp.add(neigh);
                    dq.add(neigh);
                }
            }
        }
        return comp;
    }

    // Similar to componentWithoutEdge but compare forbidden edge by endpoints (ignoring label)
    private Set<String> componentWithoutEdgeByEndpoints(Graph g, String start, Edge forbidden) {
        Set<String> comp = new HashSet<>();
        if (start == null || !g.vertices.contains(start)) {
            if (g.vertices.isEmpty()) return comp;
            start = g.vertices.iterator().next();
        }
        Deque<String> dq = new ArrayDeque<>();
        dq.add(start);
        comp.add(start);
        while (!dq.isEmpty()) {
            String v = dq.poll();
            for (Edge e : g.edges) {
                // compare endpoints ignoring label and direction for forbidden
                boolean isForbidden = false;
                if ( (e.source.equals(forbidden.source) && e.destination.equals(forbidden.destination)) ) {
                    isForbidden = true;
                }
                if (!g.isDirected && (e.source.equals(forbidden.destination) && e.destination.equals(forbidden.source))) {
                    isForbidden = true;
                }
                if (isForbidden) continue;

                String neigh = null;
                if (e.source.equals(v)) neigh = e.destination;
                else if (!g.isDirected && e.destination.equals(v)) neigh = e.source;
                if (neigh != null && !comp.contains(neigh)) {
                    comp.add(neigh);
                    dq.add(neigh);
                }
            }
        }
        return comp;
    }

    // Compute MST edges using Kruskal (treat graph as undirected for MST). Uses getWeight(edge).
    private Set<Edge> computeMSTEdges(Graph g) {
        Set<Edge> mst = new LinkedHashSet<>();
        List<String> verts = new ArrayList<>(g.vertices);
        if (verts.isEmpty()) return mst;

        List<Edge> edges = new ArrayList<>(g.edges);
        edges.sort(Comparator.comparingDouble(this::getWeight));

        // iterative union-find (DSU)
        Map<String, String> ufParent = new HashMap<>();
        for (String v : verts) ufParent.put(v, v);

        java.util.function.Function<String, String> findRoot = x -> {
            String r = x;
            while (!ufParent.get(r).equals(r)) {
                r = ufParent.get(r);
            }
            // path compression
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
            if (!ufParent.containsKey(u) || !ufParent.containsKey(v)) continue;
            String ru = findRoot.apply(u);
            String rv = findRoot.apply(v);
            if (!ru.equals(rv)) {
                ufParent.put(ru, rv);
                mst.add(e);
            }
            if (mst.size() >= Math.max(0, verts.size() - 1)) break;
        }

        return mst;
    }

    // Find edges along path between u and v inside the tree adjacency
    private List<Edge> findPathEdgesInTree(Map<String, List<Edge>> treeAdj, String u, String v, boolean directed) {
        List<Edge> result = new ArrayList<>();
        if (u == null || v == null) return result;
        // BFS on tree, store parent edge
        Deque<String> dq = new ArrayDeque<>();
        Map<String, Edge> parentEdge = new HashMap<>();
        Set<String> seen = new HashSet<>();
        dq.add(u);
        seen.add(u);
        boolean found = false;
        while (!dq.isEmpty() && !found) {
            String cur = dq.poll();
            List<Edge> neighs = treeAdj.getOrDefault(cur, Collections.emptyList());
            for (Edge e : neighs) {
                String neigh = e.source.equals(cur) ? e.destination : e.source;
                if (!seen.contains(neigh)) {
                    seen.add(neigh);
                    parentEdge.put(neigh, e);
                    dq.add(neigh);
                    if (neigh.equals(v)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        if (!seen.contains(v)) return result;
        // reconstruct path
        String cur = v;
        while (!cur.equals(u)) {
            Edge e = parentEdge.get(cur);
            if (e == null) break;
            result.add(e);
            cur = e.source.equals(cur) ? e.destination : e.source;
            // above toggling works because tree edges may be stored in either direction
        }
        return result;
    }

    // Find vertex path between u and v inside the tree adjacency (inclusive), returns list of vertices from u..v
    private List<String> findPathVerticesInTree(Map<String, List<Edge>> treeAdj, String u, String v) {
        List<String> result = new ArrayList<>();
        if (u == null || v == null) return result;
        Deque<String> dq = new ArrayDeque<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> seen = new HashSet<>();
        dq.add(u);
        seen.add(u);
        boolean found = false;
        while (!dq.isEmpty() && !found) {
            String cur = dq.poll();
            for (Edge e : treeAdj.getOrDefault(cur, Collections.emptyList())) {
                String neigh = e.source.equals(cur) ? e.destination : e.source;
                if (!seen.contains(neigh)) {
                    seen.add(neigh);
                    parent.put(neigh, cur);
                    dq.add(neigh);
                    if (neigh.equals(v)) { found = true; break; }
                }
            }
        }
        if (!seen.contains(v)) return result;
        String cur = v;
        LinkedList<String> path = new LinkedList<>();
        while (cur != null) {
            path.addFirst(cur);
            if (cur.equals(u)) break;
            cur = parent.get(cur);
        }
        return new ArrayList<>(path);
    }

    private List<String> edgeLabels(List<Edge> edges) {
        List<String> labels = new ArrayList<>();
        for (Edge e : edges) {
            labels.add(e.source + "-" + e.destination + "(" + e.label + ")");
        }
        return labels;
    }

    private Set<Edge> edgesFromVertexCycle(Graph g, List<String> cycle) {
        Set<Edge> result = new HashSet<>();
        if (cycle == null || cycle.size() < 2) return result;
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

    // Create a GridPane for string-valued matrices (used for edge-adjacency pair view)
    private GridPane createStringMatrixGrid(String[][] matrix, List<String> rowLabels, List<String> colLabels, String title) {
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

    private GridPane createFundamentalCutSetMatrix() {
        // Fresh MST-based fundamental cut-sets implementation
        List<Edge> allEdges = new ArrayList<>(graphData.edges);
        List<String> verts = new ArrayList<>(graphData.vertices);

        if (verts.isEmpty()) {
            int[][] matrix = new int[1][0];
            return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte Fundamentales"), Collections.emptyList(),
                    "Conjuntos de Corte Fundamentales");
        }

        // Build MST (treat graph as undirected for expansion minima)
        Set<Edge> treeEdgeSet = computeMSTEdges(graphData);
        List<Edge> treeEdges = new ArrayList<>(treeEdgeSet);

        if (treeEdges.isEmpty()) {
            int[][] matrix = new int[1][allEdges.size()];
            return createIntMatrixGrid(matrix, Arrays.asList("Conjuntos de Corte Fundamentales"), edgeLabels(allEdges),
                    "Conjuntos de Corte Fundamentales");
        }

        // Rows: one per tree edge (branch). Columns: all edges in graph (edge-based matrix).
        int rows = treeEdges.size();
        int cols = allEdges.size();
        int[][] matrix = new int[rows][cols];
        List<String> rowLabels = new ArrayList<>();

        // For each tree edge, compute component after removing that edge and include all edges crossing the partition
        // Build tree adjacency for the MST (undirected)
        Map<String, List<Edge>> treeAdj = new HashMap<>();
        for (Edge te : treeEdges) {
            treeAdj.computeIfAbsent(te.source, k -> new ArrayList<>()).add(te);
            treeAdj.computeIfAbsent(te.destination, k -> new ArrayList<>()).add(te);
        }

        for (int i = 0; i < treeEdges.size(); i++) {
            Edge branch = treeEdges.get(i);

            // BFS on the tree (treeAdj) ignoring the branch edge to get the component containing branch.source
            Set<String> comp = new HashSet<>();
            Deque<String> dq = new ArrayDeque<>();
            dq.add(branch.source);
            comp.add(branch.source);
            while (!dq.isEmpty()) {
                String cur = dq.poll();
                for (Edge te : treeAdj.getOrDefault(cur, Collections.emptyList())) {
                    // skip the removed branch (compare endpoints ignoring label)
                    boolean isBranch = (te.source.equals(branch.source) && te.destination.equals(branch.destination)) || (!graphData.isDirected && te.source.equals(branch.destination) && te.destination.equals(branch.source));
                    if (isBranch) continue;
                    String neigh = te.source.equals(cur) ? te.destination : te.source;
                    if (!comp.contains(neigh)) {
                        comp.add(neigh);
                        dq.add(neigh);
                    }
                }
            }
            // For every edge in the graph, include it in the cut if it has endpoints in different components
            for (int j = 0; j < allEdges.size(); j++) {
                Edge e = allEdges.get(j);
                boolean inLeft = comp.contains(e.source);
                boolean inRight = comp.contains(e.destination);
                boolean crossing = inLeft ^ inRight;
                if (crossing) {
                    if (!graphData.isDirected) matrix[i][j] = 1;
                    else {
                        if (inLeft && !inRight) matrix[i][j] = 1; // from left to right
                        else if (!inLeft && inRight) matrix[i][j] = -1; // from right to left
                    }
                }
            }

            rowLabels.add("TC" + (i + 1));
        }

        return createIntMatrixGrid(matrix, rowLabels, edgeLabels(allEdges), "Conjuntos de Corte Fundamentales");
    }

    // Helper: path from vertex to root using parent map
    @SuppressWarnings("unused")
    private List<String> pathToRoot(String v, Map<String, String> parent) {
        List<String> p = new ArrayList<>();
        String cur = v;
        while (cur != null) {
            p.add(cur);
            cur = parent.get(cur);
        }
        return p;
    }

    // Helper: find simple cycles by backtracking (limit to maxCycles)
    private List<List<String>> findSimpleCycles(Graph g, int maxCycles) {
        if (g.isDirected) {
            return findSimpleCyclesDirected(g, maxCycles);
        }

        // Undirected graphs: backtracking DFS with canonicalization to enumerate unique simple cycles
        List<List<String>> cycles = new ArrayList<>();
        List<String> verts = new ArrayList<>(g.vertices);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < verts.size(); i++) index.put(verts.get(i), i);

        Set<String> seen = new HashSet<>();

        for (int s = 0; s < verts.size(); s++) {
            String start = verts.get(s);
            Deque<String> path = new ArrayDeque<>();
            path.addLast(start);
            dfsUndirectedCycles(g, start, start, s, path, cycles, index, seen, maxCycles);
            if (cycles.size() >= maxCycles) break;
        }

        return cycles;
    }

    private void dfsUndirectedCycles(Graph g, String start, String current, int startIndex, Deque<String> path,
            List<List<String>> cycles, Map<String, Integer> index, Set<String> seen, int maxCycles) {
        if (cycles.size() >= maxCycles) return;
        for (String neigh : g.getAdjacentVertices(current)) {
            int ni = index.getOrDefault(neigh, -1);
            if (ni < startIndex) continue; // ensure smallest-index vertex in cycle is the start
            if (neigh.equals(start) && path.size() > 2) {
                List<String> cycle = new ArrayList<>(path);
                String key = canonicalizeUndirectedCycle(cycle, index);
                if (!seen.contains(key)) {
                    seen.add(key);
                    cycles.add(cycle);
                    if (cycles.size() >= maxCycles) return;
                }
            } else if (!path.contains(neigh)) {
                path.addLast(neigh);
                dfsUndirectedCycles(g, start, neigh, startIndex, path, cycles, index, seen, maxCycles);
                path.removeLast();
                if (cycles.size() >= maxCycles) return;
            }
        }
    }

    private String canonicalizeUndirectedCycle(List<String> cycle, Map<String, Integer> index) {
        // rotate cycle so smallest index vertex is first, then choose lexicographically smaller
        int n = cycle.size();
        int minPos = 0;
        int minIdx = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            int idx = index.getOrDefault(cycle.get(i), Integer.MAX_VALUE);
            if (idx < minIdx) { minIdx = idx; minPos = i; }
        }
        List<String> rotated = new ArrayList<>();
        for (int i = 0; i < n; i++) rotated.add(cycle.get((minPos + i) % n));

        List<String> rev = new ArrayList<>(rotated);
        Collections.reverse(rev);

        String a = String.join("|", rotated);
        String b = String.join("|", rev);
        return a.compareTo(b) <= 0 ? a : b;
    }

    // Remove cycles that are trivial (single-edge loops) and cycles whose edge-sets
    // are strictly contained in another cycle's edge-set. Keeps only maximal cycles.
    private List<List<String>> filterCyclesByEdgeSets(Graph g, List<List<String>> cycles) {
        if (cycles == null || cycles.isEmpty()) return cycles;

        List<Set<Edge>> edgeSets = new ArrayList<>();
        for (List<String> cycle : cycles) {
            Set<Edge> es = edgesFromVertexCycle(g, cycle);
            edgeSets.add(new HashSet<>(es));
        }

        int m = cycles.size();
        boolean[] remove = new boolean[m];

        // remove single-edge cycles (usually loops)
        for (int i = 0; i < m; i++) {
            if (edgeSets.get(i).size() <= 1) remove[i] = true;
        }

        // remove cycles whose edge-set is a strict subset of another cycle's edge-set
        for (int i = 0; i < m; i++) {
            if (remove[i]) continue;
            for (int j = 0; j < m; j++) {
                if (i == j) continue;
                if (remove[j]) continue;
                Set<Edge> a = edgeSets.get(i);
                Set<Edge> b = edgeSets.get(j);
                if (a.isEmpty() || b.isEmpty()) continue;
                if (b.containsAll(a) && b.size() > a.size()) {
                    // i is contained in j
                    remove[i] = true;
                    break;
                }
            }
        }

        List<List<String>> filtered = new ArrayList<>();
        for (int i = 0; i < m; i++) if (!remove[i]) filtered.add(cycles.get(i));
        return filtered;
    }

    // Directed simple cycles finder: basic per-start-index DFS to avoid duplicates
    private List<List<String>> findSimpleCyclesDirected(Graph g, int maxCycles) {
        List<List<String>> cycles = new ArrayList<>();
        List<String> verts = new ArrayList<>(g.vertices);
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < verts.size(); i++) index.put(verts.get(i), i);

        for (int s = 0; s < verts.size(); s++) {
            String start = verts.get(s);
            Deque<String> path = new ArrayDeque<>();
            path.addLast(start);
            dfsDirectedCycles(g, start, start, s, path, cycles, index, maxCycles);
            if (cycles.size() >= maxCycles) break;
        }
        return filterCyclesByEdgeSets(g, cycles);
    }

    private void dfsDirectedCycles(Graph g, String start, String current, int startIndex, Deque<String> path,
            List<List<String>> cycles, Map<String, Integer> index, int maxCycles) {
        if (cycles.size() >= maxCycles) return;
        for (String neigh : getNeighborsBothDirections(g, current)) {
            if (neigh.equals(start) && path.size() > 1) {
                List<String> cycle = new ArrayList<>(path);
                // ensure uniqueness: only accept cycles where the minimum index vertex is the start
                int minIdx = Integer.MAX_VALUE;
                for (String v : cycle) minIdx = Math.min(minIdx, index.getOrDefault(v, Integer.MAX_VALUE));
                if (minIdx == startIndex) {
                    cycles.add(cycle);
                    if (cycles.size() >= maxCycles) return;
                }
            } else if (!path.contains(neigh)) {
                path.addLast(neigh);
                dfsDirectedCycles(g, start, neigh, startIndex, path, cycles, index, maxCycles);
                path.removeLast();
                if (cycles.size() >= maxCycles) return;
            }
        }
    }

    // For cycle detection we must consider both outgoing and incoming edges so cycles
    // that traverse an edge against its direction are still detected (they'll be
    // recorded and later represented with -1 for that edge in the circuit matrix).
    private Set<String> getNeighborsBothDirections(Graph g, String vertex) {
        Set<String> neigh = new LinkedHashSet<>();
        for (Edge e : g.edges) {
            if (e.source.equals(vertex)) neigh.add(e.destination);
            if (e.destination.equals(vertex)) neigh.add(e.source);
        }
        return neigh;
    }

    private void findCyclesDFS(Graph g, String start, String current, Deque<String> path, List<List<String>> cycles,
            Set<String> visitedGlobal, int maxCycles) {
        if (cycles.size() >= maxCycles)
            return;
        for (String neigh : g.getAdjacentVertices(current)) {
            if (neigh.equals(start) && path.size() > 2) {
                List<String> cycle = new ArrayList<>(path);
                // normalize to avoid duplicates
                Collections.sort(cycle);
                boolean dup = false;
                for (List<String> c : cycles) {
                    List<String> copy = new ArrayList<>(c);
                    Collections.sort(copy);
                    if (copy.equals(cycle)) {
                        dup = true;
                        break;
                    }
                }
                if (!dup)
                    cycles.add(new ArrayList<>(path));
                if (cycles.size() >= maxCycles)
                    return;
            } else if (!path.contains(neigh) && !visitedGlobal.contains(neigh)) {
                path.push(neigh);
                findCyclesDFS(g, start, neigh, path, cycles, visitedGlobal, maxCycles);
                path.pop();
                if (cycles.size() >= maxCycles)
                    return;
            }
        }
    }

    // Helper: find bridges using DFS (Tarjan)
    @SuppressWarnings("unused")
    private List<Edge> findBridges(Graph g) {
        List<Edge> bridges = new ArrayList<>();
        Map<String, Integer> disc = new HashMap<>();
        Map<String, Integer> low = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        List<String> verts = new ArrayList<>(g.vertices);
        int time = 0;
        for (String v : verts) {
            if (!disc.containsKey(v)) {
                time = bridgeDFS(g, v, disc, low, parent, bridges, time);
            }
        }
        return bridges;
    }

    private int bridgeDFS(Graph g, String u, Map<String, Integer> disc, Map<String, Integer> low,
            Map<String, String> parent, List<Edge> bridges, int time) {
        disc.put(u, time);
        low.put(u, time);
        time++;
        for (String v : g.getAdjacentVertices(u)) {
            if (!disc.containsKey(v)) {
                parent.put(v, u);
                time = bridgeDFS(g, v, disc, low, parent, bridges, time);
                low.put(u, Math.min(low.get(u), low.get(v)));
                if (low.get(v) > disc.get(u)) {
                    // u-v is a bridge; find any matching edge
                    for (Edge e : g.edges) {
                        if ((e.source.equals(u) && e.destination.equals(v)) || (e.source.equals(v) && e.destination.equals(u))) {
                            bridges.add(e);
                            break;
                        }
                    }
                }
            } else if (!Objects.equals(parent.get(u), v)) {
                low.put(u, Math.min(low.get(u), disc.get(v)));
            }
        }
        return time;
    }

    // BFS/DFS component ignoring a specific edge
    @SuppressWarnings("unused")
    private Set<String> componentWithoutEdge(Graph g, String start, Edge forbidden) {
        Set<String> comp = new HashSet<>();
        Deque<String> dq = new ArrayDeque<>();
        dq.add(start);
        comp.add(start);
        while (!dq.isEmpty()) {
            String v = dq.poll();
            for (Edge e : g.edges) {
                if (e.equals(forbidden))
                    continue;
                String neigh = null;
                if (e.source.equals(v))
                    neigh = e.destination;
                else if (!g.isDirected && e.destination.equals(v))
                    neigh = e.source;
                if (neigh != null && !comp.contains(neigh)) {
                    comp.add(neigh);
                    dq.add(neigh);
                }
            }
        }
        return comp;
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
            // Use same canvas size as main graph for consistent vertex/edge sizing
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
            double radius = VERTEX_RADIUS; // always use main vertex radius for consistency
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
                        // detect presence of an opposite-direction edge in the full graph
                        boolean hasReverse = false;
                        for (Edge check : graph.edges) {
                            if (check.source.equals(firstEdge.destination) && check.destination.equals(firstEdge.source)) {
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

    @SuppressWarnings("unused")
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
                        // detect reverse edge presence in this subgraph
                        boolean hasReverse = false;
                        for (Edge check : subgraph.edges) {
                            if (check.source.equals(firstEdge.destination) && check.destination.equals(firstEdge.source)) {
                                hasReverse = true;
                                break;
                            }
                        }
                        drawEdge(canvas, sourcePos, destPos, edge.label,
                                subgraph.isDirected,
                                VERTEX_RADIUS,
                                i, parallelEdges.size(), allLabels, edgeLabelConnections, allEdgesInGroup, hasReverse);
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

        // Draw arrow at target if directed or if there is an opposite edge
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

        // If there is an opposite edge, draw an arrow at the source as well
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