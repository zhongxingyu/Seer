 /*
  * Implements Semantic checking and code output generation for
  * FactorListNode
  * 
  */
 class FactorListNode extends ASTNode {
 	/*
 	 * Instantiates FactorListNode invoked by this grammar:
 	 *
 	 *  @param lcNode represent child node
 	 *  @param yyline,yycolumn represents nodes line number and column number 
 	 */
 	public FactorListNode(ASTNode lcNode, ASTNode rcNode, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		this.addChild(lcNode);
 		this.addChild(rcNode);
 	}
 	
 	public FactorListNode(ASTNode lcNode, int yyline, int yycolumn) {
 		super(yyline, yycolumn);
 		
 		this.addChild(lcNode);
 	}
 
 	/*
 	 * symnatic check
 	 * 
 	 */
 	@Override
 	public void checkSemantics() throws Exception{
 			//symantic check for children
 		   if(this.getChildCount() == 2){
 			   this.getChildAt(0).checkSemantics();
			   this.getChildAt(1).checkSemantics();			   
 		   }
 		   else if(this.getChildCount() == 1){
 			   this.getChildAt(0).checkSemantics();
 		   }
 		   
 		   //no symantic check required for the parent node 
 		   
 		   return;	
 	}
 
 	@Override
 	public String generateCode() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
