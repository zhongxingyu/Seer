 package com.artcom.y60.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.SocketException;
 import java.net.URI;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 
 import com.artcom.y60.Logger;
 import com.artcom.y60.thread.ThreadedTask;
 
 public abstract class AsyncHttpRequest extends ThreadedTask {
 
     private static final String   LOG_TAG                  = "AsyncHttpConnection";
 
     private DefaultHttpClient     mHttpClient;
     private final HttpRequestBase mRequest;
 
     private HttpResponse          mResponse                = null;
     private final OutputStream    mResultStream            = new ByteArrayOutputStream();
 
     private HttpResponseHandler   mResponseHandlerCallback = null;
 
     public AsyncHttpRequest(String pUrl) {
         mRequest = createRequest(pUrl);
 
         HttpParams httpParams = new BasicHttpParams();
         setHttpClient(new DefaultHttpClient(httpParams));
     }
 
     public AsyncHttpRequest(String pUrl, DefaultHttpClient pHttpClient) {
         mRequest = createRequest(pUrl);
         setHttpClient(pHttpClient);
     }
 
     // Only used internally to reuse code for both constructors
     private void setHttpClient(DefaultHttpClient pHttpClient) {
         mHttpClient = pHttpClient;
 
         // overwrite user-agent if it's not already customized
         Object userAgent = mHttpClient.getParams().getParameter("http.useragent");
         if (userAgent == null || userAgent.toString().contains("Apache-HttpClient")) {
             mHttpClient.getParams().setParameter("http.useragent", "Y60/1.0 Android");
         }
 
         // remember redirects
         mHttpClient.setRedirectHandler(new DefaultRedirectHandler() {
             @Override
             public URI getLocationURI(HttpResponse response, HttpContext context)
                     throws ProtocolException {
                 URI uri = super.getLocationURI(response, context);
                 mRequest.setURI(uri);
                 return uri;
             }
         });
     }
 
     public String getBodyAsString() {
         return mResultStream.toString();
     }
 
     public void setAcceptedMimeType(String pMimeType) {
         getRequest().addHeader("Accept", pMimeType);
     }
 
     public boolean isConnecting() {
         return getProgress() == 1;
     }
 
     public boolean wasSuccessful() {
         int status = getStatusCode();
         return status >= 200 && status < 300;
     }
 
     public boolean hadClientError() {
         int status = getStatusCode();
         return status >= 400 && status < 500;
     }
 
     public boolean hadServerError() {
         int status = getStatusCode();
         return status >= 500 && status < 600;
     }
 
     @Override
     public void doInBackground() {
 
         setProgress(1);
 
         try {
             mResponse = mHttpClient.execute(mRequest);
         } catch (ClientProtocolException e) {
             onClientError(e);
             return;
         } catch (SocketException e) {
             onClientError(e);
             return;
         } catch (IOException e) {
             onIoError(e);
             return;
         }
         setProgress(2);
 
         if (mResponse == null) {
             onClientError(new NullPointerException("expected http response object is null"));
             return;
         }
 
        Logger.v(LOG_TAG, "before: onHttpHeaderAvailable with: ", mResponse.getAllHeaders());
        onHttpHeaderAvailable(mResponse.getAllHeaders());
        Logger.v(LOG_TAG, "after: onHttpHeaderAvailable with: ", mResponse.getAllHeaders());
 
         try {
             InputStream is = mResponse.getEntity().getContent();
             long downloaded = 0;
             long size = mResponse.getEntity().getContentLength();
             byte[] buffer = new byte[0xFFFF];
             int len;
             while ((len = is.read(buffer)) != -1) {
                 setProgress((int) (downloaded / size));
                 mResultStream.write(buffer, 0, len);
                 downloaded += len;
             }
         } catch (IOException e) {
             onIoError(e);
             return;
         }
 
     }
 
     public int getStatusCode() {
         if (mResponse != null) {
             return mResponse.getStatusLine().getStatusCode();
         } else {
             return -1;
         }
     }
 
     public void registerResponseHandler(HttpResponseHandler responseHandler) {
         mResponseHandlerCallback = responseHandler;
     }
 
     /**
      * @return uri of the request (gets updated when redirected)
      */
     public String getUri() {
         return mRequest.getURI().toString();
     }
 
     public String getHeader(String pHeaderName) {
         return mResponse.getFirstHeader(pHeaderName).getValue();
     }
 
     abstract protected HttpRequestBase createRequest(String pUrl);
 
     protected HttpRequestBase getRequest() {
         return mRequest;
     }
 
     @Override
     protected void onPostExecute() {
 
         if (mResponse == null) {
             onClientError(new NullPointerException("response of request " + mRequest.getURI()
                     + " was null"));
             return;
         }
 
         int status = getStatusCode();
         if (hadClientError()) {
             onClientError(status);
         } else if (hadServerError()) {
             onServerError(status);
         } else if (wasSuccessful()) {
             onSuccess(status);
         } else {
             onClientError(new Exception("do not know what to do with status code "
                     + getStatusCode()));
         }
 
         super.onPostExecute();
     }
 
     protected void onIoError(IOException e) {
         Logger.e(LOG_TAG, e);
     }
 
     protected void onClientError(Exception e) {
         Logger.e(LOG_TAG, e);
     }
 
     protected void onHttpHeaderAvailable(Header[] headers) {
         if (mResponseHandlerCallback != null) {
            Logger.v(LOG_TAG, "calling callbeack: onHeaderAvailable, with: ", headers);
             mResponseHandlerCallback.onHeaderAvailable(headers);
         }
     }
 
     protected void onSuccess(int pStatusCode) {
         if (mResponseHandlerCallback != null) {
             mResponseHandlerCallback.onSuccess(pStatusCode, mResultStream);
         }
     }
 
     protected void onClientError(int pStatusCode) {
         Logger.e(LOG_TAG, "Error response code was ", pStatusCode);
         if (mResponseHandlerCallback != null) {
             mResponseHandlerCallback.onError(pStatusCode, mResultStream);
         }
     }
 
     protected void onServerError(int pStatusCode) {
         Logger.e(LOG_TAG, "Error response code was ", pStatusCode, " body: ", mResultStream
                 .toString());
         if (mResponseHandlerCallback != null) {
             mResponseHandlerCallback.onError(pStatusCode, mResultStream);
         }
     }
 }
