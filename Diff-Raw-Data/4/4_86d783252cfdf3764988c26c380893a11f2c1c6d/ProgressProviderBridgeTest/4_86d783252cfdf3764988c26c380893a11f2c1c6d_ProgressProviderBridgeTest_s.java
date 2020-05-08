 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.core.uiprocess;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.jobs.Job;
 
 import org.eclipse.riena.core.singleton.SingletonProvider;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.ui.swt.uiprocess.SwtUISynchronizer;
 
 /**
  * Test for {@link ProgressProviderBridge}
  */
 @NonUITestCase
 public class ProgressProviderBridgeTest extends TestCase {
 
 	public void testGetRunningUIProcesses() throws Exception {
 		final ProgressProviderBridge bridge = new ProgressProviderBridge() {
 			@Override
 			protected void registerJobChangeListener() {
 				//dont register observer
 			}
 		};
 		final SingletonProvider<ProgressProviderBridge> singletonProvider = ReflectionUtils.getHidden(
 				ProgressProviderBridge.class, "PPB");
 		ReflectionUtils.setHidden(singletonProvider, "singleton", bridge);
 
 		Job.getJobManager().setProgressProvider(bridge);
 
 		final UIProcess p1 = new UIProcess("p1", new SwtUISynchronizer(), true, new Object());
 		final List<UIProcess> runningUIProcesses = bridge.getRegisteredUIProcesses();
 		assertEquals(1, runningUIProcesses.size());
 		assertEquals(p1, runningUIProcesses.get(0));
		bridge.unregisterMapping(p1.getJob());
 		assertEquals(0, bridge.getRegisteredUIProcesses().size());

 	}
 
 }
