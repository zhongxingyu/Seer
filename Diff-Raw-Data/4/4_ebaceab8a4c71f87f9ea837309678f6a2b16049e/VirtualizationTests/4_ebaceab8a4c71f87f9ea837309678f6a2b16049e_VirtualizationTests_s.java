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
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BlockedByBzBug;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.jul.TestRecords;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.SubscriptionPool;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 
 
 /**
  * @author jsefler
  *
  */
 
 // Notes...
 //<jsefler> I'm trying to strategize an automated test for the virt entitlements stuff you demo'ed on Wednesday.  I got a few questions to start with...
 //<jharris> sure
 // shoot
 //<jsefler> using the RESTapi, if I search through all the owners subscriptions and find one with a virt_limit attribute, then that means that two pools should get created corresponding to it.  correct?
 // one pool for the host and one pool fir the guests
 //<jharris> yes
 // specifically the attribute is on either the product or the pool
 //<jsefler> what does that mean?
 // the virt_limit is an attribute of the product - that I know
 // next I need to figure out what the relevant attributes are on the pool
 //<jharris> pools have attributes
 // products have attributes
 // the two pools are created, as you said
 // the physical (host) pool will have no additional attributes
 // the virt (guest) pool will have an attribute of "virt_only" set to true
 // the candlepin logic should only let virtual machines subscribe to that second pool
 // this is done by checking the virt.is_guest fact
 // that is set in subscription manager
 //<jsefler> yup - that sounds good - that's what I need to get started
 //<jharris> excellent
 // but the virt_only attribute can also just be used on a product, for example
 // so that maybe we want to start selling a product that is like RHEL for virtual machines
 // IT can just stick that virt_only attribute on the product directly
 // and it should do the same filtering
 
 
 //10/31/2011 Notes:
 //	<jsefler-lt> wottop: previously a subscription with a virt_limit attribute caused two pools to be generated... one for the host and one for the guest (indicated by virt_only=true attribute).  Now I see a third pool with "requires_host" attribute.
 //	 the third pool seems new and good.
 //	<wottop> hosted or standalone?
 //	--- bleanhar_mtg is now known as bleanhar
 //	<jsefler-lt> hosted
 //	<wottop> there should not be a requires host in hosted
 //	<wottop> also the indication is BOTH virt_only and pool_derived
 //	<jsefler-lt> yes - that is what I see here....  curl --insecure --user stage_test_12:redhat --request GET http://rubyvip.web.stage.ext.phx2.redhat.com/clonepin/candlepin/owners/6445999/pools | python -mjson.tool
 //	 wottop: so in my standalone on premise I'll get only the two pools (the old way)?
 //	<wottop> no
 //	 hosted: The creation of the bonus pool happens immediately, It is not tied to a specific host consumer. The count is related to the quantity * virt_limit of the physical pool.
 //	 standalone: a bonus pool is created each time the physical pool is used for an entitlement. The quantity is based on the quantity of that one entitlement * virt_limit. It IS tied to the host consumers id, and only guests of that Id can use them.
 //	 in the latter you might have many pools derived from the original physical pool
 //	<jsefler-lt> GOT IT
 //	<wottop> and revoking the host consumer entitlement will cause the bonus pool to go away
 //	<wottop> jsefler-lt: helpful?
 //	<jsefler-lt> wottop: yes
 //	 wottop: revoking the host consumer entitlement will cause the bonus pool to go away AND ANY ENTITLEMENTS THE GUESTS MAY BE CONSUMING-NO QUESTIONS ASKED?
 //	<wottop> The guest entitlements will get revoked. Yes.
 //	<jsefler-lt> wottop: the curl call above is against the hosted STAGE environment and it is seeing the third pool (with "requires_host" attrib).  So is stage considered standalone?   I didn't think so.
 //	<wottop> jsefler-lt: the default is standalone
 //	<jsefler-lt> wottop: I recall you saying something that a candlpin.conf value needs to be set
 //	<wottop> in master. I cannot comment on the state of STAGE
 //	<wottop> jsefler-lt: There is an entry in candlepin.conf: candlepin.standalone = [true] is the default
 //	<jsefler-lt> wottop: so stage just deployed 0.4.25 and I am seeing the third pool, which means that they are tripping the default "standalone" behavior.  Either the default behavior should not be standalone, or jomara needs to know that a new candlepin.conf value needs to be set.
 //	<wottop> jsefler-lt: it is true in the code by default
 //	 jsefler-lt: you want hosted?
 //	<jsefler-lt> wottop: true in the code by default is fine with me, but then I "think" when jomara deploys candlepin in stage/production, then he needs to set the candlepin.standalone = false.    AM I CORRECT?  I'm just trying to get all on the same page.
 //	<wottop> jsefler-lt: if you want hosted yes. Also, I would advise clearing the DB when switching between modes.
 
 // INSTRUCTIONS FOR BUILDING A XEN KERNEL ON A BEAKER PROVISIONED BOX...
 // https://docspace.corp.redhat.com/people/ndevos/blog/2011/05/26/how-to-quickly-install-a-rhel-5-system-running-xen-and-install-a-guest
 
 
 @Test(groups="VirtualizationTests")
 public class VirtualizationTests extends SubscriptionManagerCLITestScript {
 
 	
 	// Test methods ***********************************************************************
 	
 	@Test(	description="subscription-manager: facts list should report virt.is_guest and virt.host_type and virt.uuid",
 			groups={"AcceptanceTests"}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VirtFactsReportedOnThisClient_Test() {
 		
 		// make sure the original virt-what is in place 
 		RemoteFileTasks.runCommandAndAssert(client, "cp -f "+virtWhatFileBackup+" "+virtWhatFile, 0);
 		String virtWhatStdout = client.runCommandAndWait("virt-what").getStdout().trim();
 		
 		log.info("Running virt-what version: "+client.runCommandAndWait("rpm -q virt-what").getStdout().trim());
 		
 		// virt.is_guest
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(Boolean.valueOf(virtIsGuest),virtWhatStdout.equals("")?Boolean.FALSE:Boolean.TRUE,"subscription-manager facts list reports virt.is_guest as true when virt-what returns stdout.");
 		
 		// virt.host_type
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertEquals(virtHostType,virtWhatStdout.equals("")?"Not Applicable":virtWhatStdout,"subscription-manager facts list reports the same virt.host_type as what is returned by the virt-what installed on the client.");
 		
 		// virt.uuid
 		// dev note: calculation for uuid is done in /usr/share/rhsm/subscription_manager/hwprobe.py def _getVirtUUID(self):
 		String virtUuid = clienttasks.getFactValue("virt.uuid");
 		if (Boolean.parseBoolean(virtIsGuest)) {
 			if (virtHostType.contains("ibm_systemz") || virtHostType.contains("xen-dom0") || virtHostType.contains("powervm")) {
 				Assert.assertEquals(virtUuid,"Unknown","subscription-manager facts list reports virt.uuid as Unknown when the hypervisor contains \"ibm_systemz\", \"xen-dom0\", or \"powervm\".");
 			} else {
 				String expectedUuid = client.runCommandAndWait("if [ -r /system/hypervisor/uuid ]; then cat /system/hypervisor/uuid; else dmidecode -s system-uuid; fi").getStdout().trim().toLowerCase();	// TODO Not sure if the cat /system/hypervisor/uuid is exactly correct
 				Assert.assertEquals(virtUuid,expectedUuid,"subscription-manager facts list reports virt.uuid value to be the /system/hypervisor/uuid or dmidecode -s system-uuid.");
 			}
 		} else {
 			Assert.assertNull(virtUuid,"subscription-manager facts list should NOT report virt.uuid when on a host machine.");		
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list reports the host hypervisor type and uuid on which the guest client is running",
 			dataProvider="getVirtWhatData",
 			groups={}, dependsOnGroups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=70202)
 	public void VirtFactsWhenClientIsAGuest_Test(Object bugzilla, String host_type) {
 		
 		log.info("We will fake out the ability of subscription-manager to read virt-what output on a '"+host_type+"' hypervisor by clobbering virt-what with a fake bash script...");
 		forceVirtWhatToReturnGuest(host_type);
 		
 		// assert virt facts
 		if (host_type.contains("xen-dom0")) {
 			log.warning("A xen-dom0 guest is actually a special case and should be treated by subscription-manager as a host.");
 			assertsForVirtFactsWhenClientIsAHost();
 		} else {
 			assertsForVirtFactsWhenClientIsAGuest(host_type);			
 		}
 	}
 	protected void assertsForVirtFactsWhenClientIsAGuest(String host_type) {
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		// virt.is_guest
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(Boolean.valueOf(virtIsGuest),Boolean.TRUE,"subscription-manager facts list reports virt.is_guest as true when the client is running on a '"+host_type+"' hypervisor.");
 
 		// virt.host_type
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertEquals(virtHostType,host_type,"subscription-manager facts list reports the same virt.host_type value of as returned by "+virtWhatFile);
 
 		// virt.uuid
 		String virtUuid = clienttasks.getFactValue("virt.uuid");
 		if (host_type.contains("ibm_systemz") || host_type.contains("xen-dom0") || host_type.contains("powervm")) {
 			Assert.assertEquals(virtUuid,"Unknown","subscription-manager facts list reports virt.uuid as Unknown when the hypervisor is contains \"ibm_systemz\", \"xen-dom0\", or \"powervm\".");
 		} else {
 			String expectedUuid = client.runCommandAndWait("if [ -r /system/hypervisor/uuid ]; then cat /system/hypervisor/uuid; else dmidecode -s system-uuid; fi").getStdout().trim().toLowerCase();	// TODO Not sure if the cat /system/hypervisor/uuid is exactly correct
 			Assert.assertEquals(virtUuid,expectedUuid,"subscription-manager facts list reports virt.uuid value to be the /system/hypervisor/uuid or dmidecode -s system-uuid.");
 		}
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list reports when the client is running on bare metal",
 			groups={"blockedByBug-726440"}, dependsOnGroups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=70203)
 	public void VirtFactsWhenClientIsAHost_Test() {
 		
 		log.info("We will fake out the ability of subscription-manager to read virt-what output on bare metal by clobbering virt-what with a fake bash script...");
 		forceVirtWhatToReturnHost();
 		
 		// assert virt facts
 		assertsForVirtFactsWhenClientIsAHost();
 	}
 	protected void assertsForVirtFactsWhenClientIsAHost() {
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		// virt.is_guest
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(Boolean.valueOf(virtIsGuest),Boolean.FALSE,"subscription-manager facts list reports virt.is_guest as false when the client is running on bare metal.");
 
 		// virt.host_type
 		String virtWhatStdout = client.runCommandAndWait("virt-what").getStdout().trim();
 		String virtHostType = clienttasks.getFactValue("virt.host_type");
 		if (virtWhatStdout.equals("xen\nxen-dom0")) {
 			Assert.assertEquals(virtHostType,virtWhatStdout,"When virt-what reports xen-dom0, subscription-manager should treat virt.host_type as if it were really bare metal.");			
 		} else {
 			//Assert.assertEquals(virtHostType,"","subscription-manager facts list reports no value for virt.host_type when run on bare metal.");	// valid assertion prior to bug 726440/722248
 			Assert.assertEquals(virtHostType,"Not Applicable","subscription-manager facts list reports 'Not Applicable' for virt.host_type when run on bare metal.");
 		}
 		
 		// virt.uuid
 		String virtUuid = clienttasks.getFactValue("virt.uuid");
 		Assert.assertNull(virtUuid,"subscription-manager facts list should NOT report virt.uuid when run on bare metal.");
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list should not crash on virt facts when virt-what fails",
 			groups={"blockedByBug-668936","blockedByBug-768397"}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VirtFactsWhenVirtWhatFails_Test() {
 		
 		log.info("We will fail virt-what by forcing it to return a non-zero value...");
 		forceVirtWhatToFail();
 		
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		// virt.is_guest
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(virtIsGuest,"Unknown","subscription-manager facts list reports virt.is_guest as Unknown when the hypervisor is undeterminable (virt-what fails).");
 
 		// virt.host_type
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertNull(virtHostType,"subscription-manager facts list should NOT report a virt.host_type when the hypervisor is undeterminable (virt-what fails).");
 		
 		// virt.uuid
 		String virtUuid = clienttasks.getFactValue("virt.uuid");
 		Assert.assertEquals(virtUuid,"Unknown","subscription-manager facts list reports virt.uuid as Unknown when the hypervisor is undeterminable (virt-what fails).");
 	}
 	
 	
 	@Test(	description="subscription-manager: facts list should report is_guest and uuid as Unknown when virt-what is not installed",
 			groups={"blockedByBug-768397"}, dependsOnGroups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VirtFactsWhenVirtWhatIsNotInstalled_Test() {
 		
 		log.info("We will remove virt-what for this test...");
 		
 		RemoteFileTasks.runCommandAndWait(client,"rm -f "+virtWhatFile, TestRecords.action());
 		log.info("Now let's run the subscription-manager facts --list and assert the results...");
 		
 		// virt.is_guest
 		String virtIsGuest = clienttasks.getFactValue("virt.is_guest");
 		Assert.assertEquals(virtIsGuest,"Unknown","subscription-manager facts list reports virt.is_guest as Unknown when virt-what in not installed.");
 		
 		// virt.host_type
 		String virtHostType = clienttasks.getFactValue("virt.host_type");	
 		Assert.assertNull(virtHostType,"subscription-manager facts list should NOT report a virt.host_type when virt-what in not installed.");
 		
 		// virt.uuid
 		String virtUuid = clienttasks.getFactValue("virt.uuid");
 		Assert.assertEquals(virtUuid,"Unknown","subscription-manager facts list reports virt.uuid as Unknown when virt-what in not installed.");
 
 	}
 	
 	
 	
 	
 	
 	
 	
 	@Test(	description="Verify host and guest pools are generated from a virtualization-aware subscription.",
 			groups={"AcceptanceTests"/*,"blockedByBug-750279"*/},
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostAndGuestPoolsAreGeneratedForVirtualizationAwareSubscription_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 
 //		log.info("When an owner has purchased a virtualization-aware subscription ("+productName+"; subscriptionId="+subscriptionId+"), he should have subscription access to two pools: one for the host and one for the guest.");
 //
 //		// assert that there are two (one for the host and one for the guest)
 //		log.info("Using the RESTful Candlepin API, let's find all the pools generated from subscription id: "+subscriptionId);
 //		List<String> poolIds = CandlepinTasks.getPoolIdsForSubscriptionId(sm_clientUsername,sm_clientPassword,sm_serverUrl,ownerKey,subscriptionId);
 //		Assert.assertEquals(poolIds.size(), 2, "Exactly two pools should be derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 //
 //		// assert that one pool is for the host and the other is for the guest
 //		guestPoolId = null;
 //		hostPoolId = null;
 //		for (String poolId : poolIds) {
 //			if (CandlepinTasks.isPoolVirtOnly (sm_clientUsername,sm_clientPassword,poolId,sm_serverUrl)) {
 //				guestPoolId = poolId;
 //			} else {
 //				hostPoolId = poolId;
 //			}
 //		}
 //		Assert.assertNotNull(guestPoolId, "Found the guest pool id ("+guestPoolId+") with an attribute of virt_only=true");
 //		Assert.assertNotNull(hostPoolId, "Found the host pool id ("+hostPoolId+") without an attribute of virt_only=true");	
 		
 // WHEN candlepin.conf candlepin.standalone = true (IF NOT SPECIFIED, DEFAULTS TO true)
 // THE FOLLOWING THREE POOLS SHOULD NEVER OCCUR SINCE ONLY candlepin.standalone SHOULD NOT BE SWITCHED BETWEEN TRUE/FALSE
 //		[root@intel-s3ea2-04 ~]# curl --insecure --user stage_test_12:redhat --request GET http://rubyvip.web.stage.ext.phx2.redhat.com/clonepin/candlepin/owners/6445999/pools | python -mjson.tool
 //			  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
 //			                                 Dload  Upload   Total   Spent    Left  Speed
 //			100  8044    0  8044    0     0   8856      0 --:--:-- --:--:-- --:--:--  9980
 //			[
 //			    {
 //			        "accountNumber": "1508113", 
 //			        "activeSubscription": true, 
 //			        "attributes": [
 //			            {
 //			                "created": "2011-10-30T05:06:50.000+0000", 
 //			                "id": "8a99f9813350d60e0133533919f512f9", 
 //			                "name": "requires_consumer_type", 
 //			                "updated": "2011-10-30T05:06:50.000+0000", 
 //			                "value": "system"
 //			            }, 
 //			            {
 //			                "created": "2011-10-30T05:06:50.000+0000", 
 //			                "id": "8a99f9813350d60e0133533919f512fa", 
 //			                "name": "requires_host", 
 //			                "updated": "2011-10-30T05:06:50.000+0000", 
 //			                "value": "c6ec101c-2c6a-4f5d-9161-ac335d309d0e"
 //			            }, 
 //			            {
 //			                "created": "2011-10-30T05:06:50.000+0000", 
 //			                "id": "8a99f9813350d60e0133533919f512fc", 
 //			                "name": "pool_derived", 
 //			                "updated": "2011-10-30T05:06:50.000+0000", 
 //			                "value": "true"
 //			            }, 
 //			            {
 //			                "created": "2011-10-30T05:06:50.000+0000", 
 //			                "id": "8a99f9813350d60e0133533919f512fb", 
 //			                "name": "virt_only", 
 //			                "updated": "2011-10-30T05:06:50.000+0000", 
 //			                "value": "true"
 //			            }
 //			        ], 
 //			        "consumed": 0, 
 //			        "contractNumber": "2635037", 
 //			        "created": "2011-10-30T05:06:50.000+0000", 
 //			        "endDate": "2012-10-19T03:59:59.000+0000", 
 //			        "href": "/pools/8a99f9813350d60e0133533919f512f8", 
 //			        "id": "8a99f9813350d60e0133533919f512f8", 
 //			        "owner": {
 //			            "displayName": "6445999", 
 //			            "href": "/owners/6445999", 
 //			            "id": "8a85f98432e7376c013302c3a9745c68", 
 //			            "key": "6445999"
 //			        }, 
 //			        "productAttributes": [], 
 //			        "productId": "RH0103708", 
 //			        "productName": "Red Hat Enterprise Linux Server, Premium (8 sockets) (Up to 4 guests)", 
 //			        "providedProducts": [
 //			            {
 //			                "created": "2011-10-30T05:06:50.000+0000", 
 //			                "id": "8a99f9813350d60e0133533919f512fd", 
 //			                "productId": "69", 
 //			                "productName": "Red Hat Enterprise Linux Server", 
 //			                "updated": "2011-10-30T05:06:50.000+0000"
 //			            }
 //			        ], 
 //			        "quantity": 4, 
 //			        "restrictedToUsername": null, 
 //			        "sourceEntitlement": {
 //			            "href": "/entitlements/8a99f9813350d60e0133533919f512fe", 
 //			            "id": "8a99f9813350d60e0133533919f512fe"
 //			        }, 
 //			        "startDate": "2011-10-19T04:00:00.000+0000", 
 //			        "subscriptionId": "2272904", 
 //			        "updated": "2011-10-30T05:06:50.000+0000"
 //			    }, 
 //			    {
 //			        "accountNumber": "1508113", 
 //			        "activeSubscription": true, 
 //			        "attributes": [], 
 //			        "consumed": 3, 
 //			        "contractNumber": "2635037", 
 //			        "created": "2011-10-19T19:05:09.000+0000", 
 //			        "endDate": "2012-10-19T03:59:59.000+0000", 
 //			        "href": "/pools/8a99f98233137a9701331d92a4301203", 
 //			        "id": "8a99f98233137a9701331d92a4301203", 
 //			        "owner": {
 //			            "displayName": "6445999", 
 //			            "href": "/owners/6445999", 
 //			            "id": "8a85f98432e7376c013302c3a9745c68", 
 //			            "key": "6445999"
 //			        }, 
 //			        "productAttributes": [
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4301204", 
 //			                "name": "support_type", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "L1-L3"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4301205", 
 //			                "name": "sockets", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "8"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4301206", 
 //			                "name": "virt_limit", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "4"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4301207", 
 //			                "name": "name", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Red Hat Enterprise Linux Server, Premium (8 sockets) (Up to 4 guests)"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4301208", 
 //			                "name": "type", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "MKT"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4301209", 
 //			                "name": "description", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Red Hat Enterprise Linux"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a430120b", 
 //			                "name": "product_family", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Red Hat Enterprise Linux"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a430120a", 
 //			                "name": "option_code", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "1"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a430120c", 
 //			                "name": "variant", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Physical Servers"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a430120d", 
 //			                "name": "support_level", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "PREMIUM"
 //			            }
 //			        ], 
 //			        "productId": "RH0103708", 
 //			        "productName": "Red Hat Enterprise Linux Server, Premium (8 sockets) (Up to 4 guests)", 
 //			        "providedProducts": [
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a430120e", 
 //			                "productId": "69", 
 //			                "productName": "Red Hat Enterprise Linux Server", 
 //			                "updated": "2011-10-19T19:05:09.000+0000"
 //			            }
 //			        ], 
 //			        "quantity": 100, 
 //			        "restrictedToUsername": null, 
 //			        "sourceEntitlement": null, 
 //			        "startDate": "2011-10-19T04:00:00.000+0000", 
 //			        "subscriptionId": "2272904", 
 //			        "updated": "2011-10-19T19:05:09.000+0000"
 //			    }, 
 //			    {
 //			        "accountNumber": "1508113", 
 //			        "activeSubscription": true, 
 //			        "attributes": [
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4461210", 
 //			                "name": "requires_consumer_type", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "system"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4461211", 
 //			                "name": "virt_limit", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "0"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471213", 
 //			                "name": "pool_derived", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "true"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471212", 
 //			                "name": "virt_only", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "true"
 //			            }
 //			        ], 
 //			        "consumed": 3, 
 //			        "contractNumber": "2635037", 
 //			        "created": "2011-10-19T19:05:09.000+0000", 
 //			        "endDate": "2012-10-19T03:59:59.000+0000", 
 //			        "href": "/pools/8a99f98233137a9701331d92a446120f", 
 //			        "id": "8a99f98233137a9701331d92a446120f", 
 //			        "owner": {
 //			            "displayName": "6445999", 
 //			            "href": "/owners/6445999", 
 //			            "id": "8a85f98432e7376c013302c3a9745c68", 
 //			            "key": "6445999"
 //			        }, 
 //			        "productAttributes": [
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471214", 
 //			                "name": "support_type", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "L1-L3"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471215", 
 //			                "name": "sockets", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "8"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471216", 
 //			                "name": "virt_limit", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "4"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471217", 
 //			                "name": "name", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Red Hat Enterprise Linux Server, Premium (8 sockets) (Up to 4 guests)"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471218", 
 //			                "name": "type", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "MKT"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a4471219", 
 //			                "name": "description", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Red Hat Enterprise Linux"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a447121b", 
 //			                "name": "product_family", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Red Hat Enterprise Linux"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a447121a", 
 //			                "name": "option_code", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "1"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a447121c", 
 //			                "name": "variant", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "Physical Servers"
 //			            }, 
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a447121d", 
 //			                "name": "support_level", 
 //			                "productId": "RH0103708", 
 //			                "updated": "2011-10-19T19:05:09.000+0000", 
 //			                "value": "PREMIUM"
 //			            }
 //			        ], 
 //			        "productId": "RH0103708", 
 //			        "productName": "Red Hat Enterprise Linux Server, Premium (8 sockets) (Up to 4 guests)", 
 //			        "providedProducts": [
 //			            {
 //			                "created": "2011-10-19T19:05:09.000+0000", 
 //			                "id": "8a99f98233137a9701331d92a447121e", 
 //			                "productId": "69", 
 //			                "productName": "Red Hat Enterprise Linux Server", 
 //			                "updated": "2011-10-19T19:05:09.000+0000"
 //			            }
 //			        ], 
 //			        "quantity": 400, 
 //			        "restrictedToUsername": null, 
 //			        "sourceEntitlement": null, 
 //			        "startDate": "2011-10-19T04:00:00.000+0000", 
 //			        "subscriptionId": "2272904", 
 //			        "updated": "2011-10-19T19:05:09.000+0000"
 //			    }
 //			]
 
 		log.info("When a hosted owner has purchased a virtualization-aware subscription ("+productName+"; subscriptionId="+subscriptionId+"), he should have subscription access to two pools: one for the host and one for the guest.");
 
 		// assert that there are two (one for the host and one for the guest)
 		log.info("Using the RESTful Candlepin API, let's find all the pools generated from subscription id: "+subscriptionId);
 		List<JSONObject> jsonPools = CandlepinTasks.getPoolsForSubscriptionId(sm_clientUsername,sm_clientPassword,sm_serverUrl,ownerKey,subscriptionId);
 
 		if (!servertasks.statusStandalone) {
 			Assert.assertEquals(jsonPools.size(), 2, "When the candlepin.standalone is false, exactly two pools should be generated from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").  (one with no attributes, one with virt_only and pool_derived true)");	
 		} else {
 			// Note: this line of code should not be reached since this test should not be run when servertasks.statusStandalone is true
 			Assert.assertTrue(jsonPools.size()>=1, "When the candlepin.standalone is true, one or more pools (actual='"+jsonPools.size()+"') should be generated from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").  (one with no attributes, the rest with virt_only pool_derived equals true and requires_host)");			
 		}
 
 		// assert that one pool is for the host and the other is for the guest
 		guestPoolId = null;
 		hostPoolId = null;
 		for (JSONObject jsonPool : jsonPools) {
 			String poolId = jsonPool.getString("id");
 //			JSONArray attributes = jsonPool.getJSONArray("attributes");
 //			if (attributes.length()==0) {
 //				hostPoolId = poolId;
 //				continue;
 //			}
 			
 			if (Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "virt_only")) &&
 				Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "pool_derived")) &&
 				CandlepinTasks.getPoolAttributeValue(jsonPool, "requires_host")==null) {
 				guestPoolId = poolId;
 			} else if (
 				Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "virt_only")) &&
 				Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "pool_derived")) &&
 				CandlepinTasks.getPoolAttributeValue(jsonPool, "requires_host")!=null) {
 				//newGuestPoolId = poolId;  // TODO THIS IS THE NEW VIRT-AWARE MODEL FOR WHICH NEW TESTS SHOULD BE AUTOMATED
 			} else if (
 				CandlepinTasks.getPoolAttributeValue(jsonPool, "virt_only")==null &&	// TODO or false?
 				CandlepinTasks.getPoolAttributeValue(jsonPool, "pool_derived")==null &&	// TODO or false?
 				CandlepinTasks.getPoolAttributeValue(jsonPool, "requires_host")==null) {
 				hostPoolId = poolId;
 			}
 		}
 		Assert.assertNotNull(guestPoolId, "Found the virt_only/pool_derived guest pool id ("+guestPoolId+") without an attribute of requires_host");
 		Assert.assertNotNull(hostPoolId, "Found the host pool id ("+hostPoolId+")");	
 	}
 	
 	
 	@Test(	description="Verify host and guest pools quantities generated from a virtualization-aware subscription",
 			groups={"AcceptanceTests"}, // "blockedByBug-679617" indirectly when this script is run as part of the full TestNG suite since this is influenced by other scripts calling refresh pools
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostAndGuestPoolQuantities_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
 		// trick this system into believing it is a virt guest
 		forceVirtWhatToReturnGuest("kvm");
 		
 		// get the hostPool
 		List<SubscriptionPool> allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, allAvailablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+hostPool);
 
 		// assert hostPoolId quantity
 		JSONObject jsonHostPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+hostPool.poolId));	
 		int hostPoolQuantityConsumed = jsonHostPool.getInt("consumed");
 		Assert.assertEquals(Integer.valueOf(hostPool.quantity), Integer.valueOf(quantity-hostPoolQuantityConsumed), "Assuming '"+hostPoolQuantityConsumed+"' entitlements are currently being consumed from this host pool '"+hostPool.poolId+"', the quantity of available entitlements should be '"+quantity+"' minus '"+hostPoolQuantityConsumed+"'.");
 		
 		// get the guestPool
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, allAvailablePools);
 		Assert.assertNotNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+guestPool);
 
 		// assert guestPoolId quantity
 		JSONObject jsonGuestPool = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/pools/"+guestPool.poolId));	
 		int guestPoolQuantityConsumed = jsonGuestPool.getInt("consumed");
 		if (virtLimit.equals("unlimited")) {
 			Assert.assertEquals(guestPool.quantity, virtLimit, "When the subscription product has a virt_limit of 'unlimited', then the guest pool's quantity should be 'unlimited'.");
 		} else {
			Assert.assertEquals(Integer.valueOf(guestPool.quantity), Integer.valueOf(quantity*Integer.valueOf(virtLimit)-guestPoolQuantityConsumed), "Assuming '"+guestPoolQuantityConsumed+"' entitlements are currently being consumed from this guest pool '"+guestPool.poolId+"', the quantity of available entitlements should be the virt_limit of '"+virtLimit+"' times the host quantity '"+quantity+"' minus '"+guestPoolQuantityConsumed+"'.");
 		}
 	}
 		
 	
 	
 	@Test(	description="Verify the virt_limit multiplier on guest pool quantity is not clobbered by refresh pools",
 			groups={"blockedByBug-679617"},
 			dependsOnGroups={},
 			dependsOnMethods={"VerifyHostAndGuestPoolQuantities_Test"},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyGuestPoolQuantityIsNotClobberedByRefreshPools_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 		if (dbConnection==null) throw new SkipException("This testcase requires a connection to the candlepin database so that it can updateSubscriptionDatesOnDatabase.");
 
 		// get the hostPool
 		List<SubscriptionPool> allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, allAvailablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+hostPool);
 
 		// remember the hostPool quantity before calling refresh pools
 		String hostPoolQuantityBefore = hostPool.quantity;
 		
 		// get the guestPool
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, allAvailablePools);
 		Assert.assertNotNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is listed in all available subscriptions: "+guestPool);
 
 		// remember the hostPool quantity before calling refresh pools
 		String guestPoolQuantityBefore = guestPool.quantity;
 
 		log.info("Now let's modify the start date of the virtualization-aware subscription id '"+subscriptionId+"'...");
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/subscriptions"));	
 		JSONObject jsonSubscription = null;
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			if (jsonSubscription.getString("id").equals(subscriptionId)) {break;} else {jsonSubscription=null;}
 		}
 		Calendar startDate = parseISO8601DateString(jsonSubscription.getString("startDate"),"GMT");	// "startDate":"2012-02-08T00:00:00.000+0000"
 		Calendar newStartDate = (Calendar) startDate.clone(); newStartDate.add(Calendar.MONTH, -1);	// subtract a month
 		updateSubscriptionDatesOnDatabase(subscriptionId,newStartDate,null);
 
 		log.info("Now let's refresh the subscription pools...");
 		JSONObject jobDetail = CandlepinTasks.refreshPoolsUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,ownerKey);
 		jobDetail = CandlepinTasks.waitForJobDetailStateUsingRESTfulAPI(sm_serverAdminUsername,sm_serverAdminPassword,sm_serverUrl,jobDetail,"FINISHED", 10*1000, 3);
 		allAvailablePools = clienttasks.getCurrentlyAllAvailableSubscriptionPools();
 
 		// retrieve the host pool again and assert the quantity has not changed
 		hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, allAvailablePools);
 		Assert.assertEquals(hostPool.quantity, hostPoolQuantityBefore, "The quantity of entitlements available from the host pool has NOT changed after refreshing pools.");
 		
 		// retrieve the guest pool again and assert the quantity has not changed
 		guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, allAvailablePools);
 		Assert.assertEquals(guestPool.quantity, guestPoolQuantityBefore, "The quantity of entitlements available from the guest pool has NOT changed after refreshing pools.");
 	}
 	
 	
 	@Test(	description="Verify host and guest pools to a virtualization-aware subscription are subscribable on a guest system.",
 			groups={},
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostAndGuestPoolsAreSubscribableOnGuestSystem_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
 		// trick this system into believing it is a virt guest
 		forceVirtWhatToReturnGuest("kvm");
 		
 		// assert that the hostPoolId is available
 		List<SubscriptionPool> availablePools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, availablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is available on a guest system: "+hostPool);
 		
 		// attempt to subscribe to the hostPoolId
 		clienttasks.subscribeToSubscriptionPool(hostPool);
 		
 		// assert that the guestPoolId is available
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, availablePools);
 		Assert.assertNotNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is available on a guest system: "+guestPool);
 
 		// attempt to subscribe to the guestPoolId
 		clienttasks.subscribeToSubscriptionPool(guestPool);
 	}
 	
 	
 	@Test(	description="Verify only the derived host pool from a virtualization-aware subscription is subscribable on a host system.  The guest pool should not be available nor subscribable.",
 			groups={},
 			dependsOnGroups={},
 			dataProvider="getVirtSubscriptionData",
 			enabled=true)
 	public void VerifyHostPoolIsSubscribableOnHostSystemWhileGuestPoolIsNot_Test(String subscriptionId, String productName, String productId, int quantity, String virtLimit, String hostPoolId, String guestPoolId) throws JSONException, Exception {
 		if (hostPoolId==null && guestPoolId==null) throw new SkipException("Failed to find expected host and guest pools derived from virtualization-aware subscription id '"+subscriptionId+"' ("+productName+").");
 
 		// trick this system into believing it is a host
 		forceVirtWhatToReturnHost();
 		
 		// assert that the hostPoolId is available
 		List<SubscriptionPool> availablePools = clienttasks.getCurrentlyAvailableSubscriptionPools();
 		SubscriptionPool hostPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", hostPoolId, availablePools);
 		Assert.assertNotNull(hostPool,"A host pool derived from the virtualization-aware subscription id '"+subscriptionId+"' is available on a host system: "+hostPool);
 
 		// assert that the guestPoolId is NOT available
 		SubscriptionPool guestPool = SubscriptionPool.findFirstInstanceWithMatchingFieldFromList("poolId", guestPoolId, availablePools);
 		Assert.assertNull(guestPool,"A guest pool derived from the virtualization-aware subscription id '"+subscriptionId+"' should NOT be available on a host system: "+guestPool);
 
 		// attempt to subscribe to the hostPoolId
 		clienttasks.subscribeToSubscriptionPool(hostPool);
 
 		// attempt to subscribe to the guestPoolId (should be blocked)
 		SSHCommandResult result = clienttasks.subscribe(null,null,guestPoolId,null,null,null,null,null, null, null, null);
 		// Unable to entitle consumer to the pool with id '8a90f8b42e3e7f2e012e3e7fc653013e'.: rulefailed.virt.only
 		//Assert.assertContainsMatch(result.getStdout(), "^Unable to entitle consumer to the pool with id '"+guestPoolId+"'.:");
 		// RHEL58: Pool is restricted to virtual guests: '8a90f85734205a010134205ae8d80403'.
 		Assert.assertEquals(result.getStdout().trim(), "Pool is restricted to virtual guests: '"+guestPoolId+"'.");
 
 	}
 	
 	
 	@Test(	description="Verify the subscription-manager list --avail appropriately displays pools with MachineType: virtual",
 			groups={},
 			dependsOnGroups={},
 			enabled=true)
 	public void VerifyVirtualMachineTypeIsReportedInListAvailablePools_Test() throws JSONException, Exception {
 
 		// trick this system into believing it is a virt guest
 		forceVirtWhatToReturnGuest("kvm");
 		
 		boolean poolFound = false;
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			if (CandlepinTasks.isPoolVirtOnly (sm_clientUsername,sm_clientPassword,pool.poolId,sm_serverUrl)) {
 				Assert.assertEquals(pool.machineType, "virtual", "MachineType:virtual should be displayed in the available Subscription Pool listing when the pool has a virt_only=true attribute.  Pool: "+pool);
 				poolFound = true;
 			} else {
 				//Assert.assertEquals(pool.machineType, "physical", "MachineType:physical should be displayed in the available Subscription Pool listing when the pool has a virt_only=false attribute (or absense of a virt_only attribute).  Pool: "+pool);
 			}
 		}
 		if (!poolFound) throw new SkipException("Could not find an available pool with which to verify the MachineType:virtual is reported in the Subscription Pool listing.");
 	}
 	
 	@Test(	description="Verify the subscription-manager list --avail appropriately displays pools with MachineType: physical",
 			groups={},
 			dependsOnGroups={},
 			enabled=true)
 	public void VerifyPhysicalMachineTypeValuesInListAvailablePools_Test() throws JSONException, Exception {
 
 		// trick this system into believing it is a host
 		forceVirtWhatToReturnHost();
 		
 		boolean poolFound = false;
 		for (SubscriptionPool pool : clienttasks.getCurrentlyAvailableSubscriptionPools()) {
 			if (CandlepinTasks.isPoolVirtOnly (sm_clientUsername,sm_clientPassword,pool.poolId,sm_serverUrl)) {
 				//Assert.assertEquals(pool.machineType, "virtual", "MachineType:virtual should be displayed in the available Subscription Pool listing when the pool has a virt_only=true attribute.  Pool: "+pool);
 			} else {
 				Assert.assertEquals(pool.machineType, "physical", "MachineType:physical should be displayed in the available Subscription Pool listing when the pool has a virt_only=false attribute (or absense of a virt_only attribute).  Pool: "+pool);
 				poolFound = true;
 			}
 		}
 		if (!poolFound) throw new SkipException("Could not find an available pool with which to verify the MachineType:physical is reported in the Subscription Pool listing.");
 	}
 	
 	
 	@Test(	description="Verify the Candlepin API accepts PUTting of guestIds onto host consumer (to be used by virt-who)",
 			groups={"blockedByBug-737935"},
 			dependsOnGroups={},
 			enabled=true)
 	public void VerifyGuestIdsCanBePutOntoHostConsumer_Test() throws JSONException, Exception {
 
 		int k=1; JSONObject jsonConsumer; List<String> actualGuestIds = new ArrayList<String>(){};
 		
 		// trick this system into believing it is a host
 		forceVirtWhatToReturnHost();
 		
 		// create host consumer A
 		String consumerIdOfHostA = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, null, null, null, null));
 		
 		for (int c=0;c<2;c++) { // run this test twice
 			
 			// call Candlepin API to PUT some guestIds onto the host consumer A
 			JSONObject jsonData = new JSONObject();
 			List<String> expectedGuestIdsOnHostA = Arrays.asList(new String[]{"test-guestId"+k++,"test-guestId"+k++}); 
 			jsonData.put("guestIds", expectedGuestIdsOnHostA);
 			CandlepinTasks.putResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostA, jsonData);
 			
 			// get the host consumer and assert that it has all the guestIds just PUT
 			jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostA));
 			// actual guestIds
 			//DEBUGGING jsonConsumer.put("guestIds", new JSONArray(expectedGuestIdsOnHostA));
 			actualGuestIds.clear();
 			for (int g=0; g<jsonConsumer.getJSONArray("guestIds").length(); g++) {
 				actualGuestIds.add(jsonConsumer.getJSONArray("guestIds").getJSONObject(g).getString("guestId"));
 			}
 			// assert expected guestIds
 			for (String guestId : expectedGuestIdsOnHostA) Assert.assertContains(actualGuestIds, guestId);
 			Assert.assertEquals(actualGuestIds.size(), expectedGuestIdsOnHostA.size(),"All of the expected guestIds PUT on host consumer '"+consumerIdOfHostA+"' using the Candlepin API were verified.");
 
 		
 			
 			// Now let's create a second host consumer B and add its own guestIds to it and assert the same test
 			clienttasks.clean(null, null, null);	// this will keep consumer A registered
 			String consumerIdOfHostB = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, null, null, null, null));
 
 			// call Candlepin API to PUT some guestIds onto the host consumer B
 			List<String> expectedGuestIdsOnHostB = Arrays.asList(new String[]{"test-guestId"+k++,"test-guestId"+k++,"test-guestId"+k++,"test-guestId"+k++}); 
 			jsonData.put("guestIds", expectedGuestIdsOnHostB);
 			CandlepinTasks.putResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostB, jsonData);
 
 			// get the host consumer and assert that it has all the guestIds just PUT
 			jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostB));
 			// actual guestIds
 			//DEBUGGING jsonConsumer.put("guestIds", new JSONArray(expectedGuestIdsOnHostB));
 			actualGuestIds.clear();
 			//[root@jsefler-stage-6server ~]# curl --insecure --user testuser1:password --request GET https://jsefler-f14-5candlepin.usersys.redhat.com:8443/candlepin/consumers/8b7fe5e5-7178-4bad-b686-2ff8c6c19112 | python -msimplejson/tool
 			//  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
 			//                                 Dload  Upload   Total   Spent    Left  Speed
 			//100 14242    0 14242    0     0  76993      0 --:--:-- --:--:-- --:--:--  135k
 			//{
 			//<cut>
 			//    "guestIds": [
 			//        {
 			//            "created": "2011-11-23T18:01:15.325+0000", 
 			//            "guestId": "test-guestId2", 
 			//            "id": "8a90f85733cefc4c0133d196b73d6d26", 
 			//            "updated": "2011-11-23T18:01:15.325+0000"
 			//        }, 
 			//        {
 			//            "created": "2011-11-23T18:01:15.293+0000", 
 			//            "guestId": "test-guestId1", 
 			//            "id": "8a90f85733cefc4c0133d196b71d6d23", 
 			//            "updated": "2011-11-23T18:01:15.293+0000"
 			//        }
 			//    ], 
 			//<cut>
 			//    "username": "testuser1", 
 			//    "uuid": "8b7fe5e5-7178-4bad-b686-2ff8c6c19112"
 			//}
 			for (int g=0; g<jsonConsumer.getJSONArray("guestIds").length(); g++) {
 				actualGuestIds.add(jsonConsumer.getJSONArray("guestIds").getJSONObject(g).getString("guestId"));
 			}
 			// assert expected guestIds
 			for (String guestId : expectedGuestIdsOnHostB) Assert.assertContains(actualGuestIds, guestId);
 			Assert.assertEquals(actualGuestIds.size(), expectedGuestIdsOnHostB.size(),"All of the expected guestIds PUT on consumer '"+consumerIdOfHostB+"' using the Candlepin API were verified.");
 
 			
 			
 			// Now let's re-verify that the guestIds of host consumer A have not changed
 			// get the host consumer and assert that it has all the guestIds just PUT
 			jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostA));
 			// actual guestIds
 			//DEBUGGING jsonConsumer.put("guestIds", new JSONArray(expectedGuestIdsOnHostA));
 			actualGuestIds.clear();
 			for (int g=0; g<jsonConsumer.getJSONArray("guestIds").length(); g++) {
 				actualGuestIds.add(jsonConsumer.getJSONArray("guestIds").getJSONObject(g).getString("guestId"));
 			}
 			// assert expected guestIds
 			for (String guestId : expectedGuestIdsOnHostA) Assert.assertContains(actualGuestIds, guestId);
 			Assert.assertEquals(actualGuestIds.size(), expectedGuestIdsOnHostA.size(),"All of the expected guestIds PUT on consumer '"+consumerIdOfHostA+"' using the Candlepin API were verified.");
 
 		}
 	}
 	
 	
 	@Test(	description="Verify the Candlepin API denies PUTting of guestIds onto a guest consumer",
 			groups={"blockedByBug-737935"},
 			dependsOnGroups={},
 			enabled=true)
 	public void VerifyGuestIdsCanNOTBePutOntoGuestConsumer_Test() throws JSONException, Exception {
 
 		int k=1; JSONObject jsonConsumer; List<String> actualGuestIds = new ArrayList<String>(){};
 		
 		// trick this system into believing it is a guest
 		forceVirtWhatToReturnGuest("kvm");
 		
 		// create a guest consumer
 		String consumerIdOfGuest = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, null, null, null, null));
 			
 		// call Candlepin API to PUT some guestIds onto the guest consumer
 		JSONObject jsonData = new JSONObject();
 		List<String> expectedGuestIds = Arrays.asList(new String[]{"test-guestId"+k++,"test-guestId"+k++}); 
 		jsonData.put("guestIds", expectedGuestIds);
 		String result = CandlepinTasks.putResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfGuest, jsonData);
 		
 		// assert that ^ PUT request failed
 		// TODO assert the result
 		
 		// get the consumer and assert that it has None of the guestIds just PUT
 		jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfGuest));
 		// actual guestIds
 		//DEBUGGING jsonConsumer.put("guestIds", new JSONArray(expectedGuestIds));
 		actualGuestIds.clear();
 		for (int g=0; g<jsonConsumer.getJSONArray("guestIds").length(); g++) {
 			actualGuestIds.add(jsonConsumer.getJSONArray("guestIds").getJSONObject(g).getString("guestId"));
 		}
 		log.info("Consumer '"+consumerIdOfGuest+"' guestIds: "+actualGuestIds);
 		// assert expected guestIds are empty (TODO or NULL?)
 		if (actualGuestIds.size()>0) {throw new SkipException("This testcase is effectively a simulation of virt-who running on a guest and reporting that the guest has guests of its own. This is NOT a realistic scenario and Candlepin is currently not programmed to block this PUT.  No bugzilla has been opened.  Skipping this test until needed in the future.");};
 		Assert.assertEquals(actualGuestIds, new ArrayList<String>(){},"A guest '"+consumerIdOfGuest+"' consumer should not be allowed to have guestIds PUT on it using the Candlepin API.");
 	}
 	
 	
 	@Test(	description="When Host B PUTs the same guestId as HostA, the guestId should be removed from HostA (simulation of a guest moved from Host A to Host B)",
 			groups={"blockedByBug-737935"},
 			dependsOnGroups={},
 			enabled=true)
 	public void VerifyGuestIdIsRemovedFromHostConsumerAWhenHostConsumerBPutsSameGuestId_Test() throws JSONException, Exception {
 
 		int k=1; JSONObject jsonConsumer; List<String> actualGuestIds = new ArrayList<String>(){};
 		
 		// trick this system into believing it is a host
 		forceVirtWhatToReturnHost();
 		
 		// create host consumer A
 		String consumerIdOfHostA = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, true, null, null, null, null));
 		
 		for (int c=0;c<2;c++) { // run this test twice
 			
 			// call Candlepin API to PUT some guestIds onto the host consumer A
 			JSONObject jsonData = new JSONObject();
 			ArrayList<String> expectedGuestIdsOnHostA = new ArrayList<String>(){};
 			for (String guestId :  Arrays.asList(new String[]{"test-guestId"+k++,"test-guestId"+k++})) expectedGuestIdsOnHostA.add(guestId);
 
 			jsonData.put("guestIds", expectedGuestIdsOnHostA);
 			CandlepinTasks.putResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostA, jsonData);
 			
 			// get the host consumer and assert that it has all the guestIds just PUT
 			jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostA));
 			// actual guestIds
 			//DEBUGGING jsonConsumer.put("guestIds", new JSONArray(expectedGuestIdsOnHostA));
 			actualGuestIds.clear();
 			for (int g=0; g<jsonConsumer.getJSONArray("guestIds").length(); g++) {
 				actualGuestIds.add(jsonConsumer.getJSONArray("guestIds").getJSONObject(g).getString("guestId"));
 			}
 			// assert expected guestIds
 			for (String guestId : expectedGuestIdsOnHostA) Assert.assertContains(actualGuestIds, guestId);
 			Assert.assertEquals(actualGuestIds.size(), expectedGuestIdsOnHostA.size(),"All of the expected guestIds PUT on host consumer '"+consumerIdOfHostA+"' using the Candlepin API were verified.");
 
 		
 			
 			// Now let's create a second host consumer B and add its own guestIds to it and assert the same test
 			clienttasks.clean(null, null, null);	// this will keep consumer A registered
 			String consumerIdOfHostB = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, null, null, null, null));
 
 			// call Candlepin API to PUT some guestIds onto the host consumer B
 			// NOTE: decrementing k will effectively move the last guestId from HostA to HostB
 			k--;
 			log.info("Simulating the moving of guestId '"+expectedGuestIdsOnHostA.get(expectedGuestIdsOnHostA.size()-1)+"' from host consumer A to host consumer B by PUTting it on the list of guestIds for host consumer B...");
 			List<String> expectedGuestIdsOnHostB = Arrays.asList(new String[]{"test-guestId"+k++,"test-guestId"+k++,"test-guestId"+k++,"test-guestId"+k++}); 
 			jsonData.put("guestIds", expectedGuestIdsOnHostB);
 			CandlepinTasks.putResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostB, jsonData);
 
 			// get the host consumer and assert that it has all the guestIds just PUT
 			jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostB));
 			// actual guestIds
 			//DEBUGGING jsonConsumer.put("guestIds", new JSONArray(expectedGuestIdsOnHostB));
 			actualGuestIds.clear();
 			for (int g=0; g<jsonConsumer.getJSONArray("guestIds").length(); g++) {
 				actualGuestIds.add(jsonConsumer.getJSONArray("guestIds").getJSONObject(g).getString("guestId"));
 			}
 			// assert expected guestIds
 			for (String guestId : expectedGuestIdsOnHostB) Assert.assertContains(actualGuestIds, guestId);
 			Assert.assertEquals(actualGuestIds.size(), expectedGuestIdsOnHostB.size(),"All of the expected guestIds PUT on consumer '"+consumerIdOfHostB+"' using the Candlepin API were verified.");
 
 			
 			
 			// Now let's re-verify that the guestIds of host consumer A have not changed
 			// NOTE: The last guestId SHOULD BE REMOVED since it was most recently reported as a guest on HostB
 			log.info("Because guestId '"+expectedGuestIdsOnHostA.get(expectedGuestIdsOnHostA.size()-1)+"' was most recently reported as a guest on host consumer B, it should no longer be on the list of guestIds for host consumer A...");
 			expectedGuestIdsOnHostA.remove(expectedGuestIdsOnHostA.size()-1);
 
 			// get the host consumer and assert that it has all the guestIds just PUT
 			jsonConsumer = new JSONObject(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername, sm_clientPassword, sm_serverUrl, "/consumers/"+consumerIdOfHostA));
 			// actual guestIds
 			//DEBUGGING jsonConsumer.put("guestIds", new JSONArray(expectedGuestIdsOnHostA));
 			actualGuestIds.clear();
 			for (int g=0; g<jsonConsumer.getJSONArray("guestIds").length(); g++) {
 				actualGuestIds.add(jsonConsumer.getJSONArray("guestIds").getJSONObject(g).getString("guestId"));
 			}
 			// assert expected guestIds
 			for (String guestId : expectedGuestIdsOnHostA) Assert.assertContains(actualGuestIds, guestId);
 			if (actualGuestIds.size() == expectedGuestIdsOnHostA.size()+c+1) throw new SkipException("Currently Candlepin does NOT purge duplicate guest ids PUT by virt-who onto different host consumers.  The most recently PUT guest id is the winner. Entitlements should be revoked for the older guest id.  Development has decided to keep the stale guest id for potential reporting purposes, hence this test is being skipped until needed in the future."); else
 			Assert.assertEquals(actualGuestIds.size(), expectedGuestIdsOnHostA.size(),"All of the expected guestIds PUT on consumer '"+consumerIdOfHostA+"' using the Candlepin API were verified.");
 
 		}
 	}
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 683459 - Virt only skus creating two pools
 	// TODO Bug 736436 - virtual subscriptions are not included when the certificates are downloaded 
 	// TODO Bug 750659 - candlepin api /consumers/<consumerid>/guests is returning []
 	// TODO Bug 756628 - Unable to entitle consumer to the pool with id '8a90f85733d31add0133d337f9410c52'.: virt.guest.host.does.not.match.pool.owner
 	// TODO Bug 722977 - virt_only pools are not removed from an owner if the physical pool no longer has a valid virt_limit
 	
 	
 	
 	
 	
 	
 	// Configuration methods ***********************************************************************
 		
 	@BeforeClass(groups="setup")
 	public void backupVirtWhatBeforeClass() {
 		// finding location of virt-what...
 		SSHCommandResult result = client.runCommandAndWait("which virt-what");
 		virtWhatFile = new File(result.getStdout().trim());
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, virtWhatFile.getPath())==1,"virt-what is in the client's path");
 		
 		// making a backup of virt-what...
 		virtWhatFileBackup = new File(virtWhatFile.getPath()+".bak");
 		//RemoteFileTasks.runCommandAndAssert(client, "cp -np "+virtWhatFile+" "+virtWhatFileBackup, 0); // cp option -n does not exist on RHEL5 
 		if (RemoteFileTasks.testFileExists(client, virtWhatFileBackup.getPath())==0) {
 			RemoteFileTasks.runCommandAndAssert(client, "cp -p "+virtWhatFile+" "+virtWhatFileBackup, 0);
 		}
 		Assert.assertTrue(RemoteFileTasks.testFileExists(client, virtWhatFileBackup.getPath())==1,"successfully made a backup of virt-what to: "+virtWhatFileBackup);
 
 	}
 	
 	@AfterClass(groups="setup")
 	public void restoreVirtWhatAfterClass() {
 		// restoring backup of virt-what
 		if (virtWhatFileBackup!=null && RemoteFileTasks.testFileExists(client, virtWhatFileBackup.getPath())==1) {
 			RemoteFileTasks.runCommandAndAssert(client, "mv -f "+virtWhatFileBackup+" "+virtWhatFile, 0);
 		}
 	}
 	
 	@BeforeClass(groups="setup")
 	public void registerBeforeClass() throws Exception {
 		clienttasks.unregister(null, null, null);
 		String consumerId = clienttasks.getCurrentConsumerId(clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (String)null, null, null, null, false, null, null, null));
 		ownerKey = CandlepinTasks.getOwnerKeyOfConsumerId(sm_clientUsername, sm_clientPassword, sm_serverUrl, consumerId);
 	}
 	
 	@BeforeMethod(groups="setup")
 	public void unsubscribeBeforeMethod() throws Exception {
 		clienttasks.unsubscribeFromAllOfTheCurrentlyConsumedProductSubscriptions();
 	}
 	
 	// protected methods ***********************************************************************
 	
 	protected String ownerKey = "";
 	protected File virtWhatFile = null;
 	protected File virtWhatFileBackup = null;
 	
 	protected void forceVirtWhatToReturnGuest(String hypervisorType) {
 		// Note: when client is a guest, virt-what returns stdout="<hypervisor type>" and exitcode=0
 		RemoteFileTasks.runCommandAndWait(client,"echo '#!/bin/bash - ' > "+virtWhatFile+"; echo -e 'echo -e \""+hypervisorType+"\"' >> "+virtWhatFile+"; chmod a+x "+virtWhatFile, TestRecords.action());
 	}
 	
 	protected void forceVirtWhatToReturnHost() {
 		// Note: when client is a host, virt-what returns stdout="" and exitcode=0
 		RemoteFileTasks.runCommandAndWait(client,"echo '#!/bin/bash - ' > "+virtWhatFile+"; echo 'exit 0' >> "+virtWhatFile+"; chmod a+x "+virtWhatFile, TestRecords.action());
 	}
 	
 	protected void forceVirtWhatToFail() {
 		// Note: when virt-what does not know if the system is on bare metal or on a guest, it returns a non-zero value
 		RemoteFileTasks.runCommandAndWait(client,"echo '#!/bin/bash - ' > "+virtWhatFile+"; echo 'echo \"virt-what is about to exit with code 255\"; exit 255' >> "+virtWhatFile+"; chmod a+x "+virtWhatFile, TestRecords.action());
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getVirtWhatData")
 	public Object[][] getVirtWhatDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getVirtWhatDataAsListOfLists());
 	}
 	protected List<List<Object>> getVirtWhatDataAsListOfLists(){
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 
 		// man virt-what (virt-what-1.3-4.4.el6.x86_64) shows support for the following hypervisors
 
 		//									Object bugzilla, String host_type
 		ll.add(Arrays.asList(new Object[]{null,	"hyperv"}));
 		ll.add(Arrays.asList(new Object[]{null,	"ibm_systemz\nibm_systemz-direct"}));
 		ll.add(Arrays.asList(new Object[]{null,	"ibm_systemz\nibm_systemz-lpar"}));
 		ll.add(Arrays.asList(new Object[]{null,	"ibm_systemz\nibm_systemz-zvm"}));
 		ll.add(Arrays.asList(new Object[]{null,	"kvm"}));
 		ll.add(Arrays.asList(new Object[]{null,	"openvz"}));
 		ll.add(Arrays.asList(new Object[]{null,	"powervm_lx86"}));
 		ll.add(Arrays.asList(new Object[]{null,	"qemu"}));
 		ll.add(Arrays.asList(new Object[]{null,	"uml"}));
 		ll.add(Arrays.asList(new Object[]{null,	"virtualage"}));
 		ll.add(Arrays.asList(new Object[]{null,	"virtualbox"}));
 		ll.add(Arrays.asList(new Object[]{null,	"virtualpc"}));
 		ll.add(Arrays.asList(new Object[]{null,	"vmware"}));
 		ll.add(Arrays.asList(new Object[]{null,	"xen\nxen-domU"}));
 		ll.add(Arrays.asList(new Object[]{null,	"xen\nxen-hvm"}));
 		ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("757697"),	"xen\nxen-dom0"}));
 
 
 		return ll;
 	}
 	
 	
 	@DataProvider(name="getVirtSubscriptionData")
 	public Object[][] getVirtSubscriptionDataAs2dArray() throws JSONException, Exception {
 		return TestNGUtils.convertListOfListsTo2dArray(getVirtSubscriptionDataAsListOfLists());
 	}
 	protected List<List<Object>> getVirtSubscriptionDataAsListOfLists() throws JSONException, Exception {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks.statusStandalone) {log.warning("This candlepin server is configured for standalone operation.  The hosted virtualization model tests will not be executed."); return ll;}
 		
 		Calendar now = new GregorianCalendar();
 		now.setTimeInMillis(System.currentTimeMillis());
 		
 		JSONArray jsonSubscriptions = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_clientUsername,sm_clientPassword,sm_serverUrl,"/owners/"+ownerKey+"/subscriptions"));	
 		for (int i = 0; i < jsonSubscriptions.length(); i++) {
 			JSONObject jsonSubscription = (JSONObject) jsonSubscriptions.get(i);
 			String subscriptionId = jsonSubscription.getString("id");
 			Calendar startDate = parseISO8601DateString(jsonSubscription.getString("startDate"),"GMT");	// "startDate":"2012-02-08T00:00:00.000+0000"
 			Calendar endDate = parseISO8601DateString(jsonSubscription.getString("endDate"),"GMT");	// "endDate":"2013-02-07T00:00:00.000+0000"
 			int quantity = jsonSubscription.getInt("quantity");
 			JSONObject jsonProduct = (JSONObject) jsonSubscription.getJSONObject("product");
 			String productName = jsonProduct.getString("name");
 			String productId = jsonProduct.getString("id");
 			JSONArray jsonAttributes = jsonProduct.getJSONArray("attributes");
 			// loop through the attributes of this jsonProduct looking for the "virt_limit" attribute
 			for (int j = 0; j < jsonAttributes.length(); j++) {
 				JSONObject jsonAttribute = (JSONObject) jsonAttributes.get(j);
 				String attributeName = jsonAttribute.getString("name");
 				if (attributeName.equals("virt_limit")) {
 					// found the virt_limit attribute - get its value
 					String virt_limit = jsonAttribute.getString("value");
 					
 					// only retrieve data that is valid today (at this time)
 					if (startDate.before(now) && endDate.after(now)) {
 
 //						// save some computation cycles in the testcases and get the hostPoolId and guestPoolId
 //						List<String> poolIds = CandlepinTasks.getPoolIdsForSubscriptionId(sm_clientUsername,sm_clientPassword,sm_serverUrl,ownerKey,subscriptionId);
 //
 //						// determine which pool is for the guest, the other must be for the host
 //						String guestPoolId = null;
 //						String hostPoolId = null;
 //						for (String poolId : poolIds) {
 //							if (CandlepinTasks.isPoolVirtOnly (sm_clientUsername,sm_clientPassword,poolId,sm_serverUrl)) {
 //								guestPoolId = poolId;
 //							} else {
 //								hostPoolId = poolId;
 //							}
 //						}
 //						if (poolIds.size() != 2) {hostPoolId=null; guestPoolId=null;}	// set pools to null if there was a problem
 //						ll.add(Arrays.asList(new Object[]{subscriptionId, productName, productId, quantity, virt_limit, hostPoolId, guestPoolId}));
 
 						
 						// save some computation cycles in the testcases and get the hostPoolId and guestPoolId
 						List<JSONObject> jsonPools = CandlepinTasks.getPoolsForSubscriptionId(sm_clientUsername,sm_clientPassword,sm_serverUrl,ownerKey,subscriptionId);
 
 						// determine which pool is for the guest, and which is for the host
 						String guestPoolId = null;
 						String hostPoolId = null;
 						for (JSONObject jsonPool : jsonPools) {
 							String poolId = jsonPool.getString("id");
 							
 							if (Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "virt_only")) &&
 								Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "pool_derived")) &&
 								CandlepinTasks.getPoolAttributeValue(jsonPool, "requires_host")==null) {
 								guestPoolId = poolId;
 							} else if (
 								Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "virt_only")) &&
 								Boolean.TRUE.toString().equalsIgnoreCase(CandlepinTasks.getPoolAttributeValue(jsonPool, "pool_derived")) &&
 								CandlepinTasks.getPoolAttributeValue(jsonPool, "requires_host")!=null) {
 								//newGuestPoolId = poolId;  // TODO THIS IS THE NEW VIRT-AWARE MODEL FOR WHICH NEW TESTS SHOULD BE AUTOMATED
 							} else if (
 								CandlepinTasks.getPoolAttributeValue(jsonPool, "virt_only")==null &&	// TODO or false?
 								CandlepinTasks.getPoolAttributeValue(jsonPool, "pool_derived")==null &&	// TODO or false?
 								CandlepinTasks.getPoolAttributeValue(jsonPool, "requires_host")==null) {
 								hostPoolId = poolId;
 							}
 						}
 						ll.add(Arrays.asList(new Object[]{subscriptionId, productName, productId, quantity, virt_limit, hostPoolId, guestPoolId}));
 					}
 				}
 			}
 		}
 		
 		return ll;
 	}
 	
 	/* Example jsonSubscription:
 	  {
 		    "id": "8a90f8b42e398f7a012e398ff0ef0104",
 		    "owner": {
 		      "href": "/owners/admin",
 		      "id": "8a90f8b42e398f7a012e398f8d310005"
 		    },
 		    "certificate": null,
 		    "product": {
 		      "name": "Awesome OS with up to 4 virtual guests",
 		      "id": "awesomeos-virt-4",
 		      "attributes": [
 		        {
 		          "name": "variant",
 		          "value": "ALL",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        },
 		        {
 		          "name": "arch",
 		          "value": "ALL",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        },
 		        {
 		          "name": "type",
 		          "value": "MKT",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        },
 		        {
 		          "name": "version",
 		          "value": "6.1",
 		          "updated": "2011-02-18T16:17:37.961+0000",
 		          "created": "2011-02-18T16:17:37.961+0000"
 		        },
 		        {
 		          "name": "virt_limit",
 		          "value": "4",
 		          "updated": "2011-02-18T16:17:37.960+0000",
 		          "created": "2011-02-18T16:17:37.960+0000"
 		        }
 		      ],
 		      "multiplier": 1,
 		      "productContent": [
 
 		      ],
 		      "dependentProductIds": [
 
 		      ],
 		      "href": "/products/awesomeos-virt-4",
 		      "updated": "2011-02-18T16:17:37.959+0000",
 		      "created": "2011-02-18T16:17:37.959+0000"
 		    },
 		    "providedProducts": [
 		      {
 		        "name": "Awesome OS Server Bits",
 		        "id": "37060",
 		        "attributes": [
 		          {
 		            "name": "variant",
 		            "value": "ALL",
 		            "updated": "2011-02-18T16:17:22.174+0000",
 		            "created": "2011-02-18T16:17:22.174+0000"
 		          },
 		          {
 		            "name": "sockets",
 		            "value": "2",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "arch",
 		            "value": "ALL",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "type",
 		            "value": "SVC",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "warning_period",
 		            "value": "30",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          },
 		          {
 		            "name": "version",
 		            "value": "6.1",
 		            "updated": "2011-02-18T16:17:22.175+0000",
 		            "created": "2011-02-18T16:17:22.175+0000"
 		          }
 		        ],
 		        "multiplier": 1,
 		        "productContent": [
 		          {
 		            "content": {
 		              "name": "always-enabled-content",
 		              "id": "1",
 		              "type": "yum",
 		              "modifiedProductIds": [
 
 		              ],
 		              "label": "always-enabled-content",
 		              "vendor": "test-vendor",
 		              "contentUrl": "/foo/path/always",
 		              "gpgUrl": "/foo/path/always/gpg",
 		              "metadataExpire": 200,
 		              "updated": "2011-02-18T16:17:16.254+0000",
 		              "created": "2011-02-18T16:17:16.254+0000"
 		            },
 		            "flexEntitlement": 0,
 		            "physicalEntitlement": 0,
 		            "enabled": true
 		          },
 		          {
 		            "content": {
 		              "name": "never-enabled-content",
 		              "id": "0",
 		              "type": "yum",
 		              "modifiedProductIds": [
 
 		              ],
 		              "label": "never-enabled-content",
 		              "vendor": "test-vendor",
 		              "contentUrl": "/foo/path/never",
 		              "gpgUrl": "/foo/path/never/gpg",
 		              "metadataExpire": 600,
 		              "updated": "2011-02-18T16:17:16.137+0000",
 		              "created": "2011-02-18T16:17:16.137+0000"
 		            },
 		            "flexEntitlement": 0,
 		            "physicalEntitlement": 0,
 		            "enabled": false
 		          },
 		          {
 		            "content": {
 		              "name": "content",
 		              "id": "1111",
 		              "type": "yum",
 		              "modifiedProductIds": [
 
 		              ],
 		              "label": "content-label",
 		              "vendor": "test-vendor",
 		              "contentUrl": "/foo/path",
 		              "gpgUrl": "/foo/path/gpg/",
 		              "metadataExpire": 0,
 		              "updated": "2011-02-18T16:17:16.336+0000",
 		              "created": "2011-02-18T16:17:16.336+0000"
 		            },
 		            "flexEntitlement": 0,
 		            "physicalEntitlement": 0,
 		            "enabled": true
 		          }
 		        ],
 		        "dependentProductIds": [
 
 		        ],
 		        "href": "/products/37060",
 		        "updated": "2011-02-18T16:17:22.174+0000",
 		        "created": "2011-02-18T16:17:22.174+0000"
 		      }
 		    ],
 		    "endDate": "2012-02-18T00:00:00.000+0000",
 		    "startDate": "2011-02-18T00:00:00.000+0000",
 		    "quantity": 5,
 		    "contractNumber": "39",
 		    "accountNumber": "12331131231",
 		    "modified": null,
 		    "tokens": [
 
 		    ],
 		    "upstreamPoolId": null,
 		    "updated": "2011-02-18T16:17:38.031+0000",
 		    "created": "2011-02-18T16:17:38.031+0000"
 		  }
 	  */
 	
 	
 
 	
 	
 
 }
