package ontology.learning;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import ontology.learning.sampler.RandomSampler;
import ontology.learning.sampler.SamplingOracle;
import ontology.learning.utils.Utils;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import ontology.learning.expert.ExpertOracle;
import ontology.learning.expert.ReasonerExpert;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class PAClo {
	protected static final Logger logger = LogManager.getLogger("PAClo");
	


	public static void main(String[] args) {

		if (args.length != 6) {
			logger.fatal("Usage: epsilon delta initialOntology expertOntology baseSetFile outputOntology");
			System.exit(-1);
		}
		double epsilon = Double.parseDouble(args[0]);
		double delta = Double.parseDouble(args[1]);
		
		File initialOntologyStr = new File(args[2]);
		File expertOntologyStr = new File(args[3]);
		File baseSetFile = new File(args[4]);
		File resultOntologyStr = new File(args[5]);

		IRI initialOntologyIRI = IRI.create(initialOntologyStr);
		IRI resultOntologyIRI = IRI.create(resultOntologyStr);

		IRI expertOntologyIRI = IRI.create(expertOntologyStr);
		ExpertOracle expert = new ReasonerExpert(expertOntologyIRI);

		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = om.getOWLDataFactory();
		OWLReasonerFactory rf = new ReasonerFactory();

		OWLOntology initialOntology = null;
		try {
			initialOntology = om.loadOntology(initialOntologyIRI);
			logger.debug("Successfully loaded ontology");
		}
		catch (OWLOntologyCreationException e) {
			logger.fatal("Error loading ontology");
			System.exit(-1);
		}

		OWLReasoner reasoner = rf.createReasoner(initialOntology);
		reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

		Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, initialOntology);

		SamplingOracle sampler = new RandomSampler(baseSet);

		Instant start = Instant.now();
		PACOntologyLearning pacCompletion = new PACOntologyLearning(initialOntology, baseSet, expert, sampler, om, reasoner);
		pacCompletion.upperApproximation(epsilon, delta, resultOntologyIRI);
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		logger.info("Execution time: " + timeElapsed / 1000);
	}
}
