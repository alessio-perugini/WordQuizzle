package server;

import errori.*;
import server.gamelogic.ListaSfide;
import server.gamelogic.Partita;
import server.gamelogic.Sfida;
import server.storage.Storage;

import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private Utente socUser;
    ExecutorService ex;
    Selector selector;
    SocketChannel socChanClient;
    ServerSocketChannel serverSckChnl;

    public void start() { //TODO gestire eccezione
        try {
            ex = Executors.newFixedThreadPool(Settings.N_THREADS_THREAD_POOL);
            serverSckChnl = ServerSocketChannel.open();
            serverSckChnl.socket().bind(new InetSocketAddress(InetAddress.getByName("localhost"), Settings.TCP_PORT));
            serverSckChnl.configureBlocking(false);

            int ops = serverSckChnl.validOps();
            selector = Selector.open();
            serverSckChnl.register(selector, ops, null);
            Utils.SalvaSuFileHandleSIGTERM(ex);
            gestoreSfide();

            while (true) {
                selector.selectNow();

                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    // rimuove la chiave dal Selected Set, ma non dal registered Set
                    try {
                        if (key.isAcceptable()) {
                            keyAcceptableRegister(selector);
                        } else if (key.isReadable()) {
                            keyRead(key);
                        } else if (key.isWritable()) {
                            keyWrite(key);
                        }
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                        Object[] objClient = (Object[]) key.attachment();
                        if (objClient.length >= 2) {
                            Utente uCrash = (Utente) objClient[1];
                            Utils.log("crashato " + uCrash.getNickname(), uCrash);
                        }
                        key.channel().close();
                        key.cancel();
                        crashClient();
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void crashClient() {
        if (socUser != null && socUser.getNickname() != null) {
            ListaUtenti.getInstance().setConnected(socUser.getNickname(), false); //Se crasha lo disconnette
            socUser = null;
        }

        Utils.log("Client closed connection");
    }

    public void keyAcceptableRegister(Selector selector) throws IOException {
        SocketChannel client = serverSckChnl.accept();
        client.configureBlocking(false);
        SelectionKey sk = client.register(selector, SelectionKey.OP_READ);
        ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
        ByteBuffer msg = ByteBuffer.allocate(1024);
        ByteBuffer[] bfs = {length, msg};
        socUser = new Utente(sk);
        Object[] objClient = {bfs, socUser};
        sk.attach(objClient);
        Utils.log(client.getRemoteAddress().toString());
    }

    public void keyWrite(SelectionKey key) throws IOException {
        socChanClient = (SocketChannel) key.channel();
        Object[] respObj = (Object[]) key.attachment();
        String response = (String) respObj[0];
        ByteBuffer respBuf = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
        socChanClient.write(respBuf);

        if (!respBuf.hasRemaining()) {
            respBuf.clear();
            ByteBuffer length = ByteBuffer.allocate(Integer.BYTES);
            ByteBuffer msg = ByteBuffer.allocate(1024);
            ByteBuffer[] bfs = {length, msg};
            Object[] objClient = {bfs, this.socUser};
            socChanClient.register(selector, SelectionKey.OP_READ, objClient);
        }
    }

    public void keyRead(SelectionKey key) throws IOException, BufferUnderflowException {
        socChanClient = (SocketChannel) key.channel();
        Object[] objClient = (Object[]) key.attachment();
        ByteBuffer[] bfs = (ByteBuffer[]) objClient[0];
        long byteLeft = socChanClient.read(bfs);
        if (byteLeft == -1) throw new IOException();

        byte[] bytes;
        ByteBuffer msgBuf = bfs[1];
        bfs[0].flip();
        int l = 0;
        if (bfs[0].hasRemaining()) {
            l = bfs[0].getInt();
        }

        StringBuilder message = new StringBuilder();

        while (byteLeft > 0) { //TODO da fixare
            msgBuf.flip();
            bytes = new byte[msgBuf.remaining()];
            msgBuf.get(bytes);
            message.append(new String(bytes));
            msgBuf.clear();
            byteLeft = socChanClient.read(msgBuf);
            if (byteLeft == -1) throw new IOException();
        }
        this.socUser = (Utente) objClient[1];

        if (!message.toString().isEmpty()) messageParser(message.toString());
    }

    public void messageParser(String message) throws ClosedChannelException {
        StringTokenizer tokenizedLine = new StringTokenizer(message);
        String pw, nickUtente, nickAmico;
        if (socUser.getInPartita().get()) return; //se è in partita ignora le richieste di altri comandi!

        try {
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
                    try {
                        logout(nickUtente);
                    } catch (Exception e) {
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
                    try {
                        lista_amici(nickUtente);
                    } catch (Exception e) {
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
                        if (socUser.getInPartita().get()) socUser.setInPartita(new AtomicBoolean(false));
                        Utente amico = ListaUtenti.getInstance().getUser(nickAmico);
                        if (amico != null && amico.getInPartita().get()) amico.setInPartita(new AtomicBoolean(false));
                        sendResponseToClient(e.getMessage());
                    }
                    break;
                case "MOSTRA_SCORE":
                    nickUtente = tokenizedLine.nextToken();
                    try {
                        mostra_punteggio(nickUtente);
                    } catch (Exception e) {
                        sendResponseToClient(e.getMessage());
                    }
                    break;
                case "MOSTRA_CLASSIFICA":
                    nickUtente = tokenizedLine.nextToken();
                    try {
                        mostra_classifica(nickUtente);
                    } catch (Exception e) {
                        sendResponseToClient(e.getMessage());
                    }
                    break;
                default:
                    break;
            }
        } catch (NoSuchElementException nse) {
            nse.printStackTrace();
        }
    }

    public void login(String nickUtente, String password) throws IOException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (password == null || password.length() == 0) throw new IllegalArgumentException();
        if (ListaUtenti.getInstance().isConnected(nickUtente))
            throw new UserAlreadyLoggedIn("L'utente è già loggato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);

        if (profilo == null) throw new UserDoesntExists("L'utente inserito non esiste");
        if (!profilo.getPassword().equals(password)) throw new WrongPassword("Password errata");

        //associo il socket all'utente loggato
        ListaUtenti.getInstance().setConnected(nickUtente, true);
        profilo.setSelKey(this.socUser.getSelKey());
        profilo.setUdpPort(this.socUser.getUdpPort());
        this.socUser = profilo;
        Utils.log(nickUtente + " loggato!", this.socUser);

        sendResponseToClient("Login eseguito con successo");
    }

    public void logout(String nickUtente) throws ClosedChannelException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (!ListaUtenti.getInstance().isConnected(nickUtente)) throw new UserAlreadyLoggedIn();

        ListaUtenti.getInstance().setConnected(nickUtente, false);
        sendResponseToClient("Logout eseguito con successo");
        this.socUser = new Utente(this.socUser.getSelKey());
        Utils.log(nickUtente + " logout!", this.socUser);
    }

    public void aggiungi_amico(String nickUtente, String nickAmico) throws ClosedChannelException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
        if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi essere amico di te stesso");

        ListaUtenti connectedUSers = ListaUtenti.getInstance();
        Utente profileRichiedente = connectedUSers.getUser(nickUtente);
        Utente profileAmico = connectedUSers.getUser(nickAmico);

        if (profileRichiedente == null || profileAmico == null) throw new FriendNotFound("Amico non trovato");
        if (!profileAmico.isConnesso()) throw new FriendNotConnected("L'amico deve essere connesso");
        profileRichiedente.addFriend(profileAmico.getNickname());
        profileAmico.addFriend(profileRichiedente.getNickname());

        sendResponseToClient("Amicizia " + nickUtente + "-" + nickAmico + " creata");
        Utils.log(String.format("%s ha aggiunto %s alla lista amici (%s) , (%s)", nickUtente, nickAmico, Utils.getIpRemoteFromProfile(profileRichiedente), Utils.getIpRemoteFromProfile(profileAmico)));
    }

    public void lista_amici(String nickUtente) throws ClosedChannelException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        ConcurrentHashMap<String, String> listaAmici = profilo.getListaAmici();
        String listaAmiciAsJson = Storage.objectToJSON(listaAmici);
        sendResponseToClient(listaAmiciAsJson);
        Utils.log(String.format("%s %s ha chiesto di vedere la lista amici di %s", this.socUser.getNickname(), Utils.getIpRemoteFromProfile(this.socUser), nickUtente), profilo);
    }

    public void sfida(String nickUtente, String nickAmico) throws IOException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");
        if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
        if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi sfidare te stesso");
        Utente profiloUtente = ListaUtenti.getInstance().getUser(nickUtente);
        if (!profiloUtente.isFriend(nickAmico))
            throw new FriendNotFound("L'utente che vuoi sfidare non è nella tua lista amici");
        Utente amico = ListaUtenti.getInstance().getUser(nickAmico);
        if (amico != null && !amico.isConnesso()) throw new FriendNotConnected("L'amico non è connesso");
        if (amico.getInPartita().get()) throw new FriendIsAlreadyPlaying("L'amico è già in una partita");

        profiloUtente.setInPartita(new AtomicBoolean(true));

        DatagramSocket udpClient = new DatagramSocket();
        udpClient.setSoTimeout(Settings.UDP_TIMEOUT);

        InetAddress address = InetAddress.getByName(Settings.HOST_NAME);
        String msg = nickUtente;
        byte[] msgSfida = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(msgSfida, msgSfida.length, address, amico.getUdpPort());
        udpClient.send(packet);

        byte[] ack = new byte[2];
        DatagramPacket rcvPck = new DatagramPacket(ack, ack.length);

        try {
            udpClient.receive(rcvPck);
            msg = new String(rcvPck.getData());
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            udpClient.close();
            throw new NessunaRispostaDiSfida("L'amico non ha dato risposta.");
        }

        udpClient.close();

        if (msg.equals("no")) {
            profiloUtente.setInPartita(new AtomicBoolean(false));
            throw new SfidaRequestRefused("Ha rifiutato la sfida");
        }
        //sendResponseToClient(nickAmico + " ha accettato la sfida!");
        socChanClient.write(ByteBuffer.wrap((nickAmico + " ha accettato la sfida!" + "\n").getBytes(StandardCharsets.UTF_8)));
        amico.setInPartita(new AtomicBoolean(true));

        Sfida objSfida = new Sfida(profiloUtente.getNickname().hashCode() + new Random().nextInt(4));
        Partita partitaSfidante = new Partita(profiloUtente, objSfida);
        Partita partitaAmico = new Partita(amico, objSfida);
        objSfida.setPartite(partitaSfidante, partitaAmico);
        ListaSfide.getInstance().addSfida(objSfida);

        ex.execute(partitaSfidante);
        ex.execute(partitaAmico);
        Utils.log(String.format("%s ha sfidato %s (%s) , (%s)", nickUtente, nickAmico, Utils.getIpRemoteFromProfile(profiloUtente), Utils.getIpRemoteFromProfile(amico)));
    }

    public void mostra_punteggio(String nickUtente) throws ClosedChannelException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        sendResponseToClient("Punteggio: " + profilo.getPunteggioTotale());
        Utils.log(String.format("%s %s ha chiesto di vedere il punteggio di %s", this.socUser.getNickname(), Utils.getIpRemoteFromProfile(this.socUser), nickUtente), profilo);
    }

    public void mostra_classifica(String nickUtente) throws ClosedChannelException {
        if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente errato");

        Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
        if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

        ArrayList<Utente> classificaAmici = new ArrayList<>();
        classificaAmici.add(profilo);

        if (!profilo.getListaAmici().isEmpty()) { //se non ha amici ritorna solo il suo score saltando questo if
            for (String keyAmico : profilo.getListaAmici().values()) {
                Utente amico = ListaUtenti.getInstance().getUser(keyAmico);
                classificaAmici.add(amico);
            }
            classificaAmici.sort(Comparator.comparing(Utente::getPunteggioTotale).reversed());
        }
        //Serve per levare tutte le info extra dell'utente
        ArrayList<String> leaderBoardWithOnlyUserANdScore = new ArrayList<>();
        for (Utente user : classificaAmici) {
            leaderBoardWithOnlyUserANdScore.add(user.getNickname() + " " + user.getPunteggioTotale());
        }
        sendResponseToClient(Storage.objectToJSON(leaderBoardWithOnlyUserANdScore));
        Utils.log(String.format("%s %s ha chiesto di vedere la classifica di %s", this.socUser.getNickname(), Utils.getIpRemoteFromProfile(this.socUser), nickUtente), profilo);
    }

    private void sendResponseToClient(String testo) throws ClosedChannelException {
        if (testo == null) throw new IllegalArgumentException();
        if (socChanClient == null) throw new NullPointerException();

        Object[] objResponse = {testo + "\n", this.socUser};
        socChanClient.register(selector, SelectionKey.OP_WRITE, objResponse);
    }

    private void gestoreSfide() {
        WorkerSfida workerSfida = new WorkerSfida();
        Thread thGestoreSfide = new Thread(workerSfida);
        thGestoreSfide.start();
    }
}
