package server;

import server.gamelogic.Sfida;
import server.rmi.RegistrazioneServer;
import server.storage.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args){
        RegistrazioneServer regServer = new RegistrazioneServer();
        regServer.start();
        Server objServer = new Server();
        objServer.start();
    }
}
