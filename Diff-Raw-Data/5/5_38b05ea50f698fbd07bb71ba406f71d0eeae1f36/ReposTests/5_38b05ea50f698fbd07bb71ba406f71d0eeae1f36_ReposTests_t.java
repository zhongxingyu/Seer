 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.json.JSONException;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.Repo;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.sm.data.YumRepo;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  */
 @Test(groups={"ReposTests"})
 public class ReposTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: subscribe to a pool and verify that the newly entitled content namespaces are represented in the repos list",
 			enabled=true,
 			groups={"AcceptanceTests"},
 			dataProvider="getAvailableSubscriptionPoolsData")
 	//@ImplementsNitrateTest(caseId=)
 	public void ReposListReportsGrantedContentNamespacesAfterSubscribingToPool_Test(SubscriptionPool pool) throws JSONException, Exception{
 		log.info("Following is a list of previously subscribed repos...");
 		List<Repo> priorRepos = clienttasks.getCurrentlySubscribedRepos();
 		
 		//File entitlementCertFile = clienttasks.subscribeToSubscriptionPool(pool);	// for this test, we can skip the exhaustive asserts done by this call to clienttasks.subscribeToSubscriptionPool(pool)
 		File entitlementCertFile = clienttasks.subscribeToSubscriptionPool_(pool);
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertFile.getPath()),1, "Found the EntitlementCert file ("+entitlementCertFile+") that was granted after subscribing to pool id '"+pool.poolId+"'.");
 
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		
 		
 		// the following block of code was added to account for prior subscribed modifier pools that could provide more repos than expected once this pool is subscribed
 		// check the modifierSubscriptionData for SubscriptionPools that may already have been subscribed too and will modify this pool thereby enabling more repos than expected 
 		for (List<Object> row : modifierSubscriptionData) {
 			// ll.add(Arrays.asList(new Object[]{modifierPool, label, modifiedProductIds, requiredTags, providingPools}));
 			SubscriptionPool modifierPool = (SubscriptionPool)row.get(0);
 			String label = (String)row.get(1);
 			List<String> modifiedProductIds = (List<String>)row.get(2);
 			String requiredTags = (String)row.get(3);
 			List<SubscriptionPool> providingPools = (List<SubscriptionPool>)row.get(4);
 			if (providingPools.contains(pool)) {
 				if (priorSubscribedPools.contains(modifierPool)) {
 					// the modifier's content should now be available in the repos too
 					EntitlementCert modifierEntitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(modifierPool);						
 
 					// simply add the contentNamespaces (if not already there) from the modifier to the entitlement cert's contentNamespaces so they will be accounted for in the repos list test below
 					for (ContentNamespace contentNamespace : modifierEntitlementCert.contentNamespaces) {
 						if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 						if (!entitlementCert.contentNamespaces.contains(contentNamespace)) {
 							log.warning("Due to a previously subscribed modifier subscription pool ("+modifierPool.subscriptionName+"), the new repos listed should also include ContentNamespace: "+contentNamespace);
 							entitlementCert.contentNamespaces.add(contentNamespace);
 						}
 					}
 				}
 			}
 		}
 		priorSubscribedPools.add(pool);
 		
 		log.info("Following is the new list of subscribed repos after subscribing to pool: "+pool);			
 		List<Repo> actualRepos = clienttasks.getCurrentlySubscribedRepos();
 		
 		// assert that the new contentNamespaces from the entitlementCert are listed in repos
 		int numNewRepos=0;
 		for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 			if (!contentNamespace.type.equalsIgnoreCase("yum")) continue;
 
 			// instantiate the expected Repo that represents this contentNamespace
 			String expectedRepoUrl;	// the expected RepoUrl is set by joining the rhsm.conf baseurl with the downloadUrl in the contentNamespace which is usually a relative path.  When it is already a full path, leave it!
 			if (contentNamespace.downloadUrl.contains("://")) {
 				expectedRepoUrl = contentNamespace.downloadUrl;
 			} else {
 				expectedRepoUrl = clienttasks.baseurl.replaceFirst("//+$","//")+contentNamespace.downloadUrl.replaceFirst("^//+","");	// join baseurl to downloadUrl with "/"
 			}
 			Repo expectedRepo = new Repo(contentNamespace.name,contentNamespace.label,expectedRepoUrl,contentNamespace.enabled.trim().equals("1")?true:false);
 			
 			// assert the subscription-manager repos --list reports the expectedRepo (unless it requires tags that are not found in the installed product certs)
 			if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace,currentProductCerts)) {
 				Assert.assertTrue(actualRepos.contains(expectedRepo),"The newly entitled contentNamespace '"+contentNamespace+"' is represented in the subscription-manager repos --list by: "+expectedRepo);
 				
 				if (!priorRepos.contains(expectedRepo)) numNewRepos++;	// also count the number of NEW contentNamespaces
 				
 			} else {
 				Assert.assertFalse(actualRepos.contains(expectedRepo),"The newly entitled contentNamespace '"+contentNamespace+"' is NOT represented in the subscription-manager repos --list because it requires tags ("+contentNamespace.requiredTags+") that are not provided by the currently installed product certs.");
 			}
 		}
 
 		
 		// assert that the number of repos reported has increased by the number of contentNamespaces in the new entitlementCert (unless the 
 		Assert.assertEquals(actualRepos.size(), priorRepos.size()+numNewRepos, "The number of entitled repos has increased by the number of NEW contentNamespaces ("+numNewRepos+") from the newly granted entitlementCert.");
 		
 		// randomly decide to unsubscribe from the pool only for the purpose of saving on accumulated logging and avoid a java heap memory error
 		//if (randomGenerator.nextInt(2)==1) clienttasks.unsubscribe(null, entitlementCert.serialNumber, null, null, null); AND ALSO REMOVE pool FROM priorSubscribedPools
 	}
 	protected List<SubscriptionPool> priorSubscribedPools=new ArrayList<SubscriptionPool>();
 	
 	
 	@Test(	description="subscription-manager: subscribe to a future pool and verify that NO content namespaces are represented in the repos list",
 			groups={"blockedByBug-768983","unsubscribeAllBeforeThisTest"},
 			dataProvider="getAllFutureSystemSubscriptionPoolsData",
 			enabled=true)
 			//@ImplementsNitrateTest(caseId=)
 	public void ReposListReportsNoContentNamespacesAfterSubscribingToFuturePool_Test(SubscriptionPool pool) throws Exception {
 		
 		// subscribe to the future SubscriptionPool
 		SSHCommandResult subscribeResult = clienttasks.subscribe(null,pool.poolId,null,null,null,null,null,null,null,null);
 
 		// assert that the granted EntitlementCert and its corresponding key exist
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertCorrespondingToSubscribedPool(pool);
 		File entitlementCertFile = clienttasks.getEntitlementCertFileFromEntitlementCert(entitlementCert);
 		File entitlementCertKeyFile = clienttasks.getEntitlementCertKeyFileFromEntitlementCert(entitlementCert);
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertFile.getPath()), 1,"EntitlementCert file exists after subscribing to future SubscriptionPool.");
 		Assert.assertEquals(RemoteFileTasks.testFileExists(client, entitlementCertKeyFile.getPath()), 1,"EntitlementCert key file exists after subscribing to future SubscriptionPool.");
 		
 		// assuming that we are not subscribed to a non-future subscription pool, assert that there are NO subscribed repos 
 		Assert.assertEquals(clienttasks.getCurrentlySubscribedRepos().size(),0,"Assuming that we are not currently subscribed to a non-future subscription pool, then there should NOT be any repos reported after subscribing to future subscription pool '"+pool.poolId+"'.");
 		
 		// TODO we may want to randomly unsubscribe from serial number without asserting to save some computation of the accumulating entitlement certs
 	}
 	
 	
 	@Test(	description="subscription-manager: after subscribing to all pools, verify that manual edits to enable repos in redhat.repo are preserved.",
 			enabled=true,
 			groups={},
 			dataProvider="getYumReposData")
 	//@ImplementsNitrateTest(caseId=)
 	public void ReposListPreservesManualEditsToEnablementOfRedhatRepos_Test(YumRepo yumRepo){
 
 		Repo repo = new Repo(yumRepo.name,yumRepo.id,yumRepo.baseurl,yumRepo.enabled);
 
 		// assert that the yumRepo is reported in the subscription-manager repos
 		List<Repo> currentlySubscribedRepos = clienttasks.getCurrentlySubscribedRepos();
 		Assert.assertTrue(currentlySubscribedRepos.contains(repo),"The yumRepo '"+yumRepo+"' is represented in the subscription-manager repos --list by: "+repo);
 		
 		// also verify that yumRepo is reported in the yum repolist
 		Assert.assertTrue(clienttasks.getYumRepolist(yumRepo.enabled?"enabled":"disabled").contains(yumRepo.id), "yum repolist properly reports the enablement of yumRepo id '"+yumRepo.id+"' before manually changing its enabled value.");
 	
 		// manually edit the redhat.repo and change the enabled parameter for this yumRepo
 		Boolean newEnabledValue = yumRepo.enabled? false:true;	// toggle the value
 		clienttasks.updateYumRepoParameter(clienttasks.redhatRepoFile,yumRepo.id,"enabled",newEnabledValue.toString());
 		Repo newRepo = new Repo(yumRepo.name,yumRepo.id,yumRepo.baseurl,newEnabledValue);
 
 		// verify that the change is preserved by subscription-manager repos --list
 		currentlySubscribedRepos = clienttasks.getCurrentlySubscribedRepos();
 		Assert.assertTrue(currentlySubscribedRepos.contains(newRepo),"yumRepo id '"+yumRepo.id+"' was manually changed to enabled="+newEnabledValue+" and the subscription-manager repos --list reflects the change as: "+newRepo);
 		Assert.assertFalse(currentlySubscribedRepos.contains(repo),"The original repo ("+repo+") is no longer found in subscription-manager repos --list.");
 		
 		// also verify the change is reflected in yum repolist
 		Assert.assertTrue(clienttasks.getYumRepolist(newEnabledValue?"enabled":"disabled").contains(yumRepo.id), "yum repolist properly reports the enablement of yumRepo id '"+yumRepo.id+"' which was manually changed to '"+newEnabledValue+"'.");
 		Assert.assertFalse(clienttasks.getYumRepolist(!newEnabledValue?"enabled":"disabled").contains(yumRepo.id), "yum repolist properly reports the enablement of yumRepo id '"+yumRepo.id+"' which was manually changed to '"+newEnabledValue+"'.");
 	}
 
 	
 	@Test(	description="subscription-manager: repos --list reports no entitlements when not registered",
 			enabled=true,
 			groups={"blockedByBug-724809"})
 	//@ImplementsNitrateTest(caseId=)
 	public void ReposListIsEmptyWhenNotRegistered_Test(){
 		
 		clienttasks.unregister(null,null,null);		
 		
 		Assert.assertEquals( clienttasks.getCurrentlySubscribedRepos().size(),0, "No repos are reported by subscription-manager repos --list when not registered.");
 	}
	
	
	// Candidates for an automated Test:
	// TODO Bug 767620 - [RFE] subscription-manager should have a option that when set prevents the download / install of "redhat.repo"
 		
 	// Configuration methods ***********************************************************************
 
 	@BeforeClass(groups={"setup"})
 	public void setupBeforeClass() throws JSONException, Exception {
 		currentProductCerts = clienttasks.getCurrentProductCerts();
 		modifierSubscriptionData = getModifierSubscriptionDataAsListOfLists();
 	}
 	
 	@BeforeGroups(groups={"setup"}, value={"unsubscribeAllBeforeThisTest"})
 	public void unsubscribeAllBeforeGroups() {
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 	}
 	
 	
 	// Protected methods ***********************************************************************
 
 	List<ProductCert> currentProductCerts=new ArrayList<ProductCert>();
 	List<List<Object>> modifierSubscriptionData = null;
 
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getYumReposData")
 	public Object[][] getYumReposDataAs2dArray() throws Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getYumReposDataAsListOfLists());
 	}
 	protected List<List<Object>> getYumReposDataAsListOfLists() throws Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		
 		// register
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, (String)null, true, false, null, null, null);
 		
 		// subscribe to all available subscription so as to populate the redhat.repo file
 		clienttasks.subscribeToTheCurrentlyAvailableSubscriptionPoolsCollectively();
 		
 		clienttasks.getYumRepolist("all");	// trigger a yum transaction so that subscription-manager plugin will refresh redhat.repo
 		for (YumRepo yumRepo : clienttasks.getCurrentlySubscribedYumRepos()) {
 			ll.add(Arrays.asList(new Object[]{yumRepo}));
 			
 			// minimize the number of dataProvided rows (useful during automated testcase development)
 			if (Boolean.valueOf(getProperty("sm.debug.dataProviders.minimize","false"))) break;
 		}
 
 		return ll;
 	}
 
 }
