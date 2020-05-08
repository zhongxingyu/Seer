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
 * $Id: AMSetupServlet.java,v 1.59 2008-05-15 04:51:02 kevinserwin Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.iplanet.am.util.AdminUtils;
 import com.iplanet.services.ldap.DSConfigMgr;
 import com.iplanet.services.naming.WebtopNaming;
 import com.iplanet.services.util.Crypt;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.ldap.LDAPServiceException;
 import com.sun.enterprise.registration.StartRegister;
 import com.sun.identity.authentication.UI.LoginLogoutMapping;
 import com.sun.identity.authentication.config.AMAuthenticationManager;
 import com.sun.identity.authentication.internal.server.SMSAuthModule;
 import com.sun.identity.common.DebugPropertiesObserver;
 import com.sun.identity.common.configuration.ConfigurationObserver;
 import com.sun.identity.common.configuration.ConfigurationException;
 import com.sun.identity.common.configuration.ServerConfiguration;
 import com.sun.identity.common.configuration.SiteConfiguration;
 import com.sun.identity.common.configuration.UnknownPropertyNameException;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.policy.PolicyException;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.encode.Base64;
 import com.sun.identity.sm.AttributeSchema;
 import com.sun.identity.sm.OrganizationConfigManager;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SMSEntry;
 import com.sun.identity.sm.CachedSMSEntry;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import java.security.AccessController;
 import java.security.SecureRandom;
 import java.util.Enumeration;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import netscape.ldap.LDAPException;
 
 /**
  * This class is the first class to get loaded by the Servlet container. 
  * It has helper methods to determine the status of Access Manager 
  * configuration when deployed as a single web-application. If 
  * Access Manager is not deployed as single web-application then the 
  * configured status returned is always true.   
  */
 public class AMSetupServlet extends HttpServlet {
     private static ServletContext servletCtx = null;
     private static boolean isConfiguredFlag = false;
     private final static String SMS_STR = "sms";
     private static SSOToken adminToken = null;
     private final static String LEGACY_PROPERTIES = "legacy";
 
     final static String BOOTSTRAP_EXTRA = "bootstrap";    
     final static String BOOTSTRAP_FILE_LOC = "bootstrap.file";
     final static String OPENDS_DIR = "/opends";
 
     private static String errorMessage = null;
 
     /*
      * Initializes the servlet.
      */  
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         System.setProperty("file.separator", "/");
         if (servletCtx == null ) {
             servletCtx = config.getServletContext();
         }
         checkConfigProperties();
         LoginLogoutMapping.setProductInitialized(isConfiguredFlag);
         
         if (isConfiguredFlag && !ServerConfiguration.isLegacy()) { 
             try {
                 BootstrapCreator.createBootstrap();
             } catch (ConfigurationException e) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet,init", e);
             }        
         }
     }
 
     /*
      * Flag indicating if Access Manager is configured
      */  
     static public boolean isConfigured() {
         return isConfiguredFlag;
     } 
 
     /**
      * Checks if the product is already configured. This is required when
      * the container on which WAR is deployed is restarted. If  
      * product is configured the flag is set true. Also the flag is
      * set to true in case of non-single war deployment.
      */
     public static void checkConfigProperties() {
         String overrideAMC = SystemProperties.get(
             SetupConstants.AMC_OVERRIDE_PROPERTY);
         isConfiguredFlag = overrideAMC == null || 
             overrideAMC.equalsIgnoreCase("false");
         
         if ((!isConfiguredFlag) && (servletCtx != null)) {
             String baseDir = getBaseDir();
             try {
                 String bootstrapFile = getBootStrapFile();
                 if (bootstrapFile != null) {
                     isConfiguredFlag = Bootstrap.load(
                         new BootstrapData(baseDir), false);
                 } else {                    
                     if (baseDir != null) {
                         isConfiguredFlag = loadAMConfigProperties(
                             baseDir + "/" + SetupConstants.AMCONFIG_PROPERTIES);
                     }
                 }
             } catch (ConfiguratorException e) {
                 //ignore, WAR may not be configured yet.
             } catch (Exception e) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet.checkConfigProperties", e);
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
     
     /**
      * Invoked from the filter to decide which page needs to be 
      * displayed.
      * @param servletctx is the Servlet Context
      * @return true if AM is already configured, false otherwise 
      */
     public static boolean checkInitState(ServletContext servletctx) {
         return isConfiguredFlag;
     }
 
     /**
      * The main entry point for configuring Access Manager. The parameters
      * are passed from configurator page.
      *
      * @param request Servlet request.
      * @param response Servlet response. 
      * @return <code>true</code> if the configuration succeeded.
      */
     public static boolean processRequest(
         HttpServletRequest request,
         HttpServletResponse response
     ) {
         HttpServletRequestWrapper req = new HttpServletRequestWrapper(request);
         HttpServletResponseWrapper res = new HttpServletResponseWrapper(
             response);
         return processRequest(req, res);
     }
     
     public static boolean processRequest(
         IHttpServletRequest request,
         IHttpServletResponse response
     ) {
         /*
          * This logic needs refactoring later. setServiceConfigValues()
          * attempts to check if directory is up and makes a call
          * back to this class. The implementation'd
          * be cleaner if classes&methods are named better and separated than 
          * intertwined together.
          */
         ServicesDefaultValues.setServiceConfigValues(request);
         Map map = ServicesDefaultValues.getDefaultValues();
 
         // used for site configuration later
         Map siteMap = (Map)map.remove(
             SetupConstants.CONFIG_VAR_SITE_CONFIGURATION);
 
         Map userRepo = (Map)map.remove("UserStore");
         
         try {
             isConfiguredFlag = configure(map, userRepo);
             if (isConfiguredFlag) {
                 //postInitialize was called at the end of configure????
                 postInitialize(getAdminSSOToken());
             }
             LoginLogoutMapping.setProductInitialized(isConfiguredFlag);
             
             if (isConfiguredFlag) {
                 boolean legacy = ServerConfiguration.isLegacy();
                 Map bootstrapRes = createBootstrapResource(legacy);
                 String url = BootstrapData.createBootstrapResource(
                     bootstrapRes, legacy);
                 String basedir = (String) map.get(
                     SetupConstants.CONFIG_VAR_BASE_DIR);   
                 String fileBootstrap = getBootstrapLocator();
                 if (fileBootstrap != null) {
                    writeToFileEx(fileBootstrap, basedir);
                 }
 
                 if (!legacy) {
                     writeBootstrapFile(url);
                     Map mapBootstrap = new HashMap(2);
                     Set set = new HashSet(2);
                     set.add(fileBootstrap);
                     mapBootstrap.put(BOOTSTRAP_FILE_LOC, set);
 
                     if (fileBootstrap == null) {
                         set.add(getPresetConfigDir());
                     } else {
                         set.add(fileBootstrap); 
                     }
                     // this is to store the bootstrap location
                     String serverInstanceName = 
                         SystemProperties.getServerInstanceName(); 
 
                     SSOToken adminToken = (SSOToken)
                         AccessController.doPrivileged(
                         AdminTokenAction.getInstance());
                     ServerConfiguration.setServerInstance(adminToken,
                         serverInstanceName, mapBootstrap);
                     
                     // setup site configuration information
                     if ((siteMap != null) && !siteMap.isEmpty()) {
                         String site = (String)siteMap.get(
                             SetupConstants.LB_SITE_NAME);
                         String primaryURL = (String)siteMap.get(
                             SetupConstants.LB_PRIMARY_URL);
 
                         /* 
                          * If primary url is null that means we are adding
                          * to an existing site. we don't need to create it 
                          * first.
                          */
                         if ((primaryURL != null) && (primaryURL.length() > 0)) {
                             SiteConfiguration.createSite(adminToken,
                                 site, primaryURL, Collections.EMPTY_SET);
                         } 
 
                         if (!ServerConfiguration.belongToSite( 
                             adminToken, serverInstanceName, site)) 
                         {
                             ServerConfiguration.addToSite(
                                 adminToken, serverInstanceName, site);
                         }
                     }
                 }
             }
         } catch (Exception e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processRequest: error.", e);
             e.printStackTrace();
         }
         return isConfiguredFlag;
     }
     
     private static boolean configure(Map map, Map userRepo) {
         boolean configured = false;
         boolean existingConfiguration = false;
         try {
             String basedir = (String)map.get(
                 SetupConstants.CONFIG_VAR_BASE_DIR);
             File baseDirectory = new File(basedir);
             if (!baseDirectory.exists()) {
                 baseDirectory.mkdirs();
             } else {
                 SetupProgress.reportStart("emb.checkingbasedir",basedir);
                 File bootstrapFile = new File(basedir + "/" + BOOTSTRAP_EXTRA);
                 File opendsDir = new File(basedir + OPENDS_DIR);
                 if (bootstrapFile.exists() || opendsDir.exists()) {
                     SetupProgress.reportEnd("emb.basedirfailed", null);
                     throw new ConfiguratorException(
                         "Base directory specified :"+
                         basedir+
                         " cannot be used - has preexisting config data.");
                 }
                 SetupProgress.reportEnd("emb.success", null);
             }
             boolean isDITLoaded = ((String)map.get(
                 SetupConstants.DIT_LOADED)).equals("true");
                 
             String dataStore = (String)map.get(
                 SetupConstants.CONFIG_VAR_DATA_STORE);
             boolean embedded = 
                   dataStore.equals(SetupConstants.SMS_EMBED_DATASTORE);
             boolean isDSServer = false;
             boolean isADServer = false;
             if (embedded) {
                 isDSServer = true;
             } else { // Keep old behavior for now.
                 isDSServer = dataStore.equals(SetupConstants.SMS_DS_DATASTORE);
                 isADServer = dataStore.equals(SetupConstants.SMS_AD_DATASTORE);
             }
 
             if (embedded) {
                 // (i) install, configure and start an embedded instance.
                 // or
                 // (ii) install, configure, and replicate embedded instance
                 try {
                     EmbeddedOpenDS.setup(map, servletCtx);
                     // Now create the AMSetupDSConfig instance.Abort on failure
                     AMSetupDSConfig dsConfig = AMSetupDSConfig.getInstance();
                     // wait for at most 10 seconds for OpenDS to come up
                     int sleepTime = 10;
                     while (!dsConfig.isDServerUp() && (sleepTime-- > 0)) {
                         // sleep one second a time
                         Thread.sleep(1000);
                     }
                     if (!dsConfig.isDServerUp()) {
                         Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                          "AMSetupServlet.processRequest:OpenDS conn failed.");
                         return false;
                     }
 
                     // Determine if DITLoaded flag needs to be set :
                     // multi instance
                     if (EmbeddedOpenDS.isMultiServer(map)) {
                         // Replication 
                         // Temporary fix until OpenDS auto-loads schema
                         //
                         List schemaFiles = getSchemaFiles(dataStore);
                         writeSchemaFiles(basedir, schemaFiles);
                         EmbeddedOpenDS.setupReplication(map);
                         isDITLoaded = true;
                     }
                 } catch (Exception ex) {
                     Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                         "AMSetupServlet.configure: embedded = true", ex);
                     ex.printStackTrace();
                     throw new ConfiguratorException(
                         "Error setting up Embedded Directory Server: " + 
                         ex.getLocalizedMessage());
                 }
             }
             
             if ((isDSServer || isADServer ) && !isDITLoaded) {
                 List schemaFiles = getSchemaFiles(dataStore);
                 writeSchemaFiles(basedir, schemaFiles);
             }
 
             String serverURL = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URL);
             String deployuri = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URI);
             String serverInstanceName = serverURL + deployuri;
 
             // do this here since initializeConfigProperties needs the dir
             setupSecurIDDirs(basedir,deployuri);
             // do this here just because the SecurID dirs are done here
             setupSafeWordDirs(basedir, deployuri);
             Map mapFileNameToConfig = initializeConfigProperties();
             String strAMConfigProperties = (String)
                 mapFileNameToConfig.get(SetupConstants.AMCONFIG_PROPERTIES);
             String strServerConfigXML = (String)mapFileNameToConfig.get(
                 SystemProperties.CONFIG_FILE_NAME);
             Properties propAMConfig = ServerConfiguration.getProperties(
                 strAMConfigProperties);
             
             reInitConfigProperties(serverInstanceName,
                 propAMConfig, strServerConfigXML);
             SSOToken adminSSOToken = getAdminSSOToken();
             boolean bUseExtUMDS = (userRepo != null) && !userRepo.isEmpty();
 
             if (!isDITLoaded) {
                 RegisterServices regService = new RegisterServices();
                 regService.registers(adminSSOToken, bUseExtUMDS);
                 processDataRequests("/WEB-INF/template/sms");
             }
 
             if (ServerConfiguration.isLegacy(adminSSOToken)) {
                 Map mapProp = ServerConfiguration.getDefaultProperties();
                 mapProp.putAll(propAMConfig);
                 appendLegacyProperties(mapProp);
                 Properties tmp = new Properties();
                 tmp.putAll(mapProp);
                 SystemProperties.initializeProperties(tmp, true, false);
 
                 writeToFile(basedir + "/" + SetupConstants.AMCONFIG_PROPERTIES,
                     mapToString(mapProp));
                 writeToFile(basedir + "/serverconfig.xml", strServerConfigXML);
                 String hostname = (String)map.get(
                     SetupConstants.CONFIG_VAR_SERVER_HOST);
                 updatePlatformServerList(serverURL + deployuri, hostname);
             } else {
                 try {
                     if (!isDITLoaded) {
                         ServerConfiguration.createDefaults(adminSSOToken);
                     }
                     if (!isDITLoaded ||
                         !ServerConfiguration.isServerInstanceExist(
                             adminSSOToken, serverInstanceName)
                     ) {
                         ServerConfiguration.createServerInstance(adminSSOToken, 
                             serverInstanceName,
                             ServerConfiguration.getPropertiesSet(
                                 strAMConfigProperties),
                             strServerConfigXML);
                     }
                 } catch (UnknownPropertyNameException ex) {
                     // ignore, property names are valid because they are
                     // gotten from template.
                 }
 
                 ServiceConfigManager scm = new ServiceConfigManager(
                     Constants.SVC_NAME_PLATFORM, (SSOToken)AccessController.
                         doPrivileged(AdminTokenAction.getInstance()));
                 scm.addListener(ConfigurationObserver.getInstance());
             }
 
             // Embedded :get our serverid and configure embedded idRepo
             if (embedded) {
                 try {
                     String serverID = WebtopNaming.getAMServerID();
                     String entry = 
                       map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_HOST)+
                       ":"+
                       map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_PORT)+
                      "|"+ serverID;
                     String orgName = (String) 
                             map.get(SetupConstants.SM_CONFIG_ROOT_SUFFIX);
                     updateEmbeddedIdRepo(orgName, "embedded", entry);
                 } catch (Exception ex) {
                     Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                         "EmbeddedDS : failed to setup serverid", ex);
                     throw new ConfiguratorException(
                         "EmbeddedDS : failed to gsetup serverid;ex="+ex);
                 }
             } 
             SystemProperties.setServerInstanceName(serverInstanceName);
             handlePostPlugins(adminSSOToken);
             postInitialize(adminSSOToken);
 
             if ((userRepo != null) && !userRepo.isEmpty()) {
                 try {
                      // Construct the SMSEntry for the node to check to see if it this is an
                      // existing configuration store, or new store
                      ServiceConfig sc = UserIdRepo.getOrgConfig(adminSSOToken);
                      if (sc != null) {                   
                        CachedSMSEntry cEntry = CachedSMSEntry.getInstance(adminSSOToken,
                             ("ou=" + userRepo.get("userStoreHostName") +","
                             + sc.getDN()), null);
                        SMSEntry entry = cEntry.getClonedSMSEntry();
                        if (entry.isNewEntry()) {                  
                             UserIdRepo.configure(userRepo, basedir, servletCtx,
                                 adminSSOToken);
                        } else {
                            existingConfiguration = true;
                        }
                       
                      }
                 } catch (LDAPException e) {
                     throw new ConfiguratorException(e.getMessage());
                 }
             }
             
 
             /*
              * requiring the keystore.jks file in OpenSSO workspace. The
              * createIdentitiesForWSSecurity is for the JavaEE/NetBeans 
              * integration that we had done.
              */
             createPasswordFiles(basedir, deployuri);
             if ((userRepo == null) || userRepo.isEmpty()) {
                 createDemoUser();
             }
             if (!existingConfiguration) {
                  createIdentitiesForWSSecurity(serverURL, deployuri);
             }
             updateSTSwsdl(basedir, deployuri);
 
             String aceDataDir = basedir + "/" + deployuri + "/auth/ace/data";
             copyAuthSecurIDFiles(aceDataDir);
 
             startRegistrationProcess(basedir, deployuri);
 
             isConfiguredFlag = true;
             configured = true;
         } catch (FileNotFoundException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (ConfigurationException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (SecurityException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (LDAPServiceException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (IOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (SMSException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (PolicyException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (ConfiguratorException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (SSOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", e);
             e.printStackTrace();
             errorMessage = e.getMessage();
         } catch (IdRepoException idrepoe) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.configure: error", idrepoe);
             idrepoe.printStackTrace();
             errorMessage = idrepoe.getMessage();
         }
         return configured;
     }
     
     public static String getErrorMessage() {
         return (errorMessage != null) ? errorMessage : ""; 
     }
 
     private static void appendLegacyProperties(Map prop) {
         ResourceBundle res = ResourceBundle.getBundle(LEGACY_PROPERTIES);
         for (Enumeration i = res.getKeys(); i.hasMoreElements(); ) {
             String key = (String)i.nextElement();
             prop.put(key, (String)res.getString(key));
         }
     }
 
     private static void postInitialize(SSOToken adminSSOToken)
         throws SSOException, SMSException {
         AMAuthenticationManager.reInitializeAuthServices();
         
         AMIdentityRepository.clearCache();
         ServiceManager svcMgr = new ServiceManager(adminSSOToken);
         svcMgr.clearCache();
         LoginLogoutMapping lmp = new LoginLogoutMapping();
         lmp.initializeAuth(servletCtx);
         LoginLogoutMapping.setProductInitialized(true);
     }
     
     private static Map createBootstrapResource(boolean legacy)
         throws IOException {
         Map initMap = new HashMap();
         Map map = ServicesDefaultValues.getDefaultValues();
         String serverURL = (String)map.get(
             SetupConstants.CONFIG_VAR_SERVER_URL);
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
         String deployuri = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_URI);
 
         String dataStore = (String)map.get(
             SetupConstants.CONFIG_VAR_DATA_STORE);
         if (legacy) {
             initMap.put(BootstrapData.FF_BASE_DIR,
                 basedir);
         } else if (dataStore.equals(SetupConstants.SMS_FF_DATASTORE)) {
             initMap.put(BootstrapData.PROTOCOL, BootstrapData.PROTOCOL_FILE);
             initMap.put(BootstrapData.FF_BASE_DIR,
                 basedir + deployuri + "/" + SMS_STR );
             initMap.put(BootstrapData.PWD, 
                 map.get(SetupConstants.CONFIG_VAR_ADMIN_PWD));
             initMap.put(BootstrapData.BASE_DIR, basedir);
         } else {
             String tmp = (String)map.get(
                 SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_SSL);
             boolean ssl = (tmp != null) && tmp.equals("SSL");
             initMap.put(BootstrapData.PROTOCOL, (ssl) ?
                 BootstrapData.PROTOCOL_LDAPS : BootstrapData.PROTOCOL_LDAP);
             initMap.put(BootstrapData.DS_HOST, 
                 map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_HOST));
             initMap.put(BootstrapData.DS_PORT, 
                 map.get(SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_PORT));
             initMap.put(BootstrapData.DS_BASE_DN,
                 map.get(SetupConstants.CONFIG_VAR_ROOT_SUFFIX));
             initMap.put(BootstrapData.DS_MGR,
                 map.get(SetupConstants.CONFIG_VAR_DS_MGR_DN));
             initMap.put(BootstrapData.PWD, 
                 map.get(SetupConstants.CONFIG_VAR_ADMIN_PWD));
             initMap.put(BootstrapData.DS_PWD, 
                 map.get(SetupConstants.CONFIG_VAR_DS_MGR_PWD));
         }
         initMap.put(BootstrapData.SERVER_INSTANCE, serverURL + deployuri);
         return initMap;
     }
     
     private static void writeBootstrapFile(String url)
         throws IOException {
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
         String deployuri = (String)map.get(
             SetupConstants.CONFIG_VAR_SERVER_URI);
         String fileName = basedir + "/" + BOOTSTRAP_EXTRA; 
         writeToFile(fileName, url);
 
         // Do "chmod" only if it is on UNIX/Linux platform
         if (System.getProperty("path.separator").equals(":")) {
            Runtime.getRuntime().exec("/bin/chmod 400" + fileName);
         }
     }
 
     private static void handlePostPlugins(SSOToken adminSSOToken) {
         List plugins = getConfigPluginClasses();
         for (Iterator i = plugins.iterator(); i.hasNext(); ) {
             ConfiguratorPlugin plugin  = (ConfiguratorPlugin)i.next();
             plugin.doPostConfiguration(servletCtx, adminSSOToken);
         }
     }
 
     private static List getConfigPluginClasses() {
         List plugins = new ArrayList();
         try {
             ResourceBundle rb = ResourceBundle.getBundle(
                 SetupConstants.PROPERTY_CONFIGURATOR_PLUGINS);
             String strPlugins = rb.getString(
                 SetupConstants.KEY_CONFIGURATOR_PLUGINS);
 
             if (strPlugins != null) {
                 StringTokenizer st = new StringTokenizer(strPlugins);
                 while (st.hasMoreTokens()) {
                     String className = st.nextToken();
                     Class clazz = Class.forName(className);
                     plugins.add((ConfiguratorPlugin)clazz.newInstance());
                 }
             }
         } catch (IllegalAccessException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getConfigPluginClasses: error", e);
             e.printStackTrace();
         } catch (InstantiationException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getConfigPluginClasses: error", e);
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getConfigPluginClasses: error", e);
             e.printStackTrace();
         } catch (MissingResourceException e) {
             //ignore if there are no configurator plugins.
         }
         return plugins;
     }
 
     private static void reInitConfigProperties(
         String serverName,
         Properties prop,
         String strServerConfigXML
     ) throws FileNotFoundException, SMSException, IOException, SSOException,
         LDAPServiceException {
         SystemProperties.initializeProperties(prop, true, false);
         Crypt.reinitialize();
         initDSConfigMgr(strServerConfigXML);
         AdminUtils.initialize();
         SMSAuthModule.initialize();
         SystemProperties.initializeProperties(prop, true, true);
         DebugPropertiesObserver.getInstance().notifyChanges();
         
         List plugins = getConfigPluginClasses();
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(SetupConstants.CONFIG_VAR_BASE_DIR);
         for (Iterator i = plugins.iterator(); i.hasNext(); ) {
             ConfiguratorPlugin plugin = (ConfiguratorPlugin)i.next();
             plugin.reinitConfiguratioFile(basedir);
         }
     }
 
     public static String getPresetConfigDir() {
         String configDir = null;
         try {
             ResourceBundle rb = ResourceBundle.getBundle(
                 SetupConstants.BOOTSTRAP_PROPERTIES_FILE);
             configDir = rb.getString(SetupConstants.PRESET_CONFIG_DIR);
             
             if ((configDir != null) && (configDir.length() > 0)) {
                 String realPath = getNormalizedRealPath(servletCtx);
                 if (realPath != null) {
                     configDir = configDir.replaceAll(
                         SetupConstants.TAG_REALPATH, realPath);
                 } else {
                     throw new ConfiguratorException(
                         "cannot get configuration path");
                 }
             }
         } catch (MissingResourceException e) {
             //ignored because bootstrap properties file maybe absent.
         }
         return configDir;
     }
 
     /**
      * Returns location of the bootstrap file.
      *
      * @return Location of the bootstrap file. Returns null if the file
      *         cannot be located 
      * @throws ConfiguratorException if servlet context is null or deployment
      *         application real path cannot be determined.
      */
     static String getBootStrapFile()
         throws ConfiguratorException {
         String bootstrap = null;
         
         String configDir = getPresetConfigDir();
         if ((configDir != null) && (configDir.length() > 0)) {
             bootstrap = configDir + "/bootstrap";
         } else {
             String locator = getBootstrapLocator();
             FileReader frdr = null;
 
             try {
                 frdr = new FileReader(locator);
                 BufferedReader brdr = new BufferedReader(frdr);
                 bootstrap = brdr.readLine() + "/bootstrap";
             } catch (IOException e) {
                 throw new ConfiguratorException(e.getMessage());
             } finally {
                 if (frdr != null) {
                     try {
                         frdr.close();
                     } catch (IOException e) {
                         //ignore
                     }
                 }
             }
         }
         
         if (bootstrap != null) {
             File test = new File(bootstrap);
             if (!test.exists()) {
                 bootstrap = null;
             }
         }
         return bootstrap;
     }
 
     // this is the file which contains the base dir.
     // this file is not created if configuration directory is 
     // preset in bootstrap.properties
     private static String getBootstrapLocator()
         throws ConfiguratorException {
         String configDir = getPresetConfigDir();
         if ((configDir != null) && (configDir.length() > 0)) {
             return null;
         }
         
         if (servletCtx != null) {
             String path = getNormalizedRealPath(servletCtx);
             if (path != null) {
                 return System.getProperty("user.home") + "/" +
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_DIR + "/" +
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_PREFIX + path;
             } else {
                 throw new ConfiguratorException(
                     "Cannot read the bootstrap path");
             }
         } else {
             throw new ConfiguratorException("Servlet Context is null");
         }
     }
     
     private static String getBaseDir() {
         String configDir = getPresetConfigDir();
         if ((configDir != null) && (configDir.length() > 0)) {
             return configDir;
         }
         if (servletCtx != null) {
             String path = getNormalizedRealPath(servletCtx);
             if (path != null) {
                 String bootstrap = System.getProperty("user.home") + "/" +
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_DIR + "/" +
                     SetupConstants.CONFIG_VAR_BOOTSTRAP_BASE_PREFIX + path;
                 File test = new File(bootstrap);
                 if (!test.exists()) {
                     return null;
                 }
                 FileReader frdr = null;
                 try {
                     frdr = new FileReader(bootstrap);
                     BufferedReader brdr = new BufferedReader(frdr);
                     return brdr.readLine();
                 } catch (IOException e) {
                     throw new ConfiguratorException(e.getMessage());
                 } finally {
                     if (frdr != null) {
                         try {
                             frdr.close();
                         } catch (IOException e) {
                             //ignore
                         }
                     }
                 }
             } else {
                 throw new ConfiguratorException(
                     "Cannot read the bootstrap path");
             }
         } else {
             throw new ConfiguratorException("Servlet Context is null");
         }
         
  
     }
     
     public static String getNormalizedRealPath(ServletContext servletCtx) {
         String path = null;
         if (servletCtx != null) {
             path = getAppResource(servletCtx);
             
             if (path != null) {
                 String realPath = servletCtx.getRealPath("/");
                 if ((realPath != null) && (realPath.length() > 0)) {
                     realPath = realPath.replace('\\', '/');
                     path = realPath.replaceAll("/", "_");
                 } else {
                     path = path.replaceAll("/", "_");
                 }
                 int idx = path.indexOf(":");
                 if (idx != -1) {
                     path = path.substring(idx + 1);
                 }
             }
         }
         return path;
     }
 
     /**
      * Returns URL of the default resource.
      *
      * @return URL of the default resource. Returns null of servlet context is
      *         null.
      */
     private static String getAppResource(ServletContext servletCtx) {
         if (servletCtx != null) {
             try {
                 java.net.URL turl = servletCtx.getResource("/");
                 return turl.getPath();
             } catch (MalformedURLException mue) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet.getAppResource: Cannot access the resource",
                     mue);
             }
         } else {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.getAppResource: Context is null");
         }
         return null;
     }
 
     /**
      * This method takes the name of XML file, process each 
      * request object one by one immediately after parsing.
      *
      * @param xmlBaseDir is the location of request xml files
      * @throws SMSException if error occurs in the service management space.
      * @throws SSOException if administrator single sign on is not valid.
      * @throws IOException if error accessing the configuration files.
      * @throws PolicyException if policy cannot be loaded.
      */
     private static void processDataRequests(String xmlBaseDir)
         throws SMSException, SSOException, IOException, PolicyException
     {
         SSOToken ssoToken = getAdminSSOToken();
         try {
             Map map = ServicesDefaultValues.getDefaultValues();
             String hostname = (String)map.get(
                 SetupConstants.CONFIG_VAR_SERVER_HOST);
             ConfigureData configData = new ConfigureData(
                 xmlBaseDir, servletCtx, hostname, ssoToken);
             configData.configure();
         } catch (SMSException e) {
             e.printStackTrace();
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         } catch (SSOException e) {
             e.printStackTrace();
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         } catch (IOException e) {
             e.printStackTrace();
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.processDataRequests", e);
             throw e;
         }
     }
 
     /**
      * Helper method to return Admin token
      * @return Admin Token
      */
     private static SSOToken getAdminSSOToken() {
         if (adminToken == null) {
             adminToken = (SSOToken)AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
         }
         return adminToken;
     }
 
     /**
      * Initialize AMConfig.propeties with host specific values
      */
     private static Map initializeConfigProperties()
         throws SecurityException, IOException {
         Map mapFileNameToContent = new HashMap();
         List dataFiles = getTagSwapConfigFiles();
         
         Map map = ServicesDefaultValues.getDefaultValues();
         String basedir = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
        
         String deployuri =
             (String)map.get(SetupConstants.CONFIG_VAR_SERVER_URI);
         try {
             File fhm = new File(basedir + deployuri + "/" + SMS_STR);
             fhm.mkdirs();
         } catch (SecurityException e){
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.initializeConfigProperties", e);
             throw e;
         }
         
         for (Iterator i = dataFiles.iterator(); i.hasNext(); ) {
             String file = (String)i.next();
             StringBuffer sbuf = null;
     
             /*
              * if the file's not there, just skip it
              * usually will be about a file included with FAM,
              * so it's informational, rather than a "real" error.
              */
             try {
                 sbuf = readFile(file);
             } catch (IOException ioex) {
                 break;
             }
             
             int idx = file.lastIndexOf("/");
             String absFile = (idx != -1) ? file.substring(idx+1) : file;
             
             if (absFile.equalsIgnoreCase(SetupConstants.AMCONFIG_PROPERTIES)) {
                 String dbOption = 
                     (String)map.get(SetupConstants.CONFIG_VAR_DATA_STORE);
                 boolean embedded = 
                     dbOption.equals(SetupConstants.SMS_EMBED_DATASTORE);
                 boolean dbSunDS = false;
                 boolean dbMsAD  = false;
                 if (embedded) {
                     dbSunDS = true;
                 } else { // Keep old behavior for now.
                     dbSunDS = dbOption.equals(SetupConstants.SMS_DS_DATASTORE);
                     dbMsAD  = dbOption.equals(SetupConstants.SMS_AD_DATASTORE);
                 }
 
                 if (dbSunDS || dbMsAD) {
                     int idx1 = sbuf.indexOf(
                         SetupConstants.CONFIG_VAR_SMS_DATASTORE_CLASS);
                     if (idx1 != -1) {
                         sbuf.replace(idx1, idx1 +
                             (SetupConstants.CONFIG_VAR_SMS_DATASTORE_CLASS)
                             .length(),
                             SetupConstants.CONFIG_VAR_DS_DATASTORE_CLASS);
                     }
                 }
             }
 
             /*
              *  if Linux, the default PAM service name is "password",
              *  rather than "other"
              */
             if (determineOS() == SetupConstants.LINUX) {
                 map.put(SetupConstants.PAM_SERVICE_NAME,
                         SetupConstants.LINUX_PAM_SVC_NAME);
             }
             
             String swapped = ServicesDefaultValues.tagSwap(sbuf.toString(),
                 file.endsWith("xml"));
             
             if (absFile.equalsIgnoreCase(SetupConstants.AMCONFIG_PROPERTIES) ||
                 absFile.equalsIgnoreCase(SystemProperties.CONFIG_FILE_NAME)
             ) {
                 mapFileNameToContent.put(absFile, swapped);
             } else if (absFile.equalsIgnoreCase(
                     SetupConstants.SECURID_PROPERTIES))
             {
                 writeToFile(basedir + deployuri + "/auth/ace/data/" + absFile,
                     swapped);
             } else {
                 writeToFile(basedir + "/" + absFile, swapped);
             }
         }
         return mapFileNameToContent;
     }
     
     private static StringBuffer readFile(String file) 
         throws IOException {
         InputStreamReader fin = null;
         StringBuffer sbuf = new StringBuffer();
         InputStream is = null;
         
         try {
             if ((is = servletCtx.getResourceAsStream(file)) == null) {
                 throw new IOException(file + " not found");
             }
             fin = new InputStreamReader(is);
             char[] cbuf = new char[1024];
             int len;
             while ((len = fin.read(cbuf)) > 0) {
                 sbuf.append(cbuf, 0, len);
             }
         } finally {
             if (fin != null) {
                 try {
                     fin.close();
                 } catch (Exception ex) {
                     //No handling required
                 }
             }
             if (is != null) {
                 try {
                     is.close();
                 } catch (Exception ex) {
                     // no handling required
                 }
             }
         }
         return sbuf;
     }
     
     private static void writeToFileEx(String fileName, String content)
         throws IOException {    
         File btsFile = new File(fileName);
         if (!btsFile.getParentFile().exists()) {
             btsFile.getParentFile().mkdirs();
         }
         writeToFile(fileName, content);
     }
     
     static void writeToFile(String fileName, String content) 
         throws IOException {
         FileWriter fout = null;
         try {
             fout = new FileWriter(fileName);
             fout.write(content);
         } catch (IOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.writeToFile", e);
             throw e;
         } finally {
             if (fout != null) {
                 try {
                     fout.close();
                 } catch (Exception ex) {
                     //No handling requried
                 }
             }
         }
     }
 
     /**
      * Returns secure random string.
      *
      * @return secure random string.
      */
     public static String getRandomString() {
         String randomStr = null;
         try {
             byte [] bytes = new byte[24];
             SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
             random.nextBytes(bytes);
             randomStr = Base64.encode(bytes).trim();
         } catch (Exception e) {
             randomStr = null;
             Debug.getInstance(SetupConstants.DEBUG_NAME).message(
                 "AMSetupServlet.getRandomString:" +
                 "Exception in generating encryption key.", e);
             e.printStackTrace();
         }
         return (randomStr != null) ? randomStr : 
             SetupConstants.CONFIG_VAR_DEFAULT_SHARED_KEY;
     }
 
     /**
       * Returns a unused port on a given host.
       *    @param hostname (eg localhost)
       *    @param start: starting port number to check (eg 389).
       *    @param incr : port number increments to check (eg 1000).
       *    @return available port num if found. -1 of not found.
       */
     static public int getUnusedPort(String hostname, int start, int incr)
     {
         int defaultPort = -1;
         for (int i=start;i<65500 && (defaultPort == -1);i+=incr) {
             if (canUseAsPort(hostname, i))
             {
                 defaultPort = i;
             }
         }
         return defaultPort;
     }
     
     /**
       * Checks whether the given host:port is currenly under use.
       *    @param hostname (eg localhost)
       *    @param incr : port number.
       *    @return  true if not in use, false if in use.
       */
     public static boolean canUseAsPort(String hostname, int port)
     {
         boolean canUseAsPort = false;
         ServerSocket serverSocket = null;
         try {
             InetSocketAddress socketAddress =
                 new InetSocketAddress(hostname, port);
             serverSocket = new ServerSocket();
             //if (!isWindows()) {
               //serverSocket.setReuseAddress(true);
             //}
             serverSocket.bind(socketAddress);
             canUseAsPort = true;
      
             serverSocket.close();
        
             Socket s = null;
             try {
               s = new Socket();
               s.connect(socketAddress, 1000);
               canUseAsPort = false;
        
             } catch (Throwable t) {
             }
             finally {
               if (s != null) {
                 try {
                   s.close();
                 } catch (Throwable t)
                 {
                 }
               }
             }
      
      
         } catch (IOException ex) {
           canUseAsPort = false;
         } finally {
             try {
                 if (serverSocket != null) {
                     serverSocket.close();
             }
             } catch (Exception ex) { }
         }
      
         return canUseAsPort;
     }
 
     /**
       * Obtains misc config data from a remote FAM server :
       *     opends port
       *     config basedn
       *     flag to indicate replication is already on or not
       *     opends replication port or opends sugested port
       *
       * @param server - URL string representing the remote fam server
       * @param userid - admin userid on remote server (only amdmin)
       * @param password - admin password
       * @return Map of confif params.
       * @throws <code>ConfiguratonException</code>
       *   errorCodes :
       *     400=Bad Request - userid/passwd param missing
       *     401=Unauthorized - invalid credentials
       *     405=Method Not Allowed - only POST is honored
       *     408=Request Timeout - requested timed out
       *     500=Internal Server Error 
       *     701=File Not Found - incorrect deployuri/server
       *     702=Connection Error - failed to connect
       */
     public static Map getRemoteServerInfo(
                       String server, String userid, String pwd)
     {
         HttpURLConnection conn = null;
         try {
             // Construct data
             String data = "IDToken1=" + URLEncoder.encode(userid, "UTF-8")+
                           "&IDToken2="+ URLEncoder.encode(pwd, "UTF-8");
             // Send data
             URL url = new URL(server+"/getServerInfo.jsp");
             conn = (HttpURLConnection) url.openConnection();
             conn.setDoOutput(true);
             OutputStreamWriter wr = 
                        new OutputStreamWriter(conn.getOutputStream());
             wr.write(data);
             wr.flush();
             // Get the response
             BufferedReader rd = 
                 new BufferedReader(new InputStreamReader(conn.getInputStream()));
             String line;
             Map info = null;
             while ((line = rd.readLine()) != null) {
                 if (line.length() == 0)
                     continue;
                 info = BootstrapData.queryStringToMap(line);
             }
             wr.close();
             rd.close();
             return info;
         } catch (javax.net.ssl.SSLHandshakeException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).warning(
                     "AMSetupServlet.getRemoteServerInfo()", e);
             throw new ConfiguratorException("702", null, 
                          java.util.Locale.getDefault());
         } catch (IllegalArgumentException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).warning(
                     "AMSetupServlet.getRemoteServerInfo()", e);
             throw new ConfiguratorException("702", null, 
                          java.util.Locale.getDefault());
         } catch (java.net.MalformedURLException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).warning(
                     "AMSetupServlet.getRemoteServerInfo()", e);
             throw new ConfiguratorException("702", null, 
                          java.util.Locale.getDefault());
         } catch (java.net.UnknownHostException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).warning(
                     "AMSetupServlet.getRemoteServerInfo()", e);
             throw new ConfiguratorException("702", null, 
                          java.util.Locale.getDefault());
         } catch (FileNotFoundException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).warning(
                     "AMSetupServlet.getRemoteServerInfo()", e);
             throw new ConfiguratorException("701", null, 
                          java.util.Locale.getDefault());
         } catch (java.net.ConnectException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).warning(
                     "AMSetupServlet.getRemoteServerInfo()", e);
             throw new ConfiguratorException("702", null, 
                          java.util.Locale.getDefault());
         } catch (IOException e) {
             ConfiguratorException cex = null;
             int status = 0;
             if (conn != null) {
                 try {
                    status = conn.getResponseCode();
                 } catch (Exception ig) {}
             }
   
             if (status == 401 || status == 400 || status == 405 ||
                 status == 408 ) {
                  cex = new ConfiguratorException(""+status, null, 
                          java.util.Locale.getDefault());
             } else  {
                  cex = new ConfiguratorException(e.getMessage());
             }
             Debug.getInstance(SetupConstants.DEBUG_NAME).warning(
                     "AMSetupServlet.getRemoteServerInfo()", e);
             throw cex;
         }
     }
 
     /**
      * Returns schema file names.
      *
      * @param dataStore Name of data store configuration data.
      * @throws MissingResourceException if the bundle cannot be found.
      */
     private static List getSchemaFiles(String dataStore)
         throws MissingResourceException
     {
         List fileNames = new ArrayList();
         ResourceBundle rb = ResourceBundle.getBundle(
             SetupConstants.SCHEMA_PROPERTY_FILENAME);
         String strFiles;
 
         boolean embedded = 
               dataStore.equals(SetupConstants.SMS_EMBED_DATASTORE);
         boolean isDSServer = false;
         if (embedded) {
             strFiles = rb.getString(
                 SetupConstants.OPENDS_SMS_PROPERTY_FILENAME); 
         } else {
             strFiles = rb.getString(
                 SetupConstants.DS_SMS_PROPERTY_FILENAME);
         }
         
         StringTokenizer st = new StringTokenizer(strFiles);
         while (st.hasMoreTokens()) {
             fileNames.add(st.nextToken());
         }
         return fileNames;
     }
 
     private static List getTagSwapConfigFiles()
         throws MissingResourceException
     {
         List fileNames = new ArrayList();
         ResourceBundle rb = ResourceBundle.getBundle("configuratorTagSwap");
         String strFiles = rb.getString("tagswap.files");
         StringTokenizer st = new StringTokenizer(strFiles);
         while (st.hasMoreTokens()) {
             fileNames.add(st.nextToken());
         }
         return fileNames;
     }
 
 
     private static boolean isIPAddress(String hostname) {
         StringTokenizer st = new StringTokenizer(hostname, ".");
         boolean isIPAddr = (st.countTokens() == 4);
         if (isIPAddr) {
             while (st.hasMoreTokens()) {
                 String token = st.nextToken();
                 try {
                     int node = Integer.parseInt(token);
                     isIPAddr = (node >= 0) && (node < 256);
                 } catch (NumberFormatException e) {
                     isIPAddr = false;
                 }
             }
         }
         return isIPAddr;
     }
 
     /**
      * Tag swaps strings in schema files.
      *
      * @param basedir the configuration base directory.
      * @param schemaFiles List of schema files to be loaded.
      * @throws IOException if data files cannot be written.
      */
     private static void writeSchemaFiles(
         String basedir,
         List schemaFiles
     )   throws IOException
     {
         for (Iterator i = schemaFiles.iterator(); i.hasNext(); ) {
             String file = (String)i.next();
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
                 fout = new FileWriter(basedir + "/" + absFile);
                 String inpStr = sbuf.toString();
                 fout.write(ServicesDefaultValues.tagSwap(inpStr));
             } catch (IOException ioex) {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "AMSetupServlet.writeSchemaFiles: " +
                     "Exception in writing schema files:" , ioex);
                 throw ioex;
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
         AMSetupDSConfig dsConfig = AMSetupDSConfig.getInstance();
         dsConfig.loadSchemaFiles(schemaFiles);
     }
 
     /**
      * Update famsts.wsdl with Keystore location.
      *
      * @param basedir the configuration base directory.
      * @param deployuri the deployment URI. 
      * @throws IOException if password files cannot be written.
      */
     private static void updateSTSwsdl(
         String basedir, 
         String deployuri 
     ) throws IOException {
         // Get FAM web application base location.
         URL url = servletCtx.getResource("/WEB-INF/lib/fam.jar");
         String webAppLocation = (url.toString()).substring(5);
         int index = webAppLocation.indexOf("WEB-INF");
         webAppLocation = webAppLocation.substring(0, index-1);
         
         // Update famsts.wsdl with Keystore location.
         String contentWSDL = getFileContent("/WEB-INF/wsdl/famsts.wsdl");
         contentWSDL = contentWSDL.replaceAll("@KEYSTORE_LOCATION@",
             basedir + deployuri);
         BufferedWriter outWSDL = 
             new BufferedWriter(new FileWriter(webAppLocation +
             "/WEB-INF/wsdl/famsts.wsdl"));
         outWSDL.write(contentWSDL);
         outWSDL.close();
     }
     
     private static String getFileContent(String fileName) throws IOException {
         InputStream in = servletCtx.getResourceAsStream(fileName);
         if (in == null) {
             throw new IOException("Unable to open " + fileName);
         }
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         StringBuffer buff = new StringBuffer();
         String line = reader.readLine();
 
         while (line != null) {
             buff.append(line).append("\n");
             line = reader.readLine();
         }
         reader.close();
         return buff.toString();      
     }
     
     /**
      * Create the storepass and keypass files
      *
      * @param basedir the configuration base directory.
      * @param deployuri the deployment URI. 
      * @throws IOException if password files cannot be written.
      */
     private static void createPasswordFiles(
         String basedir, 
         String deployuri 
     ) throws IOException
     {
         String pwd = Crypt.encrypt("secret");
         String location = basedir + deployuri + "/" ;
         writeContent(location + ".keypass", pwd);
         writeContent(location + ".storepass", pwd);
 
         InputStream in = servletCtx.getResourceAsStream(
                 "/WEB-INF/template/keystore/keystore.jks");
         byte[] b = new byte[2007];
         in.read(b);
         in.close();
         FileOutputStream fos = new FileOutputStream(location + "keystore.jks");
         fos.write(b);
         fos.flush();
         fos.close();
     }
 
     /**
      * Helper method to create the storepass and keypass files
      *
      * @param fName is the name of the file to create.
      * @param content is the password to write in the file.
      */
     private static void writeContent(String fName, String content)
         throws IOException
     {
         FileWriter fout = null;
         try {
             fout = new FileWriter(new File(fName));
             fout.write(content);
         } catch (IOException ioex) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.writeContent: " +
                 "Exception in creating password files:" , ioex);
             throw ioex;
         } finally {
             if (fout != null) {
                 try {
                     fout.close();
                 } catch (Exception ex) {
                     //No handling requried
                 }
             }
         }
     }
 
     /**
       * Update Embedded Idrepo instance with new embedded opends isntance.
       */
     private static void  updateEmbeddedIdRepo(
         String orgName, 
         String configName, 
         String entry
     ) throws SMSException, SSOException {
         SSOToken token = (SSOToken)
             AccessController.doPrivileged(AdminTokenAction.getInstance());
         ServiceConfigManager scm = new ServiceConfigManager(token,
             IdConstants.REPO_SERVICE, "1.0");
         ServiceConfig sc = scm.getOrganizationConfig(orgName, null);
         if (sc != null) {
             ServiceConfig subConfig = sc.getSubConfig(configName);
             if (subConfig != null) {
                 Map configMap = subConfig.getAttributes();
                 Set vals = (Set)configMap.get(
                     "sun-idrepo-ldapv3-config-ldap-server");
                 vals.add(entry);
                 HashMap mp = new HashMap(2);
                 mp.put("sun-idrepo-ldapv3-config-ldap-server", vals);
                 subConfig.setAttributes(mp);
             }
         }
     }
 
     /**
      * Update platform server list and Organization alias
      */
     private static void updatePlatformServerList(
         String serverURL,
         String hostName
     ) throws SMSException, SSOException 
     {
         SSOToken token = getAdminSSOToken();
         ServiceSchemaManager ssm = new ServiceSchemaManager(
             "iPlanetAMPlatformService", token);
         ServiceSchema ss = ssm.getGlobalSchema();
         AttributeSchema as = ss.getAttributeSchema(
             "iplanet-am-platform-server-list");
         Set values = as.getDefaultValues();
         if (!isInPlatformList(values, serverURL)) {
             String instanceName = getNextAvailableServerId(values);
             values.add(serverURL + "|" + instanceName);
             as.setDefaultValues(values);
 
             // Update Organization Aliases
             OrganizationConfigManager ocm =
                 new OrganizationConfigManager(token, "/");
         
             Map attrs = ocm.getAttributes("sunIdentityRepositoryService");
             Set origValues = (Set)attrs.get("sunOrganizationAliases");
             if (!origValues.contains(hostName)) {
                 values = new HashSet();
                 values.add(hostName);
                 ocm.addAttributeValues("sunIdentityRepositoryService",
                     "sunOrganizationAliases", values);
             }
         }
     }
 
     private static String getNextAvailableServerId(Set values) {
         int instanceNumber = 1;
         int maxNumber = 1;
 
         for (Iterator items = values.iterator(); items.hasNext();) {
             String item = (String) items.next();
             int index1 = item.indexOf('|');
 
             if (index1 != -1) {
                 int index2 = item.indexOf('|', index1 + 1);
                 item = (index2 == -1) ? item.substring(index1 + 1) :
                     item.substring(index1 + 1, index2);
 
                 try {
                     int n = Integer.parseInt(item);
                     if (n > maxNumber) {
                         maxNumber = n;
                     }
                 } catch (NumberFormatException nfe) {
                     // Ignore and continue
                 }
             }
         }
         String instanceName = Integer.toString(maxNumber + 1);
         
         if (instanceName.length() == 1) {
             instanceName = "0" + instanceName;
         }
         
         return instanceName;
     }
     
     private static boolean isInPlatformList(Set values, String hostname) {
         boolean found = false;
         for (Iterator items = values.iterator(); items.hasNext() && !found;) {
             String item = (String) items.next();
             int idx = item.indexOf('|');
             if (idx != -1) {
                 String svr = item.substring(0, idx);
                 found = svr.equals(hostname);
             }
         }
         return found;
     }
     
     private static boolean isAgentServiceLoad(SSOToken token) {
         try {
             ServiceSchemaManager ssm = new ServiceSchemaManager(
                 "AgentService", token);
             return (ssm != null);
         } catch (SSOException ex) {
             return false;
         } catch (SMSException ex) {
             return false;
         }
     }
 
     private static void createDemoUser() {
         Map attributes = new HashMap();
         Set setSN = new HashSet(2);
         setSN.add("demo");
         attributes.put("sn", setSN);
         Set setCN = new HashSet(2);
         setCN.add("demo");
         attributes.put("cn", setCN);
         Set setPwd = new HashSet(2);
         setPwd.add("changeit");
         attributes.put("userpassword", setPwd);
         Set setStatus = new HashSet(2);
         setStatus.add("Active");
         attributes.put("inetuserstatus", setStatus);
         try {
             AMIdentityRepository amir = new AMIdentityRepository(
                 getAdminSSOToken(), "/");
             amir.createIdentity(IdType.USER, "demo", attributes);
         } catch (IdRepoException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.createDemoUser", e);
         } catch (SSOException e) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.createDemoUser", e);
         }
     }
 
     /**
      * Creates Identities for WS Security
      *
      * @param serverURL URL at which Access Manager is configured.
      */
     private static void createIdentitiesForWSSecurity(
         String serverURL,
         String deployuri
     ) throws IdRepoException, SSOException {
         SSOToken token = getAdminSSOToken();
         
         if (!isAgentServiceLoad(token)) {
             return;
         }
         AMIdentityRepository idrepo = new AMIdentityRepository(token, "/");
         //createUser(idrepo, "jsmith", "John", "Smith");
         //createUser(idrepo, "jondoe", "Jon", "Doe");
         Map config = new HashMap();
 
         // Add WSC configuration
         config.put("sunIdentityServerDeviceStatus","Active");
         config.put("SecurityMech","urn:sun:wss:security:null:UserNameToken");
         config.put("UserCredential","UserName:test|UserPassword:test");
         config.put("useDefaultStore","true");
         config.put("privateKeyAlias","test");
         config.put("publicKeyAlias","test");
         config.put("isRequestSign","true");
         config.put("keepSecurityHeaders","true");
         config.put("WSPEndpoint","default");
         config.put("AgentType","WSCAgent");
         createAgent(idrepo, "wsc", "WSC", "", config);
 
         // Add WSP configuration
         config.remove("AgentType");
         config.put("AgentType","WSPAgent");
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:sun:wss:security:null:UserNameToken," +
                                   "urn:sun:wss:security:null:SAML2Token-HK," +
                                   "urn:sun:wss:security:null:SAML2Token-SV");
         createAgent(idrepo, "wsp", "WSP", "", config);
 
         // Add localSTS configuration
         config.remove("AgentType");
         config.put("AgentType","STSAgent");
         config.remove("SecurityMech");
         config.remove("UserCredential");
         config.remove("keepSecurityHeaders");
         config.remove("WSPEndpoint");
         config.put("SecurityMech","urn:sun:wss:security:null:X509Token");
         config.put("STSEndpoint",serverURL + deployuri + "/sts");
         config.put("STSMexEndpoint",serverURL + deployuri + "/sts/mex");
         createAgent(idrepo, "defaultSTS", "STS", "", config);
         createAgent(idrepo, "SecurityTokenService", "STS", "", config);
 
         /*
         // Add UsernameToken profile
         createAgent(idrepo, "UserNameToken", "WSP",
             "WS-I BSP UserName Token Profile Configuration", config);
 
         // Add SAML-HolderOfKey
         config.remove("SecurityMech");
         config.remove("UserCredential");
         config.put("SecurityMech","urn:sun:wss:security:null:SAMLToken-HK");
         createAgent(idrepo, "SAML-HolderOfKey", "WSP",
             "WS-I BSP SAML Holder Of Key Profile Configuration", config);
 
         // Add SAML-SenderVouches
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:sun:wss:security:null:SAMLToken-SV");
         createAgent(idrepo, "SAML-SenderVouches", "WSP",
             "WS-I BSP SAML Sender Vouches Token Profile Configuration", config);
 
         // Add X509Token
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:sun:wss:security:null:X509Token");
         createAgent(idrepo, "X509Token", "WSP",
             "WS-I BSP X509 Token Profile Configuration", config);
 
         // Add LibertyX509Token
         config.put("TrustAuthority","LocalDisco");
         config.put("WSPEndpoint","http://wsp.com");
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:liberty:security:2005-02:null:X509");
         createAgent(idrepo, "LibertyX509Token", "WSP",
             "Liberty X509 Token Profile Configuration", config);
 
         // Add LibertyBearerToken
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:liberty:security:2005-02:null:Bearer");
         createAgent(idrepo, "LibertyBearerToken", "WSP",
             "Liberty SAML Bearer Token Profile Configuration", config);
 
         // Add LibertySAMLToken
         config.remove("SecurityMech");
         config.put("SecurityMech","urn:liberty:security:2005-02:null:SAML");
         createAgent(idrepo, "LibertySAMLToken", "WSP",
             "Liberty SAML Token Profile Configuration", config);
 
         // Add local discovery service
         config.clear();
         config.put("AgentType","DiscoveryAgent");
         config.put("Endpoint", serverURL + deployuri + "/Liberty/disco");
         createAgent(idrepo, "LocalDisco", "Discovery",
             "Local Liberty Discovery Service Configuration", config);*/
     }
 
     private static void createUser(
         AMIdentityRepository idrepo,
         String uid,
         String gn,
         String sn
     ) throws IdRepoException, SSOException 
     {
         Map attributes = new HashMap();
         Set values = new HashSet();
         values.add(uid);
         attributes.put("uid", values);
         values = new HashSet();
         values.add(gn);
         attributes.put("givenname", values);
         values = new HashSet();
         values.add(sn);
         attributes.put("sn", values);
         values = new HashSet();
         values.add(gn + " " + sn);
         attributes.put("cn", values);
         values = new HashSet();
         values.add(uid);
         attributes.put("userPassword", values);
         AMIdentity id = idrepo.createIdentity(IdType.USER, uid, attributes);
         id.assignService("sunIdentityServerDiscoveryService",
             Collections.EMPTY_MAP);
     }
 
 
     private static void createAgent(
         AMIdentityRepository idrepo,
         String name,
         String type,
         String desc,
         Map config
     ) throws IdRepoException, SSOException 
     {
         Map attributes = new HashMap();
 
         Set values = new HashSet();
         values.add(name);
         attributes.put("userpassword", values);
 
         for (Iterator i = config.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             String value = (String)config.get(key);
             values = new HashSet();
             if (value.indexOf(",") != -1) {
                 StringTokenizer st = new StringTokenizer(value, ",");
                 while(st.hasMoreTokens()) {
                     values.add(st.nextToken());
                 }
             } else {
                 values.add(value);
             }
             attributes.put(key, values);
         }
         
         idrepo.createIdentity(IdType.AGENTONLY, name, attributes);
     }
 
     private static void startRegistrationProcess(String basedir,
         String deployuri)
     {
         /*
          *  make sure the basedir + "/" + deployuri + "/lib/registration"
          *  directory exists, and then put the registration jar and xml
          *  files there, before starting the registration process.
          */
 
         String libRegDir = basedir + "/" + deployuri + "/lib/registration";
         File reglibDir = new File(libRegDir);
         if (reglibDir.mkdirs()) {
             if (copyRegFiles(libRegDir)) {
                 StartRegister sr = new StartRegister();
                 sr.servicetagTransfer();
             } else {
                 Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                     "configure: failed to copy registration files to " +
                     reglibDir.getPath());
             }
         } else {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "configure: failed to create registration lib directory");
         }
     }
 
     private static boolean copyRegFiles(String destDir) {
         String [] jarFiles = {"scn_stprs_util.jar",
                               "commons-codec-1.3.jar",
                               "opensso-register.jar"};
         String [] xmlFiles = {"servicetag-registry.xml"};
 
         try {
             for (int i = 0; i < jarFiles.length; i++) {
                 copyCtxFile ("/WEB-INF/lib/", jarFiles[i], destDir);
             }
             for (int i = 0; i < xmlFiles.length; i++) {
                 copyCtxFile ("/WEB-INF/classes/", xmlFiles[i], destDir);
             }
         } catch (IOException ioex) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.copyRegFiles:", ioex);
             return false;
         }
         return true;
     }
 
 
     private static void setupSecurIDDirs(String basedir, String deployuri) {
         /*
          *  make sure the basedir + "/" + deployuri + "/auth/ace/data"
          *  directory exists.
          */
 
         String aceDataDir = basedir + "/" + deployuri + "/auth/ace/data";
         File dataAceDir = new File(aceDataDir);
         if (!dataAceDir.mkdirs()) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.setupSecurIDDirs: " +
                 "failed to create SecurID data directory");
         }
     }
 
     private static void copyAuthSecurIDFiles (String destDir) {
         /*
          * not rsa_api.properties, as it's tagged swapped and
          * written to <configdir>/<uri>/auth/ace/data earlier.
          *
          * if file isn't copied, it's probably because this is
          * an OpenSSO deployment, rather than FAM, so it would
          * just be informational, but at the debug error level.
          * additionally, before some point, the debug stuff can't
          * be invoked.
          */
         String [] propFiles = {"log4j.properties"};
         try {
             for (int i = 0; i < propFiles.length; i++) {
                 copyCtxFile ("/WEB-INF/classes/", propFiles[i], destDir);
             }
         } catch (IOException ioex) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.copyAuthSecurIDFiles:", ioex);
         }
     }
 
     private static void setupSafeWordDirs(String basedir, String deployuri)
     {
         /*
          *  make sure the
          *  basedir + "/" + deployuri + "/auth/safeword/serverVerification" 
          *  directory exists.
          */
 
         String safewordDir = basedir + "/" + deployuri +
             "/auth/safeword/serverVerification";
         File safewordFDir = new File(safewordDir);
         if (!safewordFDir.mkdirs()) {
             Debug.getInstance(SetupConstants.DEBUG_NAME).error(
                 "AMSetupServlet.setupSafeWordDirs: " +
                 "failed to create SafeWord verification directory");
         }
     }
 
     private static boolean copyCtxFile (String srcDir, String file,
         String destDir) throws IOException
     {
         InputStream in = servletCtx.getResourceAsStream(srcDir + file);
         if (in != null) {
             FileOutputStream fos = new FileOutputStream(destDir + "/" + file);
             byte[] b = new byte[2000];
             int len;
             while ((len = in.read(b)) > 0) {
                 fos.write(b, 0, len);
             }
             fos.close();
             in.close();
         } else {
             return false;
         }
         return true;
     }
     
     private static void initDSConfigMgr(String str) 
         throws LDAPServiceException {
         ByteArrayInputStream bis = null;
         try {
             bis = new ByteArrayInputStream(str.getBytes());
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
 
     private static String mapToString(Map map) {
         StringBuffer buff = new StringBuffer();
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             buff.append(key).append("=").append((String)map.get(key))
                 .append("\n");
         }
         return buff.toString();
     }
 
     private static String determineOS() {
         String OS_ARCH = System.getProperty("os.arch");
         String OS_NAME = System.getProperty("os.name");
         if (OS_ARCH.toLowerCase().indexOf(SetupConstants.X86) >= 0) {
             if (OS_NAME.toLowerCase().indexOf(SetupConstants.WINDOWS) >= 0) {
                 return SetupConstants.WINDOWS;
             } else {
                 if (OS_NAME.toLowerCase().indexOf(SetupConstants.SUNOS) >= 0) {
                     return SetupConstants.X86SOLARIS;
                 } else {
                     return SetupConstants.LINUX;
                 }
             }
         } else {
             return SetupConstants.SOLARIS;
         }
     }
 }
