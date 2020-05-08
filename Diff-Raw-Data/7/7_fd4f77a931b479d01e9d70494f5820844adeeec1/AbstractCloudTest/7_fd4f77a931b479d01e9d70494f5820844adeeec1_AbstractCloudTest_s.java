 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.testng.Assert;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.DataProvider;
 
 import test.AbstractTest;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.ec2.Ec2CloudService;
 import test.cli.cloudify.cloud.hp.HpCloudService;
 import test.cli.cloudify.cloud.rackspace.RackspaceCloudService;
 import test.cli.cloudify.cloud.terremark.TerremarkCloudService;
 import framework.utils.LogUtils;
 
 public class AbstractCloudTest extends AbstractTest {
 	
	private static final String[][] SUPPORTED_CLOUDS = {{"ec2"}};
 	
 	private CloudService service;
 	
 	/**
 	 * set the service CloudService instance to a specific cloud provider.
 	 * all install/uninstall commands will be executed on the specified cloud.
 	 * @param cloudName
 	 */
 	public void setCloudToUse(String cloudName) {
 		
 		if (cloudName.equals("ec2")) {
 			service = Ec2CloudService.getService();
 		}
 		if (cloudName.equals("openstack")) {
 			service = HpCloudService.getService();
 
 		}
 		if (cloudName.equals("terremark")) {
 			service = TerremarkCloudService.getService();
 		}
 	}
 	
 	public CloudService getService() {
 		return service;
 	}
 	
 	@DataProvider(name = "supportedClouds")
 	public String[][] supportedClouds() {
 		return SUPPORTED_CLOUDS;
 	}
 	
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		LogUtils.log("Test Configuration Started: " + this.getClass());
 	}
 	
 	/**
 	 * Before the suite starts bootstrap all clouds.
 	 */
 	@BeforeSuite(alwaysRun = true, enabled = true)
 	public void bootstrapSupportedClouds() {
 		
 		String clouds = "";
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++) {
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			clouds = clouds + supportedCloud + ",";
 		}
 		
 		
 		LogUtils.log("bootstrapping to clouds : " + clouds);
 		
         boolean success = false;
 		try {
         	bootstrapClouds();
         	LogUtils.log("Bootstrapping to clouds finished");
         	success = true;
 		} 
 		catch (IOException e) {
 			LogUtils.log("bootstrap-cloud failed.", e);
 		} 
 		catch (InterruptedException e) {
 			LogUtils.log("bootstrap-cloud failed.", e);
 		} 
 		catch (Exception e) {
 		    LogUtils.log("bootstrap-cloud failed.", e);
 		}
 		finally {
         	if (!success) {
         		teardownClouds();
         		Assert.fail("bootstrap-cloud failed.");
         	}
         }
 		
 		LogUtils.log("succefully bootstrapped to clouds : " + clouds);
 	}
 	
 	/**
 	 * After suite ends teardown all bootstrapped clouds.
 	 */
 	@AfterSuite(enabled = true)
 	public void teardownSupportedClouds() {
 		
 		String clouds = "";
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++) {
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			clouds = clouds + supportedCloud + ",";
 		}
 
 		
 		LogUtils.log("tearing down clouds : " + clouds);
 		
 		teardownClouds();	
 		
 		LogUtils.log("succefully teared down clouds : " + clouds);
 	}
 	
 	private void bootstrapClouds() throws IOException, InterruptedException {
 		
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++) {
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			if (supportedCloud.equals("ec2")) {
 				Ec2CloudService.getService().bootstrapCloud();
 			}
 			if (supportedCloud.equals("openstack")) {
 				HpCloudService.getService().bootstrapCloud();
 			}
 			if (supportedCloud.equals("terremark")) {
 				TerremarkCloudService.getService().bootstrapCloud();
 			}
 			if (supportedCloud.equals("rackspace")) {
 				RackspaceCloudService.getService().bootstrapCloud();
 			}
 		}
 	}
 	
 	@Override
	@BeforeMethod
 	public void afterTest() {
 		
 	}
 
 	
 	private void teardownClouds() {
 		
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++) {
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			if (supportedCloud.equals("ec2")) {
 				Ec2CloudService.getService().teardownCloud();
 			}
 			if (supportedCloud.equals("openstack")) {
 				HpCloudService.getService().teardownCloud();
 			}
 			if (supportedCloud.equals("terremark")) {
 				TerremarkCloudService.getService().teardownCloud();
 			}
 		}
 	}
 	
 	/**
 	 * installs a service on a specific cloud and waits for the installation to complete.
 	 * @param servicePath - full path to the -service.groovy file on the local file system.
 	 * @param serviceName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installServiceAndWait(String servicePath, String serviceName) throws IOException, InterruptedException {
 		
 		String connectCommand = "connect " + service.getRestUrl() + ";";
 		String installCommand = new StringBuilder()
 			.append("install-service ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 			.append((servicePath.toString()).replace('\\', '/'))
 			.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		String excpectedResult = serviceName + " service installed successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 	}
 	
 	/**
 	 * installs an application on a specific cloud and waits for the installation to complete.
 	 * @param applicationPath - full path to the -application.groovy file on the local file system.
 	 * @param applicationName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installApplicationAndWait(String applicationPath, String applicationName) throws IOException, InterruptedException {
 		
 		String connectCommand = "connect " + service.getRestUrl() + ";";
 		String installCommand = new StringBuilder()
 			.append("install-application ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 			.append((applicationPath.toString()).replace('\\', '/'))
 			.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		String excpectedResult = "Application " + applicationName + " installed successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 
 	}
 	
 	/**
 	 * uninstalls a service from a specific cloud and waits for the uninstallation to complete.
 	 * @param serviceName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void uninstallServiceAndWait(String serviceName) throws IOException, InterruptedException {
 		
 		String connectCommand = "connect " + service.getRestUrl() + ";";
 		String installCommand = new StringBuilder()
 			.append("uninstall-service ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 			.append(serviceName)
 			.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		String excpectedResult = serviceName + " service uninstalled successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 
 	}
 	
 	/**
 	 * uninstalls an application from a specific cloud and waits for the uninstallation to complete.
 	 * @param applicationName - the name of the application.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void uninstallApplicationAndWait(String applicationName) throws IOException, InterruptedException {
 		
 		String connectCommand = "connect " + service.getRestUrl() + ";";
 		String installCommand = new StringBuilder()
 			.append("uninstall-application ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 			.append(applicationName)
 			.toString();
 		String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		String excpectedResult = "Application " + applicationName + " uninstalled successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 
 		
 	}
 }
