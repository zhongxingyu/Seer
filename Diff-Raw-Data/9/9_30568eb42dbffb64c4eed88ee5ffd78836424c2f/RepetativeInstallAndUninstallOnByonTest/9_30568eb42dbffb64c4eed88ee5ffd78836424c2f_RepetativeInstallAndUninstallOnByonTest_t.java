 package test.cli.cloudify.cloud.byon;
 
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 /**
  * <p>
  * 1. bootstrap byon.
  * <p>
  * 2. repeat steps 3-4 three times.
  * <p>
  * 3. install petclinic-simple and assert installation.
  * <p>
  * 4. uninstall petclinic-simple and assert successful uninstall.
  * 
  */
 public class RepetativeInstallAndUninstallOnByonTest extends AbstractByonCloudTest {
 
 	private final static int REPETITIONS = 3;
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testPetclinicSimple() throws Exception {
 
 		for (int i = 0; i < REPETITIONS; i++) {
 			LogUtils.log("petclinic install number " + (i + 1));
 			installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic-simple", "petclinic");
 
			ProcessingUnit mongod = admin.getProcessingUnits().waitFor("petclinic.mongod", 1, TimeUnit.MINUTES);
			ProcessingUnit tomcat = admin.getProcessingUnits().waitFor("petclinic.tomcat", 1, TimeUnit.MINUTES);
 			
 			AssertUtils.assertNotNull("Failed to discover processing unit petclinic.mongod even though it was installed succesfully ", mongod);
 			AssertUtils.assertNotNull("Failed to discover processing unit petclinic.tomcat even though it was installed succesfully ", tomcat);
 			
 			LogUtils.log("petclinic uninstall number " + (i + 1));
 			uninstallApplicationAndWait("petclinic");
 			LogUtils.log("Application petclinic uninstalled succesfully");
 
			mongod = admin.getProcessingUnits().waitFor("petclinic.mongod", 1, TimeUnit.MINUTES);
			tomcat = admin.getProcessingUnits().waitFor("petclinic.tomcat", 1, TimeUnit.MINUTES);
 			
 			AssertUtils.assertNull("Processing unit petclinic.mongod is still discovered even though it was uninstalled succesfully ", mongod);
 			AssertUtils.assertNull("Processing unit petclinic.tomcat is still discovered even though it was uninstalled succesfully ", tomcat);
 			
 		}
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 
 }
