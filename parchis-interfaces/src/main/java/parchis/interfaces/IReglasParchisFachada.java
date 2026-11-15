package parchis.interfaces;

import parchis.dtos.EstadoJuegoDTO;
import parchis.dtos.ResultadoTurnoDTO;

/**
 * Fachada del subsistema de reglas para el CU-01 Ejercer turno.
 *
 * <p>Contrato fijo: en el Hito 2B la fachada real (en red P2P) implementa esta
 * misma interfaz, asi que NO se modifica sin avisar al equipo y actualizar el
 * modelo de StarUML.</p>
 *
 * <p>Flujos del CU-01:</p>
 * <ul>
 *   <li>Principal: el jugador en turno tira el dado, su ficha avanza y el turno pasa al rival.</li>
 *   <li>A2: el jugador intenta tirar cuando no es su turno.</li>
 *   <li>A3: el jugador intenta tirar pero su rival todavia no esta en la partida.</li>
 * </ul>
 */
public interface IReglasParchisFachada {

    int JUGADORES_POR_PARTIDA = 2;
    int TOTAL_CASILLAS = 68;

    /**
     * Registra a un jugador en la partida. Cuando estan todos, empieza el jugador 1.
     *
     * @throws ReglasParchisException si el id ya esta registrado, el nombre esta
     *                                vacio o la partida ya esta llena.
     */
    EstadoJuegoDTO unirseAPartida(int idJugador, String nombre, String color) throws ReglasParchisException;

    /**
     * El jugador tira el dado y mueve su ficha.
     *
     * @throws ReglasParchisException A2 si no es su turno; A3 si falta el rival.
     */
    ResultadoTurnoDTO ejercerTurno(int idJugador) throws ReglasParchisException;

    /** Estado actual del juego. */
    EstadoJuegoDTO obtenerEstado();

    /**
     * Suscribe a quien quiera enterarse de los cambios del juego
     * (en el Hito 2B tambien llegaran los turnos del otro peer por aqui).
     */
    void agregarObservador(ObservadorReglas observador);
}
