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
 * $Id: AuthTest.java,v 1.9 2007-12-18 22:06:16 sridharev Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.authentication;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.authentication.AuthTestConfigUtil;
 import com.sun.identity.qatest.common.authentication.AuthenticationCommon;
 import java.util.ArrayList;
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
 
     /**
      * Constructor for the class.
      */
     public AuthTest() {
         super("AuthTest");
         ac = new AuthenticationCommon();
         moduleConfigData = new AuthTestConfigUtil(configrbName);
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
         try {
             locTestModule = testModule;
             locTestMode = testMode;
             rb = ResourceBundle.getBundle("AuthTest");
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
 
             log(Level.FINEST, "setup", "module_servicename:" +
                         module_servicename);
             log(Level.FINEST, "setup", "module_subconfigname:" +
                         module_subconfigname);
             log(Level.FINEST, "setup", "module_subconfigid:" +
                         module_subconfigid);
 
             log(Level.FINEST, "setup", "service_servicename:" +
                         service_servicename);
             log(Level.FINEST, "setup", "service_subconfigname:" +
                         service_subconfigname);
             log(Level.FINEST, "setup", "service_subconfigid:" +
                         service_subconfigid);
 
             log(Level.FINEST, "setup", "rolename:" + rolename);
             log(Level.FINEST, "setup", "username:" + user);
             log(Level.FINEST, "setup", "userpassword:" + password);
 
             Reporter.log("ModuleServiceName" + module_servicename);
             Reporter.log("ModuleSubConfigName" + module_subconfigname);
             Reporter.log("ModuleSubConfigId" + module_subconfigid);
 
             Reporter.log("ServiceServiceName" + service_servicename);
             Reporter.log("ServiceSubConfigName" + service_subconfigname);
             Reporter.log("ServiceSubConfigId" + service_subconfigid);
 
             Reporter.log("RoleName" + rolename);
             Reporter.log("UserName" + user);
             Reporter.log("UserPassword" + password);
                 
             loginURL = protocol + ":" + "//" + host + ":" + port +
                         uri + "/UI/Login";
             logoutURL = protocol + ":" + "//" + host + ":" + port +
                         uri + "/UI/Logout";
             amadmURL = protocol + ":" + "//" + host + ":" + port +
                         uri;
             log(Level.FINEST, "setup", loginURL);
             log(Level.FINEST, "setup", logoutURL);
             log(Level.FINEST, "setup", amadmURL);
             if (module_servicename.equals("iPlanetAMAuthAnonymousService"))
                 list.add("iplanet-am-auth-anonymous-users-list=" + user);
             FederationManager am = new FederationManager(amadmURL);
             WebClient webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             am.createSubConfiguration(webClient, module_servicename,
                         module_subconfigname, list, realm, module_subconfigid);
 
             list.clear();
             String svcData = "iplanet-am-auth-configuration=" + 
                     "<AttributeValuePair><Value>" + module_subconfigname +
                     " REQUIRED</Value></AttributeValuePair>";
             log(Level.FINEST, "setup", svcData);
             list.add(svcData);
             am.createSubConfiguration(webClient, service_servicename,
                     service_subconfigname, list, realm,
                     service_subconfigid);
 
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
             am.createIdentity(webClient, realm, user, "User", list);
 
             am.createIdentity(webClient, realm, rolename, "Role", null);
             am.addMember(webClient, realm, user, "User", rolename, "Role");
 
             list.clear();
             list.add("iplanet-am-auth-configuration=" + svcName);
             am.addServiceIdentity(webClient, realm, rolename, "Role",
                     service_servicename, list);
             consoleLogout(webClient, logoutURL);
         } catch(Exception e) {
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("setup");
     }
 
     /**
      * Tests for successful login into the system using correct
      * credentials
      */
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testLoginPositive()
     throws Exception {
         entering("testLoginPositive", null);
         try {
             String user = (String)rb.getString(locTestModule + ".user");
             String password = (String)rb.getString(locTestModule + ".password");
             String mode = locTestMode;
             String modevalue = (String)rb.getString(locTestModule +
                     ".modevalue." + locTestMode);
             String msg = (String)rb.getString(locTestModule + ".passmsg");
             String module_servicename =
                         (String)rb.getString(locTestModule +
                         ".module_servicename");
             WebClient wc = new WebClient();
             if (module_servicename.equals("iPlanetAMAuthAnonymousService"))
                 ac.testZeroPageLoginAnonymousPositive(wc, user, password, mode,
                         modevalue, msg);
             else
                 ac.testZeroPageLoginPositive(wc, user, password, mode,
                         modevalue, msg);
             consoleLogout(wc, logoutURL);
         } catch (Exception e) {
             log(Level.SEVERE, "testLoginPositive", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("testLoginPositive");
     }
 
     /**
      * Tests for unsuccessful login into the system using incorrect
      * credentials
      */
     @Test(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testLoginNegative()
     throws Exception {
         entering("testLoginNegative", null);
         try {
             String user = (String)rb.getString(locTestModule + ".user");
             String password = (String)rb.getString(locTestModule + ".password");
             String mode = locTestMode;
             String modevalue = (String)rb.getString(locTestModule +
                     ".modevalue." + locTestMode);
             String msg = (String)rb.getString(locTestModule + ".failmsg");
             String module_servicename =
                         (String)rb.getString(locTestModule +
                         ".module_servicename");
             WebClient wc = new WebClient();
             if (module_servicename.equals("iPlanetAMAuthAnonymousService"))
                 ac.testZeroPageLoginAnonymousNegative(wc, user, password, mode,
                         modevalue, msg);
             else
                 ac.testZeroPageLoginNegative(wc, user, password, mode,
                         modevalue, msg);
             consoleLogout(wc, logoutURL);
         } catch (Exception e) {
             log(Level.SEVERE, "testLoginNegative", e.getMessage());
             e.printStackTrace();
             throw e;
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
    @AfterClass(groups={"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void cleanup()
     throws Exception {
         entering("cleanup", null);
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
 
             log(Level.FINEST, "setup", "UserName:" + user);
             log(Level.FINEST, "setup", "RoleName:" + rolename);
             log(Level.FINEST, "setup", "service_servicename:" +
                     service_servicename);
             log(Level.FINEST, "setup", "module_subconfigname:" +
                     module_subconfigname);
             log(Level.FINEST, "setup", "svcName:" + svcName);
 
             Reporter.log("UserName:" + user);
             Reporter.log("RoleName:" + rolename);
             Reporter.log("service_servicename:" + service_servicename);
             Reporter.log("module_subconfigname:" + module_subconfigname);
             Reporter.log("service_subconfigname:" + service_subconfigname);
 
             FederationManager am = new FederationManager(amadmURL);
             WebClient webClient = new WebClient();
             consoleLogin(webClient, loginURL, adminUser, adminPassword);
             list = new ArrayList();
             list.add(user);
             am.deleteIdentities(webClient, realm, list, "User");
             list.clear();
             list.add(rolename);
             am.deleteIdentities(webClient, realm, list, "Role");
             am.deleteSubConfiguration(webClient, service_servicename,
                     service_subconfigname, realm); 
             list.clear();
             list.add(module_subconfigname);
             am.deleteAuthInstances(webClient, realm, list); 
             consoleLogout(webClient, logoutURL);
             Thread.sleep(5000);
         } catch (Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("cleanup");
     }
 }
