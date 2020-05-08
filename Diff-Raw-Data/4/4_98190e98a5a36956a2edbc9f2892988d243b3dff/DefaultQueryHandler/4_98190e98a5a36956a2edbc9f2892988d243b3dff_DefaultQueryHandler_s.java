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
 package org.jamwiki.persistency.db;
 
 import java.sql.Connection;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Properties;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.persistency.db.DatabaseHandler;
 import org.jamwiki.utils.Utilities;
 
 /**
  *
  */
 public class DefaultQueryHandler implements QueryHandler {
 
 	private static Logger logger = Logger.getLogger(DefaultQueryHandler.class.getName());
 	protected static final String SQL_PROPERTY_FILE_NAME = "sql.ansi.properties";
 
 	protected static String STATEMENT_CREATE_VIRTUAL_WIKI_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_USER_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_USER_INFO_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_VERSION_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_FILE_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE = null;
 	protected static String STATEMENT_CREATE_IMAGE_TABLE = null;
 	protected static String STATEMENT_CREATE_RECENT_CHANGE_TABLE = null;
 	protected static String STATEMENT_DELETE_RECENT_CHANGES = null;
 	protected static String STATEMENT_DROP_VIRTUAL_WIKI_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_USER_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_USER_INFO_TABLE = null;
 	protected static String STATEMENT_DROP_TOPIC_TABLE = null;
 	protected static String STATEMENT_DROP_TOPIC_VERSION_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_FILE_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_FILE_VERSION_TABLE = null;
 	protected static String STATEMENT_DROP_IMAGE_TABLE = null;
 	protected static String STATEMENT_DROP_RECENT_CHANGE_TABLE = null;
 	protected static String STATEMENT_INSERT_TOPIC = null;
 	protected static String STATEMENT_INSERT_TOPIC_VERSION = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGE = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGES = null;
 	protected static String STATEMENT_INSERT_VIRTUAL_WIKI = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE_VERSION = null;
 	protected static String STATEMENT_INSERT_WIKI_USER = null;
 	protected static String STATEMENT_INSERT_WIKI_USER_INFO = null;
 	protected static String STATEMENT_SELECT_RECENT_CHANGES = null;
 	protected static String STATEMENT_SELECT_TOPIC = null;
 	protected static String STATEMENT_SELECT_TOPICS = null;
 	protected static String STATEMENT_SELECT_TOPIC_READ_ONLY = null;
 	protected static String STATEMENT_SELECT_TOPIC_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSIONS = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION_LAST = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_VIRTUAL_WIKIS = null;
 	protected static String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_TOPIC_NAMES = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_VERSIONS = null;
 	protected static String STATEMENT_SELECT_WIKI_USER = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_PASSWORD = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_LOGIN = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_LOGINS = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_SEQUENCE = null;
 	protected static String STATEMENT_UPDATE_TOPIC = null;
 	protected static String STATEMENT_UPDATE_VIRTUAL_WIKI = null;
 	protected static String STATEMENT_UPDATE_WIKI_FILE = null;
 	protected static String STATEMENT_UPDATE_WIKI_USER = null;
 	protected static String STATEMENT_UPDATE_WIKI_USER_INFO = null;
 
 	/**
 	 *
 	 */
 	protected DefaultQueryHandler() {
 	}
 
 	/**
 	 *
 	 */
 	public void createTables(Connection conn) throws Exception {
 		WikiPreparedStatement stmt = null;
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_VIRTUAL_WIKI_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_INFO_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_IMAGE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_RECENT_CHANGE_TABLE, conn);
 	}
 
 	/**
 	 *
 	 */
 	public void dropTables(Connection conn) {
 		// note that this method is called during creation failures, so be careful to
 		// catch errors that might result from a partial failure during install.  also
 		// note that the coding style violation here is intentional since it makes the
 		// actual work of the method more obvious.
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_RECENT_CHANGE_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_IMAGE_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_VERSION_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_VERSION_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_USER_INFO_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_USER_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_VIRTUAL_WIKI_TABLE, conn);
 		} catch (Exception e) { logger.error(e.getMessage()); }
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getAllTopicNames(String virtualWiki) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(virtualWiki);
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPICS);
 		stmt.setInt(1, virtualWikiId);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getAllTopicVersions(Topic topic, boolean descending) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSIONS);
 		stmt.setInt(1, topic.getTopicId());
 		// FIXME - sort order ignored
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getAllWikiFileTopicNames(String virtualWiki) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(virtualWiki);
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_FILE_TOPIC_NAMES);
 		stmt.setInt(1, virtualWikiId);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getAllWikiFileVersions(WikiFile wikiFile, boolean descending) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_FILE_VERSIONS);
 		// FIXME - sort order ignored
 		stmt.setInt(1, wikiFile.getFileId());
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getAllWikiUserLogins() throws Exception {
 		return DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_LOGINS);
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getReadOnlyTopics(String virtualWiki) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(virtualWiki);
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_READ_ONLY);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setInt(2, 1);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getRecentChanges(String virtualWiki, int num, boolean descending) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_RECENT_CHANGES);
 		stmt.setString(1, virtualWiki);
 		stmt.setInt(2, num);
 		// FIXME - sort order ignored
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getUserContributions(String virtualWiki, String userString, int num, boolean descending) throws Exception {
 		WikiPreparedStatement stmt = null;
 		if (Utilities.isIpAddress(userString)) {
 			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
 		} else {
 			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
 		}
 		stmt.setString(1, virtualWiki);
 		stmt.setString(2, userString);
 		stmt.setInt(3, num);
 		// FIXME - sort order ignored
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getVirtualWikis() throws Exception {
 		return DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKIS);
 	}
 
 	/**
 	 *
 	 */
 	protected void init(Properties props) {
 		STATEMENT_CREATE_VIRTUAL_WIKI_TABLE      = props.getProperty("STATEMENT_CREATE_VIRTUAL_WIKI_TABLE");
 		STATEMENT_CREATE_WIKI_USER_TABLE         = props.getProperty("STATEMENT_CREATE_WIKI_USER_TABLE");
 		STATEMENT_CREATE_WIKI_USER_INFO_TABLE    = props.getProperty("STATEMENT_CREATE_WIKI_USER_INFO_TABLE");
 		STATEMENT_CREATE_TOPIC_TABLE             = props.getProperty("STATEMENT_CREATE_TOPIC_TABLE");
 		STATEMENT_CREATE_TOPIC_VERSION_TABLE     = props.getProperty("STATEMENT_CREATE_TOPIC_VERSION_TABLE");
 		STATEMENT_CREATE_WIKI_FILE_TABLE         = props.getProperty("STATEMENT_CREATE_WIKI_FILE_TABLE");
 		STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE = props.getProperty("STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE");
 		STATEMENT_CREATE_IMAGE_TABLE             = props.getProperty("STATEMENT_CREATE_IMAGE_TABLE");
 		STATEMENT_CREATE_RECENT_CHANGE_TABLE     = props.getProperty("STATEMENT_CREATE_RECENT_CHANGE_TABLE");
 		STATEMENT_DELETE_RECENT_CHANGES          = props.getProperty("STATEMENT_DELETE_RECENT_CHANGES");
 		STATEMENT_DROP_VIRTUAL_WIKI_TABLE        = props.getProperty("STATEMENT_DROP_VIRTUAL_WIKI_TABLE");
 		STATEMENT_DROP_WIKI_USER_TABLE           = props.getProperty("STATEMENT_DROP_WIKI_USER_TABLE");
 		STATEMENT_DROP_WIKI_USER_INFO_TABLE      = props.getProperty("STATEMENT_DROP_WIKI_USER_INFO_TABLE");
 		STATEMENT_DROP_TOPIC_TABLE               = props.getProperty("STATEMENT_DROP_TOPIC_TABLE");
 		STATEMENT_DROP_TOPIC_VERSION_TABLE       = props.getProperty("STATEMENT_DROP_TOPIC_VERSION_TABLE");
 		STATEMENT_DROP_WIKI_FILE_TABLE           = props.getProperty("STATEMENT_DROP_WIKI_FILE_TABLE");
 		STATEMENT_DROP_WIKI_FILE_VERSION_TABLE   = props.getProperty("STATEMENT_DROP_WIKI_FILE_VERSION_TABLE");
 		STATEMENT_DROP_IMAGE_TABLE               = props.getProperty("STATEMENT_DROP_IMAGE_TABLE");
 		STATEMENT_DROP_RECENT_CHANGE_TABLE       = props.getProperty("STATEMENT_DROP_RECENT_CHANGE_TABLE");
 		STATEMENT_INSERT_TOPIC                   = props.getProperty("STATEMENT_INSERT_TOPIC");
 		STATEMENT_INSERT_TOPIC_VERSION           = props.getProperty("STATEMENT_INSERT_TOPIC_VERSION");
 		STATEMENT_INSERT_RECENT_CHANGE           = props.getProperty("STATEMENT_INSERT_RECENT_CHANGE");
 		STATEMENT_INSERT_RECENT_CHANGES          = props.getProperty("STATEMENT_INSERT_RECENT_CHANGES");
 		STATEMENT_INSERT_VIRTUAL_WIKI            = props.getProperty("STATEMENT_INSERT_VIRTUAL_WIKI");
 		STATEMENT_INSERT_WIKI_FILE               = props.getProperty("STATEMENT_INSERT_WIKI_FILE");
 		STATEMENT_INSERT_WIKI_FILE_VERSION       = props.getProperty("STATEMENT_INSERT_WIKI_FILE_VERSION");
 		STATEMENT_INSERT_WIKI_USER               = props.getProperty("STATEMENT_INSERT_WIKI_USER");
 		STATEMENT_INSERT_WIKI_USER_INFO          = props.getProperty("STATEMENT_INSERT_WIKI_USER_INFO");
 		STATEMENT_SELECT_RECENT_CHANGES          = props.getProperty("STATEMENT_SELECT_RECENT_CHANGES");
 		STATEMENT_SELECT_TOPIC                   = props.getProperty("STATEMENT_SELECT_TOPIC");
 		STATEMENT_SELECT_TOPICS                  = props.getProperty("STATEMENT_SELECT_TOPICS");
 		STATEMENT_SELECT_TOPIC_READ_ONLY         = props.getProperty("STATEMENT_SELECT_TOPIC_READ_ONLY");
 		STATEMENT_SELECT_TOPIC_SEQUENCE          = props.getProperty("STATEMENT_SELECT_TOPIC_SEQUENCE");
 		STATEMENT_SELECT_TOPIC_VERSION           = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION");
 		STATEMENT_SELECT_TOPIC_VERSIONS          = props.getProperty("STATEMENT_SELECT_TOPIC_VERSIONS");
 		STATEMENT_SELECT_TOPIC_VERSION_LAST      = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION_LAST");
 		STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE  = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE");
 		STATEMENT_SELECT_VIRTUAL_WIKIS           = props.getProperty("STATEMENT_SELECT_VIRTUAL_WIKIS");
 		STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE   = props.getProperty("STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE");
 		STATEMENT_SELECT_WIKI_FILE               = props.getProperty("STATEMENT_SELECT_WIKI_FILE");
 		STATEMENT_SELECT_WIKI_FILE_SEQUENCE      = props.getProperty("STATEMENT_SELECT_WIKI_FILE_SEQUENCE");
 		STATEMENT_SELECT_WIKI_FILE_TOPIC_NAMES   = props.getProperty("STATEMENT_SELECT_WIKI_FILE_TOPIC_NAMES");
 		STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE = props.getProperty("STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE");
 		STATEMENT_SELECT_WIKI_FILE_VERSIONS      = props.getProperty("STATEMENT_SELECT_WIKI_FILE_VERSIONS");
 		STATEMENT_SELECT_WIKI_USER               = props.getProperty("STATEMENT_SELECT_WIKI_USER");
 		STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS = props.getProperty("STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS");
 		STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN = props.getProperty("STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN");
 		STATEMENT_SELECT_WIKI_USER_LOGIN         = props.getProperty("STATEMENT_SELECT_WIKI_USER_LOGIN");
 		STATEMENT_SELECT_WIKI_USER_LOGINS        = props.getProperty("STATEMENT_SELECT_WIKI_USER_LOGINS");
 		STATEMENT_SELECT_WIKI_USER_PASSWORD      = props.getProperty("STATEMENT_SELECT_WIKI_USER_PASSWORD");
 		STATEMENT_SELECT_WIKI_USER_SEQUENCE      = props.getProperty("STATEMENT_SELECT_WIKI_USER_SEQUENCE");
 		STATEMENT_UPDATE_TOPIC                   = props.getProperty("STATEMENT_UPDATE_TOPIC");
 		STATEMENT_UPDATE_VIRTUAL_WIKI            = props.getProperty("STATEMENT_UPDATE_VIRTUAL_WIKI");
 		STATEMENT_UPDATE_WIKI_FILE               = props.getProperty("STATEMENT_UPDATE_WIKI_FILE");
 		STATEMENT_UPDATE_WIKI_USER               = props.getProperty("STATEMENT_UPDATE_WIKI_USER");
 		STATEMENT_UPDATE_WIKI_USER_INFO          = props.getProperty("STATEMENT_UPDATE_WIKI_USER_INFO");
 	}
 
 	/**
 	 *
 	 */
 	public void insertRecentChange(RecentChange change, Connection conn) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(change.getVirtualWiki());
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_RECENT_CHANGE);
 		stmt.setInt(1, change.getTopicVersionId());
 		if (change.getPreviousTopicVersionId() != null) {
 			stmt.setInt(2, change.getPreviousTopicVersionId().intValue());
 		} else {
 			stmt.setNull(2, Types.INTEGER);
 		}
 		stmt.setInt(3, change.getTopicId());
 		stmt.setString(4, change.getTopicName());
 		stmt.setTimestamp(5, change.getEditDate());
 		stmt.setString(6, change.getEditComment());
 		if (change.getAuthorId() != null) {
 			stmt.setInt(7, change.getAuthorId().intValue());
 		} else {
 			stmt.setNull(7, Types.INTEGER);
 		}
 		stmt.setString(8, change.getAuthorName());
 		stmt.setInt(9, change.getEditType());
 		stmt.setInt(10, virtualWikiId);
 		stmt.setString(11, change.getVirtualWiki());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void insertTopic(Topic topic, Connection conn) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(topic.getVirtualWiki());
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_TOPIC);
 		stmt.setInt(1, topic.getTopicId());
 		stmt.setInt(2, virtualWikiId);
 		stmt.setString(3, topic.getName());
 		stmt.setInt(4, topic.getTopicType());
 		stmt.setInt(5, (topic.getReadOnly() ? 1 : 0));
 		stmt.setString(6, topic.getTopicContent());
 		stmt.setInt(7, (topic.getDeleted() ? 1 : 0));
 		stmt.setInt(8, (topic.getAdminOnly() ? 1 : 0));
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void insertTopicVersion(TopicVersion topicVersion, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_TOPIC_VERSION);
 		stmt.setInt(1, topicVersion.getTopicVersionId());
 		stmt.setInt(2, topicVersion.getTopicId());
 		stmt.setString(3, topicVersion.getEditComment());
 		stmt.setString(4, topicVersion.getVersionContent());
 		if (topicVersion.getAuthorId() != null) {
 			stmt.setInt(5, topicVersion.getAuthorId().intValue());
 		} else {
 			stmt.setNull(5, Types.INTEGER);
 		}
 		stmt.setInt(6, topicVersion.getEditType());
 		stmt.setString(7, topicVersion.getAuthorIpAddress());
 		stmt.setTimestamp(8, topicVersion.getEditDate());
 		if (topicVersion.getPreviousTopicVersionId() != null) {
 			stmt.setInt(9, topicVersion.getPreviousTopicVersionId().intValue());
 		} else {
 			stmt.setNull(9, Types.INTEGER);
 		}
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void insertVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
 		stmt.setInt(1, virtualWiki.getVirtualWikiId());
 		stmt.setString(2, virtualWiki.getName());
 		stmt.setString(3, virtualWiki.getDefaultTopicName());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiFile(WikiFile wikiFile, Connection conn) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(wikiFile.getVirtualWiki());
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_FILE);
 		stmt.setInt(1, wikiFile.getFileId());
 		stmt.setInt(2, virtualWikiId);
 		stmt.setString(3, wikiFile.getFileName());
 		stmt.setString(4, wikiFile.getUrl());
 		stmt.setString(5, wikiFile.getMimeType());
 		stmt.setInt(6, wikiFile.getTopicId());
 		stmt.setInt(7, (wikiFile.getDeleted() ? 1 : 0));
 		stmt.setInt(8, (wikiFile.getReadOnly() ? 1 : 0));
 		stmt.setInt(9, (wikiFile.getAdminOnly() ? 1 : 0));
 		stmt.setInt(10, wikiFile.getFileSize());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiFileVersion(WikiFileVersion wikiFileVersion, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_FILE_VERSION);
 		stmt.setInt(1, wikiFileVersion.getFileVersionId());
 		stmt.setInt(2, wikiFileVersion.getFileId());
 		stmt.setString(3, wikiFileVersion.getUploadComment());
 		stmt.setString(4, wikiFileVersion.getUrl());
 		if (wikiFileVersion.getAuthorId() != null) {
 			stmt.setInt(5, wikiFileVersion.getAuthorId().intValue());
 		} else {
 			stmt.setNull(5, Types.INTEGER);
 		}
 		stmt.setString(6, wikiFileVersion.getAuthorIpAddress());
 		stmt.setTimestamp(7, wikiFileVersion.getUploadDate());
 		stmt.setString(8, wikiFileVersion.getMimeType());
 		stmt.setInt(9, wikiFileVersion.getFileSize());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiUser(WikiUser user, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER);
 		stmt.setInt(1, user.getUserId());
 		stmt.setString(2, user.getLogin());
 		stmt.setString(3, user.getDisplayName());
 		stmt.setTimestamp(4, user.getCreateDate());
 		stmt.setTimestamp(5, user.getLastLoginDate());
 		stmt.setString(6, user.getCreateIpAddress());
 		stmt.setString(7, user.getLastLoginIpAddress());
 		stmt.setInt(8, (user.getAdmin() ? 1 : 0));
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiUserInfo(WikiUser user, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_INSERT_WIKI_USER_INFO);
 		stmt.setInt(1, user.getUserId());
 		stmt.setString(2, user.getLogin());
 		stmt.setString(3, user.getEmail());
 		stmt.setString(4, user.getFirstName());
 		stmt.setString(5, user.getLastName());
 		stmt.setString(6, user.getEncodedPassword());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupLastTopicVersion(Topic topic) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION_LAST);
 		stmt.setInt(1, topic.getTopicId());
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupLastTopicVersion(Topic topic, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION_LAST);
 		stmt.setInt(1, topic.getTopicId());
 		return stmt.executeQuery(conn);
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupTopic(String virtualWiki, String topicName) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(virtualWiki);
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setString(2, topicName);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupTopic(String virtualWiki, String topicName, Connection conn) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(virtualWiki);
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setString(2, topicName);
 		return stmt.executeQuery(conn);
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION);
 		stmt.setInt(1, topicVersionId);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_VERSION);
 		stmt.setInt(1, topicVersionId);
 		return stmt.executeQuery(conn);
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupWikiFile(String virtualWiki, int topicId) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(virtualWiki);
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_FILE);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setInt(2, topicId);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupWikiUser(int userId) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER);
 		stmt.setInt(1, userId);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupWikiUser(int userId, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER);
 		stmt.setInt(1, userId);
 		return stmt.executeQuery(conn);
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupWikiUser(String login) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_LOGIN);
 		stmt.setString(1, login);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupWikiUser(String login, String encryptedPassword) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_PASSWORD);
 		stmt.setString(1, login);
 		stmt.setString(2, encryptedPassword);
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public int nextTopicId(Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_SEQUENCE, conn);
 		int nextId = 0;
 		if (rs.size() > 0) nextId = rs.getInt("topic_id");
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 *
 	 */
 	public int nextTopicVersionId(Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE, conn);
 		int nextId = 0;
 		if (rs.size() > 0) nextId = rs.getInt("topic_version_id");
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 *
 	 */
 	public int nextVirtualWikiId(Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE, conn);
 		int nextId = 0;
 		if (rs.size() > 0) nextId = rs.getInt("virtual_wiki_id");
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 *
 	 */
 	public int nextWikiFileId(Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_FILE_SEQUENCE, conn);
 		int nextId = 0;
 		if (rs.size() > 0) nextId = rs.getInt("file_id");
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 *
 	 */
 	public int nextWikiFileVersionId(Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE, conn);
 		int nextId = 0;
 		if (rs.size() > 0) nextId = rs.getInt("file_version_id");
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 *
 	 */
 	public int nextWikiUserId(Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE, conn);
 		int nextId = 0;
 		if (rs.size() > 0) nextId = rs.getInt("wiki_user_id");
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Utility method for returning a database-appropriate value that corresponds
 	 * to the SQL function indicating the current time.
 	 */
 	private static String now() {
 		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_POSTGRES)) {
 			return "CURRENT_TIMESTAMP";
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_MYSQL)) {
 			return "CURRENT_TIMESTAMP";
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ORACLE)) {
 			return "SYSTIMESTAMP";
 		} else {
 			return "CURRENT_TIMESTAMP";
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void reloadRecentChanges(Connection conn) throws Exception {
 		DatabaseConnection.executeUpdate(STATEMENT_DELETE_RECENT_CHANGES, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES, conn);
 	}
 
 	/**
 	 * Utility method for returning a database-appropriate value that corresponds
 	 * to the SQL type for text values.
 	 */
 	private static String text() {
 		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_POSTGRES)) {
 			return "TEXT";
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_MYSQL)) {
 			return "TEXT";
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DatabaseHandler.DB_TYPE_ORACLE)) {
 			return "CLOB";
 		} else {
 			return "TEXT";
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateTopic(Topic topic, Connection conn) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(topic.getVirtualWiki());
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_TOPIC);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setString(2, topic.getName());
 		stmt.setInt(3, topic.getTopicType());
 		stmt.setInt(4, (topic.getReadOnly() ? 1 : 0));
 		stmt.setString(5, topic.getTopicContent());
 		stmt.setInt(6, (topic.getDeleted() ? 1 : 0));
 		stmt.setInt(7, topic.getTopicId());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_VIRTUAL_WIKI);
 		stmt.setString(1, virtualWiki.getDefaultTopicName());
 		stmt.setInt(2, virtualWiki.getVirtualWikiId());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiFile(WikiFile wikiFile, Connection conn) throws Exception {
 		int virtualWikiId = WikiBase.getHandler().lookupVirtualWikiId(wikiFile.getVirtualWiki());
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_FILE);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setString(2, wikiFile.getFileName());
 		stmt.setString(3, wikiFile.getUrl());
 		stmt.setString(4, wikiFile.getMimeType());
 		stmt.setInt(5, wikiFile.getTopicId());
 		stmt.setInt(6, (wikiFile.getDeleted() ? 1 : 0));
 		stmt.setInt(7, (wikiFile.getReadOnly() ? 1 : 0));
 		stmt.setInt(8, (wikiFile.getAdminOnly() ? 1 : 0));
 		stmt.setInt(9, wikiFile.getFileSize());
 		stmt.setInt(10, wikiFile.getFileId());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiUser(WikiUser user, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_USER);
 		stmt.setString(1, user.getLogin());
 		stmt.setString(2, user.getDisplayName());
 		stmt.setTimestamp(3, user.getLastLoginDate());
 		stmt.setString(4, user.getLastLoginIpAddress());
 		stmt.setInt(5, (user.getAdmin() ? 1 : 0));
 		stmt.setInt(6, user.getUserId());
 		stmt.executeUpdate(conn);
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiUserInfo(WikiUser user, Connection conn) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_UPDATE_WIKI_USER_INFO);
 		stmt.setString(1, user.getLogin());
 		stmt.setString(2, user.getEmail());
 		stmt.setString(3, user.getFirstName());
 		stmt.setString(4, user.getLastName());
 		stmt.setString(5, user.getEncodedPassword());
 		stmt.setInt(6, user.getUserId());
 		stmt.executeUpdate(conn);
 	}
 }
