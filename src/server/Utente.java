package server;

import errori.FriendAlreadyExists;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;
    public String getNickname() {
        return nickname;
    }

    private String nickname;

    public String getPassword() {
        return password;
    }

    private String password;

    public long getPunteggioTotale() {
        return punteggioTotale;
    }

    private long punteggioTotale;

    public ConcurrentHashMap<String, String> getListaAmici() {
        return listaAmici;
    }

    private ConcurrentHashMap<String, String> listaAmici;

    public boolean isConnesso() {
        return connesso;
    }

    public void setConnesso(boolean connesso) {
        this.connesso = connesso;
    }

    private boolean connesso;

    public Utente(){

    }

    public Utente(String nick, String password){
        this.nickname = nick;
        this.password = password;
    }

    public synchronized void addFriend(String amico){
        if(amico == null || amico.equals("")) throw new IllegalArgumentException("Nome amico non valido");
        if(listaAmici == null) listaAmici = new ConcurrentHashMap<>();
        if(listaAmici.get(amico) != null) throw new FriendAlreadyExists("Sei già amico");

        listaAmici.putIfAbsent(amico, amico);
    }

}
