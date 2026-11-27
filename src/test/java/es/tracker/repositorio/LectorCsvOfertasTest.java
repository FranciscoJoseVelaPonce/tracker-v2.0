package es.tracker.repositorio;

import es.tracker.modelo.Estado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LectorCsvOfertasTest {

    @TempDir
    Path carpeta;

    private final LectorCsvOfertas lector = new LectorCsvOfertas();

    @Test
    @DisplayName("Lee las líneas buenas y apunta el número de las que están mal")
    void saltaLasLineasMalas() throws Exception {
        Path csv = carpeta.resolve("candidaturas.csv");
        Files.writeString(csv, String.join("\n",
                LectorCsvOfertas.CABECERA,
                "1;Dev Java;Indra;pendiente;15/09/2026;https://empleo.ejemplo/1;Por LinkedIn",
                "2;Sin empresa;;pendiente;15/09/2026;;",
                "3;Estado raro;Indra;contratado;15/09/2026;;",
                "",
                "4;Fecha mala;Indra;pendiente;31/02/2026;;"), StandardCharsets.UTF_8);

        LectorCsvOfertas.Lectura lectura = lector.leer(csv);

        assertEquals(List.of(new FilaCsv("Dev Java", "Indra", Estado.PENDIENTE, LocalDate.of(2026, 9, 15),
                "https://empleo.ejemplo/1", "Por LinkedIn")), lectura.filas());
        assertEquals(List.of(3, 4, 6), lectura.lineasNoValidas());
    }

    @Test
    @DisplayName("Lee el CSV de datos de prueba del proyecto sin errores")
    void leeElCsvDelProyecto() throws Exception {
        LectorCsvOfertas.Lectura lectura = lector.leer(Path.of("candidaturas.csv"));

        assertEquals(15, lectura.filas().size());
        assertEquals(List.of(), lectura.lineasNoValidas());
    }

    @Test
    @DisplayName("Leer un archivo que no existe da error")
    void archivoQueNoExiste() {
        assertThrows(NoSuchFileException.class, () -> lector.leer(carpeta.resolve("falta.csv")));
    }
}
