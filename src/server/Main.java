package server;

import server.rmi.RegistrazioneServer;
import server.storage.Storage;

import java.util.concurrent.ConcurrentHashMap;

public class Main {
    static int RMIPort = 30000;
    public static void main(String[] args){
        RegistrazioneServer regServer = new RegistrazioneServer(RMIPort);
        regServer.start();
        Server objServer = new Server(1500);
        objServer.start();
    }
}
