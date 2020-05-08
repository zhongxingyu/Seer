 package org.colomoto.mddlib.operators;
 
 import org.colomoto.mddlib.MDDManager;
 import org.colomoto.mddlib.MDDOperator;
 import org.colomoto.mddlib.MDDVariable;
 import org.colomoto.mddlib.NodeRelation;
 
 /**
  * Collection of classical MDDOperators.
  * 
  * @author Aurelien Naldi
  */
 public class MDDBaseOperators {
 
 	/**
 	 * the AND operator.
 	 */
 	public static final MDDOperator AND = new MDDAndOperator();
 	/**
 	 * the OR operator.
 	 */
 	public static final MDDOperator OR = new MDDOrOperator();
 	
 	private MDDBaseOperators() {
 		// no instance of this class
 	}
 }
 
 /**
  * MDDOperator implementation for the "AND" operation.
  */
 class MDDAndOperator extends AbstractOperator {
 
 	protected MDDAndOperator() {
 		super(true);
 	}
 
 	@Override
 	public int combine(MDDManager ddmanager, int first, int other) {
 		if (first == other) {
			return first;
 		}
 		NodeRelation status = ddmanager.getRelation(first, other);
 
 		switch (status) {
 		case LN:
 		case LL:
 			if (first < 1) {
 				// no need to "use" it: it is a leaf
 				return first;
 			}
 			return ddmanager.use(other);
 		case NL:
 			if (other < 1) {
 				// no need to "use" it: it is a leaf
 				return other;
 			}
 			return ddmanager.use(first);
 		default:
 			return recurse(ddmanager, status, first, other);
 		}
 	}
 
 	@Override
 	public int recurse_multiple(MDDManager ddmanager, int[] nodes, int leafcount, MDDVariable minVar) {
 		for (int i=0 ; i<leafcount ; i++) {
 			if (nodes[i] <= 0) {
 				return 0;
 			}
 		}
 		nodes = prune_start(nodes, leafcount);
 		return super.recurse_multiple(ddmanager, nodes, 0, minVar);
 	}
 
 	@Override
 	protected int multiple_leaves(MDDManager ddmanager, int[] leaves) {
 		for (int i:leaves) {
 			if (i<1) {
 				return i;
 			}
 		}
 		return leaves[0];
 	}
 }
 
 
 /**
  * MDDOperator implementation for the "OR" operation.
  */
 class MDDOrOperator extends AbstractOperator {
 
 	protected MDDOrOperator() {
 		super(true);
 	}
 	
 	@Override
 	public int combine(MDDManager ddmanager, int first, int other) {
 		if (first == other) {
			return first;
 		}
 		NodeRelation status = ddmanager.getRelation(first, other);
 
 		switch (status) {
 		case LN:
 		case LL:
 			if (first > 0) {
 				return first;
 			}
 			return ddmanager.use(other);
 		case NL:
 			if (other > 0) {
 				return other;
 			}
 			return ddmanager.use(first);
 		default:
 			return recurse(ddmanager, status, first, other);
 		}
 	}
 	
 	@Override
 	public int recurse_multiple(MDDManager ddmanager, int[] nodes, int leafcount, MDDVariable minVar) {
 		for (int i=0 ; i<leafcount ; i++) {
 			if (nodes[i] > 0) {
 				return 1;
 			}
 		}
 		nodes = prune_start(nodes, leafcount);
 		return super.recurse_multiple(ddmanager, nodes, 0, minVar);
 	}
 	
 	@Override
 	protected int multiple_leaves(MDDManager ddmanager, int[] leaves) {
 		for (int i:leaves) {
 			if (i>0) {
 				return i;
 			}
 		}
 		return leaves[0];
 	}
 }
