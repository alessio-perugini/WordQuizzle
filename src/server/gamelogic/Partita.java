package server.gamelogic;

import server.Settings;
import server.Utente;
import server.Utils;

import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Partita implements Runnable {
    public Utente getUser() {
        return user;
    }

    private Utente user;
    public Timestamp inizioPartita;

    public boolean isFinita() {
        return finita;
    }

    public boolean finita;

    public Timestamp getFinePartita() {
        return finePartita;
    }

    public Timestamp finePartita;
    private ArrayList<HashMap<String, String>> paroleDaIndovinare;
    private int paroleTotali;
    private int sbagliate;
    private int corrette;
    private int nonRisposte;

    public int getPunteggioPartita() {
        return punteggioPartita;
    }

    private int punteggioPartita;
    SocketChannel client;

    public Partita(Utente user, Sfida sfida) {
        createDeepCopyOfWordsToGuess(sfida.getParoleDaIndovinare());//TODO mesà che lo prendo diretatmente da sfida
        this.user = user;
        this.finita = false;
        this.paroleTotali = this.paroleDaIndovinare.size();
        this.sbagliate = 0;
        this.corrette = 0;
        this.nonRisposte = 0;
        this.punteggioPartita = 0;
        this.inizioPartita = new Timestamp(System.currentTimeMillis());
        this.finePartita = Utils.addSecondsToATimeStamp(this.inizioPartita, Settings.DURATA_PARTITA_SEC);
        this.client = (SocketChannel) user.getSelKey().channel();
    }

    private void createDeepCopyOfWordsToGuess(ArrayList<HashMap<String, String>> wordToGuess) {
        this.paroleDaIndovinare = wordToGuess;//(ArrayList<HashMap<String, String>>) wordToGuess.clone();
    }

    @Override
    public void run() {
        try {
            int i = 0;

            do {
                String parolaDaTradurre = ((String) paroleDaIndovinare.get(i).keySet().toArray()[0]);
                String traduzioneGiusta = paroleDaIndovinare.get(i).get(parolaDaTradurre);
                String sendChallenge = String.format("Challenge %d/%d: %s", i + 1, this.paroleTotali, parolaDaTradurre);
                sendResponseToClient(sendChallenge);
                String parolaTradotta = readResponse();

                if (!Utils.isGivenTimeExpired(this.finePartita)) {
                    if (parolaTradotta.equals(traduzioneGiusta.toLowerCase())) {
                        this.corrette++;
                    } else {
                        this.sbagliate++;
                    }
                }
            } while (!Utils.isGivenTimeExpired(this.finePartita) && (++i < this.paroleTotali));

            this.nonRisposte = this.paroleTotali - i;
            this.punteggioPartita = (this.corrette * 2) - this.sbagliate;
            user.addPunteggioPartita(this.punteggioPartita);

            String esitoPartita = String.format("Parole corrette: %d\n Parole errate: %d\n Parole non risposte: %d\n", this.corrette, this.sbagliate, this.nonRisposte);
            sendResponseToClient(esitoPartita);
            this.finita = true;
        } catch (Exception ecc) {
            this.finita = true;
            user.setInPartita(new AtomicBoolean(false));
            ecc.printStackTrace();
        }
    }

    private String readResponse() throws IOException {
        if (client == null) throw new NullPointerException();
        ByteBuffer msg = ByteBuffer.allocate(1024);
        long byteLeft = 0;
        do {
            byteLeft = client.read(msg);
            if (byteLeft == -1) throw new IOException();
        } while (byteLeft == 0);
        return new String(msg.array()).toLowerCase().trim();
    }

    public void sendResponseToClient(String testo) {
        if (testo == null) throw new IllegalArgumentException();
        if (client == null) throw new NullPointerException();

        try {
            client.write(ByteBuffer.wrap((testo + "\n").getBytes(StandardCharsets.UTF_8)));
        } catch (IOError | IOException ecc) {
            ecc.printStackTrace();
        }
    }
}
