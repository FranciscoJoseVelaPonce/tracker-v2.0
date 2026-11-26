package es.tracker.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracionBDTest {

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("Lee la conexión de config.properties")
    void leeElArchivo() throws Exception {
        Path archivo = carpeta.resolve("config.properties");
        Files.writeString(archivo, "db.url=jdbc:mysql://localhost:3307/tracker\ndb.user=tracker\ndb.password=secreto\n",
                StandardCharsets.UTF_8);

        ConfiguracionBD configuracion = ConfiguracionBD.cargar(archivo, Map.of());

        assertEquals("jdbc:mysql://localhost:3307/tracker", configuracion.url());
        assertEquals("tracker", configuracion.usuario());
        assertEquals("secreto", configuracion.password());
    }

    @Test
    @DisplayName("Las variables de entorno tienen prioridad sobre el archivo")
    void lasVariablesDeEntornoMandan() throws Exception {
        Path archivo = carpeta.resolve("config.properties");
        Files.writeString(archivo, "db.url=jdbc:mysql://localhost:3307/tracker\ndb.user=tracker\n", StandardCharsets.UTF_8);

        ConfiguracionBD configuracion = ConfiguracionBD.cargar(archivo,
                Map.of(ConfiguracionBD.VARIABLE_USUARIO, "root", ConfiguracionBD.VARIABLE_PASSWORD, "pwd"));

        assertEquals("root", configuracion.usuario());
        assertEquals("pwd", configuracion.password());
    }

    @Test
    @DisplayName("Sin archivo ni variables de entorno avisa de que falta la URL")
    void sinConfiguracion() {
        assertThrows(IllegalStateException.class,
                () -> ConfiguracionBD.cargar(carpeta.resolve("no-existe.properties"), Map.of()));
    }
}
