package es.tracker.repositorio;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.ResumenEmpresa;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones con las empresas guardadas, sin decir donde se guardan.
 */
public interface RepositorioEmpresas {

    /**
     * Devuelve todas las empresas ordenadas por nombre.
     *
     * @return la lista de empresas
     */
    List<Empresa> listar();

    /**
     * Devuelve todas las empresas con el numero de candidaturas de cada una.
     *
     * @return la lista de empresas con su numero de candidaturas
     */
    List<ResumenEmpresa> listarConNumeroDeOfertas();

    /**
     * Busca una empresa por su id.
     *
     * @param id el id de la empresa
     * @return la empresa, o vacio si no existe
     */
    Optional<Empresa> buscarPorId(int id);

    /**
     * Busca una empresa por su nombre exacto, sin mirar mayusculas.
     *
     * @param nombre el nombre de la empresa
     * @return la empresa, o vacio si no existe
     */
    Optional<Empresa> buscarPorNombre(String nombre);

    /**
     * Guarda una empresa nueva.
     *
     * @param empresa la empresa, sin id
     * @return la empresa con el id que le ha dado la base de datos
     */
    Empresa guardar(Empresa empresa);

    /**
     * Guarda los cambios de una empresa que ya existe.
     *
     * @param empresa la empresa con su id y los datos nuevos
     * @return true si se ha actualizado, false si no existia
     */
    boolean actualizar(Empresa empresa);

    /**
     * Borra una empresa junto con sus candidaturas y las entrevistas de esas
     * candidaturas, todo en una transaccion.
     *
     * @param id el id de la empresa
     * @return true si se ha borrado, false si no existia
     */
    boolean borrar(int id);
}
