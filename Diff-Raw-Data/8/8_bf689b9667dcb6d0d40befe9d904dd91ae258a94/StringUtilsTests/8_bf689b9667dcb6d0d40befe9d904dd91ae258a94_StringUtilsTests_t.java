 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.utils;
 
 import static org.junit.Assert.*;
 import static org.oobium.utils.StringUtils.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 public class StringUtilsTests {
 
 	@Test
 	public void testCamelCase() throws Exception {
 		assertEquals("Null", 	camelCase(null));
 		assertEquals("Mymodel", camelCase("mymodel"));
 		assertEquals("MyModel", camelCase("my_model"));
 		assertEquals("MyModel", camelCase("MY_MODEL"));
 		assertEquals("Model", 	camelCase("MODEL"));
 		assertEquals("MyModel", camelCase("myModel"));
 		assertEquals("MyModel", camelCase("my model"));
 		assertEquals("MyModel", camelCase("MY MODEL"));
 		assertEquals("MyModel", camelCase("my  model"));
 		assertEquals("MyModel", camelCase("MY  MODEL"));
 		assertEquals("MyModel", camelCase("my_ Model"));
 		assertEquals("MyModel", camelCase("MY _ MODEL"));
 	}
 	
 	@Test
 	public void testTitleize() throws Exception {
 		assertEquals("Null", 		titleize(null));
 		assertEquals("Mymodel", 	titleize("mymodel"));
 		assertEquals("My Model", 	titleize("my_model"));
 		assertEquals("My Model", 	titleize("MY_MODEL"));
 		assertEquals("Model",		titleize("MODEL"));
 		assertEquals("My Model", 	titleize("MyModel"));
 		assertEquals("My Model", 	titleize("myModel"));
 		assertEquals("My Model", 	titleize("my model"));
 		assertEquals("My Model", 	titleize("MY MODEL"));
 		assertEquals("My Model", 	titleize("my  model"));
 		assertEquals("My Model", 	titleize("MY  MODEL"));
 		assertEquals("My Model", 	titleize("my_ Model"));
 		assertEquals("My Model", 	titleize("MY _ MODEL"));
 	}
 	
 	@Test
 	public void testUnderscore() throws Exception {
 		assertEquals("null",		underscored(null));
 		assertEquals("model",		underscored("model"));
 		assertEquals("model",		underscored("Model"));
 		assertEquals("my_model",	underscored("MyModel"));
 		assertEquals("my_model",	underscored("My Model"));
 		assertEquals("a_model",		underscored("A Model"));
 		assertEquals("a_model",		underscored("AModel"));
 		assertEquals("abc_model",	underscored("ABCModel"));
 		assertEquals("com.test.ab",	underscored("com.test.Ab"));
		assertEquals("esp_files",	underscored("ESP Files"));
 	}
 	
 	@Test
 	public void testVarName() throws Exception {
 		assertNull(varName((String) null));
 		assertEquals("mymodel", varName("mymodel"));
 		assertEquals("myModel", varName("my_model"));
 		assertEquals("myModel", varName("MY_MODEL"));
 		assertEquals("model",	varName("MODEL"));
 		assertEquals("myModel", varName("myModel"));
 		assertEquals("myModel", varName("my model"));
 		assertEquals("myModel", varName("MY MODEL"));
 		assertEquals("myModel", varName("my  model"));
 		assertEquals("myModel", varName("MY  MODEL"));
 		assertEquals("myModel", varName("my_ Model"));
 		assertEquals("myModel", varName("MY _ MODEL"));
 	}
 	
 	@Test
 	public void testColumnName() throws Exception {
 		assertEquals("null",		columnName(null));
 		assertEquals("model",		columnName("model"));
 		assertEquals("model",		columnName("Model"));
 		assertEquals("my_model",	columnName("MyModel"));
 		assertEquals("my_model",	columnName("My Model"));
 		assertEquals("a_model",		columnName("A Model"));
 		assertEquals("a_model",		columnName("AModel"));
 		assertEquals("abc_model",	columnName("ABCModel"));
 	}
 	
 	@Test
 	public void testHtmlEscape() throws Exception {
 		assertEquals("is a &gt; 0 &amp; a &lt; 10?", htmlEscape("is a > 0 & a < 10?"));
 	}
 	
 	@Test
 	public void testJsonEscape() throws Exception {
 		assertEquals("is a \u003E 0 \u0026 a \u003C 10?", jsonEscape("is a > 0 & a < 10?"));
 	}
 	
 	@Test
 	public void testSplitParam() throws Exception {
 		assertArrayEquals(new String[] {"client"}, splitParam("client"));
 		assertArrayEquals(new String[] {"client", "name"}, splitParam("client[name]"));
 		assertArrayEquals(new String[] {"client", "address", "city"}, splitParam("client[address][city]"));
 	}
 	
 	@Test
 	public void testMapParamsNull() throws Exception {
 		assertEquals(null, mapParams(null));
 	}
 	
 	@Test
 	public void testMapParams0() throws Exception {
 		assertEquals(new HashMap<String, Object>(), mapParams(new HashMap<String, Object>()));
 	}
 	
 	@Test
 	public void testMapParams1() throws Exception {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("client", "joe");
 		
 		Map<String, Object> expected = new HashMap<String, Object>();
 		expected.put("client", "joe");
 		
 		assertEquals(expected, mapParams(params));
 	}
 	
 	@Test
 	public void testMapParams2() throws Exception {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("client[name]", "joe");
 		params.put("client[phone]", "12345");
 		
 		Map<String, Object> expected = new HashMap<String, Object>();
 		Map<String, Object> client = new HashMap<String, Object>();
 		client.put("name", "joe");
 		client.put("phone", "12345");
 		expected.put("client", client);
 		
 		assertEquals("{client={phone=12345, name=joe}, client[phone]=12345, client[name]=joe}", mapParams(params).toString());
 	}
 	
 	@Test
 	public void testMapParams3() throws Exception {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("client[name]", "joe");
 		params.put("client[phone]", "12345");
 		params.put("client[address][city]", "Carrot City");
 		params.put("client[address][zipcode]", "12345");
 		
 		Map<String, Object> expected = new HashMap<String, Object>();
 		Map<String, Object> client = new HashMap<String, Object>();
 		client.put("name", "joe");
 		client.put("phone", "12345");
 		Map<String, Object> address = new HashMap<String, Object>();
 		address.put("city", "Carrot City");
 		address.put("zipcode", "12345");
 		client.put("address", address);
 		expected.put("client", client);
 		
 		assertEquals("{client[address][city]=Carrot City, " +
 					  "client[phone]=12345, " +
 					  "client={phone=12345, " +
 					  		  "address={zipcode=12345, city=Carrot City}, " +
 					  		  "name=joe}, " +
 					  "client[name]=joe, " +
 					  "client[address][zipcode]=12345}", mapParams(params).toString());
 	}
 	
 }
