 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.rackspace;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.cloudifysource.quality.iTests.framework.utils.IOUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.JCloudsCloudService;
 
 public class RackspaceCloudService extends JCloudsCloudService {
 
     private static final String RACKSPACE_CERT_PROPERTIES = CREDENTIALS_FOLDER + "/cloud/rackspace/rackspace-cred.properties";
 
    private Properties certProperties = getCloudProperties(RACKSPACE_CERT_PROPERTIES);
	private String user = certProperties.getProperty("user");
	private String apiKey = certProperties.getProperty("apiKey");
    private String tenantId = certProperties.getProperty("tenantId");
 
 	public RackspaceCloudService() {
 		super("rackspace");
 	}
 
 
 	@Override
 	public void injectCloudAuthenticationDetails()
 			throws IOException {
 
 		getProperties().put("user", this.user);
 		getProperties().put("apiKey", this.apiKey);
         getProperties().put("tenantId", this.tenantId);
 
 		Map<String, String> propsToReplace = new HashMap<String, String>();
 		propsToReplace.put("machineNamePrefix " + "\"cloudify-agent\"", "machineNamePrefix " + '"' + getMachinePrefix()
 				+ "cloudify-agent" + '"');
 		propsToReplace.put("managementGroup " + "\"cloudify-manager\"", "managementGroup " + '"' + getMachinePrefix()
 				+ "cloudify-manager" + '"');
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines " + getNumberOfManagementMachines());
 
 		IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 	}
 
     @Override
     public String getUser() {
         return user;
     }
 
     @Override
     public String getApiKey() {
         return apiKey;
     }
 
     @Override
     public String getRegion() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void addOverrides(Properties overridesProps) {
 
     }
 }
