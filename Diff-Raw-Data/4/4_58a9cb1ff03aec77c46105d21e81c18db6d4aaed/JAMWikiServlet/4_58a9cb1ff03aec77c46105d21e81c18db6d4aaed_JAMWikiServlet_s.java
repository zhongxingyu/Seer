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
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 /**
  *
  */
 public abstract class JAMWikiServlet extends AbstractController {
 
 	private static final Logger logger = Logger.getLogger(JAMWikiServlet.class);
 	public static final String PARAMETER_PAGE_INFO = "pageInfo";
 	public static final String PARAMETER_TOPIC = "topic";
 	public static final String PARAMETER_TOPIC_OBJECT = "topicObject";
 	public static final String PARAMETER_USER = "user";
 	public static final String PARAMETER_VIRTUAL_WIKI = "virtualWiki";
 	public static final String USER_COOKIE = "user-cookie";
 	public static final String USER_COOKIE_DELIMITER = "|";
 	// FIXME - make configurable
 	public static final int USER_COOKIE_EXPIRES = 60 * 60 * 24 * 14; // 14 days
 	private static LinkedHashMap cachedContents = new LinkedHashMap();
 
 	/**
 	 *
 	 */
 	protected void redirect(String destination, HttpServletResponse response) {
 		String url = response.encodeRedirectURL(destination);
 		try {
 			response.sendRedirect(url);
 		} catch (Exception e) {
 			logger.error(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static void buildLayout(HttpServletRequest request, ModelAndView next) throws Exception {
 		String virtualWikiName = JAMWikiServlet.getVirtualWikiFromURI(request);
 		if (virtualWikiName == null) {
 			logger.error("No virtual wiki available for page request " + request.getRequestURI());
 			virtualWikiName = WikiBase.DEFAULT_VWIKI;
 		}
 		VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 		if (virtualWiki == null) {
 			logger.error("No virtual wiki found for " + virtualWikiName);
 			virtualWikiName = WikiBase.DEFAULT_VWIKI;
 			virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 		}
 		// build the layout contents
 		String leftMenu = JAMWikiServlet.getCachedContent(request, virtualWikiName, WikiBase.SPECIAL_PAGE_LEFT_MENU, true);
 		next.addObject("leftMenu", leftMenu);
 		next.addObject("defaultTopic", virtualWiki.getDefaultTopicName());
 		next.addObject("logo", Environment.getValue(Environment.PROP_BASE_LOGO_IMAGE));
 		String bottomArea = JAMWikiServlet.getCachedContent(request, virtualWikiName, WikiBase.SPECIAL_PAGE_BOTTOM_AREA, true);
 		next.addObject("bottomArea", bottomArea);
 		next.addObject(PARAMETER_VIRTUAL_WIKI, virtualWikiName);
 	}
 
 	/**
 	 *
 	 */
 	protected Topic getRedirectTarget(Topic parent, int attempts) throws Exception {
 		if (parent.getTopicType() != Topic.TYPE_REDIRECT || !StringUtils.hasText(parent.getRedirectTo())) {
 			logger.error("getRedirectTarget() called for non-redirect topic " + parent.getName());
 			return parent;
 		}
 		// avoid infinite redirection
 		attempts++;
 		if (attempts > 10) {
 			throw new WikiException(new WikiMessage("topic.redirect.infinite"));
 		}
 		// get the topic that is being redirected to
 		Topic child = WikiBase.getHandler().lookupTopic(parent.getVirtualWiki(), parent.getRedirectTo());
 		if (child == null) {
 			// child being redirected to doesn't exist, return parent
 			return parent;
 		}
 		if (!StringUtils.hasText(child.getRedirectTo())) {
 			// found a topic that is not a redirect, return
 			return child;
 		}
 		if (WikiBase.getHandler().lookupTopic(child.getVirtualWiki(), child.getRedirectTo()) == null) {
 		}
 		// topic is a redirect, keep looking
 		return this.getRedirectTarget(child, attempts);
 	}
 
 	/**
 	 *
 	 */
 	public static String getTopicFromURI(HttpServletRequest request) throws Exception {
 		String uri = request.getRequestURI().trim();
 		// FIXME - needs testing on other platforms
 		uri = Utilities.convertEncoding(uri, "ISO-8859-1", "UTF-8");
 		if (uri == null || uri.length() <= 0) {
 			throw new Exception("URI string is empty");
 		}
 		int slashIndex = uri.lastIndexOf('/');
 		if (slashIndex == -1) {
 			throw new Exception("No topic in URL: " + uri);
 		}
 		String topic = uri.substring(slashIndex + 1);
 		topic = Utilities.decodeURL(topic);
 		return topic;
 	}
 
 	/**
 	 *
 	 */
 	public static String getTopicFromRequest(HttpServletRequest request) throws Exception {
 		String topic = request.getParameter(JAMWikiServlet.PARAMETER_TOPIC);
 		if (topic == null) {
 			topic = (String)request.getAttribute(JAMWikiServlet.PARAMETER_TOPIC);
 		}
 		if (topic == null) return null;
 		topic = Utilities.decodeURL(topic);
 		return topic;
 	}
 
 	/**
 	 *
 	 */
 	public static String getVirtualWikiFromURI(HttpServletRequest request) {
 		String uri = request.getRequestURI().trim();
 		// FIXME - needs testing on other platforms
 		uri = Utilities.convertEncoding(uri, "ISO-8859-1", "UTF-8");
 		String contextPath = request.getContextPath().trim();
 		String virtualWiki = null;
 		if (!StringUtils.hasText(uri) || contextPath == null) {
 			return null;
 		}
 		uri = uri.substring(contextPath.length() + 1);
 		int slashIndex = uri.indexOf('/');
 		if (slashIndex == -1) {
 			return null;
 		}
 		virtualWiki = uri.substring(0, slashIndex);
 		return virtualWiki;
 	}
 
 	/**
 	 *
 	 */
 	protected static boolean isTopic(HttpServletRequest request, String value) {
 		try {
 			String topic = JAMWikiServlet.getTopicFromURI(request);
 			if (!StringUtils.hasText(topic)) {
 				return false;
 			}
 			if (value != null &&  topic.equals(value)) {
 				return true;
 			}
 		} catch (Exception e) {}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	public static String getCachedContent(HttpServletRequest request, String virtualWiki, String topicName, boolean cook) {
 		String content = (String)cachedContents.get(virtualWiki + "-" + topicName);
 		if (content == null) {
 			try {
 				Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 				content = topic.getTopicContent();
 				if (cook) {
 					ParserInput parserInput = new ParserInput();
 					parserInput.setContext(request.getContextPath());
 					parserInput.setLocale(request.getLocale());
 					parserInput.setVirtualWiki(virtualWiki);
 					parserInput.setTopicName(topicName);
 					ParserOutput parserOutput = Utilities.parse(parserInput, content, topicName);
 					content = parserOutput.getContent();
 				}
 				cachedContents.put(virtualWiki + "-" + topicName, content);
 			} catch (Exception e) {
 				logger.warn("error getting cached page " + virtualWiki + " / " + topicName, e);
 				return null;
 			}
 		}
 		return content;
 	}
 
 	/**
 	 * Utility method for adding category content to the ModelAndView object.
 	 */
 	protected void loadCategoryContent(ModelAndView next, String virtualWiki, String topicName) throws Exception {
 		String categoryName = topicName.substring(WikiBase.NAMESPACE_CATEGORY.length());
 		next.addObject("categoryName", categoryName);
 		Collection categoryTopics = WikiBase.getHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_ARTICLE);
 		next.addObject("categoryTopics", categoryTopics);
 		next.addObject("numCategoryTopics", new Integer(categoryTopics.size()));
 		Collection categoryImages = WikiBase.getHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_IMAGE);
 		next.addObject("categoryImages", categoryImages);
 		next.addObject("numCategoryImages", new Integer(categoryImages.size()));
 		Collection tempSubcategories = WikiBase.getHandler().lookupCategoryTopics(virtualWiki, topicName, Topic.TYPE_CATEGORY);
 		LinkedHashMap subCategories = new LinkedHashMap();
 		for (Iterator iterator = tempSubcategories.iterator(); iterator.hasNext();) {
 			Category category = (Category)iterator.next();
 			String value = category.getChildTopicName().substring(WikiBase.NAMESPACE_CATEGORY.length());
 			subCategories.put(category.getChildTopicName(), value);
 		}
 		next.addObject("subCategories", subCategories);
 		next.addObject("numSubCategories", new Integer(subCategories.size()));
 	}
 
 	/**
 	 *
 	 */
 	protected void loadDefaults(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) {
 		// load cached top area, nav bar, etc.
 		try {
 			this.buildLayout(request, next);
 		} catch (Exception e) {
 			logger.error("Unable to build default page layout", e);
 		}
 		// add link to user page and comments page
 		WikiUser user = Utilities.currentUser(request);
 		if (user != null) {
 			next.addObject("userpage", WikiBase.NAMESPACE_USER + user.getLogin());
 			next.addObject("usercomments", WikiBase.NAMESPACE_USER_COMMENTS + user.getLogin());
 			next.addObject("adminUser", new Boolean(user.getAdmin()));
 		}
 		if (Environment.getBooleanValue(Environment.PROP_TOPIC_NON_ADMIN_TOPIC_MOVE) || (user != null && user.getAdmin())) {
 			pageInfo.setCanMove(true);
 		}
 		if (!pageInfo.getSpecial()) {
 			String article = Utilities.extractTopicLink(pageInfo.getTopicName());
 			String comments = Utilities.extractCommentsLink(pageInfo.getTopicName());
 			next.addObject("article", article);
 			next.addObject("comments", comments);
 			String editLink = "Special:Edit?topic=" + Utilities.encodeURL(pageInfo.getTopicName());
 			if (StringUtils.hasText(request.getParameter("topicVersionId"))) {
 				editLink += "&topicVersionId=" + request.getParameter("topicVersionId");
 			}
 			next.addObject("edit", editLink);
 		}
 		if (!StringUtils.hasText(pageInfo.getTopicName())) {
 			try {
 				pageInfo.setTopicName(JAMWikiServlet.getTopicFromURI(request));
 			} catch (Exception e) {
 				logger.error("Unable to load topic value in JAMWikiServlet", e);
 			}
 		}
 		next.addObject(PARAMETER_PAGE_INFO, pageInfo);
 	}
 
 	/**
 	 * Clears cached contents including the top area, left nav, bottom area, etc.
 	 * This method should be called when the contents of these areas may have been
 	 * modified.
 	 */
 	public static void removeCachedContents() {
 		cachedContents.clear();
 	}
 
 	/**
 	 * Action used when redirecting to an error page.
 	 *
 	 * @param request The servlet request object.
 	 * @param next The Spring ModelAndView object.
 	 * @param e The exception that is the source of the error.
 	 */
 	protected ModelAndView viewError(HttpServletRequest request, Exception e) {
 		if (!(e instanceof WikiException)) {
 			logger.error("Servlet error", e);
 		}
 		ModelAndView next = new ModelAndView("wiki");
 		WikiPageInfo pageInfo = new WikiPageInfo();
 		pageInfo.setPageTitle(new WikiMessage("error.title"));
 		pageInfo.setAction(WikiPageInfo.ACTION_ERROR);
 		pageInfo.setSpecial(true);
 		if (e instanceof WikiException) {
 			WikiException we = (WikiException)e;
 			next.addObject("errorMessage", we.getWikiMessage());
 		} else {
 			next.addObject("errorMessage", new WikiMessage("error.unknown", e.getMessage()));
 		}
 		loadDefaults(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 * Action used when redirecting to a login page.
 	 *
 	 * @param request The servlet request object.
 	 * @param next The Spring ModelAndView object.
 	 * @param topic The topic to be redirected to.  Valid examples are "Special:Admin",
 	 *  "StartingPoints", etc.
 	 */
 	protected ModelAndView viewLogin(HttpServletRequest request, String topic, WikiMessage errorMessage) throws Exception {
 		ModelAndView next = new ModelAndView("wiki");
 		WikiPageInfo pageInfo = new WikiPageInfo();
 		String virtualWikiName = JAMWikiServlet.getVirtualWikiFromURI(request);
 		String redirect = request.getParameter("redirect");
 		if (!StringUtils.hasText(redirect)) {
 			if (!StringUtils.hasText(topic)) {
 				VirtualWiki virtualWiki = WikiBase.getHandler().lookupVirtualWiki(virtualWikiName);
 				topic = virtualWiki.getDefaultTopicName();
 			}
 			redirect = LinkUtil.buildInternalLinkUrl(request.getContextPath(), virtualWikiName, topic, null, request.getQueryString());
 		}
 		next.addObject("redirect", redirect);
 		pageInfo.setPageTitle(new WikiMessage("login.title"));
 		pageInfo.setAction(WikiPageInfo.ACTION_LOGIN);
 		pageInfo.setSpecial(true);
 		if (errorMessage != null) {
 			next.addObject("errorMessage", errorMessage);
 		}
 		loadDefaults(request, next, pageInfo);
 		return next;
 	}
 
 	/**
 	 * Action used when viewing a topic.
 	 *
 	 * @param request The servlet request object.
 	 * @param next The Spring ModelAndView object.
 	 * @param topicName The topic being viewed.  This value must be a valid topic that
 	 *  can be loaded as a org.jamwiki.model.Topic object.
 	 */
 	protected void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, String topicName) throws Exception {
 		if (!Utilities.validateName(topicName)) {
 			throw new WikiException(new WikiMessage("common.exception.name", topicName));
 		}
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		if (!StringUtils.hasText(virtualWiki)) {
 			virtualWiki = WikiBase.DEFAULT_VWIKI;
 		}
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		if (topic == null) {
 			// topic does not exist, display empty page
 			topic = new Topic();
 			topic.setName(topicName);
 			topic.setVirtualWiki(virtualWiki);
 			next.addObject("notopic", new WikiMessage("topic.notcreated", topicName));
 		}
 		WikiMessage pageTitle = new WikiMessage("topic.title", topicName);
 		viewTopic(request, next, pageInfo, pageTitle, topic, true, false);
 	}
 
 	/**
 	 * Action used when viewing a topic.
 	 *
 	 * @param request The servlet request object.
 	 * @param next The Spring ModelAndView object.
 	 * @param topicName The topic being viewed.  This value must be a valid topic that
 	 *  can be loaded as a org.jamwiki.model.Topic object.
 	 */
 	protected void viewTopic(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo, WikiMessage pageTitle, Topic topic, boolean sectionEdit, boolean preview) throws Exception {
 		// FIXME - what should the default be for topics that don't exist?
 		String contents = "";
 		if (topic == null) {
 			throw new WikiException(new WikiMessage("common.exception.notopic"));
 		}
 		if (!Utilities.validateName(topic.getName())) {
 			throw new WikiException(new WikiMessage("common.exception.name", topic.getName()));
 		}
 		if (topic.getTopicType() == Topic.TYPE_REDIRECT && (request.getParameter("redirect") == null || !request.getParameter("redirect").equalsIgnoreCase("no"))) {
 			Topic child = this.getRedirectTarget(topic, 0);
 			if (!child.getName().equals(topic.getName())) {
 				pageInfo.setRedirectName(topic.getName());
 				pageTitle = new WikiMessage("topic.title", child.getName());
 				topic = child;
 			}
 		}
 		String virtualWiki = topic.getVirtualWiki();
 		String topicName = topic.getName();
 		String displayName = request.getRemoteAddr();
 		WikiUser user = Utilities.currentUser(request);
 		ParserInput parserInput = new ParserInput();
 		parserInput.setContext(request.getContextPath());
 		parserInput.setLocale(request.getLocale());
 		parserInput.setWikiUser(user);
 		parserInput.setTopicName(topicName);
 		parserInput.setUserIpAddress(request.getRemoteAddr());
 		parserInput.setVirtualWiki(virtualWiki);
 		parserInput.setAllowSectionEdit(sectionEdit);
 		if (preview) {
 			parserInput.setMode(ParserInput.MODE_PREVIEW);
 		}
 		ParserOutput parserOutput = Utilities.parse(parserInput, topic.getTopicContent(), topicName);
 		if (parserOutput != null) {
 			if (parserOutput.getCategories().size() > 0) {
 				LinkedHashMap categories = new LinkedHashMap();
 				for (Iterator iterator = parserOutput.getCategories().keySet().iterator(); iterator.hasNext();) {
 					String key = (String)iterator.next();
 					String value = key.substring(WikiBase.NAMESPACE_CATEGORY.length());
 					categories.put(key, value);
 				}
 				next.addObject("categories", categories);
 			}
 			topic.setTopicContent(parserOutput.getContent());
 		}
 		if (topic.getTopicType() == Topic.TYPE_CATEGORY) {
 			loadCategoryContent(next, virtualWiki, topic.getName());
 		}
 		if (topic.getTopicType() == Topic.TYPE_IMAGE) {
 			Collection fileVersions = WikiBase.getHandler().getAllWikiFileVersions(virtualWiki, topicName, true);
 			next.addObject("fileVersions", fileVersions);
 		}
 		pageInfo.setSpecial(false);
 		pageInfo.setTopicName(topicName);
 		next.addObject(JAMWikiServlet.PARAMETER_TOPIC_OBJECT, topic);
 		if (pageTitle != null) {
 			pageInfo.setPageTitle(pageTitle);
 		}
 	}
 }
