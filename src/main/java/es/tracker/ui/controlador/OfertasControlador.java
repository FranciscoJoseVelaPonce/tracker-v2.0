package es.tracker.ui.controlador;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.Estado;
import es.tracker.modelo.FiltroOferta;
import es.tracker.modelo.Oferta;
import es.tracker.servicio.OfertaService;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Pantalla con el listado de candidaturas. Los filtros se pueden combinar y la
 * busqueda se hace en la base de datos cada vez que cambia alguno.
 */
public class OfertasControlador {

    private final Servicios servicios;
    private final Navegador navegador;
    private final ObservableList<Oferta> ofertas = FXCollections.observableArrayList();

    @FXML
    private Label lblResumen;
    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<Estado> cmbEstado;
    @FXML
    private ComboBox<Empresa> cmbEmpresa;
    @FXML
    private TextField txtSalarioMin;
    @FXML
    private TextField txtSalarioMax;
    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;
    @FXML
    private Label lblErrorFiltro;
    @FXML
    private TableView<Oferta> tblOfertas;
    @FXML
    private TableColumn<Oferta, Integer> colId;
    @FXML
    private TableColumn<Oferta, String> colTitulo;
    @FXML
    private TableColumn<Oferta, String> colEmpresa;
    @FXML
    private TableColumn<Oferta, Estado> colEstado;
    @FXML
    private TableColumn<Oferta, LocalDate> colFecha;
    @FXML
    private TableColumn<Oferta, Double> colSalario;
    @FXML
    private Button btnVer;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnBorrar;

    /**
     * Crea el controlador. Lo llama el Navegador al cargar el FXML.
     *
     * @param servicios servicios de la aplicacion
     * @param navegador para cambiar de pantalla
     */
    public OfertasControlador(Servicios servicios, Navegador navegador) {
        this.servicios = servicios;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        prepararTabla();
        prepararFiltros();
        cargarEmpresas();
        buscar();
    }

