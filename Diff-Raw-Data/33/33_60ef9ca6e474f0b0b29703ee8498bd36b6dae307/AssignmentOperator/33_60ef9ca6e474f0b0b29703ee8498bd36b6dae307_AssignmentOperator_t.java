 
 package scalr.expression;
 
 import scalr.Exceptions.TypeError;
 import scalr.variable.SymbolTable;
 
 // It may be better to implement the entire getType() method using boolean matrices,
 // where the first index is the operator, the second index is the type of the variable,
 // and the third index is the type of the expression being assigned.
 public class AssignmentOperator implements Expression
 {
 	
 	private String	   operator;
 	private String	   var, func;
 	private Expression	rval;
 	
 	public AssignmentOperator(String type)
 	{
 		operator = type;
 	}
 	
 	public void addOperand(Expression expr)
 	{
 		rval = expr;
 	}
 	
 	public void setVar(String name)
 	{
 		var = name;
 	}
 	
 	public void setFunc(String name)
 	{
 		func = name;
 	}
 	
 	@Override
 	public Expression getValue(Expression... expressions)
 	{
 		// Get the variable from the SymbolTable
 		Expression lval = SymbolTable.getMember(func, var);
 		if (operator.equals("+=")) {
			if ((lval.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)
			        || (lval.getType() == ExpressionType.SEQUENCE && rval.getType() == ExpressionType.NOTE)
			        || (lval.getType() == ExpressionType.SEQUENCE && rval.getType() == ExpressionType.SEQUENCE)) {
 				BinaryOperator bo = new BinaryOperator("+");
 				bo.addOperand(rval);
 				bo.addOperand(lval);
 				Expression result = bo.getValue(expressions);
 				// If the new type isn't the same as the old type, this will throw an error (Type
 				// checking!).
 				try {
 					SymbolTable.addReference(func, var, result);
 				}
 				catch (TypeError e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 				return result;
 			}
 		}
 		else if (operator.equals("*=")) {
			if ((lval.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)
			        || (lval.getType() == ExpressionType.SEQUENCE && rval.getType() == ExpressionType.NUMBER)) {
 				BinaryOperator bo = new BinaryOperator("*");
 				bo.addOperand(rval);
 				bo.addOperand(lval);
 				Expression result = bo.getValue(expressions);
 				// If the new type isn't the same as the old type, this will throw an error (Type
 				// checking!).
 				try {
 					SymbolTable.addReference(func, var, result);
 				}
 				catch (TypeError e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 				return result;
 			}
 		}
 		else if (operator.equals("-=")) {
			if ((lval.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)) {
 				BinaryOperator bo = new BinaryOperator("-");
 				bo.addOperand(rval);
 				bo.addOperand(lval);
 				Expression result = bo.getValue(expressions);
 				// If the new type isn't the same as the old type, this will throw an error (Type
 				// checking!).
 				try {
 					SymbolTable.addReference(func, var, result);
 				}
 				catch (TypeError e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 				return result;
 			}
 		}
 		else if (operator.equals("%=")) {
			if ((lval.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)) {
 				BinaryOperator bo = new BinaryOperator("%");
 				bo.addOperand(rval);
 				bo.addOperand(lval);
 				Expression result = bo.getValue(expressions);
 				// If the new type isn't the same as the old type, this will throw an error (Type
 				// checking!).
 				try {
 					SymbolTable.addReference(func, var, result);
 				}
 				catch (TypeError e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 				return result;
 			}
 		}
 		else if (operator.equals("/=")) {
			if ((lval.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)) {
 				BinaryOperator bo = new BinaryOperator("/");
 				bo.addOperand(rval);
 				bo.addOperand(lval);
 				Expression result = bo.getValue(expressions);
 				// If the new type isn't the same as the old type, this will throw an error (Type
 				// checking!).
 				try {
 					SymbolTable.addReference(func, var, result);
 				}
 				catch (TypeError e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 				return result;
 			}
 		}
 		else if (operator.equals("%=")) {
			if ((lval.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)) {
 				BinaryOperator bo = new BinaryOperator("%");
 				bo.addOperand(rval);
 				bo.addOperand(lval);
 				Expression result = bo.getValue(expressions);
 				// If the new type isn't the same as the old type, this will throw an error (Type
 				// checking!).
 				try {
 					SymbolTable.addReference(func, var, result);
 				}
 				catch (TypeError e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 				return result;
 			}
 		}
 		// Else if's for each of the different operator types
 		
 		// We return null so as to screw up some function above us, because the programmer is trying
 		// to assign incompatible types
 		return null;
 	}
 	
 	// Take a look at the LRM to see the valid types for each assignment operator. Since * is valid
 	// for sequences and numbers, so is *= if the lval is a sequence (and by extension, a note)
 	@Override
 	public ExpressionType getType()
 	{
 		// Get the variable from the SymbolTable
 		Expression expr = SymbolTable.getMember(func, var);
 		// += is valid for numbers and sequences (and by extension, notes)
 		if (operator.equals("+=")) {
 			// Compare their types
 			if (expr.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)
 				return ExpressionType.NUMBER;
 			else if ((expr.getType() == ExpressionType.SEQUENCE)
 			        && (rval.getType() == ExpressionType.SEQUENCE || rval.getType() == ExpressionType.NOTE))
 				return ExpressionType.SEQUENCE;
 		}
 		else if (operator.equals("*=")) {
 			// Compare their types
 			if (expr.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)
 				return ExpressionType.NUMBER;
 			else if ((expr.getType() == ExpressionType.SEQUENCE)
 			        && (rval.getType() == ExpressionType.SEQUENCE || rval.getType() == ExpressionType.NOTE))
 				return ExpressionType.SEQUENCE;
 		}
 		else if (operator.equals("-=")) {
 			// Compare their types
 			if (expr.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)
 				return ExpressionType.NUMBER;
 		}
 		else if (operator.equals("/=")) {
 			// Compare their types
 			if (expr.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)
 				return ExpressionType.NUMBER;
 		}
 		else if (operator.equals("%=")) {
 			// Compare their types
 			if (expr.getType() == ExpressionType.NUMBER && rval.getType() == ExpressionType.NUMBER)
 				return ExpressionType.NUMBER;
 		}
 		// More else if's (else if's only) for each different operator
 		
 		// We return null so as to screw up some function above us, because the programmer is trying
 		// to assign incompatible types
 		return null;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return operator;
 	}
 }
