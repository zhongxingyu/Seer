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
 
 package org.eclipse.virgo.test.framework.dmkernel;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
import java.util.Dictionary;
import java.util.Hashtable;
 
 import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runners.model.InitializationError;
 import org.osgi.framework.BundleContext;
 
 import org.eclipse.virgo.teststubs.osgi.framework.StubBundleContext;
 import org.eclipse.virgo.teststubs.osgi.support.TrueFilter;
 
 public class DmKernelTestRunnerTests {
 
  private DmKernelTestRunner testRunner;
 
  private final StubBundleContext kernelBundleContext = new StubBundleContext();
 
  @Before public void setup() throws InitializationError { this.testRunner = new DmKernelTestRunner(getClass(), 1000); this.kernelBundleContext.addFilter("(org.eclipse.virgo.kernel.regionContext=true)", new
 TrueFilter()); }
 
  @Test(expected=IllegalStateException.class) public void failureWhenUserRegionBundleContextIsNotPresent() { long start = System.currentTimeMillis(); try {
 this.testRunner.getTargetBundleContext(this.kernelBundleContext); } finally { assertTrue(System.currentTimeMillis() - start >= 1000); } }
 
  @Test public void userRegionBundleContextRetrievedFromServiceRegistry() { StubBundleContext userRegionBundleContext = new StubBundleContext();
 
 Dictionary<String, Object> properties = new Hashtable<String, Object>(); properties.put("org.eclipse.virgo.kernel.regionContext", true); this.kernelBundleContext.registerService(BundleContext.class.getName(), userRegionBundleContext,
 properties);
 
  assertEquals(userRegionBundleContext, this.testRunner.getTargetBundleContext(this.kernelBundleContext)); }
 
  @Test(expected=IllegalStateException.class) public void failureWhenMultipleUserRegionBundleContextsAreAvailable() { StubBundleContext userRegionBundleContext = new StubBundleContext();
 
 Dictionary<String, Object> properties = new Hashtable<String, Object>(); properties.put("org.eclipse.virgo.kernel.regionContext", true); this.kernelBundleContext.registerService(BundleContext.class.getName(), userRegionBundleContext,
 properties); this.kernelBundleContext.registerService(BundleContext.class.getName(), userRegionBundleContext, properties);
 
  assertEquals(userRegionBundleContext, this.testRunner.getTargetBundleContext(this.kernelBundleContext)); } }
