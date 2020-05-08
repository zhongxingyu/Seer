 /*******************************************************************************
  * Copyright (c) 2010 Earth System Grid Federation
  * ALL RIGHTS RESERVED. 
  * U.S. Government sponsorship acknowledged.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  * 
  * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package esg.orp.app;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.opensaml.saml2.core.Action;
 
 import esg.orp.Parameters;
 
 /**
  * Filter used to authorize an already authenticated user to access a given resource (for the given operation).
  * The actual authorization decision is delegated to the configured implementation of {@link AuthorizationServiceFilterCollaborator}.
  * This filter retrieves the user's identity from an authentication request scope attribute that must be previously set
  * by the {@link AuthenticationFilter}.
  */
 public class AuthorizationFilter extends AccessControlFilterTemplate {
 	
 	private AuthorizationServiceFilterCollaborator authorizationService;
 	
 	private final Log LOG = LogFactory.getLog(this.getClass());
 				
 	/**
 	 * {@inheritDoc}
 	 */
 	public void attemptValidation(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) 
 				throws IOException, ServletException {
 								
 		// proceed only if authentication cookie is found in request
 		final String openid = (String)req.getAttribute(Parameters.AUTHENTICATION_REQUEST_ATTRIBUTE);
 		if (openid!=null) {
 			
 			if (LOG.isDebugEnabled()) LOG.debug("Found authentication attribute, openid="+openid);				
 			final String url = transform(this.getUrl(req));
 			
 			final boolean authorized = authorizationService.authorize(openid, url, Action.READ_ACTION);
 			if (LOG.isDebugEnabled()) LOG.debug("Openid="+openid+" url="+url+" operation="+Action.READ_ACTION+" authorization result="+authorized);					
 			if (authorized) this.assertIsValid(req);
 							   		
 		
 		} // authentication cookie found
 			
 	}
 
 	private String transform(String url) {
         int c = url.indexOf('?');
         if (c > -1) {
             url = url.substring(0, c);
         }
         
        // temporary work around to enable authorization on opendap URLs
        url = url.replace("dodsC", "fileServer").replace(".ascii", "").replace(".dods", "");
         
         return url;
     }
 
     public void init(FilterConfig filterConfig) throws ServletException { 
 		
 		super.init(filterConfig); 
 		
 		// instantiate and initialize AuthorizationService
 		try {
 			final String authorizationServiceClass = this.getMandatoryFilterParameter(Parameters.AUTHORIZATION_SERVICE);
 			this.authorizationService = (AuthorizationServiceFilterCollaborator)Class.forName(authorizationServiceClass).newInstance();
 			this.authorizationService.init(filterConfig);
 		} catch(ClassNotFoundException e) {
 			throw new ServletException(e.getMessage());
 		} catch(InstantiationException e) {
 			throw new ServletException(e.getMessage());
 		} catch(IllegalAccessException e) {
 			throw new ServletException(e.getMessage());
 		}
 
 		
 	}
 	 
 
 }
