package ontology.learning;

import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClassExpression;

public class ImplicationList extends ArrayList<Implication>{

	private static final long serialVersionUID = 1L;
	
	private Set<OWLClassExpression> baseSet;
	
	public ImplicationList(Set<OWLClassExpression> baseSet) {
		super();
		this.baseSet = baseSet;
	}
	
	@Override
	public boolean add(Implication imp) {
		if (!baseSet.containsAll(imp.getPremise()) || !baseSet.containsAll(imp.getConclusion())) 
			return(false);
		// First check if an implication with the same premise and conclusion already exists
		for (Implication x : this) {
			if (x.getPremise().equals(imp.getPremise()) && x.getConclusion().equals(imp.getConclusion()))
				return(false);
		}
		return (super.add(imp));
	}
	
	/**
	 * Computes the closure of a given attribute set under this implication set.
	 * 
	 * @param x
	 *            the attribute set to be closed
	 * @return the closure of <code>x</code> under this implication set
	 */
	public Set<OWLClassExpression> closure(Set<OWLClassExpression> x) {

		Set<OWLClassExpression> closure = new HashSet<OWLClassExpression>(x);
		boolean added;
		
		do {
			added = false;
			for (Implication imp : this) {
				if (closure.containsAll(imp.getPremise()))
					if (closure.addAll(imp.getConclusion()))
						added = true;
			}
		}
		while (added);
		return(closure);
	}
	
	public void replace(Implication oldImp, Implication newImp) throws Exception {
		int index = this.indexOf(oldImp);
		if (index == -1)
			throw new Exception("Implication not found");
		else
			this.set(index, newImp);
	}
	/*
	 * Return the implication with the given premise and conclusion. null if not found.
	 */
	public Implication getImplication(Set<OWLClassExpression> premise, Set<OWLClassExpression> conclusion) {
		for (Implication imp : this)
			if (imp.getPremise().equals(premise) && imp.getConclusion().equals(conclusion))
				return(imp);
		return(null);
	}
	
	/**
	 * Checks if a given attribute set is closed under this set of implications.
	 * 
	 * @param x
	 *            the attribute set to be checked
	 * @return <code>true</code> if <code>x</code> is closed, <code>false</code>
	 *         otherwise
	 */
	public boolean isClosed(Set<OWLClassExpression> x) {
		return x.equals(closure(x));
	}
	
}
