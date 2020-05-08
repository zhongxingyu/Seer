 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D. Willink - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: PrettyPrintExprVisitor.java,v 1.7 2011/05/13 18:41:43 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.prettyprint;
 
 import java.math.BigInteger;
 import java.util.List;
 
 import org.eclipse.ocl.examples.pivot.BooleanLiteralExp;
 import org.eclipse.ocl.examples.pivot.CallExp;
 import org.eclipse.ocl.examples.pivot.CollectionItem;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralPart;
 import org.eclipse.ocl.examples.pivot.CollectionRange;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.EnumLiteralExp;
 import org.eclipse.ocl.examples.pivot.ExpressionInOcl;
 import org.eclipse.ocl.examples.pivot.IfExp;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.IterateExp;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.IteratorExp;
 import org.eclipse.ocl.examples.pivot.LetExp;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.NullLiteralExp;
 import org.eclipse.ocl.examples.pivot.OclExpression;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.PropertyCallExp;
 import org.eclipse.ocl.examples.pivot.RealLiteralExp;
 import org.eclipse.ocl.examples.pivot.StringLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralPart;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypeExp;
 import org.eclipse.ocl.examples.pivot.UMLReflection;
 import org.eclipse.ocl.examples.pivot.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.VariableDeclaration;
 import org.eclipse.ocl.examples.pivot.VariableExp;
 import org.eclipse.ocl.examples.pivot.util.Visitable;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 /**
  * The PrettyPrintExprVisitor supports pretty printing of OCL expressions.
  * PrettyPrintOptions may be used to configure the printing.
  */
 public class PrettyPrintExprVisitor extends PrettyPrintNameVisitor
 {	
 	public static String prettyPrint(Visitable element) {
 		return prettyPrint(element, PrettyPrintTypeVisitor.createOptions(null));
 	}
 
 	public static String prettyPrint(Visitable element, Namespace namespace) {
 		return prettyPrint(element, PrettyPrintTypeVisitor.createOptions(namespace));
 	}
 
 	public static String prettyPrint(Visitable element, PrettyPrintOptions options) {
 		PrettyPrintNameVisitor visitor = new PrettyPrintExprVisitor(options);
 		try {
 			visitor.safeVisit(element);
 			return visitor.toString();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return visitor.toString() + " ... " + e.getClass().getName() + " - " + e.getLocalizedMessage();
 		}
 	}
 
 	private Precedence currentPrecedence = null;
 	
 	/**
 	 * Initializes me.
 	 */
 //	public PrettyPrintExprVisitor(Namespace scope) {
 //		super(new PrettyPrintOptions.Global(scope));
 //	}
 	public PrettyPrintExprVisitor(PrettyPrintOptions options) {
 		super(options);
 	}
 
 	protected void appendSourceNavigation(CallExp object) {
 		OclExpression source = object.getSource();
 		if (source != null) {
 			if (!(source instanceof VariableExp) || !((VariableExp)source).isImplicit()) {
				if ((source instanceof OperationCallExp)
				 && (((OperationCallExp)source).getReferredOperation() != null)
				 && (((OperationCallExp)source).getReferredOperation().getPrecedence() != null)) {
 					delegate.append("(");
 					precedenceVisit(source, null);
 					delegate.append(")");
 				}
 				else {
 					safeVisit(source);
 				}
 				if (source.getType() instanceof CollectionType) {
 					delegate.append(object.isImplicit() ? "." : "->");				// "." for implicit collect
 				}
 				else {
 					if (!object.isImplicit()) {
 						delegate.append(".");
 					}
 				}
 			}
 		}
 	}
 
 	protected void precedenceVisit(OclExpression expression, Precedence newPrecedence) {
 		Precedence savedPrecedcence = currentPrecedence;
 		try {
 			currentPrecedence = newPrecedence;
 			safeVisit(expression);
 		}
 		finally {
 			currentPrecedence = savedPrecedcence;
 		}
 	}
 
 	@Override
 	public Object visitBooleanLiteralExp(BooleanLiteralExp object) {
 		delegate.append(Boolean.toString(object.isBooleanSymbol()));
 		return null;
 	}
 
 	@Override
 	public Object visitCollectionItem(CollectionItem object) {
 		safeVisit(object.getItem());
 		return null;
 	}
 
 	@Override
 	public Object visitCollectionLiteralExp(CollectionLiteralExp object) {
 		delegate.appendName(object.getType(), context.getReservedNames());
 		List<CollectionLiteralPart> parts = object.getParts();
 		if (parts.isEmpty()) {
 			delegate.append("{}");
 		}
 		else {
 			delegate.push("{", "");
 			String prefix = ""; //$NON-NLS-1$
 			for (CollectionLiteralPart part : parts) {
 				delegate.append(prefix);
 				safeVisit(part);
 				prefix = ", ";
 			}
 			delegate.exdent("", "}", "");
 			delegate.pop();
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitCollectionRange(CollectionRange object) {
 		safeVisit(object.getFirst());
 		delegate.next("", "..", "");
         safeVisit(object.getLast());
 		return null;
 	}
 
 	@Override
 	public Object visitConstraint(Constraint object) {
 		String stereotype = object.getStereotype();
 		if (UMLReflection.BODY.equals(stereotype)) {
 			delegate.append("body");
 		}
 		else if (UMLReflection.DERIVATION.equals(stereotype)) {
 			delegate.append("der");
 		}
 		else if (UMLReflection.INITIAL.equals(stereotype)) {
 			delegate.append("init");
 		}
 		else if (UMLReflection.INVARIANT.equals(stereotype)) {
 			delegate.append("inv");
 		}
 		else if (UMLReflection.POSTCONDITION.equals(stereotype)) {
 			delegate.append("post");
 		}
 		else if (UMLReflection.PRECONDITION.equals(stereotype)) {
 			delegate.append("pre");
 		}
 		else {
 			delegate.append(stereotype);
 		}
 		if (object.getName() != null) {
 			delegate.append(" ");
 			delegate.appendName(object);
 		}
 		delegate.push(":", " ");
         safeVisit(object.getSpecification());
 		delegate.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitEnumLiteralExp(EnumLiteralExp object) {
 		safeVisit(object.getReferredEnumLiteral());
 		return null;
 	}
 
 	@Override
 	public Object visitExpressionInOcl(ExpressionInOcl object) {
 		safeVisit(object.getBodyExpression());
 		return null;
 	}
 
 	@Override
 	public Object visitIfExp(IfExp object) {
 		delegate.push("if", " ");
 		safeVisit(object.getCondition());
 		delegate.exdent(" ", "then", " ");
 		safeVisit(object.getThenExpression());
 		delegate.exdent(" ", "else", " ");
         safeVisit(object.getElseExpression());
 		delegate.exdent(" ", "endif", "");
 		delegate.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitIntegerLiteralExp(IntegerLiteralExp object) {
 		delegate.append(object.getIntegerSymbol());
 		return null;
 	}
 
 	@Override
 	public Object visitInvalidLiteralExp(InvalidLiteralExp object) {
 		delegate.append("invalid");
 		return null;
 	}
 
 	@Override
 	public Object visitIterateExp(IterateExp object) {
 		List<Variable> iterators = object.getIterators();
 		Operation referredOperation = object.getReferredIteration();
 		appendSourceNavigation(object);
 		delegate.appendName(referredOperation);
 		delegate.push("(", "");
 		String prefix = null;
 		if (iterators.size() > 0) {
 			boolean hasExplicitIterator = false;
 			for (Variable iterator : iterators) {
 				if (!iterator.isImplicit()) {
 					if (prefix != null) {
 						delegate.next(null, prefix, " ");
 					}
 					safeVisit(iterator);
 					prefix = ",";
 					hasExplicitIterator = true;
 				}
 			}
 			if (hasExplicitIterator) {
 				prefix = ";";
 			}
 			if (prefix != null) {
 				delegate.next(null, prefix, " ");
 			}
 			safeVisit(object.getResult());
 			delegate.next(null, " |", " ");
 		}
 		safeVisit(object.getBody());
 		delegate.next("", ")", "");
 		delegate.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitIteratorExp(IteratorExp object) {
 		Iteration referredIteration = object.getReferredIteration();
 		List<Variable> iterators = object.getIterators();
 		appendSourceNavigation(object);
 		if (object.isImplicit()) {
 			assert referredIteration.getName().equals("collect");
 			assert iterators.size() == 1;
 			safeVisit(object.getBody());
 		}
 		else {
 			delegate.appendName(referredIteration);
 			delegate.push("(", "");
 			if (iterators.size() > 0) {
 				String prefix = null;
 				boolean hasExplicitIterator = false;
 				for (Variable iterator : iterators) {
 					if (!iterator.isImplicit()) {
 						if (prefix != null) {
 							delegate.next(null, prefix, " ");
 						}
 						safeVisit(iterator);
 						prefix = ",";
 						hasExplicitIterator = true;
 					}
 				}
 				if (hasExplicitIterator) {
 					delegate.next(null, " |", " ");
 				}
 				else if (prefix != null) {
 					delegate.next(null, prefix, " ");
 				}
 			}
 			safeVisit(object.getBody());
 			delegate.next("", ")", "");
 			delegate.pop();
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitLetExp(LetExp object) {
 		delegate.push("let", " ");
 		safeVisit(object.getVariable());
 		delegate.exdent(" ", "in", " ");
         safeVisit(object.getIn());
 		delegate.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitNullLiteralExp(NullLiteralExp object) {
 		delegate.append("null");
 		return null;
 	}
 
 	@Override
 	public Object visitOclExpression(OclExpression object) {
 		delegate.append("<");
 		delegate.append(object.eClass().getName());
 		delegate.append(">");
 		return null;
 	}
 
 	@Override
 	public Object visitOpaqueExpression(OpaqueExpression object) {
 		delegate.append(PivotUtil.getBody(object));
 		return null;
 	}
 
 	@Override
 	public Object visitOperationCallExp(OperationCallExp object) {
 		OclExpression source = object.getSource();
 		List<OclExpression> arguments = object.getArguments();
 		Operation referredOperation = object.getReferredOperation();
 		Precedence precedence = referredOperation != null ? referredOperation.getPrecedence() : null;
 		if (precedence == null) {
 			appendSourceNavigation(object);
 			if (!object.isImplicit()) {
 				delegate.appendName(referredOperation);
 				delegate.push("(", "");
 				String prefix = null; //$NON-NLS-1$
 				for (OclExpression argument : arguments) {
 					if (prefix != null) {
 						delegate.next(null, prefix, " ");
 					}
 					precedenceVisit(argument, null);
 					prefix = ",";
 				}
 				delegate.next("", ")", "");
 				delegate.pop();
 			}
 		}
 		else {
 			boolean lowerPrecedence = (currentPrecedence != null) && precedence.getOrder().compareTo(currentPrecedence.getOrder()) > 0;
 			if (lowerPrecedence) {
 				delegate.push("(", null);
 			}
 			if (arguments.size() == 0) {			// Prefix
 				delegate.appendName(referredOperation, null);
 				if ((referredOperation != null) && PivotUtil.isValidIdentifier(referredOperation.getName())) {
 					delegate.append(" ");			// No space for unary minus
 				}
 				precedenceVisit(source, precedence);
 			}
 			else {			// Infix
 				precedenceVisit(source, precedence);
 				delegate.next(" ", delegate.getName(referredOperation, null), " ");
 				precedenceVisit(arguments.get(0), precedence);
 			}
 			if (lowerPrecedence) {
 				delegate.exdent("", ")", "");
 				delegate.pop();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitParameter(Parameter object) {
 		delegate.appendName(object);
 		Type type = object.getType();
 		if (type != null) {
 			delegate.append(" : ");
 			delegate.appendQualifiedName(type);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitPropertyCallExp(PropertyCallExp object) {
 		appendSourceNavigation(object);
 		delegate.appendName(object.getReferredProperty());
 		return null;
 	}
 
 	@Override
 	public Object visitRealLiteralExp(RealLiteralExp object) {
 		delegate.append(object.getRealSymbol());
 		return null;
 	}
 
 	@Override
 	public Object visitStringLiteralExp(StringLiteralExp object) {
 		delegate.append("'");
 		delegate.append(PivotUtil.convertToOCLString(object.getStringSymbol()));
 		delegate.append("'");
 		return null;
 	}
 
 	@Override
 	public Object visitTupleLiteralExp(TupleLiteralExp object) {
 		delegate.append("Tuple");
 		delegate.push("{", "");
 		String prefix = ""; //$NON-NLS-1$
 		for (TupleLiteralPart part : object.getParts()) {
 			delegate.append(prefix);
 			safeVisit(part);
 			prefix = ", ";
 		}
 		delegate.exdent("", "}", "");
 		delegate.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitTupleLiteralPart(TupleLiteralPart object) {
 		delegate.appendName(object);
 		delegate.append(" = ");
 		safeVisit(object.getInitExpression());
 		return null;
 	}
 
 	@Override
 	public Object visitTypeExp(TypeExp object) {
 		delegate.appendQualifiedName(object.getReferredType());
 		return null;
 	}
 
 	@Override
 	public Object visitUnlimitedNaturalLiteralExp(UnlimitedNaturalLiteralExp object) {
 		BigInteger symbol = object.getUnlimitedNaturalSymbol();
 		if (symbol.signum() < 0) {
 			delegate.append("*");
 		}
 		else {
 			delegate.append(symbol);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitVariable(Variable object) {
 		delegate.appendName(object);
 		Type type = object.getType();
 		if (type != null) {
 			delegate.append(" : ");
 			delegate.appendQualifiedName(type);
 		}
 		OclExpression initExpression = object.getInitExpression();
 		if (initExpression != null) {
 			delegate.append(" = ");
 			safeVisit(initExpression);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitVariableExp(VariableExp object) {
 		VariableDeclaration referredVariable = object.getReferredVariable();
 		if ((referredVariable != null) && "self".equals(referredVariable.getName())) {
 			delegate.appendName(referredVariable, null);
 		}
 		else {
 			delegate.appendName(referredVariable);
 		}
 		return null;
 	}
 }
