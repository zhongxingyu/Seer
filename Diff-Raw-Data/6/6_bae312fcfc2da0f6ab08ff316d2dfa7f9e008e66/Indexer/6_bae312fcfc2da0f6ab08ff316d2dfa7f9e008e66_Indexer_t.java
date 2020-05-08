 package com.mridang.moko.generics;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.loopj.android.http.PersistentCookieStore;
 import com.mridang.moko.structures.Torrent;
 
 /*
  * This is the base class for all the website plugins
  */
 public class Indexer {
 
 	/*
 	 * Custom exception for unable-to-login issues
 	 */
 	@SuppressWarnings("serial")
 	public class LoginException extends Exception {
 
 		/*
 		 * @see java.lang.Exception#Exception(String detailMessage)
 		 */
 		public LoginException(String strMessage) {
 
 			super(strMessage);
 
 		}
 
 	}
 
	/*
	 * The context of the calling class
	 */
    protected Context ctxContext = null;
 
     /*
      * Initializes this task
      *
      * @param  objContext  the instance of the calling Search class
      */
 	public Indexer(Context ctxContext) {
 
 		this.ctxContext = ctxContext;
 
 	}
 
 	/*
 	 * This method makes a HTTP POST request and is generally used for logging
 	 * in.
 	 *
 	 * @param  strUrl    the URL of the page to which to POST
 	 * @param  lstParams a list of name-value pairs to post
 	 * @return String    the results of the POST request
 	 */
 	public String doPost(String strUrl, List<NameValuePair> lstParams) throws Exception {
 
         Integer intTry = 0;
 
         while (intTry < 3) {
 
         	intTry += 1;
 
             try {
 
             	String strResponse = null;
 
             	HttpParams htpParameters = new BasicHttpParams();
             	// Set the timeout in milliseconds until a connection is established.
             	HttpConnectionParams.setConnectionTimeout(htpParameters, 10000);
             	// Set the default socket timeout (SO_TIMEOUT) 
             	HttpConnectionParams.setSoTimeout(htpParameters, 10000);                 	
             	
 				HttpPost htpPost = new HttpPost(strUrl);
 				htpPost.setEntity(new UrlEncodedFormEntity(lstParams));
 				htpPost.addHeader("Accept-Encoding", "gzip");
 				htpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20100101 Firefox/15.0.1");
 
 				DefaultHttpClient dhcClient = new DefaultHttpClient(htpParameters);
 				dhcClient.addResponseInterceptor(new Decompressor(), 0);
 
 				PersistentCookieStore pscStore = new PersistentCookieStore(this.ctxContext);
 				dhcClient.setCookieStore(pscStore);
 
 				HttpResponse resResponse = dhcClient.execute(htpPost);
 				strResponse = EntityUtils.toString(resResponse.getEntity());
 
 				return strResponse;
 
 		    } catch (Exception e) {
 
 		    	e.printStackTrace();
 
 		        if (intTry < 3) {
 		        	Log.v("generics.Indexer", String.format("Attempt #%d", intTry));
 		         } else {
 		        	 throw e;
 		         }
 
 		    }
 
 		}
 
 		return null;
 
 	}
 
     /*
      * This class is a custom HTTP response intercepter that will decompress
      * the GZIPped response
      */
     class Decompressor implements HttpResponseInterceptor {
 
         /*
          * @see org.apache.http.HttpResponseInterceptor#process(org.apache.http.
          * HttpResponse, org.apache.http.protocol.HttpContext)
          */
         public void process(HttpResponse hreResponse, HttpContext hctContext)   throws HttpException, IOException {
 
             HttpEntity entity = hreResponse.getEntity();
 
             if (entity != null) {
 
                 Header ceheader = entity.getContentEncoding();
 
                 if (ceheader != null) {
 
                     HeaderElement[] codecs = ceheader.getElements();
 
                     for (int i = 0; i < codecs.length; i++) {
 
                         if (codecs[i].getName().equalsIgnoreCase("gzip")) {
 
                             hreResponse.setEntity(new HttpEntityWrapper(entity) {
 
                                 @Override
                                 public InputStream getContent() throws IOException, IllegalStateException {
 
                                     return new GZIPInputStream(wrappedEntity.getContent());
 
                                 }
 
                                 @Override
                                 public long getContentLength() {
 
                                     return -1;
 
                                 }
 
                             });
 
                             return;
 
                         }
 
                     }
 
                 }
 
             }
 
         }
 
     }
 
 	/*
 	 * This method makes a HTTP GET request and is generally used for logging
 	 * in.
 	 *
 	 * @param  strUrl    the URL of the page to which to GET
 	 * @param  lstParams a list of name-value pairs to post
 	 * @return String    the results of the GET request
 	 */
 	public String doGet(String strUrl, List<NameValuePair> lstParams) throws Exception {
 
         Integer intTry = 0;
 
         while (intTry < 3) {
 
         	intTry += 1;
 
             try {
 
             	String strResponse = null;
 
             	HttpParams htpParameters = new BasicHttpParams();
             	// Set the timeout in milliseconds until a connection is established.
             	HttpConnectionParams.setConnectionTimeout(htpParameters, 10000);
             	// Set the default socket timeout (SO_TIMEOUT) 
             	HttpConnectionParams.setSoTimeout(htpParameters, 10000);            	
             	
     			HttpGet htpGet = new HttpGet(strUrl);
     			htpGet.addHeader("Accept-Encoding", "gzip");
     			htpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:15.0) Gecko/20100101 Firefox/15.0.1");
 
     			DefaultHttpClient dhcClient = new DefaultHttpClient(htpParameters);
     			dhcClient.addResponseInterceptor(new Decompressor(), 0);
 
     			PersistentCookieStore pscStore = new PersistentCookieStore(this.ctxContext);
     			dhcClient.setCookieStore(pscStore);
 
     			HttpResponse resResponse = dhcClient.execute(htpGet);
     			strResponse = EntityUtils.toString(resResponse.getEntity());
 
         		return strResponse;
 
             } catch (Exception e) {
 
             	e.printStackTrace();
 
                 if (intTry < 3) {
                 	Log.v("generics.Indexer", String.format("Attempt #%d", intTry));
                  } else {
                 	 throw e;
                  }
 
             }
 
         }
 
         return null;
 
 	}
 
 	/*
 	 * This method is the method that searches the site.
 	 *
 	 * @param strQuery the search string that should be used
 	 *
 	 * @return a list of results.
 	 */
 	public ArrayList<Torrent> doSearch(String strQuery) {
 
 		return null;
 
 	}
 
 }
