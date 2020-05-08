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
 package org.eclipse.riena.core;
 
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.internal.tests.Activator;
 import org.eclipse.riena.tests.RienaTestCase;
 import org.eclipse.riena.tests.collect.NonUITestCase;
 
 /**
  * Test the {@code Log4r} class.
  */
 @NonUITestCase
 public class Log4rTest extends RienaTestCase {
 
	private String savedValue;

 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		assertNotNull("Test must be a plugin unit test.", Activator.getDefault());
		savedValue = System.getProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY);
 		System.clearProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY);
 	}
 
	@Override
	protected void tearDown() throws Exception {
		System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, savedValue);
		super.tearDown();
	}

 	public void testWithContext() {
 		Logger logger = Log4r.getLogger(Activator.getDefault(), Log4rTest.class);
 		assertNotNull(logger);
 		assertFalse("ConsoleLogger".equals(logger.getClass().getSimpleName()));
 		assertFalse("NullLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 	public void testWithOutContextNoRienaDefaultLogging() {
 		Logger logger = Log4r.getLogger(null, Log4rTest.class);
 		assertNotNull(logger);
 		assertTrue("ConsoleLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 	public void testWithOutContextWithRienaDefaultLoggingFalse() {
 		System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, Boolean.FALSE.toString());
 		Logger logger = Log4r.getLogger(null, Log4rTest.class);
 		assertNotNull(logger);
 		assertTrue("NullLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 	public void testWithOutContextWithRienaDefaultLoggingTrue() {
 		System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, Boolean.TRUE.toString());
 		Logger logger = Log4r.getLogger(null, Log4rTest.class);
 		assertNotNull(logger);
 		assertTrue("ConsoleLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 	public void testWithContextByName() {
 		Logger logger = Log4r.getLogger(Activator.getDefault(), Log4rTest.class.getName());
 		assertNotNull(logger);
 		assertFalse("ConsoleLogger".equals(logger.getClass().getSimpleName()));
 		assertFalse("NullLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 	public void testWithOutContextNoRienaDefaultLoggingByName() {
 		Logger logger = Log4r.getLogger(null, Log4rTest.class.getName());
 		assertNotNull(logger);
 		assertTrue("ConsoleLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 	public void testWithOutContextWithRienaDefaultLoggingFalseByName() {
 		System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, Boolean.FALSE.toString());
 		Logger logger = Log4r.getLogger(null, Log4rTest.class.getName());
 		assertNotNull(logger);
 		assertTrue("NullLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 	public void testWithOutContextWithRienaDefaultLoggingTrueByName() {
 		System.setProperty(RienaStatus.RIENA_DEVELOPMENT_SYSTEM_PROPERTY, Boolean.TRUE.toString());
 		Logger logger = Log4r.getLogger(null, Log4rTest.class.getName());
 		assertNotNull(logger);
 		assertTrue("ConsoleLogger".equals(logger.getClass().getSimpleName()));
 	}
 
 }
