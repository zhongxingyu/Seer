 package hr.brbulic.services.web;
 
 import android.R;
 import android.util.Log;
 import hr.brbulic.services.web.interfaces.IHttpWebResponse;
 import hr.brbulic.services.web.interfaces.IWebRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.logging.StreamHandler;
 
 /**
  * Created by IntelliJ IDEA.
  * User: brbulic
  * Date: 9/30/11
  * Time: 1:25 AM
  * To change this template use File | Settings | File Templates.
  */
 public class WebRequestBasicImpl implements IWebRequest {
 
     private final static String TAG = "WebRequestBasicImpl";
 
     private WebRequestBasicImpl() {
         Log.i(TAG, "Created new instance of IWebRequest implementation");
     }
 
     private transient static IWebRequest instance;
 
     public static IWebRequest getInstance() {
         if (instance == null)
             instance = new WebRequestBasicImpl();
 
         return instance;
     }
 
 
     @Override
     public IHttpWebResponse getRequestStream(final String url) {
 
         InputStream stream;
         HttpStatus status;
 
         try {
            URL finalUrl = new URL(url);
            URLConnection conn = finalUrl.openConnection();
            if (!(conn instanceof HttpURLConnection)) {
                return new WebResponseImpl(null, 500);
            }

            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            int response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                stream = httpConn.getInputStream();
            } else
                stream = null;

 
         } catch (MalformedURLException e) {
             stream = null;
            Log.e(TAG, "Malformed Exception!");
         } catch (IOException e) {
             stream = null;
            Log.e(TAG, e.getLocalizedMessage());
         }
 
         return new WebResponseImpl(stream, HttpStatus.SC_OK);
     }
 
 
     private static class WebResponseImpl implements IHttpWebResponse {
         private final InputStream stream;
         private final int status;
 
         private WebResponseImpl(InputStream stream, int status) {
             this.stream = stream;
             this.status = status;
         }
 
         @Override
         public InputStream getInputStream() {
             return stream;
         }
 
         public int getStatus() {
             return status;
         }
     }
 
 
     @Override
     public IHttpWebResponse getResultWritableStream(String url, byte[] writableData) {
         return null;
     }
 }
