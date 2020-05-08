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
 import org.apache.log4j.Logger;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  *
  */
 public class MoveServlet extends JAMWikiServlet {
 
 	private static final Logger logger = Logger.getLogger(MoveServlet.class);
 
 	/**
 	 *
 	 */
 	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
 		ModelAndView next = new ModelAndView("wiki");
 		WikiPageInfo pageInfo = new WikiPageInfo();
 		try {
 			if (request.getParameter("move") != null) {
 				// FIXME - temporarily make admin only
 				if (!Utilities.isAdmin(request)) {
 					WikiMessage errorMessage = new WikiMessage("admin.message.loginrequired");
 					return viewLogin(request, JAMWikiServlet.getTopicFromURI(request), errorMessage);
 				}
 				move(request, next, pageInfo);
 			} else {
 				view(request, next, pageInfo);
 			}
 		} catch (Exception e) {
 			return viewError(request, e);
 		}
 		loadDefaults(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void move(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		String topicName = JAMWikiServlet.getTopicFromRequest(request);
 		if (!StringUtils.hasText(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
 		pageInfo.setPageTitle(pageTitle);
 		pageInfo.setTopicName(topicName);
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		String moveDestination = request.getParameter("moveDestination");
 		if (!StringUtils.hasText(moveDestination)) {
			pageInfo.setAction(WikiPageInfo.ACTION_MOVE);
 			next.addObject("errorMessage", new WikiMessage("move.exception.nodestination"));
 			return;
 		}
 		Topic oldTopic = WikiBase.getHandler().lookupTopic(virtualWiki, moveDestination);
 		// FIXME - allow overwriting a deleted topic
 		if (oldTopic != null) {
			pageInfo.setAction(WikiPageInfo.ACTION_MOVE);
 			next.addObject("errorMessage", new WikiMessage("move.exception.destinationexists", moveDestination));
 			return;
 		}
 		WikiUser user = Utilities.currentUser(request);
 		Integer authorId = null;
 		if (user != null) {
 			authorId = new Integer(user.getUserId());
 		}
 		String moveComment = Utilities.getMessage("move.editcomment", request.getLocale()) + " " + topicName;
 		if (StringUtils.hasText(request.getParameter("moveComment"))) {
 			moveComment += ": " + request.getParameter("moveComment");
 		}
 		TopicVersion topicVersion = new TopicVersion(authorId, request.getRemoteAddr(), moveComment, topic.getTopicContent());
 		topicVersion.setEditType(TopicVersion.EDIT_MOVE);
 		WikiBase.getHandler().moveTopic(topic, topicVersion, moveDestination);
 		viewTopic(request, next, pageInfo, moveDestination);
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = JAMWikiServlet.getTopicFromRequest(request);
 		if (!StringUtils.hasText(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		WikiMessage pageTitle = new WikiMessage("move.title", topicName);
 		pageInfo.setPageTitle(pageTitle);
 		pageInfo.setAction(WikiPageInfo.ACTION_MOVE);
 		pageInfo.setTopicName(topicName);
 	}
 }
