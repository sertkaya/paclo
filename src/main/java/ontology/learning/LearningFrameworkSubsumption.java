package ontology.learning;

import javafx.util.Pair;
import ontology.learning.sampler.SubsumptionSamplingOracle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;


// with subsumption queries
public class LearningFrameworkSubsumption {

	private OWLOntology initialOntology;
	private OWLOntologyManager om;
	private OWLDataFactory df;
	private OWLReasoner initialReasoner;

	private Set<OWLClassExpression> baseSet;
	private ExpertOracle expert;
	private SubsumptionSamplingOracle sampler;

	private int expertQueries = 0;
	private int samplerQueries = 0;

	private Logger logger = LogManager.getLogger("LearningFrameworkSubsumption");

	private Hashtable<OWLClassExpression, ArrayList<Set<OWLClassExpression>>> wrongImplicationHash;

	/**
	 * @param initialOntology: The initial ontology
	 * @param baseSet: Set with the concept descriptions
	 * @param expert: The domain expert
	 * @param sampler: Sampling oracle
	 * @param om: The ontology manager
	 * @param initialReasoner
	 */
	public LearningFrameworkSubsumption(OWLOntology initialOntology,
										Set<OWLClassExpression> baseSet,
										ExpertOracle expert,
										SubsumptionSamplingOracle sampler,
										OWLOntologyManager om,
										OWLReasoner initialReasoner) {

		this.baseSet = baseSet;
		this.initialOntology = initialOntology;

		this.om = om;
		this.df = om.getOWLDataFactory();
		this.initialReasoner = initialReasoner;

		this.expert = expert;
		this.sampler = sampler;
		
		this.expertQueries = 0;
		this.wrongImplicationHash = null;


	}

	private boolean isImplicationValid(Set<OWLClassExpression> premise, OWLClassExpression conclusion) {

		for (Set<OWLClassExpression> wrongPremise : wrongImplicationHash.get(conclusion)) {
			if (wrongPremise.containsAll(premise)) {
				return false;
			}
		}

		OWLClassExpression queryConjunction = premise.isEmpty() ? df.getOWLThing() : this.df.getOWLObjectIntersectionOf(premise);
		OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(queryConjunction, conclusion);

		if (this.initialReasoner.isEntailed(ax)) {
			return (true);
		}

		expertQueries++;
		if (this.expert.holds(ax)) {
			return true;
		}

		wrongImplicationHash.get(conclusion).add(premise);
		return false;
	}

	private Set<OWLClassExpression> implicationClosure(ArrayList<Implication> imps, Set<OWLClassExpression> s) {
		Set<OWLClassExpression> closure = new HashSet<OWLClassExpression>(s);
		boolean added;

		do {
			added = false;
			for (Implication imp : imps) {
				if (closure.containsAll(imp.getPremise()))
					if (closure.addAll(imp.getConclusion()))
						added = true;
			}
		}
		while (added);
		return(closure);
	}

	private Set<OWLClassExpression> getCounterExample(ArrayList<Implication> imps, int k) {
		int samples = 0;
		for (int i = 0; i < k; ++i) {
			Pair<Set<OWLClassExpression>, OWLClassExpression>  query = this.sampler.sample();

			samples++;
			samplerQueries++;
            Set<OWLClassExpression> premise = query.getKey();
			Set<OWLClassExpression> closure = implicationClosure(imps, premise);
            if (!closure.contains(df.getOWLNothing()) && !closure.contains(query.getValue()) && isImplicationValid(premise, query.getValue())) {
                // logger.info("Samples at this iteration: " + samples);
                return closure;
			}
		}
		// logger.info("Generated " + samples + " samples");
		return null;
	}
	
	/**
	 * Given epsilon, delta, and iteration i returns the number of calls to the sampling oracle.
	 */
	private int callsToSamplingOracle(double epsilon, double delta, int i) {
		return((int) Math.ceil(Math.log(delta/(i*(i + 1))) / Math.log(1 - epsilon)));
	}
	
