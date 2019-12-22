package client;

import server.Settings;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UdpListener implements Runnable {
    public UdpListener() {

    }

    @Override
    public void run() {
        byte[] buffer = new byte[100];

        DatagramPacket rcvPacket = new DatagramPacket(buffer, buffer.length);

        try(DatagramSocket server = new DatagramSocket(Settings.UDP_PORT)){
            System.out.println("Server up!");

            while(true){
                server.receive(rcvPacket);

                String msg = new String(rcvPacket.getData());
                System.out.println("Server ti ha inviato una richiesta di sfida" + msg);

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
