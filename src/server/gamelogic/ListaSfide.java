package server.gamelogic;

import errori.UserDoesntExists;

import java.util.concurrent.ConcurrentHashMap;

public class ListaSfide {

    private static ListaSfide instance;

    public ConcurrentHashMap<Integer, server.gamelogic.Sfida> getHashListaSfide() {
        return hashListaSfide;
    }

    private ConcurrentHashMap<Integer, server.gamelogic.Sfida> hashListaSfide;

    private ListaSfide() {
        hashListaSfide = new ConcurrentHashMap<>();
    }

    public static synchronized ListaSfide getInstance() {
        if (instance == null) instance = new ListaSfide();
        return instance;
    }

    public synchronized Sfida addSfida(Sfida sfida) {
        if (sfida == null) throw new IllegalArgumentException();
        //TODO controllare sfide duplicate ?

        return hashListaSfide.putIfAbsent(sfida.getIdSfida(), sfida);
    }

    public synchronized void removeSfida(Sfida sfida) {
        if (sfida == null) throw new IllegalArgumentException();
        if (hashListaSfide.isEmpty()) throw new UserDoesntExists();
        //TODO sistemare
        hashListaSfide.remove(sfida);
    }

    public Sfida getSfida(String key) {
        if (key == null || key.length() == 0) throw new IllegalArgumentException();
        if (hashListaSfide.isEmpty()) throw new UserDoesntExists();

        return hashListaSfide.get(key);
    }
}
