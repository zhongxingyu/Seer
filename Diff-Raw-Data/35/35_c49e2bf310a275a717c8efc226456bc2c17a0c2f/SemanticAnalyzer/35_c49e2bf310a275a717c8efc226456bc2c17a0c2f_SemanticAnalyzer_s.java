 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tarccompiler;
 
 import datamodels.Node;
 import datamodels.SymbolTableModel;
 import datamodels.Tree;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import storage.SymbolTable;
 
 
 public class SemanticAnalyzer {
     
     // Attributes
     private Tree astTree;
     private SymbolTable symbolTable;
     private ArrayList<String> list;
     
     // Constructor
     public SemanticAnalyzer(Tree tree, SymbolTable symTbl, ArrayList<String> list){
         
         this.astTree = tree;
         this.symbolTable = symTbl;
         this.list = list;
         displaySymbolTable();
         storeToken(astTree.getRoot(), list);
         System.err.println(list);
         checkDataType();
         
         //System.err.println(Collections.frequency(list, "id"));
         //System.err.println(symbolTable.table.get(0).tokenValue);
         }
     
     // Methods
     //<editor-fold defaultstate="collapsed" desc="Debugging Methods">
     private void displaySymbolTable(){
         System.out.println("\n\nSymbol Table:");
         System.out.println("token \t\t tokenVal \t\t datatype \t\t scope");
         for(int i=0; i<symbolTable.table.size(); i++){
             SymbolTableModel temp = symbolTable.table.get(i);
             System.out.println(temp.token+" \t\t "+temp.tokenValue+" \t\t "+temp.datatype+" \t\t "+temp.scope);
         }
     }
     //</editor-fold>
     
  // method will return a list of strings containing all terminals: #main, #char, #bool, #func
 void storeToken(Node n, ArrayList<String> s){
 		 //System.out.println("THIS IS NODE N!!!!!!!!" + n.getNodeData());
 		 ArrayList<Node> temp = n.getNodeChildren();
 		 
                  for(Node new_n : temp){
 			if(!new_n.getNodeData().equals("~"))
 				s.add(new_n.getNodeData());
 			
             storeToken(new_n, s);
          } 
 }
     
 //checkMain will return the Node containing #main
 Boolean checkMain(Node root){
 		
 		int i;
                 Boolean verifyFunc = true;
 		ArrayList<String> allTokens = new ArrayList<String>();
 		
 		storeToken(root, allTokens);
                 System.err.println(allTokens);
 		
 		// check if there's main
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
                     }
                     else verifyFunc = true;
 		}
                     
                 }
                 System.err.println(allTokens);
                     
 return verifyFunc;	
 }   
 
 void checkDataType(){
     
                 int i, j;
                 ArrayList<Integer> storeFuncInList = new ArrayList<Integer>();
                 ArrayList<Integer> storeFuncInST = new ArrayList<Integer>();
                // int funcCounter = Collections.frequency(list, "#func");
 
                 //store all indeces of function types found in the arraylist of tokens
                 for(i = 0;i<list.size();i++){
                     if(list.get(i).equals("#func") || list.get(i).equals("#main"))
                         storeFuncInList.add(i);
                 }
                 //store all indeces of function types found in symboltable
                 for(j = 0; j<symbolTable.getLast(); j++){
                     if( "#func".equals(symbolTable.table.get(j).datatype))
                         storeFuncInST.add(j);
                 }
    
                 Boolean stopperFailType = true;
                 Boolean stopperFailLR = true;
                 for(i = 0, j = storeFuncInList.get(i); i<(storeFuncInList.size())-1 
                         && j<storeFuncInList.get(i+1) && stopperFailType==true && stopperFailLR==true; j++){
                     //comes in assignment checking only such as: x = 5; 
                     //if true, it is a plain assignment statement with no operations involved
                     if(list.get(j).equals("=") && list.get(j+2).equals(";"))
                     {
                         //checking of datatype comes in. In function checkType, 
                         //the value to be assigned, and datatype of the variable from symboltable 
                         // to understand more: see this: 
                         //stopper_fail = checkType(list.get(j+1), symbolTable.table.get(i).dataType);
                       // LRCheck() pass nameofVal LRCheck(list.get(j-1), and datatype and scope
                         String scopeOfVar = symbolTable.table.get(i).tokenValue;
                         int k;
                         for(k = 0; symbolTable.table.get(k).scope!=scopeOfVar
                                && symbolTable.table.get(k).tokenValue!=list.get(j-1); k++);
                    stopperFailType = (checkType(list.get(j+1), symbolTable.table.get(k).datatype))?true:false;
                     System.err.println("\n" + list.get(j+1)+symbolTable.table.get(k).datatype+stopperFailType);
                     
                     if(stopperFailType==true)
                         symbolTable.table.get(k).setActualValue(list.get(j+1)); 
                     //if checkType is false, for loop will terminate=
                     
                     }
                     if(j+1==(storeFuncInList.get(i+1))-1)
                         i++;
                  }
                     System.err.println("\n"+storeFuncInList);
                     System.err.println("\n"+storeFuncInST);
 }
 
 Boolean checkType(String value, String dataType){
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
                    ret = (value.length()>1)? false: true;
                 }
                 else if("#boolean".equals(dataType))
                     ret = ("false".equals(value)|| "true".equals(value))? true: false;
 
                 return ret;
 }
 
 Boolean LRcheck(String leftVal, String dataType){
                 Boolean ret = false;
                 int i;
                 //first: traverse symbolTable, and look for the tokenValue that matches leftVal
                 for(i = 0; i<symbolTable.getLast() && (symbolTable.table.get(i).tokenValue==leftVal &&symbolTable.table.get(i).datatype==dataType); i++);
 
                 //if loop ended and i is still less than the last index of symbolTable, it means there was a match
                 //leftVal is a variable
                 if(i<symbolTable.getLast())
                     ret = true;
 
                 return ret;
 }
 //<editor-fold defaultstate="collapsed" desc="Checking-Old">
 //protected Node checkMain(){
 //        
 //       Node ptr, start;
 //       ptr = start = astTree.getRoot();
 //        ArrayList<Node> children;
 //       int i, j = 1;
 //       
 //      */ 
 //       /* for(int val=0;!start.getNodeData().contains("#main");val+=4){
 //       
 //           ArrayList<Node> children = start.getNodeChildren();
 //           for(i=0; i<children.size() && !children.get(i).getNodeData().contains("#main"); i++);
 //           
 //           if(children.get(i).getNodeData().contains("#main")){
 //               ptr = children.get(i);
 //           }
 //       }*/
 //       //as long as #main's not found, PROBLEM 1: NEED ANOTHER STOPPER in case of end of tree
 //       
 //       /* while(!ptr.getNodeData().contains("#main") && !ptr.getNodeData().contains("}")){
 //           
 //       	children = start.getNodeChildren();
 //        
 //        for(i=0, ptr = children.get(i) ; i<children.size() && !ptr.getNodeData().contains("#main"); i++, ptr = children.get(i));
 //        */
 //       /*  switch(j++){
 //            case 3: start = children.get(4); break;
 //            case 4: start = children.get(1); break;
 //            case 7: start = children.get(2); break;
 //            default: start = children.get(0);
 //        } */
 //        
 //        /* if(j==3)
 //            start = children.get(4);
 //        else if(j==4)
 //            start = children.get(1);
 //        else if(j==7)
 //            start = children.get(2);
 //        else start = children.get(0); 
 //        
 //        }
 //        
 //        
 //       
 //       return ptr;
 //    
 //    }*/
 //    
 //   /*  public class checkDeclarations{
 //        
 //     public ArrayList<String> declarationsInsideMain(){
 //         ArrayList<String> verify = null;
 //         Node ptr, mainNode;
 //         ptr = mainNode = checkMain();
 //         ArrayList<Node> children;
 //         int i, j, k=1;
 //         int counter = 1;
 //         
 //         while(counter!=0){
 //        
 //         for(i=0, children = mainNode.getNodeChildren(), ptr = children.get(i); i<children.size()
 //                 && counter!=0; i++, ptr = children.get(i)){
 //             
 //             
 //          switch(k++){
 //                    case 3: mainNode = children.get(4); break;
 //                    case 4: mainNode = children.get(1); break;
 //                    case 7: mainNode = children.get(2); break;
 //                    default: mainNode = children.get(0);
 //            }
 //          
 //             if(ptr.getNodeData().contains("{") || ptr.getNodeData().contains("}"))
 //                 counter = 0;
 //                     
 //             else{
 //                 for(j=0; j<symbolTable.getLast() && ((ptr.getNodeData() == null ? symbolTable.table.get(j).datatype != null : !ptr.getNodeData().equals(symbolTable.table.get(j).datatype))
 //                         && (children.get(i+1).getNodeData() == null ? symbolTable.table.get(j).tokenValue != null : !children.get(i+1).getNodeData().equals(symbolTable.table.get(j).tokenValue))); j++);
 //                // UNDER CONSTRUCTION
 //                /*  if(symbolTable.table.get(j).datatype=="#func")
 //                       counter = 0; */
 //             }
 //           }
 //         
 //         }
 //         return verify;
 //     }   
 //        
 //     public void declarationsInsideFunc(){
 //         
 //     }
 //     
 //     // check if the value of the variable is appropriate (according to its datatype)
 //     public Boolean checkType(Node X, Node Y){
 //         Boolean checker = true;
 //         int i;
 //         String expected = null;
 //         
 //         for(i=0; i<symbolTable.getLast() && X.getNodeData()!=symbolTable.table.get(i).tokenValue; i++);
 //         if(i<symbolTable.getLast())
 //                expected = symbolTable.table.get(i).datatype;
 //         
 //         if("#int".equals(expected)){
 //        // j = Integer.parseInt(Y.getNodeData());
 //         
 //             if(!Y.getNodeData().matches("[+-]?\\d*(\\.\\d+)?"))
 //                 checker = false;
 //         }
 //         
 //         else if("#char".equals(expected)){
 //                 if(X.getNodeData().length()>1)
 //                     checker = false;
 //         }
 //         
 //         if(!checker){
 //                 System.err.println("Type mismatch: assignment to " + X.getNodeData()+
 //              " should be " + expected + " expression");
 //         }
 //         
 //         return checker;
 //     }
 //    }
 //    
 //    /* public class checkDeclarations{
 //        
 //        protected ArrayList<Binding> declarations;
 //        int last;
 //        
 //        public checkDeclarations(){
 //            this.declarations = new ArrayList<Binding>();
 //            this.last = -1;
 //        }
 //        void checkMain(SemanticAnalyzer SA){
 //            for(i=mainStart, stop=0; i<SA.symbolTable.getLast(); i++){
 //               String val = SA.symbolTable.table.get(i).tokenValue;
 //                    if(val.equals("#int"))
 //                        insert("#int", SA.symbolTable.table.get(i+1).tokenValue, "-1");
 //            
 //                    else if(val.equals("#char"))
 //                        insert("#int", SA.symbolTable.table.get(i+1).tokenValue, "-1");
 //                    
 //                    else insert("#int", SA.symbolTable.table.get(i+1).tokenValue, "-1");
 //                    }
 //        
 //            }
 //        
 //        
 //        public void insert(String datatype, String name, String actualValue){
 //            Binding row = new Binding(datatype, name, actualValue);
 //            this.declarations.add(row);
 //            this.last++;
 //    }
 //    } */
 //</editor-fold>
 
 }
