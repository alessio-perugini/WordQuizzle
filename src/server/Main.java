package server;

import server.RMI.RegistrazioneServer;

public class Main {
    static int RMIPort = 30000;
    public static void main(String[] args){
        RegistrazioneServer regServer = new RegistrazioneServer(RMIPort);
        regServer.start();
    }
}
