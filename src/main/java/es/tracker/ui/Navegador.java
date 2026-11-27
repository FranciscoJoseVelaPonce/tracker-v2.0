package es.tracker.ui;

import es.tracker.ui.controlador.LayoutControlador;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * Carga las pantallas FXML con FXMLLoader y las pone en el centro de la ventana.
 * Crea los controladores pasandoles los servicios y el propio navegador por el
 * constructor, asi ningun controlador crea sus dependencias.
 */
public class Navegador {

    private static final String CARPETA_FXML = "/es/tracker/ui/fxml/";

    private final Servicios servicios;
    private final HostServices hostServices;
    private StackPane zonaCentral;

    /**
     * Crea el navegador.
     *
     * @param servicios    servicios que se pasan a los controladores
     * @param hostServices para abrir enlaces en el navegador del sistema
     */
    public Navegador(Servicios servicios, HostServices hostServices) {
        this.servicios = servicios;
        this.hostServices = hostServices;
    }

    /**
     * Carga la ventana principal (menu lateral y zona central) y muestra el listado de candidaturas.
     *
     * @return la raiz de la ventana principal
     */
    public Parent cargarLayout() {
        FXMLLoader cargador = crearCargador("layout.fxml");
        Parent raiz = cargar(cargador);
        LayoutControlador layout = cargador.getController();
        zonaCentral = layout.getZonaCentral();
        mostrar("ofertas.fxml");
        return raiz;
    }

    /**
     * Carga una pantalla y la pone en la zona central.
     *
     * @param fxml nombre del archivo FXML, por ejemplo "ofertas.fxml"
     * @param <T>  tipo del controlador de esa pantalla
     * @return el controlador de la pantalla, para pasarle datos
     */
    public <T> T mostrar(String fxml) {
        FXMLLoader cargador = crearCargador(fxml);
        zonaCentral.getChildren().setAll(cargar(cargador));
        return cargador.getController();
    }

    /**
     * Abre una pantalla como dialogo modal y espera a que se cierre.
     *
     * @param fxml     nombre del archivo FXML del dialogo
     * @param titulo   titulo de la ventana del dialogo
     * @param duenio   ventana desde la que se abre
     * @param preparar codigo para pasarle datos al controlador antes de mostrarlo
     * @param <T>      tipo del controlador del dialogo
     * @return el controlador, para consultar el resultado
     */
    public <T> T abrirDialogo(String fxml, String titulo, Window duenio, Consumer<T> preparar) {
        FXMLLoader cargador = crearCargador(fxml);
        Scene escena = new Scene(cargar(cargador));
        escena.getStylesheets().addAll(duenio.getScene().getStylesheets());
        T controlador = cargador.getController();
        preparar.accept(controlador);
        Stage dialogo = new Stage();
        dialogo.setTitle(titulo);
        dialogo.initOwner(duenio);
        dialogo.initModality(Modality.WINDOW_MODAL);
        dialogo.setResizable(false);
        dialogo.setScene(escena);
        dialogo.showAndWait();
        return controlador;
    }

    /**
     * Abre una url en el navegador del sistema.
     *
     * @param url la direccion a abrir
     */
    public void abrirEnlace(String url) {
        hostServices.showDocument(url);
    }

    private FXMLLoader crearCargador(String fxml) {
        FXMLLoader cargador = new FXMLLoader(Navegador.class.getResource(CARPETA_FXML + fxml));
        cargador.setControllerFactory(this::crearControlador);
        return cargador;
    }

    private Object crearControlador(Class<?> tipo) {
        try {
            return tipo.getConstructor(Servicios.class, Navegador.class).newInstance(servicios, this);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("El controlador " + tipo.getSimpleName()
                    + " necesita un constructor (Servicios, Navegador).", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("No se ha podido crear el controlador " + tipo.getSimpleName() + ".", e);
        }
    }

    private static Parent cargar(FXMLLoader cargador) {
        try {
            return cargador.load();
        } catch (IOException e) {
            throw new IllegalStateException("No se ha podido cargar la pantalla " + cargador.getLocation(), e);
        }
    }
}
