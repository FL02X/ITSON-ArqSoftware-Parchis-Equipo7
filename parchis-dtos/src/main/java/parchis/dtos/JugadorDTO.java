package parchis.dtos;

import java.io.Serializable;

/**
 * Datos de un jugador que viajan de la fachada a la presentacion.
 * Es Serializable porque en el Hito 2B viajara entre peers.
 */
public class JugadorDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nombre;
    private final String color;
    private final int casillaActual;

    public JugadorDTO(int id, String nombre, String color, int casillaActual) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.casillaActual = casillaActual;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getColor() {
        return color;
    }

    public int getCasillaActual() {
        return casillaActual;
    }
}
