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
 package org.eclipse.dltk.internal.javascript.ti;
 
 import static org.eclipse.dltk.javascript.typeinfo.ITypeNames.NUMBER;
 import static org.eclipse.dltk.javascript.typeinfo.ITypeNames.OBJECT;
 import static org.eclipse.dltk.javascript.typeinfo.ITypeNames.STRING;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.compiler.problem.IProblemCategory;
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.internal.javascript.validation.JavaScriptValidations;
 import org.eclipse.dltk.internal.javascript.validation.ValidationMessages;
 import org.eclipse.dltk.javascript.ast.ArrayInitializer;
 import org.eclipse.dltk.javascript.ast.AsteriskExpression;
 import org.eclipse.dltk.javascript.ast.BinaryOperation;
 import org.eclipse.dltk.javascript.ast.BooleanLiteral;
 import org.eclipse.dltk.javascript.ast.BreakStatement;
 import org.eclipse.dltk.javascript.ast.CallExpression;
 import org.eclipse.dltk.javascript.ast.CaseClause;
 import org.eclipse.dltk.javascript.ast.CatchClause;
 import org.eclipse.dltk.javascript.ast.CommaExpression;
 import org.eclipse.dltk.javascript.ast.ConditionalOperator;
 import org.eclipse.dltk.javascript.ast.ConstStatement;
 import org.eclipse.dltk.javascript.ast.ContinueStatement;
 import org.eclipse.dltk.javascript.ast.DecimalLiteral;
 import org.eclipse.dltk.javascript.ast.DefaultXmlNamespaceStatement;
 import org.eclipse.dltk.javascript.ast.DoWhileStatement;
 import org.eclipse.dltk.javascript.ast.EmptyExpression;
 import org.eclipse.dltk.javascript.ast.EmptyStatement;
 import org.eclipse.dltk.javascript.ast.Expression;
 import org.eclipse.dltk.javascript.ast.ForEachInStatement;
 import org.eclipse.dltk.javascript.ast.ForInStatement;
 import org.eclipse.dltk.javascript.ast.ForStatement;
 import org.eclipse.dltk.javascript.ast.FunctionStatement;
 import org.eclipse.dltk.javascript.ast.GetAllChildrenExpression;
 import org.eclipse.dltk.javascript.ast.GetArrayItemExpression;
 import org.eclipse.dltk.javascript.ast.GetLocalNameExpression;
 import org.eclipse.dltk.javascript.ast.Identifier;
 import org.eclipse.dltk.javascript.ast.IfStatement;
 import org.eclipse.dltk.javascript.ast.JSDeclaration;
 import org.eclipse.dltk.javascript.ast.JSScope;
 import org.eclipse.dltk.javascript.ast.LabelledStatement;
 import org.eclipse.dltk.javascript.ast.NewExpression;
 import org.eclipse.dltk.javascript.ast.NullExpression;
 import org.eclipse.dltk.javascript.ast.ObjectInitializer;
 import org.eclipse.dltk.javascript.ast.ObjectInitializerPart;
 import org.eclipse.dltk.javascript.ast.ParenthesizedExpression;
 import org.eclipse.dltk.javascript.ast.PropertyExpression;
 import org.eclipse.dltk.javascript.ast.PropertyInitializer;
 import org.eclipse.dltk.javascript.ast.RegExpLiteral;
 import org.eclipse.dltk.javascript.ast.ReturnStatement;
 import org.eclipse.dltk.javascript.ast.Script;
 import org.eclipse.dltk.javascript.ast.Statement;
 import org.eclipse.dltk.javascript.ast.StatementBlock;
 import org.eclipse.dltk.javascript.ast.StringLiteral;
 import org.eclipse.dltk.javascript.ast.SwitchComponent;
 import org.eclipse.dltk.javascript.ast.SwitchStatement;
 import org.eclipse.dltk.javascript.ast.ThisExpression;
 import org.eclipse.dltk.javascript.ast.ThrowStatement;
 import org.eclipse.dltk.javascript.ast.TryStatement;
 import org.eclipse.dltk.javascript.ast.UnaryOperation;
 import org.eclipse.dltk.javascript.ast.VariableDeclaration;
 import org.eclipse.dltk.javascript.ast.VariableStatement;
 import org.eclipse.dltk.javascript.ast.VoidExpression;
 import org.eclipse.dltk.javascript.ast.WhileStatement;
 import org.eclipse.dltk.javascript.ast.WithStatement;
 import org.eclipse.dltk.javascript.ast.XmlAttributeIdentifier;
 import org.eclipse.dltk.javascript.ast.XmlExpressionFragment;
 import org.eclipse.dltk.javascript.ast.XmlFragment;
 import org.eclipse.dltk.javascript.ast.XmlLiteral;
 import org.eclipse.dltk.javascript.ast.XmlTextFragment;
 import org.eclipse.dltk.javascript.ast.YieldOperator;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.parser.JSParser;
 import org.eclipse.dltk.javascript.parser.PropertyExpressionUtils;
 import org.eclipse.dltk.javascript.typeinference.IAssignProtection;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinference.ReferenceKind;
 import org.eclipse.dltk.javascript.typeinference.ReferenceLocation;
 import org.eclipse.dltk.javascript.typeinfo.IMemberEvaluator;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IParameter;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilder.IVariable;
 import org.eclipse.dltk.javascript.typeinfo.IModelBuilderExtension;
 import org.eclipse.dltk.javascript.typeinfo.IRArrayType;
 import org.eclipse.dltk.javascript.typeinfo.IRClassType;
 import org.eclipse.dltk.javascript.typeinfo.IRMapType;
 import org.eclipse.dltk.javascript.typeinfo.IRSimpleType;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.IRVariable;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 import org.eclipse.dltk.javascript.typeinfo.JSTypeSet;
 import org.eclipse.dltk.javascript.typeinfo.RModelBuilder;
 import org.eclipse.dltk.javascript.typeinfo.ReferenceSource;
 import org.eclipse.dltk.javascript.typeinfo.TypeInfoManager;
 import org.eclipse.dltk.javascript.typeinfo.TypeMode;
 import org.eclipse.dltk.javascript.typeinfo.TypeUtil;
 import org.eclipse.dltk.javascript.typeinfo.model.ArrayType;
 import org.eclipse.dltk.javascript.typeinfo.model.FunctionType;
 import org.eclipse.dltk.javascript.typeinfo.model.GenericMethod;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.MapType;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterizedType;
 import org.eclipse.dltk.javascript.typeinfo.model.SimpleType;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelFactory;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeKind;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeVariable;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeVariableReference;
 import org.eclipse.dltk.javascript.typeinfo.model.UnionType;
 import org.eclipse.dltk.javascript.typeinfo.model.util.TypeInfoModelSwitch;
 import org.eclipse.emf.ecore.EObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class TypeInferencerVisitor extends TypeInferencerVisitorBase {
 
 	public TypeInferencerVisitor(ITypeInferenceContext context) {
 		super(context);
 	}
 
 	private final Stack<Branching> branchings = new Stack<Branching>();
 
 	private class Branching {
 		public void end() {
 			branchings.remove(this);
 		}
 	}
 
 	protected Branching branching() {
 		final Branching branching = new Branching();
 		branchings.add(branching);
 		return branching;
 	}
 
 	public ReferenceSource getSource() {
 		final ReferenceSource source = context.getSource();
 		return source != null ? source : ReferenceSource.UNKNOWN;
 	}
 
 	protected void assign(IValueReference dest, IValueReference src) {
 		IRType destType = JavaScriptValidations.typeOf(dest);
 		if (destType != null && isXML(destType)) {
 			IRType srcType = JavaScriptValidations.typeOf(src);
 			if (srcType != null && !isXML(srcType))
 				return;
 		}
 		if (branchings.isEmpty()) {
 			dest.setValue(src);
 		} else {
 			dest.addValue(src, false);
 		}
 	}
 
 	private static boolean isXML(IRType srcType) {
 		return (srcType instanceof IRSimpleType)
 				&& (ITypeNames.XML.equals(srcType.getName()) || ITypeNames.XMLLIST
 						.equals(srcType.getName()));
 	}
 
 	private static final int K_NUMBER = 1;
 	private static final int K_STRING = 2;
 	private static final int K_OTHER = 4;
 
 	@Override
 	public IValueReference visitArrayInitializer(ArrayInitializer node) {
 		int kind = 0;
 		for (ASTNode astNode : node.getItems()) {
 			if (astNode instanceof StringLiteral) {
 				kind |= K_STRING;
 			} else if (astNode instanceof DecimalLiteral) {
 				kind |= K_NUMBER;
 			} else if (astNode instanceof NullExpression) {
 				// ignore
 			} else {
 				final IValueReference child = visit(astNode);
 				if (child != null && child.exists()) {
 					if (isNumber(child))
 						kind |= K_NUMBER;
 					else if (isString(child))
 						kind |= K_STRING;
 					else
 						kind |= K_OTHER;
 				} else
 					kind |= K_OTHER;
 			}
 		}
 		if (kind == K_STRING) {
 			return context.getFactory().create(peekContext(),
 					JSTypeSet.arrayOf(JSTypeSet.ref(STRING)));
 		} else if (kind == K_NUMBER) {
 			return context.getFactory().create(peekContext(),
 					JSTypeSet.arrayOf(JSTypeSet.ref(NUMBER)));
 		} else {
 			return context.getFactory().createArray(peekContext());
 		}
 	}
 
 	@Override
 	public IValueReference visitAsteriskExpression(AsteriskExpression node) {
 		return context.getFactory().createXMLList(peekContext());
 	}
 
 	@Override
 	public IValueReference visitBinaryOperation(BinaryOperation node) {
 		final IValueReference left = visit(node.getLeftExpression());
 		final int op = node.getOperation();
 		if (JSParser.ASSIGN == op) {
 			if (left != null) {
 				for (IModelBuilder modelBuilder : context.getModelBuilders()) {
 					if (modelBuilder instanceof IModelBuilderExtension) {
 						((IModelBuilderExtension) modelBuilder)
 								.processAssignment(node.getLeftExpression(),
 										left);
 					}
 				}
 			}
 			if (left != null && left.exists()) {
 				left.setAttribute(IReferenceAttributes.RESOLVING, Boolean.TRUE);
 				final IValueReference r;
 				try {
 					r = visit(node.getRightExpression());
 				} finally {
 					left.setAttribute(IReferenceAttributes.RESOLVING, null);
 				}
 				return visitAssign(left, r, node);
 			} else {
 				return visitAssign(left, visit(node.getRightExpression()), node);
 			}
 		}
 		final IValueReference right = visit(node.getRightExpression());
 		if (left == null && right instanceof ConstantValue) {
 			return right;
 		} else if (op == JSParser.LAND) {
 			return coalesce(right, left);
 		} else if (op == JSParser.GT || op == JSParser.GTE || op == JSParser.LT
 				|| op == JSParser.LTE || op == JSParser.NSAME
 				|| op == JSParser.SAME || op == JSParser.NEQ
 				|| op == JSParser.EQ) {
 			return context.getFactory().createBoolean(peekContext());
 		} else if (isNumber(left) && isNumber(right)) {
 			return context.getFactory().createNumber(peekContext());
 		} else if (op == JSParser.ADD) {
 			if (isString(left) || isString(right)) {
 				return context.getFactory().createString(peekContext());
 			}
 			return left;
 		} else if (JSParser.INSTANCEOF == op) {
 			return context.getFactory().createBoolean(peekContext());
 		} else if (JSParser.LOR == op) {
 			final JSTypeSet typeSet = JSTypeSet.create();
 			if (left != null) {
 				typeSet.addAll(left.getDeclaredTypes());
 				typeSet.addAll(left.getTypes());
 			}
 			if (right != null) {
 				typeSet.addAll(right.getDeclaredTypes());
 				typeSet.addAll(right.getTypes());
 			}
 			return new ConstantValue(typeSet);
 		} else {
 			// TODO handle other operations
 			return null;
 		}
 	}
 
 	private static IValueReference coalesce(IValueReference v1,
 			IValueReference v2) {
 		return v1 != null ? v1 : v2;
 	}
 
 	private boolean isNumber(IValueReference ref) {
 		if (ref != null) {
 			final IRType numType = JSTypeSet.ref(NUMBER);
 			if (ref.getTypes().contains(numType))
 				return true;
 			if (numType.equals(ref.getDeclaredType()))
 				return true;
 		}
 		return false;
 	}
 
 	private boolean isString(IValueReference ref) {
 		if (ref != null) {
 			final IRType strType = JSTypeSet.ref(STRING);
 			if (ref.getTypes().contains(strType))
 				return true;
 			if (strType.equals(ref.getDeclaredType()))
 				return true;
 		}
 		return false;
 	}
 
 	protected IValueReference visitAssign(IValueReference left,
 			IValueReference right, BinaryOperation node) {
 		if (left != null) {
 			if (node.getLeftExpression() instanceof PropertyExpression) {
 				final PropertyExpression property = (PropertyExpression) node
 						.getLeftExpression();
 				if (property.getObject() instanceof ThisExpression
 						&& property.getProperty() instanceof Identifier
 						&& !left.exists()) {
 					if (isFunctionDeclaration(property))
 						left.setKind(ReferenceKind.FUNCTION);
 					else
 						left.setKind(ReferenceKind.FIELD);
 					left.setLocation(ReferenceLocation.create(getSource(),
 							property.sourceStart(), property.sourceEnd(),
 							property.getProperty().sourceStart(), property
 									.getProperty().sourceEnd()));
 				}
 			}
 			if (IValueReference.ARRAY_OP.equals(left.getName())
 					&& node.getLeftExpression() instanceof GetArrayItemExpression) {
 				GetArrayItemExpression arrayItemExpression = (GetArrayItemExpression) node
 						.getLeftExpression();
 				IValueReference namedChild = extractNamedChild(
 						left.getParent(), arrayItemExpression.getIndex());
 				if (namedChild != null) {
 					assign(namedChild, right);
 				} else {
 					assign(left, right);
 				}
 			} else {
 				if (!hasUnknowParentFunctionCall(left))
 					assign(left, right);
 			}
 		}
 		return right;
 	}
 
 	private boolean hasUnknowParentFunctionCall(IValueReference reference) {
 		IValueReference parent = reference.getParent();
 		while (parent != null) {
 			if (parent.getName().equals(IValueReference.FUNCTION_OP)
 					&& !parent.exists())
 				return true;
 			parent = parent.getParent();
 		}
 		return false;
 	}
 
 	@Override
 	public IValueReference visitBooleanLiteral(BooleanLiteral node) {
 		return context.getFactory().createBoolean(peekContext());
 	}
 
 	@Override
 	public IValueReference visitBreakStatement(BreakStatement node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitCallExpression(CallExpression node) {
 		final IValueReference reference = visit(node.getExpression());
 		final List<ASTNode> args = node.getArguments();
 		final IValueReference[] arguments = new IValueReference[args.size()];
 		for (int i = 0; i < args.size(); ++i) {
 			arguments[i] = visit(args.get(i));
 		}
 		if (reference != null) {
 			final List<Method> methods = JavaScriptValidations.extractElements(
 					reference, Method.class);
 			if (methods != null && methods.size() == 1) {
 				if (methods.get(0) instanceof GenericMethod) {
 					final GenericMethod method = (GenericMethod) methods.get(0);
 					final JSTypeSet type = evaluateGenericCall(method,
 							arguments);
 					if (type != null) {
 						return new ConstantValue(type);
 					}
 				} else {
 					return new ConstantValue(JSTypeSet.normalize(getContext(),
 							methods.get(0).getType()));
 				}
 			}
 			return reference.getChild(IValueReference.FUNCTION_OP);
 		} else {
 			return null;
 		}
 	}
 
 	protected JSTypeSet evaluateGenericCall(GenericMethod method,
 			IValueReference[] arguments) {
 		final JSTypeSet[] argTypes = new JSTypeSet[arguments.length];
 		for (int i = 0; i < arguments.length; ++i) {
 			argTypes[i] = arguments[i].getDeclaredTypes();
 			if (argTypes[i].isEmpty()) {
 				argTypes[i] = arguments[i].getTypes();
 			}
 		}
 		// TODO (alex) can be pre-evaluated in GenericParameter objects.
 		final boolean genericParams[] = new boolean[method.getParameters()
 				.size()];
 		for (int i = 0; i < method.getParameters().size(); ++i) {
 			genericParams[i] = isGenericType(method.getParameters().get(i)
 					.getType());
 		}
 		final Map<TypeVariable, JSTypeSet> captures = new HashMap<TypeVariable, JSTypeSet>();
 		for (TypeVariable variable : method.getTypeParameters()) {
 			captures.put(variable, JSTypeSet.create());
 		}
 		for (int i = 0; i < argTypes.length; ++i) {
 			final int index = i < method.getParameters().size() ? i : method
 					.getParameters().size() - 1;
 			final Parameter parameter = method.getParameters().get(index);
 			if (genericParams[index]) {
 				final Capture capture = capture(parameter.getType(),
 						argTypes[i]);
 				if (capture != null) {
 					final JSTypeSet captured = captures.get(capture.variable);
 					if (captured != null) {
 						captured.addAll(capture.types);
 					}
 				}
 			} else {
 				// TODO (alex) check parameter compatibility
 			}
 		}
 		if (method.getType() != null) {
 			return evaluateReturnType(method.getType(), captures);
 		} else {
 			return null;
 		}
 	}
 
 	private JSTypeSet evaluateReturnType(JSType type,
 			Map<TypeVariable, JSTypeSet> captures) {
 		if (type instanceof TypeVariableReference) {
 			final TypeVariable variable = ((TypeVariableReference) type)
 					.getVariable();
 			return normalizeCapture(captures.get(variable));
 		} else if (type instanceof ArrayType) {
 			final JSType itemType = ((ArrayType) type).getItemType();
 			return JSTypeSet.singleton(JSTypeSet.arrayOf(evaluateReturnType(
 					itemType, captures).toRType()));
 		} else if (type instanceof ParameterizedType) {
 			List<IRType> params = new ArrayList<IRType>();
 			final ParameterizedType parameterized = (ParameterizedType) type;
 			for (JSType param : parameterized.getActualTypeArguments()) {
 				params.add(evaluateReturnType(param, captures).toRType());
 			}
 			return JSTypeSet.singleton(JSTypeSet.ref(getContext().parameterize(
 					parameterized.getTarget(), params)));
 		} else if (type instanceof SimpleType) {
 			return JSTypeSet.create(JSTypeSet.normalize(getContext(), type));
 		} else {
 			return JSTypeSet.emptySet();
 		}
 	}
 
 	private JSTypeSet normalizeCapture(JSTypeSet types) {
 		return types;
 	}
 
 	private static class Capture {
 		final TypeVariable variable;
 		final JSTypeSet types;
 
 		public Capture(TypeVariable variable, JSTypeSet types) {
 			this.variable = variable;
 			this.types = types;
 		}
 	}
 
 	private Capture capture(JSType paramType, JSTypeSet argTypes) {
 		if (paramType instanceof TypeVariableReference) {
 			return new Capture(
 					((TypeVariableReference) paramType).getVariable(), argTypes);
 		} else {
 			// TODO alex other type expressions
 			return null;
 		}
 	}
 
 	private static final TypeInfoModelSwitch<Boolean> GENERIC_TYPE_EXPRESSION = new TypeInfoModelSwitch<Boolean>() {
 		@Override
 		public Boolean doSwitch(EObject theEObject) {
 			return theEObject != null ? super.doSwitch(theEObject) : null;
 		}
 
 		@Override
 		public Boolean caseJSType(JSType object) {
 			return Boolean.FALSE;
 		}
 
 		@Override
 		public Boolean caseTypeVariableReference(TypeVariableReference object) {
 			return Boolean.TRUE;
 		}
 
 		@Override
 		public Boolean caseArrayType(ArrayType object) {
 			return doSwitch(object.getItemType());
 		}
 
 		@Override
 		public Boolean caseMapType(MapType object) {
 			final Boolean result = doSwitch(object.getKeyType());
 			if (result == Boolean.TRUE) {
 				return result;
 			}
 			return doSwitch(object.getValueType());
 		}
 
 		@Override
 		public Boolean caseUnionType(UnionType object) {
 			for (JSType type : object.getTargets()) {
 				final Boolean result = doSwitch(type);
 				if (result == Boolean.TRUE) {
 					return result;
 				}
 			}
 			return Boolean.FALSE;
 		}
 
 		@Override
 		public Boolean caseFunctionType(FunctionType object) {
 			for (Parameter parameter : object.getParameters()) {
 				final Boolean result = doSwitch(parameter.getType());
 				if (result == Boolean.TRUE) {
 					return result;
 				}
 			}
 			return doSwitch(object.getReturnType());
 		}
 	};
 
 	private boolean isGenericType(JSType type) {
 		if (type != null) {
 			final Boolean result = GENERIC_TYPE_EXPRESSION.doSwitch(type);
 			return result != null && result.booleanValue();
 		}
 		return false;
 	}
 
 	@Override
 	public IValueReference visitCommaExpression(CommaExpression node) {
 		return visit(node.getItems());
 	}
 
 	@Override
 	public IValueReference visitConditionalOperator(ConditionalOperator node) {
 		visit(node.getCondition());
 		return merge(visit(node.getTrueValue()), visit(node.getFalseValue()));
 	}
 
 	protected static final IAssignProtection PROTECT_CONST = new IAssignProtection() {
 		public IProblemIdentifier problemId() {
 			return JavaScriptProblems.REASSIGNMENT_OF_CONSTANT;
 		}
 
 		public String problemMessage() {
 			return ValidationMessages.ReassignmentOfConstant;
 		}
 	};
 
 	@Override
 	public IValueReference visitConstDeclaration(ConstStatement node) {
 		final IValueCollection context = peekContext();
 		for (VariableDeclaration declaration : node.getVariables()) {
 			IValueReference constant = createVariable(context, declaration);
 			if (constant != null)
 				constant.setAttribute(IAssignProtection.ATTRIBUTE,
 						PROTECT_CONST);
 		}
 		return null;
 	}
 
 	protected IValueReference createVariable(IValueCollection context,
 			VariableDeclaration declaration) {
 		final Identifier identifier = declaration.getIdentifier();
 		final String varName = identifier.getName();
 		final IValueReference reference = context.createChild(varName);
 		final JSVariable variable = new JSVariable();
 		variable.setName(declaration.getVariableName());
 		if (declaration.getParent() instanceof VariableStatement) {
 			for (IModelBuilder extension : this.context.getModelBuilders()) {
 				extension.processVariable(declaration, variable, reporter,
 						getTypeChecker());
 			}
 		}
 		reference.setAttribute(IReferenceAttributes.VARIABLE, variable);
 
 		reference.setKind(inFunction() ? ReferenceKind.LOCAL
 				: ReferenceKind.GLOBAL);
 		reference.setLocation(ReferenceLocation.create(getSource(),
 				declaration.sourceStart(), declaration.sourceEnd(),
 				identifier.sourceStart(), identifier.sourceEnd()));
 		initializeVariable(reference, declaration, variable);
 
 		// declared type setting must be done after the initialize else the
 		// IMemberEvaluator.valueOf() call will be reverted for types that do
 		// return a collection
 		final IRVariable rvar = RModelBuilder.create(getContext(), variable);
 		reference.setAttribute(IReferenceAttributes.R_VARIABLE, rvar);
 		if (rvar.getType() != null) {
 			setIRType(reference, rvar.getType(), true);
 		}
 
 		return reference;
 	}
 
 	protected void initializeVariable(final IValueReference reference,
 			VariableDeclaration declaration, IVariable variable) {
 		if (declaration.getInitializer() != null) {
 			final IValueReference assignment;
 			reference
 					.setAttribute(IReferenceAttributes.RESOLVING, Boolean.TRUE);
 			try {
 				assignment = visit(declaration.getInitializer());
 			} finally {
 				reference.setAttribute(IReferenceAttributes.RESOLVING, null);
 			}
 			if (assignment != null) {
 				assign(reference, assignment);
 				if (assignment.getKind() == ReferenceKind.FUNCTION
 						&& reference.getAttribute(IReferenceAttributes.METHOD) != null)
 					reference.setKind(ReferenceKind.FUNCTION);
 			}
 		}
 	}
 
 	@Override
 	public IValueReference visitContinueStatement(ContinueStatement node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitDecimalLiteral(DecimalLiteral node) {
 		return context.getFactory().createNumber(peekContext());
 	}
 
 	@Override
 	public IValueReference visitDefaultXmlNamespace(
 			DefaultXmlNamespaceStatement node) {
 		visit(node.getValue());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitDoWhileStatement(DoWhileStatement node) {
 		visit(node.getCondition());
 		visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitEmptyExpression(EmptyExpression node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitEmptyStatement(EmptyStatement node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitForEachInStatement(ForEachInStatement node) {
 		IValueReference itemReference = visit(node.getItem());
 		IValueReference iteratorReference = visit(node.getIterator());
 		IRType type = JavaScriptValidations.typeOf(iteratorReference);
 		if (type != null) {
 			if (type instanceof IRArrayType
 					&& JavaScriptValidations.typeOf(itemReference) == null) {
 				final IRType itemType = ((IRArrayType) type).getItemType();
				setIRType(itemReference, itemType, true);
 			} else if (type instanceof IRMapType
 					&& JavaScriptValidations.typeOf(itemReference) == null) {
 				final IRType itemType = ((IRMapType) type).getValueType();
				setIRType(itemReference, itemType, true);
 			} else if (ITypeNames.XMLLIST.equals(type.getName())) {
 				itemReference.setDeclaredType(JSTypeSet.ref(context
 						.getType(ITypeNames.XML)));
 			}
 		}
 		visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitForInStatement(ForInStatement node) {
 		final IValueReference item = visit(node.getItem());
 		if (item != null) {
 			assign(item, context.getFactory().createString(peekContext()));
 		}
 		visit(node.getIterator());
 		visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitForStatement(ForStatement node) {
 		if (node.getInitial() != null)
 			visit(node.getInitial());
 		if (node.getCondition() != null)
 			visit(node.getCondition());
 		if (node.getStep() != null)
 			visit(node.getStep());
 		if (node.getBody() != null)
 			visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitFunctionStatement(FunctionStatement node) {
 		final JSMethod method = generateJSMethod(node);
 		final IValueCollection function = new FunctionValueCollection(
 				peekContext(), method.getName(), node.isInlineBlock());
 
 		for (IParameter parameter : method.getParameters()) {
 			final IValueReference refArg = function.createChild(parameter
 					.getName());
 			refArg.setKind(ReferenceKind.ARGUMENT);
 			setTypeImpl(refArg, parameter.getType());
 			refArg.setLocation(parameter.getLocation());
 		}
 		final Identifier methodName = node.getName();
 		final IValueReference result;
 		if (isChildFunction(node)) {
 			result = peekContext().createChild(method.getName());
 		} else {
 			result = new AnonymousValue();
 		}
 		result.setLocation(method.getLocation());
 		result.setKind(ReferenceKind.FUNCTION);
 		result.setDeclaredType(JSTypeSet.ref(context
 				.getType(ITypeNames.FUNCTION)));
 		result.setAttribute(IReferenceAttributes.METHOD, method);
 		result.setAttribute(IReferenceAttributes.R_METHOD,
 				RModelBuilder.create(getContext(), method));
 		result.setAttribute(IReferenceAttributes.FUNCTION_SCOPE, function);
 		result.setAttribute(IReferenceAttributes.RESOLVING, Boolean.TRUE);
 		enterContext(function);
 		Set<IProblemIdentifier> suppressed = null;
 		try {
 			if (reporter != null && !method.getSuppressedWarnings().isEmpty()) {
 				suppressed = new HashSet<IProblemIdentifier>();
 				for (IProblemCategory category : method.getSuppressedWarnings()) {
 					suppressed.addAll(category.contents());
 				}
 				reporter.pushSuppressWarnings(suppressed);
 			}
 			visitFunctionBody(node);
 		} finally {
 			if (reporter != null && suppressed != null) {
 				reporter.popSuppressWarnings();
 			}
 			leaveContext();
 			result.setAttribute(IReferenceAttributes.RESOLVING, null);
 		}
 		final IValueReference returnValue = result
 				.getChild(IValueReference.FUNCTION_OP);
 		returnValue.addValue(function.getReturnValue(), true);
 		setTypeImpl(returnValue, method.getType());
 		return result;
 	}
 
 	/**
 	 * @param node
 	 * @param methodName
 	 * @return
 	 */
 	protected static boolean isChildFunction(FunctionStatement node) {
 		return node.getName() != null
 				&& !(node.getParent() instanceof BinaryOperation)
 				&& !(node.getParent() instanceof VariableDeclaration)
 				&& !(node.getParent() instanceof PropertyInitializer)
 				&& !(node.getParent() instanceof NewExpression);
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	protected JSMethod generateJSMethod(FunctionStatement node) {
 		final JSMethod method = new JSMethod(node, getSource());
 		for (IModelBuilder extension : context.getModelBuilders()) {
 			extension.processMethod(node, method, reporter, getTypeChecker());
 		}
 		return method;
 	}
 
 	public void visitFunctionBody(FunctionStatement node) {
 		handleDeclarations(node);
 		visit(node.getBody());
 	}
 
 	public void setType(IValueReference value, JSType type, boolean lazyEnabled) {
 		setTypeImpl(value, type, lazyEnabled);
 	}
 
 	private void setTypeImpl(IValueReference value, JSType type) {
 		setTypeImpl(value, type, true);
 	}
 
 	private void setTypeImpl(IValueReference value, JSType type,
 			boolean lazyEnabled) {
 		if (type == null) {
 			return;
 		}
 		final IRType rt = JSTypeSet.normalize(getContext(), type);
 		setIRType(value, rt, lazyEnabled);
 	}
 
 	/**
 	 * @param value
 	 * @param type
 	 * @param lazyEnabled
 	 * @param rt
 	 */
 	private void setIRType(IValueReference value, final IRType rt,
 			boolean lazyEnabled) {
 		if (rt instanceof IRSimpleType) {
 			final Type t = ((IRSimpleType) rt).getTarget();
 			if (t.getKind() != TypeKind.UNKNOWN) {
 				value.setDeclaredType(rt);
 				if (value instanceof IValueProvider) {
 					for (IMemberEvaluator evaluator : TypeInfoManager
 							.getMemberEvaluators()) {
 						final IValueCollection collection = evaluator.valueOf(
 								context, t);
 						if (collection != null) {
 							if (collection instanceof IValueProvider) {
 								((IValueProvider) value).getValue().addValue(
 										((IValueProvider) collection)
 												.getValue());
 							}
 						}
 					}
 				}
 			} else if (lazyEnabled) {
 				value.addValue(new LazyTypeReference(context, t.getName(),
 						peekContext()), false);
 			}
 		} else {
 			value.setDeclaredType(rt);
 		}
 	}
 
 	@Override
 	public IValueReference visitGetAllChildrenExpression(
 			GetAllChildrenExpression node) {
 		return context.getFactory().createXMLList(peekContext());
 	}
 
 	@Override
 	public IValueReference visitGetArrayItemExpression(
 			GetArrayItemExpression node) {
 		final IValueReference array = visit(node.getArray());
 		visit(node.getIndex());
 		if (array != null) {
 			// always just create the ARRAY_OP child (for code completion)
 			IValueReference child = array.getChild(IValueReference.ARRAY_OP);
 			IRType arrayType = null;
 			if (array.getDeclaredType() != null) {
 				arrayType = TypeUtil.extractArrayItemType(array
 						.getDeclaredType());
 			} else {
 				JSTypeSet types = array.getTypes();
 				if (types.size() > 0)
 					arrayType = TypeUtil.extractArrayItemType(types.getFirst());
 			}
 			if (arrayType != null && child.getDeclaredType() == null) {
 				setIRType(child, arrayType, true);
 			}
 			if (node.getIndex() instanceof StringLiteral) {
 				IValueReference namedChild = extractNamedChild(array,
 						node.getIndex());
 				if (namedChild.exists()) {
 					child = namedChild;
 					if (arrayType != null && child.getDeclaredType() == null) {
 						child.setDeclaredType(arrayType);
 					}
 				}
 			}
 			return child;
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitGetLocalNameExpression(
 			GetLocalNameExpression node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitIdentifier(Identifier node) {
 		return peekContext().getChild(node.getName());
 
 	}
 
 	private Boolean evaluateCondition(Expression condition) {
 		if (condition instanceof BooleanLiteral) {
 			return Boolean.valueOf(((BooleanLiteral) condition).getText());
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public IValueReference visitIfStatement(IfStatement node) {
 		visit(node.getCondition());
 		visitIfStatements(node);
 		return null;
 	}
 
 	protected void visitIfStatements(IfStatement node) {
 		final List<Statement> statements = new ArrayList<Statement>(2);
 		Statement onlyBranch = null;
 		final Boolean condition = evaluateCondition(node.getCondition());
 		if ((condition == null || condition.booleanValue())
 				&& node.getThenStatement() != null) {
 			statements.add(node.getThenStatement());
 			if (condition != null && condition.booleanValue()) {
 				onlyBranch = node.getThenStatement();
 			}
 		}
 		if ((condition == null || !condition.booleanValue())
 				&& node.getElseStatement() != null) {
 			statements.add(node.getElseStatement());
 			if (condition != null && !condition.booleanValue()) {
 				onlyBranch = node.getElseStatement();
 			}
 		}
 		if (!statements.isEmpty()) {
 			if (statements.size() == 1) {
 				if (statements.get(0) == onlyBranch) {
 					visit(statements.get(0));
 				} else {
 					final Branching branching = branching();
 					visit(statements.get(0));
 					branching.end();
 				}
 			} else {
 				final Branching branching = branching();
 				final List<NestedValueCollection> collections = new ArrayList<NestedValueCollection>(
 						statements.size());
 				for (Statement statement : statements) {
 					final NestedValueCollection nestedCollection = new NestedValueCollection(
 							peekContext());
 					enterContext(nestedCollection);
 					visit(statement);
 					leaveContext();
 					collections.add(nestedCollection);
 				}
 				NestedValueCollection.mergeTo(peekContext(), collections);
 				branching.end();
 			}
 		}
 	}
 
 	@Override
 	public IValueReference visitLabelledStatement(LabelledStatement node) {
 		if (node.getStatement() != null)
 			visit(node.getStatement());
 		return null;
 	}
 
 	protected static class AnonymousNewValue extends AnonymousValue {
 		@Override
 		public IValueReference getChild(String name) {
 			if (name.equals(IValueReference.FUNCTION_OP))
 				return this;
 			return super.getChild(name);
 		}
 	}
 
 	public static class VisitNewResult {
 		IValueReference typeValue;
 		IValueReference[] arguments;
 		IValueReference value;
 
 		public IValueReference getValue() {
 			return value;
 		}
 
 		public IValueReference getTypeValue() {
 			return typeValue;
 		}
 
 		public IValueReference[] getArguments() {
 			return arguments;
 		}
 	}
 
 	protected VisitNewResult visitNew(NewExpression node) {
 		final VisitNewResult result = new VisitNewResult();
 		Expression objectClass = node.getObjectClass();
 		if (objectClass instanceof CallExpression) {
 			final CallExpression call = (CallExpression) objectClass;
 			result.arguments = new IValueReference[call.getArguments().size()];
 			int index = 0;
 			for (ASTNode argument : call.getArguments()) {
 				result.arguments[index++] = visit(argument);
 			}
 			objectClass = call.getExpression();
 		} else {
 			result.arguments = new IValueReference[0];
 		}
 		result.typeValue = visit(objectClass);
 
 		if (result.typeValue != null) {
 			if (result.typeValue.getKind() == ReferenceKind.FUNCTION) {
 				Object fs = result.typeValue
 						.getAttribute(IReferenceAttributes.FUNCTION_SCOPE);
 				if (fs instanceof IValueCollection
 						&& ((IValueCollection) fs).getThis() != null) {
 					result.value = new AnonymousNewValue();
 					result.value.setValue(((IValueCollection) fs).getThis());
 					result.value.setKind(ReferenceKind.TYPE);
 					String className = PropertyExpressionUtils
 							.getPath(objectClass);
 					if (className != null) {
 						Type type = TypeInfoModelFactory.eINSTANCE.createType();
 						type.setSuperType(context.getKnownType(OBJECT, null));
 						type.setKind(TypeKind.JAVASCRIPT);
 						type.setName(className);
 						result.value.setDeclaredType(JSTypeSet.ref(type));
 					} else {
 						result.value.setDeclaredType(JSTypeSet.ref(OBJECT));
 					}
 				}
 			} else if (result.typeValue.exists()) {
 				for (IRType type : result.typeValue.getDeclaredTypes()) {
 					if (type instanceof IRClassType) {
 						result.value = new AnonymousNewValue();
 						result.value.setKind(ReferenceKind.TYPE);
 						result.value.setDeclaredType(((IRClassType) type)
 								.toItemType());
 						return result;
 					}
 				}
 				for (IRType type : result.typeValue.getTypes()) {
 					if (type instanceof IRClassType) {
 						result.value = new AnonymousNewValue();
 						result.value.setKind(ReferenceKind.TYPE);
 						result.value.setDeclaredType(((IRClassType) type)
 								.toItemType());
 						return result;
 					}
 				}
 			}
 		}
 		if (result.value == null) {
 			final String className = PropertyExpressionUtils
 					.getPath(objectClass);
 			IValueCollection contextValueCollection = peekContext();
 			if (className != null) {
 				Type knownType = context.getKnownType(className, TypeMode.CODE);
 				if (knownType != null) {
 					result.value = new AnonymousNewValue();
 					result.value.setValue(context.getFactory().create(
 							contextValueCollection, JSTypeSet.ref(knownType)));
 					result.value.setKind(ReferenceKind.TYPE);
 				} else {
 					result.value = new LazyTypeReference(context, className,
 							contextValueCollection);
 				}
 			} else {
 				result.value = new AnonymousNewValue();
 				result.value.setValue(context.getFactory().createObject(
 						contextValueCollection));
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public IValueReference visitNewExpression(NewExpression node) {
 		return visitNew(node).getValue();
 	}
 
 	@Override
 	public IValueReference visitNullExpression(NullExpression node) {
 		return null;
 	}
 
 	@Override
 	public IValueReference visitObjectInitializer(ObjectInitializer node) {
 		final IValueReference result = new AnonymousValue();
 		result.setDeclaredType(JSTypeSet.ref(OBJECT));
 		for (ObjectInitializerPart part : node.getInitializers()) {
 			if (part instanceof PropertyInitializer) {
 				final PropertyInitializer pi = (PropertyInitializer) part;
 				final IValueReference child = extractNamedChild(result,
 						pi.getName());
 				final IValueReference value = visit(pi.getValue());
 				if (child != null) {
 					child.setValue(value);
 					child.setLocation(ReferenceLocation.create(getSource(), pi
 							.getName().sourceStart(), pi.getName().sourceEnd()));
 					if (child.getKind() == ReferenceKind.UNKNOWN) {
 						child.setKind(ReferenceKind.FIELD);
 					}
 				}
 			} else {
 				// TODO handle get/set methods
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public IValueReference visitParenthesizedExpression(
 			ParenthesizedExpression node) {
 		return visit(node.getExpression());
 	}
 
 	@Override
 	public IValueReference visitPropertyExpression(PropertyExpression node) {
 		final IValueReference object = visit(node.getObject());
 		return extractNamedChild(object, node.getProperty());
 	}
 
 	protected IValueReference extractNamedChild(IValueReference parent,
 			Expression name) {
 		if (parent != null) {
 			final String nameStr;
 			if (name instanceof Identifier) {
 				nameStr = ((Identifier) name).getName();
 				IRType parentType = JavaScriptValidations.typeOf(parent);
 				if (parentType != null && isXML(parentType)) {
 					IValueReference child = parent.getChild(nameStr);
 					if (child != null && child.getDeclaredType() == null) {
 						child.setDeclaredType(JSTypeSet.ref(ITypeNames.XML));
 						return child;
 					}
 				}
 			} else if (name instanceof StringLiteral) {
 				nameStr = ((StringLiteral) name).getValue();
 			} else if (name instanceof XmlAttributeIdentifier) {
 				if (((XmlAttributeIdentifier) name).getExpression() instanceof AsteriskExpression) {
 					return visitAsteriskExpression((AsteriskExpression) ((XmlAttributeIdentifier) name)
 							.getExpression());
 				} else {
 					nameStr = ((XmlAttributeIdentifier) name)
 							.getAttributeName();
 					IValueReference child = parent.getChild(nameStr);
 					if (child != null && child.getDeclaredType() == null) {
 						child.setDeclaredType(JSTypeSet.ref(ITypeNames.XML));
 						return child;
 					}
 				}
 			} else if (name instanceof AsteriskExpression) {
 				return visitAsteriskExpression((AsteriskExpression) name);
 			} else if (name instanceof ParenthesizedExpression) {
 				visitParenthesizedExpression((ParenthesizedExpression) name);
 				return parent;
 			} else {
 				return null;
 			}
 			return parent.getChild(nameStr);
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitRegExpLiteral(RegExpLiteral node) {
 		return context.getFactory().createRegExp(peekContext());
 	}
 
 	@Override
 	public IValueReference visitReturnStatement(ReturnStatement node) {
 		if (node.getValue() != null) {
 			final IValueReference value = visit(node.getValue());
 			if (value != null) {
 				final IValueReference returnValue = peekContext()
 						.getReturnValue();
 				if (returnValue != null) {
 					returnValue.addValue(value,
 							!(value instanceof LazyTypeReference));
 				}
 			}
 			return value;
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitScript(Script node) {
 		handleDeclarations(node);
 		return visit(node.getStatements());
 	}
 
 	private void handleDeclarations(JSScope scope) {
 		for (JSDeclaration declaration : scope.getDeclarations()) {
 			if (declaration instanceof FunctionStatement) {
 
 			} else if (declaration instanceof VariableDeclaration) {
 
 			}
 		}
 	}
 
 	@Override
 	public IValueReference visitStatementBlock(StatementBlock node) {
 		for (Statement statement : node.getStatements()) {
 			visit(statement);
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitStringLiteral(StringLiteral node) {
 		return context.getFactory().createString(peekContext());
 	}
 
 	@Override
 	public IValueReference visitSwitchStatement(SwitchStatement node) {
 		if (node.getCondition() != null)
 			visit(node.getCondition());
 		for (SwitchComponent component : node.getCaseClauses()) {
 			if (component instanceof CaseClause) {
 				visit(((CaseClause) component).getCondition());
 			}
 			visit(component.getStatements());
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitThisExpression(ThisExpression node) {
 		return peekContext().getThis();
 	}
 
 	@Override
 	public IValueReference visitThrowStatement(ThrowStatement node) {
 		if (node.getException() != null)
 			visit(node.getException());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitTryStatement(TryStatement node) {
 		visit(node.getBody());
 		for (CatchClause catchClause : node.getCatches()) {
 			final NestedValueCollection collection = new NestedValueCollection(
 					peekContext());
 			IValueReference exception = collection.createChild(catchClause
 					.getException().getName());
 			exception.setDeclaredType(JSTypeSet.ref(ITypeNames.ERROR));
 
 			enterContext(collection);
 			if (catchClause.getStatement() != null) {
 				visit(catchClause.getStatement());
 			}
 			leaveContext();
 		}
 		if (node.getFinally() != null) {
 			visit(node.getFinally().getStatement());
 		}
 		return null;
 	}
 
 	@Override
 	public IValueReference visitUnaryOperation(UnaryOperation node) {
 		if (node.getOperation() == JSParser.NOT) {
 			visit(node.getExpression());
 			return context.getFactory().createBoolean(peekContext());
 		} else if (node.getOperation() == JSParser.DELETE) {
 			final IValueReference value = visit(node.getExpression());
 			if (value != null) {
 				value.delete();
 			}
 			return context.getFactory().createBoolean(peekContext());
 		} else if (node.getOperation() == JSParser.TYPEOF) {
 			visit(node.getExpression());
 			return context.getFactory().createString(peekContext());
 		} else if (node.getOperation() == JSParser.VOID) {
 			visit(node.getExpression());
 			return null;
 		} else {
 			return visit(node.getExpression());
 		}
 	}
 
 	@Override
 	public IValueReference visitVariableStatement(VariableStatement node) {
 		final IValueCollection collection = peekContext();
 		IValueReference result = null;
 		for (VariableDeclaration declaration : node.getVariables()) {
 			result = createVariable(collection, declaration);
 		}
 		return result;
 	}
 
 	@Override
 	public IValueReference visitVoidExpression(VoidExpression node) {
 		visit(node.getExpression());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitWhileStatement(WhileStatement node) {
 		if (node.getCondition() != null)
 			visit(node.getCondition());
 		if (node.getBody() != null)
 			visit(node.getBody());
 		return null;
 	}
 
 	@Override
 	public IValueReference visitWithStatement(WithStatement node) {
 		final IValueReference with = visit(node.getExpression());
 		if (with != null) {
 			final WithValueCollection withCollection = new WithValueCollection(
 					peekContext(), with);
 			enterContext(withCollection);
 			visit(node.getStatement());
 			leaveContext();
 		} else {
 			visit(node.getStatement());
 		}
 		return null;
 	}
 
 	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
 			.newInstance();
 	private DocumentBuilder docBuilder;
 
 	/**
 	 * @return
 	 * @throws ParserConfigurationException
 	 */
 	private DocumentBuilder getDocumentBuilder()
 			throws ParserConfigurationException {
 		if (docBuilder == null)
 			docBuilder = docBuilderFactory.newDocumentBuilder();
 		return docBuilder;
 	}
 
 	@Override
 	public IValueReference visitXmlLiteral(XmlLiteral node) {
 		IValueReference xmlValueReference = context.getFactory().createXML(
 				peekContext());
 
 		if (xmlValueReference instanceof IValueProvider) {
 			IRType xmlType = JSTypeSet.ref(context.getKnownType(ITypeNames.XML,
 					null));
 			IValue xmlValue = ((IValueProvider) xmlValueReference).getValue();
 			List<XmlFragment> fragments = node.getFragments();
 			StringBuilder xml = new StringBuilder();
 			for (XmlFragment xmlFragment : fragments) {
 				if (xmlFragment instanceof XmlTextFragment) {
 					String xmlText = ((XmlTextFragment) xmlFragment).getXml();
 					if (xmlText.equals("<></>"))
 						continue;
 					if (xmlText.startsWith("<>") && xmlText.endsWith("</>")) {
 						xmlText = "<xml>"
 								+ xmlText.substring(2, xmlText.length() - 3)
 								+ "</xml>";
 					}
 					xml.append(xmlText);
 				} else if (xmlFragment instanceof XmlExpressionFragment) {
 					Expression expression = ((XmlExpressionFragment) xmlFragment)
 							.getExpression();
 					visit(expression);
 					if (xml.charAt(xml.length() - 1) == '<'
 							|| xml.subSequence(xml.length() - 2, xml.length())
 									.equals("</")) {
 						if (expression instanceof Identifier) {
 							xml.append(((Identifier) expression).getName());
 						} else {
 							xml.setLength(0);
 							break;
 						}
 					} else
 						xml.append("\"\" ");
 				}
 			}
 
 			if (xml.length() > 0) {
 				try {
 					DocumentBuilder docBuilder = getDocumentBuilder();
 					Document doc = docBuilder.parse(new InputSource(
 							new StringReader(xml.toString())));
 					NodeList nl = doc.getChildNodes();
 					if (nl.getLength() == 1) {
 						Node item = nl.item(0);
 						NamedNodeMap attributes = item.getAttributes();
 						for (int a = 0; a < attributes.getLength(); a++) {
 							Node attribute = attributes.item(a);
 							xmlValue.createChild("@" + attribute.getNodeName(),
 									0);
 						}
 						createXmlChilds(xmlType, xmlValue, item.getChildNodes());
 					} else {
 						System.err.println("root should be 1 child?? " + xml);
 					}
 				} catch (Exception e) {
 				}
 			}
 		}
 		return xmlValueReference;
 	}
 
 	/**
 	 * @param xmlType
 	 * @param xmlValue
 	 * @param nl
 	 */
 	private void createXmlChilds(IRType xmlType, IValue xmlValue, NodeList nl) {
 		for (int i = 0; i < nl.getLength(); i++) {
 			Node item = nl.item(i);
 			if (item.getNodeType() == Node.TEXT_NODE) {
 				String value = item.getNodeValue();
 				if (value == null || "".equals(value.trim())) {
 					continue;
 				}
 			}
 			IValue nodeValue = xmlValue.createChild(item.getNodeName(), 0);
 			nodeValue.setDeclaredType(xmlType);
 			NamedNodeMap attributes = item.getAttributes();
 			if (attributes != null) {
 				for (int a = 0; a < attributes.getLength(); a++) {
 					Node attribute = attributes.item(a);
 					nodeValue.createChild("@" + attribute.getNodeName(), 0);
 				}
 			}
 			createXmlChilds(xmlType, nodeValue, item.getChildNodes());
 		}
 	}
 
 	@Override
 	public IValueReference visitXmlPropertyIdentifier(
 			XmlAttributeIdentifier node) {
 		return context.getFactory().createXML(peekContext());
 	}
 
 	@Override
 	public IValueReference visitYieldOperator(YieldOperator node) {
 		final IValueReference value = visit(node.getExpression());
 		if (value != null) {
 			final IValueReference reference = peekContext().getReturnValue();
 			if (reference != null) {
 				reference.addValue(value, true);
 			}
 		}
 		return null;
 	}
 
 	public static boolean isFunctionDeclaration(Expression expression) {
 		PropertyExpression pe = null;
 		if (expression instanceof PropertyExpression)
 			pe = (PropertyExpression) expression;
 		else if (expression.getParent() instanceof PropertyExpression)
 			pe = (PropertyExpression) expression.getParent();
 		if (pe != null && pe.getObject() instanceof ThisExpression
 				&& pe.getParent() instanceof BinaryOperation) {
 			return ((BinaryOperation) pe.getParent()).getRightExpression() instanceof FunctionStatement;
 		}
 		return false;
 	}
 
 }
