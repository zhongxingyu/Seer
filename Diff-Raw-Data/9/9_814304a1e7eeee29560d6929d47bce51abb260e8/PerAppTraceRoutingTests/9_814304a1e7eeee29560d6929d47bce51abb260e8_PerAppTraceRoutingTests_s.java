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
 
 package org.eclipse.virgo.web.test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import org.eclipse.virgo.util.io.FileSystemUtils;
 
 /**
  */
@Ignore("Fails on the build server, possibly due to Linux's scheduling")
 public class PerAppTraceRoutingTests extends AbstractWebIntegrationTests {
 
     @Before
     public void deleteTraceFiles() {
         FileSystemUtils.deleteRecursively("target/per-app-logging.war-0");
     }
 
     @Test
     public void perAppTraceRouting() throws Exception {
         assertDeployAndUndeployBehavior("per-app-logging", new File("src/test/apps-static/per-app-logging.war"), "");
 
         File traceFile = new File("target/per-app-logging.war-0/trace.log");
         assertTrue(traceFile.exists());
         List<String> expectedText = new ArrayList<String>();
 
         expectedText.addAll(Arrays.asList(new String[] { "System.out I System.out from PerAppLoggingTest",
             "System.err E System.err from PerAppLoggingTest" }));
 
         try {
             // Sleep for five seconds to give the trace a chance to have been flushed
             Thread.yield();
             Thread.sleep(5000);
         } catch (InterruptedException ie) {
 
         }
 
         BufferedReader reader = new BufferedReader(new FileReader(traceFile));
         String line = reader.readLine();
 
         while (line != null && !expectedText.isEmpty()) {
             Iterator<String> expected = expectedText.iterator();
             while (expected.hasNext()) {
                 if (line.contains(expected.next())) {
                     expected.remove();
                 }
             }
             line = reader.readLine();
         }
 
         assertTrue("Expected text " + expectedText + " was not found in the application's trace file", expectedText.isEmpty());
     }
 }
