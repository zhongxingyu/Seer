 /*
  * Copyright (C) 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.google.api.explorer.client;
 
 import com.google.api.explorer.client.base.ApiService;
 import com.google.api.explorer.client.event.AuthGrantedEvent;
 import com.google.api.explorer.client.event.AuthRequestedEvent;
 import com.google.api.gwt.oauth2.client.Auth;
 import com.google.api.gwt.oauth2.client.AuthRequest;
 import com.google.common.collect.Maps;
import com.google.gwt.core.client.Callback;
 import com.google.gwt.event.shared.EventBus;
 
 import java.util.Map;
 
 /**
  * Manages authentication state by accepting {@link AuthRequestedEvent}s and
  * dispatching {@link AuthGrantedEvent}s when auth is complete.
  *
  * @author jasonhall@google.com (Jason Hall)
  */
 public class AuthManager implements AuthRequestedEvent.Handler {
 
   private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
   private static final String CLIENT_ID = "835264079878.apps.googleusercontent.com";
   private static final Map<ApiService, String> AUTH_TOKENS = Maps.newHashMap();
   private final EventBus eventBus;
   private final AppState appState;
 
   public AuthManager(EventBus eventBus, AppState appState) {
     eventBus.addHandler(AuthRequestedEvent.TYPE, this);
     this.eventBus = eventBus;
     this.appState = appState;
   }
 
   @Override
   public void onAuthRequested(final AuthRequestedEvent event) {
     String serviceName = appState.getCurrentService().getName();
 
     // TODO(jasonhall): Show some indication that auth is in progress here.
 
     AuthRequest req = new AuthRequest(AUTH_URL, CLIENT_ID).withScopes(event.scope);
 
    Auth.get().login(req, new Callback<String, Throwable>() {
       @Override
      public void onSuccess(String token) {
         AUTH_TOKENS.put(appState.getCurrentService(), token);
         eventBus.fireEvent(new AuthGrantedEvent(appState.getCurrentService(), token));
       }
 
       @Override
       public void onFailure(Throwable caught) {
         throw new RuntimeException(caught);
       }
     });
   }
 
   /**
    * Get the token stored for the current service, or {@code null} if there is
    * no token.
    */
   public String getToken() {
     return AUTH_TOKENS.get(appState.getCurrentService());
   }
 
   /**
    * "Revokes" the access token, in the sense that it tells the app to forget
    * that it knows the auth token.
    *
    * <p>
    * This method does *not* actually revoke the token on the server, but will,
    * when this is supported.
    * </p>
    */
   public void revokeAccess() {
     // TODO(jasonhall): This should actually revoke access on the server, and
     // remove the token from the cookie. It currently does nothing more than
     // "forget" it knows the token.
     AUTH_TOKENS.remove(appState.getCurrentService());
   }
 }
