package Errori;

public class FriendAlreadyExists extends RuntimeException {
    public FriendAlreadyExists(){

    }

    public FriendAlreadyExists(String ecc){
        super(ecc);
    }
}
