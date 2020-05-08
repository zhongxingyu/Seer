 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Node;
 import datamodels.SymbolTableModel;
 import datamodels.Tree;
 import java.util.ArrayList;
 import storage.SymbolTable;
 
 
 public class SemanticAnalyzer {
     
     // Attributes
     private Tree astTree;
     private SymbolTable symbolTable;
     private ArrayList<String> list;
     private String semanticErrorMessage;
     
     // Constructor
     public SemanticAnalyzer(Tree tree, SymbolTable symTbl, ArrayList<String> list){
         this.semanticErrorMessage = "Success";
         this.astTree = tree;
         this.symbolTable = symTbl;
         this.list = list;
         this.storeToken(astTree.getRoot(), list);
         System.err.println("NI SUD DIRI");      
         //displaySymbolTable();    
     }
     
     // Methods
     //<editor-fold defaultstate="collapsed" desc="Debugging Methods">
     public void displaySymbolTable(){
         System.out.println("\n\nSymbol Table:");
         System.out.println("token \t\t tokenVal \t\t datatype \t\t scope \t\t actual value");
         for(int i=0; i<symbolTable.table.size(); i++){
             SymbolTableModel temp = symbolTable.table.get(i);
             System.out.println(temp.token+" \t\t "+temp.tokenValue+" \t\t\t "+temp.datatype+" \t\t\t "+temp.scope+"\t\t\t"+temp.actualValue);
         }
     }
     //</editor-fold>
     
     // Returns a list of strings containing all terminals: #main, #char, #bool, #func
     private void storeToken(Node n, ArrayList<String> s){
         ArrayList<Node> temp = n.getNodeChildren();
         for(Node new_n : temp){
             if(!new_n.getNodeData().equals("~")){
                 s.add(new_n.getNodeData());
             }
             storeToken(new_n, s);
         } 
     }
     
     // Returns the Node containing #main
     public Boolean checkMain(Node root){
         Boolean verifyFunc = true;
         ArrayList<String> allTokens = new ArrayList<String>();
         storeToken(root, allTokens);
         System.err.println(allTokens);
         // Check if there's main
         if(allTokens.contains("#main"))
         {
             System.err.println(allTokens);
             int index  = allTokens.indexOf("#main")+1;
             System.err.println(index);
             for(; index<allTokens.size() && !allTokens.get(index).equals("#func"); index++){
                 System.out.print("\n" + index + allTokens.get(index));
                 if(allTokens.get(index).equals("#func")){
                     System.err.println("There is a function after #main");
                     verifyFunc = false;
                 } else{ 
                     verifyFunc = true;
                 }
             }
         }
         System.err.println(allTokens);
         return verifyFunc;
     }   
     
     public void checkDataType(){
         int i, j;
         Boolean charTypeChecker = true;
         Boolean verifyDeclarationOfVariable = true;
             
         ArrayList<Integer> storeFuncInList = new ArrayList<Integer>();
         ArrayList<Integer> storeFuncInST = new ArrayList<Integer>();
         ArrayList<String> storeAllDecVar = new ArrayList<String>();
         
         //store all indeces of function types found in the arraylist of tokens
         for(i = 0;i<list.size();i++){
             if(list.get(i).equals("#func") || list.get(i).equals("#main")){
                 storeFuncInList.add(i);
             }
         }
         //store all indeces of function types found in symboltable
         for(j = 0; j<symbolTable.getLast(); j++){
             if( "#func".equals(symbolTable.table.get(j).datatype)){
                 storeFuncInST.add(j);
             }
         }
         
         if(storeFuncInList.isEmpty()!=true){
             for(i=0, j = storeFuncInList.get(i); i<storeFuncInList.size() && j<list.size()-1; j++){
                 //traverse in symbol table, check for char variables, verify if there's ''
                 if(list.get(j).equals("=")){
                     //check first if variable is of type #char, type char have special case: the use of ''
                     int incrementer = 2;
                     int valIncrementer = 1;
                     
                     if(list.get(j+1).equals("'")){
                         incrementer = 4;
                         valIncrementer = 2;
                     }
                     
                     if(list.get(j+incrementer).equals(";")){
                         String scopeOfVar;
                         String dt = null;
                         storeAllDecVar.add(list.get(j-1)); //varName
                         storeAllDecVar.add(list.get(j+valIncrementer)); //actualValue
                         scopeOfVar = ("#func".equals(list.get(storeFuncInList.get(i))))? (list.get(storeFuncInList.get(i)+1)):"main";
                         
                         //traverse in symbolTable to find out the datatype of the variable with declaration statement
                         int k = 0;
                         Boolean stopScan = false;
                         for(; stopScan!=true && k<=symbolTable.getLast(); k++){
                             String scopeWithHash = scopeOfVar;
                             if("main".equals(scopeOfVar)){
                                 scopeWithHash = "#main";
                             }
                             
                             if(symbolTable.table.get(k).scope.equals(scopeWithHash) && symbolTable.table.get(k).tokenValue.equals(list.get(j-1)))
                             {
                                 dt = symbolTable.table.get(k).datatype;
                                 stopScan = true;
                             }
                         }
                         
                         if(dt!=null){
                         
                         storeAllDecVar.add(dt);
                         storeAllDecVar.add("#"+scopeOfVar);
                         if(dt.equals("#char")){
                             charTypeChecker = (list.get(j+2).equals(";"))?false:true;
                             System.err.println("NUM"+ list.get(j));
                         }
                     }
                         else{
                             verifyDeclarationOfVariable = false;
                             this.setUndeclaredVarMessage();
                         }
                  }
                 }
                 if("#main".equals(list.get(j+1)) || "#func".equals(list.get(j+1))){
                     i++;
                 }
             }
             
             System.err.println("\nDEC STATEMENTS: "+storeAllDecVar);
            
            //Type Checking comes in		
 	    if(verifyDeclarationOfVariable!=false){
                     Boolean verifyDT = true;
                     Boolean verifyLR = true;
                     
                     for(i=0; i<storeAllDecVar.size(); i=i+4){
                         
                         verifyDT = (charTypeChecker==false)?false:checkType(storeAllDecVar.get(i+1), storeAllDecVar.get(i+2));
                         verifyLR = LRCheck(storeAllDecVar.get(i), storeAllDecVar.get(i+2));
                         System.err.println(storeAllDecVar.get(i) +" is a "+ storeAllDecVar.get(i+2)+
                                 " variable with a value of "+ storeAllDecVar.get(i+1)+ " STATUS: "+verifyDT + ", LR Check: " + verifyLR);
 
                         if(verifyDT.equals(true)){
                            
                             int k, l;
                             for(k = 0; k<storeAllDecVar.size()-1; k = k+4){
                                  Boolean out2ndForLoop = false;
                                  for(l = 0; l<=symbolTable.getLast() && out2ndForLoop==false; l++){
                                     //String s = storeAllDecVar.get(k)
                                     if(storeAllDecVar.get(k).contains(symbolTable.table.get(l).tokenValue) && storeAllDecVar.get(k+3).contains(symbolTable.table.get(l).scope)){
                                         symbolTable.table.get(l).actualValue = storeAllDecVar.get(k+1);
                                     }
                                 }
                             }
 
                         } else {
 
                             if(verifyLR.equals(true)){
                                 this.setAssignmentMessage();  
                             } else{
                                 this.setLValueMessage();
                             }
                         }
                     }
             }
         }
         
         else{
             this.setMainMessage();
         }
     }
     
     private Boolean checkType(String value, String dataType){
         //sample: x, add, #int
         
         Boolean ret = false;
         
         if( "#int".equals(dataType)){
             try{
                 Integer.parseInt(value);
                 return true;
             }catch(NumberFormatException e) {
                 return false;
             }
         }
         
         else if("#char".equals(dataType)){
              System.err.println(value);
             int charVerifier = 0;
             //ret = (value.length()>1)? false: true;
             try{
                 Integer.parseInt(value);
                 charVerifier = 1;
             }catch(NumberFormatException e) {
                 charVerifier = 0;
             }
             ret = (value.length()>1)? false: true;
         }
         else if("#boolean".equals(dataType)){
             ret = ("false".equals(value)|| "true".equals(value))? true: false;
         }
         return ret;
     }
     
     private Boolean LRCheck(String leftVal, String dataType){
         Boolean ret = false;
         System.err.println("LRCHECK!: " + leftVal + dataType);
         int i;
         //first: traverse symbolTable, and look for the tokenValue that matches leftVal
         for(i = 0; i<symbolTable.getLast()+1 && ret==false; i++){
             if(symbolTable.table.get(i).tokenValue.equals(leftVal) &&symbolTable.table.get(i).datatype.equals(dataType)){
                 ret = true;
             }
         }
         //if loop ended and i is still less than the last index of symbolTable, it means there was a match
         //leftVal is a variable
         return ret;
     }
     
     public void checkFuncCall(){
         //store Function Details in an Array List
         int i, j;
         ArrayList<String> storeAllFuncs = new ArrayList<String>();
         
         
         for(i = 0;i<list.size()-1 && !"#main".equals(list.get(i)); i++){
             if(list.get(i).equals("#func")){
                 storeAllFuncs.add(Integer.toString(i)); //1st: starting index of function in the list
                 storeAllFuncs.add(list.get(i+1)); //Name of Function
                 
                 //count no. of parameters
                 int numOfParam = 0;
                 for(j = i; j<list.size() && !list.get(j).equals(")"); j++){
                     if(list.get(j).equals("#int") || list.get(j).equals("#char") || list.get(j).equals("#boolean")){
                         numOfParam++;
                     }
                 }
                 storeAllFuncs.add(Integer.toString(numOfParam)); // Number of Parameters
             }
         }
         System.err.println("Function Information (by 3): "+storeAllFuncs);
         Boolean verifyDuplication = false;
           
         //check for duplicate function names
         for(i = 0;i<storeAllFuncs.size()-1; i=i+3){
              for(j = i+3;j<storeAllFuncs.size() && verifyDuplication==false; j=j+3){
                if(storeAllFuncs.get(i+1).equals(storeAllFuncs.get(j+1))){
                    verifyDuplication=true;
                    this.setDuplicateFuncNameMessage();
                }
            }
        
         }
         if(verifyDuplication==false){
         ArrayList<String> performFunc = new ArrayList<String>();
        
         //scan list for function calls
         for(i = 0;i<storeAllFuncs.size(); i=i+3){
             
             for(j = 0; j<list.size()-1; j++){
                if(list.get(j).equals(storeAllFuncs.get(i+1))){
                    
                     if(j!=Integer.parseInt(storeAllFuncs.get(i))+1){
                     
                     System.err.println("Function call at: "+j+"="+storeAllFuncs.get(i+1));
                     //storeFuncCalls.add(Integer.toString(j));
                     performFunc = checkFuncCallDetails(storeAllFuncs, j);
                     }
                 }
               else if(list.get(j).equals("#"+storeAllFuncs.get(i+1))){
                    this.setInvalidFuncMessage(); 
                   
                }
             }
         }
       }
     }
     
     private ArrayList<String> checkFuncCallDetails(ArrayList<String> allFuncs, int j){
         int i, countParam=0;
         int flagError = 0;
         ArrayList<String> funcVerify = new ArrayList<String>();
         ArrayList<String> ret = new ArrayList<String>();
         
         for(i=j+1; !")".equals(list.get(i+1)); i++){
             if(!",".equals(list.get(i+1))){
                 countParam++;
                 funcVerify.add(list.get(i+1));
             }
         }
         System.err.println("Parameters of function call: "+funcVerify);
         
         for(i=0;i<allFuncs.size()-1; i=i+3){
             String FuncName=allFuncs.get(i+1);
             if(FuncName.equals(list.get(j))){
                 if(countParam!=Integer.parseInt(allFuncs.get(i+2))){
                     System.err.println("Number of parameters in function call at line "+j+" does not match with function.");
                     this.setParameterMessage();
                     flagError = 1;
                 }
             }
         }
         ArrayList<Integer> storeFuncInList = new ArrayList<Integer>();
         String callingFunc = null;
         
         //store all indeces of function types found in the arraylist of tokens
         for(i = 0;i<list.size();i++){
             if(list.get(i).equals("#func") || list.get(i).equals("#main")){
                 storeFuncInList.add(i);
             }
         }
         storeFuncInList.add(list.size()-1);
         
         for(i = 0; i<storeFuncInList.size()-1; i++){
             if(j>storeFuncInList.get(i) && j<storeFuncInList.get(i+1)){
                 if(list.get(storeFuncInList.get(i)).equals("#func")){
                     callingFunc = list.get(storeFuncInList.get(i)+1);
                 } else{
                     callingFunc = "#main";
                 }
             }
         }
         
         System.err.println("CALLING FUNC : " + callingFunc);
         
         //parameter type matching in function call and function
         if(flagError!=1){
         Boolean paramType = paramTypeMatching(allFuncs, funcVerify, callingFunc,  j);
         
         if(paramType==false){
             this.setFuncParamtypeMismatchMessage();
         }
         else{
             for(i=0; i<funcVerify.size(); i++){
                 for(j=0; j<symbolTable.getLast()+1; j++){   
                     if(symbolTable.table.get(j).tokenValue.equals(funcVerify.get(i)) && symbolTable.table.get(j).scope.equals(callingFunc)){
                         ret.add(Integer.toString(j));
                     }
                 }
             }
           }
         }
         return ret;
     }
     
     public Boolean paramTypeMatching(ArrayList<String> funcHeader, ArrayList<String> funcCallVars, String callingFunc, int callIndex){
         Boolean ret = true;
         int i, j, k;
         ArrayList<String> funcMatchType = new ArrayList<String>();
         
         for(i=0; i<funcHeader.size(); i=i+3){
             if(list.get(callIndex).equals(funcHeader.get(i+1))){
             
                 for(j=Integer.parseInt(funcHeader.get(i))+2; !list.get(j).equals(")"); j++){
                     if(list.get(j).equals("#int") || list.get(j).equals("#char") || list.get(j).equals("#boolean")){
                         funcMatchType.add(list.get(j));
                         funcMatchType.add(list.get(j+1));
                     }
                 }
             }
         }
         for(i=0, k=0; i<funcMatchType.size() && ret==true; i=i+2, k++){
             for(j=0; j<symbolTable.getLast()+1; j++){
                 
                 if(!funcCallVars.get(k).equals(funcMatchType.get(i+1))){
                     ret = false;
                 }
                 
                 else if(symbolTable.table.get(j).tokenValue.equals(funcCallVars.get(k)) && symbolTable.table.get(j).scope.equals(callingFunc)){
                     if(!symbolTable.table.get(j).datatype.equals(funcMatchType.get(i)))
                         ret = false;
                 }
                 
                 
             }
         }
         System.err.println(funcMatchType + "+ "+ funcCallVars+ret);
         return ret;
     }
     
     public String getMessage(){
         return semanticErrorMessage;
     }
     
     private void setAssignmentMessage(){
         semanticErrorMessage = "Invalid assignment statement.";
     }
     
     private void setInvalidFuncMessage(){
         semanticErrorMessage = "Invalid function call.";
     }
     
     private void setParameterMessage(){
         semanticErrorMessage = "Number of parameters in a function call does not match with function's default number of parameters.";
     }
     
     private void setLValueMessage(){
         semanticErrorMessage = "LValue required.";
     }
     
     private void setMainMessage(){
         semanticErrorMessage = "Invalid reserved word.";
     }
     
     private void setDuplicateFuncNameMessage(){
         semanticErrorMessage = "Duplicate Function found.";
     }
     
     private void setUndeclaredVarMessage(){
         semanticErrorMessage = "Undeclared variable used.";
     }
     
     private void setFuncParamtypeMismatchMessage(){
         semanticErrorMessage = "Function Parameter Type Mismatch.";
     }
     
 }
