 package styling;
 
 import java.io.File;
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import styling.entities.ClassResult;
 import styling.parser.ClassParser;
 
 public class ClassParserTest {
 
 	@Test
 	public void testSimpleClass() throws IOException {
 		
 		File file = new File("src/main/test/dummyclasses/Example.java");
 		
		ClassResult classResult = ClassParser.Parse(file);
 		
 		Assert.assertEquals("Example", classResult.getClassName());
 		Assert.assertEquals(2, classResult.getNumberOfAttributes());
 		Assert.assertEquals(2, classResult.getMethodResultList().size());
 		Assert.assertEquals(0, classResult.getNumberOfImports());
 	}
 }
