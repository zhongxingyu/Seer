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
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.security.AccessDeniedException;
 import org.springframework.security.AuthenticationException;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.security.SpringSecurityException;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 
 /**
  * This class provides an additional filter that is added to the Spring Security
  * configuration for adding messages to the session about why a login is
  * required.
  */
 public class JAMWikiExceptionTranslationFilter implements Filter, InitializingBean {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiExceptionTranslationFilter.class.getName());
 	private String errorPage;
 	private JAMWikiErrorMessageProvider errorMessageProvider;
 
 	/**
 	 *
 	 */
 	public void afterPropertiesSet() throws Exception {
 		if (errorMessageProvider == null) {
 			throw new IllegalArgumentException("errorMessageProvider must be specified");
 		}
     }
 
 	/**
 	 *
 	 */
 	public void destroy() {
 	}
 
 	/**
 	 *
 	 */
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		if (!(request instanceof HttpServletRequest)) {
 			throw new ServletException("HttpServletRequest required");
 		}
 		try {
 			chain.doFilter(request, response);
 		} catch (SpringSecurityException ex) {
 			handleException(request, response, ex);
 			throw ex;
 		} catch (ServletException ex) {
 			if (ex.getRootCause() instanceof SpringSecurityException) {
 				handleException(request, response, (SpringSecurityException)ex.getRootCause());
 			}
 			throw ex;
 		}
 	}
 
 	/**
 	 *
 	 */
 	public JAMWikiErrorMessageProvider getErrorMessageProvider() {
 		return this.errorMessageProvider;
 	}
 
 	/**
 	 *
 	 */
 	private void handleException(ServletRequest servletRequest, ServletResponse servletResponse, SpringSecurityException exception) throws IOException, ServletException {
 		HttpServletRequest request = (HttpServletRequest)servletRequest;
 		HttpServletResponse response = (HttpServletResponse)servletResponse;
 		if (exception instanceof AccessDeniedException) {
 			request.getSession().setAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_ERROR_KEY, this.getErrorMessageProvider().getErrorMessageKey(request));
 			request.getSession().setAttribute(JAMWikiAuthenticationConstants.JAMWIKI_ACCESS_DENIED_URI_KEY, WikiUtil.getTopicFromURI(request));
 			this.handleAccessDenied(request, response, (AccessDeniedException)exception);
 		} else if (exception instanceof AuthenticationException) {
 			request.getSession().setAttribute(JAMWikiAuthenticationConstants.JAMWIKI_AUTHENTICATION_REQUIRED_KEY, this.getErrorMessageProvider().getErrorMessageKey(request));
 			request.getSession().setAttribute(JAMWikiAuthenticationConstants.JAMWIKI_AUTHENTICATION_REQUIRED_URI_KEY, WikiUtil.getTopicFromURI(request));
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void handleAccessDenied(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
 		if (this.errorPage != null) {
 			String virtualWiki = WikiUtil.getVirtualWikiFromURI(request);
 			RequestDispatcher rd = request.getRequestDispatcher("/" + virtualWiki + this.errorPage);
 			rd.forward(request, response);
 		}
		if (!response.isCommitted()) {
			// send 403 after response has been written
			response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
		}
 	}
 
 	/**
 	 *
 	 */
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 
 	/**
 	 * The error page to use. Must begin with a "/" and is interpreted relative to
 	 * the current context root.
 	 *
 	 * @param errorPage the dispatcher path to display
 	 *
 	 * @throws IllegalArgumentException if the argument doesn't comply with the above
 	 *  limitations
 	 */
 	public void setErrorPage(String errorPage) {
 		if (errorPage != null && !errorPage.startsWith("/")) {
 			throw new IllegalArgumentException("ErrorPage must begin with '/'");
 		}
 		this.errorPage = errorPage;
 	}
 
 	/**
 	 *
 	 */
 	public void setErrorMessageProvider(JAMWikiErrorMessageProvider errorMessageProvider) {
 		this.errorMessageProvider = errorMessageProvider;
 	}
 }
