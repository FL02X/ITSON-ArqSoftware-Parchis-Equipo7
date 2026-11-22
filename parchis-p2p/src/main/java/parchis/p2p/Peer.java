package parchis.p2p;

import parchis.comunicacion.IComunicacion;
import parchis.comunicacion.ReceptorMensajes;
import parchis.dtos.MensajeDTO;
import parchis.dtos.TipoMensaje;

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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Implementacion peer-to-peer del subsistema de comunicacion.
 * Cada computadora es servidor (escucha en su puerto) y cliente (se conecta a
 * otros pares) al mismo tiempo. No hay servidor central.
 */
public class Peer implements IComunicacion {

    private static final int INTENTOS_CONEXION = 10;
    private static final int ESPERA_ENTRE_INTENTOS_MS = 1000;
    private static final int ESPERA_SALUDO_MS = 5000;

    private final String nombre;
    private final Map<String, Conexion> conexiones = new LinkedHashMap<String, Conexion>();
    private final Map<TipoMensaje, List<ReceptorMensajes>> receptores =
            new EnumMap<TipoMensaje, List<ReceptorMensajes>>(TipoMensaje.class);
    private volatile EscuchaConexiones escucha;
    private ServerSocket servidor;
    private volatile boolean activo;

    public Peer(String nombre) {
        if (nombre == null || nombre.trim().isEmpty() || nombre.contains("|")) {
            throw new IllegalArgumentException("El nombre del par no puede estar vacio ni contener '|'");
        }
        this.nombre = nombre.trim();
        for (TipoMensaje t : TipoMensaje.values()) {
            receptores.put(t, new CopyOnWriteArrayList<ReceptorMensajes>());
        }
    }

    public void setEscuchaConexiones(EscuchaConexiones escucha) {
        this.escucha = escucha;
    }

    // ------------------------------------------------------------ IComunicacion

