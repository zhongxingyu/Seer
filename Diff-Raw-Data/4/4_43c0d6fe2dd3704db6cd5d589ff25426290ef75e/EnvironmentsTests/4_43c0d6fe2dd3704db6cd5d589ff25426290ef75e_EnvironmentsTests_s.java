 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.testng.SkipException;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.CandlepinTasks;
 import com.redhat.qe.sm.data.Org;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  *
  */
 @Test(groups={"EnvironmentsTests"})
 public class EnvironmentsTests extends SubscriptionManagerCLITestScript {
 
 	// Test methods ***********************************************************************
 
 	
 	@Test(	description="subscription-manager: verify that an on-premises candlepin server does NOT support environments",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void VerifyThatCandlepinDoesNotSupportEnvironments_Test() throws JSONException, Exception {
 		
 		// ask the candlepin server if it supports environment
 		boolean supportsEnvironments = CandlepinTasks.isEnvironmentsSupported(sm_clientUsername, sm_clientPassword, sm_serverUrl);
 		
 		// skip this test when candlepin supports environments
 		if (supportsEnvironments) throw new SkipException("Candlepin server '"+sm_serverHostname+"' appears to support environments, therefore this test is not applicable.");
 
 		Assert.assertFalse(supportsEnvironments,"Candlepin server '"+sm_serverHostname+"' does not support environments.");
 	}
 	
 	
 	@Test(	description="subscription-manager: run the environments module while prompting for user credentials interactively",
 			groups={},
 			dataProvider = "getInteractiveCredentialsForNonSupportedEnvironmentsData",
 			dependsOnMethods={"VerifyThatCandlepinDoesNotSupportEnvironments_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void EnvironmentsWithInteractivePromptingForCredentials_Test(Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex) {
 
 		// call environments while providing a valid username at the interactive prompt
 		String command;
 		if (client.runCommandAndWait("rpm -q expect").getExitCode().intValue()==0) {	// is expect installed?
 			// assemble an ssh command using expect to simulate an interactive supply of credentials to the environments command
 			String promptedUsernames=""; if (promptedUsername!=null) for (String username : promptedUsername.split("\\n")) {
 				promptedUsernames += "expect \\\"*Username:\\\"; send "+username+"\\\r;";
 			}
 			String promptedPasswords=""; if (promptedPassword!=null) for (String password : promptedPassword.split("\\n")) {
 				promptedPasswords += "expect \\\"*Password:\\\"; send "+password+"\\\r;";
 			}
 			// [root@jsefler-onprem-5server ~]# expect -c "spawn subscription-manager environments --org foo; expect \"*Username:\"; send qa@redhat.com\r; expect \"*Password:\"; send CHANGE-ME\r; expect eof; catch wait reason; exit [lindex \$reason 3]"
 			command = String.format("expect -c \"spawn %s environments --org foo %s %s; %s %s expect eof; catch wait reason; exit [lindex \\$reason 3]\"",
 					clienttasks.command,
 					commandLineUsername==null?"":"--username="+commandLineUsername,
 					commandLinePassword==null?"":"--password="+commandLinePassword,
 					promptedUsernames,
 					promptedPasswords);
 		} else {
 			// assemble an ssh command using echo and pipe to simulate an interactive supply of credentials to the environments command
 			String echoUsername= promptedUsername==null?"":promptedUsername;
 			String echoPassword = promptedPassword==null?"":promptedPassword;
 			String n = (promptedPassword!=null&&promptedUsername!=null)? "\n":"";
 			command = String.format("echo -e \"%s\" | %s environments --org foo %s %s",
 					echoUsername+n+echoPassword,
 					clienttasks.command,
 					commandLineUsername==null?"":"--username="+commandLineUsername,
 					commandLinePassword==null?"":"--password="+commandLinePassword);
 		}
 		
 		// attempt environments with the interactive credentials
 		SSHCommandResult sshCommandResult = client.runCommandAndWait(command);
 		
 		// assert the sshCommandResult here
 		if (expectedExitCode!=null) Assert.assertEquals(sshCommandResult.getExitCode(), expectedExitCode, "The expected exit code from the environments attempt.");
 		if (expectedStdoutRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStdout(), expectedStdoutRegex, "The expected stdout result from environments while supplying interactive credentials.");
 		if (expectedStderrRegex!=null) Assert.assertContainsMatch(sshCommandResult.getStderr(), expectedStderrRegex, "The expected stderr result from environments while supplying interactive credentials.");
 	}
 	
 	
 	@Test(	description="subscription-manager: attempt environments without --org option",
 			groups={},
 			dependsOnMethods={"VerifyThatCandlepinDoesNotSupportEnvironments_Test"},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void AttemptEnvironmentsWithoutOrg_Test() {
 		
 		SSHCommandResult environmentsResult = clienttasks.environments_(sm_clientUsername,sm_clientPassword,null,null,null,null);
 		
 		// assert environmentsResult results
 		Assert.assertEquals(environmentsResult.getStdout().trim(), "you must specify an --org","Environments should require that the org option be specified.");
 		Assert.assertEquals(environmentsResult.getExitCode(), Integer.valueOf(255),"Exit code from environments when executed without an org option.");
 
 	}
 	
 	
 //	TODO create environment tests for a candlepin server that DOES support environments...
 	
 //	@Test(	description="subscription-manager: run the environments module with valid user credentials and verify the expected environments are listed",
 //			groups={"myDevGroup"},
 //			dataProvider="getEnvironmentsForOrgsData",
 //			enabled=true)
 //	//@ImplementsNitrateTest(caseId=)
 //	public void EnvironmentsWithCredentials_Test(String username, String password, String org, List<Environment> expectedEnvironments) {
 //		log.info("Testing subscription-manager environments module using username="+username+" password="+password+" org="+org+" and expecting environmnets="+expectedEnvironments+" ...");
 //		
 //		// use subscription-manager to get the organizations for which the user has access
 //		SSHCommandResult environmentsResult = clienttasks.environments_(username, password, org, null, null, null);
 //		
 //		// when the expectedOrgs is empty, there is a special message, assert it
 //		if (expectedEnvironments.isEmpty()) {
 //			Assert.assertEquals(environmentsResult.getStdout().trim(),username+" cannot register to any organizations.","Special message when the expectedOrgs is empty.");
 //		}
 //		
 //		// parse the actual Orgs from the orgsResult
 //		List<Org> actualEnvironments = Org.parse(environmentsResult.getStdout());
 //		
 //		// assert that all of the expectedOrgs are included in the actualOrgs
 //		for (Environment expectedEnvironment : expectedEnvironments) {
 //			Assert.assertTrue(actualEnvironments.contains(expectedEnvironment), "The list of orgs returned by subscription-manager for user '"+username+"' includes expected org: "+expectedOrg);
 //		}
 //		Assert.assertEquals(actualEnvironments.size(), expectedEnvironments.size(),"The number of orgs returned by subscription-manager for user '"+username+"'.");
 //	}
 	
 	
	
 	
 	// Configuration methods ***********************************************************************
 	
 	
 	
 	// Protected methods ***********************************************************************
 
 
 	
 	// Data Providers ***********************************************************************
 
 	
 //	@DataProvider(name="getEnvironmentsForOrgsData")
 //	public Object[][] getEnvironmentsForOrgsDataDataAs2dArray() throws JSONException, Exception {
 //		return TestNGUtils.convertListOfListsTo2dArray(getEnvironmentsForOrgsDataDataAsListOfLists());
 //	}
 //	protected List<List<Object>> getEnvironmentsForOrgsDataDataAsListOfLists() throws JSONException, Exception {
 //		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 //		// Notes...
 //		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users | python -mjson.tool
 //		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1 | python -mjson.tool
 //		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1/owners | python -mjson.tool
 //
 //		// get all of the candlepin users
 //		// curl -k -u admin:admin https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users | python -mjson.tool
 //		JSONArray jsonUsers = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,sm_serverAdminUsername,sm_serverAdminPassword,"/users"));	
 //		for (int i = 0; i < jsonUsers.length(); i++) {
 //			JSONObject jsonUser = (JSONObject) jsonUsers.get(i);
 //			// {
 //			//   "created": "2011-07-01T06:40:00.951+0000", 
 //			//   "hashedPassword": "05557a2aaec7cb676df574d2eb080691949a6752", 
 //			//   "id": "8a90f8c630e46c7e0130e46ce9b70020", 
 //			//   "superAdmin": false, 
 //			//   "updated": "2011-07-01T06:40:00.951+0000", 
 //			//   "username": "minnie"
 //			// }
 //			Boolean isSuperAdmin = jsonUser.getBoolean("superAdmin");
 //			String username = jsonUser.getString("username");
 //			String password = sm_clientPasswordDefault;
 //			if (username.equals(sm_serverAdminUsername)) password = sm_serverAdminPassword;
 //			
 //			// get the user's owners
 //			// curl -k -u testuser1:password https://jsefler-onprem-62candlepin.usersys.redhat.com:8443/candlepin/users/testuser1/owners | python -mjson.tool
 //			JSONArray jsonUserOwners = new JSONArray(CandlepinTasks.getResourceUsingRESTfulAPI(sm_serverHostname,sm_serverPort,sm_serverPrefix,username,password,"/users/"+username+"/owners"));	
 //			for (int j = 0; j < jsonUserOwners.length(); j++) {
 //				JSONObject jsonOwner = (JSONObject) jsonUserOwners.get(j);
 //				// {
 //				//    "contentPrefix": null, 
 //				//    "created": "2011-07-01T06:39:58.740+0000", 
 //				//    "displayName": "Snow White", 
 //				//    "href": "/owners/snowwhite", 
 //				//    "id": "8a90f8c630e46c7e0130e46ce114000a", 
 //				//    "key": "snowwhite", 
 //				//    "parentOwner": null, 
 //				//    "updated": "2011-07-01T06:39:58.740+0000", 
 //				//    "upstreamUuid": null
 //				// }
 //				String org = jsonOwner.getString("key");
 //				String orgName = jsonOwner.getString("displayName");
 //				
 //				List<Environment> environments = new ArrayList<Environment>();
 //				
 //				// String username, String password, List<Environment> environments
 //				ll.add(Arrays.asList(new Object[]{username,password,org,environments}));
 //			}
 //			
 //
 //		}
 //		
 //		return ll;
 //	}
 //	
 //	
 //	@DataProvider(name="getInvalidCredentialsForOrgsData")
 //	public Object[][] getInvalidCredentialsForOrgsDataAs2dArray() {
 //		return TestNGUtils.convertListOfListsTo2dArray(getInvalidCredentialsForOrgsDataAsListOfLists());
 //	}
 //	protected List<List<Object>> getInvalidCredentialsForOrgsDataAsListOfLists() {
 //		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 //		String x = String.valueOf(getRandInt());
 //		
 //		// String username, String password
 //		ll.add(Arrays.asList(new Object[]{	sm_clientUsername+x,	sm_clientPassword}));
 //		ll.add(Arrays.asList(new Object[]{	sm_clientUsername,		sm_clientPassword+x}));
 //		ll.add(Arrays.asList(new Object[]{	sm_clientUsername+x,	sm_clientPassword+x}));
 //		
 //		return ll;
 //	}
 	
 	
 	// NOTE: Assumes
 	@DataProvider(name="getInteractiveCredentialsForNonSupportedEnvironmentsData")
 	public Object[][] getInteractiveCredentialsForNonSupportedEnvironmentsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getInteractiveCredentialsForNonSupportedEnvironmentsDataAsListOfLists());
 	}
 	protected List<List<Object>> getInteractiveCredentialsForNonSupportedEnvironmentsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>(); if (!isSetupBeforeSuiteComplete) return ll;
 		if (servertasks==null) return ll;
 		if (clienttasks==null) return ll;
 		
 		String stdoutMsg = "This system does not support environments.";
 		String uErrMsg = servertasks.invalidCredentialsRegexMsg();
 		String x = String.valueOf(getRandInt());
 		if (client.runCommandAndWait("rpm -q expect").getExitCode().intValue()==0) {	// is expect installed?
 			// Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			null,						null,				sm_clientPassword,	new Integer(0),		stdoutMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		null,						null,				sm_clientPassword,	new Integer(255),	uErrMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword,			sm_clientUsername,	null,				new Integer(0),		stdoutMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword+x,		sm_clientUsername,	null,				new Integer(255),	uErrMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			sm_clientPassword,			null,				null,				new Integer(0),		stdoutMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		sm_clientPassword+x,		null,				null,				new Integer(255),	uErrMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	"\n\n"+sm_clientUsername,	"\n\n"+sm_clientPassword,	null,				null,				new Integer(0),		"(\nUsername: ){3}"+sm_clientUsername+"(\nPassword: ){3}"+"\n"+stdoutMsg,	null}));
 		} else {
 			// Object bugzilla, String promptedUsername, String promptedPassword, String commandLineUsername, String commandLinePassword, Integer expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			null,						null,				sm_clientPassword,	new Integer(0),		stdoutMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		null,						null,				sm_clientPassword,	new Integer(255),	null,				uErrMsg}));	// RHEL58
 		//	ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		null,						null,				sm_clientPassword,	new Integer(0),		stdoutMsg,			null}));	// RHEL62
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword,			sm_clientUsername,	null,				new Integer(0),		stdoutMsg,			null}));
 		//	ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword+x,		sm_clientUsername,	null,				new Integer(255),	null,				uErrMsg}));
 			ll.add(Arrays.asList(new Object[] {	null,	null,						sm_clientPassword+x,		sm_clientUsername,	null,				new Integer(0),		stdoutMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername,			sm_clientPassword,			null,				null,				new Integer(0),		stdoutMsg,			null}));
 		//	ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		sm_clientPassword+x,		null,				null,				new Integer(255),	null,				uErrMsg}));
 			ll.add(Arrays.asList(new Object[] {	null,	sm_clientUsername+x,		sm_clientPassword+x,		null,				null,				new Integer(0),		stdoutMsg,			null}));
 			ll.add(Arrays.asList(new Object[] {	null,	"\n\n"+sm_clientUsername,	"\n\n"+sm_clientPassword,	null,				null,				new Integer(0),		"(Username: ){3}"+stdoutMsg,	"(Warning: Password input may be echoed.\nPassword: \n){3}"}));
 		
 		}
 		return ll;
 	}
 
 }
