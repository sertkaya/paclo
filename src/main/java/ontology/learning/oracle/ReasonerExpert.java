package ontology.learning.oracle;

import ontology.learning.ExpertOracle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import ontology.learning.Implication;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.time.Duration;
import java.time.Instant;

/**
 * An expert implementation that answers questions w.r.t. an expert ontology.
 */

public class ReasonerExpert implements ExpertOracle {
	private Logger logger = LogManager.getLogger("ExpertOracle");

	private OWLOntology ontology;
	private OWLReasoner reasoner;
	
	public ReasonerExpert(IRI iri) {

		Instant start = Instant.now();
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		try {
			this.ontology = om.loadOntology(iri);
		}
		catch (OWLOntologyCreationException e) {
			logger.fatal("Error loading ontology");
			System.exit(-1);
		}
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		logger.info("Loaded ontology: " + timeElapsed + " ms");

		OWLReasonerFactory rf = new ReasonerFactory();
		this.reasoner = rf.createReasoner(ontology);
		start = Instant.now();
		this.reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
		this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		// this.reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
		// this.reasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
		finish = Instant.now();
		timeElapsed = Duration.between(start, finish).toMillis();
		logger.info("Classified ontology: " + timeElapsed + " ms");
	}

	public boolean holds(Implication imp) {
		OWLSubClassOfAxiom ax = imp.toGCI();
		return(this.reasoner.isEntailed(ax));
	}

	public boolean holds(OWLSubClassOfAxiom ax) {
		return(this.reasoner.isEntailed(ax));
	}

	public OWLOntology getExpertOntology() {
		return(this.ontology);
	}
	
	public OWLReasoner getReasoner() {
		return(this.reasoner);
	}
}
