package parchis.reglas;

import parchis.interfaces.ReglasParchisException;
import parchis.interfaces.ReglasParchisException.Flujo;

/**
 * Alterno A3: no se puede tirar mientras el rival no este en la partida.
 */
class RivalPresente implements ValidacionTurno {

    @Override
    public void validar(int idJugador, EstadoPartida estado) throws ReglasParchisException {
        if (!estado.completa()) {
            throw new ReglasParchisException(Flujo.A3_FALTA_RIVAL,
                    "Tu rival todavia no se une a la partida. Espera a que se conecte.");
        }
    }
}
