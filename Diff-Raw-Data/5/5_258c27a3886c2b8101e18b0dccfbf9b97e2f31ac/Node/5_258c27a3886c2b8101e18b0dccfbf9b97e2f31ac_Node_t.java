 package util.ast.node;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A node within an AST.
  * 
  * @author sam
  * 
  */
 public abstract class Node implements Comparable<Node> {
 
 	protected Node parent;
 	// Note: children are ordered left-to-right (first child is leftmost child).
 	protected List<Node> children;
 
 	public Node() {
 		this(null);
 	}
 
 	public Node(Node parent) {
 		this.parent = parent;
 		this.children = new ArrayList<Node>();
 	}
 
 	public List<Node> getChildren() {
 		return children;
 	}
 	
 	public Node getParent() {
 		return parent;
 	}
 	
 	public void addChild(Node child) {
 		children.add(child);
 	}
 	
 	/**
 	 * Sets the parent of this node to be the passed node, if this node doesn't yet have a parent.
 	 * @param p - the proposed parent node
 	 * @return true if this was parent-less and passed node is now parent, false otherwise.
 	 */
 	public boolean setParent(Node p) {
 		if (parent == null) {
 			parent = p;
 			return true;
 		}
 		return false;
 	}
 	
 	/**
	 * Get the identifying name of this node. 
	 * 
	 * Override toString() in any implementation of Node to change default behavior.
 	 * @return a string representation of the identifying name of this node. 
 	 */
 	public String getName() {
 		return this.toString();
 	}
 	
 }
