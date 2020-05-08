 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controllers.secure.providers;
 
 import annotations.Provides;
 import controllers.secure.SecurityProvider;
 import extension.secure.SecurityExtensionPoint;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import models.secure.AuthUser;
 import models.secure.AuthUserImpl;
 import org.expressme.openid.Association;
 import org.expressme.openid.Authentication;
 import org.expressme.openid.Endpoint;
 import org.expressme.openid.OpenIdManager;
 import play.Logger;
 import play.Play;
 import play.cache.Cache;
 import play.mvc.Before;
 import play.mvc.Router;
 import utils.secure.GoogleAuthProcess;
 
 /**
  *
  * @author Julien Durillon
  */
 @Provides("google")
 public class GoogleSecurityProvider extends SecurityProvider {
 
     public static final String GOOGLEURL = "https://www.google.com/accounts/o8/site-xrds?hd=";
 
     @Before(priority = 50, unless = {"askGoogle", "finishAuth", "logout"})
     static void checkAccess() {
        play.Logger.debug("checkAccess Google for %s", getControllerClass().getCanonicalName());
 
         flash.put(PROVIDER_KEY, "google");
 
        if (!GoogleSecurityProvider.class.isAssignableFrom(getControllerClass())) {
             play.Logger.debug("Not assignable from");
             if (!session.contains("username")) {
                 play.Logger.debug("No username");
                 flash.put("url", "POST".equals(request.method) ? "/" : request.url);
                 askGoogle();
             }
 
             doCheck();
         }
     }
 
     private static void askGoogle() {
 
         String domain = Play.configuration.getProperty("auth.googledomain", request.domain);
 
         Logger.info("Authenticating for domain %s", domain);
 
         OpenIdManager manager = new OpenIdManager();
 
         Long id = GoogleAuthProcess.nextID();
         String finishID = "auth" + id.toString();
 
         manager.setRealm("http://" + request.domain + "/");
         Map map = new HashMap();
         map.put("id", finishID);
         manager.setReturnTo("http://" + request.domain + Router.reverse("secure.providers.GoogleSecurityProvider.finishAuth", map));
 
 
         Endpoint endpoint = manager.lookupEndpoint(GOOGLEURL + domain);
         Association association = manager.lookupAssociation(endpoint);
         String authUrl = manager.getAuthenticationUrl(endpoint, association);
 
         GoogleAuthProcess process = new GoogleAuthProcess();
         process.manager = manager;
         process.association = association;
         process.endPoint = endpoint;
 
         Cache.add(finishID, process, "10min");
 
         flash.keep("url");
         flash.put(PROVIDER_KEY, "google");
         redirect(authUrl);
     }
 
     public static void finishAuth(String id) {
 
         try {
             GoogleAuthProcess process = (GoogleAuthProcess) Cache.get(id);
             if (process == null) {
                 Logger.error("No Google Authentication process");
                 return;
             }
             OpenIdManager manager = process.manager;
             Authentication auth = manager.getAuthentication(createRequest(request.url), process.association.getRawMacKey(), "ext1");
 
             session.put("username", auth.getFirstname() + " " + auth.getLastname().toUpperCase());
             session.put("identity", auth.getIdentity());
             session.put("fullname", auth.getFullname());
             session.put("firstname", auth.getFirstname());
             session.put("lastname", auth.getLastname());
             session.put("language", auth.getLanguage());
             session.put("email", auth.getEmail());
             session.put(PROVIDER_KEY, "google");
             SecurityExtensionPoint.invokeFor(GoogleSecurityProvider.class, "onAuthenticated");
 
             redirectToOriginalURL();
         } catch (Throwable ex) {
             Logger.error(ex.getMessage());
         }
 
 
     }
 
     static HttpServletRequest createRequest(String url) throws UnsupportedEncodingException {
         int pos = url.indexOf('?');
         if (pos == (-1)) {
             throw new IllegalArgumentException("Bad url.");
         }
         String query = url.substring(pos + 1);
         String[] urlparams = query.split("[\\&]+");
         final Map<String, String> map = new HashMap<String, String>();
         for (String param : urlparams) {
             pos = param.indexOf('=');
             if (pos == (-1)) {
                 throw new IllegalArgumentException("Bad url.");
             }
             String key = param.substring(0, pos);
             String value = param.substring(pos + 1);
             map.put(key, URLDecoder.decode(value, "UTF-8"));
         }
         return (HttpServletRequest) Proxy.newProxyInstance(
            GoogleSecurityProvider.class.getClassLoader(),
            new Class[]{HttpServletRequest.class},
            new InvocationHandler() {
 
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("getParameter")) {
                        return map.get((String) args[0]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
            });
     }
 
     /**
      * Get informations about authenticated user.
      * @param session Should be the current session
      * @return
      */
     public static AuthUser doGetAuthUser() {
         Class cl = getProvider(session.get(SecurityProvider.PROVIDER_KEY));
 
         AuthUserImpl au = new AuthUserImpl(cl, session.get("username"));
 
         au.addField("identity",     session.get("identity"));
         au.addField("fullname",     session.get("fullname"));
         au.addField("firstname",    session.get("firstname"));
         au.addField("lastname",     session.get("lastname"));
         au.addField("language",     session.get("language"));
         au.addField("email",        session.get("email"));
 
 
         return au;
     }
 }
