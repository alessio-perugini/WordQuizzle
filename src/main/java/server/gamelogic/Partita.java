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

/**
 * Partita è un thread che gestisce le interazioni con il gioco. Tiene traccia dei punteggi, di quante domande mancano
 * della risposta e di scrivere la domanda da tradurre al client. Quando il thread di questa classe è in esecuzione
 * prende il controllo delle interazioni lettura e scrittura ed il selector associato al'utente di questa partita
 * rifiuterà tutti i comandi (che non dovrebbe essere possibile inviare)
 */
public class Partita implements Runnable {
    public Utente getUser() {
        return user;
    }

    /**
     * L'utente con cui si svolge la partita
     */
    private Utente user;

    /**
     * Il tempo d'inizio della partita
     */
    public Timestamp inizioPartita;

    public boolean isFinita() {
        return finita.get();
    }

    public void setFinita(boolean finita) {
        this.finita.set(finita);
    }

    /**
     * Serve per capire se la partita è conclusa. E' di tipo Atomicboolean per garantire che 1 solo thread alla volta
     * possa accedere a questo dato in quanto si potrebbe generare una concorrenza ed è necessario garantire il risultato
     * corretto, altrimenti si potrebbe tenere l'utente bloccato in stati non voluti e costringerlo alla chiusura forzata
     */
    public AtomicBoolean finita;

    public Timestamp getFinePartita() {
        return finePartita;
    }

    /**
     * Tempo di quando deve finire la partita viene settato dal costrutture
     */
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

    /**
     * Gestisce il gioco, utilizzando scrittura-lettura al primo ciclo invia il prefix che è il messaggio di benvenuto
     * poi inverà solamente le parole da tradurre. Se l'utente non risponde si rimane bloccati sulla read, qualora
     * l'utente invia una risposta ed il tempo è scaduto non controllo se la parola sia giusta per non alterare i
     * contatori ed esco dal ciclo. Se esco dal ciclo prima che il tempo sia finito allora stampo la statistica
     * delle parole indovinate e non altrimenti se quando esco dal ciclo il tempo è scaduto ci pensa il thread che fa
     * pooling sulla Lista della sfide a mandarmi con 1 solta write sia le statistiche che l'esito del vincitore
     */
    @Override
    public void run() {
        try {
            int i = 0;
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

            if (!finita.get()) sendResponseToClient(esitoPartita());

            setFinita(true);
        } catch (Exception ecc) {
            setFinita(true);
            user.setInPartita(false);
            ecc.printStackTrace();
        }
    }

    /**
     * utilizzato anche dal thread che fa spooling per mandare con 1 sola write queste statistiche e l'esito
     * del vincitore
     *
     * @return ritorna le statistiche dela partita
     */
    public String esitoPartita() {
        this.nonRisposte = this.paroleTotali - (this.corrette + this.sbagliate);
        this.punteggioPartita = (this.corrette * 2) - this.sbagliate;
        user.addPunteggioPartita(this.punteggioPartita); //Aggiungi il punteggio all'utente

        return String.format("Hai tradotto correttamente %d parole, ne hai sbagliate %d e non risposta a %d.\nHai totalizzato %d punti.", this.corrette, this.sbagliate, this.nonRisposte, this.punteggioPartita);
    }

    /**
     * Rimango in read finchè non ricevo dei dati
     *
     * @return il messaggio ricevuto
     * @throws IOException
     */
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

    /**
     * Invia la risposta al giocatore
     *
     * @param testo da inviare al giocatore
     */
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
