 
 package scalr.expression;
 
 import scalr.Exceptions.TypeError;
 import scalr.variable.SymbolTable;
 
 public class VariableReference implements Expression
 {
 	/** The string representing the name of this variable in the symbol table */
 	String	ID;
 	/** The string representing the name of the function that his variable belongs to. */
 	String	func;
 	
 	public VariableReference(String ID, String func)
 	{
 		this.ID = ID;
 		this.func = func;
 	}
 	
 	@Override
 	public Expression getValue(Expression... expressions)
 	{
 		// Evaluate the expression and store the result back in the symbol table
 		Expression expr = SymbolTable.getMember(func, ID);
 		Expression expr2 = expr.getValue(expressions);
 		// Put it back to the table
 		try {
 			SymbolTable.addReference(func, ID, expr2);
 		}
 		catch (TypeError e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 		return expr2;
 	}
 	
 	@Override
 	public ExpressionType getType()
 	{
 		return SymbolTable.getMember(func, ID).getType();
 	}
 	
	@Override
	public String toString()
	{
		return "ID: " + ID + " | Type: " + getType();
	}
	
 }
