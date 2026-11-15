package parchis.dtos;

import java.io.Serializable;

/**
 * Lo que devuelve la fachada despues de que un jugador ejerce su turno.
 */
public class ResultadoTurnoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int idJugador;
    private final int dado;
    private final int casillaAnterior;
    private final int casillaNueva;
    private final int idJugadorSiguiente;

    public ResultadoTurnoDTO(int idJugador, int dado, int casillaAnterior, int casillaNueva, int idJugadorSiguiente) {
        this.idJugador = idJugador;
        this.dado = dado;
        this.casillaAnterior = casillaAnterior;
        this.casillaNueva = casillaNueva;
        this.idJugadorSiguiente = idJugadorSiguiente;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public int getDado() {
        return dado;
    }

    public int getCasillaAnterior() {
        return casillaAnterior;
    }

    public int getCasillaNueva() {
        return casillaNueva;
    }

    public int getIdJugadorSiguiente() {
        return idJugadorSiguiente;
    }
}
