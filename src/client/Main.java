package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        try {
            SocketAddress address = new InetSocketAddress("127.0.0.1", 1500);
            SocketChannel client = SocketChannel.open(address);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            RmiClient rmiReg = new RmiClient();
            System.out.println(rmiReg.registra_utente("asd", "dsadsadsa"));
            System.out.println(rmiReg.registra_utente("boia", "dsaasdasd"));
            scriviLeggi("LOGIN asd dsadsadsa\n", client);
            scriviLeggi("ADD_FRIEND asd boia\n", client);
            scriviLeggi("ADD_FRIEND asd xD\n", client);
            scriviLeggi("LISTA_AMICI asd\n", client);
            scriviLeggi("MOSTRA_SCORE asd\n", client);
            scriviLeggi("MOSTRA_CLASSIFICA asd\n", client);
            scriviLeggi("LOGOUT asd\n", client);

            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void scriviLeggi(String messaggio, SocketChannel client) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            messaggio += "\n";
            byte[] mex = messaggio.getBytes(StandardCharsets.UTF_8);
            buffer.put(mex);
            buffer.flip();//Serve per far leggere dall'inizio al server
            client.write(buffer);

            buffer.clear();
            client.read(buffer);
            String response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
