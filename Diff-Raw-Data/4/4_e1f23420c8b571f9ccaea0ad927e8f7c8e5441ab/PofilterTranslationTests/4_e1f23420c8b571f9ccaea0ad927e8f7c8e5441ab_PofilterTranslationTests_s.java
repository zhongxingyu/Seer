 package com.redhat.qe.sm.cli.tests;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import com.redhat.qe.Assert;
 import com.redhat.qe.auto.bugzilla.BlockedByBzBug;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.sm.base.SubscriptionManagerCLITestScript;
 import com.redhat.qe.sm.data.Translation;
 import com.redhat.qe.tools.RemoteFileTasks;
 import com.redhat.qe.tools.SSHCommandResult;
 import com.redhat.qe.tools.SSHCommandRunner;
 //import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;
 
 /**
  * @author fsharath
  * @author jsefler
  * @References
  *   Engineering Localization Services: https://home.corp.redhat.com/node/53593
  *   http://git.fedorahosted.org/git/?p=subscription-manager.git;a=blob;f=po/pt.po;h=0854212f4fab348a25f0542625df343653a4a097;hb=RHEL6.3
  *   Here is the raw rhsm.po file for LANG=pt
  *   http://git.fedorahosted.org/git/?p=subscription-manager.git;a=blob;f=po/pt.po;hb=RHEL6.3
  *   
  *   https://engineering.redhat.com/trac/LocalizationServices
  *   https://engineering.redhat.com/trac/LocalizationServices/wiki/L10nRHEL6LanguageSupportCriteria
  *   
  *   https://translate.zanata.org/zanata/project/view/subscription-manager/iter/0.99.X/stats
  *   
  *   https://fedora.transifex.net/projects/p/fedora/
  *   
  *   http://translate.sourceforge.net/wiki/
  *   http://translate.sourceforge.net/wiki/toolkit/index
  *   http://translate.sourceforge.net/wiki/toolkit/pofilter
  *   http://translate.sourceforge.net/wiki/toolkit/pofilter_tests
  *   http://translate.sourceforge.net/wiki/toolkit/installation
  *   
  *   https://github.com/translate/translate
  *   
  *   Translation Bug Reporting Process
  *   https://engineering.redhat.com/trac/LocalizationServices/wiki/L10nBugReportingProcess
  *   
  *   Contacts:
  *   hi_IN: aalam@redhat.com
  *   or_IN: Manoj Giri <mgiri@redhat.com> <qe-india@redhat.com>
  *   
  **/
 @Test(groups={"PofilterTranslationTests"})
 public class PofilterTranslationTests extends SubscriptionManagerCLITestScript {
 	
 	
 	// Test Methods ***********************************************************************
 
 	@Test(	description="run pofilter translate tests on subscription manager translation files",
 			dataProvider="getSubscriptionManagerTranslationFilePofilterTestData",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void subscriptionManagerPofilter_Test(Object bugzilla, String pofilterTest, File translationFile) {
 		pofilter_Test(client, pofilterTest, translationFile);
 	}
 	
 	@Test(	description="run pofilter translate tests on candlepin translation files",
 			dataProvider="getCandlepinTranslationFilePofilterTestData",
 			groups={},
 			enabled=true)
 	//@ImplementsNitrateTest(caseId=)
 	public void candlepinPofilter_Test(Object bugzilla, String pofilterTest, File translationFile) {
 		pofilter_Test(server, pofilterTest, translationFile);
 	}
 	
 
 	// Candidates for an automated Test:
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	// Configuration Methods ***********************************************************************
 	@BeforeClass(groups={"setup"})
 	public void setupBeforeClass() {
 		// for debugging purposes, load a reduced list of pofilterTests
 		if (!getProperty("sm.debug.pofilterTests", "").equals("")) 	pofilterTests = Arrays.asList(getProperty("sm.debug.pofilterTests", "").trim().split(" *, *"));
 	}
 
 
 	
 	// Protected Methods ***********************************************************************
 	
 	// see http://translate.sourceforge.net/wiki/toolkit/pofilter_tests
 	//	Critical -- can break a program
 	//    	accelerators, escapes, newlines, nplurals, printf, tabs, variables, xmltags, dialogsizes
 	//	Functional -- may confuse the user
 	//    	acronyms, blank, emails, filepaths, functions, gconf, kdecomments, long, musttranslatewords, notranslatewords, numbers, options, purepunc, sentencecount, short, spellcheck, urls, unchanged
 	//	Cosmetic -- make it look better
 	//    	brackets, doublequoting, doublespacing, doublewords, endpunc, endwhitespace, puncspacing, simplecaps, simpleplurals, startcaps, singlequoting, startpunc, startwhitespace, validchars
 	//	Extraction -- useful mainly for extracting certain types of string
 	//    	compendiumconflicts, credits, hassuggestion, isfuzzy, isreview, untranslated
 	protected List<String> pofilterTests = Arrays.asList(
 			//	Critical -- can break a program
 			"accelerators", "escapes", "newlines", /*nplurals,*/ "printf", "tabs", "variables", "xmltags", /*dialogsizes,*/
 			//	Functional -- may confuse the user
 			/*acronyms,*/ "blank", "emails", "filepaths", /*functions,*/ "gconf", /*kdecomments,*/ "long", /*musttranslatewords,*/ "notranslatewords", /*numbers,*/ "options", /*purepunc,*/ /*sentencecount,*/ "short", /*spellcheck,*/ "urls", "unchanged",
 			//	Cosmetic -- make it look better
 			/*brackets, doublequoting, doublespacing,*/ "doublewords", /*endpunc, endwhitespace, puncspacing, simplecaps, simpleplurals, startcaps, singlequoting, startpunc, startwhitespace, validchars */
 			//	Extraction -- useful mainly for extracting certain types of string
 			/*compendiumconflicts, credits, hassuggestion, isfuzzy, isreview,*/ "untranslated");
 	
 	
 	protected void pofilter_Test(SSHCommandRunner sshCommandRunner, String pofilterTest, File translationFile) {
 		log.info("For an explanation of pofilter test '"+pofilterTest+"', see: http://translate.sourceforge.net/wiki/toolkit/pofilter_tests");
 		File translationPoFile = new File(translationFile.getPath().replaceFirst(".mo$", ".po"));
 		List<String> ignorableMsgIds = new ArrayList<String>();
 		
 		// if pofilter test -> notranslatewords, create a file with words that don't have to be translated to native language
 		final String notranslateFile = "/tmp/notranslatefile";
 		if (pofilterTest.equals("notranslatewords")) {
 			
 			// The words that need not be translated can be added this list
 			List<String> notranslateWords = Arrays.asList("Red Hat","subscription-manager","python-rhsm");
 			
 			// remove former notranslate file
 			sshCommandRunner.runCommandAndWait("rm -f "+notranslateFile);
 					
 			// echo all of the notranslateWords to the notranslateFile
 			for(String str : notranslateWords) {
 				String echoCommand = "echo \""+str+"\" >> "+notranslateFile;
 				sshCommandRunner.runCommandAndWait(echoCommand);
 			}
 			Assert.assertTrue(RemoteFileTasks.testExists(sshCommandRunner, notranslateFile),"The pofilter notranslate file '"+notranslateFile+"' has been created on the client.");
 		}
 		
 		// execute the pofilter test
 		String pofilterCommand = "pofilter --gnome -t "+pofilterTest;
 		if (pofilterTest.equals("notranslatewords")) pofilterCommand += " --notranslatefile="+notranslateFile;
 		SSHCommandResult pofilterResult = sshCommandRunner.runCommandAndWait(pofilterCommand+" "+translationPoFile);
 		Assert.assertEquals(pofilterResult.getExitCode(), new Integer(0), "Successfully executed the pofilter tests.");
 		
 		// convert the pofilter test results into a list of failed Translation objects for simplified handling of special cases 
 		List<Translation> pofilterFailedTranslations = Translation.parse(pofilterResult.getStdout());
 		
 		
 		// remove the first translation which contains only meta data
 		if (!pofilterFailedTranslations.isEmpty() && pofilterFailedTranslations.get(0).msgid.equals("")) pofilterFailedTranslations.remove(0);
 		
 		
 		// ignore the following special cases of acceptable results for each of the pofilterTests..........
 				
 		if (pofilterTest.equals("accelerators")) {
 			if (translationFile.getPath().contains("/hi/")) ignorableMsgIds = Arrays.asList("proxy url in the form of proxy_hostname:proxy_port");
 			if (translationFile.getPath().contains("/ru/")) ignorableMsgIds = Arrays.asList("proxy url in the form of proxy_hostname:proxy_port","%%prog %s [OPTIONS] CERT_FILE");
 		}
 		
 		if (pofilterTest.equals("newlines")) {
 			ignorableMsgIds = Arrays.asList(
 					"Optional language to use for email notification when subscription redemption is complete. Examples: en-us, de-de",
 					"\n"+"Unable to register.\n"+"For further assistance, please contact Red Hat Global Support Services.",
 					"Tip: Forgot your login or password? Look it up at http://red.ht/lost_password",
 					"Unable to perform refresh due to the following exception: %s",
 					""+"This migration script requires the system to be registered to RHN Classic.\n"+"However this system appears to be registered to '%s'.\n"+"Exiting.",
 					"The tool you are using is attempting to re-register using RHN Certificate-Based technology. Red Hat recommends (except in a few cases) that customers only register with RHN once.",
 					// bug 825397	""+"Redeeming the subscription may take a few minutes.\n"+"Please provide an email address to receive notification\n"+"when the redemption is complete.",	// the Subscription Redemption dialog actually expands to accommodate the message, therefore we could ignore it	// bug 825397 should fix this
 					// bug 825388	""+"We have detected that you have multiple service level\n"+"agreements on various products. Please select how you\n"+"want them assigned.", // bug 825388 or 825397 should fix this
 					"\n"+"This machine appears to be already registered to Certificate-based RHN.  Exiting.",
 					"\n"+"This machine appears to be already registered to Red Hat Subscription Management.  Exiting.");	
 		}
 		
 		if (pofilterTest.equals("xmltags")) { 
 			Boolean match = false; 
 			for(Translation pofilterFailedTranslation : pofilterFailedTranslations) {
 				// Parsing mgID and msgStr for XMLTags
 				Pattern xmlTags = Pattern.compile("<.+?>");  
 				Matcher tagsMsgID = xmlTags.matcher(pofilterFailedTranslation.msgid);
 				Matcher tagsMsgStr = xmlTags.matcher(pofilterFailedTranslation.msgstr);
 				// Populating a msgID tags into a list
 				ArrayList<String> msgIDTags = new ArrayList<String>();
 				while(tagsMsgID.find()) {
 					msgIDTags.add(tagsMsgID.group());
 				}
 				// Sorting an list of msgID tags
 				ArrayList<String> msgIDTagsSort = new ArrayList<String>(msgIDTags);
 				Collections.sort(msgIDTagsSort);
 				// Populating a msgStr tags into a list
 				ArrayList<String> msgStrTags = new ArrayList<String>();
 				while(tagsMsgStr.find()) {
 					msgStrTags.add(tagsMsgStr.group());
 				}
 				// Sorting an list of msgStr tags
 				ArrayList<String> msgStrTagsSort = new ArrayList<String>(msgStrTags);
 				Collections.sort(msgStrTagsSort);
 				// Verifying whether XMLtags are opened and closed appropriately 
 				// If the above condition holds, then check for XML Tag ordering
 				if(msgIDTagsSort.equals(msgStrTagsSort) && msgIDTagsSort.size() == msgStrTagsSort.size()) {
 					int size = msgIDTags.size(),count=0;
 					// Stack to hold XML tags
 					Stack<String> stackMsgIDTags = new Stack<String>();
 					Stack<String> stackMsgStrTags = new Stack<String>();
 					// Temporary stack to hold popped elements
 					Stack<String> tempStackMsgIDTags = new Stack<String>();
 					Stack<String> tempStackMsgStrTags = new Stack<String>();
 					while(count< size) {
 						// If it's not a close tag push into stack
 						if(!msgIDTags.get(count).contains("/")) stackMsgIDTags.push(msgIDTags.get(count));
 						else {
 							if(checkTags(stackMsgIDTags,tempStackMsgIDTags,msgIDTags.get(count))) match = true;
 							else {
 								// If an open XMLtag doesn't have an appropriate close tag exit loop
 								match = false;
 								break;
 							}
 						}
 						// If it's not a close tag push into stack
 						if(!msgStrTags.get(count).contains("/")) stackMsgStrTags.push(msgStrTags.get(count));
 						else {
 							if(checkTags(stackMsgStrTags,tempStackMsgStrTags,msgStrTags.get(count))) match = true;
 							else {
 								// If an open XMLtag doesn't have an appropriate close tag exit loop
 								match = false;
 								break;
 							}
 						}
 						// Incrementing count to point to the next element
 						count++;
 					}
 				}
 				if(match) ignorableMsgIds.add(pofilterFailedTranslation.msgid);
 			}
 		}
 		
 		if (pofilterTest.equals("filepaths")) {
 			for(Translation pofilterFailedTranslation : pofilterFailedTranslations) {
 				// Parsing mgID and msgStr for FilePaths ending ' ' (space) 
 				Pattern filePath = Pattern.compile("/.*?( |$)", Pattern.MULTILINE);  
 				Matcher filePathMsgID = filePath.matcher(pofilterFailedTranslation.msgid);
 				Matcher filePathMsgStr = filePath.matcher(pofilterFailedTranslation.msgstr);
 				ArrayList<String> filePathsInID = new ArrayList<String>();
 				ArrayList<String> filePathsInStr = new ArrayList<String>();
 				// Reading the filePaths into a list
 				while(filePathMsgID.find()) {
 					filePathsInID.add(filePathMsgID.group());
 				}
 				while(filePathMsgStr.find()) {
 					filePathsInStr.add(filePathMsgStr.group());
 				}
 				// If the lists are equal in size, then compare the contents of msdID->filePath and msgStr->filePath
 				//if(filePathsInID.size() == filePathsInStr.size()) {
 					for(int i=0;i<filePathsInID.size();i++) {
 						// If the msgID->filePath ends with '.', remove '.' and compare with msgStr->filePath
 						if(filePathsInID.get(i).trim().startsWith("//")) {
 							ignorableMsgIds.add(pofilterFailedTranslation.msgid);
 							continue;
 						}
 						//contains("//")) ignoreMsgIDs.add(pofilterFailedTranslation.msgid);
 						if(filePathsInID.get(i).trim().charAt(filePathsInID.get(i).trim().length()-1) == '.') {
 							String filePathID = filePathsInID.get(i).trim().substring(0, filePathsInID.get(i).trim().length()-1);
 							if(filePathID.equals(filePathsInStr.get(i).trim())) ignorableMsgIds.add(pofilterFailedTranslation.msgid);
 						}
 						/*else {
 							if(filePathsInID.get(i).trim().equals(filePathsInStr.get(i).trim())) ignoreMsgIDs.add(pofilterFailedTranslation.msgid);
 						}*/
 					}
 				//}
 			}
 		}
 		
 		// TODO remove or comment this ignore case once the msgID is corrected 
 		// error: 		msgid "Error: you must register or specify --username and password to list service levels"
 		// rectified:	msgid "Error: you must register or specify --username and --password to list service levels"
 		if (pofilterTest.equals("options")) {
 			ignorableMsgIds = Arrays.asList("Error: you must register or specify --username and password to list service levels");
 		}
 		
 		if (pofilterTest.equals("short")) {
 			ignorableMsgIds = Arrays.asList("No","Yes","Key","Value","N/A","None","Number");
 		}
 		
 		if (pofilterTest.equals("doublewords")) {
 			// common doublewords in the translation to ignore for all langs
 			ignorableMsgIds.addAll(Arrays.asList("Subscription Subscriptions Box","Subscription Subscriptions Label"));
 
 			// doublewords in the translation to ignore for specific langs
 			if((translationFile.getPath().contains("/pa/"))) ignorableMsgIds.addAll(Arrays.asList("Server URL can not be None"));
 			if((translationFile.getPath().contains("/hi/"))) ignorableMsgIds.addAll(Arrays.asList("Server URL can not be None"));	// more info in bug 861095
 			if((translationFile.getPath().contains("/fr/"))) ignorableMsgIds.addAll(Arrays.asList("The Subscription Management Service you register with will provide your system with updates and allow additional management."));	// msgstr "Le service de gestion des abonnements « Subscription Management » avec lequel vous vous enregistrez fournira à votre système des mises à jour et permettra une gestion supplémentaire."
 			if((translationFile.getPath().contains("/or/"))) ignorableMsgIds.addAll(Arrays.asList("Run the initial checks immediately, with no delay.","Run the initial checks immediatly, with no delay."));
 		}
 		
 		if (pofilterTest.equals("unchanged")) {
 			// common unchanged translations to ignore for all langs
 			ignorableMsgIds.addAll(Arrays.asList("registration_dialog_action_area","server_label","server_entry","proxy_button","hostname[:port][/prefix]","default_button","choose_server_label","<b>SKU:</b>","%prog [options]","%prog [OPTIONS]","%%prog %s [OPTIONS]","%%prog %s [OPTIONS] CERT_FILE"/*SEE BUG 845304*/,"<b>HTTP Proxy</b>","<b>python-rhsm version:</b> %s","<b>python-rhsm Version:</b> %s","close_button","facts_view","register_button","register_dialog_main_vbox","registration_dialog_action_area\n","prod 1, prod2, prod 3, prod 4, prod 5, prod 6, prod 7, prod 8","%s of %s","floating-point","integer","long integer","Copyright (c) 2012 Red Hat, Inc.","RHN Classic","env_select_vbox_label","environment_treeview","no_subs_label","org_selection_label","org_selection_scrolledwindow","owner_treeview","progress_label","subscription-manager: %s","python-rhsm: %s","register_details_label","register_progressbar","system_instructions_label","system_name_label","connectionStatusLabel",""+"\n"+"This software is licensed to you under the GNU General Public License, version 2 (GPLv2). There is NO WARRANTY for this software, express or implied, including the implied warranties of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2 along with this software; if not, see:\n"+"\n"+"http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt\n"+"\n"+"Red Hat trademarks are not licensed under GPLv2. No permission is granted to use or replicate Red Hat trademarks that are incorporated in this software or its documentation.\n","progress_label","Red Hat Subscription Manager", "Red Hat Subscription Validity Applet"));
 
 			// unchanged translations to ignore for specific langs
 			if (translationFile.getPath().contains("/bn_IN/")) ignorableMsgIds.addAll(Arrays.asList("Red Hat Subscription Validity Applet","Subscription Validity Applet"));
 			if (translationFile.getPath().contains("/ta_IN/")) ignorableMsgIds.addAll(Arrays.asList("Org: ","org id: %s","Repo Id:              \\t%s","Repo Url:             \\t%s"));
 			if (translationFile.getPath().contains("/pt_BR/")) ignorableMsgIds.addAll(Arrays.asList("Org: ","org id: %s","<b>subscription management service version:</b> %s","Status","Status:               \\t%s","Login:","Virtual","_Help","hostname[:port][/prefix]","virtual"));
 			if (translationFile.getPath().contains("/de_DE/")) ignorableMsgIds.addAll(Arrays.asList("Subscription Manager","Red Hat account: ","Account","<b>Account:</b>","Account:              \\t%s","<b>Subscription Management Service Version:</b> %s","<b>subscription management service version:</b> %s","Login:","Name","Name:                 \\t%s","Status","Status:               \\t%s","Version","Version:              \\t%s","_System","long integer","name: %s","label","Label","Name: %s","Release: %s","integer","Tags","Org: "));
 			if (translationFile.getPath().contains("/es_ES/")) ignorableMsgIds.addAll(Arrays.asList("Org: ","Serial","No","%s: error: %s"));
 			if (translationFile.getPath().contains("/te/"))    ignorableMsgIds.addAll(Arrays.asList("page 2"));
 			if (translationFile.getPath().contains("/pa/"))    ignorableMsgIds.addAll(Arrays.asList("<b>python-rhsm version:</b> %s"));
 			if (translationFile.getPath().contains("/fr/"))    ignorableMsgIds.addAll(Arrays.asList("Options","options","Type","Arch","Version","page 2"));
 			if (translationFile.getPath().contains("/it/"))    ignorableMsgIds.addAll(Arrays.asList("Org: ","Account","<b>Account:</b>","Account:              \\t%s","<b>Arch:</b>","Arch:                 \\t%s","Arch","Login:","No","Password:","Release: %s","Password: "));
 			
 		}
 		
 		if (pofilterTest.equals("urls")) {
 			if(translationFile.getPath().contains("/zh_CN/")) ignorableMsgIds.addAll(Arrays.asList("Server URL has an invalid scheme. http:// and https:// are supported"));
 		}
 		
 		// pluck out the ignorable pofilter test results
 		for (String msgid : ignorableMsgIds) {
 			Translation ignoreTranslation = Translation.findFirstInstanceWithMatchingFieldFromList("msgid", msgid, pofilterFailedTranslations);
 			if (ignoreTranslation!=null) {
 				log.info("Ignoring result of pofiliter test '"+pofilterTest+"' for msgid: "+ignoreTranslation.msgid);
 				pofilterFailedTranslations.remove(ignoreTranslation);
 			}
 		}
 		
 		// for convenience reading the logs, log warnings for the failed pofilter test results (when some of the failed test are being ignored)
 		if (!ignorableMsgIds.isEmpty()) for (Translation pofilterFailedTranslation : pofilterFailedTranslations) {
 			log.warning("Failed result of pofiliter test '"+pofilterTest+"' for translation: "+pofilterFailedTranslation);
 		}
 		
 		// assert that there are no failed pofilter translation test results remaining after the ignorable pofilter test results have been plucked out
 		Assert.assertEquals(pofilterFailedTranslations.size(),0, "Discounting the ignored test results, the number of failed pofilter '"+pofilterTest+"' tests for translation file '"+translationFile+"'.");
 	}
 	
 	
 	/**
 	 * @param str
 	 * @return the tag character (Eg: <b> or </b> return_val = 'b')
 	 */
 	protected char parseElement(String str){
 		return str.charAt(str.length()-2);
 	}
 	
 	/**
 	 * @param tags
 	 * @param temp
 	 * @param tagElement
 	 * @return whether every open tag has an appropriate close tag in order
 	 */
 	protected Boolean checkTags(Stack<String> tags, Stack<String> temp, String tagElement) {
 		// If there are no open tags in the stack -> return
 		if(tags.empty()) return false;
 		String popElement = tags.pop();
 		// If openTag in stack = closeTag -> return  
 		if(!popElement.contains("/") && parseElement(popElement) == parseElement(tagElement)) return true;
 		else {
 			// Continue popping elements from stack and push to temp until appropriate open tag is found
 			while(popElement.contains("/") || parseElement(popElement) != parseElement(tagElement)) {
 				temp.push(popElement);
 				// If stack = empty and no match, push back the popped elements -> return
 				if(tags.empty()) {
 					while(!temp.empty()) {
 						tags.push(temp.pop());
 					}
 					return false;
 				}
 				popElement = tags.pop();
 			}
 			// If a match is found, push back the popped elements -> return
 			while(!temp.empty()) tags.push(temp.pop());
 		}
 		return true;
 	}
 	
 	
 	
 	// Data Providers ***********************************************************************
 	
 	@DataProvider(name="getSubscriptionManagerTranslationFilePofilterTestData")
 	public Object[][] getSubscriptionManagerTranslationFilePofilterTestDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getSubscriptionManagerTranslationFilePofilterTestDataAsListOfLists());
 	}
 	protected List<List<Object>> getSubscriptionManagerTranslationFilePofilterTestDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		// Client side
 		Map<File,List<Translation>> translationFileMapForSubscriptionManager = buildTranslationFileMapForSubscriptionManager();
 		for (File translationFile : translationFileMapForSubscriptionManager.keySet()) {
 			for (String pofilterTest : pofilterTests) {
 				Set<String> bugIds = new HashSet<String>();
 
 				// Bug 825362	[es_ES] failed pofilter accelerator tests for subscription-manager translations 
 				if (pofilterTest.equals("accelerators") && translationFile.getPath().contains("/es_ES/")) bugIds.add("825362");
 				// Bug 825367	[zh_CN] failed pofilter accelerator tests for subscription-manager translations 
 				if (pofilterTest.equals("accelerators") && translationFile.getPath().contains("/zh_CN/")) bugIds.add("825367");
 				// Bug 860084 - [ja_JP] two accelerators for msgid "Configure Pro_xy"
 				if (pofilterTest.equals("accelerators") && translationFile.getPath().contains("/ja/")) bugIds.add("860084");
 				
 				
 				// Bug 825397	Many translated languages fail the pofilter newlines test
 				if (pofilterTest.equals("newlines") && !(translationFile.getPath().contains("/zh_CN/")||translationFile.getPath().contains("/ru/")||translationFile.getPath().contains("/ja/"))) bugIds.add("825397");			
 				// Bug 825393	[ml_IN][es_ES] translations should not use character ¶ for a new line. 
 				if (pofilterTest.equals("newlines") && translationFile.getPath().contains("/ml/")) bugIds.add("825393");
 				if (pofilterTest.equals("newlines") && translationFile.getPath().contains("/es_ES/")) bugIds.add("825393");
 				
 				
 				// Bug 827059	[kn] translation fails for printf test
 				if (pofilterTest.equals("printf") && translationFile.getPath().contains("/kn/")) bugIds.add("827059");
 				// Bug 827079 	[es-ES] translation fails for printf test
 				if (pofilterTest.equals("printf") && translationFile.getPath().contains("/es_ES/")) bugIds.add("827079");
 				// Bug 827085	[hi] translation fails for printf test
 				if (pofilterTest.equals("printf") && translationFile.getPath().contains("/hi/")) bugIds.add("827085");
 				// Bug 827089 	[hi] translation fails for printf test
 				if (pofilterTest.equals("printf") && translationFile.getPath().contains("/te/")) bugIds.add("827089");
 				
 				
 				// Bug 827113 	Many Translated languages fail the pofilter tabs test
 				if (pofilterTest.equals("tabs") && !(translationFile.getPath().contains("/pa/")||translationFile.getPath().contains("/mr/")||translationFile.getPath().contains("/de_DE/")||translationFile.getPath().contains("/bn_IN/"))) bugIds.add("825397");
 				
 				
 				// Bug 827161 	[bn_IN] failed pofilter xmltags tests for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/bn_IN/")) bugIds.add("827161");
 				// Bug 827208	[or] failed pofilter xmltags tests for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/or/")) bugIds.add("827208");
 				// Bug 827214	[or] failed pofilter xmltags tests for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/ta_IN/")) bugIds.add("827214");
 				// Bug 828368 - [kn] failed pofilter xmltags tests for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/kn/")) bugIds.add("828368");
 				// Bug 828365 - [kn] failed pofilter xmltags tests for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/kn/")) bugIds.add("828365");
 				// Bug 828372 - [es_ES] failed pofilter xmltags tests for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/es_ES/")) bugIds.add("828372");
 				// Bug 828416 - [ru] failed pofilter xmltags tests for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/ru/")) bugIds.add("828416");
 				// Bug 843113 - [ta_IN] failed pofilter xmltags test for subscription-manager translations
 				if (pofilterTest.equals("xmltags") && translationFile.getPath().contains("/ta_IN/")) bugIds.add("843113");
 				
 				
 				// Bug 828566	[bn-IN] cosmetic bug for subscription-manager translations
 				if (pofilterTest.equals("filepaths") && translationFile.getPath().contains("/bn_IN/")) bugIds.add("828566");
 				// Bug 828567 	[as] cosmetic bug for subscription-manager translations
 				if (pofilterTest.equals("filepaths") && translationFile.getPath().contains("/as/")) bugIds.add("828567");
 				// Bug 828576 - [ta_IN] failed pofilter filepaths tests for subscription-manager translations
 				if (pofilterTest.equals("filepaths") && translationFile.getPath().contains("/ta_IN/")) bugIds.add("828576");
 				// Bug 828579 - [ml] failed pofilter filepaths tests for subscription-manager translations
 				if (pofilterTest.equals("filepaths") && translationFile.getPath().contains("/ml/")) bugIds.add("828579");
 				// Bug 828580 - [mr] failed pofilter filepaths tests for subscription-manager translations
 				if (pofilterTest.equals("filepaths") && translationFile.getPath().contains("/mr/")) bugIds.add("828580");
 				// Bug 828583 - [ko] failed pofilter filepaths tests for subscription-manager translations
 				if (pofilterTest.equals("filepaths") && translationFile.getPath().contains("/ko/")) bugIds.add("828583");
 				
 				
 				// Bug 828810 - [kn] failed pofilter varialbes tests for subscription-manager translations
 				if (pofilterTest.equals("variables") && translationFile.getPath().contains("/kn/")) bugIds.add("828810");
 				// Bug 828816 - [es_ES] failed pofilter varialbes tests for subscription-manager translations
 				if (pofilterTest.equals("variables") && translationFile.getPath().contains("/es_ES/")) bugIds.add("828816");
 				// Bug 828821 - [hi] failed pofilter varialbes tests for subscription-manager translations
 				if (pofilterTest.equals("variables") && translationFile.getPath().contains("/hi/")) bugIds.add("828821");
 				// Bug 828867 - [te] failed pofilter varialbes tests for subscription-manager translations
 				if (pofilterTest.equals("variables") && translationFile.getPath().contains("/te/")) bugIds.add("828867");
 				
 				
 				// Bug 828903 - [bn_IN] failed pofilter options tests for subscription-manager translations 
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/bn_IN/")) bugIds.add("828903");
 				// Bug 828930 - [as] failed pofilter options tests for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/as/")) bugIds.add("828930");
 				// Bug 828948 - [or] failed pofilter options tests for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/or/")) bugIds.add("828948");
 				// Bug 828954 - [ta_IN] failed pofilter options test for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/ta_IN/")) bugIds.add("828954");
 				// Bug 828958 - [pt_BR] failed pofilter options test for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/pt_BR/")) bugIds.add("828958");
 				// Bug 828961 - [gu] failed pofilter options test for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/gu/")) bugIds.add("828961");
 				// Bug 828965 - [hi] failed pofilter options test for subscription-manager trasnlations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/hi/")) bugIds.add("828965");
 				// Bug 828966 - [zh_CN] failed pofilter options test for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/zh_CN/")) bugIds.add("828966");
 				// Bug 828969 - [te] failed pofilter options test for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/te/")) bugIds.add("828969");
 				// Bug 842898 - [it] failed pofilter options test for subscription-manager translations
 				if (pofilterTest.equals("options") && translationFile.getPath().contains("/it/")) bugIds.add("842898");
 				
 				
 				// Bug 828985 - [ml] failed pofilter urls test for subscription manager translations
 				if (pofilterTest.equals("urls") && translationFile.getPath().contains("/ml/")) bugIds.add("828985");
 				// Bug 828989 - [pt_BR] failed pofilter urls test for subscription-manager translations
 				if (pofilterTest.equals("urls") && translationFile.getPath().contains("/pt_BR/")) bugIds.add("828989");
 				// Bug 860088 - [de_DE] translation for a url should not be altered 
 				if (pofilterTest.equals("urls") && translationFile.getPath().contains("/de_DE/")) bugIds.add("860088");
 				
 				
 				// Bug 845304 - translation of the word "[OPTIONS]" has reverted
 				if (pofilterTest.equals("unchanged")) bugIds.add("845304");
 				// Bug 829459 - [bn_IN] failed pofilter unchanged option test for subscription-manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/bn_IN/")) bugIds.add("829459");
 				// Bug 829470 - [or] failed pofilter unchanged options for subscription-manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/or/")) bugIds.add("829470");
 				// Bug 829471 - [ta_IN] failed pofilter unchanged optioon test for subscription-manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/ta_IN/")) bugIds.add("829471");
 				// Bug 829476 - [ml] failed pofilter unchanged option test for subscription-manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/ml/")) bugIds.add("829476");
 				// Bug 829479 - [pt_BR] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/pt_BR/")) {bugIds.add("829479"); bugIds.add("828958");}
 				// Bug 829482 - [zh_TW] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/zh_TW/")) bugIds.add("829482");
 				// Bug 829483 - [de_DE] failed pofilter unchanged options test for suubscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/de_DE/")) bugIds.add("829483");
 				// Bug 829486 - [fr] failed pofilter unchanged options test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/fr/")) bugIds.add("829486");
 				// Bug 829488 - [es_ES] failed pofilter unchanged option tests for subscription manager translations				
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/es_ES/")) bugIds.add("829488");
 				// Bug 829491 - [it] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/it/")) bugIds.add("829491");
 				// Bug 829492 - [hi] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/hi/")) bugIds.add("829492");
 				// Bug 829494 - [zh_CN] failed pofilter unchanged option for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/zh_CN/")) bugIds.add("829494");
 				// Bug 829495 - [pa] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/pa/")) bugIds.add("829495");
 				// Bug 840914 - [te] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/te/")) bugIds.add("840914");
 				// Bug 840644 - [ta_IN] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/ta_IN/")) bugIds.add("840644");
 				// Bug 855087 -	[mr] missing translation for the word "OPTIONS" 
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/mr/")) bugIds.add("855087");
 				// Bug 855085 -	[as] missing translation for the word "OPTIONS" 
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/as/")) bugIds.add("855085");
 				// Bug 855081 -	[pt_BR] untranslated msgid "Arch" 
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/pt_BR/")) bugIds.add("855081");
				// Bug 864095 - [fr_FR] unchanged translation for msgid "System Engine Username: " 
				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/fr/")) bugIds.add("864095");
 				// Bug 864092 - [es_ES] unchanged translation for msgid "Configure Proxy" 
 				if (pofilterTest.equals("unchanged") && translationFile.getPath().contains("/es_ES/")) bugIds.add("864092");
 				
 				// Bug 841011 - [kn] failed pofilter unchanged option test for subscription manager translations
 				if (pofilterTest.equals("doublewords") && translationFile.getPath().contains("/kn/")) bugIds.add("841011");
 				// Bug 861095 - [hi_IN] duplicate words appear in two msgid translations 
 				if (pofilterTest.equals("doublewords") && translationFile.getPath().contains("/hi/")) bugIds.add("861095");
 				
 				
 				BlockedByBzBug blockedByBzBug = new BlockedByBzBug(bugIds.toArray(new String[]{}));
 				ll.add(Arrays.asList(new Object[] {blockedByBzBug, pofilterTest, translationFile}));
 				 
 			}
 		}
 		
 		return ll;
 	}
 	
 	
 	
 	@DataProvider(name="getCandlepinTranslationFilePofilterTestData")
 	public Object[][] getCandlepinTranslationFilePofilterTestDataAs2dArray() {
 		return TestNGUtils.convertListOfListsTo2dArray(getCandlepinTranslationFilePofilterTestDataAsListOfLists());
 	}
 	protected List<List<Object>> getCandlepinTranslationFilePofilterTestDataAsListOfLists() {
 		List<List<Object>> ll = new ArrayList<List<Object>>();
 		
 		// Server side
 		Map<File,List<Translation>> translationFileMapForCandlepin = buildTranslationFileMapForCandlepin();
 		for (File translationFile : translationFileMapForCandlepin.keySet()) {
 			for (String pofilterTest : pofilterTests) {
 				BlockedByBzBug bugzilla = null;
 				
 				// Bug 842450 - [ja_JP] failed pofilter newlines option test for candlepin translations
 				if (pofilterTest.equals("newlines") && translationFile.getName().equals("ja.po")) bugzilla = new BlockedByBzBug("842450");
 				if (pofilterTest.equals("tabs") && translationFile.getName().equals("ja.po")) bugzilla = new BlockedByBzBug("842450");
 				
 				// Bug 842784 - [ALL LANG] failed pofilter untranslated option test for candlepin translations
 				if (pofilterTest.equals("untranslated")) bugzilla = new BlockedByBzBug("842784");
 				
 				// Bug 865561 - [pa_IN] the pofilter escapes test is failing on msgid "Consumer Type with id {0} could not be found."
 				if (pofilterTest.equals("escapes")) bugzilla = new BlockedByBzBug("865561");
 				
 				
 				ll.add(Arrays.asList(new Object[] {bugzilla, pofilterTest, translationFile}));
 			}
 		}
 		
 		return ll;
 	}
 	
 }
