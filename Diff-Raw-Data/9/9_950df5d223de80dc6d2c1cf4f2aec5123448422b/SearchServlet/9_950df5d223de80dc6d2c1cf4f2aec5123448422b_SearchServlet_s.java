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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiConfiguration;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.SearchResultEntry;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to display search results.
  *
  * @see org.jamwiki.SearchEngine
  */
 public class SearchServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(SearchServlet.class.getName());
 	/** The name of the JSP file used to render the servlet output when searching. */
 	protected static final String JSP_SEARCH = "search.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (request.getParameter("jumpto") == null) {
 			search(request, next, pageInfo);
 		} else {
 			jumpTo(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void jumpTo(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
		String topic = request.getParameter("text");
 		String targetTopic = LinkUtil.isExistingArticle(virtualWiki, topic);
 		if (targetTopic != null) {
 			ServletUtil.redirect(next, virtualWiki, targetTopic);
 		} else {
 			next.addObject("notopic", topic);
 			this.search(request, next, pageInfo);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void search(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = pageInfo.getVirtualWikiName();
		String searchField = request.getParameter("text");
		if (StringUtils.isBlank(request.getParameter("text"))) {
 			pageInfo.setPageTitle(new WikiMessage("search.title"));
 		} else {
 			pageInfo.setPageTitle(new WikiMessage("searchresult.title", searchField));
 		}
 		next.addObject("searchConfig", WikiConfiguration.getCurrentSearchConfiguration());
 		// add a map of namespace id & label for display on the front end.
 		Map<Integer, String> namespaceMap = ServletUtil.loadNamespaceDisplayMap(virtualWiki, ServletUtil.retrieveUserLocale(request));
 		next.addObject("namespaces", namespaceMap);
 		List<Integer> selectedNamespaces = null;
 		if (request.getParameter("ns") != null) {
 			Map<Integer, Integer> selectedNamespaceMap = new TreeMap<Integer, Integer>();
 			selectedNamespaces = new ArrayList<Integer>();
 			for (String namespaceId : request.getParameterValues("ns")) {
 				selectedNamespaces.add(NumberUtils.toInt(namespaceId));
 				selectedNamespaceMap.put(NumberUtils.toInt(namespaceId), NumberUtils.toInt(namespaceId));
 			}
 			next.addObject("selectedNamespaces", selectedNamespaceMap);
 		}
 		if (!StringUtils.isBlank(searchField)) {
 			// grab search engine instance and find results
 			List<SearchResultEntry> results = WikiBase.getSearchEngine().findResults(virtualWiki, searchField, selectedNamespaces);
 			next.addObject("searchField", searchField);
 			next.addObject("results", results);
 		}
 		pageInfo.setContentJsp(JSP_SEARCH);
 		pageInfo.setSpecial(true);
 	}
 }
