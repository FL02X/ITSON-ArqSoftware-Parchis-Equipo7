package parchis.dtos;

import java.io.Serializable;

/**
 * Perfil del jugador guardado en esta computadora.
 */
public class PerfilDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;
    private final String colorPreferido;
    private final int partidasJugadas;
    private final int partidasGanadas;

    public PerfilDTO(String nombre, String colorPreferido, int partidasJugadas, int partidasGanadas) {
        this.nombre = nombre;
        this.colorPreferido = colorPreferido;
        this.partidasJugadas = partidasJugadas;
        this.partidasGanadas = partidasGanadas;
    }

    public String getNombre() {
        return nombre;
    }

    public String getColorPreferido() {
        return colorPreferido;
    }

    public int getPartidasJugadas() {
        return partidasJugadas;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }
}
