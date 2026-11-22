package parchis.dtos;

import java.io.Serializable;

/**
 * Mensaje que una aplicacion le manda a las demas por la red.
 * Inmutable y sin logica: el subsistema que lo recibe decide que hacer.
 */
public class MensajeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TipoMensaje tipo;
    private final String remitente;
    private final String contenido;

    public MensajeDTO(TipoMensaje tipo, String remitente, String contenido) {
        this.tipo = tipo;
        this.remitente = remitente;
        this.contenido = contenido == null ? "" : contenido;
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    public String getRemitente() {
        return remitente;
    }

    public String getContenido() {
        return contenido;
    }

    @Override
    public String toString() {
        return tipo + " de " + remitente + ": " + contenido;
    }
}
