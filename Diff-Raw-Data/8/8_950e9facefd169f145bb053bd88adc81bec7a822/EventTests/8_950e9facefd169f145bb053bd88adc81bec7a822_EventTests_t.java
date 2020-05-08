 package com.redhat.qe.sm.cli.tests;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.ConsumerCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.sun.syndication.feed.synd.SyndEntryImpl;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 
 
 
 /**
  * @author jsefler
  *
  *
  *
  * https://engineering.redhat.com/trac/Entitlement/wiki/CandlepinRequirements
  *    (A means automated, B means blocked)
 Events
    1. The following events should be raised
 A         1. Consumer Created
 A         2. Consumer Updated
 A         3. Consumer Deleted
 A         4. Pool Created
 A         5. Pool Updated
 A         6. Pool Deleted
 A         7. Entitlement Created
 A            Entitlement Modified
 A         8. Entitlement Deleted
 B         9. Product Created
 B        10. Product Deleted
 A        11. Owner Created
 A        12. Owner Deleted
 A        13. Export Created
 A        14. Import Done 
 A   2. Events should be consumable via RSS based on owner
 A   3. Events should be consumable via RSS based on consumer
    4. AMQP
          1. It should be possible to enable each message type to be published to an AMQP Bus.
          2. It should be possible to publish no messages to the AMQP bus. 
 
 
 jsefler 7/26/2011
 TODO CREATE A TEST TO TRIGGER ALL OF THESE EVENTS.  MANY ARE NEWER THAN WHEN THIS SCRIPT WAS ORIGINALLY WRITTEN
 EVENTS FOUND IN /candlepin/proxy/src/main/java/org/fedoraproject/candlepin/audit/EventAdapterImpl.java
 
 MESSAGES.put("CONSUMERCREATED", I18n.marktr("{0} created new consumer {1}"));
 MESSAGES.put("CONSUMERMODIFIED", I18n.marktr("{0} modified the consumer {1}"));
 MESSAGES.put("CONSUMERDELETED", I18n.marktr("{0} deleted the consumer {1}"));
 MESSAGES.put("OWNERCREATED", I18n.marktr("{0} created new owner {1}"));
 MESSAGES.put("OWNERMODIFIED", I18n.marktr("{0} modified the owner {1}"));
 MESSAGES.put("OWNERDELETED", I18n.marktr("{0} deleted the owner {1}"));
 MESSAGES.put("ENTITLEMENTCREATED",
     I18n.marktr("{0} consumed a subscription for product {1}"));
 MESSAGES.put("ENTITLEMENTMODIFIED",
     I18n.marktr("{0} modified a subscription for product {1}"));
 MESSAGES.put("ENTITLEMENTDELETED",
     I18n.marktr("{0} returned the subscription for {1}"));
 MESSAGES.put("POOLCREATED", I18n.marktr("{0} created a pool for product {1}"));
 MESSAGES.put("POOLMODIFIED", I18n.marktr("{0} modified a pool for product {1}"));
 MESSAGES.put("POOLDELETED", I18n.marktr("{0} deleted a pool for product {1}"));
 MESSAGES.put("EXPORTCREATED",
     I18n.marktr("{0} created an export for consumer {1}"));
 MESSAGES.put("IMPORTCREATED", I18n.marktr("{0} imported a manifest for owner {1}"));
 MESSAGES.put("USERCREATED", I18n.marktr("{0} created new user {1}"));
 MESSAGES.put("USERMODIFIED", I18n.marktr("{0} modified the user {1}"));
 MESSAGES.put("USERDELETED", I18n.marktr("{0} deleted the user {1}"));
 MESSAGES.put("ROLECREATED", I18n.marktr("{0} created new role {1}"));
 MESSAGES.put("ROLEMODIFIED", I18n.marktr("{0} modified the role {1}"));
 MESSAGES.put("ROLEDELETED", I18n.marktr("{0} deleted the role {1}"));
 MESSAGES.put("SUBSCRIPTIONCREATED",
     I18n.marktr("{0} created new subscription for product {1}"));
 MESSAGES.put("SUBSCRIPTIONMODIFIED",
     I18n.marktr("{0} modified a subscription for product {1}"));
 MESSAGES.put("SUBSCRIPTIONDELETED",
     I18n.marktr("{0} deleted a subscription for product {1}"));
 MESSAGES.put("ACTIVATIONKEYCREATED",
     I18n.marktr("{0} created the activation key {1}"));
 MESSAGES.put("ACTIVATIONKEYDELETED",
     I18n.marktr("{0} deleted the activation key {1}"));
  */
 
 
 @Test(groups={"EventTests"})
 public class EventTests extends SubscriptionManagerCLITestScript{
 
 	// Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: events: Consumer Created is sent over an RSS atom feed.",
 			groups={"ConsumerCreated_Test"}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void ConsumerCreated_Test() throws Exception {
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		String ownerKey = sm_clientOrg;
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
  
         // fire a register event
 		clienttasks.register(sm_clientUsername,sm_clientPassword,ownerKey,null,null,null,null, null, (String)null, null, null, null, null, null);
 		String[] newEventTitles = new String[]{"CONSUMER CREATED"};
 
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{sm_clientUsername+" created new consumer "+clienttasks.hostname};
 		}
 		// END OF WORKAROUND
 		
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		
 		// assert the consumer feed...
 		assertTheNewConsumerFeed(ownerKey, consumerCert.consumerid, null, newEventTitles);
 		
 		// assert the owner feed...
 		assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
        
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Enitlement Created is sent over an RSS atom feed.",
			groups={"EntitlementCreated_Test"}, dependsOnGroups={"ConsumerCreated_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=50403)
	public void EntitlementCreated_Test() throws Exception {
 		
 		// test prerequisites
 
 		// get the owner and consumer feeds before we test the firing of a new event
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,consumerCert.consumerid);
         SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         SyndFeed oldConsumerFeed = CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
  
         // fire a subscribe event
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		//SubscriptionPool pool = pools.get(0); // pick the first pool
 		testPool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 // debugging randlomly picked standalone non-zero virt_limit pools
 //testPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", "awesomeos-virt-4", pools);
 //testPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("productId", "awesomeos-virt-unlimited", pools);
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(testPool);
 		String[] newEventTitles = new String[]{"ENTITLEMENT CREATED"};
 
 
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{clienttasks.hostname+" consumed a subscription for product "+testPool.subscriptionName};
 		}
 		// END OF WORKAROUND
 		
 		// assert the consumer feed...
         assertTheNewConsumerFeed(ownerKey, consumerCert.consumerid, oldConsumerFeed, newEventTitles);
         
         // adjust the expected events when the candlepin server is standalone and the pool has a non-zero virt_limit 
 		if (servertasks.statusStandalone && !"0".equals(CandlepinTasks.getPoolAttributeValue(sm_clientUsername, sm_clientPassword, sm_serverUrl, testPool.poolId, "virt_limit"))) {
 			newEventTitles = new String[]{"ENTITLEMENT CREATED","POOL CREATED"};
 		}
 		
 		// assert the owner feed...
 		assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
   
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Pool Modified and Entitlement Modified is sent over an RSS atom feed.",
			groups={"blockedByBug-721141","PoolModifiedAndEntitlementModified_Test","blockedByBug-645597"}, dependsOnGroups={"EntitlementCreated_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void PoolModifiedAndEntitlementModified_Test() throws Exception {
 		if (server==null) throw new SkipException("This test requires an SSH connection to the candlepin server."); 
 
 		// get the owner and consumer feeds before we test the firing of a new event
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,consumerCert.consumerid);
 
 		// get the number of subscriptions this owner owns
 		//JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(serverHostname,serverPort,serverPrefix,clientusername,clientpassword,"/owners/"+ownerKey+"/subscriptions"));	
 			
         // find the first pool id of a currently consumed product
         List<ProductSubscription> products = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		testPool = clienttasks.getSubscriptionPoolFromProductSubscription(products.get(0),sm_clientUsername,sm_clientPassword);
 		Calendar originalStartDate = (Calendar) products.get(0).startDate.clone();
 
         SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         SyndFeed oldConsumerFeed = CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         
 		// fire an modified pool event (and subsequently a modified entitlement event because the pool was modified thereby requiring an entitlement update dropped to the consumer)
 		log.info("To fire a modified pool event (and subsequently a modified entitlement event because the pool is already subscribed too), we will modify pool '"+testPool+"' by subtracting one month from startdate...");
 		Calendar newStartDate = (Calendar) originalStartDate.clone(); newStartDate.add(Calendar.MONTH, -1);
 		updateSubscriptionPoolDatesOnDatabase(testPool,newStartDate,null);
 
 		log.info("Now let's refresh the subscription pools...");
 		JSONObject jobDetail = CandlepinTasks.refreshPoolsUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,ownerKey);
 		jobDetail = CandlepinTasks.waitForJobDetailStateUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,jobDetail,"FINISHED", 10*1000, 3);
 	
 		// assert the consumer feed...
 		List<String> newEventTitles = new ArrayList<String>();
 		newEventTitles.add("ENTITLEMENT MODIFIED");
 //		newEventTitles.add("Unknown event for user System and target "+testPool.subscriptionName);
         assertTheNewConsumerFeed(ownerKey, consumerCert.consumerid, oldConsumerFeed, newEventTitles);
 
 		// assert the owner feed...
 		////assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, new String[]{"ENTITLEMENT MODIFIED", "POOL MODIFIED"});
 		//for (int s=0; s<jsonSubscriptions.length(); s++) newEventTitles.add("POOL MODIFIED");		// NOTE: This is troublesome because the number of POOL MODIFIED events is not this predictable especially when the pool (which is randomly chosen) is a virt pool
 		//assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
         newEventTitles.add("POOL MODIFIED");
         assertTheNewOwnerFeedContains(ownerKey, oldOwnerFeed, newEventTitles);
 
 		// assert the feed...
 		////assertTheNewFeed(oldFeed, new String[]{"ENTITLEMENT MODIFIED", "POOL MODIFIED"});
 		//assertTheNewFeed(oldFeed, newEventTitles);
         assertTheNewFeedContains(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Entitlement Deleted is sent over an RSS atom feed.",
 			groups={"EnititlementDeleted_Test"}, dependsOnGroups={"PoolModifiedAndEntitlementModified_Test"},
 			enabled=true, alwaysRun=true)
 	//@ImplementsTCMS(id="")
 	public void EnititlementDeleted_Test() throws Exception {
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 		// get the owner and consumer feeds before we test the firing of a new event
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,consumerCert.consumerid);
         SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         SyndFeed oldConsumerFeed = CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
  
         // fire an unsubscribe event
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		String[] newEventTitles = new String[]{"ENTITLEMENT DELETED"};
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{clienttasks.hostname+" returned the subscription for "+testPool.subscriptionName};
 		}
 		// END OF WORKAROUND
 
 		// assert the consumer feed...
         assertTheNewConsumerFeed(ownerKey, consumerCert.consumerid, oldConsumerFeed, newEventTitles);
 
         // adjust the expected events when the candlepin server is standalone and the pool has a non-zero virt_limit 
 		if (servertasks.statusStandalone && !"0".equals(CandlepinTasks.getPoolAttributeValue(sm_clientUsername, sm_clientPassword, sm_serverUrl, testPool.poolId, "virt_limit"))) {
 			newEventTitles = new String[]{"ENTITLEMENT DELETED","POOL DELETED"};
 		}
 		
 		// assert the owner feed...
 		assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
 
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Consumer Modified is sent over an RSS atom feed.",
 			groups={"blockedByBug-721141","ConsumerModified_Test"}, dependsOnGroups={"EnititlementDeleted_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void ConsumerModified_Test() throws Exception {
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,consumerCert.consumerid);
         SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         SyndFeed oldConsumerFeed = CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
  
         // fire an facts update event by overriding a fact in /etc/rhsm/facts/event_tests.facts
 		Map<String,String> eventFacts = new HashMap<String,String>();
 		eventFacts.put("events.test.description", "Testing CONSUMER MODIFIED event fires on facts update.");
 		eventFacts.put("events.test.currentTimeMillis", String.valueOf(System.currentTimeMillis()));
 		clienttasks.createFactsFileWithOverridingValues(eventFacts);
 		clienttasks.facts(null,true, null, null, null);
 		// FYI: Another way to fire a consumer modified event is to call CandlepinTasks.setAutohealForConsumer(authenticator, password, url, consumerid, autoheal);
 		String[] newEventTitles = new String[]{"CONSUMER MODIFIED"};
 
 		// assert the consumer feed...
         assertTheNewConsumerFeed(ownerKey, consumerCert.consumerid, oldConsumerFeed, newEventTitles);
 	
 		// assert the owner feed...
 		assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
        
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Consumer Deleted is sent over an RSS atom feed.",
 			groups={"ConsumerDeleted_Test"}, dependsOnGroups={"ConsumerModified_Test","NegativeConsumerUserPassword_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void ConsumerDeleted_Test() throws Exception {
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,consumerCert.consumerid);
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
  
         // fire an unregister event
 		clienttasks.unregister(null, null, null);
 		String[] newEventTitles = new String[]{"CONSUMER DELETED"};
 		
 		// assert the owner feed...
 		assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
 
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Owner Created is sent over an RSS atom feed.",
 			groups={"OwnerCreated_Test"}, dependsOnGroups={"ConsumerDeleted_Test"},
 			enabled=true, alwaysRun=true)
 	//@ImplementsTCMS(id="")
 	public void OwnerCreated_Test() throws Exception {
 		if (server==null) throw new SkipException("This test requires an SSH connection to the candlepin server.");
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
  
         // do something that will fire a create owner event
 		testJSONOwner = servertasks.createOwnerUsingCPC(testOwnerKey);
 		String[] newEventTitles = new String[]{"OWNER CREATED"};
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{sm_serverAdminUsername+" created new owner "+testOwnerKey};
 		}
 		// END OF WORKAROUND
 		
 		// assert the owner feed...
 		assertTheNewOwnerFeed(testJSONOwner.getString("key"), null, newEventTitles);
 		
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Product Created is sent over an RSS atom feed.",
 			groups={"ProductCreated_Test"}, dependsOnGroups={"OwnerCreated_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void ProductCreated_Test() throws JSONException, IllegalArgumentException, IOException, FeedException {
 		if (server==null) throw new SkipException("This test requires an SSH connection to the candlepin server."); 
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 
         // do something that will fire a create product event
 		testJSONProduct = servertasks.createProductUsingCPC(testProductId, testProductId+" Test Product");
 		String[] newEventTitles = new String[]{"PRODUCT CREATED"};
 		
 		// WORKAROUND
 		if (true) throw new SkipException("09/02/2010 Events for PRODUCT CREATED are not yet dev complete.  Agilo Story: http://mgmt1.rhq.lab.eng.bos.redhat.com:8001/web/ticket/3737");
 
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Pool Created is sent over an RSS atom feed.",
 			groups={"PoolCreated_Test"}, dependsOnGroups={"ProductCreated_Test"},
 			enabled=true, alwaysRun=true)
 	//@ImplementsTCMS(id="")
 	public void PoolCreated_Test() throws Exception {
 		if (server==null) throw new SkipException("This test requires an SSH connection to the candlepin server."); 
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 		// get the owner and consumer feeds before we test the firing of a new event
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 
         // do something that will fire a create pool event
 		if (servertasks.branch.equals("ALPHA") || servertasks.branch.equals("BETA") || servertasks.branch.matches("^candlepin-0\\.[012]\\..*$")) {
 			// candlepin branch 0.2-  (createPoolUsingCPC was deprecated in candlepin branch 0.3+)
 			testJSONPool = servertasks.createPoolUsingCPC(testJSONProduct.getString("id"), testProductId+" Test Product", testJSONOwner.getString("id"), "99");
 		} else {
 			// candlepin branch 0.3+
 			testJSONPool = servertasks.createSubscriptionUsingCPC(testOwnerKey, testJSONProduct.getString("id"));
 			JSONObject jobDetail = servertasks.refreshPoolsUsingCPC(testOwnerKey,true);
 			CandlepinTasks.waitForJobDetailStateUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,jobDetail,"FINISHED", 10*1000, 3);
 		}
 		String[] newEventTitles = new String[]{"POOL CREATED"};
 
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{"System created a pool for product "+testJSONProduct.getString("name")};
 		}
 		// END OF WORKAROUND
 		
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 		
 		// TODO
 //		<jsefler> dgoodwin: your timing for the event for POOL CREATED is perfect.  I'll automate a test for it now.  It looks like the event is working.
 //		 dgoodwin dgregor_pto dgao
 //		<dgoodwin> jsefler: cool, three possible cases, creation via candlepin api (post /pools), refresh pools after creating a subscription, and creation of the rh personal "sub pool"
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Pool Deleted is sent over an RSS atom feed.",
 			groups={"PoolDeleted_Test"}, dependsOnGroups={"PoolCreated_Test"},
 			enabled=true, alwaysRun=true)
 	//@ImplementsTCMS(id="")
 	public void PoolDeleted_Test() throws Exception {
 		if (server==null) throw new SkipException("This test requires an SSH connection to the candlepin server."); 
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 		// get the owner and consumer feeds before we test the firing of a new event
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 
         // do something that will fire a delete pool event
 		if (servertasks.branch.equals("ALPHA") || servertasks.branch.equals("BETA") || servertasks.branch.matches("^candlepin-0\\.[012]\\..*$")) {
 			// candlepin branch 0.2-  (createPoolUsingCPC was deprecated in candlepin branch 0.3+)
 			servertasks.deletePoolUsingCPC(testJSONPool.getString("id"));
 		} else {
 			// candlepin branch 0.3+
 			servertasks.deleteSubscriptionUsingCPC(testJSONPool.getString("id"));
 			JSONObject jobDetail = servertasks.refreshPoolsUsingCPC(testOwnerKey,true);
 			CandlepinTasks.waitForJobDetailStateUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,jobDetail,"FINISHED", 10*1000, 3);
 		}
 		String[] newEventTitles = new String[]{"POOL DELETED"};
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{"System deleted a pool for product "+testJSONProduct.getString("name")};
 		}
 		// END OF WORKAROUND
 		
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Product Deleted is sent over an RSS atom feed.",
 			groups={"ProductDeleted_Test"}, dependsOnGroups={"PoolDeleted_Test"},
 			enabled=true, alwaysRun=true)
 	//@ImplementsTCMS(id="")
 	public void ProductDeleted_Test() throws JSONException, IllegalArgumentException, IOException, FeedException {
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 //		IRC CHAT ON 9/3/2010
 //		<dgoodwin> that one is worse =]
 //		 products are detached in the db (as they can also live externally)
 //		<dgoodwin> and deleting them can leave things dangling, we looked at it earlier, saw how hairy it was, and decided to *not* delete them for now
 //		 jsefler: so product delete needs to be handled as separate story, if at all IMO
 //		<jsefler> ok, so then for now there is no event firing of DELETE PRODUCT to test
 //		<dgoodwin> correct
 		
 		// WORKAROUND
 		if (true) throw new SkipException("09/02/2010 Events for PRODUCT DELETED and the cpc delete_product are not yet dev complete.");
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 
         // do something that will fire a delete product event
 		//servertasks.cpc_delete_product(testProduct.getString("id"));
 		String[] newEventTitles = new String[]{"PRODUCT DELETED"};
 		
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Export Created is sent over an RSS atom feed.",
 			groups={"ExportCreated_Test"}, dependsOnGroups={"ProductDeleted_Test"},
 			enabled=true, alwaysRun=true)
 	//@ImplementsTCMS(id="")
 	public void ExportCreated_Test() throws Exception {
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 		// start fresh by unregistering
 		clienttasks.unregister(null, null, null);
 		
 		// register a type=candlepin consumer and subscribe to get an entitlement
 		// NOTE: Without the subscribe, this bugzilla is thrown: 
 		SSHCommandResult result = clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,ConsumerType.candlepin,null,null, null, (String)null, null, false, null, null, null);
 		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		testPool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribe(null, testPool.poolId, null, null, null, null, null, null, null, null);
 		//String consumerKey = result.getStdout().split(" ")[0];
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,consumerCert.consumerid);
         SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         SyndFeed oldConsumerFeed = CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         
         // do something that will fire a exported created event
 		CandlepinTasks.exportConsumerUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,consumerCert.consumerid,"/tmp/export.zip");
 		String[] newEventTitles = new String[]{"EXPORT CREATED"};
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{ownerKey+" created an export for consumer "+consumerCert.name};
 		}
 		// END OF WORKAROUND
 		
 		// assert the consumer feed...
 		assertTheNewConsumerFeed(ownerKey, consumerCert.consumerid, oldConsumerFeed, newEventTitles);
 
 		// assert the owner feed...
 		assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
 
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Import Created is sent over an RSS atom feed.",
 			groups={"ImportCreated_Test"}, dependsOnGroups={"ExportCreated_Test","OwnerCreated_Test"},
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void ImportCreated_Test() throws Exception {
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		String ownerKey = testJSONOwner.getString("key");
         SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		SyndFeed oldOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey, sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
         
         // do something that will fire an import created event
 		CandlepinTasks.importConsumerUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,ownerKey,"/tmp/export.zip");
 		String[] newEventTitles = new String[]{"IMPORT CREATED", "POOL CREATED", "SUBSCRIPTION CREATED"};  // Note: the POOL CREATED comes from the subscribed pool
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{
 					sm_clientOrg+" imported a manifest for owner "+ownerKey,
 					sm_clientOrg+" created a pool for product "+testPool.subscriptionName,
 					sm_clientOrg+" created new subscription for product "+testPool.subscriptionName};
 		}
 		// END OF WORKAROUND
 		
 		// assert the owner feed...
 		//assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, new String[]{"IMPORT CREATED", "POOL CREATED"});
 		assertTheNewOwnerFeed(ownerKey, oldOwnerFeed, newEventTitles);
 
 		// assert the feed...
 		//assertTheNewFeed(oldFeed, new String[]{"IMPORT CREATED", "POOL CREATED", "SUBSCRIPTION CREATED"});
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: Owner Deleted is sent over an RSS atom feed.",
 			groups={"OwnerDeleted_Test"}, dependsOnGroups={"ImportCreated_Test"},
 			enabled=true, alwaysRun=true)
 	//@ImplementsTCMS(id="")
 	public void OwnerDeleted_Test() throws IllegalArgumentException, IOException, FeedException, JSONException {
 		if (server==null) throw new SkipException("This test requires an SSH connection to the candlepin server."); 
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 		String ownerKey = sm_clientOrg;
 		
 		// get the owner and consumer feeds before we test the firing of a new event
 		SyndFeed oldFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
  
 		// do something that will fire a delete owner event
 		servertasks.deleteOwnerUsingCPC(testOwnerKey);
 		String[] newEventTitles = new String[]{"OWNER DELETED"};
 		
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=721136 - jsefler 07/14/2011
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="721136"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			newEventTitles = new String[]{ownerKey+" deleted the owner "+testOwnerKey};
 		}
 		// END OF WORKAROUND
 		
 		// assert the feed...
 		assertTheNewFeed(oldFeed, newEventTitles);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: negative test for super user/password.",
 			groups={"NegativeSuperUserPassword_Test"}, dependsOnGroups={},
 			
 			enabled=true, alwaysRun=true)
 	@ImplementsNitrateTest(caseId=50404)
 	public void NegativeSuperUserPassword_Test() throws IllegalArgumentException, IOException, FeedException {
 		if (sm_serverAdminUsername.equals("")||sm_serverAdminPassword.equals("")) throw new SkipException("This test requires the candlepin server admin username and password credentials.");
 
 		String authuser="",authpwd="";
 		try {
 			// enter the wrong user, correct passwd
 			authuser = sm_serverAdminUsername+getRandInt();
 			authpwd = sm_serverAdminPassword;
 			CandlepinTasks.getSyndFeed(authuser,authpwd,sm_serverUrl);
 			Assert.fail("Expected the candlepin server request for a syndication feed to return an HTTP response code 401 Unauthorized due to invalid authorization credentials "+authuser+":"+authpwd);
 
 		} catch (IOException e) {
 			Assert.assertContainsMatch(e.getMessage(), "HTTP response code: 401", "Atom feed is unauthorized when attempting to authorize with credentials "+authuser+":"+authpwd);
 		}
 		
 		try {
 			// enter the correct user, wrong passwd
 			authuser = sm_serverAdminUsername;
 			authpwd = sm_serverAdminPassword+getRandInt();
 			CandlepinTasks.getSyndFeed(authuser,authpwd,sm_serverUrl);
 			Assert.fail("Expected the candlepin server request for a syndication feed to return an HTTP response code 401 Unauthorized due to invalid authorization credentials "+authuser+":"+authpwd);
 
 		} catch (IOException e) {
 			Assert.assertContainsMatch(e.getMessage(), "HTTP response code: 401", "Atom feed is unauthorized when attempting to authorize with credentials "+authuser+":"+authpwd);
 		}
 		
 		// finally assert success with valid credentials
 		authuser = sm_serverAdminUsername;
 		authpwd = sm_serverAdminPassword;
 		SyndFeed feed = CandlepinTasks.getSyndFeed(authuser,authpwd,sm_serverUrl);
 		Assert.assertTrue(!feed.getEntries().isEmpty(),"Atom feed for all events is successful with valid credentials "+authuser+":"+authpwd);
 	}
 	
 	
 	@Test(	description="subscription-manager: events: negative test for consumer user/password.",
 			groups={"NegativeConsumerUserPassword_Test"}, dependsOnGroups={"ConsumerCreated_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=50404)
 	public void NegativeConsumerUserPassword_Test() throws JSONException, Exception {
 		String authuser="",authpwd="";
 		ConsumerCert consumerCert = clienttasks.getCurrentConsumerCert();
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_serverAdminUsername, sm_serverAdminPassword, sm_serverUrl, consumerCert.consumerid);
 
 		try {
 			// enter the wrong user, correct passwd
 			authuser = sm_client1Username+getRandInt();
 			authpwd = sm_client1Password;
 			CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,authuser,authpwd,sm_serverUrl);
 			Assert.fail("Expected the candlepin server request for a syndication feed to return an HTTP response code 401 Unauthorized due to invalid authorization credentials "+authuser+":"+authpwd);
 
 		} catch (IOException e) {
 			Assert.assertContainsMatch(e.getMessage(), "HTTP response code: 401", "Atom feed is unauthorized when attempting to authorize with credentials "+authuser+":"+authpwd);
 		}
 		
 		try {
 			// enter the correct user, wrong passwd
 			authuser = sm_client1Username;
 			authpwd = sm_client1Password+getRandInt();
 			CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,authuser,authpwd,sm_serverUrl);
 			Assert.fail("Expected the candlepin server request for a syndication feed to return an HTTP response code 401 Unauthorized due to invalid authorization credentials "+authuser+":"+authpwd);
 
 		} catch (IOException e) {
 			Assert.assertContainsMatch(e.getMessage(), "HTTP response code: 401", "Atom feed is unauthorized when attempting to authorize with credentials "+authuser+":"+authpwd);
 		}
 		
 		try {
 			// enter the correct user, correct passwd for super admin atom
 			authuser = sm_client1Username;
 			authpwd = sm_client1Password;
 			CandlepinTasks.getSyndFeed(authuser,authpwd,sm_serverUrl);
 			Assert.fail("Expected the candlepin server request for a syndication feed to return an HTTP response code 401 Unauthorized due to invalid authorization credentials "+authuser+":"+authpwd);
 
 		} catch (IOException e) {
 			Assert.assertContainsMatch(e.getMessage(), "HTTP response code: 403", "Atom feed is forbidden when attempting to authorize with credentials "+authuser+":"+authpwd);
 		}
 		
 		// finally assert success with valid credentials
 		authuser = sm_client1Username;
 		authpwd = sm_client1Password;
 		SyndFeed feed = CandlepinTasks.getSyndFeedForConsumer(ownerKey,consumerCert.consumerid,authuser,authpwd,sm_serverUrl);
 		Assert.assertTrue(!feed.getEntries().isEmpty(),"Atom feed for consumer events is successful with valid credentials "+authuser+":"+authpwd);
 	}
 
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	@BeforeClass(groups="setup")
 	public void setupBeforeClass() throws Exception {
 		// alternative to dependsOnGroups={"RegisterWithCredentials_Test"}
 		// This allows us to satisfy a dependency on registrationDataList making TestNG add unwanted Test results.
 		// This also allows us to individually run this Test Class on Hudson.
 		RegisterWithCredentials_Test(); // needed to populate registrationDataList
 	}
 	
 	
 	
 	// Protected Methods ***********************************************************************
 	
 	protected int getFeedGrowthCount(SyndFeed newFeed, SyndFeed oldFeed) {
 		
 		int g=newFeed.getEntries().size();	// assume all of the entries represent new growth
 		
 		if (oldFeed!=null) {
 			// make sure we are not accidently comparing two feeds that represent two different stacks
 			if (!newFeed.getTitle().equals(oldFeed.getTitle())) Assert.fail("getFeedGrowthCount(oldFeed,newFeed) do not have equivalent SyndEntryImpl titles.  We should probably not be comparing these two feeds.");
 
 			int n=newFeed.getEntries().size()-1;
 			int o=oldFeed.getEntries().size()-1;
 			g=0;
 			while (g<o+1) {
 				int c=0;
 				while (o-c-g>=0 &&
 						// newFeedTitle == oldFeedTitle
 						(((SyndEntryImpl) newFeed.getEntries().get(n-c)).getTitle()).equals(((SyndEntryImpl) oldFeed.getEntries().get(o-c-g)).getTitle()) &&
 						// newFeedDescription == oldFeedDescription
 						(((SyndEntryImpl) newFeed.getEntries().get(n-c)).getDescription()==null?"null":((SyndEntryImpl) newFeed.getEntries().get(n-c)).getDescription().getValue()).equals(((SyndEntryImpl) oldFeed.getEntries().get(o-c-g)).getDescription()==null?"null":((SyndEntryImpl) oldFeed.getEntries().get(o-c-g)).getDescription().getValue()) ) {
 					c++;
 				}
 				if (o-c-g<0) {
 					g+=n-o;
 					break;
 				}
 				g++;
 			}
 		}
 		
 		// log the newest feed entries pushed onto the stack
 		for (int i=0; i<g; i++) {
 			log.info(String.format("Newest %s entries[%d]: title='%s'  description='%s'", newFeed.getTitle(), i, ((SyndEntryImpl) newFeed.getEntries().get(i)).getTitle(), ((SyndEntryImpl) newFeed.getEntries().get(i)).getDescription()==null?"null":((SyndEntryImpl) newFeed.getEntries().get(i)).getDescription().getValue()));
 		}
 
 		return g;
 	}
 
 	protected void assertTheNewOwnerFeedContains(String ownerKey, SyndFeed oldOwnerFeed, List<String> newEventTitles) throws JSONException, Exception {
 //		int oldOwnerFeed_EntriesSize = oldOwnerFeed==null? 0 : oldOwnerFeed.getEntries().size();
 
 		// assert the owner feed...
 		SyndFeed newOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey, sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		Assert.assertEquals(newOwnerFeed.getTitle(),"Event feed for owner "+CandlepinTasks.getOrgDisplayNameForOrgKey(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,ownerKey));
 		assertFeedContainsNoUnknownEvents(newOwnerFeed);
 		
 		log.info("Expecting the new feed for owner ("+ownerKey+") to have grown by events that contain (at a minimum) the following events: ");
 		for (String newEventTitle : newEventTitles) log.info("    "+newEventTitle);
 		
 //		int newOwnerFeedGrowthCount = newOwnerFeed.getEntries().size() - oldOwnerFeed_EntriesSize;
 		int newOwnerFeedGrowthCount = getFeedGrowthCount(newOwnerFeed,oldOwnerFeed);
 		log.info(newOwnerFeedGrowthCount+" new events for owner '"+ownerKey+"' have been pushed onto the atom feed stack.");
 		List<String> actualNewEventTitles = new ArrayList<String>();
 		for (int i=0; i<newOwnerFeedGrowthCount; i++) {
 			actualNewEventTitles.add(((SyndEntryImpl) newOwnerFeed.getEntries().get(i)).getTitle());
 		}
 		Assert.assertTrue(actualNewEventTitles.containsAll(newEventTitles), "The newest event feed entries for owner '"+ownerKey+"' contains (at a minimum) all of the expected new event titles.");
 	}
 	
 	protected void assertTheNewOwnerFeed(String ownerKey, SyndFeed oldOwnerFeed, List<String> newEventTitles) throws JSONException, Exception {
 //		int oldOwnerFeed_EntriesSize = oldOwnerFeed==null? 0 : oldOwnerFeed.getEntries().size();
 
 		// assert the owner feed...
 		SyndFeed newOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey, sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		Assert.assertEquals(newOwnerFeed.getTitle(),"Event feed for owner "+CandlepinTasks.getOrgDisplayNameForOrgKey(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,ownerKey));
 		assertFeedContainsNoUnknownEvents(newOwnerFeed);
 		
 		log.info("Expecting the new feed for owner ("+ownerKey+") to have grown by the following "+newEventTitles.size()+" events (in no particular order): ");
 		for (String newEventTitle : newEventTitles) log.info("    "+newEventTitle);
 		
 		//Assert.assertEquals(newOwnerFeed.getEntries().size(), oldOwnerFeed_EntriesSize+newEventTitles.size(), "The event feed length for owner '"+ownerKey+"' has increased by "+newEventTitles.size()+" entries.");
 //		if (oldOwnerFeed_EntriesSize+newEventTitles.size() <= feedLimit) {
 //			Assert.assertEquals(newOwnerFeed.getEntries().size(), oldOwnerFeed_EntriesSize+newEventTitles.size(), "The event feed length for owner '"+ownerKey+"' has increased by "+newEventTitles.size()+" entries.");
 //		} else {
 //			Assert.assertEquals(newOwnerFeed.getEntries().size(), feedLimit, "The event feed length for owner '"+ownerKey+"' has hit the max entry count as set by the Candlepin class AtomResource/ConsumerResource/OwnerResource hard-coded variable feedlimit.");			
 //		}
 		Assert.assertEquals(getFeedGrowthCount(newOwnerFeed,oldOwnerFeed), newEventTitles.size(), +newEventTitles.size()+" new event feed entries for owner '"+ownerKey+"' has been pushed onto the stack.");
 
 		List<String> newEventTitlesCloned = new ArrayList<String>(); for (String newEventTitle : newEventTitles) newEventTitlesCloned.add(newEventTitle);
 		for (int i=0; i<newEventTitles.size(); i++) {
 			String actualEventTitle = ((SyndEntryImpl) newOwnerFeed.getEntries().get(i)).getTitle();
 			Assert.assertTrue(newEventTitlesCloned.remove(actualEventTitle), "The next ("+i+") newest event feed entry ("+actualEventTitle+") for owner '"+ownerKey+"' is among the expected list of event titles.");
 		}
 	}
 	
 	protected void assertTheNewOwnerFeed(String ownerKey, SyndFeed oldOwnerFeed, String[] newEventTitles) throws JSONException, Exception {
 //		int oldOwnerFeed_EntriesSize = oldOwnerFeed==null? 0 : oldOwnerFeed.getEntries().size();
 
 		// assert the owner feed...
 		SyndFeed newOwnerFeed = CandlepinTasks.getSyndFeedForOwner(ownerKey, sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		Assert.assertEquals(newOwnerFeed.getTitle(),"Event feed for owner "+CandlepinTasks.getOrgDisplayNameForOrgKey(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,ownerKey));
 		assertFeedContainsNoUnknownEvents(newOwnerFeed);
 		
 		log.info("Expecting the new feed for owner ("+ownerKey+") to have grown by ("+newEventTitles.length+") events:");
 		int e=0;
 		for (String newEventTitle : newEventTitles) log.info(String.format("  Expecting entry[%d].title %s",e++,newEventTitle));
 		
 		//Assert.assertEquals(newOwnerFeed.getEntries().size(), oldOwnerFeed_EntriesSize+newEventTitles.length, "The event feed length for owner '"+ownerKey+"' has increased by "+newEventTitles.length+" entries.");
 //		if (oldOwnerFeed_EntriesSize+newEventTitles.length <= feedLimit) {
 //			Assert.assertEquals(newOwnerFeed.getEntries().size(), oldOwnerFeed_EntriesSize+newEventTitles.length, "The event feed length for owner '"+ownerKey+"' has increased by "+newEventTitles.length+" entries.");
 //		} else {
 //			Assert.assertEquals(newOwnerFeed.getEntries().size(), feedLimit, "The event feed length for owner '"+ownerKey+"' has hit the max entry count as set by the Candlepin class AtomResource/ConsumerResource/OwnerResource hard-coded variable feedlimit.");			
 //		}
 		Assert.assertEquals(getFeedGrowthCount(newOwnerFeed,oldOwnerFeed), newEventTitles.length, newEventTitles.length+" new event feed entries for owner '"+ownerKey+"' has been pushed onto the stack.");
 
 		int i=0;
 		for (String newEventTitle : newEventTitles) {
 			String actualEventTitle = ((SyndEntryImpl) newOwnerFeed.getEntries().get(i)).getTitle();
 			Assert.assertEquals(actualEventTitle,newEventTitle, "The next ("+i+") newest event feed entry for owner '"+ownerKey+"' is '"+newEventTitle+"'.");
 			i++;
 		}
 	}
 	
 	protected void assertTheNewConsumerFeed(String ownerKey, String consumerUuid, SyndFeed oldConsumerFeed, List<String> newEventTitles) throws IllegalArgumentException, IOException, FeedException {
 //		int oldConsumerFeed_EntriesSize = oldConsumerFeed==null? 0 : oldConsumerFeed.getEntries().size();
 
 		// assert the consumer feed...
 		SyndFeed newConsumerFeed = CandlepinTasks.getSyndFeedForConsumer(ownerKey, consumerUuid, sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		Assert.assertEquals(newConsumerFeed.getTitle(),"Event feed for consumer "+consumerUuid);
 		assertFeedContainsNoUnknownEvents(newConsumerFeed);
 		
 		log.info("Expecting the new feed for consumer ("+consumerUuid+") to have grown by the following "+newEventTitles.size()+" events (in no particular order): ");
 		for (String newEventTitle : newEventTitles) log.info("    "+newEventTitle);
 
 		//Assert.assertEquals(newConsumerFeed.getEntries().size(), oldConsumerFeed_EntriesSize+newEventTitles.size(), "The event feed for consumer "+consumerUuid+" has increased by "+newEventTitles.size()+" entries.");
 //		if (oldConsumerFeed_EntriesSize+newEventTitles.size() <= feedLimit) {
 //			Assert.assertEquals(newConsumerFeed.getEntries().size(), oldConsumerFeed_EntriesSize+newEventTitles.size(), "The event feed for consumer "+consumerUuid+" has increased by "+newEventTitles.size()+" entries.");
 //		} else {
 //			Assert.assertEquals(newConsumerFeed.getEntries().size(), feedLimit, "The event feed length for consumer '"+consumerUuid+"' has hit the max entry count as set by the Candlepin class AtomResource/ConsumerResource/OwnerResource hard-coded variable feedlimit.");			
 //		}
 		Assert.assertEquals(getFeedGrowthCount(newConsumerFeed,oldConsumerFeed), newEventTitles.size(), newEventTitles.size()+" new event feed entries for consumer '"+consumerUuid+"' has been pushed onto the stack.");
 
 		List<String> newEventTitlesCloned = new ArrayList<String>(); for (String newEventTitle : newEventTitles) newEventTitlesCloned.add(newEventTitle);
 		for (int i=0; i<newEventTitles.size(); i++) {
 			String actualEventTitle = ((SyndEntryImpl) newConsumerFeed.getEntries().get(i)).getTitle();
 			Assert.assertTrue(newEventTitlesCloned.remove(actualEventTitle), "The next ("+i+") newest event feed entry ("+actualEventTitle+") for consumer "+consumerUuid+" is among the expected list of event titles.");
 		}
 	}
 	
 	protected void assertTheNewConsumerFeed(String ownerKey, String consumerUuid, SyndFeed oldConsumerFeed, String[] newEventTitles) throws IllegalArgumentException, IOException, FeedException {
 //		int oldConsumerFeed_EntriesSize = oldConsumerFeed==null? 0 : oldConsumerFeed.getEntries().size();
 
 		// assert the consumer feed...
 		SyndFeed newConsumerFeed = CandlepinTasks.getSyndFeedForConsumer(ownerKey, consumerUuid, sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);
 		Assert.assertEquals(newConsumerFeed.getTitle(),"Event feed for consumer "+consumerUuid);
 		assertFeedContainsNoUnknownEvents(newConsumerFeed);
 		
 		log.info("Expecting the new feed for consumer ("+consumerUuid+") to have grown by ("+newEventTitles.length+") events:");
 		int e=0;
 		for (String newEventTitle : newEventTitles) log.info(String.format("  Expecting entry[%d].title %s",e++,newEventTitle));
 
 		//Assert.assertEquals(newConsumerFeed.getEntries().size(), oldConsumerFeed_EntriesSize+newEventTitles.length, "The event feed for consumer "+consumerUuid+" has increased by "+newEventTitles.length+" entries.");
 //		if (oldConsumerFeed_EntriesSize+newEventTitles.length <= feedLimit) {
 //			Assert.assertEquals(newConsumerFeed.getEntries().size(), oldConsumerFeed_EntriesSize+newEventTitles.length, "The event feed for consumer "+consumerUuid+" has increased by "+newEventTitles.length+" entries.");
 //		} else {
 //			Assert.assertEquals(newConsumerFeed.getEntries().size(), feedLimit, "The event feed length for consumer '"+consumerUuid+"' has hit the max entry count as set by the Candlepin class AtomResource/ConsumerResource/OwnerResource hard-coded variable feedlimit.");			
 //		}
 		Assert.assertEquals(getFeedGrowthCount(newConsumerFeed,oldConsumerFeed), newEventTitles.length, newEventTitles.length+" new event feed entries for consumer '"+consumerUuid+"' has been pushed onto the stack.");
 
 		int i=0;
 		for (String newEventTitle : newEventTitles) {
 			String actualEventTitle = ((SyndEntryImpl) newConsumerFeed.getEntries().get(i)).getTitle();
 			Assert.assertEquals(actualEventTitle,newEventTitle, "The next ("+i+") newest event feed entry for consumer "+consumerUuid+" is '"+newEventTitle+"'.");
 			i++;
 		}
 	}
 	
 	protected void assertTheNewFeedContains(SyndFeed oldFeed, List<String> newEventTitles) throws IllegalArgumentException, IOException, FeedException {
 //		int oldFeed_EntriesSize = oldFeed==null? 0 : oldFeed.getEntries().size();
 		
 		// assert the feed...
 		SyndFeed newFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);		
 		Assert.assertEquals(newFeed.getTitle(),"Event Feed");
 		assertFeedContainsNoUnknownEvents(newFeed);
 		
 		log.info("Expecting the new feed to have grown by events that contain (at a minimum) the following events: ");
 		for (String newEventTitle : newEventTitles) log.info("    "+newEventTitle);
 		
 //		int newFeedGrowthCount = newFeed.getEntries().size() - oldFeed_EntriesSize;
 		int newFeedGrowthCount = getFeedGrowthCount(newFeed,oldFeed);
 		log.info(newFeedGrowthCount+" new events have been pushed onto the atom feed stack.");
 		List<String> actualNewEventTitles = new ArrayList<String>();
 		for (int i=0; i<newFeedGrowthCount; i++) {
 			actualNewEventTitles.add(((SyndEntryImpl) newFeed.getEntries().get(i)).getTitle());
 		}
 		Assert.assertTrue(actualNewEventTitles.containsAll(newEventTitles), "The newest event feed entries contains (at a minimum) all of the expected new event titles.");
 	}
 	
 	protected void assertTheNewFeed(SyndFeed oldFeed, List<String> newEventTitles) throws IllegalArgumentException, IOException, FeedException {
 //		int oldFeed_EntriesSize = oldFeed==null? 0 : oldFeed.getEntries().size();
 		
 		// assert the feed...
 		SyndFeed newFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);		
 		Assert.assertEquals(newFeed.getTitle(),"Event Feed");
 		assertFeedContainsNoUnknownEvents(newFeed);
 
 		log.info("Expecting the new feed to have grown by the following "+newEventTitles.size()+" events (in no particular order): ");
 		for (String newEventTitle : newEventTitles) log.info("    "+newEventTitle);
 		
 		//Assert.assertEquals(newFeed.getEntries().size(), oldFeed_EntriesSize+newEventTitles.size(), "The event feed entries has increased by "+newEventTitles.size());
 //		if (oldFeed_EntriesSize+newEventTitles.size() <= feedLimit) {
 //			Assert.assertEquals(newFeed.getEntries().size(), oldFeed_EntriesSize+newEventTitles.size(), "The event feed entries has increased by "+newEventTitles.size());
 //		} else {
 //			Assert.assertEquals(newFeed.getEntries().size(), feedLimit, "The event feed has hit the max entry count as set by the Candlepin class AtomResource/ConsumerResource/OwnerResource hard-coded variable feedlimit.");			
 //		}
 		Assert.assertEquals(getFeedGrowthCount(newFeed,oldFeed), newEventTitles.size(), newEventTitles.size()+" new event feed entries has been pushed onto the stack.");
 
 		List<String> newEventTitlesCloned = new ArrayList<String>(); for (String newEventTitle : newEventTitles) newEventTitlesCloned.add(newEventTitle);
 		for (int i=0; i<newEventTitles.size(); i++) {
 			String actualEventTitle = ((SyndEntryImpl) newFeed.getEntries().get(i)).getTitle();
 			Assert.assertTrue(newEventTitlesCloned.remove(actualEventTitle), "The next ("+i+") newest event feed entry ("+actualEventTitle+") is among the expected list of event titles.");
 		}
 	}
 	
 	protected void assertTheNewFeed(SyndFeed oldFeed, String[] newEventTitles) throws IllegalArgumentException, IOException, FeedException {
 //		int oldFeed_EntriesSize = oldFeed==null? 0 : oldFeed.getEntries().size();
 		
 		// assert the feed...
 		SyndFeed newFeed = CandlepinTasks.getSyndFeed(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl);		
 		Assert.assertEquals(newFeed.getTitle(),"Event Feed");
 		assertFeedContainsNoUnknownEvents(newFeed);
 
 		log.info("Expecting the new feed to have grown by ("+newEventTitles.length+") events:");
 		int e=0;
 		for (String newEventTitle : newEventTitles) log.info(String.format("  Expecting entry[%d].title %s",e++,newEventTitle));
 
 		//Assert.assertEquals(newConsumerFeed.getEntries().size(), oldFeed_EntriesSize+newEventTitles.length, "The event feed entries has increased by "+newEventTitles.length);
 //		if (oldFeed_EntriesSize+newEventTitles.length <= feedLimit) {
 //			Assert.assertEquals(newFeed.getEntries().size(), oldFeed_EntriesSize+newEventTitles.length, "The event feed entries has increased by "+newEventTitles.length);
 //		} else {
 //			Assert.assertEquals(newFeed.getEntries().size(), feedLimit, "The event feed has hit the max entry count as set by the Candlepin class AtomResource/ConsumerResource/OwnerResource hard-coded variable feedlimit.");			
 //		}
 		Assert.assertEquals(getFeedGrowthCount(newFeed,oldFeed), newEventTitles.length, newEventTitles.length+" new event feed entries has been pushed onto the stack.");
 
 		int i=0;
 		for (String newEventTitle : newEventTitles) {
 			String actualEventTitle = ((SyndEntryImpl) newFeed.getEntries().get(i)).getTitle();
 			Assert.assertEquals(actualEventTitle,newEventTitle, "The next ("+i+") newest event feed entry is '"+newEventTitle+"'.");
 			i++;
 		}
 	}
 	
 	protected void assertFeedContainsNoUnknownEvents(SyndFeed feed) {
 	
 		// assert that there are no "Unknown event"s in the feed - reference https://bugzilla.redhat.com/show_bug.cgi?id=721141
 		for (int i=0;  i<feed.getEntries().size(); i++) {
 			String entryTitle = ((SyndEntryImpl) feed.getEntries().get(i)).getTitle();
 			String entryDescription = ((SyndEntryImpl) feed.getEntries().get(i)).getDescription()==null?"null":((SyndEntryImpl) feed.getEntries().get(i)).getDescription().getValue();
 			String entryAsString = String.format("%s entries[%d].title=%s   description=%s", feed.getTitle(), i, entryTitle, entryDescription);
 			
 			if (entryDescription.toLowerCase().contains("unknown event")) {
 				Assert.fail("Encountered an atom feed entry with an unknown event: "+entryAsString);
 			}
 			if (entryDescription.equalsIgnoreCase("null")) {
 				Assert.fail("Encountered an atom feed entry with a null description: "+entryAsString);			
 			}
 		}
 		Assert.assertTrue(true,"None of the entries in feed '"+feed.getTitle()+"' contain an unknown event description.");
 	}
 	
 	
 	protected String testOwnerKey = "newOwner"+System.currentTimeMillis();
 	protected JSONObject testJSONOwner;
 	protected String testProductId = "newProduct"+System.currentTimeMillis();
 	protected JSONObject testJSONProduct;
 	protected String testPoolId = "newPool"+System.currentTimeMillis();
 	protected JSONObject testJSONPool;
 	protected int feedLimit = 1000;
 	SubscriptionPool testPool = null;
 	
 	
 	// Data Providers ***********************************************************************
 
 }
