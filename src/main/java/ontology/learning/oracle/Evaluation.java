package ontology.learning.oracle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Evaluation {
    private Logger logger = LogManager.getLogger("OracleEvaluation");

    public void evaluate(OWLOntology resultOntology, OWLReasoner expertReasoner, Set<OWLClassExpression> baseSet) {

        Instant start = Instant.now();

        resultOntology.add(expertReasoner.getRootOntology().getAxioms(AxiomType.CLASS_ASSERTION));
        resultOntology.add(expertReasoner.getRootOntology().getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION));

        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner resultReasoner = rf.createReasoner(resultOntology);
        resultReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        resultReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        resultReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        resultReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Result ontology classified: " + timeElapsed + " ms");

        start = Instant.now();

        expertReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        expertReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        expertReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        expertReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        finish = Instant.now();
        timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Expert ontology classified: " + timeElapsed + " ms");

        start = Instant.now();

        float quality = 0;
        int counter = 0;
        for (OWLClassExpression ce : baseSet) {
            // logger.info("ce:" + ce);
            Set<OWLIndividual> assertedIndividuals = new HashSet<OWLIndividual>();
            for (OWLClassAssertionAxiom ax : expertReasoner.getRootOntology().getClassAssertionAxioms(ce)) {
                assertedIndividuals.add(ax.getIndividual());
            }
            // logger.info("asserted:" + assertedIndividuals);
            Set<OWLNamedIndividual> inferredIndividuals = expertReasoner.getInstances(ce, false).getFlattened();
            // logger.info("inferred:" + inferredIndividuals);
            // take set difference
            inferredIndividuals.removeAll(assertedIndividuals);
            if (!inferredIndividuals.isEmpty()) {
                logger.info("ce:" + ce);
                Set<OWLNamedIndividual> inferredIndividualsResult = resultReasoner.getInstances(ce, false).getFlattened();
                inferredIndividualsResult.removeAll(assertedIndividuals);
                logger.info(inferredIndividualsResult.size() + " / " + inferredIndividuals.size());
                quality += ((float) inferredIndividualsResult.size() / inferredIndividuals.size());
                ++counter;
            }
        }
        if (counter > 0) {
            // logger.info("quality:" + quality);
            // logger.info("counter:" + counter);
            logger.info("Quality:" + (quality / counter));
        }

        finish = Instant.now();
        timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Evaluation time: " + timeElapsed + " ms");
    }
}
