 package com.redhat.qe.sm.cli.tasks;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 
 import com.redhat.qe.api.helper.TestHelper;
 import com.redhat.qe.auto.selenium.Base64;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.RevokedCert;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 import com.redhat.qe.tools.SSLCertificateTruster;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 
 /**
  * @author jsefler
  *
  * Reference: Candlepin RESTful API Documentation: https://fedorahosted.org/candlepin/wiki/API
  */
 public class CandlepinTasks {
 
 	protected static Logger log = Logger.getLogger(SubscriptionManagerTasks.class.getName());
 	protected /*NOT static*/ SSHCommandRunner sshCommandRunner = null;
 	protected /*NOT static*/ String serverInstallDir = null;
 	public static String candlepinCRLFile	= "/var/lib/candlepin/candlepin-crl.crl";
 	public static String defaultConfigFile	= "/etc/candlepin/candlepin.conf";
 	public static String rubyClientDir	= "/client/ruby/";
 	public static File candlepinCACertFile = new File("/etc/candlepin/certs/candlepin-ca.crt");
 	public static HttpClient client;
 	public boolean isOnPremises = false;
 
 	static {
 		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
       	client = new HttpClient(connectionManager);
       	client.getParams().setAuthenticationPreemptive(true);
 		//client = new HttpClient();
 		try {
 			SSLCertificateTruster.trustAllCertsForApacheHttp();
 		}catch(Exception e) {
 			log.log(Level.SEVERE, "Failed to trust all certificates for Apache HTTP Client", e);
 		}
 	}
 	public CandlepinTasks() {
 		super();
 		
 		// TODO Auto-generated constructor stub
 	}
 	
 	public CandlepinTasks(SSHCommandRunner sshCommandRunner, String serverInstallDir, boolean isOnPremises) {
 		super();
 		this.sshCommandRunner = sshCommandRunner;
 		this.serverInstallDir = serverInstallDir;
 		this.isOnPremises = isOnPremises;
 	}
 	
 	
 	/**
 	 * @param serverImportDir
 	 * @param branch - git branch (or tag) to deploy.  The most common values are "master" and "candlepin-latest-tag" (which is a special case)
 	 */
 	public void deploy(String hostname, String serverImportDir, String branch) {
 
 		if (branch.equals("")) {
 			log.info("Skipping deploy of candlepin server since no branch was specified.");
 			return;
 		}
 		
 		log.info("Upgrading the server to the latest git tag...");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(sshCommandRunner, serverInstallDir),1,"Found the server install directory "+serverInstallDir);
 
 		RemoteFileTasks.searchReplaceFile(sshCommandRunner, "/etc/sudoers", "\\(^Defaults[[:space:]]\\+requiretty\\)", "#\\1");	// Needed to prevent error:  sudo: sorry, you must have a tty to run sudo
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "cd "+serverInstallDir+"; git checkout master; git pull", Integer.valueOf(0), null, "(Already on|Switched to branch) 'master'");
 		if (branch.equals("candlepin-latest-tag")) {
			RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "cd "+serverInstallDir+"; git tag | sort -t . -k 3 -n | tail -1", Integer.valueOf(0), "^candlepin", null);
 			branch = sshCommandRunner.getStdout().trim();
 		}
 		if (branch.startsWith("candlepin-")) {
 			RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "cd "+serverInstallDir+"; git checkout "+branch, Integer.valueOf(0), null, "HEAD is now at .* package \\[candlepin\\] release \\["+branch.substring(branch.indexOf("-")+1)+"\\]."); //HEAD is now at 560b098... Automatic commit of package [candlepin] release [0.0.26-1].
 	
 		} else {
			RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "cd "+serverInstallDir+"; git checkout "+branch, Integer.valueOf(0), null, "(Already on|Switched to branch) '"+branch+"'");	// Switched to branch 'master' // Already on 'master'
 		}
 //		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "cd "+serverInstallDir+"; git checkout "+latestGitTag, Integer.valueOf(0), null, "HEAD is now at .* package \\[candlepin\\] release \\["+latestGitTag.substring(latestGitTag.indexOf("-")+1)+"\\]."); //HEAD is now at 560b098... Automatic commit of package [candlepin] release [0.0.26-1].
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "service postgresql restart", /*Integer.valueOf(0) DON"T CHECK EXIT CODE SINCE IT RETURNS 1 WHEN STOP FAILS EVEN THOUGH START SUCCEEDS*/null, "Starting postgresql service:\\s+\\[  OK  \\]", null);
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "cd "+serverImportDir+"; git pull", Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "export FORCECERT=1; export GENDB=1; export HOSTNAME="+hostname+"; export IMPORTDIR="+serverImportDir+"; cd "+serverInstallDir+"/proxy; buildconf/scripts/deploy", Integer.valueOf(0), "Initialized!", null);
 		/* attempt to use live logging
 		SSHCommandResult sshCommandResult = sshCommandRunner.runCommandAndWait("cd "+serverInstallDir+"/proxy; buildconf/scripts/deploy", true);
 			Assert.assertEquals(sshCommandResult.getExitCode(), Integer.valueOf(0));
 			Assert.assertContainsMatch(sshCommandResult.getStdout(), "Initialized!");
 		*/
 	}
 	
 	public void cleanOutCRL() {
 		log.info("Cleaning out the certificate revocation list (CRL) "+candlepinCRLFile+"...");
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "rm -f "+candlepinCRLFile, 0);
 	}
 	
 	/**
 	 * Note: Updating the candlepin server conf files requires a restart of the tomact server.
 	 * @param parameter
 	 * @param value
 	 * 
 	 */
 	public void updateConfigFileParameter(String parameter, String value){
 		Assert.assertEquals(
 				RemoteFileTasks.searchReplaceFile(sshCommandRunner, defaultConfigFile, "^"+parameter+"\\s*=.*$", parameter+"="+value),
 				0,"Updated candlepin config parameter '"+parameter+"' to value: " + value);
 	}
 	
 	static public String getResourceUsingRESTfulAPI(String server, String port, String prefix, String authenticator, String password, String path) throws Exception {
 		GetMethod get = new GetMethod("https://"+server+":"+port+prefix+path);
 		String credentials = authenticator.equals("")? "":"-u "+authenticator+":"+password;
 		log.info("SSH alternative to HTTP request: curl -k "+credentials+" --request GET https://"+server+":"+port+prefix+path);
 		return getHTTPResponseAsString(client, get, authenticator, password);
 	}
 	static public String putResourceUsingRESTfulAPI(String server, String port, String prefix, String authenticator, String password, String path) throws Exception {
 		PutMethod put = new PutMethod("https://"+server+":"+port+prefix+path);
 		String credentials = authenticator.equals("")? "":"-u "+authenticator+":"+password;
 		log.info("SSH alternative to HTTP request: curl -k "+credentials+" --request PUT https://"+server+":"+port+prefix+path);
 		return getHTTPResponseAsString(client, put, authenticator, password);
 	}
 	static public String postResourceUsingRESTfulAPI(String server, String port, String prefix, String authenticator, String password, String path, String requestBody) throws Exception {
 		PostMethod post = new PostMethod("https://"+server+":"+port+prefix+path);
 		if (requestBody != null) {
 			post.setRequestEntity(new StringRequestEntity(requestBody, "application/json", null));
 			post.addRequestHeader("accept", "application/json");
 			post.addRequestHeader("content-type", "application/json");
 		}
 		String credentials = authenticator.equals("")? "":"--user "+authenticator+":"+password;
 		String data = requestBody==null? "":"--data '"+requestBody+"'";
 		String headers = "";
 		for ( org.apache.commons.httpclient.Header header : post.getRequestHeaders()) headers+= "--header '"+header.toString().trim()+"' ";
 
 		log.info("SSH alternative to HTTP request: curl -k --request POST "+credentials+" "+data+" "+headers+" https://"+server+":"+port+prefix+path);
 
 		return getHTTPResponseAsString(client, post, authenticator, password);
 	}
 	
 	static public JSONObject getEntitlementUsingRESTfulAPI(String server, String port, String prefix, String owner, String password, String dbid) throws Exception {
 		return new JSONObject(getResourceUsingRESTfulAPI(server, port, prefix, owner, password, "/entitlements/"+dbid));
 	}
 
 //	static public JSONObject curl_hateoas_ref_ASJSONOBJECT(SSHCommandRunner runner, String server, String port, String prefix, String owner, String password, String ref) throws JSONException {
 //		log.info("Running HATEOAS command for '"+owner+"' on candlepin server '"+server+"'...");
 //
 //		String command = "/usr/bin/curl -u "+owner+":"+password+" -k https://"+server+":"+port+"/candlepin/"+ref;
 //		
 //		// execute the command from the runner (could be *any* runner)
 //		SSHCommandResult sshCommandResult = RemoteFileTasks.runCommandAndAssert(runner, command, 0);
 //		
 //		return new JSONObject(sshCommandResult.getStdout());
 //	}
 	
 	protected static String getHTTPResponseAsString(HttpClient client, HttpMethod method, String username, String password) 
 	throws Exception {
 		HttpMethod m = doHTTPRequest(client, method, username, password);
 		String response = m.getResponseBodyAsString();
 		log.finer("HTTP server returned content: " + response);
 		m.releaseConnection();
 		
 		// When testing against a Stage or Production server where we are not granted enough authority to make HTTP Requests,
 		// our tests will fail.  This block of code is a short cut to simply skip those test. - jsefler 11/15/2010 
 		if (m.getStatusText().equalsIgnoreCase("Unauthorized")) {
 			throw new SkipException("Not authorized make HTTP request to '"+m.getURI()+"' with credentials: username='"+username+"' password='"+password+"'");
 		}
 		
 		return response;
 	}
 	
 	protected static InputStream getHTTPResponseAsStream(HttpClient client, HttpMethod method, String username, String password) 
 	throws Exception {
 		HttpMethod m =  doHTTPRequest(client, method, username, password);
 		InputStream result = m.getResponseBodyAsStream();
 		//m.releaseConnection();
 		return result;
 	}
 	
 	protected static HttpMethod doHTTPRequest(HttpClient client, HttpMethod method, String username, String password) 
 	throws Exception {
 		String server = method.getURI().getHost();
 		int port = method.getURI().getPort();
 	
 		setCredentials(client, server, port, username, password);
 		log.finer("Running HTTP request: " + method.getName() + " on " + method.getURI() + " with credentials for '"+username+"' on server '"+server+"'...");
 		if (method instanceof PostMethod){
 			RequestEntity entity =  ((PostMethod)method).getRequestEntity();
 			log.finer("HTTP Request entity: " + ((StringRequestEntity)entity).getContent());
 		}
 		log.finer("HTTP Request Headers: " + TestHelper.interpose(", ", method.getRequestHeaders()));
 		int responseCode = client.executeMethod(method);
 		log.finer("HTTP server returned: " + responseCode) ;
 		return method;
 	}
 	
 	protected static void setCredentials(HttpClient client, String server, int port, String username, String password) {
 		if (!username.equals(""))
 			client.getState().setCredentials(
 	            new AuthScope(server, port, AuthScope.ANY_REALM),
 	            new UsernamePasswordCredentials(username, password)
 	        );
 	}
 	/**
 	 * @param server
 	 * @param port
 	 * @param prefix
 	 * @param owner
 	 * @param password
 	 * @return a JSONObject representing the jobDetail.  Example:<br>
 	 * 	{
 	 * 	  "id" : "refresh_pools_2adc6dee-790f-438f-95b5-567f14dcd67d",
 	 * 	  "state" : "FINISHED",
 	 * 	  "result" : "Pools refreshed for owner admin",
 	 * 	  "startTime" : "2010-08-30T20:01:11.724+0000",
 	 * 	  "finishTime" : "2010-08-30T20:01:11.800+0000",
 	 * 	  "statusPath" : "/jobs/refresh_pools_2adc6dee-790f-438f-95b5-567f14dcd67d",
 	 * 	  "updated" : "2010-08-30T20:01:11.932+0000",
 	 * 	  "created" : "2010-08-30T20:01:11.721+0000"
 	 * 	}
 	 * @throws Exception
 	 */
 	static public JSONObject refreshPoolsUsingRESTfulAPI(String server, String port, String prefix, String user, String password, String owner) throws Exception {
 //		PutMethod put = new PutMethod("https://"+server+":"+port+prefix+"/owners/"+owner+"/subscriptions");
 //		String response = getHTTPResponseAsString(client, put, owner, password);
 //				
 //		return new JSONObject(response);
 		return new JSONObject(putResourceUsingRESTfulAPI(server, port, prefix, user, password, "/owners/"+owner+"/subscriptions"));
 	}
 	
 	static public void exportConsumerUsingRESTfulAPI(String server, String port, String prefix, String owner, String password, String consumerKey, String intoExportZipFile) throws Exception {
 		log.info("Exporting the consumer '"+consumerKey+"' for owner '"+owner+"' on candlepin server '"+server+"'...");
 		log.info("SSH alternative to HTTP request: curl -k -u "+owner+":"+password+" https://"+server+":"+port+prefix+"/consumers/"+consumerKey+"/export > "+intoExportZipFile);
 		// CURL EXAMPLE: /usr/bin/curl -k -u admin:admin https://jsefler-f12-candlepin.usersys.redhat.com:8443/candlepin/consumers/0283ba29-1d48-40ab-941f-2d5d2d8b222d/export > /tmp/export.zip
 	
 		boolean validzip = false;
 		GetMethod get = new GetMethod("https://"+server+":"+port+prefix+"/consumers/"+consumerKey+"/export");
 		InputStream response = getHTTPResponseAsStream(client, get, owner, password);
 		File zipFile = new File(intoExportZipFile);
 		FileOutputStream fos = new FileOutputStream(zipFile);
 
 		try {
 			//ZipInputStream zip = new ZipInputStream(response);
 			//ZipEntry ze = zip.getNextEntry();
 			byte[] buffer = new byte[1024];
 			int len;
 			while ((len = response.read(buffer)) != -1) {
 			    fos.write(buffer, 0, len);
 			}
 			new ZipFile(zipFile);  //will throw exception if not valid zipfile
 			validzip = true;
 		}
 		catch(Exception e) {
 			log.log(Level.INFO, "Unable to read response as zip file.", e);
 		}
 		finally{
 			get.releaseConnection();
 			fos.flush();
 			fos.close();
 		}
 		
 		Assert.assertTrue(validzip, "Response is a valid zip file.");
 	}
 	
 	static public void importConsumerUsingRESTfulAPI(String server, String port, String prefix, String owner, String password, String ownerKey, String fromExportZipFile) throws Exception {
 		log.info("Importing consumer to owner '"+ownerKey+"' on candlepin server '"+server+"'...");
 		log.info("SSH alternative to HTTP request: curl -k -u "+owner+":"+password+" -F export=@"+fromExportZipFile+" https://"+server+":"+port+prefix+"/owners/"+ownerKey+"/import");
 		// CURL EXAMPLE: curl -u admin:admin -k -F export=@/tmp/export.zip https://jsefler-f12-candlepin.usersys.redhat.com:8443/candlepin/owners/dopey/import
 
 		PostMethod post = new PostMethod("https://"+server+":"+port+prefix+"/owners/"+ownerKey+"/import");
 		File f = new File(fromExportZipFile);
 		Part[] parts = {
 			      new FilePart(f.getName(), f)
 			  };
 		post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
 		int status = client.executeMethod(post);
 		
 		Assert.assertEquals(status, 204);
 	}
 	
 	public static JSONObject getOwnerOfConsumerId(String server, String port, String prefix, String authenticator, String authenticatorPassword, String consumerId) throws JSONException, Exception {
 		// determine this consumerId's owner
 		JSONObject jsonOwner = null;
 		JSONObject jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(server, port, prefix, authenticator, authenticatorPassword,"/consumers/"+consumerId));	
 		JSONObject jsonOwner_ = (JSONObject) jsonConsumer.getJSONObject("owner");
 		jsonOwner = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(server, port, prefix, authenticator, authenticatorPassword,jsonOwner_.getString("href")));	
 
 		return jsonOwner;
 	}
 	
 	public static void dropAllConsumers(final String server, final String port, final String prefix, final String owner, final String password) throws Exception{
 		JSONArray consumers = new JSONArray(getResourceUsingRESTfulAPI(server, port, prefix, owner, password, "consumers"));
 		List<String> refs = new ArrayList<String>();
 		for (int i=0;i<consumers.length();i++) {
 			JSONObject o = consumers.getJSONObject(i);
 			refs.add(o.getString("href"));
 		}
 		final ExecutorService service = Executors.newFixedThreadPool(4);  //run 4 concurrent deletes
 		for (final String consumer: refs) {
 			service.submit(new Runnable() {
 				public void run() {
 					try {
 						HttpMethod m = new DeleteMethod("https://"+server+":"+port+prefix + consumer);
 						doHTTPRequest(client, m, owner, password);
 						m.releaseConnection();
 					}catch (Exception e) {
 						log.log(Level.SEVERE, "Could not delete consumer: " + consumer, e);
 					}
 				}
 			});
 		}
 		
 		service.shutdown();
 		service.awaitTermination(6, TimeUnit.HOURS);
 	}
 	
 	
 	/**
 	 * @param server
 	 * @param port
 	 * @param prefix
 	 * @param owner
 	 * @param password
 	 * @param jobDetail - JSONObject of a jobDetail. Example:<br>
 	 * 	{
 	 * 	  "id" : "refresh_pools_2adc6dee-790f-438f-95b5-567f14dcd67d",
 	 * 	  "state" : "RUNNING",
 	 * 	  "result" : "Pools refreshed for owner admin",
 	 * 	  "startTime" : "2010-08-30T20:01:11.724+0000",
 	 * 	  "finishTime" : "2010-08-30T20:01:11.800+0000",
 	 * 	  "statusPath" : "/jobs/refresh_pools_2adc6dee-790f-438f-95b5-567f14dcd67d",
 	 * 	  "updated" : "2010-08-30T20:01:11.932+0000",
 	 * 	  "created" : "2010-08-30T20:01:11.721+0000"
 	 * 	}
 	 * @param state - valid states: "PENDING", "CREATED", "RUNNING", "FINISHED"
 	 * @param retryMilliseconds - sleep time between attempts to get latest JobDetail
 	 * @param timeoutMinutes - give up waiting
 	 * @return
 	 * @throws Exception
 	 */
 	static public JSONObject waitForJobDetailStateUsingRESTfulAPI(String server, String port, String prefix, String owner, String password, JSONObject jobDetail, String state, int retryMilliseconds, int timeoutMinutes) throws Exception {
 		String statusPath = jobDetail.getString("statusPath");
 		int t = 0;
 		
 		// pause for the sleep interval; get the updated job detail; while the job detail's state has not yet changed
 		do {
 			// pause for the sleep interval
 			SubscriptionManagerCLITestScript.sleep(retryMilliseconds); t++;	
 			
 			// get the updated job detail
 			jobDetail = new JSONObject(getResourceUsingRESTfulAPI(server,port,prefix,owner,password,statusPath));
 		} while (!jobDetail.getString("state").equalsIgnoreCase(state) || (t*retryMilliseconds >= timeoutMinutes*60*1000));
 		
 		// assert that the state was achieved within the timeout
 		Assert.assertFalse((t*retryMilliseconds >= timeoutMinutes*60*1000), "JobDetail '"+jobDetail.getString("id")+"' changed state to '"+state+"' within '"+t*retryMilliseconds+"' milliseconds (timeout="+timeoutMinutes+" min)");
 
 		return jobDetail;
 // TODO
 //		public void getJobDetail(String id) {
 //			// /usr/bin/curl -u admin:admin -k --header 'Content-type: application/json' --header 'Accept: application/json' --request GET https://jsefler-f12-candlepin.usersys.redhat.com:8443/candlepin/jobs/refresh_pools_2adc6dee-790f-438f-95b5-567f14dcd67d
 //			
 //			{
 //				  "id" : "refresh_pools_2adc6dee-790f-438f-95b5-567f14dcd67d",
 //				  "state" : "FINISHED",
 //				  "result" : "Pools refreshed for owner admin",
 //				  "startTime" : "2010-08-30T20:01:11.724+0000",
 //				  "finishTime" : "2010-08-30T20:01:11.800+0000",
 //				  "statusPath" : "/jobs/refresh_pools_2adc6dee-790f-438f-95b5-567f14dcd67d",
 //				  "updated" : "2010-08-30T20:01:11.932+0000",
 //				  "created" : "2010-08-30T20:01:11.721+0000"
 //				}
 //		}
 
 	}
 	
 	public void restartTomcat() {
 		RemoteFileTasks.runCommandAndAssert(sshCommandRunner,"service tomcat6 restart",Integer.valueOf(0),"^Starting tomcat6: +\\[  OK  \\]$",null);
 	}
 	
 	public List<RevokedCert> getCurrentlyRevokedCerts() {
 		sshCommandRunner.runCommandAndWait("openssl crl -noout -text -in "+candlepinCRLFile);
 		String crls = sshCommandRunner.getStdout();
 		return RevokedCert.parse(crls);
 	}
 
 // DELETEME
 //	/**
 //	 * @param fieldName
 //	 * @param fieldValue
 //	 * @param revokedCerts - usually getCurrentlyRevokedCerts()
 //	 * @return - the RevokedCert from revokedCerts that has a matching field (if not found, null is returned)
 //	 */
 //	public RevokedCert findRevokedCertWithMatchingFieldFromList(String fieldName, Object fieldValue, List<RevokedCert> revokedCerts) {
 //		
 //		RevokedCert revokedCertWithMatchingField = null;
 //		for (RevokedCert revokedCert : revokedCerts) {
 //			try {
 //				if (RevokedCert.class.getField(fieldName).get(revokedCert).equals(fieldValue)) {
 //					revokedCertWithMatchingField = revokedCert;
 //				}
 //			} catch (IllegalArgumentException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (SecurityException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (IllegalAccessException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			} catch (NoSuchFieldException e) {
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //			}
 //		}
 //		return revokedCertWithMatchingField;
 //	}
 	
 	
 	public JSONObject createOwnerUsingCPC(String owner_name) throws JSONException {
 		log.info("Using the ruby client to create_owner owner_name='"+owner_name+"'...");
 
 		// call the ruby client
 		String command = String.format("cd %s; ./cpc create_owner \"%s\"", serverInstallDir+rubyClientDir, owner_name);
 		SSHCommandResult sshCommandResult = RemoteFileTasks.runCommandAndAssert(sshCommandRunner, command, 0);
 
 		return new JSONObject(sshCommandResult.getStdout().replaceAll("=>", ":"));
 		
 		// REMINDER: DateFormat used in JSON objects is...
 		// protected static String simpleDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";	// "2010-09-01T15:45:12.068+0000"
 
 	}
 	
 	static public JSONObject createOwnerUsingRESTfulAPI(String server, String port, String prefix, String owner, String password, String owner_name) throws Exception {
 // NOT TESTED
 		return new JSONObject(postResourceUsingRESTfulAPI(server, port, prefix, owner, password, "/owners", owner_name));
 	}
 	
 	public SSHCommandResult deleteOwnerUsingCPC(String owner_name) {
 		log.info("Using the ruby client to delete_owner owner_name='"+owner_name+"'...");
 
 		// call the ruby client
 		String command = String.format("cd %s; ./cpc delete_owner \"%s\"", serverInstallDir+rubyClientDir, owner_name);
 		return RemoteFileTasks.runCommandAndAssert(sshCommandRunner, command, 0);
 	}
 	
 	public JSONObject createProductUsingCPC(String id, String name) throws JSONException {
 		log.info("Using the ruby client to create_product id='"+id+"' name='"+name+"'...");
 
 		// call the ruby client
 		String command = String.format("cd %s; ./cpc create_product \"%s\" \"%s\"", serverInstallDir+rubyClientDir, id, name);
 		SSHCommandResult sshCommandResult = RemoteFileTasks.runCommandAndAssert(sshCommandRunner, command, 0);
 		
 		return new JSONObject(sshCommandResult.getStdout().replaceAll("=>", ":"));
 	}
 
 	public JSONObject createPoolUsingCPC(String productId, String ownerId, String quantity) throws JSONException {
 		log.info("Using the ruby client to create_pool productId='"+productId+"' ownerId='"+ownerId+"' quantity='"+quantity+"'...");
 
 		// call the ruby client
 		String command = String.format("cd %s; ./cpc create_pool \"%s\" \"%s\" \"%s\"", serverInstallDir+rubyClientDir, productId, ownerId, quantity);
 		SSHCommandResult sshCommandResult = RemoteFileTasks.runCommandAndAssert(sshCommandRunner, command, 0);
 		
 		return new JSONObject(sshCommandResult.getStdout().replaceAll("=>", ":"));
 	}
 	
 	public SSHCommandResult deletePoolUsingCPC(String id) {
 		log.info("Using the ruby client to delete_pool id='"+id+"'...");
 
 		// call the ruby client
 		String command = String.format("cd %s; ./cpc delete_pool \"%s\"", serverInstallDir+rubyClientDir, id);
 		return RemoteFileTasks.runCommandAndAssert(sshCommandRunner, command, 0);
 	}
 	
 	public JSONObject refreshPoolsUsingCPC(String ownerKey, boolean immediate) throws JSONException {
 		log.info("Using the ruby client to refresh_pools ownerKey='"+ownerKey+"' immediate='"+immediate+"'...");
 
 		// call the ruby client
 		String command = String.format("cd %s; ./cpc refresh_pools \"%s\" %s", serverInstallDir+rubyClientDir, ownerKey, Boolean.toString(immediate));
 		SSHCommandResult sshCommandResult = RemoteFileTasks.runCommandAndAssert(sshCommandRunner, command, 0);
 		
 		return new JSONObject(sshCommandResult.getStdout().replaceAll("=>", ":"));
 	}
 	
 	public static SyndFeed getSyndFeedForOwner(String key, String candlepinHostname, String candlepinPort, String candlepinPrefix, String candlepinUsername, String candlepinPassword) throws IllegalArgumentException, IOException, FeedException {
 		return getSyndFeedFor("owners",key,candlepinHostname,candlepinPort,candlepinPrefix,candlepinUsername,candlepinPassword);
 	}
 	
 	public static SyndFeed getSyndFeedForConsumer(String key, String candlepinHostname, String candlepinPort, String candlepinPrefix, String candlepinUsername, String candlepinPassword) throws IllegalArgumentException, IOException, FeedException {
 		return getSyndFeedFor("consumers",key,candlepinHostname,candlepinPort,candlepinPrefix,candlepinUsername,candlepinPassword);
 	}
 	
 	public static SyndFeed getSyndFeed(String candlepinHostname, String candlepinPort, String candlepinPrefix, String candlepinUsername, String candlepinPassword) throws IllegalArgumentException, IOException, FeedException {
 		return getSyndFeedFor(null,null,candlepinHostname,candlepinPort,candlepinPrefix,candlepinUsername,candlepinPassword);
 	}
 	
 	protected static SyndFeed getSyndFeedFor(String ownerORconsumer, String key, String candlepinHostname, String candlepinPort, String candlepinPrefix, String candlepinUsername, String candlepinPassword) throws IOException, IllegalArgumentException, FeedException {
 			
 		/* References:
 		 * http://www.exampledepot.com/egs/javax.net.ssl/TrustAll.html
 		 * http://www.avajava.com/tutorials/lessons/how-do-i-connect-to-a-url-using-basic-authentication.html
 		 * http://wiki.java.net/bin/view/Javawsxml/Rome
 		 */
 			
 		// Notes: Alternative curl approach to getting the atom feed:
 		// [ajay@garuda-rh proxy{pool_refresh}]$ curl -k -u admin:admin --request GET "https://localhost:8443/candlepin/owners/admin/atom" > /tmp/atom.xml; xmllint --format /tmp/atom.xml > /tmp/atom1.xml
 		// from https://bugzilla.redhat.com/show_bug.cgi?id=645597
 		
 		SSLCertificateTruster.trustAllCerts();
 		
 		// set the atom feed url for an owner, consumer, or null
 		String url = String.format("https://%s:%s%s/atom", candlepinHostname, candlepinPort, candlepinPrefix);
 		if (ownerORconsumer!=null && key!=null) {
 			url = String.format("https://%s:%s%s/%s/%s/atom", candlepinHostname, candlepinPort, candlepinPrefix, ownerORconsumer, key);
 		}
 		
         log.fine("SyndFeedUrl: "+url);
         String authString = candlepinUsername+":"+candlepinPassword;
         log.finer("SyndFeedAuthenticationString: "+authString);
  		byte[] authEncBytes = Base64.encodeBytesToBytes(authString.getBytes());
  		String authStringEnc = new String(authEncBytes);
  		log.finer("SyndFeed Base64 encoded SyndFeedAuthenticationString: "+authStringEnc);
 
  		SyndFeed feed = null;
         URL feedUrl=null;
         URLConnection urlConnection=null;
 //		try {
 			feedUrl = new URL(url);
 			urlConnection = feedUrl.openConnection();
             urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
             SyndFeedInput input = new SyndFeedInput();
             XmlReader xmlReader = new XmlReader(urlConnection);
 			feed = input.build(xmlReader);
 
 //		} catch (MalformedURLException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		} catch (IOException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (IllegalArgumentException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (FeedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 			
 		// debug logging
 		log.finest("SyndFeed from "+feedUrl+":\n"+feed);
 //log.fine("SyndFeed from "+feedUrl+":\n"+feed);
 		for (int i=0;  i<feed.getEntries().size(); i++) {
 			log.fine(String.format("%s entries[%d].title=%s   description=%s", feed.getTitle(), i, ((SyndEntryImpl) feed.getEntries().get(i)).getTitle(), ((SyndEntryImpl) feed.getEntries().get(i)).getDescription()==null?"null":((SyndEntryImpl) feed.getEntries().get(i)).getDescription().getValue()));
 		}
 
         return feed;
 	}
 		
 	public static JSONObject createPoolRequest(Integer quantity, Date startDate, Date endDate, String product, Integer contractNumber, String... providedProducts) throws JSONException{
 		JSONObject sub = new JSONObject();
 		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
 		sub.put("startDate", sdf.format(startDate));
 		sub.put("contractNumber", contractNumber);
 		sub.put("endDate", sdf.format(endDate));
 		sub.put("quantity", quantity);
 
 		List<JSONObject> pprods = new ArrayList<JSONObject>();
 		for (String id: providedProducts) {
 			JSONObject jo = new JSONObject();
 			jo.put("id", id);
 			pprods.add(jo);
 		}
 		sub.put("providedProducts", pprods);
 
 		JSONObject prod = new JSONObject();
 		prod.put("id", product);
 		
 		sub.put("product", prod);
 
 		return sub;
 	}
 	
 	public String invalidCredentialsRegexMsg() {
 		return isOnPremises? "^Invalid Credentials$":"Invalid username or password. To create a login, please visit https://www.redhat.com/wapps/ugc/register.html";
 	}
 	
 	public static void main (String... args) throws Exception {
 		
 
 		//System.out.println(CandlepinTasks.getResourceREST("candlepin1.devlab.phx1.redhat.com", "443", "xeops", "redhat", ""));
 		//CandlepinTasks.dropAllConsumers("localhost", "8443", "admin", "admin");
 		//CandlepinTasks.dropAllConsumers("candlepin1.devlab.phx1.redhat.com", "443", "xeops", "redhat");
 		//CandlepinTasks.exportConsumerUsingRESTfulAPI("jweiss.usersys.redhat.com", "8443", "/candlepin", "admin", "admin", "78cf3c59-24ec-4228-a039-1b554ea21319", "/tmp/myfile.zip");
 		Calendar cal = new GregorianCalendar();
 		cal.add(Calendar.DATE, -1);
 		Date yday = cal.getTime();
 		cal.add(Calendar.DATE, 2);
 		Date trow = cal.getTime();
 		
 		
 		//sub.put("quantity", 5);
 		
 		
 		JSONArray ja = new JSONArray(Arrays.asList(new String[] {"blah" }));
 		
 		//jo.put("john", ja);
 		//System.out.println(jo.toString());
 	}
 }
