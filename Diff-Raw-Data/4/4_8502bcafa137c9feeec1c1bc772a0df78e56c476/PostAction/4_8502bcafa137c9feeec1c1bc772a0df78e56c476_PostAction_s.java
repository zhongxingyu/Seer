 /*
  * Copyright (c) Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: May 3, 2003 / 5:05:18 PM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum.view.forum;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import net.jforum.Command;
 import net.jforum.JForum;
 import net.jforum.SessionFacade;
 import net.jforum.dao.AttachmentDAO;
 import net.jforum.dao.DataAccessDriver;
 import net.jforum.dao.ForumDAO;
 import net.jforum.dao.KarmaDAO;
 import net.jforum.dao.PostDAO;
 import net.jforum.dao.TopicDAO;
 import net.jforum.dao.UserDAO;
 import net.jforum.entities.Attachment;
 import net.jforum.entities.Forum;
 import net.jforum.entities.Post;
 import net.jforum.entities.QuotaLimit;
 import net.jforum.entities.Topic;
 import net.jforum.entities.User;
 import net.jforum.entities.UserSession;
 import net.jforum.exceptions.AttachmentException;
 import net.jforum.exceptions.AttachmentSizeTooBigException;
 import net.jforum.repository.ForumRepository;
 import net.jforum.repository.PostRepository;
 import net.jforum.repository.RankingRepository;
 import net.jforum.repository.SecurityRepository;
 import net.jforum.repository.SmiliesRepository;
 import net.jforum.repository.TopicRepository;
 import net.jforum.security.PermissionControl;
 import net.jforum.security.SecurityConstants;
 import net.jforum.util.I18n;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 import net.jforum.util.preferences.TemplateKeys;
 import net.jforum.view.forum.common.AttachmentCommon;
 import net.jforum.view.forum.common.ForumCommon;
 import net.jforum.view.forum.common.PostCommon;
 import net.jforum.view.forum.common.TopicsCommon;
 import net.jforum.view.forum.common.ViewCommon;
 
 /**
  * @author Rafael Steil
 * @version $Id: PostAction.java,v 1.107 2005/10/02 19:06:49 rafaelsteil Exp $
  */
 public class PostAction extends Command 
 {
 	public void list() throws Exception 
 	{
 		PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 		UserDAO um = DataAccessDriver.getInstance().newUserDAO();
 		TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
 
 		UserSession us = SessionFacade.getUserSession();
 		int anonymousUser = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
 
 		int topicId = this.request.getIntParameter("topic_id");
 		
 		Topic topic = TopicRepository.getTopic(new Topic(topicId));
 		
 		if (topic == null) {
 			topic = tm.selectById(topicId);
 		}
 
 		// The topic exists?
 		if (topic.getId() == 0) {
 			this.topicNotFound();
 			return;
 		}
 
 		// Shall we proceed?
 		if (!SessionFacade.isLogged()) {
 			Forum f = ForumRepository.getForum(topic.getForumId());
 			
 			if (f == null || !ForumRepository.isCategoryAccessible(f.getCategoryId())) {
 				this.setTemplateName(ViewCommon.contextToLogin());
 				return;
 			}
 		}
 		else if (!TopicsCommon.isTopicAccessible(topic.getForumId())) {
 			return;
 		}
 
 		int count = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 		int start = ViewCommon.getStartPage();
 
 		PermissionControl pc = SecurityRepository.get(us.getUserId());
 
 		boolean canEdit = false;
 		if (pc.canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT)) {
 			canEdit = true;
 		}
 
 		List helperList = PostCommon.topicPosts(pm, canEdit, us.getUserId(), topic.getId(), start, count);
 		
 		// Ugly assumption:
 		// Is moderation pending for the topic?
 		if (topic.isModerated() && helperList.size() == 0) {
 			this.notModeratedYet();
 			return;
 		}
 
 		// Set the topic status as read
 		tm.updateReadStatus(topic.getId(), us.getUserId(), true);
 		
 		tm.incrementTotalViews(topic.getId());
 		topic.setTotalViews(topic.getTotalViews() + 1);
 
 		if (us.getUserId() != anonymousUser) {
 			((Map) SessionFacade.getAttribute(ConfigKeys.TOPICS_TRACKING)).put(new Integer(topic.getId()),
 					new Long(topic.getLastPostDate().getTime()));
 		}
 		
 		this.context.put("attachmentsEnabled", SecurityRepository.canAccess(
 				SecurityConstants.PERM_ATTACHMENTS_ENABLED, Integer.toString(topic.getForumId())));
 		this.context.put("canDownloadAttachments", SecurityRepository.canAccess(
 				SecurityConstants.PERM_ATTACHMENTS_DOWNLOAD));
 		this.context.put("am", new AttachmentCommon(this.request, topic.getForumId()));
 		this.context.put("karmaVotes", DataAccessDriver.getInstance().newKarmaDAO().getUserVotes(topic.getId(), us.getUserId()));
 		this.context.put("rssEnabled", SystemGlobals.getBoolValue(ConfigKeys.RSS_ENABLED));
 		this.context.put("canRemove",
 				SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_REMOVE));
 		this.context.put("canEdit", canEdit);
 		this.setTemplateName(TemplateKeys.POSTS_LIST);
 		this.context.put("allCategories", ForumCommon.getAllCategoriesAndForums(false));
 		this.context.put("topic", topic);
 		this.context.put("rank", new RankingRepository());
 		this.context.put("posts", helperList);
 		this.context.put("forum", ForumRepository.getForum(topic.getForumId()));
 		
 		Map topicPosters = tm.topicPosters(topic.getId());
 		
 		for (Iterator iter = topicPosters.values().iterator(); iter.hasNext(); ) {
 			ViewCommon.prepareUserSignature((User)iter.next());
 		}
 		
 		this.context.put("users", topicPosters);
 		this.context.put("topicId", new Integer(topicId));
 		this.context.put("anonymousPosts", SecurityRepository.canAccess(SecurityConstants.PERM_ANONYMOUS_POST, 
 				Integer.toString(topic.getForumId())));
 		this.context.put("watching", tm.isUserSubscribed(topicId, SessionFacade.getUserSession().getUserId()));
 		this.context.put("pageTitle", topic.getTitle());
 		this.context.put("isAdmin", SecurityRepository.canAccess(SecurityConstants.PERM_ADMINISTRATION));
 		this.context.put("readonly", !SecurityRepository.canAccess(SecurityConstants.PERM_READ_ONLY_FORUMS, 
 				Integer.toString(topic.getForumId())));
 		this.context.put("replyOnly", !SecurityRepository.canAccess(SecurityConstants.PERM_REPLY_ONLY, 
 				Integer.toString(topic.getForumId())));
 
 		this.context.put("isModerator", us.isModerator(topic.getForumId()));
 
 		// Topic Status
 		this.context.put("STATUS_LOCKED", new Integer(Topic.STATUS_LOCKED));
 		this.context.put("STATUS_UNLOCKED", new Integer(Topic.STATUS_UNLOCKED));
 
 		// Pagination
 		ViewCommon.contextToPagination(start, topic.getTotalReplies(), count);
 		
 		TopicRepository.updateTopic(topic);
 	}
 	
 	/**
 	 * Given a postId, sends the user to the right page
 	 * @throws Exception
 	 */
 	public void preList() throws Exception
 	{
 		int postId = this.request.getIntParameter("post_id");
 		
 		PostDAO pdao = DataAccessDriver.getInstance().newPostDAO();
 		
 		int count = pdao.countPreviousPosts(postId);
 		int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 		
 		int topicId = this.request.getIntParameter("topic_id");
 		String page = "";
 		
 		if (count > postsPerPage) {
 			page = Integer.toString(postsPerPage * ((count - 1) / postsPerPage)) + "/";
 		} 
 
 		JForum.setRedirect(
 			this.request.getContextPath() + "/posts/list/"
 			+ page + topicId
 			+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) 
 			+ "#" + postId);
 	}
 
 	public void listByUser() throws Exception 
 	{
 		PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 		UserDAO um = DataAccessDriver.getInstance().newUserDAO();
 		TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
 		
 		UserSession us = SessionFacade.getUserSession();
 		int anonymousUser = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
 		
 		User u = um.selectById(this.request.getIntParameter("user_id"));
 		
 		if (u.getId() == 0) {
 			this.context.put("message", I18n.getMessage("User.notFound"));
 			this.setTemplateName(TemplateKeys.USER_NOT_FOUND);
 			return;
 		} 
 			
 		int count = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 		int start = ViewCommon.getStartPage();
 		int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 		
 		List posts = pm.selectByUserByLimit(u.getId(), start, postsPerPage);
 		int totalMessages = u.getTotalPosts();
 		
 		// get list of forums
 		Map topics = new HashMap();
 		Map forums = new HashMap();
 		
 		for (Iterator iter = posts.iterator(); iter.hasNext(); ) {
 			Post p = (Post)iter.next();
 
 			if (!topics.containsKey(new Integer(p.getTopicId()))) {
 				Topic t = TopicRepository.getTopic(new Topic(p.getTopicId()));
 				
 				if (t == null) {
 					t = tm.selectRaw(p.getTopicId());
 				}
 	  		this.context.put("attachmentsEnabled", SecurityRepository.canAccess(
 				SecurityConstants.PERM_ATTACHMENTS_ENABLED, Integer.toString(t.getForumId())));
     		this.context.put("am", new AttachmentCommon(this.request, t.getForumId()));				
 	
 				topics.put(new Integer(t.getId()), t);
 			}
 			
 			if (!forums.containsKey(new Integer(p.getForumId()))) {
 				Forum f = ForumRepository.getForum(p.getForumId());
 				
 				if (f == null) {
 					// Ok, probably the user does not have permission to see this forum
 					iter.remove();
 					totalMessages--;
 					continue;
 				}
 				
 				forums.put(new Integer(f.getId()), f);
 			}
 			
 			PostCommon.preparePostForDisplay(p);
 		}
 		
 		this.setTemplateName(TemplateKeys.POSTS_USER_POSTS_LIST);
 		
 	this.context.put("canDownloadAttachments", SecurityRepository.canAccess(
 				SecurityConstants.PERM_ATTACHMENTS_DOWNLOAD));				
 		this.context.put("rssEnabled", SystemGlobals.getBoolValue(ConfigKeys.RSS_ENABLED));
 		this.context.put("allCategories", ForumCommon.getAllCategoriesAndForums(false));
 		this.context.put("posts", posts);
 		this.context.put("topics", topics);
 		this.context.put("forums", forums);
 		this.context.put("u", u);
 		this.context.put("pageTitle", I18n.getMessage("PostShow.userPosts") + " " + u.getUsername());
 		
 		ViewCommon.contextToPagination(start, totalMessages, count);
 	}
 
 	public void review() throws Exception {
 		PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 		UserDAO um = DataAccessDriver.getInstance().newUserDAO();
 		TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
 
 		int userId = SessionFacade.getUserSession().getUserId();
 		int topicId = this.request.getIntParameter("topic_id");
 		Topic topic = tm.selectById(topicId);
 
 		if (!TopicsCommon.isTopicAccessible(topic.getForumId())) {
 			return;
 		}
 
 		int count = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 		int start = ViewCommon.getStartPage();
 
 		Map usersMap = tm.topicPosters(topic.getId());
 		List helperList = PostCommon.topicPosts(pm, false, userId, topic.getId(), start, count);
 		Collections.reverse(helperList);
 
 		this.setTemplateName(SystemGlobals.getValue(ConfigKeys.TEMPLATE_DIR) + "/empty.htm");
 
 		this.setTemplateName(TemplateKeys.POSTS_REVIEW);
 		this.context.put("posts", helperList);
 		this.context.put("users", usersMap);
 	}
 
 	private void topicNotFound() {
 		this.setTemplateName(TemplateKeys.POSTS_TOPIC_NOT_FOUND);
 		this.context.put("message", I18n.getMessage("PostShow.TopicNotFound"));
 	}
 
 	private void postNotFound() {
 		this.setTemplateName(TemplateKeys.POSTS_POST_NOT_FOUND);
 		this.context.put("message", I18n.getMessage("PostShow.PostNotFound"));
 	}
 	
 	private void replyOnly()
 	{
 		this.setTemplateName(TemplateKeys.POSTS_REPLY_ONLY);
 		this.context.put("message", I18n.getMessage("PostShow.replyOnly"));
 	}
 	
 	private boolean isReplyOnly(int forumId) throws Exception
 	{
 		return !SecurityRepository.canAccess(SecurityConstants.PERM_REPLY_ONLY, 
 				Integer.toString(forumId));
 	}
 
 	public void insert() throws Exception 
 	{
 		int forumId = this.request.getIntParameter("forum_id");
 
 		if (!TopicsCommon.isTopicAccessible(forumId)) {
 			return;
 		}
 		
 		if (!this.anonymousPost(forumId)
 				|| this.isForumReadonly(forumId, this.request.getParameter("topic_id") != null)) {
 			return;
 		}
 
 		if (this.request.getParameter("topic_id") != null) {
 			int topicId = this.request.getIntParameter("topic_id");
 			
 			Topic t = TopicRepository.getTopic(new Topic(topicId));
 			
 			if (t == null) {
 				t = DataAccessDriver.getInstance().newTopicDAO().selectRaw(topicId);
 			}
 			
 			if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
 				return;
 			}
 
 			if (t.getStatus() == Topic.STATUS_LOCKED) {
 				this.topicLocked();
 				return;
 			}
 
 			this.context.put("topic", t);
 			this.context.put("setType", false);
 		}
 		else {
 			if (this.isReplyOnly(forumId)) {
 				this.replyOnly();
 				return;
 			}
 			this.context.put("setType", true);
 		}
 		
 		int userId = SessionFacade.getUserSession().getUserId();
 		
 		this.setTemplateName(TemplateKeys.POSTS_INSERT);
 		
 		// Attachments
 		boolean attachmentsEnabled = SecurityRepository.canAccess(
 				SecurityConstants.PERM_ATTACHMENTS_ENABLED, Integer.toString(forumId));
 		
 		if (attachmentsEnabled && !SessionFacade.isLogged() 
 				&& !SystemGlobals.getBoolValue(ConfigKeys.ATTACHMENTS_ANONYMOUS)) {
 			attachmentsEnabled = false;
 		}
 
 		this.context.put("attachmentsEnabled", attachmentsEnabled);
 		
 		if (attachmentsEnabled) {
 			QuotaLimit ql = new AttachmentCommon(this.request, forumId).getQuotaLimit(userId);
 			this.context.put("maxAttachmentsSize", new Long(ql != null ? ql.getSizeInBytes() : 1));
 			this.context.put("maxAttachments", SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_MAX_POST));
 		}
 		
 		boolean needCaptcha = SystemGlobals.getBoolValue(ConfigKeys.CAPTCHA_POSTS);
 		
 		if (needCaptcha) {
 			SessionFacade.getUserSession().createNewCaptcha();
 		}
 		
 		this.context.put("smilies", SmiliesRepository.getSmilies());
 		this.context.put("forum", ForumRepository.getForum(forumId));
 		this.context.put("action", "insertSave");
 		this.context.put("start", this.request.getParameter("start"));
 		this.context.put("isNewPost", true);
 		this.context.put("needCaptcha", needCaptcha);
 		this.context.put("htmlAllowed",
 				SecurityRepository.canAccess(SecurityConstants.PERM_HTML_DISABLED, Integer.toString(forumId)));
 		this.context.put("canCreateStickyOrAnnouncementTopics",
 				SecurityRepository.canAccess(SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS));
 
 		User user = DataAccessDriver.getInstance().newUserDAO().selectById(userId);
 		user.setSignature(PostCommon.processText(user.getSignature()));
 		user.setSignature(PostCommon.processSmilies(user.getSignature(), SmiliesRepository.getSmilies()));
 
 		if (this.request.getParameter("preview") != null) {
 			user.setNotifyOnMessagesEnabled(this.request.getParameter("notify") != null);
 		}
 
 		this.context.put("user", user);
 	}
 
 	public void edit() throws Exception {
 		this.edit(false, null);
 	}
 
 	private void edit(boolean preview, Post p) throws Exception 
 	{
 		int userId = SessionFacade.getUserSession().getUserId();
 		int aId = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
 		boolean canAccess = false;
 
 		if (!preview) {
 			PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 			p = pm.selectById(this.request.getIntParameter("post_id"));
 
 			// The post exist?
 			if (p.getId() == 0) {
 				this.postNotFound();
 				return;
 			}
 		}
 
 		boolean isModerator = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT);
 		canAccess = (isModerator || p.getUserId() == userId);
 
 		if ((userId != aId) && canAccess) {
 			Topic topic = TopicRepository.getTopic(new Topic(p.getTopicId()));
 				
 			if (topic == null) {
 				topic = DataAccessDriver.getInstance().newTopicDAO().selectById(p.getTopicId());
 			}
 
 			if (!TopicsCommon.isTopicAccessible(topic.getForumId())) {
 				return;
 			}
 
 			if (topic.getStatus() == Topic.STATUS_LOCKED && !isModerator) {
 				this.topicLocked();
 				return;
 			}
 
 			if (preview && this.request.getParameter("topic_type") != null) {
 				topic.setType(this.request.getIntParameter("topic_type"));
 			}
 			
 			if (p.hasAttachments()) {
 				this.context.put("attachments", 
 						DataAccessDriver.getInstance().newAttachmentDAO().selectAttachments(p.getId()));
 			}
 
 			this.context.put("attachmentsEnabled", SecurityRepository.canAccess(
 					SecurityConstants.PERM_ATTACHMENTS_ENABLED, Integer.toString(p.getForumId())));
 			
 			QuotaLimit ql = new AttachmentCommon(this.request, p.getForumId()).getQuotaLimit(userId);
 			this.context.put("maxAttachmentsSize", new Long(ql != null ? ql.getSizeInBytes() : 1));
 			
 			this.context.put("maxAttachments", SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_MAX_POST));
 			this.context.put("forum", ForumRepository.getForum(p.getForumId()));
 			this.context.put("action", "editSave");
 			this.context.put("post", p);
 			this.context.put("setType", p.getId() == topic.getFirstPostId());
 			this.context.put("topic", topic);
 			this.setTemplateName(TemplateKeys.POSTS_EDIT);
 			this.context.put("start", this.request.getParameter("start"));
 			this.context.put("htmlAllowed", SecurityRepository.canAccess(SecurityConstants.PERM_HTML_DISABLED, 
 					Integer.toString(topic.getForumId())));
 			this.context.put("canCreateStickyOrAnnouncementTopics",
 					SecurityRepository.canAccess(SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS));
 		}
 		else {
 			this.setTemplateName(TemplateKeys.POSTS_EDIT_CANNOTEDIT);
 			this.context.put("message", I18n.getMessage("CannotEditPost"));
 		}
 
 		UserDAO udao = DataAccessDriver.getInstance().newUserDAO();
 		User u = udao.selectById(userId);
 		ViewCommon.prepareUserSignature(u);
 
 		if (preview) {
 			u.setNotifyOnMessagesEnabled(this.request.getParameter("notify") != null);
 			
 			if (u.getId() != p.getUserId()) {
 				// Probably a moderator is editing the message
 				User previewUser = udao.selectById(p.getUserId());
 				ViewCommon.prepareUserSignature(previewUser);
 				this.context.put("previewUser", previewUser);
 			}
 		}
 
 		this.context.put("user", u);
 	}
 	
 	public void quote() throws Exception {
 		PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 		Post p = pm.selectById(this.request.getIntParameter("post_id"));
 
 		if (!this.anonymousPost(p.getForumId())) {
 			return;
 		}
 
 		Topic t = TopicRepository.getTopic(new Topic(p.getTopicId()));
 		
 		if (t == null) {
 			t = DataAccessDriver.getInstance().newTopicDAO().selectRaw(p.getTopicId());
 		}
 
 		if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
 			return;
 		}
 
 		if (t.getStatus() == Topic.STATUS_LOCKED) {
 			this.topicLocked();
 			return;
 		}
 
 		if (p.getId() == 0) {
 			this.postNotFound();
 			return;
 		}
 		
 		if (p.isModerationNeeded()) {
 			this.notModeratedYet();
 			return;
 		}
 
 		this.context.put("forum", ForumRepository.getForum(p.getForumId()));
 		this.context.put("action", "insertSave");
 		this.context.put("post", p);
 
 		UserDAO um = DataAccessDriver.getInstance().newUserDAO();
 		User u = um.selectById(p.getUserId());
 
 		Topic topic = DataAccessDriver.getInstance().newTopicDAO().selectById(p.getTopicId());
 		int userId = SessionFacade.getUserSession().getUserId();
 		
 		this.context.put("attachmentsEnabled", SecurityRepository.canAccess(
 				SecurityConstants.PERM_ATTACHMENTS_ENABLED, Integer.toString(topic.getForumId())));
 		
 		QuotaLimit ql = new AttachmentCommon(this.request, topic.getForumId()).getQuotaLimit(userId);
 		this.context.put("maxAttachmentsSize", new Long(ql != null ? ql.getSizeInBytes() : 1));
 		
 		this.context.put("maxAttachments", SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_MAX_POST));
 		this.context.put("isNewPost", true);
 		this.context.put("topic", topic);
 		this.context.put("quote", "true");
 		this.context.put("quoteUser", u.getUsername());
 		this.setTemplateName(TemplateKeys.POSTS_QUOTE);
 		this.context.put("setType", false);
 		this.context.put("htmlAllowed", SecurityRepository.canAccess(SecurityConstants.PERM_HTML_DISABLED, 
 				Integer.toString(topic.getForumId())));
 		this.context.put("start", this.request.getParameter("start"));
 		this.context.put("user", DataAccessDriver.getInstance().newUserDAO().selectById(userId));
 	}
 
 	public void editSave() throws Exception 
 	{
 		PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 		TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
 
 		Post p = pm.selectById(this.request.getIntParameter("post_id"));
 		p = PostCommon.fillPostFromRequest(p, true);
 
 		// The user wants to preview the message before posting it?
 		if (this.request.getParameter("preview") != null) {
 			this.context.put("preview", true);
 
 			Post postPreview = new Post(p);
 			this.context.put("postPreview", PostCommon.preparePostForDisplay(postPreview));
 
 			this.edit(true, p);
 		}
 		else {
 			AttachmentCommon attachments = new AttachmentCommon(this.request, p.getForumId());
 			
 			try {
 				attachments.preProcess();
 			}
 			catch (AttachmentException e) {
 				JForum.enableCancelCommit();
 				p.setText(this.request.getParameter("message"));
 				this.context.put("errorMessage", e.getMessage());
 				this.context.put("post", p);
 				this.edit(false, p);
 				return;
 			}
 			
 			Topic t = TopicRepository.getTopic(new Topic(p.getTopicId()));
 			
 			if (t == null) {
 				t = tm.selectById(p.getTopicId());
 			}
 
 			if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
 				return;
 			}
 
 			if (t.getStatus() == Topic.STATUS_LOCKED
 					&& !SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_EDIT)) {
 				this.topicLocked();
 				return;
 			}
 
 			pm.update(p);
 			
 			// Attachments
 			attachments.editAttachments(p.getId(), p.getForumId());
 			attachments.insertAttachments(p);
 
 			// Updates the topic title
 			if (t.getFirstPostId() == p.getId()) {
 				t.setTitle(p.getSubject());
 				
 				int newType = this.request.getIntParameter("topic_type");
 				boolean changeType = SecurityRepository.canAccess(SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS)
 					&& newType != t.getType();
 				
 				if (changeType) {
 					t.setType(newType);
 				}
 				
 				tm.update(t);
 				
 				User u = DataAccessDriver.getInstance().newUserDAO().selectById(p.getUserId());
 				
 				if (changeType) {
 					TopicRepository.addTopic(t);
 				}
 				else {
 					TopicRepository.updateTopic(t);
 				}
 			}
 
 			if (this.request.getParameter("notify") == null) {
 				tm.removeSubscription(p.getTopicId(), SessionFacade.getUserSession().getUserId());
 			}
 
 			String path = this.request.getContextPath() + "/posts/list/";
 			String start = this.request.getParameter("start");
 			
 			if (start != null && !start.equals("0")) {
 				path += start + "/";
 			}
 
 			path += p.getTopicId() + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) + "#" + p.getId();
 			JForum.setRedirect(path);
 			
 			if (SystemGlobals.getBoolValue(ConfigKeys.POSTS_CACHE_ENABLED)) {
 				PostRepository.update(p.getTopicId(), PostCommon.preparePostForDisplay(p));
 			}
 		}
 	}
 	
 	public void waitingModeration()
 	{
 		this.setTemplateName(TemplateKeys.POSTS_WAITING);
 		
 		int topicId = this.request.getIntParameter("topic_id");
 		String path = this.request.getContextPath();
 		
 		if (topicId == 0) {
 			path += "/forums/show/" + this.request.getParameter("forum_id");
 		}
 		else {
 			path += "/posts/list/" + topicId;
 		}
 		
 		this.context.put("message", I18n.getMessage("PostShow.waitingModeration", 
 				new String[] { path + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) }));
 	}
 	
 	private void notModeratedYet()
 	{
 		this.setTemplateName(TemplateKeys.POSTS_NOT_MODERATED);
 		this.context.put("message", I18n.getMessage("PostShow.notModeratedYet"));
 	}
 
 	public void insertSave() throws Exception 
 	{
 		int forumId = this.request.getIntParameter("forum_id");
 		boolean firstPost = false;
 
 		if (!this.anonymousPost(forumId)) {
 			SessionFacade.setAttribute(ConfigKeys.REQUEST_DUMP, this.request.dumpRequest());
 			return;
 		}
 		
 		Topic t = new Topic(-1);
 		t.setForumId(forumId);
 
 		boolean newTopic = (this.request.getParameter("topic_id") == null);
 		
 		if (!TopicsCommon.isTopicAccessible(t.getForumId())
 				|| this.isForumReadonly(t.getForumId(), newTopic)) {
 			return;
 		}
 
 		TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
 		PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 		ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();
 
 		if (!newTopic) {
 			int topicId = this.request.getIntParameter("topic_id");
 			
 			t = TopicRepository.getTopic(new Topic(topicId));
 			
 			if (t == null) {
 				t = tm.selectById(topicId);
 			}
 			
 			if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
 				return;
 			}
 
 			// Cannot insert new messages on locked topics
 			if (t.getStatus() == Topic.STATUS_LOCKED) {
 				this.topicLocked();
 				return;
 			}
 		}
 		else {
 			if (this.isReplyOnly(forumId)) {
 				this.replyOnly();
 				return;
 			}
 		}
 		
 		if (this.request.getParameter("topic_type") != null) {
 			t.setType(this.request.getIntParameter("topic_type"));
 			
 			if (t.getType() != Topic.TYPE_NORMAL 
 					&& !SecurityRepository.canAccess(SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS)) {
 				t.setType(Topic.TYPE_NORMAL);
 			}
 		}
 
 		UserSession us = SessionFacade.getUserSession();
 		User u = new User();
 		
 		if ("1".equals(this.request.getParameter("quick")) && SessionFacade.isLogged()) {
 			u = DataAccessDriver.getInstance().newUserDAO().selectById(us.getUserId());
 			this.request.addParameter("notify", u.isNotifyOnMessagesEnabled() ? "1" : null);
 			this.request.addParameter("attach_sig", u.getAttachSignatureEnabled() ? "1" : "0");
 		}
 		else {
 			u.setId(us.getUserId());
 			u.setUsername(us.getUsername());
 		}
 
 		// Set the Post
 		Post p = PostCommon.fillPostFromRequest();
 		
 		if (p.getText() == null || p.getText().trim().equals("")) {
 			this.insert();
 			return;
 		}
 		
 		// Check the elapsed time since the last post from the user
 		int delay = SystemGlobals.getIntValue(ConfigKeys.POSTS_NEW_DELAY);
 		
 		if (delay > 0) {
 			Long lastPostTime = (Long)SessionFacade.getAttribute(ConfigKeys.LAST_POST_TIME);
 			
 			if (lastPostTime != null) {
 				if (System.currentTimeMillis() < (lastPostTime.longValue() + delay)) {
 					this.context.put("post", p);
 					this.context.put("start", this.request.getParameter("start"));
 					this.context.put("error", I18n.getMessage("PostForm.tooSoon"));
 					this.insert();
 					return;
 				}
 			}
 		}
 		
 		p.setForumId(this.request.getIntParameter("forum_id"));
 		
 		if (p.getSubject() == null || p.getSubject() == "") {
 			p.setSubject(t.getTitle());
 		}
 		
 		boolean needCaptcha = SystemGlobals.getBoolValue(ConfigKeys.CAPTCHA_POSTS);
 		
 		if (needCaptcha) {
 			if (!us.validateCaptchaResponse(this.request.getParameter("captcha_anwser"))) {
 				this.context.put("post", p);
 				this.context.put("start", this.request.getParameter("start"));
 				this.context.put("error", I18n.getMessage("CaptchaResponseFails"));
 				
 				this.insert();
 				
 				return;
 			}
 		}
 
 		boolean preview = (this.request.getParameter("preview") != null);
 		boolean moderate = false;
 		
 		if (!preview) {
 			AttachmentCommon attachments = new AttachmentCommon(this.request, forumId);
 			
 			try {
 				attachments.preProcess();
 			}
 			catch (AttachmentSizeTooBigException e) {
 				JForum.enableCancelCommit();
 				p.setText(this.request.getParameter("message"));
 				p.setId(0);
 				this.context.put("errorMessage", e.getMessage());
 				this.context.put("post", p);
 				this.insert();
 				return;
 			}
 			
 			// If topic_id is -1, then is the first post
 			if (t.getId() == -1) {
 				t.setTime(new Date());
 				t.setTitle(this.request.getParameter("subject"));
 				t.setModerated(ForumRepository.getForum(forumId).isModerated());
 				t.setPostedBy(u);
 				t.setFirstPostTime(ViewCommon.formatDate(t.getTime()));
 
 				int topicId = tm.addNew(t);
 				t.setId(topicId);
 				firstPost = true;
 			}
 			
 			// Moderators and admins don't need to have their messages moderated
 			moderate = (t.isModerated() 
 					&& !SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION)
 					&& !SecurityRepository.canAccess(SecurityConstants.PERM_ADMINISTRATION));
 
 			// Topic watch
 			if (this.request.getParameter("notify") != null) {
 				this.watch(tm, t.getId(), u.getId());
 			}
 
 			p.setTopicId(t.getId());
 
 			// Save the remaining stuff
 			p.setModerate(moderate);
 			int postId = pm.addNew(p);
 
 			if (newTopic) {
 				t.setFirstPostId(postId);
 			}
 			
 			t.setLastPostId(postId);
 			t.setLastPostBy(u);
 			t.setLastPostDate(p.getTime());
 			t.setLastPostTime(p.getFormatedTime());
 			
 			tm.update(t);
 			
 			attachments.insertAttachments(p);
 			
 			if (!moderate) {
 				if (!newTopic) {
 					TopicsCommon.notifyUsers(t, tm);
 					t.setTotalReplies(t.getTotalReplies() + 1);
 				}
 				
 				t.setTotalViews(t.getTotalViews() + 1);
 				
 				DataAccessDriver.getInstance().newUserDAO().incrementPosts(p.getUserId());
 				
 				TopicsCommon.updateBoardStatus(t, postId, firstPost, tm, fm);
 				ForumRepository.updateForumStats(t, u, p);
 				
 				String path = this.request.getContextPath() + "/posts/list/";
 				int start = ViewCommon.getStartPage();
 	
 				path += this.startPage(t, start) + "/";
 				path += t.getId() + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION) + "#" + postId;
 	
 				JForum.setRedirect(path);
 	
 				int anonymousUser = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
 				if (u.getId() != anonymousUser) {
 					((Map) SessionFacade.getAttribute(ConfigKeys.TOPICS_TRACKING)).put(new Integer(t.getId()),
 							new Long(p.getTime().getTime()));
 				}
 				
 				if (SystemGlobals.getBoolValue(ConfigKeys.POSTS_CACHE_ENABLED)) {
 					SimpleDateFormat df = new SimpleDateFormat(SystemGlobals.getValue(ConfigKeys.DATE_TIME_FORMAT));
 					p.setFormatedTime(df.format(p.getTime()));
 					
 					PostRepository.append(p.getTopicId(), PostCommon.preparePostForDisplay(p));
 				}
 			}
 			else {
 				JForum.setRedirect(this.request.getContextPath() + "/posts/waitingModeration/" + (firstPost ? 0 : t.getId())
 						+ "/" + t.getForumId()
 						+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 			}
 			
 			if (delay > 0) {
 				SessionFacade.setAttribute(ConfigKeys.LAST_POST_TIME, new Long(System.currentTimeMillis()));
 			}
 		}
 		else {
 			this.context.put("preview", true);
 			this.context.put("post", p);
 			this.context.put("start", this.request.getParameter("start"));
 
 			Post postPreview = new Post(p);
 			this.context.put("postPreview", PostCommon.preparePostForDisplay(postPreview));
 
 			this.insert();
 		}
 	}
 
 	private int startPage(Topic t, int currentStart) {
 		int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 
 		int newStart = (t.getTotalReplies() + 1) / postsPerPage * postsPerPage;
 		if (newStart > currentStart) {
 			return newStart;
 		}
 		
 		return currentStart;
 	}
 
 	public void delete() throws Exception 
 	{
 		if (!SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_POST_REMOVE)) {
 			this.setTemplateName(TemplateKeys.POSTS_CANNOT_DELETE);
 			this.context.put("message", I18n.getMessage("CannotRemovePost"));
 
 			return;
 		}
 
 		// Post
 		PostDAO pm = DataAccessDriver.getInstance().newPostDAO();
 		Post p = pm.selectById(this.request.getIntParameter("post_id"));
 
 		TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
 		Topic t = TopicRepository.getTopic(new Topic(p.getTopicId()));
 		
 		if (t == null) {
 			t = tm.selectById(p.getTopicId());
 		}
 
 		if (!TopicsCommon.isTopicAccessible(t.getForumId())) {
 			return;
 		}
 
 		if (p.getId() == 0) {
 			this.postNotFound();
 			return;
 		}
 
 		pm.delete(p);
 		DataAccessDriver.getInstance().newUserDAO().decrementPosts(p.getUserId());
 		
 		// Karma
 		KarmaDAO karmaDao = DataAccessDriver.getInstance().newKarmaDAO();
 		karmaDao.updateUserKarma(p.getUserId());
 		
 		// Attachments
 		new AttachmentCommon(this.request, p.getForumId()).deleteAttachments(p.getId(), p.getForumId());
 
 		// Topic
 		tm.decrementTotalReplies(p.getTopicId());
 
 		int maxPostId = tm.getMaxPostId(p.getTopicId());
 		if (maxPostId > -1) {
 			tm.setLastPostId(p.getTopicId(), maxPostId);
 		}
 
 		int minPostId = tm.getMinPostId(p.getTopicId());
 		if (minPostId > -1) {
 		  tm.setFirstPostId(p.getTopicId(), minPostId);
 		}
         
 		// Forum
 		ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();
 
 		maxPostId = fm.getMaxPostId(p.getForumId());
 		if (maxPostId > -1) {
 			fm.setLastPost(p.getForumId(), maxPostId);
 		}
 
 		// It was the last remaining post in the topic?
 		int totalPosts = tm.getTotalPosts(p.getTopicId());
 		if (totalPosts > 0) {
 			String page = this.request.getParameter("start");
 			String returnPath = this.request.getContextPath() + "/posts/list/";
 
 			if (page != null && !page.equals("") && !page.equals("0")) {
 				int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POST_PER_PAGE);
 				int newPage = Integer.parseInt(page);
 
 				if (totalPosts % postsPerPage == 0) {
 					newPage -= postsPerPage;
 				}
 
 				returnPath += newPage + "/";
 			}
 
 			JForum.setRedirect(returnPath + p.getTopicId() + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 		}
 		else {
 			// Ok, all posts were removed. Time to say goodbye
 			TopicsCommon.deleteTopic(p.getTopicId(), p.getForumId(), false);
 
 			JForum.setRedirect(this.request.getContextPath() + "/forums/show/" + p.getForumId()
 					+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 		}
 
 		ForumRepository.reloadForum(p.getForumId());
 		TopicRepository.clearCache(p.getForumId());
 		PostRepository.clearCache(p.getTopicId());
 	}
 
 	private void watch(TopicDAO tm, int topicId, int userId) throws Exception {
 		if (!tm.isUserSubscribed(topicId, userId)) {
 			tm.subscribeUser(topicId, userId);
 		}
 	}
 
 	public void watch() throws Exception {
 		int topicId = this.request.getIntParameter("topic_id");
 		int userId = SessionFacade.getUserSession().getUserId();
 
 		this.watch(DataAccessDriver.getInstance().newTopicDAO(), topicId, userId);
 		this.list();
 	}
 
 	public void unwatch() throws Exception {
 		if (SessionFacade.isLogged()) {
 			int topicId = this.request.getIntParameter("topic_id");
 			int userId = SessionFacade.getUserSession().getUserId();
 			String start = this.request.getParameter("start");
 
 			DataAccessDriver.getInstance().newTopicDAO().removeSubscription(topicId, userId);
 
 			String returnPath = this.request.getContextPath() + "/posts/list/";
 			if (start != null && !start.equals("")) {
 				returnPath += start + "/";
 			}
 
 			returnPath += topicId + SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION);
 
 			this.setTemplateName(TemplateKeys.POSTS_UNWATCH);
 			this.context.put("message", I18n.getMessage("ForumBase.unwatched", new String[] { returnPath }));
 		}
 		else {
 			this.setTemplateName(ViewCommon.contextToLogin());
 		}
 	}
 
 	public void downloadAttach() throws Exception
 	{
 		if ((SecurityRepository.canAccess(SecurityConstants.PERM_ATTACHMENTS_ENABLED) && 
 				!SecurityRepository.canAccess(SecurityConstants.PERM_ATTACHMENTS_DOWNLOAD))
 				|| (!SessionFacade.isLogged() && !SystemGlobals.getBoolValue(ConfigKeys.ATTACHMENTS_ANONYMOUS))) {
 			this.setTemplateName(TemplateKeys.POSTS_CANNOT_DOWNLOAD);
 			this.context.put("message", I18n.getMessage("Attachments.featureDisabled"));
 			return;
 		}
 		
 		int id = this.request.getIntParameter("attach_id");
 		
 		AttachmentDAO am = DataAccessDriver.getInstance().newAttachmentDAO();
 		Attachment a = am.selectAttachmentById(id);
 		
 		String filename = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR)
 			+ "/"
 			+ a.getInfo().getPhysicalFilename();
 		
 		if (!new File(filename).exists()) {
 			this.setTemplateName(TemplateKeys.POSTS_ATTACH_NOTFOUND);
 			this.context.put("message", I18n.getMessage("Attachments.notFound"));
 			return;
 		}
 		
 		a.getInfo().setDownloadCount(a.getInfo().getDownloadCount() + 1);
 		am.updateAttachment(a);
 		
 		FileInputStream fis = new FileInputStream(filename);
 		OutputStream os = response.getOutputStream();
 		
 		if(am.isPhysicalDownloadMode(a.getInfo().getExtension().getExtensionGroupId())) {
 			this.response.setContentType("application/octet-stream");
 		}
 		else {
 			this.response.setContentType(a.getInfo().getMimetype());
 		}
 		
 		if (this.request.getHeader("User-Agent").indexOf("Firefox") != -1) {			
 			this.response.setHeader("Content-Disposition", "attachment; filename=\"" 
 					+ new String(a.getInfo().getRealFilename().getBytes(SystemGlobals.getValue(ConfigKeys.ENCODING)), 
 							SystemGlobals.getValue(ConfigKeys.DEFAULT_CONTAINER_ENCODING)) + "\";");
 		} 
 		else {
 			this.response.setHeader("Content-Disposition", "attachment; filename=\"" 
 					+ ViewCommon.toUtf8String(a.getInfo().getRealFilename()) + "\";");
 		}
 		
 		this.response.setContentLength((int)a.getInfo().getFilesize());
 		
 		int c = 0;
 		byte[] b = new byte[4096];
 		while ((c = fis.read(b)) != -1) {
 			os.write(b, 0, c);
 		}
 		
 		fis.close();
 		os.close();
 		
 		JForum.enableBinaryContent(true);
 	}
 	
 	private void topicLocked() {
 		this.setTemplateName(TemplateKeys.POSTS_TOPIC_LOCKED);
 		this.context.put("message", I18n.getMessage("PostShow.topicLocked"));
 	}
 	
 	public void listSmilies()
 	{
 		this.setTemplateName(SystemGlobals.getValue(ConfigKeys.TEMPLATE_DIR) + "/empty.htm");
 		this.setTemplateName(TemplateKeys.POSTS_LIST_SMILIES);
 		this.context.put("smilies", SmiliesRepository.getSmilies());
 	}
 
 	private boolean isForumReadonly(int forumId, boolean isReply) throws Exception {
 		if (!SecurityRepository.canAccess(SecurityConstants.PERM_READ_ONLY_FORUMS, Integer.toString(forumId))) {
 			if (isReply) {
 				this.list();
 			}
 			else {
 				JForum.setRedirect(this.request.getContextPath() + "/forums/show/" + forumId
 						+ SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION));
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	private boolean anonymousPost(int forumId) throws Exception {
 		// Check if anonymous posts are allowed
 		if (!SessionFacade.isLogged()
 				&& !SecurityRepository.canAccess(SecurityConstants.PERM_ANONYMOUS_POST, Integer.toString(forumId))) {
 			this.setTemplateName(ViewCommon.contextToLogin());
 
 			return false;
 		}
 
 		return true;
 	}
 }
