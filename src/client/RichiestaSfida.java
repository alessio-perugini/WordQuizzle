package client;

import java.util.concurrent.atomic.AtomicBoolean;

public class RichiestaSfida {
    private static RichiestaSfida instance;

    public static synchronized RichiestaSfida getInstance() {
        if (instance == null) instance = new RichiestaSfida();
        return instance;
    }

    public AtomicBoolean getSfidaToAnswer() {
        return sfidaToAnswer;
    }

    public void setSfidaToAnswer(AtomicBoolean sfidaToAnswer) {
        this.sfidaToAnswer = sfidaToAnswer;
    }

    public AtomicBoolean sfidaToAnswer;

    private RichiestaSfida() {
        sfidaToAnswer = new AtomicBoolean(false);
    }
}
