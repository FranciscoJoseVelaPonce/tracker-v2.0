package es.tracker.ui.controlador;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.Estado;
import es.tracker.modelo.Oferta;
import es.tracker.ui.Dialogos;
import es.tracker.ui.Formatos;
import es.tracker.ui.Navegador;
import es.tracker.ui.Servicios;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Formulario de alta y edicion de candidaturas. El mismo FXML sirve para las dos
 * cosas: {@link #nueva()} lo deja vacio y {@link #editar(Oferta)} lo rellena.
 */
public class OfertaFormularioControlador {

    private final Servicios servicios;
    private final Navegador navegador;
    private Oferta ofertaEditada;

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField txtTitulo;
    @FXML
    private ComboBox<Empresa> cmbEmpresa;
    @FXML
    private ComboBox<Estado> cmbEstado;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private TextField txtEnlace;
    @FXML
    private TextField txtSalario;
    @FXML
    private TextArea txtDescripcion;
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
     * @param navegador para cambiar de pantalla
     */
    public OfertaFormularioControlador(Servicios servicios, Navegador navegador) {
        this.servicios = servicios;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList(Estado.values()));
        Formatos.textoSiVacio(cmbEmpresa);
        Formatos.prepararFecha(dpFecha);
        try {
            cmbEmpresa.setItems(FXCollections.observableArrayList(servicios.empresas().listar()));
        } catch (RuntimeException e) {
            Dialogos.error("No se han podido cargar las empresas", e.getMessage());
        }
        if (cmbEmpresa.getItems().isEmpty()) {
            lblError.setText("Todavía no hay empresas. Créala primero en la pantalla Empresas.");
        }
        btnGuardar.disableProperty().bind(txtTitulo.textProperty().isEmpty()
                .or(cmbEmpresa.valueProperty().isNull())
                .or(cmbEstado.valueProperty().isNull()));
        for (Control campo : List.of(txtTitulo, cmbEmpresa, dpFecha, txtSalario, txtEnlace)) {
            campo.focusedProperty().addListener((observable, antes, ahora) -> campo.getStyleClass().remove("campo-error"));
        }
    }

    /**
     * Prepara el formulario para una candidatura nueva: estado pendiente y fecha de hoy.
     */
    public void nueva() {
        ofertaEditada = null;
        lblTitulo.setText("Nueva candidatura");
        cmbEstado.setValue(Estado.PENDIENTE);
        dpFecha.setValue(LocalDate.now());
        txtTitulo.requestFocus();
    }

    /**
     * Rellena el formulario con una candidatura para editarla.
     *
     * @param oferta la candidatura a editar
     */
    public void editar(Oferta oferta) {
        ofertaEditada = oferta;
        lblTitulo.setText("Editar candidatura");
        txtTitulo.setText(oferta.titulo());
        cmbEmpresa.setValue(cmbEmpresa.getItems().stream()
                .filter(empresa -> Objects.equals(empresa.id(), oferta.empresa().id()))
                .findFirst()
                .orElse(null));
        cmbEstado.setValue(oferta.estado());
        dpFecha.setValue(oferta.fechaPostulacion());
        txtEnlace.setText(oferta.enlace());
        txtSalario.setText(oferta.salario() == null ? "" : Formatos.salario(oferta.salario()).replace(" €", ""));
        txtDescripcion.setText(oferta.descripcion());
        txtNotas.setText(oferta.notas());
    }

    @FXML
    private void onGuardar() {
        List<String> errores = new ArrayList<>();
        LocalDate fecha = leer(dpFecha, errores, () -> Formatos.leerFecha(dpFecha));
        Double salario = leer(txtSalario, errores, () -> Formatos.leerSalario(txtSalario.getText()));
        if (fecha == null && !dpFecha.getStyleClass().contains("campo-error")) {
            marcar(dpFecha);
            errores.add("La fecha de postulación es obligatoria.");
        }
        if (txtTitulo.getText().isBlank()) {
            marcar(txtTitulo);
            errores.add("El título no puede estar vacío.");
        }
        if (!errores.isEmpty()) {
            lblError.setText(String.join("\n", errores));
            return;
        }

        try {
            Oferta oferta = new Oferta(ofertaEditada == null ? null : ofertaEditada.id(), txtTitulo.getText(),
                    cmbEmpresa.getValue(), cmbEstado.getValue(), fecha, txtEnlace.getText(),
                    txtDescripcion.getText(), salario, txtNotas.getText());
            int id;
            if (ofertaEditada == null) {
                id = servicios.ofertas().crear(oferta).id();
            } else {
                servicios.ofertas().actualizar(oferta);
                id = oferta.id();
            }
            OfertaDetalleControlador detalle = navegador.mostrar("oferta-detalle.fxml");
            detalle.mostrar(id);
        } catch (RuntimeException e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        if (ofertaEditada == null) {
            navegador.mostrar("ofertas.fxml");
        } else {
            OfertaDetalleControlador detalle = navegador.mostrar("oferta-detalle.fxml");
            detalle.mostrar(ofertaEditada.id());
        }
    }

    private <T> T leer(Control campo, List<String> errores, Supplier<T> lectura) {
        try {
            return lectura.get();
        } catch (IllegalArgumentException e) {
            marcar(campo);
            errores.add(e.getMessage());
            return null;
        }
    }

    private static void marcar(Control campo) {
        if (!campo.getStyleClass().contains("campo-error")) {
            campo.getStyleClass().add("campo-error");
        }
    }
}
