 package scannergenerator;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 public class ScannerGeneratorTest {
 
 	@Test
 	public void fullTest() {
 		List<String> tests = new ArrayList<String>();
 
 		RDPControl rdpc = new RDPControl();
 
 		tests.add("[A-C^D-V]IN[A-Z]");
 		tests.add("[a-zA-Z]");
 		tests.add("[a-zA-Z0-9]");
 		tests.add("[a-zA-Z0-9a-zA-Z0-9]");
 		tests.add("[^0]IN[0-9]");
 		tests.add("[^G]IN[A-Z]");
 		tests.add("[^A-C]IN[A-L]");
 		tests.add("a+(b[a-z])+(cde)+[a-z]+");
 		tests.add("[^a-z]IN[A-Za-z]([A-Za-z]|[0-9])*");
 		tests.add("([A-Za-z]|[0-9])*");
 		tests.add("[^a-z]IN[A-Za-z]*");
 		tests.add("(a+)+");
 
 		System.out.println(rdpc.myParser("[a-z]"));
 		System.out.println("Testing");
 		for (String st : tests) {
 			System.out.println("Test " + st + ": " + rdpc.parseFinal(st));
 		}
 	}
 	/*
<<<<<<< Updated upstream
 	 * @Test public void apiTest() { List<DefinedClass> dc =
 	 * RDPControl.getOutput(""); for(DefinedClass c : dc) {
 	 * System.out.printf("%s %s\n", c.getName(), String.valueOf(c.getRegex()));
 	 * }
 	 * 
 	 * List<DefinedClass> dc2 = RDPControl.getOutput("doc/sample_spec.txt");
 	 * for(DefinedClass c : dc2) { System.out.printf("%s %s\n", c.getName(),
 	 * String.valueOf(c.getRegex())); } }
	 */
 
=======
 	 * @Test public void apiTest() { DefinedClass[] dc =
 	 * RDPControl.getOutput(""); for (DefinedClass c : dc) {
 	 * System.out.printf("%s %s\n", c.getName(), String.valueOf(c.getRegex()));
 	 * } }
 	 */
>>>>>>> Stashed changes
 }
