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

		String kg = "/home/bs/research/dev/paclo/src/test/resources/family-example/family-rdfs.ttl";
		String schema = "/home/bs/research/dev/paclo/src/test/resources/family-example/family-rdfs-schema.ttl";
		TripleStoreExpert expert = new TripleStoreExpert(kg, schema);

		OWLObjectProperty p31 = df.getOWLObjectProperty("wdt:P31", expert.getPrefixManager());
		OWLObjectProperty p40 = df.getOWLObjectProperty("wdt:P40", expert.getPrefixManager());

		// human
		OWLClassExpression thing = df.getOWLThing();

		// human
		OWLClassExpression q5 = df.getOWLClass("wd:Q5", expert.getPrefixManager());
		// female
		OWLClassExpression q84048852 = df.getOWLClass("wd:Q84048852", expert.getPrefixManager());
		// male
		OWLClassExpression qQ84048850 = df.getOWLClass("wd:Q84048850", expert.getPrefixManager());
		// (child some human)
		OWLClassExpression p40q5 = df.getOWLObjectSomeValuesFrom(p40, q5);
		// (child some female)
		OWLClassExpression p40q84048852  = df.getOWLObjectSomeValuesFrom(p40, q84048852);
		// (child some male)
		OWLClassExpression p40qQ84048850   = df.getOWLObjectSomeValuesFrom(p40, qQ84048850);

		// male --> human
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(qQ84048850, q5);

		// (child some human) --> (child some male)
		OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40q5, p40qQ84048850);

		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(q5, p40q84048852);
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(q5, q84048852);

		//  (child some male) --> (child some human)
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40qQ84048850, p40q5);

		//  (child some female) --> (child some human)
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40q84048852, p40q5);

		//  (child some female) --> (child some male)
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40q84048852, p40qQ84048850);

		//  (child some male) --> (child some female)
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40qQ84048850, p40q84048852);

		//  (child some male) --> owl:Thing
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40q84048852, thing);

		//  (child some owl:Thing)
		// OWLClassExpression p40Thing = om.getOWLDataFactory().getOWLObjectSomeValuesFrom(p40, thing);


		//  (child some male) --> (child some owl:Thing)
		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(p40q84048852, p40Thing);


		logger.info("Axiom:" + ax);
		if (expert.holds(ax))
			logger.info("axiom holds");
		else
			logger.info("axiom does not hold");
	}
}