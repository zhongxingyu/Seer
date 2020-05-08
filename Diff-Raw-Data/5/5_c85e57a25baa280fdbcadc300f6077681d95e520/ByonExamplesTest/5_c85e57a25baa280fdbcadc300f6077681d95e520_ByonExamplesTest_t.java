 package test.cli.cloudify.cloud.byon;
 
 import org.testng.ITestContext;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.cloud.AbstractExamplesTest;
 
 public class ByonExamplesTest extends AbstractExamplesTest {
 
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap(final ITestContext testContext) {
 		super.bootstrap(testContext);
 	}
 	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testTravel() throws Exception {
 		super.testTravel();
 	}
 	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testPetclinic()
 			throws Exception {
 		super.testPetclinic();
 	}
 
 	@AfterMethod(alwaysRun = true)
 	public void cleanUp() {
 		super.uninstallApplicationIfFound();
 		super.scanAgentNodesLeak();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() {
 		super.teardown();
 	}
 }
