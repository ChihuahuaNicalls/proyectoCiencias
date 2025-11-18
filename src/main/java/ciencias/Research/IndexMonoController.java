package ciencias.Research;

import ciencias.ResearchController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;

public class IndexMonoController {

    public static class Campo {
        private String nombre;
        private boolean esClavePrimaria;

        public Campo(String nombre, boolean esClavePrimaria) {
            this.nombre = nombre;
            this.esClavePrimaria = esClavePrimaria;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public boolean isEsClavePrimaria() { return esClavePrimaria; }
        public void setEsClavePrimaria(boolean esClavePrimaria) { this.esClavePrimaria = esClavePrimaria; }
    }

    public static class Registro {
        private Map<String, String> valores;
        private int posicionBloque;
        private int posicionRegistro;

        public Registro() {
            this.valores = new HashMap<>();
        }

        public void setValor(String campo, String valor) {
            valores.put(campo, valor);
        }

        public String getValor(String campo) {
            return valores.get(campo);
        }

        public Map<String, String> getValores() { return valores; }
        public int getPosicionBloque() { return posicionBloque; }
        public void setPosicionBloque(int posicionBloque) { this.posicionBloque = posicionBloque; }
        public int getPosicionRegistro() { return posicionRegistro; }
        public void setPosicionRegistro(int posicionRegistro) { this.posicionRegistro = posicionRegistro; }
    }

    public static class BloqueDatos {
        private int numeroBloque;
        private List<Registro> registros;
        private int capacidadMaxima;

        public BloqueDatos(int numeroBloque, int capacidadMaxima) {
            this.numeroBloque = numeroBloque;
            this.capacidadMaxima = capacidadMaxima;
            this.registros = new ArrayList<>();
        }

        public boolean agregarRegistro(Registro registro) {
            if (registros.size() < capacidadMaxima) {
                registros.add(registro);
                registro.setPosicionBloque(numeroBloque);
                registro.setPosicionRegistro(registros.size() - 1);
                return true;
            }
            return false;
        }

        public int getNumeroBloque() { return numeroBloque; }
        public List<Registro> getRegistros() { return registros; }
        public int getCantidadRegistros() { return registros.size(); }
        public int getCapacidadMaxima() { return capacidadMaxima; }
    }

    public static class EstructuraDatos {
        private List<Campo> campos;
        private List<BloqueDatos> bloques;
        private int longitudRegistroBytes;
        private int tamañoBloqueBytes;
        private int registrosPorBloque;
        private int cantidadTotalRegistros;
        private int cantidadBloques;
        private String campoClavePrimaria;

        public EstructuraDatos(List<Campo> campos, int longitudRegistroBytes, 
                              int tamañoBloqueBytes, int cantidadTotalRegistros) {
            this.campos = campos;
            this.longitudRegistroBytes = longitudRegistroBytes;
            this.tamañoBloqueBytes = tamañoBloqueBytes;
            this.cantidadTotalRegistros = cantidadTotalRegistros;

            this.registrosPorBloque = tamañoBloqueBytes / longitudRegistroBytes;
            this.cantidadBloques = (int) Math.ceil((double) cantidadTotalRegistros / registrosPorBloque);

            this.bloques = new ArrayList<>();
            for (int i = 0; i < cantidadBloques; i++) {
                bloques.add(new BloqueDatos(i, registrosPorBloque));
            }

            for (Campo campo : campos) {
                if (campo.isEsClavePrimaria()) {
                    this.campoClavePrimaria = campo.getNombre();
                    break;
                }
            }
        }

        public boolean agregarRegistro(Registro registro) {
            for (BloqueDatos bloque : bloques) {
                if (bloque.agregarRegistro(registro)) {
                    return true;
                }
            }
            return false;
        }

        public int getTotalRegistrosActuales() {
            return bloques.stream().mapToInt(BloqueDatos::getCantidadRegistros).sum();
        }

