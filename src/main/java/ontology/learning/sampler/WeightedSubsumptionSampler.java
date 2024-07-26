package ontology.learning.sampler;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WeightedSubsumptionSampler implements SubsumptionSamplingOracle {
	private Logger logger = LogManager.getLogger("WeightedSubsumptionSampler");

	private Set<OWLClassExpression> baseSet;

	// Key: Class expression from base set
	// Value: Number of its instances
	private HashMap<OWLClassExpression, Integer> instanceCounts = new HashMap<OWLClassExpression, Integer>();
	// Key: Individual name from the ABox
	// Value: Number of class expressions from the base set, that contain this individual as instance
	private HashMap<OWLNamedIndividual, Integer> typeCounts = new HashMap<OWLNamedIndividual, Integer>();
	public WeightedSubsumptionSampler(Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner) {
		this.baseSet = baseSet;

		for (OWLClassExpression ce : baseSet) {
			Set<OWLNamedIndividual> instances = initialOntologyReasoner.getInstances(ce).getFlattened();
			instanceCounts.put(ce, instanceCounts.size());
			for (OWLNamedIndividual ind : instances) {
				if (typeCounts.containsKey(ind))
					typeCounts.put(ind,typeCounts.get(ind) + 1);
				else
					typeCounts.put(ind, 1);
			}
		}
	}
	
	public Pair<Set<OWLClassExpression>, OWLClassExpression> sample() {
		// TODO: Here comes the implementation of the weighted sampler.
		// Access the frequencies over the hashmaps this.instanceCounts and this.typeCounts

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
