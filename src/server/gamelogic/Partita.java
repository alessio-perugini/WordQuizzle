package server.gamelogic;

import server.Utente;

import java.sql.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Partita {
    private Utente user;
    private Timestamp inizioPartita, finePartita;
    private PriorityBlockingQueue<String[]> paroleDaIndovinare;

    public Partita(Utente user, Sfida sfida){
        this.user = user;
        sfida.generaTraduzioni();

        this.paroleDaIndovinare = sfida.getParoleDaIndovinare();
    }

    private PriorityBlockingQueue<String[]> createDeepCopy(ArrayList<HashMap<String,String>> wordToGuess){

    }

}
