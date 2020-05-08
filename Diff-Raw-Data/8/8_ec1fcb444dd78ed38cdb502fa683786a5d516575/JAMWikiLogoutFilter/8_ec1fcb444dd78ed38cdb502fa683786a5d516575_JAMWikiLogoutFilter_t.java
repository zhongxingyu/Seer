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
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.acegisecurity.ui.logout.LogoutFilter;
 import org.acegisecurity.ui.logout.LogoutHandler;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 
 /**
  * This class is a hack implemented to work around the fact that the default
  * Acegi classes can only redirect to a single, hard-coded URL.  Due to the
  * fact that JAMWiki may have multiple virtual wikis this class overrides some
  * of the default Acegi behavior to allow additional flexibility.  Hopefully
  * future versions of Acegi will add additional flexibility and this class
  * can be removed.
  */
 public class JAMWikiLogoutFilter extends LogoutFilter {
 
 	/** Standard logger. */
 	private static WikiLogger logger = WikiLogger.getLogger(JAMWikiLogoutFilter.class.getName());
	// FIXME - this field can hopefully be removed in a future version of
	// Acegi, see the comments for setFilterProcessesUrl() below for details.
 	private String filterProcessesUrl = "/j_acegi_logout";
 
 	/**
 	 *
 	 */
 	public JAMWikiLogoutFilter(String logoutSuccessUrl, LogoutHandler[] handlers) {
 		super(logoutSuccessUrl, handlers);
 	}
 
 	/**
 	 * FIXME - override the parent method to determine if processing should
 	 * occur.  Needed due to the fact that different virtual wikis may be
 	 * used.
 	 */
 	protected boolean requiresLogout(HttpServletRequest request, HttpServletResponse response) {
 		String uri = request.getRequestURI();
 		int pathParamIndex = uri.indexOf(';');
 		if (pathParamIndex > 0) {
 			// strip everything after the first semi-colon
 			uri = uri.substring(0, pathParamIndex);
 		}
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		return uri.endsWith(request.getContextPath() + "/" + virtualWiki + filterProcessesUrl);
 	}
 
 	/**
 	 * Allow subclasses to modify the redirection message.
 	 *
 	 * @param request the request
 	 * @param response the response
 	 * @param url the URL to redirect to
 	 * @throws IOException in the event of any failure
 	 */
 	protected void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
 		if (url.equals("/DEFAULT_VIRTUAL_WIKI")) {
 			// ugly, but a hard-coded constant seems to be the only way to
 			// allow a dynamic url value
 			String virtualWikiName = Utilities.getVirtualWikiFromURI(request);
 			if (!StringUtils.hasText(virtualWikiName)) {
 				virtualWikiName = WikiBase.DEFAULT_VWIKI;
 			}
 			String topicName = Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC);
 			try {
 				VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
 				topicName = virtualWiki.getDefaultTopicName();
 			} catch (Exception e) {
 				logger.warning("Unable to retrieve default topic for virtual wiki", e);
 			}
 			url = request.getContextPath() + "/" + virtualWikiName + "/" + topicName;
 		} else if (!url.startsWith("http://") && !url.startsWith("https://")) {
 			String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 			url = request.getContextPath() + "/" + virtualWiki + url;
 		}
 		response.sendRedirect(response.encodeRedirectURL(url));
 	}
 
 	/**
	 * This method can hopefully be removed in a future version of Acegi -
	 * see http://opensource.atlassian.com/projects/spring/browse/SEC-408 for
	 * the issue report requesting that a getFilterProcessesUrl() method
	 * be added to LogoutFilter.
 	 */
 	public void setFilterProcessesUrl(String filterProcessesUrl) {
 		super.setFilterProcessesUrl(filterProcessesUrl);
 		this.filterProcessesUrl = filterProcessesUrl;
 	}
 }
