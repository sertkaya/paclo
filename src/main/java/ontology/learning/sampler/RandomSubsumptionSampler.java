package ontology.learning.sampler;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
		do {
			premise.clear();
			for (OWLClassExpression expr : baseSet) {
				if (rd.nextBoolean()) {
					premise.add(expr);
				}
			}
		} while (premise.containsAll(baseSet));

		Set<OWLClassExpression> remaining = new HashSet<OWLClassExpression>(baseSet);
		remaining.removeAll(premise);
		int k = rd.nextInt(remaining.size());
		int i = 0;
		for (OWLClassExpression conclusion : remaining) {
			if (i == k) {
				return new Pair<Set<OWLClassExpression>, OWLClassExpression>(premise, conclusion);
			}
			i++;
		}
		throw new IllegalStateException("Error in sampling.");
	}
}
