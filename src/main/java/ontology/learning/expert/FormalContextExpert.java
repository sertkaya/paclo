package ontology.learning.expert;

import fca.FormalContext;
import ontology.learning.sparql.OWL2SPARQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class FormalContextExpert implements  ExpertOracle {
    protected static final Logger logger = LogManager.getLogger();


    private FormalContext fc;
    private OWLDataFactory dataFactory;
    public  FormalContextExpert(Set<OWLClassExpression> baseSet, String KGfileName, OWLDataFactory dataFactory) {
        this.fc = new FormalContext(0, baseSet);
        this.dataFactory = dataFactory;


        // RDF4J
        Repository repo = new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));
        repo.init();

        File file = new File(KGfileName);
        RepositoryConnection con = null;
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

        Instant start = Instant.now();

        Set<Value> instances = new HashSet<>();
        for (OWLClassExpression e : baseSet) {
            String queryString = OWL2SPARQL.buildQuery(e);
            TupleQuery query = con.prepareTupleQuery(queryString);

            try (TupleQueryResult result = query.evaluate()) {
                // iterate over all solutions in the result...
                while (result.hasNext()) {
                    BindingSet solution = result.next();
                    instances.add(solution.getValue("1"));
                }
            }
        }
        logger.info("size:" + instances.size());

        for (Value v : instances) {
            logger.info("v: " + v);
            BitSet objectIntent = new BitSet(baseSet.size());
            for (int i = 0; i < this.fc.getAttributes().length; ++i) {
                OWLClassExpression c = (OWLClassExpression) this.fc.getAttributes()[i];
                logger.info("c: " + c);
                String queryStr = OWL2SPARQL.buildQuery(v, c);
                logger.info("queryStr: " + queryStr);
                BooleanQuery askQuery = con.prepareBooleanQuery(queryStr);
                boolean askResult = false;
                try {
                    askResult = askQuery.evaluate();
                } catch (QueryEvaluationException e) {
                    logger.fatal("Error executing the query:" + queryStr);
                    e.printStackTrace();
                    System.exit(-1);
                }
                if (askResult) {
                    objectIntent.set(i);
                }
            }
            this.fc.addObject(objectIntent);
        }
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("FormalContextExpert time:" + timeElapsed);
    }
    @Override
    public boolean holds(OWLSubClassOfAxiom ax) {
        BitSet premise = new BitSet(this.fc.getAttributes().length);
        premise.clear();
        BitSet conclusion = new BitSet(this.fc.getAttributes().length);
        conclusion.clear();
        Set<OWLClassExpression> p = ax.getSubClass().asConjunctSet();
        Set<OWLClassExpression> c = ax.getSuperClass().asConjunctSet();
        for (int i = 0; i < this.fc.getAttributes().length; ++i) {
            if (p.contains((OWLClassExpression) this.fc.getAttributes()[i]))
                premise.set(i);
            if (c.contains((OWLClassExpression) this.fc.getAttributes()[i]))
                conclusion.set(i);
        }
        return(this.fc.satisfiesImplication(premise, conclusion));
    }
}
