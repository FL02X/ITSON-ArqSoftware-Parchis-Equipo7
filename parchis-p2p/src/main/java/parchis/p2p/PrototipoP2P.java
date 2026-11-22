package parchis.p2p;

import parchis.comunicacion.ReceptorMensajes;
import parchis.dtos.MensajeDTO;
import parchis.dtos.TipoMensaje;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Prototipo del estilo peer-to-peer (Hito 2A): dos computadoras se mandan mensajes.
 *
 * <pre>
 * Uso: PrototipoP2P &lt;nombre&gt; &lt;puertoLocal&gt; [ipVecino:puerto ...]
 *
 * PC 1:  PrototipoP2P Ana 5000
 * PC 2:  PrototipoP2P Beto 5000 192.168.1.10:5000
 * </pre>
 *
 * Cualquiera de los dos puede escribir; el texto le llega al otro.
 * Comandos: /peers (lista los conectados) y /salir.
 */
public class PrototipoP2P {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Uso: PrototipoP2P <nombre> <puertoLocal> [ipVecino:puerto ...]");
            return;
        }
        String nombre = args[0];
        int puerto = Integer.parseInt(args[1]);

        final Peer peer = new Peer(nombre);
        peer.setEscuchaConexiones(new EscuchaConexiones() {
            @Override
            public void peerConectado(String nombrePeer) {
                System.out.println("*** Conectado con " + nombrePeer);
            }

            @Override
            public void peerDesconectado(String nombrePeer) {
                System.out.println("*** Se desconecto " + nombrePeer);
            }
        });
        peer.suscribir(TipoMensaje.TEXTO, new ReceptorMensajes() {
            @Override
            public void recibir(MensajeDTO mensaje) {
                System.out.println("[" + mensaje.getRemitente() + "] " + mensaje.getContenido());
            }
        });
        peer.iniciar(puerto);

        System.out.println("Peer '" + nombre + "' escuchando en "
                + InetAddress.getLocalHost().getHostAddress() + ":" + puerto);

        for (int i = 2; i < args.length; i++) {
            final String[] hostPuerto = args[i].split(":");
            final String destino = args[i];
            System.out.println("Buscando al peer " + destino + " ...");
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peer.conectar(hostPuerto[0], Integer.parseInt(hostPuerto[1]));
                    } catch (IOException ex) {
                        System.err.println("[P2P] " + ex.getMessage());
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        }
        System.out.println("Escribe un mensaje y presiona Enter (/peers, /salir).");

        BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String linea;
        while ((linea = teclado.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty()) {
                continue;
            }
            if ("/salir".equalsIgnoreCase(linea)) {
                break;
            }
            if ("/peers".equalsIgnoreCase(linea)) {
                System.out.println("Conectados: " + peer.getPeersConectados());
                continue;
            }
            if (peer.getPeersConectados().isEmpty()) {
                System.out.println("(todavia no hay peers conectados)");
            } else {
                peer.enviar(new MensajeDTO(TipoMensaje.TEXTO, nombre, linea));
            }
        }
        peer.desconectar();
    }
}
