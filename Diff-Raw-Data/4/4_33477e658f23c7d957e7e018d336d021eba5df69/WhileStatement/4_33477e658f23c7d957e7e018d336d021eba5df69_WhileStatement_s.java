 package titocc.compiler.elements;
 
 import java.io.IOException;
 import java.util.Stack;
 import titocc.compiler.Assembler;
 import titocc.compiler.Register;
 import titocc.compiler.Scope;
 import titocc.tokenizer.SyntaxException;
 import titocc.tokenizer.TokenStream;
 
 public class WhileStatement extends Statement
 {
 	private Expression test;
 	private Statement statement;
 
 	public WhileStatement(Expression test, Statement statement, int line, int column)
 	{
 		super(line, column);
 		this.test = test;
 		this.statement = statement;
 	}
 
 	public Expression getTest()
 	{
 		return test;
 	}
 
 	public Statement getStatement()
 	{
 		return statement;
 	}
 
 	@Override
 	public void compile(Assembler asm, Scope scope, Stack<Register> registers)
 			throws IOException, SyntaxException
 	{
 		// Loop start.
 		String loopStartLabel = scope.makeGloballyUniqueName("lbl");
 		asm.emit(loopStartLabel, "nop", "");
 
 		// Body.
 		statement.compile(asm, scope, registers);
 
 		// Loop test code is after the body so that we only need one
 		// jump instruction per iteration.
 		test.compile(asm, scope, registers);
 		asm.emit("", "jnzer", registers.peek().toString(), loopStartLabel);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "(WHILE " + test + " " + statement + ")";
 	}
 
 	public static WhileStatement parse(TokenStream tokens)
 	{
 		int line = tokens.getLine(), column = tokens.getColumn();
 		tokens.pushMark();
 		WhileStatement whileStatement = null;
 
 		Type retType = Type.parse(tokens);
 
 		if (tokens.read().toString().equals("while")) {
 			if (tokens.read().toString().equals("(")) {
 				Expression test = Expression.parse(tokens);
 				if (test != null) {
 					if (tokens.read().toString().equals(")")) {
 						Statement statement = Statement.parse(tokens);
 						if (statement != null)
 							whileStatement = new WhileStatement(test, statement, line, column);
 					}
 				}
 			}
 		}
 
 		tokens.popMark(whileStatement == null);
 		return whileStatement;
 	}
 }
