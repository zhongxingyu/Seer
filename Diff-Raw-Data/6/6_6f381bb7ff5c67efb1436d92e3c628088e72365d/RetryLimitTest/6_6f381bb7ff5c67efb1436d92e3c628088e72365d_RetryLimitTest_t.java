 /*******************************************************************************
  * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  ******************************************************************************/
 package org.cloudifysource.quality.iTests.test.cli.cloudify.retries;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.CloudifyConstants.USMState;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
 import org.cloudifysource.restclient.exceptions.RestClientException;
 import org.cloudifysource.utilitydomain.data.ServiceInstanceAttemptData;
 import org.openspaces.admin.internal.gsc.DefaultGridServiceContainer;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
 import org.openspaces.admin.space.Space;
 import org.openspaces.core.GigaSpace;
 import org.openspaces.pu.service.ServiceMonitors;
 import org.testng.annotations.Test;
 
 public class RetryLimitTest extends AbstractLocalCloudTest {
 
     private static int NUM_OF_RETRIES;
     private static int EXPECTED_NUM_OF_ATTEMPTS;
 
     private final class AdminListener implements ProcessingUnitInstanceLifecycleEventListener {
         private int instanceAdded;
         private int instanceRemoved;
 
         @Override
         public void processingUnitInstanceRemoved(ProcessingUnitInstance pui) {
             String puName = pui.getProcessingUnit().getName();
             if (puName.equals(absolutePUName)) {
                 LogUtils.log("Instance was removed");
                 instanceRemoved++;
             }
         }
 
         @Override
         public void processingUnitInstanceAdded(ProcessingUnitInstance pui) {
             String puName = pui.getProcessingUnit().getName();
             if (puName.equals(absolutePUName)) {
                 LogUtils.log("Instance was added");
                 instanceAdded++;
             }
         }
 
         public void assertResults() {
             assertEquals("Expected instance added events are missing", EXPECTED_NUM_OF_ATTEMPTS, instanceAdded);
             assertEquals("Expected instance removed events are missing", EXPECTED_NUM_OF_ATTEMPTS - 1, instanceRemoved);
         }
 
         public void assertMultipleRetries() {
             assertTrue("Expected instance added events are missing", instanceAdded > 2);
            assertTrue("Expected instance removed events are missing", instanceRemoved > 1);
         }
     }
 
     private String serviceName;
     private String applicationName;
     private String absolutePUName;
     private final String serviceDir = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/groovy-error");
     private final String appDir = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/applications/retryBadApp");
     private String groovyBackupFilePath;
     private String propsBackupFilePath;
 
     private String installCommand;
     private String unInstallCommand;
 
     private AdminListener eventListener;
     private GigaSpace managementSpace;
 
     /**
      * The proper behavior of the start detection should be: if the start detection is a java groovy file, exiting with
      * exit code 0 should be considered as true and any other exit code should be considered as false.
      *
      * @throws IOException
      * @throws InterruptedException
      * @throws RestClientException
      */
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2)
     public void testRetryLimit() throws Exception {
 
         serviceName = "groovyError";
         applicationName = "default";
         absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
         NUM_OF_RETRIES = 1;
         EXPECTED_NUM_OF_ATTEMPTS = NUM_OF_RETRIES + 1;
         retryLimitGeneralTest();
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2)
     public void testRetryLimitZero() throws Exception {
 
         serviceName = "groovyError";
         applicationName = "default";
         absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
         NUM_OF_RETRIES = 0;
         EXPECTED_NUM_OF_ATTEMPTS = NUM_OF_RETRIES + 1;
         retryLimitGeneralTest();
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2)
     public void testRetryForever() throws Exception {
 
         serviceName = "groovyError";
         applicationName = "default";
         absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
         NUM_OF_RETRIES = -1;
         retryLimitGeneralTest();
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2)
     public void testRetryLimitWithDisableSelfHealing() throws Exception {
 
         serviceName = "groovyError";
         applicationName = "default";
         absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
         NUM_OF_RETRIES = 2;
         EXPECTED_NUM_OF_ATTEMPTS = 1;
         retryLimitGeneralTest(true, false, false, false);
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2)
     public void testRetryLimitWithDefaultValue() throws Exception {
 
         serviceName = "groovyError";
         applicationName = "default";
         absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
         NUM_OF_RETRIES = 1;
         retryLimitGeneralTest(false, true, false, false);
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2)
     public void testRetryLimitNotElastic() throws Exception {
 
         serviceName = "groovyError";
         applicationName = "default";
         absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
         NUM_OF_RETRIES = 1;
         EXPECTED_NUM_OF_ATTEMPTS = NUM_OF_RETRIES + 1;
         retryLimitGeneralTest(false, false, false, true);
     }
 
     @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2)
     public void testRetryLimitWithBadApp() throws Exception {
 
         applicationName = "retryGroovyApp";
         serviceName = "groovy-error";
         absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
         NUM_OF_RETRIES = 1;
         EXPECTED_NUM_OF_ATTEMPTS = NUM_OF_RETRIES + 1;
         retryLimitGeneralTest(false, false, true, false);
     }
 
     private void initTest(int numOfRetries, boolean disableSelfHealing, boolean deleteRetryField, boolean installApp, boolean notElastic) throws Exception {
 
         Space adminSpace = admin.getSpaces().waitFor(CloudifyConstants.MANAGEMENT_SPACE_NAME, 10, TimeUnit.SECONDS);
         assertNotNull(adminSpace);
         managementSpace = adminSpace.getGigaSpace();
         ServiceInstanceAttemptData attemptData = managementSpace.read(new ServiceInstanceAttemptData());
         assertTrue("Found attempt data in space before test started", attemptData == null);
 
         initFields(disableSelfHealing, installApp);
         eventListener = new AdminListener();
 
         String groovyFilePath = serviceDir + "/error-service.groovy";
         groovyBackupFilePath = IOUtils.backupFile(groovyFilePath);
 
         String propertiesFilePath = serviceDir + "/error-service.properties";
         propsBackupFilePath = IOUtils.backupFile(propertiesFilePath);
 
         if(!installApp){
             if(deleteRetryField){
                 IOUtils.replaceTextInFile(groovyFilePath, "retries retriesLimit", "");
             }
             if(notElastic){
                 IOUtils.replaceTextInFile(groovyFilePath, "elastic true", "elastic false");
             }
             else{
                 IOUtils.replaceTextInFile(propertiesFilePath, "retriesLimit = 1", "retriesLimit = " + numOfRetries);
             }
         }
     }
 
     public void retryLimitGeneralTest() throws Exception {
         retryLimitGeneralTest(false, false, false, false);
     }
 
 
     public void retryLimitGeneralTest(boolean disableSelfHealing, boolean deleteRetryField, boolean installApp, boolean notElastic) throws Exception {
 
         initTest(NUM_OF_RETRIES, disableSelfHealing, deleteRetryField, installApp, notElastic);
 
         try {
 
             admin.getProcessingUnits().addLifecycleListener(eventListener);
 
             LogUtils.log("installing with retry limit value: " + NUM_OF_RETRIES);
             String output = CommandTestUtils.runCommandExpectedFail(installCommand);
 
             if(NUM_OF_RETRIES <= -1 || deleteRetryField){
                 eventListener.assertMultipleRetries();
             }
             else {
                 eventListener.assertResults();
             }
 
             if(NUM_OF_RETRIES > 0 && !disableSelfHealing && !deleteRetryField){
 
                 ServiceInstanceAttemptData attemptData = managementSpace.read(new ServiceInstanceAttemptData());
                 assertEquals("application name in attempt data in the space is wrong", applicationName, attemptData.getApplicationName());
                 assertEquals("attempt number in attempt data in the space is wrong", EXPECTED_NUM_OF_ATTEMPTS, attemptData.getCurrentAttemptNumber().intValue());
                 assertEquals("servic name in attempt data in the space is wrong", serviceName, attemptData.getServiceName());
                 assertEquals("instance id in attempt data in the space is wrong", attemptData.getInstanceId().intValue(), admin.getProcessingUnits().getProcessingUnit(absolutePUName).getInstances()[0].getInstanceId());
                 DefaultGridServiceContainer defaultGSC = (DefaultGridServiceContainer)admin.getProcessingUnits().getProcessingUnit(absolutePUName).getInstances()[0].getGridServiceContainer();
                 assertEquals("PID in attempt data in the space is wrong", defaultGSC.getJVMDetails().getPid(), attemptData.getGscPid());
             }
 
             if(NUM_OF_RETRIES > -1 && !deleteRetryField){
 
                 final ProcessingUnitInstance pui = admin.getProcessingUnits().getProcessingUnit(absolutePUName).getInstances()[0];
                 repetitiveAssertTrue("Instance did not reach ERROR state", new AssertUtils.RepetitiveConditionProvider() {
 
                     @Override
                     public boolean getCondition() {
                         ServiceMonitors serviceMonitors = pui.getStatistics().getMonitors().get("USM");
                         assertNotNull(serviceMonitors);
                         Object usmState = serviceMonitors.getMonitors().get("USM_State");
                         LogUtils.log("USM state is: " + usmState);
                         assertEquals(USMState.ERROR.ordinal(), usmState);
                         return true;
                     }
                 }, 30000);
             }
 
         } finally {
             IOUtils.replaceFileWithMove(new File(serviceDir + "/error-service.groovy"), new File(groovyBackupFilePath));
             IOUtils.replaceFileWithMove(new File(serviceDir + "/error-service.properties"), new File(propsBackupFilePath));
            admin.getProcessingUnits().removeLifecycleListener(eventListener);
         }
 
         CommandTestUtils.runCommandAndWait(unInstallCommand);
         ServiceInstanceAttemptData attemptDataAfterInstall = managementSpace.read(new ServiceInstanceAttemptData());
         assertTrue("Found attempt data in space after uninstall", attemptDataAfterInstall == null);
 
     }
 
     private void initFields(boolean disableSelfHealing, boolean installApp) {
 
         if(installApp){
             installCommand = "connect " + this.restUrl + ";install-application --verbose -timeout 5 " + appDir + ";exit";
         }
         else if(disableSelfHealing){
             installCommand = "connect " + this.restUrl + ";install-service --verbose -disableSelfHealing -timeout 2 " + serviceDir + ";exit";
         }
         else{
             installCommand = "connect " + this.restUrl + ";install-service --verbose -timeout 2 " + serviceDir + ";exit";
         }
 
         if(installApp){
             unInstallCommand = "connect " + this.restUrl + ";uninstall-application --verbose " + applicationName + ";exit";
         }
         else{
             unInstallCommand = "connect " + this.restUrl + ";uninstall-service --verbose " + serviceName + ";exit";
         }
     }
 
 }
