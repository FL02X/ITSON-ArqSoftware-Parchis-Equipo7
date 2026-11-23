package parchis.reglas;

import parchis.interfaces.ReglasParchisException;
import parchis.interfaces.ReglasParchisException.Flujo;

/**
 * Alterno A2: solo puede tirar el jugador que tiene el turno.
 */
class TurnoDelJugador implements ValidacionTurno {

    @Override
    public void validar(int idJugador, EstadoPartida estado) throws ReglasParchisException {
        Jugador enTurno = estado.enTurno();
        if (enTurno == null || enTurno.getId() != idJugador) {
            throw new ReglasParchisException(Flujo.A2_NO_ES_SU_TURNO,
                    "No es tu turno. Le toca a " + (enTurno == null ? "otro jugador" : enTurno.getNombre()) + ".");
        }
    }
}
