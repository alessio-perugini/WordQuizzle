package server;

import errori.UserAlreadyExists;
import server.storage.Storage;

import java.util.concurrent.ConcurrentHashMap;

public class UtentiConnessi {

    private static UtentiConnessi instance;

    private ConcurrentHashMap<String, Utente>  hashListaUtenti;

    private UtentiConnessi(){
        hashListaUtenti = (ConcurrentHashMap<String, Utente>)Storage.getObjectFromJSONFile("utenti.json");
    }

    public static synchronized UtentiConnessi getInstance(){
        if(instance == null) instance = new UtentiConnessi();
        return instance;
    }

    public synchronized void addUtente(Utente user){
        if(user == null) throw new IllegalArgumentException();
        if(hashListaUtenti.get(user.getNickname()) != null) throw new UserAlreadyExists();

        hashListaUtenti.putIfAbsent(user.getNickname(), user);

    }

    public synchronized void removeUtente(Utente user){
        if(user == null) throw new IllegalArgumentException();
        hashListaUtenti.remove(user.getNickname());
    }

    public synchronized boolean isConnected(String key){
        if(key == null || key.length() == 0) throw new IllegalArgumentException();
        return hashListaUtenti.get(key).isConnesso();
    }

    public Utente getUser(String key){
        if(key == null || key.length() == 0) throw new IllegalArgumentException();
        return hashListaUtenti.get(key);
    }

    public synchronized void setConnected(String key, boolean value){
        if(key == null || key.length() == 0) throw new IllegalArgumentException();

        Utente profilo = hashListaUtenti.get(key);
        profilo.setConnesso(value);
        hashListaUtenti.replace(key, profilo);
    }
}
