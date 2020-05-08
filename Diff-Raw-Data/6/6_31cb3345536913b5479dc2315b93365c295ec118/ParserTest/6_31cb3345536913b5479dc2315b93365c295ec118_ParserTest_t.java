 package de.bhurling.lyml;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class ParserTest {
 
 	@Test
 	public void testFixXml() {
 		String basename = "abc - 1234";
 		assertEquals("abc", YmlParser.fixXmlName(basename));
 
 		basename = "abc Test Test - 1234";
 		assertEquals("abc_test_test", YmlParser.fixXmlName(basename));
 	}
 
	@Test
	public void testFixValue() {
		String value = "abc & 1";
		assertEquals("abc &amp; 1", YmlParser.fixValue(value));
	}

 }
