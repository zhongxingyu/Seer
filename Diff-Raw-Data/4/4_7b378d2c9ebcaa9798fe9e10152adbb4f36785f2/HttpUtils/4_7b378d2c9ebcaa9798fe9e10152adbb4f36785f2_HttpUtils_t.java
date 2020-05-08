 /*
  * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.pixmob.fm2.util;
 
 import static org.pixmob.fm2.Constants.APPLICATION_NAME_USER_AGENT;
 import static org.pixmob.fm2.Constants.DEBUG;
 import static org.pixmob.fm2.Constants.TAG;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.net.HttpURLConnection;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.UnknownHostException;
 import java.security.GeneralSecurityException;
 import java.security.KeyStore;
 import java.security.SecureRandom;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.GZIPInputStream;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.KeyManagerFactory;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManagerFactory;
 
 import org.apache.http.conn.ssl.StrictHostnameVerifier;
 import org.pixmob.fm2.R;
 
 import android.content.Context;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Build;
 import android.util.Log;
 
 /**
  * Http utilities.
  * @author Pixmob
  */
 public final class HttpUtils {
     private static final SecureRandom SECURE_RANDOM = new SecureRandom();
     private static String applicationVersion;
     
     static {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
             // Disable connection pooling before Froyo:
             // http://stackoverflow.com/a/4261005/422906
             System.setProperty("http.keepAlive", "false");
         }
     }
     
     private HttpUtils() {
     }
     
     /**
      * Get Http cookies from a response.
      */
     public static void readCookies(HttpURLConnection conn, Set<String> cookies) {
         final List<String> newCookies = conn.getHeaderFields()
                 .get("Set-Cookie");
         if (newCookies != null) {
             for (final String newCookie : newCookies) {
                 cookies.add(newCookie.split(";", 2)[0]);
             }
         }
     }
     
     /**
      * Create a new Http connection for an URI.
      */
     public static HttpURLConnection newRequest(Context context, String uri,
             Set<String> cookies) throws IOException {
         if (DEBUG) {
             Log.d(TAG, "Setup connection to " + uri);
         }
         
         final HttpURLConnection conn = (HttpURLConnection) new URL(uri)
                 .openConnection();
         conn.setUseCaches(false);
         conn.setInstanceFollowRedirects(false);
         conn.setConnectTimeout(30000);
         conn.setReadTimeout(30000);
         conn.setRequestProperty("Accept-Encoding", "gzip");
         conn.setRequestProperty("User-Agent", getUserAgent(context));
         conn.setRequestProperty("Cache-Control", "max-age=0");
         conn.setDoInput(true);
         
        // Close the connection when the request is done, or the application may
        // freeze due to a bug in some Android versions.
        conn.setRequestProperty("Connection", "close");
        
         if (conn instanceof HttpsURLConnection) {
             setupSecureConnection(context, (HttpsURLConnection) conn);
         }
         
         if (cookies != null && !cookies.isEmpty()) {
             final StringBuilder buf = new StringBuilder(256);
             for (final String cookie : cookies) {
                 if (buf.length() != 0) {
                     buf.append("; ");
                 }
                 buf.append(cookie);
             }
             conn.addRequestProperty("Cookie", buf.toString());
         }
         
         return conn;
     }
     
     /**
      * Prepare a <code>POST</code> Http request.
      */
     public static HttpURLConnection newPostRequest(Context context, String uri,
             Map<String, String> params, String charset) throws IOException {
         final StringBuilder query = new StringBuilder();
         if (params != null) {
             for (final Map.Entry<String, String> e : params.entrySet()) {
                 if (query.length() != 0) {
                     query.append("&");
                 }
                 query.append(e.getKey()).append("=")
                         .append(URLEncoder.encode(e.getValue()));
             }
         }
         
         final HttpURLConnection conn = newRequest(context, uri, null);
         conn.setDoOutput(true);
         conn.setRequestProperty("Accept-Charset", charset);
         conn.setRequestProperty("Content-Type",
             "application/x-www-form-urlencoded;charset=" + charset);
         
         final OutputStream queryOutput = conn.getOutputStream();
         try {
             queryOutput.write(query.toString().getBytes(charset));
         } finally {
             IOUtils.close(queryOutput);
         }
         
         return conn;
     }
     
     /**
      * Open the {@link InputStream} of an Http response. This method supports
      * GZIP responses.
      */
     public static InputStream getInputStream(HttpURLConnection conn)
             throws IOException {
         final List<String> contentEncodingValues = conn.getHeaderFields().get(
             "Content-Encoding");
         for (final String contentEncoding : contentEncodingValues) {
             if ("gzip".contains(contentEncoding)) {
                 return new GZIPInputStream(conn.getInputStream());
             }
         }
         return conn.getInputStream();
     }
     
     /**
      * Get Http User Agent for this application.
      */
     public static final String getUserAgent(Context context) {
         if (applicationVersion == null) {
             try {
                 applicationVersion = context.getPackageManager()
                         .getPackageInfo(context.getPackageName(), 0).versionName;
             } catch (NameNotFoundException e) {
                 applicationVersion = "0.0.0";
             }
         }
         return APPLICATION_NAME_USER_AGENT + "/" + applicationVersion + " ("
                 + Build.MANUFACTURER + " " + Build.MODEL + " with Android "
                 + Build.VERSION.RELEASE + "/" + Build.VERSION.SDK_INT + ")";
     }
     
     private static KeyStore loadCertificates(Context context)
             throws IOException {
         try {
             final KeyStore localTrustStore = KeyStore.getInstance("BKS");
             final InputStream in = context.getResources().openRawResource(
                 R.raw.mykeystore);
             try {
                 localTrustStore.load(in, "mysecret".toCharArray());
             } finally {
                 in.close();
             }
             
             return localTrustStore;
         } catch (Exception e) {
             throw new IOException("Failed to load SSL certificates", e);
         }
     }
     
     /**
      * Setup SSL connection.
      */
     private static void setupSecureConnection(Context context,
             HttpsURLConnection conn) throws IOException {
         if (DEBUG) {
             Log.d(TAG, "Load custom SSL certificates");
         }
         
         final SSLContext sslContext;
         try {
             // Load SSL certificates:
             // http://nelenkov.blogspot.com/2011/12/using-custom-certificate-trust-store-on.html
             // Earlier Android versions do not have updated root CA
             // certificates, resulting in connection errors.
             final KeyStore keyStore = loadCertificates(context);
             final KeyManagerFactory km = KeyManagerFactory
                     .getInstance(KeyManagerFactory.getDefaultAlgorithm());
             km.init(keyStore, "mysecret".toCharArray());
             final TrustManagerFactory tmf;
             tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                     .getDefaultAlgorithm());
             tmf.init(keyStore);
             
             // Init SSL connection with custom certificates.
             // The same SecureRandom instance is used for every connection to
             // speed up initialization.
             sslContext = SSLContext.getInstance("TLS");
             sslContext.init(km.getKeyManagers(), tmf.getTrustManagers(),
                 SECURE_RANDOM);
         } catch (GeneralSecurityException e) {
             throw new IOException("Failed to initialize SSL engine", e);
         }
         
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             // Fix slow read:
             // http://code.google.com/p/android/issues/detail?id=13117
             // Prior to ICS, the host name is still resolved even if we already
             // know its IP address, for each connection.
             final SSLSocketFactory delegate = sslContext.getSocketFactory();
             final SSLSocketFactory socketFactory = new SSLSocketFactory() {
                 @Override
                 public Socket createSocket(String host, int port)
                         throws IOException, UnknownHostException {
                     InetAddress addr = InetAddress.getByName(host);
                     injectHostname(addr, host);
                     return delegate.createSocket(addr, port);
                 }
                 
                 @Override
                 public Socket createSocket(InetAddress host, int port)
                         throws IOException {
                     return delegate.createSocket(host, port);
                 }
                 
                 @Override
                 public Socket createSocket(String host, int port,
                         InetAddress localHost, int localPort)
                         throws IOException, UnknownHostException {
                     return delegate.createSocket(host, port, localHost,
                         localPort);
                 }
                 
                 @Override
                 public Socket createSocket(InetAddress address, int port,
                         InetAddress localAddress, int localPort)
                         throws IOException {
                     return delegate.createSocket(address, port, localAddress,
                         localPort);
                 }
                 
                 private void injectHostname(InetAddress address, String host) {
                     try {
                         Field field = InetAddress.class
                                 .getDeclaredField("hostName");
                         field.setAccessible(true);
                         field.set(address, host);
                     } catch (Exception ignored) {
                     }
                 }
                 
                 @Override
                 public Socket createSocket(Socket s, String host, int port,
                         boolean autoClose) throws IOException {
                     injectHostname(s.getInetAddress(), host);
                     return delegate.createSocket(s, host, port, autoClose);
                 }
                 
                 @Override
                 public String[] getDefaultCipherSuites() {
                     return delegate.getDefaultCipherSuites();
                 }
                 
                 @Override
                 public String[] getSupportedCipherSuites() {
                     return delegate.getSupportedCipherSuites();
                 }
             };
             conn.setSSLSocketFactory(socketFactory);
         } else {
             conn.setSSLSocketFactory(sslContext.getSocketFactory());
         }
         
         conn.setHostnameVerifier(new StrictHostnameVerifier());
     }
 }
