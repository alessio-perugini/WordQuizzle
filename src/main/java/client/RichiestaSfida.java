package client;

import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RichiestaSfida serve per sincronizzare la console con il server udp, così quando arrivano le richieste di sfida
 * tramite questa classe il server udp setta la variabile sfidaToAnser a true, il serverUdp printa a schermo la richiesta
 * di sfida e presuppone che il prossimo input da tastiera sia relativo alla risposta di sfida. Grazie a questa struttura
 * mi posso sincronizzare ed andare direttamente alla gestione della risposta di sfida
 */
public class RichiestaSfida {
    /**
     * unica istanza valida di RichiestaSFida Singleton pattern
     */
    private static RichiestaSfida instance;

    public static synchronized RichiestaSfida getInstance() {
        if (instance == null) instance = new RichiestaSfida();
        return instance;
    }

    /**
     * Serve per
     *
     * @return sfidaToAnswer
     */
    public AtomicBoolean getSfidaToAnswer() {
        return sfidaToAnswer;
    }

    public void setSfidaToAnswer(AtomicBoolean sfidaToAnswer) {
        this.sfidaToAnswer = sfidaToAnswer;
    }

    public AtomicBoolean sfidaToAnswer;

    public synchronized Timestamp getDataScadenzaRichiesta() {
        return dataScadenzaRichiesta;
    }

    public synchronized void setDataScadenzaRichiesta(Timestamp dataScadenzaRichiesta) {
        this.dataScadenzaRichiesta = dataScadenzaRichiesta;
    }

    private Timestamp dataScadenzaRichiesta;

    private RichiestaSfida() {
        sfidaToAnswer = new AtomicBoolean(false);
    }
}
