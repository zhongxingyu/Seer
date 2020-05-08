 /**
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    dvorak - initial API and implementation
  */
 package org.eclipse.gmf.tests.validate;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 
 public class AllValidateTests {
 
 	public static Test suite() {
 		TestSuite suite = new TestSuite("Test for org.eclipse.gmf.tests.validate"); //$NON-NLS-1$
 		//$JUnit-BEGIN$
 		suite.addTestSuite(ConstraintSeverityTest.class);
		suite.addTestSuite(OCLExpressionAdapterTest.class);
// FIXME next two tests were commented out due to bug #230418
//		suite.addTestSuite(ConstraintDefTest.class);		
//		suite.addTestSuite(ValueSpecDefTest.class);		
 		//$JUnit-END$
 		return suite;
 	}
 
 }
