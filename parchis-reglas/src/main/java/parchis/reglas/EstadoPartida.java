package parchis.reglas;

import parchis.dtos.EstadoJuegoDTO;
import parchis.dtos.JugadorDTO;
import parchis.interfaces.IReglasParchisFachada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Copia local del estado de la partida. En P2P cada computadora tiene la suya;
 * numeroTurno ordena los cambios para que todas las copias coincidan.
 */
class EstadoPartida {

    private final List<Jugador> jugadores = new ArrayList<Jugador>();
    private int indiceTurno;
    private int numeroTurno;
    private int ultimoDado;
    private String mensaje = "Esperando a que se unan los jugadores...";

    boolean completa() {
        return jugadores.size() == IReglasParchisFachada.JUGADORES_POR_PARTIDA;
    }

    Jugador buscar(int id) {
        for (Jugador j : jugadores) {
            if (j.getId() == id) {
                return j;
            }
        }
        return null;
    }

    /** Agrega al jugador; los turnos van por id, sin importar quien llego primero. */
    void agregar(Jugador j) {
        jugadores.add(j);
        Collections.sort(jugadores, new Comparator<Jugador>() {
            @Override
            public int compare(Jugador a, Jugador b) {
                return Integer.compare(a.getId(), b.getId());
            }
        });
        indiceTurno = 0;
        mensaje = completa()
                ? "Inicia el juego. Turno de " + jugadores.get(0).getNombre()
                : j.getNombre() + " se unio. Esperando al rival...";
    }

    Jugador enTurno() {
        return completa() ? jugadores.get(indiceTurno) : null;
    }

    /** Mueve al jugador en turno, pasa el turno y avanza numeroTurno. */
    Jugador aplicarTirada(int dado) {
        Jugador j = jugadores.get(indiceTurno);
        int nueva = j.getCasillaActual() + dado;
        if (nueva > IReglasParchisFachada.TOTAL_CASILLAS) {
            nueva -= IReglasParchisFachada.TOTAL_CASILLAS;
        }
        j.setCasillaActual(nueva);
        ultimoDado = dado;
        numeroTurno++;
        mensaje = j.getNombre() + " saco un " + dado + " y avanzo a la casilla " + nueva;
        indiceTurno = (indiceTurno + 1) % jugadores.size();
        return j;
    }

    int getNumeroTurno() {
        return numeroTurno;
    }

    int cantidadJugadores() {
        return jugadores.size();
    }

    List<Jugador> locales() {
        List<Jugador> r = new ArrayList<Jugador>();
        for (Jugador j : jugadores) {
            if (j.isLocal()) {
                r.add(j);
            }
        }
        return r;
    }

    EstadoJuegoDTO aDTO() {
        List<JugadorDTO> dtos = new ArrayList<JugadorDTO>();
        for (Jugador j : jugadores) {
            dtos.add(new JugadorDTO(j.getId(), j.getNombre(), j.getColor(), j.getCasillaActual()));
        }
        Jugador t = enTurno();
        return new EstadoJuegoDTO(dtos, t == null ? 0 : t.getId(), ultimoDado, mensaje);
    }
}
