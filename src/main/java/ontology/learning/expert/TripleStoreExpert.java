package ontology.learning.expert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import org.eclipse.rdf4j.query.MalformedQueryException;
// import org.eclipse.rdf4j.query.TupleQuery;
// import org.eclipse.rdf4j.repository.RepositoryConnection;
// import org.eclipse.rdf4j.repository.RepositoryException;

import org.semanticweb.owlapi.model.*;

// import org.aksw.owl2sparql.OWLClassExpressionToSPARQLConverter;
// import org.aksw.owl2sparql.OWLAxiomToSPARQLConverter;


public class TripleStoreExpert implements ExpertOracle {
	protected static final Logger logger = LogManager.getLogger();

	// private RepositoryConnection connection = null;
	
	public TripleStoreExpert(String fileName) {
		/*
		try {
			is = docURL.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Cannot open " + docURL);
			e.printStackTrace();
			System.exit(-1);
		}
		
		RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
		Model model = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(model));
		try {
			   rdfParser.parse(is, docURL.toString());
			}
			catch (IOException e) {
			  // handle IO problems (e.g. the file could not be read)
				logger.error("Error while parsing. Cannot read.");
			}
			catch (RDFParseException e) {
			  // handle unrecoverable parse error
				logger.error("Error while parsing. Unrecoverable parse error.");
			}
			catch (RDFHandlerException e) {
			  // handle a problem encountered by the RDFHandler
				logger.error("Error while parsing.");
			}
			finally {
			  try {
				is.close();
			} 
			  catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("Cannot close " + docURL);
				e.printStackTrace();
			  }
			}
		*/
		
		/*
		Model results = null;
		try {
			results = Rio.parse(is, docURL.toString(), RDFFormat.TURTLE);
		} catch (RDFParseException | UnsupportedRDFormatException | IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error while parsing.");
			e.printStackTrace();
			System.exit(-1);
		}
		for (Statement s : model) {
			System.out.println(s);
		}
			*/
		
		/*
		String sparqlEndpoint = "http://localhost:8080/rdf4j-server";
		Repository repo = new SPARQLRepository(sparqlEndpoint);
		
		try {
			connection = repo.getConnection();
			try {
				is = docURL.openStream();
			} catch (IOException e) {
				logger.error("Cannot open " + docURL);
				e.printStackTrace();
				System.exit(-1);
			}
		   connection.add(is, "", RDFFormat.TURTLE);
		}
		catch (RDF4JException e) {
		   // handle exception. This catch-clause is
		   // optional since RDF4JException is an unchecked exception
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		/*
		finally {
			repo.shutDown();
		}
		*/
		
	}
	
	public String buildWhereClause(int subject, OWLClassExpression c, int successorCount) {

		String s = "?" + String.valueOf(subject);
		switch (c.getClassExpressionType()) {
		case OWL_CLASS:
			return(s + " wdt:P31 " + c + ".\n");
		case OBJECT_SOME_VALUES_FROM:
			String p = (((OWLObjectSomeValuesFrom) c).getProperty()).toString();
			int object = 10 * subject + successorCount;
			String o = "?" + String.valueOf(object);

			// OWLClassExpression filler = ((OWLObjectSomeValuesFrom) c).getFiller();
			// if (filler.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM))
			successorCount = 0;
			return(s + " " + p + " " + o + ". \n" +  buildWhereClause(object, ((OWLObjectSomeValuesFrom) c).getFiller(), successorCount));
		case DATA_ALL_VALUES_FROM:
			break;
		case DATA_EXACT_CARDINALITY:
			break;
		case DATA_HAS_VALUE:
			break;
		case DATA_MAX_CARDINALITY:
			break;
		case DATA_MIN_CARDINALITY:
			break;
		case DATA_SOME_VALUES_FROM:
			break;
		case OBJECT_ALL_VALUES_FROM:
			break;
		case OBJECT_COMPLEMENT_OF:
			break;
		case OBJECT_EXACT_CARDINALITY:
			break;
		case OBJECT_HAS_SELF:
			break;
		case OBJECT_HAS_VALUE:
			break;
		case OBJECT_INTERSECTION_OF:
			String clause = "";
			for (OWLClassExpression conjunct : c.asConjunctSet()) {
				clause += buildWhereClause(subject, conjunct, successorCount);
				if (conjunct.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM))
					++successorCount;
			}
			return(clause);
		case OBJECT_MAX_CARDINALITY:
			break;
		case OBJECT_MIN_CARDINALITY:
			break;
		case OBJECT_ONE_OF:
			break;
		case OBJECT_UNION_OF:
			break;
		default:
			break;
		}

		return("xxx");
	}
	
	
	/*
	public void buildQuery(OWLClassExpression clsExpr) {
		OWLClassExpressionToSPARQLConverter owl2sparql = new OWLClassExpressionToSPARQLConverter();
		System.out.println(owl2sparql.convert(clsExpr, "v0", false));
	}
	*/
	
	public void buildSelectQuery(OWLClassExpression c) {
		// SelectQuery q = Queries.SELECT();
		
		String queryString = "PREFIX ex: <http://example.org/> \n";
		// queryString += "PREFIX foaf: <" + FOAF.NAMESPACE + "> \n";
		queryString += "SELECT ?p \n";
		queryString += "WHERE { \n";
		queryString += "    ?s ?p ?o; \n";
		queryString += "}";

		/*
		TupleQuery query = null;
		try {
			query = connection.prepareTupleQuery(queryString);
		}
		catch (RepositoryException e) {
			e.printStackTrace();
		}
		catch (MalformedQueryException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();

		}
		*/

		System.out.println(queryString);
		
		// A QueryResult is also an AutoCloseable resource, so make sure it gets closed when done.
		/*
		TupleQueryResult result = null;
		try {
			result = query.evaluate();
		}
		catch (QueryEvaluationException e) {
			e.printStackTrace();
		}
		*/
		/*
		while (result.hasNext()) {
			BindingSet solution = result.next();
			System.out.println("?p = " + solution.getValue("p"));
		}
		*/
	}
	
	
	public boolean holds(OWLSubClassOfAxiom ax) {
		// TODO Auto-generated method stub
		return false;
	}

	public OWLOntology getExpertOntology() {
		// TODO Auto-generated method stub
		return null;
	}

}
