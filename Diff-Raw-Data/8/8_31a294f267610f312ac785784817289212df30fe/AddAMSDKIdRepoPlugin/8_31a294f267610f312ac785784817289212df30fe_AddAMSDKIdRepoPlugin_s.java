 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2006 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: AddAMSDKIdRepoPlugin.java,v 1.3 2008-09-10 19:57:46 arviranga Exp $
  *
  */
 
 package com.sun.identity.cli.datastore;
 
 
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.util.Crypt;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.cli.AuthenticatedCommand;
 import com.sun.identity.cli.CLIException;
 import com.sun.identity.cli.CLIUtil;
 import com.sun.identity.cli.IOutput;
 import com.sun.identity.cli.LogWriter;
 import com.sun.identity.cli.RequestContext;
 import com.sun.identity.common.DNUtils;
 import com.sun.identity.common.configuration.ServerConfigXML;
 import com.sun.identity.common.configuration.ServerConfigXML.DirUserObject;
 import com.sun.identity.common.configuration.ServerConfigXML.ServerGroup;
 import com.sun.identity.common.configuration.ServerConfiguration;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.policy.PolicyManager;
 import com.sun.identity.policy.PolicyUtils;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 /**
  * This command creates identity.
  */
 public class AddAMSDKIdRepoPlugin extends AuthenticatedCommand {
     /**
      * Services a Commandline Request.
      *
      * @param rc Request Context.
      * @throw CLIException if the request cannot serviced.
      */
     // @SuppressWarnings("empty-statement")
     public void handleRequest(RequestContext rc) 
         throws CLIException {
         super.handleRequest(rc);
         ldapLogin();
         SSOToken adminSSOToken = getAdminSSOToken();        
         IOutput outputWriter = getOutputWriter();
         List directoryServers = rc.getOption("directory-servers");
         String basedn = getStringOptionValue("basedn").trim();
         boolean isWebEnabled = getCommandManager().webEnabled();
         String dUserPwd = (isWebEnabled) ? getStringOptionValue(
             "dsame-password-file") :
             CLIUtil.getFileContent(getStringOptionValue(
                  "dsame-password-file"), true);
         String pUserPwd = (isWebEnabled) ? getStringOptionValue(
             "puser-password-file") :
             CLIUtil.getFileContent(getStringOptionValue(
             "puser-password-file"), true);
         String namingAttr = "uid";
         String orgAttr = "o";
         String attr = getStringOptionValue("user");
         if (attr != null && attr.trim().length() > 0) {
             namingAttr = attr.trim();
         }
         attr = getStringOptionValue("org");
         if (attr != null && attr.trim().length() > 0) {
             orgAttr = attr.trim();
         }
         writeLog(LogWriter.LOG_ACCESS, Level.INFO, "ATTEMPT_ADD_AMSDK_PLUGIN",
             params);
         try {
             // If CLI, call ssoadm.jsp
             if (!isWebEnabled) {
                 URL url = new URL(getServerURL());
                 HttpURLConnection connection = (HttpURLConnection) url.
                     openConnection();
                 connection.setDoOutput(true);
                 connection.setUseCaches(true);
                 connection.setRequestMethod("POST");
                 connection.setRequestProperty(
                     "Content-type", "application/x-www-form-urlencoded");
                 // Set authenticated user's cookie
                 connection.setRequestProperty("Cookie", SystemProperties.get(
                     Constants.AM_COOKIE_NAME, "iPlanetDirectoryPro") + "=" +
                     adminSSOToken.getTokenID().toString());
 
                 PrintWriter out = new PrintWriter(connection.getOutputStream());
                 out.print("directory-servers=" + URLEncoder.encode(
                     listToString(directoryServers), "UTF-8") +
                     "&basedn=" + URLEncoder.encode(basedn, "UTF-8") +
                     "&dsame-password-file=" + URLEncoder.encode(dUserPwd,
                     "UTF-8") + "&puser-password-file=" + URLEncoder.encode(
                     pUserPwd, "UTF-8") + "&user=" + URLEncoder.encode(
                     namingAttr, "UTF-8") + "&org=" + URLEncoder.encode(
                     orgAttr, "UTF-8"));
                 out.close();
 
                 BufferedReader in = new BufferedReader(
                     new InputStreamReader(connection.getInputStream()));
                 String inputLine;
                 while ((inputLine = in.readLine()) != null) {
                     if (inputLine.startsWith(params[0])) {
                         outputWriter.printlnMessage(inputLine);
                     }
                 }
                 in.close();
             } else {
                 // Load DAI service, if not already loaded
                 String xmlData = null;
                 ServiceManager sm = new ServiceManager(adminSSOToken);
                 if (!sm.getServiceNames().contains("DAI")) {
                     xmlData = getResourceContent("ums.xml");
                     // Tag swap: @USER_NAMING_ATTR & @ORG_NAMING_ATTR
                     xmlData = xmlData.replaceAll("@USER_NAMING_ATTR@",
                         namingAttr);
                     xmlData = xmlData.replaceAll("@ORG_NAMING_ATTR@", orgAttr);
                     registerService(xmlData, adminSSOToken);
                 }
 
                 // Add AMSDK sub-schema to IdRepo service, if not loaded
                 ServiceSchemaManager ssm = new ServiceSchemaManager(
                     adminSSOToken, IdConstants.REPO_SERVICE, "1.0");
                 ServiceSchema ss = ssm.getOrganizationSchema();
                 if (!ss.getSubSchemaNames().contains("amSDK")) {
                     xmlData = getResourceContent("idRepoAmSDK.xml");
                     // Tag swap: @NORMALIZED_ORGBASED
                     xmlData = xmlData.replaceAll("@NORMALIZED_ORGBASE@",
                         DNUtils.normalizeDN(basedn));
                     InputStream xmlInputStream =
                         (InputStream) new ByteArrayInputStream(
                         xmlData.getBytes());
                     ss.addSubSchema(xmlInputStream);
                 }
 
                 // Load delegation policies for Top-level Admin Role and others
                 try {
                     xmlData = getResourceContent(
                         "defaultDelegationPoliciesForAmSDK.xml");
                     // Tag swap: @SM_CONFIG_ROOT_SUFFIX@ & @SM_ROOT_SUFFIX_HAT@
                     String smsRootSuffix = ServiceManager.getBaseDN();
                     xmlData = xmlData.replaceAll("@SM_CONFIG_ROOT_SUFFIX@",
                         smsRootSuffix);
                     String smsRootHat = smsRootSuffix.replaceAll(",", "^");
                     xmlData = xmlData.replaceAll("@SM_ROOT_SUFFIX_HAT@",
                         smsRootHat);
                     // Tag swap: @ROOT_SUFFIX@
                     xmlData = xmlData.replaceAll("@ROOT_SUFFIX@",
                         DNUtils.normalizeDN(basedn));
                     InputStream xmlInputStream =
                         (InputStream) new ByteArrayInputStream(
                         xmlData.getBytes());
                     PolicyManager pm = new PolicyManager(adminSSOToken,
                         "/sunamhiddenrealmdelegationservicepermissions");
                     PolicyUtils.createPolicies(pm, xmlInputStream);
                 } catch (Exception e) {
                     outputWriter.printlnMessage(params[0] + ": " +
                         getResourceString(
                         "datastore-add-amsdk-idrepo-plugin-policies-failed") +
                         ": " + e.getMessage());
                 }
 
                 // Update server-config.xml with AMSDK information
                 Set servers = ServerConfiguration.getServers(adminSSOToken);
                 Map newValues = new HashMap();
                 newValues.put("com.sun.am.event.connection.disable.list", "");
                 for (Iterator items = servers.iterator(); items.hasNext();) {
                     String instance = (String) items.next();
                     String serverconfig = ServerConfiguration
                         .getServerConfigXML(adminSSOToken, instance);
                     ServerConfigXML cxml = new ServerConfigXML(serverconfig);
                     ServerGroup defaultGroup = cxml.getDefaultServerGroup();
                     // Add directory servers
                     if ((directoryServers != null) &&
                         !directoryServers.isEmpty()) {
                         defaultGroup.hosts.clear();
                         int i = 1;
                         for (Iterator dshosts = directoryServers.iterator();
                             dshosts.hasNext(); i++) {
                             String dshost = (String) dshosts.next();
                             // Parse the dshost
                             String name = "SERVER" + i;
                             String type = "SIMPLE";
                             String dshostlc = dshost.toLowerCase();
                             if (dshostlc.startsWith("ldaps://")) {
                                 type = "SSL";
                                 dshost = dshost.substring(8);
                             } else if (dshostlc.startsWith("ldap://")) {
                                 dshost = dshost.substring(7);
                             }
                             int portIndex = dshost.indexOf(':');
                             String host = dshost;
                             String port = "389";
                             if (portIndex != -1) {
                                 host = dshost.substring(0, portIndex);
                                 port = dshost.substring(portIndex + 1);
                             }
                             defaultGroup.addHost(name, host, port, type);
                         }
                     }
                     // Set the base dn
                     defaultGroup.dsBaseDN = basedn;
                     // Set admin & proxy user's password
                     for (Iterator users = defaultGroup.dsUsers.iterator();
                         users.hasNext();) {
                         DirUserObject user = (DirUserObject) users.next();
                         if (user.type.equals("proxy")) {
                             user.dn = "cn=puser,ou=DSAME Users," + basedn;
                             user.password = Crypt.encode(pUserPwd);
                         } else if (user.type.equals("admin")) {
                             user.dn = "cn=dsameuser,ou=DSAME Users," + basedn;
                             user.password = Crypt.encode(dUserPwd);
                         }
                     }
                     // Saver serverconfig.xml
                     ServerConfiguration.setServerConfigXML(adminSSOToken,
                         instance, cxml.toXML());
 
                     // Enable psearch for um, aci and sm
                     ServerConfiguration.setServerInstance(adminSSOToken,
                         instance, newValues);
                 }
                 outputWriter.printlnMessage(params[0] + ": " +
                     getResourceString(
                     "datastore-add-amsdk-idrepo-plugin-succeeded"));
                 writeLog(LogWriter.LOG_ACCESS, Level.INFO,
                     "SUCCEED_ADD_AMSDK_PLUGIN", params);
             }
         } catch (Exception e) {
             String[] p = {"Adding AMSDK plugin", e.getMessage()};
             writeLog(LogWriter.LOG_ERROR, Level.INFO, 
                 "FAILED_ADD_AMSDK_PLUGIN", p);
             outputWriter.printlnMessage(params[0] + ": " + getResourceString(
                 "datastore-add-amsdk-idrepo-plugin-failed") + ": " +
                 e.getMessage());
         }
     }
     
     private String getResourceContent(String resName)
         throws IOException {
         BufferedReader rawReader = null;
         String content = null;
         try {
             rawReader = new BufferedReader(new InputStreamReader(
                 getClass().getClassLoader().getResourceAsStream(resName)));
             StringBuffer buff = new StringBuffer();
             String line = null;
             while ((line = rawReader.readLine()) != null) {
                 buff.append(line);
             }
             rawReader.close();
             rawReader = null;
             content = buff.toString();
         } finally {
             if (rawReader != null) {
                 rawReader.close();
             }
         }
         return content;
     }
 
     private void registerService(String xml, SSOToken adminSSOToken)
         throws SSOException, SMSException, IOException {
         ServiceManager serviceManager = new ServiceManager(adminSSOToken);
         InputStream serviceStream = null;
         try {
             serviceStream = (InputStream) new ByteArrayInputStream(
                 xml.getBytes());
             serviceManager.registerServices(serviceStream);
         } finally {
             if (serviceStream != null) {
                 serviceStream.close();
             }
         }
     }
     
     private String getServerURL() {
         String serverURL = null;
         String namingURL = SystemProperties.get(Constants.AM_NAMING_URL);
         if (namingURL != null) {
             int index = namingURL.toLowerCase().indexOf(
                 "/namingservice");
             if (index != -1) {
                serverURL = namingURL.substring(0, index).toLowerCase();
             } else {
                 serverURL = "";
             }
         } else {
             serverURL = SystemProperties.getServerInstanceName();
             if (serverURL == null) {
                 serverURL = "";
            } else {
                serverURL = serverURL.toLowerCase();
             }
         }
         return (serverURL + "/ssoadm.jsp?cmd=add-amsdk-idrepo-plugin&submit=");
     }
     
     private String listToString(List directoryServers) {
         String servers = null;
         for (Iterator items = directoryServers.iterator(); items.hasNext();) {
             if (servers == null) {
                 servers = items.next().toString();
             } else {
                 servers += "," + items.next().toString();
             }
         }
         return (servers);
     }
     
     private static final String TOOLS_HOME = "com.sun.identity.tools.home";
     private static final String[] params = { "add-amsdk-idrepo-plugin" };
 }
