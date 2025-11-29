package ciencias.Research;

import ciencias.ResearchController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.stage.Screen;

import java.util.*;

public class IndexMultiController {

    @FXML private TextField txtLongitudRegistro;
    @FXML private TextField txtTamañoBloque;
    @FXML private TextField txtCantidadRegistros;
    @FXML private TextField txtLongitudCampoIndice;
    @FXML private Button btnGenerar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnMaximizar;
    @FXML private Canvas canvasFlechas;
    @FXML private HBox hboxTablas;
    @FXML private ScrollPane scrollPanePrincipal;

    private ResearchController researchController;
    private boolean esPrimario = true;
    private boolean isMaximized = false;
    private javafx.stage.Stage maximizedStage;
    
    private List<VBox> nivelesIndices = new ArrayList<>();
    private VBox contenedorDatos;

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    public void setTipoIndice(boolean primario) {
        this.esPrimario = primario;
    }

    @FXML
    public void initialize() {
        scrollPanePrincipal.setFitToHeight(true);
        scrollPanePrincipal.setFitToWidth(false);
        
        // Redimensionar canvas cuando la ventana cambia
        canvasFlechas.widthProperty().bind(scrollPanePrincipal.widthProperty());
        canvasFlechas.heightProperty().addListener((obs, old, newVal) -> dibujarFlechasDinamicas());
        canvasFlechas.widthProperty().addListener((obs, old, newVal) -> dibujarFlechasDinamicas());
    }

