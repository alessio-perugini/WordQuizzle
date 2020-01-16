package server.gamelogic;

import server.ListaUtenti;
import server.Settings;
import server.Utente;
import server.Utils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
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
    SocketChannel client;

    public Partita(Utente user, Sfida sfida) throws IOException {
        this.user = user;
        sfida.generaTraduzioni();
        createDeepCopyOfWordsToGuess(sfida.getParoleDaIndovinare());
        this.paroleTotali = this.paroleDaIndovinare.size();
        this.sbagliate = 0;
        this.corrette = 0;
        this.nonRisposte = 0;
        this.inizioPartita = new Timestamp(System.currentTimeMillis());
        this.finePartita = Utils.addSecondsToATimeStamp(this.inizioPartita, Settings.DURATA_PARTITA_SEC);
        this.client = (SocketChannel)user.getSelKey().channel();
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

            ByteBuffer msg = ByteBuffer.allocate(1024);
            long byteLeftToRead = client.read(msg);
            if (byteLeftToRead == -1) throw  new IOException();
            String parolaTradotta = new String(msg.array());
            boolean firstTime = true;

            while (!Utils.isGivenTimeExpired(this.finePartita)/* && (parolaTradotta = inFromClient.readLine()) != null*/) {
                if(!firstTime){
                    byteLeftToRead = client.read(msg);
                    if (byteLeftToRead == -1) throw  new IOException();
                    parolaTradotta = new String(msg.array());
                }else{
                    firstTime = false;
                }

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
        if (client == null) throw new NullPointerException();

        try {
            client.write(ByteBuffer.wrap((testo + "\n").getBytes(StandardCharsets.UTF_8)));
        } catch (IOError | IOException ecc) {
            ecc.printStackTrace();
        }
    }
}
