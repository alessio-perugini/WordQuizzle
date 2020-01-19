package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import errori.UserAlreadyLoggedIn;
import server.Settings;
import server.Utente;
import server.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class MainClass {
    static int udpPort = Settings.UDP_PORT;
    static Utente profiloLoggato = null;
    static final BufferedReader consoleRdr = new BufferedReader(new InputStreamReader(System.in));
    static RichiestaSfida reqSfida = RichiestaSfida.getInstance();
    static boolean sonoInPartita = false;
    static boolean quit = false; //server per uscire dal client
    static SocketChannel client;
    static UdpListener udpSrv;
    static String scelta; //è la lettura della console

    public static void main(String[] args) {//Se ha argomenti il primo è la porta udp del server delle sfide su cui ascoltare
        udpPort = (args.length > 0) ? Integer.parseInt(args[0]) : Settings.UDP_PORT;
        if (udpPort < Settings.UDP_PORT) udpPort = Settings.UDP_PORT; //il server deve essere minimo sulla porta 50002

        udpSrv = new UdpListener(udpPort);//Istanzio l'udp server e avvio il thread
        Thread thUdpManager = new Thread(udpSrv);
        thUdpManager.start();

        try {//instauro la connessione con il server tcp di gioco
            SocketAddress address = new InetSocketAddress(InetAddress.getByName("localhost"), Settings.TCP_PORT);
            client = SocketChannel.open(address);
            welcome();
            while (!quit) {
                scelta = consoleRdr.readLine().trim();//leggo la scelta dell'utente levando eventuali spazi vuoti alla fine
                try {
                    StringTokenizer tokenizedLine = new StringTokenizer(scelta); //Server per spezzare la stringa per gli spazi
                    String currToken = tokenizedLine.nextToken();//prendo la prima stringa ottenuta dallo split dei spazi
                    switch (currToken) {
                        case "quit":
                            quit = true;
                            client.close();//chiudo la connessione con il server
                            break;
                        case "registra_utente":
                            registrazione(tokenizedLine);
                            break;
                        case "login":
                            login(tokenizedLine);
                            break;
                        case "logout":
                            logout();
                            break;
                        case "aggiungi_amico":
                            aggiungiAmico(tokenizedLine);
                            break;
                        case "lista_amici":
                            listaAmici();
                            break;
                        case "sfida":
                            sfida(tokenizedLine);
                            break;
                        case "mostra_punteggio":
                            punteggio();
                            break;
                        case "mostra_classifica":
                            classifica();
                            break;
                        case "--help":
                            help();
                            break;
                        default:
                            if (sonoInPartita) { //se sono in partita game gestisce l'input della console
                                game(currToken);
                            } else if (reqSfida.getSfidaToAnswer().get()) { //se devo rispondere ad una req di sfida
                                challangeRequest(currToken);
                            } else {
                                System.out.println("Comando non trovato, per la lista di comanda digitare (--help)");
                            }
                            break;
                    }
                } catch (NoSuchElementException nse) {
                    System.out.println(nse.getMessage());
                }
            }
        } catch (ConnectException ce) {
            System.out.println("/!\\Impossibile connettersi al server!/!\\\n   Controlla che il server sia aperto");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            udpSrv.quit();//Server per terminare il server udp chiudendo la connessione sbloccandomi da eventuali read
            thUdpManager.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void help() {
        System.out.println("usage : COMMAND [ ARGS ...]\n" +
                "Commands: \n" +
                "registra_utente <nickUtente > <password > registra l' utente \n" +
                "login <nickUtente > <password > effettua il login logout effettua il logout \n" +
                "logout effettua il logout \n" +
                "aggiungi_amico <nickAmico> crea relazione di amicizia con nickAmico lista_amici mostra la lista dei propri amici \n" +
                "lista_amici mostra la lista dei propri amici \n" +
                "sfida <nickAmico > richiesta di una sfida a nickAmico \n" +
                "mostra_punteggio mostra il punteggio dell’utente \n" +
                "mostra_classifica mostra una classifica degli amici dell’utente (incluso l’utente stesso) \n" +
                "quit per uscire");
    }

    private static void welcome() {
        System.out.println("                                                                                                                                 \n" +
                "                                                                                                                                 \n" +
                "           .---.                                ,----..                                                        ,--,              \n" +
                "          /. ./|                       ,---,   /   /   \\                    ,--,                             ,--.'|              \n" +
                "      .--'.  ' ;   ,---.    __  ,-.  ,---.'|  /   .     :            ,--, ,--.'|          ,----,       ,----,|  | :              \n" +
                "     /__./ \\ : |  '   ,'\\ ,' ,'/ /|  |   | : .   /   ;.  \\         ,'_ /| |  |,         .'   .`|     .'   .`|:  : '              \n" +
                " .--'.  '   \\' . /   /   |'  | |' |  |   | |.   ;   /  ` ;    .--. |  | : `--'_      .'   .'  .'  .'   .'  .'|  ' |      ,---.   \n" +
                "/___/ \\ |    ' '.   ; ,. :|  |   ,',--.__| |;   |  ; \\ ; |  ,'_ /| :  . | ,' ,'|   ,---, '   ./ ,---, '   ./ '  | |     /     \\  \n" +
                ";   \\  \\;      :'   | |: :'  :  / /   ,'   ||   :  | ; | '  |  ' | |  . . '  | |   ;   | .'  /  ;   | .'  /  |  | :    /    /  | \n" +
                " \\   ;  `      |'   | .; :|  | ' .   '  /  |.   |  ' ' ' :  |  | ' |  | | |  | :   `---' /  ;--,`---' /  ;--,'  : |__ .    ' / | \n" +
                "  .   \\    .\\  ;|   :    |;  : | '   ; |:  |'   ;  \\; /  |  :  | : ;  ; | '  : |__   /  /  / .`|  /  /  / .`||  | '.'|'   ;   /| \n" +
                "   \\   \\   ' \\ | \\   \\  / |  , ; |   | '/  ' \\   \\  ',  . \\ '  :  `--'   \\|  | '.'|./__;     .' ./__;     .' ;  :    ;'   |  / | \n" +
                "    :   '  |--\"   `----'   ---'  |   :    :|  ;   :      ; |:  ,      .-./;  :    ;;   |  .'    ;   |  .'    |  ,   / |   :    | \n" +
                "     \\   \\ ;                      \\   \\  /     \\   \\ .'`--\"  `--`----'    |  ,   / `---'        `---'         ---`-'   \\   \\  /  \n" +
                "      '---\"                        `----'       `---`                      ---`-'                                       `----'   \n" +
                "\nper la lista dei comandi digare --help");
    }

    private static void challangeRequest(String currToken) throws IOException {
        if (currToken.equals("si")) {
            udpSrv.setRispostaSfida("si");//risultato che deve inoltrare il server udp al client udp
            sonoInPartita = true; //I prossimi input da console saranno gestiti dalla func game()
            System.out.println("Attendi qualche istante per la generazione delle parole");
            printServerResponse(read()); //printa la prima parola da indovinare
        } else if (currToken.equals("no")) {
            udpSrv.setRispostaSfida("no");//risultato che deve inoltrare il server udp al client udp
            System.out.println("Sfida rifiutata");
            sonoInPartita = false;
        }
    }

    private static void game(String currToken) {
        try {
            write(currToken); //invia la parola tradotta
            String srvResp = read(); //legge nuove parole o l'esito della partita
            String toPrint = srvResp; //il # serve per capire se è l'ultimo mex di sfida
            if (srvResp.contains("#")) toPrint = toPrint.replace("#", "");
            System.out.println(toPrint);
            if (srvResp.contains("Hai tradotto correttamente")) { //è l'ultimo messaggio prima di sapere se ho vinto
                if (!srvResp.contains("#"))
                    printServerResponse(read()); //mi metto in lettura per sapere se ho vinto o perso!
                sonoInPartita = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void login(StringTokenizer tokenizedLine) {
        try {
            String nick = tokenizedLine.nextToken();
            String password = tokenizedLine.nextToken();

            if (profiloLoggato != null && profiloLoggato.getNickname().equals(nick))
                throw new UserAlreadyLoggedIn("Sei già loggato come " + nick);
            if (profiloLoggato != null) logout(); //Se logga un altro utente effettua il logout di quello corrente

            String esito = scriviLeggi("LOGIN " + nick + " " + password + " " + udpPort);
            printServerResponse(esito);
            if (esito.equals("Login eseguito con successo")) profiloLoggato = new Utente(nick, password);
        } catch (NoSuchElementException e) {
            System.out.println("login <nickUtente > <password > effettua il login");
        } catch (UserAlreadyLoggedIn ue) {
            System.out.println(ue.getMessage());
        }
    }

    public static void logout() {
        try {
            if (profiloLoggato == null) {
                System.out.println("Logout già effetuato");
                return;
            }

            String esito = scriviLeggi("LOGOUT " + profiloLoggato.getNickname());
            printServerResponse(esito);
            if (esito.equals("Logout eseguito con successo")) profiloLoggato = null;
        } catch (NoSuchElementException e) {
            System.out.println("logout effettua il logout");
        }
    }

    public static void registrazione(StringTokenizer tokenizedLine) {
        try {//invia la richiesta di registrazione al server rmi
            RmiClient rmiReg = new RmiClient();
            String nickname = tokenizedLine.nextToken();
            String pw = tokenizedLine.nextToken();
            if (rmiReg.registra_utente(nickname, pw)) System.out.println("Registrazione effettuata!");
        } catch (NoSuchElementException e) {
            System.out.println("registra_utente <nickUtente > <password >");
        }
    }

    public static void sfida(StringTokenizer tokenizedLine) {
        try {
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            String amico = tokenizedLine.nextToken();

            printServerResponse("In attesa di una risposta da parte dell'amico.");
            String response = scriviLeggi("SFIDA " + profiloLoggato.getNickname() + " " + amico + "");
            printServerResponse(response);
            sonoInPartita = response.contains("ha accettato la sfida!");// se ritorna questa stringa allora diventa true
            try {
                if (sonoInPartita) {
                    System.out.println("Attendi qualche istante per la generazione delle parole");
                    printServerResponse(read()); //legge la prima challenge
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            System.out.println("sfida <nickAmico > richiesta di una sfida a nickAmico");
        }
    }

    public static void classifica() {
        try {
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }

            String classificaJSON = scriviLeggi("MOSTRA_CLASSIFICA " + profiloLoggato.getNickname());
            //Prendo dalla risposta JSON e converto in una lista di stringhe
            final ObjectMapper mapper = new ObjectMapper();
            ArrayList<String> listaClassifica = mapper.reader()
                    .forType(new TypeReference<ArrayList<String>>() {
                    })
                    .readValue(classificaJSON.getBytes());

            Utils.printArrayList(listaClassifica, "Classifica: ");//printo in modo formattato l'oggetto letto dal json
        } catch (NoSuchElementException e) {
            System.out.println("mostra_classifica mostra una classifica degli amici dell’utente");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void punteggio() {
        try {
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }

            String response = scriviLeggi("MOSTRA_SCORE " + profiloLoggato.getNickname());
            printServerResponse(response);
        } catch (NoSuchElementException e) {
            System.out.println("mostra_punteggio mostra il punteggio dell’utente");
        }
    }

    public static void aggiungiAmico(StringTokenizer tokenizedLine) {
        try {
            String amico = tokenizedLine.nextToken();
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }

            String response = scriviLeggi("ADD_FRIEND " + profiloLoggato.getNickname() + " " + amico + "");
            printServerResponse(response);
        } catch (NoSuchElementException e) {
            System.out.println("aggiungi_amico <nickAmico> crea relazione di amicizia con nickAmico");
        }
    }

    public static void listaAmici() {
        try {
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }

            String amiciJSON = scriviLeggi("LISTA_AMICI " + profiloLoggato.getNickname());

            final ObjectMapper mapper = new ObjectMapper();//Trasformo il json in oggetto di tipo HashMap
            HashMap<String, String> listaAmici = mapper.reader()
                    .forType(new TypeReference<HashMap<String, String>>() {
                    })
                    .readValue(amiciJSON.getBytes());

            Utils.printListaAmici(listaAmici);//printo in modo formattato l'oggetto letto dal json
        } catch (NoSuchElementException e) {
            System.out.println("lista_amici mostra la lista dei propri amici");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    static void write(String messaggio) {
        try {
            messaggio += "\n"; //aggiungo la terminazione al messaggio
            client.write(ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8))); //Cre un bytebuffer dai byte del mex
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Settings.READ_BYTE_BUFFER_SIZE);
        int byteRead = client.read(buffer); //leggo la risposta del server nel bytebyffer
        isServerCrashed(byteRead); //controllo che il server non sia crashato
        String response = new String(buffer.array()).trim(); //creo una stringa dalla lettura del buffer
        buffer.clear();
        return response;
    }

    public static String scriviLeggi(String messaggio) {
        try {
            write(messaggio); //Scrivi al server il mex
            return read(); //Leggi dal server la risposta del mex inviato
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private static void isServerCrashed(int byteRead) throws IOException {
        if (byteRead >= 0) return; //se è != da -1 non fa nulla
        quit = true;
        System.out.println("Il server è crashato!");
        throw new IOException("Server crashed");
    }

    private static void printServerResponse(String msg) {
        if (msg.contains("#")) msg = msg.replace("#", "");
        System.out.println(msg);
    }
}
