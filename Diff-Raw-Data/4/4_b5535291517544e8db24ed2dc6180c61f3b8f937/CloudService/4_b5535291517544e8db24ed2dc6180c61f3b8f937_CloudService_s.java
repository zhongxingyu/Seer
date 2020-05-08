 /*******************************************************************************
  * Copyright (C) 2009, 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com> These coded instructions,
  * statements, and computer programs contain proprietary information of Hoccer GmbH Berlin, and are
  * copy protected by law. They may be used, modified and redistributed under the terms of GNU
  * General Public License referenced below. Alternative licensing without the obligations of the GPL
  * is available upon request. GPL v3 Licensing: This file is part of the "Linccer Java-API". Linccer
  * Java-API is free software: you can redistribute it and/or modify it under the terms of the GNU
  * General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version. Linccer Java-API is distributed in the hope that
  * it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You
  * should have received a copy of the GNU General Public License along with Linccer Java-API. If
  * not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.hoccer.api;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.ParseException;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.params.ConnPerRoute;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.NoConnectionReuseStrategy;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.hoccer.http.HttpClientWithKeystore;
 import com.hoccer.util.HoccerLoggers;
 
 public class CloudService {
 
     private static final String LOG_TAG = CloudService.class.getSimpleName();
 
     private static final Logger LOG = HoccerLoggers.getLogger(LOG_TAG);
 
     private HttpClientWithKeystore mHttpClient;
     protected final ClientConfig mConfig;
 
     public CloudService(ClientConfig config) {
         mConfig = config;
 
         LOG.fine("initializing " + toString());
 
         setupHttpClient();
 
         /*
          * java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(
          * java.util.logging.Level.FINEST);
          * java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(
          * java.util.logging.Level.FINEST); System.setProperty("org.apache.commons.logging.Log",
          * "org.apache.commons.logging.impl.SimpleLog");
          * System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
          * System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
          * System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
          * System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers",
          * "debug");
          */
     }
 
     public ClientConfig getClientConfig() {
         return mConfig;
     }
 
     protected void setupHttpClient() {
 
         LOG.info("setting up http client");
 
         BasicHttpParams httpParams = new BasicHttpParams();
         HttpConnectionParams.setSoTimeout(httpParams, 70 * 1000);
         HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
         ConnManagerParams.setMaxTotalConnections(httpParams, 200);
         ConnPerRoute connPerRoute = new ConnPerRouteBean(400);
         ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
         ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
         // ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams,
         // HttpClientWithKeystore.getSchemeRegistry());
         HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setContentCharset(httpParams, "utf-8");
         mHttpClient = new HttpClientWithKeystore(cm, httpParams);
         mHttpClient.getParams().setParameter("http.useragent", mConfig.getApplicationName());
         mHttpClient.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
         mHttpClient.setReuseStrategy(new NoConnectionReuseStrategy());
     }
 
     protected JSONObject convertResponseToJsonObject(HttpResponse response) throws ParseException,
             IOException, JSONException, UpdateException {
         String body = convertResponseToString(response, false);
 
         JSONObject json = null;
         try {
             json = new JSONObject(body);
         } catch (Exception e) {
             throw new ParseException("could not parse the json '" + body + "'");
         }
 
         return json;
     }
 
     protected JSONArray convertResponseToJsonArray(HttpResponse response) throws ParseException,
             IOException, JSONException, UpdateException {
         String body = convertResponseToString(response, false);
         return new JSONArray(body);
     }
 
     protected String convertResponseToString(HttpResponse response, boolean pIgnoreStatus)
             throws IOException {
         int statuscode = response.getStatusLine().getStatusCode();
         if (statuscode != 200 && statuscode != 201 && !pIgnoreStatus) {
             throw new ParseException("server respond with "
                     + response.getStatusLine().getStatusCode()
                     + ": "
                    + EntityUtils.toString(response.getEntity(), response.getStatusLine()
                            .getStatusCode() + ": <unparsable body>"));
         }
 
         HttpEntity entity = response.getEntity();
         if (entity == null) {
             throw new ParseException("http body was empty");
         }
         long len = entity.getContentLength();
 
         if (len > 2048) {
 
             throw new ParseException("http body is to big and must be streamed (max is 2048, but was " + len + " byte)");
         }
 
         String body = EntityUtils.toString(entity, HTTP.UTF_8);
         return body;
     }
 
     protected HttpClientWithKeystore getHttpClient() {
         return mHttpClient;
     }
 
     protected String sign(String url) {
         return ApiSigningTools.sign(url, mConfig.getApiKey(), mConfig.getSharedSecret());
     }
 }
