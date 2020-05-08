 package general;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import translation.Main;
import util.FileUtil;
 import util.ToolIO;
 import static org.junit.Assert.assertEquals;
 import static util.TestUtil.getTreeAsString;
 import static util.TestUtil.fileToString;
 
 public class TestInstance {
	private String path = "src" + FileUtil.separator + "test"
			+ FileUtil.separator + "resources" + FileUtil.separator
			+ "instance" + FileUtil.separator;
 
 	@Test
 	public void TestNamedInstance() throws Exception {
 		ToolIO.setMode(ToolIO.TOOL);
		StringBuilder sb = Main.start(path+ "test1"+ FileUtil.separator + "Main", null, false);
 		String result = getTreeAsString(sb.toString());
		String expected = getTreeAsString(fileToString(path+ "test1"+ FileUtil.separator + "Main.mch"));
 		assertEquals(expected, result);
 	}
 
 	@Test
 	public void TestUnnamedInstance() throws Exception {
		StringBuilder sb = Main.start(path+ "test2"+ FileUtil.separator + "Main", null, false);
 		String result = getTreeAsString(sb.toString());
		String expected = getTreeAsString(fileToString(path+ "test2"+ FileUtil.separator + "Main.mch"));
 		assertEquals(expected, result);
 	}

 	@Test
 	public void TestNoSubstitution() throws Exception {
		StringBuilder sb = Main.start(path+ "test2"+ FileUtil.separator + "Main", null, false);
 		String result = getTreeAsString(sb.toString());
		String expected = getTreeAsString(fileToString(path+ "test2"+ FileUtil.separator + "Main.mch"));
 		assertEquals(expected, result);
 	}
 
 }
