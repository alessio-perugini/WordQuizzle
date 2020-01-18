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
    private Timestamp inizioPartita, finePartita;
    private ArrayList<HashMap<String, String>> paroleDaIndovinare;
    private int paroleTotali, sbagliate, corrette, nonRisposte;
    SocketChannel client;

    public Partita(Utente user, Sfida sfida) {
        this.user = user;
        createDeepCopyOfWordsToGuess(sfida.getParoleDaIndovinare());//TODO mesà che lo prendo diretatmente da sfida
        this.paroleTotali = this.paroleDaIndovinare.size();
        this.sbagliate = 0;
        this.corrette = 0;
        this.nonRisposte = 0;
        this.inizioPartita = new Timestamp(System.currentTimeMillis());
        this.finePartita = Utils.addSecondsToATimeStamp(this.inizioPartita, Settings.DURATA_PARTITA_SEC);
        this.client = (SocketChannel) user.getSelKey().channel();
    }

    private void createDeepCopyOfWordsToGuess(ArrayList<HashMap<String, String>> wordToGuess) {
        this.paroleDaIndovinare = (ArrayList<HashMap<String, String>>) wordToGuess.clone();
    }

    @Override
    public void run() {
        try {
            int i = 0;

            do {
                String parolaDaTradurre = ((String) paroleDaIndovinare.get(i).keySet().toArray()[0]);
                String traduzioneGiusta = paroleDaIndovinare.get(i).get(parolaDaTradurre);
                System.out.println(parolaDaTradurre + " -> " + traduzioneGiusta);
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
            int punteggioPartita = (this.corrette * 2) - this.sbagliate;
            user.addPunteggioPartita(punteggioPartita);

            String esitoPartita = String.format("Parole corrette: %d\n Parole errate: %d\n Parole non risposte: %d\n", this.corrette, this.sbagliate, this.nonRisposte);
            sendResponseToClient(esitoPartita);
            user.setInPartita(new AtomicBoolean(false));
        } catch (Exception ecc) {
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
