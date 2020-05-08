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
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.Utilities;
 
 /**
  *
  */
 public class DatabaseInit {
 
 	private static final Logger logger = Logger.getLogger(DatabaseInit.class);
 
 	private static final String DATABASE_DB2 = "db2";
 	private static final String DATABASE_MYSQL = "mysql";
 	private static final String DATABASE_ORACLE = "oracle";
 	private static final String DATABASE_POSTGRES = "postgres";
 	private static final String INIT_SCRIPT_ANSI = "create_ansi.sql";
 	private static final String INIT_SCRIPT_ORACLE = "create_oracle.sql";
 
 	/**
 	 * FIXME: temporary
 	 */
 	public static void cleanup() throws Exception {
 		Connection conn = null;
 		String sql = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			Statement st = conn.createStatement();
 			// FIXME - temporary during testing
 			try {
 				sql = "DROP TABLE jam_recent_change";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP TABLE jam_notification";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP TABLE jam_topic_version";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP TABLE jam_topic";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP TABLE jam_wiki_user_info";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP TABLE jam_wiki_user";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP TABLE jam_virtual_wiki";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP SEQUENCE jam_virtual_wiki_seq";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP SEQUENCE jam_wiki_user_seq";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP SEQUENCE jam_topic_seq";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP SEQUENCE jam_topic_version_seq";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP SEQUENCE jam_notification_seq";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
 			}
 			try {
 				sql = "DROP SEQUENCE jam_recent_change_seq";
 				st.executeUpdate(sql);
 			} catch (Exception e) {
 				logger.warn("Failure while executing " + sql + " : " + e.getMessage());
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
 	private static String createScript() {
 		String databaseType = DatabaseHandler.getDatabaseType();
 		if (databaseType.equals(DATABASE_ORACLE)) {
 			return INIT_SCRIPT_ORACLE;
 		}
 		return INIT_SCRIPT_ANSI;
 	}
 
 	/**
	 *
	 */
	// FIXME - temporary
	public static void convert() throws Exception {
		ConvertVQWiki.convertFromDatabase();
		DatabaseHandler.loadVirtualWikiHashes();
	}

	/**
 	 * Run the create tables script
 	 * Ignore SQL exceptions as these may just be the result of existing tables getting in the
 	 * way of create table calls
 	 *
 	 * @throws java.lang.Exception
 	 */
 	public static void initialize() throws Exception {
 		String script = DatabaseInit.createScript();
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
 	 * Optionally purge obsolete data from the database.
 	 */
 	private void purgeVQWiki278() throws Exception {
 		Connection conn = null;
 		String sql = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			Statement st = conn.createStatement();
 			sql = "DROP TABLE WikiMember";
 			st.executeUpdate(sql);
 			sql = "DROP TABLE Notification";
 			st.executeUpdate(sql);
 			sql = "DROP TABLE VirtualWiki";
 			st.executeUpdate(sql);
 			sql = "DROP TABLE TopicReadOnly";
 			st.executeUpdate(sql);
 			sql = "DROP TABLE TopicLock";
 			st.executeUpdate(sql);
 			sql = "DROP TABLE TopicChange";
 			st.executeUpdate(sql);
 			sql = "DROP TABLE TopicVersion";
 			st.executeUpdate(sql);
 			sql = "DROP TABLE Topic";
 			st.executeUpdate(sql);
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
 	 * If the JAMWiki version has changed update the database as needed.
 	 */
 	public void upgrade() throws Exception {
 		if (WikiVersion.getCurrentVersion().before(0, 0, 1)) {
 			this.initialize();
 		}
 	}
 }