    private void prepararTabla() {
        colId.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().id()));
        colTitulo.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().titulo()));
        colEmpresa.setCellValueFactory(celda -> new ReadOnlyStringWrapper(celda.getValue().empresa().nombre()));
        colEstado.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().estado()));
        colEstado.setCellFactory(columna -> new CeldaEstado());
        colFecha.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().fechaPostulacion()));
        colFecha.setCellFactory(columna -> celdaTexto(Formatos::fecha));
        colSalario.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().salario()));
        colSalario.setCellFactory(columna -> celdaTexto(Formatos::salario));
        colSalario.setComparator(Comparator.nullsFirst(Comparator.naturalOrder()));

        SortedList<Oferta> ordenadas = new SortedList<>(ofertas);
        ordenadas.comparatorProperty().bind(tblOfertas.comparatorProperty());
        tblOfertas.setItems(ordenadas);
        tblOfertas.setPlaceholder(new Label("No hay candidaturas que coincidan con los filtros."));
        tblOfertas.setRowFactory(tabla -> {
            TableRow<Oferta> fila = new TableRow<>();
            fila.setOnMouseClicked(evento -> {
                if (evento.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirDetalle(fila.getItem());
                }
            });
            return fila;
        });

        var nadaSeleccionado = tblOfertas.getSelectionModel().selectedItemProperty().isNull();
        btnVer.disableProperty().bind(nadaSeleccionado);
        btnEditar.disableProperty().bind(nadaSeleccionado);
        btnBorrar.disableProperty().bind(nadaSeleccionado);
    }

    private void prepararFiltros() {
        cmbEstado.setItems(FXCollections.observableArrayList(Estado.values()));
        Formatos.textoSiVacio(cmbEstado);
        Formatos.textoSiVacio(cmbEmpresa);
        Formatos.prepararFecha(dpDesde);
        Formatos.prepararFecha(dpHasta);

        txtBuscar.textProperty().addListener((observable, antes, ahora) -> buscar());
        txtSalarioMin.textProperty().addListener((observable, antes, ahora) -> buscar());
        txtSalarioMax.textProperty().addListener((observable, antes, ahora) -> buscar());
        cmbEstado.valueProperty().addListener((observable, antes, ahora) -> buscar());
        cmbEmpresa.valueProperty().addListener((observable, antes, ahora) -> buscar());
        dpDesde.valueProperty().addListener((observable, antes, ahora) -> buscar());
        dpHasta.valueProperty().addListener((observable, antes, ahora) -> buscar());
    }

    private void cargarEmpresas() {
        try {
            cmbEmpresa.setItems(FXCollections.observableArrayList(servicios.empresas().listar()));
        } catch (RuntimeException e) {
            Dialogos.error("No se han podido cargar las empresas", e.getMessage());
        }
    }

    private void buscar() {
        FiltroOferta filtro;
        try {
            Empresa empresa = cmbEmpresa.getValue();
            filtro = new FiltroOferta(
                    txtBuscar.getText(),
                    cmbEstado.getValue(),
                    empresa == null ? null : empresa.id(),
                    Formatos.leerSalario(txtSalarioMin.getText()),
                    Formatos.leerSalario(txtSalarioMax.getText()),
                    dpDesde.getValue(),
                    dpHasta.getValue());
        } catch (IllegalArgumentException e) {
            lblErrorFiltro.setText(e.getMessage());
            return;
        }
        lblErrorFiltro.setText("");
        try {
            List<Oferta> encontradas = servicios.ofertas().buscar(filtro);
            ofertas.setAll(encontradas);
            lblResumen.setText(resumen(encontradas));
        } catch (RuntimeException e) {
            Dialogos.error("No se han podido cargar las candidaturas", e.getMessage());
        }
    }

    private static String resumen(List<Oferta> lista) {
        long pendientes = lista.stream().filter(oferta -> oferta.estado() == Estado.PENDIENTE).count();
        long enEntrevista = lista.stream().filter(oferta -> oferta.estado() == Estado.ENTREVISTA).count();
        return lista.size() + " candidaturas · " + pendientes + " pendientes · " + enEntrevista + " en entrevista";
    }

    @FXML
    private void onLimpiarFiltros() {
        txtBuscar.clear();
        txtSalarioMin.clear();
        txtSalarioMax.clear();
        cmbEstado.setValue(null);
        cmbEmpresa.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        buscar();
    }

    @FXML
    private void onNueva() {
        OfertaFormularioControlador formulario = navegador.mostrar("oferta-formulario.fxml");
        formulario.nueva();
    }

    @FXML
    private void onVer() {
        abrirDetalle(tblOfertas.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onEditar() {
        OfertaFormularioControlador formulario = navegador.mostrar("oferta-formulario.fxml");
        formulario.editar(tblOfertas.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onBorrar() {
        Oferta oferta = tblOfertas.getSelectionModel().getSelectedItem();
        if (!Dialogos.confirmar("Borrar candidatura", "¿Seguro que quieres borrar «" + oferta.titulo() + "» de "
                + oferta.empresa().nombre() + "? También se borrarán sus entrevistas.")) {
            return;
        }
        try {
            servicios.ofertas().borrar(oferta.id());
            buscar();
        } catch (RuntimeException e) {
            Dialogos.error("No se ha podido borrar la candidatura", e.getMessage());
        }
    }

    @FXML
    private void onImportar() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Importar candidaturas");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (separado por ;)", "*.csv"));
        File archivo = selector.showOpenDialog(tblOfertas.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        try {
            OfertaService.ResultadoImportacion resultado = servicios.ofertas().importar(archivo.toPath());
            String mensaje = "Candidaturas importadas: " + resultado.importadas()
                    + "\nEmpresas nuevas: " + resultado.empresasCreadas();
            if (!resultado.lineasNoValidas().isEmpty()) {
                mensaje += "\nLíneas no válidas que se han saltado: " + resultado.lineasNoValidas();
            }
            Dialogos.informacion("Importación terminada", mensaje);
            cargarEmpresas();
            buscar();
        } catch (IOException | RuntimeException e) {
            Dialogos.error("No se ha podido importar el archivo", e.getMessage());
        }
    }

    private void abrirDetalle(Oferta oferta) {
        OfertaDetalleControlador detalle = navegador.mostrar("oferta-detalle.fxml");
        detalle.mostrar(oferta.id());
    }

    private void cambiarEstado(Oferta oferta, Estado nuevo) {
        try {
            servicios.ofertas().cambiarEstado(oferta.id(), nuevo);
            ofertas.set(ofertas.indexOf(oferta), oferta.conEstado(nuevo));
            lblResumen.setText(resumen(ofertas));
        } catch (RuntimeException e) {
            Dialogos.error("No se ha podido cambiar el estado", e.getMessage());
            buscar();
        }
    }

    private static <T> TableCell<Oferta, T> celdaTexto(Function<T, String> formato) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia ? null : formato.apply(valor));
            }
        };
    }

    /**
     * Celda de la columna Estado: un desplegable con el color del estado que
     * permite cambiarlo directamente desde la tabla.
     */
    private final class CeldaEstado extends TableCell<Oferta, Estado> {

        private final ComboBox<Estado> desplegable = new ComboBox<>(FXCollections.observableArrayList(Estado.values()));
        private boolean actualizando;

        private CeldaEstado() {
            desplegable.getStyleClass().add("combo-estado");
            desplegable.setAccessibleText("Estado de la candidatura");
            desplegable.valueProperty().addListener((observable, antes, ahora) -> {
                pintar(ahora);
                if (!actualizando && ahora != null && antes != null && getTableRow().getItem() != null) {
                    cambiarEstado(getTableRow().getItem(), ahora);
                }
            });
        }

        @Override
        protected void updateItem(Estado estado, boolean vacia) {
            super.updateItem(estado, vacia);
            if (vacia || estado == null) {
                setGraphic(null);
                return;
            }
            actualizando = true;
            desplegable.setValue(estado);
            actualizando = false;
            setGraphic(desplegable);
        }

        private void pintar(Estado estado) {
            desplegable.getStyleClass().removeIf(clase -> clase.startsWith("estado-"));
            if (estado != null) {
                desplegable.getStyleClass().add("estado-" + estado.texto());
            }
        }
    }
}
