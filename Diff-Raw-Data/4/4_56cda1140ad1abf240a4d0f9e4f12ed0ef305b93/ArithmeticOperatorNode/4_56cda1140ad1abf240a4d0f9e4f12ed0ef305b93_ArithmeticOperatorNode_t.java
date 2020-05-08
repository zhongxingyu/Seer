 
 /*
  * Implements Semantic checking and code output generation for
  * Addition and Subtraction.
  * 
  * Example - a + 3
  * 			 a - 3
  * 			 a * 3
  * 			 a / 3
  */
 
 public class ArithmeticOperatorNode extends ASTNode{
 	
 	String arithType="";
 	
 	
 	/*
 	 * Instantiates ArithmeticOperator invoked by this grammar:
 	 * relational_operand '+' term 
 	 * relational_operand '-' term
 	 * relational_operand '/' term
 	 * relational_operand '*' term
 	 * relational_operand '%' term
 	 *  
 	 *  Example:
 	 *  a + 3
 	 *  a - 3
 	 *  a * 3
 	 *  a / 3
 	 *  a % 3
 	 *    
 	 *  @param str specifies whether the expression was addition or subtraction
 	 *  @param expr represents a relational operand
 	 *  @param stmt represents terms 
 	 */
 
 	public ArithmeticOperatorNode(String str, ASTNode relOp, ASTNode terms, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		addChild(relOp);
 		addChild(terms);
 		this.setType(str);
 		// TODO Auto-generated constructor stub
 	}
 	
 	/*
 	 *
 	 * Instantiates ArithmeticOperator invoked by this grammar:
 	 * 
 	 *  Example:
 	 *  -a
 	 *  
 	 *  @param str specifies whether the expression was addition or subtraction
 	 *  @param expr represents a relational operand
 	 *  @param stmt represents terms
 	 */
 	public ArithmeticOperatorNode(String str, ASTNode lcNode, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(lcNode);
 		this.setType(str);
 	}
 	
 	/*
 	 * Checks the semantics to make sure that integer is being operated with an integer.
 	 * Implicit conversions are not allowed
 	 * Throw an exception otherwise.
 	 * (non-Javadoc)
 	 * @see ASTNode#checkSemantics()
 	 */
 	@Override
 	public void checkSemantics() throws Exception {
 		// TODO Auto-generated method stub
 		this.getChildAt(0).checkSemantics();
		
		if (this.getChildCount() > 1)
			this.getChildAt(1).checkSemantics();
 		
 		/*
 		 * The grammar should look into that only boolean expressions or relational expressions are executed.
 		 */
 		//for unary minus
 		if(this.getChildCount() == 1)
 		{
 			if (!(this.getChildAt(0).getType().equalsIgnoreCase("int")
 					|| this.getChildAt(0).getType().equalsIgnoreCase("float"))) 
 			{
 				throw new Exception("Type mismatch: statement at Line " + this.getYyline() + ":" + 
 							this.getYycolumn()+"should be of the same type.");
 			}					
 		}
 		
 		else if (! (this.getChildAt(0).getType().equals(this.getChildAt(1).getType()) 
 				&& (this.getChildAt(0).getType().equals("float") || this.getChildAt(0).getType().equals("int"))))
 		{
 			throw new Exception("Type mismatch: statement at Line " + this.getYyline() + ":" + 
 						this.getYycolumn()+"should be of the same type.");
 		}
 		this.setType(this.getChildAt(0).getType());
 	}
 
 	
 	@Override
 	public String generateCode() {
 		// TODO Auto-generated method stub
 		// Should use arithType
 		return null;
 	}
 
 }
