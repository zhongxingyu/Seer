 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.sourcesense.alfresco.opensso;
 
 import java.util.Properties;
 
 import org.junit.Test;
 
 import com.thoughtworks.selenium.SeleneseTestCase;
 import com.thoughtworks.selenium.SeleniumException;
 
 public class WebClientSSOIntegrationTest extends SeleneseTestCase {
 
 	private String agent_password;
 	private String agent_login;
 	private String opensso_url;
 
 	public void setUp() throws Exception {
 		Properties properties = new Properties();
 		properties.load(getClass().getClassLoader().getResourceAsStream("AMConfig.properties"));
 		agent_login = (String) properties.get("com.sun.identity.agents.app.username");
 		agent_password = (String) properties.get("com.iplanet.am.service.password");
 		String openssoNaming = (String) properties.get("com.iplanet.am.naming.url");
 		opensso_url = openssoNaming.substring(0, openssoNaming.lastIndexOf('/')).concat("/UI/Login");
 
 		setUp(opensso_url, "*firefox3");
 		
 		try {
 			loginToOpenSSOConsoleAsAmAdmin();
 			deleteUsersAndGroups();
 		} catch (SeleniumException ex) {
 			System.out.println("Groups and Users already cleaned");
 		}
 	}

 	

 	@Test
 	public void testSSO() throws Exception {
 		loginToOpenSSOConsoleAsAmAdmin();
 
 		createUserWithLoginAndPass("opensso1");
 		createUserWithLoginAndPass("opensso2");
 		createUserWithLoginAndPass("admin");
 
 		createGroup("group1");
 		createGroup("group2");
 
 		associateUserWithGroups("opensso1", "group1");
 		associateUserWithGroups("opensso2", "group1", "group2");
 
 		logoutFromOpenSSODomain();
 
 		loginToAlfrescoAs("opensso1", "opensso1");
 		assertTrue(selenium.isTextPresent("opensso1"));
 
 		logoutFromOpenSSODomain();
 
 		loginToAlfrescoAs("opensso2", "opensso2");
 		assertTrue(selenium.isTextPresent("opensso2"));
 
 		logoutFromOpenSSODomain();
 
 		loginToAlfrescoAs("admin", "admin");
 
 		goToAlfrescoGroupManagmentFor("group1");
 
 		assertTrue(selenium.isTextPresent("opensso1"));
 		assertTrue(selenium.isTextPresent("opensso2"));
 
 		goToAlfrescoGroupManagmentFor("group2");
 
 		assertTrue(selenium.isTextPresent("opensso2"));
 		assertTrue(!selenium.isTextPresent("opensso1"));
 
 	}
 
 	private void goToAlfrescoGroupManagmentFor(String group) {
 		selenium.click("//img[@alt='Administration Console']");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("link=Manage User Groups");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("link=Root Groups");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("link=".concat(group));
 		selenium.waitForPageToLoad("40000");
 	}
 
 	private void logoutFromOpenSSODomain() {
 		selenium.open("/opensso/UI/Logout");
 	}
 
 	private void loginToAlfrescoAs(String user, String passwd) {
 		selenium.open("http://localhost:8080/alfresco/");
 		selenium.type("IDToken1", user);
 		selenium.type("IDToken2", passwd);
 		selenium.click("Login.Submit");
 		selenium.waitForPageToLoad("40000");
 
 	}
 
 	private void associateUserWithGroups(String user, String... groups) throws InterruptedException {
 		goToSubjectsPage();
 		selenium.waitForPageToLoad("40000");
 		selenium.click("link=".concat(user));
 		selenium.waitForPageToLoad("40000");
 		selenium.click("link=Group");
 		selenium.waitForPageToLoad("40000");
 		for (int second = 0;; second++) {
 			if (second >= 60)
 				fail("timeout");
 			try {
 				if (selenium.isTextPresent("Available"))
 					break;
 			} catch (Exception e) {
 			}
 			Thread.sleep(1000);
 		}
 
 		for (int i = 0; i < groups.length; i++) {
 			selenium.click("EntityMembership.addRemoveMembers.RemoveAllButton");
 			selenium.removeSelection("EntityMembership.addRemoveMembers.AvailableListBox", "label=administration");
 			selenium.addSelection("EntityMembership.addRemoveMembers.AvailableListBox", "label=".concat(groups[i]));
 			selenium.click("EntityMembership.addRemoveMembers.AddButton");
 			selenium.click("EntityMembership.button1");
 			selenium.waitForPageToLoad("40000");
 		}
 
 		selenium.click("EntityMembership.button3");
 		selenium.waitForPageToLoad("40000");
 
 	}
 
 	private void goToSubjectsPage() throws InterruptedException {
 		selenium.open("/opensso/task/Home");
 		clickOnLinkWithText("Access Control");
 		clickOnLinkWithText("/ (Top Level Realm)");
 		clickOnLinkWithText("Subjects");
 	}
 
 	private void deleteUsersAndGroups() throws InterruptedException {
 		goToSubjectsPage();
 		selenium.type("Entities.tfFilter", "opensso*");
 		selenium.click("Entities.btnSearch");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("Entities.tblSearch.SelectionCheckbox0");
 		selenium.click("Entities.tblSearch.SelectionCheckbox1");
 		selenium.click("Entities.tblButtonDelete");
 		selenium.waitForPageToLoad("40000");
 		selenium.type("Entities.tfFilter", "admin");
 		selenium.click("Entities.btnSearch");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("Entities.tblSearch.SelectionCheckbox0");
 		selenium.click("Entities.tblButtonDelete");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("link=Group");
 		selenium.waitForPageToLoad("40000");
 		selenium.type("Entities.tfFilter", "group*");
 		selenium.click("Entities.btnSearch");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("Entities.tblSearch.SelectionCheckbox0");
 		selenium.click("Entities.tblSearch.SelectionCheckbox1");
 		selenium.click("Entities.tblButtonDelete");
 		selenium.waitForPageToLoad("40000");
 		logoutFromOpenSSODomain();
 	}
 
 	private void createGroup(String groupName) throws InterruptedException {
 		goToSubjectsPage();
 		selenium.click("link=Group");
 		selenium.waitForPageToLoad("40000");
 		selenium.waitForPageToLoad("40000");
 		selenium.click("Entities.tblButtonAdd");
 		selenium.waitForPageToLoad("40000");
 		selenium.type("psLbl1", groupName);
 		selenium.click("EntityAdd.button1");
 		selenium.waitForPageToLoad("40000");
 	}
 
 	private void createUserWithLoginAndPass(String login) throws InterruptedException {
 		goToSubjectsPage();
 		selenium.click("Entities.tblButtonAdd");
 		selenium.waitForPageToLoad("40000");
 		selenium.type("psLbl1", login);
 		selenium.type("psLbl2", login);
 		selenium.type("psLbl3", login);
 		selenium.type("psLbl4", login);
 		selenium.type("psLbl5", login);
 		selenium.type("EntityAdd.userpassword_confirm", login);
 		selenium.click("EntityAdd.button1");
 		selenium.waitForPageToLoad("40000");
 
 	}
 
 	private void loginToOpenSSOConsoleAsAmAdmin() {
 		logoutFromOpenSSODomain();
 		selenium.open("/opensso/UI/Login");
 		selenium.type("IDToken1", "amAdmin");
 		selenium.type("IDToken2", "amAdmin");
 		selenium.click("Login.Submit");
 		selenium.waitForPageToLoad("40000");
 	}
 	
 	private void clickOnLinkWithText(String label) throws InterruptedException {
 		for (int second = 0;; second++) {
 			if (second >= 60)
 				fail("timeout");
 			try {
 				if (selenium.isTextPresent(label))
 					break;
 			} catch (Exception e) {
 			}
 			Thread.sleep(1000);
 		}
 
 		selenium.click("link=".concat(label));
 		selenium.waitForPageToLoad("40000");
 	}
 }
