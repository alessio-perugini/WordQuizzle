package server;

import org.junit.jupiter.api.Test;
import server.gamelogic.Sfida;

import java.sql.Timestamp;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void printDizionarioDellaSfida() {
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

    @Test
    void addSecondsToATimeStamp() throws ParseException {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = Utils.addSecondsToATimeStamp(start, 60);
        assertEquals(Utils.addSecondsToATimeStamp(end, -60), start);
    }

    @Test
    void isCurrentTimeExpired() {
        Timestamp start = new Timestamp(System.currentTimeMillis());
        Timestamp end = Utils.addSecondsToATimeStamp(start, 60);
        assertFalse(Utils.isGivenTimeExpired(end));
        assertTrue(Utils.isGivenTimeExpired(start));
    }
}