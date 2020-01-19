package server.rmi;

import errori.InvalidPassword;
import errori.UserAlreadyExists;
import server.ListaUtenti;
import server.Utente;

import java.rmi.server.RemoteServer;

/**
 * Servizio RMI che consente la registrazione dell'utente
 */
public class Registrazione extends RemoteServer implements RegistrazioneService {
    private static final long serialVersionUID = 1L;

    private ListaUtenti lsUtenti;

    public Registrazione() {
        lsUtenti = ListaUtenti.getInstance();
    }

    /**
     * @param nickName l'username dell'utente
     * @param password la password
     * @return true se non ah avuto problemi eccezione se ha riscontrato un qualsiasi problema
     * @throws IllegalArgumentException se il nick non sono validi
     * @throws UserAlreadyExists        Se l'utente già esiste
     * @throws InvalidPassword          Se la password non è valida
     */
    @Override
    public boolean registra_utente(String nickName, String password) throws IllegalArgumentException, UserAlreadyExists, InvalidPassword {
        if (nickName == null || nickName.length() == 0) throw new IllegalArgumentException("nickname non valido");
        if (password == null || password.length() < 6) throw new InvalidPassword("Password non valida");

        Utente user = new Utente(nickName, password);
        if (this.lsUtenti.addUtente(user) != null)
            throw new UserAlreadyExists("Il nickname già inserito è stato preso");

        return true;
    }
}
