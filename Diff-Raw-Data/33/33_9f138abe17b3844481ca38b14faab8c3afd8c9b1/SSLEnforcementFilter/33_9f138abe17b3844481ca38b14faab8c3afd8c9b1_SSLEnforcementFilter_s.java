 /**
  * Copyright (c) 2009 - 2013 By: CWS, Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cws.esolutions.security.filters;
 
 import java.util.Collections;
 import java.util.Set;
 import java.util.Arrays;
 import org.slf4j.Logger;
 import java.util.HashSet;
 import java.io.IOException;
 import javax.servlet.Filter;
 import java.util.Enumeration;
 import org.slf4j.LoggerFactory;
 import java.util.ResourceBundle;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.UnavailableException;
 import java.util.MissingResourceException;
 import org.apache.commons.lang.StringUtils;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.cws.esolutions.security.SecurityConstants;
 /*
  * SSLEnforcementFilter
  * Provides consistent SSL enforcement within the application
  * where required.
  *
  * History
  *
  * Author               Date                           Comments
  * ----------------------------------------------------------------------------
  * Kevin Huntly         11/23/2008 22:39:20            Created.
  */
 public class SSLEnforcementFilter implements Filter
 {
     private String[] ignoreURIs = null;
     private String[] ignoreHosts = null;
 
     private static final int SECURE_URL_PORT = 443;
     private static final String SECURE_URL_PREFIX = "https://";
     private static final String IGNORE_URI_LIST = "ignore.uri.list";
     private static final String IGNORE_HOST_LIST = "ignore.host.list";
     private static final String HTTP_DECRYPTED_HEADER = "HTTP:DECRYPTED";
     private static final String FILTER_CONFIG_PARAM_NAME = "filter-config";
     private static final String CNAME = SSLEnforcementFilter.class.getName();
     private static final String FILTER_CONFIG_FILE_NAME = "config/FilterConfig";
     private static final Set<String> LOCALHOST = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("localhost", "127.0.0.1")));
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(SecurityConstants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger WARN_RECORDER = LoggerFactory.getLogger(SecurityConstants.WARN_LOGGER + CNAME);
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(SecurityConstants.ERROR_LOGGER + CNAME);
 
     @Override
     public void init(final FilterConfig filterConfig) throws ServletException
     {
         final String methodName = SSLEnforcementFilter.CNAME + "#init(FilterConfig filterConfig) throws ServletException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("FilterConfig: {}", filterConfig);
         }
 
         ResourceBundle rBundle = null;
 
         try
         {
             if (filterConfig.getInitParameter(SSLEnforcementFilter.FILTER_CONFIG_PARAM_NAME) == null)
             {
                 WARN_RECORDER.warn("Filter configuration not found. Using default !");
 
                 rBundle = ResourceBundle.getBundle(SSLEnforcementFilter.FILTER_CONFIG_FILE_NAME);
             }
             else
             {
                 rBundle = ResourceBundle.getBundle(filterConfig.getInitParameter(SSLEnforcementFilter.FILTER_CONFIG_PARAM_NAME));
             }
 
             this.ignoreHosts = (
                     StringUtils.isNotEmpty(rBundle.getString(SSLEnforcementFilter.IGNORE_HOST_LIST))) ?
                             rBundle.getString(SSLEnforcementFilter.IGNORE_HOST_LIST).trim().split(","): null;
             this.ignoreURIs = (
                     StringUtils.isNotEmpty(rBundle.getString(SSLEnforcementFilter.IGNORE_URI_LIST))) ?
                             rBundle.getString(SSLEnforcementFilter.IGNORE_HOST_LIST).trim().split(","): null;
 
             if (DEBUG)
             {
                 if (this.ignoreHosts != null)
                 {
                     for (String str : this.ignoreHosts)
                     {
                         DEBUGGER.debug(str);
                     }
                 }
 
                 if (this.ignoreURIs != null)
                 {
                     for (String str : this.ignoreURIs)
                     {
                         DEBUGGER.debug(str);
                     }
                 }
             }
         }
         catch (MissingResourceException mre)
         {
             ERROR_RECORDER.error(mre.getMessage(), mre);
 
             throw new UnavailableException(mre.getMessage());
         }
     }
 
     @Override
     public void doFilter(final ServletRequest sRequest, final ServletResponse sResponse, final FilterChain filterChain) throws ServletException, IOException
     {
         final String methodName = SSLEnforcementFilter.CNAME + "#doFilter(final ServletRequest req, final servletResponse res, final FilterChain filterChain) throws ServletException, IOException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("ServletRequest: {}", sRequest);
             DEBUGGER.debug("ServletResponse: {}", sResponse);
             DEBUGGER.debug("FilterChain: {}", filterChain);
         }
 
         final HttpServletRequest hRequest = (HttpServletRequest) sRequest;
         final HttpServletResponse hResponse = (HttpServletResponse) sResponse;
         final HttpSession hSession = hRequest.getSession();
 
         if (DEBUG)
         {
             DEBUGGER.debug("HttpServletRequest: {}", hRequest);
             DEBUGGER.debug("HttpServletResponse: {}", hResponse);
             DEBUGGER.debug("HttpSession: {}", hSession);
 
             DEBUGGER.debug("Dumping session content:");

            DEBUGGER.debug("Dumping session content:");
             Enumeration<String> sessionEnumeration = hSession.getAttributeNames();
 
             while (sessionEnumeration.hasMoreElements())
             {
                 String sessionElement = sessionEnumeration.nextElement();
                 Object sessionValue = hSession.getAttribute(sessionElement);
 
                 DEBUGGER.debug("Attribute: " + sessionElement + "; Value: " + sessionValue);
             }
 
             DEBUGGER.debug("Dumping request content:");
             Enumeration<String> requestEnumeration = hRequest.getAttributeNames();
 
             while (requestEnumeration.hasMoreElements())
             {
                 String requestElement = requestEnumeration.nextElement();
                 Object requestValue = hRequest.getAttribute(requestElement);
 
                 DEBUGGER.debug("Attribute: " + requestElement + "; Value: " + requestValue);
             }
 
             DEBUGGER.debug("Dumping request parameters:");
             Enumeration<String> paramsEnumeration = hRequest.getParameterNames();
 
             while (paramsEnumeration.hasMoreElements())
             {
                 String requestElement = paramsEnumeration.nextElement();
                 Object requestValue = hRequest.getParameter(requestElement);
 
                 DEBUGGER.debug("Parameter: " + requestElement + "; Value: " + requestValue);
             }
         }
 
         try
         {
             if (SSLEnforcementFilter.LOCALHOST.contains(sRequest.getServerName()))
             {
                 filterChain.doFilter(sRequest, sResponse);
 
                 return;
             }
             else
             {
                 if ((this.ignoreHosts != null) && (this.ignoreHosts.length != 0))
                 {
                     for (String host : this.ignoreHosts)
                     {
                         String requestHost = host.trim();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug(host);
                             DEBUGGER.debug(requestHost);
                         }
     
                         if (StringUtils.equals(requestHost, sRequest.getServerName().trim()))
                         {
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("Host found in ignore list. Not processing request!");
                             }
 
                             filterChain.doFilter(sRequest, sResponse);
     
                             return;
                         }
                     }
                 }
 
                 if ((this.ignoreURIs != null) && (this.ignoreURIs.length != 0))
                 {
                     // no hosts in ignore list
                     for (String uri : this.ignoreURIs)
                     {
                         String requestURI = uri.trim();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug(uri);
                             DEBUGGER.debug(requestURI);
                         }
     
                         if (StringUtils.equals(requestURI, hRequest.getRequestURI().trim()))
                         {
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("URI found in ignore list. Not processing request!");
                             }
 
                             filterChain.doFilter(sRequest, sResponse);
     
                             return;
                         }
                     }
                 }
             }
 
             if ((hRequest.isSecure()) || (StringUtils.equals(SecurityConstants.TRUE,
                         hRequest.getHeader(SSLEnforcementFilter.HTTP_DECRYPTED_HEADER))))
             {
                 // Request came in on a secure channel or
                 // the HTTP:DECRYPTED header is true
                 // do nothing
                 if (DEBUG)
                 {
                     DEBUGGER.debug("Filter not applied to request - already secured. No action taken.");
                 }
     
                 filterChain.doFilter(sRequest, sResponse);
             }
             else
             {
                 // secure it
                 StringBuilder redirectURL = new StringBuilder()
                     .append(SSLEnforcementFilter.SECURE_URL_PREFIX)
                     .append(sRequest.getServerName())
                     .append((sRequest.getServerPort() != SSLEnforcementFilter.SECURE_URL_PORT) ? ":" + sRequest.getServerPort() : null)
                     .append(hRequest.getRequestURI())
                     .append(StringUtils.isNotBlank(hRequest.getQueryString()) ? "?" + hRequest.getQueryString() : null);
     
                 if (DEBUG)
                 {
                     DEBUGGER.debug("redirectURL: {}", redirectURL);
                 }
     
                 hResponse.sendRedirect(redirectURL.toString());
             }
         }
         catch (ServletException sx)
         {
             ERROR_RECORDER.error(sx.getMessage(), sx);
 
             filterChain.doFilter(sRequest, sResponse);
         }
         catch (IOException iox)
         {
             ERROR_RECORDER.error(iox.getMessage(), iox);
 
             filterChain.doFilter(sRequest, sResponse);
         }
     }
 
     @Override
     public void destroy()
     {
         final String methodName = SSLEnforcementFilter.CNAME + "#destroy()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         this.ignoreHosts = null;
         this.ignoreURIs = null;
     }
 }
