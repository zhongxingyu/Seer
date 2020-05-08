 package swp_compiler_ss13.fuc.semantic_analyser;
 
 import java.util.HashMap;
 import java.util.HashSet;
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
 import swp_compiler_ss13.common.ast.nodes.unary.LogicUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode;
 import swp_compiler_ss13.common.parser.ReportLog;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.Type;
 
 public class SemanticAnalyser {
 
 	private static Logger logger = Logger.getLogger(SemanticAnalyser.class);
 
 	enum Attribute {
 
 		/**
 		 * num, basic, array,...
 		 */
 		TYPE,
 		/**
 		 * <code>"1"</code> - identifier is initialized,<br/>
 		 * <code>"0"</code> - identifier is not initialized
 		 *
 		 * @see ExpressionNode
 		 */
 		INITIALIZATION_STATUS,
 		/**
 		 * name of identifier
 		 *
 		 * @see IdentifierNode
 		 */
 		IDENTIFIER,
 		CAN_BREAK,
 		TYPE_CHECK
 	}
 	private static final String IS_NOT_INITIALIZED = "0";
 	private static final String IS_INITIALIZED = "1";
 	private static final String NO_ATTRIBUTE_VALUE = "no Value";
 	private static final String CAN_BREAK = "true";
 	private static final String TYPE_MISMATCH = "type mismatch";
 	private final ReportLog errorLog;
 	private final Map<ASTNode, Map<Attribute, String>> attributes;
 	/**
 	 * Contains all initialized identifiers. As soon it has assigned it will be
 	 * added.
 	 */
 	private final Map<SymbolTable, Set<String>> initializedIdentifiers;
 
 	public SemanticAnalyser(ReportLog log) {
 		this.attributes = new HashMap<>();
 		this.initializedIdentifiers = new HashMap<>();
 		this.errorLog = log;
 	}
 
 	public AST analyse(AST ast) {
 		this.traverseAstNode(ast.getRootNode(), ast.getRootSymbolTable());
 		return ast;
 	}
 
 	protected void traverseAstNode(ASTNode node, SymbolTable table) {
 		switch (node.getNodeType()) {
 			case BasicIdentifierNode:
 				logger.trace("handle BasicIdentifierNode");
 				this.handleNode((BasicIdentifierNode) node, table);
 				break;
 			case BreakNode:
 				this.handleNode((BreakNode) node, table);
 				break;
 			case LiteralNode:
 				logger.trace("handle LiteralNode");
 				this.handleNode((LiteralNode) node, table);
 				break;
 			case ArithmeticUnaryExpressionNode:
 				logger.trace("handle ArithmeticUnaryExpressionNode");
 				this.handleNode((ArithmeticUnaryExpressionNode) node, table);
 				break;
 			case ArrayIdentifierNode:
 				break;
 			case DeclarationNode:
 				break;
 			case LogicUnaryExpressionNode:
 				handleNode((LogicUnaryExpressionNode) node, table);
 				break;
 			case PrintNode:
 				break;
 			case ReturnNode:
 				logger.trace("handle ReturnNode");
 				this.handleNode((ReturnNode) node, table);
 				break;
 			case StructIdentifierNode:
 				break;
 			case ArithmeticBinaryExpressionNode:
 				logger.trace("handle ArithmeticBinaryExpressionNode");
 				this.handleNode((ArithmeticBinaryExpressionNode) node, table);
 				break;
 			case AssignmentNode:
 				logger.trace("handle AssignmentNode");
 				this.handleNode((AssignmentNode) node, table);
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
 				logger.trace("handle BlockNode");
 				this.handleNode((BlockNode) node, table);
 				break;
 			default:
 				throw new IllegalArgumentException("unknown ASTNodeType");
 		}
 	}
 
 	protected Type.Kind getType(ASTNode node) {
 		return Type.Kind.valueOf(getAttribute(node, Attribute.TYPE));
 	}
 
 	protected Type.Kind typeLeastUpperBound(ASTNode left, ASTNode right) {
 		TreeSet<Type.Kind> types = new TreeSet<>();
 		types.add(Type.Kind.valueOf(getAttribute(left, Attribute.TYPE)));
 		types.add(Type.Kind.valueOf(getAttribute(right, Attribute.TYPE)));
 
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
 		if (!hasAttribute(node.getCondition(), Attribute.TYPE, Type.Kind.BOOLEAN.name())) {
 			errorLog.reportError("The condition must be of type bool.", -1, -1, "TypeError");
 		}
 	}
 
 	protected void handleNode(DoWhileNode node, SymbolTable table) {
 		setAttribute(node, Attribute.CAN_BREAK, CAN_BREAK);
 		traverseAstNode(node.getCondition(), table);
 		traverseAstNode(node.getLoopBody(), table);
 
 		checkLoopNode(node, table);
 	}
 
 	protected void handleNode(WhileNode node, SymbolTable table) {
 		setAttribute(node, Attribute.CAN_BREAK, CAN_BREAK);
 		traverseAstNode(node.getCondition(), table);
 		traverseAstNode(node.getLoopBody(), table);
 
 		checkLoopNode(node, table);
 	}
 
 	protected void handleNode(BreakNode node, SymbolTable table) {
 		if (!hasAttribute(node.getParentNode(), Attribute.CAN_BREAK, CAN_BREAK)) {
 			errorLog.reportError("Break can only be used in a loop.", -1, -1, "BreakOutsideLoop");
 		}
 	}
 
 	/*
 	 * Branch node
 	 */
 	protected void handleNode(BranchNode node, SymbolTable table) {
 		traverseAstNode(node.getCondition(), table);
 		traverseAstNode(node.getStatementNodeOnTrue(), table);
		traverseAstNode(node.getStatementNodeOnFalse(), table);
 
 		if (!hasAttribute(node.getCondition(), Attribute.TYPE, Type.Kind.BOOLEAN.name())) {
 			errorLog.reportError("The condition must be of type bool.", -1, -1, "TypeError");
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
 		traverseAstNode(left, table);
 
 		/*
 		 * right sub tree
 		 */
 		ExpressionNode right = node.getRightValue();
 		traverseAstNode(right, table);
 
 		Type.Kind type = typeLeastUpperBound(left, right);
 
 		if (hasAttribute(left, Attribute.TYPE_CHECK, TYPE_MISMATCH)
 			|| hasAttribute(right, Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 		} else {
 			if (type == null) {
 				setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 				errorLog.reportError("Types can not be combined, no LUB.", -1, -1, "TypeError");
 			} else {
 				setAttribute(node, Attribute.TYPE, type.name());
 			}
 		}
 	}
 	
 	protected void unaryExpression(UnaryExpressionNode node, SymbolTable table) {
 		ExpressionNode expression = node.getRightValue();
 		traverseAstNode(expression, table);
 
 		if (hasAttribute(expression, Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 		} else {
 			setAttribute(node, Attribute.TYPE, getAttribute(expression, Attribute.TYPE));
 		}
 	}
 
 	/*
 	 * Arithmetic
 	 */
 	protected void handleNode(ArithmeticBinaryExpressionNode node, SymbolTable table) {
 		binaryExpression(node, table);
 		
 		if (!hasAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			Type.Kind type = getType(node);
 
 			if (!isNumeric(type) || (type == Type.Kind.STRING && node.getOperator() == BinaryExpressionNode.BinaryOperator.ADDITION)) {
 				setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 				errorLog.reportError("Operator is not defined for those types", -1, -1, "TypeError");
 			}
 		}
 	}
 
 	protected void handleNode(ArithmeticUnaryExpressionNode node, SymbolTable table) {
 		unaryExpression(node, table);
 
 		if (hasAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			if (!isNumeric(getType(node))) {
 				setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 				errorLog.reportError("Operation is not defined for ...", -1, -1, "TypeError");
 			}
 		}
 	}
 
 	protected void handleNode(RelationExpressionNode node, SymbolTable table) {
 		binaryExpression(node, table);
 		
 		if (!hasAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			Type.Kind type = getType(node);
 
 			if (!isNumeric(type)) {
 				setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 				errorLog.reportError("Operator expects numeric operands.", -1, -1, "TypeError");
 			}
 		}
 	}
 	
 	
 	/*
 	 * Bool
 	 */
 	protected void handleNode(LogicBinaryExpressionNode node, SymbolTable table) {
 		binaryExpression(node, table);
 		
 		if (!hasAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			if (!isBool(getType(node))) {
 				setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 				errorLog.reportError("Operator expects boolean operands.", -1, -1, "TypeError");
 			}
 		}
 	}
 
 	protected void handleNode(LogicUnaryExpressionNode node, SymbolTable table) {
 		unaryExpression(node, table);
 
 		if (!hasAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			if (!isBool(getType(node))) {
 				setAttribute(node, Attribute.TYPE_CHECK, TYPE_MISMATCH);
 				errorLog.reportError("Operator expects boolean operand.", -1, -1, "TypeError");
 			}
 		}
 	}
 	
 	
 	/*
 	 * Block
 	 */
 	protected void handleNode(BlockNode node, SymbolTable table) {
 		if (hasAttribute(node.getParentNode(), Attribute.CAN_BREAK, CAN_BREAK)) {
 			setAttribute(node, Attribute.CAN_BREAK, CAN_BREAK);
 		}
 
 		SymbolTable blockScope = node.getSymbolTable();
 
 		for (StatementNode child : node.getStatementList()) {
 			this.traverseAstNode(child, blockScope);
 		}
 	}
 
 	protected void handleNode(AssignmentNode node, SymbolTable table) {
 		traverseAstNode(node.getLeftValue(), table);
 		traverseAstNode(node.getRightValue(), table);
 		
 		if (hasAttribute(node.getLeftValue(), Attribute.TYPE_CHECK, TYPE_MISMATCH) ||
 			hasAttribute(node.getRightValue(), Attribute.TYPE_CHECK, TYPE_MISMATCH)) {
 			setAttribute(node, Attribute.TYPE, TYPE_MISMATCH);
 		} else if (getType(node.getLeftValue()) != getType(node.getRightValue())) {
 			setAttribute(node, Attribute.TYPE, TYPE_MISMATCH);
 			errorLog.reportError("Expected " + getType(node.getLeftValue()).name() +
 				" found " + getType(node.getRightValue()).name(), -1, -1, "TypeError");
 		} else {
 			markIdentifierAsInitialized(table, getAttribute(node.getLeftValue(), Attribute.IDENTIFIER));
 		}
 	}
 
 	/*
 	 * Leaf nodes
 	 */
 	protected void handleNode(LiteralNode node, SymbolTable table) {
 		setAttribute(node, Attribute.INITIALIZATION_STATUS, IS_INITIALIZED);
 		setAttribute(node, Attribute.TYPE, node.getLiteralType().getKind().name());
 	}
 
 	protected void handleNode(BasicIdentifierNode node, SymbolTable table) {
 		String identifier = node.getIdentifier();
 		boolean initialzed = isInitialized(table, identifier);
 
 		setAttribute(node, Attribute.IDENTIFIER, identifier);
 		setAttribute(node, Attribute.TYPE, table.lookupType(node.getIdentifier()).getKind().name());
 
 		/*
 		 * checks
 		 */
 		if (node.getParentNode().getNodeType() != ASTNode.ASTNodeType.AssignmentNode && !initialzed) {
 			System.out.println("No init!");
 		}
 	}
 
 	protected void handleNode(ReturnNode node, SymbolTable table) {
 		IdentifierNode identifier = node.getRightValue();
 
 		if (identifier != null) {
 			traverseAstNode(identifier, table);
 
 			if (!getAttribute(identifier, Attribute.TYPE).equals(Type.Kind.LONG.name())) {
 				errorLog.reportError("Only variables of type long can be returned.", -1, -1, "TypeError");
 			}
 		}
 	}
 
 	private boolean isInitialized(SymbolTable table, String identifier) {
 		SymbolTable declarationTable = table.getDeclaringSymbolTable(identifier);
 		Set<String> identifiers = this.initializedIdentifiers.get(declarationTable);
 
 		return identifiers != null && identifiers.contains(identifier);
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
 		if (this.attributes.get(node) == null) {
 			this.attributes.put(node, new HashMap<Attribute, String>());
 		}
 		this.attributes.get(node).put(attribute, value);
 	}
 
 	protected boolean hasAttribute(ASTNode node, Attribute attribute, String value) {
 		return getAttribute(node, attribute).equals(value);
 	}
 }