    @Override
    public void iniciar(int puertoLocal) throws IOException {
        servidor = new ServerSocket(puertoLocal);
        activo = true;
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (activo) {
                    try {
                        atender(servidor.accept(), false);
                    } catch (IOException ex) {
                        if (activo) {
                            System.err.println("[P2P] Error aceptando conexion: " + ex.getMessage());
                        }
                    }
                }
            }
        }, "peer-escucha-" + puertoLocal);
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Se conecta a otro par y espera su saludo. Reintenta unos segundos por si
     * el otro aun no arranca. Lanza IOException si no lo logra.
     */
    @Override
    public void conectar(String host, int puerto) throws IOException {
        IOException ultimo = null;
        for (int intento = 1; intento <= INTENTOS_CONEXION; intento++) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, puerto), 3000);
                Conexion c = atender(socket, true);
                if (c != null && c.saludo.await(ESPERA_SALUDO_MS, TimeUnit.MILLISECONDS)) {
                    return;
                }
                if (!getPeersConectados().isEmpty()) {
                    return; // la conexion quedo duplicada y gano la otra
                }
                ultimo = new IOException("El par " + host + ":" + puerto + " no respondio el saludo");
            } catch (IOException ex) {
                ultimo = ex;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Conexion interrumpida", ex);
            }
            dormir(ESPERA_ENTRE_INTENTOS_MS);
        }
        throw new IOException("No se pudo conectar con " + host + ":" + puerto
                + (ultimo != null ? " (" + ultimo.getMessage() + ")" : ""));
    }

    @Override
    public void enviar(MensajeDTO mensaje) {
        String linea = ProtocoloP2P.aLinea(mensaje);
        for (Conexion c : destinos()) {
            c.enviar(linea);
        }
    }

    @Override
    public void suscribir(TipoMensaje tipo, ReceptorMensajes receptor) {
        receptores.get(tipo).add(receptor);
    }

    @Override
    public void desconectar() {
        activo = false;
        try {
            if (servidor != null) {
                servidor.close();
            }
        } catch (IOException ignorada) {
            // se esta cerrando
        }
        for (Conexion c : destinos()) {
            c.cerrar();
        }
        synchronized (conexiones) {
            conexiones.clear();
        }
    }

    // ------------------------------------------------------------ consultas

    public List<String> getPeersConectados() {
        synchronized (conexiones) {
            return new ArrayList<String>(conexiones.keySet());
        }
    }

    public String getNombre() {
        return nombre;
    }

    // ------------------------------------------------------------ internos

    private List<Conexion> destinos() {
        synchronized (conexiones) {
            return new ArrayList<Conexion>(conexiones.values());
        }
    }

    private Conexion atender(Socket socket, boolean laAbriYo) {
        final Conexion conexion;
        try {
            conexion = new Conexion(socket, laAbriYo);
        } catch (IOException ex) {
            cerrarSilencioso(socket);
            return null;
        }
        conexion.enviar(ProtocoloP2P.saludo(nombre));
        Thread lector = new Thread(new Runnable() {
            @Override
            public void run() {
                leer(conexion);
            }
        }, "peer-lector");
        lector.setDaemon(true);
        lector.start();
        return conexion;
    }

    private void leer(Conexion conexion) {
        try {
            String linea;
            while ((linea = conexion.entrada.readLine()) != null) {
                String[] p = ProtocoloP2P.partes(linea);
                if (p == null) {
                    continue;
                }
                if (ProtocoloP2P.HOLA.equals(p[0])) {
                    if (!registrar(conexion, p[1])) {
                        return;
                    }
                } else if (conexion.nombreRemoto != null) {
                    MensajeDTO m = ProtocoloP2P.aMensaje(p);
                    if (m != null) {
                        entregar(m);
                    }
                }
            }
        } catch (IOException ex) {
            // el otro par se fue
        } finally {
            quitar(conexion);
        }
    }

    private void entregar(MensajeDTO m) {
        for (ReceptorMensajes r : receptores.get(m.getTipo())) {
            try {
                r.recibir(m);
            } catch (RuntimeException ex) {
                System.err.println("[P2P] Error procesando " + m + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Si dos pares se conectaron entre si al mismo tiempo, ambos conservan la
     * conexion que abrio el par de nombre menor: nunca se duplican mensajes.
     */
    private boolean registrar(Conexion conexion, String nombreRemoto) {
        if (nombreRemoto.equals(nombre)) {
            System.err.println("[P2P] Se rechazo un par con el mismo nombre: " + nombreRemoto);
            conexion.cerrar();
            return false;
        }
        Conexion anterior;
        synchronized (conexiones) {
            anterior = conexiones.get(nombreRemoto);
            if (anterior != null) {
                boolean debeAbrirlaYo = nombre.compareTo(nombreRemoto) < 0;
                if (conexion.laAbriYo != debeAbrirlaYo) {
                    conexion.cerrar();
                    return false;
                }
            }
            conexion.nombreRemoto = nombreRemoto;
            conexiones.put(nombreRemoto, conexion);
        }
        conexion.saludo.countDown();
        if (anterior != null) {
            anterior.nombreRemoto = null;
            anterior.cerrar();
        } else if (escucha != null) {
            escucha.peerConectado(nombreRemoto);
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
        if (quitada && activo && escucha != null) {
            escucha.peerDesconectado(nombreRemoto);
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

    /** Una conexion abierta con otro par. */
    private static final class Conexion {
        private final Socket socket;
        private final boolean laAbriYo;
        private final BufferedReader entrada;
        private final PrintWriter salida;
        private final CountDownLatch saludo = new CountDownLatch(1);
        private volatile String nombreRemoto;

        Conexion(Socket socket, boolean laAbriYo) throws IOException {
            this.socket = socket;
            this.laAbriYo = laAbriYo;
            this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        }

        synchronized void enviar(String linea) {
            salida.println(linea);
        }

        void cerrar() {
            cerrarSilencioso(socket);
        }
    }
}
