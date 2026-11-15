package parchis.presentacion;

import parchis.interfaces.ReglasParchisException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Arranca el CU-01 Ejercer turno sobre el mock, con las dos ventanas en esta computadora.
 *
 * <p>Con el argumento {@code solo} se abre solamente la ventana del Jugador 1,
 * para probar el alterno A3 (falta el rival).</p>
 */
public class Main {

    public static void main(final String[] args) {
        final boolean solo = args.length > 0 && "solo".equalsIgnoreCase(args[0]);

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

                try {
                    controlador.unirseAPartida(1, "Jugador 1 (Rojo)", "Rojo");
                    if (!solo) {
                        controlador.unirseAPartida(2, "Jugador 2 (Azul)", "Azul");
                    }
                } catch (ReglasParchisException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }

                modelo.notificarObservadores();
                vistaJugador1.setVisible(true);
                if (vistaJugador2 != null) {
                    vistaJugador2.setVisible(true);
                }
            }
        });
    }
}