        public List<Campo> getCampos() { return campos; }
        public List<BloqueDatos> getBloques() { return bloques; }
        public int getLongitudRegistroBytes() { return longitudRegistroBytes; }
        public int getTamañoBloqueBytes() { return tamañoBloqueBytes; }
        public int getRegistrosPorBloque() { return registrosPorBloque; }
        public int getCantidadTotalRegistros() { return cantidadTotalRegistros; }
        public int getCantidadBloques() { return cantidadBloques; }
        public String getCampoClavePrimaria() { return campoClavePrimaria; }
    }

    public static class EntradaIndice {
        private String valorCampoIndexacion;
        private int puntero;

        public EntradaIndice(String valorCampoIndexacion, int puntero) {
            this.valorCampoIndexacion = valorCampoIndexacion;
            this.puntero = puntero;
        }

        public String getValorCampoIndexacion() { return valorCampoIndexacion; }
        public void setValorCampoIndexacion(String valor) { this.valorCampoIndexacion = valor; }
        public int getPuntero() { return puntero; }
        public void setPuntero(int puntero) { this.puntero = puntero; }
    }

    public static class BloqueIndice {
        private int numeroBloque;
        private List<EntradaIndice> entradas;
        private int capacidadMaxima;

        public BloqueIndice(int numeroBloque, int capacidadMaxima) {
            this.numeroBloque = numeroBloque;
            this.capacidadMaxima = capacidadMaxima;
            this.entradas = new ArrayList<>();
        }

        public boolean agregarEntrada(EntradaIndice entrada) {
            if (entradas.size() < capacidadMaxima) {
                entradas.add(entrada);
                return true;
            }
            return false;
        }

        public int getNumeroBloque() { return numeroBloque; }
        public List<EntradaIndice> getEntradas() { return entradas; }
        public int getCantidadEntradas() { return entradas.size(); }
    }

    public enum TipoIndice {
        PRIMARIO,
        SECUNDARIO
    }

    public static class EstructuraIndice {
        private TipoIndice tipo;
        private String campoIndexacion;
        private int longitudCampoIndexacionBytes;
        private int longitudPunteroBytes;
        private int tamañoEntradaBytes;
        private int tamañoBloqueBytes;
        private int entradasPorBloque;
        private int cantidadRegistrosIndice;
        private int cantidadBloquesIndice;
        private List<BloqueIndice> bloques;
        private EstructuraDatos estructuraDatos;

        public EstructuraIndice(TipoIndice tipo, String campoIndexacion,
                               int longitudCampoIndexacionBytes, int longitudPunteroBytes,
                               EstructuraDatos estructuraDatos) {
            this.tipo = tipo;
            this.campoIndexacion = campoIndexacion;
            this.longitudCampoIndexacionBytes = longitudCampoIndexacionBytes;
            this.longitudPunteroBytes = longitudPunteroBytes;
            this.estructuraDatos = estructuraDatos;

            this.tamañoEntradaBytes = longitudCampoIndexacionBytes + longitudPunteroBytes;
            this.tamañoBloqueBytes = estructuraDatos.getTamañoBloqueBytes();
            this.entradasPorBloque = tamañoBloqueBytes / tamañoEntradaBytes;

            if (tipo == TipoIndice.PRIMARIO) {
                this.cantidadRegistrosIndice = estructuraDatos.getCantidadBloques();
            } else {
                this.cantidadRegistrosIndice = estructuraDatos.getCantidadTotalRegistros();
            }

            this.cantidadBloquesIndice = (int) Math.ceil((double) cantidadRegistrosIndice / entradasPorBloque);

            this.bloques = new ArrayList<>();
            for (int i = 0; i < cantidadBloquesIndice; i++) {
                bloques.add(new BloqueIndice(i, entradasPorBloque));
            }
        }

        public void generarIndices() {
            if (tipo == TipoIndice.PRIMARIO) {
                generarIndicesPrimarios();
            } else {
                generarIndicesSecundarios();
            }
        }

        private void generarIndicesPrimarios() {
            for (BloqueDatos bloque : estructuraDatos.getBloques()) {
                if (!bloque.getRegistros().isEmpty()) {
                    Registro primerRegistro = bloque.getRegistros().get(0);
                    String valorClave = primerRegistro.getValor(estructuraDatos.getCampoClavePrimaria());
                    EntradaIndice entrada = new EntradaIndice(valorClave, bloque.getNumeroBloque());
                    agregarEntrada(entrada);
                }
            }
        }

