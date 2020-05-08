 /**
  * $Id$
  *
  * Copyright (c) 2006  University of Massachusetts Boston
  *
  * Authors: Jacob K Asiedu
  *
  * This file is part of the UMB Electronic Field Guide.
  * UMB Electronic Field Guide is free software; you can redistribute it
  * and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2, or
  * (at your option) any later version.
  *
  * UMB Electronic Field Guide is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the UMB Electronic Field Guide; see the file COPYING.
  * If not, write to:
  * Free Software Foundation, Inc.
  * 59 Temple Place, Suite 330
  * Boston, MA 02111-1307
  * USA
  */
 
 package project.efg.client.rdb.gui;
 
 import java.io.File;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import project.efg.client.impl.gui.CreateEFGUserDialog;
 import project.efg.client.utils.gui.CreateSampleDataThread;
 import project.efg.util.interfaces.EFGImportConstants;
 import project.efg.util.utils.DBObject;
 import project.efg.util.utils.EFGRDBImportUtils;
 
 /**
  * Creates the efg database if it does not already exists, create a user with
  * username efg with only select and update privileges on the tables in the efg
  * database This user is used to access the web application
  */
 public class RunSetUp {
 	static Logger log = null;
 	static {
 		try {
 			log = Logger.getLogger(RunSetUp.class);
 		} catch (Exception ee) {
 		}
 	}
 	private static JdbcTemplate jdbcTemplate;
 
 	/**
 	 * Must be called whenever user privileges are updated
 	 */
 	private static void flushPrivileges(DBObject db) {
 		try {
 			StringBuilder queryBuffer = new StringBuilder();
 			queryBuffer.append(EFGImportConstants.EFGProperties
 					.getProperty("flushprivileges"));
 			if (!queryBuffer.toString().trim().equals("")) {
 				if (jdbcTemplate == null) {
 					jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(db);
 				}
 				jdbcTemplate.execute(queryBuffer.toString());
 			}
 		} catch (Exception e) {
 
 		}
 	}
 
 	/**
 	 * 
 	 * @param db
 	 * @param superuserInfo
 	 * @return
 	 */
 	public static boolean createSuperUser(DBObject db, DBObject superuserInfo) {
 		if (superuserInfo == null) {
 			return true;
 		}
 		Object[] obj = getEFGUsers(db);
 		checkSuperUser(db, superuserInfo, obj);
 		return true;
 	}
 
 	/**
 	 * Create a super user from the information supplied by user
 	 * 
 	 * @param db
 	 * @param superuserInfo
 	 * @return
 	 */
 	private static boolean createASuperUser(DBObject db, DBObject superuserInfo) {
 		if (superuserInfo == null) {
 			return true;
 		}
 
 		StringBuilder queryBuffer = null;
 		try {
 			queryBuffer = new StringBuilder();
 			queryBuffer.append("GRANT ALL ON mysql.* to \"");
 			queryBuffer.append(superuserInfo.getUserName());
 			queryBuffer.append("\"@'localhost' IDENTIFIED BY \"");
 			queryBuffer.append(superuserInfo.getPassword());
 			queryBuffer.append("\"");
 			queryBuffer.append(" WITH GRANT OPTION ");
 			if (jdbcTemplate == null) {
 				jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(db);
 			}
 			try {
 				jdbcTemplate.execute(queryBuffer.toString());
 				flushPrivileges(db);
 			} catch (Exception eex) {
 
 			}
 
 			queryBuffer = new StringBuilder();
 			queryBuffer.append("GRANT ALL ON efg.* to \"");
 			queryBuffer.append(superuserInfo.getUserName());
 			queryBuffer.append("\"@'localhost' IDENTIFIED BY \"");
 			queryBuffer.append(superuserInfo.getPassword());
 			queryBuffer.append("\"");
 			queryBuffer.append(" WITH GRANT OPTION ");
 			if (jdbcTemplate == null) {
 				jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(db);
 			}
 			try {
 				jdbcTemplate.execute(queryBuffer.toString());
 				flushPrivileges(db);
 			} catch (Exception eex) {
 
 			}
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 			return false;
 		}
 		try {
 			queryBuffer = new StringBuilder();
 			queryBuffer.append("GRANT ALL ON efg.* to \"");
 			queryBuffer.append(superuserInfo.getUserName());
 			queryBuffer.append("\"@'%' IDENTIFIED BY \"");
 			queryBuffer.append(superuserInfo.getPassword());
 			queryBuffer.append("\"");
 			queryBuffer.append(" WITH GRANT OPTION ");
 
 			if (jdbcTemplate == null) {
 				jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(db);
 			}
 			try {
 				jdbcTemplate.execute(queryBuffer.toString());
 				flushPrivileges(db);
 
 			} catch (Exception eex) {
 
 			}
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Get an Array of current efg users. i.e users created by root user for
 	 * instance
 	 * 
 	 * @param db
 	 * @return
 	 */
 	public static Object[] getEFGUsers(DBObject db) {
 		try {
 			if (jdbcTemplate == null) {
 				jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(db);
 			}
 			String property = EFGImportConstants.EFGProperties
 					.getProperty("selectefguser");
 			StringBuilder queryBuffer = new StringBuilder(property);
 
 			List list = jdbcTemplate.queryForList(queryBuffer.toString(),
 					String.class);
 			if (list != null) {
 				list.remove(EFGImportConstants.EFGProperties
 						.getProperty("dbusername"));
 				return list.toArray();
 			}
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 
 			JOptionPane.showMessageDialog(null, ee.getMessage(),
 					"Error Message", JOptionPane.ERROR_MESSAGE);
 		}
 		return null;
 	}
 
 	/**
 	 * Delete a user
 	 * 
 	 * @param db
 	 * @param userName
 	 */
 	public static void deleteSuperUser(DBObject db, String userName) {
 		try {
 
 			if (jdbcTemplate == null) {
 				jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(db);
 			}
 			StringBuilder queryBuffer = new StringBuilder();
 			queryBuffer.append("DROP USER \"");
 			queryBuffer.append(userName);
 			queryBuffer.append("\"@'%'");
 
 			try {
 				jdbcTemplate.execute(queryBuffer.toString());
 				flushPrivileges(db);
 			} catch (Exception ee) {
 
 			}
 			queryBuffer = new StringBuilder();
 			queryBuffer.append("DROP USER \"");
 			queryBuffer.append(userName);
 			queryBuffer.append("\"@'localhost'");
 
 			try {
 				jdbcTemplate.execute(queryBuffer.toString());
 
 			} catch (Exception ee) {
 
 			}
 			try {
 				flushPrivileges(db);
 			} catch (Exception ee) {
 
 			}
 			String message = "The user : \"" + userName
 					+ "\" \n successfully deleted from system!!";
 			JOptionPane.showMessageDialog(null, message, "Success Message",
 					JOptionPane.INFORMATION_MESSAGE);
 
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 			String message = "The user : \""
 					+ userName
 					+ "\" \n could not be deleted due to an error. Consult log file for error message";
 			JOptionPane.showMessageDialog(null, message, "Error Message",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Get information needed to find a Super user
 	 * 
 	 * @return
 	 */
 	private static DBObject getSuperUser() {
 
 		CreateEFGUserDialog dialog = new CreateEFGUserDialog(null);
 		dialog.setVisible(true);
 
 		if (dialog.isSuccess()) {
 			DBObject db = dialog.getDbObject();
 			dialog.dispose();
 			return db;
 		}
 
 		return null;
 	}
 
 	/**
 	 * Find out if there is a super user..If not prompt user to create one.
 	 * 
 	 * @param dbObject
 	 * @param superuserInfo
 	 * @param obj
 	 */
 	private static void checkSuperUser(DBObject dbObject,
 			DBObject superuserInfo, Object[] obj) {
 
 		if (superuserInfo == null) {
 			superuserInfo = getSuperUser();
 		}
 
 		if (superuserInfo != null) {
 			if (obj != null && obj.length > 0) {
 				int i = 0;
 				boolean found = false;
 				while (i < obj.length) {
 					String str = (String) obj[i];
 					if (str.equalsIgnoreCase(superuserInfo.getUserName())) {
 						found = true;
 						break;
 					}
 					++i;
 				}
 				if (found) {
 					JOptionPane
 							.showMessageDialog(
 									null,
 									"User name already exists. Please create a new one",
 									"User already Exists",
 									JOptionPane.WARNING_MESSAGE);
 					checkSuperUser(dbObject, null, obj);
 				} else {
 					createASuperUser(dbObject, superuserInfo);
 				}
 			} else {
 				createASuperUser(dbObject, superuserInfo);
 			}
 		}
 
 	}
 
 	/**
 	 * Create all tables needed by application
 	 * 
 	 * @param dbObject
 	 */
 	private static DBObject createUserTables(DBObject dbObject) {
 		DBObject newDb = null;
 		if (dbObject == null) {
 			return newDb;
 		}
 
 		try {
 			newDb = dbObject.clone(EFGImportConstants.EFGProperties
 					.getProperty("dburl"));
 
 			JdbcTemplate newjdbcTemplate = EFGRDBImportUtils
 					.getJDBCTemplate(newDb);
 
 			// read from properties file
 			StringBuilder query = new StringBuilder("CREATE TABLE ");
 			query.append(" IF NOT EXISTS ");
 			query.append(EFGImportConstants.EFGProperties
 					.getProperty("users_table"));
 			query
 					.append(" ( user_name varchar(15) not null primary key, user_pass varchar(15) not null)");
 
 			newjdbcTemplate.execute(query.toString());
 
 			query = new StringBuilder("CREATE TABLE ");
 			query.append(" IF NOT EXISTS ");
 			query.append(EFGImportConstants.EFGProperties
 					.getProperty("role_table"));
 			query.append(" (");
 			query
 					.append("user_name  varchar(15) not null, role_name varchar(15) not null, ");
 			query.append("primary key (user_name, role_name) ");
 			query.append(")");
 			newjdbcTemplate.execute(query.toString());
 
 			query = new StringBuilder("INSERT INTO ");
 			query.append(EFGImportConstants.EFGProperties
 					.getProperty("role_table"));
 			query.append(" VALUES(\"");
 			query.append(EFGImportConstants.EFGProperties
 					.getProperty("dbusername"));
 			query.append("\",\"");
 			query.append(EFGImportConstants.EFGProperties
					.getProperty("dbpassword"));
 			query.append("\")");
 			newjdbcTemplate.execute(query.toString());
 
 			query = new StringBuilder("INSERT INTO ");
 			query.append(EFGImportConstants.EFGProperties
 					.getProperty("users_table"));
 			query.append(" VALUES(\"");
 			query.append(EFGImportConstants.EFGProperties
 					.getProperty("dbusername"));
 			query.append("\",\"");
 			query.append(EFGImportConstants.EFGProperties
					.getProperty("db_role"));
 			query.append("\")");
 			newjdbcTemplate.execute(query.toString());
 
 		} catch (Exception e) {
 
 		}
 		return newDb;
 
 	}
 
 	/**
 	 * Create all the helper tables needed by import
 	 * 
 	 * @param dbObject
 	 */
 	private static void createHelperTables(DBObject dbObject) {
 		// create helper tables
 		DBObject efg2DbObject = createUserTables(dbObject);
 		String query = null;
 
 		Object[] obj = getEFGUsers(dbObject);
 
 		checkSuperUser(dbObject, null, obj);
 		try {
 			query = EFGImportConstants.EFGProperties
 					.getProperty("createusercmd");
 			jdbcTemplate.execute(query);
 		} catch (Exception ee) {
 			log.error("Warning: user probably already exists");
 
 		}
 		try {
 			query = EFGImportConstants.EFGProperties
 					.getProperty("createlocalusercmd");
 			jdbcTemplate.execute(query);
 		} catch (Exception ee) {
 			log.error("Warning: user probably already exists");
 
 		}
 
 		try {
 			query = EFGImportConstants.EFGProperties.getProperty("grantcmd");
 			jdbcTemplate.execute(query);
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 
 		}
 		if (efg2DbObject == null) {
 			log.error("Cannot get connection to efg database");
 		}
 		JdbcTemplate efgjdbcTemplate = EFGRDBImportUtils
 				.getJDBCTemplate(efg2DbObject);
 		createEFG2TemplatesTable(efgjdbcTemplate);
 		createRDBTable(efgjdbcTemplate);
 		createGlossaryTable(efgjdbcTemplate);
 		loadSampleData(efg2DbObject);
 	}
 
 	private static boolean createEFG2TemplatesTable(JdbcTemplate efgjdbcTemplate) {
 		String templateName = EFGImportConstants.TEMPLATE_TABLE.toLowerCase();
 		if ((templateName == null) || (templateName.trim().equals(""))) {
 
 			return false;
 		}
 
 		StringBuilder query = new StringBuilder();
 
 		query.append("CREATE TABLE IF NOT EXISTS ");
 		query.append(templateName.toLowerCase());
 
 		query.append("( ");
 		query.append(EFGImportConstants.TEMPLATE_KEY);
 		query.append(" VARCHAR(255) not null,");
 		query.append(EFGImportConstants.GUID);
 		query.append(" VARCHAR (255), ");
 		query.append(EFGImportConstants.DISPLAY_NAME);
 		query.append(" VARCHAR(255), ");
 		query.append(EFGImportConstants.DATASOURCE_NAME);
 		query.append(" VARCHAR(255), ");
 		query.append(EFGImportConstants.TEMPLATE_NAME);
 		query.append(" VARCHAR(255), ");
 		query.append(EFGImportConstants.QUERY_STR);
 		query.append(" TEXT ");
 		query.append(")");
 		try {
 			efgjdbcTemplate.execute(query.toString());
 			return true;
 		} catch (Exception e) {
 
 		}
 		return true;
 	}
 
 	private static boolean createRDBOrGlossaryTable(
 			JdbcTemplate efgjdbcTemplate, String tableName) {
 		try {
 			StringBuilder query = new StringBuilder();
 
 			// PUT IN PROPERTIES FILE
 			query.append("CREATE TABLE IF NOT EXISTS ");
 			query.append(tableName);
 			query.append("( DS_DATA VARCHAR(255) not null,");
 			query.append("ORIGINAL_FILE_NAME TEXT, ");
 			query.append("DS_METADATA VARCHAR(255) not null, ");
 			query.append("DISPLAY_NAME VARCHAR(255) unique not null, ");
 			query.append("XSL_FILENAME_TAXON VARCHAR(255), ");
 			query.append("XSL_FILENAME_SEARCHPAGE_PLATES VARCHAR(255), ");
 			query.append("XSL_FILENAME_SEARCHPAGE_LISTS VARCHAR(255), ");
 			query.append("CSS_FILENAME VARCHAR(255), ");
 			query.append("JAVASCRIPT_FILENAME VARCHAR(255), ");
 			query.append("TEMPLATE_OBJECT BLOB ");
 			query.append(")");
 			log.debug("About to execute query : \"" + query.toString());
 			efgjdbcTemplate.execute(query.toString());
 			log.debug("Query executed successfully!!");
 			return true;
 		} catch (Exception ee) {
 			log.debug(ee.getMessage());
 		}
 		log.debug("About to return false!!!");
 		return false;
 
 	}
 
 	private static boolean createRDBTable(JdbcTemplate efgjdbcTemplate) {
 		return createRDBOrGlossaryTable(efgjdbcTemplate,
 				EFGImportConstants.EFG_RDB_TABLES);
 	}
 
 	private static boolean createGlossaryTable(JdbcTemplate efgjdbcTemplate) {
 		return createRDBOrGlossaryTable(efgjdbcTemplate,
 				EFGImportConstants.EFG_GLOSSARY_TABLES);
 	}
 
 	/**
 	 * 
 	 * @param dbObject -
 	 *            contains enough information to connect to database
 	 * @return true if connection to database was successfully made, false
 	 *         otherwise Run setup only if there is no user
 	 */
 	public synchronized static boolean runSetUp(DBObject dbObject) {
 		boolean isDBExists = false;
 		// check if you can get a connection from the efg database
 		// create efg database, users etc in main database
 
 		isDBExists = checkEFGDB(dbObject);
 		// remove me
 		// if(checkDB(dbObject)){//if this user already exists
 		// return true;
 		// }
 		if (jdbcTemplate == null) {
 			jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(dbObject);
 		}
 		// invoke the other login
 		// if user chooses cancel warn user and ask if they will like to return
 		// if no continue with null
 		// if yes show it again
 
 		// see if database exists
 		// create a super user command
 		String query = null;
 		// read from properties file
 		if (!isDBExists) {
 			query = EFGImportConstants.EFGProperties
 					.getProperty("createdatabasecmd");
 			try {
 				// create the database
 				jdbcTemplate.execute(query);
 				createHelperTables(dbObject);
 				// Load the sample data here.
 
 			} catch (Exception ee) {
 
 				createHelperTables(dbObject);
 				log.error(ee.getMessage());
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * 
 	 */
 	private static void loadSampleData(DBObject dbObject) {
 		try {
 
 			File file = new File("loadsample.sample");
 			CreateSampleDataThread ct = new CreateSampleDataThread(null,
 					dbObject);
 			ct.start();
 			if (!file.delete()) {
 				file.deleteOnExit();
 			}
 		} catch (Exception ee) {
 
 		}
 	}
 
 	/**
 	 * 
 	 * @param dbObject2
 	 * @return
 	 */
 	private static DBObject getClone(DBObject dbObject2) {
 		String url = EFGImportConstants.EFGProperties.getProperty("dburl");
 		return dbObject2.clone(url);
 	}
 
 	/**
 	 * If the efg database exists return true.
 	 * 
 	 * @param dbObject
 	 * @return
 	 */
 	private static boolean checkEFGDB(DBObject dbObject) {
 		try {
 			DBObject cloneDB = getClone(dbObject);
 
 			JdbcTemplate jdbcTemplateNew = EFGRDBImportUtils
 					.getJDBCTemplate(cloneDB);
 			if (jdbcTemplateNew != null) {
 
 				return true;
 			}
 		} catch (Exception ee) {
 			System.err.println(ee.getMessage());
 			log.error(ee.getMessage());
 		}
 
 		return false;
 	}
 
 }
 // $Log: RunSetUp.java,v $
 // Revision 1.1.1.1 2007/08/01 19:11:17 kasiedu
 // efg2.1.1.0 version of efg2
 //
 // Revision 1.6 2007/03/25 14:06:13 kasiedu
 // *** empty log message ***
 //
 // Revision 1.5 2007/01/25 23:46:48 kasiedu
 // no message
 //
 // Revision 1.4 2007/01/21 18:22:03 kasiedu
 // no message
 //
 // Revision 1.3 2007/01/21 02:10:45 kasiedu
 // no message
 //
 // Revision 1.2 2006/12/08 03:50:59 kasiedu
 // no message
 //
 // Revision 1.1.2.5 2006/09/18 18:15:26 kasiedu
 // no message
 //
 // Revision 1.1.2.4 2006/09/13 17:11:11 kasiedu
 // no message
 //
 // Revision 1.1.2.3 2006/09/10 12:02:28 kasiedu
 // no message
 //
 // Revision 1.1.2.2 2006/08/09 18:55:24 kasiedu
 // latest code conforms to what exists on Panda
 //
 // Revision 1.1.2.1 2006/06/08 13:27:42 kasiedu
 // New files
 //
 // Revision 1.1 2006/02/25 13:13:31 kasiedu
 // New classes for import GUI
 //