    @FXML
    public void generarEstructura() {
        try {
            int longitudRegistro = Integer.parseInt(txtLongitudRegistro.getText());
            int tamañoBloque = Integer.parseInt(txtTamañoBloque.getText());
            int cantidadRegistros = Integer.parseInt(txtCantidadRegistros.getText());
            int longitudCampoIndice = Integer.parseInt(txtLongitudCampoIndice.getText());

            if (longitudRegistro > tamañoBloque) {
                mostrarError("Longitud de registro no puede exceder tamaño de bloque");
                return;
            }

            int registrosPorBloqueDatos = tamañoBloque / longitudRegistro;
            int bloquesTotalesDatos = (int) Math.ceil((double) cantidadRegistros / registrosPorBloqueDatos);
            int entradasPorBloqueIndice = tamañoBloque / longitudCampoIndice;

            hboxTablas.getChildren().clear();
            nivelesIndices.clear();

            // Generar índices
            int baseParaMultinivel = esPrimario ? bloquesTotalesDatos : cantidadRegistros;
            generarIndicesMultinivel(baseParaMultinivel, entradasPorBloqueIndice);

            // Generar datos
            generarTablaDatos(bloquesTotalesDatos, registrosPorBloqueDatos, cantidadRegistros);

            // Agregar en HBOX ÚNICA: B_max, B_max-1, ..., B1, DATOS
            for (int i = nivelesIndices.size() - 1; i >= 0; i--) {
                hboxTablas.getChildren().add(nivelesIndices.get(i));
            }
            
            hboxTablas.getChildren().add(contenedorDatos);

            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> dibujarFlechasDinamicas());
            pause.play();

        } catch (NumberFormatException e) {
            mostrarError("Ingrese valores numéricos válidos");
        }
    }

    private void generarIndicesMultinivel(int bloquesNivel, int entradasPorBloque) {
        int nivelActual = 1;
        int registrosActuales = bloquesNivel;

        while (registrosActuales > 1) {
            int bloquesNivelActual = (int) Math.ceil((double) registrosActuales / entradasPorBloque);
            VBox contenedor = generarTablaIndicesNivel(bloquesNivelActual, entradasPorBloque, registrosActuales, nivelActual);
            nivelesIndices.add(contenedor);
            registrosActuales = bloquesNivelActual;
            nivelActual++;
        }
    }

    private VBox generarTablaIndicesNivel(int bloquesTotales, int entradasPorBloque, int totalRegistros, int nivelActual) {
        VBox contenedorNivel = new VBox();
        contenedorNivel.setSpacing(3);
        contenedorNivel.setStyle("-fx-border-color: #999999; -fx-border-width: 1; -fx-padding: 3;");
        contenedorNivel.setPrefWidth(300);
        contenedorNivel.setMinWidth(300);
        contenedorNivel.setMaxWidth(300);

        Label tituloNivel = new Label("Bloque Índice Nivel B" + nivelActual);
        tituloNivel.setStyle("-fx-font-size: 8; -fx-font-weight: bold;");
        contenedorNivel.getChildren().add(tituloNivel);

        Color[] coloresPasteles = {
            Color.web("#D4E8F7"),
            Color.web("#CCEEF5"),
            Color.web("#C0E8F0"),
            Color.web("#B3E0EB")
        };

        List<Integer> bloquesAMostrar = obtenerBloquesAMostrar(bloquesTotales);

        for (int i = 0; i < bloquesAMostrar.size(); i++) {
            int numeroBloque = bloquesAMostrar.get(i);
            boolean esUltimoBloque = (numeroBloque == bloquesTotales - 1);

            VBox bloqueBox = new VBox();
            bloqueBox.setStyle("-fx-border-color: #999999; -fx-border-width: 1; -fx-padding: 3;");
            bloqueBox.setStyle(bloqueBox.getStyle() + " -fx-background-color: " + colorToHex(coloresPasteles[i % 4]) + ";");

            Label tituloBloque = new Label("Bloque B" + nivelActual + " - " + (numeroBloque + 1));
            tituloBloque.setStyle("-fx-font-weight: bold; -fx-font-size: 7;");
            bloqueBox.getChildren().add(tituloBloque);

            VBox filasBox = new VBox();
            filasBox.setSpacing(1);

            int registroInicial = numeroBloque * entradasPorBloque + 1;
            int registroFinal = (numeroBloque + 1) * entradasPorBloque;
            int ultimoRegistroLleno = Math.min(registroFinal, totalRegistros);

            if (registroInicial <= totalRegistros) {
                Label primeraFila = crearFilaIndicePrimario(registroInicial, nivelActual);
                filasBox.getChildren().add(primeraFila);
            }

            if (esUltimoBloque) {
                if (ultimoRegistroLleno < registroFinal && ultimoRegistroLleno >= registroInicial) {
                    Label middleFila = crearFilaIndicePrimario(ultimoRegistroLleno, nivelActual);
                    filasBox.getChildren().add(middleFila);
                }
            } else {
                int totalRegistrosBloque = Math.min(entradasPorBloque, Math.max(0, totalRegistros - registroInicial + 1));
                if (totalRegistrosBloque > 2) {
                    int registroMedio = registroInicial + totalRegistrosBloque / 2;
                    if (registroMedio <= totalRegistros) {
                        Label middleFila = crearFilaIndicePrimario(registroMedio, nivelActual);
                        filasBox.getChildren().add(middleFila);
                    }
                }
            }

            Label ultimaFila;
            if (registroFinal <= totalRegistros) {
                ultimaFila = crearFilaIndicePrimario(registroFinal, nivelActual);
            } else {
                ultimaFila = crearFilaIndiceVacio(registroFinal);
            }
            filasBox.getChildren().add(ultimaFila);

            bloqueBox.getChildren().add(filasBox);
            contenedorNivel.getChildren().add(bloqueBox);
        }

        return contenedorNivel;
    }

    private void generarTablaDatos(int bloquesTotales, int registrosPorBloque, int cantidadRegistros) {
        contenedorDatos = new VBox();
        contenedorDatos.setSpacing(3);
        contenedorDatos.setStyle("-fx-border-color: #999999; -fx-border-width: 1; -fx-padding: 3;");
        contenedorDatos.setPrefWidth(300);
        contenedorDatos.setMinWidth(300);
        contenedorDatos.setMaxWidth(300);

        Label tituloDatos = new Label("Bloque Datos");
        tituloDatos.setStyle("-fx-font-size: 8; -fx-font-weight: bold;");
        contenedorDatos.getChildren().add(tituloDatos);

        Color[] coloresPasteles = {
            Color.web("#F0D9E8"),
            Color.web("#EDD0E0"),
            Color.web("#E8C7D8"),
            Color.web("#E0BDD0")
        };

        List<Integer> bloquesAMostrar = obtenerBloquesAMostrar(bloquesTotales);

        for (int i = 0; i < bloquesAMostrar.size(); i++) {
            int numeroBloque = bloquesAMostrar.get(i);
            boolean esUltimoBloque = (numeroBloque == bloquesTotales - 1);

            VBox bloqueBox = new VBox();
            bloqueBox.setStyle("-fx-border-color: #999999; -fx-border-width: 1; -fx-padding: 3;");
            bloqueBox.setStyle(bloqueBox.getStyle() + " -fx-background-color: " + colorToHex(coloresPasteles[i % 4]) + ";");

            Label tituloBloque = new Label("Bloque Datos " + (numeroBloque + 1));
            tituloBloque.setStyle("-fx-font-weight: bold; -fx-font-size: 7;");
            bloqueBox.getChildren().add(tituloBloque);

            Label encabezado = crearEncabezado();
            bloqueBox.getChildren().add(encabezado);

            VBox filasBox = new VBox();
            filasBox.setSpacing(1);

            int registroInicial = numeroBloque * registrosPorBloque + 1;
            int registroFinal = (numeroBloque + 1) * registrosPorBloque;
            int ultimoRegistroLleno = Math.min(registroFinal, cantidadRegistros);

            if (registroInicial <= cantidadRegistros) {
                Label primeraFila = crearFilaDatos(registroInicial, numeroBloque + 1);
                filasBox.getChildren().add(primeraFila);
            }

            if (esUltimoBloque) {
                if (ultimoRegistroLleno < registroFinal && ultimoRegistroLleno >= registroInicial) {
                    Label middleFila = crearFilaDatos(ultimoRegistroLleno, numeroBloque + 1);
                    filasBox.getChildren().add(middleFila);
                }
            } else {
                int totalRegistrosBloque = Math.min(registrosPorBloque, Math.max(0, cantidadRegistros - registroInicial + 1));
                if (totalRegistrosBloque > 2) {
                    int registroMedio = registroInicial + totalRegistrosBloque / 2;
                    if (registroMedio <= cantidadRegistros) {
                        Label middleFila = crearFilaDatos(registroMedio, numeroBloque + 1);
                        filasBox.getChildren().add(middleFila);
                    }
                }
            }

            Label ultimaFila;
            if (registroFinal <= cantidadRegistros) {
                ultimaFila = crearFilaDatos(registroFinal, numeroBloque + 1);
            } else {
                ultimaFila = crearFilaDatoVacio(registroFinal, numeroBloque + 1);
            }
            filasBox.getChildren().add(ultimaFila);

            bloqueBox.getChildren().add(filasBox);
            contenedorDatos.getChildren().add(bloqueBox);
        }
    }

    @FXML
    private void maximizar() {
        if (isMaximized) {
            if (maximizedStage != null) {
                maximizedStage.close();
            }
            isMaximized = false;
            return;
        }

        try {
            // Obtener tamaño de pantalla disponible
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Estructura de Índices - Pantalla Completa");
            stage.setWidth(screenWidth * 0.95);  // 95% del ancho
            stage.setHeight(screenHeight * 0.90); // 90% del alto
            stage.setX(screen.getVisualBounds().getMinX() + (screenWidth - screenWidth * 0.95) / 2);
            stage.setY(screen.getVisualBounds().getMinY() + (screenHeight - screenHeight * 0.90) / 2);

            javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
            
            javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox();
            Button btnVolverNormal = new Button("Volver a Vista Normal");
            btnVolverNormal.setStyle("-fx-font-size: 12; -fx-padding: 10;");
            btnVolverNormal.setOnAction(e -> stage.close());
            topBar.getChildren().add(btnVolverNormal);
            topBar.setPadding(new javafx.geometry.Insets(10));
            topBar.setStyle("-fx-background-color: #f0f0f0;");
            root.setTop(topBar);

            // Canvas GRANDE para flechas
            double canvasHeight = 100;
            Canvas canvasAmpliado = new Canvas(screenWidth * 0.90, canvasHeight);
            
            // HBox clonado con tablas
            HBox hboxClonado = clonarHBox(hboxTablas);
            ScrollPane scrollAmp = new ScrollPane(hboxClonado);
            scrollAmp.setFitToHeight(true);
            scrollAmp.setFitToWidth(false);
            scrollAmp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            scrollAmp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollAmp.setStyle("-fx-padding: 0;");

            javafx.scene.layout.BorderPane centerPane = new javafx.scene.layout.BorderPane();
            centerPane.setTop(canvasAmpliado);
            centerPane.setCenter(scrollAmp);

            root.setCenter(centerPane);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setOnCloseRequest(e -> {
                isMaximized = false;
                maximizedStage = null;
            });
            
            stage.show();
            isMaximized = true;
            maximizedStage = stage;
            
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(event -> dibujarFlechasEnCanvasAmpliado(canvasAmpliado, hboxClonado));
            pause.play();

        } catch (Exception e) {
            System.err.println("Error al maximizar: " + e.getMessage());
            e.printStackTrace();
            isMaximized = false;
        }
    }

    private HBox clonarHBox(HBox original) {
        HBox clon = new HBox();
        clon.setSpacing(original.getSpacing());
        clon.setStyle(original.getStyle());

        for (javafx.scene.Node hijo : original.getChildren()) {
            if (hijo instanceof VBox) {
                clon.getChildren().add(clonarVBox((VBox) hijo));
            }
        }

        return clon;
    }

    private void dibujarFlechasEnCanvasAmpliado(Canvas canvas, HBox hboxClonado) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        try {
            List<VBox> contenedores = new ArrayList<>();
            for (javafx.scene.Node node : hboxClonado.getChildren()) {
                if (node instanceof VBox) {
                    contenedores.add((VBox) node);
                }
            }

            if (contenedores.size() < 2) return;

            // Flechas entre cada contenedor consecutivo
            for (int i = 0; i < contenedores.size() - 1; i++) {
                List<VBox> bloquesActuales = obtenerBloquesDeContenedor(contenedores.get(i));
                List<VBox> bloquesSiguientes = obtenerBloquesDeContenedor(contenedores.get(i + 1));
                
                if (!bloquesActuales.isEmpty() && !bloquesSiguientes.isEmpty()) {
                    dibujarFlechaDesdeBloques(gc, canvas, bloquesActuales.get(0), bloquesSiguientes.get(0), Color.GREEN);
                    
                    if (bloquesSiguientes.size() > 2) {
                        dibujarFlechaDesdeBloques(gc, canvas, 
                            bloquesActuales.get(Math.min(bloquesActuales.size()/2, bloquesActuales.size()-1)), 
                            bloquesSiguientes.get(bloquesSiguientes.size()/2), 
                            Color.BLUE);
                    }
                    
                    if (bloquesSiguientes.size() > 1) {
                        dibujarFlechaDesdeBloques(gc, canvas, 
                            bloquesActuales.get(bloquesActuales.size()-1), 
                            bloquesSiguientes.get(bloquesSiguientes.size()-1), 
                            Color.RED);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error dibujando flechas ampliadas: " + e.getMessage());
        }
    }

    private List<VBox> obtenerBloquesDeContenedor(VBox contenedor) {
        List<VBox> bloques = new ArrayList<>();
        for (javafx.scene.Node node : contenedor.getChildren()) {
            if (node instanceof VBox) {
                bloques.add((VBox) node);
            }
        }
        return bloques;
    }

    private VBox clonarVBox(VBox original) {
        VBox clon = new VBox();
        clon.setSpacing(original.getSpacing());
        clon.setStyle(original.getStyle());
        clon.setPrefWidth(original.getPrefWidth());
        clon.setMinWidth(original.getMinWidth());
        clon.setMaxWidth(original.getMaxWidth());

        for (javafx.scene.Node hijo : original.getChildren()) {
            if (hijo instanceof Label) {
                Label labelOrig = (Label) hijo;
                Label labelClon = new Label(labelOrig.getText());
                labelClon.setStyle(labelOrig.getStyle());
                labelClon.setFont(labelOrig.getFont());
                clon.getChildren().add(labelClon);
            } else if (hijo instanceof VBox) {
                clon.getChildren().add(clonarVBox((VBox) hijo));
            }
        }

        return clon;
    }

    private void dibujarFlechasDinamicas() {
        if (canvasFlechas == null || hboxTablas.getChildren().isEmpty()) return;

        GraphicsContext gc = canvasFlechas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasFlechas.getWidth(), canvasFlechas.getHeight());

        try {
            List<VBox> contenedores = new ArrayList<>();
            for (javafx.scene.Node node : hboxTablas.getChildren()) {
                if (node instanceof VBox) {
                    contenedores.add((VBox) node);
                }
            }

            if (contenedores.size() < 2) return;

            // Flechas entre cada contenedor consecutivo
            for (int i = 0; i < contenedores.size() - 1; i++) {
                List<VBox> bloquesActuales = obtenerBloquesDeContenedor(contenedores.get(i));
                List<VBox> bloquesSiguientes = obtenerBloquesDeContenedor(contenedores.get(i + 1));
                
                if (!bloquesActuales.isEmpty() && !bloquesSiguientes.isEmpty()) {
                    // Verde: primero
                    dibujarFlechaDesdeBloques(gc, canvasFlechas, bloquesActuales.get(0), bloquesSiguientes.get(0), Color.GREEN);
                    
                    // Azul: medio
                    if (bloquesSiguientes.size() > 2) {
                        dibujarFlechaDesdeBloques(gc, canvasFlechas, 
                            bloquesActuales.get(Math.min(bloquesActuales.size()/2, bloquesActuales.size()-1)), 
                            bloquesSiguientes.get(bloquesSiguientes.size()/2), 
                            Color.BLUE);
                    }
                    
                    // Roja: último
                    if (bloquesSiguientes.size() > 1) {
                        dibujarFlechaDesdeBloques(gc, canvasFlechas, 
                            bloquesActuales.get(bloquesActuales.size()-1), 
                            bloquesSiguientes.get(bloquesSiguientes.size()-1), 
                            Color.RED);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error en dibujarFlechasDinamicas: " + e.getMessage());
        }
    }

    private void dibujarFlechaDesdeBloques(GraphicsContext gc, Canvas canvas, VBox bloqueOrigen, VBox bloqueDestino, Color color) {
        try {
            Bounds boundsOrigen = bloqueOrigen.localToScene(bloqueOrigen.getBoundsInLocal());
            Bounds boundsDestino = bloqueDestino.localToScene(bloqueDestino.getBoundsInLocal());
            Bounds canvasBounds = canvas.localToScene(canvas.getBoundsInLocal());

            if (boundsOrigen == null || boundsDestino == null || canvasBounds == null) return;

            double x1 = boundsOrigen.getCenterX() - canvasBounds.getMinX();
            double y1 = boundsOrigen.getMaxY() - canvasBounds.getMinY();
            double x2 = boundsDestino.getCenterX() - canvasBounds.getMinX();
            double y2 = boundsDestino.getMinY() - canvasBounds.getMinY();

            // Línea
            gc.setStroke(color);
            gc.setLineWidth(2.5);
            gc.strokeLine(x1, y1, x2, y2);

            // Punta de flecha
            double angle = Math.atan2(y2 - y1, x2 - x1);
            double arrowSize = 12;
            gc.setFill(color);
            gc.fillPolygon(
                    new double[]{x2, x2 - arrowSize * Math.cos(angle - Math.PI / 6), x2 - arrowSize * Math.cos(angle + Math.PI / 6)},
                    new double[]{y2, y2 - arrowSize * Math.sin(angle - Math.PI / 6), y2 - arrowSize * Math.sin(angle + Math.PI / 6)},
                    3
            );

        } catch (Exception e) {
            System.err.println("Error dibujando flecha: " + e.getMessage());
        }
    }

    private Label crearFilaIndicePrimario(int numeroRegistro, int nivelActual) {
        String contenido = String.format("%d | %d → B%d-%d", numeroRegistro, numeroRegistro, nivelActual - 1, numeroRegistro);
        Label label = new Label(contenido);
        label.setFont(Font.font("Monospace", 7));
        label.setStyle("-fx-padding: 1;");
        return label;
    }

    private Label crearFilaIndiceVacio(int numeroRegistro) {
        Label l = new Label(numeroRegistro + " | ... | [vacío]");
        l.setFont(Font.font("Monospace", 7));
        l.setStyle("-fx-font-style: italic; -fx-text-fill: #000000; -fx-padding: 1;");
        return l;
    }

    private Label crearFilaDatos(int numeroRegistro, int numeroBloque) {
        Label label = new Label(String.format("%d | %d | D%d | ... | Di%d | %d",
                numeroRegistro, numeroRegistro, numeroRegistro, numeroRegistro, numeroBloque));
        label.setFont(Font.font("Monospace", 7));
        label.setStyle("-fx-padding: 1;");
        return label;
    }

    private Label crearFilaDatoVacio(int numeroRegistro, int numeroBloque) {
        Label l = new Label(numeroRegistro + " | ... | [vacío] | " + numeroBloque);
        l.setFont(Font.font("Monospace", 7));
        l.setStyle("-fx-font-style: italic; -fx-text-fill: #000000; -fx-padding: 1;");
        return l;
    }

    private Label crearEncabezado() {
        Label label = new Label("# Regi | PK | D1 | ... | Di | # Blq");
        label.setFont(Font.font("Monospace", 7));
        label.setStyle("-fx-font-weight: bold; -fx-padding: 1;");
        return label;
    }

    private List<Integer> obtenerBloquesAMostrar(int totalBloques) {
        List<Integer> bloques = new ArrayList<>();
        if (totalBloques <= 4) {
            for (int i = 0; i < totalBloques; i++) {
                bloques.add(i);
            }
        } else {
            bloques.add(0);
            bloques.add(1);
            bloques.add(totalBloques / 2);
            bloques.add(totalBloques - 1);
        }
        return bloques;
    }

    @FXML
    public void limpiar() {
        hboxTablas.getChildren().clear();
        nivelesIndices.clear();
        contenedorDatos = null;
        if (isMaximized && maximizedStage != null) {
            maximizedStage.close();
            isMaximized = false;
        }
        GraphicsContext gc = canvasFlechas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasFlechas.getWidth(), canvasFlechas.getHeight());
    }

    private String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}



