 package test.cli.cloudify.cloud.azure;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import framework.tools.SGTestHelper;
 import framework.utils.ApplicationInstaller;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class AzureExamplesTest extends NewAbstractCloudTest {
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@BeforeMethod
 	public void copyAzureApplicationsToBuildDir() throws IOException {
 		copyApplicationToBuildDir("travel-azure");
 		copyApplicationToBuildDir("petclinic-simple-azure");
 		copyApplicationToBuildDir("helloworld-azure");
 		
 	}
 
 	private void copyApplicationToBuildDir(final String applicationName) throws IOException {
 		File appsDir = new File(SGTestHelper.getBuildDir() + "/recipes/apps");
 		File originalAzureApplication = new File(SGTestHelper.getBuildDir() + "/recipes/apps/" + applicationName);
 		FileUtils.deleteDirectory(originalAzureApplication);
 		File newAzureApplication = new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/cloudify/recipes/" + applicationName);
 		FileUtils.copyDirectoryToDirectory(newAzureApplication, appsDir);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testTravel() throws IOException, InterruptedException {
 		LogUtils.log("installing application travel on azure");
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/travel-azure";
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), "travel");
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.timeoutInMinutes(45);
 		applicationInstaller.install();		
 		applicationInstaller.uninstall();
 		super.scanForLeakedAgentNodes();		
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testPetclinicSimple() throws IOException, InterruptedException {
 		LogUtils.log("installing application petclinic-simple on azure");
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/petclinic-simple-azure";
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), "petclinic");
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.timeoutInMinutes(45);
 		applicationInstaller.install();		
 		applicationInstaller.uninstall();
 		super.scanForLeakedAgentNodes();		
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 5, enabled = true)
 	public void testHelloWorld() throws IOException, InterruptedException {
 		LogUtils.log("installing application helloworld on azure");
 		String applicationPath = ScriptUtils.getBuildPath() + "/recipes/apps/helloworld-azure";
 		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), "helloworld");
 		applicationInstaller.recipePath(applicationPath);
 		applicationInstaller.waitForFinish(true);
 		applicationInstaller.timeoutInMinutes(45);
 		applicationInstaller.install();		
 		applicationInstaller.uninstall();
 		super.scanForLeakedAgentNodes();		
 	}
 
 	
 	@AfterMethod(alwaysRun = true)
 	public void cleanUp() throws IOException, InterruptedException {
		super.uninstallApplicationIfFound("travel");
		super.uninstallApplicationIfFound("petclinic");
		super.uninstallApplicationIfFound("helloworld");
 		super.scanForLeakedAgentNodes();
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
