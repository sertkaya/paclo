package ontology.learning.expert;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.util.Set;

public interface ExpertOracle {

	/**
	 * Check if an axiom holds in expert's view of the domain.
	 */
	public boolean holds(OWLSubClassOfAxiom ax);
	// public boolean holds(Set<OWLClassExpression> premise, Set<OWLClassExpression> conclusion);

}
