package server;

import server.rmi.RegistrazioneServer;

public class Main {
    public static void main(String[] args) {
        RegistrazioneServer regServer = new RegistrazioneServer();
        regServer.start(); //Avvio il server RMI
        Server objServer = new Server();
        try {
            objServer.start(); //Avvio il server TCP
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
