package es.tracker.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * Una candidatura a una oferta de empleo. Desde la v2.0 la empresa es una
 * referencia a {@link Empresa} y se añaden la descripcion y el salario.
 * Al crearla se comprueba que los datos obligatorios estan bien.
 *
 * @param id               numero que identifica la oferta, o null si todavia no se ha guardado
 * @param titulo           puesto al que nos presentamos
 * @param empresa          empresa que publica la oferta
 * @param estado           como va el proceso
 * @param fechaPostulacion dia en el que mandamos la candidatura
 * @param enlace           url de la oferta, puede estar vacio
 * @param descripcion      descripcion de la oferta, puede estar vacia
 * @param salario          salario anual ofrecido, o null si no se sabe
 * @param notas            notas personales, pueden estar vacias
 */
public record Oferta(
        Integer id,
        String titulo,
        Empresa empresa,
        Estado estado,
        LocalDate fechaPostulacion,
        String enlace,
        String descripcion,
        Double salario,
        String notas) {

    /** Formato de las fechas en el CSV y en la interfaz (dd/MM/yyyy). */
    public static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * Comprueba los datos antes de crear la oferta.
     *
     * @throws IllegalArgumentException si falta el titulo, la empresa, el estado o la fecha,
     *                                  o si el salario es negativo
     */
    public Oferta {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío.");
        }
        if (empresa == null) {
            throw new IllegalArgumentException("La empresa es obligatoria.");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado es obligatorio. Estados permitidos: " + Estado.textosPermitidos());
        }
        if (fechaPostulacion == null) {
            throw new IllegalArgumentException("La fecha de postulación es obligatoria.");
        }
        if (salario != null && salario < 0) {
            throw new IllegalArgumentException("El salario no puede ser negativo.");
        }
        titulo = titulo.strip();
        enlace = enlace == null ? "" : enlace.strip();
        descripcion = descripcion == null ? "" : descripcion.strip();
        notas = notas == null ? "" : notas.strip();
    }

    /**
     * Hace una copia de la oferta con otro id. Se usa al guardarla por primera vez.
     *
     * @param nuevoId el id que le ha dado la base de datos
     * @return la oferta igual pero con el id nuevo
     */
    public Oferta conId(int nuevoId) {
        return new Oferta(nuevoId, titulo, empresa, estado, fechaPostulacion, enlace, descripcion, salario, notas);
    }

    /**
     * Hace una copia de la oferta con otro estado.
     *
     * @param nuevoEstado el estado nuevo
     * @return la oferta igual pero con el estado cambiado
     */
    public Oferta conEstado(Estado nuevoEstado) {
        return new Oferta(id, titulo, empresa, nuevoEstado, fechaPostulacion, enlace, descripcion, salario, notas);
    }

    /**
     * Texto que se ve en los desplegables de la interfaz.
     *
     * @return por ejemplo "Programador Backend · Accenture"
     */
    @Override
    public String toString() {
        return titulo + " · " + empresa.nombre();
    }
}
