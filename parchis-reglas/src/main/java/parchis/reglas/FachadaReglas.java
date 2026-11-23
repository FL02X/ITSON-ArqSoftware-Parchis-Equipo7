package parchis.reglas;

import parchis.comunicacion.IComunicacion;
import parchis.comunicacion.ReceptorMensajes;
import parchis.dtos.EstadoJuegoDTO;
import parchis.dtos.MensajeDTO;
import parchis.dtos.ResultadoTurnoDTO;
import parchis.dtos.TipoMensaje;
import parchis.interfaces.IReglasParchisFachada;
import parchis.interfaces.ObservadorReglas;
import parchis.interfaces.ReglasParchisException;
import parchis.interfaces.ReglasParchisException.Flujo;
import parchis.perfiles.NegocioException;
import parchis.perfiles.PerfilBO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subsistema Reglas real (Hito 2B). Implementa la MISMA IReglasParchisFachada
 * que el mock, asi que la presentacion no nota el cambio (Liskov).
 *
 * <p>Usa al subsistema de comunicacion solo por IComunicacion (no sabe que el
 * estilo es P2P) y guarda el perfil con PerfilBO.</p>
 *
 * <p>Consistencia entre pares: un solo escritor a la vez (el jugador en turno),
 * el valor del dado viaja en el mensaje y un numero de turno ordena los cambios.
 * Un mensaje TURNO fuera de orden o de un jugador sin turno se descarta.</p>
 */
public class FachadaReglas implements IReglasParchisFachada {

    private final IComunicacion comunicacion;
    private final PerfilBO perfiles;
    private final Dado dado;
    private final String nombrePar;
    private final EstadoPartida estado = new EstadoPartida();
    private final List<ValidacionTurno> validaciones;
    private final List<ObservadorReglas> observadores = new CopyOnWriteArrayList<ObservadorReglas>();

    public FachadaReglas(IComunicacion comunicacion, PerfilBO perfiles, String nombrePar) {
        this(comunicacion, perfiles, nombrePar, new Dado(new java.util.Random()));
    }

    FachadaReglas(IComunicacion comunicacion, PerfilBO perfiles, String nombrePar, Dado dado) {
        this.comunicacion = comunicacion;
        this.perfiles = perfiles;
        this.nombrePar = nombrePar;
        this.dado = dado;
        this.validaciones = Arrays.<ValidacionTurno>asList(new RivalPresente(), new TurnoDelJugador());
        comunicacion.suscribir(TipoMensaje.UNIRSE, new ReceptorMensajes() {
            @Override
            public void recibir(MensajeDTO mensaje) {
                recibirUnion(mensaje);
            }
        });
        comunicacion.suscribir(TipoMensaje.TURNO, new ReceptorMensajes() {
            @Override
            public void recibir(MensajeDTO mensaje) {
                recibirTurno(mensaje);
            }
        });
    }

    // ------------------------------------------------------------ IReglasParchisFachada

    @Override
    public synchronized EstadoJuegoDTO unirseAPartida(int idJugador, String nombre, String color)
            throws ReglasParchisException {
        validarUnion(idJugador, nombre);
        try {
            perfiles.obtenerOCrear(nombre, color);
        } catch (NegocioException e) {
            throw new ReglasParchisException(Flujo.REGLA_DE_PARTIDA, e.getMessage()
                    + " Revisa que MySQL este encendido y que exista db.properties.");
        }
        Jugador j = new Jugador(idJugador, nombre.trim(), color, true);
        estado.agregar(j);
        enviar(TipoMensaje.UNIRSE, codificarUnion(j));
        return notificar();
    }

    @Override
    public synchronized ResultadoTurnoDTO ejercerTurno(int idJugador) throws ReglasParchisException {
        for (ValidacionTurno v : validaciones) {
            v.validar(idJugador, estado);
        }
        int valor = dado.lanzar();
        Jugador j = estado.enTurno();
        int anterior = j.getCasillaActual();
        estado.aplicarTirada(valor);
        enviar(TipoMensaje.TURNO, estado.getNumeroTurno() + "|" + idJugador + "|" + valor);
        notificar();
        return new ResultadoTurnoDTO(idJugador, valor, anterior, j.getCasillaActual(), estado.enTurno().getId());
    }

    @Override
    public synchronized EstadoJuegoDTO obtenerEstado() {
        return estado.aDTO();
    }

    @Override
    public void agregarObservador(ObservadorReglas observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    // ------------------------------------------------------------ mensajes de otros pares

    /** UNIRSE: id|nombre|color. Si es alguien nuevo, se registra y se le responde con los jugadores locales. */
    private synchronized void recibirUnion(MensajeDTO m) {
        String[] p = m.getContenido().split("\\|", 3);
        if (p.length < 3) {
            return;
        }
        int id;
        try {
            id = Integer.parseInt(p[0]);
        } catch (NumberFormatException ex) {
            return;
        }
        if (estado.buscar(id) != null || estado.completa()) {
            return; // ya lo conocemos, o no cabe: se ignora
        }
        estado.agregar(new Jugador(id, p[1], p[2], false));
        for (Jugador local : estado.locales()) {
            enviar(TipoMensaje.UNIRSE, codificarUnion(local));
        }
        notificar();
    }

    /** TURNO: numeroTurno|idJugador|dado. Se aplica el mismo dado; no se vuelve a tirar. */
    private synchronized void recibirTurno(MensajeDTO m) {
        String[] p = m.getContenido().split("\\|");
        if (p.length < 3) {
            return;
        }
        try {
            int numero = Integer.parseInt(p[0]);
            int id = Integer.parseInt(p[1]);
            int valor = Integer.parseInt(p[2]);
            Jugador enTurno = estado.enTurno();
            if (numero != estado.getNumeroTurno() + 1 || enTurno == null || enTurno.getId() != id
                    || valor < 1 || valor > 6) {
                System.err.println("[Reglas] Turno descartado (fuera de orden o sin turno): " + m);
                return;
            }
            estado.aplicarTirada(valor);
            notificar();
        } catch (NumberFormatException ex) {
            System.err.println("[Reglas] Turno con formato invalido: " + m);
        }
    }

    // ------------------------------------------------------------ privados

    private void validarUnion(int idJugador, String nombre) throws ReglasParchisException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglasParchisException(Flujo.REGLA_DE_PARTIDA, "El jugador debe tener un nombre.");
        }
        if (estado.buscar(idJugador) != null) {
            throw new ReglasParchisException(Flujo.REGLA_DE_PARTIDA, "El jugador " + idJugador + " ya esta en la partida.");
        }
        if (estado.completa()) {
            throw new ReglasParchisException(Flujo.REGLA_DE_PARTIDA, "La partida ya esta llena.");
        }
    }

    private static String codificarUnion(Jugador j) {
        return j.getId() + "|" + j.getNombre() + "|" + j.getColor();
    }

    private void enviar(TipoMensaje tipo, String contenido) {
        try {
            comunicacion.enviar(new MensajeDTO(tipo, nombrePar, contenido));
        } catch (IOException ex) {
            System.err.println("[Reglas] No se pudo enviar " + tipo + ": " + ex.getMessage());
        }
    }

    private EstadoJuegoDTO notificar() {
        EstadoJuegoDTO dto = estado.aDTO();
        for (ObservadorReglas o : new ArrayList<ObservadorReglas>(observadores)) {
            o.juegoActualizado(dto);
        }
        return dto;
    }
}
