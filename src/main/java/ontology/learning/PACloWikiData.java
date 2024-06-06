package ontology.learning;

import ontology.learning.expert.ExpertOracle;
import ontology.learning.expert.TripleStoreExpert;
import ontology.learning.sampler.RandomSubsumptionSampler;
import ontology.learning.sampler.SubsumptionSamplingOracle;
import ontology.learning.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.InferenceType;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class PACloWikiData {
	private static final Logger logger = LogManager.getLogger(PACloWikiData.class);

	public static void main(String[] args) {

		if (args.length != 6) {
			logger.fatal("Usage: epsilon delta initialOntology knowledgeGraph baseSetFile outputOntology");
			System.exit(-1);
		}
		double epsilon = Double.parseDouble(args[0]);
		double delta = Double.parseDouble(args[1]);
		
		File initialOntologyStr = new File(args[2]);
		String knowledgeGraph = args[3];
		File baseSetFile = new File(args[4]);
		File resultOntologyStr = new File(args[5]);

		IRI initialOntologyIRI = IRI.create(initialOntologyStr);
		IRI resultOntologyIRI = IRI.create(resultOntologyStr);

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

		// Measure time
		Instant start = Instant.now();

		ExpertOracle expert = new TripleStoreExpert(knowledgeGraph);

		Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, initialOntology);

		SubsumptionSamplingOracle sampler = new RandomSubsumptionSampler(baseSet);

		PACOntologyLearningSub pacCompletion = new PACOntologyLearningSub(initialOntology, baseSet, expert, sampler, om, reasoner);
		pacCompletion.upperApproximation(epsilon, delta, resultOntologyIRI);

		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		logger.info("Execution time: " + timeElapsed / 1000);
	}
}