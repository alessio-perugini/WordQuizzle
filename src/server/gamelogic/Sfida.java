package server.gamelogic;

import server.Settings;
import server.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class Sfida {
    //private Utente userSfidante, userSfidato;
    private Partita partitaSfidante, partitaSfidato;
    private int idSfida, K_paroleDaInviare;
    private ArrayList<HashMap<String, String>> paroleDaIndovinare;

    public AtomicBoolean getTraduzioniGenerate() {
        if (traduzioniGenerate == null) traduzioniGenerate = new AtomicBoolean(false);
        return traduzioniGenerate;
    }

    public void setTraduzioniGenerate(AtomicBoolean traduzioniGenerate) {
        this.traduzioniGenerate = traduzioniGenerate;
    }

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

    public Partita getPartitaSfidato() {
        return partitaSfidato;
    }

    public Sfida(int idSfida) {
        Random rand = new Random();
        int wordToSend = rand.nextInt(Settings.MAX_PAROLE_DA_GENERARE);
        this.idSfida = idSfida;
        this.K_paroleDaInviare = (wordToSend == 0) ? Settings.MIN_PAROLE_DA_GENERARE : wordToSend;
        this.paroleDaIndovinare = Dizionario.getInstance().getNwordsFromDictionary(K_paroleDaInviare);
        generaTraduzioni();
    }

    public void setPartite(Partita pSfidante, Partita pSfidato) {
        if (pSfidante == null) this.partitaSfidante = pSfidante;
        if (pSfidato == null) this.partitaSfidato = pSfidato;
    }

    private void generaTraduzioni() {
        if (getTraduzioniGenerate().get()) return;

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
