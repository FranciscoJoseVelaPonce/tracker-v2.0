package es.tracker.ui.controlador;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.Oferta;
import es.tracker.ui.Dialogos;
import es.tracker.ui.Formatos;
import es.tracker.ui.Navegador;
import es.tracker.ui.Servicios;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;


/**
 * Pantalla de detalle de una candidatura: sus datos y los de su empresa.
 */
public class OfertaDetalleControlador {

    private final Servicios servicios;
    private final Navegador navegador;
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
                + "»? También se borrarán sus entrevistas.")) {
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
}
