package server.gamelogic;

import server.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Dizionario {
    private ArrayList<String> dizionario;

    public ArrayList<String> getDizionario() {
        return dizionario;
    }

    public Dizionario(){
        this.dizionario = new ArrayList<>();
        loadDictionaryFromFile(Settings.FILE_DIZIONARIO);
    }

    private void loadDictionaryFromFile(String path){
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)){
            String currentLine = null;
            while((currentLine = reader.readLine()) != null){
                this.dizionario.add(currentLine);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public PriorityBlockingQueue<String> getNwordsFromDictionary(int n)
    {
        if(n > this.dizionario.size()) throw new IllegalArgumentException("Il dizionare ha meno parole di quelle richieste");

        PriorityBlockingQueue<String> nParoleEstratte = new PriorityBlockingQueue<>();
        Collections.shuffle(this.dizionario);
        for (int i = 0; i < n; i++) nParoleEstratte.add(dizionario.get(i));

        return nParoleEstratte;
    }
}
