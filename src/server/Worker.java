package server;

import Errori.UserAlreadyLoggedIn;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.StringTokenizer;

public class Worker implements Runnable {

    private Socket client;

    public Worker(Socket c){
        this.client = c;
    }
    
    public void run() {
        try{
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            BufferedOutputStream outToClient = new BufferedOutputStream(client.getOutputStream());

            String message = inFromClient.readLine();
            if (message == null) {
                client.close();
                return;
            }
            
            StringTokenizer tokenizedLine = new StringTokenizer(message);

            if(tokenizedLine.nextToken().equals("LOGIN")){
                String username = tokenizedLine.nextToken();
                String pw = tokenizedLine.nextToken();

                login(username, pw);
                String response = "Login eseguito con successo";
                outToClient.write(response.getBytes(), 0, response.length());
            }

            if (tokenizedLine.nextToken().equals("LOGOUT")) {
                String username = tokenizedLine.nextToken();

                logout(username);
                String response = "Logout eseguito con successo";
                outToClient.write(response.getBytes(), 0, response.length());
            }

            if (tokenizedLine.nextToken().equals("ADD_FRIEND")) {

            }
            if (tokenizedLine.nextToken().equals("LISTA_AMICI")) {

            }
            if (tokenizedLine.nextToken().equals("SFIDA")) {

            }
            if (tokenizedLine.nextToken().equals("MOSTRA_SCORE")) {

            }
            if (tokenizedLine.nextToken().equals("MOSTRA_CLASSIFICA")) {

            }

            // read and print out the rest of the request
            message = inFromClient.readLine();
            while (message != null) {
                System.out.println("- Request: " + message);
                message = inFromClient.readLine();
            }

            client.close();
            inFromClient.close();
            System.out.println("Client closed connection");
        }catch (IOException ecc){

        }
    }

    public void login(String nickUtente, String password) {
        if(nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if(password == null || password.length() == 0) throw new IllegalArgumentException();
        if(UtentiConnessi.getInstance().isConnect(nickUtente)) throw new UserAlreadyLoggedIn();


    }
    
    public void logout(String nickUtente) {
        if(nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();

    }

    public void aggiungi_amico(String nickUtente, String nickAmico) {

    }

    public void lista_amici(String nickUtente) {

    }

    public void sfida(String nickUtente, String nickAmico) {

    }
    
    public void mostra_punteggio(String nickUtente) {

    }
    
    public void mostra_classifica(String nickUtente) {

    }
}