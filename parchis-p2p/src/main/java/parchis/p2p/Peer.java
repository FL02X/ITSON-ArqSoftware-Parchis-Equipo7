package parchis.p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nodo del estilo peer-to-peer: cada computadora es servidor (escucha en su
 * puerto) y cliente (se conecta a sus vecinos) al mismo tiempo. No hay un
 * servidor central: todos los peers son iguales.
 */
public class Peer {

    private static final int INTENTOS_CONEXION = 30;
    private static final int ESPERA_ENTRE_INTENTOS_MS = 2000;

    private final String nombre;
    private final int puerto;
    private final IReceptorMensajes receptor;
    private final Map<String, Conexion> conexiones = new LinkedHashMap<String, Conexion>();
    private ServerSocket servidor;
    private volatile boolean activo;

    public Peer(String nombre, int puerto, IReceptorMensajes receptor) {
        if (nombre.contains("|")) {
            throw new IllegalArgumentException("El nombre no puede contener '|'");
        }
        this.nombre = nombre;
        this.puerto = puerto;
        this.receptor = receptor;
    }

    /** Empieza a escuchar conexiones de otros peers. */
    public void iniciar() throws IOException {
        servidor = new ServerSocket(puerto);
        activo = true;
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (activo) {
                    try {
                        Socket socket = servidor.accept();
                        atender(socket, false);
                    } catch (IOException ex) {
                        if (activo) {
                            System.err.println("[P2P] Error aceptando conexion: " + ex.getMessage());
                        }
                    }
                }
            }
        }, "peer-escucha-" + puerto);
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Se conecta a otro peer; reintenta un rato por si el otro aun no arranca. */
    public void conectarCon(final String host, final int puertoRemoto) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int intento = 1; intento <= INTENTOS_CONEXION && activo; intento++) {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(host, puertoRemoto), 3000);
                        atender(socket, true);
                        return;
                    } catch (IOException ex) {
                        dormir(ESPERA_ENTRE_INTENTOS_MS);
                    }
                }
                System.err.println("[P2P] No se pudo conectar con " + host + ":" + puertoRemoto);
            }
        }, "peer-conecta-" + host + ":" + puertoRemoto);
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Manda un texto a todos los peers conectados. Devuelve a cuantos llego. */
    public int enviarATodos(String texto) {
        MensajeP2P mensaje = new MensajeP2P(MensajeP2P.Tipo.TEXTO, nombre, texto);
        List<Conexion> destinos;
        synchronized (conexiones) {
            destinos = new ArrayList<Conexion>(conexiones.values());
        }
        for (Conexion c : destinos) {
            c.enviar(mensaje);
        }
        return destinos.size();
    }

    public List<String> getPeersConectados() {
        synchronized (conexiones) {
            return new ArrayList<String>(conexiones.keySet());
        }
    }

    public String getNombre() {
        return nombre;
    }

    public void detener() {
        activo = false;
        try {
            if (servidor != null) {
                servidor.close();
            }
        } catch (IOException ignorada) {
            // se esta cerrando
        }
        synchronized (conexiones) {
            for (Conexion c : conexiones.values()) {
                c.cerrar();
            }
            conexiones.clear();
        }
    }

    // ------------------------------------------------------------ internos

    private void atender(Socket socket, boolean laAbriYo) {
        final Conexion conexion;
        try {
            conexion = new Conexion(socket, laAbriYo);
        } catch (IOException ex) {
            cerrarSilencioso(socket);
            return;
        }
        conexion.enviar(new MensajeP2P(MensajeP2P.Tipo.HOLA, nombre, ""));
        Thread lector = new Thread(new Runnable() {
            @Override
            public void run() {
                leer(conexion);
            }
        }, "peer-lector");
        lector.setDaemon(true);
        lector.start();
    }

    private void leer(Conexion conexion) {
        try {
            String linea;
            while ((linea = conexion.entrada.readLine()) != null) {
                MensajeP2P mensaje = MensajeP2P.deserializar(linea);
                if (mensaje == null) {
                    continue;
                }
                if (mensaje.getTipo() == MensajeP2P.Tipo.HOLA) {
                    if (!registrar(conexion, mensaje.getRemitente())) {
                        return; // conexion duplicada descartada
                    }
                } else if (conexion.nombreRemoto != null) {
                    receptor.mensajeRecibido(mensaje);
                }
            }
        } catch (IOException ex) {
            // el otro peer se fue
        } finally {
            quitar(conexion);
        }
    }

    /**
     * Registra la conexion con el nombre del peer remoto. Si los dos peers se
     * conectaron entre si al mismo tiempo, ambos conservan la que abrio el
     * peer con el nombre menor, asi nunca hay mensajes duplicados.
     */
    private boolean registrar(Conexion conexion, String nombreRemoto) {
        if (nombreRemoto.equals(nombre)) {
            System.err.println("[P2P] Se rechazo un peer con el mismo nombre: " + nombreRemoto);
            conexion.cerrar();
            return false;
        }
        Conexion anterior;
        synchronized (conexiones) {
            anterior = conexiones.get(nombreRemoto);
            if (anterior != null) {
                boolean debeAbrirlaYo = nombre.compareTo(nombreRemoto) < 0;
                boolean nuevaGana = conexion.laAbriYo == debeAbrirlaYo;
                if (!nuevaGana) {
                    conexion.cerrar();
                    return false;
                }
            }
            conexion.nombreRemoto = nombreRemoto;
            conexiones.put(nombreRemoto, conexion);
        }
        if (anterior != null) {
            anterior.nombreRemoto = null; // ya no representa al peer
            anterior.cerrar();
        } else {
            receptor.peerConectado(nombreRemoto);
        }
        return true;
    }

    private void quitar(Conexion conexion) {
        conexion.cerrar();
        String nombreRemoto = conexion.nombreRemoto;
        if (nombreRemoto == null) {
            return;
        }
        boolean quitada;
        synchronized (conexiones) {
            quitada = conexiones.get(nombreRemoto) == conexion;
            if (quitada) {
                conexiones.remove(nombreRemoto);
            }
        }
        if (quitada && activo) {
            receptor.peerDesconectado(nombreRemoto);
        }
    }

    private static void dormir(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void cerrarSilencioso(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignorada) {
            // nada que hacer
        }
    }

    /** Una conexion abierta con otro peer. */
    private static final class Conexion {
        private final Socket socket;
        private final boolean laAbriYo;
        private final BufferedReader entrada;
        private final PrintWriter salida;
        private volatile String nombreRemoto;

        Conexion(Socket socket, boolean laAbriYo) throws IOException {
            this.socket = socket;
            this.laAbriYo = laAbriYo;
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }

        synchronized void enviar(MensajeP2P mensaje) {
            salida.println(mensaje.serializar());
        }

        void cerrar() {
            cerrarSilencioso(socket);
        }
    }
}
