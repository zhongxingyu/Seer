 package parser;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 import models.CallGraph;
 import models.Clazz;
 import models.Exprezzion;
 import models.File;
 import models.Mapping;
 import models.Method;
 
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.BooleanLiteral;
 import org.eclipse.jdt.core.dom.CastExpression;
 import org.eclipse.jdt.core.dom.CharacterLiteral;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.Block;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.IVariableBinding;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.Name;
 import org.eclipse.jdt.core.dom.NullLiteral;
 import org.eclipse.jdt.core.dom.NumberLiteral;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
 
 import callgraphanalyzer.Mappings;
 
 public class Visitor extends ASTVisitor {
 	
 	private CallGraph callGraph;
 	
 	private File file;
 	private Stack<Clazz> clazzStack;
 	private Clazz currentClazz;
 	private Method currentMethod;
 	private Mappings mappings = new Mappings();
 	
 	public Visitor(CallGraph callGraph, String fileName) {
 		this.callGraph = callGraph;
 		clazzStack = new Stack<Clazz>();
 		
 		file = new File();
 		file.setFileName(fileName);
 	}
 	
 	/**
 	 * This function overrides what to do when the AST visitor 
 	 * encounters a package declaration
 	 */
 	@Override
 	public boolean visit(PackageDeclaration node) {
 		if (file.getFilePackage() != null)
 			System.err.println("ERROR: StructuredFile.java there is more than one package, using first visited");
 		else 
 			file.setFilePackage(node.getName().getFullyQualifiedName()); 
 
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when the AST visitor
 	 * encounters an import declaration
 	 */
 	@Override
 	public boolean visit(ImportDeclaration node) {
 		String imp= node.getName().getFullyQualifiedName();
 		file.addFileImport(imp);
 
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when the AST visitor
 	 * encounters a type declaration (This is mostly when 
 	 * you define a class in the Java file)
 	 */
 	@Override
 	public boolean visit(TypeDeclaration node) {
 		currentClazz = callGraph.containsClazz(file.getFilePackage() + "." + node.getName().getIdentifier());
 		if(currentClazz == null) {
 			Clazz clazz = new Clazz();
 			clazzStack.push(clazz);
 			currentClazz = clazz;
 		}
 		
 		currentClazz.setName(file.getFilePackage() + "." + node.getName().getIdentifier());
 		currentClazz.setInterface(node.isInterface());
 		
 		Type t = node.getSuperclassType();
 		if(t != null)
 			currentClazz.addUnresolvedSuperClazz(t.toString());
 		
 		List<Type> interfaces = node.superInterfaceTypes();
 		for(Type i: interfaces)
 			currentClazz.addUnresolvedInterface(i.toString());
 	
 		mappings.newMap();
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when we reach
 	 * the end of visiting a class. (Add the class to 
 	 * the call graph as we have finished parsing it)
 	 */
 	@Override
 	public void endVisit(TypeDeclaration node) {
 		// Add it to the current file
 		clazzStack.peek().setFile(file);
 		// Add to call graph
 		callGraph.addClazz(clazzStack.peek());
 		// Add to file
 		// Add to file - interfaces if needed
 		if(clazzStack.peek().isInterface())
 			file.addInterface(clazzStack.peek());
 		file.addClazz(clazzStack.pop());
 		mappings.removeMap();
 	}
 	
 	/**
 	 * This function overrides what to do when we reach
 	 * a method declaration inside a class.
 	 */
 	@Override
 	public boolean visit(MethodDeclaration node) {
 		// Get the method's parameter
 		List<SingleVariableDeclaration> parametersList =  node.parameters();
 		List<String> parameters = new ArrayList<String>();
 		for(SingleVariableDeclaration v: parametersList) {
 			parameters.add(v.getType().toString());
 		}
 		
 		// Get the unique name
 		String uniqueMethod = currentClazz.getName() + "." + node.getName().getIdentifier()+ "(";
 		for(String s: parameters)
 			uniqueMethod += s + ", ";
 		if(!parameters.isEmpty())
 			uniqueMethod = uniqueMethod.substring(0, uniqueMethod.length()-2);
 		uniqueMethod += ")";
 		
 		currentMethod = callGraph.containsMethod(uniqueMethod);
 		if(currentMethod == null) {
 			Method m = new Method();
 			currentMethod = m;
 		}
 		currentMethod.setName(uniqueMethod);
 		currentMethod.setClazz(currentClazz);
 		currentMethod.setStartLine(node.getStartPosition());
 		currentMethod.setEndLine(node.getLength() + currentMethod.getStartLine());
 		if(node.getReturnType2() != null)
 			currentMethod.setReturnType(node.getReturnType2().toString());
 
 		return super.visit(node);
 	}
 	
 	/**
 	 * This function overrides what to do when we reach
 	 * a method invocation statement
 	 */
 	@Override
 	public boolean visit(MethodInvocation node) {
		if (currentMethod == null)
			return super.visit(node);
 		// Check to see if there is an expression out front of the invocation
 		String exp;
 		String methodCall;
 		List<Exprezzion> parameters;
 		String resolvedType = "";
 		if(node.getExpression() != null)
 		{
 			System.out.println("-----------------");
 			System.out.println("Expression: " + node.getExpression().toString());
 			System.out.println("Is a Name.");
 			exp = node.getExpression().toString();
 			methodCall = node.getName().getIdentifier();
 			System.out.println("Method: " + node.getName().getIdentifier());
 			parameters = parseMethodParameters(node);

 			currentMethod.addUnresolvedExprezzion(exp, methodCall, parameters, resolvedType);
 			
 			if(node.getExpression() instanceof Name) {
 				exp = node.getExpression().toString();
 				resolvedType = mappings.lookupType(exp);
 				currentMethod.addUnresolvedExprezzion(exp, "", new ArrayList<Exprezzion>(), resolvedType);
 			}
 		}
 		else
 		{
 			// This means we have a local function call
 			System.out.println("-----------------");
 			System.out.println("Method: " + node.getName().getIdentifier());
 			exp = "";
 			methodCall = node.getName().getIdentifier();
 			parameters = parseMethodParameters(node);
 			
 			currentMethod.addUnresolvedExprezzion(exp, methodCall, parameters, resolvedType);
 		}
 		
 		return super.visit(node);
 	}
 	
 	
 	public List<Exprezzion> parseMethodParameters(MethodInvocation node) {
 		List<Exprezzion> exprezzions = new ArrayList<Exprezzion>();
 		
 		// Temp variables
 		String exp;
 		String methodCall;
 		List<Exprezzion> parameters;
 		String resolvedType = "";
 		
 		List<Expression> expressions = node.arguments();
 		int paramNum = 0;
 		for(Expression expression: expressions) {
 			// 3 Cases
 			if(expression instanceof Name)
 			{
 				exp = expression.toString();
				resolvedType = mappings.lookupType(expression.toString());
 				methodCall = "";
 				parameters = new ArrayList<Exprezzion>();
 			}
 			else if(expression instanceof MethodInvocation) {
 				// Handle if the method invocation has an expression
 				if(((MethodInvocation)expression).getExpression() != null) {
 					exp = ((MethodInvocation)expression).getExpression().toString();
 				}
 				else
 					exp = "";
 				methodCall = ((MethodInvocation)expression).getName().getIdentifier();
 				parameters = parseMethodParameters(((MethodInvocation)expression));
 			}
 			else {
 				// The parameter must be some kind of literal
 				exp = expression.toString();
 				methodCall = "";
 				parameters = new ArrayList<Exprezzion>();
 				resolvedType = resolveLiteralType(expression);
 			}
 			// Add the new parameter
 			exprezzions.add(new Exprezzion(exp, methodCall, parameters, resolvedType));
 			System.out.println("Argument: " + expression.toString());
 			paramNum++;
 		}
 		
 		return exprezzions;
 	}
 	
 	/**
 	 * This function will return a string that corresponds to the
 	 * type of the literal in the expression it is passed.
 	 * @param expression
 	 * @return
 	 */
 	private String resolveLiteralType(Expression expression) {
 		String literal = "";
 		
 		if(expression instanceof BooleanLiteral)
 			literal = "boolean";
 		else if(expression instanceof CharacterLiteral)
 			literal = "char";
 		else if(expression instanceof NullLiteral)
 			literal = "null";
 		else if(expression instanceof NumberLiteral || expression instanceof CastExpression)
 			literal = resolveNumberLiteral(expression);
 		else if(expression instanceof StringLiteral)
 			literal = "String";
 		
 		return literal;
 	}
 	
 	private String resolveNumberLiteral(Expression expression) {
 		
 		if(expression instanceof CastExpression) {
 			return ((CastExpression)expression).getType().toString();
 		}
 		
 		NumberLiteral number = (NumberLiteral)expression;
 		if(number.getToken().contains("F") || number.getToken().contains("f"))
 			return "float";
 		if(number.getToken().contains("D") || number.getToken().contains("d") || 
 				number.getToken().contains("E") || number.getToken().contains("e"))
 			return "double";
 		if(number.getToken().contains("L") || number.getToken().contains("l"))
 			return "long";
 		if(!number.getToken().contains("."))
 			return "int";
 		else
 			return "double";
 	}
 	
 	/**
 	 * This function overrides what to do when we reach
 	 * a class instance creation statement
 	 */
 	@Override
 	public boolean visit(ClassInstanceCreation node) {
 		// SHOULD BE SAME LOGIC AS WHAT TO DO WHEN WE FIND METHOD INVOCATION
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(VariableDeclarationExpression node)
 	{
 		SimpleName varName = null;
 		for (Iterator<VariableDeclarationFragment> i = node.fragments().iterator();i.hasNext();)
 		{
 			VariableDeclarationFragment frag = (VariableDeclarationFragment)i.next();
 			varName = frag.getName();
 		}
 		Mapping m = new Mapping(node.getType().toString(), varName.getFullyQualifiedName());
 		mappings.addMapping(node.fragments().get(0).toString(), m);
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(VariableDeclarationStatement node)
 	{
 		SimpleName varName = null;
 		for (Iterator<VariableDeclarationFragment> i = node.fragments().iterator();i.hasNext();)
 		{
 			VariableDeclarationFragment frag = (VariableDeclarationFragment)i.next();
 			varName = frag.getName();
 		}
 		Mapping m = new Mapping(node.getType().toString(), varName.getFullyQualifiedName());
 		mappings.addMapping(varName.getFullyQualifiedName(), m);
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(FieldDeclaration node)
 	{
 		SimpleName varName = null;
 		for (Iterator<VariableDeclarationFragment> i = node.fragments().iterator();i.hasNext();)
 		{
 			VariableDeclarationFragment frag = (VariableDeclarationFragment)i.next();
 			varName = frag.getName();
 		}
 		Mapping m = new Mapping(node.getType().toString(), varName.getFullyQualifiedName());
 		mappings.addMapping(varName.getFullyQualifiedName(), m);
 		return super.visit(node);
 	}
 	
 	@Override
 	public boolean visit(Block node)
 	{
 		mappings.newMap();
 		return super.visit(node);
 	}
 	
 	@Override 
 	public void endVisit(Block node)
 	{
 		mappings.removeMap();
 	}
 	
 	/**
 	 * This method overrides what to do when we reach
 	 * the end of a method declaration
 	 */
 	@Override
 	public void endVisit(MethodDeclaration node) {
 		// Add method to the call graph
 		callGraph.addMethod(currentMethod);
 		// Add method to the current class
 		clazzStack.peek().addMethod(currentMethod);
 	}
 	
 	public void commitFile() {
 		callGraph.addFile(file);
 	}
 }
