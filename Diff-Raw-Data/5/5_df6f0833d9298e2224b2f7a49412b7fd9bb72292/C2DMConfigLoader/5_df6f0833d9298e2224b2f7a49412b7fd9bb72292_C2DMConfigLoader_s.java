 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.android.c2dm.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.logging.Logger;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyFactory;
 
 /**
  * Stores config information related to Android Cloud to Device Messaging.
  */
 @Singleton
 class C2DMConfigLoader {
     private static final Logger log = Logger.getLogger(C2DMConfigLoader.class.getName());
     private final ObjectifyFactory of;
     
     private String currentToken;
     
     @Inject
     C2DMConfigLoader(final ObjectifyFactory of) {
         this.of = of;
     }
     
     /**
      * Update the token. Called on "Update-Client-Auth" or when admins set a new
      * token.
      * @param token
      */
     public void updateToken(String token) {
         if (token != null) {
             currentToken = token;
             
             final Objectify session = of.begin();
             final C2DMConfig dmConf = getC2DMConfig(session);
             dmConf.setAuthToken(token);
             session.put(dmConf);
         }
     }
     
     /**
      * Token expired
      */
     public void invalidateCachedToken() {
         currentToken = null;
     }
     
     /**
      * Return the auth token. It'll first memcache, if not found will use the
      * database.
      * @return
      */
     public String getToken() {
         if (currentToken == null) {
             currentToken = getC2DMConfig().getAuthToken();
         }
         return currentToken;
     }
     
     public C2DMConfig getC2DMConfig() {
         return getC2DMConfig(of.begin());
     }
     
     private C2DMConfig getC2DMConfig(Objectify session) {
        C2DMConfig dmConfig = session.find(new Key<C2DMConfig>(C2DMConfig.class, 1));
         if (dmConfig == null) {
             String token = null;
             
             final InputStream authFile = getClass().getResourceAsStream("/c2dm.auth");
             if (authFile != null) {
                 final BufferedReader reader = new BufferedReader(new InputStreamReader(authFile));
                 try {
                     token = reader.readLine();
                     if (token != null) {
                         token = token.trim();
                     }
                 } catch (IOException e) {
                 } finally {
                     try {
                         authFile.close();
                     } catch (IOException ignore) {
                     }
                 }
             }
             if (token != null) {
                 dmConfig = new C2DMConfig();
                 dmConfig.setAuthToken(token);
                 session.put(dmConfig);
             } else {
                 throw new IllegalStateException("Missing C2DM authentication token");
             }
             
             session.put(dmConfig);
         }
         return dmConfig;
     }
 }
