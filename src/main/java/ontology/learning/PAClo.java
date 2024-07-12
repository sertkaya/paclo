package ontology.learning;

import ontology.learning.graph.PACloGraph;
import ontology.learning.oracle.PACloOracle;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class PAClo {
	protected static final Logger logger = LogManager.getLogger("PAClo");

	public static void main(String[] args) {

		if (args.length < 1 || (!args[0].equals("-graph") && !args[0].equals("-ontology"))) {
			System.out.println(args[0]);
			logger.fatal("Usage: First argument has to be -graph or -ontology");
			System.exit(-1);
		}

		PACloGraph pg = null;
		PACloOracle ptbx = null;
		if (args[0].equals("-graph"))
			pg = new PACloGraph(args);
		else
			ptbx = new PACloOracle(args);
	}
}
