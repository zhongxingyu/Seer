 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  * Note: This scribe depends on register with --autosubscribe working properly
  */
 
 
 @Test(groups={"ComplianceTests","AcceptanceTests"})
 public class ComplianceTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is True when all installed products are subscribable by more than one common service level",
 			groups={"configureProductCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel","cli.tests"},
 			dataProvider="getAllProductsSubscribableByMoreThanOneCommonServiceLevelValuesData",
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenAllProductsSubscribableByMoreThanOneCommonServiceLevel_Test(Object bugzilla, String servicelevel) {
 		
 		// test register with service level
 		clienttasks.unregister_(null,null,null);
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"Before attempting to register with autosubscribe and a common servicelevel to become compliant for all the currently installed products, the system should be non-compliant (see value for fact '"+factNameForSystemCompliance+"').");
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,true,servicelevel,null,(String)null, Boolean.TRUE, false, null, null, null);
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemCompliance,
 				"When a system has products installed for which ALL are covered by available subscription pools with a common service level, the system should become compliant (see value for fact '"+factNameForSystemCompliance+"')");
 		for (ProductSubscription productSubscription : clienttasks.getCurrentlyConsumedProductSubscriptions()) {
 			Assert.assertEquals(productSubscription.serviceLevel, servicelevel,
 				"When a system has been registered with autosubscribe specifying a common service level, then all consumed product subscriptions must provide that service level.");
 		}
 		Assert.assertEquals(clienttasks.getCurrentServiceLevel(), servicelevel,
 			"When a system has been registered with autosubscribe specifying a common service level, then the consumer's service level prefernce should be set to that value.");
 	
 		// test autosubscribe (without service level) and assert that the consumed subscriptions provide the same service level as persisted during register
 		clienttasks.unsubscribe(true, null, null, null, null);
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"Before attempting to autosubscribe with a common servicelevel to become compliant for all the currently installed products, the system should be non-compliant (see value for fact '"+factNameForSystemCompliance+"').");
 		clienttasks.subscribe(true, null, (List<String>)null, null, null, null, null, null, null, null, null);
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemCompliance,
 				"When a system has products installed for which ALL are covered by available subscription pools with a common service level, the system should become compliant (see value for fact '"+factNameForSystemCompliance+"')");
 		for (ProductSubscription productSubscription : clienttasks.getCurrentlyConsumedProductSubscriptions()) {
 			Assert.assertEquals(productSubscription.serviceLevel, servicelevel,
 				"When a system has been registered with autosubscribe without specifying a common service level, then all consumed product subscriptions must provide the consumer's service level preference.");
 		}
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a compliant status when all installed products are subscribable by more than one common service level",
 			groups={"cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenAllProductsSubscribableByMoreThanOneCommonServiceLevel_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenAllProductsSubscribableByMoreThanOneCommonServiceLevel_Test() {
 		VerifyRhsmCompliancedWhenAllProductsAreSubscribable_Test();
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is True when all installed products are subscribable by one common service level",
 			groups={"configureProductCertDirForAllProductsSubscribableByOneCommonServiceLevel","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenAllProductsSubscribableByOneCommonServiceLevel_Test() {
 		VerifySystemCompliantFactWhenAllProductsSubscribableByMoreThanOneCommonServiceLevel_Test(null,allProductsSubscribableByOneCommonServiceLevelValue);
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a compliant status when all installed products are subscribable by one common service level",
 			groups={"cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenAllProductsSubscribableByOneCommonServiceLevel_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenAllProductsSubscribableByOneCommonServiceLevel_Test() {
 		VerifyRhsmCompliancedWhenAllProductsAreSubscribable_Test();
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is False when some installed products are subscribable",
 			groups={"configureProductCertDirForSomeProductsSubscribable","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenSomeProductsAreSubscribable_Test() throws JSONException, Exception {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(String)null, Boolean.TRUE, false, null, null, null);
 		Assert.assertFalse(clienttasks.getCurrentlyInstalledProducts().isEmpty(),
 				"Products are currently installed for which the compliance of only SOME are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be non-compliant (see value for fact '"+factNameForSystemCompliance+"').");
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"When a system has products installed for which only SOME are covered by available subscription pools, the system should NOT become compliant (see value for fact '"+factNameForSystemCompliance+"') even after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a non-compliant status when some installed products are subscribable",
 			groups={"blockedbyBug-723336","blockedbyBug-691480","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenSomeProductsAreSubscribable_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenSomeProductsAreSubscribable_Test() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 		RemoteFileTasks.runCommandAndWait(client, "echo 'Testing "+command+"' >> "+clienttasks.varLogMessagesFile, LogMessageUtil.action());
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenNonCompliant, null);
 		
 		// also verify the /var/syslog/messages
 		sleep(100);	// give the message thread time to be logged
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.varLogMessagesFile, null, rhsmComplianceDSyslogMessageWhenNonCompliant, null);
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is True when all installed products are subscribable",
 			groups={"configureProductCertDirForAllProductsSubscribable","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenAllProductsAreSubscribable_Test() throws JSONException, Exception {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(String)null, Boolean.TRUE, false, null, null, null);
 		Assert.assertFalse(clienttasks.getCurrentlyInstalledProducts().isEmpty(),
 				"Products are currently installed for which the compliance of ALL are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be non-compliant (see value for fact '"+factNameForSystemCompliance+"').");
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 		for (InstalledProduct installedProduct : clienttasks.getCurrentlyInstalledProducts()) {
 			Assert.assertEquals(installedProduct.status, "Subscribed","When config rhsm.productcertdir is populated with product certs for which ALL are covered by the currently available subscriptions, then each installed product status should be Subscribed.");
 		}
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemCompliance,
 				"When a system has products installed for which ALL are covered by available subscription pools, the system should become compliant (see value for fact '"+factNameForSystemCompliance+"') after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a compliant status when all installed products are subscribable (or an appropriate warning period status if an entitlement is within its warning period status)",
 			groups={"blockedbyBug-723336","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenAllProductsAreSubscribable_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenAllProductsAreSubscribable_Test() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 
 		if (clienttasks.getCurrentEntitlementCertsWithinWarningPeriod().isEmpty()) {
 			// otherwise verify the rhsmcomplianced status when we should be fully compliant
 			RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenCompliant, null);
 		} else {
 			// verify the rhsmcomplianced status when there are entitlement certs within their warning period
 			RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenInsideWarningPeriod, null);
 		}
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is False when no installed products are subscribable",
 			groups={"configureProductCertDirForNoProductsSubscribable","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenNoProductsAreSubscribable_Test() throws JSONException, Exception {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(String)null, Boolean.TRUE, false, null, null, null);
 		Assert.assertFalse(clienttasks.getCurrentlyInstalledProducts().isEmpty(),
 				"Products are currently installed for which the compliance of NONE are covered by currently available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"Before attempting to subscribe and become compliant for all the currently installed products, the system should be non-compliant (see value for fact '"+factNameForSystemCompliance+"').");
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 		for (InstalledProduct installedProduct : clienttasks.getCurrentlyInstalledProducts()) {
 			Assert.assertEquals(installedProduct.status, "Not Subscribed","When config rhsm.productcertdir is populated with product certs for which NONE are covered by the currently available subscriptions, then each installed product status should be Not Subscribed.");
 		}
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"When a system has products installed for which NONE are covered by available subscription pools, the system should NOT become compliant (see value for fact '"+factNameForSystemCompliance+"') after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a non-compliant status when no installed products are subscribable",
 			groups={"blockedbyBug-723336","blockedbyBug-691480","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenNoProductsAreSubscribable_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenNoProductsAreSubscribable_Test() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 		RemoteFileTasks.runCommandAndWait(client, "echo 'Testing "+command+"' >> "+clienttasks.varLogMessagesFile, LogMessageUtil.action());
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenNonCompliant, null);
 		
 		// also verify the /var/syslog/messages
 		sleep(100);	// give the message thread time to be logged
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+clienttasks.varLogMessagesFile, null, rhsmComplianceDSyslogMessageWhenNonCompliant, null);
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact is True when no products are installed",
 			groups={"configureProductCertDirForNoProductsInstalled","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenNoProductsAreInstalled_Test() throws JSONException, Exception {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(String)null, Boolean.TRUE, false, null, null, null);
 		Assert.assertTrue(clienttasks.getCurrentlyInstalledProducts().isEmpty(),
 				"No products are currently installed.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemCompliance,
 				"Because no products are currently installed, the system should inherently be compliant (see value for fact '"+factNameForSystemCompliance+"') even without subscribing to any subscription pools.");
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 		clienttasks.listInstalledProducts();
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemCompliance,
 				"Even after subscribing to all the available subscription pools, a system with no products installed should remain compliant (see value for fact '"+factNameForSystemCompliance+"').");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a compliant status when no products are installed (and a warning period status when at least one entitlement cert is within its warning period)",
 			groups={"blockedbyBug-723336","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenNoProductsAreInstalled_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenNoProductsAreInstalled_Test() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 
 		// verify the rhsmcomplianced status when there are entitlement certs within their warning period
 		List<EntitlementCert> entitlementCertsWithinWarningPeriod = clienttasks.getCurrentEntitlementCertsWithinWarningPeriod();
 		while (!entitlementCertsWithinWarningPeriod.isEmpty()) {
 			// assert the rhsmcomplianced status
 			//ssh root@jsefler-onprem-5server.usersys.redhat.com /usr/libexec/rhsmd -s -d
 			//Stdout: System has one or more entitlements in their warning period
 			//Stderr:
 			//ExitCode: 0
 			log.info("Asserting RhsmComplianced while at least one of the current entitlement certs is within its warning period...");
 			RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenInsideWarningPeriod, null);
 			
 			// unsubscribe from the serial
 			EntitlementCert entitlementCert = entitlementCertsWithinWarningPeriod.remove(0);
 			clienttasks.unsubscribeFromSerialNumber(entitlementCert.serialNumber);
 		}
 		
 		// verify the stdout message
 		log.info("Asserting RhsmComplianced status before unsubscribing from all currently consumed subscriptions...");
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenCompliant, null);
 		
 		// also assert RhsmComplianced when not consuming any subscriptions
 		log.info("Also asserting RhsmComplianced status after unsubscribing from all currently consumed subscriptions...");
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenCompliant, null);
 		
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact when system is already registered to RHN Classic",
 			groups={"blockedByBug-742027","RHNClassicTests","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenRegisteredToRHNClassic_Test() {
 		
 		// pre-test check for installed products
 		clienttasks.unregister(null,null,null);
 		configureProductCertDirAfterClass();
 		if (clienttasks.getCurrentlyInstalledProducts().isEmpty()) throw new SkipException("This test requires that at least one product cert is installed.");
 
 		// first assert that we are not compliant since we have not yet registered to RHN Classic
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"While at least one product cert is installed and we are NOT registered to RHN Classic, the system should NOT be compliant (see value for fact '"+factNameForSystemCompliance+"').");
 
 		// simulate registration to RHN Classic by creating a /etc/sysconfig/rhn/systemid
 		log.info("Simulating registration to RHN Classic by creating an empty systemid file '"+clienttasks.rhnSystemIdFile+"'...");
 		RemoteFileTasks.runCommandAndWait(client, "touch "+clienttasks.rhnSystemIdFile, LogMessageUtil.action());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhnSystemIdFile)==1, "RHN Classic systemid file '"+clienttasks.rhnSystemIdFile+"' is in place.");
 
 		// now assert compliance
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemCompliance,
 				"By definition, being registered to RHN Classic implies the system IS compliant no matter what products are installed (see value for fact '"+factNameForSystemCompliance+"').");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a compliant status when registered to RHN Classic",
 			groups={"RHNClassicTests","cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenRegisteredToRHNClassic_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenRegisteredToRHNClassic_Test() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenCompliantByRHNClassic, null);
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: verify the system.compliant fact remains False when all installed products are subscribable in the future",
 			groups={"blockedbyBug-737553","blockedbyBug-649068","configureProductCertDirForAllProductsSubscribableInTheFuture","cli.tests"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifySystemCompliantFactWhenAllProductsAreSubscribableInTheFuture_Test() throws JSONException, Exception {
 		clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null,null,null,null,(String)null, Boolean.TRUE, false, null, null, null);
 
 		// initial assertions
 		Assert.assertFalse(clienttasks.getCurrentlyInstalledProducts().isEmpty(),
 				"Products are currently installed for which the compliance of ALL are covered by future available subscription pools.");
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"Before attempting to subscribe to any future subscription, the system should be non-compliant (see value for fact '"+factNameForSystemCompliance+"').");
 		
 		// incrementally subscribe to each future subscription pool and assert the corresponding installed product's status
 		for (SubscriptionPool futureSystemSubscriptionPool : futureSystemSubscriptionPools) {
 			
 			// subscribe without asserting results (not necessary)
 			File entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(futureSystemSubscriptionPool);
 			List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 			
 			// assert that the Status of the installed product is "Future Subscription"
 			for (ProductCert productCert : clienttasks.getCurrentProductCertsCorrespondingToSubscriptionPool(futureSystemSubscriptionPool)) {
 				InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(productCert,installedProducts);
 				Assert.assertEquals(installedProduct.status, "Future Subscription", "Status of the installed product '"+productCert.productName+"' after subscribing to future subscription pool: "+futureSystemSubscriptionPool);
 				// TODO assert the installedProduct start/end dates
 			}
 		}
 		
 		// simply assert that actually did subscribe every installed product to a future subscription pool
 		for (InstalledProduct installedProduct : clienttasks.getCurrentlyInstalledProducts()) {
 			Assert.assertEquals(installedProduct.status, "Future Subscription", "Status of every installed product should be a Future Subscription after subscribing all installed products to a future pool.  This Installed Product: "+installedProduct);
 		}
 		
 		// finally assert that the overall system is non-compliant
 		Assert.assertEquals(clienttasks.getFactValue(factNameForSystemCompliance), factValueForSystemNonCompliance,
 				"When a system has products installed for which ALL are covered by future available subscription pools, the system should remain non-compliant (see value for fact '"+factNameForSystemCompliance+"') after having subscribed to every available subscription pool.");
 	}
 	
 	@Test(	description="rhsm-complianced: verify rhsm-complianced -d -s reports a non-compliant status when all installed products are subscribable in the future",
 			groups={"cli.tests"},
 			dependsOnMethods={"VerifySystemCompliantFactWhenAllProductsAreSubscribableInTheFuture_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void VerifyRhsmCompliancedWhenAllProductsAreSubscribableInTheFuture_Test() {
 		String command = clienttasks.rhsmComplianceD+" -s -d";
 
 		// verify the stdout message
 		RemoteFileTasks.runCommandAndAssert(client, command, Integer.valueOf(0), rhsmComplianceDStdoutMessageWhenNonCompliant, null);
 	}
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO INVERSE OF VerifySystemCompliantFactWhenAllProductsAreSubscribableInTheFuture_Test - should not be compliant for an expired subscription
 	// TODO Bug 727967 - Compliance Assistant Valid Until Date Detection Not Working
 	
 	
 	
 	// Protected Class Variables ***********************************************************************
 
 	public static final String factNameForSystemCompliance = "system.entitlements_valid"; // "system.compliant"; // changed with the removal of the word "compliance" 3/30/2011
 	public static final String factValueForSystemCompliance = "valid"; 			// "True"; RHEL62
 	public static final String factValueForSystemNonCompliance = "invalid"; 	// "False"; RHEL62
 	public static final String factValueForSystemPartialCompliance = "partial";	// "False"; RHEL62
 
 	public static final String productCertDirForSomeProductsSubscribable = "/tmp/sm-someProductsSubscribable";
 	public static final String productCertDirForAllProductsSubscribable = "/tmp/sm-allProductsSubscribable";
 	public static final String productCertDirForNoProductsSubscribable = "/tmp/sm-noProductsSubscribable";
 	public static final String productCertDirForNoProductsinstalled = "/tmp/sm-noProductsInstalled";
 	public static final String productCertDirForAllProductsSubscribableInTheFuture = "/tmp/sm-allProductsSubscribableInTheFuture";
 	public static final String productCertDirForAllProductsSubscribableByOneCommonServiceLevel = "/tmp/sm-allProductsSubscribableByOneCommonServiceLevel";
 	public static final String productCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel = "/tmp/sm-allProductsSubscribableByMoreThanOneCommonServiceLevel";
 
 	public static String allProductsSubscribableByOneCommonServiceLevelValue=null;	// the value of the service_level to expect from all of the autosubscribed pools after calling configureProductCertDirForAllProductsSubscribableByOneCommonServiceLevel
 	public static List<String> allProductsSubscribableByMoreThanOneCommonServiceLevelValues= new ArrayList<String>();	// the service_level values to expect subscription-manager-gui to prompt the user to choose from when autosubscribing after calling configureProductCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel
 
 	protected String productCertDir = null;
 	protected final String rhsmComplianceDStdoutMessageWhenNonCompliant = "System has one or more certificates that are not valid";
 	protected final String rhsmComplianceDStdoutMessageWhenCompliant = "System entitlements appear valid";
 	protected final String rhsmComplianceDStdoutMessageWhenInsideWarningPeriod = "System has one or more entitlements in their warning period";
 	protected final String rhsmComplianceDStdoutMessageWhenCompliantByRHNClassic = "System is already registered to another entitlement system";
 	protected final String rhsmComplianceDSyslogMessageWhenNonCompliant = "This system is missing one or more valid entitlement certificates. Please run subscription-manager for more information.";
 	protected List<SubscriptionPool> futureSystemSubscriptionPools = null;
 	
 	
 	
 	
 	// Protected Methods ***********************************************************************
 	
 	protected String getKeyToLongestMap (Map<String,Set<String>> map) {
 		int maxLength=0;
 		String maxKey=null;
 		for (String key : map.keySet()) {
 			if (map.get(key).size()>maxLength) {
 				maxLength = map.get(key).size();
 				maxKey = key;
 			}
 		}
 		return maxKey;
 	}
 	protected Map<String,Set<String>> getInvertedMap(Map<String,Set<String>> serviceLevelToProductIdsMap) {
 		Map<String,Set<String>> productIdsToServiceLevelsMap = new HashMap<String,Set<String>>();
 		for (String serviceLevel : serviceLevelToProductIdsMap.keySet()) {
 			for (String productId : serviceLevelToProductIdsMap.get(serviceLevel)) {
 				if (!productIdsToServiceLevelsMap.containsKey(productId)) productIdsToServiceLevelsMap.put(productId, new HashSet<String>());
 				HashSet<String> serviceLevelSet = (HashSet<String>) productIdsToServiceLevelsMap.get(productId);
 				serviceLevelSet.add(serviceLevel);
 			}
 		}
 		return productIdsToServiceLevelsMap;
 	}
 	protected Map<String,Set<String>> getServiceLevelToProductIdsMapFromEntitlementCerts(List<EntitlementCert> entitlementCerts) {
 	
 		//{Standard=[37065, 27060, 37069, 37068, 37067, 37070,        37060], 
 		// None    =[37060], 
 		// Premium =[37065,        37069, 37068, 37067, 37070,        37060]}
 		//
 		//
 		//{27060=[Standard],
 		// 37065=[Standard, Premium],
 		// 37069=[Standard, Premium],
 		// 37068=[Standard, Premium],
 		// 37067=[Standard, Premium],
 		// 37070=[Standard, Premium],
 		// 37060=[Standard, Premium, None]}
 		
 		// create maps of serviceLevel-to-productIds and productIds-to-serviceLevel
 		Map<String,Set<String>> serviceLevelToProductIdsMap = new HashMap<String,Set<String>>();
 		for (EntitlementCert entitlementCert : clienttasks.getCurrentEntitlementCerts()) {
 			String serviceLevel = entitlementCert.orderNamespace.supportLevel;
 			
 			// skip all entitlements without a service level
 			if (serviceLevel==null || serviceLevel.equals("")) continue;
 			
 			if (!serviceLevelToProductIdsMap.containsKey(serviceLevel)) serviceLevelToProductIdsMap.put(serviceLevel, new HashSet<String>());
 			HashSet<String> productIdSet = (HashSet<String>) serviceLevelToProductIdsMap.get(serviceLevel);		
 			for (ProductNamespace productNamespace : entitlementCert.productNamespaces) {
 //debugTesting if (productNamespace.id.equals("27060")) continue;
 				productIdSet.add(productNamespace.id);
 			}
 		}
 		return serviceLevelToProductIdsMap;
 	}
 	
 	
 	// Configuration Methods ***********************************************************************
 
 	@AfterGroups(groups={"setup"},value="RHNClassicTests")
 	public void removeRHNSystemIdFile() {
 		client.runCommandAndWait("rm -rf "+clienttasks.rhnSystemIdFile);;
 	}
 	
 	@BeforeClass(groups={"setup"})
 	public void setupProductCertDirsBeforeClass() throws ParseException, JSONException, Exception {
 		
 		// clean out the productCertDirs
 		for (String productCertDir : new String[]{
 				productCertDirForSomeProductsSubscribable,
 				productCertDirForAllProductsSubscribable,
 				productCertDirForNoProductsSubscribable,
 				productCertDirForNoProductsinstalled,
 				productCertDirForAllProductsSubscribableInTheFuture,
 				productCertDirForAllProductsSubscribableByOneCommonServiceLevel,
 				productCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel}) {
 			RemoteFileTasks.runCommandAndAssert(client, "rm -rf "+productCertDir, 0);
 			RemoteFileTasks.runCommandAndAssert(client, "mkdir "+productCertDir, 0);
 		}
 
 		// register and subscribe to all available subscriptions
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, true, false, null, null, null);
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 		
 		// distribute a copy of the product certs amongst the productCertDirs based on their status
 		List<InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		for (ProductCert productCert : clienttasks.getCurrentProductCerts()) {
 			InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(productCert,installedProducts);
 			
 			if (installedProduct.status.equals("Not Subscribed")) {
 				// "Not Subscribed" case...
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCert.file+" "+productCertDirForNoProductsSubscribable, 0);
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCert.file+" "+productCertDirForSomeProductsSubscribable, 0);
 			} else if (installedProduct.status.equals("Subscribed")) {
 				// "Subscribed" case...
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCert.file+" "+productCertDirForAllProductsSubscribable, 0);
 				RemoteFileTasks.runCommandAndAssert(client, "cp "+productCert.file+" "+productCertDirForSomeProductsSubscribable, 0);
 			} else {
 				// TODO "Partially Subscribed" case
 				//InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToEntitlementCert(correspondingEntitlementCert);
 			}
 		}
 		
 		
 		// setup for productCertDirForAllProductsSubscribableInTheFuture
 		//clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		List<File> productCertFilesCopied = new ArrayList<File>();
 		futureSystemSubscriptionPools = new ArrayList<SubscriptionPool>();
 		for (List<Object> futureSystemSubscriptionPoolsDataRow : getAllFutureSystemSubscriptionPoolsDataAsListOfLists()) {
 			SubscriptionPool futureSystemSubscriptionPool = (SubscriptionPool)futureSystemSubscriptionPoolsDataRow.get(0);
 			for (ProductCert productCert : clienttasks.getCurrentProductCertsCorrespondingToSubscriptionPool(futureSystemSubscriptionPool)) {
 				if (!productCertFilesCopied.contains(productCert.file)) {
 					//RemoteFileTasks.runCommandAndAssert(client, "cp -n "+productCert.file+" "+productCertDirForAllProductsSubscribableInTheFuture, 0);	// RHEL5 does not understand cp -n  
 					RemoteFileTasks.runCommandAndAssert(client, "if [ ! -e "+productCertDirForAllProductsSubscribableInTheFuture+File.separator+productCert.file.getName()+" ]; then cp "+productCert.file+" "+productCertDirForAllProductsSubscribableInTheFuture+"; fi;", 0);	// no clobber copy for both RHEL5 ad RHEL6
 					productCertFilesCopied.add(productCert.file);
 					if (!futureSystemSubscriptionPools.contains(futureSystemSubscriptionPool)) {
 						futureSystemSubscriptionPools.add(futureSystemSubscriptionPool);
 					}
 				}
 			}
 		}
 		
 		
 		// determine the serviceLevel and all the products that are subscribable by one common service level 
 		Map<String,Set<String>> serviceLevelToProductIdsMap = getServiceLevelToProductIdsMapFromEntitlementCerts(clienttasks.getCurrentEntitlementCerts());
 //debugTesting serviceLevelToProductIdsMap.get("Premium").add("17000");
 		Map<String,Set<String>> productIdsToServiceLevelsMap = getInvertedMap(serviceLevelToProductIdsMap);
 		Set<String> allProductsSubscribableByOneCommonServiceLevelCandidates = productIdsToServiceLevelsMap.keySet();
 		boolean allProductsSubscribableByOneCommonServiceLevelDeterminable=true;
 		OUT: do {
 			String serviceLevelCandidate = getKeyToLongestMap(serviceLevelToProductIdsMap);
 			// does this candidate have all candidate products?
 			if (serviceLevelToProductIdsMap.get(serviceLevelCandidate).containsAll(allProductsSubscribableByOneCommonServiceLevelCandidates)) {
 				// is there another serviceLevel that has all candidate products?
 				for (String serviceLevel : serviceLevelToProductIdsMap.keySet()) {
 					if (serviceLevel.equals(serviceLevelCandidate)) continue;
 					if (serviceLevelToProductIdsMap.get(serviceLevel).size()==serviceLevelToProductIdsMap.get(serviceLevelCandidate).size()) {
 						allProductsSubscribableByOneCommonServiceLevelDeterminable = false;
 						break OUT;
 					}
 				}
 				allProductsSubscribableByOneCommonServiceLevelValue = serviceLevelCandidate;
 				
 			} else {
 				// pluck the first candidate product that is not in the serviceLevelCandidate map of products
 				for (String productId : (String[])allProductsSubscribableByOneCommonServiceLevelCandidates.toArray(new String[]{})) {
 					if (!serviceLevelToProductIdsMap.get(serviceLevelCandidate).contains(productId)) {
 						allProductsSubscribableByOneCommonServiceLevelCandidates.remove(productId);
 						for (String serviceLevel : serviceLevelToProductIdsMap.keySet()) {
 							serviceLevelToProductIdsMap.get(serviceLevel).remove(productId);
 						}
 						break;
 					}
 				}
 			}
 		} while (allProductsSubscribableByOneCommonServiceLevelValue==null && allProductsSubscribableByOneCommonServiceLevelDeterminable);
 		// copy the products to productCertDirForAllProductsSubscribableByOneCommonServiceLevel
 		if (allProductsSubscribableByOneCommonServiceLevelDeterminable) {
 			for (ProductCert productCert : clienttasks.getCurrentProductCerts()) {
				if (allProductsSubscribableByOneCommonServiceLevelCandidates.contains(productCert.productId)) {
 					RemoteFileTasks.runCommandAndAssert(client, "cp "+productCert.file+" "+productCertDirForAllProductsSubscribableByOneCommonServiceLevel, 0);
 				}
 			}
 		} else {
 			log.warning("Cannot determine a set of products where allProductsSubscribableByOneCommonServiceLevel.");
 		}
 		
 		
 		// determine the serviceLevels and all the products that are subscribable by more than one common service level
 		serviceLevelToProductIdsMap = getServiceLevelToProductIdsMapFromEntitlementCerts(clienttasks.getCurrentEntitlementCerts());
 
 		productIdsToServiceLevelsMap = getInvertedMap(serviceLevelToProductIdsMap);
 		List<String> allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates = new ArrayList<String>();
 		for (String productId : productIdsToServiceLevelsMap.keySet()) {
 			if (productIdsToServiceLevelsMap.get(productId).size() >1) allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.add(productId);
 		}
 		if (!allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.isEmpty()) {
 
 			// randomly choose the service levels from the candidates
 //			allProductsSubscribableByMoreThanOneCommonServiceLevelValues = Arrays.asList(productIdsToServiceLevelsMap.get(allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.get(randomGenerator.nextInt(allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.size()))).toArray(new String[]{}));
 			allProductsSubscribableByMoreThanOneCommonServiceLevelValues.addAll(productIdsToServiceLevelsMap.get(allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.get(randomGenerator.nextInt(allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.size()))));
 //debugTesting allProductsSubscribableByMoreThanOneCommonServiceLevelValues = Arrays.asList(new String[]{"None", "Standard", "Premium"});
 			// pluck out the productIds that do not map to all of the values in allProductsSubscribableByMoreThanOneCommonServiceLevelValues
 			for (String  productId : productIdsToServiceLevelsMap.keySet()) {
 				if (!productIdsToServiceLevelsMap.get(productId).containsAll(allProductsSubscribableByMoreThanOneCommonServiceLevelValues)) {
 					allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.remove(productId);
 				}
 			}
 			
 			// copy the products to productCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel
 			for (ProductCert productCert : clienttasks.getCurrentProductCerts()) {
				if (allProductsSubscribableByMoreThanOneCommonServiceLevelCandidates.contains(productCert.productId)) {
 					RemoteFileTasks.runCommandAndAssert(client, "cp "+productCert.file+" "+productCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel, 0);
 				}
 			}
 		} else {
 			log.warning("Cannot determine a set of products where allProductsSubscribableByMoreThanOneCommonServiceLevel.");
 		}
 		
 		
 		this.productCertDir = clienttasks.productCertDir;
 	}
 	
 	@AfterClass(groups={"setup"},alwaysRun=true)
 	public void configureProductCertDirAfterClass() {
 		if (clienttasks==null) return;
 		if (this.productCertDir!=null) clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir", this.productCertDir);
 		allProductsSubscribableByOneCommonServiceLevelValue = null;
 		allProductsSubscribableByMoreThanOneCommonServiceLevelValues.clear();
 	}
 	
 	
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForSomeProductsSubscribable")
 	public void configureProductCertDirForSomeProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForSomeProductsSubscribable);
 		SSHCommandResult r0 = client.runCommandAndWait("ls -1 "+productCertDirForSomeProductsSubscribable+" | wc -l");
 		SSHCommandResult r1 = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribable+" | wc -l");
 		SSHCommandResult r2 = client.runCommandAndWait("ls -1 "+productCertDirForNoProductsSubscribable+" | wc -l");
 		if (Integer.valueOf(r1.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are subscribable based on the currently available subscriptions.");
 		if (Integer.valueOf(r2.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are non-subscribable based on the currently available subscriptions.");
 		Assert.assertTrue(Integer.valueOf(r0.getStdout().trim())>0 && Integer.valueOf(r1.getStdout().trim())>0 && Integer.valueOf(r2.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains some subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForAllProductsSubscribable")
 	public void configureProductCertDirForAllProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForAllProductsSubscribable);	
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribable+" | wc -l");
 		if (Integer.valueOf(r.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are subscribable based on the currently available subscriptions.");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForNoProductsSubscribable")
 	public void configureProductCertDirForNoProductsSubscribable() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForNoProductsSubscribable);
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForNoProductsSubscribable+" | wc -l");
 		if (Integer.valueOf(r.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are non-subscribable based on the currently available subscriptions.");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all non-subscribable products based on the currently available subscriptions.");
 	}
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForNoProductsInstalled")
 	public void configureProductCertDirForNoProductsInstalled() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForNoProductsinstalled);
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForNoProductsinstalled+" | wc -l");
 		Assert.assertEquals(Integer.valueOf(r.getStdout().trim()),Integer.valueOf(0),
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains no products.");
 	}
 	
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForAllProductsSubscribableInTheFuture")
 	public void configureProductCertDirForAllProductsSubscribableInTheFuture() throws JSONException, Exception {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForAllProductsSubscribableInTheFuture);	
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribableInTheFuture+" | wc -l");
 		if (Integer.valueOf(r.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are subscribable to future available subscriptions.");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all subscribable products based on future available subscriptions.");
 	}
 	
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForAllProductsSubscribableByOneCommonServiceLevel")
 	public void configureProductCertDirForAllProductsSubscribableByOneCommonServiceLevel() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForAllProductsSubscribableByOneCommonServiceLevel);	
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribableByOneCommonServiceLevel+" | wc -l");
 		if (Integer.valueOf(r.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are autosubscribable via one common service level.");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all autosubscribable products via one common service level.");
 	}
 	
 	@BeforeGroups(groups={"setup"},value="configureProductCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel")
 	public void configureProductCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.updateConfFileParameter(clienttasks.rhsmConfFile, "productCertDir",productCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel);	
 		SSHCommandResult r = client.runCommandAndWait("ls -1 "+productCertDirForAllProductsSubscribableByMoreThanOneCommonServiceLevel+" | wc -l");
 		if (Integer.valueOf(r.getStdout().trim())==0) throw new SkipException("Could not find any installed product certs that are autosubscribable via more than one common service level.");
 		Assert.assertTrue(Integer.valueOf(r.getStdout().trim())>0,
 				"The "+clienttasks.rhsmConfFile+" file is currently configured with a productCertDir that contains all autosubscribable products via more than one common service level.");
 	}
 
 	// Data Providers ***********************************************************************
 
 	@DataProvider(name="getAllProductsSubscribableByMoreThanOneCommonServiceLevelValuesData")
 	public Object[][] getAllProductsSubscribableByMoreThanOneCommonServiceLevelValuesDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllProductsSubscribableByMoreThanOneCommonServiceLevelValuesDataAsListOfLists());
 	}
 	protected List<List<Object>> getAllProductsSubscribableByMoreThanOneCommonServiceLevelValuesDataAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 
 		for (String servicelevel : allProductsSubscribableByMoreThanOneCommonServiceLevelValues) {
 			ll.add(Arrays.asList(new Object[]{null,	servicelevel}));		
 		}
 
 		return ll;
 	}
 
 }
 
