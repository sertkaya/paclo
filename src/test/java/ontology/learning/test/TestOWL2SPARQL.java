package ontology.learning.test;

import java.util.HashSet;
import java.util.Set;

import ontology.learning.sparql.OWL2SPARQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import ontology.learning.expert.TripleStoreExpert;

public class TestOWL2SPARQL {
	protected static final Logger logger = LogManager.getLogger();

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	
	public static void main(String[] args) {

		OWLClassExpression clsA = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A");
		OWLClassExpression clsB = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#B");
		OWLClassExpression clsC = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#C");
		OWLClassExpression clsD = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#D");
		OWLClassExpression thing = df.getOWLThing();
		OWLObjectProperty propR = df.getOWLObjectProperty("r");


		// depth 1
		// (some r A)
		OWLClassExpression existsRA = df.getOWLObjectSomeValuesFrom(propR, clsA);
		// (some r B)
		OWLClassExpression existsRB = df.getOWLObjectSomeValuesFrom(propR, clsB);
		// (some r B)
		OWLClassExpression existsRC = df.getOWLObjectSomeValuesFrom(propR, clsC);

		Set<OWLClassExpression> tmp = new HashSet<>();
		tmp.add(clsA);
		tmp.add(clsB);
		// (A and B)
		OWLObjectIntersectionOf AB = df.getOWLObjectIntersectionOf(tmp);

		// conjunction in depth 1
		// (some r (A and B))
		OWLClassExpression existsRAB = df.getOWLObjectSomeValuesFrom(propR, AB);

		// depth 2
		// (some r (some r A))
		OWLClassExpression existsRRA = df.getOWLObjectSomeValuesFrom(propR, existsRA);

		// conjunction in depth 2
		// (some r (some r (A and B)))
		OWLClassExpression existsRRAB = df.getOWLObjectSomeValuesFrom(propR, existsRAB);

		// (some r (A and (some r B)))
		tmp = new HashSet<>();
		tmp.add(clsA);
		tmp.add(existsRB);
		OWLClassExpression existsRA_existsRB = df.getOWLObjectSomeValuesFrom(propR, df.getOWLObjectIntersectionOf(tmp));

		tmp = new HashSet<>();
		// (some r (A and (some r (A and B))))
		tmp.add(clsA);
		tmp.add(existsRAB);
		OWLObjectIntersectionOf tmp_conjunction = df.getOWLObjectIntersectionOf(tmp);
		OWLClassExpression existsRA_existsRAB = df.getOWLObjectSomeValuesFrom(propR, tmp_conjunction);

		// (some r (A and ((some r A) and (some r B) and (some r C))))
		tmp = new HashSet<>();
		// inner conjunction
		tmp.add(clsA);
		tmp.add(existsRA);
		tmp.add(existsRB);
		tmp.add(existsRC);
		tmp_conjunction = df.getOWLObjectIntersectionOf(tmp);
		OWLClassExpression existsRA_existsRAexistsRABC = df.getOWLObjectSomeValuesFrom(propR, tmp_conjunction);

		// (some r (A and ((some r A) and (some r B) and (some r C) and (some r (A and (some r B))))))
		tmp.add(existsRA_existsRB);
		tmp_conjunction = df.getOWLObjectIntersectionOf(tmp);
		OWLClassExpression existsRA_existsRAexistsRABCexistsRA_existsRAB = df.getOWLObjectSomeValuesFrom(propR, tmp_conjunction);

		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		// depth 0
		baseSet.add(clsA);
		baseSet.add(clsB);
		// baseSet.add(clsC);

		// depth 1
		baseSet.add(existsRA);
		baseSet.add(existsRB);
		baseSet.add(existsRAB);

		// depth 2
		// baseSet.add(existsRRA);
		// baseSet.add(existsRRAB);

		// depth 3
		// baseSet.add(existsRA_existsRAexistsRABCexistsRA_existsRAB);

		String kg = "/home/bs/research/dev/pacco/src/test/resources/eu-g7-members.ttl";
		String ontology = "/home/bs/research/dev/pacco/src/test/resources/eu-members-declarations.owx";
		TripleStoreExpert expert = new TripleStoreExpert(kg, IRI.create(ontology));
		System.out.println(OWL2SPARQL.buildQuery(df.getOWLObjectIntersectionOf(baseSet)));

	}
}