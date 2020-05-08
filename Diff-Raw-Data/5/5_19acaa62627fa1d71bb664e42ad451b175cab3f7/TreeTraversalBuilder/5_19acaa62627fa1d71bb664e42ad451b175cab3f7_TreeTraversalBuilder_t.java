 package util.ast;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import util.ast.node.Node;
 
 /**
  * Constructs an Iterator<Node> over a given AST for pre-order and post-order traversal.
  * 
  * @author sam
  * 
  */
 class TreeTraversalBuilder {
 
 	static enum traversalOrder {
 		PREORDER, POSTORDER;
 	}
 	
 	static Iterator<Node> buildTraversalIterator (Node root, traversalOrder order) {
 		
 		List<Node> iteratorList = new ArrayList<Node>();
 		
 		iteratorList = buildTraversal(root, order);
 		
 		return iteratorList.iterator();
 		
 	}
 	
 	private static List<Node> buildTraversal(Node root, traversalOrder order) {
 		
 		List<Node> subTree = new ArrayList<Node>();
 		
 		// base case
 		if (root.getChildren().isEmpty()) {
 			subTree.add(root);
 			return subTree;
 		}
 		
		if (order == traversalOrder.PREORDER) {
 			subTree.add(root);
 		}
 		
 		for (Node child : root.getChildren()) {
 			subTree.addAll(buildTraversal(child, order));
 		}
 		
		if (order == traversalOrder.POSTORDER) {
 			subTree.add(root);
 		}
 		
 		return subTree;
 		
 	}
 
 }
