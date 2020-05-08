 /**
  * Represents a declaration statement
  * This is where identifiers are inserted into the symbols tables
  * @author Aiman
  */
 public class DeclarationNode extends ASTNode {
 
 	
 	private String declaredType;
 	private IDNode idNode;
         private boolean isStatement;	
 	/**
 	 * Instantiates a DeclarationNode that has been invoked by this grammar:
 	 * type ID
 	 * Example: int x
 	 *  @param typeNode the type of the identifier (e.g. int)
 	 *  @param idNode the identifier name node (e.g. x)
 	 *  @param yyline the line where this operator was found in the source code
 	 *  @param yycolumn the column where this operator was found in the source code
 	 */
 	public DeclarationNode(String idType, ASTNode idNode, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(idNode);
 		this.declaredType = idType;
 		this.idNode = (IDNode)idNode;
 		this.setType(idType);
                 isStatement = false;
 	}
 
 	/**
 	 * Declaration semantics:
 	 * Verify that the declared name has not been declared before
 	 * Inserts the symbol into the symbols table
 	 * @throws Exception
 	 */
 	@Override
 	public void checkSemantics() throws Exception {
 
 
 		this.getChildAt(0).checkSemantics();
 		
 		
 		// Now, insert this new ID into symbols table, and generate a new valid target variable name for it
 		String varName = "_smartestVar_" + this.getIdNode().getName();
 		
 		
 		if (Parser.DEBUG)
 		{
 			System.out.println("** Inserting new variable into symbols table:");
			System.out.println("Type:" + declaredType.toLowerCase());
 			System.out.println("Var Name: " + varName);
 			System.out.println("Line Number: " + ""+this.getYyline());		
 		}
 		
 		
		String[] data = { this.getDeclaredType().toLowerCase(),varName, ""+this.getYyline() }; 
 		
 		
 		
 		Parser.symbolsTable.put(this.getIdNode().getName(), data);
                this.getChildAt(0).setType(getDeclaredType().toLowerCase());
 
 
                 this.setType(this.getChildAt(0).getType());
 
 	}
 
 	@Override
 	public StringBuffer generateCode() 
 	{
 		StringBuffer output =  new StringBuffer();
 		String[] data = Parser.symbolsTable.get(this.getIdNode().getName());
 		
 		String javaType = data[0];
 		if (javaType == "question")
 			javaType = "QuestionObject";
 		
 		output.append(javaType+" ");
 		output.append(this.getChildAt(0).generateCode());
                 if (isStatement)
                 {
                         if ("string".equalsIgnoreCase(getType()))
                         {
                                output.append(" = \"\"");
                         }
                         else if ("boolean".equalsIgnoreCase(getType()))
                         {
                                 output.append(" = false");
                         }
                         else if ("float".equalsIgnoreCase(getType()))
                         {
                                 output.append(" = 0");
                         }
                         else if ("int".equalsIgnoreCase(getType()))
                         {
                                 output.append(" = 0");
                         }
                         else if ("char".equalsIgnoreCase(getType()))
                         {
                                 output.append(" = '\0'");
                         }
                         else
                         {
                                 output.append(" = null");
                         }
                 }
 		return output;
 	}
 
 	public IDNode getIdNode() {
 		return idNode;
 	}
 
 	public void setIdNode(IDNode idNode) {
 		this.idNode = idNode;
 	}
 
 	/**
 	 * @return the declaredType
 	 */
 	public String getDeclaredType() {
 		return declaredType;
 	}
         public void setIsStatement(boolean newValue)
         {
                 isStatement = newValue;
         }
 	/**
 	 * @param declaredType the declaredType to set
 	 */
 	public void setDeclaredType(String declaredType) {
 		this.declaredType = declaredType;
 	}
 
 	
 
 	
 
 }
