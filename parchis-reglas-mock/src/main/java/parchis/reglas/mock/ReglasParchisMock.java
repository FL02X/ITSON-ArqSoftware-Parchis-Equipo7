package parchis.reglas.mock;

import parchis.dtos.EstadoJuegoDTO;
import parchis.dtos.JugadorDTO;
import parchis.dtos.ResultadoTurnoDTO;
import parchis.interfaces.IReglasParchisFachada;
import parchis.interfaces.ObservadorReglas;
import parchis.interfaces.ReglasParchisException;
import parchis.interfaces.ReglasParchisException.Flujo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Subsistema de reglas simulado (Hito 2A). Guarda todo en memoria, tira el
 * dado con Random y falla igual que lo hara la fachada real en A2 y A3.
 *
 * <p>Nadie fuera de la fabrica debe nombrar esta clase.</p>
 */
public class ReglasParchisMock implements IReglasParchisFachada {

    private final Random random;
    private final List<Jugador> jugadores = new ArrayList<Jugador>();
    private final List<ObservadorReglas> observadores = new ArrayList<ObservadorReglas>();
    private int indiceTurnoActual;
    private int ultimoDado;
    private String mensajeEstado = "Esperando a que se unan los jugadores...";

    public ReglasParchisMock() {
        this(new Random());
    }

    /** Permite fijar la semilla en las pruebas para que el dado sea repetible. */
    public ReglasParchisMock(Random random) {
        this.random = random;
    }

    @Override
    public synchronized EstadoJuegoDTO unirseAPartida(int idJugador, String nombre, String color)
            throws ReglasParchisException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglasParchisException(Flujo.REGLA_DE_PARTIDA, "El jugador debe tener un nombre.");
        }
        if (buscar(idJugador) != null) {
            throw new ReglasParchisException(Flujo.REGLA_DE_PARTIDA, "El jugador " + idJugador + " ya esta en la partida.");
        }
        if (jugadores.size() >= JUGADORES_POR_PARTIDA) {
            throw new ReglasParchisException(Flujo.REGLA_DE_PARTIDA, "La partida ya esta llena.");
        }
        jugadores.add(new Jugador(idJugador, nombre.trim(), color));
        if (partidaCompleta()) {
            indiceTurnoActual = 0;
            mensajeEstado = "Inicia el juego. Turno de " + jugadores.get(0).getNombre();
        } else {
            mensajeEstado = nombre.trim() + " se unio. Esperando al rival...";
        }
        return notificarYDevolver();
    }

    @Override
    public synchronized ResultadoTurnoDTO ejercerTurno(int idJugador) throws ReglasParchisException {
        if (!partidaCompleta()) {
            throw new ReglasParchisException(Flujo.A3_FALTA_RIVAL,
                    "Tu rival todavia no se une a la partida. Espera a que se conecte.");
        }
        Jugador jugadorActual = jugadores.get(indiceTurnoActual);
        if (jugadorActual.getId() != idJugador) {
            throw new ReglasParchisException(Flujo.A2_NO_ES_SU_TURNO,
                    "No es tu turno. Le toca a " + jugadorActual.getNombre() + ".");
        }

        int casillaAnterior = jugadorActual.getCasillaActual();
        ultimoDado = lanzarDado();
        int nuevaPosicion = calcularNuevaPosicion(casillaAnterior, ultimoDado);
        jugadorActual.setCasillaActual(nuevaPosicion);
        mensajeEstado = jugadorActual.getNombre() + " saco un " + ultimoDado + " y avanzo a la casilla " + nuevaPosicion;

        indiceTurnoActual = (indiceTurnoActual + 1) % jugadores.size();
        int idSiguiente = jugadores.get(indiceTurnoActual).getId();

        notificarYDevolver();
        return new ResultadoTurnoDTO(idJugador, ultimoDado, casillaAnterior, nuevaPosicion, idSiguiente);
    }

    @Override
    public synchronized EstadoJuegoDTO obtenerEstado() {
        return construirEstado();
    }

    @Override
    public synchronized void agregarObservador(ObservadorReglas observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    // ------------------------------------------------- reglas (Unidad I)

    private int lanzarDado() {
        return random.nextInt(6) + 1;
    }

    private int calcularNuevaPosicion(int posicionActual, int pasos) {
        int nuevaPosicion = posicionActual + pasos;
        if (nuevaPosicion > TOTAL_CASILLAS) {
            nuevaPosicion = nuevaPosicion - TOTAL_CASILLAS;
        }
        return nuevaPosicion;
    }

    // ------------------------------------------------- privados

    private boolean partidaCompleta() {
        return jugadores.size() == JUGADORES_POR_PARTIDA;
    }

    private Jugador buscar(int id) {
        for (Jugador j : jugadores) {
            if (j.getId() == id) {
                return j;
            }
        }
        return null;
    }

    private EstadoJuegoDTO construirEstado() {
        List<JugadorDTO> dtos = new ArrayList<JugadorDTO>();
        for (Jugador j : jugadores) {
            dtos.add(new JugadorDTO(j.getId(), j.getNombre(), j.getColor(), j.getCasillaActual()));
        }
        int idEnTurno = partidaCompleta() ? jugadores.get(indiceTurnoActual).getId() : 0;
        return new EstadoJuegoDTO(dtos, idEnTurno, ultimoDado, mensajeEstado);
    }

    private EstadoJuegoDTO notificarYDevolver() {
        EstadoJuegoDTO estado = construirEstado();
        for (ObservadorReglas o : new ArrayList<ObservadorReglas>(observadores)) {
            o.juegoActualizado(estado);
        }
        return estado;
    }
}
