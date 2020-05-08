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
  * along with this program (gpl.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.servlets;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiMembers;
 import org.jamwiki.model.Topic;
 import org.jamwiki.persistency.PersistencyHandler;
 import org.jamwiki.persistency.db.DatabaseConnection;
 import org.jamwiki.persistency.db.DatabaseHandler;
 import org.jamwiki.persistency.db.DatabaseInit;
 import org.jamwiki.persistency.file.FileHandler;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.Utilities;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 /**
  * The <code>AdminServlet</code> servlet is the servlet which allows the administrator
  * to perform administrative actions on the wiki.
  */
 public class AdminServlet extends JAMWikiServlet implements Controller {
 
 	private static Logger logger = Logger.getLogger(AdminServlet.class.getName());
 
 	/**
 	 * This method handles the request after its parent class receives control.
 	 *
 	 * @param request - Standard HttpServletRequest object.
 	 * @param response - Standard HttpServletResponse object.
 	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
 	 */
 	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		ModelAndView next = new ModelAndView("wiki");
 		JAMWikiServlet.buildLayout(request, next);
 		String function = request.getParameter("function");
 		if (function == null) function = "";
 		// FIXME - hard coding of "function" values
 		if (!Utilities.isAdmin(request)) {
 			login(request, next);
 			return next;
 		}
 		if (function.equals("logout")) {
 			logout(request, next);
 			return next;
 		}
 		if (isTopic(request, "Special:Upgrade")) {
 			if (function.equals("Create")) {
 				upgradeCreate(request, next);
 			} else if (function.equals("Purge")) {
 				upgradePurge(request, next);
 			} else if (function.equals("Convert to File")) {
 				upgradeConvertToFile(request, next);
 			} else if (function.equals("Convert to Database")) {
 				upgradeConvertToDatabase(request, next);
 			} else if (function.equals("Load Recent Changes")) {
 				upgradeRecentChanges(request, next);
 			} else {
 				upgradeView(request, next);
 			}
 			return next;
 		}
 		if (isTopic(request, "Special:Delete")) {
 			if (function.equals("Delete")) {
 				delete(request, next);
 			} else {
 				deleteView(request, next);
 			}
 			return next;
 		}
 		if (function == null || function.length() == 0) {
 			view(request, next);
 		}
 		if (function.equals("refreshIndex")) {
 			refreshIndex(request, next);
 		}
 		if (function.equals("properties")) {
 			properties(request, next);
 		}
 		if (function.equals("clearEditLock")) {
 			clearEditLock(request, next);
 		}
 		if (function.equals("removeUser")) {
 			removeUser(request, next);
 		}
 		if (function.equals("addVirtualWiki")) {
 			addVirtualWiki(request, next);
 		}
 		if (function.equals("changePassword")) {
 			changePassword(request, next);
 		}
 		if (function.equals("panic")) {
 			panic(request, next);
 		}
 		if (function.equals("readOnly")) {
 			readOnly(request, next);
 		}
 		// FIXME - remove this
 		readOnlyList(request, next);
 		return next;
 	}
 
 	/**
 	 *
 	 */
 	private void addVirtualWiki(HttpServletRequest request, ModelAndView next) throws Exception {
 		String newWiki = request.getParameter("newVirtualWiki");
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		try {
 			logger.debug("Adding new Wiki: " + newWiki);
 			WikiBase.getInstance().addVirtualWiki(newWiki);
 			String message = Utilities.getMessage("admin.message.virtualwikiadded", request.getLocale());
 			next.addObject("message", message);
 			WikiBase.initialise(request.getLocale());
 			// refresh layout
 			JAMWikiServlet.buildLayout(request, next);
 		} catch (Exception e) {
 			logger.error("Failure while adding virtual wiki " + newWiki, e);
 			String message = "Failure while adding virtual wiki " + newWiki + ": " + e.getMessage();
 			next.addObject("message", message);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void changePassword(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		try {
 			String oldPassword = request.getParameter("oldPassword");
 			String newPassword = request.getParameter("newPassword");
 			String confirmPassword = request.getParameter("confirmPassword");
 			if (!Encryption.getEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD).equals(oldPassword)) {
 				String message = Utilities.getMessage("admin.message.oldpasswordincorrect", request.getLocale());
 				next.addObject("message", message);
 			} else if (!newPassword.equals(confirmPassword)) {
 				String message = Utilities.getMessage("admin.message.passwordsnomatch", request.getLocale());
 				next.addObject("message", message);
 			} else {
 				Encryption.setEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD, newPassword);
 				Environment.saveProperties();
 				String message = Utilities.getMessage("admin.message.passwordchanged", request.getLocale());
 				next.addObject("message", message);
 			}
 		} catch (Exception e) {
 			logger.error("Failure while changing password", e);
 			String message = "Failure while changing password: " + e.getMessage();
 			next.addObject("message", message);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void clearEditLock(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		try {
 			String topicName = request.getParameter("topic");
 			Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
 			WikiBase.getInstance().getHandler().unlockTopic(topic);
 			String message = Utilities.getMessage("admin.message.lockcleared", request.getLocale());
 			next.addObject("message", message);
 		} catch (Exception e) {
 			logger.error("Failure while clearing locks", e);
 			String message = "Failure while clearing locks: " + e.getMessage();
 			next.addObject("message", message);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void delete(HttpServletRequest request, ModelAndView next) throws Exception {
 		String topicName = JAMWikiServlet.getTopicFromRequest(request);
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		next.addObject(JAMWikiServlet.PARAMETER_TOPIC, topicName);
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_DELETE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Delete " + topicName);
 		try {
 			if (topicName == null) {
 				next.addObject("errorMessage", "No topic found");
 				return;
 			}
 			Topic topic = WikiBase.getInstance().getHandler().lookupTopic(virtualWiki, topicName);
 			WikiBase.getInstance().getHandler().delete(topic);
 			// FIXME - hard coding
 			next.addObject("message", "Topic " + topicName + " deleted successfully");
 		} catch (Exception e) {
 			logger.error("Failure while deleting topic " + topicName, e);
 			next.addObject("errorMessage", "Failure while deleting topic " + topicName + ": " + e.getMessage());
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void deleteView(HttpServletRequest request, ModelAndView next) throws Exception {
 		String topicName = JAMWikiServlet.getTopicFromRequest(request);
 		if (topicName == null) {
 			next.addObject("errorMessage", "No topic found");
 		}
 		next.addObject(JAMWikiServlet.PARAMETER_TOPIC, topicName);
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_DELETE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Delete " + topicName);
 	}
 
 	/**
 	 *
 	 */
 	private void login(HttpServletRequest request, ModelAndView next) throws Exception {
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		String page = JAMWikiServlet.getTopicFromURI(request);
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, Utilities.getMessage("login.title", request.getLocale()));
 		String redirect = Utilities.buildInternalLink(request.getContextPath(), virtualWiki, page);
 		if (request.getQueryString() != null) {
 			redirect += "?" + request.getQueryString();
 		}
 		next.addObject("redirect", redirect);
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_LOGIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Login");
 	}
 
 	/**
 	 *
 	 */
 	private void logout(HttpServletRequest request, ModelAndView next) throws Exception {
 		request.getSession().removeAttribute("admin");
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_LOGIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Login");
 	}
 
 	/**
 	 *
 	 */
 	private void panic(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		try {
 			WikiBase.getInstance().getHandler().panic();
 		} catch (Exception e) {
 			logger.error("Failure during panic reset", e);
 			String message = "Failure during panic reset: " + e.getMessage();
 			next.addObject("message", message);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void properties(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		try {
 			Encryption.togglePropertyEncryption(request.getParameter(Environment.PROP_BASE_ENCODE_PASSWORDS) != null);
 			Environment.setIntValue(
 				Environment.PROP_TOPIC_EDIT_TIME_OUT,
 				Integer.parseInt(request.getParameter(Environment.PROP_TOPIC_EDIT_TIME_OUT))
 			);
 			Environment.setValue(
 				Environment.PROP_RECENT_CHANGES_DAYS,
 				request.getParameter(Environment.PROP_RECENT_CHANGES_DAYS)
 			);
 			Environment.setValue(
 				Environment.PROP_TOPIC_MAXIMUM_BACKLINKS,
 				request.getParameter(Environment.PROP_TOPIC_MAXIMUM_BACKLINKS)
 			);
 			Environment.setIntValue(
 				Environment.PROP_SEARCH_INDEX_REFRESH_INTERVAL,
 				Integer.parseInt(request.getParameter(Environment.PROP_SEARCH_INDEX_REFRESH_INTERVAL))
 			);
 			Environment.setIntValue(
 				Environment.PROP_RECENT_CHANGES_REFRESH_INTERVAL,
 				Integer.parseInt(request.getParameter(Environment.PROP_RECENT_CHANGES_REFRESH_INTERVAL))
 			);
 			Environment.setValue(
 				Environment.PROP_EMAIL_SMTP_HOST,
 				request.getParameter(Environment.PROP_EMAIL_SMTP_HOST)
 			);
 			Environment.setValue(
 				Environment.PROP_EMAIL_SMTP_USERNAME,
 				request.getParameter(Environment.PROP_EMAIL_SMTP_USERNAME)
 			);
 			Encryption.setEncryptedProperty(
 				Environment.PROP_EMAIL_SMTP_PASSWORD,
 				request.getParameter(Environment.PROP_EMAIL_SMTP_PASSWORD)
 			);
 			Environment.setValue(
 				Environment.PROP_EMAIL_REPLY_ADDRESS,
 				request.getParameter(Environment.PROP_EMAIL_REPLY_ADDRESS)
 			);
 			Environment.setValue(
 				Environment.PROP_PARSER_NEW_LINE_BREAKS,
 				request.getParameter(Environment.PROP_PARSER_NEW_LINE_BREAKS)
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_TOPIC_VERSIONING_ON,
 				request.getParameter(Environment.PROP_TOPIC_VERSIONING_ON) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_PARSER_ALLOW_HTML,
 				request.getParameter(Environment.PROP_PARSER_ALLOW_HTML) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_TOPIC_FORCE_USERNAME,
 				request.getParameter(Environment.PROP_TOPIC_FORCE_USERNAME) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_TOPIC_ALLOW_VWIKI_LIST,
 				request.getParameter(Environment.PROP_TOPIC_ALLOW_VWIKI_LIST) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_SEARCH_ATTACHMENT_INDEXING_ENABLED,
 				request.getParameter(Environment.PROP_SEARCH_ATTACHMENT_INDEXING_ENABLED) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_SEARCH_EXTLINKS_INDEXING_ENABLED,
 				request.getParameter(Environment.PROP_SEARCH_EXTLINKS_INDEXING_ENABLED) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_ATTACH_TIMESTAMP,
 				request.getParameter(Environment.PROP_ATTACH_TIMESTAMP) != null
 			);
 			Environment.setValue(
 				Environment.PROP_ATTACH_UPLOAD_DIR,
 				request.getParameter(Environment.PROP_ATTACH_UPLOAD_DIR)
 			);
 			Environment.setValue(
 				Environment.PROP_BASE_FILE_DIR,
 				request.getParameter(Environment.PROP_BASE_FILE_DIR)
 			);
 			int persistenceType = Integer.parseInt(request.getParameter(Environment.PROP_BASE_PERSISTENCE_TYPE));
 			if (persistenceType == WikiBase.FILE) {
 				Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "FILE");
 			} else if (persistenceType == WikiBase.DATABASE) {
 				Environment.setValue(Environment.PROP_BASE_PERSISTENCE_TYPE, "DATABASE");
 			}
 			if (request.getParameter(Environment.PROP_DB_DRIVER) != null) {
 				Environment.setValue(
 					Environment.PROP_DB_DRIVER,
 					request.getParameter(Environment.PROP_DB_DRIVER)
 				);
 				Environment.setValue(
 					Environment.PROP_DB_URL,
 					request.getParameter(Environment.PROP_DB_URL)
 				);
 				Environment.setValue(
 					Environment.PROP_DB_USERNAME,
 					request.getParameter(Environment.PROP_DB_USERNAME)
 				);
 				Encryption.setEncryptedProperty(
 					Environment.PROP_DB_PASSWORD,
 					request.getParameter(Environment.PROP_DB_PASSWORD)
 				);
 				Environment.setIntValue(
 					Environment.PROP_DBCP_MAX_ACTIVE,
 					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_MAX_ACTIVE))
 				);
 				Environment.setIntValue(
 					Environment.PROP_DBCP_MAX_IDLE,
 					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_MAX_IDLE))
 				);
 				Environment.setBooleanValue(
 					Environment.PROP_DBCP_TEST_ON_BORROW,
 					request.getParameter(Environment.PROP_DBCP_TEST_ON_BORROW) != null
 				);
 				Environment.setBooleanValue(
 					Environment.PROP_DBCP_TEST_ON_RETURN,
 					request.getParameter(Environment.PROP_DBCP_TEST_ON_RETURN) != null
 				);
 				Environment.setBooleanValue(
 					Environment.PROP_DBCP_TEST_WHILE_IDLE,
 					request.getParameter(Environment.PROP_DBCP_TEST_WHILE_IDLE) != null
 				);
 				Environment.setIntValue(
 					Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME,
 					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_MIN_EVICTABLE_IDLE_TIME))
 				);
 				Environment.setIntValue(
 					Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS,
 					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_TIME_BETWEEN_EVICTION_RUNS))
 				);
 				Environment.setIntValue(
 					Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN,
 					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_NUM_TESTS_PER_EVICTION_RUN))
 				);
 				Environment.setIntValue(
 					Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION,
 					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_WHEN_EXHAUSTED_ACTION))
 				);
 				Environment.setValue(
 					Environment.PROP_DBCP_VALIDATION_QUERY,
 					request.getParameter(Environment.PROP_DBCP_VALIDATION_QUERY)
 				);
 				Environment.setBooleanValue(
 					Environment.PROP_DBCP_REMOVE_ABANDONED,
 					request.getParameter(Environment.PROP_DBCP_REMOVE_ABANDONED) != null
 				);
 				Environment.setIntValue(
 					Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT,
 					Integer.parseInt(request.getParameter(Environment.PROP_DBCP_REMOVE_ABANDONED_TIMEOUT))
 				);
 				Environment.setBooleanValue(
 					Environment.PROP_DBCP_LOG_ABANDONED,
 					request.getParameter(Environment.PROP_DBCP_LOG_ABANDONED) != null
 				);
 			}
 			Environment.setBooleanValue(
 				Environment.PROP_TOPIC_USE_PREVIEW,
 				request.getParameter(Environment.PROP_TOPIC_USE_PREVIEW) != null
 			);
 			Environment.setValue(
 				Environment.PROP_BASE_DEFAULT_TOPIC,
 				request.getParameter(Environment.PROP_BASE_DEFAULT_TOPIC)
 			);
 			Environment.setValue(
 				Environment.PROP_PARSER_CLASS,
 				request.getParameter(Environment.PROP_PARSER_CLASS)
 			);
 			int maxFileSizeInKB = Integer.parseInt(request.getParameter(Environment.PROP_ATTACH_MAX_FILE_SIZE));
 			Environment.setIntValue(
 				Environment.PROP_ATTACH_MAX_FILE_SIZE,
 				maxFileSizeInKB * 1000
 			);
 			Environment.setValue(
 				Environment.PROP_ATTACH_TYPE,
 				request.getParameter(Environment.PROP_ATTACH_TYPE)
 			);
 			if (request.getParameter(Environment.PROP_DB_TYPE) != null) {
 				Environment.setValue(
 					Environment.PROP_DB_TYPE,
 					request.getParameter(Environment.PROP_DB_TYPE)
 				);
 			}
 			if (request.getParameter(Environment.PROP_BASE_SERVER_HOSTNAME) !=  null && !request.getParameter(Environment.PROP_BASE_SERVER_HOSTNAME).equals("")) {
 				Environment.setValue(
 					Environment.PROP_BASE_SERVER_HOSTNAME,
 					request.getParameter(Environment.PROP_BASE_SERVER_HOSTNAME)
 				);
 			} else {
 				Environment.setValue(Environment.PROP_BASE_SERVER_HOSTNAME, "");
 			}
 			Environment.setValue(
 				Environment.PROP_FILE_ENCODING,
 				request.getParameter(Environment.PROP_FILE_ENCODING)
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_PARSER_SEPARATE_WIKI_TITLE_WORDS,
 				request.getParameter(Environment.PROP_PARSER_SEPARATE_WIKI_TITLE_WORDS) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_PARSER_TOC,
 				request.getParameter(Environment.PROP_PARSER_TOC) != null
 			);
 			Environment.setBooleanValue(
 				Environment.PROP_EMAIL_SUPPRESS_NOTIFY_WITHIN_SAME_DAY,
 				request.getParameter(Environment.PROP_EMAIL_SUPPRESS_NOTIFY_WITHIN_SAME_DAY) != null
 			);
 			int membershipType = Integer.parseInt(request.getParameter(Environment.PROP_USERGROUP_TYPE));
 			String usergroupType;
 			if (membershipType == WikiBase.LDAP) {
 				usergroupType = "LDAP";
 			} else if (membershipType == WikiBase.DATABASE) {
 				usergroupType = "DATABASE";
 			} else {
 				usergroupType = "0";
 			}
 			Environment.setValue(Environment.PROP_USERGROUP_TYPE, usergroupType);
 			String[] autoFill = {
 				Environment.PROP_USERGROUP_FACTORY,
 				Environment.PROP_USERGROUP_URL,
 				Environment.PROP_USERGROUP_USERNAME,
 				Environment.PROP_USERGROUP_PASSWORD,
 				Environment.PROP_USERGROUP_BASIC_SEARCH,
 				Environment.PROP_USERGROUP_SEARCH_RESTRICTIONS,
 				Environment.PROP_USERGROUP_USERID_FIELD,
 				Environment.PROP_USERGROUP_FULLNAME_FIELD,
 				Environment.PROP_USERGROUP_MAIL_FIELD,
 				Environment.PROP_USERGROUP_DETAILVIEW
 			};
 			for (int i = 0; i < autoFill.length; i++) {
 				if (request.getParameter(autoFill[i]) != null) {
 					if (autoFill[i].equals(Environment.PROP_USERGROUP_PASSWORD)) {
 						Encryption.setEncryptedProperty(
 							Environment.PROP_USERGROUP_PASSWORD,
 							request.getParameter(autoFill[i])
 						);
 					} else {
 						Environment.setValue(autoFill[i], request.getParameter(autoFill[i]));
 					}
 				}
 			}
 			if (Environment.getValue(Environment.PROP_BASE_FILE_DIR) == null) {
 				// if home directory set empty, use system home directory
 				String dir = System.getProperty("user.home") + System.getProperty("file.separator") + "wiki";
 				Environment.setValue(Environment.PROP_BASE_FILE_DIR, dir);
 			}
 			if (WikiBase.getPersistenceType() == WikiBase.DATABASE) {
 				// initialize connection pool in its own try-catch to avoid an error
 				// causing property values not to be saved.
 				try {
 					DatabaseConnection.setPoolInitialized(false);
 				} catch (Exception e) {
 					String message = e.getMessage();
 					next.addObject("message", message);
 				}
 			}
 			Environment.saveProperties();
 			WikiBase.initialise(request.getLocale());
 			// refresh layout
 			JAMWikiServlet.buildLayout(request, next);
 			String message = Utilities.getMessage("admin.message.changessaved", request.getLocale());
 			next.addObject("message", message);
 		} catch (Exception e) {
 			logger.error("Failure while processing property values", e);
 			String message = "Failure while processing property values: " + e.getMessage();
 			next.addObject("message", message);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void readOnly(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		if (request.getParameter("addReadOnly") != null) {
 			String topicName = request.getParameter("readOnlyTopic");
 			WikiBase.getInstance().getHandler().addReadOnlyTopic(virtualWiki, topicName);
 		}
 		if (request.getParameter("removeReadOnly") != null) {
 			String[] topics = request.getParameterValues("markRemove");
 			for (int i = 0; i < topics.length; i++) {
 				String topicName = topics[i];
 				WikiBase.getInstance().getHandler().removeReadOnlyTopic(virtualWiki, topicName);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void readOnlyList(HttpServletRequest request, ModelAndView next) throws Exception {
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		Collection readOnlyTopics = new ArrayList();
 		try {
 			readOnlyTopics = WikiBase.getInstance().getHandler().getReadOnlyTopics(virtualWiki);
 			next.addObject("readOnlyTopics", readOnlyTopics);
 		} catch (Exception e) {
 			// Ignore database error - probably just an invalid setting, the
 			// user may not have config'd yet
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void refreshIndex(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		try {
 			WikiBase.getInstance().getSearchEngineInstance().refreshIndex();
 			String message = Utilities.getMessage("admin.message.indexrefreshed", request.getLocale());
 			next.addObject("message", message);
 		} catch (Exception e) {
 			logger.error("Failure while refreshing search index", e);
 			String message = "Failure while refreshing search index: " + e.getMessage();
 			next.addObject("message", message);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void removeUser(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 		String virtualWiki = JAMWikiServlet.getVirtualWikiFromURI(request);
 		String user = request.getParameter("userName");
 		try {
 			WikiMembers members = WikiBase.getInstance().getWikiMembersInstance(virtualWiki);
 			if (members.removeMember(user)) {
 				String message = user + Utilities.getMessage("admin.message.userremoved.success", request.getLocale());
 				next.addObject("message", message);
 			} else {
 				String message = user + Utilities.getMessage("admin.message.userremoved.failure", request.getLocale());
 				next.addObject("message", message);
 			}
 		} catch (Exception e) {
 			logger.error("Failure while removing user " + user, e);
 			String message = "Failure while removing user " + user + ": " + e.getMessage();
 			next.addObject("message", message);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void upgradeConvertToDatabase(HttpServletRequest request, ModelAndView next) throws Exception {
 		try {
 			FileHandler fromHandler = new FileHandler();
 			DatabaseHandler toHandler = new DatabaseHandler();
 			Vector messages = PersistencyHandler.convert(fromHandler, toHandler);
 			next.addObject("message", "Database values successfully written to files");
 			next.addObject("messages", messages);
 		} catch (Exception e) {
 			logger.error("Failure while executing database-to-file conversion", e);
 			next.addObject("errorMessage", "Failure while executing database-to-file-conversion: " + e.getMessage());
 		}
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_UPGRADE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Upgrade");
 	}
 
 	/**
 	 *
 	 */
 	private void upgradeConvertToFile(HttpServletRequest request, ModelAndView next) throws Exception {
 		try {
 			FileHandler toHandler = new FileHandler();
 			DatabaseHandler fromHandler = new DatabaseHandler();
 			Vector messages = PersistencyHandler.convert(fromHandler, toHandler);
 			next.addObject("message", "Database values successfully written to files");
 			next.addObject("messages", messages);
 		} catch (Exception e) {
 			logger.error("Failure while executing database-to-file conversion", e);
 			next.addObject("errorMessage", "Failure while executing database-to-file-conversion: " + e.getMessage());
 		}
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_UPGRADE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Upgrade");
 	}
 
 	/**
 	 *
 	 */
 	private void upgradeCreate(HttpServletRequest request, ModelAndView next) throws Exception {
 		try {
 			DatabaseInit.initialize();
 			next.addObject("message", "Database tables successfully created");
 		} catch (Exception e) {
 			logger.error("Failure while executing database creation", e);
 			next.addObject("errorMessage", "Failure while executing database creation: " + e.getMessage());
 		}
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_UPGRADE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Upgrade");
 	}
 
 	/**
 	 *
 	 */
 	private void upgradePurge(HttpServletRequest request, ModelAndView next) throws Exception {
 		try {
 			DatabaseInit.cleanup();
 			next.addObject("message", "Database tables successfully purged");
 		} catch (Exception e) {
 			logger.error("Failure while executing database cleanup", e);
 			next.addObject("errorMessage", "Failure while executing database cleanup: " + e.getMessage());
 		}
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_UPGRADE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Upgrade");
 	}
 
 	/**
 	 *
 	 */
 	private void upgradeRecentChanges(HttpServletRequest request, ModelAndView next) throws Exception {
 		try {
 			// FIXME - database specific
 			org.jamwiki.persistency.db.DatabaseHandler.loadRecentChanges();
 			next.addObject("message", "Recent changes successfully loaded");
 		} catch (Exception e) {
 			logger.error("Failure while loading recent changes", e);
 			next.addObject("errorMessage", "Failure while loading recent changes: " + e.getMessage());
 		}
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_UPGRADE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Upgrade");
 	}
 
 	/**
 	 *
 	 */
 	private void upgradeView(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN_UPGRADE);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Upgrade");
 	}
 
 	/**
 	 *
 	 */
 	private void view(HttpServletRequest request, ModelAndView next) throws Exception {
 		next.addObject(JAMWikiServlet.PARAMETER_ACTION, JAMWikiServlet.ACTION_ADMIN);
 		next.addObject(JAMWikiServlet.PARAMETER_SPECIAL, new Boolean(true));
 		next.addObject(JAMWikiServlet.PARAMETER_TITLE, "Special:Admin");
 	}
 }
