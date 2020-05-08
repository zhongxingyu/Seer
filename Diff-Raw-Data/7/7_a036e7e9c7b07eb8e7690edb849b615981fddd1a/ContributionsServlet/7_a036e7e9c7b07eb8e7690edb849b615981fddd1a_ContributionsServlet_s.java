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
 package org.jamwiki.servlets;
 
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to generate a list of all edits made by a user.
  */
 public class ContributionsServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(ContributionsServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output. */
 	protected static final String JSP_CONTRIBUTIONS = "contributions.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		this.contributions(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void contributions(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		String userString = WikiUtil.getParameterFromRequest(request, "contributor", false);
 		Pagination pagination = ServletUtil.loadPagination(request, next);
 		List<RecentChange> contributions = WikiBase.getDataHandler().getUserContributions(virtualWiki, userString, pagination, true);
 		next.addObject("contributions", contributions);
 		next.addObject("numContributions", contributions.size());
 		next.addObject("contributor", userString);
 		pageInfo.setPageTitle(new WikiMessage("contributions.title", userString));
 		pageInfo.setContentJsp(JSP_CONTRIBUTIONS);
 		pageInfo.setSpecial(true);
 	}
 }
