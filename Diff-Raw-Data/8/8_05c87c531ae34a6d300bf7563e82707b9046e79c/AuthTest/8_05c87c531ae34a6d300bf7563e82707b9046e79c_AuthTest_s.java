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
 * $Id: AuthTest.java,v 1.14 2008-06-26 19:58:31 rmisra Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.SMSConstants;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.authentication.AuthTestConfigUtil;
 import com.sun.identity.qatest.common.authentication.AuthenticationCommon;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import org.testng.Reporter;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 /**
  * This class does the following:
  * (1) Create a module instance with a given auth level
  * (2) Create an auth service using the auth module
  * (3) Create a new user and assign the auth service to the user
  * (4) Create a new role
  * (5) Assign the auth service and the user to that role
  * (6) Do a module, role, service, level and user based authentication
  * using htmlunit and zero page login with URL parameters. 
  * (7) Repeat the following scenarios for Active Directory, LDAP, 
  * Membership, Anonymous, NT and JDBC modules)
  */
 public class AuthTest extends TestCommon {
 
     private AuthenticationCommon ac;
     private ResourceBundle rb;
     private String module_servicename;
     private String module_subconfigname;
     private String module_subconfigid;
     private String service_servicename;
     private String service_subconfigname;
     private String service_subconfigid;
     private String rolename;
     private String user;
     private String password;
     private String svcName;
     private String locTestModule;
     private String locTestMode;
     private String loginURL;
     private String logoutURL;
     private String amadmURL;
     private List list;
     private AuthTestConfigUtil moduleConfigData;
     private String configrbName = "authenticationConfigData";
     private boolean isValidTest = true;
     private WebClient webClient;
 
     /**
      * Constructor for the class.
      */
     public AuthTest() {
         super("AuthTest");
         ac = new AuthenticationCommon();
         moduleConfigData = new AuthTestConfigUtil(configrbName);
         logoutURL = protocol + ":" + "//" + host + ":" + port + uri + 
                 "/UI/Logout";
     }
 
     /**
      * This method is to configure the initial setup. It does the following:
      * (1) Create a module instance with a given auth level
      * (2) Create an auth service using the auth module
      * (3) Create a new user and assign the auth service to the user
      * (4) Create a new role
      * (5) Assign the auth service and the user to that role
      * This is called only once per auth module.
      */
     @Parameters({"testModule","testMode"})
     @BeforeClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void setup(String testModule, String testMode)
     throws Exception {
         Object[] params = {testModule, testMode};
         entering("setup", params);
         webClient = new WebClient();
         try {
             isValidTest = moduleConfigData.isValidModuleTest(testModule);
             if (isValidTest) {
                 locTestModule = testModule;
                 locTestMode = testMode;
                 rb = ResourceBundle.getBundle("authentication" + fileseparator +
                         "AuthTest");
                 list = moduleConfigData.getModuleDataAsList(locTestModule);
                 module_servicename = (String)rb.getString(locTestModule +
                         ".module_servicename");
                 module_subconfigname = (String)rb.getString(locTestModule +
                         ".module_subconfigname");
                 module_subconfigid = (String)rb.getString(locTestModule +
                         ".module_subconfigid");
 
                 service_servicename = (String)rb.getString(locTestModule +
                         ".service_servicename");
                 service_subconfigname = (String)rb.getString(locTestModule +
                         ".service_subconfigname");
                 service_subconfigid = (String)rb.getString(locTestModule +
                         ".service_subconfigid");
 
                 rolename = (String)rb.getString(locTestModule + ".rolename");
                 user = (String)rb.getString(locTestModule + ".user");
                 password = (String)rb.getString(locTestModule + ".password");
 
                 log(Level.FINEST, "setup", "module_servicename: " +
                             module_servicename);
                 log(Level.FINEST, "setup", "module_subconfigname: " +
                             module_subconfigname);
                 log(Level.FINEST, "setup", "module_subconfigid: " +
                             module_subconfigid);
 
                 log(Level.FINEST, "setup", "service_servicename: " +
                             service_servicename);
                 log(Level.FINEST, "setup", "service_subconfigname: " +
                             service_subconfigname);
                 log(Level.FINEST, "setup", "service_subconfigid: " +
                             service_subconfigid);
                 log(Level.FINEST, "setup", "module_subconfig_list: " + list);
 
                 log(Level.FINEST, "setup", "rolename: " + rolename);
                 log(Level.FINEST, "setup", "username: " + user);
                 log(Level.FINEST, "setup", "userpassword: " + password);
 
                 Reporter.log("ModuleServiceName: " + module_servicename);
                 Reporter.log("ModuleSubConfigName: " + module_subconfigname);
                 Reporter.log("ModuleSubConfigId: " + module_subconfigid);
 
                 Reporter.log("ServiceServiceName: " + service_servicename);
                 Reporter.log("ServiceSubConfigName: " + service_subconfigname);
                 Reporter.log("ServiceSubConfigId: " + service_subconfigid);
 
                 Reporter.log("ModuleSubConfigList: " + list);
 
                 Reporter.log("RoleName: " + rolename);
                 Reporter.log("UserName: " + user);
                 Reporter.log("UserPassword: " + password);
 
                 loginURL = getLoginURL("/");
                 amadmURL = protocol + ":" + "//" + host + ":" + port +
                             uri;
                 log(Level.FINEST, "setup", loginURL);
                 log(Level.FINEST, "setup", logoutURL);
                 log(Level.FINEST, "setup", amadmURL);
                 if (module_servicename.equals("iPlanetAMAuthAnonymousService"))
                     list.add("iplanet-am-auth-anonymous-users-list=" + user);
                 FederationManager am = new FederationManager(amadmURL);
                 consoleLogin(webClient, loginURL, adminUser, adminPassword);
                 log(Level.FINE, "setup", "Creating module sub-configuration " + 
                         module_subconfigname + "...");
                 if (FederationManager.getExitCode(am.createSubCfg(webClient, 
                         module_servicename, module_subconfigname, list, realm, 
                         module_subconfigid, "0")) != 0) {
                     log(Level.SEVERE, "setup", 
                             "createSubCfg (module) famadm command failed");
                     assert false;
                 }
 
                 list.clear();
                 String svcData = "iplanet-am-auth-configuration=" + 
                         "<AttributeValuePair><Value>" + module_subconfigname +
                         " REQUIRED</Value></AttributeValuePair>";
                 log(Level.FINEST, "setup", "ServiceData: " + svcData);
                 list.add(svcData);
                 log(Level.FINE, "setup", "Creating service sub-configuration " + 
                         service_subconfigname + "...");            
                 if (FederationManager.getExitCode(am.createSubCfg(webClient, 
                         service_servicename, service_subconfigname, list, realm,
                         service_subconfigid, "0")) != 0) {
                     log(Level.SEVERE, "setup", 
                             "createSubCfg (service) famadm command failed");
                     assert false;
                 }
 
                 int iIdx = service_subconfigname.indexOf("/");
                 svcName = service_subconfigname.substring(iIdx+1,
                             service_subconfigname.length());
                 log(Level.FINEST, "setup", "svcName:" + svcName);
 
                 list.clear();
                 list.add("sn=" + user);
                 list.add("cn=" + user);
                 list.add("userpassword=" + password);
                 list.add("inetuserstatus=Active");
                 list.add("iplanet-am-user-auth-config=" + svcName);
                 log(Level.FINE, "setup", "Creating user " + user + " ...");
                 if (FederationManager.getExitCode(am.createIdentity(webClient, 
                         realm, user, "User", list)) != 0) {
                     log(Level.SEVERE, "setup", 
                             "createIdentity (User) famadm command failed");
                     assert false;
                 }
 
                 if (ac.getSMSCommon().isPluginConfigured(
                        SMSConstants.SMS_DATASTORE_SCHEMA_TYPE_AMSDK, realm)) {           
                     log(Level.FINE, "setup", "Creating role " + rolename + 
                             " ...");
                     if (FederationManager.getExitCode(am.createIdentity(
                             webClient, realm, rolename, "Role", null)) != 0) {
                         log(Level.SEVERE, "setup", 
                                 "createIdentity (Role) famadm command failed");
                         assert false;
                     }
                     log(Level.FINE, "setup", "Assigning the user " + user + 
                             " to role " + rolename + " ...");
                     if (FederationManager.getExitCode(am.addMember(webClient, 
                             realm, user, "User", rolename, "Role")) != 0) {
                         log(Level.SEVERE, "setup", 
                                 "addMember famadm (User) call failed");
                         assert false;
                     }
                     list.clear();
                     list.add("iplanet-am-auth-configuration=" + svcName);
                     log(Level.FINE, "setup", "Assigning the service " + 
                             service_servicename + " to the role " + 
                             rolename + "...");
                     if (FederationManager.getExitCode(am.addSvcIdentity(
                             webClient, realm, rolename, "Role", 
                             service_servicename, list)) != 0) {
                         log(Level.SEVERE, "setup", 
                                 "addSvcIdentity famadm command failed");
                         assert false;
                     }                
                 } else {
                     log(Level.FINEST, "setup", 
                             "Creation of a role, assignment of user to role, " +
                             "and assignment of service to role skipped for " + 
                             "non amsdk plugin ...");
                 }
             } else {
                 log(Level.FINEST, "setup", "Skipping setup of " + testModule + 
                      " auth module test on a Windows based server");
             }
         } catch(AssertionError ae) {
             log(Level.SEVERE, "setup", 
                     "Calling cleanup due to failed famadm exit code ...");
             cleanup(testModule);
             throw ae;
         } catch(Exception e) {
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (isValidTest) {
                 consoleLogout(webClient, logoutURL);
             }
         } 
         exiting("setup");
     }
 
     /**
      * Tests for successful login into the system using correct
      * credentials
      */
     @Parameters({"testModule"})
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testLoginPositive(String testModule)
     throws Exception {
         Object[] params = {testModule};
         entering("testLoginPositive", params);
 
         if (isValidTest) {
             webClient = new WebClient();
             try {
                 String loginUser = (String)rb.getString(locTestModule + ".user");
                 String loginPassword = (String)rb.getString(locTestModule + 
                         ".password");
                 String mode = locTestMode;
                 String modevalue = (String)rb.getString(locTestModule +
                         ".modevalue." + locTestMode);
                 String msg = (String)rb.getString(locTestModule + ".passmsg");
 
                 if (module_servicename.equals(
                         "iPlanetAMAuthAnonymousService")) {
                     ac.testZeroPageLoginAnonymousPositive(webClient, loginUser,
                             loginPassword, mode, modevalue, msg);
                 } else {
                     ac.testZeroPageLoginPositive(webClient, loginUser, 
                             loginPassword, mode, modevalue, msg);
                 }
 
             } catch (Exception e) {
                 log(Level.SEVERE, "testLoginPositive", e.getMessage());
                 e.printStackTrace();
                 throw e;
             } finally {
                 consoleLogout(webClient, logoutURL);                
             }
         } else {
             log(Level.FINEST, "testLoginPositive", "Skipping " + testModule + 
                     " auth module test on a Windows based server");                
         }
         exiting("testLoginPositive");
     }
 
     /**
      * Tests for unsuccessful login into the system using incorrect
      * credentials
      */
     @Parameters({"testModule"})    
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testLoginNegative(String testModule)
     throws Exception {
         Object[] params = {testModule};
         entering("testLoginNegative", params);
         webClient = new WebClient();        
         if (isValidTest) {
             try {
                 String loginUser = (String)rb.getString(locTestModule + ".user");
                 String loginPassword = (String)rb.getString(locTestModule + 
                         ".password");
                 String mode = locTestMode;
                 String modevalue = (String)rb.getString(locTestModule +
                         ".modevalue." + locTestMode);
                 String msg = (String)rb.getString(locTestModule + ".failmsg");
                 if (module_servicename.equals("iPlanetAMAuthAnonymousService")) 
                 {
                     ac.testZeroPageLoginAnonymousNegative(webClient, loginUser, 
                             loginPassword, mode, modevalue, msg);
                 } else {
                     ac.testZeroPageLoginNegative(webClient, loginUser, 
                             loginPassword, mode, modevalue, msg);
                 }
             } catch (Exception e) {
                 log(Level.SEVERE, "testLoginNegative", e.getMessage());
                 e.printStackTrace();
                 throw e;
             } finally {
                 consoleLogout(webClient, logoutURL);                
             }
         } else {
             log(Level.FINEST, "testLoginNegative", "Skipping " + testModule + 
                     " auth module test on a Windows based server");           
         }
         exiting("testLoginNegative");
     }
 
     /**
      * This method is to clear the initial setup. It does the following:
      * (1) Delete authentication service
      * (2) Delete authentication instance
      * (3) Delete all users and roles
      * This is called only once per auth module.
      */
     @Parameters({"testModule"})
     @AfterClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void cleanup(String testModule)
     throws Exception {
         Object[] params = {testModule};
         entering("cleanup", params);
         webClient = new WebClient();
         if (isValidTest) {
             try {
                 user = (String)rb.getString(locTestModule + ".user");
                 rolename = (String)rb.getString(locTestModule + ".rolename");
                 service_servicename =
                         (String)rb.getString(locTestModule +
                         ".service_servicename");
                 service_subconfigname =
                         (String)rb.getString(locTestModule +
                         ".service_subconfigname");
                 module_subconfigname =
                         (String)rb.getString(locTestModule +
                         ".module_subconfigname");
 
                 log(Level.FINEST, "cleanup", "UserName:" + user);
                 log(Level.FINEST, "cleanup", "RoleName:" + rolename);
                 log(Level.FINEST, "cleanup", "service_servicename:" +
                         service_servicename);
                 log(Level.FINEST, "cleanup", "module_subconfigname:" +
                         module_subconfigname);
                 log(Level.FINEST, "cleanup", "svcName:" + svcName);
 
                 Reporter.log("UserName:" + user);
                 Reporter.log("RoleName:" + rolename);
                 Reporter.log("service_servicename:" + service_servicename);
                 Reporter.log("module_subconfigname:" + module_subconfigname);
                 Reporter.log("service_subconfigname:" + service_subconfigname);
 
                 FederationManager am = new FederationManager(amadmURL);
                 consoleLogin(webClient, loginURL, adminUser, adminPassword);
                 list = new ArrayList();
                 list.add(user);
                 log(Level.FINE, "cleanup", "Deleting user " + user + " ...");
                 if (FederationManager.getExitCode(am.deleteIdentities(webClient, 
                         realm, list, "User")) != 0) {
                     log(Level.SEVERE, "cleanup", 
                             "deleteIdentities (User) famadm command failed");
                 }
                 list.clear();
                 list.add(rolename);
 
                 if (ac.getSMSCommon().isPluginConfigured(
                        SMSConstants.SMS_DATASTORE_SCHEMA_TYPE_AMSDK, realm)) {
                     log(Level.FINE, "cleanup", "Deleting role " + rolename + 
                             " ...");
                     if (FederationManager.getExitCode(
                             am.deleteIdentities(webClient, realm, list, 
                             "Role")) !=0) {
                         log(Level.SEVERE, "cleanup", 
                                 "deleteIdentities(Role) famadm command failed");
                     }
                 }
 
                 log(Level.FINE, "cleanup", 
                         "Deleting service sub-configuration " + 
                         service_subconfigname + " ...");            
                 if (FederationManager.getExitCode(am.deleteSubCfg(webClient, 
                         service_servicename, service_subconfigname, realm)) 
                         != 0 ) {
                     log(Level.SEVERE, "cleanup", 
                             "deleteSubCfg (Service) famadm command failed");
                 }
                 list.clear();
                 list.add(module_subconfigname);
 
                 log(Level.FINE, "cleanup", "Deleting module instance(s) " + 
                         list + " ...");               
                 if (FederationManager.getExitCode(
                         am.deleteAuthInstances(webClient, realm, list)) != 0) {
                     log(Level.SEVERE, "cleanup", 
                             "deleteAuthInstances famadm command failed");
                 }
             } catch (Exception e) {
                 log(Level.SEVERE, "cleanup", e.getMessage());
                 e.printStackTrace();
                 throw e;
             } finally {
                 consoleLogout(webClient, logoutURL);
                 Thread.sleep(5000);            
             }
         } else {
             log(Level.FINEST, "setup", "Skipping cleanup for " + testModule + 
                      " auth module test on a Windows based server");            
         }
         exiting("cleanup");
     }
 }
