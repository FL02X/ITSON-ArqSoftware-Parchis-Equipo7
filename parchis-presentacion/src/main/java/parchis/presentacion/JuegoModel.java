package parchis.presentacion;

import parchis.dtos.EstadoJuegoDTO;
import parchis.dtos.JugadorDTO;
import parchis.interfaces.ObservadorReglas;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo del MVC. Ya no calcula nada: guarda la ultima fotografia del juego
 * que manda la fachada y avisa a las vistas (Observer).
 */
public class JuegoModel implements ObservadorReglas {

    private final List<ObservadorJuego> observadores = new ArrayList<ObservadorJuego>();
    private EstadoJuegoDTO estado = new EstadoJuegoDTO(
            Collections.<JugadorDTO>emptyList(), 0, 0, "Esperando a que se unan los jugadores...");

    public void agregarObservador(ObservadorJuego observador) {
        this.observadores.add(observador);
    }

    /** La fachada avisa que cambio el juego. */
    @Override
    public void juegoActualizado(EstadoJuegoDTO nuevoEstado) {
        this.estado = nuevoEstado;
        notificarObservadores();
    }

    public void notificarObservadores() {
        // Las vistas Swing solo se tocan desde el hilo de eventos.
        if (SwingUtilities.isEventDispatchThread()) {
            avisar();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    avisar();
                }
            });
        }
    }

    private void avisar() {
        for (ObservadorJuego o : new ArrayList<ObservadorJuego>(observadores)) {
            o.actualizar(this);
        }
    }

    public List<JugadorDTO> getJugadores() {
        return estado.getJugadores();
    }

    public JugadorDTO getJugador(int id) {
        return estado.getJugador(id);
    }

    /** Id del jugador en turno, o 0 si aun falta el rival. */
    public int getIdJugadorEnTurno() {
        return estado.getIdJugadorEnTurno();
    }

    public JugadorDTO getJugadorActual() {
        return estado.getJugador(estado.getIdJugadorEnTurno());
    }

    public int getUltimoDado() {
        return estado.getUltimoDado();
    }

    public String getMensajeEstado() {
        return estado.getMensajeEstado();
    }
}
