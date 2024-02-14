package ontology.learning.sampler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A random sampling oracle
 *
 */
public class RandomSampler implements SamplingOracle {
	private Set<OWLClassExpression> baseSet;
	
	public RandomSampler(Set<OWLClassExpression> baseSet) {
		this.baseSet = baseSet;
	}
	
	/**
	 * Randomly select a subset of base set
	 */
	public Set<OWLClassExpression> sample() {
		
		Random rd = new Random();
		Set<OWLClassExpression> sample = new HashSet<OWLClassExpression>();
		for (OWLClassExpression expr : baseSet) {
			if (rd.nextBoolean())
				sample.add(expr);
		}
		return(sample);
	}
}
