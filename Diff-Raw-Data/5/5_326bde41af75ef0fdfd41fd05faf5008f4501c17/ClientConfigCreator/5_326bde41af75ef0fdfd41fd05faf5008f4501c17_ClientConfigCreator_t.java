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
 * $Id: ClientConfigCreator.java,v 1.18 2008-03-05 18:00:06 mrudulahg Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.setup;
 
 import com.sun.identity.qatest.common.TestConstants;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.net.InetAddress;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.PropertyResourceBundle;
 
 /**
  * This class does the following:
  * (a) This class creates the tag swapped AMConfig.properties file. It takes 
  *     AMClient.properties and Configurator-<server name> files under resources 
  *     directory and create a new file called AMConfig.properties file by tag
  *     swapping attribute annotated with values as @COPY_FROM_CONFIG@ with
  *     values read from Configurator-<server name> file specified under the
  *     resources directory. This  AMConfig.properties is  the client side
  *     AMConfig.properties file.
  * (b) This class creates the tag swapped multi server config data properties
  *     file. It takes the two Configurator-<server name> files under resources 
  *     directory and create a new config file (the name of this file is 
  *     specific to the multi server mode under execution. For eg for samlv2 its
  *     called  samlv2TestConfigData) by tag swapping attribute annotated with
  *     values as @COPY_FROM_CONFIG@ with values read from
  *     Configurator-<server name> file specified under the resources directory.
  *     This file is consumed by the respective module to do its configuration.
  */
 public class ClientConfigCreator { 
 
     private String newline = System.getProperty("line.separator");
     private String fileseparator = System.getProperty("file.separator");
     private String uriseparator = "/";
     private String hostname;
     private Map properties_ss = new HashMap();
     private Map properties_saml = new HashMap();
     private Map properties_idff = new HashMap();
     private Map properties_wsfed = new HashMap();
     private Map properties_sae = new HashMap();
     private String ALL_FILE_CLIENT_PROPERTIES = "resources" + fileseparator +
             "AMConfig.properties";
     private String SAML_FILE_CLIENT_PROPERTIES =
             "resources" + fileseparator + "samlv2" + fileseparator +
             "samlv2TestConfigData.properties";
     private String IDFF_FILE_CLIENT_PROPERTIES =
             "resources" + fileseparator + "idff" + fileseparator +
             "idffTestConfigData.properties";
     private String WSFED_FILE_CLIENT_PROPERTIES =
             "resources" + fileseparator + "wsfed" + fileseparator +
             "WSFedTestConfigData.properties";
     private String SAE_FILE_CLIENT_PROPERTIES =
             "resources" + fileseparator + "sae" + fileseparator +
             "saeTestConfigData.properties";
 
     /**
      * Default constructor. Calls method to transfer properties from:
      * (a) default AMClient.properties to client AMConfig.properties file. 
      * (b) multiple configuration files to a single multi server execution mode
      *     file.
      */
     public ClientConfigCreator(String testDir, String serverName1,
             String serverName2, String executionMode)
         throws Exception {
 
         InetAddress addr = InetAddress.getLocalHost();
         hostname = addr.getCanonicalHostName();
 
         if ((serverName2.indexOf("SERVER_NAME2")) != -1) {
             getDefaultValues(testDir, serverName1);
         } else {
             PropertyResourceBundle configDef1 = new PropertyResourceBundle(
                     new FileInputStream(testDir + fileseparator + "resources" +
                     fileseparator + "Configurator-" +
                     serverName1 + ".properties"));
             if (configDef1.getString(TestConstants.
                     KEY_ATT_MULTIPROTOCOL_ENABLED).equalsIgnoreCase("true")) {
                 getDefaultValues(testDir, serverName1, 
                         configDef1.getString(TestConstants.KEY_ATT_IDFF_SP), 
                         properties_idff);
                 getDefaultValues(testDir, serverName1, 
                         configDef1.getString(TestConstants.KEY_ATT_WSFED_SP), 
                         properties_wsfed);
                 getDefaultValues(testDir, serverName1, serverName2, 
                         properties_saml);
             } else {
                 getDefaultValues(testDir, serverName1, serverName2, 
                         properties_saml);
                 getDefaultValues(testDir, serverName1, serverName2, 
                         properties_idff);
                 getDefaultValues(testDir, serverName1, serverName2, 
                         properties_wsfed);
                 getDefaultValues(testDir, serverName1, serverName2, 
                         properties_sae);
             }
             createFileFromMap(properties_saml, SAML_FILE_CLIENT_PROPERTIES);
             createFileFromMap(properties_idff, IDFF_FILE_CLIENT_PROPERTIES);
             createFileFromMap(properties_wsfed, WSFED_FILE_CLIENT_PROPERTIES);
             createFileFromMap(properties_sae, SAE_FILE_CLIENT_PROPERTIES);
         }
         createFileFromMap(properties_ss, ALL_FILE_CLIENT_PROPERTIES);
     }
 
     /**
      * Method to do the actual transfer of properties from  default
      * AMClient.properties to client AMConfig.properties file.
      */
     private void getDefaultValues(String testDir, String serverName)
         throws Exception {
 
         PropertyResourceBundle configDef = new PropertyResourceBundle(
             new FileInputStream(testDir + fileseparator + "resources" +
                 fileseparator + "Configurator-" + serverName + ".properties"));
 
         PropertyResourceBundle clientDef = new PropertyResourceBundle(
             new FileInputStream(testDir + fileseparator + "resources" +
                 fileseparator + "AMClient.properties"));
 
         String strNamingURL = configDef.getString(
                 TestConstants.KEY_ATT_NAMING_SVC);
 
         int iFirstSep = strNamingURL.indexOf(":");
         String strProtocol = strNamingURL.substring(0, iFirstSep);  
 
         int iSecondSep = strNamingURL.indexOf(":", iFirstSep + 1);
         String strHost = strNamingURL.substring(iFirstSep + 3, iSecondSep);
 
         int iThirdSep = strNamingURL.indexOf(uriseparator, iSecondSep + 1);
         String strPort = strNamingURL.substring(iSecondSep + 1, iThirdSep);
 
         int iFourthSep = strNamingURL.indexOf(uriseparator, iThirdSep + 1);
         String strURI = uriseparator + strNamingURL.substring(iThirdSep + 1,
                 iFourthSep);
 
         for (Enumeration e = clientDef.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             String value = (String)clientDef.getString(key);
 
             if (value.equals("@COPY_FROM_CONFIG@")) {
                 if (key.equals(TestConstants.KEY_AMC_PROTOCOL))
                     value = strProtocol;
                 else if (key.equals(TestConstants.KEY_AMC_HOST))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_AMC_PORT))
                     value = strPort;
                 else if (key.equals(TestConstants.KEY_AMC_URI))
                     value = strURI;
                 else if (key.equals(TestConstants.KEY_AMC_NAMING_URL))
                     value = strNamingURL;
                 else if (key.equals(TestConstants.KEY_AMC_BASEDN))
                     value = configDef.getString(
                             TestConstants.KEY_ATT_CONFIG_ROOT_SUFFIX);
                 else if (key.equals(TestConstants.KEY_AMC_SERVICE_PASSWORD))
                     value = configDef.getString(
                             TestConstants.KEY_ATT_SERVICE_PASSWORD);
                 else if (key.equals(TestConstants.KEY_ATT_AM_ENC_PWD))
                     value = configDef.getString(
                             TestConstants.KEY_ATT_AM_ENC_KEY);
                 else if (key.equals(TestConstants.KEY_AMC_WSC_CERTALIAS))
                     value = configDef.getString(
                             TestConstants.KEY_AMC_WSC_CERTALIAS);
                 else if (key.equals(TestConstants.KEY_AMC_KEYSTORE))
                     value = configDef.getString(
                             TestConstants.KEY_AMC_KEYSTORE);
                 else if (key.equals(TestConstants.KEY_AMC_KEYPASS))
                     value = configDef.getString(
                             TestConstants.KEY_AMC_KEYPASS);
                 else if (key.equals(TestConstants.KEY_AMC_STOREPASS))
                     value = configDef.getString(
                             TestConstants.KEY_AMC_STOREPASS);
                 else if (key.equals(TestConstants.KEY_AMC_XMLSIG_CERTALIAS))
                     value = configDef.getString(
                             TestConstants.KEY_AMC_XMLSIG_CERTALIAS);
                 else if (key.equals(TestConstants.KEY_AMC_IDM_CACHE_ENABLED))
                     value = configDef.getString(
                             TestConstants.KEY_AMC_IDM_CACHE_ENABLED);
                 else if (key.equals(TestConstants.KEY_AMC_AUTHNSVC_URL))
                     value = strProtocol + "://" + strHost + ":" + strPort +
                             strURI + "/" + "Liberty/authnsvc";
                 else if (key.equals(TestConstants.KEY_AMC_NOTIFICATION_URL))
                     value = "http://" + hostname + ":" + configDef.getString(
                             TestConstants.KEY_ATT_NOTIFICATION_URI);
             }
             value = value.replace("@BASE_DIR@", testDir + fileseparator +
                     serverName);
             properties_ss.put(key, value);
         }
 
         for (Enumeration e = configDef.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             String value = (String)configDef.getString(key);
             if (!key.equals(TestConstants.KEY_ATT_NAMING_SVC) &&
                     !key.equals(TestConstants.KEY_ATT_DEFAULTORG) &&
                     !key.equals(TestConstants.KEY_ATT_METAALIAS)  &&
                     !key.equals(TestConstants.KEY_ATT_ENTITY_NAME) &&
                     !key.equals(TestConstants.KEY_ATT_COT) &&
                     !key.equals(TestConstants.KEY_ATT_CERTALIAS) &&
                     !key.equals(TestConstants.KEY_ATT_PROTOCOL) &&
                     !key.equals(TestConstants.KEY_ATT_HOST) &&
                     !key.equals(TestConstants.KEY_ATT_PORT) &&
                     !key.equals(TestConstants.KEY_ATT_DEPLOYMENT_URI)) {
                 properties_ss.put(key, value);
             }
         }
   
         properties_ss.put(TestConstants.KEY_ATT_SERVER_NAME, serverName);
     }
 
     /**
      * Method to do the actual transfer of properties from  default
      * configuration files to single multi server execution mode config data
      * file.
      */
     private void getDefaultValues(String testDir, String serverName1,
             String serverName2, Map properties_protocol)
         throws Exception {
 
         PropertyResourceBundle configDef1 = new PropertyResourceBundle(
             new FileInputStream(testDir + fileseparator + "resources" +
                 fileseparator + "Configurator-" +
                 serverName1 + ".properties"));
 
         String strNamingURL = configDef1.getString(
                 TestConstants.KEY_ATT_NAMING_SVC);
 
         int iFirstSep = strNamingURL.indexOf(":");
         String strProtocol = strNamingURL.substring(0, iFirstSep);  
 
         int iSecondSep = strNamingURL.indexOf(":", iFirstSep + 1);
         String strHost = strNamingURL.substring(iFirstSep + 3, iSecondSep);
 
         int iThirdSep = strNamingURL.indexOf(uriseparator, iSecondSep + 1);
         String strPort = strNamingURL.substring(iSecondSep + 1, iThirdSep);
 
         int iFourthSep = strNamingURL.indexOf(uriseparator, iThirdSep + 1);
         String strURI = uriseparator + strNamingURL.substring(iThirdSep + 1,
                 iFourthSep);
 
         for (Enumeration e = configDef1.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             String value = (String)configDef1.getString(key);
 
             if (value.equals("@COPY_FROM_CONFIG@")) {
                 if (key.equals(TestConstants.KEY_ATT_PROTOCOL))
                     value = strProtocol;
                 else if (key.equals(TestConstants.KEY_ATT_HOST))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_ATT_PORT))
                     value = strPort;
                 else if (key.equals(TestConstants.KEY_ATT_DEPLOYMENT_URI))
                     value = strURI;
                 else if (key.equals(TestConstants.KEY_ATT_METAALIAS))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_ATT_ENTITY_NAME))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_ATT_COT))
                     value = "idpcot";
             }
             if (key.equals(TestConstants.KEY_ATT_METAALIAS)) {
                 if (!configDef1.getString(TestConstants.
                         KEY_ATT_EXECUTION_REALM).endsWith("/")) {
                     value = configDef1.getString(
                             TestConstants.KEY_ATT_EXECUTION_REALM) + "/" + 
                             value;
                 } else {
                     value = configDef1.getString(
                            TestConstants.KEY_ATT_EXECUTION_REALM) + 
                             value;
                 }
             }
             if (!key.equals(TestConstants.KEY_ATT_NAMING_SVC) &&
                     !key.equals(TestConstants.KEY_ATT_DEFAULTORG) &&
                     !key.equals(TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT) &&
                     !key.equals(TestConstants.KEY_ATT_LOG_LEVEL))
             properties_protocol.put("idp_" + key, value);
         }
 
         PropertyResourceBundle configDef2 = new PropertyResourceBundle(
             new FileInputStream(testDir + fileseparator + "resources" +
                 fileseparator + "Configurator-" +
                 serverName2 + ".properties"));
 
         strNamingURL = configDef2.getString(TestConstants.KEY_ATT_NAMING_SVC);
  
         iFirstSep = strNamingURL.indexOf(":");
         strProtocol = strNamingURL.substring(0, iFirstSep);
 
         iSecondSep = strNamingURL.indexOf(":", iFirstSep + 1);
         strHost = strNamingURL.substring(iFirstSep + 3, iSecondSep);
 
         iThirdSep = strNamingURL.indexOf(uriseparator, iSecondSep + 1);
         strPort = strNamingURL.substring(iSecondSep + 1, iThirdSep);
 
         iFourthSep = strNamingURL.indexOf(uriseparator, iThirdSep + 1);
         strURI = uriseparator + strNamingURL.substring(iThirdSep + 1,
                 iFourthSep);
 
         PropertyResourceBundle clientDef = new PropertyResourceBundle(
             new FileInputStream(testDir + fileseparator + "resources" +
                 fileseparator + "AMClient.properties"));
 
         for (Enumeration e = clientDef.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             String value = (String)clientDef.getString(key);
 
             if (value.equals("@COPY_FROM_CONFIG@")) {
                 if (key.equals(TestConstants.KEY_AMC_PROTOCOL))
                     value = strProtocol;
                 else if (key.equals(TestConstants.KEY_AMC_HOST))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_AMC_PORT))
                     value = strPort;
                 else if (key.equals(TestConstants.KEY_AMC_URI))
                     value = strURI;
                 else if (key.equals(TestConstants.KEY_AMC_NAMING_URL))
                     value = strNamingURL;
                 else if (key.equals(TestConstants.KEY_AMC_BASEDN))
                     value = configDef2.getString(
                             TestConstants.KEY_ATT_CONFIG_ROOT_SUFFIX);
                 else if (key.equals(TestConstants.KEY_AMC_SERVICE_PASSWORD))
                     value = configDef2.getString(
                             TestConstants.KEY_ATT_SERVICE_PASSWORD);
                 else if (key.equals(TestConstants.KEY_ATT_AM_ENC_PWD))
                     value = configDef2.getString(
                             TestConstants.KEY_ATT_AM_ENC_KEY);
                 else if (key.equals(TestConstants.KEY_AMC_WSC_CERTALIAS))
                     value = configDef2.getString(
                             TestConstants.KEY_AMC_WSC_CERTALIAS);
                 else if (key.equals(TestConstants.KEY_AMC_KEYSTORE))
                     value = configDef2.getString(
                             TestConstants.KEY_AMC_KEYSTORE);
                 else if (key.equals(TestConstants.KEY_AMC_KEYPASS))
                     value = configDef2.getString(
                             TestConstants.KEY_AMC_KEYPASS);
                 else if (key.equals(TestConstants.KEY_AMC_STOREPASS))
                     value = configDef2.getString(
                             TestConstants.KEY_AMC_STOREPASS);
                 else if (key.equals(TestConstants.KEY_AMC_XMLSIG_CERTALIAS))
                     value = configDef2.getString(
                             TestConstants.KEY_AMC_XMLSIG_CERTALIAS);
                 else if (key.equals(TestConstants.KEY_AMC_IDM_CACHE_ENABLED))
                     value = configDef2.getString(
                             TestConstants.KEY_AMC_IDM_CACHE_ENABLED);
                 else if (key.equals(TestConstants.KEY_AMC_AUTHNSVC_URL))
                     value = strProtocol + "://" + strHost + ":" + strPort +
                             strURI + "/" + "Liberty/authnsvc";
                 else if (key.equals(TestConstants.KEY_AMC_NOTIFICATION_URL))
                     value = "http://" + hostname + ":" + configDef2.getString(
                             TestConstants.KEY_ATT_NOTIFICATION_URI);
             }
             value = value.replace("@BASE_DIR@", testDir + fileseparator +
                     serverName1 + "_" + serverName2);
             properties_ss.put(key, value);
         }
 
         for (Enumeration e = configDef2.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             String value = (String)configDef2.getString(key);
             if (!key.equals(TestConstants.KEY_ATT_NAMING_SVC) &&
                     !key.equals(TestConstants.KEY_ATT_DEFAULTORG) &&
                     !key.equals(TestConstants.KEY_ATT_METAALIAS)  &&
                     !key.equals(TestConstants.KEY_ATT_ENTITY_NAME) &&
                     !key.equals(TestConstants.KEY_ATT_COT) &&
                     !key.equals(TestConstants.KEY_ATT_CERTALIAS) &&
                     !key.equals(TestConstants.KEY_ATT_PROTOCOL) &&
                     !key.equals(TestConstants.KEY_ATT_HOST) &&
                     !key.equals(TestConstants.KEY_ATT_PORT) &&
                     !key.equals(TestConstants.KEY_ATT_DEPLOYMENT_URI)) {
                 properties_ss.put(key, value);
             }
         }
 
         properties_ss.put(TestConstants.KEY_ATT_SERVER_NAME, serverName1 + "_" +
                 serverName2);
 
         for (Enumeration e = configDef2.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             String value = (String)configDef2.getString(key);
 
             if (value.equals("@COPY_FROM_CONFIG@")) {
                 if (key.equals(TestConstants.KEY_ATT_PROTOCOL))
                     value = strProtocol;
                 else if (key.equals(TestConstants.KEY_ATT_HOST))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_ATT_PORT))
                     value = strPort;
                 else if (key.equals(TestConstants.KEY_ATT_DEPLOYMENT_URI))
                     value = strURI;
                 else if (key.equals(TestConstants.KEY_ATT_METAALIAS))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_ATT_ENTITY_NAME))
                     value = strHost;
                 else if (key.equals(TestConstants.KEY_ATT_COT))
                     value = "spcot";
             }
             if (key.equals(TestConstants.KEY_ATT_METAALIAS)) {
                 if (!configDef2.getString(TestConstants.
                         KEY_ATT_EXECUTION_REALM).endsWith("/")) {
                     value = configDef2.getString(
                             TestConstants.KEY_ATT_EXECUTION_REALM) + "/" + 
                             value;
                 } else {
                     value = configDef2.getString(
                             TestConstants.KEY_ATT_EXECUTION_REALM) +  value;
                 }
             }
             if (!key.equals(TestConstants.KEY_ATT_NAMING_SVC) &&
                     !key.equals(TestConstants.KEY_ATT_DEFAULTORG) &&
                     !key.equals(TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT) &&
                     !key.equals(TestConstants.KEY_ATT_LOG_LEVEL))
             properties_protocol.put("sp_" + key, value);
         }
     }
 
     /**
      * Reads data from a Map object, creates a new file and writes data to that
      * file
      */
     private void createFileFromMap(Map properties, String fileName)
         throws Exception
     {
         StringBuffer buff = new StringBuffer();
         for (Iterator i = properties.entrySet().iterator(); i.hasNext(); ) {
             Map.Entry entry = (Map.Entry)i.next();
             buff.append(entry.getKey())
                 .append("=")
                 .append(entry.getValue())
                 .append("\n");
         }
 
         BufferedWriter out = new BufferedWriter(new FileWriter(
             fileName));
         out.write(buff.toString());
         out.close();
     }
 
     public static void main(String args[]) {
         try {
             ClientConfigCreator creator = new ClientConfigCreator(args[0],
                     args[1], args[2], args[3]);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
