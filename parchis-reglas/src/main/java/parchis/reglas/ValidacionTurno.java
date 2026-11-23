package parchis.reglas;

import parchis.interfaces.ReglasParchisException;

/**
 * Una regla que se revisa antes de ejercer un turno (abierto-cerrado:
 * una regla nueva es una clase nueva en la lista, no otro if).
 */
interface ValidacionTurno {

    void validar(int idJugador, EstadoPartida estado) throws ReglasParchisException;
}
