package server;

import errori.UserAlreadyExists;
import errori.UserDoesntExists;
import server.storage.Storage;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class UtentiConnessi {

    private static UtentiConnessi instance;

    public ConcurrentHashMap<String, Utente> getHashListaUtenti() {
        return hashListaUtenti;
    }

    private ConcurrentHashMap<String, Utente>  hashListaUtenti;

    private UtentiConnessi(){
        hashListaUtenti = (ConcurrentHashMap<String, Utente>)Storage.getObjectFromJSONFile("utenti.json");
    }

    public static synchronized UtentiConnessi getInstance(){
        if(instance == null) instance = new UtentiConnessi();
        return instance;
    }

    public synchronized Utente addUtente(Utente user){
        if(user == null) throw new IllegalArgumentException();
        if(!hashListaUtenti.isEmpty() && hashListaUtenti.get(user.getNickname()) != null) throw new UserAlreadyExists();

        return hashListaUtenti.putIfAbsent(user.getNickname(), user);
    }

    public synchronized void removeUtente(Utente user){
        if(user == null) throw new IllegalArgumentException();
        if(hashListaUtenti.isEmpty()) throw new UserDoesntExists();

        hashListaUtenti.remove(user.getNickname());
    }

    public synchronized boolean isConnected(String key){
        if(key == null || key.length() == 0) throw new IllegalArgumentException();
        if(hashListaUtenti.isEmpty() || getUser(key) == null) throw new UserDoesntExists("L'utente non esiste");

        return hashListaUtenti.get(key).isConnesso();
    }

    public Utente getUser(String key){
        if(key == null || key.length() == 0) throw new IllegalArgumentException();
        if(hashListaUtenti.isEmpty()) throw new UserDoesntExists();

        return hashListaUtenti.get(key);
    }

    public synchronized void setConnected(String key, boolean value){
        if(key == null || key.length() == 0) throw new IllegalArgumentException();
        if(hashListaUtenti.isEmpty()) throw new UserDoesntExists();

        Utente profilo = hashListaUtenti.get(key);
        profilo.setConnesso(value);
        hashListaUtenti.replace(key, profilo);
    }
}
