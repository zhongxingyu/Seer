 /*
  * Copyright 2013 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.query.command.Fields;
 
 public class FieldsParserTest {
 	@Test
 	public void testSelectSingleField() {
 		FieldsParser p = new FieldsParser();
 		Fields fields = (Fields) p.parse(null, "fields sport");
 
 		assertEquals(1, fields.getFields().size());
 		assertTrue(fields.isSelector());
 		assertEquals("sport", fields.getFields().get(0));
 		assertEquals("fields sport", fields.toString());
 	}
 
 	@Test
 	public void testSelectMultiFields() {
 		FieldsParser p = new FieldsParser();
 		Fields fields = (Fields) p.parse(null, "fields sip,sport, dip, dport ");
 
 		assertEquals(4, fields.getFields().size());
 		assertTrue(fields.isSelector());
 		assertEquals("sip", fields.getFields().get(0));
 		assertEquals("sport", fields.getFields().get(1));
 		assertEquals("dip", fields.getFields().get(2));
 		assertEquals("dport", fields.getFields().get(3));
 		assertEquals("fields sip, sport, dip, dport", fields.toString());
 	}
 
 	@Test
 	public void testFilterFields() {
 		FieldsParser p = new FieldsParser();
 		Fields fields = (Fields) p.parse(null, "fields - note,user ");
 
 		assertEquals(2, fields.getFields().size());
 		assertFalse(fields.isSelector());
 		assertEquals("note", fields.getFields().get(0));
 		assertEquals("user", fields.getFields().get(1));
 		assertEquals("fields - note, user", fields.toString());
 	}
 
 	@Test
 	public void testBrokenFields() {
 		try {
 			FieldsParser p = new FieldsParser();
 			p.parse(null, "fields - ");
 			fail();
 		} catch (QueryParseException e) {
 			assertEquals("no-field-args", e.getType());
 		}
 	}
	
	
	// test for araqne/issue#714
	@Test
	public void testFieldNameTrimming() {
		FieldsParser p = new FieldsParser();
		Fields fields = (Fields) p.parse(null, "fields sip\t,\nsport,\ndip,\n dport ");

		assertEquals(4, fields.getFields().size());
		assertTrue(fields.isSelector());
		assertEquals("sip", fields.getFields().get(0));
		assertEquals("sport", fields.getFields().get(1));
		assertEquals("dip", fields.getFields().get(2));
		assertEquals("dport", fields.getFields().get(3));
		assertEquals("fields sip, sport, dip, dport", fields.toString());
	}
 }
