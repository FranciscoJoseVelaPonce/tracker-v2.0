package es.tracker.config;

import es.tracker.repositorio.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Crea las tablas al arrancar ejecutando schema.sql. Como todas las sentencias
 * usan CREATE TABLE IF NOT EXISTS, se puede ejecutar todas las veces sin perder datos.
 */
public final class InicializadorBD {

    /** Nombre del script con la creacion del esquema, dentro de los recursos. */
    public static final String SCRIPT = "/schema.sql";

    private InicializadorBD() {
    }

    /**
     * Ejecuta el script del esquema. Todas las sentencias van en una transaccion.
     *
     * @param fabrica de donde se saca la conexion
     * @throws PersistenciaException si no se encuentra el script o falla alguna sentencia
     */
    public static void crearTablas(FabricaConexiones fabrica) {
        String script = leerScript();
        try (Connection conexion = fabrica.abrir()) {
            conexion.setAutoCommit(false);
            try (Statement sentencia = conexion.createStatement()) {
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) {
                        sentencia.execute(sql);
                    }
                }
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se han podido crear las tablas: " + e.getMessage(), e);
        }
    }

    private static String leerScript() {
        try (InputStream entrada = InicializadorBD.class.getResourceAsStream(SCRIPT)) {
            if (entrada == null) {
                throw new PersistenciaException("No se encuentra el script " + SCRIPT, null);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se ha podido leer el script " + SCRIPT, e);
        }
    }
}
