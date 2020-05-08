 import java.io.*;
 
 import org.antlr.runtime.*;
 import org.antlr.runtime.tree.*;
 import org.antlr.stringtemplate.*;
 
 public class Proj5 {
 	public static void main(String[] args) throws Exception {
         ANTLRInputStream input = new ANTLRInputStream(System.in);
 		LogoJVM1Lexer lexer = new LogoJVM1Lexer(input);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
 		LogoJVM1Parser parser = new LogoJVM1Parser(tokens);
 		LogoJVM1Parser.prog_return r = parser.prog(); // LogoJVM1.g
 
 		FileReader stgFile = new FileReader("LogoST.stg");
         StringTemplateGroup stg = new StringTemplateGroup(stgFile);
         stgFile.close();
 
         CommonTree t = (CommonTree)r.getTree();

        System.out.println( "; " + t.toStringTree() );

         CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
         nodes.setTokenStream(tokens);
         LogoTree walker = new LogoTree(nodes);
 		walker.setTemplateLib(stg);
		LogoTree.prog_return w = walker.prog( parser.numOps, parser.locals ); //LogoTree.g
 
 		StringTemplate st = (StringTemplate)w.getTemplate(); 
         System.out.println(st.toString());
 	}
 }
