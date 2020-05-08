 package org.motechproject.ananya.kilkari.web.interceptors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Enumeration;
 
 public class HttpLogInterceptor extends HandlerInterceptorAdapter {
     private final static Logger LOG = LoggerFactory.getLogger(HttpLogInterceptor.class);
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         StringBuilder parameterBuilder = new StringBuilder();
 
         Enumeration<String> requestKeys = request.getParameterNames();
         while (requestKeys.hasMoreElements()) {
             String key = requestKeys.nextElement();
             parameterBuilder.append(String.format("%s=>%s,", key, request.getParameter(key)));
         }
 
        LOG.info("Request START [uri=%s | Parameters = {%s}]", request.getRequestURI(), parameterBuilder.toString());
         return true;
     }
 
     @Override
     public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
         LOG.info(String.format("Request END [uri=%s]", request.getRequestURI()));
     }
 }
