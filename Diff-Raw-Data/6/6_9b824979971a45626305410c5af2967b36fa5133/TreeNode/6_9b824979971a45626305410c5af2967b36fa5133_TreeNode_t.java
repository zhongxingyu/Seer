 /*
  * Class Name:			TreeNode.java
  * Class Purpose:		A generic tree structure
  * Created by:			boris on 2011-11-04
  */
 package name.bobnet.android.rl.core.util;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class TreeNode {
 
 	// variables
 	private Map<String, TreeNode> children;
 	private Object value;
 	private String name;
 
 	/**
 	 * Create a tree node with a value
 	 * 
	 * @param value
	 *            the value of the tree node
	 * @param name
	 *            the name of the node
 	 */
 	public TreeNode(Object value, String name) {
 		this(name);
 		this.value = value;
 	}
 
 	/**
 	 * Create a tree node with a null value
 	 * 
 	 * @param name
 	 *            the name of the node
 	 */
 	public TreeNode(String name) {
 		children = new HashMap<String, TreeNode>();
 		this.name = name;
 	}
 
 	/**
 	 * @return the value
 	 */
 	public Object getValue() {
 		return value;
 	}
 
 	/**
 	 * @param value
 	 *            the value to set
 	 */
 	public void setValue(Object value) {
 		this.value = value;
 	}
 
 	/**
 	 * get the number of children
 	 * 
 	 * @return the number of children
 	 */
 	public int getChildCount() {
 		return children.size();
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Find a child of the node by name
 	 * 
 	 * @param name
 	 *            the name of the child
 	 */
 	public TreeNode getChild(String name) {
 		return children.get(name);
 	}
 
 	/**
 	 * Navigate to a child in the tree and return it
 	 * 
 	 * @param path
 	 *            an array specifying the path of the child { "node1", "node2",
 	 *            "node3" }
 	 * @return the node at the specified path or null if noting was found
 	 */
 	public TreeNode getChild(String[] path) {
 		// variables
 		TreeNode cNode = this, nNode = null;
 
 		// navigate the tree
 		for (String sNode : path) {
 			// get the new node
 			nNode = cNode.getChild(sNode);
 
 			// check if it's good
 			if (nNode == null) {
 				// bad path
 				return null;
 			} else {
 				// move on
 				cNode = nNode;
 			}
 		}
 
 		// return the node
 		return cNode;
 	}
 
 	/**
 	 * Add a tree node to this node
 	 * 
 	 * @param node
 	 *            the tree node to add
 	 */
 	public void addChild(TreeNode node) {
 		children.put(node.getName(), node);
 	}
 
 	/**
 	 * get a set of the names of all the children
 	 * 
 	 * @return a set of the names of all the children
 	 */
 	public Set<String> getNames() {
 		return children.keySet();
 	}
 
 	/**
 	 * get all the children of the node
 	 * 
 	 * @return a collection of the children
 	 */
 	public Collection<TreeNode> getChildren() {
 		return children.values();
 	}
 
 	/**
 	 * Clear the children of the node
 	 */
 	public void clearChildren() {
 		children.clear();
 	}
 }
