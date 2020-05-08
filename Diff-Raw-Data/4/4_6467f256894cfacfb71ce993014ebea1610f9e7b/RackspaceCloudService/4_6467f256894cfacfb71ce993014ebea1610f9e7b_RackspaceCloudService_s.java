 package test.cli.cloudify.cloud.services.rackspace;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 
 import test.cli.cloudify.cloud.services.AbstractCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.IOUtils;
 import framework.utils.ScriptUtils;
 
 public class RackspaceCloudService extends AbstractCloudService {
 	
 	private String cloudName = "rsopenstack";
 	private String user = "gsrackspace";
 	private String apiKey = "1ee2495897b53409f4643926f1968c0c";
 	private String tenant = "658142";
 
 	public String getCloudName() {
 		return cloudName;
 	}
 
 	public void setCloudName(String cloudName) {
 		this.cloudName = cloudName;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public void setApiKey(String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	public String getTenant() {
 		return tenant;
 	}
 
 	public void setTenant(String tenant) {
 		this.tenant = tenant;
 	}
 
 	@Override
 	public void injectAuthenticationDetails() throws IOException {
 	
 		String cloudTestPath = (SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/" + cloudName).replace('\\', '/');
 
 		// cloud plugin should include recipe that includes secret key 
 		File cloudPluginDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/" + cloudName + "/");
 		File originalCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.groovy");
 		File backupCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.backup");
 
 		// first make a backup of the original file
 		FileUtils.copyFile(originalCloudDslFile, backupCloudDslFile);
 		
 		Map<String, String> propsToReplace = new HashMap<String,String>();
		propsToReplace.put("ENTER_USER", user);
		propsToReplace.put("ENTER_API_KEY", apiKey);
 		propsToReplace.put("cloudify_agent_", this.machinePrefix + "cloudify_agent");
 		propsToReplace.put("cloudify_manager", this.machinePrefix + "cloudify_manager");
 		propsToReplace.put("ENTER_TENANT", tenant);
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines "  + numberOfManagementMachines);
 		propsToReplace.put("\"openstack.wireLog\": \"false\"", "\"openstack.wireLog\": \"true\"");
 		
 		IOUtils.replaceTextInFile(originalCloudDslFile, propsToReplace);
 	}
 	
 	
 
 }
