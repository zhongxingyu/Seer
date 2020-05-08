 package augment;
 
 import graph.model.GraphFileReader;
 
 import java.io.FileNotFoundException;
 
 import org.junit.Test;
 
 import util.GraphDuplicateChecker;
 
 public class DupsTest {
 	
 	@Test
 	public void testSix_x() throws FileNotFoundException {
		GraphDuplicateChecker checker = new GraphDuplicateChecker();
		checker.checkForDuplicates(GraphFileReader.readAll("output/mckay/six_x.txt"));
 	}
 	
 	@Test
     public void testSix_4() throws FileNotFoundException {
        GraphDuplicateChecker checker = new GraphDuplicateChecker();
        checker.checkForDuplicates(GraphFileReader.readAll("output/mckay/six_4.txt"));
     }
 
 }
