package es.tracker.repositorio;

import es.tracker.config.ConfiguracionBD;
import es.tracker.config.FabricaConexiones;
import es.tracker.config.InicializadorBD;

import java.util.UUID;

/**
 * Crea para cada test una base de datos H2 nueva en memoria, en modo MySQL y con
 * las tablas del mismo schema.sql que usa la aplicacion.
 */
final class BaseDatosPrueba {

    private BaseDatosPrueba() {
    }

    static FabricaConexiones nueva() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        FabricaConexiones fabrica = new FabricaConexiones(new ConfiguracionBD(url, "sa", ""));
        InicializadorBD.crearTablas(fabrica);
        return fabrica;
    }
}
