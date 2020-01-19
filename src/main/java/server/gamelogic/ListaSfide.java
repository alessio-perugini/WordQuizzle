package server.gamelogic;

import errori.SfidaAlreadyExists;
import errori.UserDoesntExists;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ListaSfide tiene traccia di tutte le sfide che sono presenti sul server. Il suo compito è quello di fornire
 * informazioni utili da dare al thread che fa pooling su questa struttura per verificare che la partita sia finita
 * e decretare così il vincitore. Utilizza il signleton pattern quindi è garantio avere 1 sola istanza di questa classe.
 */
public class ListaSfide {

    private static ListaSfide instance;

    /**
     * @return ritorna la lista di sfide
     */
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

    /**
     * Aggiunge alla lista la sfida passata per param
     *
     * @param sfida oggetto sfida valido da aggiungire alla lista
     * @return null se non è presenta alcuna sfida
     */
    public synchronized Sfida addSfida(Sfida sfida) {
        if (sfida == null) throw new IllegalArgumentException();
        if (!hashListaSfide.isEmpty() && hashListaSfide.get(sfida.getIdSfida()) != null)
            throw new SfidaAlreadyExists("La sfida è già stata aggiunta");

        return hashListaSfide.putIfAbsent(sfida.getIdSfida(), sfida);
    }

    /**
     * Rimuove la sfida dalla lista, se ne occupa il th che fa pooling
     *
     * @param sfida oggetto sfida valido da aggiungire alla lista
     */
    public synchronized void removeSfida(Sfida sfida) {
        if (sfida == null) throw new IllegalArgumentException();
        if (hashListaSfide.isEmpty()) throw new UserDoesntExists();

        hashListaSfide.remove(sfida);
    }
}
