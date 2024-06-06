package ontology.learning.expert;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public interface ExpertOracle {

	/**
	 * Check if an axiom holds in expert's view of the domain.
	 */
	public boolean holds(OWLSubClassOfAxiom ax);

}
