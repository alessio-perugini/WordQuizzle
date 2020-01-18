package errori;

public class FriendNotFound extends RuntimeException {
    public FriendNotFound() {

    }

    public FriendNotFound(String ecc) {
        super(ecc);
    }
}
