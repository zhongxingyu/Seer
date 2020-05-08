 /*
  * Copyright 2010, 2011 mapsforge.org
  *
  * This program is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.mapsforge.applications.debug.osmosis;
 
 import java.util.Stack;
 import java.util.Vector;
 
 import org.mapsforge.poi.PoiCategory;
 
 /**
  * A POI category representation that stores a node, its parent node and its child nodes.
  * 
  * @author Karsten Groll
  * 
  */
 public class DoubleLinkedPoiCategory implements PoiCategory {
 	private String title;
 	private PoiCategory parent;
 
 	private Vector<DoubleLinkedPoiCategory> childCategories;
 
 	// The categories id
 	private int id = -1;
 
 	DoubleLinkedPoiCategory(String title, PoiCategory parent) {
 		this.title = title;
 		this.parent = parent;
 
 		this.childCategories = new Vector<DoubleLinkedPoiCategory>();
 
 		if (parent != null) {
 			((DoubleLinkedPoiCategory) parent).addChildNode(this);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getTitle() {
 		return this.title;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public PoiCategory getParent() {
 		return this.parent;
 	}
 
 	/**
 	 * Adds a child node for this category.
 	 * 
 	 * @param categoryNode
 	 *            The node to be added.
 	 */
 	void addChildNode(DoubleLinkedPoiCategory categoryNode) {
 		this.childCategories.add(categoryNode);
 	}
 
 	/**
 	 * Make sure you call {@link #calculateCategoryIDs(DoubleLinkedPoiCategory, int)} first.
 	 * 
 	 * @return The node's ID.
 	 */
 	int getID() {
 		return this.id;
 	}
 
 	/**
 	 * Generates a GraphViz source representation as a tree having the current node as its root.
 	 * 
 	 * @param rootNode
 	 *            The resulting graph's root node. (You can use any sub node to get a sub-graph.)
 	 * @return a GraphViz source representation as a tree having the current node as its root.
 	 */
 	public static String getGraphVizString(DoubleLinkedPoiCategory rootNode) {
 		StringBuilder sb = new StringBuilder();
 		Stack<DoubleLinkedPoiCategory> stack = new Stack<DoubleLinkedPoiCategory>();
 		stack.push(rootNode);
 
 		DoubleLinkedPoiCategory currentNode = null;
 		sb.append("digraph Categories {\r\n");
 		while (!stack.isEmpty()) {
 			currentNode = stack.pop();
 			for (DoubleLinkedPoiCategory childNode : currentNode.childCategories) {
 				stack.push(childNode);
 				sb.append("\t\"" + currentNode.getTitle() + " (" + currentNode.getID() + ")" + "\" -> \"" + childNode.getTitle()
 						+ " (" + childNode.getID() + ")" + "\"\r\n");
 			}
 		}
 
 		sb.append("}\r\n");
 		return sb.toString();
 	}
 
 	/**
 	 * This method calculates an unique ID for all nodes in the tree. For each node's 'n' ID named
 	 * 'ID_'n at depth 'd' the following invariants must be true:
 	 * <ul>
 	 * <li>ID > max(ID of all child nodes)</li>
 	 * <li>All nodes' IDs left of n must be < ID_n.</li>
 	 * <li>All nodes' IDs right of n must be > ID_n.</li>
 	 * </ul>
 	 * 
 	 * @param rootNode
 	 *            The trees root node. (<strong>Any other node will result in invalid IDs!</strong>)
 	 * @param maxValue
 	 *            Global maximum ID.
 	 * @return The root node's ID.
 	 */
 	public static int calculateCategoryIDs(DoubleLinkedPoiCategory rootNode, int maxValue) {
 		int newMax = maxValue;
 		for (DoubleLinkedPoiCategory c : rootNode.childCategories) {
 			newMax = calculateCategoryIDs(c, newMax);
 		}
 
		rootNode.id = maxValue;
 
 		return newMax + 1;
 	}
 
 }
