package ontology.learning.test;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

import ontology.learning.ExpertOracle;
import ontology.learning.PACOntologyLearning;
import ontology.learning.RandomSampler;
import ontology.learning.ReasonerExpert;
import ontology.learning.SamplingOracle;

public class TestPACOntologyCompletion {

	static OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	static OWLDataFactory df = om.getOWLDataFactory();
	
	public OWLClassExpression parseClassExpression(String expr) {
		ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		parser.setStringToParse(expr);
		return(parser.parseClassExpression());
	}
	
	public static void main(String[] args) {

		// OWLClassExpression clsA = df.getOWLClass("A");
		// OWLClassExpression clsB = df.getOWLClass("B");
		// OWLClassExpression clsC = df.getOWLClass("C");
		OWLClassExpression clsA = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A");
		OWLClassExpression clsB = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#B");
		OWLClassExpression clsC = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#C");
		OWLClassExpression clsD = df.getOWLClass("http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#D");
		OWLObjectProperty propR = df.getOWLObjectProperty("r");
		
		OWLClassExpression existsRA = df.getOWLObjectSomeValuesFrom(propR, clsA);
		OWLClassExpression existsRB = df.getOWLObjectSomeValuesFrom(propR, clsB);
		
		Set<OWLClassExpression> baseSet = new HashSet<OWLClassExpression>();
		baseSet.add(clsA);
		baseSet.add(clsB);
		baseSet.add(clsC);
		baseSet.add(clsD);
		// baseSet.add(existsRA);
		// baseSet.add(existsRB);
	
		File expertOntology = new File("/home/bs/research/dev/pacco/src/test/resources/expertOntology.owx");
		IRI expertOntologyIRI = IRI.create(expertOntology);
		ExpertOracle expert = new ReasonerExpert(expertOntologyIRI);
		System.out.println("Ontology prefix: " + expert.getExpertOntology().getOntologyID().getOntologyIRI().get());
		System.out.println("Axioms:");
		expert.getExpertOntology().logicalAxioms().forEach(System.out::println);

		SamplingOracle sampler = new RandomSampler(baseSet);

		File myOntology = new File("/home/bs/research/dev/pacco/src/test/resources/myOntology.owx");
		IRI myOntologyIRI = IRI.create(myOntology);

		PACOntologyLearning pacCompletion = new PACOntologyLearning(myOntologyIRI, baseSet, expert, sampler);
		// pacCompletion.upperApproximation(0.8, 0.5);
		// System.out.println(pacCompletion.callsToSamplingOracle(0.5, 0.5, 3));

		// ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
		// ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(om.getOntologyConfigurator(), df);
		// parser.setStringToParse("Class: <http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A>"); 
		// parser.parseClassExpression();
		
		/*
		ManchesterOWLSyntaxClassExpressionParser clsExprP = new ManchesterOWLSyntaxClassExpressionParser(df, new ShortFormEntityChecker(new CachingBidirectionalShortFormProvider() {
			
			@Override
			protected String generateShortForm(OWLEntity entity) {
				// TODO Auto-generated method stub
				return null;
			}
		}));
		clsExprP.parse("Class: <http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A>");
		*/
		
		OWLOntology ontology = null;
		try {
			// ontology = om.createOntology(IRI.create("urn:test:test"));
			ontology = om.createOntology(expert.getExpertOntology().getOntologyID().getOntologyIRI().get());
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		OWLDataFactory datafactory = ontology.getOWLOntologyManager().getOWLDataFactory();
		// ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass("urn:test:A")));
	    // ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass("urn:test:B")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass(expert.getExpertOntology().getOntologyID().getOntologyIRI().get() + "#" + "A")));
		ontology.add(df.getOWLDeclarationAxiom(df.getOWLClass(expert.getExpertOntology().getOntologyID().getOntologyIRI().get() + "#" + "B")));
		ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(om.getOntologyConfigurator(), df);
		parser.setDefaultOntology(ontology);
		
		
		final Map<String, OWLEntity> map = new HashMap<>();
	    ontology.signature().forEach(x -> map.put(x.getIRI().getFragment(), x));
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
	    
		// parser.setStringToParse("A EquivalentTo: (NOT B)");
	    // OWLAxiom parseAxiom = parser.parseAxiom();
		parser.setStringToParse("A and not B");
	    OWLClassExpression parseExpr = parser.parseClassExpression();
	    System.out.println("of.main() " + parseExpr);
	    /*
		parser.setStringToParse("Class: <somePrefix#Father>" +
		                "   EquivalentTo: \n" +
		                "        <somePrefix#Male>\n" +
		                "         and <somePrefix#Parent>");
		parser.setStringToParse("Class: <http://www.semanticweb.org/bs/ontologies/2023/11/untitled-ontology-20#A>");
		parser.parseClassExpression();
		*/
	}
}