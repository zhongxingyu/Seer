 package rhsm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import rhsm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author ssalevan
  * @author jsefler
  *
  */
 @Test(groups={"GeneralTests"})
 public class GeneralTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to access functionality without registering",
 			groups={},
 			dataProvider="UnregisteredCommandData")
 	@ImplementsNitrateTest(caseId=41697)
 	public void AttemptingCommandsWithoutBeingRegistered_Test(String command) {
 		log.info("Testing subscription-manager-cli command without being registered, expecting it to fail: "+ command);
 		clienttasks.unregister(null, null, null);
 		//RemoteFileTasks.runCommandExpectingNonzeroExit(sshCommandRunner, command);
 		//RemoteFileTasks.runCommandAndAssert(client,command,1,"^Error: You need to register this system by running `register` command before using this option.",null);
 		// results changed after bug fix 749332
 		RemoteFileTasks.runCommandAndAssert(client,command,255,"^"+clienttasks.msg_ConsumerNotRegistered,null);
 
 	}
 	
 	
 	@Test(	description="subscription-manager-cli: attempt to access functionality that does not exist",
 			groups={},
 			dataProvider="NegativeFunctionalityData")
 	public void AttemptingCommandsThatAreInvalid_Test(String command, Integer expectedExitCode, String expectedStdout, String expectedStderr) {
 		log.info("Testing subscription-manager-cli command that is invalid, expecting it to fail: "+ command);
 		SSHCommandResult result = client.runCommandAndWait(command);
 		if (expectedExitCode!=null)	Assert.assertEquals(result.getExitCode(), expectedExitCode, "The expected exit code.");
 		if (expectedStdout!=null)	Assert.assertEquals(result.getStdout().trim(), expectedStdout, "The expected stdout message.");
 		if (expectedStderr!=null)	Assert.assertEquals(result.getStderr().trim(), expectedStderr, "The expected stderr message.");
 	}
 	
 	
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 688469 - subscription-manager <module> --help does not work in localized environment.
 	// TODO Bug 684941 - Deleting a product with a subscription gives ugly error
 	// TODO Bug 629708 - import/export validation error wrapped
 	// TODO Bug 744536 - [ALL LANG] [RHSM CLI] unsubscribe module _unexpected 'ascii' code can't decode ...message.
 	
 	// Data Providers ***********************************************************************
 	
 
 	
 	@DataProvider(name="UnregisteredCommandData")
 	public Object[][] getUnregisteredCommandDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getUnregisteredCommandDataAsListOfLists());
 	}
 	public List<List<Object>> getUnregisteredCommandDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" facts --update"}));  test moved to FactsTests.FactsWhenNotRegistered_Test()
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" identity"}));  test moved to IdentityTests.IdentityWhenNotRegistered_Test()
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" list"}));	restriction lifted by https://bugzilla.redhat.com/show_bug.cgi?id=725870
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" list --available --all"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" list --available"}));
 		//ll.add(Arrays.asList(new Object[]{clienttasks.command+" list --consumed"}));	restriction lifted by https://bugzilla.redhat.com/show_bug.cgi?id=725870
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" refresh"}));
 // this functionality appears to have been removed: subscription-manager-0.71-1.el6.i686  - jsefler 7/21/2010
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --product=FOO"}));
 // this functionality appears to have been removed: subscription-manager-0.93.14-1.el6.x86_64 - jsefler 1/21/2011
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --regtoken=FOO"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --pool=FOO"}));
 // ability to unsubscribe without being registered was added after fix for bug 735338  jsefler 9/13/2011
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --all"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --serial=FOO"}));
 // this functionality appears to have been removed: subscription-manager-0.68-1.el6.i686  - jsefler 7/12/2010
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --product=FOO"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --regtoken=FOO"}));
 //		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --pool=FOO"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" redeem"}));
 
 		return ll;
 	}
 	
 	@DataProvider(name="NegativeFunctionalityData")
 	public Object[][] getNegativeFunctionalityDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getNegativeFunctionalityDataAsListOfLists());
 	}
 	protected List<List<Object>> getNegativeFunctionalityDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// due to design changes, this is a decent place to dump old commands that have been removed
 		
 		// String command, int expectedExitCode, String expectedStdoutRegex, String expectedStderrRegex
 		
 		// negative tests that require the system to be unregistered first...
 		clienttasks.unregister(null,null,null);
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --product=FOO",					new Integer(2),		clienttasks.command+": error: no such option: --product", "Usage: subscription-manager unsubscribe [OPTIONS]"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --regtoken=FOO",				new Integer(2),		clienttasks.command+": error: no such option: --regtoken", "Usage: subscription-manager unsubscribe [OPTIONS]"}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" unsubscribe --pool=FOO",					new Integer(2),		clienttasks.command+": error: no such option: --pool", "Usage: subscription-manager unsubscribe [OPTIONS]"}));
		ll.add(Arrays.asList(new Object[]{clienttasks.command+" register --servicelevel=foo",				new Integer(255),	"Error: Must use --auto-attach with --servicelevel.", ""}));	// changed by bug 874804		ll.add(Arrays.asList(new Object[]{clienttasks.command+" register --servicelevel=foo",				new Integer(255),	"Error: Must use --autosubscribe with --servicelevel.", ""}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" list --installed --servicelevel=foo",		new Integer(255),	"Error: --servicelevel is only applicable with --available or --consumed", ""}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe",									new Integer(255),	"This system is not yet registered. Try 'subscription-manager register --help' for more information.",	""}));
 		
 		// negative tests that require the system to be registered first...
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" register --username "+sm_clientUsername+" --password "+sm_clientPassword+(sm_clientOrg==null?"":" --org "+sm_clientOrg),									new Integer(0),	null,	""}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe",									new Integer(255),	"Error: This command requires that you specify a pool with --pool or use --auto.",	""}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --pool=123 --auto",				new Integer(255),	"Error: Only one of --pool or --auto may be used with this command.", ""}));
 		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --pool=123 --servicelevel=foo",	new Integer(255),	"Error: Must use --auto with --servicelevel.", ""}));
		ll.add(Arrays.asList(new Object[]{clienttasks.command+" subscribe --pool=123 --servicelevel=foo",	new Integer(255),	"Error: Must use --auto with --servicelevel.", ""}));
 
 		return ll;
 	}
 }
