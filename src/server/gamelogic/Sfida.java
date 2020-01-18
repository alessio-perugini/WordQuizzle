package server.gamelogic;

import server.Utente;
import server.Utils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Sfida {
    private Utente userSfidante, userSfidato;
    private Partita partitaSfidante, partitaSfidato;
    private int idSfida, K_paroleDaInviare;
    private ArrayList<HashMap<String, String>> paroleDaIndovinare;
    private AtomicBoolean traduzioniGenerate;

    public ArrayList<HashMap<String, String>> getParoleDaIndovinare() {
        if (!traduzioniGenerate.get()) generaTraduzioni();

        return paroleDaIndovinare;
    }

    public int getIdSfida() {
        return idSfida;
    }

    public Partita getPartitaSfidante() {
        return partitaSfidante;
    }

    public void setPartitaSfidante(Partita partitaSfidante) {
        this.partitaSfidante = partitaSfidante;
    }

    public Partita getPartitaSfidato() {
        return partitaSfidato;
    }

    public void setPartitaSfidato(Partita partitaSfidato) {
        this.partitaSfidato = partitaSfidato;
    }

    public Sfida(Utente userSfidante, Utente userSfidato) {
        if (userSfidante == null || userSfidato == null) throw new IllegalArgumentException();

        Random rand = new Random();
        this.idSfida = userSfidante.hashCode() + rand.nextInt();
        this.K_paroleDaInviare = rand.nextInt(20);//TODO da sistemare
        this.paroleDaIndovinare = Dizionario.getInstance().getNwordsFromDictionary(K_paroleDaInviare);
        this.userSfidante = userSfidante;
        this.userSfidato = userSfidato;
        this.traduzioniGenerate = new AtomicBoolean(false);
    }

    public void generaTraduzioni() {
        if (this.userSfidante == null || this.userSfidato == null)
            throw new IllegalArgumentException("L'amico non ha ancora accettato la sfida");
        if (traduzioniGenerate.get()) return;

        for (Iterator<HashMap<String, String>> elm = this.paroleDaIndovinare.iterator(); elm.hasNext(); ) {
            try {
                HashMap<String, String> elemento = elm.next();
                Object[] keys = elemento.keySet().toArray();
                elemento.replace((String) keys[0], Utils.sendHttpRequest((String) keys[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.traduzioniGenerate.set(true);
    }
}
