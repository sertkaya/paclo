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

    public void evaluate(OWLOntology resultOntology, OWLReasoner expertReasoner, Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner) {

        Instant start = Instant.now();

        // resultOntology.add(expertReasoner.getRootOntology().getAxioms(AxiomType.CLASS_ASSERTION));
        // resultOntology.add(expertReasoner.getRootOntology().getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION));
        resultOntology.add(expertReasoner.getRootOntology().getABoxAxioms(Imports.INCLUDED));

        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner resultReasoner = rf.createReasoner(resultOntology);
        resultReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        resultReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        resultReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        resultReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Result ontology classified: " + timeElapsed + " ms");

        // no need to classify. already classified in ReasonerExpert
        /*
        start = Instant.now();

        expertReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        expertReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        expertReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        expertReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        finish = Instant.now();
        timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Expert ontology classified: " + timeElapsed + " ms");
         */

        start = Instant.now();
        float quality = 0;
        int counter = 0;
        for (OWLClassExpression ce : baseSet) {
            // logger.info("ce:" + ce);
            // Set<OWLIndividual> instancesInitialOntology = new HashSet<OWLIndividual>();
            // TODO: Correct this! Retrieve not the asserted individuals, but the individuals that follow from the ABox
            // w.r.t. the initial (possibly empty) TBox. So, ask the reasoner with the initial ontology as the root ontology.
            // for (OWLClassAssertionAxiom ax : expertReasoner.getRootOntology().getClassAssertionAxioms(ce)) {
            //     instancesInitialOntology.add(ax.getIndividual());
            // }
            // TODO done.
            Set<OWLNamedIndividual> instancesInitialOntology = initialOntologyReasoner.getInstances(ce).getFlattened();
            // logger.info("asserted:" + instancesInitialOntology);
            Set<OWLNamedIndividual> inferredIndividuals = expertReasoner.getInstances(ce, false).getFlattened();
            // logger.info("inferred:" + inferredIndividuals);
            // take set difference
            inferredIndividuals.removeAll(instancesInitialOntology);
            if (!inferredIndividuals.isEmpty()) {
                // logger.info("ce:" + ce);
                Set<OWLNamedIndividual> inferredIndividualsResult = resultReasoner.getInstances(ce, false).getFlattened();
                inferredIndividualsResult.removeAll(instancesInitialOntology);
                // logger.info(inferredIndividualsResult.size() + " / " + inferredIndividuals.size());
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
