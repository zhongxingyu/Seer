 package org.cloudifysource.quality.iTests.test.esm.component.machines;
 
 import org.cloudifysource.esc.driver.provisioning.CloudifyMachineProvisioningConfig;
 import org.cloudifysource.esc.driver.provisioning.ElasticMachineProvisioningCloudifyAdapter;
 import iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import org.cloudifysource.quality.iTests.test.esm.component.SlaEnforcementTestUtils;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsa.GridServiceContainerOptions;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.internal.admin.InternalAdmin;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.zone.config.AnyZonesConfig;
 import org.openspaces.grid.gsm.capacity.CapacityRequirements;
 import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
 import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
 import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
 import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
 import org.openspaces.grid.gsm.machines.isolation.DedicatedMachineIsolation;
 import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
 import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioning;
 import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioningAdapterFactory;
 import org.testng.Assert;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicReference;
 
 public abstract class AbstractMachinesSlaEnforcementTest extends AbstractFromXenToByonGSMTest {
 
     protected static final String PU_NAME = "testspace";
 	protected final static long CONTAINER_MEGABYTES = 128;
     protected final static long BIG_CONTAINER_MEGABYTES = 250;
 	protected MachinesSlaEnforcement machinesSlaEnforcement;
     protected MachinesSlaEnforcementEndpoint endpoint;
     protected ProcessingUnit pu;
     protected ElasticMachineProvisioningCloudifyAdapter machineProvisioning; 
     private static NonBlockingElasticMachineProvisioningAdapterFactory nonblockingAdapterFactory = new NonBlockingElasticMachineProvisioningAdapterFactory();
     protected static final String ZONE = "test_zone";
     protected static final String WRONG_ZONE = "wrong_test_zone";
     
 
 	protected void updateMachineProvisioningConfig(CloudifyMachineProvisioningConfig config) {
 		if (machineProvisioning != null) {
 			LogUtils.log("machineProvisioning:"+machineProvisioning.getConfig().getProperties().toString());
 		}
 		LogUtils.log("config.machineProvisioning:"+config.getProperties().toString());
 		if (machineProvisioning == null || 
 		    !machineProvisioning.getProperties().equals(config.getProperties())) {
 			
 			if (machineProvisioning != null) {
 				try {
 					machineProvisioning.destroy();
 					machineProvisioning=null;
 				} catch (Exception e) {
 		            Assert.fail("Failed to destroy machinesSlaEnforcement",e);
 		        }
 			}
 			machineProvisioning = new ElasticMachineProvisioningCloudifyAdapter();
 	        machineProvisioning.setAdmin(admin);
 	        config.setCloudConfigurationDirectory(getService().getPathToCloudFolder());
 	        machineProvisioning.setProperties(config.getProperties());
 	        ElasticProcessingUnitMachineIsolation isolation = new DedicatedMachineIsolation(PU_NAME);
 	        machineProvisioning.setElasticProcessingUnitMachineIsolation(isolation);
 
 	        machineProvisioning.setElasticGridServiceAgentProvisioningProgressEventListener(agentEventListener);
 
 	        machineProvisioning.setElasticMachineProvisioningProgressChangedEventListener(machineEventListener);
 	        try {
 	            machineProvisioning.afterPropertiesSet();
 	        } catch (final Exception e) {
 	            e.printStackTrace();
 	            Assert.fail("Failed to initialize elastic scale handler",e);
 	        }
 		}
 	}
     
     protected void enforceNumberOfMachines(int numberOfMachines, boolean bigContainer) throws InterruptedException {
         CloudifyMachineProvisioningConfig config = getMachineProvisioningConfig();
         config.setDedicatedManagementMachines(true);
         updateMachineProvisioningConfig(config);
         CapacityMachinesSlaPolicy sla =  createSla(numberOfMachines, bigContainer);
         SlaEnforcementTestUtils.enforceSlaAndWait(admin, endpoint, sla, machineProvisioning);
     }
 
 
     protected void enforceNumberOfMachinesOneContainerPerMachine(int numberOfMachines, boolean bigContainer) throws InterruptedException {
         CloudifyMachineProvisioningConfig config = getMachineProvisioningConfig();
         config.setDedicatedManagementMachines(true);
         updateMachineProvisioningConfig(config);
         CapacityMachinesSlaPolicy sla = createSlaOneContainerPerMachine(numberOfMachines,bigContainer);
         SlaEnforcementTestUtils.enforceSlaAndWait(admin, endpoint, sla, machineProvisioning);
     }
 
     protected CapacityMachinesSlaPolicy createSlaOneContainerPerMachine(int numberOfMachines , boolean bigContainer) {
         long MACHINE_MEMORY_CAPACITY_MB = (long)getFirstManagementMachinePhysicalMemoryInMB();
         long memoryCapacityPerMachineInMB = MACHINE_MEMORY_CAPACITY_MB - super.getMachineProvisioningConfig().getReservedMemoryCapacityPerMachineInMB();
         long memoryCapacityInMB;
         if (bigContainer){
             memoryCapacityPerMachineInMB -= memoryCapacityPerMachineInMB % BIG_CONTAINER_MEGABYTES;
             memoryCapacityInMB = numberOfMachines * BIG_CONTAINER_MEGABYTES;
         }
         else {
             memoryCapacityPerMachineInMB -= memoryCapacityPerMachineInMB % CONTAINER_MEGABYTES;
             memoryCapacityInMB = numberOfMachines * CONTAINER_MEGABYTES;
         }
         return createSlaOneContainerPerMachine(numberOfMachines,machineProvisioning, memoryCapacityInMB,  bigContainer);
     }
 
     protected CapacityMachinesSlaPolicy createSla(int numberOfMachines, boolean bigContainer) {
         long MACHINE_MEMORY_CAPACITY_MB = (long)getFirstManagementMachinePhysicalMemoryInMB();
         long maximumMemoryInMB ;
         long minimumMemoryInMB ;
         long memoryCapacityPerMachineInMB ;
         if (bigContainer){
              maximumMemoryInMB = pu.getTotalNumberOfInstances() * BIG_CONTAINER_MEGABYTES;
              minimumMemoryInMB = Math.min(numberOfMachines,2) * BIG_CONTAINER_MEGABYTES;
             memoryCapacityPerMachineInMB = MACHINE_MEMORY_CAPACITY_MB - super.getMachineProvisioningConfig().getReservedMemoryCapacityPerMachineInMB();
             memoryCapacityPerMachineInMB -= memoryCapacityPerMachineInMB % BIG_CONTAINER_MEGABYTES;
         }
         else {
             maximumMemoryInMB = pu.getTotalNumberOfInstances() * CONTAINER_MEGABYTES;
             minimumMemoryInMB = Math.min(numberOfMachines,2) * CONTAINER_MEGABYTES;
             memoryCapacityPerMachineInMB = MACHINE_MEMORY_CAPACITY_MB - super.getMachineProvisioningConfig().getReservedMemoryCapacityPerMachineInMB();
             memoryCapacityPerMachineInMB -= memoryCapacityPerMachineInMB % CONTAINER_MEGABYTES;
         }
 
 		long memoryCapacityInMB = numberOfMachines * memoryCapacityPerMachineInMB;
 		if (memoryCapacityInMB < minimumMemoryInMB) {
 			memoryCapacityInMB = minimumMemoryInMB;
 		}
 		if (memoryCapacityInMB > maximumMemoryInMB) {
 			memoryCapacityInMB = maximumMemoryInMB;
 		}
 		return createSla(numberOfMachines,machineProvisioning, memoryCapacityInMB,bigContainer);
     }
     
     protected void enforceUndeploy(boolean bigContainer) throws InterruptedException {
         CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
         sla.setCapacityRequirements(new CapacityRequirements());
         sla.setMinimumNumberOfMachines(0);
         sla.setMaximumNumberOfMachines(pu.getTotalNumberOfInstances());
         sla.setMaximumNumberOfContainersPerMachine(pu.getTotalNumberOfInstances());
         if (bigContainer){
             sla.setContainerMemoryCapacityInMB(BIG_CONTAINER_MEGABYTES);
         }
         else {
             sla.setContainerMemoryCapacityInMB(CONTAINER_MEGABYTES);
         }
         sla.setMachineProvisioning(nonblockingAdapterFactory.create(machineProvisioning));
         sla.setMachineIsolation(new DedicatedMachineIsolation(pu.getName()));
         sla.setGridServiceAgentZones(new AnyZonesConfig());
         SlaEnforcementTestUtils.enforceSlaAndWait(admin, endpoint, sla, machineProvisioning);
     }
     
     protected CapacityMachinesSlaPolicy createSla(int numberOfMachines, ElasticMachineProvisioning machineProvisioning, long memoryCapacityInMB,boolean bigContainer) {
         CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
         sla.setCapacityRequirements(new CapacityRequirements(new MemoryCapacityRequirement(memoryCapacityInMB)));
         sla.setMinimumNumberOfMachines(Math.min(numberOfMachines,2));
         sla.setMaximumNumberOfMachines(pu.getTotalNumberOfInstances());
         sla.setMaximumNumberOfContainersPerMachine(pu.getTotalNumberOfInstances());
         if (bigContainer){
             sla.setContainerMemoryCapacityInMB(BIG_CONTAINER_MEGABYTES);
         }
         else {
             sla.setContainerMemoryCapacityInMB(CONTAINER_MEGABYTES);
         }
         sla.setMachineProvisioning(nonblockingAdapterFactory.create(machineProvisioning));
         sla.setMachineIsolation(new DedicatedMachineIsolation(pu.getName()));
         sla.setGridServiceAgentZones(new AnyZonesConfig());
         return sla;
     }
 
     protected CapacityMachinesSlaPolicy createSlaOneContainerPerMachine(int numberOfMachines, ElasticMachineProvisioning machineProvisioning, long memoryCapacityInMB , boolean bigContainer) {
         CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
         sla.setCapacityRequirements(new CapacityRequirements(new MemoryCapacityRequirement(memoryCapacityInMB)));
         sla.setMinimumNumberOfMachines(Math.min(numberOfMachines,2));
         sla.setMaximumNumberOfMachines(pu.getTotalNumberOfInstances());
         sla.setMaximumNumberOfContainersPerMachine(1);
         if (bigContainer){
             sla.setContainerMemoryCapacityInMB(BIG_CONTAINER_MEGABYTES);
         }
         else {
             sla.setContainerMemoryCapacityInMB(CONTAINER_MEGABYTES);
         }
         sla.setMachineProvisioning(nonblockingAdapterFactory.create(machineProvisioning));
         sla.setMachineIsolation(new DedicatedMachineIsolation(pu.getName()));
         sla.setGridServiceAgentZones(new AnyZonesConfig());
         return sla;
     }
 
     protected static MachinesSlaEnforcementEndpoint createEndpoint(final ProcessingUnit pu,
             final MachinesSlaEnforcement machinesSlaEnforcement) {
         final AtomicReference<MachinesSlaEnforcementEndpoint> endpointRef = new AtomicReference<MachinesSlaEnforcementEndpoint>();
         final CountDownLatch latch = new CountDownLatch(1);
         ((InternalAdmin) pu.getAdmin())
                 .scheduleNonBlockingStateChange(new Runnable() {
                     public void run() {
                         MachinesSlaEnforcementEndpoint endpoint = machinesSlaEnforcement.createEndpoint(pu);
                         endpointRef.set(endpoint);
                         latch.countDown();
                     }
                 });
         try {
             latch.await();
         } catch (InterruptedException e) {
             Assert.fail("Interrupted", e);
         }
         return endpointRef.get();
     }
 
 	protected GridServiceContainer startContainerOnAgent(GridServiceAgent gsa) {
 		final String containerZone = pu.getRequiredZones()[0];
 		return gsa.startGridServiceAndWait(new GridServiceContainerOptions()
                 .vmInputArgument("-Dcom.gs.zones="+containerZone)
                 .vmInputArgument("-Dcom.gs.transport_protocol.lrmi.bind-port="+LRMI_BIND_PORT_RANGE));
 	}
 
 }
