 import java.io.IOException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class ParserTest {
 
 	@Test
 	public void testParseFile() throws IOException {
 		Parser parser = new Parser("TestCase.txt");
 		
 		parser.parseFile();
 		
		Assert.assertEquals(parser.getPublications().get(0).getTitlePaper(), "Sexual Selection, Resource Distribution, and Population Size in Synthetic Sympatric Speciation");
 	}
 	
 	@Test
 	public void testParseFileString() throws IOException {
 		Parser parser = new Parser("TestCase.txt");
 		
 		parser.parseFile();
 		
		Assert.assertEquals(parser.getPublications().get(1).toString(), "Journal Article, Authors = Eskridge, Brent E.; Hougen, Dean F.; Paper Title = Extending Adaptive Fuzzy Behavior Hierarchies to Multiple Levels of Composite Behaviors, Serial Title = Robotics and Autonomous Systems, Starting Page = 1076, Ending Page = 1084, Time of Publication = September2010, Hyperlink = http://dx.doi.org/10.7551/978-0-262-31050-5-ch020");
 	}
 	
 	@Test
 	public void testParseFileAuthor() throws IOException {
 		Parser parser = new Parser("TestCase.txt");
 		
 		parser.parseFile();
 		
		Assert.assertEquals(parser.getPublications().get(1).getAuthorsString().contains("Dean"), true);
 	}
 
 }
