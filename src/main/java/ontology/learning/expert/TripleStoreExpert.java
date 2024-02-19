package ontology.learning.expert;

import ontology.learning.sparql.OWL2SPARQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.system.Txn;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class TripleStoreExpert implements ExpertOracle {
	protected static final Logger logger = LogManager.getLogger();

	private OWLOntology ontology;

	private RDFConnection conn;

	public TripleStoreExpert(String fileName) {
		Dataset dataset = DatasetFactory.createTxnMem();
		conn = RDFConnection.connect(dataset);

		Txn.executeWrite(conn, () ->{
			System.out.println("Load a file");
			conn.load(fileName);
			// conn.load("http://example/g0", "data.ttl");
			System.out.println("In write transaction");
			// conn.queryResultSet(query, ResultSetFormatter::out);
		});
		// And again - implicit READ transaction.
		System.out.println("After write transaction");
		// conn.queryResultSet(query, ResultSetFormatter::out);

		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = om.getOWLDataFactory();
		try {
			this.ontology = om.createOntology();
		}
		catch (OWLOntologyCreationException e) {
			logger.error("Error creating ontology");
			System.exit(-1);
		}

		OWLClassExpression q5 = df.getOWLClass("http://www.wikidata.org/entity/Q5");
		OWLClassExpression q6581072 = df.getOWLClass("http://www.wikidata.org/entity/Q6581072");
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass("http://www.wikidata.org/entity/Q5")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass("http://www.wikidata.org/entity/Q6581072")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLObjectProperty("http://www.wikidata.org/entity/P31")));

		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(q6581072, q5);
		// ontology.addAxiom(ax);
		// ontology.getSignature().add(om.getOWLDataFactory().getOWLObjectProperty("<http://www.wikidata.org/entity/P31>"));

		// OWLReasonerFactory rf = new ReasonerFactory();
		// OWLReasoner reasoner = rf.createReasoner(ontology);
		// reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

    }

	public boolean holds(OWLSubClassOfAxiom ax) {
		// TODO Auto-generated method stub
		String queryStrLhs = OWL2SPARQL.buildQuery(ax.getSubClass());
		System.out.println("querStrLhs:" + queryStrLhs);
		Query queryLhs = QueryFactory.create(queryStrLhs);
		Txn.executeWrite(conn, () ->{
			conn.queryResultSet(queryLhs, ResultSetFormatter::out);
		});
		// conn.queryResultSet(queryLhs, ResultSetFormatter::out);
		return false;
	}

	public OWLOntology getExpertOntology() {
		return(this.ontology);
	}

}
