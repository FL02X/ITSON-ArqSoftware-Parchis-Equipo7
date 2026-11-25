package parchis.fabrica;

import parchis.datos.PerfilDAOMySQL;
import parchis.interfaces.IReglasParchisFachada;
import parchis.p2p.Peer;
import parchis.perfiles.PerfilBO;
import parchis.reglas.FachadaReglas;
import parchis.reglas.mock.ReglasParchisMock;

import java.io.IOException;

/**
 * Unico lugar que conoce las clases concretas: el subsistema Reglas (real o
 * mock), el estilo de comunicacion (Peer) y el DAO de MySQL.
 * Cambiar el mock por el real, o el estilo, es cambiar esta clase.
 */
public final class FabricaReglas {

    /** Hito 2A: true. Hito 2B: false (subsistema real con red y MySQL). */
    private static final boolean USAR_MOCKS = false;

    private static IReglasParchisFachada instancia;
    private static Peer comunicacion;
    private static String nombrePar = "local";

    private FabricaReglas() {
    }

    /**
     * Prepara la red antes de pedir la fachada. Sin llamarlo, el juego corre
     * en una sola computadora (las dos ventanas en la misma PC).
     *
     * @param hostRival null si esta computadora abre la partida (anfitrion)
     */
    public static synchronized void configurarRed(String nombre, int puertoLocal, String hostRival, int puertoRival)
            throws IOException {
        nombrePar = nombre;
        comunicacion = new Peer(nombre);
        comunicacion.iniciar(puertoLocal);
        if (hostRival != null) {
            comunicacion.conectar(hostRival, puertoRival);
        }
    }

    public static synchronized IReglasParchisFachada obtenerFachada() {
        if (instancia == null) {
            if (USAR_MOCKS) {
                instancia = new ReglasParchisMock();
            } else {
                if (comunicacion == null) {
                    comunicacion = new Peer(nombrePar); // sin red: nadie a quien enviar
                }
                instancia = new FachadaReglas(comunicacion, new PerfilBO(new PerfilDAOMySQL()), nombrePar);
            }
        }
        return instancia;
    }
}
