package es.tracker.ui.controlador;

import es.tracker.modelo.Entrevista;
import es.tracker.modelo.Oferta;
import es.tracker.modelo.ResultadoEntrevista;
import es.tracker.modelo.TipoEntrevista;
import es.tracker.ui.Formatos;
import es.tracker.ui.Navegador;
import es.tracker.ui.Servicios;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Dialogo para añadir o editar una entrevista desde el detalle de una candidatura.
 */
public class EntrevistaDialogoControlador {

    private final Servicios servicios;
    private Oferta oferta;
    private Entrevista entrevistaEditada;
    private boolean guardada;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblOferta;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private ComboBox<TipoEntrevista> cmbTipo;
    @FXML
    private ComboBox<ResultadoEntrevista> cmbResultado;
    @FXML
    private TextArea txtNotas;
    @FXML
    private Label lblError;
    @FXML
    private Button btnGuardar;

    /**
     * Crea el controlador. Lo llama el Navegador al cargar el FXML.
     *
     * @param servicios servicios de la aplicacion
     * @param navegador no se usa en este dialogo
     */
    public EntrevistaDialogoControlador(Servicios servicios, Navegador navegador) {
        this.servicios = servicios;
    }

    @FXML
    private void initialize() {
        cmbTipo.setItems(FXCollections.observableArrayList(TipoEntrevista.values()));
        cmbResultado.setItems(FXCollections.observableArrayList(ResultadoEntrevista.values()));
        Formatos.textoSiVacio(cmbTipo);
        Formatos.prepararFecha(dpFecha);
        btnGuardar.disableProperty().bind(cmbTipo.valueProperty().isNull().or(cmbResultado.valueProperty().isNull()));
    }

    /**
     * Prepara el dialogo antes de mostrarlo.
     *
     * @param oferta     candidatura a la que pertenece la entrevista
     * @param entrevista entrevista a editar, o null para crear una nueva
     */
    public void preparar(Oferta oferta, Entrevista entrevista) {
        this.oferta = oferta;
        this.entrevistaEditada = entrevista;
        lblOferta.setText(oferta.toString());
        if (entrevista == null) {
            lblTitulo.setText("Nueva entrevista");
            dpFecha.setValue(LocalDate.now());
            cmbResultado.setValue(ResultadoEntrevista.PENDIENTE);
        } else {
            lblTitulo.setText("Editar entrevista");
            dpFecha.setValue(entrevista.fecha());
            cmbTipo.setValue(entrevista.tipo());
            cmbResultado.setValue(entrevista.resultado());
            txtNotas.setText(entrevista.notas());
        }
    }

    /**
     * Dice si el usuario ha guardado la entrevista o ha cancelado.
     *
     * @return true si se ha guardado
     */
    public boolean isGuardada() {
        return guardada;
    }

    @FXML
    private void onGuardar() {
        try {
            Entrevista entrevista = new Entrevista(entrevistaEditada == null ? null : entrevistaEditada.id(), oferta,
                    Formatos.leerFecha(dpFecha), cmbTipo.getValue(), txtNotas.getText(), cmbResultado.getValue());
            if (entrevistaEditada == null) {
                servicios.entrevistas().crear(entrevista);
            } else {
                servicios.entrevistas().actualizar(entrevista);
            }
            guardada = true;
            cerrar();
        } catch (RuntimeException e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) btnGuardar.getScene().getWindow()).close();
    }
}
