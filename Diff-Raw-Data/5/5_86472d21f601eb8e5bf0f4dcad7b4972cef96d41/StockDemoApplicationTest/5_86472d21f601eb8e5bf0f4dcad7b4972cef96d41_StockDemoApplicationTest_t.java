 package test.cli.cloudify.cloud.ec2;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import framework.tools.SGTestHelper;
 
 public class StockDemoApplicationTest extends NewAbstractCloudTest {
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 	
 	@BeforeMethod
	public void prepareApplication() throws IOException {
 		File stockDemoAppSG = new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/recipes/stockdemo");
		File appsFolder = new File(SGTestHelper.getBuildDir() + "/recipes/apps/stockdemo");
 		FileUtils.copyDirectory(stockDemoAppSG, appsFolder);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, groups = "1", enabled = true)
 	public void testStockDemo() throws IOException, InterruptedException {
 		doSanityTest("stockdemo", "stockdemo");
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 	
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		
 		/* copy premium license to cloudify-overrides in order to run xap pu's */
 		String overridesFolder = getService().getPathToCloudFolder() + "/upload/cloudify-overrides";
 		File cloudifyPremiumLicenseFileSGPath = new File(SGTestHelper.getSGTestRootDir() + "/bin/gslicense.xml");
 		File cloudifyPremiumLicenseFileOverridesPath = new File(overridesFolder + "/gslicense.xml");
 		Map<File,File> filesToReplace = new HashMap<File,File>();
 		filesToReplace.put(cloudifyPremiumLicenseFileOverridesPath,cloudifyPremiumLicenseFileSGPath);
 		getService().addFilesToReplace(filesToReplace);
 		
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 }
