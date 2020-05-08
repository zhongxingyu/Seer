 /*******************************************************************************
  * Copyright (c) 2013 Zheng Sun.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Zheng Sun - initial API and implementation
  ******************************************************************************/
 
 package tv.huohua.peterson.network;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 final public class NetworkUtils {
     static {
         HttpURLConnection.setFollowRedirects(true);
     }
 
     public static Map<String, List<String>> getQueryParams(final String url) {
         try {
             final Map<String, List<String>> params = new HashMap<String, List<String>>();
             final String[] urlParts = url.split("\\?");
             if (urlParts.length > 1) {
                 final String query = urlParts[1];
                 for (final String param : query.split("&")) {
                     final String[] pair = param.split("=");
                     final String key = URLDecoder.decode(pair[0], "UTF-8");
                     String value = "";
                     if (pair.length > 1) {
                         value = URLDecoder.decode(pair[1], "UTF-8");
                     }
 
                     List<String> values = params.get(key);
                     if (values == null) {
                         values = new ArrayList<String>();
                         params.put(key, values);
                     }
                     values.add(value);
                 }
             }
             return params;
         } catch (final UnsupportedEncodingException exception) {
             return new HashMap<String, List<String>>();
         }
     }
 
     public static HttpResponse httpQuery(final HttpRequest request) throws ClientProtocolException, IOException {
         final DefaultHttpClient client = new DefaultHttpClient();
         final HttpParams params = client.getParams();
         HttpConnectionParams.setConnectionTimeout(params, 40 * 1000);
         HttpConnectionParams.setSoTimeout(params, 30 * 1000);
 
         final HttpRequestBase requestBase;
         if (request.getHttpMethod().equals(HttpRequest.HTTP_METHOD_GET)) {
             final StringBuilder builder = new StringBuilder(request.getUrl());
             if (request.getParams() != null) {
                 if (!request.getUrl().contains("?")) {
                     builder.append("?");
                 } else {
                     builder.append("&");
                 }
                 builder.append(request.getParamsAsString());
             }
             final HttpGet get = new HttpGet(builder.toString());
             requestBase = get;
         } else if (request.getHttpMethod().equals(HttpRequest.HTTP_METHOD_POST)) {
             final HttpPost post = new HttpPost(request.getUrl());
             if (request.getEntity() != null) {
                 post.setEntity(request.getEntity());
             } else {
                post.setEntity(new UrlEncodedFormEntity(request.getParamsAsList(), "UTF-8"));
             }
             requestBase = post;
         } else if (request.getHttpMethod().equals(HttpRequest.HTTP_METHOD_PUT)) {
             final HttpPut put = new HttpPut(request.getUrl());
            put.setEntity(new UrlEncodedFormEntity(request.getParamsAsList(), "UTF-8"));
             if (request.getEntity() != null) {
                 put.setEntity(request.getEntity());
             } else {
                put.setEntity(new UrlEncodedFormEntity(request.getParamsAsList(), "UTF-8"));
             }
             requestBase = put;
         } else if (request.getHttpMethod().equals(HttpRequest.HTTP_METHOD_DELETE)) {
             final StringBuilder builder = new StringBuilder(request.getUrl());
             if (request.getParams() != null) {
                 if (!request.getUrl().contains("?")) {
                     builder.append("?");
                 } else {
                     builder.append("&");
                 }
                 builder.append(request.getParamsAsString());
             }
             final HttpDelete delete = new HttpDelete(builder.toString());
             requestBase = delete;
         } else {
             return null;
         }
 
         if (request.getHeaders() != null) {
             final Iterator<Map.Entry<String, String>> iterator = request.getHeaders().entrySet().iterator();
             while (iterator.hasNext()) {
                 final Map.Entry<String, String> kv = iterator.next();
                 requestBase.setHeader(kv.getKey(), kv.getValue());
             }
         }
         return client.execute(requestBase);
     }
 
     public static boolean isNetworkAvailable(Context context) {
         ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo info = cm.getActiveNetworkInfo();
         return (info != null && info.isConnected());
     }
 
     private NetworkUtils() {
     }
 }
