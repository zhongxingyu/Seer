 package jsclassloader;
 
import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import jsclassloader.classes.ClassFileSet;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ClassFileSetTest
 {
 
 	List<String> expected = new ArrayList<String>();
 	
 	@Before
 	public void setUp() {
 	}
 	
 	@After
 	public void tearDown() 
 	{
 		expected.clear();
 	}
 
 	@Test
 	public void testFindAllClasses() throws IOException 
 	{
 		Config config = new Config();
 		config.setProperty(Config.PROP_SOURCE_FOLDERS, "src/test/resources/jsbundler");
 		
 		ClassFileSet unit = new ClassFileSet(config);
 		
 		List<String> result = unit.getAllJsClasses();
 		
 		expected.add("package.pkgclass1");
 		expected.add("package.pkgclass2");
 		expected.add("package.pkgclass3");
 		expected.add("rootclass1");
 		expected.add("rootclass2");
 		
 		for (String expectedItem : expected) {
 			Assert.assertTrue("all classes list must contain item: " + expectedItem, result.contains(expectedItem));
 		}
 	}
 	
 	/**
 	 * make sure we're getting all the right classes in the dependency tree test folder as well.
 	 * @throws java.io.IOException
 	 */
 	@Test
 	public void testFindDepTestClasses() throws IOException 
 	{
 		Config config = new Config();
 		config.setProperty(Config.PROP_SOURCE_FOLDERS, "src/test/resources/dependency-tree");
 		ClassFileSet unit = new ClassFileSet(config);
 		
 		List<String> result = unit.getAllJsClasses();
 		
 		expected.add("abra.cad.abra.Hey");
 		expected.add("abra.cad.abra.Presto");
 		expected.add("abra.cad.abra.Rabbit");
 		expected.add("abra.cad.abra.Hat");
 		expected.add("abra.cad.abra.open.sesame.Genie");
 		expected.add("abra.cad.abra.open.sesame.Lamp");
 		
 		expected.add("ala.kazam.Zap");
 		expected.add("ala.kazam.Fizzle");
 		expected.add("ala.kazam.smoke.Mirrors");
 		
 		expected.add("something.else.Entirely");
 		
 		for (String expectedItem : expected) {
 			Assert.assertTrue("all classes list must contain item: " + expectedItem, result.contains(expectedItem));
 		}
 	}
 	
 	/**
 	 * make sure we can look up which source root folder each class came from
 	 * @throws java.io.IOException
 	 */
 	@Test
 	public void testFindSrcFolderByClasses() throws IOException 
 	{
 		Config config = new Config();
 		config.setProperty(Config.PROP_SOURCE_FOLDERS, "src/test/resources/dependency-tree,src/test/resources/jsbundler");
 		ClassFileSet unit = new ClassFileSet(config);
 		
		String expected = "src/test/resources/dependency-tree";
 
		expected = expected.replace("/", File.separator);
		Assert.assertEquals(expected, unit.getSrcDirFromClassname("ala.kazam.Zap"));
 		
 	}
 }
