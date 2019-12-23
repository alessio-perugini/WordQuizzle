package server;

import server.MyMemoryAPI.Converter;
import server.MyMemoryAPI.MyMemoryResponse;
import server.gamelogic.Sfida;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils {
    private static final int MAX_PORT_NUM = 65535;
    public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
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
        //TODO levare la print
        return getFullResponse(con);
    }

    private static String getFullResponse(HttpURLConnection con) throws IOException {
        Reader streamReader = null;

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

    public static void printDizionarioDellaSfida(Sfida objSfida){
        objSfida.generaTraduzioni();
        for(Iterator<HashMap<String, String>> elm = objSfida.getParoleDaIndovinare().iterator(); elm.hasNext();){
            HashMap<String, String> words = elm.next();
            Object[] keySet = (Object[])words.keySet().toArray();
            System.out.println((String)keySet[0] + " -> " + words.get(keySet[0]));
        }
    }

    public static boolean udpPortAvailable(int port) {
        if (port < 49152 || port > 65535) throw new IllegalArgumentException("Invalid start port: " + port);

        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }
        }

        return false;
    }
}
