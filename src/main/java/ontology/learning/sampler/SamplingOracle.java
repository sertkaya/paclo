package ontology.learning.sampler;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;

public interface SamplingOracle {

	public Set<OWLClassExpression> sample();
}
