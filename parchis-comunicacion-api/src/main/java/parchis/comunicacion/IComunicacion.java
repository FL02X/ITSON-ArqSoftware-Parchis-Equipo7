package parchis.comunicacion;

import parchis.dtos.MensajeDTO;
import parchis.dtos.TipoMensaje;

import java.io.IOException;

/**
 * Subsistema de comunicacion. Cada equipo lo implementa con su estilo; el resto
 * del sistema no sabe cual es. El nuestro es peer-to-peer (modulo parchis-p2p).
 */
public interface IComunicacion {

    /** Empieza a escuchar en el puerto local para que otros pares se conecten. */
    void iniciar(int puertoLocal) throws IOException;

    /** Se conecta a otro par. Regresa cuando la conexion quedo lista. */
    void conectar(String host, int puerto) throws IOException;

    /** Manda el mensaje a todos los pares conectados. */
    void enviar(MensajeDTO mensaje) throws IOException;

    /** Pide que se le entreguen los mensajes de ese tipo. */
    void suscribir(TipoMensaje tipo, ReceptorMensajes receptor);

    /** Cierra todas las conexiones. */
    void desconectar();
}
