 package com.sap.prd.mobile.ios.mios;
 
 /*
  * #%L
  * it-xcode-maven-plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.it.Verifier;
 import org.junit.Test;
 
 public class SpecificTargetTest extends XCodeTest
 {
   @Test
   public void buildSpecificTarget() throws Exception
   {
     final String testName = getTestName();
     final File remoteRepositoryDirectory = getRemoteRepositoryDirectory(getClass()
       .getName());
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     Map<String, String> additionalSystemProperties = new HashMap<String, String>();
     additionalSystemProperties.put("xcode.app.defaultConfigurations", "Release");
     additionalSystemProperties.put("xcode.app.defaultSdks", "iphoneos");
     additionalSystemProperties.put("xcode.target", "Target2");
 
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, "1.0." + String.valueOf(System.currentTimeMillis()));
     
     Verifier verifier = test(testName, new File(getTestRootDirectory(), "multiple-targets/MultipleTargets"), "compile", THE_EMPTY_LIST, additionalSystemProperties,
           pomReplacements, new NullProjectModifier());
     assertCorrectTargetBuild(new File(verifier.getBasedir(),
           verifier.getLogFileName()));
   }
 
   private void assertCorrectTargetBuild(File logFile) throws IOException
   {
     BufferedReader reader = new BufferedReader(new FileReader(logFile));
     try {
       String line;
       boolean target1Built = false;
       boolean target2Built = false;
       while ((line = reader.readLine()) != null)
       {
         target1Built |= line
          .matches("=== BUILD.*TARGET Target1 OF PROJECT MultipleTargets WITH CONFIGURATION Release ===");
         target2Built |= line
          .matches("=== BUILD.*TARGET Target2 OF PROJECT MultipleTargets WITH CONFIGURATION Release ===");
       }
       Assert.assertFalse("Target1 must not be built", target1Built);
       Assert.assertTrue("Target2 must be built", target2Built);
     }
     finally {
       IOUtils.closeQuietly(reader);
     }
   }
 }
