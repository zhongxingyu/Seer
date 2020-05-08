 package acceptanstest9;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Scanner;
 
 import members.Competitor;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import result.CvsReader;
 import result.Parser;
 import result.ParserException;
 import sort.LapCompetitorPrinter;
 
 public class Test9 {
 	private Parser parser;
 	
 	@Before
 	public void initialize(){
 		parser = new Parser();
 	}
 	
	@Test
 	public void testResult() throws FileNotFoundException, ParserException{
 		Map<Integer, Competitor> competitors;
 		
 		competitors = parser.parse(new CvsReader("src/acceptanstest9/starttider.txt").readAll());
 		parser.parse(new CvsReader("src/acceptanstest9/maltider.txt").readAll(), competitors);
 		parser.parse(new CvsReader("src/acceptanstest9/namnfil.txt").readAll(), competitors);
 		
 		LapCompetitorPrinter printer = new LapCompetitorPrinter();
 		printer.printResults(new ArrayList<Competitor>(competitors.values()), "src/test/accept9_result.txt");
 		testResultFiles();
 	}
 		
 	private void testResultFiles() throws FileNotFoundException {
 		File file1 = new File("src/acceptanstest9/resultat.txt");
 		File file2 = new File("src/test/accept9_result.txt");
 		Scanner scan1 = new Scanner(file1);
 		Scanner scan2 = new Scanner(file2);
 		String line1, line2;
 		while (scan1.hasNext() && scan2.hasNext()) {
 			line1 = scan1.nextLine();
 			line2 = scan2.nextLine();
 			assertEquals("Wrong result.", line1, line2);
 		}
 	}
 	
 	
 }
