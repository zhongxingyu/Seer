 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SessionUpgrade.java,v 1.1 2007-08-25 02:21:23 sridharev Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.authentication.AuthTestConfigUtil;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.authentication.AuthContext;
 import com.sun.identity.authentication.spi.AuthLoginException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.Reporter;
 import org.testng.annotations.Test;
 
 /**
  * This class <code>SessionUpgrade</code>.
  * Performs Session Upgrade tests.Session Upgrade is Login to the
  * first module instance and use the token create AuthContext and
  * Login to the second module instance should give the AuthType 
  * secondmoduleinstance|firstmoduleinstance.
  * 
  */
 public class SessionUpgrade extends TestCommon {
     
     private ResourceBundle testResources;
     private String testModules;
     private static String indexName = "";
     private static AuthContext.IndexType indexType;
     private static String orgName;
     private String url;
     private boolean userExists;
     private String createUserProp;
     private String FirstModuleName;
     private String FirstModuleUserName;
     private String FirstModulePassword;
     private String SecondModuleName;
     private String SecondModuleUserName;
     private String SecondModulePassword;
     private String configrbName = "authenticationConfigData";
     private WebClient webClient;
     private List moduleConfigData;
     private String moduleServiceName;
     private String moduleSubConfig;
     private String moduleSubConfigId;
     private String userName;
     private String password;
     private String logoutURL;
     private FederationManager fm;
     private List<String> testUserList = new ArrayList<String>();
     private String strVerify;
     
     /**
      * Default Constructor
      */
     public SessionUpgrade() {
         super("SessionUpgrade");
         url = protocol + ":" + "//" + host + ":" + port + uri;
         logoutURL = url + "/UI/Logout";
         fm = new FederationManager(url);
         testResources = ResourceBundle.getBundle("SessionUpgrade");
     }
     
     /**
      * Set up the system for testing.Create authentication instances
      * create users before testing the session Upgrade feature
      */
     @BeforeClass(groups={"ds_ds","ds_ds_sec"})
     public void setup() 
     throws Exception {
         entering("setup", null);
         webClient = new WebClient();
         try {
             testModules = testResources.getString("am-auth-test-modules");
             orgName = testResources.getString("am-auth-test-realm");
             FirstModuleName = testResources.getString("am-auth-session" +
                     "-test-firstmodule");
             FirstModuleUserName = testResources.getString("am-auth-session" +
                     "-test-firstuser");
             FirstModulePassword = testResources.getString("am-auth-session-" +
                     "test-firstpasswd");
             SecondModuleName = testResources.getString("am-auth-session-" +
                     "test-secondmodule");
             SecondModuleUserName = testResources.getString("am-auth-session-" +
                     "test-seconduser");
             SecondModulePassword = testResources.getString("am-auth-session-" +
                     "test-secondpasswd");
             log(Level.FINEST, "setup", "testModules " + testModules);
             log(Level.FINEST, "setup", "OrgName " + orgName);
             log(Level.FINEST, "setup", "FirstModuleName: " + FirstModuleName);
             log(Level.FINEST, "setup", "SecondModuleName: " + SecondModuleName);
             log(Level.FINEST, "setup", "FirstModuleUserName: " + FirstModuleUserName);
             log(Level.FINEST, "setup", "FirstModuleUserPass: " + FirstModulePassword);
             log(Level.FINEST, "setup", "SecondModuleUserName: " + SecondModuleUserName);
             log(Level.FINEST, "setup", "SecondModuleUserPass: " + SecondModulePassword);
             Reporter.log("testModuleNames " + testModules);
             Reporter.log("OrgName " + orgName);
             Reporter.log("FirstModuleName: " + FirstModuleName);
             Reporter.log("SecondModuleName: " + SecondModuleName);
             Reporter.log("FirstModuleUserName: " + FirstModuleUserName);
             Reporter.log("FirstModuleUserPass: " + FirstModulePassword);
             Reporter.log("SecondModuleUserName: " + SecondModuleUserName);
             Reporter.log("SecondModuleUserPass: " + SecondModulePassword);
             StringTokenizer moduleTokens = new StringTokenizer(testModules, ",");
             strVerify = SecondModuleName + "|" + FirstModuleName;
             List<String> moduleList = getListFromTokens(moduleTokens);
             for (String modName: moduleList) {
                 createModule(modName);
                 log(Level.FINEST, "setup", "Module Created");
                 createUserProp = testResources.getString("am-auth-test-" +
                         modName + "-createTestUser");
                 userName = testResources.getString("am-auth-test-" +
                         modName + "-user");
                 userName.trim();
                 password = testResources.getString("am-auth-test-" +
                         modName + "-password");
                 password.trim();
                 userExists = new Boolean(createUserProp).booleanValue();
                 if (!userExists) {
                     createUser(userName, password);
                 }
             }
         } catch(Exception e) {
             cleanup();
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
         exiting("setup");
     }
     
     /**
      * Tests for SessionUpgrade by login into the system using correct
      * credentials for two different modules
      */
     @Test(groups={"ds_ds","ds_ds_sec"})
     public void testSessionUpgrade()
     throws Exception {
         AuthContext lc = null;
         Callback[] callbacks = null;
         Callback[] acallback = null;
         try {
             lc = new AuthContext(orgName);
             indexType = AuthContext.IndexType.MODULE_INSTANCE;
             indexName = FirstModuleName;
             lc.login(indexType, indexName);
         } catch (AuthLoginException le) {
             log(Level.SEVERE, "testSessionUpgrade", le.getMessage());
             le.printStackTrace();
             return;
         }
         while (lc.hasMoreRequirements()) {
             callbacks = lc.getRequirements();
             if (callbacks != null) {
                 try {
                     for (int i = 0; i < callbacks.length; i++) {
                         if (callbacks[i] instanceof NameCallback) {
                             NameCallback namecallback =
                                     (NameCallback)callbacks[i];
                             namecallback.setName(FirstModuleUserName);
                         }
                         if (callbacks[i] instanceof PasswordCallback) {
                             PasswordCallback passwordcallback =
                                     (PasswordCallback)callbacks[i];
                             passwordcallback.setPassword(
                                     FirstModulePassword.toCharArray());
                         }
                     }
                     lc.submitRequirements(callbacks);
                 } catch (Exception e) {
                     log(Level.SEVERE, "testSessionUpgrade", e.getMessage());
                     e.printStackTrace();
                     return;
                 }
             }
         }
         if (lc.getStatus() == AuthContext.Status.SUCCESS) {
             try {
                 SSOToken obtainedToken = lc.getSSOToken();
                 AuthContext newlc = new AuthContext(obtainedToken);
                 newlc.login(AuthContext.IndexType.MODULE_INSTANCE, 
                         SecondModuleName);
                 while (newlc.hasMoreRequirements()) {
                     acallback = newlc.getRequirements();
                     if (callbacks != null) {
                         try {
                             for (int i = 0; i < acallback.length; i++){
                                 if (acallback[i] instanceof NameCallback) {
                                     NameCallback namecallback =
                                             (NameCallback)acallback[i];
                                     namecallback.setName(SecondModuleUserName);
                                 }
                                 if (acallback[i] instanceof PasswordCallback) {
                                     PasswordCallback passwordcallback =
                                             (PasswordCallback)acallback[i];
                                     passwordcallback.setPassword(
                                             SecondModulePassword.toCharArray());
                                 }
                             }
                             newlc.submitRequirements(acallback);
                         } catch (Exception e) {
                             log(Level.SEVERE, "testSessionUpgrade", 
                                     e.getMessage());
                             return;
                         }
                     }
                 }
                 if (newlc.getStatus() == AuthContext.Status.SUCCESS) {
                     SSOToken upgradedToken = newlc.getSSOToken();
                     assert (upgradedToken.getProperty("AuthType").equals(strVerify));
                 }
             } catch (Exception ex) {
                 log(Level.SEVERE, "testSessionUpgrade", ex.getMessage());
                 ex.printStackTrace();
                 throw ex;
             }
         }
     }
     
     /**
      * Performs cleanup after tests are done.
      * Deletes the authentication instances and users created
      * by this test scenario
      */
     @AfterClass(groups={"ds_ds","ds_ds_sec"})
     public void cleanup()
     throws Exception {
         entering("cleanup", null);
         WebClient webClient = new WebClient();
         try {
             log(Level.FINEST, "cleanup", url);
             log(Level.FINEST, "cleanup", "Users:" + testUserList);
             List<String> listModInstance = new ArrayList<String>();
             listModInstance.add(moduleSubConfig);
             log(Level.FINEST, "cleanup", "listModInstance" + listModInstance);
             consoleLogin(webClient, url, adminUser, adminPassword);
             HtmlPage page = (HtmlPage)fm.deleteAuthInstances(webClient,
                     realm, listModInstance);
             log(Level.FINEST, "cleanup", "Page" + page.asXml());
             fm.deleteIdentities(webClient, realm, testUserList, "User");
         } catch(Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             consoleLogout(webClient, logoutURL);
         }
         exiting("cleanup");
     }
     
     /**
      * Creates the required test users on the system for each
      * Chain to be executed
      * @param user
      * @param password
      **/
     private void createUser(String newUser, String userpassword) {
         List<String> userList = new ArrayList<String>();
         userList.add("sn=" + newUser);
         userList.add("cn=" + newUser);
         userList.add("userpassword=" + userpassword);
         userList.add("inetuserstatus=Active");
         log(Level.FINEST, "createUser", "userList " + userList);
         testUserList.add(newUser);
         try {
             AuthTestConfigUtil userConfig =
                     new AuthTestConfigUtil(configrbName);
             userConfig.setTestConfigRealm(orgName);
             userConfig.createUser(userList, newUser);
         } catch(Exception e) {
             log(Level.SEVERE, "createUsers", e.getMessage());
             e.printStackTrace();
            throw e;
         }
     }
     
     /**
      * Calls Authentication Utility class to create the module instances
      * for a given module instance name
      * @param moduleName
      */
     private void createModule(String mName) {
         try {
             AuthTestConfigUtil moduleConfig =
                     new AuthTestConfigUtil(configrbName);
             Map modMap = moduleConfig.getModuleData(mName);
             moduleServiceName = moduleConfig.getModuleServiceName();
             moduleSubConfig = moduleConfig.getModuleSubConfigName();
             moduleSubConfigId = moduleConfig.getModuleSubConfigId();
             moduleConfigData = moduleConfig.getListFromMap(modMap, mName);
             log(Level.FINEST, "createModule", "ModuleServiceName :" +
                     moduleServiceName);
             log(Level.FINEST, "createModule", "ModuleSubConfig :" +
                     moduleSubConfig);
             log(Level.FINEST, "createModule", "ModuleSubConfigId :" +
                     moduleSubConfigId);
             moduleConfig.createModuleInstances(moduleServiceName,
                     moduleSubConfig, moduleConfigData, moduleSubConfigId);
         } catch(Exception e) {
             log(Level.SEVERE, "createModule", e.getMessage());
             e.printStackTrace();
            throw e;
         }
     }
 }
        
