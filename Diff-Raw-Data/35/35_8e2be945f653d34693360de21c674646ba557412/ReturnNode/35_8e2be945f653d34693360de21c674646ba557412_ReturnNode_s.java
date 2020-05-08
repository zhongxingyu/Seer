 
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
			if (!returnType.equalsIgnoreCase(this.getChildAt(0).getType()))
 			{
				throw new Exception("Expecting return type " + returnType + " not " + this.getChildAt(0).getType());
 			}
 	}
 	public String generateCode() 
 	{
 		return null;
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
