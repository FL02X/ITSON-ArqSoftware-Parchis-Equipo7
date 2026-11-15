package parchis.p2p;

/**
 * Mensaje que intercambian los peers. Viaja como una linea de texto:
 * TIPO|remitente|contenido
 */
public class MensajeP2P {

    public enum Tipo {
        /** Saludo inicial: identifica al peer que abre o acepta la conexion. */
        HOLA,
        /** Mensaje de texto del prototipo. En 2B aqui viajaran los cambios de la sala. */
        TEXTO
    }

    private static final String SEPARADOR = "|";

    private final Tipo tipo;
    private final String remitente;
    private final String contenido;

    public MensajeP2P(Tipo tipo, String remitente, String contenido) {
        this.tipo = tipo;
        this.remitente = remitente;
        this.contenido = contenido == null ? "" : contenido;
    }

    public String serializar() {
        // Los saltos de linea romperian el protocolo de lineas, se reemplazan.
        return tipo.name() + SEPARADOR + remitente + SEPARADOR + contenido.replace('\n', ' ').replace('\r', ' ');
    }

    public static MensajeP2P deserializar(String linea) {
        if (linea == null) {
            return null;
        }
        String[] partes = linea.split("\\|", 3);
        if (partes.length < 2) {
            return null;
        }
        try {
            Tipo tipo = Tipo.valueOf(partes[0]);
            return new MensajeP2P(tipo, partes[1], partes.length == 3 ? partes[2] : "");
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getRemitente() {
        return remitente;
    }

    public String getContenido() {
        return contenido;
    }
}
