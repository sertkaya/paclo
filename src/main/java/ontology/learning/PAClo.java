package ontology.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ontology.learning.sampler.RandomSampler;
import ontology.learning.sampler.SamplingOracle;
import ontology.learning.utils.Utils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

import ontology.learning.expert.ExpertOracle;
import ontology.learning.expert.ReasonerExpert;
import ontology.learning.sampler.RandomSubsumptionSampler;
import ontology.learning.sampler.SubsumptionSamplingOracle;

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
		
		File initialOntology = new File(args[2]);
		File expertOntology = new File(args[3]);
		File baseSetFile = new File(args[4]);
		File resultOntology = new File(args[5]);
		
		IRI expertOntologyIRI = IRI.create(expertOntology);
		ExpertOracle expert = new ReasonerExpert(expertOntologyIRI);
		
		Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, expert.getExpertOntology());
		
		SamplingOracle sampler = new RandomSampler(baseSet);
		// SubsumptionSamplingOracle sampler = new RandomSubsumptionSampler(baseSet);

		IRI initialOntologyIRI = IRI.create(initialOntology);
		IRI resultOntologyIRI = IRI.create(resultOntology);

		Instant start = Instant.now();
		PACOntologyLearning pacCompletion = new PACOntologyLearning(initialOntologyIRI, baseSet, expert, sampler);
		// PACOntologyLearningSub pacCompletion = new PACOntologyLearningSub(initialOntologyIRI, baseSet, expert, sampler);
		pacCompletion.upperApproximation(epsilon, delta, resultOntologyIRI);
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		logger.info("Execution time: " + timeElapsed / 1000);
	}

}
