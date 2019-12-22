package errori;

public class FriendNotConnected extends RuntimeException {
    public FriendNotConnected(){

    }

    public FriendNotConnected(String ecc){
        super(ecc);
    }
}
