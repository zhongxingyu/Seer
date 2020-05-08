 package test.cli.cloudify.recipes;
 
 import java.io.IOException;
 
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 import framework.utils.ScriptUtils;
 
 public class ProductExamplesTest extends AbstractLocalCloudTest{
 	private String examplesDirPath = ScriptUtils.getBuildPath() + "/examples";
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installTravel() throws IOException, InterruptedException{
 		String travelDirPath = examplesDirPath + "/travel";
		String cliOutput = runCommand("connect " + this.restUrl + ";install-application --verbose " + travelDirPath);
 		assertTrue("travel app couln't be installed", cliOutput.toLowerCase().contains("application travel installed successfully"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installPetclinic() throws IOException, InterruptedException{
 		String petclinicDirPath = examplesDirPath + "/petclinic";
		String cliOutput = runCommand("connect " + this.restUrl + ";install-application --verbose " + petclinicDirPath);
 		assertTrue("petclinic app couln't be installed", cliOutput.toLowerCase().contains("application petclinic-mongo installed successfully"));
 	}
 }
