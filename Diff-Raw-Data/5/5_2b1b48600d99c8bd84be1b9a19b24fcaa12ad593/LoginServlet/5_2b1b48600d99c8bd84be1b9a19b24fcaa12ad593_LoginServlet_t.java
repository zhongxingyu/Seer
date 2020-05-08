 /**
  * Copyright (C) 2003 FEIDE
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package no.feide.moria.servlet;
 
 import java.util.Properties;
 import java.util.Arrays;
 import java.util.logging.Logger;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 import java.util.Timer;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.Vector;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.Cookie;
 
 import no.feide.moria.Configuration;
 import no.feide.moria.ConfigurationException;
 import no.feide.moria.Credentials;
 import no.feide.moria.SessionException;
 import no.feide.moria.NoSuchSessionException;
 import no.feide.moria.SessionStore;
 import no.feide.moria.Session;
 import no.feide.moria.User;
 import no.feide.moria.BackendException;
 import no.feide.moria.SessionStoreTask;
 import no.feide.moria.authorization.WebService;
 import no.feide.moria.stats.StatsStore;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.context.Context;
 import org.apache.velocity.servlet.VelocityServlet;
 import org.apache.velocity.exception.ParseErrorException;
 import org.apache.velocity.exception.ResourceNotFoundException;
 
 import javax.xml.rpc.server.ServletEndpointContext;
 
 import com.oreilly.servlet.LocaleNegotiator;
 
 /**
  * Presents the actual login page.
  * @author Lars Preben S. Arnesen l.p.arnesen@usit.uio.no
  * @version $Id$
  */
 public class LoginServlet extends VelocityServlet {
     
     /** Used for logging. */
     private static Logger log = Logger.getLogger(LoginServlet.class.toString());
     /** Statistics */
     private StatsStore stats = StatsStore.getInstance();
 
     /** Constant for property lookup. */
     private static String NOSESSION  = "nosession";
 
     /** Constant for property lookup. */
     private static String MAXLOGIN   = "maxlogin";
 
     /** Constant for property lookup. */
     private static String AUTHFAILED = "auth";
 
     /** Constant for property lookup. */
     private static String GENERIC    = "generic";
 
 
     /** The URL the user should post it's username and password to. */
     private String loginURL;
 
     /** Local pointer to session store. */
     private SessionStore sessionStore;
     
     /** Flag if the user should be displayed a complete list of attributes. */
     private boolean showAllAttributes = false;
 
     /** Default language */
     private static String defaultLang;
 
     /** Timer for the session time out service. */
     Timer sessionTimer = new Timer(true);
 
     
 
     /**
      * Some basic initialization.
      * @throws ServletException If a <code>SessionException</code> or a
      *                          <code>ConfigurationException</code> is caught.
      */
     public void init()
     throws ServletException {
         log.finer("init()");
         
         try {
             
             /* Set default language */
             defaultLang = Configuration.getProperty("no.feide.moria.defaultLanguage");
 
             /* Initialize session timeout timer */
             int sessionDelaySec = new Integer(Configuration.getProperty("no.feide.moria.SessionTimerDelay")).intValue();
             log.config("Starting time out service. Repeat every "+sessionDelaySec+" seconds.");
             sessionTimer.scheduleAtFixedRate(new SessionStoreTask(), new Date(), sessionDelaySec*1000);
         
             /* Set local pointer to session store. */
             sessionStore = SessionStore.getInstance();
         } catch (SessionException e) {
             log.severe("SessionException caught and re-thrown as ServletException");
             throw new ServletException("SessionException caught", e);
         } catch (ConfigurationException e) {
             log.severe("ConfigurationException caught and re-thrown as ServletException");
             throw new ServletException("ConfigurationException caught", e);
         }
     }
 
 
     /**
      * Stops the background maintenance thread.
      */
     public void destroy() {
         log.finer("destroy()");
         sessionTimer.cancel();
     }
    
 
 
     /**
      * Called by the VelocityServlet init(). Reads the template path
      * from Properties.
      * @throws IOException If a <code>ConfigurationException</code> is
      *                     caught.
      */
     protected Properties loadConfiguration(ServletConfig config )
     throws IOException, FileNotFoundException {
         log.finer("loadConfiguration(ServletConfig)");
 
         try {
             
             loginURL = Configuration.getProperty("no.feide.moria.LoginURL");
 
             Properties p = new Properties();
             String path = Configuration.getProperty("no.feide.moria.servlet.TemplateDir");
             
 
             p.setProperty("file.resource.loader.cache", "true");
             p.setProperty("file.resource.loader.modificationCheckInterval","0");
 
             /* If path is null, log it. */ 
             if (path == null) {
                 log.severe("Path to Velocity templates not set.");
                 throw new FileNotFoundException("Template path not found.");
             }
 
             p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,  path);
             p.setProperty("runtime.log", Configuration.getProperty("no.feide.moria.VelocityLog"));
             
             return p;
             
         } catch (ConfigurationException e) {
             log.severe("ConfigurationException caught and re-thrown as IOException");
             throw new IOException("ConfigurationException caught");
         }
     }
 
 
 
 
 
 
     /**
      * Handles all http requests from the client. A login page is
      * returned for GET-requests and POST requests are considered as
      * login attempts.
      * @param request  The http request
      * @param response The http response
      * @param context  The Velocity contex
      * @return Template to be used for request
      */   
     public Template handleRequest( HttpServletRequest request,
 	HttpServletResponse response, Context context ) throws ServletException {        
         
         log.finer("handleRequest(HttpServletRequest, HttpServletResponse, Context)");
 
 
         /* A GET should only return the login page. POST is used for
          * login attempts.*/ 
         try {
             if (request.getMethod().equals("GET")) 
                 return loginPage(request, response, context);
                         
             else if (request.getMethod().equals("POST"))
                 return attemptLogin(request, response, context);
 
             else {
                 log.severe("Unsupported http request: "+request.getMethod());
                 return genLoginTemplate(request, response, context, null, GENERIC);
             }
         }
 
         catch( ParseErrorException e ) {
             log.severe("Parse error. " + e);
             throw new ServletException(e);
         }
 
         catch( ResourceNotFoundException e ) {
             log.severe("Template file not found. " + e);
             throw new ServletException(e);
         }
 
         catch( Exception e ) {
             StringWriter stackTrace = new StringWriter();
             e.printStackTrace(new PrintWriter(stackTrace));
             log.severe("Unspecified error during template parsing: \n" + stackTrace.toString());
             throw new ServletException(e);
         }
         
     }
 
 
 
     /**
      *  Creates a Template based on the login tamplate file. If an
      *  error message is supplied, the error message is displayed. If
      *  no sessionID is supplied the login login form is not displayed.
      */
     private Template genLoginTemplate(HttpServletRequest request, HttpServletResponse response, Context context, Session session, String errorType) throws ParseErrorException, ResourceNotFoundException, MissingResourceException, Exception {
 
         String bundleName = "login";
         String acceptLanguage = request.getHeader("Accept-Language");
         String sessionID = null;
         String selectedLanguage = defaultLang;
         ResourceBundle bundle = null;
         ResourceBundle fallback = null;
 
         if (session != null) {
             sessionID = session.getID();
         }
 
        if (acceptLanguage == null || acceptLanguage.equals("")) 
             acceptLanguage = "no";
 
         StringTokenizer tokenizer = new StringTokenizer(acceptLanguage, ",");
         Locale locale = null;
 
         /* Find fallback resource bundle. */
         try {
             fallback = ResourceBundle.getBundle(bundleName, new Locale("bogus"));
         }
         catch (MissingResourceException e) {
             /* No fallback */
         }            
 
         /* Parse Accept-Language and find matching resource bundle */
         while (tokenizer.hasMoreTokens()) {
             String lang = tokenizer.nextToken();
             int index;
 
             /* Languages are devided by ";" */
             if ((index = lang.indexOf(";")) != -1) {
                 lang = lang.substring(0, index);
             }
 
             lang = lang.trim();
 
             /* Language and country is devided by "-" (optional) */
             if ((index = lang.indexOf("-")) != -1) {
                 lang = lang.substring(0, index);
             }
             
             selectedLanguage = lang;
                             
             locale = new Locale(lang);
             bundle = ResourceBundle.getBundle(bundleName, locale);
 
             /* Abort search if a bundle (not fallback) is found */
             if (bundle != fallback) 
                 break;
 
             /* About search if the fallback bundle is actually requested */
             else if (bundle == fallback && locale.getLanguage().equals(Locale.getDefault().getLanguage()))
                 break;
 
         }
 
 
 
         /* Should never happen, but just in case. */
         if (bundle == null)
             bundle = ResourceBundle.getBundle(bundleName, new Locale(defaultLang));
        
 
         String wsName = null;
         String wsURL  = null;
 
         if (session != null) {
             wsName = session.getWebService().getName();
             wsURL  = session.getWebService().getUrl();
         }
 
         /* Set template-variables from properties */
         for (Enumeration e = bundle.getKeys(); e.hasMoreElements();) {
             String key = (String) e.nextElement();
             String value = bundle.getString(key);
             int index;
             
             /* This changes WS_NAME to hyperlink in all property
              * strings from the bundle.*/
             if ((index = value.indexOf("WS_NAME")) != -1 && wsName != null) {
                 value = value.substring(0, index)+"<A href=\""+wsURL+"\">"+wsName+"</A>"+value.substring(index+7, value.length());
             }
 
             context.put(key, value);
         }   
 
         /* Get realm from cookie or parameter. */
         String realm = "";
         realm = request.getParameter("realm");
             
         if (realm == null) {
             realm = "";
             Cookie[] cookies = request.getCookies();
             if (cookies != null) {
                 for (int i = 0; i < cookies.length; i++) {
                     if (cookies[i].getName().equals("realm")) {
                         realm = cookies[i].getValue();
                     }
                 }
             }
         }
 
         
         /* List of organizations */
         HashMap orgNames = Configuration.getOrgNames(selectedLanguage);
         if (orgNames == null)
             orgNames = Configuration.getOrgNames(defaultLang);
         
         // Sort list by organization name
         String  orgList  = "";
         String[] orgNamesStr = (String[]) orgNames.keySet().toArray(new String[orgNames.size()]);
         Arrays.sort(orgNamesStr);
 
         // Create option for selection list
         for (int i = 0; i < orgNamesStr.length; i++) {
 
             String orgShort = (String) orgNamesStr[i];
             if (!realm.equals(orgShort))
                 orgList += "<option value=\""+orgShort+"\">"+orgNames.get(orgShort)+"</option>";
             else
                 orgList += "<option selected=\"true\" value=\""+orgShort+"\">"+orgNames.get(orgShort)+"</option>";
         }
 
         context.put("orgListOptions", orgList);
 
 
         /* Set or reset error messages */
         if (errorType != null) {
             context.put("errorMessage", context.get("error_"+errorType));
             context.put("errorDescription", context.get("error_"+errorType+"_desc"));
         }
      
         else {
             context.remove("errorMessage");
             context.remove("errorDescription");
         }
 
 
         if (sessionID != null) { 
             context.put("loginURL", loginURL+"?id="+sessionID);
 
             String secLevel = session.getAttributesSecLevel().toLowerCase();
             context.put("expl_data", context.get("expl_data_"+secLevel));
 
             /* Detailed list of attributes */
             if (showAllAttributes) {
                 Vector attrNames = new Vector();
                 HashMap attributes = session.getWebService().getAttributes();
                 for (Iterator it = attributes.keySet().iterator(); it.hasNext();) {
                     attrNames.add(context.get("ldap_"+it.next()));
                 }
                 context.put("attrNames", attrNames);
 
                 /* Link to page with detailed attribute list. */
                 if (context.get("attrs_hide") != null)
                     context.put("showHideLink", "<A href=\""+loginURL+"?id="+sessionID+"\">"+context.get("attrs_hide")+"</A>");
                 else 
                     context.put("showHideLink", "");
             }
             else if (context.get("attrs_show") != null) {
                 context.put("showHideLink", "<A href=\""+loginURL+"?id="+sessionID+"&showAttrs=yes\">"+context.get("attrs_show")+"</A>");
             }
             else 
                 context.put("showHideLink", "");
         }
         else 
             /* If no sessionID then remove loginURL */
             context.remove("loginURL");
 
         
         return getTemplate("login.vtl");
     }
 
 
     
 
 
     /**
      *  Generates a template for the loginPage. The request should
      *  contain a valid Moria sessionID.
      *  @param request  The http request
      *  @param response The http response
      *  @param context  The Velocity context
      *  @return Template, a login form or an error message if the
      *  session is invalid.
      */
     private Template loginPage(HttpServletRequest request, HttpServletResponse response, Context context) throws ParseErrorException, ResourceNotFoundException, Exception {
 
         log.finer("loginPage(HttpServletRequest, HttpServletResponse, Context");
 
         /* Get session ID */
         String id = request.getParameter("id");
         log.fine("SessionID: "+id);
 
         if (request.getParameter("showAttrs") != null)
             showAllAttributes = request.getParameter("showAttrs").equals("yes");
         else 
             showAllAttributes = false;
 
 
 
         /* Try to use SSO */
         Session existingSession = null; 
         HttpSession httpSession = 
             ((HttpServletRequest)request).getSession(true);
             
         /* Find existing session */
         String existingSessionID = (String) httpSession.getAttribute("moriaID");
 
         try {
             existingSession = sessionStore.getSession(existingSessionID);
         }
 
         catch (NoSuchSessionException e) {
             /* If no old session exist, then SSO is impossible.
              * Continue with normal authentication. */
             log.fine("Did not find SSO session: "+existingSessionID);
             existingSession = null;
         }
 
         try {
             Session session = sessionStore.getSession(id);
 
             httpSession.setAttribute("moriaID", session.getID());
 
             
             if (existingSession != null) {
                 log.fine("Existing SSO session found.");
 
                 /* Session has to be authenticated and locked to be
                    used in SSO. If not locked another web service is
                    using the session. */
                 if (existingSession.isAuthenticated() && existingSession.isLocked()) {
                     HashMap cachedAttributes = existingSession.getCachedAttributes();
                     if (cachedAttributes != null && cachedAttributes.size() > 0) {
 
                         if (session.allowSso()) {
                             log.info("Redirect to WebService (SSO), "+session.getWebService().getName());
                            stats.loginAttempt(session.getWebService().getId(), "SSO");
                             session.setCachedAttributes(cachedAttributes);
                             sessionStore.deleteSession(existingSession);
                             session.unlock(existingSession.getUser());
                             redirectToWebService(response, session);
                             return null;
                         }
                     }
                 }
             }
 
             session.initiateAuthentication();
 
             return genLoginTemplate(request, response, context, session, null);
         }
         
         catch (NoSuchSessionException e) {
             log.warning("Request Login page: DENIED, Session not found: "+id);
             return genLoginTemplate(request, response, context, null, NOSESSION);
         }
 
         catch (SessionException e) {
             log.warning("Session exception:\n"+e);
             return genLoginTemplate(request, response, context, null, GENERIC);
         }
     }
 
 
 
 
 
 
     /**
      *  Authenticates the user based on the http request. The request
      *  should be supplied with parameters (from the login form) with
      *  username and password. The user is authenticated and
      *  redirected back to the originating web service if the
      *  authentication is successful. If not the user is presented
      *  with a new login form and an error message.
      *  @param request  The http request containing username and password
      *  @param response The http response
      *  @param contex   The Velocity context
      *  @return Template, the login form
      */
     private Template attemptLogin(HttpServletRequest request, HttpServletResponse response, Context context) throws ParseErrorException, ResourceNotFoundException, Exception {
 
         log.finer("attemptLogin(HttpServletRequest, HttpServletResponse, Context)");
 
         Session session = null;
         String id       = request.getParameter("id");
         String username = request.getParameter("username");
         String realm    = request.getParameter("realm");
         String password = request.getParameter("password");
 
 
         /* Concatinate realm with username */
         if ((realm != null && !realm.equals("")) && (username != null && username.indexOf("@") == -1))
             username += "@"+realm;
 
         /* Get session */
         try {
             session = sessionStore.getSession(id);
         }
         
         catch (NoSuchSessionException e) {
             return genLoginTemplate(request, response, context, null, NOSESSION);
         }
 
         catch (SessionException e) {
             return genLoginTemplate(request, response, context, null, GENERIC);
         }
 
         String log_prefix = "Authentication attempt from "+username+": ";
 
         if (!session.authenticationInitiated()) {
             log.warning(log_prefix+"DENIED, Login page not requested SID="+session.getID());
             throw new Exception("Premature login attempt.");
         }
 
 
 
         /* Authenticate */
         try {
             Credentials c = new Credentials(username, password);
             if (!session.authenticateUser(c)) {
                 log.info(log_prefix+"FAILED");
                stats.loginAttempt(session.getWebService().getId(), "FAILED");
         
                 /* If the user has exceeded the maximum login
                 attempts, the session is now gone. */
                 try {
                     session = sessionStore.getSession(id);
                 }
                 catch (NoSuchSessionException e) {
                     return genLoginTemplate(request, response, context, null, MAXLOGIN);
                 }
 
                 return genLoginTemplate(request, response, context, 
                                         session, AUTHFAILED);
             }
         } 
         
         catch (BackendException e) {
             log.severe("BackendException caught and re-thrown as ServletException\n"+e);
 
             return genLoginTemplate(request, response, context, null, GENERIC);
         } 
 
         catch (SessionException e) {
             log.severe("SessionException caught and re-thrown as ServletException\n"+e);
                 return genLoginTemplate(request, response, context, null, GENERIC);
         }
 
 
         /* Success; redirect to the original session URL and
          * include the updated session ID in URL and HttpSession. */
         
         log.info(log_prefix+"SUCCESS");
        stats.loginAttempt(session.getWebService().getId(), "SUCCESS");
 
         HttpSession httpSession = 
             ((HttpServletRequest)request).getSession(true);
         
 
         httpSession.setAttribute("moriaID", session.getID());
 
         /* Remember realm (cookie). */
         Cookie cookie = new Cookie("realm", realm);
         int validDays = new Integer(Configuration.getProperty("no.feide.moria.servlet.realmCookieValidDays")).intValue();
         cookie.setMaxAge(validDays*24*60*60); // Days to seconds
         cookie.setComment("Home organization");
         cookie.setVersion(1);
         response.addCookie(cookie);
 
         redirectToWebService(response, session);
     
         return null; // Do not use template for redirect.
     }
     
     /**
      * Set response to 302 (redirect) and location header so that the
      * user is redirected back to the web service.
      * @throws ConfigurationException If unable to get the session's redirect
      *                                URL.
      */ 
     private void redirectToWebService(HttpServletResponse response, Session session)
     throws ConfigurationException {
         response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  
         response.setHeader("Location", session.getRedirectURL());
         log.info("Authenticated user, redirected back to: "+session.getRedirectURL());
     }
 
 
 }
