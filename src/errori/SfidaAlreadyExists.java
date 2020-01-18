package errori;

public class SfidaAlreadyExists extends RuntimeException {
    public SfidaAlreadyExists() {

    }

    public SfidaAlreadyExists(String ecc) {
        super(ecc);
    }
}
