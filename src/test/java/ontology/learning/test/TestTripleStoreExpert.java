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

		OWLObjectProperty p31 = df.getOWLObjectProperty("wdt:P31", expert.getPrefixManager());
		OWLObjectProperty p40 = df.getOWLObjectProperty("wdt:P40", expert.getPrefixManager());

		// human
		OWLClassExpression q5 = df.getOWLClass("wdt:Q5", expert.getPrefixManager());
		// female
		OWLClassExpression q84048852 = df.getOWLClass("wdt:Q84048852", expert.getPrefixManager());
		// male
		OWLClassExpression qQ84048850 = df.getOWLClass("wdt:Q84048850", expert.getPrefixManager());
		// (child some human)
		OWLClassExpression p40q5 = df.getOWLObjectSomeValuesFrom(p40, q5);
		// (child some female)
		OWLClassExpression p40q84048852  = df.getOWLObjectSomeValuesFrom(p40, q84048852);
		// (child some male)
		OWLClassExpression p40qQ84048850   = df.getOWLObjectSomeValuesFrom(p40, qQ84048850);

		// (child some human) --> (child some male)
		OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40q5, p40qQ84048850);
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(q5, p40q84048852);
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(q5, q84048852);
		expert.holds(ax);
	}
}