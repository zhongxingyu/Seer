 package name.kazennikov.annotations;
 
 import java.io.File;
 import java.nio.charset.Charset;
 
 import name.kazennikov.annotations.fsm.JapePlusFSM;
 import name.kazennikov.annotations.patterns.JapeNGASTParser;
 import name.kazennikov.annotations.patterns.Phase;
 import name.kazennikov.annotations.patterns.Rule;
 
 import com.google.common.io.Files;
 
 public class TestParseDir {
 	public static void testParseDirs(File f) throws Exception {
 		if(!f.exists())
 			return;
 		
 		if(f.isFile()) {
 			String s = Files.toString(f, Charset.forName("UTF-8"));
 			Phase phase = JapeNGASTParser.parse(s);
 
 
 			for(Rule r : phase.getRules()) {
				JapePlusFSM fsm = new JapePlusFSM();
 				fsm.addRule(r);
 
 				fsm.toDot("test_pre.dot");
 				fsm.determinize();
 				fsm.toDot("test_det.dot");
 				fsm.minimize();
 				fsm.toDot("test_min.dot");
 				fsm.toDot("test.dot");

 			}
 
 		} else if(f.isDirectory()) {
 			for(File file : f.listFiles()) {
 				testParseDirs(file);
 			}
 		}
 	}
 	
 	
 	public static void main(String[] args) throws Exception {
 		testParseDirs(new File("jape/parser"));
 
 	}
 
 }
