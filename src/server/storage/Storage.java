package server.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import server.Utente;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    public static void writeObjectToJSONFile(String filename, Object obj) {
        try {
            File file = new File(filename);
            file.createNewFile();

            ObjectMapper objectMapper = new ObjectMapper();
            byte[] content = objectMapper.writeValueAsBytes(obj);//Serializza l'oggetto in byte

            try (FileOutputStream out = new FileOutputStream(filename)) {
                out.write(content); //Scrivo sul file
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String objectToJSON(Object obj) {
        String json = null;
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(obj);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static Object getObjectFromJSONFile(String path) {
        if (path == null || path.equals("")) throw new IllegalArgumentException();

        try {//Controlla se esiste il file (path)
            FileChannel.open(Paths.get(path), StandardOpenOption.READ);
        } catch (IOException fe) {//Se non esiste crea il file (path), con dentro un json vuoto
            writeObjectToJSONFile(path, new ConcurrentHashMap<String, Utente>());
            fe.printStackTrace();
        }

        try (FileInputStream in = new FileInputStream(path); //Creo l'input stream del file
             ByteArrayOutputStream out = new ByteArrayOutputStream()) { //creo l'out su dove scrivere i byte letti
            byte[] byteArray = new byte[512];
            int bytesCount;

            while ((bytesCount = in.read(byteArray)) != -1) {
                out.write(byteArray, 0, bytesCount);
            }

            final ObjectMapper mapper = new ObjectMapper(); //leggo i byte e ricreo l'oggetto
            ConcurrentHashMap<String, Utente> ls = mapper.reader()
                    .forType(new TypeReference<ConcurrentHashMap<String, Utente>>() {
                    })
                    .readValue(out.toByteArray());
            return ls;
        } catch (Exception ec) {
            ec.printStackTrace();
        }
        return null;
    }
}
