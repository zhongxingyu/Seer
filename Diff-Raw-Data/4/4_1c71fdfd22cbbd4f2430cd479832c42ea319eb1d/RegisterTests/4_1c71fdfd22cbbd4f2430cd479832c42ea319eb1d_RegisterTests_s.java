 package com.redhat.qe.sm.cli.tests;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.ConsumerCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 @Test(groups={"register"})
 public class RegisterTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager-cli: register to a Candlepin server",
 			groups={"RegisterWithUsernameAndPassword_Test"},
 			dataProvider="getUsernameAndPasswordData")
 	@ImplementsNitrateTest(cases={41677})
 	public void RegisterWithUsernameAndPassword_Test(String username, String password) {
 		log.info("Testing registration to a Candlepin using username="+username+" and password="+password);
 		
 		// determine this user's ability to register
 		SSHCommandResult registerResult = clienttasks.register_(username, password, null, null, null, null, null);
 			
 		// determine this user's available subscriptions
 		List<SubscriptionPool> allAvailableSubscriptionPools=null;
 		if (registerResult.getExitCode()==0) {
 			allAvailableSubscriptionPools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		}
 		
 		// determine this user's owner
 		JSONObject jsonOwner = null;
 		if (registerResult.getExitCode()==0) {
 			String consumerId = clienttasks.getCurrentConsumerId(registerResult);	// c48dc3dc-be1d-4b8d-8814-e594017d63c1 testuser1
 			try {
 				jsonOwner = CandlepinTasks.getOwnerOfConsumerId(serverHostname,serverPort,serverPrefix,serverAdminUsername,serverAdminPassword, consumerId);
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		RegistrationData userData = new RegistrationData(username,password,jsonOwner,registerResult,allAvailableSubscriptionPools);
 		registrationDataList.add(userData);
 		clienttasks.unregister_();
 		
 		Assert.assertEquals(registerResult.getExitCode(), Integer.valueOf(0), "The register command was a success.");
 		Assert.assertContainsMatch(registerResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+username);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using bogus credentials",
 			groups={},
 			dataProvider="getBogusRegistrationData")
 	@ImplementsNitrateTest(cases={41691, 47918})
 	public void Registration_Test(String username, String password, ConsumerType type, String name, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		log.info("Testing registration to a Candlepin using various options and data and asserting various expected results.");
 		
 		// ensure we are unregistered
 //DO NOT		clienttasks.unregister();
 		
 		// attempt the registration
 		SSHCommandResult sshCommandResult = clienttasks.register_(username, password, type, name, consumerId, autosubscribe, force);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode);
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex);
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register to a Candlepin server using bogus credentials and check for localized strings results",
 			groups={},
 			dataProvider="getInvalidRegistrationWithLocalizedStringsData")
 	@ImplementsNitrateTest(cases={41691})
 	public void AttemptRegistrationWithInvalidCredentials_Test(String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex) {
 
 		// ensure we are unregistered
 		clienttasks.unregister();
 		
 		log.info("Attempting to register to a candlepin server using invalid credentials and expecting output in language "+(lang==null?"DEFAULT":lang));
 		String command = String.format("%s %s register --username=%s --password=%s", lang==null?"":"LANG="+lang, clienttasks.command, username, password);
 		RemoteFileTasks.runCommandAndAssert(client, command, exitCode, stdoutRegex, stderrRegex);
 		
 		// assert that the consumer cert and key have NOT been dropped
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client,clienttasks.consumerKeyFile),0, "Consumer key file '"+clienttasks.consumerKeyFile+"' does NOT exist after an attempt to register with invalid credentials.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client,clienttasks.consumerCertFile),0, "Consumer cert file '"+clienttasks.consumerCertFile+" does NOT exist after an attempt to register with invalid credentials.");
 	}
 	
 
 	@Test(	description="subscription-manager-cli: attempt to register a user who has unaccepted Terms and Conditions",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(cases={48502})
 	public void AttemptRegistrationWithUnacceptedTermsAndConditions_Test() {
 		String username = usernameWithUnacceptedTC;
 		String password = passwordWithUnacceptedTC;
 		if (username.equals("")) throw new SkipException("Must specify a username who has not accepted Terms & Conditions before attempting this test.");
 		AttemptRegistrationWithInvalidCredentials_Test(null,username,password,255, null, "You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register a user who has been disabled",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(cases={50210})
 	public void AttemptRegistrationWithDisabledUserCredentials_Test() {
 		String username = disabledUsername;
 		String password = disabledPassword;
 		if (username.equals("")) throw new SkipException("Must specify a username who has been disabled before attempting this test.");
 		AttemptRegistrationWithInvalidCredentials_Test(null,username,password,255, null,"The user has been disabled, if this is a mistake, please contact customer service.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 			groups={"blockedByBug-602378", "blockedByBug-616137"},
 			enabled=true)
 	public void RegisterWithAutosubscribe_Test() {
 
 		log.info("RegisterWithAutosubscribe_Test Strategy:");
 		log.info(" For DEV and QA testing purposes, we may not have valid products installed on the client, therefore we will fake an installed product by following this strategy:");
 		log.info(" 1. Register and subscribe to a randomly available pool");
 		log.info(" 2. copy the downloaded entitlement cert to the product cert directory (this will fake rhsm into believing that the same product is installed)");
 		log.info(" 3. reregister with autosubscribe and assert that the product is bound");
 
 		// Register and subscribe to a randomly available pool
 		clienttasks.unregister();
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		fakeProductCertFile = new File(clienttasks.productCertDir+File.separator+"FAKE_"+entitlementCertFile.getName());
 		
 		// copy the downloaded entitlement cert to the product cert directory (this will fake rhsm into believing that the same product is installed)
 		cleaupAfterClass(); // removes the fake product cert file
 		RemoteFileTasks.runCommandAndAssert(client, "cp "+entitlementCertFile.getPath()+" "+fakeProductCertFile, Integer.valueOf(0));
 		ProductCert fakeProductCert = clienttasks.getProductCertFromProductCertFile(fakeProductCertFile);
 		
 		// reregister with autosubscribe and assert that the product is bound
 		clienttasks.unregister();
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE, null);
 		
 		// assert that the sshCommandResult from register indicates the fakeProductCert was subscribed
 		/* Sample sshCommandResult.getStdout():
 		 * d67df9c8-f381-4449-9d17-56094ea58092 testuser1
 		 * Subscribed to Products:
 		 *      RHEL for Physical Servers SVC(37060)
 		 */
 		String productName = fakeProductCert.productName;
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "register with autotosubscribe appears to have subscribed to something");
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^\\s+"+productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"), "Expected ProductName '"+productName+"' was reported as autosubscribed in the output from register with autotosubscribe.");
 
 		// assert that the productName is consumed
 		ProductSubscription productSubscription = clienttasks.findProductSubscriptionWithMatchingFieldFromList("productName", productName, clienttasks.getCurrentlyConsumedProductSubscriptions());
 		Assert.assertNotNull(productSubscription, "Expected ProductSubscription with ProductName '"+productName+"' is consumed after registering with autosubscribe.");
 
 		// assert that the productName is installed and subscribed
 		InstalledProduct installedProduct = clienttasks.findInstalledProductWithMatchingFieldFromList("productName", productName, clienttasks.getCurrentlyInstalledProducts());
 		Assert.assertNotNull(installedProduct, "The status of expected product with ProductName '"+productName+"' is reported in the list of installed products.");
 		Assert.assertEquals(installedProduct.status, "Subscribed", "After registering with autosubscribe, the status of Installed Product '"+productName+"' is Subscribed.");
 	}
 
 	
 	@Test(	description="subscription-manager-cli: register with --force",
 			groups={"blockedByBug-623264"},
 			enabled=true)
 	public void RegisterWithForce_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister();
 		
 		// make sure you are first registered
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null);
 		String firstConsumerId = clienttasks.getCurrentConsumerId();
 		
 		// subscribe to a random pool (so as to consume an entitlement)
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// attempt to register again and assert that you are warned that the system is already registered
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null);
 		Assert.assertTrue(sshCommandResult.getStdout().startsWith("This system is already registered."),"Expecting: This system is already registered.");
 		
 		// register with force
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE);
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
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(cases={62352})
 	public void RegisterWithName_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister();
 		
 		// register with a name
 		String name = "RegisterWithName_Tester";
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,null,name,null,null, null);
 		
 		// assert the stdout reflects the register name
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+name,"Stdout from register with --name value of "+name);
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
 			groups={"blockedByBug-636843"},
 			enabled=true)
 	@ImplementsNitrateTest(cases={56327})
 	public void ReregisterBasicRegistration_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		String consumerIdBefore = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null));
 		
 		// take note of your identity cert before reregister
 		ConsumerCert consumerCertBefore = clienttasks.getCurrentConsumerCert();
 		
 		// subscribe to a random pool
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// get a list of the consumed products
 		List<ProductSubscription> consumedProductSubscriptionsBefore = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		
 		// reregister
 		//clienttasks.reregister(null,null,null);
 		clienttasks.reregisterToExistingConsumer(clientusername,clientpassword,consumerIdBefore);
 		
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
 			groups={"blockedByBug-624106"},
 			enabled=true)
 	@ImplementsNitrateTest(cases={56328})
 	public void ReregisterWithBadIdentityCert_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister();
 		clienttasks.register(clientusername,clientpassword,null,null,null,null, null);
 		
 		// take note of your identity cert
 		ConsumerCert consumerCertBefore = clienttasks.getCurrentConsumerCert();
 		
 		// subscribe to a random pool
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// get a list of the consumed products
 		List<ProductSubscription> consumedProductSubscriptionsBefore = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		
 		// Now.. mess up your identity..  by borking its content
 		log.info("Messing up the identity cert by borking its content...");
 		RemoteFileTasks.runCommandAndAssert(client, "openssl x509 -noout -text -in "+clienttasks.consumerCertFile+" > /tmp/stdout; mv /tmp/stdout -f "+clienttasks.consumerCertFile, 0);
 		
 		// reregister w/ username, password, and consumerid
 		//clienttasks.reregister(client1username,client1password,consumerCertBefore.consumerid);
 		log.warning("The subscription-manager-cli reregister module has been eliminated and replaced by register --consumerid (b3c728183c7259841100eeacb7754c727dc523cd)...");
 		clienttasks.register(clientusername,clientpassword,null,null,consumerCertBefore.consumerid,null, Boolean.TRUE);
 		
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
 	
 // TODO Automation Candidates for Reregister tests: 
 //		https://bugzilla.redhat.com/show_bug.cgi?id=627685
 //		https://bugzilla.redhat.com/show_bug.cgi?id=627665
 	
 	
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	File fakeProductCertFile = null;
 	@AfterClass (alwaysRun=true)
 	public void cleaupAfterClass() {
 		if (fakeProductCertFile!=null) {
 			client.runCommandAndWait("rm -f "+fakeProductCertFile);
 		}
 	}
 	
 	@BeforeGroups(value={"RegisterWithUsernameAndPassword_Test"},alwaysRun=true)
 	public void unregisterBeforeRegisterWithUsernameAndPassword_Test() {
 		clienttasks.unregister_();
 	}
 	@AfterGroups(value={"RegisterWithUsernameAndPassword_Test"},alwaysRun=true)
 	public void generateRegistrationReportTableAfterRegisterWithUsernameAndPassword_Test() {
 		
 		// now dump out the list of userData to a file
 	    File file = new File("CandlepinRegistrationReport.html"); // this will be in the workspace directory on hudson
 	    DateFormat dateFormat = new SimpleDateFormat("MMM d HH:mm:ss yyyy z");
 	    try {
 	    	Writer output = new BufferedWriter(new FileWriter(file));
 			
 			// write out the rows of the table
 			output.write("<html>\n");
 			output.write("<table border=1>\n");
 			output.write("<h2>Candlepin Registration Report</h2>");
 			//output.write("<h3>(generated on "+dateFormat.format(System.currentTimeMillis())+")</h3>");
 			output.write("Candlepin hostname= <b>"+serverHostname+"</b>\n");
 			output.write("(generated on "+dateFormat.format(System.currentTimeMillis())+")\n");
 			output.write("<tr><th>Owner</th><th>Username/<BR>Password</th><th>Registration Output</th><th>All Available Subscriptions (to system consumers)</th></tr>\n");
 			for (RegistrationData registeredConsumer : registrationDataList) {
 				if (registeredConsumer.jsonOwner==null) {
 					output.write("<tr bgcolor=#F47777>");
 				} else {output.write("<tr>");}
 				if (registeredConsumer.jsonOwner!=null) {
 					output.write("<td valign=top>"+registeredConsumer.jsonOwner.getString("key")+"</td>");
 				} else {output.write("<td/>");};
 				if (registeredConsumer.username!=null) {
 					output.write("<td valign=top>"+registeredConsumer.username+"/<BR>"+registeredConsumer.password+"</td>");
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
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	// Protected methods ***********************************************************************
 
 	protected void checkInvalidRegistrationStrings(SSHCommandRunner sshCommandRunner, String username, String password){
 		sshCommandRunner.runCommandAndWait("subscription-manager-cli register --username="+username+this.getRandInt()+" --password="+password+this.getRandInt()+" --force");
 		Assert.assertContainsMatch(sshCommandRunner.getStdout(),
 				"Invalid username or password. To create a login, please visit https:\\/\\/www.redhat.com\\/wapps\\/ugc\\/register.html");
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="getBogusRegistrationData")
 	public Object[][] getBogusRegistrationDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getBogusRegistrationDataAsListOfLists());
 	}
 	protected List<List<Object>> getBogusRegistrationDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		// String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer exitCode, String stdoutRegex, String stderrRegex
 		// 									username,			password,						type,	name,	consumerId,	autosubscribe,	force,			debug,	exitCode,				stdoutRegex,																	stderrRegex
 		ll.add(Arrays.asList(new Object[]{	"",					"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
		ll.add(Arrays.asList(new Object[]{	clientusername,		"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
		ll.add(Arrays.asList(new Object[]{	"",					clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			"Invalid username or password"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername+"X",	String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			"Invalid username or password"}));
 		ll.add(Arrays.asList(new Object[]{	clientusername,		String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			"Invalid username or password"}));
 
 		// force a successful registration, and then...
 		// FIXME: https://bugzilla.redhat.com/show_bug.cgi?id=616065
 		ll.add(Arrays.asList(new Object[]{	clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"[a-f,0-9,\\-]{36} "+clientusername,											null}));	// https://bugzilla.redhat.com/show_bug.cgi?id=616065
 
 		// ... try to register again even though the system is already registered
 		ll.add(Arrays.asList(new Object[]{	clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.FALSE,	null,	Integer.valueOf(1),		"This system is already registered. Use --force to override",					null}));
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getInvalidRegistrationWithLocalizedStringsData")
 	public Object[][] getInvalidRegistrationWithLocalizedStringsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationWithLocalizedStringsAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationWithLocalizedStringsAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 
 		// String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex
 		
 		// registration test for a user who is invalid
 		ll.add(Arrays.asList(new Object[]{"en_US.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Invalid username or password":"Invalid username or password. To create a login, please visit https://www.redhat.com/wapps/ugc/register.html"}));
 		
 		// registration test for a user who is invalid (translated)
 		if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"},"de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültiger Benutzername oder Kennwort":"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc")}));
 		else                     ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362",                       "de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültiger Benutzername oder Kennwort":"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc")}));
 
 		// registration test for a user who has not accepted Red Hat's Terms and conditions (translated)  Man, why did you do something?
 		if (!usernameWithUnacceptedTC.equals("")) {
 			if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"},"de_DE.UTF8", usernameWithUnacceptedTC, passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!")}));
 			else                     ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362",                       "de_DE.UTF8", usernameWithUnacceptedTC, passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!")}));
 		}
 		
 		// registration test for a user who has been disabled (translated)
 		if (!disabledUsername.equals("")) {
 			ll.add(Arrays.asList(new Object[]{"en_US.UTF8", disabledUsername, disabledPassword, 255, null,"The user has been disabled, if this is a mistake, please contact customer service."}));
 		}
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getUsernameAndPasswordData")
 	public Object[][] getUsernameAndPasswordDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUsernameAndPasswordDataAsListOfLists());
 	}
 	protected List<List<Object>> getUsernameAndPasswordDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		String[] usernames = clientUsernames.split(",");
 		String[] passwords = clientPasswords.split(",");
 		String password = passwords[0].trim();
 		for (int i = 0; i < usernames.length; i++) {
 			String username = usernames[i].trim();
 			// when there is not a 1:1 relationship between usernames and passwords, the last password is repeated
 			// this allows one to specify only one password when all the usernames share the same password
 			if (i<passwords.length) password = passwords[i].trim();
 			ll.add(Arrays.asList(new Object[]{username,password}));
 		}
 		
 		return ll;
 	}
 }
