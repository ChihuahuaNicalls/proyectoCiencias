package ciencias.Research;

import ciencias.ResearchController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

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
    
    private List<VBox> bloquesIndicesMostrados = new ArrayList<>();
    private List<VBox> bloquesDatosMostrados = new ArrayList<>();

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

        int registrosPorBloqueDatos = tamañoBloque / longitudRegistro;
        int bloquesTotalesDatos = (int) Math.ceil((double) cantidadRegistros / registrosPorBloqueDatos);

        int entradasPorBloqueIndice = tamañoBloque / longitudCampoIndice;
        int totalRegistrosIndice = esPrimario ? bloquesTotalesDatos : cantidadRegistros;
        int bloquesTotalesIndice = (int) Math.ceil((double) totalRegistrosIndice / entradasPorBloqueIndice);

        bloquesIndicesMostrados.clear();
        bloquesDatosMostrados.clear();

        generarTablaIndices(bloquesTotalesIndice, entradasPorBloqueIndice, totalRegistrosIndice);
        generarTablaDatos(bloquesTotalesDatos, registrosPorBloqueDatos, cantidadRegistros);

        // Esperar a que el layout esté completamente renderizado antes de dibujar las flechas
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(event -> dibujarFlechasDinamicas());
        pause.play();

    } catch (NumberFormatException e) {
        mostrarError("Ingrese valores numéricos válidos");
    }
}

    private void generarTablaIndices(int bloquesTotales, int entradasPorBloque, int totalRegistros) {
        vboxIndices.getChildren().clear();

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

            Label tituloBloque = new Label("Bloque Índice " + (numeroBloque + 1));
            tituloBloque.setStyle("-fx-font-weight: bold; -fx-font-size: 7;");
            bloqueBox.getChildren().add(tituloBloque);

            VBox filasBox = new VBox();
            filasBox.setSpacing(1);

            int registroInicial = numeroBloque * entradasPorBloque + 1;
            int registroFinal = (numeroBloque + 1) * entradasPorBloque;
            int ultimoRegistroLleno = Math.min(registroFinal, totalRegistros);

            // PRIMER REGISTRO
            if (registroInicial <= totalRegistros) {
                Label primeraFila = crearFilaIndice(registroInicial, esPrimario);
                filasBox.getChildren().add(primeraFila);
            }

            // REGISTRO DEL MEDIO O ÚLTIMO LLENO (especial para último bloque)
            if (esUltimoBloque) {
                // En el último bloque: mostrar último registro lleno como "del medio"
                if (ultimoRegistroLleno < registroFinal && ultimoRegistroLleno >= registroInicial) {
                    Label middleFila = crearFilaIndice(ultimoRegistroLleno, esPrimario);
                    filasBox.getChildren().add(middleFila);
                }
            } else {
                // En bloques normales: mostrar del medio como antes
                int totalRegistrosBloque = Math.min(entradasPorBloque, Math.max(0, totalRegistros - registroInicial + 1));
                if (totalRegistrosBloque > 2) {
                    int registroMedio = registroInicial + totalRegistrosBloque / 2;
                    if (registroMedio <= totalRegistros) {
                        Label middleFila = crearFilaIndice(registroMedio, esPrimario);
                        filasBox.getChildren().add(middleFila);
                    }
                }
            }

            // ÚLTIMO REGISTRO (teórico)
            Label ultimaFila;
            if (registroFinal <= totalRegistros) {
                ultimaFila = crearFilaIndice(registroFinal, esPrimario);
            } else {
                ultimaFila = crearFilaIndiceVacio(registroFinal);
            }
            filasBox.getChildren().add(ultimaFila);

            bloqueBox.getChildren().add(filasBox);
            vboxIndices.getChildren().add(bloqueBox);
            bloquesIndicesMostrados.add(bloqueBox);
        }
    }

    private void generarTablaDatos(int bloquesTotales, int registrosPorBloque, int cantidadRegistros) {
        vboxDatos.getChildren().clear();

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

            // PRIMER REGISTRO
            if (registroInicial <= cantidadRegistros) {
                Label primeraFila = crearFilaDatos(registroInicial, numeroBloque + 1);
                filasBox.getChildren().add(primeraFila);
            }

            // REGISTRO DEL MEDIO O ÚLTIMO LLENO (especial para último bloque)
            if (esUltimoBloque) {
                // En el último bloque: mostrar último registro lleno como "del medio"
                if (ultimoRegistroLleno < registroFinal && ultimoRegistroLleno >= registroInicial) {
                    Label middleFila = crearFilaDatos(ultimoRegistroLleno, numeroBloque + 1);
                    filasBox.getChildren().add(middleFila);
                }
            } else {
                // En bloques normales: mostrar del medio como antes
                int totalRegistrosBloque = Math.min(registrosPorBloque, Math.max(0, cantidadRegistros - registroInicial + 1));
                if (totalRegistrosBloque > 2) {
                    int registroMedio = registroInicial + totalRegistrosBloque / 2;
                    if (registroMedio <= cantidadRegistros) {
                        Label middleFila = crearFilaDatos(registroMedio, numeroBloque + 1);
                        filasBox.getChildren().add(middleFila);
                    }
                }
            }

            // ÚLTIMO REGISTRO (teórico)
            Label ultimaFila;
            if (registroFinal <= cantidadRegistros) {
                ultimaFila = crearFilaDatos(registroFinal, numeroBloque + 1);
            } else {
                ultimaFila = crearFilaDatoVacio(registroFinal, numeroBloque + 1);
            }
            filasBox.getChildren().add(ultimaFila);

            bloqueBox.getChildren().add(filasBox);
            vboxDatos.getChildren().add(bloqueBox);
            bloquesDatosMostrados.add(bloqueBox);
        }
    }

    private Label crearFilaIndice(int numeroRegistro, boolean primario) {
        String contenido;
        if (primario) {
            contenido = String.format("%d | %d → Bloque %d", numeroRegistro, numeroRegistro, numeroRegistro);
        } else {
            contenido = String.format("%d | %d → Registro %d", numeroRegistro, numeroRegistro, numeroRegistro);
        }
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
        Label label = new Label(String.format("%d | %d | D%d | D%d | ... | Di%d | %d",
                numeroRegistro, numeroRegistro, numeroRegistro, numeroRegistro + 1000, numeroRegistro, numeroBloque));
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
            bloques.add(0);
            bloques.add(1);
            bloques.add(totalBloques / 2);
            bloques.add(totalBloques - 1);
        }
        return bloques;
    }

    private void dibujarFlechasDinamicas() {
        // Validación robusta antes de dibujar
        if (canvasFlechas == null || vboxIndices == null || vboxDatos == null) {
            System.out.println("DEBUG - Componentes nulos");
            return;
        }

        if (bloquesIndicesMostrados == null || bloquesDatosMostrados == null) {
            System.out.println("DEBUG - Listas de bloques nulas");
            return;
        }

        if (bloquesIndicesMostrados.isEmpty() || bloquesDatosMostrados.isEmpty()) {
            System.out.println("DEBUG - Bloques vacíos");
            return;
        }

        GraphicsContext gc = canvasFlechas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasFlechas.getWidth(), canvasFlechas.getHeight());

        try {
            // FLECHA 1: Verde (Primer bloque)
            if (bloquesIndicesMostrados.size() > 0 && bloquesDatosMostrados.size() > 0) {
                VBox indice1 = bloquesIndicesMostrados.get(0);
                VBox datos1 = bloquesDatosMostrados.get(0);
                if (indice1 != null && datos1 != null) {
                    dibujarFlechaDesdeBloques(gc, indice1, datos1, Color.GREEN);
                }
            }

            // FLECHA 2: Azul (Bloque medio) - SOLO si hay más de 2 bloques
            if (bloquesIndicesMostrados.size() > 2 && bloquesDatosMostrados.size() > 2) {
                int midIdx = bloquesIndicesMostrados.size() / 2;
                int midDat = bloquesDatosMostrados.size() / 2;
                VBox indice2 = bloquesIndicesMostrados.get(midIdx);
                VBox datos2 = bloquesDatosMostrados.get(midDat);
                if (indice2 != null && datos2 != null) {
                    dibujarFlechaDesdeBloques(gc, indice2, datos2, Color.BLUE);
                }
            }

            // FLECHA 3: Rojo (Último bloque) - SOLO si hay más de 1 bloque
            if (bloquesIndicesMostrados.size() > 1 && bloquesDatosMostrados.size() > 1) {
                int lastIdx = bloquesIndicesMostrados.size() - 1;
                int lastDat = bloquesDatosMostrados.size() - 1;
                VBox indice3 = bloquesIndicesMostrados.get(lastIdx);
                VBox datos3 = bloquesDatosMostrados.get(lastDat);
                if (indice3 != null && datos3 != null) {
                    dibujarFlechaDesdeBloques(gc, indice3, datos3, Color.RED);
                }
            }

        } catch (Exception e) {
            System.err.println("DEBUG - Error inesperado al dibujar flechas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void dibujarFlechaDesdeBloques(GraphicsContext gc, VBox bloqueIndice, VBox bloqueDatos, Color color) {
        try {
            Bounds boundsIndice = bloqueIndice.localToScene(bloqueIndice.getBoundsInLocal());
            Bounds boundsDatos = bloqueDatos.localToScene(bloqueDatos.getBoundsInLocal());
            Bounds canvasBounds = canvasFlechas.localToScene(canvasFlechas.getBoundsInLocal());

            if (boundsIndice == null || boundsDatos == null || boundsIndice.isEmpty() || boundsDatos.isEmpty() || canvasBounds == null) {
                return;
            }

            // Salida: lado derecho del bloque de índices
            double x1 = boundsIndice.getCenterX() + boundsIndice.getWidth() / 2 - canvasBounds.getMinX();
            double y1 = boundsIndice.getCenterY() - canvasBounds.getMinY();

            // Entrada: lado izquierdo del bloque de datos
            double x2 = boundsDatos.getCenterX() - boundsDatos.getWidth() / 2 - canvasBounds.getMinX();
            double y2 = boundsDatos.getCenterY() - canvasBounds.getMinY();

            // Validar coordenadas en rango del canvas
            if (x1 < 0 || x1 > canvasFlechas.getWidth() || y1 < 0 || y1 > canvasFlechas.getHeight()) {
                return;
            }
            if (x2 < 0 || x2 > canvasFlechas.getWidth() || y2 < 0 || y2 > canvasFlechas.getHeight()) {
                return;
            }

            gc.setStroke(color);
            gc.setLineWidth(2.5);
            gc.strokeLine(x1, y1, x2, y2);

            // Punta de flecha
            double angle = Math.atan2(y2 - y1, x2 - x1);
            double arrowSize = 10;
            gc.setFill(color);
            gc.fillPolygon(
                    new double[]{x2, x2 - arrowSize * Math.cos(angle - Math.PI / 6), x2 - arrowSize * Math.cos(angle + Math.PI / 6)},
                    new double[]{y2, y2 - arrowSize * Math.sin(angle - Math.PI / 6), y2 - arrowSize * Math.sin(angle + Math.PI / 6)},
                    3
            );

        } catch (Exception e) {
            // Silent - flecha fallida no arriba con error
        }
    }

    @FXML
    public void limpiar() {
        vboxIndices.getChildren().clear();
        vboxDatos.getChildren().clear();
        canvasFlechas.getGraphicsContext2D().clearRect(0, 0, canvasFlechas.getWidth(), canvasFlechas.getHeight());
        bloquesIndicesMostrados.clear();
        bloquesDatosMostrados.clear();
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









