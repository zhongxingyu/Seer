 package org.cloudifysource.quality.iTests.test.esm.component.machines;
 
 import java.util.Collection;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsa.GridServiceManagerOptions;
 import org.openspaces.admin.space.SpaceDeployment;
 import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
 import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.quality.iTests.test.esm.component.SlaEnforcementTestUtils;
 
 public class MachinesSlaEnforcementTwoManagementMachinesByonTest extends AbstractMachinesSlaEnforcementTest {
     
 	
 	@BeforeMethod
     public void beforeTest() {
 		super.beforeTestInit();
         
         updateMachineProvisioningConfig(getMachineProvisioningConfig());
         machinesSlaEnforcement = new MachinesSlaEnforcement();
     }
 	
 	@BeforeClass
 	protected void bootstrap() throws Exception {
 		super.bootstrapBeforeClass();
 	}
 	
 	@AfterMethod
     public void afterTest() {
         machinesSlaEnforcement.destroyEndpoint(pu);
         pu.undeploy();
         
         try {
             machinesSlaEnforcement.destroy();
         } catch (Exception e) {
             Assert.fail("Failed to destroy machinesSlaEnforcement",e);
         }
         
         try {
             machineProvisioning.destroy();
         } catch (Exception e) {
             Assert.fail("Failed to destroy machineProvisioning",e);
         }
         super.afterTest();
     }
     	
 	@AfterClass(alwaysRun = true)
 	protected void teardownAfterClass() throws Exception {
 		super.teardownAfterClass();
 	}
 	
    @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = false)
     public void oneMachineTest() throws Exception  {
         
         // the first GSAs is already started in BeginTest
         repetitiveAssertNumberOfGSAsAdded(1, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
 
         Assert.assertEquals(1,admin.getGridServiceManagers().getSize());
 
         pu = super.deploy(new SpaceDeployment(PU_NAME).partitioned(10,1).addZone(ZONE));
         Assert.assertNotNull(pu);
         // Start a seconds machine and put a LUS on it
         GridServiceAgent gsa2 = startNewByonMachine(getElasticMachineProvisioningCloudifyAdapter(), OPERATION_TIMEOUT,TimeUnit.MILLISECONDS);
         repetitiveAssertNumberOfGSAsAdded(2, OPERATION_TIMEOUT);
         gsa2.startGridService(new GridServiceManagerOptions().vmInputArgument("-Dcom.gs.transport_protocol.lrmi.bind-port="+LRMI_BIND_PORT_RANGE));
         Assert.assertTrue(admin.getGridServiceManagers().waitFor(2,OPERATION_TIMEOUT,TimeUnit.MILLISECONDS));
         
         
         // enforce numberOfMachines SLA
         endpoint = createEndpoint(pu, machinesSlaEnforcement);
        CapacityMachinesSlaPolicy sla = createSla(1);
         
         String firstApprovedAgentUid = null; 
         for (int i = 0 ; i < 3 ; i++) {
 
         	SlaEnforcementTestUtils.enforceSlaAndWait(admin, endpoint, sla, machineProvisioning);
         	Collection<String> agentUids = endpoint.getAllocatedCapacity(sla).getAgentUids();
             assertEquals(1, agentUids.size());
         	String allocatedAgentUid = agentUids.iterator().next();
         	if (firstApprovedAgentUid == null) {
         		firstApprovedAgentUid = allocatedAgentUid;
         	}
         	else {
         		Assert.assertEquals(
         				firstApprovedAgentUid,
         				allocatedAgentUid,
         				"MachinesSla keeps changing its mind");
         	}
         }
         
         // make sure no extra machine was started nor terminated
     	// even after SLA has reached steady state
         repetitiveAssertNumberOfGSAsAdded(2, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
         
     }
  }
