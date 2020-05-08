 package com.britesnow.samplesocial.service;
 
 import com.britesnow.samplesocial.dao.SocialIdEntityDao;
 import com.britesnow.samplesocial.entity.Service;
 import com.britesnow.samplesocial.entity.SocialIdEntity;
 import com.britesnow.samplesocial.oauth.OAuthType;
 import com.britesnow.samplesocial.oauth.OAuthUtils;
 import com.britesnow.samplesocial.oauth.OauthException;
 import com.britesnow.samplesocial.oauth.OauthTokenExpireException;
 import com.britesnow.snow.util.JsonUtil;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import org.scribe.model.*;
 import org.scribe.oauth.OAuthService;
 
 import java.util.Date;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static org.scribe.model.OAuthConstants.EMPTY_TOKEN;
 
 
 @Singleton
 public class GoogleAuthService implements AuthService {
 
     @Inject
     private GoogleAuthService googleAuthService;
     @Inject
     private SocialIdEntityDao socialIdEntityDao;
     private final OAuthService oAuthService;
 
     @Inject
     public GoogleAuthService(OAuthUtils oAuthUtils) {
         oAuthService = oAuthUtils.getOauthService(OAuthType.GOOGLE);
     }
 
     @Override
     public SocialIdEntity getSocialIdEntity(Long userId) {
         SocialIdEntity socialId = socialIdEntityDao.getSocialdentity(userId, Service.Google);
         if (socialId != null) {
             if (socialId.getTokenDate().getTime() > System.currentTimeMillis()) {
                 socialId.setValid(true);
             } else {
                 throw new OauthTokenExpireException(getAuthorizationUrl());
             }
             return socialId;
         }
         //if result is null, need redo auth
         throw new OauthException(getAuthorizationUrl());
     }
 
     public String getAuthorizationUrl() {
         return oAuthService.getAuthorizationUrl(EMPTY_TOKEN);
     }
 
     public boolean updateAccessToken(String verifierCode, long userId) {
         Verifier verifier = new Verifier(verifierCode);
         Token accessToken = oAuthService.getAccessToken(EMPTY_TOKEN, verifier);
         if (accessToken.getToken() != null) {
             //get expire date
             String rawResponse = accessToken.getRawResponse();
             Pattern expire = Pattern.compile("\"expires_in\"\\s*:\\s*(\\d+)");
             Matcher matcher = expire.matcher(rawResponse);
             long expireDate = -1;
             if (matcher.find()) {
                 expireDate = System.currentTimeMillis() + (Integer.valueOf(matcher.group(1)) - 100) * 1000;
             }
             //get userinfo
             OAuthRequest request = new OAuthRequest(Verb.GET, OAuthUtils.PROFILE_ENDPOINT);
             oAuthService.signRequest(accessToken, request);
             Response response = request.send();
             System.out.println(response.getBody());
             Map profile = JsonUtil.toMapAndList(response.getBody());
             System.out.println(profile);
             //todo extract userinfo
            SocialIdEntity social = socialIdEntityDao.getSocialdentity(userId, Service.Google);
             boolean newSocial = false;
             if (social == null) {
                 social = new SocialIdEntity();
                 newSocial = true;
             }
             social.setEmail((String) profile.get("email"));
             social.setUser_id(userId);
             social.setToken(accessToken.getToken());
             social.setService(Service.Google);
             social.setTokenDate(new Date(expireDate));
             if (newSocial) {
                 socialIdEntityDao.save(social);
             } else {
                 socialIdEntityDao.update(social);
             }
             return true;
         }
         throw new OauthException(getAuthorizationUrl());
 
     }
 }
