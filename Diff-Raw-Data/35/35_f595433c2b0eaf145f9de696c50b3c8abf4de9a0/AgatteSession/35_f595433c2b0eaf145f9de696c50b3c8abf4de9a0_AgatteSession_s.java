 package com.agatteclient;
 
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.net.http.AndroidHttpClient;
 import android.preference.PreferenceActivity;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpCookie;
 import java.net.URI;
 
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 /**
  * Created by Rémi Pannequin on 24/09/13.
  */
 public class AgatteSession {
 
     private String session_id;
     private String server;
     private long session_expire;
     //private URI login_url;
     private HttpGet login_rq;
     private HttpGet logout_rq;
     private HttpPost auth_rq;
     private HttpGet query_day_rq;
     private HttpPost exec_rq;
     private List<NameValuePair> credentials;
     private HttpClient client;
     private HttpContext context;
 
 
     private static final String LOGIN_DIR = "/app/login.form";
     private static final String LOGOUT_DIR = "/app/logout.form";
     private static final String AUTH_DIR = "/j_acegi_security_check";
     private static final String EXEC_DIR = "/top/top.form";
     private static final String QUERY_DIR = "/top/top.form?numMen=2";
     private static final String USER = "j_username";
     private static final String PASSWORD = "j_password";
     private static final String AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
 
 
     /**
      * Create a new Agatte session.
      * @throws URISyntaxException If server YRL is not correct
      * @throws UnsupportedEncodingException if username and password include chars unsupported in this encoding
      */
     public AgatteSession() throws URISyntaxException, UnsupportedEncodingException {
         //super ("AgatteConnectionService");
         credentials = new ArrayList<NameValuePair>(2);
     }
 
     public AgatteSession(String server, String user, String passwd) throws URISyntaxException, UnsupportedEncodingException {
         //super ("AgatteConnectionService");
         credentials = new ArrayList<NameValuePair>(2);
         this.setServer(server);
         this.setUser(user);
         this.setPassword(passwd);
 
     }
 
 
     /*@Override
     protected void onHandleIntent(Intent arg0) {
         login();
     }
     */
 
     void logout() {
 
         try {
             client.execute(logout_rq, context);
             //this.session_expire = new Date(cookie.getMaxAge());
             //compute expiration date according to System.date(); ??
             this.session_id = null;
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Attempt to log in Agatte :ie open an agatte session
      *
      * @return false if login failed, true if no errors were detected
      */
     boolean login() {
 
         context = new BasicHttpContext();
         CookieStore cookieStore = new BasicCookieStore();
         context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
         client = AndroidHttpClient.newInstance(AGENT);
         try {
             HttpResponse response1 = client.execute(login_rq, context);
             //test whether response is OK
             if (response1.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                 return false;
             }
             //update sessionID and expiration date
             boolean found_id = false;
             for (Cookie cookie : cookieStore.getCookies())
                 if (cookie.getName().equals("JSESSIONID")) {
                     //this.session_expire = new Date(cookie.getMaxAge());
                     //compute expiration date according to System.date(); ??
                     this.session_expire = System.currentTimeMillis();
                     this.session_id = cookie.getValue();
                     found_id = true;
                 }
             if (!found_id) { return false; }
 
             HttpResponse response2 = client.execute(auth_rq, context);
             //if (response2.getStatusLine().getStatusCode() != HttpStatus.SC_TEMPORARY_REDIRECT) {
             //    return false;
             //}
             for (Header h: response2.getHeaders("Location")) {
                 //value should be URL("https", server, "/");

             }
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     void query_day() {
         if (!isConnected()) {
             return;
         }
         try {
             client = AndroidHttpClient.newInstance(AGENT);
             client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
             //http.protocol.handle-redirects
 
            HttpResponse response1 = client.execute(query_day_rq, context);
            if (response1.getStatusLine().getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                //302 response :authentication did not succeed...
 
 
            }
 
            if (response1.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
 
             }
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse(response1.getEntity().getContent());
             //TODO :get element from doc
             Element nl = doc.getElementById("rappelTop");
 
         } catch (ParserConfigurationException e) {
              e.printStackTrace();
         } catch (SAXException e) {
              e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     void exec() {
         if (!isConnected()) {
             return;
         }
         try {
             HttpResponse response1 = client.execute(exec_rq, context);
             //verify that response is a redirection to topOk.htm
 
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public String getServer() {
         return server;
     }
 
     public void setServer(String server) throws URISyntaxException {
         this.server = server;
         this.login_rq = new HttpGet(new URI("https", this.getServer(), LOGIN_DIR, null));
         this.logout_rq= new HttpGet(new URI("https", this.getServer(), LOGOUT_DIR, null));
         this.auth_rq= new HttpPost(new URI("https", this.getServer(), AUTH_DIR, null));
         this.exec_rq= new HttpPost(new URI("https", this.getServer(), EXEC_DIR, null));
         this.query_day_rq= new HttpGet(new URI("https", this.getServer(), QUERY_DIR, null));
     }
 
     public String getUser() {
         return credentials.get(0).getValue();
     }
 
     public void setUser(String user) throws UnsupportedEncodingException {
         credentials.add(0, new BasicNameValuePair(USER, user));
         auth_rq.setEntity(new UrlEncodedFormEntity(credentials));
     }
 
     public String getPassword() {
         return credentials.get(1).getValue();
     }
 
     public void setPassword(String password)  throws UnsupportedEncodingException {
         credentials.add(1, new BasicNameValuePair(PASSWORD, password));
         auth_rq.setEntity(new UrlEncodedFormEntity(credentials));
     }
 
     /**
      * Return the connection status of the agatte session.
      *
      * @return
      */
     public boolean isConnected() {
         return ((this.session_id != null) && (this.session_expire < System.currentTimeMillis() + 120000));
     }
 }
