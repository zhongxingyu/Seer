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
 
 import org.json.JSONArray;
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
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.ConsumerCert;
 import com.redhat.qe.sm.data.EntitlementCert;
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
 @Test(groups={"RegisterTests"})
 public class RegisterTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager-cli: register to a Candlepin server",
 			groups={"RegisterWithUsernameAndPassword_Test"},
 			dataProvider="getUsernameAndPasswordData")
 	@ImplementsNitrateTest(caseId=41677)
 	public void RegisterWithUsernameAndPassword_Test(String username, String password) {
 		log.info("Testing registration to a Candlepin using username="+username+" and password="+password);
 		
 		// determine this user's ability to register
 		SSHCommandResult registerResult = clienttasks.register_(username, password, null, null, null, null, null, null, null, null);
 			
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
 				ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(serverHostname,serverPort,serverPrefix,username,password, consumerId);
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
 		
 		Assert.assertEquals(registerResult.getExitCode(), Integer.valueOf(0), "The register command was a success.");
 		Assert.assertContainsMatch(registerResult.getStdout().trim(), "[a-f,0-9,\\-]{36} "+/*username*/clienttasks.hostname);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using bogus credentials",
 			groups={},
 			dataProvider="getBogusRegistrationData")
 //	@ImplementsNitrateTest(caseId={41691, 47918})
 	@ImplementsNitrateTest(caseId=47918)
 	public void Registration_Test(Object meta, String username, String password, ConsumerType type, String name, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		log.info("Testing registration to a Candlepin using various options and data and asserting various expected results.");
 		
 		// ensure we are unregistered
 //DO NOT		clienttasks.unregister();
 		
 		// attempt the registration
 		SSHCommandResult sshCommandResult = clienttasks.register_(username, password, type, name, consumerId, autosubscribe, force, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode);
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex);
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register to a Candlepin server using bogus credentials and check for localized strings results",
 			groups={},
 			dataProvider="getInvalidRegistrationWithLocalizedStringsData")
 	@ImplementsNitrateTest(caseId=41691)
 	public void AttemptRegistrationWithInvalidCredentials_Test(Object meta, String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex) {
 
 		// ensure we are unregistered
 		clienttasks.unregister(null, null, null);
 		
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
 	@ImplementsNitrateTest(caseId=48502)
 	public void AttemptRegistrationWithUnacceptedTermsAndConditions_Test() {
 		String username = usernameWithUnacceptedTC;
 		String password = passwordWithUnacceptedTC;
 		if (username.equals("")) throw new SkipException("Must specify a username who has not accepted Terms & Conditions before attempting this test.");
 		AttemptRegistrationWithInvalidCredentials_Test(null, null,username,password,255, null, "You must first accept Red Hat's Terms and conditions. Please visit https://www.redhat.com/wapps/ugc");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to register a user who has been disabled",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=50210)
 	public void AttemptRegistrationWithDisabledUserCredentials_Test() {
 		String username = disabledUsername;
 		String password = disabledPassword;
 		if (username.equals("")) throw new SkipException("Must specify a username who has been disabled before attempting this test.");
 		AttemptRegistrationWithInvalidCredentials_Test(null, null,username,password,255, null,"The user has been disabled, if this is a mistake, please contact customer service.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: register to a Candlepin server using autosubscribe functionality",
 			groups={"RegisterWithAutosubscribe_Test","blockedByBug-602378", "blockedByBug-616137", "blockedByBug-678049"},
 			enabled=true)
 	public void RegisterWithAutosubscribe_Test() throws JSONException, Exception {
 
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
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", tmpProductCertDir);
 
 		// Register and assert that no products appear to be installed since we changed the productCertDir to a temporary
 		clienttasks.unregister(null, null, null);
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE, null, null, null, null);
 		// pre-fix for blockedByBug-678049 Assert.assertContainsNoMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "register with autotosubscribe should NOT appear to have subscribed to something when there are no installed products.");
 		Assert.assertContainsNoMatch(sshCommandResult.getStdout().trim(), "^Installed Products:", "register with autotosubscribe should NOT list the status of installed products when there are no installed products.");
 		Assert.assertEquals(clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null).getStdout().trim(),"No installed Products to list",
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
 			JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/pools/"+pool.poolId));	
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
 		sshCommandResult = clienttasks.register(clientusername, clientpassword, null, null, null, Boolean.TRUE, null, null, null, null);
 		
 		// assert that the sshCommandResult from register indicates the fakeProductCert was subscribed
 		/* pre-fix for blockedByBug-678049 
 		 * Sample sshCommandResult.getStdout():
 		 * d67df9c8-f381-4449-9d17-56094ea58092 testuser1
 		 * Subscribed to Products:
 		 *      RHEL for Physical Servers SVC(37060)
 		 *      Red Hat Enterprise Linux High Availability (for RHEL Entitlement)(4)
 		 */
 		 /*
 		  * Sample sshCommandResult.getStdout():
 		  * cadf825a-6695-41e3-b9eb-13d7344159d3 jsefler-onprem03.usersys.redhat.com
 		  * Installed Products:
 		  *    Clustering Bits - Subscribed
 		  *    Awesome OS Server Bits - Not Installed
 		  *    Shared Storage Bits - Not Installed
 		  *    Management Bits - Not Installed
 		  *    Large File Support Bits - Not Installed
 		  *    Load Balancing Bits - Not Installed
 		  */
 
 		// assert that our fake product install appears to have been autosubscribed
 		InstalledProduct autoSubscribedProduct = InstalledProduct.findFirstInstanceWithMatchingFieldFromList("status", "Subscribed", InstalledProduct.parse(clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null).getStdout()));
 		Assert.assertNotNull(autoSubscribedProduct,	"We appear to have autosubscribed to our fake product install.");
 		// pre-fix for blockedByBug-678049 Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^Subscribed to Products:", "The stdout from register with autotosubscribe indicates that we have subscribed to something");
 		// pre-fix for blockedByBug-678049 Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^\\s+"+autoSubscribedProduct.productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)"), "Expected ProductName '"+autoSubscribedProduct.productName+"' was reported as autosubscribed in the output from register with autotosubscribe.");
 		Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), ".* - Subscribed", "The stdout from register with autotosubscribe indicates that we have automatically subscribed at least one of this system's installed products to an available subscription pool.");
 		// FIXME The following two asserts lead to misleading failures when the entitlementCertFile that we using to fake as a tmpProductCertFile happens to have multiple bundled products inside.
 		//Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), "^\\s+"+autoSubscribedProduct.productName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")+" - Subscribed", "Expected ProductName '"+autoSubscribedProduct.productName+"' was reported as autosubscribed in the output from register with autotosubscribe.");
 		//Assert.assertNotNull(ProductSubscription.findFirstInstanceWithMatchingFieldFromList("productName", autoSubscribedProduct.productName, clienttasks.getCurrentlyConsumedProductSubscriptions()),"Expected ProductSubscription with ProductName '"+autoSubscribedProduct.productName+"' is consumed after registering with autosubscribe.");
 	}
 
 	
 	@Test(	description="subscription-manager-cli: register with --force",
 			groups={"blockedByBug-623264"},
 			enabled=true)
 	public void RegisterWithForce_Test() {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// make sure you are first registered
 		SSHCommandResult sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null, null, null, null);
 		String firstConsumerId = clienttasks.getCurrentConsumerId();
 		
 		// subscribe to a random pool (so as to consume an entitlement)
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		if (pools.isEmpty()) throw new SkipException("Cannot randomly pick a pool for subscribing when there are no available pools for testing."); 
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// attempt to register again and assert that you are warned that the system is already registered
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,null, null, null, null);
 		Assert.assertTrue(sshCommandResult.getStdout().startsWith("This system is already registered."),"Expecting: This system is already registered.");
 		
 		// register with force
 		sshCommandResult = clienttasks.register(clientusername,clientpassword,null,null,null,null,Boolean.TRUE, null, null, null);
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
 			dataProvider="getRegisterWithNameData",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=62352) // caseIds=81089 81090 81091
 	public void RegisterWithName_Test(Object meta, String name, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// register with a name
 		SSHCommandResult sshCommandResult = clienttasks.register_(clientusername,clientpassword,null,name,null,null, null, null, null, null);
 		
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
 	
 	
 	@Test(	description="subscription-manager-cli: register with --name and --type",
 			dataProvider="getRegisterWithNameAndTypeData",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithNameAndType_Test(Object meta, String username, String password, String name, ConsumerType type, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// register with a name
 		SSHCommandResult sshCommandResult = clienttasks.register_(username,password,type,name,null,null, null, null, null, null);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode,"ExitCode after register with --name="+name+" --type="+type+" options:");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout().trim(), expectedStdoutRegex,"Stdout after register with --name="+name+" --type="+type+" options:");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr().trim(), expectedStderrRegex,"Stderr after register with --name="+name+" --type="+type+" options:");
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
 	@ImplementsNitrateTest(caseId=56327)
 	public void ReregisterBasicRegistration_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister(null, null, null);
 		String consumerIdBefore = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null));
 		
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
 	@ImplementsNitrateTest(caseId=56328)
 	public void ReregisterWithBadIdentityCert_Test() {
 		
 		// start fresh by unregistering and registering
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null);
 		
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
 		RemoteFileTasks.runCommandAndAssert(client, "openssl x509 -noout -text -in "+clienttasks.consumerCertFile+" > /tmp/stdout; mv /tmp/stdout -f "+clienttasks.consumerCertFile, 0);
 		
 		// reregister w/ username, password, and consumerid
 		//clienttasks.reregister(client1username,client1password,consumerCertBefore.consumerid);
 		log.warning("The subscription-manager-cli reregister module has been eliminated and replaced by register --consumerid (b3c728183c7259841100eeacb7754c727dc523cd)...");
 		clienttasks.register(clientusername,clientpassword,null,null,consumerCertBefore.consumerid,null, Boolean.TRUE, null, null, null);
 		
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
 
 	 */
 	@Test(	description="register with existing consumerid should automatically refresh entitlements",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=72845)
 	public void ReregisterWithConsumerIdShouldAutomaticallyRefreshEntitlements_Test() {
 		
 		// register with username and password and remember the consumerid
 		clienttasks.unregister(null, null, null);
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(clientusername,clientpassword,null,null,null,null, null, null, null, null));
 		
 		// subscribe to one or more subscriptions
 		//// subscribe to a random pool
 		//List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		//SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		//clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 
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
 		clienttasks.register(clientusername,clientpassword,null,null,consumerId,null, null, null, null, null);
 
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
 		client.runCommandAndWait("echo \"\" > "+clienttasks.rhsmFactsJsonFile, LogMessageUtil.action());
 		SSHCommandResult result = client.runCommandAndWait("cat "+clienttasks.rhsmFactsJsonFile, LogMessageUtil.action());
 		Assert.assertTrue(result.getStdout().trim().equals(""), "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' is empty.");
 		
 		log.info("Attempt to register with an empty rhsm facts file (expecting success)...");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null);
 	}
 	
 	
 	@Test(	description="register with a missing /var/lib/rhsm/facts/facts.json file",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	
 	public void RegisterWithAnMissingRhsmFactsJsonFile_Test() {
 		
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==1, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' exists");
 		log.info("Deleting rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"'...");
 		RemoteFileTasks.runCommandAndWait(client, "rm -f "+clienttasks.rhsmFactsJsonFile, LogMessageUtil.action());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmFactsJsonFile)==0, "rhsm facts json file '"+clienttasks.rhsmFactsJsonFile+"' has been removed");
 		
 		log.info("Attempt to register with a missing rhsm facts file (expecting success)...");
 		clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null);
 	}
 	
 	
 	@Test(	description="register with interactive prompting for credentials",
 			groups={"blockedByBug-678151"},
 			dataProvider = "getInteractiveRegistrationData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void RegisterWithInteractivePromptingForUsername_Test(Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 		
 		// ensure we are unregistered
 		clienttasks.unregister(null,null,null);
 
 		// register while providing a valid username at the interactive prompt
 		// assemble an ssh command using echo and pipe to simulate an interactively supply of credentials to the register command
 		String echoUsername= promptedUsername==null?"":promptedUsername;
 		String echoPassword = promptedPassword==null?"":promptedPassword;
 		String n = (promptedPassword!=null&&promptedUsername!=null)? "\n":"";
 		String command = String.format("echo -e \"%s\" | %s register %s %s",
 				echoUsername+n+echoPassword,
 				clienttasks.command,
 				commandLineUsername==null?"":"--username="+commandLineUsername,
 				commandLinePassword==null?"":"--password="+commandLinePassword);
 		
 		// attempt to register with the interactive credentials
 		SSHCommandResult sshCommandResult = client.runCommandAndWait(command);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode, "The expected exit code from the register attempt.");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout(), expectedStdoutRegex, "The expected stdout result from register while supplying interactive credentials.");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr(), expectedStderrRegex, "The expected stderr result from register while supplying interactive credentials.");
 	}
 	
 	
 	@Test(	description="User is warned when already registered using RHN Classic",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=75972)	
 	public void InteroperabilityRegister_Test() {
 
 		final String interoperabilityWarningMessage = 
 			"WARNING" +"\n\n"+
 			"You have already registered with RHN using RHN Classic technology. This tool requires registration using RHN Certificate-Based Entitlement technology." +"\n\n"+
 			"Except for a few cases, Red Hat recommends customers only register with RHN once." +"\n\n"+
 			"For more information, including alternate tools, consult this Knowledge Base Article: https://access.redhat.com/kb/docs/DOC-45563";
 
 		log.info("Simulating registration by RHN Classic by creating an empty systemid file '"+clienttasks.rhnSystemIdFile+"'...");
 		RemoteFileTasks.runCommandAndWait(client, "touch "+clienttasks.rhnSystemIdFile, LogMessageUtil.action());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhnSystemIdFile)==1, "RHN Classic systemid file '"+clienttasks.rhnSystemIdFile+"' is in place.");
 		
 		log.info("Attempt to register while already registered via RHN Classic...");
 		SSHCommandResult result = clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null);
 		//Assert.assertTrue(result.getStdout().startsWith(interoperabilityWarningMessage), "subscription-manager warns the registerer when the system is already registered via RHN Classic with this expected message:\n"+interoperabilityWarningMessage);
 		Assert.assertContainsMatch(result.getStdout(),"^"+interoperabilityWarningMessage, "subscription-manager warns the registerer when the system is already registered via RHN Classic with the expected message.");
 
 		log.info("Now let's make sure we are NOT warned when we are NOT already registered via RHN Classic...");
 		RemoteFileTasks.runCommandAndWait(client, "rm -rf "+clienttasks.rhnSystemIdFile, LogMessageUtil.action());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhnSystemIdFile)==0, "RHN Classic systemid file '"+clienttasks.rhnSystemIdFile+"' is gone.");
 		result = clienttasks.register(clientusername, clientpassword, null, null, null, null, Boolean.TRUE, null, null, null);
 		//Assert.assertFalse(result.getStdout().startsWith(interoperabilityWarningMessage), "subscription-manager does NOT warn registerer when the system is not already registered via RHN Classic.");
 		Assert.assertContainsNoMatch(result.getStdout(),interoperabilityWarningMessage, "subscription-manager does NOT warn registerer when the system is NOT already registered via RHN Classic.");
 	}
 	
 
 	
 	// Candidates for an automated Test:
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=627685
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=627665
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=668814
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=669395
 
 	
 	
 	
 
 	// Protected Class Variables ***********************************************************************
 	
 	protected final String tmpProductCertDir = "/tmp/productCertDir";
 	protected String productCertDir = null;
 	
 	// Configuration methods ***********************************************************************
 
 	@BeforeGroups(value={"RegisterWithAutosubscribe_Test"})
 	public void storeProductCertDir() {
 		this.productCertDir = clienttasks.productCertDir;
 	}
 	@AfterGroups(value={"RegisterWithAutosubscribe_Test"},alwaysRun=true)
 	@AfterClass (alwaysRun=true)
 	public void cleaupAfterClass() {
 		if (clienttasks==null) return;
 		if (this.productCertDir!=null) clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", this.productCertDir);
 		client.runCommandAndWait("rm -rf "+tmpProductCertDir);
 		client.runCommandAndWait("rm -rf "+clienttasks.rhnSystemIdFile);
 	}
 
 	
 	@BeforeGroups(value={"RegisterWithUsernameAndPassword_Test"},alwaysRun=true)
 	public void unregisterBeforeRegisterWithUsernameAndPassword_Test() {
 		if (clienttasks==null) return;
 		clienttasks.unregister_(null, null, null);
 	}
 	@AfterGroups(value={"RegisterWithUsernameAndPassword_Test"},alwaysRun=true)
 	public void generateRegistrationReportTableAfterRegisterWithUsernameAndPassword_Test() {
 		
 		// now dump out the list of userData to a file
 	    File file = new File("CandlepinRegistrationReport.html"); // this will be in the automation.dir directory on hudson (workspace/automatjon/sm)
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
 				if (registeredConsumer.ownerKey==null) {
 					output.write("<tr bgcolor=#F47777>");
 				} else {output.write("<tr>");}
 				if (registeredConsumer.ownerKey!=null) {
 					output.write("<td valign=top>"+registeredConsumer.ownerKey+"</td>");
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
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 
 		// Object bugzilla, String username, String password, String type, String consumerId, Boolean autosubscribe, Boolean force, String debug, Integer exitCode, String stdoutRegex, String stderrRegex
 		ll.add(Arrays.asList(new Object[] {null,	clientusername,					String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {null,	clientusername+getRandInt(),	String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {null,	clientusername,					String.valueOf(getRandInt()),	null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	null,																			uErrMsg}));
 
 		// force a successful registration, and then...
 		ll.add(Arrays.asList(new Object[]{	new BlockedByBzBug(new String[]{"616065","669395"}),
 													clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(0),		"[a-f,0-9,\\-]{36} "+/*clientusername*/clienttasks.hostname,					null}));
 
 		// ... try to register again even though the system is already registered
 		ll.add(Arrays.asList(new Object[] {null,	clientusername,		clientpassword,					null,	null,	null,		null,			Boolean.FALSE,	null,	Integer.valueOf(1),		"This system is already registered. Use --force to override",					null}));
 
 		/* moving these testcases with missing username/password to a dedicated test due to behavior introduced by https://bugzilla.redhat.com/show_bug.cgi?id=678151
 		// moved to @DataProvider(name="getInteractiveRegistrationData")
 		ll.add(Arrays.asList(new Object[] {null,	"",					"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username and password are required to register, try register --help.",	null}));
 		ll.add(Arrays.asList(new Object[] {null,	clientusername,		"",								null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: password not provided. Use --password <value>",							null}));
 		ll.add(Arrays.asList(new Object[] {null,	"",					clientpassword,					null,	null,	null,		null,			Boolean.TRUE,	null,	Integer.valueOf(255),	"Error: username not provided. Use --username <name>",							null}));
 		*/
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getInteractiveRegistrationData")
 	public Object[][] getInteractiveRegistrationDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInteractiveRegistrationDataAsListOfLists());
 	}
 	protected List<List<Object>> getInteractiveRegistrationDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 		// Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 		ll.add(Arrays.asList(new Object[] {	null,	clientusername,					null,							null,			clientpassword,	new Integer(0),		"[a-f,0-9,\\-]{36} "+clienttasks.hostname,	null}));
 		ll.add(Arrays.asList(new Object[] {	null,	clientusername+getRandInt(),	null,							null,			clientpassword,	new Integer(255),	null,										uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {	null,	null,							clientpassword,					clientusername,	null,			new Integer(0),		"[a-f,0-9,\\-]{36} "+clienttasks.hostname,	null}));
 		ll.add(Arrays.asList(new Object[] {	null,	null,							clientpassword+getRandInt(),	clientusername,	null,			new Integer(255),	null,										uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {	null,	clientusername,					clientpassword,					null,			null,			new Integer(0),		"[a-f,0-9,\\-]{36} "+clienttasks.hostname,	null}));
 		ll.add(Arrays.asList(new Object[] {	null,	clientusername+getRandInt(),	clientpassword+getRandInt(),	null,			null,			new Integer(255),	null,										uErrMsg}));
 		ll.add(Arrays.asList(new Object[] {	null,	"\n\n"+clientusername,			"\n\n"+clientpassword,			null,			null,			new Integer(0),		"(Username: ){3}[a-f,0-9,\\-]{36} "+clienttasks.hostname,	"(Warning: Password input may be echoed.\nPassword: \n){3}"}));
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getInvalidRegistrationWithLocalizedStringsData")
 	public Object[][] getInvalidRegistrationWithLocalizedStringsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInvalidRegistrationWithLocalizedStringsAsListOfLists());
 	}
 	protected List<List<Object>> getInvalidRegistrationWithLocalizedStringsAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 
 		// String lang, String username, String password, Integer exitCode, String stdoutRegex, String stderrRegex
 		
 		// registration test for a user who is invalid
 		ll.add(Arrays.asList(new Object[]{null, "en_US.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, uErrMsg}));
 		
 		// registration test for a user who with "invalid credentials" (translated)
 		if (!isServerOnPremises)	ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"}),	"de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültige Berechtigungnachweise"/*"Ungültige Mandate"*//*"Ungültiger Benutzername oder Kennwort"*/:"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc"}));
 		else 						ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362"),                      	"de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültige Berechtigungnachweise"/*"Ungültige Mandate"*//*"Ungültiger Benutzername oder Kennwort"*/:"Ungültiger Benutzername oder Kennwort. So erstellen Sie ein Login, besuchen Sie bitte https://www.redhat.com/wapps/ugc"}));
 
 									ll.add(Arrays.asList(new Object[]{null,													"en_US.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Invalid Credentials":"Invalid Credentials"}));
 									ll.add(Arrays.asList(new Object[]{null,													"de_DE.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Ungültige Berechtigungnachweise":"Ungültige Berechtigungnachweise"}));
 									ll.add(Arrays.asList(new Object[]{null,													"es_ES.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Credenciales inválidas":"Credenciales inválidas"}));
 									ll.add(Arrays.asList(new Object[]{null,													"fr_FR.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Informations d’identification invalides":"Informations d’identification invalides"}));
 									ll.add(Arrays.asList(new Object[]{null,													"it_IT.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Credenziali invalide":"Credenziali invalide"}));
 									ll.add(Arrays.asList(new Object[]{null,													"ja_JP.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "無効な識別情報":"無効な識別情報"}));
 									ll.add(Arrays.asList(new Object[]{null,													"ko_KR.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "잘못된 인증 정보":"잘못된 인증 정보"}));
 									ll.add(Arrays.asList(new Object[]{null,													"pt_BR.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Credenciais inválidos":"Credenciais inválidos"}));
 									ll.add(Arrays.asList(new Object[]{null,													"ru_RU.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "Недопустимые реквизиты":"Недопустимые реквизиты"}));
 									ll.add(Arrays.asList(new Object[]{null,													"zh_CN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "无效证书":"无效证书"}));
 									ll.add(Arrays.asList(new Object[]{null,													"zh_TW.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "無效的認證":"無效的認證"}));
 									ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("683914"),							"as_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "অবৈধ পৰিচয়":"অবৈধ পৰিচয়"}));
 									ll.add(Arrays.asList(new Object[]{null,													"bn_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "অবৈধ পরিচয়":"অবৈধ পরিচয়"}));
 									ll.add(Arrays.asList(new Object[]{null,													"hi_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "अवैध श्रेय":"अवैध श्रेय"}));
 									ll.add(Arrays.asList(new Object[]{null,													"mr_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "अवैध श्रेय":"अवैध श्रेय"}));
 									ll.add(Arrays.asList(new Object[]{null,													"gu_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "અયોગ્ય શ્રેય":"અયોગ્ય શ્રેય"}));
 									ll.add(Arrays.asList(new Object[]{null,													"kn_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "ಅಮಾನ್ಯವಾದ ಪರಿಚಯಪತ್ರ":"ಅಮಾನ್ಯವಾದ ಪರಿಚಯಪತ್ರ"}));
 									ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("683914"),							"ml_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "തെറ്റായ ആധികാരികതകള്‍":"തെറ്റായ ആധികാരികതകള്‍"}));
 									ll.add(Arrays.asList(new Object[]{null,													"or_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "ଅବୈଧ ପ୍ରାଧିକରଣ":"ଅବୈଧ ପ୍ରାଧିକରଣ"}));
 									ll.add(Arrays.asList(new Object[]{null,													"pa_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "ਗਲਤ ਕਰੀਡੈਂਸ਼ਲ":"ਗਲਤ ਕਰੀਡੈਂਸ਼ਲ"}));
 									ll.add(Arrays.asList(new Object[]{null,													"ta_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "தவறான சான்றுகள்":"தவறான சான்றுகள்"}));
 									ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("683914"),							"te_IN.UTF8", clientusername+getRandInt(), clientpassword+getRandInt(), 255, null, isServerOnPremises? "చెల్లని ప్రమాణాలు":"చెల్లని ప్రమాణాలు"}));
 
 		// registration test for a user who has not accepted Red Hat's Terms and conditions (translated)  Man, why did you do something?
 		if (!usernameWithUnacceptedTC.equals("")) {
 			if (!isServerOnPremises) ll.add(Arrays.asList(new Object[]{new BlockedByBzBug(new String[]{"615362","642805"}),"de_DE.UTF8", usernameWithUnacceptedTC, passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!"}));
 			else                     ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("615362"),                       "de_DE.UTF8", usernameWithUnacceptedTC, passwordWithUnacceptedTC, 255, null, "Mensch, warum hast du auch etwas zu tun?? Bitte besuchen https://www.redhat.com/wapps/ugc!!!!!!!!!!!!!!!!!!"}));
 		}
 		
 		// registration test for a user who has been disabled (translated)
 		if (!disabledUsername.equals("")) {
 			ll.add(Arrays.asList(new Object[]{null, "en_US.UTF8", disabledUsername, disabledPassword, 255, null,"The user has been disabled, if this is a mistake, please contact customer service."}));
 		}
 		// [root@jsefler-onprem-server ~]# for l in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do echo ""; echo ""; echo "# LANG=$l.UTF8 subscription-manager clean --help"; LANG=$l.UTF8 subscription-manager clean --help; done;
 		/* TODO reference for locales
 		[root@jsefler-onprem03 ~]# rpm -lq subscription-manager | grep locale
 		/usr/share/locale/as_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/bn_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/de_DE/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/en_US/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/es_ES/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/fr_FR/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/gu_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/hi_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/it_IT/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ja_JP/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/kn_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ko_KR/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ml_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/mr_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/or_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/pa_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/pt_BR/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ru_RU/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/ta_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/te_IN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/zh_CN/LC_MESSAGES/rhsm.mo
 		/usr/share/locale/zh_TW/LC_MESSAGES/rhsm.mo
 		*/
 		
 		return ll;
 	}
 	
 
 	@DataProvider(name="getRegisterWithNameAndTypeData")
 	public Object[][] getRegisterWithNameAndTypeDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithNameAndTypeDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithNameAndTypeDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		String username=clientusername;
 		String password=clientpassword;
 
 		// flatten all the ConsumerType values into a comma separated list
 		String consumerTypesAsString = "";
 		for (ConsumerType type : ConsumerType.values()) consumerTypesAsString+=type+",";
 		consumerTypesAsString = consumerTypesAsString.replaceAll(",$", "");
 		
 		// interate across all ConsumerType values and append rows to the dataProvider
 		for (ConsumerType type : ConsumerType.values()) {
 			String name = type.toString()+"_NAME";
 			List <String> registerableConsumerTypes = Arrays.asList(getProperty("sm.consumerTypes", consumerTypesAsString).trim().split(" *, *")); // registerable consumer types
 			
 			// decide what username and password to test with
 			if (type.equals(ConsumerType.person) && !getProperty("sm.rhpersonal.username", "").equals("")) {
 				username = getProperty("sm.rhpersonal.username", "");
 				password = getProperty("sm.rhpersonal.password", "");
 			} else {
 				username = clientusername;
 				password = clientpassword;
 			}
 			
 			// String username, String password, String name, ConsumerType type, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 			if (registerableConsumerTypes.contains(type.toString())) {
 				if (type.equals(ConsumerType.person)) {
 					ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("661130"),	username,	password,	name,	type,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+username,	null}));
 				} else {
 					ll.add(Arrays.asList(new Object[]{null,  							username,	password,	name,	type,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));			
 				}
 			} else {
 				ll.add(Arrays.asList(new Object[]{ null,								username,	password,	name,	type,	Integer.valueOf(255),	null,	"No such consumer type: "+type}));			
 	
 			}
 		}
 
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getRegisterWithNameData")
 	public Object[][] getRegisterWithNameDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRegisterWithNameDataAsListOfLists());
 	}
 	protected List<List<Object>> getRegisterWithNameDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		String invalidNameStderr = "System name must consist of only alphanumeric characters, periods, dashes and underscores.";	// bugzilla 672233
 		       invalidNameStderr = "System name cannot contain most special characters.";	// bugzilla 677405
 		String maxCharsStderr = "Name of the consumer should be shorter than 250 characters\\.";
 		String name;
 
 		// valid names according to bugzilla 672233
 		name = "periods...dashes---underscores___alphanumerics123";
 										ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));
 		name = "249_characters_678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
 										ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));
 
 		// the tolerable characters has increased due to bugzilla 677405 and agilo task http://gibson.usersys.redhat.com/agilo/ticket/5235 (6.1) As an IT Person, I would like to ensure that user service and candlepin enforce the same valid character rules (QE); Developer beav "Christopher Duryee" <cduryee@redhat.com>
 		// https://bugzilla.redhat.com/show_bug.cgi?id=677405#c1
 		name = "[openingBracket[";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} \\[openingBracket\\[",	null}));
 		name = "]closingBracket]";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} \\]closingBracket\\]",	null}));
 		name = "{openingBrace{";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} \\{openingBrace\\{",	null}));
 		name = "}closingBrace}";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} \\}closingBrace\\}",	null}));
		name = "(openingParenthesis(";	ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} \\(openingParentesis\\(",	null}));
		name = ")closingParenthesis)";	ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} \\)closingParentesis\\)",	null}));
 		name = "?questionMark?";		ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} \\?questionMark\\?",	null}));
 		name = "@at@";					ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));
 		name = "!exclamationPoint!";	ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));
 		name = "`backTick`";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));
 		name = "'singleQuote'";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));
 		name = "pound#sign";			ll.add(Arrays.asList(new Object[] {null,	name,	Integer.valueOf(0),	"[a-f,0-9,\\-]{36} "+name,	null}));	// Note: pound signs within the name are acceptable, but not at the beginning
 
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
 }
