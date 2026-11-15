package parchis.p2p;

/**
 * Quien quiera enterarse de lo que pasa en la red de peers.
 */
public interface IReceptorMensajes {

    void mensajeRecibido(MensajeP2P mensaje);

    void peerConectado(String nombrePeer);

    void peerDesconectado(String nombrePeer);
}
