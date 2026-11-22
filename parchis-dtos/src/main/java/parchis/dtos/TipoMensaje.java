package parchis.dtos;

/**
 * Tipos de mensaje que viajan entre computadoras.
 */
public enum TipoMensaje {
    /** Texto libre (prototipo del estilo, Hito 2A). */
    TEXTO,
    /** Un jugador se une a la partida. Contenido: id|nombre|color */
    UNIRSE,
    /** Un jugador ejercio su turno. Contenido: numeroTurno|idJugador|dado */
    TURNO
}
