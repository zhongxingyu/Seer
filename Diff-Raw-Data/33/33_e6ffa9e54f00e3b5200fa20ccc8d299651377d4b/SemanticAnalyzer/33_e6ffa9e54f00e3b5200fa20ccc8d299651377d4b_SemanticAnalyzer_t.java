 package IntermediateCodeGeneration;
 
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
 
 /**
  * Analizador semantico. Agrega información semantica al parse tree y crea la
  * tabla de simbolos.
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class SemanticAnalyzer {
 
     private Parser parser;
     private SymbolTable symbolTable;
     private ICGenerator ICG;
 
     public SemanticAnalyzer(String input, String output) {
         parser = new Parser(input);
         symbolTable = parser.getSymbolTable();
         ICG = new ICGenerator(output);
     }
 
     public void checkSemantics() throws LexicalException, SyntacticException, SemanticException {
         parser.analize();
         declarationCheck();
         ICG.setup(symbolTable);
         sentencesCheck();
        //symbolTable.printOffsets();
         ICG.generateOutput();
     }
 
     private void declarationCheck() throws SemanticException {
         symbolTable.declarationCheckInheritance();
         symbolTable.declarationCheckReturnType();
         symbolTable.declarationCheckVariables();
         symbolTable.consolidateInheritance();
         symbolTable.consolidateConstructors();
         symbolTable.declarationCheckMainExistence();
     }
 
     private void sentencesCheck() throws SemanticException {
         symbolTable.setICG(ICG);
         symbolTable.sentenceCheck();
     }
 }
