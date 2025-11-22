package ciencias.Research;

import ciencias.ResearchController;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;

public class IndexMonoController {

    @FXML private TextField txtLongitudRegistro;
    @FXML private TextField txtTamañoBloque;
    @FXML private TextField txtCantidadRegistros;
    @FXML private TextField txtLongitudCampoIndice;
    @FXML private Button btnGenerar;
    @FXML private Button btnLimpiar;
    @FXML private Canvas canvasFlechas;
    @FXML private VBox vboxIndices;
    @FXML private VBox vboxDatos;

    private ResearchController researchController;
    private boolean esPrimario = true;
    
    // Para almacenar referencias a los labels de flecha
    private Label flechaLabel1Indice, flechaLabel2Indice, flechaLabel3Indice;
    private Label flechaLabel1Datos, flechaLabel2Datos, flechaLabel3Datos;

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    public void setTipoIndice(boolean primario) {
        this.esPrimario = primario;
    }

    @FXML
    public void initialize() {
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

            // Cálculos para DATOS
            int registrosPorBloqueDatos = tamañoBloque / longitudRegistro;
            int bloquesTotalesDatos = (int) Math.ceil((double) cantidadRegistros / registrosPorBloqueDatos);

            // Cálculos para ÍNDICE
            int longitudPuntero = 4;
            int tamañoEntrada = longitudCampoIndice + longitudPuntero;
            int entradasPorBloqueIndice = tamañoBloque / tamañoEntrada;

            // En PRIMARIO: registros índice = bloques de datos
            // En SECUNDARIO: registros índice = registros de datos
            int totalRegistrosIndice = esPrimario ? bloquesTotalesDatos : cantidadRegistros;
            int bloquesTotalesIndice = (int) Math.ceil((double) totalRegistrosIndice / entradasPorBloqueIndice);

            System.out.println("=== DATOS ===");
            System.out.println("Registros por bloque: " + registrosPorBloqueDatos);
            System.out.println("Bloques totales datos: " + bloquesTotalesDatos);
            System.out.println("=== ÍNDICE ===");
            System.out.println("Total registros índice: " + totalRegistrosIndice);
            System.out.println("Entradas por bloque índice: " + entradasPorBloqueIndice);
            System.out.println("Bloques totales índice: " + bloquesTotalesIndice);

            // Generar tablas
            generarTablaIndices(bloquesTotalesIndice, entradasPorBloqueIndice, totalRegistrosIndice);
            generarTablaDatos(bloquesTotalesDatos, registrosPorBloqueDatos, cantidadRegistros);

            // Dibujar flechas
            javafx.application.Platform.runLater(() -> dibujarFlechas());

        } catch (NumberFormatException e) {
            mostrarError("Ingrese valores numéricos válidos");
        }
    }

    private void generarTablaIndices(int bloquesTotales, int entradasPorBloque, int totalRegistros) {
        vboxIndices.getChildren().clear();
        flechaLabel1Indice = null;
        flechaLabel2Indice = null;
        flechaLabel3Indice = null;

        Color[] colores = {Color.web("#E3F2FD"), Color.web("#BBDEFB"), Color.web("#90CAF9"), Color.web("#64B5F6")};
        List<Integer> bloquesAMostrar = obtenerBloquesAMostrar(bloquesTotales);

        System.out.println("Bloques a mostrar en índices: " + bloquesAMostrar);

        for (int i = 0; i < bloquesAMostrar.size(); i++) {
            int numeroBloque = bloquesAMostrar.get(i);
            VBox bloqueBox = new VBox();
            bloqueBox.setStyle("-fx-border-color: #999999; -fx-border-width: 1; -fx-padding: 3;");
            bloqueBox.setStyle(bloqueBox.getStyle() + " -fx-background-color: " + colorToHex(colores[i % 4]) + ";");

            Label tituloBloque = new Label("Bloque Índice " + (numeroBloque + 1));
            tituloBloque.setStyle("-fx-font-weight: bold; -fx-font-size: 7;");
            bloqueBox.getChildren().add(tituloBloque);

            VBox filasBox = new VBox();
            filasBox.setSpacing(1);

            // Registros de este bloque de índice
            int registroInicial = numeroBloque * entradasPorBloque + 1;
            int registroFinal = Math.min((numeroBloque + 1) * entradasPorBloque, totalRegistros);
            int totalRegistrosBloque = registroFinal - registroInicial + 1;

            System.out.println("Bloque " + numeroBloque + ": registros " + registroInicial + " a " + registroFinal);

            // PRIMER REGISTRO del bloque
            if (registroInicial <= totalRegistros) {
                Label primeraFila = crearFilaIndice(registroInicial);
                filasBox.getChildren().add(primeraFila);
                
                // Marcar flecha 1 si es el primer bloque mostrado
                if (numeroBloque == bloquesAMostrar.get(0)) {
                    flechaLabel1Indice = primeraFila;
                }
            }

            // REGISTRO DEL MEDIO del bloque
            if (totalRegistrosBloque > 2) {
                int registroMedio = registroInicial + totalRegistrosBloque / 2;
                if (registroMedio <= totalRegistros) {
                    Label middleFila = crearFilaIndice(registroMedio);
                    filasBox.getChildren().add(middleFila);
                }
            }

            // ÚLTIMO REGISTRO del bloque
            if (totalRegistrosBloque > 1 && registroFinal <= totalRegistros) {
                Label ultimaFila = crearFilaIndice(registroFinal);
                filasBox.getChildren().add(ultimaFila);
                
                // Marcar flechas según el bloque
                if (numeroBloque == bloquesAMostrar.get(bloquesAMostrar.size() / 2)) {
                    flechaLabel2Indice = ultimaFila;
                }
                if (numeroBloque == bloquesAMostrar.get(bloquesAMostrar.size() - 1)) {
                    flechaLabel3Indice = ultimaFila;
                }
            }

            bloqueBox.getChildren().add(filasBox);
            vboxIndices.getChildren().add(bloqueBox);
        }
    }

    private void generarTablaDatos(int bloquesTotales, int registrosPorBloque, int cantidadRegistros) {
        vboxDatos.getChildren().clear();
        flechaLabel1Datos = null;
        flechaLabel2Datos = null;
        flechaLabel3Datos = null;

        Color[] colores = {Color.web("#F3E5F5"), Color.web("#E1BEE7"), Color.web("#CE93D8"), Color.web("#BA68C8")};
        List<Integer> bloquesAMostrar = obtenerBloquesAMostrar(bloquesTotales);

        System.out.println("Bloques a mostrar en datos: " + bloquesAMostrar);

        for (int i = 0; i < bloquesAMostrar.size(); i++) {
            int numeroBloque = bloquesAMostrar.get(i);
            VBox bloqueBox = new VBox();
            bloqueBox.setStyle("-fx-border-color: #999999; -fx-border-width: 1; -fx-padding: 3;");
            bloqueBox.setStyle(bloqueBox.getStyle() + " -fx-background-color: " + colorToHex(colores[i % 4]) + ";");

            Label tituloBloque = new Label("Bloque Datos " + (numeroBloque + 1));
            tituloBloque.setStyle("-fx-font-weight: bold; -fx-font-size: 7;");
            bloqueBox.getChildren().add(tituloBloque);

            Label encabezado = crearEncabezado();
            bloqueBox.getChildren().add(encabezado);

            VBox filasBox = new VBox();
            filasBox.setSpacing(1);

            int registroInicial = numeroBloque * registrosPorBloque + 1;
            int registroFinal = Math.min((numeroBloque + 1) * registrosPorBloque, cantidadRegistros);
            int totalRegistrosBloque = registroFinal - registroInicial + 1;

            // PRIMER REGISTRO
            if (registroInicial <= cantidadRegistros) {
                Label primeraFila = crearFilaDatos(registroInicial, numeroBloque + 1);
                filasBox.getChildren().add(primeraFila);
                
                if (numeroBloque == bloquesAMostrar.get(0)) {
                    flechaLabel1Datos = primeraFila;
                }
            }

            // REGISTRO DEL MEDIO
            if (totalRegistrosBloque > 2) {
                int registroMedio = registroInicial + totalRegistrosBloque / 2;
                if (registroMedio <= cantidadRegistros) {
                    Label middleFila = crearFilaDatos(registroMedio, numeroBloque + 1);
                    filasBox.getChildren().add(middleFila);
                }
            }

            // ÚLTIMO REGISTRO
            if (totalRegistrosBloque > 1 && registroFinal <= cantidadRegistros) {
                Label ultimaFila = crearFilaDatos(registroFinal, numeroBloque + 1);
                filasBox.getChildren().add(ultimaFila);
                
                if (numeroBloque == bloquesAMostrar.get(bloquesAMostrar.size() / 2)) {
                    flechaLabel2Datos = ultimaFila;
                }
                if (numeroBloque == bloquesAMostrar.get(bloquesAMostrar.size() - 1)) {
                    flechaLabel3Datos = ultimaFila;
                }
            }

            bloqueBox.getChildren().add(filasBox);
            vboxDatos.getChildren().add(bloqueBox);
        }
    }

    private Label crearFilaIndice(int numeroRegistro) {
        String contenido = String.format("%d | %d → Reg %d", numeroRegistro, numeroRegistro, numeroRegistro);
        Label label = new Label(contenido);
        label.setFont(Font.font("Monospace", 7));
        label.setStyle("-fx-padding: 1;");
        return label;
    }

    private Label crearFilaDatos(int numeroRegistro, int numeroBloque) {
        Label label = new Label(String.format("%d | %d | D%d | D%d | ... | Di%d | %d",
                numeroRegistro, numeroRegistro, numeroRegistro, numeroRegistro + 1000, numeroRegistro, numeroBloque));
        label.setFont(Font.font("Monospace", 7));
        label.setStyle("-fx-padding: 1;");
        return label;
    }

    private Label crearEncabezado() {
        Label label = new Label("# Regi | PK | D1 | D2 | ... | Di | # Blq");
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
            bloques.add(0);                          // Bloque 0 (primer bloque)
            bloques.add(1);                          // Bloque 1 (segundo bloque)
            bloques.add(totalBloques / 2);           // Bloque del medio
            bloques.add(totalBloques - 1);           // Último bloque
        }
        System.out.println("Total bloques: " + totalBloques + ", A mostrar: " + bloques);
        return bloques;
    }

    private void dibujarFlechas() {
        GraphicsContext gc = canvasFlechas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasFlechas.getWidth(), canvasFlechas.getHeight());

        System.out.println("Dibujando flechas...");
        System.out.println("Flecha 1 - Índice: " + flechaLabel1Indice + ", Datos: " + flechaLabel1Datos);
        System.out.println("Flecha 2 - Índice: " + flechaLabel2Indice + ", Datos: " + flechaLabel2Datos);
        System.out.println("Flecha 3 - Índice: " + flechaLabel3Indice + ", Datos: " + flechaLabel3Datos);

        try {
            // FLECHA 1: Verde
            if (flechaLabel1Indice != null && flechaLabel1Datos != null) {
                dibujarFlechaEntreLabels(gc, flechaLabel1Indice, flechaLabel1Datos, Color.GREEN);
                System.out.println("✓ Flecha 1 (Verde) dibujada");
            }

            // FLECHA 2: Azul
            if (flechaLabel2Indice != null && flechaLabel2Datos != null) {
                dibujarFlechaEntreLabels(gc, flechaLabel2Indice, flechaLabel2Datos, Color.BLUE);
                System.out.println("✓ Flecha 2 (Azul) dibujada");
            }

            // FLECHA 3: Rojo
            if (flechaLabel3Indice != null && flechaLabel3Datos != null) {
                dibujarFlechaEntreLabels(gc, flechaLabel3Indice, flechaLabel3Datos, Color.RED);
                System.out.println("✓ Flecha 3 (Rojo) dibujada");
            }

        } catch (Exception e) {
            System.err.println("Error al dibujar flechas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void dibujarFlechaEntreLabels(GraphicsContext gc, Label labelIzq, Label labelDer, Color color) {
        try {
            Bounds boundsIzq = labelIzq.localToScene(labelIzq.getBoundsInLocal());
            Bounds boundsDer = labelDer.localToScene(labelDer.getBoundsInLocal());
            Bounds canvasBounds = canvasFlechas.localToScene(canvasFlechas.getBoundsInLocal());

            if (boundsIzq == null || boundsDer == null || boundsIzq.isEmpty() || boundsDer.isEmpty()) {
                System.err.println("Bounds vacíos");
                return;
            }

            // Convertir a coordenadas del canvas
            double x1 = boundsIzq.getCenterX() - canvasBounds.getMinX();
            double y1 = boundsIzq.getCenterY() - canvasBounds.getMinY();
            double x2 = boundsDer.getCenterX() - canvasBounds.getMinX();
            double y2 = boundsDer.getCenterY() - canvasBounds.getMinY();

            System.out.println("Dibujando línea de (" + x1 + "," + y1 + ") a (" + x2 + "," + y2 + ")");

            // Dibujar línea
            gc.setStroke(color);
            gc.setLineWidth(2);
            gc.strokeLine(x1, y1, x2, y2);

            // Punta de flecha
            double angle = Math.atan2(y2 - y1, x2 - x1);
            double arrowSize = 8;
            gc.setFill(color);
            gc.fillPolygon(
                    new double[]{x2, x2 - arrowSize * Math.cos(angle - Math.PI / 6), x2 - arrowSize * Math.cos(angle + Math.PI / 6)},
                    new double[]{y2, y2 - arrowSize * Math.sin(angle - Math.PI / 6), y2 - arrowSize * Math.sin(angle + Math.PI / 6)},
                    3
            );

        } catch (Exception e) {
            System.err.println("Error dibujando flecha: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void limpiar() {
        vboxIndices.getChildren().clear();
        vboxDatos.getChildren().clear();
        canvasFlechas.getGraphicsContext2D().clearRect(0, 0, canvasFlechas.getWidth(), canvasFlechas.getHeight());
        flechaLabel1Indice = null;
        flechaLabel2Indice = null;
        flechaLabel3Indice = null;
        flechaLabel1Datos = null;
        flechaLabel2Datos = null;
        flechaLabel3Datos = null;
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







