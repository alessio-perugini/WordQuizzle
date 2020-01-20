package server;

import errori.UserAlreadyExists;
import errori.UserDoesntExists;
import server.storage.Storage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Lista degli utenti registrati, carica in memoria tutti gli utenti letti dal json, poi se un utente si logga viene
 * associato all'utente caricato dal json aggiungendogli il socket. utilizza il singleton pattern. Utilizzo una
 * concurrentHashMap poichè dentro la strutura dati potrebbero accedere i thread di rmi, partita, sfida e il selector
 * ed ognuno di questi può scrivere su una proprietà dell'oggeto Profilo che prendono dalla lista, quindi ho la necessità
 * di garantire la mutua esclusione.
 */
public class ListaUtenti {

    private static ListaUtenti instance;

    public ConcurrentHashMap<String, Utente> getHashListaUtenti() {
        return hashListaUtenti;
    }

    private ConcurrentHashMap<String, Utente> hashListaUtenti;

    private ListaUtenti() {
        hashListaUtenti = (ConcurrentHashMap<String, Utente>) Storage.getObjectFromJSONFile(Settings.JSON_FILENAME);
    }

    public static synchronized ListaUtenti getInstance() {
        if (instance == null) instance = new ListaUtenti();
        return instance;
    }

    /**
     * Aggiunge un utente alla lista
     *
     * @param user
     * @return null se non è presente nella lista, il puntatore a Utente se esiste
     */
    public synchronized Utente addUtente(Utente user) {
        if (user == null) throw new IllegalArgumentException();
        if (!hashListaUtenti.isEmpty() && hashListaUtenti.get(user.getNickname()) != null)
            throw new UserAlreadyExists("L'utente esiste già");

        return hashListaUtenti.putIfAbsent(user.getNickname(), user);
    }

    /**
     * Rimuove l'utente dalla lista
     *
     * @param user
     */
    public synchronized void removeUtente(Utente user) {
        if (user == null) throw new IllegalArgumentException();
        if (hashListaUtenti.isEmpty()) throw new UserDoesntExists();

        hashListaUtenti.remove(user.getNickname());
    }

    /**
     * Mi dice se l'utente è connesso
     *
     * @param key nickname dell'utente
     * @return true se è connesso
     */
    public synchronized boolean isConnected(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException();
        if (hashListaUtenti.isEmpty() || getUser(key) == null) throw new UserDoesntExists("L'utente non esiste");

        return hashListaUtenti.get(key).isConnesso();
    }

    /**
     * Ottiene l'utente
     *
     * @param key nickname dell'utente
     * @return puntatore a l'utente trovato
     */
    public synchronized Utente getUser(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException();
        if (hashListaUtenti.isEmpty()) throw new UserDoesntExists();

        return hashListaUtenti.get(key);
    }

    /**
     * Consente di settare online o meno l'utente passando come paramtreo true o false
     *
     * @param key   nickname
     * @param value true se voglio settarlo come connesso
     */
    public synchronized void setConnected(String key, boolean value) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException();
        if (hashListaUtenti.isEmpty()) throw new UserDoesntExists();

        Utente profilo = hashListaUtenti.get(key);
        profilo.setConnesso(value);
        hashListaUtenti.replace(key, profilo);
    }
}
