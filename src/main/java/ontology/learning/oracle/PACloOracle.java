package ontology.learning.oracle;

import ontology.learning.ExpertOracle;
import ontology.learning.ILearningFrameworkSubsumption;
import ontology.learning.LearningFrameworkSubsumption;
import ontology.learning.LearningFrameworkSubsumptionUpper;
import ontology.learning.sampler.RandomSubsumptionSampler;
import ontology.learning.sampler.SubsumptionSamplingOracle;
import ontology.learning.sampler.WeightedABoxInducedSubsumptionSampler;
import ontology.learning.sampler.ABoxInducedSubsumptionSampler;
import ontology.learning.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
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

        if (args.length < 7 || args.length > 9) {
            logger.fatal("Usage: -ontology epsilon delta initialOntology expertOntology baseSetFile outputOntology [-upper] [-uniform]");
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

        ExpertOracle expert = new ReasonerExpert(expertOntologyIRI);

        OWLOntologyManager om =  OWLManager.createOWLOntologyManager();
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

        // Add the ABox assertions from the expert ontology to the initial ontology
        // Needed for the weightedsampler and for the evaluation.
        initialOntology.add(((ReasonerExpert) expert).getExpertOntology().getABoxAxioms(Imports.INCLUDED));

        OWLReasoner initialOntologyReasoner = rf.createReasoner(initialOntology);
        initialOntologyReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        initialOntologyReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        initialOntologyReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        initialOntologyReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        Set<OWLClassExpression> baseSet = Utils.readBaseSet(baseSetFile, initialOntology);

        SubsumptionSamplingOracle sampler = null;
        if (args.length > 7) {
            if (args[args.length - 1].equals("-uniform")) {
                sampler = new RandomSubsumptionSampler(baseSet);
                logger.info("Uniform sampler");
            } else if (args[args.length - 1].equals("-weighted")) {
                sampler = new WeightedABoxInducedSubsumptionSampler(baseSet, initialOntologyReasoner, false);
                logger.info("Weighted ABox-induced sampler");
            }
        }
        if (sampler == null) {
            sampler = new ABoxInducedSubsumptionSampler(baseSet, initialOntologyReasoner, false);
            logger.info("ABox-induced sampler");
        }

        Instant start = Instant.now();
        ILearningFrameworkSubsumption framework = (args.length > 7 && args[7].equals("-upper")) ?
            new LearningFrameworkSubsumptionUpper(initialOntology, baseSet, expert, sampler, initialOntologyReasoner) :
            new LearningFrameworkSubsumption(initialOntology, baseSet, expert, sampler, initialOntologyReasoner);
        OWLOntology resultOntology = framework.approximation(epsilon, delta, resultOntologyIRI);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Execution time: " + timeElapsed + " ms");

        Evaluation e = new Evaluation();
        e.evaluate(resultOntology, ((ReasonerExpert) expert).getReasoner(), baseSet, initialOntologyReasoner);
    }


}
