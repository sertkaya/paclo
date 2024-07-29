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
	private HashMap<OWLNamedIndividual, ArrayList<OWLClassExpression>> instanceTypes;

	private OWLNamedIndividual[] instanceNames;
	private long[] instanceWeights;
	private long cumulativeInstanceWeight;

	private boolean uniform_conclusions = false;

	private static Random rd = new Random();


	public WeightedSubsumptionSampler(Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner, boolean uniform_conclusions) {
		this.baseSet = baseSet;
		this.uniform_conclusions = uniform_conclusions;
		update_sampler(initialOntologyReasoner);
	}
	
	public WeightedSubsumptionSampler(Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner) {
		this(baseSet, initialOntologyReasoner, false);
	}

	public Pair<Set<OWLClassExpression>, OWLClassExpression> sample() {
		Set<OWLClassExpression> premise = samplePremise();
		return new Pair<>(premise, sampleConclusion(premise));
	}

	public void update_sampler(OWLReasoner reasoner) {
		logger.info("Update starting");
		instanceTypes = new HashMap<OWLNamedIndividual, ArrayList<OWLClassExpression>>();
		for (OWLClassExpression ce : baseSet) {
			System.out.print(ce + ": ");
			Set<OWLNamedIndividual> instances = reasoner.getInstances(ce, true).getFlattened();
			System.out.println(instances.size());
			instanceCounts.put(ce, instances.size());
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
		logger.info("Update finished");
	}

	private Set<OWLClassExpression> samplePremise() {
		Set<OWLClassExpression> premise = new HashSet<OWLClassExpression>();
		do {
			premise.clear();
			for (OWLClassExpression expr : instanceTypes.get(instanceNames[getRandomIndex(instanceWeights, cumulativeInstanceWeight)])) {
				if (rd.nextBoolean()) {
					premise.add(expr);
				}
			}
		} while (premise.size() == baseSet.size());
		return premise;
	}

	private OWLClassExpression sampleConclusion(Set<OWLClassExpression> premise) {
		Set<OWLClassExpression> remaining = new HashSet<OWLClassExpression>(baseSet);
		remaining.removeAll(premise);
		assert remaining.size() > 0;

		OWLClassExpression[] types = remaining.toArray(new OWLClassExpression[0]);
		if (this.uniform_conclusions) {
			return types[rd.nextInt(types.length)];
		}

		long[] weights = new long[types.length];
		long total = 0;
		for (int i = 0; i < types.length; ++i) {
			total += (instanceNames.length - instanceCounts.get(types[i]));
			weights[i] = total;
		}

		return types[getRandomIndex(weights, total)];
	}



	private static int getRandomIndex(long[] weights, long total) {
		int index = Math.abs(Arrays.binarySearch(weights, Math.abs(rd.nextLong()) % total) + 1);
		while (index > 0 && weights[index] == weights[index - 1]) {
			index--;
		}
		return index; // TODO: Check if this is correct.
	}

}
