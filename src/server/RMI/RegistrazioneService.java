package server.RMI;

import Errori.UserAlreadyExists;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrazioneService extends Remote {
    public boolean registra_utente(String nickName, String password) throws RemoteException, IllegalArgumentException, UserAlreadyExists;
}
