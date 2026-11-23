package parchis.reglas;

import java.util.Random;

/**
 * Dado de seis caras. Solo lo tira la computadora del jugador en turno;
 * el valor viaja en el mensaje TURNO y los demas pares no vuelven a tirar.
 */
class Dado {

    private final Random random;

    Dado(Random random) {
        this.random = random;
    }

    int lanzar() {
        return random.nextInt(6) + 1;
    }
}
