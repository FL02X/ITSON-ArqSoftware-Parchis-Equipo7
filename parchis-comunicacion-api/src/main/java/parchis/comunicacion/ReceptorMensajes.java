package parchis.comunicacion;

import parchis.dtos.MensajeDTO;

/**
 * Quien quiere recibir mensajes de la red.
 * Ojo: recibir() llega en un hilo de red, no en el de Swing.
 */
public interface ReceptorMensajes {

    void recibir(MensajeDTO mensaje);
}
