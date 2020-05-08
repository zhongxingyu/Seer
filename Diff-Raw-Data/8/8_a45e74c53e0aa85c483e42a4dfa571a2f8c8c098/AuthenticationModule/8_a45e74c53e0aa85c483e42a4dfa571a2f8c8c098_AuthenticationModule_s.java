 /**
  * XWeb project
  * Created by Hamed Abdollahpour
  * https://github.com/abdollahpour/xweb
  */
 
 package ir.xweb.module;
 
 import ir.xweb.server.Constants;
 import ir.xweb.server.XWebUser;
 import ir.xweb.util.Base64;
 import ir.xweb.util.CookieTools;
 import ir.xweb.util.Tools;
 import org.apache.commons.fileupload.FileItem;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class AuthenticationModule extends Module {
 
     private Logger logger = LoggerFactory.getLogger("AuthenticationModule");
 
     public final static String PARAM_COOKIE_AGE = "cookie-age";
 
     public final static String PARAM_XML_SOURCE = "xml-source";
 
     public final static String PARAM_JSON_SOURCE = "json-source";
 
     public final static String PARAM_TEXT_SOURCE = "text-source";
 
     public final static String PARAM_DEFAULT = "default";
 
     public final static String PARAM_REDIRECT = "redirect";
 
     public final static String PARAM_CHECK = "check";
 
     public final static String PARAM_IGNORE = "ignore";
 
     private final static int DEFAULT_COOKIE_AGE = 60 * 60 * 24 * 30; // 1 month
 
     private final Map<String, XWebUser> defaultSource = new HashMap<String, XWebUser>();
 
     private final int cookieAge;
 
     private final String redirect;
 
     private final String check;
 
     private final String ignore;
 
     public AuthenticationModule(final Manager manager, final ModuleInfo info, final ModuleParam properties) {
         super(manager, info, properties);
 
         cookieAge = properties.getInt(PARAM_COOKIE_AGE, DEFAULT_COOKIE_AGE);
 
         redirect = properties.getString(PARAM_REDIRECT, null);
         check = properties.getString(PARAM_CHECK, null);
         ignore = properties.getString(PARAM_IGNORE, null);
 
         if(properties.containsKey(PARAM_XML_SOURCE)) {

         } else if(properties.containsKey(PARAM_JSON_SOURCE)) {

         } else if(properties.containsKey(PARAM_TEXT_SOURCE)) {

         }
     }
 
     private void importXmlSource(final String path) {
         final File file = new File(path);
         if(file.exists()) {
             final SAXBuilder builder = new SAXBuilder();
             final File xmlFile = new File(path);
 
             try {
 
                 final Document document = builder.build(xmlFile);
                 final Element rootNode = document.getRootElement();
                 final List list = rootNode.getChildren("user");
 
                 for (int i = 0; i < list.size(); i++) {
                     final Element u = (Element) list.get(i);
 
                     final String username = u.getAttributeValue("username");
                     final String password = u.getAttributeValue("password");
                     final String role = u.getAttributeValue("role");
 
                     final XWebUser user = new XWebUser() {
                         @Override
                         public String getIdentifier() {
                             return username;
                         }
 
                         @Override
                         public String getRole() {
                             return role;
                         }
 
                         @Override
                         public Object getExtra() {
                             return password;
                         }
                     };
 
                     defaultSource.put(username, user);
                 }
 
             } catch (IOException ex) {
                 logger.error("Error to access XML data source", ex);
             } catch (JDOMException ex) {
                 logger.error("Error to parse", ex);
             }
         } else {
             logger.error("XML source not found: " + path);
         }
     }
 
     private void importJsonSource(final String path) {
         final File file = new File(path);
         if(file.exists()) {
             try {
                 final String text = Tools.readTextFile(file);
                 final JSONArray array = new JSONArray(text);
 
                 for (int i = 0; i < array.length(); i++) {
                     final JSONObject u = array.getJSONObject(i);
 
                     final String username = u.getString("username");
                     final String password = u.getString("password");
                     final String role = u.getString("role");
 
                     XWebUser user = new XWebUser() {
                         @Override
                         public String getIdentifier() {
                             return username;
                         }
 
                         @Override
                         public String getRole() {
                             return password;
                         }
 
                         @Override
                         public Object getExtra() {
                             return role;
                         }
                     };
 
                     defaultSource.put(username, user);
                 }
             } catch (IOException ex) {
                 logger.error("Error to access json source", ex);
             } catch (JSONException ex) {
                 logger.error("Error to parse json source", ex);
             }
 
         } else {
             logger.error("JSON source not found: " + path);
         }
     }
 
     private void importTextSource(final String path) {
         final File file = new File(path);
         if(file.exists()) {
             try {
                 final String text = Tools.readTextFile(file);
                 final BufferedReader reader = new BufferedReader(new StringReader(text));
 
                 String line;
                 while((line = reader.readLine()) != null) {
                     if(line.length() > 0) {
                         final String[] parts = line.split("\t");
                         if(parts.length == 3) {
                             final String username = parts[0];
                             final String password = parts[1];
                             final String role = parts[2];
 
                             XWebUser user = new XWebUser() {
                                 @Override
                                 public String getIdentifier() {
                                     return username;
                                 }
 
                                 @Override
                                 public String getRole() {
                                     return role;
                                 }
 
                                 @Override
                                 public Object getExtra() {
                                     return password;
                                 }
                             };
 
                             defaultSource.put(username, user);
                         } else {
                             logger.error("Illegal line text user source");
                         }
                     }
                 }
             } catch (IOException ex) {
                 logger.error("Error to access text source", ex);
             }
 
         } else {
             logger.error("JSON source not found: " + path);
         }
     }
 
     @Override
     public void init(final ServletContext context) {
         final String defaultUser = getProperties().getString(PARAM_DEFAULT, null);
         if(defaultUser != null) {
             createDefaultUser(context, defaultUser);
         } else {
             logger.warn("Default user not found!");
         }
     }
 
     @Override
     public void doFilter(
             ServletContext context,
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain) throws IOException, ServletException {
 
         HttpSession session = request.getSession();
 
         XWebUser user = (XWebUser) session.getAttribute(Constants.SESSION_USER);
 
         // pre-check to speedup checking
         if(user != null) {
             filterChain.doFilter(request, response);
             return;
         }
 
         // check for permission patterns
         URI uri = null;
         try {
             uri = new URI(request.getRequestURI());
         } catch (Exception ex) {
             // It will never happen because it passed by http request
             throw new IOException(ex);
         }
 
         // we don't care about context path, so we trunk it
         final String path = uri.getPath().substring(request.getContextPath().length());
 
         if(uri.equals(redirect)){
             filterChain.doFilter(request, response);
             return;
         }
 
 
 
         if(check != null && !path.matches(check)) {
             filterChain.doFilter(request, response);
             return;
         }
 
         if(ignore != null && path.matches(ignore)) {
             filterChain.doFilter(request, response);
             return;
         }
 
 
         // login with cookie
         String uuid = CookieTools.getCookieValue(request, Constants.COOKIE_AUTH_REMEMBER);
         if(uuid != null) {
             logger.debug("Try to login with cookie. UUID: " + uuid);
             user = getUserWithUUID(context, uuid);
             if(user != null) {
                 logger.debug("User successfully login with UUID: " + uuid);
                 session.setAttribute(Constants.SESSION_USER, user);
                 filterChain.doFilter(request, response);
                 return;
             } else {
                 logger.debug("UUID not valid for login (maybe expired: " + uuid);
             }
         }
 
         String header = request.getHeader("Authorization");
         if (header != null) {
             logger.debug("Try to authenticate by HTTP authentication");
 
             if(header.startsWith("Basic")) {
                 String base64Token = header.substring(6);
                 String token = new String(Base64.decode(base64Token));
 
                 user = getUserWithUUID (context, uuid);
                 if(user != null) {
                     logger.debug("User successfully login with HTTP authentication: " + token);
                     session.setAttribute(Constants.SESSION_USER, user);
                     filterChain.doFilter(request, response);
                     return;
                 } else {
                     logger.debug("HTTP token not valid for login. maybe expired: " + token);
                 }
 
             } else {
                 throw new IllegalStateException("Unsupported authentication method");
             }
         }
 
 
         // We handle API requests with different authentication method (Role base)
         boolean isApiCall = path.equals(Constants.MODULE_URI_PERFIX);
 
         // for API call there's 3 choice for login
         // 1: Session (already login)
         // 2: Cookie (UUID)
         // 3: HTTP authentication
         if(isApiCall) {
             // After Iteration 4 we will process for permission inside of Module
             filterChain.doFilter( request, response );
             return;
         } else if(redirect != null) {
             if(!redirect.equals(path)) {
                 // redirect to redirect page.
                 // on redirect page we male relative path (not absolute)
 
                 String redirectPath = context.getContextPath() + redirect;
                 String urlPath = context.getContextPath() + uri;
 
                 int lastIndex = redirectPath.lastIndexOf('/');
                 if(lastIndex > -1) {
                     urlPath = urlPath.substring(lastIndex + 1);
                 }
 
                 response.sendRedirect(redirectPath + "?url=" + URLEncoder.encode(urlPath, "UTF-8"));
             } else {
                 filterChain.doFilter( request, response );
                 return;
             }
         } else {
             // Set standard HTTP/1.1 no-cache headers.
             response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
             // Set standard HTTP/1.0 no-cache header.
             response.setHeader("Pragma", "no-cache");
 
             response.setHeader( "WWW-Authenticate", "Basic realm=\"" + "jaRk79" + "\"" );
             response.sendError( HttpServletResponse.SC_UNAUTHORIZED );
         }
     }
 
     @Override
     public void process(ServletContext context, HttpServletRequest request,
                         HttpServletResponse response, ModuleParam params,
                         HashMap<String, FileItem> files) throws IOException {
 
         String action  = params.validate("action", "login|check|logout", true).getString(null);
         logger.debug("Login module request for action = " + action);
 
         /**
          * Actions:
          * login: Fully login into system. User need to enter captcha code also
          * temp_pass: generate temporary password for simple authenticate transactions
          */
 
         if("login".equals(action)) {
             String identifier = params.validate("id", null, true).getString(null);
             // Password hashed with MD5
             String password = params.validate("password", null, true).getString(null);
             String captcha = params.validate("captcha", CaptchaModule.SESSION_CAPTCHA_PATTERN, true).getString(null);
 
             CaptchaModule.validateOrThrow(request, captcha);
 
             logger.info("User try to login: " + identifier);
 
             boolean remember = "true".equals(params.getString("remember", "false"));
             // (user, temporary password, is temporary password (false by default))
             XWebUser user = getUserWithId(context, identifier, password);
 
             if (user != null) {
                 request.getSession().setAttribute(Constants.SESSION_USER, user);
 
                 if(remember) {
                     String uuid = generateUUID(context, identifier);
 
                     if(uuid != null) {
                         CookieTools.addCookie(request, response, Constants.COOKIE_AUTH_REMEMBER, uuid, cookieAge);
                         response.getWriter().write(uuid);
                     }
                 } else {
                     CookieTools.removeCookie(request, response, Constants.COOKIE_AUTH_REMEMBER);
                 }
 
                 logger.info(identifier + " successfully login into system. Remember = " + remember);
 
                 return;
             } else {
                 throw new ModuleException(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
             }
         } else if("logout".equals(action)) {
             request.getSession().invalidate();
             CookieTools.removeCookie(request, response, Constants.COOKIE_AUTH_REMEMBER);
         }
     }
 
     public XWebUser getUserWithUUID(ServletContext context, String uuid) {
         return null;
     }
 
     public XWebUser getUserWithId(ServletContext context, String userId, String pass) {
         return null;
     }
 
     public String generateUUID(ServletContext context, String userId) {
         return null;
     }
 
     /**
      * Create default user if it's not exist
      * @param context
      * @param username
      * @return true if user created false if it's already exist
      */
     public boolean createDefaultUser(ServletContext context, String username) {
         return false;
     }
 
 }
