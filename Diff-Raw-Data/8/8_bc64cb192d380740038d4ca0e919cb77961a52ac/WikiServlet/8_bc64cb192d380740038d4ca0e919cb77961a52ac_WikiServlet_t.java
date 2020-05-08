 /**
  * The WikiServlet handles the main interactions from the user and forwards to the
  * appropriate servlets and JSPs
  *
  * Copyright 2002 Gareth Cronin
  * This software is subject to the GNU Lesser General Public Licence (LGPL)
  */
 package org.jmwiki.servlets;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Vector;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.log4j.Logger;
 import org.jmwiki.ActionManager;
 import org.jmwiki.Environment;
 import org.jmwiki.PluginManager;
 import org.jmwiki.PseudoTopicHandler;
 import org.jmwiki.SearchEngine;
 import org.jmwiki.SearchResultEntry;
 import org.jmwiki.WikiAction;
 import org.jmwiki.WikiBase;
 import org.jmwiki.persistency.Topic;
 import org.jmwiki.users.Usergroup;
 import org.jmwiki.utils.JSPUtils;
 import org.jmwiki.utils.Utilities;
 
 /*
  * TODO This Servlet is a litle bit too complex. It could be a good idea to
  * refactorize and split resposibilities in different objects.
  */
 
 public class WikiServlet extends JMWikiServlet {
 
 	private static final Logger logger = Logger.getLogger(WikiServlet.class);
 	// constants used as the action parameter in calls to this servlet
 	public static final String ACTION_ADMIN = "action_admin";
 	public static final String ACTION_ATTACH = "action_attach";
 	public static final String ACTION_CANCEL = "Cancel";
 	public static final String ACTION_DIFF = "action_diff";
 	public static final String ACTION_EDIT = "action_edit";
 	public static final String ACTION_FIRST_USE = "action_firstuse";
 	public static final String ACTION_HISTORY = "action_history";
 	public static final String ACTION_IMPORT = "action_import";
 	public static final String ACTION_LOGIN = "action_login";
 	public static final String ACTION_MEMBER = "action_member";
 	public static final String ACTION_MENU_JUMP = "action_menujump";
 	public static final String ACTION_NOTIFY = "action_notify";
 	public static final String ACTION_PRINT = "action_printable";
 	public static final String ACTION_RSS = "RSS";
 	public static final String ACTION_SAVE_USER = "action_save_user";
 	public static final String ACTION_SEARCH = "action_search";
 	public static final String ACTION_UNLOCK = "action_unlock";
 	public static final String ACTION_VIEW_ATTACHMENT = "action_view_attachment";
 	public static final String ACTION_ALL_TOPICS = "all_topics";
 	public static final String ACTION_APPEND = "append";
 	public static final String ACTION_EDIT_USER = "edit_user";
 	public static final String ACTION_LOCKLIST = "locklist";
 	public static final String ACTION_ORPHANED_TOPICS = "orphaned_topics";
 	public static final String ACTION_PREVIEW = "preview";
 	public static final String ACTION_RECENT_CHANGES = "recent_changes";
 	public static final String ACTION_SAVE = "save";
 	public static final String ACTION_SAVE_TEMPLATE = "save_template";
 	public static final String ACTION_SEARCH_RESULTS = "search_results";
 	public static final String ACTION_TODO_TOPICS = "todo_topics";
 	public static final String ACTION_DELETE = "action_delete";
 	public static final String PARAMETER_ACTION = "action";
 	public static final String PARAMETER_SPECIAL = "special";
 
 	/** Servlet context */
 	// why static? Better not to do that.
 	private static ServletContext servletContext = null;
 	private static Map cachedContents = new HashMap();
 
 	/**
 	 *
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		// store servlet context reference so that it can be accessed from a static
 		// context
 		servletContext = config.getServletContext();
 	}
 
 	/**
 	 * Returns a reference to the current servlet context
 	 */
 	public static ServletContext getCurrentContext() {
 		return servletContext;
 	}
 
 	/**
 	 *
 	 */
 	protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
 		throws ServletException, IOException {
 		super.doPut(httpServletRequest, httpServletResponse);
 		PluginManager.setRealPath(httpServletRequest.getSession().getServletContext().getRealPath("/"));
 	}
 
 	/**
 	 * GET requests should come from topic display requests, edit requests, diffs
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		logger.debug("GET Topic: " + request.getQueryString());
 		PluginManager.setRealPath(request.getSession().getServletContext().getRealPath("/"));
 		request.setAttribute("lastRequest", request.getRequestURL());
 		// expire now
 		response.setDateHeader("Expires", System.currentTimeMillis() - 24 * 60 * 60 * 1000);
 		String topic = request.getQueryString();
 		if (topic == null) {
 			topic = "StartingPoints";
 		}
 		if (topic.equals("")) {
 			topic = "StartingPoints";
 		}
 		if (topic.indexOf('&') > 0) {
 			topic = topic.substring(0, topic.indexOf('&'));
 		}
 		if (request.getCharacterEncoding() != null) {
 			topic = JSPUtils.decodeURL(topic, request.getCharacterEncoding());
 		} else {
 			topic = JSPUtils.decodeURL(topic, response.getCharacterEncoding());
 		}
 		String virtualWiki = null;
 		try {
 			virtualWiki = Utilities.extractVirtualWiki(request);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		logger.debug("virtual wiki: " + virtualWiki);
 		request.setAttribute("virtual-wiki", virtualWiki);
 		request.setAttribute("virtualWiki", virtualWiki);
 		buildLayout(request, virtualWiki);
 		if (Environment.getValue(Environment.PROP_TOPIC_BASE_CONTEXT) == null) {
 			try {
 				//StringBuffer originalURL = request.getRequestURL();
 				StringBuffer originalURL = request.getRequestURL();
 				logger.info(originalURL);
 				String baseContext = originalURL.substring(0, originalURL.length() - virtualWiki.length() - 6);
 				Environment.setValue(Environment.PROP_TOPIC_BASE_CONTEXT, baseContext);
 			} catch (Exception e) {
 				error(request, response, e);
 				return;
 			}
 		}
 		RequestDispatcher dispatch;
 		// make decision on action if one exists
 		String action = request.getParameter("action");
 		logger.info("Servlet action: " + action);
 		if (action != null) {
 			String actionRedirect = PseudoTopicHandler.getInstance().getRedirectURL(action);
 			if (action.equals(ACTION_EDIT)) {
 				// a request to edit a topic
 				if (Environment.getBooleanValue(Environment.PROP_TOPIC_FORCE_USERNAME)) {
 					if (Utilities.getUserFromRequest(request) == null) {
 						try {
 							request.setAttribute("userList", WikiBase.getInstance().getUsergroupInstance().getListOfAllUsers());
 						} catch (Exception e) { }
 						dispatch = request.getRequestDispatcher(PseudoTopicHandler.getInstance().getRedirectURL("SetUsername"));
 						dispatch.forward(request, response);
 						return;
 					}
 				}
 				dispatch = request.getRequestDispatcher("/EditServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_RSS)) {
 				dispatch = request.getRequestDispatcher("/RSS");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_RECENT_CHANGES)) {
 				dispatch = request.getRequestDispatcher("/RecentChanges");
 				dispatch.forward(request, response);
 				return;
 			} else if (actionRedirect != null) {
 				logger.debug("Using redirect from pseudotopics actions: " + actionRedirect);
 				request.setAttribute(WikiServlet.PARAMETER_ACTION, action);
 				// FIXME - this is a mess, clean it up
 				if (action.equals("WikiLockList")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_LOCKLIST);
 				} else if (action.equals("RecentChanges")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_RECENT_CHANGES);
 				} else if (action.equals("WikiSearch")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_SEARCH);
 				} else if (action.equals("SetUsername")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_EDIT_USER);
 				} else if (action.equals("ToDoWikiTopics")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_TODO_TOPICS);
 				} else if (action.equals("OrphanedWikiTopics")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_ORPHANED_TOPICS);
 				} else if (action.equals("AllWikiTopics")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_ALL_TOPICS);
 				} else if (action.equals("WikiLogin")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_LOGIN);
 				} else if (action.equals("RSS")) {
 					request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 					request.setAttribute(PARAMETER_ACTION, ACTION_RSS);
 				} else {
 					logger.info("Unknown PseudoTopic action " + action);
 				}
 				dispatch(actionRedirect, request, response);
 				return;
 			} else if (ActionManager.getInstance().actionExists(action)) {
 				logger.debug("using action mapping from ActionManager");
 				try {
 					WikiAction wikiAction = ActionManager.getInstance().getActionInstance(action);
 					if (wikiAction != null) {
 						wikiAction.doAction(request, response);
 						return;
 					}
 				} catch (Exception e) {
 					logger.error("error running action", e);
 					request.setAttribute("exception", e);
 					request.setAttribute("title", "Error");
 					log("Error in " + this.getClass(), e);
 					if (e instanceof WikiServletException) {
 						request.setAttribute("javax.servlet.jsp.jspException", e);
 					}
 					dispatch("/WEB-INF/jsp/servlet-error.jsp", request, response);
 					return;
 				}
			} else if (action.equals(ACTION_SEARCH)) {
				dispatch = request.getRequestDispatcher("/SearchServlet");
				dispatch.forward(request, response);
				return;
			} else if (action.equals(ACTION_SEARCH_RESULTS)) {
				dispatch = request.getRequestDispatcher("/SearchServlet");
				dispatch.forward(request, response);
				return;
 			}
 		}
 		logger.debug("no action mappings, assuming topic");
 		request.setAttribute("topic", JSPUtils.decodeURL(topic));
 		request.setAttribute("title", JSPUtils.decodeURL(topic));
 		// make decision based on topic
 		response.setContentType("text/html");
 		String pseudotopicRedirect = PseudoTopicHandler.getInstance().getRedirectURL(topic);
 		if (pseudotopicRedirect != null) {
 			// FIXME - this is a mess, clean it up
 			if (topic.equals("WikiLockList")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_LOCKLIST);
 			} else if (topic.equals("RecentChanges")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_RECENT_CHANGES);
 			} else if (topic.equals("WikiSearch")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_SEARCH);
 			} else if (topic.equals("SetUsername")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_EDIT_USER);
 			} else if (topic.equals("ToDoWikiTopics")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_TODO_TOPICS);
 			} else if (topic.equals("OrphanedWikiTopics")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_ORPHANED_TOPICS);
 			} else if (topic.equals("AllWikiTopics")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_ALL_TOPICS);
 			} else if (topic.equals("WikiLogin")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_LOGIN);
 			} else if (topic.equals("RSS")) {
 				request.setAttribute(WikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 				request.setAttribute(PARAMETER_ACTION, ACTION_RSS);
 			} else {
 				logger.info("Unknown PseudoTopic topic " + topic);
 			}
 			PseudoTopicHandler.getInstance().setAttributes(topic, request);
 			dispatch(pseudotopicRedirect, request, response);
 			return;
 		} else {
 			dispatchTopic(request, topic, virtualWiki, response);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static void buildLayout(HttpServletRequest request, String virtualWiki) {
 		// build the layout contents
 		ResourceBundle messages = getMessages(request.getLocale());
 		addIfNotEmpty(
 			request, "leftMenu", getCachedContent(
 				virtualWiki,
 				messages.getString("specialpages.leftMenu")
 			)
 		);
 		request.setAttribute(
 			"topArea", getCachedContent(
 				virtualWiki,
 				messages.getString("specialpages.topArea")
 			)
 		);
 		request.setAttribute(
 			"bottomArea", getCachedContent(
 				virtualWiki,
 				messages.getString("specialpages.bottomArea")
 			)
 		);
 		request.setAttribute(
 			"StyleSheet", getCachedRawContent(
 				virtualWiki,
 				messages.getString("specialpages.stylesheet")
 			)
 		);
 	}
 
 	/**
 	 *
 	 */
 	private static ResourceBundle getMessages(Locale locale) {
 		ResourceBundle messages = ResourceBundle.getBundle("ApplicationResources", locale);
 		return messages;
 	}
 
 	/**
 	 * Setup request and despatch to the JSP to display a topic
 	 * @param request
 	 * @param topic
 	 * @param virtualWiki
 	 * @param response
 	 * @param en
 	 */
 	private void dispatchTopic(HttpServletRequest request, String topic, String virtualWiki, HttpServletResponse response) {
 		try {
 			if (WikiBase.getInstance().isAdminOnlyTopic(request.getLocale(), virtualWiki, topic)) {
 				if (!Utilities.isAdmin(request)) {
 					request.setAttribute("title", Utilities.resource("login.title", request.getLocale()));
 					logger.debug("Current URL: " + request.getRequestURL());
 					String rootPath = JSPUtils.createLocalRootPath(request, virtualWiki);
 					StringBuffer buffer = new StringBuffer();
 					buffer.append(rootPath);
 					buffer.append("Wiki?" + topic);
 					request.setAttribute(
 						"redirect",
 						buffer.toString()
 					);
 					request.setAttribute(WikiServlet.PARAMETER_ACTION, WikiServlet.ACTION_LOGIN);
 					dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
 					return;
 				}
 			}
 		} catch (Exception e) {
 			logger.error("error checking admin only topic", e);
 		}
 		String contents = null;
 		try {
 			String rawcontents = WikiBase.getInstance().readRaw(virtualWiki, topic);
 			// deal with redirection
 			if (rawcontents.startsWith("redirect:")) {
 				if (rawcontents.trim().length() > 9) {
 					// The rest until end or newline is a topic name.
 					if (rawcontents.indexOf("\n") >= 0) {
 						topic = rawcontents.substring(rawcontents.indexOf(":") + 1, rawcontents.indexOf("\n")).trim();
 					} else {
 						topic = rawcontents.substring(rawcontents.indexOf(":") + 1).trim();
 					}
 					redirect("Wiki?" + JSPUtils.encodeURL(topic, response.getCharacterEncoding()), response);
 					return;
 				}
 			}
 			// convert the rawcontent to html content
 			contents = WikiBase.getInstance().cook(new BufferedReader(new StringReader(rawcontents)), virtualWiki);
 		} catch (Exception e) {
 			error(request, response, e);
 			return;
 		}
 		// highlight search result
 		if (request.getParameter("highlight") != null) {
 			String highlightparam = request.getParameter("highlight");
 			String highlighttext = "<b style=\"color:black;background-color:#ffff66\">###</b>";
 			contents = markToReplaceOutsideHTML(contents, highlightparam);
 			for (int i = 0; i < highlightparam.length(); i++) {
 				String myhighlightparam = highlightparam.substring(0, i)
 					+ highlightparam.substring(i, i + 1).toUpperCase();
 				if ((i + 1) < highlightparam.length()) {
 					myhighlightparam += highlightparam.substring(i + 1);
 				}
 				String highlight = highlighttext;
 				highlight = Utilities.replaceString(highlight, "###", myhighlightparam);
 				contents = replaceMarked(contents, myhighlightparam, highlight);
 				myhighlightparam = highlightparam.substring(0, i)
 					+ highlightparam.substring(i, i + 1).toLowerCase();
 				if ((i + 1) < highlightparam.length()) {
 					myhighlightparam += highlightparam.substring(i + 1);
 				}
 				highlight = highlighttext;
 				highlight = Utilities.replaceString(highlight, "###", myhighlightparam);
 				contents = replaceMarked(contents, myhighlightparam, highlight);
 			}
 		}
 		// -------------
 		// Handle page history (breadcrumb trail)
 		HttpSession session = request.getSession();
 		Vector history = (Vector) session.getAttribute("history");
 		String historyVirtualWiki = (String) session.getAttribute("historyVirtualWiki");
 		if (historyVirtualWiki != null) {
 			if (!virtualWiki.equals(historyVirtualWiki)) {
 				// reset history on virtual wiki changes
 				history = new Vector();
 				session.setAttribute("historyVirtualWiki", virtualWiki);
 			}
 		} else {
 			session.setAttribute("historyVirtualWiki", virtualWiki);
 		}
 		if (history == null) {
 			history = new Vector();
 		}
 		// if user clicked on "refresh"
 		if (history.size() > 0 && topic.equals(history.lastElement())) {
 			history.remove(history.size() - 1);
 		}
 		// add current page to history
 		if (history.contains(topic)) {
 			// go back in history
 			int found = history.indexOf(topic);
 			int pos = history.size() - 1;
 			while (pos >= found) {
 				history.remove(pos);
 				pos--;
 			}
 		}
 		// store the history in the request
 		request.setAttribute("historyThisPage", new Vector(history));
 		// really add it
 		history.add(topic);
 		// store it in session
 		session.setAttribute("history", history);
 		try {
 			SearchEngine sedb = WikiBase.getInstance().getSearchEngineInstance();
 			int maxBackLinks = Environment.getIntValue(Environment.PROP_TOPIC_MAXIMUM_BACKLINKS);
 			// create backlinks
 			if (topic != null) {
 				Collection results = sedb.findLinkedTo(virtualWiki, topic);
 				ResourceBundle messages = getMessages(request.getLocale());
 				if (results != null && results.size() > 0) {
 					StringBuffer buffer = new StringBuffer("");
 					buffer.append("<br /><br /><span class=\"backlinks\">");
 					buffer.append(topic);
 					buffer.append(" ");
 					buffer.append(messages.getString("topic.ismentionedon"));
 					buffer.append(" ");
 					Iterator it = results.iterator();
 					String divider = "";
 					int i = 0;
 					for (; i < maxBackLinks && it.hasNext(); i++) {
 						SearchResultEntry result = (SearchResultEntry) it.next();
 						String pathRoot = JSPUtils.createLocalRootPath(request, virtualWiki);
 						request.setAttribute("pathRoot", pathRoot);
 						if (!result.getTopic().equals(topic)) {
 							buffer.append(divider);
 							buffer.append("<a href=\"");
 							buffer.append(pathRoot);
 							buffer.append("Wiki?");
 							buffer.append(result.getTopic());
 							if (result.getFoundWord().length() > 0) {
 								buffer.append("&highlight=");
 								buffer.append(JSPUtils.encodeURL(result.getFoundWord(), response.getCharacterEncoding()));
 							}
 							buffer.append("\">");
 							buffer.append(result.getTopic());
 							buffer.append("</a>");
 							divider = " | ";
 						}
 					}
 					if (i == 20) {
 						buffer.append("...");
 					}
 					buffer.append("</span>");
 					contents += buffer.toString();
 				}
 			}
 		} catch (Exception e) {
 			logger.warn(e.getMessage(), e);
 		}
 		request.setAttribute("contents", contents);
 		logger.debug("contents: " + contents);
 		if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
 			try {
 				logger.debug("topic: " + topic);
 				Topic topicData = new Topic(topic);
 				java.util.Date revDate = topicData.getMostRecentRevisionDate(virtualWiki);
 				String userid = topicData.getMostRecentAuthor(virtualWiki);
 				Usergroup usergroup = WikiBase.getInstance().getUsergroupInstance();
 				String author = usergroup.getFullnameById(userid);
 				if (revDate != null) {
 					request.setAttribute("lastRevisionDate", revDate);
 					if (author != null) {
 						request.setAttribute("lastAuthor", author);
 						request.setAttribute("lastAuthorDetails", usergroup.getUserDetails(userid));
 					}
 				}
 			} catch (Exception e) {
 				logger.warn(e.getMessage(), e);
 			}
 		}
 		boolean readOnly = false;
 		try {
 			if (topic != null) {
 				Topic t = new Topic(topic);
 				readOnly = t.isReadOnlyTopic(virtualWiki);
 			}
 		} catch (Exception e) {
 			logger.warn(e.getMessage(), e);
 		}
 		request.setAttribute("readOnly", new Boolean(readOnly));
 		dispatch("/WEB-INF/jsp/wiki.jsp", request, response);
 	}
 
 	/**
 	 *
 	 */
 	private static void addIfNotEmpty(HttpServletRequest request, String name, String content) {
 		logger.debug("addIfNotEmpty called for " + name + "/" + content);
 		if (content == null) {
 			logger.debug("content provided is null, returning");
 			return;
 		}
 		content = content.trim();
 		if ("".equals(content)) {
 			logger.debug("content is empty, returning");
 			return;
 		}
 		if ("delete".equalsIgnoreCase(content)) {
 			logger.debug("content is marked for purging, returning");
 			return;
 		}
 		if ("This is a new topic".equalsIgnoreCase(content)) {
 			logger.debug("topic is an unchanged new topic, returning");
 			return;
 		}
 		logger.debug("setting content " + name + " = " + content);
 		request.setAttribute(name, content);
 	}
 
 	/**
 	 *
 	 */
 	private interface WikiReader {
 		String read(String virtualWiki, String topic) throws Exception;
 	}
 
 	/**
 	 *
 	 */
 	private static WikiReader cookedReader = new WikiReader() {
 		public String read(String virtualWiki, String topic) throws Exception {
 			return WikiBase.getInstance().readCooked(virtualWiki, topic);
 		}
 	};
 
 	/**
 	 *
 	 */
 	private static WikiReader rawReader = new WikiReader() {
 		public String read(String virtualWiki, String topic) throws Exception {
 			return WikiBase.getInstance().readRaw(virtualWiki, topic);
 		}
 	};
 
 	/**
 	 *
 	 */
 	private static String getCached(String virtualWiki, String topic, WikiReader wr) {
 		String content = (String) cachedContents.get(virtualWiki + "-" + topic);
 		if (content == null) {
 			try {
 				logger.debug("reloading topic " + topic);
 				content = wr.read(virtualWiki, topic);
 				synchronized (cachedContents) {
 					cachedContents.put(virtualWiki + "-" + topic, content);
 				}
 			} catch (Exception e) {
 				logger.warn("error getting cached page", e);
 				return null;
 			}
 		}
 		return content;
 	}
 
 	/**
 	 * Implements a caching for "fixed" contents.
 	 * Caching is particularly important for often-asked topics (like left-side-menu,
 	 * top-banners, bottom-banners, etc. that are asked for every request)
 	 *
 	 * @param virtualWiki the virtual wiki
 	 * @param topic	   the topic name
 	 * @return the topic contents
 	 */
 	private static String getCachedContent(String virtualWiki, String topic) {
 		return getCached(virtualWiki, topic, cookedReader);
 	}
 
 	/**
 	 * Implements a caching for "fixed" contents.
 	 * Caching is particularly important for often-asked topics (like left-side-menu,
 	 * top-banners, bottom-banners, etc. that are asked for every request)
 	 *
 	 * @param virtualWiki the virtual wiki
 	 * @param topic	   the topic name
 	 * @return the topic contents
 	 */
 	private static String getCachedRawContent(String virtualWiki, String topic) {
 		return getCached(virtualWiki, topic, rawReader);
 	}
 
 	/**
 	 * Clears the cached content
 	 * This method is called when a "edit-save" or "edit-cancel" is invoked.
 	 * <p/>
 	 * Clearing all cached contents forces to reload.
 	 */
 	private void removeCachedContents() {
 		if (logger.isDebugEnabled()) {
 			logger.debug(
 				"Removing Cached Contents; " +
 				"cachedContents.size() = " + cachedContents.size()
 			);
 		}
 		cachedContents.clear();
 	}
 
 	/**
 	 * POST requests should only come from saves and searches
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		logger.debug("POST");
 		request.setAttribute("lastRequest", request.getRequestURL());
 		RequestDispatcher dispatch;
 		response.setContentType("text/html");
 		String virtualWiki = null;
 		try {
 			virtualWiki = Utilities.extractVirtualWiki(request);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		logger.debug("virtual wiki: " + virtualWiki);
 		//  TODO virtual-wiki and virtualWiki request params should be unified.
 		request.setAttribute("virtual-wiki", virtualWiki);
 		request.setAttribute("virtualWiki", virtualWiki);
 		buildLayout(request, virtualWiki);
 		// make decision based on action
 		String action = request.getParameter("action");
 		if (action != null) {
 			logger.debug("Wiki action: " + action);
 			String actionRedirect = PseudoTopicHandler.getInstance().getRedirectURL(action);
 			logger.debug("actionRedirect: " + actionRedirect);
 			// first, convert locale-specific action into a constant.  this isn't
 			// terribly important with the current code, but if anything is to
 			// be done with action values in the future it will be helpful.
 			if (checkAction(action, Utilities.resource("edit.action.save", request.getLocale()))) {
 				action = ACTION_SAVE;
 			} else if (checkAction(action, Utilities.resource("edit.action.append", request.getLocale()))) {
 				action = ACTION_APPEND;
 			} else if (checkAction(action, Utilities.resource("edit.action.preview", request.getLocale()))) {
 				action = ACTION_PREVIEW;
 			} else if (checkAction(action, Utilities.resource("edit.action.savetemplate", request.getLocale()))) {
 				action = ACTION_SAVE_TEMPLATE;
 			} else if (action.equals(Utilities.resource("edit.action.cancel", request.getLocale()))) {
 				action = ACTION_CANCEL;
 			}
 			if (actionRedirect != null) {
 				// Handle the layout pages in a cache: do not reload the for every
 				// request, just reload when an update is made on any page (SaveTopicServlet)
 				// This is for performance reasons.
 				//
 				// In the previous version the removeCachedContents() call was added in update
 				// methods; in this version, this didn't work due to this new
 				// implementation  by PseudoTopicHandler, so the
 				// checkActionAndRemoveCachedContentsIfNeeded method call was added.
 				//
 				// TODO: Simplify this servlet.
 				checkActionAndRemoveCachedContentsIfNeeded(action, request.getLocale());
 				logger.debug("Using redirect from pseudotopics actions: " + actionRedirect);
 				request.setAttribute(PARAMETER_ACTION, action);
 				dispatch(actionRedirect, request, response);
 				return;
 			} else if (action.equals(ACTION_SEARCH)) {
 				// a search request has been made
 				dispatch = request.getRequestDispatcher("/SearchServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_SAVE)) {
 				// a save request has been made
 				logger.debug("Dispatching save");
 				removeCachedContents();
 				dispatch = request.getRequestDispatcher("/SaveTopicServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_APPEND)) {
 				// a append template request has been made
 				logger.debug("Dispatching append template");
 				dispatch = request.getRequestDispatcher("/SaveTopicServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_PREVIEW)) {
 				// a save request has been made
 				logger.debug("Dispatching preview");
 				removeCachedContents();
 				request.setAttribute(ACTION_PREVIEW, "true");
 				dispatch = request.getRequestDispatcher("/EditServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_ADMIN)) {
 				// request to save admin values
 				logger.debug("Despatching admin servlet");
 				dispatch = request.getRequestDispatcher("/AdministrationServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_SAVE_TEMPLATE)) {
 				// save template
 				logger.debug("Despatching save template");
 				removeCachedContents();
 				dispatch = request.getRequestDispatcher("/SaveTemplateServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_RSS)) {
 				// save template
 				dispatch = request.getRequestDispatcher("/RSS");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_CANCEL)) {
 				// cancellation of edit
 				String topic = request.getParameter("topic");
 				try {
 					WikiBase.getInstance().unlockTopic(virtualWiki, topic);
 				} catch (Exception err) {
 					error(request, response, new WikiServletException(err.getMessage()));
 					return;
 				}
 				String next = "Wiki?" + topic;
 				removeCachedContents();
 				response.sendRedirect(response.encodeRedirectURL(next));
 				return;
 			} else if (action.equals(ACTION_SAVE_USER)) {
 				String username = request.getParameter("username");
 				if (username == null || username.trim().length() == 0) {
 					// FIXME: this is a fix for VQW-54, but it could be handled more elegantly
 					ResourceBundle messages = getMessages(request.getLocale());
 					error(request, response, new WikiServletException(messages.getString("exception.badusername")));
 					return;
 				}
 				Cookie c = Utilities.createUsernameCookie(username);
 				logger.debug("Delivering cookie: " + c.getName() + " " + c.getValue());
 				try {
 					response.addCookie(c);
 				} catch (Exception e) {
 					logger.warn(e.getMessage(), e);
 					ResourceBundle messages = getMessages(request.getLocale());
 					error(request, response, new WikiServletException(messages.getString("exception.badusername")));
 					return;
 				}
 				String topic = request.getParameter("topic");
 				request.setAttribute("title", "SetUsername");
 				if (topic == null || "".equals(topic)) {
 					try {
 						request.setAttribute("userList", WikiBase.getInstance().getUsergroupInstance().getListOfAllUsers());
 					} catch (Exception e) { }
 					dispatch = request.getRequestDispatcher(PseudoTopicHandler.getInstance().getRedirectURL("SetUsername"));
 					dispatch.forward(request, response);
 					return;
 				} else {
 					request.setAttribute("topic", topic);
 					dispatch = request.getRequestDispatcher("/EditServlet");
 					dispatch.forward(request, response);
 					return;
 				}
 			} else if (action.equals(ACTION_NOTIFY)) {
 				dispatch = request.getRequestDispatcher("/NotifyServlet");
 				dispatch.forward(request, response);
 				return;
 			} else if (action.equals(ACTION_MEMBER)) {
 				dispatch = request.getRequestDispatcher("/MemberServlet");
 				dispatch.forward(request, response);
 				return;
 			}
 			// in case, no action can be dispatched, go to a normal wiki page
 		}
 		dispatch = request.getRequestDispatcher("/WEB-INF/jsp/wiki.jsp");
 		dispatch.forward(request, response);
 	}
 
 	/**
 	 * if the action is Save, SaveTemplate or CancelFromEdit, clears the cache
 	 * and force the wiki to reload layout page elements
 	 * (topArea, bottomArea, leftMenu, etc.)
 	 */
 	private void checkActionAndRemoveCachedContentsIfNeeded(String action, Locale locale) {
 		if (action.equals(ACTION_SAVE) || action.equals(ACTION_SAVE_TEMPLATE) || action.equals(ACTION_CANCEL)) {
 			removeCachedContents();
 		}
 	}
 
 	/**
 	 *
 	 */
 	private boolean checkAction(String original, String test) {
 		if (original.equals(test)) {
 			return true;
 		}
 		// find in test the first character, which is not alpabetic
 		int count;
 		String test2 = test.toLowerCase();
 		for (count = 0; count < test.length(); count++) {
 			if (!(test2.charAt(count) >= 'a' && test2.charAt(count) <= 'z')) {
 				break;
 			}
 		}
 		count--;
 		if (count < 0) {
 			return false;
 		}
 		// fix by pcs_org
 		if (original.length() < count) {
 			return false;
 		}
 		if (original.substring(0, count).equals(test.substring(0, count))) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Mark all needles in a haystack, so that they can be replaced later. Take special care on HTML,
 	 * so that no needle is replaced inside a HTML tag.
 	 *
 	 * @param haystack The haystack to go through.
 	 * @param needle   The needle to search.
 	 * @return The haystack with all needles (outside HTML) marked with the char \u0000
 	 */
 	public static String markToReplaceOutsideHTML(String haystack, String needle) {
 		if (needle.length() == 0) {
 			return haystack;
 		}
 		StringBuffer sb = new StringBuffer();
 		boolean inHTMLmode = false;
 		int l = haystack.length();
 		for (int j = 0; j < l; j++) {
 			char c = haystack.charAt(j);
 			switch (c) {
 				case '<':
 					if (((j + 1) < l) && (haystack.charAt(j + 1) != ' ')) {
 						inHTMLmode = true;
 					}
 					break;
 				case '>':
 					if (inHTMLmode) {
 						inHTMLmode = false;
 					}
 					break;
 			}
 			if ((c == needle.charAt(0) || Math.abs(c - needle.charAt(0)) == 32) &&
 				!inHTMLmode) {
 				boolean ok = true;
 				if ((j + needle.length()) > l ||
 					!haystack.substring(j, j + needle.length()).equalsIgnoreCase(needle)) {
 					ok = false;
 				}
 				if (ok) {
 					sb.append('\u0000');
 					for (int k = 0; k < needle.length(); k++) {
 						sb.append(haystack.charAt(j + k));
 					}
 					j = j + needle.length() - 1;
 				} else {
 					sb.append(c);
 				}
 			} else {
 				sb.append(c);
 			}
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Replace all needles inside the text with their replacements.
 	 *
 	 * @param text		The text or haystack, where all needles are already marked with the unicode character \u0000
 	 * @param needle	  The needle to search
 	 * @param replacement The text, which replaces the needle
 	 * @return String containing the text with the needle replaced by the replacement.
 	 */
 	public static String replaceMarked(String text, String needle, String replacement) {
 		needle = '\u0000' + needle;
 		text = Utilities.replaceString(text, needle, replacement);
 		return text;
 	}
 }
