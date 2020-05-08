 package com.janrain.oauth2;
 
 import com.janrain.backplane2.server.BackplaneServerException;
 import com.janrain.backplane2.server.BackplaneMessage;
 import com.janrain.backplane2.server.GrantType;
 import com.janrain.backplane2.server.Scope;
 import com.janrain.backplane2.server.Token;
 import com.janrain.backplane2.server.dao.DAOFactory;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.crypto.ChannelUtil;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.*;
 
 /**
  * @author Johnny Bufu
  */
 public class AnonymousTokenRequest implements TokenRequest {
 
     // - PUBLIC
 
     public AnonymousTokenRequest( String callback, String bus, String scope, String refreshToken,
                                   DAOFactory daoFactory, HttpServletRequest request, String authHeader) throws TokenException {
 
         this.daoFactory = daoFactory;
 
         this.grantType = StringUtils.isEmpty(refreshToken) ? GrantType.ANONYMOUS : GrantType.REFRESH_ANONYMOUS;
 
         if (StringUtils.isBlank(callback)) {
             throw new TokenException("Callback cannot be blank");
         }
 
         if (!callback.matches("[\\._a-zA-Z0-9]*")) {
             throw new TokenException("callback parameter value is malformed");
         }
 
         this.bus = bus;
         if ( StringUtils.isEmpty(refreshToken) ^ StringUtils.isNotEmpty(this.bus)) {
             throw new TokenException("bus parameter is required if and only if refresh_token is not present");
         }
 
         try {
             if ( StringUtils.isNotEmpty(this.bus) && daoFactory.getBusDao().get(this.bus) == null) {
                 throw new TokenException("Invalid bus: " + bus);
             }
         } catch (BackplaneServerException e) {
             logger.error("error processing anonymous token request: " + e.getMessage(), e);
             throw new TokenException(OAuth2.OAUTH2_TOKEN_SERVER_ERROR, "error processing anonymous token request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         }
 
         this.requestScope = new Scope(scope);
        if ( this.requestScope.isAuthorizationRequired() ) {
             throw new TokenException(OAuth2.OAUTH2_TOKEN_INVALID_SCOPE, "Buses and channels not allowed in the scope of anonymous token requests");
         }
 
         if (StringUtils.isNotEmpty(refreshToken)) {
             this.refreshToken = Token.fromRequest(daoFactory, request, refreshToken, authHeader);
             if (! this.refreshToken.getScope().containsScope(this.requestScope)) {
                 throw new TokenException(OAuth2.OAUTH2_TOKEN_INVALID_SCOPE, "invalid scope for refresh token: " + refreshToken + " : " + scope);
             }
         }
 
         // todo: check this properly, perhaps in controller?
         // throw new TokenException("Must not include client_secret for anonymous token requests");
     }
 
     @Override
     public Map<String,Object> tokenResponse() throws TokenException {
         logger.info("Responding to anonymous token request...");
         final Token accessToken;
         final Integer expiresIn = grantType.getTokenExpiresSecondsDefault();
         Date expires = new Date(System.currentTimeMillis() + expiresIn.longValue() * 1000);
         Scope processedScope = processScope();
         try {
             accessToken = new Token.Builder(grantType, processedScope.toString()).expires(expires).buildToken();
             daoFactory.getTokenDao().persist(accessToken);
             return accessToken.response(generateRefreshToken(grantType.getRefreshType(), processedScope, daoFactory));
         } catch (Exception e) {
             logger.error("error processing anonymous access token request: " + e.getMessage(), e);
             throw new TokenException(OAuth2.OAUTH2_TOKEN_SERVER_ERROR, "error processing anonymous token request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         } finally {
             logger.info("exiting anonymous token request");
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
 
     private static final Logger logger = Logger.getLogger(AnonymousTokenRequest.class);
 
     private static final int CHANNEL_NAME_LENGTH = 32;
 
     private DAOFactory daoFactory;
     private final GrantType grantType;
     private final String bus;
     private final Scope requestScope;
     private Token refreshToken;
 
     private static String generateRefreshToken(GrantType refreshType, Scope scope, DAOFactory daoFactory) throws SimpleDBException, BackplaneServerException {
         if (refreshType == null || ! refreshType.isRefresh()) return null;
         Token refreshToken = new Token.Builder(refreshType, scope.toString()).buildToken();
         daoFactory.getTokenDao().persist(refreshToken);
         return refreshToken.getIdValue();
     }
 
     private Scope processScope() throws TokenException {
         Map<BackplaneMessage.Field,LinkedHashSet<String>> scopeMap = new LinkedHashMap<BackplaneMessage.Field, LinkedHashSet<String>>();
         scopeMap.putAll(requestScope.getScopeMap());
         final String bus;
         final String channel;
         if (refreshToken != null ) {
             final Set<String> channels = refreshToken.getScope().getScopeFieldValues(BackplaneMessage.Field.CHANNEL);
             final Set<String> buses = refreshToken.getScope().getScopeFieldValues(BackplaneMessage.Field.BUS);
             if ( channels == null || channels.isEmpty() || channels.size() > 1 ||
                  buses == null || buses.isEmpty() || buses.size() > 1 ) {
                 throw new TokenException("invalid anonymous refresh token: " + refreshToken.getIdValue());
             } else {
                 bus = buses.iterator().next();
                 channel = channels.iterator().next();
             }
         } else {
             bus = this.bus; // bind generated channel to the requested bus
             channel = ChannelUtil.randomString(CHANNEL_NAME_LENGTH);
         }
         scopeMap.put(BackplaneMessage.Field.BUS, new LinkedHashSet<String>() {{ add(bus);}});
         scopeMap.put(BackplaneMessage.Field.CHANNEL, new LinkedHashSet<String>() {{ add(channel);}});
         return new Scope(scopeMap);
     }
 }
