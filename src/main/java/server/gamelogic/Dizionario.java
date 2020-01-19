package server.gamelogic;

import server.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Gestisce la creazione del dizionario delle parole da tradurre. Carica prima le parole dal file scegliendo K parole
 * random
 */
public class Dizionario {
    private ArrayList<String> dizionario;
    private static Dizionario instance;

    public ArrayList<String> getDizionario() {
        return dizionario;
    }

    private Dizionario() {
        this.dizionario = new ArrayList<>();
        loadDictionaryFromFile(Settings.FILE_DIZIONARIO);
    }

    public static synchronized Dizionario getInstance() {
        if (instance == null) instance = new Dizionario();
        return instance;
    }

    /**
     * Carica il dizionario in memoria dato il path in ingresso
     *
     * @param path percorso del file del dizionario
     */
    private void loadDictionaryFromFile(String path) {//Apre in lettura il file del dizionario (se c'è)

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                this.dizionario.add(currentLine);//Aggiungi al dizionario la riga letta
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Genera un arraylist di coppia ita-eng dove eng non è settato e verrà inserito quando si fa la richiesta all'api
     *
     * @param n il numero delle parole che deve prendere dal dizionario
     * @return ritorna una lista di hashmap che contine parole-traduzione
     */
    public ArrayList<HashMap<String, String>> getNwordsFromDictionary(int n) {
        if (n > this.dizionario.size())
            throw new IllegalArgumentException("Il dizionario ha meno parole di quelle richieste");

        ArrayList<HashMap<String, String>> nParoleEstratte = new ArrayList<>();
        Collections.shuffle(this.dizionario); //Serve per randomizzare le parole visto che estraggo da 0 a N

        for (int i = 0; i < n; i++) {
            HashMap<String, String> ita_eng = new HashMap<>();
            ita_eng.put(dizionario.get(i), null);
            nParoleEstratte.add(ita_eng); //aggiungo le N paroli alla nuova Lista di coppie <Parola, Traduzione>
        }

        return nParoleEstratte;
    }
}
