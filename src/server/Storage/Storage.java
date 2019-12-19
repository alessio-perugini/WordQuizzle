package server.Storage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import server.Utente;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    public Storage(){
        ConcurrentHashMap<String, Utente>  gg = new ConcurrentHashMap<>();
        gg.putIfAbsent("asd", new Utente("asd", "dsa"));
        gg.putIfAbsent("asd2", new Utente("asd2", "dsa2"));

        //TODO Check se esistono i 3 file json
        writeFirstFile("utenti.json", gg);
        ConcurrentHashMap<String, Utente> lista = (ConcurrentHashMap<String, Utente>)getObjectFromJSONFile("utenti.json");
        lista.putIfAbsent("xD", new Utente("xD", "asd1asd"));
        writeFirstFile("utenti.json", lista);

    }

    public void writeFirstFile(String filename, Object obj){
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
                System.out.println("Remove file, close channels, clear resources");
                bb.clear();
                outWrapChannel.close();
            }catch (Exception e){

            }
        }catch (Exception e){

        }
    }

    public String objectToJSON(Object obj){
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

    public Object getObjectFromJSONFile(String path){
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
