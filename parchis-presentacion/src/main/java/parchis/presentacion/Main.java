package parchis.presentacion;

import parchis.fabrica.FabricaReglas;
import parchis.interfaces.ReglasParchisException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Arranca el CU-01 Ejercer turno.
 *
 * <pre>
 * Sin argumentos                       dos ventanas en esta computadora
 * solo                                 solo la ventana del Jugador 1 (para probar A3)
 * red &lt;nombre&gt; &lt;puerto&gt;                esta PC abre la partida: Jugador 1 (Rojo)
 * red &lt;nombre&gt; &lt;puerto&gt; &lt;ip:puerto&gt;   esta PC se une al anfitrion: Jugador 2 (Azul)
 * </pre>
 */
public class Main {

    public static void main(final String[] args) {
        if (args.length >= 3 && "red".equalsIgnoreCase(args[0])) {
            iniciarEnRed(args);
        } else {
            iniciarLocal(args.length > 0 && "solo".equalsIgnoreCase(args[0]));
        }
    }

    /** Una ventana por computadora; la otra computadora es el rival. */
    private static void iniciarEnRed(String[] args) {
        final String nombre = args[1];
        int puerto = Integer.parseInt(args[2]);
        String host = null;
        int puertoRival = 0;
        if (args.length >= 4) {
            String[] hp = args[3].split(":");
            host = hp[0];
            puertoRival = Integer.parseInt(hp[1]);
        }
        final boolean anfitrion = host == null;
        try {
            FabricaReglas.configurarRed(nombre, puerto, host, puertoRival);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "No se pudo iniciar la red: " + ex.getMessage(),
                    "Parchis en red", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JuegoModel modelo = new JuegoModel();
                TurnoController controlador = new TurnoController(modelo);
                int id = anfitrion ? 1 : 2;
                TableroView vista = new TableroView(id, controlador);
                vista.setTitle("Parchis en red - " + nombre + (anfitrion ? " (anfitrion)" : ""));
                modelo.agregarObservador(vista);
                unir(controlador, id, nombre, anfitrion ? "Rojo" : "Azul");
                modelo.notificarObservadores();
                vista.setLocationRelativeTo(null);
                vista.setVisible(true);
            }
        });
    }

    /** Las dos ventanas en esta computadora (como en el Hito 2A). */
    private static void iniciarLocal(final boolean solo) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JuegoModel modelo = new JuegoModel();
                TurnoController controlador = new TurnoController(modelo);

                TableroView vistaJugador1 = new TableroView(1, controlador);
                modelo.agregarObservador(vistaJugador1);
                vistaJugador1.setLocation(100, 200);

                TableroView vistaJugador2 = null;
                if (!solo) {
                    vistaJugador2 = new TableroView(2, controlador);
                    modelo.agregarObservador(vistaJugador2);
                    vistaJugador2.setLocation(600, 200);
                }

                unir(controlador, 1, "Jugador 1 (Rojo)", "Rojo");
                if (!solo) {
                    unir(controlador, 2, "Jugador 2 (Azul)", "Azul");
                }

                modelo.notificarObservadores();
                vistaJugador1.setVisible(true);
                if (vistaJugador2 != null) {
                    vistaJugador2.setVisible(true);
                }
            }
        });
    }

    private static void unir(TurnoController controlador, int id, String nombre, String color) {
        try {
            controlador.unirseAPartida(id, nombre, color);
        } catch (ReglasParchisException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "No se pudo unir a la partida",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
