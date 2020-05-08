 
 package scalr.expression;
 
 import java.util.ArrayList;
 
 import scalr.Exceptions.TypeError;
 import scalr.variable.Note;
 import scalr.variable.Sequence;
 import scalr.variable.SymbolTable;
 
 /**
  * Represent the foreach statement in our language. A sample statements is: foreach (n) in seq ...
  * end
  * @author kugurst and Kofi
  */
 public class ForEachStatement implements Expression
 {
 	/** The stmts to execute as we iterate through this for each */
 	ArrayList<Expression>	stmts	= new ArrayList<Expression>();
 	/** The sequence to iterate over */
 	Expression	          sequence;
 	/** The name of the note to assign when we are iterating. */
 	String	              noteName;
 	
 	public ForEachStatement(String var)
 	{
 		noteName = var;
 	}
 	
 	public void addStatement(Expression expr)
 	{
 		if (expr != null)
 			stmts.add(expr);
 	}
 	
 	public void addSequence(Expression expr)
 	{
 		sequence = expr;
 	}
 	
 	/**
 	 * ForEach statements return nothing. It is improper to use them in another expression that
 	 * requires a value.
 	 */
 	@Override
 	public Expression getValue(Expression... expressions)
 	{
 		// At this point, we should be able to evaluate the sequence
 		Sequence seq = (Sequence) sequence.getValue(expressions);
 		// Iterate through the sequence (note, the getValue of a sequence automatically evaluates
 		// all the notes)
 		for (Expression e : seq.getSequence()) {
 			Note n = (Note) e;
 			// Add the current note to the symbol table as this note value
 			try {
 				SymbolTable.addReference(SymbolTable.currentFunctionScope, noteName, n);
 			}
 			catch (TypeError e1) {
 				e1.printStackTrace();
 				System.exit(1);
 			}
			
 		}
 		return null;
 	}
 	
 	/**
 	 * Likewise, this has no ExpressionType
 	 */
 	@Override
 	public ExpressionType getType()
 	{
 		return null;
 	}
 	
 }
