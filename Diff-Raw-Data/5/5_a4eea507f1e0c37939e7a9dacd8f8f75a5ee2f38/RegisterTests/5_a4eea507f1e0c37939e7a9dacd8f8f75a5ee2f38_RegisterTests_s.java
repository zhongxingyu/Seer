 package rhsm.cli.tests;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BlockedByBzBug;
 import com.redhat.qe.auto.bugzilla.BzChecker;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.jul.TestRecords;
 import rhsm.base.ConsumerType;
 import rhsm.base.SubscriptionManagerCLITestScript;
 import rhsm.cli.tasks.CandlepinTasks;
 import rhsm.data.ConsumerCert;
 import rhsm.data.EntitlementCert;
 import rhsm.data.InstalledProduct;
 import rhsm.data.ProductCert;
 import rhsm.data.ProductSubscription;
 import rhsm.data.Repo;
 import rhsm.data.SubscriptionPool;
 import rhsm.data.YumRepo;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 @Test(groups={"RegisterTests"})
 public class RegisterTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	
 	
 	@BeforeGroups(value={"RegisterWithCredentials_Test"},alwaysRun=true)
 	public void beforeRegisterWithCredentials_Test() {
 		if (clienttasks==null) return;
 		clienttasks.unregister_(null, null, null);
 	}
 	@Test(	description="subscription-manager-cli: register to a Candlepin server",
 			groups={"RegisterWithCredentials_Test", "AcceptanceTests"},
 			dataProvider="getRegisterCredentialsData")
 	@ImplementsNitrateTest(caseId=41677)
 	public void RegisterWithCredentials_Test(String username, String password, String org) {
 		log.info("Testing registration to a Candlepin using username="+username+" password="+password+" org="+org+" ...");
 		
 		// cleanup from last test when needed
 		clienttasks.unregister_(null, null, null);
 		
 		// determine this user's ability to register
 		SSHCommandResult registerResult = clienttasks.register_(username, password, org, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 			
 		// determine this user's available subscriptions
 		List<SubscriptionPool> allAvailableSubscriptionPools=null;
 		if (registerResult.getExitCode()==0) {
 			allAvailableSubscriptionPools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		}
 		
 		// determine this user's owner
 		String ownerKey = null;
 		if (registerResult.getExitCode()==0) {
 			String consumerId = clienttasks.getCurrentConsumerId(registerResult);	// c48dc3dc-be1d-4b8d-8814-e594017d63c1 testuser1
 			try {
 				ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(username,password,sm_serverUrl,consumerId);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		RegistrationData userData = new RegistrationData(username,password,ownerKey,registerResult,allAvailableSubscriptionPools);
 		registrationDataList.add(userData);
 		clienttasks.unregister_(null, null, null);
 		
 		// when no org was given by the dataprovider, then this user must have READ_ONLY access to any one or more orgs
 		if (org==null) {
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=718205 - jsefler 07/01/2011
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			String bugId="718205"; 
 			try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				// When org==null, then this user has no access to any org/owner
 				// 1. the user has only READ_ONLY access to one org:
 				//		exitCode=1 stdout= User dopey cannot access organization/owner snowwhite
 				// 2. the user has only READ_ONLY access to more than one org:
 				//		exitCode=1 stdout= You must specify an organization/owner for new consumers.
 				// Once a Candlepin API is in place to figure this out, fix the OR in the Assert.assertContainsMatch(...)
 				Assert.assertContainsMatch(registerResult.getStderr().trim(), "User "+username+" cannot access organization/owner \\w+|You must specify an organization/owner for new consumers.");	// User testuser3 cannot access organization/owner admin	// You must specify an organization/owner for new consumers.
 				Assert.assertFalse(registerResult.getExitCode()==0, "The exit code indicates that the register attempt was NOT a success for a READ_ONLY user.");
 				return;
 			}
 			// END OF WORKAROUND
 			
 			Assert.assertEquals(registerResult.getStderr().trim(), username+" cannot register to any organizations.", "Error message when READ_ONLY user attempts to register.");
 			Assert.assertEquals(registerResult.getExitCode(), Integer.valueOf(255), "The exit code indicates that the register attempt was NOT a success for a READ_ONLY user.");
 			return;
 		}
 		Assert.assertEquals(registerResult.getExitCode(), Integer.valueOf(0), "The exit code indicates that the register attempt was a success.");
 		//Assert.assertContainsMatch(registerResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+/*username*/clienttasks.hostname);	// applicable to RHEL61 and RHEL57 
 		Assert.assertContainsMatch(registerResult.getStdout().trim(), "The system has been registered with id: [a-f,0-9,\\-]{36}");
 	}
 	@AfterGroups(value={"RegisterWithCredentials_Test"},alwaysRun=true)
 	public void generateRegistrationReportTableAfterRegisterWithCredentials_Test() {
 		
 		// now dump out the list of userData to a file
 	    File file = new File("CandlepinRegistrationReport.html"); // this will be in the automation.dir directory on hudson (workspace/automatjon/sm)
 	    DateFormat dateFormat = new SimpleDateFormat("MMM d HH:mm:ss yyyy z");
 	    try {
 	    	Writer output = new BufferedWriter(new FileWriter(file));
 			
 			// write out the rows of the table
 			output.write("<html>\n");
 			output.write("<table border=1>\n");
 			output.write("<h2>Candlepin Registration Report</h2>\n");
 			//output.write("<h3>(generated on "+dateFormat.format(System.currentTimeMillis())+")</h3>");
 			output.write("Candlepin hostname= <b>"+sm_serverHostname+"</b><br>\n");
 			output.write(dateFormat.format(System.currentTimeMillis())+"\n");
 			output.write("<tr><th>Username/<BR>Password</th><th>OrgKey</th><th>Register Result</th><th>All Available Subscriptions<BR>(to system consumers)</th></tr>\n");
 			for (RegistrationData registeredConsumer : registrationDataList) {
 				if (registeredConsumer.ownerKey==null) {
 					output.write("<tr bgcolor=#F47777>");
 				} else {output.write("<tr>");}
 				if (registeredConsumer.username!=null) {
 					output.write("<td valign=top>"+registeredConsumer.username+"/<BR>"+registeredConsumer.password+"</td>");
 				} else {output.write("<td/>");};
 				if (registeredConsumer.ownerKey!=null) {
 					output.write("<td valign=top>"+registeredConsumer.ownerKey+"</td>");
 				} else {output.write("<td/>");};
 				if (registeredConsumer.registerResult!=null) {
 					output.write("<td valign=top>"+registeredConsumer.registerResult.getStdout()+registeredConsumer.registerResult.getStderr()+"</td>");
 				} else {output.write("<td/>");};
 				if (registeredConsumer.allAvailableSubscriptionPools!=null) {
 					output.write("<td valign=top><ul>");
 					for (SubscriptionPool availableSubscriptionPool : registeredConsumer.allAvailableSubscriptionPools) {
 						output.write("<li>"+availableSubscriptionPool+"</li>");
 					}
 					output.write("</ul></td>");
 				} else {output.write("<td/>");};
 				output.write("</tr>\n");
 			}
 			output.write("</table>\n");
 			output.write("</html>\n");
 		    output.close();
 		    //log.info(file.getCanonicalPath()+" exists="+file.exists()+" writable="+file.canWrite());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using bogus credentials",
 			groups={},
 			dataProvider="getAttemptRegistrationWithInvalidCredentials_Test")
 //	@ImplementsNitrateTest(caseId={41691, 47918})
 	@ImplementsNitrateTest(caseId=47918)
 	public void AttemptRegistrationWithInvalidCredentials_Test(Object meta, String username, String password, String owner, ConsumerType type, String name, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		log.info("Testing registration to a Candlepin using various options and data and asserting various expected results.");
 		
 		// ensure we are unregistered
 //DO NOT		clienttasks.unregister();
 		
 		// attempt the registration
 		SSHCommandResult sshCommandResult = clienttasks.register_(username, password, owner, null, type, name, consumerId, autosubscribe, null, null, (String)null, null, null, force, null, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode);
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex);
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex);
 	}
 	@DataProvider(name="getAttemptRegistrationWithInvalidCredentials_Test")
 	public Object[][] getAttemptRegistrationWithInvalidCredentials_TestDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getAttemptRegistrationWithInvalidCredentials_TestDataAsListOfLists());
 	}
 	protected List<List<Object>> getAttemptRegistrationWithInvalidCredentials_TestDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 		String randomString = String.valueOf(getRandInt());
 
 		// Object bugzilla, String username, String password, String owner, String type, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer exitCode, String stdoutRegex, String stderrRegex
 		ll.add(Arrays.asList(new Object[] {null,							sm_clientUsername,					String.valueOf(getRandInt()),	null,							null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {null,							sm_clientUsername+getRandInt(),		sm_clientPassword,				null,							null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {null,							sm_clientUsername+getRandInt(),		String.valueOf(getRandInt()),	null,							null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,		uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {null,							sm_clientUsername,					sm_clientPassword,				null,							null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,		/*"You must specify an organization/owner for new consumers."*/"You must specify an organization for new consumers."}));
 		ll.add(Arrays.asList(new Object[] {null,							sm_clientUsername,					sm_clientPassword,				randomString,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,		/*"Organization/Owner "+randomString+" does not exist."*/"Organization "+randomString+" does not exist."}));
 		ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("734114"),	sm_clientUsername,					sm_clientPassword,				"\"foo bar\"",					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,		/*"Organization/Owner "+"foo bar"+" does not exist."*/"Organization "+"foo bar"+" does not exist."}));
 
 		// force a successful registration, and then...
 		ll.add(Arrays.asList(new Object[]{	new BlockedByBzBug(new String[]{"616065","669395"}),
 													sm_clientUsername,		sm_clientPassword,					sm_clientOrg,	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"The system has been registered with id: [a-f,0-9,\\-]{36}",					null}));
 
 		// ... try to register again even though the system is already registered
 		ll.add(Arrays.asList(new Object[] {null,	sm_clientUsername,		sm_clientPassword,					null,			null,	null,	null,		null,			Boolean.FALSE,	null,	Integer.valueOf(1),		"This system is already registered. Use --force to override",					null}));
 
 		return ll;
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register a user who has unaccepted Terms and Conditions",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=48502)
 	public void AttemptRegistrationWithUnacceptedTermsAndConditions_Test() {
 		String username = sm_usernameWithUnacceptedTC;
 		String password = sm_passwordWithUnacceptedTC;
 		if (username.equals("")) throw new SkipException("Must specify a username who has not accepted Terms & Conditions before attempting this test.");
 
 		// ensure we are unregistered
 		clienttasks.unregister(null, null, null);
 		
 		log.info("Attempting to register to a candlepin server using invalid credentials");
 		String stderrRegex = "You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc";
 		String command = String.format("%s register --username=%s --password=%s", clienttasks.command, username, password);
 		RemoteFileTasks.runCommandAndAssert(client, command, new Integer(255), null, stderrRegex);
 		
 		// assert that a consumer cert and key have NOT been installed
 		Assert.assertTrue(!RemoteFileTasks.testExists(client,clienttasks.consumerKeyFile()), "Consumer key file '"+clienttasks.consumerKeyFile()+"' does NOT exist after an attempt to register with invalid credentials.");
 		Assert.assertTrue(!RemoteFileTasks.testExists(client,clienttasks.consumerCertFile()), "Consumer cert file '"+clienttasks.consumerCertFile()+" does NOT exist after an attempt to register with invalid credentials.");
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register a user who has been disabled",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=50210)
 	public void AttemptRegistrationWithDisabledUserCredentials_Test() {
 		String username = sm_disabledUsername;
 		String password = sm_disabledPassword;
 		if (username.equals("")) throw new SkipException("Must specify a username who has been disabled before attempting this test.");
 		
 		// ensure we are unregistered
 		clienttasks.unregister(null, null, null);
 		
 		log.info("Attempting to register to a candlepin server using disabled credentials");
 		String stderrRegex = "The user has been disabled, if this is a mistake, please contact customer service.";
 		String command = String.format("%s register --username=%s --password=%s", clienttasks.command, username, password);
 		RemoteFileTasks.runCommandAndAssert(client, command, new Integer(255), null, stderrRegex);
 		
 		// assert that a consumer cert and key have NOT been installed
 		Assert.assertTrue(!RemoteFileTasks.testExists(client,clienttasks.consumerKeyFile()), "Consumer key file '"+clienttasks.consumerKeyFile()+"' does NOT exist after an attempt to register with disabled credentials.");
 		Assert.assertTrue(!RemoteFileTasks.testExists(client,clienttasks.consumerCertFile()), "Consumer cert file '"+clienttasks.consumerCertFile()+" does NOT exist after an attempt to register with disabled credentials.");
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 			groups={"RegisterWithAutosubscribe_Test","blockedByBug-602378", "blockedByBug-616137", "blockedByBug-678049", "blockedByBug-737762", "blockedByBug-743082", "AcceptanceTests"},
 			enabled=false)	// the strategy for this test has been improved in the new implementation of RegisterWithAutosubscribe_Test() 
 	@Deprecated
 	public void RegisterWithAutosubscribe_Test_DEPRECATED() throws JSONException, Exception {
 
 		log.info("RegisterWithAutosubscribe_Test Strategy:");
 		log.info(" For DEV and QA testing purposes, we may not have valid products installed on the client, therefore we will fake an installed product by following this strategy:");
 		log.info(" 1. Change the rhsm.conf configuration for productCertDir to point to a new temporary product cert directory.");
 		log.info(" 2. Register with autosubscribe and assert that no product binding has occurred.");
 		log.info(" 3. Subscribe to a randomly available pool");
 		log.info(" 4. Copy the downloaded entitlement cert to the temporary product cert directory.");
 		log.info("    (this will fake rhsm into believing that the same product is installed)");
 		log.info(" 5. Reregister with autosubscribe and assert that a product has been bound.");
 
 		// create a clean temporary productCertDir and change the rhsm.conf to point to it
 		RemoteFileTasks.runCommandAndAssert(client, "rm -rf "+tmpProductCertDir, Integer.valueOf(0)); // incase something was leftover from a prior run
 		RemoteFileTasks.runCommandAndAssert(client, "mkdir "+tmpProductCertDir, Integer.valueOf(0));
 		this.productCertDir = clienttasks.productCertDir;	// store the original productCertDir
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", tmpProductCertDir);
 
 		// Register and assert that no products appear to be installed since we changed the productCertDir to a temporary directory
 		clienttasks.unregister(null, null, null);
 		SSHCommandResult sshCommandResult = clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, Boolean.TRUE, null, null, (String)null, null, null, null, false, null, null, null);
 
 		//[root@jsefler-r63-server ~]# subscription-manager register --username testuser1 --password password --auto --org admin
 		//The system has been registered with id: 243ea73d-01bb-458d-a7a5-2d61fde69494 
 		//Installed Product Current Status:
 		//ProductName:          	Awesome OS for S390 Bits 
 		//Status:               	Not Subscribed           
 		//
 		//ProductName:          	Stackable with Awesome OS for x86_64 Bits
 		//Status:               	Subscribed   
 		
 		// pre-fix for blockedByBug-678049 Assert.assertContainsNoMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "register with autosubscribe should NOT appear to have subscribed to something when there are no installed products.");
 		Assert.assertTrue(InstalledProduct.parse(sshCommandResult.getStdout()).isEmpty(),
 				"The Installed Product Current Status should be empty when attempting to register with autosubscribe without any product certs installed.");
 		Assert.assertEquals(clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null, null, null).getStdout().trim(),"No installed products to list",
 				"Since we changed the productCertDir configuration to an empty location, we should not appear to have any products installed.");
 		//List <InstalledProduct> currentlyInstalledProducts = InstalledProduct.parse(clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null).getStdout());
 		//for (String status : new String[]{"Not Subscribed","Subscribed"}) {
 		//	Assert.assertNull(InstalledProduct.findFirstInstanceWithMatchingFieldFromList("status", status, currentlyInstalledProducts),
 		//			"When no product certs are installed, then we should not be able to find a installed product with status '"+status+"'.");
 		//}
 
 		// subscribe to a randomly available pool
 		/* This is too random
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		*/
 		// subscribe to the first available pool that provides one product
 		File entitlementCertFile = null;
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+pool.poolId));	
 			JSONArray jsonProvidedProducts = jsonPool.getJSONArray("providedProducts");
 			if (jsonProvidedProducts.length()==1) {
 				entitlementCertFile = clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 				break;
 			}
 		}
 		if (entitlementCertFile==null) throw new SkipException("Could not find an available pool that provides only one product with which to test register with --autosubscribe.");
 		
 		// copy the downloaded entitlement cert to the temporary product cert directory (this will fake rhsm into believing that the same product is installed)
 		RemoteFileTasks.runCommandAndAssert(client, "cp "+entitlementCertFile.getPath()+" "+tmpProductCertDir, Integer.valueOf(0));
 		File tmpProductCertFile = new File(tmpProductCertDir+File.separator+entitlementCertFile.getName());
 		ProductCert fakeProductCert = clienttasks.getProductCertFromProductCertFile(tmpProductCertFile);
 		
 		// reregister with autosubscribe and assert that the product is bound
 		clienttasks.unregister(null, null, null);
 		sshCommandResult = clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, Boolean.TRUE, null, null, (String)null, null, null, null, false, null, null, null);
 		
 		// assert that the sshCommandResult from register indicates the fakeProductCert was subscribed
 		/* # subscription-manager register --username=testuser1 --password=password
 		d67df9c8-f381-4449-9d17-56094ea58092 testuser1
 		Subscribed to Products:
 		     RHEL for Physical Servers SVC(37060)
 		     Red Hat Enterprise Linux High Availability (for RHEL Entitlement)(4)
 		*/
 		
 		/* # subscription-manager register --username=testuser1 --password=password
 		cadf825a-6695-41e3-b9eb-13d7344159d3 jsefler-onprem03.usersys.redhat.com
 		Installed Products:
 		    Clustering Bits - Subscribed
 		    Awesome OS Server Bits - Not Installed
 		*/
 		
 		/* # subscription-manager register --username=testuser1 --password=password --org=admin --autosubscribe
 		The system has been registered with id: f95fd9bb-4cc8-428e-b3fd-d656b14bfb89 
 		Installed Product Current Status:
 
 		ProductName:         	Awesome OS for S390X Bits
 		Status:               	Subscribed  
 		*/
 
 		// assert that our fake product install appears to have been autosubscribed
 		InstalledProduct autoSubscribedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("status", "Subscribed", InstalledProduct.parse(clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null, null, null).getStdout()));
 		Assert.assertNotNull(autoSubscribedProduct,	"We appear to have autosubscribed to our fake product install.");
 		// pre-fix for blockedByBug-678049 Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "The stdout from register with autotosubscribe indicates that we have subscribed to something");
 		// pre-fix for blockedByBug-678049 Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^\\s+"+autoSubscribedProduct.productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"), "Expected ProductName '"+autoSubscribedProduct.productName+"' was reported as autosubscribed in the output from register with autotosubscribe.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), ".* - Subscribed", "The stdout from register with autotosubscribe indicates that we have automatically subscribed at least one of this system's installed products to an available subscription pool.");
 		List<InstalledProduct> autosubscribedProductStatusList = InstalledProduct.parse(sshCommandResult.getStdout());
 		Assert.assertEquals(autosubscribedProductStatusList.size(), 1, "Only one product was autosubscribed."); 
 		Assert.assertEquals(autosubscribedProductStatusList.get(0),new InstalledProduct(fakeProductCert.productName,null,null,null,"Subscribed",null,null),
 				"As expected, ProductName '"+fakeProductCert.productName+"' was reported as subscribed in the output from register with autotosubscribe.");
 
 		// WARNING The following two asserts lead to misleading failures when the entitlementCertFile that we using to fake as a tmpProductCertFile happens to have multiple bundled products inside.  This is why we search for an available pool that provides one product early in this test.
 		//Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^\\s+"+autoSubscribedProduct.productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")+" - Subscribed", "Expected ProductName '"+autoSubscribedProduct.productName+"' was reported as autosubscribed in the output from register with autotosubscribe.");
 		//Assert.assertNotNull(ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName", autoSubscribedProduct.productName, clienttasks.getCurrentlyConsumedProductSubscriptions()),"Expected ProductSubscription with ProductName '"+autoSubscribedProduct.productName+"' is consumed after registering with autosubscribe.");
 	}
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 			groups={"RegisterWithAutosubscribe_Test","blockedByBug-602378", "blockedByBug-616137", "blockedByBug-678049", "blockedByBug-737762", "blockedByBug-743082", "AcceptanceTests"},
 			enabled=false)
 	@Deprecated
 	public void RegisterWithAutosubscribe_Test_DEPRECATED_2() throws JSONException, Exception {
 
 		log.info("RegisterWithAutosubscribe_Test Strategy:");
 		log.info(" 1. Change the rhsm.conf configuration for productCertDir to point to a new temporary product cert directory.");
 		log.info(" 2. Register with autosubscribe and assert that no product binding has occurred.");
 		log.info(" 3. Using the candlepin REST API, we will find an available pool that provides a product that we have installed.");
 		log.info(" 4. Copy the installed product to a temporary product cert directory so that we can isolate the expected product that will be autosubscribed.");
 		log.info(" 5. Reregister with autosubscribe and assert that the temporary product has been bound.");
 
 		// get the product certs that are currently installed
 		List<ProductCert> installedProductCerts = clienttasks.getCurrentProductCerts();
 		
 		// create a clean temporary productCertDir and change the rhsm.conf to point to it
 		RemoteFileTasks.runCommandAndAssert(client, "rm -rf "+tmpProductCertDir, Integer.valueOf(0)); // incase something was leftover from a prior run
 		RemoteFileTasks.runCommandAndAssert(client, "mkdir "+tmpProductCertDir, Integer.valueOf(0));
 		this.productCertDir = clienttasks.productCertDir;	// store the original productCertDir
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", tmpProductCertDir);
 
 		// Register and assert that no products appear to be installed since we changed the productCertDir to a temporary directory
 		SSHCommandResult sshCommandResult = clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, Boolean.TRUE, null, null, (String)null, null, null, true, false, null, null, null);
 
 		//[root@jsefler-r63-server ~]# subscription-manager register --username testuser1 --password password --auto --org admin
 		//The system has been registered with id: 243ea73d-01bb-458d-a7a5-2d61fde69494 
 		//Installed Product Current Status:
 		
 		// pre-fix for blockedByBug-678049 Assert.assertContainsNoMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "register with autosubscribe should NOT appear to have subscribed to something when there are no installed products.");
 		Assert.assertTrue(InstalledProduct.parse(sshCommandResult.getStdout()).isEmpty(),
 				"The Installed Product Current Status should be empty when attempting to register with autosubscribe without any product certs installed.");
 		Assert.assertEquals(clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null, null, null).getStdout().trim(),"No installed products to list",
 				"Since we changed the productCertDir configuration to an empty location, we should not appear to have any products installed.");
 
 		// subscribe to the first available pool that provides one product (whose product cert was also originally installed)
 		File tmpProductCertFile = null;
 		OUTERLOOP: for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+pool.poolId));	
 			JSONArray jsonProvidedProducts = jsonPool.getJSONArray("providedProducts");
 			if (jsonProvidedProducts.length()==1) {	// FIXME: I doubt this check is needed anymore
 				JSONObject jsonProvidedProduct = jsonProvidedProducts.getJSONObject(0);
 				String productId = jsonProvidedProduct.getString("productId");
 				
 				// now install the product that this pool will cover to our tmpProductCertDir
 				/* NOT WORKING IN STAGE SINCE THE /products/{PRODUCT_ID}/certificate PATH APPEARS BLACK-LISTED
 				JSONObject jsonProductCert = new JSONObject (CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+productId+"/certificate"));
 				String cert = jsonProductCert.getString("cert");
 				String key = jsonProductCert.getString("key");
 				tmpProductCertFile = new File(tmpProductCertDir+File.separator+"AutosubscribeProduct_"+productId+".pem");
 				client.runCommandAndWait("echo \""+cert+"\" > "+tmpProductCertFile);
 				break;
 				*/
 				
 				// now search for an existing installed product that matches and install it as our new tmpProductCert
 				for (ProductCert productCert : installedProductCerts) {
 					if (productCert.productId.equals(productId)) {
 						tmpProductCertFile = new File(tmpProductCertDir+File.separator+"AutosubscribeProduct_"+productId+".pem");
 						client.runCommandAndWait("cp "+productCert.file+" "+tmpProductCertFile);
 						break OUTERLOOP;
 					}
 				}
 			}
 		}
 		if (tmpProductCertFile==null) throw new SkipException("Could not find an available pool that provides only one product with which to test register with --autosubscribe.");
 		ProductCert tmpProductCert = clienttasks.getProductCertFromProductCertFile(tmpProductCertFile);
 		
 		// reregister with autosubscribe and assert that the product is bound
 		sshCommandResult = clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, Boolean.TRUE, null, null, (String)null, null, null, true, false, null, null, null);
 		
 		// assert that the sshCommandResult from register indicates the tmpProductCert was subscribed
 		
 		/* # subscription-manager register --username=testuser1 --password=password --org=admin --autosubscribe
 		The system has been registered with id: f95fd9bb-4cc8-428e-b3fd-d656b14bfb89 
 		Installed Product Current Status:
 
 		ProductName:         	Awesome OS for S390X Bits
 		Status:               	Subscribed  
 		*/
 
 		// assert that our tmp product install appears to have been autosubscribed
 		InstalledProduct autoSubscribedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("status", "Subscribed", InstalledProduct.parse(clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null, null, null).getStdout()));
 		Assert.assertNotNull(autoSubscribedProduct,	"We appear to have autosubscribed to our fake product install.");
 		// pre-fix for blockedByBug-678049 Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "The stdout from register with autotosubscribe indicates that we have subscribed to something");
 		// pre-fix for blockedByBug-678049 Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^\\s+"+autoSubscribedProduct.productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"), "Expected ProductName '"+autoSubscribedProduct.productName+"' was reported as autosubscribed in the output from register with autotosubscribe.");
 		//Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), ".* - Subscribed", "The stdout from register with autotosubscribe indicates that we have automatically subscribed at least one of this system's installed products to an available subscription pool.");
 		List<InstalledProduct> autosubscribedProductStatusList = InstalledProduct.parse(sshCommandResult.getStdout());
 		Assert.assertEquals(autosubscribedProductStatusList.size(), 1, "Only one product appears installed."); 
 		Assert.assertEquals(autosubscribedProductStatusList.get(0),new InstalledProduct(tmpProductCert.productName,null,null,null,"Subscribed",null,null),
 				"As expected, ProductName '"+tmpProductCert.productName+"' was reported as subscribed in the output from register with autotosubscribe.");
 	}
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 			groups={"blockedByBug-602378", "blockedByBug-616137", "blockedByBug-678049", "blockedByBug-737762", "blockedByBug-743082", "AcceptanceTests"},
 			enabled=true)
 	public void RegisterWithAutosubscribe_Test() throws JSONException, Exception {
 
 		// determine all of the productIds that are provided by available subscriptions
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, false, null, null, null);
 		Set<String> providedProductIds = new HashSet<String>();
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+pool.poolId));	
 			JSONArray jsonProvidedProducts = jsonPool.getJSONArray("providedProducts");
 			for (int k = 0; k < jsonProvidedProducts.length(); k++) {
 				JSONObject jsonProvidedProduct = (JSONObject) jsonProvidedProducts.get(k);
 				String providedProductName = jsonProvidedProduct.getString("productName");
 				String providedProductId = jsonProvidedProduct.getString("productId");
 				providedProductIds.add(providedProductId);
 			}
 		}
 		
 		// assert that all installed products are "Not Subscribed"
 		List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		for (InstalledProduct installedProduct : installedProducts) {
 			Assert.assertEquals(installedProduct.status,"Not Subscribed","Installed product status for productId '"+installedProduct.productId+"' prior to registration with autosubscribe.");
 		}
 		
 		// now register with --autosubscribe
 		SSHCommandResult registerResult = clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, true, null, null, (String)null, null, null, true, false, null, null, null);
 		//	201208091741:18.316 - FINE: ssh root@pogolinux-1.rhts.eng.rdu.redhat.com subscription-manager register --username=stage_test_12 --password=redhat --autosubscribe --force (com.redhat.qe.tools.SSHCommandRunner.run)
 		//	201208091741:50.504 - FINE: Stdout: 
 		//	The system with UUID 5e88b5ef-e218-423c-8cf1-380d37476580 has been unregistered
 		//	The system has been registered with id: edfabea2-0bd4-4584-bb3e-f3bca9a3a442 
 		//	Installed Product Current Status:
 		//	Product Name:         	Red Hat Enterprise Linux Server
 		//	Status:               	Subscribed
 		//
 		//	201208091741:50.505 - FINE: Stderr:  
 		//	201208091741:50.505 - FINE: ExitCode: 0 
 		
 		// IF THE INSTALLED PRODUCT ID IS PROVIDED BY AN AVAILABLE SUBSCRIPTION, THEN IT SHOULD GET AUTOSUBSCRIBED! (since no service preference level has been set)
 		
 		// assert the expected installed product status in the feedback from register with --autosubscribe
 		List<InstalledProduct> autosubscribedProducts = InstalledProduct.parse(registerResult.getStdout());
 		for (InstalledProduct autosubscribedProduct : autosubscribedProducts) {
 			InstalledProduct installedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("productName", autosubscribedProduct.productName, installedProducts);
 			if (providedProductIds.contains(installedProduct.productId)) {
 				Assert.assertEquals(autosubscribedProduct.status,"Subscribed","Status for productName '"+autosubscribedProduct.productName+"' in feedback from registration with autosubscribe.");
 			} else {
 				Assert.assertEquals(autosubscribedProduct.status,"Not Subscribed","Status for productName '"+autosubscribedProduct.productName+"' in feedback from registration with autosubscribe.");
 			}
 		}
 		
 		// assert the expected installed product status in the list --installed after the register with --autosubscribe
 		installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		for (InstalledProduct installedProduct : installedProducts) {
 			if (providedProductIds.contains(installedProduct.productId)) {
 				Assert.assertEquals(installedProduct.status,"Subscribed","Status for Installed productId '"+installedProduct.productId+"' in list of installed products after registration with autosubscribe.");
 			} else {
 				Assert.assertEquals(installedProduct.status,"Not Subscribed","Status for Installed productId '"+installedProduct.productId+"' in list of installed products after registration with autosubscribe.");
 			}
 		}
 		Assert.assertEquals(autosubscribedProducts.size(), installedProducts.size(), "The 'Installed Product Current Status' reported during register --autosubscribe should contain the same number of products as reported by list --installed.");
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: register with --force",
 			groups={"blockedByBug-623264"},
 			enabled=true)
 	public void RegisterWithForce_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// make sure you are first registered
 		SSHCommandResult sshCommandResult = clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null, null, null, (String)null, null, null, null, false, null, null, null);
 		String firstConsumerId = clienttasks.getCurrentConsumerId();
 		
 		// subscribe to a random pool (so as to consume an entitlement)
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		if (pools.isEmpty()) throw new SkipException("Cannot randomly pick a pool for subscribing when there are no available pools for testing."); 
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// attempt to register again and assert that you are warned that the system is already registered
 		sshCommandResult = clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null, null, null, (String)null, null, null, null, false, null, null, null);
 		Assert.assertTrue(sshCommandResult.getStdout().startsWith("This system is already registered."),"Expecting: This system is already registered.");
 		
 		// register with force
 		sshCommandResult = clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 		String secondConsumerId = clienttasks.getCurrentConsumerId();
 		
 		// assert the stdout reflects a new consumer
 		Assert.assertTrue(sshCommandResult.getStdout().startsWith("The system with UUID "+firstConsumerId+" has been unregistered"),
 				"The system with UUID "+firstConsumerId+" has been unregistered");
 		Assert.assertTrue(!secondConsumerId.equals(firstConsumerId),
 				"After registering with force, a newly registered consumerid was returned.");
 
 		// assert that the new consumer is not consuming any entitlements
 		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertEquals(productSubscriptions.size(),0,"After registering with force, no product subscriptions should be consumed.");
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: register with --name",
 			dataProvider="getRegisterWithName_TestData",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=62352) // caseIds=81089 81090 81091
 	public void RegisterWithName_Test(Object bugzilla, String name, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// register with a name
 		SSHCommandResult sshCommandResult = clienttasks.register_(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,name,null, null, null, null, (String)null, null, null, null, null, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode,"ExitCode after register with --name=\""+name+"\" option:");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after register with --name=\""+name+"\" option:");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex,"Stderr after register with --name=\""+name+"\" option:");
 		
 		// assert that the name is happily placed in the consumer cert
 		if (expectedExitCode!=null && expectedExitCode==0) {
 			ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 			Assert.assertEquals(consumerCert.name, name, "");
 		}
 	}
 	@DataProvider(name="getRegisterWithName_TestData")
 	public Object[][] getRegisterWithName_TestDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithName_TestDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithName_TestDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		String invalidNameStderr = "System name must consist of only alphanumeric characters, periods, dashes and underscores.";	// bugzilla 672233
 		       invalidNameStderr = "System name cannot contain most special characters.";	// bugzilla 677405
 		String maxCharsStderr = "Name of the consumer should be shorter than 250 characters\\.";
 		String name;
 		String successfulStdout = "The system has been registered with id: [a-f,0-9,\\-]{36}";
 
 		// valid names according to bugzilla 672233
 		name = "periods...dashes---underscores___alphanumerics123";
 										ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} "+name*/,	null}));
 		name = "249_characters_678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
 										ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} "+name*/,	null}));
 
 		// the tolerable characters has increased due to bugzilla 677405 and agilo task http://gibson.usersys.redhat.com/agilo/ticket/5235 (6.1) As an IT Person, I would like to ensure that user service and candlepin enforce the same valid character rules (QE); Developer beav "Christopher Duryee" <cduryee@redhat.com>
 		// https://bugzilla.redhat.com/show_bug.cgi?id=677405#c1
 		name = "[openingBracket[";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} \\[openingBracket\\["*/,	null}));
 		name = "]closingBracket]";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} \\]closingBracket\\]"*/,	null}));
 		name = "{openingBrace{";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} \\{openingBrace\\{"*/,	null}));
 		name = "}closingBrace}";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} \\}closingBrace\\}"*/,	null}));
 		name = "(openingParenthesis(";	ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} \\(openingParenthesis\\("*/,	null}));
 		name = ")closingParenthesis)";	ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} \\)closingParenthesis\\)"*/,	null}));
 		name = "?questionMark?";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} \\?questionMark\\?"*/,	null}));
 		name = "@at@";					ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} "+name*/,	null}));
 		name = "!exclamationPoint!";	ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} "+name*/,	null}));
 		name = "`backTick`";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} "+name*/,	null}));
 		name = "'singleQuote'";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} "+name*/,	null}));
 		name = "pound#sign";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	successfulStdout/*"[a-f,0-9,\\-]{36} "+name*/,	null}));	// Note: pound signs within the name are acceptable, but not at the beginning
 
 		// invalid names
 		// Note: IT Services invalid characters can be tested by trying to Sign Up a new login here: https://www.webqa.redhat.com/wapps/sso/login.html
 		// Invalid Chars: (") ($) (^) (<) (>) (|) (+) (%) (/) (;) (:) (,) (\) (*) (=) (~)  // from https://bugzilla.redhat.com/show_bug.cgi?id=677405#c1
 		name = "\"doubleQuotes\"";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "$dollarSign$";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "^caret^";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "<lessThan<";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = ">greaterThan>";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "|verticalBar|";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "+plus+";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "%percent%";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "/slash/";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = ";semicolon;";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = ":colon:";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = ",comma,";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "\\backslash\\";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "*asterisk*";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "=equal=";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		name = "~tilde~";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 
 		// spaces are also rejected characters from IT Services
 		name = "s p a c e s";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 
 		// special case (pound sign at the beginning is a limitation in the x509 certificates)
 		name = "#poundSign";				ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(255),	null,	"System name cannot begin with # character"}));
 
 		//	
 		// http://www.ascii.cl/htmlcodes.htm
 		// TODO
 		//name = "é";						ll.add(Arrays.asList(new Object[]{	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		//name = "ë";						ll.add(Arrays.asList(new Object[]{	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 		//name = "â";						ll.add(Arrays.asList(new Object[]{	name,	Integer.valueOf(255),	null,	invalidNameStderr}));
 
 
 
 		// names that are too long (>=250 chars)
 		name = "250_characters_6789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
 										ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("672233"),	name,	Integer.valueOf(255),	null,	maxCharsStderr}));
 
 
 		return ll;
 	}
 	
 	
 	
 	@Test(	description="subscription-manager-cli: register with --name and --type",
 			dataProvider="getRegisterWithNameAndType_TestData",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithNameAndType_Test(Object bugzilla, String username, String password, String owner, String name, ConsumerType type, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// register with a name
 		SSHCommandResult sshCommandResult = clienttasks.register_(username,password,owner,null,type,name,null, null, null, null, (String)null, null, null, null, null, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode,"ExitCode after register with --name="+name+" --type="+type+" options:");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after register with --name="+name+" --type="+type+" options:");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex,"Stderr after register with --name="+name+" --type="+type+" options:");
 	}
 	@DataProvider(name="getRegisterWithNameAndType_TestData")
 	public Object[][] getRegisterWithNameAndType_TestDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithNameAndType_TestDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithNameAndType_TestDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		String username=sm_clientUsername;
 		String password=sm_clientPassword;
 		String owner=sm_clientOrg;
 
 		List <String> registerableConsumerTypes = new ArrayList<String> ();
 		JSONArray jsonConsumerTypes = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,"/consumertypes"));	
 		for (int i = 0; i < jsonConsumerTypes.length(); i++) {
 			JSONObject jsonConsumerType = (JSONObject) jsonConsumerTypes.get(i);
 			String consumerType = jsonConsumerType.getString("label");
 			registerableConsumerTypes.add(consumerType);
 		}
 		
 		// interate across all ConsumerType values and append rows to the dataProvider
 		for (ConsumerType type : ConsumerType.values()) {
 			String name = type.toString()+"_NAME";
 			
 			// decide what username and password to test with
 			if (type.equals(ConsumerType.person) && !getProperty("sm.rhpersonal.username", "").equals("")) {
 				username = sm_rhpersonalUsername;
 				password = sm_rhpersonalPassword;
 				owner = sm_rhpersonalOrg;
 			} else {
 				username = sm_clientUsername;
 				password = sm_clientPassword;
 				owner = sm_clientOrg;
 			}
 			
 			// String username, String password, String owner, String name, ConsumerType type, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 			if (registerableConsumerTypes.contains(type.toString())) {
 				/* applicable to RHEL61 and RHEL57
 				if (type.equals(ConsumerType.person)) {
 					ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("661130"),	username,	password,	name,	type,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+username,	null}));
 				} else {
 					ll.add(Arrays.asList(new Object[]{null,  							username,	password,	name,	type,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));			
 				}
 				*/
 				ll.add(Arrays.asList(new Object[]{null,  	username,	password,	owner,	name,	type,	Integer.valueOf(0),	"The system has been registered with id: [a-f,0-9,\\-]{36}",	null}));			
 			} else {
 				ll.add(Arrays.asList(new Object[]{ null,	username,	password,	owner,	name,	type,	Integer.valueOf(255),	null,	"No such consumer type: "+type}));			
 	
 			}
 		}
 
 		return ll;
 	}
 	
 	
 	
 	@Test(	description="assert that a consumer can register with a release value and that subscription-manager release will return the set value",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithRelease_Test() {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,"Foo",(List<String>)null,null,null,true,null, null, null, null);		
 		Assert.assertEquals(clienttasks.getCurrentRelease(), "Foo", "The release value retrieved after registering with the release.");
 	}
 	
 	
 	
 	/**
 	 * https://tcms.engineering.redhat.com/case/56327/?from_plan=2476
 		Actions:
 
 			* register a client to candlepin
 			* subscribe to a pool
 			* list consumed
 			* reregister
 
 	    Expected Results:
 
 	 		* check the identity cert has not changed
 	        * check the consumed entitlements have not changed
 	 */
 	@Test(	description="subscription-manager-cli: reregister basic registration",
 			groups={"blockedByBug-636843","AcceptanceTests"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=56327)
 	public void ReregisterBasicRegistration_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister(null, null, null);
 		String consumerIdBefore = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(String)null,null,null, null, false, null, null, null));
 		
 		// take note of your identity cert before reregister
 		ConsumerCert consumerCertBefore = clienttasks.getCurrentConsumerCert();
 		
 		// subscribe to a random pool
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		if (pools.isEmpty()) throw new SkipException("Cannot randomly pick a pool for subscribing when there are no available pools for testing."); 
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// get a list of the consumed products
 		List<ProductSubscription> consumedProductSubscriptionsBefore = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		
 		// reregister
 		//clienttasks.reregister(null,null,null);
 		clienttasks.reregisterToExistingConsumer(sm_clientUsername,sm_clientPassword,consumerIdBefore);
 		
 		// assert that the identity cert has not changed
 		ConsumerCert consumerCertAfter = clienttasks.getCurrentConsumerCert();
 		Assert.assertEquals(consumerCertBefore, consumerCertAfter, "The consumer identity cert has not changed after reregistering with consumerid.");
 		
 		// assert that the user is still consuming the same products
 		List<ProductSubscription> consumedProductSubscriptionsAfter = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(
 				consumedProductSubscriptionsAfter.containsAll(consumedProductSubscriptionsBefore) &&
 				consumedProductSubscriptionsBefore.size()==consumedProductSubscriptionsAfter.size(),
 				"The list of consumed products after reregistering is identical.");
 	}
 	
 	
 	
 	/**
 	 * https://tcms.engineering.redhat.com/case/56328/?from_plan=2476
 	 * 
 		Actions:
 
 	 		* register a client to candlepin (take note of the uuid returned)
 	 		* take note of your identity cert info using openssl x509
 	 		* subscribe to a pool
 	 		* list consumed
 	 		* ls /etc/pki/entitlement/products
 	 		* Now.. mess up your identity..  mv /etc/pki/consumer/cert.pem /bak
 	 		* run the "reregister" command w/ username and passwd AND w/consumerid=<uuid>
 
 		Expected Results:
 
 	 		* after running reregister you should have a new identity cert
 	 		* after registering you should still the same products consumed (list consumed)
 	 		* the entitlement serials should be the same as before the registration
 	 */
 	@Test(	description="subscription-manager-cli: bad identity cert",
 			groups={"blockedByBug-624106","blockedByBug-844069","ReregisterWithBadIdentityCert_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=56328)
 	public void ReregisterWithBadIdentityCert_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(String)null,null,null, null, false, null, null, null);
 		
 		// take note of your identity cert
 		ConsumerCert consumerCertBefore = clienttasks.getCurrentConsumerCert();
 		
 		// subscribe to a random pool
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		if (pools.isEmpty()) throw new SkipException("Cannot randomly pick a pool for subscribing when there are no available pools for testing."); 
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// get a list of the consumed products
 		List<ProductSubscription> consumedProductSubscriptionsBefore = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		
 		// Now.. mess up your identity..  by borking its content
 		log.info("Messing up the identity cert by borking its content...");
 		RemoteFileTasks.runCommandAndAssert(client, "openssl x509 -noout -text -in "+clienttasks.consumerCertFile()+" > /tmp/stdout; mv /tmp/stdout -f "+clienttasks.consumerCertFile(), 0);
 		
 		// reregister w/ username, password, and consumerid
 		//clienttasks.reregister(client1username,client1password,consumerCertBefore.consumerid);
 		log.warning("The subscription-manager-cli reregister module has been eliminated and replaced by register --consumerid (b3c728183c7259841100eeacb7754c727dc523cd)...");
 		clienttasks.register(sm_clientUsername,sm_clientPassword,null,null,null,null,consumerCertBefore.consumerid, null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 		
 		// assert that the identity cert has not changed
 		ConsumerCert consumerCertAfter = clienttasks.getCurrentConsumerCert();
 		Assert.assertEquals(consumerCertBefore, consumerCertAfter, "The consumer identity cert has not changed after reregistering with consumerid.");
 	
 		// assert that the user is still consuming the same products
 		List<ProductSubscription> consumedProductSubscriptionsAfter = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(
 				consumedProductSubscriptionsAfter.containsAll(consumedProductSubscriptionsBefore) &&
 				consumedProductSubscriptionsBefore.size()==consumedProductSubscriptionsAfter.size(),
 				"The list of consumed products after reregistering is identical.");
 	}
 	@AfterGroups(groups={"setup"},value={"ReregisterWithBadIdentityCert_Test"})
 	public void afterReregisterWithBadIdentityCert_Test() {
 		// needed in case ReregisterWithBadIdentityCert_Test fails to prevent succeeding tests from failing with an "Error loading certificate"
 		clienttasks.unregister_(null, null, null);	// give system an opportunity to clean it's consumerid with the server
 		clienttasks.clean(null, null, null);	// needed in case the system failed to unregister
 	}
 	
 	
 	/**
 	 * https://tcms.engineering.redhat.com/case/72845/?from_plan=2476
 	 * 
 Actions:
 
     * register with username and password and remember the consumerid
     * subscribe to one or more subscriptions
     * list the consumed subscriptions and remember them
     * clean system
     * assert that there are no entitlements on the system
     * register with same username, password and existing consumerid
     * assert that originally consumed subscriptions are once again being consumed
 
 	
 Expected Results:
 
     * when registering a new system to an already existing consumer, all of the existing consumers entitlement certs should be downloaded to the new system
 	 * @throws Exception 
 	 * @throws JSONException 
 
 	 */
 	@Test(	description="register with existing consumerid should automatically refresh entitlements",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=72845)
 	public void ReregisterWithConsumerIdShouldAutomaticallyRefreshEntitlements_Test() throws JSONException, Exception {
 		
 		// register with username and password and remember the consumerid
 		clienttasks.unregister(null, null, null);
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, null, null, null, (String)null, null, null, null, false, null, null, null));
 		
 		// subscribe to one or more subscriptions
 		//// subscribe to a random pool
 		//List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		//SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		//clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 
 		// list the consumed subscriptions and remember them
 		List <ProductSubscription> originalConsumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		// also remember the current entitlement certs
 		List <EntitlementCert> originalEntitlementCerts= clienttasks.getCurrentEntitlementCerts();
 		
 		// clean system
 		clienttasks.clean(null, null, null);
 		
 		// assert that there are no entitlements on the system
 		//Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().isEmpty(),"There are NO consumed Product Subscriptions on this system after running clean");
 		Assert.assertTrue(clienttasks.getCurrentEntitlementCerts().isEmpty(),"There are NO Entitlement Certs on this system after running clean");
 		
 		// register with same username, password and existing consumerid
 		// Note: no need to register with force as running clean wipes system of all local registration data
 		clienttasks.register(sm_clientUsername,sm_clientPassword,null,null,null,null,consumerId, null, null, null, (String)null, null, null, null, false, null, null, null);
 
 		// assert that originally consumed subscriptions are once again being consumed
 		List <ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertEquals(consumedProductSubscriptions.size(),originalConsumedProductSubscriptions.size(), "The number of consumed Product Subscriptions after registering to an existing consumerid matches his original count.");
 		for (ProductSubscription productSubscription : consumedProductSubscriptions) {
 			Assert.assertContains(originalConsumedProductSubscriptions, productSubscription);
 		}
 		// assert that original entitlement certs are once on the system
 		List <EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 		Assert.assertEquals(entitlementCerts.size(),originalEntitlementCerts.size(), "The number of Entitlement Certs on the system after registering to an existing consumerid matches his original count.");
 		for (EntitlementCert entitlementCert : entitlementCerts) {
 			Assert.assertContains(originalEntitlementCerts, entitlementCert);
 		}
 		
 	}
 	
 	
 	
 	@Test(	description="register with an empty /var/lib/rhsm/facts/facts.json file",
 			groups={"blockedByBug-667953","blockedByBug-669208"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterWithAnEmptyRhsmFactsJsonFile_Test() {
 		
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==1, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' exists");
 		log.info("Emptying rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"'...");
 		client.runCommandAndWait("echo \"\" > "+clienttasks.rhsmFactsJsonFile, TestRecords.action());
 		SSHCommandResult result = client.runCommandAndWait("cat "+clienttasks.rhsmFactsJsonFile, TestRecords.action());
 		Assert.assertTrue(result.getStdout().trim().equals(""), "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' is empty.");
 		
 		log.info("Attempt to register with an empty rhsm facts file (expecting success)...");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 	}
 	
 	
 	
 	@Test(	description="register with a missing /var/lib/rhsm/facts/facts.json file",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterWithAnMissingRhsmFactsJsonFile_Test() {
 		
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==1, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' exists");
 		log.info("Deleting rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"'...");
 		RemoteFileTasks.runCommandAndWait(client, "rm -f "+clienttasks.rhsmFactsJsonFile, TestRecords.action());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==0, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' has been removed");
 		
 		log.info("Attempt to register with a missing rhsm facts file (expecting success)...");
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, Boolean.TRUE, false, null, null, null);
 	}
 	
 	
 	
 	@Test(	description="register with interactive prompting for credentials",
 			groups={"blockedByBug-678151"},
 			dataProvider = "getRegisterWithInteractivePromptingForCredentials_TestData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithInteractivePromptingForCredentials_Test(Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, String commandLineOrg, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		
 		// ensure we are unregistered
 		clienttasks.unregister(null,null,null);
 
 		// call register while providing a valid username at the interactive prompt
 		String command;
 		if (client.runCommandAndWait("rpm -q expect").getExitCode().intValue()==0) {	// is expect installed?
 			// assemble an ssh command using expect to simulate an interactive supply of credentials to the register command
 			String promptedUsernames=""; if (promptedUsername!=null) for (String username : promptedUsername.split("\\n")) {
 				promptedUsernames += "expect \\\"*Username:\\\"; send "+username+"\\\r;";
 			}
 			String promptedPasswords=""; if (promptedPassword!=null) for (String password : promptedPassword.split("\\n")) {
 				promptedPasswords += "expect \\\"*Password:\\\"; send "+password+"\\\r;";
 			}
 			// [root@jsefler-onprem-5server ~]# expect -c "spawn subscription-manager register; expect \"*Username:\"; send qa@redhat.com\r; expect \"*Password:\"; send CHANGE-ME\r; expect eof; catch wait reason; exit [lindex \$reason 3]"
 			command = String.format("expect -c \"spawn %s register %s %s %s; %s %s expect eof; catch wait reason; exit [lindex \\$reason 3]\"",
 					clienttasks.command,
 					commandLineUsername==null?"":"--username="+commandLineUsername,
 					commandLinePassword==null?"":"--password="+commandLinePassword,
 					commandLineOrg==null?"":"--org="+commandLineOrg,
 					promptedUsernames,
 					promptedPasswords);
 		} else {
 			// assemble an ssh command using echo and pipe to simulate an interactive supply of credentials to the register command
 			// [root@jsefler-stage-6server ~]# echo -e "testuser1" | subscription-manager register --password password --org=admin
 			String echoUsername= promptedUsername==null?"":promptedUsername;
 			String echoPassword = promptedPassword==null?"":promptedPassword;
 			String n = (promptedPassword!=null&&promptedUsername!=null)? "\n":"";	// \n works;  \r does not work
 			command = String.format("echo -e \"%s\" | %s register %s %s %s",
 					echoUsername+n+echoPassword,
 					clienttasks.command,
 					commandLineUsername==null?"":"--username="+commandLineUsername,
 					commandLinePassword==null?"":"--password="+commandLinePassword,
 					commandLineOrg==null?"":"--org="+commandLineOrg);
 		}
 		// attempt to register with the interactive credentials
 		SSHCommandResult sshCommandResult = client.runCommandAndWait(command);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode, "The expected exit code from the register attempt.");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout(), expectedStdoutRegex, "The expected stdout result from register while supplying interactive credentials.");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr(), expectedStderrRegex, "The expected stderr result from register while supplying interactive credentials.");
 	}
 	@DataProvider(name="getRegisterWithInteractivePromptingForCredentials_TestData")
 	public Object[][] getRegisterWithInteractivePromptingForCredentials_TestDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithInteractivePromptingForCredentials_TestDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithInteractivePromptingForCredentials_TestDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 		String x = String.valueOf(getRandInt());
 		if (client.runCommandAndWait("rpm -q expect").getExitCode().intValue()==0) {	// is expect installed?
 			// Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, String commandLineOwner, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			null,						null,				sm_clientPassword,	sm_clientOrg,	new Integer(0),		"The system has been registered with id: [a-f,0-9,\\-]{36}",				null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		null,						null,				sm_clientPassword,	sm_clientOrg,	new Integer(255),	uErrMsg,																	null}));
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword,			sm_clientUsername,	null,				sm_clientOrg,	new Integer(0),		"The system has been registered with id: [a-f,0-9,\\-]{36}",				null}));
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword+x,		sm_clientUsername,	null,				sm_clientOrg,	new Integer(255),	uErrMsg,																	null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			sm_clientPassword,			null,				null,				sm_clientOrg,	new Integer(0),		"The system has been registered with id: [a-f,0-9,\\-]{36}",				null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		sm_clientPassword+x,		null,				null,				sm_clientOrg,	new Integer(255),	uErrMsg,																	null}));
 			ll.add(Arrays.asList(new Object[] {	null,	"\n\n"+sm_clientUsername,	"\n\n"+sm_clientPassword,	null,				null,				sm_clientOrg,	new Integer(0),		"(\nUsername: ){3}"+sm_clientUsername+"(\nPassword: ){3}"+"\nThe system has been registered with id: [a-f,0-9,\\-]{36}",	null}));		
 		} else {
 			// Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, String commandLineOwner, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			null,						null,				sm_clientPassword,	sm_clientOrg,	new Integer(0),		"The system has been registered with id: [a-f,0-9,\\-]{36}",				null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		null,						null,				sm_clientPassword,	sm_clientOrg,	new Integer(255),	null,																		uErrMsg}));
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword,			sm_clientUsername,	null,				sm_clientOrg,	new Integer(0),		"The system has been registered with id: [a-f,0-9,\\-]{36}",				null}));
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword+x,		sm_clientUsername,	null,				sm_clientOrg,	new Integer(255),	null,																		uErrMsg}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			sm_clientPassword,			null,				null,				sm_clientOrg,	new Integer(0),		"The system has been registered with id: [a-f,0-9,\\-]{36}",				null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		sm_clientPassword+x,		null,				null,				sm_clientOrg,	new Integer(255),	null,																		uErrMsg}));
 			ll.add(Arrays.asList(new Object[] {	null,	"\n\n"+sm_clientUsername,	"\n\n"+sm_clientPassword,	null,				null,				sm_clientOrg,	new Integer(0),		"(Username: ){3}The system has been registered with id: [a-f,0-9,\\-]{36}",	"(Warning: Password input may be echoed.\nPassword: \n){3}"}));		
 		}
 		return ll;
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: attempt register to --environment when the candlepin server does not support environments should fail",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptRegisterToEnvironmentWhenCandlepinDoesNotSupportEnvironments_Test() throws JSONException, Exception {
 		// ask the candlepin server if it supports environment
 		boolean supportsEnvironments = CandlepinTasks.isEnvironmentsSupported(sm_clientUsername, sm_clientPassword, sm_serverUrl);
 		
 		// skip this test when candlepin supports environments
 		if (supportsEnvironments) throw new SkipException("Candlepin server '"+sm_serverHostname+"' appears to support environments, therefore this test is not applicable.");
 
 		SSHCommandResult result = clienttasks.register_(sm_clientUsername,sm_clientPassword,sm_clientOrg,"foo",null,null,null,null,null,null,(String)null,null, null, true, null, null, null, null);
 		
 		// assert results
 		Assert.assertEquals(result.getStderr().trim(), "Error: Server does not support environments.","Attempt to register to an environment on a server that does not support environments should be blocked.");
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255),"Exit code from register to environment when the candlepin server does NOT support environments.");
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: attempt register to --environment without --org option should fail",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptRegisterToEnvironmentWithoutOrg_Test() throws JSONException, Exception {
 		// ask the candlepin server if it supports environment
 		boolean supportsEnvironments = CandlepinTasks.isEnvironmentsSupported(sm_clientUsername, sm_clientPassword, sm_serverUrl);
 
 		SSHCommandResult result = clienttasks.register_(sm_clientUsername,sm_clientPassword,null,"foo",null,null,null,null,null,null,(String)null,null, null, true, null, null, null, null);
 
 		// skip this test when candlepin does not support environments
 		if (!supportsEnvironments) {
 			// but before we skip, we can verify that environments are unsupported by this server
 			Assert.assertEquals(result.getStderr().trim(), "Error: Server does not support environments.","Attempt to register to an environment on a server that does not support environments should be blocked.");
 			Assert.assertEquals(result.getExitCode(), Integer.valueOf(255),"Exit code from register to environment when the candlepin server does NOT support environments.");
 			throw new SkipException("Candlepin server '"+sm_serverHostname+"' does not support environments, therefore this test is not applicable.");
 		}
 
 		// assert results when candlepin supports environments
 		Assert.assertEquals(result.getStdout().trim(), "Error: Must specify --org to register to an environment.","Registering to an environment requires that the org be specified.");
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255),"Exit code from register with environment option and without org option.");
 	}
 	
 	
 	protected String rhsm_baseurl = null;
 	@BeforeGroups(value={"RegisterWithBaseurl_Test"}, alwaysRun=true)
 	public void beforeRegisterWithBaseurl_Test() {
 		if (clienttasks==null) return;
 		rhsm_baseurl = clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm", "baseurl");
 	}
 	@Test(	description="subscription-manager-cli: register with --baseurl",
 			dataProvider="getRegisterWithBaseurl_TestData",
 			groups={"RegisterWithBaseurl_Test","AcceptanceTests"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithBaseurl_Test(Object bugzilla, String baseurl, String baseurlConfigured, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		// get original baseurl at the beginning of this test
 		//String baseurlBeforeTest = clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm","baseurl");
 		String baseurlBeforeTest = clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "baseurl");
 		
 		// register with a baseurl
 		SSHCommandResult sshCommandResult = clienttasks.register_(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, false, null, null, (String)null, null, baseurl, true, null, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode,"ExitCode after register with --baseurl="+baseurl+" and other options:");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after register with --baseurl="+baseurl+" and other options:");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex,"Stderr after register with --baseurl="+baseurl+" and other options:");
 		Assert.assertContainsNoMatch(sshCommandResult.getStderr().trim(), "Traceback.*","Stderr after register with --baseurl="+baseurl+" and other options should not contain a Traceback.");
 	
 		// negative testcase assertions........
 		if (expectedExitCode.equals(new Integer(255))) {
 			// assert that the current config remains unchanged when the expectedExitCode is 255
 			//Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm","baseurl"), baseurlBeforeTest, "The rhsm configuration for baseurl should remain unchanged when attempting to register with an invalid baseurl.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "baseurl"), baseurlBeforeTest, "The "+clienttasks.rhsmConfFile+" configuration for [rhsm] baseurl should remain unchanged when attempting to register with an invalid baseurl.");
 			
 			// nothing more to do after these negative testcase assertions
 			return;
 		}
 		
 		// positive testcase assertions........
 		// assert that the current config has been updated to the new expected baseurl
 		//Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "rhsm","baseurl"), baseurlConfigured, "The rhsm configuration for baseurl has been updated to the new baseurl with correct format (https://hostname:443/prefix).");
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "baseurl"), baseurlConfigured, "The "+clienttasks.rhsmConfFile+" configuration for [rhsm] baseurl has been updated to the new baseurl with correct format (https://hostname:443/prefix).");
 	
 //		// TODO maybe assert yumRepos  7/24/2012
 //		for (YumRepo yumRepo : clienttasks.getCurrentlySubscribedYumRepos()) {
 //			Assert.assertTrue(yumRepo.baseurl.matches("^"+baseurlConfigured.replaceFirst("/$","")+"/\\w+.*"),"Newly configured baseurl '"+baseurlConfigured+"' is utilized by yumRepo: "+yumRepo);
 //		}
 //		
 //		// TODO maybe assert repos --list  7/24/2012
 //		for (Repo repo : clienttasks.getCurrentlySubscribedRepos()) {
 //			Assert.assertTrue(repo.repoUrl.matches("^"+baseurlConfigured.replaceFirst("/$","")+"/\\w+.*"),"Newly configured baseurl '"+baseurlConfigured+"' is utilized by repo: "+repo);
 //		}
 
 	}
 	@AfterGroups(value={"RegisterWithBaseurl_Test"},alwaysRun=true)
 	public void afterRegisterWithBaseurl_Test() {
 		if (rhsm_baseurl!=null) clienttasks.config(null,null,true,new String[]{"rhsm","baseurl",rhsm_baseurl});
 	}
 	
 	@DataProvider(name="getRegisterWithBaseurl_TestData")
 	public Object[][] getRegisterWithBaseurl_TestDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithBaseurl_TestDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithBaseurl_TestDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		String defaultHostname = "cdn.redhat.com";
 		
 		//  --baseurl=BASE_URL    base url for content in form of https://hostname:443/prefix
 
 		// Object bugzilla, String baseurl, String baseurlConfigured, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 		// positive tests
 		ll.add(Arrays.asList(new Object[] {	null,							"https://myhost.example.com:900/myapp/",	"https://myhost.example.com:900/myapp/",	new Integer(0),		null,			null}));
		ll.add(Arrays.asList(new Object[] {	new BlockedByBzBug("842830"),	"http://myhost.example.com:900/myapp/",		"http://myhost.example.com:900/myapp/",		new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"https://https:900/myapp/",					"https://https:900/myapp/",					new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"https://http:900/myapp/",					"https://http:900/myapp/",					new Integer(0),		null,			null}));
		ll.add(Arrays.asList(new Object[] {	new BlockedByBzBug("842830"),	"http://http:900/myapp/",					"http://http:900/myapp/",					new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com:900/myapp/",			"https://myhost.example.com:900/myapp/",	new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com:900/myapp",				"https://myhost.example.com:900/myapp",		new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com:900/",					/*"https://myhost.example.com:900/"*/"https://myhost.example.com:900",			new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com:900",					"https://myhost.example.com:900",			new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com/",						/*"https://myhost.example.com/"*/"https://myhost.example.com",					new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com/myapp",					"https://myhost.example.com/myapp",			new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com/myapp/",				"https://myhost.example.com/myapp/",		new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost.example.com",						"https://myhost.example.com",				new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost-examp_e.com",						"https://myhost-examp_e.com",				new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"myhost",									"https://myhost",							new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"/myapp",									"https://"+defaultHostname+"/myapp",		new Integer(0),		null,			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							":900/myapp",								"https://"+defaultHostname+":900/myapp",	new Integer(0),		null,			null}));
 
 		// ignored tests
 		ll.add(Arrays.asList(new Object[] {	null,							"",											ll.get(ll.size()-1).get(2)/* last set */,	new Integer(0),		null,			null}));	
 	
 		// negative tests
 		ll.add(Arrays.asList(new Object[] {	null,							"https:/hostname",							null,			new Integer(255),	"Error parsing baseurl: Server URL has an invalid scheme. http:// and https:// are supported",			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"https:hostname/prefix",					null,			new Integer(255),	"Error parsing baseurl: Server URL has an invalid scheme. http:// and https:// are supported",			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"http//hostname/prefix",					null,			new Integer(255),	"Error parsing baseurl: Server URL has an invalid scheme. http:// and https:// are supported",			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"http/hostname/prefix",						null,			new Integer(255),	"Error parsing baseurl: Server URL has an invalid scheme. http:// and https:// are supported",			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"ftp://hostname",							null,			new Integer(255),	"Error parsing baseurl: Server URL has an invalid scheme. http:// and https:// are supported",			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"git://hostname/prefix",					null,			new Integer(255),	"Error parsing baseurl: Server URL has an invalid scheme. http:// and https:// are supported",			null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"https://hostname:/prefix",					null,			new Integer(255),	"Error parsing baseurl: Server url port could not be parsed",											null}));
 		ll.add(Arrays.asList(new Object[] {	new BlockedByBzBug("842845"),	"https://hostname:PORT/prefix",				null,			new Integer(255),	"Error parsing baseurl: Server url port should be numeric",												null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"https://",									null,			new Integer(255),	"Error parsing baseurl: Server URL is just a schema. Should include hostname, and/or port and path",	null}));
 		ll.add(Arrays.asList(new Object[] {	null,							"http://",									null,			new Integer(255),	"Error parsing baseurl: Server URL is just a schema. Should include hostname, and/or port and path",	null}));
 		return ll;
 	}
 	
 	
 	
 	protected String server_hostname = null;
 	protected String server_port = null;
 	protected String server_prefix = null;
 	@BeforeGroups(value={"RegisterWithServerurl_Test"}, groups={"setup"})
 	public void beforeRegisterWithServerurl_Test() {
 		if (clienttasks==null) return;
 		server_hostname	= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "hostname");
 		server_port		= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "port");
 		server_prefix	= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "prefix");
 	}
 	@Test(	description="subscription-manager-cli: register with --serverurl",
 			dataProvider="getServerurl_TestData",
 			groups={"RegisterWithServerurl_Test","AcceptanceTests"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithServerurl_Test(Object bugzilla, String serverurl, String expectedHostname, String expectedPort, String expectedPrefix, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrMatch) {
 		// get original server at the beginning of this test
 		String hostnameBeforeTest	= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "hostname");
 		String portBeforeTest		= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "port");
 		String prefixBeforeTest		= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "prefix");
 		
 		// register with a serverurl
 		SSHCommandResult sshCommandResult = clienttasks.register_(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, false, null, null, (String)null, serverurl, null, true, null, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null)	Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode,"ExitCode after register with --serverurl="+serverurl+" and other options:");
 		if (expectedStdoutRegex!=null)	Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after register with --serverurl="+serverurl+" and other options:");
 		if (expectedStderrMatch!=null)	Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrMatch,"Stderr after register with --serverurl="+serverurl+" and other options:");
 		Assert.assertContainsNoMatch(sshCommandResult.getStderr().trim(), "Traceback.*","Stderr after register with --serverurl="+serverurl+" and other options should not contain a Traceback.");
 		
 		// negative testcase assertions........
 		if (expectedExitCode.equals(new Integer(255))) {
 			// assert that the current config remains unchanged when the expectedExitCode is 255
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "hostname"), hostnameBeforeTest, "The "+clienttasks.rhsmConfFile+" configuration for [server] hostname should remain unchanged when attempting to register with an invalid serverurl.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "port"),	portBeforeTest, "The "+clienttasks.rhsmConfFile+" configuration for [server] port should remain unchanged when attempting to register with an invalid serverurl.");
 			Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "prefix"), prefixBeforeTest, "The "+clienttasks.rhsmConfFile+" configuration for [server] prefix should remain unchanged when attempting to register with an invalid serverurl.");
 			
 			return;	// nothing more to do after these negative testcase assertions;
 		}
 		
 		// positive testcase assertions........
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "hostname"), expectedHostname, "The "+clienttasks.rhsmConfFile+" configuration for [server] hostname has been updated from the specified --serverurl "+serverurl);
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "port"), expectedPort, "The "+clienttasks.rhsmConfFile+" configuration for [server] port has been updated from the specified --serverurl "+serverurl);
 		Assert.assertEquals(clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "prefix"), expectedPrefix, "The "+clienttasks.rhsmConfFile+" configuration for [server] prefix has been updated from the specified --serverurl "+serverurl);
 	}
 	@AfterGroups(value={"RegisterWithServerurl_Test"}, groups={"setup"})
 	public void afterRegisterWithServerurl_Test() {
 		if (server_hostname!=null)	clienttasks.config(null,null,true,new String[]{"server","hostname",server_hostname});
 		if (server_port!=null)		clienttasks.config(null,null,true,new String[]{"server","port",server_port});
 		if (server_prefix!=null)	clienttasks.config(null,null,true,new String[]{"server","prefix",server_prefix});
 	}
 	
 	
 	@Test(	description="subscription-manager: register with --autosubscribe and --auto-attach can be used interchangably",
 			groups={"blockedByBug-874749","blockedByBug-874804"},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithAutoattachDeprecatesAutosubscribe_Test() throws Exception {
 		SSHCommandResult result = client.runCommandAndWait(clienttasks.command+" register --help");
 		Assert.assertContainsMatch(result.getStdout(), "^\\s*--autosubscribe\\s+Deprecated, see --auto-attach$");
 		
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult autosubscribeResult = clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, true, null, null, (String)null, null, null, null, false, null, null, null);
 		String autosubscribeResultAsString = autosubscribeResult.toString();
 		autosubscribeResultAsString = autosubscribeResultAsString.replaceAll("[a-f,0-9,\\-]{36}", "<UUID>");	// normalize the UUID so we can ignore it when asserting equal results
 		
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult autoattachResult = client.runCommandAndWait(clienttasks.command+" register --auto-attach --username=\""+sm_clientUsername+"\" --password=\""+sm_clientPassword+"\" " + (sm_clientOrg==null?"":"--org="+sm_clientOrg));
 		String autoattachResultAsString = autoattachResult.toString();
 		autoattachResultAsString = autoattachResultAsString.replaceAll("[a-f,0-9,\\-]{36}", "<UUID>");	// normalize the UUID so we can ignore it when asserting equal results
 		Assert.assertEquals(autosubscribeResultAsString, autoattachResultAsString, "Results from register with '--autosubscribe' and '--auto-attach' options should be identical.");
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 627685 - subscription-manager-cli reregister without specifying consumerid may unintentionally remove entitlements
 	// TODO Bug 627665 - subscription-manager-cli reregister should not allow a --username to reregister using a --consumerid that belongs to someone else
 	// TODO Bug 668814 - firstboot and subscription-manager display "network error" on server 500
 	// TODO Bug 669395 - gui defaults to consumer name of the hostname and doesn't let you set it to empty string. cli defaults to username, and does let you set it to empty string
 	// TODO Bug 693896 - subscription-manager does not always reload dbus scripts automatically //done
 	// TODO Bug 719378 - White space in user name causes error //done
 	
 	
 	// Protected Class Variables ***********************************************************************
 	
 	protected final String tmpProductCertDir = "/tmp/productCertDir";	// TODO Not being used anymore; DELETEME
 	protected String productCertDir = null;	// TODO Not being used anymore; DELETEME
 
 
 	
 	// Configuration methods ***********************************************************************
 
 	@AfterGroups(groups={"setup"}, value={"RegisterWithAutosubscribe_Test"})
 	@AfterClass (alwaysRun=true)
 	public void cleaupAfterClass() {
 		if (clienttasks==null) return;
 		
 		// restore the originally configured productCertDir
 		if (this.productCertDir!=null) clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", this.productCertDir);
 		
 		// delete temporary files and directories
 		client.runCommandAndWait("rm -rf "+tmpProductCertDir);
 	}
 
 	// Protected methods ***********************************************************************
 
 	protected void checkInvalidRegistrationStrings(SSHCommandRunner sshCommandRunner, String username, String password){
 		sshCommandRunner.runCommandAndWait("subscription-manager-cli register --username="+username+this.getRandInt()+" --password="+password+this.getRandInt()+" --force");
 		Assert.assertContainsMatch(sshCommandRunner.getStdout(),
 				"Invalid username or password. To create a login, please visit https:\\/\\/www.redhat.com\\/wapps\\/ugc\\/register.html");
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 
 }
