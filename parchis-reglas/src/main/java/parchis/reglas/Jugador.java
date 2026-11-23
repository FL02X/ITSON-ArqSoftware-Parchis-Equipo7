package parchis.reglas;

/**
 * Entidad interna del subsistema Reglas. Hacia afuera viaja como JugadorDTO.
 */
class Jugador {

    private final int id;
    private final String nombre;
    private final String color;
    private final boolean local;
    private int casillaActual;

    Jugador(int id, String nombre, String color, boolean local) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.local = local;
    }

    int getId() {
        return id;
    }

    String getNombre() {
        return nombre;
    }

    String getColor() {
        return color;
    }

    /** true si juega en esta computadora; false si llego por la red. */
    boolean isLocal() {
        return local;
    }

    int getCasillaActual() {
        return casillaActual;
    }

    void setCasillaActual(int casillaActual) {
        this.casillaActual = casillaActual;
    }
}
