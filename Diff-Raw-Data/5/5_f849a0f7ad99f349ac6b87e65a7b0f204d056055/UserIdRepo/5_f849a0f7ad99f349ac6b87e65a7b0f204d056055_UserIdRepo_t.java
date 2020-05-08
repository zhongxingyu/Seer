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
 * $Id: UserIdRepo.java,v 1.6 2008-06-13 18:17:47 kenwho Exp $
  *
  * Copyright 2008 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.iplanet.am.util.SSLSocketFactoryManager;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.common.LDAPUtils;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.sm.AttributeSchema;
 import com.sun.identity.sm.OrganizationConfigManager;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import javax.servlet.ServletContext;
 import netscape.ldap.LDAPAttribute;
 import netscape.ldap.LDAPAttributeSet;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPEntry;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPSearchResults;
 
 /**
  * This class does Directory Server related tasks for 
  * Access Manager deployed as single web-application. 
  */
 class UserIdRepo {
     private static final String umSunDSForAM;
     private static final String umSunDSGeneric;
     private static UserIdRepo instance = new UserIdRepo();
 
     static {
         ResourceBundle rb = ResourceBundle.getBundle(
             SetupConstants.PROPERTY_FILENAME);
         umSunDSForAM = rb.getString("umSunDSForAM");
         umSunDSGeneric = rb.getString("umSunDSGeneric");
     }
    
     private UserIdRepo() {
     }
     
     public static UserIdRepo getInstance() {
         return instance;
     }
     
     void configure(
         Map userRepo, 
         String basedir,
         ServletContext servletCtx,
         SSOToken adminToken
     ) throws Exception {
         String type = (String) userRepo.get(SetupConstants.USER_STORE_TYPE);
         if (type == null) {
             type = SetupConstants.UM_LDAPv3ForAMDS;
         }
 
         boolean bFAMUserSchema = type.equals(SetupConstants.UM_LDAPv3ForAMDS);
         if (bFAMUserSchema) {
             loadSchema(userRepo, basedir, servletCtx);
         }
 
         addSubConfig(userRepo, type, adminToken);
     }
 
     private void addSubConfig(
         Map userRepo, 
         String type, 
         SSOToken adminToken
     ) throws SMSException, SSOException, IOException {
         String xml = null;
         if (type.equals(SetupConstants.UM_LDAPv3ForAMDS)) {
             xml = getResourceContent(umSunDSForAM);
         } else if (type.equals(SetupConstants.UM_LDAPv3)) {
             xml = getResourceContent(umSunDSGeneric);
         }
 
         if (xml != null) {
             Map data = ServicesDefaultValues.getDefaultValues();
             xml = xml.replaceAll("@SM_CONFIG_ROOT_SUFFIX@",
                 (String)data.get(SetupConstants.SM_CONFIG_ROOT_SUFFIX));
             xml = xml.replaceAll("@UM_CONFIG_ROOT_SUFFIX@",
                 (String) userRepo.get(
                 SetupConstants.USER_STORE_ROOT_SUFFIX));
             xml = xml.replaceAll("@UM_DIRECTORY_SERVER@", getHost(userRepo));
             xml = xml.replaceAll("@UM_DIRECTORY_PORT@", getPort(userRepo));
             xml = xml.replaceAll("@UM_DS_DIRMGRDN@", getBindDN(userRepo));
             xml = xml.replaceAll("@UM_DS_DIRMGRPASSWD@",
                 getBindPassword(userRepo));
 
             String s = (String) userRepo.get(SetupConstants.USER_STORE_SSL);
             String ssl = ((s != null) && s.equals("SSL")) ? "true" : "false";
             xml = xml.replaceAll("@UM_SSL@", ssl);
             registerService(xml, adminToken);
         }
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
     
     static ServiceConfig getOrgConfig(SSOToken adminToken) 
         throws SMSException, SSOException {
         ServiceConfigManager svcCfgMgr = new ServiceConfigManager(
             IdConstants.REPO_SERVICE, adminToken);
         ServiceConfig cfg = svcCfgMgr.getOrganizationConfig("", null);
         Map values = new HashMap();
         if (cfg == null) {
             OrganizationConfigManager orgCfgMgr =
                 new OrganizationConfigManager(adminToken, "/");
             ServiceSchemaManager schemaMgr = new ServiceSchemaManager(
                 IdConstants.REPO_SERVICE, adminToken);
             ServiceSchema orgSchema = schemaMgr.getOrganizationSchema();
             Set attrs = orgSchema.getAttributeSchemas();
 
             for (Iterator iter = attrs.iterator(); iter.hasNext();) {
                 AttributeSchema as = (AttributeSchema) iter.next();
                 values.put(as.getName(), as.getDefaultValues());
             }
             cfg = orgCfgMgr.addServiceConfig(IdConstants.REPO_SERVICE,
                 values);
         }
         return cfg;
     }
     
     private String getHost(Map userRepo) {
         return (String)userRepo.get(SetupConstants.USER_STORE_HOST);
     }
     
     private String getPort(Map userRepo) {
         return (String)userRepo.get(SetupConstants.USER_STORE_PORT);
     }
     
     private String getBindDN(Map userRepo) {
         return (String) userRepo.get(SetupConstants.USER_STORE_LOGIN_ID);
     }
     
     private String getBindPassword(Map userRepo) {
         return (String) userRepo.get(SetupConstants.USER_STORE_LOGIN_PWD);
     }
     
     private void loadSchema(
         Map userRepo, 
         String basedir,
         ServletContext servletCtx
     ) throws Exception {
         LDAPConnection ld = null;
         try {
             ld = getLDAPConnection(userRepo);
             String dbName = getDBName(userRepo, ld);
             List schemas = writeSchemaFiles(basedir, dbName, servletCtx);
             for (Iterator i = schemas.iterator(); i.hasNext(); ) {
                 String file = (String)i.next();
                 LDAPUtils.createSchemaFromLDIF(file, ld);
             }
         } finally {
             disconnectDServer(ld);
         }
     }
     
     private List writeSchemaFiles(
         String basedir, 
         String dbName,
         ServletContext servletCtx
     ) throws IOException {
         List files = new ArrayList();
         ResourceBundle rb = ResourceBundle.getBundle(
             SetupConstants.SCHEMA_PROPERTY_FILENAME);
         String strFiles = rb.getString(SetupConstants.SUNDS_LDIF);
 
         StringTokenizer st = new StringTokenizer(strFiles);
         while (st.hasMoreTokens()) {
             String file = st.nextToken();
             InputStreamReader fin = new InputStreamReader(
                 servletCtx.getResourceAsStream(file));
             StringBuffer sbuf = new StringBuffer();
             char[] cbuf = new char[1024];
             int len;
             while ((len = fin.read(cbuf)) > 0) {
                 sbuf.append(cbuf, 0, len);
             }
             FileWriter fout = null;
             try {
                 int idx = file.lastIndexOf("/");
                 String absFile = (idx != -1) ? file.substring(idx+1) : file;
                 String outfile = basedir + "/" + absFile;
                 fout = new FileWriter(outfile);
                 String inpStr = sbuf.toString();
                 inpStr = inpStr.replaceAll("@DB_NAME@", dbName);
                 fout.write(ServicesDefaultValues.tagSwap(inpStr));
                 files.add(outfile);
             } finally {
                 if (fin != null) {
                     try {
                         fin.close();
                     } catch (Exception ex) {
                         //No handling requried
                     }
                 }
                 if (fout != null) {
                     try {
                         fout.close();
                     } catch (Exception ex) {
                         //No handling requried
                     }
                 }
             }
         }
         return files;
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
     
     private void disconnectDServer(LDAPConnection ld)
         throws LDAPException {
         if ((ld != null) && ld.isConnected()) {
             ld.disconnect();
         }
     }
     
     private LDAPConnection getLDAPConnection(Map userRepo)
         throws Exception {
         String s = (String) userRepo.get(SetupConstants.USER_STORE_SSL);
         boolean ssl = ((s != null) && s.equals("SSL"));
         LDAPConnection ld = (ssl) ? new LDAPConnection(
             SSLSocketFactoryManager.getSSLSocketFactory()) :
             new LDAPConnection();
         ld.setConnectTimeout(300);
 
         int port = Integer.parseInt(getPort(userRepo));
         ld.connect(3, getHost(userRepo), port,
             getBindDN(userRepo), getBindPassword(userRepo));
         return ld;
     }
 
     private String getDBName(Map userRepo, LDAPConnection ld)
         throws LDAPException {
         String dbName = null;
         String suffix = (String) userRepo.get(
             SetupConstants.USER_STORE_ROOT_SUFFIX);
        String filter = "cn=" + suffix; 
 
         LDAPSearchResults results = ld.search("cn=mapping tree,cn=config",
             LDAPConnection.SCOPE_SUB, filter, null, false);
         while (results.hasMoreElements()) {
             LDAPEntry entry = results.next();
             String dn = entry.getDN();
             LDAPAttributeSet set = entry.getAttributeSet();
             Enumeration e = set.getAttributes();
             while (e.hasMoreElements() && (dbName == null)) {
                 LDAPAttribute attr = (LDAPAttribute) e.nextElement();
                 String name = attr.getName();
                 if (name.equals("nsslapd-backend")) {
                     String[] value = attr.getStringValueArray();
                     if (value.length > 0) {
                         dbName = value[0];
                     }
                 }
             }
         }
         return (dbName != null) ? dbName : "userRoot";
     }
 }
