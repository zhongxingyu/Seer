 package com.mtgi.analytics.xml;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.net.URL;
 
 import org.junit.Test;
 
 public class BinaryToXSLTTest {
 
 	@Test
 	public void testStreamingXSL() throws Exception {
 		
 		File xsl = getResource(BinaryToXSLTTest.class, "BinaryToXSLTTest.xsl");
 		File in = getResource(BinaryToXSLTTest.class, "BinaryToXSLTTest-input.bxml");
 		File expectedData = getResource(BinaryToXSLTTest.class, "BinaryToXSLTTest-output.sql");
 		File out = File.createTempFile("BinaryToXSLTTest", ".sql");
 		out.deleteOnExit();
 		
 		String[] args = { 
 			"-tool", "xslt", 
 			"-split", "event", 
 			"-xsl", xsl.getAbsolutePath(), 
 			in.getAbsolutePath(), out.getAbsolutePath() 
 		};
 		assertEquals("process completes successfully", 0, Main.process(args));
 		assertTrue("sanity check: some data generated", out.length() > 0);
 		
 		BufferedReader expected = new BufferedReader(new FileReader(expectedData));
 		try {
 			BufferedReader actual = new BufferedReader(new FileReader(out));
 			try {
 
 				int line = 1;
 				for (String s = expected.readLine(); s != null; s = expected.readLine()) {
 					assertEquals("line " + line++ + " matches", s, actual.readLine());
 				}
 				assertNull("no data remains in results", actual.readLine());
 				
 			} finally {
 				actual.close();
 			}
 		} finally {
 			expected.close();
 		}
 		
 		out.delete();
 	}
 	
	public static File getResource(Class<?> test, String name) {
 		URL url = test.getResource(name);
 		if (url == null)
 			fail("unable to find test resource " + name);
 		String path = url.toExternalForm();
 		if (path.startsWith("file:/")) {
			path = path.substring(6);
			if (!path.startsWith("/"))
				path = "/" + path;
			File file = new File(path);
 			assertTrue("File exists at " + file.getAbsolutePath(), file.isFile());
 			return file;
 		}
 		throw new AssertionError("Unrecognizable resource path: " + path);
 	}
 }
