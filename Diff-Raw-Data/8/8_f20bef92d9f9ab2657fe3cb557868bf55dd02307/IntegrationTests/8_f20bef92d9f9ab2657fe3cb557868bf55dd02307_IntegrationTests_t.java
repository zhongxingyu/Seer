 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.auto.testng.TestNgPriority;
 import com.redhat.qe.sm.base.ConsumerType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.ContentNamespace;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductCert;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 /**
  * @author jsefler
  *
  * Reference: https://docspace.corp.redhat.com/docs/DOC-60198
  * http://gibson.usersys.redhat.com:9000/Integration-Testing-Issues
  */
 @Test(groups={"IntegrationTests"})
 public class IntegrationTests extends SubscriptionManagerCLITestScript{
 
 	
 	// Test Methods ***********************************************************************
 
 	@Test(	description="register and subscribe to expected product subscription",
 			groups={},
 			dataProvider="getSubscribeData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void Subscribe_Test(String username, String password, String productId) {
 		clienttasks.register(username, password, null, null, null, null, true, null, null, null);
 		File entitlementCertFile = clienttasks.subscribeToProductId(productId);
 
 		EntitlementCert entitlementCert = clienttasks.getEntitlementCertFromEntitlementCertFile(entitlementCertFile);
 		entitlementCertData.add(Arrays.asList(new Object[]{username, password, productId, entitlementCert}));
 	}
 	
 	@Test(	description="verify the CDN provides packages for the default enabled content set after subscribing to a product subscription",
 			groups={"VerifyPackagesAreAvailable"},
 			dependsOnMethods={"Subscribe_Test"}, alwaysRun=true,
 			dataProvider="getDefaultEnabledContentNamespaceData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void VerifyPackagesAreAvailableForDefaultEnabledContentNamespace_Test(String username, String password, String productId, ContentNamespace contentNamespace) {
 		String abled = contentNamespace.enabled.equals("1")? "enabled":"disabled";	// is this an enabled or disabled test?
 
 		// register
 		clienttasks.register(username, password, null, null, null, null, true, null, null, null);
 		
 		// assert that there are not yet any available packages from the default enabled/disabled repo
 		Integer packageCount = clienttasks.getYumRepolistPackageCount(contentNamespace.label);
 		Assert.assertEquals(packageCount,Integer.valueOf(0),"Before subscribing to product subscription '"+productId+"', the number of available packages '"+packageCount+"' from the default "+abled+" repo '"+contentNamespace.label+"' is zero.");
 
 		// subscribe
 		clienttasks.subscribeToProductId(productId);
 
 		// Assert that after subscribing, the default enabled/disabled repo is now included in yum repolist
 		ArrayList<String> repolist = clienttasks.getYumRepolist(abled);
 		if (clienttasks.areAllRequiredTagsInContentNamespaceProvidedByProductCerts(contentNamespace, currentProductCerts)) {
 			Assert.assertTrue(repolist.contains(contentNamespace.label),
 				"Yum repolist "+abled+" includes "+abled+" repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+productId+"'.");
 		} else {
 			log.warning("Did not find all the requiredTags '"+contentNamespace.requiredTags+"' for this content namespace amongst the currently installed products.");
 			Assert.assertFalse(repolist.contains(contentNamespace.label),
 				"Yum repolist "+abled+" excludes "+abled+" repo id/label '"+contentNamespace.label+"' after having subscribed to Subscription ProductId '"+productId+"' because not all requiredTags ("+contentNamespace.requiredTags+") in the contentNamespace are provided by the currently installed productCerts.");
 			throw new SkipException("This contentNamespace has requiredTags '"+contentNamespace.requiredTags+"' that were not found amongst all of the currently installed products.  Therefore we cannot verify that the CDN is providing packages for repo '"+contentNamespace.label+"'.");
 		}
 
 		// verify the yum repolist contentNamespace.label returns more than 0 packages
 		String options = contentNamespace.enabled.equals("1")? contentNamespace.label:contentNamespace.label+" --enablerepo="+contentNamespace.label;
 		packageCount = clienttasks.getYumRepolistPackageCount(options);
 		Assert.assertTrue(packageCount>0,"After subscribing to product subscription '"+productId+"', the number of available packages '"+packageCount+"' from the default "+abled+" repo '"+contentNamespace.label+"' is greater than zero.");
 	}
 	
 	
 	@Test(	description="verify the CDN provides packages for the non-default enabled content set after subscribing to a product subscription",
 			groups={"VerifyPackagesAreAvailable"},
 			dependsOnMethods={"Subscribe_Test"}, alwaysRun=true,
 			dataProvider="getDefaultDisabledContentNamespaceData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void VerifyPackagesAreAvailableForDefaultDisabledContentNamespace_Test(String username, String password, String productId, ContentNamespace contentNamespace) {
 		Assert.assertEquals(contentNamespace.enabled,"0","Reconfirming that we are are about to test a default disabled contentNamespace.");
 		VerifyPackagesAreAvailableForDefaultEnabledContentNamespace_Test(username, password, productId, contentNamespace);
 	}
 	
 	@Test(	description="ensure an available package can be downloaded/installed/removed from the enabled repo ",
 			groups={},
 			dependsOnMethods={"Subscribe_Test"}, alwaysRun=true,
 			dependsOnGroups={"VerifyPackagesAreAvailable"},
 			dataProvider="getContentNamespaceData",
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=) //TODO Find a tcms caseId
 	public void InstallAndRemoveAnyPackageFromContentNamespace_Test(String username, String password, String productId, ContentNamespace contentNamespace) {
 
 		// register
 		clienttasks.register(username, password, null, null, null, null, true, null, null, null);
 		
 		// subscribe
 		clienttasks.subscribeToProductId(productId);
 		
 		// make sure there is a positive package count provided by this repo
 		Integer packageCount = clienttasks.getYumRepolistPackageCount(contentNamespace.label+" --enablerepo="+contentNamespace.label);
 		if (packageCount==0) {
 			throw new SkipException("Cannot install a package from this repo '"+contentNamespace.label+"' since it is not providing any packages.");
 		}
 		
 		// find an available package that is uniquely provided by this repo
 		String pkg = clienttasks.findUniqueAvailablePackageFromRepo(contentNamespace.label);
 		if (pkg==null) {
 			throw new SkipException("Could NOT find a unique available package from this repo '"+contentNamespace.label+"' to attempt an install/remove test.");
 		}
 		
 		// install the package and assert that it is successfully installed
 		clienttasks.yumInstallPackageFromRepo(pkg, contentNamespace.label); //pkgInstalled = true;
 		
 		// now remove the package
 		clienttasks.yumRemovePackage(pkg);
 	}
 	
 	
 	
 	
 //	@Test()
 //	@TestNgPriority(400)
 //	public void Test400() {}	
 //	@Test()
 //	@TestNgPriority(500)
 //	public void Test500() {}
 //	@Test()
 //	@TestNgPriority(100)
 //	public void Test100() {}
 //	@Test()
 //	@TestNgPriority(200)
 //	public void Test200() {}
 //	@Test(dependsOnMethods={"Test200"})	// adding a dependsOn* breaks the priority
 //	@TestNgPriority(300)
 //	public void Test300() {}
 
 	// Candidates for an automated Test:
	// TODO Bug 689031 - nss needs to be able to use pem files interchangeably in a single process 
 
 	
 	
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	@BeforeClass(groups={"setup"})
 	public void getCurrentProductCertsBeforeClass() {
 		currentProductCerts = clienttasks.getCurrentProductCerts();
 	}
 	
 //	@BeforeClass(groups={"setup"})
 //	public void yumCleanAllBeforeClass() {
 //		clienttasks.yumClean("all");
 //	}
 	
 	// Protected Methods ***********************************************************************
 	
 	List<List<Object>> entitlementCertData = new ArrayList<List<Object>>();
 	List<ProductCert> currentProductCerts = new ArrayList<ProductCert>();
 
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getSubscribeData")
 	public Object[][] getSubscribeDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getSubscribeDataAsListOfLists());
 	}
 	protected List<List<Object>> getSubscribeDataAsListOfLists() throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		//JSONArray jsonIntegrationTestData = new JSONArray(getProperty("sm.integrationTestData", "<>").replaceAll("<", "[").replaceAll(">", "]")); // hudson parameters use <> instead of []
 		JSONArray jsonIntegrationTestData = new JSONArray(getProperty("sm.integrationTestData", "[]").replaceFirst("^\"", "").replaceFirst("\"$", "").replaceAll("<", "[").replaceAll(">", "]")); // hudson JSONArray parameters get surrounded with double quotes that need to be stripped
 		for (int i = 0; i < jsonIntegrationTestData.length(); i++) {
 			JSONObject jsonIntegrationTestDatum = (JSONObject) jsonIntegrationTestData.get(i);
 			String username = jsonIntegrationTestDatum.getString("username");
 			String password = jsonIntegrationTestDatum.getString("password");
 			String arch = "ALL";
 			if (jsonIntegrationTestDatum.has("arch")) arch = jsonIntegrationTestDatum.getString("arch");
 			String variant = "ALL";
 			if (jsonIntegrationTestDatum.has("variant")) variant = jsonIntegrationTestDatum.getString("variant");
 	
 			// skip this jsonIntegrationTestDatum when it does not match the client arch
 			if (!arch.equals("ALL") && !arch.equals(clienttasks.arch)) continue;
 			
 			// skip this jsonIntegrationTestDatum when it does not match the client variant
 			if (!variant.equals("ALL") && !variant.equals(clienttasks.variant)) continue;
 		
 			JSONArray jsonProductIdsData = (JSONArray) jsonIntegrationTestDatum.getJSONArray("productIdsData");
 			for (int j = 0; j < jsonProductIdsData.length(); j++) {
 				JSONObject jsonProductIdsDatum = (JSONObject) jsonProductIdsData.get(j);
 				String productId = jsonProductIdsDatum.getString("productId");
 				
 				// String username, String password, String productId
 				ll.add(Arrays.asList(new Object[]{username, password, productId}));
 			}
 		}
 		
 		return ll;
 	}
 	
 	@DataProvider(name="getDefaultEnabledContentNamespaceData")
 	public Object[][] getDefaultEnabledContentNamespaceDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getContentNamespaceDataAsListOfLists("1"));
 	}
 	@DataProvider(name="getDefaultDisabledContentNamespaceData")
 	public Object[][] getDefaultDisabledContentNamespaceDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getContentNamespaceDataAsListOfLists("0"));
 	}
 	@DataProvider(name="getContentNamespaceData")
 	public Object[][] getContentNamespaceDataAs2dArray() throws JSONException {
 		return TestNGUtils.convertListOfListsTo2dArray(getContentNamespaceDataAsListOfLists(null));
 	}
 	protected List<List<Object>> getContentNamespaceDataAsListOfLists(String enabledValue) throws JSONException {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		for (List<Object> row : entitlementCertData) {
 			String username = (String) row.get(0);
 			String password = (String) row.get(1);
 			String productId = (String) row.get(2);
 			EntitlementCert entitlementCert = (EntitlementCert) row.get(3);
 			
 			for (ContentNamespace contentNamespace : entitlementCert.contentNamespaces) {
 				if (contentNamespace.enabled.equals(enabledValue) || enabledValue==null) {	// enabled="1", not enabled="0", either=null
 					
 					// String username, String password, String productId, ContentNamespace contentNamespace
 					ll.add(Arrays.asList(new Object[]{username, password, productId, contentNamespace}));
 				}
 			}
 		}
 		return ll;
 	}
 
 
 
 
 }
