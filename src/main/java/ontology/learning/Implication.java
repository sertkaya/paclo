package ontology.learning;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class Implication {

	private Set<OWLClassExpression> premise, conclusion;
	private OWLDataFactory df;
	
	public Implication(Set<OWLClassExpression> lhs, Set<OWLClassExpression> rhs, OWLDataFactory df) {
		this.premise = lhs;
		this.conclusion = rhs;
		this.df = df;
	}

	public OWLSubClassOfAxiom toGCI() {
		OWLClassExpression premiseExpr;
		// TODO: Check this!
		// is the conjunction owl:Thing if premise is empty?
		if (premise.isEmpty())
			premiseExpr = df.getOWLThing();
		else
			premiseExpr = df.getOWLObjectIntersectionOf(premise);
		OWLClassExpression conclusionExpr = conclusion.size() > 1 ? df.getOWLObjectIntersectionOf(conclusion) : conclusion.iterator().next();
		OWLSubClassOfAxiom gci = df.getOWLSubClassOfAxiom(premiseExpr, conclusionExpr);
		return(gci);
	}

	public Set<OWLClassExpression> getPremise() {
		return(this.premise);
	}

	public Set<OWLClassExpression> getConclusion() {
		return(this.conclusion);
	}
	
	public String toString() {
		String s = "{";
		for (OWLClassExpression clsExp : this.getPremise())
			s += clsExp.toString() + " ";
		s += "} => {";
		for (OWLClassExpression clsExp : this.getConclusion())
			s += clsExp.toString() + " ";
		s += "}";
		
		return(s);
	}
}
