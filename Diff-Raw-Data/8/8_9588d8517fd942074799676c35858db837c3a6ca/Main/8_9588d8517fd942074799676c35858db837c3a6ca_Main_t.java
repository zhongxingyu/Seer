 package IntermediateCodeGeneration;
 
 /**
  * CLI para el analizador semantico
  *
  * @author Ramiro Agis
  * @author Victoria Martínez de la Cruz
  */
 public class Main {
 
     /**
      * Constructor de la clase Main.
      *
      * @param args
      */
     public static void main(String[] args) {
         if ((args.length != 1) && (args.length != 2)) {
             System.err.println("Cantidad de argumentos inválida.");
            System.err.println("Uso: java -jar Minijava.jar <IN_FILE> <OUT_FILE>");
             return;
         }
         
         String outputFile;
         if (args.length == 1) {
            outputFile = args[0] + ".CeIASM.txt";
         } else {
             outputFile = args[1];
         }
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(args[0], outputFile);
         try {
             semanticAnalyzer.checkSemantics();
         } catch (SemanticException | SyntacticException | LexicalException exc) {
             System.err.println(exc);
         }
     }
 }
