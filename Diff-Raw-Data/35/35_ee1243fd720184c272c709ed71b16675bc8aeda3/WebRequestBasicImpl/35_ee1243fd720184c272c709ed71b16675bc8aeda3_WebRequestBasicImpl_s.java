 package hr.brbulic.services.web;
 
 import android.R;
 import android.util.Log;
 import hr.brbulic.services.web.interfaces.IHttpWebResponse;
 import hr.brbulic.services.web.interfaces.IWebRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 
 import java.io.IOException;
 import java.io.InputStream;
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
            URL updateURL = new URL(url);
            URLConnection conn = updateURL.openConnection();
            stream = conn.getInputStream();
 
         } catch (MalformedURLException e) {
             stream = null;
            Log.e(TAG, "");
         } catch (IOException e) {
             stream = null;
            Log.e(TAG, "Cannot get response stream!");
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
