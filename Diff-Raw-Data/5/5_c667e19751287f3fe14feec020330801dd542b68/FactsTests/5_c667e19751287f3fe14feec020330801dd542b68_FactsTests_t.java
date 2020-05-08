 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BzChecker;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.cli.tasks.SubscriptionManagerTasks;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 
 
 /**
  * @author jsefler
  */
 @Test(groups={"FactsTests"})
 public class FactsTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 
 	/**
 	 * @author skallesh
 	 */
 	@Test(    description="subscription-manager: facts --update (when registered)",
 			            groups={"MyTestFacts","blockedByBug-707525"},
 			            enabled=true)
 	public void FactsUpdateWhenRegistered_Test() {
 			                       
 		 clienttasks.register(sm_clientUsername, sm_clientPassword,sm_clientOrg, null, null, null, null, null, null, null, (List<String>)null, null,null, false, null, null, null, null);
 		 SSHCommandResult result = clienttasks.facts(null, true,null, null, null);
 	     Assert.assertEquals(result.getStdout().trim(),"Successfully updated the system facts.");
 	}
 	
 	
 	@Test(	description="subscription-manager: facts --update (when not registered)",
 			groups={"blockedByBug-654429"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void FactsUpdateWhenNotRegistered_Test() {
 		
 		// make sure we are not registered
 		clienttasks.unregister(null, null, null);
 		
 		log.info("Assert that one must be registered to update the facts...");
 		for (Boolean list : new Boolean[]{true,false}) {			
 			SSHCommandResult result = clienttasks.facts_(list, true, null, null, null);
 			Assert.assertEquals(result.getStdout().trim(),clienttasks.msg_ConsumerNotRegistered,
 				"One must be registered to update the facts.");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: facts --list (when not registered)",
 			groups={"blockedByBug-654429","blockedByBug-661329","blockedByBug-666544"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void FactsListWhenNotRegistered_Test() {
 		
 		// make sure we are not registered
 		clienttasks.unregister(null, null, null);
 		
 		log.info("Assert that one need not be registered to list the facts...");		
 		SSHCommandResult result = clienttasks.facts(true, false, null, null, null);
 		Assert.assertContainsNoMatch(result.getStderr(),clienttasks.msg_ConsumerNotRegistered,
 				"One need not be registered to list the facts.");
 		Assert.assertContainsNoMatch(result.getStdout(),clienttasks.msg_ConsumerNotRegistered,
 				"One need not be registered to list the facts.");
 	}
 	
 	
 	@Test(	description="subscription-manager: facts (without --list or --update)",
 			groups={"blockedByBug-654429"},
 			enabled=false)	// was enabled before Bug 811594 - [RFE] facts command should default to list; replaced by FactsDefaultsToFactsList_Test()
 	@Deprecated
 	//@ImplementsNitrateTest(caseId=)
 	public void FactsWithoutListOrUpdate_Test_DEPRECATED() {
 		
 		log.info("Assert that one need one must specify --list or --update...");		
 		SSHCommandResult result = clienttasks.facts_(false, false, null, null, null);
 		Assert.assertEquals(result.getExitCode(), Integer.valueOf(255),
 				"exitCode from the facts without --list or --update");
 		Assert.assertEquals(result.getStdout().trim(),clienttasks.msg_NeedListOrUpdateOption,
 				"stdout from facts without --list or --update");
 	}
 	@Test(	description="subscription-manager: facts (without --list or --update) should default to --list",
 			groups={"blockedByBug-811594"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void FactsDefaultsToFactsList_Test() {
 		
 		SSHCommandResult listResult = clienttasks.facts(true, null, null, null, null);
 		SSHCommandResult defaultResult = clienttasks.facts(null, null, null, null, null);
 		
 		log.info("Asserting that that the default facts result without specifying any options is the same as the result from facts --list...");
 		Assert.assertEquals(defaultResult.getExitCode(), listResult.getExitCode(),
 				"exitCode from facts without options should match exitCode from the facts --list");
 		Assert.assertEquals(defaultResult.getStderr(), listResult.getStderr(),
 				"stderr from facts without options should match stderr from the facts --list");
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId = "838123"; boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			String fact = "net.interface.sit0.mac_address";
 			String factRegex = "net\\.interface\\.sit0\\.mac_address: [A-F\\d:]+\\n";
 			log.warning("Fact '"+fact+"' will be extracted and disregarded during the following facts list comparison since its value is not constant.");
 			Assert.assertEquals(defaultResult.getStdout().replaceFirst(factRegex, ""), listResult.getStdout().replaceFirst(factRegex, ""),
 					"stdout from facts without options should match stdout from the facts --list");
 		} else
 		// END OF WORKAROUND
 		Assert.assertEquals(defaultResult.getStdout(), listResult.getStdout(),
 				"stdout from facts without options should match stdout from the facts --list");
 	}
 	
 	
 	@Test(	description="subscription-manager: facts and rules: consumer facts list",
 			groups={"AcceptanceTests"}, dependsOnGroups={},
 			dataProvider="getClientsData",
 			enabled=true)
 	@ImplementsNitrateTest(caseId=56386)
 	public void ConsumerFactsList_Test(SubscriptionManagerTasks smt) {
 		
 		// start with fresh registrations using the same clientusername user
 		smt.unregister(null, null, null);
 		smt.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 		
 		// list the system facts
 		smt.facts(true, false, null, null, null);
 	}
 	
 	
 	@Test(	description="subscription-manager: facts and rules: fact check RHEL distribution",
 			groups={"blockedByBug-666540"}, dependsOnGroups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=56329)
 	public void FactCheckRhelDistribution_Test() {
 		
 		// skip if client1 and client2 are not a Server and Workstation distributions
 		SSHCommandRunner workClient = null,servClient = null;
 		SubscriptionManagerTasks workClientTasks = null, servClientTasks = null;
 		if (client1!=null && client1tasks.getRedhatRelease().startsWith("Red Hat Enterprise Linux Workstation")) {
 			workClient = client1; workClientTasks = client1tasks;
 		}
 		if (client2!=null && client2tasks.getRedhatRelease().startsWith("Red Hat Enterprise Linux Workstation")) {
 			workClient = client2; workClientTasks = client2tasks;
 		}
 		if (client1!=null && client1tasks.getRedhatRelease().startsWith("Red Hat Enterprise Linux Server")) {
 			servClient = client1; servClientTasks = client1tasks;
 		}
 		if (client2!=null && client2tasks.getRedhatRelease().startsWith("Red Hat Enterprise Linux Server")) {
 			servClient = client2; servClientTasks = client2tasks;
 		}
 		if (workClient==null || servClient==null) {
 			throw new SkipException("This test requires a RHEL Workstation client and a RHEL Server client.");
 		}
 		
 		// start with fresh registrations using the same clientusername user
 		workClientTasks.unregister(null, null, null);
 		servClientTasks.unregister(null, null, null);
 		workClientTasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 		servClientTasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 		
 
 		// get all the pools available to each client
 		List<SubscriptionPool> workClientPools = workClientTasks.getCurrentlyAvailableSubscriptionPools();
 		List<SubscriptionPool> servClientPools = servClientTasks.getCurrentlyAvailableSubscriptionPools();
 		
 		log.info("Verifying that the pools available to the Workstation consumer are not identitcal to those available to the Server consumer...");
 		if (!(!workClientPools.containsAll(servClientPools) || !servClientPools.containsAll(workClientPools))) {
 			// TODO This testcase needs more work.  Running on different variants of RHEL alone is not enough to assert that the available pools are different.  In fact, then should be the same if the subscriptions are all set with a variant attribute of ALL
 			throw new SkipException("The info message above is not accurate... The assertion that the pools available to a Workstation consumer versus a Server consumer is applicable ONLY when the org's subscriptions includes a variant aware subscription.  In fact, if the org's subscriptions are all set with a variant attribute of ALL, then the available pools should be identical.  This automated test needs some work.");
 		}
 
 		Assert.assertTrue(!workClientPools.containsAll(servClientPools) || !servClientPools.containsAll(workClientPools),
 				"Because the facts of a system client running RHEL Workstation versus RHEL Server should be different, the available subscription pools to these two systems should not be the same.");
 
 		// FIXME TODO Verify with development that these are valid asserts
 		//log.info("Verifying that the pools available to the Workstation consumer do not contain Server in the ProductName...");
 		//log.info("Verifying that the pools available to the Server consumer do not contain Workstation in the ProductName...");
 
 	}
 	
 	@Test(	description="subscription-manager: facts and rules: check sockets",
 			groups={}, dependsOnGroups={},
 			dataProvider="getClientsData",
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void AssertPoolsWithSocketsGreaterThanSystemsCpuSocketAreNotAvailable_Test(SubscriptionManagerTasks smt) throws Exception {
 		smt.unregister(null, null, null);
 		String consumerId = smt.getCurrentConsumerId(smt.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, null, null, null, (String)null, null, null, null, false, null, null, null));
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 		
 		boolean foundPoolWithSocketAttributes = false;
 		boolean conclusiveTest = false;
 		
 		// get all the pools available to each client
 		List<SubscriptionPool> clientPools = smt.getCurrentlyAvailableSubscriptionPools();
 		
 		// get the number of cpu_sockets for this system consumer
 		String factName = "cpu.cpu_socket(s)";
 //TODO need a workaround for bug 696791 when getFactValue(factName)==null when getFactValue("uname.machine").equals("s390x"); should probably treat this as though socket is 1   see http://hudson.rhq.lab.eng.bos.redhat.com:8080/hudson/job/rhsm-beaker-on-premises-RHEL5/179/TestNG_Report/
 		int systemValue = Integer.valueOf(smt.getFactValue(factName));
 		log.info(factName+" for this system consumer: "+systemValue);
 		
 		// loop through the owner's subscriptions
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			String poolId = jsonSubscription.getString("id");
 			JSONObject jsonProduct = (JSONObject) jsonSubscription.getJSONObject("product");
 			String subscriptionName = jsonProduct.getString("name");
 			String productId = jsonProduct.getString("id");
 			JSONArray jsonAttributes = jsonProduct.getJSONArray("attributes");
 			// loop through the attributes of this subscription looking for the "sockets" attribute
 			for (int j = 0; j < jsonAttributes.length(); j++) {
 				JSONObject jsonAttribute = (JSONObject) jsonAttributes.get(j);
 				String attributeName = jsonAttribute.getString("name");
 				if (attributeName.equals("sockets")) {
 					// found the sockets attribute
 					foundPoolWithSocketAttributes = true;
 					SubscriptionPool pool = new SubscriptionPool(productId,poolId);
 					
 					// get the value of the sockets attribute
 					// test if the sockets attribute value is not numeric (e.g. null)
 					if (jsonAttribute.isNull("value")) {
 						// do not mark productAttributesPassRulesCheck = false;
 						log.info("Since this sockets value is null, Subscription Pool "+pool+" may or may not be available depending on other facts besides "+factName+" (e.g. arch).");
 						break;						
 					}
 					// test if the sockets attribute value is not numeric (e.g. "zero")
 					try {Integer.valueOf(jsonAttribute.getString("value"));}
 					catch (NumberFormatException e) {
 						// do not mark productAttributesPassRulesCheck = false;
 						log.info("Since this sockets value '"+jsonAttribute.getString("value")+"' is a non-integer, Subscription Pool "+pool+" may or may not be available depending on other facts besides "+factName+" (e.g. arch).");
 						break;
 					}
 					int poolValue = jsonAttribute.getInt("value");
 					
 					// assert that if the maximum cpu_sockets for this subscription pool is greater than the cpu_sockets facts for this consumer, then this product should NOT be available
 					log.fine("Maximum sockets for this subscriptionPool name="+subscriptionName+": "+poolValue);
 					if (poolValue < systemValue) {
 						Assert.assertFalse(clientPools.contains(pool), "Subscription Pool "+pool+" IS NOT available since this system's "+factName+" ("+systemValue+") exceeds the maximum ("+poolValue+") for this pool to be a candidate for availability.");
 						conclusiveTest = true;
 					} else {
 						log.info("Subscription Pool "+pool+" may or may not be available depending on other facts besides "+factName+" (e.g. arch).");
 					}
 					break;
 				}
 			}
 		}
 		if (jsonSubscriptions.length()==0) {
 			log.warning("No owner subscriptions were found for a system registered by '"+sm_clientUsername+"' and therefore we could not attempt this test.");
 			throw new SkipException("No owner subscriptions were found for a system registered by '"+sm_clientUsername+"' and therefore we could not attempt this test.");		
 		}
 		if (!conclusiveTest) {
 			//log.warning("The facts for this system did not allow us to perform a conclusive test.");
 			throw new SkipException("The facts for this system did not allow us to perform a conclusive test.");
 		}
 		Assert.assertTrue(foundPoolWithSocketAttributes,"At least one Subscription Pool was found for which we could attempt this test.");
 	}
 	
 	@Test(	description="subscription-manager: facts and rules: check arch",
 			groups={}, dependsOnGroups={},
 			dataProvider="getClientsData",
 			enabled=true)
 	//@ImplementsTCMS(id="")
 	public void AssertPoolsWithAnArchDifferentThanSystemsArchitectureAreNotAvailable_Test(SubscriptionManagerTasks smt) throws Exception {
 		smt.unregister(null, null, null);
 		String consumerId = smt.getCurrentConsumerId(smt.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, null, null, null, (String)null, null, null, null, false, null, null, null));
 		String ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 
 		boolean foundPoolWithArchAttributes = false;
 		boolean conclusiveTest = false;
 		
 		// get all the pools available to this client
 		List<SubscriptionPool> clientPools = smt.getCurrentlyAvailableSubscriptionPools();
 		
 		// get the number of cpu_sockets for this system consumer
 		String factName = "cpu.architecture";
 		String systemValue = smt.getFactValue(factName);
 		log.info(factName+" for this system consumer: "+systemValue);
 		
 		// loop through the owner's subscriptions
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			String poolId = jsonSubscription.getString("id");
 			JSONObject jsonProduct = (JSONObject) jsonSubscription.getJSONObject("product");
 			String subscriptionName = jsonProduct.getString("name");
 			String productId = jsonProduct.getString("id");
 			JSONArray jsonAttributes = jsonProduct.getJSONArray("attributes");
 			// loop through the attributes of this subscription looking for the "sockets" attribute
 			for (int j = 0; j < jsonAttributes.length(); j++) {
 				JSONObject jsonAttribute = (JSONObject) jsonAttributes.get(j);
 				String attributeName = jsonAttribute.getString("name");
 				if (attributeName.equals("arch")) {
 					// found the arch attribute - get its value
 					foundPoolWithArchAttributes = true;
 					String poolValue = jsonAttribute.getString("value");
 					
 					// assert that if the maximum cpu_sockets for this subscription pool is greater than the cpu_sockets facts for this consumer, then this product should NOT be available
 					log.fine("Arch for this subscriptionPool name="+subscriptionName+": "+poolValue);
 					SubscriptionPool pool = new SubscriptionPool(productId,poolId);
 					if (!poolValue.equalsIgnoreCase(systemValue) && !poolValue.equalsIgnoreCase("ALL")) {
 						Assert.assertFalse(clientPools.contains(pool), "Subscription Pool "+pool+" IS NOT available since this system's "+factName+" ("+systemValue+") does not match ("+poolValue+") for this pool to be a candidate for availability.");
 						conclusiveTest = true;
 					} else {
 						log.info("Subscription Pool "+pool+" may or may not be available depending on other facts besides "+factName+".");
 					}
 					break;
 				}
 			}
 		}
 		if (jsonSubscriptions.length()==0) {
 			log.warning("No owner subscriptions were found for a system registered by '"+sm_clientUsername+"' and therefore we could not attempt this test.");
 			throw new SkipException("No owner subscriptions were found for a system registered by '"+sm_clientUsername+"' and therefore we could not attempt this test.");		
 		}
 		if (!conclusiveTest) {
 			log.warning("The facts for this system did not allow us to perform a conclusive test.");
 			throw new SkipException("The facts for this system did not allow us to perform a conclusive test.");
 		}
 		Assert.assertTrue(foundPoolWithArchAttributes,"At least one Subscription Pools was found for which we could attempt this test.");
 	}
 	
 	@Test(	description="subscription-manager: facts and rules: bypass rules due to type",
 			groups={"blockedByBug-641027"}, dependsOnGroups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=56331)
 	public void BypassRulesDueToType_Test() throws Exception {
 		// determine which client is a RHEL Workstation
 		SSHCommandRunner client = null;
 		SubscriptionManagerTasks clienttasks = null;
 		if (client1!=null && client1tasks.getRedhatRelease().startsWith("Red Hat Enterprise Linux Workstation")) {
 			client = client1; clienttasks = client1tasks;
 		} else if (client2!=null && client2tasks.getRedhatRelease().startsWith("Red Hat Enterprise Linux Workstation")) {
 			client = client2; clienttasks = client2tasks;
 		} else {
 			throw new SkipException("This test requires a Red Hat Enterprise Linux Workstation.");
 		}
 
 		// on a RHEL workstation register to candlepin (as type system)
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, ConsumerType.system, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 
 		// get a list of available pools and all available pools (for this system consumer)
 		List<SubscriptionPool> compatiblePoolsAsSystemConsumer = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		List<SubscriptionPool> allPoolsAsSystemConsumer = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		
 		Assert.assertFalse(compatiblePoolsAsSystemConsumer.containsAll(allPoolsAsSystemConsumer),
 				"Without bypassing the rules, not *all* pools are available for subscribing by a type=system consumer.");
 		Assert.assertTrue(allPoolsAsSystemConsumer.containsAll(compatiblePoolsAsSystemConsumer),
 				"The pools available to a type=system consumer is a subset of --all --available pools.");
 		
 		// now register to candlepin (as type candlepin)
 		clienttasks.unregister(null, null, null);
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, ConsumerType.candlepin, null, null, null, null, null, (String)null, null, null, null, false, null, null, null);
 
 		// get a list of available pools and all available pools (for this candlepin consumer)
 		List<SubscriptionPool> compatiblePoolsAsCandlepinConsumer = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		List<SubscriptionPool> allPoolsAsCandlepinConsumer = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 
 		Assert.assertTrue(compatiblePoolsAsCandlepinConsumer.containsAll(allPoolsAsCandlepinConsumer) && allPoolsAsCandlepinConsumer.containsAll(compatiblePoolsAsCandlepinConsumer),
 				"The pools available to a type=candlepin consumer bypass the rules (list --all --available is identical to list --available).");
 	
 		// now assert that all the pools can be subscribed to by the consumer (registered as type candlepin)
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 	}
 	
 	
 	@Test(	description="subscription-manager: assert that the cpu_socket(s) fact matches the value from lscpu",
 			groups={"AcceptanceTest","blockedByBug-707292"/*,"blockedByBug-751205"*/}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void MatchingCPUSocketsFact_Test() {
 		clienttasks.deleteFactsFileWithOverridingValues();
 		
 		// get the value of cpu_sockets as determined by subscription-manager facts
 		String cpu_sockets = clienttasks.getFactValue("cpu.cpu_socket(s)");
 		
 		if (clienttasks.redhatRelease.contains("release 5")) {
 			//String sockets = clienttasks.sockets;
 			String sockets = client.runCommandAndWait("for cpu in `ls -1 /sys/devices/system/cpu/ | egrep cpu[[:digit:]]`; do echo \"cpu `cat /sys/devices/system/cpu/$cpu/topology/physical_package_id`\"; done | grep cpu | uniq | wc -l").getStdout().trim();
 			Assert.assertEquals(cpu_sockets, sockets, "The fact 'cpu_socket(s)' value='"+cpu_sockets+"' should match the 'CPU socket(s)' value='"+sockets+"' as calculated above.");
 			return;
 		}
 		else /*if (clienttasks.redhatRelease.contains("release 6"))*/ {
 			client.runCommandAndWait("lscpu");
 			//String sockets = clienttasks.sockets;
 			String sockets = client.runCommandAndWait("lscpu | grep 'CPU socket'").getStdout().split(":")[1].trim();
 			Assert.assertEquals(cpu_sockets, sockets, "The fact 'cpu_socket(s)' value='"+cpu_sockets+"' should match the 'CPU socket(s)' value='"+sockets+"' reported by lscpu.");
 			return;
 		}
 	}
 
 	
 	@Test(	description="when registering to an existing consumerid, the facts for the system should be updated automatically upon registering",
 			groups={"blockedByBug-810236"}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AssertFactsUpdateWhenRegisteringWithConsumerId_Test() throws JSONException, Exception {
 		if (client1==null || client2==null) throw new SkipException("This test requires two clients.");
 
 		// give client1 a custom fact
 		client1tasks.deleteFactsFileWithOverridingValues();
 		Map<String,String> customFactsMap = new HashMap<String,String>();
 		String client1CustomFactName = "custom.fact.client1";
 		customFactsMap.clear();
 		customFactsMap.put(client1CustomFactName,client1tasks.hostname);
 		client1tasks.createFactsFileWithOverridingValues(customFactsMap);
 		
 		// register client1 and get the original facts for consumerid from client1
 		String consumerId = client1tasks.getCurrentConsumerId(client1tasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, Boolean.TRUE, null, null, null, null));
 		Map<String,String> client1FactsMap = client1tasks.getFacts();
 		
 		// get consumerid's facts from Candlepin
 		Map<String,String> consumer1FactsMap = CandlepinTasks.getConsumerFacts(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 		JSONObject jsonConsumer = new JSONObject (CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerId));
 		log.info("Consumer '"+consumerId+"' facts on the candlepin server are: \n"+jsonConsumer.getJSONObject("facts").toString(5));
 		
 		// assert that the candlepin's view of consumerid's facts are identical to the local client1's system facts
 		Assert.assertTrue(doSystemFactsMatchConsumerFacts(consumerId, client1FactsMap, consumer1FactsMap),"The facts on consumer '"+consumerId+"' known to the candlepin server are equivalent to the subscription-manager facts --list on client system '"+client1tasks.hostname+"'.");
 		
 		client1tasks.clean(null,null,null);
 		
 		
 		// give client2 a custom fact
 		client2tasks.deleteFactsFileWithOverridingValues();
 		String client2CustomFactName = "custom.fact.client2";
 		customFactsMap.clear();
 		customFactsMap.put(client2CustomFactName,client2tasks.hostname);
 		client2tasks.createFactsFileWithOverridingValues(customFactsMap);
 		
 		// register client2 to the existing consumerid and get the facts from client2
 		Assert.assertEquals(client2tasks.getCurrentConsumerId(client2tasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, consumerId, null, null, null, (String)null, null, null, null, null, null, null, null)), consumerId, "Registering to an existing consumerId should return the same consumerId.");
 		Map<String,String> client2FactsMap = client2tasks.getFacts();
 
 		// get consumerid's facts from Candlepin again
 		Map<String,String> consumer2FactsMap = CandlepinTasks.getConsumerFacts(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 		jsonConsumer = new JSONObject (CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerId));
 		log.info("Consumer '"+consumerId+"' facts on the candlepin server are now: \n"+jsonConsumer.getJSONObject("facts").toString(5));
 
 		// now assert that candlepin's view of the consumerid facts has been automatically updated to those from client2 system facts who just registered to existing consumer
 		Assert.assertTrue(doSystemFactsMatchConsumerFacts(consumerId, client2FactsMap, consumer2FactsMap),"The facts on consumer '"+consumerId+"' known to the candlepin server have automatically been updated after client system '"+client2tasks.hostname+"' registered using an existing consumerId.");
 		Assert.assertTrue(!consumer2FactsMap.containsKey(client1CustomFactName),"After client2 "+client2tasks.hostname+" registered to existing consumerId '"+consumerId+"', the original custom fact '"+client1CustomFactName+"' set by original client1 system '"+client1tasks.hostname+"' is has been automatically cleaned from the consumer facts known on the candlepin server.");
 	}
 	
 	
 	@Test(	description="the facts for net.interface.sit0.mac_address and net.interface.lo.mac_address should not be listed",
 			groups={"blockedByBug-838123"}, dependsOnGroups={},
			enabled=true)	// TODO re-implement this test after fix for Bug 866645
 	//@ImplementsNitrateTest(caseId=)
 	public void AssertFactsForNetInterfaceMacAddress_Test() {
 		
 		Map<String,String> clientFactsMap = clienttasks.getFacts();
 		for (String macAddressFact : new String[]{"net.interface.sit0.mac_address","net.interface.lo.mac_address"}) {
 			Assert.assertNull(clientFactsMap.get(macAddressFact), "After fix for bug 838123, the '"+macAddressFact+"' fact should not exist.");
 		}
 	}
 
 
 	
 	// Candidates for an automated Test:
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=669513
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=664847#2
 	// TODO https://bugzilla.redhat.com/show_bug.cgi?id=629670
 	// TODO Bug 706552 - Wrong DMI structures length: 3263 bytes announced, structures occupy 3265 bytes.
 	// TODO Bug 707525 - Facts update command displays consumed uuid
 	// TODO Bug 722239 - subscription-manager cli does not show all facts
 	// TODO Bug 700821 - Update of consumer facts does not update the update timestamp
	// TODO Bug 866645 - [RFE] Omit interfaces from facts based on their type. 
	
 	
 	// TODO create tests that overrides the facts, for example....  and the uses getSystemSubscriptionPoolProductDataAsListOfLists()
 	// see TODO MOVE THIS BLOCK OF TESTING INTO ITS OWN "RULES CHECK TEST" from SubscribeTests
 	//String factsFile = clienttasks.factsDir+"/subscriptionTests.facts";
 	//client.runCommandAndWait("echo '{\"cpu.cpu_socket(s)\": \"4\"}' > "+factsFile);	// create an override for facts  // use clienttasks.createFactsFileWithOverridingValues(...)
 	//clienttasks.facts(true,true, null, null, null);
 
 	// TODO Activation Notes
 	// To enable activation on the QA env, the machine factsdmi.system.manufacturer must contain string "DELL" and the fact dmi.system.serial_number must be known on the RedHat IT backend:  put them in this overrider file: /etc/rhsm/facts
 	//	<aedwards> dmi.system.manufacturer
 	//	<aedwards> dmi.system.serial_number
 	//	<aedwards> candlepin.subscription.activation.debug_prefix
 	// To enable activation on the onpremises env set the config value in /etc/candlepin/candlepin.conf candlepin.subscription.activation.debug_prefix to a value like "activator" and then when you register use the --consumername=activator<BLAH> to see the "Activate a Subscription" button
 	// [root@jsefler-onprem-server facts]# cat /etc/rhsm/facts/activator.facts 
 	// {"dmi.system.manufacturer": "MyDELLManfacturer","dmi.system.serial_number":"CNZFGH6"}
 	// https://engineering.redhat.com/trac/Entitlement/wiki/DellActivation
 	// TODO Bug 701458 - "We are currently processing your subscription activation, please check back later." should not render as an "Error activating subscription:"
 	// TODO activate test:
 	// against QA or Stage env...
 	// [root@jsefler-onprem-workstation facts]# subscription-manager activate --email=jsefler@redhat.com
 	// A subscription was not found for the given Dell service tag: CNZFGH6
 	// [root@jsefler-onprem-workstation facts]# subscription-manager unregister
 	// against QA 
 	//	[root@jsefler-onprem-workstation facts]# subscription-manager activate --email=jsefler@redhat.com
 	//	Your subscription activation is being processed and should be available soon. You will be notified via email once it is available. If you have any questions, additional information can be found here: https://access.redhat.com/kb/docs/DOC-53864.
 	//	[root@jsefler-onprem-workstation facts]# subscription-manager activate --email=jsefler@redhat.com
 	//	The Dell service tag: CNZFGH1, has already been used to activate a subscription
 	//	[root@jsefler-onprem-workstation facts]# 
 
 	// TODO Bug 746241 - UEPConnection.updateConsumer will not allow passing [] for facts, installed_products, or guest_uuids
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 
 	
 	
 	
 	// Protected Methods ***********************************************************************
 
 	protected boolean doSystemFactsMatchConsumerFacts(String consumerId, Map<String,String> systemFactsMap, Map<String,String> consumerFactsMap) {
 		boolean mapsAreEqual=true;
 		for (Map<String,String> m : Arrays.asList(systemFactsMap,consumerFactsMap)) {	// normalize boolean facts
 			for (String k : m.keySet()) {
 				if (m.get(k).equalsIgnoreCase(Boolean.TRUE.toString())) m.put(k,Boolean.TRUE.toString());
 				if (m.get(k).equalsIgnoreCase(Boolean.FALSE.toString())) m.put(k,Boolean.FALSE.toString());
 			}
 		}
 		for (String key : consumerFactsMap.keySet()) {
 			if (key.equals("system.name") || key.equals("system.uuid")) {log.info("Skipping comparison of extended fact '"+key+"'.");continue;}
 			if (systemFactsMap.containsKey(key) && !systemFactsMap.get(key).equals(consumerFactsMap.get(key))) {
 				log.warning("Consumer '"+consumerId+"' on client "+client1tasks.hostname+" has a local system fact '"+key+"' value '"+systemFactsMap.get(key)+"' which does not match value '"+consumerFactsMap.get(key)+"' from the remote candlepin API.");
 				if (key.equals("net.interface.sit0.mac_address")) {log.warning("Skipping comparison of fact '"+key+"'.  The local system value appears to change unpredictably.  The current value on the system '"+systemFactsMap.get(key)+"' may be acceptably different than the value on the consumer '"+consumerFactsMap.get(key)+"';  see Bugzilla https://bugzilla.redhat.com/show_bug.cgi?id=838123");continue;}
 				if (systemFactsMap.get(key).equals("Unknown") && consumerFactsMap.get(key).trim().equals("")) {log.info("Ignoring mismatch for fact '"+key+"'; see Bugzilla https://bugzilla.redhat.com/show_bug.cgi?id=722248");continue;}
 				mapsAreEqual=false;
 			} else if (!systemFactsMap.containsKey(key)) {
 				log.warning("Consumer '"+consumerId+"' from the remote candlepin API has a fact '"+key+"' which is absent from the local system facts on client "+client1tasks.hostname+".");
 				mapsAreEqual=false;	
 			}
 		}
 		for (String key : systemFactsMap.keySet()) {
 			if (key.equals("system.name") || key.equals("system.uuid")) {log.info("Skipping comparison of extended fact '"+key+"'.");continue;}
 			if (consumerFactsMap.containsKey(key) && !consumerFactsMap.get(key).equals(systemFactsMap.get(key))) {
 				log.warning("Consumer '"+consumerId+"' from the remote candlepin API has a system fact '"+key+"' value '"+consumerFactsMap.get(key)+"' which does not match value '"+systemFactsMap.get(key)+"' from the local system fact on client "+client1tasks.hostname+".");
 				if (key.equals("net.interface.sit0.mac_address")) {log.warning("Skipping comparison of fact '"+key+"'.  The local system value appears to change unpredictably.  The current value on the system '"+systemFactsMap.get(key)+"' may be acceptably different than the value on the consumer '"+consumerFactsMap.get(key)+"';  see Bugzilla https://bugzilla.redhat.com/show_bug.cgi?id=838123");continue;}
 				if (systemFactsMap.get(key).equals("Unknown") && consumerFactsMap.get(key).trim().equals("")) {log.info("Ignoring mismatch for fact '"+key+"'; see Bugzilla https://bugzilla.redhat.com/show_bug.cgi?id=722248");continue;}
 				mapsAreEqual=false;
 			} else if (!consumerFactsMap.containsKey(key)) {
 				log.warning("Consumer '"+consumerId+"' on client "+client1tasks.hostname+" has a fact '"+key+"' which is absent from the remote candlepin API.");
 				mapsAreEqual=false;	
 			}
 		}
 		return mapsAreEqual;
 	}
 
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getClientsData")
 	public Object[][] getClientsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getClientsDataAsListOfLists());
 	}
 	protected List<List<Object>> getClientsDataAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 
 		// SSHCommandRunner client
 		if (client1!= null)	ll.add(Arrays.asList(new Object[]{client1tasks}));
 		if (client2!= null)	ll.add(Arrays.asList(new Object[]{client2tasks}));
 
 		return ll;
 	}
 	
 
 }
