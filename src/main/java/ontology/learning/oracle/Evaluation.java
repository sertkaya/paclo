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
import java.util.Set;

public class Evaluation {
    private Logger logger = LogManager.getLogger("OracleEvaluation");

    public void evaluate(OWLOntology resultOntology, OWLReasoner expertReasoner, Set<OWLClassExpression> baseSet) {

        resultOntology.add(expertReasoner.getRootOntology().getAxioms(AxiomType.CLASS_ASSERTION));
        resultOntology.add(expertReasoner.getRootOntology().getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION));

        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner resultReasoner = rf.createReasoner(resultOntology);
        resultReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        resultReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        resultReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        resultReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        expertReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_HIERARCHY);
        expertReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        expertReasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS);
        expertReasoner.precomputeInferences(InferenceType.OBJECT_PROPERTY_ASSERTIONS);

        for (OWLClassExpression ce : baseSet) {
            Set<OWLNamedIndividual> directIndividuals = expertReasoner.getInstances(ce, true).getFlattened();
            Set<OWLNamedIndividual> individuals = expertReasoner.getInstances(ce, false).getFlattened();
            // take set difference
            individuals.removeAll(directIndividuals);
            if (!individuals.isEmpty()) {
                logger.info("ce:" + ce);
                Set<OWLNamedIndividual> directIndividualsResult = resultReasoner.getInstances(ce, true).getFlattened();
                Set<OWLNamedIndividual> individualsResult = resultReasoner.getInstances(ce, false).getFlattened();
                individualsResult.removeAll(directIndividualsResult);
                logger.info(individualsResult.size() + " / " + individuals.size());
            }
        }
    }
}
