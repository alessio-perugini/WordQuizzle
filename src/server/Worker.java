package server;

import errori.*;
import server.gamelogic.ListaSfide;
import server.gamelogic.Partita;
import server.gamelogic.Sfida;
import server.storage.Storage;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Worker implements Runnable {

    private Utente socUser;

    public Worker(Socket c) {
        try{
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(c.getInputStream()));
            BufferedOutputStream outToClient = new BufferedOutputStream(c.getOutputStream());
            socUser = new Utente(c, inFromClient, outToClient);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String message;

            while ((message = socUser.getInFromClient().readLine()) != null) {
                StringTokenizer tokenizedLine = new StringTokenizer(message);
                String pw, nickUtente, nickAmico;
                try{
                    switch (tokenizedLine.nextToken()) {
                        case "LOGIN":
                            nickUtente = tokenizedLine.nextToken();
                            pw = tokenizedLine.nextToken();
                            String udpPort = tokenizedLine.nextToken();
                            this.socUser.setUdpPort(Integer.parseInt(udpPort));

                            try {
                                login(nickUtente, pw);
                            } catch (Exception e) {
                                e.printStackTrace();
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
                            nickUtente = tokenizedLine.nextToken();
                            nickAmico = tokenizedLine.nextToken();

                            try {
                                sfida(nickUtente, nickAmico);
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendResponseToClient(e.getMessage());
                            }
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
                }catch (NoSuchElementException nse){
                    nse.printStackTrace();
                }
            }

            socUser.getClient().close();
            if(socUser != null) ListaUtenti.getInstance().setConnected(socUser.getNickname(), false); //Se crasha lo disconnette
            socUser.getInFromClient().close();
            System.out.println("Client closed connection");
        } catch (IOException ecc) {
            ecc.printStackTrace();
        }
    }

    public void login(String nickUtente, String password) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (password == null || password.length() == 0) throw new IllegalArgumentException();
        if (ListaUtenti.getInstance().isConnected(nickUtente))
            throw new UserAlreadyLoggedIn("L'utente è già loggato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);

        if (profilo == null) throw new UserDoesntExists("L'utente inserito non esiste");
        if (!profilo.getPassword().equals(password)) throw new WrongPassword("Password errata");
        //associo il socket all'utente loggato
        ListaUtenti.getInstance().setConnected(nickUtente, true);
        profilo.setClient(this.socUser.getClient());
        profilo.setUdpPort(this.socUser.getUdpPort());
        profilo.setInFromClient(this.socUser.getInFromClient());
        profilo.setOutToClient(this.socUser.getOutToClient());
        this.socUser = profilo;
        sendResponseToClient("Login eseguito con successo");
    }

    public void logout(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (!ListaUtenti.getInstance().isConnected(nickUtente)) throw new UserAlreadyLoggedIn();

        ListaUtenti.getInstance().setConnected(nickUtente, false);
        this.socUser = null;
        sendResponseToClient("Logout eseguito con successo");
    }

    public void aggiungi_amico(String nickUtente, String nickAmico) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
        if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi essere amico di te stesso");

        ListaUtenti connectedUSers = ListaUtenti.getInstance();
        Utente profileRichiedente = connectedUSers.getUser(nickUtente);
        Utente profileAmico = connectedUSers.getUser(nickAmico);

        if (profileRichiedente == null || profileAmico == null) throw new FriendNotFound("Amico non trovato");
        if(!profileAmico.isConnesso()) throw new FriendNotConnected("L'amico deve essere connesso"); //TODO vedere
        profileRichiedente.addFriend(profileAmico.getNickname());
        profileAmico.addFriend(profileRichiedente.getNickname());

        sendResponseToClient("Amicizia " + nickUtente + "-" + nickAmico + " creata");
    }

    public void lista_amici(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        ConcurrentHashMap<String, String> listaAmici = profilo.getListaAmici();
        String listaAmiciAsJson = Storage.objectToJSON(listaAmici);
        sendResponseToClient(listaAmiciAsJson);
    }

    public void sfida(String nickUtente, String nickAmico) throws IOException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");
        if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
        if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi sfidare te stesso");
        Utente profiloUtente = ListaUtenti.getInstance().getUser(nickUtente);
        if(!profiloUtente.isFriend(nickAmico)) throw new FriendNotFound("L'utente che vuoi sfidare non è nella tua lista amici");
        Utente amico = ListaUtenti.getInstance().getUser(nickAmico);
        if (amico != null && !amico.isConnesso()) throw new FriendNotConnected("L'amico non è connesso");
        //TODO gestire le IOEXC dei thread per lanciare un custom err

        DatagramSocket udpClient = new DatagramSocket();
        udpClient.setSoTimeout(Settings.UDP_TIMEOUT);

        InetAddress address = InetAddress.getByName(Settings.HOST_NAME);
        String msg = nickUtente;
        byte[] msgSfida = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(msgSfida, msgSfida.length, address, amico.getUdpPort());
        udpClient.send(packet);

        byte[] ack = new byte[2];
        DatagramPacket rcvPck = new DatagramPacket(ack, ack.length);

        try{
            udpClient.receive(rcvPck);
            msg = new String(rcvPck.getData());
        }catch (SocketTimeoutException e){
            e.printStackTrace();
            udpClient.close();
            throw new NessunaRispostaDiSfida("L'amico non ha dato risposta.");
        }

        udpClient.close();

        if(msg.equals("no")) throw new SfidaRequestRefused("Ha rifiutato la sfida");
        sendResponseToClient(nickAmico + " ha accettato la sfida!");
        System.out.println("Sfida accettata");

        Sfida objSfida = new Sfida(profiloUtente, amico);
        ListaSfide sfide = ListaSfide.getInstance();
        sfide.addSfida(objSfida);

        Partita p1 = new Partita(profiloUtente, objSfida);
        Partita p2 = new Partita(amico, objSfida);
        Thread thPartita1 = new Thread(p1);//TODO vedere se creare un thread che fa pooling sulla struttura dati
        Thread thPartita2 = new Thread(p2);
        thPartita1.start();
        thPartita2.start();

        //TODO creare il codice della sfida
    }

    public void mostra_punteggio(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        sendResponseToClient("Punteggio: " + profilo.getPunteggioTotale());
    }

    public void mostra_classifica(String nickUtente) {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente errato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        ArrayList<Utente> classificaAmici = new ArrayList<>();
        classificaAmici.add(profilo);

        if(!profilo.getListaAmici().isEmpty()) { //se non ha amici ritorna solo il suo score saltando questo if
            for (String keyAmico : profilo.getListaAmici().values()) {
                Utente amico = ListaUtenti.getInstance().getUser(keyAmico);
                classificaAmici.add(amico);
            }
            classificaAmici.sort(Comparator.comparing(Utente::getPunteggioTotale).reversed());
        }
        //Serve per levare tutte le info extra dell'utente
        ArrayList<String> leaderBoardWithOnlyUserANdScore = new ArrayList<>();
        for(Utente user : classificaAmici){
            leaderBoardWithOnlyUserANdScore.add(user.getNickname() + " " + user.getPunteggioTotale());
        }
        sendResponseToClient(Storage.objectToJSON(leaderBoardWithOnlyUserANdScore));
    }

    private void sendResponseToClient(String testo) {
        if (testo == null) throw new IllegalArgumentException();
        if(socUser.getOutToClient() == null) throw new NullPointerException();

        try {
            socUser.getOutToClient().write((testo + "\n").getBytes(StandardCharsets.UTF_8), 0, testo.length() + 1);
            socUser.getOutToClient().flush();
        } catch (IOError | IOException ecc) {
            ecc.printStackTrace();
        }
    }
}