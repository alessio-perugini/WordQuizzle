package errori;

public class SfidaRequestRefused extends RuntimeException {
    public SfidaRequestRefused(){

    }

    public SfidaRequestRefused(String ecc){
        super(ecc);
    }
}
