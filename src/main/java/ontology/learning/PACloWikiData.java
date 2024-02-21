package ontology.learning;

import ontology.learning.expert.ExpertOracle;
import ontology.learning.expert.TripleStoreExpert;
import ontology.learning.sampler.RandomSubsumptionSampler;
import ontology.learning.sampler.SubsumptionSamplingOracle;
import ontology.learning.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.io.File;
import java.util.Set;

public class PACloWikiData {
	protected static final Logger logger = LogManager.getLogger();
	


	public static void main(String[] args) {

		if (args.length != 6) {
			logger.fatal("Usage: epsilon delta initialOntology knowledgeGraph baseSetFile outputOntology");
			System.exit(-1);
		}
		double epsilon = Double.parseDouble(args[0]);
		double delta = Double.parseDouble(args[1]);
		
		File initialOntology = new File(args[2]);
		String knowledgeGraph = args[3];
		File baseSetFile = new File(args[4]);
		File resultOntology = new File(args[5]);
		
		ExpertOracle expert = new TripleStoreExpert(knowledgeGraph);
		
		Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, expert.getExpertOntology());
		
		// SamplingOracle sampler = new RandomSampler(baseSet);
		SubsumptionSamplingOracle sampler = new RandomSubsumptionSampler(baseSet);

		IRI initialOntologyIRI = IRI.create(initialOntology);
		IRI resultOntologyIRI = IRI.create(resultOntology);

		// PACOntologyCompletion pacCompletion = new PACOntologyCompletion(initialOntologyIRI, baseSet, expert, sampler);
		PACOntologyLearningSub pacCompletion = new PACOntologyLearningSub(initialOntologyIRI, baseSet, expert, sampler);
		pacCompletion.upperApproximation(epsilon, delta, resultOntologyIRI);

	}

}
