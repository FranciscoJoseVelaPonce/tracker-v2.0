package es.tracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre conexiones con la base de datos usando la configuracion. Cada operacion
 * pide su propia conexion y la cierra con try-with-resources.
 */
public class FabricaConexiones {

    private final ConfiguracionBD configuracion;

    /**
     * Crea la fabrica.
     *
     * @param configuracion url, usuario y contraseña de la base de datos
     */
    public FabricaConexiones(ConfiguracionBD configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * Abre una conexion nueva. Quien la pide es quien la tiene que cerrar.
     *
     * @return la conexion abierta
     * @throws SQLException si no se puede conectar
     */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(configuracion.url(), configuracion.usuario(), configuracion.password());
    }
}
