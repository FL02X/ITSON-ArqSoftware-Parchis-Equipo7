# ITSON-ArqSoftware-Parchis-Equipo5

Juan Pedro Gastelum Romero |
Diego González Domínguez |
Cesar Armando Gaxiola Ley |
Hiddekel Yocupicio Ruiz |

## Unidad II – Parchís en red

**CU-01: Ejercer turno (Tirar dado).** El jugador en turno presiona el botón, se tira el dado,
su ficha avanza y el turno pasa al rival. Las pantallas se actualizan con Observer.
**Estilo arquitectónico:** Peer-to-peer.

| Flujo | Qué pasa | Dónde falla |
|---|---|---|
| Principal | Tirar dado → avanzar → pasar el turno | – |
| A2 | El jugador presiona el botón cuando no es su turno | `ejercerTurno` lanza `ReglasParchisException` (A2_NO_ES_SU_TURNO) |
| A3 | El jugador presiona el botón y su rival aún no se une | `ejercerTurno` lanza `ReglasParchisException` (A3_FALTA_RIVAL) |

El código parte del MVC de la Unidad I (etiqueta `unidad-1`) y conserva sus nombres.

## Módulos (uno por componente de la vista de componentes)

| Capa | Módulo | Contenido | Depende de |
|---|---|---|---|
| Presentación | `parchis-presentacion` | `TableroView`, `TurnoController`, `JuegoModel`, `ObservadorJuego`, `Main` | dtos, interfaces, fabrica |
| Negocio | `parchis-fabrica` | `FabricaReglas`: la **única** clase que conoce las implementaciones | interfaces, reglas, reglas-mock, comunicacion-api, p2p, perfiles, datos |
| Negocio | `parchis-interfaces` | `IReglasParchisFachada`, `ObservadorReglas`, `ReglasParchisException` | dtos |
| Negocio | `parchis-reglas` | Subsistema Reglas **real**: `FachadaReglas`, `EstadoPartida`, `Dado`, `ValidacionTurno` (`RivalPresente`, `TurnoDelJugador`), `Jugador` | interfaces, dtos, comunicacion-api, perfiles |
| Negocio | `parchis-reglas-mock` | `ReglasParchisMock` (Hito 2A) | interfaces, dtos |
| Negocio | `parchis-comunicacion-api` | `IComunicacion`, `ReceptorMensajes` | dtos |
| Negocio | `parchis-p2p` | Subsistema Comunicación P2P: `Peer` implementa `IComunicacion`; `ProtocoloP2P`, `EscuchaConexiones`, `PrototipoP2P` | comunicacion-api, dtos |
| Negocio | `parchis-perfiles` | Objeto de negocio `PerfilBO`, `NegocioException` | datos, dtos |
| Datos | `parchis-datos` | `IPerfilDAO`, `PerfilDAOMySQL` (todo el SQL), `PerfilEntidad`, `ConexionBD`, `PersistenciaException` | driver de MySQL |
| – | `parchis-dtos` | `JugadorDTO`, `EstadoJuegoDTO`, `ResultadoTurnoDTO`, `MensajeDTO`, `TipoMensaje`, `PerfilDTO` | – |

Reglas usa la red solo por `IComunicacion` (no sabe que el estilo es P2P) y el perfil solo por `PerfilBO`.
Cambiar el mock por el real es una línea en `FabricaReglas` (`USAR_MOCKS`).

## Preparar MySQL (una vez en cada computadora)

1. Ejecutar `parchis-datos/sql/parchis.sql` en MySQL Workbench. Crea la base `parchis` y la tabla `perfil`.
2. Copiar `db.properties.ejemplo` como **`db.properties`** en la carpeta raíz del proyecto y poner su usuario y contraseña.
   `db.properties` está en `.gitignore`: **nunca se sube**.

## Cómo correr

Abrir el proyecto padre en NetBeans → *Clean and Build*. Los argumentos se ponen en
clic derecho en `parchis-presentacion` → *Properties → Run → Arguments*.

| Argumentos | Qué hace |
|---|---|
| *(vacío)* | Las dos ventanas en esta computadora |
| `solo` | Solo la ventana del Jugador 1 (para probar A3) |
| `red Ana 5000` | **PC 1**: abre la partida como Jugador 1 (Rojo) y espera al rival |
| `red Beto 5000 192.168.1.10:5000` | **PC 2**: se une a la PC 1 (su IP) como Jugador 2 (Azul) |

Primero se corre la PC 1 y después la PC 2. Si no conecta: mismo Wi-Fi (o el hotspot de un celular)
y permitir Java en el firewall de Windows.

**Prototipo P2P (Hito 2A):** clic derecho en `parchis-p2p` → *Properties → Run → Arguments*:
`Ana 5000` en la PC 1 y `Beto 5000 192.168.1.10:5000` en la PC 2. Lo que se escribe en una consola
aparece en la otra (`/peers`, `/salir`).

## Consistencia entre pares (el riesgo del P2P)

- Un solo escritor a la vez: solo el jugador en turno puede tirar (`TurnoDelJugador`, A2).
- El valor del dado viaja en el mensaje `TURNO numeroTurno|idJugador|dado`; el otro par no vuelve a tirar.
- Cada par aplica solo el turno que espera (`numeroTurno + 1`, del jugador en turno); lo demás se descarta.
- Si dos pares se conectan entre sí a la vez, ambos conservan la conexión que abrió el de nombre menor.
