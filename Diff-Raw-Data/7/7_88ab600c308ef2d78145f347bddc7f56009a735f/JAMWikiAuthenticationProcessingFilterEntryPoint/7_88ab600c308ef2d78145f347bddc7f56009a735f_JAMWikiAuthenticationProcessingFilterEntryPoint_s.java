 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.authentication;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.security.AuthenticationException;
 import org.springframework.security.ui.webapp.AuthenticationProcessingFilterEntryPoint;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 
 /**
  * This class is a hack implemented to work around the fact that the default
  * Spring Security classes can only redirect to a single, hard-coded URL.  Due to the
  * fact that JAMWiki may have multiple virtual wikis this class overrides some
  * of the default Spring Security behavior to allow additional flexibility.  Hopefully
  * future versions of Spring Security will add additional flexibility and this class
  * can be removed.
  */
 public class JAMWikiAuthenticationProcessingFilterEntryPoint extends AuthenticationProcessingFilterEntryPoint {
 
 	/** Standard logger. */
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAuthenticationProcessingFilterEntryPoint.class.getName());
 
 	/**
 	 * Return the URL to redirect to in case of a login being required.  This method
 	 * uses the configured login URL and prepends the virtual wiki.
 	 */
 	protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
		String uri = request.getRequestURI();
		// FIXME - move the "strip after semicolon" code to WikiUtil
		int pathParamIndex = uri.indexOf(';');
		if (pathParamIndex > 0) {
			// strip everything after the first semi-colon
			uri = uri.substring(0, pathParamIndex);
		}
 		String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 		return "/" + virtualWiki + this.getLoginFormUrl();
 	}
 }
