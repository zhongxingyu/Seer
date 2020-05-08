 package com.wapplix.data;
 
 import android.content.Context;
 import android.net.Uri;
 import android.os.Build;
 
 import com.wapplix.data.post.BodyWriter;
 import com.wapplix.data.post.FileWriter;
 import com.wapplix.data.post.MultipartBodyWriter;
 import com.wapplix.data.post.UrlEncodedBodyWriter;
 import com.wapplix.loaders.EndpointLoader;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author Michaël André
  */
 public abstract class Endpoint<TResponse> {
     
     public static final String METHOD_GET = "GET";
     public static final String METHOD_POST = "POST";
     public static final String METHOD_PUT = "PUT";
     public static final String METHOD_DELETE = "DELETE";
 
     private String mUrlBase;
     private String mMethod;
     private Map<String, String> mQueryParameters = new HashMap<String, String>();
     private Map<String, String> mPostValues = new HashMap<String, String>();
     private BodyWriter mBodyWriter;
 
     public Endpoint(String urlBase, String method, Map<String, String> parameters) {
         this.mUrlBase = urlBase;
         this.mMethod = method;
         if (parameters == null) { return; }
         if (METHOD_POST.equalsIgnoreCase(method) || METHOD_PUT.equalsIgnoreCase(method)) {
             for (Map.Entry<String, String> entry : parameters.entrySet()) {
                 setQueryParameter(entry.getKey(), entry.getValue());
             }
         } else {
             for (Map.Entry<String, String> entry : parameters.entrySet()) {
                 setPostValue(entry.getKey(), entry.getValue());
             }
         }
     }
 
     public String getMethod() {
         return mMethod;
     }
 
     public String getUrlBase() {
         return mUrlBase;
     }
 
     public Map<String, String> getQueryParameters() {
         return mQueryParameters;
     }
 
     public Map<String, String> getPostValues() {
         return mPostValues;
     }
 
     public final void setQueryParameter(String key, String value) {
         mQueryParameters.put(key, value);
     }
 
 
     public final void setPostValue(String key, String value) {
         mPostValues.put(key, value);
     }
 
     public void setMultipartPostValue(String key, File file) {
         setMultipartPostValue(key, new FileWriter(file));
     }
 
     public void setMultipartPostValue(String key, BodyWriter writer) {
         if (!(mBodyWriter instanceof MultipartBodyWriter)) {
             mBodyWriter = new MultipartBodyWriter();
         } else {
             mBodyWriter = new MultipartBodyWriter();
         }
         ((MultipartBodyWriter) mBodyWriter).setPart(key, writer);
     }
 
     public HttpURLConnection openConnection() throws Exception {
         String url = mUrlBase;
         if (!mQueryParameters.isEmpty()) {
             Uri.Builder b = Uri.parse(url).buildUpon();
             for (Map.Entry<String, String> entry : mQueryParameters.entrySet()) {
                 b.appendQueryParameter(entry.getKey(), entry.getValue());
             }
             url = b.toString();
         }
         HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
         connection.setRequestMethod(mMethod);
 
         // http://stackoverflow.com/questions/15411213/android-httpsurlconnection-eofexception
         if (Build.VERSION.SDK_INT > 13) {
             connection.setRequestProperty("Connection", "close");
         }
         return connection;
     }
         
     protected void sendRequestBody(HttpURLConnection connection) throws Exception {
         if (!mPostValues.isEmpty()) {
             if (mBodyWriter == null) {
                 mBodyWriter = new UrlEncodedBodyWriter(mPostValues);
             } else if (mBodyWriter instanceof MultipartBodyWriter) {
                 MultipartBodyWriter bodyWriter = (MultipartBodyWriter) mBodyWriter;
                 for (Map.Entry<String, String> entry : mPostValues.entrySet()) {
                     bodyWriter.setPart(entry.getKey(), entry.getValue());
                 }
             }
         }
         if (mBodyWriter != null) {
             String contentType = mBodyWriter.getContentType();
             if (contentType != null) connection.setRequestProperty("Content-Type", contentType);
             int contentLength = mBodyWriter.getContentLength();
             if (contentLength > -1) {
                 connection.setRequestProperty("Content-Length", Integer.toString(contentLength));
                 connection.setFixedLengthStreamingMode(contentLength);
             } else {
                 connection.setChunkedStreamingMode(0);
             }
             connection.setDoOutput(true);
             BufferedOutputStream wr = new BufferedOutputStream(connection.getOutputStream());
             mBodyWriter.writeBody(wr);
             wr.close();
         }
     }
 
 
     public final TResponse readResponse(HttpURLConnection connection) throws Exception {
         try {
             sendRequestBody(connection);
             InputStream is;
             try {
                 is = connection.getInputStream();
             } catch (IOException ex) {
             	int statusCode = connection.getResponseCode();
                 throw getResponseException(statusCode, connection.getErrorStream(), ex);
             }
             return getResponseData(is);
         } finally {
             connection.disconnect();
         }
     }
     
     public final TResponse process() throws Exception {
     	return readResponse(openConnection());
     }
     
     protected abstract TResponse getResponseData(InputStream inputStream) throws Exception;
     
     protected EndpointException getResponseException(int statusCode, InputStream errorStream, IOException cause) throws Exception {
         return new EndpointException(statusCode, errorStream, cause);
     }
     
     public void executeTask(EndpointTask.Listener<TResponse> listener) {
         EndpointTask<TResponse> task = new EndpointTask<TResponse>(this, listener);
         task.execute((Void[]) null);
     }
     
     public EndpointLoader<TResponse> getLoader(Context context) {
     	return new EndpointLoader<TResponse>(context, this);
     }
     
 }
