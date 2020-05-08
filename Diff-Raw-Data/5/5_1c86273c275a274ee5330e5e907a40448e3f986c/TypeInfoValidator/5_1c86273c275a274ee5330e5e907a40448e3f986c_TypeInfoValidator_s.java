 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.validation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.core.builder.IBuildContext;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.internal.javascript.parser.JSDocValidatorFactory.TypeChecker;
 import org.eclipse.dltk.internal.javascript.ti.ElementValue;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.ti.ITypeInferenceContext;
 import org.eclipse.dltk.internal.javascript.ti.JSMethod;
 import org.eclipse.dltk.internal.javascript.ti.MemberPredicates;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.internal.javascript.ti.TypeInferencerVisitor;
 import org.eclipse.dltk.javascript.ast.Argument;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.JSNode;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.PropertyExpressionUtils;
 import org.eclipse.dltk.javascript.parser.Reporter;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinference.ValueReferenceUtil;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IMethod;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IParameter;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IVariable;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.JSType2;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.ArrayType;
 import org.eclipse.dltk.javascript.typeinfo.model.Element;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.MapType;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterKind;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.RecordType;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeRef;
 import org.eclipse.dltk.javascript.typeinfo.model.UnionType;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.osgi.util.NLS;
 
 public class TypeInfoValidator implements IBuildParticipant {
 
 	public void build(IBuildContext context) throws CoreException {
 		final Script script = JavaScriptValidations.parse(context);
 		if (script == null) {
 			return;
 		}
 		final TypeInferencer2 inferencer = new TypeInferencer2();
 		inferencer.setModelElement(context.getSourceModule());
 		final Reporter reporter = JavaScriptValidations.createReporter(context);
 		final ValidationVisitor visitor = new ValidationVisitor(inferencer,
 				reporter);
 		inferencer.setVisitor(visitor);
 		final TypeChecker typeChecker = new TypeChecker(inferencer, reporter);
 		visitor.setJSDocTypeChecker(typeChecker);
 		inferencer.doInferencing(script);
 		typeChecker.validate();
 	}
 
 	private static enum VisitorMode {
 		NORMAL, CALL
 	}
 
 	private interface ExpressionValidator {
 		void call();
 	}
 
 	private static class StackedExpressionValidator implements
 			ExpressionValidator {
 
 		private final List<ExpressionValidator> stacked = new ArrayList<TypeInfoValidator.ExpressionValidator>();
 		private final Reporter reporter;
 
 		public StackedExpressionValidator(Reporter reporter) {
 			this.reporter = reporter;
 		}
 
 		public void call() {
 			for (ExpressionValidator validator : stacked) {
 				int count = reporter.getProblemCount();
 				validator.call();
 				if (reporter.getProblemCount() != count)
 					break;
 			}
 		}
 
 		public void push(ExpressionValidator expressionValidator) {
 			stacked.add(expressionValidator);
 		}
 	}
 
 	private static class CallExpressionValidator implements ExpressionValidator {
 		private final CallExpression node;
 		private final IValueReference reference;
 		private final ValidationVisitor visitor;
 		private final IValueReference[] arguments;
 		private final List<Method> methods;
 
 		public CallExpressionValidator(CallExpression node,
 				IValueReference reference, IValueReference[] arguments,
 				List<Method> methods, ValidationVisitor visitor) {
 			this.node = node;
 			this.reference = reference;
 			this.arguments = arguments;
 			this.methods = methods;
 			this.visitor = visitor;
 		}
 
 		public void call() {
 			visitor.validateCallExpression(node, reference, arguments, methods);
 		}
 	}
 
 	private static class ReturnNode {
 
 		final ReturnStatement node;
 		final IValueReference returnValueReference;
 
 		public ReturnNode(ReturnStatement node,
 				IValueReference returnValueReference) {
 			this.node = node;
 			this.returnValueReference = returnValueReference;
 		}
 
 	}
 
 	private static class TestReturnStatement implements ExpressionValidator {
 
 		private final List<ReturnNode> lst;
 		private final Reporter reporter;
 		private final IMethod jsMethod;
 
 		public TestReturnStatement(IMethod jsMethod, List<ReturnNode> lst,
 				Reporter reporter) {
 			this.jsMethod = jsMethod;
 			this.lst = lst;
 			this.reporter = reporter;
 		}
 
 		public void call() {
 			JSType2 firstType = null;
 			for (int i = 0; i < lst.size(); i++) {
 				ReturnNode element = lst.get(i);
 				JSType methodType = jsMethod.getType();
 				if (methodType != null
 						&& methodType.getKind() == TypeKind.RECORD) {
 					String failedPropertyTypeString = ValidationVisitor
 							.testObjectPropertyType(
 									element.returnValueReference, methodType);
 					if (failedPropertyTypeString != null) {
 						reporter.reportProblem(
 								JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 								NLS.bind(
 										ValidationMessages.DeclarationMismatchWithActualReturnType,
 										new String[] { jsMethod.getName(),
 												TypeUtil.getName(methodType),
 												failedPropertyTypeString }),
 								element.node.sourceStart(), element.node
 										.sourceEnd());
 					}
 					return;
 				}
 				JSType2 type = JSTypeSet.normalize(JavaScriptValidations
 						.typeOf(element.returnValueReference));
 
 				if (type != null
 						&& methodType != null
 						&& !JSTypeSet.normalize(methodType).isAssignableFrom(
 								type)) {
 					ReturnStatement node = element.node;
 					reporter.reportProblem(
 							JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 							NLS.bind(
 									ValidationMessages.DeclarationMismatchWithActualReturnType,
 									new String[] { jsMethod.getName(),
 											TypeUtil.getName(methodType),
 											TypeUtil.getName(type) }), node
 									.sourceStart(), node.sourceEnd());
 				}
 
 				if (firstType == null && type != null) {
 					firstType = type;
 				}
 			}
 
 			if (firstType != null) {
 				for (int i = 1; i < lst.size(); i++) {
 					ReturnNode next = lst.get(i);
 					JSType2 nextType = JSTypeSet
 							.normalize(JavaScriptValidations
 									.typeOf(next.returnValueReference));
 					if (nextType != null
 							&& (!nextType.isAssignableFrom(firstType) && !firstType
 									.isAssignableFrom(nextType))) {
 
 						reporter.reportProblem(
 								JavaScriptProblems.RETURN_INCONSISTENT,
 								NLS.bind(
 										ValidationMessages.ReturnTypeInconsistentWithPreviousReturn,
 										new String[] {
 												TypeUtil.getName(nextType),
 												TypeUtil.getName(firstType) }),
 								next.node.sourceStart(), next.node.sourceEnd());
 
 					}
 				}
 			}
 		}
 
 	}
 
 	private static class NotExistingIdentiferValidator implements
 			ExpressionValidator {
 		private final Expression identifer;
 		private final IValueReference reference;
 		private final ValidationVisitor visitor;
 
 		public NotExistingIdentiferValidator(Expression identifer,
 				IValueReference reference, ValidationVisitor visitor) {
 			this.identifer = identifer;
 			this.reference = reference;
 			this.visitor = visitor;
 		}
 
 		public void call() {
 			visitor.validate(identifer, reference);
 		}
 	}
 
 	private static class NewExpressionValidator implements ExpressionValidator {
 
 		private final NewExpression node;
 		private final IValueReference reference;
 		final IValueCollection collection;
 		private final ValidationVisitor validator;
 
 		public NewExpressionValidator(NewExpression node,
 				IValueReference reference, IValueCollection collection,
 				ValidationVisitor validator) {
 			this.node = node;
 			this.reference = reference;
 			this.collection = collection;
 			this.validator = validator;
 		}
 
 		public void call() {
 			validator.checkExpressionType(collection, node.getObjectClass(),
 					reference);
 		}
 
 	}
 
 	private static class TypeValidator implements ExpressionValidator {
 
 		final ValidationVisitor validator;
 		final IValueReference reference;
 		final ASTNode node;
 
 		public TypeValidator(ValidationVisitor validator,
 				IValueReference reference, ASTNode node) {
 			this.validator = validator;
 			this.reference = reference;
 			this.node = node;
 		}
 
 		public void call() {
 			validator.checkExpressionType(null, node, reference);
 		}
 	}
 
 	private static class PropertyExpressionHolder implements
 			ExpressionValidator {
 		private final PropertyExpression node;
 		private final IValueReference reference;
 		private final ValidationVisitor visitor;
 		private final boolean exists;
 
 		public PropertyExpressionHolder(PropertyExpression node,
 				IValueReference reference, ValidationVisitor visitor,
 				boolean exists) {
 			this.node = node;
 			this.reference = reference;
 			this.visitor = visitor;
 			this.exists = exists;
 		}
 
 		public void call() {
 			visitor.validateProperty(node, reference, exists);
 		}
 	}
 
 	public static class ValidationVisitor extends TypeInferencerVisitor {
 
 		private final Reporter reporter;
 
 		private final List<ExpressionValidator> expressionValidators = new ArrayList<ExpressionValidator>();
 
 		public ValidationVisitor(ITypeInferenceContext context,
 				Reporter reporter) {
 			super(context);
 			this.reporter = reporter;
 			setProblemReporter(reporter);
 		}
 
 		private final Map<ASTNode, VisitorMode> modes = new IdentityHashMap<ASTNode, VisitorMode>();
 
 		private final Stack<ASTNode> visitStack = new Stack<ASTNode>();
 
 		private StackedExpressionValidator stackedExpressionValidator;
 
 		@Override
 		public IValueReference visit(ASTNode node) {
 			boolean rootNode = visitStack.isEmpty();
 			visitStack.push(node);
 			try {
 				return super.visit(node);
 			} finally {
 				if (rootNode) {
 					for (ExpressionValidator call : expressionValidators
 							.toArray(new ExpressionValidator[expressionValidators
 									.size()])) {
 						call.call();
 					}
 				}
 				visitStack.pop();
 			}
 		}
 
 		private VisitorMode currentMode() {
 			final VisitorMode mode = modes.get(visitStack.peek());
 			return mode != null ? mode : VisitorMode.NORMAL;
 		}
 
 		@Override
 		public IValueReference visitNewExpression(NewExpression node) {
 			boolean started = false;
 			if (!(node.getObjectClass() instanceof FunctionStatement))
 				started = startExpressionValidator();
 			try {
 				IValueReference reference = super.visitNewExpression(node);
 				pushExpressionValidator(new NewExpressionValidator(node,
 						reference, peekContext(), this));
 				return reference;
 			} finally {
 				if (started)
 					stopExpressionValidator();
 			}
 		}
 
 		private final Stack<FunctionScope> functionScopes = new Stack<FunctionScope>();
 
 		public static class FunctionScope {
 			final List<ReturnNode> returnNodes = new ArrayList<ReturnNode>();
 		}
 
 		public void enterFunctionScope() {
 			functionScopes.push(new FunctionScope());
 		}
 
 		public void leaveFunctionScope(IMethod method) {
 			final FunctionScope scope = functionScopes.pop();
 			if (method != null) {
 				if (!scope.returnNodes.isEmpty()) {
 					pushExpressionValidator(new TestReturnStatement(method,
 							scope.returnNodes, reporter));
 				} else if (method.getType() != null) {
 					final ReferenceLocation location = method.getLocation();
 					reporter.reportProblem(
 							JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 							NLS.bind(
 									ValidationMessages.DeclarationMismatchNoReturnType,
 									new String[] { method.getName(),
 											TypeUtil.getName(method.getType()) }),
 							location.getNameStart(), location.getNameEnd());
 				}
 			}
 		}
 
 		@Override
 		public IValueReference visitFunctionStatement(FunctionStatement node) {
 			validateHidesByFunction(node);
 
 			enterFunctionScope();
 			IValueReference reference = super.visitFunctionStatement(node);
 			final IMethod method = (IMethod) reference
 					.getAttribute(IReferenceAttributes.PARAMETERS);
 			leaveFunctionScope(method);
 
 			return reference;
 		}
 
 		private void validateHidesByFunction(FunctionStatement node) {
 			List<Argument> args = node.getArguments();
 			IValueCollection peekContext = peekContext();
 			for (Argument argument : args) {
 				IValueReference child = peekContext.getChild(argument
 						.getArgumentName());
 				if (child.exists()) {
 					if (child.getKind() == ReferenceKind.PROPERTY) {
 						Property property = (Property) child
 								.getAttribute(IReferenceAttributes.ELEMENT);
 						if (!property.isHideAllowed()) {
 							if (property.getDeclaringType() != null) {
 								reporter.reportProblem(
 										JavaScriptProblems.PARAMETER_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.ParameterHidesPropertyOfType,
 												new String[] {
 														argument.getArgumentName(),
 														property.getDeclaringType()
 																.getName() }),
 										argument.sourceStart(), argument
 												.sourceEnd());
 							} else {
 								reporter.reportProblem(
 										JavaScriptProblems.PARAMETER_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.ParameterHidesProperty,
 												argument.getArgumentName()),
 										argument.sourceStart(), argument
 												.sourceEnd());
 							}
 						}
 					} else if (!Boolean.TRUE.equals(child
 							.getAttribute(IReferenceAttributes.HIDE_ALLOWED))) {
 						if (child.getKind() == ReferenceKind.FUNCTION) {
 							reporter.reportProblem(
 									JavaScriptProblems.PARAMETER_HIDES_FUNCTION,
 									NLS.bind(
 											ValidationMessages.ParameterHidesFunction,
 											argument.getArgumentName()),
 									argument.sourceStart(), argument
 											.sourceEnd());
 						} else {
 							reporter.reportProblem(
 									JavaScriptProblems.PARAMETER_HIDES_VARIABLE,
 									NLS.bind(
 											ValidationMessages.ParameterHidesVariable,
 											argument.getArgumentName()),
 									argument.sourceStart(), argument
 											.sourceEnd());
 						}
 					}
 				}
 			}
 			if (node.isDeclaration()) {
 				final IValueReference child;
 				final IValueCollection parentScope = getParentScope(peekContext);
 				if (parentScope == null) {
 					child = peekContext.getChild(node.getName().getName());
 					if (getSource().equals(child.getLocation().getSource())) {
 						return;
 					}
 				} else {
 					child = parentScope.getChild(node.getName().getName());
 				}
 				if (child.exists()) {
 					if (child.getKind() == ReferenceKind.PROPERTY) {
 						Property property = (Property) child
 								.getAttribute(IReferenceAttributes.ELEMENT);
 						if (!property.isHideAllowed()) {
 							if (property.getDeclaringType() != null) {
 								reporter.reportProblem(
 										JavaScriptProblems.FUNCTION_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.FunctionHidesPropertyOfType,
 												new String[] {
 														node.getName()
 																.getName(),
 														property.getDeclaringType()
 																.getName() }),
 										node.getName().sourceStart(), node
 												.getName().sourceEnd());
 							} else {
 								reporter.reportProblem(
 										JavaScriptProblems.FUNCTION_HIDES_VARIABLE,
 										NLS.bind(
 												ValidationMessages.FunctionHidesProperty,
 												node.getName().getName()), node
 												.getName().sourceStart(), node
 												.getName().sourceEnd());
 							}
 						}
 					} else if (!Boolean.TRUE.equals(child
 							.getAttribute(IReferenceAttributes.HIDE_ALLOWED))) {
 						if (child.getKind() == ReferenceKind.FUNCTION) {
 							reporter.reportProblem(
 									JavaScriptProblems.FUNCTION_HIDES_FUNCTION,
 									NLS.bind(
 											ValidationMessages.FunctionHidesFunction,
 											node.getName().getName()), node
 											.getName().sourceStart(), node
 											.getName().sourceEnd());
 						} else {
 							reporter.reportProblem(
 									JavaScriptProblems.FUNCTION_HIDES_VARIABLE,
 									NLS.bind(
 											ValidationMessages.FunctionHidesVariable,
 											node.getName().getName()), node
 											.getName().sourceStart(), node
 											.getName().sourceEnd());
 						}
 					}
 				}
 			}
 		}
 
 		@Override
 		public IValueReference visitReturnStatement(ReturnStatement node) {
 			IValueReference returnValueReference = super
 					.visitReturnStatement(node);
 			if (returnValueReference != null
 					|| node.getValue() instanceof NullExpression) {
 				if (!functionScopes.isEmpty()) {
 					final FunctionScope scope = functionScopes.peek();
 					scope.returnNodes.add(new ReturnNode(node,
 							returnValueReference));
 				}
 			}
 			return returnValueReference;
 		}
 
 		@Override
 		public IValueReference visitCallExpression(CallExpression node) {
 			final ASTNode expression = node.getExpression();
 			modes.put(expression, VisitorMode.CALL);
 
 			boolean started = startExpressionValidator();
 			try {
 				final IValueReference reference = visit(expression);
 				modes.remove(expression);
 				if (reference == null)
 					return null;
 				final List<ASTNode> callArgs = node.getArguments();
 				IValueReference[] arguments = new IValueReference[callArgs
 						.size()];
 				final List<Method> methods = JavaScriptValidations
 						.extractElements(reference, Method.class);
 				pushExpressionValidator(new CallExpressionValidator(node,
 						reference, arguments, methods, this));
 				if (started) {
 					stopExpressionValidator();
 					started = false;
 				}
 				for (int i = 0, size = callArgs.size(); i < size; ++i) {
 					arguments[i] = visit(callArgs.get(i));
 				}
 				return reference.getChild(IValueReference.FUNCTION_OP);
 			} finally {
 				if (started)
 					stopExpressionValidator();
 			}
 		}
 
 		private void pushExpressionValidator(
 				ExpressionValidator expressionValidator) {
 			if (stackedExpressionValidator != null) {
 				stackedExpressionValidator.push(expressionValidator);
 			} else {
 				expressionValidators.add(expressionValidator);
 			}
 
 		}
 
 		private void stopExpressionValidator() {
 			if (stackedExpressionValidator != null) {
 				expressionValidators.add(stackedExpressionValidator);
 				stackedExpressionValidator = null;
 			}
 
 		}
 
 		private boolean startExpressionValidator() {
 			if (stackedExpressionValidator == null) {
 				stackedExpressionValidator = new StackedExpressionValidator(
 						reporter);
 				return true;
 			}
 			return false;
 		}
 
 		/**
 		 * @param node
 		 * @param reference
 		 * @param methods
 		 * @return
 		 */
 		protected void validateCallExpression(CallExpression node,
 				final IValueReference reference, IValueReference[] arguments,
 				List<Method> methods) {
 
 			final Expression expression = node.getExpression();
 			final Expression methodNode;
 			if (expression instanceof PropertyExpression) {
 				methodNode = ((PropertyExpression) expression).getProperty();
 			} else {
 				methodNode = expression;
 			}
 
 			if (methods == null || methods.size() == 0)
 				methods = JavaScriptValidations.extractElements(reference,
 						Method.class);
 			if (methods != null) {
 				final List<ASTNode> callArgs = node.getArguments();
 				Method method = JavaScriptValidations.selectMethod(methods,
 						arguments);
 				if (method == null) {
 					final JSType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					if (type != null) {
 						if (type.getKind() == TypeKind.JAVA) {
 							reporter.reportProblem(
 									JavaScriptProblems.WRONG_JAVA_PARAMETERS,
 									NLS.bind(
 											ValidationMessages.MethodNotSelected,
 											new String[] { reference.getName(),
 													type.getName(),
 													describeArgTypes(arguments) }),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 
 						} else {
 							// TODO also a JS error (that should be
 							// configurable)
 						}
 					}
 					return;
 				}
 				if (!validateParameterCount(method, callArgs)) {
 					reportMethodParameterError(methodNode, arguments, method);
 					return;
 				}
 				if (method.isDeprecated()) {
 					reportDeprecatedMethod(methodNode, reference, method);
 				}
 				if (JavaScriptValidations.isStatic(reference.getParent())
 						&& !method.isStatic()) {
 					JSType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					reporter.reportProblem(
 							JavaScriptProblems.INSTANCE_METHOD,
 							NLS.bind(
 									ValidationMessages.StaticReferenceToNoneStaticMethod,
 									reference.getName(), TypeUtil.getName(type)),
 							methodNode.sourceStart(), methodNode.sourceEnd());
 				} else if (reference.getParent() != null
 						&& !JavaScriptValidations.isStatic(reference
 								.getParent()) && method.isStatic()) {
 					JSType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					reporter.reportProblem(JavaScriptProblems.STATIC_METHOD,
 							NLS.bind(
 									ValidationMessages.ReferenceToStaticMethod,
 									reference.getName(), type.getName()),
 							methodNode.sourceStart(), methodNode.sourceEnd());
 				}
 				final EList<Parameter> parameters = method.getParameters();
 				if (!validateParameters(parameters, arguments)) {
 					String name = method.getName();
 					if (name == null) {
 						Identifier identifier = PropertyExpressionUtils
 								.getIdentifier(methodNode);
 						if (identifier != null)
 							name = identifier.getName();
 					}
 					reporter.reportProblem(
 							JavaScriptProblems.WRONG_PARAMETERS,
 							NLS.bind(
 									ValidationMessages.MethodNotApplicableInScript,
 									new String[] {
 											name,
 											describeParamTypes(parameters),
 											describeArgTypes(arguments,
 													parameters) }), methodNode
 									.sourceStart(), methodNode.sourceEnd());
 				}
 
 			} else {
 				Object attribute = reference.getAttribute(
 						IReferenceAttributes.PARAMETERS, true);
 				if (attribute instanceof JSMethod) {
 					JSMethod method = (JSMethod) attribute;
 					if (method.isDeprecated()) {
 						reporter.reportProblem(
 								JavaScriptProblems.DEPRECATED_FUNCTION,
 								NLS.bind(ValidationMessages.DeprecatedFunction,
 										reference.getName()), methodNode
 										.sourceStart(), methodNode.sourceEnd());
 					}
 					if (method.isPrivate() && reference.getParent() != null
 							&& !isThisCall(expression)) {
 						reporter.reportProblem(
 								JavaScriptProblems.PRIVATE_FUNCTION, NLS.bind(
 										ValidationMessages.PrivateFunction,
 										reference.getName()), methodNode
 										.sourceStart(), methodNode.sourceEnd());
 					}
 					List<IParameter> parameters = method.getParameters();
 					if (!validateParameters(parameters, arguments)) {
 						String name = method.getName();
 						if (name == null) {
 							Identifier identifier = PropertyExpressionUtils
 									.getIdentifier(methodNode);
 							if (identifier != null)
 								name = identifier.getName();
 						}
 						reporter.reportProblem(
 								JavaScriptProblems.WRONG_PARAMETERS,
 								NLS.bind(
 										ValidationMessages.MethodNotApplicableInScript,
 										new String[] {
 												name,
 												describeParamTypes(parameters),
 												describeArgTypes(arguments,
 														parameters) }),
 								methodNode.sourceStart(), methodNode
 										.sourceEnd());
 					}
 				} else if (!isArrayLookup(expression)
 						&& !isUntypedParameter(reference)) {
 
 					final JSType type = JavaScriptValidations.typeOf(reference
 							.getParent());
 					if (type != null) {
 						if (type.getKind() == TypeKind.JAVA) {
 							reporter.reportProblem(
 									JavaScriptProblems.UNDEFINED_JAVA_METHOD,
 									NLS.bind(
 											ValidationMessages.UndefinedMethod,
 											reference.getName(), type.getName()),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 						} else if (JavaScriptValidations.isStatic(reference
 								.getParent())
 								&& hasInstanceMethod(type, reference.getName())) {
 							reporter.reportProblem(
 									JavaScriptProblems.INSTANCE_METHOD,
 									NLS.bind(
 											ValidationMessages.StaticReferenceToNoneStaticMethod,
 											reference.getName(), type.getName()),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 						} else if (!reference.exists()) {
 							reporter.reportProblem(
 									JavaScriptProblems.UNDEFINED_METHOD,
 									NLS.bind(
 											ValidationMessages.UndefinedMethodInScript,
 											reference.getName()), methodNode
 											.sourceStart(), methodNode
 											.sourceEnd());
 						}
 					} else {
 						JSType referenceType = JavaScriptValidations
 								.typeOf(reference);
 						if (referenceType instanceof TypeRef) {
 							Type t = ((TypeRef) referenceType).getTarget();
 							while (t != null) {
 								if (t.getName().equals(ITypeNames.FUNCTION)) {
 									return;
 								}
 								t = t.getSuperType();
 							}
 						}
 						if (expression instanceof NewExpression) {
 							if (reference.getKind() == ReferenceKind.TYPE) {
 								return;
 							}
 							JSType newType = JavaScriptValidations
 									.typeOf(reference);
 							if (newType != null) {
 								return;
 							}
 
 						}
 						IValueReference parent = reference;
 						while (parent != null) {
 							if (parent.getName() == IValueReference.ARRAY_OP) {
 								// ignore array lookup function calls
 								// like: array[1](),
 								// those are dynamic.
 								return;
 							}
 							parent = parent.getParent();
 						}
 						if (expression instanceof NewExpression) {
 
 							reporter.reportProblem(
 									JavaScriptProblems.UNKNOWN_TYPE, NLS.bind(
 											ValidationMessages.UnknownType,
 											((NewExpression) expression)
 													.getObjectClass()
 													.toSourceString("")),
 									methodNode.sourceStart(), methodNode
 											.sourceEnd());
 
 						} else {
 							reporter.reportProblem(
 									JavaScriptProblems.UNDEFINED_METHOD,
 									NLS.bind(
 											ValidationMessages.UndefinedMethodInScript,
 											reference.getName()), methodNode
 											.sourceStart(), methodNode
 											.sourceEnd());
 						}
 					}
 				}
 			}
 			return;
 		}
 
 		/**
 		 * Checks if the passed reference is an untyped parameter. This method
 		 * helps to identify the common case of callbacks.
 		 * 
 		 * @param reference
 		 * @return
 		 */
 		private boolean isUntypedParameter(IValueReference reference) {
 			return reference.getKind() == ReferenceKind.ARGUMENT
 					&& reference.getDeclaredType() == null;
 		}
 
 		private boolean isThisCall(Expression expression) {
 			return expression instanceof PropertyExpression
 					&& ((PropertyExpression) expression).getObject() instanceof ThisExpression
 					&& ((PropertyExpression) expression).getProperty() instanceof Identifier;
 		}
 
 		private boolean hasInstanceMethod(JSType type, String name) {
 			return ElementValue.findMember(type, name,
 					MemberPredicates.NON_STATIC) != null;
 		}
 
 		private boolean isArrayLookup(ASTNode expression) {
 			ASTNode walker = expression;
 			while (walker != null) {
 				if (walker instanceof GetArrayItemExpression)
 					return true;
 				if (walker instanceof PropertyExpression) {
 					if (((PropertyExpression) walker).getObject() instanceof GetArrayItemExpression)
 						return true;
 				}
 				if (walker instanceof JSNode) {
 					walker = ((JSNode) walker).getParent();
 				} else
 					return false;
 			}
 			return false;
 		}
 
 		private void reportDeprecatedMethod(ASTNode methodNode,
 				IValueReference reference, Method method) {
 			if (method.getDeclaringType() != null) {
 				reporter.reportProblem(
 						JavaScriptProblems.DEPRECATED_METHOD,
 						NLS.bind(ValidationMessages.DeprecatedMethod, reference
 								.getName(), method.getDeclaringType().getName()),
 						methodNode.sourceStart(), methodNode.sourceEnd());
 			} else {
 				reporter.reportProblem(JavaScriptProblems.DEPRECATED_METHOD,
 						NLS.bind(ValidationMessages.DeprecatedTopLevelMethod,
 								reference.getName()), methodNode.sourceStart(),
 						methodNode.sourceEnd());
 			}
 		}
 
 		private void reportMethodParameterError(ASTNode methodNode,
 				IValueReference[] arguments, Method method) {
 			if (method.getDeclaringType() != null) {
 				IProblemIdentifier problemId = JavaScriptProblems.WRONG_PARAMETERS;
 				if (method.getDeclaringType().getKind() == TypeKind.JAVA) {
 					problemId = JavaScriptProblems.WRONG_JAVA_PARAMETERS;
 				}
 				reporter.reportProblem(problemId, NLS.bind(
 						ValidationMessages.MethodNotApplicable,
 						new String[] { method.getName(),
 								describeParamTypes(method.getParameters()),
 								method.getDeclaringType().getName(),
 								describeArgTypes(arguments) }), methodNode
 						.sourceStart(), methodNode.sourceEnd());
 			} else {
 				reporter.reportProblem(JavaScriptProblems.WRONG_PARAMETERS, NLS
 						.bind(ValidationMessages.TopLevelMethodNotApplicable,
 								new String[] {
 										method.getName(),
 										describeParamTypes(method
 												.getParameters()),
 										describeArgTypes(arguments) }),
 						methodNode.sourceStart(), methodNode.sourceEnd());
 			}
 		}
 
 		private boolean validateParameters(List<IParameter> parameters,
 				IValueReference[] arguments) {
 			if (arguments.length > parameters.size()
 					&& !(parameters.size() > 0 && parameters.get(
 							parameters.size() - 1).isVarargs()))
 				return false;
 			int testTypesSize = parameters.size();
 			if (parameters.size() > arguments.length) {
 				for (int i = arguments.length; i < parameters.size(); i++) {
 					final IParameter p = parameters.get(i);
 					if (!p.isOptional() && !p.isVarargs())
 						return false;
 				}
 				testTypesSize = arguments.length;
 			} else if (parameters.size() < arguments.length) {
 				// is var args..
 				testTypesSize = parameters.size() - 1;
 			}
 
 			for (int i = 0; i < testTypesSize; i++) {
 				IValueReference argument = arguments[i];
 				IParameter parameter = parameters.get(i);
 				if (parameter.getType() instanceof RecordType
 						&& argument != null) {
 					Set<String> argumentsChildren = argument
 							.getDirectChildren();
 					for (Member member : ((RecordType) parameter.getType())
 							.getMembers()) {
 						if (argumentsChildren.contains(member.getName())) {
 							if (member.getType() != null) {
 								IValueReference child = argument
 										.getChild(member.getName());
 								if (!testArgumentType(member.getType(), child))
 									return false;
 							}
 						} else if (member
 								.getAttribute(IReferenceAttributes.OPTIONAL) == null) {
 							return false;
 						}
 					}
 
 				} else {
 					JSType paramType = parameter.getType();
 					if (!testArgumentType(paramType, argument))
 						return false;
 				}
 			}
 			// test var args
 			if (parameters.size() < arguments.length) {
 				int varargsParameter = parameters.size() - 1;
 				JSType paramType = parameters.get(varargsParameter).getType();
 
 				for (int i = varargsParameter; i < arguments.length; i++) {
 					IValueReference argument = arguments[i];
 					if (!testArgumentType(paramType, argument))
 						return false;
 				}
 
 			}
 			return true;
 		}
 
 		private boolean validateParameters(EList<Parameter> parameters,
 				IValueReference[] arguments) {
 			if (arguments.length > parameters.size()
 					&& !(parameters.size() > 0 && parameters.get(
 							parameters.size() - 1).getKind() == ParameterKind.VARARGS))
 				return false;
 			int testTypesSize = parameters.size();
 			if (parameters.size() > arguments.length) {
 				for (int i = arguments.length; i < parameters.size(); i++) {
 					final ParameterKind pkind = parameters.get(i).getKind();
 					if (pkind != ParameterKind.OPTIONAL
 							&& pkind != ParameterKind.VARARGS)
 						return false;
 				}
 				testTypesSize = arguments.length;
 			} else if (parameters.size() < arguments.length) {
 				// is var args..
 				testTypesSize = parameters.size() - 1;
 			}
 
 			for (int i = 0; i < testTypesSize; i++) {
 				IValueReference argument = arguments[i];
 				Parameter parameter = parameters.get(i);
 				if (parameter.getType() instanceof RecordType
 						&& argument != null) {
 					Set<String> argumentsChildren = argument
 							.getDirectChildren();
 					for (Member member : ((RecordType) parameter.getType())
 							.getMembers()) {
 						if (argumentsChildren.contains(member.getName())) {
 							if (member.getType() != null) {
 								IValueReference child = argument
 										.getChild(member.getName());
 								if (!testArgumentType(member.getType(), child))
 									return false;
 							}
 						} else if (member
 								.getAttribute(IReferenceAttributes.OPTIONAL) == null) {
 							return false;
 						}
 					}
 
 				} else {
 					JSType paramType = parameter.getType();
 					if (!testArgumentType(paramType, argument))
 						return false;
 				}
 			}
 			// test var args
 			if (parameters.size() < arguments.length) {
 				int varargsParameter = parameters.size() - 1;
 				JSType paramType = parameters.get(varargsParameter).getType();
 
 				for (int i = varargsParameter; i < arguments.length; i++) {
 					IValueReference argument = arguments[i];
 					if (!testArgumentType(paramType, argument))
 						return false;
 				}
 
 			}
 			return true;
 		}
 
 		/**
 		 * @param paramType
 		 * @param argument
 		 * @return
 		 */
 		private boolean testArgumentType(JSType paramType,
 				IValueReference argument) {
 			if (argument != null && paramType != null) {
 				if (paramType.getKind() == TypeKind.RECORD) {
 					return testObjectPropertyType(argument, paramType) == null;
 				}
 
 				JSType argumentType = argument.getDeclaredType();
 				if (argumentType == null && !argument.getTypes().isEmpty()) {
 					argumentType = argument.getTypes().getFirst();
 				}
 				if (argumentType != null) {
 					return JSTypeSet.normalize(
 							context.resolveTypeRef(paramType))
 							.isAssignableFrom(
 									JSTypeSet.normalize(context
 											.resolveTypeRef(argumentType)));
 				}
 			}
 			return true;
 		}
 
 		/**
 		 * @param element
 		 * @param type
 		 */
 		private static String testObjectPropertyType(IValueReference reference,
 				JSType type) {
 			if (type.getKind() == TypeKind.RECORD) {
 				Type realType = TypeUtil.extractType(type);
 				if (realType != null) {
 					EList<Member> members = realType.getMembers();
 					for (Member member : members) {
 						IValueReference child = reference.getChild(member
 								.getName());
 						JSType referenceType = JavaScriptValidations
 								.typeOf(child);
 						if (!child.exists()
 								|| !(referenceType != null
 										&& member.getType() != null && JSTypeSet
 										.normalize(member.getType())
 										.isAssignableFrom(
 												JSTypeSet
 														.normalize(referenceType)))) {
 							Set<String> children = reference
 									.getDirectChildren();
 							if (children.size() == 0)
 								return "{}";
 							StringBuilder typeString = new StringBuilder();
 							typeString.append('{');
 							for (String childName : children) {
 								typeString.append(childName);
 								typeString.append(':');
 								JSType childType = JavaScriptValidations
 										.typeOf(reference.getChild(childName));
 								String typeName = TypeUtil.getName(childType);
 								typeString.append(typeName == null ? "Object"
 										: typeName);
 								typeString.append(',');
 							}
 							typeString.setLength(typeString.length() - 1);
 							typeString.append('}');
 
 							return typeString.toString();
 						}
 					}
 				}
 			}
 			return null;
 		}
 
 		/**
 		 * @param parameters
 		 * @return
 		 */
 		private String describeParamTypes(EList<Parameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (Parameter parameter : parameters) {
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (parameter.getKind() == ParameterKind.OPTIONAL)
 					sb.append('[');
 				if (parameter.getType() != null) {
 					sb.append(parameter.getType().getName());
 				} else {
 					sb.append('?');
 				}
 				if (parameter.getKind() == ParameterKind.OPTIONAL)
 					sb.append(']');
 				if (parameter.getKind() == ParameterKind.VARARGS) {
 					sb.append("...");
 				}
 			}
 			return sb.toString();
 		}
 
 		private String describeParamTypes(List<IParameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (IParameter parameter : parameters) {
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (parameter.getType() instanceof RecordType) {
 					sb.append('{');
 					for (Member member : ((RecordType) parameter.getType())
 							.getMembers()) {
 						if (sb.length() > 1)
 							sb.append(", ");
 						if (member.getAttribute(IReferenceAttributes.OPTIONAL) != null)
 							sb.append('[');
 						sb.append(member.getName());
 						if (member.getType() != null) {
 							sb.append(':');
 							sb.append(member.getType().getName());
 						}
 						if (member.getAttribute(IReferenceAttributes.OPTIONAL) != null)
 							sb.append(']');
 					}
 					sb.append('}');
 				} else if (parameter.getType() != null) {
 					if (parameter.isOptional())
 						sb.append("[");
 					if (parameter.isVarargs())
 						sb.append("...");
 					sb.append(parameter.getType().getName());
 					if (parameter.isOptional())
 						sb.append("]");
 				} else {
 					sb.append('?');
 				}
 			}
 			return sb.toString();
 		}
 
 		/**
 		 * @param arguments
 		 * @return
 		 */
 		private String describeArgTypes(IValueReference[] arguments) {
 			return describeArgTypes(arguments,
 					Collections.<IParameter> emptyList());
 		}
 
 		/**
 		 * @param arguments
 		 * @param parameters
 		 * @return
 		 */
 		private String describeArgTypes(IValueReference[] arguments,
 				List<IParameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < arguments.length; i++) {
 				IValueReference argument = arguments[i];
 				IParameter parameter = parameters.size() > i ? parameters
 						.get(i) : null;
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (argument == null) {
 					sb.append("null");
 				} else if (parameter != null
 						&& parameter.getType() instanceof RecordType) {
 					Set<String> directChildren = argument.getDirectChildren();
 					sb.append('{');
 					for (String childName : directChildren) {
 						if (sb.length() > 1)
 							sb.append(", ");
 						sb.append(childName);
 						JSType type = JavaScriptValidations.typeOf(argument
 								.getChild(childName));
 						if (type != null) {
 							sb.append(':');
 							sb.append(type.getName());
 						}
 					}
 					sb.append('}');
 				} else if (parameter != null && parameter.getType() != null
 						&& parameter.getType().getKind() == TypeKind.RECORD) {
 					// XXX doesn't enter into this block now
 					sb.append(ValidationVisitor.testObjectPropertyType(
 							argument, parameter.getType()));
 				} else if (argument.getDeclaredType() != null) {
 					sb.append(argument.getDeclaredType().getName());
 				} else {
 					final JSTypeSet types = argument.getTypes();
 					if (types.size() == 1) {
 						sb.append(types.getFirst().getName());
 					} else {
 						sb.append('?');
 					}
 				}
 			}
 			return sb.toString();
 		}
 
 		private String describeArgTypes(IValueReference[] arguments,
 				EList<Parameter> parameters) {
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < arguments.length; i++) {
 				IValueReference argument = arguments[i];
 				Parameter parameter = parameters.size() > i ? parameters.get(i)
 						: null;
 				if (sb.length() != 0) {
 					sb.append(',');
 				}
 				if (argument == null) {
 					sb.append("null");
 				} else if (parameter != null
 						&& parameter.getType() instanceof RecordType) {
 					Set<String> directChildren = argument.getDirectChildren();
 					sb.append('{');
 					for (String childName : directChildren) {
 						if (sb.length() > 1)
 							sb.append(", ");
 						sb.append(childName);
 						JSType type = JavaScriptValidations.typeOf(argument
 								.getChild(childName));
 						if (type != null) {
 							sb.append(':');
 							sb.append(type.getName());
 						}
 					}
 					sb.append('}');
 				} else if (parameter != null && parameter.getType() != null
 						&& parameter.getType().getKind() == TypeKind.RECORD) {
 					// XXX doesn't enter into this block now
 					sb.append(ValidationVisitor.testObjectPropertyType(
 							argument, parameter.getType()));
 				} else if (argument.getDeclaredType() != null) {
 					sb.append(argument.getDeclaredType().getName());
 				} else {
 					final JSTypeSet types = argument.getTypes();
 					if (types.size() == 1) {
 						sb.append(types.getFirst().getName());
 					} else {
 						sb.append('?');
 					}
 				}
 			}
 			return sb.toString();
 		}
 
 		private <E extends Member> E extractElement(IValueReference reference,
 				Class<E> elementType, Boolean staticModifierValue) {
 			final List<E> elements = JavaScriptValidations.extractElements(
 					reference, elementType);
 			if (staticModifierValue != null && elements != null
 					&& elements.size() > 1) {
 				for (E e : elements) {
 					if (e.isStatic() == staticModifierValue.booleanValue())
 						return e;
 				}
 			}
 
 			return elements != null ? elements.get(0) : null;
 		}
 
 		/**
 		 * Validates the parameter count, returns <code>true</code> if correct.
 		 * 
 		 * @param method
 		 * @param callArgs
 		 * 
 		 * @return
 		 */
 		private boolean validateParameterCount(Method method,
 				List<ASTNode> callArgs) {
 			final EList<Parameter> params = method.getParameters();
 			if (params.size() == callArgs.size()) {
 				return true;
 			}
 			if (params.size() < callArgs.size()
 					&& !params.isEmpty()
 					&& params.get(params.size() - 1).getKind() == ParameterKind.VARARGS) {
 				return true;
 			}
 			if (params.size() > callArgs.size()
 					&& (params.get(callArgs.size()).getKind() == ParameterKind.OPTIONAL || params
 							.get(callArgs.size()).getKind() == ParameterKind.VARARGS)) {
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public IValueReference visitPropertyExpression(PropertyExpression node) {
 			boolean started = startExpressionValidator();
 			try {
 				final IValueReference result = super
 						.visitPropertyExpression(node);
 				if (result != null) {
 					if (currentMode() != VisitorMode.CALL) {
 						pushExpressionValidator(new PropertyExpressionHolder(
 								node, result, this, result.exists()));
 					}
 					return result;
 				} else {
 					return null;
 				}
 			} finally {
 				if (started)
 					stopExpressionValidator();
 			}
 		}
 
 		@Override
 		protected IValueReference visitAssign(IValueReference left,
 				IValueReference right, BinaryOperation node) {
 			if (left != null) {
 				if (left.getAttribute(IReferenceAttributes.CONSTANT) != null) {
 					reporter.reportProblem(
 							JavaScriptProblems.REASSIGNMENT_OF_CONSTANT,
 							ValidationMessages.ReassignmentOfConstant,
 							node.sourceStart(), node.sourceEnd());
 				} else
 					validate(node.getLeftExpression(), left);
 			}
 			return super.visitAssign(left, right, node);
 		}
 
 		protected boolean validate(Expression expr, IValueReference reference) {
 			final IValueReference parent = reference.getParent();
 			if (parent == null) {
 				// top level
 				if (expr instanceof Identifier && !reference.exists()) {
 					if (expr.getParent() instanceof BinaryOperation
 							&& ((BinaryOperation) expr.getParent())
 									.getOperation() == JSParser.INSTANCEOF
 							&& ((BinaryOperation) expr.getParent())
 									.getRightExpression() == expr) {
 						reporter.reportProblem(JavaScriptProblems.UNKNOWN_TYPE,
 								NLS.bind(ValidationMessages.UnknownType,
 										reference.getName()), expr
 										.sourceStart(), expr.sourceEnd());
 
 					} else {
 						reporter.reportProblem(
 								JavaScriptProblems.UNDECLARED_VARIABLE,
 								NLS.bind(ValidationMessages.UndeclaredVariable,
 										reference.getName()), expr
 										.sourceStart(), expr.sourceEnd());
 					}
 					return false;
 				}
 			} else if (expr instanceof PropertyExpression
 					&& validate(((PropertyExpression) expr).getObject(), parent)) {
 				final JSType type = JavaScriptValidations.typeOf(parent);
 				if (type != null && type.getKind() == TypeKind.JAVA
 						&& !reference.exists()) {
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_JAVA_PROPERTY,
 							NLS.bind(ValidationMessages.UndefinedProperty,
 									reference.getName(), type.getName()), expr
 									.sourceStart(), expr.sourceEnd());
 					return false;
 				}
 			} else if (expr instanceof GetArrayItemExpression
 					&& !validate(((GetArrayItemExpression) expr).getArray(),
 							parent)) {
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		public IValueReference visitIdentifier(Identifier node) {
 			final IValueReference result = super.visitIdentifier(node);
 			final Property property = extractElement(result, Property.class,
 					null);
 			if (property != null && property.isDeprecated()) {
 				reportDeprecatedProperty(property, null, node);
 			} else {
 				if (!result.exists()
 						&& !(node.getParent() instanceof CallExpression && ((CallExpression) node
 								.getParent()).getExpression() == node)) {
 					pushExpressionValidator(new NotExistingIdentiferValidator(
 							node, result, this));
 				}
 			}
 			return result;
 		}
 
 		private static IValueCollection getParentScope(
 				final IValueCollection collection) {
 			IValueCollection c = collection;
 			while (c != null && !c.isScope()) {
 				c = c.getParent();
 			}
 			if (c != null) {
 				c = c.getParent();
 				if (c != null) {
 					return c;
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected IValueReference createVariable(IValueCollection context,
 				VariableDeclaration declaration) {
 			validateHidesByVariable(context, declaration);
 			return super.createVariable(context, declaration);
 		}
 
 		private void validateHidesByVariable(IValueCollection context,
 				VariableDeclaration declaration) {
 			final IValueReference child;
 			final Identifier identifier = declaration.getIdentifier();
 			final IValueCollection parentScope = getParentScope(context);
 			if (parentScope == null) {
 				child = context.getChild(identifier.getName());
 				if (getSource().equals(child.getLocation().getSource())) {
 					return;
 				}
 			} else {
 				child = parentScope.getChild(identifier.getName());
 			}
 			if (child.exists()) {
 				if (child.getKind() == ReferenceKind.ARGUMENT) {
 					reporter.reportProblem(
 							JavaScriptProblems.VAR_HIDES_PARAMETER, NLS.bind(
 									ValidationMessages.VariableHidesParameter,
 									declaration.getVariableName()), identifier
 									.sourceStart(), identifier.sourceEnd());
 				} else if (child.getKind() == ReferenceKind.FUNCTION) {
 					reporter.reportProblem(
 							JavaScriptProblems.VAR_HIDES_FUNCTION, NLS.bind(
 									ValidationMessages.VariableHidesFunction,
 									declaration.getVariableName()), identifier
 									.sourceStart(), identifier.sourceEnd());
 				} else if (child.getKind() == ReferenceKind.PROPERTY) {
 					Property property = (Property) child
 							.getAttribute(IReferenceAttributes.ELEMENT);
 					if (property != null && property.getDeclaringType() != null) {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_PROPERTY,
 								NLS.bind(
 										ValidationMessages.VariableHidesPropertyOfType,
 										declaration.getVariableName(), property
 												.getDeclaringType().getName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 					} else {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_PROPERTY,
 								NLS.bind(
 										ValidationMessages.VariableHidesProperty,
 										declaration.getVariableName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 
 					}
 				} else if (child.getKind() == ReferenceKind.METHOD) {
 					Method method = (Method) child
 							.getAttribute(IReferenceAttributes.ELEMENT);
 					if (method != null && method.getDeclaringType() != null) {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_METHOD,
 								NLS.bind(
 										ValidationMessages.VariableHidesMethodOfType,
 										declaration.getVariableName(), method
 												.getDeclaringType().getName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 					} else {
 						reporter.reportProblem(
 								JavaScriptProblems.VAR_HIDES_METHOD, NLS.bind(
 										ValidationMessages.VariableHidesMethod,
 										declaration.getVariableName()),
 								identifier.sourceStart(), identifier
 										.sourceEnd());
 					}
 				} else {
 					reporter.reportProblem(
 							JavaScriptProblems.DUPLICATE_VAR_DECLARATION,
 							NLS.bind(ValidationMessages.VariableHidesVariable,
 									declaration.getVariableName()), identifier
 									.sourceStart(), identifier.sourceEnd());
 				}
 
 			}
 		}
 
 		protected void validateProperty(PropertyExpression propertyExpression,
 				IValueReference result, boolean exists) {
 			final Expression propName = propertyExpression.getProperty();
 			final Member member = extractElement(result, Member.class,
 					JavaScriptValidations.isStatic(result.getParent()));
 			if (member != null) {
 				if (member.isDeprecated()) {
 					final Property parentProperty = extractElement(
 							result.getParent(), Property.class, null);
 					if (parentProperty != null
 							&& parentProperty.getDeclaringType() == null) {
 						if (member instanceof Property)
 							reportDeprecatedProperty((Property) member,
 									parentProperty, propName);
 						else if (member instanceof Method)
 							reportDeprecatedMethod(propName, result,
 									(Method) member);
 					} else {
 						if (member instanceof Property)
 							reportDeprecatedProperty((Property) member,
 									member.getDeclaringType(), propName);
 						else if (member instanceof Method)
 							reportDeprecatedMethod(propName, result,
 									(Method) member);
 					}
 				} else if (!member.isVisible()) {
 					final Property parentProperty = extractElement(
 							result.getParent(), Property.class, null);
 					if (parentProperty != null
 							&& parentProperty.getDeclaringType() == null) {
 						if (member instanceof Property)
 							reportHiddenProperty((Property) member,
 									parentProperty, propName);
 						// else if (member instanceof Method)
 						// reportDeprecatedMethod(propName, result,
 						// (Method) member);
 					} else if (member instanceof Property) {
 						reportHiddenProperty((Property) member,
 								member.getDeclaringType(), propName);
 					}
 				} else if (JavaScriptValidations.isStatic(result.getParent())
 						&& !member.isStatic()) {
 					JSType type = context.resolveTypeRef(JavaScriptValidations
 							.typeOf(result.getParent()));
 					reporter.reportProblem(
 							JavaScriptProblems.INSTANCE_PROPERTY,
 							NLS.bind(
 									ValidationMessages.StaticReferenceToNoneStaticProperty,
 									result.getName(), TypeUtil.getName(type)),
 							propName.sourceStart(), propName.sourceEnd());
 				} else if (!JavaScriptValidations.isStatic(result.getParent())
 						&& member.isStatic()) {
 					JSType type = JavaScriptValidations.typeOf(result
 							.getParent());
 					reporter.reportProblem(
 							JavaScriptProblems.STATIC_PROPERTY,
 							NLS.bind(
 									ValidationMessages.ReferenceToStaticProperty,
 									result.getName(), type.getName()), propName
 									.sourceStart(), propName.sourceEnd());
 				}
 			} else if ((!exists && !result.exists())
 					&& !isArrayLookup(propertyExpression)) {
 				final JSType type = context
 						.resolveTypeRef(JavaScriptValidations.typeOf(result
 								.getParent()));
 				if (type != null && type.getKind() == TypeKind.JAVA) {
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_JAVA_PROPERTY, NLS
 									.bind(ValidationMessages.UndefinedProperty,
 											result.getName(), type.getName()),
 							propName.sourceStart(), propName.sourceEnd());
 				} else if (type != null
 						&& shouldBeDefined(propertyExpression)
 						&& (type.getKind() == TypeKind.JAVASCRIPT
 								|| type.getKind() == TypeKind.PREDEFINED || type
 								.getKind() == TypeKind.EXTERNAL_JS)) {
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_PROPERTY,
 							NLS.bind(
 									ValidationMessages.UndefinedPropertyInScriptType,
 									result.getName(), type.getName()), propName
 									.sourceStart(), propName.sourceEnd());
 				} else if (shouldBeDefined(propertyExpression)) {
 					final String parentPath = PropertyExpressionUtils
 							.getPath(propertyExpression.getObject());
 					reporter.reportProblem(
 							JavaScriptProblems.UNDEFINED_PROPERTY,
 							NLS.bind(
 									ValidationMessages.UndefinedPropertyInScript,
 									result.getName(),
 									parentPath != null ? parentPath
 											: "javascript"), propName
 									.sourceStart(), propName.sourceEnd());
 				}
 			} else {
 				IVariable variable = (IVariable) result
 						.getAttribute(IReferenceAttributes.VARIABLE);
 				if (variable != null) {
 					if (variable.isDeprecated()) {
 						reporter.reportProblem(
 								JavaScriptProblems.DEPRECATED_VARIABLE,
 								NLS.bind(ValidationMessages.DeprecatedVariable,
 										variable.getName()), propName
 										.sourceStart(), propName.sourceEnd());
 					}
 					if (variable.isPrivate() && result.getParent() != null) {
 						reporter.reportProblem(
 								JavaScriptProblems.PRIVATE_VARIABLE, NLS.bind(
 										ValidationMessages.PrivateVariable,
 										variable.getName()), propName
 										.sourceStart(), propName.sourceEnd());
 					}
 					return;
 				}
 			}
 		}
 
 		private boolean shouldBeDefined(PropertyExpression propertyExpression) {
 			if (propertyExpression.getParent() instanceof BinaryOperation) {
 				BinaryOperation bo = (BinaryOperation) propertyExpression
 						.getParent();
 				if (bo.getRightExpression() == propertyExpression) {
 					return bo.getOperation() != JSParser.LAND
 							&& bo.getOperation() != JSParser.LOR;
 				}
 			}
 			if (propertyExpression.getParent() instanceof VariableDeclaration) {
 				return true;
 			}
 			if (propertyExpression.getParent() instanceof CallExpression) {
 				return true;
 			}
 			if (propertyExpression.getParent() instanceof PropertyExpression) {
 				return true;
 			}
 			return false;
 		}
 
 		private void reportDeprecatedProperty(Property property, Element owner,
 				ASTNode node) {
 			final String msg;
 			if (owner instanceof Type) {
 				msg = NLS.bind(ValidationMessages.DeprecatedProperty,
 						property.getName(), owner.getName());
 			} else if (owner instanceof Property) {
 				msg = NLS.bind(ValidationMessages.DeprecatedPropertyOfInstance,
 						property.getName(), owner.getName());
 			} else {
 				msg = NLS.bind(ValidationMessages.DeprecatedPropertyNoType,
 						property.getName());
 			}
 			reporter.reportProblem(JavaScriptProblems.DEPRECATED_PROPERTY, msg,
 					node.sourceStart(), node.sourceEnd());
 		}
 
 		private void reportHiddenProperty(Property property, Element owner,
 				ASTNode node) {
 			final String msg;
 			if (owner instanceof Type) {
 				msg = NLS.bind(ValidationMessages.HiddenProperty,
 						property.getName(), owner.getName());
 			} else if (owner instanceof Property) {
 				msg = NLS.bind(ValidationMessages.HiddenPropertyOfInstance,
 						property.getName(), owner.getName());
 			} else {
 				msg = NLS.bind(ValidationMessages.HiddenPropertyNoType,
 						property.getName());
 			}
 			reporter.reportProblem(JavaScriptProblems.HIDDEN_PROPERTY, msg,
 					node.sourceStart(), node.sourceEnd());
 		}
 
 		protected void checkExpressionType(IValueCollection collection,
 				ASTNode node, IValueReference reference) {
 			JSTypeSet types = reference.getTypes();
 			if (types.size() > 0) {
 				checkType(node, types.getFirst(), collection);
 			} else if (reference.getDeclaredType() == null) {
 				final String lazyName = ValueReferenceUtil
 						.getLazyName(reference);
 				if (lazyName != null) {
 					reportUnknownType(node, lazyName);
 				}
 			} else {
 				checkType(node, reference.getDeclaredType(), collection);
 			}
 		}
 
 		@Override
 		public void setType(ASTNode node, IValueReference value, JSType type,
 				boolean lazy) {
 			super.setType(node, value, type, lazy);
 			if (type != null) {
 				if (lazy) {
 					pushExpressionValidator(new TypeValidator(this, value, node));
 				} else {
 					checkType(node, type, null);
 				}
 			}
 		}
 
 		/**
 		 * @param node
 		 * @param type
 		 */
 		protected void checkType(ASTNode node, JSType type,
 				IValueCollection collection) {
 			if (type != null) {
 				type = context.resolveTypeRef(type);
 				Assert.isTrue(type.getKind() != TypeKind.UNRESOLVED);
 				if (type.getKind() == TypeKind.UNKNOWN) {
 					if (!(type instanceof TypeRef && collection != null && collection
 							.getChild(((TypeRef) type).getName()).exists())) {
 						reportUnknownType(node, TypeUtil.getName(type));
 					}
 				} else if (type instanceof ArrayType) {
 					checkType(node, ((ArrayType) type).getItemType(),
 							collection);
 				} else if (type instanceof MapType) {
 					checkType(node, ((MapType) type).getValueType(), collection);
 					checkType(node, ((MapType) type).getKeyType(), collection);
 				} else if (type instanceof UnionType) {
 					for (JSType part : ((UnionType) type).getTargets()) {
 						checkType(node, part, collection);
 					}
 				} else {
 					final Type t = TypeUtil.extractType(type);
 					if (t != null && t.isDeprecated()) {
 						reporter.reportProblem(
 								JavaScriptProblems.DEPRECATED_TYPE, NLS.bind(
 										ValidationMessages.DeprecatedType,
 										TypeUtil.getName(type)), node
 										.sourceStart(), node.sourceEnd());
 					}
 				}
 			} else {
 				reportUnknownType(node, TypeUtil.getName(type));
 			}
 		}
 
 		public void reportUnknownType(ASTNode node, String name) {
 			reportUnknownType(JavaScriptProblems.UNKNOWN_TYPE, node, name);
 		}
 
 		public void reportUnknownType(IProblemIdentifier identifier,
 				ASTNode node, String name) {
 			reporter.reportProblem(identifier,
 					NLS.bind(ValidationMessages.UnknownType, name),
 					node.sourceStart(), node.sourceEnd());
 		}
 	}
 
 }
