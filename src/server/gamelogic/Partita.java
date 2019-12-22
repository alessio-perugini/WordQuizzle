package server.gamelogic;

import server.Utente;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Partita {
    private Utente user;
    private Timestamp inizioPartita, finePartita;
    private ArrayList<HashMap<String,String>> paroleDaIndovinare;

    public Partita(Utente user, Sfida sfida){
        this.user = user;
        sfida.generaTraduzioni();
        createDeepCopyOfWordsToGuess(sfida.getParoleDaIndovinare());
    }

    private void createDeepCopyOfWordsToGuess(ArrayList<HashMap<String,String>> wordToGuess){
        this.paroleDaIndovinare = (ArrayList<HashMap<String,String>>)wordToGuess.clone();
    }

}
