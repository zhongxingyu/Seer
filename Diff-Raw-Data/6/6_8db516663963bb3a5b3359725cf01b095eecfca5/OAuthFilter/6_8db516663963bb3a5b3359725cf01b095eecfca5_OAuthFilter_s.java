 package com.sampullara.firebrowser;
 
 import net.oauth.OAuthAccessor;
 import net.oauth.OAuthConsumer;
 import net.oauth.OAuthException;
 import net.oauth.OAuthServiceProvider;
 import net.oauth.client.OAuthClient;
 import net.oauth.client.httpclient4.HttpClient4;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Cookie;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Do the OAuth dance
  * <p/>
  * User: samp
  * Date: Jul 17, 2009
  * Time: 2:13:21 PM
  * To change this template use File | Settings | File Templates.
  */
 public class OAuthFilter implements Filter {
   private static final int EXPIRES_28_DAYS = 24*3600*28;
 
   private OAuthConsumer consumer;
   private HttpClient4 httpClient;
   private static final int DELETE_COOKIE = 0;
   private static final int TEMPORARY_COOKIE = -1;
 
   /**
    * These are the relevant settings for the FireEagle OAuth endpoint
    * 
    * @param filterConfig
    * @throws ServletException
    */
   public void init(FilterConfig filterConfig) throws ServletException {
     httpClient = new HttpClient4();
     OAuthServiceProvider provider = new OAuthServiceProvider(
             "https://fireeagle.yahooapis.com/oauth/request_token",
             "https://fireeagle.yahoo.net/oauth/authorize",
             "https://fireeagle.yahooapis.com/oauth/access_token");
     consumer = new OAuthConsumer(
            "url",
            "key",
            "secret",
             provider);
   }
 
   /**
    * Get authorization for accessing FireEagle
    * 
    * @param request
    * @param response
    * @param chain
    * @throws IOException
    * @throws ServletException
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
     HttpServletRequest req = (HttpServletRequest) request;
     HttpServletResponse res = (HttpServletResponse) response;
 
     // We store the values for the access token, request token and token secret in cookies
     // If they are present we populate their values for later use.
     String at = null;
     String rt = null;
     String ts = null;
     Cookie[] cookies = req.getCookies();
     if (cookies != null) {
       for (Cookie cookie : cookies) {
         String key = cookie.getName();
         String value = cookie.getValue();
         if (key.equals("at")) {
           at = value;
         } else if (key.equals("rt")) {
           rt = value;
         } else if (key.equals("ts")) {
           ts = value;
         }
       }
     }
 
     try {
       OAuthClient oc = new OAuthClient(httpClient);
       OAuthAccessor oa = new OAuthAccessor(consumer);
       if (at == null) {
         // OAuth 1.0a will return an oauth_verifier that we must return to get the access token
         String ov = req.getParameter("oauth_verifier");
         if (ov == null && rt == null) {
           // We ask for a request token and use that to redirect the client to FireEagle
           oc.getRequestToken(oa);
           // Use cookies to transfer data between requests temporarily
           addCookie(res, "rt", oa.requestToken, TEMPORARY_COOKIE);
           addCookie(res, "ts", oa.tokenSecret, TEMPORARY_COOKIE);
           res.sendRedirect("https://fireeagle.yahoo.net/oauth/authorize?oauth_token=" + oa.requestToken);
           return;
         } else {
           // Someone has returned after we have already grabbed a request token but we
           // don't yet have an AT yet. Let's use all the info to got and get one
           Map<String, String> params = new HashMap<String, String>();
           if (ov != null) params.put("oauth_verfier", ov);
           if (rt != null) oa.requestToken = rt;
           if (ts != null) oa.tokenSecret = ts;
           oc.getAccessToken(oa, "GET", params.entrySet());
           // Store the result in the cookies for a long time between authorizations
           addCookie(res, "at", oa.accessToken, EXPIRES_28_DAYS);
           addCookie(res, "ts", oa.tokenSecret, EXPIRES_28_DAYS);
           // Delete the request token
           deleteCookie(res, "rt");
         }
       } else {
         // We already have an AT and ATS for this browser
         oa.accessToken = at;
         oa.tokenSecret = ts;
       }
       // Pass the OAuthClient and OAuthAccessor to subsequent servlets and JSPs
       request.setAttribute("oa", oa);
       request.setAttribute("oc", oc);
     } catch (OAuthException e) {
       // We have failed to authorize
       res.sendError(401, e.getMessage());
       // Delete all the cookies just in case they try again
       deleteCookie(res, "rt");
       deleteCookie(res, "at");
       deleteCookie(res, "ts");
       return;
     } catch (URISyntaxException e) {
       // Assertion error
       res.sendError(500, e.getMessage());
       return;
     }
     chain.doFilter(request, response);
   }
 
   /**
    * Cookies are deleted when their expiry is 0
    *
    * @param res
    * @param name
    */
   private void deleteCookie(HttpServletResponse res, String name) {
     addCookie(res, name, "", DELETE_COOKIE);
   }
 
   /**
    * Add a cookie to the response
    * 
    * @param res
    * @param name
    * @param value
    * @param expires
    */
   private void addCookie(HttpServletResponse res, String name, String value, int expires) {
     Cookie rtcookie = new Cookie(name, value);
     rtcookie.setMaxAge(expires);
     res.addCookie(rtcookie);
   }
 
   public void destroy() {
   }
 }
