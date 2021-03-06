 /*******************************************************************************
  * Copyright (c) 2004 - 2005 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Jun 10, 2005
   */
 package org.eclipse.mylar.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.mylar.core.tests.TaskscapeTest;
 import org.eclipse.mylar.java.tests.JavaStructureTest;
 import org.eclipse.mylar.java.tests.TaskscapeManagerTest;
 import org.eclipse.mylar.java.tests.search.JUnitReferencesSearchPluginTest;
 import org.eclipse.mylar.java.tests.search.JavaImplementorsSearchPluginTest;
 import org.eclipse.mylar.java.tests.search.JavaReadAccessSearchPluginTest;
 import org.eclipse.mylar.java.tests.search.JavaReferencesSearchPluginTest;
 import org.eclipse.mylar.java.tests.search.JavaWriteAccessSearchPluginTest;
 import org.eclipse.mylar.tasks.bugzilla.tests.BugzillaSearchPluginTest;
 import org.eclipse.mylar.tasks.bugzilla.tests.BugzillaStackTraceTest;
 import org.eclipse.mylar.tasks.tests.TaskListManagerTest;
 import org.eclipse.mylar.xml.tests.XMLSearchPluginTest;
 	
 public class AllTests {
 
     public static Test suite() {
         TestSuite suite = new TestSuite("Test for org.eclipse.mylar.tests");
         //$JUnit-BEGIN$
 
         // Java Tests
         suite.addTestSuite(TaskscapeManagerTest.class);
         suite.addTestSuite(JavaStructureTest.class);
         suite.addTestSuite(JavaImplementorsSearchPluginTest.class);
         suite.addTestSuite(JavaReadAccessSearchPluginTest.class);
         suite.addTestSuite(JavaReferencesSearchPluginTest.class);
         suite.addTestSuite(JavaWriteAccessSearchPluginTest.class);
         suite.addTestSuite(JUnitReferencesSearchPluginTest.class);
 
         // Bugzilla Tests - XXX FAIL
         suite.addTestSuite(BugzillaSearchPluginTest.class);
         suite.addTestSuite(BugzillaStackTraceTest.class);
         
         // Xml Tests 
         suite.addTestSuite(XMLSearchPluginTest.class);
         
         // Tasklist Tests
         suite.addTestSuite(TaskListManagerTest.class);
 //        suite.addTestSuite(XStreamUsageTest.class);
 
         // Core Tests
         suite.addTestSuite(TaskscapeTest.class);
 //        suite.addTestSuite(InteractionEventExternalizationTest.class);
 //        suite.addTestSuite(ManualUiTest.class);

         //$JUnit-END$
         return suite;
     }
 
 }
