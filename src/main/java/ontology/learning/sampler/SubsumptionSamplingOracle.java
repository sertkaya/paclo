package ontology.learning.sampler;

import java.util.Set;
import javafx.util.Pair;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public interface SubsumptionSamplingOracle {
    public Pair<Set<OWLClassExpression>, OWLClassExpression> sample();
    public void update_sampler(OWLReasoner reasoner, boolean updateConclusion);
}
