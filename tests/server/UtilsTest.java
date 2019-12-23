package server;

import org.junit.jupiter.api.Test;
import server.gamelogic.Sfida;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void printDizionarioDellaSfida() {
        Sfida objSfida = new Sfida(new Utente("Claudio", "123456"), new Utente("Cristiana", "asd1asd"));
        Utils.printDizionarioDellaSfida(objSfida);

    }

    @Test
    void udpPortAvailable() {/*
        Socket s = null;
        boolean aperto = false;
        try
        {
            s = new Socket("localhost", 50002);
            aperto = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            aperto = false;
        }
        finally
        {
            if(s != null)
                try {s.close();}
                catch(Exception e){}
        }*/
        //assertTrue(aperto);
        assertTrue(Utils.udpPortAvailable(50002));

    }
}