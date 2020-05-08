 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.testng.SkipException;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.LogMessageUtil;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.cli.tasks.SubscriptionManagerTasks;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 
 /**
  * @author jsefler
  *
  */
 @Test(groups={"HelpTests"})
 public class HelpTests extends SubscriptionManagerCLITestScript{
 	
 	
 	// Test Methods ***********************************************************************
 	
 	@Test(	description="subscription-manager-cli: man page",
 			groups={},
 			enabled=true)
 	@ImplementsNitrateTest(caseId=41697)
 	public void ManPageForCLI_Test() {
 		if (clienttasks==null) throw new SkipException("A client connection is needed for this test.");
 		String cliCommand = clienttasks.command;
 		RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+cliCommand,0);
 		RemoteFileTasks.runCommandAndAssert(client,"whatis "+cliCommand,0,"^"+cliCommand+" ",null);
 		log.warning("We only tested the existence of the man page; NOT the content.");
 	}
 	
 	@Test(	description="subscription-manager-gui: man page",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void ManPageForGUI_Test() {
 		if (clienttasks==null) throw new SkipException("A client connection is needed for this test.");
 		String guiCommand = clienttasks.command+"-gui";
 
 		// is the guiCommand installed?
 		if (client.runCommandAndWait("rpm -q "+clienttasks.command+"-gnome").getStdout().contains("is not installed")) {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+guiCommand,1,null,"^No manual entry for "+guiCommand);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+guiCommand,0,"^"+guiCommand+": nothing appropriate",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 			throw new SkipException(guiCommand+" is not installed and therefore its man page is also not installed.");
 		} else {
 			RemoteFileTasks.runCommandAndAssert(client,"man -P cat "+guiCommand,0);
 			RemoteFileTasks.runCommandAndAssert(client,"whatis "+guiCommand,0,"^"+guiCommand+" ",null);
 			log.warning("In this test we tested only the existence of the man page; NOT the content.");
 		}
 	}
 	
 	@Test(	description="subscription-manager-cli: assert only expected command line options are available",
 			groups={"blockedByBug-664581"/*,"blockedByBug-693527"*/},
 			dataProvider="ExpectedCommandLineOptionsData")
 	@ImplementsNitrateTest(caseId=46713)
 	//@ImplementsNitrateTest(caseId=46707)
 	public void ExpectedCommandLineOptions_Test(Object meta, String command, String stdoutRegex, List<String> expectedOptions) {
 		log.info("Testing subscription-manager-cli command line options '"+command+"' and verifying that only the expected options are available.");
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,command,0);
 		
 		Pattern pattern = Pattern.compile(stdoutRegex, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(result.getStdout());
 		Assert.assertTrue(matcher.find(),"Available command line options are shown with command: "+command);
 		
 		// find all the matches to stderrRegex
 		List <String> actualOptions = new ArrayList<String>();
 		do {
 			actualOptions.add(matcher.group().trim());
 		} while (matcher.find());
 		
 		// assert all of the expectedOptions were found and that no unexpectedOptions were found
 		for (String expectedOption : expectedOptions) {
 			if (!actualOptions.contains(expectedOption)) {
 				log.warning("Could not find the expected command '"+command+"' option '"+expectedOption+"'.");
 			} else {
 				Assert.assertTrue(actualOptions.contains(expectedOption),"The expected command '"+command+"' option '"+expectedOption+"' is available.");
 			}
 		}
 		for (String actualOption : actualOptions) {
 			if (!expectedOptions.contains(actualOption))
 				log.warning("Found an unexpected command '"+command+"' option '"+actualOption+"'.");
 		}
 		Assert.assertTrue(actualOptions.containsAll(expectedOptions), "All of the expected command '"+command+"' line options are available.");
 		Assert.assertTrue(expectedOptions.containsAll(actualOptions), "All of the available command '"+command+"' line options are expected.");
 	}
 	
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 694662 - the whitespace in the title line of man subscription-manager-gui is completely consumed
	
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 	
 	@BeforeClass(groups={"setup"})
 	public void makewhatisBeforeClass() {
 		// running makewhatis to ensure that the whatis database is built on Beaker provisioned systems
 		RemoteFileTasks.runCommandAndAssert(client,"makewhatis",0);
 	}
 	
 	
 	
 	
 	// Data Providers ***********************************************************************
 
 	
 	@DataProvider(name="ExpectedCommandLineOptionsData")
 	public Object[][] getExpectedCommandLineOptionsDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getExpectedCommandLineOptionsDataAsListOfLists());
 	}
 	protected List<List<Object>> getExpectedCommandLineOptionsDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		
 		// String command, String stdoutRegex, List<String> expectedOptions
 		String module;
 		String modulesRegex = "^	\\w+";
 		String optionsRegex = "^  --\\w+[(?:=\\w)]*|^  -\\w[(?:=\\w)]*\\, --\\w+[(?:=\\w)]*";
 		
 		// MODULES
 		List <String> modules = new ArrayList<String>();
 		modules.add("activate");
 		modules.add("clean");
 		modules.add("facts");
 		modules.add("identity");
 		modules.add("list");
 		modules.add("refresh");
 		modules.add("register");
 //		modules.add("reregister");
 		modules.add("subscribe");
 		modules.add("unregister");
 		modules.add("unsubscribe");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h",clienttasks.command+" --help"}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" [options] MODULENAME --help";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, modulesRegex, modules}));
 		}
 		
 		// MODULE: activate
 		module = "activate";
 		List <String> activateOptions = new ArrayList<String>();
 		activateOptions.add("-h, --help");
 		activateOptions.add("--debug=DEBUG");
 //		activateOptions.add("-k, --insecure");
 		activateOptions.add("--email=EMAIL");
 		activateOptions.add("--locale=LOCALE");
 		activateOptions.add("--proxy=PROXY_URL");
 		activateOptions.add("--proxyuser=PROXY_USER");
 		activateOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, activateOptions}));
 		}
 		
 		// MODULE: clean
 		module = "clean";
 		List <String> cleanOptions = new ArrayList<String>();
 		cleanOptions.add("-h, --help");
 		cleanOptions.add("--debug=DEBUG");
 		// removed in https://bugzilla.redhat.com/show_bug.cgi?id=664581
 		//cleanOptions.add("--proxy=PROXY_URL");
 		//cleanOptions.add("--proxyuser=PROXY_USER");
 		//cleanOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, cleanOptions}));
 		}
 		
 		// MODULE: facts
 		module = "facts";
 		List <String> factsOptions = new ArrayList<String>();
 		factsOptions.add("-h, --help");
 		factsOptions.add("--debug=DEBUG");
 //		factsOptions.add("-k, --insecure");
 		factsOptions.add("--list");
 		factsOptions.add("--update");
 		factsOptions.add("--proxy=PROXY_URL");
 		factsOptions.add("--proxyuser=PROXY_USER");
 		factsOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, factsOptions}));
 		}
 		
 		// MODULE: identity
 		module = "identity";
 		List <String> identityOptions = new ArrayList<String>();
 		identityOptions.add("-h, --help");
 		identityOptions.add("--debug=DEBUG");
 		identityOptions.add("--username=USERNAME");
 		identityOptions.add("--password=PASSWORD");
 		identityOptions.add("--regenerate");
 		identityOptions.add("--force");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=678151
 		identityOptions.add("--proxy=PROXY_URL");
 		identityOptions.add("--proxyuser=PROXY_USER");
 		identityOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, identityOptions}));
 		}
 		
 		// MODULE: list
 		module = "list";
 		List <String> listOptions = new ArrayList<String>();
 		listOptions.add("-h, --help");
 		listOptions.add("--debug=DEBUG");
 //		listOptions.add("-k, --insecure");
 		listOptions.add("--installed");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=634254
 		listOptions.add("--consumed");
 		listOptions.add("--available");
 		listOptions.add("--all");
 		listOptions.add("--ondate=ON_DATE");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=672562
 		listOptions.add("--proxy=PROXY_URL");
 		listOptions.add("--proxyuser=PROXY_USER");
 		listOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, listOptions}));
 		}
 		
 		// MODULE: refresh
 		module = "refresh";
 		List <String> refreshOptions = new ArrayList<String>();
 		refreshOptions.add("-h, --help");
 		refreshOptions.add("--debug=DEBUG");
 		refreshOptions.add("--proxy=PROXY_URL");
 		refreshOptions.add("--proxyuser=PROXY_USER");
 		refreshOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, refreshOptions}));
 		}
 		
 		// MODULE: register
 		module = "register";
 		List <String> registerOptions = new ArrayList<String>();
 		registerOptions.add("-h, --help");
 		registerOptions.add("--debug=DEBUG");
 //		registerOptions.add("-k, --insecure");
 		registerOptions.add("--username=USERNAME");
 		registerOptions.add("--type=CONSUMERTYPE");
 		registerOptions.add("--name=CONSUMERNAME");
 		registerOptions.add("--password=PASSWORD");
 		registerOptions.add("--consumerid=CONSUMERID");
 		registerOptions.add("--autosubscribe");
 		registerOptions.add("--force");
 		registerOptions.add("--proxy=PROXY_URL");
 		registerOptions.add("--proxyuser=PROXY_USER");
 		registerOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[]{null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("628589"), smHelpCommand, optionsRegex, registerOptions}));
 		}
 		
 //		// MODULE: reregister
 //		List <String> reregisterOptions = new ArrayList<String>();
 //		reregisterOptions.add("-h, --help");
 //		reregisterOptions.add("--debug=DEBUG");
 ////		reregisterOptions.add("-k, --insecure");
 //		reregisterOptions.add("--username=USERNAME");
 //		reregisterOptions.add("--password=PASSWORD");
 //		reregisterOptions.add("--consumerid=CONSUMERID");
 //		for (String smHelpCommand : new String[]{clienttasks.command+" -h reregister",clienttasks.command+" --help reregister"}) {
 //			List <String> usages = new ArrayList<String>();
 //			String usage = "Usage: "+clienttasks.command+" reregister [OPTIONS]";
 //			usages.add(usage);
 //			ll.add(Arrays.asList(new Object[]{ smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 //			ll.add(Arrays.asList(new Object[]{ smHelpCommand, optionsRegex, reregisterOptions}));
 //		}
 		
 		// MODULE: subscribe
 		module = "subscribe";
 		List <String> subscribeOptions = new ArrayList<String>();
 		subscribeOptions.add("-h, --help");
 		subscribeOptions.add("--debug=DEBUG");
 		subscribeOptions.add("--pool=POOL");
 		subscribeOptions.add("--auto");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=680399
 //		subscribeOptions.add("-k, --insecure");
 //		subscribeOptions.add("--regtoken=REGTOKEN");	// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 //		subscribeOptions.add("--email=EMAIL");			// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 //		subscribeOptions.add("--locale=LOCALE");		// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 		subscribeOptions.add("--proxy=PROXY_URL");
 		subscribeOptions.add("--proxyuser=PROXY_USER");
 		subscribeOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, subscribeOptions}));
 		}
 		
 		// MODULE: unregister
 		module = "unregister";
 		List <String> unregisterOptions = new ArrayList<String>();
 		unregisterOptions.add("-h, --help");
 		unregisterOptions.add("--debug=DEBUG");
 //		unregisterOptions.add("-k, --insecure");
 		unregisterOptions.add("--proxy=PROXY_URL");
 		unregisterOptions.add("--proxyuser=PROXY_USER");
 		unregisterOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, unregisterOptions}));
 		}
 		
 		// MODULE: unsubscribe
 		module = "unsubscribe";
 		List <String> unsubscribeOptions = new ArrayList<String>();
 		unsubscribeOptions.add("-h, --help");
 		unsubscribeOptions.add("--debug=DEBUG");
 //		unsubscribeOptions.add("-k, --insecure");
 		unsubscribeOptions.add("--serial=SERIAL");
 		unsubscribeOptions.add("--all");
 		unsubscribeOptions.add("--proxy=PROXY_URL");
 		unsubscribeOptions.add("--proxyuser=PROXY_USER");
 		unsubscribeOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, unsubscribeOptions}));
 		}
 		
 		
 		return ll;
 	}
 }
