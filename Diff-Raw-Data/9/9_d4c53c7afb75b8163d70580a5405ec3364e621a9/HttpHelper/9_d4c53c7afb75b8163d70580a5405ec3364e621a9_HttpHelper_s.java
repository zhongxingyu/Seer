 
 package com.lukekorth.android_http;
 
 import android.content.Context;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.util.Log;
 
 import com.google.gson.Gson;
 import com.integralblue.httpresponsecache.HttpResponseCache;
 
 import org.apache.http.NameValuePair;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.List;
 
 public class HttpHelper {
 
     private static final String TAG = "android-http";
     private boolean DEBUG_HTTP;
 
     private Context mContext;
     private static Gson gson = null;
     private int connectTimeout = 10 * 1000; // 10 seconds in milliseconds
     private int readTimeout = 60 * 1000; // 60 seconds in milliseconds
 
     public HttpHelper(Context context) {
         Init(context, (long) 10 * 1024 * 1024); // 10 MiB
     }
 
     public HttpHelper(Context context, long size) {
         Init(context, size);
     }
 
     private void Init(Context context, long size) {
         mContext = context;
 
         DEBUG_HTTP = IsDebug();
 
         try {
             HttpResponseCache.install(new File(context.getCacheDir(), "http"), size);
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "IOException occured while getting cache dir");
         }
     }
 
     public static Gson getGson() {
         if (gson == null)
             gson = new Gson();
 
         return gson;
     }
 
     public void setConnectTimeout(int seconds) {
         connectTimeout = seconds * 1000;
     }
 
     public void setReadTimeout(int seconds) {
         readTimeout = seconds * 1000;
     }
 
     public String get(String url) {
         HttpURLConnection urlConnection = null;
         String response = null;
         try {
             urlConnection = (HttpURLConnection) new URL(url).openConnection();
 
             urlConnection.setConnectTimeout(connectTimeout);
             urlConnection.setReadTimeout(readTimeout);
 
             InputStream in = new BufferedInputStream(urlConnection.getInputStream());
             response = readStream(in);
 
             if (DEBUG_HTTP) {
                 Log.d(TAG, "response code: " + urlConnection.getResponseCode());
                 Log.d(TAG, "response payload: " + response);
             }
         } catch (MalformedURLException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "MalformedURLException occured while parsing url");
         } catch (IOException e) {
             if (DEBUG_HTTP)
                Log.w(TAG,
                        "IOException occured while trying to open connection or getting input stream. "
                                + e.getMessage());
         } finally {
             urlConnection.disconnect();
         }
 
         return response;
     }
 
     public String get(String url, List<NameValuePair> nameValuePairs) {
         url += "?";
 
         for (NameValuePair pair : nameValuePairs) {
             try {
                 url += pair.getName() + "=" + URLEncoder.encode(pair.getValue(), "UTF-8") + "&";
             } catch (UnsupportedEncodingException e) {
                 if (DEBUG_HTTP)
                     Log.w(TAG, "UnsupportedEncodingException while url encoding query string");
             }
         }
 
         url = url.substring(0, url.length() - 1);
 
         if (DEBUG_HTTP) {
             Log.d(TAG, "url: " + url);
             Log.d(TAG, "query string: " + nameValuePairs.toString());
         }
 
         return get(url);
     }
 
     @SuppressWarnings({
             "unchecked", "rawtypes"
     })
     public <T> T get(String url, Class type) {
         if (gson == null) {
             gson = new Gson();
         }
 
         return (T) gson.fromJson(get(url), type);
     }
 
     @SuppressWarnings({
             "unchecked", "rawtypes"
     })
     public <T> T get(String url, List<NameValuePair> nameValuePairs, Class type) {
         if (gson == null) {
             gson = new Gson();
         }
 
         return (T) gson.fromJson(get(url, nameValuePairs), type);
     }
 
     public String post() {
 
     }
 
     private String readStream(InputStream in) {
         InputStreamReader is = new InputStreamReader(in);
         BufferedReader br = new BufferedReader(is);
         StringBuilder sb = new StringBuilder();
 
         try {
             String read = br.readLine();
 
             while (read != null) {
                 sb.append(read);
                 read = br.readLine();
             }
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "IOException occured while reading response from server");
         }
 
         try {
             in.close();
             is.close();
             br.close();
         } catch (IOException e) {
             if (DEBUG_HTTP)
                 Log.w(TAG, "IOException occured while closing streams");
         }
 
         return sb.toString();
     }
 
     // http://izvornikod.com/Blog/tabid/82/EntryId/13/How-to-check-if-your-android-application-is-running-in-debug-or-release-mode.aspx
     private boolean IsDebug() {
         boolean debuggable = false;
 
         PackageManager pm = mContext.getPackageManager();
         try {
             ApplicationInfo appinfo = pm.getApplicationInfo(mContext.getPackageName(), 0);
             debuggable = (0 != (appinfo.flags &= ApplicationInfo.FLAG_DEBUGGABLE));
         } catch (NameNotFoundException e) {
             /* debuggable variable will remain false */
         }
 
         return debuggable;
     }
 
 }
