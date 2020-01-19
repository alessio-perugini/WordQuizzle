package server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import errori.FriendAlreadyExists;

import java.io.Serializable;
import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public AtomicBoolean getInPartita() {
        if (inPartita == null) inPartita = new AtomicBoolean(false);
        return inPartita;
    }

    public void setInPartita(AtomicBoolean inPartita) {
        this.inPartita = inPartita;
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
    }

    public Utente(SelectionKey selectionKey) {
        this.udpPort = Settings.UDP_PORT;
        this.selKey = selectionKey;
    }

    public Utente(String nick, String password) {
        this.nickname = nick;
        this.password = password;
    }

    public synchronized void addFriend(String amico) {
        if (amico == null || amico.equals("")) throw new IllegalArgumentException("Nome amico non valido");
        if (listaAmici == null) listaAmici = new ConcurrentHashMap<>();
        if (listaAmici.get(amico) != null) throw new FriendAlreadyExists("Sei già amico");

        listaAmici.putIfAbsent(amico, amico);
    }

    public boolean isFriend(String amico) {
        if (amico == null || amico.equals("")) throw new IllegalArgumentException("Nome amico non valido");
        if (listaAmici == null) listaAmici = new ConcurrentHashMap<>();
        return (listaAmici.get(amico) != null);
    }

    public void addPunteggioPartita(int punteggio) {
        this.punteggioTotale += punteggio;
    }

}
