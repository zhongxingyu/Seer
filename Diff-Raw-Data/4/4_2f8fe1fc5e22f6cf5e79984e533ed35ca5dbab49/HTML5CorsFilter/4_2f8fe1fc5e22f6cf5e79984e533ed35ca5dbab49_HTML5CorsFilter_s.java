 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.evasion.cloud.service.security;
 
 import java.io.IOException;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author sgl
  */
 public class HTML5CorsFilter implements javax.servlet.Filter {
 
     private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HTML5CorsFilter.class);
     //private Set<String> whitelist = Sets.newHashSet("[AllowedOrigin1]", "[AllowedOrigin2]");
 
     @Override
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
 
         LOG.debug("HTML5CorsFilter add HTML5 CORS Headers");
         HttpServletRequest request = (HttpServletRequest) req;
         HttpServletResponse response = (HttpServletResponse) resp;
 
         if (request.getMethod().equals("OPTIONS")) {
             response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
             response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept");
             response.addHeader("Access-Control-Max-Age", "1800");
         }
 
         String origin = request.getHeader("Origin");
 
         //if (origin != null && whitelist.contains(origin)) {
             response.addHeader("Access-Control-Allow-Origin", origin);
             response.addHeader("Access-Control-Allow-Credentials", "true");
         //}
         chain.doFilter(request, response);
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
     }
 
     @Override
     public void destroy() {
     }
 }
