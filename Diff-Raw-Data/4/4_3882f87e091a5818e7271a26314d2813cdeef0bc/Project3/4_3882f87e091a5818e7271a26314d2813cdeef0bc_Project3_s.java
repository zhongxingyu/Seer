 package ch5;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Create a non-recursive program that will traverse a binary tree in Preorder
  * or Postorder
  * 
  * Main calls an input procedure.
  * 
  * Input procedure: The tree should be input one node at a time from the
  * terminal and stored as an array. The input should be node index, int value.
  * Mirror the input
  * 
  * An input to the Main procedure would be to output the tree in Preorder or
  * Postorder.
  * 
  * Vist Procedure: "visit" should be to output the value in the node.
  * 
  * @author Haijun Su Created date: 2013-11-1
  */
 public class Project3 {
 
 	ArrayBinaryTree tree = null;
 
 	Map<Integer, Integer> visitStatus = new HashMap<Integer, Integer>();
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		Project3 proj = new Project3();
 		proj.buildTree();
 		System.out.println("Your input: " + proj.tree);
 		proj.preOrderVisit();
 		proj.postOrderVisit();
 		proj.inOrderVisit();
 
 	}
 
 	/**
 	 * Build tree from user's input
 	 */
 	private void buildTree() {
 		// input format
 		Pattern nodePattern = Pattern
 				.compile("(^(\\s*)(\\d+)(\\s*),(\\s*)(\\d+)(\\s*))|(^(\\s*)0(\\s*))");
 		// ask for tree height
 		System.out.print("Please input the heigh value of your tree: ");
 		while (true) {
 			Scanner scan = new Scanner(System.in);
 			int height = scan.nextInt();
 			if (height < 1) {
 				System.out
 						.println("The value is too small. Please try again: ");
 			} else {
 				tree = new ArrayBinaryTree(height);
 				break;
 			}
 		}
 		// ask for node information
 		while (true) {
 			try {
 				System.out
 						.println("Please input node information[index, value], [0 - finished]: ");
 				Scanner scan = new Scanner(System.in);
 				String line = scan.nextLine();
 				Matcher matcher = nodePattern.matcher(line);
 				if (!matcher.matches()) {
 					System.out
 							.println("Format error. Please check your input and try again.");
 					continue;
 				}
 				StringTokenizer st = new StringTokenizer(line, ",");
 				int index = 0;
 				int value = 0;
 				while (st.hasMoreTokens()) {
 					if (index == 0)
 						index = Integer.parseInt(st.nextToken().trim());
 					else
 						value = Integer.parseInt(st.nextToken().trim());
 				}
 				if (index == 0) {
 					break;
 				}
 				tree.addNode(index, value);
 			} catch (Exception e) {
 				System.out.println(e.getMessage()
 						+ ". Please check your input and try again.");
 			}
 		}
 
 	}
 
 	/**
 	 * Traverse a binary tree in Preorder
 	 */
 	private void preOrderVisit() {
 		System.out.println("PreOrderVisit tree nodes: ");
 		// clean visitStatus
 		visitStatus.clear();
 		boolean isFirst = true;
 		int nodeIndex = 1;
 		int counter = visitCounter(nodeIndex);
 		while (!(nodeIndex == 1 && counter == 2)) {
 			if (counter == 0) {
 				// visit node
 				if (isFirst) {
 					isFirst = false;
 				} else {
 					System.out.print(", ");
 				}
 				System.out.print(tree.getNodeValue(nodeIndex));
 				if (tree.hasLeft(nodeIndex)) {
 					// visit left node
 					nodeIndex = tree.left(nodeIndex);
 				}
 			} else if (counter == 1) {
 				// visit right node
 				if (tree.hasRright(nodeIndex)) {
 					nodeIndex = tree.right(nodeIndex);
 				}
 			} else {
 				// finished children, back to parent
 				nodeIndex = tree.getParent(nodeIndex);
 			}
 			counter = visitCounter(nodeIndex);
 		}
 		System.out.println();
 		visitStatus.clear();
 	}
 
 	/**
 	 * Traverse a binary tree in Postorder
 	 */
 	private void postOrderVisit() {
 		System.out.println("PostOrderVisit tree nodes: ");
 		// clean visitStatus
 		visitStatus.clear();
 		boolean isFirst = true;
 		int nodeIndex = 1;
 		int counter = visitCounter(nodeIndex);
 		// It prints the root value when counter is three.
 		while (!(nodeIndex == 1 && counter == 3)) {
 			if (counter == 0) {
 				if (tree.hasLeft(nodeIndex)) {
 					// visit left node
 					nodeIndex = tree.left(nodeIndex);
 				}
 			} else if (counter == 1) {
 				// visit right node
 				if (tree.hasRright(nodeIndex)) {
 					nodeIndex = tree.right(nodeIndex);
 				}
 			} else {
 				// visit node
 				if (isFirst) {
 					isFirst = false;
 				} else {
 					System.out.print(", ");
 				}
 				System.out.print(tree.getNodeValue(nodeIndex));
 				// finished children, back to parent
 				nodeIndex = tree.getParent(nodeIndex);
 			}
 			counter = visitCounter(nodeIndex);
 		}
 		System.out.println();
 		visitStatus.clear();
 	}
 
 	/**
 	 * Traverse a binary tree in Inorder
 	 */
 	private void inOrderVisit() {
 		System.out.println("InOrderVisit tree nodes: ");
 		// clean visitStatus
 		visitStatus.clear();
 		boolean isFirst = true;
 		int nodeIndex = 1;
 		int counter = visitCounter(nodeIndex);
 		while (!(nodeIndex == 1 && counter == 2)) {
 			if (counter == 0) {
 				if (tree.hasLeft(nodeIndex)) {
 					// visit left node
 					nodeIndex = tree.left(nodeIndex);
 				}
 			} else if (counter == 1) {
 				// visit node
 				if (isFirst) {
 					isFirst = false;
 				} else {
 					System.out.print(", ");
 				}
 				System.out.print(tree.getNodeValue(nodeIndex));
 				// visit right node
 				if (tree.hasRright(nodeIndex)) {
 					nodeIndex = tree.right(nodeIndex);
 				}
 			} else {
 				// finished children, back to parent
 				nodeIndex = tree.getParent(nodeIndex);
 			}
 			counter = visitCounter(nodeIndex);
 		}
 		System.out.println();
 		visitStatus.clear();
 	}
 
 	/**
 	 * 
 	 * @param nodeIndex
 	 * @return
 	 */
 	private int visitCounter(int nodeIndex) {
 		int counter = 0;
 		if (visitStatus.get(nodeIndex) != null) {
 			counter = visitStatus.get(nodeIndex);
 		}
 		// Increase counter visit status
 		visitStatus.put(nodeIndex, (counter + 1));
 		return counter;
 	}
 }
 
 /**
  * Binary tree representation by Array
  * 
  * @author Haijun Su Created date: 2013-11-1
  */
 class ArrayBinaryTree {
 
 	private Integer[] nodes = null;
 
 	int height = 0;
 
 	/**
 	 * Initial tree height
 	 * 
 	 * @param size
 	 */
 	public ArrayBinaryTree(int height) {
 		// position 0 is reserved by tree.
 		// position 1 is the root node
 		this.height = height;
		this.nodes = new Integer[(int) Math.pow(2.0d, (double) height)];
 	}
 
 	/**
 	 * Add node in the tree. Last input overrides previous value.
 	 * 
 	 * @param index
 	 *            node index
 	 * @param value
 	 *            node value
 	 * @throws Exception
 	 */
 	public void addNode(int index, int value) throws Exception {
 		if (index < 1)
 			throw new Exception("Node index value must is greater than one.");
 		if (index >= nodes.length)
 			throw new Exception("The node index is too big. Maximun value is "
 					+ (nodes.length - 1));
 		if (index == 1) {
 			nodes[index] = value;
 			return;
 		}
 		// Check if parent node exists
 		int parent = getParent(index);
 		if (nodes[parent] == null) {
 			throw new Exception(
 					"Please add parent node first! Parent node index is "
 							+ parent);
 		}
 		nodes[index] = value;
 	}
 
 	/**
 	 * Get the parent node index.Left node is index/2. Right node is (index-1)/2
 	 * 
 	 * @param index
 	 * @return parent node index
 	 */
 	public int getParent(int index) {
 		return (index % 2) == 0 ? (index / 2) : (index - 1) / 2;
 	}
 
 	/**
 	 * Get left node index
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public int left(int index) {
 		return 2 * index;
 	}
 
 	/**
 	 * Get right node index
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public int right(int index) {
 		return 2 * index + 1;
 	}
 
 	/**
 	 * Get node value
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public Integer getNodeValue(int index) {
 		return nodes[index];
 	}
 
 	/**
 	 * Check if left node exists
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public boolean hasLeft(int index) {
 		int leftIndex = left(index);
 		if (leftIndex >= nodes.length || getNodeValue(leftIndex) == null)
 			return false;
 		return true;
 	}
 
 	/**
 	 * Check if right node exists
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public boolean hasRright(int index) {
 		int rightIndex = right(index);
 		if (rightIndex >= nodes.length || getNodeValue(rightIndex) == null)
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		String strTree = "The BinaryTree:\n";
 		int index = 1;
 		for (int i = 0; i < height; i++) {
 			// row start offset, the first is 1
 			int offset = (int) Math.pow(2.0d, (double) i);
 
 			for (int j = offset; j < offset + (int) Math.pow(2.0d, (double) i); j++) {
 				strTree += index + "(" + (nodes[j] == null ? " " : nodes[j])
 						+ ")  ";
 				index++;
 			}
 			strTree += "\n";
 		}
 		return "ArrayBinaryTree [nodes=" + Arrays.toString(nodes) + "]\n"
 				+ strTree;
 	}
 
 }
