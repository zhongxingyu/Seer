 package org.geworkbench.engine.cascript;
 
 import antlr.debug.misc.ASTFrame;
 import antlr.CommonAST;
 import antlr.TokenStreamException;
 import antlr.RecognitionException;
 
 import java.io.DataInputStream;
 import java.io.StringBufferInputStream;
 
 /**
  * @author Behrooz Badii
  * @author John Watkinson
  */
 public class CasEngine {
 
     public static void runScript(String scriptText) throws TokenStreamException, RecognitionException {
         DataInputStream input = new DataInputStream(new StringBufferInputStream(scriptText));
         // Create the lexer and parser and feed them the input
         CASLexer lexer = new CASLexer(input);
         CASParser parser = new CASParser(lexer);
         parser.program(); // "file" is the main rule in the parser
        //if there are parsing errors, do not interpret
        if ( lexer.nr_error > 0 || parser.nr_error > 0 ) {
            throw new CasException( "Parsing errors found. Stop." );
        }
         // Get the AST from the parser
         CommonAST parseTree = (CommonAST) parser.getAST();
 
         // Print the AST in a human-readable format
         //System.out.println(parseTree.toStringList());
 
         // Open a window in which the AST is displayed graphically
         ASTFrame frame = new ASTFrame("AST from the CAS parser", parseTree);
         frame.setVisible(true);
 
         //walk this tree for interpretation
         CASWalker walk = new CASWalker();
         walk.walkme(parseTree);
 
     }
 }
