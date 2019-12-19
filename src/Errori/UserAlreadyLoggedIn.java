package Errori;

public class UserAlreadyLoggedIn extends RuntimeException {
    public UserAlreadyLoggedIn(){

    }

    public UserAlreadyLoggedIn(String ecc){
        super(ecc);
    }
}
