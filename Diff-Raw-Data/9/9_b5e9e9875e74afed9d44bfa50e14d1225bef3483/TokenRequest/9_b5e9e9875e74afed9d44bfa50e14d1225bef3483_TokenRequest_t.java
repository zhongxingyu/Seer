 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.oauth2;
 
 import com.janrain.backplane2.server.Grant;
 import com.janrain.backplane2.server.Scope;
 import com.janrain.backplane2.server.Token;
 import com.janrain.backplane2.server.config.Backplane2Config;
 import com.janrain.backplane2.server.config.Client;
 import com.janrain.backplane2.server.dao.DaoFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import javax.inject.Inject;
 
 import static com.janrain.oauth2.OAuth2.OAUTH2_TOKEN_INVALID_GRANT;
 import static com.janrain.oauth2.OAuth2.OAUTH2_TOKEN_UNSUPPORTED_GRANT;
 
 /**
  * Token request
  * @author Tom Raney
  */
 
 public class TokenRequest {
 
     String grant_type;
     String redirect_uri;
     String codeId;
     String scope;
     String callback;
     Grant grant;
     Client client;
     Scope scopes;
     DaoFactory daoFactory;
 
     @Inject
     private Backplane2Config bpConfig;
 
     private static final Logger logger = Logger.getLogger(TokenRequest.class);
 
     public TokenRequest(DaoFactory daoFactory, Client client, String grant_type, String redirect_uri, String codeId, String scope, String callback) {
         this.client = client;
         this.grant_type = grant_type;
         this.redirect_uri = redirect_uri;
         this.codeId = codeId;
         this.scope = scope;
         this.daoFactory = daoFactory;
         this.callback = callback;
 
         try {
             setGrant(daoFactory.getGrantDao().getGrantSetCodeUsed(codeId));
         } catch (Exception e) {
             //do nothing
         }
     }
 
     public TokenRequest(Client anonymousClient, String grant_type, String scope, String callback) {
         this.client = anonymousClient;
         this.grant_type = grant_type;
         this.scope = scope;
         this.callback = callback;
     }
 
     public Grant getGrant() {
         return this.grant;
     }
 
     public void setGrant(Grant grant) {
         this.grant = grant;
     }
 
     /**
      * Validates input
      * @return null if no errors exist or a HashMap with an error
      */
 
     public void validate() throws SimpleDBException, TokenException {
          // use Oauth2 5.2 response codes for errors - http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-5.2
 
         if (StringUtils.isNotEmpty(callback)) {
             if (!callback.matches("[\\._a-zA-Z0-9]*")) {
                 throw new TokenException("callback parameter value is malformed");
             }
         }
 
         if (client == null || StringUtils.isEmpty(client.getClientId())) {
             throw new TokenException("Missing value for client_id");
         }
 
         String client_id = client.getClientId();
         String client_secret = client.getClientSecret();
 
        if (!"client_credentials".equals(grant_type) && !"code".equals(grant_type)) {
             throw new TokenException(OAUTH2_TOKEN_UNSUPPORTED_GRANT, "Invalid grant type");
         }
 
         if ((grant_type.equals("code") && StringUtils.isEmpty(codeId)) || (grant_type.equals("client_credentials") && StringUtils.isNotEmpty(codeId)) ) {
             throw new TokenException("Missing code value");
         }
 
         if (grant_type.equals("client_credentials") && !client_id.equals(Token.ANONYMOUS) && StringUtils.isEmpty(client_secret)) {
             throw new TokenException("Missing client_secret value");
         }
 
         if (grant_type.equals("code") && StringUtils.isEmpty(redirect_uri)) {
             throw new TokenException("Missing redirect_uri value");
         }
 
         if (grant_type.equals("client_credentials") && client_id.equals(Token.ANONYMOUS) && StringUtils.isNotEmpty(client_secret)) {
             throw new TokenException("Must not include client_secret for anonymous requests");
         }
 
         // check the codeId
         if (StringUtils.isNotEmpty(codeId)) {
             //did a grant record pull from the db?
             if (grant == null) {
                 throw new TokenException(OAUTH2_TOKEN_INVALID_GRANT, "Authorization code is invalid");
             }
 
             if (this.grant.isCodeExpired()) {
                 throw new TokenException(OAUTH2_TOKEN_INVALID_GRANT, "Authorization code is expired");
             }
 
             // check the client - grant binding
             if ( ! grant.getGrantClientId().equals(client.getClientId()) ) {
                 throw new TokenException(OAUTH2_TOKEN_INVALID_GRANT, "Invalid grant");
             }
             
             // check redirect_uri
             try {
                 OAuth2.validateRedirectUri(redirect_uri, client.getRedirectUri());
             } catch (ValidationException e) {
                 throw new TokenException(e.getCode(), "Invalid redirect_uri.");
             }
         }
 
         if (StringUtils.isNotEmpty(scope)) {
             try {
                 this.scopes = new Scope(scope);
                 if (this.grant != null && !grant.isAllowedBuses(scopes.getBusesInScope())) {
                     throw new TokenException("Invalid scope");
                 }
             } catch (TokenException e) {
                 throw new TokenException(e.getOauthErrorCode(), e.getMessage());
             }
         }
     }
 }
 
 
