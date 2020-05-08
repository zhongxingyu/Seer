 package obr;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import moca.graphs.vertices.Vertex;
 
 public class FrontierGuardedCheck implements DecidableClassCheck{
 
 	public static final DecidableClassLabel LABEL = new DecidableClassLabel("frontier-guarded",false,false,true);
 	
 	/**
 	 * Checks a graph of rule dependencies.
 	 * @return the decidable class label if the grd belongs to this decidable class, null otherwise.
 	 */
 	public DecidableClassLabel grdCheck(GraphRuleDependencies grd) {
 		if (check(grd.getVertexCollection()))
 			return LABEL;
 		return null;
 	}
 
 	/**
 	 * Checks only a strongly connected component of the graph of rule dependencies.
 	 * @param grd The graph of rule dependencies.
 	 * @param scc The strongly connected component to be checked.
 	 * @return the decidable class label if the strongly connected component belongs to this decidable class, null otherwise.
 	 */
 	public DecidableClassLabel sccCheck(GraphRuleDependencies grd, int sccID) {
 		if (check(grd.getComponent(sccID)))
 			return LABEL;
 		return null;
 	}
 
 	protected boolean check(Iterable<Vertex<AtomicRule> > rules) {
 		for (Vertex<AtomicRule> vrule : rules) {
 			if (!ruleCheck(vrule.getValue()))
 				return false;
 		}
 		return true;
 	}
 
 	protected boolean ruleCheck(AtomicRule rule) {
 		ArrayList<Vertex<Object>> frontier = rule.frontier();
 		boolean guarded;
 		for (Atom atom : rule) {
 			guarded = true;
 			for (Vertex<Object> vertexTerm : frontier) {
 				Term t = (Term)(vertexTerm.getValue());
 				if (!atom.contains(t)) {
 					guarded = false;
 					break;
 				}
 			}
 			if (guarded)
 				return true;
 		}
 		return false;
 	}
 
 };
 
