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
 * $Id: TestCommon.java,v 1.18 2007-08-17 22:46:27 bt199000 Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.qatest.common;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
 import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
 import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.authentication.AuthContext;
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.lang.StringBuffer;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.ResourceBundle;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import org.testng.Reporter;
 
 /**
  * This class is the base for all <code>OpenSSO</code> QA testcases.
  * It has commonly used methods.
  */
 public class TestCommon implements TestConstants {
     private String logEntryTemplate;
     private String className;
     static private ResourceBundle rb_amconfig;
     static protected String adminUser;
     static protected String adminPassword;
     static protected String basedn;
     static protected String host;
     static protected String protocol;
     static protected String port;
     static protected String uri;
     static protected String realm;
     static protected String serverName;
     static protected Level logLevel;
     static private Logger logger;
     private String productSetupResult;
     
     protected static String newline = System.getProperty("line.separator");
     protected static String fileseparator =
             System.getProperty("file.separator");
     
     static {
         try {
             rb_amconfig = ResourceBundle.getBundle(
                     TestConstants.TEST_PROPERTY_AMCONFIG);
             logger = Logger.getLogger("com.sun.identity.qatest");
             serverName = rb_amconfig.getString(
                     TestConstants.KEY_ATT_SERVER_NAME);
             FileHandler fileH = new FileHandler(serverName + fileseparator +
                     "logs");
             SimpleFormatter simpleF = new SimpleFormatter();
             fileH.setFormatter(simpleF);
             logger.addHandler(fileH);
             String logL = rb_amconfig.getString(
                     TestConstants.KEY_ATT_LOG_LEVEL);
             if ((logL != null)) {
                 logger.setLevel(Level.parse(logL));
             }
             logLevel = logger.getLevel();
             adminUser = rb_amconfig.getString(
                     TestConstants.KEY_ATT_AMADMIN_USER);
             adminPassword = rb_amconfig.getString(
                     TestConstants.KEY_ATT_AMADMIN_PASSWORD);
             basedn = rb_amconfig.getString(TestConstants.KEY_AMC_BASEDN);
             protocol = rb_amconfig.getString(TestConstants.KEY_AMC_PROTOCOL);
             host = rb_amconfig.getString(TestConstants.KEY_AMC_HOST);
             port = rb_amconfig.getString(TestConstants.KEY_AMC_PORT);
             uri = rb_amconfig.getString(TestConstants.KEY_AMC_URI);
             realm = rb_amconfig.getString(TestConstants.KEY_ATT_REALM);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     private TestCommon() {
     }
     
     protected TestCommon(String componentName) {
         logEntryTemplate = this.getClass().getName() + ".{0}: {1}";
         className = this.getClass().getName();
         productSetupResult = rb_amconfig.getString(
                 TestConstants.KEY_ATT_PRODUCT_SETUP_RESULT);
         assert (productSetupResult.equals("pass"));
     }
     
     /**
      * Writes a log entry for entering a test method.
      */
     protected void entering(String methodName, Object[] params) {
         if (params != null) {
             logger.entering(className, methodName, params);
         } else {
             logger.entering(className, methodName);
         }
     }
     
     /**
      * Writes a log entry for exiting a test method.
      */
     protected void exiting(String methodName) {
         logger.exiting(className, methodName);
     }
     
     /**
      * Writes a log entry.
      */
     protected void log(Level level, String methodName, Object message) {
         Object[] args = {methodName, message};
         logger.log(level, MessageFormat.format(logEntryTemplate, args));
     }
     
     /**
      * Writes a log entry.
      */
     protected void log(
             Level level,
             String methodName,
             String message,
             Object[] params
             ) {
         Object[] args = {methodName, message};
         logger.log(level, MessageFormat.format(logEntryTemplate, args), params);
     }
     
     /**
      * Writes a log entry for testng report
      */
     protected void logTestngReport(Map m) {
         Set s = m.keySet();
         Iterator it = s.iterator();
         while (it.hasNext()) {
             String key = (String)it.next();
             String value = (String)m.get(key);
             Reporter.log(key + "=" + value);
         }
     }
     
     /**
      * Returns single sign on token.
      */
     protected SSOToken getToken(String name, String password, String basedn)
     throws Exception {
         log(Level.FINEST, "getToken", name);
         log(Level.FINEST, "getToken", password);
         log(Level.FINEST, "getToken", basedn);
         AuthContext authcontext = new AuthContext(basedn);
         authcontext.login();
         javax.security.auth.callback.Callback acallback[] =
                 authcontext.getRequirements();
         for (int i = 0; i < acallback.length; i++){
             if (acallback[i] instanceof NameCallback) {
                 NameCallback namecallback = (NameCallback)acallback[i];
                 namecallback.setName(name);
             }
             if (acallback[i] instanceof PasswordCallback) {
                 PasswordCallback passwordcallback =
                         (PasswordCallback)acallback[i];
                 passwordcallback.setPassword(password.toCharArray());
             }
         }
         
         authcontext.submitRequirements(acallback);
         if (authcontext.getStatus() ==
                 com.sun.identity.authentication.AuthContext.Status.SUCCESS)
             log(Level.FINEST, "getToken", "Successful authentication ....... ");
         SSOToken ssotoken = authcontext.getSSOToken();
         log(Level.FINEST, "getToken",
                 (new StringBuilder()).append("TOKENCREATED>>> ").
                 append(ssotoken).toString());
         return ssotoken;
     }
     
     /**
      * Validate single sign on token.
      */
     protected boolean validateToken(SSOToken ssotoken)
     throws Exception {
         log(Level.INFO, "validateToken", "Inside validate token");
         SSOTokenManager stMgr = SSOTokenManager.getInstance();
         boolean bVal = stMgr.isValidToken(ssotoken);
         if (bVal)
             log(Level.INFO, "validateToken", "Token is Valid");
         else
             log(Level.INFO, "validateToken", "Token is Invalid");
         return bVal;
     }
     
     /**
      * Destroys single sign on token.
      */
     protected void destroyToken(SSOToken ssotoken)
     throws Exception {
         log(Level.INFO, "destroyToken", "Inside destroy token");
         if (validateToken(ssotoken)) {
             SSOTokenManager stMgr = SSOTokenManager.getInstance();
             stMgr.destroyToken(ssotoken);
         }
     }
     
     /**
      * Returns the base directory where code base is
      * checked out.
      */
     protected String getBaseDir()
     throws Exception {
         log(Level.FINE, "getBaseDir", "Inside getBaseDir");
         String strCD =  System.getProperty("user.dir");
         log(Level.FINEST, "getBaseDir", "Current Directory:" + strCD);
         return (strCD);
     }
     
     /**
      * Reads a file containing data-value pairs and returns that as a list
      * object.
      */
     protected List getListFromFile(String fileName)
     throws Exception {
         ArrayList list = null;
         if (fileName != null) {
             list = new ArrayList();
             BufferedReader input = new BufferedReader(new FileReader(fileName));
             String line = null;
             while ((line=input.readLine()) != null) {
                 if ((line.indexOf("=")) != -1)
                     list.add(line);
             }
             log(Level.FINEST, "getListFromFile", "List :" + list);
             if (input != null)
                 input.close();
         }
         return (list);
     }
     
     /**
      * Login to admin console using htmlunit
      */
     protected HtmlPage consoleLogin(
             WebClient webclient,
             String amUrl,
             String amadmUser,
             String amadmPassword)
     throws Exception {
         entering("consoleLogin", null);
         log(Level.FINEST, "consoleLogin", "JavaScript Enabled:" +
                 webclient.isJavaScriptEnabled());
         log(Level.FINEST, "consoleLogin", "Redirect Enabled:" +
                 webclient.isRedirectEnabled());
         URL url = new URL(amUrl);
         HtmlPage page = (HtmlPage)webclient.getPage(amUrl);
         log(Level.FINEST, "consoleLogin", page.getTitleText());
         HtmlForm form = page.getFormByName("Login");
         HtmlHiddenInput txt1 =
                 (HtmlHiddenInput)form.getInputByName("IDToken1");
         txt1.setValueAttribute(amadmUser);
         HtmlHiddenInput txt2 =
                 (HtmlHiddenInput)form.getInputByName("IDToken2");
         txt2.setValueAttribute(amadmPassword);
         page = (HtmlPage)form.submit();
         exiting("consoleLogin");
         return (page);
     }
     
     /**
      * Creates a map object and adds all the configutaion properties to that.
      */
     protected Map getConfigurationMap(String rb)
     throws Exception {
         entering("getConfigurationMap", null);
         ResourceBundle cfg = ResourceBundle.getBundle(rb);
         Map<String, String> map = new HashMap<String, String>();
         map.put("serverurl",protocol + ":" + "//" + host + ":" + port);
         map.put("serveruri",uri);
         map.put(TestConstants.KEY_ATT_COOKIE_DOMAIN, cfg.getString(
                 TestConstants.KEY_ATT_COOKIE_DOMAIN));
         map.put(TestConstants.KEY_ATT_AMADMIN_PASSWORD, cfg.getString(
                 TestConstants.KEY_ATT_AMADMIN_PASSWORD));
         map.put(TestConstants.KEY_ATT_CONFIG_DIR, cfg.getString(
                 TestConstants.KEY_ATT_CONFIG_DIR));
         map.put(TestConstants.KEY_ATT_CONFIG_DATASTORE, cfg.getString(
                 TestConstants.KEY_ATT_CONFIG_DATASTORE));
         map.put(TestConstants.KEY_ATT_AM_ENC_KEY,
                 cfg.getString(TestConstants.KEY_ATT_AM_ENC_KEY));
         map.put(TestConstants.KEY_ATT_DIRECTORY_SERVER, cfg.getString(
                 TestConstants.KEY_ATT_DIRECTORY_SERVER));
         map.put(TestConstants.KEY_ATT_DIRECTORY_PORT, cfg.getString(
                 TestConstants.KEY_ATT_DIRECTORY_PORT));
         map.put(TestConstants.KEY_ATT_CONFIG_ROOT_SUFFIX,
                 cfg.getString(TestConstants.KEY_ATT_CONFIG_ROOT_SUFFIX));
         map.put(TestConstants.KEY_ATT_SM_ROOT_SUFFIX,
                 cfg.getString(TestConstants.KEY_ATT_SM_ROOT_SUFFIX));
         map.put(TestConstants.KEY_ATT_DS_DIRMGRDN, cfg.getString(
                 TestConstants.KEY_ATT_DS_DIRMGRDN));
         map.put(TestConstants.KEY_ATT_DS_DIRMGRPASSWD,
                 cfg.getString(TestConstants.KEY_ATT_DS_DIRMGRPASSWD));
         map.put(TestConstants.KEY_ATT_LOAD_UMS, cfg.getString(
                 TestConstants.KEY_ATT_LOAD_UMS));
         exiting("getConfigurationMap");
         
         return map;
     }
     
     /**
      * Configures opensso using the configurator page. It map needs to set the
      * following values:
      * serverurl                 <protocol + ":" + "//" + host + ":" + port>
      * serveruri                 <URI for configured instance>
      * cookiedomain              <full cookie domain name>
      * amadmin_password          <password for amadmin user>
      * config_dir                <directory where product will be installed>
      * datastore                 <type of statstore: faltfile, dirServer or
      *                            activeDir>
      * directory_server          <directory server hostname>
      * directory_port            <directory server port>
      * config_root_suffix        <suffix under which configuration data will
      *                            be stored>
      * sm_root_suffix            <suffix where sms data will be stored>
      * ds_dirmgrdn               <directory user with administration
      *                            privilages>
      * ds_dirmgrpasswd           <password for directory user with
      *                            administration privilages>
      * load_ums                  <to load user schema or not(yes or no)>
      */
     protected boolean configureProduct(Map map)
     throws Exception {
         entering("configureProduct", null);
         
         log(Level.FINEST, "configureProduct", "Configuration Map:" + map);
         
         WebClient webclient = new WebClient();
         String strURL = (String)map.get("serverurl") +
                 (String)map.get("serveruri") + "/UI/Login";
         log(Level.FINEST, "configureProduct", "strURL:" + strURL);
         URL url = new URL(strURL);
         HtmlPage page = null;
         try {
             page = (HtmlPage)webclient.getPage(url);
         } catch(com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException e)
         {
             log(Level.INFO, "configureProduct", strURL + " cannot be reached.");
             return false;
         }
         
         if (getHtmlPageStringIndex(page, "Not Found") != -1) {
             log(Level.INFO, "configureProduct",
                     "Product Configuration was not" +
                     " successfull." + strURL + "was not found." +
                     " Please check if war is deployed properly.");
             exiting("configureProduct");
             return false;
         }
         
         if (getHtmlPageStringIndex(page, "configurator.jsp") != -1) {
             log(Level.INFO, "configureProduct", "Inside configurator.");
             HtmlForm form = (HtmlForm)page.getForms().get(0);
             
             HtmlTextInput txtServer =
                     (HtmlTextInput)form.getInputByName("SERVER_URL");
             txtServer.setValueAttribute((String)map.get("serverurl"));
             
             HtmlTextInput txtCookieDomain =
                     (HtmlTextInput)form.getInputByName("COOKIE_DOMAIN");
             txtCookieDomain.setValueAttribute((String)map.get(
                     TestConstants.KEY_ATT_COOKIE_DOMAIN));
             
             HtmlPasswordInput txtAmadminPassword =
                     (HtmlPasswordInput)form.getInputByName("ADMIN_PWD");
             txtAmadminPassword.setValueAttribute((String)map.get(
                     TestConstants.KEY_ATT_AMADMIN_PASSWORD));
             HtmlPasswordInput txtAmadminPasswordR =
                     (HtmlPasswordInput)form.getInputByName("ADMIN_CONFIRM_PWD");
             txtAmadminPasswordR.setValueAttribute((String)map.get(
                     TestConstants.KEY_ATT_AMADMIN_PASSWORD));
             
             HtmlTextInput txtConfigDir =
                     (HtmlTextInput)form.getInputByName("BASE_DIR");
             txtConfigDir.setValueAttribute((String)map.get(
                     TestConstants.KEY_ATT_CONFIG_DIR));
 
             HtmlTextInput txtEncryptionKey =
                     (HtmlTextInput)form.getInputByName("AM_ENC_KEY");
             String strEncryptKey = (String)map.get(
                     TestConstants.KEY_ATT_AM_ENC_KEY);
             if (!(strEncryptKey.equals(null)) && !(strEncryptKey.equals("")))
                 txtEncryptionKey.setValueAttribute(strEncryptKey);
 
             String strConfigStore = (String)map.get(
                     TestConstants.KEY_ATT_CONFIG_DATASTORE);
             log(Level.INFO, "configureProduct", "Config store is:" +
                     strConfigStore);
 
             HtmlRadioButtonInput rbDataStore =
                     (HtmlRadioButtonInput)form.getInputByName("DATA_STORE");
             rbDataStore.setDefaultValue(strConfigStore);
 
             if (strConfigStore.equals("flatfile")) {
                 log(Level.INFO, "configureProduct",
                         "Doing File System configuration.");
             } else {
                 log(Level.INFO, "configureProduct",
                         "Doing directory configuration.");
                 if (strConfigStore.equals("dirServer")) {
                     log(Level.INFO, "configureProduct",
                         "Doing Sun ONE Directory Server configuration.");
                 }
                 if (strConfigStore.equals("activeDir")) {
                     log(Level.INFO, "configureProduct",
                         "Doing Active Directory configuration.");
                 }
                 
                 HtmlTextInput txtDirServerName =
                         (HtmlTextInput)form.getInputByName("DIRECTORY_SERVER");
                 txtDirServerName.
                         setValueAttribute((String)map.get(
                         TestConstants.KEY_ATT_DIRECTORY_SERVER));
                 
                 HtmlTextInput txtDirServerPort =
                         (HtmlTextInput)form.getInputByName("DIRECTORY_PORT");
                 txtDirServerPort.
                         setValueAttribute((String)map.get(
                         TestConstants.KEY_ATT_DIRECTORY_PORT));
                 
                 HtmlTextInput txtDirConfigData =
                         (HtmlTextInput)form.getInputByName("ROOT_SUFFIX");
                 txtDirConfigData.setValueAttribute((String)map.
                         get(TestConstants.KEY_ATT_CONFIG_ROOT_SUFFIX));
                 
                 HtmlTextInput txtDirSMData =
                         (HtmlTextInput)form.
                         getInputByName("SM_CONFIG_ROOT_SUFFIX");
                 txtDirSMData.setValueAttribute((String)map.
                         get(TestConstants.KEY_ATT_SM_ROOT_SUFFIX));
                 
                 HtmlTextInput txtDirAdminDN =
                         (HtmlTextInput)form.getInputByName("DS_DIRMGRDN");
                 txtDirAdminDN.setValueAttribute((String)map.
                         get(TestConstants.KEY_ATT_DS_DIRMGRDN));
                 
                 HtmlPasswordInput txtDirAdminPassword =
                         (HtmlPasswordInput)form.
                         getInputByName("DS_DIRMGRPASSWD");
                 txtDirAdminPassword.setValueAttribute((String)map.
                         get(TestConstants.KEY_ATT_DS_DIRMGRPASSWD));
                 
                 HtmlCheckBoxInput chkLoadUMS =
                         (HtmlCheckBoxInput)form.getInputByName("DS_UM_SCHEMA");
                 if (((String)map.get(TestConstants.KEY_ATT_LOAD_UMS)).
                         equals("yes"))
                     chkLoadUMS.setChecked(true);
                 else
                     chkLoadUMS.setChecked(false);
             }
             try {
                 page = (HtmlPage)form.submit();
                 log(Level.FINEST, "configureProduct", "Returned Page:" +
                         page.asXml());
             } catch (com.gargoylesoftware.htmlunit.ScriptException e) {
                 log(Level.SEVERE, "configureProduct", e.getMessage(), null);
                 e.printStackTrace();
                 throw e;
             }
             strURL = url + "?" + "IDToken1=" + adminUser + "&IDToken2=" +
                     map.get(TestConstants.KEY_ATT_AMADMIN_PASSWORD);
             log(Level.INFO, "configureProduct", "strURL:" + strURL);
             url = new URL(strURL);
             page = (HtmlPage)webclient.getPage(url);
             if ((getHtmlPageStringIndex(page, "Authentication Failed") != -1) ||
                     (getHtmlPageStringIndex(page, "configurator.jsp") != -1)) {
                 log(Level.INFO, "configureProduct",
                         "Product Configuration was" +
                         " not successfull. Configuration failed.");
                 strURL = (String)map.get("serverurl") +
                         (String)map.get("serveruri") + "/UI/Logout";
                 consoleLogout(webclient, strURL);
                 exiting("configureProduct");
                 return false;
             } else {
                 log(Level.INFO, "configureProduct",
                         "Product Configuration was" +
                         " successfull. New bits were successfully configured.");
                 exiting("configureProduct");
                 return true;
             }
         } else {
             strURL = url + "?" + "IDToken1=" + adminUser + "&IDToken2=" +
                     map.get(TestConstants.KEY_ATT_AMADMIN_PASSWORD);
             log(Level.INFO, "configureProduct", "url:" + strURL);
             url = new URL(strURL);
             page = (HtmlPage)webclient.getPage(url);
             if (getHtmlPageStringIndex(page, "Authentication Failed") != -1) {
                 log(Level.INFO, "configureProduct", "Product was already" +
                         " configured. Super admin login failed.");
                 strURL = (String)map.get("serverurl") +
                         (String)map.get("serveruri") + "/UI/Logout";
                 consoleLogout(webclient, strURL);
                 exiting("configureProduct");
                 return false;
             } else {
                 log(Level.INFO, "configureProduct", "Product was already" +
                         " configured. Super admin login successfull.");
                 exiting("configureProduct");
                 return true;
             }
         }
     }
     
     /**
      * Logout from admin console using htmlunit
      */
     protected void consoleLogout(
             WebClient webclient,
             String amUrl)
     throws Exception {
         entering("consoleLogout", null);
         log(Level.FINEST, "consoleLogout", "JavaScript Enabled:" +
                 webclient.isJavaScriptEnabled());
         log(Level.FINEST, "consoleLogout", "Redirect Enabled:" +
                 webclient.isRedirectEnabled());
         URL url = new URL(amUrl);
         HtmlPage page = (HtmlPage)webclient.getPage(amUrl);
         log(Level.FINEST, "consoleLogout", page.getTitleText());
         exiting("consoleLogout");
     }
     
     /**
      * Checks whether the string exists on the page
      */
     protected int getHtmlPageStringIndex(
             HtmlPage page,
             String searchStr)
     throws Exception {
         entering("getHtmlPageStringIndex", null);
         String strPage = page.asXml();
         log(Level.FINEST, "getHtmlPageStringIndex", "Search string:" +
                 searchStr);
         log(Level.FINEST, "getHtmlPageStringIndex", "Search page\n:" +
                 strPage);
         int iIdx = strPage.indexOf(searchStr);
         if (iIdx != -1)
             log(Level.FINEST, "getHtmlPageStringIndex",
                     "Search string found on page:" + iIdx);
         else
             log(Level.FINEST, "getHtmlPageStringIndex",
                     "Search string not found on page:" + iIdx);
         exiting("getHtmlPageStringIndex");
         return iIdx;
     }
     
     /**
      * Reads data from a Map object, creates a new file and writes data to that
      * file
      */
     protected void createFileFromMap(Map properties, String fileName)
     throws Exception {
         log(Level.FINEST, "createFileFromMap", "Map:" + properties);
         log(Level.FINEST, "createFileFromMap", "fileName:" + fileName);
         StringBuffer buff = new StringBuffer();
         for (Iterator i = properties.entrySet().iterator(); i.hasNext(); ) {
             Map.Entry entry = (Map.Entry)i.next();
             String valueString = entry.getValue().toString();
             buff.append(entry.getKey())
             .append("=")
             .append(valueString.substring(1, valueString.length()-1))
             .append("\n");
         }
         
         BufferedWriter out = new BufferedWriter(new FileWriter(
                 fileName));
         out.write(buff.toString());
         out.close();
     }
     
     /**
      * Reads data from a ResourceBundle object and creates a Map containing all
      * the attribute keys and values. It also takes in a Set of attribute key
      * names, which if specified, are not put into the Map. This is to ensure
      * selective selection of attribute key and value pairs.
      */
     protected Map getMapFromResourceBundle(String rbName, Set set)
     throws Exception {
         Map map = new HashMap();
         ResourceBundle rb = ResourceBundle.getBundle(rbName);
         for (Enumeration e = rb.getKeys(); e.hasMoreElements(); ) {
             String key = (String)e.nextElement();
             String value = (String)rb.getString(key);
             if (set != null) {
                 if (!set.contains(key))
                     map.put(key, value);
             } else {
                 map.put(key, value);
             }
         }
         return (map);
     }
     
     /**
      * Reads data from a ResourceBundle object and creates a Map containing all
      * the attribute keys and values.
      * @param resourcebundle name
      */
     protected Map getMapFromResourceBundle(String rbName)
     throws Exception {
         Map map = getMapFromResourceBundle(rbName, null);
         return (map);
     }
     
     /**
      * Returns a map of String to Set of String from a formatted string.
      * The format is
      * <pre>
      * &lt;key1&gt;=&lt;value11&gt;,&lt;value12&gt;...,&lt;value13&gt;;
      * &lt;key2&gt;=&lt;value21&gt;,&lt;value22&gt;...,&lt;value23&gt;; ...
      * &lt;keyn&gt;=&lt;valuen1&gt;,&lt;valuen2&gt;...,&lt;valuen3&gt;
      * </pre>
      */
     protected static Map<String, Set<String>> parseStringToMap(String str) {
         Map<String, Set<String>> map = new HashMap<String, Set<String>>();
         StringTokenizer st = new StringTokenizer(str, ";");
         while (st.hasMoreTokens()) {
             String token = st.nextToken();
             int idx = token.indexOf("=");
             if (idx != -1) {
                 Set<String> set = new HashSet<String>();
                 map.put(token.substring(0, idx).trim(), set);
                 StringTokenizer st1 = new StringTokenizer(
                         token.substring(idx+1), ",");
                 while (st1.hasMoreTokens()) {
                     set.add(st1.nextToken().trim());
                 }
             }
         }
         return map;
     }
     
     /**
      * Returns set of string. This is a convenient method for adding a set of
      * string into a map. In this project, we usually have the
      * <code>Map&lt;String, Set&lt;String&gt;&gt; and many times, we just
      * want to add a string to the map.
      */
     protected static Set<String> putSetIntoMap(
             String key,
             Map<String, Set<String>> map,
             String value
             ) {
         Set<String> set = new HashSet<String>();
         set.add(value);
         map.put(key, set);
         return set;
     }
     
     /**
      * Returns LoginURL based on the realm under test
      * @param realm
      * @return loginURL
      */
     protected static String getLoginURL(String strOrg){
         String loginURL;
         if ((strOrg.equals("")) || (strOrg.equalsIgnoreCase("/"))) {
             loginURL = protocol + ":" + "//" + host + ":" + port + uri
                     + "/UI/Login";
         } else {
             loginURL = protocol + ":" + "//" + host + ":" + port + uri
                     + "/UI/Login" + "?org=" + strOrg ;
         }
         return loginURL;
     }
     
     /**
      * Returns the List for the given tokens
      * @param string tokens
      * @return list of the tokens
      */
     protected List getListFromTokens(StringTokenizer strTokens){
         List<String> list = new ArrayList<String>();
         while (strTokens.hasMoreTokens()) {
             list.add(strTokens.nextToken());
         }
         return list;
     }
     
     /*
      * Gets the baseDirectory to create the XML files
      */
     protected String getTestBase()
     throws Exception {
         String testbaseDir = null;
         ResourceBundle rbamconfig = ResourceBundle.getBundle(
                     TestConstants.TEST_PROPERTY_AMCONFIG);
         testbaseDir = getBaseDir() + fileseparator
         + rbamconfig.getString(TestConstants.KEY_ATT_SERVER_NAME)
         + fileseparator + "built"
         + fileseparator + "classes"
         + fileseparator ;
 
         return (testbaseDir);   
     }
 
     /**
      * Takes a token separated string and returns each individual
      * token as part of a list.
      */
     public List getAttributeList(String strList, String token) 
     throws Exception {
         StringTokenizer stk = new StringTokenizer(strList, token);
         List<String> attList = new ArrayList<String>();
         while (stk.hasMoreTokens()) {
             attList.add(stk.nextToken());
         }
         return (attList);
     }
 
     /**
      * Takes a token separated string and returns each individual
      * token as part of a Map.
      */
     public Map getAttributeMap(String strList, String token)
     throws Exception {
         StringTokenizer stk = new StringTokenizer(strList, token);
         Map map = new HashMap();
         int idx;
         String strToken;
         while (stk.hasMoreTokens()) {
             strToken = stk.nextToken();
             idx = strToken.indexOf("=");
            map.put(strToken.substring(0, idx-1), strToken.substring(idx,
                     strToken.length()));
         }
         log(Level.FINEST, "getAttributeMap", map);
         return (map);
     }
 
     /**
      * Returns set of string. This is a convenient method for adding multipe set
      * of string into a map. The value contains the multiple string sepearete by
      * token string.
      */
     protected static Set<String> putSetIntoMap(
             String key,
             Map<String, Set<String>> map,
             String value,
             String token
             )
     throws Exception {
         StringTokenizer stk = new StringTokenizer(value, token);
         Set<String> setValue = new HashSet<String>();
         while (stk.hasMoreTokens()) {
             setValue.add(stk.nextToken());
         }
         map.put(key, setValue);
         return setValue;
     }
 
     /**
      * Concatenates second set to a first set
      * @param set1 first set
      * @param set2 second set to be concatenated with the first set
      */
     protected static void concatSet(Set set1, Set set2)
     throws Exception {
         Iterator keyIter = set2.iterator();
         String item;
         while (keyIter.hasNext()) {
             item = (String)keyIter.next();
             set1.add(item);
         }
     }
 }
