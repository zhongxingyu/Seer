 package eu.play_project.dcep.distributedetalis;
 
 import jpl.PrologException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.graph.Node;
 import com.jtalis.core.JtalisContextImpl;
 
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.api.UsePrologSemWebLib;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class PrologSemWebLib implements UsePrologSemWebLib {
 	private static JtalisContextImpl ctx;
 	int oldValue = 0;
 	long internalEventId = 0; // Ordered event ids. id_0 < id_1 < id2 ...
 	private static Logger logger = LoggerFactory.getLogger(PrologSemWebLib.class);
 
 	@Override
 	public void init(JtalisContextImpl ctx) throws DistributedEtalisException {
 		logger.debug("Initializing " + PrologSemWebLib.class.getSimpleName());
 		
 		PrologSemWebLib.ctx = ctx;
 
 		try {
 			// Load SWI-Prolog Semantic Web Library
 			logger.debug("Loading SWI-Prolog Semantic Web Library");
 			ctx.getEngineWrapper().executeGoal("[library(semweb/rdf_db)]");
 			ctx.getEngineWrapper().executeGoal("[library(xpath)]");
 			ctx.getEngineWrapper().executeGoal("use_module(library(xpath))");
 			ctx.getEngineWrapper().executeGoal("use_module(library(random))");
 			ctx.setEtalisFlags("garbage_clt", "on");
 			ctx.setEtalisFlags("garbage_control","general");
 			ctx.setEtalisFlags("save_ruleId", "on");
 		} catch (PrologException e) {
 			throw new DistributedEtalisException("Error loading SWI-Prolog libraries: " + e.getMessage());
 		}
 	}
 
 	@Override
 	public Boolean addEvent(CompoundEvent event)
 			throws DistributedEtalisException {
 		Boolean dataAddedToTriplestore = true;
 		Boolean gcDataAdded = true;
 		StringBuilder prologString = new StringBuilder();
 		int i = 0;
 
 		for (Quadruple quadruple : event) {
 			i++;
 
 			// TODO use prolog type system
 			Node o = quadruple.getObject();
 			String rdfObject;
 			if (o.isLiteral()) {
 				rdfObject = "'"
 						+ escapeForProlog(quadruple.getObject()
 								.getLiteralLexicalForm()) + "'";
 			} else {
 				// 5.) Resource URI
 				rdfObject = "'" + o.getURI() + "'";
 			}
 			if (i > 1) {
 				prologString.append(", ");
 			}
 			// Add data to triplestore
 			prologString.append("rdf_assert(" + "'" + quadruple.getSubject()
 					+ "', " + "'" + quadruple.getPredicate() + "', "
 					+ rdfObject + ", " + "'" + event.getGraph() + "')");
 		}
 
 		dataAddedToTriplestore = addPayloadToPlTriplestore(prologString.toString());
 
 		// Add GC counter.
 		gcDataAdded = ctx.getEngineWrapper().executeGoal(
 				"assert(referenceCounter('" + event.getGraph() + "', "
 						+ internalEventId + ", -1))");
 		
 		internalEventId++;
 
 		if (!gcDataAdded) {
 			throw new DistributedEtalisException(
 					"Failed to insert garbage collection information in Prolog.");
 		}
 
 		return (dataAddedToTriplestore && gcDataAdded);
 }
 
 	/**
 	 * Execute given String in prolog. If it was not possible retry it after 1ms.
 	 * @param prologString code to execute in prolog.
 	 * @return true if code was executed.
 	 */
 	private boolean addPayloadToPlTriplestore(String prologString){
 		try{
 			boolean result = ctx.getEngineWrapper().executeGoal(prologString);
 //			ctx.getEngineWrapper().executeGoal("printRdfStat");
 //			ctx.getEngineWrapper().executeGoal("printNumberOfEvents");
 //			ctx.getEngineWrapper().executeGoal("printRefCountN");
 //			ctx.getEngineWrapper().executeGoal("printReferenceCounters");
 			
 			return result;
 		} catch (PrologException e) {
 			if (e.getMessage().contains("error(permission_error(write, rdf_db, default)")) {
 				logger.warn("Error: db is locked. Try again.");
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 				return this.addPayloadToPlTriplestore(prologString);
 			} else {
 				logger.error("Error on new event.", e);
 				return false;
 			}
 			
 			
 		}
 	}
 	@Override
 	public CompoundEvent getRdfData(String complexEventID) {
 		throw new RuntimeException("Not implemented in this class.");
 	}
 	
 	/**
 	 * Escape all characters which are illegal in Prolog's quoted strings:
 	 * {@code It's me, Mario.} becomes {@code It\'s me, Mario.}. The resulting
 	 * strings are meanto to be used as <i>quoted atoms</i> in Prolog, between
 	 * single quotes.
 	 * 
	 * @see <a
	 *      href="http://www.swi-prolog.org/pldoc/doc_for?object=section%284,%272.15.1.2%27,swi%28%27/doc/Manual/syntax.html%27%29%29">SWI
	 *      Prolog Character Escape Syntax</a>
 	 */
 	public static String escapeForProlog(String s) {
 		return s.replaceAll("'", "\'");
 	}
 }
