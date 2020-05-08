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
 package org.jamwiki.model;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Provides an object representing a Wiki log entry.
  */
 public class LogItem {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(LogItem.class.getName());
 	public static final int LOG_TYPE_ALL = -1;
 	public static final int LOG_TYPE_DELETE = 1;
 	public static final int LOG_TYPE_IMPORT = 2;
 	public static final int LOG_TYPE_MOVE = 3;
 	public static final int LOG_TYPE_PERMISSION = 4;
 	public static final int LOG_TYPE_UPLOAD = 6;
 	public static final int LOG_TYPE_USER_CREATION = 7;
 	public static Map<Integer, String> LOG_TYPES = new LinkedHashMap<Integer, String>();
 	static {
 		LOG_TYPES.put(LOG_TYPE_ALL, "log.caption.log.all");
 		LOG_TYPES.put(LOG_TYPE_DELETE, "log.caption.log.deletion");
 		LOG_TYPES.put(LOG_TYPE_IMPORT, "log.caption.log.import");
 		LOG_TYPES.put(LOG_TYPE_MOVE, "log.caption.log.move");
 		LOG_TYPES.put(LOG_TYPE_PERMISSION, "log.caption.log.permission");
 		LOG_TYPES.put(LOG_TYPE_UPLOAD, "log.caption.log.upload");
 		LOG_TYPES.put(LOG_TYPE_USER_CREATION, "log.caption.log.user");
 	}
 
 	private String logComment = null;
 	private Timestamp logDate = null;
 	private List<String> logParams = null;
 	private int logType = -1;
 	private Integer topicId = null;
 	private Integer topicVersionId = null;
 	private String userDisplayName = null;
 	private Integer userId = null;
 	private String virtualWiki = null;
 
 	/**
 	 *
 	 */
 	public LogItem() {
 	}
 
 	/**
 	 * Create a log item from a topic, topic version and author name.  If the topic
 	 * version is not valid for logging this method will return <code>null</code>.
 	 */
 	public static LogItem initLogItem(Topic topic, TopicVersion topicVersion, String authorName) {
 		LogItem logItem = new LogItem();
 		if (!topicVersion.isLoggable() || !topicVersion.isRecentChangeAllowed()) {
 			return null;
 		}
 		logItem.setLogParams(topicVersion.getVersionParams());
 		switch (topicVersion.getEditType()) {
 			case TopicVersion.EDIT_DELETE:
 				logItem.setLogType(LOG_TYPE_DELETE);
 				break;
 			case TopicVersion.EDIT_UNDELETE:
 				logItem.setLogType(LOG_TYPE_DELETE);
 				// add a param to distinguish undeletes from deletes
 				logItem.addLogParam(Integer.toString(TopicVersion.EDIT_UNDELETE));
 				break;
 			case TopicVersion.EDIT_MOVE:
 				if (StringUtils.isBlank(topic.getRedirectTo())) {
 					// add an additional check to ensure that reloading values does not create a bogus entry
 					return null;
 				}
 				logItem.setLogType(LOG_TYPE_MOVE);
 				break;
 			case TopicVersion.EDIT_PERMISSION:
 				logItem.setLogType(LOG_TYPE_PERMISSION);
 				break;
 			case TopicVersion.EDIT_IMPORT:
 				if (topic.getCurrentVersionId() != topicVersion.getTopicVersionId()) {
 					// only log the current version as an import item
 					return null;
 				}
 				logItem.setLogType(LOG_TYPE_IMPORT);
 				break;
 			case TopicVersion.EDIT_UPLOAD:
 				logItem.setLogType(LOG_TYPE_UPLOAD);
 				break;
 			default:
 				// not valid for logging
 				return null;
 		}
 		logItem.setLogComment(topicVersion.getEditComment());
 		logItem.setLogDate(topicVersion.getEditDate());
 		logItem.setTopicId(topic.getTopicId());
 		logItem.setTopicVersionId(topicVersion.getTopicVersionId());
 		logItem.setUserDisplayName(authorName);
 		logItem.setUserId(topicVersion.getAuthorId());
 		logItem.setVirtualWiki(topic.getVirtualWiki());
 		return logItem;
 	}
 
 	/**
 	 * Create a log item from a wiki user.
 	 */
 	public static LogItem initLogItem(WikiUser wikiUser, String virtualWiki) {
 		LogItem logItem = new LogItem();
 		logItem.setLogType(LOG_TYPE_USER_CREATION);
 		logItem.setLogDate(wikiUser.getCreateDate());
 		logItem.setUserDisplayName(wikiUser.getUsername());
 		logItem.setUserId(wikiUser.getUserId());
 		logItem.setVirtualWiki(virtualWiki);
 		// format user log is "New user account created" (no params needed)
 		return logItem;
 	}
 
 	/**
 	 *
 	 */
 	public static WikiMessage retrieveLogWikiMessage(int logType, String logParamString) {
 		String[] logParams = null;
 		if (!StringUtils.isBlank(logParamString)) {
 			logParams = logParamString.split("\\|");
 		}
 		WikiMessage logWikiMessage = null;
 		if (logType == LogItem.LOG_TYPE_DELETE) {
 			if (logParams != null && logParams.length >= 2 && StringUtils.equals(logParams[1], Integer.toString(TopicVersion.EDIT_UNDELETE))) {
 				logWikiMessage = new WikiMessage("log.message.undeletion", logParams);
 			} else {
 				logWikiMessage = new WikiMessage("log.message.deletion", logParams);
 			}
 		} else if (logType == LogItem.LOG_TYPE_IMPORT) {
 			logWikiMessage = new WikiMessage("log.message.import", logParams);
 		} else if (logType == LogItem.LOG_TYPE_MOVE) {
 			logWikiMessage = new WikiMessage("log.message.move", logParams);
 		} else if (logType == LogItem.LOG_TYPE_PERMISSION) {
 			logWikiMessage = new WikiMessage("log.message.permission", logParams);
 		} else if (logType == LogItem.LOG_TYPE_UPLOAD) {
 			logWikiMessage = new WikiMessage("log.message.upload", logParams);
 		} else if (logType == LogItem.LOG_TYPE_USER_CREATION) {
 			logWikiMessage = new WikiMessage("log.message.user");
 		}
 		return logWikiMessage;
 	}
 
 	/**
 	 *
 	 */
 	public String getLogComment() {
 		return this.logComment;
 	}
 
 	/**
 	 *
 	 */
 	public void setLogComment(String logComment) {
 		this.logComment = logComment;
 	}
 
 	/**
 	 *
 	 */
 	public Timestamp getLogDate() {
 		return this.logDate;
 	}
 
 	/**
 	 *
 	 */
 	public void setLogDate(Timestamp logDate) {
 		this.logDate = logDate;
 	}
 
 	/**
 	 * Utility method for adding a log param.
 	 */
 	private void addLogParam(String param) {
 		if (this.logParams == null) {
 			this.logParams = new ArrayList<String>();
 		}
 		this.logParams.add(param);
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getLogParams() {
 		return this.logParams;
 	}
 
 	/**
 	 *
 	 */
 	public void setLogParams(List<String> logParams) {
 		this.logParams = logParams;
 	}
 
 	/**
 	 * Utility method for converting the log params to a pipe-delimited string.
 	 */
 	public String getLogParamString() {
 		return Utilities.listToDelimitedString(this.logParams, "|");
 	}
 
 	/**
 	 * Utility method for converting a log params pipe-delimited string to a list.
 	 */
 	public void setLogParamString(String logParamsString) {
 		this.setLogParams(Utilities.delimitedStringToList(logParamsString, "|"));
 	}
 
 	/**
 	 *
 	 */
 	public int getLogType() {
 		return this.logType;
 	}
 
 	/**
 	 *
 	 */
 	public void setLogType(int logType) {
 		this.logType = logType;
 	}
 
 	/**
 	 * Utility method for retrieving the log type caption for the specific log type.
 	 */
 	public String getLogWikiLinkCaption() {
 		return LOG_TYPES.get(this.logType);
 	}
 
 	/**
 	 * Utility method for displaying a formatted log message specific to the log type and
 	 * params.
 	 */
 	public WikiMessage getLogWikiMessage() {
 		return LogItem.retrieveLogWikiMessage(this.getLogType(), this.getLogParamString());
 	}
 
 	/**
 	 *
 	 */
 	public Integer getTopicId() {
 		return this.topicId;
 	}
 
 	/**
 	 *
 	 */
 	public void setTopicId(Integer topicId) {
 		this.topicId = topicId;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getTopicVersionId() {
 		return this.topicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public void setTopicVersionId(Integer topicVersionId) {
 		this.topicVersionId = topicVersionId;
 	}
 
 	/**
 	 *
 	 */
 	public String getUserDisplayName() {
 		return this.userDisplayName;
 	}
 
 	/**
 	 *
 	 */
 	public void setUserDisplayName(String userDisplayName) {
 		this.userDisplayName = userDisplayName;
 	}
 
 	/**
 	 *
 	 */
 	public Integer getUserId() {
 		return this.userId;
 	}
 
 	/**
 	 *
 	 */
 	public void setUserId(Integer userId) {
 		this.userId = userId;
 	}
 
 	/**
 	 *
 	 */
 	public String getVirtualWiki() {
 		return this.virtualWiki;
 	}
 
 	/**
 	 *
 	 */
 	public void setVirtualWiki(String virtualWiki) {
 		this.virtualWiki = virtualWiki;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isDelete() {
 		return this.logType == LOG_TYPE_DELETE;
 	}
 
 	/**
 	 *
 	 */
	public boolean isImport() {
 		return this.logType == LOG_TYPE_IMPORT;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isMove() {
 		return this.logType == LOG_TYPE_MOVE;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isPermission() {
 		return this.logType == LOG_TYPE_PERMISSION;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isUpload() {
 		return this.logType == LOG_TYPE_UPLOAD;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isUser() {
 		return this.logType == LOG_TYPE_USER_CREATION;
 	}
 }
