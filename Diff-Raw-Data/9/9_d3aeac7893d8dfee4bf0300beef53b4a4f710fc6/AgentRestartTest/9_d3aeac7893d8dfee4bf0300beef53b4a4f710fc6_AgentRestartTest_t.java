 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.agentrestart;
 
 import iTests.framework.utils.LogUtils;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsa.GridServiceAgents;
 import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
 import org.openspaces.admin.gsa.events.GridServiceAgentLifecycleEventListener;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * 
  * @author adaml
  *
  */
 public class AgentRestartTest extends AbstractAgentMaintenanceModeTest {
 	
 	private static final long TEN_SECONDS_MILLIS = 10000;
 
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testAgentRestart() throws Exception {
         installServiceAndWait(getServicePath(SERVICE_NAME), SERVICE_NAME);
         final String absolutePuName = ServiceUtils.getAbsolutePUName(APP_NAME, SERVICE_NAME);
         
 
         //These latches describe a proper esm behaviur. 
         final CountDownLatch removed = new CountDownLatch(1);
 		final CountDownLatch added = new CountDownLatch(3);
 		
 		final CountDownLatch removedAfterStabilized = new CountDownLatch((int) removed.getCount() + 1);
 		final CountDownLatch addedAfterStabilized = new CountDownLatch((int) added.getCount() + 1);
         final GridServiceAgentAddedEventListener agentListener = new GridServiceAgentLifecycleEventListener() {
 			
 			@Override
 			public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
 				LogUtils.log("agent removed event has been fired");
 				removed.countDown();
 				removedAfterStabilized.countDown();
 			}
 			
 			@Override
 			public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
 				LogUtils.log("agent added event has been fired");
 				added.countDown();
 				addedAfterStabilized.countDown();
 			}
 		};
 		
 		admin.addEventListener(agentListener);
 		LogUtils.log("Starting maintenance mode for pu with name " + absolutePuName);
 		//set maintenance mode for a long time.
 		startMaintenanceMode(TimeUnit.MINUTES.toMillis(INFINITY_MINUTES));
 		
 		restartAgentMachine(absolutePuName);
 		
 		LogUtils.log("Waiting for esm to recover from machine shutdown.");
 		
 		assertTrue("agent machine did not stop as expected.", removed.await(DEFAULT_WAIT_MINUTES, TimeUnit.MINUTES));
 		assertTrue("agent machine was not added as expected.",added.await(DEFAULT_WAIT_MINUTES, TimeUnit.MINUTES));
 		
 		assertTrue("detected agent removed after cluster was stabilized.", !removedAfterStabilized.await(5l, TimeUnit.MINUTES));
 		assertTrue("detected agent added after cluster was stabilized.", addedAfterStabilized.getCount() == 1);
 		
 		assertNumberOfMachines(2);
 		stopMaintenanceMode(absolutePuName);
 		
 		LogUtils.log("ESM has recovered successfully. uninstalling service " + SERVICE_NAME);
 		uninstallServiceAndWait(SERVICE_NAME);
     }
     
     
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testAgentRestartAfterTimeoutExpires() throws Exception {
     	installServiceAndWait(getServicePath(SERVICE_NAME), SERVICE_NAME);
     	
     	final String absolutePuName = ServiceUtils.getAbsolutePUName(APP_NAME, SERVICE_NAME);
     	
     	LogUtils.log("Starting maintenance mode for pu with name " + absolutePuName + " for 5 minutes");
     	final long maintenanceModeStartTime = System.currentTimeMillis();
     	
     	final String serviceIPBefore = getServiceIP(absolutePuName);
     	
     	startMaintenanceMode(TimeUnit.MINUTES.toSeconds(5));
     	
     	gracefullyShutdownAgent(absolutePuName);
     	
     	
     	GridServiceAgents gridServiceAgents = admin.getGridServiceAgents();
     	
     	while (gridServiceAgents.getSize() != 1) {
     		LogUtils.log("Waiting for admin to detect agent failure...");
     		Thread.sleep(TEN_SECONDS_MILLIS);
     		gridServiceAgents = admin.getGridServiceAgents();
     	}
     	LogUtils.log("Agent failure was detected by the admin.");
     	
     	while (gridServiceAgents.getSize() != 2) {
     		LogUtils.log("Waiting for agent to restart after maintenance mode duration expires..");
     		Thread.sleep(TEN_SECONDS_MILLIS);
     		gridServiceAgents = admin.getGridServiceAgents();
     	}
    	LogUtils.log("agent recovery was detected by the admin. Waiting for service instance to start...");
    	
    	final boolean serviceStarted = admin.getProcessingUnits()
    			.getProcessingUnit(ServiceUtils.getAbsolutePUName("default", SERVICE_NAME)).waitFor(1, 10, TimeUnit.MINUTES);
    	if (!serviceStarted) {
    		AssertFail("Service did not recover after agent recovery from maintenance mode.");
    	}
    	LogUtils.log("Service " + SERVICE_NAME + " started successfully.");
     	
     	final String serviceIPAfter = getServiceIP(absolutePuName);
     	
     	long recoveryDuration = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - maintenanceModeStartTime);
     	
     	LogUtils.log("asserting maintenance mode duration.");
     	assertTrue("Agent recovered before maintenance period expired.", recoveryDuration >= 5);
     	assertTrue("Agent recovery took more time then expected.", recoveryDuration <= 10);
     	
     	LogUtils.log("asserting new agent was provisioned on new machine.");
     	assertTrue("Expecting service instance to start on a different machine.", !serviceIPBefore.equals(serviceIPAfter));
     	
     	uninstallServiceAndWait(SERVICE_NAME);
     	
     }
     
 	@Override
 	protected void customizeCloud() throws Exception {
 		super.customizeCloud();
 		getService().setSudo(false);
 		getService().getProperties().put("keyFile", "testKey.pem");
 	}
 	
 	@Override
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		uninstallServiceIfFound(SERVICE_NAME);
 		super.teardown();
 	}
 }
