 package swp_compiler_ss13.fuc.semantic_analyser;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.IdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.binary.ArithmeticBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.DoWhileNode;
 import swp_compiler_ss13.common.ast.nodes.binary.LogicBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.LoopNode;
 import swp_compiler_ss13.common.ast.nodes.binary.RelationExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.WhileNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BreakNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArithmeticUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArrayIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.unary.LogicUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.PrintNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 import swp_compiler_ss13.common.ast.nodes.unary.StructIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode;
 import static swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode.UnaryOperator.MINUS;
 import swp_compiler_ss13.common.report.ReportLog;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.report.ReportType;
 import swp_compiler_ss13.common.types.Type;
 import swp_compiler_ss13.common.types.derived.ArrayType;
 import swp_compiler_ss13.common.types.derived.DerivedType;
 import swp_compiler_ss13.common.types.derived.Member;
 import swp_compiler_ss13.common.types.derived.StructType;
 import swp_compiler_ss13.common.types.primitive.DoubleType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 
 public class SemanticAnalyser implements swp_compiler_ss13.common.semanticAnalysis.SemanticAnalyser {
 
 	private static Logger logger = Logger.getLogger(SemanticAnalyser.class);
 
 	enum Attribute {
 
 		/**
 		 * num, basic, array,...
 		 */
 		TYPE,
 		/**
 		 * name of identifier
 		 *
 		 * @see IdentifierNode
 		 */
 		IDENTIFIER,
 		CAN_BREAK,
 		TYPE_CHECK,
 		CODE_STATE
 	}
 	private static final String NO_ATTRIBUTE_VALUE = "undefined";
 	private static final String CAN_BREAK = "true";
 	private static final String TYPE_MISMATCH = "type mismatch";
 	private static final String DEAD_CODE = "dead";
 	private ReportLog errorLog;
 	private Map<ASTNode, Map<Attribute, String>> attributes;
 	private Map<ASTNode, Type> typeDeclarations;
 	private Map<ASTNode, Object> staticValues;
 	/**
 	 * Contains all initialized identifiers. As soon it has assigned it will be
 	 * added.
 	 */
 	private Map<SymbolTable, Set<String>> initializedIdentifiers;
 
 	public SemanticAnalyser() {
 		attributes = new HashMap<>();
 		initializedIdentifiers = new HashMap<>();
 		typeDeclarations = new HashMap<>();
 		staticValues = new HashMap<>();
 	}
 
 	public SemanticAnalyser(ReportLog log) {
 		attributes = new HashMap<>();
 		initializedIdentifiers = new HashMap<>();
 		typeDeclarations = new HashMap<>();
 		staticValues = new HashMap<>();
 		errorLog = log;
 	}
 
 	protected Map<SymbolTable, Set<String>> copy(Map<SymbolTable, Set<String>> m) {
 		Map<SymbolTable, Set<String>> c = new HashMap<>(m);
 
 		for (SymbolTable x : m.keySet()) {
 			Set<String> i = new HashSet<>();
 
 			for (String y : m.get(x)) {
 				i.add(y);
 			}
 
 			c.put(x, i);
 		}
 
 		return c;
 	}
 
 	@Override
 	public void setReportLog(ReportLog log) {
 		errorLog = log;
 	}
 
 	@Override
 	public AST analyse(AST ast) {
 		assert (errorLog != null);
 
 		attributes.clear();
 		initializedIdentifiers.clear();
 
 		logger.debug("Analyzing ... please stand by!");
 		traverse(ast.getRootNode(), ast.getRootSymbolTable());
 
 		return ast;
 	}
 
 	protected boolean hasStaticValue(ASTNode node) {
 		return staticValues.containsKey(node);
 	}
 
 	protected Object getStaticValue(ASTNode node) {
 		if (staticValues.containsKey(node)) {
 			return staticValues.get(node);
 		} else {
 			return null;
 		}
 	}
 
 	protected void evaluateStaticExpresionValue(ExpressionNode node) {
 		Object result = null;
 
 		if (node instanceof LiteralNode) {
 			LiteralNode expr = (LiteralNode) node;
 			switch (expr.getLiteralType().getKind()) {
 				case LONG:
 					result = Long.valueOf(expr.getLiteral());
 					break;
 				case DOUBLE:
 					result = Double.valueOf(expr.getLiteral());
 					break;
 				case BOOLEAN:
 					result = Boolean.valueOf(expr.getLiteral());
 					break;
 				case STRING:
 					result = expr.getLiteral();
 					break;
 			}
 		} else if (node instanceof AssignmentNode) {
 			AssignmentNode expr = (AssignmentNode) node;
 			result = getStaticValue(expr.getRightValue());
 		} else if (node instanceof UnaryExpressionNode) {
 			UnaryExpressionNode expr = (UnaryExpressionNode) node;
 			ExpressionNode rightexpr = expr.getRightValue();
 			Object rightvalue = getStaticValue(rightexpr);
 
 			if (rightvalue == null) {
 				return;
 			}
 
 			if (rightvalue instanceof Number) {
 				boolean useDouble = getType(rightexpr) == Type.Kind.DOUBLE;
 				Number right = (Number) rightvalue;
 
 				switch (expr.getOperator()) {
 					case MINUS:
 						if (useDouble) {
 							result = -right.doubleValue();
 						} else {
 							result = -right.longValue();
 						}
 						break;
 				}
 			} else if (rightvalue instanceof Boolean) {
 				Boolean right = (Boolean) rightvalue;
 
 				switch (expr.getOperator()) {
 					case LOGICAL_NEGATE:
 						result = !right;
 						break;
 				}
 			}
 		} else if (node instanceof BinaryExpressionNode) {
 			BinaryExpressionNode expr = (BinaryExpressionNode) node;
 			ExpressionNode leftexpr = expr.getLeftValue();
 			ExpressionNode rightexpr = expr.getRightValue();
 			Object leftvalue = getStaticValue(leftexpr);
 			Object rightvalue = getStaticValue(rightexpr);
 
 			if (leftvalue == null || rightvalue == null) {
 				return;
 			}
 
 			if (leftvalue instanceof Number && rightvalue instanceof Number) {
 				Number left = (Number) leftvalue;
 				Number right = (Number) rightvalue;
 				boolean useDouble = false;
 
 				if (getType(leftexpr) == Type.Kind.DOUBLE || getType(rightexpr) == Type.Kind.DOUBLE) {
 					useDouble = true;
 				}
 
 				switch (expr.getOperator()) {
 					case ADDITION:
 						if (useDouble) {
 							result = left.doubleValue() + right.doubleValue();
 						} else {
 							result = left.longValue() + right.longValue();
 						}
 						break;
 					case SUBSTRACTION:
 						if (useDouble) {
 							result = left.doubleValue() - right.doubleValue();
 						} else {
 							result = left.longValue() - right.longValue();
 						}
 						break;
 					case MULTIPLICATION:
 						if (useDouble) {
 							result = left.doubleValue() * right.doubleValue();
 						} else {
 							result = left.longValue() * right.longValue();
 						}
 						break;
 					case DIVISION:
 						if (useDouble) {
 							result = left.doubleValue() / right.doubleValue();
 						} else {
 							result = left.longValue() / right.longValue();
 						}
 						break;
 					case LESSTHAN:
 						if (useDouble) {
 							result = left.doubleValue() < right.doubleValue();
 						} else {
 							result = left.longValue() < right.longValue();
 						}
 						break;
 					case LESSTHANEQUAL:
 						if (useDouble) {
 							result = left.doubleValue() <= right.doubleValue();
 						} else {
 							result = left.longValue() <= right.longValue();
 						}
 						break;
 					case GREATERTHAN:
 						if (useDouble) {
 							result = left.doubleValue() > right.doubleValue();
 						} else {
 							result = left.longValue() > right.longValue();
 						}
 						break;
 					case GREATERTHANEQUAL:
 						if (useDouble) {
 							result = left.doubleValue() >= right.doubleValue();
 						} else {
 							result = left.longValue() >= right.longValue();
 						}
 						break;
 					case EQUAL:
 						if (useDouble) {
 							result = left.doubleValue() == right.doubleValue();
 						} else {
 							result = left.longValue() == right.longValue();
 						}
 						break;
 					case INEQUAL:
 						if (useDouble) {
 							result = left.doubleValue() != right.doubleValue();
 						} else {
 							result = left.longValue() != right.longValue();
 						}
 						break;
 				}
 			} else if (leftvalue instanceof Boolean && rightvalue instanceof Boolean) {
 				Boolean left = (Boolean) leftvalue;
 				Boolean right = (Boolean) rightvalue;
 
 				switch (expr.getOperator()) {
 					case EQUAL:
 						result = left == right;
 						break;
 					case INEQUAL:
 						result = left != right;
 						break;
 					case LOGICAL_AND:
 						result = left && right;
 						break;
 					case LOGICAL_OR:
 						result = left || right;
 						break;
 				}
 			}
 		}
 
 		if (result != null) {
 			staticValues.put(node, result);
 			logger.debug("static value for " + node + ": " + result.toString());
 		}
 	}
 
 	protected void inheritAttribute(ASTNode parent, ASTNode child, Attribute attr) {
 		String value = getAttribute(parent, attr);
 
 		if (!value.equals(NO_ATTRIBUTE_VALUE)) {
 			setAttribute(child, attr, value);
 		}
 	}
 
 	protected Long getLongValue(ASTNode node) {
 		Object v = getStaticValue(node);
 
 		if (v instanceof Long) {
 			return (Long) v;
 		} else {
 			return null;
 		}
 	}
 
 	protected boolean isValueNumericallyZero(ASTNode node) {
 		Object v = getStaticValue(node);
 
 		if (v instanceof Long) {
 			return (Long) v == 0;
 		} else if (v instanceof Double) {
 			return (Double) v == 0;
 		}
 
 		return false;
 	}
 
 	protected void traverse(ASTNode node, SymbolTable table) {
 		logger.debug("traverse: " + node);
 
 		inheritAttribute(node.getParentNode(), node, Attribute.CAN_BREAK);
 
 		switch (node.getNodeType()) {
 			case BasicIdentifierNode:
 				handleNode((BasicIdentifierNode) node, table);
 				break;
 			case BreakNode:
 				handleNode((BreakNode) node, table);
 				break;
 			case LiteralNode:
 				handleNode((LiteralNode) node, table);
 				break;
 			case ArithmeticUnaryExpressionNode:
 				handleNode((ArithmeticUnaryExpressionNode) node, table);
 				break;
 			case ArrayIdentifierNode:
 				handleNode((ArrayIdentifierNode) node, table);
 				break;
 			case DeclarationNode:
 				break;
 			case LogicUnaryExpressionNode:
 				handleNode((LogicUnaryExpressionNode) node, table);
 				break;
 			case PrintNode:
 				handleNode((PrintNode) node, table);
 				break;
 			case ReturnNode:
 				handleNode((ReturnNode) node, table);
 				break;
 			case StructIdentifierNode:
 				handleNode((StructIdentifierNode) node, table);
 				break;
 			case ArithmeticBinaryExpressionNode:
 				handleNode((ArithmeticBinaryExpressionNode) node, table);
 				break;
 			case AssignmentNode:
 				handleNode((AssignmentNode) node, table);
 				break;
 			case DoWhileNode:
 				handleNode((DoWhileNode) node, table);
 				break;
 			case LogicBinaryExpressionNode:
 				handleNode((LogicBinaryExpressionNode) node, table);
 				break;
 			case RelationExpressionNode:
 				handleNode((RelationExpressionNode) node, table);
 				break;
 			case WhileNode:
 				handleNode((WhileNode) node, table);
 				break;
 			case BranchNode:
 				handleNode((BranchNode) node, table);
 				break;
 			case BlockNode:
 				handleNode((BlockNode) node, table);
 				break;
 			default:
 				throw new IllegalArgumentException("unknown ASTNodeType");
 		}
 
		if (node instanceof ExpressionNode && !hasTypeError(node)) {
 			evaluateStaticExpresionValue((ExpressionNode) node);
 		}
 	}
 
 	protected Type.Kind getType(ASTNode node) {
 		String attr = getAttribute(node, Attribute.TYPE);
 		if (!attr.equals(NO_ATTRIBUTE_VALUE)) {
 			return Type.Kind.valueOf(attr);
 		} else {
 			return null;
 		}
 	}
 
 	protected Type.Kind leastUpperBoundType(ASTNode left, ASTNode right) {
 		TreeSet<Type.Kind> types = new TreeSet<>();
 		Type.Kind leftType = getType(left);
 		Type.Kind rightType = getType(right);
 
 		if (leftType == null || rightType == null) {
 			return null;
 		}
 
 		types.add(leftType);
 		types.add(rightType);
 
 		if (types.size() == 1) {
 			return types.first();
 		}
 
 		if (types.contains(Type.Kind.LONG) && types.contains(Type.Kind.DOUBLE)) {
 			return Type.Kind.DOUBLE;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Check if k is in (LONG, DOUBLE).
 	 *
 	 * @param k
 	 * @return
 	 */
 	protected boolean isNumeric(Type.Kind k) {
 		return k == Type.Kind.DOUBLE || k == Type.Kind.LONG;
 	}
 
 	protected boolean isBool(Type.Kind k) {
 		return k == Type.Kind.BOOLEAN;
 	}
 
 	/*
 	 * Loops
 	 */
 	protected void checkLoopNode(LoopNode node, SymbolTable table) {
 		if (getType(node.getCondition()) != Type.Kind.BOOLEAN) {
 			errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(), "The condition must be of type bool.");
 		}
 	}
 
 	protected void handleNode(DoWhileNode node, SymbolTable table) {
 		setAttribute(node, Attribute.CAN_BREAK, CAN_BREAK);
 		traverse(node.getLoopBody(), table);
 		traverse(node.getCondition(), table);
 
 		checkLoopNode(node, table);
 	}
 
 	protected void handleNode(WhileNode node, SymbolTable table) {
 		setAttribute(node, Attribute.CAN_BREAK, CAN_BREAK);
 		traverse(node.getCondition(), table);
 
 		/*
 		 * Ignore all initialization after leaving the body's scope,
 		 * because the body may not be entered. But inside the body
 		 * the initializations are valid.
 		 * TODO: Better checking!
 		 */
 		Map<SymbolTable, Set<String>> before = copy(initializedIdentifiers);
 		traverse(node.getLoopBody(), table);
 		initializedIdentifiers = before;
 
 		checkLoopNode(node, table);
 	}
 
 	protected void handleNode(BreakNode node, SymbolTable table) {
 		if (!hasAttribute(node.getParentNode(), Attribute.CAN_BREAK, CAN_BREAK)) {
 			errorLog.reportError(ReportType.UNDEFINED, node.coverage(), "Break can only be used in a loop.");
 		}
 	}
 
 	/*
 	 * Branch node
 	 */
 	protected List<SymbolTable> getSymbolTableChain(SymbolTable table) {
 		List<SymbolTable> chain = new LinkedList<>();
 
 		do {
 			chain.add(table);
 			table = table.getParentSymbolTable();
 		} while (table != null);
 
 		return chain;
 	}
 
 	protected void handleNode(BranchNode node, SymbolTable table) {
 		traverse(node.getCondition(), table);
 
 		Map<SymbolTable, Set<String>> beforeTrue = copy(initializedIdentifiers);
 		List<SymbolTable> beforeTrueTableChain = getSymbolTableChain(table);
 		traverse(node.getStatementNodeOnTrue(), table);
 
 		if (node.getStatementNodeOnFalse() != null) {
 			Map<SymbolTable, Set<String>> beforeFalse = copy(initializedIdentifiers);
 			initializedIdentifiers = beforeTrue;
 			traverse(node.getStatementNodeOnFalse(), table);
 
 			/*
 			 * Remove all initializations that do not occur in both branches.
 			 */
 			for (SymbolTable s : beforeTrueTableChain) {
 				if (initializedIdentifiers.containsKey(s)) {
 					/*
 					 * initializedIdentifiers contains the state after the
 					 * else branch, beforeFalse contains the state after
 					 * the true branch.
 					 * 
 					 * Remove all from else, that were not also assigent
 					 * in the true branch.
 					 */
 					logger.debug(s + ": f:" + initializedIdentifiers.get(s) + ",  t:" + beforeFalse.get(s));
 					initializedIdentifiers.get(s).retainAll(beforeFalse.get(s));
 				}
 			}
 		} else {
 			logger.debug("No else branch, reset initialization.");
 			initializedIdentifiers = beforeTrue;
 		}
 
 		if (getType(node.getCondition()) != Type.Kind.BOOLEAN) {
 			errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(), "The condition must be of type bool.");
 			markTypeError(node);
 		}
 	}
 
 	/*
 	 * Unary and binary expressions.
 	 */
 	protected void binaryExpression(BinaryExpressionNode node, SymbolTable table) {
 		/*
 		 * Left sub tree
 		 */
 		ExpressionNode left = node.getLeftValue();
 		traverse(left, table);
 
 		/*
 		 * right sub tree
 		 */
 		ExpressionNode right = node.getRightValue();
 		traverse(right, table);
 
 		Type.Kind type = leastUpperBoundType(left, right);
 
 		if (hasTypeError(left) || hasTypeError(right)) {
 			markTypeError(node);
 		} else {
 			if (type == null) {
 				markTypeError(node);
 				errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 					"No implicit cast (no upper bound) defined for " + getType(left) + " and " + getType(right) + ".");
 			} else {
 				setType(node, type);
 			}
 		}
 	}
 
 	protected void unaryExpression(UnaryExpressionNode node, SymbolTable table) {
 		ExpressionNode expression = node.getRightValue();
 		traverse(expression, table);
 
 		if (hasTypeError(expression)) {
 			markTypeError(node);
 		} else {
 			setType(node, getType(expression));
 		}
 	}
 
 	/*
 	 * Arithmetic
 	 */
 	protected void handleNode(ArithmeticBinaryExpressionNode node, SymbolTable table) {
 		binaryExpression(node, table);
 
 		if (!hasTypeError(node)) {
 			Type.Kind type = getType(node);
 
 			if (!isNumeric(type) /*|| (type == Type.Kind.STRING && node.getOperator() == BinaryExpressionNode.BinaryOperator.ADDITION)*/) {
 				markTypeError(node);
 				errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 					"Operator " + node.getOperator().name() + " is not defined for " + getType(node) + ".");
 			} else if (node.getOperator() == BinaryExpressionNode.BinaryOperator.DIVISION) {
 				if (isValueNumericallyZero(node.getRightValue())) {
 					errorLog.reportError(ReportType.DIVISION_BY_ZERO, node.getRightValue().coverage(), "Division by zero.");
					markTypeError(node);
 				}
 			}
 		}
 	}
 
 	protected void handleNode(ArithmeticUnaryExpressionNode node, SymbolTable table) {
 		unaryExpression(node, table);
 
 		if (!hasTypeError(node)) {
 			if (!isNumeric(getType(node))) {
 				markTypeError(node);
 				errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 					"Operation " + node.getOperator() + " is not defined for " + getType(node) + ".");
 			}
 		}
 	}
 
 	protected void handleNode(RelationExpressionNode node, SymbolTable table) {
 		binaryExpression(node, table);
 
 		if (!hasTypeError(node)) {
 			Type.Kind type = getType(node);
 
 			BinaryExpressionNode.BinaryOperator op = node.getOperator();
 
 			if (!isNumeric(type) && (op != BinaryExpressionNode.BinaryOperator.EQUAL || op != BinaryExpressionNode.BinaryOperator.INEQUAL)) {
 				markTypeError(node);
 				errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 					"Operator " + node.getOperator() + " expects numeric operands.");
 			} else {
 				/*
 				 * A ReleationExpression results in a boolean.
 				 */
 				setAttribute(node, Attribute.TYPE, Type.Kind.BOOLEAN.name());
 			}
 		}
 	}
 
 	/*
 	 * Bool
 	 */
 	protected void handleNode(LogicBinaryExpressionNode node, SymbolTable table) {
 		binaryExpression(node, table);
 
 		if (!hasTypeError(node)) {
 			if (!isBool(getType(node))) {
 				markTypeError(node);
 				errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 					"Operator " + node.getOperator() + " expects boolean operands");
 			}
 		}
 	}
 
 	protected void handleNode(LogicUnaryExpressionNode node, SymbolTable table) {
 		unaryExpression(node, table);
 
 		if (!hasTypeError(node)) {
 			if (!isBool(getType(node))) {
 				markTypeError(node);
 				errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 					"Operator " + node.getOperator() + " expects boolean operand.");
 			}
 		}
 	}
 
 	/*
 	 * Block
 	 */
 	protected void handleNode(BlockNode node, SymbolTable table) {
 		SymbolTable blockScope = node.getSymbolTable();
 
 		for (StatementNode child : node.getStatementList()) {
 			if (hasAttribute(node, Attribute.CODE_STATE, DEAD_CODE)) {
 				errorLog.reportError(ReportType.UNDEFINED, child.coverage(),
 					"Unreachable statement, see previous “return” in block.");
 			}
 
 			traverse(child, blockScope);
 		}
 	}
 
 	protected void handleNode(AssignmentNode node, SymbolTable table) {
 		IdentifierNode lvalue = node.getLeftValue();
 		ExpressionNode rvalue = node.getRightValue();
 
 		traverse(rvalue, table);
 		traverse(lvalue, table);
 
 		if (hasAttribute(node.getLeftValue(), Attribute.TYPE_CHECK, TYPE_MISMATCH)
 			|| hasAttribute(node.getRightValue(), Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			markTypeError(node);
 		} else if (getType(node.getLeftValue())
 			!= leastUpperBoundType(node.getLeftValue(), node.getRightValue())) {
 
 			markTypeError(node);
 			errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 				"Expected " + getType(node.getLeftValue())
 				+ " found " + getType(node.getRightValue()));
 		} else if (getTypeDeclaration(lvalue) instanceof DerivedType) {
 			errorLog.reportError(ReportType.TYPE_MISMATCH, lvalue.coverage(), "Only primitve types can be assigned.");
 			markTypeError(node);
 		} else {
 			markIdentifierAsInitialized(table, getAttribute(node.getLeftValue(), Attribute.IDENTIFIER));
 			setAttribute(node, Attribute.TYPE, getAttribute(node.getLeftValue(), Attribute.TYPE));
 
 			logger.debug("Assignment: " + getAttribute(node.getLeftValue(), Attribute.IDENTIFIER)
 				+ ":" + getType(node.getLeftValue()) + " := " + getType(node.getRightValue()));
 		}
 	}
 
 	/*
 	 * Leaf nodes
 	 */
 	protected void handleNode(LiteralNode node, SymbolTable table) {
 		setAttribute(node, Attribute.TYPE, node.getLiteralType().getKind().name());
 	}
 
 	protected void handleNode(BasicIdentifierNode node, SymbolTable table) {
 		String identifier = node.getIdentifier();
 		Type t = table.lookupType(node.getIdentifier());
 		boolean initialzed = isInitialized(table, identifier);
 
 		if (t == null) {
 			errorLog.reportError(ReportType.UNDECLARED_VARIABLE_USAGE, node.coverage(),
 				"Identifier “" + identifier + "” has not been declared.");
 			markTypeError(node);
 			return;
 		}
 
 		boolean array = t.getKind() == Type.Kind.ARRAY;
 
 		logger.debug("BasicIdentifierNode: identifier=" + identifier + ", initialized=" + initialzed + ", type=" + t);
 
 
 
 		setAttribute(node, Attribute.IDENTIFIER, identifier);
 		setAttribute(node, Attribute.TYPE, t.getKind().name());
 		setTypeDeclaration(node, t);
 
 		/*
 		 * checks
 		 */
 		boolean reportInitialization = false;
 
 		if (node.getParentNode() instanceof AssignmentNode) {
 			AssignmentNode p = (AssignmentNode) node.getParentNode();
 			reportInitialization = p.getLeftValue() != node;
 		} else if (node.getParentNode().getNodeType() != ASTNode.ASTNodeType.AssignmentNode && !array) {
 			reportInitialization = true;
 		}
 
 		if (reportInitialization && !initialzed) {
 			errorLog.reportWarning(ReportType.UNDEFINED, node.coverage(),
 				"Variable “" + identifier + "” may be used without initialization.");
 		}
 	}
 
 	protected void handleNode(ArrayIdentifierNode node, SymbolTable table) {
 		ExpressionNode index = node.getIndexNode();
 		IdentifierNode identifier = node.getIdentifierNode();
 
 		traverse(index, table);
 		traverse(identifier, table);
 
 		if (getTypeDeclaration(identifier) instanceof ArrayType) {
 			ArrayType identifierType = (ArrayType) getTypeDeclaration(identifier);
 			Type t = identifierType.getInnerType();
 			setNodeType(node, t);
 
 			if (getType(index) == Type.Kind.LONG) {
 				Long indexValue = getLongValue(index);
 
 				if (indexValue != null) {
 					if (indexValue < 0) {
 						errorLog.reportError(ReportType.TYPE_MISMATCH, index.coverage(), "Array index can not be negative.");
 						markTypeError(node);
 					} else if (indexValue >= identifierType.getLength()) {
 						errorLog.reportError(ReportType.TYPE_MISMATCH, index.coverage(), "Array index (" + indexValue + ") is out of bound.");
 						markTypeError(node);
 					}
 				}
 			} else {
 				errorLog.reportError(ReportType.TYPE_MISMATCH, index.coverage(), "Array index must be of type long.");
 				markTypeError(node);
 			}
 		} else {
 			errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(), "Array access to non array type.");
 			markTypeError(node);
 		}
 	}
 
 	protected void handleNode(StructIdentifierNode node, SymbolTable table) {
 		IdentifierNode struct = node.getIdentifierNode();
 		traverse(struct, table);
 
 		Type t = getTypeDeclaration(struct);
 
 		if (t instanceof StructType) {
 			StructType st = (StructType) t;
 			Type ft = getStructMemberType(st, node.getFieldName());
 			setAttribute(node, Attribute.TYPE, ft.getKind().name());
 			setTypeDeclaration(node, ft);
 		} else {
 			throw new IllegalArgumentException("Type must be StructType.");
 		}
 	}
 
 	protected void handleNode(ReturnNode node, SymbolTable table) {
 		IdentifierNode identifier = node.getRightValue();
 
 		if (identifier != null) {
 			traverse(identifier, table);
 
 			if (!getAttribute(identifier, Attribute.TYPE).equals(Type.Kind.LONG.name())) {
 				errorLog.reportError(ReportType.TYPE_MISMATCH, node.coverage(),
 					"Only variables of type long can be returned.");
 			}
 		}
 
 		setAttribute(node.getParentNode(), Attribute.CODE_STATE, DEAD_CODE);
 	}
 
 	protected void handleNode(PrintNode node, SymbolTable table) {
 		IdentifierNode identifier = node.getRightValue();
 		traverse(identifier, table);
 	}
 
 	private boolean isInitialized(SymbolTable table, String identifier) {
 		SymbolTable declarationTable = table.getDeclaringSymbolTable(identifier);
 		Set<String> identifiers = this.initializedIdentifiers.get(declarationTable);
 
 		return identifiers != null && identifiers.contains(identifier);
 	}
 
 	protected Type getStructMemberType(StructType s, String field) {
 		for (Member m : s.members()) {
 			if (m.getName().equals(field)) {
 				return m.getType();
 			}
 		}
 
 		return null;
 	}
 
 	protected void setType(ASTNode node, Type.Kind t) {
 		setAttribute(node, Attribute.TYPE, t.name());
 	}
 	
 	protected void setNodeType(ASTNode node, Type t) {
 		setTypeDeclaration(node, t);
 		setAttribute(node, Attribute.TYPE, t.getKind().name());
 	}
 	
 	protected void markTypeError(ASTNode node) {
 		setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 	}
 	
 	protected boolean hasTypeError(ASTNode node) {
 		return hasAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 	}
 
 	protected void setTypeDeclaration(ASTNode node, Type t) {
 		typeDeclarations.put(node, t);
 	}
 
 	protected Type getTypeDeclaration(ASTNode node) {
 		if (!typeDeclarations.containsKey(node)) {
 			throw new IllegalArgumentException("Node has no known type declaration.");
 		}
 
 		return typeDeclarations.get(node);
 	}
 
 	protected void markIdentifierAsInitialized(SymbolTable table, String identifier) {
 		SymbolTable declarationTable = table.getDeclaringSymbolTable(identifier);
 
 		if (!initializedIdentifiers.containsKey(declarationTable)) {
 			initializedIdentifiers.put(declarationTable, new HashSet<String>());
 		}
 
 		initializedIdentifiers.get(declarationTable).add(identifier);
 	}
 
 	protected String getAttribute(ASTNode node, Attribute attribute) {
 		Map<Attribute, String> nodeMap = this.attributes.get(node);
 		if (nodeMap == null) {
 			return NO_ATTRIBUTE_VALUE;
 		}
 		String value = nodeMap.get(attribute);
 		return value == null ? NO_ATTRIBUTE_VALUE : value;
 	}
 
 	protected void setAttribute(ASTNode node, Attribute attribute, String value) {
 		if (!this.attributes.containsKey(node)) {
 			this.attributes.put(node, new HashMap<Attribute, String>());
 		}
 		this.attributes.get(node).put(attribute, value);
 	}
 
 	protected boolean hasAttribute(ASTNode node, Attribute attribute, String value) {
 		return getAttribute(node, attribute).equals(value);
 	}
 }
