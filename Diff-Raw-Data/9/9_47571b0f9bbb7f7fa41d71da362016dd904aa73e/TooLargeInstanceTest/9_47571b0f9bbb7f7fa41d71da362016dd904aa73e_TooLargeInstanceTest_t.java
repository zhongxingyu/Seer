 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.publicprovisioning;
 
 import java.io.IOException;
 
 import org.cloudifysource.dsl.internal.CloudifyErrorMessages;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import iTests.framework.utils.AssertUtils;
 
 /**
  * CLOUDIFY-1414
  * @author elip
  *
  */
 public class TooLargeInstanceTest extends AbstractPublicProvisioningByonCloudTest {
 	
 	private static final String GROOVY_ONE = "groovy-one";
 
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testFaultyService() throws IOException, InterruptedException {
 			
 		int reservedMemoryCapacityPerManagementMachineInMB = getService().getCloud().getProvider().getReservedMemoryCapacityPerManagementMachineInMB();
 		int machineMemoryMB = getService().getCloud().getCloudCompute().getTemplates().get(DEFAULT_TEMPLATE_NAME).getMachineMemoryMB();
 		
 		int memoryForServicesOnManagementMachine = machineMemoryMB - reservedMemoryCapacityPerManagementMachineInMB;
 				
 		// this installation should fail because there is not machine that can accommodate the instance (memory wise)
 		String output = installManualPublicProvisioningServiceAndWait(GROOVY_ONE, 1, memoryForServicesOnManagementMachine * 2, 0, DEFAULT_TEMPLATE_NAME, true, true);
		String error = "Cannot install service " + GROOVY_ONE + ". The requested memory";
		AssertUtils.assertTrue("Installation output does not contains the error " + error, output.contains(error));
 		
 		super.scanForLeakedAgentNodes();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testFaultyApplication() throws IOException, InterruptedException {
 			
 		int reservedMemoryCapacityPerManagementMachineInMB = getService().getCloud().getProvider().getReservedMemoryCapacityPerManagementMachineInMB();
 		int machineMemoryMB = getService().getCloud().getCloudCompute().getTemplates().get(DEFAULT_TEMPLATE_NAME).getMachineMemoryMB();
 		
 		int memoryForServicesOnManagementMachine = machineMemoryMB - reservedMemoryCapacityPerManagementMachineInMB;
 				
 		// this installation should fail because there is not machine that can accommodate the instance (memory wise)
 		String output = installManualPublicProvisioningApplicationAndWait(GROOVY_ONE, 1, memoryForServicesOnManagementMachine * 2, 0, DEFAULT_TEMPLATE_NAME, true, true);
        String error = "Cannot install service " + GROOVY_ONE + ". The requested memory";
		AssertUtils.assertTrue("Installation output does not contains the error " + error, output.contains(error));
 		
 		super.scanForLeakedAgentNodes();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}	
 }
