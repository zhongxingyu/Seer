 /*
  * Copyright 2004-2006 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.flex2.rpc.remoting.security.authentication.impl;
 
 import java.security.Principal;
 
 import org.seasar.flex2.rpc.remoting.message.RemotingMessageConstants;
 import org.seasar.flex2.rpc.remoting.message.data.Message;
 import org.seasar.flex2.rpc.remoting.message.data.factory.MessageFactory;
 import org.seasar.flex2.rpc.remoting.security.RemotingServicePrincipal;
 import org.seasar.flex2.rpc.remoting.security.RemotingServicePrincipalStorage;
 import org.seasar.flex2.rpc.remoting.security.authentication.RemotingServiceAuthenticationContext;
 import org.seasar.flex2.rpc.remoting.security.realm.RemotingServiceRealm;
 
 public class RemotingServiceAuthenticationContextImpl implements
         RemotingServiceAuthenticationContext {
 
     private MessageFactory messageFactory;
 
     private RemotingServiceRealm realm;
 
     private RemotingServicePrincipalStorage storage;
 
     public void authenticate() {
         if (getRemotingServicePrincipal() == null) {
             doAuthenticate(messageFactory.createRequestMessage());
         }
     }
 
     public void configRealm(final RemotingServiceRealm realm) {
         this.realm = realm;
     }
 
     public final Principal getUserPrincipal() {
         return storage.getUserPrincipal();
     }
 
     public void invalidate() {
         storage.savePrincipal(null);
     }
 
     public boolean isAuthenticated() {
        if (getUserPrincipal() == null ) {
             authenticate();
         }
         return getUserPrincipal() != null;
     }
 
     public boolean isUserInRole(final String role) {
         boolean isUserInRole = false;
 
         if (isAuthenticated()) {
             if (realm.hasRole(getRemotingServicePrincipal(), role)) {
                 isUserInRole = true;
             }
         }
         return isUserInRole;
     }
 
     public void setMessageFactory(MessageFactory messageFactory) {
         this.messageFactory = messageFactory;
     }
 
     public void setStorage(RemotingServicePrincipalStorage storage) {
         this.storage = storage;
     }
 
     private final void doAuthenticate(final Message requestMessage) {
         final String userid = requestMessage
                .getHeader(RemotingMessageConstants.REMOTE_CREDENTIALS_USERNAME);
         if (userid != null) {
             final String password = requestMessage
                    .getHeader(RemotingMessageConstants.REMOTE_CREDENTIALS_PASSWORD);
             storage.savePrincipal(realm.authenticate(userid, password));
         }
     }
 
     private final RemotingServicePrincipal getRemotingServicePrincipal() {
         return (RemotingServicePrincipal) getUserPrincipal();
     }
 }
