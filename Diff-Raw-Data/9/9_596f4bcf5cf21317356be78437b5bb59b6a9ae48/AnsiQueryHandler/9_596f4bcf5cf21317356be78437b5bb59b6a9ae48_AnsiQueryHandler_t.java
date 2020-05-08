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
 package org.jamwiki.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Properties;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.Interwiki;
 import org.jamwiki.model.LogItem;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.RoleMap;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.UserBlock;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiGroup;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.model.WikiUserDetails;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Default implementation of the QueryHandler implementation for retrieving, inserting,
  * and updating data in the database.  This method uses ANSI SQL and should therefore
  * work with any fully ANSI-compliant database.
  */
 public class AnsiQueryHandler implements QueryHandler {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(AnsiQueryHandler.class.getName());
 	protected static final String SQL_PROPERTY_FILE_NAME = "sql/sql.ansi.properties";
 
 	protected static String STATEMENT_CONNECTION_VALIDATION_QUERY = null;
 	protected static String STATEMENT_CREATE_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_CREATE_CATEGORY_TABLE = null;
 	protected static String STATEMENT_CREATE_CATEGORY_INDEX = null;
 	protected static String STATEMENT_CREATE_CONFIGURATION_TABLE = null;
 	protected static String STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_CREATE_GROUP_MEMBERS_TABLE = null;
 	protected static String STATEMENT_CREATE_GROUP_TABLE = null;
 	protected static String STATEMENT_CREATE_INTERWIKI_TABLE = null;
 	protected static String STATEMENT_CREATE_LOG_TABLE = null;
 	protected static String STATEMENT_CREATE_NAMESPACE_TABLE = null;
 	protected static String STATEMENT_CREATE_NAMESPACE_TRANSLATION_TABLE = null;
 	protected static String STATEMENT_CREATE_RECENT_CHANGE_TABLE = null;
 	protected static String STATEMENT_CREATE_ROLE_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT = null;
 	protected static String STATEMENT_CREATE_TOPIC_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_LINKS_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_LINKS_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_PAGE_NAME_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_PAGE_NAME_LOWER_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_NAMESPACE_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_VIRTUAL_WIKI_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_CURRENT_VERSION_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_VERSION_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_VERSION_TOPIC_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_VERSION_PREVIOUS_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_VERSION_USER_DISPLAY_INDEX = null;
 	protected static String STATEMENT_CREATE_TOPIC_VERSION_USER_ID_INDEX = null;
 	protected static String STATEMENT_CREATE_USER_BLOCK_TABLE = null;
 	protected static String STATEMENT_CREATE_USERS_TABLE = null;
 	protected static String STATEMENT_CREATE_VIRTUAL_WIKI_TABLE = null;
 	protected static String STATEMENT_CREATE_WATCHLIST_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_FILE_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_USER_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX = null;
 	protected static String STATEMENT_DELETE_AUTHORITIES = null;
 	protected static String STATEMENT_DELETE_CONFIGURATION = null;
 	protected static String STATEMENT_DELETE_GROUP_AUTHORITIES = null;
 	protected static String STATEMENT_DELETE_INTERWIKI = null;
 	protected static String STATEMENT_DELETE_LOG_ITEMS = null;
 	protected static String STATEMENT_DELETE_LOG_ITEMS_BY_TOPIC_VERSION = null;
 	protected static String STATEMENT_DELETE_NAMESPACE_TRANSLATIONS = null;
 	protected static String STATEMENT_DELETE_RECENT_CHANGES = null;
 	protected static String STATEMENT_DELETE_RECENT_CHANGES_TOPIC = null;
 	protected static String STATEMENT_DELETE_RECENT_CHANGES_TOPIC_VERSION = null;
 	protected static String STATEMENT_DELETE_TOPIC_CATEGORIES = null;
 	protected static String STATEMENT_DELETE_TOPIC_LINKS = null;
 	protected static String STATEMENT_DELETE_TOPIC_VERSION = null;
 	protected static String STATEMENT_DELETE_WATCHLIST_ENTRY = null;
 	protected static String STATEMENT_DROP_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_DROP_CATEGORY_TABLE = null;
 	protected static String STATEMENT_DROP_CONFIGURATION_TABLE = null;
 	protected static String STATEMENT_DROP_GROUP_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_DROP_GROUP_MEMBERS_TABLE = null;
 	protected static String STATEMENT_DROP_GROUP_TABLE = null;
 	protected static String STATEMENT_DROP_INTERWIKI_TABLE = null;
 	protected static String STATEMENT_DROP_LOG_TABLE = null;
 	protected static String STATEMENT_DROP_NAMESPACE_TABLE = null;
 	protected static String STATEMENT_DROP_NAMESPACE_TRANSLATION_TABLE = null;
 	protected static String STATEMENT_DROP_RECENT_CHANGE_TABLE = null;
 	protected static String STATEMENT_DROP_ROLE_TABLE = null;
 	protected static String STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT = null;
 	protected static String STATEMENT_DROP_TOPIC_TABLE = null;
 	protected static String STATEMENT_DROP_TOPIC_LINKS_TABLE = null;
 	protected static String STATEMENT_DROP_TOPIC_VERSION_TABLE = null;
 	protected static String STATEMENT_DROP_USER_BLOCK_TABLE = null;
 	protected static String STATEMENT_DROP_USERS_TABLE = null;
 	protected static String STATEMENT_DROP_VIRTUAL_WIKI_TABLE = null;
 	protected static String STATEMENT_DROP_WATCHLIST_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_FILE_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_FILE_VERSION_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_USER_TABLE = null;
 	protected static String STATEMENT_INSERT_AUTHORITY = null;
 	protected static String STATEMENT_INSERT_CATEGORY = null;
 	protected static String STATEMENT_INSERT_CONFIGURATION = null;
 	protected static String STATEMENT_INSERT_GROUP = null;
 	protected static String STATEMENT_INSERT_GROUP_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_GROUP_AUTHORITY = null;
 	protected static String STATEMENT_INSERT_GROUP_MEMBER = null;
 	protected static String STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_INTERWIKI = null;
 	protected static String STATEMENT_INSERT_LOG_ITEM = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_BLOCK = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_IMPORT = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_MOVE = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_UNBLOCK = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_UPLOAD = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_USER = null;
 	protected static String STATEMENT_INSERT_NAMESPACE = null;
 	protected static String STATEMENT_INSERT_NAMESPACE_TRANSLATION = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGE = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGES_LOGS = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGES_VERSIONS = null;
 	protected static String STATEMENT_INSERT_ROLE = null;
 	protected static String STATEMENT_INSERT_TOPIC = null;
 	protected static String STATEMENT_INSERT_TOPIC_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_TOPIC_LINKS = null;
 	protected static String STATEMENT_INSERT_TOPIC_VERSION = null;
 	protected static String STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_USER = null;
 	protected static String STATEMENT_INSERT_USER_BLOCK = null;
 	protected static String STATEMENT_INSERT_USER_BLOCK_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_VIRTUAL_WIKI = null;
 	protected static String STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_WATCHLIST_ENTRY = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE_VERSION = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_WIKI_USER = null;
 	protected static String STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT = null;
 	protected static String STATEMENT_SELECT_AUTHORITIES_AUTHORITY = null;
 	protected static String STATEMENT_SELECT_AUTHORITIES_LOGIN = null;
 	protected static String STATEMENT_SELECT_AUTHORITIES_USER = null;
 	protected static String STATEMENT_SELECT_CATEGORIES = null;
 	protected static String STATEMENT_SELECT_CATEGORY_TOPICS = null;
 	protected static String STATEMENT_SELECT_CONFIGURATION = null;
 	protected static String STATEMENT_SELECT_GROUP = null;
 	protected static String STATEMENT_SELECT_GROUP_AUTHORITIES = null;
 	protected static String STATEMENT_SELECT_GROUPS_AUTHORITIES = null;
 	protected static String STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_GROUP_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_INTERWIKIS = null;
 	protected static String STATEMENT_SELECT_LOG_ITEMS = null;
 	protected static String STATEMENT_SELECT_LOG_ITEMS_BY_TYPE = null;
 	protected static String STATEMENT_SELECT_NAMESPACE_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_NAMESPACES = null;
 	protected static String STATEMENT_SELECT_RECENT_CHANGES = null;
 	protected static String STATEMENT_SELECT_ROLES = null;
 	protected static String STATEMENT_SELECT_TOPIC_BY_ID = null;
 	protected static String STATEMENT_SELECT_TOPIC_BY_TYPE = null;
 	protected static String STATEMENT_SELECT_TOPIC_COUNT = null;
 	protected static String STATEMENT_SELECT_TOPIC = null;
 	protected static String STATEMENT_SELECT_TOPIC_HISTORY = null;
 	protected static String STATEMENT_SELECT_TOPIC_LINK_ORPHANS = null;
 	protected static String STATEMENT_SELECT_TOPIC_LINKS = null;
 	protected static String STATEMENT_SELECT_TOPIC_LOWER = null;
 	protected static String STATEMENT_SELECT_TOPIC_NAME = null;
 	protected static String STATEMENT_SELECT_TOPIC_NAME_LOWER = null;
 	protected static String STATEMENT_SELECT_TOPIC_NAMES = null;
 	protected static String STATEMENT_SELECT_TOPICS_ADMIN = null;
 	protected static String STATEMENT_SELECT_TOPIC_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION_NEXT_ID = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_USER_BLOCKS = null;
 	protected static String STATEMENT_SELECT_USER_BLOCKS_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_USERS_AUTHENTICATION = null;
 	protected static String STATEMENT_SELECT_VIRTUAL_WIKIS = null;
 	protected static String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WATCHLIST = null;
 	protected static String STATEMENT_SELECT_WATCHLIST_CHANGES = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_COUNT = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_VERSIONS = null;
 	protected static String STATEMENT_SELECT_WIKI_USER = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_COUNT = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_LOGIN = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_USERS = null;
 	protected static String STATEMENT_UPDATE_GROUP = null;
 	protected static String STATEMENT_UPDATE_ROLE = null;
 	protected static String STATEMENT_UPDATE_NAMESPACE = null;
 	protected static String STATEMENT_UPDATE_RECENT_CHANGES_PREVIOUS_VERSION_ID = null;
 	protected static String STATEMENT_UPDATE_TOPIC = null;
 	protected static String STATEMENT_UPDATE_TOPIC_NAMESPACE = null;
 	protected static String STATEMENT_UPDATE_TOPIC_VERSION = null;
 	protected static String STATEMENT_UPDATE_TOPIC_VERSION_PREVIOUS_VERSION_ID = null;
 	protected static String STATEMENT_UPDATE_USER = null;
 	protected static String STATEMENT_UPDATE_USER_BLOCK = null;
 	protected static String STATEMENT_UPDATE_VIRTUAL_WIKI = null;
 	protected static String STATEMENT_UPDATE_WIKI_FILE = null;
 	protected static String STATEMENT_UPDATE_WIKI_USER = null;
 	private Properties props = null;
 
 	/**
 	 *
 	 */
 	protected AnsiQueryHandler() {
 		props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME);
 		this.init(props);
 	}
 
 	/**
 	 *
 	 */
 	public boolean authenticateUser(String username, String encryptedPassword, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_USERS_AUTHENTICATION);
 			stmt.setString(1, username);
 			stmt.setString(2, encryptedPassword);
 			return (stmt.executeQuery().next());
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean autoIncrementPrimaryKeys() {
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	public String connectionValidationQuery() {
 		return STATEMENT_CONNECTION_VALIDATION_QUERY;
 	}
 
 	/**
 	 *
 	 */
 	public void createTables(Connection conn) throws SQLException {
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_VIRTUAL_WIKI_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_USERS_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_NAMESPACE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_NAMESPACE_TRANSLATION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_PAGE_NAME_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_PAGE_NAME_LOWER_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_NAMESPACE_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VIRTUAL_WIKI_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_CURRENT_VERSION_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_TOPIC_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_PREVIOUS_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_USER_DISPLAY_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_USER_ID_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_LINKS_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_LINKS_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_CATEGORY_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_CATEGORY_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_GROUP_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_GROUP_MEMBERS_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_ROLE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_AUTHORITIES_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_LOG_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_RECENT_CHANGE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WATCHLIST_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_INTERWIKI_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_CONFIGURATION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_USER_BLOCK_TABLE, conn);
 	}
 
 	/**
 	 *
 	 */
 	public void deleteGroupAuthorities(int groupId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_GROUP_AUTHORITIES);
 			stmt.setInt(1, groupId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteInterwiki(Interwiki interwiki, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_INTERWIKI);
 			stmt.setString(1, interwiki.getInterwikiPrefix());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteRecentChanges(int topicId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_RECENT_CHANGES_TOPIC);
 			stmt.setInt(1, topicId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteTopicCategories(int childTopicId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_TOPIC_CATEGORIES);
 			stmt.setInt(1, childTopicId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteTopicLinks(int topicId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_TOPIC_LINKS);
 			stmt.setInt(1, topicId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteTopicVersion(int topicVersionId, Integer previousTopicVersionId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			// delete references to the topic version from the log table
 			stmt = conn.prepareStatement(STATEMENT_DELETE_LOG_ITEMS_BY_TOPIC_VERSION);
 			stmt.setInt(1, topicVersionId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			// delete references to the topic version from the recent changes table
 			stmt = conn.prepareStatement(STATEMENT_DELETE_RECENT_CHANGES_TOPIC_VERSION);
 			stmt.setInt(1, topicVersionId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			// update any recent changes that refer to this record as the previous record
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_RECENT_CHANGES_PREVIOUS_VERSION_ID);
 			if (previousTopicVersionId != null) {
 				stmt.setInt(1, previousTopicVersionId);
 			} else {
 				stmt.setNull(1, Types.INTEGER);
 			}
 			stmt.setInt(2, topicVersionId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			// delete the topic version record
 			stmt = conn.prepareStatement(STATEMENT_DELETE_TOPIC_VERSION);
 			stmt.setInt(1, topicVersionId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteUserAuthorities(String username, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_AUTHORITIES);
 			stmt.setString(1, username);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_WATCHLIST_ENTRY);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topicName);
 			stmt.setInt(3, userId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
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
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_USER_BLOCK_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_CONFIGURATION_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_INTERWIKI_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WATCHLIST_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_RECENT_CHANGE_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_LOG_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_GROUP_AUTHORITIES_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_AUTHORITIES_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_ROLE_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_GROUP_MEMBERS_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_GROUP_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_CATEGORY_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_VERSION_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_LINKS_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_VERSION_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_NAMESPACE_TRANSLATION_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_NAMESPACE_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_USER_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_USERS_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_VIRTUAL_WIKI_TABLE, conn);
 		} catch (SQLException e) { logger.error(e.getMessage()); }
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeQuery(String prop, Connection conn) throws SQLException {
 		String sql = this.props.getProperty(prop);
 		if (sql == null) {
 			throw new SQLException("No property found for " + prop);
 		}
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(sql);
 			stmt.executeQuery();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeUpdate(String prop, Connection conn) throws SQLException {
 		String sql = this.props.getProperty(prop);
 		if (sql == null) {
 			throw new SQLException("No property found for " + prop);
 		}
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(sql);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 * Return a simple query, that if successfully run indicates that JAMWiki
 	 * tables have been initialized in the database.
 	 *
 	 * @return Returns a simple query that, if successfully run, indicates
 	 *  that JAMWiki tables have been set up in the database.
 	 */
 	public String existenceValidationQuery() {
 		return STATEMENT_SELECT_VIRTUAL_WIKIS;
 	}
 
 	/**
 	 * In rare cases a single statement cannot easily be used across databases, such
 	 * as "date is null" and "date is not null".  Rather than having two separate
 	 * SQL statements the statement is instead "date is {0} null", and a Java
 	 * MessageFormat object is then used to modify the SQL.
 	 *
 	 * @param sql The SQL statement in MessageFormat format ("date is {0} null").
 	 * @param params An array of objects (which should be strings) to use when
 	 *  formatting the message.
 	 * @return A formatted SQL string.
 	 */
 	protected String formatStatement(String sql, Object[] params) {
 		if (params == null || params.length == 0) {
 			return sql;
 		}
 		try {
			// replace all single quotes with '' since otherwise MessageFormat
			// will treat the content is a quoted string
			return MessageFormat.format(sql.replaceAll("'", "''"), params);
 		} catch (IllegalArgumentException e) {
 			String msg = "Unable to format " + sql + " with values: ";
 			for (int i = 0; i < params.length; i++) {
 				msg += (i > 0) ? " | " + params[i] : params[i];
 			}
 			logger.warn(msg);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<WikiFileVersion> getAllWikiFileVersions(WikiFile wikiFile, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_FILE_VERSIONS);
 			// FIXME - sort order ignored
 			stmt.setInt(1, wikiFile.getFileId());
 			rs = stmt.executeQuery();
 			List<WikiFileVersion> fileVersions = new ArrayList<WikiFileVersion>();
 			while (rs.next()) {
 				fileVersions.add(this.initWikiFileVersion(rs));
 			}
 			return fileVersions;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> getCategories(int virtualWikiId, String virtualWikiName, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.getCategoriesStatement(conn, virtualWikiId, virtualWikiName, pagination);
 			rs = stmt.executeQuery();
 			List<Category> results = new ArrayList<Category>();
 			while (rs.next()) {
 				Category category = new Category();
 				category.setName(rs.getString("category_name"));
 				// child topic name not initialized since it is not needed
 				category.setVirtualWiki(virtualWikiName);
 				category.setSortKey(rs.getString("sort_key"));
 				// topic type not initialized since it is not needed
 				results.add(category);
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getCategoriesStatement(Connection conn, int virtualWikiId, String virtualWikiName, Pagination pagination) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_CATEGORIES);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setInt(2, pagination.getNumResults());
 		stmt.setInt(3, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public List<LogItem> getLogItems(int virtualWikiId, String virtualWikiName, int logType, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		List<LogItem> logItems = new ArrayList<LogItem>();
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.getLogItemsStatement(conn, virtualWikiId, virtualWikiName, logType, pagination, descending);
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				logItems.add(this.initLogItem(rs, virtualWikiName));
 			}
 			return logItems;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getLogItemsStatement(Connection conn, int virtualWikiId, String virtualWikiName, int logType, Pagination pagination, boolean descending) throws SQLException {
 		int index = 1;
 		PreparedStatement stmt = null;
 		if (logType == -1) {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_LOG_ITEMS);
 		} else {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_LOG_ITEMS_BY_TYPE);
 			stmt.setInt(index++, logType);
 		}
 		stmt.setInt(index++, virtualWikiId);
 		stmt.setInt(index++, pagination.getNumResults());
 		stmt.setInt(index++, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.getRecentChangesStatement(conn, virtualWiki, pagination, descending);
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getRecentChangesStatement(Connection conn, String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_RECENT_CHANGES);
 		stmt.setString(1, virtualWiki);
 		stmt.setInt(2, pagination.getNumResults());
 		stmt.setInt(3, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByLogin(String loginFragment) throws SQLException {
 		if (StringUtils.isBlank(loginFragment)) {
 			return new ArrayList<RoleMap>();
 		}
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_AUTHORITIES_LOGIN);
 			loginFragment = '%' + loginFragment.toLowerCase() + '%';
 			stmt.setString(1, loginFragment);
 			rs = stmt.executeQuery();
 			LinkedHashMap<Integer, RoleMap> roleMaps = new LinkedHashMap<Integer, RoleMap>();
 			while (rs.next()) {
 				Integer userId = rs.getInt("wiki_user_id");
 				RoleMap roleMap = new RoleMap();
 				if (roleMaps.containsKey(userId)) {
 					roleMap = roleMaps.get(userId);
 				} else {
 					roleMap.setUserId(userId);
 					roleMap.setUserLogin(rs.getString("username"));
 				}
 				roleMap.addRole(rs.getString("authority"));
 				roleMaps.put(userId, roleMap);
 			}
 			return new ArrayList<RoleMap>(roleMaps.values());
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByRole(String authority) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_AUTHORITIES_AUTHORITY);
 			stmt.setString(1, authority);
 			stmt.setString(2, authority);
 			rs = stmt.executeQuery();
 			LinkedHashMap<String, RoleMap> roleMaps = new LinkedHashMap<String, RoleMap>();
 			while (rs.next()) {
 				int userId = rs.getInt("wiki_user_id");
 				int groupId = rs.getInt("group_id");
 				RoleMap roleMap = new RoleMap();
 				String key = userId + "|" + groupId;
 				if (roleMaps.containsKey(key)) {
 					roleMap = roleMaps.get(key);
 				} else {
 					if (userId > 0) {
 						roleMap.setUserId(userId);
 						roleMap.setUserLogin(rs.getString("username"));
 					}
 					if (groupId > 0) {
 						roleMap.setGroupId(groupId);
 						roleMap.setGroupName(rs.getString("group_name"));
 					}
 				}
 				roleMap.addRole(rs.getString("authority"));
 				roleMaps.put(key, roleMap);
 			}
 			return new ArrayList<RoleMap>(roleMaps.values());
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapGroup(String groupName) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_GROUP_AUTHORITIES);
 			stmt.setString(1, groupName);
 			rs = stmt.executeQuery();
 			List<Role> roles = new ArrayList<Role>();
 			while (rs.next()) {
 				roles.add(this.initRole(rs));
 			}
 			return roles;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapGroups() throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_GROUPS_AUTHORITIES);
 			rs = stmt.executeQuery();
 			LinkedHashMap<Integer, RoleMap> roleMaps = new LinkedHashMap<Integer, RoleMap>();
 			while (rs.next()) {
 				Integer groupId = rs.getInt("group_id");
 				RoleMap roleMap = new RoleMap();
 				if (roleMaps.containsKey(groupId)) {
 					roleMap = roleMaps.get(groupId);
 				} else {
 					roleMap.setGroupId(groupId);
 					roleMap.setGroupName(rs.getString("group_name"));
 				}
 				roleMap.addRole(rs.getString("authority"));
 				roleMaps.put(groupId, roleMap);
 			}
 			return new ArrayList<RoleMap>(roleMaps.values());
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapUser(String login) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_AUTHORITIES_USER);
 			stmt.setString(1, login);
 			rs = stmt.executeQuery();
 			List<Role> roles = new ArrayList<Role>();
 			while (rs.next()) {
 				roles.add(this.initRole(rs));
 			}
 			return roles;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoles() throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_ROLES);
 			rs = stmt.executeQuery();
 			List<Role> roles = new ArrayList<Role>();
 			while (rs.next()) {
 				roles.add(this.initRole(rs));
 			}
 			return roles;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getTopicHistory(int topicId, Pagination pagination, boolean descending, boolean selectDeleted) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = getTopicHistoryStatement(conn, topicId, pagination, descending, selectDeleted);
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getTopicHistoryStatement(Connection conn, int topicId, Pagination pagination, boolean descending, boolean selectDeleted) throws SQLException {
 		// the SQL contains the syntax "is {0} null", which needs to be formatted as a message.
 		Object[] params = {""};
 		if (selectDeleted) {
 			params[0] = "not";
 		}
 		String sql = this.formatStatement(STATEMENT_SELECT_TOPIC_HISTORY, params);
 		PreparedStatement stmt = conn.prepareStatement(sql);
 		stmt.setInt(1, topicId);
 		stmt.setInt(2, pagination.getNumResults());
 		stmt.setInt(3, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getTopicsAdmin(int virtualWikiId, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.getTopicsAdminStatement(conn, virtualWikiId, pagination);
 			rs = stmt.executeQuery();
 			List<String> results = new ArrayList<String>();
 			while (rs.next()) {
 				results.add(rs.getString("topic_name"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getTopicsAdminStatement(Connection conn, int virtualWikiId, Pagination pagination) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_TOPICS_ADMIN);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setInt(2, pagination.getNumResults());
 		stmt.setInt(3, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public Map<Object, UserBlock> getUserBlocks(Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_USER_BLOCKS);
 			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
 			rs = stmt.executeQuery();
 			Map<Object, UserBlock> userBlocks = new LinkedHashMap<Object, UserBlock>();
 			while (rs.next()) {
 				UserBlock userBlock = this.initUserBlock(rs);
 				if (userBlock.getWikiUserId() != null) {
 					userBlocks.put(userBlock.getWikiUserId(), userBlock);
 				}
 				if (userBlock.getIpAddress() != null) {
 					userBlocks.put(userBlock.getIpAddress(), userBlock);
 				}
 			}
 			return userBlocks;
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getUserContributionsByLogin(String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.getUserContributionsByLoginStatement(conn, virtualWiki, login, pagination, descending);
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getUserContributionsByLoginStatement(Connection conn, String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
 		stmt.setString(1, virtualWiki);
 		stmt.setString(2, login);
 		stmt.setInt(3, pagination.getNumResults());
 		stmt.setInt(4, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getUserContributionsByUserDisplay(String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.getUserContributionsByUserDisplayStatement(conn, virtualWiki, userDisplay, pagination, descending);
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getUserContributionsByUserDisplayStatement(Connection conn, String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
 		stmt.setString(1, virtualWiki);
 		stmt.setString(2, userDisplay);
 		stmt.setInt(3, pagination.getNumResults());
 		stmt.setInt(4, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public List<VirtualWiki> getVirtualWikis(Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_VIRTUAL_WIKIS);
 			rs = stmt.executeQuery();
 			List<VirtualWiki> results = new ArrayList<VirtualWiki>();
 			while (rs.next()) {
 				VirtualWiki virtualWiki = new VirtualWiki(rs.getString("virtual_wiki_name"));
 				virtualWiki.setVirtualWikiId(rs.getInt("virtual_wiki_id"));
 				virtualWiki.setRootTopicName(rs.getString("default_topic_name"));
 				virtualWiki.setLogoImageUrl(rs.getString("logo_image_url"));
 				virtualWiki.setMetaDescription(rs.getString("meta_description"));
 				virtualWiki.setSiteName(rs.getString("site_name"));
 				results.add(virtualWiki);
 			}
 			return results;
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getWatchlist(int virtualWikiId, int userId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WATCHLIST);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, userId);
 			rs = stmt.executeQuery();
 			List<String> watchedTopicNames = new ArrayList<String>();
 			while (rs.next()) {
 				watchedTopicNames.add(rs.getString("topic_name"));
 			}
 			return watchedTopicNames;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getWatchlist(int virtualWikiId, int userId, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.getWatchlistStatement(conn, virtualWikiId, userId, pagination);
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement getWatchlistStatement(Connection conn, int virtualWikiId, int userId, Pagination pagination) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_WATCHLIST_CHANGES);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setInt(2, userId);
 		stmt.setInt(3, pagination.getNumResults());
 		stmt.setInt(4, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	protected void init(Properties properties) {
 		this.props = properties;
 		STATEMENT_CONNECTION_VALIDATION_QUERY    = props.getProperty("STATEMENT_CONNECTION_VALIDATION_QUERY");
 		STATEMENT_CREATE_CONFIGURATION_TABLE     = props.getProperty("STATEMENT_CREATE_CONFIGURATION_TABLE");
 		STATEMENT_CREATE_GROUP_TABLE             = props.getProperty("STATEMENT_CREATE_GROUP_TABLE");
 		STATEMENT_CREATE_INTERWIKI_TABLE         = props.getProperty("STATEMENT_CREATE_INTERWIKI_TABLE");
 		STATEMENT_CREATE_NAMESPACE_TABLE         = props.getProperty("STATEMENT_CREATE_NAMESPACE_TABLE");
 		STATEMENT_CREATE_NAMESPACE_TRANSLATION_TABLE = props.getProperty("STATEMENT_CREATE_NAMESPACE_TRANSLATION_TABLE");
 		STATEMENT_CREATE_ROLE_TABLE              = props.getProperty("STATEMENT_CREATE_ROLE_TABLE");
 		STATEMENT_CREATE_VIRTUAL_WIKI_TABLE      = props.getProperty("STATEMENT_CREATE_VIRTUAL_WIKI_TABLE");
 		STATEMENT_CREATE_WIKI_USER_TABLE         = props.getProperty("STATEMENT_CREATE_WIKI_USER_TABLE");
 		STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX   = props.getProperty("STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX");
 		STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT = props.getProperty("STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT");
 		STATEMENT_CREATE_TOPIC_TABLE             = props.getProperty("STATEMENT_CREATE_TOPIC_TABLE");
 		STATEMENT_CREATE_TOPIC_LINKS_TABLE       = props.getProperty("STATEMENT_CREATE_TOPIC_LINKS_TABLE");
 		STATEMENT_CREATE_TOPIC_LINKS_INDEX       = props.getProperty("STATEMENT_CREATE_TOPIC_LINKS_INDEX");
 		STATEMENT_CREATE_TOPIC_PAGE_NAME_INDEX   = props.getProperty("STATEMENT_CREATE_TOPIC_PAGE_NAME_INDEX");
 		STATEMENT_CREATE_TOPIC_PAGE_NAME_LOWER_INDEX = props.getProperty("STATEMENT_CREATE_TOPIC_PAGE_NAME_LOWER_INDEX");
 		STATEMENT_CREATE_TOPIC_NAMESPACE_INDEX   = props.getProperty("STATEMENT_CREATE_TOPIC_NAMESPACE_INDEX");
 		STATEMENT_CREATE_TOPIC_VIRTUAL_WIKI_INDEX = props.getProperty("STATEMENT_CREATE_TOPIC_VIRTUAL_WIKI_INDEX");
 		STATEMENT_CREATE_TOPIC_CURRENT_VERSION_INDEX = props.getProperty("STATEMENT_CREATE_TOPIC_CURRENT_VERSION_INDEX");
 		STATEMENT_CREATE_TOPIC_VERSION_TABLE     = props.getProperty("STATEMENT_CREATE_TOPIC_VERSION_TABLE");
 		STATEMENT_CREATE_TOPIC_VERSION_TOPIC_INDEX = props.getProperty("STATEMENT_CREATE_TOPIC_VERSION_TOPIC_INDEX");
 		STATEMENT_CREATE_TOPIC_VERSION_PREVIOUS_INDEX = props.getProperty("STATEMENT_CREATE_TOPIC_VERSION_PREVIOUS_INDEX");
 		STATEMENT_CREATE_TOPIC_VERSION_USER_DISPLAY_INDEX = props.getProperty("STATEMENT_CREATE_TOPIC_VERSION_USER_DISPLAY_INDEX");
 		STATEMENT_CREATE_TOPIC_VERSION_USER_ID_INDEX = props.getProperty("STATEMENT_CREATE_TOPIC_VERSION_USER_ID_INDEX");
 		STATEMENT_CREATE_USER_BLOCK_TABLE        = props.getProperty("STATEMENT_CREATE_USER_BLOCK_TABLE");
 		STATEMENT_CREATE_USERS_TABLE             = props.getProperty("STATEMENT_CREATE_USERS_TABLE");
 		STATEMENT_CREATE_WIKI_FILE_TABLE         = props.getProperty("STATEMENT_CREATE_WIKI_FILE_TABLE");
 		STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE = props.getProperty("STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE");
 		STATEMENT_CREATE_AUTHORITIES_TABLE       = props.getProperty("STATEMENT_CREATE_AUTHORITIES_TABLE");
 		STATEMENT_CREATE_CATEGORY_TABLE          = props.getProperty("STATEMENT_CREATE_CATEGORY_TABLE");
 		STATEMENT_CREATE_CATEGORY_INDEX          = props.getProperty("STATEMENT_CREATE_CATEGORY_INDEX");
 		STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE = props.getProperty("STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE");
 		STATEMENT_CREATE_GROUP_MEMBERS_TABLE     = props.getProperty("STATEMENT_CREATE_GROUP_MEMBERS_TABLE");
 		STATEMENT_CREATE_LOG_TABLE               = props.getProperty("STATEMENT_CREATE_LOG_TABLE");
 		STATEMENT_CREATE_RECENT_CHANGE_TABLE     = props.getProperty("STATEMENT_CREATE_RECENT_CHANGE_TABLE");
 		STATEMENT_CREATE_WATCHLIST_TABLE         = props.getProperty("STATEMENT_CREATE_WATCHLIST_TABLE");
 		STATEMENT_DELETE_AUTHORITIES             = props.getProperty("STATEMENT_DELETE_AUTHORITIES");
 		STATEMENT_DELETE_CONFIGURATION           = props.getProperty("STATEMENT_DELETE_CONFIGURATION");
 		STATEMENT_DELETE_GROUP_AUTHORITIES       = props.getProperty("STATEMENT_DELETE_GROUP_AUTHORITIES");
 		STATEMENT_DELETE_INTERWIKI               = props.getProperty("STATEMENT_DELETE_INTERWIKI");
 		STATEMENT_DELETE_LOG_ITEMS               = props.getProperty("STATEMENT_DELETE_LOG_ITEMS");
 		STATEMENT_DELETE_LOG_ITEMS_BY_TOPIC_VERSION = props.getProperty("STATEMENT_DELETE_LOG_ITEMS_BY_TOPIC_VERSION");
 		STATEMENT_DELETE_NAMESPACE_TRANSLATIONS  = props.getProperty("STATEMENT_DELETE_NAMESPACE_TRANSLATIONS");
 		STATEMENT_DELETE_RECENT_CHANGES          = props.getProperty("STATEMENT_DELETE_RECENT_CHANGES");
 		STATEMENT_DELETE_RECENT_CHANGES_TOPIC    = props.getProperty("STATEMENT_DELETE_RECENT_CHANGES_TOPIC");
 		STATEMENT_DELETE_RECENT_CHANGES_TOPIC_VERSION = props.getProperty("STATEMENT_DELETE_RECENT_CHANGES_TOPIC_VERSION");
 		STATEMENT_DELETE_TOPIC_CATEGORIES        = props.getProperty("STATEMENT_DELETE_TOPIC_CATEGORIES");
 		STATEMENT_DELETE_TOPIC_LINKS             = props.getProperty("STATEMENT_DELETE_TOPIC_LINKS");
 		STATEMENT_DELETE_TOPIC_VERSION           = props.getProperty("STATEMENT_DELETE_TOPIC_VERSION");
 		STATEMENT_DELETE_WATCHLIST_ENTRY         = props.getProperty("STATEMENT_DELETE_WATCHLIST_ENTRY");
 		STATEMENT_DROP_AUTHORITIES_TABLE         = props.getProperty("STATEMENT_DROP_AUTHORITIES_TABLE");
 		STATEMENT_DROP_CATEGORY_TABLE            = props.getProperty("STATEMENT_DROP_CATEGORY_TABLE");
 		STATEMENT_DROP_CONFIGURATION_TABLE       = props.getProperty("STATEMENT_DROP_CONFIGURATION_TABLE");
 		STATEMENT_DROP_GROUP_AUTHORITIES_TABLE   = props.getProperty("STATEMENT_DROP_GROUP_AUTHORITIES_TABLE");
 		STATEMENT_DROP_GROUP_MEMBERS_TABLE       = props.getProperty("STATEMENT_DROP_GROUP_MEMBERS_TABLE");
 		STATEMENT_DROP_GROUP_TABLE               = props.getProperty("STATEMENT_DROP_GROUP_TABLE");
 		STATEMENT_DROP_INTERWIKI_TABLE           = props.getProperty("STATEMENT_DROP_INTERWIKI_TABLE");
 		STATEMENT_DROP_LOG_TABLE                 = props.getProperty("STATEMENT_DROP_LOG_TABLE");
 		STATEMENT_DROP_NAMESPACE_TABLE           = props.getProperty("STATEMENT_DROP_NAMESPACE_TABLE");
 		STATEMENT_DROP_NAMESPACE_TRANSLATION_TABLE = props.getProperty("STATEMENT_DROP_NAMESPACE_TRANSLATION_TABLE");
 		STATEMENT_DROP_RECENT_CHANGE_TABLE       = props.getProperty("STATEMENT_DROP_RECENT_CHANGE_TABLE");
 		STATEMENT_DROP_ROLE_TABLE                = props.getProperty("STATEMENT_DROP_ROLE_TABLE");
 		STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT = props.getProperty("STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT");
 		STATEMENT_DROP_TOPIC_TABLE               = props.getProperty("STATEMENT_DROP_TOPIC_TABLE");
 		STATEMENT_DROP_TOPIC_LINKS_TABLE         = props.getProperty("STATEMENT_DROP_TOPIC_LINKS_TABLE");
 		STATEMENT_DROP_TOPIC_VERSION_TABLE       = props.getProperty("STATEMENT_DROP_TOPIC_VERSION_TABLE");
 		STATEMENT_DROP_USER_BLOCK_TABLE          = props.getProperty("STATEMENT_DROP_USER_BLOCK_TABLE");
 		STATEMENT_DROP_USERS_TABLE               = props.getProperty("STATEMENT_DROP_USERS_TABLE");
 		STATEMENT_DROP_VIRTUAL_WIKI_TABLE        = props.getProperty("STATEMENT_DROP_VIRTUAL_WIKI_TABLE");
 		STATEMENT_DROP_WATCHLIST_TABLE           = props.getProperty("STATEMENT_DROP_WATCHLIST_TABLE");
 		STATEMENT_DROP_WIKI_USER_TABLE           = props.getProperty("STATEMENT_DROP_WIKI_USER_TABLE");
 		STATEMENT_DROP_WIKI_FILE_TABLE           = props.getProperty("STATEMENT_DROP_WIKI_FILE_TABLE");
 		STATEMENT_DROP_WIKI_FILE_VERSION_TABLE   = props.getProperty("STATEMENT_DROP_WIKI_FILE_VERSION_TABLE");
 		STATEMENT_INSERT_AUTHORITY               = props.getProperty("STATEMENT_INSERT_AUTHORITY");
 		STATEMENT_INSERT_CATEGORY                = props.getProperty("STATEMENT_INSERT_CATEGORY");
 		STATEMENT_INSERT_CONFIGURATION           = props.getProperty("STATEMENT_INSERT_CONFIGURATION");
 		STATEMENT_INSERT_GROUP                   = props.getProperty("STATEMENT_INSERT_GROUP");
 		STATEMENT_INSERT_GROUP_AUTO_INCREMENT    = props.getProperty("STATEMENT_INSERT_GROUP_AUTO_INCREMENT");
 		STATEMENT_INSERT_GROUP_AUTHORITY         = props.getProperty("STATEMENT_INSERT_GROUP_AUTHORITY");
 		STATEMENT_INSERT_GROUP_MEMBER            = props.getProperty("STATEMENT_INSERT_GROUP_MEMBER");
 		STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT");
 		STATEMENT_INSERT_INTERWIKI               = props.getProperty("STATEMENT_INSERT_INTERWIKI");
 		STATEMENT_INSERT_LOG_ITEM                = props.getProperty("STATEMENT_INSERT_LOG_ITEM");
 		STATEMENT_INSERT_LOG_ITEMS_BLOCK         = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_BLOCK");
 		STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE");
 		STATEMENT_INSERT_LOG_ITEMS_IMPORT        = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_IMPORT");
 		STATEMENT_INSERT_LOG_ITEMS_MOVE          = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_MOVE");
 		STATEMENT_INSERT_LOG_ITEMS_UNBLOCK       = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_UNBLOCK");
 		STATEMENT_INSERT_LOG_ITEMS_UPLOAD        = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_UPLOAD");
 		STATEMENT_INSERT_LOG_ITEMS_USER          = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_USER");
 		STATEMENT_INSERT_NAMESPACE               = props.getProperty("STATEMENT_INSERT_NAMESPACE");
 		STATEMENT_INSERT_NAMESPACE_TRANSLATION   = props.getProperty("STATEMENT_INSERT_NAMESPACE_TRANSLATION");
 		STATEMENT_INSERT_RECENT_CHANGE           = props.getProperty("STATEMENT_INSERT_RECENT_CHANGE");
 		STATEMENT_INSERT_RECENT_CHANGES_LOGS     = props.getProperty("STATEMENT_INSERT_RECENT_CHANGES_LOGS");
 		STATEMENT_INSERT_RECENT_CHANGES_VERSIONS = props.getProperty("STATEMENT_INSERT_RECENT_CHANGES_VERSIONS");
 		STATEMENT_INSERT_ROLE                    = props.getProperty("STATEMENT_INSERT_ROLE");
 		STATEMENT_INSERT_TOPIC                   = props.getProperty("STATEMENT_INSERT_TOPIC");
 		STATEMENT_INSERT_TOPIC_AUTO_INCREMENT    = props.getProperty("STATEMENT_INSERT_TOPIC_AUTO_INCREMENT");
 		STATEMENT_INSERT_TOPIC_LINKS             = props.getProperty("STATEMENT_INSERT_TOPIC_LINKS");
 		STATEMENT_INSERT_TOPIC_VERSION           = props.getProperty("STATEMENT_INSERT_TOPIC_VERSION");
 		STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT");
 		STATEMENT_INSERT_USER                    = props.getProperty("STATEMENT_INSERT_USER");
 		STATEMENT_INSERT_USER_BLOCK              = props.getProperty("STATEMENT_INSERT_USER_BLOCK");
 		STATEMENT_INSERT_USER_BLOCK_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_USER_BLOCK_AUTO_INCREMENT");
 		STATEMENT_INSERT_VIRTUAL_WIKI            = props.getProperty("STATEMENT_INSERT_VIRTUAL_WIKI");
 		STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT");
 		STATEMENT_INSERT_WATCHLIST_ENTRY         = props.getProperty("STATEMENT_INSERT_WATCHLIST_ENTRY");
 		STATEMENT_INSERT_WIKI_FILE               = props.getProperty("STATEMENT_INSERT_WIKI_FILE");
 		STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT");
 		STATEMENT_INSERT_WIKI_FILE_VERSION       = props.getProperty("STATEMENT_INSERT_WIKI_FILE_VERSION");
 		STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT");
 		STATEMENT_INSERT_WIKI_USER               = props.getProperty("STATEMENT_INSERT_WIKI_USER");
 		STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT");
 		STATEMENT_SELECT_AUTHORITIES_AUTHORITY   = props.getProperty("STATEMENT_SELECT_AUTHORITIES_AUTHORITY");
 		STATEMENT_SELECT_AUTHORITIES_LOGIN       = props.getProperty("STATEMENT_SELECT_AUTHORITIES_LOGIN");
 		STATEMENT_SELECT_AUTHORITIES_USER        = props.getProperty("STATEMENT_SELECT_AUTHORITIES_USER");
 		STATEMENT_SELECT_CATEGORIES              = props.getProperty("STATEMENT_SELECT_CATEGORIES");
 		STATEMENT_SELECT_CATEGORY_TOPICS         = props.getProperty("STATEMENT_SELECT_CATEGORY_TOPICS");
 		STATEMENT_SELECT_CONFIGURATION           = props.getProperty("STATEMENT_SELECT_CONFIGURATION");
 		STATEMENT_SELECT_GROUP                   = props.getProperty("STATEMENT_SELECT_GROUP");
 		STATEMENT_SELECT_GROUP_AUTHORITIES       = props.getProperty("STATEMENT_SELECT_GROUP_AUTHORITIES");
 		STATEMENT_SELECT_GROUPS_AUTHORITIES      = props.getProperty("STATEMENT_SELECT_GROUPS_AUTHORITIES");
 		STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE  = props.getProperty("STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE");
 		STATEMENT_SELECT_GROUP_SEQUENCE          = props.getProperty("STATEMENT_SELECT_GROUP_SEQUENCE");
 		STATEMENT_SELECT_INTERWIKIS              = props.getProperty("STATEMENT_SELECT_INTERWIKIS");
 		STATEMENT_SELECT_LOG_ITEMS               = props.getProperty("STATEMENT_SELECT_LOG_ITEMS");
 		STATEMENT_SELECT_LOG_ITEMS_BY_TYPE       = props.getProperty("STATEMENT_SELECT_LOG_ITEMS_BY_TYPE");
 		STATEMENT_SELECT_NAMESPACE_SEQUENCE      = props.getProperty("STATEMENT_SELECT_NAMESPACE_SEQUENCE");
 		STATEMENT_SELECT_NAMESPACES              = props.getProperty("STATEMENT_SELECT_NAMESPACES");
 		STATEMENT_SELECT_RECENT_CHANGES          = props.getProperty("STATEMENT_SELECT_RECENT_CHANGES");
 		STATEMENT_SELECT_ROLES                   = props.getProperty("STATEMENT_SELECT_ROLES");
 		STATEMENT_SELECT_TOPIC_BY_ID             = props.getProperty("STATEMENT_SELECT_TOPIC_BY_ID");
 		STATEMENT_SELECT_TOPIC_BY_TYPE           = props.getProperty("STATEMENT_SELECT_TOPIC_BY_TYPE");
 		STATEMENT_SELECT_TOPIC_COUNT             = props.getProperty("STATEMENT_SELECT_TOPIC_COUNT");
 		STATEMENT_SELECT_TOPIC                   = props.getProperty("STATEMENT_SELECT_TOPIC");
 		STATEMENT_SELECT_TOPIC_HISTORY           = props.getProperty("STATEMENT_SELECT_TOPIC_HISTORY");
 		STATEMENT_SELECT_TOPIC_LINK_ORPHANS      = props.getProperty("STATEMENT_SELECT_TOPIC_LINK_ORPHANS");
 		STATEMENT_SELECT_TOPIC_LINKS             = props.getProperty("STATEMENT_SELECT_TOPIC_LINKS");
 		STATEMENT_SELECT_TOPIC_LOWER             = props.getProperty("STATEMENT_SELECT_TOPIC_LOWER");
 		STATEMENT_SELECT_TOPIC_NAME              = props.getProperty("STATEMENT_SELECT_TOPIC_NAME");
 		STATEMENT_SELECT_TOPIC_NAME_LOWER        = props.getProperty("STATEMENT_SELECT_TOPIC_NAME_LOWER");
 		STATEMENT_SELECT_TOPIC_NAMES             = props.getProperty("STATEMENT_SELECT_TOPIC_NAMES");
 		STATEMENT_SELECT_TOPICS_ADMIN            = props.getProperty("STATEMENT_SELECT_TOPICS_ADMIN");
 		STATEMENT_SELECT_TOPIC_SEQUENCE          = props.getProperty("STATEMENT_SELECT_TOPIC_SEQUENCE");
 		STATEMENT_SELECT_TOPIC_VERSION           = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION");
 		STATEMENT_SELECT_TOPIC_VERSION_NEXT_ID   = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION_NEXT_ID");
 		STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE  = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE");
		STATEMENT_SELECT_USER_BLOCKS             = props.getProperty("STATEMENT_SELECT_USER_BLOCKS");
		STATEMENT_SELECT_USER_BLOCKS_SEQUENCE    = props.getProperty("STATEMENT_SELECT_USER_BLOCKS_SEQUENCE");
 		STATEMENT_SELECT_USERS_AUTHENTICATION    = props.getProperty("STATEMENT_SELECT_USERS_AUTHENTICATION");
 		STATEMENT_SELECT_VIRTUAL_WIKIS           = props.getProperty("STATEMENT_SELECT_VIRTUAL_WIKIS");
 		STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE   = props.getProperty("STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE");
 		STATEMENT_SELECT_WATCHLIST               = props.getProperty("STATEMENT_SELECT_WATCHLIST");
 		STATEMENT_SELECT_WATCHLIST_CHANGES       = props.getProperty("STATEMENT_SELECT_WATCHLIST_CHANGES");
 		STATEMENT_SELECT_WIKI_FILE               = props.getProperty("STATEMENT_SELECT_WIKI_FILE");
 		STATEMENT_SELECT_WIKI_FILE_COUNT         = props.getProperty("STATEMENT_SELECT_WIKI_FILE_COUNT");
 		STATEMENT_SELECT_WIKI_FILE_SEQUENCE      = props.getProperty("STATEMENT_SELECT_WIKI_FILE_SEQUENCE");
 		STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE = props.getProperty("STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE");
 		STATEMENT_SELECT_WIKI_FILE_VERSIONS      = props.getProperty("STATEMENT_SELECT_WIKI_FILE_VERSIONS");
 		STATEMENT_SELECT_WIKI_USER               = props.getProperty("STATEMENT_SELECT_WIKI_USER");
 		STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS = props.getProperty("STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS");
 		STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN = props.getProperty("STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN");
 		STATEMENT_SELECT_WIKI_USER_COUNT         = props.getProperty("STATEMENT_SELECT_WIKI_USER_COUNT");
 		STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD = props.getProperty("STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD");
 		STATEMENT_SELECT_WIKI_USER_LOGIN         = props.getProperty("STATEMENT_SELECT_WIKI_USER_LOGIN");
 		STATEMENT_SELECT_WIKI_USER_SEQUENCE      = props.getProperty("STATEMENT_SELECT_WIKI_USER_SEQUENCE");
 		STATEMENT_SELECT_WIKI_USERS              = props.getProperty("STATEMENT_SELECT_WIKI_USERS");
 		STATEMENT_UPDATE_GROUP                   = props.getProperty("STATEMENT_UPDATE_GROUP");
 		STATEMENT_UPDATE_NAMESPACE               = props.getProperty("STATEMENT_UPDATE_NAMESPACE");
 		STATEMENT_UPDATE_RECENT_CHANGES_PREVIOUS_VERSION_ID = props.getProperty("STATEMENT_UPDATE_RECENT_CHANGES_PREVIOUS_VERSION_ID");
 		STATEMENT_UPDATE_TOPIC_NAMESPACE         = props.getProperty("STATEMENT_UPDATE_TOPIC_NAMESPACE");
 		STATEMENT_UPDATE_ROLE                    = props.getProperty("STATEMENT_UPDATE_ROLE");
 		STATEMENT_UPDATE_TOPIC                   = props.getProperty("STATEMENT_UPDATE_TOPIC");
 		STATEMENT_UPDATE_TOPIC_VERSION           = props.getProperty("STATEMENT_UPDATE_TOPIC_VERSION");
 		STATEMENT_UPDATE_TOPIC_VERSION_PREVIOUS_VERSION_ID = props.getProperty("STATEMENT_UPDATE_TOPIC_VERSION_PREVIOUS_VERSION_ID");
 		STATEMENT_UPDATE_USER                    = props.getProperty("STATEMENT_UPDATE_USER");
 		STATEMENT_UPDATE_USER_BLOCK              = props.getProperty("STATEMENT_UPDATE_USER_BLOCK");
 		STATEMENT_UPDATE_VIRTUAL_WIKI            = props.getProperty("STATEMENT_UPDATE_VIRTUAL_WIKI");
 		STATEMENT_UPDATE_WIKI_FILE               = props.getProperty("STATEMENT_UPDATE_WIKI_FILE");
 		STATEMENT_UPDATE_WIKI_USER               = props.getProperty("STATEMENT_UPDATE_WIKI_USER");
 	}
 
 	/**
 	 *
 	 */
 	private Category initCategory(ResultSet rs, String virtualWikiName) throws SQLException {
 		Category category = new Category();
 		category.setName("category_name");
 		category.setVirtualWiki(virtualWikiName);
 		category.setChildTopicName(rs.getString("topic_name"));
 		category.setSortKey(rs.getString("sort_key"));
 		category.setTopicType(TopicType.findTopicType(rs.getInt("topic_type")));
 		return category;
 	}
 
 	/**
 	 *
 	 * had to override the insertTopicVersion method for the Caché implementation
 	 * need this to be public so that CacheQueryHandler can use it
 	 */
 	public LogItem initLogItem(ResultSet rs, String virtualWikiName) throws SQLException {
 		LogItem logItem = new LogItem();
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			logItem.setUserId(userId);
 		}
 		logItem.setUserDisplayName(rs.getString("display_name"));
 		int topicId = rs.getInt("topic_id");
 		if (topicId > 0) {
 			logItem.setTopicId(topicId);
 		}
 		int topicVersionId = rs.getInt("topic_version_id");
 		if (topicVersionId > 0) {
 			logItem.setTopicVersionId(topicVersionId);
 		}
 		logItem.setLogDate(rs.getTimestamp("log_date"));
 		logItem.setLogComment(rs.getString("log_comment"));
 		logItem.setLogParamString(rs.getString("log_params"));
 		logItem.setLogType(rs.getInt("log_type"));
 		logItem.setLogSubType(rs.getInt("log_sub_type"));
 		logItem.setVirtualWiki(virtualWikiName);
 		return logItem;
 	}
 
 	/**
 	 * Initialize a recent change record from a result set.
 	 */
 	protected RecentChange initRecentChange(ResultSet rs) throws SQLException {
 		RecentChange change = new RecentChange();
 		int topicVersionId = rs.getInt("topic_version_id");
 		if (topicVersionId > 0) {
 			change.setTopicVersionId(topicVersionId);
 		}
 		int previousTopicVersionId = rs.getInt("previous_topic_version_id");
 		if (previousTopicVersionId > 0) {
 			change.setPreviousTopicVersionId(previousTopicVersionId);
 		}
 		int topicId = rs.getInt("topic_id");
 		if (topicId > 0) {
 			change.setTopicId(topicId);
 		}
 		change.setTopicName(rs.getString("topic_name"));
 		change.setCharactersChanged(rs.getInt("characters_changed"));
 		change.setChangeDate(rs.getTimestamp("change_date"));
 		change.setChangeComment(rs.getString("change_comment"));
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			change.setAuthorId(userId);
 		}
 		change.setAuthorName(rs.getString("display_name"));
 		int editType = rs.getInt("edit_type");
 		if (editType > 0) {
 			change.setEditType(editType);
 			change.initChangeWikiMessageForVersion(editType, rs.getString("log_params"));
 		}
 		int logType = rs.getInt("log_type");
 		Integer logSubType = (rs.getInt("log_sub_type") <= 0) ? null : rs.getInt("log_sub_type");
 		if (logType > 0) {
 			change.setLogType(logType);
 			change.setLogSubType(logSubType);
 			change.initChangeWikiMessageForLog(logType, logSubType, rs.getString("log_params"), change.getTopicVersionId());
 		}
 		change.setVirtualWiki(rs.getString("virtual_wiki_name"));
 		return change;
 	}
 
 	/**
 	 *
 	 */
 	private Role initRole(ResultSet rs) throws SQLException {
 		Role role = new Role(rs.getString("role_name"));
 		role.setDescription(rs.getString("role_description"));
 		return role;
 	}
 
 	/**
 	 *
 	 */
 	private Topic initTopic(ResultSet rs, String virtualWikiName) throws SQLException {
 		Topic topic = new Topic(virtualWikiName, rs.getString("topic_name"));
 		topic.setAdminOnly(rs.getInt("topic_admin_only") != 0);
 		int currentVersionId = rs.getInt("current_version_id");
 		if (currentVersionId > 0) {
 			topic.setCurrentVersionId(currentVersionId);
 		}
 		topic.setTopicContent(rs.getString("version_content"));
 		// FIXME - Oracle cannot store an empty string - it converts them
 		// to null - so add a hack to work around the problem.
 		if (topic.getTopicContent() == null) {
 			topic.setTopicContent("");
 		}
 		topic.setTopicId(rs.getInt("topic_id"));
 		topic.setReadOnly(rs.getInt("topic_read_only") != 0);
 		topic.setDeleteDate(rs.getTimestamp("delete_date"));
 		topic.setTopicType(TopicType.findTopicType(rs.getInt("topic_type")));
 		topic.setRedirectTo(rs.getString("redirect_to"));
 		// if a topic by this name has been deleted then there will be multiple results and
 		// the one we want is the last one.  due to the fact that the result set may be
 		// FORWARD_ONLY re-run this method for the remaining available results in the result
 		// set - it's inefficient, but safe.
 		if (rs.getTimestamp("delete_date") != null) {
 			// this is an inefficient way to get the last result, but due to the fact that
 			// the result set may be forward only it's the safest.
 			if (rs.next()) {
 				topic = this.initTopic(rs, virtualWikiName);
 			}
 		}
 		return topic;
 	}
 
 	/**
 	 *
 	 */
 	private TopicVersion initTopicVersion(ResultSet rs) throws SQLException {
 		TopicVersion topicVersion = new TopicVersion();
 		topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
 		topicVersion.setTopicId(rs.getInt("topic_id"));
 		topicVersion.setEditComment(rs.getString("edit_comment"));
 		topicVersion.setVersionContent(rs.getString("version_content"));
 		// FIXME - Oracle cannot store an empty string - it converts them
 		// to null - so add a hack to work around the problem.
 		if (topicVersion.getVersionContent() == null) {
 			topicVersion.setVersionContent("");
 		}
 		int previousTopicVersionId = rs.getInt("previous_topic_version_id");
 		if (previousTopicVersionId > 0) {
 			topicVersion.setPreviousTopicVersionId(previousTopicVersionId);
 		}
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			topicVersion.setAuthorId(userId);
 		}
 		topicVersion.setCharactersChanged(rs.getInt("characters_changed"));
 		topicVersion.setVersionParamString(rs.getString("version_params"));
 		topicVersion.setEditDate(rs.getTimestamp("edit_date"));
 		topicVersion.setEditType(rs.getInt("edit_type"));
 		topicVersion.setAuthorDisplay(rs.getString("wiki_user_display"));
 		return topicVersion;
 	}
 
 	/**
 	 *
 	 */
 	private UserBlock initUserBlock(ResultSet rs) throws SQLException {
 		Integer wikiUserId = (rs.getInt("wiki_user_id") > 0) ? rs.getInt("wiki_user_id") : null;
 		String ipAddress = rs.getString("ip_address");
 		Timestamp blockEndDate = rs.getTimestamp("block_end_date");
 		int blockedByUserId = rs.getInt("blocked_by_user_id");
 		UserBlock userBlock = new UserBlock(wikiUserId, ipAddress, blockEndDate, blockedByUserId);
 		userBlock.setBlockId(rs.getInt("user_block_id"));
 		userBlock.setBlockDate(rs.getTimestamp("block_date"));
 		userBlock.setBlockReason(rs.getString("block_reason"));
 		userBlock.setUnblockDate(rs.getTimestamp("unblock_date"));
 		userBlock.setUnblockReason(rs.getString("unblock_reason"));
 		int unblockedByUserId = rs.getInt("unblocked_by_user_id");
 		if (unblockedByUserId > 0) {
 			userBlock.setUnblockedByUserId(unblockedByUserId);
 		}
 		return userBlock;
 	}
 
 	/**
 	 *
 	 */
 	private WikiFile initWikiFile(ResultSet rs, String virtualWikiName) throws SQLException {
 		WikiFile wikiFile = new WikiFile();
 		wikiFile.setFileId(rs.getInt("file_id"));
 		wikiFile.setAdminOnly(rs.getInt("file_admin_only") != 0);
 		wikiFile.setFileName(rs.getString("file_name"));
 		wikiFile.setVirtualWiki(virtualWikiName);
 		wikiFile.setUrl(rs.getString("file_url"));
 		wikiFile.setTopicId(rs.getInt("topic_id"));
 		wikiFile.setReadOnly(rs.getInt("file_read_only") != 0);
 		wikiFile.setDeleteDate(rs.getTimestamp("delete_date"));
 		wikiFile.setMimeType(rs.getString("mime_type"));
 		wikiFile.setFileSize(rs.getInt("file_size"));
 		return wikiFile;
 	}
 
 	/**
 	 *
 	 */
 	private WikiFileVersion initWikiFileVersion(ResultSet rs) throws SQLException {
 		WikiFileVersion wikiFileVersion = new WikiFileVersion();
 		wikiFileVersion.setFileVersionId(rs.getInt("file_version_id"));
 		wikiFileVersion.setFileId(rs.getInt("file_id"));
 		wikiFileVersion.setUploadComment(rs.getString("upload_comment"));
 		wikiFileVersion.setUrl(rs.getString("file_url"));
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			wikiFileVersion.setAuthorId(userId);
 		}
 		wikiFileVersion.setUploadDate(rs.getTimestamp("upload_date"));
 		wikiFileVersion.setMimeType(rs.getString("mime_type"));
 		wikiFileVersion.setAuthorDisplay(rs.getString("wiki_user_display"));
 		wikiFileVersion.setFileSize(rs.getInt("file_size"));
 		return wikiFileVersion;
 	}
 
 	/**
 	 *
 	 */
 	private WikiGroup initWikiGroup(ResultSet rs) throws SQLException {
 		WikiGroup wikiGroup = new WikiGroup();
 		wikiGroup.setGroupId(rs.getInt("group_id"));
 		wikiGroup.setName(rs.getString("group_name"));
 		wikiGroup.setDescription(rs.getString("group_description"));
 		return wikiGroup;
 	}
 
 	/**
 	 *
 	 */
 	private WikiUser initWikiUser(ResultSet rs) throws SQLException {
 		String username = rs.getString("login");
 		WikiUser user = new WikiUser(username);
 		user.setUserId(rs.getInt("wiki_user_id"));
 		user.setDisplayName(rs.getString("display_name"));
 		user.setCreateDate(rs.getTimestamp("create_date"));
 		user.setLastLoginDate(rs.getTimestamp("last_login_date"));
 		user.setCreateIpAddress(rs.getString("create_ip_address"));
 		user.setLastLoginIpAddress(rs.getString("last_login_ip_address"));
 		user.setDefaultLocale(rs.getString("default_locale"));
 		user.setEmail(rs.getString("email"));
 		user.setEditor(rs.getString("editor"));
 		user.setSignature(rs.getString("signature"));
 		return user;
 	}
 
 	/**
 	 *
 	 */
 	public void insertCategories(List<Category> categoryList, int virtualWikiId, int topicId, Connection conn) throws SQLException {
 		if (topicId == -1) {
 			throw new SQLException("Invalid topicId passed to method AnsiQueryHandler.insertCategories");
 		}
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_CATEGORY);
 			for (Category category : categoryList) {
 				stmt.setInt(1, topicId);
 				stmt.setString(2, category.getName());
 				stmt.setString(3, category.getSortKey());
 				stmt.addBatch();
 			}
 			stmt.executeBatch();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertGroupAuthority(int groupId, String authority, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_AUTHORITY);
 			stmt.setInt(1, groupId);
 			stmt.setString(2, authority);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertGroupMember(String username, int groupId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_MEMBER);
 				int groupMemberId = this.nextGroupMemberId(conn);
 				stmt.setInt(index++, groupMemberId);
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT);
 			}
 			stmt.setString(index++, username);
 			stmt.setInt(index++, groupId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertInterwiki(Interwiki interwiki, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_INTERWIKI);
 			stmt.setString(1, interwiki.getInterwikiPrefix());
 			stmt.setString(2, interwiki.getInterwikiPattern());
 			stmt.setString(3, interwiki.getInterwikiDisplay());
 			stmt.setInt(4, interwiki.getInterwikiType());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertLogItem(LogItem logItem, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEM);
 			stmt.setTimestamp(1, logItem.getLogDate());
 			stmt.setInt(2, virtualWikiId);
 			if (logItem.getUserId() == null) {
 				stmt.setNull(3, Types.INTEGER);
 			} else {
 				stmt.setInt(3, logItem.getUserId());
 			}
 			stmt.setString(4, logItem.getUserDisplayName());
 			stmt.setInt(5, logItem.getLogType());
 			if (logItem.getLogSubType() == null) {
 				stmt.setNull(6, Types.INTEGER);
 			} else {
 				stmt.setInt(6, logItem.getLogSubType());
 			}
 			stmt.setString(7, logItem.getLogComment());
 			stmt.setString(8, logItem.getLogParamString());
 			if (logItem.getTopicId() == null) {
 				stmt.setNull(9, Types.INTEGER);
 			} else {
 				stmt.setInt(9, logItem.getTopicId());
 			}
 			if (logItem.getTopicVersionId() == null) {
 				stmt.setNull(10, Types.INTEGER);
 			} else {
 				stmt.setInt(10, logItem.getTopicVersionId());
 			}
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertRecentChange(RecentChange change, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_RECENT_CHANGE);
 			if (change.getTopicVersionId() == null) {
 				stmt.setNull(1, Types.INTEGER);
 			} else {
 				stmt.setInt(1, change.getTopicVersionId());
 			}
 			if (change.getPreviousTopicVersionId() == null) {
 				stmt.setNull(2, Types.INTEGER);
 			} else {
 				stmt.setInt(2, change.getPreviousTopicVersionId());
 			}
 			if (change.getTopicId() == null) {
 				stmt.setNull(3, Types.INTEGER);
 			} else {
 				stmt.setInt(3, change.getTopicId());
 			}
 			stmt.setString(4, change.getTopicName());
 			stmt.setTimestamp(5, change.getChangeDate());
 			stmt.setString(6, change.getChangeComment());
 			if (change.getAuthorId() == null) {
 				stmt.setNull(7, Types.INTEGER);
 			} else {
 				stmt.setInt(7, change.getAuthorId());
 			}
 			stmt.setString(8, change.getAuthorName());
 			if (change.getEditType() == null) {
 				stmt.setNull(9, Types.INTEGER);
 			} else {
 				stmt.setInt(9, change.getEditType());
 			}
 			stmt.setInt(10, virtualWikiId);
 			stmt.setString(11, change.getVirtualWiki());
 			if (change.getCharactersChanged() == null) {
 				stmt.setNull(12, Types.INTEGER);
 			} else {
 				stmt.setInt(12, change.getCharactersChanged());
 			}
 			if (change.getLogType() == null) {
 				stmt.setNull(13, Types.INTEGER);
 			} else {
 				stmt.setInt(13, change.getLogType());
 			}
 			if (change.getLogSubType() == null) {
 				stmt.setNull(14, Types.INTEGER);
 			} else {
 				stmt.setInt(14, change.getLogSubType());
 			}
 			stmt.setString(15, change.getParamString());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertRole(Role role, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_ROLE);
 			stmt.setString(1, role.getAuthority());
 			stmt.setString(2, role.getDescription());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertTopic(Topic topic, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC);
 				int topicId = this.nextTopicId(conn);
 				topic.setTopicId(topicId);
 				stmt.setInt(index++, topic.getTopicId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setInt(index++, virtualWikiId);
 			stmt.setString(index++, topic.getName());
 			stmt.setInt(index++, topic.getTopicType().id());
 			stmt.setInt(index++, (topic.getReadOnly() ? 1 : 0));
 			if (topic.getCurrentVersionId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, topic.getCurrentVersionId());
 			}
 			stmt.setTimestamp(index++, topic.getDeleteDate());
 			stmt.setInt(index++, (topic.getAdminOnly() ? 1 : 0));
 			stmt.setString(index++, topic.getRedirectTo());
 			stmt.setInt(index++, topic.getNamespace().getId());
 			stmt.setString(index++, topic.getPageName());
 			stmt.setString(index++, topic.getPageName().toLowerCase());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				topic.setTopicId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertTopicLinks(List<Topic> topicLinks, int topicId, Connection conn) throws SQLException {
 		if (topicId == -1) {
 			throw new SQLException("Invalid topicId passed to method AnsiQueryHandler.insertTopicLinks");
 		}
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_LINKS);
 			for (Topic topicLink : topicLinks) {
 				stmt.setInt(1, topicId);
 				stmt.setInt(2, topicLink.getNamespace().getId());
 				stmt.setString(3, topicLink.getPageName());
 				stmt.addBatch();
 			}
 			stmt.executeBatch();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertTopicVersion(TopicVersion topicVersion, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_VERSION);
 				int topicVersionId = this.nextTopicVersionId(conn);
 				topicVersion.setTopicVersionId(topicVersionId);
 				stmt.setInt(index++, topicVersion.getTopicVersionId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			if (topicVersion.getEditDate() == null) {
 				Timestamp editDate = new Timestamp(System.currentTimeMillis());
 				topicVersion.setEditDate(editDate);
 			}
 			stmt.setInt(index++, topicVersion.getTopicId());
 			stmt.setString(index++, topicVersion.getEditComment());
 			stmt.setString(index++, topicVersion.getVersionContent());
 			if (topicVersion.getAuthorId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, topicVersion.getAuthorId());
 			}
 			stmt.setInt(index++, topicVersion.getEditType());
 			stmt.setString(index++, topicVersion.getAuthorDisplay());
 			stmt.setTimestamp(index++, topicVersion.getEditDate());
 			if (topicVersion.getPreviousTopicVersionId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, topicVersion.getPreviousTopicVersionId());
 			}
 			stmt.setInt(index++, topicVersion.getCharactersChanged());
 			stmt.setString(index++, topicVersion.getVersionParamString());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				topicVersion.setTopicVersionId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertUserAuthority(String username, String authority, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_AUTHORITY);
 			stmt.setString(1, username);
 			stmt.setString(2, authority);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertUserBlock(UserBlock userBlock, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_USER_BLOCK);
 				int blockId = this.nextUserBlockId(conn);
 				userBlock.setBlockId(blockId);
 				stmt.setInt(index++, userBlock.getBlockId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_USER_BLOCK_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			if (userBlock.getWikiUserId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, userBlock.getWikiUserId());
 			}
 			stmt.setString(index++, userBlock.getIpAddress());
 			stmt.setTimestamp(index++, userBlock.getBlockDate());
 			stmt.setTimestamp(index++, userBlock.getBlockEndDate());
 			stmt.setString(index++, userBlock.getBlockReason());
 			stmt.setInt(index++, userBlock.getBlockedByUserId());
 			stmt.setTimestamp(index++, userBlock.getUnblockDate());
 			stmt.setString(index++, userBlock.getUnblockReason());
 			if (userBlock.getUnblockedByUserId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, userBlock.getUnblockedByUserId());
 			}
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				userBlock.setBlockId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertUserDetails(WikiUserDetails userDetails, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_USER);
 			stmt.setString(1, userDetails.getUsername());
 			stmt.setString(2, userDetails.getPassword());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
 				int virtualWikiId = this.nextVirtualWikiId(conn);
 				virtualWiki.setVirtualWikiId(virtualWikiId);
 				stmt.setInt(index++, virtualWiki.getVirtualWikiId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setString(index++, virtualWiki.getName());
 			stmt.setString(index++, (virtualWiki.isDefaultRootTopicName() ? null : virtualWiki.getRootTopicName()));
 			stmt.setString(index++, (virtualWiki.isDefaultLogoImageUrl() ? null : virtualWiki.getLogoImageUrl()));
 			stmt.setString(index++, (virtualWiki.isDefaultMetaDescription() ? null : virtualWiki.getMetaDescription()));
 			stmt.setString(index++, (virtualWiki.isDefaultSiteName() ? null : virtualWiki.getSiteName()));
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				virtualWiki.setVirtualWikiId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_WATCHLIST_ENTRY);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topicName);
 			stmt.setInt(3, userId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiFile(WikiFile wikiFile, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE);
 				int fileId = this.nextWikiFileId(conn);
 				wikiFile.setFileId(fileId);
 				stmt.setInt(index++, wikiFile.getFileId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setInt(index++, virtualWikiId);
 			stmt.setString(index++, wikiFile.getFileName());
 			stmt.setString(index++, wikiFile.getUrl());
 			stmt.setString(index++, wikiFile.getMimeType());
 			stmt.setInt(index++, wikiFile.getTopicId());
 			stmt.setTimestamp(index++, wikiFile.getDeleteDate());
 			stmt.setInt(index++, (wikiFile.getReadOnly() ? 1 : 0));
 			stmt.setInt(index++, (wikiFile.getAdminOnly() ? 1 : 0));
 			stmt.setLong(index++, wikiFile.getFileSize());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				wikiFile.setFileId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiFileVersion(WikiFileVersion wikiFileVersion, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE_VERSION);
 				int fileVersionId = this.nextWikiFileVersionId(conn);
 				wikiFileVersion.setFileVersionId(fileVersionId);
 				stmt.setInt(index++, wikiFileVersion.getFileVersionId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			if (wikiFileVersion.getUploadDate() == null) {
 				Timestamp uploadDate = new Timestamp(System.currentTimeMillis());
 				wikiFileVersion.setUploadDate(uploadDate);
 			}
 			stmt.setInt(index++, wikiFileVersion.getFileId());
 			stmt.setString(index++, wikiFileVersion.getUploadComment());
 			stmt.setString(index++, wikiFileVersion.getUrl());
 			if (wikiFileVersion.getAuthorId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, wikiFileVersion.getAuthorId());
 			}
 			stmt.setString(index++, wikiFileVersion.getAuthorDisplay());
 			stmt.setTimestamp(index++, wikiFileVersion.getUploadDate());
 			stmt.setString(index++, wikiFileVersion.getMimeType());
 			stmt.setLong(index++, wikiFileVersion.getFileSize());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				wikiFileVersion.setFileVersionId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiGroup(WikiGroup group, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP);
 				int groupId = this.nextWikiGroupId(conn);
 				group.setGroupId(groupId);
 				stmt.setInt(index++, group.getGroupId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setString(index++, group.getName());
 			stmt.setString(index++, group.getDescription());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				group.setGroupId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiUser(WikiUser user, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_USER);
 				int nextUserId = this.nextWikiUserId(conn);
 				user.setUserId(nextUserId);
 				stmt.setInt(index++, user.getUserId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setString(index++, user.getUsername());
 			stmt.setString(index++, user.getDisplayName());
 			stmt.setTimestamp(index++, user.getCreateDate());
 			stmt.setTimestamp(index++, user.getLastLoginDate());
 			stmt.setString(index++, user.getCreateIpAddress());
 			stmt.setString(index++, user.getLastLoginIpAddress());
 			stmt.setString(index++, user.getDefaultLocale());
 			stmt.setString(index++, user.getEmail());
 			stmt.setString(index++, user.getEditor());
 			stmt.setString(index++, user.getSignature());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				user.setUserId(rs.getInt(1));
 			}
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> lookupCategoryTopics(int virtualWikiId, String virtualWikiName, String categoryName) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_CATEGORY_TOPICS);
 			// category name must be lowercase since search is case-insensitive
 			categoryName = categoryName.toLowerCase();
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, categoryName);
 			rs = stmt.executeQuery();
 			List<Category> results = new ArrayList<Category>();
 			while (rs.next()) {
 				results.add(this.initCategory(rs, virtualWikiName));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Map<String, String> lookupConfiguration() throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Map<String, String> configuration = new HashMap<String, String>();
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_CONFIGURATION);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				// note that the value must be trimmed since Oracle cannot store empty
 				// strings (it converts them to NULL) so empty config values are stored
 				// as " ".
 				configuration.put(rs.getString("config_key"), rs.getString("config_value").trim());
 			}
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 		return configuration;
 	}
 
 	/**
 	 *
 	 */
 	public List<Interwiki> lookupInterwikis(Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Map<String, Interwiki> interwikis = new TreeMap<String, Interwiki>();
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_INTERWIKIS);
 			rs = stmt.executeQuery();
 			String interwikiPrefix, interwikiPattern, interwikiDisplay;
 			int interwikiType;
 			while (rs.next()) {
 				interwikiPrefix = rs.getString("interwiki_prefix");
 				interwikiPattern = rs.getString("interwiki_pattern");
 				interwikiDisplay = rs.getString("interwiki_display");
 				interwikiType = rs.getInt("interwiki_type");
 				Interwiki interwiki = new Interwiki(interwikiPrefix, interwikiPattern, interwikiDisplay);
 				interwiki.setInterwikiType(interwikiType);
 				interwikis.put(interwikiPrefix, interwiki);
 			}
 		} finally {
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 		return new ArrayList<Interwiki>(interwikis.values());
 	}
 
 	/**
 	 *
 	 */
 	public List<Namespace> lookupNamespaces(Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Map<Integer, Namespace> namespaces = new TreeMap<Integer, Namespace>();
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_NAMESPACES);
 			rs = stmt.executeQuery();
 			// because there is no consistent way to sort null keys, get all data and then
 			// create Namespace objects by initializing main namespaces first, then the talk
 			// namespaces that reference the main namespace.
 			Map<Integer, Namespace> talkNamespaces = new HashMap<Integer, Namespace>();
 			while (rs.next()) {
 				int namespaceId = rs.getInt("namespace_id");
 				Namespace namespace = namespaces.get(namespaceId);
 				if (namespace == null) {
 					String namespaceLabel = rs.getString("namespace");
 					namespace = new Namespace(namespaceId, namespaceLabel);
 				}
 				String virtualWiki = rs.getString("virtual_wiki_name");
 				String namespaceTranslation = rs.getString("namespace_translation");
 				if (virtualWiki != null) {
 					namespace.getNamespaceTranslations().put(virtualWiki, namespaceTranslation);
 				}
 				namespaces.put(namespaceId, namespace);
 				int mainNamespaceId = rs.getInt("main_namespace_id");
 				if (!rs.wasNull()) {
 					talkNamespaces.put(mainNamespaceId, namespace);
 				}
 			}
 			for (int mainNamespaceId : talkNamespaces.keySet()) {
 				Namespace mainNamespace = namespaces.get(mainNamespaceId);
 				if (mainNamespace == null) {
 					logger.warn("Invalid namespace reference - bad database data.  Namespace references invalid main namespace with ID " + mainNamespaceId);
 				}
 				Namespace talkNamespace = talkNamespaces.get(mainNamespaceId);
 				talkNamespace.setMainNamespace(mainNamespace);
 				namespaces.put(talkNamespace.getId(), talkNamespace);
 			}
 		} finally {
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 		return new ArrayList<Namespace>(namespaces.values());
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(int virtualWikiId, String virtualWikiName, Namespace namespace, String pageName, Connection conn) throws SQLException {
 		if (namespace.getId().equals(Namespace.SPECIAL_ID)) {
 			// invalid namespace
 			return null;
 		}
 		boolean closeConnection = (conn == null);
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Topic topic = null;
 		try {
 			if (conn == null) {
 				conn = DatabaseConnection.getConnection();
 			}
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC);
 			stmt.setString(1, pageName);
 			stmt.setInt(2, virtualWikiId);
 			stmt.setInt(3, namespace.getId());
 			rs = stmt.executeQuery();
 			topic = (rs.next() ? this.initTopic(rs, virtualWikiName) : null);
 		} finally {
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 		try {
 			if (topic == null && !namespace.isCaseSensitive() && !pageName.toLowerCase().equals(pageName)) {
 				stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_LOWER);
 				stmt.setString(1, pageName.toLowerCase());
 				stmt.setInt(2, virtualWikiId);
 				stmt.setInt(3, namespace.getId());
 				rs = stmt.executeQuery();
 				topic = (rs.next() ? this.initTopic(rs, virtualWikiName) : null);
 			}
 			return topic;
 		} finally {
 			if (closeConnection) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			} else {
 				// close only the statement and result set - leave the connection open for further use
 				DatabaseConnection.closeConnection(null, stmt, rs);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopicById(int virtualWikiId, String virtualWikiName, int topicId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_BY_ID);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, topicId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initTopic(rs, virtualWikiName) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Map<Integer, String> lookupTopicByType(int virtualWikiId, TopicType topicType1, TopicType topicType2, int namespaceStart, int namespaceEnd, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.lookupTopicByTypeStatement(conn, virtualWikiId, topicType1, topicType2, namespaceStart, namespaceEnd, pagination);
 			rs = stmt.executeQuery();
 			Map<Integer, String> results = new LinkedHashMap<Integer, String>();
 			while (rs.next()) {
 				results.put(rs.getInt("topic_id"), rs.getString("topic_name"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement lookupTopicByTypeStatement(Connection conn, int virtualWikiId, TopicType topicType1, TopicType topicType2, int namespaceStart, int namespaceEnd, Pagination pagination) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_BY_TYPE);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setInt(2, topicType1.id());
 		stmt.setInt(3, topicType2.id());
 		stmt.setInt(4, namespaceStart);
 		stmt.setInt(5, namespaceEnd);
 		stmt.setInt(6, pagination.getNumResults());
 		stmt.setInt(7, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 *
 	 */
 	public int lookupTopicCount(int virtualWikiId, int namespaceStart, int namespaceEnd) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_COUNT);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, namespaceStart);
 			stmt.setInt(3, namespaceEnd);
 			stmt.setInt(4, TopicType.REDIRECT.id());
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("topic_count") : 0;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public String lookupTopicName(int virtualWikiId, String virtualWikiName, Namespace namespace, String pageName) throws SQLException {
 		if (namespace.getId().equals(Namespace.SPECIAL_ID)) {
 			// invalid namespace
 			return null;
 		}
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String topicName = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_NAME);
 			stmt.setString(1, pageName);
 			stmt.setInt(2, virtualWikiId);
 			stmt.setInt(3, namespace.getId());
 			rs = stmt.executeQuery();
 			topicName = (rs.next() ? rs.getString("topic_name") : null);
 		} finally {
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 		try {
 			if (topicName == null && !namespace.isCaseSensitive() && !pageName.toLowerCase().equals(pageName)) {
 				stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_NAME_LOWER);
 				stmt.setString(1, pageName.toLowerCase());
 				stmt.setInt(2, virtualWikiId);
 				stmt.setInt(3, namespace.getId());
 				rs = stmt.executeQuery();
 				topicName = (rs.next() ? rs.getString("topic_name") : null);
 			}
 			return topicName;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupTopicLinks(int virtualWikiId, Namespace namespace, String pageName) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_LINKS);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, namespace.getId());
 			stmt.setString(3, pageName);
 			rs = stmt.executeQuery();
 			List<String> results = new ArrayList<String>();
 			while (rs.next()) {
 				results.add(rs.getString("topic_name"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupTopicLinkOrphans(int virtualWikiId, int namespaceId) throws SQLException{
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_LINK_ORPHANS);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, namespaceId);
 			stmt.setInt(3, TopicType.REDIRECT.id());
 			rs = stmt.executeQuery();
 			List<String> results = new ArrayList<String>();
 			while (rs.next()) {
 				results.add(rs.getString("topic_name"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Map<Integer, String> lookupTopicNames(int virtualWikiId, boolean includeDeleted, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_NAMES);
 			stmt.setInt(1, virtualWikiId);
 			rs = stmt.executeQuery();
 			Map<Integer, String> results = new LinkedHashMap<Integer, String>();
 			while (rs.next()) {
 				if (includeDeleted || rs.getTimestamp("delete_date") == null) {
 					results.put(rs.getInt("topic_id"), rs.getString("topic_name"));
 				}
 			}
 			return results;
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(int topicVersionId) throws SQLException {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			return this.lookupTopicVersion(topicVersionId, conn);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 * Private version of lookupTopicVersion that works with an existing connection
 	 * to allow lookups as part of a transaction.
 	 */
 	private TopicVersion lookupTopicVersion(int topicVersionId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSION);
 			stmt.setInt(1, topicVersionId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initTopicVersion(rs) : null;
 		} finally {
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Integer lookupTopicVersionNextId(int topicVersionId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSION_NEXT_ID);
 			stmt.setInt(1, topicVersionId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("topic_version_id") : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiFile lookupWikiFile(int virtualWikiId, String virtualWikiName, int topicId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_FILE);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, topicId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initWikiFile(rs, virtualWikiName) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 * Return a count of all wiki files currently available on the Wiki.  This
 	 * method excludes deleted files.
 	 *
 	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the files
 	 *  being retrieved.
 	 */
 	public int lookupWikiFileCount(int virtualWikiId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_FILE_COUNT);
 			stmt.setInt(1, virtualWikiId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("file_count") : 0;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiGroup lookupWikiGroup(String groupName) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_GROUP);
 			stmt.setString(1, groupName);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initWikiGroup(rs) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(int userId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER);
 			stmt.setInt(1, userId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initWikiUser(rs) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public int lookupWikiUser(String username, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_LOGIN);
 			stmt.setString(1, username);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("wiki_user_id") : -1;
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 * Return a count of all wiki users.
 	 */
 	public int lookupWikiUserCount() throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_COUNT);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("user_count") : 0;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public String lookupWikiUserEncryptedPassword(String username) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD);
 			stmt.setString(1, username);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getString("password") : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupWikiUsers(Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = this.lookupWikiUsersStatement(conn, pagination);
 			rs = stmt.executeQuery();
 			List<String> results = new ArrayList<String>();
 			while (rs.next()) {
 				results.add(rs.getString("login"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected PreparedStatement lookupWikiUsersStatement(Connection conn, Pagination pagination) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USERS);
 		stmt.setInt(1, pagination.getNumResults());
 		stmt.setInt(2, pagination.getOffset());
 		return stmt;
 	}
 
 	/**
 	 * Retrieve the next available group member id from the group members table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available group member id from the group members table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextGroupMemberId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE, "id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available topic id from the topic table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available topic id from the topic table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextTopicId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_TOPIC_SEQUENCE, "topic_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available topic version id from the topic version table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available topic version id from the topic version table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 * 
 	 * had to override the insertTopicVersion method for the Caché implementation
 	 * need this to be public so that CacheQueryHandler can use it
 	 */
 	public int nextTopicVersionId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE, "topic_version_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available user block id from the user block table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available user block id from the user block table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextUserBlockId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_USER_BLOCKS_SEQUENCE, "user_block_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available virtual wiki id from the virtual wiki table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available virtual wiki id from the virtual wiki table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextVirtualWikiId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE, "virtual_wiki_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki file id from the wiki file table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki file id from the wiki file table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiFileId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_WIKI_FILE_SEQUENCE, "file_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki file version id from the wiki file
 	 * version table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki file version id from the wiki file
 	 *  version table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiFileVersionId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE, "file_version_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki group id from the wiki group table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki group id from the wiki group table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiGroupId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_GROUP_SEQUENCE, "group_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki user id from the wiki user table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki user id from the wiki user table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiUserId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE, "wiki_user_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 *
 	 */
 	public void reloadLogItems(int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_LOG_ITEMS);
 			stmt.setInt(1, virtualWikiId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE);
 			stmt.setInt(1, LogItem.LOG_TYPE_DELETE);
 			stmt.setString(2, "");
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_DELETE);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE);
 			stmt.setInt(1, LogItem.LOG_TYPE_DELETE);
 			stmt.setString(2, "|" + TopicVersion.EDIT_UNDELETE);
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_UNDELETE);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE);
 			stmt.setInt(1, LogItem.LOG_TYPE_PERMISSION);
 			stmt.setString(2, "");
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_PERMISSION);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_IMPORT);
 			stmt.setInt(1, LogItem.LOG_TYPE_IMPORT);
 			stmt.setInt(2, TopicVersion.EDIT_IMPORT);
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_IMPORT);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_MOVE);
 			stmt.setInt(1, LogItem.LOG_TYPE_MOVE);
 			stmt.setInt(2, virtualWikiId);
 			stmt.setInt(3, TopicVersion.EDIT_MOVE);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_UPLOAD);
 			stmt.setInt(1, LogItem.LOG_TYPE_UPLOAD);
 			stmt.setInt(2, virtualWikiId);
 			stmt.setInt(3, TopicVersion.EDIT_NORMAL);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_USER);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, LogItem.LOG_TYPE_USER_CREATION);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_BLOCK);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, LogItem.LOG_TYPE_BLOCK);
 			stmt.setInt(3, LogItem.LOG_SUBTYPE_BLOCK_BLOCK);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_UNBLOCK);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, LogItem.LOG_TYPE_BLOCK);
 			stmt.setInt(3, LogItem.LOG_SUBTYPE_BLOCK_UNBLOCK);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void orderTopicVersions(Topic topic, int virtualWikiId, List<Integer> topicVersionIdList) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			conn.setAutoCommit(false);
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_TOPIC_VERSION_PREVIOUS_VERSION_ID);
 			Integer previousTopicVersionId = null;
 			boolean hasBatchData = false;
 			for (int topicVersionId : topicVersionIdList) {
 				if (previousTopicVersionId != null) {
 					stmt.setInt(1, previousTopicVersionId);
 					stmt.setInt(2, topicVersionId);
 					stmt.addBatch();
 					hasBatchData = true;
 				}
 				previousTopicVersionId = topicVersionId;
 			}
 			if (hasBatchData) {
 				stmt.executeBatch();
 			}
 			TopicVersion topicVersion = this.lookupTopicVersion(previousTopicVersionId, conn);
 			topic.setCurrentVersionId(previousTopicVersionId);
 			topic.setTopicContent(topicVersion.getVersionContent());
 			this.updateTopic(topic, virtualWikiId, conn);
 			conn.commit();
 		} catch (SQLException e) {
 			if (conn != null) {
 				try {
 					conn.rollback();
 				} catch (Exception ex) {}
 			}
 			throw e;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void reloadRecentChanges(Connection conn, int limit) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DELETE_RECENT_CHANGES, conn);
 			stmt = conn.prepareStatement(STATEMENT_INSERT_RECENT_CHANGES_VERSIONS);
 			stmt.setInt(1, limit);
 			stmt.executeUpdate();
 			DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES_LOGS, conn);
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateConfiguration(Map<String, String> configuration, Connection conn) throws SQLException {
 		Statement stmt = null;
 		PreparedStatement pstmt = null;
 		try {
 			stmt = conn.createStatement();
 			stmt.executeUpdate(STATEMENT_DELETE_CONFIGURATION);
 			pstmt = conn.prepareStatement(STATEMENT_INSERT_CONFIGURATION);
 			for (String key : configuration.keySet()) {
 				pstmt.setString(1, key);
 				// FIXME - Oracle cannot store an empty string - it converts them
 				// to null - so add a hack to work around the problem.
 				String value = configuration.get(key);
 				if (StringUtils.isBlank(value)) {
 					value = " ";
 				}
 				pstmt.setString(2, value);
 				pstmt.addBatch();
 			}
 			pstmt.executeBatch();
 		} finally {
 			DatabaseConnection.closeStatement(pstmt);
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateNamespace(Namespace mainNamespace, Namespace commentsNamespace, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			// update if the ID is specified AND a namespace with the same ID already exists
 			boolean isUpdate = (mainNamespace.getId() != null && this.lookupNamespaces(conn).indexOf(mainNamespace) != -1);
 			// if adding determing the namespace ID(s)
 			if (!isUpdate && mainNamespace.getId() == null) {
 				// note - this returns the last id in the system, so add one
 				int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_NAMESPACE_SEQUENCE, "namespace_id", conn);
 				if (nextId < 200) {
 					// for custom namespaces start with IDs of 200 or more to leave room for future expansion
 					nextId = 199;
 				}
 				mainNamespace.setId(nextId + 1);
 				if (commentsNamespace != null) {
 					commentsNamespace.setId(nextId + 2);
 				}
 			}
 			// execute the adds/updates
 			stmt = (isUpdate) ? conn.prepareStatement(STATEMENT_UPDATE_NAMESPACE) : conn.prepareStatement(STATEMENT_INSERT_NAMESPACE);
 			stmt.setString(1, mainNamespace.getDefaultLabel());
 			stmt.setNull(2, Types.INTEGER);
 			stmt.setInt(3, mainNamespace.getId());
 			stmt.addBatch();
 			if (commentsNamespace != null) {
 				stmt.setString(1, commentsNamespace.getDefaultLabel());
 				stmt.setInt(2, commentsNamespace.getMainNamespace().getId());
 				stmt.setInt(3, commentsNamespace.getId());
 				stmt.addBatch();
 			}
 			stmt.executeBatch();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateNamespaceTranslations(List<Namespace> namespaces, String virtualWiki, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			// delete any existing translation then add the new one
 			stmt = conn.prepareStatement(STATEMENT_DELETE_NAMESPACE_TRANSLATIONS);
 			stmt.setInt(1, virtualWikiId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_NAMESPACE_TRANSLATION);
 			String translatedNamespace;
 			for (Namespace namespace : namespaces) {
 				translatedNamespace = namespace.getLabel(virtualWiki);
 				if (translatedNamespace.equals(namespace.getDefaultLabel())) {
 					continue;
 				}
 				stmt.setInt(1, namespace.getId());
 				stmt.setInt(2, virtualWikiId);
 				stmt.setString(3, translatedNamespace);
 				stmt.addBatch();
 			}
 			stmt.executeBatch();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateRole(Role role, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_ROLE);
 			stmt.setString(1, role.getDescription());
 			stmt.setString(2, role.getAuthority());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateTopic(Topic topic, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_TOPIC);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topic.getName());
 			stmt.setInt(3, topic.getTopicType().id());
 			stmt.setInt(4, (topic.getReadOnly() ? 1 : 0));
 			if (topic.getCurrentVersionId() == null) {
 				stmt.setNull(5, Types.INTEGER);
 			} else {
 				stmt.setInt(5, topic.getCurrentVersionId());
 			}
 			stmt.setTimestamp(6, topic.getDeleteDate());
 			stmt.setInt(7, (topic.getAdminOnly() ? 1 : 0));
 			stmt.setString(8, topic.getRedirectTo());
 			stmt.setInt(9, topic.getNamespace().getId());
 			stmt.setString(10, topic.getPageName());
 			stmt.setString(11, topic.getPageName().toLowerCase());
 			stmt.setInt(12, topic.getTopicId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateTopicNamespaces(List<Topic> topics, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_TOPIC_NAMESPACE);
 			for (Topic topic : topics) {
 				stmt.setInt(1, topic.getNamespace().getId());
 				stmt.setString(2, topic.getPageName());
 				stmt.setString(3, topic.getPageName().toLowerCase());
 				stmt.setInt(4, topic.getTopicId());
 				stmt.addBatch();
 			}
 			stmt.executeBatch();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateTopicVersion(TopicVersion topicVersion, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_TOPIC_VERSION);
 			stmt.setInt(1, topicVersion.getTopicId());
 			stmt.setString(2, topicVersion.getEditComment());
 			stmt.setString(3, topicVersion.getVersionContent());
 			if (topicVersion.getAuthorId() == null) {
 				stmt.setNull(4, Types.INTEGER);
 			} else {
 				stmt.setInt(4, topicVersion.getAuthorId());
 			}
 			stmt.setInt(5, topicVersion.getEditType());
 			stmt.setString(6, topicVersion.getAuthorDisplay());
 			stmt.setTimestamp(7, topicVersion.getEditDate());
 			if (topicVersion.getPreviousTopicVersionId() == null) {
 				stmt.setNull(8, Types.INTEGER);
 			} else {
 				stmt.setInt(8, topicVersion.getPreviousTopicVersionId());
 			}
 			stmt.setInt(9, topicVersion.getCharactersChanged());
 			stmt.setString(10, topicVersion.getVersionParamString());
 			stmt.setInt(11, topicVersion.getTopicVersionId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateUserBlock(UserBlock userBlock, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_USER_BLOCK);
 			if (userBlock.getWikiUserId() == null) {
 				stmt.setNull(1, Types.INTEGER);
 			} else {
 				stmt.setInt(1, userBlock.getWikiUserId());
 			}
 			stmt.setString(2, userBlock.getIpAddress());
 			stmt.setTimestamp(3, userBlock.getBlockDate());
 			stmt.setTimestamp(4, userBlock.getBlockEndDate());
 			stmt.setString(5, userBlock.getBlockReason());
 			stmt.setInt(6, userBlock.getBlockedByUserId());
 			stmt.setTimestamp(7, userBlock.getUnblockDate());
 			stmt.setString(8, userBlock.getUnblockReason());
 			if (userBlock.getUnblockedByUserId() == null) {
 				stmt.setNull(9, Types.INTEGER);
 			} else {
 				stmt.setInt(9, userBlock.getUnblockedByUserId());
 			}
 			stmt.setInt(10, userBlock.getBlockId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateUserDetails(WikiUserDetails userDetails, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_USER);
 			stmt.setString(1, userDetails.getPassword());
 			stmt.setInt(2, 1);
 			stmt.setString(3, userDetails.getUsername());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_VIRTUAL_WIKI);
 			stmt.setString(1, (virtualWiki.isDefaultRootTopicName() ? null : virtualWiki.getRootTopicName()));
 			stmt.setString(2, (virtualWiki.isDefaultLogoImageUrl() ? null : virtualWiki.getLogoImageUrl()));
 			stmt.setString(3, (virtualWiki.isDefaultMetaDescription() ? null : virtualWiki.getMetaDescription()));
 			stmt.setString(4, (virtualWiki.isDefaultSiteName() ? null : virtualWiki.getSiteName()));
 			stmt.setInt(5, virtualWiki.getVirtualWikiId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiFile(WikiFile wikiFile, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_WIKI_FILE);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, wikiFile.getFileName());
 			stmt.setString(3, wikiFile.getUrl());
 			stmt.setString(4, wikiFile.getMimeType());
 			stmt.setInt(5, wikiFile.getTopicId());
 			stmt.setTimestamp(6, wikiFile.getDeleteDate());
 			stmt.setInt(7, (wikiFile.getReadOnly() ? 1 : 0));
 			stmt.setInt(8, (wikiFile.getAdminOnly() ? 1 : 0));
 			stmt.setLong(9, wikiFile.getFileSize());
 			stmt.setInt(10, wikiFile.getFileId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiGroup(WikiGroup group, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_GROUP);
 			stmt.setString(1, group.getName());
 			stmt.setString(2, group.getDescription());
 			stmt.setInt(3, group.getGroupId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiUser(WikiUser user, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_WIKI_USER);
 			stmt.setString(1, user.getUsername());
 			stmt.setString(2, user.getDisplayName());
 			stmt.setTimestamp(3, user.getLastLoginDate());
 			stmt.setString(4, user.getLastLoginIpAddress());
 			stmt.setString(5, user.getDefaultLocale());
 			stmt.setString(6, user.getEmail());
 			stmt.setString(7, user.getEditor());
 			stmt.setString(8, user.getSignature());
 			stmt.setInt(9, user.getUserId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 }
