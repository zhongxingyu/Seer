 package test.cli.cloudify.cloud.byon;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Assert;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.machine.Machine;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CommandTestUtils;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import test.cli.cloudify.cloud.services.byon.MultipleTemplatesByonCloudService;
 import framework.utils.ServiceInstaller;
 
 public class CloudOverridesESMFailoverTest extends AbstractKillManagementTest {
 
 	private final static String SIMPLE_RECIPE_FOLDER = CommandTestUtils.getPath("apps/USM/usm/simple-with-template");
 	
 	private MultipleTemplatesByonCloudService service = new MultipleTemplatesByonCloudService();
 
 	
 	@Override
 	protected Machine getMachineToKill() {
 		admin.getElasticServiceManagers().waitFor(1, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		ElasticServiceManager elasticServiceManager = admin.getElasticServiceManagers().getManagers()[0];
 		Machine esmMachine = elasticServiceManager.getMachine();
 		return esmMachine;
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
		service.setNumberOfHostsForTemplate("TEMPLATE_1", 2);
		service.setNumberOfHostsForTemplate("TEMPLATE_2", 0);
		service.setNumberOfHostsForTemplate("TEMPLATE_3", 0);
		// this is for validation purposes, cant have an empty list as host-list in BYON
		service.addHostToTemplate("192.168.1.1","TEMPLATE_2");
		service.addHostToTemplate("192.168.1.2", "TEMPLATE_3");
 		super.bootstrap(service);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testCloudOverridesPersistsAfterESMFailover() throws Exception {
 		
 		File createOverridesFile = null;
 		try {
 						
 			ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), "simple");
 			serviceInstaller.setRecipePath(SIMPLE_RECIPE_FOLDER);
 			serviceInstaller.setCloudOverrides(createCloudOverrideProperties());
 			
 			serviceInstaller.install();
 			
 			// check that override was indeed taken under consideration
 			assertOverrides("default.simple");
 			
 			// terminate the ESM machine and start it again
 			String hostAddress = getMachineToKill().getHostAddress();
 			restartMachineAndWait(hostAddress);
 			startManagement(hostAddress);
 			
 			// make sure the management is up
 			Assert.assertTrue("could not find 2 gsm's after failover", 
 					admin.getGridServiceManagers().waitFor(2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 			Assert.assertTrue("could not find 2 lus's after failover", 
 					admin.getLookupServices().waitFor(2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 			
 			Assert.assertTrue("could not find 2 webui instances after failover", admin.getProcessingUnits().getProcessingUnit("webui").waitFor(2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 			Assert.assertTrue("could not find 2 rest after failover", admin.getProcessingUnits().getProcessingUnit("rest").waitFor(2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 			Assert.assertTrue("could not find 2 space after failover", admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace").waitFor(2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS));
 			
 			
 			// increment instance, this should use the same cloud driver so no overrides needed
 			// after esm failover the cloud driver config should have been loaded into the esm from the gsm
 			serviceInstaller.setInstances(2);
 			
 			assertOverrides("default.simple");
 			
 			serviceInstaller.uninstall();
 			
 			super.scanForLeakedAgentNodes();
 						
 		} finally {
 			if (createOverridesFile != null) {
 				createOverridesFile.delete();
 			}
 		}
 		
 		
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 	
 	@Override
 	protected void customizeCloud() throws Exception {
 		super.customizeCloud();
 		getService().setNumberOfManagementMachines(2);
 	}
 	
 	private Map<String, Object> createCloudOverrideProperties() {
 		
 		Map<String, Object> cloudOverrides = new HashMap<String, Object>();
 		cloudOverrides.put("myEnvVariable", "DEFAULT_OVERRIDES_ENV_VARIABLE");
 
 		return cloudOverrides;
 	}
 	
 	private void assertOverrides(final String puName) {
 		
 		Machine managementMachine = getManagementMachines().get(0);
 		
 		for (Machine agentMachine : getAgentMachines(puName)) {
 			
 			GridServiceAgent managementAgent = managementMachine.getGridServiceAgent();
 			GridServiceAgent agentAgent = agentMachine.getGridServiceAgent();
 			
 			String managementMachineEnvVariable = managementAgent.getVirtualMachine().getDetails().
 					getEnvironmentVariables().get(ByonCloudService.ENV_VARIABLE_NAME);
 			String agentMachineEnvVariable = agentAgent.getVirtualMachine().getDetails().
 					getEnvironmentVariables().get(ByonCloudService.ENV_VARIABLE_NAME);
 			
 			// management machine uses default env 
 			assertEquals(ByonCloudService.ENV_VARIABLE_VALUE, managementMachineEnvVariable);
 			// agent machine should have the overrides because we install with overrides
 			assertEquals("DEFAULT_OVERRIDES_ENV_VARIABLE", agentMachineEnvVariable);
 		}
 		
 		
 	}
 
 }
