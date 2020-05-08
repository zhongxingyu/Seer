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
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
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
 		switch (topicVersion.getEditType()) {
 			case TopicVersion.EDIT_DELETE:
 			case TopicVersion.EDIT_UNDELETE:
 				logItem.setLogType(LOG_TYPE_DELETE);
 				// format for delete log is "Topic {0} deleted"
 				logItem.addLogParam(topic.getName());
 				break;
 			case TopicVersion.EDIT_MOVE:
 				if (StringUtils.isBlank(topic.getRedirectTo())) {
 					// moves create two versions, one for the old topic name and one for the new
 					// topic name.  only the first needs a log item.
 					return null;
 				}
 				logItem.setLogType(LOG_TYPE_MOVE);
 				// format for move log is "Topic {0} renamed to {1}"
 				logItem.addLogParam(topic.getName());
 				logItem.addLogParam(topic.getRedirectTo());
 				break;
 			case TopicVersion.EDIT_PERMISSION:
 				logItem.setLogType(LOG_TYPE_PERMISSION);
 				// format for permission log is "Permissions updated for topic {0}"
 				logItem.addLogParam(topic.getName());
 				break;
 			case TopicVersion.EDIT_IMPORT:
				if (topic.getCurrentVersionId() != topicVersion.getTopicVersionId()) {
					// only log the current version as an import item
					return null;
				}
 				logItem.setLogType(LOG_TYPE_IMPORT);
 				// format for import log is "Topic {0} imported"
 				logItem.addLogParam(topic.getName());
 				break;
 			default:
 				if (topic.getTopicType() == Topic.TYPE_FILE || topic.getTopicType() == Topic.TYPE_IMAGE) {
 					logItem.setLogType(LOG_TYPE_UPLOAD);
 					// format user log is "File {0} uploaded"
 					logItem.addLogParam(topic.getName());
 					break;
 				}
 				// not valid for logging
 				return null;
 		}
 		logItem.setLogComment(topicVersion.getEditComment());
 		logItem.setLogDate(topicVersion.getEditDate());
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
 		if (this.logParams == null || this.logParams.isEmpty()) {
 			return null;
 		}
 		String result = "";
 		for (String logParam : this.logParams) {
 			if (result.length() > 0) {
 				result += "|";
 			}
 			result += logParam;
 		}
 		return result;
 	}
 
 	/**
 	 * Utility method for converting a log params pipe-delimited string to a list.
 	 */
 	public void setLogParamString(String logParamsString) {
 		if (!StringUtils.isBlank(logParamsString)) {
 			List<String> logParams = Arrays.asList(logParamsString.split("\\|"));
 			this.setLogParams(logParams);
 		}
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
