 package com.cise.ufl.rpal;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Stack;
 
 class TreeNode {
 	String token;
 	
 	private  TreeNode leftChild = null;
 	private TreeNode rightChild = null;
 	
 	TreeNode (String token) {
 		this.token = token;
 	}
 	
 	public void setLeftChild (TreeNode t) {
 		this.leftChild = t;
 	}
 	
 	public void setRightChild (TreeNode t) {
 		this.rightChild = t;
 	}
 	
 	public TreeNode getLeftChild () {
 		return this.leftChild;
 	}
 	
 	public TreeNode getRightChild () {
 		return this.rightChild;
 	}
 	
 	public String getTokenValue () {
 		return this.token;
 	}
 	
 }
 
 public class Parser {
     Lexer lexer = new Lexer ();
    // HashMap lexTable;
     private static String nextToken;
     private static int index = -1;
     
     private static final String ID = "ID:";
     private static final String STR = "STR:";
     private static final String INT = "INT:";
     
     Stack stack = new Stack ();
     ArrayList tokenList;
     static String [] reserved = {"let", "in", "fn", "where", "aug", "within", "and", "eq"};
     static ArrayList reservedTokens = new ArrayList ();
     
     private TreeNode rootTreeNode;
     
     
     static {
     	for (String s: reserved ) {
     		reservedTokens.add(s);
     	}
     }
     /**
      * Constructor for the Parser
      * 
      * @param fileName
      */
     public Parser (String fileName) {
     	 lexer.readFile (fileName);
     	 lexer.constructTokens();
     	 tokenList = lexer.getTokens();
     	// lexTable = lexer.getLexTable(); 
     	 nextToken = getNextToken();
     }
     
    public Parser () {
 	   System.out.println ("Default Constructor");
    }
     
     
     /**
      * Utility to read the tokens and to identify if it's the correct one.
      * 
      * @param token
      * @throws Exception
      */
     public void readToken (String token) throws Exception {
     	System.out.println ("READING Current TOKEN: " + token + " Next Token: " + nextToken);
     	if ( ! token.equalsIgnoreCase (nextToken)) {
     		throw new Exception ("Error: Expected "+ token + " but found: "+ nextToken);
     	}
     	String type = lexer.getTypeOfToken(token);
     	if ((type.equalsIgnoreCase("Identifier") ||  type.equalsIgnoreCase("Integer") || type.equalsIgnoreCase("String")) && !token.equals("in") &&!token.equals("eq")
     	&& !token.equals("rec") && !token.equals ("where") && !token.equals ("let") && !token.equals("within") &&!token.equals("and") &&!token.equals("fn") &&!token.equals (",")
     		&&!token.equals (".") && !token.equals ("le") && !token.equals ("gr") && !token.equals ("ge") && !token.equals ("ls") && !token.equals("or") && !token.equals("not")
     		&& !token.equals("aug") && !token.equals ("nil"))
     	{
     		System.out.println ("Building " + token + " with 0 children");
     		if (token.equals ("nil")) {
    			token = "nil";
     		}
     		else if (lexer.getTypeOfToken(token).equals("String")) {
     			token = "<"+STR+token+">";
     		}	
     		else if (lexer.getTypeOfToken(token).equals("Integer")) {
     			token = "<"+INT+token+">";
     		}
     		else if (lexer.getTypeOfToken(token).equals("Identifier")) {
     			token = "<"+ID+token+">";
     		}
     		else {
     			token = token;
     		}
     		Build_tree (token, 0);
     	}
     	nextToken = getNextToken ();
     	System.out.println ("NEXT TOKEN: " + nextToken);
     }
 
     /**
      * Utility to build the tree. It uses a first child - next sibling approach
      * 
      * @param token
      * @param n
      */
 	private void Build_tree(String token, int n) {
 		TreeNode treeNode = new TreeNode (token);
 		ArrayList <TreeNode>treeNodesList = new ArrayList<TreeNode>  ();
 		TreeNode tempNode, lastNode, lastButOneNode;
 		if (n == 0) {  // Just push the tree to the stack
 			stack.push(treeNode);
 		}
 		else {   // We will pop 'i' trees from the stack, connect it to-gether like a first child next sibling and then push the resulting one to the stack again...
 			for (int i=0; i<n ; i++) {
 				treeNodesList .add((TreeNode) stack.pop());
 			}
 			Collections.reverse(treeNodesList);
 		  
 					
 			//System.out.println (treeNodesList);
 			for (int i=0; i<treeNodesList.size()-1; i++) {
 				//   TreeNode temp = treeNodesList.get(i);
 				    System.out.println ("Setting right child of  "+ treeNodesList.get(i).getTokenValue() + " to be " + treeNodesList.get(i+1).getTokenValue());
 				    treeNodesList.get(i).setRightChild(treeNodesList.get(i+1)); // ? shouldnt we update the tree node ??
 				    
 			}
 			 System.out.println ("Setting Left child of "+ treeNode.getTokenValue()+" to be " + treeNodesList.get(0).getTokenValue());
 			treeNode.setLeftChild(treeNodesList.get(0));
 			
 			
 			
 			stack.push(treeNode);
 			
 			
 		}
 		
 	}
 	
 	/**\
 	 * Tester for Build tree
 	 * 
 	 */
 	public void testBuild_tree () {
 		Build_tree ("let", 0);
 		Build_tree("where", 0);
 		Build_tree("then", 2);
 		preOrderTraversal();
 	}
 	
 	private void preOrderTraversal () {
 		int depth =0 ;
 		int noLeft = 0;
 		
 		if (stack.empty()) {
 			System.out.println ("No trees in the stack!");
 		}
 		TreeNode root = (TreeNode) stack.pop();
 		TreeNode temp = root;
 		this.rootTreeNode = temp;
 		preOrder (root, depth);
 	}
 	
 	private void preOrder (TreeNode t, int depth) {
 		
 		String dot="";
 		for(int i=0;i<depth;i++)
 		{
 			dot=dot+".";
 		}
 		System.out.println (dot + t.getTokenValue());
 		depth++;
 		if  (t.getLeftChild() != null ) {
 		    preOrder (t.getLeftChild(), depth);
 		}
 	    if (t.getRightChild() != null) {  
 		    preOrder (t.getRightChild(),depth-1);
 	    }
 	}
 	
 	/*private int height(TreeNode root)
 	{
         int ldepth=0;
         int rdepth=0;
 		if(root.getTokenValue() == this.rootTreeNode.getTokenValue())
 	        return 0;
 	    ldepth = height(root.getLeftChild()) + 1;
 	    rdepth = height(root.getRightChild()) + 1;
 
 	    if(ldepth > rdepth)
 	        return ldepth;
 	    else
 	        return rdepth;
 	}*/
 
 	/**
 	 * 
 	 * Utility to get the next token from the input. Or rather the lexer table. This updates the nextToken, which is always one token ahead
 	 * @return
 	 */
 	private String getNextToken() {
 		index++;
 		if (index == tokenList.size())  {
 			return "PARSE_COMPLETE";
 		}
 		return (String) tokenList.get(index);
 	}
 	
 	private boolean isReserved (String token) {
 		//System.out.println ("Check for " + token);
 		if (reservedTokens.contains(token)) {
 			//System.out.println ("Came here true " + token);
 			return true;
 		}
 		else {
 			//System.out.println ("Came here false" + token);
 			return false;
 		}
 	}
 	
 	/************************************************************
 	 * Procedures for the various Non terminals...
 	 * 
 	 ************************************************************/
 	
 	public void fn_E () throws Exception {
 		System.out.println ("In Fn E" );
 		if (nextToken.equalsIgnoreCase("let")) {
 		
 				readToken("let");
 				fn_D ();
 				readToken("in");
 				fn_E ();
 				System.out.println ("Building tree with Let node and 2 children");
 				Build_tree("let", 2);
 		/*		if (nextToken.equalsIgnoreCase("PARSE_COMPLETE")) {
 					System.out.println ("*******PARSING COMPLETE*****");
 					System.out.println ("-------TREE VALUES----------");
 					preOrderTraversal ();
 					System.exit(0);
 				}*/
 		}
 		else if (nextToken.equalsIgnoreCase("fn")) {
 		
 				readToken ("fn");
 				int n = 0;
 				do {
 					fn_Vb();
 					n++;
 					} while (lexer.getTypeOfToken(nextToken).equals ("Identifier") || lexer.getTypeOfToken(nextToken).equals ("(") );
 				readToken (".");
 				fn_E ();
 				System.out.println ("Building tree with Lambda and "+(n+1)+" children");
 				Build_tree ("lambda", n+1);
 		/*		if (nextToken.equalsIgnoreCase("PARSE_COMPLETE")) {
 					System.out.println ("*******PARSING COMPLETE*****");
 					System.out.println ("-------TREE VALUES----------");
 					preOrderTraversal ();
 					System.exit(0);
 				}*/
 				
 			}
 		else {
 			fn_Ew ();
 		    }
 	}
 
 
 	private void fn_Ew() throws Exception {
 		System.out.println ("In Fn Ew" );
 		fn_T ();
 		if (nextToken.equalsIgnoreCase ("where")) {
 			readToken ("where");		
 			fn_Dr();
 			Build_tree("where", 2);
 			System.out.println ("Building tree with Where node and 2 children");
 		}
 		
 	}
 
 
 	private void fn_Dr() throws Exception {
 		System.out.println ("In Fn Dr" );
 		if (nextToken.equalsIgnoreCase ("rec")) {
 				readToken ("rec");
 				fn_Db();
 				Build_tree("rec", 1);
 				System.out.println ("Building tree with rec node and 1 children");
 		}
 		else {
 		    fn_Db();
 		}
 		
 	}
 
 
 	private void fn_Db() throws Exception {
 		System.out.println ("In Fn Db" );
 		
 		if (lexer.getTypeOfToken(nextToken).equalsIgnoreCase("(")) {
 			
 				readToken ("(");
 				fn_D ();
 				readToken (")");
 		
 		
 		}	
 		
 		else { 
 			if ( lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Identifier")) {
         	int n = 1;
         	fn_V1();
         	if (nextToken.equals ("=")) {
         		readToken ("=");
         		fn_E ();
         		System.out.println ("Building tree with = node and 2 children");
         		 Build_tree("=", 2);
         	}
         	else {
         	
         	do {
 				fn_Vb();
 				n++;
 				System.out.println ("N Value " + n);
 				} while ( (!nextToken.equals("=")) && lexer.getTypeOfToken(nextToken).equals ("Identifier") || lexer.getTypeOfToken(nextToken).equals ("(") );
    			readToken ("=");
 			fn_E ();
 			System.out.println ("Building fcn_form with " + (n+1));
 			Build_tree("function_form", (n+1));
         	}
         }
 	}
 	/*	else {
 		    //readToken (nextToken);
 			fn_V1();
 		    readToken ("=");
 		    fn_E ();
 		    System.out.println ("Building tree with = node and 2 children");
 		    Build_tree("=", 2);
    		}*/
    	}
 
 
 	private void fn_T() throws Exception {
 		System.out.println ("In Fn T" );
 		fn_Ta ();
 		//if (lexer.getTypeOfToken(nextToken).equals ("(")) {		
 		if (lexer.getTypeOfToken(nextToken).equals (",")) {	
 				int n = 0;
 			do {
 				//readToken ("(") ;
 				readToken (",") ;
 				fn_Ta();
 				//readToken (")") ;
 				n++;
 			} //while (lexer.getTypeOfToken(nextToken).equals ("(") );
 			while (lexer.getTypeOfToken(nextToken).equals (",") );
 			System.out.println ("Building tree with Tau node and "+(n+1)+ " children");
 			Build_tree("tau", n+1);
 		}
 	}
 
 
 	private void fn_Ta() throws Exception {
 		System.out.println ("In Fn Ta" );
 		fn_Tc ();
 		
 		if (nextToken.equalsIgnoreCase ("aug")) {
 		    while (nextToken.equalsIgnoreCase ("aug")) {
 			    readToken ("aug");
 			    fn_Tc();
 			    System.out.println ("Building tree with aug node and 2 children");
 			    Build_tree ("aug", 2);
 			    
 		    }
 		}
 	}
 
 
 	private void fn_Tc() throws Exception {
 		System.out.println ("In Fn Tc" );
 		fn_B ();
 		if (nextToken.equalsIgnoreCase("->")) {		
 			readToken ("->");
 			fn_Tc();
 			readToken ("|");
 		    fn_Tc();
 		    System.out.println ("Building tree with -> node and 3 children");
 		    Build_tree ("->", 3);
 		}
  	}
 
 
 	private void fn_B() throws Exception {
 		System.out.println ("In Fn B" );
         fn_Bt ();
         if (nextToken.equalsIgnoreCase("or")) {
         	while(nextToken.equalsIgnoreCase("or")) {
         	    readToken ("or");
         	    fn_Bt ();
         	    System.out.println ("Building tree with OR node and 2 children");
         	    Build_tree ("or", 2);
         	}
         }
 	}
 
 
 	private void fn_Bt() throws Exception {
 		System.out.println ("In Fn Bt" );
 	        fn_Bs ();
 	        if (nextToken.equalsIgnoreCase("&")) {
 	        	while(nextToken.equalsIgnoreCase("&")) {
 	        	    readToken ("&");
 	        	    fn_Bs ();
 	        	    System.out.println ("Building tree with & node and 2 children");
 	        	    Build_tree ("&", 2);
 	        	}
 	        } 
 	}
 
 
 	private void fn_Bs() throws Exception {
 		System.out.println ("In Fn Bs" );
 		if (nextToken.equalsIgnoreCase("not")) {
 			readToken ("not");
 			fn_Bp ();
 			Build_tree ("not", 1);
 			System.out.println ("Building tree with NOT node and 1 children");
 		}
 		else {
 			fn_Bp();
 		}
 		}
 
 
 	private void fn_Bp() throws Exception {
 		System.out.println ("In Fn Bp" );
 		
 		fn_A ();
 		if (nextToken.equalsIgnoreCase("eq")) {
 			readToken ("eq");
 			fn_A ();
 			Build_tree ("eq", 2);
 			System.out.println ("Building tree with eq node and 2 children");
 		}
 		else if (nextToken.equalsIgnoreCase("ne")) {
 			readToken ("ne");
 			fn_A ();
 			Build_tree ("ne", 2);	
 			System.out.println ("Building tree with NE node and 2 children");
 		}
 		
 		else  {
 	
 			String temp = nextToken;
 			if (temp.equalsIgnoreCase("gr") || temp.equalsIgnoreCase(">")) {
 				 readToken (temp);
 				 fn_A ();
 				 Build_tree("gr", 2);
 				 System.out.println ("Building tree with GR node and 2 children");
 			}
 			else if (temp.equalsIgnoreCase("ge") || temp.equalsIgnoreCase(">=")) {
 				 readToken (temp);
 				 fn_A ();
 				 Build_tree("ge", 2);
 				 System.out.println ("Building tree with GE node and 2 children");
 			}
 			else if (temp.equalsIgnoreCase("ls") || temp.equalsIgnoreCase("<")) {
 				 readToken (temp);
 				 fn_A ();
 				 Build_tree("ls", 2);
 				 System.out.println ("Building tree with LS node and 2 children");
 			}
 			else if (temp.equalsIgnoreCase("le") || temp.equalsIgnoreCase(">")) {
 				 readToken (temp);
 				 fn_A ();
 				 Build_tree("le", 2);
 				 System.out.println ("Building tree with LE node and 2 children");
 			}
 		}
 	}
 
 
 	private void fn_A() throws Exception {
 		System.out.println ("In Fn A" );
 		if (nextToken.equalsIgnoreCase("+")) {
 			readToken ("+");
 			fn_At ();
 		}
 		else if (nextToken.equalsIgnoreCase("-")) {
 			readToken ("-");
 			fn_At ();
 			Build_tree ("neg", 1);
 			System.out.println ("Building tree with NEG node and 1 children");
 		}
 		else {
 		    fn_At ();
 		}
 		while (nextToken.equalsIgnoreCase("+") || nextToken.equalsIgnoreCase("-")  ) {
 			if (nextToken.equalsIgnoreCase("+")) {
 				readToken ("+");
 				fn_At ();
 				Build_tree ("+", 2);
 				System.out.println ("Building tree with + node and 2 children");
 			}
 			else {
 				readToken ("-");
 				fn_At ();
 				Build_tree ("-", 2);
 				System.out.println ("Building tree with - node and 2 children");
 			}
 		}
 	}
 
 
 	
 
 
 	private void fn_At() throws Exception {
 		System.out.println ("In Fn At" );
 		fn_Af();
 		while (nextToken.equalsIgnoreCase("*") || nextToken.equalsIgnoreCase("/")  ) {
 			if (nextToken.equalsIgnoreCase("*")) {
 				readToken ("*");
 				fn_Af();
 				System.out.println ("Building tree with * node and 2 children");
 				Build_tree ("*", 2);
 			}
 			else {
 				readToken ("/");
 				fn_Af ();
 				System.out.println ("Building tree with / node and 2 children");
 				Build_tree ("/", 2);
 			}
 	
 		}
 	
 		
 	}
 
 	private void fn_Af() throws Exception {
 		System.out.println ("In Fn Af" );
 		fn_Ap();
 		if (nextToken.equalsIgnoreCase("**")) {
 			readToken ("**");
 			fn_Af();
 			System.out.println ("Building tree with ** node and 2 children");
 			Build_tree ("**", 2);
 		}
 	
 	}
 
 	private void fn_Ap() throws Exception {
 		System.out.println ("In Fn Ap" );
 	    fn_R();
 	    if (nextToken.equalsIgnoreCase("@")) {
 	        while (nextToken.equalsIgnoreCase("@")) {
 	    	    readToken ("@");
 	    	    readToken (nextToken);
 	    	    fn_R();
 	    	    System.out.println ("Building tree with @ node and 3 children");
 	    	    Build_tree ("@", 3);
 	        }
 	    }
 	}
 
 /*	private void fn_R() throws Exception {
 		
 	    //fn_Rn ();
 		int n=0;
         boolean status = false;
 		while ( (!isReserved(nextToken) && !lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Arrow") && !nextToken.equals("|")) && !lexer.getTypeOfToken (nextToken).equalsIgnoreCase("Operator_symbol")
 				&& ! lexer.getTypeOfToken (nextToken).equalsIgnoreCase(")") &&
 				(lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Integer") ||
 			 lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("Identifier") || lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("true") 
 		       || lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("false") || lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("nil") || 
 		       lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("(") || lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("dummy")))
 		    {
 			
 			System.out.println ("In Fn R "+  nextToken );
 			fn_Rn ();	
 		
 			n++;
 			
 			if (status) {
 				status=false;
 				System.out.println ("Building tree with Gamma node for the last non-terminal and 2 children");
 				Build_tree ("gamma", 2);
 				break;
 			}
 			
 			if (n>1)  {  // sort of a hack
 				System.out.println ("Building tree with Gamma node and 2 children");
 				Build_tree ("gamma", 2);
 			    status = true;
 			}
 			if (nextToken.equalsIgnoreCase("PARSE_COMPLETE")) {
 				return;
 			}
    		    }
 
 	}*/
 	private void fn_R() throws Exception {
 		fn_Rn ();
 		if (nextToken.equalsIgnoreCase("PARSE_COMPLETE")) {
 			return;
 		}
 		while ( (!isReserved(nextToken) && !lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Arrow") 
 				&& !nextToken.equals("|") && !lexer.getTypeOfToken (nextToken).equalsIgnoreCase("Operator_symbol") 
 				&& !nextToken.equals("gr") && !nextToken.equals("ge") && !nextToken.equals("ls") && !nextToken.equals("le") && !nextToken.equals("or") 
 				&& ! lexer.getTypeOfToken (nextToken).equalsIgnoreCase(")") && !nextToken.equals(",") && !nextToken.equals("aug"))  &&
 				(lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Integer") ||
 			     lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("Identifier") || lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("true") 
 		           || lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("false")  || 
 		              lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("(") || lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("dummy")) ||
 		              lexer.getTypeOfToken(nextToken).equalsIgnoreCase ("String") )
 		    {
 			
 			
 			fn_Rn ();	
 		
 			System.out.println ("After Fn R "+  nextToken );
 			Build_tree ("gamma", 2);
 			if (nextToken.equalsIgnoreCase("PARSE_COMPLETE")) {
 				return;
 			}
 		    }
 			
 	}
 	
 	
 	private void fn_Rn() throws Exception {
 		System.out.println ("In Fn Rn" );
 
 	    if (nextToken.equalsIgnoreCase("True")) {
 			readToken ("True");
 			Build_tree ("True", 1);
 			System.out.println ("Building tree with TRUE node and 1 children");
 		}
 		else if (nextToken.equalsIgnoreCase("False")) {
 			readToken ("False");
 			Build_tree ("False", 1);
 			System.out.println ("Building tree with FALSE node and 1 children");
 		}
 		else if (nextToken.equalsIgnoreCase("nil")) {
 			readToken ("nil");
			Build_tree ("nil", 0);
 			System.out.println ("Building tree with NIL node and 1 children");
 		}
 		else if (nextToken.equalsIgnoreCase("(")) {
 			readToken ("(");
 			fn_E();		
 			readToken (")");
 			System.out.println("Back to R with token "+ nextToken );
 		}
 		else if (nextToken.equalsIgnoreCase("dummy")){
 			readToken ("dummy");
 			Build_tree ("dummy",1);
 			System.out.println ("Building tree with Dummy node and 1 children");
 		}
 		else {
 		//	return;
 			//System.out.println ("About to read in Rn " + nextToken );
 			// Build_tree ("Gamma",2);
 			//nextToken =  getNextToken();
 			if (lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Identifier") || lexer.getTypeOfToken(nextToken).equalsIgnoreCase("String")|| lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Integer")) {
 				readToken (nextToken); 
 			}
 		}
 	}
 
 
 
 	private void fn_Vb() throws Exception {
 		System.out.println ("In Fn vb" );
 		/*if (lexer.getTypeOfToken(nextToken).equalsIgnoreCase("Identifier")) {
 			readToken (nextToken);
 		}*/
 		
 			if (nextToken.equalsIgnoreCase("(")) {
 				readToken ("(");
 				if (nextToken.equalsIgnoreCase(")")) {
 					Build_tree ("()", 2);
 					System.out.println ("Building tree with () node and 2 children");
 				}
 				else {
 					fn_V1 ();
 					readToken (")");
 				}
 			}
 		
 			else {
 				readToken (nextToken);
 			}
 		
 	}
 
 
 	private void fn_V1() throws Exception {
 		System.out.println ("In Fn V1" );
 		int n=0;
 		if (lexer.getTypeOfToken(nextToken).equals("Identifier")) {
 			readToken (nextToken);
 		}
 		//while (lexer.getTypeOfToken(nextToken).equals("Identifier") || nextToken.equals(",")) {
 		if (nextToken.equals(",")) {
 		    while (nextToken.equals(",")) {
 		        readToken (",");
 			    readToken (nextToken);
 			    n++;
 		    }
 		    Build_tree (",",n+1);
 		    System.out.println ("Building tree with , node and " + (n+1) + " children");
 	    }
 	}
 
 	private void fn_D() throws Exception {
 		System.out.println ("In Fn D" );
 		fn_Da();
 		if (nextToken.equalsIgnoreCase("within")) {
 			readToken ("within");
 			fn_D();
 			Build_tree ("within", 2);
 			System.out.println ("Building tree with Within node and 2 children");
 		}
 	
 	}
 	
 	private void fn_Da() throws Exception {
 		System.out.println ("In Fn Da" );
 		fn_Dr ();
 		if (nextToken.equalsIgnoreCase("and")) {
 			int n=0;
 			while ( nextToken.equalsIgnoreCase ("and")) {
 				readToken ("and");
 				fn_Dr ();
 				n++;
 			}
 			Build_tree ("and", n+1);
 			System.out.println ("Building tree with AND node and " + (n+1) + " children");
 		}
 	}
 
 	public static void main (String args[]) {
 		Parser p = new Parser (args[0]);
 		//p.testBuild_tree();
 		try {
 		    p.fn_E();
 		    if (nextToken.equalsIgnoreCase("PARSE_COMPLETE")) {
 				System.out.println ("*******PARSING COMPLETE*****");
 				System.out.println ("-------TREE VALUES----------");
 				p.preOrderTraversal ();
 				System.exit(0);
 			}
 		} catch (Exception e) {
 			System.out.println (e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
      
 }
