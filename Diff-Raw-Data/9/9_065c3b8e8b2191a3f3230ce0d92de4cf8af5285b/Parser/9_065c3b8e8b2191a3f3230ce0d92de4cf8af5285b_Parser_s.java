 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Node;
 import datamodels.Token;
 import datamodels.Tree;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EmptyStackException;
 import java.util.Stack;
 import storage.LookUpTable;
 /**
  *
  * @author charles_yu102
  */
 public class Parser {
     
     // Attributes
     private Stack<Token> tokens;
     public Stack<String> productions;
     private LookUpTable lookUpTable;
     private Boolean errorDetected = false;
     private String errorMessage;
     private Tree parserTree;
     private Node treePtr;
     
     // Constructor
     public Parser(ArrayList<Token> t){
         // Initialize data structures
         this.tokens = new Stack<Token>();
         this.productions = new Stack<String>();
         this.tokens.addAll(t);
         Collections.reverse(this.tokens); //reverse the order for our stack
         this.lookUpTable = new LookUpTable();
         this.productions.push("PROGRAM");
         this.initTree();   
     }
     
     //<editor-fold defaultstate="collapsed" desc="Debugging Methods">
     public void displayTokenStack(){
         System.out.println("Input Stack: ");
         for(int i=tokens.size()-1; i >= 0; i--){
             System.out.println("Token: "+tokens.get(i).getToken()+"\t TokenInfo: "+tokens.get(i).getTokenInfo());
         }
     }
     public void displayTree(Node n){
        
         System.out.print(" " + n.getNodeData());
         ArrayList<Node> temp = n.getNodeChildren();
         for(Node new_n : temp){
            
             displayTree(new_n);
             System.out.println("");
          }
     }
     //</editor-fold>
     
     public String methodLLParser(){
         //this.displayTokenStack();
         do{
             //System.out.println("input stack top: "+tokens.peek().getToken());
             //System.out.println("productions: "+productions+"\n\n");
             try{
                 if(!tokens.peek().getToken().equals(productions.peek())){
                     if(productions.peek().equals("epsilon")){            // Peek of production stack is epsilon
                         adjustTreePtr();
                         productions.pop();
                     } else{                                              
                         if(lookUpTable.isTerminal(productions.peek())){  // Peek of both stacks are terminals and dont match
                             errorDetected = true;
                             errorMessage = "Error found while parsing "+tokens.peek().getToken();
                         } else{                                          // Peek of both stacks dont match and there is a production
                             errorDetected = splitProductionTop();
                         }
                     }
                 } else {                                                // Peek of input stack is terminal and matches production stack
                     adjustTreePtr();
                     tokens.pop();
                     productions.pop();
                 }
             }catch(EmptyStackException ex){ 
                 errorDetected = true;
                 errorMessage = "Error found while parsing code";
             }
         }while(errorDetected == false && !tokens.empty());
 
         if(tokens.empty() && !productions.empty()){
             errorMessage = "Error found while parsing code.";
         } else if(errorDetected == false){
             errorMessage = "Syntax check - Success!";
         }        
         
         return errorMessage;
     }
     
     private boolean splitProductionTop(){
         boolean errorFound = false;
         String prod = lookUpTable.lookUp(productions.peek(), tokens.peek().getToken());
         if(prod.equals("null")){
             errorFound = true;
             errorMessage = "Error found while parsing "+tokens.peek().getToken();
         } else{
             // Check if production top matches tree ptr node
             if(!productions.peek().equals(treePtr.getNodeData())){
                 adjustTreePtr();
             }
             productions.pop();
             // Check for exceptions
             prod = fixExceptions(prod);
             // Parse production
             String[] tNt = prod.split(" ");
             // Add productions to tree and move to leftmost
             treePtr.setNodeChildren(this.formChildren(treePtr, tNt));
             treePtr = treePtr.getNodeChildren().get(0);
             // Push to production stack
             for(int i=tNt.length-1; i>=0 ; i--){
                 productions.push(tNt[i]);
             }
         }
         return errorFound;
     }
     
     private String fixExceptions(String prod){
         String correctProd = "";
         // Try to split production
         String[] prods = prod.split("8");
         // Return based on exception
         if(prods.length == 1){
             correctProd = prods[0];
         } else{
            if(prods[0].equals("ASSIGNMENT_STATEMENT")){ // For OTHER_STATEMENT Exception
                 if(tokens.get(tokens.size()-2).getToken().equals("=")){
                     correctProd = prods[0];
                 } else{
                     correctProd = prods[1];
                 }
             } else if(prods[0].equals("VALUE")){              // For ASSIGNMENT_VALUE Exception
                 if(tokens.get(tokens.size()-2).getToken().equals(";")){
                     correctProd = prods[0];
                 } else{
                     correctProd = prods[1];
                 }
             }
         }
         return correctProd;
     }
     
     //<editor-fold defaultstate="collapsed" desc="Tree Manipulation Methods">
     private void initTree(){
         this.parserTree = new Tree();
         this.treePtr= parserTree.getRoot();
     }
     
     private ArrayList<Node> formChildren(Node parent, String[] prods){
         ArrayList children = new ArrayList<Node>();
         //COMMENTED: System.out.println("\nParent "+ parent.getNodeData());
         for(int i=0; i<prods.length; i++){
             //COMMENTED: System.out.println("Child "+prods[i]);
             children.add(new Node(prods[i], parent));
         }
         return children;
     }
     
     private void adjustTreePtr(){
         boolean validPosition = false;
         while( validPosition == false && !treePtr.getNodeData().equals("PROGRAM")){
             String curProd = treePtr.getNodeData();
             // Check for int, char, string, id exception
             if(curProd.equals("int") || curProd.equals("char") || curProd.equals("string") || curProd.equals("id")){
                 treePtr.setNodeData(tokens.peek().getTokenInfo());
                 curProd = treePtr.getNodeData();
             }
             // Go up to parent
             treePtr = treePtr.getNodeParent();
             // Get current index
             int curNdx = treePtr.getIndexOfChild(curProd);
             // Find next position
             if(curNdx+1 < treePtr.getNumOfChildren()){
                 // Check for "string" exception
                 if(curProd.equals("\"") && productions.size()>1 && !productions.get(productions.size()-2).equals("string")){
                     treePtr = treePtr.getNodeChildren().get(curNdx+2);
                     validPosition = true;
                     if(!productions.get(productions.size()-2).equals("\"")){
                         validPosition = false;
                         treePtr = treePtr.getNodeParent();
                     }
                     // Check for 'char' exception
                 } else if(curProd.equals("'") && productions.size()>1 && !productions.get(productions.size()-2).equals("char")){
                     treePtr = treePtr.getNodeChildren().get(curNdx+2);
                     validPosition = true;
                     if(!productions.get(productions.size()-2).equals("'")){
                         validPosition = false;
                         treePtr = treePtr.getNodeParent();
                     }
                 } else { // Normal next child
                     treePtr = treePtr.getNodeChildren().get(curNdx+1);
                     validPosition = true;
                 }  
             } 
         }
     }
     public Tree getParserTree(){
         return this.parserTree;
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="Naming Conventions">
     //Check if string does not contain any alphabets
     private Boolean isNumeric(String numeric){
         try{ 
             Integer.parseInt(numeric); 
             return true; 
         }catch(Exception ex){ 
             return false; 
         }
     }
     
     //Check if string does not contain any numbers
     private Boolean isAlphabet(String alphabet){
         return false;
     }
     
     //Check if naming convention is correct
     private Boolean namingConvention(String variable){
         String []specialCharacters = new String[]{"~", "!", "@", "#", "$", 
                                                   "%", "^", "&", "*", "(",
                                                   ")", "-", "=", "+", "*",
                                                   "/", "\\", "|", "?", ">",
                                                   "<", ".", ",", "{", "}",
                                                   "[", "]", ";", "`", "'",
                                                   ":", " "};
         Boolean isCorrect = false;
         char firstChar = variable.charAt(0);
         
         //The first character of the identifier must be a letter or underscore
         if(firstChar == '_' || !Character.isDigit(firstChar)){
             for(int counter = 0; counter < specialCharacters.length; counter++){
                 if(variable.contains(specialCharacters[counter])){
                     isCorrect = false;
                     break;
                 }
                 else{
                     isCorrect = true;
                 }
             }
         }
         
         return isCorrect;
     }
     //</editor-fold>
 }
