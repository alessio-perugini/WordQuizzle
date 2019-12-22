package server.gamelogic;

import server.Utente;

import static org.junit.jupiter.api.Assertions.*;

class PartitaTest {

    @org.junit.jupiter.api.Test
    void PartitaTest(){
        Utente u1 = new Utente("Claudio", "123456");
        Utente u2 = new Utente("Cristiana", "asd1asd");
        Sfida objSfida = new Sfida(u1, u2);
        Partita objPartita = new Partita(u1, objSfida);
        assertNotNull(objPartita);
    }
}