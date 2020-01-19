package server.gamelogic;

import errori.SfidaAlreadyExists;
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
        if (!hashListaSfide.isEmpty() && hashListaSfide.get(sfida.getIdSfida()) != null)
            throw new SfidaAlreadyExists("La sfida è già stata aggiunta");

        return hashListaSfide.putIfAbsent(sfida.getIdSfida(), sfida);
    }

    public synchronized void removeSfida(Sfida sfida) {
        if (sfida == null) throw new IllegalArgumentException();
        if (hashListaSfide.isEmpty()) throw new UserDoesntExists();

        hashListaSfide.remove(sfida);
    }

    public Sfida getSfida(Integer key) {
        if (key == null || key == 0) throw new IllegalArgumentException();
        if (hashListaSfide.isEmpty()) throw new UserDoesntExists();

        return hashListaSfide.get(key);
    }
}
