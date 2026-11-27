package es.tracker.repositorio;

import es.tracker.modelo.Estado;
import es.tracker.modelo.Oferta;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee el CSV de candidaturas con el formato de la v1.0
 * (id;titulo;empresa;estado;fechaPostulacion;enlace;notas). El id del archivo no se
 * usa porque al importar la base de datos da ids nuevos.
 */
public class LectorCsvOfertas {

    /** Primera linea del archivo con el nombre de las columnas. */
    public static final String CABECERA = "id;titulo;empresa;estado;fechaPostulacion;enlace;notas";
    private static final int NUMERO_CAMPOS = 7;

    /**
     * Lee el archivo. Las lineas que estan mal no paran la lectura, se apuntan aparte.
     *
     * @param ruta el archivo CSV
     * @return las filas leidas bien y los numeros de linea que estaban mal
     * @throws IOException si el archivo no existe o no se puede leer
     */
    public Lectura leer(Path ruta) throws IOException {
        if (Files.notExists(ruta)) {
            throw new NoSuchFileException(ruta.toString());
        }
        List<FilaCsv> filas = new ArrayList<>();
        List<Integer> lineasNoValidas = new ArrayList<>();
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea = lector.readLine();
            int numeroLinea = 1;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    filas.add(desdeLinea(linea));
                } catch (RuntimeException e) {
                    lineasNoValidas.add(numeroLinea);
                }
            }
        }
        return new Lectura(filas, lineasNoValidas);
    }

    private static FilaCsv desdeLinea(String linea) {
        String[] campos = linea.split(";", -1);
        if (campos.length != NUMERO_CAMPOS) {
            throw new IllegalArgumentException("Número de campos incorrecto");
        }
        return new FilaCsv(
                campos[1],
                campos[2],
                Estado.desdeTexto(campos[3]),
                LocalDate.parse(campos[4].strip(), Oferta.FORMATO_FECHA),
                campos[5],
                campos[6]);
    }

    /**
     * Resultado de leer el archivo.
     *
     * @param filas           filas leidas bien
     * @param lineasNoValidas numeros de linea que no se han podido leer
     */
    public record Lectura(List<FilaCsv> filas, List<Integer> lineasNoValidas) {
    }
}
