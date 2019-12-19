package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args){
        try {
            SocketAddress address = new InetSocketAddress("127.0.0.1", 1500);
            SocketChannel client = SocketChannel.open(address);

            ByteBuffer buffer = ByteBuffer.allocate(512);
            byte[] mex = "LOGIN asd dsa\n".getBytes(StandardCharsets.UTF_8);
            buffer.put(mex);
            buffer.flip();//Serve per far leggere dall'inizio al server
            client.write(buffer);

            buffer.clear();
            client.read(buffer);
            String response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
            client.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
