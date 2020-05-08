 package test.cli.cloudify;
 
 import static org.testng.AssertJUnit.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
 import org.openspaces.pu.service.CustomServiceMonitors;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.usm.USMTestUtils;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 import framework.utils.SSHUtils;
 import framework.utils.ScriptUtils;
 
 
 public class InternalUSMPuServiceDownTest extends AbstractLocalCloudTest {
 	
 	ProcessingUnit tomcat;
 	Long tomcatPId;
 	Machine machineA;
 	WebClient client;
 	Machine[] machines;
 
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		super.beforeTest();	
 		client = new WebClient(BrowserVersion.getDefault());
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void tomcatServiceDownAndCorruptedTest() throws IOException, InterruptedException, PackagingException {
 		
 		String serviceDir = ScriptUtils.getBuildPath() + "/recipes/tomcat";
 		String command = "connect " + this.restUrl + ";" + "install-service " + "--verbose -timeout 10 " + serviceDir;
 		try {
 			LogUtils.log("installing tomcat service using Cli");
 			CommandTestUtils.runCommandAndWait(command);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 		LogUtils.log("Retrieving tomcat process pid from admin");
 		tomcat = admin.getProcessingUnits().waitFor(ServiceUtils.getAbsolutePUName("default", "tomcat"), 10, TimeUnit.SECONDS);
 		assertNotNull(tomcat);
 		ProcessingUnitUtils.waitForDeploymentStatus(tomcat, DeploymentStatus.INTACT);
 		assertTrue(tomcat.getStatus().equals(DeploymentStatus.INTACT));
 		
 		ProcessingUnitInstance tomcatInstance = tomcat.getInstances()[0];	
 		assertTrue("Tomcat instance is not in RUNNING State", USMTestUtils.waitForPuRunningState("default.tomcat", 60, TimeUnit.SECONDS, admin));
 		CustomServiceMonitors customServiceDetails = (CustomServiceMonitors) tomcatInstance.getStatistics().getMonitors().get("USM");
 		GridServiceContainer container = tomcatInstance.getGridServiceContainer();
 		machineA = container.getMachine();
 		tomcatPId = (Long) customServiceDetails.getMonitors().get("Actual Process ID");
 		
 		client = new WebClient(BrowserVersion.getDefault());
 		
 		final CountDownLatch removed = new CountDownLatch(1);
 		final CountDownLatch added = new CountDownLatch(2);
 		
 		LogUtils.log("adding a lifecycle listener to tomcat pu");
 		ProcessingUnitInstanceLifecycleEventListener eventListener = new ProcessingUnitInstanceLifecycleEventListener() {
 			
 			@Override
 			public void processingUnitInstanceRemoved(
 					ProcessingUnitInstance processingUnitInstance) {
 				LogUtils.log("USM processing unit instance has been removed due to tomcat failure");
 				removed.countDown();	
 			}
 			
 			@Override
 			public void processingUnitInstanceAdded(
 					ProcessingUnitInstance processingUnitInstance) {
 				LogUtils.log("USM processing unit instance has been added");
 				added.countDown();	
 			}
 		};
 		tomcat.addLifecycleListener(eventListener);
 		
 		String pathToTomcat;
 		
 		LogUtils.log("deleting catalina.sh/bat from pu folder");
		// TODO - this is hard coded to a specific tomcat version!
		String catalinaPath = "/work/processing-units/default_tomcat_1/ext/install/apache-tomcat-7.0.23/bin/catalina.";
 		String filePath = ScriptUtils.getBuildPath()+ catalinaPath;
 		if (isWindows()) {
 			pathToTomcat = filePath + "bat";
 		}
 		else {
 			pathToTomcat = filePath + "sh";
 		}
 		
 		File tomcatRun = new File(pathToTomcat);
 		
		assertTrue("failed while deleting file: " + tomcatRun, tomcatRun.delete());
 		
 		LogUtils.log("killing tomcat process");
 		if (isWindows()) {	
 			int result = ScriptUtils.killWindowsProcess(tomcatPId.intValue());
 			assertTrue(result == 0);	
 		}
 		else {
 			SSHUtils.killProcess(machineA.getHostAddress(), tomcatPId.intValue());
 		}
 		
 		LogUtils.log("waiting for tomcat pu instances to decrease");
 		assertTrue("Tomcat PU instance was not decresed", removed.await(20, TimeUnit.SECONDS));
 		assertTrue("ProcessingUnitInstanceRemoved event has not been fired", removed.getCount() == 0);
 		LogUtils.log("waiting for tomcat pu instances to increase");
 		added.await();
 		assertTrue("ProcessingUnitInstanceAdded event has not been fired", added.getCount() == 0);	
 		LogUtils.log("verifiying tomcat service in running");
 		assertTomcatPageExists(client);	
 		LogUtils.log("all's well that ends well :)");
 	}
 	
 	private boolean isWindows() {
 		return (System.getenv("windir") != null);
 	}
 	
 	private void assertTomcatPageExists(WebClient client) {
 		
         HtmlPage page = null;
         try {
             page = client.getPage("http://" + machineA.getHostAddress() + ":8080");
         } catch (IOException e) {
             fail(e.getMessage());
         }
         assertEquals("OK", page.getWebResponse().getStatusMessage());
 		
 	}
 }
