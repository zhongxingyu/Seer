 /*
  * ******************************************************************************
  *  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
  *  *
  *  * Licensed under the Apache License, Version 2.0 (the "License");
  *  * you may not use this file except in compliance with the License.
  *  * You may obtain a copy of the License at
  *  *
  *  *       http://www.apache.org/licenses/LICENSE-2.0
  *  *
  *  * Unless required by applicable law or agreed to in writing, software
  *  * distributed under the License is distributed on an "AS IS" BASIS,
  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  * See the License for the specific language governing permissions and
  *  * limitations under the License.
  *  ******************************************************************************
  */
 
 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.ScriptUtils;
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.domain.cloud.ScriptLanguages;
 import org.cloudifysource.esc.installer.EnvironmentFileBuilder;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import org.cloudifysource.utilitydomain.openspaces.OpenspacesConstants;
 import org.cloudifysource.utilitydomain.openspaces.OpenspacesDomainUtils;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.nio.file.Paths;
 import java.util.HashMap;
 import java.util.concurrent.TimeoutException;
 
 /**
  * Tests the functionality of the generic bootstrap-management script we provide.
  *
  * NOTE : This test cannot run a windows machine.
  *
  * @author Eli Polonsky
  * @since 3.0.0
  */
 public class BootstrapManagementScriptTest {
 
     /**
      * We use byon since it use tarzan as the storage for java and cloudify installations.
      * This will also serve as our running directory.
      */
     private static ByonCloudService byonCloudService;
 
     /**
      * Create the {@link ByonCloudService} we will use.
      *
      * @throws Exception In case of an initialization error.
      */
     @BeforeClass
     public static void prepareService() throws Exception {
 
         if (ScriptUtils.isWindows()) {
             throw new UnsupportedOperationException("Cannot run this test on a windows box since it runs a shell "
                     + "script");
         }
 
         byonCloudService = new ByonCloudService();
         byonCloudService.init(BootstrapManagementScriptTest.class.getSimpleName());
         byonCloudService.injectCloudAuthenticationDetails();
 
         // replace text in cloud driver
         IOUtils.replaceTextInFile(byonCloudService.getPathToCloudGroovy(),
                 byonCloudService.getAdditionalPropsToReplace());
 
         // create the properties file with the credentails.
         IOUtils.writePropertiesToFile(byonCloudService.getProperties(),
                 new File(byonCloudService.getPathToCloudFolder() + "/" +  byonCloudService.getCloudName()
                         + "-cloud.properties"));
     }
 
     /**
      *
      * CLOUDIFY-2204.
      *
      * Run the bootstrap-management script and verify:
      *
      * 1. Gigaspaces tarball is deleted from the home directory.
      * 2. Java is deleted from the home directory.
      *
      * @throws Exception In case of an unexpected failure.
      */
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT)
     public void testCleanHomeDirectory() throws Exception {
 
         runBootstrapScript("management");
 
         File gigaspacesTar = new File(byonCloudService.getPathToCloudFolder(), "gigaspaces.tar.gz");
         AssertUtils.assertFalse("Found " + gigaspacesTar.getAbsolutePath() + " after the bootstrap-management script"
                 + " ended", gigaspacesTar.exists());
 
         File javaBin = new File(byonCloudService.getPathToCloudFolder(), "java.bin");
         AssertUtils.assertFalse("Found " + javaBin.getAbsolutePath() + " after the bootstrap-management script"
                 + " ended", javaBin.exists());
     }
 
     /**
      * Cleans the /tmp/${user} directory from the host.
      * @throws TimeoutException In case the delete operation times out.
      */
     @AfterMethod
     public void cleanup() throws TimeoutException {
         ScriptUtils.executeCommandLine("rm -rf /tmp/" + System.getProperty("user.name"),
                 AbstractTestSupport.DEFAULT_TEST_TIMEOUT);
     }
 
     /**
      * Clean the machine by killing all java processes that were started during the bootstrap stage.
      *
      * @throws TimeoutException In case the kill operation times out.
      */
     @AfterClass
     public void clean() throws TimeoutException {
         if (SGTestHelper.isDevMode() && ScriptUtils.isLinuxMachine()) {
             throw new UnsupportedOperationException("Cannot kill all java processes on a linux box in dev mode since "
                     + "this will kill the IDE process. Please make sure all java processes started by this test are "
                     + "shutdown");
         }
         ScriptUtils.executeCommandLine("killall -9 java", AbstractTestSupport.DEFAULT_TEST_TIMEOUT);
     }
 
 
 
     /**
      * Setup the shared runtime env for running the script.
      *
      * 1. Copy the script to a temp folder
      * 2. Copy the cloud file to the same temp folder
      * 3. Prepare shared environment variables.
      * 4. Source this environment to the bootstrap-management script.
      * 5. Run the script.
      *
      * NOTE : We run the script so that it will fail fast.
      *        We are not interested in actually launching the agent. // TODO - Are we?
      *
      *
      * @param mode The mode to run the script in. 'agent' or 'management'.
      *
      * @throws Exception In case one of the operations failed.
      */
     private void runBootstrapScript(final String mode) throws Exception {
 
         EnvironmentFileBuilder environmentFileBuilder = new EnvironmentFileBuilder(ScriptLanguages.LINUX_SHELL,
                 new HashMap<String, String>());
 
         environmentFileBuilder.exportVar("LUS_IP_ADDRESS", "127.0.0.1:" + OpenspacesConstants.DEFAULT_LUS_PORT);
         environmentFileBuilder.exportVar("GSA_MODE", mode);
         environmentFileBuilder.exportVar("MACHINE_IP_ADDRESS", "127.0.0.1");
         environmentFileBuilder.exportVar("GIGASPACES_LINK",
                 OpenspacesDomainUtils.getCloudDependentConfig().getDownloadUrl());
         environmentFileBuilder.exportVar("WORKING_HOME_DIRECTORY", byonCloudService.getPathToCloudFolder());
         environmentFileBuilder.exportVar("CLOUD_FILE", new File(byonCloudService.getPathToCloudGroovy())
                 .getAbsolutePath());
 
         environmentFileBuilder.build();
 
         String environmentFileName = environmentFileBuilder.getEnvironmentFileName();
 
         File envFile = new File(byonCloudService.getPathToCloudFolder(), environmentFileName);
         FileUtils.writeStringToFile(envFile, environmentFileBuilder.toString());
 
        File pathToScript = new File(byonCloudService.getPathToCloudFolder() + File.separator + "upload",
                "bootstrap-management.sh");
 
         ScriptUtils.executeCommandLine("bash " + pathToScript.getAbsolutePath(),
                 AbstractTestSupport.DEFAULT_TEST_TIMEOUT);
     }
 
 }
