package es.tracker.ui.controlador;

import es.tracker.modelo.Entrevista;
import es.tracker.modelo.FiltroOferta;
import es.tracker.modelo.Oferta;
import es.tracker.modelo.ResultadoEntrevista;
import es.tracker.modelo.TipoEntrevista;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Pantalla de gestion de entrevistas: todas las entrevistas con su candidatura y
 * empresa, y un formulario al lado para darlas de alta o editarlas.
 */
public class EntrevistasControlador {

    private final Servicios servicios;
    private final Navegador navegador;
    private final ObservableList<Entrevista> entrevistas = FXCollections.observableArrayList();
    private Entrevista entrevistaEditada;

    @FXML
    private Label lblResumen;
    @FXML
    private TableView<Entrevista> tblEntrevistas;
    @FXML
    private TableColumn<Entrevista, LocalDate> colFecha;
    @FXML
    private TableColumn<Entrevista, String> colCandidatura;
    @FXML
    private TableColumn<Entrevista, String> colEmpresa;
    @FXML
    private TableColumn<Entrevista, TipoEntrevista> colTipo;
    @FXML
    private TableColumn<Entrevista, ResultadoEntrevista> colResultado;
    @FXML
    private Button btnVerCandidatura;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnBorrar;
    @FXML
    private Label lblTituloFormulario;
    @FXML
    private ComboBox<Oferta> cmbOferta;
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
     * @param navegador para abrir el detalle de una candidatura
     */
    public EntrevistasControlador(Servicios servicios, Navegador navegador) {
        this.servicios = servicios;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().fecha()));
        colFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia ? null : Formatos.fecha(fecha));
            }
        });
        colCandidatura.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().oferta().titulo()));
        colEmpresa.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().oferta().empresa().nombre()));
        colTipo.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().tipo()));
        colResultado.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().resultado()));
        colResultado.setCellFactory(columna -> new CeldaResultado<>());

        SortedList<Entrevista> ordenadas = new SortedList<>(entrevistas);
        ordenadas.comparatorProperty().bind(tblEntrevistas.comparatorProperty());
        tblEntrevistas.setItems(ordenadas);
        tblEntrevistas.setPlaceholder(new Label("Todavía no hay entrevistas."));

        var nadaSeleccionado = tblEntrevistas.getSelectionModel().selectedItemProperty().isNull();
        btnVerCandidatura.disableProperty().bind(nadaSeleccionado);
        btnEditar.disableProperty().bind(nadaSeleccionado);
        btnBorrar.disableProperty().bind(nadaSeleccionado);

        cmbTipo.setItems(FXCollections.observableArrayList(TipoEntrevista.values()));
        cmbResultado.setItems(FXCollections.observableArrayList(ResultadoEntrevista.values()));
        Formatos.textoSiVacio(cmbOferta);
        Formatos.textoSiVacio(cmbTipo);
        Formatos.prepararFecha(dpFecha);
        btnGuardar.disableProperty().bind(cmbOferta.valueProperty().isNull()
                .or(cmbTipo.valueProperty().isNull())
                .or(cmbResultado.valueProperty().isNull()));

        try {
            cmbOferta.setItems(FXCollections.observableArrayList(servicios.ofertas().buscar(FiltroOferta.TODAS)));
        } catch (RuntimeException e) {
            Dialogos.error("No se han podido cargar las candidaturas", e.getMessage());
        }
        limpiarFormulario();
        cargar();
    }

    private void cargar() {
        try {
            List<Entrevista> lista = servicios.entrevistas().listar();
            entrevistas.setAll(lista);
            long pendientes = lista.stream().filter(entrevista -> entrevista.resultado() == ResultadoEntrevista.PENDIENTE).count();
            lblResumen.setText(lista.size() + (lista.size() == 1 ? " entrevista · " : " entrevistas · ")
                    + pendientes + (pendientes == 1 ? " pendiente de resultado" : " pendientes de resultado"));
        } catch (RuntimeException e) {
            Dialogos.error("No se han podido cargar las entrevistas", e.getMessage());
        }
    }

    private void limpiarFormulario() {
        entrevistaEditada = null;
        lblTituloFormulario.setText("Nueva entrevista");
        cmbOferta.setValue(null);
        dpFecha.setValue(LocalDate.now());
        cmbTipo.setValue(null);
        cmbResultado.setValue(ResultadoEntrevista.PENDIENTE);
        txtNotas.clear();
        lblError.setText("");
    }

    @FXML
    private void onNueva() {
        tblEntrevistas.getSelectionModel().clearSelection();
        limpiarFormulario();
        cmbOferta.requestFocus();
    }

    @FXML
    private void onEditar() {
        Entrevista entrevista = tblEntrevistas.getSelectionModel().getSelectedItem();
        limpiarFormulario();
        entrevistaEditada = entrevista;
        lblTituloFormulario.setText("Editar entrevista");
        cmbOferta.setValue(cmbOferta.getItems().stream()
                .filter(oferta -> Objects.equals(oferta.id(), entrevista.oferta().id()))
                .findFirst()
                .orElse(entrevista.oferta()));
        dpFecha.setValue(entrevista.fecha());
        cmbTipo.setValue(entrevista.tipo());
        cmbResultado.setValue(entrevista.resultado());
        txtNotas.setText(entrevista.notas());
    }

    @FXML
    private void onGuardar() {
        try {
            LocalDate fecha = Formatos.leerFecha(dpFecha);
            Entrevista entrevista = new Entrevista(entrevistaEditada == null ? null : entrevistaEditada.id(),
                    cmbOferta.getValue(), fecha, cmbTipo.getValue(), txtNotas.getText(), cmbResultado.getValue());
            if (entrevistaEditada == null) {
                servicios.entrevistas().crear(entrevista);
            } else {
                servicios.entrevistas().actualizar(entrevista);
            }
            limpiarFormulario();
            cargar();
        } catch (RuntimeException e) {
            lblError.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        limpiarFormulario();
    }

    @FXML
    private void onVerCandidatura() {
        Entrevista entrevista = tblEntrevistas.getSelectionModel().getSelectedItem();
        OfertaDetalleControlador detalle = navegador.mostrar("oferta-detalle.fxml");
        detalle.mostrar(entrevista.oferta().id());
    }

    @FXML
    private void onBorrar() {
        Entrevista entrevista = tblEntrevistas.getSelectionModel().getSelectedItem();
        if (!Dialogos.confirmar("Borrar entrevista", "¿Seguro que quieres borrar la entrevista del "
                + Formatos.fecha(entrevista.fecha()) + " de «" + entrevista.oferta().titulo() + "»?")) {
            return;
        }
        try {
            servicios.entrevistas().borrar(entrevista.id());
            limpiarFormulario();
            cargar();
        } catch (RuntimeException e) {
            Dialogos.error("No se ha podido borrar la entrevista", e.getMessage());
        }
    }
}
