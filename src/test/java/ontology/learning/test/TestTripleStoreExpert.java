package ontology.learning.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import ontology.learning.expert.TripleStoreExpert;

public class TestTripleStoreExpert {
	protected static final Logger logger = LogManager.getLogger();

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	
	public static void main(String[] args) {

		OWLClassExpression clsA = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A");
		OWLClassExpression clsB = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#B");
		OWLClassExpression clsC = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#C");
		OWLClassExpression clsD = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#D");
		OWLClassExpression thing = df.getOWLThing();
		OWLObjectProperty propR = df.getOWLObjectProperty("r");
	
		OWLClassExpression existsRA = df.getOWLObjectSomeValuesFrom(propR, clsA);
		OWLClassExpression existsRB = df.getOWLObjectSomeValuesFrom(propR, clsB);
		
		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		baseSet.add(clsA);
		baseSet.add(clsB);
		baseSet.add(clsC);
		// baseSet.add(existsRA);
		// baseSet.add(existsRB);
	
		/*
		URL kgURL = null;
		try {
			kgURL = new URL("file:///home/bs/research/dev/pacco/src/test/resources/eu-g7-members.ttl");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			logger.error("URL not correct.");
			e.printStackTrace();
		}
		*/
		String kg = "/home/bs/research/dev/pacco/src/test/resources/eu-g7-members.ttl";
		TripleStoreExpert expert = new TripleStoreExpert(kg);
		System.out.println("query:" + expert.buildWhereClause(0, clsA));
		
		// OWLClassExpression lhs = df.getOWLObjectIntersectionOf(clsA);
		// OWLClassExpression rhs = df.getOWLObjectIntersectionOf(clsB);
		// OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(lhs, rhs);
		// OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(clsA, clsB);
		// System.out.println(expert.holds(ax));
		
			
	}
}