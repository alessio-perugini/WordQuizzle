package server;

import server.rmi.RegistrazioneServer;
import server.storage.Storage;

import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args){
        RegistrazioneServer regServer = new RegistrazioneServer();
        regServer.start();
        Server objServer = new Server();
        objServer.start();
    }
}
