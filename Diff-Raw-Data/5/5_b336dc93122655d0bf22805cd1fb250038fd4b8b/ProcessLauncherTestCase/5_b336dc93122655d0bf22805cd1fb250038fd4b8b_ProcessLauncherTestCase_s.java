 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf.processes.launcher;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.runtime.utils.Host;
 import org.eclipse.tcf.te.tcf.processes.core.interfaces.launcher.IProcessLauncher;
 import org.eclipse.tcf.te.tcf.processes.core.launcher.ProcessLauncher;
 import org.eclipse.tcf.te.tests.tcf.TcfTestCase;
 
 /**
  * Process launcher test cases.
  */
 public class ProcessLauncherTestCase extends TcfTestCase {
 
 	/**
 	 * Provides a test suite to the caller which combines all single
 	 * test bundled within this category.
 	 *
 	 * @return Test suite containing all test for this test category.
 	 */
 	public static Test getTestSuite() {
 		TestSuite testSuite = new TestSuite("Test TCF process launcher framework"); //$NON-NLS-1$
 
 			// add ourself to the test suite
 			testSuite.addTestSuite(ProcessLauncherTestCase.class);
 
 		return testSuite;
 	}
 
 	//***** BEGIN SECTION: Single test methods *****
 	//NOTE: All method which represents a single test case must
 	//      start with 'test'!
 
 	public void testProcessLauncher() {
 		assertNotNull("Test peer missing.", peer); //$NON-NLS-1$
 
 		// Determine the location of the "HelloWorld" executable
 		IPath path = getHelloWorldLocation();
 		assertTrue("Missing hello world example for current OS and Arch:" + Platform.getOS() + "/" + Platform.getOSArch(), //$NON-NLS-1$ //$NON-NLS-2$
 						path != null && path.toFile().exists() && path.toFile().canRead());
 
 		// Create the process streams proxy
 		ProcessStreamsProxy proxy = new ProcessStreamsProxy();
 		// Create the process launcher
 		ProcessLauncher launcher = new ProcessLauncher(proxy);
 
 		// Create the launch properties
 		IPropertiesContainer properties = new PropertiesContainer();
 		properties.setProperty(IProcessLauncher.PROP_PROCESS_PATH, path.toString());
 		properties.setProperty(IProcessLauncher.PROP_PROCESS_ASSOCIATE_CONSOLE, true);
 
 		// Launch the process
 		launcher.launch(peer, properties, new Callback() {
 			@Override
 			protected void internalDone(Object caller, IStatus status) {
 				if (status.getSeverity() != IStatus.OK && status.getSeverity() != IStatus.INFO) {
 					System.out.println("ProcessLauncherTestCase: launch returned with status:\n" + status.toString()); //$NON-NLS-1$
 				}
 			}
 		});
 
 		// Read the output from the proxy
 		String output = null;
		int counter = 20;
 		while (counter > 0) {
			output = proxy.getOutputReader().getOutput();
 			if (output != null && !"".equals(output.trim())) break; //$NON-NLS-1$
 			waitAndDispatch(2000);
 			counter--;
 		}
 		assertEquals("Unexpected output from HelloWorld test application.", "Hello World", output != null ? output.trim() : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 
 	//***** END SECTION: Single test methods *****
 
 	private IPath getHelloWorldLocation() {
 		IPath path = getDataLocation("helloWorld", true, true); //$NON-NLS-1$
 		if (path != null) {
 			path = path.append("HelloWorld"); //$NON-NLS-1$
 			if (Host.isWindowsHost()) {
 				path = path.addFileExtension("exe"); //$NON-NLS-1$
 			}
 		}
 
 		return path;
 	}
 }
