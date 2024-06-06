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
	RepositoryConnection con;

	private PrefixManager pm;
	// private OWLReasoner reasoner;

	// public TripleStoreExpert(String KGfileName, IRI initialOntology) {
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

		/*
        OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = om.getOWLDataFactory();
		try {
			this.ontology = om.loadOntology(initialOntology);
		}
		catch (OWLOntologyCreationException e) {
			logger.fatal("Error loading ontology");
			e.printStackTrace();
			System.exit(-1);
		}
		 */

		/*
		OWLReasonerFactory rf = new ReasonerFactory();
		this.reasoner = rf.createReasoner(ontology);
		this.reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
		 */

    }

	public boolean holds(OWLSubClassOfAxiom ax) {

		/*
		String queryStrLhs = OWL2SPARQL.buildQuery(ax.getSubClass());
		String queryStrRhs = OWL2SPARQL.buildQuery(ax.getSuperClass());

		logger.debug("query starting");
		logger.trace("ax: " + ax);
		logger.trace("queryStrLhs:\n" + queryStrLhs);
		logger.trace("queryStrRhs:\n" + queryStrRhs);
		logger.trace("ASK Query:\n" + OWL2SPARQL.buildQuery(ax));

		// TODO: Use instead an ASK query to make it more efficient!
		*/

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

			return(resultsLhs.isEmpty());
		}

		/*
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
		boolean selectResult = resultsRhs.containsAll(resultsLhs);
		 */

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

		// if (askResult == selectResult)
		// 	logger.error("ASK and SELECT not equivalent!");

		// return(selectResult);
		return(!askResult);
	}

	/*
	public OWLOntology getExpertOntology() {
		return(this.ontology);
	}
	 */

	public PrefixManager getPrefixManager() {
		return pm;
	}
}
