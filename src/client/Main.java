package client;

import server.Settings;
import server.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Main {

    public static void main(String[] args) {
        int udpPort = (args.length > 0) ? Integer.parseInt(args[0]) : 50001;
        try{
            while(!Utils.udpPortAvailable(++udpPort));
        }catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }

        UdpListener udpSrv = new UdpListener(udpPort);
        Thread thUdpListner = new Thread(udpSrv);
        thUdpListner.start();

        BufferedReader consoleRdr = new BufferedReader(new InputStreamReader(System.in));


        try {
            SocketAddress address = new InetSocketAddress(InetAddress.getByName("localhost"), Settings.TCP_PORT);
            SocketChannel client = SocketChannel.open(address);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            boolean quit = false;
            String scelta = "";

            while(!quit){
                scelta = consoleRdr.readLine().trim();
                StringTokenizer tokenizedLine = new StringTokenizer(scelta);
                try{
                    switch (tokenizedLine.nextToken()){
                        case "quit":
                            client.close();
                            break;
                        case "registra_utente":
                            RmiClient rmiReg = new RmiClient();
                            String nickname = tokenizedLine.nextToken();
                            String pw = tokenizedLine.nextToken();
                            System.out.println((rmiReg.registra_utente(nickname, pw)) ? "Registrato": "Non registrato");
                            break;
                        case "login":
                            String nick = tokenizedLine.nextToken();
                            String password = tokenizedLine.nextToken();
                            scriviLeggi("LOGIN "+nick+" "+password+" " + udpPort, client);
                            break;
                        case "logout":
                            String nick2 = tokenizedLine.nextToken();
                            scriviLeggi("LOGOUT " + nick2, client);
                            break;
                        case "aggiungi_amico":
                            String u1 = tokenizedLine.nextToken();
                            String u2 = tokenizedLine.nextToken();
                            scriviLeggi("ADD_FRIEND "+u1+" "+u2+"", client);
                            break;
                        case "lista_amici":
                            String user1 = tokenizedLine.nextToken();
                            scriviLeggi("LISTA_AMICI " + user1, client);
                            break;
                        case "sfida":
                            String us1 = tokenizedLine.nextToken();
                            String us2 = tokenizedLine.nextToken();
                            scriviLeggi("SFIDA "+us1+" "+us2+"", client);
                            break;
                        case "mostra_punteggio":
                            String utente = tokenizedLine.nextToken();
                            scriviLeggi("MOSTRA_SCORE " + utente, client);
                            break;
                        case "mostra_classifica":
                            String utente1 = tokenizedLine.nextToken();
                            scriviLeggi("MOSTRA_CLASSIFICA " + utente1, client);
                            break;
                    }
                }catch (NoSuchElementException nse){
                    System.out.println(nse.getMessage());
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //TODO gestire la chiusura del thudp
        try{
            thUdpListner.interrupt();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void scriviLeggi(String messaggio, SocketChannel client) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            messaggio += "\n";
            byte[] mex = messaggio.getBytes(StandardCharsets.UTF_8);
            buffer.put(mex);
            buffer.flip();//Serve per far leggere dall'inizio al server
            client.write(buffer);

            buffer.clear();
            client.read(buffer);
            String response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
