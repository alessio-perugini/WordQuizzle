package server.gamelogic;

import server.gamelogic.Dizionario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class DizionarioTest {

    @org.junit.jupiter.api.Test
    void getDizionario() {
        assertNotNull(Dizionario.getInstance().getDizionario());
    }

    @org.junit.jupiter.api.Test
    void getInstance() {
        Dizionario dict1 = Dizionario.getInstance();
        Dizionario dict2 = Dizionario.getInstance();
        assertEquals(dict1, dict2);
    }

    @org.junit.jupiter.api.Test
    void getNwordsFromDictionary() {
        Dizionario dict = Dizionario.getInstance();

        ArrayList<HashMap<String, String>> result = dict.getNwordsFromDictionary(4);
        assertNotNull(result);
    }
}