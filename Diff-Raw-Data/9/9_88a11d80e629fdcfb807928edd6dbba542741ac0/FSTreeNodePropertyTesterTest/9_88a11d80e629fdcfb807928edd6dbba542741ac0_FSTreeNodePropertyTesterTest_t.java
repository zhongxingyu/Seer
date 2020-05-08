 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf.filesystem.testers;
 
 import org.eclipse.tcf.te.runtime.utils.Host;
 import org.eclipse.tcf.te.tcf.filesystem.internal.testers.FSTreeNodePropertyTester;
 import org.eclipse.tcf.te.tests.tcf.filesystem.FSPeerTestCase;
 
 @SuppressWarnings("restriction")
 public class FSTreeNodePropertyTesterTest extends FSPeerTestCase {
 	private FSTreeNodePropertyTester tester;
     @Override
     protected void setUp() throws Exception {
 	    super.setUp();
 	    tester = new FSTreeNodePropertyTester();
     }
 
 	public void testIsFile() {
 		assertTrue(tester.test(testFile, "isFile", null, null)); //$NON-NLS-1$
 		assertFalse(tester.test(testFolder, "isFile", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsDirectory() {
 		assertFalse(tester.test(testFile, "isDirectory", null, null)); //$NON-NLS-1$
 		assertTrue(tester.test(testFolder, "isDirectory", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsReadable() {
 		assertTrue(tester.test(testFile, "isReadable", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsWritable() {
 		assertTrue(tester.test(testFile, "isWritable", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsExecutable() {
		assertTrue(tester.test(testFile, "isExecutable", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsRoot() {
 		assertFalse(tester.test(testFolder, "isRoot", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsSystemRoot() {
 		assertFalse(tester.test(testFolder, "isSystemRoot", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsWindows() {
 		if(Host.isWindowsHost()) {
 			assertTrue(tester.test(testFile, "isWindows", null, null)); //$NON-NLS-1$
 		}
 		else {
 			assertFalse(tester.test(testFile, "isWindows", null, null)); //$NON-NLS-1$
 		}
 	}
 	
 	public void testIsReadOnly() {
 		assertFalse(tester.test(testFile, "isReadOnly", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testIsHidden() {
 		assertFalse(tester.test(testFile, "isHidden", null, null)); //$NON-NLS-1$
 	}
 	
 	public void testGetCacheState() {
 		assertFalse(tester.test(testFile, "getCacheState", null, "consistent")); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 	
 	public void testTestParent() {
 		assertTrue(tester.test(test11File, "testParent", new Object[]{"testParent", "testParent", "isDirectory"}, null)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 	}
 }
