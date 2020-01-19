package server;

import server.gamelogic.ListaSfide;
import server.gamelogic.Partita;
import server.gamelogic.Sfida;

import java.util.Iterator;

/**
 * Gestisce la terminazione di una partita dovuta alla scadenza o al termine anticipato della sfida. Fa pooling sulla
 * ListaSfide e controlla che se è finito il tempo ad entrambi i giocatori notifica ad entrambi chi a vinto.
 * Se uno dei 2 giocatori non risponde all'ultimo messaggio del server dopo che è scaduto il tempo, questo consente di
 * liberare l'altro giocatore che ha terminato prima della fine, notificandolo così della eventuale vittoria e
 * liberandolo dalla read dell'esito della partita. Se l'utente non ha finito tutte le risposte in tempo mando 1 sola
 * write che contiene statistiche della partita + esito finale.
 */
public class WorkerSfida implements Runnable {
    ListaSfide lsSfide;

    public WorkerSfida() {
        lsSfide = ListaSfide.getInstance();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) { //Se non è interrotto
            Iterator<Sfida> it = lsSfide.getHashListaSfide().values().iterator();
            while (it.hasNext()) { //Controllo se ci sono sfide
                Sfida sfida = it.next();
                if (sfida == null) continue;

                Partita p1 = sfida.getPartitaSfidante();
                Partita p2 = sfida.getPartitaSfidato();
                if (p1 == null || p2 == null)
                    continue;//Se non sono null controllo se è finito il tempo o entrambi gli utenti hanno finito la partita
                if (!(Utils.isGivenTimeExpired(p1.getFinePartita()) && Utils.isGivenTimeExpired(p2.getFinePartita())) && !(p1.isFinita() && p2.isFinita()))
                    continue;
                Utils.log("/!\\ Invio messaggio fine partita");

                sendStatsIfNotEndedBeforeExpire(p1);
                sendStatsIfNotEndedBeforeExpire(p2);

                if (p1.getPunteggioPartita() > p2.getPunteggioPartita()) {//vince p1
                    p1.getUser().addPunteggioPartita(Settings.PUNTI_EXTRA);
                    p1.sendResponseToClient(String.format("Congratulazioni, hai vinto! Hai guadagnato %s punti extra, per un totale di %d punti!#", Settings.PUNTI_EXTRA, p1.getPunteggioPartita() + Settings.PUNTI_EXTRA));
                    p2.sendResponseToClient(String.format("Hai perso! Hai totalizzato %d punti!#", p2.getPunteggioPartita()));
                } else if (p1.getPunteggioPartita() < p2.getPunteggioPartita()) {//vince p2
                    p2.getUser().addPunteggioPartita(Settings.PUNTI_EXTRA);
                    p1.sendResponseToClient(String.format("Hai perso! Hai totalizzato %d punti!#", p1.getPunteggioPartita()));
                    p2.sendResponseToClient(String.format("Congratulazioni, hai vinto! Hai guadagnato %s punti extra, per un totale di %d punti!#", Settings.PUNTI_EXTRA, p2.getPunteggioPartita() + Settings.PUNTI_EXTRA));
                } else {//pareggiano
                    String pareggio = String.format("Hai pareggiato con %s punti!#", p1.getPunteggioPartita());
                    p1.sendResponseToClient(pareggio);
                    p2.sendResponseToClient(pareggio);
                }
                p1.getUser().setInPartita(false);
                p2.getUser().setInPartita(false);
                lsSfide.removeSfida(sfida);
                Utils.log("Sfida terminata!");
                it.remove();
            }
        }
    }

    /**
     * Se l'utente ha finito tutte le risposte in tempo mando le statisiche della partita
     *
     * @param p
     */
    private void sendStatsIfNotEndedBeforeExpire(Partita p) {
        if (p.isFinita()) return;
        p.setFinita(true);
        p.sendResponseToClient(p.esitoPartita());
    }
}
