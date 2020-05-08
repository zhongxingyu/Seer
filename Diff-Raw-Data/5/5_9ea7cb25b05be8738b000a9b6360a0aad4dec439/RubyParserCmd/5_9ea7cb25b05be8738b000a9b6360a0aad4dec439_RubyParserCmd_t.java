 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jruby.ast.Node;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.eclipse.shams.resources.ShamFile;
 import org.rubypeople.rdt.internal.core.DefaultWorkingCopyOwner;
 import org.rubypeople.rdt.internal.core.RubyProject;
 import org.rubypeople.rdt.internal.core.RubyScript;
 import org.rubypeople.rdt.internal.core.RubyScriptElementInfo;
 import org.rubypeople.rdt.internal.core.RubyScriptStructureBuilder;
import org.rubypeople.rdt.internal.core.SourceFolder;
import org.rubypeople.rdt.internal.core.SourceFolderRoot;
 import org.rubypeople.rdt.internal.core.parser.RdtWarnings;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 
 public class RubyParserCmd {
 
     private File root;
     private boolean verbose;
 
     private List okFiles = new ArrayList();
     private List syntaxErrorFiles = new ArrayList();
     private int repeat = 1;
     private Map elements = new Hashtable()  ; 
 
     public RubyParserCmd(List args) {
         if (!parseArgs(args)){
             System.err.println("java ... " + getClass().getName() + "[-v] <dirOrFile>");
             System.exit(-1);
         }
          
     }
 
     private boolean parseArgs(List args) {
         args = new ArrayList(args);
         if (args.size() < 1)
             return false;
         
         if (args.get(0).equals("-v")) {
             args.remove(0);
             verbose = true;
         }
         
         if (args.size() < 1)
             return false;
         
         if (args.get(0).equals("-n")) {
             if (args.size() < 2)
                 return false;
             repeat = Integer.parseInt((String) args.get(1));
             args.remove(0);
             args.remove(0);
         }
         
 
         if (args.size() < 1)
             return false;
         
         root = new File((String) args.get(0));
         return true;
     }
 
     public void run() throws FileNotFoundException {
     	
         parseTree(root);
         if (verbose) {
             for (Iterator iter = syntaxErrorFiles.iterator(); iter.hasNext();) {
                 String fileName = (String) iter.next();
                 System.err.println(fileName);
             }
         }
         System.err.println(syntaxErrorFiles.size() + " Errors; " 
                 + okFiles.size()+" OK");
         
         System.err.println("NewElements: " + elements.size()) ;
     }
     
     private void parseTree(File file) throws FileNotFoundException {
     	
 		if (!file.isDirectory()) {
 			if (file.getName().endsWith(".rb"))
 				for (int i = 0; i < repeat; i++) {
 					parseOneFile(file.getAbsolutePath());
 				}
 		} else {
 			File[] files = file.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				parseTree(files[i]);
 			}
 		}
 
 	}
 
     private void parseOneFile(String file) {
         RubyParser parser = new RubyParser(new RdtWarnings());
         try {
             Node node = parser.parse(new ShamFile(file), new FileReader(file));
 			RubyScriptElementInfo unitInfo = new RubyScriptElementInfo() ; 
			RubyScript script = new RubyScript(null, file, DefaultWorkingCopyOwner.PRIMARY ) ;
 			RubyScriptStructureBuilder visitor = new RubyScriptStructureBuilder(script, unitInfo, elements);
 			if (node != null) {
 				node.accept(visitor);
 			}
 			else {
 				System.out.println("Node is null for : " + file) ;
 				syntaxErrorFiles.add(file);
 				return ;
 			}
         } catch (SyntaxException e) {
             syntaxErrorFiles.add(file);
             return ;
         } catch (Exception e) {
             System.out.println(e.getClass().getName() + "->" + e.getMessage());
             syntaxErrorFiles.add(file);
             return ;
         }
         okFiles.add(file);
     }
     
 
     
     public static void main(String[] args) throws FileNotFoundException {
         new RubyParserCmd(Arrays.asList(args)).run();
     }
 }
