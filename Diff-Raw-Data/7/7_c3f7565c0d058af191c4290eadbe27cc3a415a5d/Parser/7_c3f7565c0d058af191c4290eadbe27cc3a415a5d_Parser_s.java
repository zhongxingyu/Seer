 package frontend;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Stack;
 import java.util.TreeMap;
 
 import intermediate.*;
 import backend.*;
 
 /**
  * A simple Scheme parser.
  * @author Ronald Mak
  */
 public class Parser
 {
     private Scanner scanner;
     private ArrayList<IntermediateCode> topLevelLists;
     private boolean define;
     private boolean lambda;
     private boolean scope;
     private String functionName;
     private int symbolTableLevel;
     private SymbolTableStack symbolTableStack;
     private Stack<Integer> parenthesisCount;
 
 
     /**
      * Constructor.
      * @param scanner the simple Scheme scanner.
      */
     public Parser(Scanner scanner)
     {
         this.scanner = scanner;
         this.topLevelLists = new ArrayList<IntermediateCode>();
         this.define = false;
         this.lambda = false;
         this.scope = false;
         this.functionName = null;
         this.symbolTableLevel = 1;
     }
     /**
      * The parse method.
      * This version also builds parse trees.
      */
     public void parse()
     {
         Token token;
 
         // Loop to get tokens until the end of file.
         while ((token = nextToken()).getType() != TokenType.END_OF_FILE) {
             TokenType tokenType = token.getType();
 
             if (tokenType == TokenType.LEFT_PAREN) {
                 topLevelLists.add(parseList());
             }
         }
     }
 
     /**
      * Get and return the next token from the scanner.
      * Enter identifiers and symbols into the symbol table.
      * @return the next token.
      */
     private Token nextToken()
     {
 
         Token token = null;
         // TO DO: Need to fix scanner.nextToken(); error
         try {
             token = scanner.nextToken();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         TokenType tokenType = token.getType();
 
         // If "define" is previous token, then current token will be function name like "proc".
         // Then current token will point to an Top level symboltableEntry and put it into the symbolTableStack.
 
           if(define)
         {
             define = false;
             functionName = token.getText();
             SymbolTableEntry entry = new SymbolTableEntry(functionName);
             token.setEntry(entry);
 
             SymbolTable toplevel = new SymbolTable(symbolTableLevel++);
             toplevel.addEntry(functionName, entry);
             symbolTableStack.push(toplevel);
         }
 
         switch (token.getType()) {
             case DEFINE: define = true;
                 break;
 
             case LEFT_PAREN:
                 if (!parenthesisCount.empty()) {
                     parenthesisCount.push(parenthesisCount.pop() + 1);
                 }
                 break;
             case RIGHT_PAREN:
                 if (!parenthesisCount.empty() && parenthesisCount.peek() > 0) {
                     parenthesisCount.push(parenthesisCount.pop() - 1);
 
                     if (parenthesisCount.peek() == 0) {
                         parenthesisCount.pop();
 
                         // TODO: Comment this line out if you want to see parse tree for debugging.
                         // TODO: This should stay in the final version.
                         symbolTableStack.pop();
 
                         /*if (scanner.currentChar() == ')' || scanner.peekChar() == ')') {
                             parseList();
                         } */
                     }
                 }
             case END_OF_FILE:
                return null;
             case LAMBDA: lambda = true;
             case LET:
             case LETSTAR:
             case LETREC:
             {
                symbolTableStack.push(new SymbolTable());
 
                 if (!parenthesisCount.empty()) {
                     parenthesisCount.push(parenthesisCount.pop() - 1);
                 }
 
                 parenthesisCount.push(1);
 
                 // increase symbolTableLevel, create symbol table for scope like Lambda, let, letstar, letrec
                 // then push it into symbolTableStack.
                 scope = true;
                 symbolTableLevel++;
                 SymbolTable table = new SymbolTable(symbolTableLevel);
                 symbolTableStack.push(table);
                 break;
             }
             default:
         }
 
         return token;
     }
 
     /**
      * Parse a list and build a parse tree.
      * @return the root of the tree.
      */
     private IntermediateCode parseList()
     {
         IntermediateCode root = new IntermediateCode();
         IntermediateCode currentNode = null;
 
         // Get the first token after the opening left parenthesis.
         Token token = nextToken();
         TokenType tokenType = token.getType();
 
         // Loop to get tokens until the closing right parenthesis.
         while (tokenType != TokenType.RIGHT_PAREN) {
 
 
             parenthesisCount = new Stack<Integer>();
 
             // Set currentNode initially to the root,
             // then move it down the cdr links.
             if (currentNode == null) {
                 currentNode = root;
             }
             else {
                 IntermediateCode newNode = new IntermediateCode();
                 currentNode.setCdr(newNode);
                 currentNode = newNode;
             }
 
             // Top level's function name in the symbolTableEntry point back to current (lambda) node in the parser tree.
             if(lambda)
             {
                 lambda = false;
                 symbolTableStack.get(1).getEntry(functionName).setIntermediateCode(currentNode);
             }
 
             // link the current node in the parser tree to level 2 symbolTable
             if(scope)
             {
                 scope = false;
                 currentNode.setSymbolTable(symbolTableStack.peek());
             }
 
             // Left parenthesis: Parse a sublist and return the root
             // of the subtree which is set as the car of the current node.
             // Otherwise, set the token as the data of the current node.
             if (tokenType == TokenType.LEFT_PAREN) {
                 currentNode.setCar(parseList());
             }
             else {
                 currentNode.setText(token.getText());
                 currentNode.setType(token.getType());
             }
 
             // Get the next token for the next time around the loop.
             token = nextToken();
             tokenType = token.getType();
         }
 
         return root;  // of the parse tree
     }
 
     public Scanner getScanner() {
         return scanner;
     }
 
     public ArrayList<IntermediateCode> getICodes() {
         return topLevelLists;
     }
 
     public SymbolTableStack getSymTabStack() {
         return symbolTableStack;
     }
 
     public Token currentToken() {
         return scanner.currentToken();
     }
 
 }
