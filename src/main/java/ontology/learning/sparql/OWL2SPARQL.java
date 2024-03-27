package ontology.learning.sparql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.StringComparator;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class OWL2SPARQL {

    protected static final Logger logger = LogManager.getLogger();

    static String prefixStr = "PREFIX owl: <http://www.w3.org/2002/07/owl/>\nPREFIX wd: <http://www.wikidata.org/entity/>\n" +
        "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n\n";
    public static String buildQuery(OWLSubClassOfAxiom ax) {
        String queryStr = prefixStr + "ASK {\n";
        queryStr += buildWhereClause(1, ax.getSubClass().getNNF(), 0);
        queryStr += "FILTER NOT EXISTS {\n" + buildWhereClause(1, ax.getSuperClass().getNNF(), 99) + "}\n";
        queryStr += "}\n";
        return(queryStr);
    }

    public static String buildQuery(OWLClassExpression c) {
        String queryStr = prefixStr + "SELECT DISTINCT ?1 WHERE {\n" +
                buildWhereClause(1, c.getNNF(), 0) + "}";
        return(queryStr);
    }

    /**
     *
     * @param subject the root variable (use 1)
     * @param c the owl classexpression in NNF to be converted to SPARQL query
     * @param successorCount number of successors in the current role depth (use 0)
     * @return
     * For subject = 1 and successorCount = 0,
     * the variables at role depth 1 are ?10, ?11, ?12 ...
     * at role depth 2 are ?20, ?21, ?22 ...
     */
    private static String buildWhereClause(int subject, OWLClassExpression c, int successorCount) {

        String s = "?" + String.valueOf(subject);
        String clause = "";
        switch (c.getClassExpressionType()) {
            case OWL_CLASS:
                if (c.isOWLThing()) {
                    return(s + " ?p " +  "?o.\n");
                }
                if (c.isOWLNothing()) {
                    return(s + " wdt:NONEXISTING_PROPERTY " +  "wd:NONEXISTING_OBJECT.\n");
                }
                // return(s + " wdt:P31/wdt:P279* " + c + ".\n");
                // return(s + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>/<http://www.w3.org/2000/01/rdf-schema#subClassOf>* " + c + ".\n");
                // return(s + " wdt:P31 " + c + ".\n");
                return(s + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ".\n");
            case OBJECT_SOME_VALUES_FROM:
                String p = (((OWLObjectSomeValuesFrom) c).getProperty()).toString();
                int object = 10 * subject + successorCount;
                String o = "?" + String.valueOf(object);

                successorCount = 0;
                return(s + " " + p + " " + o + ". \n" +  buildWhereClause(object, ((OWLObjectSomeValuesFrom) c).getFiller(), successorCount));
            case OBJECT_INTERSECTION_OF:
                clause = "";
                for (OWLClassExpression conjunct : c.asConjunctSet()) {
                    clause += buildWhereClause(subject, conjunct, successorCount);
                    if (conjunct.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM))
                        ++successorCount;
                }
                return(clause);
            case OBJECT_UNION_OF:
                clause = "";
                Set<OWLClassExpression> disjunctSet = c.asDisjunctSet();
                while (disjunctSet.iterator().hasNext()) {
                    OWLClassExpression disjunct = disjunctSet.iterator().next();
                    clause += "{" + buildWhereClause(subject, disjunct, successorCount) + "}\n";
                    if (disjunctSet.iterator().hasNext())
                        clause += " UNION\n";
                }
                return(clause);
            case OBJECT_COMPLEMENT_OF:
                clause = s + "?predicate_place_holder [].\n";
                clause += "FILTER NOT EXISTS {" + s + " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + c + ".}\n";
                return(clause);
            case DATA_ALL_VALUES_FROM:
            case DATA_EXACT_CARDINALITY:
            case DATA_HAS_VALUE:
            case DATA_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_SOME_VALUES_FROM:
            case OBJECT_ALL_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_HAS_SELF:
            case OBJECT_HAS_VALUE:
            case OBJECT_MAX_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_ONE_OF:
                logger.error("Unsupported concept constructor");
                return("");
            default:
                logger.error("Unknown concept constructor");
                return("");
        }

    }
}
