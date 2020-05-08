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
 * $Id: SetupProduct.java,v 1.6 2007-09-27 21:51:53 rmisra Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.setup;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.qatest.common.FederationManager;
 import com.sun.identity.qatest.common.LDAPCommon;
 import com.sun.identity.qatest.common.SMSCommon;
 import com.sun.identity.qatest.common.SMSConstants;
 import com.sun.identity.qatest.common.TestCommon;
 import com.sun.identity.qatest.common.TestConstants;
 import java.util.logging.Level;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.PropertyResourceBundle;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 /**
  * This class calls the TestCommon.configureProduct method to configure
  * the product. In case product is already configured, nothing happens.
  */
 public class SetupProduct extends TestCommon {
 
     private String FILE_CLIENT_PROPERTIES = "AMConfig.properties";
     private Map properties = new HashMap();
     private SMSCommon smsc;
     private SSOToken admintoken;
 
     /**
      * If the system is execution in single server mode, it makes a call to
      * configure the product. If system is execution in multi server mode
      * (samlv2 etc), no product configuration call is made.
      */
     public SetupProduct(String serverName1, String serverName2)
     throws Exception
     {
         super("SetupProduct");
       
         try {
             boolean bserver1;
             boolean bserver2;
 
             if ((serverName1.indexOf("SERVER_NAME1") == -1) &&
                     (serverName2.indexOf("SERVER_NAME2") == -1)) {
                 log(Level.FINE, "SetupProduct", "Initiating setup for " +
                     serverName1);
                 ResourceBundle cfg1 = ResourceBundle.
                         getBundle("Configurator-" + serverName1);
                 String strURL = cfg1.
                         getString(TestConstants.KEY_ATT_NAMING_SVC);
                 log(Level.FINE, "SetupProduct", "Server URL: " + strURL);
                 Map map = getURLComponents(strURL);
                 log(Level.FINE, "SetupProduct", "Server URL Components: " +
                         map);
                 String protocol = (String)map.get("protocol");
                 String host = (String)map.get("host");
                 String port = (String)map.get("port");
                 String uri = (String)map.get("uri");
                 bserver1 = configureProduct(getConfigurationMap("Configurator-"
                         + serverName1, protocol, host, port, uri));
                 if (!bserver1) {
                     log(Level.FINE, "SetupProduct",
                             "Configuration failed for " + serverName1);
                     assert false;
                 } else {
                     String strUMDatastore1 = cfg1.getString("umdatastore");
                     if (!(strUMDatastore1.equals("embedded"))) {
                         String adminUser = cfg1.getString(
                                 TestConstants.KEY_ATT_AMADMIN_USER);
                         String adminPassword = cfg1.getString(
                                 TestConstants.KEY_ATT_AMADMIN_PASSWORD);
 
                         String loginURL = protocol + ":" + "//" + host + ":" +
                                 port + uri + "/UI/Login";
                         String logoutURL = protocol + ":" + "//" + host + ":" +
                                 port + uri + "/UI/Logout";
                         String famadmURL = protocol + ":" + "//" + host + ":" +
                                 port + uri;
 
                         List list = new ArrayList();
                         ResourceBundle cfg1Data =
                                 ResourceBundle.getBundle("configGlobalData");
                         int dCount = new Integer(cfg1Data.getString(
                                 SMSConstants.SMS_DATASTORE_PARAMS_PREFIX +
                                 "0." +
                                 SMSConstants.SMS_DATASTORE_COUNT)).intValue();
                         WebClient webClient = null;
                         try {
                             FederationManager famadm =
                                     new FederationManager(famadmURL);
                             webClient = new WebClient();
                             consoleLogin(webClient, loginURL, adminUser,
                                     adminPassword);
 
                             String dsRealm = null;
                             String dsType;
                             String dsName;
                             String adminId;
                             String ldapServer;
                             String ldapPort;
                             String dsAdminPassword;
                             String orgName;
                             String sslEnabled;
                             String authId;
                             String psearchBase;
                             String groupAtt;
                             String roleAtt;
                             String userObjClass;
                             String userAtt;
 
                             for (int i = 0; i < dCount; i++) {
                                 dsType = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_DATASTORE_TYPE + "." +
                                         i);
                                 dsName = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_DATASTORE_NAME + "." +
                                         i);
                                 adminId = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_DATASTORE_ADMINID +
                                         "." + i);
                                 dsRealm = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_DATASTORE_REALM +
                                         "." + i);
                                 ldapServer = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_LDAP_SERVER +
                                         "." + i);
                                 ldapPort = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_LDAP_PORT +
                                         "." + i);
                                 dsAdminPassword = cfg1Data.getString(
                                         SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_DATASTORE_ADMINPW +
                                         "." + i);
                                 orgName = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.
                                         SMS_LDAPv3_ORGANIZATION_NAME + "." + i);
                                 sslEnabled = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_LDAP_SSL_ENABLED
                                         + "." + i);
                                 authId = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_AUTHID
                                         + "." + i);
                                 psearchBase = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_LDAP_PSEARCHBASE
                                         + "." + i);
                                 groupAtt = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_GROUP_ATTR
                                         + "." + i);
                                 roleAtt = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_ROLE_ATTR
                                         + "." + i);
                                 userObjClass = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_USER_OBJECT_CLASS
                                         + "." + i);
                                 userAtt = cfg1Data.getString(SMSConstants.
                                         SMS_DATASTORE_PARAMS_PREFIX + "0." +
                                         SMSConstants.SMS_LDAPv3_USER_ATTR
                                         + "." + i);
                                 list.add(SMSConstants.
                                         SMS_LDAPv3_LDAP_SERVER + "=" +
                                         ldapServer + ":" + ldapPort);
                                 list.add(SMSConstants.
                                         SMS_LDAPv3_AUTHPW + "=" +
                                         dsAdminPassword);
                                 list.add(SMSConstants.
                                         SMS_LDAPv3_ORGANIZATION_NAME + "=" +
                                         orgName);
                                 list.add(SMSConstants.
                                         SMS_LDAPv3_LDAP_SSL_ENABLED + "=" +
                                         sslEnabled);
                                 list.add(SMSConstants.
                                         SMS_LDAPv3_AUTHID + "=" +
                                         authId);
                                 list.add(SMSConstants.
                                         SMS_LDAPv3_LDAP_PSEARCHBASE + "=" +
                                         psearchBase);
                                 if (groupAtt.indexOf("|") != 0) {
                                     List locList = getAttributeList(groupAtt, "|");
                                     for (int j = 0; j < locList.size(); j++) {
                                         list.add(SMSConstants.
                                             SMS_LDAPv3_GROUP_ATTR + "=" +
                                             (String)locList.get(j));
                                     }
                                 } else {
                                     list.add(SMSConstants.
                                         SMS_LDAPv3_GROUP_ATTR + "=" +
                                         groupAtt);
                                 }
                                 if (roleAtt.indexOf("|") != 0) {
                                     List locList = getAttributeList(roleAtt, "|");
                                     for (int j = 0; j < locList.size(); j++) {
                                         list.add(SMSConstants.
                                             SMS_LDAPv3_ROLE_ATTR + "=" +
                                             (String)locList.get(j));
                                     }
                                 } else {
                                     list.add(SMSConstants.
                                         SMS_LDAPv3_ROLE_ATTR + "=" +
                                         roleAtt);
                                 }
                                 if (userObjClass.indexOf("|") != 0) {
                                     List locList = getAttributeList(userObjClass, "|");
                                     for (int j = 0; j < locList.size(); j++) {
                                         list.add(SMSConstants.
                                             SMS_LDAPv3_USER_OBJECT_CLASS + "=" +
                                             (String)locList.get(j));
                                     }
                                 } else {
                                     list.add(SMSConstants.
                                         SMS_LDAPv3_USER_OBJECT_CLASS + "=" +
                                         userObjClass);
                                 }
                                 if (userAtt.indexOf("|") != 0) {
                                     List locList = getAttributeList(userAtt, "|");
                                     for (int j = 0; j < locList.size(); j++) {
                                         list.add(SMSConstants.
                                             SMS_LDAPv3_USER_ATTR + "=" +
                                             (String)locList.get(j));
                                     }
                                 } else {
                                     list.add(SMSConstants.
                                         SMS_LDAPv3_USER_ATTR + "=" +
                                         userAtt);
                                 }
                                 log(Level.FINEST, "setup", "Datastore" +
                                         " attributes list:" + list);
                                 if (getHtmlPageStringIndex(
                                         famadm.listDatastores(webClient,
                                         dsRealm), dsName) == -1)
                                     famadm.createDatastore(webClient, dsRealm,
                                             dsName, dsType, list);
                                 list.clear();
                                 LDAPCommon ldc = new LDAPCommon(ldapServer,
                                         ldapPort, adminId, dsAdminPassword,
                                         orgName);
                                 ResourceBundle smsGblCfg = ResourceBundle.
                                         getBundle("SMSGlobalConfig");
                                 String schemaString = (String)smsGblCfg.
                                         getString(SMSConstants.SMS_SCHEMNA_LIST
                                         + "." +
                                         SMSConstants.SMS_DATASTORE_TYPE_AMDS); 
                                 String schemaAttributes = (String)smsGblCfg.
                                         getString(SMSConstants.SMS_SCHEMNA_ATTR
                                         + "." +
                                         SMSConstants.SMS_DATASTORE_TYPE_AMDS);
                                 ldc.loadAMUserSchema(schemaString,
                                         schemaAttributes);
                             }
                            list.add("files");
                             if (getHtmlPageStringIndex(
                                     famadm.listDatastores(webClient, dsRealm),
                                    "files") != -1)
                                 famadm.deleteDatastores(webClient, realm, list);
                         } catch (Exception e) {
                             log(Level.SEVERE, "setup", e.getMessage());
                             e.printStackTrace();
                         } finally {
                             consoleLogout(webClient, logoutURL);
                         }
                     }
                 }
 
                 log(Level.FINE, "SetupProduct", "Initiating setup for " +
                     serverName2);
                 bserver2 = configureProduct(
                         getConfigurationMap("Configurator-" + serverName2));
                 if (!bserver2) {
                     log(Level.FINE, "SetupProduct", "Configuration failed" +
                             " for " + serverName2);
                     assert false;
                 } else {
                     ResourceBundle cfg2 =
                             ResourceBundle.getBundle("Configurator-" +
                             serverName2);
                     String strUMDatastore2 = cfg2.getString("umdatastore");
                     log(Level.FINE, "SetupProduct", "UM Datastore for " +
                             serverName2 + " is " + strUMDatastore2);
                     if (!(strUMDatastore2.equals("embedded"))) {
                         admintoken = getToken(adminUser, adminPassword, basedn);
                         smsc = new SMSCommon(admintoken, "SMSGlobalConfig");
                         smsc.createDataStore(1, "configGlobalData");
                        smsc.deleteDataStore(realm, "files");
                     }
                 }
             } else if ((serverName1.indexOf("SERVER_NAME1") == -1) &&
                     (serverName2.indexOf("SERVER_NAME2") != -1)) {
                 bserver1 = configureProduct(getConfigurationMap("Configurator-"
                         + serverName1));
                 if (!bserver1) {
                     log(Level.FINE, "SetupProduct", "Configuration failed for" +
                             " " + serverName1);
                     setSingleServerSetupFailedFlag();
                     assert false;
                 } else {
                     ResourceBundle cfg1 =
                             ResourceBundle.getBundle("Configurator-" +
                             serverName1);
                     String strUMDatastore1 = cfg1.getString("umdatastore");
                     log(Level.FINE, "SetupProduct", "UM Datastore for " +
                             serverName1 + " is " + strUMDatastore1);
                     if (!(strUMDatastore1.equals("embedded"))) {
                         admintoken = getToken(adminUser, adminPassword, basedn);
                         smsc = new SMSCommon(admintoken, "SMSGlobalConfig");
                         smsc.createDataStore(1, "configGlobalData");
                        smsc.deleteDataStore(realm, "files");
                     }
                 }
             } else if ((serverName1.indexOf("SERVER_NAME1") != -1) &&
                     (serverName2.indexOf("SERVER_NAME2") == -1)) {
                 log(Level.FINE, "SetupProduct", "Unsupported configuration." +
                         " Cannot have SERVER_NAME2 specified without" +
                         " SERVER_NAME1.");
                 assert false;
             }
         } catch(Exception e) {
             log(Level.SEVERE, "setup", e.getMessage());
             e.printStackTrace();
         } finally {
             destroyToken(admintoken);
         }
     }
 
     public void setSingleServerSetupFailedFlag() {
         entering("setSingleServerSetupFailedFlag", null);
         try {
             log(Level.FINE, "setSingleServerSetupFailedFlag", "Single server" +
                     " product configuration failed.");
             Set set = new HashSet();
             set.add((String)TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT);
             properties = getMapFromResourceBundle("AMConfig", set);
             properties.put(TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT, "fail");
             createFileFromMap(properties, "resources" + fileseparator +
                         FILE_CLIENT_PROPERTIES);
         } catch(Exception e) {
             log(Level.SEVERE, "setSingleServerSetupFailedFlag", e.getMessage());
             e.printStackTrace();
         }
         exiting("setSingleServerSetupFailedFlag");
     }
 
     public static void main(String args[]) {
         try {
             SetupProduct cp = new SetupProduct(args[0], args[1]);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
