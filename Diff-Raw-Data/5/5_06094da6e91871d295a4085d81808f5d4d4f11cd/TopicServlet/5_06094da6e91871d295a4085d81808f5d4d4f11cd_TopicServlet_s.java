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
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to display a JAMWiki topic.
  */
 public class TopicServlet extends JAMWikiServlet {
 
 	/** Logger for this class and subclasses. */
 	private static final WikiLogger logger = WikiLogger.getLogger(TopicServlet.class.getName());
 
 	/**
 	 * This method handles the request after its parent class receives control. It gets the topic's name and the
 	 * virtual wiki name from the uri, loads the topic and returns a view to the end user.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		view(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = WikiUtil.getTopicFromURI(request);
 		if (StringUtils.isBlank(topicName)) {
 			String virtualWikiName = pageInfo.getVirtualWikiName();
 			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(virtualWikiName);
 			topicName = virtualWiki.getDefaultTopicName();
 		}
 		String virtualWiki = pageInfo.getVirtualWikiName();
 		if (StringUtils.isBlank(virtualWiki)) {
 			virtualWiki = WikiBase.DEFAULT_VWIKI;
 		}
 		Topic topic = ServletUtil.initializeTopic(virtualWiki, topicName);
 		if (topic.getTopicId() <= 0) {
 			// topic does not exist, display empty page
			next.addObject("notopic", new WikiMessage("topic.notcreated", topicName));
 		}
 		WikiMessage pageTitle = new WikiMessage("topic.title", topicName);
 		ServletUtil.viewTopic(request, next, pageInfo, pageTitle, topic, true);
 	}
 }
