package Errori;

public class PasswordNotValid extends RuntimeException {
    public PasswordNotValid(){

    }

    public PasswordNotValid(String ecc){
        super(ecc);
    }
}
