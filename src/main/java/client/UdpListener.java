package client;

import server.Settings;
import server.Utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utilizzato per avviare il thread che si mette in ascolto su una porta udp per ricevere e gesstire le richieste di sfida
 */
public class UdpListener implements Runnable {
    private int udpPort; //porta udp su cui stare in ascolto per le sfide
    private RichiestaSfida richiestaSfida; //struttura dati per sincronizzarmi con il client Main
    public AtomicBoolean sfidaAnswered; //Se ha risposto alla sfida dalla console
    private DatagramSocket server;

    public void setRispostaSfida(String rispostaSfida) {
        this.sfidaAnswered.set(true); //Setto la variabile che mi fa uscire dall'attesa della risposta dalla console
        this.rispostaSfida = rispostaSfida; //Ottengo l'esito (si/no))
    }

    private String rispostaSfida;

    /**
     * Inizzializza le varibili glovali
     *
     * @param uPort porta udp su cui mettere in ascolto il server
     */
    public UdpListener(int uPort) {
        this.richiestaSfida = RichiestaSfida.getInstance(); //Struttura dati di sync con la console main
        this.udpPort = uPort;
        this.rispostaSfida = "";
        this.sfidaAnswered = new AtomicBoolean(false);
    }

    public void quit() { //se voglio uscire dal client devi uscire da una possibile lettura sul socket udp
        if (server != null) server.close();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[100]; //Preparo il DPacket per la lettura
        DatagramPacket rcvPacket = new DatagramPacket(buffer, buffer.length);

        try {//Apro il server udp
            server = new DatagramSocket(udpPort, InetAddress.getByName(Settings.HOST_NAME));
            while (!Thread.currentThread().isInterrupted()) { //Se non ho richieste di uscire dal client continuo
                server.receive(rcvPacket); //In lettura per un pacchetto

                String nomeSfidante = new String(rcvPacket.getData()); //trasformo la risposta (il nome dello sfidante) in stringa
                System.out.println(nomeSfidante.trim() + " ti vuole sfidare accetti (si/no): ");
                Timestamp scadenza = Utils.addSecondsToATimeStamp(new Timestamp(System.currentTimeMillis()), Settings.UDP_TIMEOUT / 1000);
                richiestaSfida.setDataScadenzaRichiesta(scadenza);
                richiestaSfida.setSfidaToAnswer(new AtomicBoolean(true)); //Serve per gestire il prossimo input da console come risposta alla sfida

                while (!Thread.currentThread().isInterrupted() && !this.sfidaAnswered.get())
                    ; //Aspetto fino a che non mi da una risposta
                if (Thread.currentThread().isInterrupted())
                    return; //Se il client vuole uscire chiudo subito senza inviare la risposta al server

                String risposta = this.rispostaSfida;
                if (this.sfidaAnswered.get() && (risposta.equals("si") || risposta.equals("no"))) {
                    risposta = this.rispostaSfida;
                    this.sfidaAnswered.set(false);
                    byte[] ackBuf = risposta.getBytes(StandardCharsets.UTF_8);//Preparo il pacchetto da inviare al clientTcp
                    DatagramPacket ack = new DatagramPacket(ackBuf, ackBuf.length, InetAddress.getByName(Settings.HOST_NAME), rcvPacket.getPort());
                    server.send(ack); //invia il pacchetto di risposta della sfida
                } else {//se sbaglia a scrivere si/no presuppongo sia no ma lascia che il client udp riceva il timeout
                    this.rispostaSfida = "";
                }
                //Riabilita la gestione della console da parte delle altre funzionalità non inerenti alla risposta di sfida
                richiestaSfida.setSfidaToAnswer(new AtomicBoolean(false));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
