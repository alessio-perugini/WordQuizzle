package server;

import Errori.FriendAlreadyExists;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

@JsonIgnoreProperties(value = { "nickname" })
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
    private long punteggioTotale;
    private ConcurrentHashMap<String, String> listaAmici;

    public Utente(){

    }

    public Utente(String nick, String password){
        this.nickname = nick;
        this.password = password;
    }

    public synchronized void addFriend(String amico){
        if(amico == null || amico.equals("")) throw new IllegalArgumentException();
        if(listaAmici.get(amico) != null) throw new FriendAlreadyExists("Sei già amico");

        listaAmici.putIfAbsent(amico, amico);
    }

}
