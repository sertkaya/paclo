package ontology.learning.expert;

import ontology.learning.sparql.OWL2SPARQL;

import org.apache.jena.graph.*;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.apache.jena.vocabulary.SchemaDO.model;

public class TripleStoreExpert implements ExpertOracle {
	protected static final Logger logger = LogManager.getLogger();

	// RepositoryConnection con;

	private PrefixManager pm;

	private Reasoner rdfsReasoner;

	private Model graph;

	private InfModel inferencingModel;
	public TripleStoreExpert(String KGfileName) {
		pm = new DefaultPrefixManager();
		pm.setPrefix("wd", "http://www.wikidata.org/entity/");
		pm.setPrefix("wdt", "http://www.wikidata.org/prop/direct/");
		pm.setPrefix("owl", "http://www.w3.org/2002/07/owl/");

		this.graph = RDFDataMgr.loadModel(KGfileName);
		// this.rdfsReasoner = ReasonerRegistry.getRDFSReasoner();
		// this.rdfsReasoner = this.rdfsReasoner.bindSchema(graph);

		this.inferencingModel = ModelFactory.createRDFSModel(graph);

		// RDF4J
		/*
		Repository repo = new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));

		File file = new File(KGfileName);
		try {
			con = repo.getConnection();
		} catch (RepositoryException e) {
			logger.fatal("Could not connect to the repository");
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			con.add(file, RDFFormat.TURTLE);
		}
		catch (IOException e) {
			logger.fatal("Could not open the repository file");
			e.printStackTrace();
		}
		catch (RDFParseException e) {
			logger.fatal("Error parsing the repository file");
			e.printStackTrace();
			System.exit(-1);
		}
		 */

    }

	public boolean holds(OWLSubClassOfAxiom ax) {

		String askQueryStr = OWL2SPARQL.buildQuery(ax);
		Instant start = Instant.now();
		Query query = QueryFactory.create(askQueryStr) ;

		QueryExecution qexec = QueryExecutionFactory.create(query, this.inferencingModel) ;
		boolean askResult = qexec.execAsk() ;
		qexec.close() ;
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		if (timeElapsed > 500) {
			logger.info("SPARQL query took time: " + timeElapsed + " miliseconds.");
			logger.info("query:" + askQueryStr);
		}


		/*
		Instant start = Instant.now();
		if (ax.getSuperClass().isOWLNothing()) {
			String queryStrLhs = OWL2SPARQL.buildQuery(ax.getSubClass());
			Set<Value> resultsLhs = new HashSet<>();
			TupleQuery queryLhs = con.prepareTupleQuery(queryStrLhs);
			try {
				TupleQueryResult result = queryLhs.evaluate();
				for (BindingSet bindingSet : result) {
					Value v = bindingSet.getValue("1");
					resultsLhs.add(v);
				}
			} catch (QueryEvaluationException e) {
				logger.fatal("Error executing the query:" + queryStrLhs);
				e.printStackTrace();
				System.exit(-1);
			}

			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			if (timeElapsed > 500) {
				logger.info("SPARQL query took time: " + timeElapsed + " miliseconds.");
				logger.info("query:" + queryStrLhs + " ==> " + ax.getSuperClass());
			}
			return(resultsLhs.isEmpty());
		}

		String askQueryStr = OWL2SPARQL.buildQuery(ax);
		BooleanQuery askQuery = con.prepareBooleanQuery(askQueryStr);
		boolean askResult = false;
		try {
			askResult = askQuery.evaluate();
		} catch (QueryEvaluationException e) {
			logger.fatal("Error executing the query:" + askQueryStr);
			e.printStackTrace();
			System.exit(-1);
        }

		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		if (timeElapsed > 500) {
			logger.info("SPARQL query took time: " + timeElapsed + " miliseconds.");
			logger.info("query:" + askQueryStr);
		}
		*/
		return(!askResult);
	}

	public PrefixManager getPrefixManager() {
		return pm;
	}
}
