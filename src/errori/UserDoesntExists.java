package errori;

public class UserDoesntExists extends RuntimeException {
    public UserDoesntExists(){

    }

    public UserDoesntExists(String ecc){
        super(ecc);
    }
}
