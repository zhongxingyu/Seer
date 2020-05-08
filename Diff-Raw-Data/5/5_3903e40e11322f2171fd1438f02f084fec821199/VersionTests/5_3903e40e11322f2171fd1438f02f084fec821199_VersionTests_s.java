 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.testng.SkipException;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BzChecker;
 import com.redhat.qe.jul.TestRecords;
 import com.redhat.qe.sm.base.CandlepinType;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  */
 @Test(groups={"VersionTests"})
 public class VersionTests extends SubscriptionManagerCLITestScript {
 
 	// SAMPLE RESULTS
 	//	[root@jsefler-rhel59 ~]# subscription-manager version
 	//	remote entitlement server: Unknown
 	//	remote entitlement server type: subscription management service
 	//	subscription-manager: 1.0.9-1.git.37.53fde9a.el5
 	//	python-rhsm: 1.0.3-1.git.2.47dc8f4.el5
 	
 	
 	
 	// Test methods ***********************************************************************
 
 	@Test(	description="assert that the installed version of subscription-manager is reported by the subscription-manager version module ",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VersionOfSubscriptionManager_Test() {
 		
 		// get the expected results for subscription-manager rpm version
 		String expectedReport = client.runCommandAndWait("rpm -q subscription-manager --queryformat '%{NAME}: %{VERSION}-%{RELEASE}'").getStdout().trim();
 		
 		// get the actual version results from subscription-manager
 		SSHCommandResult actualResult = clienttasks.version();
 		
 		// assert results
 		Assert.assertTrue(actualResult.getStdout().contains(expectedReport),"The version report contains the expected string '"+expectedReport+"'");
 	}
 	
 	
 	@Test(	description="assert that the installed version of python-rhsm is reported by the subscription-manager version module ",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VersionOfPythonRhsm_Test() {
 		
 		// get the expected results for python-rhsm rpm version
 		String expectedReport = client.runCommandAndWait("rpm -q python-rhsm --queryformat '%{NAME}: %{VERSION}-%{RELEASE}'").getStdout().trim();
 		
 		// get the actual version results from subscription-manager
 		SSHCommandResult actualResult = clienttasks.version();
 
 		// assert results
 		Assert.assertTrue(actualResult.getStdout().contains(expectedReport),"The version report contains the expected string '"+expectedReport+"'");
 	}
 	
 	
 	@Test(	description="assert that the candlepin sever version is reported by the version module (expect Unknown when not registered)",
 			groups={"blockedByBug-862308"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VersionOfCandlepinWhenUnregistered_Test() {
 
 		// make sure we are not registered
 		clienttasks.unregister(null, null, null);
 		
 		String expectedType = "FIXME_TEST: UNKNOWN CANDLEPIN TYPE";
 		if (sm_serverType==CandlepinType.standalone)	expectedType = "subscription management service";
 		if (sm_serverType==CandlepinType.hosted)		expectedType = "subscription management service";	// TODO not sure if this is correct
 		if (sm_serverType==CandlepinType.sam)			expectedType = "SAM";		// TODO not sure if this is correct
 		if (sm_serverType==CandlepinType.katello)		expectedType = "Katello";	// TODO not sure if this is correct
 		assertServerVersion(servertasks.statusVersion, expectedType);
 	}
 	
 	protected String server_hostname;
 	@Test(	description="assert that the candlepin sever version is reported as Unknown when not registered AND hostname is bogus",
 			groups={"VersionOfCandlepinWhenUnregisteredAndHostnameIsUnknown_Test","blockedByBug-843191"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VersionOfCandlepinWhenUnregisteredAndHostnameIsUnknown_Test() {
 
 		// make sure we are not registered
 		clienttasks.unregister(null, null, null);
 		
 		// invalidate the server hostname
 		server_hostname	= clienttasks.getConfFileParameter(clienttasks.rhsmConfFile, "server", "hostname");
 		clienttasks.config(null, null, true, new String[]{"server","hostname","UNKNOWN"});
 		
 		assertServerVersion("Unknown","Unknown");
 	}
 	@AfterGroups(value={"VersionOfCandlepinWhenUnregisteredAndHostnameIsUnknown_Test"}, groups={"setup"})
 	public void afterVersionOfCandlepinWhenUnregisteredAndHostnameIsUnknown_Test() {
 		if (server_hostname!=null)	clienttasks.config(null,null,true,new String[]{"server","hostname",server_hostname});
 	}
 	
 	
 	@Test(	description="assert that the candlepin sever version and type are reported by the subscription-manager version module",
 			groups={/*blockedByBug-843649*/"AcceptanceTests"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VersionOfCandlepinWhenRegistered_Test() {
 		
 		// TEMPORARY WORKAROUND FOR BUG
 		String bugId="843649"; //  Bug 843649 - subscription-manager server version reports Unknown against prod/stage candlepin
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen && sm_serverType==CandlepinType.hosted) {
 			throw new SkipException("Skipping this test against a hosted (sharded) Candlepin environment while bug "+bugId+" is open.");
 		}
 		// END OF WORKAROUND
 		
 		// make sure we are registered
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (List<String>)null, null, null, null, null, null, null, null);
 		
 		String expectedType = "FIXME_TEST: UNKNOWN CANDLEPIN TYPE";
 		if (sm_serverType==CandlepinType.standalone)	expectedType = "subscription management service";
 		if (sm_serverType==CandlepinType.hosted)		expectedType = "subscription management service";	// TODO not sure if this is correct
 		if (sm_serverType==CandlepinType.sam)			expectedType = "SAM";		// TODO not sure if this is correct
 		if (sm_serverType==CandlepinType.katello)		expectedType = "Katello";	// TODO not sure if this is correct
 		assertServerVersion(servertasks.statusVersion, expectedType);
 	}
 	
 	
 	@Test(	description="assert the sever version and type when registered to RHN Classic (and simultaneously NOT registered to Subscription Management)",
 			groups={"blockedByBug-852328","VersionOfServerWhenUsingRHNClassic_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VersionOfServerWhenRegisteredAndUsingRHNClassic_Test() {
 		
 		if (Arrays.asList("6.1","6.2","6.3","5.7","5.8","5.9").contains(clienttasks.redhatReleaseXY)) {
 			throw new SkipException("Blocking bugzilla 852328 was fixed in a subsequent release.  Skipping this test since we already know it will fail in RHEL release '"+clienttasks.redhatReleaseXY+"'.");
 		}
 
 		// make sure we are registered
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (List<String>)null, null, null, null, null, null, null, null);
 		
 		// simulate registration to RHN Classic by creating a /etc/sysconfig/rhn/systemid
 		log.info("Simulating registration to RHN Classic by creating an empty systemid file '"+clienttasks.rhnSystemIdFile+"'...");
 		RemoteFileTasks.runCommandAndWait(client, "touch "+clienttasks.rhnSystemIdFile, TestRecords.action());
 		Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile), "RHN Classic systemid file '"+clienttasks.rhnSystemIdFile+"' is in place.");
 		
 		assertServerVersion("Unknown","RHN Classic and subsciption management service");
 	}
 	@Test(	description="assert the sever version and type when registered to RHN Classic (and simultaneously registered to Subscription Management)",
 			groups={"VersionOfServerWhenUsingRHNClassic_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VersionOfServerWhenUnregisteredAndUsingRHNClassic_Test() {
 
 		// make sure we are unregistered
 		clienttasks.unregister(null,null,null);
 		
 		// simulate registration to RHN Classic by creating a /etc/sysconfig/rhn/systemid
 		log.info("Simulating registration to RHN Classic by creating an empty systemid file '"+clienttasks.rhnSystemIdFile+"'...");
 		RemoteFileTasks.runCommandAndWait(client, "touch "+clienttasks.rhnSystemIdFile, TestRecords.action());
 		Assert.assertTrue(RemoteFileTasks.testExists(client, clienttasks.rhnSystemIdFile), "RHN Classic systemid file '"+clienttasks.rhnSystemIdFile+"' is in place.");
 		
 		assertServerVersion("Unknown","RHN Classic");
 	}
 	@AfterGroups(groups={"setup"},value="VersionOfServerWhenUsingRHNClassic_Test")
 	public void afterVersionOfServerWhenUsingRHNClassic_Test() {
 		if (clienttasks!=null) {
 			clienttasks.removeRhnSystemIdFile();
 		}
 	}
 	
 	
 	@Test(	description="assert that no errors are reported while executing version module while registered and unregistered",
 			groups={"blockedByBug-848409"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyNoErrorWhileCheckingServerVersion_Test() {
 		
 		// from Bug 848409 - Error while checking server version: No such file or directory
 		//	[root@jsefler-59server ~]# subscription-manager version
 		//	Error while checking server version: No such file or directory
 		//	remote entitlement server: Unknown
 		//	remote entitlement server type: Unknown
 		//	subscription-manager: 1.0.13-1.git.27.2a76fe7.el5
 
 		// assert results from version do not contain an error (while unregistered)
 		String error = "Error";
 		clienttasks.unregister(null,null,null);
 		SSHCommandResult versionResult = clienttasks.version();
 		Assert.assertTrue(!versionResult.getStdout().contains(error),"Stdout from the version report does NOT contain an '"+error+"' message (while unregistered).");
 		Assert.assertTrue(!versionResult.getStderr().contains(error),"Stderr from the version report does NOT contain an '"+error+"' message (while unregistered).");
 		
 		// assert results from version do not contain an error (while registered)
 		clienttasks.register(sm_clientUsername, sm_clientPassword, sm_clientOrg, null, null, null, null, null, null, null, (List<String>)null, null, null, null, null, null, null, null);
 		versionResult = clienttasks.version();
 		Assert.assertTrue(!versionResult.getStdout().contains(error),"Stdout from the version report does NOT contain an '"+error+"' message (while registered).");
 		Assert.assertTrue(!versionResult.getStderr().contains(error),"Stderr from the version report does NOT contain an '"+error+"' message (while registered).");
 	
 	}
 	
 	
 	
 	// Candidates for an automated Test:
 	
 	
 	
 	// Configuration methods ***********************************************************************
 
 	
 	
 	// Protected methods ***********************************************************************
 
 	protected void assertServerVersion(String serverVersion, String serverType) {
 		// set the expected results
 		String expectedVersion;
		expectedVersion = "remote entitlement server: "+serverVersion;	// changed by bug 846834
		expectedVersion = "registered to: "+serverVersion;
 		String expectedType;
 		expectedType = "server type: "+serverType;
 		
 		// get the actual version results from subscription-manager
 		SSHCommandResult actualResult = clienttasks.version();
 		
 		// assert results
 		Assert.assertTrue(actualResult.getStdout().contains(expectedVersion),"The version report contains the expected string '"+expectedVersion+"'");
 		Assert.assertTrue(actualResult.getStdout().contains(expectedType),"The version report contains the expected string '"+expectedType+"'");
 	}
 
 	
 	// Data Providers ***********************************************************************
 
 
 }
