 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 
 import org.testng.Assert;
 import org.testng.ITestContext;
 import org.testng.ITestListener;
 import org.testng.ITestResult;
 import org.testng.TestRunner;
 
 import test.AbstractTestSupport;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.services.CloudService;
 import test.cli.cloudify.cloud.services.CloudServiceManager;
 
 import com.j_spaces.kernel.JSpaceUtilities;
 
 import framework.report.MailReporterProperties;
 import framework.tools.SGTestHelper;
 import framework.tools.SimpleMail;
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public abstract class NewAbstractCloudTest extends AbstractTestSupport {
 
 	protected CloudService cloud;
 
 	// initialized in bootstrap
 	private String cloudName;
 	private String uniqueName;
 
 	private String lastTestName;
 	private int lastTestResult;// see ITestResult
 
 	protected abstract void customizeCloud() throws Exception;
 	
 	protected void afterBootstrap() throws Exception {} 
 	
 	protected void beforeTeardown() throws Exception {}
 	
 	protected void afterTeardown() throws Exception {}
 	
 	
 	protected void bootstrap(final ITestContext testContext) {
 		bootstrap(testContext, null);
 	}
 	
 	protected void bootstrap(final ITestContext iTestContext, CloudService service) {
 		final TestRunner runner = (TestRunner) iTestContext;
 		runner.addTestListener(new TestNameListener());
 		cloudName = this.getCloudName();
 		if (this.isReusableCloud()) {
 			throw new UnsupportedOperationException(this.getClass().getName() + "Requires reusable clouds, which are not supported yet");
 		}
 
 		uniqueName = this.getClass().getSimpleName();
 
 		if (service == null) { // use the default cloud service if non is specified
 			this.cloud = CloudServiceManager.getInstance().getCloudService(cloudName, uniqueName);
 		}
 		else {
 			this.cloud = service; // use the custom service to execute bootstrap and teardown commands
 		}
 
 		try {
 			customizeCloud(); // customize cloud settings before bootstrap
 		} 
 		catch (Exception e) {
 			AssertFail("Customizing of cloud (" + cloudName + ", " + uniqueName + ") failed with the following error: " + e.getMessage(), e);
 		}
 
 		try {
 			this.cloud.bootstrapCloud(); // bootstrap the cloud
 		} 
 		catch (final Exception e) {
 			AssertFail("Bootstrapping of cloud (" + cloudName + ", " + uniqueName + ") failed with the following error: " + e.getMessage(), e);
 		}
 		
 		try {
 			afterBootstrap(); // run optional post-bootstrap steps (e.g. create admin)
 
 		} catch (final Exception e) {
 			AssertFail("AfterBootstrap method of cloud (" + cloudName + ", " + uniqueName + ") failed with the following error: " + e.getMessage(), e);
 		}
 	}
 
 	protected void teardown() {
 				
 		final String cloudName = this.getCloudName();
 
 		// run optional pre-teardown steps (e.g. clean objects)
 		try {
 			beforeTeardown();
 		} 
 		catch (final Exception e) {
 			AssertFail("BeforeTeardown method of cloud (" + cloudName + ", " + uniqueName + ") failed with the following error: " + e.getMessage(), e);
 		}
 		
 		// perform teardown
 		if (this.cloud == null) {
 			LogUtils.log("No teardown was executed as the cloud instance for this class was not created");
 		} 
 		else {
 			try {
 				if (this.cloud.isBootstrapped()) {
 					this.cloud.teardownCloud();
 				} 
 				else {
 					LogUtils.log("The cloud was not bootstrapped, so no teardown required.");
 					this.cloud.afterTeardown();
 				}
 			} 
 			catch (final Exception e) {
 				LogUtils.log("Tear-down of cloud (" + cloudName + ", " + uniqueName + ") failed with the following error: " + e.getMessage(), e);
 				sendTeardownCloudFailedMail(cloudName, e);
 			}
 		}
 	}
 	
 	/******
 	 * Returns the name of the cloud, as used in the bootstrap-cloud command.
 	 * 
 	 * @return
 	 */
 	protected abstract String getCloudName();
 
 	/********
 	 * Indicates if the cloud used in this test is reusable - which means it may have already been bootstrapped and can
 	 * be reused. Non reusable clouds are bootstrapped at the beginning of the class, and torn down at its end. Reusable
 	 * clouds are torn down when the suite ends.
 	 * 
 	 * @return
 	 */
 	protected abstract boolean isReusableCloud();
 
 	
 	/**
 	 * This method is meant for the simple tests. all it does is install the application, and the immediately uninstalls
 	 * it.
 	 * 
 	 * @param cloudName - the cloud on which to install
 	 * @param applicationFolderName - the folder in which the application resides
 	 * @param applicationName - the name of the application as defined in (*-application.groovy)
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void doSanityTest(String applicationFolderName, String applicationName) throws IOException, InterruptedException {
 		LogUtils.log("installing application " + applicationName + " on " + cloudName);
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
 		try {
 			installApplicationAndWait(applicationPath, applicationName);
 		} 
 		finally {
 			if ((getService() != null) && (getService().getRestUrls() != null)) {
 				String command = "connect " + getRestUrl() + ";list-applications";
 				String output = CommandTestUtils.runCommandAndWait(command);
 				if (output.contains(applicationName)) {
 					uninstallApplicationAndWait(applicationName);
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * installs a service on a specific cloud and waits for the installation to complete.
 	 * 
 	 * @param servicePath - full path to the -service.groovy file on the local file system.
 	 * @param serviceName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installServiceAndWait(final String servicePath, final String serviceName)
 			throws IOException, InterruptedException {
 
 		final String restUrl = getRestUrl();
 
 		final String connectCommand = "connect " + restUrl + ";";
 		final String installCommand = new StringBuilder()
 				.append("install-service ")
 				.append("--verbose ")
 				.append("-timeout ")
 				.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 				.append(servicePath.toString().replace('\\', '/'))
 				.toString();
 		final String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
		final String excpectedResult = "Service \"" + serviceName + "\" successfully installed";
		assertTrue("output " + output + " Does not contain " + excpectedResult,
				output.toLowerCase().contains(excpectedResult.toLowerCase()));
 	}
 
 	/**
 	 * installs an application on a specific cloud and waits for the installation to complete.
 	 * 
 	 * @param applicationPath - full path to the -application.groovy file on the local file system.
 	 * @param applicationName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installApplicationAndWait(final String applicationPath, final String applicationName)
 			throws IOException, InterruptedException {
 		installApplication(applicationPath, applicationName, 0, true, false);
 	}
 
 	/**
 	 * installs an application on a specific cloud and waits for the installation to complete.
 	 * 
 	 * @param applicationPath - full path to the -application.groovy file on the local file system.
 	 * @param applicationName - the name of the service.
 	 * @param wait - used for determining if to wait for command
 	 * @param failCommand - used for determining if the command is expected to fail
 	 * @param timeout - time in minutes to wait for command to finish. Give non-negative value to override 30 minute
 	 *        default.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void installApplication(final String applicationPath, final String applicationName, final int timeout,
 			final boolean wait,
 			final boolean failCommand)
 			throws IOException, InterruptedException {
 
 		final String restUrl = getRestUrl();
 
 		long timeoutToUse;
 		if (timeout > 0) {
 			timeoutToUse = timeout;
 		} else {
 			timeoutToUse = TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2);
 		}
 
 		final String connectCommand = "connect " + restUrl + ";";
 		final String installCommand = new StringBuilder()
 				.append("install-application ")
 				.append("--verbose ")
 				.append("-timeout ")
 				.append(timeoutToUse).append(" ")
 				.append(applicationPath.toString().replace('\\', '/'))
 				.toString();
 		final String output = CommandTestUtils.runCommand(connectCommand + installCommand, wait, failCommand);
 		final String excpectedResult = "Application " + applicationName + " installed successfully";
 		if (!failCommand) {
 			System.out.println("output:" + output);
 			assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 		} else {
 			assertTrue(output.toLowerCase().contains("operation failed"));
 		}
 
 	}
 	
 	/**
 	 * uninstalls a service from a specific cloud and waits for the uninstallation to complete.
 	 * 
 	 * @param serviceName - the name of the service.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void uninstallServiceAndWait(final String serviceName)
 			throws IOException, InterruptedException {
 
 		final String restUrl = getRestUrl();
 		String url = null;
 		try {
 			url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
 			DumpUtils.dumpMachines(restUrl);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to create dump for this url - " + url, e);
 		}
 
 		final String connectCommand = "connect " + restUrl + ";";
 		final String installCommand = new StringBuilder()
 				.append("uninstall-service ")
 				.append("--verbose ")
 				.append("-timeout ")
 				.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 				.append(serviceName)
 				.toString();
 		final String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		final String excpectedResult = serviceName + " service uninstalled successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 
 	}
 
 
 	public void scanNodesLeak() {
 		final boolean leakedAgentScanResult = this.cloud.afterTest();
 
 		if (this.lastTestResult == ITestResult.SUCCESS) {
 			// test passed - check for leaked VMs
 			if (!leakedAgentScanResult) {
 				// The test passed, but machines leaked, so the configuration should fail.
 				AssertFail("Test: " + lastTestName + " ended successfully, but leaked nodes were found!");
 			}
 		} 
 		else {
 			LogUtils.log("Test: " + lastTestName + " failed, and some leaked nodes were found too");
 		}
 	}
 
 	protected String getRestUrl() {
 		if (cloud.getRestUrls() == null) {
 			Assert.fail("Test requested the REST URLs for the cloud, but they were not set. This may indeicate that the cloud was not bootstrapped properly");
 		}
 
 		final String restUrl = cloud.getRestUrls()[0];
 		return restUrl;
 
 	}
 
 	/**
 	 * uninstalls an application from a specific cloud and waits for the uninstallation to complete.
 	 * 
 	 * @param applicationName - the name of the application.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void uninstallApplicationAndWait(final String applicationName)
 			throws IOException, InterruptedException {
 
 		final String restUrl = getRestUrl();
 		String url = null;
 		try {
 			url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
 			DumpUtils.dumpMachines(restUrl);
 		} catch (final Exception e) {
 			LogUtils.log("Failed to create dump for this url - " + url, e);
 		}
 		final String connectCommand = "connect " + restUrl + ";";
 		final String installCommand = new StringBuilder()
 				.append("uninstall-application ")
 				.append("--verbose ")
 				.append("-timeout ")
 				.append(TimeUnit.MILLISECONDS.toMinutes(DEFAULT_TEST_TIMEOUT * 2)).append(" ")
 				.append(applicationName)
 				.toString();
 		final String output = CommandTestUtils.runCommandAndWait(connectCommand + installCommand);
 		final String excpectedResult = "Application " + applicationName + " uninstalled successfully";
 		assertTrue(output.toLowerCase().contains(excpectedResult.toLowerCase()));
 
 	}
 
 	protected void sendTeardownCloudFailedMail(final String cloudName, final Throwable error) {
 
 		if (!isDevMode()) {
 			final String url = "";
 			final Properties props = new Properties();
 			final InputStream in = this.getClass().getResourceAsStream("mailreporter.properties");
 			try {
 				props.load(in);
 				in.close();
 				System.out.println("mailreporter.properties: " + props);
 			} catch (final IOException e) {
 				throw new RuntimeException("failed to read mailreporter.properties file - " + e, e);
 			}
 
 			final String title = "teardown-cloud " + cloudName + " failure";
 
 			final StringBuilder sb = new StringBuilder();
 			sb.append("<html>").append("\n");
 			sb.append("<h2>A failure occurerd while trying to teardown " + cloudName + " cloud.</h2><br>").append("\n");
 			sb.append(
 					"<h4>This may have been caused because bootstrapping to this cloud was unsuccessul, or because of a different exception.<h4><br>")
 					.append("\n");
 			sb.append("<p>here is the exception : <p><br>").append("\n");
 			sb.append(JSpaceUtilities.getStackTrace(error)).append("\n");
 			sb.append("<h4>in any case, please make sure the machines are terminated<h4><br>");
 			sb.append(url).append("\n");
 			sb.append("</html>");
 
 			final MailReporterProperties mailProperties = new MailReporterProperties(props);
 			try {
 				SimpleMail.send(mailProperties.getMailHost(), mailProperties.getUsername(),
 						mailProperties.getPassword(),
 						title, sb.toString(),
 						mailProperties.getCloudifyRecipients());
 			} catch (final Exception e) {
 				LogUtils.log("Failed sending mail to recipents : " + mailProperties.getCloudifyRecipients());
 			}
 		}
 	}
 
 	private boolean isDevMode() {
 		return SGTestHelper.isDevMode();
 	}
 
 	public CloudService getService() {
 		return cloud;
 	}
 
 
 	
 	private class TestNameListener implements ITestListener {
 
 		@Override
 		public void onFinish(final ITestContext arg0) {
 
 		}
 
 		@Override
 		public void onStart(final ITestContext arg0) {
 
 		}
 
 		@Override
 		public void onTestFailedButWithinSuccessPercentage(final ITestResult arg0) {
 
 		}
 
 		@Override
 		public void onTestFailure(final ITestResult arg0) {
 			lastTestName = arg0.getName();
 			lastTestResult = ITestResult.FAILURE;
 		}
 
 		@Override
 		public void onTestSkipped(final ITestResult arg0) {
 			lastTestName = arg0.getName();
 			lastTestResult = ITestResult.SKIP;
 
 		}
 
 		@Override
 		public void onTestStart(final ITestResult arg0) {
 		}
 
 		@Override
 		public void onTestSuccess(final ITestResult arg0) {
 			lastTestName = arg0.getName();
 			lastTestResult = ITestResult.SUCCESS;
 		}
 
 	}
 }
