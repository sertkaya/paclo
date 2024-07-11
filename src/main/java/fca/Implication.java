package fca;

import java.util.BitSet;
import java.util.Set;

public class Implication {

    private BitSet premise;
    private BitSet conclusion;

    private Object[] attributes;
    public Implication(Set premise, Set conclusion, Object[] attributes) {
        this.premise = fca.Utils.toBitSet(premise, attributes);
        this.conclusion = fca.Utils.toBitSet(conclusion, attributes);
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return(fca.Utils.toSet(premise, attributes).toString()  + fca.Utils.toSet(conclusion, attributes).toString());
    }
}
