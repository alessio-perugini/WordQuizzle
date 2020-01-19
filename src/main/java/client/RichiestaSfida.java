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
     * Serve per vedere se devo rispondere ad una richiesta di sfida
     *
     * @return sfidaToAnswer
     */
    public AtomicBoolean getSfidaToAnswer() {
        return sfidaToAnswer;
    }

    /**
     * Serve per settare se devo rispondere o meno a richieste di sfida
     *
     * @param sfidaToAnswer
     */
    public void setSfidaToAnswer(AtomicBoolean sfidaToAnswer) {
        this.sfidaToAnswer = sfidaToAnswer;
    }

    /**
     * Utilizzata per segnalare se devo rispondere ad una richiesta di sfida
     */
    public AtomicBoolean sfidaToAnswer;

    public synchronized Timestamp getDataScadenzaRichiesta() {
        return dataScadenzaRichiesta;
    }

    /**
     * @param dataScadenzaRichiesta il tempo di quando scade la richiesta di sfida
     */
    public synchronized void setDataScadenzaRichiesta(Timestamp dataScadenzaRichiesta) {
        this.dataScadenzaRichiesta = dataScadenzaRichiesta;
    }

    /**
     * Utilizzata per vedere se ho esaurito il tempo di risposta alla sfida
     */
    private Timestamp dataScadenzaRichiesta;

    private RichiestaSfida() {
        sfidaToAnswer = new AtomicBoolean(false);
    }
}
