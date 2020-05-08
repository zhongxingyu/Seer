 
 public class ReturnNode extends ASTNode{
 
 	private String returnType;
 	
 	ReturnNode(String returnType, ASTNode returnExpression, int yyline, int yycolumn)
 	{
 		super(yyline,yycolumn);
 		this.returnType = returnType;
 		this.addChild(returnExpression);
 	}
 	
 	public void checkSemantics() throws Exception
 	{
 			for (ASTNode child : this.getChildren())
 			{
 				if (child != null)
 					child.checkSemantics();
 			}
                         if (returnType.equalsIgnoreCase("void") && this.getChildAt(0) != null )
                         {
                                 throw new Exception("Cannot return a value in a void function");
                         }
 
                         String returnExpressionType;
                         if (this.getChildAt(0) == null)
                                 returnExpressionType = "void";
                         else
                                 returnExpressionType = this.getChildAt(0).getType();
 
 
 			if (!returnType.equalsIgnoreCase(returnExpressionType))
 			{
                                 if(!("double".equalsIgnoreCase(returnType) && "int".equalsIgnoreCase(returnExpressionType)))
 				        throw new Exception("Expecting return type " + returnType + " not " + returnExpressionType);
 			}
                         this.setType("return");
 	}
 	public StringBuffer generateCode() 
 	{
 		StringBuffer output = new StringBuffer();
 		output.append("return ");
 		if (this.getChildAt(0) != null)
 			output.append(this.getChildAt(0).generateCode());
 		return output;
 	}
 	public String getReturnType()
 	{
 		return returnType; 
 	}
 	public void setReturnType(String returnType)
 	{
 		this.returnType = returnType;
 	}
 	
 }
