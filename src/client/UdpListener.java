package client;

import server.Settings;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpListener implements Runnable {
    private int udpPort;
    private RichiestaSfida richiestaSfida;
    public AtomicBoolean sfidaAnswered;
    private DatagramSocket server;

    public void setRispostaSfida(String rispostaSfida) {
        this.sfidaAnswered.set(true);
        this.rispostaSfida = rispostaSfida;
    }

    private String rispostaSfida;

    public UdpListener(int uPort) {
        this.richiestaSfida = RichiestaSfida.getInstance();
        this.udpPort = uPort;
        this.rispostaSfida = "";
        this.sfidaAnswered = new AtomicBoolean(false);
    }

    public void quit() {
        if (server != null) server.close();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[100];

        DatagramPacket rcvPacket = new DatagramPacket(buffer, buffer.length);
        //TODO check udp port
        try {
            server = new DatagramSocket(udpPort, InetAddress.getByName(Settings.HOST_NAME));
            while (!Thread.currentThread().isInterrupted()) {
                server.receive(rcvPacket);

                String msg = new String(rcvPacket.getData());
                System.out.println(msg.trim() + " ti vuole sfidare accetti (si/no): ");
                richiestaSfida.setSfidaToAnswer(new AtomicBoolean(true));

                while (!Thread.currentThread().isInterrupted() && !this.sfidaAnswered.get())
                    ; //Aspetto fino a che non mi da una risposta
                if (Thread.currentThread().isInterrupted()) return;

                String risposta = this.rispostaSfida;
                if (this.sfidaAnswered.get() && (risposta.equals("si") || risposta.equals("no"))) {
                    risposta = this.rispostaSfida;
                    this.sfidaAnswered.set(false);
                    byte[] ackBuf = risposta.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket ack = new DatagramPacket(ackBuf, ackBuf.length, InetAddress.getByName(Settings.HOST_NAME), rcvPacket.getPort());
                    server.send(ack);
                } else {//se sbaglia a scrivere si o no presuppongo sia no
                    this.rispostaSfida = "";
                }

                richiestaSfida.setSfidaToAnswer(new AtomicBoolean(false));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
