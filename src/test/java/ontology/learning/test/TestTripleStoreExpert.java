package ontology.learning.test;

import ontology.learning.expert.TripleStoreExpert;
import ontology.learning.sparql.OWL2SPARQL;
import ontology.learning.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class TestTripleStoreExpert {
	protected static final Logger logger = LogManager.getLogger();

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	
	public static void main(String[] args) {

		String kg = "/home/bs/research/dev/paclo/src/test/resources/example-1.ttl";
		String baseSetFileName = "/home/bs/research/dev/paclo/src/test/resources/baseSet-example-1";
		TripleStoreExpert expert = new TripleStoreExpert(kg);
		File baseSetFile = new File(baseSetFileName);
		Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, expert.getExpertOntology());
		for (OWLClassExpression c : baseSet) {
			System.out.println("query:" + OWL2SPARQL.buildQuery(c));
		}

	}
}