 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.compiler;
 
 import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.declarations.Argument;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Expression;
 import org.eclipse.dltk.ast.expressions.Literal;
 import org.eclipse.dltk.ast.expressions.StringLiteral;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.compiler.IElementRequestor.TypeInfo;
 
 public class SourceElementRequestVisitor extends ASTVisitor {
 
 	protected IElementRequestor fRequestor = null;
 
 	protected boolean fInClass = false; // if we are in class
 	protected boolean fInMethod = false; // if we are in method
 
 	protected TypeDeclaration fCurrentClass = null;
 	protected MethodDeclaration fCurrentMethod = null;
 
 	// Used to hold visited nodes in depth
 	protected Stack<ASTNode> fNodes = new Stack<ASTNode>();
 
 	/**
 	 * @since 2.0
 	 */
 	public SourceElementRequestVisitor(IElementRequestor requesor) {
 		this.fRequestor = requesor;
 	}
 
 	/**
 	 * @return method node that encloses current element, or <code>null</code>
 	 *         if there's no one.
 	 */
 	public MethodDeclaration getCurrentMethod() {
 		return this.fCurrentMethod;
 	}
 
 	/**
 	 * @return class node that encloses current element, or <code>null</code> if
 	 *         there's no one.
 	 */
 	protected TypeDeclaration getCurrentClass() {
 		return this.fCurrentClass;
 	}
 
 	protected String makeLanguageDependentValue(ASTNode expr) {
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * Called then end of methd are visited. Method are added to model before
 	 * this call is done.
 	 */
 	protected void onEndVisitMethod(MethodDeclaration method) {
 
 	}
 
 	protected String[] processSuperClasses(TypeDeclaration type) {
 		List names = type.getSuperClassNames();
 		if (names == null) {
 			return null;
 		}
 		if (names.isEmpty()) {
 			return null;
 		}
 
 		return (String[]) names.toArray(new String[names.size()]);
 	}
 
 	/**
 	 * Called then end of class are visited. Class are added to model before
 	 * this call is done.
 	 */
 	protected void onEndVisitClass(TypeDeclaration type) {
 	}
 
 	/**
 	 * Called before method info is propagated to the source element requestor.
 	 * This is a last chance to modify the method info
 	 * 
 	 * @since 2.0
 	 */
 	protected void modifyMethodInfo(MethodDeclaration methodDeclaration,
 			ISourceElementRequestor.MethodInfo mi) {
 	}
 
 	/**
 	 * Called before method info is propagated to the source element requestor.
 	 * This is a last chance to modify the class info
 	 * 
 	 * @since 2.0
 	 */
 	protected void modifyClassInfo(TypeDeclaration typeDeclaration, TypeInfo ti) {
 	}
 
 	/**
 	 * Creates correct string value from expression. For example for
 	 * StringLiteral returns "value". And so on.
 	 * 
 	 * Return "" if it is imposible to make value from expression.
 	 * 
 	 * @param expr
 	 * @return
 	 */
 	protected String makeValue(ASTNode stmt) {
 		// if (!(stmt instanceof Expression))
 		// return null;
 
 		String value = ""; //$NON-NLS-1$
 		if (stmt instanceof StringLiteral) {
 			value = "\"" + ((StringLiteral) stmt).getValue() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
 		} else if (stmt instanceof Literal) {
 			value = ((Literal) stmt).getValue();
 		} else /* if (stmt instanceof ExtendedVariableReference) */{
 			// If it is Dot.
 			// Lets make recursive value parsing in this case.
 			value += this.makeLanguageDependentValue(stmt);
 		}
 
 		return value;
 	}
 
 	public boolean endvisit(MethodDeclaration method) throws Exception {
 		this.fRequestor.exitMethod(method.sourceEnd());
 		this.fInMethod = false;
 		this.fCurrentMethod = null;
 
 		this.onEndVisitMethod(method);
 
 		this.fNodes.pop();
 		return true;
 	}
 
 	public boolean endvisit(TypeDeclaration type) throws Exception {
 		this.fRequestor.exitType(type.sourceEnd());
 		this.fInClass = false;
 		this.fCurrentClass = null;
 
 		this.onEndVisitClass(type);
 
 		this.fNodes.pop();
 		return true;
 	}
 
 	public boolean visit(MethodDeclaration method) throws Exception {
 		this.fNodes.push(method);
 		List args = method.getArguments();
 
 		String[] parameter = new String[args.size()];
 		String[] initializers = new String[args.size()];
 		for (int a = 0; a < args.size(); a++) {
 			Argument arg = (Argument) args.get(a);
 			parameter[a] = arg.getName();
			if(arg.getInitialization() != null){
				if (arg.getInitialization() instanceof Literal) {
					Literal scalar = (Literal) arg.getInitialization();
					initializers[a] = scalar.getValue();
				}
			}
 		}
 
 		ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 		mi.parameterNames = parameter;
 		mi.name = method.getName();
 		mi.modifiers = method.getModifiers();
 		mi.nameSourceStart = method.getNameStart();
 		mi.nameSourceEnd = method.getNameEnd() - 1;
 		mi.declarationStart = method.sourceStart();
 		mi.parameterInitializers = initializers;
 
 		modifyMethodInfo(method, mi);
 
 		this.fRequestor.enterMethod(mi);
 
 		this.fInMethod = true;
 		this.fCurrentMethod = method;
 		return true;
 	}
 
 	public boolean visit(TypeDeclaration type) throws Exception {
 		this.fNodes.push(type);
 
 		ISourceElementRequestor.TypeInfo info = new ISourceElementRequestor.TypeInfo();
 		info.modifiers = type.getModifiers();
 		info.name = type.getName();
 		info.nameSourceStart = type.getNameStart();
 		info.nameSourceEnd = type.getNameEnd() - 1;
 		info.declarationStart = type.sourceStart();
 		info.superclasses = this.processSuperClasses(type);
 
 		modifyClassInfo(type, info);
 
 		this.fRequestor.enterType(info);
 
 		this.fInClass = true;
 		this.fCurrentClass = type;
 
 		return true;
 	}
 
 	public boolean endvisit(ModuleDeclaration declaration) throws Exception {
 		this.fRequestor.exitModule(declaration.sourceEnd());
 		this.fNodes.pop();
 		return true;
 	}
 
 	public boolean visit(ModuleDeclaration declaration) throws Exception {
 		this.fNodes.push(declaration);
 		this.fRequestor.enterModule();
 		return true;
 	}
 
 	public boolean endvisit(Expression expression) throws Exception {
 		this.fNodes.pop();
 		return true;
 	}
 
 	public boolean endvisit(Statement statement) throws Exception {
 		this.fNodes.pop();
 		return true;
 	}
 
 	public boolean visit(Expression expression) throws Exception {
 		this.fNodes.push(expression);
 		return true;
 	}
 
 	public boolean visit(Statement statement) throws Exception {
 		this.fNodes.push(statement);
 		return true;
 	}
 }
