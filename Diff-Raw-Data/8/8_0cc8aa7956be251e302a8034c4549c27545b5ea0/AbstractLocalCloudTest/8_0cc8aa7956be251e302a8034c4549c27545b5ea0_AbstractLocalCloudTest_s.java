 package test.cli.cloudify;
 
 import static framework.utils.LogUtils.log;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import framework.utils.*;
 import net.jini.discovery.Constants;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.type.JavaType;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.*;
 
 import com.gigaspaces.cloudify.shell.StringUtils;
 import com.gigaspaces.cloudify.shell.commands.CLIException;
 import com.gigaspaces.cloudify.shell.rest.ErrorStatusException;
 import test.AbstractTest;
 
 public class AbstractLocalCloudTest extends AbstractTest {
 
     protected final int WAIT_FOR_TIMEOUT = 20;
     private final int HTTP_STATUS_OK = 200;
     private final int restPort = 8100;
     protected static String restUrl = null;
     protected static final String DEFAULT_APPLICTION_NAME = "default";
     private static Set<String> clientStartupPIDs = null;
     private static Set<String> localCloudPIDs = null;
     private static Set<String> alivePIDs = null;
 
 
     @BeforeSuite
     public void beforeSuite() throws Exception {
         LogUtils.log("Tearing-down existing localclouds");
         runCommand("teardown-localcloud");
         clientStartupPIDs = SetupUtils.getLocalProcesses();
         try {
             LogUtils.log("Performing bootstrap");
             boolean portOpenBeforeBootstrap = PortConnectionUtils.isPortOpen("localhost", restPort);
             assertTrue("port " + restPort + " is open on localhost before rest deployment. will not try to deploy rest"
                     , !portOpenBeforeBootstrap);
             runCommand("bootstrap-localcloud");
         } catch (Exception e) {
             LogUtils.log("Booststrap Failed." + e);
             e.printStackTrace();
         }
 
         try {
             this.admin = getAdminWithLocators();
         } catch (UnknownHostException e1) {
             LogUtils.log("Could not create admin " + e1);
             e1.printStackTrace();
         }
         assertTrue("Could not find LUS of local cloud", admin.getLookupServices().waitFor(1, WAIT_FOR_TIMEOUT, TimeUnit.SECONDS));
         try {
             restUrl = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + restPort;
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
         alivePIDs = SetupUtils.getLocalProcesses();
         localCloudPIDs = SetupUtils.getClientProcessesIDsDelta(clientStartupPIDs, alivePIDs);
     }
 
     @BeforeClass
     public void beforeClass() throws Exception{
         LogUtils.log("Test Class Configuration Started: " + this.getClass());
         try {
             this.admin = getAdminWithLocators();
         } catch (UnknownHostException e1) {
             LogUtils.log("Could not create admin " + e1);
             throw e1;
         }
     }
 
     @BeforeMethod
     public void beforeTest() {
         LogUtils.log("Test Configuration Started: " + this.getClass());
     }
 
     @AfterMethod(alwaysRun = true)
     public void afterTest() {
 
         if (admin != null) {
             TeardownUtils.snapshot(admin);
             uninstallAllRunningServices(admin);
         }
         try {
             Set<String> currentPids = SetupUtils.getLocalProcesses();
             Set<String> delta = SetupUtils.getClientProcessesIDsDelta(alivePIDs, currentPids);
 
             if (delta.size() > 0) {
                 String pids = "";
                 for (String pid : delta) {
                     pids += pid + ", ";
                 }
                 LogUtils.log("WARNING There is a leak PIDS [ " + pids + "] are alive");
                 SetupUtils.killProcessesByIDs(delta);
                 LogUtils.log("INFO killing all orphan processes");
                 SetupUtils.killProcessesByIDs(localCloudPIDs);
                 LogUtils.log("INFO killing local cloud processes and boostraping again");
                 beforeSuite();
             }
         } catch (Exception e) {
             LogUtils.log("WARNING Failed to kill processes");
             e.printStackTrace();
         }
     }
 
     @AfterSuite(alwaysRun = true)
     public void afterSuite() {
         try {
             LogUtils.log("Tearing-down localcloud");
             runCommand("teardown-localcloud");
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         try {
             TeardownUtils.teardownAll(admin);
         } catch (Throwable t) {
             log("failed to teardown", t);
         }
         admin.close();
         admin = null;
     }
 
     private Admin getAdminWithLocators() throws UnknownHostException {
         //Class LocalhostGridAgentBootsrapper defines the locator discovery addresses.
         String nicAddress = Constants.getHostAddress();
         //int defaultLusPort = Constants.getDiscoveryPort();
         AdminFactory factory = new AdminFactory();
         LogUtils.log("adding locator to admin : " + nicAddress + ":4168");
         factory.addLocator(nicAddress + ":4168");
         return factory.createAdmin();
     }
 
     //This method implementation is used in order to access the admin api
     //without having to worry about locators issue.
     protected Map<String, Object> getAdminData(final String relativeUrl)
             throws CLIException {
         final String url = getFullUrl("/admin/" + relativeUrl);
         LogUtils.log("performing http get to url: " + url);
         final HttpGet httpMethod = new HttpGet(url);
         return readHttpAdminMethod(httpMethod);
     }
 
     private String getFullUrl(String relativeUrl) {
        return this.restUrl + relativeUrl;
     }
 
     private Map<String, Object> readHttpAdminMethod(
             final HttpRequestBase httpMethod) throws CLIException {
         InputStream instream = null;
         try {
             DefaultHttpClient httpClient = new DefaultHttpClient();
             final HttpResponse response = httpClient.execute(httpMethod);
             if (response.getStatusLine().getStatusCode() != HTTP_STATUS_OK) {
                 LogUtils.log(httpMethod.getURI() + " response code " + response.getStatusLine().getStatusCode());
                 throw new CLIException(response.getStatusLine().toString());
             }
             final HttpEntity entity = response.getEntity();
             if (entity == null) {
                 final ErrorStatusException e = new ErrorStatusException(
                         "comm_error");
                 LogUtils.log(httpMethod.getURI() + " response entity is null", e);
                 throw e;
             }
             instream = entity.getContent();
             final String responseBody = StringUtils.getStringFromStream(instream);
             LogUtils.log(httpMethod.getURI() + " http get response: " + responseBody);
             final Map<String, Object> responseMap = jsonToMap(responseBody);
             return responseMap;
         } catch (final ClientProtocolException e) {
             LogUtils.log(httpMethod.getURI() + " Rest api error", e);
             throw new ErrorStatusException("comm_error", e, e.getMessage());
         } catch (final IOException e) {
             LogUtils.log(httpMethod.getURI() + " Rest api error", e);
             throw new ErrorStatusException("comm_error", e, e.getMessage());
         } finally {
             if (instream != null) {
                 try {
                     instream.close();
                 } catch (final IOException e) {
                 }
             }
             httpMethod.abort();
         }
     }
 
     //returns the number of processing unit instances of the specified service
     protected int getProcessingUnitInstanceCount(String absolutePUName) throws CLIException {
         String puNameAdminUrl = "processingUnits/Names/" + absolutePUName;
         Map<String, Object> mongoProcessingUnitAdminData = getAdminData(puNameAdminUrl);
         return (Integer) mongoProcessingUnitAdminData.get("Instances-Size");
     }
 
     private static Map<String, Object> jsonToMap(final String response)
             throws IOException {
         final JavaType javaType = TypeFactory.type(Map.class);
         ObjectMapper objectMapper = new ObjectMapper();
         return objectMapper.readValue(response, javaType);
     }
 
     protected String runCommand(String command) throws IOException, InterruptedException {
         return CommandTestUtils.runCommandAndWait(command);
     }
 
     protected void uninstallApplication(String applicationName) {
         try {
             DumpUtils.dumpLogs(admin);
            runCommand("connect " + this.restUrl + ";uninstall-application " + applicationName);
         } catch (Exception e) {
             LogUtils.log("Failed to uninstall " + applicationName, e);
             e.printStackTrace();
         }
     }
 
     protected void uninstallService(String serviceName) {
         try {
             DumpUtils.dumpLogs(admin);
            runCommand("connect " + this.restUrl + ";uninstall-service " + serviceName);
         } catch (Exception e) {
             LogUtils.log("Failed to uninstall " + serviceName, e);
             e.printStackTrace();
         }
     }
 
     public void uninstallAllRunningServices(Admin admin) {
         DumpUtils.dumpLogs(admin);
         for (ProcessingUnit pu : admin.getProcessingUnits().getProcessingUnits()) {
             if (!pu.getName().equals("webui") && !pu.getName().equals("rest") && !pu.getName().equals("cloudifyManagementSpace")) {
                 if (!pu.undeployAndWait(30, TimeUnit.SECONDS)) {
                     LogUtils.log("Failed to uninstall " + pu.getName());
                 }
                 else {
                 	LogUtils.log("Uninstalled service: " + pu.getName());
                 }
             }
         }
     }
 }
