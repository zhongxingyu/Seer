 package general;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import translation.Main;
 import util.ToolIO;
 import static org.junit.Assert.assertEquals;
 import static util.TestUtil.getTreeAsString;
 import static util.TestUtil.fileToString;
 
 public class TestInstance {
 
 	@Test
 	public void TestNamedInstance() throws Exception {
 		ToolIO.setMode(ToolIO.TOOL);
		String dir = "src\\test\\resources\\instance\\test1\\";
		StringBuilder sb = Main.start(dir +"Main", null, false);
 		String result = getTreeAsString(sb.toString());
		String expected = getTreeAsString(fileToString(dir +"Main.mch"));
 		assertEquals(expected, result);
 	}
 
 	@Test
 	public void TestUnnamedInstance() throws Exception {
		String dir = "src\\test\\resources\\instance\\test2\\";
		StringBuilder sb = Main.start(dir +"Main", null, false);
 		String result = getTreeAsString(sb.toString());
		String expected = getTreeAsString(fileToString(dir +"Main.mch"));
 		assertEquals(expected, result);
 	}
	
 	@Test
 	public void TestNoSubstitution() throws Exception {
		String dir = "src\\test\\resources\\instance\\test3\\";
		StringBuilder sb = Main.start(dir +"Main", null, false);
 		String result = getTreeAsString(sb.toString());
		String expected = getTreeAsString(fileToString(dir +"Main.mch"));
 		assertEquals(expected, result);
 	}
	
	
	
 
 }
