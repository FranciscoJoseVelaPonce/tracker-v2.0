package es.tracker.ui.controlador;

import es.tracker.ui.Navegador;
import es.tracker.ui.Servicios;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

/**
 * Controlador de la ventana principal: el menu lateral cambia la pantalla que se
 * ve en la zona central.
 */
public class LayoutControlador {

    private final Navegador navegador;

    @FXML
    private ToggleGroup grupoMenu;
    @FXML
    private StackPane zonaCentral;

    /**
     * Crea el controlador. Lo llama el Navegador al cargar el FXML.
     *
     * @param servicios no se usan en esta pantalla
     * @param navegador para cambiar de pantalla
     */
    public LayoutControlador(Servicios servicios, Navegador navegador) {
        this.navegador = navegador;
    }

    @FXML
    private void initialize() {
        grupoMenu.selectedToggleProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo == null) {
                anterior.setSelected(true);
            }
        });
    }

    /**
     * Devuelve el panel donde se cargan las pantallas.
     *
     * @return la zona central de la ventana
     */
    public StackPane getZonaCentral() {
        return zonaCentral;
    }

    @FXML
    private void onCandidaturas() {
        navegador.mostrar("ofertas.fxml");
    }

    @FXML
    private void onEmpresas() {
        navegador.mostrar("empresas.fxml");
    }

    @FXML
    private void onEntrevistas() {
        navegador.mostrar("entrevistas.fxml");
    }
}
