 /*
  *  Copyright (C) 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  * 
  *  These coded instructions, statements, and computer programs contain
  *  proprietary information of Hoccer GmbH Berlin, and are copy protected
  *  by law. They may be used, modified and redistributed under the terms
  *  of GNU General Public License referenced below. 
  *     
  *  Alternative licensing without the obligations of the GPL is
  *  available upon request.
  * 
  *  GPL v3 Licensing:
  * 
  *  This file is part of the "Hoccer Java-API".
  * 
  *  Hoccer Java-API is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Hoccer Java-API is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Hoccer Java-API. If not, see <http: * www.gnu.org/licenses/>.
  */
 package com.hoccer.api;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.util.EntityUtils;
 
 import com.google.gson.Gson;
 
 public class HoccerClient {
 
     private DefaultHttpClient      mHttpClient;
     private final ClientConfig     mConfig;
     private final RemoteClientData mRemoteClientData;
 
     public HoccerClient(ClientConfig config) throws ClientProtocolException, IOException {
         mConfig = config;
         setupHttpClient();
 
         Gson gson = new Gson();
 
         HttpPost clientCreationRequest = new HttpPost("http://linker.beta.hoccer.com/clients");
         clientCreationRequest
                 .setEntity(new StringEntity(gson.toJson(new RemoteClientData(mConfig))));
 
         mRemoteClientData = gson.fromJson(convert(mHttpClient.execute(clientCreationRequest)),
                 RemoteClientData.class);
     }
 
     public String getId() {
         return mRemoteClientData.uri.substring(9);
     }
 
     private void setupHttpClient() {
         BasicHttpParams httpParams = new BasicHttpParams();
         HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
         ConnManagerParams.setMaxTotalConnections(httpParams, 100);
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
         ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
         mHttpClient = new DefaultHttpClient(cm, httpParams);
         mHttpClient.getParams().setParameter("http.useragent", mConfig.getApplicationName());
     }
 
     private String convert(HttpResponse response) throws ParseException, IOException {
         HttpEntity entity = response.getEntity();
         if (entity == null) {
             throw new ParseException("http body was empty");
         }
         long len = entity.getContentLength();
         if (len > 2048) {
            throw new ParseException("http body is to big and must be streamed");
         }
 
         String body = EntityUtils.toString(entity);
         return body;
     }
 
     static class RemoteClientData {
         public String uri;
         public String application;
 
         RemoteClientData() {
         }
 
         RemoteClientData(ClientConfig configuration) {
             application = configuration.getApplicationName();
         }
     }
 }
