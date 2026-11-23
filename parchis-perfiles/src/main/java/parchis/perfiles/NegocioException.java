package parchis.perfiles;

/**
 * Error de negocio de los perfiles (envuelve los errores tecnicos del DAO).
 */
public class NegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public NegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
