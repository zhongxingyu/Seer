 package com.cqlybest.common.controller;
 
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import weibo4j.Friendships;
 import weibo4j.Oauth;
 import weibo4j.Users;
 import weibo4j.http.AccessToken;
 import weibo4j.model.WeiboException;
 import weibo4j.util.WeiboConfig;
 
 import com.cqlybest.common.Constant;
 import com.cqlybest.common.bean.User;
 import com.cqlybest.common.bean.WeiboAccessToken;
 import com.cqlybest.common.bean.WeiboAuthToken;
 import com.cqlybest.common.bean.WeiboUser;
 import com.cqlybest.common.service.CentralConfig;
 import com.cqlybest.common.service.SettingsService;
 import com.cqlybest.common.service.UserService;
 
 @Controller
 public class WeiboLoginController {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(WeiboLoginController.class);
   private static final Oauth WEIBO_OAUTH = new Oauth();
 
   @Autowired
   private UserService userService;
   @Autowired
   private SettingsService settingsService;
   @Autowired
   private CentralConfig centralConfig;
 
   @RequestMapping("/connector/weibo_login")
   public String weiboLogin(HttpServletRequest request) {
    String redirect = "redirect:/redirect";
     try {
       String redirectURI =
           request.getScheme() + "://" + request.getServerName() + request.getContextPath()
               + "/connector/weibo";
       WeiboConfig.updateProperties(Constant.CLIENT_ID, centralConfig
           .get(CentralConfig.WEIBO_APP_KEY));
       WeiboConfig.updateProperties(Constant.CLIENT_SECRET, centralConfig
           .get(CentralConfig.WEIBO_APP_SECRET));
       WeiboConfig.updateProperties(Constant.REDIRECT_URI, redirectURI);
       redirect =
           "redirect:" + WEIBO_OAUTH.authorize(Constant.RESPONSE_TYPE_CODE, Constant.SCOPE_ALL);
     } catch (WeiboException e) {
       e.printStackTrace();
     }
 
     return redirect;
   }
 
   @RequestMapping("/connector/weibo")
   public String weibo(@RequestParam String state, @RequestParam String code,
       HttpServletRequest request) {
    String redirect = "redirect:/redirect";
     try {
       AccessToken accessToken = WEIBO_OAUTH.getAccessTokenByCode(code);
       WeiboUser weiboUser = new WeiboUser();
       weiboUser.setId(accessToken.getUid());
       WeiboAccessToken token = new WeiboAccessToken();
       // token.setAppKey(appKey);
       // token.setAppId(appId);
       // token.setCid(cid);
       // token.setSubAppkey(subAppkey);
       token.setToken(accessToken.getAccessToken());
       token.setExpireIn(Long.valueOf(accessToken.getExpireIn()));
       Date current = new Date();
       token.setCreatedTime(current);
       token.setLastUpdated(current);
       weiboUser.getTokens().add(token);
 
       // 读取用户详细信息
       Users api = new Users();
       api.setToken(accessToken.getAccessToken());
 
 
       User user = userService.register(weiboUser, api.showUserById(accessToken.getUid()));
       SecurityContextHolder.getContext().setAuthentication(new WeiboAuthToken(user));
 
       // 关注官方微博
       String official =
           (String) ((Map<?, ?>) ((Map<?, ?>) settingsService.getSettings().get("basic"))
               .get("weibo")).get("id");
       try {
         if (StringUtils.isNotBlank(official)) {
           Friendships friendships = new Friendships();
           friendships.setToken(accessToken.getAccessToken());
           friendships.createFriendshipsByName(official);
           LOGGER.info("{} followed {}.", user.getNickname(), official);
         }
       } catch (WeiboException e) {
         LOGGER.warn("{} follow {} failed: {}", user.getNickname(), official, e.getMessage());
         e.printStackTrace();
       }
     } catch (WeiboException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     return redirect;
   }
 
 }
