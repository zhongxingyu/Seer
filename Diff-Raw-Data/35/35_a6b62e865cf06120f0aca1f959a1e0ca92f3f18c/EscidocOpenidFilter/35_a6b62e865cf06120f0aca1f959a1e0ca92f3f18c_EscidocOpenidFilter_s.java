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
 package de.escidoc.core.aa.openid;
 
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.springframework.security.authentication.ProviderNotFoundException;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.openid.OpenIDAuthenticationFilter;
 
 /**
  * @author MIH
  */
 public class EscidocOpenidFilter extends OpenIDAuthenticationFilter {
 
     private Pattern openidProviderPattern = null;
 
     private String openidProviderRegex = null;
 
     /**
     * Authentication has two phases. <ol> <li>The initial submission of the claimed OpenID. A redirect to the URL
     * returned from the consumer will be performed and null will be returned.</li> <li>The redirection from the OpenID
     * server to the return_to URL, once it has authenticated the user</li> </ol>
      */
     @Override
     public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
         throws AuthenticationException, IOException {
         String openidIdentifier = request.getParameter(DEFAULT_CLAIMED_IDENTITY_FIELD);
         if (openidIdentifier != null && openidProviderPattern != null
             && !openidProviderPattern.matcher(openidIdentifier).find()) {
            throw new ProviderNotFoundException("specified openId-provider is not supported");
         }
         return super.attemptAuthentication(request, response);
     }
 
     /**
      * Sets the openid-provider-regex
     *
     * @param openidProviderRegex openidProviderRegex
      */
     public void setOpenidProviderRegex(String openidProviderRegex) {
         this.openidProviderRegex = openidProviderRegex;
         if (openidProviderRegex == null || openidProviderRegex.equals("")) {
             openidProviderRegex = null;
         }
         else {
             openidProviderPattern = Pattern.compile(openidProviderRegex);
         }
     }
 
 }
