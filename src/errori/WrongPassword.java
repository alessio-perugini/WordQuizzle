package errori;

public class WrongPassword extends RuntimeException {
    public WrongPassword(){

    }

    public WrongPassword(String ecc){
        super(ecc);
    }
}
