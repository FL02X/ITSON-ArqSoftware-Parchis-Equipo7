package parchis.datos;

/**
 * Un renglon de la tabla perfil. Solo la usan la capa de datos y PerfilBO;
 * la presentacion nunca ve una entidad (recibe PerfilDTO).
 */
public class PerfilEntidad {

    private final String nombre;
    private final String colorPreferido;
    private final int partidasJugadas;
    private final int partidasGanadas;

    public PerfilEntidad(String nombre, String colorPreferido, int partidasJugadas, int partidasGanadas) {
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
