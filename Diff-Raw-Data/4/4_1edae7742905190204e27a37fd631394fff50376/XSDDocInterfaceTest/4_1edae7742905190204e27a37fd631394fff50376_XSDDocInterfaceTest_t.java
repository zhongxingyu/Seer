 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser;
 
 import static org.junit.Assert.*;
 
 import org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface;
import org.ebayopensource.turmeric.tools.annoparser.config.ConfigurationReader;
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 import org.ebayopensource.turmeric.tools.annoparser.customparser.impl.TestAnnoParserClass;
 import org.ebayopensource.turmeric.tools.annoparser.parser.XSDParser;
 import org.ebayopensource.turmeric.tools.annoparser.parser.impl.XSDParserImpl;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.*;
 import java.io.InputStream;
 
 import javax.print.DocFlavor.URL;
 
 /**
  * The Class XSDDocInterfaceTest.
  *
  * @author sdaripelli
  */
 public class XSDDocInterfaceTest {
 
 	
 	private XSDDocInterface xsdIntf = null;
 	
 	/**
 	 * Sets the up.
 	 *
 	 * @throws Exception the exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		String xsdPath = this.getClass().getClassLoader().getResource("ebaySvc.wsdl").toExternalForm();
		ConfigurationReader.loadDefaultConfiguration();
 		Context.getContext().addParser("maxLength",new TestAnnoParserClass());
 		XSDParser parser = new XSDParserImpl();
 		xsdIntf = parser.parse(xsdPath);
 	}
 
 	/**
 	 * Tear down.
 	 *
 	 * @throws Exception the exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/**
 	 * Test get all elements.
 	 */
 	@Test
 	public void testGetAllElements() {
 		int noOfElementsFound = xsdIntf.getAllIndependentElements().size();
 		assertEquals(339, noOfElementsFound);
 	}
 
 	/**
 	 * Test get all enums.
 	 */
 	@Test
 	public void testGetAllEnums() {
 		int noOfEnums = xsdIntf.getAllEnums().size();
 		assertEquals(50, noOfEnums);
 	}
 
 	/**
 	 * Test get all simple types.
 	 */
 	@Test
 	public void testGetAllSimpleTypes() {
 		List<SimpleType> simpleTypes = xsdIntf.getAllSimpleTypes();
 		int noOfSimpleTypes = simpleTypes.size();
 		assertEquals(5,noOfSimpleTypes);
 		
 	}
 
 	/**
 	 * Test get all complex types.
 	 */
 	@Test
 	public void testGetAllComplexTypes() {
 		List<ComplexType> complexTypes = xsdIntf.getAllComplexTypes();
 		int noOfComplexTypes = complexTypes.size();
 		assertEquals(55,noOfComplexTypes);
 	}
 
 	/**
 	 * Test get element complex type map.
 	 */
 	@Test
 	public void testGetElementComplexTypeMap() {
 		Map elemCTypeMap = xsdIntf.getElementComplexTypeMap();
 		int noOfElems = elemCTypeMap.keySet().size();
 		assertEquals(47,noOfElems);
 		
 		int noOfCTypes = elemCTypeMap.values().size();
 		assertEquals(47,noOfCTypes);
 	}
 
 	/**
 	 * Test search c type.
 	 */
 	@Test
 	public void testSearchCType() {
 		ComplexType ctype = xsdIntf.searchCType("FindBestMatchItemDetailsByKeywordsInternalResponse");
 		assertEquals("FindBestMatchItemDetailsByKeywordsInternalResponse", ctype.getName());
 	}
 
 	/**
 	 * Test search simple type.
 	 */
 	@Test
 	public void testSearchSimpleType() {
 		List<SimpleType> stypes = xsdIntf.getAllSimpleTypes();
 		for(SimpleType stype: stypes) {
 			System.out.println("stype name" + stype.getName());
 		}
 		SimpleType stype = xsdIntf.searchSimpleType("BestMatchOutputSelectorType");
 		assertEquals("BestMatchOutputSelectorType", stype.getName());
 	}
 
 	/**
 	 * Test search element.
 	 */
 	@Test
 	public void testSearchElement() {
 		Element elem = xsdIntf.searchIndependentElement("formatBucketParam");
 		assertEquals("formatBucketParam", elem.getName());
 	}
 
 	
 
 	
 	
 
 
 }
