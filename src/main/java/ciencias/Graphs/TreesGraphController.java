package ciencias.Graphs;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.geometry.Point2D;
import javafx.scene.input.ScrollEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class TreesGraphController {

    @FXML
    private ScrollPane graph1;
    @FXML
    private ScrollPane graph2;

    @FXML
    private MenuButton graphModify;
    @FXML
    private MenuButton graphModify1;
    @FXML
    private MenuButton graphModify2;
    @FXML
    private MenuButton SelectTree;

    @FXML
    private Spinner<Integer> vertNum;
    @FXML
    private Button vertAddButton;
    @FXML
    private Button vertDeleteButton;
    @FXML
    private TextField vertDelete;

    @FXML
    private TextField edgeOrg;
    @FXML
    private TextField edgeDest;
    @FXML
    private Button edgeAddButton;
    @FXML
    private Button edgeDeleteButton;
    @FXML
    private TextField edgeWeightNotation;

    @FXML
    private Text modificationText;
    @FXML
    private Text operationText;

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

    @FXML
    private ScrollPane medianaGraph;
    @FXML
    private ScrollPane centerGraph;

    @FXML
    private ScrollPane Tree1;
    @FXML
    private ScrollPane Complemento;

    @FXML
    private TextField excText;
    @FXML
    private TextField radiusText;
    @FXML
    private TextField longText;

    @FXML
    private Button distArboles;
    @FXML
    private TextField distText;
    @FXML
    private TextFlow distErrorFlow;

    private Graph graphData1;
    private Graph graphData2;

    private Stack<GraphState> history1;
    private Stack<GraphState> redo1;

    private Stack<GraphState> history2;
    private Stack<GraphState> redo2;

    private Map<String, Point2D> layout1;
    private Map<String, Point2D> layout2;

    private int selectedGraph = 0; // 0 = none, 1 = graph1, 2 = graph2
    private int metricsGraph = 0; // 0 = none, 1 = graph1, 2 = graph2 (para métricas)
    private int treeGraphSelected = 0; // which graph to take tree from
    private int treeTypeSelected = 0; // 1 = minimum, 2 = maximum

    private static final double VERTEX_RADIUS = 20;
    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 600;

    @FXML
    private void initialize() {
        graphData1 = new Graph();
        graphData2 = new Graph();

        history1 = new Stack<>();
        redo1 = new Stack<>();
        history2 = new Stack<>();
        redo2 = new Stack<>();

        layout1 = new HashMap<>();
        layout2 = new HashMap<>();

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                Integer.MAX_VALUE, 1);
        vertNum.setValueFactory(valueFactory);
        vertNum.setEditable(true);

        // setup MenuButton items to set selection and enable/disable controls
        for (MenuItem item : graphModify.getItems()) {
            item.setOnAction(e -> {
                graphModify.setText(item.getText());
                if ("Grafo 1".equals(item.getText())) {
                    selectedGraph = 1;
                } else if ("Grafo 2".equals(item.getText())) {
                    selectedGraph = 2;
                } else {
                    selectedGraph = 0;
                }
                updateControlsEnabled();
            });
        }

        // setup MenuButton1 items for metrics display
        for (MenuItem item : graphModify1.getItems()) {
            item.setOnAction(e -> {
                graphModify1.setText(item.getText());
                if ("Grafo 1".equals(item.getText())) {
                    metricsGraph = 1;
                    updateMetrics(graphData1);
                } else if ("Grafo 2".equals(item.getText())) {
                    metricsGraph = 2;
                    updateMetrics(graphData2);
                }
            });
        }

        // setup MenuButton2 items for selecting which graph to generate tree from
        if (graphModify2 != null) {
            for (MenuItem item : graphModify2.getItems()) {
                item.setOnAction(e -> {
                    graphModify2.setText(item.getText());
                    if ("Grafo 1".equals(item.getText())) treeGraphSelected = 1;
                    else if ("Grafo 2".equals(item.getText())) treeGraphSelected = 2;
                    else treeGraphSelected = 0;
                    updateTreeDisplayIfNeeded();
                });
            }
        }

        // setup SelectTree items for choosing minimum or maximum spanning tree
        if (SelectTree != null) {
            for (MenuItem item : SelectTree.getItems()) {
                item.setOnAction(e -> {
                    SelectTree.setText(item.getText());
                    String lower = item.getText().toLowerCase();
                    if (lower.contains("min") || lower.contains("mín")) treeTypeSelected = 1;
                    else if (lower.contains("max") || lower.contains("máx")) treeTypeSelected = 2;
                    else treeTypeSelected = 0;
                    updateTreeDisplayIfNeeded();
                });
            }
        }

        // start with controls disabled until a graph is selected
        updateControlsEnabled();

        setupZoomAndScroll(graph1);
        setupZoomAndScroll(graph2);
        setupZoomAndScroll(medianaGraph);
        setupZoomAndScroll(centerGraph);
        setupZoomAndScroll(Tree1);
        setupZoomAndScroll(Complemento);

        if (distArboles != null) {
            distArboles.setOnAction(e -> calculateTreeDistance());
        }

        if (distErrorFlow != null) {
            distErrorFlow.setVisible(false);
        }

        updateAllDisplays();
    }

    private void updateControlsEnabled() {
        boolean enabled = selectedGraph == 1 || selectedGraph == 2;
        vertNum.setDisable(!enabled);
        vertAddButton.setDisable(!enabled);
        vertDeleteButton.setDisable(!enabled);
        vertDelete.setDisable(!enabled);
        edgeOrg.setDisable(!enabled);
        edgeDest.setDisable(!enabled);
        edgeAddButton.setDisable(!enabled);
        edgeDeleteButton.setDisable(!enabled);
        edgeWeightNotation.setDisable(!enabled);
        undoButton.setDisable(!enabled);
        redoButton.setDisable(!enabled);
        restartButton.setDisable(!enabled);
        saveButton.setDisable(!enabled);
        loadButton.setDisable(!enabled);
    }

    private void setupZoomAndScroll(ScrollPane scrollPane) {
        if (scrollPane == null)
            return;
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();
                double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
                if (scrollPane.getContent() != null) {
                    double h = scrollPane.getHvalue();
                    double v = scrollPane.getVvalue();
                    scrollPane.getContent().setScaleX(scrollPane.getContent().getScaleX() * zoomFactor);
                    scrollPane.getContent().setScaleY(scrollPane.getContent().getScaleY() * zoomFactor);
                    scrollPane.setHvalue(h);
                    scrollPane.setVvalue(v);
                }
            }
        });
        scrollPane.setPannable(true);
    }

    private Graph getActiveGraph() {
        return selectedGraph == 1 ? graphData1 : graphData2;
    }

    private Stack<GraphState> getActiveHistory() {
        return selectedGraph == 1 ? history1 : history2;
    }

    private Stack<GraphState> getActiveRedo() {
        return selectedGraph == 1 ? redo1 : redo2;
    }

    private Map<String, Point2D> getActiveLayout() {
        return selectedGraph == 1 ? layout1 : layout2;
    }

    private ScrollPane getActiveScrollPane() {
        return selectedGraph == 1 ? graph1 : graph2;
    }

    private void saveStateActive() {
        getActiveHistory().push(getActiveGraph().getState());
        getActiveRedo().clear();
    }

    @FXML
    private void vertAdd() {
        if (selectedGraph == 0) {
            modificationText.setText("Elija un grafo en 'Modificar' antes de operar");
            return;
        }
        saveStateActive();
        int num = vertNum.getValue();
        List<String> added = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            String v = getActiveGraph().generateNextVertexName();
            getActiveGraph().addVertex(v);
            added.add(v);
        }
        updateDisplayForActive();
        modificationText.setText("Añadidos: " + String.join(", ", added));
    }

    @FXML
    private void vertDelete() {
        if (selectedGraph == 0) {
            modificationText.setText("Elija un grafo en 'Modificar' antes de operar");
            return;
        }
        String v = vertDelete.getText().trim().toUpperCase();
        if (v.isEmpty()) {
            modificationText.setText("Ingrese un vertice");
            return;
        }
        if (!getActiveGraph().hasVertex(v)) {
            modificationText.setText("Vertice no existe: " + v);
            return;
        }
        saveStateActive();
        getActiveGraph().removeVertex(v);
        vertDelete.clear();
        updateDisplayForActive();
        modificationText.setText("Vertice eliminado: " + v);
    }

    @FXML
    private void edgeAdd() {
        if (selectedGraph == 0) {
            modificationText.setText("Elija un grafo en 'Modificar' antes de operar");
            return;
        }
        String s = edgeOrg.getText().trim().toUpperCase();
        String d = edgeDest.getText().trim().toUpperCase();
        String w = edgeWeightNotation.getText().trim();
        if (s.isEmpty()) {
            modificationText.setText("Ingrese vertice origen");
            return;
        }
        if (d.isEmpty()) d = s;
        if (!getActiveGraph().hasVertex(s) || !getActiveGraph().hasVertex(d)) {
            modificationText.setText("Los vertices deben existir");
            return;
        }
        String label = w.isEmpty() ? "1" : w;
        try {
            double weight = Double.parseDouble(label);
            if (weight <= 0) {
                modificationText.setText("Ponderacion debe ser positiva (> 0)");
                return;
            }
        } catch (NumberFormatException e) {
            modificationText.setText("Ponderacion debe ser un numero");
            return;
        }
        try {
            saveStateActive();
            getActiveGraph().addEdge(s, d, label);
            edgeOrg.clear();
            edgeDest.clear();
            edgeWeightNotation.clear();
            updateDisplayForActive();
            modificationText.setText("Arista agregada: " + s + " - " + d + " (peso: " + label + ")");
        } catch (IllegalStateException ex) {
            modificationText.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void edgeDelete() {
        if (selectedGraph == 0) {
            modificationText.setText("Elija un grafo en 'Modificar' antes de operar");
            return;
        }
        String s = edgeOrg.getText().trim().toUpperCase();
        String d = edgeDest.getText().trim().toUpperCase();
        String w = edgeWeightNotation.getText().trim();
        if (s.isEmpty()) {
            modificationText.setText("Ingrese vertice origen");
            return;
        }
        if (d.isEmpty()) d = s;
        String label = w.isEmpty() ? "1" : w;
        saveStateActive();
        getActiveGraph().removeEdge(s, d, label);
        edgeOrg.clear();
        edgeDest.clear();
        edgeWeightNotation.clear();
        updateDisplayForActive();
        modificationText.setText("Arista eliminada: " + s + " - " + d);
    }

    @FXML
    private void undo() {
        if (selectedGraph == 0) return;
        Stack<GraphState> hist = getActiveHistory();
        Stack<GraphState> rd = getActiveRedo();
        if (hist.isEmpty()) {
            modificationText.setText("No hay acciones para deshacer");
            return;
        }
        rd.push(getActiveGraph().getState());
        GraphState prev = hist.pop();
        getActiveGraph().setState(prev);
        updateDisplayForActive();
        modificationText.setText("Accion deshecha");
    }

    @FXML
    private void redo() {
        if (selectedGraph == 0) return;
        Stack<GraphState> hist = getActiveHistory();
        Stack<GraphState> rd = getActiveRedo();
        if (rd.isEmpty()) {
            modificationText.setText("No hay acciones para rehacer");
            return;
        }
        hist.push(getActiveGraph().getState());
        GraphState next = rd.pop();
        getActiveGraph().setState(next);
        updateDisplayForActive();
        modificationText.setText("Accion rehecha");
    }

    @FXML
    private void restart() {
        if (selectedGraph == 0) return;
        saveStateActive();
        if (selectedGraph == 1) {
            graphData1 = new Graph();
            layout1.clear();
            history1.clear();
            redo1.clear();
        } else {
            graphData2 = new Graph();
            layout2.clear();
            history2.clear();
            redo2.clear();
        }
        updateDisplayForActive();
        modificationText.setText("Grafo reiniciado");
    }

    @FXML
    private void save() {
        if (selectedGraph == 0) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar grafo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fc.showSaveDialog(stage);
        if (file == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            // Convert internal GraphState to SharedGraphData for cross-tab compatibility
            Graph activeGraph = getActiveGraph();
            SharedGraphData sharedData = convertToSharedGraphData(activeGraph);
            oos.writeObject(sharedData);
            modificationText.setText("Grafo guardado: " + file.getName());
        } catch (Exception e) {
            modificationText.setText("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void load() {
        if (selectedGraph == 0) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Cargar grafo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        Stage stage = (Stage) loadButton.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file == null) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object loadedObject = ois.readObject();
            Graph activeGraph = getActiveGraph();
            
            if (loadedObject instanceof SharedGraphData) {
                // Load from shared format (compatible with other tabs)
                SharedGraphData sharedData = (SharedGraphData) loadedObject;
                convertFromSharedGraphData(activeGraph, sharedData);
            } else if (loadedObject instanceof GraphState) {
                // Fallback: load from legacy format
                GraphState st = (GraphState) loadedObject;
                activeGraph.setState(st);
            }
            
            getActiveLayout().clear();
            getActiveHistory().clear();
            getActiveRedo().clear();
            updateDisplayForActive();
            modificationText.setText("Grafo cargado: " + file.getName());
        } catch (Exception e) {
            modificationText.setText("Error al cargar: " + e.getMessage());
        }
    }

    private SharedGraphData convertToSharedGraphData(Graph graph) {
        SharedGraphData sharedData = new SharedGraphData();
        sharedData.vertices = new LinkedHashSet<>(graph.vertices);
        sharedData.isDirected = graph.isDirected;
        sharedData.isWeighted = true; // TreesGraphController always uses weighted graphs
        
        for (Graph.Edge e : graph.edges) {
            SharedGraphData.SharedEdge sharedEdge = new SharedGraphData.SharedEdge(
                    e.source,
                    e.destination,
                    e.label,
                    false // TreesGraphController doesn't use sum edges
            );
            sharedData.edges.add(sharedEdge);
        }
        
        return sharedData;
    }

    private void convertFromSharedGraphData(Graph graph, SharedGraphData sharedData) {
        graph.vertices.clear();
        graph.edges.clear();
        graph.isDirected = sharedData.isDirected;
        
        graph.vertices.addAll(sharedData.vertices);
        
        for (SharedGraphData.SharedEdge sharedEdge : sharedData.edges) {
            try {
                graph.addEdge(sharedEdge.source, sharedEdge.destination, sharedEdge.label);
            } catch (IllegalStateException ex) {
                // Edge might already exist, skip
            }
        }
    }

    private void updateAllDisplays() {
        drawGraph(graphData1, graph1, layout1);
        drawGraph(graphData2, graph2, layout2);
    }

    private void updateDisplayForActive() {
        if (selectedGraph == 1) drawGraph(graphData1, graph1, layout1);
        else drawGraph(graphData2, graph2, layout2);
        updateMetricsIfNeeded();
        updateTreeDisplayIfNeeded();
    }

    private void updateMetricsIfNeeded() {
        if (metricsGraph == 1) {
            updateMetrics(graphData1);
        } else if (metricsGraph == 2) {
            updateMetrics(graphData2);
        }
    }

    private void drawGraph(Graph graph, ScrollPane scrollPane, Map<String, Point2D> customLayout) {
        Pane canvas = new Pane();
        canvas.setPrefSize(Math.max(CANVAS_WIDTH, 600), Math.max(CANVAS_HEIGHT, 400));

        if (graph == null || graph.isEmpty()) {
            scrollPane.setContent(canvas);
            return;
        }

        if (customLayout == null || customLayout.isEmpty() || !customLayout.keySet().containsAll(graph.vertices)) {
            Map<String, Point2D> layout = calculateGraphLayout(graph, canvas.getPrefWidth() / 2,
                    canvas.getPrefHeight() / 2, VERTEX_RADIUS);
            if (customLayout != null) {
                customLayout.clear();
                customLayout.putAll(layout);
            }
        }

        for (Graph.Edge e : graph.edges) {
            if (e.isLoop) {
                Point2D p = customLayout.get(e.source);
                if (p != null) drawLoop(canvas, p.getX(), p.getY(), VERTEX_RADIUS);
            } else {
                Point2D s = customLayout.get(e.source);
                Point2D t = customLayout.get(e.destination);
                if (s != null && t != null) drawEdgeWithLabel(canvas, s.getX(), s.getY(), t.getX(), t.getY(), e.label);
            }
        }

        for (String v : graph.vertices) {
            Point2D p = customLayout.get(v);
            if (p != null) drawVertex(canvas, p.getX(), p.getY(), VERTEX_RADIUS, v);
        }

        scrollPane.setContent(canvas);
    }

    private void drawVertex(Pane canvas, double x, double y, double radius, String label) {
        Circle c = new Circle(x, y, radius);
        c.setFill(Color.WHITE);
        c.setStroke(Color.BLACK);
        canvas.getChildren().add(c);
        Text t = new Text(label);
        t.setX(x - t.getLayoutBounds().getWidth() / 2);
        t.setY(y + 4);
        canvas.getChildren().add(t);
    }

    private void drawEdge(Pane canvas, double sx, double sy, double tx, double ty) {
        drawEdgeWithLabel(canvas, sx, sy, tx, ty, null);
    }

    private void drawEdgeWithLabel(Pane canvas, double sx, double sy, double tx, double ty, String label) {
        Line l = new Line(sx, sy, tx, ty);
        l.setStroke(Color.BLACK);
        l.setStrokeWidth(2);
        canvas.getChildren().add(l);
        if (label != null && !label.isEmpty()) {
            double midX = (sx + tx) / 2;
            double midY = (sy + ty) / 2;
            Text weightText = new Text(midX, midY - 10, label);
            weightText.setStyle("-fx-font-weight: bold; -fx-font-size: 10;");
            weightText.setFill(Color.BLUE);
            canvas.getChildren().add(weightText);
        }
    }

    private void drawLoop(Pane canvas, double x, double y, double radius) {
        double startX = x - radius;
        double startY = y - radius / 2;
        double endX = x - radius + 1;
        double endY = y + radius / 2;
        javafx.scene.shape.CubicCurve curve = new javafx.scene.shape.CubicCurve(startX, startY, x - radius - 30,
                startY - 30, x - radius - 30, endY + 30, endX, endY);
        curve.setStroke(Color.BLACK);
        curve.setFill(Color.TRANSPARENT);
        curve.setStrokeWidth(2);
        canvas.getChildren().add(curve);
    }

    private Map<String, Point2D> calculateGraphLayout(Graph graph, double centerX, double centerY, double radius) {
        Map<String, Point2D> positions = new HashMap<>();
        List<String> vertices = new ArrayList<>(graph.vertices);
        if (vertices.isEmpty()) return positions;
        int n = vertices.size();
        double layoutRadius = Math.min(250, Math.max(80, n * 25));
        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + layoutRadius * Math.cos(angle);
            double y = centerY + layoutRadius * Math.sin(angle);
            positions.put(vertices.get(i), new Point2D(x, y));
        }
        return positions;
    }

    // --- Inner data classes and graph operations ---
    private static class GraphState implements Serializable {
        private static final long serialVersionUID = 1L;
        Set<String> vertices;
        List<Graph.Edge> edges;
        boolean isDirected;
    }

    private static class Graph implements Serializable {
        private static final long serialVersionUID = 1L;
        Set<String> vertices = new LinkedHashSet<>();
        List<Edge> edges = new ArrayList<>();
        boolean isDirected = false;

        GraphState getState() {
            GraphState s = new GraphState();
            s.vertices = new LinkedHashSet<>(vertices);
            s.edges = new ArrayList<>(edges);
            s.isDirected = false;
            return s;
        }

        void setState(GraphState s) {
            if (s == null) return;
            vertices = new LinkedHashSet<>(s.vertices != null ? s.vertices : Collections.emptySet());
            edges = new ArrayList<>(s.edges != null ? s.edges : Collections.emptyList());
            isDirected = false;
        }

        void addVertex(String v) {
            vertices.add(v);
        }

        void removeVertex(String v) {
            vertices.remove(v);
            edges.removeIf(e -> e.source.equals(v) || e.destination.equals(v));
        }

        boolean hasVertex(String v) {
            return vertices.contains(v);
        }

        void addEdge(String s, String d, String label) {
            isDirected = false;
            if (s.equals(d)) {
                boolean exists = edges.stream().anyMatch(e -> e.isLoop && e.source.equals(s));
                if (exists) throw new IllegalStateException("Ya existe bucle en " + s);
                edges.add(new Edge(s, d, label));
                return;
            }
            String a = s.compareTo(d) <= 0 ? s : d;
            String b = s.compareTo(d) <= 0 ? d : s;
            boolean exists = edges.stream().anyMatch(e -> !e.isLoop && e.source.equals(a) && e.destination.equals(b));
            if (exists) throw new IllegalStateException("Ya existe arista entre " + s + " y " + d);
            edges.add(new Edge(a, b, label));
        }

        void removeEdge(String s, String d, String label) {
            if (!isDirected) {
                edges.removeIf(e -> ((e.source.equals(s) && e.destination.equals(d)) || (e.source.equals(d) && e.destination.equals(s))) && e.label.equals(label));
            } else {
                edges.removeIf(e -> e.source.equals(s) && e.destination.equals(d) && e.label.equals(label));
            }
        }

        boolean isEmpty() {
            return vertices.isEmpty();
        }

        private String generateNextVertexName() {
            if (vertices.isEmpty()) return "A";
            List<String> sorted = new ArrayList<>(vertices);
            sorted.sort((a, b) -> {
                if (a.length() != b.length()) return a.length() - b.length();
                return a.compareTo(b);
            });
            String last = sorted.get(sorted.size() - 1);
            if (last.length() == 1) {
                char c = last.charAt(0);
                if (c < 'Z') return String.valueOf((char)(c + 1));
                return "AA";
            } else {
                char[] cs = last.toCharArray();
                for (int i = cs.length - 1; i >= 0; i--) {
                    if (cs[i] < 'Z') { cs[i]++; return new String(cs); }
                    cs[i] = 'A';
                }
                return last + "A";
            }
        }

        private static class Edge implements Serializable {
            private static final long serialVersionUID = 1L;
            String source;
            String destination;
            String label;
            boolean isLoop;
            Edge(String s, String d, String l) { source = s; destination = d; label = l; isLoop = s.equals(d); }
        }
    }

    private void updateMetrics(Graph graph) {
        if (graph == null || graph.isEmpty()) {
            excText.setText("");
            radiusText.setText("");
            longText.setText("");
            medianaGraph.setContent(new Pane());
            centerGraph.setContent(new Pane());
            return;
        }

        double[][] distances = computeFloydWarshall(graph);

        Map<String, Double> eccentricities = new HashMap<>();
        List<String> vertices = new ArrayList<>(graph.vertices);

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
            if (ecc < radius) radius = ecc;
            if (ecc > diameter) diameter = ecc;
        }

        excText.setText(eccentricities.toString());
        radiusText.setText((radius == Double.POSITIVE_INFINITY ? "∞" : String.valueOf((int) radius)));
        longText.setText((diameter == Double.POSITIVE_INFINITY ? "∞" : String.valueOf((int) diameter)));

        updateMedianaAndCenter(graph, eccentricities, radius, distances);
    }

    private double[][] computeFloydWarshall(Graph graph) {
        List<String> vertices = new ArrayList<>(graph.vertices);
        int n = vertices.size();
        double[][] dist = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = (i == j) ? 0 : Double.POSITIVE_INFINITY;
            }
        }

        Map<String, Integer> vertexIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            vertexIndex.put(vertices.get(i), i);
        }

        for (Graph.Edge edge : graph.edges) {
            int u = vertexIndex.get(edge.source);
            int v = vertexIndex.get(edge.destination);
            double weight = getWeight(edge);
            dist[u][v] = Math.min(dist[u][v], weight);
            if (!graph.isDirected) {
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

    private double getWeight(Graph.Edge edge) {
        try {
            return Double.parseDouble(edge.label);
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }

    private void updateMedianaAndCenter(Graph graph, Map<String, Double> eccentricities, double radius, double[][] distances) {
        List<String> vertices = new ArrayList<>(graph.vertices);

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

        Graph centerSubgraph = createInducedSubgraph(graph, centerVertices);
        Graph medianaSubgraph = createInducedSubgraph(graph, medianaVertices);

        drawGraph(centerSubgraph, centerGraph, new HashMap<>());
        drawGraph(medianaSubgraph, medianaGraph, new HashMap<>());
    }

    private Graph createInducedSubgraph(Graph graph, Set<String> vertices) {
        Graph subgraph = new Graph();
        subgraph.vertices.addAll(vertices);
        subgraph.isDirected = false;

        for (Graph.Edge edge : graph.edges) {
            if (vertices.contains(edge.source) && vertices.contains(edge.destination)) {
                subgraph.edges.add(new Graph.Edge(edge.source, edge.destination, edge.label));
            }
        }

        return subgraph;
    }

    // --- Spanning tree utilities ---
    private void updateTreeDisplayIfNeeded() {
        if (treeGraphSelected <= 0 || treeTypeSelected <= 0) {
            if (Tree1 != null) Tree1.setContent(new Pane());
            if (Complemento != null) Complemento.setContent(new Pane());
            return;
        }
        Graph src = treeGraphSelected == 1 ? graphData1 : graphData2;
        if (src == null || src.isEmpty()) {
            modificationText.setText("Grafo seleccionado vacio");
            if (Tree1 != null) Tree1.setContent(new Pane());
            if (Complemento != null) Complemento.setContent(new Pane());
            return;
        }
        if (!isUndirected(src)) {
            modificationText.setText("El grafo debe ser no dirigido para construir arboles de expansion");
            if (Tree1 != null) Tree1.setContent(new Pane());
            if (Complemento != null) Complemento.setContent(new Pane());
            return;
        }
        if (!isWeighted(src)) {
            modificationText.setText("El grafo debe ser ponderado (todas las aristas con peso numerico)");
            if (Tree1 != null) Tree1.setContent(new Pane());
            if (Complemento != null) Complemento.setContent(new Pane());
            return;
        }
        if (!isConnected(src)) {
            modificationText.setText("El grafo debe ser conexo para construir un arbol de expansion");
            if (Tree1 != null) Tree1.setContent(new Pane());
            if (Complemento != null) Complemento.setContent(new Pane());
            return;
        }

        boolean minimum = treeTypeSelected == 1;
        Graph tree = computeSpanningTree(src, minimum);
        Graph complement = computeSpanningTreeComplement(src, tree);
        
        if (Tree1 != null) drawGraph(tree, Tree1, new HashMap<>());
        if (Complemento != null) drawGraph(complement, Complemento, new HashMap<>());
        modificationText.setText((minimum ? "Arbol de expansion minima" : "Arbol de expansion maxima") + " y complemento generados para Grafo " + treeGraphSelected);
    }

    private boolean isUndirected(Graph g) {
        return g != null && !g.isDirected;
    }

    private boolean isWeighted(Graph g) {
        if (g == null) return false;
        for (Graph.Edge e : g.edges) {
            try {
                Double.parseDouble(e.label);
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    private boolean isConnected(Graph g) {
        if (g == null) return false;
        if (g.vertices.isEmpty()) return false;
        Iterator<String> it = g.vertices.iterator();
        if (!it.hasNext()) return false;
        String start = it.next();
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(start);
        visited.add(start);
        while (!stack.isEmpty()) {
            String v = stack.pop();
            for (Graph.Edge e : g.edges) {
                if (e.isLoop) continue;
                if (e.source.equals(v) && !visited.contains(e.destination)) {
                    visited.add(e.destination);
                    stack.push(e.destination);
                } else if (e.destination.equals(v) && !visited.contains(e.source)) {
                    visited.add(e.source);
                    stack.push(e.source);
                }
            }
        }
        return visited.size() == g.vertices.size();
    }

    private Graph computeSpanningTree(Graph g, boolean minimum) {
        Graph tree = new Graph();
        tree.vertices.addAll(g.vertices);
        // prepare edge list
        class EInfo { String a,b; double w; String label; }
        List<EInfo> list = new ArrayList<>();
        for (Graph.Edge e : g.edges) {
            if (e.isLoop) continue; // skip loops in spanning tree
            EInfo ei = new EInfo();
            ei.a = e.source;
            ei.b = e.destination;
            ei.label = e.label;
            ei.w = getWeight(e);
            list.add(ei);
        }
        // sort
        list.sort((x,y) -> {
            int cmp = Double.compare(x.w, y.w);
            return minimum ? cmp : -cmp;
        });

        // DSU map
        Map<String, String> parent = new HashMap<>();
        for (String v : g.vertices) parent.put(v, v);
        Function<String, String> find = new Function<String, String>() {
            public String apply(String x) {
                String p = parent.get(x);
                if (p.equals(x)) return x;
                String r = apply(p);
                parent.put(x, r);
                return r;
            }
        };
        BiConsumer<String, String> unite = (x,y) -> {
            String rx = find.apply(x);
            String ry = find.apply(y);
            if (!rx.equals(ry)) parent.put(rx, ry);
        };

        for (EInfo ei : list) {
            String ra = find.apply(ei.a);
            String rb = find.apply(ei.b);
            if (!ra.equals(rb)) {
                tree.edges.add(new Graph.Edge(ei.a, ei.b, ei.label));
                unite.accept(ei.a, ei.b);
            }
            if (tree.edges.size() == Math.max(0, tree.vertices.size() - 1)) break;
        }

        return tree;
    }

    private Graph computeSpanningTreeComplement(Graph original, Graph spanningTree) {
        Graph complement = new Graph();
        complement.vertices.addAll(original.vertices);
        complement.isDirected = false;

        // Create a set of edges in the spanning tree for fast lookup
        Set<String> treeEdgesSet = new HashSet<>();
        for (Graph.Edge e : spanningTree.edges) {
            String edgeKey = getEdgeKey(e.source, e.destination);
            treeEdgesSet.add(edgeKey);
        }

        // Add all edges from original that are NOT in the spanning tree
        for (Graph.Edge e : original.edges) {
            if (e.isLoop) continue; // skip loops
            String edgeKey = getEdgeKey(e.source, e.destination);
            if (!treeEdgesSet.contains(edgeKey)) {
                complement.edges.add(new Graph.Edge(e.source, e.destination, e.label));
            }
        }

        return complement;
    }

    private String getEdgeKey(String a, String b) {
        // Create a canonical key for undirected edges
        String first = a.compareTo(b) <= 0 ? a : b;
        String second = a.compareTo(b) <= 0 ? b : a;
        return first + "-" + second;
    }

    @FXML
    private void calculateTreeDistance() {
        if (distErrorFlow != null) {
            distErrorFlow.getChildren().clear();
            distErrorFlow.setVisible(false);
        }

        // Get minimum spanning trees for both graphs
        Graph mst1 = null;
        Graph mst2 = null;
        String error = null;

        // Check and generate MST for graph 1
        if (graphData1 == null || graphData1.isEmpty()) {
            error = "El Grafo 1 está vacío. No se puede calcular el árbol de expansión.";
        } else if (!isUndirected(graphData1)) {
            error = "El Grafo 1 debe ser no dirigido para calcular el árbol de expansión.";
        } else if (!isWeighted(graphData1)) {
            error = "El Grafo 1 debe ser ponderado (todas las aristas con peso numérico).";
        } else if (!isConnected(graphData1)) {
            error = "El Grafo 1 debe ser conexo para calcular el árbol de expansión.";
        } else {
            mst1 = computeSpanningTree(graphData1, true); // true = minimum
        }

        // Check and generate MST for graph 2 if no error yet
        if (error == null) {
            if (graphData2 == null || graphData2.isEmpty()) {
                error = "El Grafo 2 está vacío. No se puede calcular el árbol de expansión.";
            } else if (!isUndirected(graphData2)) {
                error = "El Grafo 2 debe ser no dirigido para calcular el árbol de expansión.";
            } else if (!isWeighted(graphData2)) {
                error = "El Grafo 2 debe ser ponderado (todas las aristas con peso numérico).";
            } else if (!isConnected(graphData2)) {
                error = "El Grafo 2 debe ser conexo para calcular el árbol de expansión.";
            } else {
                mst2 = computeSpanningTree(graphData2, true); // true = minimum
            }
        }

        // If there's an error, display it
        if (error != null) {
            if (distErrorFlow != null) {
                javafx.scene.text.Text errorText = new javafx.scene.text.Text(error);
                errorText.setStyle("-fx-font-size: 12; -fx-fill: #d32f2f;");
                distErrorFlow.getChildren().add(errorText);
                distErrorFlow.setVisible(true);
            }
            if (distText != null) {
                distText.setText("");
            }
            return;
        }

        // Calculate distance between the two MSTs
        double distance = calculateSpanningTreeDistance(mst1, mst2);
        if (distText != null) {
            distText.setText(String.format("%.2f", distance));
        }
        modificationText.setText("Distancia entre árboles de expansión mínima calculada: " + String.format("%.2f", distance));
    }

    private double calculateSpanningTreeDistance(Graph mst1, Graph mst2) {
        // Formula: ((A1 union A2) - (A1 interseccion A2)) / 2
        // Where A1 and A2 are the sets of weighted edges

        // Create sets of edges with weights for both trees
        Set<String> edgeSet1 = new HashSet<>();
        Set<String> edgeSet2 = new HashSet<>();
        Map<String, Double> edgeWeights1 = new HashMap<>();
        Map<String, Double> edgeWeights2 = new HashMap<>();

        // Add edges from MST1 with their canonical keys and weights
        for (Graph.Edge e : mst1.edges) {
            String edgeKey = getEdgeKey(e.source, e.destination);
            edgeSet1.add(edgeKey);
            double weight = getWeight(e);
            edgeWeights1.put(edgeKey, weight);
        }

        // Add edges from MST2 with their canonical keys and weights
        for (Graph.Edge e : mst2.edges) {
            String edgeKey = getEdgeKey(e.source, e.destination);
            edgeSet2.add(edgeKey);
            double weight = getWeight(e);
            edgeWeights2.put(edgeKey, weight);
        }

        // Calculate A1 union A2
        Set<String> unionSet = new HashSet<>(edgeSet1);
        unionSet.addAll(edgeSet2);

        // Calculate A1 intersection A2
        Set<String> intersectionSet = new HashSet<>(edgeSet1);
        intersectionSet.retainAll(edgeSet2);

        // Calculate (A1 union A2) - (A1 intersection A2)
        Set<String> symmetricDifference = new HashSet<>(unionSet);
        symmetricDifference.removeAll(intersectionSet);

        // Sum the weights of edges in the symmetric difference
        double sumWeights = 0;
        for (String edgeKey : symmetricDifference) {
            double weight = edgeWeights1.containsKey(edgeKey) ? edgeWeights1.get(edgeKey) : edgeWeights2.get(edgeKey);
            sumWeights += weight;
        }

        // Divide by 2
        return sumWeights / 2.0;
    }
}
