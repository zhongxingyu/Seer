 /*
  * Password Management Servlets (PWM)
  * http://code.google.com/p/pwm/
  *
  * Copyright (c) 2006-2009 Novell, Inc.
  * Copyright (c) 2009-2010 The PWM Project
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package password.pwm;
 
 import com.novell.ldapchai.ChaiConstant;
 import com.novell.ldapchai.ChaiFactory;
 import com.novell.ldapchai.ChaiUser;
 import com.novell.ldapchai.cr.CrSetting;
 import com.novell.ldapchai.exception.ChaiOperationException;
 import com.novell.ldapchai.exception.ChaiUnavailableException;
 import com.novell.ldapchai.impl.edir.entry.EdirEntries;
 import com.novell.ldapchai.provider.ChaiConfiguration;
 import com.novell.ldapchai.provider.ChaiProvider;
 import com.novell.ldapchai.provider.ChaiProviderFactory;
 import com.novell.ldapchai.provider.ChaiSetting;
 import com.novell.ldapchai.util.SearchHelper;
 import password.pwm.bean.SessionStateBean;
 import password.pwm.config.Configuration;
 import password.pwm.config.FormConfiguration;
 import password.pwm.config.Message;
 import password.pwm.config.PwmSetting;
 import password.pwm.error.ErrorInformation;
 import password.pwm.error.PwmError;
 import password.pwm.error.PwmException;
 import password.pwm.util.PwmLogger;
 import password.pwm.util.PwmRandom;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.NumberFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * A collection of static methods used throughout PWM
  *
  * @author Jason D. Rivard
  */
 public class Helper {
 // ------------------------------ FIELDS ------------------------------
 
     private static final PwmLogger LOGGER = PwmLogger.getLogger(Helper.class);
 
     // -------------------------- STATIC METHODS --------------------------
 
     private Helper() {
     }
 
     public static ChaiProvider createChaiProvider(
             final Configuration config,
             final String userDN,
             final String userPassword,
             final int idleTimeoutMs
     )
             throws ChaiUnavailableException {
         final ChaiConfiguration chaiConfig = createChaiConfiguration(config, userDN, userPassword, idleTimeoutMs);
         LOGGER.trace("creating new chai provider using config of " + chaiConfig.toString());
         return ChaiProviderFactory.createProvider(chaiConfig);
     }
 
     private static ChaiConfiguration createChaiConfiguration(
             final Configuration config,
             final String userDN,
             final String userPassword,
             final int idleTimeoutMs
     )
             throws ChaiUnavailableException {
         final List<String> ldapURLs = config.readStringArraySetting(PwmSetting.LDAP_SERVER_URLS);
 
         final ChaiConfiguration chaiConfig = new ChaiConfiguration(ldapURLs, userDN, userPassword);
 
         chaiConfig.setSetting(ChaiSetting.PROMISCUOUS_SSL, Boolean.toString(config.readSettingAsBoolean(PwmSetting.LDAP_PROMISCUOUS_SSL)));
         chaiConfig.setSetting(ChaiSetting.EDIRECTORY_ENABLE_NMAS, Boolean.toString(config.readSettingAsBoolean(PwmSetting.EDIRECTORY_ENABLE_NMAS)));
 
         chaiConfig.setCrSetting(CrSetting.CHAI_ATTRIBUTE_NAME, config.readSettingAsString(PwmSetting.CHALLENGE_USER_ATTRIBUTE));
         chaiConfig.setCrSetting(CrSetting.ALLOW_DUPLICATE_RESPONSES, Boolean.toString(config.readSettingAsBoolean(PwmSetting.CHALLENGE_ALLOW_DUPLICATE_RESPONSES)));
         chaiConfig.setCrSetting(CrSetting.CHAI_CASE_INSENSITIVE, Boolean.toString(config.readSettingAsBoolean(PwmSetting.CHALLENGE_CASE_INSENSITIVE)));
 
         // if possible, set the ldap timeout.
         if (idleTimeoutMs > 0) {
             chaiConfig.setSetting(ChaiSetting.WATCHDOG_ENABLE, "true");
             chaiConfig.setSetting(ChaiSetting.WATCHDOG_IDLE_TIMEOUT, Long.toString(idleTimeoutMs));
             chaiConfig.setSetting(ChaiSetting.WATCHDOG_CHECK_FREQUENCY, Long.toString(60 * 1000));
         } else {
             chaiConfig.setSetting(ChaiSetting.WATCHDOG_ENABLE, "false");
         }
 
         // write out any configured values;
         final List<String> rawValues = config.readStringArraySetting(PwmSetting.LDAP_CHAI_SETTINGS);
         final Map<String, String> configuredSettings = Configuration.convertStringListToNameValuePair(rawValues, "=");
         for (final String key : configuredSettings.keySet()) {
             final ChaiSetting theSetting = ChaiSetting.forKey(key);
             if (theSetting == null) {
                 LOGGER.error("ignoring unknown chai setting '" + key + "'");
             } else {
                 chaiConfig.setSetting(theSetting, configuredSettings.get(key));
             }
         }
 
         return chaiConfig;
     }
 
     public static void addLdapGuidValue(
             final PwmSession pwmSession
     )
             throws ChaiUnavailableException {
         {
             final String currentGuid = pwmSession.getUserInfoBean().getUserGuid();
             if (currentGuid != null && currentGuid.length() > 1) {
                 return;
             }
         }
 
         if (!pwmSession.getConfig().readSettingAsBoolean(PwmSetting.LDAP_GUID_AUTO_ADD)) {
             LOGGER.warn(pwmSession, "user " + pwmSession.getUserInfoBean().getUserDN() + " does not have a valid GUID");
             return;
         }
 
         final String GUIDattributeName = pwmSession.getConfig().readSettingAsString(PwmSetting.LDAP_GUID_ATTRIBUTE);
         LOGGER.trace(pwmSession, "assigning new GUID to user " + pwmSession.getUserInfoBean().getUserDN());
 
         final ChaiProvider proxyChaiProvider = pwmSession.getContextManager().getProxyChaiProvider();
         final String baseContext = pwmSession.getConfig().readSettingAsString(PwmSetting.LDAP_CONTEXTLESS_ROOT);
         int attempts = 0;
         while (attempts < 10) {
             final String newGUID;
             {
                 final StringBuilder guidValue = new StringBuilder();
                 guidValue.append(Long.toHexString(System.currentTimeMillis()).toUpperCase());
                 while (guidValue.length() < 12) {
                     guidValue.insert(0, "0");
                 }
                 guidValue.insert(0, PwmRandom.getInstance().alphaNumericString(20).toUpperCase());
                 newGUID = guidValue.toString();
             }
             final SearchHelper searchHelper = new SearchHelper(ChaiProvider.SEARCH_SCOPE.SUBTREE);
             searchHelper.setFilter(GUIDattributeName, newGUID);
             try {
                 final Map<String, Properties> result = proxyChaiProvider.search(baseContext, searchHelper);
                 if (result.isEmpty()) {
                     try {
                         pwmSession.getContextManager().getProxyChaiUserActor(pwmSession).addAttribute(GUIDattributeName, newGUID);
                         pwmSession.getUserInfoBean().getAllUserAttributes().setProperty(GUIDattributeName, newGUID);
                         LOGGER.info(pwmSession, "added GUID value '" + newGUID + "' to user " + pwmSession.getUserInfoBean().getUserDN());
                         return;
                     } catch (PwmException e) {
                        LOGGER.error(pwmSession, "error writing GUID value to user: " + e.getMessage(), e);
                     }
                 }
             } catch (ChaiOperationException e) {
                LOGGER.error(pwmSession, "unexpected error while searching GUID for uniqueness: " + e.getMessage(), e);
             }
             attempts++;
         }
     }
 
     /**
      * Append auxClasses    configured in the PWM configuration to the ldap user object.
      *
      * @param userDN     userDN userDN of the user to add to
      * @param pwmSession Current pwmSession, used for logging
      * @throws com.novell.ldapchai.exception.ChaiUnavailableException
      *          if the ldap server is unavailable
      */
     public static void addConfiguredUserObjectClass(
             final String userDN,
             final PwmSession pwmSession
     )
             throws ChaiUnavailableException {
         final Set<String> newObjClasses = new HashSet<String>(pwmSession.getConfig().readStringArraySetting(PwmSetting.AUTO_ADD_OBJECT_CLASSES));
         if (newObjClasses.isEmpty()) {
             return;
         }
         final ChaiUser theUser = ChaiFactory.createChaiUser(userDN, pwmSession.getContextManager().getProxyChaiProvider());
         addUserObjectClass(theUser, newObjClasses, pwmSession);
     }
 
     private static void addUserObjectClass(final ChaiUser theUser, final Set<String> newObjClasses, final PwmSession pwmSession)
             throws ChaiUnavailableException {
         String auxClass = null;
         try {
             final Set<String> existingObjClasses = theUser.readMultiStringAttribute(ChaiConstant.ATTR_LDAP_OBJECTCLASS);
             newObjClasses.removeAll(existingObjClasses);
 
             for (final String newObjClass : newObjClasses) {
                 auxClass = newObjClass;
                 theUser.addAttribute(ChaiConstant.ATTR_LDAP_OBJECTCLASS, auxClass);
                 LOGGER.info(pwmSession, "added objectclass '" + auxClass + "' to user " + theUser.getEntryDN());
             }
         } catch (ChaiOperationException e) {
             final StringBuilder errorMsg = new StringBuilder();
 
             errorMsg.append("error adding objectclass '").append(auxClass).append("' to user ");
             errorMsg.append(theUser.getEntryDN());
             errorMsg.append(": ");
             errorMsg.append(e.toString());
 
             LOGGER.error(pwmSession, errorMsg.toString());
         }
     }
 
     /**
      * Wrapper for {@link #forwardToErrorPage(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext, boolean)} )}
      * with forceLogout=true;
      *
      * @param req        Users http request
      * @param resp       Users http response
      * @param theContext The Servlet context
      * @throws IOException      if there is an error writing to the response
      * @throws ServletException if there is a problem accessing the http objects
      */
     public static void forwardToErrorPage(
             final HttpServletRequest req,
             final HttpServletResponse resp,
             final ServletContext theContext
     )
             throws IOException, ServletException {
         forwardToErrorPage(req, resp, theContext, true);
     }
 
     /**
      * Forwards the user to the error page.  Callers to this method should populate the sesssion bean's
      * session error state.  If the session error state is null, then this method will populate it
      * with a generic unknown error.
      *
      * @param req         Users http request
      * @param resp        Users http response
      * @param theContext  The Servlet context
      * @param forceLogout if the user should be unauthenticed after showing the error
      * @throws IOException      if there is an error writing to the response
      * @throws ServletException if there is a problem accessing the http objects
      */
     public static void forwardToErrorPage(
             final HttpServletRequest req,
             final HttpServletResponse resp,
             final ServletContext theContext,
             final boolean forceLogout
     )
             throws IOException, ServletException {
         final PwmSession pwmSession = PwmSession.getPwmSession(req);
         final SessionStateBean ssBean = pwmSession.getSessionStateBean();
 
         if (ssBean.getSessionError() == null) {
             ssBean.setSessionError(new ErrorInformation(PwmError.ERROR_UNKNOWN));
         }
 
         final String url = SessionFilter.rewriteURL('/' + PwmConstants.URL_JSP_ERROR, req, resp);
         theContext.getRequestDispatcher(url).forward(req, resp);
 
         if (forceLogout) {
             pwmSession.unauthenticateUser();
         }
     }
 
     public static void forwardToLoginPage(
             final HttpServletRequest req,
             final HttpServletResponse resp
     )
             throws IOException {
         final String loginServletURL = req.getContextPath() + "/private/" + PwmConstants.URL_SERVLET_LOGIN;
         resp.sendRedirect(SessionFilter.rewriteRedirectURL(loginServletURL, req, resp));
     }
 
     public static void forwardToOriginalRequestURL(
             final HttpServletRequest req,
             final HttpServletResponse resp
     )
             throws IOException {
         final PwmSession pwmSession = PwmSession.getPwmSession(req);
         final SessionStateBean ssBean = pwmSession.getSessionStateBean();
 
         String destURL = ssBean.getOriginalRequestURL();
 
         if (destURL == null || destURL.indexOf(PwmConstants.URL_SERVLET_LOGIN) != -1) { // fallback, shouldnt need to be used.
             destURL = req.getContextPath();
         }
 
         resp.sendRedirect(SessionFilter.rewriteRedirectURL(destURL, req, resp));
     }
 
     public static void forwardToSuccessPage(
             final HttpServletRequest req,
             final HttpServletResponse resp,
             final ServletContext theContext
     )
             throws IOException, ServletException {
         final SessionStateBean ssBean = PwmSession.getSessionStateBean(req.getSession());
 
         if (ssBean.getSessionSuccess() == null) {
             ssBean.setSessionSuccess(Message.SUCCESS_UNKNOWN);
         }
 
         final String url = SessionFilter.rewriteURL('/' + PwmConstants.URL_JSP_SUCCESS, req, resp);
         theContext.getRequestDispatcher(url).forward(req, resp);
     }
 
     public static void forwardToWaitPage(
             final HttpServletRequest req,
             final HttpServletResponse resp,
             final ServletContext theContext,
             final String nextURL
     )
             throws IOException, ServletException {
         final SessionStateBean ssBean = PwmSession.getSessionStateBean(req.getSession());
         ssBean.setPostWaitURL(SessionFilter.rewriteURL(nextURL, req, resp));
 
         final String url = SessionFilter.rewriteURL('/' + PwmConstants.URL_JSP_WAIT, req, resp);
         theContext.getRequestDispatcher(url).forward(req, resp);
     }
 
     public static String md5sum(final File theFile)
             throws IOException {
         return md5sum(new FileInputStream(theFile));
     }
 
     public static String md5sum(final InputStream is)
             throws IOException {
         final InputStream bis = is instanceof BufferedInputStream ? is : new BufferedInputStream(is);
 
         final MessageDigest messageDigest;
         try {
             messageDigest = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException e) {
             return null;
         }
 
         final byte[] buffer = new byte[1024];
         int length;
         while (true) {
             length = bis.read(buffer, 0, buffer.length);
             if (length == -1) {
                 break;
             }
             messageDigest.update(buffer, 0, length);
         }
         bis.close();
 
         final byte[] bytes = messageDigest.digest();
 
         return byteArrayToHexString(bytes);
     }
 
     /**
      * Convert a byte[] array to readable string format. This makes the "hex" readable
      *
      * @param in byte[] buffer to convert to string format
      * @return result String buffer in String format
      */
     public static String byteArrayToHexString(final byte in[]) {
         final String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
 
         if (in == null || in.length <= 0) {
             return "";
         }
 
         final StringBuilder out = new StringBuilder(in.length * 2);
 
         for (final byte b : in) {
             byte ch = (byte) (b & 0xF0);    // strip off high nibble
             ch = (byte) (ch >>> 4);         // shift the bits down
             ch = (byte) (ch & 0x0F);        // must do this is high order bit is on!
             out.append(pseudo[(int) ch]);   // convert the nibble to a String Character
             ch = (byte) (b & 0x0F);         // strip off low nibble
             out.append(pseudo[(int) ch]);   // convert the nibble to a String Character
         }
 
         return out.toString();
     }
 
     /**
      * Update the user's "lastUpdated" attribute.  By default this is "pwmLastUpdate" attribute
      *
      * @param pwmSession to lookup session info
      * @param theUser    ldap user to operate on
      * @return true if successful;
      * @throws ChaiUnavailableException if the directory is unavailble
      */
     public static boolean updateLastUpdateAttribute(final PwmSession pwmSession, final ChaiUser theUser)
             throws ChaiUnavailableException {
         boolean success = false;
 
         final String updateAttribute = pwmSession.getConfig().readSettingAsString(PwmSetting.PASSWORD_LAST_UPDATE_ATTRIBUTE);
 
         if (updateAttribute != null && updateAttribute.length() > 0) {
             final String currentTimestamp = EdirEntries.convertDateToZulu(new Date(System.currentTimeMillis()));
             try {
                 final String pwdLastModifiedAttr = pwmSession.getConfig().readSettingAsString(PwmSetting.PASSWORD_LAST_UPDATE_ATTRIBUTE);
                 theUser.writeStringAttribute(pwdLastModifiedAttr, currentTimestamp);
                 LOGGER.debug(pwmSession, "wrote pwdLastModified update attribute for " + theUser.getEntryDN());
                 success = true;
             } catch (ChaiOperationException e) {
                 LOGGER.debug(pwmSession, "error writing update attribute for user '" + theUser.getEntryDN() + "' " + e.getMessage());
             }
         }
 
         return success;
     }
 
     /**
      * Pause the calling thread the specified amount of time.
      *
      * @param sleepTimeMS - a time duration in milliseconds
      * @return time actually spent sleeping
      */
     public static long pause(final long sleepTimeMS) {
         final long startTime = System.currentTimeMillis();
         do {
             try {
                 final long sleepTime = sleepTimeMS - (System.currentTimeMillis() - startTime);
                 Thread.sleep(sleepTime > 0 ? sleepTime : 5);
             } catch (InterruptedException e) {
                 //who cares
             }
         } while ((System.currentTimeMillis() - startTime) < sleepTimeMS);
 
         return System.currentTimeMillis() - startTime;
     }
 
     public static void invokeExternalChangeMethods(
             final PwmSession pwmSession,
             final String oldPassword,
             final String newPassword) {
         final List<String> externalMethods = pwmSession.getConfig().readStringArraySetting(PwmSetting.EXTERNAL_CHANGE_METHODS);
 
         // process any configured external change password methods configured.
         for (final String classNameString : externalMethods) {
             if (classNameString != null && classNameString.length() > 0) {
                 try {
                     // load up the class and get an instance.
                     final Class<?> theClass = Class.forName(classNameString);
                     final ExternalChangeMethod externalClass = (ExternalChangeMethod) theClass.newInstance();
 
                     // invoke the passwordChange method;
                     final boolean success = externalClass.passwordChange(pwmSession, oldPassword, newPassword);
 
                     if (success) {
                         LOGGER.info(pwmSession, "externalPasswordMethod '" + classNameString + "' was successfull");
                     } else {
                         LOGGER.warn(pwmSession, "externalPasswordMethod '" + classNameString + "' was not successfull");
                     }
                 } catch (ClassCastException e) {
                     LOGGER.warn(pwmSession, "configured external class " + classNameString + " is not an instance of " + ExternalChangeMethod.class.getName());
                 } catch (ClassNotFoundException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage() + "; perhaps the class is not in the classpath?");
                 } catch (IllegalAccessException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage());
                 } catch (InstantiationException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage());
                 }
             }
         }
     }
 
 
     public static List<Integer> invokeExternalJudgeMethods(
             final Configuration config,
             final PwmSession pwmSession,
             final String password) {
         final List<String> externalMethods = config.readStringArraySetting(PwmSetting.EXTERNAL_JUDGE_METHODS);
         final List<Integer> returnList = new ArrayList<Integer>();
 
         // process any configured external change password methods configured.
         for (final String classNameString : externalMethods) {
             if (classNameString != null && classNameString.length() > 0) {
                 try {
                     // load up the class and get an instance.
                     final Class<?> theClass = Class.forName(classNameString);
                     final ExternalJudgeMethod externalClass = (ExternalJudgeMethod) theClass.newInstance();
 
                     // invoke the passwordChange method;
                     final int result = externalClass.judgePassword(pwmSession, password);
                     LOGGER.debug(pwmSession, "externalJudgeMethod '" + classNameString + "' returned a value of " + result);
                     returnList.add(result);
                 } catch (ClassCastException e) {
                     LOGGER.warn(pwmSession, "configured external class " + classNameString + " is not an instance of " + ExternalChangeMethod.class.getName());
                 } catch (ClassNotFoundException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage() + "; perhaps the class is not in the classpath?");
                 } catch (IllegalAccessException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage());
                 } catch (InstantiationException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage());
                 }
             }
         }
 
         return returnList;
     }
 
     public static List<ErrorInformation> invokeExternalRuleMethods(
             final Configuration config,
             final PwmSession pwmSession,
             final PwmPasswordPolicy pwmPasswordPolicy,
             final String password) {
         final List<String> externalMethods = config.readStringArraySetting(PwmSetting.EXTERNAL_RULE_METHODS);
         final List<ErrorInformation> returnList = new ArrayList<ErrorInformation>();
 
         // process any configured external change password methods configured.
         for (final String classNameString : externalMethods) {
             if (classNameString != null && classNameString.length() > 0) {
                 try {
                     // load up the class and get an instance.
                     final Class<?> theClass = Class.forName(classNameString);
                     final ExternalRuleMethod externalClass = (ExternalRuleMethod) theClass.newInstance();
                     final List<ErrorInformation> loopReturnList = new ArrayList<ErrorInformation>();
 
                     // invoke the passwordChange method;
                     final ExternalRuleMethod.RuleValidatorResult result = externalClass.validatePasswordRules(pwmSession, pwmPasswordPolicy, password);
                     if (result != null && result.getPwmErrors() != null) {
                         for (final ErrorInformation errorInformation : result.getPwmErrors()) {
                             loopReturnList.add(errorInformation);
                             LOGGER.debug(pwmSession, "externalRuleMethod '" + classNameString + "' returned a value of " + errorInformation.toDebugStr());
                         }
                     }
                     if (result != null && result.getStringErrors() != null) {
                         for (final String errorString : result.getStringErrors()) {
                             final ErrorInformation errorInformation = new ErrorInformation(PwmError.PASSWORD_UNKNOWN_VALIDATION, errorString);
                             loopReturnList.add(errorInformation);
                             LOGGER.debug(pwmSession, "externalRuleMethod '" + classNameString + "' returned a value of " + errorInformation.toDebugStr());
                         }
                     }
                     if (loopReturnList.isEmpty()) {
                         LOGGER.debug(pwmSession, "externalRuleMethod '" + classNameString + "' returned no values");
                     }
                     returnList.addAll(loopReturnList);
                 } catch (ClassCastException e) {
                     LOGGER.warn(pwmSession, "configured external class " + classNameString + " is not an instance of " + ExternalChangeMethod.class.getName());
                 } catch (ClassNotFoundException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage() + "; perhaps the class is not in the classpath?");
                 } catch (IllegalAccessException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage());
                 } catch (InstantiationException e) {
                     LOGGER.warn(pwmSession, "unable to load configured external class: " + classNameString + " " + e.getMessage());
                 }
             }
         }
 
         return returnList;
     }
 
     public static boolean testEmailAddress(final String address) {
         final Pattern pattern = Pattern.compile("^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*$");
         final Matcher matcher = pattern.matcher(address);
         return matcher.matches();
     }
 
     public static boolean testUserMatchQueryString(
             final PwmSession pwmSession,
             final String objectDN,
             final String queryString
     )
             throws ChaiUnavailableException, PwmException {
         if (objectDN == null || objectDN.length() < 1) {
             return true;
         }
 
         if (queryString == null || queryString.length() < 1) {
             return true;
         }
 
         final ChaiProvider provider = pwmSession.getContextManager().getProxyChaiProvider();
 
         try {
             final Map<String, Properties> results = provider.search(objectDN, queryString, new String[]{}, ChaiProvider.SEARCH_SCOPE.SUBTREE);
 
             if (results == null || results.size() != 1) {
                 return false;
             }
 
             final String returnedDN = Helper.trimString(results.keySet().iterator().next(), ",");
 
             if (returnedDN.equals(objectDN)) {
                 return true;
             }
         } catch (ChaiOperationException e) {
             LOGGER.debug(pwmSession, "error testing match query string: " + queryString, e);
         }
 
         return false;
     }
 
     /**
      * Strips specified characters of the beginning and end of a string.  Similar to
      * {@link String#trim()}, except the caller can specify an arbitrary character instead
      * of just whitespace chars.
      *
      * @param str   String to operate on
      * @param chars A String containing characters to remove from beginning/end
      * @return the (possibly) modifed str value
      */
     public static String trimString(
             final String str,
             final String chars
     ) {
         if (chars == null || chars.length() < 1) {
             return str;
         }
 
         final StringBuilder sb = new StringBuilder(str);
 
         for (int i = 0; i < chars.length(); i++) {
             if (sb.charAt(0) == chars.charAt(i)) {
                 sb.delete(0, 1);
             }
 
             if (sb.charAt(sb.length() - 1) == chars.charAt(i)) {
                 sb.delete(sb.length() - 2, sb.length() - 1);
             }
         }
 
         return sb.toString();
     }
 
 
     /**
      * Writes a Map of values to ldap onto the supplied user object.
      * The map key must be a string of attribute names.  The map value
      * must be either a string, or a {@link password.pwm.config.FormConfiguration} object.
      * <p/>
      * Any ldap operation exceptions are not reported (but logged).
      *
      * @param pwmSession       for looking up session info
      * @param theUser          User to write to
      * @param stringOrParamMap A map with String keys and String or {@link password.pwm.config.FormConfiguration} values. @throws ChaiUnavailableException
      * @throws ChaiUnavailableException if the directory is unavailable
      * @throws ChaiOperationException   if their is an unexpected ldap problem
      */
     public static void writeMapToEdir(final PwmSession pwmSession, final ChaiUser theUser, final Map stringOrParamMap)
             throws ChaiUnavailableException, ChaiOperationException {
         for (final Object key : stringOrParamMap.keySet()) {
             final String attrName = (String) key;
             final Object mapValue = stringOrParamMap.get(attrName);
             String attrValue = null;
             if (mapValue instanceof String) {
                 attrValue = (String) mapValue;
             } else if (mapValue instanceof FormConfiguration) {
                 attrValue = ((FormConfiguration) mapValue).getValue();
             }
 
             if (attrValue != null && attrValue.length() > 0) {
                 try {
                     theUser.writeStringAttribute(attrName, attrValue);
                     LOGGER.info(pwmSession, "set attribute on user " + theUser.getEntryDN() + " (" + attrName + "=" + attrValue + ")");
                 } catch (ChaiOperationException e) {
                     LOGGER.error(pwmSession, "error setting attribute on user " + theUser.getEntryDN() + " (" + attrName + "=" + attrValue + ") " + e.getMessage());
                     throw e;
                 }
             } else {
                 LOGGER.debug(pwmSession, "no value to set for " + attrName + " on user " + theUser.getEntryDN() + ", skipping");
             }
         }
     }
 
     public static String binaryArrayToHex(final byte[] buf) {
         final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
         final char[] chars = new char[2 * buf.length];
         for (int i = 0; i < buf.length; ++i) {
             chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
             chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
         }
         return new String(chars);
     }
 
     public static long getFileDirectorySize(final File dir) {
         long size = 0;
         try {
             if (dir.isFile()) {
                 size = dir.length();
             } else {
                 final File[] subFiles = dir.listFiles();
 
                 for (final File file : subFiles) {
                     if (file.isFile()) {
                         size += file.length();
                     } else {
                         size += getFileDirectorySize(file);
                     }
 
                 }
             }
         } catch (NullPointerException e) {
             // file was deleted before file size could be read
         }
 
         return size;
     }
 
     public static String formatDiskSize(final long diskSize) {
         final float COUNT = 1000;
         if (diskSize < 1) {
             return "n/a";
         }
 
         if (diskSize == 0) {
             return "0";
         }
 
         final NumberFormat nf = NumberFormat.getInstance();
         nf.setMaximumFractionDigits(2);
 
         if (diskSize > COUNT * COUNT * COUNT) {
             final StringBuilder sb = new StringBuilder();
             sb.append(nf.format(diskSize / COUNT / COUNT / COUNT));
             sb.append(" GB");
             return sb.toString();
         }
 
         if (diskSize > COUNT * COUNT) {
             final StringBuilder sb = new StringBuilder();
             sb.append(nf.format(diskSize / COUNT / COUNT));
             sb.append(" MB");
             return sb.toString();
         }
 
         return NumberFormat.getInstance().format(diskSize) + " bytes";
     }
 
     public static Locale localeResolver(final Locale desiredLocale, final Collection<Locale> localePool) {
         if (desiredLocale == null || localePool == null || localePool.isEmpty()) {
             return null;
         }
 
         for (final Locale loopLocale : localePool) {
             if (loopLocale.getLanguage().equalsIgnoreCase(desiredLocale.getLanguage())) {
                 if (loopLocale.getCountry().equalsIgnoreCase(desiredLocale.getCountry())) {
                     if (loopLocale.getVariant().equalsIgnoreCase(desiredLocale.getVariant())) {
                         return loopLocale;
                     }
                 }
             }
         }
 
         for (final Locale loopLocale : localePool) {
             if (loopLocale.getLanguage().equalsIgnoreCase(desiredLocale.getLanguage())) {
                 if (loopLocale.getCountry().equalsIgnoreCase(desiredLocale.getCountry())) {
                     return loopLocale;
                 }
             }
         }
 
         for (final Locale loopLocale : localePool) {
             if (loopLocale.getLanguage().equalsIgnoreCase(desiredLocale.getLanguage())) {
                 return loopLocale;
             }
         }
 
         final Locale emptyLocale = parseLocaleString("");
         if (localePool.contains(emptyLocale)) {
             return emptyLocale;
         }
 
         return null;
     }
 
     public static Locale parseLocaleString(final String localeString) {
         if (localeString == null) {
             return new Locale("");
         }
 
         final StringTokenizer st = new StringTokenizer(localeString, "_");
 
         if (!st.hasMoreTokens()) {
             return new Locale("");
         }
 
         final String language = st.nextToken();
         if (!st.hasMoreTokens()) {
             return new Locale(language);
         }
 
         final String country = st.nextToken();
         if (!st.hasMoreTokens()) {
             return new Locale(language, country);
         }
 
         final String variant = st.nextToken("");
         return new Locale(language, country, variant);
     }
 
 
     public static String readRequestBody(final HttpServletRequest request, final int maxChars) throws IOException {
         final StringBuilder inputData = new StringBuilder();
         String line;
         try {
             final BufferedReader reader = request.getReader();
             while (((line = reader.readLine()) != null) && inputData.length() < maxChars) {
                 inputData.append(line);
             }
         } catch (Exception e) {
             LOGGER.error("error reading request body stream: " + e.getMessage());
         }
         return inputData.toString();
     }
 
     public static String debugHttpRequest(final HttpServletRequest req) {
         final StringBuilder sb = new StringBuilder();
 
         sb.append(req.getMethod());
         sb.append(" request for: ");
         sb.append(req.getRequestURI());
 
         if (req.getParameterMap().isEmpty()) {
             sb.append(" (no params)");
         } else {
             sb.append("\n");
 
             for (final Enumeration paramNameEnum = req.getParameterNames(); paramNameEnum.hasMoreElements();) {
                 final String paramName = (String) paramNameEnum.nextElement();
                 final Set<String> paramValues = Validator.readStringsFromRequest(req, paramName, 1024);
 
                 for (final String paramValue : paramValues) {
                     sb.append("  ").append(paramName).append("=");
                     if (paramName.toLowerCase().contains("password") || paramName.startsWith(PwmConstants.PARAM_RESPONSE_PREFIX)) {
                         sb.append("***removed***");
                     } else {
                         sb.append('\'');
                         sb.append(paramValue);
                         sb.append('\'');
                     }
 
                     sb.append('\n');
                 }
             }
 
             sb.deleteCharAt(sb.length() - 1);
         }
         return sb.toString();
     }
 }
