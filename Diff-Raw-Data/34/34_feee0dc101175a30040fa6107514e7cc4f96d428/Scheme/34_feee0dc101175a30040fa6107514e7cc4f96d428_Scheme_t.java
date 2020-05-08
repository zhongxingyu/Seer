 import backend.Backend;
 import frontend.Parser;
 import frontend.Scanner;
 import frontend.Source;
 import intermediate.IntermediateCode;
 import intermediate.SymbolTableStack;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Scheme {
     private Parser parser;
     private Source source;
    private ArrayList<IntermediateCode> intermediateCodes;
     private SymbolTableStack symbolTableStack;
     private Backend backend;
 
     public Scheme(String filePath) {
         try {
             source = new Source(filePath);
             parser = new Parser(new Scanner(source));
             backend = new Backend();
 
             parser.parse();
             source.close();
 
             intermediateCodes = parser.getICodes();
             symbolTableStack = parser.getSymTabStack();
             backend.process(intermediateCodes, symbolTableStack);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public static void main(String [] args) {
         if (args.length != 1) {
             System.out.println("Usage: Scheme <filepath>");
             return;
         }
 
         Scheme scheme = new Scheme(args[0]);
     }
 }
