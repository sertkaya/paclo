package ontology.learning.sampler;

import javafx.util.Pair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import java.math.BigInteger;

public class WeightedABoxInducedSubsumptionSampler implements SubsumptionSamplingOracle {
	private Logger logger = LogManager.getLogger("WeightedSubsumptionSampler");

	private Set<OWLClassExpression> baseSet;


	// Key: Class expression from base set
	// Value: Number of its instances
	private HashMap<OWLClassExpression, Integer> instanceCounts = new HashMap<OWLClassExpression, Integer>();
	// Key: Individual name from the ABox
	// Value: ArrayList of class expressions from the base set that have this individual as an instance
	private HashMap<OWLNamedIndividual, ArrayList<OWLClassExpression>> instanceTypes;

	private OWLNamedIndividual[] instanceNames;
	private BigInteger[] instanceWeights;
	private BigInteger cumulativeInstanceWeight = BigInteger.ZERO;
	private long numberOfInstances;

	private boolean uniformConclusions = false;

	private static Random rd = new Random();


	public WeightedABoxInducedSubsumptionSampler(Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner, boolean uniformConclusions) {
		this.baseSet = baseSet;
		this.uniformConclusions = uniformConclusions;
		numberOfInstances = initialOntologyReasoner.getRootOntology().getIndividualsInSignature().size();
		update_sampler(initialOntologyReasoner, true);
	}
	
	public WeightedABoxInducedSubsumptionSampler(Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner) {
		this(baseSet, initialOntologyReasoner, false);
	}

	public Pair<Set<OWLClassExpression>, OWLClassExpression> sample() {
		Set<OWLClassExpression> premise = samplePremise();
		return new Pair<>(premise, sampleConclusion(premise));
	}

	public void update_sampler(OWLReasoner reasoner, boolean updateConclusion) {
		logger.info("Update starting");
		if (updateConclusion) {
			logger.info("Update conclusions");
		} else {
			logger.info("Do not update conclusions");
		}
		if (this.uniformConclusions) {
			logger.info("Uniform conclusions");
		} else {
			logger.info("Nonunifrom conclusions");
		}
		instanceTypes = new HashMap<OWLNamedIndividual, ArrayList<OWLClassExpression>>();
		for (OWLClassExpression ce : baseSet) {
			// System.out.print(ce + ": ");
			Set<OWLNamedIndividual> instances = reasoner.getInstances(ce).getFlattened();
			if (instances.size() > 0)
				logger.debug(ce + ":" + instances.size());
			// System.out.println(instances.size());
			if (updateConclusion) {
				instanceCounts.put(ce, instances.size());
			}
			for (OWLNamedIndividual ind : instances) {
				if (instanceTypes.containsKey(ind)) {
					instanceTypes.get(ind).add(ce);
				} else {
					instanceTypes.put(ind, new ArrayList<>(Collections.singletonList(ce)));
				}
			}
		}

		instanceNames = new OWLNamedIndividual [instanceTypes.size()];
		instanceWeights = new BigInteger [instanceTypes.size()];

		int i = 0;
		cumulativeInstanceWeight = BigInteger.ZERO;
		for (Map.Entry<OWLNamedIndividual, ArrayList<OWLClassExpression>> entry : instanceTypes.entrySet()) {
			instanceNames[i] = entry.getKey();
			//assert cumulativeInstanceWeight + (1 << entry.getValue().size()) > cumulativeInstanceWeight :
			//																	"Weights are too large.";
			// cumulativeInstanceWeight += (1 << entry.getValue().size());
			cumulativeInstanceWeight = cumulativeInstanceWeight.add(BigInteger.ONE.shiftLeft(entry.getValue().size()));
			instanceWeights[i++] = cumulativeInstanceWeight;
			logger.info(instanceNames[i - 1] + ": " + instanceWeights[i - 1]);
		}
		logger.info("cumulative instance weight:" + cumulativeInstanceWeight);
		logger.info("Update finished");
		logger.info("Instances: " + numberOfInstances);
//		for (OWLClassExpression ce : baseSet) {
//			logger.info(ce + ": " + (numberOfInstances - instanceCounts.get(ce)));
//		}
	}

	private Set<OWLClassExpression> samplePremise() {
		Set<OWLClassExpression> premise = new HashSet<OWLClassExpression>();
		do {
			premise.clear();
			int i = getRandomIndex(instanceWeights, cumulativeInstanceWeight);
			OWLNamedIndividual ind = instanceNames[i];
			logger.info("Premise from " + ind + " (" + i + "/" +instanceWeights.length + ")");
			for (OWLClassExpression expr : instanceTypes.get(ind)) {
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
		if (this.uniformConclusions) {
			return types[rd.nextInt(types.length)];
		}

		long[] weights = new long[types.length];
		long total = 0;
		for (int i = 0; i < types.length; ++i) {
			total += (numberOfInstances - instanceCounts.get(types[i]));
			weights[i] = total;
		}

		return types[getRandomIndex(weights, total)];
	}

	private static int getRandomIndex(long[] weights, long total) {
		int index = Math.abs(Arrays.binarySearch(weights, Math.abs(rd.nextLong()) % total) + 1);
		if (index == weights.length)
			index--;
		while (index > 0 && weights[index] == weights[index - 1]) {
			index--;
		}
		return index; // TODO: Check if this is correct.
	}

	private static int getRandomIndex(BigInteger[] weights, BigInteger total) {
		BigInteger r;
		do {
			r = new BigInteger(total.bitLength(), rd);
		} while (r.compareTo(total) >= 0);
		// int index = Math.abs(Arrays.binarySearch(weights, Math.abs(rd.nextLong()) % total) + 1);
		int index = Math.abs(Arrays.binarySearch(weights, r) + 1);
		if (index == weights.length)
			index--;
		while (index > 0 && weights[index] == weights[index - 1]) {
			index--;
		}
		return index; // TODO: Check if this is correct.
	}

}
