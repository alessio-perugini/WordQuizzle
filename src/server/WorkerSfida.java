package server;

import server.gamelogic.ListaSfide;
import server.gamelogic.Partita;
import server.gamelogic.Sfida;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerSfida implements Runnable {
    ListaSfide lsSfide;

    public WorkerSfida(){
        lsSfide = ListaSfide.getInstance();
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()){
            Iterator it = lsSfide.getHashListaSfide().values().iterator();
            while (it.hasNext()){
                Sfida sfida =  (Sfida)it.next();
                if(sfida == null) continue;

                Partita p1 = sfida.getPartitaSfidante();
                Partita p2 = sfida.getPartitaSfidato();
                if(p1 == null || p2 == null) continue;
                if(!(p1.isFinita() && p2.isFinita())) continue;
                //if(!(Utils.isGivenTimeExpired(p1.getFinePartita()) && Utils.isGivenTimeExpired(p2.getFinePartita()))) continue;
                Utils.log("/!\\ Invio messaggio fine partita");
                if(p1.getPunteggioPartita() > p2.getPunteggioPartita()){//vince p1
                    p1.sendResponseToClient(String.format("Hai vinto con %s punti! %s ha totalizzato %s punti.", p1.getPunteggioPartita(), p2.getUser().getNickname(), p2.getPunteggioPartita()));
                    p2.sendResponseToClient(String.format("Hai perso con %s punti! %s ha totalizzato %s punti.", p2.getPunteggioPartita(), p1.getUser().getNickname(), p1.getPunteggioPartita()));
                }else if(p1.getPunteggioPartita() < p2.getPunteggioPartita()){//vince p2
                    p1.sendResponseToClient(String.format("Hai perso con %s punti! %s ha totalizzato %s punti.", p1.getPunteggioPartita(), p2.getUser().getNickname(), p2.getPunteggioPartita()));
                    p2.sendResponseToClient(String.format("Hai vinto con %s punti! %s ha totalizzato %s punti.", p2.getPunteggioPartita(), p1.getUser().getNickname(), p1.getPunteggioPartita()));
                }else {//pareggiano
                    String pareggio = String.format("Hai pareggiato con %s punti!", p1.getPunteggioPartita());
                    p1.sendResponseToClient(pareggio);
                    p2.sendResponseToClient(pareggio);
                }
                p1.getUser().setInPartita(new AtomicBoolean(false));
                p2.getUser().setInPartita(new AtomicBoolean(false));
                lsSfide.removeSfida(sfida);
                Utils.log("Sfida terminata!");
                it.remove();
            }
        }
    }
}
