 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.util;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 /**
  * Test stuff from {@code PropertiesUtils}.
  */
 public class PropertiesUtilsTest extends TestCase {
 
 	/**
 	 *
 	 */
 	public interface IDefault {
 		String get(String key);
 	}
 
 	public void testAsMapNullString() {
 		Map<String, String> map = PropertiesUtils.asMap(null);
 		assertEquals(0, map.size());
 	}
 
 	public void testAsMapWithHashtable() {
 		Hashtable<String, String> hashtable = new Hashtable<String, String>();
 		hashtable.put("a", "1");
 		hashtable.put("b", "2");
 		Map<String, String> map = PropertiesUtils.asMap(hashtable);
 		assertEquals(2, map.size());
 		assertEquals("1", map.get("a"));
 		assertEquals("2", map.get("b"));
 	}
 
 	public void testAsMapWithHashtableWithExpectation() {
 		Hashtable<String, String> hashtable = new Hashtable<String, String>();
 		hashtable.put("a", "1");
 		hashtable.put("b", "2");
 		Map<String, String> map = PropertiesUtils.asMap(hashtable, "a", "b");
 		assertEquals(2, map.size());
 		assertEquals("1", map.get("a"));
 		assertEquals("2", map.get("b"));
 	}
 
 	public void testAsMapEmptyString() {
 		Map<String, String> map = PropertiesUtils.asMap("");
 		assertEquals(0, map.size());
 	}
 
 	public void testAsMap() {
 		Map<String, String> map = PropertiesUtils.asMap("a=1,b=2");
 		assertEquals(2, map.size());
 		assertEquals("1", map.get("a"));
 		assertEquals("2", map.get("b"));
 	}
 
 	public void testAsMapWithExpectationFullfill() {
 		Map<String, String> map = PropertiesUtils.asMap("a=1,b=2", "a", "b");
 		assertEquals("1", map.get("a"));
 		assertEquals("2", map.get("b"));
 	}
 
 	public void testAsMapWithExpectationFails() {
 		try {
 			PropertiesUtils.asMap("a=1,b=2", "a", "c");
 			fail();
 		} catch (IllegalArgumentException e) {
 			//ok
 		}
 	}
 
 	public void testAsMapIgnoreWhitespace() {
 		Map<String, String> map = PropertiesUtils.asMap(" a= 1 ,  b = 2   ");
 		assertEquals("1", map.get("a"));
 		assertEquals("2", map.get("b"));
 	}
 
 	public void testAsMapFailMissingEquals() {
 		try {
 			PropertiesUtils.asMap(" a: 1 ,  b = 2   ");
 			fail();
 		} catch (IllegalArgumentException e) {
 			// ok
 		}
 	}
 
 	public void testAsMapWithDefaults() {
 		Map<String, String> map = PropertiesUtils.asMap("a=5", Literal.map("a", "1").map("b", "2").map("c", "3"));
 		assertEquals(3, map.size());
 		assertEquals("5", map.get("a"));
 		assertEquals("2", map.get("b"));
 		assertEquals("3", map.get("c"));
 	}
 
 	public void testAsArrayEmpty() {
 		assertEquals(0, PropertiesUtils.asArray("").length);
 		assertEquals(0, PropertiesUtils.asArray(null).length);
 	}
 
 	public void testAsArrayWithContent() {
 		String[] actual = PropertiesUtils.asArray("a,b,c");
 		assertEquals(3, actual.length);
 		assertEquals("a", actual[0]);
 		assertEquals("b", actual[1]);
 		assertEquals("c", actual[2]);
 	}
 
 	public void testAsArrayWithContentPlusFinalComma() {
 		String[] actual = PropertiesUtils.asArray("a,");
 		assertEquals(2, actual.length);
 		assertEquals("a", actual[0]);
 		assertEquals("", actual[1]);
 	}
 
 	public void testAsArrayWithEmptyContentWithinCommas() {
 		String[] actual = PropertiesUtils.asArray("a,b,,c");
 		assertEquals(4, actual.length);
 		assertEquals("a", actual[0]);
 		assertEquals("b", actual[1]);
 		assertEquals("", actual[2]);
 		assertEquals("c", actual[3]);
 	}
 
 	public void testAsArrayWithOnlyEmptyContentWithinCommas() {
 		String[] actual = PropertiesUtils.asArray(",,,");
 		assertEquals(4, actual.length);
 		assertEquals("", actual[0]);
 		assertEquals("", actual[1]);
 		assertEquals("", actual[2]);
 		assertEquals("", actual[3]);
 	}
 
 	public void testAsArrayWithOnlyOneComma() {
 		String[] actual = PropertiesUtils.asArray(",");
 		assertEquals(2, actual.length);
 		assertEquals("", actual[0]);
 		assertEquals("", actual[1]);
 	}
 
 	public void testAsArrayWithBackslashComma() {
 		String[] actual = PropertiesUtils.asArray("1,2\\,3,4");
 		assertEquals(3, actual.length);
 		assertEquals("1", actual[0]);
 		assertEquals("2,3", actual[1]);
 		assertEquals("4", actual[2]);
 	}
 
 	public void testAsArrayWithBackslashBackSlashComma() {
 		String[] actual = PropertiesUtils.asArray("1,2\\,3,\\\\,4");
 		assertEquals(4, actual.length);
 		assertEquals("1", actual[0]);
 		assertEquals("2,3", actual[1]);
 		assertEquals("\\", actual[2]);
 		assertEquals("4", actual[3]);
 	}
 
 	public void testAsArrayWithUnknownEscapeCharacter() {
 		try {
 			PropertiesUtils.asArray("1,2\\n,3,\\\\,4");
 			fail();
 		} catch (IllegalArgumentException e) {
 			// ok
 		}
 	}
 
 }
