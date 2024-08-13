package ontology.learning;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.IRI;

public interface ILearningFrameworkSubsumption {
    public OWLOntology approximation(double epsilon, double delta, IRI resultOntologyIRI);
}
