package server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import errori.FriendAlreadyExists;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class Utente implements Serializable {

    public String getNickname() {
        return nickname;
    }

    private String nickname, password;


    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    @JsonIgnore
    private int udpPort;
    public String getPassword() {
        return password;
    }

    public long getPunteggioTotale() {
        return punteggioTotale;
    }

    private long punteggioTotale;

    public ConcurrentHashMap<String, String> getListaAmici() {
        if(this.listaAmici == null) this.listaAmici = new ConcurrentHashMap<>();
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
        this.udpPort = Settings.UDP_PORT;
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

    public boolean isFriend(String amico){
        if(amico == null || amico.equals("")) throw new IllegalArgumentException("Nome amico non valido");
        return (listaAmici.get(amico) != null);
    }

}
