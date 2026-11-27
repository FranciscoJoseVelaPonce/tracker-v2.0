package es.tracker.servicio;

import es.tracker.modelo.Estado;
import es.tracker.modelo.FiltroOferta;
import es.tracker.modelo.Oferta;
import es.tracker.repositorio.LectorCsvOfertas;
import es.tracker.repositorio.RepositorioOfertas;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Logica de las candidaturas: buscar con filtros, crear, editar, cambiar el estado,
 * borrar e importar el CSV. No sabe nada de JDBC ni de JavaFX.
 */
public class OfertaService {

    private final RepositorioOfertas repositorio;
    private final LectorCsvOfertas lectorCsv;
    private final Clock reloj;

    /**
     * Crea el servicio.
     *
     * @param repositorio donde se guardan las candidaturas
     * @param lectorCsv   lector del CSV para importar
     * @param reloj       para saber la fecha de hoy (en los tests se pone una fija)
     */
    public OfertaService(RepositorioOfertas repositorio, LectorCsvOfertas lectorCsv, Clock reloj) {
        this.repositorio = repositorio;
        this.lectorCsv = lectorCsv;
        this.reloj = reloj;
    }

    /**
     * Busca las candidaturas que cumplen el filtro.
     *
     * @param filtro criterios de busqueda, se pueden combinar
     * @return las candidaturas que lo cumplen
     */
    public List<Oferta> buscar(FiltroOferta filtro) {
        return repositorio.buscar(filtro == null ? FiltroOferta.TODAS : filtro);
    }

    /**
     * Busca una candidatura por su id.
     *
     * @param id el id de la candidatura
     * @return la candidatura con su empresa
     * @throws NoSuchElementException si no existe
     */
    public Oferta ver(int id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> noExiste(id));
    }

    /**
     * Guarda una candidatura nueva.
     *
     * @param oferta la candidatura, sin id
     * @return la candidatura guardada con su id
     * @throws IllegalArgumentException si ya tiene id, la empresa no esta guardada,
     *                                  la fecha es futura o el enlace no es valido
     */
    public Oferta crear(Oferta oferta) {
        if (oferta.id() != null) {
            throw new IllegalArgumentException("Una candidatura nueva no puede tener id.");
        }
        validar(oferta);
        return repositorio.guardar(oferta);
    }

    /**
     * Guarda los cambios de una candidatura.
     *
     * @param oferta la candidatura con su id y los datos nuevos
     * @throws IllegalArgumentException si los datos no son validos
     * @throws NoSuchElementException   si la candidatura no existe
     */
    public void actualizar(Oferta oferta) {
        if (oferta.id() == null) {
            throw new IllegalArgumentException("Para editar una candidatura hace falta su id.");
        }
        validar(oferta);
        if (!repositorio.actualizar(oferta)) {
            throw noExiste(oferta.id());
        }
    }

    /**
     * Cambia el estado de una candidatura.
     *
     * @param id     el id de la candidatura
     * @param estado el estado nuevo
     * @throws IllegalArgumentException si el estado es null
     * @throws NoSuchElementException   si la candidatura no existe
     */
    public void cambiarEstado(int id, Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("Elige un estado.");
        }
        if (!repositorio.cambiarEstado(id, estado)) {
            throw noExiste(id);
        }
    }

    /**
     * Borra una candidatura junto con sus entrevistas.
     *
     * @param id el id de la candidatura
     * @throws NoSuchElementException si la candidatura no existe
     */
    public void borrar(int id) {
        if (!repositorio.borrar(id)) {
            throw noExiste(id);
        }
    }

    /**
     * Importa a la base de datos las candidaturas de un CSV con el formato de la v1.0.
     * Las lineas que estan mal se saltan y se avisa de cuales son. Las validas se
     * guardan todas juntas en una transaccion.
     *
     * @param ruta el CSV
     * @return cuantas candidaturas y empresas se han creado y que lineas estaban mal
     * @throws IOException si el archivo no existe o no se puede leer
     */
    public ResultadoImportacion importar(Path ruta) throws IOException {
        LectorCsvOfertas.Lectura lectura = lectorCsv.leer(ruta);
        int empresasCreadas = lectura.filas().isEmpty() ? 0 : repositorio.importar(lectura.filas(), LocalDate.now(reloj));
        return new ResultadoImportacion(lectura.filas().size(), empresasCreadas, lectura.lineasNoValidas());
    }

    private void validar(Oferta oferta) {
        if (oferta.empresa().id() == null) {
            throw new IllegalArgumentException("Guarda primero la empresa antes de usarla en una candidatura.");
        }
        if (oferta.fechaPostulacion().isAfter(LocalDate.now(reloj))) {
            throw new IllegalArgumentException("La fecha de postulación no puede ser posterior a hoy.");
        }
        Validaciones.comprobarUrl(oferta.enlace(), "El enlace de la oferta");
    }

    private static NoSuchElementException noExiste(int id) {
        return new NoSuchElementException("No existe ninguna candidatura con el ID " + id + ".");
    }

    /**
     * Lo que devuelve la importacion.
     *
     * @param importadas      numero de candidaturas que se han guardado
     * @param empresasCreadas numero de empresas nuevas que se han creado
     * @param lineasNoValidas lineas del CSV que no se han podido leer
     */
    public record ResultadoImportacion(int importadas, int empresasCreadas, List<Integer> lineasNoValidas) {
    }
}
