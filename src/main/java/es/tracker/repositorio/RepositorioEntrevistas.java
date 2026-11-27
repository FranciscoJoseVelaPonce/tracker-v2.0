package es.tracker.repositorio;

import es.tracker.modelo.Entrevista;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones con las entrevistas guardadas, sin decir donde se guardan.
 */
public interface RepositorioEntrevistas {

    /**
     * Devuelve todas las entrevistas con su candidatura y empresa, de la mas reciente a la mas antigua.
     *
     * @return la lista de entrevistas
     */
    List<Entrevista> listar();

    /**
     * Devuelve las entrevistas de una candidatura ordenadas por fecha.
     *
     * @param idOferta el id de la candidatura
     * @return la lista de entrevistas de esa candidatura
     */
    List<Entrevista> listarDeOferta(int idOferta);

    /**
     * Busca una entrevista por su id.
     *
     * @param id el id de la entrevista
     * @return la entrevista, o vacio si no existe
     */
    Optional<Entrevista> buscarPorId(int id);

    /**
     * Guarda una entrevista nueva.
     *
     * @param entrevista la entrevista, sin id
     * @return la entrevista con el id que le ha dado la base de datos
     */
    Entrevista guardar(Entrevista entrevista);

    /**
     * Guarda los cambios de una entrevista que ya existe.
     *
     * @param entrevista la entrevista con su id y los datos nuevos
     * @return true si se ha actualizado, false si no existia
     */
    boolean actualizar(Entrevista entrevista);

    /**
     * Borra una entrevista.
     *
     * @param id el id de la entrevista
     * @return true si se ha borrado, false si no existia
     */
    boolean borrar(int id);
}
