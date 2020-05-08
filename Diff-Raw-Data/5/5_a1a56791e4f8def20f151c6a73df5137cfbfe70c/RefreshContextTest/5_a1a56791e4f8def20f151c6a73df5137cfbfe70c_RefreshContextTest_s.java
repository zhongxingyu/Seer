 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.myfaces.scripting.core.refreshContext;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.myfaces.scripting.api.Configuration;
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 import org.apache.myfaces.scripting.refresh.RefreshContext;
 import org.apache.myfaces.scripting.refresh.ReloadingMetadata;
 import org.junit.Test;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Set;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * Testcases for the refresh context
  *
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  */
 
 public class RefreshContextTest {
     private static String JAVA_FILE_ENDING = ".java";
 
     private static String PROBE1 = "../../src/test/resources/compiler/TestProbe1.java";
     private static String PROBE2 = "../../src/test/resources/compiler/TestProbe2.java";
     private static String RESOURCES = "../../src/test/resources/";
 
     File probe1;
     File probe2;
     File root;
 
     public RefreshContextTest() {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
 
         String currentPath = loader.getResource("./").getPath();
         String sourcePath1 = currentPath + PROBE1;
         String sourcePath2 = currentPath + PROBE2;
         String rootPath = currentPath + RESOURCES;
 
         sourcePath1 = FilenameUtils.normalize(sourcePath1);
         sourcePath2 = FilenameUtils.normalize(sourcePath2);
         rootPath = FilenameUtils.normalize(rootPath);
 
         probe1 = new File(sourcePath1);
         probe2 = new File(sourcePath2);
         root = new File(rootPath);
 
         WeavingContext.setConfiguration(new Configuration());
         WeavingContext.getConfiguration().addSourceDir(ScriptingConst.ENGINE_TYPE_JSF_JAVA, root.getAbsolutePath());
         WeavingContext.setRefreshContext(new RefreshContext());
     }
 
     @Test
     public void testTaingLog() {
         RefreshContext ctx = WeavingContext.getRefreshContext();
        ctx.setTaintLogTimeout(3);
 
         ReloadingMetadata data = new ReloadingMetadata();
         data.setAClass(this.getClass());
         data.setTainted(true);
         data.setTimestamp(System.currentTimeMillis());
 
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
 
         assertTrue("three new entries in the log", ctx.getTaintHistory(0l).size() == 3);
         try {
            Thread.sleep(5);
         } catch (InterruptedException e) {
             fail(e.toString());
         }
         ctx.gcTaintLog();
         assertTrue("All entries gced", ctx.getTaintHistory(0l).size() == 0);
 
         ctx.setTaintLogTimeout(300000000l);
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
         ctx.gcTaintLog();
         assertTrue("three new entries in the log", ctx.getTaintHistory(0l).size() == 3);
 
     }
 
     @Test
     public void testTaintHistory() {
         RefreshContext ctx = WeavingContext.getRefreshContext();
         ctx.setTaintLogTimeout(3);
 
         ReloadingMetadata data = new ReloadingMetadata();
         data.setAClass(this.getClass());
         data.setTainted(true);
         data.setTimestamp(System.currentTimeMillis());
 
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
 
         Set<String> result = ctx.getTaintHistoryClasses(0l);
         assertTrue("Taint history contains", result.contains(this.getClass().getName()));
         assertTrue("Taint history size", result.size() == 1);
 
     }
 
     @Test
     public void testTaintHistoryLastNoOfEntroies() {
         RefreshContext ctx = WeavingContext.getRefreshContext();
         ctx.setTaintLogTimeout(3);
 
         ReloadingMetadata data = new ReloadingMetadata();
         data.setAClass(this.getClass());
         data.setTainted(true);
         data.setTimestamp(System.currentTimeMillis());
 
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
         ctx.addTaintLogEntry(data);
 
         Collection<ReloadingMetadata> result = ctx.getLastTainted(100);
         assertTrue("Taint history size", result.size() == 3);
         result = ctx.getLastTainted(2);
         assertTrue("Taint history size", result.size() == 2);
         result = ctx.getLastTainted(0);
         assertTrue("Taint history size", result.size() == 0);
 
     }
 
 
 }
