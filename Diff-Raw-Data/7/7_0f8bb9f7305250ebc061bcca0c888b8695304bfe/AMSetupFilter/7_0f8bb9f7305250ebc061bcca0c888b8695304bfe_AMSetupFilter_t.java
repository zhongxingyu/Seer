 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AMSetupFilter.java,v 1.5 2008-01-31 22:56:40 jonnelson Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.setup;
 
 import com.sun.identity.authentication.UI.LoginLogoutMapping;
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * This filter brings administrator to a configuration page
  * where the product can be configured if the product is not
  * yet configured.
 */
 public final class AMSetupFilter implements Filter {
     private FilterConfig config;
     private ServletContext servletCtx;
     private boolean initialized;
     private boolean passthrough;
     private static final String SETUPURI = "/configurator.jsp";
 
     private static String[] fList = { ".css", ".js", ".jpg", ".gif", ".png", 
         "SMSObjectIF" }; 
 
     /**
      * Redirects request to configuration page if the product is not yet 
      * configured.
      *
      * @param request Servlet Request.
      * @param response Servlet Response.
      * @param filterChain Filter Chain.
      * @throws IOException if configuration file cannot be read.
      * @throws ServletException if there are errors in the servlet space.
      */
     public void doFilter(
         ServletRequest request, 
         ServletResponse response, 
         FilterChain filterChain
     ) throws IOException, ServletException 
     {
         HttpServletRequest  httpRequest = (HttpServletRequest) request;
         HttpServletResponse httpResponse = (HttpServletResponse) response ;
         try {
             //Check to see if AM is configured 
            if (AMSetupServlet.isConfigured() ||
                httpRequest.getRequestURI().endsWith(".htm")) 
            {
                 filterChain.doFilter(httpRequest, httpResponse);
             } else {
                 if (isPassthrough() && validateStream(httpRequest)) {
                     filterChain.doFilter(httpRequest, httpResponse);
                 } else {
                     String url = httpRequest.getScheme() + "://" +
                          httpRequest.getServerName() + ":" +
                          httpRequest.getServerPort() +
                          httpRequest.getContextPath() + SETUPURI;
                     httpResponse.sendRedirect(url);
                     markPassthrough();
                 }
             }
         } catch(Exception ex) {
             ex.printStackTrace();
             throw new ServletException("AMSetupFilter.doFilter", ex);
         }
     }
 
     /**
      * Returns <code>true</code> if the request for resources.
      *
      * @param httpRequest HTTP Servlet request.
      * @return <code>true</code> if the request for resources.
      */
     private boolean validateStream(HttpServletRequest httpRequest) {
         String uri =  httpRequest.getRequestURI();
         boolean ok = false;
         for (int i = 0; (i < fList.length) && !ok; i++) {
             ok = (uri.indexOf(fList[i]) != -1);
         }
         return ok;     
     }
 
     /**
      * Destroy the filter config on sever shutdowm 
      */
     public void destroy() {
         config = null;
     }
     
     /**
      * Initializes the filter.
      *
      * @param filterConfig Filter Configuration.
      */
     public void init(FilterConfig filterConfig) {
         setFilterConfig(filterConfig);
         servletCtx = filterConfig.getServletContext();
         initialized = AMSetupServlet.checkInitState(servletCtx); 
         if (!initialized) {
             //Set the encryption Key
             servletCtx.setAttribute("am.enc.pwd",
                 AMSetupServlet.getRandomString());
         }
     }
     
     /**
      * Initializes the filter configuration.
      *
      * @param fconfig Filter Configuration.
      */
     public void setFilterConfig(FilterConfig fconfig) {
         config = fconfig;
     }
     
     /**
      * Returns <code>true</code> if the request is allowed without processing.
      *
      * @return <code>true</code> if the request is allowed without processing.
      */
     private boolean isPassthrough() {
         return passthrough;
     }
 
     /**
      * Sets the request for images such that they are not processed.
      */
     private void markPassthrough() {
         passthrough = true;
     }
 }
