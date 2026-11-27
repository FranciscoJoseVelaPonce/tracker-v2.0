package es.tracker.servicio;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.ResumenEmpresa;
import es.tracker.repositorio.RepositorioEmpresas;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Logica de las empresas: listar, crear, editar y borrar. No permite dos empresas
 * con el mismo nombre.
 */
public class EmpresaService {

    private final RepositorioEmpresas repositorio;

    /**
     * Crea el servicio.
     *
     * @param repositorio donde se guardan las empresas
     */
    public EmpresaService(RepositorioEmpresas repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Devuelve todas las empresas ordenadas por nombre.
     *
     * @return la lista de empresas
     */
    public List<Empresa> listar() {
        return repositorio.listar();
    }

    /**
     * Devuelve todas las empresas con cuantas candidaturas tiene cada una.
     *
     * @return la lista de empresas con su numero de candidaturas
     */
    public List<ResumenEmpresa> listarConNumeroDeOfertas() {
        return repositorio.listarConNumeroDeOfertas();
    }

    /**
     * Guarda una empresa nueva.
     *
     * @param empresa la empresa, sin id
     * @return la empresa guardada con su id
     * @throws IllegalArgumentException si ya existe otra con ese nombre o la web no es valida
     */
    public Empresa crear(Empresa empresa) {
        if (empresa.id() != null) {
            throw new IllegalArgumentException("Una empresa nueva no puede tener id.");
        }
        validar(empresa);
        return repositorio.guardar(empresa);
    }

    /**
     * Guarda los cambios de una empresa.
     *
     * @param empresa la empresa con su id y los datos nuevos
     * @throws IllegalArgumentException si ya existe otra con ese nombre o la web no es valida
     * @throws NoSuchElementException   si la empresa no existe
     */
    public void actualizar(Empresa empresa) {
        if (empresa.id() == null) {
            throw new IllegalArgumentException("Para editar una empresa hace falta su id.");
        }
        validar(empresa);
        if (!repositorio.actualizar(empresa)) {
            throw noExiste(empresa.id());
        }
    }

    /**
     * Borra una empresa con todas sus candidaturas y entrevistas.
     *
     * @param id el id de la empresa
     * @throws NoSuchElementException si la empresa no existe
     */
    public void borrar(int id) {
        if (!repositorio.borrar(id)) {
            throw noExiste(id);
        }
    }

    private void validar(Empresa empresa) {
        repositorio.buscarPorNombre(empresa.nombre())
                .filter(otra -> !Objects.equals(otra.id(), empresa.id()))
                .ifPresent(otra -> {
                    throw new IllegalArgumentException("Ya existe una empresa llamada " + otra.nombre() + ".");
                });
        Validaciones.comprobarUrl(empresa.web(), "La web");
    }

    private static NoSuchElementException noExiste(int id) {
        return new NoSuchElementException("No existe ninguna empresa con el ID " + id + ".");
    }
}
