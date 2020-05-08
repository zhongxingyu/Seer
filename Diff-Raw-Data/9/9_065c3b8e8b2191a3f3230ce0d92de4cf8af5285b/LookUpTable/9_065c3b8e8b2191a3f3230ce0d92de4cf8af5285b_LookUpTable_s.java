 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package storage;
 
 
 /**
  *
  * @author Fealrone Alajas
  */
 public class LookUpTable {
     // Attributes
     public String[] terminals, nonTerminals;
     private String[][] table;
     
     // Constructor
     public LookUpTable(){
         this.initializeNonTerminals();
         this.initializeTerminals();
         this.populateLookUpTable();
         //this.displayLookUpTable();
         
     }
     
     //Look up our data in our LookUpTable
     @SuppressWarnings("empty-statement")
     public String lookUp(String nonTerminal, String terminal){
         int count_terminal, count_nonTerminal;
         
         //get the index for our given terminal and nonTerminal
         for(count_terminal = 0; count_terminal < this.terminals.length && !terminal.equals(this.terminals[count_terminal]); count_terminal++);
         for(count_nonTerminal = 0; count_nonTerminal < this.nonTerminals.length && !nonTerminal.equals(this.nonTerminals[count_nonTerminal]); count_nonTerminal++);
         
         if(count_terminal == this.terminals.length || count_nonTerminal == this.nonTerminals.length) {
             return "2D Array out of bounds";
         }
         else {
             return table[count_nonTerminal][count_terminal];
         }
     }
     
     //Initialize our column header
     private void initializeTerminals(){
         terminals = new String[]{"id", "int", "char", "#main", "#func",
                                  "if", "else", "end", "while", "#int",
                                  "#char", "#boolean", "+", "-", "*",
                                  "/", "%", "<", ">", "<=", ">=", "==",
                                  "!=", "&", "|", "=", "(", ")", "{",
                                  "}", ";", ",", "'", "#getInt", "#getChar",
                                  "#getBoolean", "#puts", "true", "false", "\""};
     }
     
     //Initialize our row header
     private void initializeNonTerminals(){
         nonTerminals = new String[]{"PROGRAM", "MAIN", "FUNCTION_LIST",
                                     "FUNCTION", "PARAMETER_LIST", 
                                     "PARAMETER_OPTION", "PARAMETER", 
                                     "DATATYPE", "STATEMENT_LIST",                                     
                                     "DECLARATION_LIST", "DECLARATION",
                                     "STATEMENT", "OTHER_STATEMENT", 
                                     "ITERATION_STATEMENT", "CONDITIONAL_LIST",                                                                       
                                     "CONDITIONAL_OPTION", "CONDITION", 
                                     "VALUE", "BOOLEAN", "RELATIONAL_OPERATOR", 
                                     "LOGICAL_OPERATOR", "ASSIGNMENT_STATEMENT",
                                     "ASSIGNMENT_VALUE", "ARITHMETIC_CHOICE", 
                                     "VARNUM", "ARITHMETIC_OPERATOR", 
                                     "IF_ELSE_STATEMENT", "ELSE_PART", 
                                     "FUNCTION_CALL", "CALL_PARAM_LIST", 
                                     "CALL_PARAM_OPTION", "INPUT", "OUTPUT","OUTPUT_OPTION"};
     }
     
     //Populate data for our LookUpTable
     private void populateLookUpTable(){
         table = new String[][]{
                         //PROGRAM
                         {"null", "null", "null", "MAIN", "FUNCTION_LIST MAIN", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //MAIN
                         {"null", "null", "null", "#main ( ) { STATEMENT_LIST }", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //FUNCTION_LIST
                         {"epsilon", "epsilon", "epsilon", "epsilon", "FUNCTION FUNCTION_LIST",
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon",
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon"},
                         
                         //FUNCTION
                         {"null", "null", "null", "null", "#func id ( PARAMETER_LIST ) { STATEMENT_LIST }",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //PARAMETER_LIST
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "PARAMETER PARAMETER_OPTION", 
                          "PARAMETER PARAMETER_OPTION", "PARAMETER PARAMETER_OPTION", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "epsilon", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //PARAMETER_OPTION
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "epsilon", "null", "null", 
                          "null", ", PARAMETER PARAMETER_OPTION ", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //PARAMETER
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "DATATYPE id", 
                          "DATATYPE id", "DATATYPE id", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //DATATYPE
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "#int", 
                          "#char", "#boolean", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //STATEMENT_LIST
                         {"STATEMENT", "null", "null", "null", "null",
                          "STATEMENT", "null", "null", "STATEMENT", "DECLARATION_LIST STATEMENT", 
                          "DECLARATION_LIST STATEMENT", "DECLARATION_LIST STATEMENT", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "epsilon", 
                          "null", "null", "null", "STATEMENT", "STATEMENT", 
                          "STATEMENT", "STATEMENT", "null", "null", "null"},
                         
                         //DECLARATION_LIST
                         {"epsilon", "epsilon", "epsilon", "epsilon", "epsilon",
                          "epsilon", "epsilon", "epsilon", "epsilon", "DECLARATION DECLARATION_LIST", 
                          "DECLARATION DECLARATION_LIST", "DECLARATION DECLARATION_LIST", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon",
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon",
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon"},
                         
                         //DECLARATION
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "DATATYPE id ;", 
                          "DATATYPE id ;", "DATATYPE id ;", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //STATEMENT
                         {"OTHER_STATEMENT STATEMENT", "epsilon", "epsilon", "epsilon", "epsilon",
                          "OTHER_STATEMENT STATEMENT", "epsilon", "epsilon", "OTHER_STATEMENT STATEMENT", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "epsilon", "epsilon", 
                          "epsilon", "epsilon", "epsilon", "OTHER_STATEMENT STATEMENT", "OTHER_STATEMENT STATEMENT", 
                          "OTHER_STATEMENT STATEMENT", "OTHER_STATEMENT STATEMENT", "epsilon", "epsilon", "epsilon"},
                         
                         //OTHER_STATEMENT
                         {"ASSIGNMENT_STATEMENT8FUNCTION_CALL", "null", "null", "null", "null",
                          "IF_ELSE_STATEMENT", "null", "null", "ITERATION_STATEMENT", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "INPUT", "INPUT", 
                          "INPUT", "OUTPUT", "null", "null", "null"},
                         
                         //ITERATION_STATEMENT
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "while ( CONDITIONAL_LIST ) STATEMENT end", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //CONDITIONAL_LIST
                         {"CONDITION CONDITIONAL_OPTION", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "CONDITION CONDITIONAL_OPTION", "CONDITION CONDITIONAL_OPTION","null"},
                         
                         //CONDITIONAL_OPTION
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "LOGICAL_OPERATOR CONDITION CONDITIONAL_OPTION", "LOGICAL_OPERATOR CONDITION CONDITIONAL_OPTION", 
                          "null", "null", "epsilon", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //CONDITION
                         {"id RELATIONAL_OPERATOR VALUE", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "BOOLEAN", "BOOLEAN", "null"},
                         
                         //VALUE
                         {"id", "int", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "' char '", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //BOOLEAN
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "true", "false", "null"},
                         
                         //RELATIONAL_OPERATOR
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "<", ">", "<=", 
                          ">=", "==", "!=", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //LOGICAL_OPERATOR
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "&", "|",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //ASSIGNMENT_STATEMENT
                         {"id = ASSIGNMENT_VALUE ;", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //ASSIGNMENT_VALUE
                         {"VALUE8VARNUM ARITHMETIC_CHOICE", "VALUE8VARNUM ARITHMETIC_CHOICE", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "VALUE", "null", "null", 
                         "null", "null", "null", "null", "null"},
                         
                         //ARITHMETIC_CHOICE
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "ARITHMETIC_OPERATOR VARNUM ARITHMETIC_CHOICE", "ARITHMETIC_OPERATOR VARNUM ARITHMETIC_CHOICE", "ARITHMETIC_OPERATOR VARNUM ARITHMETIC_CHOICE", 
                          "ARITHMETIC_OPERATOR VARNUM ARITHMETIC_CHOICE", "ARITHMETIC_OPERATOR VARNUM ARITHMETIC_CHOICE", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "epsilon", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null"},
                         
                         //VARNUM
                         {"id", "int", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //ARITHMETIC OPERATOR
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "+", "-", "*",
                          "/", "%", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //IF_ELSE_STATEMENT
                         {"null", "null", "null", "null", "null",
                          "if ( CONDITIONAL_LIST ) STATEMENT ELSE_PART end", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //ELSE_PART
                         {"null", "null", "null", "null", "null",
                          "null", "else STATEMENT", "epsilon", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //FUNCTION_CALL
                         {"id ( CALL_PARAM_LIST ) ;", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //CALL_PARAM_LIST
                         {"VALUE CALL_PARAM_OPTION", "VALUE CALL_PARAM_OPTION", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "epsilon", "null", "null", 
                          "null", "null", "VALUE CALL_PARAM_OPTION", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //CALL_PARAM_OPTION
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "epsilon", "null", "null", 
                          "null", ", VALUE CALL_PARAM_OPTION", "null", "null", "null",
                          "null", "null", "null", "null", "null"},
                         
                         //INPUT
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "#getInt ( id ) ;", "#getChar ( id ) ;",
                          "#getBoolean ( id ) ;", "null", "null", "null", "null"},
                         
                         //OUTPUT
                         {"null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "#puts ( OUTPUT_OPTION ) ;", "null", "null", "null"},
                         
                         //OUTPUT OPTION
                         {"id", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "null", 
                          "null", "null", "null", "null", "null",
                          "null", "null", "null", "null", "\" string \""},
                     };
     }
     
     // Check if production is a terminal
     public boolean isTerminal(String prod){
         boolean check = false;
         for(int i=0; check == false && i<this.terminals.length; i++){
             if(prod.equals(this.terminals[i])){
                 check = true;
             }
         }
         return check;
     }
     
     //Display our LookUpTable
     private void displayLookUpTable(){
         System.out.println("____________________________");
         for(int a = 0; a < this.nonTerminals.length; a++){
             for(int b = 0; b < this.terminals.length; b++){
                 System.out.print(" "+table[a][b]);
             }
             System.out.println();
         }
         System.out.println("____________________________");
     }
 }
