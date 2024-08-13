package ontology.learning.oracle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;


public class Evaluation {
    private Logger logger = LogManager.getLogger("OracleEvaluation");

    public void evaluate(OWLOntology resultOntology, OWLReasoner expertReasoner, Set<OWLClassExpression> baseSet, OWLReasoner initialOntologyReasoner) {
        // NB: Removes disjointness axioms from resultOntology

        Instant start = Instant.now();


        OWLOntologyManager manager = resultOntology.getOWLOntologyManager();
        OWLClass owlNothing = manager.getOWLDataFactory().getOWLNothing();
        Set<OWLSubClassOfAxiom> disjointnessAxioms = resultOntology.subClassAxiomsForSuperClass(owlNothing)
                    .collect(Collectors.toSet());
        System.out.println(disjointnessAxioms.size() + " disjointness axioms");
        manager.removeAxioms(resultOntology, disjointnessAxioms);

        resultOntology.add(expertReasoner.getRootOntology().getABoxAxioms(Imports.INCLUDED));

        OWLReasonerFactory rf = new ElkReasonerFactory();
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
        float sum_of_precisions = 0;
        float sum_of_recalls = 0;
        int counter = 0;
        int allInferred = 0;
        int allInferredResult = 0;
        int allShared = 0;
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
            Set<OWLNamedIndividual> inferredIndividualsResult = resultReasoner.getInstances(ce, false).getFlattened();
            // logger.info("inferred:" + inferredIndividuals);
            // take set difference
            // inferredIndividuals.removeAll(instancesInitialOntology);
            if (!inferredIndividuals.isEmpty() || !inferredIndividualsResult.isEmpty()) {
                logger.info("ce:" + ce);
                allInferred += inferredIndividuals.size();
                allInferredResult += inferredIndividualsResult.size();

                // Set<OWLNamedIndividual> shared = new HashSet<>(allInferred);
                // shared.retainAll(inferredIndividualsResult);
                Set<OWLNamedIndividual> shared = inferredIndividuals.stream()
                        .filter(ind -> inferredIndividualsResult.stream().anyMatch(ind2 -> ind.getIRI().equals(ind2.getIRI())))
                        .collect(Collectors.toSet());
                allShared += shared.size();

                float precision = inferredIndividualsResult.size() > 0 ? (float) shared.size() / inferredIndividualsResult.size() : 1;
                logger.info("Precision = " + shared.size() + "/" + inferredIndividualsResult.size() + " = " + precision);
                sum_of_precisions += precision;

                float recall = inferredIndividuals.size() > 0 ? (float) shared.size() / inferredIndividuals.size() : 1;
                logger.info("Recall = " + shared.size() + "/" + inferredIndividuals.size() + " = " + recall);
                sum_of_recalls += recall;

                ++counter;
            }
        }
        logger.info("Classes with inferred instances: " + counter);
        if (counter > 0) {
            logger.info("Macro precision: " + (sum_of_precisions / counter));
            logger.info("Macro recall: " + (sum_of_recalls / counter));

            logger.info("Micro precision: " + ((float) allShared / allInferredResult));
            logger.info("= " + allShared + "/" + allInferredResult);
            logger.info("Micro recall: " + ((float) allShared / allInferred));
            logger.info("= " + allShared + "/" + allInferred);
        }
        logger.info("Classes without inferred instances: " + (baseSet.size() - counter));

        finish = Instant.now();
        timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Evaluation time: " + timeElapsed + " ms");
    }
}
