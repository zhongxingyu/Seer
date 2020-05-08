 package org.cloudifysource.quality.iTests.test.esm.stateless.manual.memory;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import iTests.framework.utils.DeploymentUtils;
 import iTests.framework.utils.GsmTestUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.esc.driver.provisioning.CloudifyMachineProvisioningConfig;
 import org.cloudifysource.esc.driver.provisioning.byon.ByonProvisioningDriver;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.util.CloudTestUtils;
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
 import org.openspaces.core.util.MemoryUnit;
 import org.openspaces.grid.gsm.machines.plugins.exceptions.ElasticMachineProvisioningException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.log.LogEntries;
 import com.gigaspaces.log.LogEntryMatcher;
 import com.gigaspaces.log.LogEntryMatchers;
 
 public class DedicatedStatelessManualFailoverAwareTest extends AbstractFromXenToByonGSMTest {
 
     private static final String UNEXPECTED_ESM_LOG_STATEMENT = ElasticMachineProvisioningException.class.getName();
     private static final String EXPECTED_ESM_LOG_STATEMENT ="failover-aware-provisioning-driver";
     
     ProcessingUnit pu;
     
 	@BeforeMethod
     public void beforeTest() {
         super.beforeTestInit();
     }
 
     @BeforeClass
     protected void bootstrap() throws Exception {
         super.bootstrapBeforeClass();
     }
 
     @AfterMethod
     public void afterTest() {
         super.afterTest();
     }
 
     @AfterClass(alwaysRun = true)
     protected void teardownAfterClass() throws Exception {
         super.teardownAfterClass();
     }
 
     @Override
     protected String getCloudName() {
         return "byon-xap-cloudify-management-space";
     }
     
     /**
      * CLOUDIFY-2180
      * Tests that after failover the cloud driver #start() method receives MachineInfo of the failed machine.
      */
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled=true)
     public void testFailedMachineDetails() throws Exception {
         deployPu();
         machineFailover(getAgent(pu));
         assertUndeployAndWait(pu);
     }
 
     /**
      * CLOUDIFY-2180
      * Tests that after failover the cloud driver #start() method receives MachineInfo of the failed machine.
      * Machine is killed only after ESM is restarted. This checks the ESM persists context to space.
      */
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled=true)
     public void testFailedMachineDetailsAfterEsmRestart() throws Exception {
         deployPu();
         restartEsmAndWait();
         machineFailover(getAgent(pu));
         assertUndeployAndWait(pu);
     }
 
     /**
      * CLOUDIFY-2180
      * Tests that after failover the cloud driver #start() method receives MachineInfo of the failed machine.
      * Machine is killed while ESM is down. This checks the ESM persists context to space and can detect machine failure while it was down.
      */
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled=true)
     public void testFailedMachineWhileEsmIsDown() throws Exception {
         deployPu();
         // hold reference to agent before restarting the lus 
         final GridServiceAgent agent = getAgent(pu);
         final ElasticServiceManager[] esms = admin.getElasticServiceManagers().getManagers();
         assertEquals("Expected only 1 ESM instance. instead found " + esms.length, 1, esms.length);
         killEsm();
         machineFailover(agent);
         assertUndeployAndWait(pu);
     }
 
     private void machineFailover(GridServiceAgent agent) throws Exception {
         // stop machine and check ESM log that it starts a machine that is aware of the failed machine.
         LogUtils.log("Stopping agent " + agent.getUid());
         stopByonMachine(getElasticMachineProvisioningCloudifyAdapter(), agent, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         repetitiveAssertNumberOfGSAsRemoved(1, OPERATION_TIMEOUT);
         
         // check CLOUDIFY-2180
         repetitiveAssertFailoverAware();
         GsmTestUtils.waitForScaleToCompleteIgnoreCpuSla(pu, 1, OPERATION_TIMEOUT);
         
         // check ESM not tried to start too many machines, which would eventually result in machine start failure
         repetitiveAssertNoStartMachineFailures();
     }
 
 	private ProcessingUnit deployProcessingUnitOnSeperateMachine() {
 		final String name = "simpleStatelessPu";
 	    final File archive = DeploymentUtils.getArchive(name + ".jar");
         final String puName = name +String.valueOf(new Random().nextInt());
         
         // it is very important for puName to change between tests and not to change between esm restarts
         // this allows the FailoverAwareByonProvisioningDriver mock to read and write state to files 
         // and recover from esm failures that way.
         final ElasticStatelessProcessingUnitDeployment deployment =
                 new ElasticStatelessProcessingUnitDeployment(archive)
                 .name(puName)
                 .memoryCapacityPerContainer(1, MemoryUnit.GIGABYTES)
         		.scale(
 	                new ManualCapacityScaleConfigurer()
 	                .memoryCapacity(1, MemoryUnit.GIGABYTES)
 	                .create());
         
         final CloudifyMachineProvisioningConfig provisioningConfig = getMachineProvisioningConfig();
         provisioningConfig.setDedicatedManagementMachines(true); // do not deploy instance on management machine, since it is going down.
 		deployment.dedicatedMachineProvisioning(provisioningConfig);
 
         return super.deploy(deployment);
 	}
 
     private GridServiceAgent getAgent(ProcessingUnit pu) {
     	return pu.getInstances()[0].getGridServiceContainer().getGridServiceAgent();
     }
     
     @Override
     public void beforeBootstrap() throws IOException {
     	CloudTestUtils.replaceCloudDriverImplementation(
     			getService(),
     			ByonProvisioningDriver.class.getName(), //old class
     			"org.cloudifysource.quality.iTests.FailoverAwareByonProvisioningDriver", //new class
     			"location-aware-provisioning-byon", "2.2-SNAPSHOT"); //jar
     }
     
     private void repetitiveAssertFailoverAware() {
 
         LogUtils.log("Waiting for 1 "+ EXPECTED_ESM_LOG_STATEMENT  + " log entry before undeploying PU");
         final LogEntryMatcher logMatcher = LogEntryMatchers.containsString(EXPECTED_ESM_LOG_STATEMENT);
         repetitiveAssertTrue("Expected " + EXPECTED_ESM_LOG_STATEMENT +" log", new RepetitiveConditionProvider() {
             
             @Override
             public boolean getCondition() {
                 final ElasticServiceManager[] esms = admin.getElasticServiceManagers().getManagers();
                 if (esms.length != 1) {
                     LogUtils.log("Exepcted exactly 1 esm. Discovered " + esms.length);
                     return false;
                 }
                 else {
                     final LogEntries logEntries = esms[0].logEntries(logMatcher);
                     final int count = logEntries.logEntries().size();
                     LogUtils.log("Exepcted at least one "+ EXPECTED_ESM_LOG_STATEMENT + " log entries. Actual :" + count);
                     return count > 0;
                 }
             }
         } , OPERATION_TIMEOUT);
     }
     
     private void repetitiveAssertNoStartMachineFailures() {
     	
     	final ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         LogUtils.log("Checking there is no "+ UNEXPECTED_ESM_LOG_STATEMENT  + " log entry before undeploying PU");
         final LogEntryMatcher logMatcher = LogEntryMatchers.containsString(UNEXPECTED_ESM_LOG_STATEMENT);
         AssertUtils.repetitiveAssertConditionHolds("Unexpected " + UNEXPECTED_ESM_LOG_STATEMENT +" log", new RepetitiveConditionProvider() {
             
             @Override
             public boolean getCondition() {
                 final LogEntries logEntries = esm.logEntries(logMatcher);
                 final int count = logEntries.logEntries().size();
                 LogUtils.log("Exepcted no "+ UNEXPECTED_ESM_LOG_STATEMENT + " log entries. Actual :" + count);
                 return count == 0;
             }
         } , TimeUnit.SECONDS.toMillis(30));
     }
     
 
     private void deployPu() {
         repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
         pu = deployProcessingUnitOnSeperateMachine();
         GsmTestUtils.waitForScaleToCompleteIgnoreCpuSla(pu, 1, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
     }
 }
