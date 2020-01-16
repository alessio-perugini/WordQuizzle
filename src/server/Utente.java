package server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import errori.FriendAlreadyExists;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Serializable;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

public class Utente implements Serializable {

    @JsonIgnore
    private int udpPort;
    @JsonIgnore
    private boolean connesso;
    private long punteggioTotale;
    private String nickname, password;
    private ConcurrentHashMap<String, String> listaAmici;
    /*@JsonIgnore
    private Socket client;*/

    public ByteBuffer getReadBuf() {
        if(readBuf == null) readBuf = ByteBuffer.allocate(1024);
        return readBuf;
    }

    @JsonIgnore
    private ByteBuffer readBuf;

    public SocketChannel getSocChannel() {
        return socChannel;
    }

    public void setSocChannel(SocketChannel socChannel) {
        this.socChannel = socChannel;
    }

    @JsonIgnore
    private SocketChannel socChannel;
    /*
    public void setClient(Socket client) {
        this.client = client;
    }

    public Socket getClient() {
        return client;
    }*/

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
        if(this.listaAmici == null) this.listaAmici = new ConcurrentHashMap<>();
        return listaAmici;
    }

    public boolean isConnesso() {
        return connesso;
    }

    public void setConnesso(boolean connesso) {
        this.connesso = connesso;
    }

    public Utente(){
        this.udpPort = Settings.UDP_PORT;
        this.connesso = false;
    }

    public Utente(SocketChannel sckChnl){
        this.udpPort = Settings.UDP_PORT;
        this.socChannel = sckChnl;
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

    public void addPunteggioPartita(int punteggio){
        this.punteggioTotale += punteggio;
    }

}
