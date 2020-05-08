 
 /*
  * Implements Semantic checking and code output generation for
  * Addition and Subtraction.
  * 
  * Example - a + 3
  * 			 a - 3
  * 			 a * 3
  * 			 a / 3
  */
 
 /**
  * The Class ArithmeticOperatorNode.
  * @author Parth and Kshitij
  */
 public class ArithmeticOperatorNode extends ASTNode{
 
 	/** The arith type. */
 	private String arithType="";
 
 
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
 
 	/**
 	 * Instantiates a new arithmetic operator node.
 	 *
 	 * @param str the str
 	 * @param relOp the rel op
 	 * @param terms the terms
 	 * @param yyline the yyline
 	 * @param yycolumn the yycolumn
 	 */
 	public ArithmeticOperatorNode(String str, ASTNode relOp, ASTNode terms, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		addChild(relOp);
 		addChild(terms);
 		this.setArithType(str);
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
 	/**
 	 * Instantiates a new arithmetic operator node.
 	 *
 	 * @param str the str
 	 * @param lcNode the lc node
 	 * @param yyline the yyline
 	 * @param yycolumn the yycolumn
 	 */
 	public ArithmeticOperatorNode(String str, ASTNode lcNode, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(lcNode);
 		this.setArithType(str);
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
 					|| this.getChildAt(0).getType().equalsIgnoreCase("double"))) 
 			{
				throw new Exception("Type mismatch: statement at Line " + this.getYyline() + ":" + 
						this.getYycolumn()+"should be of the same type. Unary");
 			}	
 			if(this.getChildAt(0).getType().equals("double"))
 				this.setType("double");
 			else
 				this.setType("int");
 		}
 		else
 		{
 
 			if(!((this.getChildAt(0).getType().equals("double") || this.getChildAt(0).getType().equals("int"))
 					&& (this.getChildAt(1).getType().equals("double") || this.getChildAt(1).getType().equals("int"))))
 			{
 				throw new Exception("Cannot do Arithmetic operation on "+this.getChildAt(0).getType()+ " & " + 
 						this.getChildAt(1).getType()+" on line "+ this.getYyline() + ":" + 
 						this.getYycolumn());
 			}
 			if(this.getChildAt(0).getType().equals("double") || this.getChildAt(1).getType().equals("double"))
 				this.setType("double");
 			else
 				this.setType("int");
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see ASTNode#generateCode()
 	 */
 	@Override
 	public StringBuffer generateCode() {
 		// TODO Auto-generated method stub
 		// Should use arithType
 		StringBuffer output = new StringBuffer();
 		if(this.getChildCount() > 1)
 		{
 			output.append(this.getChildAt(0).generateCode());
 			if ("multiplication".equalsIgnoreCase(getArithType()))
 			{
 				output.append(" * ");
 			}
 			else if ("division".equalsIgnoreCase(getArithType()))
 			{
 				output.append(" / ");
 			}
 			else if ("addition".equalsIgnoreCase(getArithType()))
 			{
 				output.append(" + ");
 			}
 			else if ("subtraction".equalsIgnoreCase(getArithType()))
 			{
 				output.append(" - ");
 			}
 			else if ("modulus".equalsIgnoreCase(getArithType()))
 			{
 				output.append(" % ");
 			}
 			output.append(this.getChildAt(1).generateCode());
 		}
 		else
 		{
 			output.append(" - ");
 			output.append(this.getChildAt(0).generateCode());
 		}
 		return output;
 	}
 
 	/**
 	 * @return the arithType
 	 */
 	public String getArithType() {
 		return arithType;
 	}
 
 	/**
 	 * @param arithType the arithType to set
 	 */
 	public void setArithType(String arithType) {
 		this.arithType = arithType;
 	}
 
 }
 
