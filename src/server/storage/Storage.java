package server.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import server.Utente;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
            byte[] content = objectMapper.writeValueAsBytes(obj);

            try (FileOutputStream fis = new FileOutputStream(filename)) {
                FileChannel outWrapChannel = fis.getChannel();
                ByteBuffer bb = ByteBuffer.wrap(content);
                outWrapChannel.write(bb);

                bb.clear();
                outWrapChannel.close();
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
        }

        try {
            //TODO migliroare con il buffered stream
            FileChannel inChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            boolean stop = false;

            while (!stop) {
                int bytesRead = inChannel.read(buffer);
                if (bytesRead == -1) stop = true;
            }
            inChannel.close();

            final ObjectMapper mapper = new ObjectMapper();
            ConcurrentHashMap<String, Utente> ls = mapper.reader()
                    .forType(new TypeReference<ConcurrentHashMap<String, Utente>>() {
                    })
                    .readValue(buffer.array());
            return ls;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
