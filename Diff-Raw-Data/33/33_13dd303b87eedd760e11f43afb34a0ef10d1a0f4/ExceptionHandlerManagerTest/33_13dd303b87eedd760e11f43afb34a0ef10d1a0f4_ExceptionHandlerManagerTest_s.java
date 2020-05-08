 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.exception;
 
 import junit.framework.Assert;
 
 import org.eclipse.riena.internal.core.exceptionmanager.IExceptionHandlerDefinition;
 import org.eclipse.riena.internal.core.exceptionmanager.SimpleExceptionHandlerManager;
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.NonUITestCase;
 
 /**
  * 
  */
 @NonUITestCase
 public class ExceptionHandlerManagerTest extends RienaTestCase {
 
 	private SimpleExceptionHandlerManager manager;
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		manager = new SimpleExceptionHandlerManager();
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		manager = null;
 		super.tearDown();
 	}
 
 	public void testAddHandler() {
 		TestExceptionHandler testEH = new TestExceptionHandler("test.exception.handler1", null, null);
 		manager.update(new IExceptionHandlerDefinition[] { getTestDefinition(testEH) });
 
 		Exception exception = new Exception("test");
 		manager.handleException(exception);
 
 		Assert.assertEquals("expected exception", testEH.getThrowable(), exception);
 	}
 
 	private IExceptionHandlerDefinition getTestDefinition(final TestExceptionHandler testEH) {
 		return new IExceptionHandlerDefinition() {
 
 			public IExceptionHandler createExceptionHandler() {
 				return testEH;
 			}
 
 			public String getBefore() {
				return null;
 			}
 
 			public String getExceptionHandler() {
				return TestExceptionHandler.class.getName();
 			}
 
 			public String getName() {
 				return testEH.getName();
 			}
 		};
 	}
 
	public void testAddHandlerChain() {
		TestExceptionHandler testEH1 = new TestExceptionHandler("test.exception.handler1", null, null);
		TestExceptionHandler testEH2 = new TestExceptionHandler("test.exception.handler2", "test.exception.handler1",
				IExceptionHandlerManager.Action.OK);
		manager.update(new IExceptionHandlerDefinition[] { getTestDefinition(testEH1), getTestDefinition(testEH2) });

		Exception exception = new Exception("test");
		manager.handleException(exception);

		Assert.assertEquals("expected exception", testEH2.getThrowable(), exception);
		Assert.assertNull("expected no exception", testEH1.getThrowable());
	}

 }
