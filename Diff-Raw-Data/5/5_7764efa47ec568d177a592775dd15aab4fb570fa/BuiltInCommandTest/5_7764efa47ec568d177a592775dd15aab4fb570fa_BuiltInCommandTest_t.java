 package org.cloudifysource.quality.iTests.test.cli.cloudify;
 
 import iTests.framework.utils.LogUtils;
 
 import java.io.IOException;
 
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.testng.annotations.Test;
 
 public class BuiltInCommandTest extends AbstractLocalCloudTest{
 
 	public static final String SERVICE_NAME = "simple";
 	public static final String SERVICE_FOLDER_NAME = "simple";
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testUSMInstall() throws IOException, InterruptedException {
 		String servicePath = getUsmServicePath(SERVICE_FOLDER_NAME);
 		LogUtils.log("Installing service " + SERVICE_NAME);
 		CommandTestUtils.runCommandAndWait("connect " + restUrl + 
 				";install-service --verbose " + servicePath);
 
 		String output = CommandTestUtils.runCommandAndWait("connect " + restUrl
				+ ";invoke " + SERVICE_NAME + " cloudify:start-maintenance-mode 5");
 		assertTrue("expection sucess status output. instead output was: " + output,
 				output.contains("agent failure detection disabled successfully for a period of 5 minutes"));
 
 
 		output = CommandTestUtils.runCommandAndWait("connect " + restUrl 
				+ ";invoke " + SERVICE_NAME + " cloudify:stop-maintenance-mode");
 		assertTrue("expection sucess status output. instead output was: " + output,
 				output.contains("agent failure detection enabled successfully"));
 	}
 
 	private String getUsmServicePath(String dirOrFilename) {
 		return CommandTestUtils.getPath("src/main/resources/apps/USM/usm/" + dirOrFilename);
 	}
 }
