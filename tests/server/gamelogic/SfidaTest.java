package server.gamelogic;

import org.junit.jupiter.api.Test;
import server.Utente;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SfidaTest {

    @Test
    void generaTraduzioni() {
        Sfida objSfida = new Sfida(new Utente("Claudio", "123456"), new Utente("Cristiana", "asd1asd"));
        objSfida.generaTraduzioni();
        ArrayList<HashMap<String,String>> paroleDaIndovinare = objSfida.getParoleDaIndovinare();
        assertNotNull(paroleDaIndovinare);
    }
}