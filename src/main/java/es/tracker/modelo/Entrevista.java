package es.tracker.modelo;

import java.time.LocalDate;

/**
 * Una entrevista de una candidatura. Al crearla se comprueba que los datos
 * obligatorios estan bien.
 *
 * @param id        numero que la identifica, o null si todavia no se ha guardado
 * @param oferta    candidatura a la que pertenece
 * @param fecha     dia de la entrevista
 * @param tipo      telefonica, tecnica, presencial u online
 * @param notas     notas sobre la entrevista, pueden estar vacias
 * @param resultado pendiente, positivo o negativo
 */
public record Entrevista(Integer id, Oferta oferta, LocalDate fecha, TipoEntrevista tipo,
                         String notas, ResultadoEntrevista resultado) {

    /**
     * Comprueba los datos antes de crear la entrevista.
     *
     * @throws IllegalArgumentException si falta la oferta, la fecha, el tipo o el resultado
     */
    public Entrevista {
        if (oferta == null || oferta.id() == null) {
            throw new IllegalArgumentException("La entrevista tiene que pertenecer a una candidatura guardada.");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de la entrevista es obligatoria.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de entrevista es obligatorio.");
        }
        if (resultado == null) {
            throw new IllegalArgumentException("El resultado de la entrevista es obligatorio.");
        }
        notas = notas == null ? "" : notas.strip();
    }

    /**
     * Hace una copia de la entrevista con otro id. Se usa al guardarla por primera vez.
     *
     * @param nuevoId el id que le ha dado la base de datos
     * @return la entrevista igual pero con el id nuevo
     */
    public Entrevista conId(int nuevoId) {
        return new Entrevista(nuevoId, oferta, fecha, tipo, notas, resultado);
    }
}
