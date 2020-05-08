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
 
 import java.text.DateFormat;
 import java.util.Collection;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to display the edit history information for a topic.
  */
 public class HistoryServlet extends JAMWikiServlet {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(HistoryServlet.class.getName());
 	protected static final String JSP_HISTORY = "history.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (StringUtils.hasText(request.getParameter("topicVersionId"))) {
 			viewVersion(request, next, pageInfo);
 		} else {
 			history(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void history(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		String topicName = Utilities.getTopicFromRequest(request);
 		if (!StringUtils.hasText(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		pageInfo.setContentJsp(JSP_HISTORY);
 		pageInfo.setTopicName(topicName);
 		pageInfo.setPageTitle(new WikiMessage("history.title", topicName));
 		Pagination pagination = Utilities.buildPagination(request, next);
 		Collection changes = WikiBase.getDataHandler().getRecentChanges(virtualWiki, topicName, pagination, true);
 		next.addObject("changes", changes);
 		next.addObject("numChanges", new Integer(changes.size()));
 	}
 
 	/**
 	 *
 	 */
 	private void viewVersion(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		// display an older version
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		String topicName = Utilities.getTopicFromRequest(request);
 		int topicVersionId = Integer.parseInt(request.getParameter("topicVersionId"));
 		TopicVersion topicVersion = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId, null);
 		if (topicVersion == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		topic.setTopicContent(topicVersion.getVersionContent());
 		String versionDate = DateFormat.getDateTimeInstance().format(topicVersion.getEditDate());
 		WikiMessage pageTitle = new WikiMessage("topic.title", topicName + " @" + versionDate);
 		ServletUtil.viewTopic(request, next, pageInfo, pageTitle, topic, false);
 	}
 }
