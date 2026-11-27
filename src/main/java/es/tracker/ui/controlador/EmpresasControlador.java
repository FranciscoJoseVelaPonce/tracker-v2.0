package es.tracker.ui.controlador;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.ResumenEmpresa;
import es.tracker.ui.Dialogos;
import es.tracker.ui.Formatos;
import es.tracker.ui.Navegador;
import es.tracker.ui.Servicios;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.List;

/**
 * Pantalla de gestion de empresas: tabla con cuantas candidaturas tiene cada una
 * y formulario al lado para darlas de alta o editarlas.
 */
public class EmpresasControlador {

    private static final List<String> SECTORES = List.of("Tecnología", "Consultoría", "Telecomunicaciones",
            "Banca", "Seguros", "Administración pública", "Educación", "Sin especificar");

    private final Servicios servicios;
    private final ObservableList<ResumenEmpresa> empresas = FXCollections.observableArrayList();
    private Empresa empresaEditada;

    @FXML
    private Label lblResumen;
    @FXML
    private TableView<ResumenEmpresa> tblEmpresas;
    @FXML
    private TableColumn<ResumenEmpresa, String> colNombre;
    @FXML
    private TableColumn<ResumenEmpresa, String> colSector;
    @FXML
    private TableColumn<ResumenEmpresa, String> colWeb;
    @FXML
    private TableColumn<ResumenEmpresa, Integer> colOfertas;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnBorrar;
    @FXML
    private Label lblTituloFormulario;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<String> cmbSector;
    @FXML
    private TextField txtWeb;
    @FXML
    private DatePicker dpFechaRegistro;
    @FXML
    private Label lblError;
    @FXML
    private Button btnGuardar;

    /**
     * Crea el controlador. Lo llama el Navegador al cargar el FXML.
     *
     * @param servicios servicios de la aplicacion
     * @param navegador no se usa en esta pantalla
     */
    public EmpresasControlador(Servicios servicios, Navegador navegador) {
        this.servicios = servicios;
    }

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().empresa().nombre()));
        colSector.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().empresa().sector()));
        colWeb.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().empresa().web()));
        colOfertas.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().ofertas()));

        SortedList<ResumenEmpresa> ordenadas = new SortedList<>(empresas);
        ordenadas.comparatorProperty().bind(tblEmpresas.comparatorProperty());
        tblEmpresas.setItems(ordenadas);
        tblEmpresas.setPlaceholder(new Label("Todavía no hay empresas."));

        var nadaSeleccionado = tblEmpresas.getSelectionModel().selectedItemProperty().isNull();
        btnEditar.disableProperty().bind(nadaSeleccionado);
        btnBorrar.disableProperty().bind(nadaSeleccionado);

        cmbSector.setItems(FXCollections.observableArrayList(SECTORES));
        Formatos.prepararFecha(dpFechaRegistro);
        btnGuardar.disableProperty().bind(txtNombre.textProperty().isEmpty()
                .or(cmbSector.getEditor().textProperty().isEmpty()));
        txtNombre.focusedProperty().addListener((observable, antes, ahora) -> txtNombre.getStyleClass().remove("campo-error"));
        txtWeb.focusedProperty().addListener((observable, antes, ahora) -> txtWeb.getStyleClass().remove("campo-error"));

        limpiarFormulario();
        cargar();
    }

    private void cargar() {
        try {
            List<ResumenEmpresa> lista = servicios.empresas().listarConNumeroDeOfertas();
            empresas.setAll(lista);
            int candidaturas = lista.stream().mapToInt(ResumenEmpresa::ofertas).sum();
            lblResumen.setText(lista.size() + " empresas · " + candidaturas + " candidaturas enviadas");
        } catch (RuntimeException e) {
            Dialogos.error("No se han podido cargar las empresas", e.getMessage());
        }
    }

    private void limpiarFormulario() {
        empresaEditada = null;
        lblTituloFormulario.setText("Nueva empresa");
        txtNombre.clear();
        cmbSector.getEditor().clear();
        cmbSector.setValue(null);
        txtWeb.clear();
        dpFechaRegistro.setValue(LocalDate.now());
        lblError.setText("");
        txtNombre.getStyleClass().remove("campo-error");
        txtWeb.getStyleClass().remove("campo-error");
    }

    @FXML
    private void onNueva() {
        tblEmpresas.getSelectionModel().clearSelection();
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    @FXML
    private void onEditar() {
        Empresa empresa = tblEmpresas.getSelectionModel().getSelectedItem().empresa();
        limpiarFormulario();
        empresaEditada = empresa;
        lblTituloFormulario.setText("Editar empresa");
        txtNombre.setText(empresa.nombre());
        cmbSector.setValue(empresa.sector());
        cmbSector.getEditor().setText(empresa.sector());
        txtWeb.setText(empresa.web());
        dpFechaRegistro.setValue(empresa.fechaRegistro());
        txtNombre.requestFocus();
    }

    @FXML
    private void onGuardar() {
        try {
            LocalDate fecha = Formatos.leerFecha(dpFechaRegistro);
            Empresa empresa = new Empresa(empresaEditada == null ? null : empresaEditada.id(), txtNombre.getText(),
                    cmbSector.getEditor().getText(), txtWeb.getText(), fecha);
            if (empresaEditada == null) {
                servicios.empresas().crear(empresa);
            } else {
                servicios.empresas().actualizar(empresa);
            }
            limpiarFormulario();
            cargar();
        } catch (RuntimeException e) {
            String mensaje = e.getMessage() == null ? "Revisa los datos." : e.getMessage();
            if (mensaje.contains("empresa llamada") || mensaje.contains("nombre")) {
                txtNombre.getStyleClass().add("campo-error");
            } else if (mensaje.startsWith("La web")) {
                txtWeb.getStyleClass().add("campo-error");
            }
            lblError.setText(mensaje);
        }
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
    }

    @FXML
    private void onBorrar() {
        ResumenEmpresa seleccionada = tblEmpresas.getSelectionModel().getSelectedItem();
        String aviso = seleccionada.ofertas() == 0 ? ""
                : " Se borrarán también sus " + seleccionada.ofertas() + " candidaturas y las entrevistas de esas candidaturas.";
        if (!Dialogos.confirmar("Borrar empresa", "¿Seguro que quieres borrar " + seleccionada.empresa().nombre() + "?" + aviso)) {
            return;
        }
        try {
            servicios.empresas().borrar(seleccionada.empresa().id());
            limpiarFormulario();
            cargar();
        } catch (RuntimeException e) {
            Dialogos.error("No se ha podido borrar la empresa", e.getMessage());
        }
    }
}
