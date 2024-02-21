package ontology.learning.expert;

import ontology.learning.sparql.OWL2SPARQL;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.system.Txn;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TripleStoreExpert implements ExpertOracle {
	protected static final Logger logger = LogManager.getLogger();

	private OWLOntology ontology;

	private RDFConnection conn;

	private PrefixManager pm;

	public TripleStoreExpert(String fileName) {
		pm = new DefaultPrefixManager();
		pm.setPrefix("wdt", "http://www.wikidata.org/entity/");
		pm.setPrefix("owl", "http://www.w3.org/2002/07/owl/");

		Dataset dataset = DatasetFactory.createTxnMem();
		conn = RDFConnection.connect(dataset);

		Txn.executeWrite(conn, () ->{
			conn.load(fileName);
			// conn.load("http://example/g0", "data.ttl");
			// conn.queryResultSet(query, ResultSetFormatter::out);
		});
		// And again - implicit READ transaction.
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

		// OWLClassExpression q5 = df.getOWLClass("http://www.wikidata.org/entity/Q5");
		// OWLClassExpression q6581072 = df.getOWLClass("http://www.wikidata.org/entity/Q6581072");
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass("http://www.wikidata.org/entity/Q5")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass("http://www.wikidata.org/entity/Q84048852")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass("http://www.wikidata.org/entity/Q84048850")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLObjectProperty("http://www.wikidata.org/entity/P31")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLObjectProperty("http://www.wikidata.org/entity/P40")));

		// OWLSubClassOfAxiom ax = om.getOWLDataFactory().getOWLSubClassOfAxiom(q6581072, q5);
		// ontology.addAxiom(ax);
		// ontology.getSignature().add(om.getOWLDataFactory().getOWLObjectProperty("<http://www.wikidata.org/entity/P31>"));

		// OWLReasonerFactory rf = new ReasonerFactory();
		// OWLReasoner reasoner = rf.createReasoner(ontology);
		// reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

    }

	public boolean holds(OWLSubClassOfAxiom ax) {
		String queryStrLhs = OWL2SPARQL.buildQuery(ax.getSubClass());
		String queryStrRhs = OWL2SPARQL.buildQuery(ax.getSuperClass());
		// Query queryLhs = QueryFactory.create(queryStrLhs);
		/*
		Txn.executeWrite(conn, () ->{
			conn.queryResultSet(queryLhs, ResultSetFormatter::out);
		});
		 */
		// conn.queryResultSet(queryLhs, ResultSetFormatter::out);

		// TODO: Check for a more efficient way of doing this.
		// Formulate a single SPARQL query for checking containment of lhs in rhs?
		Set<Node> resultsLhs = new HashSet<>();
		conn.queryResultSet(queryStrLhs, rs->{
			List<QuerySolution> list = Iter.toList(rs);
			list.stream()
					.map(qs->qs.get("1"))
					.filter(Objects::nonNull)
					.map(RDFNode::asNode)
					.forEach(n->resultsLhs.add((Node) n));
		});
		Set<Node> resultsRhs = new HashSet<>();
		conn.queryResultSet(queryStrRhs, rs->{
			List<QuerySolution> list = Iter.toList(rs);
			list.stream()
					.map(qs->qs.get("1"))
					.filter(Objects::nonNull)
					.map(RDFNode::asNode)
					.forEach(n->resultsRhs.add((Node) n));
		});
		return(resultsRhs.containsAll(resultsLhs));
	}

	public OWLOntology getExpertOntology() {
		return(this.ontology);
	}

	public PrefixManager getPrefixManager() {
		return pm;
	}
}
