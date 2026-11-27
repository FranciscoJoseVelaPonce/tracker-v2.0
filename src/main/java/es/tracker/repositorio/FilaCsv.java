package es.tracker.repositorio;

import es.tracker.modelo.Estado;

import java.time.LocalDate;

/**
 * Una linea valida del CSV de candidaturas de la v1.0. La empresa viene como texto
 * y al importar se busca o se crea en la tabla de empresas.
 *
 * @param titulo           puesto
 * @param nombreEmpresa    nombre de la empresa
 * @param estado           estado de la candidatura
 * @param fechaPostulacion dia en que se envio
 * @param enlace           url de la oferta, puede estar vacio
 * @param notas            notas, pueden estar vacias
 */
public record FilaCsv(String titulo, String nombreEmpresa, Estado estado, LocalDate fechaPostulacion,
                      String enlace, String notas) {

    /**
     * Comprueba los datos de la linea.
     *
     * @throws IllegalArgumentException si falta el titulo, la empresa, el estado o la fecha
     */
    public FilaCsv {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        if (nombreEmpresa == null || nombreEmpresa.isBlank()) {
            throw new IllegalArgumentException("La empresa no puede estar vacía.");
        }
        if (estado == null || fechaPostulacion == null) {
            throw new IllegalArgumentException("El estado y la fecha son obligatorios.");
        }
        titulo = titulo.strip();
        nombreEmpresa = nombreEmpresa.strip();
        enlace = enlace == null ? "" : enlace.strip();
        notas = notas == null ? "" : notas.strip();
    }
}
