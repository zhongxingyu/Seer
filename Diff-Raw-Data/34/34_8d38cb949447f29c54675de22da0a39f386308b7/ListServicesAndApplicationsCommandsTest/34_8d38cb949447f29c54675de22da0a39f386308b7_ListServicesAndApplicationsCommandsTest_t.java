 package test.cli.cloudify;
 
 import java.io.IOException;
 
 import org.junit.Assert;
 import org.testng.annotations.Test;
 
 import framework.utils.LogUtils;
 
 public class ListServicesAndApplicationsCommandsTest extends AbstractLocalCloudTest{
 			
 
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void test() throws Exception{
 		checkListsBeforeInstallation(); 
 		installApplication();
 		checkListsAfterInstallation();
 	}
 	
 	private void installApplication() throws IOException, InterruptedException {
 		final String applicationDir = CommandTestUtils.getPath("apps/USM/usm/applications/simple");
 		String output = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";install-application " + applicationDir);
 		Assert.assertTrue(output.contains("Application simple installed successfully"));
 	}
 	
 	private void checkListsAfterInstallation() throws IOException, InterruptedException {
		String output = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";use-application simple;list-services");
 		output = output.toLowerCase();
		Assert.assertTrue("listing of application services failed.", output.contains("simple"));
 		
 		output = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-applications");
 		output = output.toLowerCase();
		Assert.assertTrue("command list-application failed.", output.contains("simple"));
 	}
 	
 	private void checkListsBeforeInstallation() throws IOException,
 			InterruptedException {
		
 		String output = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-services");
		
 		output = output.toLowerCase();
 		Assert.assertFalse("wrong output", output.contains("operation failed"));
 		LogUtils.log("assert that the output contains an empty line (the correct output when there are no services)");
		Assert.assertTrue("Output for list-services command does not contain a new line.", assertStringContainsOneEmptyLine(output));
		
 		output = CommandTestUtils.runCommandAndWait("connect " + restUrl + ";list-applications");
 		output = output.toLowerCase();
		Assert.assertFalse("list-applicartion command failed.", output.contains("operation failed"));
 		
 		LogUtils.log("assert that the output contains an management and an empty line (the correct output when there are no applications)");
		Assert.assertTrue("Output for list-services command does not contain a new line.", assertStringContainsOneEmptyLine(output));
		Assert.assertTrue("Output does not reveal the management service.", output.contains("management"));
	}

	private boolean assertStringContainsOneEmptyLine(String output) {
		String lines[] = output.split("\\r?\\n\\r?\\n");
		return lines.length == 2 ? true : false;
 	}
 }
