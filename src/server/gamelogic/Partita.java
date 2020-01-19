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
        return finita.get();
    }

    public void setFinita(boolean finita) {
        this.finita = new AtomicBoolean(finita);
    }

    public AtomicBoolean finita; //Se la partita è conclusa

    public Timestamp getFinePartita() {
        return finePartita;
    }

    public Timestamp finePartita; //Quando deve finire la partita
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
        this.paroleDaIndovinare = sfida.getParoleDaIndovinare();
        this.user = user;
        this.finita = new AtomicBoolean(false);
        this.paroleTotali = this.paroleDaIndovinare.size();
        this.sbagliate = 0;
        this.corrette = 0;
        this.nonRisposte = 0;
        this.punteggioPartita = 0;
        this.inizioPartita = new Timestamp(System.currentTimeMillis());
        this.finePartita = Utils.addSecondsToATimeStamp(this.inizioPartita, Settings.DURATA_PARTITA_SEC);
        this.client = (SocketChannel) user.getSelKey().channel();
    }

    private int i;
    @Override
    public void run() {
        try {
            i = 0;
            String prefix = String.format("Via alla sfida di traduzione!\nAvete %d secondi per tradurre correttamente %d parole.\n", Settings.DURATA_PARTITA_SEC, this.paroleTotali);
            do {
                String parolaDaTradurre = ((String) paroleDaIndovinare.get(i).keySet().toArray()[0]);
                String traduzioneGiusta = paroleDaIndovinare.get(i).get(parolaDaTradurre);
                String sendChallenge = String.format("Challenge %d/%d: %s", i + 1, this.paroleTotali, parolaDaTradurre);
                sendResponseToClient((i == 0) ? prefix + sendChallenge : sendChallenge);
                String parolaTradotta = readResponse();

                if (!Utils.isGivenTimeExpired(this.finePartita)) { //Se il tempo è scaduto non considera l'ultima read
                    if (parolaTradotta.equals(traduzioneGiusta.toLowerCase())) {
                        this.corrette++;
                    } else {
                        this.sbagliate++;
                    }
                }
            } while (!Utils.isGivenTimeExpired(this.finePartita) && (++i < this.paroleTotali));

            if (!finita.get()) {
                this.nonRisposte = this.paroleTotali - i;
                this.punteggioPartita = (this.corrette * 2) - this.sbagliate;
                user.addPunteggioPartita(this.punteggioPartita); //Aggiungi il punteggio all'utente

                String esitoPartita = String.format("Hai tradotto correttamente %d parole, ne hai sbagliate %d e non risposta a %d.\nHai totalizzato %d punti.", this.corrette, this.sbagliate, this.nonRisposte, this.punteggioPartita);

                sendResponseToClient(esitoPartita);
            }

            setFinita(true);
        } catch (Exception ecc) {
            setFinita(true);
            user.setInPartita(new AtomicBoolean(false));
            ecc.printStackTrace();
        }
    }

    public String esitoPartita() {
        this.nonRisposte = this.paroleTotali - i;
        this.punteggioPartita = (this.corrette * 2) - this.sbagliate;
        user.addPunteggioPartita(this.punteggioPartita); //Aggiungi il punteggio all'utente

        String esitoPartita = String.format("Hai tradotto correttamente %d parole, ne hai sbagliate %d e non risposta a %d.\nHai totalizzato %d punti.", this.corrette, this.sbagliate, this.nonRisposte, this.punteggioPartita);
        return esitoPartita;
    }

    private String readResponse() throws IOException {
        if (client == null) throw new NullPointerException();
        ByteBuffer msg = ByteBuffer.allocate(Settings.READ_BYTE_BUFFER_SIZE);
        long byteLeft;
        do {//finchè non legge la risposta dal client
            byteLeft = client.read(msg);
            if (byteLeft == -1) throw new IOException();
        } while (byteLeft == 0);
        return new String(msg.array()).toLowerCase().trim();
    }

    public void sendResponseToClient(String testo) {
        if (testo == null) throw new IllegalArgumentException();
        if (client == null) throw new NullPointerException();

        try {//Invia la risposta al client
            client.write(ByteBuffer.wrap((testo + "\n").getBytes(StandardCharsets.UTF_8)));
        } catch (IOError | IOException ecc) {
            ecc.printStackTrace();
        }
    }
}
