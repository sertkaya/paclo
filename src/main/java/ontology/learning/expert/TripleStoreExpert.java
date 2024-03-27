package ontology.learning.expert;

import ontology.learning.sparql.OWL2SPARQL;
/*
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateExecution;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.system.Txn;
*/
import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.inferencer.fc.DedupingInferencer;
import org.eclipse.rdf4j.sail.inferencer.fc.DirectTypeHierarchyInferencer;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.*;
import java.util.*;

public class TripleStoreExpert implements ExpertOracle {
	protected static final Logger logger = LogManager.getLogger();

	private OWLOntology ontology;

	/*
	private RDFConnection conn;
	private Dataset dataset;
	*/

	RepositoryConnection con;

	private PrefixManager pm;
	private OWLReasoner reasoner;

	public TripleStoreExpert(String KGfileName, IRI initialOntology) {
		pm = new DefaultPrefixManager();
		pm.setPrefix("wd", "http://www.wikidata.org/entity/");
		pm.setPrefix("wdt", "http://www.wikidata.org/prop/direct/");
		pm.setPrefix("owl", "http://www.w3.org/2002/07/owl/");

		/*
		// Jena
		this.dataset = DatasetFactory.createTxnMem();
		conn = RDFConnection.connect(this.dataset);

		Txn.executeWrite(conn, () ->{
			conn.load(KGfileName);
		});
		 */
		// RDF4J
		Repository repo = new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));
		// Repository repo = new SailRepository(new MemoryStore());
		// Repository repo = new SailRepository(new DedupingInferencer(new MemoryStore()));
		File file = new File(KGfileName);
		try {
			con = repo.getConnection();
			// try {
				// con.add(file, baseURI, RDFFormat.TURTLE);
			// }
			// TODO: Keep the connection open?
			// finally {
			// 	con.close();
			// }
		} catch (RepositoryException e) {
			logger.fatal("Could not connect to the repository");
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			// con.add(file, baseURI, RDFFormat.RDFXML);
			con.add(file, RDFFormat.TURTLE);
		}
		catch (java.io.IOException e) {
			logger.fatal("Could not open the repository file");
			e.printStackTrace();
		}
		catch (RDFParseException e) {
			logger.fatal("Error parsing the repository file");
			e.printStackTrace();
			System.exit(-1);
		}

        // Materialize the subclass relation
		// First get the subclass relations
		String prefix = "PREFIX owl: <http://www.w3.org/2002/07/owl/>\nPREFIX wd: <http://www.wikidata.org/entity/>\n" + "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n\n";
		String q = prefix + "SELECT DISTINCT ?s ?o WHERE {?s wdt:P279 ?o}";
		/*
		// Jena
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
			Resource key = entry.getKey();
			Set<Resource> value = entry.getValue();
			logger.trace("Key=" + key + ", Value=" + value);
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
					logger.trace("ent:" + entity);
					logger.trace("st:" + supertype);
					String insertQuery = prefix + "INSERT DATA {<" + entity + "> wdt:P31 <" + supertype + ">}\n";
					UpdateRequest request = UpdateFactory.create(insertQuery) ;
					UpdateExecution.dataset(dataset).update(request).execute();
					logger.trace(insertQuery);
				}
			}
		}
		qExec.close() ;
		dataset.commit() ;
		 */

		// For debugging
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

		/*
		try {
			// con.add(file, baseURI, RDFFormat.RDFXML);
			con.add(file, RDFFormat.TURTLE);
		}
		catch (java.io.IOException e) {
			logger.fatal("Could not open the repository file");
			e.printStackTrace();
		}
		catch (RDFParseException e) {
			logger.fatal("Error parsing the repository file");
			e.printStackTrace();
			System.exit(-1);
		}
		 */

		String queryStrLhs = OWL2SPARQL.buildQuery(ax.getSubClass());
		String queryStrRhs = OWL2SPARQL.buildQuery(ax.getSuperClass());

		logger.debug("query starting");
		logger.trace("ax: " + ax);
		logger.trace("queryStrLhs:\n" + queryStrLhs);
		logger.trace("queryStrRhs:\n" + queryStrRhs);
		logger.trace("ASK Query:\n" + OWL2SPARQL.buildQuery(ax));
		// TODO: Check for a more efficient way of doing this.
		// Formulate a single SPARQL query for checking containment of lhs in rhs?

		/*
		// Jena
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
		 */

		/*
		// For debugging Jena
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

		/*
		// Jena
		logger.trace("resultsLhs: " + resultsLhs);
		logger.trace("resultsRhs: " + resultsRhs);
		return(resultsRhs.containsAll(resultsLhs));
		 */

		// TODO: Use instead an ASK query to make it more efficient!
		Set<Value> resultsLhs = new HashSet<>();
		TupleQuery queryLhs = con.prepareTupleQuery(queryStrLhs);
		try {
			TupleQueryResult result = queryLhs.evaluate();
			for (BindingSet bindingSet: result) {
				Value v = bindingSet.getValue("1");
				resultsLhs.add(v);
			}
		} catch (QueryEvaluationException e) {
			logger.fatal("Error executing the query:" + queryStrLhs);
            e.printStackTrace();
			System.exit(-1);
        }

        Set<Value> resultsRhs = new HashSet<>();
		TupleQuery queryRhs = con.prepareTupleQuery(queryStrRhs);
		try {
			TupleQueryResult result = queryRhs.evaluate();
			for (BindingSet bindingSet: result) {
				Value v = bindingSet.getValue("1");
				resultsRhs.add(v);
			}
		} catch (QueryEvaluationException e) {
			logger.fatal("Error executing the query:" + queryStrRhs);
			e.printStackTrace();
			System.exit(-1);
        }

		logger.debug("query executed");
		logger.debug("resultsLhs:" + resultsLhs);
		logger.debug("resultsRhs:" + resultsRhs);

        return(resultsRhs.containsAll(resultsLhs));
	}

	public OWLOntology getExpertOntology() {
		return(this.ontology);
	}

	public PrefixManager getPrefixManager() {
		return pm;
	}
}
