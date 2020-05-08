 package test.cli.cloudify.cloud.azure;
 
 import org.cloudifysource.esc.driver.provisioning.azure.client.MicrosoftAzureRestClient;
 import org.cloudifysource.esc.driver.provisioning.azure.model.HostedService;
 import org.cloudifysource.esc.driver.provisioning.azure.model.HostedServices;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.CloudServiceManager;
 import test.cli.cloudify.cloud.services.azure.MicrosoftAzureCloudService;
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 
 /**
  * CLOUDIFY-1431
  * @author elip
  *
  */
 public class LeakingCloudServiceTest extends NewAbstractCloudTest {
 	
 	private String leakingServiceName;
 	private MicrosoftAzureRestClient azureClient;
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testLeakingCloudService() throws Exception {
		
		String expectedOutput = "The supplied password must be 6-72 characters long and meet password complexity requirements";
		
 		MicrosoftAzureCloudService azureCloudService = (MicrosoftAzureCloudService) CloudServiceManager.getInstance().getCloudService(getCloudName());
 		
 		// this password is invalid due to azure password restrictions.
 		azureCloudService.setPassword("1234");
 		azureCloudService.getBootstrapper().setBootstrapExpectedToFail(true);
 		
 		// this caused the bug. a cloud service is created before the new machine is requested.
 		// because of the invalid request, the machine provisioning fails. but the cloud service
 		// created beforehand was not deleted.
 		super.bootstrap(azureCloudService);
 		String bootstrapOutput = azureCloudService.getBootstrapper().getLastActionOutput();
		AssertUtils.assertTrue("Bootstrap failed but did not contain the necessary output " + expectedOutput, bootstrapOutput.toLowerCase().contains(expectedOutput.toLowerCase()));
 		
 		azureClient = azureCloudService.getRestClient();
 		
 		HostedServices cloudServices = azureClient.listHostedServices();
 		for (HostedService cloudService : cloudServices) {
 			cloudService = azureClient.getHostedService(cloudService.getServiceName(), true);
 			if (cloudService.getDeployments().getDeployments().isEmpty()) {
 				// zombi cloud service
 				leakingServiceName = cloudService.getServiceName();
 				AssertUtils.assertFail("Found a leaking cloud service due to a failed bootstrap operations : cloud service = " + cloudService.getServiceName());
 			}
 		}
 	}
 
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		if (leakingServiceName != null) {
 			LogUtils.log("Deleting leaking cloud service : " + leakingServiceName);
 			azureClient.deleteCloudService(leakingServiceName, System.currentTimeMillis() + 60 * 1000);
 		}
 	}
 	
 	@Override
 	protected String getCloudName() {
 		return "azure";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 
 }
