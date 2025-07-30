package ontology.learning.sampler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import javafx.util.Pair;

public class ABoxInducedSubsumptionSampler implements SubsumptionSamplingOracle {
	private OWLNamedIndividual[] individuals;
	private OWLClassExpression[] baseConcepts;
	private Map<OWLNamedIndividual, ArrayList<OWLClassExpression>> individualTypes;
	private long[] noninstanceCounts;

	private boolean uniformConclusions;

	private Logger logger = LogManager.getLogger("ABoxInducedSubsumptionSampler");

    public ABoxInducedSubsumptionSampler(Set<OWLClassExpression> baseSet, OWLReasoner reasoner, boolean uniformConclusions) {
		this.baseConcepts = baseSet.toArray(new OWLClassExpression[0]);;
		this.uniformConclusions = uniformConclusions;

		individuals = reasoner.getRootOntology().getIndividualsInSignature().toArray(new OWLNamedIndividual[0]);
		individualTypes = new HashMap<>();
		noninstanceCounts = new long[baseConcepts.length];
		update_sampler(reasoner, true);
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

		individualTypes.clear();
		
		for (int i = 0; i < baseConcepts.length; ++i) {
			OWLClassExpression ce = baseConcepts[i];
			Set<OWLNamedIndividual> instances = reasoner.getInstances(ce).getFlattened();
			if (updateConclusion && !uniformConclusions) {
				noninstanceCounts[i] = individuals.length - instances.size();
			}
			for (OWLNamedIndividual ind : instances) {
				individualTypes.computeIfAbsent(ind, k -> new ArrayList<>()).add(ce);
			}
		}
		logger.info("Individuals with types:" + individualTypes.keySet().size());
		logger.info("Update finished");
    }

    public Pair<Set<OWLClassExpression>, OWLClassExpression> sample() {
		Set<OWLClassExpression> premise = samplePremise();
		return new Pair<>(premise, sampleConclusion(premise));
    }

	private Set<OWLClassExpression> samplePremise() {
		Set<OWLClassExpression> premise = new HashSet<OWLClassExpression>();
		do {
			premise.clear();
			OWLNamedIndividual ind = individuals[ThreadLocalRandom.current().nextInt(individuals.length)];
			if (individualTypes.containsKey(ind)) {
				for (OWLClassExpression expr : individualTypes.get(ind)) {
					if (ThreadLocalRandom.current().nextBoolean()) {
						premise.add(expr);
					}
				}
			}
			/* This is an alternative method, which seems too slow.
			for (OWLClassExpression expr : baseConcepts) {
				if (ThreadLocalRandom.current().nextBoolean() && isInstance(ind, expr)) {
					premise.add(expr);
				}
			}
			*/
		} while (premise.size() == baseConcepts.length);
		return premise;        
    }

	/*
	private boolean isInstance(OWLNamedIndividual ind, OWLClassExpression expr) {
		return reasoner.isEntailed(reasoner.getRootOntology()
										   .getOWLOntologyManager()
										   .getOWLDataFactory()
										   .getOWLClassAssertionAxiom(expr, ind));
	}
	*/

	private OWLClassExpression sampleConclusion(Set<OWLClassExpression> premise) {
		Set<OWLClassExpression> remaining = new HashSet<>(Arrays.asList(baseConcepts));
		remaining.removeAll(premise);
		assert remaining.size() > 0;
		OWLClassExpression[] types = remaining.toArray(new OWLClassExpression[0]);

		if (uniformConclusions) {
			return types[ThreadLocalRandom.current().nextInt(types.length)];
		}


		long[] weights = new long[types.length];
		if (!premise.contains(types[0])) {
			weights[0] = noninstanceCounts[0];
		}
		for (int i = 1; i < types.length; ++i) {
			if (!premise.contains(types[i])) {
				weights[i] = weights[i - 1] + noninstanceCounts[i];
			}
		}

		return types[randomIndex(weights)];
	}

	private int randomIndex(long[] weights) {
		int low = 0;
		int high = weights.length - 1;
		long r = ThreadLocalRandom.current().nextLong(weights[high]);
		while (low < high) {
			int mid = (low + high) / 2;
			if (r < weights[mid]) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return low;
	}
 }
