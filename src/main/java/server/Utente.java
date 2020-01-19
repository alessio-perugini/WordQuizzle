package server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import errori.FriendAlreadyExists;

import java.io.Serializable;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Gestisce tutte le info dell'utente quali il punteggio totale di ogni partita effettuata, se è loggato, la porta udp
 * la lista degli amici e la Selectionkey che verrà utilizzata dal threadpartita per gestire la comunicazione con
 * il gioco. La lista amici e la variabile inPartita devono essere garantite mutue esclusive in quanto più thread possono
 * accedervi ed effettuare modifiche.
 */
public class Utente implements Serializable {

    private long punteggioTotale;
    private String nickname, password;
    private ConcurrentHashMap<String, String> listaAmici;
    @JsonIgnore
    private int udpPort;
    @JsonIgnore
    private boolean connesso;
    @JsonIgnore
    private AtomicBoolean inPartita;
    @JsonIgnore
    private SelectionKey selKey; //Mi server per poter prendere il channel su cui scrivere

    public boolean getInPartita() {
        if (this.inPartita == null) this.inPartita = new AtomicBoolean(false);
        return inPartita.get();
    }

    public void setInPartita(boolean inPartita) {
        this.inPartita.set(inPartita);
    }

    public SelectionKey getSelKey() {
        return selKey;
    }

    public void setSelKey(SelectionKey selKey) {
        this.selKey = selKey;
    }

    public String getNickname() {
        return nickname;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    public String getPassword() {
        return password;
    }

    public long getPunteggioTotale() {
        return punteggioTotale;
    }

    public ConcurrentHashMap<String, String> getListaAmici() {
        if (this.listaAmici == null) this.listaAmici = new ConcurrentHashMap<>();
        return listaAmici;
    }

    public boolean isConnesso() {
        return connesso;
    }

    public void setConnesso(boolean connesso) {
        this.connesso = connesso;
    }

    public Utente() {
        this.udpPort = Settings.UDP_PORT;
        this.connesso = false;
        this.inPartita = new AtomicBoolean(false);
    }

    /**
     * Costruttore che inizializza udp port, partita e selectionKey. Usato nelal fase di registrazione del selector
     *
     * @param selectionKey
     */
    public Utente(SelectionKey selectionKey) {
        this.udpPort = Settings.UDP_PORT;
        this.selKey = selectionKey;
        this.inPartita = new AtomicBoolean(false);
    }

    /**
     * Costruttore che inizilizza solo nick e pw. Utilizzato dal client
     *
     * @param nick
     * @param password
     */
    public Utente(String nick, String password) { //lo usa il client
        this.inPartita = new AtomicBoolean(false);
        this.nickname = nick;
        this.password = password;
    }

    /**
     * Aggiungi amico alla lista
     *
     * @param amico
     */
    public synchronized void addFriend(String amico) {
        if (amico == null || amico.equals("")) throw new IllegalArgumentException("Nome amico non valido");
        if (listaAmici == null) listaAmici = new ConcurrentHashMap<>();
        if (listaAmici.get(amico) != null) throw new FriendAlreadyExists("Sei già amico");

        listaAmici.putIfAbsent(amico, amico);
    }

    /**
     * Controlla se il nickname passato per param è presente nella lista amici
     *
     * @param amico
     * @return
     */
    public synchronized boolean isFriend(String amico) {
        if (amico == null || amico.equals("")) throw new IllegalArgumentException("Nome amico non valido");
        if (listaAmici == null) listaAmici = new ConcurrentHashMap<>();
        return (listaAmici.get(amico) != null);
    }

    /**
     * Aggiungi il punteggio della partita al punteggio totale
     *
     * @param punteggio
     */
    public void addPunteggioPartita(int punteggio) {
        this.punteggioTotale += punteggio;
    }

}
