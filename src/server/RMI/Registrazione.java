package server.RMI;

import Errori.PasswordNotValid;
import Errori.UserAlreadyExists;
import server.UtentiConnessi;
import server.Utente;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

public class Registrazione extends RemoteServer implements RegistrazioneService {
    private static final long serialVersionUID = 1L;

    private UtentiConnessi lsUtenti;

    public Registrazione(){
        lsUtenti = UtentiConnessi.getInstance();
    }

    @Override
    public boolean registra_utente(String nickName, String password) throws RemoteException, IllegalArgumentException, UserAlreadyExists {
        if(nickName == null || nickName.length() == 0) throw new IllegalArgumentException("nickname non valido");
        if(password == null || password.length() < 6) throw new PasswordNotValid("Password non valida");

        Utente user = new Utente(nickName, password);

        this.lsUtenti.addUtente(user);

        return false;
    }

}
