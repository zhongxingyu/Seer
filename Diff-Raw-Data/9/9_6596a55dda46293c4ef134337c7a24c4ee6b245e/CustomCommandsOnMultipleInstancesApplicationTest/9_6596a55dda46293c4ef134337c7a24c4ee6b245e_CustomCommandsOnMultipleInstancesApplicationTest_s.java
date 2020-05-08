 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import framework.utils.DumpUtils;
 import framework.utils.TeardownUtils;
 import junit.framework.Assert;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 import com.gigaspaces.cloudify.dsl.internal.DSLException;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 
 import framework.utils.LogUtils;
 
 public class CustomCommandsOnMultipleInstancesApplicationTest extends AbstractLocalCloudTest {
 
     private final String APPLICAION_DIR_PATH = CommandTestUtils
             .getPath("apps/USM/usm/applications/simpleCustomCommandsMultipleInstances");
     private int totalInstancesService2;
 
     @BeforeClass
     public void beforeClass() throws Exception{
         super.beforeClass();
         installApplication();
         String absolutePUNameSimple1 = ServiceUtils.getAbsolutePUName("simpleCustomCommandsMultipleInstances", "simpleCustomCommandsMultipleInstances-1");
         String absolutePUNameSimple2 = ServiceUtils.getAbsolutePUName("simpleCustomCommandsMultipleInstances", "simpleCustomCommandsMultipleInstances-2");
         ProcessingUnit pu1 = admin.getProcessingUnits().waitFor(absolutePUNameSimple1, WAIT_FOR_TIMEOUT, TimeUnit.SECONDS);
         ProcessingUnit pu2 = admin.getProcessingUnits().waitFor(absolutePUNameSimple2, WAIT_FOR_TIMEOUT, TimeUnit.SECONDS);
         assertNotNull(pu1);
         assertNotNull(pu2);
         assertTrue("applications was not installed", pu1.waitFor(pu1.getTotalNumberOfInstances(), WAIT_FOR_TIMEOUT, TimeUnit.SECONDS));
         assertTrue("applications was not installed", pu2.waitFor(pu2.getTotalNumberOfInstances(), WAIT_FOR_TIMEOUT, TimeUnit.SECONDS));
         assertNotNull("applications was not installed", admin.getApplications().getApplication("simpleCustomCommandsMultipleInstances"));
         assertTrue("USM Service State is NOT RUNNING", USMTestUtils.waitForPuRunningState(absolutePUNameSimple1, 60, TimeUnit.SECONDS, admin));
         assertTrue("USM Service State is NOT RUNNING", USMTestUtils.waitForPuRunningState(absolutePUNameSimple2, 60, TimeUnit.SECONDS, admin));
         totalInstancesService2 = pu2.getTotalNumberOfInstances();
     }
 
     @Override
     @AfterMethod
     public void afterTest() {
         TeardownUtils.snapshot(admin);
         DumpUtils.dumpLogs(admin);
     }
 
     @AfterClass
     public void afterClass() throws IOException, InterruptedException {
         runCommand("connect " + restUrl +
                 ";uninstall-application --verbose simpleCustomCommandsMultipleInstances");
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testPrintCommandOnApp() throws Exception {
         LogUtils.log("Checking print command on all instances");
         checkPrintCommandOnapp();
 
         LogUtils.log("Starting to check print command by instance id");
         for (int i = 1; i <= totalInstancesService2; i++)
             checkPrintCommandOnApp(i);
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testParamsCommand() throws Exception {
         LogUtils.log("Checking params command on all instances");
         checkParamsCommand();
 
         LogUtils.log("Starting to check params command by instance id");
         for (int i = 1; i <= totalInstancesService2; i++)
             checkParamsCommand(i);
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testExceptionCommand() throws Exception {
         LogUtils.log("Checking exception command on all instances");
         checkExceptionCommand();
 
         LogUtils.log("Starting to check exception command by instance id");
         for (int i = 1; i <= totalInstancesService2; i++)
             checkExceptionCommand(i);
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testRunScriptCommand() throws Exception {
         LogUtils.log("Checking runScript command on all instances");
         checkRunScriptCommand();
 
         LogUtils.log("Starting to check runScript command by instance id");
         for (int i = 1; i <= totalInstancesService2; i++)
             checkRunScriptCommand(i);
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
     public void testContextCommand() throws Exception {
         LogUtils.log("Checking context command on all instances");
         checkContextCommandOnApp();
 
         LogUtils.log("Starting to check context command by instance id");
         for (int i = 1; i <= totalInstancesService2; i++)
             checkContextCommandOnApp(i);
     }
 
     private void checkPrintCommandOnapp() throws IOException, InterruptedException {
         String invokePrintResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke simpleCustomCommandsMultipleInstances-2 print");
 
         assertTrue("Custom command 'print' returned unexpected result from instance #1: " + invokePrintResult
                 , invokePrintResult.contains("OK from instance #1"));
         assertTrue("Custom command 'print' returned unexpected result from instance #2: " + invokePrintResult
                 , invokePrintResult.contains("OK from instance #2"));
     }
 
     private void checkPrintCommandOnApp(int instanceId) throws IOException, InterruptedException {
         String invokePrintResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke -instanceid " + instanceId + " simpleCustomCommandsMultipleInstances-2 print");
 
         assertTrue("Custom command 'print' returned unexpected result from instance #" + instanceId + ": " + invokePrintResult
                 , invokePrintResult.contains("OK from instance #" + instanceId));
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             if (i == instanceId)
                 continue;
             Assert.assertFalse("should not recive any output from instance" + i, invokePrintResult.contains("instance #" + i));
         }
     }
 
     private void checkParamsCommand() throws IOException, InterruptedException {
         String invokeParamsResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke simpleCustomCommandsMultipleInstances-2 params 2 3");
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             assertTrue("Custom command 'params' returned unexpected result from instance #" + i + ": " + invokeParamsResult
                     , invokeParamsResult.contains("OK from instance #" + i) && invokeParamsResult.contains("Result: this is the custom parameters command. expecting 123: 123"));
         }
     }
 
     private void checkParamsCommand(int instanceId) throws IOException, InterruptedException {
         String invokeParamsResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                + "; invoke -instanceid " + instanceId + " simpleCustomCommandsMultipleInstances-2 params");
 
         assertTrue("Custom command 'params' returned unexpected result from instance #" + instanceId + ": " + invokeParamsResult
                 , invokeParamsResult.contains("OK from instance #" + instanceId) && invokeParamsResult.contains("Result: this is the custom parameters command. expecting 123: 123"));
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             if (i == instanceId)
                 continue;
             Assert.assertFalse("should not recive any output from instance" + i, invokeParamsResult.contains("instance #" + i));
         }
     }
 
     private void checkExceptionCommand() throws IOException, InterruptedException {
         String invokeExceptionResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke simpleCustomCommandsMultipleInstances-2 exception");
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             assertTrue("Custom command 'exception' returned unexpected result from instance #" + i + ": " + invokeExceptionResult
                     , invokeExceptionResult.contains("FAILED from instance #" + i) && invokeExceptionResult.contains("This is an error test"));
         }
     }
 
     private void checkExceptionCommand(int instanceId) throws IOException, InterruptedException {
         String invokeExceptionResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke -instanceid " + instanceId + " simpleCustomCommandsMultipleInstances-2 exception");
 
         assertTrue("Custom command 'exception' returned unexpected result from instance #" + instanceId + ": " + invokeExceptionResult
                 , invokeExceptionResult.contains("FAILED from instance #" + instanceId) && invokeExceptionResult.contains("This is an error test"));
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             if (i == instanceId)
                 continue;
             Assert.assertFalse("should not recive any output from instance" + i, invokeExceptionResult.contains("instance #" + i));
         }
     }
 
     private void checkRunScriptCommand() throws IOException, InterruptedException {
         String invokeRunScriptResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke simpleCustomCommandsMultipleInstances-2 runScript");
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             assertTrue("Custom command 'runScript' returned unexpected result from instance #" + i + ": " + invokeRunScriptResult
                     , invokeRunScriptResult.contains("OK from instance #" + i) && invokeRunScriptResult.contains("Result: 2"));
         }
     }
 
     private void checkRunScriptCommand(int instanceId) throws IOException, InterruptedException {
         String invokeRunScriptResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke -instanceid " + instanceId + " simpleCustomCommandsMultipleInstances-2 runScript");
 
         assertTrue("Custom command 'exception' returned unexpected result from instance #" + instanceId + ": " + invokeRunScriptResult
                 , invokeRunScriptResult.contains("OK from instance #" + instanceId) && invokeRunScriptResult.contains("Result: 2"));
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             if (i == instanceId)
                 continue;
             Assert.assertFalse("should not recive any output from instance" + i, invokeRunScriptResult.contains("instance #" + i));
         }
     }
 
     private void checkContextCommandOnApp() throws IOException, InterruptedException {
         String invokeContextResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke simpleCustomCommandsMultipleInstances-2 context");
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             assertTrue("Custom command 'runScript' returned unexpected result from instance #" + i + ": " + invokeContextResult
                     , invokeContextResult.contains("OK from instance #" + i) && invokeContextResult.contains("Service Dir is:"));
         }
     }
 
     private void checkContextCommandOnApp(int instanceId) throws IOException, InterruptedException {
         String invokeContextResult = runCommand("connect " + restUrl + ";use-application simpleCustomCommandsMultipleInstances"
                 + "; invoke -instanceid " + instanceId + " simpleCustomCommandsMultipleInstances-2 context");
 
         assertTrue("Custom command 'exception' returned unexpected result from instance #" + instanceId + ": " + invokeContextResult
                 , invokeContextResult.contains("OK from instance #" + instanceId) && invokeContextResult.contains("Service Dir is:"));
 
         for (int i = 1; i <= totalInstancesService2; i++) {
             if (i == instanceId)
                 continue;
             Assert.assertFalse("should not recive any output from instance" + i, invokeContextResult.contains("instance #" + i));
         }
     }
 
     private void installApplication() throws PackagingException, IOException, InterruptedException, DSLException {
         File applicationDir = new File(APPLICAION_DIR_PATH);
         ServiceReader.getApplicationFromFile(applicationDir).getApplication();
         runCommand("connect " + restUrl + ";install-application --verbose " + APPLICAION_DIR_PATH);
     }
 }
