package parchis.datos;

/**
 * Error tecnico de la capa de datos. SQLException nunca sale del DAO:
 * se traduce a esta excepcion.
 */
public class PersistenciaException extends Exception {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
