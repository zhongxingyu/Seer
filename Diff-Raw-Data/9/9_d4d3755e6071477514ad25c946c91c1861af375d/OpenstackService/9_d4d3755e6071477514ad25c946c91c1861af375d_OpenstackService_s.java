 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.openstack;
 
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.AbstractCloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;
 
 public class OpenstackService extends AbstractCloudService {
 
     private static final String CREDENTIALS_PROPERTIES = CREDENTIALS_FOLDER + "/cloud/openstack/openstack-cred.properties";
 
     public static final String USER_PROP = "user";
     public static final String TENANT_PROP = "tenant";
     public static final String API_KEY_PROP = "apiKey";
     public static final String KEYPAIR_PROP = "keyPair";
     public static final String KEYFILE_PROP = "keyFile";
     public static final String ENDPOINT_PROP = "openstackUrl";
     public static final String HARDWARE_PROP = "hardwareId";
     public static final String IMAGE_PROP = "imageId";
 
     private final Properties properties = getCloudProperties(CREDENTIALS_PROPERTIES);
 
     protected static final String US_EAST_REGION = "us-east-1";
 
     private String user = properties.getProperty("user");
     private String tenant = properties.getProperty("tenant");
     private String apiKey = properties.getProperty("apiKey");
     private String keyPair = properties.getProperty("keyPair");
     private String endpoint = properties.getProperty("openstackUrl");
     private String hardwareId = properties.getProperty("hardwareId");
     private String imageId = properties.getProperty("imageId");
 
     private boolean securityEnabled = false;
 
     public OpenstackService() {
         super("openstack");
         LogUtils.log("credentials file is at: " + CREDENTIALS_PROPERTIES);
     }
 
     public OpenstackService(boolean securityEnabled) {
         super("openstack");
         LogUtils.log("credentials file is at: " + CREDENTIALS_PROPERTIES);
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
 
         getProperties().put(USER_PROP, this.user);
         getProperties().put(TENANT_PROP, this.tenant);
         getProperties().put(API_KEY_PROP, this.apiKey);
         getProperties().put(KEYPAIR_PROP, this.keyPair);
         getProperties().put(KEYFILE_PROP, sshKeyPemName);
         getProperties().put(ENDPOINT_PROP, this.endpoint);
 
         getProperties().put(HARDWARE_PROP, this.hardwareId);
         getProperties().put(IMAGE_PROP, this.imageId);
 
         propsToReplace.put("cloudify-agent-", getMachinePrefix() + "agent-");
         propsToReplace.put("cloudify-manager-", getMachinePrefix() + "manager-");
         propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines " + getNumberOfManagementMachines());
         propsToReplace.put("javaUrl", "// javaUrl");
         propsToReplace.put("// \"externalRouterName\" : \"router-ext\",", "\"externalRouterName\" : \"hpclouddev-router\",");

 
         String pathToCloudGroovy = getPathToCloudGroovy();
         IOUtils.replaceTextInFile(pathToCloudGroovy, propsToReplace);
 
         // Copy pem file
         final File fileToCopy = new File(CREDENTIALS_FOLDER + "/cloud/" + getCloudName() + "/" + sshKeyPemName);
         final File targetLocation = new File(getPathToCloudFolder() + "/upload/");
         FileUtils.copyFileToDirectory(fileToCopy, targetLocation);
 
         if (securityEnabled) {
             File keystoreSrc = new File(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH);
             File keystoreDest = new File(getPathToCloudFolder());
             FileUtils.copyFileToDirectory(keystoreSrc, keystoreDest);
         }
     }
 
     @Override
     public String getUser() {
         return user;
     }
 
     @Override
     public String getApiKey() {
         return apiKey;
     }
 
     public String getCloudProperty(String key) {
         String pathToCloudProperty = getPathToCloudFolder() + "/" + getCloudName() + "-cloud.properties";
         Properties props = new Properties();
         try {
             props.load(new FileInputStream(pathToCloudProperty));
         } catch (Exception e) {
             return null;
         }
         String property = (String) props.getProperty(key);
         if (property != null) {
             if (property.startsWith("\"")) {
                 property = property.substring(1);
             }
             if (property.endsWith("\"")) {
                 property = property.substring(0, property.length() - 1);
             }
         }
         return property;
     }
 }
