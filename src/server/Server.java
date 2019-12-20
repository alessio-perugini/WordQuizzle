package server;

import server.storage.Storage;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    int PORT = 1500;
    private int nThreads = 100;

    public Server(int port){
        PORT = (port > 0) ? port : PORT;
    }

    public void start() {
        ExecutorService ex;

        try (ServerSocket server = new ServerSocket();) {
            server.bind(new InetSocketAddress(InetAddress.getByName("localhost"), PORT));
            ex = Executors.newFixedThreadPool(nThreads);
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
                Thread.sleep(20);
                Storage.writeObjectToJSONFile("utenti.json", UtentiConnessi.getInstance().getHashListaUtenti());
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }));

        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook ran!");
            ex.shutdownNow();
            Storage.writeObjectToJSONFile("utenti.json", UtentiConnessi.getInstance().getHashListaUtenti());
        }));

        try{
            thread.join();
            thread.interrupt();
        }catch (InterruptedException ecc){
            System.out.println("Interrupt ricevuto " + ecc.getMessage());
        }


    }
}
