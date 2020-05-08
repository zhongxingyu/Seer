 
 
 /*
  * Implements semantic checking and output code generation
  * for an 'if-else' statement
  * Example :
  * 			if(i<=4){i=i+1;}
  * 			OR
  * 			if(i<=4){i=i+1;}
  * 				else{i=i-1;}
  */
 public class IfStatementNode extends ASTNode implements NoSemiColonStatement{
 	
 	private int ifType;
 
 	/* 
 	 *
 	 * Instantiates AssignmentOperatorNode invoked by this grammar:
 	 * IF '(' expression ')' '{' statements '}'. Note expression should be relational expression or boolean expression
 	 * 
 	 *  Example:
 	 *  if(i<=4){i=i+1;}
 	 *  
 	 *  @param expr represents an expression which is either relational or boolean
 	 *  @param stmt represents the statements to be executed when 'if' condition is satisfied.
 	 */
 	public IfStatementNode(ASTNode expr, ASTNode stmt, int yyline, int yycolumn)
 	{
 		super(yyline, yycolumn);
 		this.addChild(expr);
 		this.addChild(stmt);
 		this.ifType=1;
 	}
 	
 	/*
 	 *  Instantiates AssignmentOperatorNode invoked by this grammar:
 	 *  IF '(' expression ')' '{' statements '}' ELSE '{' statements '}' 
 	 * 
 	 *  Example:
  	 * 	if(i<=4){i=i+1;}
  	 * 		else{i=i-1;}
  	 * 
  	 *  @param expr represents an expression which is either relational or boolean
  	 *  @param stmt1 represents the statements to be executed when 'if' condition is satisfied.
   	 *  @param stmt2 represents the statements to be executed when 'if' condition is not satisfied.
 	 */
 	public IfStatementNode(ASTNode expr, ASTNode stmt1, ASTNode stmt2, int yyline, int yycolumn)
 	{
 		super(yyline, yycolumn);
 		this.addChild(expr);
 		this.addChild(stmt1);
 		this.addChild(stmt2);
 		this.ifType=2;
 	}
 
 	/*
 	 * This is the semantic analysis for the if loop.
 	 * The expression should be of type boolean.
 	 * @throws Exception 
 	 * @see ASTNode#checkSemantics()
 	 */
 	@Override
 	public void checkSemantics() throws Exception{
 		// TODO Auto-generated method stub
 		
 		if(this.getChildCount()==2)
 		{	
 			this.getChildAt(0).checkSemantics();
 			this.getChildAt(1).checkSemantics();
                        if ("return".equalsIgnoreCase(this.getChildAt(1).getType()))
                                this.setType("return");
 		}
 		else if(this.getChildCount()==3)
 		{	
 			this.getChildAt(0).checkSemantics();
 			this.getChildAt(1).checkSemantics();
 			this.getChildAt(2).checkSemantics();
                         if ("return".equalsIgnoreCase(this.getChildAt(1).getType()) && "return".equalsIgnoreCase(this.getChildAt(2).getType()))
                                 this.setType("return");
 		}
 		
 		if (! this.getChildAt(0).getType().equals("boolean"))
 		{
 			throw new Exception("Type mismatch: statement at Line " + this.getYyline() + ":" + this.getYycolumn()+" should be a boolean; found: " + this.getChildAt(0).getType());
 		}
 		
 	}
 
 	@Override
 	public StringBuffer generateCode() 
 	{
 		StringBuffer output = new StringBuffer();
 		output.append("if("+this.getChildAt(0).generateCode()+")");
 		output.append("\n");
 		output.append("{");
 		if(this.ifType==1)
 		{
 			output.append(this.getChildAt(1).generateCode());
 		}
 		else if(this.ifType==2)
 		{
 			output.append(this.getChildAt(1).generateCode());
 			output.append("\n");
 			output.append("}");
 			output.append("else");
 			output.append("{");
 			output.append(this.getChildAt(2).generateCode());
 		}
 		output.append("\n");
 		output.append("}");
 		return output;
 	}
 	
 	public int getIfType() {
 		return ifType;
 	}
 
 	public void setIfType(int ifType) {
 		this.ifType = ifType;
 	}
 }
