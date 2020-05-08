 package mobi.monaca.framework;
 
 import java.net.URI;
 import java.security.KeyStore;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import mobi.monaca.framework.nativeui.menu.MenuRepresentation;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentationBuilder;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.framework.util.MySSLSocketFactory;
 
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.params.ConnPerRoute;
 import org.apache.http.conn.routing.HttpRoute;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.util.Log;
 
 /** This class manage the application's global state and variable. */
 public class MonacaApplication extends Application {
 
     private static final String TAG = MonacaApplication.class.getSimpleName();
 	protected static BasicHttpParams connectionParams = null;
     protected static ClientConnectionManager connectionManager = null;
     protected static List<MonacaPageActivity> pages = null;
     protected static Map<String, MenuRepresentation> menuMap = null;
     protected static MonacaApplication self = null;
     protected static List<Cookie> cookies = null;
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
     }
 
     @Override
     public void onCreate() {
     	MyLog.i(TAG, "onCreate()");
         super.onCreate();
         self = this;
         
         createMenuMap();
     }
 
 	protected void createMenuMap() {
 		menuMap = new MenuRepresentationBuilder(getApplicationContext()).buildFromAssets(this, "www/app.menu");
 	}
     
     
     public static boolean allowAccess(String url) {
         
         if (url.startsWith("file://")) {
             Context context = self.getApplicationContext();
             
             try {
                 url = new URI(url).normalize().toString();
             } catch (Exception e) {
             	MyLog.e(TAG, e.getMessage());
                 return false;
             }
             
             if (url.startsWith("file:///android_asset/")) {
                 return true;
             }
             
             if (url.startsWith("file://" + context.getApplicationInfo().dataDir)) {
                 return !url.startsWith("file://" + context.getApplicationInfo().dataDir + "/shared_prefs/");
             }
             
             //allow access to SD card (some app need access to photos in SD card)
             if (url.startsWith("file:///mnt/")){
             	return true;
            }
            
             return false;
         }
         
         return true;
     }
 
     /** Add a MonacaPageActivity instance to the page list. */
     public static void addPage(MonacaPageActivity page) {
         if (pages == null) {
             pages = new ArrayList<MonacaPageActivity>();
         }
 
         pages.add(page);
     }
 
     /** Remove a MonacaPageActivity instance from the page list. */
     public static void removePage(MonacaPageActivity page) {
         if (pages != null) {
             pages.remove(page);
         }
     }
 
     /** Get either MenuRepresentation or null from menu name. */
     public static MenuRepresentation findMenuRepresentation(String name) {
     	MyLog.v(TAG, "findMenuRepresentation. name:" + name + ", menuMap:" + menuMap);
         if (menuMap != null) {
             return menuMap.containsKey(name) ? menuMap.get(name) : null;
         }
         return null;
     }
 
     /** Get all MonacaPageActivity instances in this application. */
     public static List<MonacaPageActivity> getPages() {
         return pages != null ? pages : new ArrayList<MonacaPageActivity>();
     }
 
     @Override
     public void onTerminate() {
     	MyLog.i(TAG, "onTerminate()");
         if (connectionManager != null) {
             connectionManager.shutdown();
         }
         
         connectionManager = null;
         connectionParams = null;
         pages = null;
         menuMap = null;
         
         self = null;
 
         super.onTerminate();
     }
     
 
     protected static BasicHttpParams getHttpParams() {
         if (connectionParams != null) {
             return connectionParams;
         }
         connectionParams = new BasicHttpParams();
 
         ConnManagerParams.setMaxTotalConnections(connectionParams, 1000);
         ConnManagerParams.setTimeout(connectionParams, 8000);
         ConnManagerParams.setMaxConnectionsPerRoute(connectionParams,
                 new ConnPerRoute() {
                     @Override
                     public int getMaxForRoute(HttpRoute route) {
                         return 2000;
                     }
                 });
 
         return connectionParams;
     }
 
     protected static ClientConnectionManager getClientConnectionManager() {
         if (connectionManager != null) {
             return connectionManager;
         }
 
         BasicHttpParams params = getHttpParams();
 
         SchemeRegistry registry = new SchemeRegistry();
         registry.register(new Scheme("http", PlainSocketFactory
                 .getSocketFactory(), 80));
 
         try {
             KeyStore trustStore = KeyStore.getInstance(KeyStore
                     .getDefaultType());
             trustStore.load(null, null);
             SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
             registry.register(new Scheme("https", sf, 443));
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         ClientConnectionManager connManager = new ThreadSafeClientConnManager(
                 params, registry);
         return connManager;
     }
 
     public static DefaultHttpClient buildHttpClient() {
         HttpParams params = getHttpParams().copy();
 
         params.setParameter(ClientPNames.COOKIE_POLICY,
                 CookiePolicy.BROWSER_COMPATIBILITY);
 
         params.setParameter("http.connection.timeout", 10000);
         params.setParameter("http.socket.timeout", 10000);
 
         DefaultHttpClient httpClient = new DefaultHttpClient(
                 getClientConnectionManager(), params);
 
         return httpClient;
     }
 
     public static void closeStaleConnections() {
         ClientConnectionManager connManager = getClientConnectionManager();
         connManager.closeExpiredConnections();
         connManager.closeIdleConnections(5, TimeUnit.SECONDS);
         MyLog.d(MonacaApplication.class.getSimpleName(),
                 "closeStaleConnections() is called.");
     }
     
     public static List<Cookie> getCookies() {
 		return cookies;
 	}
     
     public static void setCookies(List<Cookie> cookies) {
 		MonacaApplication.cookies = cookies;
 	}
 
 }
