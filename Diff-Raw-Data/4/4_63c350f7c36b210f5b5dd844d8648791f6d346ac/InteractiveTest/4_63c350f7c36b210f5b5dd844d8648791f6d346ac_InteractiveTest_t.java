 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.examples.test;
 
import java.io.File;
 import java.io.StringReader;
 import java.lang.reflect.Field;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.modelcc.examples.language.simplearithmeticexpression.Expression;
 import org.modelcc.io.ModelReader;
 import org.modelcc.io.java.JavaModelReader;
 import org.modelcc.language.LanguageSpecification;
 import org.modelcc.language.factory.LanguageSpecificationFactory;
 import org.modelcc.lexer.lamb.Lamb;
 import org.modelcc.lexer.lamb.LexicalGraph;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.lexer.recognizer.regexp.RegExpPatternRecognizer;
 import org.modelcc.metamodel.Model;
 import org.modelcc.parser.fence.FenceConstraintEnforcer;
 import org.modelcc.parser.fence.FenceGrammarParser;
 import org.modelcc.parser.fence.ParsedGraph;
 import org.modelcc.parser.fence.SyntaxGraph;
 
 /**
  * Interactive model-based language specification test.
  * @author elezeta
  */
 public class InteractiveTest {
    
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
  
     /**
      * Main class.
      * @param args the arguments.
      */
     public static void main(String args[]) {
    	System.setProperty("java.library.path", "lib/lwjgl-2.9.0/native/macosx"+File.pathSeparatorChar+"lib/lwjgl-2.9.0/native/windows"+File.pathSeparatorChar+"lib/lwjgl-2.9.0/native/linux"+File.pathSeparatorChar+"ModelCCExamples_lib"+File.pathSeparatorChar+System.getProperty("java.library.path"));
     	Field fieldSysPath;
 		try {
 			fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
 	    	fieldSysPath.setAccessible( true );
 	    	fieldSysPath.set( null, null );
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
         if (args.length>0) {
             if (args[0].equals("bench")) {
                 LanguageSpecification ls;
                 Lamb lamb = new Lamb();
                 FenceGrammarParser fencegp = new FenceGrammarParser();
                 FenceConstraintEnforcer fencece = new FenceConstraintEnforcer();
                 
                 try {
                     ModelReader jmr = new JavaModelReader(Expression.class);
                     Model m = jmr.read();
                     LanguageSpecificationFactory lsf = new LanguageSpecificationFactory();
                     ls = lsf.create(m);
                 } catch (Exception ex) {
                     Logger.getLogger(InteractiveTest.class.getName()).log(Level.SEVERE, null, ex);
                     return;
                 }
                 
                 int jmax = 2;
 
                 String inputs[] = {"3+5",
                                    "2+4+3+2+3+4",
                                    "1+4+5+6+7+8+9+7+6+4+3+2+5+6+34+67+7+2+4+6",
                                    "10*4+5-22+(0.6*52-22)+10*4+5-22+(0.6*52-22)+10*4+5-22+(0.6*52-22)+10*4+5-22+(0.6*52-22)+10*4+5-22+(0.6*52-22)+10*4+5-22+(0.6*52-22)+10*4+5-22+(0.6*52-22)",
                                    "1+2+3+4+5+6+7+8+9+1+2+3+4+5+6+7+8+9+1+2+3+4+5+6+7+8+9+1+2+3+4+5+6+7+8+9+1+2+3+4+5+6+7+8+9+1+2+3+4+5+6+7+8+9+1+2+3+4+5+6+7+8+9+1+2+3+4+5+6+7+8+9"};
                 
                 long tlex = 0;
                 long tpar = 0;
                 long texp = 0;
                 if (jmax == 1)
                     jmax = 3;
                 for (int i = 0;i < inputs.length;i++) {
                     for (int j = 0;j < jmax;j++) {
                         String input = inputs[i];
                         long t0 = System.currentTimeMillis();
                         LexicalGraph lg = lamb.scan(ls.getLexicalSpecification(),null,new StringReader(input));
                         long t1 = System.currentTimeMillis();
                         ParsedGraph pg = fencegp.parse(ls.getSyntacticSpecification().getGrammar(), lg);
                         long t2 = System.currentTimeMillis();
                         SyntaxGraph sg = fencece.enforce(ls.getSyntacticSpecification().getConstraints(), pg);
                         long t3 = System.currentTimeMillis();
                         if (sg.getRoots().size() != 1) {
                             System.out.println("ERROR: interpretations = "+sg.getRoots().size());
                             System.exit(-1);
                         }
                         else {
                             if (j > 0) {
                                 tlex += t1-t0;
                                 tpar += t2-t1;
                                 texp += t3-t2;
                             }
                             if (j == jmax-1) {
                                 System.out.println("INPUT: "+input);
                                 System.out.println("Lexical analysis: "+(t1-t0));
                                 System.out.println("         Parsing: "+(t2-t1));
                                 System.out.println("       Expansion: "+(t3-t2));
                                 System.out.println("");
                             }
                         }
                     }
                 }
                 
                 System.out.println("");
                 System.out.println("TOTAL:");
                 System.out.println("Lexical analysis: "+tlex/(jmax-1));
                 System.out.println("         Parsing: "+tpar/(jmax-1));
                 System.out.println("       Expansion: "+texp/(jmax-1));
                 
                 
                 
             }
             else if (args[0].equals("test")) {
                 LanguageSpecification ls;
                 Lamb lamb = new Lamb();
                 FenceGrammarParser fencegp = new FenceGrammarParser();
                 FenceConstraintEnforcer fencece = new FenceConstraintEnforcer();
                 
                 try {
                     ModelReader jmr = new JavaModelReader(Expression.class);
                     Model m = jmr.read();
                     LanguageSpecificationFactory lsf = new LanguageSpecificationFactory();
                     ls = lsf.create(m);
                 } catch (Exception ex) {
                     Logger.getLogger(InteractiveTest.class.getName()).log(Level.SEVERE, null, ex);
                     return;
                 }
                 
                 long tlex = 0;
                 long tpar = 0;
                 long texp = 0;
                 String input = "2+2";
                 long t0 = System.currentTimeMillis();
                 LexicalGraph lg = lamb.scan(ls.getLexicalSpecification(),null,new StringReader(input));
                 long t1 = System.currentTimeMillis();
                 ParsedGraph pg = fencegp.parse(ls.getSyntacticSpecification().getGrammar(), lg);
                 long t2 = System.currentTimeMillis();
                 SyntaxGraph sg = fencece.enforce(ls.getSyntacticSpecification().getConstraints(), pg);
                 long t3 = System.currentTimeMillis();
                 if (sg.getRoots().size() != 1) {
                     System.out.println("ERROR: interpretations = "+sg.getRoots().size());
                     System.exit(-1);
                 }
                 else {
                         tlex = t1-t0;
                         tpar = t2-t1;
                         texp = t3-t2;
                 }
                 
                 System.out.println("");
                 System.out.println("TOTAL:");
                 System.out.println("Lexical analysis: "+tlex);
                 System.out.println("         Parsing: "+tpar);
                 System.out.println("       Expansion: "+texp);
                 
                 
                 
             }
             else {
                 System.err.println("");
                 System.err.println("Invalid LANGUAGEID.");
                 main(new String[]{});
             }            
         }
         else {
             ModelCCExamplesWindow win = new ModelCCExamplesWindow();
             win.setVisible(true);
         }
     }
 }
