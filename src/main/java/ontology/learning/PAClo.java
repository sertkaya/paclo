package ontology.learning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

import ontology.learning.expert.ExpertOracle;
import ontology.learning.expert.ReasonerExpert;
import ontology.learning.sampler.RandomSubsumptionSampler;
import ontology.learning.sampler.SubsumptionSamplingOracle;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class PAClo {
	protected static final Logger logger = LogManager.getLogger();
	
	public static Set<OWLClassExpression> readBaseSet(File f, OWLOntology o) {
		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = om.getOWLDataFactory();
		ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(om.getOntologyConfigurator(), df);
		parser.setDefaultOntology(o);
		
		final Map<String, OWLEntity> map = new HashMap<>();
	    o.signature().forEach(x -> map.put(x.getIRI().getFragment(), x));
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

	public static void main(String[] args) {

		if (args.length != 6) {
			logger.fatal("Usage: epsilon delta initialOntology expertOntology baseSetFile outputOntology");
			System.exit(-1);
		}
		double epsilon = Double.parseDouble(args[0]);
		double delta = Double.parseDouble(args[1]);
		
		File initialOntology = new File(args[2]);
		File expertOntology = new File(args[3]);
		File baseSetFile = new File(args[4]);
		File resultOntology = new File(args[5]);
		
		IRI expertOntologyIRI = IRI.create(expertOntology);
		ExpertOracle expert = new ReasonerExpert(expertOntologyIRI);
		
		Set<OWLClassExpression> baseSet = readBaseSet(baseSetFile, expert.getExpertOntology());
		
		// SamplingOracle sampler = new RandomSampler(baseSet);
		SubsumptionSamplingOracle sampler = new RandomSubsumptionSampler(baseSet);

		IRI initialOntologyIRI = IRI.create(initialOntology);
		IRI resultOntologyIRI = IRI.create(resultOntology);

		// PACOntologyCompletion pacCompletion = new PACOntologyCompletion(initialOntologyIRI, baseSet, expert, sampler);
		PACOntologyLearningSub pacCompletion = new PACOntologyLearningSub(initialOntologyIRI, baseSet, expert, sampler);
		pacCompletion.upperApproximation(epsilon, delta, resultOntologyIRI);

	}

}
