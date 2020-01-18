package errori;

public class InvalidPassword extends RuntimeException {
    public InvalidPassword() {

    }

    public InvalidPassword(String ecc) {
        super(ecc);
    }
}
