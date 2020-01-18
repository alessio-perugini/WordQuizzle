package errori;

public class FriendIsAlreadyPlaying extends RuntimeException {
    public FriendIsAlreadyPlaying() {

    }

    public FriendIsAlreadyPlaying(String ecc) {
        super(ecc);
    }
}
