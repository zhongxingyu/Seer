 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 import java.io.FileReader;
 
 public class LinAlg {
 
   public static String generate( String input, String templateFile ) 
   throws Exception {
       CharStream is = new ANTLRStringStream( input );
       return generate( is, templateFile );
   }
 
   public static String generate( String input ) 
   throws Exception {
       CharStream is = new ANTLRStringStream( input );
       return generate( is );
   }
 
   public static String generate( CharStream input ) throws Exception {
     return generate( input, "Python.stg");
   }
 
   public static String generate( CharStream input, String templateFile ) throws Exception {
     LinAlgExprLexer lex = new LinAlgExprLexer(input);
     CommonTokenStream tokens = new CommonTokenStream(lex);
     // System.out.println("tokens="+tokens);
     LinAlgExprParser parser = new LinAlgExprParser(tokens);
    LinAlgExprParser.function_return r = parser.function();
    // LinAlgExprParser.body_return r = parser.body();
 
     CommonTree t = (CommonTree) r.getTree();
     System.out.println( t.toStringTree() ); // Display parse tree    
     CommonTreeNodeStream nodes = new CommonTreeNodeStream( t );
     nodes.setTokenStream( tokens );
 
     FileReader groupFileR = new FileReader( templateFile );
     StringTemplateGroup templates = new StringTemplateGroup( groupFileR );
     groupFileR.close();
 
     ExprGen walker = new ExprGen( nodes );
     walker.setTemplateLib( templates );
    ExprGen.function_return ret = walker.function();
    // ExprGen.body_return ret = walker.body();
     return ret.getTemplate().toString();
   }
 }
