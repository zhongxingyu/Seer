 package test.cli.cloudify.cloud.ec2;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 
 public class EuWestRegionEc2Test extends NewAbstractCloudTest {
 
 	private Ec2CloudService service;
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
	public void testEuWestRegion() throws IOException, InterruptedException {
 		doSanityTest("travel", "travel");
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 
 	@Override
 	protected void customizeCloud() {
 		service = (Ec2CloudService) getService();
 		service.setPemFileName("sgtest-eu");
 		service.setAdditionalPropsToReplace(new HashMap<String, String>());
		service.getAdditionalPropsToReplace().put("imageId \"us-east-1/ami-76f0061f\"", "imageId \"eu-west-1/ami-24506250\"");
 		service.getAdditionalPropsToReplace().put("locationId \"us-east-1\"", "locationId \"eu-west-1\"");
 		service.setMachinePrefix(this.getClass().getName() + CloudTestUtils.SGTEST_MACHINE_PREFIX);
 	}
 
 }
