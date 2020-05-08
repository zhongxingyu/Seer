 import java.util.ArrayList;
 
 public class FunctionNode extends ASTNode
 {
         private ArrayList<ASTNode> paramList;
         private ArrayList<ASTNode> stmtList;
         private String rtrnType;
         private String identifier;
 
        public FunctionNode(String returnType, String id, ArrayList<ASTNode> parameterList, ArrayList<ASTNode> statementList, int yyline, int yycol)
         {
                 super(yyline, yycol);
                 rtrnType = returnType;
                 paramList = parameterList;
                 stmtList = statementList;
                identifier = id;
         }
 	public void checkSemantics() throws Exception
         {
                 if (paramList != null)
                 {
                         for (ASTNode param : paramList)
                         {
                                 param.checkSemantics();
                         }
                 }
                 if (stmtList != null)
                 {
                         for (ASTNode statement : stmtList)
                         {
                                 statement.checkSemantics();
                         }
                 }
                 if (Parser.functionSymbolsTable.containsKey(identifier.toLowerCase()))
                 {
                         throw new Exception("Line " + this.getYyline() +
                                 ": Function " + identifier.toLowerCase() +
                                 " is already defined");
                 }
                 else
                 {
                         String javaID = "_smartestFunction_" + identifier;
                         ArrayList<String> parameterList = new ArrayList<String>();
                         for (ASTNode param : paramList)
                         {
                                 parameterList.add(param.getType());
                         }
 
                         Parser.functionSymbolsTable.put(identifier.toLowerCase(), new FunctionSymbolTableEntry(identifier, javaID, parameterList));
                 }
                 setType(rtrnType);
         }
 	
 		public String getIdentifier()
 		{
 			return identifier;
 		}
 	
         public String generateCode()
         {
                 return "";
         }
         
 }
