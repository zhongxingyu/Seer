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
 * $Id: ChainTest.java,v 1.10 2009-01-15 15:12:13 cmwesley Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 /**
  * This class automates the following test cases:
  * AccessManager_AuthModule(JAASSS)_1, AccessManager_AuthModule(JAASSS)_2,
  * AccessManager_AuthModule(JAASSS)_3, AccessManager_AuthModule(JAASSS)_4,
  * AccessManager_AuthModule(JAASSS)_5, AccessManager_AuthModule(JAASSS)_6,
  * AccessManager_AuthModule(JAASSS)_7, AccessManager_AuthModule(JAASSS)_8,
  * AccessManager_AuthModule(JAASSS)_9, AccessManager_AuthModule(JAASSS)_10,
  * AccessManager_AuthModule(JAASSS)_11, AccessManager_AuthModule(JAASSS)_12,
  * AccessManager_AuthModule(JAASSS)_13, AccessManager_AuthModule(JAASSS)_14
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.authentication.AuthTestConfigUtil;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import org.testng.Reporter;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 /**
  * This class drives the chaining tests, Each Chain can have the number of
  * module instances.
  * Creates the chain test setup based on the test chain
  * mentioned in the test resource bundle, The following are performed in order
  * to test chaining.
  * - Create the module instances by the given module instance names.
  * - Create services (Chain) for the number of modules involved in the chain.
  * - Validates each chain
  */
 public class ChainTest extends TestCommon {
     
     private ResourceBundle testResources;
     private String chainModules;
     private String chainService;
     private String chainUserNames;
     private String chainModInstances;
     private String chainSuccessURL;
     private String chainFailureURL;
     private List moduleConfigData;
     private String moduleServiceName;
     private String moduleSubConfig;
     private String moduleSubConfigId;
     private String testDescription;
     private int numOfSufficientUsers = -1;
     private String configrbName = "authenticationConfigData";
     private List<String> testUserList = new ArrayList<String>();
     
     /**
      * Default Constructor
      **/
     public ChainTest() {
        super("ChainTest");
     }
     
     /**
      * Creates the necessary configuration for the chain suchs as 
      * creating module instances and chain service by the given service name
      * before performing the tests.
      */
     @Parameters({"testChainName"})
     @BeforeClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void setup(String testChainName)
     throws Exception {
         Object[] params = {testChainName};
         testResources = ResourceBundle.getBundle("authentication" +
                 fileseparator + "ChainTest");
         entering("setup", params);
         chainModules = testResources.getString("am-auth-test-" + 
                 testChainName + "-modules");
         chainService = testResources.getString("am-auth-test-" +
                 testChainName + "-servicename");
         chainUserNames = testResources.getString("am-auth-test-" +
                 testChainName+ "-users");
         chainModInstances = testResources.getString("am-auth-test-" +
                 testChainName+ "-instances");
         chainSuccessURL = testResources.getString("am-auth-test-" + 
                 testChainName + "-successURL");
         chainFailureURL = testResources.getString("am-auth-test-" + 
                 testChainName + "-failureURL");
         testDescription = testResources.getString("am-auth-test-" + 
                 testChainName + "-description");
         try {
             numOfSufficientUsers = Integer.parseInt(
                     testResources.getString("am-auth-test-" +
                     testChainName + "-num-of-sufficient-users"));
         } catch (MissingResourceException mre) {
             log(Level.FINEST, "setup", "Parameter am-auth-test-" +
                     testChainName + "-num-of-sufficient-users was not used.");
         }
 
         log(Level.FINEST, "setup", "testChainName: " + testChainName);
         log(Level.FINEST, "setup", "Description of Chain Test: " + 
                 testDescription);
         log(Level.FINEST, "setup", "Modules for Chain: " + chainModules);
         log(Level.FINEST, "setup", "Service Name for Chain: " + chainService);
         log(Level.FINEST, "setup", "Users for the Chain: " +
                 chainUserNames);
         log(Level.FINEST, "setup", "Module Instances for Chain: " +
                 chainModInstances);
         log(Level.FINEST, "setup", "SuccessURL for the Chain: " 
                 + chainSuccessURL);
         log(Level.FINEST, "setup", "Failure URL for the Chain: " 
                 + chainFailureURL);
         Reporter.log("ChainName: " + testChainName);
         Reporter.log("Test Description: " + testDescription);
         Reporter.log("Modules for Chain: " + chainModules);
         Reporter.log("Service Name for Chain: " + chainService);
         Reporter.log("Users for the Chain: " + chainUserNames);
         Reporter.log("Module Intances for Chain: " + chainModInstances);
         Reporter.log("SuccessURL for the Chain: " + chainSuccessURL);
         Reporter.log("Failure URL for the Chain: " + chainFailureURL);
         
         String[] modTokens = chainModules.split(",");
         List<String> chainList = new ArrayList<String>();
         try {
             for (String modName: modTokens){
                 if (!chainList.add(modName)) {
                     log(Level.SEVERE, "setup", 
                             "Module could not be added to list");
                 }                
                 log(Level.FINE, "setup", "Creating auth module " + 
                         modName + " ...");
                 createModule(modName);
             }
             createService(chainService, chainModInstances, chainSuccessURL,
                     chainFailureURL);
             log(Level.FINEST, "setup", "ChainName " + testChainName);
             Map users = createUserMap(chainUserNames);
             createUsers(users, testChainName);
         } catch (Exception e) {
             log(Level.SEVERE, "setup", 
                     "Calling cleanup due to failed ssoadm exit code ...");
             cleanup(testChainName); 
             throw e;            
         }
         exiting("setup");
     }
 
     /**
      * Peform the test validation for this chain/service. This method
      * performs the positive test validation by calling
      * <code>ChainTestValidation</code> and its appropriate method.
      */
     @Parameters({"testChainName"})
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void validatePositiveTests(String testChainName) 
     throws Exception {
         Object[] params = {testChainName};
         entering("validatePositiveTests", params);
         Map executeMap;
         executeMap = new HashMap();
         executeMap.put("Chainname", testChainName);
         executeMap.put("passmessage", testResources.getString("am-auth-test-" +
                testChainName + "-passmsg"));
         executeMap.put("failmessage", testResources.getString("am-auth-test-" + 
                 testChainName + "-failmsg"));
         String testUsers = testResources.getString("am-auth-test-" +
                 testChainName + "-users");
         if (numOfSufficientUsers == -1) {
             executeMap.put("users", testUsers);
         } else {
             String[] sufficientUsers = testUsers.split("\\|");
             StringBuffer userBuffer = new StringBuffer();
             for (int i=0; i < numOfSufficientUsers && 
                     i < sufficientUsers.length; ) {
                 userBuffer.append(sufficientUsers[i++]);
                 if (i < numOfSufficientUsers) {
                     userBuffer.append("|");
                 }
             }
             executeMap.put("users", userBuffer.toString());
         }
         executeMap.put("servicename", testResources.getString("am-auth-test-" +
                 testChainName + "-servicename"));
         log(Level.FINEST, "validatePositiveTests", "ExecuteMap: " + executeMap);
         log(Level.FINEST, "validatePositiveTests", "TestDescription: " + 
                 testDescription);
         Reporter.log("Test Description: " + testDescription);
         ChainTestValidation ct = new ChainTestValidation(executeMap);
         ct.testServicebasedPositive();
         exiting("validatePositiveTests");
     }
     
     /**
      * Peform the test validation for this chain/service. This method
      * performs the Negative test validation by calling
      * <code>ChainTestValidation</code> and its appropriate method.
      */
     @Parameters({"testChainName"})
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void validateNegativeTests(String testChainName) 
     throws Exception {
         Object[] params = {testChainName};
         entering("validateNegativeTests", params);
         Map executeMap;
         executeMap = new HashMap();
         executeMap.put("Chainname",testChainName);
         executeMap.put("passmessage", testResources.getString("am-auth-test-" +
                testChainName + "-passmsg"));
         executeMap.put("failmessage", testResources.getString("am-auth-test-" + 
                 testChainName + "-failmsg"));
         executeMap.put("users", testResources.getString("am-auth-test-" +
                 testChainName + "-users"));
         executeMap.put("servicename", testResources.getString("am-auth-test-" +
                 testChainName + "-servicename"));
         log(Level.FINEST, "validateNegativeTests", "ExecuteMap:" + executeMap);
         log(Level.FINEST, "validateNegativeTests", "ExecuteMap: " + executeMap);
         log(Level.FINEST, "validateNegativeTests", "TestDescription: " + 
                 testDescription);
         Reporter.log("Test Description: " + testDescription);        
         ChainTestValidation ct = new ChainTestValidation(executeMap);
         ct.testServicebasedNegative();
         exiting("validateNegativeTests");
     }
 
     /**
      * Clean up is called post execution of each chain before entering into
      * the next chain, This is done to maintain the system in a clean state
      * after executing each test scenario, in this case each chain/service
      * authentication is done.This processed in this method are:
      * 1. Delete the authentication service/Chain
      * 2. Delete the modules involved in that service/chain
      * 3. Delete the users involved/create for this chain.
      */
     @Parameters({"testChainName"})
     @AfterClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void cleanup(String testChainName)
         throws Exception {
         Object[] params = {testChainName};
         entering("cleanup", params);
         try {
             String url = getLoginURL("/") ;
             log(Level.FINEST, "cleanup", url);
             log(Level.FINEST, "cleanup", "chainName: " + testChainName);
             log(Level.FINEST, "validateNegativeTests", "TestDescription: " + 
                     testDescription);
             Reporter.log("chainName:" + testChainName);
             Reporter.log("Test Description: " + testDescription);
             StringTokenizer moduleInstances = new StringTokenizer
                     (chainModInstances,"|");
             List<String> chainList = new ArrayList<String>();
             chainList.add(chainService);
             log(Level.FINEST, "cleanup", "ChainList: " + chainList);
             List<String> instanceNames = new ArrayList<String>();
             while (moduleInstances.hasMoreTokens()) {
                 String[] instanceData = moduleInstances.nextToken().split(",");
                 instanceNames.add(instanceData[0]);
             }
             String ssoadmURL = protocol + ":" + "//" + host + ":" + port + uri ;
             FederationManager am = new FederationManager(ssoadmURL);
             WebClient webClient = new WebClient();
             consoleLogin(webClient, url, adminUser, adminPassword);
             log(Level.FINE, "cleanup", "Deleting auth configurations " + 
                     chainList + " ...");
             if (FederationManager.getExitCode(am.deleteAuthCfgs(webClient, 
                     realm, chainList)) != 0) {
                 log(Level.SEVERE, "cleanup", 
                         "deleteAuthCfgs ssoadm command failed");
             }
             
             log(Level.FINE, "cleanup", "Deleting auth instances " + 
                     instanceNames);
             if (FederationManager.getExitCode(am.deleteAuthInstances(webClient, 
                     realm, instanceNames)) != 0) {
                 log(Level.SEVERE, "cleanup", 
                         "deleteAuthInstances ssoadm command failed");
             }
             
             log(Level.FINE, "cleanup", "Deleting user(s) " + testUserList + 
                     " ...");
             if (FederationManager.getExitCode(am.deleteIdentities(webClient, 
                     realm, testUserList, "User")) != 0) {
                 log(Level.SEVERE, "cleanup", 
                         "deleteIdentities (User) ssoadm command failed");
             }            
             String logoutURL = protocol + ":" + "//" + host + ":" + port + uri 
                     +  "/UI/Logout";
             consoleLogout(webClient, logoutURL);
         } catch(Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("cleanup");
     }
 
     /**
      * Call Authentication Utility class to create the module instances
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
             log(Level.FINEST,"createModule", "ModuleServiceName :" + 
                     moduleServiceName);
             log(Level.FINEST,"createModule", "ModuleSubConfig :" + 
                     moduleSubConfig);
             log(Level.FINEST,"createModule", "ModuleSubConfigId :" + 
                     moduleSubConfigId);
             moduleConfigData = getListFromMap(modMap, mName);
             moduleConfig.createModuleInstances(moduleServiceName,
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
      * Creates authentication service for the given module instances
      * by instantiating the authentication utility class
      * @param name of service
      * @param module instances for this service
      * @param service successURL
      * @param service FailureURL
      **/
     private void createService(String mService, String mInstances,
             String mSuccess, String mFailure){
         try {
             String srv_attribute_name = "iplanet-am-auth-configuration=" +
                     "<AttributeValuePair>";
             List<String> chainList = new ArrayList<String>();
             String[] testChainInstances = mInstances.split("\\|");
             StringBuffer reqModules = new StringBuffer(); 
             for (String instance: testChainInstances) {
                 log(Level.FINEST, "createService", "ModuleInstance: " +
                          instance);
                 String[] instanceFields = instance.split(","); 
                 reqModules.append("<Value>");
                 for (String field: instanceFields) {
                     log(Level.FINEST, "createService", "ModuleField: " +
                             field);
                      reqModules.append(field);
                      reqModules.append(" ");                   
                 }
                 reqModules.append("</Value>");
             }
             String srvData = srv_attribute_name + reqModules + 
                     "</AttributeValuePair>";
             log(Level.FINEST, "createService", "ServiceData: " + srvData);
             chainList.add(srvData);
             chainList.add("iplanet-am-auth-login-success-url=" + mSuccess);
             chainList.add("iplanet-am-auth-login-failure-url=" + mFailure);
             AuthTestConfigUtil serviceConfig = 
                     new AuthTestConfigUtil(configrbName);
             serviceConfig.createServices(mService, chainList);
         } catch(AssertionError ae) {
             log(Level.SEVERE, "createService", 
                     "Creation of the service " + mService + " failed");
             throw ae;            
         } catch(Exception e) {
             log(Level.SEVERE, "createService", e.getMessage());
             e.printStackTrace();
         }
     }
     
     /**
      * Creates the required test users on the system for each
      * Chain to be executed
      * @param user map to be created
      * @param ChainName
      **/
     private void createUsers(Map testUsers, String testChain){
         List<String> userList = new ArrayList<String>();
         for (Iterator iter = testUsers.entrySet().iterator(); iter.hasNext(); )
         {
             Map.Entry entry = (Map.Entry)iter.next();
             String newUser = (String)entry.getKey();
             newUser.trim();
             testUserList.add(newUser);
             String userPassword = (String)entry.getValue();
             userPassword.trim();
             String aliasName = testResources.getString("am-auth-test-" +
                     testChain + "-" + newUser + "-alias");
             userList.add("sn=" + newUser);
             userList.add("cn=" + newUser);
             userList.add("userpassword=" + userPassword);
             userList.add("iplanet-am-user-alias-list=" + aliasName);
             userList.add("inetuserstatus=Active");
             try {
                 AuthTestConfigUtil userConfig = 
                         new AuthTestConfigUtil(configrbName);
                 userConfig.createUser(userList, newUser);
             } catch(AssertionError ae) {
                 log(Level.SEVERE, "createUsers", 
                         "Creation of the user " + newUser + " failed");
                 throw ae;                
             } catch(Exception e) {
                 log(Level.SEVERE, "createUsers", e.getMessage());
                 e.printStackTrace();
             }
         }
     }
     
     /**
      * Creates users map
      * @param Row of users:password with a delimeter for each user involved
      */
     private Map createUserMap(String sUsers) 
     throws MissingResourceException {
         Map userMap = new HashMap();
         String[] userTokens = sUsers.split("\\|");
         for (String uName: userTokens){
             int uLength = uName.length();
             int uIndex = uName.indexOf(":");
             String userName = uName.substring(0, uIndex);
             String userPass = uName.substring(uIndex + 1, uLength);
             userMap.put(userName, userPass);
         }
         log(Level.FINEST, "createUserMap", "User Map: " + userMap);
         return userMap;
     }
     
     /**
      * Get the list of users from Map, to create the
      * users.This is need for the <code>FederationManager</code> to
      * create users on the System
      * @param Map of users to be creared
      * @param moduleName
      */
     private List getListFromMap(Map lMap, String moduleName) {
         Object escapeModServiceName = moduleName + ".module-service-name";
         Object escapeModSubConfigName = moduleName + ".module-subconfig-name";
         lMap.remove(escapeModServiceName);
         lMap.remove(escapeModSubConfigName);
         List<String> list = new ArrayList<String>();
         for (Iterator iter = lMap.entrySet().iterator(); iter.hasNext(); ) {
             Map.Entry entry = (Map.Entry)iter.next();
             String userkey = (String)entry.getKey();
             int sindex=userkey.indexOf(".");
             CharSequence cseq = userkey.subSequence(0, sindex + 1);
             userkey=userkey.replace(cseq, "");
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
