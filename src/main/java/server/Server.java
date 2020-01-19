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

/**
 * Server tcp che gestisce i client, il salvataggio e le partite. Utilizza un ThreadPool per gestire le partite degli
 * utenti. Un thread WorkerSfida che fa pooling sulla ListaSfide e se vede che una sfida è scaduta notifica i client
 * e lo leva dalla lista delle sfide in esecuzione. Utilizza un selector per gestire i client che si collegano. Se un
 * utente sta sfidando o riceve la sfida viene settata una variabile che mostra l'utente in gioco in maniera preventiva
 * da scartare così eventuali richieste che altri utenti possono mandare a questi in attesa della risposta di sfida,
 * evitando così di far rimanere il client in stati inconsistenti. Ad ogni client viene dato un attachment che ha l'obj
 * Utente con il suo selection key e un ByteBuffer così da garantirmi ogni qualvolta effettuo delle op su di loro che
 * si sta lavorando su dati consistenti. La variabile socUser viene costantemente cambiata e punta a l'attachment del
 * client scelto dal selector in quel momento.
 */
public class Server {
    private Utente socUser;
    ExecutorService ex;
    Selector selector;
    SocketChannel socChanClient;
    ServerSocketChannel serverSckChnl;

    /**
     * Fa partire il server tcp il thread che gestisce il sigint e il thread che fa pooling sulle sfide ed il selector
     */
    public void start() {
        try {
            ex = Executors.newFixedThreadPool(Settings.N_THREADS_THREAD_POOL); //th delle partite
            serverSckChnl = ServerSocketChannel.open(); //apro il server e lo metto non bloccante
            serverSckChnl.socket().bind(new InetSocketAddress(InetAddress.getByName("localhost"), Settings.TCP_PORT));
            serverSckChnl.configureBlocking(false);

            int ops = serverSckChnl.validOps();//Op valide
            selector = Selector.open();//Apro il selector
            serverSckChnl.register(selector, ops, null);
            //Gestisce il th che fa pooling sulla sfide in corso per vedere se sono terminate
            WorkerSfida workerSfida = new WorkerSfida();
            Thread thGestoreSfide = new Thread(workerSfida);
            thGestoreSfide.start();
            Utils.SalvaSuFileHandleSIGTERM(ex, thGestoreSfide); //Gestisce il salvatagio a sigterm e salvataggio automatico

            while (true) {
                selector.select();

                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    // rimuove la chiave dal Selected Set, ma non dal registered Set
                    try {
                        if (key.isAcceptable()) {
                            keyAcceptableRegister(selector); //Se devo registrare
                        } else if (key.isReadable()) {
                            keyRead(key); //Se devo leggere
                        } else if (key.isWritable()) {
                            keyWrite(key); //Se devo scrivere
                        }
                    } catch (IOException ex2) { //Se il client crasha o chiude la connessione
                        //ex2.printStackTrace();
                        Object[] objClient = (Object[]) key.attachment();
                        if (objClient.length >= 2) { //recupero se possibile l'oggetto utente associato
                            Utente uCrash = (Utente) objClient[1];
                            crashClient(uCrash); //Gestisce la disconnessione improvvisa
                        }
                        key.channel().close(); //Chiudo il channel e cancello la chiave
                        key.cancel();
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Se il client era offline notifica che ha chiuso la connessione, se era online vuold dire che è crashato
     *
     * @param uCrash utente che si è disconnesso
     */
    public void crashClient(Utente uCrash) {
        if (uCrash != null && uCrash.getNickname() != null) {
            boolean crashed = (uCrash.isConnesso());
            ListaUtenti.getInstance().setConnected(uCrash.getNickname(), false); //Se crasha lo disconnette
            Utils.log(((crashed) ? uCrash.getNickname() + " è crashato" : uCrash.getNickname() + " ha chiuso la connessione"), uCrash);
        }
    }

    /**
     * Registra un nuovo client al selector
     *
     * @param selector
     * @throws IOException
     */
    public void keyAcceptableRegister(Selector selector) throws IOException {
        SocketChannel client = serverSckChnl.accept(); //metto il client in non blocking
        client.configureBlocking(false); //Registro la chiave in lettura
        SelectionKey sk = client.register(selector, SelectionKey.OP_READ);
        ByteBuffer msg = ByteBuffer.allocate(Settings.READ_BYTE_BUFFER_SIZE);
        ByteBuffer[] bfs = {msg};
        socUser = new Utente(sk);
        Object[] objClient = {bfs, socUser};
        sk.attach(objClient); //Associo una struttura dati contente buffer e obj utente per ogni client
        Utils.log(client.getRemoteAddress().toString());
    }

    /**
     * gestisce la scrittura del channel
     *
     * @param key
     * @throws IOException
     */
    public void keyWrite(SelectionKey key) throws IOException {
        socChanClient = (SocketChannel) key.channel(); //Recupero il socChannel
        Object[] respObj = (Object[]) key.attachment(); //Recupero l'oggetto associato
        String response = (String) respObj[0];
        ByteBuffer respBuf = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
        socChanClient.write(respBuf); //Scrivo sul channel

        if (!respBuf.hasRemaining()) {
            respBuf.clear(); //Pulisco e mi registro in lettura
            ByteBuffer msg = ByteBuffer.allocate(Settings.READ_BYTE_BUFFER_SIZE);
            ByteBuffer[] bfs = {msg};
            Object[] objClient = {bfs, this.socUser};
            socChanClient.register(selector, SelectionKey.OP_READ, objClient);
        }
    }

    /**
     * Gesce la lettuar del channel
     *
     * @param key
     * @throws IOException
     * @throws BufferUnderflowException
     */
    public void keyRead(SelectionKey key) throws IOException, BufferUnderflowException {
        socChanClient = (SocketChannel) key.channel();
        Object[] objClient = (Object[]) key.attachment(); //Recupero l'obj associato al client
        ByteBuffer[] bfs = (ByteBuffer[]) objClient[0];
        long byteLeft = socChanClient.read(bfs); //leggo e controllo che non sia crashato
        if (byteLeft == -1) throw new IOException();
        ByteBuffer msgBuf = bfs[0]; //prendo il ByteBuffer dall'obj dell'attachment

        StringBuilder message = new StringBuilder();

        msgBuf.flip(); //mi preparo per la lettura
        byte[] bytes = new byte[msgBuf.remaining()];
        msgBuf.get(bytes);
        message.append(new String(bytes)); //Scrivo i byte dentro la stringa
        msgBuf.clear(); //pulisco il buffer
        byteLeft = socChanClient.read(msgBuf);  //controllo che non sia chiusa la connessione
        if (byteLeft == -1) throw new IOException();

        this.socUser = (Utente) objClient[1]; //Recupero l'obj utente dall'attachment

        if (!message.toString().isEmpty()) messageParser(message.toString()); //Gestisco i comandi letti
    }

    /**
     * Fa il parsing dei comandi che inva il client. Se l'utente è in partita ignora i comandi che riceve da quel client
     * poichè è gestito dal thread della partita.
     *
     * @param message
     */
    public void messageParser(String message) {
        StringTokenizer tokenizedLine = new StringTokenizer(message);
        String pw, nickUtente, nickAmico;
        if (socUser.getInPartita()) return; //se è in partita ignora le richieste di altri comandi!

        try {
            switch (tokenizedLine.nextToken()) {
                case "LOGIN":
                    nickUtente = tokenizedLine.nextToken();
                    pw = tokenizedLine.nextToken();
                    String udpPort = tokenizedLine.nextToken(); //se è il login la terza parola è la porta
                    this.socUser.setUdpPort(Integer.parseInt(udpPort)); //Setto la porta udp all'utente del soc

                    login(nickUtente, pw);
                    break;
                case "LOGOUT":
                    nickUtente = tokenizedLine.nextToken();

                    logout(nickUtente);
                    break;
                case "ADD_FRIEND":
                    nickUtente = tokenizedLine.nextToken();
                    nickAmico = tokenizedLine.nextToken();

                    aggiungi_amico(nickUtente, nickAmico);
                    break;
                case "LISTA_AMICI":
                    nickUtente = tokenizedLine.nextToken();

                    lista_amici(nickUtente);
                    break;
                case "SFIDA":
                    nickUtente = tokenizedLine.nextToken();
                    nickAmico = tokenizedLine.nextToken();

                    sfida(nickUtente, nickAmico);
                    break;
                case "MOSTRA_SCORE":
                    nickUtente = tokenizedLine.nextToken();

                    mostra_punteggio(nickUtente);
                    break;
                case "MOSTRA_CLASSIFICA":
                    nickUtente = tokenizedLine.nextToken();

                    mostra_classifica(nickUtente);
                    break;
                default:
                    break;
            }
        } catch (NoSuchElementException | IOException nse) {
            nse.printStackTrace();
        }
    }

    /**
     * effettua il login dell'utente, viene cercato nella lista degli utenti registrati e se presente associa la sua key
     * udp port all'oggetto caricato. E prendo il puntatore a questo aggiornato con i dettagli della connessione.
     *
     * @param nickUtente
     * @param password
     * @throws IOException
     */
    public void login(String nickUtente, String password) throws IOException {
        try {
            if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
            if (password == null || password.length() == 0) throw new IllegalArgumentException();
            if (ListaUtenti.getInstance().isConnected(nickUtente))
                throw new UserAlreadyLoggedIn("L'utente è già loggato");
            //ottiene il profilo da quelli caricati in memoria del json
            Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);

            if (profilo == null) throw new UserDoesntExists("L'utente inserito non esiste");
            if (!profilo.getPassword().equals(password)) throw new WrongPassword("Password errata");

            //Trovato il profilo che era nel json gli associo la SelectionKey e lo metto online
            ListaUtenti.getInstance().setConnected(nickUtente, true);
            profilo.setSelKey(this.socUser.getSelKey());
            profilo.setUdpPort(this.socUser.getUdpPort());
            this.socUser = profilo;
            Utils.log(nickUtente + " loggato!", this.socUser);

            sendResponseToClient("Login eseguito con successo");
        } catch (Exception e) {
            sendResponseToClient(e.getMessage());
        }
    }

    /**
     * effettuo il logout dell'utente e lo setto offline nella lista degli utenti caricati in memoria
     *
     * @param nickUtente
     * @throws ClosedChannelException
     */
    public void logout(String nickUtente) throws ClosedChannelException {
        try {
            if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
            if (!ListaUtenti.getInstance().isConnected(nickUtente)) throw new UserAlreadyLoggedIn();

            ListaUtenti.getInstance().setConnected(nickUtente, false); //Lo disconnetto dagli utenti in memoria
            sendResponseToClient("Logout eseguito con successo");
            Utils.log(nickUtente + " logout!", this.socUser);
        } catch (Exception e) {
            sendResponseToClient(e.getMessage());
        }
    }

    /**
     * Aggiunge un amico alla propria lista amici
     *
     * @param nickUtente
     * @param nickAmico
     * @throws ClosedChannelException
     */
    public void aggiungi_amico(String nickUtente, String nickAmico) throws ClosedChannelException {
        try {
            if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
            if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
            if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi essere amico di te stesso");

            ListaUtenti connectedUSers = ListaUtenti.getInstance(); //l'amico deve essere online per aggiungerlo
            Utente profileRichiedente = connectedUSers.getUser(nickUtente);
            Utente profileAmico = connectedUSers.getUser(nickAmico);

            if (profileRichiedente == null || profileAmico == null) throw new FriendNotFound("Amico non trovato");
            if (!profileAmico.isConnesso()) throw new FriendNotConnected("L'amico deve essere connesso");
            profileRichiedente.addFriend(profileAmico.getNickname());
            profileAmico.addFriend(profileRichiedente.getNickname());

            sendResponseToClient("Amicizia " + nickUtente + "-" + nickAmico + " creata");
            Utils.log(String.format("%s ha aggiunto %s alla lista amici (%s) , (%s)", nickUtente, nickAmico, Utils.getIpRemoteFromProfile(profileRichiedente), Utils.getIpRemoteFromProfile(profileAmico)));
        } catch (Exception e) {
            sendResponseToClient(e.getMessage());
        }
    }

    /**
     * Mostra la propria lista amici inviandola come json
     *
     * @param nickUtente
     * @throws ClosedChannelException
     */
    public void lista_amici(String nickUtente) throws ClosedChannelException {
        try {
            if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

            Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
            if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

            ConcurrentHashMap<String, String> listaAmici = profilo.getListaAmici();
            String listaAmiciAsJson = Storage.objectToJSON(listaAmici); //invio la mia lista amici come json
            sendResponseToClient(listaAmiciAsJson);
            Utils.log(String.format("%s %s ha chiesto di vedere la lista amici di %s", this.socUser.getNickname(), Utils.getIpRemoteFromProfile(this.socUser), nickUtente), profilo);
        } catch (Exception e) {
            sendResponseToClient(e.getMessage());
        }
    }

    /**
     * Gestisce la richiesta di sfida che viene effettuata tramite udp. C'è un timer che se scatta vuol dire che non è
     * stata data risposta e scarta la sfida. In modo preventivo metto sia l'amico che l'utente sfidante in modalità
     * partita così da scartare possibili richieste di sfida da parte di altri utenti a questi 2, finchè non ho un esito.
     * Che può essere dipeso dal timer o dal si/no del client.
     *
     * @param nickUtente    il nome di chi chiede la sfida
     * @param profiloUtente profilo del richidente sfida
     * @param amico         profilo dell'amico
     * @throws IOException
     * @throws SfidaRequestRefused
     * @throws NessunaRispostaDiSfida
     */
    private void sendUdpChallenge(String nickUtente, Utente profiloUtente, Utente amico) throws IOException, SfidaRequestRefused, NessunaRispostaDiSfida {
        DatagramSocket udpClient = new DatagramSocket();
        udpClient.setSoTimeout(Settings.UDP_TIMEOUT); //Setto il timeout di scadenza

        InetAddress address = InetAddress.getByName(Settings.HOST_NAME);
        String msg = nickUtente; //Il mex sarò il mio nickname con il quale richiedo la sfida all'amico
        byte[] msgSfida = msg.getBytes(StandardCharsets.UTF_8); //creo il buffer ed mando il pacchetto su udp port amico
        DatagramPacket packet = new DatagramPacket(msgSfida, msgSfida.length, address, amico.getUdpPort());
        udpClient.send(packet);

        byte[] ack = new byte[2]; //Mi metto in ricezione per il mex di conferma/rifiuto (si/no)
        DatagramPacket rcvPck = new DatagramPacket(ack, ack.length);

        try {
            udpClient.receive(rcvPck); //Se ricevo qualcosa creo la stringa
            msg = new String(rcvPck.getData());
        } catch (SocketTimeoutException e) {
            e.printStackTrace(); //Se scatta il timeout chiudo la connessione e notifico che non ho ricevuto risposta
            udpClient.close(); //Tolgo il flag che sono in partita per poter accettare future richieste
            profiloUtente.setInPartita(false);
            amico.setInPartita(false);
            throw new NessunaRispostaDiSfida("L'amico non ha dato risposta.");
        }

        udpClient.close(); //Chiudo la connessione udp se l'amico ha accettato la sfida

        if (msg.equals("no")) { //Se ha rifiutatato mi tolgo dallo stato della partita e notifico che ha rifiutato
            profiloUtente.setInPartita(false);
            amico.setInPartita(false);
            throw new SfidaRequestRefused("Ha rifiutato la sfida");
        }
    }

    /**
     * Gestisce al sfida dei 2 utenti. Viene creato l'oggetto sfida e i 2 oggetti Partita. Viene realizzate una
     * associazione tra questi e poi i thread partita vengono assegnati al threadpool e lancaiti.
     *
     * @param nickUtente
     * @param nickAmico
     * @throws IOException
     */
    public void sfida(String nickUtente, String nickAmico) throws IOException {
        try {
            if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");
            if (nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");
            if (nickUtente.equals(nickAmico)) throw new IllegalArgumentException("Non puoi sfidare te stesso");
            Utente profiloUtente = ListaUtenti.getInstance().getUser(nickUtente);
            if (!profiloUtente.isFriend(nickAmico))
                throw new FriendNotFound("L'utente che vuoi sfidare non è nella tua lista amici");
            Utente amico = ListaUtenti.getInstance().getUser(nickAmico);
            if (amico != null && !amico.isConnesso()) throw new FriendNotConnected("L'amico non è connesso");
            if (amico.getInPartita()) throw new FriendIsAlreadyPlaying("L'amico è già in una partita");
            //Serve per evitare di ricevere richieste sovrapposte di sfida
            profiloUtente.setInPartita(true);
            amico.setInPartita(true); //Setto che l'amico è in partita
            sendUdpChallenge(nickUtente, profiloUtente, amico);//invia la richiesta di sfida su udp
            //Se l'amico ha accettato lo notifico scrivendo sul socket channel
            socChanClient.write(ByteBuffer.wrap((nickAmico + " ha accettato la sfida!" + "\n").getBytes(StandardCharsets.UTF_8)));

            Sfida objSfida = new Sfida(profiloUtente.getNickname().hashCode() + new Random().nextInt(4));
            Partita partitaSfidante = new Partita(profiloUtente, objSfida);
            Partita partitaAmico = new Partita(amico, objSfida);
            objSfida.setPartite(partitaSfidante, partitaAmico);
            ListaSfide.getInstance().addSfida(objSfida);

            ex.execute(partitaSfidante);
            ex.execute(partitaAmico);
            Utils.log(String.format("%s ha sfidato %s (%s) , (%s)", nickUtente, nickAmico, Utils.getIpRemoteFromProfile(profiloUtente), Utils.getIpRemoteFromProfile(amico)));

        } catch (Exception e2) {
            if (socUser.getInPartita()) socUser.setInPartita(false);
            Utente amico = ListaUtenti.getInstance().getUser(nickAmico);
            if (amico != null && amico.getInPartita()) amico.setInPartita(false);
            sendResponseToClient(e2.getMessage());
        }
    }

    /**
     * Mostra il puntegio del giocatore
     *
     * @param nickUtente
     * @throws ClosedChannelException
     */
    public void mostra_punteggio(String nickUtente) throws ClosedChannelException {
        try {
            if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");
            Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
            if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

            sendResponseToClient("Punteggio: " + profilo.getPunteggioTotale());
            Utils.log(String.format("%s %s ha chiesto di vedere il punteggio di %s", this.socUser.getNickname(), Utils.getIpRemoteFromProfile(this.socUser), nickUtente), profilo);
        } catch (Exception e) {
            sendResponseToClient(e.getMessage());
        }
    }

    /**
     * Mostra la classifica inviata come json
     *
     * @param nickUtente
     * @throws ClosedChannelException
     */
    public void mostra_classifica(String nickUtente) throws ClosedChannelException {
        try {
            if (nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente errato");
            Utente profilo = ListaUtenti.getInstance().getUser(nickUtente);
            if (profilo == null) throw new UserDoesntExists("L'utente cercato non esiste");

            String leaderboardJson = generateLeaderBoardJSON(profilo);
            sendResponseToClient(leaderboardJson);
            Utils.log(String.format("%s %s ha chiesto di vedere la classifica di %s", this.socUser.getNickname(), Utils.getIpRemoteFromProfile(this.socUser), nickUtente), profilo);
        } catch (Exception e) {
            sendResponseToClient(e.getMessage());
        }
    }

    /**
     * invia la risposta al client registrando una chiava in scrittura sul selector
     *
     * @param testo
     * @throws ClosedChannelException
     */
    private void sendResponseToClient(String testo) throws ClosedChannelException {
        if (testo == null) throw new IllegalArgumentException();
        if (socChanClient == null) throw new NullPointerException();

        Object[] objResponse = {testo + "\n", this.socUser}; //Prepara il mex da scrivere con l'oggetto utente
        socChanClient.register(selector, SelectionKey.OP_WRITE, objResponse); //Si registra il scrittura
    }

    /**
     * Genera la classifica amici filtrando solo i campi utili e creando poi il json
     *
     * @param profilo
     * @return json della classifica amici
     */
    private String generateLeaderBoardJSON(Utente profilo) {
        ArrayList<Utente> classificaAmici = new ArrayList<>();
        classificaAmici.add(profilo); //Mi aggiungo alla lista degli amici su cui generare la classifica sorted

        if (!profilo.getListaAmici().isEmpty()) { //se non ha amici ritorna solo il suo score saltando questo if
            for (String keyAmico : profilo.getListaAmici().values()) {
                Utente amico = ListaUtenti.getInstance().getUser(keyAmico);
                classificaAmici.add(amico);
            } //Sorta per punteggio più alto
            classificaAmici.sort(Comparator.comparing(Utente::getPunteggioTotale).reversed());
        }

        //Serve per levare tutte le info extra dell'utente
        ArrayList<String> leaderBoardWithOnlyUserANdScore = new ArrayList<>(); //Filtro per ottenere solo nome e punteggio
        for (Utente user : classificaAmici)
            leaderBoardWithOnlyUserANdScore.add(user.getNickname() + " " + user.getPunteggioTotale());

        return Storage.objectToJSON(leaderBoardWithOnlyUserANdScore);
    }
}
