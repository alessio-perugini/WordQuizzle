package client;

import errori.InvalidPassword;
import errori.UserAlreadyExists;
import server.Settings;
import server.rmi.RegistrazioneService;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiClient {
    public boolean registra_utente(String username, String pw) {
        Remote remoteObject;
        RegistrazioneService serverObject;

        try {
            Registry r = LocateRegistry.getRegistry(Settings.RMI_PORT);
            remoteObject = r.lookup("REGISTRAZIONE-SERVER"); //cerco dal rmiServer la funzione di registrazione
            serverObject = (RegistrazioneService) remoteObject;
            return serverObject.registra_utente(username, pw); //chiamo la funzione del server per registrarmi
        } catch (IllegalArgumentException | UserAlreadyExists | InvalidPassword e) {
            System.out.println(e.getMessage());
        } catch (RemoteException | NotBoundException e2) {
            e2.printStackTrace();
        }
        return false;
    }
}