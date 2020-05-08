 package com.intuit.qbo.plugin;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import oauth.signpost.OAuth;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.basic.DefaultOAuthConsumer;
 import oauth.signpost.basic.DefaultOAuthProvider;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 import oauth.signpost.exception.OAuthNotAuthorizedException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import com.amazonaws.util.json.JSONException;
 import com.amazonaws.util.json.JSONObject;
 import com.intuit.ipp.core.Context;
 import com.intuit.ipp.core.ServiceType;
 import com.intuit.ipp.data.CompanyInfo;
 import com.intuit.ipp.exception.FMSException;
 import com.intuit.ipp.security.IAuthorizer;
 import com.intuit.ipp.security.OAuthAuthorizer;
 import com.intuit.ipp.security.TicketAuthorizer;
 import com.intuit.ipp.services.DataService;
 import com.intuit.ipp.services.QueryResult;
 import com.intuit.ipp.util.Config;
 
 /*
  * This is example code of how you can get oAuth tokens with a ticket
  * And also call v3 services with a ticket
  */
 public class InternalIPPManager {
     public final static String AUTHID = "authid";
 
     public final static String TICKET = "ticket";
 
     public final static String PARENTID = "parentid";
 
     // Set from app.properties
     public static String appToken;
     public static String consumerKey;
     public static String consumerSecret;
 
     public static Properties props = new Properties();
     static {
         try {
             props.load(InternalIPPManager.class
                     .getResourceAsStream("/app.properties"));
         } catch (IOException e) {
         }
 
         appToken = props.getProperty("appToken");
         consumerKey = props.getProperty("oauth_consumer_key");
         consumerSecret = props.getProperty("oauth_consumer_secret");
     }
 
     public static BasicClientCookie buildCookie(String name, String value) {
         BasicClientCookie cookie = new BasicClientCookie(name, value);
         cookie.setDomain(".intuit.com");
         cookie.setPath("/");
         return cookie;
     }
 
     public static Map<String, String> getQueryMap(String query) {
         String[] params = query.split("&");
         Map<String, String> map = new HashMap<String, String>();
         for (String param : params) {
             String name = param.split("=")[0];
             String value = param.split("=")[1];
             map.put(name, value);
         }
         return map;
     }
 
     public static OAuthConsumer retrieveoAuthToken(String ticket,
             String realmId, String authId) {
         OAuthProvider provider = new DefaultOAuthProvider(
                 props.getProperty("request_token_url"),
                 props.getProperty("access_token_url"), "");
         OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey,
                 consumerSecret);
 
         try {
             provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
 
             // Hydrate browser with cookies and call Workplace Logon
             DefaultHttpClient client = new DefaultHttpClient();
             CookieStore cookies = new BasicCookieStore();
            String cookiePrefix = props.getProperty("cookie.prefix");
             cookies.addCookie(buildCookie(cookiePrefix + TICKET, ticket));
             cookies.addCookie(buildCookie(cookiePrefix + "tkt", ticket));
             cookies.addCookie(buildCookie(cookiePrefix + PARENTID, realmId));
             cookies.addCookie(buildCookie(cookiePrefix + "agentid", authId));
             cookies.addCookie(buildCookie(cookiePrefix + "gauthid", authId));
             cookies.addCookie(buildCookie(cookiePrefix + AUTHID, authId));
             client.setCookieStore(cookies);
 
             HttpContext context = new BasicHttpContext();
             context.setAttribute(ClientContext.COOKIE_STORE, cookies);
             HttpGet httpGet = new HttpGet(props.getProperty("autogrant_url")
                     + consumer.getToken());
             HttpResponse response = client.execute(httpGet, context);
 
             HttpEntity httpEntity = response.getEntity();
             InputStream stream = httpEntity.getContent();
             byte[] bytes = new byte[2048];
             StringBuilder builder = new StringBuilder();
             int len = stream.read(bytes, 0, 2048);
             while (len > 0) {
                 builder.append(new String(bytes));
                 len = stream.read(bytes, 0, 2048);
             }
 
             JSONObject responseObject = new JSONObject(builder.toString());
             String verifier = responseObject.getString("verifier");
 
             if (verifier != null && verifier.length() == 7) {
                 provider.retrieveAccessToken(consumer, verifier);
 
                 return consumer;
             }
         } catch (JSONException exception) {
         } catch (OAuthMessageSignerException oAuthMessageSignerException) {
         } catch (OAuthNotAuthorizedException oAuthNotAuthorizedException) {
         } catch (OAuthExpectationFailedException oAuthExpectationFailedException) {
         } catch (OAuthCommunicationException oAuthCommunicationException) {
         } catch (UnsupportedEncodingException ex) {
         } catch (IOException ex) {
         }
 
         return null;
     }
 
     public static Map<String, String> getCookieAuth(HttpServletRequest request) {
         Map<String, String> result = new HashMap<String, String>();
        String cookiePrefix = props.getProperty("cookie.prefix");
         for (Cookie cookie : request.getCookies()) {
             if (cookie.getName().equals(cookiePrefix + TICKET)) {
                 result.put(TICKET, cookie.getValue());
             }
             if (cookie.getName().equals(cookiePrefix + AUTHID)) {
                 result.put(AUTHID, cookie.getValue());
             }
            if (cookie.getName().equals(cookiePrefix + PARENTID)) {
                 result.put(PARENTID, cookie.getValue());
             }
         }
 
         return result;
     }
     
     public static String getRealmId(HttpServletRequest request) {
       Map<String, String> cookies = getCookieAuth(request);
       
       return cookies.get(PARENTID);
     }
 
     /*
      * Auto grant an oAuth token
      */
     public static OAuthConsumer retrieveoAuthToken(HttpServletRequest request) {
         Map<String, String> cookieAuth = getCookieAuth(request);
         return retrieveoAuthToken(cookieAuth.get(TICKET),
                 cookieAuth.get(PARENTID), cookieAuth.get(AUTHID));
     }
 
     /*
      * Dynamically setup the platform config
      */
      private static void setupQBOAndPlatformConfig() {
        try {
          if (props.getProperty("ipp_platform_service_url") != null && !props.getProperty("ipp_platform_service_url").isEmpty()) {
            Config.setProperty(Config.BASE_URL_PLATFORMSERVICE, props.getProperty("ipp_platform_service_url"));
          }
          if (props.getProperty("qbo_url") != null && !props.getProperty("qbo_url").isEmpty()) {
            Config.setProperty(Config.BASE_URL_QBO, props.getProperty("qbo_url"));           
          }
        } catch (Exception ex) {
        }
      }
           
     /*
      * Example of calling a v3 service API with just a ticket and IPP dev kit
      */
     public static CompanyInfo getCompanyInfoWithTicket(
             HttpServletRequest request) throws FMSException {
         Map<String, String> cookieAuth = getCookieAuth(request);
         IAuthorizer authorizer = new TicketAuthorizer(cookieAuth.get(TICKET),
                 appToken);
         Context context = new Context(authorizer, ServiceType.QBO,
                 cookieAuth.get(PARENTID));
         DataService service = new DataService(context);
         CompanyInfo filter = new CompanyInfo();
         filter.setId(cookieAuth.get(PARENTID));
         CompanyInfo company = service.findById(filter);
 
         return company;
     }
 
     /*
      * Example of calling a v3 service API with oAuth
      */
     public static CompanyInfo getCompanyInfoWithoAuth(OAuthConsumer consumer,
             String realmId) throws FMSException {
         setupQBOAndPlatformConfig();
         IAuthorizer authorizer = new OAuthAuthorizer(consumer.getConsumerKey(),
                 consumer.getConsumerSecret(), consumer.getToken(),
                 consumer.getTokenSecret());
         Context context = new Context(authorizer, ServiceType.QBO, realmId);
         DataService service = new DataService(context);
         CompanyInfo filter = new CompanyInfo();
         filter.setId(realmId);
         CompanyInfo company = service.findById(filter);
 
         return company;
     }
 
     /*
      * Example of calling a v3 query service API with oAuth
      */
     public static int getCustomerCountWithoAuth(OAuthConsumer consumer,
             String realmId) throws FMSException {
         setupQBOAndPlatformConfig();
         IAuthorizer authorizer = new OAuthAuthorizer(consumer.getConsumerKey(),
                 consumer.getConsumerSecret(), consumer.getToken(),
                 consumer.getTokenSecret());
         Context context = new Context(authorizer, ServiceType.QBO, realmId);
         DataService service = new DataService(context);
         QueryResult result = service.executeQuery("select count(*) from customer");
 
         return result.getTotalCount();
     }
 }
