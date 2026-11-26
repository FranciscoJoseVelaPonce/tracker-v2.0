package es.tracker.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Datos para conectarse a la base de datos. No estan escritos en el codigo:
 * se leen del archivo config.properties y se pueden cambiar con variables de entorno.
 *
 * @param url      url JDBC, por ejemplo jdbc:mysql://localhost:3307/tracker
 * @param usuario  usuario de la base de datos
 * @param password contraseña del usuario
 */
public record ConfiguracionBD(String url, String usuario, String password) {

    /** Variable de entorno que, si existe, sustituye a db.url. */
    public static final String VARIABLE_URL = "TRACKER_DB_URL";
    /** Variable de entorno que, si existe, sustituye a db.user. */
    public static final String VARIABLE_USUARIO = "TRACKER_DB_USER";
    /** Variable de entorno que, si existe, sustituye a db.password. */
    public static final String VARIABLE_PASSWORD = "TRACKER_DB_PASSWORD";

    /**
     * Comprueba que haya url.
     *
     * @throws IllegalStateException si la url esta vacia
     */
    public ConfiguracionBD {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Falta la URL de la base de datos (db.url en config.properties o la variable "
                    + VARIABLE_URL + ").");
        }
        usuario = usuario == null ? "" : usuario;
        password = password == null ? "" : password;
    }

    /**
     * Lee la configuracion del archivo indicado y aplica encima las variables de entorno.
     * Si el archivo no existe solo se usan las variables de entorno.
     *
     * @param archivo el config.properties
     * @param entorno las variables de entorno (normalmente System.getenv())
     * @return la configuracion que hay que usar
     * @throws IOException si el archivo existe pero no se puede leer
     */
    public static ConfiguracionBD cargar(Path archivo, Map<String, String> entorno) throws IOException {
        Properties propiedades = new Properties();
        if (Files.exists(archivo)) {
            try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                propiedades.load(lector);
            }
        }
        return new ConfiguracionBD(
                entorno.getOrDefault(VARIABLE_URL, propiedades.getProperty("db.url")),
                entorno.getOrDefault(VARIABLE_USUARIO, propiedades.getProperty("db.user")),
                entorno.getOrDefault(VARIABLE_PASSWORD, propiedades.getProperty("db.password")));
    }
}
