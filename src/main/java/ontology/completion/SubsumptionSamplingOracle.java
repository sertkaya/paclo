package ontology.completion;

import java.util.Set;
import javafx.util.Pair;

import org.semanticweb.owlapi.model.OWLClassExpression;

public interface SubsumptionSamplingOracle {
    public Pair<Set<OWLClassExpression>, OWLClassExpression> sample();
}
