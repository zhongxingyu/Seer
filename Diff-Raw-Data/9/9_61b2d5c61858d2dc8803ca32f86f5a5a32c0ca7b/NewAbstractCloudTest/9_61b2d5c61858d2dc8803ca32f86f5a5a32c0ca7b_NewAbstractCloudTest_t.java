 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 
 import org.openspaces.admin.Admin;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 
 import test.AbstractTestSupport;
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.services.CloudService;
 import test.cli.cloudify.cloud.services.CloudServiceManager;
 import test.cli.cloudify.security.SecuredCloudService;
 
 import com.gigaspaces.internal.utils.StringUtils;
 
 import framework.utils.ApplicationInstaller;
 import framework.utils.AssertUtils;
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.ServiceInstaller;
 
 public abstract class NewAbstractCloudTest extends AbstractTestSupport {
 
 	private static final int TEN_SECONDS_IN_MILLIS = 10000;
 
 	private static final int MAX_SCAN_RETRY = 3;
 
 	protected CloudService cloudService;
 
 	protected void customizeCloud() throws Exception {};
 
 	protected void beforeBootstrap() throws Exception {}
 
 	protected void afterBootstrap() throws Exception {} 
 
 	protected void beforeTeardown() throws Exception {}
 
 	protected void afterTeardown() throws Exception {}
 
 
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
 
 	@BeforeMethod
 	public void beforeTest() {	
 		LogUtils.log("Creating test folder");
 	}
 
 	public CloudService getService() {
 		return cloudService;
 	}	
 
 	protected void bootstrap() throws Exception {
 		bootstrap(null);
 	}
 
 	protected void bootstrap(CloudService service) throws Exception {
 		if (this.isReusableCloud()) {
 			throw new UnsupportedOperationException(this.getClass().getName() + "Requires reusable clouds, which are not supported yet");
 		}
 
 		if (service == null) { // use the default cloud service if non is specified
 			this.cloudService = CloudServiceManager.getInstance().getCloudService(this.getCloudName());
 		}
 		else {
 			this.cloudService = service; // use the custom service to execute bootstrap and teardown commands
 		}
 
 		this.cloudService.init(this.getClass().getSimpleName().toLowerCase());
 
 		LogUtils.log("Customizing cloud");
 		customizeCloud();
 
 		beforeBootstrap();
 		this.cloudService.setMachinePrefix(System.getProperty("user.name") + "-" + this.getClass().getSimpleName().toLowerCase() + "-");
 		this.cloudService.bootstrapCloud();
 
 		afterBootstrap();
 	}
 
 	protected void teardown() throws Exception {
 
 		beforeTeardown();
 
 		if (this.cloudService == null) {
 			LogUtils.log("No teardown was executed as the cloud instance for this class was not created");
 		} 
 		this.cloudService.teardownCloud();
 		afterTeardown();
 	}
 	
 	protected void teardown(Admin admin) throws Exception {
 
 		beforeTeardown();
 
 		if (this.cloudService == null) {
 			LogUtils.log("No teardown was executed as the cloud instance for this class was not created");
 		} 
 		this.cloudService.teardownCloud(admin);
 		afterTeardown();
 	}
 
 
 	protected void doSanityTest(String applicationFolderName, String applicationName) throws IOException, InterruptedException {
 		LogUtils.log("installing application " + applicationName + " on " + cloudService.getCloudName());
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
 		installApplicationAndWait(applicationPath, applicationName);
 		uninstallApplicationAndWait(applicationName);
 		scanForLeakedAgentNodes();
 	}
 
 	protected void doSanityTest(String applicationFolderName, String applicationName, final int timeout) throws IOException, InterruptedException {
 		LogUtils.log("installing application " + applicationName + " on " + cloudService.getCloudName());
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/" + applicationFolderName;
 		installApplicationAndWait(applicationPath, applicationName, timeout);
 		uninstallApplicationIfFound(applicationName);
 		scanForLeakedAgentNodes();
 	}
 
 	protected void scanForLeakedAgentNodes() {
 
 		if (cloudService == null) {
 			return;
 		}
 
 		// We will give a short timeout to give the ESM 
 		// time to recognize that he needs to shutdown the machine.
 		try {
 			Thread.sleep(TEN_SECONDS_IN_MILLIS);
 		} catch (InterruptedException e) {
 		}
 
 		Throwable first = null;
 		for (int i = 0 ; i < MAX_SCAN_RETRY ; i++) {
 			try {
 				boolean leakedAgentNodesScanResult = this.cloudService.scanLeakedAgentNodes();
 				if (leakedAgentNodesScanResult == true) {
 					return;
 				} else {
 					Assert.fail("Leaked nodes were found!");
 				}
 				break;
			} catch (final Exception t) {
 				first = t;
 				LogUtils.log("Failed scaning for leaked nodes. attempt number " + (i + 1) , t);
 			}
 		}
 		if (first != null) {
 			Assert.fail("Failed scanning for leaked nodes after " + MAX_SCAN_RETRY + " attempts. First exception was --> " + first.getMessage(), first);
 		}
 	}
 	
 	protected void installApplicationAndWait(String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.install();
 	}
 
 	protected void installApplicationAndWait(String applicationPath, String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.install();
 	}
 
 	protected void uninstallApplicationAndWait(String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.uninstall();
 	}
 
 	protected void uninstallApplicationIfFound(String applicationName) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.uninstallIfFound();
 	}
 
 	protected void installApplicationAndWait(String applicationPath, String applicationName, int timeout) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.timeoutInMinutes(timeout);
 		applicationInstaller.install();
 	}
 
 	protected void installApplicationAndWait(String applicationPath, String applicationName, int timeout , boolean expectToFail) throws IOException, InterruptedException {
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.expectToFail(expectToFail);
 		applicationInstaller.timeoutInMinutes(timeout);
 		applicationInstaller.install();		
 	}
 
 
 	protected void installServiceAndWait(String servicePath, String serviceName) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.recipePath(servicePath);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.install();
 	}
 
 	protected void uninstallServiceAndWait(String serviceName) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.uninstall();
 	}
 
 	protected void uninstallServiceIfFound(String serviceName) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.uninstallIfFound();
 	}
 
 	protected void installServiceAndWait(String servicePath, String serviceName, int timeout) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.recipePath(servicePath);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.timeoutInMinutes(timeout);
 		serviceInstaller.install();
 	}
 
 	protected void installServiceAndWait(String servicePath, String serviceName, int timeout , boolean expectToFail) throws IOException, InterruptedException {
 		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
 		serviceInstaller.recipePath(servicePath);
 		serviceInstaller.waitForFinish(true);
 		serviceInstaller.expectToFail(expectToFail);
 		serviceInstaller.timeoutInMinutes(timeout);
 		serviceInstaller.install();
 	}
 
 	protected String getRestUrl() {
 
 		String finalUrl = null;
 
 		String[] restUrls = cloudService.getRestUrls();
 
 		AssertUtils.assertNotNull("No rest URL's found. there was probably a problem with bootstrap", restUrls);
 
 		if (restUrls.length == 1) {
 			finalUrl = restUrls[0];
 		} else {
 			for (String url : restUrls) {
 				String command = "connect " + url;
 				try {
 					LogUtils.log("trying to connect to rest with url " + url);
 					CommandTestUtils.runCommandAndWait(command);
 					finalUrl = url;
 					break;
 				} catch (Throwable e) {
 					LogUtils.log("caught an exception while trying to connect to rest server with url " + url, e);
 				}
 			}			
 		}
 		if (finalUrl == null) {
 			Assert.fail("Failed to find a working rest URL. tried : " + StringUtils.arrayToCommaDelimitedString(restUrls));
 		}
 		return finalUrl;
 	}
 
 	protected String getWebuiUrl() {
 		return cloudService.getWebuiUrls()[0];		
 	}
 
 	protected void dumpMachines() {
 		final String restUrl = getRestUrl();
 		String url = null;
 		try {
 			String cloudifyUser = "";
 			String cloudifyPassword = "";
 			
 			url = restUrl + "/service/dump/machines/?fileSizeLimit=50000000";
 			if (cloudService instanceof SecuredCloudService) {
 				SecuredCloudService securedService = (SecuredCloudService)cloudService;
 				cloudifyUser = securedService.getCloudifyUsername();
 				cloudifyPassword = securedService.getCloudifyPassword();
 			}
 			DumpUtils.dumpMachines(restUrl, cloudifyUser, cloudifyPassword);
 			
 		} catch (final Exception e) {
 			LogUtils.log("Failed to create dump for this url - " + url, e);
 		}
 	}	
 }
