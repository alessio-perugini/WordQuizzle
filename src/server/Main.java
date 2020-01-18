package server;

import server.rmi.RegistrazioneServer;

public class Main {
    public static void main(String[] args) {
        RegistrazioneServer regServer = new RegistrazioneServer();
        regServer.start();
        Server objServer = new Server();
        try {
            objServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
