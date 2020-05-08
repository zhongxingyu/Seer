 package com.cffreedom.utils.db;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.naming.Binding;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cffreedom.beans.DbConn;
 import com.cffreedom.exceptions.DbException;
 import com.cffreedom.exceptions.InfrastructureException;
 import com.cffreedom.utils.Utils;
 import com.cffreedom.utils.file.FileUtils;
 
 /**
  * Original Class: com.cffreedom.utils.db.DbUtils
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-04-11 	markjacobsen.net 	Added the ability to specify outputTo to runSql() - null/STDOUT or fileName
  * 2013-04-12	markjacobsen.net 	Added getObjectSerializedInBlob()
  * 2013-04-12 	markjacobsen.net 	Added additional testConnection() that accepts a DbConn bean
  * 2013-04-12 	markjacobsen.net 	Running test SQL in testConnection() if we can - not just making a connection
  * 2013-04-16 	markjacobsen.net 	testConnection() methods now return a boolean
  * 2013-04-22 	markjacobsen.net 	Added toInClausItems()
  * 2013-04-27 	markjacobsen.net 	Added getResultSet()
  * 2013-05-18 	markjacobsen.net 	Added listTables()
  * 2013-05-23	markjacobsen.net 	Updated outputResultSet() to handle RAW format better
  * 2013-07-05	markjacobsen.net 	Added getJndiDataSourceNames()
  * 2013-07-06 	markjacobsen.net 	Using slf4j
 * 2013-07-20	markjacobsen.net 	Fixed getConnectionJNDI()
  */
 public class DbUtils
 {
 	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.db.DbUtils");
 	
 	public static enum FORMAT {CSV,TAB,XML,RAW,NO_OUTPUT};
 	
 	public final static String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
 	public final static String DRIVER_DB2_JCC = "com.ibm.db2.jcc.DB2Driver";
 	public final static String DRIVER_DB2_APP = "COM.ibm.db2.jdbc.app.DB2Driver";
 	public final static String DRIVER_DB2_NET = "COM.ibm.db2.jdbc.net.DB2Driver";
 	public final static String DRIVER_SQL_SERVER_2005 = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
 	public final static String DRIVER_ODBC = "sun.jdbc.odbc.JdbcOdbcDriver";
 	public final static String DRIVER_SQLITE = "org.sqlite.JDBC";
 
 	public final static String TYPE_MYSQL = "MYSQL";
 	public final static String TYPE_DB2_JCC = "DB2_JCC";
 	public final static String TYPE_DB2_APP = "DB2_APP";
 	public final static String TYPE_DB2 = TYPE_DB2_JCC;
 	public final static String TYPE_SQL_SERVER = "SQL_SERVER";
 	public final static String TYPE_ODBC = "ODBC";
 	public final static String TYPE_SQLITE = "SQLITE";
 
 	public final static String SQL_TEST_SQLSERVER = "SELECT getDate()";
 	public final static String SQL_TEST_DB2 = "SELECT CURRENT_TIMESTAMP FROM SYSIBM.SYSDUMMY1";
 	
 	public final static String SQL_LIST_TABLES_DB2 = "SELECT TRIM(CREATOR)||\'.\'||TRIM(NAME) AS TABLE_NM FROM SYSIBM.SYSTABLES WHERE TYPE = \'T\' AND CREATOR NOT IN (\'SYSIBM\', \'SYSPROC\') ORDER BY CREATOR, NAME";
 	public final static String SQL_LIST_TABLES_SQLSERVER = "SELECT TABLE_SCHEMA+\'.\'+TABLE_NAME AS TABLE_NM FROM information_schema.tables WHERE TABLE_TYPE = \'BASE TABLE\' ORDER BY TABLE_SCHEMA, TABLE_NAME";
 	public final static String SQL_LIST_TABLES_MYSQL = "SELECT CONCAT(TABLE_SCHEMA, \'.\', TABLE_NAME) AS TABLE_NM FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = \'BASE TABLE\' ORDER BY TABLE_SCHEMA, TABLE_NAME";
 	
 	public static List<String> getJndiDataSourceNames()
 	{
 		List<String> dataSources = new ArrayList<String>();
 		try
 		{
 			Context ctx = new InitialContext();
 			NamingEnumeration<?> bindings = ctx.listBindings("java:comp/env/jdbc");
 
 			while (bindings.hasMore())
 			{
 				Binding binding = (Binding) bindings.next();
 				dataSources.add(binding.getName());
 			}
 
 		}
 		catch (Exception e)
 		{
 			dataSources = null;
 		}
 		
 		return dataSources;
 	}
 	
 	public static void listTables(DbConn dbconn)
 	{
 		Connection conn = null;
 		try
 		{
 			conn = DbUtils.getConnection(dbconn.getDriver(), dbconn.getUrl(), dbconn.getUser(), dbconn.getPassword());
 			String testSql = DbUtils.getListTablesSql(dbconn.getType());
 			if (testSql != null)
 			{
 				runSql(conn, testSql, FORMAT.RAW);
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		finally
 		{
 			try {conn.close();} catch (Exception e) {}
 		}
 	}
 	
 	public static boolean testConnection(DbConn dbconn, String user, String pass)
 	{
 		return testConnection(dbconn.getType(), dbconn.getHost(), dbconn.getDb(), dbconn.getPort(), user, pass);
 	}
 	
 	public static boolean testConnection(String type, String host, String db, int port, String user, String pass)
 	{
 		String driver = DbUtils.getDriver(type);
 		String url = DbUtils.getUrl(type, host, db, port);
 		try
 		{
 			Connection conn = DbUtils.getConnection(driver, url, user, pass);
 			String testSql = DbUtils.getTestSql(type);
 			if (testSql != null)
 			{
 				if (runSql(conn, DbUtils.getTestSql(type)) == false)
 				{
 					throw new DbException("Error running test SQL");
 				}
 			}
 			conn.close();
 			logger.debug("SUCCESS: " + url);
 			return true;
 		}
 		catch (DbException e)
 		{
 			logger.error("{}", e.getMessage());
 			return false;
 		}
 		catch (InfrastructureException e)
 		{
 			logger.error("{}", e.getMessage());
 			return false;
 		}
 		catch (SQLException e)
 		{
 			int errorCode = e.getErrorCode();
 			String sqlState = e.getSQLState();
 			String readable = "";
 			if  (
 				(DbUtils.isDb2(type) == true) && 
 				(errorCode == -1060) && 
 				(sqlState.equalsIgnoreCase("08004") == true)
 				)
 			{
 				readable = user + " does not have CONNECT permission: ";
 			}
 			logger.error("ERROR: SQLException: {} {}: {} ({}/{})", readable, url, e.getMessage(), errorCode, sqlState);
 			return false;
 		}
 	}
 	
 	/**
 	 * Run a script.  Prompts are in the form [prompt:text|replaceVar|optDefaultVal] and should be
 	 * one per line listed at the top.
 	 * @param conn
 	 * @param file
 	 */
 	public static int runSqlScript(Connection conn, String file) { return runSqlScript(conn, file, FORMAT.XML); }
 	public static int runSqlScript(Connection conn, String file, FORMAT format) { return runSqlScript(conn, file, format, ";"); }
 	public static int runSqlScript(Connection conn, String file, FORMAT format, String delimiter) { return runSqlScript(conn, file, format, delimiter, true); }
 	public static int runSqlScript(Connection conn, String file, FORMAT format, String delimiter, boolean outputResults)
 	{
 		final String PROMPT_KEY = "[prompt:";
 		HashMap<String, String> replaceVals = new HashMap<String, String>();
 		int errors = 0;
 		int worked = 0;
 		
 		logger.debug("Running: " + file);
 		
 		ArrayList<String> content = FileUtils.getFileLines(file, " ");
 		String temp = "";
 		for (String line : content)
 		{				
 			if (line.trim().length() > 0)
 			{
 				if (line.indexOf(PROMPT_KEY) == 0)
 				{
 					// There is a prompt
 					String tempPrompt = line.replaceFirst(Pattern.quote(PROMPT_KEY), "");
 					tempPrompt = tempPrompt.substring(0, tempPrompt.length() - 1);
 					String[] prompt = tempPrompt.split("\\|");
 					String text = prompt[0];
 					String replaceHolder = prompt[1];
 					String defaultVal = null;
 					if (prompt.length == 2)
 					{
 						// There is NO default value for the prompt
 						replaceHolder = replaceHolder.substring(0, replaceHolder.length() - 1);
 					}
 					else if (prompt.length >= 3)
 					{
 						// There is a default value for the prompt
 						String tmpDef = prompt[2].trim();
 						defaultVal = tmpDef.substring(0, tmpDef.length() - 1);
 					}
 					String promptVal = Utils.prompt(text, defaultVal);
 					replaceVals.put(replaceHolder, promptVal);
 					line = "";
 				}
 				
 				if (line.indexOf("--") >= 0)
 				{
 					int endIndex = line.indexOf("--");
 					if (endIndex > 0)
 					{
 						line = line.substring(0, endIndex - 1);
 					}
 					else
 					{
 						line = "";
 					}
 				}
 			
 				temp += line;
 				if (Utils.lastChar(line.trim()).equalsIgnoreCase(delimiter) == true)
 				{
 					// run it
 					String sql = temp.substring(0, temp.trim().length() - 1);
 					
 					for(Map.Entry<String,String> entry : replaceVals.entrySet())
 					{
 						logger.debug("Replacing \"{}\" with \"{}\"", entry.getKey(), entry.getValue());
 						sql = sql.replace(entry.getKey(), entry.getValue());
 					}
 					
 					if (runSql(conn, sql, format) == false)
 					{
 						errors++;
 					}
 					else
 					{
 						worked++;
 					}
 					Utils.output("\n\n");
 					
 					// reset
 					temp = "";
 				}
 			}
 		}
 		
 		if (outputResults == true)
 		{
 			Utils.output(worked + " statements completed successfully during script execution");
 			Utils.output(errors + " statements generated errors during script execution");
 		}
 		
 		return errors;
 	}
 	
 	public static boolean runSql(Connection conn, String sql) { return runSql(conn, sql, FORMAT.XML); }
 	public static boolean runSql(Connection conn, String sql, FORMAT format) { return runSql(conn, sql, format, null); }
 	public static boolean runSql(Connection conn, String sql, FORMAT format, String outputTo)
 	{
 		boolean success = false;
 		
 		try
 		{
 			Statement stmt = conn.createStatement();
 			logger.debug("Running: " + sql);
 			boolean hasResults = stmt.execute(sql);
 			logger.debug("Ran sql");
 			
 			if ((format.compareTo(DbUtils.FORMAT.NO_OUTPUT) != 0) && (hasResults == true))
 			{
 				ResultSet rs = stmt.getResultSet();
 				outputResultSet(rs, outputTo, format);
 				try { rs.close(); } catch (Exception e) {}
 			}
 			
 			stmt.close();
 			success = true;
 		}
 		catch (SQLException e)
 		{
 			success = false;
 		}
 		catch (IOException e)
 		{
 			success = false;
 		}
 		catch (ClassNotFoundException e)
 		{
 			success = false;
 		}
 		return success;
 	}
 	
 	/**
 	 * Execute SQL and return the result set
 	 * @param conn DB Connection
 	 * @param sql SQL to execute
 	 * @return ResultSet for the SQL
 	 */
 	public static ResultSet getResultSet(Connection conn, String sql)
 	{
 		ResultSet rs = null;
 		
 		try
 		{
 			Statement stmt = conn.createStatement();
 			logger.debug("Running: " + sql);
 			rs = stmt.executeQuery(sql);
 		}
 		catch (SQLException e)
 		{
 			rs = null;
 		}
 		return rs;
 	}
 	
 	/**
 	 * Output the contents of a ResultSet
 	 * @param rs ResultSet to output
 	 * @param file Full path to write output to. Null if output to STDOUT
 	 * @param format How to show the data (delimited, xml, etc)
 	 * @throws SQLException
 	 * @throws IOException
 	 * @throws ClassNotFoundException
 	 */
 	public static void outputResultSet(ResultSet rs, String file, FORMAT format) throws SQLException, IOException, ClassNotFoundException
 	{		
 		if (rs != null)
 		{
 			StringBuffer sb = new StringBuffer();
 			ResultSetMetaData md = rs.getMetaData();
 			int cols = md.getColumnCount();
 			
 			String separator = null;
 			if (format.compareTo(DbUtils.FORMAT.CSV) == 0) { separator = ","; }
 			if (format.compareTo(DbUtils.FORMAT.TAB) == 0) { separator = "\t"; }
 			if (format.compareTo(DbUtils.FORMAT.RAW) == 0) { separator = ""; }
 			
 			// START: Output header
 			if  (
 				(format.compareTo(DbUtils.FORMAT.XML) != 0) &&
 				(format.compareTo(DbUtils.FORMAT.RAW) != 0)
 				)
 			{
 				// Output column headers
 				for (int i = 1; i <= cols; i++) // ResultSet columns are 1 (not 0) based
 				{
 					sb.append(md.getColumnLabel(i) + separator);
 				}
 				sb.append("\n");
 			}
 			else if (format.compareTo(DbUtils.FORMAT.XML) == 0)
 			{
 				sb.append("<results>\n");
 			}
 			// END: Output header
 			
 			// START: Output data
 			while (rs.next() == true)
 			{
 				if (format.compareTo(DbUtils.FORMAT.XML) == 0) { sb.append("<result row=\"" + rs.getRow() + "\">\n"); }
 				for (int i = 1; i <= cols; i++) // ResultSet columns are 1 (not 0) based
 				{
 					if (format.compareTo(DbUtils.FORMAT.XML) == 0)
 					{
 						sb.append("    <" + md.getColumnLabel(i) + ">" + rs.getString(i) + "</" + md.getColumnLabel(i) + ">\n");
 					}
 					else
 					{
 						sb.append(rs.getString(i) + separator);
 					}
 				}
 				if (format.compareTo(DbUtils.FORMAT.XML) == 0) { sb.append("</result>"); }
 				sb.append("\n");
 			}
 			// END: Output data
 			
 			// Output footer
 			if (format.compareTo(DbUtils.FORMAT.XML) == 0) { sb.append("</results>\n"); }
 			
 			// Final output
 			if ((file == null) || (file.equalsIgnoreCase("STDOUT") == true) || (file.length() == 0))
 			{
 				Utils.output(sb.toString());
 			}
 			else
 			{
 				FileUtils.writeStringToFile(file, sb.toString(), false);
 			}
 		}
 	}
 	
 	/** 
 	 * @param conn DB Connection
 	 * @param sql SQL to get back 1 row with 1 field that is a BLOB
 	 * @return The contents of the BLOB as an Object
 	 * @throws DbException 
 	 */
 	public static Object getObjectSerializedInBlob(Connection conn, String sql) throws DbException
 	{
 		Object returnVal = null;
 		try
 		{
 			Statement stmt = conn.createStatement();
 			boolean hasResults = stmt.execute(sql);
 			if (hasResults == true)
 			{
 				ResultSet rs = stmt.getResultSet();
 				while (rs.next() == true)
 				{
 					byte[] buf = rs.getBytes(1);
 				    ObjectInputStream objectIn = null;
 				    if (buf != null)
 				    {
 				    	objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
 				    }
 				    returnVal = objectIn.readObject();
 				}
 			}
 		}
 		catch (SQLException e)
 		{
 			throw new DbException("SQLException - " + e.getMessage(), e);
 		}
 		catch (IOException e)
 		{
 			throw new DbException("IOException - " + e.getMessage());
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw new DbException("ClassNotFoundException - " + e.getMessage());
 		}
 		return returnVal;
 	}
 	
 	public static boolean validFormat(String valueToCheck)
     {
     	try
     	{
     		FORMAT.valueOf(valueToCheck);
     		return true;
     	}
     	catch (IllegalArgumentException e)
     	{
     		return false;
     	}
     }
 	
 	/**
 	 * Convert a ArrayList of Strings to a SQL IN clause values ex: 'val 1', 'val 2', 'something else'
 	 * @param vals ArrayList of strings
 	 * @param stringList true if you want values surrounded by single quotes
 	 * @return String containing SQL IN clause values
 	 */
 	public static String toInClausItems(ArrayList<String> vals, boolean stringList)
 	{
 		StringBuffer retVal = new StringBuffer();
 		String valPrefix = "";
 		String valSuffix = "";
 		int counter = 0;
 		
 		if (stringList == true)
 		{
 			valPrefix = "\'";
 			valSuffix = "\'";
 		}
 		
 		for (String val : vals)
 		{
 			counter++;
 			retVal.append(valPrefix);
 			retVal.append(val);
 			retVal.append(valSuffix);
 			if (counter < vals.size())
 			{
 				retVal.append(", ");
 			}
 		}
 		
 		return retVal.toString();
 	}
 	
 	public static String getDriver(String type)
 	{
 		if (isMySql(type) == true)
 		{
 			return DbUtils.DRIVER_MYSQL;
 		}
 		else if (isDb2JCC(type) == true)
 		{
 			return DbUtils.DRIVER_DB2_JCC;
 		}
 		else if (isDb2App(type) == true)
 		{
 			return DbUtils.DRIVER_DB2_APP;
 		}
 		else if (isSqlServer(type) == true)
 		{
 			return DbUtils.DRIVER_SQL_SERVER_2005;
 		}
 		else if (isOdbc(type) == true)
 		{
 			return DbUtils.DRIVER_ODBC;
 		}
 		else if (isSqlLite(type) == true)
 		{
 			return DbUtils.DRIVER_SQLITE;
 		}
 		else
 		{
 			return "";
 		}
 	}
 
 	public static String getUrl(String type, String host, String db)
 	{
 		return getUrl(type, host, db, 0);
 	}
 
 	public static String getUrl(String type, String host, String db, int port)
 	{
 		if (isMySql(type) == true)
 		{
 			if (port <= 0)
 			{
 				port = getDefaultPort(type);
 			}
 			return "jdbc:mysql://" + host + ":" + port + "/" + db;
 		}
 		else if (isDb2JCC(type) == true)
 		{
 			return "jdbc:db2://" + host + ":" + port + "/" + db;
 		}
 		else if (isDb2App(type) == true)
 		{
 			return "jdbc:db2:" + db;
 		}
 		else if (isSqlServer(type) == true)
 		{
 			if (port <= 0)
 			{
 				port = getDefaultPort(type);
 			}
 			return "jdbc:microsoft:sqlserver://" + host + ":" + port + ";databaseName=" + db;
 		}
 		else if (isOdbc(type) == true)
 		{
 			return "jdbc:odbc:" + db;
 		}
 		else if (isSqlLite(type) == true)
 		{
 			return "jdbc:sqlite:" + db;
 		}
 		else
 		{
 			return "";
 		}
 	}
 	
 	public static int getDefaultPort(String dbType)
 	{
 		if (dbType == null)
 		{
 			return 0;
 		}
 		else if (isMySql(dbType) == true)
 		{
 			return 3306;
 		}
 		else if (isSqlServer(dbType) == true)
 		{
 			return 1443;
 		}
 		else if (isDb2(dbType) == true)
 		{
 			return 50000;
 		}
 		else
 		{
 			return 0;
 		}
 	}
 	
 	public static Connection getConnection(String driver, String url, String user, String pass) throws DbException, InfrastructureException
 	{	
 		try
 		{
 			logger.debug("Creating new connection\n   Driver: {}\n   Url: {}\n   user: {}", driver, url, user);
 			Class.forName(driver);
 			return DriverManager.getConnection(url, user, pass);
 		}
 		catch (SQLException e)
 		{
 			throw new DbException(e);
 		}
 		catch (ClassNotFoundException e)
 		{
 			throw new InfrastructureException("ERROR: " + url + ": ClassNotFoundException (check that the driver is on the CLASSPATH): " + e.getMessage(), e);
 		}
 	}
 	
	public static Connection getConnectionJNDI(String dsn) throws InfrastructureException, DbException { return getConnectionJNDI(dsn, "java:comp/env"); }
	public static Connection getConnectionJNDI(String dsn, String initContext) throws InfrastructureException, DbException
 	{
 		logger.debug("Getting connection: {}", dsn);
 		
 		try
 		{
 			Context initialContext = new InitialContext();
			Context envCtx = (Context)initialContext.lookup(initContext);
			DataSource datasource = (DataSource)envCtx.lookup(dsn);
 			if (datasource != null) {
 		        return datasource.getConnection();
 			}
 			else
 			{
 				throw new InfrastructureException("Unable to get JNDI connection: " + dsn);
 			}
 		}
 		catch (NamingException e)
 		{
 			throw new InfrastructureException("NamingException looking up JNDI connection: " + dsn);
 		}
 		catch (SQLException e)
 		{
 			throw new DbException("SQLException getting JNDI connection: " + dsn);
 		}
 	}
 
 	public static ResultSet execQuery(Connection conn, String sql) throws SQLException
 	{
 		Statement stmt = conn.createStatement();
 		return stmt.executeQuery(sql);
 	}
 
 	public static int getRecordCount(ResultSet rs) throws SQLException
 	{
 		int count = 0;
 
 		while (rs.next())
 		{
 			count++;
 		}
 
 		return count;
 	}
 	
 	public static String getListTablesSql(String dbType)
 	{
 		if (isDb2(dbType) == true)
 		{
 			 return DbUtils.SQL_LIST_TABLES_DB2;
 		}
 		else if (isSqlServer(dbType) == true)
 		{
 			return DbUtils.SQL_LIST_TABLES_SQLSERVER;
 		}
 		else if (isMySql(dbType) == true)
 		{
 			return DbUtils.SQL_LIST_TABLES_MYSQL;
 		}
 		else
 		{
 			return null;
 		}
 	}	
 	
 	public static String getTestSql(String dbType)
 	{
 		if (isDb2(dbType) == true)
 		{
 			 return DbUtils.SQL_TEST_DB2;
 		}
 		else if (isSqlServer(dbType) == true)
 		{
 			return DbUtils.SQL_TEST_SQLSERVER;
 		}
 		else
 		{
 			return null;
 		}
 	}	
 	
 	public static boolean isOdbc(String dbType)
 	{
 		if (dbType.equalsIgnoreCase(DbUtils.TYPE_ODBC) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}	
 	
 	public static boolean isMySql(String dbType)
 	{
 		if (dbType.equalsIgnoreCase(DbUtils.TYPE_MYSQL) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}	
 	
 	public static boolean isSqlServer(String dbType)
 	{
 		if (dbType.equalsIgnoreCase(DbUtils.TYPE_SQL_SERVER) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}	
 	
 	public static boolean isSqlLite(String dbType)
 	{
 		if (dbType.equalsIgnoreCase(DbUtils.TYPE_SQLITE) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}	
 	
 	public static boolean isDb2(String dbType)
 	{
 		if 
 		(
 			(isDb2Misc(dbType) == true) ||
 			(isDb2App(dbType) == true) ||
 			(isDb2JCC(dbType) == true)
 		)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}	
 	
 	public static boolean isDb2Misc(String dbType)
 	{
 		if (dbType.equalsIgnoreCase(DbUtils.TYPE_DB2) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}	
 	
 	public static boolean isDb2JCC(String dbType)
 	{
 		if (dbType.equalsIgnoreCase(DbUtils.TYPE_DB2_JCC) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	public static boolean isDb2App(String dbType)
 	{
 		if (dbType.equalsIgnoreCase(DbUtils.TYPE_DB2_APP) == true){
 			return true;
 		}else{
 			return false;
 		}
 	}
 }
