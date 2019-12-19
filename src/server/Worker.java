package server;

import Errori.FriendAlreadyExists;
import Errori.FriendNotFound;
import Errori.UserAlreadyLoggedIn;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class Worker implements Runnable {

    private Socket client;
    private BufferedOutputStream outToClient;

    public Worker(Socket c){
        this.client = c;
    }
    
    public void run() {
        try{
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
            outToClient = new BufferedOutputStream(client.getOutputStream());

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
            }

            if (tokenizedLine.nextToken().equals("LOGOUT")) {
                String username = tokenizedLine.nextToken();

                logout(username);
            }

            if (tokenizedLine.nextToken().equals("ADD_FRIEND")) {
                String nickUtente = tokenizedLine.nextToken();
                String nickAmico = tokenizedLine.nextToken();

                aggiungi_amico(nickUtente, nickAmico);
            }
            if (tokenizedLine.nextToken().equals("LISTA_AMICI")) {
                String nickUtente = tokenizedLine.nextToken();
                lista_amici(nickUtente);
            }
            if (tokenizedLine.nextToken().equals("SFIDA")) { //TODO UDP request

            }
            if (tokenizedLine.nextToken().equals("MOSTRA_SCORE")) {
                String nickUtente = tokenizedLine.nextToken();
                mostra_punteggio(nickUtente);
            }
            if (tokenizedLine.nextToken().equals("MOSTRA_CLASSIFICA")) {
                String nickUtente = tokenizedLine.nextToken();
                mostra_classifica(nickUtente);
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
        if(UtentiConnessi.getInstance().isConnected(nickUtente)) throw new UserAlreadyLoggedIn();

        //TODO check se esiste nel file

        sendResponseToClient("Login eseguito con successo");
    }
    
    public void logout(String nickUtente) {
        if(nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if(!UtentiConnessi.getInstance().isConnected(nickUtente)) throw new UserAlreadyLoggedIn();

        sendResponseToClient("Logout eseguito con successo");
    }

    public void aggiungi_amico(String nickUtente, String nickAmico) {
        if(nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException();
        if(nickAmico == null || nickAmico.length() == 0) throw new IllegalArgumentException("nickAmico arrato");

        UtentiConnessi connectedUSers = UtentiConnessi.getInstance();
        Utente profileRichiedente = connectedUSers.getUser(nickUtente);
        Utente profileAmico = connectedUSers.getUser(nickAmico);

        if(profileRichiedente == null || profileAmico == null) throw new FriendNotFound("Amico non trovato");

        try{
            profileRichiedente.addFriend(profileAmico.getNickname());
        }catch (FriendAlreadyExists e){
            sendResponseToClient(e.getMessage());
            return;
        }catch (IllegalArgumentException e2){
            sendResponseToClient("Nome amico non valido");
            return;
        }

        sendResponseToClient("Amicizia " + nickUtente + "-" + nickAmico + " creata");
    }

    public void lista_amici(String nickUtente) {
        if(nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

    }

    public void sfida(String nickUtente, String nickAmico) {

    }
    
    public void mostra_punteggio(String nickUtente) {
        if(nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

    }
    
    public void mostra_classifica(String nickUtente) {
        if(nickUtente == null || nickUtente.length() == 0) throw new IllegalArgumentException("nickUtente arrato");

    }

    private void sendResponseToClient(String testo){
        if(testo == null) throw new IllegalArgumentException();
        try{
            outToClient.write(testo.getBytes(), 0, testo.length());
        }catch (IOError | IOException ecc){
            ecc.printStackTrace();
        }
    }
}