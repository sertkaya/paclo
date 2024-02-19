package ontology.learning.sparql;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

public class OWL2SPARQL {

    public static String buildQuery(OWLClassExpression c) {
        String queryStr = "SELECT DISTINCT ?1 WHERE {\n" + buildWhereClause(1, c, 0) + "}";
        return(queryStr);
    }

    private static String buildWhereClause(int subject, OWLClassExpression c, int successorCount) {

        String s = "?" + String.valueOf(subject);
        switch (c.getClassExpressionType()) {
            case OWL_CLASS:
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
