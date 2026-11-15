package parchis.dtos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fotografia del juego que la fachada entrega a la presentacion.
 * Inmutable: la presentacion no puede cambiar el estado del subsistema.
 */
public class EstadoJuegoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<JugadorDTO> jugadores;
    private final int idJugadorEnTurno;
    private final int ultimoDado;
    private final String mensajeEstado;

    public EstadoJuegoDTO(List<JugadorDTO> jugadores, int idJugadorEnTurno, int ultimoDado, String mensajeEstado) {
        this.jugadores = Collections.unmodifiableList(new ArrayList<JugadorDTO>(jugadores));
        this.idJugadorEnTurno = idJugadorEnTurno;
        this.ultimoDado = ultimoDado;
        this.mensajeEstado = mensajeEstado;
    }

    public List<JugadorDTO> getJugadores() {
        return jugadores;
    }

    /** Id del jugador en turno, o 0 si la partida aun no tiene a todos sus jugadores. */
    public int getIdJugadorEnTurno() {
        return idJugadorEnTurno;
    }

    /** Ultimo valor del dado, o 0 si nadie ha tirado. */
    public int getUltimoDado() {
        return ultimoDado;
    }

    public String getMensajeEstado() {
        return mensajeEstado;
    }

    /** Busca un jugador por id; null si no esta. */
    public JugadorDTO getJugador(int id) {
        for (JugadorDTO j : jugadores) {
            if (j.getId() == id) {
                return j;
            }
        }
        return null;
    }
}
