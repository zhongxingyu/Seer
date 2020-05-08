 package org.cloudifysource.quality.iTests.test.esm.stateless.manual.memory;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.esc.driver.provisioning.byon.ByonProvisioningDriver;
 import iTests.framework.tools.SGTestHelper;
import org.cloudifysource.quality.iTests.framework.utils.DeploymentUtils;
import org.cloudifysource.quality.iTests.framework.utils.GsmTestUtils;
import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
import org.cloudifysource.quality.iTests.framework.utils.ToStringUtils;
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.gsa.ElasticServiceManagerOptions;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.CapacityRequirementsConfigurer;
 import org.openspaces.admin.pu.elastic.config.ManualCapacityPerZonesScaleConfigurer;
 import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
 import org.openspaces.admin.zone.Zone;
 import org.openspaces.admin.zone.config.ExactZonesConfig;
 import org.openspaces.admin.zone.config.ExactZonesConfigurer;
 import org.openspaces.core.util.MemoryUnit;
 import org.testng.annotations.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 public class DedicatedStatelessManualPerZoneByonFailoverTest extends AbstractFromXenToByonGSMTest {
 
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
 
     private static final long TIME_TO_START_AGENT_MILLIS = TimeUnit.MINUTES.toMillis(3);
     String ZONE1 = "zone1";
     String ZONE2 = "zone2";
     String CLOUD_PREFIX = "__cloud.zone.";
 
     /**
      * @throws Exception
      * @see "GS-10573", GS-10577
      */
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled=true)
     public void testSpecifyZone() throws Exception {
         twoZoneFailoverTest(true);
     }
 
     /**
      * @throws Exception
      * @see "GS-10576", GS-10577
      */
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled=true)
     public void testNoSpecifyZone() throws Exception {
         twoZoneFailoverTest(false);
     }
 
     public void twoZoneFailoverTest(boolean specifyZone) throws Exception {
         repetitiveAssertNumberOfGSAsAdded(1, OPERATION_TIMEOUT);
 
         File archive = DeploymentUtils.getArchive("simpleStatelessPu.jar");
         ElasticStatelessProcessingUnitDeployment deployment =
                 new ElasticStatelessProcessingUnitDeployment(archive)
                         .memoryCapacityPerContainer(1, MemoryUnit.GIGABYTES);
 
         if (specifyZone) {
             deployment.scale(
                     new ManualCapacityPerZonesScaleConfigurer()
                             .addZone(new String[] {CLOUD_PREFIX+ZONE1},
                                     new CapacityRequirementsConfigurer()
                                             .memoryCapacity(1, MemoryUnit.GIGABYTES)
                                             .create())
                             .addZone(new String[] {CLOUD_PREFIX+ZONE2},
                                     new CapacityRequirementsConfigurer()
                                             .memoryCapacity(1, MemoryUnit.GIGABYTES)
                                             .create())
                             .create());
             deployment.dedicatedMachineProvisioning(getMachineProvisioningConfig());
         }
         else {
             deployment.scale(
                     new ManualCapacityScaleConfigurer()
                             .memoryCapacity(2, MemoryUnit.GIGABYTES)
                             .enableGridServiceAgentZonesAware()
                             .atMostOneContainerPerMachine()
                             .create());
 
             //round robin on 4 zones, we should expect only ZONE1,ZONE2 to start
             deployment.dedicatedMachineProvisioning(getMachineProvisioningConfigDedicatedManagement());
         }
 
         final ProcessingUnit pu = super.deploy(deployment);
 
         GsmTestUtils.waitForScaleToCompleteIgnoreCpuSla(pu, 2, OPERATION_TIMEOUT);
 
         repetitiveAssertNumberOfGSAsAdded(3, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSCsAdded(2, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSCsRemoved(0, OPERATION_TIMEOUT);
 
         // 2 options for zones prefix:
         // 1. "__cloud.zone." is appended by the ElasticMachineProvisioningCloudifyAdapter when locationId is null
         // 2. we should manually append the prefix "__cloud.zone." when adding zones explicitly
         GridServiceAgent gsa1 = assertOneGridServiceAgentsInZone(CLOUD_PREFIX+ZONE1);
         assertOneGridServiceAgentsInZone(CLOUD_PREFIX+ZONE2);
 
         stopByonMachine(getElasticMachineProvisioningCloudifyAdapter(), gsa1, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         repetitiveAssertGridServiceAgentRemoved(gsa1, OPERATION_TIMEOUT);
 
         GsmTestUtils.waitForScaleToCompleteIgnoreCpuSla(pu, 2, OPERATION_TIMEOUT);
         assertOneGridServiceAgentsInZone(CLOUD_PREFIX+ZONE1);
         assertOneGridServiceAgentsInZone(CLOUD_PREFIX+ZONE2);
         repetitiveAssertNumberOfGSAsAdded(4, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSAsRemoved(1, OPERATION_TIMEOUT);
 
         ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         assertNotNull(esm);
         restartElasticServiceManager(esm);
 
         repetitiveAssertNumberOfGSAsHolds(4,1, TIME_TO_START_AGENT_MILLIS);
 
         assertUndeployAndWait(pu);
     }
 
     private GridServiceAgent assertOneGridServiceAgentsInZone(String zoneName) {
         Zone zone = admin.getZones().getByName(zoneName);
        assertNotNull("Could not find agent with zone " + zoneName +" Agents="+ ToStringUtils.gsasToString(admin.getGridServiceAgents()),zone);
         GridServiceAgent[] agents = zone.getGridServiceAgents().getAgents();
         assertEquals(1, agents.length);
 
         ExactZonesConfig expectedZones = new ExactZonesConfigurer().addZone(zoneName).create();
         for (GridServiceAgent agent : agents) {
             assertEquals(expectedZones, agent.getExactZones());
         }
         return agents[0];
     }
 
     @Override
     public void beforeBootstrap() throws IOException {
         String s = System.getProperty("file.separator");
         String repoQualityItests = DeploymentUtils.getQualityItestsPath(s);
         // copy custom location aware driver to cloudify-overrides
         File locationAwareDriver = new File (repoQualityItests +s+"location-aware-provisioning-byon"+s+"1.0-SNAPSHOT"+s+"location-aware-provisioning-byon-1.0-SNAPSHOT.jar");
         File uploadOverrides =
                 new File(getService().getPathToCloudFolder() + "/upload/cloudify-overrides/");
         if (!uploadOverrides.exists()) {
             uploadOverrides.mkdir();
         }
 
         File uploadEsmDir = new File(uploadOverrides.getAbsoluteFile() + "/lib/platform/esm");
         File localEsmFolder = new File(SGTestHelper.getBuildDir() + "/lib/platform/esm");
 
         FileUtils.copyFileToDirectory(locationAwareDriver, uploadEsmDir, true);
         FileUtils.copyFileToDirectory(locationAwareDriver, localEsmFolder, false);
 
         final Map<String, String> propsToReplace = new HashMap<String, String>();
 
         final String oldCloudDriverClazz = ByonProvisioningDriver.class.getName();
         String newCloudDriverClazz = "org.cloudifysource.quality.iTests.LocationAwareByonProvisioningDriver" ;
 
         propsToReplace.put(toClassName(oldCloudDriverClazz),toClassName(newCloudDriverClazz));
         IOUtils.replaceTextInFile(getService().getPathToCloudGroovy(), propsToReplace);
     }
 
     public String toClassName(String className) {
         return "className \""+className+"\"";
     }
 
     private ElasticServiceManager restartElasticServiceManager(final ElasticServiceManager esm) {
         final GridServiceAgent agent = esm.getGridServiceAgent();
         GsmTestUtils.killElasticServiceManager(esm);
         return agent.startGridServiceAndWait(new ElasticServiceManagerOptions().vmInputArgument("-Dcom.gs.transport_protocol.lrmi.bind-port="+LRMI_BIND_PORT_RANGE));
     }
 }
