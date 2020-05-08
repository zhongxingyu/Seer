 package com.redhat.qe.sm.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.SkipException;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsTCMS;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.auto.testopia.Assert;
 import com.redhat.qe.sm.abstractions.EntitlementCert;
 import com.redhat.qe.sm.abstractions.ProductSubscription;
 import com.redhat.qe.sm.abstractions.SubscriptionPool;
 import com.redhat.qe.sm.base.SubscriptionManagerTestScript;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 @Test(groups={"unsubscribe"})
 public class UnsubscribeTests extends SubscriptionManagerTestScript{
 	
 	@Test(description="subscription-manager-cli: unsubscribe consumer to an entitlement using product ID",
 //			dependsOnGroups={"sm_stage4"},
 //			groups={"sm_stage5", "blockedByBug-584137", "blockedByBug-602852"},
 			groups={"blockedByBug-584137", "blockedByBug-602852"},
 			dataProvider="getAllConsumedProductSubscriptionsData")
 	@ImplementsTCMS(id="41688")
 	public void UnsubscribeFromValidProductIDs_Test(ProductSubscription productSubscription){
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 //		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 		clienttasks.unsubscribeFromProductSubscription(productSubscription);
 	}
 	
 	
 	@Test(description="Unsubscribe product entitlement and re-subscribe",
 //			dependsOnGroups={"sm_stage4"},
 //			groups={"sm_stage5", "blockedByBug-584137", "blockedByBug-602852"},
 			groups={"blockedByBug-584137", "blockedByBug-602852"},
 			dataProvider="getAllConsumedProductSubscriptionsData")
 	@ImplementsTCMS(id="41898")
 	public void ResubscribeAfterUnsubscribe_Test(ProductSubscription productSubscription){
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 //		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		
 //		// first make sure we are subscribed to all pools
 //		sm.register(username,password,null,null,null,null);
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		
 		// now loop through each consumed product subscription and unsubscribe/re-subscribe
 		SubscriptionPool pool = clienttasks.getSubscriptionPoolFromProductSubscription(productSubscription);
 		if (clienttasks.unsubscribeFromProductSubscription(productSubscription))
 			clienttasks.subscribeToSubscriptionPoolUsingProductId(pool);	// only re-subscribe when unsubscribe was a success
 	}
 	
 	
 // FIXME DELETEME This test was re-designed as a data-provided test below
 //	@Test(description="Copy entitlement certificates into /etc/pki/entitlement/product after unsubscribe",
 //			dependsOnGroups={"sm_stage4"},
 //			groups={"sm_stage5", "blockedByBug-584137", "blockedByBug-602852"})
 //	@ImplementsTCMS(id="41903")
 //	public void UnsubscribeAndReplaceCert_Test(){
 //		sshCommandRunner.runCommandAndWait("killall -9 yum");
 //		String randDir = "/tmp/sm-certs-"+Integer.toString(this.getRandInt());
 //		
 //		// make sure we are registered and then subscribe to each available subscription pool
 //		sm.register(username,password,null,null,null,null);
 //		sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 //		
 //		// copy certs to temp dir
 //		sshCommandRunner.runCommandAndWait("rm -rf "+randDir);
 //		sshCommandRunner.runCommandAndWait("mkdir -p "+randDir);
 //		sshCommandRunner.runCommandAndWait("cp /etc/pki/entitlement/product/* "+randDir);
 //		
 ////		sm.unsubscribeFromEachOfTheCurrentlyConsumedProductSubscriptions();
 //		sm.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 //		
 //		sshCommandRunner.runCommandAndWait("cp -f "+randDir+"/* /etc/pki/entitlement/product");
 //		RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner,
 //				"yum repolist");
 //		
 //		/* FIXME Left off development here:
 //201007152042:41.317 - FINE: ssh root@jsefler-rhel6-clientpin.usersys.redhat.com yum repolist (com.redhat.qe.tools.SSHCommandRunner.run)
 //201007152042:42.353 - FINE: Stdout: 
 //Loaded plugins: refresh-packagekit, rhnplugin, rhsmplugin
 //Updating Red Hat repositories.
 //repo id                              repo name                            status
 //never-enabled-content                never-enabled-content                0
 //rhel-latest                          Latest RHEL 6                        0
 //repolist: 0
 // (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 //201007152042:42.354 - FINE: Stderr: 
 //This system is not registered with RHN.
 //RHN support will be disabled.
 //http://redhat.com/foo/path/never/repodata/repomd.xml: [Errno 14] HTTP Error 404 : http://www.redhat.com/foo/path/never/repodata/repomd.xml 
 //Trying other mirror.
 // (com.redhat.qe.tools.SSHCommandRunner.runCommandAndWait)
 //201007152042:42.360 - SEVERE: Test Failed: UnsubscribeAndReplaceCert_Test (com.redhat.qe.auto.testng.TestNGListener.onTestFailure)
 //java.lang.AssertionError: Command 'yum repolist' returns nonzero error code: 0 expected:<true> but was:<false>
 //		 */
 //	}
 	
 	@Test(description="Attempt to re-use revoked entitlement certificates",
 //			dependsOnGroups={"sm_stage4"},
 //			groups={"sm_stage5", "blockedByBug-584137", "blockedByBug-602852"},
 			groups={"blockedByBug-584137", "blockedByBug-602852"},
 			dataProvider="getAllAvailableSubscriptionPoolsData")
 	@ImplementsTCMS(id="41903")
 	public void AttemptToReuseRevokedEntitlementCerts_Test(SubscriptionPool subscriptionPool){
 		client.runCommandAndWait("killall -9 yum");
 		String randDir = "/tmp/sm-certForSubscriptionPool-"+subscriptionPool.poolId;
 		
 		// subscribe to the subscription pool (one at a time)
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();	// assure we are completely unsubscribed
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(subscriptionPool);
 		
 		// assert all of the entitlement certs are displayed in the stdout from "yum repolist all"
 		clienttasks.assertEntitlementCertsAreReportedInYumRepolist(clienttasks.getCurrentEntitlementCerts());
 		// FIXME: may want to also assert that the sshCommandRunner.getStderr() does not contains an error on the entitlementCert.download_url e.g.: http://redhat.com/foo/path/never/repodata/repomd.xml: [Errno 14] HTTP Error 404 : http://www.redhat.com/foo/path/never/repodata/repomd.xml 
  
 		// copy certs to temp dir
 		client.runCommandAndWait("rm -rf "+randDir);
 		client.runCommandAndWait("mkdir -p "+randDir);
 		client.runCommandAndWait("cp /etc/pki/entitlement/product/* "+randDir);
 		
 		// unsubscribe from the subscription pool (Note: should be the only one subscribed too
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 		
 		// copy revoked certs back to /etc/pki/entitlement/product/
 		client.runCommandAndWait("cp -f "+randDir+"/* /etc/pki/entitlement/product");
 		
 		// run another yum repolist all and assert that the "current entitlement has been revoked."
 		clienttasks.assertEntitlementCertsAreReportedInYumRepolist(clienttasks.getCurrentEntitlementCerts());
 		throw new SkipException("FIXME: THIS AUTOMATED TEST IS INCOMPLETE. Need to assert that the yum repolist displayed a stderr message that entitlements from this subscription pool have been revoked: "+subscriptionPool);
 	}
 
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getAllConsumedProductSubscriptionsData")
 	public Object[][] getAllConsumedProductSubscriptionsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getAllConsumedProductSubscriptionsDataAsListOfLists());
 	}
 	protected List<List<Object>> getAllConsumedProductSubscriptionsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// first make sure we are subscribed to all pools
		clienttasks.register(clientusername,clientpassword,null,null,null,null);
 //		c1sm.subscribeToEachOfTheCurrentlyAvailableSubscriptionPools();
 		clienttasks.subscribeToAllOfTheCurrentlyAvailableSubscriptionPools();
 
 		
 		// then assemble a list of all consumed ProductSubscriptions
 		for (ProductSubscription productSubscription : clienttasks.getCurrentlyConsumedProductSubscriptions()) {
 			ll.add(Arrays.asList(new Object[]{productSubscription}));		
 		}
 		
 		return ll;
 	}
 	
 
 }
