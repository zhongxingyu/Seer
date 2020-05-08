 package com.janrain.oauth2;
 
 import com.janrain.backplane2.server.*;
 import com.janrain.backplane2.server.config.Client;
 import com.janrain.backplane2.server.dao.DAOFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.util.Pair;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 import static com.janrain.oauth2.OAuth2.OAUTH2_TOKEN_INVALID_CLIENT;
 import static com.janrain.oauth2.OAuth2.OAUTH2_TOKEN_UNSUPPORTED_GRANT;
 import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
 
 /**
  * @author Johnny Bufu
  */
 public class AuthenticatedTokenRequest implements TokenRequest {
 
     // - PUBLIC
 
     public AuthenticatedTokenRequest(String grantType, Client authenticatedClient, String code,
                                      String redirectUri, String refreshToken, String scope,
                                      DAOFactory daoFactory, HttpServletRequest request, String authHeader) throws TokenException {
 
         this.daoFactory = daoFactory;
 
         this.authenticatedClientId = authenticatedClient.getClientId();
         this.authenticatedClientSourceUrl = authenticatedClient.getSourceUrl();
         try {
             // this must be done as early as possible in order to invalidate misused codes/grants, before any other failures
             if (StringUtils.isNotEmpty(code)) {
                 // throw whenever a code is parameter is supplied, regardless of the grant type declared in the request
                 this.codeGrant = new GrantLogic(daoFactory).getAndActivateCodeGrant(code, this.authenticatedClientId);
             }
         } catch (BackplaneServerException e) {
             logger.error("error processing token request: " + e.getMessage(), e);
             throw new TokenException(OAuth2.OAUTH2_TOKEN_SERVER_ERROR, "error processing token request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
         if (StringUtils.isEmpty(authenticatedClientId)) {
             throw new TokenException(OAUTH2_TOKEN_INVALID_CLIENT, "Client authentication failed", SC_UNAUTHORIZED);
         }
 
         this.grantType = GrantType.fromOauthType(grantType);
         if (this.grantType == null) {
             throw new TokenException(OAUTH2_TOKEN_UNSUPPORTED_GRANT, "Invalid grant type: " + grantType);
         }
 
         if ( this.grantType == GrantType.AUTHORIZATION_CODE ^ StringUtils.isNotEmpty(code) ) {
             throw new TokenException("code is required if and only if grant_type is authorization_code");
         }
 
         if ( this.grantType == GrantType.AUTHORIZATION_CODE ^ codeGrant != null ) {
             throw new TokenException("Invalid code: " + code);
         }
 
         if ( this.grantType == GrantType.REFRESH_PRIVILEGED ^ StringUtils.isNotEmpty(refreshToken) ) {
             throw new TokenException("refresh_token is required if and only if grant_type is refresh_token");
         }
 
         if ( this.grantType == GrantType.AUTHORIZATION_CODE ^ StringUtils.isNotEmpty(redirectUri) ) {
             throw new TokenException("redirect_uri is required if and only if grant_type is authorization_code");
         }
         try {
             OAuth2.validateRedirectUri(redirectUri, authenticatedClient.getRedirectUri());
         } catch (ValidationException e) {
             throw new TokenException(e.getCode(), "Invalid redirect_uri: " + redirectUri);
         }
 
         if (StringUtils.isNotEmpty(refreshToken)) {
             this.refreshToken = Token.fromRequest(daoFactory, request, refreshToken, authHeader);
         }
 
         this.requestScope = new Scope(scope);
     }
 
     @Override
     public Map<String,Object> tokenResponse() throws TokenException {
         logger.info("Responding to authenticated token request...");
         final Token accessToken;
        final Integer expiresIn = grantType.getTokenExpiresSecondsDefault();
         Date expires = new Date(System.currentTimeMillis() + expiresIn.longValue() * 1000);
         Pair<Scope,List<String>> scopeGrants = processScope();
         try {
             accessToken = new Token.Builder(grantType.getAccessType(), scopeGrants.getLeft().toString())
                     .expires(expires)
                     .issuedToClient(authenticatedClientId)
                     .clientSourceUrl(authenticatedClientSourceUrl)
                     .grants(scopeGrants.getRight())
                     .buildToken();
             daoFactory.getTokenDao().persist(accessToken);
             return accessToken.response(generateRefreshToken(grantType.getRefreshType(), accessToken));
         } catch (SimpleDBException e) {
             logger.error("error processing anonymous access token request: " + e.getMessage(), e);
             throw new TokenException(OAuth2.OAUTH2_TOKEN_SERVER_ERROR, "error processing anonymous token request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         } catch (BackplaneServerException bpe) {
             logger.error("error processing anonymous access token request: " + bpe.getMessage(), bpe);
             throw new TokenException(OAuth2.OAUTH2_TOKEN_SERVER_ERROR, "error processing anonymous token request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 
         } finally {
             try {
                 if (this.refreshToken != null) {
                     daoFactory.getTokenDao().delete(this.refreshToken.getIdValue());
                 }
             } catch (BackplaneServerException e) {
                 logger.error("error deleting used refresh token: " + refreshToken.getIdValue(), e);
             }
         }
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(AuthenticatedTokenRequest.class);
 
     private final DAOFactory daoFactory;
     private final GrantType grantType;
     private final String authenticatedClientId;
     private final String authenticatedClientSourceUrl;
     private final Scope requestScope;
 
     private Grant codeGrant;
     private Token refreshToken;
 
     private Pair<Scope,List<String>> processScope() throws TokenException {
 
         if (refreshToken != null) {
             return new Pair<Scope, List<String>>(Scope.checkCombine(refreshToken.getScope(), requestScope), refreshToken.getBackingGrants());
         }
 
         if (codeGrant != null) {
             return new Pair<Scope, List<String>>(Scope.checkCombine(new Scope(codeGrant.get(Grant.GrantField.AUTHORIZED_SCOPES)),requestScope), new ArrayList<String>() {{add(codeGrant.getIdValue());}});
         }
 
         // client credentials scope
         try {
             Map<Scope,Set<Grant>> scopeGrantsMap = new GrantLogic(daoFactory).retrieveClientGrants(authenticatedClientId, requestScope);
             List<String> allGrants = new ArrayList<String>();
             for(Set<Grant> grants : scopeGrantsMap.values()) {
                 for(Grant grant : grants) {
                     allGrants.add(grant.getIdValue());
                 }
             }
 
             if (requestScope.isAuthorizationRequired()) {
                 if (!scopeGrantsMap.keySet().containsAll(requestScope.getAuthReqScopes())) {
                     throw new TokenException(OAuth2.OAUTH2_TOKEN_INVALID_SCOPE, "unauthorized scope: " + requestScope);
                 }
                 return new Pair<Scope, List<String>>(requestScope, allGrants);
             } else {
                 return new Pair<Scope, List<String>>(Scope.checkCombine(scopeGrantsMap.keySet().iterator().next(), requestScope), allGrants);
             }
         } catch (BackplaneServerException e) {
             logger.error("error processing token request: " + e.getMessage(), e);
             throw new TokenException(OAuth2.OAUTH2_TOKEN_SERVER_ERROR, "error processing token request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
     }
 
     private String generateRefreshToken(GrantType refreshType, Token accessToken) throws SimpleDBException, BackplaneServerException {
         if (! refreshType.isRefresh()) return null;
         Token refreshToken = new Token.Builder(refreshType, accessToken.getScopeString())
                 .issuedToClient(authenticatedClientId)
                 .clientSourceUrl(authenticatedClientSourceUrl)
                 .grants(accessToken.getBackingGrants())
                 .buildToken();
         daoFactory.getTokenDao().persist(refreshToken);
         return refreshToken.getIdValue();
     }
 }
