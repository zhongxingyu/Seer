 package uk.ac.ucl.comp2010.bestgroup;
 import java.util.*;
 
 import uk.ac.ucl.comp2010.bestgroup.AST.*;
 
 public class SemanticsVisitor extends Visitor{
 
 	LinkedList<HashMap<String, DeclNode>> symbolTables;
 	String returnNodeType;
 
 	public void error(String err, Node node) {
 		System.out.println(err + " (line " + node.lineNumber + ", col " + node.charNumber + ")");
 	}
 
 	public SemanticsVisitor() {
 		symbolTables = new LinkedList<HashMap<String, DeclNode>>();
 	}
 
 	private void insert(String id, DeclNode node){
 		symbolTables.getLast().put(id, node);
 	}
 
 	private void beginScope() {
 		symbolTables.addLast(new HashMap<String, DeclNode>());
 	}
 
 	private void endScope(){
 		symbolTables.removeLast();
 	}
 
 	//Possibly change to boolean return-type?
 	private DeclNode lookup(String id){
 		for(ListIterator<HashMap<String, DeclNode>> it = symbolTables.listIterator(symbolTables.size()); it.hasPrevious();){
 			DeclNode declaration = it.previous().get(id);
 			if(declaration != null){
 				return declaration;
 			}
 		}
 		return null;
 	}
 
 	private DeclNode lookupFirst(String id){
 		return symbolTables.getFirst().get(id);
 	}
 
 	//could possibly give more detailed error description if I change it from boolean to int or string and at least 3 returns.
 	/* private boolean lookupProperty(String id, String property){
     	//Looks if variable has been declared
     	DeclNode variableDecl = lookup(id);
     	//System.out.println("Entering, looking for: " + id);
     	if(variableDecl != null){
     		//System.out.println(variableDecl.toString());
     	}
     	if(variableDecl instanceof VarDeclNode){
     		//Now need to check what type it is and if it has been declared at the top.
     		String variableType = ((VarDeclNode) variableDecl).var.type;
     		//System.out.println("Found: " + variableType);
     		DeclNode customType = symbolTables.getFirst().get(variableType);
     		if(customType != null){
     			if(customType instanceof DatatypeDeclNode){
     				return true;
     			}
     		}
     	}
     	return false;
     }*/
 
 
 
 
 
 
 
 	// -------------------- SCOPE CHECKING --------------------------
 
 	@Override
 	public Object visit(ProgramNode node){
 		beginScope();
 		//System.out.println("Beginning Scope, declaration size: " + node.declarations.size());
 		visitList(node.declarations);
 		beginScope();
 		visit(node.main);
 		endScope();
 		endScope();
 		return null;
 
 	}
 
 	@Override
 	public Object visit(AccessorNode node) {
 		String variableId = node.path.getFirst();
 		DeclNode vardecl = lookup(variableId);
 		if (vardecl == null && !(vardecl instanceof VarDeclNode)) {
 			error("Variable " + variableId + " does not exist", node);
 			return null;
 		} else {
 			if (node.path.size() == 1) {
 				return ((VarDeclNode) vardecl).var.type;
 			} else {
 				String t = ((VarDeclNode) vardecl).var.type;
 				DeclNode type;
 				pathloop: for (int p = 0; p < node.path.size() - 1; p++) {
 					type = lookupFirst(t);
 					for (ListIterator<VarTypeNode> ti = ((DatatypeDeclNode) type).fields
 							.listIterator(); ti.hasNext();) {
 						VarTypeNode n = ti.next();
 						if (n.id.equals(node.path.get(p + 1))) {
 							t = n.type;
 							continue pathloop;
 						}
 					}
 					String e = node.path.getFirst();
 					for (int i = 1; i <= p; i++) {
 						if (i > 0) {
 							e += ".";
 						}
 						e += node.path.get(p);
 					}
 					e += " (type: " + t + ") does not have field "
 							+ node.path.get(p + 1);
 					error(e, node);
 					return null;
 				}
 				return t;
 			}
 		}
 	}
 
 
 	@Override
 	public Object visit(DatatypeDeclNode node) {
 		if (symbolTables.getFirst().containsKey(node.id)) {
 			error("Can't declare " + node.id + " twice in same scope", node);
 			return null;
 		}		
 
 		for (ListIterator<VarTypeNode> li = node.fields.listIterator(); li
 				.hasNext();) {
 			VarTypeNode n = li.next();
 			if (!isType(n.type)) {
 				error("Type " + n.type + " does not exist", n);
 				return null;
 			}
 		}
 
 		insert(node.id, node);
 
 		return null;
 		// for declaring datatypes
 	}
 
 
 
 	@Override
 	public Object visit(FuncCallExprNode node) {
 		DeclNode fdef = lookupFirst(node.id);
 		if (fdef == null || !(fdef instanceof FuncDeclNode)) {
 			error("Function " + node.id + " does not exit", node);
 			return null;
 		} else if (node.args.size() != ((FuncDeclNode) fdef).args.size()) {
 			error("Function " + node.id + " should take "
 					+ ((FuncDeclNode) fdef).args.size() + " argument(s) ("
 					+ node.args.size() + " given)", node);
 		} else {
 			for (int i = 0; i < node.args.size(); i++) {
 				String refType = (String) visit(node.args.get(i));
 				String declType = (String) (((FuncDeclNode) fdef).args.get(i).type);
 				if (!isSupertype(refType, declType)) {
 					error("Argument " + (i + 1) + " of function " + node.id
 							+ " should be of type " + declType + " (" + refType
 							+ " given)", node.args.get(i));
 				}
 			}
 		}
 		return ((FuncDeclNode) fdef).type;
 		// for calling functions
 	}
 
 	@Override
 	public Object visit(FuncDeclNode node) {
 		returnNodeType = node.type.toString();
 		if (!isType(node.type) && node.type != "void") {
 			error("Type " + node.type + " does not exist", node);
 			return null;
 		}
 
 		if (symbolTables.getFirst().containsKey(node.id)) {
 			error("Can't declare " + node.id + " twice in same scope", node);
 			return null;
 		}
 
 		insert(node.id, node);
 
 		beginScope();
 
 		for (ListIterator<VarTypeNode> li = node.args.listIterator(); li
 				.hasNext();) {
 			VarTypeNode n = li.next();
 			if (!isType(n.type)) {
 				error("Type " + n.type + " does not exist", n);
 				endScope();
 				return null;
 			} else {
 				insert(n.id, new VarDeclNode(n, new LinkedList<ExprNode>()));
 			}
 		}
 
 		visit(node.body);
 
 		endScope();
         
 		return null;
 		// for declaring functions
 	}
 
 
 
 	@Override
 	
 	public Object visit(VarDeclNode node) {
 		if (symbolTables.getLast().containsKey(node.var.id)) {
 			error("Can't declare " + node.var.id + " twice in same scope", node);
 			return null;
 		}
 		
 		if (!isType(node.var.type)) {
 			error("Type " + node.var.type + " does not exist", node);
 			return null;
 		}
 		
 		if(node.value != null) { //variable is initialised
 			if(node.var.type.equals("int") || node.var.type.equals("bool") || node.var.type.equals("float") || node.var.type.equals("char") || node.var.type.equals("tuple") || node.var.type.equals("list") || node.var.type.equals("string")) {
 				if(node.value.size() != 1) {
 					error("Invalid variable initialisation", node);
 				} else {
 					String v = (String) visit(node.value.getFirst());
 					if(v != null && ! isSupertype(v, node.var.type)) {
 						error("Can't convert from " + v + " to " + node.var.type, node);	
 					}
 				}
 			} else {
 				visitList(node.value);
 			}
 		}
 		
 		insert(node.var.id, node);
 		
 		return null;
 		// for declaring variables
 	}
 
 
 
 
 	// ---------------------------- TYPE CHECKING -----------------------
 
 
 	public boolean isType(String type) {
 		if (type.equals("string") || type.equals("int") || type.equals("float")
 				|| type.equals("bool") || type.equals("list") || type.equals("tuple")) {
 			return true;
 		}
 
 		DeclNode t = lookupFirst(type);
 
 		if (t == null || !(t instanceof DatatypeDeclNode)) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public boolean isSupertype(String sub, String sup) {
 		if(sub.equals(sup))
 			return true;
 		
 		if(sub.equals("int") && sup.equals("float"))
 			return true;
 		
 		if((sub.equals("list") || (sub.length()>=5 && sub.substring(0, 5).equals("list.")) || sub.equals("char")) && (sup.equals("tuple") || sup.equals("list")))
 			return true;
 		
 		
 		if(sub.equals("string") && sup.equals("list.char") || sub.equals("list.char") && sup.equals("string"))
 			return true;
 		
 		if(sub.length()>=5 && sub.substring(0, 5).equals("list.") && sup.length()>=5 && sup.substring(0, 5).equals("list."))
 			return isSupertype(sub.substring(5), sup.substring(5));
 
 		return false;
 	}
 
 
 	@Override
 	public Object visit(BoolNode node){
 		return "bool";
 	}
 
 	@Override
 	public Object visit(CharNode node){
 		return "char";
 	}
 
 	@Override
 	public Object visit(FloatNode node){
 		return "float";  	
 	}
 	
 
 	@Override
 	public Object visit(IntNode node) {
 		return "int";
 	}
 	
 	public Object visit(StringNode node){
 		return "string";
 	}
 	
 	public Object visit(SequenceNode node){
 		if(node.isTuple) {
 			visitList(node.elements);
 			return "tuple";
 		}
 		
 		if(node.elements.size() == 0) {
 			return "list";
 		}
 			
 		String type = (String) visit(node.elements.getFirst());
 		for(ListIterator<ExprNode>li = node.elements.listIterator(1); li.hasNext();) {
 			ExprNode n = li.next();
 			String v = (String) visit(n);
 			if(type != null) {
 				if(isSupertype(type, v)) {
 					type = v;
 				} else if(! isSupertype(v, type)) {
 					type = null;
 				}
 			}
 		}
 		if(type == null) {
 			return "list";
 		} else {
 			return "list." + type;
 		}
 	}
 	
 	public Object visit(AssignmentNode node) {
 		String t = (String) visit(node.var);
 		if(t == null)
 			return null;
 		if(t.equals("int") || t.equals("bool") || t.equals("float") || t.equals("char") || t.equals("tuple") || t.equals("list") || t.equals("string")) {
 			String v = (String) visit(node.value);
 			if(v != null && ! isSupertype(v, t)) {
 				error("Can't convert from " + v + " to " + t, node);	
 			}
 		} else {
 			visit(node.value);
 		}
 		return null;
 	}
 	
 	
 	public Object visit(IndexNode node) {
 		String i = (String) visit(node.index);
 		if(i != null && i != "int") {
 			error("int expected for indexing, " + i + "given", node);
 		}
 		
 		String v = (String) visit(node.var);
 		if(v == null) {
 			return null;
 		} else if(! isSupertype(v, "tuple")){
 			error("Indexing requires a sequence. " + v + " given", node);
 			return null;
 		} else if(v.equals("string")) {
 			return "char";
 		} else if(v.length()>=5 && v.substring(0, 5).equals("list.")){
 			return v.substring(5);
 		} else {			
 			return null;
 		}		
 	}
 	
 	public Object visit(IndexRangeNode node) {
 		String f = (String) visit(node.from);
 		if(f != null && f != "int") {
 			error("int expected for indexing, " + f + "given", node);
 		}
 		
 		String t = (String) visit(node.to);
 		if(t != null && t != "int") {
 			error("int expected for indexing, " + t + "given", node);
 		}
 		
 		String v = (String) visit(node.var);
 		return v;
 	}
 	
 	@Override
 	public Object visit(ComparisonNode node){
 		String left = (String) visit(node.left);
 		String right = (String) visit(node.right);
 		if(left == null || right == null) {
 			return "bool";
 		} else if((left.equals("int") && right.equals("int")) || (left.equals("float") && right.equals("float")) || (left.equals("bool") && right.equals("bool")) || (left.equals("int") && right.equals("float")) || (left.equals("float") && right.equals("int"))){
 			return "bool";
 		} else {
 			error("Can't interpret <" + left + "> " + node.op + " <" + right + ">", node);
 			return "bool";
 		}
 	}
 	
 	@Override
 	public Object visit(NegativeNode node) {
 		String e = (String) visit(node.expr);
 		if(e == null) {
 			return null;
 		} else if(e.equals("int")){
 			return "int";
 		} else if(e.equals("float")) {
 			return "float";
 		} else {
 			error("Can't interpret -<" + e + ">. Expecting numeric value", node);
 			return null;
 		}
 	}
 	
 	@Override
 	public Object visit(NotNode node) {
 		String e = (String) visit(node.expr);
 		if(e != null && !e.equals("bool")) {
 			error("Can't interpret !<" + e + ">. Expecting bool", node);
 		}
 		return "bool";
 	}
 	
 	@Override
 	public Object visit(BooleanOperationNode node){
 		String left = (String) visit(node.left);
 		String right = (String) visit(node.right);
 		if(left == null || right == null) {
 			return "bool";
 		} else if(left.equals("bool") && right.equals("bool")){
 			return "bool";
 		} else {
 			error("Can't interpret <" + left + "> " + node.op + " <" + right + ">", node);
 			return "bool";
 		}
 	}
 
 	@Override
 	public Object visit(ConcatNode node){
 		String left = (String) visit(node.left);
 		String right = (String) visit(node.right);
 		if(left == null || right == null) {
 			return null;
 		} else if(isSupertype(left, "list") && isSupertype(right, "list")){
 			if(left.equals(right)) {
 				return left;
 			} else {
 				return "list";
 			}
 		} else if(isSupertype(left, "tuple") && isSupertype(right, "tuple")){
 			return "tuple";
<<<<<<< HEAD
=======
 		} else if (left.equals("list") && right.equals("list")){
 			return "list";
 		} else if (left.equals("string") && right.equals("string")){
 		    return "string";		    
>>>>>>> 50ffe44ba5ea30c495eb977c5abbcab5f4f45824
 		} else {
 			error("Can't concatenate types " + node.left + " and " + node.right, node);
 			return null;
 		}
 	}
 
 
 
 	@Override
 	public Object visit(EqualsNode node){
 		String left = (String) visit(node.left);
 		String right = (String) visit(node.right);
 		if(left == null || right == null) {
 			return "bool";
 		} else if (isSupertype(left, right)){
 			return "bool";
 		} else if(isSupertype(right, left)){
 			return "bool";
 		} else {
 			error("Can't interpret <" + left + "> " + node.op + " <" + right + ">", node);
 			return "bool";
 		}
 	}
 
 	@Override
 	public Object visit(IfNode node) {
 		if(visit(node.condition) != "bool") {
 			error("If condition must be a boolean", node.condition);
 		}
 		visit(node.true_block);
 		if(node.false_block != null)
 			visit(node.false_block);		
 		return null;
 	}
 	
 	@Override
 	public Object visit(WhileNode node) {
 		if(visit(node.condition) != "bool") {
 			error("While condition must be a boolean", node.condition);
 		}
 		visit(node.loop);		
 		return null;
 	}
 	
 	
 	@Override
 	public Object visit(RepeatNode node) {
 		if(visit(node.condition) != "bool") {
 			error("Repeat condition must be a boolean", node.condition);
 		}
 		visit(node.loop);		
 		return null;
 	}
 
 	@Override
 	public Object visit(InNode node) {
 		visit(node.left);
 		String r = (String) visit(node.right);
 		if(r != null && ! isSupertype((String) r, "tuple")) {
 			error("'in' can only refer to a sequence. " + r + " given", node);
 		}
 		return "bool";
 	}
 	
 	@Override
 	public Object visit(LengthNode node) {
 		String s = (String) visit(node.sequence);
 		if(s != null && ! isSupertype((String) s, "tuple")) {
 			error("'len' can only refer to a sequence. " + s + " given", node);
 		}
 		return "int";
 	}
 
 
 	@Override
 	public Object visit(NumericOperationNode node) {
 		String left = (String) visit(node.left);
 		String right = (String) visit(node.right);
 		if(left == null || right == null) {
 			return null;
 		} else if(left.equals("int") && right.equals("int")) {
 			return "int";
 		} else if(left.equals("int") && right.equals("float") || left.equals("float") && right.equals("int") || left.equals("float") && right.equals("float")){
 			return "float";
 		} else {
 			error("Can't interpret <" + left + "> " + node.op + " <" + right + ">", node);
 			return null;
 		}
 	}
 	
 	@Override
 	public Object visit(ReturnNode node){
 		if(node.expr.toString().equals(returnNodeType)){
 			return "Not sure what this returns";
 		}
 		
 		return "ReturnNode error - return type must be the same as declaration type";
 	}
 
 }
