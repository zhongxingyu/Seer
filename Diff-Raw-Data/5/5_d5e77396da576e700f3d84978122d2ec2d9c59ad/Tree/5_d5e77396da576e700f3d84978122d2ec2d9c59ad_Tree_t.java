 package util;
 
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.math.BigInteger;
 
 /**
  * A simple tree.
  */
 public class Tree {
 	public Object Value;
 	public Tree Left;
 	public Tree Right;
 	
 	/**
 	 * Create an empty tree.
 	 */
 	public Tree() {
 	}
 	
 	/**
 	 * Create a leaf (no subtrees).
 	 * @param value The leaf's value.
 	 */
 	public Tree(Object value) {
 		Value = value;
 		Left = new Tree();
 		Right = new Tree();
 	}
 	
 	/**
 	 * Create a new leaf with possible subtrees.
 	 * @param value The leaf's value.
 	 * @param left Left subtree.
 	 * @param right Right subtree.
 	 */
 	public Tree(Object value, Tree left, Tree right) {
 		Value = value;
 		Left = left;
 		Right = right;
 	}
 	
 	/**
 	 * Convert a tree to a string.
 	 */
 	public String toString() {
 		if (Value == null) return "[empty-tree]";
		else if (isLeaf()) return "[leaf " + Value + "]";
 		else return "[tree " + Value + " " + Left + " " + Right + "]";
 	}
 	
 	/**
 	 * Draw a tree.
 	 * @return An image containing the tree.
 	 */
 	public void drawTree(Graphics2D g, int width, int height) {
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, width, height);
 		
 		g.setColor(Color.BLACK);
 		int h = height();
 		int w = new BigInteger("2").pow(h).intValue();
 		
 		draw(g, width / 2, height / (h + 1), width / (w + 1), width - width / (w + 1),  height / (h + 1));
 	}
 	
 	/**
 	 * Check if this tree is empty.
 	 * @return True if empty.
 	 */
 	public boolean isEmpty() {
 		return Value == null;
 	}
 	
 	/**
 	 * Check if this tree is a leaf.
 	 * @return True if a leaf.
 	 */
 	public boolean isLeaf() {
		return (Left.Value == null && Right.Value == null);
 	}
 	
 	/**
 	 * Draw this node.
 	 * 
 	 * @param g The graphics object to draw the tree with.
 	 * @param x The x location to center the node on.
 	 * @param y The y location to center the node on.
 	 * @param left The left side of that sub tree.
 	 * @param right The right side of that sub tree.
 	 * @param skip The amount to skip vertically per branch.
 	 */
 	private void draw(Graphics2D g, int x, int y, int left, int right, int skip) {
 		if (Value == null) return;
 		
 		// Calculate a weighted midpoint so that off balance trees will draw pleasantly.
 		int leftSize = 1 + Left.size();
 		int rightSize = 1 + Right.size();
 		double leftWeight = (double) leftSize / ((double) leftSize + (double) rightSize);
 		int mid = left + (int) ((double) (right - left) * leftWeight);
 		
 		// Calculate the view points for the left and right subtrees (yes, even if they won't be drawn)
 		int leftX = (left + mid) / 2;
 		int rightX = (mid + right) / 2;
 		
 		// Left subtree.
 		if (Left.Value != null) {
 			g.drawLine(x, y, leftX, y + skip);
 			Left.draw(g, leftX, y + skip, left, mid, skip);
 		}
 		
 		// Right subtree.
 		if (Right.Value != null) {
 			g.drawLine(x, y, rightX, y + skip);
 			Right.draw(g, rightX, y + skip, mid, right, skip);
 		}
 				
 		// My node.
 		String s = Value.toString();
 		FontMetrics fm = g.getFontMetrics();
 		int w = fm.stringWidth(s);
 		
 		g.setColor(Color.WHITE);
 		g.fillOval(x - w / 2 - 10, y - 15, w + 20, 30);
 		g.setColor(Color.BLACK);
 		g.drawOval(x - w / 2 - 10, y - 15, w + 20, 30);
 		
 		g.drawString(Value.toString(), x - fm.stringWidth(s) / 2, y + fm.getAscent() / 2);
 	}
 	
 	/**
 	 * Calculate the height of the tree (empty tree is 0, leaf is 1).
 	 * @return The height of the tree.
 	 */
 	private int height() {
 		if (Value == null) return 0;
 		else return 1 + Math.max(Left.height(), Right.height());
 	}
 	
 	/**
 	 * Count this node plus all its children.
 	 * @return The number of nodes.
 	 */
 	private int size() {
 		if (Value == null) return 0;
 		else return 1 + Left.size() + Right.size();
 	}
 }
