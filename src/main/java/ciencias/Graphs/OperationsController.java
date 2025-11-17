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
import javafx.scene.shape.StrokeType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import java.util.*;
import java.io.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
    private TextField edgeWeight;
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

    private class GraphState implements Serializable {
        Set<String> vertices;
        List<Edge> edges;
        boolean isDirected;

        public GraphState(Set<String> vertices, List<Edge> edges, boolean isDirected) {
            this.vertices = new HashSet<>(vertices);
            this.edges = new ArrayList<>(edges);
            this.isDirected = isDirected;
        }
    }

    private class Edge implements Serializable {
        String source;
        String destination;
        double weight;
        boolean isSumEdge;

        public Edge(String source, String destination, double weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
            this.isSumEdge = false;
        }

        public Edge(String source, String destination, double weight, boolean isSumEdge) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
            this.isSumEdge = isSumEdge;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            Edge edge = (Edge) obj;
            return source.equals(edge.source) && destination.equals(edge.destination) &&
                    weight == edge.weight && isSumEdge == edge.isSumEdge;
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, destination, weight, isSumEdge);
        }
    }

    private class Graph {
        private Set<String> vertices;
        private List<Edge> edges;
        private boolean isDirected;
        private boolean hasEdges;

        public Graph() {
            this.vertices = new HashSet<>();
            this.edges = new ArrayList<>();
            this.isDirected = false;
            this.hasEdges = false;
        }

        public Graph(Set<String> vertices, List<Edge> edges, boolean isDirected) {
            this.vertices = new HashSet<>(vertices);
            this.edges = new ArrayList<>(edges);
            this.isDirected = isDirected;
            this.hasEdges = !edges.isEmpty();
        }

        public Graph copy() {
            return new Graph(this.vertices, this.edges, this.isDirected);
        }

        public GraphState getState() {
            return new GraphState(vertices, edges, isDirected);
        }

        public void setState(GraphState state) {
            this.vertices = new HashSet<>(state.vertices);
            this.edges = new ArrayList<>(state.edges);
            this.isDirected = state.isDirected;
            this.hasEdges = !state.edges.isEmpty();
        }

        public void addVertex(String vertex) {
            vertices.add(vertex);
        }

        public void removeVertex(String vertex) {
            vertices.remove(vertex);
            edges.removeIf(edge -> edge.source.equals(vertex) || edge.destination.equals(vertex));
        }

        public void addEdge(String source, String destination, double weight) {
            addEdge(source, destination, weight, false);
        }

        public void addEdge(String source, String destination, double weight, boolean isSumEdge) {

            if (hasEdges) {

                Edge newEdge = new Edge(source, destination, weight, isSumEdge);
                if (isDirected != edgeDirection.isSelected()) {
                    throw new IllegalStateException(
                            "No se pueden mezclar aristas dirigidas y no dirigidas en el mismo grafo");
                }
            } else {

                isDirected = edgeDirection.isSelected();
                hasEdges = true;
            }

            edges.add(new Edge(source, destination, weight, isSumEdge));
        }

        public void removeEdge(String source, String destination) {
            removeEdge(source, destination, 1.0, false);
        }

        public void removeEdge(String source, String destination, double weight, boolean isSumEdge) {

            if (!isDirected) {
                for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
                    Edge edge = it.next();
                    if ((edge.source.equals(source) && edge.destination.equals(destination) ||
                            edge.source.equals(destination) && edge.destination.equals(source)) &&
                            edge.weight == weight && edge.isSumEdge == isSumEdge) {
                        it.remove();
                        break;
                    }
                }
            } else {

                for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
                    Edge edge = it.next();
                    if (edge.source.equals(source) && edge.destination.equals(destination) &&
                            edge.weight == weight && edge.isSumEdge == isSumEdge) {
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

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        vertNum.setValueFactory(valueFactory);
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
        edgeWeight.setDisable(!enabled);
        undoButton.setDisable(!enabled);
        redoButton.setDisable(!enabled);
        restartButton.setDisable(!enabled);
        saveButton.setDisable(!enabled);
    }

    private void setOperationControlsEnabled(boolean enabled) {
        graphOperate.setDisable(!enabled);
        operations.setDisable(!enabled);
        operateButton.setDisable(!enabled);
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
                setOperationControlsEnabled(true);
                updateOperationsMenu();
                operationText.setText("Operando sobre: " + currentOperatingGraph);

                fusionContractionText.setVisible(false);
                fusionContractionItem1.setVisible(false);
                fusionContractionItem2.setVisible(false);
                fusionContractionItem1.clear();
                fusionContractionItem2.clear();
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
        String weightText = edgeWeight.getText().trim();
        Graph currentGraph = getCurrentModifyingGraph();

        if (currentGraph == null) {
            modificationText.setText("Error: Seleccione un grafo para modificar");
            return;
        }

        if (source.isEmpty() || destination.isEmpty()) {
            modificationText.setText("Error: Ingrese origen y destino");
            return;
        }

        if (!currentGraph.hasVertex(source) || !currentGraph.hasVertex(destination)) {
            modificationText.setText("Error: Los vértices deben existir");
            return;
        }

        if (source.equals(destination)) {
            modificationText.setText("Error: No se permiten bucles (mismo origen y destino)");
            return;
        }

        double weight = 1.0;
        if (!weightText.isEmpty()) {
            try {
                weight = Double.parseDouble(weightText);
                if (weight <= 0) {
                    modificationText.setText("Error: El peso debe ser positivo");
                    return;
                }
            } catch (NumberFormatException e) {
                modificationText.setText("Error: Peso debe ser un número");
                return;
            }
        }

        try {
            saveGraphState();
            currentGraph.addEdge(source, destination, weight);
            updateGraphDisplay();
            modificationText.setText("Arista añadida: " + source + " → " + destination + " (peso: " + weight + ")");
        } catch (IllegalStateException e) {
            modificationText.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void edgeDelete() {
        String source = edgeOrg.getText().trim().toUpperCase();
        String destination = edgeDest.getText().trim().toUpperCase();
        String weightText = edgeWeight.getText().trim();
        Graph currentGraph = getCurrentModifyingGraph();

        if (currentGraph == null) {
            modificationText.setText("Error: Seleccione un grafo para modificar");
            return;
        }

        if (source.isEmpty() || destination.isEmpty()) {
            modificationText.setText("Error: Ingrese origen y destino");
            return;
        }

        double weight = 1.0;
        if (!weightText.isEmpty()) {
            try {
                weight = Double.parseDouble(weightText);
            } catch (NumberFormatException e) {
                modificationText.setText("Error: Peso debe ser un número");
                return;
            }
        }

        List<Edge> edgesBetween = currentGraph.getEdgesBetween(source, destination);
        if (edgesBetween.isEmpty()) {
            modificationText.setText("Error: No existe arista entre " + source + " y " + destination);
            return;
        }

        saveGraphState();
        currentGraph.removeEdge(source, destination, weight, false);
        updateGraphDisplay();
        modificationText.setText("Arista eliminada: " + source + " → " + destination);
    }

    @FXML
    private void operate() {
        if (currentOperation == null) {
            operationText.setText("Error: Seleccione una operación");
            return;
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

        Map<Edge, String> edgeToVertex = new HashMap<>();

        for (Edge edge : original.edges) {

            String vertexName = edge.source + edge.destination;
            lineGraph.addVertex(vertexName);
            edgeToVertex.put(edge, vertexName);
        }

        for (Edge e1 : original.edges) {
            for (Edge e2 : original.edges) {
                if (e1 != e2 && (e1.source.equals(e2.source) || e1.source.equals(e2.destination) ||
                        e1.destination.equals(e2.source) || e1.destination.equals(e2.destination))) {
                    lineGraph.addEdge(edgeToVertex.get(e1), edgeToVertex.get(e2), 1.0);
                }
            }
        }

        return lineGraph;
    }

    private Graph createComplementGraph(Graph original) {
        Graph complement = new Graph();
        complement.isDirected = original.isDirected;

        complement.vertices.addAll(original.vertices);

        for (String v1 : original.vertices) {
            for (String v2 : original.vertices) {
                if (!v1.equals(v2) && !original.hasEdge(v1, v2)) {
                    complement.addEdge(v1, v2, 1.0);
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

            if (!newSource.equals(newDest) && !result.hasEdge(newSource, newDest)) {
                result.addEdge(newSource, newDest, edge.weight);
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
                result.addEdge(newSource, newDest, edge.weight);
            }
        }

        return result;
    }

    private Graph union(Graph g1, Graph g2) {
        Graph union = new Graph();
        union.isDirected = g1.isDirected || g2.isDirected;

        union.vertices.addAll(g1.vertices);
        union.vertices.addAll(g2.vertices);

        for (Edge edge : g1.edges) {
            union.addEdge(edge.source, edge.destination, edge.weight);
        }
        for (Edge edge : g2.edges) {
            union.addEdge(edge.source, edge.destination, edge.weight);
        }

        return union;
    }

    private Graph intersection(Graph g1, Graph g2) {
        Graph intersection = new Graph();
        intersection.isDirected = g1.isDirected && g2.isDirected;

        for (String vertex : g1.vertices) {
            if (g2.vertices.contains(vertex)) {
                intersection.addVertex(vertex);
            }
        }

        for (Edge edge : g1.edges) {
            if (g2.hasEdge(edge.source, edge.destination) &&
                    intersection.vertices.contains(edge.source) &&
                    intersection.vertices.contains(edge.destination)) {
                intersection.addEdge(edge.source, edge.destination, edge.weight);
            }
        }

        return intersection;
    }

    private Graph ringSum(Graph g1, Graph g2) {

        Graph ringSum = union(g1, g2);

        for (Edge edge : g1.edges) {
            if (g2.hasEdge(edge.source, edge.destination)) {
                ringSum.removeEdge(edge.source, edge.destination, edge.weight, false);
            }
        }

        return ringSum;
    }

    private Graph sum(Graph g1, Graph g2) {
        Graph sum = union(g1, g2);

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                if (!sum.hasEdge(v1, v2)) {

                    sum.addEdge(v1, v2, 1.0, true);
                }
            }
        }

        return sum;
    }

    private Graph cartesianProduct(Graph g1, Graph g2) {
        Graph product = new Graph();
        product.isDirected = g1.isDirected || g2.isDirected;

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                product.addVertex("(" + v1 + "," + v2 + ")");
            }
        }

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                for (String u1 : g1.vertices) {
                    for (String u2 : g2.vertices) {
                        if (v1.equals(u1) && g2.hasEdge(v2, u2)) {
                            product.addEdge("(" + v1 + "," + v2 + ")", "(" + u1 + "," + u2 + ")", 1.0);
                        } else if (v2.equals(u2) && g1.hasEdge(v1, u1)) {
                            product.addEdge("(" + v1 + "," + v2 + ")", "(" + u1 + "," + u2 + ")", 1.0);
                        }
                    }
                }
            }
        }

        return product;
    }

    private Graph tensorProduct(Graph g1, Graph g2) {
        Graph product = new Graph();
        product.isDirected = g1.isDirected || g2.isDirected;

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                product.addVertex("(" + v1 + "," + v2 + ")");
            }
        }

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                for (String u1 : g1.vertices) {
                    for (String u2 : g2.vertices) {
                        if (g1.hasEdge(v1, u1) && g2.hasEdge(v2, u2)) {
                            product.addEdge("(" + v1 + "," + v2 + ")", "(" + u1 + "," + u2 + ")", 1.0);
                        }
                    }
                }
            }
        }

        return product;
    }

    private Graph composition(Graph g1, Graph g2) {
        Graph comp = new Graph();
        comp.isDirected = g1.isDirected || g2.isDirected;

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                comp.addVertex("(" + v1 + "," + v2 + ")");
            }
        }

        for (String v1 : g1.vertices) {
            for (String v2 : g2.vertices) {
                for (String u1 : g1.vertices) {
                    for (String u2 : g2.vertices) {
                        if (g1.hasEdge(v1, u1) || (v1.equals(u1) && g2.hasEdge(v2, u2))) {
                            comp.addEdge("(" + v1 + "," + v2 + ")", "(" + u1 + "," + u2 + ")", 1.0);
                        }
                    }
                }
            }
        }

        return comp;
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
        graph1Data = new Graph();
        graph2Data = new Graph();
        graphResultData = new Graph();

        graph1History.clear();
        graph2History.clear();
        graph1RedoStack.clear();
        graph2RedoStack.clear();

        setInitialState();
        updateAllGraphDisplays();
        modificationText.setText("Sistema reiniciado");
        operationText.setText("");
    }

    @FXML
    private void save() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar grafos");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));

            Stage stage = (Stage) saveButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {

                    oos.writeObject(graph1Data.getState());
                    oos.writeObject(graph2Data.getState());
                    oos.writeObject(graphResultData.getState());

                    modificationText.setText("Grafos guardados en: " + file.getName());
                }
            }
        } catch (Exception e) {
            modificationText.setText("Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void load() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Cargar grafos");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));

            Stage stage = (Stage) loadButton.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

                    graph1Data.setState((GraphState) ois.readObject());
                    graph2Data.setState((GraphState) ois.readObject());
                    graphResultData.setState((GraphState) ois.readObject());

                    updateAllGraphDisplays();
                    modificationText.setText("Grafos cargados desde: " + file.getName());
                    updateOperationState();
                }
            }
        } catch (Exception e) {
            modificationText.setText("Error al cargar: " + e.getMessage());
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

    private void updateOperationState() {

        boolean bothGraphsHaveData = !graph1Data.vertices.isEmpty() && !graph2Data.vertices.isEmpty();
        graphOperate.setDisable(!bothGraphsHaveData);

        if (!bothGraphsHaveData) {
            graphOperate.setText("Elegir opcion");
            operations.setText("Elegir operacion");
            setOperationControlsEnabled(false);
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
    }

    private void updateAllGraphDisplays() {
        drawGraph(graph1Data, graph1);
        drawGraph(graph2Data, graph2);
        drawGraph(graphResultData, graphResult);
    }

    private void drawGraph(Graph graph, ScrollPane scrollPane) {
        Pane canvas = new Pane();
        canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);

        double centerX = CANVAS_WIDTH / 2;
        double centerY = CANVAS_HEIGHT / 2;

        Map<String, Point2D> vertexPositions = calculateGraphLayout(graph, centerX, centerY, VERTEX_RADIUS);

        Map<String, List<Edge>> edgesByPair = new HashMap<>();
        for (Edge edge : graph.edges) {
            String key = graph.isDirected ? edge.source + "->" + edge.destination
                    : (edge.source.compareTo(edge.destination) < 0 ? edge.source + "-" + edge.destination
                            : edge.destination + "-" + edge.source);

            edgesByPair.computeIfAbsent(key, k -> new ArrayList<>()).add(edge);
        }

        for (List<Edge> parallelEdges : edgesByPair.values()) {
            if (parallelEdges.isEmpty())
                continue;

            Edge firstEdge = parallelEdges.get(0);
            Point2D sourcePos = vertexPositions.get(firstEdge.source);
            Point2D destPos = vertexPositions.get(firstEdge.destination);

            if (sourcePos != null && destPos != null) {
                if (parallelEdges.size() == 1) {

                    drawEdge(canvas, sourcePos, destPos, firstEdge.weight,
                            graph.isDirected, VERTEX_RADIUS, firstEdge.isSumEdge, 0);
                } else {

                    for (int i = 0; i < parallelEdges.size(); i++) {
                        Edge edge = parallelEdges.get(i);
                        drawEdge(canvas, sourcePos, destPos, edge.weight,
                                graph.isDirected, VERTEX_RADIUS, edge.isSumEdge, i);
                    }
                }
            }
        }

        for (Map.Entry<String, Point2D> entry : vertexPositions.entrySet()) {
            String vertex = entry.getKey();
            Point2D position = entry.getValue();
            drawVertex(canvas, position.getX(), position.getY(), VERTEX_RADIUS, vertex);
        }

        scrollPane.setContent(canvas);
    }

    private Map<String, Point2D> calculateGraphLayout(Graph graph, double centerX, double centerY, double radius) {
        Map<String, Point2D> positions = new HashMap<>();
        List<String> vertices = new ArrayList<>(graph.vertices);

        if (vertices.isEmpty()) {
            return positions;
        }

        if (vertices.size() <= 10) {

            double layoutRadius = Math.min(150, vertices.size() * 20);
            for (int i = 0; i < vertices.size(); i++) {
                double angle = 2 * Math.PI * i / vertices.size();
                double x = centerX + layoutRadius * Math.cos(angle);
                double y = centerY + layoutRadius * Math.sin(angle);
                positions.put(vertices.get(i), new Point2D(x, y));
            }
        } else {

            double area = 1000 * vertices.size();
            double k = Math.sqrt(area / vertices.size());

            Random rand = new Random();
            for (String vertex : vertices) {
                double x = centerX + (rand.nextDouble() - 0.5) * 300;
                double y = centerY + (rand.nextDouble() - 0.5) * 300;
                positions.put(vertex, new Point2D(x, y));
            }

            for (int iter = 0; iter < 100; iter++) {
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
                    Point2D diff = pos2.subtract(pos1);
                    double distance = diff.magnitude();
                    if (distance > 0) {
                        double force = (distance * distance) / k;
                        Point2D disp1 = displacements.get(edge.source).add(diff.normalize().multiply(force));
                        Point2D disp2 = displacements.get(edge.destination).subtract(diff.normalize().multiply(force));
                        displacements.put(edge.source, disp1);
                        displacements.put(edge.destination, disp2);
                    }
                }

                for (String vertex : vertices) {
                    Point2D disp = displacements.get(vertex);
                    double length = disp.magnitude();
                    if (length > 0) {
                        disp = disp.normalize().multiply(Math.min(length, 10));
                    }
                    Point2D newPos = positions.get(vertex).add(disp);

                    newPos = new Point2D(
                            Math.max(radius + 10, Math.min(CANVAS_WIDTH - radius - 10, newPos.getX())),
                            Math.max(radius + 10, Math.min(CANVAS_HEIGHT - radius - 10, newPos.getY())));
                    positions.put(vertex, newPos);
                }
            }
        }

        return positions;
    }

    private void drawVertex(Pane canvas, double x, double y, double radius, String label) {
        Circle circle = new Circle(x, y, radius);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        canvas.getChildren().add(circle);

        Text text = new Text(x - 5, y + 5, label);
        canvas.getChildren().add(text);
    }

    private void drawEdge(Pane canvas, Point2D source, Point2D target, double weight,
            boolean isDirected, double radius, boolean isSumEdge, int parallelIndex) {

        double arrowLength = 15;
        double arrowWidth = 8;

        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double unitX = dx / distance;
        double unitY = dy / distance;

        double adjustedTargetX = target.getX() - unitX * radius;
        double adjustedTargetY = target.getY() - unitY * radius;
        double adjustedSourceX = source.getX() + unitX * radius;
        double adjustedSourceY = source.getY() + unitY * radius;

        double offset = (parallelIndex - 0.5) * 8;
        double offsetX = -unitY * offset;
        double offsetY = unitX * offset;

        Line line = new Line(adjustedSourceX + offsetX, adjustedSourceY + offsetY,
                adjustedTargetX + offsetX, adjustedTargetY + offsetY);

        if (isSumEdge) {

            line.getStrokeDashArray().addAll(5d, 5d);
            line.setStroke(Color.BLUE);
        } else {
            line.setStroke(Color.BLACK);
        }

        line.setStrokeWidth(2);
        canvas.getChildren().add(line);

        if (isDirected) {
            double angle = Math.atan2(adjustedTargetY - adjustedSourceY, adjustedTargetX - adjustedSourceX);

            double arrowX = adjustedTargetX + offsetX - arrowLength * Math.cos(angle);
            double arrowY = adjustedTargetY + offsetY - arrowLength * Math.sin(angle);

            double x2 = arrowX + arrowWidth * Math.cos(angle + Math.PI / 2);
            double y2 = arrowY + arrowWidth * Math.sin(angle + Math.PI / 2);

            double x3 = arrowX + arrowWidth * Math.cos(angle - Math.PI / 2);
            double y3 = arrowY + arrowWidth * Math.sin(angle - Math.PI / 2);

            Polygon arrowHead = new Polygon();
            arrowHead.getPoints().addAll(
                    adjustedTargetX + offsetX, adjustedTargetY + offsetY,
                    x2, y2,
                    x3, y3);
            arrowHead.setFill(isSumEdge ? Color.BLUE : Color.BLACK);
            canvas.getChildren().add(arrowHead);
        }

        if (weight != 1.0 || parallelIndex > 0) {
            double midX = (adjustedSourceX + adjustedTargetX) / 2 + offsetX;
            double midY = (adjustedSourceY + adjustedTargetY) / 2 + offsetY;

            double textOffsetX = -unitY * 15;
            double textOffsetY = unitX * 15;

            Text weightText = new Text(midX + textOffsetX, midY + textOffsetY,
                    parallelIndex > 0 ? String.valueOf(parallelIndex + 1) : String.valueOf(weight));
            weightText.setFill(isSumEdge ? Color.BLUE : Color.RED);
            weightText.setStyle("-fx-font-weight: bold;");
            canvas.getChildren().add(weightText);
        }
    }

    private void setupListeners() {

    }
}