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
 package com.sap.prd.mobile.ios.mios;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.apache.maven.it.Verifier;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class VersionWithAppAsSnapshotTest extends XCodeTest
 {
   private static File remoteRepositoryDirectory = null, appTestBaseDir = null;
   private static String dynamicVersion = null,
         testName = null;
 
   private static Verifier appVerifier = null;
   
   @BeforeClass
   public static void __setup() throws Exception {
 
     dynamicVersion = "1.0." + String.valueOf(System.currentTimeMillis());
     testName = VersionWithAppAsSnapshotTest.class.getName() +  File.separator + Thread.currentThread().getStackTrace()[1].getMethodName();
 
     remoteRepositoryDirectory = getRemoteRepositoryDirectory(VersionWithAppAsSnapshotTest.class.getName());
 
     prepareRemoteRepository(remoteRepositoryDirectory);
 
     Properties pomReplacements = new Properties();
     pomReplacements.setProperty(PROP_NAME_DEPLOY_REPO_DIR, remoteRepositoryDirectory.getAbsolutePath());
     pomReplacements.setProperty(PROP_NAME_DYNAMIC_VERSION, dynamicVersion);
 
     test(testName, new File(getTestRootDirectory(), "straight-forward-with-app-as-snapshot/MyLibrary"), "deploy",
           THE_EMPTY_LIST, THE_EMPTY_MAP, pomReplacements);
     
     appVerifier = test(testName, new File(getTestRootDirectory(), "straight-forward-with-app-as-snapshot/MyApp"),
           "deploy",
           THE_EMPTY_LIST,
           null, pomReplacements);    
 
     appTestBaseDir = new File(appVerifier.getBasedir());
   }
 
   @Test
   public void testCFBundeShortVersionInInfoPlist() throws Exception
   {    
     final File infoPList = new File(appTestBaseDir, "target/checkout/src/xcode/build/Release-iphoneos/MyApp.app/Info.plist");
     assertEquals("CFBundleShortVersion in file '" + infoPList + "' is not the expected version '" + dynamicVersion + "'.", dynamicVersion, new PListAccessor(infoPList).getStringValue(PListAccessor.KEY_BUNDLE_SHORT_VERSION_STRING));
   }
 
   @Test
   public void testCFBundeVersionInInfoPlist() throws Exception
   {
     final File infoPList = new File(appTestBaseDir, "target/checkout/src/xcode/build/Release-iphoneos/MyApp.app/Info.plist");
    assertEquals("CFBundleVersion in file '" + infoPList + "' is not the expected version '" + dynamicVersion + "-SNAPSHOT'.", dynamicVersion + "-SNAPSHOT", new PListAccessor(infoPList).getStringValue(PListAccessor.KEY_BUNDLE_VERSION));
   }
 }
