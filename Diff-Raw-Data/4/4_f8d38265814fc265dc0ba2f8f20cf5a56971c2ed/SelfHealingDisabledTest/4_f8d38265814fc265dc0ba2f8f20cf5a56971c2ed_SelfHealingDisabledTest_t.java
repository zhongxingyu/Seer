 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.LogUtils;
 import org.cloudifysource.dsl.internal.CloudifyConstants.USMState;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.cloudifysource.quality.iTests.framework.utils.usm.USMTestUtils;
 import org.cloudifysource.restclient.RestException;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.concurrent.TimeUnit;
 
 
 public class SelfHealingDisabledTest extends AbstractLocalCloudTest {
 
 	final private String INNER_RECIPE_DIR_PATH = CommandTestUtils
 			.getPath("src/main/resources/apps/USM/usm/groovy-inner-error");
 
     final private String RECIPE_DIR_PATH = CommandTestUtils
             .getPath("src/main/resources/apps/USM/usm/failedGroovy");
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testNoSelfHealing() throws IOException, InterruptedException,
 			PackagingException, DSLException, RestException {
 
 		final String result = installService(false);
 		assertTrue("Could not find expected error message",
 				result.contains("This is a failure test"));
 
 		final ProcessingUnit pu = findPU();
 
 		USMTestUtils.getUSMServiceState("default.groovy", admin);
 		final ProcessingUnitInstance pui = findPUI(pu);
 		final USMState state = USMTestUtils.getPUIUSMState(pui);
 		assertEquals(USMState.ERROR, state);
 
 		uninstallService();
 	}
 
    @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = false)
    public void testInitNoSelfHealing() throws IOException, InterruptedException,
             PackagingException, DSLException, RestException {
         long timeout = 120000;
         String failMsg = "attribute changed to more than 01 - test failed because service restarted";
         CommandTestUtils.runCommandUsingFile("connect " + restUrl + ";" +
                 "set-attributes -scope global '{\"GlobalAttribute\":\"0\"}';");
 
         new Thread("New Thread") {
             public void run(){
                 try {
                     installService(true);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }.start();
 
         try {
             repetitiveAssertTrue("Global attribute changed - service restarted", new AssertUtils.RepetitiveConditionProvider() {
                 @Override
                 public boolean getCondition() {
                     String globalAttributeResult = null;
                     boolean success = false;
                     try {
                         globalAttributeResult = runCommand("connect " + restUrl
                                 + ";" + "list-attributes -scope global;");
                         success = globalAttributeResult.toLowerCase().contains("{\"globalattribute\":\"011\"}");
 
                     } catch (Exception e) {
                         e.printStackTrace();
                         AssertFail("Failed to get global attribute");
                     }
                     finally {
                         return success;
                     }
                 }
             }, timeout);
 
             AssertFail(failMsg);
         }
         catch (AssertionError e){
             if (e.getMessage().equals(failMsg)){
                 uninstallService();
                 AssertFail(failMsg);
             }
             uninstallService();
             LogUtils.log("Caught an assertion error as expected - check passed");
         }
     }
 
 	private ProcessingUnitInstance findPUI(final ProcessingUnit pu)
 			throws UnknownHostException {
 		final boolean found = pu.waitFor(1, 30, TimeUnit.SECONDS);
 		assertTrue("Could not find instance of deployed service", found);
 
 		final ProcessingUnitInstance pui = pu.getInstances()[0];
 		return pui;
 	}
 
 	private ProcessingUnit findPU() {
 		final ProcessingUnit pu = this.admin.getProcessingUnits().waitFor(
 				ServiceUtils.getAbsolutePUName("default", "groovy"), 30,
 				TimeUnit.SECONDS);
 		assertNotNull("Could not find processing unit for installed service",
 				pu);
 		return pu;
 	}
 
 	private String installService(boolean inner) throws PackagingException, IOException,
 			InterruptedException, DSLException {
         final File serviceDir;
         serviceDir = inner ? new File(INNER_RECIPE_DIR_PATH) : new File(RECIPE_DIR_PATH);
         String path = inner ? INNER_RECIPE_DIR_PATH : RECIPE_DIR_PATH;
 		ServiceReader.getServiceFromDirectory(serviceDir).getService();
 		return CommandTestUtils.runCommandExpectedFail("connect " + restUrl
 				+ ";install-service -disableSelfHealing --verbose "
 				+ path);
 	}
 
 	private String uninstallService() throws PackagingException, IOException,
 			InterruptedException, DSLException {
 
 		return runCommand("connect " + restUrl
 				+ ";uninstall-service --verbose groovy");
 	}
 
 }
