 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Stack;
 import titocc.compiler.Assembler;
 import titocc.compiler.InternalCompilerException;
 import titocc.compiler.Register;
 import titocc.compiler.Scope;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 
 public class BinaryExpression extends Expression
 {
 	static final String[][] binaryOperators = {
 		{"||"},
 		{"&&"},
 		{"|"},
 		{"^"},
 		{"&"},
 		{"=="},
 		{"!="},
 		{"<", "<=", ">", ">="},
 		{"<<", ">>"},
 		{"+", "-"},
 		{"*", "/", "%"}
 	};
 	private String operator;
 	private Expression left, right;
 
 	public BinaryExpression(String operator, Expression left, Expression right,
 			int line, int column)
 	{
 		super(line, column);
 		this.operator = operator;
 		this.left = left;
 		this.right = right;
 	}
 
 	public String getOperator()
 	{
 		return operator;
 	}
 
 	public Expression getLeft()
 	{
 		return left;
 	}
 
 	public Expression getRight()
 	{
 		return right;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
 			throws IOException, SyntaxException
 	{
 		Register pushedRegister = pushRegister(asm, registers);
 
 		// Evaluate left expression and store it in the first available register.
 		left.compile(asm, scope, registers);
 		Register leftRegister = registers.pop();
 
 		// Evaluate right expression and store it in the next register.
 		right.compile(asm, scope, registers);
 
 		// Evaluate the operation and store the result in the left register.
 		compileOperator(asm, scope, leftRegister, registers.peek());
 
 		registers.push(leftRegister);
 
 		// Pop registers.
 		popRegister(asm, registers, pushedRegister);
 	}
 
 	private void compileOperator(Assembler asm, Scope scope, Register left, Register right)
 			throws IOException, SyntaxException
 	{
 		String jumpLabel, jumpLabel2;
 		switch (operator) {
 			//TODO short circuit && and ||
 			case "||":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				jumpLabel2 = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "jnzer", left.toString(), jumpLabel);
 				asm.emit("", "jnzer", right.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=0");
 				asm.emit("", "jump", left.toString(), jumpLabel2);
 				asm.emit(jumpLabel, "load", left.toString(), "=1");
 				asm.emit(jumpLabel2, "nop", "");
 				break;
 			case "&&":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				jumpLabel2 = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "jzer", left.toString(), jumpLabel);
 				asm.emit("", "jzer", right.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=1");
 				asm.emit("", "jump", left.toString(), jumpLabel2);
 				asm.emit(jumpLabel, "load", left.toString(), "=0");
 				asm.emit(jumpLabel2, "nop", "");
 				break;
 			case "|":
 				asm.emit("or", operator, operator);
 				break;
 			case "^":
 				asm.emit("xor", operator, operator);
 				break;
 			case "&":
 				asm.emit("and", operator, operator);
 				break;
 			case "==":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "comp", left.toString(), right.toString());
 				asm.emit("", "load", left.toString(), "=1");
				asm.emit("", "jeq", left.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=0");
 				asm.emit(jumpLabel, "nop", "");
 				break;
 			case "!=":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "comp", left.toString(), right.toString());
 				asm.emit("", "load", left.toString(), "=1");
				asm.emit("", "jneq", left.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=0");
 				asm.emit(jumpLabel, "nop", "");
 				break;
 			case "<":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "comp", left.toString(), right.toString());
 				asm.emit("", "load", left.toString(), "=1");
 				asm.emit("", "jles", left.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=0");
 				asm.emit(jumpLabel, "nop", "");
 				break;
 			case "<=":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "comp", left.toString(), right.toString());
 				asm.emit("", "load", left.toString(), "=1");
 				asm.emit("", "jngre", left.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=0");
 				asm.emit(jumpLabel, "nop", "");
 				break;
 			case ">":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "comp", left.toString(), right.toString());
 				asm.emit("", "load", left.toString(), "=1");
 				asm.emit("", "jgre", left.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=0");
 				asm.emit(jumpLabel, "nop", "");
 				break;
 			case ">=":
 				jumpLabel = scope.makeGloballyUniqueName("lbl");
 				asm.emit("", "comp", left.toString(), right.toString());
 				asm.emit("", "load", left.toString(), "=1");
 				asm.emit("", "jnles", left.toString(), jumpLabel);
 				asm.emit("", "load", left.toString(), "=0");
 				asm.emit(jumpLabel, "nop", "");
 				break;
 			case "<<":
 				asm.emit("", "shl", left.toString(), right.toString());
 				break;
 			case ">>":
 				asm.emit("", "shr", left.toString(), right.toString());
 				break;
 			case "+":
 				asm.emit("", "add", left.toString(), right.toString());
 				break;
 			case "-":
 				asm.emit("", "sub", left.toString(), right.toString());
 				break;
 			case "*":
 				asm.emit("", "mul", left.toString(), right.toString());
 				break;
 			case "/":
 				asm.emit("", "div", left.toString(), right.toString());
 				break;
 			case "%":
 				asm.emit("", "mod", left.toString(), right.toString());
 				break;
 			default:
 				throw new InternalCompilerException("Invalid operator in BinaryExpression.");
 		}
 	}
 
 	@Override
 	public Integer getCompileTimeValue()
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(BIN_EXPR " + operator + " " + left + " " + right + ")";
 	}
 
 	public static Expression parse(TokenStream tokens)
 	{
 		return parseImpl(tokens, 0);
 	}
 
 	private static Expression parseImpl(TokenStream tokens, int priority)
 	{
 		if (priority == binaryOperators.length)
 			return PrefixExpression.parse(tokens);
 
 		int line = tokens.getLine(), column = tokens.getColumn();
 		tokens.pushMark();
 		Expression expr = parseImpl(tokens, priority + 1);
 
 		if (expr != null)
 			while (true) {
 				tokens.pushMark();
 				Expression right = null;
 				String op = tokens.read().toString();
 				if (Arrays.asList(binaryOperators[priority]).contains(op))
 					right = parseImpl(tokens, priority + 1);
 
 				tokens.popMark(right == null);
 				if (right != null)
 					expr = new BinaryExpression(op, expr, right, line, column);
 				else
 					break;
 			}
 
 		tokens.popMark(expr == null);
 		return expr;
 	}
 }
