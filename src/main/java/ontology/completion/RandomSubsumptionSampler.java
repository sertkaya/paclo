package ontology.completion;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.obolibrary.obo2owl.OwlStringTools.OwlStringException;
import org.semanticweb.owlapi.model.OWLClassExpression;

import javafx.util.Pair;

public class RandomSubsumptionSampler implements SubsumptionSamplingOracle {
	private Set<OWLClassExpression> baseSet;
	
	public RandomSubsumptionSampler(Set<OWLClassExpression> baseSet) {
		this.baseSet = baseSet;
	}
	
	/**
	 * Randomly select a subsumption query over baseSet
	 */
	public Pair<Set<OWLClassExpression>, OWLClassExpression> sample() {
		
		Random rd = new Random();
		Set<OWLClassExpression> premise = new HashSet<OWLClassExpression>();
		for (OWLClassExpression expr : baseSet) {
			if (rd.nextBoolean())
				premise.add(expr);
		}

        int k = rd.nextInt(baseSet.size());
        int i = 0;
        OWLClassExpression conclusion = null;
        for (OWLClassExpression e : baseSet) {
            if (i == k) {
                conclusion = e;
                break;
            }
            i++;
        }
		return new Pair<Set<OWLClassExpression>, OWLClassExpression>(premise, conclusion);
	}
}
