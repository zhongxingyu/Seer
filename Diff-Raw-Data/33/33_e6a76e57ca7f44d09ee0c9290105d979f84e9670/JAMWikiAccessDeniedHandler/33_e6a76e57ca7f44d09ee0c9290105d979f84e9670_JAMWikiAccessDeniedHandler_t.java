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
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.acegisecurity.AccessDeniedException;
 import org.acegisecurity.ui.AccessDeniedHandler;
 import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;
 
 /**
  * Handle AccessDeniedExceptions thrown by the Acegi security framework.  This
  * class is based on the org.acegisecurity.ui.AccessDeniedHandler class.
  */
 public class JAMWikiAccessDeniedHandler implements AccessDeniedHandler {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiAccessDeniedHandler.class.getName());
 	private String errorPage;
 
 	/**
 	 *
 	 */
 	public void handle(ServletRequest servletRequest, ServletResponse servletResponse, AccessDeniedException accessDeniedException) throws IOException, ServletException {
 		HttpServletRequest request = (HttpServletRequest)servletRequest;
 		HttpServletResponse response = (HttpServletResponse)servletResponse;
 		if (this.errorPage != null) {
			String uri = request.getRequestURI();
			// FIXME - move the "strip after semicolon" code to WikiUtil
			int pathParamIndex = uri.indexOf(';');
			if (pathParamIndex > 0) {
				// strip everything after the first semi-colon
				uri = uri.substring(0, pathParamIndex);
			}
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
 }
