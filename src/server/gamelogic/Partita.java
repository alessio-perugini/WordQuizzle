package server.gamelogic;

import server.ListaUtenti;
import server.Settings;
import server.Utente;
import server.Utils;

import java.io.IOError;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Partita implements Runnable {
    private Utente user;
    private Timestamp inizioPartita, finePartita;
    private ArrayList<HashMap<String,String>> paroleDaIndovinare;
    private int paroleTotali, sbagliate, corrette, nonRisposte;


    public Partita(Utente user, Sfida sfida){
        this.user = user;
        sfida.generaTraduzioni();
        createDeepCopyOfWordsToGuess(sfida.getParoleDaIndovinare());
        this.paroleTotali = this.paroleDaIndovinare.size();
        this.sbagliate = 0;
        this.corrette = 0;
        this.nonRisposte = 0;
        this.inizioPartita = new Timestamp(System.currentTimeMillis());
        this.finePartita = Utils.addSecondsToATimeStamp(this.inizioPartita, Settings.DURATA_PARTITA_SEC);
    }

    private void createDeepCopyOfWordsToGuess(ArrayList<HashMap<String,String>> wordToGuess){
        this.paroleDaIndovinare = (ArrayList<HashMap<String,String>>)wordToGuess.clone();
    }

    @Override
    public void run() {
        try {
            int i = 0;
            String parolaDaTradurre = ((String)paroleDaIndovinare.get(i).keySet().toArray()[0]);
            String sendChallenge = String.format("Challenge %d/%d: %s", i + 1, this.paroleTotali, parolaDaTradurre);
            sendResponseToClient(sendChallenge);

            String parolaTradotta;

            while (!Utils.isGivenTimeExpired(this.finePartita) && (parolaTradotta = user.getInFromClient().readLine()) != null) {

                if(parolaTradotta.equals(paroleDaIndovinare.get(i).get(parolaDaTradurre))){
                    this.corrette += 2;
                }else{
                    this.sbagliate++;
                }
                i++;
                parolaDaTradurre = ((String)paroleDaIndovinare.get(i).keySet().toArray()[0]);
                sendChallenge = String.format("Challenge %d/%d: %s", i + 1, this.paroleTotali, parolaDaTradurre);
                sendResponseToClient(sendChallenge);
            }
            this.nonRisposte = this.paroleTotali - i;
            int punteggioPartita = this.corrette - this.sbagliate;
            user.addPunteggioPartita(punteggioPartita);
            String esitoPartita = String.format("Parole corrette: %d\n Parole errate: %d\n Parole non risposte: %d\n", this.corrette, this.sbagliate, this.nonRisposte);
            sendResponseToClient(esitoPartita);
        } catch (IOException ecc) {
            ecc.printStackTrace();
        }
    }

    private void sendResponseToClient(String testo) {
        if (testo == null) throw new IllegalArgumentException();
        if(user.getOutToClient() == null) throw new NullPointerException();

        try {
            user.getOutToClient().write((testo + "\n").getBytes(StandardCharsets.UTF_8), 0, testo.length() + 1);
            user.getOutToClient().flush();
        } catch (IOError | IOException ecc) {
            ecc.printStackTrace();
        }
    }
}
