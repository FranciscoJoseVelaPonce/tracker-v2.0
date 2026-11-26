package es.tracker.repositorio;

import es.tracker.modelo.Estado;
import es.tracker.modelo.FiltroOferta;
import es.tracker.modelo.Oferta;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones con las candidaturas guardadas, sin decir donde se guardan.
 */
public interface RepositorioOfertas {

    /**
     * Busca las candidaturas que cumplen todos los criterios del filtro, de la mas
     * reciente a la mas antigua.
     *
     * @param filtro los criterios; los campos null no se usan
     * @return las candidaturas que cumplen el filtro, con su empresa
     */
    List<Oferta> buscar(FiltroOferta filtro);

    /**
     * Busca una candidatura por su id.
     *
     * @param id el id de la candidatura
     * @return la candidatura con su empresa, o vacio si no existe
     */
    Optional<Oferta> buscarPorId(int id);

    /**
     * Guarda una candidatura nueva.
     *
     * @param oferta la candidatura, sin id y con una empresa ya guardada
     * @return la candidatura con el id que le ha dado la base de datos
     */
    Oferta guardar(Oferta oferta);

    /**
     * Guarda los cambios de una candidatura que ya existe.
     *
     * @param oferta la candidatura con su id y los datos nuevos
     * @return true si se ha actualizado, false si no existia
     */
    boolean actualizar(Oferta oferta);

    /**
     * Cambia solo el estado de una candidatura.
     *
     * @param id     el id de la candidatura
     * @param estado el estado nuevo
     * @return true si se ha cambiado, false si no existia
     */
    boolean cambiarEstado(int id, Estado estado);

    /**
     * Borra una candidatura y sus entrevistas en una transaccion.
     *
     * @param id el id de la candidatura
     * @return true si se ha borrado, false si no existia
     */
    boolean borrar(int id);
}
