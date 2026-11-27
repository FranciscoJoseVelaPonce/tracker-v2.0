package es.tracker.servicio;

import es.tracker.modelo.Entrevista;
import es.tracker.repositorio.RepositorioEntrevistas;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Logica de las entrevistas: listar todas o las de una candidatura, crear, editar y borrar.
 */
public class EntrevistaService {

    private final RepositorioEntrevistas repositorio;

    /**
     * Crea el servicio.
     *
     * @param repositorio donde se guardan las entrevistas
     */
    public EntrevistaService(RepositorioEntrevistas repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Devuelve todas las entrevistas, de la mas reciente a la mas antigua.
     *
     * @return la lista de entrevistas
     */
    public List<Entrevista> listar() {
        return repositorio.listar();
    }

    /**
     * Devuelve las entrevistas de una candidatura.
     *
     * @param idOferta el id de la candidatura
     * @return sus entrevistas ordenadas por fecha
     */
    public List<Entrevista> deOferta(int idOferta) {
        return repositorio.listarDeOferta(idOferta);
    }

    /**
     * Guarda una entrevista nueva.
     *
     * @param entrevista la entrevista, sin id
     * @return la entrevista guardada con su id
     */
    public Entrevista crear(Entrevista entrevista) {
        if (entrevista.id() != null) {
            throw new IllegalArgumentException("Una entrevista nueva no puede tener id.");
        }
        return repositorio.guardar(entrevista);
    }

    /**
     * Guarda los cambios de una entrevista.
     *
     * @param entrevista la entrevista con su id y los datos nuevos
     * @throws NoSuchElementException si la entrevista no existe
     */
    public void actualizar(Entrevista entrevista) {
        if (entrevista.id() == null) {
            throw new IllegalArgumentException("Para editar una entrevista hace falta su id.");
        }
        if (!repositorio.actualizar(entrevista)) {
            throw noExiste(entrevista.id());
        }
    }

    /**
     * Borra una entrevista.
     *
     * @param id el id de la entrevista
     * @throws NoSuchElementException si la entrevista no existe
     */
    public void borrar(int id) {
        if (!repositorio.borrar(id)) {
            throw noExiste(id);
        }
    }

    private static NoSuchElementException noExiste(int id) {
        return new NoSuchElementException("No existe ninguna entrevista con el ID " + id + ".");
    }
}
