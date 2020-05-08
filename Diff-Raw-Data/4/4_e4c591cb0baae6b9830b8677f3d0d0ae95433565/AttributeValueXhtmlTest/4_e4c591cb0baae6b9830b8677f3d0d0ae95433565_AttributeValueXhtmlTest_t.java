 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.provider;
 
 import org.eclipse.rmf.reqif10.AttributeValueXHTML;
 import org.eclipse.rmf.reqif10.ReqIF10Factory;
 import org.junit.After;
 import org.junit.Before;
 
 /**
  * A test case for the model object '<em><b>Attribute Value Complex</b></em>'.
  */
 public class AttributeValueXhtmlTest extends AttributeValueTest {
 
 	/**
 	 * Returns the fixture for this Attribute Value Complex test case.
 	 */
 	@Override
 	protected AttributeValueXHTML getFixture() {
 		return (AttributeValueXHTML)fixture;
 	}
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Before
 	public void setUpAttributeValueXhtmlTest() throws Exception {
 		setFixture(ReqIF10Factory.eINSTANCE.createAttributeValueXHTML());
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@After
 	public void tearDownAttributeValueXhtmlTest() throws Exception {
 		setFixture(null);
 	}
 
 	@Override
 	public Object getValueObject() {
		return ReqIF10Factory.eINSTANCE.createXhtmlContent();
 	}
 
 } //AttributeValueComplexTest
