 package com.mmtzj.service;
 
 import org.apache.shiro.session.mgt.SessionKey;
 import org.apache.shiro.web.servlet.Cookie;
 import org.apache.shiro.web.servlet.SimpleCookie;
 import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
 import org.apache.shiro.web.util.WebUtils;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import java.io.Serializable;
 
 public class WebSessionManager extends DefaultWebSessionManager {
 
     public WebSessionManager() {
         super.setGlobalSessionTimeout(1000 * 3600 * 24 * 365 * 2);
         Cookie cookie = new SimpleCookie("MMTZJ_SESS_ID");
         cookie.setHttpOnly(true); // more secure, protects against XSS attacks
        cookie.setMaxAge(3600 * 24 * 365 *2);
 
         setSessionIdCookie(cookie);
         setSessionIdCookieEnabled(true);
 
     }
 
     @Override
     public Serializable getSessionId(SessionKey key) {
         Serializable id = super.getSessionId(key);
         if (id == null && WebUtils.isWeb(key)) {
             ServletRequest request = WebUtils.getRequest(key);
             HttpServletRequest req = (HttpServletRequest) request;
             String userAgent = req.getHeader("user-agent");
             if (isRobot(userAgent)) {
                 return "mmtzj:invalid:user-agent";
             }
             ServletResponse response = WebUtils.getResponse(key);
             id = getSessionId(request, response);
         }
         return id;
     }
 
     private boolean isRobot(String userAgent) {
         String[] kw_spiders = "bot,crawl,spider,slurp,sohu-search,lycos,robozilla".split(",");
         String[] kw_browsers = "msie,netscape,opera,konqueror,mozilla".split(",");
         if (userAgent == null || userAgent.length() == 0) {
             return true;
         }
         if (userAgent.length() < 25) {
             return true;
         }
 
         String ua = userAgent.toLowerCase();
         for (String kw : kw_spiders) {
             if (ua.contains(kw)) {
                 return true;
             }
         }
 
         for (String kw : kw_browsers) {
             if (ua.contains(kw)) {
                 return false;
             }
         }
         return true;
     }
 
 }
