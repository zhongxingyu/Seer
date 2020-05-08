 package Source;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import Source.Variable.VAR_TYPE;
 
 public class AbstractSyntaxTree {
 	Node root;
 	private HashMap<Variable,Object> variables;
 	public static boolean DEBUG = true; 
 	
 	public AbstractSyntaxTree(Node root) {
 		this.root = root;
 		variables = new HashMap<Variable,Object>();
 	}
 	/**
 	 * Walks through the tree, calling operations as needed
 	 * @throws ParseError 
 	 */
 	public void walk() throws ParseError {
 		// Get statement-list
 		if (DEBUG) System.out.println("WALK");
 		Node stl = this.root.children.get(1);
 		walkStatementList(stl);
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void walkStatementList(Node stl) throws ParseError {
 		if (DEBUG) System.out.println("STL");
 		Node statement = stl.children.get(0);
 		Node firstToken = statement.children.get(0);
 		/**
 		 *  Statement possibilities
 		 * <statement> -> ID = <exp> ;
 		 * <statement> -> ID = # <exp> ; 
 		 * <statement> -> ID = maxfreqstring (ID);
 		 * <statement> -> replace REGEX with ASCII-STR in  <file-names> ;
 		 * <statement> -> recursivereplace REGEX with ASCII-STR in  <file-names> ;
 		 * <statement> -> print ( <exp-list> ) ;
 		 */
 		if (firstToken.name.equalsIgnoreCase("ID")) {
 			Variable id = firstToken.data;
 			if (DEBUG) System.out.println("ID:"+id.value);
 			Node valToken = statement.children.get(2);
 			Variable val = null;
 			if (valToken.name.equalsIgnoreCase("EXP")) {
 				boolean doLoad = false;
 				if ( valToken.children.size() == 1 
 						&& valToken.children.get(0).name.equalsIgnoreCase("ID") 
 						&& variables.containsKey(valToken.children.get(0).data) )
 					doLoad = true;
 				val = walkExpression(valToken, doLoad);
 			} else if (valToken.name.equalsIgnoreCase("#")) {
 				if (DEBUG) System.out.println("COUNT");			
 				int count = 0;
 				ArrayList<StringMatch> matches = (ArrayList<StringMatch>)walkExpression(statement.children.get(3), true).value;
 				for (StringMatch sm : matches) {
 					for (FileLoc fileloc : sm.getFilelocs()) {
 						count += fileloc.getNumLoc();
 					}
 				}
 				val = new Variable(Variable.VAR_TYPE.INT, count );
 			} else if (valToken.name.equalsIgnoreCase("MAXFREQSTRING")) {
 				// TODO : Implement Max freq string here.
 				val = null;
 				Variable a = (Variable)variables.get(stl.children.get(0).children.get(4).data);
 				ArrayList<StringMatch> matches = (ArrayList<StringMatch>)a.value;
 				val = new Variable(Variable.VAR_TYPE.STRINGLIST,Operations.maxfreqstring(matches));
 			}
 			
 			if (DEBUG) System.out.println("  SET VAR ID:"+id+"="+val);
 			variables.put(id, val);
 		} else if (firstToken.name.equalsIgnoreCase("REPLACE")) {
 			if (DEBUG) System.out.println("REPLACE");
 	
 			Node regx = statement.children.get(1);
 			String regX = regx.data.value.toString();
 	
 			Node asci = statement.children.get(3);
 			String ascI = asci.data.value.toString();
 	
 			Node inFile = statement.children.get(5);
 			String iFile = inFile.name;
 	
 			Node outFile = statement.children.get(7);
 			String oFile = outFile.name;
 	
 			try {
 				Operations.replace(regX, ascI, iFile, oFile);
 			} catch (IOException e) {
 				System.out.println("Replace could not finish Operation. Make sure files provided exists.");
 				e.printStackTrace();
 			}
 		} else if (firstToken.name.equalsIgnoreCase("RECURSIVE_REPLACE")) {
 			if (DEBUG) System.out.println("RECURSIVE_REPLACE");
 			Node regx = statement.children.get(1);
 			String regX = regx.data.value.toString();
 	
 			Node asci = statement.children.get(3);
 			String ascI = asci.data.value.toString();
 	
 			Node inFile = statement.children.get(5);
 			String iFile = inFile.name;
 	
 			Node outFile = statement.children.get(7);
 			String oFile = outFile.name;
 	
 			try {
 				Operations.recursiveReplace(regX, ascI, iFile, oFile);
 			} catch (IOException e) {
 				System.out.println("Recursive Replace could not finish Operation. Make sure files provided exists.");
 				e.printStackTrace();
 			}
 		} else if (firstToken.name.equalsIgnoreCase("PRINT")) {
 			if (DEBUG) System.out.println("PRINT");
 			walkPrint( statement.children.get(2));
 		}
 		
 		Node tail = stl.children.get(1);
 		if (tail != null)
 			walkStatementList(tail);
 	}
 	/**
 	 * Walk Print
 	 * @param stl
 	 * @throws ParseError 
 	 */
 	private void walkPrint(Node el) throws ParseError {
 		ArrayList<Variable> variableList = walkExpressionList(el);
 		
 		if (variableList.isEmpty())
 			System.out.println("Print: ()");
 		else {
 			System.out.print("Print: ( ");
 			for (int i = 0; i < variableList.size(); i++) {
 				if (i>0)
 					System.out.print(", ");
 				if(variableList.get(i).type == VAR_TYPE.INT) {
 					System.out.println("," + variableList.get(i).value);
 				} else {
 					System.out.println(variableList.get(i));
 				}
 			}
 			System.out.println(" )");
 		}
 	}
 	
 	
 	/**
 	 * <exp-list> -> <exp> <exp-list-tail>
 	 * @param node
 	 * @throws ParseError 
 	 */
 	@SuppressWarnings("unchecked")
	private ArrayList<Variable> walkExpressionList(Node el, boolean doLoad) throws ParseError {
 		ArrayList<Variable> variableList = new ArrayList<Variable>();
 		
		Variable v = walkExpression(el.children.get(0),doLoad);
 		variableList.add(v);
 		
 		
 		Node tail = el.children.get(1);
 		if (tail != null) {
 			ArrayList<Variable> vb = walkExpressionListTail(tail);
 			if(vb != null)
 				variableList.addAll(vb);
 		}
 		
 		return variableList;
 	}
 	
 	/**
 	 *  <exp-list-tail> -> , <exp> <exp-list-tail>
 	 *	<exp-list-tail> -> epislon
 	 * 
 	 * @param tail
 	 * @return
 	 * @throws ParseError 
 	 */
 	private ArrayList<Variable> walkExpressionListTail(Node el) throws ParseError {
 		if(el == null) {
 			return null;
 		}
 		ArrayList<Variable> variableList = new ArrayList<Variable>();
 		
 		Variable v = walkExpression(el.children.get(1),true);
 		variableList.add(v);
 		Node tail = el.children.get(2);
 		if (tail != null) {
 			ArrayList<Variable> vb = walkExpressionListTail(tail);
 			if(vb != null)
 				variableList.addAll(vb);
 		}
 		
 		
 		return variableList;
 	}
 	/**
 	 * Walks through expression returning either an Integer or an ArrayList<StringMatch>
 	 * An expression in MiniRE can be:
 	 * -	A find expression, whose format is gfind REGEX in filenameh where filename is the name of a text file surrounded by "fs.  
 	 * -    A variable, of type integer or string-match list. Using a variable not (yet) assigned to is an error.
 	 * -	#v which returns the length (as an integer) of string-match list variable v, ie. the number of strings contained in the string list. 
 	 * -	Set operations applied to string-match lists that return modified lists: union (returns union of the two lists), intersection (returns intersection of the two lists), and difference (first list minus second). Represented by literal tokens union, inters, and -, respectively. Associativity is by parentheses and left to right.
 	 * <exp>-> ID  | ( <exp> ) | <term> <exp-tail>
 	 * @return
 	 * @throws ParseError 
 	 */
 	private Variable walkExpression(Node exp,boolean doLoad) throws ParseError {
 		if (DEBUG) System.out.println("EXP");
 		Node first = exp.children.get(0);
 		if (first.name.equalsIgnoreCase("ID")) {
 			if (DEBUG) System.out.println("  LOAD ID:"+first.data+"(from variables? "+doLoad+" )");
 			if (doLoad) {
 				Variable loaded = (Variable) variables.get( first.data );
 				if (loaded != null)
 					return loaded;
 				else { 
 					System.out.println("  LOAD ID:"+first.data+"(from variables? "+doLoad+" ) : " + exp + " : datastore: "+variables);
 					throw new ParseError("VARIABLE ID: "+first.data+" not in datastore.");
 //					return first.data;
 				}
 			}
 			else
 				return first.data;
 		} else if (first.name.equalsIgnoreCase("TERM")) {
 			ArrayList<StringMatch> matches = walkTerm(first);
 			Node tail = exp.children.get(1);
 			return walkExpressionTail(matches, tail);
 		} else if(first.name.equalsIgnoreCase("(")) {
 			return walkExpression(first.children.get(1),doLoad);
 		} else {
 			if (DEBUG) System.out.println("HMM... (Expected first child == ID or TERM or '('): "+exp+" & it's first child:"+first);
 			throw new ParseError("VARIABLE: "+first+" not in datastore.");
 //			return first.data;
 		}
 	}
 	
 	/**
 	 * <exp-tail> ->  <bin-op> <term> <exp-tail> 
 	 * <exp-tail> -> epsilon
 	 * @param matches
 	 * @param tail
 	 * @return
 	 * @throws ParseError 
 	 */
 	private Variable walkExpressionTail(ArrayList<StringMatch> matches,
 			Node tail) throws ParseError {
 		if (tail == null)
 			return new Variable(Variable.VAR_TYPE.STRINGLIST, matches );
 		String binop = tail.children.get(0).name;
 		if (DEBUG) System.out.println("BINOP "+binop);
 		ArrayList<StringMatch> rightSide = walkTerm( tail.children.get(1) );
 		ArrayList<StringMatch> newList = null;
 		if (binop.equalsIgnoreCase("INTERS")) {
 			newList = Operations.inters(matches, rightSide);
 		} else if (binop.equalsIgnoreCase("DIFF")) {
 			newList = Operations.diff(matches, rightSide);
 		} else if (binop.equalsIgnoreCase("UNION")) {
 			newList = Operations.union(matches, rightSide);
 		} else {
 			throw new ParseError("ERROR: <BIN-OP>:"+binop+" not known.");
 		}
 		matches.clear();
 		matches.addAll(newList);
 		return walkExpressionTail( matches, tail.children.get(2) );
 	}
 	/**
 	 * Term is always a FIND
 	 * <term> -> find REGEX in  <file-name>
 	 * @param first
 	 * @return
 	 */
 	private ArrayList<StringMatch> walkTerm(Node term) {
 		if (DEBUG) System.out.println("TERM");
 		String regex = (String) term.children.get(1).data.value;
 		String filename = (String) term.children.get(3).data.value;
 		if (DEBUG) System.out.println("FIND "+regex+" IN "+filename);
 		ArrayList<StringMatch> matches = new ArrayList<StringMatch>();
 		try {
 			matches = Operations.find(regex, filename);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch(ParseError e){
 			e.printStackTrace();
 		}
 		return matches;
 	}
 	@Override
 	public String toString() {
 		return "AST{ " + root + " }";
 	}
 }
