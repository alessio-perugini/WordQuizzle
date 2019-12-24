package server;

import server.storage.Storage;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public void start() {
        ExecutorService ex;

        try (ServerSocket server = new ServerSocket();) {
            server.bind(new InetSocketAddress(InetAddress.getByName("localhost"), Settings.TCP_PORT));
            ex = Executors.newFixedThreadPool(Settings.N_THREADS_THREAD_POOL);
            SalvaSuFileHandleSIGTERM(ex); //TODO magari fare una variabile globale che quando c'è una cosa nuova si setta a true così salva farla atomica

            while (true) {
                System.out.println("Waiting for clients...");
                try {
                    Socket socClient = server.accept();
                    Worker worker = new Worker(socClient);
                    ex.execute(worker);
                } catch (IOException e) {
                    System.out.println("Client closed connection or some error appeared");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SalvaSuFileHandleSIGTERM(ExecutorService ex){
        Thread thread = new Thread(new Thread(() -> {
            try{
                Thread.sleep(20000);
                System.out.println("/!\\ LOG: Salvataggio automatico in corso...");
                Storage.writeObjectToJSONFile(Settings.JSON_FILENAME, UtentiConnessi.getInstance().getHashListaUtenti());
                System.out.println("/!\\ LOG: Salvataggio completato.");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }));

        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("/!\\ Shutdown hook ran! /!\\");
            ex.shutdownNow();
            ex.shutdown();
            while(!ex.isTerminated()){}

            try{
                thread.join();
                thread.interrupt();
            }catch (InterruptedException ecc){
                System.out.println("Interrupt ricevuto " + ecc.getMessage());
            }
            Storage.writeObjectToJSONFile(Settings.JSON_FILENAME, UtentiConnessi.getInstance().getHashListaUtenti());
            System.out.println("LOG: Salvataggio completato.");
        }));
    }
}
