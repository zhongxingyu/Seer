 package cps450;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import cps450.oodle.analysis.*;
 import cps450.oodle.node.*;
 
 public class SemanticChecker extends DepthFirstAdapter {
 
 	SymbolTable symbolTable;
 	Hashtable<Node, Type> typeDecorations;
 	Hashtable<String, ClassDeclaration> classTable;
 	Integer errorCount;
 	int curArgCount;
 	ClassDeclaration currentClassDeclaration = null;
 	String currentClassName;
 	MethodDeclaration currentMethodDeclaration = null;
 	
 	public SemanticChecker() {
 		super();
 	}
 	
 	public SemanticChecker(Hashtable<String, ClassDeclaration> _classTable, Hashtable<Node, Type> _typeDecorations) {
 		super();
 		classTable = _classTable;
 		symbolTable = new SymbolTable();
 		typeDecorations = _typeDecorations;
 		errorCount = 0;
 		Type.intialize(classTable);
 		
 		symbolTable.push("out", new VariableDeclaration(Type.getType("Writer"), "SYSTEM DECLARED"));
 		symbolTable.push("in", new VariableDeclaration(Type.getType("Reader"), "SYSTEM DECLARED"));
 		
 		symbolTable.beginScope();
 	}
 	
 	private String locationFor(Token t) {
 		return SourceHolder.instance().getFilenameFor(t) + ":" + SourceHolder.instance().getLineNumberFor(t) + "," + t.getPos();
 	}
 	
 	private void reportError(Token t, String errorText) {
 		this.errorCount++;
 		String output = "";
 		if (t != null) {
 			output += locationFor(t) + ":";
 		}
 		System.out.println(output + errorText);
 	}
 	
 	private Type typeFor(PType type) {
 		if (type == null) {
 			return Type.getType("void");
 		} else if (type instanceof AIntegerType) {
 			return Type.getType("int");
 		} else if (type instanceof AStringType) {
 			return Type.getType("string");
 		} else if (type instanceof ABooleanType) {
 			return Type.getType("boolean");
 		} else if (type instanceof AOtherType) {
 			return Type.getType(((AOtherType)type).getId().getText());
 		}
 		return null; // TODO: throw exception
 	}
 
 	@Override
 	public void inAStart(AStart node) {
 		if (node.getClassDef().size() < 1) {
 			reportError(null, "You must define at least one class");
 		}
 	}
 	
 	@Override
 	public void outAStart(AStart node) {
		if (currentClassDeclaration != null && currentClassDeclaration.getMethod("start") == null) {
 			reportError(null, "You must define the method 'start' in the primary class '" + currentClassName + "'");
 		}
 	}
 	
 	@Override
 	public void outAAddExpression(AAddExpression node) {
 		Type result = Type.getType("error");
 		Type lt = typeDecorations.get(node.getExpr1());
 		Type rt = typeDecorations.get(node.getExpr2());
 		Token token = (node.getOperator() instanceof APlusOperator ? ((APlusOperator)node.getOperator()).getOp() : ((AMinusOperator)node.getOperator()).getOp());
 
 		if (lt.compatibleWith(rt)) {
 			// Types match, now check to see that they are both integers
 			Type expectedType = Type.getType("int");
 			if (lt != expectedType) { // only need to check one side since we already know they are the same type
 				reportError(token, "Expected expressions of type '" + expectedType.getName() + "' got '" + lt.getName() + "' at operator " + token.getText());
 			} else {
 				result = lt;
 			}
 		} else {
 			reportError(token, "Found expressions of mismatched types ('" + lt.getName() + "' and '" + rt.getName() + "') at operator " + token.getText());
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAAndExpression(AAndExpression node) {
 		Type result = Type.getType("error");
 		Type lt = typeDecorations.get(node.getExpr1());
 		Type rt = typeDecorations.get(node.getExpr2());
 		Token token = node.getAnd();
 
 		if (lt.compatibleWith(rt)) {
 			// Types match, now check to see that they are both booleans
 			Type expectedType = Type.getType("boolean");
 			if (lt != expectedType) { // only need to check one side since we already know they are the same type
 				reportError(token, "Expected expressions of type '" + expectedType.getName() + "' got '" + lt.getName() + "' at operator " + token.getText());
 			} else {
 				result = lt;
 			}
 		} else {
 			reportError(token, "Found expressions of mismatched types ('" + lt.getName() + "' and '" + rt.getName() + "') at operator " + token.getText());
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAAssignmentStatement(AAssignmentStatement node) {
 		// TODO: Need to implement checking on array accessing assignments
 		Type result = Type.getType("error");
 		
 		Declaration decl = null;
 		Symbol id = symbolTable.lookup(node.getId().getText());
 		if (id == null) {
 			reportError(node.getId(), "Attempted to assign a value to an undefined variable '" + node.getId().getText() + "'");
 		} else {
 			decl = id.getDeclaration();
 			if (decl instanceof VariableDeclaration || decl == currentMethodDeclaration) {
 				if (decl instanceof MethodDeclaration  && decl.getType().getName().equals("void")) {
 					reportError(node.getId(), "Attempted to set a return value in a void method.");
 				} else {
 					Type expectedType = decl.getType();
 					Type exprType = typeDecorations.get(node.getValue());
 					if (expectedType.compatibleWith(exprType)) {
 						result = exprType;
 					} else {
 						reportError(node.getId(), "Expected expression of type '" + expectedType.getName() + "' got '" + exprType.getName() + "' at assignment to variable '" + node.getId().getText() + "'");
 					}
 				}
 			} else {
 				reportError(node.getId(), "Attempted to assign a value to a non-variable identifier '" + node.getId().getText() + "'");
 			}
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAArrayExpression(AArrayExpression node) {
 		reportError(node.getId(), "Arrays are not yet supported");
 		typeDecorations.put(node, Type.getType("error"));
 	}
 
 	@Override
 	public void outAArgumentDeclaration(AArgumentDeclaration node) {
 		VariableDeclaration decl = new VariableDeclaration( typeFor(node.getType()), locationFor(node.getName()) );
 		if (symbolTable.scopeContains(node.getName().getText()) != null) {
 			reportError(node.getName(), "Argument '" + node.getName().getText() + "' previously defined.");
 		}
 		symbolTable.push(node.getName().getText(), decl);
 		
 		decl.setArgumentPosition(curArgCount);
 		currentMethodDeclaration.addVariable(node.getName().getText(), (VariableDeclaration)decl);
 		
 		decl.setDeclaringKlass(currentClassDeclaration);
 		
 		curArgCount += 1;
 	}
 
 	@Override
 	public void outACallExpression(ACallExpression node) {
 		Type returnType = Type.getType("error");
 		
 		ClassDeclaration klass = node.getObject() == null ?
 				currentClassDeclaration : classTable.get(typeDecorations.get(node.getObject()).getName());
 		MethodDeclaration decl = klass.getMethod(node.getMethod().getText());
 		if (decl == null) {
 			reportError(node.getMethod(), "Used undefined method '" + node.getMethod().getText() + "'");
 		} else {
 			// Check count of arguments
 			Integer actualArgCount = node.getArguments().size();
 			if (decl.getArgumentCount() != actualArgCount) {
 				reportError(node.getMethod(), "Wrong number of arguments given to method call: expected " + decl.getArgumentCount() + " got " + actualArgCount);
 			} else {
 				// Size is correct, check types
 				int i = 0;
 				for (Iterator<PExpression> args = node.getArguments().iterator(); args.hasNext();) {
 					PExpression arg = args.next();
 					Type argType = typeDecorations.get(arg);
 					Type correctArgType = decl.getArgumentTypes().get(i);
 					if (!correctArgType.compatibleWith(argType)) {
 						reportError(node.getMethod(), "Wrong type encountered in method call at argument " + (i+1) + ": expected '" + correctArgType.getName() + "' found '" + argType.getName() + "'");
 					}
 					i++;
 				}
 			}
 			returnType = decl.getType();
 		}
 		
 		typeDecorations.put(node, returnType);
 	}
 
 	@Override
 	public void inAClassDef(AClassDef node) {
 		currentClassName = node.getBeginName().getText();
 		currentClassDeclaration = new ClassDeclaration(Type.getType(currentClassName), locationFor(node.getBeginName()), currentClassName);
 		classTable.put(currentClassName, currentClassDeclaration);
 		
 		// Verify that the naming on both ends is the same
 		if (!currentClassName.equals(node.getEndName().getText())) {
 			reportError(node.getEndName(), "Expected " + currentClassName + " ending the class definition, but instead found " + node.getEndName().getText());
 		}
 		
 		// Inheritance
 		String parentClassName = node.getExtends() != null ? node.getExtends().getText() : "";
 		if (parentClassName.equals("") && !currentClassName.equals("ood")) {
 			parentClassName = "ood";
 		}
 		ClassDeclaration parent = classTable.get(parentClassName);
 		
 		// Verify that the inheritance is from a previously defined class
 		if (parent == null && !currentClassName.equals("ood")) {
 			reportError(node.getExtends(), "Class '" + currentClassName + "' cannot extend as as yet undefined class '" + parentClassName + "'");
 			parent = classTable.get("ood");
 		}
 		
 		currentClassDeclaration.inheritFrom(parent);
 		
 		symbolTable.push(currentClassName, currentClassDeclaration);
 		
 		// Special externally linked methods
 		if (currentClassName.equals("Reader")) {
 			MethodDeclaration decl = new MethodDeclaration(Type.getType("int"), "SYSTEM_DECLARED", new ArrayList<Type>(), currentClassDeclaration, "io_read");
 			currentClassDeclaration.addMethod("io_read", decl);
 		} else if (currentClassName.equals("Writer")) {
 			ArrayList<Type> outTypes = new ArrayList<Type>();
 			outTypes.add(Type.getType("int"));
 			MethodDeclaration decl = new MethodDeclaration(Type.getType("void"), "SYSTEM_DECLARED", outTypes, currentClassDeclaration, "io_write");
 			currentClassDeclaration.addMethod("io_write", decl);
 		}
 		
 		// Reset processing
 		currentMethodDeclaration = null;
 		symbolTable.beginScope();
 	}
 
 	@Override
 	public void outAClassDef(AClassDef node) {
 		symbolTable.endScope();
 	}
 
 	@Override
 	public void outAComparisonExpression(AComparisonExpression node) {
 		Type result = Type.getType("error");
 		Type lt = typeDecorations.get(node.getExpr1());
 		Type rt = typeDecorations.get(node.getExpr2());
 		Token token = null;
 		if (node.getOperator() instanceof AGreaterOperator) {
 			token = ((AGreaterOperator)node.getOperator()).getOp();
 		} else if (node.getOperator() instanceof AGreaterEqualOperator) {
 			token = ((AGreaterEqualOperator)node.getOperator()).getOp();
 		} else if (node.getOperator() instanceof AEqualOperator) {
 			token = ((AEqualOperator)node.getOperator()).getOp();
 		}
 
 		if (lt.compatibleWith(rt)) {
 			// Types match, now check to see that they are both integers or strings
 			Type integer = Type.getType("int");
 			Type string = Type.getType("string");
 			if (!(lt == integer || lt == string || node.getOperator() instanceof AEqualOperator)) { // only need to check one side since we already know they are the same type
 				reportError(token, "Expected expressions of type 'integer' or 'string' in comparison got '" + lt.getName() + "' at operator " + token.getText());
 			} else {
 				result = Type.getType("boolean");
 			}
 		} else {
 			reportError(token, "Found expressions of mismatched types ('" + lt.getName() + "' and '" + rt.getName() + "') at operator " + token.getText());
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAConcatenationExpression(AConcatenationExpression node) {
 		Type result = Type.getType("error");
 		Type lt = typeDecorations.get(node.getExpr1());
 		Type rt = typeDecorations.get(node.getExpr2());
 		Token token = node.getConcatOp();
 
 		if (lt.compatibleWith(rt)) {
 			// Types match, now check to see that they are both strings
 			Type expectedType = Type.getType("string");
 			if (lt != expectedType) { // only need to check one side since we already know they are the same type
 				reportError(token, "Expected expressions of type '" + expectedType.getName() + "' got '" + lt.getName() + "' at operator " + token.getText());
 			} else {
 				result = lt;
 			}
 		} else {
 			reportError(token, "Found expressions of mismatched types ('" + lt.getName() + "' and '" + rt.getName() + "') at operator " + token.getText());
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAIdentifierExpression(AIdentifierExpression node) {
 		Type result = Type.getType("error");
 		
 		Symbol symbol = symbolTable.lookup(node.getId().getText());
 		if (symbol == null) {
 			reportError(node.getId(), "Attempted to used undeclared variable '" + node.getId().getText() + "' in expression");
 		} else {
 			Declaration decl = symbol.getDeclaration();
 			if (decl instanceof VariableDeclaration) {
 				result = decl.getType();
 			} else {
 				reportError(node.getId(), "Attempted to use a non-variable identifier in an expression context");
 			}
 		}
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAIfStatement(AIfStatement node) {
 		Type result = Type.getType("error");
 		Type actualType = typeDecorations.get(node.getExpression());
 		Type expectedType = Type.getType("boolean");
 		Token token = node.getIf();
 		
 		if (expectedType.compatibleWith(actualType)) {
 			result = actualType;
 		} else {
 			reportError(token, "Expected expression of type '" + expectedType.getName() + "' got '" + actualType.getName() + "' in if conditional expression");
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAIntegerExpression(AIntegerExpression node) {
 		typeDecorations.put(node, Type.getType("int"));
 	}
 
 	@Override
 	public void outALoopStatement(ALoopStatement node) {
 		Type result = Type.getType("error");
 		Type actualType = typeDecorations.get(node.getCase());
 		Type expectedType = Type.getType("boolean");
 		Token token = node.getLoop();
 		
 		if (expectedType.compatibleWith(actualType)) {
 			result = actualType;
 		} else {
 			reportError(token, "Expected expression of type '" + expectedType.getName() + "' got '" + actualType.getName() + "' in loop conditional expression");
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void inAMethodDeclaration(AMethodDeclaration node) {
 		String name = node.getBeginName().getText();
 		
 		// Get the parameter types
 		ArrayList<Type> argumentTypes = new ArrayList<Type>();
 		for (Iterator<PArgumentDeclaration> i = node.getArgumentDeclaration().iterator(); i.hasNext();) {
 			AArgumentDeclaration arg = (AArgumentDeclaration)i.next();
 			argumentTypes.add( typeFor(arg.getType()) );
 		}
 		
 		curArgCount = 0;
 		
 		// Verify that the naming on both ends is the same
 		if (!name.equals(node.getEndName().getText())) {
 			reportError(node.getEndName(), "Expected " + name + " ending the method declaration, but instead found " + node.getEndName().getText());
 		}
 		
 		// Push the method declaration onto the symbol table
 		MethodDeclaration declaration = new MethodDeclaration( typeFor(node.getType()), locationFor(node.getBeginName()), argumentTypes, currentClassDeclaration, name );
 		MethodDeclaration previousDeclaration = currentClassDeclaration.getMethod(name);
 		if (previousDeclaration != null && previousDeclaration.getKlass() == currentClassDeclaration) {
 			reportError(node.getBeginName(), "Method '" + node.getBeginName().getText() + "' previously defined at " + previousDeclaration.getLocation() + ".");
 		}
 		symbolTable.push(node.getBeginName().getText(), declaration);
 		currentMethodDeclaration = declaration;
 		currentClassDeclaration.addMethod(name, declaration);
 		
 		// Increment the scope
 		symbolTable.beginScope();
 		
 		// Add the method name as a variable (for assigning the return value)
 		VariableDeclaration returnDecl = new VariableDeclaration( typeFor(node.getType()), locationFor(node.getBeginName()) );
 		currentMethodDeclaration.incrementLocalCount();
 		returnDecl.setLocalPosition(currentMethodDeclaration.getLocalCount());
 		currentMethodDeclaration.addVariable(name, returnDecl);
 		
 
 		// Add the self argument to the method's variables (as argument)
 		VariableDeclaration self = new VariableDeclaration( currentClassDeclaration.getType(), locationFor(node.getBeginName()) );
 		self.setArgumentPosition(curArgCount);
 		currentMethodDeclaration.addVariable("me", self);
 		curArgCount += 1;
 	}
 
 	@Override
 	public void outAMethodDeclaration(AMethodDeclaration node) {
 		symbolTable.endScope();
 	}
 
 	@Override
 	public void outAMultExpression(AMultExpression node) {
 		Type result = Type.getType("error");
 		Type lt = typeDecorations.get(node.getExpr1());
 		Type rt = typeDecorations.get(node.getExpr2());
 		Token token = (node.getOperator() instanceof AMultOperator ? ((AMultOperator)node.getOperator()).getOp() : ((ADivOperator)node.getOperator()).getOp());
 
 		if (lt.compatibleWith(rt)) {
 			// Types match, now check to see that they are both integers
 			Type expectedType = Type.getType("int");
 			if (lt != expectedType) { // only need to check one side since we already know they are the same type
 				reportError(token, "Expected expressions of type '" + expectedType.getName() + "' got '" + lt.getName() + "' at operator " + token.getText());
 			} else {
 				result = lt;
 			}
 		} else {
 			reportError(token, "Found expressions of mismatched types ('" + lt.getName() + "' and '" + rt.getName() + "') at operator " + token.getText());
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outANullExpression(ANullExpression node) {
 		typeDecorations.put(node, Type.getType("null"));
 	}
 
 	@Override
 	public void outAOrExpression(AOrExpression node) {
 		Type result = Type.getType("error");
 		Type lt = typeDecorations.get(node.getExpr1());
 		Type rt = typeDecorations.get(node.getExpr2());
 		Token token = node.getOr();
 
 		if (lt.compatibleWith(rt)) {
 			// Types match, now check to see that they are both booleans
 			Type expectedType = Type.getType("boolean");
 			if (lt != expectedType) { // only need to check one side since we already know they are the same type
 				reportError(token, "Expected expressions of type '" + expectedType.getName() + "' got '" + lt.getName() + "' at operator " + token.getText());
 			} else {
 				result = lt;
 			}
 		} else {
 			reportError(token, "Found expressions of mismatched types ('" + lt.getName() + "' and '" + rt.getName() + "') at operator " + token.getText());
 		}
 		
 		typeDecorations.put(node, result);
 	}
 
 	@Override
 	public void outAStringExpression(AStringExpression node) {
 		typeDecorations.put(node, Type.getType("string"));
 	}
 
 	@Override
 	public void outATrueExpression(ATrueExpression node) {
 		typeDecorations.put(node, Type.getType("boolean"));
 	}
 
 	@Override
 	public void outAUnaryExpression(AUnaryExpression node) {
 		Type result = Type.getType("error");
 		Type actualType = typeDecorations.get(node.getExpression());
 		Type expectedType = null;
 		Token token = null;
 		
 		if (node.getOperator() instanceof ANotOperator) {
 			expectedType = Type.getType("boolean");
 			token = ((ANotOperator)node.getOperator()).getOp();
 		} else if (node.getOperator() instanceof AMinusOperator) {
 			expectedType = Type.getType("int");
 			token = ((AMinusOperator)node.getOperator()).getOp();
 		} else if (node.getOperator() instanceof APlusOperator) {
 			expectedType = Type.getType("int");
 			token = ((APlusOperator)node.getOperator()).getOp();
 		}
 		
 		
 		if (expectedType.compatibleWith(actualType)) {
 			result = actualType;
 		} else {
 			reportError(token, "Expected expression of type '" + expectedType.getName() + "' got '" + actualType.getName() + "' at operator " + token.getText());
 		}
 		
 		typeDecorations.put(node, result);
 	}
 	
 	@Override
 	public void outAVarDeclaration(AVarDeclaration node) {
 		VariableDeclaration decl = new VariableDeclaration( typeFor(node.getType()), locationFor(node.getName()) );
 		Symbol previousDeclaration = symbolTable.scopeContains(node.getName().getText());
 		if (previousDeclaration != null && !(previousDeclaration.getDeclaration() instanceof VariableDeclaration && ((VariableDeclaration)previousDeclaration.getDeclaration()).getDeclaringKlass() != null && ((VariableDeclaration)previousDeclaration.getDeclaration()).getDeclaringKlass() != currentClassDeclaration)) {
 			reportError(node.getName(), "Variable '" + node.getName().getText() + "' previously defined at "  + previousDeclaration.getDeclaration().getLocation());
 		}
 		symbolTable.push(node.getName().getText(), decl);
 		
 		if (currentMethodDeclaration != null) {
 			currentMethodDeclaration.incrementLocalCount();
 			decl.setLocalPosition(currentMethodDeclaration.getLocalCount());
 			currentMethodDeclaration.addVariable(node.getName().getText(), decl);
 		} else {
 			decl.setInstancePosition(currentClassDeclaration.getInstanceVariableCount());
 			currentClassDeclaration.addVariable(node.getName().getText(), decl);
 		}
 		
 		decl.setDeclaringKlass(currentClassDeclaration);
 	}
 
 	@Override
 	public void outAFalseExpression(AFalseExpression node) {
 		typeDecorations.put(node, Type.getType("boolean"));
 	}
 
 	@Override
 	public void outAMeExpression(AMeExpression node) {
 		typeDecorations.put(node, Type.getType(currentClassName));
 	}
 
 	@Override
 	public void outANewObjectExpression(ANewObjectExpression node) {
 		typeDecorations.put(node, typeFor(node.getType()));
 	}
 
 	@Override
 	public void inAConcatenationExpression(AConcatenationExpression node) {
 		reportError(node.getConcatOp(), "String concatenation not yet supported");
 	}
 
 	@Override
 	public void inAStringExpression(AStringExpression node) {
 		typeDecorations.put(node, Type.getType("String"));
 	}
 
 	@Override
 	public void inAStringType(AStringType node) {
 		//reportError(node.getString(), "Strings not yet supported");
 	}
 
 	public Integer getErrorCount() {
 		return this.errorCount;
 	}
 	
 }
