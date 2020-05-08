 /**
  * <copyright>
  *
  * Copyright (c) 2010,2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: EssentialOCLLeft2RightVisitor.java,v 1.23 2011/05/23 05:51:23 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.essentialocl.cs2pivot;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.ocl.examples.domain.library.LibraryFeature;
 import org.eclipse.ocl.examples.domain.library.LibraryValidator;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.pivot.BooleanLiteralExp;
 import org.eclipse.ocl.examples.pivot.CallExp;
 import org.eclipse.ocl.examples.pivot.ClassifierType;
 import org.eclipse.ocl.examples.pivot.CollectionItem;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralPart;
 import org.eclipse.ocl.examples.pivot.CollectionRange;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.ConstructorExp;
 import org.eclipse.ocl.examples.pivot.ConstructorPart;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.EnumLiteralExp;
 import org.eclipse.ocl.examples.pivot.EnumerationLiteral;
 import org.eclipse.ocl.examples.pivot.Environment;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.Feature;
 import org.eclipse.ocl.examples.pivot.IfExp;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidType;
 import org.eclipse.ocl.examples.pivot.IterateExp;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.IteratorExp;
 import org.eclipse.ocl.examples.pivot.LambdaType;
 import org.eclipse.ocl.examples.pivot.LetExp;
 import org.eclipse.ocl.examples.pivot.LoopExp;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.NullLiteralExp;
 import org.eclipse.ocl.examples.pivot.NumericLiteralExp;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotFactory;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.PropertyCallExp;
 import org.eclipse.ocl.examples.pivot.StringLiteralExp;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.TupleLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralPart;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypeExp;
 import org.eclipse.ocl.examples.pivot.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.VariableDeclaration;
 import org.eclipse.ocl.examples.pivot.VariableExp;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.messages.OCLMessages;
 import org.eclipse.ocl.examples.pivot.scoping.EnvironmentView;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeFilter;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ModelElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.TypedRefCS;
 import org.eclipse.ocl.examples.xtext.base.cs2pivot.CS2PivotConversion;
 import org.eclipse.ocl.examples.xtext.base.scoping.BaseScopeView;
 import org.eclipse.ocl.examples.xtext.essentialocl.attributes.BinaryOperationFilter;
 import org.eclipse.ocl.examples.xtext.essentialocl.attributes.ImplicitCollectFilter;
 import org.eclipse.ocl.examples.xtext.essentialocl.attributes.ImplicitCollectionFilter;
 import org.eclipse.ocl.examples.xtext.essentialocl.attributes.UnaryOperationFilter;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.BinaryOperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.BooleanLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.CollectionLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.CollectionLiteralPartCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.CollectionTypeCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ConstructorExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ConstructorPartCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ContextCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.EssentialOCLCSTPackage;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.ExpSpecificationCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.IfExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.IndexExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InfixExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InvalidLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.InvocationExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.LetExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.LetVariableCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NameExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NamedExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigatingArgCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigationOperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NavigationRole;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NestedExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NullLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.NumberLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.OperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.PrefixExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.SelfExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.StringLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TupleLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TupleLiteralPartCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.TypeLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.UnaryOperatorCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.UnlimitedNaturalLiteralExpCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.essentialOCLCST.VariableCS;
 import org.eclipse.ocl.examples.xtext.essentialocl.utilities.EssentialOCLUtils;
 
 public class EssentialOCLLeft2RightVisitor extends AbstractEssentialOCLLeft2RightVisitor
 {
 	private static final Logger logger = Logger.getLogger(EssentialOCLLeft2RightVisitor.class);
 
 	protected final MetaModelManager metaModelManager;
 	
 	public EssentialOCLLeft2RightVisitor(CS2PivotConversion context) {
 		super(context);
 		this.metaModelManager = context.getMetaModelManager();
 	}
 
 	protected OCLExpression checkImplementation(NamedExpCS csNavigatingExp,
 			Feature feature, CallExp callExp, OCLExpression expression) {
 		LibraryFeature implementation;
 		try {
 			implementation = metaModelManager.getImplementation(feature);
 		} catch (Exception e) {
 			return context.addBadExpressionError(csNavigatingExp, "Failed to load '" + feature.getImplementationClass() + "': " + e);
 		}
 		if (implementation != null) {
 			LibraryValidator validator = implementation.getValidator(metaModelManager);
 			if (validator != null) {
 				Diagnostic diagnostic = validator.validate(metaModelManager, callExp);
 				if (diagnostic != null) {
 					context.addDiagnostic(csNavigatingExp, diagnostic);
 				}
 			}
 		}
 		return expression;
 	}
 
 /*	private TemplateParameterSubstitution findFormalParameter(TemplateParameter formalTemplateParameter, Type actualType) {
 		for (TemplateBinding actualTemplateBinding : actualType.getTemplateBinding()) {
 			for (TemplateParameterSubstitution actualTemplateParameterSubstitution : actualTemplateBinding.getParameterSubstitution()) {
 				TemplateParameter actualFormal = actualTemplateParameterSubstitution.getFormal();
 				if (actualFormal == formalTemplateParameter) {
 					return actualTemplateParameterSubstitution;
 				}
 			}
 		}
 		if (actualType instanceof org.eclipse.ocl.examples.pivot.Class) {
 			for (org.eclipse.ocl.examples.pivot.Class superClass : ((org.eclipse.ocl.examples.pivot.Class)actualType).getSuperClass()) {
 				TemplateParameterSubstitution actualTemplateParameterSubstitution = findFormalParameter(formalTemplateParameter, superClass);
 				if (actualTemplateParameterSubstitution != null) {
 					return actualTemplateParameterSubstitution;
 				}
 			}
 		}
 		return null;
 	} */
 
 	protected Operation getBadOperation() {
 		InvalidType invalidType = metaModelManager.getOclInvalidType();
 		Operation badOperation = PivotUtil.getNamedElement(invalidType.getOwnedOperation(), "oclBadOperation");
 		return badOperation;
 	}
 
 	protected Property getBadProperty() {
 		InvalidType invalidType = metaModelManager.getOclInvalidType();
 		Property badProperty = PivotUtil.getNamedElement(invalidType.getOwnedAttribute(), "oclBadProperty");
 		return badProperty;
 	}
 
 /*	private TemplateParameter getFormal(List<TemplateBinding> templateBindings, TemplateParameter templateParameter) {
 		for (TemplateBinding templateBinding : templateBindings) {
 			for (TemplateParameterSubstitution templateParameterSubstitution : templateBinding.getParameterSubstitution()) {
 				if (templateParameter == templateParameterSubstitution.getFormal()) {
 					return templateParameterSubstitution.getActual().getOwningTemplateParameter();
 				}
 			}
 		}
 		return null;
 	} */
 
 	protected VariableDeclaration getImplicitSource(ModelElementCS csExp, Feature feature) {
 		EObject eContainer = csExp.eContainer();
 		if (csExp instanceof InvocationExpCS) {
 			Type namedElementType = PivotUtil.getOwningType(feature);
 			InvocationExpCS csNavigatingExp = (InvocationExpCS) csExp;
 			CallExp iteratorExp = PivotUtil.getPivot(CallExp.class, csNavigatingExp);
 			if (iteratorExp instanceof LoopExp) {
 				for (Variable iterator : ((LoopExp)iteratorExp).getIterator()) {
 					Type type = iterator.getType();
 					if (metaModelManager.conformsTo(type, namedElementType)) {
 						return iterator;
 					}
 				}
 				if (iteratorExp instanceof IterateExp) {
 					Variable iterator = ((IterateExp)iteratorExp).getResult();
 					Type type = iterator.getType();
 					if (metaModelManager.conformsTo(type, namedElementType)) {
 						return iterator;
 					}
 				}
 			}
 		}
 		else if (eContainer instanceof InvocationExpCS) {
 			EReference eContainmentFeature = csExp.eContainmentFeature();
 			if (eContainmentFeature == EssentialOCLCSTPackage.Literals.INVOCATION_EXP_CS__ARGUMENT) {
 				Type namedElementType = PivotUtil.getOwningType(feature);
 				InvocationExpCS csNavigatingExp = (InvocationExpCS) eContainer;
 				CallExp iteratorExp = PivotUtil.getPivot(CallExp.class, csNavigatingExp);
 				if (iteratorExp instanceof LoopExp) {
 					for (Variable iterator : ((LoopExp)iteratorExp).getIterator()) {
 						Type type = iterator.getType();
 						if (metaModelManager.conformsTo(type, namedElementType)) {
 							return iterator;
 						}
 					}
 					if (iteratorExp instanceof IterateExp) {
 						Variable iterator = ((IterateExp)iteratorExp).getResult();
 						Type type = iterator.getType();
 						if (metaModelManager.conformsTo(type, namedElementType)) {
 							return iterator;
 						}
 					}
 				}
 			}
 		}
 		else if (csExp instanceof ContextCS) {
 			ContextCS csContext = (ContextCS) csExp;
 			ExpressionInOCL pivotElement = PivotUtil.getPivot(ExpressionInOCL.class, csContext);
 			return pivotElement.getContextVariable();
 		}
 		else if (csExp instanceof ExpSpecificationCS) {
 			ExpressionInOCL pivotElement = PivotUtil.getPivot(ExpressionInOCL.class, csExp);
 			return pivotElement.getContextVariable();
 		}
 		if (eContainer instanceof ContextCS) {
 			return getImplicitSource((ModelElementCS) eContainer, feature);
 		}
 		else if (eContainer instanceof ExpSpecificationCS) {
 			return getImplicitSource((ModelElementCS) eContainer, feature);
 		}
 		else if (eContainer instanceof ExpCS) {
 			return getImplicitSource((ModelElementCS) eContainer, feature);
 		}
 		else if (eContainer instanceof NavigatingArgCS) {
 			return getImplicitSource((ModelElementCS) eContainer, feature);
 		}
 		return null;
 	}
 
 	protected Type getSourceElementType(InvocationExpCS csNavigatingExp, OCLExpression source) {
 		Type sourceType = source.getType();
 		boolean isCollectionNavigation = PivotConstants.COLLECTION_NAVIGATION_OPERATOR.equals(csNavigatingExp.getParent().getName());
 		if (!isCollectionNavigation) {
 			return sourceType;
 		}
 		if (sourceType instanceof CollectionType) {
 			return ((CollectionType)sourceType).getElementType();
 		}
 		else {
 			return sourceType;
 		}
 	}
 
 	protected EnumLiteralExp resolveEnumLiteral(ExpCS csExp, EnumerationLiteral enumerationLiteral) {
 		EnumLiteralExp expression = context.refreshModelElement(EnumLiteralExp.class, PivotPackage.Literals.ENUM_LITERAL_EXP, csExp);
 		context.setType(expression, enumerationLiteral.getEnumeration());
 		expression.setReferredEnumLiteral(enumerationLiteral);
 		return expression;
 	}
 
 	protected void resolveIterationAccumulators(InvocationExpCS csNavigatingExp, LoopExp expression) {
 		Iteration iteration = expression.getReferredIteration();
 		List<Variable> pivotAccumulators = new ArrayList<Variable>();
 		//
 		//	Explicit accumulator
 		//
 		for (int argIndex = 0; argIndex < csNavigatingExp.getArgument().size(); argIndex++) {
 			NavigatingArgCS csArgument = csNavigatingExp.getArgument().get(argIndex);
 			if (csArgument.getRole() != NavigationRole.ACCUMULATOR) {
 				continue;
 			}
 			if (csArgument.getInit() == null) {
 				context.addDiagnostic(csArgument, "Missing initializer for accumulator");
 			}
 //			if (csArgument.getOwnedType() != null) {
 //				context.addError(csArgument, "Unexpected type for parameter");
 //			}
 			ExpCS csName = csArgument.getName();
 			Variable acc = PivotUtil.getPivot(Variable.class, csName);
 			acc.setRepresentedParameter(iteration.getOwnedAccumulator().get(pivotAccumulators.size()));
 			pivotAccumulators.add(acc);
 		}
 		//
 		//	Implicit Accumulator
 		//
 		if (expression instanceof IterateExp) {
 			IterateExp iterateExp = (IterateExp)expression;
 			if (pivotAccumulators.size() > 1) {
 				context.addDiagnostic(csNavigatingExp, "Iterate calls cannot have more than one accumulator");			
 			}
 			else {
 				iterateExp.setResult(pivotAccumulators.get(0));
 			}
 		}
 		else if (pivotAccumulators.size() > 0) {
 			context.addDiagnostic(csNavigatingExp, "Iteration calls cannot have an accumulator");			
 		}
 	}
 
 	protected void resolveIterationBody(InvocationExpCS csNavigatingExp, LoopExp expression) {
 		List<OCLExpression> pivotBodies = new ArrayList<OCLExpression>();
 		for (NavigatingArgCS csArgument : csNavigatingExp.getArgument()) {
 			if (csArgument.getRole() == NavigationRole.EXPRESSION) {
 				if (csArgument.getInit() != null) {
 					context.addDiagnostic(csArgument, "Unexpected initializer for expression");
 				}
 				if (csArgument.getOwnedType() != null) {
 					context.addDiagnostic(csArgument, "Unexpected type for expression");
 				}
 				OCLExpression exp = context.visitLeft2Right(OCLExpression.class, csArgument.getName());
 //				context.installPivotElement(csArgument, exp);
 				context.installPivotUsage(csArgument, exp);
 				pivotBodies.add(exp);
 			}
 		}
 		if (pivotBodies.size() != 1) {
 			expression.setBody(context.addBadExpressionError(csNavigatingExp, "Iteration calls must have exactly one body"));
 		}
 		else {
 			expression.setBody(pivotBodies.get(0));
 		}
 	}
 
 	protected LoopExp resolveIterationCall(InvocationExpCS csNavigatingExp, OCLExpression source, Iteration iteration) {
 		NamedExpCS csNamedExp = csNavigatingExp; //.getNamedExp();
 		LoopExp expression;
 		if (iteration.getOwnedAccumulator().size() > 0) {
 			expression = context.refreshModelElement(IterateExp.class, PivotPackage.Literals.ITERATE_EXP, csNamedExp);
 		}
 		else {
 			expression = context.refreshModelElement(IteratorExp.class, PivotPackage.Literals.ITERATOR_EXP, csNamedExp);
 		}
 		context.setReferredIteration(expression, iteration);
 		context.installPivotUsage(csNavigatingExp, expression);		
 		//
 		resolveIterationAccumulators(csNavigatingExp, expression);
 		resolveIterationIterators(csNavigatingExp, source, expression);
 //		resolveLoopBody(csNavigatingExp, expression);
 		resolveOperationReturnType(expression);
 		return expression;
 	}
 
 	protected void resolveIterationExplicitAccumulators(InvocationExpCS csNavigatingExp) {
 		//
 		//	Explicit accumulator
 		//
 		for (int argIndex = 0; argIndex < csNavigatingExp.getArgument().size(); argIndex++) {
 			NavigatingArgCS csArgument = csNavigatingExp.getArgument().get(argIndex);
 			if (csArgument.getRole() != NavigationRole.ACCUMULATOR) {
 				continue;
 			}
 			ExpCS csName = csArgument.getName();
 			Variable acc = PivotUtil.getPivot(Variable.class, csName);
 			context.installPivotUsage(csArgument, acc);
 			OCLExpression initExpression = context.visitLeft2Right(OCLExpression.class, csArgument.getInit());
 			acc.setInitExpression(initExpression);
 			TypedRefCS csAccType = csArgument.getOwnedType();
 			Type accType;
 			if (csAccType != null) {
 				accType = PivotUtil.getPivot(Type.class, csAccType);
 			}
 			else {
 				accType = initExpression.getType();
 			}
 			context.setType(acc, accType);
 		}
 	}
 
 	protected void resolveIterationIterators(InvocationExpCS csNavigatingExp,
 			OCLExpression source, LoopExp expression) {
 		Iteration iteration = expression.getReferredIteration();
 		List<Variable> pivotIterators = new ArrayList<Variable>();
 		//
 		//	Explicit iterators
 		//
 		int iterationIteratorsSize = iteration.getOwnedIterator().size();
 		for (int argIndex = 0; argIndex < csNavigatingExp.getArgument().size(); argIndex++) {
 			NavigatingArgCS csArgument = csNavigatingExp.getArgument().get(argIndex);
 			if (csArgument.getRole() != NavigationRole.ITERATOR) {
 				continue;
 			}
 			if (iterationIteratorsSize <= argIndex) {
 				context.addWarning(csArgument, OCLMessages.RedundantIterator_WARNING_, iteration.getName());
 				continue;
 			}
 			if (csArgument.getInit() != null) {
 				context.addDiagnostic(csArgument, "Unexpected initializer for iterator");
 			}
 //			if (csArgument.getOwnedType() == null) {
 //				context.addError(csArgument, "Missing type for iterator");
 //			}
 			ExpCS csName = csArgument.getName();
 			Variable iterator = PivotUtil.getPivot(Variable.class, csName);
 			context.installPivotUsage(csArgument, iterator);
 			iterator.setRepresentedParameter(iteration.getOwnedIterator().get(pivotIterators.size()));
 			TypedRefCS csType = csArgument.getOwnedType();
 			Type varType = csType != null ? PivotUtil.getPivot(Type.class, csType) : null;
 			if (varType == null) {
 				varType = getSourceElementType(csNavigatingExp, source);
 			}
 			context.setType(iterator, varType);
 			pivotIterators.add(iterator);
 		}
 		//
 		//	Implicit Iterators
 		//
 		while (pivotIterators.size() < iterationIteratorsSize) {
 			String varName = Integer.toString(pivotIterators.size()+1) + "_";
 			Variable iterator = context.refreshModelElement(Variable.class, PivotPackage.Literals.VARIABLE, null);
 			context.refreshName(iterator, varName);
 			Type varType = getSourceElementType(csNavigatingExp, source);
 			context.setType(iterator, varType);
 			iterator.setImplicit(true);
 			iterator.setRepresentedParameter(iteration.getOwnedIterator().get(pivotIterators.size()));
 			pivotIterators.add(iterator);
 		}
 		context.refreshList(expression.getIterator(), pivotIterators);
 	}
 
 	/**
 	 * Synthesize any any implicit collect() call. The return type is left unresolved since operation parameters or loop body must be resolved first.
 	 */
 	protected CallExp resolveNavigationFeature(NamedExpCS csElement, OCLExpression source, Feature feature, CallExp callExp) {
 		CallExp navigationExp = callExp;
 		Type requiredSourceType = PivotUtil.getOwningType(feature);
 		boolean isDotNavigation = false;
		OperatorCS parent = csElement.getParent();
		if ((parent instanceof NavigationOperatorCS) && !(parent.getSource() == csElement)) {
			NavigationOperatorCS navigationOperatorCS = (NavigationOperatorCS)parent;
			isDotNavigation = PivotConstants.OBJECT_NAVIGATION_OPERATOR.equals(navigationOperatorCS.getName());
 			if (isDotNavigation) {
 				Type actualSourceType = source.getType();
 				if ((actualSourceType instanceof CollectionType) && !(requiredSourceType instanceof CollectionType)) {
 					Type elementType = ((CollectionType)actualSourceType).getElementType();
 					IteratorExp iteratorExp = context.refreshModelElement(IteratorExp.class, PivotPackage.Literals.ITERATOR_EXP, null);
 					iteratorExp.setImplicit(true);
 					EnvironmentView environmentView = new EnvironmentView(metaModelManager, PivotPackage.Literals.LOOP_EXP__REFERRED_ITERATION, "collect");
 					environmentView.addFilter(new ImplicitCollectFilter(metaModelManager, (CollectionType) actualSourceType, elementType));
 					Type lowerBoundType = (Type) PivotUtil.getLowerBound(actualSourceType);
 					environmentView.computeLookups(lowerBoundType, null, null, null);
 					Iteration resolvedIteration = (Iteration)environmentView.getContent();
 					context.setReferredIteration(iteratorExp, resolvedIteration);
 					Variable iterator = context.refreshModelElement(Variable.class, PivotPackage.Literals.VARIABLE, null); // FIXME reuse
 					Parameter resolvedIterator = resolvedIteration.getOwnedIterator().get(0);
 					iterator.setRepresentedParameter(resolvedIterator);
 					context.refreshName(iterator, "1_");
 					context.setType(iterator, elementType);
 					iterator.setImplicit(true);
 					iteratorExp.getIterator().add(iterator);
 					VariableExp variableExp = context.refreshModelElement(VariableExp.class, PivotPackage.Literals.VARIABLE_EXP, null); // FIXME reuse
 					variableExp.setReferredVariable(iterator);
 					variableExp.setImplicit(true);
 					context.setType(variableExp, elementType);
 					callExp.setSource(variableExp);			
 					iteratorExp.setBody(callExp);
 					navigationExp = iteratorExp;
 				}
 			}
 		}
 		navigationExp.setSource(source);
 		return navigationExp;
 	}
 
 	/**
 	 * Resolve any implicit source and any associated implicit oclAsSet().
 	 */
 	protected OCLExpression resolveNavigationSource(NamedExpCS csNameExp, Feature feature) {
 		boolean isCollectionNavigation = false;
 		OperatorCS csOperator = csNameExp.getParent();
 		OCLExpression source = null;
 		if (csOperator instanceof NavigationOperatorCS) {
 			ExpCS csSource = csOperator.getSource();
 			if (csSource != csNameExp) {
 				source = PivotUtil.getPivot(OCLExpression.class, csSource);
 				isCollectionNavigation = csOperator.getName().equals(PivotConstants.COLLECTION_NAVIGATION_OPERATOR);
 			}
 		}
 		if (source == null) {
 			ModelElementCS csPivoted = EssentialOCLUtils.getPivotedCS(csOperator != null ? csOperator : csNameExp);
 			ElementCS csChild = EssentialOCLUtils.getPivotingChildCS(csPivoted);
 			ModelElementCS csParent = EssentialOCLUtils.getPivotingParentCS(csChild);
 			ModelElementCS csPivotedParent = EssentialOCLUtils.getPivotedCS(csParent);
 			VariableDeclaration implicitSource = getImplicitSource(csPivotedParent, feature);
 			if (implicitSource != null) {
 				VariableExp sourceAccess = PivotFactory.eINSTANCE.createVariableExp();
 				sourceAccess.setReferredVariable(implicitSource);
 				context.setType(sourceAccess, implicitSource.getType());
 				sourceAccess.setImplicit(true);
 				source = sourceAccess;
 			}
 		}
 		if (source != null) {
 			Type actualSourceType = source.getType();
 			if (isCollectionNavigation && !(actualSourceType instanceof CollectionType)) {
 				OperationCallExp expression = context.refreshModelElement(OperationCallExp.class, PivotPackage.Literals.OPERATION_CALL_EXP, csOperator);
 				expression.setImplicit(true);
 				expression.setSource(source);
 				expression.setName("oclAsSet");
 				resolveOperationCall(expression, csOperator, new ImplicitCollectionFilter(metaModelManager, actualSourceType));
 				source = expression;
 			}
 		}
 		return source;
 	}
 
 	protected OCLExpression resolveOperation(InvocationExpCS csNavigatingExp) {
 		NamedExpCS csNamedExp = csNavigatingExp; //.getNamedExp();
 		//
 		//	Need to resolve types for operation arguments in order to disambiguate
 		//	operation names. No need to resolve iteration arguments since for those
 		//	we only need to count iterators.
 		//
 		resolveOperationArgumentTypes(csNavigatingExp);
 		resolveIterationExplicitAccumulators(csNavigatingExp);
 		//
 		//	Resolve the static operation/iteration by name and known operation argument types.
 		//
 		NamedElement namedElement = csNamedExp.getNamedElement();
 		if ((namedElement == null) || namedElement.eIsProxy()) {
 			namedElement = getBadOperation();
 			OperationCallExp operationCallExp = context.refreshModelElement(OperationCallExp.class, PivotPackage.Literals.OPERATION_CALL_EXP, csNamedExp);
 			context.setReferredOperation(operationCallExp, null);
 			context.installPivotUsage(csNavigatingExp, operationCallExp);		
 			context.setType(operationCallExp, metaModelManager.getOclInvalidType());
 			return operationCallExp;
 		}
 		else if (namedElement instanceof Operation) {
 			Operation operation = (Operation)namedElement;
 			Operation baseOperation = metaModelManager.resolveBaseOperation(operation);
 			OCLExpression source = resolveNavigationSource(csNavigatingExp, operation);
 			CallExp outerExpression;
 			CallExp innerExpression;
 			if (operation instanceof Iteration) {
 				Iteration iteration = (Iteration)operation;
 				innerExpression = resolveIterationCall(csNavigatingExp, source, iteration);
 				outerExpression = resolveNavigationFeature(csNavigatingExp, source, baseOperation, innerExpression);
 				resolveIterationBody(csNavigatingExp, (LoopExp)innerExpression);
 			}
 			else {
 				OperationCallExp operationCallExp = context.refreshModelElement(OperationCallExp.class, PivotPackage.Literals.OPERATION_CALL_EXP, csNamedExp);
 				context.setReferredOperation(operationCallExp, operation);
 				context.installPivotUsage(csNavigatingExp, operationCallExp);		
 				innerExpression = operationCallExp;
 				outerExpression = resolveNavigationFeature(csNavigatingExp, source, baseOperation, innerExpression);
 				resolveOperationArguments(csNavigatingExp, source, operation, operationCallExp);
 			}
 			resolveOperationReturnType(innerExpression);
 			if (outerExpression != innerExpression) {
 				resolveOperationReturnType(outerExpression);
 			}
 			return checkImplementation(csNavigatingExp, operation, innerExpression, outerExpression);
 		}
 		else {
 			return resolveUnknownOperation(csNamedExp);
 		}
 	}
 
 	protected OCLExpression resolveUnknownOperation(NamedExpCS csNamedExp) {
 		return context.addBadExpressionError(csNamedExp, "Operation name expected");
 	}
 
 	/**
 	 * Determine the type of each operation argument so that the appropriate operation overload can be selected.
 	 * Iterator bodies are left unresolved.
 	 */
 	protected void resolveOperationArgumentTypes(InvocationExpCS csNavigatingExp) {
 		for (NavigatingArgCS csArgument : csNavigatingExp.getArgument()) {
 			if (csArgument.getRole() == NavigationRole.ITERATOR) {
 				break;
 			}
 			else if (csArgument.getRole() == NavigationRole.ACCUMULATOR) {
 				break;
 			}
 			else if (csArgument.getRole() == NavigationRole.EXPRESSION) {
 				OCLExpression arg = context.visitLeft2Right(OCLExpression.class, csArgument.getName());
 				if (arg != null) {
 					context.installPivotUsage(csArgument, arg);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Complete the installation of each operation argument in its operation call.
 	 */
 	protected void resolveOperationArguments(InvocationExpCS csNavigatingExp,
 			OCLExpression source, Operation operation, OperationCallExp expression) {
 		List<OCLExpression> pivotArguments = new ArrayList<OCLExpression>();
 		List<NavigatingArgCS> csArguments = csNavigatingExp.getArgument();
 		List<Parameter> ownedParameters = operation.getOwnedParameter();
 		int parametersCount = ownedParameters.size();
 		int csArgumentCount = csArguments.size();
 		if (csArgumentCount > 0) {
 			if (csArguments.get(0).getRole() != NavigationRole.EXPRESSION) {
 				context.addDiagnostic(csNavigatingExp, "Operation calls can only specify expressions");			
 			}
 			for (int argIndex = 0; argIndex < csArgumentCount; argIndex++) {
 				NavigatingArgCS csArgument = csArguments.get(argIndex);
 				if (csArgument.getInit() != null) {
 					context.addDiagnostic(csArgument, "Unexpected initializer for expression");
 				}
 				if (csArgument.getOwnedType() != null) {
 					context.addDiagnostic(csArgument, "Unexpected type for expression");
 				}
 				OCLExpression arg = PivotUtil.getPivot(OCLExpression.class, csArgument);
 				if (arg != null) {
 					pivotArguments.add(arg);
 				}
 			}
 		}
 		if ((csArgumentCount != parametersCount) && (operation != getBadOperation())) {
 			String boundMessage = DomainUtil.bind(OCLMessages.MismatchedArgumentCount_ERROR_, csArgumentCount, parametersCount);
 			context.addDiagnostic(csNavigatingExp, boundMessage);			
 		}
 		context.refreshList(expression.getArgument(), pivotArguments);
 	}
 
 	protected void resolveOperationCall(OperationCallExp expression, OperatorCS csOperator, ScopeFilter filter) {
 		EnvironmentView environmentView = new EnvironmentView(metaModelManager, PivotPackage.Literals.OPERATION_CALL_EXP__REFERRED_OPERATION, expression.getName());
 		environmentView.addFilter(filter);
 		Type sourceType = expression.getSource().getType();
 		if (sourceType instanceof LambdaType) {								// FIXME Modularize this
 			sourceType = ((LambdaType)sourceType).getResultType();
 		}
 		int size = 0;
 		if (sourceType != null) {
 			Type lowerBoundType = (Type) PivotUtil.getLowerBound(sourceType);
 			size = environmentView.computeLookups(lowerBoundType, null, null, null);
 		}
 		if (size == 1) {
 			Operation operation = (Operation)environmentView.getContent();
 			context.setReferredOperation(expression, operation);
 			resolveOperationReturnType(expression);
 		}
 		else {
 			StringBuilder s = new StringBuilder();
 			for (OCLExpression argument : expression.getArgument()) {
 				Type argumentType = argument.getType();
 				if (argumentType instanceof LambdaType) {								// FIXME Modularize this
 					argumentType = ((LambdaType)argumentType).getResultType();
 				}
 				if (s.length() > 0) {
 					s.append(",");
 				}
 				if (argumentType != null) {
 					s.append(argumentType.toString());
 				}
 			}
 			String boundMessage;
 			if (s.length() > 0) {
 				boundMessage = DomainUtil.bind(OCLMessages.UnresolvedOperationCall_ERROR_, csOperator, sourceType, s.toString());
 			}
 			else {
 				boundMessage = DomainUtil.bind(OCLMessages.UnresolvedOperation_ERROR_, csOperator, sourceType);
 			}
 //			context.addBadExpressionError(csOperator, boundMessage);
 			context.addDiagnostic(csOperator, boundMessage);
 			Operation badOperation = getBadOperation();
 			context.setReferredOperation(expression, badOperation);
 			context.setType(expression, metaModelManager.getOclInvalidType());
 		}
 	}
 
 	protected void resolveOperationReturnType(CallExp callExp) {
 		Operation operation = null;
 		if (callExp instanceof OperationCallExp) {
 			operation = ((OperationCallExp)callExp).getReferredOperation();
 		}
 		else if (callExp instanceof LoopExp) {
 			operation = ((LoopExp)callExp).getReferredIteration();
 		}
 		if (operation == null) {
 			return;
 		}
 		Map<TemplateParameter, ParameterableElement> templateBindings = new HashMap<TemplateParameter, ParameterableElement>();
 		Type sourceType = null;
 		OCLExpression source = callExp.getSource();
 		if (source != null) {
 			sourceType = source.getType();
 		}
 		if (sourceType != null) {
 			if (operation.isStatic() && (sourceType instanceof ClassifierType)) {
 				sourceType = ((ClassifierType)sourceType).getInstanceType();
 			}
 			templateBindings.put(null, sourceType);		// Use the null key to pass OclSelf without creating an object
 		}
 		PivotUtil.getAllTemplateParameterSubstitutions(templateBindings, sourceType);
 //		PivotUtil.getAllTemplateParameterSubstitutions(templateBindings, operation);
 		TemplateSignature templateSignature = operation.getOwnedTemplateSignature();
 		if (templateSignature != null) {
 			for (TemplateParameter templateParameter : templateSignature.getOwnedParameter()) {
 				templateBindings.put(templateParameter, null);
 			}
 		}
 		@SuppressWarnings("unused")		// Should never happen; just for debugging
 		boolean isConformant = true;
 		if (callExp instanceof OperationCallExp) {
 			List<Parameter> parameters = operation.getOwnedParameter();
 			List<OCLExpression> arguments = ((OperationCallExp)callExp).getArgument();
 			int iMax = Math.min(parameters.size(), arguments.size());
 			for (int i = 0; i < iMax; i++) {
 				Parameter parameter = parameters.get(i);
 				OCLExpression argument = arguments.get(i);
 				if ((parameter != null) && (argument != null)) {
 					Type parameterType = PivotUtil.getBehavioralType(metaModelManager.getTypeWithMultiplicity(parameter));
 					Type argumentType = PivotUtil.getBehavioralType(argument.getType());
 					if (!metaModelManager.conformsTo(argumentType, parameterType, templateBindings)) {
 						isConformant = false;
 					}
 				}
 			}
 		}
 		else if (callExp instanceof LoopExp) {
 			if (callExp instanceof IterateExp) {
 				List<Parameter> accumulators = ((Iteration)operation).getOwnedAccumulator();
 				if (accumulators.size() >= 1) {
 					Parameter accumulator = accumulators.get(0);
 					Variable result = ((IterateExp)callExp).getResult();
 					if ((accumulator != null) && (result != null)) {
 						Type accumulatorType = PivotUtil.getBehavioralType(accumulator.getType());
 						Type resultType = PivotUtil.getBehavioralType(result.getType());
 						if (!metaModelManager.conformsTo(resultType, accumulatorType, templateBindings)) {
 							isConformant = false;
 						}
 					}
 				}
 			}
 			List<Parameter> parameters = ((Iteration)operation).getOwnedParameter();
 			if (parameters.size() >= 1) {
 				Parameter parameter = parameters.get(0);
 				OCLExpression body = ((LoopExp)callExp).getBody();
 				if ((parameter != null) && (body != null)) {
 					Type parameterType = parameter.getType();
 					if (parameterType instanceof LambdaType) {		// Should always be a LambdaType
 						parameterType = ((LambdaType)parameterType).getResultType();
 					}
 					Type bodyType = PivotUtil.getBehavioralType(body.getType());
 					if (!metaModelManager.conformsTo(bodyType, parameterType, templateBindings)) {
 						isConformant = false;
 					}
 				}
 			}
 		}
 		Type returnType = metaModelManager.getSpecializedType(metaModelManager.getTypeWithMultiplicity(operation), templateBindings);
 		if ((operation instanceof Iteration) && "collect".equals(operation.getName()) && (callExp instanceof LoopExp) && (returnType instanceof CollectionType)) {
 			OCLExpression body = ((LoopExp)callExp).getBody();
 			if (body != null) {
 				Type bodyType = PivotUtil.getBehavioralType(body.getType());
 				if (bodyType instanceof CollectionType) {
 					Type elementType = bodyType;
 					while (elementType instanceof CollectionType) {
 						elementType = ((CollectionType)elementType).getElementType();
 					}
 					boolean isOrdered = ((CollectionType)bodyType).isOrdered() && ((CollectionType)returnType).isOrdered();
 //					boolean isUnique = /*((CollectionType)bodyType).isUnique() &&*/ ((CollectionType)returnType).isUnique();
 					returnType = metaModelManager.getCollectionType(isOrdered, false, elementType);
 				}
 			}
 		}
 		context.setType(callExp, returnType);
 	}
 
 	protected OCLExpression resolvePropertyCallExp(NamedExpCS csNameExp, Property property) {
 		OCLExpression source = resolveNavigationSource(csNameExp, property);
 		PropertyCallExp innerExpression = context.refreshModelElement(PropertyCallExp.class, PivotPackage.Literals.PROPERTY_CALL_EXP, csNameExp);
 		innerExpression.setReferredProperty(property);
 		context.setTypeWithMultiplicity(innerExpression, property);		// FIXME resolve template parameter		
 		CallExp outerExpression = resolveNavigationFeature(csNameExp, source, property, innerExpression);
 		if (outerExpression != innerExpression) {
 			resolveOperationReturnType(outerExpression);
 		}
 		return outerExpression;
 	}
 
 	protected OCLExpression resolvePropertyNavigation(NamedExpCS csNamedExp) {
 		NamedElement namedElement = csNamedExp.getNamedElement();
 		if ((namedElement == null) || namedElement.eIsProxy()) {
 			namedElement = getBadProperty();
 			PropertyCallExp expression = context.refreshModelElement(PropertyCallExp.class, PivotPackage.Literals.PROPERTY_CALL_EXP, csNamedExp);
 			expression.setReferredProperty(null);
 //			context.installPivotUsage(csNavigatingExp, operationCallExp);		
 			context.setType(expression, metaModelManager.getOclInvalidType());
 			return expression;
 		}
 		else if (namedElement instanceof Property) {
 			return resolvePropertyCallExp(csNamedExp, (Property)namedElement);
 		}
 		else {
 			return context.addBadExpressionError(csNamedExp, "Property name expected");
 		}
 	}
 
 	protected TypeExp resolveTypeExp(ExpCS csExp, Type type) {
 		TypeExp expression = context.refreshModelElement(TypeExp.class, PivotPackage.Literals.TYPE_EXP, csExp);
 		context.setType(expression, metaModelManager.getClassifierType(type));
 		expression.setReferredType(type);
 		return expression;
 	}
 
 	protected VariableExp resolveVariableExp(NameExpCS csNameExp, VariableDeclaration variableDeclaration) {
 		VariableExp expression = context.refreshModelElement(VariableExp.class, PivotPackage.Literals.VARIABLE_EXP, csNameExp);
 		expression.setReferredVariable(variableDeclaration);
 		context.setType(expression, variableDeclaration.getType());
 		return expression;
 	}
 	  
 	@Override
 	public Element visitBinaryOperatorCS(BinaryOperatorCS csOperator) {
 		OperationCallExp expression = PivotUtil.getPivot(OperationCallExp.class, csOperator);
 		OCLExpression source = context.visitLeft2Right(OCLExpression.class, csOperator.getSource());
 		expression.setSource(source);
 		OCLExpression argument = context.visitLeft2Right(OCLExpression.class, csOperator.getArgument());
 		context.refreshList(expression.getArgument(), Collections.singletonList(argument));
 		resolveOperationCall(expression, csOperator, new BinaryOperationFilter(metaModelManager, source.getType(), argument.getType()));
 		return expression;
 	}
 
 	@Override
 	public Element visitBooleanLiteralExpCS(BooleanLiteralExpCS csBooleanLiteralExp) {
 		BooleanLiteralExp expression = PivotUtil.getPivot(BooleanLiteralExp.class, csBooleanLiteralExp);
 		expression.setBooleanSymbol(Boolean.valueOf(csBooleanLiteralExp.getName()));
 		context.setType(expression, metaModelManager.getBooleanType());
 		return expression;
 	}
 
 	@Override
 	public Element visitCollectionLiteralExpCS(CollectionLiteralExpCS csCollectionLiteralExp) {
 		Type commonType = null;
 //		InvalidLiteralExp invalidValue = null;
 		for (CollectionLiteralPartCS csPart : csCollectionLiteralExp.getOwnedParts()) {
 			CollectionLiteralPart pivotPart = context.visitLeft2Right(CollectionLiteralPart.class, csPart);
 			Type type = pivotPart.getType();
 //			if (type instanceof InvalidType) {	// FIXME Use propagated reason via InvalidType
 //				if (invalidValue == null) {
 //					invalidValue = metaModelManager.createInvalidValue(csPart, null, "Invalid Collection content", null);
 //				}
 //			}
 //			else
 			if (commonType == null) {
 				commonType = type;
 			}
 			else {
 				commonType = metaModelManager.getCommonType(commonType, type, null);
 			}
 		}
 //		if (invalidValue != null) {
 //			context.installPivotElement(csCollectionLiteralExp, invalidValue);
 //			return invalidValue;
 //		}
 		CollectionLiteralExp expression = PivotUtil.getPivot(CollectionLiteralExp.class, csCollectionLiteralExp);
 		CollectionTypeCS ownedCollectionType = csCollectionLiteralExp.getOwnedType();
 		String collectionTypeName = ownedCollectionType.getName();
 		TypedRefCS ownedElementType = ownedCollectionType.getOwnedType();
 		if (ownedElementType != null) {
 			commonType = (Type) ownedElementType.getPivot();
 		}
 		if (commonType == null) {
 			commonType = metaModelManager.createUnspecifiedType();
 		}
 		Type type = metaModelManager.getLibraryType(collectionTypeName, Collections.singletonList(commonType));
 		context.setType(expression, type);
 		expression.setKind(PivotUtil.getCollectionKind((CollectionType) type));
 		return expression;
 	}
 
 	@Override
 	public Element visitCollectionLiteralPartCS(CollectionLiteralPartCS csCollectionLiteralPart) {
 		ExpCS csFirst = csCollectionLiteralPart.getExpressionCS();
 		OCLExpression pivotFirst = context.visitLeft2Right(OCLExpression.class, csFirst);
 		ExpCS csLast = csCollectionLiteralPart.getLastExpressionCS();
 		if (csLast == null) {
 			CollectionItem expression = PivotUtil.getPivot(CollectionItem.class, csCollectionLiteralPart);	
 			expression.setItem(pivotFirst);
 		}
 		else {
 			CollectionRange expression = PivotUtil.getPivot(CollectionRange.class, csCollectionLiteralPart);
 			expression.setFirst(pivotFirst);
 			OCLExpression pivotLast = context.visitLeft2Right(OCLExpression.class, csLast);
 			expression.setLast(pivotLast);
 		}
 		Type type = pivotFirst.getType();
 		if (csLast != null) {
 			OCLExpression pivotLast = PivotUtil.getPivot(OCLExpression.class, csLast);
 			Type secondType = pivotLast.getType();
 			type = metaModelManager.getCommonType(type, secondType, null);
 		}
 		CollectionLiteralPart expression = PivotUtil.getPivot(CollectionLiteralPart.class, csCollectionLiteralPart);
 		context.setType(expression, type);
 		return expression;
 	}
 
 	@Override
 	public Element visitCollectionTypeCS(CollectionTypeCS object) {
 		return null;
 	}
 
 	@Override
 	public Element visitConstructorExpCS(ConstructorExpCS csConstructorExp) {
 		ConstructorExp expression = PivotUtil.getPivot(ConstructorExp.class, csConstructorExp);	
 		expression.setType((Type) csConstructorExp.getNamedElement());
 		for (ConstructorPartCS csPart : csConstructorExp.getOwnedParts()) {
 			context.visitLeft2Right(ConstructorPart.class, csPart);
 		}
 		return expression;
 	}
 
 	@Override
 	public Element visitConstructorPartCS(ConstructorPartCS csConstructorPart) {
 		ConstructorPart pivotElement = PivotUtil.getPivot(ConstructorPart.class, csConstructorPart);	
 		pivotElement.setReferredProperty(csConstructorPart.getProperty());
 		OCLExpression initExpression = context.visitLeft2Right(OCLExpression.class, csConstructorPart.getInitExpression());
 		pivotElement.setInitExpression(initExpression);
 		return pivotElement;
 	}
 
 	@Override
 	public Element visitContextCS(ContextCS csContext) {
 		ExpressionInOCL pivotElement = PivotUtil.getPivot(ExpressionInOCL.class, csContext);
 		ExpCS csExpression = csContext.getOwnedExpression();
 		OCLExpression expression = context.visitLeft2Right(OCLExpression.class, csExpression);
 		if (expression != null) {
 			if (pivotElement.getBodyExpression() == null) {
 				pivotElement.setBodyExpression(expression);
 				context.setType(pivotElement, expression.getType());
 			}
 			else {
 				pivotElement.setMessageExpression(expression);
 			}
 //			context.setType(pivotElement, expression.getType());
 		}
 		return pivotElement;
 	}
 
 	@Override
 	public Element visitExpCS(ExpCS object) {
 		return null;
 	}
 
 	@Override
 	public Element visitExpSpecificationCS(ExpSpecificationCS object) {
 		ExpressionInOCL pivotElement = PivotUtil.getPivot(ExpressionInOCL.class, object);
 		pivotElement.getLanguage().add(PivotConstants.OCL_LANGUAGE);
 		OCLExpression expression = context.visitLeft2Right(OCLExpression.class, object.getOwnedExpression());
 		pivotElement.setBodyExpression(expression);
 		return pivotElement;
 	}
 
 	@Override
 	public Element visitIfExpCS(IfExpCS csIfExp) {
 		IfExp expression = PivotUtil.getPivot(IfExp.class, csIfExp);
 		expression.setCondition(context.visitLeft2Right(OCLExpression.class, csIfExp.getCondition()));
 		OCLExpression thenExpression = context.visitLeft2Right(OCLExpression.class, csIfExp.getThenExpression());
 		expression.setThenExpression(thenExpression);
 		OCLExpression elseExpression = context.visitLeft2Right(OCLExpression.class, csIfExp.getElseExpression());
 		expression.setElseExpression(elseExpression);
 		context.setType(expression, metaModelManager.getCommonType(thenExpression.getType(), elseExpression.getType(), null));
 		return expression;
 	}
 
 	@Override
 	public Element visitIndexExpCS(IndexExpCS csIndexExp) {
 		// Navigating completions are orchestrated by the SimpleNamedExpCS.
 		return null;
 	}
 
 	@Override
 	public Element visitInfixExpCS(InfixExpCS csInfixExp) {
 		//
 		//	Find the root.
 		//
 		OperatorCS csRoot = csInfixExp.getOwnedOperator().get(0);
 		for (OperatorCS csParent = csRoot.getParent(); csParent != null; csParent = csParent.getParent()) {
 			csRoot = csParent;
 		}
 		//
 		//	Build the corresponding AST and reuse as the Infix node.
 		//
 		OCLExpression pivot = context.visitLeft2Right(OCLExpression.class, csRoot);		
 		context.installPivotUsage(csInfixExp, pivot);
 		return pivot;
 	}
 
 	@Override
 	public Element visitInvalidLiteralExpCS(InvalidLiteralExpCS csInvalidLiteralExp) {
 		InvalidLiteralExp expression = PivotUtil.getPivot(InvalidLiteralExp.class, csInvalidLiteralExp);
 		if (expression == null) {
 			expression = metaModelManager.createInvalidExpression();
 		}
 //		expression.setType(metaModelManager.getOclInvalidType());
 		context.installPivotUsage(csInvalidLiteralExp, expression);
 		return expression;
 	}
 
 	@Override
 	public Element visitInvocationExpCS(InvocationExpCS csNavigatingExp) {
 		OperatorCS csParent = csNavigatingExp.getParent();
 		if ((csParent instanceof NavigationOperatorCS)
 		 && (csNavigatingExp != csParent.getSource())) {
 			return PivotUtil.getPivot(OCLExpression.class, csNavigatingExp);
 		}
 		else {
 			return resolveOperation(csNavigatingExp);
 		}
 	}
 
 	@Override
 	public Element visitLetExpCS(LetExpCS csLetExp) {
 		// Each CS Let Variable becomes a Pivot LetExpression and Variable
 		// The CS Let therefore just re-uses the Pivot of the first CS Let Variable
 		LetExp firstLetExp = null;
 		LetExp lastLetExp = null;
 		for (LetVariableCS csLetVariable : csLetExp.getVariable()) {
 			Variable variable = PivotUtil.getPivot(Variable.class, csLetVariable);
 			LetExp letExp;
 			EObject variableContainer = variable.eContainer();
 			if (variableContainer instanceof LetExp) {
 				letExp = (LetExp)variableContainer;
 			}
 			else {
 				letExp = context.refreshModelElement(LetExp.class, PivotPackage.Literals.LET_EXP, null); // FIXME reuse
 			}
 			letExp.setVariable(variable);		
 			ExpCS csInitExpression = csLetVariable.getInitExpression();
 			OCLExpression initExpression = context.visitLeft2Right(OCLExpression.class, csInitExpression);
 			variable.setInitExpression(initExpression);
 			Type initType = initExpression != null ? initExpression.getType() : null;
 			TypedRefCS csVariableType = csLetVariable.getOwnedType();
 			Type variableType = csVariableType != null ? PivotUtil.getPivot(Type.class, csVariableType) : null;
 			if (variableType == null) {
 				variableType = initType;
 			}
 			context.setType(variable, variableType);
 			
 			if (lastLetExp != null) {
 				lastLetExp.setIn(letExp);
 				context.installPivotUsage(csLetExp, letExp);
 			}
 			else {
 				firstLetExp = letExp;
 				context.installPivotUsage(csLetExp, firstLetExp);
 			}
 			lastLetExp = letExp;
 		}
 		if (lastLetExp != null) {
 			OCLExpression in = context.visitLeft2Right(OCLExpression.class, csLetExp.getIn());
 			lastLetExp.setIn(in);
 			Type type = in.getType();
 			for (OCLExpression letExp = firstLetExp; (letExp != in) && (letExp != null); letExp = ((LetExp)letExp).getIn()) {
 				context.setType(letExp, type);
 			}
 		}
 		return firstLetExp;
 	}
 
 	@Override
 	public Element visitLetVariableCS(LetVariableCS csLetVariable) {
 		return null;	// Handled by parent
 	}
 
 	@Override
 	public Element visitNameExpCS(NameExpCS csNameExp) {
 		EObject eContainer = csNameExp.eContainer();
 		if (eContainer instanceof InvocationExpCS) {
 			EObject eContainerContainer = eContainer.eContainer();
 			if (eContainerContainer instanceof NamedExpCS) {
 				logger.warn("Unsupported '" + eContainerContainer.eClass().getName() + "' for () navigation");
 			}
 			return null;
 		}
 //		else if (eContainer instanceof IndexExpCS) {
 //			EObject eContainerContainer = eContainer.eContainer();
 //			if (eContainerContainer instanceof NamedExpCS) {
 //				logger.warn("Unsupported '" + eContainerContainer.eClass().getName() + "' for [] navigation");
 //			}
 //			return new IndexExpCSCompletion(context, (IndexExpCS) eContainer);
 //		}
 		else {
 			Element element = csNameExp.getPathName().getElement();
 			if ((element == null) || element.eIsProxy()) {
 				Element pivot = csNameExp.getPivot();
 				if (pivot instanceof InvalidLiteralExp) {
 					return pivot;
 				}
 				InvalidLiteralExp invalidLiteralExp = metaModelManager.createInvalidExpression();
 				context.installPivotUsage(csNameExp, invalidLiteralExp);
 				return invalidLiteralExp;
 //				return context.addBadProxyError(EssentialOCLCSTPackage.Literals.NAME_EXP_CS__ELEMENT, csNameExp);
 			}
 			else if (element instanceof VariableDeclaration) {
 				return resolveVariableExp(csNameExp, (VariableDeclaration)element);
 			}
 			else if (element instanceof Property) {
 				return resolvePropertyCallExp(csNameExp, (Property) element);
 			}
 			else if (element instanceof Operation) {
 				return context.addBadExpressionError(csNameExp, "No parameters for operation " + ((Operation)element).getName());
 			}
 			else if (element instanceof Type) {
 				return resolveTypeExp(csNameExp, (Type) element);
 			}
 			else if (element instanceof EnumerationLiteral) {
 				return resolveEnumLiteral(csNameExp, (EnumerationLiteral) element);
 			}
 			else {
 				return context.addBadExpressionError(csNameExp, "Unsupported NameExpCS " + element.eClass().getName());		// FIXME
 			}
 		}
 	}
 
 	@Override
 	public Element visitNavigatingArgCS(NavigatingArgCS csNavigatingArg) {
 		OCLExpression pivot = PivotUtil.getPivot(OCLExpression.class, csNavigatingArg.getName());
 		context.installPivotUsage(csNavigatingArg, pivot);
 		return pivot;
 	}
 
 	@Override
 	public OCLExpression visitNavigationOperatorCS(NavigationOperatorCS csOperator) {
 		@SuppressWarnings("unused")
 		OCLExpression sourceExp = context.visitLeft2Right(OCLExpression.class, csOperator.getSource());
 		OCLExpression navigatingExp = null;
 		ExpCS argument = csOperator.getArgument();
 		if (argument instanceof InvocationExpCS) {
 			navigatingExp = resolveOperation((InvocationExpCS) argument);
 		}
 		else if (argument instanceof NamedExpCS) {
 			navigatingExp = resolvePropertyNavigation((NamedExpCS) argument);
 		}
 		else {
 			navigatingExp = context.addBadExpressionError(argument, "bad navigation argument");
 		}
 		context.installPivotUsage(csOperator, navigatingExp);
 //		assert sourceExp.eContainer() != null; -- need to insert into invalidLiteralExp for bad navigation
 		return navigatingExp;
 	}
 
 	@Override
 	public Element visitNestedExpCS(NestedExpCS csNestedExp) {
 		OCLExpression pivot = context.visitLeft2Right(OCLExpression.class, csNestedExp.getSource());
 		context.installPivotUsage(csNestedExp, pivot);
 		return pivot;
 	}
 
 	@Override
 	public Element visitNullLiteralExpCS(NullLiteralExpCS csNullLiteralExp) {
 		NullLiteralExp expression = PivotUtil.getPivot(NullLiteralExp.class, csNullLiteralExp);
 		context.setType(expression, metaModelManager.getOclVoidType());
 		return expression;
 	}
 
 	@Override
 	public Element visitNumberLiteralExpCS(NumberLiteralExpCS csNumberLiteralExp) {
 		NumericLiteralExp expression = PivotUtil.getPivot(NumericLiteralExp.class, csNumberLiteralExp);
 		if (expression instanceof UnlimitedNaturalLiteralExp) {
 			context.setType(expression, metaModelManager.getUnlimitedNaturalType());
 		}
 		else if (expression instanceof IntegerLiteralExp) {
 			context.setType(expression, metaModelManager.getIntegerType());
 		}
 		else {
 			context.setType(expression, metaModelManager.getRealType());
 		}
 		return expression;
 	}
 
 	@Override
 	public Element visitOperatorCS(OperatorCS object) {
 		return null;
 	}
 
 	@Override
 	public Element visitPrefixExpCS(PrefixExpCS csPrefixExp) {
 		UnaryOperatorCS csRoot = csPrefixExp.getOwnedOperator().get(0);
 		if (csPrefixExp.eContainer() instanceof InfixExpCS) {
 			// PrefixExpCS embedded in InfixExpCS is resolved as part of the Infix tree;		
 		}
 		else {
 //			initializePrefixOperators(csPrefixExp, null);
 			context.visitLeft2Right(OCLExpression.class, csRoot);		
 		}
 		OCLExpression pivotElement = PivotUtil.getPivot(OCLExpression.class, csRoot);
 		context.installPivotUsage(csPrefixExp, pivotElement);
 		return pivotElement;
 	}
 
 	@Override
 	public Element visitSelfExpCS(SelfExpCS csSelfExp) {	// FIXME Just use VariableExpCS
 		VariableExp expression = PivotUtil.getPivot(VariableExp.class, csSelfExp);
 		EnvironmentView environmentView = new EnvironmentView(metaModelManager, PivotPackage.Literals.EXPRESSION_IN_OCL__CONTEXT_VARIABLE, Environment.SELF_VARIABLE_NAME);
 		ElementCS parent = csSelfExp.getLogicalParent();
 		BaseScopeView.computeLookups(environmentView, parent, csSelfExp, csSelfExp.eContainingFeature(), null);
 		VariableDeclaration variableDeclaration = (VariableDeclaration) environmentView.getContent();
 		expression.setReferredVariable(variableDeclaration);
 		context.setType(expression, variableDeclaration != null ? variableDeclaration.getType() : metaModelManager.getOclVoidType());
 		return expression;
 	}
 
 	@Override
 	public Element visitStringLiteralExpCS(StringLiteralExpCS csStringLiteralExp) {
 		StringLiteralExp pivotElement = PivotUtil.getPivot(StringLiteralExp.class, csStringLiteralExp);
 		context.setType(pivotElement, metaModelManager.getStringType());
 		return pivotElement;
 	}
 
 	@Override
 	public Element visitTupleLiteralExpCS(TupleLiteralExpCS csTupleLiteralExp) {
 		TupleLiteralExp expression = PivotUtil.getPivot(TupleLiteralExp.class, csTupleLiteralExp);	
 		for (TupleLiteralPartCS csPart : csTupleLiteralExp.getOwnedParts()) {
 			context.visitLeft2Right(TupleLiteralPart.class, csPart);
 		}
 		String tupleTypeName = "Tuple"; //ownedCollectionType.getName();
 		Type type = metaModelManager.getTupleType(tupleTypeName, expression.getPart(), null);
 		context.setType(expression, type);
 		return expression;
 	}
 
 	@Override
 	public Element visitTupleLiteralPartCS(TupleLiteralPartCS csTupleLiteralPart) {
 		TupleLiteralPart pivotElement = PivotUtil.getPivot(TupleLiteralPart.class, csTupleLiteralPart);	
 		OCLExpression initExpression = context.visitLeft2Right(OCLExpression.class, csTupleLiteralPart.getInitExpression());
 		pivotElement.setInitExpression(initExpression);
 		TypedRefCS csType = csTupleLiteralPart.getOwnedType();
 		Type type = csType != null ? PivotUtil.getPivot(Type.class, csType) : initExpression.getType();
 		context.setType(pivotElement, type);
 		return pivotElement;
 	}
 
 	@Override
 	public Element visitTypeLiteralExpCS(TypeLiteralExpCS csTypeLiteralExp) {
 		TypedRefCS csType = csTypeLiteralExp.getOwnedType();
 //		context.visitInOrder(csType, null);
 		Type type = PivotUtil.getPivot(Type.class, csType);
 		return resolveTypeExp(csTypeLiteralExp, type);
 	}
 
 	@Override
 	public Element visitUnaryOperatorCS(UnaryOperatorCS csOperator) {
 		OperationCallExp expression = PivotUtil.getPivot(OperationCallExp.class, csOperator);
 		OCLExpression source = context.visitLeft2Right(OCLExpression.class, csOperator.getSource());
 		expression.setSource(source);
 		resolveOperationCall(expression, csOperator, new UnaryOperationFilter(metaModelManager, source.getType()));
 		return expression;
 	}
 
 	@Override
 	public Element visitUnlimitedNaturalLiteralExpCS(UnlimitedNaturalLiteralExpCS csUnlimitedNaturalLiteralExp) {
 		UnlimitedNaturalLiteralExp expression = PivotUtil.getPivot(UnlimitedNaturalLiteralExp.class, csUnlimitedNaturalLiteralExp);
 		context.setType(expression, metaModelManager.getUnlimitedNaturalType());
 		return expression;
 	}
 
 	@Override
 	public Element visitVariableCS(VariableCS csVariable) {
 		Variable variable = PivotUtil.getPivot(Variable.class, csVariable);
 		OCLExpression initExpression = PivotUtil.getPivot(OCLExpression.class, csVariable.getInitExpression());
 		TypedRefCS csType = csVariable.getOwnedType();
 		Type type;
 		if (csType != null) {
 			type = PivotUtil.getPivot(Type.class, csType);
 		}
 		else {
 			type = initExpression.getType();
 			// FIXME deduction is more complex that this
 		}
 		variable.setInitExpression(initExpression);
 		context.setType(variable, type);
 		return variable;
 	}	
 }
