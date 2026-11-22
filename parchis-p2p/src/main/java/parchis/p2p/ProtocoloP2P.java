package parchis.p2p;

import parchis.dtos.MensajeDTO;
import parchis.dtos.TipoMensaje;

/**
 * Protocolo de texto entre pares: una linea por mensaje.
 * <pre>
 *   HOLA|nombre|            saludo al abrir la conexion (interno del estilo)
 *   TIPO|remitente|contenido   un MensajeDTO; TIPO es un TipoMensaje
 * </pre>
 * El contenido puede llevar '|': solo se separan los dos primeros campos.
 */
final class ProtocoloP2P {

    static final String HOLA = "HOLA";
    private static final String SEPARADOR = "|";

    private ProtocoloP2P() {
    }

    static String saludo(String nombre) {
        return HOLA + SEPARADOR + nombre + SEPARADOR;
    }

    static String aLinea(MensajeDTO m) {
        return m.getTipo().name() + SEPARADOR + m.getRemitente() + SEPARADOR
                + m.getContenido().replace('\n', ' ').replace('\r', ' ');
    }

    /** Partes [tipo, remitente, contenido] o null si la linea no es valida. */
    static String[] partes(String linea) {
        if (linea == null) {
            return null;
        }
        String[] p = linea.split("\\|", 3);
        if (p.length < 2) {
            return null;
        }
        return new String[]{p[0], p[1], p.length == 3 ? p[2] : ""};
    }

    /** Convierte una linea de datos en MensajeDTO; null si el tipo no existe. */
    static MensajeDTO aMensaje(String[] p) {
        try {
            return new MensajeDTO(TipoMensaje.valueOf(p[0]), p[1], p[2]);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
