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
 import java.sql.SQLException;
 import java.util.List;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.DataHandler;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.transaction.TransactionDefinition;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 
 /**
  * This class simply contains utility methods for upgrading database schemas
  * (if needed) between JAMWiki versions.  In general upgrade methods will only
  * be maintained for a few versions and then deleted - for example, JAMWiki version 10.0.0
  * does not need to keep the upgrade methods from JAMWiki 0.0.1 around.
  */
 public class DatabaseUpgrades {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseUpgrades.class.getName());
 
 	/**
 	 *
 	 */
 	private DatabaseUpgrades() {
 	}
 
 	private static TransactionDefinition getTransactionDefinition() {
 		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
 		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
 		return def;
 	}
 
 	/**
 	 * Special login method - it cannot be assumed that the database schema
 	 * is unchanged, so do not use standard methods.
 	 */
 	public static boolean login(String username, String password) throws WikiException {
 		try {
 			return (WikiBase.getDataHandler().authenticate(username, password));
 		} catch (DataAccessException e) {
 			logger.severe("Unable to authenticate user during upgrade", e);
 			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static List<WikiMessage> upgrade080(List<WikiMessage> messages) throws WikiException {
 		String dbType = Environment.getValue(Environment.PROP_DB_TYPE);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction(getTransactionDefinition());
 			Connection conn = DatabaseConnection.getConnection();
 			if (StringUtils.equals(dbType, DataHandler.DATA_HANDLER_POSTGRES)) {
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_GROUP_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_GROUP_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "group_id", "jam_group"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_GROUP_MEMBERS_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_MEMBERS_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_GROUP_MEMBERS_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "id", "jam_group_members"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_TOPIC_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_TOPIC_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_id", "jam_topic"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_TOPIC_VERSION_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_VERSION_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_TOPIC_VERSION_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_version_id", "jam_topic_version"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_VIRTUAL_WIKI_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_VIRTUAL_WIKI_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_VIRTUAL_WIKI_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "virtual_wiki_id", "jam_virtual_wiki"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_WIKI_FILE_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_WIKI_FILE_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_id", "jam_file"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_WIKI_FILE_VERSION_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_VERSION_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_WIKI_FILE_VERSION_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_version_id", "jam_file_version"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_SEQUENCE_WIKI_USER_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_USER_ID", conn);
 				WikiBase.getDataHandler().executeUpgradeQuery("UPGRADE_080_SET_SEQUENCE_WIKI_USER_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "wiki_user_id", "jam_wiki_user"));
 			} else if (StringUtils.equals(dbType, DataHandler.DATA_HANDLER_MYSQL)) {
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "group_id", "jam_group"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_GROUP_MEMBERS_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "id", "jam_group_members"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_id", "jam_topic"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_TOPIC_VERSION_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "topic_version_id", "jam_topic_version"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_VIRTUAL_WIKI_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "virtual_wiki_id", "jam_virtual_wiki"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_id", "jam_file"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_FILE_VERSION_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "file_version_id", "jam_file_version"));
 				WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ALTER_WIKI_USER_ID", conn);
 				messages.add(new WikiMessage("upgrade.message.db.column.modified", "wiki_user_id", "jam_wiki_user"));
 			}
 			// add jam_log table
 			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_LOG_TABLE", conn);
 			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_log"));
 			// add wiki_user_display column to jam_topic_version
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_TOPIC_VERSION_USER_DISPLAY", conn);
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_UPDATE_TOPIC_VERSION_USER_DISPLAY", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.added", "wiki_user_display", "jam_topic_version"));
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_DROP_TOPIC_VERSION_IP_ADDRESS", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.dropped", "wiki_user_ip_address", "jam_topic_version"));
 			// add wiki_user_display column to jam_file_version
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_FILE_VERSION_USER_DISPLAY", conn);
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_UPDATE_FILE_VERSION_USER_DISPLAY", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.added", "wiki_user_display", "jam_file_version"));
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_DROP_FILE_VERSION_IP_ADDRESS", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.dropped", "wiki_user_ip_address", "jam_file_version"));
 			// add version_param column to jam_topic_version
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_ADD_TOPIC_VERSION_VERSION_PARAMS", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.added", "version_params", "jam_topic_version"));
 			// drop and restore the jam_recent_change table
 			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_DROP_RECENT_CHANGE_TABLE", conn);
 			messages.add(new WikiMessage("upgrade.message.db.table.dropped", "jam_recent_change"));
 			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_RECENT_CHANGE_TABLE", conn);
 			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_recent_change"));
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			logger.severe("Database failure during upgrade", e);
 			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
 		}
 		DatabaseConnection.commit(status);
 		try {
 			// perform a separate transaction to update existing data.  this code is in its own
 			// transaction since if it fails the upgrade can still be considered successful.
 			status = DatabaseConnection.startTransaction(getTransactionDefinition());
 			Connection conn = DatabaseConnection.getConnection();
 			// update the edit type field for topic versions
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_UPDATE_TOPIC_VERSION_UPLOAD_EDIT_TYPE", conn);
 			messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_topic_version"));
 		} catch (SQLException e) {
 			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
 			// do not throw this error and halt the upgrade process - populating the field
 			// is not required for existing systems.
 			logger.warning("Failure while updating edit_type value in jam_topic_version.  See UPGRADE.txt for instructions on how to manually complete this optional step.", e);
 			try {
 				DatabaseConnection.rollbackOnException(status, e);
 			} catch (Exception ex) {
 				// ignore
 			}
 			status = null; // so we do not try to commit
 		}
 		if (status != null) {
 			DatabaseConnection.commit(status);
 		}
 		try {
 			// perform a separate transaction to update existing data.  this code is in its own
 			// transaction since if it fails the upgrade can still be considered successful.
 			status = DatabaseConnection.startTransaction(getTransactionDefinition());
 			Connection conn = DatabaseConnection.getConnection();
 			// assign ROLE_IMPORT
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_INSERT_ROLE_ROLE_IMPORT", conn);
 			messages.add(new WikiMessage("upgrade.message.db.data.added", "jam_role"));
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_080_INSERT_AUTHORITIES_ROLE_IMPORT", conn);
 			messages.add(new WikiMessage("upgrade.message.db.data.added", "jam_authorities"));
 		} catch (SQLException e) {
 			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
 			// do not throw this error and halt the upgrade process - populating the field
 			// is not required for existing systems.
 			logger.warning("Failure while updating ROLE_IMPORT in the jam_authorities table.  See UPGRADE.txt for instructions on how to manually complete this optional step.", e);
 			try {
 				DatabaseConnection.rollbackOnException(status, e);
 			} catch (Exception ex) {
 				// ignore
 			}
 			status = null; // so we do not try to commit
 		}
 		if (status != null) {
 			DatabaseConnection.commit(status);
 		}
 		return messages;
 	}
 
 	/**
 	 *
 	 */
 	public static List<WikiMessage> upgrade090(List<WikiMessage> messages) throws WikiException {
 		String dbType = Environment.getValue(Environment.PROP_DB_TYPE);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction(getTransactionDefinition());
 			Connection conn = DatabaseConnection.getConnection();
 			// add the namespace tables
 			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_NAMESPACE_TABLE", conn);
 			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_namespace"));
 			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_NAMESPACE_TRANSLATION_TABLE", conn);
 			messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_namespace_translation"));
 			// populate the namespace table
 			WikiDatabase.setupDefaultNamespaces();
 			messages.add(new WikiMessage("upgrade.message.db.data.added", "jam_namespace"));
 			// update jam_topic to add a namespace column, defaulted to the main namespace
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_NAMESPACE_ID", conn);
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_NAMESPACE_ID_CONSTRAINT", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.added", "namespace_id", "jam_topic"));
 			// update jam_topic to add page_name and page_name_lower
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.added", "page_name", "jam_topic"));
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME_LOWER", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.added", "page_name_lower", "jam_topic"));
 			// add an index for topic_id on the jam_topic_version table
 			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_VERSION_TOPIC_INDEX", conn);
 			messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_topic_version"));
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			logger.severe("Database failure during upgrade", e);
 			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			logger.severe("Database failure during upgrade", e);
 			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
 		}
 		DatabaseConnection.commit(status);
 		try {
 			status = DatabaseConnection.startTransaction(getTransactionDefinition());
 			Connection conn = DatabaseConnection.getConnection();
 			// populate jam_topic.namespace_id, jam_topic.page_name and jam_topic.page_name_lower
 			int numUpdated = WikiDatabase.fixIncorrectTopicNamespaces();
 			messages.add(new WikiMessage("admin.maintenance.message.namespaces", Integer.toString(numUpdated)));
 			// add not null constraints for jam_topic.page_name and jam_topic.page_name_lower
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME_NOT_NULL_CONSTRAINT", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.modified", "page_name", "jam_topic"));
 			WikiBase.getDataHandler().executeUpgradeUpdate("UPGRADE_090_ADD_TOPIC_PAGE_NAME_LOWER_NOT_NULL_CONSTRAINT", conn);
 			messages.add(new WikiMessage("upgrade.message.db.column.modified", "page_name_lower", "jam_topic"));
			// add the indexes after adding the not null constraint, otherwise MS SQL Server gets angry
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_PAGE_NAME_INDEX", conn);
			WikiBase.getDataHandler().executeUpgradeUpdate("STATEMENT_CREATE_TOPIC_PAGE_NAME_LOWER_INDEX", conn);
 		} catch (SQLException e) {
 			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
 			// do not throw this error and halt the upgrade process - populating the field
 			// is not required for existing systems.
 			logger.warning("Failure while populating correct namespace_id values in the jam_topic table.  Try running the 'Fix Incorrect Topic Namespaces' from Special:Maintenance to complete this step.", e);
 			try {
 				DatabaseConnection.rollbackOnException(status, e);
 			} catch (Exception ex) {
 				// ignore
 			}
 			status = null; // so we do not try to commit
 		} catch (DataAccessException e) {
 			messages.add(new WikiMessage("upgrade.error.nonfatal", e.getMessage()));
 			// do not throw this error and halt the upgrade process - populating the field
 			// is not required for existing systems.
 			logger.warning("Failure while populating correct namespace_id values in the jam_topic table.  Try running the 'Fix Incorrect Topic Namespaces' from Special:Maintenance to complete this step.", e);
 			try {
 				DatabaseConnection.rollbackOnException(status, e);
 			} catch (Exception ex) {
 				// ignore
 			}
 			status = null; // so we do not try to commit
 		}
 		if (status != null) {
 			DatabaseConnection.commit(status);
 		}
 		return messages;
 	}
 }
