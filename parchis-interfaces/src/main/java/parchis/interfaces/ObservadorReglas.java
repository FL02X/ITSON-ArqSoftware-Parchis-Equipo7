package parchis.interfaces;

import parchis.dtos.EstadoJuegoDTO;

/**
 * Observer del subsistema de reglas. Lo implementa JuegoModel para enterarse
 * de cada cambio del juego, venga de esta computadora o (en 2B) del otro peer.
 */
public interface ObservadorReglas {

    void juegoActualizado(EstadoJuegoDTO estado);
}
