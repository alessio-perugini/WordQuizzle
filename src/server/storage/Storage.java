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
import java.util.concurrent.ConcurrentMap;

public class Storage {
    public void test(){
        ConcurrentHashMap<String, Utente>  gg = new ConcurrentHashMap<>();
        gg.putIfAbsent("asd", new Utente("asd", "dsa"));
        gg.putIfAbsent("asd2", new Utente("asd2", "dsa2"));

        //TODO Check se esistono i 3 file json
        writeObjectToJSONFile("utenti.json", gg);
        ConcurrentHashMap<String, Utente> lista = (ConcurrentHashMap<String, Utente>)getObjectFromJSONFile("utenti.json");
        lista.putIfAbsent("xD", new Utente("xD", "asd1asd"));
        writeObjectToJSONFile("utenti.json", lista);

    }

    public Storage(){

    }

    public static void writeObjectToJSONFile(String filename, Object obj){
        try{
            File file = new File(filename);
            file.createNewFile();

            ObjectMapper objectMapper = new ObjectMapper();
            byte[] content = objectMapper.writeValueAsBytes(obj);

            try (FileOutputStream fis = new FileOutputStream(filename)) {
                FileChannel outWrapChannel = fis.getChannel();
                ByteBuffer bb = ByteBuffer.wrap(content);
                outWrapChannel.write(bb);

                // Clear resources
                System.out.println("close channels, clear resources");
                bb.clear();
                outWrapChannel.close();
            }catch (Exception e){

            }
        }catch (Exception e){

        }
    }

    public static String concurrentMapToJSON(ConcurrentMap<String, String> obj){
        String json = null;
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(obj);
            System.out.println(json);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static String objectToJSON(Object obj){
        String json = null;
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(obj);
            System.out.println(json);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static Object getObjectFromJSONFile(String path){
        if(path == null || path.equals("")) throw new IllegalArgumentException();

        try {
            //TODO migliroare con il buffered stream
            FileChannel inChannel = FileChannel.open(Paths.get(path), StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            boolean stop = false;

            while (!stop){
                int bytesRead = inChannel.read(buffer);
                if(bytesRead == -1) stop = true;
            }
            inChannel.close();

            final ObjectMapper mapper = new ObjectMapper();
            ConcurrentHashMap<String, Utente> ls = mapper.reader()
                    .forType(new TypeReference<ConcurrentHashMap<String, Utente>>() {})
                    .readValue(buffer.array());

            return ls;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
