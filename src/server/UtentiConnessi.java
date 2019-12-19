package server;

import errori.UserAlreadyExists;

import java.util.concurrent.ConcurrentHashMap;

public class UtentiConnessi {

    private static UtentiConnessi instance;

    private ConcurrentHashMap<String, Utente>  hashListaUtenti;

    private UtentiConnessi(){
        hashListaUtenti = new ConcurrentHashMap<>();
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
        return hashListaUtenti.get(key) != null;
    }

    public Utente getUser(String key){
        if(key == null || key.length() == 0) throw new IllegalArgumentException();
        return hashListaUtenti.get(key);
    }
}
