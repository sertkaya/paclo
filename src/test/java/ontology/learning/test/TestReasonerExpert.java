package ontology.learning.test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.IRIComparator;

import ontology.learning.ExpertOracle;
import ontology.learning.Implication;
import ontology.learning.ImplicationList;
import ontology.learning.PACOntologyLearning;
import ontology.learning.RandomSampler;
import ontology.learning.ReasonerExpert;
import ontology.learning.SamplingOracle;

public class TestReasonerExpert {

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	
	public static void main(String[] args) {

		OWLClassExpression clsA = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A");
		OWLClassExpression clsB = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#B");
		OWLClassExpression clsC = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#C");
		OWLClassExpression clsD = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#D");
		OWLObjectProperty propR = df.getOWLObjectProperty("r");
	
		OWLClassExpression existsRA = df.getOWLObjectSomeValuesFrom(propR, clsA);
		OWLClassExpression existsRB = df.getOWLObjectSomeValuesFrom(propR, clsB);
		
		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		baseSet.add(clsA);
		baseSet.add(clsB);
		baseSet.add(clsC);
		// baseSet.add(existsRA);
		// baseSet.add(existsRB);
	
		File expertOntology = new File("/home/bs/research/dev/pacco/src/test/resources/expertOntology.owx");
		IRI expertOntologyIRI = IRI.create(expertOntology);
		ReasonerExpert expert = new ReasonerExpert(expertOntologyIRI);
		
		// OWLClassExpression lhs = df.getOWLObjectIntersectionOf(clsA);
		// OWLClassExpression rhs = df.getOWLObjectIntersectionOf(clsB);
		// OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(lhs, rhs);
		OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(clsA, clsB);
		
		System.out.println(expert.holds(ax));
		
		expert.getReasoner().getSubClasses(clsB, false).forEach(System.out::println);
			
	}
}