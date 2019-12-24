package client;

import server.Settings;
import server.Utente;
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

    static int udpPort = 50001;
    static Utente profiloLoggato = null;
    public static void main(String[] args) {
        udpPort = (args.length > 0) ? Integer.parseInt(args[0]) : 50001;
        try {
            while (!Utils.udpPortAvailable(++udpPort)) ;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        BufferedReader consoleRdr = new BufferedReader(new InputStreamReader(System.in));

        UdpListener udpSrv = new UdpListener(udpPort, consoleRdr);
        Thread thUdpListner = new Thread(udpSrv);
        thUdpListner.start();

        try {
            SocketAddress address = new InetSocketAddress(InetAddress.getByName("localhost"), Settings.TCP_PORT);
            SocketChannel client = SocketChannel.open(address);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            boolean quit = false;
            String scelta = "";

            while (!quit) {
                scelta = consoleRdr.readLine().trim();
                StringTokenizer tokenizedLine = new StringTokenizer(scelta);
                try {
                    switch (tokenizedLine.nextToken()) {
                        case "quit":
                            client.close();
                            break;
                        case "registra_utente":
                            registrazione(tokenizedLine, client);
                            break;
                        case "login":
                            login(tokenizedLine, client);
                            break;
                        case "logout":
                            logout(client);
                            break;
                        case "aggiungi_amico":
                            aggiungiAmico(tokenizedLine, client);
                            break;
                        case "lista_amici":
                            listaAmici(client);
                            break;
                        case "sfida":
                            sfida(tokenizedLine, client);
                            break;
                        case "mostra_punteggio":
                            punteggio(client);
                            break;
                        case "mostra_classifica":
                            classifica(client);
                            break;
                        case "--help":
                            System.out.println("usage : COMMAND [ ARGS ...]\n" +
                                    "Commands: \n" +
                                    "registra_utente <nickUtente > <password > registra l' utente \n" +
                                    "login <nickUtente > <password > effettua il login logout effettua il logout \n" +
                                    "aggiungi_amico <nickAmico> crea relazione di amicizia con nickAmico lista_amici mostra la lista dei propri amici \n" +
                                    "sfida <nickAmico > richiesta di una sfida a nickAmico \n" +
                                    "mostra_punteggio mostra il punteggio dell’utente \n" +
                                    "mostra_classifica mostra una classifica degli amici dell’utente (incluso l’utente stesso)");
                            break;
                        default:
                            System.out.println("Comando non trovato, per la lista di comanda digitare (--help)");
                            break;
                    }
                } catch (NoSuchElementException nse) {
                    System.out.println(nse.getMessage());
                }

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //TODO gestire la chiusura del thudp
        try {
            thUdpListner.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void login(StringTokenizer tokenizedLine, SocketChannel client) {
        try{
            String nick = tokenizedLine.nextToken();
            String password = tokenizedLine.nextToken();

            if(profiloLoggato != null) logout(client); //Se logga un altro utente effettua il logout

            String esito = scriviLeggi("LOGIN " + nick + " " + password + " " + udpPort, client);

            if(esito.equals("Login eseguito con successo")) profiloLoggato = new Utente(nick, password);
        }catch (NoSuchElementException e){
            System.out.println("login <nickUtente > <password > effettua il login");
        }

    }

    public static void logout(SocketChannel client) {
        try{
            if(profiloLoggato == null){
                System.out.println("Logout già effetuato");
                return;
            }
            String esito = scriviLeggi("LOGOUT " + profiloLoggato.getNickname(), client);
            if(esito.equals("Logout eseguito con successo")) profiloLoggato = null;
        }catch (NoSuchElementException e){
            System.out.println("logout effettua il logout");
        }

    }

    public static void registrazione(StringTokenizer tokenizedLine, SocketChannel client) {
        try{
            RmiClient rmiReg = new RmiClient();
            String nickname = tokenizedLine.nextToken();
            String pw = tokenizedLine.nextToken();
            System.out.println((rmiReg.registra_utente(nickname, pw)) ? "Registrato" : "Non registrato");
        }catch (NoSuchElementException e){
            System.out.println("registra_utente <nickUtente > <password >");
        }
    }

    public static void sfida(StringTokenizer tokenizedLine, SocketChannel client) {
        try{
            String amico = tokenizedLine.nextToken();
            if(profiloLoggato == null){
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            scriviLeggi("SFIDA " + profiloLoggato.getNickname() + " " + amico + "", client);
        }catch (NoSuchElementException e){
            System.out.println("sfida <nickAmico > richiesta di una sfida a nickAmico");
        }
    }

    public static void classifica(SocketChannel client) {
        try{
            if(profiloLoggato == null){
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            scriviLeggi("MOSTRA_CLASSIFICA " + profiloLoggato.getNickname(), client);
        }catch (NoSuchElementException e){
            System.out.println("mostra_classifica mostra una classifica degli amici dell’utente");
        }
    }

    public static void punteggio(SocketChannel client) {
        try{
            if(profiloLoggato == null){
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            scriviLeggi("MOSTRA_SCORE " + profiloLoggato.getNickname(), client);
        }catch (NoSuchElementException e){
            System.out.println("mostra_punteggio mostra il punteggio dell’utente");
        }
    }

    public static void aggiungiAmico(StringTokenizer tokenizedLine, SocketChannel client) {
        try{
            String amico = tokenizedLine.nextToken();
            if(profiloLoggato == null){
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            scriviLeggi("ADD_FRIEND " + profiloLoggato.getNickname() + " " + amico + "", client);
        }catch (NoSuchElementException e){
            System.out.println("aggiungi_amico <nickAmico> crea relazione di amicizia con nickAmico");
        }
    }

    public static void listaAmici(SocketChannel client) {
        try{
            if(profiloLoggato == null){
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            scriviLeggi("LISTA_AMICI " + profiloLoggato.getNickname(), client);
        }catch (NoSuchElementException e){
            System.out.println("lista_amici mostra la lista dei propri amici");
        }
    }

    public static String scriviLeggi(String messaggio, SocketChannel client) {
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
            return response;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
