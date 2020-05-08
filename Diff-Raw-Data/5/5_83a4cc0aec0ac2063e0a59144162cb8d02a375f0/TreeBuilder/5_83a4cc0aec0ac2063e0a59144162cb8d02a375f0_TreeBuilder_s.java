 package latte.grammar;
 
 import latte.grammar.latteParser.program_return;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 public class TreeBuilder {
 
 	private HashMap<String, CommonTree> storage_func = new HashMap<String, CommonTree>();
 	private Stack<HashMap<String, Integer>> storage_vars = new Stack<HashMap<String,Integer>>();
 	
 	public CommonTree buildTree(String program_data) throws RecognitionException {
 		CharStream charStream = new ANTLRStringStream(program_data);
 		latteLexer lexer = new latteLexer(charStream);
 		TokenStream tokenStream = new CommonTokenStream(lexer);
 		latteParser parser = new latteParser(tokenStream);
 
 		program_return program = parser.program();
 		return program.tree;
 	}
 
 	public void checkType(CommonTree root) throws TypesMismatchException {
 		loadFunctions(root);
 		checkTypes(root);
 		checkReturns();
 	}
 
 	private void checkReturns() throws TypesMismatchException {
 		for (Iterator<CommonTree> iterator = storage_func.values().iterator(); iterator.hasNext();) {
 			CommonTree fun = iterator.next();
 			if (fun != null) {
 				@SuppressWarnings("unchecked")
 				List<CommonTree> topdef = fun.getChildren();
 	
 				int expectedReturn = topdef.get(0).token.getType();
 				CommonTree blockLookup;
 				if (topdef.get(2).token.getType() == latteParser.BLOCK) {
 					blockLookup = topdef.get(2);
 				} else {
 					blockLookup = topdef.get(3);
 				}
 	
 				storage_vars.push(new HashMap<String, Integer>());
 				CommonTree args = topdef.get(2);
 				if (args.getType() == latteParser.ARGS) {
 					@SuppressWarnings("unchecked")
 					List<CommonTree> argsToLoad = args.getChildren();
 					for(int i = 0; i < argsToLoad.size(); i++) {
 						CommonTree arg = argsToLoad.get(i);
 						String ident = arg.getChild(1).getText();
 						int type = arg.getChild(0).getType();
 						storage_vars.peek().put(ident, type);
 					}
 				}
 				returnLookup(expectedReturn, blockLookup);
 				storage_vars.pop();
 			}
 		}
 	}
 
 	private boolean returnLookup(int expectedReturn, CommonTree blockLookup) throws TypesMismatchException {
 		@SuppressWarnings("unchecked")
 		List<CommonTree> children = blockLookup.getChildren();
 		
 		// Only last stmt can be return
 		if (children != null) {
 			for (int i = 0; i < children.size() - 1; i++) {
 				isNotReturn(children.get(i), true);
 			}
 			return isReturn(children.get(children.size()-1), expectedReturn);
 		} else if (expectedReturn != latteParser.TYPE_VOID) {
 			throw new TypesMismatchException("Return was expected at the end.");
 		}
 		
 		return false;
 	}
 
 	private boolean isReturn(CommonTree commonTree, int expectedReturn) throws TypesMismatchException {
 		int type = commonTree.token.getType();
 		
 		switch (type) {
 		case latteParser.RET:
 			int givenType = checkTypes((CommonTree)commonTree.getChild(0)); 
 			if (expectedReturn != givenType) {
 				throw new TypesMismatchException("Return type mismatch.");
 			}
 			return true;
 			
 		case latteParser.RETV:
 			if (expectedReturn != latteParser.TYPE_VOID) {
 				throw new TypesMismatchException("Return type mismatch 2.");
 			}
 			return true;
 
 		case latteParser.COND: {
 			boolean result = false;
 			if (commonTree.getChildren().size() == 3) {
 				CommonTree expr = (CommonTree)commonTree.getChild(0);
 				if (expr.token.getType() == latteParser.TRUE) {
 					result = isReturn((CommonTree)commonTree.getChild(1), expectedReturn);
 				} else if (expr.token.getType() == latteParser.FALSE) {
 					result = isReturn((CommonTree)commonTree.getChild(2), expectedReturn);
 				} else {
 					boolean lret = isReturn((CommonTree)commonTree.getChild(1), expectedReturn);
 					boolean rret = isReturn((CommonTree)commonTree.getChild(2), expectedReturn);
 					result = (lret && rret);
 				}
 			} else {
 				CommonTree expr = (CommonTree)commonTree.getChild(0);
 				if (expr.token.getType() == latteParser.FALSE) {
 					result = false;
 				} else {
 					result = isReturn((CommonTree)commonTree.getChild(1), expectedReturn);
 				}
 			}
 			return result;
 		}
 
 		case latteParser.BLOCK: {
 			storage_vars.push(new HashMap<String, Integer>());
 			boolean result = returnLookup(expectedReturn, commonTree);
 			storage_vars.pop();
 			
 			return result;
 		}
 		
 		case latteParser.DECL: {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> decl = commonTree.getChildren();
 			int varType = decl.get(0).token.getType();
 			for(int i = 1; i < decl.size(); i++) {
 				CommonTree child = decl.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 				storage_vars.peek().put(ident, varType);
 			}
 			break;
 		}
 
 		default:
 			if (expectedReturn != latteParser.TYPE_VOID) {
 				throw new TypesMismatchException("Last stmt is not return stmt.");
 			}
 			break;
 		}
 		
 		return false;
 	}
 
 	private boolean isNotReturn(CommonTree commonTree, boolean topLevel) throws TypesMismatchException {
 		int type = commonTree.token.getType();
 		
 		switch (type) {
 		case latteParser.RET:
 		case latteParser.RETV:
 			if (topLevel) {
 				throw new TypesMismatchException("Return earlier than end of block");	
 			} else {
 				return false;
 			}
 
 		case latteParser.COND:
 			boolean result = true;
 
 			if (commonTree.getChildren().size() == 3) {
 				CommonTree expr = (CommonTree)commonTree.getChild(1);
 				if (expr.token.getType() == latteParser.TRUE) {
 					result = isNotReturn((CommonTree)commonTree.getChild(1), true);
 				} else if (expr.token.getType() == latteParser.FALSE) {
 					result = isNotReturn((CommonTree)commonTree.getChild(2), true);
 				} else {
 					boolean lret = isNotReturn((CommonTree)commonTree.getChild(1), false);
 					boolean rret = isNotReturn((CommonTree)commonTree.getChild(2), false);
 					result = (lret || rret);
 				}
 			} else {
 				CommonTree expr = (CommonTree)commonTree.getChild(1);
 				if (expr.token.getType() == latteParser.TRUE) {
 					result = isNotReturn((CommonTree)commonTree.getChild(1), true);
 				}
 			}
 			
 			if (result) {
 				return true;
 			} else {
 				if (topLevel) {
 					throw new TypesMismatchException("Return earlier than end of block");	
 				} else {
 					return false;
 				}
 			}
 
 		case latteParser.BLOCK:
 			storage_vars.push(new HashMap<String, Integer>());
 			@SuppressWarnings("unchecked")
 			List<CommonTree> children = commonTree.getChildren();
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					isNotReturn(child, true);
 				}
 			}
 			storage_vars.pop();
 			
 			break;
 
 		case latteParser.DECL: {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> decl = commonTree.getChildren();
 			int varType = decl.get(0).token.getType();
 			for(int i = 1; i < decl.size(); i++) {
 				CommonTree child = decl.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 				storage_vars.peek().put(ident, varType);
 			}
 			break;
 		}
 			
 		default:
 			break;
 		}
 		
 		return true;
 	}
 
 	private void loadFunctions(CommonTree root) throws TypesMismatchException {
 		// lang defined functions
 		storage_func.put("printString", null);
 		storage_func.put("printInt", null);
 		storage_func.put("readString", null);
 		storage_func.put("readInt", null);
 		
 		if (root.token == null) {
 			@SuppressWarnings("unchecked")
 			List<CommonTree> children = root.getChildren();
 
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					addFunction(child);
 				}
 			}			
 		} else {
 			addFunction(root);
 		}
 	}
 
 	private void addFunction(CommonTree topdef) throws TypesMismatchException {
 		@SuppressWarnings("unchecked")
 		List<CommonTree> func = topdef.getChildren();
 		
 		String ident = func.get(1).token.getText();
 		if (lookupFun(ident)) {
 			throw new TypesMismatchException("Function name duplicated");
 		}
 		storage_func.put(ident, topdef);
 	}
 	
 	private int lookupVar(String ident) {
 		for(int i = storage_vars.size()-1; i >= 0; i--) {
 			HashMap<String,Integer> locVar = storage_vars.get(i);
 			if (locVar.containsKey(ident)) {
 				return locVar.get(ident);
 			}
 		}
 		return -1;
 	}
 
 	private boolean lookupFun(String funName) {
 		return storage_func.containsKey(funName);
 	}
 
 	public int checkTypes(CommonTree root) throws TypesMismatchException {
 		int token_type = -1;
 		if (root.token != null) {
 			token_type = root.token.getType();
 		}
 		@SuppressWarnings("unchecked")
 		List<CommonTree> children = root.getChildren();
 
 		switch (token_type) {
 		
 		// int int || str str
 		case latteParser.OP_PLUS: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 			
 			if (type_left != type_right || 
 					(type_left != latteParser.TYPE_INT &&
 					type_left != latteParser.TYPE_STRING)) {
 				throw new TypesMismatchException("Add mismatch");
 			}
 			
 			return type_left;
 		}
 			
 		// int int
 		case latteParser.OP_MINUS:
 		case latteParser.OP_TIMES:
 		case latteParser.OP_DIV:
 		case latteParser.OP_MOD: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					type_left != latteParser.TYPE_INT) {
 				throw new TypesMismatchException("Mismatch");
 			}
 
 			return latteParser.TYPE_INT;
 		}
 		
 		// int int -> bool
 		case latteParser.OP_LTH:
 		case latteParser.OP_LE:
 		case latteParser.OP_GTH:
 		case latteParser.OP_GE: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					type_left != latteParser.TYPE_INT) {
 				throw new TypesMismatchException("Mismatch");
 			}
 
 			return latteParser.TYPE_BOOLEAN;
 		}
 		
		// int int || bool bool
 		case latteParser.OP_EQU:
 		case latteParser.OP_NE: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					(type_left != latteParser.TYPE_INT &&
 					type_left != latteParser.TYPE_BOOLEAN)) {
 				throw new TypesMismatchException("Mismatch");
 			}
 
			return type_left;
 		}
 		
 		// bool bool
 		case latteParser.OP_AND:
 		case latteParser.OP_OR: {
 			int type_left = checkTypes(children.get(0));
 			int type_right = checkTypes(children.get(1));
 
 			if (type_left != type_right ||
 					type_left != latteParser.TYPE_BOOLEAN) {
 				throw new TypesMismatchException("Mismatch");
 			}
 
 			return type_left;
 		}
 		
 		case latteParser.NEGATION: {
 			int type_left = checkTypes(children.get(0));
 
 			if (type_left != latteParser.TYPE_INT) {
 				throw new TypesMismatchException("Mismatch");
 			}
 			
 			return latteParser.TYPE_INT;
 		}
 		
 		case latteParser.NOT: {
 			int type_left = checkTypes(children.get(0));
 
 			if (type_left != latteParser.TYPE_BOOLEAN) {
 				throw new TypesMismatchException("Mismatch");
 			}
 			
 			return latteParser.TYPE_BOOLEAN;
 		}
 
 		case latteParser.INTEGER:
 			return latteParser.TYPE_INT;
 		case latteParser.FALSE:
 			return latteParser.TYPE_BOOLEAN;
 		case latteParser.TRUE:
 			return latteParser.TYPE_BOOLEAN;
 		case latteParser.STRING:
 			return latteParser.TYPE_STRING;
 		case latteParser.VAR_IDENT: {
 			int result = lookupVar(children.get(0).token.getText());
 			if (result != -1) {
 				return lookupVar(children.get(0).token.getText());
 			} else {
 				throw new TypesMismatchException("unknown variable");
 			}
 		}
 			
 		case latteParser.EAPP: {
 			String funName = children.get(0).token.getText();
 			if (!lookupFun(funName)) {
 				throw new TypesMismatchException("No such function");
 			}
 
 			if (funName.compareTo("printString") == 0) { 
 				return checkPrintString(children); 
 			}
 			if (funName.compareTo("printInt") == 0) { 
 				return checkPrintInt(children); 
 			}
 			if (funName.compareTo("readString") == 0) { 
 				return checkReadString(children); 
 			}
 			if (funName.compareTo("readInt") == 0) { 
 				return checkReadInt(children); 
 			}
 			
 			CommonTree func = storage_func.get(funName);
 			CommonTree args = (CommonTree)func.getChildren().get(2);
 
 			// args type cheking
 			if (args.token.getType() == latteParser.ARGS) {
 				@SuppressWarnings("unchecked")
 				List<CommonTree> argsList = args.getChildren();
 				if (children.size()-1 != argsList.size()) {
 					throw new TypesMismatchException("No of passed arguments mismatch.");
 				}
 				for (int i = 0; i < argsList.size(); i++) {
 					int givenType = checkTypes(children.get(i+1));
 					int expectedType = argsList.get(i).getChild(0).getType();
 					if (givenType != expectedType) {
 						throw new TypesMismatchException("Expected different type argument.");
 					}
 				}
 			} else if (children.size()-1 != 0) {
 				throw new TypesMismatchException("Zero argument function but args given.");
 			}
 			
 			return checkTypes((CommonTree)func.getChildren().get(0));
 		}
 		
 		case latteParser.ASS: {
 			int type = lookupVar(children.get(0).token.getText());
 			if (type != -1) {
 				int currType = checkTypes(children.get(1));
 				if (type != currType) {
 					throw new TypesMismatchException("Mismatch in assignment");
 				}
 			} else {
 				throw new TypesMismatchException("unknown variable");
 			}
 			
 			break;
 		}
 
 		case latteParser.DECR:
 		case latteParser.INCR: {
 			int type = lookupVar(children.get(0).token.getText());
 			if (type == -1) {
 				throw new TypesMismatchException("unknown variable");
 			} else if (type != latteParser.TYPE_INT) {
 				throw new TypesMismatchException("wrong type in incr/decr");
 			}
 			
 			break;
 		}
 		
 		case latteParser.BLOCK: {
 			if (children != null) {
 				// new block vars
 				storage_vars.push(new HashMap<String, Integer>());
 	
 				// iterating with new variables block
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					checkTypes(child);
 				}
 				
 				// old block vars
 				storage_vars.pop();
 			}
 			break;
 		}
 	
 		case latteParser.TOP_DEF: {
 			// new block vars
 			storage_vars.push(new HashMap<String, Integer>());
 
 			// checking args integrity
 			CommonTree args = children.get(2);
 			if (args.getType() == latteParser.ARGS) {
 				@SuppressWarnings("unchecked")
 				List<CommonTree> argsToLoad = args.getChildren();
 				for(int i = 0; i < argsToLoad.size(); i++) {
 					CommonTree arg = argsToLoad.get(i);
 					String ident = arg.getChild(1).getText();
 					int type = arg.getChild(0).getType();
 					if (lookupVar(arg.getChild(1).getText()) != -1) {
 						throw new TypesMismatchException("funct declr: already exists");
 					}
 					storage_vars.peek().put(ident, type);
 				}
 				checkTypes(children.get(3));
 			} else {
 				checkTypes(children.get(2));
 			}
 			
 			// old block vars
 			storage_vars.pop();
 			break;
 		}
 
 		case latteParser.DECL: {
 			int type = children.get(0).token.getType();
 			for(int i = 1; i < children.size(); i++) {
 				CommonTree child = children.get(i);
 				@SuppressWarnings("unchecked")
 				List<CommonTree> declaration = child.getChildren();
 				String ident = declaration.get(0).token.getText();
 
 				if (lookupVar(ident) != -1) {
 					throw new TypesMismatchException("already exists");
 				}
 				
 				if (child.token.getType() == latteParser.INIT) {
 					int currType = checkTypes(declaration.get(1));
 					if (type != currType) {
 						throw new TypesMismatchException("Mismatch");
 					}
 				}
 
 				storage_vars.peek().put(ident, type);
 			}
 			break;
 		}
 
 //		case latteParser.COND: {
 //			CommonTree expr = children.get(0);
 //			if (expr.token.getType() == latteParser.TRUE) {
 //				root.replaceChildren(0, 0, 0);// = children.get(1);
 //			}
 //			break;
 //		}
 
 		default: {
 			if (children != null) {
 				for (Iterator<CommonTree> i = children.iterator(); i.hasNext();) {
 					CommonTree child = i.next();
 					checkTypes(child);
 				}
 			}
 			break;
 		}
 		}
 
 		return token_type;
 	}
 
 	private int checkReadInt(List<CommonTree> children) throws TypesMismatchException {
 		if (children.size()-1 != 0) {
 			throw new TypesMismatchException("printStr expects zero argument.");
 		}
 		return latteParser.TYPE_INT;
 	}
 
 	private int checkReadString(List<CommonTree> children) throws TypesMismatchException {
 		if (children.size()-1 != 0) {
 			throw new TypesMismatchException("printStr expects zero argument.");
 		}
 		return latteParser.TYPE_STRING;
 	}
 
 	private int checkPrintString(List<CommonTree> children) throws TypesMismatchException {
 		if (children.size()-1 != 1) {
 			throw new TypesMismatchException("printStr expects one argument.");
 		}
 		int givenType = checkTypes(children.get(1));
 		int expectedType = latteParser.TYPE_STRING;
 		if (givenType != expectedType) {
 			throw new TypesMismatchException("Expected `different type argument.");
 		}
 		return latteParser.TYPE_VOID;
 	}
 
 	private int checkPrintInt(List<CommonTree> children) throws TypesMismatchException {
 		if (children.size()-1 != 1) {
 			throw new TypesMismatchException("printInt expects one argument.");
 		}
 		int givenType = checkTypes(children.get(1));
 		int expectedType = latteParser.TYPE_INT;
 		if (givenType != expectedType) {
 			throw new TypesMismatchException("Expected `different type argument.");
 		}
 		return latteParser.TYPE_VOID;
 	}
 }
