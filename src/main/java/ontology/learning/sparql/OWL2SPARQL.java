package ontology.learning.sparql;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.StringComparator;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class OWL2SPARQL {

    static String prefixStr = "PREFIX owl: <http://www.w3.org/2002/07/owl/>\nPREFIX wdt: <http://www.wikidata.org/entity/>\n\n";
    public static String buildQuery(OWLSubClassOfAxiom ax) {
        String queryStr = prefixStr + "SELECT DISTINCT ?1 WHERE {\n" +
                buildWhereClause(1, ax.getSubClass(), 0) +
                " FILTER NOT EXISTS {\n" +
                buildWhereClause(1, ax.getSuperClass(), 0) +
                "}\n}\n";
        return(queryStr);
    }
    public static String buildQuery(OWLClassExpression c) {
        String queryStr = prefixStr + "SELECT DISTINCT ?1 WHERE {\n" +
                buildWhereClause(1, c, 0) + "}";
        return(queryStr);
    }

    /**
     *
     * @param subject the root variable (use 1)
     * @param c the owl classexpression to convert
     * @param successorCount number of successors in the current role depth (use 0)
     * @return
     * For subject = 1 and successorCount = 0,
     * the variables at role depth 1 are ?10, ?11, ?12 ...
     * at role depth 2 are ?20, ?21, ?22 ...
     */
    private static String buildWhereClause(int subject, OWLClassExpression c, int successorCount) {

        String s = "?" + String.valueOf(subject);
        switch (c.getClassExpressionType()) {
            case OWL_CLASS:
                // return(s + " wdt:P31 " + c + ".\n");
                // return(s + " <http://www.wikidata.org/entity/P31> " + c + ".\n");
                if (c.isOWLThing()) {
                    return(s + " ?p " +  "?o.\n");
                }
                if (c.isOWLNothing()) {
                    return(s + " wdt:NONEXISTING_PROPERTY " +  "wdt:NONEXISTING_OBJECT.\n");
                }
                return(s + " wdt:P31 " + c + ".\n");
            case OBJECT_SOME_VALUES_FROM:
                String p = (((OWLObjectSomeValuesFrom) c).getProperty()).toString();
                int object = 10 * subject + successorCount;
                String o = "?" + String.valueOf(object);

                // OWLClassExpression filler = ((OWLObjectSomeValuesFrom) c).getFiller();
                // if (filler.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM))
                successorCount = 0;
                return(s + " " + p + " " + o + ". \n" +  buildWhereClause(object, ((OWLObjectSomeValuesFrom) c).getFiller(), successorCount));
            case DATA_ALL_VALUES_FROM:
                break;
            case DATA_EXACT_CARDINALITY:
                break;
            case DATA_HAS_VALUE:
                break;
            case DATA_MAX_CARDINALITY:
                break;
            case DATA_MIN_CARDINALITY:
                break;
            case DATA_SOME_VALUES_FROM:
                break;
            case OBJECT_ALL_VALUES_FROM:
                break;
            case OBJECT_COMPLEMENT_OF:
                break;
            case OBJECT_EXACT_CARDINALITY:
                break;
            case OBJECT_HAS_SELF:
                break;
            case OBJECT_HAS_VALUE:
                break;
            case OBJECT_INTERSECTION_OF:
                String clause = "";
                for (OWLClassExpression conjunct : c.asConjunctSet()) {
                    clause += buildWhereClause(subject, conjunct, successorCount);
                    if (conjunct.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM))
                        ++successorCount;
                }
                return(clause);
            case OBJECT_MAX_CARDINALITY:
                break;
            case OBJECT_MIN_CARDINALITY:
                break;
            case OBJECT_ONE_OF:
                break;
            case OBJECT_UNION_OF:
                break;
            default:
                break;
        }

        return("xxx");
    }
}
