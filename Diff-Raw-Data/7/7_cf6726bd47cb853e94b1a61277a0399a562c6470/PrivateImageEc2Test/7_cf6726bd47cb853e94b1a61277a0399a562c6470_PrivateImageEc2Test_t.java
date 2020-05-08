 package test.cli.cloudify.cloud.ec2;
 
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 
 public class PrivateImageEc2Test extends NewAbstractCloudTest {	
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installTest()
 			throws Exception {
 		doSanityTest("petclinic", "petclinic");
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 
 	@Override
 	protected void customizeCloud() {
 		final Ec2CloudService ec2Service = (Ec2CloudService) cloud;
 		ec2Service.getAdditionalPropsToReplace().put("imageId \"us-east-1/ami-76f0061f\"",
 				"imageId \"us-east-1/ami-93b068fa\"");
 	};
 
 }
