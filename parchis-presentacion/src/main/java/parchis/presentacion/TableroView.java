package parchis.presentacion;

import parchis.dtos.JugadorDTO;
import parchis.interfaces.ReglasParchisException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Pantalla de cada jugador (storyboard "Tirar dado"). Los componentes son
 * los mismos del storyboard: lblTitulo, lblEstadoTurno, lblUltimoDado,
 * lblInfoJugador1, lblInfoJugador2 y btnEjercerTurno.
 */
public class TableroView extends JFrame implements ObservadorJuego {

    private static final long serialVersionUID = 1L;

    private final int idJugadorPropietario;
    private final TurnoController controlador;

    private final JLabel lblTitulo;
    private final JLabel lblEstadoTurno;
    private final JLabel lblUltimoDado;
    private final JLabel lblInfoJugador1;
    private final JLabel lblInfoJugador2;
    private final JButton btnEjercerTurno;

    public TableroView(int idJugadorPropietario, TurnoController controlador) {
        this.idJugadorPropietario = idJugadorPropietario;
        this.controlador = controlador;

        setTitle("Parchis - Ventana Jugador " + idJugadorPropietario);
        setSize(460, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelCentro = new JPanel(new GridLayout(5, 1));
        this.lblTitulo = new JLabel("VISTA DEL JUGADOR " + idJugadorPropietario, JLabel.CENTER);
        this.lblEstadoTurno = new JLabel("Estado: Iniciando...", JLabel.CENTER);
        this.lblUltimoDado = new JLabel("Ultimo Dado: -", JLabel.CENTER);
        this.lblInfoJugador1 = new JLabel("Jugador 1: sin conectar", JLabel.CENTER);
        this.lblInfoJugador2 = new JLabel("Jugador 2: sin conectar", JLabel.CENTER);

        panelCentro.add(this.lblTitulo);
        panelCentro.add(this.lblEstadoTurno);
        panelCentro.add(this.lblUltimoDado);
        panelCentro.add(this.lblInfoJugador1);
        panelCentro.add(this.lblInfoJugador2);

        this.btnEjercerTurno = new JButton("Lanzar Dado y Mover");
        this.btnEjercerTurno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ejercerTurno();
            }
        });

        add(panelCentro, BorderLayout.CENTER);
        add(this.btnEjercerTurno, BorderLayout.SOUTH);
    }

    private void ejercerTurno() {
        try {
            controlador.ejercerTurno(idJugadorPropietario);
            // No hace falta pintar aqui: la fachada avisa al modelo y el modelo a las dos vistas.
        } catch (ReglasParchisException ex) {
            // Alternos A2 (no es tu turno) y A3 (falta el rival).
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No puedes tirar", JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void actualizar(JuegoModel modelo) {
        this.lblInfoJugador1.setText(textoJugador(modelo.getJugador(1), 1));
        this.lblInfoJugador2.setText(textoJugador(modelo.getJugador(2), 2));

        if (modelo.getUltimoDado() > 0) {
            this.lblUltimoDado.setText("Ultimo Dado: " + modelo.getUltimoDado() + " (" + modelo.getMensajeEstado() + ")");
        }

        JugadorDTO enTurno = modelo.getJugadorActual();
        if (enTurno == null) {
            this.lblEstadoTurno.setText("Esperando a que se conecte el rival...");
        } else if (enTurno.getId() == this.idJugadorPropietario) {
            this.lblEstadoTurno.setText(">>> ES TU TURNO DE JUGAR <<<");
        } else {
            this.lblEstadoTurno.setText("Esperando el turno de " + enTurno.getNombre() + "...");
        }
        // El boton se queda activo: si se presiona fuera de turno, la fachada
        // responde con el alterno A2 (o A3 si falta el rival) y se muestra el aviso.
    }

    private static String textoJugador(JugadorDTO jugador, int numero) {
        if (jugador == null) {
            return "Jugador " + numero + ": sin conectar";
        }
        return jugador.getNombre() + ": Casilla " + jugador.getCasillaActual();
    }
}
