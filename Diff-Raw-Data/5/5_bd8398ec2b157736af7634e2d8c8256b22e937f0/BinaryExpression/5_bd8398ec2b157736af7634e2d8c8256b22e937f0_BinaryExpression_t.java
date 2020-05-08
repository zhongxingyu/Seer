 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import titocc.compiler.Assembler;
 import titocc.compiler.Registers;
 import titocc.compiler.Scope;
 import titocc.compiler.types.CType;
 import titocc.compiler.types.IntType;
 import titocc.compiler.types.VoidType;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 import titocc.util.Position;
 
 /**
  * Expression formed by a binary operator and two operands.
  *
  * <p> EBNF Definition:
  *
  * <br> BINARY_EXPRESSION = [BINARY_EXPRESSION "||"] BINARY_EXPRESSION2
  *
  * <br> BINARY_EXPRESSION2 = [BINARY_EXPRESSION2 "&&"] BINARY_EXPRESSION3
  *
  * <br> BINARY_EXPRESSION3 = [BINARY_EXPRESSION3 "|"] BINARY_EXPRESSION4
  *
  * <br> BINARY_EXPRESSION4 = [BINARY_EXPRESSION4 "^"] BINARY_EXPRESSION5
  *
  * <br> BINARY_EXPRESSION5 = [BINARY_EXPRESSION5 "&"] BINARY_EXPRESSION6
  *
  * <br> BINARY_EXPRESSION6 = [BINARY_EXPRESSION6 "=="] BINARY_EXPRESSION7
  *
  * <br> BINARY_EXPRESSION7 = [BINARY_EXPRESSION7 "!=") BINARY_EXPRESSION8
  *
  * <br> BINARY_EXPRESSION8 = [BINARY_EXPRESSION8 ("<" | "<=" | ">" | ">=")] BINARY_EXPRESSION9
  *
  * <br> BINARY_EXPRESSION9 = [BINARY_EXPRESSION9 ("<<" | ">>")] BINARY_EXPRESSION10
  *
  * <br> BINARY_EXPRESSION10 = [BINARY_EXPRESSION10 ("+" | "-")] BINARY_EXPRESSION11
  *
  * <br> BINARY_EXPRESSION11 = [BINARY_EXPRESSION11 ("*" | "/" | "%")] PREFIX_EXPRESSION
  */
 public class BinaryExpression extends Expression
 {
 	private enum Type
 	{
 		BITWISE, ARITHMETIC, EQUALITY, RELATIONAL, LOGICAL
 	};
 
 	/**
 	 * Binary operator with mnemonic and operation type.
 	 */
 	private static class Operator
 	{
 		public String mnemonic;
 
 		public Type type;
 
 		int priority;
 
 		public Operator(String mnemonic, Type type, int priority)
 		{
 			this.mnemonic = mnemonic;
 			this.type = type;
 			this.priority = priority;
 		}
 	}
 
 	/**
 	 * Binary operators, their main instructions and priorities.
 	 */
 	static final Map<String, Operator> binaryOperators = new HashMap<String, Operator>()
 	{
 		{
 			put("||", new Operator("jnzer", Type.LOGICAL, 1));
 			put("&&", new Operator("jzer", Type.LOGICAL, 2));
 			put("|", new Operator("or", Type.BITWISE, 3));
 			put("^", new Operator("xor", Type.BITWISE, 4));
 			put("&", new Operator("and", Type.BITWISE, 5));
 			put("==", new Operator("jequ", Type.EQUALITY, 6));
 			put("!=", new Operator("jnequ", Type.EQUALITY, 7));
 			put("<", new Operator("jles", Type.RELATIONAL, 8));
 			put("<=", new Operator("jngre", Type.RELATIONAL, 8));
 			put(">", new Operator("jgre", Type.RELATIONAL, 8));
 			put(">=", new Operator("jnles", Type.RELATIONAL, 8));
 			put("<<", new Operator("shl", Type.BITWISE, 9));
 			put(">>", new Operator("shr", Type.BITWISE, 9));
 			put("+", new Operator("add", Type.ARITHMETIC, 10));
 			put("-", new Operator("sub", Type.ARITHMETIC, 10));
 			put("*", new Operator("mul", Type.ARITHMETIC, 11));
 			put("/", new Operator("div", Type.ARITHMETIC, 11));
 			put("%", new Operator("mod", Type.ARITHMETIC, 11));
 		}
 	};
 
 	/**
 	 * Binary operator as a string.
 	 */
 	private final String operator;
 
 	/**
 	 * Left hand side expression;
 	 */
 	private final Expression left;
 
 	/**
 	 * Right hand side expression.
 	 */
 	private final Expression right;
 
 	/**
 	 * Constructs a BinaryExpression.
 	 *
 	 * @param operator operator as string
 	 * @param left left operand
 	 * @param right right operand
 	 * @param position starting position of the binary expression
 	 */
 	public BinaryExpression(String operator, Expression left, Expression right,
 			Position position)
 	{
 		super(position);
 		this.operator = operator;
 		this.left = left;
 		this.right = right;
 	}
 
 	/**
 	 * Returns the operator.
 	 *
 	 * @return the operator
 	 */
 	public String getOperator()
 	{
 		return operator;
 	}
 
 	/**
 	 * Returns the left operand.
 	 *
 	 * @return the left operand
 	 */
 	public Expression getLeft()
 	{
 		return left;
 	}
 
 	/**
 	 * Returns the right operand
 	 *
 	 * @return the right operand
 	 */
 	public Expression getRight()
 	{
 		return right;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Registers regs)
 			throws IOException, SyntaxException
 	{
 		checkTypes(scope);
 
 		// Evaluate LHS; load value to 1st register.
 		left.compile(asm, scope, regs);
 
 		// Allocate a second register for right operand.
 		regs.allocate(asm);
 
 		// Compile right expression and the operator.
 		Type opType = binaryOperators.get(operator).type;
 		if (opType == Type.BITWISE || opType == Type.ARITHMETIC)
 			compileSimpleOperator(asm, scope, regs);
 		else if (opType == Type.LOGICAL)
 			compileLogicalOperator(asm, scope, regs);
 		else if (opType == Type.RELATIONAL || opType == Type.EQUALITY)
 			compileComparisonOperator(asm, scope, regs);
 
 		// Deallocate the second register.
 		regs.deallocate(asm);
 	}
 
 	private void compileRight(Assembler asm, Scope scope, Registers regs)
 			throws SyntaxException, IOException
 	{
 		// Evaluate RHS; load to second register;
 		regs.removeFirst();
 		right.compile(asm, scope, regs);
 		regs.addFirst();
 	}
 
 	private void compileSimpleOperator(Assembler asm, Scope scope, Registers regs)
 			throws IOException, SyntaxException
 	{
 		int leftIncrSize = left.getType(scope).decay().getIncrementSize();
 		int rightIncrSize = right.getType(scope).decay().getIncrementSize();
 
 		if (leftIncrSize > 1 && rightIncrSize > 1) {
 			// POINTER - POINTER.
 			compileRight(asm, scope, regs);
 			asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(),
 					regs.get(1).toString());
 			asm.emit("div", regs.get(0).toString(), "=" + leftIncrSize);
 		} else if (leftIncrSize > 1) {
 			// POINTER + INTEGER or POINTER - INTEGER.
 			compileRight(asm, scope, regs);
 			asm.emit("mul", regs.get(1).toString(), "=" + leftIncrSize);
 			asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(),
 					regs.get(1).toString());
 		} else if (rightIncrSize > 1) {
 			// INTEGER + POINTER.
 			asm.emit("mul", regs.get(0).toString(), "=" + rightIncrSize);
 			compileRight(asm, scope, regs);
 			asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(),
 					regs.get(1).toString());
 		} else {
 			compileRight(asm, scope, regs);
 			asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(),
 					regs.get(1).toString());
 		}
 	}
 
 	private void compileComparisonOperator(Assembler asm, Scope scope, Registers regs)
 			throws IOException, SyntaxException
 	{
 		compileRight(asm, scope, regs);
 		String jumpLabel = scope.makeGloballyUniqueName("lbl");
 		asm.emit("comp", regs.get(0).toString(), regs.get(1).toString());
 		asm.emit("load", regs.get(0).toString(), "=1");
 		asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(), jumpLabel);
 		asm.emit("load", regs.get(0).toString(), "=0");
 		asm.addLabel(jumpLabel);
 	}
 
 	private void compileLogicalOperator(Assembler asm, Scope scope, Registers regs)
 			throws IOException, SyntaxException
 	{
 		// Short circuit evaluation; only evaluate RHS if necessary.
 		String jumpLabel = scope.makeGloballyUniqueName("lbl");
 		String jumpLabel2 = scope.makeGloballyUniqueName("lbl");
 		asm.emit(binaryOperators.get(operator).mnemonic, regs.get(0).toString(), jumpLabel);
 		compileRight(asm, scope, regs);
 		asm.emit(binaryOperators.get(operator).mnemonic, regs.get(1).toString(), jumpLabel);
 		asm.emit("load", regs.get(0).toString(), operator.equals("||") ? "=0" : "=1");
 		asm.emit("jump", regs.get(0).toString(), jumpLabel2);
 		asm.addLabel(jumpLabel);
 		asm.emit("load", regs.get(0).toString(), operator.equals("||") ? "=1" : "=0");
 		asm.addLabel(jumpLabel2);
 	}
 
 	@Override
 	public Integer getCompileTimeValue()
 	{
 		// Compile time evaluation of binary operators could be implemented here.
 		return null;
 	}
 
 	@Override
 	public CType getType(Scope scope) throws SyntaxException
 	{
 		return checkTypes(scope);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(BIN_EXPR " + operator + " " + left + " " + right + ")";
 	}
 
 	private CType checkTypes(Scope scope) throws SyntaxException
 	{
 		Operator op = binaryOperators.get(operator);
 		CType leftType = left.getType(scope).decay();
 		CType rightType = right.getType(scope).decay();
 		CType leftDeref = leftType.dereference();
 		CType rightDeref = rightType.dereference();
 
 		if (op.type == Type.LOGICAL) {
 			if (leftType.isScalar() && rightType.isScalar())
 				return new IntType();
 		} else if (op.type == Type.EQUALITY) {
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return new IntType();
 			if (leftDeref.equals(rightDeref))
 				return new IntType();
 			if (leftDeref instanceof VoidType && (rightDeref.isObject()
 					|| rightDeref.isIncomplete()))
 				return new IntType();
 			if (rightDeref instanceof VoidType && (leftDeref.isObject()
 					|| leftDeref.isIncomplete()))
 				return new IntType();
 			if (leftType.isPointer() && rightType.isInteger()
 					&& new Integer(0).equals(right.getCompileTimeValue()))
 				return new IntType();
 			if (rightType.isPointer() && leftType.isInteger()
 					&& new Integer(0).equals(left.getCompileTimeValue()))
 				return new IntType();
 		} else if (op.type == Type.RELATIONAL) {
			if (leftType.isArithmetic() && rightType.isArithmetic()) //TODO arithmetic->real
 				return new IntType();
			if (leftDeref.equals(rightDeref) && (leftDeref.isObject() || leftDeref.isIncomplete()))
 				return new IntType();
 		} else if (operator.equals("+")) {
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return new IntType();
 			if (leftDeref.isObject() && rightType.isInteger())
 				return leftType;
 			if (leftType.isInteger() && rightDeref.isObject())
 				return rightType;
 		} else if (operator.equals("-")) {
 			if (leftType.isArithmetic() && rightType.isArithmetic())
 				return new IntType();
 			if (leftDeref.isObject() && rightType.isInteger())
 				return leftType;
 			if (leftDeref.isObject() && rightDeref.equals(leftDeref))
 				return new IntType();
 		} else if (op.type == Type.BITWISE) {
 			if (leftType.isInteger() && rightType.isInteger())
 				return new IntType();
 		} else if (op.type == Type.ARITHMETIC) {
 			if (leftType.isArithmetic() && (rightType.isInteger()
 					|| (!operator.equals("%") && rightType.isArithmetic())))
 				return new IntType();
 		}
 
 		throw new SyntaxException("Incompatible operands for operator " + operator + ".",
 				getPosition());
 	}
 
 	/**
 	 * Attempts to parse a syntactic binary expression from token stream. If parsing fails the
 	 * stream is reset to its initial position.
 	 *
 	 * @param tokens source token stream
 	 * @return Expression object or null if tokens don't form a valid expression
 	 */
 	public static Expression parse(TokenStream tokens)
 	{
 		return parseImpl(tokens, 0);
 	}
 
 	/**
 	 * Recursive implementation of the parsing method. Each call parses one
 	 * priority level of binary operators.
 	 */
 	private static Expression parseImpl(TokenStream tokens, int priority)
 	{
 		if (priority == 12)
 			return PrefixExpression.parse(tokens);
 
 		Position pos = tokens.getPosition();
 		tokens.pushMark();
 		Expression expr = parseImpl(tokens, priority + 1);
 
 		if (expr != null) {
 			while (true) {
 				tokens.pushMark();
 				Expression right = null;
 				String op = tokens.read().toString();
 				if (binaryOperators.containsKey(op) && binaryOperators.get(op).priority == priority)
 					right = parseImpl(tokens, priority + 1);
 
 				tokens.popMark(right == null);
 				if (right != null)
 					expr = new BinaryExpression(op, expr, right, pos);
 				else
 					break;
 			}
 		}
 
 		tokens.popMark(expr == null);
 		return expr;
 	}
 }
