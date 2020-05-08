 package com.redhat.qe.sm.cli.tests;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BlockedByBzBug;
 import com.redhat.qe.auto.bugzilla.BzChecker;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.InstalledProduct;
 import com.redhat.qe.sm.data.OrderNamespace;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductNamespace;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  *  @author ssalevan
  *  @author jsefler
  *
  */
 @Test(groups={"ListTests"})
 public class ListTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager-cli: list available subscriptions (when not consuming)",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=41678)
 	public void EnsureAvailableSubscriptionsListed_Test() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 		String availableSubscriptionPools = clienttasks.listAvailableSubscriptionPools().getStdout();
 		Assert.assertContainsMatch(availableSubscriptionPools, "Available Subscriptions","" +
 				"Available Subscriptions are listed for '"+sm_clientUsername+"' to consume.");
 		Assert.assertContainsNoMatch(availableSubscriptionPools, "No available subscription pools to list",
 				"Available Subscriptions are listed for '"+sm_clientUsername+"' to consume.");
 
 		log.warning("These manual TCMS instructions are not really achievable in this automated test...");
 		log.warning(" * List produced matches the known data contained on the Candlepin server");
 		log.warning(" * Confirm that the marketing names match.. see prereq link https://engineering.redhat.com/trac/IntegratedMgmtQE/wiki/sm-prerequisites");
 		log.warning(" * Match the marketing names w/ https://www.redhat.com/products/");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list available subscriptions - verify that among all the subscriptions available to this consumer, those that satisfy the system hardware are listed as available",
 			groups={"AcceptanceTests", "blockedByBugzilla-712502","unsubscribeBeforeGroup"},
 			dataProvider="getAvailableSystemSubscriptionPoolProductData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41678)
 	public void EnsureHardwareMatchingSubscriptionsAreListedAsAvailable_Test(String productId, JSONArray bundledProductDataAsJSONArray) {
 //if(!productId.equals("null-sockets")) throw new SkipException("debugging...");		
 		// implicitly registered in dataProvider; no need to register with force; saves time
 		//clienttasks.register(clientusername, clientpassword, null, null, null, null, true, null, null, null);
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "Expected SubscriptionPool with ProductId '"+productId+"' is listed as available for subscribing.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list available subscriptions - verify that among all the subscriptions available to this consumer, those that do NOT satisfy the hardware are NOT listed as available",
 			groups={"AcceptanceTests", "blockedByBugzilla-712502","unsubscribeBeforeGroup"},
 			dataProvider="getNonAvailableSystemSubscriptionPoolProductData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41678)
 	public void EnsureNonHardwareMatchingSubscriptionsAreNotListedAsAvailable_Test(String productId) {
 		// implicitly registered in dataProvider; no need to register with force; saves time
 		//clienttasks.register(clientusername, clientpassword, null, null, null, null, true, null, null, null);
 		
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 		Assert.assertNull(pool, "As expected, SubscriptionPool with ProductId '"+productId+"' is NOT listed as available for subscribing.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list consumed entitlements (when not consuming)",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=41679)
 	public void EnsureConsumedEntitlementsListed_Test() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 		String consumedProductSubscription = clienttasks.listConsumedProductSubscriptions().getStdout();
 		Assert.assertContainsMatch(consumedProductSubscription, "No consumed subscription pools to list",
 				"No consumed subscription pools listed for '"+sm_clientUsername+"' after registering (without autosubscribe).");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: list consumed entitlements",
 			groups={},
 			dataProvider="getAllSystemSubscriptionPoolProductData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41679)
 	public void EnsureConsumedEntitlementsListed_Test(String productId, JSONArray bundledProductDataAsJSONArray) throws JSONException, Exception {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 		
 		SubscriptionPool pool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", productId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 		Assert.assertNotNull(pool, "SubscriptionPool with ProductId '"+productId+"' is available for subscribing.");
 		EntitlementCert  entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(clienttasks.subscribeToSubscriptionPool_(pool));
 		List<ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		Assert.assertTrue(!consumedProductSubscriptions.isEmpty(),"The list of Consumed Product Subscription is NOT empty after subscribing to a pool with ProductId '"+productId+"'.");
 		for (ProductSubscription productSubscription : consumedProductSubscriptions) {
 			Assert.assertEquals(productSubscription.serialNumber, entitlementCert.serialNumber,
 					"SerialNumber of Consumed Product Subscription matches the serial number from the current entitlement certificate.");
 		}	
 	}
 	
 	@Test(	description="subscription-manager-cli: list installed products",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureInstalledProductsListed_Test() {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 
 		List <ProductCert> productCerts = clienttasks.getCurrentProductCerts();
 		String installedProductsAsString = clienttasks.listInstalledProducts().getStdout();
 		//List <InstalledProduct> installedProducts = clienttasks.getCurrentlyInstalledProducts();
 		List <InstalledProduct> installedProducts = InstalledProduct.parse(installedProductsAsString);
 
 		// assert some stdout
 		if (installedProducts.size()>0) {
 			String bannerTitle = "Installed Product Status";
 			Assert.assertTrue(installedProductsAsString.contains(bannerTitle), "The list of installed products is entitled '"+bannerTitle+"'.");
 		}
 		
 		// assert the number of installed product matches the product certs installed
 		Assert.assertEquals(installedProducts.size(), productCerts.size(), "A single product is reported as installed for each product cert found in "+clienttasks.productCertDir);
 
 		// assert that each of the installed ProductCerts are listed as InstalledProducts with status "Not Subscribed"
 		for (ProductCert productCert : productCerts) {
 			InstalledProduct installedProduct = clienttasks.getInstalledProductCorrespondingToProductCert(productCert,installedProducts);
 			Assert.assertNotNull(installedProduct, "The following installed product cert is included by subscription-manager in the list --installed: "+(installedProduct==null?"null":installedProduct));	
 			Assert.assertEquals(installedProduct.status, "Not Subscribed", "The status of installed product when newly registered: "+installedProduct);
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: ensure list [--installed] produce the same results",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureListAndListInstalledAreTheSame_Test() throws JSONException, Exception {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 
 		// assert same results when no subscribed to anything...
 		log.info("assert list [--installed] produce same results when not subscribed to anything...");
 		SSHCommandResult listResult = clienttasks.list_(null, null, null, null, null, null, null, null, null);
 		SSHCommandResult listInstalledResult = clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null, null, null);
 		
 		Assert.assertEquals(listResult.getStdout(), listInstalledResult.getStdout(), "'list' and 'list --installed' produce the same stdOut results.");
 		Assert.assertEquals(listResult.getStderr(), listInstalledResult.getStderr(), "'list' and 'list --installed' produce the same stdErr results.");
 		Assert.assertEquals(listResult.getExitCode(), listInstalledResult.getExitCode(), "'list' and 'list --installed' produce the same exitCode results.");
 		
 		
 		// assert same results when subscribed to something...
 		log.info("assert list [--installed] produce same results when subscribed to something...");
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPool_(pool);
 		listResult = clienttasks.list_(null, null, null, null, null, null, null, null, null);
 		listInstalledResult = clienttasks.list_(null, null, null, Boolean.TRUE, null, null, null, null, null);
 		
 		Assert.assertEquals(listResult.getStdout(), listInstalledResult.getStdout(), "'list' and 'list --installed' produce the same stdOut results.");
 		Assert.assertEquals(listResult.getStderr(), listInstalledResult.getStderr(), "'list' and 'list --installed' produce the same stdErr results.");
 		Assert.assertEquals(listResult.getExitCode(), listInstalledResult.getExitCode(), "'list' and 'list --installed' produce the same exitCode results.");
 	}
 	
 
 	@Test(	description="subscription-manager: list of consumed entitlements should display consumed product marketing name",
 			groups={},
 			dataProvider="getAllEntitlementCertsData",
 			enabled=false)	// this test implementation is no longer valid after the change in format for consumed product subscriptions (from many to one) - see bug 806986
 	@Deprecated
 	@ImplementsNitrateTest(caseId=48092, fromPlan=2481)
 	public void EnsureListConsumedMatchesProductsListedInTheEntitlementCerts_Test_DEPRECATED(EntitlementCert entitlementCert) {
 
 		// assert: The list of consumed products matches the products listed in the entitlement cert
 		List<ProductSubscription> productSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		List<ProductSubscription> productSubscriptionsWithMatchingSerialNumber = ProductSubscription.findAllInstancesWithMatchingFieldFromList("serialNumber", entitlementCert.serialNumber, productSubscriptions);
 		//Assert.assertTrue(productSubscriptionsWithMatchingSerialNumber.size()>0, "Found consumed product subscription(s) whose SerialNumber matches this entitlement cert: "+entitlementCert);
 		//Assert.assertEquals(productSubscriptionsWithMatchingSerialNumber.size(),entitlementCert.productNamespaces.size(), "Found consumed product subscription(s) for each of the bundleProducts (total of '"+entitlementCert.productNamespaces.size()+"' expected) whose SerialNumber matches this entitlement cert: "+entitlementCert);
 		int productSubscriptionsWithMatchingSerialNumberSizeExpected = entitlementCert.productNamespaces.size()==0?1:entitlementCert.productNamespaces.size(); // when there are 0 bundledProducts, we are still consuming 1 ProductSubscription
 		Assert.assertEquals(productSubscriptionsWithMatchingSerialNumber.size(),productSubscriptionsWithMatchingSerialNumberSizeExpected, "Found consumed product subscription(s) for each of the bundleProducts (total of '"+productSubscriptionsWithMatchingSerialNumberSizeExpected+"' expected) whose SerialNumber matches this entitlement cert: "+entitlementCert);
 
 		for (ProductNamespace productNamespace : entitlementCert.productNamespaces) {
 			List<ProductSubscription> matchingProductSubscriptions = ProductSubscription.findAllInstancesWithMatchingFieldFromList("productName", productNamespace.name, productSubscriptionsWithMatchingSerialNumber);
 			Assert.assertEquals(matchingProductSubscriptions.size(), 1, "Found one bundledProduct name '"+productNamespace.name+"' in the list of consumed product subscriptions whose SerialNumber matches this entitlement cert: "+entitlementCert);
 			ProductSubscription correspondingProductSubscription = matchingProductSubscriptions.get(0);
 			log.info("We are about to assert that this consumed Product Subscription: "+correspondingProductSubscription);
 			log.info("...represents this ProductNamespace: "+productNamespace);
 			log.info("...corresponding to this OrderNamespace: "+entitlementCert.orderNamespace);
 			log.info("...from this EntitlementCert: "+entitlementCert);
 			Assert.assertEquals(correspondingProductSubscription.productName, productNamespace.name, "productName from ProductSubscription in list --consumed matches productName from ProductNamespace in EntitlementCert.");
 			Assert.assertEquals(correspondingProductSubscription.contractNumber, entitlementCert.orderNamespace.contractNumber, "contractNumber from ProductSubscription in list --consumed matches contractNumber from OrderNamespace in EntitlementCert.");
 			Assert.assertEquals(correspondingProductSubscription.accountNumber, entitlementCert.orderNamespace.accountNumber, "accountNumber from ProductSubscription in list --consumed matches accountNumber from OrderNamespace in EntitlementCert.");
 			Assert.assertEquals(correspondingProductSubscription.serialNumber, entitlementCert.serialNumber, "serialNumber from ProductSubscription in list --consumed matches serialNumber from EntitlementCert.");
 			
 			Calendar now = Calendar.getInstance();
 			if (now.after(entitlementCert.orderNamespace.startDate) && now.before(entitlementCert.orderNamespace.endDate)) {
 				Assert.assertTrue(correspondingProductSubscription.isActive, "isActive is True when the current time ("+EntitlementCert.formatDateString(now)+") is between the start/end dates in the EntitlementCert: "+entitlementCert);
 			} else {
 				Assert.assertFalse(correspondingProductSubscription.isActive, "isActive is False when the current time ("+EntitlementCert.formatDateString(now)+") is NOT between the start/end dates in the EntitlementCert: "+entitlementCert);
 			}
 			
 			// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=660713 - jsefler 12/12/2010
 			Boolean invokeWorkaroundWhileBugIsOpen = true;
 			try {String bugId="660713"; if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				log.warning("The workaround while this bug is open is to skip the assertion that: startDates and endDates match");
 			} else {
 			// END OF WORKAROUND
 			Assert.assertEquals(ProductSubscription.formatDateString(correspondingProductSubscription.startDate), ProductSubscription.formatDateString(entitlementCert.orderNamespace.startDate), "startDate from ProductSubscription in list --consumed matches startDate from OrderNamespace ("+OrderNamespace.formatDateString(entitlementCert.orderNamespace.startDate)+") after conversion from GMT in EntitlementCert to local time.");
 			Assert.assertEquals(ProductSubscription.formatDateString(correspondingProductSubscription.endDate), ProductSubscription.formatDateString(entitlementCert.orderNamespace.endDate), "endDate from ProductSubscription in list --consumed matches endDate from OrderNamespace ("+OrderNamespace.formatDateString(entitlementCert.orderNamespace.endDate)+") after conversion from GMT in EntitlementCert to local time.");
 			}
 		}
 	}
 	@Test(	description="subscription-manager: list of consumed entitlements should display the provided product marketing names",
 			groups={},
 			dataProvider="getAllEntitlementCertsData",
 			enabled=true)	// this new test implementation was implemented due to change in list of consumed product subscriptions (from many to one) - see bug 806986
 	@ImplementsNitrateTest(caseId=48092, fromPlan=2481)
 	public void EnsureListConsumedMatchesProductsListedInTheEntitlementCerts_Test(EntitlementCert entitlementCert) {
 
 		// find the consumed product subscription corresponding to this entitlement cert and assert there is only one found
 		List<ProductSubscription> allConsumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		List<ProductSubscription> productSubscriptions = ProductSubscription.findAllInstancesWithMatchingFieldFromList("serialNumber", entitlementCert.serialNumber, allConsumedProductSubscriptions);
 		Assert.assertEquals(productSubscriptions.size(),1, "Found a single consumed product subscription with a matching serialNumber from this entitlementCert: "+entitlementCert);
 		ProductSubscription productSubscription = productSubscriptions.get(0);
 		List<String> providedProductNames = new ArrayList<String>();
 		for (ProductNamespace productNamespace : entitlementCert.productNamespaces) providedProductNames.add(productNamespace.name);
 		
 		// TEMPORARY WORKAROUND FOR BUG
 		if (entitlementCert.orderNamespace.supportLevel==null || entitlementCert.orderNamespace.supportType==null) {
 			String bugId = "842170";
 			boolean invokeWorkaroundWhileBugIsOpen = true;
 			try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 			if (invokeWorkaroundWhileBugIsOpen) {
 				throw new SkipException("While bug "+bugId+" is open, skipping assertion of consumed product subscription in list for entitlement's with a null support level/type.");
 			}
 		}
 		// END OF WORKAROUND
 		
		// when the entitlement OID value parsed was null, it should effectively be reported as ""  (Reference related bugs 842170 847354)
		if (entitlementCert.orderNamespace.supportLevel==null) entitlementCert.orderNamespace.supportLevel="";
		if (entitlementCert.orderNamespace.supportType==null) entitlementCert.orderNamespace.supportType="";
		
 		
 		//	Subscription Name:    	Awesome OS Server Bundled (2 Sockets, Standard Support)
 		//	Provides:             	Clustering Bits
 		//	                      	Awesome OS Server Bits
 		//	                      	Shared Storage Bits
 		//	                      	Management Bits
 		//	                      	Large File Support Bits
 		//	                      	Load Balancing Bits
 		//	SKU:                  	awesomeos-server-2-socket-std
 		//	Contract:             	36
 		//	Account:              	12331131231
 		//	Serial Number:        	6683485045354827351
 		//	Active:               	True
 		//	Quantity Used:        	1
 		//	Service Level:        	Standard
 		//	Service Type:         	L1-L3
 		//	Starts:               	07/20/2012
 		//	Ends:                 	07/20/2013
 		
 		// assert all of the product subscription's fields match the entitlement cert
 		Assert.assertEquals(productSubscription.productName, entitlementCert.orderNamespace.productName, "productName from ProductSubscription in list --consumed matches productName from OrderNamespace in this entitlementCert");
 		Assert.assertTrue(productSubscription.provides.containsAll(providedProductNames) && providedProductNames.containsAll(productSubscription.provides), "The consumed product subscription provides all the expected products "+providedProductNames+" from the provided ProductNamespaces in the entitlementCert.");
 		Assert.assertEquals(productSubscription.productId, entitlementCert.orderNamespace.productId, "productId from ProductSubscription in list --consumed matches productId from OrderNamespace in this entitlementCert");
 		Assert.assertEquals(productSubscription.contractNumber, entitlementCert.orderNamespace.contractNumber, "contractNumber from ProductSubscription in list --consumed matches contractNumber from OrderNamespace in this entitlementCert");
 		Assert.assertEquals(productSubscription.accountNumber, entitlementCert.orderNamespace.accountNumber, "accountNumber from ProductSubscription in list --consumed matches accountNumber from OrderNamespace in this entitlementCert");
 		Calendar now = Calendar.getInstance();
 		if (now.after(entitlementCert.orderNamespace.startDate) && now.before(entitlementCert.orderNamespace.endDate)) {
 			Assert.assertTrue(productSubscription.isActive, "isActive is True when the current time ("+EntitlementCert.formatDateString(now)+") is between the start/end dates in this entitlementCert");
 		} else {
 			Assert.assertFalse(productSubscription.isActive, "isActive is False when the current time ("+EntitlementCert.formatDateString(now)+") is NOT between the start/end dates in this entitlementCert");
 		}
 		Assert.assertEquals(productSubscription.quantityUsed.toString(), entitlementCert.orderNamespace.quantityUsed, "quantityUsed from ProductSubscription in list --consumed matches quantityUsed from OrderNamespace in this entitlementCert");
 		Assert.assertEquals(productSubscription.serviceLevel, entitlementCert.orderNamespace.supportLevel, "serviceLevel from ProductSubscription in list --consumed matches supportLevel from OrderNamespace in this entitlementCert");
 		Assert.assertEquals(productSubscription.serviceType, entitlementCert.orderNamespace.supportType, "serviceType from ProductSubscription in list --consumed matches serviceType from OrderNamespace in this entitlementCert");
 		Assert.assertEquals(ProductSubscription.formatDateString(productSubscription.startDate), ProductSubscription.formatDateString(entitlementCert.orderNamespace.startDate), "startDate from ProductSubscription in list --consumed matches startDate from OrderNamespace ("+OrderNamespace.formatDateString(entitlementCert.orderNamespace.startDate)+") after conversion from GMT in EntitlementCert to local time.");
 		Assert.assertEquals(ProductSubscription.formatDateString(productSubscription.endDate), ProductSubscription.formatDateString(entitlementCert.orderNamespace.endDate), "endDate from ProductSubscription in list --consumed matches endDate from OrderNamespace ("+OrderNamespace.formatDateString(entitlementCert.orderNamespace.endDate)+") after conversion from GMT in EntitlementCert to local time.");
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: RHEL Personal should be the only available subscription to a consumer registered as type person",
 			groups={"EnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test() throws JSONException {
 //		String rhelPersonalProductId = getProperty("sm.rhpersonal.productId", "");
 //		if (rhelPersonalProductId.equals("")) throw new SkipException("This testcase requires specification of a RHPERSONAL_PRODUCTID.");
 		
 		// decide what username and password to test with
 		String username = sm_clientUsername;
 		String password = sm_clientPassword;
 		String owner = sm_clientOrg;
 		if (!sm_rhpersonalUsername.equals("")) {
 			username = sm_rhpersonalUsername;
 			password = sm_rhpersonalPassword;
 			owner = sm_rhpersonalOrg;
 		}
 		
 		// register a person
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(username, password, owner, null, ConsumerType.person, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 
 		// assert that subscriptions with personal productIds are available to this person consumer
 		List<SubscriptionPool> subscriptionPools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		for (String personProductId : getPersonProductIds()) {
 			SubscriptionPool rhelPersonalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", personProductId, subscriptionPools);
 			Assert.assertNotNull(rhelPersonalPool,"Personal ProductId '"+personProductId+"' is available to this consumer registered as type person");
 		}
 	
 		// assert that personal subscriptions are the only available pools to this person consumer
 		for (SubscriptionPool subscriptionPool : subscriptionPools) {
 			Assert.assertTrue(getPersonProductIds().contains(subscriptionPool.productId), "This available ProductId '"+subscriptionPool.productId+"' available to the registered person is among the expected list of personal products that we expect to be consumable by this person.");
 		}
 	}
 	@AfterGroups(groups={}, value="EnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test", alwaysRun=true)
 	public void teardownAfterEnsureOnlyRHELPersonalIsAvailableToRegisteredPerson_Test() {
 		if (clienttasks!=null) clienttasks.unregister_(null, null, null);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: RHEL Personal should not be an available subscription to a consumer registered as type system",
 			groups={"EnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test() throws JSONException {
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, ConsumerType.system, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 		SubscriptionPool rhelPersonalPool = null;
 		
 		for (String personProductId : getPersonProductIds()) {
 			// assert that RHEL Personal *is not* included in --available subscription pools
 			rhelPersonalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", personProductId, clienttasks.getCurrentlyAvailableSubscriptionPools());
 			Assert.assertNull(rhelPersonalPool,"Personal ProductId '"+personProductId+"' is NOT available to this consumer from --available subscription pools when registered as type system.");
 			
 			/* behavior changed on list --all --available  (3/4/2011)
 			// also assert that RHEL Personal *is* included in --all --available subscription pools
 			rhelPersonalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", personProductId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 			Assert.assertNotNull(rhelPersonalPool,"Personal ProductId '"+personProductId+"' is included in --all --available subscription pools when registered as type system.");
 			*/
 			// also assert that RHEL Personal *is not* included in --all --available subscription pools
 			rhelPersonalPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", personProductId, clienttasks.getCurrentlyAllAvailableSubscriptionPools());
 			Assert.assertNull(rhelPersonalPool,"Personal ProductId '"+personProductId+"' is NOT available to this consumer from --all --available subscription pools when registered as type system.");
 		}
 	}
 	@AfterGroups(groups={}, value="EnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test", alwaysRun=true)
 	public void teardownAfterEnsureRHELPersonalIsNotAvailableToRegisteredSystem_Test() {
 		if (clienttasks!=null) clienttasks.unregister_(null, null, null);
 	}
 	
 
 	
 	
 	@Test(	description="subscription-manager: subcription manager list consumed should be permitted without being registered",
 			groups={"blockedByBug-725870"},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void AttemptListConsumedWithoutBeingRegistered_Test() {
 		
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult listResult = clienttasks.listConsumedProductSubscriptions();
 		
 		// assert redemption results
 		Assert.assertEquals(listResult.getStdout().trim(), "No consumed subscription pools to list","List consumed should NOT require that the system be registered.");
 		Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(0),"Exit code from list consumed when executed without being registered.");
 	}
 	
 	@Test(	description="subscription-manager: subcription manager list installed should be permitted without being registered",
 			groups={"blockedByBug-725870"},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void AttemptListInstalledWithoutBeingRegistered_Test() {
 		
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult listResult = clienttasks.listInstalledProducts();
 	}
 	
 	@Test(	description="subscription-manager: subcription manager list should be permitted without being registered",
 			groups={"blockedByBug-725870"},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void AttemptListWithoutBeingRegistered_Test() {
 		
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult listResult = clienttasks.list_(null,null,null,null,null,null,null, null, null);
 		
 		Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(0), "The exit code from the list command indicates a success.");
 	}
 	
 	@Test(	description="subscription-manager: subcription manager list available should require being registered",
 			groups={},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void AttemptListAvailableWithoutBeingRegistered_Test() {
 		SSHCommandResult listResult;
 		clienttasks.unregister(null,null,null);
 		
 		listResult = clienttasks.list_(null,true,null,null,null,null,null, null, null);
 		//Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(1), "The exit code from the list available command indicates a problem.");
 		//Assert.assertEquals(listResult.getStdout().trim(), "Error: You need to register this system by running `register` command before using this option.","Attempting to list available subscriptions should require registration.");
 		// results changed after bug fix 749332
 		Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(255), "The exit code from the list available command indicates a problem.");
 		Assert.assertEquals(listResult.getStdout().trim(), clienttasks.msg_ConsumerNotRegistered,"Attempting to list --available subscriptions should require registration.");
 
 		listResult = clienttasks.list_(true,true,null,null,null,null,null, null, null);
 		//Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(1), "The exit code from the list all available command indicates a problem.");
 		//Assert.assertEquals(listResult.getStdout().trim(), "Error: You need to register this system by running `register` command before using this option.","Attempting to list all available subscriptions should require registration.");
 		// results changed after bug fix 749332
 		Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(255), "The exit code from the list all available command indicates a problem.");
 		Assert.assertEquals(listResult.getStdout().trim(), clienttasks.msg_ConsumerNotRegistered,"Attempting to list --all --available subscriptions should require registration.");
 
 	}
 	
 	
 	
 	
 	
 	@Test(	description="subscription-manager: subcription manager list future subscription pools for a system",
 			groups={"blockedByBug-672562"},
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void ListAllAvailableWithFutureOnDate_Test() throws Exception {
 		
 		List<List<Object>> allFutureJSONPoolsDataAsListOfLists = getAllFutureJSONPoolsDataAsListOfLists(ConsumerType.system);
 		if (allFutureJSONPoolsDataAsListOfLists.size()<1) throw new SkipException("Cannot find any future subscriptions to test");
 		
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		List<String> onDatesTested = new ArrayList<String>();
 		
 		
 		// assemble a list of ondate strings to test
 		List<String> onDatesToTest = new ArrayList<String>();
 		for (List<Object> l : allFutureJSONPoolsDataAsListOfLists) {
 			JSONObject futureJSONPool = (JSONObject) l.get(0);
 			
 			String startDate = futureJSONPool.getString("startDate");
 			
 			// add one day to this start date to use for subscription-manager list --ondate test (ASSUMPTION: these subscriptions last longer than one day)
 			Calendar onDate = parseISO8601DateString(startDate,"GMT"); onDate.add(Calendar.DATE, 1);
 			DateFormat yyyy_MM_dd_DateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			String onDateToTest = yyyy_MM_dd_DateFormat.format(onDate.getTime());
 			
 			if (!onDatesToTest.contains(onDateToTest)) onDatesToTest.add(onDateToTest);
 		}
 		
 		// assemble a list of future subscription poolIds to verify in the subscription-manager list --available --ondate
 		List<String> futurePoolIds = new ArrayList<String>();
 		for (List<Object> l : allFutureJSONPoolsDataAsListOfLists) {
 			JSONObject jsonPool = (JSONObject) l.get(0);
 			String id = jsonPool.getString("id");
 			futurePoolIds.add(id);
 		}
 		
 		// use this list to store all of the poolIds found on a future date listing
 		List<String>futurePoolIdsListedOnDate = new ArrayList<String>();
 		
 		for (List<Object> l : allFutureJSONPoolsDataAsListOfLists) {
 			JSONObject futureJSONPool = (JSONObject) l.get(0);
 			
 			// add one day to this start date to use for subscription-manager list --ondate test
 			Calendar onDate = parseISO8601DateString(futureJSONPool.getString("startDate"),"GMT"); onDate.add(Calendar.DATE, 1);
 			DateFormat yyyy_MM_dd_DateFormat = new SimpleDateFormat("yyyy-MM-dd");
 			String onDateToTest = yyyy_MM_dd_DateFormat.format(onDate.getTime());
 			
 			// if we already tested with this ondate string, then continue
 			if (onDatesTested.contains(onDateToTest)) continue;
 			
 			// list all available onDateToTest
 			SSHCommandResult listResult = clienttasks.list_(true,true,null,null,null,onDateToTest,null, null, null);
 			Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --all --available --ondate command indicates a success.");
 
 			List<SubscriptionPool> subscriptionPools = SubscriptionPool.parse(listResult.getStdout());
 			Assert.assertTrue(subscriptionPools.size()>=1,"A list of SubscriptionPools was returned from the list module using a valid ondate option.");
 			
 			// assert that each of the SubscriptionPools listed is indeed active on the requested date
 			for (SubscriptionPool subscriptionPool : subscriptionPools) {
 				JSONObject jsonPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+subscriptionPool.poolId));
 				Calendar startDate = parseISO8601DateString(jsonPool.getString("startDate"),"GMT");	// "startDate":"2012-02-08T00:00:00.000+0000"
 				Calendar endDate = parseISO8601DateString(jsonPool.getString("endDate"),"GMT");	// "endDate":"2013-02-07T00:00:00.000+0000"
 				Boolean activeSubscription = jsonPool.getBoolean("activeSubscription");	// TODO I don't yet understand how to test this property.  I'm assuming it is true
 				Assert.assertTrue(startDate.before(onDate)&&endDate.after(onDate)&&activeSubscription,"SubscriptionPool '"+subscriptionPool.poolId+"' is indeed active and listed as available ondate='"+onDateToTest+"'.");
 							
 				// for follow-up assertions keep a list of all the futurePoolIds that are found on the listing date (excluding pools that are active now since they are not considered future pools)
 				if (startDate.after(now)) {
 					if (!futurePoolIdsListedOnDate.contains(subscriptionPool.poolId)) futurePoolIdsListedOnDate.add(subscriptionPool.poolId);
 				}
 			}
 			
 			// remember that this date was just tested
 			onDatesTested.add(onDateToTest);
 		}
 		
 		Assert.assertEquals(futurePoolIdsListedOnDate.size(), futurePoolIds.size(),"The expected count of all of the expected future subscription pools for systems was listed on future dates.");
 		for (String futurePoolId : futurePoolIds) futurePoolIdsListedOnDate.remove(futurePoolId);
 		Assert.assertTrue(futurePoolIdsListedOnDate.isEmpty(),"All of the expected future subscription pools for systems were listed on future dates.");
 	}
 	
 	
 	
 	@Test(	description="subscription-manager: subcription manager list all available should filter by servicelevel when this option is passed.",
 			groups={"blockedByBug-800933","blockedByBug-800999"},
 			dataProvider="getListAvailableWithServicelevelData",
 			enabled=true)
 			@ImplementsNitrateTest(caseId=157228)
 	public void ListAvailableWithServicelevel_Test(Object bugzilla, String servicelevel) throws Exception {
 		SSHCommandResult listResult;
 		List<SubscriptionPool> expectedSubscriptionPools, filteredSubscriptionPools;
 				
 		// list all available (without service level)
 		listResult = clienttasks.list_(true,true,null,null,null,null,null,null,null);
 		List<SubscriptionPool> allAvailableSubscriptionPools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		
 		// determine the subset of expected pools with a case-insensitive matching servicelevel
 		expectedSubscriptionPools = SubscriptionPool.findAllInstancesWithCaseInsensitiveMatchingFieldFromList("serviceLevel", servicelevel, allAvailableSubscriptionPools);
 
 		// list all available filtered by servicelevel
 		listResult = clienttasks.list_(true,true,null,null,servicelevel,null,null,null,null);
 		Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --all --available --servicelevel command indicates a success.");
 		
 		// assert results
 		filteredSubscriptionPools = SubscriptionPool.parse(listResult.getStdout());
 		Assert.assertTrue(filteredSubscriptionPools.containsAll(expectedSubscriptionPools),"The actual list of --all --available --servicelevel=\""+servicelevel+"\" SubscriptionPools contains all of the expected SubscriptionPools (the expected list contains only pools with ServiceLevel=\""+servicelevel+"\")");
 		Assert.assertTrue(expectedSubscriptionPools.containsAll(filteredSubscriptionPools),"The expected list of SubscriptionPools contains all of the actual SubscriptionPools returned by list --all --available --servicelevel=\""+servicelevel+"\".");
 		if (expectedSubscriptionPools.isEmpty()) Assert.assertEquals(listResult.getStdout().trim(), "No available subscription pools to list","Expected message when no subscription remain after list is filtered by --servicelevel=\""+servicelevel+"\".");
 				
 		// list all available (without service level)
 		listResult = clienttasks.list_(false,true,null,null,null,null,null,null,null);
 		List<SubscriptionPool> availableSubscriptionPools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		
 		// determine the subset of expected pools with a matching servicelevel
 		expectedSubscriptionPools = SubscriptionPool.findAllInstancesWithCaseInsensitiveMatchingFieldFromList("serviceLevel", servicelevel, availableSubscriptionPools);
 		
 		// list available filtered by servicelevel
 		listResult = clienttasks.list_(false,true,null,null,servicelevel,null,null,null,null);
 		Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --all --available --servicelevel command indicates a success.");
 		
 		// assert results
 		filteredSubscriptionPools = SubscriptionPool.parse(listResult.getStdout());
 		Assert.assertTrue(filteredSubscriptionPools.containsAll(expectedSubscriptionPools),"The actual list of --available --servicelevel=\""+servicelevel+"\" SubscriptionPools contains all of the expected SubscriptionPools (the expected list contains only pools with ServiceLevel=\""+servicelevel+"\")");
 		Assert.assertTrue(expectedSubscriptionPools.containsAll(filteredSubscriptionPools),"The expected list of SubscriptionPools contains all of the actual SubscriptionPools returned by list --available --servicelevel=\""+servicelevel+"\".");
 		if (expectedSubscriptionPools.isEmpty()) Assert.assertEquals(listResult.getStdout().trim(), "No available subscription pools to list","Expected message when no subscription remain after list is filtered by --servicelevel=\""+servicelevel+"\".");
 	}
 
 	
 	@Test(	description="subscription-manager: subcription manager list consumed should filter by servicelevel when this option is passed.",
 			groups={"blockedByBug-800933","blockedByBug-800999"},
 			dataProvider="getConsumedWithServicelevelData",
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void ListConsumedWithServicelevel_Test(Object bugzilla, String servicelevel) throws Exception {
 		SSHCommandResult listResult;
 		List<ProductSubscription> expectedProductSubscriptions, filteredProductSubscriptions;
 				
 		// list consumed (without service level)
 		listResult = clienttasks.list_(false,false,true,null,null,null,null,null,null);
 		List<ProductSubscription> allConsumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		
 		// determine the subset of expected pools with a matching servicelevel
 		// CASE SENSITIVE expectedProductSubscriptions = ProductSubscription.findAllInstancesWithMatchingFieldFromList("serviceLevel", servicelevel, allConsumedProductSubscriptions);
 		expectedProductSubscriptions = ProductSubscription.findAllInstancesWithCaseInsensitiveMatchingFieldFromList("serviceLevel", servicelevel, allConsumedProductSubscriptions);
 
 		// list consumed filtered by servicelevel
 		listResult = clienttasks.list_(false,false,true,null,servicelevel,null,null,null,null);
 		Assert.assertEquals(listResult.getExitCode(), Integer.valueOf(0), "The exit code from the list --consumed --servicelevel command indicates a success.");
 		
 		// assert results
 		filteredProductSubscriptions = ProductSubscription.parse(listResult.getStdout());
 		Assert.assertTrue(filteredProductSubscriptions.containsAll(expectedProductSubscriptions),"The actual list of --consumed --servicelevel=\""+servicelevel+"\" ProductSubscriptions contains all of the expected ProductSubscriptions (the expected list contains only consumptions with ServiceLevel=\""+servicelevel+"\")");
 		Assert.assertTrue(expectedProductSubscriptions.containsAll(filteredProductSubscriptions),"The expected list of ProductSubscriptions contains all of the actual ProductSubscriptions returned by list --consumed --servicelevel=\""+servicelevel+"\".");
 		if (expectedProductSubscriptions.isEmpty()) Assert.assertEquals(listResult.getStdout().trim(), "No consumed subscription pools to list","Expected message when no consumed subscriptions remain after list is filtered by --servicelevel=\""+servicelevel+"\".");
 	}
 	
 	// Candidates for an automated Test:
 	// TODO Bug 709412 - subscription manager cli uses product name comparisons in the list command
 	// TODO Bug 710141 - OwnerInfo needs to only show info for pools that are active right now, for all the stats
 	// TODO Bug 734880 - subscription-manager list --installed reports differently between LEGACY vs NEW SKU subscriptions  (Note: Bryan says that this had nothing to do with Legacy vs Non Legacy - it was simply a regression in bundled products when stacking was introduced)
 	// TODO Bug 803386 - display product ID in product details pane on sm-gui and cli
 	// TODO Bug 805415 - s390x Partially Subscribed  (THIS IS EFFECTIVELY A COMPLIANCE TEST ON A 0 SOCKET SUBSCRIPTION/PRODUCT)
 	
 	
 	// Configuration methods ***********************************************************************
 	
 	
 	
 	@BeforeClass(groups="setup")
 	public void registerBeforeClass() {
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 	}
 	
 	// NOTE: This method is not necessary, but adds a little more spice to ListAvailableWithFutureOnDate_Test
 	@BeforeClass(groups="setup", dependsOnMethods="registerBeforeClass")
 	public void createFutureSubscriptionPoolBeforeClass() throws Exception {
 		// don't bother attempting to create a subscription unless onPremises
 //		if (!sm_serverType.equals(CandlepinType.standalone)) return;
 		if (server==null) {
 			log.warning("Skipping createFutureSubscriptionPoolBeforeClass() when server is null.");
 			return;	
 		}
 		
 		// find a randomly available product id
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		String randomAvailableProductId = pool.productId;
 		
 		// create a future subscription and refresh pools for it
 		JSONObject futureJSONPool = CandlepinTasks.createSubscriptionAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, 15, 5/*years*/*365*24*60, 6/*years*/*365*24*60, getRandInt(), getRandInt(), randomAvailableProductId, null);
 	}
 	
 
 	@BeforeGroups(groups="setup",value="unsubscribeBeforeGroup")
 	public void unsubscribeBeforeGroup() {
 		//clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribe_(true, null, null, null, null);
 	}
 	
 	@BeforeClass(groups="setup")
 	public void createSubscriptionsWithVariationsOnProductAttributeSockets() throws JSONException, Exception {
 		String name,productId;
 		List<String> providedProductIds = new ArrayList<String>();
 		Map<String,String> attributes = new HashMap<String,String>();
 		JSONObject jsonEngProduct, jsonMktProduct, jsonSubscription;
 		if (server==null) {
 			log.warning("Skipping createSubscriptionsWithVariationsOnProductAttributeSockets() when server is null.");
 			return;	
 		}
 //debugTesting if (true) return;
 	
 		// Awesome OS for 0 sockets
 		name = "Awesome OS for systems with sockets value=0";
 		productId = "0-sockets";
 		providedProductIds.clear();
 		providedProductIds.add("90001");
 		attributes.clear();
 		attributes.put("sockets", "0");
 		attributes.put("version", "1.0");
 		attributes.put("variant", "server");
 		attributes.put("arch", "ALL");
 		attributes.put("warning_period", "30");
 		// delete already existing subscription and products
 		CandlepinTasks.deleteSubscriptionsAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+providedProductIds.get(0));
 		// create a new engineering product, marketing product that provides the engineering product, and a subscription for the marketing product
 		attributes.put("type", "SVC");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name+" BITS", providedProductIds.get(0), 1, attributes, null);
 		attributes.put("type", "MKT");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name, productId, 1, attributes, null);
 		CandlepinTasks.createSubscriptionAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, 20, -1*24*60/*1 day ago*/, 15*24*60/*15 days from now*/, getRandInt(), getRandInt(), productId, providedProductIds);
 
 	
 		// Awesome OS for no sockets
 		name = "Awesome OS for systems with no sockets";
 		productId = "no-sockets";
 		providedProductIds.clear();
 		providedProductIds.add("90002");
 		attributes.clear();
 		attributes.remove("sockets");
 		attributes.put("version", "0.0");
 		attributes.put("variant", "workstation");
 		attributes.put("arch", "ALL");
 		attributes.put("warning_period", "30");
 		// delete already existing subscription and products
 		CandlepinTasks.deleteSubscriptionsAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+providedProductIds.get(0));
 		// create a new engineering product, marketing product that provides the engineering product, and a subscription for the marketing product
 		attributes.put("type", "SVC");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name+" BITS", providedProductIds.get(0), 1, attributes, null);
 		attributes.put("type", "MKT");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name, productId, 1, attributes, null);
 		CandlepinTasks.createSubscriptionAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, 20, -1*24*60/*1 day ago*/, 15*24*60/*15 days from now*/, getRandInt(), getRandInt(), productId, providedProductIds);
 		
 		
 		// Awesome OS for "zero" sockets
 		name = "Awesome OS for systems with sockets value=\"unknown\"";
 		productId = "unknown-sockets";
 		providedProductIds.clear();
 		providedProductIds.add("90003");
 		attributes.clear();
 		attributes.put("sockets", "zero");
 		attributes.put("version", "0.0");
 		attributes.put("variant", "workstation");
 		attributes.put("arch", "ALL");
 		attributes.put("warning_period", "30");
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId = "795552";
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			log.warning("Skipping the creation of product: "+name);
 		} else {
 		// END OF WORKAROUND
 		// delete already existing subscription and products
 		CandlepinTasks.deleteSubscriptionsAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+providedProductIds.get(0));
 		// create a new engineering product, marketing product that provides the engineering product, and a subscription for the marketing product
 		attributes.put("type", "SVC");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name+" BITS", providedProductIds.get(0), 1, attributes, null);
 		attributes.put("type", "MKT");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name, productId, 1, attributes, null);
 		CandlepinTasks.createSubscriptionAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, 20, -1*24*60/*1 day ago*/, 15*24*60/*15 days from now*/, getRandInt(), getRandInt(), productId, providedProductIds);
 		}
 		
 		
 		// Awesome OS for null sockets
 		name = "Awesome OS for systems with sockets value=null";
 		productId = "null-sockets";
 		providedProductIds.clear();
 		providedProductIds.add("90004");
 		attributes.clear();
 		attributes.put("sockets", null);
 		attributes.put("version", "0.0");
 		attributes.put("variant", "server");
 		attributes.put("arch", "ALL");
 		attributes.put("warning_period", "30");
 		// TEMPORARY WORKAROUND FOR BUG
 		invokeWorkaroundWhileBugIsOpen = true;
 		String bugId1 = "807452";
 		String bugId2 = "813529";
 		try {if (invokeWorkaroundWhileBugIsOpen&&(BzChecker.getInstance().isBugOpen(bugId1)||BzChecker.getInstance().isBugOpen(bugId2))) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId1).toString()+" Bugzilla "+bugId1+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId1+")");log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId2).toString()+" Bugzilla "+bugId2+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId2+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			log.warning("Skipping the creation of product: "+name);
 		} else {
 		// END OF WORKAROUND
 		// delete already existing subscription and products
 		CandlepinTasks.deleteSubscriptionsAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+productId);
 		CandlepinTasks.deleteResourceUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, "/products/"+providedProductIds.get(0));
 		// create a new engineering product, marketing product that provides the engineering product, and a subscription for the marketing product
 		attributes.put("type", "SVC");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name+" BITS", providedProductIds.get(0), 1, attributes, null);
 		attributes.put("type", "MKT");
 		CandlepinTasks.createProductUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, name, productId, 1, attributes, null);
 		CandlepinTasks.createSubscriptionAndRefreshPoolsUsingRESTfulAPI(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, sm_clientOrg, 20, -1*24*60/*1 day ago*/, 15*24*60/*15 days from now*/, getRandInt(), getRandInt(), productId, providedProductIds);
 		}
 		
 		
 		// TODO: To get the product certs, use the CandlepinTasks REST API:
         //"url": "/products/{product_uuid}/certificate", 
         //"GET"
 
 	}	
 
 	
 	
 	// Data Providers ***********************************************************************
 	
 	
 	
 	@DataProvider(name="getListAvailableWithServicelevelData")
 	public Object[][] getListAvailableWithServicelevelDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getListAvailableWithServicelevelDataAsListOfLists());
 	}
 	protected List<List<Object>>getListAvailableWithServicelevelDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// register with force (so we can find the org to which the sm_clientUsername belongs in case sm_clientOrg is null)
 		String org = sm_clientOrg;
 		if (org==null) {
 			String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, "SubscriptionServicelevelConsumer", null, null, null, null, (String)null, null, null, true, false, null, null, null));
 			org = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername,sm_clientPassword,sm_serverUrl,consumerId);
 		}
 		// get all the valid service levels available to this org	
 		for (String serviceLevel : CandlepinTasks.getServiceLevelsForOrgKey(sm_clientUsername, sm_clientPassword, sm_serverUrl, org)) {
 			ll.add(Arrays.asList(new Object[] {null,	serviceLevel}));
 			ll.add(Arrays.asList(new Object[] {null,	randomizeCaseOfCharactersInString(serviceLevel)}));	// run again with the serviceLevel case randomized
 		}
 		ll.add(Arrays.asList(new Object[] {null,	""}));
 		ll.add(Arrays.asList(new Object[] {null,	"FOO"}));
 
 		
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getConsumedWithServicelevelData")
 	public Object[][] getConsumedWithServicelevelDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getConsumedWithServicelevelDataAsListOfLists());
 	}
 	protected List<List<Object>>getConsumedWithServicelevelDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// register with force and subscribe to all available subscriptions collectively
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, "SubscriptionServicelevelConsumer", null, null, null, null, (String)null, null, null, true, false, null, null, null));
 		String org = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername,sm_clientPassword,sm_serverUrl,consumerId);
 		clienttasks.subscribeToTheCurrentlyAllAvailableSubscriptionPoolsCollectively();
 		
 		// get all the valid service levels available to this org	
 		for (String serviceLevel : CandlepinTasks.getServiceLevelsForOrgKey(sm_clientUsername, sm_clientPassword, sm_serverUrl, org)) {
 			Object bugzilla = null;
 			if (serviceLevel.equals("None")) bugzilla = new BlockedByBzBug("842170");
 			ll.add(Arrays.asList(new Object[] {bugzilla,	serviceLevel}));
 		}
 		ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("842170"),	""}));
 		ll.add(Arrays.asList(new Object[] {null,							"FOO"}));
 
 		
 		return ll;
 	}
 }
