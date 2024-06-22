package fca;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class FormalContext {

    protected static final Logger logger = LogManager.getLogger();

    private ArrayList<Pair<Object, BitSet>> matrix;
    private Object[] attributes;

    public FormalContext(Set attributes) {
        this.attributes = attributes.toArray();
        this.matrix = new ArrayList<Pair<Object, BitSet>>();
    }

    public int getAttributeCount() {
        return(this.attributes.length);
    }

    public int getObjectCount() {
        return(this.matrix.size());
    }

    public boolean addObject(Object o, Set intent) {
        BitSet b = this.toBitSet(intent);
        return(this.matrix.add(new Pair(o, b)));
    }

    public Object[] getAttributes() {
        return(this.attributes);
    }

    // objects -> attributes
    private BitSet upArrow(Set<Integer> x) {
        BitSet result = new BitSet(this.getAttributeCount());
        result.set(0, this.getAttributeCount() - 1, true);
        for (Integer i : x) {
            result.and(this.matrix.get(i).getValue());
        }

        return(result);
    }

    private BitSet toBitSet(Set y) {
        BitSet b = new BitSet(this.getAttributeCount());
        // b.set(0, this.getAttributeCount() - 1, false);
        b.clear();
        for (int i = 0; i < this.getAttributeCount(); ++i) {
            if (y.contains(this.attributes[i]))
                b.set(i);
        }
        return(b);
    }

    // attributes -> objects
    private Set<Integer> downArrow(BitSet y) {
        Set<Integer> result = new HashSet<Integer>();
        if (y.isEmpty()) {
            for (int i = 0; i < this.getObjectCount(); ++i)
                result.add(i);
            return(result);
        }
        for (int i = 0; i < this.getObjectCount(); ++i) {
            BitSet tmp = (BitSet) this.matrix.get(i).getValue().clone();
            tmp.and(y);
            if (tmp.equals(y))
                result.add(i);
        }

        return(result);
    }

    public boolean satisfiesImplication(Set premise, Set conclusion) {
        Set cPrime =  this.downArrow(this.toBitSet(conclusion));
        Set pPrime =  this.downArrow(this.toBitSet(premise));
        return(cPrime.containsAll(pPrime));
    }

}
