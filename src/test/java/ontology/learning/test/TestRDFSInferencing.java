package ontology.learning.test;

import ontology.learning.graph.TripleStoreExpert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class TestRDFSInferencing {
	protected static final Logger logger = LogManager.getLogger();

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	
	public static void main(String[] args) {

		String kg = "/home/bs/research/dev/paclo/src/test/resources/example-rdfs.ttl";
		TripleStoreExpert expert = new TripleStoreExpert(kg);

		// top
		OWLClassExpression thing = df.getOWLThing();

		// ex:A
		OWLClassExpression exA = df.getOWLClass("http://example.org/A");
		// ex:B
		OWLClassExpression exB = df.getOWLClass("http://example.org/B");
		// ex:C
		OWLClassExpression exC = df.getOWLClass("http://example.org/C");
		// ex:D
		OWLClassExpression exD = df.getOWLClass("http://example.org/D");
		// ex:E
		OWLClassExpression exE = df.getOWLClass("http://example.org/E");


		// (child some human) --> (child some male)
		OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(exB, exE);

		logger.info("Axiom:" + ax);
		if (expert.holds(ax))
			logger.info("axiom holds");
		else
			logger.info("axiom does not hold");
	}
}