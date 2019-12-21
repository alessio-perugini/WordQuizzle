package server.rmi;

import server.Settings;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RegistrazioneServer {

    public void start(){
        try{
            Registrazione reg = new Registrazione();
            RegistrazioneService stub = (RegistrazioneService) UnicastRemoteObject.exportObject(reg, 0);

            //Creo regestry sulla PORT
            LocateRegistry.createRegistry(Settings.RMI_PORT);
            Registry r = LocateRegistry.getRegistry(Settings.RMI_PORT);

            //pubblico lo stub nel regestry
            r.rebind("REGISTRAZIONE-SERVER", stub);

            System.out.println("RMI registrazione ready!");
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }

}
