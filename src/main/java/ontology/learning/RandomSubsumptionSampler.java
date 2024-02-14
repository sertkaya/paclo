package ontology.learning;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.obolibrary.obo2owl.OwlStringTools.OwlStringException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;

import javafx.util.Pair;

public class RandomSubsumptionSampler implements SubsumptionSamplingOracle {
	private Set<OWLClassExpression> baseSet;
	private OWLClassExpression nothing = OWLManager.createOWLOntologyManager().getOWLDataFactory().getOWLNothing();
	
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

		OWLClassExpression conclusion = nothing;
		if (!premise.containsAll(baseSet)) {
			Set<OWLClassExpression> remaining = new HashSet<OWLClassExpression>(baseSet);
			int k = rd.nextInt(remaining.size());
			int i = 0;
			for (OWLClassExpression e : remaining) {
				if (i == k) {
					conclusion = e;
					break;
				}
				i++;
			}
		}
		
		return new Pair<Set<OWLClassExpression>, OWLClassExpression>(premise, conclusion);
	}
}
