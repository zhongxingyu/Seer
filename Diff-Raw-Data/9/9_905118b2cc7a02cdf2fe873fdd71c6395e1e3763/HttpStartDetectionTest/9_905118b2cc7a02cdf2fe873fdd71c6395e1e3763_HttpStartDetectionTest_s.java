 package test.cli.cloudify;
 
 import java.io.IOException;
 
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.testng.annotations.Test;
 
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 
 public class HttpStartDetectionTest extends AbstractCommandTest {
 	
 	private Machine machineA;
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1")
 	public void httpStartDetectionTest() throws IOException, InterruptedException {
 		String serviceDir = SGTestHelper.getSGTestRootDir().replace("\\", "/") + "/apps/USM/usm/tomcatHttpStartDetection";
 		doTest(serviceDir);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1")
 	public void urlLivenessDetectorTest() throws IOException, InterruptedException {
 		String serviceDir = SGTestHelper.getSGTestRootDir().replace("\\", "/") + "/apps/USM/usm/tomcatHttpLivenessDetectorPlugin";
 		doTest(serviceDir);
 	}
 	
 	private void doTest(String serviceDir) {
 		machineA = admin.getProcessingUnits().getProcessingUnit("rest").getInstances()[0].getMachine();
 		String command = "connect " + restUrl + ";install-service --verbose " + serviceDir + ";exit";
 		try {
 			
 			LogUtils.log("Installing tomcat on port 8081");
 			runCommand(command);
			assertTrue("tomcat service should not be installed", admin.getProcessingUnits().getProcessingUnit("tomcat").getStatus().equals(DeploymentStatus.BROKEN));
 		
 		} catch (IOException e) {
 			e.printStackTrace();
 			super.afterTest();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			super.afterTest();
 		}	
 	}
 }
