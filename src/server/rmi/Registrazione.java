package server.rmi;

import errori.InvalidPassword;
import errori.UserAlreadyExists;
import server.ListaUtenti;
import server.Utente;

import java.rmi.server.RemoteServer;

public class Registrazione extends RemoteServer implements RegistrazioneService {
    private static final long serialVersionUID = 1L;

    private ListaUtenti lsUtenti;

    public Registrazione() {
        lsUtenti = ListaUtenti.getInstance();
    }

    @Override
    public boolean registra_utente(String nickName, String password) throws IllegalArgumentException, UserAlreadyExists {
        if (nickName == null || nickName.length() == 0) throw new IllegalArgumentException("nickname non valido");
        if (password == null || password.length() < 6) throw new InvalidPassword("Password non valida");

        Utente user = new Utente(nickName, password);
        //TODO da cambiare che ritorna una string
        try {
            return this.lsUtenti.addUtente(user) == null;
        } catch (Exception ecc) {
            ecc.printStackTrace();
            return false;
        }
    }
}
