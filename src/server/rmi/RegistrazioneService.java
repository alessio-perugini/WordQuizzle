package server.rmi;

import errori.InvalidPassword;
import errori.UserAlreadyExists;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegistrazioneService extends Remote {
    boolean registra_utente(String nickName, String password) throws RemoteException, IllegalArgumentException, UserAlreadyExists, InvalidPassword;
}
