 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded.jsf;
 
 import com.flexive.war.JsonWriter;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.search.ResultViewType;
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.util.*;
 
 /**
  * Test cases for the JsonWriter component.
  * 
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  *
  */
 @Test(groups = "shared")
 public class JsonWriterTest {
 
 	@Test
 	public void testEmptyArray() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		writer.closeArray();
 		writer.finishResponse();
 		assert "[]".equals(out.toString());
 	}
 	
 	@Test
 	public void testEmptyMap() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startMap();
 		writer.closeMap();
 		writer.finishResponse();
 		assert "{}".equals(out.toString());
 	}
 	
 	@Test
 	public void testSimpleMap() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startMap();
 		writer.writeAttribute("simpleStringProperty", "test");
 		writer.writeAttribute("intProperty", 1234);
 		writer.closeMap();
 		writer.finishResponse();
 		assert "{\"simpleStringProperty\":'test',\"intProperty\":1234}".equals(out.toString()) 
 			: "Unexpected output: " + out.toString();
 	}
 
 	@Test
 	public void testSimpleArray() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		writer.writeLiteral("abc");
 		writer.writeLiteral(1234);
 		writer.closeArray();
 		writer.finishResponse();
 		assert "['abc',1234]".equals(out.toString()) : "Unexpected output: " + out.toString();
 	}
 	
 	@Test
 	public void testArrayOfObjects() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		String expected = "[";
 		for (int i = 0; i < 10; i++) {
 			writer.startMap();
 			writer.writeAttribute("name", "object" + i);
 			writer.writeAttribute("color", "black");
 			writer.writeAttribute("id", i);
 			expected += (i > 0 ? "," : "") + "{\"name\":'object" + i + "',\"color\":'black',\"id\":" + i + "}";
 			writer.closeMap();
 		}
 		expected += "]";
 		writer.closeArray();
 		writer.finishResponse();
 		assert expected.equals(out.toString()) : "Unexpected output: " + out.toString();
 	}
 
     @Test
     public void testWriteMapValue() throws IOException {
         final StringWriter out = new StringWriter();
         final JsonWriter writer = new JsonWriter(out);
         final Map<Long, String> map = new HashMap<Long, String>();
         map.put(1L, "first value");
         map.put(2L, "second value");
        writer.writeAttribute("map", map);
        writer.finishResponse();
         assert out.toString().contains("first value") : "Unexpected output: " + out.toString();
         assert out.toString().contains("second value") : "Unexpected output: " + out.toString();
         assert out.toString().charAt(0) == '{' : "Unexpected output: " + out.toString();
     }
 	
 	@Test
 	public void testMatchingParensArray() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		try {
 			writer.closeMap();
 			assert false : "Map should not be closed if an array was opened before.";
 		} catch (Exception e) {
 			writer.closeArray();
 		}
 		writer.finishResponse();
 		assert "[]".equals(out.toString());
 	}
 
 	@Test
 	public void testMatchingParensMap() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startMap();
 		try {
 			writer.closeArray();
 			assert false : "Array should not be closed if a map was opened before.";
 		} catch (Exception e) {
 			writer.closeMap();
 		}
 		writer.finishResponse();
 		assert "{}".equals(out.toString());
 	}
 	
 	@Test
 	public void testAttributeOnlyInMap() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		try {
 			writer.writeAttribute("test", "test");
 			assert false : "Attributes may not be written in arrays.";
 		} catch (Exception e) {
 			// pass
 		}
 		writer.startMap();
 		writer.writeAttribute("id", 1234);
 		writer.closeMap();
 		writer.closeArray();
 		writer.finishResponse();
 		assert "[{\"id\":1234}]".equals(out.toString()) : "Unexpected output: " + out.toString();
 	}
 	
 	@Test
 	public void testNoTopLevelAttributes() {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		try {
 			writer.writeAttribute("test", 1234);
 			assert false : "Attributes must be contained in maps.";
 		} catch (Exception e) {
 			// pass
 		}
 	}
 
 	@Test
 	public void testNoTopLevelLiterals() {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		try {
 			writer.writeLiteral(1234);
 			assert false : "Literals must be contained in arrays.";
 		} catch (Exception e) {
 			// pass
 		}
 	}
 
 	@Test
 	public void testLiteralsOnlyInArray() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		writer.startMap();
 		try {
 			writer.writeLiteral(1234);
 			assert false : "Literals must be contained in arrays.";
 		} catch (Exception e) {
 			writer.closeMap();
 		}
 		writer.writeLiteral(4567);
 		writer.closeArray();
 		writer.finishResponse();
 		assert "[{},4567]".equals(out.toString()) : "Unexpected output: " + out.toString();
 	}
 	
 	@Test
 	public void testFinishResponseOpenArray() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		try {
 			writer.finishResponse();
 			assert false : "Response invalid, finishResponse may not succeed.";
 		} catch (Exception e) {
 			// pass
 		}
 		assert "[".equals(out.toString());
 	}
 	
 	@Test
 	public void testWriteAttribute() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startMap();
 		writer.startAttribute("name");
 		try {
 			writer.closeMap();
 			assert false : "Map must not be closed before attribute is written.";
 		} catch (Exception e) {
 			// pass
 		}
 		writer.writeAttributeValue("test");
 		writer.closeMap();
 		writer.finishResponse();
 		assert "{\"name\":'test'}".equals(out.toString());
 	}
 	
 	@Test
 	public void testInvalidWriteAttributeValue() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startMap();
 		try {
 			writer.writeAttributeValue(1234);
 			assert false : "May not write attribute value without attribute name.";
 		} catch (Exception e) {
 			// pass
 		}
 		writer.closeMap();
 		assert "{}".equals(out.toString());
 	}
 	
 	@Test
 	public void testFilteringQuotes() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startMap();
 		writer.writeAttribute("name", "\"test\"");
 		writer.writeAttribute("name2", "'test'");
 		writer.closeMap();
 		writer.finishResponse();
 		assert "{\"name\":'\"test\"',\"name2\":'\\'test\\''}".equals(out.toString()) : "Unexpected response: " + out.toString();
 	}
 	
 	@Test
 	public void testAttributeArray() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startMap();
 		writer.writeAttribute("id", 1234);
 		writer.startAttribute("myList");
 		writer.startArray();
 		writer.writeLiteral(1);
 		writer.writeLiteral(2);
 		writer.writeLiteral(3);
 		writer.closeArray();
 		writer.writeAttribute("name", "test");
 		writer.startAttribute("anotherList");
 		writer.startArray();
 		writer.writeLiteral(99);
 		writer.closeArray();
 		writer.startAttribute("nestedList");
 		writer.startArray();
 		writer.startArray();
 		writer.writeLiteral(1);
 		writer.writeLiteral(2);
 		writer.closeArray();
 		writer.writeLiteral(3);
 		writer.startArray();
 		writer.writeLiteral(4);
 		writer.writeLiteral(5);
 		writer.closeArray();
 		writer.closeArray();
 		writer.closeMap();
 		writer.finishResponse();
 		assert "{\"id\":1234,\"myList\":[1,2,3],\"name\":'test',\"anotherList\":[99],\"nestedList\":[[1,2],3,[4,5]]}".equals(out.toString()) : "Unexpected response: " + out.toString();
 	}
 	
 	@Test
 	public void testAttributeMap() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.startArray();
 		
 		writer.startMap();
 		writer.startAttribute("child");
 		writer.startMap();
 		writer.writeAttribute("id", 1234);
 		writer.startAttribute("group");
 		writer.startMap();
 		writer.writeAttribute("value", 21);
 		writer.closeMap();
 		writer.closeMap();
 		writer.closeMap();
 		
 		writer.writeLiteral(42);
 		
 		writer.startMap();
 		writer.startAttribute("child");
 		writer.startMap();
 		writer.writeAttribute("id", 44);
 		writer.closeMap();
 		writer.closeMap();
 		
 		writer.closeArray();
 		writer.finishResponse();
 		assert "[{\"child\":{\"id\":1234,\"group\":{\"value\":21}}},42,{\"child\":{\"id\":44}}]".equals(out.toString()) : "Unexpected output: " + out.toString(); 
 	}
 	
 	@Test
 	public void testDoubleQuotedStringValues() throws IOException {
 		StringWriter out = new StringWriter();
 		JsonWriter writer = new JsonWriter(out);
 		writer.setSingleQuotesForStrings(false);
 		writer.startMap();
 		writer.writeAttribute("name", "\"quotedValue\"");
 		writer.closeMap();
 		writer.finishResponse();
 		assert "{\"name\":\"\\\"quotedValue\\\"\"}".equals(out.toString()) : "Unexpected output: " + out.toString();
 	}
 
     @Test
     public void testWriteArrayValue() throws IOException {
         StringWriter out = new StringWriter();
         JsonWriter writer = new JsonWriter(out);
         writer.startMap();
         writer.writeAttribute("value", Arrays.asList(new Object[] { "stringValue", 123, 123.456 }));
         writer.closeMap();
         writer.finishResponse();
         assert "{\"value\":['stringValue',123,123.456]}".equals(out.toString()) : "Unexpected output: " + out.toString();
     }
 
     @Test
     public void testWriteDate() throws IOException {
         StringWriter out = new StringWriter();
         JsonWriter writer = new JsonWriter(out, Locale.ENGLISH);
         writer.startArray();
         writer.writeLiteral(new Date(0));
         writer.closeArray();
         writer.finishResponse();
         assert ("['" + DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH).format(new Date(0)) + "']").equals(out.toString()) : "Unexepcted output: " + out.toString();
     }
 
     @Test
     public void testWriteInvalidMap() throws IOException {
         final StringWriter out = new StringWriter();
         final JsonWriter writer = new JsonWriter(out);
         writer.startMap();
         writer.startAttribute("attr");
         writer.startMap();
         writer.writeAttribute("prop", "value");
         writer.closeMap();
         try {
             writer.startMap();
             assert false : "Map outside array";
         } catch (FxRuntimeException e) {
             // succeed
         }
     }
 
     @Test
     public void testWriteInvalidArray() throws IOException {
         final StringWriter out = new StringWriter();
         final JsonWriter writer = new JsonWriter(out);
         writer.startMap();
         writer.startAttribute("attr");
         writer.startMap();
         writer.writeAttribute("prop", "value");
         writer.closeMap();
         try {
             writer.startArray();
             assert false : "Array outside array";
         } catch (FxRuntimeException e) {
             // succeed
         }
     }
 
     @Test
     public void testWriteEnum() throws IOException {
         final StringWriter out = new StringWriter();
         final JsonWriter writer = new JsonWriter(out);
         writer.startMap()
               .writeAttribute("enum", ResultViewType.THUMBNAILS)
               .closeMap()
               .finishResponse();
         assert writer.toString().contains("'" + ResultViewType.THUMBNAILS + "'")
                 : "Enum value not escaped: " + writer.toString();
     }
 }
