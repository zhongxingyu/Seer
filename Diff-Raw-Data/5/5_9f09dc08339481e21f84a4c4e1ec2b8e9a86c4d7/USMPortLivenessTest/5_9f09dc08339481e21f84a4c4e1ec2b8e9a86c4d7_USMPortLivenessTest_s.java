 package test.usm;
 
 import static framework.utils.AdminUtils.loadGSC;
 import static framework.utils.AdminUtils.loadGSM;
 
 import java.util.concurrent.TimeUnit;
 
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 public class USMPortLivenessTest extends UsmAbstractTest{
 
 
 
 	@BeforeMethod
 	@Override
 	public void beforeTest() {
 		super.beforeTest();
 		loadGSM(admin.getGridServiceAgents().waitForAtLeastOne(10, TimeUnit.SECONDS));
 		loadGSC(admin.getGridServiceAgents().waitForAtLeastOne(10, TimeUnit.SECONDS));
		this.processName = "SimpleFilewriteAndPortOpener-service";
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void legitUSMPortConfigurationTest() throws Exception{
 		this.serviceFileName = "allPortsOpen-service.groovy";
 		assertOnePUForProcess();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void wrongPortTest() throws Exception{
 
 		this.serviceFileName = "notAllPortsOpen-service.groovy";
 		assertZeroPUsForProcess();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void legitUSMPortConfigurationStartDetectionTest() throws Exception{
 		this.serviceFileName = "allPortsOpen_UsingStartDetection-service.groovy";
 		assertOnePUForProcess();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void wrongPortStartDetectionTest() throws Exception{
 
 		this.serviceFileName = "notAllPortsOpen_UsingStartDetection-service.groovy";
 		assertZeroPUsForProcess();
 	}
 }
