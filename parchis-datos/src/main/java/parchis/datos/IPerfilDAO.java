package parchis.datos;

/**
 * Acceso a la tabla perfil. PerfilBO solo conoce esta interfaz.
 */
public interface IPerfilDAO {

    /** El perfil con ese nombre, o null si no existe en esta computadora. */
    PerfilEntidad buscar(String nombre) throws PersistenciaException;

    /** Guarda un perfil nuevo. */
    void guardar(PerfilEntidad perfil) throws PersistenciaException;
}
