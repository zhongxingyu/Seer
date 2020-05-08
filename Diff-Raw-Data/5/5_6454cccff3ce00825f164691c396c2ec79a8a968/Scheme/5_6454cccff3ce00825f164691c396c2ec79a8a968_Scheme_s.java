 import backend.Backend;
 import frontend.Parser;
 import frontend.Scanner;
 import frontend.Source;
 import intermediate.IntermediateCode;
 import intermediate.IntermediateCode;
 import intermediate.SymbolTableStack;
 
 import java.io.IOException;
 
 public class Scheme {
     private Parser parser;
     private Source source;
     private IntermediateCode intermediateCode;
     private SymbolTableStack symbolTableStack;
     private Backend backend;
 
     public Scheme(String filePath) {
         try {
             source = new Source(filePath);
             parser = new Parser(new Scanner(source));
             backend = new Backend();
 
             parser.parse();
             source.close();
 
             intermediateCode = parser.getICode();
             symbolTableStack = parser.getSymTabStack();
             backend.process(intermediateCode, symbolTableStack);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String [] args) {
        // Do no error checking. Assume the filename is correct and syntax is correct
         Scheme scheme = new Scheme(args[0]);
     }
 }
