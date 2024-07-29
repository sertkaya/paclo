package ontology.learning.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Utils {

    protected static final Logger logger = LogManager.getLogger();

    public static Set<OWLClassExpression> readBaseSet(File f, OWLOntology initialOntology) {

        OWLOntologyManager om = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = om.getOWLDataFactory();

        /*
        OWLOntology initialOntology = null;
        try {
            initialOntology = om.loadOntology(initialOntologyIRI);
            logger.debug("Successfully loaded ontology");
        }
        catch (OWLOntologyCreationException e) {
            logger.fatal("Error loading ontology");
            System.exit(-1);
        }
         */

        Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
        ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(om.getOntologyConfigurator(), df);
        parser.setDefaultOntology(initialOntology);

        final Map<String, OWLEntity> map = new HashMap<>();
        initialOntology.signature().forEach(x -> map.put(x.getIRI().getFragment(), x));
        parser.setOWLEntityChecker(new OWLEntityChecker() {
            private <T> T v(String name, Class<T> t) {
                OWLEntity e = map.get(name);
                if (t.isInstance(e)) {
                    return t.cast(e);
                }
                return null;
            }

            @Override
            public OWLObjectProperty getOWLObjectProperty(String name) {
                return v(name, OWLObjectProperty.class);
            }

            @Override
            public OWLNamedIndividual getOWLIndividual(String name) {
                return v(name, OWLNamedIndividual.class);
            }

            @Override
            public OWLDatatype getOWLDatatype(String name) {
                return v(name, OWLDatatype.class);
            }

            @Override
            public OWLDataProperty getOWLDataProperty(String name) {
                return v(name, OWLDataProperty.class);
            }

            @Override
            public OWLClass getOWLClass(String name) {
                return v(name, OWLClass.class);
            }

            @Override
            public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
                return v(name, OWLAnnotationProperty.class);
            }
        });

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            logger.fatal("File " + f.getAbsolutePath() + " not found");
            e.printStackTrace();
        }
        OWLClassExpression clsExpr;
        String line;
        try {
            line = reader.readLine();
            while (line != null) {
                if (line.startsWith("#")) {
                    line = reader.readLine();
                    continue;
                }
                if (line.equals("owl:Nothing")) {
                    clsExpr = df.getOWLNothing();
                }
                else {
                    parser.setStringToParse(line);
                    clsExpr = parser.parseClassExpression();
                }
                baseSet.add(clsExpr);
                line = reader.readLine();
            }
        } catch (IOException e) {
            logger.fatal("Error reading from file");
            e.printStackTrace();
        }


        try {
            reader.close();
        } catch (IOException e) {
            logger.error("Error closing file");
            e.printStackTrace();
        }

        return(baseSet);
    }
}
