 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import java.io.*;
 import org.antlr.runtime.*;
 
 
public class TandemTest
 {
     public static void main(String args[])
     {
        try
        {
             CharStream input = new ANTLRFileStream(args[0]);
             WateredDownTanGLexer lexer = new WateredDownTanGLexer(input);
 
             TokenStream ts = new CommonTokenStream(lexer);
             WateredDownTanGParser parse = new WateredDownTanGParser(ts);
             parse.tanG();
         }
         catch(Throwable t)
         {
             System.out.println("Exception: "+t);
             t.printStackTrace();
         }
     }
 }
