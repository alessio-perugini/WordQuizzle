package server.storage;

import org.junit.jupiter.api.Test;
import server.Settings;
import server.Utente;

import java.util.concurrent.ConcurrentHashMap;

class StorageTest {

    @Test
    void writeObjectToJSONFile() {
        ConcurrentHashMap<String, Utente> hashListaUtenti = (ConcurrentHashMap<String, Utente>) Storage.getObjectFromJSONFile(Settings.JSON_FILENAME);
        Storage.writeObjectToJSONFile(Settings.JSON_FILENAME, hashListaUtenti);
    }
}