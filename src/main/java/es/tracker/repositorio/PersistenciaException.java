package es.tracker.repositorio;

/**
 * Error al leer o escribir en la base de datos. Envuelve la SQLException para
 * que el servicio y la interfaz no dependan de JDBC y puedan mostrar un mensaje claro.
 */
public class PersistenciaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepcion.
     *
     * @param mensaje explicacion del error para el usuario
     * @param causa   el error original, puede ser null
     */
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
