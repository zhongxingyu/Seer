 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.privateEc2;
 
 import iTests.framework.utils.IOUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.AbstractCloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 
 public class PrivateEc2Service extends AbstractCloudService {
 
    private static final String EC2_CERT_PROPERTIES = CREDENTIALS_FOLDER + "/cloud/privateEc2/ec2-cred.properties";
     private final Properties certProperties = getCloudProperties(EC2_CERT_PROPERTIES);
 
     protected final String ACCESS_KEY_PROP = "accessKey";
     protected final String API_KEY_PROP = "apiKey";
     protected final String KEYPAIR_PROP = "keyPair";
     protected final String KEYFILE_PROP = "keyFile";
     protected final String LOCATION_PROP = "locationId";
 
     protected static final String US_EAST_REGION = "us-east-1";
 
     private String user = certProperties.getProperty("user");
     private String apiKey = certProperties.getProperty("apiKey");
     private String keyPair = certProperties.getProperty("keyPair");
     private boolean securityEnabled = false;
 
     public PrivateEc2Service() {
         super("privateEc2");
     }
 
     public PrivateEc2Service(boolean securityEnabled) {
         super("privateEc2");
         this.securityEnabled = securityEnabled;
     }
 
     @Override
     public String getRegion() {
         return System.getProperty("ec2.region", US_EAST_REGION);
     }
 
     @Override
     public void injectCloudAuthenticationDetails() throws IOException {
         final Map<String, String> propsToReplace = new HashMap<String, String>();
 
         // add a pem file
         final String sshKeyPemName = this.keyPair + ".pem";
 
         getProperties().put(ACCESS_KEY_PROP, this.user);
         getProperties().put(API_KEY_PROP, this.apiKey);
         getProperties().put(API_KEY_PROP, this.apiKey);
         getProperties().put(KEYPAIR_PROP, this.keyPair);
         getProperties().put(KEYFILE_PROP, sshKeyPemName);
         getProperties().put(LOCATION_PROP, US_EAST_REGION);
 
         propsToReplace.put("cloudify-agent-", getMachinePrefix() + "cloudify-agent");
         propsToReplace.put("cloudify-manager", getMachinePrefix() + "cloudify-manager");
         propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines "
                 + getNumberOfManagementMachines());
 
         propsToReplace.put("\"cfnManagerTemplate\":\".*\"", "\"cfnManagerTemplate\":\"" + getPathToCloudFolder() + "/privateEc2-cfn.template\"");
         propsToReplace.put("\"cloudDirectory\":\".*\"", "\"cloudDirectory\":\"" + getPathToCloudFolder() + "\"");
 
         IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 
        
        final File fileToCopy = new File(CREDENTIALS_FOLDER + "/cloud/" + getCloudName() + "/" + sshKeyPemName);//new File(CREDENTIALS_FOLDER + "/" + sshKeyPemName);
         final File targetLocation = new File(getPathToCloudFolder() + "/upload/");
         FileUtils.copyFileToDirectory(fileToCopy, targetLocation);
 
         if (securityEnabled) {
             File keystoreSrc = new File(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH);
             File keystoreDest = new File(getPathToCloudFolder());
             FileUtils.copyFileToDirectory(keystoreSrc, keystoreDest);
         }
     }
 
     public void setUser(final String user) {
         this.user = user;
     }
 
     @Override
     public String getUser() {
         return user;
     }
 
     public void setApiKey(final String apiKey) {
         this.apiKey = apiKey;
     }
 
     @Override
     public String getApiKey() {
         return apiKey;
     }
 
     public void setKeyPair(final String keyPair) {
         this.keyPair = keyPair;
     }
 
     public String getKeyPair() {
         return keyPair;
     }
 }
