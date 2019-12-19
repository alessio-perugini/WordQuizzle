package server;

import server.rmi.RegistrazioneServer;
import server.storage.Storage;

public class Main {
    static int RMIPort = 30000;
    public static void main(String[] args){
        RegistrazioneServer regServer = new RegistrazioneServer(RMIPort);
        regServer.start();
        Server objServer = new Server(1500);
        objServer.start();
    }
}
