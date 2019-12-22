package server;

import errori.*;
import server.storage.Storage;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Worker implements Runnable {

    private Socket client;
    private BufferedOutputStream outToClient;
    private Utente socUser;

    public Worker(Socket c) {
        this.client = c;
    }

    public void run() {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            outToClient = new BufferedOutputStream(client.getOutputStream());

            String message;

            while ((message = inFromClient.readLine()) != null) {
                StringTokenizer tokenizedLine = new StringTokenizer(message);
                String pw, nickUtente, nickAmico;

                switch (tokenizedLine.nextToken()) {
                    case "LOGIN":
                        nickUtente = tokenizedLine.nextToken();
                        pw = tokenizedLine.nextToken();

                        try {
                            login(nickUtente, pw);
                        } catch (Exception e) {
                            sendResponseToClient(e.getMessage());
                        }
                        break;
                    case "LOGOUT":
                        nickUtente = tokenizedLine.nextToken();
                        try{
                            logout(nickUtente);
                        }catch (Exception e){
                            sendResponseToClient(e.getMessage());
                        }
                        break;
                    case "ADD_FRIEND":
                        nickUtente = tokenizedLine.nextToken();
                        nickAmico = tokenizedLine.nextToken();
                        try {
                            aggiungi_amico(nickUtente, nickAmico);
                        } catch (Exception e) {
                            sendResponseToClient(e.getMessage());
                        }
                        break;
                    case "LISTA_AMICI":
                        nickUtente = tokenizedLine.nextToken();
                        try{
                            lista_amici(nickUtente);
                        }catch (Exception e){
                            sendResponseToClient(e.getMessage());
                        }
                        break;
                    case "SFIDA":
                        //TODO UDP request
                        break;
                    case "MOSTRA_SCORE":
                        nickUtente = tokenizedLine.nextToken();
                        try{
                            mostra_punteggio(nickUtente);
                        }catch (Exception e){
                            sendResponseToClient(e.getMessage());
                        }
                        break;
                    case "MOSTRA_CLASSIFICA":
                        nickUtente = tokenizedLine.nextToken();
                        try{
                            mostra_classifica(nickUtente);
                        }catch (Exception e){
                            sendResponseToClient(e.getMessage());
                        }
                        break;
                }
            }

            client.close();
            UtentiConnessi.getInstance().setConnected(socUser.getNickname(), false); //Se crasha lo disconnette
            inFromClient.close();
            System.out.println("Client closed connection");
        } catch (IOException ecc) {
            ecc.printStackTrace();
        }
    }

    public void login(String nickUtente, String password) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (password == null || password.length() == 0) throw new IllegalArgumentException();
        if (UtentiConnessi.getInstance().isConnected(nickUtente))
            throw new UserAlreadyLoggedIn("L'utente è già loggato");

        Utente profilo = UtentiConnessi.getInstance().getUser(nickUtente);

        if (profilo == null) throw new UserDoesntExists("L'utente inserito non esiste");
        if (!profilo.getPassword().equals(password)) throw new WrongPassword("Password errata");

        UtentiConnessi.getInstance().setConnected(nickUtente, true);
        this.socUser = profilo;
        sendResponseToClient("Login eseguito con successo");
    }

    public void logout(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (!UtentiConnessi.getInstance().isConnected(nickUtente)) throw new UserAlreadyLoggedIn();

        UtentiConnessi.getInstance().setConnected(nickUtente, false);
        sendResponseToClient("Logout eseguito con successo");
    }

    public void aggiungi_amico(String nickUtente, String nickAmico) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
        if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi essere amico di te stesso");

        UtentiConnessi connectedUSers = UtentiConnessi.getInstance();
        Utente profileRichiedente = connectedUSers.getUser(nickUtente);
        Utente profileAmico = connectedUSers.getUser(nickAmico);

        if (profileRichiedente == null || profileAmico == null) throw new FriendNotFound("Amico non trovato");

        profileRichiedente.addFriend(profileAmico.getNickname());

        sendResponseToClient("Amicizia " + nickUtente + "-" + nickAmico + " creata");
    }

    public void lista_amici(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

        Utente profilo = UtentiConnessi.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        ConcurrentHashMap<String, String> listaAmici = profilo.getListaAmici();
        String listaAmiciAsJson = Storage.objectToJSON(listaAmici);
        sendResponseToClient(listaAmiciAsJson);
    }

    public void sfida(String nickUtente, String nickAmico) throws IOException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");
        if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
        if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi sfidare te stesso");
        Utente amico = UtentiConnessi.getInstance().getUser(nickAmico);
        if (amico != null && !amico.isConnesso()) throw new FriendNotConnected("L'amico non è connesso");
        //TODO gestire le IOEXC dei thread per lanciare un custom err

        DatagramSocket udpClient = new DatagramSocket(Settings.UDP_PORT);
        udpClient.setSoTimeout(20000);

        InetAddress address = InetAddress.getByName(Settings.HOST_NAME);
        String msg = "SFIDA " + nickUtente;
        byte[] msgSfida = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(msgSfida, msgSfida.length, address, Settings.UDP_PORT);
        udpClient.send(packet);

        byte[] ack = new byte[4];
        DatagramPacket rcvPck = new DatagramPacket(ack, ack.length);

        try{
            udpClient.receive(rcvPck);
            msg = new String(rcvPck.getData());
        }catch (SocketTimeoutException e){

        }

        udpClient.close();

        if(msg.equals("no")) throw new SfidaRequestRefused("Ha rifiutato la sfida");

        //TODO creare il codice della sfida
    }

    public void mostra_punteggio(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

        Utente profilo = UtentiConnessi.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        sendResponseToClient("Punteggio: " + profilo.getPunteggioTotale());
    }

    public void mostra_classifica(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

        Utente profilo = UtentiConnessi.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        ArrayList<Utente> classificaAmici = new ArrayList<>();
        classificaAmici.add(profilo);

        if(!profilo.getListaAmici().isEmpty()) { //se non ha amici ritorna solo il suo score saltando questo if
            for (Iterator<String> i = profilo.getListaAmici().values().iterator(); i.hasNext();) {
                String keyAmico = i.next();
                Utente amico = UtentiConnessi.getInstance().getUser(keyAmico);
                classificaAmici.add(amico);
            }

            classificaAmici.sort(Comparator.comparing(Utente::getPunteggioTotale).reversed());
        }

        sendResponseToClient(Storage.objectToJSON(classificaAmici));
    }

    private void sendResponseToClient(String testo) {
        if (testo == null) throw new IllegalArgumentException();

        try {
            outToClient.write((testo + "\n").getBytes(StandardCharsets.UTF_8), 0, testo.length() + 1);
            outToClient.flush();
        } catch (IOError | IOException ecc) {
            ecc.printStackTrace();
        }
    }
}