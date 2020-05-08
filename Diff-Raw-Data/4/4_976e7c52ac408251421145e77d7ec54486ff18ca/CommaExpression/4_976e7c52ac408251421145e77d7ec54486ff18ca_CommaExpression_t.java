 package titocc.compiler.elements;
 
 import java.io.IOException;
 import titocc.compiler.Assembler;
 import titocc.compiler.Registers;
 import titocc.compiler.Scope;
 import titocc.compiler.types.CType;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 import titocc.util.Position;
 
 /**
  * Two expressions separated by comma operator. Left expression is evaluted and discarded first and
  * then right expression is returned. Does not return an lvalue even when right operand is lvalue.
  * Has lowest priority of all operators.
  *
  * <p> EBNF definition:
  *
  * <br> COMMA_EXPRESSION = [COMMA_EXPRESSION ","] ASSIGNMENT_EXPRESSION
  */
 public class CommaExpression extends Expression
 {
 	/**
 	 * Left hand side expression;
 	 */
 	private final Expression left;
 
 	/**
 	 * Right hand side expression.
 	 */
 	private Expression right;
 
 	/**
 	 * Constructs a CommaExpression.
 	 *
 	 * @param left left operand
 	 * @param right right operand
 	 * @param position starting position of the comma expression
 	 */
 	public CommaExpression(Expression left, Expression right, Position position)
 	{
 		super(position);
 		this.left = left;
 		this.right = right;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Registers regs)
 			throws IOException, SyntaxException
 	{
 		// Evaluate left operand and ignore it.
 		left.compile(asm, scope, regs);
 
 		// Evaluate right operand in first register.
 		right.compile(asm, scope, regs);
 	}
 
 	@Override
 	public CType getType(Scope scope) throws SyntaxException
 	{
		// Unlike () expressions, the comma operator causes decay on right operand. E.g. if
		// "int a[2]" then "sizeof(0,a)" equals "sizeof(int*)".
		return right.getType(scope).decay();
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(CE " + left + " " + right + ")";
 	}
 
 	/**
 	 * Attempts to parse a syntactic comma expression from token stream. If parsing fails the
 	 * stream is reset to its initial position.
 	 *
 	 * @param tokens source token stream
 	 * @return Expression object or null if tokens don't form a valid comma expression
 	 */
 	public static Expression parse(TokenStream tokens)
 	{
 		Position pos = tokens.getPosition();
 		tokens.pushMark();
 		Expression expr = AssignmentExpression.parse(tokens);
 
 		if (expr != null) {
 			while (true) {
 				tokens.pushMark();
 				Expression right = null;
 				if (tokens.read().toString().equals(","))
 					right = CommaExpression.parse(tokens);
 
 				tokens.popMark(right == null);
 				if (right != null)
 					expr = new CommaExpression(expr, right, pos);
 				else
 					break;
 			}
 		}
 
 		tokens.popMark(expr == null);
 		return expr;
 	}
 }
