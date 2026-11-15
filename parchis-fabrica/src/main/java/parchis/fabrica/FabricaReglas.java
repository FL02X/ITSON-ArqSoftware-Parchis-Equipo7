package parchis.fabrica;

import parchis.interfaces.IReglasParchisFachada;
import parchis.reglas.mock.ReglasParchisMock;

/**
 * Unico lugar que conoce la implementacion concreta del subsistema de reglas.
 *
 * <p>Hito 2B: cambiar SOLO esta clase (y la dependencia del pom) para que
 * devuelva la fachada real en lugar de ReglasParchisMock. Ni TurnoController,
 * ni JuegoModel, ni las vistas se tocan.</p>
 */
public final class FabricaReglas {

    private static IReglasParchisFachada instancia;

    private FabricaReglas() {
    }

    public static synchronized IReglasParchisFachada obtenerFachada() {
        if (instancia == null) {
            instancia = new ReglasParchisMock();
        }
        return instancia;
    }
}
