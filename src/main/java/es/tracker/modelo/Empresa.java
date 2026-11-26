package es.tracker.modelo;

import java.time.LocalDate;

/**
 * Una empresa a la que se han enviado candidaturas. Al crearla se comprueba
 * que los datos obligatorios estan bien.
 *
 * @param id            numero que la identifica, o null si todavia no se ha guardado
 * @param nombre        nombre de la empresa
 * @param sector        sector de actividad, por ejemplo "Consultoría"
 * @param web           url de la web corporativa, puede estar vacia
 * @param fechaRegistro dia en que se registro la empresa en la aplicacion
 */
public record Empresa(Integer id, String nombre, String sector, String web, LocalDate fechaRegistro) {

    /**
     * Comprueba los datos antes de crear la empresa.
     *
     * @throws IllegalArgumentException si falta el nombre, el sector o la fecha de registro
     */
    public Empresa {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa no puede estar vacío.");
        }
        if (sector == null || sector.isBlank()) {
            throw new IllegalArgumentException("El sector de la empresa no puede estar vacío.");
        }
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("La fecha de registro es obligatoria.");
        }
        nombre = nombre.strip();
        sector = sector.strip();
        web = web == null ? "" : web.strip();
    }

    /**
     * Hace una copia de la empresa con otro id. Se usa al guardarla por primera vez.
     *
     * @param nuevoId el id que le ha dado la base de datos
     * @return la empresa igual pero con el id nuevo
     */
    public Empresa conId(int nuevoId) {
        return new Empresa(nuevoId, nombre, sector, web, fechaRegistro);
    }

    /**
     * El nombre es lo que se ve en los desplegables de la interfaz.
     *
     * @return el nombre de la empresa
     */
    @Override
    public String toString() {
        return nombre;
    }
}
