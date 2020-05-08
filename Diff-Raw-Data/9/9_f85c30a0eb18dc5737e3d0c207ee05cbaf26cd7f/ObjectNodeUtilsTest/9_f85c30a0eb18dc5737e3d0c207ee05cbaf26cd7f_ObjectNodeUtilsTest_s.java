 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.runtime.tests.parser.objectnode;
 
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamReader;
 
 import org.ebayopensource.turmeric.junit.rules.TestingDir;
 import org.ebayopensource.turmeric.runtime.binding.impl.parser.objectnode.ObjectNodeStreamReader;
 import org.ebayopensource.turmeric.runtime.binding.objectnode.ObjectNode;
 import org.ebayopensource.turmeric.runtime.binding.utils.ObjectNodeUtils;
 import org.ebayopensource.turmeric.runtime.tests.common.AbstractTurmericTestCase;
 import org.junit.Assert;
 import org.junit.Rule;
 import org.junit.Test;
 
 import com.ebay.kernel.util.FileUtils;
 
 public class ObjectNodeUtilsTest extends AbstractTurmericTestCase {
 
 	@Rule 
 	public TestingDir testingdir = new TestingDir();
 	
 	@Test
 	public void testPrintNode() throws Exception {
 		// ObjectNode representation for books.xml and traverse to the root
 		// element
 		InputStream inStream = ObjectNodeUtilsTest.class.getResourceAsStream("books.xml");
 		XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(inStream);
 		ObjectNodeStreamReader oNodeReader = new ObjectNodeStreamReader(xmlStreamReader);
 		ObjectNode rootNodeON = oNodeReader.getObjectNode();
 
 		StringWriter sw = new StringWriter();
 		
 		ObjectNodeUtils.writeAsXML(rootNodeON, sw, false, "utf-8");
 		
 		FileWriter tmpFW = new FileWriter(testingdir.getFile("books.exp.xml"));
 		tmpFW.write(sw.toString());
 		tmpFW.close();
 		
 		String exp = FileUtils.getResourceAsString(ObjectNodeUtilsTest.class, "books.exp.xml");
 		Assert.assertEquals("Printed version ain't matching them great expectations", exp, sw.toString());
 		
 		sw.getBuffer().setLength(0);
 		ObjectNodeUtils.writeAsXML(rootNodeON, sw, true, "utf-8");
 		
 		tmpFW = new FileWriter(testingdir.getFile("booksPretty.exp.xml"));
 		tmpFW.write(sw.toString());
 		tmpFW.close();
		
		String expPretty = FileUtils.getResourceAsString(ObjectNodeUtilsTest.class, "booksPretty.exp.xml");
		Assert.assertEquals("Printed version ain't matching them great expectations", expPretty, sw.toString());
 	}
 
 }
