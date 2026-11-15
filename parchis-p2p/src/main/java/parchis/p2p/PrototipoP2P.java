package parchis.p2p;

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

        Peer peer = new Peer(nombre, puerto, new IReceptorMensajes() {
            @Override
            public void mensajeRecibido(MensajeP2P mensaje) {
                System.out.println("[" + mensaje.getRemitente() + "] " + mensaje.getContenido());
            }

            @Override
            public void peerConectado(String nombrePeer) {
                System.out.println("*** Conectado con " + nombrePeer);
            }

            @Override
            public void peerDesconectado(String nombrePeer) {
                System.out.println("*** Se desconecto " + nombrePeer);
            }
        });
        peer.iniciar();

        System.out.println("Peer '" + nombre + "' escuchando en "
                + InetAddress.getLocalHost().getHostAddress() + ":" + puerto);

        for (int i = 2; i < args.length; i++) {
            String[] hostPuerto = args[i].split(":");
            peer.conectarCon(hostPuerto[0], Integer.parseInt(hostPuerto[1]));
            System.out.println("Buscando al peer " + args[i] + " ...");
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
            int enviados = peer.enviarATodos(linea);
            if (enviados == 0) {
                System.out.println("(todavia no hay peers conectados)");
            }
        }
        peer.detener();
    }
}
