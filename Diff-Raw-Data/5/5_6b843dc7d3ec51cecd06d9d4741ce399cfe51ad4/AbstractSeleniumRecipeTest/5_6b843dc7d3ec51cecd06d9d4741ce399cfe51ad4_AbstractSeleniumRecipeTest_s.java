 package test.webui.recipes;
 
 import java.io.IOException;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 import framework.utils.ScriptUtils;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.webui.AbstractSeleniumTest;
 
 public class AbstractSeleniumRecipeTest extends AbstractSeleniumTest {
 	
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 	
 	}
 	
 	@Override
 	@AfterMethod
 	public void afterTest() {
 	
 	}
 	
 	public boolean bootstrapLocalCloud() throws IOException, InterruptedException {
 		String command = "bootstrap-localcloud --verbose";
 		String output = CommandTestUtils.runCommandAndWait(command);
 		return output.contains("Local-cloud started succesfully");
 	}
 	
 	public boolean tearDownLocalCloud() throws IOException, InterruptedException {
 		String command = "teardown-localcloud --verbose";
 		String output = CommandTestUtils.runCommandAndWait(command);
 		return output.contains("Completed local-cloud teardown");
 	}
 	
 	public boolean installApplication(String applicationName) throws IOException, InterruptedException {
 		String gigaDir = ScriptUtils.getBuildPath();	
 		String pathToApplication = gigaDir + "/recipes/" + applicationName;	
		String command = "install-application --verbose -timeout 25 " + pathToApplication;
 		String output = CommandTestUtils.runCommandAndWait(command);
 		return output.contains("installed successfully");
 			}
 	
 	public boolean installService(String serviceName) throws IOException, InterruptedException {
 		String gigaDir = ScriptUtils.getBuildPath();	
 		String pathToService = gigaDir + "/recipes/" + serviceName;	
		String command = "install-service --verbose -timeout 25 " + pathToService;
 		String output = CommandTestUtils.runCommandAndWait(command);
 		return output.contains("successfully installed");
 	}
 }
