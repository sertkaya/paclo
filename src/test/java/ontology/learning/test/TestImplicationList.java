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

public class TestImplicationList {

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
		
		Set<OWLClassExpression> premise1 = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> conclusion1 = new HashSet<OWLClassExpression>();
		
		premise1.add(clsA);
		premise1.add(clsB);
		conclusion1.add(existsRA);
		Implication imp1 = new Implication(premise1, conclusion1, df);
		
		Set<OWLClassExpression> premise2 = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> conclusion2 = new HashSet<OWLClassExpression>();
		premise2.clear();
		conclusion2.clear();
		premise2.add(clsA);
		premise2.add(clsB);
		premise2.add(clsC);
		conclusion2.add(existsRB);
		Implication imp2 = new Implication(premise2, conclusion2, df);
		
		Set<OWLClassExpression> premise3 = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> conclusion3 = new HashSet<OWLClassExpression>();
		premise3.clear();
		conclusion3.clear();
		premise3.add(clsA);
		conclusion3.add(clsB);
		Implication imp3 = new Implication(premise3, conclusion3, df);

		Set<OWLClassExpression> premise4 = new HashSet<OWLClassExpression>();
		Set<OWLClassExpression> conclusion4 = new HashSet<OWLClassExpression>();
		premise4.clear();
		conclusion4.clear();
		conclusion4.add(clsA);
		Implication imp4 = new Implication(premise4, conclusion4, df);

		ImplicationList imps = new ImplicationList(baseSet);
		imps.add(imp1);
		imps.add(imp2);
		imps.add(imp3);
		imps.add(imp4);
		
		for (Implication imp: imps)
			System.out.println(imp);
		
		Set<OWLClassExpression> x = new HashSet<OWLClassExpression>();
		// x.add(clsA);
		
		Set<OWLClassExpression> closure = imps.closure(x);
		
		System.out.println(closure);
	}
}