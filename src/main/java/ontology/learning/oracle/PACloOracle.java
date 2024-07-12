package ontology.learning.oracle;

import ontology.learning.ExpertOracle;
import ontology.learning.LearningFrameworkSubsumption;
import ontology.learning.sampler.RandomSubsumptionSampler;
import ontology.learning.sampler.SubsumptionSamplingOracle;
import ontology.learning.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class PACloOracle {
    private static final Logger logger = LogManager.getLogger(PACloOracle.class);

    public PACloOracle(String[] args) {

        if (args.length != 7) {
            logger.fatal("Usage: -ontology epsilon delta initialOntology expertOntology baseSetFile outputOntology");
            System.exit(-1);
        }

        double epsilon = Double.parseDouble(args[1]);
        double delta = Double.parseDouble(args[2]);

        File initialOntologyFile = new File(args[3]);
        File expertOntologyFile = new File(args[4]);
        File baseSetFile = new File(args[5]);
        File resultOntologyFile = new File(args[6]);

        IRI initialOntologyIRI = IRI.create(initialOntologyFile);
        IRI resultOntologyIRI = IRI.create(resultOntologyFile);
        IRI expertOntologyIRI = IRI.create(expertOntologyFile);

        Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, initialOntologyIRI);

        ExpertOracle expert = new ReasonerExpert(expertOntologyIRI);
        SubsumptionSamplingOracle sampler = new RandomSubsumptionSampler(baseSet);

        Instant start = Instant.now();
        LearningFrameworkSubsumption framework = new LearningFrameworkSubsumption(initialOntologyIRI, baseSet, expert, sampler);
        OWLOntology resultOntology = framework.upperApproximation(epsilon, delta, resultOntologyIRI);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Execution time: " + timeElapsed + " ms");

        Evaluation e = new Evaluation();
        e.evaluate(resultOntology, ((ReasonerExpert) expert).getReasoner(), baseSet);
    }


}