        private void generarIndicesSecundarios() {
            int posicionGlobal = 0;
            for (BloqueDatos bloque : estructuraDatos.getBloques()) {
                for (Registro registro : bloque.getRegistros()) {
                    String valorCampo = registro.getValor(campoIndexacion);
                    EntradaIndice entrada = new EntradaIndice(valorCampo, posicionGlobal);
                    agregarEntrada(entrada);
                    posicionGlobal++;
                }
            }
        }

        private boolean agregarEntrada(EntradaIndice entrada) {
            for (BloqueIndice bloque : bloques) {
                if (bloque.agregarEntrada(entrada)) {
                    return true;
                }
            }
            return false;
        }

        public TipoIndice getTipo() { return tipo; }
        public String getCampoIndexacion() { return campoIndexacion; }
        public int getTamañoEntradaBytes() { return tamañoEntradaBytes; }
        public int getEntradasPorBloque() { return entradasPorBloque; }
        public int getCantidadRegistrosIndice() { return cantidadRegistrosIndice; }
        public int getCantidadBloquesIndice() { return cantidadBloquesIndice; }
        public List<BloqueIndice> getBloques() { return bloques; }
        public int getLongitudCampoIndexacionBytes() { return longitudCampoIndexacionBytes; }
        public int getLongitudPunteroBytes() { return longitudPunteroBytes; }
    }

    @FXML private Label titleIndex;
    @FXML private ComboBox<String> cmbTipoIndice;

    @FXML private Label lblCantidadCampos;
    @FXML private TextField txtCantidadCampos;
    @FXML private Label lblLongitudRegistro;
    @FXML private TextField txtLongitudRegistro;
    @FXML private Label lblTamañoBloque;
    @FXML private TextField txtTamañoBloque;
    @FXML private Label lblCantidadRegistros;
    @FXML private TextField txtCantidadRegistros;
    @FXML private Button btnCrearEstructura;

    @FXML private Pane panelCamposVistaData;
    @FXML private Button btnAgregarRegistro;
    @FXML private Text txtRegistrosCount;

    @FXML private TextFlow flowPrimarioInfo;
    @FXML private Label lblCampoSecundario;
    @FXML private ComboBox<String> cmbCampoIndexacion;
    @FXML private Label lblLongitudCampoIndice;
    @FXML private TextField txtLongitudCampoIndice;
    @FXML private Label lblLongitudPuntero;
    @FXML private TextField txtLongitudPuntero;
    @FXML private Button btnCrearIndice;

    @FXML private Label lblResultadosHeader;
    @FXML private Text txtResultados;

    @FXML private Button btnReiniciar;
    @FXML private Button btnGuardar;

    private EstructuraDatos estructuraDatos;
    private EstructuraIndice estructuraIndice;
    private TipoIndice tipoIndiceSeleccionado;
    private ResearchController researchController;

    public void setResearchController(ResearchController researchController) {
        this.researchController = researchController;
    }

    @FXML
    public void initialize() {
        cmbTipoIndice.setItems(FXCollections.observableArrayList("PRIMARIO", "SECUNDARIO"));
    }

    @FXML
    public void onTipoIndiceChanged() {
        String tipoSeleccionado = cmbTipoIndice.getValue();
        if (tipoSeleccionado == null) {
            return;
        }
        tipoIndiceSeleccionado = TipoIndice.valueOf(tipoSeleccionado);
    }

    @FXML
    public void crearEstructuraDatos() {
        try {
            int cantidadCampos = Integer.parseInt(txtCantidadCampos.getText());
            int longitudRegistro = Integer.parseInt(txtLongitudRegistro.getText());
            int tamañoBloque = Integer.parseInt(txtTamañoBloque.getText());
            int cantidadRegistros = Integer.parseInt(txtCantidadRegistros.getText());

            if (cantidadCampos < 2) {
                mostrarError("Debe haber al menos 2 campos");
                return;
            }

            if (longitudRegistro > tamañoBloque) {
                mostrarError("Longitud de registro no puede exceder tamaño de bloque");
                return;
            }

            List<Campo> campos = solicitarNombresCampos(cantidadCampos);
            if (campos == null) return;

            estructuraDatos = new EstructuraDatos(campos, longitudRegistro, tamañoBloque, cantidadRegistros);

            crearCamposEnPanel();
            actualizarComboCampos();
            btnReiniciar.setDisable(false);
            btnGuardar.setDisable(false);

        } catch (NumberFormatException e) {
            mostrarError("Ingrese valores numéricos válidos");
        }
    }

