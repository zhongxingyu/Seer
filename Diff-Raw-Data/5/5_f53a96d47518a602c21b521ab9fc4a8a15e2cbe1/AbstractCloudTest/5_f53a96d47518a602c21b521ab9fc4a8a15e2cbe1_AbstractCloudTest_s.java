 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.DataProvider;
 
 import com.j_spaces.kernel.JSpaceUtilities;
 
 import test.AbstractTest;
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.ec2.Ec2CloudService;
 import test.cli.cloudify.cloud.hp.HpCloudService;
 import test.cli.cloudify.cloud.rackspace.RackspaceCloudService;
 import test.cli.cloudify.cloud.terremark.TerremarkCloudService;
 import test.cli.cloudify.cloud.byon.ByonCloudService;
 import framework.report.MailReporterProperties;
 import framework.tools.SimpleMail;
 import framework.utils.LogUtils;
 
 public class AbstractCloudTest extends AbstractTest {
 	
 	public static final String RACKSPACE = "rackspace";
 	public static final String BYON = "byon";
 	public static final String TERREMARK = "terremark";
 	public static final String OPENSTACK = "openstack";
 	public static final String EC2 = "ec2";
 
	private static final String[][] SUPPORTED_CLOUDS = {{EC2}};
 	
 	private CloudService service;
 	
 	public void setService(CloudService service) {
 		this.service = service;
 	}
 	
 	/**
 	 * set the service CloudService instance to a specific cloud provider.
 	 * all install/uninstall commands will be executed on the specified cloud.
 	 * @param cloudName
 	 */
 	public void setCloudToUse(String cloudName) {
 		
 		if (cloudName.equals(EC2)) {
 			service = Ec2CloudService.getService();
 		}
 		if (cloudName.equals(OPENSTACK)) {
 			service = HpCloudService.getService();
 		}
 		if (cloudName.equals(TERREMARK)) {
 			service = TerremarkCloudService.getService();
 		}
 		if (cloudName.equals(BYON)) {
 			service = ByonCloudService.getService();
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
         	success = bootstrapClouds();
         	if (success) {
         		LogUtils.log("Bootstrapping to clouds finished");
         	}
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
 		
 		LogUtils.log("finished tearing down clouds : " + clouds);
 	}
 	
 	private boolean bootstrapClouds() {
 		
 		int numberOfSuccesfullyBootstrappedClouds = 0;
 		
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++) {
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			if (supportedCloud.equals(EC2)) {
 				try {
 					Ec2CloudService.getService().bootstrapCloud();
 					numberOfSuccesfullyBootstrappedClouds++;
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while bootstrapping ec2", e);
 				}
 				
 			}
 			if (supportedCloud.equals(OPENSTACK)) {
 				try {
 					HpCloudService.getService().bootstrapCloud();
 					numberOfSuccesfullyBootstrappedClouds++;
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while bootstrapping openstack", e);
 				}
 			}
 			if (supportedCloud.equals(TERREMARK)) {
 				try {
 					TerremarkCloudService.getService().bootstrapCloud();
 					numberOfSuccesfullyBootstrappedClouds++;
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while bootstrapping terremark", e);
 				}
 			}
 			if (supportedCloud.equals(RACKSPACE)) {
 				try {
 					RackspaceCloudService.getService().bootstrapCloud();
 					numberOfSuccesfullyBootstrappedClouds++;
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while bootstrapping rackspace", e);
 				}
 			}
 			if (supportedCloud.equals(BYON)) {
 				try {
 					ByonCloudService.getService().bootstrapCloud();
 					numberOfSuccesfullyBootstrappedClouds++;
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while bootstrapping byon", e);
 				}				
 			}
 		}
 		
 		if (numberOfSuccesfullyBootstrappedClouds > 0) return true;
 		return false;	
 	}
 	
 	@Override
 	@AfterMethod
 	public void afterTest() {
 		
 	}
 
 	
 	private void teardownClouds() {
 		
 		for (int j = 0 ; j < SUPPORTED_CLOUDS.length ; j++) {
 			String supportedCloud = SUPPORTED_CLOUDS[j][0];
 			if (supportedCloud.equals(EC2)) {
 				try {
 					Ec2CloudService.getService().teardownCloud();
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while tearing down ec2", e);
 					sendTeardownCloudFailedMail(EC2, e);
 				}
 			}
 			if (supportedCloud.equals(OPENSTACK)) {
 				try {
 					HpCloudService.getService().teardownCloud();
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while tearing down openstack", e);
 					sendTeardownCloudFailedMail(OPENSTACK, e);
 				}
 			}
 			if (supportedCloud.equals(TERREMARK)) {
 				try {
 					TerremarkCloudService.getService().teardownCloud();
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while tearing down terremark", e);
 					sendTeardownCloudFailedMail(TERREMARK, e);
 				}
 			}
 			if (supportedCloud.equals(BYON)) {
 				try {
 					ByonCloudService.getService().teardownCloud();
 				}
 				catch (Throwable e) {
 					LogUtils.log("caught an exception while tearing down byon", e);
 					sendTeardownCloudFailedMail(BYON, e);
 				}
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
 		
 		if (service.getRestUrl() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		
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
 		installApplication(applicationPath , applicationName, 0 , true , false);
 	}
 	
 	/**
 	 * installs an application on a specific cloud and waits for the installation to complete.
 	 * @param applicationPath - full path to the -application.groovy file on the local file system.
 	 * @param applicationName - the name of the service.
 	 * @param wait - used for determining if to wait for command 
 	 * @param failCommand  - used for determining if the command is expected to fail 
 	 * @param timeout - time in minutes to wait for command to finish. Give non-negative value to override 30 minute default.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installApplication(String applicationPath, String applicationName,int timeout ,boolean wait ,boolean failCommand) throws IOException, InterruptedException {
 		
 		if (service.getRestUrl() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		long timeoutToUse;
		if(timeout >= 0)
 			timeoutToUse = timeout;
 		else
 			timeoutToUse = TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2);
 		
 		String connectCommand = "connect " + service.getRestUrl() + ";";
 		String installCommand = new StringBuilder()
 			.append("install-application ")
 			.append("--verbose ")
 			.append("-timeout ")
 			.append(timeoutToUse).append(" ")
 			.append((applicationPath.toString()).replace('\\', '/'))
 			.toString();
 		String output = CommandTestUtils.runCommand(connectCommand + installCommand, wait, failCommand);
 		String excpectedResult = "Application " + applicationName + " installed successfully";
 		if(!failCommand)
 			assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 		else
 			assertTrue(output.toLowerCase().contains("operation failed"));
 
 	}
 	
 	/**
 	 * uninstalls a service from a specific cloud and waits for the uninstallation to complete.
 	 * @param serviceName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void uninstallServiceAndWait(String serviceName) throws IOException, InterruptedException {
 		
 		if (service.getRestUrl() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		
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
 		
 		if (service.getRestUrl() == null) {
 			Assert.fail("Test failed becuase the cloud was not bootstrapped properly");
 		}
 		
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
 	
 	protected void sendTeardownCloudFailedMail(String cloudName, Throwable error) {
 		
 		if (!isDevMode()) {
 			String url = null;
 			if (cloudName.equals(EC2)) {
 				url = CloudTestUtils.EC2_MANAGEMENT_CONSOLE_URL;
 			}
 			if (cloudName.equals(OPENSTACK)) {
 				url = CloudTestUtils.HPCLOUD_MANAGEMENT_CONSOLE_URL;
 			}
 			if (cloudName.equals(BYON)) {
 				url = "";
 			}
 			
 			Properties props = new Properties();
 			InputStream in = this.getClass().getResourceAsStream("mailreporter.properties");
 			try {
 				props.load(in);
 				in.close();
 				System.out.println("mailreporter.properties: " + props);
 			} catch (IOException e) {
 				throw new RuntimeException("failed to read mailreporter.properties file - " + e, e);
 			}
 			
 			String title = "teardown-cloud " + cloudName + " failure";
 			
 	        StringBuilder sb = new StringBuilder();
 	        sb.append("<html>").append("\n");
 	        sb.append("<h2>A failure occurerd while trying to teardown " + cloudName + " cloud.</h2><br>").append("\n");
 	        sb.append("<h4>This may have been caused because bootstrapping to this cloud was unsuccessul, or because of a different exception.<h4><br>").append("\n");
 	        sb.append("<p>here is the exception : <p><br>").append("\n");
 	        sb.append(JSpaceUtilities.getStackTrace(error)).append("\n");
 	        sb.append("<h4>in any case, please make sure the machines are terminated<h4><br>");
 	        sb.append(url).append("\n");
 	        sb.append("</html>");
 	        
 			MailReporterProperties mailProperties = new MailReporterProperties(props);
 	        try {
 				SimpleMail.send(mailProperties.getMailHost(), mailProperties.getUsername(), 
 						mailProperties.getPassword(),
 						title, sb.toString(), 
 						mailProperties.getCloudifyRecipients());
 			} catch (Exception e) {
 				LogUtils.log("Failed sending mail to recipents : " + mailProperties.getCloudifyRecipients());
 			}	
 		}		
 	}
 	
 	private boolean isDevMode() {
 		
 		String user = System.getenv("USER");
 		return ((user == null) || !(user.equals("tgrid")));
 	}
 }
