package server;

import server.MyMemoryAPI.Converter;
import server.MyMemoryAPI.MyMemoryResponse;
import server.storage.Storage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Utils {
    public static String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

    public static String sendHttpRequest(String parolaDaTradurre) throws IOException {
        URL url = new URL(Settings.API_URL + "get?");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("q", parolaDaTradurre);
        parameters.put("langpair", "it|en");

        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(Utils.getParamsString(parameters));
        out.flush();
        out.close();
        return getFullResponse(con);
    }

    public static void SalvaSuFileHandleSIGTERM(ExecutorService ex) {
        Thread thread = new Thread(new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(20000);
                    System.out.println("/!\\ LOG: Salvataggio automatico in corso...");
                    Storage.writeObjectToJSONFile(Settings.JSON_FILENAME, ListaUtenti.getInstance().getHashListaUtenti());
                    System.out.println("/!\\ LOG: Salvataggio completato.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("/!\\ Shutdown hook ran! /!\\");
            ex.shutdown();
            while (!ex.isTerminated()) ;

            thread.interrupt();

            Storage.writeObjectToJSONFile(Settings.JSON_FILENAME, ListaUtenti.getInstance().getHashListaUtenti());
            Utils.log("LOG: Salvataggio completato.");
        }));
    }

    private static String getFullResponse(HttpURLConnection con) throws IOException {
        Reader streamReader;

        if (con.getResponseCode() > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }

        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();

        MyMemoryResponse data = Converter.fromJsonString(content.toString());
        return data.getResponseData().getTranslatedText();
    }
/*
    public static void printDizionarioDellaSfida(Sfida objSfida) {
        //objSfida.generaTraduzioni();
        for (Iterator<HashMap<String, String>> elm = objSfida.getParoleDaIndovinare().iterator(); elm.hasNext(); ) {
            HashMap<String, String> words = elm.next();
            Object[] keySet = (Object[]) words.keySet().toArray();
            System.out.println((String) keySet[0] + " -> " + words.get(keySet[0]));
        }
    }*/

    public static Timestamp addSecondsToATimeStamp(Timestamp start, int sec) {
        return new Timestamp(start.getTime() + (sec * 1000L));
    }

    public static boolean isGivenTimeExpired(Timestamp end) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return end.before(now);
    }

    public static void log(String message) {
        String ora = new Timestamp(System.currentTimeMillis()).toLocalDateTime().toString();
        System.out.println("[" + ora + "] " + message);
    }

    public static String getIpRemoteFromProfile(Utente user) {
        try {
            return (user != null) ? ((SocketChannel) user.getSelKey().channel()).getRemoteAddress().toString() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void printArrayList(ArrayList<String> ls, String prefix) {
        String toPrint = prefix + "";
        for (String elemento : ls) toPrint += elemento + ", ";
        toPrint = toPrint.substring(0, toPrint.length() - 2);
        System.out.println(toPrint);
    }

    public static void printListaAmici(ConcurrentHashMap<String, String> ls) {
        if (ls == null) return;

        Iterator it = ls.values().iterator();
        String toPrint = "Lista amici: ";
        while (it.hasNext()) {
            String amico = (String) it.next();
            toPrint += (it.hasNext()) ? amico + ", " : amico;
            it.remove();
        }
        System.out.println(toPrint);
    }


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
