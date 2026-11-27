package es.tracker.ui.controlador;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.Entrevista;
import es.tracker.modelo.Oferta;
import es.tracker.modelo.ResultadoEntrevista;
import es.tracker.modelo.TipoEntrevista;
import es.tracker.ui.Dialogos;
import es.tracker.ui.Formatos;
import es.tracker.ui.Navegador;
import es.tracker.ui.Servicios;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;

import java.time.LocalDate;
import java.util.List;

/**
 * Pantalla de detalle de una candidatura: sus datos, la empresa y sus entrevistas
 * en un panel desplegable.
 */
public class OfertaDetalleControlador {

    private final Servicios servicios;
    private final Navegador navegador;
    private final ObservableList<Entrevista> entrevistas = FXCollections.observableArrayList();
    private Oferta oferta;

    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblSubtitulo;
    @FXML
    private Label lblEstado;
    @FXML
    private Label lblPuesto;
    @FXML
    private Label lblEstadoTexto;
    @FXML
    private Label lblFecha;
    @FXML
    private Hyperlink lnkEnlace;
    @FXML
    private Label lblSalario;
    @FXML
    private Label lblDescripcion;
    @FXML
    private Label lblNotas;
    @FXML
    private Label lblEmpresa;
    @FXML
    private Label lblSector;
    @FXML
    private Hyperlink lnkWeb;
    @FXML
    private Label lblRegistrada;
    @FXML
    private TitledPane panelEntrevistas;
    @FXML
    private TableView<Entrevista> tblEntrevistas;
    @FXML
    private TableColumn<Entrevista, LocalDate> colEntFecha;
    @FXML
    private TableColumn<Entrevista, TipoEntrevista> colEntTipo;
    @FXML
    private TableColumn<Entrevista, ResultadoEntrevista> colEntResultado;
    @FXML
    private Button btnEditarEntrevista;
    @FXML
    private Button btnBorrarEntrevista;

    /**
     * Crea el controlador. Lo llama el Navegador al cargar el FXML.
     *
     * @param servicios servicios de la aplicacion
     * @param navegador para cambiar de pantalla
     */
    public OfertaDetalleControlador(Servicios servicios, Navegador navegador) {
        this.servicios = servicios;
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        colEntFecha.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().fecha()));
        colEntFecha.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia ? null : Formatos.fecha(fecha));
            }
        });
        colEntTipo.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().tipo()));
        colEntResultado.setCellValueFactory(celda -> new ReadOnlyObjectWrapper<>(celda.getValue().resultado()));
        colEntResultado.setCellFactory(columna -> new CeldaResultado<>());
        tblEntrevistas.setItems(entrevistas);
        tblEntrevistas.setPlaceholder(new Label("Todavía no hay entrevistas."));

        var nadaSeleccionado = tblEntrevistas.getSelectionModel().selectedItemProperty().isNull();
        btnEditarEntrevista.disableProperty().bind(nadaSeleccionado);
        btnBorrarEntrevista.disableProperty().bind(nadaSeleccionado);
    }

    /**
     * Carga y muestra una candidatura. Si ya no existe se vuelve al listado.
     *
     * @param idOferta el id de la candidatura
     */
    public void mostrar(int idOferta) {
        try {
            oferta = servicios.ofertas().ver(idOferta);
        } catch (RuntimeException e) {
            Dialogos.error("No se puede mostrar la candidatura", e.getMessage());
            navegador.mostrar("ofertas.fxml");
            return;
        }
        Empresa empresa = oferta.empresa();
        lblTitulo.setText(oferta.titulo());
        lblSubtitulo.setText(empresa.nombre() + " · enviada el " + Formatos.fecha(oferta.fechaPostulacion()));
        lblEstado.setText(oferta.estado().toString());
        lblEstado.getStyleClass().add("estado-" + oferta.estado().texto());
        lblPuesto.setText(oferta.titulo());
        lblEstadoTexto.setText(oferta.estado().toString());
        lblFecha.setText(Formatos.fecha(oferta.fechaPostulacion()));
        mostrarEnlace(lnkEnlace, oferta.enlace());
        lblSalario.setText(oferta.salario() == null ? "No indicado" : Formatos.salario(oferta.salario()) + " / año");
        lblDescripcion.setText(oferta.descripcion().isEmpty() ? "Sin descripción." : oferta.descripcion());
        lblNotas.setText(oferta.notas().isEmpty() ? "Sin notas." : oferta.notas());
        lblEmpresa.setText(empresa.nombre());
        lblSector.setText(empresa.sector());
        mostrarEnlace(lnkWeb, empresa.web());
        lblRegistrada.setText(Formatos.fecha(empresa.fechaRegistro()));
        cargarEntrevistas();
    }

    private void cargarEntrevistas() {
        try {
            List<Entrevista> deLaOferta = servicios.entrevistas().deOferta(oferta.id());
            entrevistas.setAll(deLaOferta);
            panelEntrevistas.setText("Entrevistas (" + deLaOferta.size() + ")");
        } catch (RuntimeException e) {
            Dialogos.error("No se han podido cargar las entrevistas", e.getMessage());
        }
    }

    private static void mostrarEnlace(Hyperlink enlace, String url) {
        enlace.setText(url.isEmpty() ? "—" : url);
        enlace.setDisable(url.isEmpty());
    }

    @FXML
    private void onVolver() {
        navegador.mostrar("ofertas.fxml");
    }

    @FXML
    private void onEditar() {
        OfertaFormularioControlador formulario = navegador.mostrar("oferta-formulario.fxml");
        formulario.editar(oferta);
    }

    @FXML
    private void onBorrar() {
        if (!Dialogos.confirmar("Borrar candidatura", "¿Seguro que quieres borrar «" + oferta.titulo()
                + "»? También se borrarán sus " + entrevistas.size() + " entrevistas.")) {
            return;
        }
        try {
            servicios.ofertas().borrar(oferta.id());
            navegador.mostrar("ofertas.fxml");
        } catch (RuntimeException e) {
            Dialogos.error("No se ha podido borrar la candidatura", e.getMessage());
        }
    }

    @FXML
    private void onAbrirEnlace() {
        navegador.abrirEnlace(oferta.enlace());
    }

    @FXML
    private void onAbrirWeb() {
        navegador.abrirEnlace(oferta.empresa().web());
    }

    @FXML
    private void onAnadirEntrevista() {
        abrirDialogoEntrevista(null);
    }

    @FXML
    private void onEditarEntrevista() {
        abrirDialogoEntrevista(tblEntrevistas.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void onBorrarEntrevista() {
        Entrevista entrevista = tblEntrevistas.getSelectionModel().getSelectedItem();
        if (!Dialogos.confirmar("Borrar entrevista", "¿Seguro que quieres borrar la entrevista del "
                + Formatos.fecha(entrevista.fecha()) + "?")) {
            return;
        }
        try {
            servicios.entrevistas().borrar(entrevista.id());
            cargarEntrevistas();
        } catch (RuntimeException e) {
            Dialogos.error("No se ha podido borrar la entrevista", e.getMessage());
        }
    }

    private void abrirDialogoEntrevista(Entrevista entrevista) {
        EntrevistaDialogoControlador dialogo = navegador.abrirDialogo("entrevista-dialogo.fxml",
                entrevista == null ? "Nueva entrevista" : "Editar entrevista",
                tblEntrevistas.getScene().getWindow(),
                controlador -> controlador.preparar(oferta, entrevista));
        if (dialogo.isGuardada()) {
            cargarEntrevistas();
        }
    }
}
