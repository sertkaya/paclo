package ontology.learning.sampler;

import javafx.util.Pair;

import org.apache.commons.lang3.ObjectUtils.Null;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WeightedSubsumptionSampler implements SubsumptionSamplingOracle {
	private Logger logger = LogManager.getLogger("WeightedSubsumptionSampler");

	private Set<OWLClassExpression> baseSet;

	// Key: Class expression from base set
	// Value: Number of its instances
	private HashMap<OWLClassExpression, Integer> instanceCounts = new HashMap<OWLClassExpression, Integer>();
	// Key: Individual name from the ABox
	// Value: ArrayList of class expressions from the base set that have this individual as an instance
	private HashMap<OWLNamedIndividual, ArrayList<OWLClassExpression>> instanceTypes =
										new HashMap<OWLNamedIndividual, ArrayList<OWLClassExpression>>();

	private OWLNamedIndividual[] instanceNames;
	private long[] instanceWeights;
	private long cumulativeInstanceWeight;

	public WeightedSubsumptionSampler(Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner) {
		this.baseSet = baseSet;

		for (OWLClassExpression ce : baseSet) {
			Set<OWLNamedIndividual> instances = initialOntologyReasoner.getInstances(ce).getFlattened();
			instanceCounts.put(ce, instanceCounts.size());
			for (OWLNamedIndividual ind : instances) {
				if (instanceTypes.containsKey(ind)) {
					instanceTypes.get(ind).add(ce);
				} else {
					instanceTypes.put(ind, new ArrayList<>(Collections.singletonList(ce)));
				}
			}
		}

		instanceNames = new OWLNamedIndividual [instanceTypes.size()];
		instanceWeights = new long [instanceTypes.size()];

		int i = 0;
		cumulativeInstanceWeight = 0;
		for (Map.Entry<OWLNamedIndividual, ArrayList<OWLClassExpression>> entry : instanceTypes.entrySet()) {
			instanceNames[i] = entry.getKey();
			assert cumulativeInstanceWeight + (1 << entry.getValue().size()) > cumulativeInstanceWeight :
																				"Weights are too large.";
			cumulativeInstanceWeight += (1 << entry.getValue().size());
			instanceWeights[i++] = cumulativeInstanceWeight;
		}
	}
	
	public Pair<Set<OWLClassExpression>, OWLClassExpression> sample() {
		// Here comes the implementation of the weighted sampler.

		Set<OWLClassExpression> premise = samplePremise();


		Set<OWLClassExpression> remaining = new HashSet<OWLClassExpression>(baseSet);
		
		remaining.removeAll(premise);
		int k = new Random().nextInt(remaining.size());
		int i = 0;
		for (OWLClassExpression conclusion : remaining) {
			if (i == k) {
				return new Pair<Set<OWLClassExpression>, OWLClassExpression>(premise, conclusion);
			}
			i++;
		}
		throw new IllegalStateException("Error in sampling.");
	}

	private Set<OWLClassExpression> samplePremise() {
		Random rd = new Random();

		long weight = Math.abs(rd.nextLong()) % cumulativeInstanceWeight;
		int index = Math.abs(Arrays.binarySearch(instanceWeights, weight) + 1); // TODO: Check if this is correct.

		Set<OWLClassExpression> premise = new HashSet<OWLClassExpression>();
		for (OWLClassExpression expr : instanceTypes.get(instanceNames[index])) {
			if (rd.nextBoolean()) {
				premise.add(expr);
			}
		}
		return premise;
	}
}