    private List<Campo> solicitarNombresCampos(int cantidadCampos) {
        List<Campo> campos = new ArrayList<>();

        for (int i = 0; i < cantidadCampos; i++) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Campo " + (i + 1));
            dialog.setHeaderText("Nombre del campo " + (i + 1));
            dialog.setContentText("Nombre:");

            Optional<String> resultado = dialog.showAndWait();
            if (!resultado.isPresent()) return null;

            String nombre = resultado.get().trim();
            if (nombre.isEmpty()) {
                mostrarError("Campo vacío");
                return null;
            }

            boolean esPrimaria = false;
            if (i == 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Clave Primaria");
                alert.setHeaderText("¿Es este la clave primaria?");
                alert.setContentText("Campo: " + nombre);
                Optional<ButtonType> r = alert.showAndWait();
                esPrimaria = r.isPresent() && r.get() == ButtonType.OK;
            }

            campos.add(new Campo(nombre, esPrimaria));
        }

        if (campos.stream().noneMatch(Campo::isEsClavePrimaria)) {
            campos.get(0).setEsClavePrimaria(true);
        }

        return campos;
    }

    private void crearCamposEnPanel() {
        panelCamposVistaData.getChildren().clear();
        VBox vbox = new VBox();
        vbox.setSpacing(8);

        for (Campo campo : estructuraDatos.getCampos()) {
            Label label = new Label(campo.getNombre() + ":");
            TextField textField = new TextField();
            textField.setId("txt_" + campo.getNombre());
            textField.setPrefWidth(200);
            vbox.getChildren().addAll(label, textField);
        }

        panelCamposVistaData.getChildren().add(vbox);
    }

    private void actualizarComboCampos() {
        if (estructuraDatos != null) {
            List<String> campos = new ArrayList<>();
            for (Campo c : estructuraDatos.getCampos()) {
                campos.add(c.getNombre());
            }
            cmbCampoIndexacion.setItems(FXCollections.observableArrayList(campos));

            if (tipoIndiceSeleccionado == TipoIndice.PRIMARIO) {
                flowPrimarioInfo.setVisible(true);
                lblCampoSecundario.setVisible(false);
                cmbCampoIndexacion.setVisible(false);
            } else {
                flowPrimarioInfo.setVisible(false);
                lblCampoSecundario.setVisible(true);
                cmbCampoIndexacion.setVisible(true);
            }
        }
    }

