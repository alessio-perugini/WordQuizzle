package server;

import server.MyMemoryAPI.Converter;
import server.MyMemoryAPI.MyMemoryResponse;
import server.gamelogic.DefaultWords;
import server.storage.Storage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contiene alcuni metodi per facilitare la lettura del codice nella classi in cui richiamano le funzioni qui presenti
 */
public class Utils {

    /**
     * Controlla se esiste il file del dizionare. In caso contrario prende dall'enum alcuni nomi e crea un file con
     * quelli.
     */
    public static void checkDictionaryFile() {
        String filename = Settings.FILE_DIZIONARIO;

        try {//Controlla se esiste il file (path)
            FileChannel.open(Paths.get(filename), StandardOpenOption.READ);
        } catch (IOException fe) {//Se non esiste crea il file (path), con dentro un json vuoto
            try {
                File file = new File(filename);
                file.createNewFile();

                List<String> enumNames = Stream.of(DefaultWords.values())
                        .map(Enum::name)
                        .collect(Collectors.toList());

                try (FileOutputStream out = new FileOutputStream(filename)) {
                    for (String elm : enumNames) {
                        elm += "\n";
                        out.write(elm.getBytes());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Dai parametri da inserire all'api li trasforma nella query
     *
     * @param params
     * @return stringa che sarebbe la query dell'url
     */
    private static String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        //preparo la query associando nome variabile al valore
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString(); //levo l'ultimo carattere &
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

    /**
     * Invia la richiesta http utilizzata per l'api rest e ritorna in caso positivo tutto il json della risposta
     *
     * @param parolaDaTradurre
     * @return il json del'api
     * @throws IOException
     */
    public static String sendHttpRequest(String parolaDaTradurre) throws IOException {
        URL url = new URL(Settings.API_URL + "get?"); //URL + primo parametro costante della query per l'api
        HttpURLConnection con = (HttpURLConnection) url.openConnection(); //Apro lo connessione
        con.setRequestMethod("GET"); //Richiedo una connessione con il metodo GET

        Map<String, String> parameters = new HashMap<>();
        parameters.put("q", parolaDaTradurre); //parametro con la parola da traturre
        parameters.put("langpair", "it|en"); //come devo tradurre la prole d'ingresso a quella d'uscita

        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(Utils.getParamsString(parameters)); //Costruisce la query e scrive sullo stream
        out.flush();
        out.close();
        return getFullResponse(con); //Leggo la risposta completa
    }

    /**
     * Ottiene la risposta completa dell'http. Controlla che non ci siano codice d'errore. In caso non ci siano leggo
     * il contenuto che contiene il json della risposta http.
     *
     * @param con
     * @return
     * @throws IOException
     */
    private static String getFullResponse(HttpURLConnection con) throws IOException {
        Reader streamReader;
        //Controllo se ci sono errori
        if (con.getResponseCode() > 299) streamReader = new InputStreamReader(con.getErrorStream());
        else streamReader = new InputStreamReader(con.getInputStream());

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder(); //Costruisco il json leggendo per riga
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();

        MyMemoryResponse data = Converter.fromJsonString(content.toString());//Costruisco dal json l'oggetto
        return data.getResponseData().getTranslatedText();//Prendo solo il campo delle parola tradotta
    }

    /**
     * Gestisce il salvataggio automatico del file se settato dalle impostazioni e gestisce il segnale di shutdown
     * aspettando che finsicano le sfide in esecuzione ed una volta terminate effettua l'ultimo salvataggio su file.
     *
     * @param ex             threadpool da fermare (quello delle partite)
     * @param thGestoreSfide
     */
    public static void SalvaSuFileHandleSIGTERM(ExecutorService ex, Thread thGestoreSfide) {
        Thread thread = new Thread(new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {//th che salva in automatico ogni X secondi l'oggetto degli utenti + partite nel file json
                    Thread.sleep(Settings.AUTO_SAVE_FREQUENCY);
                    System.out.println("/!\\ LOG: Salvataggio automatico in corso...");
                    Storage.writeObjectToJSONFile(Settings.JSON_FILENAME, ListaUtenti.getInstance().getHashListaUtenti());
                    System.out.println("/!\\ LOG: Salvataggio completato.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));

        if (Settings.AUTO_SAVE_JSON) thread.start(); //Se l'autosave è disabilitato non starta il thread
        //Gestisco il SIGINT aspettando che finiscano le partite in corso
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("/!\\ Shutdown hook ran! /!\\");
            if (Settings.AUTO_SAVE_JSON) thread.interrupt();
            ex.shutdownNow();
            while (!ex.isTerminated()) ;
            thGestoreSfide.interrupt();
            //Salvo su file ed esco
            Storage.writeObjectToJSONFile(Settings.JSON_FILENAME, ListaUtenti.getInstance().getHashListaUtenti());
            Utils.log("LOG: Salvataggio completato.");
        }));
    }

    /**
     * Aggiungi dei secondi ad un timestamp
     *
     * @param start
     * @param sec
     * @return il timestamp con i secondi aggiunti
     */
    public static Timestamp addSecondsToATimeStamp(Timestamp start, int sec) {
        return new Timestamp(start.getTime() + (sec * 1000L));
    }

    /**
     * Servver per capire se l'orario passato è scaduto oppure no
     *
     * @param end
     * @return Se l'orario passato per parametro è > di now ritorna false.
     */
    public static boolean isGivenTimeExpired(Timestamp end) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return end.before(now);
    }

    /**
     * Ottiene il emote address dell'utente
     *
     * @param user
     * @return remote address dell'utente
     */
    public static String getIpRemoteFromProfile(Utente user) {
        try {
            return (user != null) ? ((SocketChannel) user.getSelKey().channel()).getRemoteAddress().toString() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Stampa l'arraylist di stringhe. (Utilizzato come log per vedere la classifica)
     *
     * @param ls
     * @param prefix
     */
    public static void printArrayList(ArrayList<String> ls, String prefix) {
        StringBuilder toPrint = new StringBuilder(prefix + "");
        for (String elemento : ls) toPrint.append(elemento).append(", ");
        toPrint = new StringBuilder(toPrint.substring(0, toPrint.length() - 2));
        System.out.println(toPrint);
    }

    /**
     * Stampa a schermo il value della hashmap e lo concatena con una "," (utilizzato per printare la lista amici)
     *
     * @param ls
     */
    public static void printListaAmici(HashMap<String, String> ls) {
        if (ls == null) return;

        Iterator<String> it = ls.values().iterator();
        StringBuilder toPrint = new StringBuilder("Lista amici: ");
        while (it.hasNext()) {
            String amico = it.next();
            toPrint.append((it.hasNext()) ? amico + ", " : amico);
            it.remove();
        }
        System.out.println(toPrint);
    }

    /**
     * Printa a schermo con l'orario corrente il messaggio passato per param
     *
     * @param message
     */
    public static void log(String message) {
        String ora = new Timestamp(System.currentTimeMillis()).toLocalDateTime().toString();
        System.out.println("[" + ora + "] " + message);
    }

    /**
     * printa a schermo con l'orario corrente + l'ip dell'utente, il messaggio passato per param
     *
     * @param message
     * @param user
     */
    public static void log(String message, Utente user) {
        try {
            String ip = getIpRemoteFromProfile(user);
            String ora = new Timestamp(System.currentTimeMillis()).toLocalDateTime().toString();
            System.out.println("[" + ora + "] (" + ip + ") " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
