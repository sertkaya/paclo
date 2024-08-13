package ontology.learning.graph;

import ontology.learning.ExpertOracle;
import ontology.learning.LearningFrameworkSubsumptionUpper;
import ontology.learning.sampler.RandomSubsumptionSampler;
import ontology.learning.sampler.SubsumptionSamplingOracle;
import ontology.learning.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class PACloGraph {
    private static final Logger logger = LogManager.getLogger(PACloGraph.class);
    public PACloGraph(String[] args) {
        if (args.length != 7) {
            logger.fatal("Usage: -graph epsilon delta initialOntology knowledgeGraph baseSetFile outputOntology");
            System.exit(-1);
        }

        double epsilon = Double.parseDouble(args[1]);
        double delta = Double.parseDouble(args[2]);

        File initialOntologyFile = new File(args[3]);
        String knowledgeGraphFileName = args[4];
        File baseSetFile = new File(args[5]);
        File resultOntologyFile = new File(args[6]);

        IRI initialOntologyIRI = IRI.create(initialOntologyFile);
        IRI resultOntologyIRI = IRI.create(resultOntologyFile);

        OWLOntologyManager om = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = om.getOWLDataFactory();
        OWLReasonerFactory rf = new ElkReasonerFactory();

        OWLOntology initialOntology = null;
        try {
            initialOntology = om.loadOntology(initialOntologyIRI);
            logger.debug("Successfully loaded initial ontology");
        }
        catch (OWLOntologyCreationException e) {
            logger.fatal("Error loading initial ontology");
            System.exit(-1);
        }
        OWLReasoner initialOntologyReasoner = rf.createReasoner(initialOntology);
        initialOntologyReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        // Measure time
        Instant start = Instant.now();

        Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, initialOntology);
        // baseSet.add(df.getOWLThing());
        baseSet.add(df.getOWLNothing());

        ExpertOracle expert = new FormalContextExpert(baseSet, knowledgeGraphFileName, df);

        SubsumptionSamplingOracle sampler = new RandomSubsumptionSampler(baseSet);
        // SamplingOracle sampler = new RandomSampler(baseSet);

        LearningFrameworkSubsumptionUpper framework = new LearningFrameworkSubsumptionUpper(initialOntology, baseSet, expert, sampler, initialOntologyReasoner);
        // LearningFrameworkCompletion pacCompletion = new LearningFrameworkCompletion(initialOntology, baseSet, expert, sampler, om, reasoner);
        framework.approximation(epsilon, delta, resultOntologyIRI);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Execution time: " + timeElapsed + " ms");
    }
}
