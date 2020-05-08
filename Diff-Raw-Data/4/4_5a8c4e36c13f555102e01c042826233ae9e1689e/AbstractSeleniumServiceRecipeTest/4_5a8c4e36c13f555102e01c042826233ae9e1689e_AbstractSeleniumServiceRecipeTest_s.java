 package test.webui.recipes.services;
 
 import java.io.IOException;
 
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.webui.recipes.AbstractSeleniumRecipeTest;
 import framework.utils.DumpUtils;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.ScriptUtils;
 
 public class AbstractSeleniumServiceRecipeTest extends AbstractSeleniumRecipeTest {
 
 	public static final String MANAGEMENT = "management";
 
 	private boolean wait = true;
 	private String pathToService;
 	private String serviceName;
 
 	public void setCurrentRecipe(String recipe) {
 		serviceName = recipe;
 		String gigaDir = ScriptUtils.getBuildPath();	
 		this.pathToService = gigaDir + "/recipes/" + recipe;
 	}
 	
 	public void setPathToServiceRelativeToSGTestRootDir(String recipe, String relativePath) {
 		this.serviceName = recipe;
 		this.pathToService = CommandTestUtils.getPath(relativePath);
 	}
 
 	public void setWait(boolean wait) {
 		this.wait = wait;
 	}
 
 
 	@BeforeMethod(alwaysRun = true)
 	public void install() throws IOException, InterruptedException {	
 		assertTrue("test failed because bootstrap was not succesfull", isBootstraped());
 		AdminFactory factory = new AdminFactory();
 		LogUtils.log("Adding locators to new admin factory");
 		factory.addLocator("127.0.0.1:4168");
 		LogUtils.log("creating new admin from factory");
 		admin = factory.createAdmin();
 		LogUtils.log("Installing service " + pathToService);
 		assertTrue("Failed to install service " + pathToService, installService(pathToService, wait)); 
 		LogUtils.log("retrieving webui url");
 		ProcessingUnit webui = admin.getProcessingUnits().waitFor("webui");
 		ProcessingUnitUtils.waitForDeploymentStatus(webui, DeploymentStatus.INTACT);
 		assertTrue(webui != null);
 		assertTrue(webui.getInstances().length != 0);	
 		String url = ProcessingUnitUtils.getWebProcessingUnitURL(webui).toString();	
 		startWebBrowser(url); 
 	}
 
 	@AfterMethod(alwaysRun = true)
 	public void uninstall() throws IOException, InterruptedException {
		uninstallService(serviceName, wait);
 		stopWebBrowser();
 		if (admin != null) {
 			if (!isDevMode()) {
 				DumpUtils.dumpLogs(admin);
 			}
 			undeployNonManagementServices();
 			admin.close();
 			admin = null;
 		}
 	}
 
 	public static boolean installService(String pathToService, boolean wait) throws IOException, InterruptedException {
 		String command = "connect localhost:8100;install-service --verbose -timeout 25 " + pathToService;
 		if (wait) {
 			LogUtils.log("Waiting for install-service to finish...");
 			String output = CommandTestUtils.runCommandAndWait(command);
 			boolean success = output.contains("successfully installed");
 			if (success) {
 				LogUtils.log("Cli returned that service was installed succesfully");
 				return true;
 			}
 			else {
 				LogUtils.log("Cli returned that service was not installed succesfully");
 				return false;
 			}
 		}
 		else {
 			LogUtils.log("Not waiting for service to finish, assuming it will succeed");
 			CommandTestUtils.runCommand(command);
 			return true;
 		}
 	}
 
 	public boolean uninstallService(String serviceName, boolean wait) throws IOException, InterruptedException {
 		String command = "connect localhost:8100;uninstall-service --verbose -timeout 25 " + serviceName;
 		if (wait) {
 			LogUtils.log("Waiting for uninstall-service to finish...");
 			String output = CommandTestUtils.runCommandAndWait(command);
 			boolean success = output.contains("Successfully undeployed");
 			if (success) {
 				LogUtils.log("Cli returned that service was un-installed succesfully");
 				return true;
 			}
 			else {
 				LogUtils.log("Cli returned that service was not un-installed succesfully");
 				return false;
 			}
 		}
 		else {
 			CommandTestUtils.runCommand(command);
 			return true;
 		}
 	}
 }
