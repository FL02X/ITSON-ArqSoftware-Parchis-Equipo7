package parchis.presentacion;

/**
 * Observer de la presentacion: lo implementan las vistas (TableroView).
 */
public interface ObservadorJuego {
    void actualizar(JuegoModel modelo);
}
