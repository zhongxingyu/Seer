 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.testng.SkipException;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsTCMS;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.SubscriptionManagerTasks;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 @Test(groups={"subscribe"})
 public class SubscribeTests extends SubscriptionManagerCLITestScript{
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using product ID",
 			enabled=false,	// Subscribing to a Subscription Pool using --product Id has been removed in subscription-manager-0.71-1.el6.i686.
 //			dependsOnGroups={"sm_stage3"},
 //			groups={"sm_stage4", "blockedByBug-584137"},
 			groups={"blockedByBug-584137"},
 			dataProvider="getAvailableSubscriptionPoolsData")
 	@ImplementsTCMS(id="41680")
 	public void SubscribeToValidSubscriptionsByProductID_Test(SubscriptionPool pool){
 //		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using product ID",
 //			dependsOnGroups={"sm_stage3"},
 //			groups={"sm_stage4", "blockedByBug-584137", "not_implemented"},
 			groups={"blockedByBug-584137"},
 			enabled=false)
 	public void SubscribeToASingleEntitlementByProductID_Test(){
 		clienttasks.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		SubscriptionPool MCT0696 = new SubscriptionPool("MCT0696", "696");
 		MCT0696.addProductID("Red Hat Directory Server");
 		clienttasks.subscribeToSubscriptionPoolUsingProductId(MCT0696);
 		//this.refreshSubscriptions();
 		for (ProductSubscription pid:MCT0696.associatedProductIDs){
 			Assert.assertTrue(clienttasks.getCurrentlyConsumedProductSubscriptions().contains(pid),
 					"ProductID '"+pid.productName+"' consumed from Pool '"+MCT0696.subscriptionName+"'");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using pool ID",
 //			dependsOnGroups={"sm_stage3"},
 //			groups={"sm_stage4", "blockedByBug-584137"},
 			groups={"blockedByBug-584137"},
 			dataProvider="getAvailableSubscriptionPoolsData")
 	@ImplementsTCMS(id="41686")
 	public void SubscribeToValidSubscriptionsByPoolID_Test(SubscriptionPool pool){
 // non-dataProvided test procedure
 //		sm.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to each available subscription pool using pool ID",
 			groups={"blockedByBug-584137"},
 			dataProvider="getGoodRegistrationData")
 	@ImplementsTCMS(id="41686")
 	public void SubscribeConsumerToEachAvailableSubscriptionPoolUsingPoolId_Test(String username, String password){
 		clienttasks.unregister();
 		clienttasks.register(username, password, ConsumerType.system, null, Boolean.FALSE, Boolean.FALSE);
 		clienttasks.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: subscribe consumer to an entitlement using registration token",
 //			dependsOnGroups={"sm_stage8"},
 //			groups={"sm_stage9", "blockedByBug-584137", "not_implemented"},
 			groups={"blockedByBug-584137"},
 			enabled=false)
 	@ImplementsTCMS(id="41681")
 	public void SubscribeToRegToken_Test(){
 		clienttasks.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.subscribeToRegToken(regtoken);
 	}
 	
 	
 	@Test(	description="Subscribed for Already subscribed Entitlement.",
 //			dependsOnGroups={"sm_stage3"},
 //			groups={"sm_stage4", "blockedByBug-584137", "not_implemented"},
 			groups={"blockedByBug-584137"},
 			enabled=false)
 	@ImplementsTCMS(id="41897")
 	public void SubscribeAndSubscribeAgain_Test(){
 		//sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		for(SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);
 			clienttasks.subscribeToProduct(pool.subscriptionName);
 		}
 	}
 	
 
 // FIXME: THIS ORIGINAL TEST WAS NOT COMPLETE.  REPLACEMENT BELOW IS WORK IN PROGRESS
 //	@Test(description="subscription-manager Yum plugin: enable/disable",
 //			dependsOnGroups={"sm_stage5"},
 //			groups={"sm_stage6"})
 //	@ImplementsTCMS(id="41696")
 //	public void EnableYumRepoAndVerifyContentAvailable_Test(){
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 //		this.adjustRHSMYumRepo(true);
 //		/*for(ProductID sub:this.consumedProductIDs){
 //			ArrayList<String> repos = this.getYumRepolist();
 //			Assert.assertTrue(repos.contains(sub.productId),
 //					"Yum reports product subscribed to repo: " + sub.productId);
 //		}*/
 //	}
 	@Test(	description="subscription-manager Yum plugin: enable/disable",
 //			dependsOnGroups={"sm_stage5"},
 //			groups={"sm_stage6", "not_implemented"},
 			enabled=false)
 	@ImplementsTCMS(id="41696")
 	public void EnableYumRepoAndVerifyContentAvailable_Test() {
 		clienttasks.unregister();
 		clienttasks.register(clientusername, clientpassword, null, null, null, null);
 		clienttasks.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		
 		// Edit /etc/yum/pluginconf.d/rhsmplugin.conf and ensure that the enabled directive is set to 1
 		clienttasks.adjustRHSMYumRepo(true);
 
 		// 1. Run a 'yum repolist' and get a list of all of the available repositories corresponding to your entitled products
 		// 1. Repolist contains repositories corresponding to your entitled products
 //		for(ProductSubscription sub:sm.getCurrentlyConsumedProductSubscriptions()){
 //			ArrayList<String> repos = this.getYumRepolist();
 //			Assert.assertTrue(repos.contains(sub.productId),
 //					"Yum reports product subscribed to repo: " + sub.productId);
 //		}
 		
 		// Edit /etc/yum/pluginconf.d/rhsmplugin.conf and ensure that the enabled directive is set to 0
 		clienttasks.adjustRHSMYumRepo(false);
 		
 		// 2. Run a 'yum repolist' and get a list of all of the available repositories corresponding to your entitled products
 		// 2. Repolist does not contain repositories corresponding to your entitled products
 throw new SkipException("THIS TESTCASE IS UNDER CONSTRUCTION. IMPLEMENTATION OF https://tcms.engineering.redhat.com/case/41696/?search=41696");		
 
 	}
 	
 	
 	@Test(	description="subscription-manager Yum plugin: ensure ...",
 //	        dependsOnGroups={"sm_stage6"},
 //	        groups={"sm_stage7"},
 	        enabled=true)
 	@ImplementsTCMS(id="47578")
 	public void VerifyReposAvailableForEnabledContent(){
 //	    ArrayList<String> repos = this.getYumRepolist();
 //	    
 //	    for (EntitlementCert cert:clienttasks.getCurrentEntitlementCerts()){
 //	    	if(cert.enabled.contains("1"))
 //	    		Assert.assertTrue(repos.contains(cert.label),
 //	    				"Yum reports enabled content subscribed to repo: " + cert.label);
 //	    	else
 //	    		Assert.assertFalse(repos.contains(cert.label),
 //	    				"Yum reports enabled content subscribed to repo: " + cert.label);
 //	    }
 // FIXME: Untested Alternative to above procedure is:
 		clienttasks.unregister();
 	    clienttasks.register(clientusername, clientpassword, null, null, null, null);
 	    clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 	    List<EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 	    Assert.assertTrue(!entitlementCerts.isEmpty(),"After subscribing to all available subscription pools, there must be some entitlements."); // or maybe we should skip when nothing is consumed 
 	    clienttasks.assertEntitlementCertsInYumRepolist(entitlementCerts,true);
 	}
 	
 	
 	@Test(	description="subscription-manager Yum plugin: ensure content can be downloaded/installed",
 //			dependsOnGroups={"sm_stage6"},
 //			groups={"sm_stage7", "not_implemented"},
 			enabled=false)
 	@ImplementsTCMS(id="41695")
 	public void InstallPackageFromRHSMYumRepo_Test(){
 		HashMap<String, String[]> pkgList = clienttasks.getPackagesCorrespondingToSubscribedRepos();
 		for(ProductSubscription productSubscription : clienttasks.getCurrentlyConsumedProductSubscriptions()){
 			String pkg = pkgList.get(productSubscription.productName)[0];
 			log.info("Attempting to install first pkg '"+pkg+"' from product subscription: "+productSubscription);
 			log.info("timeout of two minutes for next three commands");
 			RemoteFileTasks.runCommandExpectingNoTracebacks(client,
 					"yum repolist");
 			RemoteFileTasks.runCommandExpectingNoTracebacks(client,
 					"yum install -y "+pkg);
 			RemoteFileTasks.runCommandExpectingNoTracebacks(client,
 					"rpm -q "+pkg);
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager Yum plugin: enable/disable",
 //			dependsOnGroups={"sm_stage7"},
 //			groups={"sm_stage8", "not_implemented"},
 			enabled=false)
 	@ImplementsTCMS(id="41696")
 	public void DisableYumRepoAndVerifyContentNotAvailable_Test(){
 		clienttasks.adjustRHSMYumRepo(false);
 		for(SubscriptionPool sub:clienttasks.getCurrentlyAvailableSubscriptionPools())
 			for(String repo:this.getYumRepolist())
 				if(repo.contains(sub.subscriptionName))
 					Assert.fail("After unsubscribe, Yum still has access to repo: "+repo);
 	}
 	
 	
 	@Test(	description="rhsmcertd: change certFrequency",
 //			dependsOnGroups={"sm_stage3"},
 //			groups={"sm_stage4"},
 			dataProvider="getCertFrequencyData",
 			groups={"blockedByBug-617703"},
 			enabled=true)
 	@ImplementsTCMS(id="41692")
 	public void rhsmcertdChangeCertFrequency_Test(int minutes) {
 
 		log.info("First test with an unregistered user and verify that the rhsmcertd actually fails since it cannot self-identify itself to the candlepin server.");
 		clienttasks.unregister();
 		clienttasks.restart_rhsmcertd(minutes, false);
 		log.info("Appending a marker in the '"+SubscriptionManagerTasks.rhsmcertdLogFile+"' so we can assert that the certificates are being updated every '"+minutes+"' minutes");
 		String marker = "Testing rhsm.conf certFrequency="+minutes+" when unregistered..."; // https://tcms.engineering.redhat.com/case/41692/
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+SubscriptionManagerTasks.rhsmcertdLogFile,Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+SubscriptionManagerTasks.rhsmcertdLogFile,Integer.valueOf(0),marker,null);
 		sleep(minutes*60*1000);sleep(10000);	// give the rhsmcertd a chance check in with the candlepin server and update the certs
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+SubscriptionManagerTasks.rhsmcertdLogFile,Integer.valueOf(0),"update failed \\(\\d+\\), retry in "+minutes+" minutes",null);
		//RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+SubscriptionManagerTasks.rhsmLogFile,Integer.valueOf(0),"Either the consumer is not registered with candlepin or the certificates are corrupted. Certificate updation using daemon failed.",null);
		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+SubscriptionManagerTasks.rhsmLogFile,Integer.valueOf(0),"Either the consumer is not registered or the certificates are corrupted. Certificate update using daemon failed.",null);

 		
 		log.info("Now test with an registered user and verify that the rhsmcertd succeeds because he can identify himself to the candlepin server.");
 	    clienttasks.register(clientusername, clientpassword, null, null, null, null);
 		clienttasks.restart_rhsmcertd(minutes, false);
 		log.info("Appending a marker in the '"+SubscriptionManagerTasks.rhsmcertdLogFile+"' so we can assert that the certificates are being updated every '"+minutes+"' minutes");
 		marker = "Testing rhsm.conf certFrequency="+minutes+" when registered..."; // https://tcms.engineering.redhat.com/case/41692/
 		RemoteFileTasks.runCommandAndAssert(client,"echo \""+marker+"\" >> "+SubscriptionManagerTasks.rhsmcertdLogFile,Integer.valueOf(0));
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+SubscriptionManagerTasks.rhsmcertdLogFile,Integer.valueOf(0),marker,null);
 		sleep(minutes*60*1000);sleep(10000);	// give the rhsmcertd a chance check in with the candlepin server and update the certs
 		RemoteFileTasks.runCommandAndAssert(client,"tail -1 "+SubscriptionManagerTasks.rhsmcertdLogFile,Integer.valueOf(0),"certificates updated",null);
 
 		/* tail -f /var/log/rhsm/rhsm.log
 		 * 2010-09-10 12:05:06,338 [ERROR] main() @certmgr.py:75 - Either the consumer is not registered with candlepin or the certificates are corrupted. Certificate updation using daemon failed.
 		 */
 		
 		/* tail -f /var/log/rhsm/rhsmcertd.log
 		 * Fri Sep 10 11:59:50 2010: started: interval = 1 minutes
 		 * Fri Sep 10 11:59:51 2010: update failed (255), retry in 1 minutes
 		 * testing rhsm.conf certFrequency=1 when unregistered.
 		 * Fri Sep 10 12:00:51 2010: update failed (255), retry in 1 minutes
 		 * Fri Sep 10 12:01:04 2010: started: interval = 1 minutes
 		 * Fri Sep 10 12:01:05 2010: certificates updated
 		 * testing rhsm.conf certFrequency=1 when registered.
 		 * Fri Sep 10 12:02:05 2010: certificates updated
 		*/
 	}
 	
 	
 	@Test(	description="rhsmcertd: ensure certificates synchronize",
 //			dependsOnGroups={"sm_stage3"},
 //			groups={"sm_stage4"},
 			groups={"blockedByBug-617703"},
 			enabled=true)
 	@ImplementsTCMS(id="41694")
 	public void rhsmcertdEnsureCertificatesSynchronize_Test(){
 //FIXME Replacing ssalevan's original implementation of this test... 10/5/2010 jsefler
 //		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 //		//SubscribeToASingleEntitlementByProductID_Test();
 //		client.runCommandAndWait("rm -rf "+clienttasks.entitlementCertDir+"/*");
 //		client.runCommandAndWait("rm -rf "+clienttasks.productCertDir+"/*");
 //		//certFrequency_Test(1);
 //		clienttasks.restart_rhsmcertd(1,true);
 ////		client.runCommandAndWait("cat /dev/null > "+rhsmcertdLogFile);
 ////		//sshCommandRunner.runCommandAndWait("rm -f "+rhsmcertdLogFile);
 ////		//sshCommandRunner.runCommandAndWait("/etc/init.d/rhsmcertd restart");
 ////		this.sleep(70*1000);
 ////		
 ////		Assert.assertEquals(RemoteFileTasks.grepFile(client,
 ////				rhsmcertdLogFile,
 ////				"certificates updated"),
 ////				0,
 ////				"rhsmcertd reports that certificates have been updated");
 //		
 //		//verify that PEM files are present in all certificate directories
 //		RemoteFileTasks.runCommandAndAssert(client, "ls "+clienttasks.entitlementCertDir+" | grep pem", 0, "pem", null);
 //		RemoteFileTasks.runCommandAndAssert(client, "ls "+clienttasks.entitlementCertDir+"/product | grep pem", 0, "pem", null);
 //		// this directory will only be populated if you upload ur own license, not while working w/ candlepin
 //		/*RemoteFileTasks.runCommandAndAssert(sshCommandRunner, "ls /etc/pki/product", 0, "pem", null);*/
 		
 		// start with a cleanly unregistered system
 		clienttasks.unregister();
 		
 		// register a clean user
 	    clienttasks.register(clientusername, clientpassword, null, null, null, null);
 	    
 	    // subscribe to all the available pools
 	    clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools(ConsumerType.system);
 	    
 	    // get all of the current entitlement product certs and remember them
 	    List<File> entitlementCertFiles = clienttasks.getCurrentEntitlementCertFiles();
 	    
 	    // delete all of the entitlement cert files
 	    client.runCommandAndWait("rm -rf "+clienttasks.entitlementCertDir+"/*");
 	    Assert.assertEquals(clienttasks.getCurrentEntitlementCertFiles().size(), 0,
 	    		"All the entitlement product certs have been deleted.");
 		
 	    // restart the rhsmcertd to run every 1 minute and wait for a refresh
 		clienttasks.restart_rhsmcertd(1, true);
 		
 		// assert that rhsmcertd has refreshed the entitled product certs back to the original
 	    Assert.assertEquals(clienttasks.getCurrentEntitlementCertFiles(), entitlementCertFiles,
 	    		"All the deleted entitlement product certs have been re-synchronized by rhsm cert deamon.");
 	}
 	
 	
 	
 	// Protected Methods ***********************************************************************
 	
 	protected ArrayList<String> getYumRepolist(){
 		ArrayList<String> repos = new ArrayList<String>();
 		client.runCommandAndWait("killall -9 yum");
 		
 		client.runCommandAndWait("yum repolist");
 		String[] availRepos = client.getStdout().split("\\n");
 		
 		int repolistStartLn = 0;
 		int repolistEndLn = 0;
 		
 		for(int i=0;i<availRepos.length;i++)
 			if (availRepos[i].contains("repo id"))
 				repolistStartLn = i + 1;
 			else if (availRepos[i].contains("repolist:"))
 				repolistEndLn = i;
 		
 		for(int i=repolistStartLn;i<repolistEndLn;i++)
 			repos.add(availRepos[i].split(" ")[0]);
 		
 		return repos;
 	}
 	
 //	protected void adjustRHSMYumRepo(boolean enabled){
 //		Assert.assertEquals(
 //				RemoteFileTasks.searchReplaceFile(client, 
 //						rhsmYumRepoFile, 
 //						"^enabled=.*$", 
 //						"enabled="+(enabled?'1':'0')),
 //						0,
 //						"Adjusted RHSM Yum Repo config file, enabled="+(enabled?'1':'0')
 //				);
 //	}
 	
 	
 	// Data Providers ***********************************************************************
 
 	
 
 	
 	@DataProvider(name="getCertFrequencyData")
 	public Object[][] getCertFrequencyDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getCertFrequencyDataAsListOfLists());
 	}
 	protected List<List<Object>> getCertFrequencyDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		// int minutes
 		ll.add(Arrays.asList(new Object[]{2}));
 		ll.add(Arrays.asList(new Object[]{1}));
 		
 		return ll;
 	}
 }
