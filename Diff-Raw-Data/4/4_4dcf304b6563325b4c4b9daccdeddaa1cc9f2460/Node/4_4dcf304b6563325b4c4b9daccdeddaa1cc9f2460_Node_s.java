 package util.ast.node;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import front_end.ConsoleLexer;
 
 import back_end.Visitor;
 
 /**
  * A node within an AST.
  * 
  * @author sam
  * 
  */
 public abstract class Node implements Comparable<Node> {
 
 	// logger used for all nodes
 	protected final static Logger LOGGER = Logger.getLogger(ConsoleLexer.class
 			.getName());
 
 	protected Node parent;
 	// Note: children are ordered left-to-right (first child is leftmost child).
 	protected List<Node> children;
 
 	/**
 	 * Construct a new node.
 	 */
 	public Node() {
 		this(new ArrayList<Node>());
 	}
 
 	public abstract void accept(Visitor v);
 
 	public abstract int visitorTest(Visitor v);
 
 	/**
 	 * Construct a new node with parent as it's parent.
 	 * 
 	 * @param parent
 	 *            the node that will become the parent of this node.
 	 */
 	public Node(List<Node> children) {
 		this.parent = null;
 		this.children = children;
 	}
 
 	/**
 	 * Get all the children of this node, sorted in left-> order.
 	 * 
 	 * @return a list of all children of this node.
 	 */
 	public List<Node> getChildren() {
 		return children;
 	}
 
 	/**
 	 * Get the parent of this node.
 	 * 
 	 * @return the parent of this node.
 	 */
 	public Node getParent() {
 		return parent;
 	}
 
 	/**
 	 * Add a child to this node. The new child will always been the rightmost
 	 * child in this node's subtree.
 	 * 
 	 * @param child
 	 *            - the node to be added.
 	 */
 	public void addChild(Node child) {
 		// Node.LOGGER.info("Before adding child to Node: " + this.toString());
 		if (children == null) {
 			children = new ArrayList<Node>();
 		}
 
 		// don't add a null child
 		if (child == null) {
 			Node.LOGGER.fine(this.toString() + " is  adding "
 					+ "a null child in addChild");
 		} else {
 			children.add(child);
 			child.setParent(this);
 		}
 	}
 
 	/**
 	 * Sets the parent of this node to be the passed node, if this node doesn't
 	 * yet have a parent.
 	 * 
 	 * @param p
 	 *            - the proposed parent node
 	 * @throws UnsupportedOperationException
 	 *             if this node already has a parent.
 	 */
 	public void setParent(Node p) {
 		if (parent == null) {
 			parent = p;
 			return;
 		}
 		throw new UnsupportedOperationException(this.toString()
 				+ " already has a parent!");
 	}
 
 	/**
 	 * Get the identifying name of this node.
 	 * 
 	 * @return a string representation of the identifying name of this node.
 	 */
 	public abstract String getName();
 
 	/**
 	 * A convenient and concise String representation of this node.
 	 * 
 	 * Override getName() in any implementation of Node to change default
 	 * behavior.
 	 * 
 	 * @return a string representation of this node.
 	 */
 	@Override
 	public String toString() {
 		return this.getName();
 	}
 
 	public String getChildrenString() {
 
 		StringBuilder stringBuilder = new StringBuilder();
 
 		stringBuilder.append(this.getName());
 
 		stringBuilder.append(" Children: [");
 
 		for (Node child : children) {
 			stringBuilder.append(child.getName());
 			stringBuilder.append("; ");
 		}
 
 		if (!children.isEmpty()) {
 			stringBuilder.replace(stringBuilder.lastIndexOf("; "),
 					stringBuilder.length(), "");
 		}
 
 		stringBuilder.append("]");
 
 		return stringBuilder.toString();
 
 	}
 
 	/**
 	 * Compares this node to another node.
 	 * 
 	 * Comparison is done based on the names of the two nodes.
 	 */
 	@Override
 	public int compareTo(Node that) {
 		return this.getName().compareTo(that.getName());
 	}
 
 }
