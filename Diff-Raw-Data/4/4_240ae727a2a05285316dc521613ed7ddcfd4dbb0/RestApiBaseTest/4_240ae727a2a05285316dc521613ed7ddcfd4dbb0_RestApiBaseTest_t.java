 /**
  * The contents of this file are subject to the license and copyright
  * detailed in the LICENSE and NOTICE files at the root of the source
  * tree and available online at
  *
  * http://www.dspace.org/license/
  */
 
 package uk.ac.jorum.integration;
 
 import org.junit.Before;
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.AfterClass;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.HttpClient;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.utils.URIUtils;
 import java.net.URI;
 import org.dspace.storage.rdbms.DatabaseManager;
 import org.dspace.core.ConfigurationManager;
 import org.dspace.browse.IndexBrowse;
 import java.io.File;
 import java.io.FileReader;
 
 public abstract class RestApiBaseTest {
   private static String apiHost = "localhost";
   private static String apiMountPoint = "/dspace-rest";
   private static String apiProtocol = "http";
   private static int apiPort = 9090;
   private HttpClient client;
 
   @Before
     public void ApiSetup() {
       client = new DefaultHttpClient();
     }
 
   @After
     public void ApiTeardown(){
       client.getConnectionManager().shutdown();
     }
 
   protected String makeRequest(String endpoint) throws Exception {
     return makeRequest(endpoint, "");
   }
   
   protected String makeRequest(String endpoint, String queryString) throws Exception {
 	URI uri = URIUtils.createURI(apiProtocol, apiHost, apiPort, apiMountPoint + endpoint, queryString, "");
 	HttpGet httpget = new HttpGet(uri);
 	httpget.addHeader("Accept", "application/json");
     ResponseHandler<String> responseHandler = new BasicResponseHandler();
     return client.execute(httpget, responseHandler);
     
   }
   
   protected int getResponseCode(String endpoint, String queryString) throws Exception{
 	URI uri = URIUtils.createURI(apiProtocol, apiHost, apiPort, apiMountPoint + endpoint, queryString, "");
 	HttpGet httpget = new HttpGet(uri);
 	httpget.addHeader("Accept", "application/json");
 	HttpResponse response = client.execute(httpget);
     return response.getStatusLine().getStatusCode();
   }
 
   protected int getResponseCode(String endpoint) throws Exception{
 	return getResponseCode(endpoint, "");
   }
 
   protected static void loadDatabase(String filename) throws Exception {
     ConfigurationManager.loadConfig("src/test/resources/config/dspace-integ-testrun.cfg");
    System.out.println("Loading database file " + filename);
     DatabaseManager.loadSql(new FileReader(new File(filename).getCanonicalPath()));
   }
 
   protected void loadFixture(String fixtureName) throws Exception {
    loadDatabase("src/test/resources/setup/cleardb.sql");
     loadDatabase("src/test/resources/fixtures/" + fixtureName + ".sql");
   }
 }
