 package com.redhat.qe.sm.cli.tests;
 
 import java.util.List;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.EntitlementCert;
 import com.redhat.qe.sm.data.ProductSubscription;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 
 /**
  * @author jsefler
  *
  *
  */
 @Test(groups={"RefreshTests"})
 public class RefreshTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="subscription-manager-cli: refresh and verify entitlements are updated",
 			groups={"AcceptanceTests","RefreshEntitlements_Test"},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=64182)	// http://gibson.usersys.redhat.com/agilo/ticket/4022
 	public void RefreshEntitlements_Test() {
 		
 		// Start fresh by unregistering and registering...
 		log.info("Start fresh by unregistering and registering...");
 		clienttasks.unregister(null, null, null);
 		clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername,sm_clientPassword,sm_clientOrg,null,null,null,null, null, null, null, (String)null, null, false, null, null, null));
 		
 		// make sure the certFrequency will not affect the results of this test
 		log.info("Change the certFrequency to a large value to assure the rhsmcertd does not interfere with this test.");
 		clienttasks.restart_rhsmcertd(60, null, false);
 		
 		// Subscribe to a randomly available pool...
 		log.info("Subscribe to a randomly available pool...");
		List<SubscriptionPool> pools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool pool = pools.get(randomGenerator.nextInt(pools.size())); // randomly pick a pool
 		clienttasks.subscribeToSubscriptionPoolUsingPoolId(pool);
 		
 		// remember the currently consumed product subscriptions (and entitlement certs)
 		List<ProductSubscription> consumedProductSubscriptions = clienttasks.getCurrentlyConsumedProductSubscriptions();
 		List<EntitlementCert> entitlementCerts = clienttasks.getCurrentEntitlementCerts();
 
 		// remove your entitlements
 		log.info("Removing the entitlement certs...");
 		clienttasks.removeAllCerts(false,true, false);
 		Assert.assertEquals(clienttasks.getCurrentEntitlementCerts().size(),0,"Entitlements have been removed.");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions().size(),0,"Consumed subscription pools do NOT exist after entitlements have been removed.");
 
 		// refresh
 		log.info("Refresh...");
 		clienttasks.refresh(null, null, null);
 		
 		// Assert the entitlement certs are restored after the refresh
 		log.info("After running refresh, assert that the entitlement certs are restored...");
 		Assert.assertEquals(clienttasks.getCurrentEntitlementCerts(),entitlementCerts,"Original entitlements have been restored.");
 		Assert.assertEquals(clienttasks.getCurrentlyConsumedProductSubscriptions(),consumedProductSubscriptions,"Original consumed product subscriptions have been restored.");
 	}
 	
 	
 	@Test(	description="[abrt] subscription-manager-0.95.17-1.el6_1: Process /usr/bin/rhsmcertd was killed by signal 11 (SIGSEGV)",
 			groups={"blockedByBug-725535","VerificationFixForBug725535_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)	// http://gibson.usersys.redhat.com/agilo/ticket/4022
 	public void VerificationFixForBug725535_Test() {
 		
 		// assert that rhsmcertd restart successfully before actually running this test
 		clienttasks.restart_rhsmcertd(null,null,false);
 		
 		// block the ability of subscription-manager to write to /var/run/rhsm/update by creating a directory in its place
 		removeRhsmUpdateFileAfterGroups();
 		client.runCommandAndWait("mkdir "+clienttasks.rhsmUpdateFile);
 		
 		// mark the /var/log/messages so we can search for an abrt afterwards
 		String marker = "SM TestClass marker "+String.valueOf(System.currentTimeMillis());	// using a timestamp on the class marker will help identify the test class during which a denial is logged
 		RemoteFileTasks.markFile(client, clienttasks.varLogMessagesFile, marker);
 		//clienttasks.rhsmcertdServiceRestart(null,null,false);
 		RemoteFileTasks.runCommandAndAssert(client,"service rhsmcertd restart",Integer.valueOf(0),"^Starting rhsmcertd \\d+ \\d+\\[  OK  \\]$",null);	
 		//RemoteFileTasks.runCommandAndAssert(client,"service rhsmcertd status",Integer.valueOf(0),"^rhsmcertd \\(pid \\d+ \\d+\\) is running...$",null);	// RHEL62
 		RemoteFileTasks.runCommandAndAssert(client,"service rhsmcertd status",Integer.valueOf(0),"^rhsmcertd \\(pid( \\d+){1,2}\\) is running...$",null);	// RHEL62 or RHEL58
 		
 		/* # tail /var/log/rhsm/rhsmcertd.log
 		Tue Sep 27 17:36:32 2011: started: interval = 240 minutes
 		Tue Sep 27 17:36:32 2011: started: interval = 1440 minutes
 		Tue Sep 27 17:36:32 2011: certificates updated
 		Tue Sep 27 17:36:32 2011: error opening /var/run/rhsm/update to write
 		timestamp: Is a directory
 		Tue Sep 27 17:36:32 2011: certificates updated
 		Tue Sep 27 17:36:32 2011: error opening /var/run/rhsm/update to write
 		timestamp: Is a directory
 		*/
 		
 		/* tail /var/log/messages
 		Sep 27 14:58:42 jsefler-onprem-62server kernel: rhsmcertd[7117]: segfault at 0 ip 00000039f7a665be sp 00007fff37437d40 error 4 in libc-2.12.so[39f7a00000+197000]
 		Sep 27 14:58:42 jsefler-onprem-62server abrt[7174]: saved core dump of pid 7117 (/usr/bin/rhsmcertd) to /var/spool/abrt/ccpp-2011-09-27-14:58:42-7117.new/coredump (323584 bytes)
 		Sep 27 14:58:42 jsefler-onprem-62server abrtd: Directory 'ccpp-2011-09-27-14:58:42-7117' creation detected
 		Sep 27 14:58:42 jsefler-onprem-62server abrtd: Package 'subscription-manager' isn't signed with proper key
 		Sep 27 14:58:42 jsefler-onprem-62server abrtd: Corrupted or bad dump /var/spool/abrt/ccpp-2011-09-27-14:58:42-7117 (res:2), deleting
 		Sep 27 14:58:43 jsefler-onprem-62server kernel: rhsmcertd[7119]: segfault at 0 ip 00000039f7a665be sp 00007fff37437d40 error 4 in libc-2.12.so[39f7a00000+197000]
 		Sep 27 14:58:43 jsefler-onprem-62server abrt[7201]: not dumping repeating crash in '/usr/bin/rhsmcertd'
 		*/
 		
 		// verify that no subscription-manager abrt was logged to /var/log/messages 
 		Assert.assertTrue(RemoteFileTasks.getTailFromMarkedFile(client, clienttasks.varLogMessagesFile, marker, null/*abrt*/).trim().equals(""), "No segfault was logged in '"+clienttasks.varLogMessagesFile+"' on "+client.getConnection().getHostname()+" while regression testing bug 725535.");
 
 	}
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 665118 - Refresh pools will not notice change in provided products 
 	
 	
 	
 	// Configuration methods ***********************************************************************
 
 	@BeforeGroups(value="VerificationFixForBug725535_Test",groups={"setup"})
 	@AfterGroups(value="VerificationFixForBug725535_Test",groups={"setup"})
 	public void removeRhsmUpdateFileAfterGroups () {
 		if (clienttasks==null) return;
 		client.runCommandAndWait("rm -f "+clienttasks.rhsmUpdateFile+"; rmdir "+clienttasks.rhsmUpdateFile);
 		//client.runCommandAndWait("rm -f "+clienttasks.rhsmUpdateFile);
 		//client.runCommandAndWait("rmdir "+clienttasks.rhsmUpdateFile);
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, clienttasks.rhsmUpdateFile)==0, "rhsm update file '"+clienttasks.rhsmUpdateFile+"' has been removed.");
 	}
 	
 	@AfterClass(groups={"setup"})
 	public void rhsmcertdServiceRestartAfterClass () {
 		if (clienttasks==null) return;
 		clienttasks.restart_rhsmcertd(null,null,false);
 	}
 	
 	// Protected methods ***********************************************************************
 
 
 	
 	// Data Providers ***********************************************************************
 
 
 }
