package errori;

public class NessunaRispostaDiSfida extends RuntimeException {
    public NessunaRispostaDiSfida(){

    }

    public NessunaRispostaDiSfida(String ecc){
        super(ecc);
    }
}
