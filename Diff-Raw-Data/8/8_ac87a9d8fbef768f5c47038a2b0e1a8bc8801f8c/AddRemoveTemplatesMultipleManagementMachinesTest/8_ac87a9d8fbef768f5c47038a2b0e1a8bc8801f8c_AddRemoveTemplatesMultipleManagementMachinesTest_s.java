 package test.cli.cloudify.cloud.byon;
 
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.SSHUtils;
 
 public class AddRemoveTemplatesMultipleManagementMachinesTest extends AbstractByonAddRemoveTemplatesTest{
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
	public void addRemoveTempaltes() throws Exception {
 				
 		TemplatesBatchHandler handler = new TemplatesBatchHandler();
 		TemplateDetails template = handler.addTemplate();
 		String templateName = template.getTemplateName();
 		addTempaltes(handler);
 		assertExpectedListTempaltes();
 				
 		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();	
 
 		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, true);
 		verifyTemplateExistence(mngMachinesIP[1], template, templateRemotePath, true);
 							
 		removeTemplate(handler, templateName, false, null);
 		assertExpectedListTempaltes();
 		
 		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, false);
 		verifyTemplateExistence(mngMachinesIP[1], template, templateRemotePath, false);
 			
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
	public void failedAddRemoveTempaltes() throws Exception {
 		
 		TemplatesBatchHandler handler = new TemplatesBatchHandler();
 		TemplateDetails template = handler.addTemplate();
 		String templateName = template.getTemplateName();
 		addTempaltes(handler);
 		
 		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();
 		
		SSHUtils.runCommand(mngMachinesIP[1], OPERATION_TIMEOUT, "rm -f " + templateRemotePath, USER, PASSWORD);
 		
 		String output = removeTemplate(handler, templateName, true, null);
 		
 		AssertUtils.assertTrue("successfully removed template from " + mngMachinesIP[1], output.contains("Failed to remove"));
 		AssertUtils.assertTrue("successfully removed template from " + mngMachinesIP[1], output.contains(templateName));
 		AssertUtils.assertTrue("successfully removed template from " + mngMachinesIP[1], output.contains(mngMachinesIP[1]));
 		
 		handler.addExpectedToFailTemplate(template);
 		output = addTempaltes(handler);
 				
 		int failedIndex = output.indexOf("Failed to add the following templates:");
 		AssertUtils.assertTrue("successfully added " + templateName + " to " + mngMachinesIP[1], failedIndex != -1);
 
 		int successIndex = output.indexOf("Successfully added the following templates:");
 		AssertUtils.assertTrue("failed to add " + templateName + " to " + mngMachinesIP[0], successIndex != -1);
 		
 		String failedOutput = output.substring(failedIndex, successIndex);
 		String successOutput = output.substring(successIndex);
 		
 		AssertUtils.assertTrue("successfully added " + templateName + " to " + mngMachinesIP[1], failedOutput.contains(mngMachinesIP[1]));
 		AssertUtils.assertTrue("successfully added " + templateName + " to " + mngMachinesIP[1], failedOutput.contains(templateName));
 
 		AssertUtils.assertTrue("failed to add " + templateName + " to " + mngMachinesIP[0], successOutput.contains(mngMachinesIP[0]));
 		AssertUtils.assertTrue("failed to add " + templateName + " to " + mngMachinesIP[0], successOutput.contains(templateName));
 		
 	}
 
 	protected void startManagement(String machine1) throws Exception {
 		
 		for (int i = 0 ; i < 3 ; i++) {
 			try {
 				LogUtils.log(SSHUtils.runCommand(machine1, DEFAULT_TEST_TIMEOUT,  ByonCloudService.BYON_HOME_FOLDER + "/gigaspaces/tools/cli/cloudify.sh start-management", ByonCloudService.BYON_CLOUD_USER, ByonCloudService.BYON_CLOUD_PASSWORD));
 				return;
 			} catch (Throwable t) {
 				LogUtils.log("Failed to start management on machine " + machine1 + ". Attempt number " + (i + 1));
 			}
 		}
 		
 		AssertUtils.assertFail("Failed to start management on machine " + machine1 + ".");
 	}
 	
 	@Override
 	public boolean isBootstrap() {
 		return true;
 	}
 
 	@Override
 	public boolean isTeardown() {
 		return true;
 	}
 
 	@Override
 	public int getNumOfMngMachines() {
 		return 2;
 	}
 
 
 
 }
