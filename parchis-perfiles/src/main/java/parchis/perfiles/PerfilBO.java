package parchis.perfiles;

import parchis.datos.IPerfilDAO;
import parchis.datos.PerfilEntidad;
import parchis.datos.PersistenciaException;
import parchis.dtos.PerfilDTO;

/**
 * Objeto de negocio del perfil: aplica las reglas y usa el DAO por su interfaz.
 * No tiene SQL; convierte entre entidad y DTO y traduce errores tecnicos.
 */
public class PerfilBO {

    private final IPerfilDAO dao;

    public PerfilBO(IPerfilDAO dao) {
        this.dao = dao;
    }

    /**
     * Si el jugador ya tiene perfil en esta computadora lo carga;
     * si no, lo crea con el color que eligio.
     */
    public PerfilDTO obtenerOCrear(String nombre, String color) throws NegocioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NegocioException("El perfil necesita un nombre.", null);
        }
        try {
            PerfilEntidad guardado = dao.buscar(nombre.trim());
            if (guardado != null) {
                return convertir(guardado);
            }
            PerfilEntidad nuevo = new PerfilEntidad(nombre.trim(), color, 0, 0);
            dao.guardar(nuevo);
            return convertir(nuevo);
        } catch (PersistenciaException e) {
            throw new NegocioException("No se pudo cargar ni guardar el perfil de " + nombre + ".", e);
        }
    }

    private static PerfilDTO convertir(PerfilEntidad e) {
        return new PerfilDTO(e.getNombre(), e.getColorPreferido(), e.getPartidasJugadas(), e.getPartidasGanadas());
    }
}
