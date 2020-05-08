 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2;
 
 import java.io.IOException;
 
 public class Ec2WinCloudService extends Ec2CloudService {
 
     private static final String DEFAULT_EU_WEST_MEDIUM_WIN_AMI = "eu-west-1/ami-843b31f0";
    private static final String DEFAULT_US_EAST_MEDIUM_WIN_AMI = "us-east-1/ami-2542c04c";
 
 	public Ec2WinCloudService() {
 		super("ec2-win");
 	}
 
 	@Override
 	public String getCloudName() {
 		return "ec2-win";
 	}
 
 	@Override
 	public void injectCloudAuthenticationDetails() throws IOException {
 		getAdditionalPropsToReplace().put("cloudifyagent", getMachinePrefix() + "cloudify-agent");
 		getAdditionalPropsToReplace().put("cloudifymanager", getMachinePrefix() + "cloudify-manager");
 		super.injectCloudAuthenticationDetails();
 		getProperties().put("hardwareId", "m1.large");
 		if (getRegion().contains("eu")) {
 			getProperties().put("imageId", DEFAULT_EU_WEST_MEDIUM_WIN_AMI);
 		} else {
			getProperties().put("locationId", "us-east-1c");
 			getProperties().put("imageId", DEFAULT_US_EAST_MEDIUM_WIN_AMI);
 		}
 	}
 }
