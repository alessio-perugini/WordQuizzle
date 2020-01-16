package client;

import server.ListaUtenti;

import java.util.concurrent.atomic.AtomicBoolean;

public class RichiestaSfida {
    private static RichiestaSfida instance;

    public static synchronized RichiestaSfida getInstance(){
        if(instance == null) instance = new RichiestaSfida();
        return instance;
    }

    public AtomicBoolean getSfidaAnswered() {
        return sfidaAnswered;
    }

    public void setSfidaAnswered(AtomicBoolean sfidaAnswered) {
        this.sfidaAnswered = sfidaAnswered;
    }

    public AtomicBoolean sfidaAnswered;

    private RichiestaSfida(){
        sfidaAnswered = new AtomicBoolean(false);
    }
}
