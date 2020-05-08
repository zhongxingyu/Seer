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
 * $Id: ServicesDefaultValues.java,v 1.22 2008-01-24 19:58:40 goodearth Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.sun.identity.common.DNUtils;
 import com.sun.identity.shared.encode.Hash;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.util.Crypt;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import netscape.ldap.util.DN;
 
 /**
  * This class holds the default values of service schema.
  */
 public class ServicesDefaultValues {
     private static ServicesDefaultValues instance = new ServicesDefaultValues();
     private static Set preappendSlash = new HashSet();
     private static Set trimSlash = new HashSet();
     private Map defValues = new HashMap();
     
     static {
         preappendSlash.add(SetupConstants.CONFIG_VAR_PRODUCT_NAME);
         preappendSlash.add(SetupConstants.CONFIG_VAR_OLD_CONSOLE_URI);
         preappendSlash.add(SetupConstants.CONFIG_VAR_CONSOLE_URI);
         preappendSlash.add(SetupConstants.CONFIG_VAR_SERVER_URI);
         trimSlash.add(SetupConstants.CONFIG_VAR_CONSOLE_URI);
         trimSlash.add(SetupConstants.CONFIG_VAR_SERVER_URI);
     }
 
     private ServicesDefaultValues() {
         ResourceBundle bundle = ResourceBundle.getBundle(
             "serviceDefaultValues");
         Enumeration e = bundle.getKeys();
         while (e.hasMoreElements()) {
             String key = (String)e.nextElement();
             defValues.put(key, bundle.getString(key));
         }
     }
 
     /**
      * This method validates the form fields and populates the
      * map with valid values.
      *
      * @param request is the Servlet Request.
      */
     public static void setServiceConfigValues(
         IHttpServletRequest request
     ) {
         Locale locale = (Locale)request.getLocale();
         Map map = instance.defValues;
         map.putAll(request.getParameterMap());
         
         String base = (String)map.get(
             SetupConstants.CONFIG_VAR_BASE_DIR);
         base = base.replace('\\', '/');
         map.put(SetupConstants.CONFIG_VAR_BASE_DIR, base);
 
         if (!isEncryptionKeyValid()){
             throw new ConfiguratorException("configurator.encryptkey",
                 null, locale);
         }
         validatePassword(locale);
         if (!isServiceURLValid()) {
             throw new ConfiguratorException("configurator.invalidhostname",
                 null, locale);
         }
 
         String cookieDomain = (String)map.get(
             SetupConstants.CONFIG_VAR_COOKIE_DOMAIN);
         if (!isCookieDomainValid(cookieDomain)) { 
             throw new ConfiguratorException("configurator.invalidcookiedomain", 
                 null, locale); 
         }
 
         setDeployURI(request.getContextPath(), map);
 
         String hostname = (String)map.get(
             SetupConstants.CONFIG_VAR_SERVER_HOST);
         map.put(SetupConstants.CONFIG_VAR_COOKIE_DOMAIN, 
             getCookieDomain(cookieDomain, hostname));
         setPlatformLocale();
         
         String dbOption = (String)map.get(SetupConstants.CONFIG_VAR_DATA_STORE);
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
             AMSetupDSConfig dsConfig = AMSetupDSConfig.getInstance();
             dsConfig.setDSValues();
 
             //try to connect to the DS with the supplied host/port
             if (embedded ==  false) {
                 if (!embedded && !dsConfig.isDServerUp()) {
                     dsConfig = null;
                     throw new ConfiguratorException(
                         "configurator.dsconnnectfailure", null, locale);
                 }
                 if ((!DN.isDN((String) map.get(
                     SetupConstants.CONFIG_VAR_ROOT_SUFFIX))) ||
                         (!dsConfig.connectDSwithDN())
                 ) {
                     dsConfig = null;
                     throw new ConfiguratorException("configurator.invalidsuffix",
                         null, locale);
                 }
                 String dbName = dsConfig.getDBName();
                 if ((dbName != null) && (dbName.length() > 0)) {
                     map.put(SetupConstants.DB_NAME, dbName);
                 }
                 
                 if (dbSunDS) {
                     map.put(SetupConstants.DIT_LOADED, dsConfig.isDITLoaded());
                     map.put(SetupConstants.DATASTORE_NOTIFICATION, "true");
                     map.put(SetupConstants.DISABLE_PERSISTENT_SEARCH, "");
                     boolean loadSDKSchema = ((String)map.get(
                         SetupConstants.CONFIG_VAR_DS_UM_SCHEMA))
                             .equals("sdkSchema");
                     if (!loadSDKSchema) {
                         map.put(SetupConstants.XML_COMMENT_START, "<!--");
                         map.put(SetupConstants.XML_COMMENT_END, "-->");
                     } else {
                         map.put(SetupConstants.XML_COMMENT_START, "");
                         map.put(SetupConstants.XML_COMMENT_END, "");
                     }
                 } else {
                     map.put(SetupConstants.DATASTORE_NOTIFICATION, "false");
                     map.put(SetupConstants.DISABLE_PERSISTENT_SEARCH, 
                         "aci,um,sm");
                     map.put(SetupConstants.XML_COMMENT_START, "<!--");
                     map.put(SetupConstants.XML_COMMENT_END, "-->");
                 }
             } else { 
                 map.put(SetupConstants.DATASTORE_NOTIFICATION, "false");
                 map.put(SetupConstants.DISABLE_PERSISTENT_SEARCH, "aci,um,sm");
                 map.put(SetupConstants.XML_COMMENT_START, "<!--");
                 map.put(SetupConstants.XML_COMMENT_END, "-->");
             }
         } else {
             map.put(SetupConstants.DATASTORE_NOTIFICATION, "true");
             map.put(SetupConstants.XML_COMMENT_START, "");
             map.put(SetupConstants.XML_COMMENT_END, "");
         }
     }
 
     /**
      * Set the platform locale.
      */
     private static void setPlatformLocale() {
         Map map = instance.defValues;
         String locale = (String)map.get(
             SetupConstants.CONFIG_VAR_PLATFORM_LOCALE);
         if (locale == null) {
             map.put(SetupConstants.CONFIG_VAR_PLATFORM_LOCALE, 
                 SetupConstants.DEFAULT_PLATFORM_LOCALE);
         }
     }
 
    /**
      * Validates serverURL.
      *
      * @return <code>true</code> if service URL is valid.
      */
     private static boolean isServiceURLValid() {
         String protocol = "http";
         String port = "80";
         String hostName;
         Map map = instance.defValues;
         String hostURL = (String)map.get(SetupConstants.CONFIG_VAR_SERVER_URL);
         boolean valid = (hostURL != null) && (hostURL.length() > 0);
         try {
             if (valid) {
                 if ((hostURL.indexOf("http", 0) == -1) &&
                     (hostURL.indexOf("https", 0) == -1)) {
                     int idx = hostURL.lastIndexOf(":");
                     if ((idx != -1)) {
                         port = hostURL.substring(idx + 1);
                         hostName = hostURL.substring(0, idx);
                     } else {
                         hostName = hostURL;
                     }
                     if (port.equals("443")) {
                         protocol = "https";
                     }
                 } else {
                     URL serverURL = new URL(hostURL);
                     int intPort = serverURL.getPort();
                     protocol = serverURL.getProtocol();
                     if (intPort < 0) {
                         if (protocol.equalsIgnoreCase("https")) {
                             port = "443";
                         }
                     } else {
                         port = Integer.toString(intPort);
                     }
                     hostName = serverURL.getHost();
                 }
                 if (isHostnameValid(hostName)) {
                     map.put(SetupConstants.CONFIG_VAR_SERVER_HOST, hostName);
                     map.put(SetupConstants.CONFIG_VAR_SERVER_PROTO, protocol);
                     map.put(SetupConstants.CONFIG_VAR_SERVER_PORT, port);
                     map.put(SetupConstants.CONFIG_VAR_SERVER_URL, 
                         protocol + "://" + hostName + ":" + port);
                 } else {
                     valid = false;
                 }
             }
         } catch (MalformedURLException mue){
            valid = false;
         }
         return valid;
     }
 
     /*
      * valid: localhost (no period)
      * valid: abc.sun.com (two periods)
      *
      * @param hostname is the user specified host name.
      * @return <code>true</code> if syntax for host is correct.
      */
     private static boolean isHostnameValid(String hostname) {
         boolean valid = (hostname != null) && (hostname.length() > 0);
         if (valid) {
             int idx = hostname.lastIndexOf(".");
             if ((idx != -1) && (idx != (hostname.length() -1))) {
                 int idx1 = hostname.lastIndexOf(".", idx-1);
                 valid = (idx1 != -1) && (idx1 < (idx -1));
             }
         }
         return valid;
     }
 
     /**
      * Validates if cookie Domain is syntactically correct.
      *
      * @param cookieDomain is the user specified cookie domain.
      * @return <code>true</code> if syntax for cookie domain is correct.
      */
     private static boolean isCookieDomainValid(String cookieDomain) {
         boolean valid = (cookieDomain == null) || (cookieDomain.length() == 0);
 
         if (!valid) {
             int idx1 = cookieDomain.lastIndexOf(".");
 
             // need to have a period and cannot be the last char.
             valid = (idx1 == -1) || (idx1 != (cookieDomain.length() -1));
 
             if (valid) {
                 int idx2 = cookieDomain.lastIndexOf(".", idx1-1);
                 /*
                  * need to be have a period before the last one e.g.
                  * .sun.com and cannot be ..com
                  */
                 valid = (idx2 != -1) && (idx2 < (idx1 -1));
             }
         }
         return valid;
     }
 
     /**
      * Returns the cookie Domain based on the hostname.
      *
      * @param cookieDomain is the user specified cookie domain.
      * @param hostname is the host for which the cookie domain is set.
      * @return cookieDomain containing the valid cookie domain for
      *         the specified hostname.
      */
     private static String getCookieDomain(
         String cookieDomain,
         String hostname
     ) {
         int idx = hostname.lastIndexOf(".");
         if ((idx == -1) || (idx == (hostname.length() -1)) ||
             isIPAddress(hostname)
         ) {
             cookieDomain = "";
         } else if ((cookieDomain == null) || (cookieDomain.length() == 0)) {
             // try to determine the cookie domain if it is not set
             String topLevelDomain = hostname.substring(idx+1);
             int idx2 = hostname.lastIndexOf(".", idx-1);
 
             if ((idx2 != -1) && (idx2 < (idx -1))) {
                 cookieDomain = hostname.substring(idx2);
             }
         }
         return cookieDomain;
     }
 
     /**
      * Validates if the hostname is IP address.
      *
      * @param hostname is the user specified hostname.
      * @return <code>true</code> if hostname is an IP Address.
      */
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
      * Validates the encryption key.
      *
      * @return <code>true</code> if ecryption key is valid.
      */
     private static boolean isEncryptionKeyValid() {
         Map map = instance.defValues;
         String ekey = ((String)map.get(
             SetupConstants.CONFIG_VAR_ENCRYPTION_KEY));
         if (ekey == null) {
             ekey = AMSetupServlet.getRandomString().trim();
             map.put(SetupConstants.CONFIG_VAR_ENCRYPTION_KEY, ekey);
         }
         return ((ekey != null) && (ekey.length() > 10)) ? true : false;
     }
 
     /**
      * Validates Admin passwords.
      */
     private static void validatePassword(Locale locale) {
         Map map = instance.defValues;
         String adminPwd = ((String)map.get(
             SetupConstants.CONFIG_VAR_ADMIN_PWD)).trim();
         String confirmAdminPwd = ((String)map.get(
             SetupConstants.CONFIG_VAR_CONFIRM_ADMIN_PWD)).trim();
         if (isPasswordValid(adminPwd, confirmAdminPwd, locale)) {
             SystemProperties.initializeProperties(
                 SetupConstants.ENC_PWD_PROPERTY, (((String) map.get(
                     SetupConstants.CONFIG_VAR_ENCRYPTION_KEY)).trim()));
             Crypt.reinitialize();
             map.put(SetupConstants.HASH_ADMIN_PWD, (String)Hash.hash(adminPwd));
         }
 
         String urlAccessAgentPwd = ((String)map.get(
             SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD)).trim();
         String urlAccessAgentPwdConfirm = ((String)map.get(
             SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD_CONFIRM)).trim();
         validateURLAccessAgentPassword(adminPwd, urlAccessAgentPwd,
             urlAccessAgentPwdConfirm, locale);
 
         String dbOption = (String)map.get(SetupConstants.CONFIG_VAR_DATA_STORE);
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
             String dsMgrPwd = ((String)map.get(
                 SetupConstants.CONFIG_VAR_DS_MGR_PWD)).trim();
             if (!embedded || embedded && dsMgrPwd.length() != 0 ) {
                 // Quick install : if embedded make passwd same as admin passwd
                 adminPwd = dsMgrPwd ; 
             } else {
                 map.put( SetupConstants.CONFIG_VAR_DS_MGR_PWD, adminPwd );
             }
             if (adminPwd != null) {
                 map.put(SetupConstants.CONFIG_VAR_ADMIN_PWD, adminPwd);
             }
         }
         String encryptAdminPwd = Crypt.encrypt(adminPwd);
         String ldapUserPwd = ((String)map.get(
             SetupConstants.LDAP_USER_PWD)).trim();
 
         map.put(SetupConstants.ENCRYPTED_LDAP_USER_PWD, 
             (String)Crypt.encrypt(ldapUserPwd));
         map.put(SetupConstants.HASH_LDAP_USER_PWD, 
             (String)Hash.hash(ldapUserPwd));
         map.put(SetupConstants.SSHA512_LDAP_USERPWD, 
             (String)EmbeddedOpenDS.hash(adminPwd));
 
         map.put(SetupConstants.ENCRYPTED_ADMIN_PWD, encryptAdminPwd);
         map.put(SetupConstants.ENCRYPTED_AD_ADMIN_PWD, encryptAdminPwd);
         map.remove(SetupConstants.CONFIG_VAR_CONFIRM_ADMIN_PWD);
         map.remove(SetupConstants.CONFIG_VAR_AMLDAPUSERPASSWD_CONFIRM);
     }
 
 
     /*
      * valid: password greater than 8 characters
      * valid: password and confirm passwords match
      *
      * @param pwd  is the Admin password.
      * @param cPwd is the confirm Admin password.
      * @param locale Locale of the HTTP Request.
      * @return <code>true</code> if password is valid.
      */
     private static boolean isPasswordValid(
         String pwd, 
         String cPwd, 
         Locale locale
     ) {
         if ((pwd != null) && (pwd.length() > 7)) {
             if (!pwd.equals(cPwd)) {
                  throw new ConfiguratorException("configurator.nopasswdmatch",
                      null, locale);
             }
         } else {
              throw new ConfiguratorException("configurator.passwdlength",
                  null, locale);
         }
         return true;
     }
 
     private static boolean validateURLAccessAgentPassword(
         String amadminPwd,
         String pwd,
         String cPwd,
         Locale locale
     ) {
         if ((pwd != null) && (pwd.length() > 7)) {
             if (!pwd.equals(cPwd)) {
                 throw new ConfiguratorException(
                     "configurator.urlaccessagent.passwd.nomatch", null, locale);
             }
 
             if (amadminPwd.equals(pwd)) {
                 throw new ConfiguratorException(
                     "configurator.urlaccessagent.passwd.match.amadmin.pwd",
                     null, locale);
             }
         } else {
             throw new ConfiguratorException("configurator.passwdlength",
                 null, locale);
         }
         return true;
     }
 
     /**
      * Returns the map of default attribute name to its value.
      *
      * @return the map of default attribute name to its value.
      */
     public static Map getDefaultValues() {
         return instance.defValues;
     }
     
     /**
      * Set the deploy URI.
      *
      * @param deployURI Deploy URI.
      * @param map Service attribute values.
      */
     public static void setDeployURI(String deployURI, Map map) {
         map.put(SetupConstants.CONFIG_VAR_PRODUCT_NAME, deployURI);
         map.put(SetupConstants.CONFIG_VAR_OLD_CONSOLE_URI, deployURI);
         map.put(SetupConstants.CONFIG_VAR_CONSOLE_URI, deployURI);
         map.put(SetupConstants.CONFIG_VAR_SERVER_URI, deployURI);
     }
 
     /**
      * Returns the tag swapped string.
      *
      * @param orig String to be tag swapped.
      * @return the tag swapped string.
      */
     public static String tagSwap(String orig) {
         Map map = instance.defValues;
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             String value = (String)map.get(key);
             
             if (preappendSlash.contains(key)) {
                 orig = orig.replaceAll("/@" + key + "@", value);
                 
                 if (trimSlash.contains(key)) {
                     orig = orig.replaceAll("@" + key + "@", value.substring(1));
                 }
             } else if (key.equals(SetupConstants.CONFIG_VAR_ROOT_SUFFIX)) {
                 String normalized = DNUtils.normalizeDN(value);
                 orig = orig.replaceAll(
                     "@" + SetupConstants.SM_ROOT_SUFFIX_HAT + "@",
                     normalized.replaceAll(",", "^"));
                 String rfced = (new DN(value)).toRFCString();
                 orig = orig.replaceAll(
                     "@" + SetupConstants.CONFIG_VAR_ROOT_SUFFIX + "@", rfced);
             } else {
                 orig = orig.replaceAll("@" + key + "@", value);
             }
         }
         return orig;
     }
 }
