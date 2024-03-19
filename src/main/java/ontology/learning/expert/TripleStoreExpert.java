package ontology.learning.expert;

import ontology.learning.sparql.OWL2SPARQL;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.*;

public class TripleStoreExpert implements ExpertOracle {
	protected static final Logger logger = LogManager.getLogger();

	private OWLOntology ontology;

	private RDFConnection conn;

	private PrefixManager pm;
	private OWLReasoner reasoner;

	private Dataset dataset;

	public TripleStoreExpert(String KGfileName, IRI initialOntology) {
		pm = new DefaultPrefixManager();
		pm.setPrefix("wd", "http://www.wikidata.org/entity/");
		pm.setPrefix("wdt", "http://www.wikidata.org/prop/direct/");
		pm.setPrefix("owl", "http://www.w3.org/2002/07/owl/");

		this.dataset = DatasetFactory.createTxnMem();
		conn = RDFConnection.connect(this.dataset);

		Txn.executeWrite(conn, () ->{
			conn.load(KGfileName);
		});

		// Materialize the subclass relation
		// First get the subclass relations
		String prefix = "PREFIX owl: <http://www.w3.org/2002/07/owl/>\nPREFIX wd: <http://www.wikidata.org/entity/>\n" + "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n\n";
		String q = prefix + "SELECT DISTINCT ?s ?o WHERE {?s wdt:P279 ?o}";
		HashMap<Resource, Set<Resource>> superclasses = new HashMap<>();

		QueryExecution qExec = conn.query(q);
		ResultSet rs = qExec.execSelect() ;
		while(rs.hasNext()) {
			QuerySolution qs = rs.next() ;
			Resource subclass = qs.getResource("s");
			Resource superclass = qs.getResource("o");
			if (!superclasses.containsKey(subclass)) {
				superclasses.put(subclass, new HashSet<>());
			}
			superclasses.get(subclass).add(superclass);
		}
		qExec.close() ;

		for (Map.Entry<Resource, Set<Resource>> entry : superclasses.entrySet()) {
			// System.out.println(entry.getKey());
			// System.out.println(entry.getValue());
			Resource key = entry.getKey();
			Set<Resource> value = entry.getValue();
			System.out.println("Key=" + key + ", Value=" + value);
		}
		// Now add the materialized relations
		dataset.begin(TxnType.WRITE) ;
		q = prefix + "SELECT DISTINCT ?s ?o WHERE {?s wdt:P31 ?o}";
		qExec = conn.query(q);
		rs = qExec.execSelect() ;

		Resource lastEntity = null;
		while(rs.hasNext()) {
			QuerySolution qs = rs.next() ;
			Resource entity = qs.getResource("s");
			lastEntity = entity;
			Resource type = qs.getResource("o");
			if (superclasses.containsKey(type)) {
				for (Resource supertype : superclasses.get(type)) {
					System.out.println("ent:" + entity);
					System.out.println("st:" + supertype);
					String insertQuery = prefix + "INSERT DATA {<" + entity + "> wdt:P31 <" + supertype + ">}\n";
					UpdateRequest request = UpdateFactory.create(insertQuery) ;
					UpdateExecution.dataset(dataset).update(request).execute();
					System.out.println(insertQuery);
				}
			}
		}
		qExec.close() ;
		dataset.commit() ;

		// String qTmp = prefix + "SELECT DISTINCT ?o WHERE {<" + lastEntity + "> wdt:P31 ?o}\n";
		// qExec = conn.query(q);
		// rs = qExec.execSelect() ;
		// while(rs.hasNext()) {
		// 	QuerySolution qs = rs.next() ;
		// 	System.out.println("XXX:" + lastEntity + ":" + qs.getResource("o"));
		// }



		// Model m = dataset.getDefaultModel();
        // try {
        //     m.write(new FileWriter("xxx"));
        // } catch (IOException e) {
        //     throw new RuntimeException(e);
        // }


        OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = om.getOWLDataFactory();
		try {
			// this.ontology = om.createOntology();
			this.ontology = om.loadOntology(initialOntology);
		}
		catch (OWLOntologyCreationException e) {
			logger.fatal("Error loading ontology");
			e.printStackTrace();
			System.exit(-1);
		}

		OWLReasonerFactory rf = new ReasonerFactory();
		this.reasoner = rf.createReasoner(ontology);
		this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

    }

	public boolean holds(OWLSubClassOfAxiom ax) {
		String queryStrLhs = OWL2SPARQL.buildQuery(ax.getSubClass());
		String queryStrRhs = OWL2SPARQL.buildQuery(ax.getSuperClass());

		logger.debug("query starting");
		// logger.debug("ax: " + ax);
		// logger.debug("queryStrLhs:" + queryStrLhs);
		// logger.debug("queryStrRhs:" + queryStrRhs);
		// TODO: Check for a more efficient way of doing this.
		// Formulate a single SPARQL query for checking containment of lhs in rhs?

		dataset.begin(ReadWrite.READ) ;
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
		if (!resultsLhs.isEmpty()) {
			conn.queryResultSet(queryStrRhs, rs -> {
				List<QuerySolution> list = Iter.toList(rs);
				list.stream()
						.map(qs -> qs.get("1"))
						.filter(Objects::nonNull)
						.map(RDFNode::asNode)
						.forEach(n -> resultsRhs.add((Node) n));
			});
		}
		dataset.end() ;

		/*
		QueryExecution qExecLhs = conn.query(queryStrLhs);
		ResultSet rsLhs = qExecLhs.execSelect() ;

		QueryExecution qExecRhs = conn.query(queryStrRhs);
		ResultSet rsRhs = qExecRhs.execSelect() ;

		Set<Resource> resultsLhs = new HashSet<>();
		while(rsLhs.hasNext()) {
			QuerySolution qs = rsLhs.next() ;
			resultsLhs.add(qs.getResource("1"));
		}
		qExecLhs.close() ;

		Set<Resource> resultsRhs = new HashSet<>();
		while(rsRhs.hasNext()) {
			QuerySolution qs = rsRhs.next() ;
			resultsRhs.add(qs.getResource("1"));
		}
		qExecRhs.close() ;
		 */

		logger.debug("query executed");
		// logger.debug("resultsLhs: " + resultsLhs);
		// logger.debug("resultsRhs: " + resultsRhs);
		return(resultsRhs.containsAll(resultsLhs));
	}

	public OWLOntology getExpertOntology() {
		return(this.ontology);
	}

	public PrefixManager getPrefixManager() {
		return pm;
	}
}
