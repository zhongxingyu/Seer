 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.aa.shibboleth;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.web.AuthenticationEntryPoint;
 
 import de.escidoc.core.common.util.xml.XmlUtility;
 
 public class ShibbolethAuthenticationEntryPoint implements AuthenticationEntryPoint {
 
     private String serviceProviderBaseUrl;
 
     private String sessionInitiatorPath;
 
     @Override
     public void commence(
         final HttpServletRequest request, final HttpServletResponse response,
         final AuthenticationException authException) throws IOException, ServletException, UnsupportedEncodingException {
         final HttpServletRequest httpRequest = (HttpServletRequest) request;
         // FIXME:URL!!!
         final StringBuilder target = new StringBuilder(this.serviceProviderBaseUrl).append("aa/login");
 
         final String queryString = httpRequest.getQueryString();
         if (queryString != null) {
             target.append('?');
             target.append(queryString);
         }
         final String redirectUrl =
             httpRequest.getHeader(ShibbolethDetails.SHIB_SESSION_ID) == null ? target.toString() : this.serviceProviderBaseUrl
                 + this.sessionInitiatorPath
                 + "?target="
                 + URLEncoder.encode(target.toString(), XmlUtility.CHARACTER_ENCODING);
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).sendRedirect(redirectUrl);
        }
     }
 
     /**
      * Injects the base url of the service provider.
      *
      * @param serviceProviderBaseUrl The serviceProviderBaseUrl to inject.
      */
     public void setServiceProviderBaseUrl(final String serviceProviderBaseUrl) {
 
         this.serviceProviderBaseUrl =
             serviceProviderBaseUrl.endsWith("/") ? serviceProviderBaseUrl : serviceProviderBaseUrl + '/';
     }
 
     /**
      * Injects the path to the session initiator. This path is relative to the service provider base URL.
      *
      * @param sessionInitiatorPath The sessionInitiatorPath to inject.
      */
     public void setSessionInitiatorPath(final String sessionInitiatorPath) {
 
         this.sessionInitiatorPath =
             sessionInitiatorPath.startsWith("/") ? sessionInitiatorPath.substring(1) : sessionInitiatorPath;
     }
 
 }
