package server.gamelogic;

import server.Utente;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Sfida {

    private Utente userSfidante, userSfidato;
    private Partita partitaSfidante, partitaSfidato;
    private int idSfida;
    private int K_paroleDaInviare;
    private PriorityBlockingQueue<String> paroleDaIndovinare;

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

    public Sfida(Utente userSfidante, Utente userSfidato){
        if(userSfidante == null || userSfidato == null) throw new IllegalArgumentException();

        Random rand = new Random();
        this.idSfida = rand.nextInt();
        this.K_paroleDaInviare = rand.nextInt(20);
        Dizionario.getInstance().getNwordsFromDictionary(K_paroleDaInviare);
        this.userSfidante = userSfidante;
        this.userSfidato = userSfidato;
    }


}
