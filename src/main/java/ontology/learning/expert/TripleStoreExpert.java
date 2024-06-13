package ontology.learning.expert;

import ontology.learning.sparql.OWL2SPARQL;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class TripleStoreExpert implements ExpertOracle {
	protected static final Logger logger = LogManager.getLogger();

	RepositoryConnection con;

	private PrefixManager pm;

	public TripleStoreExpert(String KGfileName) {
		pm = new DefaultPrefixManager();
		pm.setPrefix("wd", "http://www.wikidata.org/entity/");
		pm.setPrefix("wdt", "http://www.wikidata.org/prop/direct/");
		pm.setPrefix("owl", "http://www.w3.org/2002/07/owl/");

		// RDF4J
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
    }

	public boolean holds(OWLSubClassOfAxiom ax) {

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
		return(!askResult);
	}

	public PrefixManager getPrefixManager() {
		return pm;
	}
}
