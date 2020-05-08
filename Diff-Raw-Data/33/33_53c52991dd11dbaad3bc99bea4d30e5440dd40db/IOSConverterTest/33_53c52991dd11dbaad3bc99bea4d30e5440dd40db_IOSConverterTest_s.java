 package org.jabylon.ios;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.mockito.Mockito.when;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import org.apache.tools.ant.filters.StringInputStream;
 import org.eclipse.emf.ecore.resource.ContentHandler.ByteOrderMark;
 import org.jabylon.properties.PropertiesFactory;
 import org.jabylon.properties.Property;
 import org.jabylon.properties.PropertyAnnotation;
 import org.jabylon.properties.types.impl.PropertiesHelper;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 public class IOSConverterTest {
 
 
 	private IOSConverter fixture;
 	private StringWriter writer;
 
 	@Before
 	public void setup() {
 		fixture = new IOSConverter(true,null);
 		writer = new StringWriter();
 	}
 
 
 	@Test
 	public void testReadUnicodePropertyWithUnicodeEscapes() throws IOException {
 
 		fixture = new IOSConverter(false);
 		BufferedReader reader = new BufferedReader(new StringReader("\"äö\\u00DC\" = \"aaa\";"));
 
 		try {
 			Property property = fixture.readProperty(reader);
 			assertEquals("even with escaping turned of, it must still parse the escaped strings", "äöÜ", property.getKey());
 			assertEquals("aaa", property.getValue());
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 	}
 
 	@Test
 	public void testWriteProperty() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setComment("test");
 		property.setKey("key");
 		property.setValue("value");
 		fixture.writeProperty(writer, property);
 		assertEquals("/*test*/\n\"key\" = \"value\";\n", writer.toString());
 	}
 
 	@Test
 	public void testWritePropertyUnicode() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setComment("test");
 		property.setKey("ä");
 		property.setValue("ü");
 		fixture.writeProperty(writer, property);
 		assertEquals("/*test*/\n\"\\u00e4\" = \"\\u00fc\";\n", writer.toString());
 
 	}
 
 	@Test
 	public void testWritePropertyKeyWithSpaces() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setComment("test");
 		property.setKey("key with spaces");
 		property.setValue("test");
 		fixture.writeProperty(writer, property);
 		assertEquals("/*test*/\n\"key with spaces\" = \"test\";\n", writer.toString());
 
 	}
 
 	@Test
 	public void testWritePropertyMultiline() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key");
 		property.setValue("test\ntest");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\" = \"test\\ntest\";\n", writer.toString());
 
 	}
 
 
 	@Test
 	public void testWritePropertyWindowsMultiline() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key");
 		property.setValue("test\r\ntest");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\" = \"test\\r\\ntest\";\n", writer.toString());
 
 	}
 
 	@Test
 	public void testWritePropertyKeyMultiline() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key\nkey");
 		property.setValue("test");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\\nkey\" = \"test\";\n", writer.toString());
 
 	}
 	
 	@Test
 	public void testWritePropertyMultilineComment() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setComment("test\ntest");
 		property.setKey("key");
 		property.setValue("test");
 		fixture.writeProperty(writer, property);
 		assertEquals("/*test\ntest*/\n\"key\" = \"test\";\n", writer.toString());
 
 	}
 
 	@Test
 	public void testCheckForBom() throws Exception {
 		byte[] bytes = ByteOrderMark.UTF_16BE.bytes();
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		out.write(bytes);
 		out.write("test".getBytes());
 
 		ByteArrayInputStream stream = new ByteArrayInputStream(out.toByteArray());
 		ByteOrderMark bom = PropertiesHelper.checkForBom(stream);
 		assertEquals(ByteOrderMark.UTF_16BE, bom);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 		assertEquals("The reader must contain everything after the bom", "test", reader.readLine());
 
 	}
 
 	@Test
 	public void testCheckForBomNoBom() throws Exception {
 		ByteArrayInputStream stream = new ByteArrayInputStream("test".getBytes());
 		ByteOrderMark bom = PropertiesHelper.checkForBom(stream);
 		assertNull(bom);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 		assertEquals("The reader must contain everything after the bom", "test", reader.readLine());
 
 	}
 
 	@Test(expected=IllegalArgumentException.class)
 	public void testCheckForBomWrongStream() throws Exception {
 		InputStream in = Mockito.mock(InputStream.class);
 		when(in.markSupported()).thenReturn(false);
 		PropertiesHelper.checkForBom(in);
 	}
 
 	/**
 	 * tests that files that contain only a newline are handled correctly
 	 * http://github.com/jutzig/jabylon/issues/issue/104
 	 * @throws IOException
 	 */
     @Test
 	public void testEmptyFile() throws IOException {
     	StringInputStream in = new StringInputStream("\n\r");
     	Property property = fixture.readProperty(new BufferedReader(new InputStreamReader(in)));
     	assertNull(property);
 	}
 
     /**
      * tests that unicode non breakable space (\u00A0)is preserved
      * see https://github.com/jutzig/jabylon/issues/149
      * @throws IOException
      */
 	@Test
 	public void testPreserveNBSPWrite() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key");
 		property.setValue("test\u00A0");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\" = \"test\\u00a0\";\n", writer.toString());
 	}
 
 	/**
      * tests that unicode non breakable space (\u00A0)is preserved
      * see https://github.com/jutzig/jabylon/issues/149
      * @throws IOException
      */
 	@Test
 	public void testPreserveNBSPWriteNoEscaping() throws IOException {
 		fixture = new IOSConverter(false);
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key");
 		property.setValue("test\u00A0");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\" = \"test\u00A0\";\n", writer.toString());
 	}
 
     /**
      * tests that unicode non breakable space (\u00A0)is preserved
      * see https://github.com/jutzig/jabylon/issues/149
      * @throws IOException
      */
 	@Test
 	public void testPreserveNBSPRead() throws IOException {
 		BufferedReader reader = new BufferedReader(new StringReader("\"key\" = \"test\\u00a0\";\n"));
 
 		try {
 			Property property = fixture.readProperty(reader);
 			assertEquals("the NBSP must survive", "test\u00a0", property.getValue());
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 	}
 
     /**
      * tests that unicode non breakable space (\u00A0)is preserved
      * see https://github.com/jutzig/jabylon/issues/149
      * @throws IOException
      */
 	@Test
 	public void testPreserveNBSPReadNoEscaping() throws IOException {
 		fixture = new IOSConverter(false);
 		BufferedReader reader = new BufferedReader(new StringReader("\"key\" = \"test\u00a0\";\n"));
 
 		try {
 			Property property = fixture.readProperty(reader);
 			assertEquals("the NBSP must survive", "test\u00a0", property.getValue());
 		} finally {
 			if (reader != null)
 				reader.close();
 		}
 	}
 
 
     /**
      * tests that the parser resets a key when the newline is not escaped
      * see https://github.com/jutzig/jabylon/issues/183
      * @throws IOException
      */
     @Test
     public void testMultilineHandling() throws IOException {
         fixture = new IOSConverter(false);
         BufferedReader reader = new BufferedReader(new StringReader("/* foo*/\n\r"
                                                                     +"\"PROC_INST_DISPLAY_IMG\"=\"dashb_display_12.gif\";"));
 
         try {
             Property property = fixture.readProperty(reader);
             assertEquals("dashb_display_12.gif", property.getValue());
             assertEquals("PROC_INST_DISPLAY_IMG", property.getKey());
         } finally {
             if (reader != null)
                 reader.close();
         }
     }
     
     /**
      * tests that leading spaces are preserved
      * @throws IOException
      */
 	@Test
 	public void testLeadingSpacesWrite() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key");
 		property.setValue(" test");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\" = \" test\";\n", writer.toString());
 	}
 	
 
     /**
      * tests that leading spaces are preserved during parsing
      * @throws IOException
      */
 	@Test
 	public void testLeadingSpacesRead() throws IOException {
 		BufferedReader reader = new BufferedReader(new StringReader("\"key\" = \" value\";"));
 		Property property = fixture.readProperty(reader);
 		assertEquals("key", property.getKey());
 		assertEquals(" value", property.getValue());
 
 	}
 	
     /**
      * tests that newlines are converted to \n
      * see https://github.com/jutzig/jabylon/issues/185
      * @throws IOException
      */
 	@Test
 	public void testLFWrite() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key");
 		property.setValue("test\ntest");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\" = \"test\\ntest\";\n", writer.toString());
 	}
 	
 	/**
      * tests that CRLFs are converted to \r\n
      * see https://github.com/jutzig/jabylon/issues/185
      * @throws IOException
      */
 	@Test
 	public void testCRLFWrite() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setKey("key");
 		property.setValue("test\r\ntest");
 		fixture.writeProperty(writer, property);
 		assertEquals("\"key\" = \"test\\r\\ntest\";\n", writer.toString());
 	}
 	
     /**
      * tests that \n and \r are replaced with actual values during reading
      * see https://github.com/jutzig/jabylon/issues/186
      * @throws IOException
      */
 	@Test
 	public void testCRLFRead() throws IOException {
 		BufferedReader reader = new BufferedReader(new StringReader("\"key\" = \"value\\r\\n\";"));
 		Property property = fixture.readProperty(reader);
 		assertEquals("key", property.getKey());
 		assertEquals("value\r\n", property.getValue());
 
 	}
 	
 	/**
      * tests that annotations are written correctly
      * @throws IOException
      */
 	@Test
 	public void testWritePropertyWithAnnotation() throws IOException {
 		Property property = PropertiesFactory.eINSTANCE.createProperty();
 		property.setComment("comment");
 		PropertyAnnotation ann1 = PropertiesFactory.eINSTANCE.createPropertyAnnotation();
 		ann1.setName("foo");
 		ann1.getValues().put("test", "value");
 		property.getAnnotations().add(ann1);
 		
 		PropertyAnnotation ann2 = PropertiesFactory.eINSTANCE.createPropertyAnnotation();
 		ann2.setName("bar");
 		property.getAnnotations().add(ann2);
 		property.setKey("key");
 		property.setValue("test");
 		fixture.writeProperty(writer, property);
 		assertEquals("/*@foo(test=\"value\")@bar\ncomment*/\n\"key\" = \"test\";\n", writer.toString());
 	}
 	
     /**
      * tests that annotations are read correctly
      * @throws IOException
      */
 	@Test
 	public void testReadPropertyWithAnnotation() throws IOException {
 		BufferedReader reader = new BufferedReader(new StringReader("/*@foo(test=\"value\")@bar*/\n\"key\" = \"value\";"));
 		Property property = fixture.readProperty(reader);
 		assertEquals("key", property.getKey());
 		assertEquals("value", property.getValue());
 		assertEquals(2, property.getAnnotations().size());
 		assertEquals("foo", property.getAnnotations().get(0).getName());
 	}
 	
     /**
      * tests that properties can still be parsed if the semicolon is missing.
      * The semicolon at the end really is mandatory, but when it's just parsing
      * we can be more linient.
      * @throws IOException
      */
 	@Test
 	public void testReadPropertyWithoutSemicolon() throws IOException {
 		BufferedReader reader = new BufferedReader(new StringReader("\"key\" = \"value\""));
 		Property property = fixture.readProperty(reader);
 		assertEquals("key", property.getKey());
 		assertEquals("value", property.getValue());
 	}	
 
     protected BufferedReader asReader(String string)
     {
     	return new BufferedReader(new StringReader(string));
     }
 	
 }
