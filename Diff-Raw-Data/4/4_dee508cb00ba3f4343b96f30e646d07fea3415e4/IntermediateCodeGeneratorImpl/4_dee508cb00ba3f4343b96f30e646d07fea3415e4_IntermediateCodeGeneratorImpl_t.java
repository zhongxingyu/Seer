 package swp_compiler_ss13.fuc.ir;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.ast.AST;
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.IdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.StatementNode;
 import swp_compiler_ss13.common.ast.nodes.binary.ArithmeticBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
 import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
 import swp_compiler_ss13.common.ast.nodes.binary.DoWhileNode;
 import swp_compiler_ss13.common.ast.nodes.binary.LogicBinaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.RelationExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.WhileNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BreakNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
 import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
 import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArithmeticUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ArrayIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
 import swp_compiler_ss13.common.ast.nodes.unary.LogicUnaryExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.unary.PrintNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 import swp_compiler_ss13.common.ast.nodes.unary.StructIdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode.UnaryOperator;
 import swp_compiler_ss13.common.backend.Quadruple;
 import swp_compiler_ss13.common.backend.Quadruple.Operator;
 import swp_compiler_ss13.common.ir.IntermediateCodeGenerator;
 import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.Type;
 import swp_compiler_ss13.common.types.Type.Kind;
 import swp_compiler_ss13.common.types.derived.ArrayType;
 import swp_compiler_ss13.common.types.primitive.BooleanType;
 import swp_compiler_ss13.common.types.primitive.DoubleType;
 import swp_compiler_ss13.common.types.primitive.LongType;
 import swp_compiler_ss13.common.types.primitive.StringType;
 
 /**
  * Create the intermediate code representation for the given AST
  * 
  * @author "Frank Zechert"
  * @version 1
  */
 public class IntermediateCodeGeneratorImpl implements IntermediateCodeGenerator {
 
 	/**
 	 * The Log4J Logger
 	 */
 	private static Logger logger = Logger.getLogger(IntermediateCodeGeneratorImpl.class);
 
 	/**
 	 * Number of the next free label
 	 */
 	private static long labelNum = 0L;
 
 	/**
 	 * The generated intermediate code
 	 */
 	private List<Quadruple> irCode;
 
 	/**
 	 * List of used names. This is needed for single static assignment.
 	 */
 	private List<String> usedNames;
 
 	/**
 	 * The stack of symbol tables
 	 */
 	private Stack<SymbolTable> currentSymbolTable;
 
 	/**
 	 * Store for intermediate results
 	 */
 	private Stack<IntermediateResult> intermediateResults;
 
 	/**
 	 * is used to store the level of the outermost array index
 	 */
 	Integer arrayLevel = null;
 
 	/**
 	 * when processing an arrayIdentifier it needs to be known if it is used in
 	 * an assignment or referenced
 	 */
 	Boolean arrayAssignment = false;
 
 	/**
 	 * Reset the intermediate code generator. This is called first for every
 	 * generateIntermediateCode() to ensure that severeal calls to
 	 * generateIntermediateCode do not interfere.
 	 */
 	private void reset() {
 
 		logger.trace("Resetting the intermediate code generator.");
 
 		this.irCode = new LinkedList<>();
 		this.usedNames = new LinkedList<>();
 		this.currentSymbolTable = new Stack<>();
 		this.intermediateResults = new Stack<>();
 	}
 
 	@Override
 	public List<Quadruple> generateIntermediateCode(AST ast) throws IntermediateCodeGeneratorException {
 		if (ast == null) {
 			logger.fatal("The argument ast in generateIntermediateCode(AST ast) can not be null!");
 			throw new IntermediateCodeGeneratorException(
 					"The argument ast in generateIntermediateCode(AST ast) can not be null!");
 		}
 
 		ASTNode program = ast.getRootNode();
 		if (program == null) {
 			logger.fatal("The ast given in generateIntermediateCode(AST ast) must have a root node!");
 			throw new IntermediateCodeGeneratorException(
 					"The ast given in generateIntermediateCode(AST ast) must have a root node");
 		}
 		this.reset();
 		this.callProcessing(program);
 		return this.irCode;
 	}
 
 	/**
 	 * call the method that handles the node in the AST
 	 * 
 	 * @param node
 	 *            The node to handle
 	 * @throws IntermediateCodeGeneratorException
 	 *             An error occurred
 	 */
 	private void callProcessing(ASTNode node) throws IntermediateCodeGeneratorException {
 		logger.debug("Processing next node: " + node.getNodeType().toString() + " with "
 				+ (node.getNumberOfNodes() - 1) + " subnodes");
 		switch (node.getNodeType()) {
 		case ArithmeticBinaryExpressionNode:
 			this.processArithmeticBinaryExpressionNode((ArithmeticBinaryExpressionNode) node);
 			break;
 		case ArithmeticUnaryExpressionNode:
 			this.processArithmeticUnaryExpressionNode((ArithmeticUnaryExpressionNode) node);
 			break;
 		case ArrayIdentifierNode:
 			this.processArrayIdentifierNode((ArrayIdentifierNode) node);
 			break;
 		case AssignmentNode:
 			this.processAssignmentNode((AssignmentNode) node);
 			break;
 		case BasicIdentifierNode:
 			this.processBasicIdentifierNode((BasicIdentifierNode) node);
 			break;
 		case BlockNode:
 			this.processBlockNode((BlockNode) node);
 			break;
 		case BranchNode:
 			this.processBranchNode((BranchNode) node);
 			break;
 		case BreakNode:
 			this.processBreakNode((BreakNode) node);
 			break;
 		case DeclarationNode:
 			this.processDeclarationNode((DeclarationNode) node);
 			break;
 		case DoWhileNode:
 			this.processDoWhileNode((DoWhileNode) node);
 			break;
 		case LiteralNode:
 			this.processLiteralNode((LiteralNode) node);
 			break;
 		case LogicBinaryExpressionNode:
 			this.processLogicBinaryExpressionNode((LogicBinaryExpressionNode) node);
 			break;
 		case LogicUnaryExpressionNode:
 			this.processLogicUnaryExpressionNode((LogicUnaryExpressionNode) node);
 			break;
 		case PrintNode:
 			this.processPrintNode((PrintNode) node);
 			break;
 		case RelationExpressionNode:
 			this.processRelationExpressionNode((RelationExpressionNode) node);
 			break;
 		case ReturnNode:
 			this.processReturnNode((ReturnNode) node);
 			break;
 		case StructIdentifierNode:
 			this.processStructIdentifierNode((StructIdentifierNode) node);
 			break;
 		case WhileNode:
 			this.processWhileNode((WhileNode) node);
 			break;
 		default:
 			throw new IntermediateCodeGeneratorException("Unknown node type: " + node.getNodeType().toString());
 		}
 	}
 
 	/**
 	 * Process a block node and generate the needed IR Code
 	 * 
 	 * @param node
 	 *            The block node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             an error occurred while process the node
 	 */
 	private void processBlockNode(BlockNode node) throws IntermediateCodeGeneratorException {
 		// push current symbol table
 		this.currentSymbolTable.push(node.getSymbolTable());
 
 		// get declarations
 		Iterator<DeclarationNode> declIterator = node.getDeclarationIterator();
 		while (declIterator.hasNext()) {
 			this.callProcessing(declIterator.next());
 		}
 
 		// get statements
 		Iterator<StatementNode> statementIterator = node.getStatementIterator();
 		while (statementIterator.hasNext()) {
 			StatementNode statement = statementIterator.next();
 			this.callProcessing(statement);
 		}
 
 		// pop the symbol scope
 		this.currentSymbolTable.pop();
 	}
 
 	/**
 	 * Process a while node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processWhileNode(WhileNode node) throws IntermediateCodeGeneratorException {
 		throw new IntermediateCodeGeneratorException(new UnsupportedOperationException());
 	}
 
 	/**
 	 * Process a structidentifier node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processStructIdentifierNode(StructIdentifierNode node) throws IntermediateCodeGeneratorException {
 		throw new IntermediateCodeGeneratorException(new UnsupportedOperationException());
 	}
 
 	/**
 	 * Process a return node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processReturnNode(ReturnNode node) throws IntermediateCodeGeneratorException {
 		IdentifierNode right = node.getRightValue();
 		this.callProcessing(right);
 		IntermediateResult intermediateResult = this.intermediateResults.pop();
 		this.irCode.add(QuadrupleFactory.returnNode(intermediateResult.getValue()));
 	}
 
 	/**
 	 * Process a relationexpression node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processRelationExpressionNode(RelationExpressionNode node) throws IntermediateCodeGeneratorException {
 		BinaryOperator operator = node.getOperator();
 
 		ExpressionNode leftNode = node.getLeftValue();
 		ExpressionNode rightNode = node.getRightValue();
 
 		this.callProcessing(leftNode);
 		this.callProcessing(rightNode);
 
 		IntermediateResult rightResult = this.intermediateResults.pop();
 		IntermediateResult leftResult = this.intermediateResults.pop();
 
 		boolean castNeeded = CastingFactory.isCastNeeded(leftResult, rightResult);
 		String castedleft = leftResult.getValue();
 		String castedright = rightResult.getValue();
 		Type type = leftResult.getType();
 		if (castNeeded) {
 			if (CastingFactory.isNumeric(leftResult) && CastingFactory.isNumeric(rightResult)) {
 				type = new DoubleType();
 				if (leftResult.getType().getKind() == Kind.LONG) {
 					castedleft = this.createAndSaveTemporaryIdentifier(type);
 					this.irCode.add(CastingFactory.createCast(leftResult.getType(), leftResult.getValue(), type,
 							castedleft));
 				}
 				if (rightResult.getType().getKind() == Kind.LONG) {
 					this.irCode.add(CastingFactory.createCast(rightResult.getType(), rightResult.getValue(), type,
 							castedright));
 				}
 			} else {
 				String err = String.format("unsupported types %s %s and %s %s for relation expression",
 						leftResult.getType(), leftResult.getValue(), rightResult.getType(), rightResult.getValue());
 				logger.fatal(err);
 				throw new IntermediateCodeGeneratorException(err);
 			}
 		}
 
 		String result = this.createAndSaveTemporaryIdentifier(type);
 
 		switch (operator) {
 		case EQUAL:
 			this.irCode.add(QuadrupleFactory.relationEqual(castedleft, castedright, result, type));
 			break;
 		case GREATERTHAN:
 			this.irCode.add(QuadrupleFactory.relationGreater(castedleft, castedright, result, type));
 			break;
 		case GREATERTHANEQUAL:
 			this.irCode.add(QuadrupleFactory.relationGreaterEqual(castedleft, castedright, result, type));
 			break;
 		case INEQUAL:
 			String tmp = this.createAndSaveTemporaryIdentifier(new BooleanType());
			this.irCode.add(QuadrupleFactory.relationEqual(castedleft, castedright, tmp, type));
			this.irCode.add(QuadrupleFactory.booleanArithmetic(UnaryOperator.LOGICAL_NEGATE, tmp, result));
 			break;
 		case LESSTHAN:
 			this.irCode.add(QuadrupleFactory.relationLess(castedleft, castedright, result, type));
 			break;
 		case LESSTHANEQUAL:
 			this.irCode.add(QuadrupleFactory.relationLessEqual(castedleft, castedright, result, type));
 			break;
 		case ADDITION:
 		case DIVISION:
 		case LOGICAL_AND:
 		case LOGICAL_OR:
 		case MULTIPLICATION:
 		case SUBSTRACTION:
 		default:
 			String err = "BinaryOperator %s is not allowed within a RelationExpressionNode";
 			String errf = String.format(err, operator);
 			logger.fatal(errf);
 			throw new IntermediateCodeGeneratorException(errf);
 		}
 
 		this.intermediateResults.push(new IntermediateResult(result, type));
 	}
 
 	/**
 	 * Process a print node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processPrintNode(PrintNode node) throws IntermediateCodeGeneratorException {
 		this.callProcessing(node.getRightValue());
 		IntermediateResult result = this.intermediateResults.pop();
 		this.irCode.add(QuadrupleFactory.print(result.getValue(), result.getType()));
 	}
 
 	/**
 	 * Process a logicunaryexpression node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processLogicUnaryExpressionNode(LogicUnaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 		this.callProcessing(node.getRightValue());
 		IntermediateResult result = this.intermediateResults.pop();
 
 		if (result.getType().getKind() != Kind.BOOLEAN) {
 			String err = "Unary logic operation NOT is not supported for type " + result.getType();
 			logger.fatal(err);
 			throw new IntermediateCodeGeneratorException(err);
 		}
 
 		String temp = this.createAndSaveTemporaryIdentifier(new BooleanType());
 		this.irCode.add(QuadrupleFactory.unaryNot(result.getValue(), temp));
 		this.intermediateResults.push(new IntermediateResult(temp, new BooleanType()));
 	}
 
 	/**
 	 * Process a logicbinaryexpression node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processLogicBinaryExpressionNode(LogicBinaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 		this.callProcessing(node.getLeftValue());
 		this.callProcessing(node.getRightValue());
 
 		IntermediateResult rightResult = this.intermediateResults.pop();
 		IntermediateResult leftResult = this.intermediateResults.pop();
 
 		if (leftResult.getType().getKind() != Kind.BOOLEAN || rightResult.getType().getKind() != Kind.BOOLEAN) {
 			String err = "Binary logic operation is not supported for " + leftResult.getType() + " and "
 					+ rightResult.getType();
 			logger.fatal(err);
 			throw new IntermediateCodeGeneratorException(err);
 		}
 
 		String temp = this.createAndSaveTemporaryIdentifier(new BooleanType());
 		this.irCode.add(QuadrupleFactory.booleanArithmetic(node.getOperator(), leftResult.getValue(),
 				rightResult.getValue(), temp));
 		this.intermediateResults.push(new IntermediateResult(temp, new BooleanType()));
 	}
 
 	/**
 	 * Process a literal node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processLiteralNode(LiteralNode node) throws IntermediateCodeGeneratorException {
 		String literal = node.getLiteral();
 		Type type = node.getLiteralType();
 		switch (type.getKind()) {
 		case DOUBLE:
 		case LONG:
 		case BOOLEAN:
 			// Literal of type Long or Double needs to start with a # to mark it
 			// as a constant
 			this.intermediateResults.push(new IntermediateResult("#" + literal, type));
 			break;
 		case STRING:
 			// Literal of type String needs to be in " and start with a #
 			// Replace all " in the string with \"
 			this.intermediateResults
 					.push(new IntermediateResult("#\"" + literal.replaceAll("\"", "\\\"") + "\"", type));
 			break;
 		default:
 			// Literal of other types are not defined yet.
 			throw new IntermediateCodeGeneratorException("Literal node of type " + node.getLiteralType().toString()
 					+ " is not supported");
 		}
 	}
 
 	/**
 	 * Process a relationexpression node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processDoWhileNode(DoWhileNode node) throws IntermediateCodeGeneratorException {
 		throw new IntermediateCodeGeneratorException(new UnsupportedOperationException());
 	}
 
 	/**
 	 * Process a declaration node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processDeclarationNode(DeclarationNode node) throws IntermediateCodeGeneratorException {
 		String identifierName = node.getIdentifier();
 		Type identifierType = node.getType();
 		// save the new declared variable into our structures
 		this.saveIdentifier(identifierName, identifierType);
 	}
 
 	/**
 	 * Process a break node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processBreakNode(BreakNode node) throws IntermediateCodeGeneratorException {
 		throw new IntermediateCodeGeneratorException(new UnsupportedOperationException());
 	}
 
 	/**
 	 * Process a branch node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processBranchNode(BranchNode node) throws IntermediateCodeGeneratorException {
 		ExpressionNode condition = node.getCondition();
 		StatementNode onTrue = node.getStatementNodeOnTrue();
 		StatementNode onFalse = node.getStatementNodeOnFalse();
 
 		this.callProcessing(condition);
 		IntermediateResult conditionResult = this.intermediateResults.pop();
 		if (conditionResult.getType().getKind() != Kind.BOOLEAN) {
 			String err = "A condition is not of type boolean but of unsupported type " + conditionResult.getType();
 			logger.fatal(err);
 			throw new IntermediateCodeGeneratorException(err);
 		}
 
 		String trueLabel = this.createNewLabel();
 		String falseLabel = this.createNewLabel();
 		String endLabel = this.createNewLabel();
 
 		this.irCode.add(QuadrupleFactory.branch(conditionResult.getValue(), trueLabel, falseLabel));
 		this.irCode.add(QuadrupleFactory.label(trueLabel));
 		this.callProcessing(onTrue);
 		this.irCode.add(QuadrupleFactory.jump(endLabel));
 		this.irCode.add(QuadrupleFactory.label(falseLabel));
 
 		if (onFalse != null) {
 			this.callProcessing(onFalse);
 		}
 
 		this.irCode.add(QuadrupleFactory.label(endLabel));
 
 	}
 
 	/**
 	 * Process a basicidentifier node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processBasicIdentifierNode(BasicIdentifierNode node) throws IntermediateCodeGeneratorException {
 		// A basic identifier can be pushed to the stack of results immediately
 		String identifier = node.getIdentifier();
 		Type identifierType = this.currentSymbolTable.peek().lookupType(identifier);
 
 		// arrays get assigned with their full type but we want the base type in
 		// the center of it
 		while (identifierType instanceof ArrayType) {
 			identifierType = ((ArrayType) identifierType).getInnerType();
 		}
 
 		String actualIdentifier = this.loadIdentifier(identifier);
 		this.intermediateResults.push(new IntermediateResult(actualIdentifier, identifierType));
 	}
 
 	/**
 	 * Process a assignment node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processAssignmentNode(AssignmentNode node) throws IntermediateCodeGeneratorException {
 		// the id to assign the value to
 		IdentifierNode id = node.getLeftValue();
 
 		this.arrayAssignment = true;
 		this.arrayLevel = null;
 		this.callProcessing(id);
 		Integer toArrayIndex = this.arrayLevel;
 		this.arrayLevel = null;
 
 		IntermediateResult toIntermediate = this.intermediateResults.pop();
 
 		// the id to assign the value to is a basic identifier.
 		// no need to resolve an array or struct etc.
 		StatementNode value = node.getRightValue();
 
 		// process the right hand expression
 		this.callProcessing(value);
 		Integer fromArrayIndex = this.arrayLevel;
 		this.arrayLevel = null;
 		this.arrayAssignment = false;
 
 		// the result of the right hand expression
 		IntermediateResult fromIntermediate = this.intermediateResults.pop();
 
 		// get the name of the id and resolve it from our saved structures
 		String toId = toIntermediate.getValue();
 
 		// the type of the id. If necessary the value of the right hand
 		// expression
 		// needs to be casted to this type.
 		Type toType = toIntermediate.getType();
 
 		// check if the cast is needed
 		boolean castNeeded = CastingFactory.isCastNeeded(toType, fromIntermediate.getType());
 
 		// get the name and id of the source
 		String fromId = fromIntermediate.getValue();
 		Type fromType = fromIntermediate.getType();
 
 		if (fromArrayIndex != null && toArrayIndex != null) {
 			// assign array to array
 			String temporary = this.createAndSaveTemporaryIdentifier(fromType);
 			this.irCode.addAll(QuadrupleFactory.assign(fromType, fromId, temporary, fromArrayIndex, null));
 			if (castNeeded) {
 				String temporary2 = this.createAndSaveTemporaryIdentifier(toType);
 				this.irCode.add(CastingFactory.createCast(fromType, temporary, toType, temporary2));
 				this.irCode.addAll(QuadrupleFactory.assign(toType, temporary2, toId, null, toArrayIndex));
 				toId = temporary2;
 			} else {
 				this.irCode.addAll(QuadrupleFactory.assign(toType, temporary, toId, null, toArrayIndex));
 				toId = temporary;
 			}
 		} else {
 			if (castNeeded) {
 				if (toArrayIndex == null) {
 					// assign array to variable
 					String temporary = this.createAndSaveTemporaryIdentifier(fromType);
 					this.irCode.addAll(QuadrupleFactory.assign(fromType, fromId, temporary, fromArrayIndex,
 							toArrayIndex));
 					this.irCode.add(CastingFactory.createCast(fromType, temporary, toType, toId));
 					toId = temporary;
 				} else {
 					// assign variable to array
 					String temporary = this.createAndSaveTemporaryIdentifier(toType);
 					this.irCode.add(CastingFactory.createCast(fromType, fromId, toType, temporary));
 					this.irCode.addAll(QuadrupleFactory.assign(toType, temporary, toId, fromArrayIndex, toArrayIndex));
 					toId = temporary;
 				}
 			} else {
 				this.irCode.addAll(QuadrupleFactory.assign(toType, fromId, toId, fromArrayIndex, toArrayIndex));
 			}
 		}
 		this.intermediateResults.push(new IntermediateResult(toId, toType));
 	}
 
 	/**
 	 * Process a arrayidentifier node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processArrayIdentifierNode(ArrayIdentifierNode node) throws IntermediateCodeGeneratorException {
 
 		IdentifierNode innerNode = node.getIdentifierNode();
 
 		boolean outerArray = false;
 		if (this.arrayLevel == null) {
 			this.arrayLevel = node.getIndex();
 			outerArray = true;
 		}
 
 		this.callProcessing(innerNode);
 
 		// array assignments are special so don't push the reference of the
 		// outermost array
 		if (!this.arrayAssignment || !outerArray) {
 			IntermediateResult innerResult = this.intermediateResults.pop();
 			Type nodeType = innerResult.getType();
 			String tmpIdentifier;
 
 			if (outerArray) {
 				tmpIdentifier = this.createAndSaveTemporaryIdentifier(nodeType);
 			} else {
 				tmpIdentifier = this.createAndSaveTemporaryIdentifierReference(nodeType);
 			}
 			this.currentSymbolTable.peek().lookupType(tmpIdentifier);
 
 			// FIXME just looks at the outermost type so this works
 			Type arrayType = new ArrayType(new StringType(0L), 0);
 			if (outerArray) {
 				arrayType = nodeType;
 			}
 			this.irCode.add(QuadrupleFactory.arrayGet(arrayType, innerResult.getValue(), "#" + node.getIndex(),
 					tmpIdentifier));
 			this.intermediateResults.push(new IntermediateResult(tmpIdentifier, innerResult.getType()));
 		}
 	}
 
 	/**
 	 * Process a arithmeticunaryexpression node
 	 * 
 	 * @param node
 	 *            the node to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             Something went wrong
 	 */
 	private void processArithmeticUnaryExpressionNode(ArithmeticUnaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 		if (node.getOperator() != UnaryOperator.MINUS) {
 			// No unary operations except for minus are supported by arithmetic
 			// nodes
 			throw new IntermediateCodeGeneratorException("Unsupported arithmetic unary operator");
 		}
 		ExpressionNode rightNode = node.getRightValue();
 
 		// process the right hand value
 		this.callProcessing(rightNode);
 
 		IntermediateResult rightIntermediate = this.intermediateResults.pop();
 
 		String temp = this.createAndSaveTemporaryIdentifier(rightIntermediate.getType());
 		this.irCode.add(QuadrupleFactory.unaryMinus(rightIntermediate.getType(), rightIntermediate.getValue(), temp));
 		this.intermediateResults.push(new IntermediateResult(temp, rightIntermediate.getType()));
 	}
 
 	/**
 	 * Binary Arithmetic Operation
 	 * 
 	 * @param node
 	 *            the arithmetic binary expression node
 	 * @throws IntermediateCodeGeneratorException
 	 *             something went wrong
 	 */
 	private void processArithmeticBinaryExpressionNode(ArithmeticBinaryExpressionNode node)
 			throws IntermediateCodeGeneratorException {
 
 		// process the left and right value first
 		this.callProcessing(node.getLeftValue());
 		this.callProcessing(node.getRightValue());
 
 		// get the left and right value
 		IntermediateResult right = this.intermediateResults.pop();
 		IntermediateResult left = this.intermediateResults.pop();
 
 		// check if a cast is needed
 		boolean castNeeded = CastingFactory.isCastNeeded(right, left);
 
 		if (!castNeeded) {
 			// no cast is needed
 			// either both values are of type LONG or of type DOUBLE
 			if (left.getType() instanceof LongType) {
 				// both values are of type LONG
 				String temp = this.createAndSaveTemporaryIdentifier(new LongType());
 				this.irCode.add(QuadrupleFactory.longArithmeticBinaryOperation(node.getOperator(), left.getValue(),
 						right.getValue(), temp));
 
 				this.intermediateResults.push(new IntermediateResult(temp, new LongType()));
 			} else if (left.getType() instanceof DoubleType) {
 				// both values are of type DOUBLE
 				String temp = this.createAndSaveTemporaryIdentifier(new DoubleType());
 				this.irCode.add(QuadrupleFactory.longArithmeticBinaryOperation(node.getOperator(), left.getValue(),
 						right.getValue(), temp));
 
 				this.intermediateResults.push(new IntermediateResult(temp, new DoubleType()));
 			} else {
 				// this is an unsupported combination of types
 				String err = "Arithmetic Binary Expression with arguments of types %s and %s is not supported";
 				String errf = String.format(err, left.getType().toString(), right.getType().toString());
 				logger.fatal(errf);
 				throw new IntermediateCodeGeneratorException(errf, new UnsupportedOperationException());
 			}
 		} else {
 			// A cast is needed.
 			// Only LONG and DOUBLE types are valid for arithmetic operations
 			// We will always cast to type DOUBLE as it is more precise than
 			// LONG
 			// and nothing will be lost during the conversion
 			if (left.getType() instanceof LongType) {
 				// the left value is of type LONG, so it needs to be casted
 				String temp = this.createAndSaveTemporaryIdentifier(new DoubleType());
 				String temp2 = this.createAndSaveTemporaryIdentifier(new DoubleType());
 				this.irCode.add(CastingFactory.createCast(left.getType(), left.getValue(), new DoubleType(), temp));
 				this.irCode.add(QuadrupleFactory.doubleArithmeticBinaryOperation(node.getOperator(), temp,
 						right.getValue(), temp2));
 
 				this.intermediateResults.push(new IntermediateResult(temp2, new DoubleType()));
 			} else if (right.getType() instanceof LongType) {
 				// the right value is of type LONG, so it needs to be casted
 				String temp = this.createAndSaveTemporaryIdentifier(new DoubleType());
 				String temp2 = this.createAndSaveTemporaryIdentifier(new DoubleType());
 				this.irCode.add(CastingFactory.createCast(right.getType(), right.getValue(), new DoubleType(), temp));
 				this.irCode.add(QuadrupleFactory.doubleArithmeticBinaryOperation(node.getOperator(), left.getValue(),
 						temp, temp2));
 
 				this.intermediateResults.push(new IntermediateResult(temp2, new DoubleType()));
 			} else {
 				// this combinations of types is not supported
 				String err = "Arithmetic Binary Expression with arguments of types %s and %s is not supported";
 				String errf = String.format(err, left.getType().toString(), right.getType().toString());
 				logger.fatal(errf);
 				throw new IntermediateCodeGeneratorException(errf, new UnsupportedOperationException());
 			}
 		}
 
 	}
 
 	/**
 	 * Save the given identifier to the interal store of variables
 	 * 
 	 * @param identifier
 	 *            The name of the variable
 	 * @param type
 	 *            the type of the variable
 	 * @return The renamed name of the variable that was stored
 	 * @throws IntermediateCodeGeneratorException
 	 *             an exception occurred
 	 */
 	private String saveIdentifier(String identifier, Type type) throws IntermediateCodeGeneratorException {
 		if (!this.usedNames.contains(identifier)) {
 			// an identifier with this name was not yet used.
 			// it does not need to be renamed to stick to SSA
 			this.usedNames.add(identifier);
 			this.irCode.addAll(QuadrupleFactory.declaration(identifier, type));
 			return identifier;
 		} else {
 			// rename is required to keep single static assignment
 			String newName = this.currentSymbolTable.peek().getNextFreeTemporary();
 			this.currentSymbolTable.peek().putTemporary(newName, type);
 			this.usedNames.add(newName);
 			this.currentSymbolTable.peek().setIdentifierAlias(identifier, newName);
 			this.irCode.addAll(QuadrupleFactory.declaration(newName, type));
 			return newName;
 		}
 	}
 
 	/**
 	 * Create a new temporary value and save it to the internal store of
 	 * variables
 	 * 
 	 * @param type
 	 *            The type of the new variable
 	 * @return The name of the new variable
 	 * @throws IntermediateCodeGeneratorException
 	 *             An error occurred
 	 */
 	private String createAndSaveTemporaryIdentifier(Type type) throws IntermediateCodeGeneratorException {
 		// Condition: getNextFreeTemporary never returns a name that was already
 		// used in the IR until now
 		// (globally unique names)
 		String id = this.currentSymbolTable.peek().getNextFreeTemporary();
 		this.currentSymbolTable.peek().putTemporary(id, type);
 		this.usedNames.add(id);
 		this.irCode.addAll(QuadrupleFactory.declaration(id, type));
 		return id;
 	}
 
 	/**
 	 * Create a new temporary variable reference and save it to the internal
 	 * store of variables
 	 * 
 	 * @param type
 	 *            The type of the new variable reference
 	 * @return The name of the new variable
 	 * @throws IntermediateCodeGeneratorException
 	 *             An error occurred
 	 */
 	private String createAndSaveTemporaryIdentifierReference(Type type) throws IntermediateCodeGeneratorException {
 		String id = this.currentSymbolTable.peek().getNextFreeTemporary();
 		this.currentSymbolTable.peek().putTemporary(id, type);
 		this.usedNames.add(id);
 		this.irCode.add(new QuadrupleImpl(Operator.DECLARE_REFERENCE, Quadruple.EmptyArgument, Quadruple.EmptyArgument,
 				id));
 		return id;
 	}
 
 	/**
 	 * Load the given identifier and return its actual name (if renaming was
 	 * done)
 	 * 
 	 * @param id
 	 *            The identifier name to load
 	 * @return The actual name of the identifier
 	 * @throws IntermediateCodeGeneratorException
 	 *             Identifier was not found
 	 */
 	private String loadIdentifier(String id) throws IntermediateCodeGeneratorException {
 		String name = this.currentSymbolTable.peek().getIdentifierAlias(id);
 		if (name == null) {
 			logger.fatal("Undeclared variable found: " + id);
 			throw new IntermediateCodeGeneratorException("Undeclared variable found: " + id);
 		}
 		return name;
 	}
 
 	/**
 	 * Get a new free label name
 	 * 
 	 * @return The label name
 	 */
 	private String createNewLabel() {
 		return "label" + (labelNum++);
 	}
 }
