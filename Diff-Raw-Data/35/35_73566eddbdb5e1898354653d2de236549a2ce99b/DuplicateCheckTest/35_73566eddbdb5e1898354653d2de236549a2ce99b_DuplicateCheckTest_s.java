 package hybrid;
 
 import graph.model.GraphFileReader;
 import graph.model.IntGraph;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.Test;
 
 import util.GraphDuplicateChecker;
 
 public class DuplicateCheckTest {
 	
 	@Test
 	public void testFours() throws IOException {
 		String filepath = "output/hybrid/four_x.txt";
 		List<IntGraph> graphs = GraphFileReader.readAll(filepath);
		GraphDuplicateChecker checker = new GraphDuplicateChecker();
		checker.checkForDuplicates(graphs);
 	}
 	
 	@Test
 	public void testFives() throws IOException {
 		String filepath = "output/hybrid/five_x.txt";
 		List<IntGraph> graphs = GraphFileReader.readAll(filepath);
		GraphDuplicateChecker checker = new GraphDuplicateChecker();
		checker.checkForDuplicates(graphs);
 	}
 	
 	@Test
 	public void testSixes() throws IOException {
 		String filepath = "output/hybrid/six_x.txt";
 		List<IntGraph> graphs = GraphFileReader.readAll(filepath);
		GraphDuplicateChecker checker = new GraphDuplicateChecker();
		checker.checkForDuplicates(graphs);
 	}
 	
 	@Test
 	public void testSevens() throws IOException {
 		String filepath = "output/hybrid/seven_x.txt";
 		List<IntGraph> graphs = GraphFileReader.readAll(filepath);
		GraphDuplicateChecker checker = new GraphDuplicateChecker();
		checker.checkForDuplicates(graphs);
 	}
 	
 	@Test
 	public void testEight_4() throws IOException {
 		String filepath = "output/hybrid/eight_4.txt";
 		List<IntGraph> graphs = GraphFileReader.readAll(filepath);
		GraphDuplicateChecker checker = new GraphDuplicateChecker();
		checker.checkForDuplicates(graphs);
 	}
 	
 	@Test
 	public void testEight_3() throws IOException {
 		String filepath = "output/hybrid/eight_3.txt";
 		List<IntGraph> graphs = GraphFileReader.readAll(filepath);
		GraphDuplicateChecker checker = new GraphDuplicateChecker();
		checker.checkForDuplicates(graphs);
 	}
 
 }