    @FXML
    public void agregarRegistro() {
        if (estructuraDatos == null) {
            mostrarError("Cree estructura primero");
            return;
        }

        if (estructuraDatos.getTotalRegistrosActuales() >= estructuraDatos.getCantidadTotalRegistros()) {
            mostrarError("Capacidad máxima alcanzada");
            return;
        }

        try {
            Registro registro = new Registro();

            for (Campo campo : estructuraDatos.getCampos()) {
                TextField tf = (TextField) panelCamposVistaData.lookup("#txt_" + campo.getNombre());
                if (tf != null) {
                    String valor = tf.getText().trim();
                    if (valor.isEmpty()) {
                        mostrarError("Campo vacío: " + campo.getNombre());
                        return;
                    }
                    registro.setValor(campo.getNombre(), valor);
                }
            }

            if (estructuraDatos.agregarRegistro(registro)) {
                for (Campo campo : estructuraDatos.getCampos()) {
                    TextField tf = (TextField) panelCamposVistaData.lookup("#txt_" + campo.getNombre());
                    if (tf != null) tf.clear();
                }
                actualizarContadorRegistros();
            }

        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    private void actualizarContadorRegistros() {
        if (estructuraDatos != null) {
            int actual = estructuraDatos.getTotalRegistrosActuales();
            int total = estructuraDatos.getCantidadTotalRegistros();
            txtRegistrosCount.setText("Registros: " + actual + "/" + total);
        }
    }

    @FXML
    public void crearEstructuraIndice() {
        if (estructuraDatos == null) {
            mostrarError("Cree estructura de datos primero");
            return;
        }

        if (estructuraDatos.getTotalRegistrosActuales() == 0) {
            mostrarError("Agregue al menos un registro");
            return;
        }

        try {
            String campoIndexacion;
            if (tipoIndiceSeleccionado == TipoIndice.PRIMARIO) {
                campoIndexacion = estructuraDatos.getCampoClavePrimaria();
            } else {
                campoIndexacion = cmbCampoIndexacion.getValue();
                if (campoIndexacion == null) {
                    mostrarError("Seleccione campo de indexación");
                    return;
                }
            }

            int longitudCampo = Integer.parseInt(txtLongitudCampoIndice.getText());
            int longitudPuntero = Integer.parseInt(txtLongitudPuntero.getText());

            estructuraIndice = new EstructuraIndice(tipoIndiceSeleccionado, campoIndexacion,
                    longitudCampo, longitudPuntero, estructuraDatos);

            estructuraIndice.generarIndices();
            mostrarResultados();

        } catch (NumberFormatException e) {
            mostrarError("Valores numéricos inválidos");
        }
    }

    private void mostrarResultados() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== ESTRUCTURA DE ÍNDICES ===\n\n");
        sb.append("Tipo: ").append(estructuraIndice.getTipo()).append("\n");
        sb.append("Campo: ").append(estructuraIndice.getCampoIndexacion()).append("\n\n");

        sb.append("=== PARÁMETROS ===\n");
        sb.append("Longitud campo: ").append(estructuraIndice.getLongitudCampoIndexacionBytes()).append(" bytes\n");
        sb.append("Longitud puntero: ").append(estructuraIndice.getLongitudPunteroBytes()).append(" bytes\n");
        sb.append("Tamaño entrada: ").append(estructuraIndice.getTamañoEntradaBytes()).append(" bytes\n");
        sb.append("Entradas/bloque: ").append(estructuraIndice.getEntradasPorBloque()).append("\n");
        sb.append("Registros índice: ").append(estructuraIndice.getCantidadRegistrosIndice()).append("\n");
        sb.append("Bloques índice: ").append(estructuraIndice.getCantidadBloquesIndice()).append("\n\n");

        sb.append("=== ENTRADAS DE ÍNDICE ===\n\n");
        for (BloqueIndice bloque : estructuraIndice.getBloques()) {
            sb.append("Bloque ").append(bloque.getNumeroBloque()).append(":\n");
            for (EntradaIndice entrada : bloque.getEntradas()) {
                sb.append("  ").append(entrada.getValorCampoIndexacion());
                sb.append(" → ").append(entrada.getPuntero());
                sb.append(" (").append(tipoIndiceSeleccionado == TipoIndice.PRIMARIO ? "bloque" : "registro").append(")\n");
            }
            sb.append("\n");
        }

        txtResultados.setText(sb.toString());
    }

    @FXML
    public void reiniciar() {
        estructuraDatos = null;
        estructuraIndice = null;
        tipoIndiceSeleccionado = null;

        cmbTipoIndice.setValue(null);
        txtCantidadCampos.clear();
        txtLongitudRegistro.clear();
        txtTamañoBloque.clear();
        txtCantidadRegistros.clear();
        txtLongitudCampoIndice.clear();
        txtLongitudPuntero.clear();
        panelCamposVistaData.getChildren().clear();
        txtRegistrosCount.setText("Registros: 0/0");
        txtResultados.setText("");

        btnReiniciar.setDisable(true);
        btnGuardar.setDisable(true);
    }

    @FXML
    public void guardarResultados() {
        if (estructuraIndice == null) {
            mostrarError("No hay resultados para guardar");
            return;
        }
        mostrarInfo("Funcionalidad de guardar implementar según necesidades");
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

