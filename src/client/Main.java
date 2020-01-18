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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    static int udpPort = 50002;
    static Utente profiloLoggato = null;
    static final BufferedReader consoleRdr = new BufferedReader(new InputStreamReader(System.in));
    static RichiestaSfida reqSfida = RichiestaSfida.getInstance();
    static boolean sonoInPartita = false;
    static boolean quit;

    public static void main(String[] args) {
        udpPort = (args.length > 0) ? Integer.parseInt(args[0]) : 50002;
        if (udpPort < 50002) udpPort = 50002;

        UdpListener udpSrv = new UdpListener(udpPort);
        Thread thUdpManager = new Thread(udpSrv);
        thUdpManager.start();

        try {
            SocketAddress address = new InetSocketAddress(InetAddress.getByName("localhost"), Settings.TCP_PORT);
            SocketChannel client = SocketChannel.open(address);

            quit = false;
            String scelta;

            while (!quit) {
                scelta = consoleRdr.readLine().trim();
                try {
                    StringTokenizer tokenizedLine = new StringTokenizer(scelta);
                    String currToken = tokenizedLine.nextToken();
                    switch (currToken) {
                        case "quit":
                            quit = true;
                            client.close();
                            break;
                        case "registra_utente":
                            registrazione(tokenizedLine);
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
                                    "logout effettua il logout \n" +
                                    "aggiungi_amico <nickAmico> crea relazione di amicizia con nickAmico lista_amici mostra la lista dei propri amici \n" +
                                    "lista_amici mostra la lista dei propri amici \n" +
                                    "sfida <nickAmico > richiesta di una sfida a nickAmico \n" +
                                    "mostra_punteggio mostra il punteggio dell’utente \n" +
                                    "mostra_classifica mostra una classifica degli amici dell’utente (incluso l’utente stesso) \n" +
                                    "quit per uscire");
                            break;
                        default:
                            if (sonoInPartita) {
                                try {
                                    write(currToken, client);
                                    String srvResp = read(client);
                                    if (srvResp.contains("Hai tradotto correttamente")) {
                                        read(client);//mi metto in lettura per dirmi se ho vinto o perso!
                                        sonoInPartita = false;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if (reqSfida.getSfidaToAnswer().get()) {
                                    if (currToken.equals("si")) {
                                        udpSrv.setRispostaSfida("si");
                                        sonoInPartita = true;
                                        printServerResponse("Attendi qualche istante per la generazione delle parole");
                                        read(client);//printa la prima parola da indovinare
                                    } else if (currToken.equals("no")) {
                                        udpSrv.setRispostaSfida("no");
                                        sonoInPartita = false;
                                    }
                                } else {
                                    System.out.println("Comando non trovato, per la lista di comanda digitare (--help)");
                                }
                            }
                            break;
                    }
                } catch (NoSuchElementException nse) {
                    System.out.println(nse.getMessage());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            udpSrv.quit();
            thUdpManager.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void login(StringTokenizer tokenizedLine, SocketChannel client) {
        try {
            String nick = tokenizedLine.nextToken();
            String password = tokenizedLine.nextToken();

            if (profiloLoggato != null && profiloLoggato.getNickname().equals(nick))
                throw new UserAlreadyLoggedIn("Sei già loggato come " + nick);
            if (profiloLoggato != null) logout(client); //Se logga un altro utente effettua il logout

            String esito = scriviLeggi("LOGIN " + nick + " " + password + " " + udpPort, client);
            printServerResponse(esito);
            if (esito.equals("Login eseguito con successo")) profiloLoggato = new Utente(nick, password);
        } catch (NoSuchElementException e) {
            System.out.println("login <nickUtente > <password > effettua il login");
        } catch (UserAlreadyLoggedIn ue) {
            System.out.println(ue.getMessage());
        }

    }

    public static void logout(SocketChannel client) {
        try {
            if (profiloLoggato == null) {
                System.out.println("Logout già effetuato");
                return;
            }
            String esito = scriviLeggi("LOGOUT " + profiloLoggato.getNickname(), client);
            printServerResponse(esito);
            if (esito.equals("Logout eseguito con successo")) profiloLoggato = null;
        } catch (NoSuchElementException e) {
            System.out.println("logout effettua il logout");
        }

    }

    public static void registrazione(StringTokenizer tokenizedLine) {
        try {
            RmiClient rmiReg = new RmiClient();
            String nickname = tokenizedLine.nextToken();
            String pw = tokenizedLine.nextToken();
            if (rmiReg.registra_utente(nickname, pw)) System.out.println("Registrazione effettuata!");
        } catch (NoSuchElementException e) {
            System.out.println("registra_utente <nickUtente > <password >");
        }
    }

    public static void sfida(StringTokenizer tokenizedLine, SocketChannel client) {
        try {
            String amico = tokenizedLine.nextToken();
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            printServerResponse("In attesa di una risposta da parte dell'amico.");
            String response = scriviLeggi("SFIDA " + profiloLoggato.getNickname() + " " + amico + "", client);
            printServerResponse(response);
            sonoInPartita = response.contains("ha accettato la sfida!");
            try {
                if (sonoInPartita) read(client); //legge la prima challenge
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NoSuchElementException e) {
            System.out.println("sfida <nickAmico > richiesta di una sfida a nickAmico");
        }
    }

    public static void classifica(SocketChannel client) {
        try {
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            String classificaJSON = scriviLeggi("MOSTRA_CLASSIFICA " + profiloLoggato.getNickname(), client);
            final ObjectMapper mapper = new ObjectMapper();
            ArrayList<String> listaClassifica = mapper.reader()
                    .forType(new TypeReference<ArrayList<String>>() {
                    })
                    .readValue(classificaJSON.getBytes());
            Utils.printArrayList(listaClassifica, "Classifica: ");
        } catch (NoSuchElementException e) {
            System.out.println("mostra_classifica mostra una classifica degli amici dell’utente");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void punteggio(SocketChannel client) {
        try {
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            String response = scriviLeggi("MOSTRA_SCORE " + profiloLoggato.getNickname(), client);
            printServerResponse(response);

        } catch (NoSuchElementException e) {
            System.out.println("mostra_punteggio mostra il punteggio dell’utente");
        }
    }

    public static void aggiungiAmico(StringTokenizer tokenizedLine, SocketChannel client) {
        try {
            String amico = tokenizedLine.nextToken();
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            String response = scriviLeggi("ADD_FRIEND " + profiloLoggato.getNickname() + " " + amico + "", client);
            printServerResponse(response);

        } catch (NoSuchElementException e) {
            System.out.println("aggiungi_amico <nickAmico> crea relazione di amicizia con nickAmico");
        }
    }

    public static void listaAmici(SocketChannel client) {
        try {
            if (profiloLoggato == null) {
                System.out.println("Devi prima effettuare il login!");
                return;
            }
            String amiciJSON = scriviLeggi("LISTA_AMICI " + profiloLoggato.getNickname(), client);

            final ObjectMapper mapper = new ObjectMapper();
            ConcurrentHashMap<String, String> listaAmici = mapper.reader()
                    .forType(new TypeReference<ConcurrentHashMap<String, String>>() {
                    })
                    .readValue(amiciJSON.getBytes());

            Utils.printListaAmici(listaAmici);
        } catch (NoSuchElementException e) {
            System.out.println("lista_amici mostra la lista dei propri amici");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    static void write(String messaggio, SocketChannel client) {
        try {
            messaggio += "\n";
            client.write(ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String read(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int byteRead = client.read(buffer);
        isServerCrashed(byteRead);
        String response = new String(buffer.array()).trim();
        System.out.println(response);
        buffer.clear();
        return response;
    }

    public static String scriviLeggi(String messaggio, SocketChannel client) {
        try {
            messaggio += "\n";
            client.write(ByteBuffer.wrap(messaggio.getBytes(StandardCharsets.UTF_8)));

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int byteRead = client.read(buffer);
            isServerCrashed(byteRead);
            String response = new String(buffer.array()).trim();
            buffer.clear();
            return response;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private static void isServerCrashed(int byteRead) throws IOException {
        if (byteRead >= 0) return;
        quit = true;
        System.out.println("Il server è crashato!");
        throw new IOException("Server crashed");
    }

    private static void printServerResponse(String msg) {
        System.out.println(msg);
    }
}
