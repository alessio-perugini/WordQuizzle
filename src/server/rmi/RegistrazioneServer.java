package server.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RegistrazioneServer {

    int PORT = 30000;

    public RegistrazioneServer(){

    }

    public RegistrazioneServer(int port){
        PORT = (port > 0 ) ? port : PORT;
    }

    public void start(){
        try{
            Registrazione reg = new Registrazione();
            RegistrazioneService stub = (RegistrazioneService) UnicastRemoteObject.exportObject(reg, 0);

            //Creo regestry sulla PORT
            LocateRegistry.createRegistry(PORT);
            Registry r = LocateRegistry.getRegistry(PORT);

            //pubblico lo stub nel regestry
            r.rebind("REGISTRAZIONE-SERVER", stub);

            System.out.println("RMI registrazione ready!");
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
