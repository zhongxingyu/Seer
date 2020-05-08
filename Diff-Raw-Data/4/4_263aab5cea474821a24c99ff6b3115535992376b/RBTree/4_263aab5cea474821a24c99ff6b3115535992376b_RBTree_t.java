 /**
  * RBTree.java:<p>
  * A simple tree to hold recycling data.
  *   Stores children in an array for O(1) lookup.
  *   Includes link to parent for reverse traversal.
  * 
  * University of Washington, Bothell
  * CSS 360
  * Spring 2011
  * Professor: Valentin Razmov
  * Recycle Buddy Group
  *
  * @author Niko Simonson
  * @since 5/3/11
  * @latest 5/11/11
  * @version 0.0.04
  * 5/3/11 0.0.01 - Created tree structure.
  * 5/5/11 0.0.02 - added build(), full comments, and childNum
  * 5/8/11 0.0.03 - changed version scheme, added some build() functionality
  * 5/11/11 0.0.04 - build tree with dummy data
  */
 
 package recycleBuddy;
 
 import java.io.*; // streaming data
 
 
 public class RBTree {
 
 	/**
 	* RBTree Constructor
 	*
 	* Creates an empty tree with a parent and an empty node.
 	* 
 	* @param reference to parent (can be null, but must be explicitly passed)
 	*/
 	public RBTree(RBTree caller, int childNumber) {
 		// Set parent tree.
 		parent = caller;
 		
 		// Create an empty node.
 		thisNode = new RBTreeNode();
 		
 		// Assign child number
 		childNum = childNumber;
 	}
 	
 	/**
 	* build
 	*
 	* Builds a full data tree based on external data.
 	* 
 	* @param path for file stream
 	* 
 	* @postcondition A full tree of recycling data is constructed.
 	*/
 	public void build(String dataPath) throws FileNotFoundException {		
 		try {
 			// Create dummy data
 			thisNode.setImagePath("plasticBin.jpg");
 			thisNode.setText("This is the top");
 			thisNode.setTitle("root");
 			
 			children = new RBTree[6];
 			
			for(int i = 0; i < children.length; i++) {
				children[i] = new RBTree(this, i);
			}
			
 			RBTreeNode childNode = children[0].getThisNode();
 			
 			childNode.setImagePath("child.jpg");
 			childNode.setText("first child");
 			childNode.setTitle("1st");
 			
 			childNode = children[1].getThisNode();
 			
 			childNode.setImagePath("child.jpg");
 			childNode.setText("second child");
 			childNode.setTitle("2nd");
 			
 
 			childNode = children[2].getThisNode();
 			
 			childNode.setImagePath("child.jpg");
 			childNode.setText("further children");
 			childNode.setTitle("more");
 			
 
 			childNode = children[3].getThisNode();
 			
 			childNode.setImagePath("child.jpg");
 			childNode.setText("further children");
 			childNode.setTitle("more");
 			
 			childNode = children[4].getThisNode();
 			
 			childNode.setImagePath("child.jpg");
 			childNode.setText("further children");
 			childNode.setTitle("more");
 			
 			childNode = children[5].getThisNode();
 			
 			childNode.setImagePath("child.jpg");
 			childNode.setText("further children");
 			childNode.setTitle("more");
 			
 			
 			
 			/* Remove all data reading 
 			// open the initial file
 			FileReader recycleFile = new FileReader(dataPath);
 			
 			// place in buffered reader for easier parsing
 			BufferedReader recycleData = new BufferedReader(recycleFile);
 			
 			String[] readData = new String[4];
 			
 			// close the initial file
 			
 			// for each file to be read
 			//   open the file
 			//   read the data to:
 			
 			//discard self-identification
 			for (int x = 0; x < 6; ++x) {
 				if (recycleData.ready()) {
 					recycleData.readLine();
 				}				
 			}
 			
 			
 			for (int i = 0; i < 4; ++i) {
 				if (recycleData.ready()) {
 					readData[i] = recycleData.readLine();
 				}
 			}
 			// fill this tree's node			
 			thisNode.setTitle(readData[0]);
 			thisNode.setText(readData[1]);
 			thisNode.setImagePath(readData[2]);
 			childNum = Integer.parseInt(readData[3]);
 				
 			// lengthwise or breadthwise recursion here?
 			
 			// create children trees
 			
 			// base case
 			if (0 == childNum) {
 				recycleData.readLine();
 				return;
 			}
 			else {
 				//children = new RBTree[childNum];
 				
 				for (int offspring = 0; offspring < childNum; ++offspring) {
 					children[offspring] = new RBTree(this, offspring);
 					children[offspring].build(recycleData);
 				}
 			}
 
 			
 			//     return to parent if there are no more children to create
 			//       and parent is not null
 			//     stop when there are no more children to create and parent is null
 			//   close the file
 			recycleData.close();
 			
 			// Is this command necessary?
 			recycleFile.close();
 			*/
 			
 			
 		}
 		catch (Exception e) {
 			
 		}
 		
 		
 		
 
 	} // end build
 	
 	
 	
 	/**
 	* buildHelper 
 	*
 	* Private helper class.
 	* Builds a full data tree based on external data.
 	* 
 	* @param path for file stream
 	* 
 	* @postcondition A full tree of recycling data is constructed.
 	*/
 	private void buildHelper(BufferedReader recycleData) throws FileNotFoundException {		
 		try {			
 			String[] readData = new String[4];
 			
 			// close the initial file
 			
 			// for each file to be read
 			//   open the file
 			//   read the data to:
 			
 			//discard self-identification
 			for (int x = 0; x < 6; ++x) {
 				if (recycleData.ready()) {
 					recycleData.readLine();
 				}				
 			}
 			
 			
 			for (int i = 0; i < 4; ++i) {
 				if (recycleData.ready()) {
 					readData[i] = recycleData.readLine();
 				}
 			}
 			// fill this tree's node			
 			thisNode.setTitle(readData[0]);
 			thisNode.setText(readData[1]);
 			thisNode.setImagePath(readData[2]);
 			childNum = Integer.parseInt(readData[3]);
 				
 			// lengthwise or breadthwise recursion here?
 			
 			// create children trees
 			
 			// base case
 			if (0 == childNum) {
 				recycleData.readLine();
 				return;
 			}
 			else {
 				//children = new RBTree[childNum];
 				
 				for (int offspring = 0; offspring < childNum; ++offspring) {
 					children[offspring] = new RBTree(this, offspring);
 					children[offspring].buildHelper(recycleData);
 				}
 			}
 		}
 		catch (Exception e) {
 			
 		}
 	} // end overloaded build
 	
 	
 	
 	// ACCESSORS
 	// Straightforward code; comment later.
 	public RBTreeNode getThisNode() {
 		return thisNode;
 	}
 	
 	public RBTree getParent() {
 		return parent;
 	}
 	
 	public RBTree getChild(int whichChild) {
 		// if child exists at index
 		if (null != children[whichChild]) {
 			return children[whichChild];
 		}
 		else {
 			return null;
 		}
 	}
 	
 	public int getChildNum() {
 		return childNum;
 	}
 	
 	// PRIVATE MEMBERS		
 	private RBTreeNode thisNode; // data held by this tree
 	private RBTree[] children; // child trees
 	private RBTree parent; // parent tree
 	private int childNum; // which child is this tree
 } // end class
