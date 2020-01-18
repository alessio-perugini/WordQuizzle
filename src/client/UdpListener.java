package client;

import server.Settings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class UdpListener implements Runnable {
    private int udpPort;
    private RichiestaSfida richiestaSfida;
    public AtomicBoolean sfidaAnswered;

    public void setRispostaSfida(String rispostaSfida) {
        this.rispostaSfida = rispostaSfida;
    }

    private String rispostaSfida;

    public UdpListener(int uPort) {
        this.richiestaSfida = RichiestaSfida.getInstance();
        this.udpPort = uPort;
        this.rispostaSfida = "";
        this.sfidaAnswered = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        byte[] buffer = new byte[100];

        DatagramPacket rcvPacket = new DatagramPacket(buffer, buffer.length);
        //TODO check udp port
        try (DatagramSocket server = new DatagramSocket(udpPort, InetAddress.getByName(Settings.HOST_NAME))) {
            System.out.println("Server up!");

            while (true) {
                server.receive(rcvPacket);

                String msg = new String(rcvPacket.getData());
                System.out.println(msg.trim() + " ti vuole sfidare accetti (si/no): ");
                richiestaSfida.setSfidaAnswered(new AtomicBoolean(true));

                while (!this.sfidaAnswered.get()) ; //Aspetto fino a che non mi da una risposta

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

                richiestaSfida.setSfidaAnswered(new AtomicBoolean(false));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
