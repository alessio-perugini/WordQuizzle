package server;

import java.util.concurrent.ConcurrentHashMap;

public class Utente {

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private String nickname;
    private String password;
    private ConcurrentHashMap<String, Utente> listaAmici;

    public Utente(){

    }

    public Utente(String nick, String password){
        this.nickname = nick;
        this.password = password;
    }

}
