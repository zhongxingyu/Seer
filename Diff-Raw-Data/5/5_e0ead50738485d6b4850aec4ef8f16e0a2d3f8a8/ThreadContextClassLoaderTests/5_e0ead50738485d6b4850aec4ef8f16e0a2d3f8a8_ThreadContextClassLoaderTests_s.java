 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.deployer.test;
 
 import java.io.File;
 
 import org.junit.Assert;
import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Test deploying an application which has one module which checks that an exported package from the other module is available via the thread
  * context class loader.
  * 
  */
@Ignore("Bug 361635")
 public class ThreadContextClassLoaderTests extends AbstractParTests {
     
     private static final String BUNDLE_A_SYMBOLIC_NAME = "tccltest-1-BundleA";
 
    @Ignore("Bug 361635")
     @Test public void testThreadContextClassLoader() throws Throwable {
         File file = new File("src/test/resources/tccltest.par");
         deploy(file);
         Assert.assertNotNull(ApplicationContextUtils.getApplicationContext(this.context, BUNDLE_A_SYMBOLIC_NAME));
     }
 }