	/**
	 * Complete a given set of concept expressions 
	 * @param query The set of concept expressions to be completed
	 */
	public Set<OWLClassExpression> complete(Set<OWLClassExpression> query) {

		OWLClassExpression queryConjunction;
		if (query.isEmpty())
			queryConjunction = df.getOWLThing();
		else
			queryConjunction = this.df.getOWLObjectIntersectionOf(query);
		
		if (queryConjunction.isBottomEntity() || isImplicationValid(query, df.getOWLNothing())) {
			Set<OWLClassExpression> s = new HashSet<OWLClassExpression>();
			s.add(df.getOWLNothing());
			return(s);
		}

		Set<OWLClassExpression> completion = new HashSet<OWLClassExpression>(query);
		for (OWLClassExpression c : baseSet) {
			if (!query.contains(c) && isImplicationValid(query, c)) {
				completion.add(c);
			}
		}
		
		return(completion);
	}
	
	/**
	 * Computes an upper approximation of expert's view of the domain.
	 */
	public OWLOntology upperApproximation(double epsilon, double delta, IRI resultOntologyIRI) {
		expertQueries = 0;
		ArrayList<Implication> imps = new ArrayList<Implication>();
		Set<OWLClassExpression> counterExample;

		wrongImplicationHash = new Hashtable<>();
		for (OWLClassExpression c : baseSet) {
			wrongImplicationHash.put(c, new ArrayList<Set<OWLClassExpression>>());
		}
		wrongImplicationHash.put(df.getOWLNothing(), new ArrayList<Set<OWLClassExpression>>());

		int iteration = 1;
		boolean found = false;

		while ((counterExample = getCounterExample(imps, callsToSamplingOracle(epsilon, delta, iteration))) != null) { 
			// logger.info("iteration:" + iteration);
			// logger.info("expert queries:" + this.expertQueries);
			// logger.info("implications:" + imps.size());
			found = false;
			Implication imp = null;
			for (int i = 0; i < imps.size(); i++) {
				imp = imps.get(i);
				if (!counterExample.containsAll(imp.getPremise())) {
					Set<OWLClassExpression> newPremise = new HashSet<OWLClassExpression>(imp.getPremise());
					newPremise.retainAll(counterExample);
				
					Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(newPremise));
					if (!newPremise.equals(newConclusion)) {
						found = true;
						// construct the new implication
						newConclusion.removeAll(newPremise);
						Implication newImp = new Implication(newPremise, newConclusion, df);
						
						// replace imp with the newImp
						imps.set(i, newImp);
						
						if (!counterExample.containsAll(newConclusion)) {
						    break;
						}
					}
				}
			}
			if (!found) {
				Set<OWLClassExpression> newConclusion = new HashSet<OWLClassExpression>(complete(counterExample));
				// construct the new implication
				newConclusion.removeAll(counterExample);
				Implication newImp = new Implication(counterExample, newConclusion, df);
				if (imps.add(newImp)) {
					logger.debug("Added implication: " + newImp);
				} else {
					logger.error("Could not add implication: " + newImp);

				}
			}
			++iteration;
		}

		OWLOntology resultOntology = null;
		try {
			resultOntology = om.createOntology(resultOntologyIRI);
			resultOntology.add(initialOntology.getAxioms());
		} catch (OWLOntologyCreationException e) {
			logger.fatal("Could not create the result ontology");
            System.exit(-1);
        }

		int axiomCount = 0;
        for (int i = 0; i < imps.size(); ++i) {
			OWLSubClassOfAxiom ax = imps.get(i).toGCI();
			if (this.initialReasoner.isEntailed(ax)) {
				logger.debug("Did not add axiom: " + ax);
			} else {
				resultOntology.add(ax);
				++axiomCount;
				logger.debug("Added axiom: " + ax);
			}
		}

	    logger.info("Total iterations: " + (iteration - 1));
		logger.info("Expert queries: " + this.expertQueries);
		logger.info("Sampler queries: " + this.samplerQueries);
		logger.info("Axioms added: " + axiomCount);

		try {
			resultOntology.saveOntology(new OWLXMLDocumentFormat(), resultOntologyIRI);
		} catch (OWLOntologyStorageException e) {
			logger.fatal("Error while saving result ontology");
			e.printStackTrace();
		}
		return(resultOntology);
	}
}