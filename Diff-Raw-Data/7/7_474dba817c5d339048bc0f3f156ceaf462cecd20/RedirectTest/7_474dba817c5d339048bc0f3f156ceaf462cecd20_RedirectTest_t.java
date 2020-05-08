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
 * $Id: RedirectTest.java,v 1.9 2008-07-02 20:58:26 cmwesley Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.authentication.AuthTestConfigUtil;
 import com.sun.identity.qatest.common.authentication.AuthTestsValidator;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import org.testng.Reporter;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 /**
  * Each RedirectTest will have a module instance name and the realm
  * associated with it to perform the test.
  * This class does the following :
  * - Create the realm
  * - Create the module instances for that realm
  * - Create users to Login if required to create.
  * - Validates for module based authentication pass and failure case
  * - Validates for the goto and gotoOnFail URL for this instance.
  */
 public class RedirectTest extends TestCommon {
     
     private ResourceBundle testResources;
     private String moduleOnSuccess;
     private String moduleOnFail;
     private String modulePassMsg;
     private String moduleFailMsg;
     private String moduleGotoPassMsg;
     private String moduleOnFailPassMsg;
     private String createUserProp;
     private boolean userExists;
     private String cleanupFlag;
     private boolean debug;
     private String userName;
     private String password;
     private String redirectURL;
     private String uniqueIdentifier;
     private List moduleConfigData;
     private String moduleServiceName;
     private String moduleSubConfig;
     private String moduleSubConfigId;
     private String configrbName = "authenticationConfigData";
     private List<String> testUserList = new ArrayList<String>();
     private boolean isValidTest = true;
     private AuthTestConfigUtil moduleConfig;
     
     /**
      * Default Constructor
      **/
     public RedirectTest() {
         super("RedirectTest");
         moduleConfig = new AuthTestConfigUtil(configrbName);        
     }
         
     /**
      * Reads the necessary test configuration and prepares the system
      * for module/goto/gotoOnFail redirection tests.The following are done
      * in the setup
      * - Create module instances
      * - Create realm
      * - Create Users , If needed
      */
     @Parameters({"testModule", "testRealm"})    
     @BeforeClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void setup(String testModule, String testRealm) 
     throws Exception {
         Object[] params = {testModule, testRealm};
         entering("setup", params);
         try {
             isValidTest = moduleConfig.isValidModuleTest(testModule);            
             if (isValidTest) {
                 testResources = ResourceBundle.getBundle("authentication" +
                         fileseparator + "RedirectTest");
                 moduleOnSuccess = testResources.getString("am-auth-test-" +
                         testModule + "-goto");
                 moduleOnFail = testResources.getString("am-auth-test-" +
                         testModule + "-gotoOnFail");
                 modulePassMsg = testResources.getString("am-auth-test-" +
                         testModule + "-module-passmsg");
                 moduleFailMsg = testResources.getString("am-auth-test-" +
                         testModule + "-module-failmsg");
                 moduleGotoPassMsg = testResources.getString("am-auth-test-" +
                         testModule + "-goto-passmsg");
                 moduleOnFailPassMsg = testResources.getString("am-auth-test-" +
                         testModule + "-gotoOnFail-passmsg");
                ResourceBundle configBundle = 
                        ResourceBundle.getBundle("authentication" + 
                        fileseparator + configrbName);
                 moduleSubConfig = configBundle.getString(testModule + 
                         ".module-subconfig-name");
                 userName = testResources.getString("am-auth-test-" +
                         testModule + "-user");
                 userName.trim();
                 password = testResources.getString("am-auth-test-" +
                         testModule + "-password");
                 password.trim();
                 cleanupFlag = testResources.getString("am-auth-test-debug");
                 debug = new Boolean(cleanupFlag).booleanValue();
                 log(Level.FINEST, "setup", "ModuleName: " + testModule);
                 log(Level.FINEST, "setup", "RealmName: " + testRealm);
                 log(Level.FINEST, "setup", "Success URL: " + moduleOnSuccess);
                 log(Level.FINEST, "setup", "Failure URL: " + moduleOnFail);
                 log(Level.FINEST, "setup", "modulePassMsg: " + modulePassMsg);
                 log(Level.FINEST, "setup", "moduleFailMsg: " + moduleFailMsg);
                 log(Level.FINEST, "setup", "modulePassMsg: " + moduleGotoPassMsg);
                 log(Level.FINEST, "setup", "modulePassMsg: " + moduleOnFailPassMsg);
                 log(Level.FINEST, "setup", "userName: " + userName);
                 log(Level.FINEST, "setup", "password: " + password);
                 log(Level.FINEST, "setup", "debug: " + debug);
                 Reporter.log("testModuleName: " + testModule);
                 Reporter.log("Realm: " + testRealm);
                 Reporter.log("testModuleName: " + moduleOnSuccess);
                 Reporter.log("moduleOnFail: " + moduleOnFail);
                 Reporter.log("modulePassMsg: " + modulePassMsg);
                 Reporter.log("moduleFailMsg: " + moduleFailMsg);
                 Reporter.log("moduleFailMsg: " + moduleGotoPassMsg);
                 Reporter.log("moduleFailMsg: " + moduleOnFailPassMsg);
                 Reporter.log("moduleuserName: " + userName);
                 Reporter.log("modulepassword: " + password);
                 Reporter.log("cleanupFlag: " + debug);
 
                 if (!testRealm.equals("/")) {
                     createTestRealm(testRealm);
                     uniqueIdentifier = testRealm + "_" + testModule;
                     redirectURL = getLoginURL(testRealm) + "&amp;"
                             + "module=" + moduleSubConfig;
                 } else {
                     redirectURL = getLoginURL(testRealm) + "?" + "module="
                             + moduleSubConfig;
                     uniqueIdentifier = "rootrealm" + "_" + testModule;
                 }
 
                 createModule(testRealm, testModule);
                 createUserProp = testResources.getString("am-auth-test-" +
                         testModule + "-createTestUser");
                 userExists = new Boolean(createUserProp).booleanValue();
                 testUserList.add(userName);
                 if (!userExists) {
                     createUser(testRealm, userName, password);
                 }
             } else {
                 log(Level.FINEST, "setup", "Skipping setup of " + testModule + 
                      " auth module test on a Windows based server");                
             }
         } catch (AssertionError ae) {
             log(Level.SEVERE, "setup", 
                     "Calling cleanup due to failed famadm exit code ...");
             cleanup(testModule, testRealm); 
             throw ae;
         }
         exiting("setup");
     }
     
     /*
      * Validate the module based authentication for the module
      * under test for correct user and password behaviour
      */
     @Parameters({"testModule", "testRealm"})    
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void validateModuleTestsPositive(String testModule, String testRealm)
     throws Exception {
         Object[] params = {testModule, testRealm};        
         entering("validateModuleTestsPositive", params);
         if (isValidTest) {
             Map executeMap = new HashMap();
             executeMap.put("redirectURL", redirectURL);
             executeMap.put("userName", userName);
             executeMap.put("password", password);
             executeMap.put("modulePassMsg", modulePassMsg);
             executeMap.put("moduleFailMsg", moduleFailMsg);
             executeMap.put("uniqueIdentifier", uniqueIdentifier);
             AuthTestsValidator authTestValidator =
                     new AuthTestsValidator(executeMap);
             authTestValidator.testModulebasedPostive();
         } else {
             log(Level.FINEST, "validateModuleTestsPositive", "Skipping " + 
                     testModule + " auth module test on a Windows based server");             
         }
         exiting("validateModuleTestsPositive");
     }
     
     /*
      * Validate the module based authentication for the module
      * under test for incorrect user and password behaviour
      */
     @Parameters({"testModule", "testRealm"})        
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void validateModuleTestsNegative(String testModule, String testRealm)
     throws Exception {
         Object[] params = {testModule, testRealm};
         entering("validateModuleTestsNegative", params);
         if (isValidTest) {        
             Map executeMap = new HashMap();
             executeMap.put("redirectURL", redirectURL);
             executeMap.put("userName", userName);
             executeMap.put("password", password);
             executeMap.put("modulePassMsg", modulePassMsg);
             executeMap.put("moduleFailMsg", moduleFailMsg);
             executeMap.put("uniqueIdentifier", uniqueIdentifier);
             AuthTestsValidator authTestValidator =
                     new AuthTestsValidator(executeMap);
             authTestValidator.testModulebasedNegative();
         } else {
             log(Level.FINEST, "validateModuleTestsNegative", "Skipping " + 
                     testModule + " auth module test on a Windows based server");             
         }
         exiting("validateModuleTestsNegative");
     }
     
     /*
      * Validate the module based authentication for the module
      * under test for "goto" param when auth success behaviour
      */
     @Parameters({"testModule", "testRealm"})        
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void validateGotoTests(String testModule, String testRealm)
     throws Exception {
         Object params[] = {testModule, testRealm};
         entering("validateGotoTests", params);
         if (isValidTest) {
             Map executeMap = new HashMap();
             String gotoURL = redirectURL + "&amp;goto=" + moduleOnSuccess;
             executeMap.put("redirectURL", redirectURL);
             executeMap.put("userName", userName);
             executeMap.put("password", password);
             executeMap.put("modulePassMsg", moduleGotoPassMsg);
             executeMap.put("moduleFailMsg", moduleOnFailPassMsg);
             executeMap.put("gotoURL", gotoURL);
             executeMap.put("uniqueIdentifier", uniqueIdentifier);
             AuthTestsValidator authTestValidator =
                     new AuthTestsValidator(executeMap);
             authTestValidator.testModuleGoto();
         } else {
             log(Level.FINEST, "validateGotoTests", "Skipping " + testModule + 
                     " auth module test on a Windows based server");             
         }
         exiting("validateGotoTests");
     }
     
     /*
      * Validate the module based authentication for the module
      * under test for "GotoOnfail" param with unsuccessful authentication
      * behaviour
      */
     @Parameters({"testModule", "testRealm"})       
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void validateGotoOnFailTests(String testModule, String testRealm)
     throws Exception {
         Object[] params = {testModule, testRealm};
         entering("validateGotoOnFailTests", params);
         if (isValidTest) {        
             Map executeMap = new HashMap();
             String gotoURL = redirectURL + "&amp;gotoOnFail=" + moduleOnFail;
             executeMap.put("redirectURL", redirectURL);
             executeMap.put("userName", userName);
             executeMap.put("password", password);
             executeMap.put("modulePassMsg", moduleGotoPassMsg);
             executeMap.put("moduleFailMsg", moduleOnFailPassMsg);
             executeMap.put("gotoURL", gotoURL);
             executeMap.put("uniqueIdentifier", uniqueIdentifier);
             AuthTestsValidator authTestValidator = 
                     new AuthTestsValidator(executeMap);
             authTestValidator.testModuleGotoOnFail();
         } else {
             log(Level.FINEST, "validateGotoOnFailTests", "Skipping " + 
                     testModule + " auth module test on a Windows based server");               
         }
         exiting("validateGotoOnFailTests");
     }
     
     /**
      * Clean up is called post execution of each module and realm .
      * This is done to maintain the system in a clean state
      * after executing each test scenario, in this case each module based
      * authentication tests for all possible cases.
      * This processed in this method are:
      * 1. Delete the authentication module
      * 2. Delete the realm involved only if it is not root realm
      * 3. Delete the users involved/created for this test if any.
      */
     @Parameters({"testModule", "testRealm"})        
     @AfterClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void cleanup(String testModule, String testRealm)
     throws Exception {
         Object[] params = {testModule, testRealm};
         entering("cleanup", params);
         if (isValidTest) {
             if (!debug) {
                 try {
                     log(Level.FINEST, "cleanup", "TestRealm: " + testRealm);
                     log(Level.FINEST, "cleanup", "TestModule: " + testModule);
                     Reporter.log("TestRealm: " + testRealm);
                     Reporter.log("TestModule: " + testModule);                
                     String url = getLoginURL("/"); 
                     String famadmURL  = protocol + ":" + "//" + host + ":"
                                     + port + uri ;
                     log(Level.FINE, "cleanup", "Login URL: " + url);
                     log(Level.FINE, "cleanup", "famadm.jsp URL" + famadmURL);
                     FederationManager am = new FederationManager(famadmURL);
                     WebClient webClient = new WebClient();
                     deleteModule(testRealm, testModule);                
                     consoleLogin(webClient, url, adminUser, adminPassword);
 
                     String delRealm = testRealm;
                     if (!testRealm.equals("/")) {
                         delRealm = "/" + testRealm;
                     }
                     if ((testUserList != null) && !testUserList.isEmpty()) {
                         log(Level.FINE, "cleanup", "Deleting user " + userName + 
                                 " from realm " + delRealm + " ...");
                         if (FederationManager.getExitCode(am.deleteIdentities(
                                 webClient, delRealm, testUserList, "User")) != 0) {
                             log(Level.SEVERE, "cleanup", 
                                     "deleteIdentities famadm command failed");
                         }
                     }
 
                     if (!testRealm.equals("/")) {
                         log(Level.FINE, "cleanup", "Deleting realm " + 
                                 delRealm + " ...");                    
                         if (FederationManager.getExitCode(am.deleteRealm(
                                 webClient, delRealm, true)) != 0) {
                             log(Level.SEVERE, "cleanup", 
                                     "deleteRealm famadm command failed");
                         }
                     }
 
                     String logoutUrl = protocol + ":" + "//" + host + ":" + 
                             port + uri + "/UI/Logout";
                     consoleLogout(webClient, logoutUrl);
                 } catch(Exception e) {
                     log(Level.SEVERE, "cleanup", e.getMessage());
                     e.printStackTrace();
                     throw e;
                 }
             } else {
                 log(Level.FINEST, "cleanup", 
                         "Debug flag was set cleanup method was not performed");
             }
         } else {
             log(Level.FINEST, "setup", "Skipping cleanup for " + testModule + 
                      " auth module test on a Windows based server");                
         }
         exiting("cleanup");
     }
     
     /**
      * Call Authentication Utility class to create the module instances
      * for a given module instance name
      * @param mRealm - the realm in which the module sub-configuration will be 
      * created.
      * @param moduleName - the name of the module sub-configuration to be 
      * created
      */
     private void createModule(String mRealm, String mName) {
         try {
             moduleConfig.setTestConfigRealm(mRealm);
             Map modMap = moduleConfig.getModuleData(mName);
             moduleServiceName = moduleConfig.getModuleServiceName();
             moduleSubConfig = moduleConfig.getModuleSubConfigName();
             moduleSubConfigId = moduleConfig.getModuleSubConfigId();
             log(Level.FINEST, "createModule", "ModuleServiceName: " +
                     moduleServiceName);
             log(Level.FINEST, "createModule", "ModuleSubConfig: " +
                     moduleSubConfig);
             log(Level.FINEST, "createModule", "ModuleSubConfigId: " +
                     moduleSubConfigId);
             moduleConfigData = getListFromMap(modMap, mName);
             moduleConfig.createModuleInstances(mRealm, moduleServiceName,
                     moduleSubConfig, moduleConfigData, moduleSubConfigId);
         } catch(AssertionError ae) {
             log(Level.SEVERE, "createModule", 
                     "Creation of the sub-configuration " + moduleSubConfig + 
                     " failed");
             throw ae;
         } catch(Exception e) {
             log(Level.SEVERE, "createModule", e.getMessage());
             e.printStackTrace();
         }
     }
     
     /**
      * Call Authentication Utility class to create the module instances
      * for a given module instance name
      * @param mRealm - the realm in which the module sub-configuration will be 
      * created.
      * @param moduleName - the name of the module sub-configuration to be 
      * created
      */
     private void deleteModule(String mRealm, String mName)
     throws Exception {
         WebClient webClient = new WebClient();
         try {
             moduleConfig.setTestConfigRealm(mRealm);
             Map modMap = moduleConfig.getModuleData(mName);
             moduleServiceName = moduleConfig.getModuleServiceName();
             moduleSubConfig = moduleConfig.getModuleSubConfigName();
             log(Level.FINEST, "deleteModule", "ModuleServiceName: " +
                     moduleServiceName);
             log(Level.FINEST, "deleteModule", "ModuleSubConfig: " +
                     moduleSubConfig);
             moduleConfigData = getListFromMap(modMap, mName);
             moduleConfig.deleteModuleInstances(mRealm, moduleServiceName,
                     moduleSubConfig);
         } catch(AssertionError ae) {
             log(Level.SEVERE, "deleteModule", 
                     "Deletion of the sub-configuration " + moduleSubConfig + 
                     " failed");
         } catch(Exception e) {
             log(Level.SEVERE, "deleteModule", e.getMessage());
             e.printStackTrace();
         } finally {
             String logoutUrl = protocol + ":" + "//" + host + ":" + port + uri +
                     "/UI/Logout";
             consoleLogout(webClient, logoutUrl);            
         }
     }    
     
     /**
      * Creates the required test users on the system for each
      * Chain to be executed
      * @param user map to be created
      * @param ChainName
      **/
     private void createUser(String userRealm, String newUser, 
             String userpassword) {
         List<String> userList = new ArrayList<String>();
         userList.add("sn=" + newUser);
         userList.add("cn=" + newUser);
         userList.add("userpassword=" + userpassword);
         userList.add("inetuserstatus=Active");
         log(logLevel, "createUser", "userList " + userList);
         testUserList.add(newUser);
         try {
             AuthTestConfigUtil userConfig =
                     new AuthTestConfigUtil(configrbName);
             userConfig.setTestConfigRealm(userRealm);
             userConfig.createUser(userList, newUser);
         } catch(Exception e) {
             log(Level.SEVERE, "createUsers", e.getMessage());
             e.printStackTrace();
         }
     }
     
     /**
      * Create the test realm
      * @param realm
      */
     private void createTestRealm(String newRealm){
         try {
             AuthTestConfigUtil realmConfig =
                     new AuthTestConfigUtil(configrbName);
             realmConfig.createRealms("/" + newRealm);
         } catch(Exception e) {
             log(Level.SEVERE, "createTestRealm", e.getMessage(), null);
             e.printStackTrace();
         }
     }
     
     /**
      * Get the list of users from Map, to create the
      * users.This is need for the <code>FederationManager</code> to
      * create users on the System
      * @param Map of users to be creared
      * @param moduleName
      */
     private List getListFromMap(Map lMap, String moduleName){
         Object escapeModServiceName = moduleName + ".module-service-name";
         Object escapeModSubConfigName = moduleName + ".module-subconfig-name";
         lMap.remove(escapeModServiceName);
         lMap.remove(escapeModSubConfigName);
         List<String> list = new ArrayList<String>();
         for (Iterator iter = lMap.entrySet().iterator(); iter.hasNext();) {
             Map.Entry entry = ( Map.Entry)iter.next();
             String userkey = (String)entry.getKey();
             int sindex = userkey.indexOf(".");
             CharSequence cseq = userkey.subSequence(0, sindex + 1);
             userkey = userkey.replace(cseq , "");
             userkey.trim();
             String userval = (String)entry.getValue();
             String uadd = userkey + "=" + userval;
             uadd.trim();
             list.add(uadd);
         }
         log(Level.FINEST, "getListFromMap", "UserList: " + list);        
         return list;
     }
 }
