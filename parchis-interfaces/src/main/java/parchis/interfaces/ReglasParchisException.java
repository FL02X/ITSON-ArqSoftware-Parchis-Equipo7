package parchis.interfaces;

/**
 * Error de negocio del subsistema de reglas. El flujo indica que alterno del
 * CU-01 ocurrio, para que la presentacion y las pruebas lo distingan.
 */
public class ReglasParchisException extends Exception {

    private static final long serialVersionUID = 1L;

    public enum Flujo {
        /** A2: el jugador quiso tirar fuera de su turno. */
        A2_NO_ES_SU_TURNO,
        /** A3: el rival todavia no esta en la partida. */
        A3_FALTA_RIVAL,
        /** Errores al unirse (id repetido, nombre vacio, partida llena). */
        REGLA_DE_PARTIDA
    }

    private final Flujo flujo;

    public ReglasParchisException(Flujo flujo, String mensaje) {
        super(mensaje);
        this.flujo = flujo;
    }

    public Flujo getFlujo() {
        return flujo;
    }
}
