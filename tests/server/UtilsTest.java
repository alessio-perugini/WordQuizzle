package server;

import org.junit.jupiter.api.Test;
import server.gamelogic.Sfida;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void printDizionarioDellaSfida() {
        Sfida objSfida = new Sfida(new Utente("Claudio", "123456"), new Utente("Cristiana", "asd1asd"));
        Utils.printDizionarioDellaSfida(objSfida);

    }
}