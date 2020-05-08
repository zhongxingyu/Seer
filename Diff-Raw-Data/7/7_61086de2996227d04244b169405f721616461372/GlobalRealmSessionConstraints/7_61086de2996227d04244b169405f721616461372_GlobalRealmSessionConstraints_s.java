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
 * $Id: GlobalRealmSessionConstraints.java,v 1.1 2008-04-19 01:30:44 srivenigan Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.session;
 
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.qatest.common.IDMCommon;
 import com.sun.identity.qatest.common.SMSCommon;
 import com.sun.identity.qatest.common.TestCommon;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 /**
  * This class is used to test 'Active Sessions' dynamic attribute for 
  * both admin and the user at the Global and Realm levels. All the 
  * tests depend on two constraints :
  *  a)Exempt top-level admins (can be YES/NO) 
  *  b)Resulting behavior if session quota exhausted 
  *            (can be DENY_ACESS/DESTROY_OLD_SESSION)
  */
 public class GlobalRealmSessionConstraints extends TestCommon {
 
     private SSOToken admintoken;
     private IDMCommon idmc;
     private SMSCommon smsc;
     private Map map;
     private Map quotamap;
     private Set set;
     private String defActiveSessions = "5";
     private String testUser = "sessConsTest";
     private String testAdminUser = "sessConsAdminTest";    
     private String btladmin;
     private String resultBehavior;
     private String sessionsrvc = "iPlanetAMSessionService";
     private String dynSrvcType = "Dynamic";
     private String globalSrvcType = "Global";
     private String quotaAttr = "iplanet-am-session-quota-limit";
     private boolean dynSrvcRealmAssigned = false;
     private boolean cleanedUp = false;
     
     /**
     * SessionConstraints Constructor
     * Creates single admintoken and objects of common classes.
     * 
     * @throws java.lang.Exception
     */
     public GlobalRealmSessionConstraints() throws Exception {
         super("GlobalRealmSessionConstraints");
         admintoken = getToken(adminUser, adminPassword, basedn);
         idmc = new IDMCommon();  
         smsc = new SMSCommon(admintoken);
         quotamap = new HashMap();
     }
 
     /**
      * Initialization method. Setup:
      * (a) Creates adminuser, user Identities 
      * (b) Parameter inheritancelevel takes values Global/Realm
      * (c) Sets the session service at the inheritance level and 
      *     sets active number of sessions to "1"
      * 
      * @throws java.lang.Exception
      */
     @Parameters({"inheritancelevel"})
     @BeforeClass(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void setup(String inheritancelevel)
     throws Exception {
         entering("setup", null);
         try {
             idmc.createDummyUser(admintoken, realm, "", testAdminUser);
             idmc.addUserMember(admintoken, testAdminUser,
                     "Top-level Admin Role" , IdType.ROLE);
             log(Level.FINE,"setup", "Created user " + testAdminUser +
                     " identity and added Top-level Admin role to this user");
 
             idmc.createDummyUser(admintoken, realm, "", testUser);
             log(Level.FINE,"setup", "Created user " + 
                     testUser + " identity");
 
             if (inheritancelevel.equals("Global")) {
                 String activeSessionsBU = getGlobalSessionQuotaAttribute(
                         sessionsrvc, quotaAttr, dynSrvcType);
                 defActiveSessions = activeSessionsBU;
                 log(Level.FINE, "setup", "Number of active sessions before " +
                         "updating dynamic service attributes: "
                         + activeSessionsBU);
                 if (!activeSessionsBU.equals("1")) {
                     quotamap = fillQuotaMap(quotaAttr, "1");
                     smsc.updateGlobalServiceDynamicAttributes(sessionsrvc,
                             quotamap);
                     String activeSessionsAU = getGlobalSessionQuotaAttribute(
                         sessionsrvc, quotaAttr, dynSrvcType);
                     assert activeSessionsAU.equals("1");
                     log(Level.FINE, "setup", "Number of active sessions " +
                             "at Global level after updating dynamic service " +
                             "attributes: " + activeSessionsAU);
                 }
             } else if (inheritancelevel.equals("Realm")) {
                 if (smsc.isServiceAssigned(sessionsrvc, realm)) {
                     smsc.unassignDynamicServiceRealm(sessionsrvc, realm);
                 }
                 String globalActiveSessions = getGlobalSessionQuotaAttribute(
                         sessionsrvc, quotaAttr, dynSrvcType);
                 if (globalActiveSessions.equals("1")) {
                     quotamap = fillQuotaMap(quotaAttr, "5");
                     smsc.updateGlobalServiceDynamicAttributes(sessionsrvc,
                             quotamap);
                     globalActiveSessions = getGlobalSessionQuotaAttribute(
                             sessionsrvc, quotaAttr, dynSrvcType);
                 }
                 quotamap = fillQuotaMap(quotaAttr, "1");
                 smsc.assignDynamicServiceRealm(sessionsrvc, realm, quotamap);
                 dynSrvcRealmAssigned = true;
                 smsc.updateServiceAttrsRealm(sessionsrvc, realm, quotamap);
                 String realmActiveSessions = getRealmSessionQuotaAttribute(
                         sessionsrvc, realm);
                 assert !globalActiveSessions.equals("1");                
                 assert realmActiveSessions.equals("1");
                 log(Level.FINE, "setup", "Number of active sessions at " +
                         "Realm level after updating dynamic service " +
                         "attributes: " + realmActiveSessions);
             } 
         } catch(Exception e) {
             cleanup();
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
             throw e;
         }
         exiting("setup");
     }
     
     /**
      *  Tests the following case:
      * (a) Max session quota set to "1" in global/realm session service
      * (b) Set Exempt top-level admins from constraint checking to Yes
      * (c) Set Resulting behavior if session quota exhausted to
      *     DESTROY_OLD_SESSION
      * (d) User is super admin
      * (e) Validates that only one session can be created for user and old
      *     session is destroyed when a new session is created
      *     
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void testMaxSessionQuotaGlobalRealmYDADOSAdmin()
     throws Exception {
         entering("testMaxSessionQuotaGlobalRealmYDADOSAdmin", null);
         SSOToken ssotokenOrig = null;
         SSOToken ssotokenNew = null;
         try {
             if (getBtlAdminAttr().equals("YES")) {
                 ssotokenOrig = getToken(testAdminUser, 
                                         testAdminUser, basedn);
                 ssotokenNew = getToken(testAdminUser, 
                                         testAdminUser, basedn);
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmYDADOSAdmin", 
                         "Original token is valid and new " +
                         "token is valid here");               
                 assert validateToken(ssotokenOrig);
                 assert validateToken(ssotokenNew);
             } else {
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmYDADOSAdmin",
                         "Resulting behaviour and top level " +
                         "exempt attributes do not apply for this testcase");
             }
         } catch (Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmYDADOSAdmin",
                     e.getMessage()); 
             e.printStackTrace();
         } finally {
             if (!cleanedUp) {
                 destroyToken(ssotokenOrig);
                 destroyToken(ssotokenNew);
             }
         }
         exiting("testMaxSessionQuotaGlobalRealmYDADOSAdmin");
     }
 
     /**
      * Tests the following case:
      * (a) Max session quota set to 1 in global/realm session service
      * (b) Set Exempt top-level admins from constraint checking to No
      * (c) Set Resulting behavior if session quota exhausted to
      *     DESTROY_OLD_SESSION
      * (d) User is super admin
      * (e) Validates that only one session can be created for user and old
      *     session is destroyed when a new session is created
      * 
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testMaxSessionQuotaGlobalRealmYDADOSAdmin"})
     public void testMaxSessionQuotaGlobalRealmNDOSAdmin()
     throws Exception {
         entering("testMaxSessionQuotaGlobalRealmNDOSAdmin", null);
         SSOToken ssotokenOrig = null;
         SSOToken ssotokenNew = null;
         try {
             if (getBtlAdminAttr().equals("NO") && 
                     getresultBehaviorAttr().equals("DESTROY_OLD_SESSION")) {
                 ssotokenOrig = getToken(testAdminUser, testAdminUser, basedn);
                 set = new HashSet();
                 set = getAllUserTokens(admintoken, testAdminUser);
                 if (set.size() >= 2) {
                     assert false;
                 } else {
                     ssotokenNew = getToken(testAdminUser, 
                              testAdminUser, basedn);
                     log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDOSAdmin", 
                              "Original token is invalid and new " +
                              "token is valid here");
                     assert validateToken(ssotokenNew);
                 }
             } else {
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDOSAdmin",
                           "Resulting behaviour and top level "
                           + "exempt attributes do not apply for this testcase");
             }
         } catch(Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmNDOSAdmin", 
                     e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (!cleanedUp)
                 destroyToken(ssotokenNew);
         }
         exiting("testMaxSessionQuotaGlobalRealmNDOSAdmin");
     }
 
     /**
      * Tests the following case: 
      * (a) Max session quota set to 1 in global/realm session service 
      * (b) Set Exempt top-level admins from constraint checking to No 
      * (c) Set Resulting behavior if session quota exhausted to DENY_ACCESS
      * (d) User is super admin
      * (e) Validates that only one session
      *     can be created for admin and new session is denied access
      * 
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testMaxSessionQuotaGlobalRealmYDADOSAdmin"})
     public void testMaxSessionQuotaGlobalRealmNDAAdmin()
     throws Exception {
     	entering("testMaxSessionQuotaGlobalRealmNDAAdmin", null);        
         SSOToken ssotokenOrig = null;
         SSOToken ssotokenNew = null;
         try {
             if (getBtlAdminAttr().equals("NO") && 
                     getresultBehaviorAttr().equals("DENY_ACCESS")) {
                 ssotokenOrig = getToken(testAdminUser,
                         testAdminUser, basedn);
                 set = new HashSet();
                 set = getAllUserTokens(admintoken, testAdminUser);
                 if (set.size() >= 2) {
                     assert false;
                 } else {
                     try {
                         ssotokenNew = getToken(testAdminUser,
                                 testAdminUser, basedn);
                     } catch(Exception e) {
                       log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmNDAUser",
                                 "Cannot create new token: " + e.getMessage());
                         e.printStackTrace();
                     }
                     log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDAAdmin", 
                             "Original token is valid and new " +
                             "token is invalid here");
                     assert validateToken(ssotokenOrig);
                     assert !validateToken(ssotokenNew);
                 }
             } else {
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDAAdmin",
                         "Resulting behaviour and top level " +
                         "exempt attributes do not apply for this testcase");
             }
         } catch(Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmNDAAdmin", 
                     e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (!cleanedUp) {
                 destroyToken(ssotokenOrig);
                 destroyToken(ssotokenNew);                
             }
         }
         exiting("testMaxSessionQuotaGlobalRealmNDAAdmin");
     }
 
     /**
      * Tests the following case:
      * (a) Max session quota set to 1 in global/realm session service
      * (b) Set Exempt top-level admins from constraint checking to Yes
      * (c) Set Resulting behavior if session quota exhausted to
      *     DESTROY_OLD_SESSION
      * (d) User is not a super admin
      * (e) Validates that only one session can be created for user and old
      *     session is destroyed when a new session is created
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testMaxSessionQuotaGlobalRealmYDADOSAdmin"})
     public void testMaxSessionQuotaGlobalRealmYDOSUser()
     throws Exception {
         entering("testMaxSessionQuotaGlobalRealmYDOSUser", null);
         SSOToken ssotokenOrig = null;
         SSOToken ssotokenNew = null;
         try {
             if (getBtlAdminAttr().equals("YES") && 
                     getresultBehaviorAttr().equals("DESTROY_OLD_SESSION")) {
                 ssotokenOrig = getToken(testAdminUser, 
                         testAdminUser, basedn);
                 set = new HashSet();
                 set = getAllUserTokens(admintoken, testUser);
                 if (set.size() >= 2) {
                     assert false;
                 } else {
                     ssotokenNew = getToken(testAdminUser, 
                             testAdminUser, basedn);
                     log(Level.FINE, "testMaxSessionQuotaGlobalRealmYDOSUser", 
                             "Original token is invalid and new " +
                             "token is valid here");
                     assert validateToken(ssotokenNew);
                 }
             } else {
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmYDOSUser",
                         "Resulting behaviour and top level " +
                         "exempt attributes do not apply for this testcase");
             }
         } catch(Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmYDOSUser", 
                     e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (!cleanedUp) {
                 destroyToken(ssotokenOrig);
                 destroyToken(ssotokenNew);
             }
         }
         exiting("testMaxSessionQuotaGlobalRealmYDOSUser");
     }
 
     /**
      * Tests the following case:
      * (a) Max session quota set to 1 in global/realm session service
      * (b) Set Exempt top-level admins from constraint checking to No
      * (c) Set Resulting behavior if session quota exhausted to
      *     DESTROY_OLD_SESSION
      * (d) User is not a super admin
      * (e) Validates that only one session can be created for user and old
      *     session is destroyed when a new session is created
      *     
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testMaxSessionQuotaGlobalRealmYDADOSAdmin"})
     public void testMaxSessionQuotaGlobalRealmNDOSUser()
     throws Exception {
         entering("testMaxSessionQuotaGlobalRealmNDOSUser", null);
         SSOToken ssotokenOrig = null;
         SSOToken ssotokenNew = null;
         try {
             if (getBtlAdminAttr().equals("NO") && 
                     getresultBehaviorAttr().equals("DESTROY_OLD_SESSION")) {
                 ssotokenOrig = getToken(testAdminUser, 
                         testAdminUser, basedn);
                 set = new HashSet();
                 set = getAllUserTokens(admintoken, testUser);
                 if (set.size() >= 2) {
                     assert false;
                 } else {
                     ssotokenNew = getToken(testAdminUser, 
                             testAdminUser, basedn);
                     log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDOSUser", 
                             "Original token is invalid and new " +
                             "token is valid here");
                     assert validateToken(ssotokenNew);
                 }
             } else {
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDOSUser",
                         "Resulting behaviour and top level " +
                         "exempt attributes do not apply for this testcase");
             }
         } catch(Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmNDOSUser",
                     e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (!cleanedUp)
                 destroyToken(ssotokenNew);
         }
         exiting("testMaxSessionQuotaGlobalRealmNDOSUser");
     }
 
     /**
      * Tests the following case:
      * (a) Max session quota set to 1 in global/realm session service
      * (b) Set Exempt top-level admins from constraint checking to No
      * (c) Set Resulting behavior if session quota exhausted to
      *     DENY_ACCESS
      * (d) User is not a super admin
      * (e) Validates that only one session can be created for admin and 
      *     new session is denied access
      *     
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testMaxSessionQuotaGlobalRealmYDADOSAdmin"})
     public void testMaxSessionQuotaGlobalRealmNDAUser()
     throws Exception {
         entering("testMaxSessionQuotaGlobalRealmNDAUser", null);
         SSOToken ssotokenOrig = null;
         SSOToken ssotokenNew = null;
         try {
             if (getBtlAdminAttr().equals("NO") && 
                     getresultBehaviorAttr().equals("DENY_ACCESS")) {
                 ssotokenOrig = getToken(testUser, 
                         testUser, basedn);
                 set = new HashSet();
                 set = getAllUserTokens(admintoken, testUser);
                 if (set.size() >= 2) {
                     assert false;
                 } else {
                     try {
                         ssotokenNew = getToken(adminUser, 
                                 adminPassword, basedn);
                     } catch(Exception e) {
                       log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmNDAUser",
                                 "Cannot create new token: " + e.getMessage());
                         e.printStackTrace();
                     } 
                     log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDAUser", 
                             "Original token is valid and new " +
                             "token is invalid here");
                     assert validateToken(ssotokenOrig);
                     assert !validateToken(ssotokenNew);
                 }
             } else {
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmNDAUser",
                         "Resulting behaviour and top level " +
                         "exempt attributes do not apply for this testcase");
             }
         } catch(Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmNDAUser", 
                     e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (!cleanedUp) {
                 destroyToken(ssotokenOrig);
                 destroyToken(ssotokenNew);
             }
         }
         exiting("testMaxSessionQuotaGlobalRealmNDAUser");
     }
     
     /**
      * Tests the following case:
      * (a) Max session quota set to 1 in global/realm session service
      * (b) Set Exempt top-level admins from constraint checking to Yes
      * (c) Set Resulting behavior if session quota exhausted to
      *     DENY_ACCESS
      * (d) User is not a super admin
      * (e) Validates that only one session can be created for admin 
      *     and new session is denied access
      *     
      * @throws java.lang.Exception
      */
     @Test(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"}, 
     dependsOnMethods = {"testMaxSessionQuotaGlobalRealmYDADOSAdmin"})
     public void testMaxSessionQuotaGlobalRealmYDAUser()
     throws Exception {
         entering("testMaxSessionQuotaGlobalRealmYDAUser", null);
         SSOToken ssotokenOrig = null;
         SSOToken ssotokenNew = null;
         try {
             if (getBtlAdminAttr().equals("YES") && 
                     getresultBehaviorAttr().equals("DENY_ACCESS")) {
                 ssotokenOrig = getToken(testUser, 
                         testUser, basedn);
                 set = new HashSet();
                 set = getAllUserTokens(admintoken, testUser);
                 if (set.size() >= 2) {
                     assert false;
                 } else {
                     try{
                         ssotokenNew = getToken(testUser, 
                                 testUser, basedn);
                     } catch(Exception e) {
                       log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmNDAUser",
                                 "Cannot create new token: " + e.getMessage());
                         e.printStackTrace();
                     }
                     log(Level.FINE, "testMaxSessionQuotaGlobalRealmYDAUser", 
                             "Original token is valid and new " +
                             "token is invalid here");
                     assert validateToken(ssotokenOrig);
                     assert !validateToken(ssotokenNew);
                 }
             } else {
                 log(Level.FINE, "testMaxSessionQuotaGlobalRealmYDAUser",
                         "Resulting behaviour and top level " +
                         "exempt attributes do not apply for this testcase");
             }
         } catch(Exception e) {
             cleanup();
             cleanedUp = true;
             log(Level.SEVERE, "testMaxSessionQuotaGlobalRealmYDAUser", 
                     e.getMessage());
             e.printStackTrace();
             throw e;
         } finally {
             if (!cleanedUp) {
                 destroyToken(ssotokenOrig);
                 destroyToken(ssotokenNew);
             }
         }
         exiting("testMaxSessionQuotaGlobalRealmYDAUser");
     }
     
     /**
      * Cleans up the testcase attributes set in setup
      * 
      * @throws java.lang.Exception
      */
     @AfterClass(groups = {"ds_ds", "ds_ds_sec", "ff_ds", "ff_ds_sec"})
     public void cleanup()
     throws Exception {
         entering("cleanup", null);
         try {
             if (dynSrvcRealmAssigned) {
                 smsc.unassignDynamicServiceRealm(sessionsrvc, realm);
             } 
             log(Level.FINEST, "cleanup", "Cleaning TestAdminUser: " 
                     + testAdminUser);
             idmc.removeUserMember(admintoken, testAdminUser,
                     "Top-level Admin Role" , IdType.ROLE, realm);
             idmc.deleteIdentity(admintoken, realm, IdType.USER, testAdminUser);
             log(Level.FINEST, "cleanup", "Cleaning User: " + testUser);
             idmc.deleteIdentity(admintoken, realm, IdType.USER,
                     testUser);
             quotamap = fillQuotaMap(quotaAttr, defActiveSessions);
             smsc.updateGlobalServiceDynamicAttributes(
                     sessionsrvc, quotamap);
         } catch (Exception e) {
             log(Level.SEVERE, "cleanup", e.getMessage());
             e.printStackTrace();
             throw e;
        }
         exiting("cleanup");
     }
     
     /**
      * @return String - Exempt top-level admins from constraint checking
      *                  Global attribute value (Bypass Top Level Admin)
      *                    
      * @throws java.lang.Exception
      */
     private String getBtlAdminAttr()
     throws Exception {
         set = new HashSet();
         set = smsc.getAttributeValueFromSchema(sessionsrvc,
                 "iplanet-am-session-enable-session-constraint-bypass-" +
                 "topleveladmin", globalSrvcType);
         Iterator itr = set.iterator();
         btladmin = (String) itr.next();
         return btladmin;
     }
 
     /**
      * 
      * @return String - Resulting behavior if session quota exhausted
      * 					Global attribute value
      * 
      * @throws java.lang.Exception
      */
     private String getresultBehaviorAttr()
     throws Exception {
         set = smsc.getAttributeValueFromSchema(sessionsrvc,
                 "iplanet-am-session-constraint-resulting-behavior", 
                 globalSrvcType);
         Iterator itr = set.iterator();
         resultBehavior = (String) itr.next();
         return resultBehavior;
     }
     
     /**
      * @param serviceName contains servicename as String
      * @param attributeName name of attribute in the service
      * @param attributeType type of the attribute (User/Role etc)
      * @return String containing global active sessions
      * 
      * @throws java.lang.Exception
      */
     private String getGlobalSessionQuotaAttribute(String serviceName, 
             String attributeName, 
             String attributeType) 
     throws Exception {
         String globalActiveSessions = "0";
         set = smsc.getAttributeValueFromSchema(serviceName,
               attributeName, attributeType);
         Iterator itr = set.iterator();
         globalActiveSessions = (String)itr.next();
         return globalActiveSessions;
     }
 
     /**
      * @param serviceName contains servicename as String
      * @param realm contains realm name as String
      * @return String containing realm active sessions
      * 
      * @throws java.lang.Exception
      */
     private String getRealmSessionQuotaAttribute(String serviceName, 
             String realm) 
     throws Exception {
         String realmActiveSessions = "0";
         map = new HashMap();
         map = smsc.getDynamicServiceAttributeRealm(serviceName, realm);
         set = (Set)map.get(quotaAttr);
         Iterator iter = set.iterator();
         realmActiveSessions = (String)iter.next();
         return realmActiveSessions;
     }
     
     /**
      * @param quotaattr Quota Schema Attribute
      * @param quotavalue Value to be set 
      * @return Map map with quota value set
      * 
      * @throws java.lang.Exception
      */
     private Map fillQuotaMap(String quotaattr, String quotavalue) 
     throws Exception {
         Map quotaMap = new HashMap();
         set = new HashSet();
         set.add(quotavalue);
         quotaMap.put(quotaattr, set);
         return quotaMap;
     }
 }
