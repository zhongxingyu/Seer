 package com.britesnow.samplesocial.service;
 
 import com.britesnow.samplesocial.dao.SocialIdEntityDao;
 import com.britesnow.samplesocial.entity.Service;
 import com.britesnow.samplesocial.entity.SocialIdEntity;
 import com.britesnow.samplesocial.oauth.OAuthType;
 import com.britesnow.samplesocial.oauth.OAuthUtils;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import org.scribe.model.OAuthConstants;
 import org.scribe.model.Token;
 import org.scribe.model.Verifier;
 import org.scribe.oauth.OAuthService;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
 
 @Singleton
 public class LinkedInAuthService implements AuthService {
 
     @Inject
     private SocialIdEntityDao socialIdEntityDao;
     private final OAuthService oAuthService;
 
     private final LoadingCache<String, Token> tokenCache;
 
     @Inject
     public LinkedInAuthService(OAuthUtils oAuthUtils) {
         oAuthService = oAuthUtils.getOauthService(OAuthType.LINKEDIN);
 
         tokenCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterAccess(3, TimeUnit.MINUTES)
                 .build(new CacheLoader<String, Token>() {
                     @Override
                     public Token load(String oauthCode) throws Exception {
                         return OAuthConstants.EMPTY_TOKEN;
                     }
                 });
     }
 
     @Override
     public SocialIdEntity getSocialIdEntity(Long userId) {
         SocialIdEntity socialId = socialIdEntityDao.getSocialdentity(userId, Service.LinkedIn);
         if (socialId != null) {
             socialId.setValid(true);
         }
         return socialId;
     }
 
     public String getAuthorizationUrl() {
         Token reqToken = oAuthService.getRequestToken();
         tokenCache.put(reqToken.getToken(), reqToken);
         return oAuthService.getAuthorizationUrl(reqToken);
     }
 
     public boolean updateAccessToken(String requestToken, String verifierCode, long userId)  {
         try {
             Verifier verifier = new Verifier(verifierCode);
             Token reqToken = tokenCache.get(requestToken);
             Token accessToken = oAuthService.getAccessToken(reqToken, verifier);
             if (accessToken.getToken() != null) {
                SocialIdEntity social = socialIdEntityDao.getSocialdentity(userId, Service.LinkedIn);
                 boolean newSocial = false;
                 if (social == null) {
                     social = new SocialIdEntity();
                     newSocial = true;
                 }
                 social.setUser_id(userId);
                 social.setToken(accessToken.getToken());
                 social.setService(Service.LinkedIn);
                 if (newSocial) {
                     socialIdEntityDao.save(social);
                 } else {
                     socialIdEntityDao.update(social);
                 }
 
                 return true;
             }
         } catch (ExecutionException e) {
             e.printStackTrace();
             return false;
         }
         return false;
     }
 }
