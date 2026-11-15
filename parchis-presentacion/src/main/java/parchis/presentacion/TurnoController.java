package parchis.presentacion;

import parchis.dtos.ResultadoTurnoDTO;
import parchis.fabrica.FabricaReglas;
import parchis.interfaces.IReglasParchisFachada;
import parchis.interfaces.ReglasParchisException;

/**
 * Controlador del CU-01 Ejercer turno. Conoce la fachada solo por su
 * interfaz y la obtiene de la fabrica; nunca nombra al mock.
 */
public class TurnoController {

    private final IReglasParchisFachada fachadaReglas;
    private final JuegoModel modelo;

    public TurnoController(JuegoModel modelo) {
        this.modelo = modelo;
        this.fachadaReglas = FabricaReglas.obtenerFachada();
        this.fachadaReglas.agregarObservador(modelo);
        this.modelo.juegoActualizado(fachadaReglas.obtenerEstado());
    }

    public void unirseAPartida(int idJugador, String nombre, String color) throws ReglasParchisException {
        fachadaReglas.unirseAPartida(idJugador, nombre, color);
    }

    /** btnEjercerTurno dispara este metodo (storyboard). */
    public ResultadoTurnoDTO ejercerTurno(int idJugador) throws ReglasParchisException {
        return fachadaReglas.ejercerTurno(idJugador);
    }
}
