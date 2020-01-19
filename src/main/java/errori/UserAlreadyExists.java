package errori;

public class UserAlreadyExists extends RuntimeException {
    public UserAlreadyExists() {

    }

    public UserAlreadyExists(String ecc) {
        super(ecc);
    }
}
