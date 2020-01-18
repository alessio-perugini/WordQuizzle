package server.gamelogic;

import server.Settings;
import server.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Sfida {
    //private Utente userSfidante, userSfidato;
    private Partita partitaSfidante, partitaSfidato;
    private int idSfida, K_paroleDaInviare;
    private ArrayList<HashMap<String, String>> paroleDaIndovinare;

    public ArrayList<HashMap<String, String>> getParoleDaIndovinare() {
        if (paroleDaIndovinare == null) generaTraduzioni();

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
        this.K_paroleDaInviare = Math.max(wordToSend, Settings.MIN_PAROLE_DA_GENERARE);
        this.paroleDaIndovinare = Dizionario.getInstance().getNwordsFromDictionary(K_paroleDaInviare);
        generaTraduzioni();
    }

    public void setPartite(Partita pSfidante, Partita pSfidato) {
        if (this.partitaSfidante == null) this.partitaSfidante = pSfidante;
        if (this.partitaSfidato == null) this.partitaSfidato = pSfidato;
    }

    private void generaTraduzioni() {
        for (Iterator<HashMap<String, String>> elm = this.paroleDaIndovinare.iterator(); elm.hasNext(); ) {
            try {
                HashMap<String, String> elemento = elm.next();
                Object[] keys = elemento.keySet().toArray();
                elemento.replace((String) keys[0], Utils.sendHttpRequest((String) keys[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
