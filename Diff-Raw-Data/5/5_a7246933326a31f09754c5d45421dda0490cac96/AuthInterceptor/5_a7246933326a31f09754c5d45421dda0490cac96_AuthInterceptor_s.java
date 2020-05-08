 package com.directi.train.tweetapp.interceptor;
 
 import com.directi.train.tweetapp.services.AuthStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class AuthInterceptor extends HandlerInterceptorAdapter {
     private AuthStore authStore;
 
     @Autowired
     public AuthInterceptor(AuthStore authStore) {
         this.authStore = authStore;
     }
 
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
             throws Exception {
 
         String cookieName = "accesstoken";
 
         if (request.getParameter(cookieName) != null) {
             String accessToken = request.getParameter(cookieName);
             request.setAttribute(cookieName, accessToken);
             Boolean flag = authStore.isValid(accessToken);
             if (flag) {
                request.setAttribute("userName",authStore.getUserName(accessToken));
                 return flag;
             }
         }
 
         Cookie[] cookies = request.getCookies();
         if (cookies != null) {
             for (Cookie cookie : cookies) {
                 if (cookie.getName().equals(cookieName)) {
                     String accessToken = cookie.getValue();
                     request.setAttribute(cookieName, accessToken);
                     Boolean flag = authStore.isValid(accessToken);
                     if (flag) {
                        request.setAttribute("userName",authStore.getUserName(accessToken));
                         return flag;
                     }
                 }
             }
         }
 
         response.sendRedirect("/");
         return false;
     }
 }
