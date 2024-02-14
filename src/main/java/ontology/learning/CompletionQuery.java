package ontology.learning;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

public class CompletionQuery {
	private Set<OWLClassExpression> query;
	private Set<OWLClassExpression> baseSet;
	private OWLDataFactory dataFactory;
	
	public CompletionQuery(Set<OWLClassExpression> baseSet, Set<OWLClassExpression> query, OWLDataFactory dataFactory) {
		this.baseSet = baseSet;
		this.dataFactory = dataFactory;
		this.query = query;
	}
	
	public Set<OWLClassExpression> getQuery() {
		return(this.query);
	}
	
}
