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
 package org.jamwiki.persistency.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Locale;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.persistency.PersistencyHandler;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.Utilities;
 
 /**
  *
  */
 public class DatabaseHandler extends PersistencyHandler {
 
 	public static final String DB_TYPE_ORACLE = "oracle";
 	public static final String DB_TYPE_MYSQL = "mysql";
 	private static final String INIT_SCRIPT_ANSI = "create_ansi.sql";
 	private static final String INIT_SCRIPT_ORACLE = "create_oracle.sql";
 	private static final Logger logger = Logger.getLogger(DatabaseHandler.class);
 	private static Hashtable virtualWikiIdHash = null;
 	private static Hashtable virtualWikiNameHash = null;
 
 	private static final String STATEMENT_INSERT_TOPIC =
 		"insert into jam_topic ( "
 		+   "topic_id, virtual_wiki_id, topic_name, topic_type, "
 		+   "topic_locked_by, topic_lock_date, topic_read_only, topic_content, "
 		+   "topic_lock_session_key "
 		+ ") values ( "
 		+   "?, ?, ?, ?, ?, ?, ?, ?, ?"
 		+ ") ";
 	private static final String STATEMENT_INSERT_TOPIC_VERSION =
 		"insert into jam_topic_version ("
 		+   "topic_version_id, topic_id, edit_comment, version_content, "
 		+   "wiki_user_id, edit_type, wiki_user_ip_address, edit_date "
 		+ ") values ( "
 		+   "?, ?, ?, ?, ?, ?, ?, ? "
 		+ ") ";
 	private static final String STATEMENT_INSERT_RECENT_CHANGE =
 		"insert into jam_recent_change ("
 		+   "topic_version_id, previous_topic_version_id, topic_id, "
 		+   "topic_name, edit_date, edit_comment, wiki_user_id, "
 		+   "display_name, edit_type, virtual_wiki_id, virtual_wiki_name "
 		+ ") values ( "
 		+   "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
 		+ ") ";
 	private static final String STATEMENT_INSERT_RECENT_CHANGES =
 		"INSERT INTO jam_recent_change ( "
 		+   "topic_version_id, topic_id, "
 		+   "topic_name, edit_date, wiki_user_id, display_name, "
 		+   "edit_type, virtual_wiki_id, virtual_wiki_name, edit_comment "
 		+ ") "
 		+ "SELECT "
 		+   "jam_topic_version.topic_version_id, jam_topic.topic_id, "
 		+   "jam_topic.topic_name, jam_topic_version.edit_date, "
 		+   "jam_topic_version.wiki_user_id, jam_wiki_user.display_name, "
 		+   "jam_topic_version.edit_type, jam_virtual_wiki.virtual_wiki_id, "
 		+   "jam_virtual_wiki.virtual_wiki_name, jam_topic_version.edit_comment "
 		+ "FROM jam_topic, jam_topic_version, jam_wiki_user, jam_virtual_wiki "
 		+ "WHERE jam_topic.topic_id = jam_topic_version.topic_id "
 		+ "AND jam_topic_version.wiki_user_id = jam_wiki_user.wiki_user_id "
 		+ "AND jam_topic.virtual_wiki_id = jam_virtual_wiki.virtual_wiki_id "
 		+ "AND jam_topic.topic_deleted = FALSE ";
 	private static final String STATEMENT_INSERT_VIRTUAL_WIKI =
 		"insert into jam_virtual_wiki ("
 		+   "virtual_wiki_id, virtual_wiki_name "
 		+ ") values ( "
 		+   "?, ? "
 		+ ") ";
 	private static final String STATEMENT_INSERT_WIKI_USER =
 		"insert into jam_wiki_user ("
 		+   "wiki_user_id, login, display_name, create_date, "
 		+   "last_login_date, create_ip_address, last_login_ip_address, "
 		+   "is_admin "
 		+ ") values ( "
		+   "?, ?, ?, ?, ?, ?, ?, ? "
 		+ ") ";
 	private static final String STATEMENT_INSERT_WIKI_USER_INFO =
 		"insert into jam_wiki_user_info ("
 		+   "wiki_user_id, login, email, first_name, last_name, encoded_password "
 		+ ") values ( "
 		+   "?, ?, ?, ?, ?, ? "
 		+ ") ";
 	private static final String STATEMENT_SELECT_RECENT_CHANGES =
 		"select * from jam_recent_change "
 		+ "where virtual_wiki_name = ? "
 		+ "order by edit_date desc "
 		+ "limit ? ";
 	private static final String STATEMENT_SELECT_TOPIC =
 		"select * from jam_topic "
 		+ "where virtual_wiki_id = ? "
 		+ "and topic_name = ? ";
 	private static final String STATEMENT_SELECT_TOPICS =
 		"select * from jam_topic "
 		+ "where virtual_wiki_id = ? "
 		+ "and topic_deleted = FALSE ";
 	private static final String STATEMENT_SELECT_TOPIC_READ_ONLY =
 		"select * from jam_topic "
 		+ "where virtual_wiki_id = ? "
 		+ "and topic_read_only = ? "
 		+ "and topic_deleted = FALSE ";
 	private static final String STATEMENT_SELECT_TOPIC_LOCKED =
 		"select * from jam_topic "
 		+ "where virtual_wiki_id = ? "
 		+ "and topic_lock_session_key is not null "
 		+ "and topic_deleted = FALSE ";
 	private static final String STATEMENT_SELECT_TOPIC_SEQUENCE =
 		"select nextval('jam_topic_seq') as topic_id ";
 	private static final String STATEMENT_SELECT_TOPIC_VERSION =
 		"select * from jam_topic_version "
 		+ "where topic_version_id = ? ";
 	private static final String STATEMENT_SELECT_TOPIC_VERSIONS =
 		"select * from jam_topic_version "
 		+ "where topic_id = ? "
 		+ "order by topic_version_id desc ";
 	private static final String STATEMENT_SELECT_TOPIC_VERSION_LAST =
 		"select max(topic_version_id) as topic_version_id from jam_topic_version "
 		+ "where topic_id = ? ";
 	private static final String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE =
 		"select nextval('jam_topic_version_seq') as topic_version_id ";
 	private static final String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE =
 		"select nextval('jam_virtual_wiki_seq') as virtual_wiki_id ";
 	private static final String STATEMENT_SELECT_WIKI_USER_SEQUENCE =
 		"select nextval('jam_wiki_user_seq') as wiki_user_id ";
 	private static final String STATEMENT_SELECT_WIKI_USER =
 	    "select jam_wiki_user.wiki_user_id, jam_wiki_user.login, "
 	    +   "jam_wiki_user.display_name, jam_wiki_user.create_date, "
 	    +   "jam_wiki_user.last_login_date, jam_wiki_user.create_ip_address, "
 	    +   "jam_wiki_user.last_login_ip_address, jam_wiki_user.is_admin, "
 	    +   "jam_wiki_user_info.email, jam_wiki_user_info.first_name, "
 	    +   "jam_wiki_user_info.last_name, jam_wiki_user_info.encoded_password "
 	    + "from jam_wiki_user "
 	    + "left outer join jam_wiki_user_info "
 	    + "on (jam_wiki_user.wiki_user_id = jam_wiki_user_info.wiki_user_id) "
 	    + "where jam_wiki_user.wiki_user_id = ? ";
 	private static final String STATEMENT_SELECT_WIKI_USER_PASSWORD =
 	    "select wiki_user_id from jam_wiki_user_info "
 	    + "where login = ? "
 	    + "and encoded_password = ? ";
 	private static final String STATEMENT_SELECT_WIKI_USER_LOGIN =
 	    "select wiki_user_id from jam_wiki_user_info "
 	    + "where login = ? ";
 	private static final String STATEMENT_SELECT_WIKI_USER_LOGINS =
 	    "select login from jam_wiki_user_info ";
 	private static final String STATEMENT_UPDATE_TOPIC =
 		"update jam_topic set "
 		+ "virtual_wiki_id = ?, "
 		+ "topic_name = ?, "
 		+ "topic_type = ?, "
 		+ "topic_locked_by = ?, "
 		+ "topic_lock_date = ?, "
 		+ "topic_read_only = ?, "
 		+ "topic_content = ?, "
 		+ "topic_lock_session_key = ?, "
 		+ "topic_deleted = ? "
 		+ "where topic_id = ? ";
 	private static final String STATEMENT_UPDATE_WIKI_USER =
 		"update jam_wiki_user set "
 		+ "login = ?, "
 		+ "display_name = ?, "
 		+ "last_login_date = ?, "
 		+ "last_login_ip_address = ?, "
 		+ "is_admin = ? "
 		+ "where wiki_user_id = ? ";
 	private static final String STATEMENT_UPDATE_WIKI_USER_INFO =
 		"update jam_wiki_user_info set "
 		+ "login = ?, "
 		+ "email = ?, "
 		+ "first_name = ?, "
 		+ "last_name = ?, "
 		+ "encoded_password = ? "
 		+ "where wiki_user_id = ? ";
 
 	/**
 	 *
 	 */
 	public DatabaseHandler() {
 	}
 
 	/**
 	 *
 	 */
 	protected void addRecentChange(RecentChange change) throws Exception {
 		int virtualWikiId = lookupVirtualWikiId(change.getVirtualWiki());
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_RECENT_CHANGE);
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
 			stmt.executeUpdate();
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void addTopic(Topic topic) throws Exception {
 		int virtualWikiId = lookupVirtualWikiId(topic.getVirtualWiki());
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			if (topic.getTopicId() <= 0) {
 				// add
 				WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_SEQUENCE, conn);
 				topic.setTopicId(rs.getInt("topic_id"));
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC);
 				stmt.setInt(1, topic.getTopicId());
 				stmt.setInt(2, virtualWikiId);
 				stmt.setString(3, topic.getName());
 				stmt.setInt(4, topic.getTopicType());
 				if (topic.getLockedBy() > 0) {
 					stmt.setInt(5, topic.getLockedBy());
 				} else {
 					stmt.setNull(5, Types.INTEGER);
 				}
 				stmt.setTimestamp(6, topic.getLockedDate());
 				stmt.setBoolean(7, topic.getReadOnly());
 				stmt.setString(8, topic.getTopicContent());
 				stmt.setString(9, topic.getLockSessionKey());
 				stmt.executeUpdate();
 			} else {
 				// update
 				stmt = conn.prepareStatement(STATEMENT_UPDATE_TOPIC);
 				stmt.setInt(1, virtualWikiId);
 				stmt.setString(2, topic.getName());
 				stmt.setInt(3, topic.getTopicType());
 				if (topic.getLockedBy() > 0) {
 					stmt.setInt(4, topic.getLockedBy());
 				} else {
 					stmt.setNull(4, Types.INTEGER);
 				}
 				stmt.setTimestamp(5, topic.getLockedDate());
 				stmt.setBoolean(6, topic.getReadOnly());
 				stmt.setString(7, topic.getTopicContent());
 				stmt.setString(8, topic.getLockSessionKey());
 				stmt.setBoolean(9, topic.getDeleted());
 				stmt.setInt(10, topic.getTopicId());
 				stmt.executeUpdate();
 			}
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE, conn);
 			topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
 			Timestamp editDate = new Timestamp(System.currentTimeMillis());
 			if (topicVersion.getEditDate() != null) {
 				editDate = topicVersion.getEditDate();
 			}
 			stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_VERSION);
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
 			stmt.setTimestamp(8, editDate);
 			stmt.executeUpdate();
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void addVirtualWiki(String virtualWikiName) throws Exception {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		int virtualWikiId;
 		try {
 			conn = DatabaseConnection.getConnection();
 			WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE, conn);
 			virtualWikiId = rs.getInt("virtual_wiki_id");
 			stmt = conn.prepareStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, virtualWikiName);
 			stmt.executeUpdate();
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt);
 			}
 		}
 		if (virtualWikiNameHash != null) {
 			virtualWikiNameHash.put(virtualWikiName, new Integer(virtualWikiId));
 		}
 		if (virtualWikiIdHash != null) {
 			virtualWikiIdHash.put(new Integer(virtualWikiId), virtualWikiName);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void addWikiUser(WikiUser user) throws Exception {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			if (user.getUserId() <= 0) {
 				// add
 				WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE, conn);
 				user.setUserId(rs.getInt("wiki_user_id"));
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_USER);
 				stmt.setInt(1, user.getUserId());
 				stmt.setString(2, user.getLogin());
 				stmt.setString(3, user.getDisplayName());
 				stmt.setTimestamp(4, user.getCreateDate());
 				stmt.setTimestamp(5, user.getLastLoginDate());
 				stmt.setString(6, user.getCreateIpAddress());
 				stmt.setString(7, user.getLastLoginIpAddress());
 				stmt.setBoolean(8, user.getAdmin());
 				stmt.executeUpdate();
 				// FIXME - may be in LDAP
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_USER_INFO);
 				stmt.setInt(1, user.getUserId());
 				stmt.setString(2, user.getLogin());
 				stmt.setString(3, user.getEmail());
 				stmt.setString(4, user.getFirstName());
 				stmt.setString(5, user.getLastName());
 				stmt.setString(6, user.getEncodedPassword());
 				stmt.executeUpdate();
 			} else {
 				// update
 				stmt = conn.prepareStatement(STATEMENT_UPDATE_WIKI_USER);
 				stmt.setString(1, user.getLogin());
 				stmt.setString(2, user.getDisplayName());
 				stmt.setTimestamp(3, user.getLastLoginDate());
 				stmt.setString(4, user.getLastLoginIpAddress());
 				stmt.setBoolean(5, user.getAdmin());
 				stmt.setInt(6, user.getUserId());
 				stmt.executeUpdate();
 				// FIXME - may be in LDAP
 				stmt = conn.prepareStatement(STATEMENT_UPDATE_WIKI_USER_INFO);
 				stmt.setString(1, user.getLogin());
 				stmt.setString(2, user.getEmail());
 				stmt.setString(3, user.getFirstName());
 				stmt.setString(4, user.getLastName());
 				stmt.setString(5, user.getEncodedPassword());
 				stmt.setInt(6, user.getUserId());
 				stmt.executeUpdate();
 			}
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static String createScript() {
 		String databaseType = DatabaseHandler.getDatabaseType();
 		if (databaseType.equals(DB_TYPE_ORACLE)) {
 			return INIT_SCRIPT_ORACLE;
 		}
 		return INIT_SCRIPT_ANSI;
 	}
 
 	/**
 	 * Run the create tables script
 	 * Ignore SQL exceptions as these may just be the result of existing tables getting in the
 	 * way of create table calls
 	 *
 	 * @throws java.lang.Exception
 	 */
 	private static void createTables() throws Exception {
 		String script = DatabaseHandler.createScript();
 		String contents = Utilities.readFile(script);
 		StringTokenizer tokens = new StringTokenizer(contents, ";");
 		String sql = null;
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			Statement st = conn.createStatement();
 			while (tokens.hasMoreTokens()) {
 				sql = tokens.nextToken();
 				st.executeUpdate(sql);
 			}
 			st.close();
 		} catch (Exception e) {
 			if (sql != null) {
 				throw new Exception("Failure while executing SQL: " + sql, e);
 			}
 			throw e;
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List getAllTopicNames(String virtualWiki) throws Exception {
 		List all = new ArrayList();
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			int virtualWikiId = lookupVirtualWikiId(virtualWiki);
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPICS);
 			stmt.setInt(1, virtualWikiId);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				all.add(rs.getString("topic_name"));
 			}
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public List getAllVersions(String virtualWiki, String topicName) throws Exception {
 		List all = new ArrayList();
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Topic topic = lookupTopic(virtualWiki, topicName);
 		if (topic == null) {
 			throw new Exception("No topic exists for " + virtualWiki + " / " + topicName);
 		}
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSIONS);
 			stmt.setInt(1, topic.getTopicId());
 			rs = stmt.executeQuery();
 			WikiResultSet wrs = new WikiResultSet(rs);
 			while (wrs.next()) {
 				all.add(initTopicVersion(wrs));
 			}
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public List getAllWikiUserLogins() throws Exception {
 		List all = new ArrayList();
 		WikiResultSet rs = DatabaseConnection.executeQuery(STATEMENT_SELECT_WIKI_USER_LOGINS);
 		while (rs.next()) {
 			all.add(rs.getString("login"));
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public static String getDatabaseType() {
 		return Environment.getValue(Environment.PROP_DB_TYPE);
 	}
 
 	/**
 	 *
 	 */
 	public List getLockList(String virtualWiki) throws Exception {
 		List all = new ArrayList();
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_LOCKED);
 			stmt.setInt(1, virtualWikiId);
 			rs = stmt.executeQuery();
 			WikiResultSet wrs = new WikiResultSet(rs);
 			while (wrs.next()) {
 				Topic topic = initTopic(wrs);
 				all.add(topic);
 			}
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
 		Collection all = new ArrayList();
 		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_READ_ONLY);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setBoolean(2, true);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				all.add(rs.getString("topic_name"));
 			}
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			}
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getRecentChanges(String virtualWiki, int numChanges) throws Exception {
 		ArrayList all = new ArrayList();
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_RECENT_CHANGES);
 			stmt.setString(1, virtualWiki);
 			stmt.setInt(2, numChanges);
 			rs = stmt.executeQuery();
 			WikiResultSet wrs = new WikiResultSet(rs);
 			while (wrs.next()) {
 				RecentChange change = initRecentChange(wrs);
 				all.add(change);
 			}
 			return all;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Collection getVirtualWikiList() throws Exception {
 		if (virtualWikiNameHash == null) {
 			loadVirtualWikiHashes();
 		}
 		return virtualWikiNameHash.keySet();
 	}
 
 	/**
 	 *
 	 */
 	public boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception {
 		Topic topic = lookupTopic(virtualWiki, topicName);
 		if (topic == null) {
 			// new topic
 			return true;
 		}
 		if (topic.getLockSessionKey() == null) {
 			return lockTopic(virtualWiki, topicName, key);
 		}
 		// FIXME - old code included a check to see if last version was made after the time
 		// the lock was taken.  that should be impossible with the new code.
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	public void initialize(Locale locale, WikiUser user) throws Exception {
 		String sql = null;
 		WikiResultSet rs = null;
 		boolean tablesExist = false;
 		sql = "select 1 from jam_virtual_wiki ";
 		try {
 			rs = DatabaseConnection.executeQuery(sql);
 			tablesExist = rs.next();
 		} catch (Exception e) {
 			// thrown if table doesn't exist, so safe to ignore
 		}
 		if (!tablesExist) {
 			// set up tables
 			DatabaseHandler.createTables();
 		}
 		sql = "select * from jam_virtual_wiki ";
 		rs = DatabaseConnection.executeQuery(sql);
 		if (rs.size() == 0) {
 			addVirtualWiki(WikiBase.DEFAULT_VWIKI);
 		}
 		super.initialize(locale, user);
 	}
 
 	/**
 	 *
 	 */
 	protected static RecentChange initRecentChange(WikiResultSet rs) {
 		try {
 			RecentChange change = new RecentChange();
 			change.setTopicVersionId(rs.getInt("topic_version_id"));
 			int previousTopicVersionId = rs.getInt("previous_topic_version_id");
 			if (previousTopicVersionId > 0) change.setPreviousTopicVersionId(new Integer(previousTopicVersionId));
 			change.setTopicId(rs.getInt("topic_id"));
 			change.setTopicName(rs.getString("topic_name"));
 			change.setEditDate(rs.getTimestamp("edit_date"));
 			change.setEditComment(rs.getString("edit_comment"));
 			int userId = rs.getInt("wiki_user_id");
 			if (userId > 0) change.setAuthorId(new Integer(userId));
 			change.setAuthorName(rs.getString("display_name"));
 			change.setEditType(rs.getInt("edit_type"));
 			change.setVirtualWiki(rs.getString("virtual_wiki_name"));
 			return change;
 		} catch (Exception e) {
 			logger.error("Failure while initializing recent change", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static Topic initTopic(WikiResultSet rs) {
 		try {
 			int virtualWikiId = rs.getInt("virtual_wiki_id");
 			String virtualWiki = lookupVirtualWikiName(virtualWikiId);
 			Topic topic = new Topic();
 			topic.setAdminOnly(rs.getBoolean("topic_admin_only"));
 			topic.setName(rs.getString("topic_name"));
 			topic.setVirtualWiki(virtualWiki);
 			topic.setTopicContent(rs.getString("topic_content"));
 			topic.setTopicId(rs.getInt("topic_id"));
 			topic.setLockedBy(rs.getInt("topic_locked_by"));
 			topic.setLockedDate(rs.getTimestamp("topic_lock_date"));
 			topic.setLockSessionKey(rs.getString("topic_lock_session_key"));
 			topic.setReadOnly(rs.getBoolean("topic_read_only"));
 			topic.setDeleted(rs.getBoolean("topic_deleted"));
 			topic.setTopicType(rs.getInt("topic_type"));
 			return topic;
 		} catch (Exception e) {
 			logger.error("Failure while initializing topic", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static TopicVersion initTopicVersion(WikiResultSet rs) {
 		try {
 			TopicVersion topicVersion = new TopicVersion();
 			topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
 			topicVersion.setTopicId(rs.getInt("topic_id"));
 			topicVersion.setEditComment(rs.getString("edit_comment"));
 			topicVersion.setVersionContent(rs.getString("version_content"));
 			int userId = rs.getInt("wiki_user_id");
 			if (userId > 0) topicVersion.setAuthorId(new Integer(userId));
 			topicVersion.setEditDate(rs.getTimestamp("edit_date"));
 			topicVersion.setEditType(rs.getInt("edit_type"));
 			topicVersion.setAuthorIpAddress(rs.getString("wiki_user_ip_address"));
 			return topicVersion;
 		} catch (Exception e) {
 			logger.error("Failure while initializing topic version", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static WikiUser initWikiUser(WikiResultSet rs) {
 		try {
 			WikiUser user = new WikiUser();
 			user.setUserId(rs.getInt("wiki_user_id"));
 			user.setLogin(rs.getString("login"));
 			user.setDisplayName(rs.getString("display_name"));
 			user.setCreateDate(rs.getTimestamp("create_date"));
 			user.setLastLoginDate(rs.getTimestamp("last_login_date"));
 			user.setCreateIpAddress(rs.getString("create_ip_address"));
 			user.setLastLoginIpAddress(rs.getString("last_login_ip_address"));
 			user.setAdmin(rs.getBoolean("is_admin"));
 			// FIXME - may be in LDAP
 			user.setEmail(rs.getString("email"));
 			user.setFirstName(rs.getString("first_name"));
 			user.setLastName(rs.getString("last_name"));
 			user.setEncodedPassword(rs.getString("encoded_password"));
 			return user;
 		} catch (Exception e) {
 			logger.error("Failure while initializing user", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static boolean isMySQL() {
 		return Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MYSQL);
 	}
 
 	/**
 	 *
 	 */
 	public static boolean isOracle() {
 		return Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_ORACLE);
 	}
 
 	/**
 	 *
 	 */
 	public static void loadRecentChanges() throws Exception {
 		String sql;
 		sql = "DELETE from jam_recent_change";
 		DatabaseConnection.executeUpdate(sql);
 		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES);
 		// FIXME - slow
 		sql = "SELECT topic_id, topic_version_id from jam_recent_change "
 		    + "WHERE previous_topic_version_id is null ";
 		WikiResultSet rs = DatabaseConnection.executeQuery(sql);
 		while (rs.next()) {
 			// FIXME - postgres specific
 			sql = "UPDATE jam_recent_change SET "
 			    + "previous_topic_version_id = ( "
 			    +   "select max(jam_topic_version.topic_version_id) "
 			    +   "from jam_topic_version "
 			    +   "where jam_topic_version.topic_id = " + rs.getInt("topic_id") + " "
 			    +   "and jam_topic_version.topic_version_id < " + rs.getInt("topic_version_id") + " "
 			    + ") "
 			    + "where topic_id = " + rs.getInt("topic_id") + " "
 			    + "and topic_version_id = " + rs.getInt("topic_version_id") + " ";
 			DatabaseConnection.executeUpdate(sql);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static void loadVirtualWikiHashes() throws Exception {
 		virtualWikiNameHash = new Hashtable();
 		virtualWikiIdHash = new Hashtable();
 		String sql = "select * from jam_virtual_wiki ";
 		try {
 			WikiResultSet rs = DatabaseConnection.executeQuery(sql);
 			while (rs.next()) {
 				Integer value = new Integer(rs.getInt("virtual_wiki_id"));
 				String key = rs.getString("virtual_wiki_name");
 				virtualWikiNameHash.put(key, value);
 				virtualWikiIdHash.put(value, key);
 			}
 		} catch (Exception e) {
 			logger.error("Failure while loading virtual wiki hashtable ", e);
 			// if there is an error make sure the hashtable is reset since it wasn't
 			// properly initialized
 			virtualWikiNameHash = null;
 			virtualWikiIdHash = null;
 			throw e;
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		Topic topic = lookupTopic(virtualWiki, topicName);
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSION_LAST);
 			stmt.setInt(1, topic.getTopicId());
 			rs = stmt.executeQuery();
 			if (rs == null) return null;
 			rs.next();
 			int topicVersionId = rs.getInt("topic_version_id");
 			return lookupTopicVersion(virtualWiki, topicName, topicVersionId);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
 		int virtualWikiId = lookupVirtualWikiId(virtualWiki);
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topicName);
 			rs = stmt.executeQuery();
 			WikiResultSet wrs = new WikiResultSet(rs);
 			if (wrs.size() == 0) return null;
 			return initTopic(wrs);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSION);
 			stmt.setInt(1, topicVersionId);
 			rs = stmt.executeQuery();
 			WikiResultSet wrs = new WikiResultSet(rs);
 			if (wrs.size() == 0) return null;
 			return initTopicVersion(wrs);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static int lookupVirtualWikiId(String virtualWikiName) throws Exception {
 		if (virtualWikiNameHash == null) {
 			loadVirtualWikiHashes();
 		}
 		Integer virtualWikiId = (Integer)virtualWikiNameHash.get(virtualWikiName);
 		if (virtualWikiId == null) {
 			throw new Exception("Virtual wiki " + virtualWikiName + " not found");
 		}
 		return virtualWikiId.intValue();
 	}
 
 	/**
 	 *
 	 */
 	protected static String lookupVirtualWikiName(int virtualWikiId) throws Exception {
 		if (virtualWikiIdHash == null) {
 			loadVirtualWikiHashes();
 		}
 		String virtualWikiName = (String)virtualWikiIdHash.get(new Integer(virtualWikiId));
 		if (virtualWikiName == null) {
 			throw new Exception("Virtual wiki " + virtualWikiId + " not found");
 		}
 		return virtualWikiName;
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(int userId) throws Exception {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER);
 			stmt.setInt(1, userId);
 			rs = stmt.executeQuery();
 			WikiResultSet wrs = new WikiResultSet(rs);
 			if (wrs.size() == 0) return null;
 			return initWikiUser(wrs);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(String login) throws Exception {
 		// FIXME - handle LDAP
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_LOGIN);
 			stmt.setString(1, login);
 			rs = stmt.executeQuery();
 			if (!rs.next()) return null;
 			int userId = rs.getInt("wiki_user_id");
 			return lookupWikiUser(userId);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(String login, String password) throws Exception {
 		// FIXME - handle LDAP
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_PASSWORD);
 			stmt.setString(1, login);
 			stmt.setString(2, Encryption.encrypt(password));
 			rs = stmt.executeQuery();
 			if (!rs.next()) return null;
 			int userId = rs.getInt("wiki_user_id");
 			return lookupWikiUser(userId);
 		} finally {
 			if (conn != null) {
 				DatabaseConnection.closeConnection(conn, stmt, rs);
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static boolean testDatabase() {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 		} catch (Exception e) {
 			// database settings incorrect
 			logger.error("Invalid database settings", e);
 			return false;
 		} finally {
 			if (conn != null) DatabaseConnection.closeConnection(conn);
 		}
 		return true;
 	}
 }
