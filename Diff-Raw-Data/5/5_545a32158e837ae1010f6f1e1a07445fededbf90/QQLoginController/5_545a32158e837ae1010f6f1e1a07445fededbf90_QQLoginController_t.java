 package com.cqlybest.common.controller;
 
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.cqlybest.common.bean.QQAccessToken;
 import com.cqlybest.common.bean.QQAuthToken;
 import com.cqlybest.common.bean.QQUser;
 import com.cqlybest.common.bean.User;
 import com.cqlybest.common.service.QQConnectInitService;
 import com.cqlybest.common.service.UserService;
 import com.qq.connect.QQConnectException;
 import com.qq.connect.api.OpenID;
 import com.qq.connect.api.qzone.UserInfo;
 import com.qq.connect.javabeans.AccessToken;
 import com.qq.connect.oauth.Oauth;
 import com.qq.connect.utils.QQConnectConfig;
 
 @Controller
 public class QQLoginController {
 
   private static final Oauth OAUTH = new Oauth();
 
   @Autowired
   private UserService userService;
 
   @RequestMapping("/connector/qq_login")
   public String qqLogin(HttpServletRequest request) {
    String redirect = "redirect:/redirect";
     try {
       String redirectURI =
           request.getScheme() + "://" + request.getServerName() + request.getContextPath()
               + "/connector/qq";
       QQConnectConfig.updateProperties(QQConnectInitService.REDIRECT_URI, redirectURI);
       redirect = "redirect:" + OAUTH.getAuthorizeURL(request);
     } catch (QQConnectException e) {
       e.printStackTrace();
     }
 
     return redirect;
   }
 
   @RequestMapping("/connector/qq")
   public String qq(HttpServletRequest request) {
    String redirect = "redirect:/redirect";
     try {
       AccessToken accessToken = OAUTH.getAccessTokenByRequest(request);
       String id = new OpenID(accessToken.getAccessToken()).getUserOpenID();
       QQUser qqUser = new QQUser();
       qqUser.setId(id);
       QQAccessToken token = new QQAccessToken();
       // token.setAppId(appId);
       token.setToken(accessToken.getAccessToken());
       token.setExpireIn(accessToken.getExpireIn());
       Date current = new Date();
       token.setCreatedTime(current);
       token.setLastUpdated(current);
       qqUser.getTokens().add(token);
 
       User user =
           userService
               .register(qqUser, new UserInfo(accessToken.getAccessToken(), id).getUserInfo());
       SecurityContextHolder.getContext().setAuthentication(new QQAuthToken(user));
     } catch (QQConnectException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     return redirect;
   }
 }
