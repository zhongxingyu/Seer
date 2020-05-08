 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.common.util.service;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.commons.codec.binary.Base64;
 
 import de.escidoc.core.common.servlet.EscidocServlet;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 /*
  * Created on 05.10.2006
  * 
  */
 
 /**
  * @author mih
  * 
  *         Class for requesting http-requests.
  * 
  * 
  */
 public class HttpRequester {
     private int timeout = 180000;
 
     private static final boolean SSL = false;
 
     private String domain = null;
 
     private String securityHandle = null;
 
     private String cookie = null;
 
     /**
      * Default-Constructor.
      * 
      * @param domain
      *            The domain to send requests to.
      * @binding
      */
     public HttpRequester(final String domain) {
         this.domain = domain;
     }
 
     /**
      * Constructor with security-Handle for HTTP-Basic-Authentication.
      * 
      * @param domain
      *            The domain to send requests to.
      * @param securityHandle
      *            The escidoc security handle.
      * 
      * @binding
      */
     public HttpRequester(final String domain, final String securityHandle) {
         this.securityHandle = securityHandle;
         this.domain = domain;
     }
 
     /**
      * Sets the followRedirects-Flag of the URLConnection.
      * 
      * @param flag
      *            boolean flag
      * 
      */
     public void setFollowRedirects(final boolean flag) {
         HttpURLConnection.setFollowRedirects(flag);
         HttpsURLConnection.setFollowRedirects(flag);
     }
 
     /**
      * Sends a GET-request to given URI and returns result as String.
      * 
      * @param resource
      *            String resource
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doGet(final String resource) throws Exception {
         return request(resource, "GET", null);
     }
 
     /**
      * Sends a PUT-request with the given body to given URI and returns result
      * as String.
      * 
      * @param resource
      *            String resource
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doPut(final String resource, final String body)
         throws Exception {
         if (body == null || body.equals("")) {
             throw new Exception("body may not be null");
         }
         return request(resource, "PUT", body);
     }
 
     /**
      * Sends a POST-request with the given body to given URI and returns result
      * as String.
      * 
      * @param resource
      *            String resource
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doPost(final String resource, final String body)
         throws Exception {
         if (body == null || body.equals("")) {
             throw new Exception("body may not be null");
         }
         return request(resource, "POST", body);
     }
 
     /**
      * Sends a DELETE-request to given URI and returns result as String.
      * 
      * @param resource
      *            String resource
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     public String doDelete(final String resource) throws Exception {
         return request(resource, "DELETE", null);
     }
 
     /**
      * Sends request with given method and given body to given URI and returns
      * result as String.
      * 
      * @param resource
      *            String resource
      * @param method
      *            String method
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     private String request(
         final String resource, final String method, final String body)
         throws Exception {
         if (SSL) {
             return requestSsl(resource, method, body);
         }
         else {
             return requestNoSsl(resource, method, body);
         }
     }
 
     /**
      * Sends request with given method and given body to given URI and returns
      * result as String.
      * 
      * @param resource
      *            String resource
      * @param method
      *            String method
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     private String requestSsl(
         final String resource, final String method, final String body)
         throws Exception {
         URL url;
         InputStream is = null;
         StringBuffer response = new StringBuffer();
 
         // Open Connection to given resource
         url = new URL(domain + resource);
         TrustManager[] tm = { new RelaxedX509TrustManager() };
         SSLContext sslContext = SSLContext.getInstance("SSL");
         sslContext.init(null, tm, new java.security.SecureRandom());
         SSLSocketFactory sslSF = sslContext.getSocketFactory();
         HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
         con.setSSLSocketFactory(sslSF);
 
         // Set Basic-Authentication Header
         if (securityHandle != null && !securityHandle.equals("")) {
             String encoding =
                new String(Base64.encodeBase64(securityHandle
                     .getBytes(XmlUtility.CHARACTER_ENCODING)));
             con.setRequestProperty("Authorization", "Basic " + encoding);
             // Set Cookie
             con.setRequestProperty("Cookie", StringUtility.concatenateToString(
                 EscidocServlet.COOKIE_LOGIN, "=", securityHandle));
         }
         else if (getCookie() != null) {
             con.setRequestProperty("Cookie", getCookie());
         }
 
         // Set request-method and timeout
         con.setRequestMethod(method.toUpperCase());
         con.setReadTimeout(timeout);
 
         // If PUT or POST, write given body in Output-Stream
         if ((method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST"))
             && body != null) {
             con.setDoOutput(true);
             OutputStream out = con.getOutputStream();
             out.write(body.getBytes(XmlUtility.CHARACTER_ENCODING));
             out.flush();
             out.close();
         }
 
         // Request
         is = con.getInputStream();
         setCookie(con.getHeaderField("Set-cookie"));
 
         // Read response
         String currentLine = null;
         BufferedReader br =
             new BufferedReader(new InputStreamReader(is,
                 XmlUtility.CHARACTER_ENCODING));
         while ((currentLine = br.readLine()) != null) {
             response.append(currentLine + "\n");
         }
         is.close();
         return response.toString();
     }
 
     /**
      * Sends request with given method and given body to given URI and returns
      * result as String.
      * 
      * @param resource
      *            String resource
      * @param method
      *            String method
      * @param body
      *            String body
      * @return String response
      * @throws Exception
      *             e
      * 
      */
     private String requestNoSsl(
         final String resource, final String method, final String body)
         throws Exception {
         URL url;
         HttpURLConnection con = null;
         InputStream is = null;
         OutputStream out = null;
         StringBuffer response = new StringBuffer();
 
         try {
             // Open Connection to given resource
             url = new URL(domain + resource);
             con = (HttpURLConnection) url.openConnection();
 
             // Set Basic-Authentication Header
             if (securityHandle != null && !securityHandle.equals("")) {
                 String encoding =
                    new String(Base64.encodeBase64(securityHandle
                         .getBytes(XmlUtility.CHARACTER_ENCODING)));
                 con.setRequestProperty("Authorization", "Basic " + encoding);
                 // Set Cookie
                 con.setRequestProperty("Cookie", StringUtility
                     .concatenateToString(EscidocServlet.COOKIE_LOGIN, "=",
                         securityHandle));
             }
             else if (getCookie() != null) {
                 con.setRequestProperty("Cookie", getCookie());
             }
 
             // Set request-method and timeout
             con.setRequestMethod(method.toUpperCase());
             con.setReadTimeout(timeout);
 
             // If PUT or POST, write given body in Output-Stream
             if ((method.equalsIgnoreCase("PUT") || method
                 .equalsIgnoreCase("POST"))
                 && body != null) {
                 con.setDoOutput(true);
                 out = con.getOutputStream();
                 out.write(body.getBytes(XmlUtility.CHARACTER_ENCODING));
                 out.flush();
                 out.close();
             }
 
             // Request
             is = con.getInputStream();
             setCookie(con.getHeaderField("Set-cookie"));
 
             // Read response
             String currentLine = null;
             BufferedReader br =
                 new BufferedReader(new InputStreamReader(is,
                     XmlUtility.CHARACTER_ENCODING));
             while ((currentLine = br.readLine()) != null) {
                 response.append(currentLine + "\n");
             }
         }
         finally {
             try {
                 out.close();
             }
             catch (Exception e) {
             }
             try {
                 is.close();
             }
             catch (Exception e) {
             }
             try {
                 con.disconnect();
             }
             catch (Exception e) {
             }
         }
         return response.toString();
     }
 
     /**
      * @author mih
      * 
      *         Overwrite X509TrustManager.
      * 
      * 
      */
     class RelaxedX509TrustManager implements X509TrustManager {
         /**
          * Gets accepted Issuers.
          * 
          * @return X509Certificate[] response
          * 
          */
         public java.security.cert.X509Certificate[] getAcceptedIssuers() {
             return null;
         }
 
         /**
          * Checks Client trusted.
          * 
          * @param chain
          *            X509Certificate[]
          * @param authType
          *            String
          * 
          */
         public void checkClientTrusted(
             final java.security.cert.X509Certificate[] chain,
             final String authType) {
         }
 
         /**
          * Checks Server trusted.
          * 
          * @param chain
          *            X509Certificate[]
          * @param authType
          *            String
          * 
          */
         public void checkServerTrusted(
             final java.security.cert.X509Certificate[] chain,
             final String authType) {
         }
     }
 
     /**
      * @return the cookie
      */
     public String getCookie() {
         return cookie;
     }
 
     /**
      * @param cookie
      *            the cookie to set
      */
     public void setCookie(final String cookie) {
         this.cookie = cookie;
     }
 
     /**
      * @param timeout
      *            the timeout to set
      */
     public void setTimeout(final int timeout) {
         this.timeout = timeout;
     }
 
 }
