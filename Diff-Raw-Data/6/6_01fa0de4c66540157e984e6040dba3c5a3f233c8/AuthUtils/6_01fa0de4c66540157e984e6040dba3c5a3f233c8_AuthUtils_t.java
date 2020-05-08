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
 * $Id: AuthUtils.java,v 1.8 2006-11-06 20:24:32 pawand Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.authentication.service;
 
 import com.iplanet.am.util.AMClientDetector;
 import com.iplanet.am.util.SystemProperties;
 import com.sun.identity.shared.xml.XMLUtils;
 import com.iplanet.dpro.session.Session;
 import com.iplanet.dpro.session.SessionID;
 import com.iplanet.dpro.session.service.InternalSession;
 import com.iplanet.dpro.session.share.SessionEncodeURL;
 import com.iplanet.services.cdm.AuthClient;
 import com.iplanet.services.cdm.Client;
 import com.iplanet.services.cdm.ClientsManager;
 import com.iplanet.services.naming.WebtopNaming;
 import com.iplanet.services.util.Crypt;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.authentication.AuthContext;
 import com.sun.identity.authentication.config.AMAuthConfigUtils;
 import com.sun.identity.authentication.config.AMAuthLevelManager;
 import com.sun.identity.authentication.server.AuthContextLocal;
 import com.sun.identity.authentication.spi.AMLoginModule;
 import com.sun.identity.authentication.spi.AuthLoginException;
 import com.sun.identity.authentication.util.ISAuthConstants;
 import com.sun.identity.common.DNUtils;
 import com.sun.identity.common.FQDNUtils;
 import com.sun.identity.common.ISLocaleContext;
 import com.sun.identity.common.RequestUtils;
 import com.sun.identity.common.ResourceLookup;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.security.EncodeAction;
 import com.sun.identity.session.util.SessionUtils;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.encode.CookieUtils;
 import com.sun.identity.shared.encode.URLEncDec;
 import com.sun.identity.shared.locale.Locale;
 import com.sun.identity.sm.SMSEntry;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.AccessController;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.login.AppConfigurationEntry;
 import javax.security.auth.login.Configuration;
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * This class has utility methods for domain cookie management, URL formatting, 
  * getting handle to AuthContext and LoginState objects, retrieving and 
  * validation session from session identifier, retrieving the client types 
  * attribute fields that determines the client type , etc.
  */
 public class AuthUtils {
     /**
      * Default client type
      */
     public static final String  DEFAULT_CLIENT_TYPE ="genericHTML";
     private static final String  DEFAULT_CONTENT_TYPE="text/html";
     private static final String  DEFAULT_FILE_PATH="html";
     private static final String  DSAME_VERSION="7.0";
     /**
      * Error message
      */
     public static final String ERROR_MESSAGE = "Error_Message";
     /**
      * Error template
      */
     public static final String ERROR_TEMPLATE = "Error_Template";
     /**
      * Message delemeter
      */
     public static final String MSG_DELIMITER= "|";
     /**
      * Auth resource bundle name
      */
     public static final String BUNDLE_NAME="amAuth";
     
     private static boolean setRequestEncoding = false;
     
     private static ArrayList pureJAASModuleClasses = new ArrayList();
     private static ArrayList ISModuleClasses = new ArrayList();
     private static AMClientDetector clientDetector;
     private static Client defaultClient;
     private static FQDNUtils fqdnUtils;
     private static Hashtable moduleService = new Hashtable();
     private static ResourceBundle bundle;
     private static final boolean urlRewriteInPath = Boolean.valueOf(
         SystemProperties.get(Constants.REWRITE_AS_PATH,"")).booleanValue();
 
     private static final String templatePath =
         Constants.FILE_SEPARATOR + ISAuthConstants.CONFIG_DIR +
         Constants.FILE_SEPARATOR + ISAuthConstants.AUTH_DIR;
     private static final String rootSuffix = SMSEntry.getRootSuffix();
     
     // dsame version
     private static String dsameVersion =
         SystemProperties.get(Constants.AM_VERSION,DSAME_VERSION);
     
     /* Constants.AM_COOKIE_NAME is the AM Cookie which
      * gets set when the user has authenticated
      */
     private static String cookieName = SystemProperties.get(
         Constants.AM_COOKIE_NAME);
     
     /* Constants.AM_AUTH_COOKIE_NAME is the Auth Cookie which
      * gets set during the authentication process.
      */
     private static String authCookieName = SystemProperties.get(
         Constants.AM_AUTH_COOKIE_NAME, ISAuthConstants.AUTH_COOKIE_NAME);
     private static String loadBalanceCookieName = SystemProperties.get(
         Constants.AM_LB_COOKIE_NAME);
     private static String persistentCookieName = SystemProperties.get(
         Constants.AM_PCOOKIE_NAME);
     private static String loadBalanceCookieValue = SystemProperties.get(
         Constants.AM_LB_COOKIE_VALUE);
     private static String serviceURI = SystemProperties.get(
         Constants.AM_SERVICES_DEPLOYMENT_DESCRIPTOR) + "/UI/Login";
     
     // Name of the webcontainer
     private static String webContainer = SystemProperties.get(
         Constants.IDENTITY_WEB_CONTAINER);
     private static String serverURL = null;
     static Debug utilDebug = Debug.getInstance("amAuthUtils");
     
     static {
         // Initialzing variables
         String installTime = 
         SystemProperties.get(AdminTokenAction.AMADMIN_MODE, "false");
         if (installTime.equalsIgnoreCase("false")) {
             clientDetector = new AMClientDetector();
             if (isClientDetectionEnabled()) {
                 defaultClient = ClientsManager.getDefaultInstance();
             }
         }
         fqdnUtils = new FQDNUtils();
         bundle = Locale.getInstallResourceBundle(BUNDLE_NAME);
         if (webContainer != null && webContainer.length() > 0) {
             if (webContainer.indexOf("BEA") >= 0 ||
                 webContainer.indexOf("IBM5.1") >= 0 ) {
                 setRequestEncoding = true;
             }
         }
         
         String proto = SystemProperties.get(Constants.DISTAUTH_SERVER_PROTOCOL);
         String host = null;
         String port = null;
         if (proto != null && proto.length() != 0 ) {
             host = SystemProperties.get(Constants.DISTAUTH_SERVER_HOST);
             port = SystemProperties.get(Constants.DISTAUTH_SERVER_PORT);
         } else {
             proto = SystemProperties.get(Constants.AM_SERVER_PROTOCOL);
             host = SystemProperties.get(Constants.AM_SERVER_HOST);
             port = SystemProperties.get(Constants.AM_SERVER_PORT);
         }
         serverURL = proto + "://" + host + ":" + port;
     }
     
     /**
      * Creates <code>AuthUtils</code> object
      */
     public AuthUtils() {
         utilDebug.message("AuthUtil: constructor");
     }
     
     /* retrieve session */
     /**
      * Returns session associated with <code>AuthContextLocal</code> object
      * @param authContext auth context has session
      * @return session associated with <code>AuthContextLocal</code> object
      */
     public InternalSession getSession(AuthContextLocal authContext) {
         InternalSession sess = getLoginState(authContext).getSession();
         if (utilDebug.messageEnabled()) {
             utilDebug.message("returning session : " + sess);
         }
         return sess;
     }
     
     /* this method does the following
      * 1. initializes authService (AuthD) if not already done.
      * 2. parses the request parameters and stores in dataHash
      * 3. Retrieves the AuthContext object from the global table
      * 4. if this is found then updates the loginState request
      *    type to false and updates the parameter hash table in
      *   loginstate object.
      
      * on error throws AuthException
      */
     
     /**
      * Returns the <code>AuthContext</code> Handle for the Request.
      *
      * @param request HTTP Servlet Request.
      * @param response HTTP Servlet Response.
      * @param sid Session ID for this request.
      * @param isSessionUpgrade
      * @param isBackPost
      * @return <code>AuthContextLocal</code> object
      * @throws AuthException
      */
     public static AuthContextLocal getAuthContext(
         HttpServletRequest request,
         HttpServletResponse response,
         SessionID sid,
         boolean isSessionUpgrade,
         boolean isBackPost
     ) throws AuthException {
         return getAuthContext(request, response, sid, isSessionUpgrade,
             isBackPost, false);
     }
     
     /**
      * Returns the <code>AuthContext</code> Handle for the Request.
      *
      * @param request HTTP Servlet Request.
      * @param response HTTP Servlet Response.
      * @param sid Session ID for this request.
      * @param isSessionUpgrade
      * @param isBackPost
      * @param isLogout
      * @return <code>AuthContextLocal</code> object.
      * @throws AuthException
      */
     public static AuthContextLocal getAuthContext(
         HttpServletRequest request,
         HttpServletResponse response,
         SessionID sid,
         boolean isSessionUpgrade,
         boolean isBackPost,
         boolean isLogout
     ) throws AuthException {
         utilDebug.message("In AuthUtils:getAuthContext");
         Hashtable dataHash;
         AuthContextLocal authContext = null;
         LoginState loginState = null;
         // initialize auth service.
         AuthD ad = AuthD.getAuth();
         
         try {
             dataHash = parseRequestParameters(request);
             // commented this since it debug file
             // has too many messages and making the file large
             // in size.
             authContext = retrieveAuthContext(request, sid);
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("AuthUtil:getAuthContext:sid is.. .: " + sid);
                 utilDebug.message("AuthUtil:getAuthContext:authContext is.. .: "
                 + authContext);
             }
             
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("isSessionUpgrade  :" + isSessionUpgrade);
                 utilDebug.message(
                     "BACK with Request method POST : " + isBackPost);
             }
             
             if ((authContext == null)  && (isLogout)) {
                 return null;
             }
             
             if ((authContext == null) || (isSessionUpgrade) || (isBackPost)) {
                 try {
                     loginState = new LoginState();
                     if (isSessionUpgrade) {
                         loginState.setPrevAuthContext(authContext);
                         loginState.setSessionUpgrade(isSessionUpgrade);
                     } else if (isBackPost) {
                         loginState.setPrevAuthContext(authContext);
                     }
                     
                     authContext =
                     loginState.createAuthContext(request,response,sid,dataHash);
                     authContext.setLoginState(loginState);
                     String queryOrg =
                         getQueryOrgName(request,getOrgParam(dataHash));
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("query org is .. : "+ queryOrg);
                     }
                     loginState.setQueryOrg(queryOrg);
                 } catch (AuthException ae) {
                     utilDebug.message("Error creating AuthContextLocal : ");
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("Exception " , ae);
                     }
                     throw new AuthException(ae);
                 }
             } else {
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message(
                         "getAuthContext: found existing request.");
                 }
                 try {
                     authContext = processAuthContext(authContext,request,
                     response,dataHash,sid);
                     loginState = getLoginState(authContext);
                     loginState.setRequestType(false);
                 } catch (AuthException ae) {
                     utilDebug.message("Error Retrieving AuthContextLocal");
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("Exception " , ae);
                     }
                     throw new AuthException(AMAuthErrorCode.AUTH_ERROR, null);
                 }
             }
             
         } catch (Exception ee) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message(
                     "Error creating AuthContextLocal : " + ee.getMessage());
             }
             
             throw new AuthException(ee);
         }
         return authContext;
         
     }
     
     
     // processAuthContext checks for arg=newsession in the HttpServletRequest
     // if request has arg=newsession then destroy session and create a new
     // AuthContextLocal object.
     static AuthContextLocal processAuthContext(
         AuthContextLocal authContext,
         HttpServletRequest request,
         HttpServletResponse response,
         Hashtable dataHash,
         SessionID sid
     ) throws AuthException {
         // initialize auth service.
         AuthD ad = AuthD.getAuth();
         
         LoginState loginState = getLoginState(authContext);
         com.iplanet.dpro.session.service.InternalSession sess = null;
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("in processAuthContext authcontext : " 
                 + authContext );
             utilDebug.message("in processAuthContext request : " + request);
             utilDebug.message("in processAuthContext response : " + response);
             utilDebug.message("in processAuthContext sid : " + sid);
         }
         
         if (newSessionArgExists(dataHash, sid) &&
         (loginState.getLoginStatus() == LoginStatus.AUTH_SUCCESS)) {
             // destroy auth context and create new one.
             utilDebug.message("newSession arg exists");
             destroySession(loginState);
             try{
                 loginState = new LoginState();
                 authContext = loginState.createAuthContext(request,response,
                 sid,dataHash);
                 authContext.setLoginState(loginState);
                 String queryOrg =
                 getQueryOrgName(request,getOrgParam(dataHash));
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("query org is .. : "+ queryOrg);
                 }
                 loginState.setQueryOrg(queryOrg);
             } catch (AuthException ae) {
                 utilDebug.message("Error creating AuthContextLocal");
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("Exception " , ae);
                 }
                 throw new AuthException(AMAuthErrorCode.AUTH_ERROR, null);
             }
         } else {
             // update loginState - requestHash , sess
             utilDebug.message("new session arg does not exist");
             loginState.setHttpServletRequest(request);
             loginState.setHttpServletResponse(response);
             loginState.setParamHash(dataHash);
             sess = ad.getSession(sid);
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("AuthUtil :Session is .. : " + sess);
             }
             loginState.setSession(sess);
             loginState.persistentCookieArgExists();
             loginState.setRequestLocale(request);
             if (checkForCookies(request)) {
                 loginState.setCookieDetect(false);
             }
         }
         return authContext;
     }
 
     /**
      * Returns <code>LoginState</code> object associated with 
      * <code>AuthContext</code>
      * @param authContext auth context has <code>LoginState</code>
      * @return <code>LoginState</code> object associated with 
      * <code>AuthContext</code>
      */
     public static LoginState getLoginState(AuthContextLocal authContext) {
         LoginState loginState = null;
         if (authContext != null) {
             loginState = authContext.getLoginState();
         }
         return loginState;
     }
     
     /**
      * Parses request parameters in <code>HttpServletRequest</code> object
      * @param request <code>HttpServletRequest</code> has parameters
      * @return <code>Hashtable</code> of request parameters
      */
     public static Hashtable parseRequestParameters(HttpServletRequest request) {
         Enumeration requestEnum = request.getParameterNames();
         return decodeHash(request,requestEnum);
     }
     
     private static Hashtable decodeHash(
         HttpServletRequest request,
         Enumeration names) {
         Hashtable data = new Hashtable();
         String enc = request.getParameter("gx_charset");
         if (utilDebug.messageEnabled()) {
             utilDebug.message("AuthUtils::decodeHash:enc = "+enc);
         }
         while (names.hasMoreElements()) {
             String name = (String) names.nextElement();
             String value = request.getParameter(name);
             if (setRequestEncoding) {
                 data.put(name, Locale.URLDecodeField(value, enc,AuthD.debug));
             } else {
                 data.put(name, value);
             }
         } // while
         return data;
     }
     
     /**
      * Returns request parameters in auth context
      * @param authContext auth context has request parameters 
      * @return <code>Hashtable</code> of parameters
      */
     public Hashtable getRequestParameters(AuthContextLocal authContext) {
         LoginState loginState = getLoginState(authContext);
         if (loginState != null) {
             return loginState.getRequestParamHash();
         } else {
             return new Hashtable();
         }
     }
     
     /**
      * Returns the sid from the authh context object
      * @param authContext auth context has sid
      * @return the sid from the authh context object
      * @throws AuthException if it fails to retrieve sid
      */
     public static String getSidString(AuthContextLocal authContext)
             throws AuthException {
         InternalSession sess = null;
         String sidString = null;
         try {
             if (authContext != null) {
                 LoginState loginState = authContext.getLoginState();
                 if (loginState != null) {
                     SessionID sid = loginState.getSid();
                     if (sid != null) {
                         sidString = sid.toString();
                     }
                 }
             }
         } catch (Exception  e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error retreiving sid.. :" + e.getMessage());
             }
             /// no need to have error code since the method where this is called
             /// generates AUTH_ERROR
             throw new AuthException("noSid", new Object[] {e.getMessage()});
         }
         return sidString;
     }
     
     /**
      * Returns the Cookie object created based on the <code>cookieName</code>,
      * Session ID and <code>cookieDomain</code>. If Session is in Invalid State
      * then cookie is created with Authentication Cookie Name , if
      * Active/Inactive Session state Access Manager Cookie Name will be used
      * to create cookie.
      *
      * @param ac Authentication context object.
      *@param cookieDomain the cookie domain for creating cookie.
      * @return Cookie object
      */
     public Cookie getCookieString(AuthContextLocal ac,String cookieDomain) {
         Cookie cookie=null;
         String cookieName = getCookieName();
         try {
             String sidString= getSidString(ac);
             LoginState loginState = getLoginState(ac);
             if (loginState != null && loginState.isSessionInvalid()) {
                 cookieName = getAuthCookieName();
                 utilDebug.message("Create AM AUTH cookie");
             }
             cookie = createCookie(cookieName,sidString,cookieDomain);
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error getting sid : " + e.getMessage());
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Cookie is : " + cookie);
         }
         return cookie;
     }
     
     /**
      * Returns the Logout cookie.
      *
      * @param ac Authentication Context object.
      * @param cookieDomain Cookie domain.
      * @return Logout cookie.
      */
     public Cookie getLogoutCookie(AuthContextLocal ac, String cookieDomain) {
         LoginState loginState = getLoginState(ac);
         SessionID sid = loginState.getSid();
         String logoutCookieString = getLogoutCookieString(sid);
         Cookie logoutCookie = createCookie(logoutCookieString,cookieDomain);
         logoutCookie.setMaxAge(0);
         return logoutCookie;
     }
     
     /**
      * Returns the Logout cookie.
      *
      * @param sid Session ID.
      * @param cookieDomain Cookie domain.
      * @return Logout cookie string.
      */
     public Cookie getLogoutCookie(SessionID sid,String cookieDomain) {
         String logoutCookieString = getLogoutCookieString(sid);
         Cookie logoutCookie = createCookie(logoutCookieString,cookieDomain);
         logoutCookie.setMaxAge(0);
         return logoutCookie;
     }
     
     
     /**
      * Returns the encrpted Logout cookie string.
      * The format of this cookie is:
      * <code>LOGOUT@protocol@servername@serverport@sessiondomain</code>
      *
      * @param sid Session ID.
      * @return Logout cookie string.
      */
     public static String getLogoutCookieString(SessionID sid) {
         String logout_cookie = null;
         try {
             logout_cookie = (String) AccessController.doPrivileged(
             new EncodeAction(
             "LOGOUT" + "@" +
             sid.getSessionServerProtocol() + "@" +
             sid.getSessionServer() + "@" +
             sid.getSessionServerPort() + "@" +
             sid.getSessionDomain(), Crypt.getHardcodedKeyEncryptor()));
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Logout cookie : " + logout_cookie);
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error creating cookie : " + e.getMessage());
             }
         }
         return logout_cookie ;
     }
     
     /**
      * Returns Cookie to be set in the response.
      *
      * @param cookieValue Value of cookie.
      * @param cookieDomain Domain for which cookie will be set.
      * @return Cookie object.
      */
     public Cookie createCookie(String cookieValue, String cookieDomain) {
         
         String cookieName = getCookieName();
         if (utilDebug.messageEnabled()) {
             utilDebug.message("cookieName : " + cookieName);
             utilDebug.message("cookieValue : " + cookieValue);
             utilDebug.message("cookieDomain : " + cookieDomain);
         }
         return createCookie(cookieName,cookieValue,cookieDomain);
     }
 
     /**
      * Returns true if request is new.
      * @param ac auth context to be checked for new request
      * @return <code>true</code> if it has new request
      */
     public boolean isNewRequest(AuthContextLocal ac) {
         
         LoginState loginState = getLoginState(ac);
         if (loginState.isNewRequest()) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("this is a newRequest");
             }
             return true;
         } else {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("this is an existing request");
             }
             return false;
         }
     }
     
     /**
      * Returns url for login success
      * @param authContext auth context to be checked for login success
      * @return url for login success
      */
     public String getLoginSuccessURL(AuthContextLocal authContext) {
         String successURL = null;
         LoginState loginState = getLoginState(authContext);
         if (loginState == null) {
             successURL = AuthD.getAuth().defaultSuccessURL;
         } else {
             successURL = getLoginState(authContext).getSuccessLoginURL();
         }
         return successURL;
     }
     
     /**
      * Returns url for login failure
      * @param authContext auth context to be checked for login failure
      * @return url for login failure
      */
     public String getLoginFailedURL(AuthContextLocal authContext) {
         
         try {
             LoginState loginState = getLoginState(authContext);
             if (loginState == null) {
                 return AuthD.getAuth().defaultFailureURL;
             }
             String loginFailedURL=loginState.getFailureLoginURL();
             if (utilDebug.messageEnabled()) {
                 utilDebug.message(
                     "AuthUtils: getLoginFailedURL "+ loginFailedURL);
             }
             
             // remove the loginstate/authContext from the hashtable
             //removeLoginStateFromHash(authContext);
             //       destroySession(authContext);
             return loginFailedURL;
         } catch (Exception e) {
             utilDebug.message("Exception " , e);
             return null;
         }
     }
     
     /**
      * Returns filename  - will use FileLookUp API
      * for UI only - this returns the relative path
      * @param authContext 
      * @param fileName
      * @return filename 
      */
     public String getFileName(AuthContextLocal authContext,String fileName) {
         
         LoginState loginState = getLoginState(authContext);
         String relFileName = null;
         if (loginState != null) {
             relFileName = getLoginState(authContext).getFileName(fileName);
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getFileName:AuthUtilsFile name is :"
             + relFileName);
         }
         return relFileName;
     }
     
     /**
      * Returns status of Inet domain for given auth context
      * @param authContext auth context to be tested
      * @return <code>true</code> if organization is active
      */
     public boolean getInetDomainStatus(AuthContextLocal authContext) {
         return getLoginState(authContext).getInetDomainStatus();
     }
     
     /**
      * Check if new session arg exists
      * @param dataHash <code>Hashtable</code> object has newsession
      * @param sid <code>SessionID</code> object gets searched
      * @return <code>true</code> if new session arg exists
      */
     public static boolean newSessionArgExists(
         Hashtable dataHash,
         SessionID sid) {
         String arg = (String)dataHash.get("arg");
         if (arg != null && arg.equals("newsession")) {
             if (retrieveAuthContext(sid) != null) {
                 return true;
             } else {
                 return false;
             }
         }
         return false;
     }
     
     /**
      * Encode url
      * @param url url to get encoded
      * @param authContext auth context associated with url
      * @param response <code>HttpServletResponse</code> associated with url
      * @return encoded url
      */
     public String encodeURL(String url,
     AuthContextLocal authContext,
     HttpServletResponse response) {
         if (utilDebug.messageEnabled()) {
             utilDebug.message("AuthUtils:input url is :"+ url);
         }
         LoginState loginState = getLoginState(authContext);
         String encodedURL;
         
         if (loginState==null) {
             encodedURL = url;
         } else {
             encodedURL = loginState.encodeURL(url,response);
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("AuthUtils:encoded url is :"+encodedURL);
         }
         
         return encodedURL;
     }
     
     /**
      * Returns <code>Locale</code> for given authContext
      * @param authContext to get checked for <code>Locale</code>
      * @return <code>Locale</code> associated with given authContext
      */
     public String getLocale(AuthContextLocal authContext) {
         // initialize auth service.
         AuthD ad = AuthD.getAuth();
         
         if (authContext == null) {
             return  ad.getPlatformLocale();
         }
         
         LoginState loginState = getLoginState(authContext);
         if (loginState == null) {
             return ad.getPlatformLocale();
         }
         
         return loginState.getLocale();
     }
     
     /**
      * Returns queried organization name in <code>HttpServletRequest</code> 
      * object.
      * @param request <code>HttpServletRequest</code> object to be checked.
      * @param org organization name to get checked from request
      * @return queried organization name in <code>HttpServletRequest</code> 
      * object.
      */
     public static String getQueryOrgName(HttpServletRequest request,
     String org) {
         String queryOrg = null;
         if ((org != null) && (org.length() != 0)) {
             queryOrg = org;
         } else {
             if (request != null) {
                 queryOrg = request.getServerName();
             }
         }
         if (utilDebug.messageEnabled()){
             utilDebug.message("queryOrg is :" + queryOrg);
         }
         return queryOrg;
     }
     
     static void destroySession(LoginState loginState) {
         try {
             if (loginState != null) {
                 loginState.destroySession();
             }
         } catch (Exception e)  {
             utilDebug.message("Error destroySEssion : " , e);
         }
     }
     
     /**
      * Destroys session associated with auth context
      * @param authContext auth context has session
      */
     public void destroySession(AuthContextLocal authContext) {
         if (authContext != null) {
             LoginState loginState = getLoginState(authContext);
             destroySession(loginState);
         }
     }
     
     /**
      * Prints cookies in the request
      * use for debugging purposes
      * @param req <code>HttpServletRequest</code> object has cookie
      */
     public static void printCookies(HttpServletRequest req) {
         Cookie ck[] = req.getCookies();
         if (ck == null) {
             utilDebug.message("No Cookie in header");
             return;
         }
         for (int i = 0; i < ck.length; ++i) {
             if ( utilDebug.messageEnabled()) {
                 utilDebug.message("Received Cookie:" + ck[i].getName() + " = " +
                 ck[i].getValue());
             }
         }
     }
     
     /**
      * Returns <code>true</code> if the session has timed out or the page has
      * timed out.
      *
      * @param authContext Authentication context object for the request.
      * @return <code>true</code> if the session has timed out or the page has
      *         timed out.
      */
     public boolean sessionTimedOut(AuthContextLocal authContext) {
         boolean timedOut = false;
         
         LoginState loginState = getLoginState(authContext);
         
         if (loginState != null) {
             timedOut = loginState.isTimedOut();
             
             if (!timedOut) {
                 com.iplanet.dpro.session.service.InternalSession sess =
                     loginState.getSession();
                 if ((sess == null) && AuthD.isHttpSessionUsed()) {
                     HttpSession hsess = loginState.getHttpSession();
                     timedOut = (hsess == null);
                 } else if (sess != null) {
                     timedOut = sess.isTimedOut();
                 }
                 loginState.setTimedOut(timedOut);
             }
             
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("AuthUtils.sessionTimedOut: " + timedOut);
             }
         }
         return timedOut;
     }
 
     /**
      * Prints reqParameters in <code>Hashtable</code> object
      * @param reqParameters <code>Hashtable</code> object has parameters
      */
     public static void printHash(Hashtable reqParameters) {
         try {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("AuthRequest: In printHash" + reqParameters);
             }
             if (reqParameters == null) {
                 return;
             }
             Enumeration Edata = reqParameters.keys();
             while (Edata.hasMoreElements()) {
                 Object key =  Edata.nextElement();
                 Object value = reqParameters.get(key);
                 utilDebug.message("printHash Key is : " + key);
                 if (value instanceof String[]) {
                     String tmp[] = (String[])value;
                     for (int ii=0; ii < tmp.length; ii++) {
                         if (utilDebug.messageEnabled()) {
                             utilDebug.message("printHash : String[] keyname ("+
                                 key + ") = " + tmp[ii]);
                         }
                     }
                 }
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.warning("Exception: printHash :" , e);
             }
         }
     }
     
     /**
      * Returns the value of argument iPSPCookie entered on the URL
      * @param authContext auth context to be checked
      * @return <code>true</code> if persistent cookie is enabled
      */
     public boolean isPersistentCookieOn(AuthContextLocal authContext) {
         return getLoginState(authContext).isPersistentCookieOn();
     }
     
     /**
      * Returns persistent cookie setting from core auth profile
      * @param authContext auth context to be checked
      * @return <code>true</code> if persistent cookie is enabled
      */
     public boolean getPersistentCookieMode(AuthContextLocal authContext) {
         return getLoginState(authContext).getPersistentCookieMode();
     }
     
     /**
      * Return persistent cookie
      * @param authContext auth context to be checked
      * @param cookieDomain cookie domain associated with auth context
      * @return persistent cookie associated with auth context
      */
     public Cookie getPersistentCookieString(AuthContextLocal authContext,
     String cookieDomain ) {
         return null;
     }
     
     /**
      * Returns the persistent cookie associated with auth context
      * @param authContext auth context to be checked for pcookie
      * @return the persistent cookie associated with auth context
      */
     public String searchPersistentCookie(AuthContextLocal authContext) {
         LoginState loginState = getLoginState(authContext);
         return loginState.searchPersistentCookie();
     }
     
     /**
      * Creates persistent cookie
      * @param authContext auth context associated with pcookie
      * @param cookieDomain cookie domain associated with auth context
      * @return pcookie associated with auth context
      * @throws AuthException if it fails to create pcookie
      */
     public Cookie createPersistentCookie(
         AuthContextLocal authContext,
         String cookieDomain
     ) throws AuthException {
         Cookie pCookie=null;
         try {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("cookieDomain : " + cookieDomain);
             }
             LoginState loginState = getLoginState(authContext);
             pCookie = loginState.setPersistentCookie(cookieDomain);
             return pCookie;
         } catch (Exception e) {
             utilDebug.message("Unable to create persistent Cookie");
             throw new AuthException(AMAuthErrorCode.AUTH_ERROR, null);
         }
     }
     
     /**
      * Creates lb cookie
      * @param authContext auth context associated with lb cookie
      * @param cookieDomain cookie domain associated with auth context
      * @param persist <code>true</code> if it is persistent
      * @return pcookie associated with auth context
      * @throws AuthException if it fails to create pcookie
      */
     public Cookie createlbCookie(
         AuthContextLocal authContext,
         String cookieDomain,
         boolean persist
     ) throws AuthException {
         Cookie lbCookie=null;
         try {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("cookieDomain : " + cookieDomain);
             }
             LoginState loginState = getLoginState(authContext);
             lbCookie = loginState.setlbCookie(cookieDomain, persist);
             return lbCookie;
         } catch (Exception e) {
             utilDebug.message("Unable to create Load Balance Cookie");
             throw new AuthException(AMAuthErrorCode.AUTH_ERROR, null);
         }
         
     }
     
     /**
      * Sets lb cookie to <code>HttpServletResponse</code> object
      * @param authContext auth context associated with lb cookie
      * @param response <code>true</code> if it is persistent
      * @throws AuthException if it fails to create pcookie
      */
     public void setlbCookie(
         AuthContextLocal authContext,
         HttpServletResponse response
     ) throws AuthException {
         String cookieName = getlbCookieName();
         if (cookieName != null && cookieName.length() != 0) {
             Set domains = getCookieDomains();
             if (!domains.isEmpty()) {
                 for (Iterator it = domains.iterator(); it.hasNext(); ) {
                     String domain = (String)it.next();
                     Cookie cookie = createlbCookie(authContext, domain, false);
                     response.addCookie(cookie);
                 }
             } else {
                 response.addCookie(createlbCookie(authContext, null, false));
             }
         }
     }
     
     /**
      * Sets lb cookie to <code>HttpServletResponse</code> object
      * @param response <code>true</code> if it is persistent
      * @throws AuthException if it fails to retrieve cookie
      */
     public void setlbCookie(HttpServletResponse response) throws AuthException {
         String cookieName = getlbCookieName();
         if (cookieName != null && cookieName.length() != 0) {
             Set domains = getCookieDomains();
             if (!domains.isEmpty()) {
                 for (Iterator it = domains.iterator(); it.hasNext(); ) {
                     String domain = (String)it.next();
                     Cookie cookie = createlbCookie(domain);
                     response.addCookie(cookie);
                 }
             } else {
                 response.addCookie(createlbCookie(null));
             }
         }
     }
     
     /**
      * Creates a Cookie with the cookieName , cookieValue for
      * the cookie domains specified.
      *
      * @param cookieName is the name of the cookie.
      * @param cookieValue is the value fo the cookie.
      * @param cookieDomain for which the cookie is to be set.
      * @return the cookie object.
      */
     public Cookie createCookie(
         String cookieName,
         String cookieValue,
         String cookieDomain) {
         if (utilDebug.messageEnabled()) {
             utilDebug.message("cookieName   : " + cookieName);
             utilDebug.message("cookieValue  : " + cookieValue);
             utilDebug.message("cookieDomain : " + cookieDomain);
         }
         
         Cookie cookie = null;
         try {
             // hardcoded need to read from attribute and set cookie
             // for all domains
             cookie = CookieUtils.newCookie(cookieName, cookieValue,
             "/", cookieDomain);
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error creating cookie. : " + e.getMessage());
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("createCookie Cookie is set : " + cookie);
         }
         return cookie;
     }
     
     /**
      * Creates new persistent cookie with given domain and auth context
      * @param cookieDomain cookie domain for cookie
      * @param authContext auth context associated with new cookie
      * @return new cookie
      */
     public Cookie clearPersistentCookie(
         String cookieDomain,
         AuthContextLocal authContext) {
         String pCookieValue = LoginState.encodePCookie();
         int maxAge = 0;
         
         Cookie clearPCookie = createPersistentCookie(getPersistentCookieName(),
         pCookieValue,maxAge,cookieDomain);
         
         return clearPCookie;
     }
     
     /**
      * Creates new lb cookie with given <code>HttpServletResponse</code>response
      * @param response <code>HttpServletResponse</code> response object 
      *        associated with ne wlb cookie
      */
     public void clearlbCookie(HttpServletResponse response){
         String cookieName = getlbCookieName();
         utilDebug.message("clear lb cookie: " + cookieName);
         if (cookieName != null && cookieName.length() != 0) {
             Set domains = getCookieDomains();
             if (!domains.isEmpty()) {
                 for (Iterator it = domains.iterator(); it.hasNext(); ) {
                     String domain = (String)it.next();
                     Cookie cookie =
                     createPersistentCookie(cookieName, "LOGOUT", 0, domain);
                     response.addCookie(cookie);
                 }
             } else {
                 response.addCookie(
                 createPersistentCookie(cookieName, "LOGOUT", 0, null));
             }
         }
     }
     
     /**
      * Returns the indexType for auth context object
      * @param authContext auth context that has indexType
      * @return IndexType from given auth context
      */
     public AuthContext.IndexType getIndexType(AuthContextLocal authContext) {
         
         try {
             AuthContext.IndexType indexType = null;
             LoginState loginState = getLoginState(authContext);
             
             if (loginState != null) {
                 indexType = loginState.getIndexType();
             }
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("in getIndexType, index type : " + indexType);
             }
             return indexType;
         } catch (Exception e) {
             utilDebug.message("ERROR in getIndexType : " , e);
             return null;
         }
     }
     
     /**
      * Returns array of Callback that are associated with given auth context
      * @param authContext auth context that has array of Callback
      * @return array of Callback that are associated with given auth context
      */
     public Callback[] getRecdCallback(AuthContextLocal authContext) {
         LoginState loginState = getLoginState(authContext);
         Callback[] recdCallback = null;
         if (loginState != null) {
             recdCallback = loginState.getRecdCallback();
         }
         
         if ( recdCallback != null ) {
             if (utilDebug.messageEnabled()) {
                 for (int i = 0; i < recdCallback.length; i++) {
                     utilDebug.message(
                         "in getRecdCallback, recdCallback[" + i + "] :"
                         + recdCallback[i]);
                 }
             }
         }
         else {
             utilDebug.message("in getRecdCallback, recdCallback is null");
         }
         
         return recdCallback;
     }
     
     /**
      * Returns the the error message for given error code
      * @param errorCode error code
      * @return the the error message for given error code
      */
     public String getErrorMessage(String errorCode) {
         String errorMessage = getErrorVal(errorCode,ERROR_MESSAGE);
         return errorMessage;
     }
     
     /**
      * Returns the the error template for the error code
      * @param errorCode 
      * @return the the error template for the error code
      */
     public String getErrorTemplate(String errorCode) {
         String errorTemplate = getErrorVal(errorCode,ERROR_TEMPLATE);
         return errorTemplate;
     }
     
     /**
      * Returns the resource based on the default values.
      *
      * @param request Reference to HTTP Servlet Request object.
      * @param fileName Name of the file
      * @return Path to the resource.
      */
     public String getDefaultFileName(
         HttpServletRequest request,
         String fileName) {
         // initialize auth service.
         AuthD ad = AuthD.getAuth();
         
         String locale = ad.getPlatformLocale();
         String filePath = getFilePath(getClientType(request));
         String fileRoot = ISAuthConstants.DEFAULT_DIR;
         
         String templateFile = null;
         try {
             templateFile = ResourceLookup.getFirstExisting(
             ad.getServletContext(),
             fileRoot,locale,null,filePath,fileName,
             templatePath,true);
         } catch (Exception e) {
             templateFile = templatePath + fileRoot +
                 Constants.FILE_SEPARATOR + fileName;
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getDefaultFileName:templateFile is :" +
             templateFile);
         }
         return templateFile;
     }
     
     /**
      * Returns the orgDN for given <code>AuthContextLocal</code> object
      * @param authContext to get checked
      * @return the orgDN for given <code>AuthContextLocal</code> object
      */
     public String getOrgDN(AuthContextLocal authContext) {
         String orgDN = null;
         LoginState loginState = getLoginState(authContext);
         if (loginState != null) {
             orgDN = loginState.getOrgDN();
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("orgDN is : " + orgDN);
         }
         return orgDN;
     }
     
     /**
      * Returns auth context for given organization
      * @param orgName for new auth context
      * @return auth context for given organization
      * @throws AuthException if it fails to create <code>AuthContext</code>
      */
     public static AuthContextLocal getAuthContext(String orgName)
             throws AuthException {
         return getAuthContext(orgName,"0",false, null);
     }
     
     /**
      * Returns auth context for given organization and sessionID
      * @param orgName for new auth context
      * @param sessionID for new auth context
      * @return auth context for given organization and sessionID
      * @throws AuthException if it fails to create <code>AuthContext</code>
      */
     public static AuthContextLocal getAuthContext(
         String orgName,
         String sessionID
     ) throws AuthException {
         return getAuthContext(orgName,sessionID,false, null);
     }
     
     /**
      * Returns auth context for given organization and sessionID
      * @param orgName for new auth context
      * @param req <code>HttpServletRequest</code> object has session 
      *        for new auth context
      * @return auth context for given organization and session in request
      * @throws AuthException if it fails to create <code>AuthContext</code>
      */
     public static AuthContextLocal getAuthContext(
         String orgName,
         HttpServletRequest req
     ) throws AuthException {
         return getAuthContext(orgName, "0", false, req);
     }
     
     /**
      * Returns auth context for given organization and sessionID
      * @param orgName for new auth context
      * @param sessionID for new auth context
      * @param logout Logout request - if yes then no session.
      * @return auth context for given organization and session in request
      * @throws AuthException if it fails to create <code>AuthContext</code>
      */
     public static AuthContextLocal getAuthContext(
         String orgName,
         String sessionID,
         boolean logout
     ) throws AuthException {
         return getAuthContext(orgName, sessionID, logout, null);
     }
     
     /**
      * Returns auth context for given <code>HttpServletRequest</code> object
      * and sessionID.
      * @param req for new auth context
      * @param sessionID for new auth context
      * @return auth context for given organization and session in request
      * @throws AuthException if it fails to create <code>AuthContext</code>
      */
     public static AuthContextLocal getAuthContext(
         HttpServletRequest req,
         String sessionID
     ) throws AuthException {
         return getAuthContext(null, sessionID, false, req);
     }
     
     /**
      * Creates authentication context for organization and session ID, if
      * <code>sessionupgrade</code> then save the previous authentication
      * context and create new authentication context.
      *
      * @param orgName organization name to login too
      * @param sessionID sessionID of the request - "0" if new request
      * @param isLogout Logout request - if yes then no session.
      * @param req HTTP Servlet Request.
      * @return the created authentication context.
      * @throws AuthException if it fails to create <code>AuthContext</code>
      */
     public static AuthContextLocal getAuthContext(
         String orgName,
         String sessionID,
         boolean isLogout,
         HttpServletRequest req
     ) throws AuthException {
         AuthContextLocal authContext = null;
         SessionID sid = null;
         LoginState loginState = null;
         boolean sessionUpgrade = false;
         AuthD ad = AuthD.getAuth();
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("orgName : " + orgName);
             utilDebug.message("sessionID is " + sessionID);
             utilDebug.message("sessionID is " + sessionID.length());
             utilDebug.message("isLogout : " + isLogout);
         }
         try {
             if ((sessionID != null) && (!sessionID.equals("0"))) {
                 sid = new SessionID(sessionID);
                 authContext = retrieveAuthContext(req, sid);
                 
                 // check if this sesson id is active, if yes then it
                 // is a session upgrade case.
                 LoginState prevLoginState = getLoginState(authContext);
                 com.iplanet.dpro.session.service.InternalSession sess = null;
                 if (prevLoginState != null) {
                     sess = prevLoginState.getSession();
                 }
                 if (sess == null) {
                     sessionUpgrade = false;
                 } else {
                     int sessionState = sess.getState();
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("sid from sess is : " + sess.getID());
                         utilDebug.message("sess is : " + sessionState);
                     }
                     sessionUpgrade = true;
                     if ((sessionState == Session.INVALID)  || (isLogout)) {
                         sessionUpgrade = false;
                     }
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message(
                             "session upgrade is : "+ sessionUpgrade);
                     }
                 }
             }
             
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("AuthUtil:getAuthContext:sid is.. .: " + sid);
                 utilDebug.message("AuthUtil:getAuthContext:authContext is.. .: "
                     + authContext);
                 utilDebug.message(
                     "AuthUtil:getAuthContext:sessionUpgrade is.. .: "
                     + sessionUpgrade);
             }
             
             if ((orgName == null) && (authContext == null)) {
                 utilDebug.error("Cannot create authcontext with null org " );
                 throw new AuthException(AMAuthErrorCode.AUTH_ERROR, null);
             }
             
             if ((orgName != null) && ((authContext ==null) || (sessionUpgrade))
             ) {
                 try {
                     loginState = new LoginState();
                     if (sessionUpgrade) {
                         loginState.setPrevAuthContext(authContext);
                         loginState.setSessionUpgrade(sessionUpgrade);
                     }
                     
                     authContext = loginState.createAuthContext(sid,orgName,req);
                     authContext.setLoginState(loginState);
                     String queryOrg = getQueryOrgName(null,orgName);
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("query org is .. : "+ queryOrg);
                     }
                     loginState.setQueryOrg(queryOrg);
                 } catch (AuthException ae) {
                     utilDebug.message("Error creating AuthContextLocal 2: ");
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("Exception " , ae);
                     }
                     throw new AuthException(ae);
                 }
             } else {
                 // update loginState
                 try {
                     com.iplanet.dpro.session.service.InternalSession
                     requestSess = AuthD.getSession(sessionID);
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message(
                             "AuthUtil :Session is .. : " + requestSess);
                     }
                     loginState = getLoginState(authContext);
                     loginState.setSession(requestSess);
                     loginState.setRequestType(false);
                 } catch (Exception ae) {
                     utilDebug.message("Error Retrieving AuthContextLocal" );
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("Exception " , ae);
                     }
                     throw new AuthException(AMAuthErrorCode.AUTH_ERROR, null);
                 }
                 
             }
             
             
         } catch (Exception ee) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message(
                     "Error creating AuthContextLocal 2: " + ee.getMessage());
             }
             
             throw new AuthException(ee);
         }
         return authContext;
     }
     
     /**
      * Returns a set of authentication modules whose authentication
      * level equals to or greater than the specified authentication Level. If
      * no such module exists, an empty set will be returned.
      *
      * @param authLevel authentication level.
      * @param organizationDN  DN for the organization.
      * @param clientType Client type, e.g. <code>genericHTML</code>.
      * @return Set of authentication modules whose authentication level equals
      *         to or greater that the specified authentication Level.
      */
     public static Set getAuthModules(
         int authLevel,
         String organizationDN,
         String clientType) {
         return AMAuthLevelManager.getInstance().getModulesForLevel(authLevel,
         organizationDN, clientType);
     }
     
     /**
      * Returns the previous authcontext for given <code>AuthContextLocal</code>
      *  object.
      * @param authContext auth context previous authcontext
      * @return the previous authcontext for given <code>AuthContextLocal</code>
      *  object.
      */
     public AuthContextLocal getPrevAuthContext(AuthContextLocal authContext) {
         LoginState loginState = getLoginState(authContext);
         AuthContextLocal oldAuthContext = loginState.getPrevAuthContext();
         return oldAuthContext;
     }
     
     /**
      * Returns the previous LoginState for the authconext 
      * @param oldAuthContext old auth context associated with 
      *        <code>LoginState</code> object
      * @return <code>LoginState</code> object associated with given authconext 
      */
     public LoginState getPrevLoginState(AuthContextLocal oldAuthContext) {
         return getLoginState(oldAuthContext);
     }
     
     /**
      * Returns the auth context based on the <code>SessionID</code> object.
      * @param sid <code>SessionID</code> object associated with auth context
      * @return <code>AuthContextLocal</code> object associated with given
      *         <code>SessionID</code> object
      * @throws AuthException if it fails to create with given 
      *         <code>SessionID</code> object
      */
     public AuthContextLocal getOrigAuthContext(SessionID sid)
             throws AuthException {
         AuthContextLocal authContext = null;
         // initialize auth service.
         AuthD ad = AuthD.getAuth();
         try {
             authContext = retrieveAuthContext(sid);
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("AuthUtil:getOrigAuthContext:sid is.:"+sid);
                 utilDebug.message("AuthUtil:getOrigAuthContext:authContext is:"
                 + authContext);
             }
             com.iplanet.dpro.session.service.InternalSession sess =
             getLoginState(authContext).getSession();
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Session is : "+ sess);
                 if (sess != null) {
                     utilDebug.message("Session State is : "+ sess.getState());
                 }
                 utilDebug.message("Returning Orig AuthContext:"+authContext);
             }
             
             if (sess == null) {
                 return null;
             } else {
                 int status = sess.getState();
                 if (status == Session.INVALID){
                     return null;
                 }
                 return authContext;
             }
             
         } catch (Exception e) {
             return null;
         }
     }
     
     /**
      * Check if the session is active for given <code>AuthContextLocal</code>
      * @param oldAuthContext auth context has associated session
      * @return <code>true</code> if associated session is active
      */
     public boolean isSessionActive(AuthContextLocal oldAuthContext) {
         try {
             com.iplanet.dpro.session.service.InternalSession sess =
             getSession(oldAuthContext);
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Sess is : " + sess);
             }
             boolean sessionValid = false;
             if (sess != null) {
                 if (sess.getState() == Session.VALID) {
                     sessionValid = true;
                 }
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("Sess State is : " + sess.getState());
                     utilDebug.message("Is Session Active : " + sessionValid);
                 }
             }
             return sessionValid;
         } catch (Exception e) {
             return false;
         }
     }
     
     /**
      * Returns session property for give property name from 
      * <code>AuthContextLocal</code> object
      * @param property session property name to return
      * @param oldAuthContext auth context object has sessionproperty
      * @return session property value for given property name
      */
     public String getSessionProperty(
         String property,
         AuthContextLocal oldAuthContext) {
         String value = null;
         try {
             com.iplanet.dpro.session.service.InternalSession sess =
             getSession(oldAuthContext);
             if (sess != null) {
                 value = sess.getProperty(property);
             }
         } catch (Exception e) {
             utilDebug.message("Error : " ,e);
         }
         return value;
     }
     
     /**
      * Returns session upgrade for given <code>AuthContextLocal</code> object
      * @param authContext auth context object to get checked
      * @return <code>true</code> if session upgrade for given auth context
      */
     public boolean isSessionUpgrade(AuthContextLocal authContext) {
         boolean isSessionUpgrade = false;
         LoginState loginState =  getLoginState(authContext);
         if (loginState != null) {
             isSessionUpgrade = loginState.isSessionUpgrade();
         }
         return isSessionUpgrade;
     }
     
     /**
      * Sets true or false for given <code>AuthContextLocal</code> object
      * @param ac auth context to be set CookieSupported flag
      * @param flag true if CookieSupported is true
      */
     public void setCookieSupported(AuthContextLocal ac, boolean flag) {
         LoginState loginState =  getLoginState(ac);
         if (loginState==null) {
             return;
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("set cookieSupported to : " + flag);
             utilDebug.message("set cookieDetect to false");
         }
         loginState.setCookieSupported(flag);
     }
     
     /**
      * Returns CookieSupported flag for given 
      * <code>AuthContextLocal</code> object
      * @param ac auth context to be checked for CookieSupported flag
      * @return <code>true</code> if CookieSupported is true
      */
     public boolean isCookieSupported(AuthContextLocal ac) {
         LoginState loginState =  getLoginState(ac);
         if (loginState==null) {
             return false;
         }
         return loginState.isCookieSupported();
     }
     
     /**
      * Returns true if cookie is set for given <code>AuthContextLocal</code> 
      * object.
      * @param ac auth context to be checked for cookie
      * @return <code>true</code> if cookie is set for given 
      * <code>AuthContextLocal</code> object.
      */
     public boolean isCookieSet(AuthContextLocal ac) {
         LoginState loginState =  getLoginState(ac);
         if (loginState==null) {
             return false;
         }
         return loginState.isCookieSet();
     }
     
     /**
      * Returns <code>true</code> if cookies found in the request.
      *
      * @param req HTTP Servlet Request.
      * @param ac Authentication Context.
      * @return <code>true</code> if cookies found in request.
      */
     public boolean checkForCookies(HttpServletRequest req, AuthContextLocal ac){
         LoginState loginState =  getLoginState(ac);
         if (loginState!=null) {
             utilDebug.message("set cookieSet to false.");
             loginState.setCookieSet(false);
             loginState.setCookieDetect(false);
         }
         // came here if cookie not found , return false
         return (
         (CookieUtils.getCookieValueFromReq(req,getAuthCookieName()) != null)
         ||
         (CookieUtils.getCookieValueFromReq(req,getCookieName()) !=null));
     }
     
     /**
      * Checks if <code>HttpServletRequest</code> object has configured name of
      * cookie.
      * @param req <code>HttpServletRequest</code> to get checked for cookie name
      * @return <code>true</code> if <code>HttpServletRequest</code> object 
      * has configured name of cookie.
      */
     public static boolean checkForCookies(HttpServletRequest req) {
         // came here if cookie not found , return false
         return (
         (CookieUtils.getCookieValueFromReq(req,getAuthCookieName()) != null)
         ||
         (CookieUtils.getCookieValueFromReq(req,getCookieName()) !=null));
     }
     
     /**
      * Returns login url for given <code>AuthContextLocal</code> object.
      * @param authContext auth context to be checked for url 
      * @return login url for given <code>AuthContextLocal</code> object.
      */
     public String getLoginURL(AuthContextLocal authContext) {
         LoginState loginState =  getLoginState(authContext);
         if (loginState==null) {
             return null;
         }
         return loginState.getLoginURL();
     }
     
     /**
      * Returns the flag indicating a request "forward" after
      * successful authentication.      
      *
      * @param authContext AuthContextLocal object
      * @param req HttpServletRequest object
      * @return the boolean flag.
      */
     public boolean isForwardSuccess(AuthContextLocal authContext, 
         HttpServletRequest req) {
         boolean isForward = forwardSuccessExists(req);
         if (!isForward) {
             LoginState loginState = getLoginState(authContext);
             if (loginState != null) {
                 isForward = loginState.isForwardSuccess();
             } 
         }
         return isForward;
     }
     
     /**
      * Returns <code>true</code> if the request has the
      * <code>forward=true</code> query parameter.
      *
      * @param req HttpServletRequest object
      * @return <code>true</code> if this parameter is present.
      */
     public boolean forwardSuccessExists(HttpServletRequest req) {
         String forward = req.getParameter("forward");
         boolean isForward =
             (forward != null) && forward.equals("true");
         if (utilDebug.messageEnabled()) {
             utilDebug.message("forwardSuccessExists : "+ isForward);
         }
         return isForward;
     }
 
     /**
      * Returns <code>AuthContextLocal</code> object for <code>SessionID</code>
      * object.
      * @param sid <code>SessionID</code> object for 
      * <code>AuthContextLocal</code> object.
      * @return <code>AuthContextLocal</code> object associated with given sid.
      */
     public static AuthContextLocal getAuthContextFromHash(SessionID sid) {
         AuthContextLocal authContext = null;
         if (sid != null) {
             authContext = retrieveAuthContext(sid);
         }
         return authContext;
     }
     
     /**
      * Returns Original Redirect URL for Auth to redirect the Login request
      * @param request <code>HttpServletRequest</code> object to be checked for
      *        Redirect URL.
      * @param sessID sessionID to be checked if valid
      * @return Original Redirect URL for Auth to redirect the Login request
      */
     public String getOrigRedirectURL(HttpServletRequest request,
     SessionID sessID) {
         try {
             String sidString = null;
             if (sessID != null) {
                 sidString = sessID.toString();
             }
             SSOTokenManager manager = SSOTokenManager.getInstance();
             SSOToken ssoToken = manager.createSSOToken(sidString);
             if (manager.isValidToken(ssoToken)) {
                 utilDebug.message("Valid SSOToken");
                 String origRedirectURL = ssoToken.getProperty("successURL");
                 String gotoURL = request.getParameter("goto");
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message(
                         "Original successURL : " + origRedirectURL);
                     utilDebug.message("Request gotoURL : " + gotoURL);
                 }
                 if ((gotoURL != null) && (gotoURL.length() != 0)){
                     origRedirectURL = gotoURL;
                 }
                 return origRedirectURL;
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error in getOrigRedirectURL:"+ e.toString());
             }
             return null;
         }
         return null;
     }
     
     /**
      * Returns array of callbacks per Page state
      * @param authContext auth context object to be checked for callbacks
      * @param pageState page state to be checked for callbacks
      * @return array of callbacks per Page state
      */
     public Callback[] getCallbacksPerState(
         AuthContextLocal authContext,
         String pageState) {
         LoginState loginState = getLoginState(authContext);
         Callback[] recdCallback = null;
         if (loginState != null) {
             recdCallback = loginState.getCallbacksPerState(pageState);
         }
         if ( recdCallback != null ) {
             if (utilDebug.messageEnabled()) {
                 for (int i = 0; i < recdCallback.length; i++) {
                     utilDebug.message(
                         "in getCallbacksPerState, recdCallback[" + i + "] :"
                         + recdCallback[i]);
                 }
             }
         }
         else {
             utilDebug.message("in getCallbacksPerState, recdCallback is null");
         }
         return recdCallback;
     }
     
     /**
      * Sets callbacks per Page state
      * @param authContext <code>AuthContextLocal</code> object to be set 
      *        callbacks
      * @param pageState page state to be set with callbacks
      * @param callbacks to be set in <code>AuthContextLocal</code> object
      */
     public void setCallbacksPerState(
         AuthContextLocal authContext,
         String pageState,
         Callback[] callbacks) {
         LoginState loginState = getLoginState(authContext);
         
         if (loginState != null) {
             loginState.setCallbacksPerState(pageState, callbacks);
         }
         if ( callbacks != null ) {
             if (utilDebug.messageEnabled()) {
                 for (int i = 0; i < callbacks.length; i++) {
                     utilDebug.message(
                         "in setCallbacksPerState, callbacks[" + i + "] :"
                         + callbacks[i]);
                 }
             }
         }
         else {
             utilDebug.message("in setCallbacksPerState, callbacks is null");
         }
     }
     
     /**
      * Adds Logout cookie to URL.
      *
      * @param url The URL to be rewritten with the logout cookie.
      * @param logoutCookie Logout cookie String.
      * @param isCookieSupported <code>true</code> if cookie is supported.
      * @return a URL with the logout cookie appended to the URL.
      */
     public static String addLogoutCookieToURL(
         String url,
         String logoutCookie,
         boolean isCookieSupported) {
         String logoutURL = null;
         if ((logoutCookie == null) || (isCookieSupported)) {
             logoutURL = url;
         } else {
             
             StringBuffer cookieString = new StringBuffer();
             
             cookieString.append(URLEncDec.encode(getCookieName()))
                 .append("=").append(URLEncDec.encode(logoutCookie));
             
             StringBuffer encodedURL = new StringBuffer();
             if (url.indexOf("?") != -1) {
                 cookieString.insert(0,"&amp;");
             } else {
                 cookieString.insert(0,"?");
             }
             
             cookieString.insert(0,url);
             logoutURL = cookieString.toString();
             
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("cookieString is : "+ cookieString);
             }
         }
         
         /*if (utilDebug.messageEnabled()) {
          *  utilDebug.message("logoutURL is : "+ logoutURL);
          *}
          */
         
         return logoutURL;
     }
     
     /**
      * Returns the SessionID . This is required to added the
      * session server , port , protocol info to the Logout Cookie.
      * SessionID is retrieved from Auth service if a handle on
      * the authcontext object is there otherwise retrieve from
      * the request object.
      *
      * @param authContext Authentication context which is handle to the
      *        authentication service.
      * @param request HTTP Servlet Request.
      * @return the Session ID.
      */
     public SessionID getSidValue(
         AuthContextLocal authContext,
         HttpServletRequest request) {
         SessionID sessionId = null;
         if (authContext != null)  {
             utilDebug.message("AuthContext is not null");
             try {
                 String sid = getSidString(authContext);
                 if (sid != null) {
                     sessionId = new SessionID(sid);
                 }
             } catch (Exception e) {
                 utilDebug.message("Exception getting sid",e);
             }
         }
         
         if (sessionId == null) {
             utilDebug.message("Sid from AuthContext is null");
             sessionId = new SessionID(request);
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("sid is : " + sessionId);
         }
         return sessionId;
     }
     
     /**
      * Returns <code>true</code> if cookie is supported.
      * the value is retrieved from the authentication service if a
      * handle on the authentication context object is there otherwise
      * check the HTTP Servlet Request object to see if the
      * Access Manager cookie is in the request header.
      *
      * @param authContext is the handle to the authentication service
      *             for the request.
      * @param request HTTP Servlet Request for the request.
      * @return <code>true</code> if cookie is supported.
      */
     public boolean isCookieSupported(
         AuthContextLocal authContext,
         HttpServletRequest request) {
         boolean cookieSupported;
         if (authContext != null)  {
             utilDebug.message("AuthContext is not null");
             cookieSupported = isCookieSupported(authContext);
         } else {
             cookieSupported = checkForCookies(request,null);
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Cookie supported" + cookieSupported);
         }
         return cookieSupported;
     }
     
     /**
      * Returns the previous index type after module is selected in authlevel
      * or composite advices.
      *
      * @param ac Authentication Context instance.
      * @return Previous index type.
      */
     public AuthContext.IndexType getPrevIndexType(AuthContextLocal ac) {
         LoginState loginState = getLoginState(ac);
         if (loginState != null) {
             return loginState.getPreviousIndexType();
         } else {
             return null;
         }
     }
     
     /**
      * Returns whether the auth module is or the auth chain contains pure JAAS
      * module(s).
      *
      * @param configName Configuratoin name.
      * @param amlc
      * @return 1 for pure JAAS module; -1 for module(s) provided by IS only.
      * @throws AuthLoginException if it fails to check 
      *         <code>AMLoginContext</code>
      */
     public static int isPureJAASModulePresent(
         String configName,
         AMLoginContext amlc
     ) throws AuthLoginException {
         
         if (AuthD.enforceJAASThread) {
             return 1;
         }
         int returnValue = -1;
         
         Configuration ISConfiguration = null;
         try {
             ISConfiguration = Configuration.getConfiguration();
         } catch (Exception e) {
             return 1;
         }
         
         AppConfigurationEntry[] entries =
         ISConfiguration.getAppConfigurationEntry(configName);
         if (entries == null) {
             throw new AuthLoginException("amAuth",
             AMAuthErrorCode.AUTH_CONFIG_NOT_FOUND, null);
         }
         // re-use the obtained configuration
         amlc.setConfigEntries(entries);
         
         for (int i = 0; i < entries.length; i++) {
             String className = entries[i].getLoginModuleName();
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("config entry: " + className);
             }
             if (pureJAASModuleClasses.contains(className)) {
                 returnValue = 1;
                 break;
             } else if (ISModuleClasses.contains(className)) {
                 continue;
             }
             
             try {
                 Object classObject = Class.forName(className, true,
                     Thread.currentThread().getContextClassLoader()
                     ).newInstance();
                 if (classObject instanceof AMLoginModule) {
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message(className +
                         " is instance of AMLoginModule");
                     }
                     synchronized(ISModuleClasses) {
                         if (! ISModuleClasses.contains(className)) {
                             ISModuleClasses.add(className);
                         }
                     }
                 } else {
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message(className + " is a pure jaas module");
                     }
                     synchronized(pureJAASModuleClasses) {
                         if (! pureJAASModuleClasses.contains(className)) {
                             pureJAASModuleClasses.add(className);
                         }
                     }
                     returnValue = 1;
                     break;
                 }
             } catch (Exception e) {
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("fail to instantiate class for " +
                     className);
                 }
                 synchronized(pureJAASModuleClasses) {
                     if (! pureJAASModuleClasses.contains(className)) {
                         pureJAASModuleClasses.add(className);
                     }
                 }
                 returnValue = 1;
                 break;
             }
         }
         return returnValue;
     }
     
     /**
      * Returns the module service name in either
      * <code>iplanet-am-auth format&lt;module.toLowerCase()>Service(old)</code>
      * or <code>sunAMAuth<module>Service format(new)</code>.
      * @param moduleName module name for configured module service name
      * @return Module service name.
      */
     public static String getModuleServiceName(String moduleName) {
         String serviceName = (String) moduleService.get(moduleName);
         if (serviceName == null) {
             serviceName = AMAuthConfigUtils.getModuleServiceName(moduleName);
             try {
                 SSOToken token = (SSOToken) AccessController.doPrivileged(
                 AdminTokenAction.getInstance());
                 new ServiceSchemaManager(serviceName, token);
             } catch (Exception e) {
                 serviceName = AMAuthConfigUtils.getNewModuleServiceName(
                 moduleName);
             }
             moduleService.put(moduleName, serviceName);
         }
         return serviceName;
     }
    
     /**
      * Returns Auth service revision number 
      * @return Auth service revision number 
      */
     public static int getAuthRevisionNumber(){
         try {
             SSOToken token = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
             ServiceSchemaManager scm = new ServiceSchemaManager(
             ISAuthConstants.AUTH_SERVICE_NAME, token);
             return scm.getRevisionNumber();
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("getAuthRevisionNumber error", e);
             }
         }
         return 0;
     }
     
     /**
      * Returns the Session ID for the request.
      * The cookie in the request for invalid sessions is in authentication
      * cookie, <code>com.iplanet.am.auth.cookie</code>,  and for
      * active/inactive sessions in <code>com.iplanet.am.cookie</code>.
      *
      * @param request HTTP Servlet Request.
      * @return Session ID for this request.
      */
     private static SessionID getSidFromCookie(HttpServletRequest request) {
         SessionID sessionID = null;
         /// get auth cookie
         String authCookieName = getAuthCookieName();
         String sidValue =
         CookieUtils.getCookieValueFromReq(request,authCookieName);
         if (sidValue == null) {
             sidValue =
             SessionEncodeURL.getSidFromURL(request,authCookieName);
         }
         if (sidValue != null) {
             sessionID = new SessionID(sidValue);
             utilDebug.message("sidValue from Auth Cookie");
         }
         return sessionID;
     }
     
     /**
      * Returns the Session ID for this request. If Authentication Cookie and
      * Valid AM Cookie are there and request method is GET then use Valid
      * AM Cookie else use Auth Cookie. The cookie in the request for invalid
      * sessions is in authentication cookie,
      * <code>com.iplanet.am.auth.cookie</code>, and for active/inactive
      * sessions in <code>com.iplanet.am.cookie</code>.
      *
      *  @param request HTTP Servlet Request.
      *  @return Session ID for this request.
      */
     public SessionID getSessionIDFromRequest(HttpServletRequest request) {
         boolean isGetRequest= (request !=null &&
         request.getMethod().equalsIgnoreCase("GET"));
         SessionID amCookieSid = new SessionID(request);
         SessionID authCookieSid = getSidFromCookie(request);
         SessionID sessionID;
         if (authCookieSid == null) {
             sessionID = amCookieSid;
         } else {
             if (isGetRequest) {
                 sessionID = amCookieSid;
             } else {
                 sessionID = authCookieSid;
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("AuthUtils:returning sessionID:" + sessionID);
         }
         return sessionID;
     }
     
     /**
      * Returns success URL for this request.
      * If goto parameter is in the current request then returns the goto
      * parameter else returns the success URL set in the valid session.
      *
      * @param request HTTP Servlet Request.
      * @param authContext Authentication Context for this request.
      * @return Success URL.
      */
     public String getSuccessURL(
         HttpServletRequest request,
         AuthContextLocal authContext) {
         String successURL = null;
         if (request != null) {
             successURL = request.getParameter("goto");
         }
         if ((successURL == null) || (successURL.length() == 0) ||
         (successURL.equalsIgnoreCase("null")) ) {
             LoginState loginState = getLoginState(authContext);
             if (loginState == null) {
                 successURL = getSessionProperty("successURL",authContext);
             } else {
                 successURL =
                 getLoginState(authContext).getConfiguredSuccessLoginURL();
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getSuccessURL : " + successURL);
         }
         return successURL;
     }
     
     /**
      * Returns <code>true</code> if the request has the
      * <code>arg=newsession</code> query parameter.
      *
      * @param reqDataHash
      * @return <code>true</code> if this parameter is present.
      */
     public boolean newSessionArgExists(Hashtable reqDataHash) {
         String arg = (String) reqDataHash.get("arg");
         boolean newSessionArgExists =
         (arg != null) && arg.equals("newsession");
         if (utilDebug.messageEnabled()) {
             utilDebug.message("newSessionArgExists : " + newSessionArgExists);
         }
         return newSessionArgExists;
     }
     
     /**
      * Returns the AuthContext.IndexType for given string index type value
      * @param strIndexType string index type value
      * @return the AuthContext.IndexType for given string index type value
      */
     public AuthContext.IndexType getIndexType(String strIndexType) {
         AuthContext.IndexType indexType = null;
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getIndexType : strIndexType = " + strIndexType);
         }
         if (strIndexType != null) {
             if (strIndexType.equalsIgnoreCase("user")) {
                 indexType = AuthContext.IndexType.USER;
             } else if (strIndexType.equalsIgnoreCase("role")) {
                 indexType = AuthContext.IndexType.ROLE;
             } else if (strIndexType.equalsIgnoreCase("service")) {
                 indexType = AuthContext.IndexType.SERVICE;
             } else if (strIndexType.equalsIgnoreCase("module_instance")) {
                 indexType = AuthContext.IndexType.MODULE_INSTANCE;
             } else if (strIndexType.equalsIgnoreCase("level")) {
                 indexType = AuthContext.IndexType.LEVEL;
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getIndexType : IndexType = " + indexType);
         }
         return indexType;
     }
     
     /**
      * Returns the index name given index type from the existing valid session
      * @param ssoToken ssotoken has valid session
      * @param indexType index type gets retrieved from ssotoken
      * @return the index name given index type from the existing valid session
      */
     public String getIndexName(
         SSOToken ssoToken,
         AuthContext.IndexType indexType) {
         String indexName = "";
         try {
             if (indexType == AuthContext.IndexType.USER) {
                 indexName = ssoToken.getProperty("UserToken");
             } else if (indexType == AuthContext.IndexType.ROLE) {
                 indexName = ssoToken.getProperty("Role");
             } else if (indexType == AuthContext.IndexType.SERVICE) {
                 indexName = ssoToken.getProperty("Service");
             } else if (indexType == AuthContext.IndexType.MODULE_INSTANCE) {
                 indexName =getLatestIndexName(ssoToken.getProperty("AuthType"));
             } else if (indexType == AuthContext.IndexType.LEVEL) {
                 indexName = ssoToken.getProperty("AuthLevel");
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error in getIndexName :"+ e.toString());
             }
             return indexName;
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getIndexName : IndexType = " + indexType);
             utilDebug.message("getIndexName : IndexName = " + indexName);
         }
         return indexName;
     }
     
     // Get the first or latest index name from the string of index names
     // separated by "|".
     private String getLatestIndexName(String indexName) {
         String firstIndexName = indexName;
         if (indexName != null) {
             StringTokenizer st = new StringTokenizer(indexName,"|");
             if (st.hasMoreTokens()) {
                 firstIndexName = (String)st.nextToken();
             }
         }
         return firstIndexName;
     }
     
     // search valve in the String
     private boolean isContain(String value, String key) {
         if (value == null) {
             return false;
         }
         try {
             if (value.indexOf("|") != -1) {
                 StringTokenizer st = new StringTokenizer(value, "|");
                 while (st.hasMoreTokens()) {
                     if ((st.nextToken()).equals(key)) {
                         return true;
                     }
                 }
             } else {
                 if (value.trim().equals(key.trim())) {
                     return true;
                 }
             }
         } catch (Exception e) {
             utilDebug.error("error : " + e.toString());
         }
         return false;
     }
     
     /**
      * Checks if this is Session Upgrade case for given ssoToken and request 
      * parameters
      * @param ssoToken ssotoken object to get checked for session upgrade
      * @param reqDataHash hash object has request parameters
      * @return <code>true</code> if this is Session Upgrade case for given 
      * ssoToken and request parameters
      */
     public boolean checkSessionUpgrade(SSOToken ssoToken,Hashtable reqDataHash){
         utilDebug.message("Check Session upgrade!");
         String tmp = null;
         String value = null;
         boolean upgrade = false;
         try {
             if (reqDataHash.get("user")!=null) {
                 tmp = (String) reqDataHash.get("user");
                 value = ssoToken.getProperty("UserToken");
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("user : " + tmp);
                     utilDebug.message("userToken : " + value);
                 }
                 if (!tmp.equals(value)) {
                     upgrade = true;
                 }
             } else if (reqDataHash.get("role")!=null) {
                 tmp = (String) reqDataHash.get("role");
                 value = ssoToken.getProperty("Role");
                 if (!isContain(value, tmp)) {
                     upgrade = true;
                 }
             } else if (reqDataHash.get("service")!=null) {
                 tmp = (String) reqDataHash.get("service");
                 value = ssoToken.getProperty("Service");
                 if (!isContain(value, tmp)) {
                     upgrade = true;
                 }
             } else if (reqDataHash.get("module")!=null) {
                 tmp = (String) reqDataHash.get("module");
                 value = ssoToken.getProperty("AuthType");
                 if (!isContain(value, tmp)) {
                     upgrade = true;
                 }
             } else if (reqDataHash.get("authlevel")!=null) {
                 int i = Integer.parseInt((String)reqDataHash.get("authlevel"));
                 if (i>Integer.parseInt(ssoToken.getProperty("AuthLevel"))) {
                     upgrade = true;
                 }
             } else if ( reqDataHash.get(Constants.COMPOSITE_ADVICE) != null ) {
                 tmp = (String)reqDataHash.get(Constants.COMPOSITE_ADVICE);
                 String orgName = ssoToken.getProperty("Organization");
                 String clientType = ssoToken.getProperty("clientType");
                 Set moduleInstances =
                 processCompositeAdviceXML(tmp, orgName, clientType);
                 value = ssoToken.getProperty("AuthType");
                 if ((moduleInstances != null) && (!moduleInstances.isEmpty())) {
                     Iterator iter = moduleInstances.iterator();
                     while (iter.hasNext()) {
                         // get the module instance name
                         String moduleName = (String) iter.next();
                         if (!isContain(value, moduleName)) {
                             upgrade = true;
                             break;
                         }
                     }
                 }
             }
         } catch (Exception e) {
             utilDebug.message("Exception in checkSessionUpgrade : " + e);
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Check session upgrade : " + upgrade);
         }
         return upgrade;
     }
     
     /**
      * Returns the set of Module instances resulting from a 'composite advice'
      * for given compositeAdvice and orgDN, clientType
      * @param xmlCompositeAdvice composite advice in xml
      * @param orgDN organization DN for module instances 
      * @param clientType client type for module instances 
      * @return the set of Module instances resulting from a 'composite advice'
      */
     public static Set processCompositeAdviceXML(
         String xmlCompositeAdvice,
         String orgDN,
         String clientType) {
         Set returnModuleInstances = null;
         try {
             String decodedAdviceXML = URLEncDec.decode(xmlCompositeAdvice);
             Map adviceMap = parseAdvicesXML(decodedAdviceXML);
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("processCompositeAdviceXML - decoded XML : "
                 + decodedAdviceXML);
                 utilDebug.message("processCompositeAdviceXML - result Map : "
                 + adviceMap);
             }
             if (adviceMap != null) {
                 returnModuleInstances = new HashSet();
                 Set keySet = adviceMap.keySet();
                 Iterator keyIter = keySet.iterator();
                 while (keyIter.hasNext()) {
                     String name = (String)keyIter.next();
                     Set values = (Set)adviceMap.get(name);
                     if (name.equals(
                         Constants.AUTH_SCHEME_CONDITION_ADVICE)
                     ) {
                         returnModuleInstances.addAll(values);
                     }
                     if (name.equals(
                         Constants.AUTH_LEVEL_CONDITION_ADVICE)
                     ) {
                         Set newAuthLevelModules =
                         processAuthLevelCondition(values,orgDN,clientType);
                         returnModuleInstances.addAll(newAuthLevelModules);
                     }
                 }
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error in processCompositeAdviceXML : "
                 + e.toString());
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message(
                 "processCompositeAdviceXML - returnModuleInstances : "
             + returnModuleInstances);
         }
         return returnModuleInstances;
     }
     
     // Returns the set of module instances having lowest auth level from a
     // given set of auth level values
     private static Set processAuthLevelCondition(Set authLevelvalues,
     String orgDN, String clientType) {
         if (utilDebug.messageEnabled()) {
             utilDebug.message("processAuthLevelCondition - authLevelvalues : "
             + authLevelvalues);
         }
         Set returnModuleInstances = Collections.EMPTY_SET;
         try {
             if (authLevelvalues != null) {
                 // First get the lowest auth level value from a given set
                 int minAuthlevel = Integer.MAX_VALUE;
                 Iterator iter = authLevelvalues.iterator();
                 while (iter.hasNext()) {
                     //get the localized value
                     String strAuthLevel = (String) iter.next();
                     try {
                         int authLevel = Integer.parseInt(strAuthLevel);
                         if (authLevel < minAuthlevel) {
                             minAuthlevel = authLevel;
                         }
                     } catch (Exception nex) {
                         continue;
                     }
                 }
                 returnModuleInstances = getAuthModules(
                     minAuthlevel, orgDN, clientType);
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message(
                         "processAuthLevelCondition - returnModuleInstances : "
                         + returnModuleInstances + " for auth level : "
                         + minAuthlevel);
                 }
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error in processAuthLevelCondition : "
                 + e.toString());
             }
         }
         return returnModuleInstances;
     }
     
     /**
      * Check if client detection is enabled
      * @return <code>true</code> if client detection is enabled
      */
     public static boolean isClientDetectionEnabled() {
         boolean clientDetectionEnabled = false;
         
         if (clientDetector != null) {
             String detectionEnabled = clientDetector.detectionEnabled();
             clientDetectionEnabled = detectionEnabled.equalsIgnoreCase("true");
         } else {
             utilDebug.message("getClientDetector,Service does not exist");
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message(
                 "clientDetectionEnabled = " + clientDetectionEnabled);
         }
         return clientDetectionEnabled;
     }
     
     /**
      * Returns the client type. If client detection is enabled then client type
      * is determined by the <code>ClientDetector</code> class otherwise
      * <code>defaultClientType</code> set in
      * <code>iplanet-am-client-detection-default-client-type</code>
      * is assumed to be the client type.
      *
      * @param req HTTP Servlet Request.
      * @return client type.
      */
     public String getClientType(HttpServletRequest req) {
         if (isClientDetectionEnabled() && (clientDetector != null)) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("clienttype = "
                 +clientDetector.getClientType(req));
             }
             return clientDetector.getClientType(req);
         }
         return getDefaultClientType();
     }
     
     /**
      * Returns default client.
      *
      * @return default client.
      */
     public static String getDefaultClientType() {
         String defaultClientType = DEFAULT_CLIENT_TYPE;
         if (defaultClient != null) {
             try {
                 defaultClientType = defaultClient.getClientType();
                 // add observer, so auth will be notified if the client changed
                 // defClient.addObserver(this);
             } catch (Exception e) {
                 utilDebug.error("getDefaultClientType Error : " + e.toString());
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getDefaultClientType, ClientType = " +
             defaultClientType);
         }
         return defaultClientType;
     }
     
     /**
      * Returns the client Object associated with a clientType
      * default instance is returned if the instance could not be found
      * @param clientType for client Object 
      * @return the client Object associated with a clientType
      */
     private Client getClientInstance(String clientType) {
         if (!clientType.equals(getDefaultClientType())) {
             try {
                 return AuthClient.getInstance(clientType,null);
             } catch (Exception ce) {
                 utilDebug.warning("getClientInstance()" , ce);
             }
         }
         return defaultClient;
     }
     
     /**
      * Returns the requested property from clientData (example fileIdentifer).
      *
      * @param clientType
      * @param property
      * @return the requested property from clientData.
      */
     private String getProperty(String clientType, String property) {
         
         try {
             return getClientInstance(clientType).getProperty(property);
         } catch (Exception ce) {
             // which means we did not get the client Property
             utilDebug.warning("Error retrieving Client Data : " + property + 
                 ce.toString());
             // if this was not the default client type then lets
             // try to get the default client Property
             return getDefaultProperty(property);
         }
     }
     
     /**
      * Returns the requested property for default client.
      *
      * @param property
      * @return the requested property for default client.
      */
     public String getDefaultProperty(String property) {
         try {
             return defaultClient.getProperty(property);
         } catch (Exception ce) {
             utilDebug.warning("Could not get " + property + ce.toString());
         }
         return null;
     }
     
     /**
      * Returns the charset associated with the client type.
      * @param clientType client type for charset
      * @param locale for charset
      * @return the charset associated with the client type.
      */
     public String getCharSet(String clientType,java.util.Locale locale) {
         String charset = Client.CDM_DEFAULT_CHARSET;
         try {
             charset = getClientInstance(clientType).getCharset(locale);
         } catch (Exception ce) {
             // which means we did not get the client locale
             utilDebug.warning("Error retrieving Client Data : " + locale + 
                 ce.toString());            
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Charset from Client is : " + charset);
         }
         return charset;
     }
     
     /**
      * Returns the file path associated with a client type.
      *
      * @param clientType Client Type.
      * @return the file path associated with a client type.
      */
     public String getFilePath(String clientType) {
         String filePath = getProperty(clientType,"filePath");
         return (filePath == null) ? DEFAULT_FILE_PATH : filePath;
     }
     
     /**
      * Returns the content type associated with a client type
      * if no content type found then return the default.
      *
      * @param clientType Client Type.
      * @return The content type associated with a client type.
      */
     public String getContentType(String clientType) {
         String contentType = getProperty(clientType,"contentType");
         return (contentType == null) ? DEFAULT_CONTENT_TYPE : contentType;
     }
     
     /**
      * Returns "cookieSupport" if cookies are supported for given clientType
      *
      * @param clientType client type to check for cookie support
      * @return "cookieSupport" if cookies are supported for given clientType
      */
     public String getCookieSupport(String clientType) {
         String cookieSup = getProperty(clientType,"cookieSupport");
         return cookieSup;
     }
     
     /**
      * Returns <code>true</code> if this client is an HTML client.
      *
      * @param clientType
      * @return <code>true</code> if this client is an HTML client.
      */
     public boolean isGenericHTMLClient(String clientType) {
         String type = getProperty(clientType,"genericHTML");
         return (type == null) || type.equals("true");
     }  
    
     /**
      * Returns <code>true</code> if <code>cookieSupport</code> is set or
      * <code>cookieDetection</code> mode has been detected .This is used to
      * determine whether cookie should be set in response or not.
      *
      * @param clientType client type to check if cookie is supported
      * @return <code>true</code> if cookie is supported for given client type
      */
     public boolean isSetCookie(String clientType) {
         boolean setCookie = setCookieVal(clientType, "true");
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("setCookie : " + setCookie);
         }
         
         return setCookie;
     }
     
    /**
     * Returns <code>true</code> if the cookie detect is set, cookie support
     * values to determine if cookie should be rewritten or set.
     *
     * @param clientType client type to check if the cookie detect is set
     * @param value 
     * @return <code>true</code> if the cookie detect is set
     */
     public boolean setCookieVal(String clientType, String value) {
         String cookieSupport = getCookieSupport(clientType);
         boolean cookieDetect = getCookieDetect(cookieSupport);
         
         boolean cookieSup =  ((cookieSupport !=null) &&
         (cookieSupport.equalsIgnoreCase(value) ||
         cookieSupport.equalsIgnoreCase(
         ISAuthConstants.COOKIE_DETECT_PROPERTY)));
         boolean setCookie = (cookieSup || cookieDetect) ;
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("cookieSupport : " + cookieSupport);
             utilDebug.message("cookieDetect : " + cookieDetect);
             utilDebug.message(" setCookie is : " +  setCookie);
         }
         
         return setCookie;
     }
     
     /**
      * Returns <code>true</code> if <code>cookieDetect</code> mode.
      *
      * @param cookieSupport <code>true</code> if cookie is supported.
      * @return <code>true</code> if cookie detect mode.
      */
     public boolean getCookieDetect(String cookieSupport) {
         boolean cookieDetect
         = ((cookieSupport == null) ||
         (cookieSupport.equalsIgnoreCase(
         ISAuthConstants.COOKIE_DETECT_PROPERTY)));
         if (utilDebug.messageEnabled()) {
             utilDebug.message("CookieDetect : " + cookieDetect);
         }
         return cookieDetect;
     }
     
     /**
      * Returns the client URL from the String passed URL passed is in the
      * format <code>clientType | URL</code>.
      *
      * @param urlString URL.
      * @param index Position of delimiter "|".
      * @param request <code>HttpServletRequest</code> object to be compared with
      *        urlString URL.
      * @return Client URL.
      */
     public String getClientURLFromString(
         String urlString,
         int index,
         HttpServletRequest request) {
         String clientURL = null;
         if (urlString != null) {
             String clientTypeInUrl = urlString.substring(0,index);
             if ((clientTypeInUrl != null) &&
             (clientTypeInUrl.equals(getClientType(request)))) {
                 if (urlString.length() > index) {
                     clientURL = urlString.substring(index+1);
                 }
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Client URL is :" + clientURL);
         }
         return clientURL;
     }
     
    /**
     * Returns <code>true</code> if <code>cookieSupport</code> is false and
     * cookie Detect mode (which is rewrite as well as set cookie the first
     * time). This determines whether URL should be rewritten or not.
     *
     * @param clientType
     * @return <code>true</code> if <code>cookieSupport</code> is false
     */
     public boolean isUrlRewrite(String clientType) {
         
         boolean rewriteURL = setCookieVal(clientType,"false");
         if (utilDebug.messageEnabled()) {
             utilDebug.message("rewriteURL : " + rewriteURL);
         }
         
         return rewriteURL;
     }
     
     /**
      * Returns DSAME Version
      * @return DSAME Version
      */
     public static String getDSAMEVersion() {
         return dsameVersion;
     }
     
     /**Returns the Auth Cookie Name.
      *
      * @return authCookieName, a String,the auth cookie name.
      */
     public static String getAuthCookieName() {
         return authCookieName;
     }
     
     /**
      * Returns configured cookie name
      * @return configured cookie name
      */
     public static String getCookieName() {
         return cookieName;
     }
     
     /**
      * Returns configured Persistent Cookie Name
      * @return configured Persistent Cookie Name
      */
     public static String getPersistentCookieName() {
         return persistentCookieName;
     }
     
     /**
      * Returns configured LB Cookie Name
      * @return configured LB Cookie Name
      */
     public static String getlbCookieName() {
         return loadBalanceCookieName;
     }
     
     /**
      * Returns configured LB Cookie Value
      * @return configured LB Cookie Value
      */
     public static String getlbCookieValue() {
         return loadBalanceCookieValue;
     }
 
     /**
      * Returns configured set of cookie domains
      * @return configured set of cookie domains
      */
     public Set getCookieDomains() {
         Set cookieDomains = Collections.EMPTY_SET;
         try {
             SSOToken token = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
             try {
                 ServiceSchemaManager scm  = new ServiceSchemaManager(
                 "iPlanetAMPlatformService",token);
                 ServiceSchema psc = scm.getGlobalSchema();
                 Map attrs = psc.getAttributeDefaults();
                 cookieDomains =
                 (Set)attrs.get(ISAuthConstants.PLATFORM_COOKIE_DOMAIN_ATTR);
             } catch (SMSException ex) {
                 // Ignore the exception and leave cookieDomains empty;
                 utilDebug.message("getCookieDomains - SMSException ");
             }
             if (cookieDomains == null) {
                 cookieDomains = Collections.EMPTY_SET;
             }
         } catch (SSOException ex) {
             // unable to get SSOToken
             utilDebug.message("getCookieDomains - SSOException ");
         }
         if (utilDebug.messageEnabled() && (!cookieDomains.isEmpty())) {
             utilDebug.message("CookieDomains : ");
             Iterator iter = cookieDomains.iterator();
             while (iter.hasNext()) {
                 utilDebug.message("  " + (String)iter.next());
             }
         }
         return cookieDomains;
     }
     
     /**
      * Returns the organization DN. The organization DN is deteremined based on
      * the query parameters <code>org</code> OR <code>domain</code> OR
      * the server host name. For backward compatibility the organization name
      * will be determined from <code>requestURI</code> in the case where either
      * query params OR server host name are not valid and organization DN
      * cannot be found.
      * <p>
      * The orgDN is determined based on and in order,by the SDK:
      * <pre>
      * 1. OrgDN - organization dn.
      * 2. Domain - check if org is a domain by trying to get
      *    domain component
      * 3  Org path- check if the orgName passed is a path (eg."/suborg1")
      * 4. URL - check if the orgName passed is a DNS alias (URL).
      * 5. If no orgDN is found null is returned.
      * </pre>
      *
      * @param orgParam org or domain query param, or the server host name.
      * @param noQueryParam <code>true</code> if the request did not have query.
      * @param request HTTP Servlet Request object.
      * @return  Organization DN.
      */
     public String getOrganizationDN(
         String orgParam,
         boolean noQueryParam,
         HttpServletRequest request) {
         String orgName = null;
         SSOToken token = (SSOToken) AccessController.doPrivileged(
         AdminTokenAction.getInstance());
         
         // try to get the host name if org or domain Param is null
         try {
             orgName = IdUtils.getOrganization(token,orgParam);
            if ((orgName != null) && (orgName.length() != 0)) {
                orgName = orgName.toLowerCase();
            }
         } catch (Exception oe) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Could not get orgName : ",oe);
             }
         }
         
         // if orgName is null then match the DNS Alias Name
         // to the full url ie. proto:/server/amserver/UI/Login
         // This is for backward compatibility
         
         if (((orgName == null) || orgName.length() == 0) && (noQueryParam)) {
             if (request != null) {
                 String url = request.getRequestURL().toString();
                 int index  = url.indexOf(";");
                 if (index != -1) {
                     orgParam = stripPort(url.substring(0,index));
                 } else {
                     orgParam = stripPort(url);
                 }
                 
                 try {
                     orgName = IdUtils.getOrganization(token,orgParam);
                 } catch (Exception e) {
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message(
                             "Could not get orgName : " + orgParam, e);
                     }
                 }
             }
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getOrganizationDN : orgParam... :" + orgParam);
             utilDebug.message("getOrganizationDN : orgDN ... :" + orgName);
         }
         return orgName;
     }
     
     /**
      * Returns the organization parameter and determines the organization
      * DN based on query parameters. The organization DN is determined based on
      * the query parameters <code>org</code> OR <code>domain</code> OR
      * the server host name. For backward compatibility the organization name
      * will be determined from <code>requestURI</code> in the case where either
      * query params OR server host name are not valid and orgDN cannot be found.
      * The <code>orgDN</code> is determined based on and in order,by the SDK:
      * <pre>
      * 1. OrgDN - organization dn.
      * 2. Domain - check if org is a domain by trying to get
      *    domain component
      * 3  Org path- check if the orgName passed is a path (eg."/suborg1")
      * 4. URL - check if the orgName passed is a DNS alias (URL).
      * 5. If no orgDN is found null is returned.
      * </pre>
      *
      * @param request HTTP Servlet Request object.
      * @param requestHash Map of the query parameters.
      * @return Organization DN.
      */
     public String getDomainNameByRequest(
         HttpServletRequest request,
         Hashtable requestHash) {
         boolean noQueryParam=false;
         
         String orgParam = getOrgParam(requestHash);
         if (utilDebug.messageEnabled()) {
             utilDebug.message("orgParam is.. :" + orgParam);
         }
         
         // try to get the host name if org or domain Param is null
         if ((orgParam == null) || (orgParam.length() == 0)) {
             noQueryParam= true;
             orgParam = request.getServerName();
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Hostname : " + orgParam);
             }
         }
         String orgDN = getOrganizationDN(orgParam,noQueryParam,request);
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("orgDN is " + orgDN);
         }
         
         return orgDN;
     }
     
     /**
      * Returns the organization or domain parameter passed as a query in the
      * request.
      *
      * @param requestHash Map of query parameters.
      * @return Organization or domain parameter.
      */
     public static String getOrgParam(Hashtable requestHash) {
         String orgParam = null;
         if ((requestHash != null) && !requestHash.isEmpty()) {
             orgParam = (String) requestHash.get(ISAuthConstants.DOMAIN_PARAM);
             if ((orgParam == null) || (orgParam.length() == 0)) {
                 orgParam = (String)requestHash.get(ISAuthConstants.ORG_PARAM);
             }
             if ((orgParam == null) || (orgParam.length() == 0)) {
                 orgParam = (String)requestHash.get(ISAuthConstants.REALM_PARAM);
             }
         }
         return orgParam;
     }
     
     String stripPort(String in) {
         try {
             URL url = new URL(in);
             return (url.getProtocol() + "://" + url.getHost()+ url.getFile());
         } catch (MalformedURLException ex) {
             return in;
         }
     }
     
     /**
      * Returns <code>true</code> if the host name entered in the URL is valid.
      *
      * @param hostName Host name.
      * @return <code>true</code> if the host name entered in the URL is valid.
      */
     public static boolean isValidFQDNRequest(String hostName) {
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("hostName is : " + hostName);
         }
         
         boolean retVal = fqdnUtils.isHostnameValid(hostName);
         
         if (retVal) {
             utilDebug.message("hostname  and fqdnDefault match returning true");
         } else {
             utilDebug.message("hostname and fqdnDefault don't match");
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("retVal is : " + retVal);
         }
         return retVal;
     }
     
     /**
      * Returns the valid hostname from the fqdn map and
      * constructs the correct url. the request will be forwarded to the new url
      * @param partialHostName partial host name has to be completed with fqdn
      *        information.
      * @param servletRequest
      * @return the valid hostname from the fqdn map and constructs the correct 
      *         url. the request will be forwarded to the new url
      */
     public static String getValidFQDNResource(
         String partialHostName,
         HttpServletRequest servletRequest) {
         // get mapping from table
         if(utilDebug.messageEnabled()) {
             utilDebug.message("Get mapping for " + partialHostName);
         }
         
         String validHostName =
         fqdnUtils.getFullyQualifiedHostName(partialHostName);
         
         if (validHostName == null) {
             validHostName = partialHostName;
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("fully qualified hostname :"+ validHostName);
         }
         
         String requestURL = constructURL(validHostName,servletRequest);
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Request URL :"+ requestURL);
         }
         return requestURL;
     }
     
     /**
      * Returns the host name from the servlet request's host header or
      * get it using servletRequest:getServerName() in the case
      * where host header is not found
      * @param servletRequest <code>HttpServletRequest</code> object to get
      *        host name
      * @return the host name from the servlet request
      */
     public static String getHostName(HttpServletRequest servletRequest) {
         // get the host header
         String hostname = servletRequest.getHeader("host");
         if (hostname != null) {
             int i = hostname.indexOf(":");
             if (i != -1) {
                 hostname = hostname.substring(0,i);
             }
         } else {
             hostname = servletRequest.getServerName();
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Returning host name : " + hostname);
         }
         return hostname;
         
     }
     
     /* construct the url */
     static String constructURL(
         String validHostName,
         HttpServletRequest servletRequest) {
         String scheme =
         RequestUtils.getRedirectProtocol(
             servletRequest.getScheme(),validHostName);
         int port = servletRequest.getServerPort();
         String requestURI = servletRequest.getRequestURI();
         String queryString = servletRequest.getQueryString();
         
         StringBuffer urlBuffer = new StringBuffer();
         urlBuffer.append(scheme).append("://")
         .append(validHostName).append(":")
         .append(port).append(requestURI);
         
         if (queryString != null) {
             urlBuffer.append("?")
             .append(queryString);
         }
         
         String urlString = urlBuffer.toString();
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("returning new url : " + urlString);
         }
         
         return urlString;
     }
     
     /**
      * Returns login url for given <code>HttpServletRequest</code> object
      * @param request <code>HttpServletRequest</code> object to retrieve 
      *        login url
      * @return login url for given <code>HttpServletRequest</code> object
      */
     public String constructLoginURL(HttpServletRequest request) {
         StringBuffer loginURL = new StringBuffer(serviceURI);
         String qString = request.getQueryString();
         if ((qString != null) && (qString.length() != 0)) {
             loginURL.append("?");
             loginURL.append(qString);
         }
         return (loginURL.toString());
     }
     
     /**
      * Returns valid <code>SSOToken</code> object for given 
      * <code>SessionID</code> object.
      * @param sessID for ssotoken
      * @return  valid <code>SSOToken</code> object for given 
      * <code>SessionID</code> object.
      */
     public SSOToken getExistingValidSSOToken(SessionID sessID) {
         SSOToken ssoToken = null;
         try {
             if (sessID != null) {
                 String sidString = sessID.toString();
                 SSOTokenManager manager = SSOTokenManager.getInstance();
                 SSOToken currentToken = manager.createSSOToken(sidString);
                 if (manager.isValidToken(currentToken)) {
                     ssoToken = currentToken;
                 }
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message(
                     "Error in getExistingValidSSOToken :"+ e.toString());
             }
             return ssoToken;
         }
         return ssoToken;
     }
     
     /**
      * Returns error value for given errorCode and type
      * @param errorCode for error value
      * @param type for error value
      * @return error value for given errorCode and type
      */
     public String getErrorVal(String errorCode,String type) {
         String errorMsg=null;
         String templateName=null;
         String resProperty = bundle.getString(errorCode);
         if (utilDebug.messageEnabled()) {
             utilDebug.message("errorCod is.. : " + errorCode);
             utilDebug.message("resProperty is.. : " + resProperty);
         }
         if ((resProperty != null) && (resProperty.length() != 0)) {
             int commaIndex = resProperty.indexOf(MSG_DELIMITER);
             if (commaIndex != -1) {
                 templateName = resProperty.substring(
                     commaIndex+1,resProperty.length());
                 errorMsg = resProperty.substring(0,commaIndex);
             } else {
                 errorMsg = resProperty;
             }
         }
         
         if (type.equals(ERROR_MESSAGE)) {
             return errorMsg;
         } else if (type.equals(ERROR_TEMPLATE)) {
             return templateName;
         } else {
             return null;
         }
     }
     
     /**
      * Check if cookie is supported for given <code>HttpServletRequest</code>
      * object.
      * @param req <code>HttpServletRequest</code> object to be checked for 
      *        cookie.
      * @return <code>true</code> if cookie is supported for given 
      * <code>HttpServletRequest</code> object.
      */
     public boolean isCookieSupported(HttpServletRequest req) {
         boolean cookieSupported = true;
         String cookieSupport = getCookieSupport(getClientType(req));
         if ((cookieSupport != null) && cookieSupport.equals("false")){
             cookieSupported = false;
         }
         return cookieSupported;
     }
     
     /**
      * Check if cookie is set for given <code>HttpServletRequest</code>
      * object.
      * @param req <code>HttpServletRequest</code> object to be checked for 
      *        cookie.
      * @return <code>true</code> if cookie is set for given 
      * <code>HttpServletRequest</code> object.
      */
     public boolean isCookieSet(HttpServletRequest req) {
         boolean cookieSet = false;
         String cookieSupport = getCookieSupport(getClientType(req));
         boolean cookieDetect = getCookieDetect(cookieSupport);
         if (isClientDetectionEnabled() && cookieDetect) {
             cookieSet = true;
         }
         return cookieSet;
     }
     
     /**
      * Creates Persistent Cookie with given name, value, etc.
      * @param name cookie name
      * @param value cookie value
      * @param maxAge cookie max age
      * @param cookieDomain  cookie domain
      * @return Persistent Cookie with given name, value, etc.
      */
     public static Cookie createPersistentCookie(
         String name,
         String value,
         int maxAge,
         String cookieDomain) {
         Cookie pCookie = CookieUtils.newCookie(name, value, "/", cookieDomain);
         if (maxAge >= 0) {
             pCookie.setMaxAge(maxAge);
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("pCookie is.. :" + pCookie);
         }
         
         return pCookie;
     }
 
     /**
      * Creates LB Cookie with given cookie domain
      * @param cookieDomain  cookie domain
      * @return LB Cookie with given cookie domain
      * @throws AuthException if it fails to create cookie
      */
     public Cookie createlbCookie(String cookieDomain) throws AuthException {
         Cookie lbCookie = null;
         try {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("cookieDomain : " + cookieDomain);
             }
             String cookieName = getlbCookieName();
             String cookieValue = getlbCookieValue();
             lbCookie = createPersistentCookie(
                 cookieName, cookieValue, -1, cookieDomain);
             return lbCookie;
         } catch (Exception e) {
             utilDebug.message("Unable to create Load Balance Cookie");
             throw new AuthException(AMAuthErrorCode.AUTH_ERROR, null);
         }
     }
     
     /**
      * Returns the Cookie object created based on  the cookie Name, Session ID
      * and cookie domain. If authentication context status is not
      * <code>SUCCESS</code> then cookie is created with Authentication Cookie
      * Name, else Access Manager Cookie Name will be used to create cookie.
      *
      * @param ac Authentication Context object.
      * @param cookieDomain Cookie domain for creating cookie.
      * @return Created Cookie.
      */
     public Cookie getCookieString(AuthContext ac, String cookieDomain) {
         Cookie cookie = null;
         String cookieName = getAuthCookieName();
         String cookieValue = serverURL + serviceURI;
         try {
             if (ac.getStatus() == AuthContext.Status.SUCCESS) {
                 cookieName = getCookieName();
                 cookieValue = ac.getAuthIdentifier();
                 utilDebug.message("Create AM cookie");
             }
             cookie = createCookie(cookieName,cookieValue,cookieDomain);
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error getCookieString : " + e.getMessage());
             }
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Cookie is : " + cookie);
         }
         return cookie;
     }
     
     /**
      * Returns URL with the cookie value in the URL.
      * The cookie in the rewritten url will have the access manager cookie if
      * session is active/inactive and authentication cookie if session is
      * invalid
      *
      * @param url URL.
      * @param request HTTP Servlet Request.
      * @param ac Authentication Context object.
      * @return the encoded URL.
      */
     public String encodeURL(
         String url,
         HttpServletRequest request,
         AuthContext ac) {
         if (isCookieSupported(request)) {
             return url;
         }
         
         String cookieName = getAuthCookieName();
         if (ac.getStatus() == AuthContext.Status.SUCCESS) {
             cookieName = getCookieName();
         }
         
         String encodedURL = url;
         if (urlRewriteInPath) {
             encodedURL = encodeURL(url, SessionUtils.SEMICOLON, false,
                 cookieName, ac.getAuthIdentifier());
         } else {
             encodedURL = encodeURL(url, SessionUtils.QUERY, true, cookieName,
                 ac.getAuthIdentifier());
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("encodeURL : URL = " + url +
             ", \nRewritten URL = " + encodedURL);
         }
         return (encodedURL);
     }
     
     private String encodeURL(
         String url,
         short encodingScheme,
         boolean escape,
         String cookieName,
         String strSessionID) {
         String encodedURL = url;
         String cookieStr = SessionEncodeURL.createCookieString(
             cookieName, strSessionID);
         encodedURL = SessionEncodeURL.encodeURL(cookieStr,url,
         encodingScheme,escape);
         return encodedURL;
     }
     
     /**
      * Returns the resource based on the default values.
      *
      * @param request Reference to HTTP Servlet Request object.
      * @param fileName Name of the file.
      * @param locale Locale
      * @param servletContext Servlet context for server.
      * @return Path to the resource.
      */
     public String getDefaultFileName(
         HttpServletRequest request,
         String fileName,
         java.util.Locale locale,
         ServletContext servletContext) {
         String strlocale = "";
         if (locale != null) {
             strlocale = locale.toString();
         }
         String filePath = getFilePath(getClientType(request));
         String fileRoot = ISAuthConstants.DEFAULT_DIR;
         
         String templateFile = null;
         try {
             templateFile = ResourceLookup.getFirstExisting(
                 servletContext, fileRoot, strlocale, null, filePath, fileName,
                 templatePath, true);
         } catch (Exception e) {
             templateFile = templatePath + fileRoot +
                 Constants.FILE_SEPARATOR + fileName;
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getDefaultFileName:templateFile is :" +
             templateFile);
         }
         return templateFile;
     }
     
     /**
      * Returns the root suffix example <code>o=isp</code>.
      *
      * @return Root suffix.
      */
     public String getRootSuffix() {
         // rootSuffix is already normalized in SMSEntry
         return rootSuffix;
     }
     
    /* get the root dir to start lookup from./<default org>
     * default is /default
     */
     private String getFileRoot() {
         String fileRoot = ISAuthConstants.DEFAULT_DIR;
         String rootOrgName = DNUtils.DNtoName(rootSuffix);
         if (utilDebug.messageEnabled()) {
             utilDebug.message("rootOrgName is : " + rootOrgName);
         }
         if (rootOrgName != null) {
             fileRoot = rootOrgName;
         }
         return fileRoot;
     }
     
     /* insert chartset in the filename */
     private String getCharsetFileName(String fileName) {
         ISLocaleContext localeContext = new ISLocaleContext();
         String charset = localeContext.getMIMECharset();
         if (fileName == null) {
             return null;
         }
         
         int i = fileName.indexOf(".");
         String charsetFilename = null;
         if (i != -1) {
             charsetFilename = fileName.substring(0, i) + "_" + charset +
             fileName.substring(i);
         } else {
             charsetFilename = fileName + "_" + charset;
         }
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("charsetFilename is : "+ charsetFilename);
         }
         return charsetFilename;
     }
     
     /**
      * Returns the resource (file) using resource lookup
      * @param fileRoot
      * @param localeName
      * @param orgFilePath
      * @param filePath
      * @param filename
      * @param templatePath
      * @param servletContext
      * @param request
      * @return the resource (file) using resource lookup
      */
     public String getResourceLocation(
         String fileRoot,
         String localeName,
         String orgFilePath,
         String filePath,
         String filename,
         String templatePath,
         ServletContext servletContext,
         HttpServletRequest request) {
         String resourceName = null;
         String clientType = getClientType(request);
         if ((clientType != null) &&
         (!clientType.equals(getDefaultClientType()))) {
             // non-HTML client
             String charsetFileName = getCharsetFileName(filename);
             resourceName =
             ResourceLookup.getFirstExisting(servletContext,fileRoot,
             localeName,orgFilePath,
             filePath,charsetFileName,
             templatePath,true);
         }
         if (resourceName == null) {
             resourceName = ResourceLookup.getFirstExisting(servletContext,
             fileRoot,localeName,
             orgFilePath,
             filePath,filename,
             templatePath,true);
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("Resource is.. " + resourceName);
         }
         return resourceName;
     }
     
     /**
      * Returns the filePath parameter for FileLookUp
      * filePath = indexName (service name) + clientPath (eg. html).
      * @param request
      * @param indexType
      * @param indexName
      * @return the filePath parameter for FileLookUp
      * filePath = indexName (service name) + clientPath (eg. html).
      */
     public String getFilePath(
         HttpServletRequest request,
         AuthContext.IndexType indexType,
         String indexName) {
         String filePath = getFilePath(getClientType(request));
         String serviceName = null;
         StringBuffer filePathBuffer = new StringBuffer();
         // only if index name is service type then need it
         // as part of the filePath since  service can have
         // have different auth template
         
         if ((indexType != null) &&
         (indexType.equals(AuthContext.IndexType.SERVICE))) {
             serviceName = indexName;
         }
         
         if ((filePath == null) && (serviceName == null)) {
             return null;
         }
         
         if ((filePath != null) && (filePath.length() > 0))  {
             filePathBuffer.append(Constants.FILE_SEPARATOR).append(filePath);
         }
         
         if ((serviceName != null) && (serviceName.length() >0)) {
             filePathBuffer.append(Constants.FILE_SEPARATOR).append(serviceName);
         }
         
         String newFilePath = filePathBuffer.toString();
         if (utilDebug.messageEnabled()) {
             utilDebug.message("FilePath is.. :" + newFilePath);
         }
         
         return newFilePath;
     }
     
     /* retrieves the org path to search resource
      * eg. if orgDN = o=org1,o=org11,o=org12,dc=iplanet,dc=com
      * then orgFilePath will be org12/org11/org1
      */
     String getOrgFilePath(String orgDN) {
         if (utilDebug.messageEnabled()) {
             utilDebug.message("getOrgFilePath : orgDN is: " + orgDN);
         }
         String normOrgDN = DNUtils.normalizeDN(orgDN);
         String orgPath = null;
         
         if (normOrgDN != null) {
             StringBuffer orgFilePath = new StringBuffer();
             String remOrgDN = normOrgDN;
             String orgName = null;
             while ((remOrgDN != null) && (remOrgDN.length() != 0)
             && !remOrgDN.equals(getRootSuffix())) {
                 orgName = DNUtils.DNtoName(remOrgDN);
                 orgFilePath = orgFilePath.insert(
                     0, Constants.FILE_SEPARATOR + orgName);
                 int i = remOrgDN.indexOf(",");
                 if (i != -1) {
                     remOrgDN = remOrgDN.substring(i+1);
                 }
                 if (utilDebug.messageEnabled()){
                     utilDebug.message("remOrgDN is : "+ remOrgDN);
                 }
             }
             orgPath = orgFilePath.toString();
         }
         
         if (utilDebug.messageEnabled()){
             utilDebug.message("getOrgFilePath: orgPath is : " + orgPath);
         }
         return orgPath;
     }
     
     /**
      * Returns the File name based on the given input values.
      *
      * @param fileName Name of the file.
      * @param localeName Locale Name.
      * @param orgDN Organization DN.
      * @param servletRequest HTTP Servlet Request object.
      * @param servletContext Servlet context for server.
      * @param indexType Authentication context index type.
      * @param indexName Index name associated with the index type.
      * @return File name of the resource.
      */
     public String getFileName(
         String fileName,
         String localeName,
         String orgDN,
         HttpServletRequest servletRequest,
         ServletContext servletContext,
         AuthContext.IndexType indexType,
         String indexName) {
         String fileRoot = getFileRoot();
         String templateFile = null;
         try {
             // get the filePath  Client filePath + serviceName
             String filePath = getFilePath(servletRequest,indexType,indexName);
             String orgFilePath = getOrgFilePath(orgDN);
             
             if (utilDebug.messageEnabled()) {
                 utilDebug.message(
                     "Calling ResourceLookup: filename = " + fileName +
                     ", defaultOrg = " + fileRoot + ", locale = " + localeName +
                     ", filePath = " + filePath + ", orgPath = " + orgFilePath);
             }
             
             templateFile = getResourceLocation(fileRoot,localeName,orgFilePath,
             filePath,fileName,templatePath,servletContext,servletRequest);
         } catch (Exception e) {
             utilDebug.message("Error getting File : " + e.getMessage());
             templateFile = templatePath + Constants.FILE_SEPARATOR +
                 ISAuthConstants.DEFAULT_DIR + Constants.FILE_SEPARATOR +
                 fileName;
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("File/Resource is : " + templateFile);
         }
         return templateFile;
     }
     
     /**
      * Returns Auth Cookie Value for given <code>HttpServletRequest</code>
      *  object
      * @param req <code>HttpServletRequest</code> object to be check for cookie
      * @return Auth Cookie Value for given <code>HttpServletRequest</code> 
      *  object
      */
     public String getAuthCookieValue(HttpServletRequest req) {
         return CookieUtils.getCookieValueFromReq(req,getAuthCookieName());
     }
 
     /**
      * Returns domain name for given requestHash
      * @param requestHash <code>Hashtable</code> object to be checked for
      *        domain name.
      * @return domain name for given requestHash
      */
     public String getDomainNameByRequest(Hashtable requestHash) {
         String orgParam = getOrgParam(requestHash);
         if (utilDebug.messageEnabled()) {
             utilDebug.message("orgParam is.. :" + orgParam);
         }
         // try to get the host name if org or domain Param is null
         if ((orgParam == null) || (orgParam.length() == 0)) {
             orgParam = "/";
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("defaultOrg : " + orgParam);
             }
         }
         String orgDN = getOrganizationDN(orgParam,false,null);
         
         if (utilDebug.messageEnabled()) {
             utilDebug.message("orgDN is " + orgDN);
         }
         return orgDN;
     }
     
     
     // returns AuthContextLocal object from Session object identified by 'sid'.
     // if not found then check it in the HttpSession.
     private static AuthContextLocal retrieveAuthContext(
         HttpServletRequest req,
         SessionID sid) {
         AuthContextLocal acLocal = null;        
         if (req != null && AuthD.isHttpSessionUsed()) {
             HttpSession hs = req.getSession(false);
             if (hs != null) {
                 acLocal = (AuthContextLocal)hs.getAttribute(
                     ISAuthConstants.AUTH_CONTEXT_OBJ);
                 if (utilDebug.messageEnabled() && acLocal != null) {
                     utilDebug.message("authContext from httpsession: " 
                         + acLocal);
                 }
             }
         } else if (sid != null) {
             acLocal = retrieveAuthContext(sid);
         }
 
         return acLocal;
     }
     
     // retrieve the AuthContextLocal object from the Session object.
     private static AuthContextLocal retrieveAuthContext(SessionID sid) {
         com.iplanet.dpro.session.service.InternalSession is =
             AuthD.getSession(sid);        
         AuthContextLocal localAC = null;
         if (is != null) {
             localAC = (AuthContextLocal)
             is.getObject(ISAuthConstants.AUTH_CONTEXT_OBJ);
         }
         if (utilDebug.messageEnabled()) {
             utilDebug.message("retrieveAuthContext - InternalSession = " + is);
             utilDebug.message("retrieveAuthContext - aclocal = " + localAC);
         }
         return localAC;
     }
     
     /**
      * Removes the <code>AuthContextLocal</code> object in the Session object
      * identified by <code>sid</code>.
      *
      * @param sid Session ID.
      */
     public static void removeAuthContext(SessionID sid) {
         com.iplanet.dpro.session.service.InternalSession is =
         AuthD.getSession(sid);
         if (is != null) {
             is.removeObject(ISAuthConstants.AUTH_CONTEXT_OBJ);
         }
     }
     
     /**
      * Check whether the request is coming to the server who created the
      * original Auth request or session
      * @param cookieURL
      * @param isServer 
      * @return <code>true</code> if the request is coming to the server 
      *   who created the original Auth request or session
      */
     public boolean isLocalServer(String cookieURL, boolean isServer) {
         boolean local = false;
         try {
             String urlStr   = serverURL + serviceURI;
 
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("This server URL : " + urlStr);
                 utilDebug.message("Server URL from cookie : " + cookieURL);
             }
 
             if ((urlStr != null) && (cookieURL != null) &&
                 (cookieURL.equalsIgnoreCase(urlStr))) {
                 local = true;
             }
             if (!local && isServer && (cookieURL != null)) {
                 int uriIndex = cookieURL.indexOf(serviceURI);
                 String tmpCookieURL = cookieURL;
                 if (uriIndex != -1) {
                     tmpCookieURL = cookieURL.substring(0,uriIndex);
                 }
                 Vector platformList = WebtopNaming.getPlatformServerList();
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("search CookieURL : " + tmpCookieURL);
                     utilDebug.message("platform server List : " + platformList);
                 }
                 // if cookie URL is not in the Platform server list then
                 // consider as new authentication for that local server
                 if (!platformList.contains(tmpCookieURL)) {
                     local = true;
                 }
             }
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Error isLocalServer : " + e.getMessage());
             }
         }
         return local;
     }
 
     /**
      * Checks hether the request is coming to the server who created the
      * original Auth request or session
      * @param cookieURL
      * @param inputURI the URI to be apended toserver URL
      * @return <code>true</code> if is local server,<code>false</code>otherwise
      * 
      */
     public boolean isLocalServer(String cookieURL, String inputURI) {
         int uriIndex = cookieURL.indexOf(inputURI);
         String tmpCookieURL = cookieURL;
         if (uriIndex != -1) {
             tmpCookieURL = cookieURL.substring(0,uriIndex);
         }
         return isLocalServer(tmpCookieURL+serviceURI, true);
     }
     
     /**
      * Sends the request to the original authentication server and receives
      * the result data.
      *
      * @param request HTTP Servlet Request to be sent.
      * @param response HTTP Servlet Response to be received.
      * @param cookieURL URL of the original authentication server to be
      *        connected.
      *
      * @return Map of the result data from the original server's response.
      */
     public HashMap sendAuthRequestToOrigServer(
         HttpServletRequest request,
         HttpServletResponse response,
         String cookieURL) {
         HashMap origRequestData = new HashMap();
         
         // Print request Headers
         if (utilDebug.messageEnabled()) {
             Enumeration requestHeaders = request.getHeaderNames();
             while (requestHeaders.hasMoreElements()) {
                 String name = (String) requestHeaders.nextElement();
                 Enumeration value = (Enumeration)request.getHeaders(name);
                 utilDebug.message("Header name = " + name + " Value = " +
                 value);
             } // w
         }
         
         // Open URL connection
         HttpURLConnection conn = null;
         OutputStream  out = null;
         String strCookies = null;
         try {
             URL authURL = new URL(cookieURL);
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Connecting to : " + authURL);
             }
             conn = (HttpURLConnection)authURL.openConnection();
             conn.setDoOutput( true );
             conn.setUseCaches( false );
             conn.setRequestMethod("POST");
             conn.setFollowRedirects(false);
             conn.setInstanceFollowRedirects(false);
             
             // replay cookies
             strCookies = getCookiesString(request);
             if (strCookies != null) {
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("Sending cookies : " + strCookies);
                 }
                 conn.setRequestProperty("Cookie", strCookies);
             }
             conn.setRequestProperty(
                 "Content-Type", "application/x-www-form-urlencoded");
             conn.setRequestProperty(
                 "Content-Length", request.getHeader("content-length"));
             conn.setRequestProperty("Host", request.getHeader("host"));
             
             // Sending Output to Original Auth server...
             utilDebug.message("SENDING DATA ... ");
             String in_requestData = getFormData(request);
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Request data : " + in_requestData);
             }
             out = conn.getOutputStream();
             PrintWriter pw = new PrintWriter(out);
             pw.print(in_requestData); // here we "send" the request body
             pw.flush();
             pw.close();
             
             // Receiving input from Original Auth server...
             utilDebug.message("RECEIVING DATA ... ");
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("Response Code: " + conn.getResponseCode());
                 utilDebug.message("Response Message: "
                 + conn.getResponseMessage());
                 utilDebug.message("Follow redirect : "
                 + conn.getFollowRedirects());
             }
             
             // Check response code
             if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                 
                 // Input from Original servlet...
                 StringBuffer in_buf = new StringBuffer();
                 BufferedReader in = new BufferedReader(
                 new InputStreamReader(conn.getInputStream(), "UTF-8"));
                 int len;
                 char[] buf = new char[1024];
                 while((len = in.read(buf,0,buf.length)) != -1) {
                     in_buf.append(buf,0,len);
                 }
                 String in_string = in_buf.toString();
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("Received response data : " + in_string);
                 }
                 origRequestData.put("OUTPUT_DATA",in_string);
                 
             } else {
                 utilDebug.message("Response code NOT OK");
             }
             
             String client_type = conn.getHeaderField("AM_CLIENT_TYPE");
             if (client_type != null) {
                 origRequestData.put("AM_CLIENT_TYPE", client_type);
             }
             String redirect_url = conn.getHeaderField("Location");
             if (redirect_url != null) {
                 origRequestData.put("AM_REDIRECT_URL", redirect_url);
             }
             
             // retrieves cookies from the response
             Map headers = conn.getHeaderFields();
             processCookies(headers, response);
             
             out.flush();
             
         } catch (Exception e) {
             if (utilDebug.messageEnabled()) {
                 utilDebug.message("send exception : " , e);
             }
         } finally {
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException ioe) {
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("send IOException : "
                         + ioe.toString());
                     }
                 }
             }
         }
         
         return origRequestData;
     }
     
     // Gets the request form data in the form of string
     private String getFormData(HttpServletRequest request) {
         StringBuffer buffer = new StringBuffer();
         buffer.append("");
         Enumeration requestEnum = request.getParameterNames();
         while (requestEnum.hasMoreElements()) {
             String name = (String) requestEnum.nextElement();
             String value = request.getParameter(name);
             buffer.append(URLEncDec.encode(name));
             buffer.append('=');
             buffer.append(URLEncDec.encode(value));
             if (requestEnum.hasMoreElements()) {
                 buffer.append('&');
             }
         }
         return (buffer.toString());
     }
     
     // parses the cookies from the response header and adds them in
     // the HTTP response.
     private void processCookies(Map headers, HttpServletResponse response) {
         if (utilDebug.messageEnabled()) {
             utilDebug.message("processCookies : headers : " + headers);
         }
         
         if (headers == null || headers.isEmpty()) {
             return;
         }
         
         for (Iterator hrs = headers.entrySet().iterator(); hrs.hasNext();) {
             Map.Entry me = (Map.Entry)hrs.next();
             String key = (String) me.getKey();
             if (key != null && (key.equalsIgnoreCase("Set-cookie") ||
                 (key.equalsIgnoreCase("Cookie")))) {
                 List list = (List)me.getValue();
                 if (list == null || list.isEmpty()) {
                     continue;
                 }
                 Cookie cookie = null;
                 String domain = null;
                 String path = null;
                 for (Iterator it = list.iterator(); it.hasNext(); ) {
                     String cookieStr = (String)it.next();
                     if (utilDebug.messageEnabled()) {
                         utilDebug.message("processCookies : cookie : "
                         + cookieStr);
                     }
                     StringTokenizer stz = new StringTokenizer(cookieStr, ";");
                     if (stz.hasMoreTokens()) {
                         String nameValue = (String)stz.nextToken();
                         int index = nameValue.indexOf("=");
                         if (index == -1) {
                             continue;
                         }
                         String tmpName = nameValue.substring(0, index).trim();
                         String value = nameValue.substring(index + 1);
                         Set domains = getCookieDomains();
                         if (!domains.isEmpty()) {
                             for (Iterator itcd = domains.iterator();
                             itcd.hasNext(); ) {
                                 domain = (String)itcd.next();
                                 cookie = createCookie(tmpName, value, domain);
                                 response.addCookie(cookie);
                             }
                         } else {
                             cookie = createCookie(tmpName, value, null);
                             response.addCookie(cookie);
                         }
                     }
                 }
             }
         }
     }
     
     // Get cookies string from HTTP request object
     private String getCookiesString(HttpServletRequest request) {
         Cookie cookies[] = request.getCookies();
         StringBuffer cookieStr = null;
         String strCookies = null;
         // Process Cookies
         if (cookies != null) {
             for (int nCookie = 0; nCookie < cookies.length; nCookie++) {
                 if (utilDebug.messageEnabled()) {
                     utilDebug.message("Cookie name = "
                     + cookies[nCookie].getName());
                     utilDebug.message("Cookie value = "
                     + cookies[nCookie].getValue());
                 }
                 if (cookieStr == null) {
                     cookieStr = new StringBuffer();
                 } else {
                     cookieStr.append(";");
                 }
                 cookieStr.append(cookies[nCookie].getName())
                 .append("=")
                 .append(cookies[nCookie].getValue());
             }
         }
         if (cookieStr != null) {
             strCookies = cookieStr.toString();
         }
         return strCookies;
     }
 
 
     /**
      * Parses an XML string representation of policy advices and 
      * returns a Map of advices.  The keys of returned map would be advice name 
      * keys. Each key is a String object. The values against each key is a 
      * Set of String(s) of advice values
      *
      * @param advicesXML XML string representation of policy advices conforming
      * to the following DTD. The input string may not be validated against the 
      * dtd for performance reasons.  
 
          <!-- This DTD defines the Advices that could be included in
         ActionDecision nested in PolicyDecision. Agents would post this
         Advices to authentication service URL
 
         Unique Declaration name for DOCTYPE tag:
                   "iPlanet Policy Advices Interface 1.0 DTD"
         -->
 
 
         <!ELEMENT    AttributeValuePair    (Attribute, Value*) >
 
 
         <!-- Attribute defines the attribute name i.e., a configuration
              parameter.
         -->
         <!ELEMENT    Attribute     EMPTY >
         <!ATTLIST    Attribute 
               name    NMTOKEN    #REQUIRED 
         >
 
 
         <!-- Value element represents a value string.
         -->
         <!ELEMENT    Value    ( #PCDATA ) >
 
 
         <!-- Advices element provides some additional info which may help the 
              client could use to influence the policy decision
         -->
         <!ELEMENT    Advices   ( AttributeValuePair+ ) >
 
      *
      * @return the map of policy advices parsed from the passed in advicesXML
      *         If the passed in advicesXML is null, null would be returned
 
      */
     public static Map parseAdvicesXML(String advicesXML) {
 
         if(utilDebug.messageEnabled()) {
             utilDebug.message("parseAdvicesXML():"
                     + " entering, advicesXML= " + advicesXML);
         }
 
         Map advices = null;
         if (advicesXML != null) {
             Document document = XMLUtils.toDOMDocument(advicesXML,
                                                        utilDebug);
             if (document != null) {
                 Node advicesNode 
                     = XMLUtils.getRootNode(
                         document, Constants.ADVICES_TAG_NAME);
                 if (advicesNode != null) {
                     advices = XMLUtils.parseAttributeValuePairTags(
                         advicesNode);
                 } else {
                     utilDebug.message(
                         "parseAdvicesXML():advicesNode is null");
                 }
             } else {
                 utilDebug.message("parseAdvicesXML(): document is null");
             }
         }
 
         if(utilDebug.messageEnabled()) {
             utilDebug.message("parseAdvicesXML():" +
                                " returning, advices= " + advices);
         }
 
         return advices;
     }
 }
