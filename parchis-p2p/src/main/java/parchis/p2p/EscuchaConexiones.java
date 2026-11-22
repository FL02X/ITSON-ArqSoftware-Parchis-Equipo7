package parchis.p2p;

/**
 * Avisos opcionales de cuando un par se conecta o se va (los usa el prototipo).
 */
public interface EscuchaConexiones {

    void peerConectado(String nombrePeer);

    void peerDesconectado(String nombrePeer);
}
