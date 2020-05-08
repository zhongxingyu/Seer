 package test.cli.cloudify.cloud;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.testng.Assert;
 
 import test.cli.cloudify.CloudTestUtils;
 import test.cli.cloudify.CommandTestUtils;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.WebUtils;
 
 public abstract class AbstractCloudService implements CloudService {
 	
 	public static final int NUM_OF_MANAGEMENT_MACHINES = 1;
 	
     protected URL[] restAdminUrl = new URL[NUM_OF_MANAGEMENT_MACHINES];
     protected URL[] webUIUrl = new URL[NUM_OF_MANAGEMENT_MACHINES];
 	
 	public abstract String getCloudName();
 	
 	public abstract void injectAuthenticationDetails() throws IOException;
 	
     public URL getMachinesUrl(String url) throws Exception {
         return new URL(stripSlash(url) + "/admin/machines");
     }
     
     private void overrideLicenseAndLogs() {
     	File logging = new File(SGTestHelper.getSGTestRootDir() + "/config/gs_logging.properties");
     	File license = new File(SGTestHelper.getSGTestRootDir() + "apps/cloudify/cloud/gslicense.xml");
     	File uploadOverrides = new File(ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/" + getCloudName() + "/upload/cloudify-overrides/");
     	uploadOverrides.mkdir();
     	File uploadLoggsDir = new File(uploadOverrides.getAbsoluteFile() + "config/");
     	uploadLoggsDir.mkdir();
     	try {
    		FileUtils.copyFile(logging, uploadLoggsDir);
    		FileUtils.contentEquals(license, uploadOverrides);
     	} catch (IOException e) {
     		LogUtils.log("Failed to copy files to cloudify-overrides directory" , e);
     	}
     }
 	
     private static String stripSlash(String str) {
         if (str == null || !str.endsWith("/")) {
             return str;
         }
         return str.substring(0, str.length()-1);
     }
  
     
 	@Override
 	public void bootstrapCloud() throws IOException, InterruptedException {
 		
 		overrideLicenseAndLogs();
 		injectAuthenticationDetails();
 		String output = CommandTestUtils.runCommandAndWait("bootstrap-cloud --verbose " + getCloudName());
 		LogUtils.log("Extracting rest url's from cli output");
 		restAdminUrl = extractRestAdminUrls(output, NUM_OF_MANAGEMENT_MACHINES);
 		LogUtils.log("Extracting webui url's from cli output");
 		webUIUrl = extractWebuiUrls(output, NUM_OF_MANAGEMENT_MACHINES);
 		assertBootstrapServicesAreAvailable();
 	    
 	    URL machinesURL;
 		try {
 			machinesURL = getMachinesUrl(restAdminUrl[0].toString());
 		    AssertUtils.assertEquals("Expecting " + NUM_OF_MANAGEMENT_MACHINES + " machines", 
 		    		NUM_OF_MANAGEMENT_MACHINES, CloudTestUtils.getNumberOfMachines(machinesURL));
 		} catch (Exception e) {
 			LogUtils.log("caught exception while geting number of management machines", e);
 		}
 	}
 
 	@Override
 	public void teardownCloud() throws IOException, InterruptedException {
 		try {
 			CommandTestUtils.runCommandAndWait("teardown-cloud --verbose -force " + getCloudName());
 		}
 		finally {
 			try {
 				deleteCloudFiles(getCloudName());
 			} catch (IOException e) {
 				Assert.fail("Failed to clean up after test finished: " + e.getMessage(), e);
 			}
 		}	
 	}
 
 	@Override
 	public String getRestUrl() {
 		if (restAdminUrl[0] != null) { // this means the cloud was bootstrapped properly			
 			return restAdminUrl[0].toString();
 		}
 		return null;
 	}
 	
 	@Override 
 	public String getWebuiUrl() {
 		if (webUIUrl[0] != null) { // this means the cloud was bootstrapped properly
 			return webUIUrl[0].toString();			
 		}
 		return null;
 	}
 	
 	
 	private URL[] extractRestAdminUrls(String output, int numberOfManagementMachines) throws MalformedURLException {
 		
 		URL[] restAdminUrls = new URL[numberOfManagementMachines];
 		
 		Pattern restPattern = Pattern.compile(CloudTestUtils.REST_URL_REGEX);
 		Matcher restMatcher = restPattern.matcher(output);
 		
 		// This is sort of hack.. currently we are outputting this over ssh and locally with different results
 		for (int i = 0; i < numberOfManagementMachines ; i++) {
 			AssertUtils.assertTrue("Could not find actual rest url", restMatcher.find());
 			String rawRestAdminUrl = restMatcher.group(1);
 			restAdminUrls[i] = new URL(rawRestAdminUrl);
 		}
 
 		return restAdminUrls;
 
 	}
 
 	private URL[] extractWebuiUrls(String cliOutput, int numberOfManagementMachines) throws MalformedURLException {
 		
 		URL[] webuiUrls = new URL[numberOfManagementMachines];
 		
 		Pattern webUIPattern = Pattern.compile(CloudTestUtils.WEBUI_URL_REGEX);
 		Matcher webUIMatcher = webUIPattern.matcher(cliOutput);
 		
 		// This is sort of hack.. currently we are outputting this over ssh and locally with different results
 		for (int i = 0; i < numberOfManagementMachines ; i++) {
 			AssertUtils.assertTrue("Could not find actual webui url", webUIMatcher.find());
 			String rawWebUIUrl = webUIMatcher.group(1);
 			webuiUrls[i] = new URL(rawWebUIUrl);
 		}
 		
 		return webuiUrls;
 	}
 	
 	private void deleteCloudFiles(String cloudName) throws IOException {
 		
 		File cloudPluginDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/" + cloudName + "/");
 		File originalCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.groovy");
 		File backupCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.backup");
 		File targetPemFolder = new File(ScriptUtils.getBuildPath(), "tools/cli/plugins/esc/" + cloudName + "/upload/");
 		
 		for (File file : targetPemFolder.listFiles()) {
 			if (file.getName().contains(".pem")) {
 				FileUtils.deleteQuietly(file);
 				break;
 			}
 		}
 		
 		FileUtils.copyFile(backupCloudDslFile, originalCloudDslFile);
 		FileUtils.deleteQuietly(backupCloudDslFile);
 		
 
 	}
 
 	
 	private void assertBootstrapServicesAreAvailable() throws MalformedURLException {
 		
 		for (int i = 0; i < restAdminUrl.length; i++) {
 			// The rest home page is a JSP page, which will fail to compile if there is no JDK installed. So use testrest instead
 			assertWebServiceAvailable(new URL( restAdminUrl[i].toString() + "/service/testrest"));
 			assertWebServiceAvailable(webUIUrl[i]);
 		}
 
 		
 	}
 	
 	private static void assertWebServiceAvailable(final URL url) {
         AssertUtils.repetitiveAssertTrue(url + " is not up", new RepetitiveConditionProvider() {
             public boolean getCondition() {
                 try {
                     return WebUtils.isURLAvailable(url);
                 } catch (Exception e) {
                     return false;
                 }
             }
         }, CloudTestUtils.OPERATION_TIMEOUT);	    
 	}
 
 
 }
