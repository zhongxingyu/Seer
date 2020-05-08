 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.core.tests;
 
 import java.io.ByteArrayInputStream;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.validators.core.IValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.eclipse.dltk.validators.internal.core.ValidatorDefinitionsContainer;
 import org.eclipse.dltk.validators.internal.core.ValidatorManager;
 
 public class ValidatorContainerTests extends TestCase {
 	public void testValidatorContainer001() throws Exception {
 		ValidatorDefinitionsContainer co = new ValidatorDefinitionsContainer();
 		assertNotNull(co);
 		SimpleValidatorType vt = findSimpleValidtor();
 		assertNotNull(vt);
 		assertTrue(vt.isConfigurable());
 		IValidator v1 = vt.createValidator("v1");
 		assertNotNull(v1);
 		co.addValidator(v1);
 		IValidator v2 = vt.createValidator("v2");
 		((SimpleValidator)v2).setValid(false);
 		assertNotNull(v2);
 		co.addValidator(v2);
 		String xml = co.getAsXML();
 		assertNotNull(xml);
 		System.out.println(xml);
		List validValidatorsList = co.getValidatorsList();
 		List validatorList = co.getValidatorList();
 		assertNotNull(validValidatorsList);
 		assertNotNull(validatorList);
 		assertTrue(validValidatorsList.contains(v1));
		assertTrue(validValidatorsList.contains(v2));
 		assertTrue(validatorList.contains(v1));
 		assertTrue(validatorList.contains(v2));
 		
 		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
 		ValidatorDefinitionsContainer co2 = ValidatorDefinitionsContainer.parseXMLIntoContainer(bais);
 		assertNotNull(co2);
 		List validatorList2 = co2.getValidatorList();
 		assertNotNull(validatorList2);
 		assertEquals(2, validatorList.size());
 		List validatorList3 = co2.getValidatorList("#");
 		assertNotNull(validatorList3);
 		assertEquals(2, validatorList.size());
 		for (int i = 0; i < validatorList2.size(); i++) {
 			IValidator v = (IValidator)validatorList2.get(i);
 			assertNotNull(v);
 			assertTrue(v.getID().equals("v1") || v.getID().equals("v2") );
 			if( v.getID().equals("v1")) {
 				assertTrue( ((SimpleValidator)v).valid );
 			}
 			if( v.getID().equals("v2")) {
 				assertFalse( ((SimpleValidator)v).valid );
 			}
 		}
 	}
 
 	private SimpleValidatorType findSimpleValidtor() throws CoreException {
 		IValidatorType[] allValidatorTypes;
 		allValidatorTypes = ValidatorManager.getAllValidatorTypes();
 		for (int i = 0; i < allValidatorTypes.length; i++) {
 			if( allValidatorTypes[i] instanceof SimpleValidatorType ) {
 				return (SimpleValidatorType)allValidatorTypes[i];
 			}
 		}
 		return null;
 	}
 }
