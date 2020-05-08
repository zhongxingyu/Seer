 package no.feide.moria.servlet;
 
 import java.util.Properties;
 import java.util.logging.Logger;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 
 import java.io.PrintWriter;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import no.feide.moria.Credentials;
 import no.feide.moria.SessionException;
 import no.feide.moria.NoSuchSessionException;
 import no.feide.moria.SessionStore;
 import no.feide.moria.Session;
 import no.feide.moria.User;
 import no.feide.moria.BackendException;
 
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
     private static String NOSESSION  = "nosession";
     private static String MAXLOGIN   = "maxlogin";
     private static String AUTHFAILED = "auth";
     private static String GENERIC    = "generic";
 
     String loginURL;
 
     SessionStore sessionStore = SessionStore.getInstance();
 
 
     /**
      *   Called by the VelocityServlet init(). Reads the template path
      *   from Properties.
      */
     protected Properties loadConfiguration(ServletConfig config )
         throws IOException, FileNotFoundException {
  
        log.finer("loadConfiguration(ServletConfig)");
 
 
        if (System.getProperty("no.feide.moria.config.file") == null) {
            log.fine("no.feide.moria.config.file not set; default is \"/moria.properties\"");
            System.getProperties().load(getClass().getResourceAsStream("/moria.properties"));
        }
        else {
            log.fine("no.feide.moria.config.file set to \""+System.getProperty("no.feide.moria.config.file")+'\"');
            System.getProperties().load(getClass().getResourceAsStream(System.getProperty("no.feide.moria.config.file")));
        }
 
 
        loginURL = System.getProperty("no.feide.moria.LoginURL");
 
         Properties p = new Properties();
        String path = System.getProperty("no.feide.moria.servlet.TemplateDir");

 
         /* If path is null, log it. */ // Should also abort?
         if (path == null) {
             log.severe("Path to Velocity templates not set. (Properties, moria.properties)");
             path = "/";
         }
 
         p.setProperty( Velocity.FILE_RESOURCE_LOADER_PATH,  path );
         // p.setProperty( "runtime.log", path + "velocity.log" );
         // Should set log directory.
 
         return p;
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
             log.severe("Unspecified error during template parsing: " + e);
             throw new ServletException(e);
         }
         
     }
 
 
 
 
 
 
     /**
      *  Creates a Template based on the login tamplate file. If an
      *  error message is supplied, the error message is displayed. If
      *  no sessionID is supplied the login login form is not displayed.
      */
     private Template genLoginTemplate(HttpServletRequest request, HttpServletResponse response, Context context, String sessionID, String errorType) throws ParseErrorException, ResourceNotFoundException, MissingResourceException, Exception {
 
         String bundleName = "login";
         String acceptLanguage = request.getHeader("Accept-Language");
         ResourceBundle bundle = null;
         ResourceBundle fallback = null;
         // nn, no;q=0.80, nb;q=0.60, en-us;q=0.40, en;q=0.20
 
         StringTokenizer tokenizer = new StringTokenizer(acceptLanguage, ",");
         Locale locale = null;
 
         // Find fallback resource bundle.
         try {
             fallback = ResourceBundle.getBundle(bundleName, new Locale("bogus"));
         }
         catch (MissingResourceException e) {
             // No fallback
         }            
 
         /* Parse Accept-Language and find matching resource bundle */
         while (tokenizer.hasMoreTokens()) {
             String lang = tokenizer.nextToken();
             int index;
 
             // Languages are devided by ";"
             if ((index = lang.indexOf(";")) != -1) {
                 lang = lang.substring(0, index);
             }
 
             lang = lang.trim();
 
             // Language and country is devided by "-" (optional)
             if ((index = lang.indexOf("-")) != -1) {
                 lang = lang.substring(0, index);
             }
             
             
             locale = new Locale(lang);
             bundle = ResourceBundle.getBundle(bundleName, locale);
 
             // Abort search if a bundle (not fallback) is found
             if (bundle != fallback) 
                 break;
 
             // About search if the fallback bundle is actually requested
             else if (bundle == fallback && locale.getLanguage().equals(Locale.getDefault().getLanguage()))
                 break;
 
         }
 
 
 
         // Should never happen, but just in case.
         if (bundle == null)
             bundle = ResourceBundle.getBundle(bundleName, new Locale("no"));
 
 
         // Set template-variables from properties
         for (Enumeration e = bundle.getKeys(); e.hasMoreElements();) {
             String key = (String) e.nextElement();
             String value = bundle.getString(key);
             int index;
             
             if ((index = value.indexOf("WS_NAME")) != -1) {
                 // TODO: Read name+url from web service data.
                 value = value.substring(0, index)+"<A href=\"http://www.uio.no\">Tjenestenavn</A>"+value.substring(index+7, value.length());
             }
                 
 
             context.put(key, value);
         }        
 
         // Set or reset error messages
         if (errorType != null) {
             context.put("errorMessage", bundle.getString("error_"+errorType));
             context.put("errorDescription", bundle.getString("error_"+errorType+"_desc"));
         }
      
         else {
             context.remove("errorMessage");
             context.remove("errorDescription");
         }
 
 
         // If no sessionID then remove loginURL
         if (sessionID != null) 
             context.put("loginURL", loginURL+"?id="+sessionID);
         
         else 
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
         
         String id = request.getParameter("id");
         
         try {
             
             Session session = sessionStore.getSession(id);
 
             if (session == null) {
                 return genLoginTemplate(request, response, context, null, NOSESSION);
             }
             
             // Should also generate new sessionID
             // (SessionStore.confirmSession()) to avoid the web
             // service to do authentication without users intervention.
             return genLoginTemplate(request, response, context, session.getID(), null);
         }
         
         catch (SessionException e) {
             
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
         String password = request.getParameter("password");
 
         // Get session
         try {
             session = sessionStore.getSession(id);
             if (session == null) {
                 return genLoginTemplate(request, response, context, null, NOSESSION);
             }
 
         }
         
         catch (SessionException e) {
             return genLoginTemplate(request, response, context, null, GENERIC);
         }
 
 
         // Authenticate
         try {
             Credentials c = new Credentials(username, password);
             if (!session.authenticateUser(c)) {
                 log.info("Authentication failed");
         
                 /* If the user has exceeded the maximum login
                 attempts, the session is now gone. */
                 try {
                     session = sessionStore.getSession(id);
                 }
                 catch (NoSuchSessionException e) {
                     return genLoginTemplate(request, response, context, session.getID(), MAXLOGIN);
                 }
 
                 return genLoginTemplate(request, response, context, 
                                         session.getID(), AUTHFAILED);
             }
         } 
         
         catch (BackendException e) {
             // A user-friendly message would be preferable...
             log.severe("BackendException caught and re-thrown as ServletException");
                 return genLoginTemplate(request, response, context, null, GENERIC);
         } 
 
         catch (SessionException e) {
             // A user-friendly message would be preferable...
             log.severe("SessionException caught and re-thrown as ServletException");
                 return genLoginTemplate(request, response, context, null, GENERIC);
         }
 
 
         /* Success; redirect to the original session URL and
          * include the updated session ID. */
 
         response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  
         response.setHeader("Location", session.getRedirectURL());
         log.info("Redirect to Mellon: "+session.getRedirectURL());
     
         return null; // Do not use template for redirect.
     }
 
 
 
 
 
 
 
 }
