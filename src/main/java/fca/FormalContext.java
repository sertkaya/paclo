package fca;

import org.eclipse.rdf4j.query.algebra.In;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class FormalContext {
    /*
    class BitMatrix {
        public BitSet[] matrix;
        public BitMatrix(int rows, int columns) {
            matrix = new BitSet[rows];
            for (int i = 0; i < rows; ++i)
                matrix[i] = new BitSet(columns);
        }
        public BitSet getRow(int i) {
            return(matrix[i]);
        }

        // public boolean addRow(BitSet b) {
        //     matrix.
        // }
    }
     */

    // private BitMatrix matrix;
    private ArrayList<BitSet> matrix;
    private int objectCount;
    private int attributeCount;
    private Object[] attributes;

    public FormalContext(int objects, Set attributes) {
        this.objectCount = objects;
        this.attributeCount = attributes.size();
        this.attributes = attributes.toArray();
        // this.matrix = new BitMatrix(objects, attributes);
        this.matrix = new ArrayList<BitSet>();
    }

    public boolean addObject(BitSet objectIntent) {
        return(this.matrix.add(objectIntent));
    }

    public Object[] getAttributes() {
        return(this.attributes);
    }

    public BitSet upArrow(Set<Integer> A) {
        BitSet result = new BitSet(this.attributeCount);
        result.set(0, this.attributeCount - 1, true);
        /*
        for (Integer i : A) {
            result.and(this.matrix.getRow(i));
        }
         */
        for (BitSet objectIntent : this.matrix)
            result.and(objectIntent);

        return(result);
    }

    public Set<Integer> downArrow(BitSet B) {
        Set<Integer> result = new HashSet<Integer>();
        for (int i = 0; i < this.matrix.size(); ++i) {
        // for (int i = 0; i < this.objectCount; ++i) {
            // if (this.matrix.getRow(i).stream().allMatch(B::get))
            if (this.matrix.get(i).stream().allMatch(B::get))
                result.add(i);
        }
        return(result);
    }

    public boolean satisfiesImplication(BitSet premise, BitSet conclusion) {
        return(downArrow(conclusion).containsAll(downArrow(premise)));
    }
}
