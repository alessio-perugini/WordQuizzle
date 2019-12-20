package client;

import server.rmi.RegistrazioneService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiClient {
    int port = 30000;

    public boolean registra_utente(String username, String pw){
        Remote remoteObject;
        RegistrazioneService serverObject;

        try{
            Registry r = LocateRegistry.getRegistry(port);
            remoteObject = r.lookup("REGISTRAZIONE-SERVER");
            serverObject = (RegistrazioneService) remoteObject;
            return serverObject.registra_utente(username, pw);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  false;
    }
}
