 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.nodomain.volkerk.JDBCOverlayLib;
 
 import com.sun.rowset.CachedRowSetImpl;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sql.rowset.CachedRowSet;
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import org.nodomain.volkerk.JDBCOverlayLib.helper.*;
 
 /**
  *
  * @author volker
  */
 abstract public class JDBC_GenericDB {
     
     protected static final int MYSQL_DEFAULT_PORT = 3306;
     
     /**
      * A logger for error messages
      */
     private final static Logger LOGGER = Logger.getLogger("JDBC Overlay");
 
     /**
      * The connection object for the database handled by this class
      */
     protected Connection conn;
     
     /**
      * The user name for the database server
      */
     protected String dbUser;
     
     /**
      * The password for the database server
      */
     protected String dbPasswd;
     
     /**
      * The name of the database to be accessed
      */
     protected String dbName;
 
     /**
      * An enum to identify the database type for the connection
      */
     public enum DB_ENGINE {
         SQLITE,
         MYSQL
     };
     
     /**
      * The database type for the connection
      */
     protected DB_ENGINE dbType;
     
     /**
      * The name of the server to connect to
      */
     protected String dbServer;
     
     /**
      * The server port to connect to
      */
     protected int dbPort;
     
     /**
      * A counter for executed queries; for debugging purposes only
      */
     protected long queryCounter = 0;
 
 //----------------------------------------------------------------------------
     
     /**
      * Creates a new database instance and connects to the DBMS
      * 
      * @param t the database engine type
      * @param srv the name of the database server or null for DBMS-specific default
      * @param port the port of the database server to connect to or 0 for DBMS-specific default
      * @param name the database name to open; the database must exist
      * @param user the user name for the database connection (null if not applicable)
      * @param pw the password for the database connection (null if not applicable)
      */
     public JDBC_GenericDB(DB_ENGINE t, String srv, int port, String name, String user, String pw) throws SQLException {
         conn = null;
         dbType = t;
         dbName = name;
         dbServer = srv;
         dbUser = user;
         dbPasswd = pw;
         dbPort = port;
         
         Properties connProps = new Properties();
         
         // build the connection string
         String connStr = "";
         if (t == DB_ENGINE.MYSQL)
         {
             try {
                 // Load the driver... SHOULDN'T be necessary anymore, but anyway...
                 Class.forName("com.mysql.jdbc.Driver");
             } catch (ClassNotFoundException ex) {
                 Logger.getLogger(JDBC_GenericDB.class.getName()).log(Level.SEVERE, null, ex);
             }
             
             connProps.put("user", dbUser);
             connProps.put("password", dbPasswd);
             
             // apply defaults
             if ((dbServer == null) || (dbServer.equals(""))) dbServer = "localhost";
             if (dbPort == 0) dbPort = MYSQL_DEFAULT_PORT;
             
             connStr = "jdbc:mysql://" + dbServer + ":" + dbPort + "/" + dbName;
         }
         else if (t == DB_ENGINE.SQLITE)
         {
             throw new NotImplementedException();
         }
         
         // get the connection
         try
         {
             conn = DriverManager.getConnection(connStr, connProps);
         }
         catch (SQLException ex)
         {
             throw new IllegalArgumentException("Invalid connection parameters for database! Details: " + ex.getMessage());
         }
         
         // create tables and views
         populateTables();
         populateViews();
     }
     
 //----------------------------------------------------------------------------
 
     /**
      * Close the database connection and free all resources
      */
     public void close()
     {
         if (conn != null)
         {
             try
             {
                 conn.close();
             }
             catch (SQLException ex)
             {
                 log(Level.SEVERE, null, ex);
             }
             conn = null;
         }
     }
 
 //----------------------------------------------------------------------------
 
     /**
      * To be overridden by child classes for the actual table creation upon object initialization
      */
     abstract protected void populateTables() throws SQLException;
 
 //----------------------------------------------------------------------------
     
     /**
      * To be overridden by child classes for the actual view creation upon object initialization
      */
     abstract protected void populateViews() throws SQLException;
     
 //----------------------------------------------------------------------------
     
     /**
      * Executes a SQL-Query which returns no data
      * 
      * @param baseSqlStmt the SQL statement with placeholders ("?")
      * @param params the objects to fill the placeholders
      * 
      * @return the number of affected rows
      * 
      * @throws SQLException 
      */
     public int execNonQuery(String baseSqlStmt, Object ... params) throws SQLException
     {
         queryCounter++;
         
         if ((params == null) || (params.length == 0))
         {
             try {
                 return conn.createStatement().executeUpdate(baseSqlStmt);
             }
             catch (SQLException e) {
                 log(Level.SEVERE, "ExecNonQuery failed: ", "\n",
                         "QUERY: ", baseSqlStmt, "\n",
                         "ERROR: ", e.getMessage());
                 throw e;
             }
         }
         
         try (PreparedStatement st = prepStatement(baseSqlStmt, params))
         {
             return st.executeUpdate();
         }
         catch (SQLException e)
         {
             log(Level.SEVERE, "ExecNonQuery failed: ", "\n",
                     "QUERY: ", baseSqlStmt, "\n",
                     "ERROR: ", e.getMessage());
             throw e;
         }
     }
     
 //----------------------------------------------------------------------------
 
     /**
      * Executes a SQL-Query which returns a complete ResultSet as data
      * 
      * @param baseSqlStmt the SQL statement with placeholders ("?")
      * @param params the objects to fill the placeholders
      * 
      * @throws SQLException
      * 
      * @return the CachedRowSet object for the retrieved data
      */
     public CachedRowSet execContentQuery(String baseSqlStmt, Object ... params) throws SQLException
     {
         ResultSet rs = null;
         CachedRowSet result;
         
         if ((params == null) || (params.length == 0))
         {
             try {
                 result = new CachedRowSetImpl();
                 Statement st = conn.createStatement();
                 rs = st.executeQuery(baseSqlStmt);
                 result.populate(rs);
                 return result;
             }
             catch (SQLException e) {
                 log(Level.SEVERE, "ExecContentQuery with no parameters failed: ", "\n",
                         "QUERY: ", baseSqlStmt, "\n",
                         "ERROR: ", e.getMessage());
                 throw e;
             }
         }
         
         try (PreparedStatement st = prepStatement(baseSqlStmt, params))
         {
             result = new CachedRowSetImpl();
             rs = st.executeQuery();
             result.populate(rs);
         }
         catch (SQLException e)
         {
             log(Level.SEVERE, "ExecContentQuery failed: ", "\n",
                     "QUERY: ", baseSqlStmt, "\n",
                     "ERROR: ", e.getMessage());
             throw e;
         }
         
         queryCounter++;
         
         return result;
     }
     
 //----------------------------------------------------------------------------
 
     /**
      * Executes a SQL-Query which returns a single value in the first column of the first row of the query result
      * 
      * @param baseSqlStmt the SQL statement with placeholders ("?")
      * @param params the objects to fill the placeholders
      * 
      * @throws SQLException 
      * 
      * @return the retrieved object or null
      */
     public Object execScalarQuery(String baseSqlStmt, Object ... params) throws SQLException
     {
         ResultSet rs = null;
         
         if ((params == null) || (params.length == 0))
         {
             rs = conn.createStatement().executeQuery(baseSqlStmt);
             
             if (!(rs.first()))
             {
                 log("Scalar query returned no data!");
                 return null;
             }
             
             return rs.getObject(1);
         }
         
         try (PreparedStatement st = prepStatement(baseSqlStmt, params))
         {
             rs = st.executeQuery();
             queryCounter++;
             
             if (!(rs.first()))
             {
                 log("Scalar query returned no data!");
                 return null;
             }
             
             return rs.getObject(1);
         }
         catch (SQLException e)
         {
             log(Level.SEVERE, "ExecContentQuery failed: ", "\n",
                     "QUERY: ", baseSqlStmt, "\n",
                     "ERROR: ", e.getMessage());
             throw e;
         }
     }
     
 //----------------------------------------------------------------------------
 
     /**
      * Executes a SQL-Query which returns a single int in the first column of the first row of the query result
      * 
      * @param baseSqlStmt the SQL statement with placeholders ("?")
      * @param params the objects to fill the placeholders
      * 
      * @throws SQLException 
      * 
      * @return the retrieved Integer or null
      */
     public Integer execScalarQueryInt(String baseSqlStmt, Object ... params) throws SQLException
     {
         Object o = execScalarQuery(baseSqlStmt, params);
         
         if (o == null) return null;
         
         return Integer.parseInt(o.toString());
     }
     
 //----------------------------------------------------------------------------
 
     protected PreparedStatement prepStatement(String baseSqlStmt, Object ... params) throws SQLException
     {
         PreparedStatement result = conn.prepareStatement(baseSqlStmt);
         
         for (int i = 0; i < params.length; i++)
         {
             if ((params[i] != null) && ((params[i].getClass().isEnum())))
             {
                 result.setObject(i+1, params[i].toString());
             }
             else
             {
                 result.setObject(i+1, params[i]);
             }
         }
         
         return result;
     }
 
 //----------------------------------------------------------------------------
     
     /**
      * Logs a message with "INFO" level
      * @param msg arbitrary list of objects (e. g. strings) to log
      */
     protected void log(Object ... msg)
     {
         log(Level.INFO, helper.strCat(msg));
     }
     
 //----------------------------------------------------------------------------
     
     /**
      * Logs a message with custom level
      * @param msg arbitrary list of objects (e. g. strings) to log
      */
     protected void log(Level lvl, Object ... msg)
     {
         LOGGER.log(lvl, helper.strCat(msg));
     }
     
 //----------------------------------------------------------------------------
     
     /**
      * Helper function for easy table creation, e. g. from within populateTables
      * 
      * @param tabName contains the name of the table to be created
      * @param colDefs is a list of column definitions for this table
      */
     public void tableCreationHelper(String tabName, List<String> colDefs) throws SQLException
     {
         String sql = "CREATE TABLE IF NOT EXISTS " + tabName + " (";
         sql += "id INTEGER NOT NULL PRIMARY KEY ";
         
         if (dbType == DB_ENGINE.MYSQL) sql += "AUTO_INCREMENT";
         else  sql += "AUTOINCREMENT";
         
         sql += ", " + helper.commaSepStringFromList(colDefs);
         
         sql += ");";
        System.err.println(sql);
         execNonQuery(sql);
     }
     
 //----------------------------------------------------------------------------
     
     /**
      * Helper function for easy table creation, e. g. from within populateTables
      * 
      * @param tabName contains the name of the table to be created
      * @param colDefs is a list of column definitions for this table
      */
     public void tableCreationHelper(String tabName, String ... colDefs) throws SQLException
     {
         tableCreationHelper(tabName, Arrays.asList(colDefs));
     }
 	
 //----------------------------------------------------------------------------
     
     /**
      * Helper function for easy view creation, e. g. from within populateViews
      * 
      * @param viewName contains the name of the view to be created
      * @param selectStmt is the sql-select-statement for this view
      */    
     public void viewCreationHelper(String viewName, String selectStmt) throws SQLException
     {
         String sql = "CREATE VIEW IF NOT EXISTS";
         if (dbType == DB_ENGINE.MYSQL) sql = "CREATE OR REPLACE VIEW";
         
         sql += " " + viewName + " AS ";
         sql += selectStmt;
         execNonQuery(sql);
     }
 
 //----------------------------------------------------------------------------
     
     /**
      * Return a list of all views or all tables in the database
      * 
      * @param getViews must be set to true to return view names; table names otherwise
      * @return an ArrayList of strings with names
      */
     public List<String> allTableNames(boolean getViews)
     {
         String sql = "";
         
         if (dbType == DB_ENGINE.SQLITE)
         {
             String tableType = getViews ? "view" : "table";
 
             sql = "SELECT * FROM sqlite_master WHERE type='";
             sql += tableType + "'";
         }
         else if (dbType == DB_ENGINE.MYSQL)
         {
             String tableType = getViews ? "VIEW" : "BASE TABLE";
             sql = "SHOW FULL TABLES WHERE TABLE_TYPE LIKE '" + tableType + "'";
         }
         
         ArrayList<String> result = new ArrayList<>();
         
         // SHOW TABLES etc. doesn't work with cached results, so
         // we have to do the query manually here
         try
         {
             ResultSet rs = conn.createStatement().executeQuery(sql);
             
             // in SQLITE, the second column contains the table name and
             // in MYSQL, the first column contains the table name and
             int resultCol;
             if (dbType == DB_ENGINE.SQLITE) resultCol = 2;
             else resultCol = 1;
         
             while (rs.next())
             {
                 String n = rs.getString(resultCol);
 
                 // for sqlite, the list contains one internal table, which we skip
                 if ((dbType == DB_ENGINE.SQLITE) && (n.startsWith("sqlite_"))) continue;
 
                 result.add(n);
             }
         }
         catch (Exception e)
         {
             genericExceptionHandler(e, "Get all table names failed, SQL = ", sql);
         }
         
         return result;
     }
     
 //----------------------------------------------------------------------------
     
     /**
      * Returns a list of all tables in the database
      * 
      * @return the table names as ArrayList
      */
     public List<String> allTableNames()
     {
         return allTableNames(false);
     }
 	
 //----------------------------------------------------------------------------
     
     /**
      * Returns a list of all views in the database
      * 
      * @return the view names as ArrayList
      */
     public List<String> allViewNames()
     {
         return allTableNames(true);
     }
 	
 //----------------------------------------------------------------------------
     
     /**
      * Generic exception handler which simply terminates the program
      * 
      * @param e the exception that occured
      * @param objs additional info (e. g. strings) which will be added to the output
      */
     protected void genericExceptionHandler(Exception e, Object ... objs)
     {
         log(Level.SEVERE, objs);
         log(Level.SEVERE, "Terminating with fatal exception:\n",
                 e.getMessage(), objs);
         
         try
         {
             // try to close the database to avoid data corruption
             conn.close();
         }
         catch (SQLException ex) {}
         
         // Unconditional exit
         //System.exit(42);
     }
 	
 //----------------------------------------------------------------------------
     
     /**
      * Get the current query counter
      * @return the current query counter
      */
     public long getQueryCounter()
     {
         return queryCounter;
     }
 	
 //----------------------------------------------------------------------------
     
     /**
      * Reset the query counter to zero
      */
     public void resetQueryCounter()
     {
         queryCounter = 0;
     }
 	
 //----------------------------------------------------------------------------
     
     /**
      * Checks whether the DB contains a specific view or table
      * 
      * @param name is the name of the view / table to look for
      * @param isView must be set to true if "name" refers to a view
      * @return true if the database contains the table / view named "name"
      */
     public boolean hasTableOrView(String name, boolean isView)
     {
         for (String n : allTableNames(isView))
         {
             if (n.equals(name)) return true;
         }
         return false;
     }
 	    
 //----------------------------------------------------------------------------
     
     /**
      * Checks whether the DB contains a specific table
      * 
      * @param tabName is the name of the table to look for
      * @return true if the database contains the table named "name"
      */
     public boolean hasTable(String tabName)
     {
         return hasTableOrView(tabName, false);
     }
 	
 //----------------------------------------------------------------------------
     
     /**
      * Checks whether the DB contains a specific view
      * 
      * @param viewName is the name of the view to look for
      * @return true if the database contains the view named "name"
      */
     public boolean hasView(String viewName)
     {
         return hasTableOrView(viewName, true);
     }    
 
 //----------------------------------------------------------------------------
     
     /**
      * Helper function to create a FOREIGN-KEY-statement for CREATE TABLE purposes
      * 
      * @param keyName the of the column which will reference to the other table
      * @param referedTable the name of the table to which id-column we will refer to
      * @return the string with the FOREIGN KEY statement
      */
     public static String genForeignKeyClause(String keyName, String referedTable)
     {
         //string sql = "FOREIGN KEY (";
         //sql += keyName;
         //sql += ") REFERENCES ";
         //sql += referedTable;
         //sql += "(id)";
         //return sql;
        return keyName + " INTEGER, FOREIGN KEY (" + keyName + ") REFERENCES " + referedTable + "(id)";
     }
 	
 //----------------------------------------------------------------------------
     
     public DB_ENGINE getEngineType()
     {
         return dbType;
     }
 
 //----------------------------------------------------------------------------
    
     public int getLastInsertRowId() throws SQLException
     {
         String sql = "SELECT ";
         
         if (dbType == DB_ENGINE.SQLITE) sql += "last_insert_rowid()";
         else sql += "LAST_INSERT_ID()";
         
         return execScalarQueryInt(sql);
     }
 
 //----------------------------------------------------------------------------
     
     /**
      * Returns the instance of a database table
      * 
      * @param tabName the name of the table to access
      * @return the SqliteTable instance of that table
      */
     public JDBC_Tab t(String tabName)
     {
         return new JDBC_Tab(this, tabName);
     }    
 
 //----------------------------------------------------------------------------
 
     /**
      * Returns the instance of a database view
      * 
      * @param viewName the name of the view to access
      * @return the SqliteView instance of that view
      */
     public JDBC_View v(String viewName)
     {
         return new JDBC_View(this, viewName);
     }
 
 //----------------------------------------------------------------------------
 
     /**
      * Shortcut to return a table-row-column cell content as String
      * 
      * @param tabName the name of the table to look-up
      * @param rowId the ID of the row to look up
      * @param colName the name of column which contains the value
      * @return the cell content as String
      */
     public String trc(String tabName, int rowId, String colName) throws SQLException
     {
         TabRow r = new TabRow(this, tabName, rowId);
         return r.c(colName);
     }
 
 //----------------------------------------------------------------------------
 
     /**
      * Shortcut to return a table-row-column cell content as String
      * 
      * @param tabName the name of the table to look-up
      * @param rowId the ID of the row to look up
      * @param colName the name of column which contains the value
      * @return the cell content as Integer
      */
     public Integer trcInt(String tabName, int rowId, String colName) throws SQLException
     {
         TabRow r = new TabRow(this, tabName, rowId);
         return r.asInt(colName);
     }
 
 //----------------------------------------------------------------------------
 
     /**
      * Shortcut to return a table-row-column cell content as Boolean
      * 
      * @param tabName the name of the table to look-up
      * @param rowId the ID of the row to look up
      * @param colName the name of column which contains the value
      * @return the cell content as Boolean
      */
     public Boolean trcBool(String tabName, int rowId, String colName) throws SQLException
     {
         TabRow r = new TabRow(this, tabName, rowId);
         return r.asBool(colName);
     }
 
 //----------------------------------------------------------------------------
     
 
 //----------------------------------------------------------------------------
     
 
 //----------------------------------------------------------------------------
     
 
 //----------------------------------------------------------------------------
     
 
 //----------------------------------------------------------------------------
     
     
 }
