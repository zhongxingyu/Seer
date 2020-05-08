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
 * $Id: Bootstrap.java,v 1.2 2007-10-24 20:51:04 veiming Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.iplanet.am.util.AdminUtils;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.ldap.DSConfigMgr;
 import com.iplanet.services.ldap.LDAPServiceException;
 import com.iplanet.services.util.Crypt;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.authentication.internal.AuthContext;
 import com.sun.identity.authentication.internal.AuthPrincipal;
 import com.sun.identity.authentication.internal.InvalidAuthContextException;
 import com.sun.identity.authentication.internal.server.SMSAuthModule;
 import com.sun.identity.common.configuration.ConfigurationObserver;
 import com.sun.identity.common.configuration.ServerConfiguration;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.ServiceConfigManager;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.security.AccessController;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 import javax.security.auth.login.LoginException;
 
 /**
  * This class is responsible for bootstrapping the WAR.
  */
 public class Bootstrap {
     /**
      * Directory where bootstrap file resides.
      */
     public static final String JVM_OPT_BOOTSTRAP = "bootstrap.dir";
     
     static final String PROTOCOL = "protocol";
     static final String SERVER_INSTANCE = "serverinstance";
     static final String FF_BASE_DIR = "ffbasedir";
     static final String BASE_DIR = "basedir";
     static final String DS_HOST = "dshost";
     static final String DS_PORT = "dsport";
     static final String DS_PWD = "dspwd";
     static final String DS_MGR = "dsmgr";
     static final String DS_BASE_DN = "dsbasedn";
     static final String PWD = "pwd";
     static final String EMBEDDED_DS = "embeddedds";
     static final String PROT_FILE = "file";
     static final String PROT_LDAP = "ldap";
     static final String PROTOCOL_FILE = "file://";
     static final String PROTOCOL_LDAP = "ldap://";
     
     private static final String LOCATION = "location";
     private static final String PROP_DEBUG_LEVEL = 
         "com.iplanet.services.debug.level";
     private static final String PROP_SMS_FILE_DIR =
         "com.sun.identity.sm.flatfile.root_dir";
     private static final String PROP_SMS_OBJECT =
         "com.sun.identity.sm.sms_object_class_name";
     private static final String PROP_SMS_OBJECT_FILE =
         "com.sun.identity.sm.flatfile.SMSEnhancedFlatFileObject";
     private static final String PROP_SMS_OBJECT_LDAP =
         "com.sun.identity.sm.ldap.SMSLdapObject";
     private static final String PROP_SECURITY_ENCRYPTOR =
         "com.iplanet.security.encryptor";
     private static final String DEFAULT_SECURITY_ENCRYPTOR =
         "com.iplanet.services.util.JCEEncryption";
     private final static String BOOTSTRAP = "bootstrap";
 
     private static boolean isBootstrap;
 
     private Bootstrap() {
     }
     
     /**
      * Loads System Property with the bootstrap file that is
      * found in <code>JVM_OPT_BOOTSTRAP</code> stated directory.
      *
      * @throws Exception if properties cannot be loaded.
      */
     public synchronized static void load()
         throws Exception
     {   
         if (!isBootstrap) {
             String basedir = System.getProperty(JVM_OPT_BOOTSTRAP);
             load(basedir);
             SystemProperties.initializeProperties("com.iplanet.am.naming.url",
                 SystemProperties.getServerInstanceName() + "/namingservice");
         }
     }
 
     /**
      * Loads System Property with the bootstrap file that is
      * found in a directory.
      *
      * @param basedir Directory where bootstrap file resides.
      *        bootstrap file can contain either an URL where 
      *        we can go fetch the server configuration properties
      *        or a file that contains the properties.
      * @throws Exception if properties cannot be loaded.
      */
     public static Properties load(String basedir)
         throws Exception {
         if (!basedir.endsWith(File.separator)) {
             basedir = basedir + File.separator;
         }
 
         Properties prop = null;
         String amConfigProperties = basedir +
             SetupConstants.AMCONFIG_PROPERTIES;
         File file = new File(amConfigProperties);
         if (file.exists()) {
             prop = new Properties();
             InputStream propIn = new FileInputStream(amConfigProperties);
             try {
                 prop.load(propIn);
             } finally {
                 propIn.close();
             }
         } else {
             isBootstrap = true;
             String bootstrapFile = basedir + BOOTSTRAP;
             String urlBootstrap = readFile(bootstrapFile);
             prop = getConfiguration(urlBootstrap, true);
         }
         
         return prop;
     }
 
     static boolean load(String bootstrap, boolean reinit)
         throws Exception {
         boolean configured = false;
         
         if (bootstrap != null) {
             if (!bootstrap.startsWith(PROTOCOL_FILE) &&
                 !bootstrap.startsWith(PROTOCOL_LDAP)
             ) {
                 String amConfigProperties =  bootstrap + "/" +
                     SetupConstants.AMCONFIG_PROPERTIES;
                 configured = loadAMConfigProperties(amConfigProperties);
             } else {
                 configured = bootstrap(bootstrap, reinit);
             }
             isBootstrap = true;
         }
         return configured;
     }
     
     /**
      * Returns <code>true</code> if able to bootstrap the system.
      *
      * @param bootstrapInfo an URL that contains information on how to
      *        fetch the server configuration properties.
      * @param reinit <code>true</code> to re initialize the system.
      * @throws Exception if there are errors in bootstrapping.
      */
     public static boolean bootstrap(String bootstrapInfo, boolean reinit) 
         throws Exception {
         return (getConfiguration(bootstrapInfo, reinit) != null);
     }
 
     /**
      * Returns System Property with an URL.
      *
      * @param bootstrapInfo an URL that contains information on how to
      *        fetch the server configuration properties.
      * @param reinit <code>true</code> to re initialize the system.
      * @throws Exception if properties cannot be loaded.
      */
     private static Properties getConfiguration(
         String bootstrapInfo,
         boolean reinit
     ) throws Exception {
         Properties properties = null;
         Properties prop = new Properties();
         prop.setProperty(PROP_SECURITY_ENCRYPTOR, DEFAULT_SECURITY_ENCRYPTOR);
         prop.setProperty(Constants.SERVER_MODE, "true");
 
         boolean isLdap = false;
         // need to do this because URL class does not understand ldap://
         if (bootstrapInfo.startsWith("ldap://")) {
             bootstrapInfo = "http://" +  bootstrapInfo.substring(7);
             isLdap = true;            
         }
         
         URL url = new URL(bootstrapInfo);
         String instanceName = null;
         String dshost = "ds.opensso.java.net";
         String dsport = "389";
         String dsbasedn = "dc=opensso,dc=java,dc=net";
         String dsmgr = "cn=dsameuser,ou=DSAME Users," + dsbasedn;
         String dspwd = "11111111";
         Map mapQuery = queryStringToMap(url.getQuery());
         String pwd = (String)mapQuery.get(PWD);
         boolean proceed = true;
 
         //NOTE: need to add more protocol is we support more of them
         if (!isLdap) {
             prop.setProperty(PROP_SMS_OBJECT, PROP_SMS_OBJECT_FILE);
             prop.setProperty(PROP_SMS_FILE_DIR,
                 (String)mapQuery.get(FF_BASE_DIR));
             instanceName = url.getPath();
             int idx = instanceName.lastIndexOf('/');
             if (idx != -1) {
                 instanceName = instanceName.substring(idx+1);
             }
             instanceName = URLDecoder.decode(instanceName, "UTF-8");
             initializeSystemComponents(prop, dsbasedn, reinit, isLdap, dshost,
                 dsport, dsmgr, dspwd, pwd);
         } else {
             prop.setProperty(PROP_SMS_OBJECT, PROP_SMS_OBJECT_LDAP);
             dshost = url.getHost();
             dsport = Integer.toString(url.getPort());
             dsbasedn = (String)mapQuery.get(DS_BASE_DN);
             dsmgr = (String)mapQuery.get(DS_MGR);
             dspwd = (String)mapQuery.get(DS_PWD);
             instanceName = URLDecoder.decode(url.getPath(), "UTF-8");
             if (instanceName.startsWith("/")) {
                 instanceName = instanceName.substring(1);
             }
             
             initializeSystemComponents(prop, dsbasedn, reinit, isLdap, dshost,
                 dsport, dsmgr, dspwd, pwd);
 
             String embeddedDS = (String)mapQuery.get(EMBEDDED_DS);
             if ((embeddedDS != null) && (embeddedDS.length() > 0)) {
                startEmbeddedDS(embeddedDS);
                proceed = true;
             }
         }
 
         if (proceed) {
             String dsameUser = "cn=dsameuser,ou=DSAME Users," + dsbasedn;
             SSOToken ssoToken = getSSOToken(dsbasedn, dsameUser,
                 Crypt.decode(pwd, Crypt.getHardcodedKeyEncryptor()));
             try {
                 properties = ServerConfiguration.getServerInstance(
                     ssoToken, instanceName);
                 if (properties != null) {
                     SystemProperties.initializeProperties(
                         properties, true, false);
                     String serverConfigXML =
                         ServerConfiguration.getServerConfigXML(
                             ssoToken, instanceName);
                     Crypt.reinitialize();
                     loadServerConfigXML(serverConfigXML);
                     AdminUtils.initialize();
                     SMSAuthModule.initialize();
                     SystemProperties.initializeProperties(
                         properties, true, true);
                     SystemProperties.setServerInstanceName(instanceName);
 
                     ServiceConfigManager scm = new ServiceConfigManager(
                         Constants.SVC_NAME_PLATFORM, (SSOToken)AccessController.
                         doPrivileged(AdminTokenAction.getInstance()));
                     scm.addListener(new ConfigurationObserver());
                 }
             } catch (SMSException e) {
                 //ignore. product is not configured yet.
                 properties = null;
             }
         }
         return properties;
     }
     
     private static void initializeSystemComponents(
         Properties prop,
         String dsbasedn, 
         boolean reinit,
         boolean isLdap,
         String dshost,
         String dsport,
         String dsmgr,
         String dspwd,
         String pwd
     ) throws LDAPServiceException {
         String dsameUser = "cn=dsameuser,ou=DSAME Users," + dsbasedn;
         
         prop.setProperty(PROP_DEBUG_LEVEL, "error");
         prop.setProperty("com.sun.identity.authentication.special.users",
             dsameUser);
         prop.setProperty("com.sun.identity.authentication.super.user",
             dsameUser);
         SystemProperties.initializeProperties(prop, true);
         
         String template = (isLdap) ? BOOTSTRAP_SERVER_CONFIG_LDAP :
             BOOTSTRAP_SERVER_CONFIG_FILE;
         template = template.replaceAll("@" + DS_HOST + "@", dshost);
         template = template.replaceAll("@" + DS_PORT + "@", dsport);
         template = template.replaceAll("@" + DS_MGR + "@", dsmgr);
         template = template.replaceAll("@" + DS_BASE_DN + "@", dsbasedn);
         template = template.replaceAll("@" + DS_PWD + "@", dspwd);
         template = template.replaceAll("@" + PWD + "@", pwd);
         Crypt.reinitialize();
         loadServerConfigXML(template);
         
         if (reinit) {
             AdminUtils.initialize();
             SMSAuthModule.initialize();
         }
     }
 
    private static void startEmbeddedDS(String odsDir) {
         File odsDirFile = new File(odsDir);
 
        if (odsDirFile.exists() && !EmbeddedOpenDS.isStarted()) {
            try {
                SetupProgress.reportStart("emb.startemb", null);
                EmbeddedOpenDS.startServer(odsDir);
                //Thread.sleep(5000);
                SetupProgress.reportEnd("emb.success", null);
            } catch (Exception ex) {
                //ignore, it maybe started.
             }
         }
     }
     
     private static boolean loadAMConfigProperties(String fileLocation)
         throws IOException {
         boolean loaded = false;
         File test = new File(fileLocation);
         
         if (test.exists()) {
             FileInputStream fin = null;
             
             try {
                 fin = new FileInputStream(fileLocation);
                 if (fin != null) {
                     Properties props = new Properties();
                     props.load(fin);
                     SystemProperties.initializeProperties(props);
                     loaded =true;
                 }
             } finally {
                 if (fin != null) {
                     try {
                         fin.close();
                     } catch (IOException e) {
                         //ignore
                     }
                 }
             }
         }
         return loaded;
     }
 
     private static void loadServerConfigXML(String xml)
         throws LDAPServiceException {
         ByteArrayInputStream bis = null;
         try {
             bis = new ByteArrayInputStream(xml.getBytes());
             DSConfigMgr.initInstance(bis);
         } finally {
             if (bis != null) {
                 try {
                     bis.close();
                 } catch (IOException e) {
                     //ignore
                 }
             }
         }
     }
     
     static String createBootstrapResource(Map configuration, boolean legacy)
         throws UnsupportedEncodingException
     {
         StringBuffer buff = new StringBuffer();
         
         String protocol = (String)configuration.get(PROTOCOL);
         String pwd = Crypt.encode((String)configuration.get(PWD),
             Crypt.getHardcodedKeyEncryptor());
         String serverInstance = (String)configuration.get(
             SERVER_INSTANCE);
 
         if (legacy) {
             buff.append((String)configuration.get(FF_BASE_DIR));
         } else if (protocol.equals(PROTOCOL_FILE)) {
             buff.append(PROTOCOL_FILE);
             buff.append((String)configuration.get(BASE_DIR));
             buff.append("/");
             buff.append(URLEncoder.encode(serverInstance, "UTF-8"));
             buff.append("?");
             buff.append(PWD);
             buff.append("=");
             buff.append(URLEncoder.encode(pwd, "UTF-8"));
             buff.append("&");
             buff.append(FF_BASE_DIR);
             buff.append("=");
             buff.append(URLEncoder.encode(
                 (String)configuration.get(FF_BASE_DIR), "UTF-8"));
         } else {
             buff.append(PROTOCOL_LDAP);
             buff.append((String)configuration.get(DS_HOST));
             buff.append(":");
             buff.append((String)configuration.get(DS_PORT));
             buff.append("/");
             buff.append(URLEncoder.encode(serverInstance, "UTF-8"));
             buff.append("?");
             buff.append(PWD);
             buff.append("=");
             buff.append(URLEncoder.encode(pwd, "UTF-8"));
             
             String embeddedDS = (String)configuration.get(EMBEDDED_DS);
             
             if ((embeddedDS != null) && (embeddedDS.length() > 0)) {
                 buff.append("&");
                 buff.append(EMBEDDED_DS);
                 buff.append("=");
                 buff.append(URLEncoder.encode(embeddedDS, "UTF-8"));
             }
             buff.append("&");
             buff.append(DS_BASE_DN);
             buff.append("=");
             buff.append(URLEncoder.encode((String)configuration.get(
                 DS_BASE_DN), "UTF-8"));
             buff.append("&");
             buff.append(DS_MGR);
             buff.append("=");
             buff.append(URLEncoder.encode((String)configuration.get(
                 DS_MGR), "UTF-8")); 
             buff.append("&");
             buff.append(DS_PWD);
             buff.append("=");
             buff.append(URLEncoder.encode(
                 Crypt.encode((String)configuration.get(DS_PWD),
                     Crypt.getHardcodedKeyEncryptor()), "UTF-8")); 
         }
         return buff.toString();
     }
 
     static String create(
         String bootstrapFile, 
         Map configuration, 
         boolean legacy
     ) throws IOException {
         File btsFile = new File(bootstrapFile);
         if (!btsFile.getParentFile().exists()) {
             btsFile.getParentFile().mkdirs();
         }
 
         String url = createBootstrapResource(configuration, legacy);
         AMSetupServlet.writeToFile(bootstrapFile, url);
         return url;
     }
     
     private static SSOToken getSSOToken(
         String basedn,
         String bindUser, 
         String bindPwd
     ) throws LoginException, InvalidAuthContextException {
         SSOToken ssoToken = null;
         AuthPrincipal principal = new AuthPrincipal(bindUser);
         AuthContext ac = new AuthContext(
             basedn, principal, bindPwd.toCharArray());
         if (ac.getLoginStatus() == AuthContext.AUTH_SUCCESS) {
             ssoToken = ac.getSSOToken();
         }
         return ssoToken;
     }
 
     private static String readFile(String file) 
         throws IOException
     {
         BufferedReader in = null;
         String str = null;
         
         try {
             in = new BufferedReader(new FileReader(file));
             if (in.ready()) {
                 str = in.readLine();
             }
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (Exception ex) {
                     //No handling requried
                 }
             }
         }
         return str;
     }
 
     private static Map queryStringToMap(String str)
         throws UnsupportedEncodingException {
         Map map = new HashMap();
         StringTokenizer st = new StringTokenizer(str, "&");
 
         while (st.hasMoreTokens()) {
             String s = st.nextToken();
             int idx = s.indexOf('=');
             map.put(s.substring(0, idx), URLDecoder.decode(
                 s.substring(idx +1), "UTF-8"));
         }
         return map;
     }
 
     private static final String BOOTSTRAP_SERVER_CONFIG_COMMON =
         "<iPlanetDataAccessLayer>" +
         "<ServerGroup name=\"default\" minConnPool=\"1\" maxConnPool=\"1\">" +
         "<Server name=\"Server1\" host=\"@" + DS_HOST + "@\" " +
         "port=\"@" + DS_PORT + "@\" type=\"SIMPLE\" />" +
         "<User name=\"User1\" type=\"admin\">" +
         "<DirDN>cn=dsameuser,ou=DSAME Users,@" + DS_BASE_DN + "@</DirDN>" +
         "<DirPassword>@" + PWD + "@</DirPassword>" +
         "</User>" +
         "<BaseDN>@" + DS_BASE_DN + "@</BaseDN>" +
         "</ServerGroup>";
     private static final String BOOTSTRAP_SERVER_CONFIG_FILE =
         BOOTSTRAP_SERVER_CONFIG_COMMON + "</iPlanetDataAccessLayer>";
     private static final String BOOTSTRAP_SERVER_CONFIG_LDAP = 
         BOOTSTRAP_SERVER_CONFIG_COMMON + 
         "<ServerGroup name=\"sms\" minConnPool=\"1\" maxConnPool=\"10\">" +
         "<Server name=\"Server1\" host=\"@" + DS_HOST + "@\" " +
         "port=\"@" + DS_PORT + "@\" type=\"SIMPLE\" />" +
         "<User name=\"User2\" type=\"admin\">" +
         "<DirDN>@" + DS_MGR + "@</DirDN>" +
         "<DirPassword>@" + DS_PWD + "@</DirPassword>" +
         "</User>" +
         "<BaseDN>@" + DS_BASE_DN + "@</BaseDN>" +
         "</ServerGroup>" +
         "</iPlanetDataAccessLayer>";
 }
