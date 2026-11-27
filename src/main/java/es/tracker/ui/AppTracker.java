package es.tracker.ui;

import es.tracker.config.ConfiguracionBD;
import es.tracker.config.FabricaConexiones;
import es.tracker.config.InicializadorBD;
import es.tracker.repositorio.LectorCsvOfertas;
import es.tracker.repositorio.RepositorioEmpresasJdbc;
import es.tracker.repositorio.RepositorioEntrevistasJdbc;
import es.tracker.repositorio.RepositorioOfertasJdbc;
import es.tracker.servicio.EmpresaService;
import es.tracker.servicio.EntrevistaService;
import es.tracker.servicio.OfertaService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;

/**
 * Aplicacion JavaFX. Lee la configuracion, crea las tablas si no existen, junta
 * repositorios y servicios y muestra la ventana principal.
 */
public class AppTracker extends Application {

    /** Archivo con los datos de conexion, en la carpeta desde la que se ejecuta. */
    public static final Path ARCHIVO_CONFIGURACION = Path.of("config.properties");

    /**
     * Prepara todo y abre la ventana. Si no se puede conectar con la base de datos
     * se avisa con un mensaje y se cierra la aplicacion.
     *
     * @param ventana la ventana principal que da JavaFX
     */
    @Override
    public void start(Stage ventana) {
        Servicios servicios;
        try {
            servicios = crearServicios();
        } catch (IOException | RuntimeException e) {
            Dialogos.error("No se puede conectar con la base de datos",
                    e.getMessage() + "\n\nComprueba que MySQL está arrancado y revisa config.properties.");
            Platform.exit();
            return;
        }

        Navegador navegador = new Navegador(servicios, getHostServices());
        Scene escena = new Scene(navegador.cargarLayout(), 1200, 760);
        escena.getStylesheets().add(AppTracker.class.getResource("/es/tracker/ui/css/tracker.css").toExternalForm());
        ventana.setTitle("Tracker de Candidaturas");
        ventana.setMinWidth(1000);
        ventana.setMinHeight(640);
        ventana.setScene(escena);
        ventana.show();
    }

    private static Servicios crearServicios() throws IOException {
        ConfiguracionBD configuracion = ConfiguracionBD.cargar(ARCHIVO_CONFIGURACION, System.getenv());
        FabricaConexiones fabrica = new FabricaConexiones(configuracion);
        InicializadorBD.crearTablas(fabrica);
        return new Servicios(
                new OfertaService(new RepositorioOfertasJdbc(fabrica), new LectorCsvOfertas(), Clock.systemDefaultZone()),
                new EmpresaService(new RepositorioEmpresasJdbc(fabrica)),
                new EntrevistaService(new RepositorioEntrevistasJdbc(fabrica)));
    }
}
