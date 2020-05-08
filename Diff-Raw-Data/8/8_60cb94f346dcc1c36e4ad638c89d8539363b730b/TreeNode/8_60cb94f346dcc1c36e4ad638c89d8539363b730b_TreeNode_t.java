 /**
  * 
  */
 package cytoscape.dialogs.plugins;
 
 import cytoscape.plugin.PluginInfo;
 
 import java.util.Vector;
 
 public class TreeNode {
 	private Vector<TreeNode> children;
 
 	private PluginInfo infoObj;
 
 	private String title;
 
 	private TreeNode parent;
 
 	private boolean childAllowed;
 
 	/**
 	 * Creates a TreeNode with given title, no parent and allows children.
 	 * 
 	 * @param Title
 	 */
 	public TreeNode(String Title) {
 		init(Title, true);
 	}
 
 	/**
 	 * Creates a TreeNode with given title, no parent and allows children if
 	 * specified.
 	 * 
 	 * @param Title
 	 * @param allowsChildren
 	 */
 	public TreeNode(String Title, boolean allowsChildren) {
 		init(Title, allowsChildren);
 	}
 
 	/**
 	 * Creates a TreeNode with given PluginInfo object, no parent and does not
 	 * allow children.
 	 * 
 	 * @param obj
 	 */
 	public TreeNode(PluginInfo obj) {
 		init(obj.getName(), false);
 		addObject(obj);
 	}
 
 	/**
 	 * Creates a TreeNode with given PluginInfo object, no parent and allows
 	 * children if specified.
 	 * 
 	 * @param obj
 	 * @param allowsChildren
 	 */
 	public TreeNode(PluginInfo obj, boolean allowsChildren) {
 		init(obj.getName(), allowsChildren);
 		addObject(obj);
 	}
 
 	private void init(String Title, boolean childrenOk) {
 		children = new Vector<TreeNode>();
 		title = Title;
 		childAllowed = childrenOk;
 	}
 
 	/**
 	 * Returns true if this node has no children or does not allow them.
 	 * 
 	 * @return
 	 */
 	public boolean isLeaf() {
		if (childAllowed)
 			return false;
 		else
 			return true;
 	}
 
 	/**
 	 * Returns true if otherNode is an ancestor of this node.
 	 * 
 	 * @param otherNode
 	 * @return
 	 */
 	public boolean isNodeAncestor(TreeNode otherNode) {
 		return recursiveParentLookup(this, otherNode);
 	}
 
 	/**
 	 * Sets this nodes parent to newParent, child list does not change.
 	 * 
 	 * @param newParent
 	 */
 	public void setParent(TreeNode newParent) {
 		parent = newParent;
 	}
 
 	/**
 	 * Gets the parent of this node
 	 * 
 	 * @return TreeNode
 	 */
 	public TreeNode getParent() {
 		return parent;
 	}
 
 	/**
 	 * Adds a PluginInfo object to this node.
 	 * 
 	 * @param info
 	 */
 	public void addObject(PluginInfo info) {
 		infoObj = info;
 	}
 
 	/**
 	 * Adds newChild to this node if children are allowed.
 	 * 
 	 * @param newChild
 	 */
 	public void addChild(TreeNode newChild) {
 		if (childAllowed) {
 			children.add(newChild);
 			newChild.setParent(this);
 		} else
 			System.err.println(getTitle() + " does not allow child nodes");
 	}
 
 	/**
 	 * Removes child from this node.
 	 * 
 	 * @param child
 	 */
 	public void removeChild(TreeNode child) {
 		children.remove(child);
 	}
 
 	/**
 	 * Clears the list of children for this node.
 	 */
 	public void removeChildren() {
 		children.clear();
 	}
 
 	/**
 	 * Adds all children to the child list of this node if children are allowed.
 	 * 
 	 * @param children
 	 */
 	public void addChildren(TreeNode[] children) {
 		if (childAllowed) {
 			for (TreeNode c : children) {
 				addChild(c);
 			}
 		} else
 			System.err.println(getTitle() + " does not allow child nodes");
 	}
 
 	/**
 	 * Gets the list of children for this node
 	 * 
 	 * @return Vector<TreeNode>
 	 */
 	public Vector<TreeNode> getChildren() {
 		return children;
 	}
 
 	/**
 	 * Get total number of children for this node
 	 * 
 	 * @return int
 	 */
 	public int getChildCount() {
 		return children.size();
 	}
 
 	/**
 	 * Get child from this node's child list at given index.
 	 * 
 	 * @param index
 	 * @return TreeNode
 	 */
 	public TreeNode getChildAt(int index) {
 		return children.get(index);
 	}
 
 	/**
 	 * Gets the index of child from this node's child list.
 	 * 
 	 * @param child
 	 * @return int
 	 */
 	public int getIndexOfChild(TreeNode child) {
 		return children.indexOf(child);
 	}
 
 	/**
 	 * Gets the total (recursively) of all child nodes that are not leaves.
 	 * 
 	 * @return int
 	 */
 	public int getTreeCount() {
 		return recursiveTotalSubCategorySize(this);
 	}
 
 	/**
 	 * Gets the total (recursively) of all leaves under this node.
 	 * 
 	 * @return
 	 */
 	public int getLeafCount() {
 		return recursiveTotalLeafSize(this);
 	}
 
 	/**
 	 * Gets the title of this node.
 	 * 
 	 * @return String
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * Gets the PluginInfo object of this node.
 	 * 
 	 * @return PluginInfo
 	 */
 	public PluginInfo getObject() {
 		return infoObj;
 	}
 
 	/**
 	 * Gets string representation of this node as 'title: total leaves'
 	 */
 	public String toString() {
 		if (infoObj != null)
 			return infoObj.toString();
 		else
 			return getTitle() + ": " + getLeafCount();
 	}
 
 	private int recursiveTotalSubCategorySize(TreeNode node) {
 		int n = 0;
 		n += node.getChildCount();
 		for (TreeNode c : node.getChildren()) {
 			n += recursiveTotalSubCategorySize(c);
 		}
 		return n;
 	}
 
 	private int recursiveTotalLeafSize(TreeNode node) {
 		int n = 0;
 		if (node.getChildCount() == 0) {
 			n = 1;
 		}
 		for (TreeNode c : node.getChildren()) {
 			n += recursiveTotalLeafSize(c);
 		}
 		return n;
 	}
 
 	private boolean recursiveParentLookup(TreeNode node, TreeNode ancestor) {
 		boolean lookup = false;
 		if (node.getParent() != null) {
 			if (node.getParent().equals(ancestor))
 				lookup = true;
 			else
 				lookup = recursiveParentLookup(node.getParent(), ancestor);
 		}
 		return lookup;
 	}
 
 }
