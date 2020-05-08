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
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Used to provide capability for deleting, undeleting, and protecting topics.
  */
 public class ManageServlet extends JAMWikiServlet {
 
 	private static WikiLogger logger = WikiLogger.getLogger(ManageServlet.class.getName());
 	protected static final String JSP_ADMIN_MANAGE = "admin-manage.jsp";
 
 	/**
 	 *
 	 */
 	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		if (StringUtils.hasText(request.getParameter("delete"))) {
 			delete(request, next, pageInfo);
 		} else if (StringUtils.hasText(request.getParameter("undelete"))) {
 			undelete(request, next, pageInfo);
 		} else if (StringUtils.hasText(request.getParameter("permissions"))) {
 			permissions(request, next, pageInfo);
 		} else {
 			view(request, next, pageInfo);
 		}
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void delete(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = Utilities.getTopicFromRequest(request);
 		if (topicName == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		deletePage(request, next, pageInfo, topicName);
 		if (StringUtils.hasText(request.getParameter("manageCommentsPage"))) {
 			String manageCommentsPage = Utilities.decodeFromRequest(request.getParameter("manageCommentsPage"));
 			if (Utilities.isCommentsPage(manageCommentsPage) && !manageCommentsPage.equals(topicName)) {
 				deletePage(request, next, pageInfo, manageCommentsPage);
 			}
 		}
 		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
 		view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void deletePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName) throws Exception {
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true, null);
 		if (topic.getDeleted()) {
 			logger.warning("Attempt to delete a topic that is already deleted: " + virtualWiki + " / " + topicName);
 			return;
 		}
 		String contents = "";
 		topic.setTopicContent(contents);
 		TopicVersion topicVersion = new TopicVersion(Utilities.currentUser(request), request.getRemoteAddr(), request.getParameter("deleteComment"), contents);
 		topicVersion.setEditType(TopicVersion.EDIT_DELETE);
 		WikiBase.getDataHandler().deleteTopic(topic, topicVersion, true, null);
 	}
 
 	/**
 	 *
 	 */
 	private void permissions(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = Utilities.getTopicFromRequest(request);
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		if (topicName == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		topic.setReadOnly(request.getParameter("readOnly") != null);
 		topic.setAdminOnly(request.getParameter("adminOnly") != null);
 		TopicVersion topicVersion = new TopicVersion(Utilities.currentUser(request), request.getRemoteAddr(), Utilities.formatMessage("manage.message.permissions", request.getLocale()), topic.getTopicContent());
 		topicVersion.setEditType(TopicVersion.EDIT_PERMISSION);
 		WikiBase.getDataHandler().writeTopic(topic, topicVersion, null, true, null);
 		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
 		view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void undelete(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = Utilities.getTopicFromRequest(request);
 		if (topicName == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		undeletePage(request, next, pageInfo, topicName);
 		if (StringUtils.hasText(request.getParameter("manageCommentsPage"))) {
 			String manageCommentsPage = Utilities.decodeFromRequest(request.getParameter("manageCommentsPage"));
 			if (Utilities.isCommentsPage(manageCommentsPage) && !manageCommentsPage.equals(topicName)) {
 				undeletePage(request, next, pageInfo, manageCommentsPage);
 			}
 		}
 		next.addObject("message", new WikiMessage("manage.message.updated", topicName));
 		view(request, next, pageInfo);
 	}
 
 	/**
 	 *
 	 */
 	private void undeletePage(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName) throws Exception {
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true, null);
 		if (!topic.getDeleted()) {
 			logger.warning("Attempt to undelete a topic that is not deleted: " + virtualWiki + " / " + topicName);
 			return;
 		}
 		TopicVersion previousVersion = WikiBase.getDataHandler().lookupTopicVersion(topic.getCurrentVersionId().intValue(), null);
 		while (previousVersion != null && previousVersion.getPreviousTopicVersionId() != null && previousVersion.getEditType() == TopicVersion.EDIT_DELETE) {
 			// loop back to find the last non-delete edit
 			previousVersion = WikiBase.getDataHandler().lookupTopicVersion(previousVersion.getPreviousTopicVersionId().intValue(), null);
 		}
 		String contents = previousVersion.getVersionContent();
 		topic.setTopicContent(contents);
 		TopicVersion topicVersion = new TopicVersion(Utilities.currentUser(request), request.getRemoteAddr(), request.getParameter("undeleteComment"), contents);
 		topicVersion.setEditType(TopicVersion.EDIT_UNDELETE);
 		WikiBase.getDataHandler().undeleteTopic(topic, topicVersion, true, null);
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
 		String topicName = Utilities.getTopicFromRequest(request);
 		String virtualWiki = Utilities.getVirtualWikiFromURI(request);
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, true, null);
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		String commentsPage = Utilities.extractCommentsLink(topicName);
 		if (!topicName.equals(commentsPage)) {
 			Topic commentsTopic = WikiBase.getDataHandler().lookupTopic(virtualWiki, commentsPage, true, null);
 			if (commentsTopic != null && commentsTopic.getDeleted() == topic.getDeleted()) {
 				// add option to also move comments page
 				next.addObject("manageCommentsPage", commentsPage);
 			}
 		}
 		next.addObject("readOnly", new Boolean(topic.getReadOnly()));
 		next.addObject("adminOnly", new Boolean(topic.getAdminOnly()));
 		next.addObject("deleted", new Boolean(topic.getDeleteDate() != null));
 		pageInfo.setTopicName(topicName);
 		pageInfo.setContentJsp(JSP_ADMIN_MANAGE);
 		pageInfo.setPageTitle(new WikiMessage("manage.title", topicName));
 	}
 }
