package ontology.learning;

import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.HermiT.ReasonerFactory;
// import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormatFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import javafx.util.Pair;
import ontology.learning.expert.ExpertOracle;
import ontology.learning.sampler.SubsumptionSamplingOracle;


// with subsumption queries
public class PACOntologyLearningSub {
	
	private OWLOntology ontology;
	private OWLOntologyManager om;
	private OWLDataFactory df;
	private OWLReasonerFactory rf;
	private OWLReasoner reasoner;

	private Set<OWLClassExpression> baseSet;
	private ExpertOracle expert;
	private SubsumptionSamplingOracle sampler;

	private int expertQueries;
	private int samplerQueries;

	private Logger logger = LogManager.getLogger("PACOntologyLearningSub");

	private Hashtable<OWLClassExpression, ArrayList<Set<OWLClassExpression>>> wrongImplicationHash;

	/**
	 * @param baseSet
	 * @param ontology: IRI of the (possibly empty) initial ontology 
	 * @param expert: The domain expert 
	 * @param sampler
	 */
	public PACOntologyLearningSub(IRI ontologyIRI,
                                    Set<OWLClassExpression> baseSet, 
                                    ExpertOracle expert, 
                                    SubsumptionSamplingOracle sampler) {
		om = OWLManager.createOWLOntologyManager();
		df = om.getOWLDataFactory();
		rf = new ReasonerFactory();
		// rf = new ElkReasonerFactory();

		this.baseSet = new HashSet<OWLClassExpression>(baseSet);
		// TODO: read the baseSet! From file? 
		// or let the user select from a list?

		OWLOntology ontology = null;
		try {
			ontology = om.loadOntology(ontologyIRI);
		    logger.debug("Successfully loaded ontology");
		}
		catch (OWLOntologyCreationException e) {
		    logger.fatal("Error loading ontology");
			System.exit(-1);
		}
		this.ontology = ontology;

		this.reasoner = rf.createNonBufferingReasoner(ontology);
		this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

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

		expertQueries++;
		if (this.expert.holds(ax)) {
			return true;
		}

		wrongImplicationHash.get(conclusion).add(premise);
		return false;
	}
	
	public Set<OWLClassExpression> getCounterExample(ImplicationList imps, int k) {
		int samples = 0;
		for (int i = 0; i < k; ++i) {
			Pair<Set<OWLClassExpression>, OWLClassExpression>  query = this.sampler.sample();
			samples++;
			samplerQueries++;
            Set<OWLClassExpression> premise = query.getKey();
			Set<OWLClassExpression> closure = imps.closure(premise);
            if (!closure.contains(df.getOWLNothing()) && !closure.contains(query.getValue()) && isImplicationValid(premise, query.getValue())) {
                logger.info("Samples at this iteration: " + samples);
                return closure;
			}
		}
		logger.info("Generated " + samples + " samples");
		return null;
	}
	
	/**
	 * Given epsilon, delta, and iteration i returns the number of calls to the sampling oracle.
	 */
	public int callsToSamplingOracle(double epsilon, double delta, int i) {
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
		for (OWLClassExpression c : this.baseSet) {
			if (!query.contains(c) && isImplicationValid(query, c)) {
				completion.add(c);
			}
		}
		
		return(completion);
	}
	
	public String prettyPrintAxiom(OWLSubClassOfAxiom ax) {
		// not a neat solution, but works
		String axStr = ax.toString();
		int i = axStr.lastIndexOf("<");
		int j = axStr.lastIndexOf("/") + 1;
		String pattern = axStr.substring(i, j);
		String s = axStr.replaceAll(pattern, "").replaceAll(">", "");
		return(s);
	}
	/**
	 * Computes an upper approximation of expert's view of the domain.
	 * @param ontology the initial ontology
	 */
	public OWLOntology upperApproximation(double epsilon, double delta, IRI resultOntologyIRI) {
		expertQueries = 0;
		ImplicationList imps = new ImplicationList(baseSet);
		Set<OWLClassExpression> counterExample;

		// Hashtable for storing implication -> GCI. 
		// Later used to find out the GCI to remove from the ontology
		// Key: implicaton 
		// Value: corresponding GCI
		// Hashtable<Implication, OWLSubClassOfAxiom> implicationAxiomHash = new Hashtable<>();

		wrongImplicationHash = new Hashtable<>();
		for (OWLClassExpression c : this.baseSet) {
			wrongImplicationHash.put(c, new ArrayList<Set<OWLClassExpression>>());
		}
		wrongImplicationHash.put(df.getOWLNothing(), new ArrayList<Set<OWLClassExpression>>());

		int iteration = 1;
		boolean found = false;

		while ((counterExample = getCounterExample(imps, callsToSamplingOracle(epsilon, delta, iteration))) != null) { 
			logger.info("iteration:" + iteration);
			logger.info("expert queries:" + this.expertQueries);
			logger.info("implications:" + imps.size());
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
			resultOntology.add(ontology.getAxioms());
		} catch (OWLOntologyCreationException e) {
			logger.fatal("Could not create the result ontology");
            System.exit(-1);
        }

		int axiomCount = 0;
        for (int i = 0; i < imps.size(); ++i) {
			OWLSubClassOfAxiom ax = imps.get(i).toGCI();
			if (this.reasoner.isEntailed(ax)) {
				// logger.debug("Did not add axiom: " + prettyPrintAxiom(ax));
				logger.debug("Did not add axiom: " + ax);
			} else {
				resultOntology.add(ax);
				++axiomCount;
				// logger.debug("Added axiom: " + prettyPrintAxiom(ax));
				logger.debug("Added axiom: " + ax);
			}
		}

	    logger.info("Total iterations: " + (iteration - 1));
		logger.info("Expert queries: " + this.expertQueries);
		logger.info("Samples generated: " + this.samplerQueries);
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
