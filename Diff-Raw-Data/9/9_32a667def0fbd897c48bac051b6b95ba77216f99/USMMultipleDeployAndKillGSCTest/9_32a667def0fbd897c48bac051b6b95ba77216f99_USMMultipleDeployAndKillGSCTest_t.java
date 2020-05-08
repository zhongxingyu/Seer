 package test.usm;
 
import java.util.concurrent.TimeUnit;

 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 
 import com.gigaspaces.cloudify.dsl.Service;
import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
 
 import framework.utils.AdminUtils;
 import framework.utils.LogUtils;
 
 public class USMMultipleDeployAndKillGSCTest extends UsmAbstractTest {
 
 	private Machine machineA, machineB;
 	private GridServiceContainer gscA;
 
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		super.beforeTest();
 
 		// 1 GSM and 2 GSC at 2 machines
 		LogUtils.log("waiting for 2 machines");
 		admin.getMachines().waitFor(2);
 
 		LogUtils.log("waiting for 2 GSAs");
 		admin.getGridServiceAgents().waitFor(2);
 
 		final GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
 		final GridServiceAgent gsaA = agents[0];
 		final GridServiceAgent gsaB = agents[1];
 
 		machineA = gsaA.getMachine();
 		machineB = gsaB.getMachine();
 
 		// Start GSM A, GSC A, GSM B, GSC B
 		LogUtils.log("starting: 1 GSM and 2 GSC at 2 machines");
 		final GridServiceManager gsmA = AdminUtils.loadGSM(machineA); // GSM A
 		gscA = AdminUtils.loadGSC(machineA); // GSC A
 		// loadGSM(machineB); //GSM B
		processName = CloudifyConstants.DEFAULT_APPLICATION_NAME + "." + processName;
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "2")
 	public void test() throws Exception {
 
 		final Service service = USMTestUtils.usmDeploy(processName, this.serviceFileName);
 
 		// File f = new
 		// File("/export/tgrid/sgtest/deploy/local-builds/build_5593-462/gigaspaces-xap-premium-8.0.3-m3/deploy/simplejavaprocess-service");
 		//
 		// System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
 		// +f.exists());
 		// if(f.exists())
 		// System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"
 		// + f.lastModified());
 
 		// loadGSM(machineB); //GSM B
 		final ProcessingUnit pu = admin.getProcessingUnits().waitFor(service.getName());
 		pu.waitFor(pu.getTotalNumberOfInstances());
		assertTrue(USMTestUtils.waitForPuRunningState(processName, 60, TimeUnit.SECONDS, admin));
 		pu.startStatisticsMonitor();
 
 		USMTestUtils.assertMonitors(pu);
 
 		final GridServiceContainer gscB = AdminUtils.loadGSC(machineB); // GSC B
 		gscA.kill();
 		pu.waitFor(pu.getTotalNumberOfInstances());
 
 		USMTestUtils.assertMonitors(pu);
 
 		Assert.assertEquals(1, admin.getProcessingUnits().getProcessingUnit(service.getName()).getInstances().length);
 
 		final GridServiceContainer gscA = AdminUtils.loadGSC(machineA); // GSC A
 		gscB.kill();
 		pu.waitFor(pu.getTotalNumberOfInstances());
 
 		USMTestUtils.assertMonitors(pu);
 
 		pu.undeploy();
 		Assert.assertNull(admin.getProcessingUnits().getProcessingUnit(service.getName()));
 
 	}
 
 }
