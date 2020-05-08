 
 package backend.main;
 
 import java.util.List;
 
 import matrixDraw.MatrixDraw;
 import backend.blocks.Countable;
 import backend.blocks.Matrix;
 import backend.blocks.Scalar;
 import backend.computations.infrastructure.Solution;
 
 /** A tree of ParseNodes is returned to the front-end after a computation. This allows the front-end to understand the order 
  *  of operations. Less preferential operations like "+" or "-" will generally appear at the top of the tree. Basically, a current node's
  *  children must contain solutions that were computed before the solution contained in the current node.  
  * 
  * @author baebi
  */
 public class ParseNode {
 	private Solution _solution; // The Solution of this ParseNode
 	private ParseNode _left;    // contains the Solution to the 1st argument to the operation that created _solution
 	private ParseNode _right;   // contains the Solution to the 2nd argument to the operation that created _solution
 	private String _toCompute;  // the latex string detailing which part of the equation is being computed at this step
 	private ToComputeTreeNode _toComputeTree; // the tree that details how to construct <_toCompute>  
 	// Note, if the 1st or 2nd arguments to the operation that created _solution were not themselves computed in
 	// a Computable, the respective _left or _right will be null
 	
 	
 	/** 
 	 * Constructor for a ParseNode, a container for a Solution object
 	 * 
 	 * @param solution the Solution of a single operation
 	 * @param arg1 the ParseNode that contains the Solution which was used as the first argument to compute <solution>
 	 * @param arg2 the ParseNode that contains the Solution which was used as the second argument to compute <solution>
 	 */
 	public ParseNode(Solution solution,ParseNode arg1, ParseNode arg2){
 		_solution = solution;
 		_left = arg1;
 		_right = arg2;
 	}
 	
 	
 	/**
 	 * Generates and returns the string that details the current state of the equation at this ParseNode's position in the overall computation
 	 * 
 	 * @return a latex string
 	 * @throws Exception setComputeStringTree must be called before getComputeString
 	 */
 	public String getComputeString() throws Exception{
 		if (_toComputeTree == null){
 			throw new Exception("ERROR (ParseNode.java): ComputeString Tree must be set before getComputeString() call");
 		}
 		_toCompute = getComputeStringHelper(_toComputeTree);
 		return _toCompute;
 	}
 	
 	
 	/**
 	 * Recursive Helper to getComputeString()
 	 * 
 	 * @param root the current ToComputeTreeNode node in the String tree
 	 * @return the String representing the equation rooted at <root>
 	 */
 	private String getComputeStringHelper(ToComputeTreeNode root){
 		StringBuilder b = new StringBuilder();
 		if (root.getLeft() != null){
 			b.append(getComputeStringHelper(root.getLeft()));
 		}
 		b.append(root.getValue());
 		if (root.getRight() != null){
 			b.append(getComputeStringHelper(root.getRight()));
 		}
 		return b.toString();
 	}
 	
 	/**
 	 * Method used only for testing
 	 * 
 	 * @param root the root of the ToComputeTreeNode String tree
 	 */
 	protected void setComputeStringTreeNonRecursive(ToComputeTreeNode root){
 		_toComputeTree = root;
 	}
 	
 	private ToComputeTreeNode copyTree(ToComputeTreeNode root){
 		if(root == null){
 			return null;
 		}
 		ToComputeTreeNode left = copyTree(root.getLeft());
 		ToComputeTreeNode right = copyTree(root.getRight());
 		return new ToComputeTreeNode(left,right,root.getValue());
 		
 	}
 	
 	/**
 	 * 
 	 * @param root the root of 
 	 * @param toReplace this should be a node on the tree rooted at <root>
 	 * @return the node in the tree rooted at <root> that should be expanded next
 	 */
 	public void setComputeStringTree(ToComputeTreeNode root, ToComputeTreeNode toReplace){
 		if (toReplace == null){
 			return;
 		}
 		
 		toReplace.setValue(_solution.getOp().getString());
 		List<Countable> args = _solution.getInputs();
 		
 		String toSetArg1 = getToSet(args.get(0));
 		
 		if (args.size() == 2){ // was it a unary or binary operator?
 			String toSetArg2 = getToSet(args.get(1));
			toReplace.setLeft(new ToComputeTreeNode(null,null,"{("+toSetArg1));
			toReplace.setRight(new ToComputeTreeNode(null,null,toSetArg2+")}"));
 			_toComputeTree = copyTree(root);
			toReplace.setLeft(new ToComputeTreeNode(null,null,"{"+toSetArg1 ));
			toReplace.setRight(new ToComputeTreeNode(null,null,toSetArg2+"}"));
 		}else{
 			toReplace.setValue("("+_solution.getOp().getString());
 			toReplace.setLeft(null);
 			toReplace.setRight(new ToComputeTreeNode(null,null,toSetArg1+")"));
 			_toComputeTree = copyTree(root);
 			toReplace.setValue(_solution.getOp().getString());
 			toReplace.setRight(new ToComputeTreeNode(null,null,toSetArg1));
 		}
 
 	}
 	
 	
 	/**
 	 * Gets the proper latex string for a given countable
 	 * 
 	 * @param c a Countable
 	 * @return the latex string for the <c>
 	 */
 	private String getToSet(Countable c){
 		if (c instanceof Matrix){
 			return  MatrixDraw.getCorrectLatex(_solution.getDisplayType(),(Matrix) c);
 		}else{
 			return ((Scalar) c).getDisplayValue();
 		}
 	}
 	
 	
 	/**
 	 * @return the solution in this ParseNode
 	 */
 	public Solution getSolution(){
 		return _solution;
 	}
 	
 	
 	/**
 	 * @return the latex string detailing which part of the equation is being computed at this step
 	 */
 	public String getToCompute(){
 		return _toCompute;
 	}
 	
 	
 	/**
 	 * @return the tree detailing how to create _toCompute
 	 */
 	public ToComputeTreeNode getToComputeTree(){
 		return _toComputeTree;
 	}
 	
 	
 	/**
 	 * @return the solution comprising the first argument to the solution in this ParseNode
 	 */
 	public ParseNode getLeft(){
 		return _left;
 	}
 	
 	
 	/**
 	 * @return the solution comprising the second argument to the solution in this ParseNode
 	 */
 	public ParseNode getRight(){
 		return _right;
 	}
 	
 	/**
 	 * @return whether the node is a leaf node
 	 */
 	public boolean isLeaf(){
 		if(_left == null && _right == null){
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 }
