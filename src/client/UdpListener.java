package client;

import server.Settings;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UdpListener implements Runnable {
    int udpPort;
    public UdpListener(int uPort) {
        this.udpPort = uPort;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[100];

        DatagramPacket rcvPacket = new DatagramPacket(buffer, buffer.length);
        //TODO check udp port
        try(DatagramSocket server = new DatagramSocket(udpPort, InetAddress.getByName(Settings.HOST_NAME))){
            System.out.println("Server up!");

            while(true){
                server.receive(rcvPacket);

                String msg = new String(rcvPacket.getData());
                System.out.println(msg + " ti vuole sfidare accetti (si/no): ");

                String risposta = "si"; //TODO parametrizzare
                byte[] ackBuf = risposta.getBytes(StandardCharsets.UTF_8);
                DatagramPacket ack = new DatagramPacket(ackBuf, ackBuf.length, InetAddress.getByName(Settings.HOST_NAME), rcvPacket.getPort());
                server.send(ack);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
