 package net.sf.springderby;
 
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 
 import junit.framework.TestCase;
 
 public class LoggerOutputStreamTest extends TestCase {
 	private final static String[] testStrings =
 		{
 			"Vestibulum semper. Nullam non odio. Aliquam quam.",
 			"Mauris eu lectus non nunc auctor ullamcorper.",
 			"Sed tincidunt molestie enim. Phasellus lobortis justo sit amet quam.",
 			"Duis nulla erat, varius a, cursus in, tempor sollicitudin, mauris.",
 			"Aliquam mi velit, consectetuer mattis, consequat tristique, pulvinar ac, nisl.",
 			"Aliquam mattis vehicula elit. Proin quis leo sed tellus scelerisque molestie.",
 			"Quisque luctus. Integer mattis. Donec id augue sed leo aliquam egestas.",
 			"Quisque in sem. Donec dictum enim in dolor. Praesent non erat.",
 			"Nulla ultrices vestibulum quam."
 		};
 	
 	public void test() throws Exception {
 		TestLog log = new TestLog();
 		PrintWriter out = new PrintWriter(new OutputStreamWriter(new LoggerOutputStream(log, "UTF-8"), "UTF-8"), false);
 		for (int i=0; i<testStrings.length; i++) {
 			out.print(testStrings[i]);
 			out.print("\n");
 		}
 		out.close();
 		for (int i=0; i<testStrings.length; i++) {
 			assertEquals(testStrings[i], log.pop());
 		}
		assertFalse(log.hasMessages());
	}
	
	public void testNoEndOfLine() throws Exception {
		TestLog log = new TestLog();
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new LoggerOutputStream(log, "UTF-8"), "UTF-8"), false);
		out.print("TEST1\n");
		out.print("TEST2");
		out.close();
		assertEquals("TEST1", log.pop());
		assertEquals("TEST2", log.pop());
		assertFalse(log.hasMessages());
 	}
 }
