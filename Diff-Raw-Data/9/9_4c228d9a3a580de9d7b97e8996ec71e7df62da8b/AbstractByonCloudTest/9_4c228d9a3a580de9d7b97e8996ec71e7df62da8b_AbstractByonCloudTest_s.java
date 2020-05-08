 package test.cli.cloudify.cloud.byon;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 
 import test.cli.cloudify.cloud.NewAbstractCloudTest;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 
 public class AbstractByonCloudTest extends NewAbstractCloudTest {
 	
 	@Override
 	protected void beforeTeardown() throws Exception {
 		closeAdmin();
 	}
 	
 	protected boolean isFilteredAdmin() {
 		return false;
 	}
 
 	protected Admin admin;
 
 	public ByonCloudService getService() {
 		return (ByonCloudService) super.getService();
 	}
 
 
 	@Override
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 
 
 	@Override
 	protected void afterBootstrap() throws Exception {
 		super.afterBootstrap();
 		admin = super.createAdmin();
 	}
 	
 	@Override
 	protected AdminFactory createAdminFactory() {
 		
 		ByonCloudService cloudService = getService();
 		AdminFactory factory = new AdminFactory();
 		String[] managementHosts;
 		// if the cloud is using web services 
 		if (!cloudService.isNoWebServices()){
 			managementHosts = cloudService.getRestUrls();		
			for (String host : managementHosts) {				
				String utlNoHttp = host.substring(7); /* remove "http://" */
 				String ip = utlNoHttp.split(":")[0];
 				factory.addLocators(ip + ":" + CloudifyConstants.DEFAULT_LUS_PORT);
 			}
 		}
 		// if the cloud is not using web services
 		else {
 			managementHosts = cloudService.getMachines();			
 			String host = managementHosts[0];
 			factory.addLocators(host + ":" + CloudifyConstants.DEFAULT_LUS_PORT);
 		}
 		return factory;
 	}
 
 	protected void closeAdmin() {
 		if (admin != null) {
 			admin.close();
 			admin = null;
 		}
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "byon";
 	}
 
 	@Override
 	protected boolean isReusableCloud() {
 		return false;
 	}
 
 
 	@Override
 	protected void customizeCloud() throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 
 	protected List<Machine> getManagementMachines() {
 		return getProcessingUnitMachines("rest");
 	}
 
 	protected List<Machine> getAgentMachines(final String processingUnitName) {
 		return getProcessingUnitMachines(processingUnitName);
 	}
 
 	protected List<Machine> getAllMachines() {
 		return Arrays.asList(admin.getMachines().getMachines());
 	}
 
 	private List<Machine> getProcessingUnitMachines(final String processingUnitName) {
 		List<Machine> machines = new ArrayList<Machine>();
 		ProcessingUnit pu = admin.getProcessingUnits().waitFor(processingUnitName, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		for (ProcessingUnitInstance puInstance : pu.getInstances()) {
 			machines.add(puInstance.getMachine());
 		}
 		return machines;
 	}
 }
