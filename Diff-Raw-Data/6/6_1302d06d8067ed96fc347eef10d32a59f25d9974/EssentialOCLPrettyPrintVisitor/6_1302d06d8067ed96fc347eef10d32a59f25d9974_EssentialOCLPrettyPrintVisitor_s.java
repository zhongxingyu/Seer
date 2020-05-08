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
 
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.ocl.examples.domain.values.Value;
 import org.eclipse.ocl.examples.pivot.BooleanLiteralExp;
 import org.eclipse.ocl.examples.pivot.CallExp;
 import org.eclipse.ocl.examples.pivot.CollectionItem;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralPart;
 import org.eclipse.ocl.examples.pivot.CollectionRange;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.ConstructorExp;
 import org.eclipse.ocl.examples.pivot.ConstructorPart;
 import org.eclipse.ocl.examples.pivot.EnumLiteralExp;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.IfExp;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.IterateExp;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.IteratorExp;
 import org.eclipse.ocl.examples.pivot.LetExp;
 import org.eclipse.ocl.examples.pivot.NullLiteralExp;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.Property;
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
 import org.eclipse.ocl.examples.pivot.util.AbstractVisitor;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 /**
  * The PrettyPrintExprVisitor supports pretty printing of OCL expressions.
  * PrettyPrintOptions may be used to configure the printing.
  */
 public class EssentialOCLPrettyPrintVisitor extends PivotPrettyPrintVisitor
 {	
 	private static final class Factory implements PrettyPrinter.Factory
 	{
 		private Factory() {
 			PrettyPrinter.addFactory(PivotPackage.eINSTANCE, this);
 		}
 
 		public @NonNull AbstractVisitor<Object, PrettyPrinter> createPrettyPrintVisitor(@NonNull PrettyPrinter printer) {
 			return new EssentialOCLPrettyPrintVisitor(printer);
 		}
 	}
 
 	public static @NonNull PrettyPrinter.Factory FACTORY = new Factory();
 
 	public EssentialOCLPrettyPrintVisitor(@NonNull PrettyPrinter context) {
 		super(context);
 	}
 
 	protected void appendSourceNavigation(@NonNull CallExp object) {
 		OCLExpression source = object.getSource();
 		if (source != null) {
 			if (!(source instanceof VariableExp) || !((VariableExp)source).isImplicit()) {
 				if ((source instanceof OperationCallExp)
 				 && (((OperationCallExp)source).getReferredOperation() != null)
 				 && (((OperationCallExp)source).getReferredOperation().getPrecedence() != null)) {
 					context.append("(");
 					context.precedenceVisit(source, null);
 					context.append(")");
 				}
 				else {
 					safeVisit(source);
 				}
 				if (source.getType() instanceof CollectionType) {
 					context.append(object.isImplicit() ? "." : "->");				// "." for implicit collect
 				}
 				else {
 					if (!object.isImplicit()) {
 						context.append(".");
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public Object visitBooleanLiteralExp(@NonNull BooleanLiteralExp object) {
 		context.append(Boolean.toString(object.isBooleanSymbol()));
 		return null;
 	}
 
 	@Override
 	public Object visitCollectionItem(@NonNull CollectionItem object) {
 		safeVisit(object.getItem());
 		return null;
 	}
 
 	@Override
 	public Object visitCollectionLiteralExp(@NonNull CollectionLiteralExp object) {
 		context.appendName(object.getType(), context.getReservedNames());
 		List<CollectionLiteralPart> parts = object.getPart();
 		if (parts.isEmpty()) {
 			context.append("{}");
 		}
 		else {
 			context.push("{", "");
 			String prefix = ""; //$NON-NLS-1$
 			for (CollectionLiteralPart part : parts) {
 				context.append(prefix);
 				safeVisit(part);
 				prefix = ", ";
 			}
 			context.exdent("", "}", "");
 			context.pop();
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitCollectionRange(@NonNull CollectionRange object) {
 		safeVisit(object.getFirst());
 		context.next("", "..", "");
         safeVisit(object.getLast());
 		return null;
 	}
 
 	@Override
 	public Object visitConstraint(@NonNull Constraint object) {
 		String stereotype = object.getStereotype();
 		if (UMLReflection.BODY.equals(stereotype)) {
 			context.append("body");
 		}
 		else if (UMLReflection.DERIVATION.equals(stereotype)) {
 			context.append("der");
 		}
 		else if (UMLReflection.INITIAL.equals(stereotype)) {
 			context.append("init");
 		}
 		else if (UMLReflection.INVARIANT.equals(stereotype)) {
 			context.append("inv");
 		}
 		else if (UMLReflection.POSTCONDITION.equals(stereotype)) {
 			context.append("post");
 		}
 		else if (UMLReflection.PRECONDITION.equals(stereotype)) {
 			context.append("pre");
 		}
 		else {
 			context.append(stereotype);
 		}
 		if (object.getName() != null) {
 			context.append(" ");
 			context.appendName(object);
 		}
 		context.push(":", " ");
         safeVisit(object.getSpecification());
 		context.pop();
 		return null;
 	}
 
     @Override
 	public Value visitConstructorExp(@NonNull ConstructorExp object) {
 		Type type = object.getType();
 		if (type != null) {
 			context.appendQualifiedType(type);
 		}
 		context.push("{", "");
 		String prefix = ""; //$NON-NLS-1$
 		for (ConstructorPart part : object.getPart()) {
 			context.append(prefix);
 			safeVisit(part);
 			prefix = ", ";
 		}
 		context.exdent("", "}", "");
 		context.pop();
 		return null;
     }
 
 	@Override
 	public String visitConstructorPart(@NonNull ConstructorPart part) {
 		context.appendName(part.getReferredProperty());
 		OCLExpression initExpression = part.getInitExpression();
 		if (initExpression != null) {
 			context.append(" = ");
 			safeVisit(initExpression);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitEnumLiteralExp(@NonNull EnumLiteralExp object) {
 		safeVisit(object.getReferredEnumLiteral());
 		return null;
 	}
 
 	@Override
 	public Object visitExpressionInOCL(@NonNull ExpressionInOCL object) {
 		safeVisit(object.getBodyExpression());
 		return null;
 	}
 
 	@Override
 	public Object visitIfExp(@NonNull IfExp object) {
 		context.push("if", " ");
 		safeVisit(object.getCondition());
 		context.exdent(" ", "then", " ");
 		safeVisit(object.getThenExpression());
 		context.exdent(" ", "else", " ");
         safeVisit(object.getElseExpression());
 		context.exdent(" ", "endif", "");
 		context.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitIntegerLiteralExp(@NonNull IntegerLiteralExp object) {
 		context.append(object.getIntegerSymbol());
 		return null;
 	}
 
 	@Override
 	public Object visitInvalidLiteralExp(@NonNull InvalidLiteralExp object) {
 		context.append("invalid");
 		return null;
 	}
 
 	@Override
 	public Object visitIterateExp(@NonNull IterateExp object) {
 		Iteration referredIteration = object.getReferredIteration();
 		OCLExpression body = object.getBody();
 		Variable result = object.getResult();
 		if (context.showNames()) {
 			List<Variable> iterators = object.getIterator();
 			appendSourceNavigation(object);
 			context.appendName(referredIteration);
 			context.push("(", "");
 			String prefix = null;
 			if (iterators.size() > 0) {
 				boolean hasExplicitIterator = false;
 				for (Variable iterator : iterators) {
 					if (!iterator.isImplicit()) {
 						if (prefix != null) {
 							context.next(null, prefix, " ");
 						}
 //						safeVisit(iterator);
 						context.appendName(iterator);
 						prefix = ",";
 						hasExplicitIterator = true;
 					}
 				}
 				if (hasExplicitIterator) {
 					prefix = ";";
 				}
 				if (prefix != null) {
 					context.next(null, prefix, " ");
 				}
 				safeVisit(result);
 				context.next(null, " |", " ");
 			}
 			safeVisit(body);
 			context.next("", ")", "");
 			context.pop();
 		}
 		else {
 			OCLExpression source = object.getSource();
 			if (source != null) {
 				safeVisit(source.getType());
 				context.append("::");
 			}
 			context.appendName(referredIteration);
 			context.push("(", "");
 			String prefix = null;
 			for (Variable iterator : object.getIterator()) {
 				if (prefix != null) {
 					context.next(null, prefix, " ");
 				}
 				context.appendName(iterator);
 				context.append(" : ");
 				safeVisit(iterator.getType());
 				prefix = ",";
 			}
 			context.next(null, ";", " ");
 			context.appendName(result);
 			context.append(" : ");
 			safeVisit(result.getType());
 			context.next(null, " |", " ");
 			safeVisit(body != null ? body.getType() : null);
 			context.next("", ")", "");
 			context.pop();
 			context.append(" : ");
 			safeVisit(object.getType());
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitIteratorExp(@NonNull IteratorExp object) {
 		Iteration referredIteration = object.getReferredIteration();
 		OCLExpression body = object.getBody();
 		if (context.showNames()) {
 			List<Variable> iterators = object.getIterator();
 			appendSourceNavigation(object);
 			if (object.isImplicit()) {
 				assert referredIteration.getName().equals("collect");
 				assert iterators.size() == 1;
 				safeVisit(body);
 			}
 			else {
 				context.appendName(referredIteration);
 				context.push("(", "");
 				if (iterators.size() > 0) {
 					String prefix = null;
 					boolean hasExplicitIterator = false;
 					for (Variable iterator : iterators) {
 						if (!iterator.isImplicit()) {
 							if (prefix != null) {
 								context.next(null, prefix, " ");
 							}
 //							safeVisit(iterator);
 							context.appendName(iterator);
 							prefix = ",";
 							hasExplicitIterator = true;
 						}
 					}
 					if (hasExplicitIterator) {
 						context.next(null, " |", " ");
 					}
 					else if (prefix != null) {
 						context.next(null, prefix, " ");
 					}
 				}
 				safeVisit(body);
 				context.next("", ")", "");
 				context.pop();
 			}
 		}
 		else {
 			OCLExpression source = object.getSource();
 			if (source != null) {
 				safeVisit(source.getType());
 				context.append("::");
 			}
 			context.appendName(referredIteration);
 			context.push("(", "");
 			String prefix = null;
 			for (Variable iterator : object.getIterator()) {
 				if (prefix != null) {
 					context.next(null, prefix, " ");
 				}
 				context.appendName(iterator);
 				context.append(" : ");
 				safeVisit(iterator.getType());
 				prefix = ",";
 			}
 			context.next(null, " |", " ");
 			safeVisit(body != null ? body.getType() : null);
 			context.next("", ")", "");
 			context.pop();
 			context.append(" : ");
 			safeVisit(object.getType());
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitLetExp(@NonNull LetExp object) {
 		context.push("let", " ");
 		safeVisit(object.getVariable());
 		context.exdent(" ", "in", " ");
         safeVisit(object.getIn());
 		context.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitNullLiteralExp(@NonNull NullLiteralExp object) {
 		context.append("null");
 		return null;
 	}
 
 	@Override
 	public Object visitOCLExpression(@NonNull OCLExpression object) {
 		context.append("<");
 		context.append(object.eClass().getName());
 		context.append(">");
 		return null;
 	}
 
 	@Override
 	public Object visitOpaqueExpression(@NonNull OpaqueExpression object) {
 		String body = PivotUtil.getBody(object);
 		if (body != null) {
 			context.append(body);
 		}
 		else {
 			context.append("null -- not specified");
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitOperationCallExp(@NonNull OperationCallExp object) {
 		OCLExpression source = object.getSource();
 		List<OCLExpression> arguments = object.getArgument();
 		Operation referredOperation = object.getReferredOperation();
 		if (context.showNames()) {
 			Precedence precedence = referredOperation != null ? referredOperation.getPrecedence() : null;
 			if (precedence == null) {
 				appendSourceNavigation(object);
 				if (!object.isImplicit()) {
 					context.appendName(referredOperation);
 					context.push("(", "");
 					String prefix = null; //$NON-NLS-1$
 					for (OCLExpression argument : arguments) {
 						if (prefix != null) {
 							context.next(null, prefix, " ");
 						}
 						context.precedenceVisit(argument, null);
 						prefix = ",";
 					}
 					context.next("", ")", "");
 					context.pop();
 				}
 			}
 			else {
 				Precedence currentPrecedence = context.getCurrentPrecedence();
 				boolean lowerPrecedence = (currentPrecedence  != null) && (precedence.getOrder() > currentPrecedence.getOrder());
 				if (lowerPrecedence) {
 					context.push("(", null);
 				}
 				if (arguments.size() == 0) {			// Prefix
 					context.appendName(referredOperation, null);
 					if ((referredOperation != null) && PivotUtil.isValidIdentifier(referredOperation.getName())) {
 						context.append(" ");			// No space for unary minus
 					}
 					context.precedenceVisit(source, precedence);
 				}
 				else {			// Infix
 					context.precedenceVisit(source, precedence);
 					String name = context.getName(referredOperation, null);
 					assert name != null;
 					context.next(" ", name, " ");
 					context.precedenceVisit(arguments.get(0), precedence);
 				}
 				if (lowerPrecedence) {
 					context.exdent("", ")", "");
 					context.pop();
 				}
 			}
 		}
 		else {
 			if (source != null) {
 				safeVisit(source.getType());
 				context.append("::");
 			}
 			context.appendName(referredOperation);
 			context.push("(", "");
 			String prefix = null;
 			for (OCLExpression argument : arguments) {
 				if (prefix != null) {
 					context.next(null, prefix, " ");
 				}
 				safeVisit(argument.getType());
 				prefix = ",";
 			}
 			context.next("", ")", "");
 			context.pop();
 			context.append(" : ");
 			safeVisit(object.getType());
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitParameter(@NonNull Parameter object) {
 		context.appendName(object);
 		Type type = object.getType();
 		if (type != null) {
 			context.append(" : ");
 			context.appendQualifiedType(type);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitPropertyCallExp(@NonNull PropertyCallExp object) {
 		Property referredProperty = object.getReferredProperty();
 		if (context.showNames()) {
 			appendSourceNavigation(object);
 			context.appendName(referredProperty);
 		}
 		else {
 			safeVisit(referredProperty);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitRealLiteralExp(@NonNull RealLiteralExp object) {
 		context.append(object.getRealSymbol());
 		return null;
 	}
 
 	@Override
 	public Object visitStringLiteralExp(@NonNull StringLiteralExp object) {
 		context.append("'");
 		context.append(PivotUtil.convertToOCLString(object.getStringSymbol()));
 		context.append("'");
 		return null;
 	}
 
 	@Override
 	public Object visitTupleLiteralExp(@NonNull TupleLiteralExp object) {
 		context.append("Tuple");
 		context.push("{", "");
 		String prefix = ""; //$NON-NLS-1$
 		for (TupleLiteralPart part : object.getPart()) {
 			context.append(prefix);
 			safeVisit(part);
 			prefix = ", ";
 		}
 		context.exdent("", "}", "");
 		context.pop();
 		return null;
 	}
 
 	@Override
 	public Object visitTupleLiteralPart(@NonNull TupleLiteralPart object) {
 		context.appendName(object);
 		context.append(" = ");
 		safeVisit(object.getInitExpression());
 		return null;
 	}
 
 	@Override
 	public Object visitTypeExp(@NonNull TypeExp object) {
 		Type type = object.getReferredType();
 		if (type != null) {
 			context.appendQualifiedType(type);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitUnlimitedNaturalLiteralExp(@NonNull UnlimitedNaturalLiteralExp object) {
 		Number symbol = object.getUnlimitedNaturalSymbol();
		if (((symbol instanceof BigInteger) && symbol.equals(BigInteger.valueOf(-1))) || (symbol.longValue() == -1)){
 			context.append("*");
 		}
 		else {
 			context.append(symbol.toString());
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitVariable(@NonNull Variable object) {
 		context.appendName(object);
 		Type type = object.getType();
 		if (type != null) {
 			context.append(" : ");
 			context.appendQualifiedType(type);
 		}
 		OCLExpression initExpression = object.getInitExpression();
 		if (initExpression != null) {
 			context.append(" = ");
 			safeVisit(initExpression);
 		}
 		return null;
 	}
 
 	@Override
 	public Object visitVariableExp(@NonNull VariableExp object) {
 		VariableDeclaration referredVariable = object.getReferredVariable();
 		if ((referredVariable != null) && "self".equals(referredVariable.getName())) {
 			context.appendName(referredVariable, null);
 		}
 		else {
 			context.appendName(referredVariable);
 		}
 		return null;
 	}
 }
