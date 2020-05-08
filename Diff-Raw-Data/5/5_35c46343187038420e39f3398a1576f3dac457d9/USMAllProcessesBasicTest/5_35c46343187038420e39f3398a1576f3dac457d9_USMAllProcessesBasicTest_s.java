 package test.usm;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.cloudify.dsl.Service;
 import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 
 import framework.utils.AdminUtils;
 import framework.utils.LogUtils;
 
 /**
  * cross platform example for the usm
  * 
  * @author rafi
  * @since 8.0
  */
 public class USMAllProcessesBasicTest extends UsmAbstractTest {
 
 	private static final int MODIFIED_PORT = 7790;
 
 	private Machine machineA;
 
 	private GridServiceContainer gscA;
 
 	@BeforeMethod
 	@Override
 	public void beforeTest() {
 		super.beforeTest();
 		LogUtils.log("waiting for 1 machine");
 		admin.getMachines().waitFor(1); // TODO - ALWAYS INCLUDE TIMEOUT!!!
 		LogUtils.log("waiting for 1 GSA instances");
 		admin.getGridServiceAgents().waitFor(1);
 		final GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
 		final GridServiceAgent gsaA = agents[0];
 		machineA = gsaA.getMachine();
 		final GridServiceManager gsmA = AdminUtils.loadGSM(machineA); // GSM A
 		gscA = AdminUtils.loadGSC(machineA); // GSC A
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void jmxTest() throws Exception {
 		setProcessName(CloudifyConstants.DEFAULT_APPLICATION_NAME + "." + UsmAbstractTest.SIMPLE_JAVA);
 		final Service service = USMTestUtils.usmDeploy(processName, UsmAbstractTest.SIMPLE_JAVA_SERVICE_FILE_NAME);
 		final ProcessingUnit pu = admin.getProcessingUnits().waitFor(processName);
 		pu.waitFor(pu.getTotalNumberOfInstances());
 		assertTrue(USMTestUtils.waitForPuRunningState(processName, 60, TimeUnit.SECONDS, admin));
 		pu.startStatisticsMonitor();
 		final ProcessingUnitInstance pui = pu.getInstances()[0];
 		final Map<String, Object> monitors = pui.getStatistics().getMonitors().get("USM").getMonitors();
 		final String details = (String) monitors.get("Details");
 		final String type = (String) monitors.get("Type");
 		final Integer counter = (Integer) monitors.get("Counter");
 		Assert.assertNotNull(counter);
 	}
 
 	/***********
 	 * Tests that multiple groovy service files in a service dir are supported.
 	 * The modified version opens port 7790. The test checks that the port opens, and then un-deploys.
 	 * @throws Exception .
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void simpleProcessModifiedTest() throws Exception {
 		setProcessName(CloudifyConstants.DEFAULT_APPLICATION_NAME + "." + UsmAbstractTest.SIMPLE_JAVA);
 		this.serviceFileName = "simplejava-modifiedservice.groovy";
 
 		final Service service = USMTestUtils.usmDeploy(processName, this.serviceFileName);
 		final ProcessingUnit pu = admin.getProcessingUnits().waitFor(processName);
 
 		pu.waitFor(pu.getTotalNumberOfInstances());
 		assertTrue(USMTestUtils.waitForPuRunningState(processName, 60, TimeUnit.SECONDS, admin));
 		final ProcessingUnitInstance pui = pu.getInstances()[0];
 		final String address = pui.getMachine().getHostAddress();
 		assertTrue("Connection test to simple process port failed", isPortOpen(address, MODIFIED_PORT));
 
 		pu.undeploy();
		Assert.assertNull(admin.getProcessingUnits().getProcessingUnit(service.getName()));
 
 	}
 
 	protected boolean isPortOpen(final String address, final int port) {
 		Socket socket = null;
 		try {
 			socket = new Socket();
 			socket.connect(new InetSocketAddress(address, port));
 			return true;
 		} catch (final Exception e) {
 			return false;
 		} finally {
 			try {
 				if (socket != null) {
 					socket.close();
 				}
 
 			} catch (final IOException ioe) {
 				// ignore
 			}
 		}
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void simpleProcessTest() throws Exception {
 		setProcessName(CloudifyConstants.DEFAULT_APPLICATION_NAME + "." + UsmAbstractTest.SIMPLE_JAVA);
 		this.serviceFileName = UsmAbstractTest.SIMPLE_JAVA_SERVICE_FILE_NAME;
 		basicTest();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = false)
 	public void cassandraTest() throws Exception {
 		setProcessName(CloudifyConstants.DEFAULT_APPLICATION_NAME + "." + UsmAbstractTest.CASSANDRA);
 		this.serviceFileName = UsmAbstractTest.CASSANDRA_SERVICE_FILE_NAME;
 		extendedTest(new String[] { "Completed Tasks", "Pending Tasks" });
 	}
 
 	public void basicTest() throws IOException, PackagingException {
 		extendedTest(new String[] {});
 	}
 
 	void extendedTest(final String[] monitors) throws IOException, PackagingException {
 		final Service service = USMTestUtils.usmDeploy(processName, this.serviceFileName);
 		final ProcessingUnit pu = admin.getProcessingUnits().waitFor(processName);
 		pu.waitFor(pu.getTotalNumberOfInstances());
 		assertTrue(USMTestUtils.waitForPuRunningState(processName, 60, TimeUnit.SECONDS, admin));
 		pu.startStatisticsMonitor();
 
 		USMTestUtils.assertMonitors(pu, monitors);
 
 		pu.undeploy();
		Assert.assertNull(admin.getProcessingUnits().getProcessingUnit(service.getName()));
 
 	}
 
 }
