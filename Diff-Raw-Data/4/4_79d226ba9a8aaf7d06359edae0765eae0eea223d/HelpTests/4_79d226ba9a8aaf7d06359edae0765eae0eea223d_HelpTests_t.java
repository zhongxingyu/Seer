 package com.redhat.qe.sm.cli.tests;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.testng.SkipException;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.auto.tcms.ImplementsNitrateTest;
 import com.redhat.qe.auto.testng.Assert;
 import com.redhat.qe.auto.testng.BlockedByBzBug;
 import com.redhat.qe.auto.testng.BzChecker;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
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
 			groups={},
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
 	
 	
 	@Test(	description="subscription-manager-cli: assert help commands return translated text",
 			groups={},
 			dataProvider="TranslatedCommandLineHelpData")
 	//@ImplementsNitrateTest(caseId=)
 	public void TranslatedCommandLineHelp_Test(Object meta, String lang, String command, List<String> stdoutRegexs) {
 		SSHCommandResult result = RemoteFileTasks.runCommandAndAssert(client,"LANG="+lang+".UTF-8 "+command,0,stdoutRegexs,null);
 	}
 	
 	
 	// Candidates for an automated Test:
 	// TODO Bug 694662 - the whitespace in the title line of man subscription-manager-gui is completely consumed
	// TODO Bug 752321 - [ALL LANG] [RHSM CLI] Word [OPTIONS] is unlocalized and some message translation is still not complete
	//      TODO NESTED LANG LOOP...  for L in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do for C in list refresh register subscribe unregister unsubscribe clean config environments facts identity import orgs redeem repos; do echo ""; echo "# LANG=$L.UTF8 subscription-manager $C --help | grep OPTIONS"; LANG=$L.UTF8 subscription-manager $C --help | grep OPTIONS; done; done;
 	
 	// Configuration Methods ***********************************************************************
 	
 	@BeforeClass(groups={"setup"})
 	public void makewhatisBeforeClass() {
 		// running makewhatis to ensure that the whatis database is built on Beaker provisioned systems
 		RemoteFileTasks.runCommandAndAssert(client,"makewhatis",0);
 	}
 	
 	
 	// Protected Methods ***********************************************************************
 
 	protected List<String> newList(String item) {
 		List <String> newList = new ArrayList<String>();
 		newList.add(item);
 		return newList;
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
 		String optionsRegex = "^  --[\\w\\.]+(=[\\w\\.]+)*|^  -\\w(=\\w+)*\\, --\\w+(=\\w+)*";
 		/* EXAMPLE FOR optionsRegex
 		  -h, --help            show this help message and exit
 		  --list                list the configuration for this system
 		  --remove=REMOVE       remove configuration entry by section.name
 		  --server.hostname=SERVER.HOSTNAME
 		*/
 
 		
 		// MODULES
 		List <String> modules = new ArrayList<String>();
 		modules.add("config");
 		modules.add("import");
 		modules.add("redeem");
 		modules.add("orgs");
 		modules.add("repos");
 		modules.add("clean");
 		modules.add("environments");
 		modules.add("facts");
 		modules.add("identity");
 		modules.add("list");
 		modules.add("refresh");
 		modules.add("register");
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
 		
 		// MODULE: config
 		module = "config";
 		List <String> configOptions = new ArrayList<String>();
 		configOptions.add("-h, --help");
 		configOptions.add("--list");
 		configOptions.add("--remove=REMOVE");
 		configOptions.add("--server.ca_cert_dir=SERVER.CA_CERT_DIR");
 		configOptions.add("--server.hostname=SERVER.HOSTNAME");
 		configOptions.add("--server.insecure=SERVER.INSECURE");
 		configOptions.add("--server.port=SERVER.PORT");
 		configOptions.add("--server.prefix=SERVER.PREFIX");
 		configOptions.add("--server.proxy_hostname=SERVER.PROXY_HOSTNAME");
 		configOptions.add("--server.proxy_password=SERVER.PROXY_PASSWORD");
 		configOptions.add("--server.proxy_port=SERVER.PROXY_PORT");
 		configOptions.add("--server.proxy_user=SERVER.PROXY_USER");
 		configOptions.add("--server.repo_ca_cert=SERVER.REPO_CA_CERT");
 		configOptions.add("--server.ssl_verify_depth=SERVER.SSL_VERIFY_DEPTH");
 		configOptions.add("--rhsm.baseurl=RHSM.BASEURL");
 		configOptions.add("--rhsm.ca_cert_dir=RHSM.CA_CERT_DIR");
 		configOptions.add("--rhsm.consumercertdir=RHSM.CONSUMERCERTDIR");
 		configOptions.add("--rhsm.entitlementcertdir=RHSM.ENTITLEMENTCERTDIR");
 		configOptions.add("--rhsm.hostname=RHSM.HOSTNAME");
 		configOptions.add("--rhsm.insecure=RHSM.INSECURE");
 		configOptions.add("--rhsm.port=RHSM.PORT");
 		configOptions.add("--rhsm.prefix=RHSM.PREFIX");
 		configOptions.add("--rhsm.productcertdir=RHSM.PRODUCTCERTDIR");
 		configOptions.add("--rhsm.proxy_hostname=RHSM.PROXY_HOSTNAME");
 		configOptions.add("--rhsm.proxy_password=RHSM.PROXY_PASSWORD");
 		configOptions.add("--rhsm.proxy_port=RHSM.PROXY_PORT");
 		configOptions.add("--rhsm.proxy_user=RHSM.PROXY_USER");
 		configOptions.add("--rhsm.repo_ca_cert=RHSM.REPO_CA_CERT");
 		configOptions.add("--rhsm.ssl_verify_depth=RHSM.SSL_VERIFY_DEPTH");
 		configOptions.add("--rhsmcertd.ca_cert_dir=RHSMCERTD.CA_CERT_DIR");
 		configOptions.add("--rhsmcertd.certfrequency=RHSMCERTD.CERTFREQUENCY");
 		configOptions.add("--rhsmcertd.healfrequency=RHSMCERTD.HEALFREQUENCY");
 		configOptions.add("--rhsmcertd.hostname=RHSMCERTD.HOSTNAME");
 		configOptions.add("--rhsmcertd.insecure=RHSMCERTD.INSECURE");
 		configOptions.add("--rhsmcertd.port=RHSMCERTD.PORT");
 		configOptions.add("--rhsmcertd.prefix=RHSMCERTD.PREFIX");
 		configOptions.add("--rhsmcertd.proxy_hostname=RHSMCERTD.PROXY_HOSTNAME");
 		configOptions.add("--rhsmcertd.proxy_password=RHSMCERTD.PROXY_PASSWORD");
 		configOptions.add("--rhsmcertd.proxy_port=RHSMCERTD.PROXY_PORT");
 		configOptions.add("--rhsmcertd.proxy_user=RHSMCERTD.PROXY_USER");
 		configOptions.add("--rhsmcertd.repo_ca_cert=RHSMCERTD.REPO_CA_CERT");
 		configOptions.add("--rhsmcertd.ssl_verify_depth=RHSMCERTD.SSL_VERIFY_DEPTH");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, configOptions}));
 		}
 		
 		// MODULE: import
 		module = "import";
 		List <String> importOptions = new ArrayList<String>();
 		importOptions.add("-h, --help");
 		//importOptions.add("--certificate=CERTIFICATE_FILES");	// prior to fix for Bug 735212
 		importOptions.add("--certificate=CERTIFICATE_FILE");
 		// TEMPORARY WORKAROUND FOR BUG: https://bugzilla.redhat.com/show_bug.cgi?id=733873
 		boolean invokeWorkaroundWhileBugIsOpen = true;
 		String bugId="733873"; 
 		try {if (invokeWorkaroundWhileBugIsOpen&&BzChecker.getInstance().isBugOpen(bugId)) {log.fine("Invoking workaround for "+BzChecker.getInstance().getBugState(bugId).toString()+" Bugzilla bug "+bugId+".  (https://bugzilla.redhat.com/show_bug.cgi?id="+bugId+")");} else {invokeWorkaroundWhileBugIsOpen=false;}} catch (XmlRpcException xre) {/* ignore exception */} catch (RuntimeException re) {/* ignore exception */}
 		if (invokeWorkaroundWhileBugIsOpen) {
 			importOptions.add("--proxy=PROXY_URL");
 			importOptions.add("--proxyuser=PROXY_USER");
 			importOptions.add("--proxypassword=PROXY_PASSWORD");
 		}
 		// END OF WORKAROUND
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, importOptions}));
 		}
 
 		// MODULE: redeem
 		module = "redeem";
 		List <String> redeemOptions = new ArrayList<String>();
 		redeemOptions.add("-h, --help");
 		redeemOptions.add("--email=EMAIL");
 		redeemOptions.add("--locale=LOCALE");
 		redeemOptions.add("--proxy=PROXY_URL");
 		redeemOptions.add("--proxyuser=PROXY_USER");
 		redeemOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, redeemOptions}));
 		}
 		
 		// MODULE: orgs
 		module = "orgs";
 		List <String> orgsOptions = new ArrayList<String>();
 		orgsOptions.add("-h, --help");
 		orgsOptions.add("--username=USERNAME");
 		orgsOptions.add("--password=PASSWORD");
 		orgsOptions.add("--proxy=PROXY_URL");
 		orgsOptions.add("--proxyuser=PROXY_USER");
 		orgsOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, orgsOptions}));
 		}
 		
 		// MODULE: repos
 		module = "repos";
 		List <String> reposOptions = new ArrayList<String>();
 		reposOptions.add("-h, --help");
 		reposOptions.add("--list");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, reposOptions}));
 		}
 		
 		// MODULE: clean
 		module = "clean";
 		List <String> cleanOptions = new ArrayList<String>();
 		cleanOptions.add("-h, --help");
 		// removed in https://bugzilla.redhat.com/show_bug.cgi?id=664581
 		//cleanOptions.add("--proxy=PROXY_URL");
 		//cleanOptions.add("--proxyuser=PROXY_USER");
 		//cleanOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("664581"), smHelpCommand, optionsRegex, cleanOptions}));
 		}
 		
 		// MODULE: environments
 		module = "environments";
 		List <String> environmentsOptions = new ArrayList<String>();
 		environmentsOptions.add("-h, --help");
 		environmentsOptions.add("--username=USERNAME");
 		environmentsOptions.add("--password=PASSWORD");
 		environmentsOptions.add("--org=ORG");
 		environmentsOptions.add("--proxy=PROXY_URL");
 		environmentsOptions.add("--proxyuser=PROXY_USER");
 		environmentsOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, environmentsOptions}));
 		}
 		
 		// MODULE: facts
 		module = "facts";
 		List <String> factsOptions = new ArrayList<String>();
 		factsOptions.add("-h, --help");
 		factsOptions.add("--list");
 		factsOptions.add("--update");
 		factsOptions.add("--proxy=PROXY_URL");
 		factsOptions.add("--proxyuser=PROXY_USER");
 		factsOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, factsOptions}));
 		}
 		
 		// MODULE: identity
 		module = "identity";
 		List <String> identityOptions = new ArrayList<String>();
 		identityOptions.add("-h, --help");
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
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, identityOptions}));
 		}
 		
 		// MODULE: list
 		module = "list";
 		List <String> listOptions = new ArrayList<String>();
 		listOptions.add("-h, --help");
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
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, listOptions}));
 		}
 		
 		// MODULE: refresh
 		module = "refresh";
 		List <String> refreshOptions = new ArrayList<String>();
 		refreshOptions.add("-h, --help");
 		refreshOptions.add("--proxy=PROXY_URL");
 		refreshOptions.add("--proxyuser=PROXY_USER");
 		refreshOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, refreshOptions}));
 		}
 		
 		// MODULE: register
 		module = "register";
 		List <String> registerOptions = new ArrayList<String>();
 		registerOptions.add("-h, --help");
 		registerOptions.add("--username=USERNAME");
 		registerOptions.add("--type=CONSUMERTYPE");
 		registerOptions.add("--name=CONSUMERNAME");
 		registerOptions.add("--password=PASSWORD");
 		registerOptions.add("--consumerid=CONSUMERID");
 		registerOptions.add("--org=ORG");
 		registerOptions.add("--environment=ENVIRONMENT");
 		registerOptions.add("--autosubscribe");
 		registerOptions.add("--force");
 		registerOptions.add("--activationkey=ACTIVATION_KEYS");
 		registerOptions.add("--proxy=PROXY_URL");
 		registerOptions.add("--proxyuser=PROXY_USER");
 		registerOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[]{null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[]{new BlockedByBzBug("628589"), smHelpCommand, optionsRegex, registerOptions}));
 		}
 		
 		// MODULE: subscribe
 		module = "subscribe";
 		List <String> subscribeOptions = new ArrayList<String>();
 		subscribeOptions.add("-h, --help");
 		subscribeOptions.add("--pool=POOL");
 		subscribeOptions.add("--quantity=QUANTITY");
 		subscribeOptions.add("--auto");	// result of https://bugzilla.redhat.com/show_bug.cgi?id=680399
 		//subscribeOptions.add("--regtoken=REGTOKEN");	// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 		//subscribeOptions.add("--email=EMAIL");			// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 		//subscribeOptions.add("--locale=LOCALE");		// https://bugzilla.redhat.com/show_bug.cgi?id=670823
 		subscribeOptions.add("--proxy=PROXY_URL");
 		subscribeOptions.add("--proxyuser=PROXY_USER");
 		subscribeOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, subscribeOptions}));
 		}
 		
 		// MODULE: unregister
 		module = "unregister";
 		List <String> unregisterOptions = new ArrayList<String>();
 		unregisterOptions.add("-h, --help");
 		unregisterOptions.add("--proxy=PROXY_URL");
 		unregisterOptions.add("--proxyuser=PROXY_USER");
 		unregisterOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, unregisterOptions}));
 		}
 		
 		// MODULE: unsubscribe
 		module = "unsubscribe";
 		List <String> unsubscribeOptions = new ArrayList<String>();
 		unsubscribeOptions.add("-h, --help");
 		unsubscribeOptions.add("--serial=SERIAL");
 		unsubscribeOptions.add("--all");
 		unsubscribeOptions.add("--proxy=PROXY_URL");
 		unsubscribeOptions.add("--proxyuser=PROXY_USER");
 		unsubscribeOptions.add("--proxypassword=PROXY_PASSWORD");
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h "+module,clienttasks.command+" --help "+module}) {
 			List <String> usages = new ArrayList<String>();
 			String usage = "Usage: "+clienttasks.command+" "+module+" [OPTIONS]";
 			if (clienttasks.redhatRelease.contains("release 5")) usage = usage.replaceFirst("^Usage", "usage"); // TOLERATE WORKAROUND FOR Bug 693527 ON RHEL5
 			usages.add(usage);
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$", usages}));
 			ll.add(Arrays.asList(new Object[] {null, smHelpCommand, optionsRegex, unsubscribeOptions}));
 		}
 		
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="TranslatedCommandLineHelpData")
 	public Object[][] getTranslatedCommandLineHelpDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getTranslatedCommandLineHelpDataAsListOfLists());
 	}
 	protected List<List<Object>> getTranslatedCommandLineHelpDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		if (clienttasks==null) return ll;
 		String usage,lang,module;
 		
 		// MODULES
 		for (String smHelpCommand : new String[]{clienttasks.command+" -h",clienttasks.command+" --help"}) {
 
 			// # for L in en_US de_DE es_ES fr_FR it_IT ja_JP ko_KR pt_BR ru_RU zh_CN zh_TW as_IN bn_IN hi_IN mr_IN gu_IN kn_IN ml_IN or_IN pa_IN ta_IN te_IN; do echo ""; echo "# LANG=$L subscription-manager --help | grep -- --help"; LANG=$L subscription-manager  --help | grep -- --help; done;
 			
 			// TODO new BlockedByBzBug("707080")
 			lang = "en_US"; usage = "(U|u)sage: subscription-manager [options] MODULENAME --help";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "de_DE"; usage = "(V|v)erbrauch: subscription-manager [options] MODULENAME --help";		ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "es_ES"; usage = "(U|u)so: subscription-manager [opciones] NOMBREDEMÓDULO --help";		ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "fr_FR"; usage = "(U|u)tilisation: subscription-manager [options] MODULENAME --help";	ll.add(Arrays.asList(new Object[] {new BlockedByBzBug(new String[]{"707080","743734","743732"}), lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "it_IT"; usage = "(U|u)tilizzo: subscription-manager [options] MODULENAME --help";		ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ja_JP"; usage = "使用法: subscription-manager [オプション] モジュール名 --help";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ko_KR"; usage = "사용법: subscription-manager [options] MODULENAME --help";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pt_BR"; usage = "(U|u)so: subscription-manager [options] MODULENAME --help";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ru_RU"; usage = "(Ф|ф)ормат: subscription-manager [параметры] МОДУЛЬ --help";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_CN"; usage = "使用: subscription-manager [options] MODULENAME --help";				ll.add(Arrays.asList(new Object[] {new BlockedByBzBug(new String[]{"707080","743732"}), lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_TW"; usage = "使用方法：subscription-manager [options] MODULENAME --help";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "as_IN"; usage = "ব্যৱহাৰ: subscription-manager [বিকল্পসমূহ] MODULENAME --help";				ll.add(Arrays.asList(new Object[] {new BlockedByBzBug(new String[]{"743732","750807"}), lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "bn_IN"; usage = "ব্যবহারপ্রণালী: subscription-manager [options] MODULENAME --help";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "hi_IN"; usage = "प्रयोग: subscription-manager [options] MODULENAME --help";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "mr_IN"; usage = "(वापर|वपार): subscription-manager [options] MODULENAME --help";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "gu_IN"; usage = "વપરાશ: subscription-manager [options] MODULENAME --help";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "kn_IN"; usage = "ಬಳಕೆ: subscription-manager [options] MODULENAME --help";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ml_IN"; usage = "ഉപയോഗിയ്ക്കേണ്ട വിധം: subscription-manager [options] MODULENAME --help";	ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "or_IN"; usage = "ବ୍ଯବହାର ବିଧି: subscription-manager [options] MODULENAME --help";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pa_IN"; usage = "ਵਰਤੋਂ: subscription-manager [options] MODULENAME --help";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ta_IN"; usage = "பயன்பாடு: subscription-manager [விருப்பங்கள்] MODULENAME --help";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "te_IN"; usage = "వాడుక: subscription-manager [options] MODULENAME --help";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			
 			// TODO MODULE: clean
 			// TODO MODULE: activate
 			// TODO MODULE: facts
 			// TODO MODULE: identity
 			// TODO MODULE: list
 			// TODO MODULE: refresh
 			
 			// MODULE: register
 			module = "register";
 			lang = "en_US"; usage = "(U|u)sage: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "de_DE"; usage = "(V|v)erbrauch: subscription-manager register [OPTIONS]";		ll.add(Arrays.asList(new Object[] {new BlockedByBzBug("693527"), lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "es_ES"; usage = "(U|u)so: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "fr_FR"; usage = "(U|u)tilisation : subscription-manager register [OPTIONS]";	ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "it_IT"; usage = "(U|u)tilizzo: subscription-manager register [OPTIONS]";		ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ja_JP"; usage = "使用法: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ko_KR"; usage = "사용법: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pt_BR"; usage = "(U|u)so: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ru_RU"; usage = "(Ф|ф)ормат: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_CN"; usage = "使用：subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "zh_TW"; usage = "使用方法：subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "as_IN"; usage = "ব্যৱহাৰ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {new BlockedByBzBug(new String[]{"743732"}), lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "bn_IN"; usage = "ব্যবহারপ্রণালী: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "hi_IN"; usage = "प्रयोग: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "mr_IN"; usage = "(वापर|वपार): subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "gu_IN"; usage = "વપરાશ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "kn_IN"; usage = "ಬಳಕೆ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ml_IN"; usage = "ഉപയോഗിയ്ക്കേണ്ട വിധം: subscription-manager register [OPTIONS]";	ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			//lang = "or_IN"; usage = "ବ୍ଯବହାର ବିଧି: subscription-manager register [OPTIONS]";			ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));	//rhel57 rhel61
 			lang = "or_IN"; usage = "ଉପଯୋଗ: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "pa_IN"; usage = "ਵਰਤੋਂ: subscription-manager register [OPTIONS]";					ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "ta_IN"; usage = "பயன்பாடு: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 			lang = "te_IN"; usage = "వా‍డుక: subscription-manager register [OPTIONS]";				ll.add(Arrays.asList(new Object[] {null, lang, smHelpCommand+" "+module, newList(usage.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]")+"$")}));
 
 			// TODO MODULE: subscribe
 			// TODO MODULE: unregister
 			// TODO MODULE: unsubscribe
 		}
 		
 		return ll;
 	}
 }
