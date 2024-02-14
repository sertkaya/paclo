package ontology.learning.test;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import ontology.learning.Implication;
import ontology.learning.ImplicationList;
import ontology.learning.RandomSampler;
import ontology.learning.SamplingOracle;

public class TestRandomSampler {

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	
	public static void main(String[] args) {

		OWLClassExpression clsA = df.getOWLClass("A");
		OWLClassExpression clsB = df.getOWLClass("B");
		OWLClassExpression clsC = df.getOWLClass("C");
		OWLObjectProperty propR = df.getOWLObjectProperty("r");
	
		OWLClassExpression existsRA = df.getOWLObjectSomeValuesFrom(propR, clsA);
		OWLClassExpression existsRB = df.getOWLObjectSomeValuesFrom(propR, clsB);
		
		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		baseSet.add(clsA);
		baseSet.add(clsB);
		baseSet.add(clsC);
		baseSet.add(existsRA);
		baseSet.add(existsRB);
		
		SamplingOracle sampler = new RandomSampler(baseSet);
		Set<OWLClassExpression> sample = sampler.sample();
		
		System.out.println(sample);

	}

}
