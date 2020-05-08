 /*******************************************************************************
  * Copyright (c) 2014 itemis AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mark Broerkens (itemis AG) - initial API and implementation
  *******************************************************************************/
 package org.eclipse.rmf.tests.reqif10.serialization.uc000.tc1000;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import org.eclipse.rmf.reqif10.AttributeDefinitionBoolean;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIFContent;
 import org.eclipse.rmf.reqif10.ReqIFHeader;
 import org.eclipse.rmf.tests.reqif10.serialization.util.AbstractTestCase;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class TC0001000ContainmentEStructuralFeatureTests extends AbstractTestCase {
 	static final String TEST_CASE_ID = "TC0001000";
 	static final String REFERENCE_DATA_FILENAME = getWorkingFileName(getReferenceDataFileName(TEST_CASE_ID, false));
 	static final String EXPORT_DATA_FILENAME = getWorkingFileName(getFirstExportFileName(TEST_CASE_ID, false));
 	static ReqIF originalReqIF = null;
 	static ReqIF loadedReqIF = null;
 
 	@BeforeClass
 	public static void setupOnce() throws Exception {
 		AbstractTestCase.setupOnce();
 		originalReqIF = new TC0001000ContainmentEStructuralFeatureModelBuilder().getReqIF();
 		saveReqIFFile(originalReqIF, REFERENCE_DATA_FILENAME);
 		loadedReqIF = loadReqIFFile(REFERENCE_DATA_FILENAME);
 	}
 
 	@Test
 	public void test0002ReferenceSingleUnsetDefaultValue() {
 		AttributeDefinitionBoolean attributeDefinitionBoolean = (AttributeDefinitionBoolean) loadedReqIF.getCoreContent().getSpecTypes().get(0)
 				.getSpecAttributes().get(0);
 		assertFalse(attributeDefinitionBoolean.isSetDefaultValue());
 		assertNull(attributeDefinitionBoolean.getDefaultValue());
 	}
 
 	@Test
 	public void test0003ReferenceSingleSetNotDefaultValue() {
 		assertTrue(loadedReqIF.isSetTheHeader());
 		assertNotNull(loadedReqIF.getTheHeader());
 	}
	
 	@Test
 	public void test0004ReferenceSingleSetDefaultValue() {
 		AttributeDefinitionBoolean attributeDefinitionBoolean = (AttributeDefinitionBoolean) loadedReqIF.getCoreContent().getSpecTypes().get(0)
 				.getSpecAttributes().get(1);
 		assertTrue(attributeDefinitionBoolean.isSetDefaultValue());
 		assertNull(attributeDefinitionBoolean.getDefaultValue());
 	}
 
 	@Test
 	public void test0006ReferenceManyUnsetDefaultValue() {
 		ReqIFContent reqIFContent = loadedReqIF.getCoreContent();
 		assertFalse(reqIFContent.isSetSpecRelationGroups());
 		assertTrue(reqIFContent.getSpecRelationGroups().isEmpty());
 	}
 
 	@Test
 	public void test0007ReferenceManySetNotDefaultValue() {
 		ReqIFContent reqIFContent = loadedReqIF.getCoreContent();
 		assertTrue(reqIFContent.isSetDatatypes());
 		assertFalse(reqIFContent.getDatatypes().isEmpty());
 	}
 
 	@Test
 	public void test0008ReferenceManySetDefaultValue() {
 		assertTrue(loadedReqIF.isSetToolExtensions());
 		assertTrue(loadedReqIF.getToolExtensions().isEmpty());
 	}
 
 	@Test
 	public void test0010AttributeSingleUnsetDefaultValue() {
 		ReqIFHeader reqIFHeader = loadedReqIF.getTheHeader();
 		assertFalse(reqIFHeader.isSetComment());
 		assertNull(reqIFHeader.getComment());
 	}
 
 	@Test
 	public void test0011AttributeSingleSetNotDefaultValue() {
 		ReqIFHeader reqIFHeader = loadedReqIF.getTheHeader();
 		assertTrue(reqIFHeader.isSetIdentifier());
		assertEquals("identifier", reqIFHeader.getIdentifier());
 	}
 
 	@Test
 	public void test0012AttributeSingleSetDefaultValue() {
 		ReqIFHeader reqIFHeader = loadedReqIF.getTheHeader();
 		assertTrue(reqIFHeader.isSetTitle());
 		assertEquals(null, reqIFHeader.getTitle());
 	}
 
 }
