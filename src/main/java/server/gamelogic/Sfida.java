package server.gamelogic;

import server.Settings;
import server.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Sfida tiene traccia della sfida associata ai 2 utenti, si occupa di generare la K parole da
 * indovinare assicurandosi compreso da un max e min che può essere settato dalle config. Ogni parola viene poi
 * 1 ad 1 inviata all'api rest per ricevere la traduzione popolando così la HashMap con parola italiana-inglese
 * Una volta che ha finito di prendere le traduzioni dall'api si può iniziare la partita.
 */
public class Sfida {
    private Partita partitaSfidante, partitaSfidato;
    private int idSfida;
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
        Random rand = new Random();//sceglie quante parole deve generare
        int wordToSend = rand.nextInt(Settings.MAX_PAROLE_DA_GENERARE);
        this.idSfida = idSfida;
        int k_paroleDaInviare = Math.max(wordToSend, Settings.MIN_PAROLE_DA_GENERARE);
        this.paroleDaIndovinare = Dizionario.getInstance().getNwordsFromDictionary(k_paroleDaInviare);
        generaTraduzioni();
    }

    public void setPartite(Partita pSfidante, Partita pSfidato) {
        if (this.partitaSfidante == null) this.partitaSfidante = pSfidante;
        if (this.partitaSfidato == null) this.partitaSfidato = pSfidato;
    }

    /**
     * Genera le traduzioni inglese per le parole già presenti nel'hashmap
     */
    private void generaTraduzioni() {
        for (HashMap<String, String> elemItaEng : this.paroleDaIndovinare) {
            try {//Aggiungo le coppie della traduzione data dall'api con sendHttpRequest
                Object[] keys = elemItaEng.keySet().toArray();
                elemItaEng.replace((String) keys[0], Utils.sendHttpRequest((String) keys[0]));
                Utils.log(String.format("Sfida (%d): %s -> %s", this.idSfida, elemItaEng.keySet().toArray()[0], elemItaEng.values().toArray()[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
