package server;

import server.RMI.RegistrazioneServer;
import server.Storage.Storage;

public class Main {
    static int RMIPort = 30000;
    public static void main(String[] args){
        Storage objStorage = new Storage();
        objStorage.getObjectFromJSONFile("utenti.json");
        RegistrazioneServer regServer = new RegistrazioneServer(RMIPort);
        regServer.start();
        Server objServer = new Server(1500);
        objServer.start();


    }
}
